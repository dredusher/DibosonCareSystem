package com.usher.diboson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.support.v4.app.NotificationCompat;

public class TextToSpeechService extends Service implements OnInitListener,OnUtteranceCompletedListener

{
	// =============================================================================
	// 08/02/2014 ECU created to handle text to speech
	//            ECU changed to use an ArrayList in case I want to buffer phrases in
	//                the future
	// 12/02/2014 ECU changed to use onStartCommand rather than onStart
	// 20/02/2014 ECU change to use the SpokenPhrase class
	//            ECU create method to be able to insert some silence
	// 04/03/2016 ECU changed to use a handler rather than a thread
	// 22/05/2016 ECU if music is being played then pause it while text is being
	//                spoken then resume it afterwards
	//            ECU added the utterance completed listener (deprecated at API 18
	//                but need to support earlier versions)
	// =============================================================================
	final static String TAG		=	"TextToSpeechService";
	// =============================================================================
	private static final boolean	WAKELOCK_ENABLED = false;		// 11/04/2015 ECU moved here from variable
	// =============================================================================
	public static boolean			ready = false;			
	// =============================================================================
	static	SpokenPhrase			currentPhraseToSpeak;			// 20/02/2014 ECU added
	static  boolean					musicPaused			= false;	// 22/05/2016
	static	boolean					noInitialWelcome	= false;	// 25/03/2016 ECU added
			NotificationManager		notificationManager;			// 09/02/2014 ECU added
	static	List<SpokenPhrase> 		phrasesToSpeak 		= new ArrayList<SpokenPhrase> ();
	static 	PowerManager			powerManager;	
			SpeechHandler 			speechHandler;					// 04/03/2016 ECU added
	static	HashMap<String, String> speechParams = new HashMap<String, String>();
																	// 22/05/2016 ECU added
	static  TextToSpeech			textToSpeech 		= null;
			PowerManager.WakeLock	wakeLock 			= null;		// 12/02/2014 ECU added
	// =============================================================================
	@Override
	public IBinder onBind (Intent theIntent)
	{
		// -------------------------------------------------------------------------
		return null;
	}
	// =============================================================================
	@Override
	public void onCreate()
	{
		// -------------------------------------------------------------------------
		// 12/10/215 ECU added the super call
		// -------------------------------------------------------------------------
		super.onCreate ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================	
	@Override
	public int onStartCommand (Intent intent,int flags,int startId) 
	{
		// -------------------------------------------------------------------------
		// 25/03/2016 ECU check if any arguments passed through
		// 02/04/2016 ECU put in a check on 'null' intent which could happen if
		//                restarted by the OS
		// -------------------------------------------------------------------------
		if (intent != null)
		{
			Bundle extras = intent.getExtras ();
 		
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 25/03/2016 ECU MainActivity passes through whether it was started
				//                by an alarm interrupt - if it has then do not want to
				//                speak the welcome message
				// -----------------------------------------------------------------
				noInitialWelcome = extras.getBoolean (StaticData.PARAMETER_ALARM_START,false);
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 08/02/2014 ECU create the text to speech object
		// -------------------------------------------------------------------------
		textToSpeech = new TextToSpeech (this,this);
		// -------------------------------------------------------------------------
		// 09/02/2014 ECU check whether to run the service in the foreground
		//                do this if the speaking clock is enabled
		// -------------------------------------------------------------------------
		if (PublicData.storedData.speakingClock.enabled)
			StartServiceInForeground ();
		// -------------------------------------------------------------------------
		// 22/02/2014 ECU set up the power manager which will be used for monitoring
		//                the screen
		// -------------------------------------------------------------------------
		powerManager = (PowerManager) getSystemService (POWER_SERVICE);
		// -------------------------------------------------------------------------
		// 04/03/2016 ECU declare the handler
		// -------------------------------------------------------------------------
		speechHandler = new SpeechHandler ();
		// -------------------------------------------------------------------------
		// 08/02/2014 ECU add a welcoming message
		// 11/02/2014 ECU added some more text
		// 26/01/2015 ECU changed to use string resources
		// 06/10/2015 ECU only announce the 'welcome' if the app has been started
		//                manually
		// 25/03/2016 ECU put in the check on noInitialWelcome
		// -------------------------------------------------------------------------
		if (PublicData.startedManually && !noInitialWelcome)
		{	
			SpeakAPhrase (getString (R.string.welcome_to_care_system));
			SpeakAPhrase (getString (R.string.the_aim_is));
			SpeakAPhrase (getString (R.string.try_and_relax));
			// ---------------------------------------------------------------------
			// 18/10/2015 ECU indicate the status of the remote device
			// ---------------------------------------------------------------------
			if (PublicData.blueToothService)
			{
				SpeakAPhrase (getString (R.string.remote_device_available));
			}
			// ---------------------------------------------------------------------
			// 04/06/2016 ECU check if there is a network - if none then display an
			//                informative message
			// ---------------------------------------------------------------------
			if (!Utilities.checkForNetwork (this))
			{
				// -----------------------------------------------------------------
				// 04/06/2016 ECU no network so tell the user
				// -----------------------------------------------------------------
				SpeakAPhrase (getString (R.string.no_network));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------		
		// 12/02/2014 ECU indicate service to be restarted - added after changing to
		//                onStartCommand
		// -------------------------------------------------------------------------
		return Service.START_STICKY;
	}
	// =============================================================================
	@Override
	public void onDestroy() 
	{
		// -------------------------------------------------------------------------
		// 08/02/2014 ECU if the text-to-speech engine is running then
		//                close it down
		// -------------------------------------------------------------------------
		if (textToSpeech != null) 
		{
			textToSpeech.stop ();
			textToSpeech.shutdown ();
			// ---------------------------------------------------------------------
			// 10/02/2014 ECU add the null to prevent some warnings on isSpeaking
			// ---------------------------------------------------------------------
			textToSpeech = null;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 04/03/2016 ECU indicate no longer ready
		// -------------------------------------------------------------------------
		ready = false;
		// -------------------------------------------------------------------------
		// 12/02/2014 ECU release the wake lock
		//			  ECU put in a check on 'null'
		// 23/02/2014 ECU changed to use the enabled flag
		// 11/04/2015 ECU changed to use 'WAKE...' rather than a variable 'wake...'
		// -------------------------------------------------------------------------
		if (WAKELOCK_ENABLED)
			wakeLock.release();
		// -------------------------------------------------------------------------	
		super.onDestroy();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onInit (int theStatus) 
	{	
		// -------------------------------------------------------------------------
		// 08/02/2014 ECU only interested in text-to-speech engine events
		// -------------------------------------------------------------------------
		if (theStatus == TextToSpeech.SUCCESS) 
		{
			// ---------------------------------------------------------------------
			//            ECU Setting speech language
			// 08/02/2014 ECU changed from Locale.UK
			// --------------------------------------------------------------------- 		
			int result = textToSpeech.setLanguage (Locale.getDefault());
			// ---------------------------------------------------------------------  
			// 09/01/2017 ECU Note - If your device doesn't support language you set above
			// ---------------------------------------------------------------------  
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) 
			{
				// -----------------------------------------------------------------
				// 08/11/2013 ECU use the custom toast
				// 11/04/2015 ECU changed to put to log file rather than LogCat
				//            ECU changed to use the resource
				// -----------------------------------------------------------------	
				Utilities.LogToProjectFile (TAG,getString (R.string.default_language_not_supported),true);
			} 
			else
			{
				// -----------------------------------------------------------------	
				// 08/02/2014 ECU it appears that the engine has initialised successfully
	        	//                so start the handler
				// 04/04/2014 ECU start up the thread - was previously a Handler
				// 26/03/2015 ECU put in the check on whether the threads are already alive
				//                This seems to happen if the app is restarted very quickly 
				//                after it has been destroyed
				// 04/03/2016 ECU changed to start up the handler
				// -----------------------------------------------------------------
				speechHandler.sleep (1000);
				// -----------------------------------------------------------------
				// 22/05/2016 ECU set up the parameters that will be passed with
				//                each spoken phrase
				// -----------------------------------------------------------------
				speechParams.put (TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,StaticData.SPOKEN_PHRASE_COMPLETED);
				// -----------------------------------------------------------------
				// 22/05/2016 ECU set up the 'utterance' listener
				// -----------------------------------------------------------------
				textToSpeech.setOnUtteranceCompletedListener (this); 
				// -----------------------------------------------------------------	
				// 08/02/2014 ECU indicate that the service is ready to process phrases
				// -----------------------------------------------------------------	
				ready = true;
				// -----------------------------------------------------------------
				// 04/03/2016 ECU indicate that service is ready for use
				// -----------------------------------------------------------------
				Utilities.LogToProjectFile (TAG, getString (R.string.ready_to_go));
				// -----------------------------------------------------------------
			}
		} 
		else 
		{
			// ---------------------------------------------------------------------
			// 08/11/2013 ECU use the custom toast
			// 11/04/2015 ECU changed to put to log file rather than LogCat
			// ---------------------------------------------------------------------	
			Utilities.LogToProjectFile (TAG,getString (R.string.tts_initialisation_failed),true);
			// ---------------------------------------------------------------------
		}
	};
	// =============================================================================
	@Override
	public void onUtteranceCompleted (String theUtteranceID) 
	{
		// -------------------------------------------------------------------------
		// 22/05/2016 ECU as only this service is responsible for getting the engine 
		//                the actual ID is irrelevant but for completeness then
		//                check the supplied value
		// -------------------------------------------------------------------------
		if (theUtteranceID.equals (StaticData.SPOKEN_PHRASE_COMPLETED))
		{
			// ---------------------------------------------------------------------
			// 22/05/2016 ECU check if needed to resume the music player
			// ---------------------------------------------------------------------
			if (musicPaused)
			{
				// -----------------------------------------------------------------
				// 22/05/2016 ECU indicate that no longer paused
				// ------------------------------------------------------------------
				musicPaused 			= false;
				// -----------------------------------------------------------------
				// 22/05/2016 ECU get the music player to 'resume'
				// -----------------------------------------------------------------
				MusicPlayer.playOrPause (true);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 03/06/2016 ECU if all of the text has been spoken then
			//           	  tell any listeners
			//            ECU add the check on the action flag
			// ---------------------------------------------------------------------
			if (phrasesToSpeak.size() == 0 && currentPhraseToSpeak.actionFlag)
			{
				PublicData.messageHandler.sendEmptyMessage (StaticData.MESSAGE_ACTION_FINISHED);
			}
			// ---------------------------------------------------------------------	
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void Flush ()
	{
		// -------------------------------------------------------------------------
		// 27/11/2015 ECU created to clear the queue and stop anything that is 
		//                running
		// -------------------------------------------------------------------------
		textToSpeech.stop ();
		phrasesToSpeak.clear ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public boolean Speaking ()
	{
		// -------------------------------------------------------------------------
		// 20/02/2014 ECU return the state of the TTS engine 
		//
		//                true .... is currently speaking
		//                false ... is not speaking
		// -------------------------------------------------------------------------
		return textToSpeech.isSpeaking ();
	}
	// =============================================================================
	public static void Silence (int theDuration)
	{
		// -------------------------------------------------------------------------
		// 20/02/2014 ECU created to insert a period of silence into the current chain.
		//                The period is specified in 'milliseconds'
		// -------------------------------------------------------------------------
		phrasesToSpeak.add (new SpokenPhrase (theDuration));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SpeakAPhrase (String thePhraseToSpeak)
	{
		// -------------------------------------------------------------------------
		// 08/02/2014 ECU add this phrase into the list
		// 20/02/2014 ECU change to use SpokenPhrase
		// -------------------------------------------------------------------------
		phrasesToSpeak.add (new SpokenPhrase (thePhraseToSpeak));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SpeakAPhrase (String thePhraseToSpeak,boolean theActionFlag)
	{
		// -------------------------------------------------------------------------
		// 03/06/2016 ECU created to pass the action flag through
		// -------------------------------------------------------------------------
		phrasesToSpeak.add (new SpokenPhrase (thePhraseToSpeak,theActionFlag));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static class SpeechHandler extends Handler
    {
		// -------------------------------------------------------------------------
		// 05/01/2016 ECU created as a message handler
		// -------------------------------------------------------------------------
        @Override
        public void handleMessage (Message theMessage) 
        {   
        	// ---------------------------------------------------------------------
        	// 05/05/2015 ECU change to switch on the type of message received
        	//                which is in '.what'
        	// ---------------------------------------------------------------------
        	switch (theMessage.what)
        	{
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_SLEEP:
        			// -------------------------------------------------------------
        			// 10/01/2016 ECU a local message to get the server terminated
        			// -------------------------------------------------------------
        			// -------------------------------------------------------------
					// 08/02/2014 ECU check if ready to start talking
					// 10/02/2014 ECU added in the null check
					// -------------------------------------------------------------
					if (textToSpeech != null && !textToSpeech.isSpeaking())
					{
						// ---------------------------------------------------------
						// 08/02/2014 ECU engine is not speaking so can starting to talk
						//            ECU only process one phrase per loop to give a natural
						//                break between phrases
						// ---------------------------------------------------------
						if (phrasesToSpeak.size() > 0)
						{	
							// -----------------------------------------------------
							// 20/02/2014 ECU get the object at the top of the list
							// -----------------------------------------------------
							currentPhraseToSpeak = phrasesToSpeak.get (0);
							// -----------------------------------------------------			
							// 08/02/2014 ECU get the engine to speak the required phrase
							// 20/02/2014 ECU change to use SpokenPhrase class
							//            ECU decide whether the object is for 'spoken text' or
							//                a 'period of silence'.
							// -----------------------------------------------------
							if (currentPhraseToSpeak.silence == StaticData.NO_RESULT)
							{
								// -------------------------------------------------
								// 20/02/2014 ECU the object contains a phrase to be
								//                spoken
								// -------------------------------------------------
								// 22/05/2016 ECU if music is playing then pause it
								//                while the phrase is spoken
								// -------------------------------------------------
								if (PublicData.mediaPlayer != null)
								{
									if (PublicData.mediaPlayer.isPlaying() && !musicPaused)
									{
										// -----------------------------------------
										// 22/05/2016 ECU music is playing so pause
										//                it and indicate the fact
										// -----------------------------------------
										musicPaused 			= true;
										// -----------------------------------------------------------------
										// 22/05/2016 ECU get the music player to 'pause'
										// -----------------------------------------------------------------
										MusicPlayer.playOrPause (false);
										// -----------------------------------------------------------------
									}
								}
								// -------------------------------------------------
								// 22/05/2016 ECU add the params so that can detect
								//                when the spoken phrase has ended
								// -------------------------------------------------
								textToSpeech.speak (currentPhraseToSpeak.phrase,TextToSpeech.QUEUE_ADD,speechParams);
							}
							else
							{
								// -------------------------------------------------
								// 20/02/2014 ECU the object contains a request to 
								//                insert a period of silence in
								//                milliseconds
								// -------------------------------------------------
								textToSpeech.playSilence (currentPhraseToSpeak.silence,TextToSpeech.QUEUE_ADD,null);
							}
							// -----------------------------------------------------
							// 08/02/2014 ECU delete this phrase from the list
							// -----------------------------------------------------
							phrasesToSpeak.remove (0);
							// -----------------------------------------------------
						}
					}
					// -------------------------------------------------------------	
					// 12/02/2014 ECU wait a short period of time
					// 04/03/2016 ECU keep looping until the flag indicates that the
					//                service is stopping
					// -------------------------------------------------------------
					if (textToSpeech != null)
						sleep (200);
        			// -------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        	}
        	// ---------------------------------------------------------------------
        }
        /* ------------------------------------------------------------------------- */
        public void sleep (long delayMillis)
        {	
        	// ---------------------------------------------------------------------
        	// 06/01/2016 ECU changed to use MESSAGE_SLEEP instead of 0
        	// ---------------------------------------------------------------------
            this.removeMessages (StaticData.MESSAGE_SLEEP);
            sendMessageDelayed(obtainMessage (StaticData.MESSAGE_SLEEP), delayMillis);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    };
	// =============================================================================
	void StartServiceInForeground ()
	{
		// -------------------------------------------------------------------------
		// 09/02/2014 ECU try and place service in the foreground so that it runs
		//                even when the device is in standby mode
		// -------------------------------------------------------------------------
		// 16/03/2016 ECU changed to use the notification builder
		// -------------------------------------------------------------------------
		NotificationCompat.Builder notificationBuilder =
			    			new NotificationCompat.Builder (this)
			    						.setSmallIcon (R.drawable.text_icon)
			    						.setContentTitle (TAG)
			    						.setStyle(new NotificationCompat.BigTextStyle().bigText (PublicData.storedData.speakingClock.Summary()));
		// -------------------------------------------------------------------------
		// 26/06/2013 ECU specify the activity to be started if the user selects it
		//                from the list of notifications
		// -------------------------------------------------------------------------
		PendingIntent resultPendingIntent = PendingIntent.getActivity (this,
																	   0,
																	   new Intent (this,TimerEventActivity.class),
																	   PendingIntent.FLAG_UPDATE_CURRENT);
		notificationBuilder.setContentIntent (resultPendingIntent);
		// ---------------------------------------------------------------------
		// 09/02/2014 ECU put the service into the foreground
		// 26/03/2016 ECU changed to use the static data
		// -------------------------------------------------------------------------
		startForeground (StaticData.NOTIFICATION_TTS_SERVICE, notificationBuilder.build());
		// -------------------------------------------------------------------------
		// 12/02/2014 ECU try and get the wake lock
		// 23/02/2014 ECU handle wakelock if enabled
		// 11/04/2015 ECU changed to use 'WAKE...' rather than a variable 'wake...'
		// -------------------------------------------------------------------------
		if (WAKELOCK_ENABLED)
		{	
			PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
			// ---------------------------------------------------------------------
			// 12/02/2014 ECU try and acquire the wake lock
			// ---------------------------------------------------------------------
			wakeLock.acquire();
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================	
}


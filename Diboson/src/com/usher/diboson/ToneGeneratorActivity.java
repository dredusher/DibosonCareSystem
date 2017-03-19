package com.usher.diboson;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.TextView;

public class ToneGeneratorActivity extends DibosonActivity 
{
	// =============================================================================
	// 09/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// =============================================================================
	
	// =============================================================================
	static Context 				context;
	static int					duration 	= 1000;
	static int					tone 		= 440;
	static ToneData 			toneData 	= null;
	static ToneRefreshHandler 	toneRefreshHandler;
	static Tones				tones;
	static TextView				toneTextView;
	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		// 09/10/2015 ECU want to check if the activity is being newly created or
		//                just recreated having been destroyed by Android
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been created 'anew'
			// ---------------------------------------------------------------------
			// 15/08/2015 ECU set up common aspects of the activity
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity(this,StaticData.ACTIVITY_FULL_SCREEN);
			// ---------------------------------------------------------------------
			setContentView(R.layout.activity_tone_generator);
			// ---------------------------------------------------------------------
			// 15/08/2015 ECU get the textview that will be used to display messages
			// ---------------------------------------------------------------------
			toneTextView = (TextView) findViewById (R.id.tone_generator_textview);
			// ---------------------------------------------------------------------
			// 15/08/2015 ECU remember the context for later use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 15/08/2015 ECU set up the handler of messages
			// ---------------------------------------------------------------------
			toneRefreshHandler = new ToneRefreshHandler ();
			// ---------------------------------------------------------------------
			// 18/08/2015 ECU check if tones have been provided
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
			// ---------------------------------------------------------------------
			// 18/08/2015 ECU check if tones have been supplied with the intent
			// ---------------------------------------------------------------------
			if (extras != null)
			{
				// -----------------------------------------------------------------
				// 15/08/2015 ECU try and get the supplied tones
				// -----------------------------------------------------------------
 	   			tones  =  (Tones) extras.getSerializable (StaticData.PARAMETER_TONES); 
 	   			if (tones != null)
 	   			{
 	   				toneTextView.setText ("Playing supplied tones");
 	   				toneData = new ToneData (tones,Utilities.createAMethod (ToneGeneratorActivity.class,"ToneFinished"));
 	   				toneData.Play();
 	   			}
 	   			// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 15/08/2015 ECU start up the tone generator dialogue
				// -----------------------------------------------------------------
				toneRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_TONE);
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been recreated after being destroyed
			//                by Android so just exit this activity
			// ---------------------------------------------------------------------
			finish ();
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return true;
	}
	// =============================================================================
	@Override
	public void onDestroy()
	{	
		// -------------------------------------------------------------------------
		// 15/08/2015 ECU called when activity is being destroyed
		// -------------------------------------------------------------------------
		super.onDestroy();
	}
	/* ============================================================================ */
	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event) 
	{
		// -------------------------------------------------------------------------
		// 20/02/2014 ECU added to process the back key
		// -------------------------------------------------------------------------
	    if( keyCode == KeyEvent.KEYCODE_BACK) 
	    {	
	    	// ---------------------------------------------------------------------
	    	// 15/08/2015 ECU stop playing any tone before exiting
	    	// ---------------------------------------------------------------------
	    	if (toneData != null)
	    	{
	    		toneData.StopPlaying();
	    		toneData = null;
	    	}
	    	// ---------------------------------------------------------------------
	    	finish ();
	    }    
	    return super.onKeyDown(keyCode, event);   
	}
	// =============================================================================
	@Override 
	protected void onPause () 
	{ 
		// -------------------------------------------------------------------------
		super.onPause(); 
	} 	
	// =============================================================================
	
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class ToneRefreshHandler extends Handler
    {
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
        		case StaticData.MESSAGE_DURATION:
        			// -------------------------------------------------------------
        			// 15/08/2015 ECU get the tone from the message
        			// -------------------------------------------------------------
        			tone = (Integer) theMessage.obj;
        			// -------------------------------------------------------------
        			// 18/08/2015 ECU added the 'cancel' button legend
        			// 06/03/2016 ECU added the '1' as the minimum value
        			// -------------------------------------------------------------
        			DialogueUtilities.sliderChoice (context,
        					"Select the duration (in mS)",
        					"Please enter the duration, which is in milliSeconds, that you want the tone to sound.",
        					R.drawable.timer,
        					null,
        					duration,
        					1,
        					StaticData.TONE_DURATION_MAXIMUM,
        					getString(R.string.click_to_set_duration),
        					Utilities.createAMethod (ToneGeneratorActivity.class,"ToneDuration",0),
        					getString(R.string.cancel_operation));
                    // -------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_TONE:
        			// -------------------------------------------------------------
        			// 18/08/2015 ECU added the 'cancel' button legend
        			// 06/03/2016 ECU added TONE_FREQUENCY_MINIMUM as the minimum value
        			// -------------------------------------------------------------
        			DialogueUtilities.sliderChoice (context,
        					"Select the tone (in Hz)",
        					"Please enter the frequency, which is in Hz, of the tone that is to be generated.",
        					R.drawable.tone_generator,
        					null,
        					tone,
        					StaticData.TONE_FREQUENCY_MINIMUM,
        					StaticData.TONE_FREQUENCY_MAXIMUM,
        					getString(R.string.click_to_set_tone),
        					Utilities.createAMethod (ToneGeneratorActivity.class,"ToneChange",0),
        					getString(R.string.cancel_operation));
        			// -------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_SLEEP:
        			// -------------------------------------------------------------
        			// 05/05/2015 ECU put any code that is the result of a 'sleep'
        			//                here
        			// -------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        	}
        	// ---------------------------------------------------------------------
        }
        /* ------------------------------------------------------------------------- */
        public void sleep(long delayMillis)
        {	
        	// ---------------------------------------------------------------------
        	// 05/05/2015 ECU changed to use MESSAGE_SLEEP instead of 0
        	// ---------------------------------------------------------------------
            this.removeMessages (StaticData.MESSAGE_SLEEP);
            sendMessageDelayed(obtainMessage (StaticData.MESSAGE_SLEEP), delayMillis);
        }
    };
	// =============================================================================
	
	// =============================================================================
	public static void ToneDone ()
	{
		toneRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_TONE);
	}
	// =============================================================================
	public static void ToneChange (int theTone)
	{
		Message message = toneRefreshHandler.obtainMessage (StaticData.MESSAGE_DURATION);
		message.obj 	= theTone;
		toneRefreshHandler.sendMessage (message);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void ToneDuration (int theDuration)
	{
		duration = theDuration;
		// -------------------------------------------------------------------------
		// 15/08/2015 ECU update the status message
		// -------------------------------------------------------------------------
		toneTextView.setText (String.format(context.getString(R.string.tone_generator_message),tone,duration));
		// -------------------------------------------------------------------------
		toneData = new ToneData (tone,theDuration,Utilities.createAMethod (ToneGeneratorActivity.class,"ToneDone"));
		toneData.Play();
	}
	// =============================================================================
	public static void ToneFinished ()
	{
		((Activity)context).finish ();
	}
	// =============================================================================
}

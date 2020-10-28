package com.usher.diboson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ToneGeneratorActivity extends DibosonActivity 
{
	// =============================================================================
	// 09/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 13/12/2018 ECU remove the static use of context
	// 08/08/2019 ECU added the mosquito handler
	// 17/03/2020 ECU added the metronome option
	// 18/03/2020 ECU changed the layout to use buttons to trigger the tone
	//                generator and metronome
	// 23/06/2020 ECU removed 'onKeyDown' and replaced with 'onBackPressed'
	// =============================================================================
	
	// =============================================================================
		   	Context 			context;
		   	int					duration 	= 1000;
		   	Metronome			metronome;
			ImageView			metronomeImageView;
		   	int					metronomeMeasuredBeats;				// 17/03/2020 ECU added
			int					metronomeTempo;						// 17/03/2020 ECU added
		   	boolean				mosquito	= false;
		   	int					tone 		= 440;
		   	ToneData 			toneData 	= null;
	static 	ToneRefreshHandler 	toneRefreshHandler;
		   	Tones				tones;
	static  TextView			toneTextView;
		   	Object				underlyingObject;
	// =============================================================================
	@Override
	protected void onCreate (Bundle savedInstanceState) 
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
			// 18/03/2020 ECU set up the tone generator button listener
			// ---------------------------------------------------------------------
			((Button) findViewById (R.id.tone_generator_button)).setOnClickListener (new View.OnClickListener()
			{
				// -----------------------------------------------------------------
				@Override
				public void onClick (View view)
				{
					// -------------------------------------------------------------
					// 18/03/2020 ECU if a metronome is running then stop it
					// -------------------------------------------------------------
					metronomeStop ();
					// -------------------------------------------------------------
					// 15/08/2015 ECU start up the tone generator dialogue
					// -------------------------------------------------------------
					toneRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_TONE);
					// -------------------------------------------------------------
				}
				// -------------------------------------------------------------
			});
			// -----------------------------------------------------------------
			// 17/03/2020 ECU resize the metronome image and set the listener
			// -----------------------------------------------------------------
			metronomeImageView = (ImageView) findViewById (R.id.metronome_image);
			RelativeLayout.LayoutParams layoutparams = (RelativeLayout.LayoutParams) metronomeImageView.getLayoutParams();
			layoutparams.height	= PublicData.screenHeight / 4;
			layoutparams.width 	= PublicData.screenWidth / 4;
			metronomeImageView.setLayoutParams(layoutparams);
			// -----------------------------------------------------------------
			((Button) findViewById (R.id.metronome_button)).setOnClickListener (new View.OnClickListener()
			{
				// -------------------------------------------------------------
				@Override
				public void onClick (View view)
				{
					// ---------------------------------------------------------
					// 17/03/2020 ECU metronome
					// ---------------------------------------------------------
					metronomeBPM ();
					// ---------------------------------------------------------
				}
				// -------------------------------------------------------------
			});
			// ---------------------------------------------------------------------
			// 15/08/2015 ECU get the textview that will be used to display messages
			// ---------------------------------------------------------------------
			toneTextView = (TextView) findViewById (R.id.tone_generator_textview);
			// ---------------------------------------------------------------------
			// 15/08/2015 ECU remember the context for later use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 13/12/2018 ECU remember the underlying object for later use
			// ---------------------------------------------------------------------
			underlyingObject = this;
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
 	   			// -----------------------------------------------------------------
 	   			if (tones != null)
 	   			{
 	   				// -------------------------------------------------------------
 	   				// 08/08/2019 ECU decide if 'mosquito mode' is on or off
 	   				//            ECU the '== 0' is needed for the very first time
 	   				//                the app is installed
 	   				// -------------------------------------------------------------
 	   				if ((PublicData.storedData.mosquitoFrequency == 0) ||
 	   					(PublicData.storedData.mosquitoFrequency == StaticData.NOT_SET))
 	   				{
 	   					// ---------------------------------------------------------
 	   					toneTextView.setText (getString (R.string.tones_supplied));
 	   					// ---------------------------------------------------------
 	   					// 08/08/2019 ECU not in mosquito mode so just play the tones
 	   					// ---------------------------------------------------------
 	   					toneData = new ToneData (tones,Utilities.createAMethod (ToneGeneratorActivity.class,"ToneFinished"));
 	   					// ---------------------------------------------------------
 	   				}
 	   				else
 	   				{
 	   					// ---------------------------------------------------------
 	   					// 08/08/2019 ECU in mosquito mode so play the associated 
 	   					//                tone
 	   					// ---------------------------------------------------------
 	   					toneTextView.setText (getString (R.string.tones_mosquito));
 	   					// ---------------------------------------------------------
 	   					// 21/06/2020 ECU added 'gap' - previously just used
 	   					//                the 'duration'
 	   					// ---------------------------------------------------------
 	   					toneData = new ToneData (PublicData.storedData.mosquitoFrequency,
 	   											 PublicData.storedData.mosquitoDuration,
 	   											 PublicData.storedData.mosquitoGap,
 	   										     StaticData.MOSQUITO_REPEATS,
 	   										     Utilities.createAMethod (ToneGeneratorActivity.class,"ToneFinished"));
 	   					// ---------------------------------------------------------
 	   				}
 	   				// -------------------------------------------------------------
 	   				// 08/08/2019 ECU Note - play the defined tone sequence
 	   				// -------------------------------------------------------------
 	   				toneData.Play ();
 	   				// -------------------------------------------------------------
 	   			}
 	   			// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
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
		// 08/08/2019 ECU make sure that the screen display is refreshed
		// -------------------------------------------------------------------------
		GridActivity.gridRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_ADAPTER);
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public void onBackPressed()
	{
		// -------------------------------------------------------------------------
		// 20/02/2014 ECU added to process the back key
		// 21/06/2020 ECU Note - could use 'onBackPressed' but this was not available
		//                       when the code was originally written
		// 23/06/2020 ECU changed to 'onBackPressed'
		// -------------------------------------------------------------------------
	   	// 15/08/2015 ECU stop playing any tone before exiting
	    // -------------------------------------------------------------------------
	    if (toneData != null)
	    {
	    	// ---------------------------------------------------------------------
	    	toneData.StopPlaying ();
	    	toneData = null;
	    	// ---------------------------------------------------------------------
	    }
		// -------------------------------------------------------------------------
		// 17/03/2020 ECU check if the metronome is to be stopped
		// 18/03/2020 ECU use the new method
		// -------------------------------------------------------------------------
		metronomeStop ();
	    // -------------------------------------------------------------------------
	    finish ();
	    // -------------------------------------------------------------------------
	    // 23/06/2020 ECU call the main method
	    // -------------------------------------------------------------------------
		super.onBackPressed ();
		// -------------------------------------------------------------------------
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
        			// 13/12/2018 ECU changed to use '..NonStatic'
        			// 08/08/2019 ECU changed to include 'mosquito' option
        			// 21/06/2020 ECU changed to use resources
        			// -------------------------------------------------------------
        			DialogueUtilitiesNonStatic.sliderChoice (context,
        													 underlyingObject,
        													 context.getString (R.string.tone_generator_duration_title),
        													 context.getString (R.string.tone_generator_duration_summary),
        													 R.drawable.timer,
        													 null,
        													 duration,
        													 1,
        													 (mosquito ? StaticData.TONE_DURATION_MAXIMUM_MOSQUITO 
        															   : StaticData.TONE_DURATION_MAXIMUM),
        													 getString (R.string.click_to_set_duration),
        													 Utilities.createAMethod (ToneGeneratorActivity.class,"ToneDuration",0),
        													 getString (R.string.cancel_operation),
        													 Utilities.createAMethod (ToneGeneratorActivity.class,"CancelOperation",0));
                    // -------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_TONE:
        			// -------------------------------------------------------------
        			// 08/08/2019 ECU make sure default to 'non mosquito'
        			// -------------------------------------------------------------
        			mosquito = false;
        			// -------------------------------------------------------------
        			// 19/03/2020 ECU clear the tone display
        			// -------------------------------------------------------------
        			toneTextView.setText (StaticData.BLANK_STRING);
        			// -------------------------------------------------------------
        			// 18/08/2015 ECU added the 'cancel' button legend
        			// 06/03/2016 ECU added TONE_FREQUENCY_MINIMUM as the minimum value
        			// 13/12/2018 ECU changed to use '..NonStatic'
        			// 10/08/2019 ECU add the final 'true' flag to indicate that
        			//                the dialogue can be cancelled
        			// 21/06/2020 ECU changed to use resources
        			// -------------------------------------------------------------
        			DialogueUtilitiesNonStatic.sliderChoice (context,
        					                                 underlyingObject,
        					                                 context.getString (R.string.tone_generator_frequency_title),
        					                                 context.getString (R.string.tone_generator_frequency_summary),
        					                                 R.drawable.tone_generator,
        					                                 null,
        					                                 tone,
        					                                 StaticData.TONE_FREQUENCY_MINIMUM,
        					                                 StaticData.TONE_FREQUENCY_MAXIMUM,
        					                                 getString (R.string.click_to_set_tone),
        					                                 Utilities.createAMethod (ToneGeneratorActivity.class,"ToneChange",0),
        					                                 getString (R.string.mosquito_title),
        					                                 Utilities.createAMethod (ToneGeneratorActivity.class,"Mosquito",0),
        					                                 true);
        			// -------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_FINISH:
        			// -------------------------------------------------------------
        			// 13/12/2018 ECU added to 'finish' this activity
        			// -------------------------------------------------------------
        			finish ();
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
        public void sleep (long delayMillis)
        {	
        	// ---------------------------------------------------------------------
        	// 05/05/2015 ECU changed to use MESSAGE_SLEEP instead of 0
        	// ---------------------------------------------------------------------
            this.removeMessages (StaticData.MESSAGE_SLEEP);
            sendMessageDelayed (obtainMessage (StaticData.MESSAGE_SLEEP), delayMillis);
            // ---------------------------------------------------------------------
        }
    };
	// =============================================================================
	
	
	// =============================================================================
	public void CancelOperation (int theDuration)
	{
		// -------------------------------------------------------------------------
		// 08/08/2019 ECU created to handle the 'cancel operation'
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void Mosquito (int theFrequency)
	{
		// -------------------------------------------------------------------------
		// 08/08/2019 ECU indicate that this is a mosquito operation
		// -------------------------------------------------------------------------
		mosquito = true;
		// -------------------------------------------------------------------------
		// 13/12/2018 ECU changed from 'static'
		// -------------------------------------------------------------------------
		Message message = toneRefreshHandler.obtainMessage (StaticData.MESSAGE_DURATION);
		message.obj 	= theFrequency;
		toneRefreshHandler.sendMessage (message);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void ToneChange (int theTone)
	{
		// -------------------------------------------------------------------------
		// 13/12/2018 ECU changed from 'static'
		// -------------------------------------------------------------------------
		Message message = toneRefreshHandler.obtainMessage (StaticData.MESSAGE_DURATION);
		message.obj 	= theTone;
		toneRefreshHandler.sendMessage (message);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void ToneDone ()
	{
		// -------------------------------------------------------------------------
		toneRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_TONE);
		// -------------------------------------------------------------------------
		// 19/03/2020 ECU clear the tone display
		// -------------------------------------------------------------------------
		toneTextView.setText (StaticData.BLANK_STRING);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void ToneDuration (int theDuration)
	{
		// -------------------------------------------------------------------------
		duration = theDuration;
		// -------------------------------------------------------------------------
		// 15/08/2015 ECU update the status message
		// 13/12/2018 ECU changed from 'static'
		// -------------------------------------------------------------------------
		toneTextView.setText (String.format (getString(R.string.tone_generator_message),tone,duration));
		// -------------------------------------------------------------------------
		// 08/08/2019 ECU check if trying to set up a 'mosquito operation'
		// -------------------------------------------------------------------------
		if (!mosquito)
		{
			// ---------------------------------------------------------------------
			// 08/08/2019 ECU ensure that the stored details are reset
			// ---------------------------------------------------------------------
			PublicData.storedData.mosquitoFrequency = StaticData.NOT_SET;
			// ---------------------------------------------------------------------
			toneData = new ToneData (tone,theDuration,Utilities.createAMethod (ToneGeneratorActivity.class,"ToneDone"));
			// ---------------------------------------------------------------------
			// 08/08/2019 ECU Note - play the defined tone
			// 21/06/2020 ECU moved here
			// ---------------------------------------------------------------------
			toneData.Play ();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 08/08/2019 ECU ensure that the stored details are set
			// ---------------------------------------------------------------------
			PublicData.storedData.mosquitoDuration  = theDuration;
			PublicData.storedData.mosquitoFrequency = tone;
			// ---------------------------------------------------------------------
			// 21/06/2020 ECU request the gap between 'mosquito blips'
			// ---------------------------------------------------------------------
			DialogueUtilitiesNonStatic.sliderChoice (context,
					                                 underlyingObject,
													 context.getString (R.string.tone_generator_gap_title),
													 context.getString (R.string.tone_generator_gap_summary),
													 R.drawable.timer,
													 null,
													 (PublicData.storedData.mosquitoGap != StaticData.NOT_SET) ? PublicData.storedData.mosquitoGap
													                                                           : theDuration,
													 1,
													 StaticData.TONE_DURATION_MAXIMUM_MOSQUITO,
													 getString (R.string.click_to_set_duration),
													 Utilities.createAMethod (ToneGeneratorActivity.class,"ToneGapDuration",0),
													 getString (R.string.cancel_operation),
													 Utilities.createAMethod (ToneGeneratorActivity.class,"CancelOperation",0));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void ToneFinished ()
	{
		// -------------------------------------------------------------------------
		// 13/12/2018 ECU changed from 'static'
		//            ECU send message to 'finish' this activity
		// -------------------------------------------------------------------------
		toneRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_FINISH);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void ToneGapDuration (int theGapDuration)
	{
		// -------------------------------------------------------------------------
		// 21/06/2020 ECU set the gap between 'mosquito blips'
		// -------------------------------------------------------------------------
		PublicData.storedData.mosquitoGap = theGapDuration;
		// -------------------------------------------------------------------------
		toneData = new ToneData (tone,
								 PublicData.storedData.mosquitoDuration,
								 theGapDuration,
								 StaticData.MOSQUITO_REPEATS,
								 Utilities.createAMethod (ToneGeneratorActivity.class,"ToneDone"));
		// -------------------------------------------------------------------------
		// 21/06/2020 ECU Note - play the defined tone
		// -------------------------------------------------------------------------
		toneData.Play ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================

	// =============================================================================
	void metronomeBPM ()
	{
		// -------------------------------------------------------------------------
		// 17/03/2020 ECU request the tempo in 'beats per minute
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.sliderChoice (context,
												 underlyingObject,
												 getString (R.string.metronome_tempo_title),
												 getString (R.string.metronome_tempo_summary),
												 R.drawable.metronome,
												 null,
												 60,
												 10,
												 600,
												 getString (R.string.metronome_tempo_click),
												 Utilities.createAMethod (ToneGeneratorActivity.class,"metronomeTempoMethod",0),
												 null,
												 null,
												 true);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void metronomeTempoMethod (int theTempo)
	{
		// -------------------------------------------------------------------------
		// 17/03/2020 ECU store the entered tempo before requesting the 'measured
		//                beats
		// -------------------------------------------------------------------------
		metronomeTempo = theTempo;
		// -------------------------------------------------------------------------
		// 17/03/2020 ECU now request the 'measured beats'
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.sliderChoice (context,
										         underlyingObject,
												 getString (R.string.metronome_measured_beats_title),
												 getString (R.string.metronome_measured_beats_summary),
												 R.drawable.metronome,
												 null,
												 4,
												 2,
												 32,
												 getString (R.string.metronome_measured_beats_click),
												 Utilities.createAMethod (ToneGeneratorActivity.class,"metronomeMeasureBeatsMethod",0),
												 null,
												 null,
												 true);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void metronomeMeasureBeatsMethod (int theMeasuredBeats)
	{
		// -------------------------------------------------------------------------
		// 17/03/2020 ECU store the 'measured beats' before requesting the beats
		// -------------------------------------------------------------------------
		metronomeMeasuredBeats = theMeasuredBeats;
		// -------------------------------------------------------------------------
		// 17/03/2020 ECU now request the number of beats per measure
		// 22/03/2020 ECU changed to use 'listChoice'
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.listChoice (context,
												underlyingObject,
								      			getString (R.string.metronome_beats_title),
									  			Metronome.NOTE_VALUES,
									  			Utilities.createAMethod (ToneGeneratorActivity.class,"metronomeNoteValueMethod",0),
									  			getString (R.string.cancel),
									  			null);
		// ------------------------------------------------------------------------
	}
	// =============================================================================

	// =============================================================================
	public void metronomeNoteValueMethod (int theNoteValue)
	{
		// -------------------------------------------------------------------------
		// 17/03/2020 ECU get the entered beats
		//            ECU now start up the metronome with the input details
		// 18/03/2020 ECU make sure that any existing metronome has stopped
		// -------------------------------------------------------------------------
		metronomeStop ();
		metronome = new Metronome (metronomeTempo,metronomeMeasuredBeats,theNoteValue);
		// -------------------------------------------------------------------------
		// 17/03/2020 ECU indicate the image to be 'flashed' on each note
		// -------------------------------------------------------------------------
		metronome.setView ((View) metronomeImageView);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void metronomeStop ()
	{
		// -------------------------------------------------------------------------
		// 18/03/2020 ECU stop the metronome if it exists
		// -------------------------------------------------------------------------
		if (metronome != null)
		{
			// ---------------------------------------------------------------------
			metronome.stop ();
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
}

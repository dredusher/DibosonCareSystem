package com.usher.diboson;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class AudioAnalyser extends DibosonActivity
{
	/* ============================================================================= */
	// 19/10/2013 ECU created - this activity receives data from
	//                the microphone and processes it
	// 21/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 08/06/2016 ECU in doing tests found that there were problems
	//                with this activity hanging - when went back to
	//                Activity from DibosonActivity then things seem
	//                to be OK. Do not understand.
    //            ECU IMPORTANT - further investigation seems to 
	//                =========   indicate that the problem is with
	//                            the handling of the BACK key. Removed
	//                            the onKeyDown which was handling the
	//                            BACK key and added onBackPressed
	//                            but the problem persisted until
	//                            the call to super.onBackPressed
	//                            was removed - not sure why. Put
	//                            in some logging and the 
	//            ECU VERY IMPORTANT - further investigation seemed
	//                ==============   to indicate that there are
	//                                 memory leak issues when using
	//                                 an AsyncTask which runs for
	//                                 a long time so that it can
	//                                 carry on running even when
	//                                 the activity has been 'finished'.
	//                                 For this reason decided to change
	//                                 to a thread.
	// 20/11/2016 ECU rearrange the code so that a 'handler' is used
	//                instead of threads - makes the code cleaner
	//                and easier to understand. Also feel it helps
	//                with the synchronisation of the display and the
	//                processing
	//            ECU although have looked at speeding things up the
	//                biggest delay is the reading in of the data
	// 22/11/2016 ECU Note - some timings seem to show that acquiring
	//                       and processing data takes ~ 500 mS so
	//                       that get 2 processing cycles per second.
	/* ============================================================================= */
	//final static String 	TAG = "AudioAnalyser";
	/* ============================================================================= */
	// 19/01/2014 ECU some notes. The Nyquist-Shannon theorem says that
	//                in order to reproduce a signal then :-
	//				  the sampling rate must be greater than twice the
	//                maximum frequency of the signal.
	//
	// 				  The human hearing range is 20 Hz to 20 kHz so that
	//                a sampling rate of 44.1 KHz is chosen (this is the
	//                rate chosen for CDs).
	// -----------------------------------------------------------------------------
	public final static int		SAMPLING_RATE 	= 44100;		// 18/01/2014 ECU was 8000;
	public final static double  MAX_VALUE      	= (double) (Short.MAX_VALUE + 1);
	public final static double  MINIMUM_GAP     = 3000;			// 18/01/2014 ECU added
	/* ============================================================================= */
	public static int		bufferSize;							// 18/01/2014 ECU was 1024;
	public static int		frequencyEnd 	= SAMPLING_RATE / 2;// 18/01/2014 ECU added
	public static int		frequencyStart	= 0;				// 18/01/2014 ECU added
	public static double []	magnitudes 		= null;
	public static RefreshHandler 	
							refreshHandler;
	public static boolean   resetScale 		= false;
	public static float     resolution;	
	public static double	yMaximum 		= 0;
	/* ============================================================================= */
	static 	AudioRecord		audioRecord 	= null;
	static View  			dibosonView;
	static FastFourierTransform	
							fastFourierTransform;				// 18/11/2016 ECU added
	static TextView			frequencyField;						// 20/11/2016 ECU added
	static Complex [] 		sourceData;
	static Complex	[] 		transformedData; 
	static TextView			volumeField;						// 20/11/2016 ECU added
	/* ============================================================================= */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 21/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 28/11/2016 ECU call method to set the screen up
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true,true,false);
			// ---------------------------------------------------------------------
			// 20/11/2016 ECU changed from 'diboson_view' now that frequency and
			//                volume are being set here rather than in DibosonView
			// ---------------------------------------------------------------------
			setContentView (R.layout.audio_analyser); 
			// ---------------------------------------------------------------------
			dibosonView = findViewById (R.id.DibosonViewMain);
		
			dibosonView.setOnClickListener (viewClickListener);
			// ---------------------------------------------------------------------
			//20/11/2016 ECU set up the text fields that will be updated
			// ---------------------------------------------------------------------
			frequencyField 	= (TextView) findViewById (R.id.maximum_volume_frequency);
			volumeField 	= (TextView) findViewById (R.id.maximum_volume);
			// ---------------------------------------------------------------------
			// 18/11/2016 ECU initialise the fast fourier transform object
			// ---------------------------------------------------------------------
			fastFourierTransform = new FastFourierTransform ();
			// ---------------------------------------------------------------------
			// 20/11/2016 ECU initialise the audio record parameters
			// ---------------------------------------------------------------------
			initialiseAudioRecord ();
			// ---------------------------------------------------------------------
			// 20/11/2016 ECU declare the message handler that will synchronise the
			//                processing and display - put after initialiseAudioRecord
			//                which sets up some of the parameters that it needs
			// ---------------------------------------------------------------------
			refreshHandler = new RefreshHandler ();
			// ---------------------------------------------------------------------
			// 20/11/2016 ECU now start up the processing
			// ---------------------------------------------------------------------
			refreshHandler.sendEmptyMessage (StaticData.MESSAGE_PROCESS);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 21/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	@Override
	public void onBackPressed()
	{
		// -------------------------------------------------------------------------
		// 08/06/2016 ECU created to handled the 'back' key
	    // -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
		// 08/06/2016 ECU removed following the tests detailed at the top of this
		//                activity
		//            ECU NOTE - restored see the notes about AsyncTask at the top
		//                       of the activity
		// -------------------------------------------------------------------------
	    super.onBackPressed();			// 08/06/2016 ECU removed
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
	private View.OnClickListener viewClickListener = new View.OnClickListener() 
	{
		@Override
		public void onClick(View theView) 
		{
			resetScale = !resetScale;
		}
	};
	// ===========================================================================	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate (R.menu.audio_analyser, menu);
		
		return true;
	}
	/* ============================================================================= */
	@Override
    public void onDestroy()
    {	
		// -------------------------------------------------------------------------
		// 21/10/2015 ECU added
		// 08/06/2016 ECU changed from 'recording' to 'processing'
		// 20/11/2016 ECU changed to send message to indicate to close everything down
		// -------------------------------------------------------------------------
		refreshHandler.sendEmptyMessage (StaticData.MESSAGE_FINISH);
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
    }
	// =============================================================================
	public boolean onOptionsItemSelected (MenuItem item)
	{
		// -------------------------------------------------------------------------
		// 18/01/2014 ECU take the actions depending on which menu is selected
		// -------------------------------------------------------------------------
		switch (item.getItemId())
		{
			// ---------------------------------------------------------------------
			case R.id.frequency_increment_start:
				// -----------------------------------------------------------------
				// 18/01/2014 ECU added
				// -----------------------------------------------------------------
				IncrementFrequency ();
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case R.id.frequency_decrement_end:
				// -----------------------------------------------------------------
				// 18/01/2014 ECU added	
				// -----------------------------------------------------------------
				DecrementFrequency ();
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------	
			case R.id.frequency_reset:
				// -----------------------------------------------------------------
				// 18/01/2014 ECU added
				// -----------------------------------------------------------------
				frequencyStart = 0;
				frequencyEnd   = SAMPLING_RATE / 2;
				break;
			// ---------------------------------------------------------------------			
		}
		// -------------------------------------------------------------------------
		return true;
	}
	// =============================================================================
	public static void DecrementFrequency ()
	{
		// -------------------------------------------------------------------------
		// 11/04/2014 ECU added context as an argument
		// -------------------------------------------------------------------------
		if ((frequencyEnd - frequencyStart) > MINIMUM_GAP)
			frequencyEnd -= 1000;
		else
		{
			Utilities.popToast ("Have reached the minimum gap of " + MINIMUM_GAP + " Hz");
		}
	}
	// =============================================================================
	public static void IncrementFrequency ()
	{
		// --------------------------------------------------------
		// 11/04/2014 ECU added context as an argument
		// --------------------------------------------------------
		if ((frequencyEnd - frequencyStart) > MINIMUM_GAP)
			frequencyStart += 1000;
		else
		{
			Utilities.popToast ("Have reached the minimum gap of " + MINIMUM_GAP + " Hz");
		}
	}
	// =============================================================================
	void initialiseAudioRecord ()
	{
		// -------------------------------------------------------------------------
		// 19/10/2013 ECU it would appear that the AudioRecord.read returns half the 
		//                size of the specified buffer or the number of shorts 
		//                requested - whichever is the smaller.
		// -------------------------------------------------------------------------
		// 19/10/2013 ECU get the minimum size of buffer for the specified audio format
		// 19/01/2014 ECU change from 1 (which means use default) to CHANNEL_IN_MONO
		// -------------------------------------------------------------------------
		bufferSize =  AudioRecord.getMinBufferSize (SAMPLING_RATE,
													AudioFormat.CHANNEL_IN_MONO,
													AudioFormat.ENCODING_PCM_16BIT);
		// -------------------------------------------------------------------------
		// 24/10/2013 ECU want to set a minimumBufferSize that works with the fft 
		//                algorithm
		// -------------------------------------------------------------------------	
		if (!Utilities.isNumberAPowerOfTwo(bufferSize))
		{
			// ---------------------------------------------------------------------
			// 24/10/2013 ECU find the next even number that is a power of 2
			// ---------------------------------------------------------------------
			while (!Utilities.isNumberAPowerOfTwo(bufferSize))
				bufferSize += 2;
		}
		// -------------------------------------------------------------------------
		// 19/10/2013 ECU set the buffer that will receive the audio data
		// -------------------------------------------------------------------------

		// -------------------------------------------------------------------------
		// 19/10/2013 ECU create the AudioRecord with the required parameters
		// -------------------------------------------------------------------------
		audioRecord = new AudioRecord (MediaRecorder.AudioSource.MIC, 
									   SAMPLING_RATE,
									   AudioFormat.CHANNEL_IN_MONO,
									   AudioFormat.ENCODING_PCM_16BIT,
									   bufferSize);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void ProcessAudioBuffer (short [] theBuffer, int theNumberToProcess)
	{
		// -------------------------------------------------------------------------
		// 18/11/2016 ECU set the frequency resolution based on what was read
		// -------------------------------------------------------------------------
		resolution = ((float) SAMPLING_RATE)  / (float) theNumberToProcess;
		// -------------------------------------------------------------------------
		// 20/10/2013 ECU create complex input from the source data
		// -------------------------------------------------------------------------		
		sourceData = new Complex [theNumberToProcess];
		// -------------------------------------------------------------------------
		// 20/10/2013 ECU declare array for the transformed data
		// -------------------------------------------------------------------------
		transformedData  = new Complex [theNumberToProcess];
		// -------------------------------------------------------------------------
		// 20/10/2013 ECU build the input array for the fft
		// -------------------------------------------------------------------------
		for (int index = 0; index < theNumberToProcess; index++)
		{
			// ---------------------------------------------------------------------
			// 20/10/2013 ECU real part adjust for size of short, and no imaginary part
			// ---------------------------------------------------------------------
			sourceData [index] = new Complex ((double) theBuffer[index] / MAX_VALUE,0); 
		}
		// -------------------------------------------------------------------------
		// 20/10/2013 ECU perform the fast Fourier transform of the input data
		// 18/11/2016 ECU changed to use local object rather than statically
		// -------------------------------------------------------------------------
		transformedData = fastFourierTransform.fft (sourceData);
		// -------------------------------------------------------------------------
		// 20/10/2013 ECU build a magnitudes array from the fft output
		// -------------------------------------------------------------------------
		magnitudes = new double [theNumberToProcess/2];
		
		for (int index = 0; index < theNumberToProcess/2; index++) 
		{
			magnitudes[index] = transformedData[index].abs();
		}
		// -------------------------------------------------------------------------				
		// 20/10/2013 ECU want to invalidate the view so that data is redrawn
		//                but to get access to view then need to get to User
		//                Interface
		// -------------------------------------------------------------------------
		// 20/10/2013 ECU cause an onDraw to be actioned
		// -------------------------------------------------------------------------
		dibosonView.invalidate ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static class RefreshHandler extends Handler
    {
		// -------------------------------------------------------------------------
		// 20/11/2016 ECU declare the data used within this class
		// -------------------------------------------------------------------------
		short []   	inputBuffer = new short [bufferSize];
		int       	numberOfShortsRead;
		boolean		process		= true;
		// -------------------------------------------------------------------------
        @Override
        public void handleMessage (Message theMessage) 
        {   
        	switch (theMessage.what)
        	{
        		case StaticData.MESSAGE_FINISH:
        			// -------------------------------------------------------------
        			// 20/11/2016 ECU called when all processing is to finish
        			// -------------------------------------------------------------
        			process = false;
        			// -------------------------------------------------------------
        			// 20/11/2016 ECU Note - now stop and clear audio record resources
        			//                       if they have been allocated
        			// -------------------------------------------------------------
        			if (audioRecord != null)
        			{
        				// ---------------------------------------------------------
        				// 08/06/2016 ECU check if recording
        				// ---------------------------------------------------------
        				if (audioRecord.getState() == AudioRecord.RECORDSTATE_RECORDING)
        				{
        					//------------------------------------------------------
        					// 08/06/2016 ECU recording so stop it
        					// -----------------------------------------------------
        					audioRecord.stop ();
        				}
        				// ---------------------------------------------------------
        				// 08/06/2016 ECU now release the resources
        				// ---------------------------------------------------------
        				audioRecord.release ();
        				audioRecord = null;
        				// ---------------------------------------------------------
        			}
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_DATA:
        			// -------------------------------------------------------------
        			// 20/11/2016 ECU display the data and then drop through to
        			//                the normal processing
        			//            ECU arg1  =  the frequency with the maximum 'volume'
        			//                obj   =  the maximum 'volume' - a Double
        			// -------------------------------------------------------------
        			frequencyField.setText (String.format ("%.3f Hz",frequencyStart + (theMessage.arg1 * resolution)));
        			volumeField.setText    (String.format ("%.3f",((Double) theMessage.obj)));
        			// -------------------------------------------------------------
        			// 20/11/2016 ECU just drop through - do NOT add a break
        			// -------------------------------------------------------------
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_PROCESS:
        			// -------------------------------------------------------------
        			// 20/11/2016 ECU called to read a buffer and get it processed
        			//            ECU do a check to ensure that processing has not
        			//                been stopped
        			// -------------------------------------------------------------
        			if (process)
        			{
        				// ---------------------------------------------------------
        				// 19/10/2013 ECU now start recording
        				// ---------------------------------------------------------
        				audioRecord.startRecording ();
        				// ---------------------------------------------------------
        				// 19/10/2013 ECU read in a block of data (see the comment 
        				//				  at head of task about number returned)
        				// 22/11/2016 ECU Note - it seems to take ~ 120 mS to perform the
        				//                read
        				// ---------------------------------------------------------
        				numberOfShortsRead = audioRecord.read (inputBuffer,0,bufferSize);
        				// ---------------------------------------------------------
        				// 15/02/2014 ECU need the numberOfShortsRead to be a power of 2 
        				//				  so although the code is horrible will do 
        				//				  the following to get things working
        				// ---------------------------------------------------------
        				if (!Utilities.isNumberAPowerOfTwo (numberOfShortsRead))
        				{
        					// -----------------------------------------------------
        					// 24/10/2013 ECU find the next even number that is a 
        					//                power of 2
        					// -----------------------------------------------------
        					while (!Utilities.isNumberAPowerOfTwo (numberOfShortsRead))
        						numberOfShortsRead -= 2;
        				}
        				// ---------------------------------------------------------
        				// 24/10/2013 ECU stop recording so that there is no 'buffer 
        				//				  overflow' - bit of a lazy way but as the
        				//                data is only being monitored then serves 
        				//				  its purpose
        				// ---------------------------------------------------------
        				audioRecord.stop ();
        				// ---------------------------------------------------------
        				// 20/10/2013 ECU now process the received buffer
        				// 20/11/2016 ECU was passing through 
        				//					Arrays.copyOf (inputBuffer,numberOfShortsRead)
        				//                but changed to just pass through the inputBuffer
        				// 22/11/2016 ECU Note - the processing seems to take 
        				//                       ~ 60 mS
        				// ---------------------------------------------------------
        				ProcessAudioBuffer (inputBuffer,numberOfShortsRead);
        				// ---------------------------------------------------------
        			}
					// -------------------------------------------------------------
        			break;
        	}	
        }
        // -------------------------------------------------------------------------
    };
}

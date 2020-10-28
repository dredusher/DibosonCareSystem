package com.usher.diboson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

//==================================================================================
public class RevCounterActivity extends DibosonActivity implements SensorEventListener
{
	// =============================================================================
	private static final String TAG = "RevCounterActivity";
	// =============================================================================

	// =============================================================================
	private static final int	MESSAGE_DISPLAY		= 	0;
	private static final int	MESSAGE_EVENT		= 	1;
	private static final int	MESSAGE_EVENT_CLEAR	= 	2;
	private static final int	MESSAGE_SAMPLE		= 	3;
	private static final int	MESSAGE_STROBE_OFF	=	4;
	private static final int	MESSAGE_STROBE_ON	=	5;
	private static final int	SAMPLING_NUMBER		= 	200;
	private static final int	SAMPLING_TIME		= 	10 * 1000;
	private static final int	VALUE_AMBIENT		=	0;
	private static final int	VALUE_HIGH			=	1;
	// =============================================================================
	static int		strobeFrequency = 0;
	// =============================================================================
	boolean			armed = false;
	String			cameraID;
	CameraManager   cameraManager;
	ImageView	 	eventImageView;
	TextView		eventsPerMinuteView;
	TableRow		eventsRow;
	TextView		informationField;
	float			levelAmbient;
	float			levelHigh;
	TextView		lightLevelAmbientView;
	TextView		lightLevelCurrentView;
	TextView		lightLevelHighView;
	Sensor			lightSensor;
	MessageHandler	messageHandler;
	boolean			messageHandlerRunning;
	boolean			monitor = false;
	int				sample;
	Message			sampleMessage;
	float []		samples = new float [SAMPLING_NUMBER];
	boolean 		sampling = false;
	int				samplingValue;
	SensorManager 	sensorManager;
	int				strobeCounter = 0;
	int				strobeDuration;
	int				strobeGap;
	long			tickCurrent = 0l;
	long            tickPrevious = 0l;
	ToneGenerator 	toneGenerator;
	float			triggerHigh;
	TableRow		triggerHighRow;
	TextView		triggerHighView;
	float			triggerLow;
	TableRow		triggerLowRow;
	TextView		triggerLowView;
	float			value;
	// =============================================================================
	
	// =============================================================================
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		// 13/06/2019 ECU put in the check as to whether the activity has been created
		//                anew or is being recreated after having been destroyed by
		//                the Android OS
		// -------------------------------------------------------------------------
		// 14/09/2019 ECU check is there is an ambient light sensor
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 14/09/2019 ECU check if this device has an 'ambient light' sensor -
			//                if not then display a message and terminate this activity
			// ---------------------------------------------------------------------
			if (SensorService.lightSensor != null)
			{
				// -----------------------------------------------------------------
				// 25/11/2015 ECU the activity has been created anew
				// -----------------------------------------------------------------
				Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
				// -----------------------------------------------------------------
				setContentView(R.layout.activity_rev_counter);
				// -----------------------------------------------------------------
				informationField 	= (TextView) findViewById (R.id.rev_information_field);
				// -----------------------------------------------------------------
				// 23/10/2020 ECU set up the value fields
				// -----------------------------------------------------------------
				eventImageView			= (ImageView) findViewById (R.id.event_image);
				eventsPerMinuteView		= (TextView) findViewById (R.id.value_events);
				eventsRow				= (TableRow) findViewById (R.id.events_row);
				lightLevelAmbientView 	= (TextView) findViewById (R.id.value_ambient_light_level);
				lightLevelCurrentView 	= (TextView) findViewById (R.id.value_current_light_level);
				lightLevelHighView	 	= (TextView) findViewById (R.id.value_high_light_level);
				triggerHighRow			= (TableRow) findViewById (R.id.high_trigger_row);
				triggerHighView			= (TextView) findViewById (R.id.value_high_light_trigger);
				triggerLowRow			= (TableRow) findViewById (R.id.low_trigger_row);
				triggerLowView			= (TextView) findViewById (R.id.value_low_light_trigger);
				// -----------------------------------------------------------------
				// 18/06/2019 ECU set up the button listeners
				// -----------------------------------------------------------------
				((Button) findViewById (R.id.set_light_ambient)).setOnClickListener (buttonListener);
				((Button) findViewById (R.id.set_light_high)).setOnClickListener (buttonListener);
				((Button) findViewById (R.id.start_monitoring)).setOnClickListener (buttonListener);	
				// -----------------------------------------------------------------
				messageHandler = new MessageHandler ();
				messageHandlerRunning = true;
				// -----------------------------------------------------------------
				sensorManager 	= (SensorManager) getSystemService (SENSOR_SERVICE); 
				lightSensor 	= sensorManager.getDefaultSensor (Sensor.TYPE_LIGHT);
				// -----------------------------------------------------------------
				// 19/06/2019 ECU set up the tone generator that will be used later
				// -----------------------------------------------------------------
				toneGenerator = new ToneGenerator (AudioManager.STREAM_MUSIC, 100);
				// -----------------------------------------------------------------
				// 13/06/2019 ECU register this activity to receive events from the
				//                sensor manager
				// -----------------------------------------------------------------
				sensorManager.registerListener (this, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
				// -----------------------------------------------------------------
				// 13/06/2019 ECU start up the strobe light
				// 14/06/2019 ECU check if there is a flash unit - the 'true' confirms
				//                that any resources used in the check are released
				// -----------------------------------------------------------------
				if (FlashLight.flashLightCheck (true))
				{
					// -------------------------------------------------------------
					DialogueUtilitiesNonStatic.sliderChoice (this,
													 	 	 this,
													 	 	 getString (R.string.strobe_frequency),
													 	 	 getString (R.string.strobe_frequency_summary),
													 	 	 R.drawable.timer,
													 	 	 null,
													 	 	 strobeFrequency,
													 	 	 0,
													 	 	 100,
													 	 	 getString (R.string.strobe_frequency),
													 	 	 Utilities.createAMethod (RevCounterActivity.class,"SetStrobeMethod",0),
													 	 	 getString (R.string.cancel_operation));
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 14/09/2019 ECU inform the user that there is no light sensor
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (getString (R.string.ambient_light_sensor_none),true);
				// -----------------------------------------------------------------
				// 14/09/2019 ECU just exit this activity
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 13/06/2019 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------			
		}
	}
	// =============================================================================
	@Override
	public void onAccuracyChanged (Sensor theSensor, int theAccuracy) 
	{
	}
	// =============================================================================
	@Override
	public void onBackPressed ()
	{
		// -------------------------------------------------------------------------
		// 13/06/2019 ECU created to detect the BACK key and to finish the current
		//                activity
		// -------------------------------------------------------------------------
		setLED (false);
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
		// 13/06/2019 ECU now call the super for this method
		// -------------------------------------------------------------------------
		super.onBackPressed ();
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
	@Override
	public void onDestroy () 
	{
		// -------------------------------------------------------------------------
		// 14/09/2019 ECU check whether there was an ambient sensor
		// -------------------------------------------------------------------------
		if (SensorService.lightSensor != null)
		{
			// ---------------------------------------------------------------------
			// 13/06/2019 ECU unregister this service as a listener of sensor events
			// ---------------------------------------------------------------------
			sensorManager.unregisterListener (this); 
			// ---------------------------------------------------------------------
			messageHandlerRunning = false;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		super.onDestroy ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onSensorChanged (SensorEvent theSensorEvent) 
	{
		// -------------------------------------------------------------------------
		// 18/06/2019 ECU remember the current value
		// -------------------------------------------------------------------------
		value = theSensorEvent.values [0];
		// -------------------------------------------------------------------------
		// 23/10/2020 ECU display the current value
		// -------------------------------------------------------------------------
		lightLevelCurrentView.setText (String.valueOf (value));
		// -------------------------------------------------------------------------
		// 18/06/2019 ECU check if monitor is on
		// -------------------------------------------------------------------------
		if (monitor)
		{
			// ---------------------------------------------------------------------
			// 19/06/2019 ECU check for arming / disarming
			// ---------------------------------------------------------------------
			if (!armed && (value > triggerHigh))
			{
				armed = true;
				// -----------------------------------------------------------------
				// 23/10/2020 ECU indicate that the event has happened
				// -----------------------------------------------------------------
				messageHandler.sendEmptyMessage (MESSAGE_EVENT);
				// -----------------------------------------------------------------
			}
			else
			if (armed && (value < triggerLow))
			{
				armed = false;
			}
			// ---------------------------------------------------------------------
		}
		else
		// -------------------------------------------------------------------------
		// 19/06/2019 ECU check if any sampling is required
		// -------------------------------------------------------------------------
		if (!sampling)
		{
			// ---------------------------------------------------------------------
			// 19/06/2019 ECU just display the current light level
			// ---------------------------------------------------------------------

			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 19/06/2019 ECU store the current value as a sample
			// ---------------------------------------------------------------------
			samples [sample++] = value;
			// ---------------------------------------------------------------------
			// 19/06/2019 ECU check if there are enough samples
			// ---------------------------------------------------------------------
			if (sample >= SAMPLING_NUMBER)
			{
				// -----------------------------------------------------------------
				// 19/06/2019 ECU have enough samples
				// -----------------------------------------------------------------
				sampling = false;
				// -----------------------------------------------------------------
				// 19/06/2019 ECU no need to wait any longer so
				//                	delete the outstanding message
				//					send message to process the samples
				// -----------------------------------------------------------------
				messageHandler.removeMessages (MESSAGE_SAMPLE);
				Message localMessage = messageHandler.obtainMessage (MESSAGE_SAMPLE,samplingValue,StaticData.NOT_SET);
				messageHandler.sendMessage (localMessage);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void beep ()
	{
		// -------------------------------------------------------------------------
		// 19/06/2019 ECU created to sound a short beep
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 19/06/2019 ECU sound a short beep
			// ---------------------------------------------------------------------
			toneGenerator.startTone (ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_SP_PRI,20); 
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	private View.OnClickListener buttonListener = new View.OnClickListener() 
	{
		// -------------------------------------------------------------------------
		@Override
		public void onClick(View view) 
		{	
			//----------------------------------------------------------------------
			// 17/03/2015 ECU now process depending on which button pressed
			//----------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.set_light_ambient:
					// -------------------------------------------------------------
					// 18/06/2019 ECU set the ambient light level
					// -------------------------------------------------------------
					lightLevelAmbientView.setText ("sampling");
					// -------------------------------------------------------------
					getSamples (VALUE_AMBIENT);
					// -------------------------------------------------------------	 
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case R.id.set_light_high:
					// -------------------------------------------------------------
					// 18/06/2019 ECU set the 'high light' level
					// -------------------------------------------------------------
					lightLevelHighView.setText ("sampling");
					// -------------------------------------------------------------
					getSamples (VALUE_HIGH);
					// -------------------------------------------------------------	 
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case R.id.start_monitoring:
					// -------------------------------------------------------------
					// 18/06/2019 ECU start the monitoring
					// -------------------------------------------------------------
					// 22/10/2020 ECU hide the information field
					// -------------------------------------------------------------
					informationField.setVisibility (View.INVISIBLE);
					// -------------------------------------------------------------
					// 23/10/2020 ECU make the information rows visible
					// -------------------------------------------------------------
					eventsRow.setVisibility (View.VISIBLE);
					triggerHighRow.setVisibility (View.VISIBLE);
					triggerLowRow.setVisibility (View.VISIBLE);
					// -------------------------------------------------------------
					if (levelHigh > levelAmbient)
					{
						// ---------------------------------------------------------
						// 19/06/2019 ECU set up the triggers
						// ---------------------------------------------------------
						float localQuarter = (levelHigh - levelAmbient) / 4;
						triggerHigh = levelHigh - localQuarter;
						triggerLow  = levelAmbient + localQuarter;
						// --------------------------------------------------------
						// 23/10/2020 ECU display the trigger levels
						// --------------------------------------------------------
						triggerHighView.setText(String.valueOf (triggerHigh));
						triggerLowView.setText(String.valueOf  (triggerLow));
						// ---------------------------------------------------------
						// 18/06/2019 ECU start up the monitoring
						// ---------------------------------------------------------
						monitor = true;
						// ---------------------------------------------------------
						// 23/10/2020 ECU intialise the counters
						// ---------------------------------------------------------
						tickCurrent = System.currentTimeMillis();
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	void getSamples (int theValue)
	{
		// -------------------------------------------------------------------------
		// 19/06/2019 ECU get samples of the light level
		// -------------------------------------------------------------------------
		samplingValue = theValue;
		sampleMessage = messageHandler.obtainMessage (MESSAGE_SAMPLE, theValue, StaticData.NOT_SET);
		messageHandler.sendMessageDelayed (sampleMessage,SAMPLING_TIME);
		// -------------------------------------------------------------------------
		sample 		= 0;
		sampling 	= true;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class MessageHandler extends Handler
    {
		// -------------------------------------------------------------------------
		// -------------------------------------------------------------------------
		// 13/06/2019 ECU created to process messages
		// -------------------------------------------------------------------------
		// -------------------------------------------------------------------------
        @Override
        public void handleMessage (Message theMessage) 
        {   
        	if (messageHandlerRunning)
        	{
        		// -----------------------------------------------------------------
        		// 21/03/2017 ECU switch on the message type
        		// -----------------------------------------------------------------
        		switch (theMessage.what)
        		{
        			// =============================================================
					case MESSAGE_DISPLAY:
						// ---------------------------------------------------------
						// 24/10/2020 ECU display the events per minute
						// ---------------------------------------------------------
						eventsPerMinuteView.setText (String.format ("%.2f",(((float) StaticData.MILLISECONDS_PER_MINUTE) / (float) (tickCurrent - tickPrevious))));
						// ---------------------------------------------------------
						break;
        			// =============================================================
					case MESSAGE_EVENT:
						// ---------------------------------------------------------
						// 23/10/2020 ECU an 'event' has happened
						// ---------------------------------------------------------
						beep ();
						// ---------------------------------------------------------
						eventImageView.setVisibility(View.VISIBLE);
						// ---------------------------------------------------------
						sendEmptyMessageDelayed (MESSAGE_EVENT_CLEAR,100);
						// ---------------------------------------------------------
						// 24/10/2020 ECU get time of this event
						// ---------------------------------------------------------
						tickPrevious = tickCurrent;
						tickCurrent = System.currentTimeMillis ();
						// ---------------------------------------------------------
						// 24/10/2020 ECU calculate the ticks per minute
						// ---------------------------------------------------------
						sendEmptyMessage (MESSAGE_DISPLAY);
						// ---------------------------------------------------------
						break;
					// =============================================================
					case MESSAGE_EVENT_CLEAR:
						// ---------------------------------------------------------
						// 23/10/2020 ECU an 'event' is to be cleared
						// ---------------------------------------------------------
						eventImageView.setVisibility(View.INVISIBLE);
						// ---------------------------------------------------------
						break;
        			// =============================================================
        			case MESSAGE_SAMPLE:
        				// ---------------------------------------------------------
        				// 19/06/2019 ECU handle the sampling messages
        				// ---------------------------------------------------------
        				sampling = false;
        				// ---------------------------------------------------------
        				// 19/06/2019 ECU now work out the average of the samples
        				// ---------------------------------------------------------
        				if (sample > 0)
        				{
        					float localTotal = 0f;
        					for (int index = 0; index < sample; index++)
        					{
        						localTotal += samples [index];
        					}
        					float sampleAverage = localTotal / sample;
        					// -----------------------------------------------------
        					// 19/06/2019 ECU now decide which variable is to be set
        					// -----------------------------------------------------
        					switch (theMessage.arg1)
        					{
        						// -------------------------------------------------
        						case VALUE_AMBIENT:
        							levelAmbient = sampleAverage;
        							// ---------------------------------------------
        							// 23/10/2020 ECU display the value
        							// ---------------------------------------------
        							lightLevelAmbientView.setText (String.valueOf (levelAmbient));
        							// ---------------------------------------------
        							break;
        						// -------------------------------------------------
        						case VALUE_HIGH:
        							levelHigh = sampleAverage;
									// ---------------------------------------------
									// 23/10/2020 ECU display the value
									// ---------------------------------------------
									lightLevelHighView.setText (String.valueOf (levelHigh));
									// ---------------------------------------------
        							break;
        						// -------------------------------------------------
        					}
        				}
        				// ---------------------------------------------------------
        				break;
        			// =============================================================
        			case MESSAGE_STROBE_OFF:
        				// ---------------------------------------------------------
        				// 13/06/2019 ECU switch off the torch
        				// ---------------------------------------------------------
        				setLED (false);
        				// ---------------------------------------------------------
        				break;
        			// =============================================================
        			case MESSAGE_STROBE_ON:
        				strobeCounter++;
        				// ---------------------------------------------------------
        				// 13/06/2019 ECU switch on the torch
        				// ---------------------------------------------------------
        				setLED (true);
        				sendEmptyMessageDelayed (MESSAGE_STROBE_ON,strobeGap);
        				sendEmptyMessageDelayed (MESSAGE_STROBE_OFF,strobeDuration);
        				// ---------------------------------------------------------
        				break;
        			// =============================================================
        			default:
        				break;
        		// =================================================================      	
        		}
        	}
        	// ---------------------------------------------------------------------
        }
        // =========================================================================
        // =========================================================================
    };
    // =============================================================================
    void setLED (boolean theState)
    {
    	// --------------------------------------------------------------------------
    	// 14/06/2019 ECU set the state of the LED 
    	// --------------------------------------------------------------------------
    	try 
    	{
    		cameraManager.setTorchMode (cameraID,theState);
    	} 	
    	catch (Exception theException) 
    	{
    	} 
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
	@SuppressLint ("InlinedApi")
	public void SetStrobeMethod (int theFrequency)
	{
		// -------------------------------------------------------------------------
		if (theFrequency > 0)
		{
			// ---------------------------------------------------------------------
			// 14/06/2019 ECU the strobe light is required so set up the parameters
			// ---------------------------------------------------------------------
			cameraManager 	= (CameraManager) getSystemService (Context.CAMERA_SERVICE);
			try 
			{
				cameraID 		= cameraManager.getCameraIdList()[0];
			} 
			catch (CameraAccessException theException) 
			{
			}
			// ---------------------------------------------------------------------
			// 13/06/2019 ECU set up the frequency parameters
			// ---------------------------------------------------------------------
			startStrobe (theFrequency);
			strobeFrequency = theFrequency;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    void startStrobe (int theFrequency)
    {
    	// -------------------------------------------------------------------------
    	strobeGap 		= 1000 / theFrequency;
    	strobeDuration 	= (strobeGap / 4);
    	// -------------------------------------------------------------------------
    	messageHandler.sendEmptyMessage (MESSAGE_STROBE_ON);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
}

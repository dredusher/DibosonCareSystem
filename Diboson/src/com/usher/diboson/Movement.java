package com.usher.diboson;

import android.annotation.SuppressLint;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;

// =================================================================================
public class Movement 
{
	// =============================================================================
	// 05/10/2017 ECU created to store and handle variables and methods associated
	//                with the detection of 'device movement'
	//
	//                movementDuration ..... the time in milliSeconds for which the
	//                                       accelerometer readings will be checked
	//                                       for 'movement'
	//                movementTrigger ...... the change in 'g Force' which indicates
	//                                       when 'movement' has occurred
	//                movementActions ...... the 'actions' that are to be processed 
	//                                       when 'movement' has occurred
	//                movementGap .......... the gap between consequetive triggers -
	//                                       if a trigger occurs during this time
	//                                       then it will be ignored
	// 29/09/2018 ECU added the 'activationDelay'
	//				  activationDelay ...... the delay between the alarm being
	//                                       triggered and the actions being processed
	// =============================================================================
	
	// =============================================================================
	public static int				activationDelay;
	public 		  boolean			active;
	public static float 			gForcePrevious = StaticData.NOT_SET;
	public static MessageHandler	messageHandler;
	public static String			movementActions;
	public static int				movementDuration;
	public static int				movementGap;
	public static float				movementTrigger;
				  boolean			userIntervention;
	// =============================================================================
	
	// =============================================================================
	public Movement (int theInitialDelay,int theDuration,float theMovementTrigger,String theActions,int theGap)
	{
		// -------------------------------------------------------------------------
		// 05/10/2017 ECU initialise any variables
		// 06/10/2017 ECU added the initial delay - this does not require anything
		//                to be stored
		// -------------------------------------------------------------------------
		messageHandler		= new MessageHandler ();
		movementActions		= theActions;
		movementDuration	= theDuration;
		movementGap			= theGap;
		movementTrigger		= theMovementTrigger;
		// -------------------------------------------------------------------------
		// 29/09/2018 ECU set up the delay
		// -------------------------------------------------------------------------
		activationDelay		= 5 * 1000;		// 10 seconds in mS
		// -------------------------------------------------------------------------
		// 29/09/2018 ECU indicate that by default no user intervention is required
		// -------------------------------------------------------------------------
		userIntervention = false;
		// -------------------------------------------------------------------------
		// 06/10/2017 ECU indicate that the 'motion detector' is not active
		// -------------------------------------------------------------------------
		updateActiveState (false);
		// -------------------------------------------------------------------------
		// 05/10/2017 ECU start up the sensor handling
		// 06/10/2017 ECU changed to use the initial delay
		// -------------------------------------------------------------------------
		messageHandler.sendEmptyMessageDelayed (StaticData.MESSAGE_START,theInitialDelay);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public Movement ()
	{
		// -------------------------------------------------------------------------
		// 06/10/2017 ECU created to use the stored values
		// -------------------------------------------------------------------------
		this	(PublicData.storedData.movementParameters.initialDelay,
				 PublicData.storedData.movementParameters.duration,
				 PublicData.storedData.movementParameters.trigger,
				 PublicData.storedData.movementParameters.actions,
				 PublicData.storedData.movementParameters.gap);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void Finish ()
	{
		// -------------------------------------------------------------------------
		// 06/10/2017 ECU prevent it starting if not reached that stage
		// -------------------------------------------------------------------------
		messageHandler.removeMessages(StaticData.MESSAGE_START);
		// -------------------------------------------------------------------------
		// 06/10/2017 ECU tell the message handler to 'finish'
		// -------------------------------------------------------------------------
		messageHandler.sendEmptyMessage (StaticData.MESSAGE_FINISH);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public static void sensorHandler (Object theSensorEventAsObject)
	{
		// -------------------------------------------------------------------------
		// 05/10/2017 ECU created to be called by the SensorService to handle 
		//                accelerometer events
		//            ECU cast the argument to local event data
		// -------------------------------------------------------------------------
		SensorEvent sensorEvent = (SensorEvent) theSensorEventAsObject;
		// -------------------------------------------------------------------------
		// 05/10/2017 ECU from the components work out the magnitude of the 'g force'
		// -------------------------------------------------------------------------
		float gForce = ((float) Math.sqrt ((sensorEvent.values [0] * sensorEvent.values [0]) + 
										   (sensorEvent.values [1] * sensorEvent.values [1]) + 
				                           (sensorEvent.values [2] * sensorEvent.values [2]))) / SensorManager.GRAVITY_EARTH;
		// -------------------------------------------------------------------------
		// 05/10/2017 ECU check if the change of gForce indicates that the device
		//                'has moved'. But first check that there is a previous value
		// -------------------------------------------------------------------------
		if (gForcePrevious != StaticData.NOT_SET)
		{
			// ---------------------------------------------------------------------
			// 05/10/2017 ECU check if the gForce has changed sufficiently to
			//                indicate 'movement'
			// ---------------------------------------------------------------------
			if ((Math.abs (gForcePrevious - gForce)) > movementTrigger)
			{
				// -----------------------------------------------------------------
				// 05/10/2017 ECU it appears that the device has moved so trigger
				//                the necessary actions
				// 29/09/2018 ECU changed from MESSAGE_PROCESS_ACTIONS to
				//				  MESSAGE_USER_ACTIONS
				// -----------------------------------------------------------------
				messageHandler.sendEmptyMessage (StaticData.MESSAGE_USER_ACTIONS);
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 05/10/2017 ECU remember the current value of the gForce
		// -------------------------------------------------------------------------
		gForcePrevious = gForce;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class MessageHandler extends Handler
	{
		// -------------------------------------------------------------------------
		// 29/09/2018 ECU added 'checkUserInput'
		// -------------------------------------------------------------------------
		boolean checkUserInput	= false;
		boolean ignoreMessage 	= false;
		// -------------------------------------------------------------------------
		@Override
	    public void handleMessage(Message theMessage) 
	    {   
			switch (theMessage.what)
			{
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_FINISH:
					// -------------------------------------------------------------
					// 05/10/2017 ECU deregister the listener for sensor events
					// -------------------------------------------------------------
					SensorService.accelerometerEnablement (false,null,0);
					// -------------------------------------------------------------
					// 06/10/2017 ECU indicate that detector has finished
					// -------------------------------------------------------------
					updateActiveState (false);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_PROCESS_ACTIONS:
					// -------------------------------------------------------------
					// 05/10/2017 ECU process the stored actions unless currently
					//                being told to ignore messages until the 'time out'
					//                occurs
					// -------------------------------------------------------------
					if (!ignoreMessage)
					{
						// ---------------------------------------------------------
						// 29/09/2018 ECU it is time to process the actions but have
						//                the initial delay to allow the user to cancel
						// ---------------------------------------------------------
						Utilities.actionHandler (MainActivity.activity,movementActions);
						// ---------------------------------------------------------
						// 05/10/2017 ECU indicate that subsequent messages are to be 
						//                ignored until the timer expires
						// ---------------------------------------------------------
						ignoreMessage = true;
						sendEmptyMessageDelayed(StaticData.MESSAGE_TIME_OUT, movementGap);
						// ---------------------------------------------------------
					}
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_START:
					// -------------------------------------------------------------
					// 06/10/2017 ECU indicate that the detector is active
					// -------------------------------------------------------------
					updateActiveState (true);
					// -------------------------------------------------------------
					// 05/10/2017 ECU register the listener for accelerometer changes
					// -------------------------------------------------------------
					SensorService.accelerometerEnablement (true,
							   							   Utilities.createAMethod (Movement.class,"sensorHandler",(Object) null),
							   							   SensorManager.SENSOR_DELAY_NORMAL);
					// -------------------------------------------------------------
					// 05/10/2017 ECU indicate when the monitoring is to stop
					// -------------------------------------------------------------
					this.sendEmptyMessageDelayed (StaticData.MESSAGE_FINISH,movementDuration);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_TIME_OUT:
					// -------------------------------------------------------------
					// 05/10/2017 ECU reset the flag to indicate that messages can be
					//                processed
					// -------------------------------------------------------------
					ignoreMessage = false;
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_USER_ACTIONS:
					// -------------------------------------------------------------
					// 29/09/2018 ECU start the processing after a period which allows
					//                the user to touch the volume key to stop the
					//                processing
					// -------------------------------------------------------------
					sendEmptyMessageDelayed (StaticData.MESSAGE_PROCESS_ACTIONS,activationDelay);
					// -------------------------------------------------------------
					// 29/09/2018 ECU indicate that the user can interrupt the alarm
					// -------------------------------------------------------------
					userIntervention = true;
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}
	    }
	};
	// =============================================================================
	void updateActiveState (boolean theActiveState)
	{
		// -------------------------------------------------------------------------
		// 06/10/2017 ECU created to set the 'active' state and get the icon updated
		// -------------------------------------------------------------------------
		active = theActiveState;
		GridActivity.gridRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_ADAPTER); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public boolean VolumeKeyPressed ()
	{
		// -------------------------------------------------------------------------
		// 29/09/2018 ECU called when the volume key has been pressed
		// 20/08/2019 ECU changed to 'boolean' to indicate whether the key has been
		//                processed
		// -------------------------------------------------------------------------
		if (userIntervention)
		{
			// ---------------------------------------------------------------------
			// 29/09/2018 ECU in the period when a user pressing the volume key
			//                will deactivate the alarm
			// ---------------------------------------------------------------------
			messageHandler.removeMessages (StaticData.MESSAGE_PROCESS_ACTIONS);
			// ---------------------------------------------------------------------
			// 29/09/2018 ECU indicate that not monitoring
			// ---------------------------------------------------------------------
			userIntervention = false;
			// ---------------------------------------------------------------------
			// 29/09/2018 ECU tell the user that the alarm has been cancelled
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (MainActivity.activity.getString (R.string.theft_alarm_cancelled),true);
			// ---------------------------------------------------------------------
			// 20/08/2019 ECU indicate that the key has been processed
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 20/08/2019 ECU indicate that the key has not been processed locally but
		//                if the device is 'protected' then indicate that it has been
		//                processed
		// -------------------------------------------------------------------------
		return active;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}
// =================================================================================

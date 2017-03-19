package com.usher.diboson;

import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AlarmManagerReceiver extends BroadcastReceiver
{
	// =============================================================================
	// 01/12/2015 ECU added the PANIC_ALARM handling
	// 29/12/2015 ECU added bluetooth discovery handling
	/* ============================================================================= */
	final static String TAG = "AlarmManagerReceiver";
	/* ============================================================================= */
	
	/* ============================================================================= */
	@Override
	public void onReceive (Context context, Intent intent) 
	{
		Bundle localExtras = intent.getExtras();
		// -------------------------------------------------------------------------
		// 02/12/2015 ECU added the retrieval of ALARM_TYPE which may or may not be 
		//                used by tasks using the alarm manager
		// -------------------------------------------------------------------------
		int theAlarmID 		= StaticData.NO_RESULT;
		int theAlarmType	= StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
		if (localExtras != null)
	    {
			theAlarmID 		= intent.getIntExtra (StaticData.PARAMETER_ALARM_ID,StaticData.NO_RESULT);  
			theAlarmType 	= intent.getIntExtra (StaticData.PARAMETER_ALARM_TYPE,StaticData.NO_RESULT);  
	    }
		// -------------------------------------------------------------------------
		// 07/03/2016 ECU check whether this is receiver is being called when the
		//                app is not running. Normally the receiver is disabled
		//                when the app is destroyed unless the panic alarm is enabled
		// -------------------------------------------------------------------------
		if ((PublicData.storedData != null) && PublicData.storedData.initialised)
		{
			//----------------------------------------------------------------------
			// 23/02/2014 ECU log useful information
			// 15/12/2015 ECU added the alarm type
			//----------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG,"Alarm ID = " + theAlarmID + " " + theAlarmType);
			// ---------------------------------------------------------------------
			// 23/02/2014 ECU handle the individual alarms
			// ---------------------------------------------------------------------	   	
			switch (theAlarmID)
			{
				// -----------------------------------------------------------------
				case StaticData.ALARM_ID_BLUETOOTH_DISCOVERY:
					// -------------------------------------------------------------
					// 29/12/2015 ECU created to handle the interrupt associated with
					//                bluetooth discovery
					// 31/12/2015 ECU put in the cheeck on null - just in case
					// -------------------------------------------------------------
					if (PublicData.bluetoothUtilities != null)
						BluetoothUtilities.handleAlarm (context,theAlarmType);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.ALARM_ID_PANIC_ALARM:
					// -------------------------------------------------------------
					// 01/12/2015 ECU added to handle a panic alarm timer
					// 02/12/2015 ECU add the type
					//            ECU check if the user interface is up and running
					//                if not then do not action and disable the
					//                panic alarm
					// -------------------------------------------------------------
					if (PublicData.userInterfaceRunning)
					{
						PanicAlarmActivity.handleAlarm (context,theAlarmType);
					}
					else
					{
						// ---------------------------------------------------------
						// 02/12/2015 ECU disable the panic alarm
						// ---------------------------------------------------------
						PublicData.storedData.panicAlarm.enabled = false;
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.ALARM_ID_SPEAKING_CLOCK:
					// -------------------------------------------------------------
					// 01/12/2015 ECU changed to use StaticData.AL.....
					// -------------------------------------------------------------
					Utilities.SpeakingClock ();
					// -------------------------------------------------------------
					// 26/02/2014 ECU IMPORTANT - see notes
					// 06/03/2014 ECU the code that was here has been moved
					// 09/03/2014 ECU renamed the 'fix'
					// -------------------------------------------------------------
					APIIssues.Fix001 (android.os.Build.VERSION.SDK_INT);
					// -------------------------------------------------------------
					// 23/02/2014 ECU set up the next time in the sequence
					// -------------------------------------------------------------
					SpeakingClockActivity.RepeatTheAlarm (context);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.ALARM_ID_WESTMINSTER_CHIME:
					// -------------------------------------------------------------
					// 11/03/2017 ECU handle a Westminster Chime
					// -------------------------------------------------------------
					SpeakingClockActivity.westminsterChime (context);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 07/03/2016 ECU an alarm was received when the app was not running
			//                so try and start the app
			// ---------------------------------------------------------------------
			// 07/03/2016 ECU want to set up another alarm with the same parameters
			//                as this to be executed some time after the app is
			//                started which will be StaticData.RESTART_TIME millisecs
			//                from now
			// 03/11/2016 ECU changed to use the global alarm manager
			// ---------------------------------------------------------------------
			PendingIntent alarmPendingIntent = PendingIntent.getBroadcast (context,
													theAlarmID,intent,Intent.FLAG_ACTIVITY_NEW_TASK);  
			// ---------------------------------------------------------------------
			// 24/12/2015 ECU changed to use the new method
			// 03/11/2016 ECU changed to use the global alarm manager
			// 06/02/2017 ECU go back to local arm manager in case the Public version
			//                not set yet
			// ---------------------------------------------------------------------
			Utilities.SetAnExactAlarm ((AlarmManager) context.getSystemService (Context.ALARM_SERVICE),
									   (Calendar.getInstance()).getTimeInMillis() + (StaticData.RESTART_TIME + StaticData.RESTART_INTERRUPT),
									   alarmPendingIntent);
			// ---------------------------------------------------------------------
			// 07/03/2016 ECU added the 'true' argument to indicate that the user
			//                interface should be started without user input
			// ---------------------------------------------------------------------
			MainActivity.restartThisApp (context,true);
			// ---------------------------------------------------------------------
		}
	} 
	/* ============================================================================= */
}

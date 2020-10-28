package com.usher.diboson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
	// 19/11/2018 ECU added the watchdog timer
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
		// 26/07/2017 ECU added the alarm time which will, if provided, contain
		//                the time when the alarm was for rather than the current
		//                time
		// -------------------------------------------------------------------------
		int 	alarmID 	= StaticData.NO_RESULT;
		long	alarmTime	= StaticData.NO_RESULT;
		int 	alarmType	= StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
		if (localExtras != null)
	    {
			alarmID 	= intent.getIntExtra (StaticData.PARAMETER_ALARM_ID,StaticData.NO_RESULT);  
			alarmType 	= intent.getIntExtra (StaticData.PARAMETER_ALARM_TYPE,StaticData.NO_RESULT); 
			// ---------------------------------------------------------------------
			// 26/07/2017 ECU check if the actual time required has been stored
			// ---------------------------------------------------------------------
			alarmTime 	= intent.getLongExtra (StaticData.PARAMETER_ALARM_TIME,StaticData.NOT_SET); 
			// ---------------------------------------------------------------------
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
			// 26/07/2017 ECU make the type conditional
			// 16/03/2019 ECU added the alarmTime and make conditional
			//----------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG,"Alarm ID = " + alarmID + 
					((alarmType != StaticData.NO_RESULT) ? (" " + alarmType) 
							                             : StaticData.BLANK_STRING) +
					((alarmTime != StaticData.NO_RESULT) ? (" " + new SimpleDateFormat (StaticData.ALARM_TIME_FORMAT,Locale.getDefault()).format(alarmTime))
							                             : StaticData.BLANK_STRING));
			// ---------------------------------------------------------------------
			// 23/02/2014 ECU handle the individual alarms
			// ---------------------------------------------------------------------	   	
			switch (alarmID)
			{
				// -----------------------------------------------------------------
				case StaticData.ALARM_ID_BLUETOOTH_DISCOVERY:
					// -------------------------------------------------------------
					// 29/12/2015 ECU created to handle the interrupt associated with
					//                bluetooth discovery
					// 31/12/2015 ECU put in the cheeck on null - just in case
					// -------------------------------------------------------------
					if (PublicData.bluetoothUtilities != null)
						BluetoothUtilities.handleAlarm (context,alarmType);
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
						PanicAlarmActivity.handleAlarm (context,alarmType);
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
				case StaticData.ALARM_ID_RANDOM_EVENT:
					// -------------------------------------------------------------
					// 21/06/2018 ECU a 'random event' has occurred
					// -------------------------------------------------------------
					PublicData.storedData.randomEvent.alarmHandler (context);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.ALARM_ID_SPEAKING_CLOCK:
					// -------------------------------------------------------------
					// 01/12/2015 ECU changed to use StaticData.AL.....
					// 20/03/2017 ECU pass through the context
					// 23/11/2018 ECU because the Android OS doesn't return the
					//                alarm at the exact time that it was set then
					//                indicate that the current time should be spoken
					// 16/03/2019 ECU just tidied up the code
					// -------------------------------------------------------------
					SpeakingClockActivity.SpeakingClock (context,StaticData.NOT_SET);
					// -------------------------------------------------------------
					// 26/02/2014 ECU IMPORTANT - see notes
					// 06/03/2014 ECU the code that was here has been moved
					// 09/03/2014 ECU renamed the 'fix'
					// 19/07/2017 ECU pass through the context to the fix
					// -------------------------------------------------------------
					APIIssues.Fix001 (context,android.os.Build.VERSION.SDK_INT);
					// -------------------------------------------------------------
					// 23/02/2014 ECU set up the next time in the sequence
					// -------------------------------------------------------------
					SpeakingClockActivity.RepeatTheAlarm (context);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.ALARM_ID_WATCHDOG_TIMER:
					// -------------------------------------------------------------
					// 19/11/2018 ECU handle a watchdog timer alarm
					// -------------------------------------------------------------
					PublicData.storedData.watchdogTimer.Alarm (context);
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
			// 15/03/2019 ECU addedFLAG_CURRENT_FLAG
			// ---------------------------------------------------------------------
			PendingIntent alarmPendingIntent = PendingIntent.getBroadcast (context,
													alarmID,intent,Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_UPDATE_CURRENT);  
			// ---------------------------------------------------------------------
			// 24/12/2015 ECU changed to use the new method
			// 03/11/2016 ECU changed to use the global alarm manager
			// 06/02/2017 ECU go back to local alarm manager in case the Public version
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

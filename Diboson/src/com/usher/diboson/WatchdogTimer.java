package com.usher.diboson;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.io.Serializable;

public class WatchdogTimer implements Serializable
{
	// =============================================================================
	// 19/11/2018 ECU created to contain the data and methods associated with a
	//                'watchdog timer'
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	// 19/22/2018 ECU declare any associated data
	// =============================================================================
	public int	interval = 0;			// the interval in seconds between successive 
	                                    // timers. If set to NOT_SET or 0 then the
										// watchdog facility is disabled
	// =============================================================================
	
	// =============================================================================
	public void Alarm (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 19/11/2018 ECU created to handle alarms as they occur
		// -------------------------------------------------------------------------
		PublicData.messageHandler.sendEmptyMessage (StaticData.MESSAGE_WATCHDOG_TIMER);
		// -------------------------------------------------------------------------
		// 19/11/2018 ECU set up the next timer
		//--------------------------------------------------------------------------
		SetWatchdogTimer (theContext);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void Initialise (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 19/11/2018 ECU created to initialise the watchdog mechanism
		// -------------------------------------------------------------------------
		// 19/11/2018 ECU check if the system has ever been initialised
		// -------------------------------------------------------------------------
		if (PublicData.storedData.watchdogTimer == null)
		{
			// ---------------------------------------------------------------------
			// 19/11/2018 ECU the object has never been initialised so create it
			// ---------------------------------------------------------------------
			PublicData.storedData.watchdogTimer = new WatchdogTimer ();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 19/11/2018 ECU the object exists so check if the system is 'active'
			// ---------------------------------------------------------------------
			if (PublicData.storedData.watchdogTimer.interval != 0 && 
				PublicData.storedData.watchdogTimer.interval != StaticData.NOT_SET)
			{
				// -----------------------------------------------------------------
				// 19/11/2018 ECU need to start up the watchdog timer system
				// -----------------------------------------------------------------
				PublicData.storedData.watchdogTimer.SetWatchdogTimer (theContext);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void SetInterval (Context theContext,int theInterval)
	{
		// -------------------------------------------------------------------------
		// 19/11/2018 ECU called by the settings activity to set the interval and
		//                if necessary then trigger the next watchdog timer
		// -------------------------------------------------------------------------
		interval = theInterval;
		// -------------------------------------------------------------------------
		// 19/11/2018 ECU if the interval is 0 then no need to start the timer
		// -------------------------------------------------------------------------
		if (interval > 0)
		{
			SetWatchdogTimer (theContext);
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void SetWatchdogTimer (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 19/11/2018 ECU set the next watchdog timer but only if the interval is
		//                non-zero
		// -------------------------------------------------------------------------
		if (interval > 0)
		{
			// ---------------------------------------------------------------------
			long localNextTime = Utilities.getAdjustedTime (false) + (interval * 1000);
			// ---------------------------------------------------------------------	
			Intent alarmIntent = new Intent (theContext, AlarmManagerReceiver.class);
			// ---------------------------------------------------------------------
			// 15/03/2019 ECU added PendingIntent.FLAG_UPDATE_CURRENT
			// 09/05/2020 ECU changed to use 'ALARM.....FLAGS'
			// ----------------------------------------------------------------------
			alarmIntent.putExtra (StaticData.PARAMETER_ALARM_ID,StaticData.ALARM_ID_WATCHDOG_TIMER);
			PendingIntent	alarmPendingIntent = PendingIntent.getBroadcast (theContext,
																			 StaticData.ALARM_ID_WATCHDOG_TIMER,
																			 alarmIntent,
																			 StaticData.ALARM_PENDING_INTENT_FLAGS);
			// ---------------------------------------------------------------------
			// 19/11/2018 ECU now set an exact timer
			// ---------------------------------------------------------------------
			Utilities.SetAnExactAlarm (PublicData.alarmManager,
									   localNextTime, 
									   alarmPendingIntent);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================

}

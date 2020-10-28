package com.usher.diboson;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.io.Serializable;
import java.util.Random;

public class RandomEvent implements Serializable
{
	// -----------------------------------------------------------------------------
	// 21/06/2018 ECU created to hold data and classes for a 'random event'
	// -----------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;
	// -----------------------------------------------------------------------------
	private static final String TAG = "RandomEvent";
	// -----------------------------------------------------------------------------
	
	// -----------------------------------------------------------------------------
	String	actions;				// 21/06/2018 ECU actions to be taken when the random
									//                event happens
	int		gap;					// 22/06/2018 ECU the minimum gap between events
	int		period;					// 21/06/2018 ECU set up the random event period
	// -----------------------------------------------------------------------------

	// =============================================================================
	public RandomEvent ()
	{
		// -------------------------------------------------------------------------
		// 21/06/2018 ECU public constructor
		// -------------------------------------------------------------------------
		actions = null;
		gap		= 5;
		period	= 60;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	private static PendingIntent alarmPendingIntent;
	private static Random		 randomNumber = new Random ();
	// =============================================================================
	
	// =============================================================================
	public void alarmHandler (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 21/06/2018 ECU created to handle a 'random event' event
		// -------------------------------------------------------------------------
		Utilities.actionHandler (theContext,PublicData.storedData.randomEvent.actions);
		// -------------------------------------------------------------------------
		// 21/06/2018 ECU set up the next 'random event'
		// -------------------------------------------------------------------------
		setAlarm (theContext);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void cancelAlarm ()
	{
		// -------------------------------------------------------------------------
		// 21/06/2018 ECU created to cancel any outstanding alarms
		// -------------------------------------------------------------------------
		if (alarmPendingIntent != null)
			PublicData.alarmManager.cancel (alarmPendingIntent);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print (Context theContext,String theHeader)
	{
		// -------------------------------------------------------------------------
		// 22/06/2018 ECU print a summary of the parameters
		// 02/07/2018 ECU changed to use the formatted string and added theContext
		// -------------------------------------------------------------------------
		return String.format (theContext.getString (R.string.random_event_format),theHeader,period,gap,actions);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void setAlarm (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 21/06/2018 ECU created to handle the 'random event' handler but only if
		//                parameters have been set
		// -------------------------------------------------------------------------
		if (actions != null && 
				!actions.equalsIgnoreCase (StaticData.BLANK_STRING) &&
				period > 0)
		{
			// ---------------------------------------------------------------------
			// 21/06/2018 ECU generate a random number to set the next time
			// 22/06/2018 ECU added the gap
			// 30/06/2018 ECU corrected a problem in that 'gap' was not being
			//                converted to milliseconds
			// ---------------------------------------------------------------------
			long localNextTime = Utilities.getAdjustedTime (false) + 
									(gap * (int) StaticData.MILLISECONDS_PER_MINUTE) +
									(randomNumber.nextInt (period * (int) StaticData.MILLISECONDS_PER_MINUTE));
			// ---------------------------------------------------------------------
			// 21/06/2018 ECU use the random number to generate a timer
			// ---------------------------------------------------------------------	
			Intent alarmIntent = new Intent (theContext, AlarmManagerReceiver.class);
			// ---------------------------------------------------------------------
			// 15/03/2019 ECU added PendingIntent.FLAG_UPDATE_CURRENT
			// 09/05/2020 ECU changed to use 'ALARM....FLAGS'
			// ---------------------------------------------------------------------
			alarmIntent.putExtra (StaticData.PARAMETER_ALARM_ID,StaticData.ALARM_ID_RANDOM_EVENT);
			alarmPendingIntent = PendingIntent.getBroadcast (theContext,
															  StaticData.ALARM_ID_RANDOM_EVENT,
															  alarmIntent,
															  StaticData.ALARM_PENDING_INTENT_FLAGS);
			// ---------------------------------------------------------------------
			// 21/06/2018 ECU now set an exact timer
			// ---------------------------------------------------------------------
			Utilities.SetAnExactAlarm (PublicData.alarmManager,
									   localNextTime, 
									   alarmPendingIntent);
			// ---------------------------------------------------------------------
			// 22/06/2018 ECU log the details of the next event
			// 02/07/2018 ECU added theContext
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG,Print (theContext,"Next event : " + PublicData.dateSimpleFormatHHMMDDMMYY.format (localNextTime)));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

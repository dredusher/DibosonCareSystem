package com.usher.diboson;

import java.io.Serializable;
import java.util.Calendar;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;


@SuppressLint("DefaultLocale")
public class Monitor implements Serializable
{
	/* ============================================================================= */
	// 18/11/2014 ECU created to contain details for the monitoring service
	// 28/04/2015 ECU removed the inactiveCounter and put in MainActivity
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	// =============================================================================
	final static String TAG		=	"Monitor";
	// =============================================================================
	public int		duration			 = 2;				// 24/11/2014 ECU added
	public boolean  email				 = false;			// 27/04/2015 ECU added
	public boolean	enabled				 = false;
	public int 		inactivePeriod		 = 10;				// 21/11/2014 ECU added
		   int      lastTimeChecked		 = StaticData.NO_RESULT;
		   boolean 	midnightPassed 		 = false;			// 21/11/2014 ECU moved here
		   													//                from method
	public boolean  spansMidnight		 = false;			// 20/11/2014 ECU added
	public int		startTimeHour		 = 0;
	public int		startTimeMinute		 = 0;
	public int		stopTimeHour		 = 0;
	public int		stopTimeMinute		 = 0;
	public boolean	timedRecording		 = false;			// 24/11/2014 ECU added
	public int		triggerLevel		 = 10000;
	/* ============================================================================= */
	public Monitor (boolean theEnableFlag,int theTriggerLevel)
	{
		// -------------------------------------------------------------------------
		// 18/11/2014 ECU copy across the variables
		// -------------------------------------------------------------------------
		enabled 		= theEnableFlag;
		triggerLevel 	= theTriggerLevel;
	}
	// ==============================================================================
	public void checkTime (Context theContext)
	{
		// --------------------------------------------------------------------------
		// 21/11/2014 ECU complete rewrite but still not happy so may consider using
		//                the alarm manager.
		// --------------------------------------------------------------------------	
		if (enabled)
		{
			Calendar calendar 	= Calendar.getInstance();
			int currentTime 	= calendar.get (Calendar.HOUR_OF_DAY) * 60 +
										calendar.get (Calendar.MINUTE);
			// ---------------------------------------------------------------------
			// 01/09/2015 ECU changed to use StaticData
			// ---------------------------------------------------------------------
			if (currentTime > StaticData.MINUTES_PER_DAY) currentTime = currentTime - StaticData.MINUTES_PER_DAY;
			// ---------------------------------------------------------------------
			// 21/11/2014 ECU check if a day has passed
			// ---------------------------------------------------------------------
			if (lastTimeChecked != StaticData.NO_RESULT && !midnightPassed && currentTime < lastTimeChecked)
			{
				midnightPassed = true;
			}
			// ---------------------------------------------------------------------
			// 21/11/2014 ECU remember this time
			// ---------------------------------------------------------------------
			lastTimeChecked = currentTime;
			// ---------------------------------------------------------------------
			// 21/11/2014 ECU handle the counter which is set up after a noise had
			//                been heard. Whilst the counter > 0 then the noise
			//                will not be checked for again.
			// 23/11/2014 ECU added the check on service running
			// 28/04/2015 ECU changed to use the inactivity counter in MAinActivity
			// ---------------------------------------------------------------------
			if (PublicData.monitorServiceRunning && PublicData.monitorInactivityCounter > 0)
			{
				PublicData.monitorInactivityCounter--;
			}
			// ---------------------------------------------------------------------
			int startTime = (startTimeHour * 60) + startTimeMinute;
			int stopTime  = (stopTimeHour * 60)  + stopTimeMinute;
			
			if (!PublicData.monitorServiceRunning 
					&& (currentTime >= startTime) 
					&& (currentTime < (stopTime + (spansMidnight ? StaticData.MINUTES_PER_DAY : 0))))
			{
				// ------------------------------------------------------------------
				// 19/11/2014 ECU within the start range
				// ------------------------------------------------------------------
				// 21/11/2014 ECU initialise the inactive counter
				// 28/04/2015 ECU changed to use the counter in MainActivity
				// ------------------------------------------------------------------
				PublicData.monitorInactivityCounter = 0;
				// ------------------------------------------------------------------
				// 19/11/2014 ECU start up the monitor service
				// ------------------------------------------------------------------
				theContext.startService (new Intent (theContext,MonitorService.class));
				// ------------------------------------------------------------------
				// 21/11/2014 ECU reset the midnight checking variable
				// ------------------------------------------------------------------
				midnightPassed = false;
				// ------------------------------------------------------------------
			}
			else
			if (PublicData.monitorServiceRunning 
					&& (currentTime >= stopTime + (spansMidnight ? (midnightPassed ? 0 
																			       : StaticData.MINUTES_PER_DAY) 
																 : 0)))
			{
				// -----------------------------------------------------------------
				// 19/11/2014 ECU time to stop the service
				// -----------------------------------------------------------------
				theContext.stopService (new Intent (theContext,MonitorService.class));
				// -----------------------------------------------------------------
			}
		}			
	}
	// =============================================================================
	public String print ()
	{
		// -------------------------------------------------------------------------
		// 25/11/2014 ECU just tidy up the displayed information
		// 27/04/2015 ECU included 'email'
		// -------------------------------------------------------------------------
		return "Active Period " + String.format("%02d:%02d",startTimeHour,startTimeMinute) + " to " +
					String.format("%02d:%02d",stopTimeHour,stopTimeMinute) + "\n" +
				(spansMidnight ? "The active period spans midnight\n" : "") +
				"The inactive period after a noise is " + inactivePeriod + " minutes\n" +
				"Trigger Level = " + triggerLevel + "\n" +
				"Noise monitoring : " + (enabled ? "enabled" : "disabled") + "\n" + 
				"Send Email : " + (email ? "enabled" : "disabled") + "\n" + 
				"Service : " + (PublicData.monitorServiceRunning ? "running" : "not running") + "\n" +
				"Timed Recording is " + (timedRecording ? ("enabled for " + duration + " minutes") : "disabled");
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String returnStartTime ()
	{
		return returnTime (startTimeHour,startTimeMinute);
	}
	// =============================================================================
	public String returnStopTime ()
	{
		return returnTime (stopTimeHour,stopTimeMinute);
	}
	// =============================================================================
	public String returnTime (int theHour,int theMinute)
	{
		return String.format ("%02d:%02d",theHour,theMinute);
	}
	// =============================================================================
	public void setStartTime (String theFormattedTime)
	{
		String[] timeParts = theFormattedTime.split(":");
        
        startTimeHour 	= Integer.parseInt	(timeParts[0]);
        startTimeMinute	= Integer.parseInt	(timeParts[1]);;
	}
	// =============================================================================
	public void setStopTime (String theFormattedTime)
	{
		String[] timeParts = theFormattedTime.split(":");
        
        stopTimeHour 	= Integer.parseInt	(timeParts[0]);
        stopTimeMinute	= Integer.parseInt	(timeParts[1]);
        //--------------------------------------------------------------------------
        // 20/11/2014 ECU check whether the period spans midnight
        // -------------------------------------------------------------------------
        spansMidnight = (stopTimeHour < startTimeHour);
        // -------------------------------------------------------------------------
	}
	// =============================================================================
}

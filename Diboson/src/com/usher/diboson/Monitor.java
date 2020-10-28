package com.usher.diboson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import java.io.Serializable;


@SuppressLint("DefaultLocale")
public class Monitor implements Serializable
{
	/* ============================================================================= */
	// 18/11/2014 ECU created to contain details for the monitoring service
	// 28/04/2015 ECU removed the inactiveCounter and put in MainActivity
	// 03/12/2019 ECU added 'actions'
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	// =============================================================================
	final static String TAG		=	"Monitor";
	// =============================================================================
	public String	actions				 = null;			// 03/12/2019 ECU added
	public int		duration			 = 2;				// 24/11/2014 ECU added
	public boolean  email				 = false;			// 27/04/2015 ECU added
	public boolean	enabled				 = false;
	public int 		inactivePeriod		 = 10;				// 21/11/2014 ECU added
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
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public Monitor (boolean theEnableFlag,
	                String	theStartTime,
	                String  theStopTime,
	                int		theTriggerLevel,
	                boolean	theTimedRecording,	int	theDuration,
	                boolean theEmailFlag,
	                int     theInactivePeriod,
	                String  theActions)
	{
		// -------------------------------------------------------------------------
		// 17/12/2019 ECU set the variables from the supplied arguments
		// -------------------------------------------------------------------------
		actions			=	theActions;
		duration		=	theDuration;
		email			= 	theEmailFlag;
		enabled			= 	theEnableFlag;
		inactivePeriod	=	theInactivePeriod;
		timedRecording  = 	theTimedRecording;
		triggerLevel	= 	theTriggerLevel;
		// -------------------------------------------------------------------------
		setStartTime (theStartTime);
		setStopTime  (theStopTime);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public Monitor (Monitor theMonitor)
	{
		// -------------------------------------------------------------------------
		// 18/12/2019 ECU created to make a copy of the specified record
		// -------------------------------------------------------------------------
		Copy (theMonitor);
		// -------------------------------------------------------------------------
	}
	// =============================================================================

	// =============================================================================
	public void Compare (Context theContext,Monitor theMonitorRecord)
	{
		// -------------------------------------------------------------------------
		// 17/12/2019 ECU check whether the 'stored data' copy differs from that
		//                being supplied. If yes then it should override the
		//                stored data version and either start or stop the monitor
		//                service
		// 18/12/2019 ECU tried to use 'hash code' but this is not appropriate for
		//                this type of check
		// -------------------------------------------------------------------------
		boolean localRestart	= false;
		// -------------------------------------------------------------------------
		// 18/12/2019 ECU only check those elements which would cause a restart or
		//                close down
		// -------------------------------------------------------------------------
		if ((startTimeHour   != theMonitorRecord.startTimeHour)   ||
		    (startTimeMinute != theMonitorRecord.startTimeMinute) ||
			(stopTimeHour    != theMonitorRecord.stopTimeHour)    ||
			(stopTimeMinute  != theMonitorRecord.stopTimeMinute))
		{
			// ---------------------------------------------------------------------
			// 18/12/2019 ECU something has changed so a 'restart' is appropriate
			// ---------------------------------------------------------------------
			localRestart = true;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 18/12/2019 ECU remember the state of the 'enabled' flag because the 'Copy'
		//                will overwrite its value
		// -------------------------------------------------------------------------
		boolean localEnabled = enabled;
		// -------------------------------------------------------------------------
		// 18/12/2019 ECU update this record with that supplied
		// -------------------------------------------------------------------------
		Copy (theMonitorRecord);
		// -------------------------------------------------------------------------
		// 18/12/2019 ECU now check if the enablement has changed
		// -------------------------------------------------------------------------
		if (localEnabled != theMonitorRecord.enabled)
		{
			// ---------------------------------------------------------------------
			// 18/12/2019 ECU 'enablement' has changed so decide what to do
			// ---------------------------------------------------------------------
			if (theMonitorRecord.enabled)
			{
				if (!PublicData.monitorServiceRunning)
				{
					// -------------------------------------------------------------
					// 17/12/2019 ECU start up the monitor service
					// -------------------------------------------------------------
					theContext.startService (new Intent (theContext,MonitorService.class));
					// -------------------------------------------------------------
				}
			}
			else
			{
				if (PublicData.monitorServiceRunning)
				{
					// -------------------------------------------------------------
					// 17/12/2019 ECU stop the monitor servcie
					// -------------------------------------------------------------
					theContext.stopService (new Intent (theContext,MonitorService.class));
					// -------------------------------------------------------------
				}
			}
		}
		// -------------------------------------------------------------------------
		// 18/12/2019 ECU if already running but some of the data has changed then
		//                need to do a reset
		// -------------------------------------------------------------------------
		if (PublicData.monitorServiceRunning && localRestart)
		{
			// ---------------------------------------------------------------------
			// 18/12/2019 ECU tell the handler to restart
			// ---------------------------------------------------------------------
			MonitorHandler.Restart ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void Copy (Monitor theMonitor)
	{
		// -------------------------------------------------------------------------
		// 18/12/2019 ECU created to copy across the contents of the specified
		//                record into the current one
		// 23/12/2019 ECU added 'spansMidnight'
		// -------------------------------------------------------------------------
		actions			=	theMonitor.actions;
		duration		=	theMonitor.duration;
		email			= 	theMonitor.email;
		enabled			= 	theMonitor.enabled;
		inactivePeriod	=	theMonitor.inactivePeriod;
		spansMidnight	=	theMonitor.spansMidnight;
		startTimeHour	=   theMonitor.startTimeHour;
		startTimeMinute	=   theMonitor.startTimeMinute;
		stopTimeHour	=   theMonitor.stopTimeHour;
		stopTimeMinute	=   theMonitor.stopTimeMinute;
		timedRecording  = 	theMonitor.timedRecording;
		triggerLevel	= 	theMonitor.triggerLevel;
		// -------------------------------------------------------------------------
	}
	// =============================================================================

	// =============================================================================
	public String print ()
	{
		// -------------------------------------------------------------------------
		// 25/11/2014 ECU just tidy up the displayed information
		// 27/04/2015 ECU included 'email'
		// 03/12/2019 ECU changed to use TIME...
		//            ECU added actions
		// -------------------------------------------------------------------------
		return "Active Period " + String.format(StaticData.TIME_FORMAT,startTimeHour,startTimeMinute) + " to " +
					String.format(StaticData.TIME_FORMAT,stopTimeHour,stopTimeMinute) + StaticData.NEWLINE +
				(spansMidnight ? "The active period spans midnight\n" : StaticData.BLANK_STRING) +
				"The inactive period after a noise is " + inactivePeriod + " minutes\n" +
				"Trigger Level = " + triggerLevel + StaticData.NEWLINE +
				"Noise monitoring : " + (enabled ? "enabled" : "disabled") + StaticData.NEWLINE + 
				"Send Email : " + (email ? "enabled" : "disabled") + StaticData.NEWLINE + 
				"Service : " + (PublicData.monitorServiceRunning ? "running" : "not running") + StaticData.NEWLINE +
				"Timed Recording is " + (timedRecording ? ("enabled for " + duration + " minutes") : "disabled") +
				(Utilities.emptyString (actions) ? (StaticData.NEWLINE + "Actions : " + actions)
				                                  : StaticData.BLANK_STRING);
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
		// -------------------------------------------------------------------------
		// 03/12/2019 ECU changed to use TIME....
		// -------------------------------------------------------------------------
		return String.format (StaticData.TIME_FORMAT,theHour,theMinute);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void setStartTime (String theFormattedTime)
	{
		// -------------------------------------------------------------------------
		// 03/12/2019 ECU changed to use static
		// -------------------------------------------------------------------------
		String[] timeParts = theFormattedTime.split (StaticData.ACTION_DELIMITER);
        
        startTimeHour 	= Integer.parseInt	(timeParts[0]);
        startTimeMinute	= Integer.parseInt	(timeParts[1]);
        // -------------------------------------------------------------------------
	}
	// =============================================================================
	public void setStopTime (String theFormattedTime)
	{
		// -------------------------------------------------------------------------
		// 03/12/2019 ECU changed to use static
		// -------------------------------------------------------------------------
		String [] timeParts = theFormattedTime.split (StaticData.ACTION_DELIMITER);
        
        stopTimeHour 	= Integer.parseInt	(timeParts[0]);
        stopTimeMinute	= Integer.parseInt	(timeParts[1]);
        //--------------------------------------------------------------------------
        // 20/11/2014 ECU check whether the period spans midnight
        // 23/12/2019 ECU changing the checking to include minutes
        // -------------------------------------------------------------------------
        spansMidnight = ((stopTimeHour <  startTimeHour) ||
						((stopTimeHour == startTimeHour) && (stopTimeMinute < startTimeMinute)));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void trimFiles (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 07/12/2019 ECU trim any files in the 'noises' directory which are older
		//                than three days
		// -------------------------------------------------------------------------
		Utilities.trimFilesInDirectory (PublicData.projectFolder + theContext.getString (R.string.noises_directory),3);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

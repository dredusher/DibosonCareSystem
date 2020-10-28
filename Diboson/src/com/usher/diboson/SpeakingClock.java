package com.usher.diboson;

import java.io.Serializable;

public class SpeakingClock implements Serializable
{
	/* ============================================================================= */
	// 09/01/2014 ECU created to contain details for the speaking clock
	// 25/07/2017 ECU added westminsterChimeEnd
	//						   true ....... the chiming ends at the specified time
	//						   false ...... the chiming starts at the specified time
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	public boolean	enabled					= false;
	public int 		gap					 	= 10;
	public long		nextAlarmTime		 	= 0;		// 23/02/2014 ECU added
	public boolean  showText			 	= false;	// 01/02/2017 ECU added
		   boolean	spansMidnight		 	= false;	// 11/03/2014 ECU added
	public int		startHour			 	= 8;
	public int		startMinute			 	= 0;
	public int		startTime			 	= 0;
	public int		stopHour			 	= 18;
	public int		stopMinute			 	= 0;
	public int      stopTime			 	= 0;
	public boolean  westminsterChime	 	= false;	// 10/03/2017 ECU added
	public boolean  westminsterChimeEnd		= true;		// 25/07/2017 ECU added
	/* ============================================================================= */
	public SpeakingClock (boolean theEnableFlag,
				          int theStartHour,int theStartMinute,
				          int theStopHour,int theStopMinute,
				          int theGap,
				          boolean theShowTextFlag,
				          boolean theWestminsterChime,
				          boolean theWestminsterChimeEnd)
	{
		// -------------------------------------------------------------------------
		// 09/02/2014 ECU copy across the variables
		// 01/02/2017 ECU added the show text flag
		// 10/03/2017 ECU added the Westminster chime
		// -------------------------------------------------------------------------
		enabled				= theEnableFlag;
		gap					= theGap;
		nextAlarmTime		= 0;						// 23/02/2014 ECU added
		showText			= theShowTextFlag;			// 01/02/2017 ECU added	
		startHour			= theStartHour;
		startMinute			= theStartMinute;
		stopHour			= theStopHour;
		stopMinute			= theStopMinute;
		westminsterChime	= theWestminsterChime;		// 10/03/2017 ECU added
		westminsterChimeEnd	= theWestminsterChimeEnd;	// 25/07/2017 ECU added
		// -------------------------------------------------------------------------
		// 09/02/2014 ECU work out the minutes which will be used for testing
		// -------------------------------------------------------------------------
		startTime   = (startHour * 60) + startMinute;
		stopTime	= (stopHour  * 60) + stopMinute;
		// -------------------------------------------------------------------------
		// 11/03/2014 ECU check whether the end time is earlier than the
		//                the start time which means that the active period
		//                spans midnight
		// -------------------------------------------------------------------------
		spansMidnight = (stopTime < startTime) ? true : false;
		// -------------------------------------------------------------------------
		
	}
	/* ============================================================================= */
	public boolean IsActive (int theHour,int theMinute)
	{
		// -------------------------------------------------------------------------
		// 09/02/2014 ECU returns whether the speaking clock is enabled
		//                and if it is then is the supplied time in the
		//                active range
		// -------------------------------------------------------------------------
		if (enabled)
		{
			// ---------------------------------------------------------------------
			// 09/02/2014 ECU enabled but check the active range
			// ---------------------------------------------------------------------
			int suppliedTime = (theHour * 60) + theMinute;
			// ---------------------------------------------------------------------
			// 11/03/2014 ECU put in the check for when the times span midnight
			// ---------------------------------------------------------------------
			if (!spansMidnight)
			{
				// -----------------------------------------------------------------
				// 11/03/2014 ECU does not span midnight so normal check
				// -----------------------------------------------------------------
				
				if ((suppliedTime >= startTime) && (suppliedTime <= stopTime))
				{
					// -------------------------------------------------------------
					// 09/02/2014 ECU is in the active range
					// -------------------------------------------------------------
					return true;
				}
				else
					return false;
			}
			else
			{
				// -----------------------------------------------------------------
				// 11/03/2014 ECU the time period spans midnight so handle
				//                1) the time leading up to midnight
				//                2) the time after midnight
				// -----------------------------------------------------------------
				if (suppliedTime >= startTime && suppliedTime <= ((23 * 60) + 59))
				{
					// -------------------------------------------------------------
					// 11/03/2014 ECU period leading up to midnight
					// -------------------------------------------------------------
					return true;
				}
				else
				if (suppliedTime <= stopTime)
				{
					// -------------------------------------------------------------
					// 11/03/2014 ECU period after midnight and before the stop time
					// -------------------------------------------------------------
					return true;
				}
				else
				{
					return false;
				}
				
			}
		}
		else
		{
			// -------------------------------------------------------------------------
			// 09/02/2014 ECU not enabled
			// -------------------------------------------------------------------------
			return false;
		}	
	}
	/* ============================================================================= */
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 22/02/2014 ECU printed message depends on the enabled state
		// 09/03/2014 ECU changed with the method AdjustedNumber being
		//                moved into Utilities
		// 13/03/2014 ECU added print of 'spansMidnight'
		// -------------------------------------------------------------------------
		if (enabled)
			return "Enable State : " + enabled + StaticData.NEWLINE +
					"Active from  " + Utilities.AdjustedNumber(startHour) + ":" + Utilities.AdjustedNumber(startMinute) +
					" until " + Utilities.AdjustedNumber(stopHour) + ":" + Utilities.AdjustedNumber(stopMinute) + " \n" +
					"Interval : " + gap + " minutes" +
					(spansMidnight ? "\nPeriod Spans Midnight" : StaticData.BLANK_STRING +
					(westminsterChime ? "\n\nChimes at " + (westminsterChimeEnd ? "End" : "Start") : StaticData.BLANK_STRING));
		else
			return "The speaking clock is disabled";
	}
	/* ============================================================================= */
	public String Summary ()
	{
		// -------------------------------------------------------------------------
		// 09/02/2014 ECU return a summary that can be used by the service
		// 09/03/2014 ECU changed with the method AdjustedNumber being
		//                moved into Utilities
		// -------------------------------------------------------------------------
		
		return "Speaking Clock every " + PublicData.storedData.speakingClock.gap + " minutes from " +
					Utilities.AdjustedNumber (startHour) + ":" + Utilities.AdjustedNumber (startMinute) + " until " +
					Utilities.AdjustedNumber (stopHour) + ":" + Utilities.AdjustedNumber (stopMinute);
	}
	/* ============================================================================= */
}

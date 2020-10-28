package com.usher.diboson;

import java.io.Serializable;

public class EPGAlarm implements Serializable
{
	// =============================================================================
	// 30/09/2015 ECU created to hold information passed through to the alarm 
	//                manager
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	public  int         advanceCounter;			// 30/10/2015 ECU added
	public  long		date;					// 17/10/2015 ECU added
	public	EPGEntry	EPGEntry;
	public	String		TVChannelName;
	// =============================================================================
	
	// =============================================================================
	public EPGAlarm (EPGEntry theEPGEntry,String theTVChannelName)
	{
		// -------------------------------------------------------------------------
		// 30/09/2015 ECU pass across to internal variables
		// -------------------------------------------------------------------------
		EPGEntry		= theEPGEntry;
		TVChannelName 	= theTVChannelName;
		// -------------------------------------------------------------------------
		// 17/10/2015 ECU calculate and store the date/time defined by the EPG fields
		// -------------------------------------------------------------------------
		date = Utilities.getTime (EPGEntry.fields [StaticData.EPG_DATE],EPGEntry.fields [StaticData.EPG_START_TIME]);
		// -------------------------------------------------------------------------
		// 30/10/2015 ECU initialise the advance counter
		// -------------------------------------------------------------------------
		advanceCounter = StaticData.EPG_ADVANCE_COUNTER;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public long NextWarningTime (boolean theDecrementFlag)
	{
		// -------------------------------------------------------------------------
		// 30/10/2015 ECU created to decrement the advance counter and to return the
		//                associated time in milliseconds
		// 26/01/2017 ECU added the decrement flag
		// -------------------------------------------------------------------------
		if (theDecrementFlag)
			advanceCounter--;
		else
		{
			//----------------------------------------------------------------------
			// 26/01/2017 ECU check if there is enough time for the number of minutes
			//                required
			// ---------------------------------------------------------------------
			int minutesGap = (int)((date - Utilities.getAdjustedTime(false)) / (long) StaticData.MILLISECONDS_PER_MINUTE);
			if (advanceCounter > minutesGap)
			{
				// -----------------------------------------------------------------
				// 26/01/2017 ECU the currently set counter is too big for the minutes
				//                remaining so reset it accordingly
				// -----------------------------------------------------------------
				advanceCounter = minutesGap;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 26/01/2017 ECU added REMINDER_GAP - see the notes in ShowEPGActivity
		// -------------------------------------------------------------------------
		return (date - (advanceCounter * StaticData.MILLISECONDS_PER_MINUTE) - StaticData.EPG_REMINDER_GAP);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 17/10/2015 ECU created to print the summary
		// -------------------------------------------------------------------------
		return "Channel : " + TVChannelName + StaticData.NEWLINE + 
			   "Program : " + EPGEntry.PrintProgram();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

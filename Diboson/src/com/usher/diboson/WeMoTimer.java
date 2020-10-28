package com.usher.diboson;

import java.io.Serializable;

public class WeMoTimer implements Serializable
{
	// =============================================================================
	// 25/02/2015 ECU created to hold details of a WeMo timer
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	public static final String []	ACTIONS 			= {"Switch Off","Switch On"};
	public static final int			ACTION_SWITCH_OFF 	= 0;
	public static final int			ACTION_SWITCH_ON 	= 1;
	// =============================================================================
	public int 			action			= StaticData.NO_RESULT;
	public  boolean []	days			= {false,false,false,false,false,false,false};
	private String		friendlyName	= StaticData.BLANK_STRING;
	public  int 		hour			= StaticData.NO_RESULT;
	public  int 		minute			= StaticData.NO_RESULT;
	// =============================================================================
	public WeMoTimer ()
	{
		
	}
	// -----------------------------------------------------------------------------
	public WeMoTimer (String theFriendlyName)
	{
		friendlyName	=	theFriendlyName;
	}
	// -----------------------------------------------------------------------------
	public WeMoTimer (String theFriendlyName,boolean [] theDays,int theHour,int theMinute,int theAction)
	{
		// -------------------------------------------------------------------------
		// 25/02/2015 ECU copy across the variables
		// -------------------------------------------------------------------------
		action			= 	theAction;
		days			=   theDays;
		friendlyName	=	theFriendlyName;
		hour			=	theHour;
		minute			=	theMinute;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void actionTimer ()
	{
		// -------------------------------------------------------------------------
		// 25/02/2015 ECU action this timer
		// -------------------------------------------------------------------------
		WeMoService.SetDeviceState (friendlyName, (action == ACTION_SWITCH_ON));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public boolean checkTimer (int theDay,int theHour,int theMinute)
	{
		// -------------------------------------------------------------------------
		// 25/02/2015 ECU checks whether the timer matches the input values
		//            ECU '-1' because Sunday in Calendar is 1 not 0
		// 11/12/2016 ECU tidied up with the use of daysOfTheWeek
		// -------------------------------------------------------------------------
		return (days[theDay] && (hour == theHour) && (minute == theMinute));
		// -------------------------------------------------------------------------
		
	}
	// =============================================================================
	public String getFriendlyName ()
	{
		// -------------------------------------------------------------------------
		// 27/02/2015 ECU returns the friendly name associated with this timer
		// -------------------------------------------------------------------------
		return friendlyName;
	}
	// =============================================================================
	public static int numberOfTimers (String theFriendlyName)
	{
		// -------------------------------------------------------------------------
		// 01/03/2015 ECU return the number of timers for the device whose friendly
		//                name is specified
		// 08/03/2015 ECU put in the 'null' check as a 'belt and braces' measure
		// -------------------------------------------------------------------------
		int		timerCount = 0;
		// -------------------------------------------------------------------------
		if ((PublicData.storedData.wemoTimers != null) && (PublicData.storedData.wemoTimers.size() > 0))
		{
			// ---------------------------------------------------------------------
			// 27/02/2015 ECU because the list array can be sorted then the original
			//                index needed to be stored in the individual item
			//                hence the need to use theDeviceIndex
			// ---------------------------------------------------------------------
			for (int theIndex = 0; theIndex < PublicData.storedData.wemoTimers.size(); theIndex++)
			{
				// -----------------------------------------------------------------
				// 27/02/2015 ECU check if timer is for this device
				// -----------------------------------------------------------------
				if (PublicData.storedData.wemoTimers.get (theIndex).getFriendlyName ().equals(theFriendlyName))
				{
					// -------------------------------------------------------------
					// 01/03/2015 ECU a timer has been found for the specified device
					//                so increment the counter
					// -------------------------------------------------------------
					timerCount++;
				}
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 01/03/2015 ECU return the number of timers found for the device
		// -------------------------------------------------------------------------
		return timerCount;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 25/02/2015 ECU print out details of the stored timer
		// 27/02/2015 ECU changed to use the PrintTime method
		// -------------------------------------------------------------------------
		String printString = "Device : " + friendlyName + StaticData.NEWLINE +
			   "Time : " + PrintTime() + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 25/02/2015 ECU print out the days
		// 11/12/2016 ECU changed to use daysOfTheWeek
		// -------------------------------------------------------------------------
		for (int theDay = 0; theDay < days.length; theDay++)
		{
			if (days [theDay])
				printString += PublicData.daysOfTheWeek [theDay] + StaticData.NEWLINE;
		}
		// -------------------------------------------------------------------------   
		return printString + "Action : " + ACTIONS [action];
	}
	// =============================================================================
	public String PrintAction ()
	{
		// -------------------------------------------------------------------------
		// 27/02/2015 ECU created to return the stored action
		// -------------------------------------------------------------------------
		return "Action : " + ACTIONS [action];
	}
	// =============================================================================
	public String PrintAll ()
	{
		// -------------------------------------------------------------------------
		// 27/02/2015 ECU return a summary of all defined timers
		// -------------------------------------------------------------------------
		String printString = StaticData.BLANK_STRING;
		if (PublicData.storedData.wemoTimers.size() > 0)
		{
			for (int theTimer = 0; theTimer < PublicData.storedData.wemoTimers.size(); theTimer++)
			{
				printString += "Timer Number : " + theTimer + "\n=================\n" +
									PublicData.storedData.wemoTimers.get(theTimer).Print() + "\n\n";
				
			}
		}
		// -------------------------------------------------------------------------
		return printString;
	}
	// =============================================================================
	public String PrintDays ()
	{
		String resultString = "Days :";
		// -------------------------------------------------------------------------
		// 27/02/2015 ECU generate the string for the days for which the timer
		//                applies
		// 11/12/2016 ECU changed to use daysOfTheWeek
		// -------------------------------------------------------------------------
		for (int theDay = 0; theDay < days.length; theDay++)
		{
			if (days [theDay])
				resultString += " " + String.format ("%.3s",PublicData.daysOfTheWeek [theDay]);
		}
		// -------------------------------------------------------------------------
		return resultString;
	}
	// =============================================================================
	public String PrintTime ()
	{
		// -------------------------------------------------------------------------
		// 27/02/2015 ECU created to return the stored time
		// -------------------------------------------------------------------------
		return "Time : " + String.format("%02d",hour) + ":" + String.format("%02d",minute);
	}
	// =============================================================================
	public void setDetails (int theHour,int theMinute)
	{
		hour			=	theHour;
		minute			=	theMinute;
	}
	// =============================================================================
}

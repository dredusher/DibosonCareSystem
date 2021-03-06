package com.usher.diboson;

import java.io.Serializable;
import java.util.Calendar;

public class DibosonTime implements Serializable
{
	// =============================================================================
	// 26/11/2015 ECU created to hold hour / minute data
	// =============================================================================
	private static final long serialVersionUID = 1L;
	
	// =============================================================================
	int	hour;
	int	minute;
	// =============================================================================
	
	// =============================================================================
	public DibosonTime (int theHour, int theMinute)
	{
		// -------------------------------------------------------------------------
		// 26/11/2015 ECU created as constructor to set the variables from the
		//                arguments
		// -------------------------------------------------------------------------
		hour	= theHour;
		minute	= theMinute;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public DibosonTime (DibosonTime theTime)
	{
		// -------------------------------------------------------------------------
		// 26/11/2015 ECU created when setting this new object from another
		// -------------------------------------------------------------------------
		hour	= theTime.hour;
		minute	= theTime.minute;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public DibosonTime (long theDateInMilliseconds)
	{
		// -------------------------------------------------------------------------
		// 14/03/2016 ECU created to take a date (supplied in milliseconds) and to
		//                extract the hours and minutes
		// -------------------------------------------------------------------------
		Calendar localCalendar = Calendar.getInstance ();
		localCalendar.setTimeInMillis (theDateInMilliseconds);
		// -------------------------------------------------------------------------
		// 14/03/2016 ECU now extract the required components
		// -------------------------------------------------------------------------
		hour 	= localCalendar.get (Calendar.HOUR_OF_DAY);
		minute	= localCalendar.get(Calendar.MINUTE);
		// -------------------------------------------------------------------------
	}
	// =============================================================================

	// =============================================================================
	
	// =============================================================================
	public void adjustTime (int theAdjustment)
	{
		// -------------------------------------------------------------------------
		// 26/11/2015 ECU created to adjust the stored time by 'theAdjustment' which
		//                is supplied in minutes - theAdjustment can span several
		//                hours
		// -------------------------------------------------------------------------
		int	localHour 	= theAdjustment / 60;
		int localMinute = theAdjustment - (localHour * 60);
		// -------------------------------------------------------------------------
		// 26/11/2015 ECU adjust the hour first
		// -------------------------------------------------------------------------
		hour += localHour;
		if (hour > 23)
			hour -= 24;
		// -------------------------------------------------------------------------
		// 26/11/2015 ECU adjust the minute
		// -------------------------------------------------------------------------
		minute += localMinute;
		if (minute > 59)
		{
			// ---------------------------------------------------------------------
			// 26/11/2015 ECU gone across the hour so adjust
			// ---------------------------------------------------------------------
			minute -= 60;
			// ---------------------------------------------------------------------
			// 26/11/2015 ECU increment the hour
			// ---------------------------------------------------------------------
			hour++;
			if (hour > 23)
				hour -= 24;
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public long MilliSeconds ()
	{
		// -------------------------------------------------------------------------
		// 14/03/2016 ECU created to returen the time in milliseconds
		// -------------------------------------------------------------------------
		Calendar localCalendar = Calendar.getInstance ();
		localCalendar.set (Calendar.HOUR_OF_DAY,hour);
		localCalendar.set (Calendar.MINUTE,minute);
		localCalendar.set (Calendar.SECOND,0);
		// -------------------------------------------------------------------------
		// 14/03/2016 ECU return the time in milliseconds
		// -------------------------------------------------------------------------
		return localCalendar.getTimeInMillis();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public int Minutes ()
	{
		// -------------------------------------------------------------------------
		// 26/11/2015 ECU return the stored time in minutes
		// -------------------------------------------------------------------------
		return (hour * 60) + minute;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 26/11/2015 ECU created to return the time in printable form
		// 03/12/2019 ECU changed to use TIME....
		// -------------------------------------------------------------------------
		return String.format (StaticData.TIME_FORMAT,hour,minute);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

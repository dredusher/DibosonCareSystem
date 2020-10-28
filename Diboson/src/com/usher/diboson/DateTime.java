package com.usher.diboson;

public class DateTime 
{
	// =============================================================================
	// 07/03/2015 ECU provides the structure for time and date and utilities to set 
	//                the components
	// =============================================================================
	public	int		hour;
	public	int		minute;
	// =============================================================================
	
	// =============================================================================
	public DateTime (int theHour,int theMinute)
	{
		// -------------------------------------------------------------------------
		// 07/03/2015 ECU constructor to set hour and minute components
		// -------------------------------------------------------------------------
		hour	=	theHour;
		minute	=	theMinute;
	}
	// =============================================================================
	
	// =============================================================================
	public static DateTime returnTime (String theTimeString)
	{
		// -------------------------------------------------------------------------
		// 07/03/2015 ECU returns a DateTime object with hour and minute set. The
		//                input argument will have the format '<hour>:<minute>'
		// -------------------------------------------------------------------------
		String[] timeParts = theTimeString.split(":");
		
		return (new DateTime (Integer.parseInt	(timeParts[0]),Integer.parseInt	(timeParts[1])));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String returnTime (int theHour,int theMinute)
	{
		// -------------------------------------------------------------------------
		// 07/03/2015 ECU return the time as a string of the correct format
		// -------------------------------------------------------------------------
		return String.format ("%02d:%02d",theHour,theMinute);
	}
	// =============================================================================
}

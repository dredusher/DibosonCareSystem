package com.usher.diboson;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class RecordedNote implements Serializable 
{
	// =============================================================================
	// 26/10/2016 ECU created to hold details of a recorded audio note
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	long	endTime;
	String	fileName;
	long	startTime;
	// =============================================================================
	
	// =============================================================================
	public RecordedNote (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 26/10/2016 ECU create an object for the specified filename and set the
		//                time when this occurred
		// -------------------------------------------------------------------------
		fileName	= theFileName;
		startTime = Utilities.getAdjustedTime (true);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public String Duration ()
	{
		// -------------------------------------------------------------------------
		// 26/10/2016 ECU get the duration as a string
		// 24/07/2017 ECU changed to use ALARM...
		// -------------------------------------------------------------------------
		SimpleDateFormat localFormat = new SimpleDateFormat (StaticData.ALARM_TIME_FORMAT,Locale.getDefault());
		localFormat.setTimeZone (TimeZone.getTimeZone ("UTC"));
		return localFormat.format ((endTime - startTime));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void setEndTime ()
	{
		// -------------------------------------------------------------------------
		// 26/10/2016 ECU created to set the end time
		// -------------------------------------------------------------------------
		endTime = Utilities.getAdjustedTime (true);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String StartTime (boolean theColonFlag)
	{
		// -------------------------------------------------------------------------
		// 26/10/2016 ECU return the start time in printable form
		// 27/10/2016 ECU changed to use the date_format resource
		// 28/10/2016 ECU changed to use dateFormatDDMMYYYY
		// -------------------------------------------------------------------------
		String localString = (new SimpleDateFormat (PublicData.dateFormatDDMMYYYY + " HH:mm",Locale.getDefault())).format (startTime);
		// -------------------------------------------------------------------------
		// 26/10/2016 ECU check if the ':' needs to be changed to '-' - because of
		//                use in an actions string
		// 16/11/2019 ECU changed from 'replaceAll' to 'replace' because
		//                the former requires a REGEX so not sure why it ever
		//				  worked
		// -------------------------------------------------------------------------
		return (theColonFlag ? (localString.replace (":", "-")) : localString);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

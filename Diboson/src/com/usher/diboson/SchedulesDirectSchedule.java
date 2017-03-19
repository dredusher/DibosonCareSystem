package com.usher.diboson;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class SchedulesDirectSchedule implements Serializable
{
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	String	date;
	int		duration;
	String	time;
	String	programID;
	long    startTime;
	String	endTime;
	// =============================================================================
	public SchedulesDirectSchedule (long theStartTime,String theDate,String theTime,int theDuration,String theProgramID)
	{
		date = theDate;
		duration = theDuration;
		programID = theProgramID;
		time = theTime;
		// -------------------------------------------------------------------------
		// 21/07/2016 ECU store the time and calculate the end time
		// -------------------------------------------------------------------------
		startTime = theStartTime;
		long localEndTime   = startTime + (long) (duration * 1000);
		// -------------------------------------------------------------------------
		SimpleDateFormat timeFormat = new SimpleDateFormat ("HH:mm:ss",Locale.getDefault());
		timeFormat.setTimeZone (TimeZone.getDefault());
		endTime = timeFormat.format (localEndTime);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print ()
	{
		return ("Date : " + date + "  Time : " + time + " Duration : " + duration );
	}
	// =============================================================================
}

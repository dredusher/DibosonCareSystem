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
		// 24/07/2017 ECU changed to use ALARM...
		// -------------------------------------------------------------------------
		SimpleDateFormat timeFormat = new SimpleDateFormat (StaticData.ALARM_TIME_FORMAT,Locale.getDefault());
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

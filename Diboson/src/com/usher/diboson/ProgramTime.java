package com.usher.diboson;

import java.io.Serializable;

public class ProgramTime implements Serializable
{
	// =============================================================================
	// 16/09/2015 ECU created to hold the timings of a program
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	private long	endTime;				// the end time of the program
	private	long	startTime;				// the start time of the program
	// =============================================================================
	
	// =============================================================================
	public ProgramTime (long theStartTime,long theEndTime)
	{
		// -------------------------------------------------------------------------
		// 16/09/2015 ECU constructor to define the details of a program
		// -------------------------------------------------------------------------
		endTime		= theEndTime;
		startTime	= theStartTime;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print ()
	{
		return "Start Time " + startTime + " End Time " + endTime;
	}
	// =============================================================================
}

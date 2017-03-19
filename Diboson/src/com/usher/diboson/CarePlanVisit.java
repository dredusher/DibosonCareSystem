package com.usher.diboson;

import java.io.Serializable;
import java.util.List;

public class CarePlanVisit implements Serializable
{
	/* ============================================================================= */
	// 12/01/2014 ECU created to contain details of a carer
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	public long			startTime;			// start time of the visit
	public int			duration;			// duration of the visit in minutes	
	public int			agencyIndex;		// index of the agency
	public int			carerIndex;			// index of the carer
	public boolean [] 	tasks;				// correspond to entries in CarePlanVisitActivity
	/* ============================================================================= */
	public CarePlanVisit (long theStartTime,int theDuration,int theAgencyIndex,
								int theCarerIndex,boolean [] theTaskFlags)
	{
		// -------------------------------------------------------------------------
		// 12/01/2014 ECU copy variables into the class on creation
		// -------------------------------------------------------------------------
		startTime		= theStartTime;
		duration		= theDuration;
		agencyIndex		= theAgencyIndex;
		carerIndex		= theCarerIndex;
		tasks			= theTaskFlags;		// 13/01/2014 ECU added
	}
	// =============================================================================
	public Carer CarerRecord ()
	{
		// -------------------------------------------------------------------------
		// 02/10/2016 ECU return the carer record for this visit
		// -------------------------------------------------------------------------
		return PublicData.carers.get (carerIndex);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public long EndTime ()
	{
		// -------------------------------------------------------------------------
		// 31/01/2016 ECU created to return the end of the visit
		// -------------------------------------------------------------------------
		return startTime + (long) (duration * StaticData.MILLISECONDS_PER_MINUTE);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 08/12/2016 ECU a bit of a tidy up to use Static rather than literals
		// -------------------------------------------------------------------------
		String localString =  "Start Time : " + PublicData.dateFormatterShort.format (startTime) + StaticData.NEWLINE +
							  "Duration   : " + duration + " minute" + Utilities.AddAnS (duration) + StaticData.NEWLINE +
							  "Agency     : " + (PublicData.agencies.get(agencyIndex).name) + StaticData.NEWLINE +
							  "Carer      : " + (PublicData.carers.get(carerIndex).name) + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		String localTaskString = "Tasks      : ";
		
		for (int theIndex = 0; theIndex < tasks.length; theIndex++)
		{
			if (tasks [theIndex])
			{
				// -----------------------------------------------------------------
				// 06/12/2016 ECU change to use PublicData - no need to set the
				//                preferred patient name as this is done once
				//                on initialisation
				// -----------------------------------------------------------------
				localString += localTaskString + PublicData.tasksToDo [theIndex] + StaticData.NEWLINE;
				// -----------------------------------------------------------------
				// 31/08/2015 ECU reset the header for the task field
				// -----------------------------------------------------------------
				localTaskString = "           : ";
				// -----------------------------------------------------------------
			}
		}
		
		return localString;
	}
	// =============================================================================
	public static String printSelected (List<CarePlanVisit> visits)
	{
		// -------------------------------------------------------------------------
		// 01/03/2017 ECU created to return a summary of the selected visits
		// -------------------------------------------------------------------------
		String summaryString = StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
		// 01/03/2017 ECU loop through the visits
		// -------------------------------------------------------------------------
		for (int visit = 0; visit < visits.size(); visit++)
		{
			// ---------------------------------------------------------------------
			// 01/03/2017 ECU add in a summary of this visit
			// ---------------------------------------------------------------------
			summaryString += visits.get (visit).Print ();
			// ---------------------------------------------------------------------
			// 02/03/2017 ECU print a separating line
			// ---------------------------------------------------------------------
			summaryString += StaticData.SEPARATOR_LOWER;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 01/03/2017 ECU return the generated string
		// -------------------------------------------------------------------------
		return summaryString;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public String PrintStartTime ()
	{
		return "Start Time : " + PublicData.dateFormatterShort.format (startTime);
	}
	/* ============================================================================= */
	public String ShortPrint ()
	{
		return PublicData.dateFormatterShort.format (startTime) + " for " + duration + " minutes by " + (PublicData.carers.get(carerIndex).name);
	}
	// =============================================================================
}

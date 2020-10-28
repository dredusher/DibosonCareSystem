package com.usher.diboson;

import java.io.Serializable;
import java.util.List;

public class CarePlanVisit implements Serializable
{
	/* ============================================================================= */
	// 12/01/2014 ECU created to contain details of a carer
	// 25/03 2017 ECU put in methods to try and adjust the stored tasks if the
	//                PublicData.tasksToDo has been edited
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	public long			startTime;			// start time of the visit
	public int			duration;			// duration of the visit in minutes	
	public int			agencyIndex;		// index of the agency
	public int			carerIndex;			// index of the carer
	public boolean [] 	tasks;				// correspond to entries in CarePlanVisitActivity
	/* ============================================================================= */
	public CarePlanVisit (long theStartTime,
			              int theDuration,
			              int theAgencyIndex,
						  int theCarerIndex,
						  boolean [] theTaskFlags)
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
	public void tasksAdjustment (int theLength)
	{
		// -------------------------------------------------------------------------
		// 25/03/2017 ECU the length of the master tasks list has altered
		// -------------------------------------------------------------------------
		boolean [] newTasks = new boolean [theLength];
		// -------------------------------------------------------------------------
		if (theLength > tasks.length)
		{
			// ---------------------------------------------------------------------
			// 25/03/2017 ECU the tasks list has grown in size so create new list
			// ---------------------------------------------------------------------
			// 25/03/2017 ECU copy across the existing tasks - the extra entries will
			//                be false by default
			// ---------------------------------------------------------------------
			for (int task = 0; task < tasks.length; task++)
				newTasks [task] = tasks [task];
			// ---------------------------------------------------------------------	
		}
		else
		{
			// ---------------------------------------------------------------------
			// 25/03/2017 ECU there are less entries now so only copy up to the new
			//                new length
			// ---------------------------------------------------------------------
			for (int task = 0; task < theLength; task++)
				newTasks [task] = tasks [task];
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 25/03/2017 ECU now reset the original entry
		// -------------------------------------------------------------------------
		tasks = newTasks;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void tasksAdjustmentAll ()
	{
		// -------------------------------------------------------------------------
		// 25/03/2017 ECU created to modify the tasks associated with a visit
		//                following the addition or deletion of tasks
		// -------------------------------------------------------------------------
		int tasksLength = PublicData.tasksToDo.length;
		// -------------------------------------------------------------------------
		for (int theDayIndex = 0; theDayIndex < PublicData.daysOfTheWeek.length; theDayIndex++)
		{
			// ---------------------------------------------------------------------
			// 25/03/2017 ECU check if any visits defined for the indexed day
			// ---------------------------------------------------------------------
			if (PublicData.carePlan.visits [theDayIndex] != null)
			{
				// -----------------------------------------------------------------
				// 25/03/2017 ECU check if there are any visits
				// -----------------------------------------------------------------
				if (PublicData.carePlan.visits [theDayIndex].size() > 0)
				{
					// -------------------------------------------------------------
					// 25/03/2017 ECU loop for each visit
					// -------------------------------------------------------------
					for (int theVisit = 0; theVisit < PublicData.carePlan.visits [theDayIndex].size(); theVisit++)
					{
						if (PublicData.carePlan.visits [theDayIndex].get(theVisit).tasks.length != PublicData.tasksToDo.length)
						{
							// -----------------------------------------------------
							// 25/03/2017 ECU get each visit to adjust its stored tasks
							// ------------------------------------------------------
							PublicData.carePlan.visits [theDayIndex].get(theVisit).tasksAdjustment (tasksLength);
							// ------------------------------------------------------
						}
					}
					// -------------------------------------------------------------
				}
				// ----------------------------------------------------------------- 
			}
			// ---------------------------------------------------------------------
		}
		// ------------------------------------------------------------------------
	}
	// =============================================================================
}

package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CarePlan implements Serializable
{
	/* ================================================================= */
	// 12/01/2014 ECU created to contain details of a carer
	// 24/07/2019 ECU provide a new method 'Print (day ...)' so that the
	//                planned visits for the specified day can be printed.
	//                Changed 'Print ()' to use this method.
	/* ================================================================= */
	private static final long serialVersionUID = 1L;
	/* ================================================================= */
	@SuppressWarnings("unchecked")
	public List<CarePlanVisit> [] visits = (ArrayList<CarePlanVisit>[]) new ArrayList [StaticData.DAYS_PER_WEEK];
	/* ================================================================= */
	public CarePlan ()
	{
		for (int theIndex  = 0; theIndex < StaticData.DAYS_PER_WEEK; theIndex++)
		{
			visits [theIndex] = new ArrayList<CarePlanVisit> ();
		}
	}
	/* ================================================================= */
	public String Print ()
	{
		String localMessage = StaticData.BLANK_STRING;
		
		for (int theIndex = 0; theIndex < PublicData.daysOfTheWeek.length; theIndex++)
		{
			// ---------------------------------------------------------------------
			// 11/12/2016 ECU changed to use 'daysOfWeek'
			// 24/07/2019 ECU changed to use the new method
			// ----------------------------------------------------------------------
			localMessage += Print (theIndex);
			// ---------------------------------------------------------------------
			// 31/08/2015 ECU add a separating blank line
			// ---------------------------------------------------------------------
			localMessage += StaticData.NEWLINE;
		}
		return localMessage;
	}
	// =============================================================================
	public String Print (int theDayOfTheWeek)
	{
		String localMessage = StaticData.BLANK_STRING;
		
		// ---------------------------------------------------------------------
		// 11/12/2016 ECU chaned to use 'daysOfWeek'
		// ----------------------------------------------------------------------
		localMessage += "Visits for " + PublicData.daysOfTheWeek [theDayOfTheWeek] + 
										"\n====================\n";
		// ----------------------------------------------------------------------	
		if (visits [theDayOfTheWeek].size() > 0)
		{
			for (int theVisit = 0; theVisit < visits [theDayOfTheWeek].size(); theVisit++)
			{
				localMessage += visits [theDayOfTheWeek].get (theVisit).Print () +
						"----------------------------\n";			
			}
		}
		else
		{
			localMessage += "There are no scheduled visits\n";
		}
		return localMessage;
	}
	/* ============================================================================= */
	public static List <CarePlanVisit> getPlan ()
	{
		// -------------------------------------------------------------------------
		// 02/10/2016 ECU called to check if there is a visit planned for 'now'
		//                need to get today's day and the time
		// 30/04/2020 ECU changed to return an array because there may be more than
		//                one carer visiting at the same time
		// -------------------------------------------------------------------------
		List <CarePlanVisit> visits = new ArrayList<CarePlanVisit> ();
		// -------------------------------------------------------------------------
		int localDay = Utilities.DayOfWeek ();
		Calendar localCalendar = Calendar.getInstance ();
		long localTime = Utilities.ConvertTime (localCalendar.get(Calendar.HOUR_OF_DAY),localCalendar.get (Calendar.MINUTE));
		// -------------------------------------------------------------------------
		// 02/10/2016 ECU now scan through the schedules looking for a match
		// -------------------------------------------------------------------------
		List <CarePlanVisit> localVisits = PublicData.carePlan.visits [localDay];
		// -------------------------------------------------------------------------
		if (localVisits.size () > 0)
		{
			for (int index = 0; index < localVisits.size(); index++)
			{
				// -----------------------------------------------------------------
				// 05/10/2016 ECU add in the 'slop time' either side
				// ------------------------------------------------------------------
				if ((localTime >= (localVisits.get (index).startTime) - StaticData.CARER_VISIT_SLOP_TIME) && 
					(localTime <= (localVisits.get (index).EndTime() + StaticData.CARER_VISIT_SLOP_TIME)))
				{
					// -------------------------------------------------------------
					// 02/10/2016 ECU the time is within a scheduled visit
					// 30/04/2020 ECU store this plan into the list
					// -------------------------------------------------------------
					visits.add (localVisits.get (index));
					// -------------------------------------------------------------
				}
			}
		}
		// -------------------------------------------------------------------------
		// 02/10/2016 ECU nothing matched so indicate that fact
		// 30/04/2020 ECU return the retrieved visits
		// -------------------------------------------------------------------------
		return visits;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public int HashCode ()
	{
		// -------------------------------------------------------------------------
		// 30/03/2016 ECU created to generate a hashcode from the internal lists
		// -------------------------------------------------------------------------
		int localHashCode = 0;
		
		for (int theIndex  = 0; theIndex < StaticData.DAYS_PER_WEEK; theIndex++)
		{
			localHashCode += visits [theIndex].hashCode();
		}
		// -------------------------------------------------------------------------
		// 30/03/2016 ECU return the generated hashcode
		// -------------------------------------------------------------------------
		return localHashCode;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

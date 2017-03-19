package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CarePlan implements Serializable
{
	/* ================================================================= */
	// 12/01/2014 ECU created to contain details of a carer
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
		String localMessage = "";
		
		for (int theIndex = 0; theIndex < PublicData.daysOfTheWeek.length; theIndex++)
		{
			// ---------------------------------------------------------------------
			// 11/12/2016 ECU chaned to use 'daysOfWeek'
			// ----------------------------------------------------------------------
			localMessage += "Visits for " + PublicData.daysOfTheWeek [theIndex] + 
										"\n====================\n";
			
			if (visits [theIndex].size() > 0)
			{
				for (int theVisit = 0; theVisit < visits [theIndex].size(); theVisit++)
				{
					localMessage += visits [theIndex].get(theVisit).Print() +
							"----------------------------\n";			
				}
			}
			else
			{
				localMessage += "There are no scheduled visits\n";
			}
			// ---------------------------------------------------------------------
			// 31/08/2015 ECU add a separating blank line
			// ---------------------------------------------------------------------
			localMessage += "\n";
		}
		return localMessage;
	}
	/* ============================================================================= */
	public static CarePlanVisit getPlan ()
	{
		// -------------------------------------------------------------------------
		// 02/10/2016 ECU called to check if there is a visit planned for 'now'
		//                need to get today's day and the time
		// -------------------------------------------------------------------------
		int localDay = Utilities.DayOfWeek ();
		Calendar localCalendar = Calendar.getInstance ();
		long localTime = Utilities.ConvertTime (localCalendar.get(Calendar.HOUR_OF_DAY),localCalendar.get(Calendar.MINUTE));
		// -------------------------------------------------------------------------
		// 02/10/2016 ECU now scan through the schedules looking for a match
		// -------------------------------------------------------------------------
		List <CarePlanVisit> localVisits = PublicData.carePlan.visits [localDay];
		if (localVisits.size() > 0)
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
					// -------------------------------------------------------------
					return localVisits.get (index);
					// -------------------------------------------------------------
				}
			}
		}
		// -------------------------------------------------------------------------
		// 02/10/2016 ECU nothing matched so indicate that fact
		// -------------------------------------------------------------------------
		return null;
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

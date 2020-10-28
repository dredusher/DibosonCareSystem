package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DoseDaily implements Serializable
{
	/* ==================================================================== */
	// 29/05/2013 ECU remember the number of stored dose times
	// 16/01/2014 ECU changed DoseTime to List<>
	/* ==================================================================== */
	private static final long serialVersionUID = 1L;
	/* ==================================================================== */
	List<DoseTime> doseTimes = new ArrayList<DoseTime>();
	/* ==================================================================== */
	public static ArrayList<ListItem> BuildList (int medicationIndex)
	{
		// -------------------------------------------------------------------------
		// 01/04/2014 ECU created to build up the list for the Selector class
		// 26/02/2016 ECU Error - get an 'index out of range' error here when
		//                deleting the medication from the dose list
		// -------------------------------------------------------------------------
		ArrayList<ListItem> listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 22/02/2016 ECU try and trap an error - even though need to find the reason
		// 11/12/2016 ECU changed to use daysOfTheWeek
		// -------------------------------------------------------------------------
		try
		{
			for (int theIndex = 0; theIndex < StaticData.DAYS_PER_WEEK; theIndex++)
			{
				listItems.add (new ListItem (Utilities.AbsoluteFileName (PublicData.medicationDetails.get (medicationIndex).photo),
							   PublicData.daysOfTheWeek [theIndex],
							   (PublicData.medicationDetails.get (medicationIndex).dailyDoseTimes [theIndex] != null) ? 
									   "Number of doses = " + PublicData.medicationDetails.get (medicationIndex).dailyDoseTimes [theIndex].doseTimes.size() : 
										   "No doses",
							   StaticData.BLANK_STRING,
							   theIndex));
			}
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 26/02/2016 ECU only exception is probably 'out of range'
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		return listItems;
	}
	/* ============================================================================= */
	public String Print (String theInset)
	{
		// -------------------------------------------------------------------------
		// 16/01/2014 ECU changed to reflect use of List<>
		// 17/01/2014 ECU change the layout of the string
		// 29/11/2017 ECU bit of tidying up
		//            ECU added theInset as argument
		// -------------------------------------------------------------------------
		String theString = "     Number of Doses this day : " + doseTimes.size() + StaticData.NEWLINE;
	
		for (int index = 0; index < doseTimes.size(); index++)
		{
			theString += "          Dose : " + (index  + 1) + "     " + doseTimes.get (index).Print (theInset) + StaticData.NEWLINE;
		}
		// -------------------------------------------------------------------------		
		return theString;
	}
	// =============================================================================
}

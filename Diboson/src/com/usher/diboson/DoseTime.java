package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;

public class DoseTime implements Serializable
{
	/* ================================================================= */
	// 29/05/2013 ECU created
	/* ================================================================= */
	private static final long serialVersionUID = 1L;
	/* ================================================================= */
	int		hours;					// hour for dose to be taken
	int		minutes;				// minute for dose to be taken
	Dose 	dose;					// amount + units of dose
	String	notes;					// any notes required
	/* ================================================================= */
	public static ArrayList<ListItem> BuildList (int medicationIndex,int doseIndex)
	{
		// -------------------------------------------------------------------------
		// 01/04/2014 ECU created to build up the list for the Selector class
		// 27/11/2014 ECU put in the checks on size 
		// -------------------------------------------------------------------------
		ArrayList<ListItem> listItems = new ArrayList<ListItem>();
		
		if (PublicData.medicationDetails.get(medicationIndex).dailyDoseTimes[doseIndex].doseTimes.size() > 0)
		{	
			for (int theIndex = 0; theIndex < PublicData.medicationDetails.get(medicationIndex).dailyDoseTimes[doseIndex].doseTimes.size(); theIndex++)
			{
				listItems.add (new ListItem (Utilities.AbsoluteFileName(PublicData.medicationDetails.get(medicationIndex).photo),
					    PublicData.medicationDetails.get(medicationIndex).name + StaticData.NEWLINE +
						   PublicData.medicationDetails.get(medicationIndex).dailyDoseTimes[doseIndex].doseTimes.get(theIndex).PrintTime (),
						PublicData.medicationDetails.get(medicationIndex).dailyDoseTimes[doseIndex].doseTimes.get(theIndex).dose.Print (StaticData.INSET),
						PublicData.medicationDetails.get(medicationIndex).dailyDoseTimes[doseIndex].doseTimes.get(theIndex).PrintNotes (StaticData.INSET),
						theIndex));
			}
		}
		// -------------------------------------------------------------------------
		return listItems;
	}
	/* ============================================================================= */
	public String PrintNotes (String theInset)
	{
		// -------------------------------------------------------------
		// 25/03/2014 ECU created to return the notes
		// 29/11/2017 ECU added theInset
		// -------------------------------------------------------------
		return theInset + "Notes : " + notes;
	}
	/* ================================================================= */
	public String PrintTime ()
	{
		// -------------------------------------------------------------
		// 25/03/2014 ECU created to return the formatted time of dose
		// ------------------------------------------------------------- 
		return String.format ("Time of Dose : %02d:%02d",hours,minutes);
	}
	/* ================================================================= */
	public String Print (String theInset)
	{
		// -------------------------------------------------------------
		// 27/11/2014 ECU use PrintTime method for formatted time
		// 28/11/2017 ECU changed to use new PrintD.. method
		// 29/11/2017 ECU added theInset argument and pass through
		// -------------------------------------------------------------
		return PrintTime () + StaticData.NEWLINE + PrintDoseTime (theInset);
		// -------------------------------------------------------------
	}
	// =================================================================
	public String PrintDoseTime (String theInset)
	{
		// -------------------------------------------------------------
		// 28/11/2017 ECU created to just print dose time without actual
		//                time
		// -------------------------------------------------------------
		return dose.Print (theInset) + StaticData.NEWLINE + theInset + "Notes  : " + notes;
		// -------------------------------------------------------------
	}
	/* ================================================================= */
}

package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MedicationDetails implements Serializable
{	
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	String			actionsConfirmed = null;// 02/08/2019 ECU added - actions to be taken
											//                when a dose is confirmed
	String			actionsRejected = null;	// 02/08/2019 ECU added - actions to be taken
											//                when a dose is rejected
	String 			name;
	String 			description;
	String			form;
	String 			photo = null;			// 23/06/2013 ECU file name of associated file
											// 10/12/2013 ECU change to full path	
	DoseDaily [] 	dailyDoseTimes = null;
											// 16/01/2014 ECU there will be an entry in the 
											//			  following array for each day of the 
	                            			//            week
	int				stock;
	/* ============================================================================= */
	public boolean DoseExists (int theRequiredDay)
	{
		// -------------------------------------------------------------------------
		// 25/03/2014 ECU check if the correct dose elements have been initialised
		// -------------------------------------------------------------------------
		if (dailyDoseTimes == null)
			return false;
		else
		if (dailyDoseTimes [theRequiredDay] == null)
			return false;
		else
		if (dailyDoseTimes [theRequiredDay].doseTimes.size() == 0)
			return false;
		
		// -------------------------------------------------------------------------
		// 25/03/2014 ECU indicate that specified day has some doses
		// -------------------------------------------------------------------------
		return true;
	}
	/* ============================================================================= */
	public static ArrayList<ListItem> BuildList ()
	{
		// -------------------------------------------------------------------------
		// 01/04/2014 ECU created to build up the list for the Selector class
		// -------------------------------------------------------------------------
		ArrayList<ListItem> listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 27/11/2014 ECU add in the check on size
		// -------------------------------------------------------------------------
		if (PublicData.medicationDetails.size() > 0)
		{
			for (int theIndex = 0; theIndex < PublicData.medicationDetails.size(); theIndex++)
			{
				listItems.add (new ListItem (Utilities.AbsoluteFileName (PublicData.medicationDetails.get(theIndex).photo),
						PublicData.medicationDetails.get(theIndex).name,
						PublicData.medicationDetails.get(theIndex).description,
						StaticData.BLANK_STRING,
						theIndex));
			}
		}
		// -------------------------------------------------------------------------
		return listItems;
	}
	// =============================================================================
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 11/12/2016 ECU changed to use the new method
		// -------------------------------------------------------------------------
		String theString = 	PrintMedication ();
		// -------------------------------------------------------------------------
		// 27/11/2014 ECU print out the dosages
		// 11/12/2016 ECU changed to use daysOfTheWeek
		// 02/03/2017 ECU changed to use StaticData SEPARATOR 
		// 29/11/2017 ECU added the INSET
		// -------------------------------------------------------------------------
		if (dailyDoseTimes != null)
		{
			for (int index = 0; index < dailyDoseTimes.length; index++)
			{
				if (dailyDoseTimes [index] != null)
				{
					theString += StaticData.SEPARATOR_LOWER +
							PublicData.daysOfTheWeek [index] + StaticData.NEWLINE + dailyDoseTimes [index].Print(StaticData.INSET);
				}
			}
		}	
		return theString;
	}
	// ==========================================================================
	public String PrintMedication ()
	{
		// ----------------------------------------------------------------------
		// 11/12/2016 ECU created to just print the medication details - nothing
		//                about the doses
		// 29/11/2017 ECU changed to use StaticData
		// -----------------------------------------------------------------------
		return	"Medication   : " + name 		+ StaticData.NEWLINE +
				"Description  : " + description + StaticData.NEWLINE +
				"Form         : " + form 		+ StaticData.NEWLINE +
				"Photo        : " + photo 		+ StaticData.NEWLINE ;
		// -----------------------------------------------------------------------
	}
	/* ========================================================================== */
	public static void EmailMedicationDetails (boolean theTypeOfEmail)
	{
    	// -------------------------------------------------------------------------
    	// 01/04/2014 ECU send the generated medication details by email
		// 11/12/2016 ECU added 'theTypeOfEmail' to indicate which type of email
		//                is to be sent
		//                    true .............. send the medication listed by
		//                                        medication
		//                    false ............. send the medication on a day by day
		//                                        basis
    	// -------------------------------------------------------------------------
    	if (PublicData.medicationDetails.size() > 0)
    	{
    		String totalMedicationDetails = StaticData.BLANK_STRING;
    		// ---------------------------------------------------------------------
    		// 11/12/2016 ECU decide which type of email is to be sent
    		// 02/03/2017 ECU changed to use StaticData rather than SystemInfoActivity
    		//                SEPARATOR
    		// ---------------------------------------------------------------------
    		if (theTypeOfEmail)
    		{
    			for (int theMedication = 0; theMedication < PublicData.medicationDetails.size(); theMedication++)
    			{
    				totalMedicationDetails += StaticData.SEPARATOR + 
    						PublicData.medicationDetails.get (theMedication).Print ();
    			}
    		}
    		else
    		{
    			// -----------------------------------------------------------------
    			// 11/12/2016 ECU changed to use daysOfTheWeek
    			// -----------------------------------------------------------------
    			for (int day = 0; day < PublicData.daysOfTheWeek.length; day++)
    			{
    				totalMedicationDetails += MedicationActivity.dosesPerDay (day) + StaticData.NEWLINE + StaticData.NEWLINE;
    			}
    		}
    		// -------------------------------------------------------------------------
        	// 01/04/2014 ECU confirm the action to the user
        	// -------------------------------------------------------------------------
        	Utilities.popToast ("Emailing the Medication Details to " + PublicData.emailDetails.recipients,true);
    		
        	Utilities.SendEmailMessage(MainActivity.activity, "Medication Details",totalMedicationDetails,true);
    	}
    	// -------------------------------------------------------------------------	
	}
	// =============================================================================
	public static String printSelected (List<MedicationDetails> theMedication,int theDay)
	{
		// -------------------------------------------------------------------------
		// 01/03/2017 ECU created to generated summary of the selected medication
		//                details
		//                    theDay ....... day for doses to be included. If set
		//                                   to NO_RESULT then all days are included
		// 26/08/2017 ECU changed to use BLANK_STRING
		// -------------------------------------------------------------------------
		String summaryString = StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
		// 01/03/2017 ECU set up the start and end days
		// -------------------------------------------------------------------------
		int startDay = 0;
		int endDay   = StaticData.DAYS_PER_WEEK;
		// -------------------------------------------------------------------------
		// 01/03/2017 ECU adjust if only a particular day is required
		// -------------------------------------------------------------------------
		if (theDay != StaticData.NO_RESULT)
		{
			// ---------------------------------------------------------------------
			// 01/03/2017 ECU a particular day is required
			// ---------------------------------------------------------------------
			startDay = theDay;
			endDay	 = theDay + 1;
		}
		// -------------------------------------------------------------------------
		// 01/03/2017 ECU loop through all medication
		// -------------------------------------------------------------------------
		for (int medication = 0; medication < theMedication.size(); medication++)
		{
			// ---------------------------------------------------------------------
			summaryString += theMedication.get (medication).PrintMedication () + StaticData.NEWLINE;
			// ---------------------------------------------------------------------
			// 01/03/2017 ECU now loop through the doses
			// ---------------------------------------------------------------------
			for (int day = startDay; day < endDay; day++)
			{
				// -----------------------------------------------------------------
				// 01/03/2017 ECU now loop through the doses for the indexed day
				// -----------------------------------------------------------------
				DoseDaily doseDaily = theMedication.get (medication).dailyDoseTimes [day];
				// -----------------------------------------------------------------
				for (int dose = 0; dose < doseDaily.doseTimes.size(); dose++)
				{
					// -------------------------------------------------------------
					// 01/03/2017 ECU print out dose details
					// 29/11/2017 ECU added INSET
					// -------------------------------------------------------------
					summaryString += doseDaily.doseTimes.get (dose).Print (StaticData.INSET) + StaticData.NEWLINE;
					// -------------------------------------------------------------
					// 02/03/2017 ECU add in a separator
					// -------------------------------------------------------------
					summaryString += StaticData.SEPARATOR_LOWER;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 02/03/2017 ECU add a separator
				// -----------------------------------------------------------------
				summaryString += StaticData.SEPARATOR;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 01/03/2017 ECU nor return the summary
		// -------------------------------------------------------------------------
		return summaryString;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static boolean validateIndices (int theMedication,int theDoseDay,int theDose)
	{
		// -------------------------------------------------------------------------
		// 26/03/2017 ECU created to check that there is an entry for the indices
		//                that are specified - initially used by MedicationActivity
		//                which responds to dose alarms
		// -------------------------------------------------------------------------
		// 26/03/2017 ECU first check the medication index
		// -------------------------------------------------------------------------
		if (PublicData.medicationDetails != null && 
			PublicData.medicationDetails.size() > 0 &&
			theMedication < PublicData.medicationDetails.size())
		{
			// ---------------------------------------------------------------------
			// 26/03/2017 ECU there are medication records so can continue
			// ---------------------------------------------------------------------
			MedicationDetails localRecord = PublicData.medicationDetails.get (theMedication);
			// ---------------------------------------------------------------------
			// 26/03/2017 ECU check if the dose exists
			// ---------------------------------------------------------------------
			if (localRecord.DoseExists (theDoseDay))
			{
				// -----------------------------------------------------------------
				// 26/03/2017 ECU get the list of dose for the specified day
				// -----------------------------------------------------------------
				DoseDaily doseDaily = localRecord.dailyDoseTimes [theDoseDay];
				// -----------------------------------------------------------------
				// 26/03/2017 ECU validate the particular dose
				// -----------------------------------------------------------------
				if (doseDaily != null && 
					doseDaily.doseTimes != null && 
					doseDaily.doseTimes.size() > 0 && 
					theDose < doseDaily.doseTimes.size())
				{
					// -------------------------------------------------------------
					// 26/03/2017 ECU everything seems valid so continue
					// -------------------------------------------------------------
					return true;
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 26/03/2017 ECU seems to be an invalid index
					// -------------------------------------------------------------
					return false;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 26/03/2017 ECU there are no doses on the specified day
				// -----------------------------------------------------------------
				return false;
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 26/03/2017 ECU there are no medication records or the requested one
			//                is outside the bounds of the list
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
}
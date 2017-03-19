package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class EPGEntry implements Serializable
{
	// =============================================================================
	// 18/09/2015 ECU created to hold the details of an EPG entry for a TV channel
	//
	//				  The fields on each line are separated by a tilde "~" character 
	//				  and end with a new line. 
	//
	//				  The fields that are supplied are:
	//
	//					0	Programme Title
	//					1	Sub-Title
	//					2	Episode
	//					3	Year
	//					4	Director
	//					5	Performers (Cast) - This will be either a string containing 
	//						the actors names or be a number of character name / actor name 
	//						pairs which are separated by an asterisk '*' and each pair by a pipe '|' character.
	//					6	Premiere
	//					7	Film
	//					8	Repeat
	//					9	Subtitles
	//					10	Widescreen
	//					11	New series
	//					12	Deaf signed
	//					13	Black and White
	//					14	Film star rating
	//					15	Film certificate
	//					16	Genre
	//					17	Description
	//					18	Radio Times Choice - selected by the Radio Times editorial team.
	//					19	Date
	//					20	Start Time
	//					21	End Time
	//					22	Duration (Minutes)
	// =============================================================================

	private static final long serialVersionUID = 1L;
	// =============================================================================
	public String 	fields [];
	public boolean	selected;					// 26/09/2015 ECU added to indicate
												//                that the field has been
												//                selected
	// =============================================================================
	
	// =============================================================================
	public EPGEntry (String theEntryToParse,boolean theSelectionFlag)
	{
		fields = theEntryToParse.split ("[~]");
		// -------------------------------------------------------------------------
		// 15/10/2015 ECU to try and limit the size then clear some fields
		// -------------------------------------------------------------------------
		if (fields.length == StaticData.EPG_FIELD_COUNT)
		{
			for (int theField : new int [] { StaticData.EPG_CAST,
										     StaticData.EPG_DIRECTOR,
										     StaticData.EPG_SUB_TITLE})
			{
				fields [theField] = "";	
			}
		}
		// -------------------------------------------------------------------------
		// 26/09/2015 ECU reset the 'selection' flag
		// -------------------------------------------------------------------------
		selected = theSelectionFlag;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public EPGEntry (String theEntryToParse)
	{
		// -------------------------------------------------------------------------
		// 26/09/2015 ECU call the master method
		// -------------------------------------------------------------------------
		this (theEntryToParse,false);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public EPGEntry (String [] theEPGFields)
	{
		// -------------------------------------------------------------------------
		// 24/06/2016 ECU created to set the fields directly
		// 21/07/2016 ECU changed to use the copyOf
		// -------------------------------------------------------------------------
		fields      = Arrays.copyOf (theEPGFields,theEPGFields.length);
		selected 	= false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String [] emptyEPGArray ()
	{
		// -------------------------------------------------------------------------
		// 24/06/2016 ECU created to return an empty EPG array
		// -------------------------------------------------------------------------
		String [] localEntries = new String [StaticData.EPG_FIELD_COUNT];
		
		for (int theIndex = 0; theIndex < StaticData.EPG_FIELD_COUNT; theIndex++)
			localEntries [theIndex] = "";
		
		return localEntries;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public int getDuration ()
	{
		// -------------------------------------------------------------------------
		// 14/02/2017 ECU created to return the duration of the current entry
		// -------------------------------------------------------------------------
		return Integer.parseInt (fields [StaticData.EPG_DURATION]);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public Long getEndTimeMillisecs ()
	{
		// -------------------------------------------------------------------------
		// 15/02/2017 ECU created to return the end time as an exact time
		// -------------------------------------------------------------------------
		return (Long) Utilities.getTime (fields [StaticData.EPG_DATE],fields [StaticData.EPG_END_TIME]);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static int getFirstProgram (ArrayList<EPGEntry> theEPGData,Long theTime)
	{
		// -------------------------------------------------------------------------
		// 15/02/2017 ECU created to find the first entry which has theTime between
		//                its start and end times
		// -------------------------------------------------------------------------
		int endCheck;
		int startCheck;
		// -------------------------------------------------------------------------
		if ((theEPGData != null) && (theEPGData.size() > 0))
		{
			// ---------------------------------------------------------------------
			// 15/02/2017 ECU now loop through the entries
			// ---------------------------------------------------------------------
			for (int theEntry = 0; theEntry < theEPGData.size(); theEntry++)
			{
				// -----------------------------------------------------------------
				// 15/02/2017 ECU do the checks on the specified time and the program
				//                timings
				// ------------------------------------------------------------------
				endCheck 	= theEPGData.get (theEntry).getEndTimeMillisecs ().compareTo (theTime);
				startCheck 	= theEPGData.get (theEntry).getStartTimeMillisecs ().compareTo (theTime);
				// -----------------------------------------------------------------
				// 15/02/2017 ECU check if the time is relevant to this program
				// -----------------------------------------------------------------
				if (((startCheck == 0) || (startCheck < 0)) && (endCheck > 0))	
				{
					// -------------------------------------------------------------
					// 15/02/2017 ECU found a program so return its index
					// -------------------------------------------------------------
					return theEntry;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 15/02/2017 ECU indicate that 'the time' does not correspond to a program
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String getStartTime ()
	{
		// -------------------------------------------------------------------------
		// 15/02/2017 ECU created to return the start time of this item
		// -------------------------------------------------------------------------
		return fields [StaticData.EPG_START_TIME];
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public Long getStartTimeMillisecs ()
	{
		// -------------------------------------------------------------------------
		// 15/02/2017 ECU created to return the start time as an exact time
		// -------------------------------------------------------------------------
		return (Long) Utilities.getTime (fields [StaticData.EPG_DATE],fields [StaticData.EPG_START_TIME]);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public boolean isValid ()
	{
		// -------------------------------------------------------------------------
		// 25/09/2015 ECU created to indicate if the entry is valid
		// -------------------------------------------------------------------------
		return (fields.length == StaticData.EPG_FIELD_COUNT);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print ()
	{
		if ((fields.length == StaticData.EPG_FIELD_COUNT))
		{
			return String.format("%-35.35s %-10s %-8s %-4s", fields [StaticData.EPG_PROGRAM_TITLE],
														  	 fields [StaticData.EPG_DATE],
														  	 fields [StaticData.EPG_START_TIME],
														  	 fields [StaticData.EPG_DURATION]);
		}
		else
		{
			return null;
		}
	}
	// =============================================================================
	public String ProgramDetails ()
	{
		// -------------------------------------------------------------------------
		// 07/07/2016 ECU created to return the program details
		// -------------------------------------------------------------------------
		return fields [StaticData.EPG_DATE] + "   " + 
			   fields [StaticData.EPG_START_TIME] + " to " + 
			   fields [StaticData.EPG_END_TIME];
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String ProgramName ()
	{
		// -------------------------------------------------------------------------
		// 07/07/2016 ECU created to return the name of the program
		// -------------------------------------------------------------------------
		return fields [StaticData.EPG_PROGRAM_TITLE];
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String PrintAll ()
	{
		// -------------------------------------------------------------------------
		// 06/07/2016 ECU created to print details of the program
		// 24/07/2016 ECU added the description and genre
		// 13/01/2017 ECU just change the spacing and add the MONO_SPACED introducer
		// -------------------------------------------------------------------------
		if ((fields.length == StaticData.EPG_FIELD_COUNT))
		{
			return  StaticData.MONO_SPACED + 
					"Program     : "    + fields [StaticData.EPG_PROGRAM_TITLE] + StaticData.NEWLINE +
				    "Date        : "    + fields [StaticData.EPG_DATE] 			+ StaticData.NEWLINE +
					"Start Time  : " 	+ fields [StaticData.EPG_START_TIME]	+ StaticData.NEWLINE +
					"End Time    : "   	+ fields [StaticData.EPG_END_TIME] 		+ StaticData.NEWLINE +
					"Description : "   	+ fields [StaticData.EPG_DESCRIPTION] 	+ StaticData.NEWLINE +
					"Genre       : "   	+ fields [StaticData.EPG_GENRE];
		}
		else
		{
			return null;
		}
	}
	// =============================================================================
	public String PrintProgram ()
	{
		// -------------------------------------------------------------------------
		// 15/10/2015 ECU created to return the program title
		// -------------------------------------------------------------------------
		return fields [StaticData.EPG_PROGRAM_TITLE];
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public boolean search (String theSearchString)
	{
		// -------------------------------------------------------------------------
		// 26/09/2015 ECU created to check if any fields in the entry match the
		//                specified search string
		// 28/09/2015 ECU limit the number of fields checked
		// -------------------------------------------------------------------------
		int[] searchFields = {StaticData.EPG_PROGRAM_TITLE,
							  StaticData.EPG_DESCRIPTION,
							  StaticData.EPG_GENRE};
		// -------------------------------------------------------------------------
		for (int field : searchFields)
		{
			if (fields[field].toUpperCase(Locale.getDefault()).contains(theSearchString.toUpperCase(Locale.getDefault())))
				return true;
		}
		// -------------------------------------------------------------------------
		// 26/09/2015 ECU indicate no match found
		// -------------------------------------------------------------------------
		return false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void setStartHourMinute ()
	{
		// -------------------------------------------------------------------------
		// 19/02/2017 ECU create to return the start hour of an entry
		// -------------------------------------------------------------------------
		String [] localFields = fields [StaticData.EPG_START_TIME].split(StaticData.ACTION_DELIMITER);
		// -------------------------------------------------------------------------
		// 19/02/2017 ECU return the hour part of the string
		// -------------------------------------------------------------------------
		ShowEPGActivity.scrolledTimeHour 	= Integer.parseInt(localFields [0]);
		ShowEPGActivity.scrolledTimeMinute 	= Integer.parseInt(localFields [1]);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

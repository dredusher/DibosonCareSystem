package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;

public class TVChannel implements Serializable,Comparable<TVChannel>
{
	// =============================================================================
	// 16/09/2015 ECU created to hold information about TV channels and
	//                timings associated with each channel
	// 16/10/2015 ECU rearranged so that EPG entries are kept in separate lists
	//                for each date
	// 23/06/2016 ECU added the ID which is now used with JSON
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	public  String				channelID;			// the ID of the channel
	public  int					channelNumber;		// the number of the channel
	public  String				channelName;		// free text name of the channel
	public  ArrayList<ArrayList<EPGEntry>> 
								EPGEntries;			// 16/10/2015 ECU added
	public  String				fileName;			// where data will be written
	// =============================================================================
	
	// =============================================================================
	public TVChannel (int theChannelNumber,String theChannelName,String theChannelID)
	{
		// -------------------------------------------------------------------------
		// 16/09/2015 ECU constructor to define the channel
		// 23/06/2016 ECU added the ID as an argument
		// -------------------------------------------------------------------------
		channelNumber		= theChannelNumber;
		channelName			= theChannelName;
		// -------------------------------------------------------------------------
		// 23/06/2016 ECU set the channel's ID
		// -------------------------------------------------------------------------
		channelID			= theChannelID;
		// -------------------------------------------------------------------------
		// 16/09/2015 ECU initialise the program times
		// -------------------------------------------------------------------------
		//programTimes		= new ArrayList<ProgramTime>();
		// -------------------------------------------------------------------------
		// 19/09/2015 ECU add the associated EPG for this channel
		// -------------------------------------------------------------------------
		EPGEntries			= new ArrayList<ArrayList<EPGEntry>>();
		// -------------------------------------------------------------------------
		// 16/10/2015 ECU set the file name where data is written
		// -------------------------------------------------------------------------
		fileName = PublicData.epgFolder + channelName.replaceAll(" ","_");
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public TVChannel (int theChannelNumber,String theChannelName)
	{
		// -------------------------------------------------------------------------
		// 23/06/2016 ECU added to call the main constructor with no ID
		// -------------------------------------------------------------------------
		this (theChannelNumber,theChannelName,null);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public int compareTo (TVChannel theTVChannel) 
	{
		// -------------------------------------------------------------------------
		// 24/12/2015 ECU created to enable sorting of the TV channels which is based
		//                on the name of the channel
		// -------------------------------------------------------------------------
		return this.channelName.compareToIgnoreCase(theTVChannel.channelName);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public ArrayList<EPGEntry> EPGentriesByDate (String theDate)
	{
		// -------------------------------------------------------------------------
		// 16/10/2015 ECU return the entries that correspond to the specified date
		// -------------------------------------------------------------------------
		int localDateIndex = getDateIndex (theDate);
		// -------------------------------------------------------------------------
		if (localDateIndex != StaticData.NO_RESULT)
		{
			// ---------------------------------------------------------------------
			// 16/10/2015 ECU a date has been found
			// ---------------------------------------------------------------------
			return EPGEntries.get(localDateIndex);
		}
		else
		{
			// ---------------------------------------------------------------------
			// 16/10/2015 ECU nothing found
			// ---------------------------------------------------------------------
			return new ArrayList<EPGEntry> ();
		}
	}
	// =============================================================================
	public static ArrayList<EPGEntry> EPGentriesFilter (ArrayList<EPGEntry> theEPGentries,String theFilterString)
	{
		ArrayList<EPGEntry> localEntries = new ArrayList<EPGEntry> ();
		// -------------------------------------------------------------------------
		// 26/09/2015 ECU loop for all entries
		// -------------------------------------------------------------------------
		if (theEPGentries.size() > 0)
		{
			for (int entry = 0; entry < theEPGentries.size(); entry++)
			{
				if (theEPGentries.get(entry).search (theFilterString))
				{
					// -------------------------------------------------------------
					// 26/09/2015 ECU can put the search method here
					// -------------------------------------------------------------
					localEntries.add (theEPGentries.get(entry));
					// -------------------------------------------------------------
				}
			}
		}
		// -------------------------------------------------------------------------
		return localEntries;
	}
	// =============================================================================
	int getDateIndex (String theDate)
	{
		// -------------------------------------------------------------------------
		// 16/10/2015 ECU created to return the index of the array list corresponding
		//                to the specified date - or NO_RESULT if date not found
		// -------------------------------------------------------------------------
		for (int localDate = 0; localDate < EPGEntries.size(); localDate ++)
		{
			// -----------------------------------------------------------------
			// 16/10/2015 ECU loop through the stored entries to find the specified
			//                date
			// 03/03/2016 ECU because the EPGEntries is a 2D list and am
			//                specifically getting record '0' then put in a
			//                check on size. For clarity 
			// -----------------------------------------------------------------
			if ((EPGEntries.get (localDate).size() > 0) &&
				(EPGEntries.get (localDate).get(0).fields[StaticData.EPG_DATE].equalsIgnoreCase(theDate)))
						return localDate;		
			// -----------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 16/10/2015 ECU no date found so indicate the fact
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void parseURLData (ArrayList<String> theURLData)
	{
		// -------------------------------------------------------------------------
		// 16/10/2015 ECU created to parse the URL data into the required internal lists
		// -------------------------------------------------------------------------
		int					localDateIndex  = -1;
		String				localDate		= "";
		EPGEntry 			localEPGentry;
		// ------------------------------------------------------------------------
		// 16/10/2015 ECU initialise the EPG array
		// ------------------------------------------------------------------------
		EPGEntries = new ArrayList<ArrayList<EPGEntry>> ();
		// -------------------------------------------------------------------------
		// 19/09/2015 ECU copy the data into the EPG for this channel
		// -------------------------------------------------------------------------
		if (theURLData != null && theURLData.size() > 0)
		{
			// ---------------------------------------------------------------------
			for (int line = 0; line < theURLData.size(); line++)
			{
				// -----------------------------------------------------------------
				localEPGentry = new EPGEntry (theURLData.get(line));
				// -----------------------------------------------------------------
				if (localEPGentry.isValid())
				{
					if (!localEPGentry.fields[StaticData.EPG_DATE].equalsIgnoreCase(localDate))
					{
						// ---------------------------------------------------------
						// 16/10/2015 ECU the date has changed so generate new ArrayList
						// ---------------------------------------------------------
						EPGEntries.add (new ArrayList<EPGEntry>());
						// ---------------------------------------------------------
						// 16/10/2015 ECU store and increment the working variables
						// ---------------------------------------------------------
						localDate = localEPGentry.fields[StaticData.EPG_DATE];
						localDateIndex++;
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 25/09/2015 ECU only add the entry if it is valid
					// -------------------------------------------------------------
				    EPGEntries.get (localDateIndex).add (localEPGentry);
				    // -------------------------------------------------------------
				}
			}
		}
	}
	// =============================================================================
	public String Print (String theDate)
	{
		String localString = "Channel Number = " + channelNumber + "   Channel Name = " + channelName + "\n\n";
		String tempString;
		// -------------------------------------------------------------------------
		// 16/10/2015 ECU get the array index corresponding to the date
		// -------------------------------------------------------------------------
		int localDateIndex = getDateIndex (theDate);
		// -------------------------------------------------------------------------
		// 16/10/2015 ECU the date has been found so print out the entries
		// -------------------------------------------------------------------------
		if (localDateIndex != StaticData.NO_RESULT)
		{
			if (EPGEntries.get(localDateIndex).size() > 0)
			{
				for (int EPGentry = 0; EPGentry < EPGEntries.get(localDateIndex).size(); EPGentry++)
				{
					tempString = EPGEntries.get(localDateIndex).get (EPGentry).Print ();
				
					if (tempString != null) localString += tempString + "\n";
				}
			}
		}
		// -------------------------------------------------------------------------
		return localString;
	}
	// =============================================================================
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 23/06/2016 ECU print a summary of the object
		// -------------------------------------------------------------------------
		return "Name : " + channelName + "  ID : " + channelID + "  Number : " + channelNumber;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print (boolean theDummyFlag)
	{
		String localResponse = Print () + "\n";
		
		localResponse += "Number of Dates " + EPGEntries.size() + "\n";
		
		for (int theDate = 0; theDate < EPGEntries.size(); theDate++)
		{
			int localSize = EPGEntries.get(theDate).size();
			
			localResponse += "     Number of Entries " + localSize + "\n";
			
			for (int theEntry = 0; theEntry < localSize; theEntry++)
			{
				localResponse += (EPGEntries.get(theDate)).get(theEntry).Print() + "\n";
			}	
		}
		return localResponse;
	}
	// =============================================================================
	public ArrayList<String> Print (String theDate,boolean theDummyFlag)
	{
		ArrayList<String> theStrings = new ArrayList<String>();
		String tempString;
		// -------------------------------------------------------------------------
		// 16/10/2015 ECU get the array index corresponding to the date
		// -------------------------------------------------------------------------
		int localDateIndex = getDateIndex (theDate);
		// -------------------------------------------------------------------------
		// 16/10/2015 ECU the date has been found so print out the entries
		// -------------------------------------------------------------------------
		if (localDateIndex != StaticData.NO_RESULT)
		{
			if (EPGEntries.get(localDateIndex).size() > 0)
			{
				for (int EPGentry = 0; EPGentry < EPGEntries.get(localDateIndex).size(); EPGentry++)
				{
					tempString = EPGEntries.get(localDateIndex).get (EPGentry).Print ();
					
					if (tempString != null) theStrings.add(tempString);
				}
			}
		}
		// -------------------------------------------------------------------------
		return theStrings;
	}
	// =============================================================================
	void readFromDisk (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 16/10/2015 ECU read the EPG data from disk
		// -------------------------------------------------------------------------
		TVChannel localTVChannel = (TVChannel) AsyncUtilities.readObjectFromDisk (theContext,fileName);
		// -------------------------------------------------------------------------
		// 16/10/2015 ECU now copy across the data
		// -------------------------------------------------------------------------
		if (localTVChannel != null)
		{	
			EPGEntries = localTVChannel.EPGEntries;
		}
		else
		{
			EPGEntries	= new ArrayList<ArrayList<EPGEntry>>();
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void writeToDisk ()
	{
		// -------------------------------------------------------------------------
		// 16/10/2015 ECU created to write this object to disk
		// -------------------------------------------------------------------------
		AsyncUtilities.writeObjectToDisk (fileName,this);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

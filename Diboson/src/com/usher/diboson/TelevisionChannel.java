package com.usher.diboson;

import java.util.ArrayList;

public class TelevisionChannel
{
	/* ============================================================================= */
	// 01/03/2014 ECU created to store the television channel details
	/* ============================================================================= */
	public String	channelName;
	public int		channel;
	/* ============================================================================= */
	public TelevisionChannel (String theName,int theChannel)
	{
		channelName		= theName;
		channel			= theChannel;
	}
	/* ============================================================================= */
	public static ArrayList<Integer> ReturnChannel (int theChannel)
	{
		// -------------------------------------------------------------------------
		// 01/03/2014 ECU this method returns the individual digits of the supplied
		//                integer
		// -------------------------------------------------------------------------
		
		int localChannel 				= theChannel;
		ArrayList<Integer> localList 	= new ArrayList<Integer>();
		
		// ------------------------------------------------------------------------
		// 01/03/2014 ECU loop for all digits in the number
		// ------------------------------------------------------------------------
		
		while (localChannel > 0)
		{			
			localList.add (0, (localChannel % 10));		// store the remainder
			localChannel /= 10;							// adjust the number
		}
		
		// -------------------------------------------------------------------------
		// 01/03/2014 ECU return the list of digits
		// -------------------------------------------------------------------------		
		return localList;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 18/11/2017 ECU print out the current record
		// -------------------------------------------------------------------------
		return "Channel Name : " + channelName + " - " + channel;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

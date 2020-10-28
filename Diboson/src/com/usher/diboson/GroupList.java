package com.usher.diboson;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// =================================================================================
public class GroupList implements Serializable
{
	// -----------------------------------------------------------------------------
	// 06/10/2016 ECU created to hold a list of activities
	// -----------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;
	// -----------------------------------------------------------------------------
	final static String TAG = "GroupList";
	// -----------------------------------------------------------------------------
	// 06/10/2016 ECU declare the list of activities
	// 07/10/2016 ECU changed to use GridImages 
	// 19/10/2016 ECU had to change the logic because 'GridImages' contains
	//                drawables which can change across compilations so changed to
	//                store a pointer into the grid images array with the GridActivity
	//                class
	// -----------------------------------------------------------------------------
	List<Integer> 		activities;					// 19/10/2016 ECU changed from <GridImages>
	String  			groupListName;
	// -----------------------------------------------------------------------------

	// =============================================================================
	public GroupList (String theGroupName)
	{
		// -------------------------------------------------------------------------
		// 28/07/2020 ECU created to set a default group name
		// -------------------------------------------------------------------------
		groupListName	= theGroupName;
		// -------------------------------------------------------------------------
		// 28/07/2020 ECU preset the 'activities' to an empty list
		// -------------------------------------------------------------------------
		activities		= new ArrayList<Integer> ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void AddActivity (int theActivityIndex)
	{
		// -------------------------------------------------------------------------
		// 07/10/2016 ECU created to add a new entry into a group list
		// 08/10/2016 ECU added the group
		// 18/04/2017 ECU added the context as an argument
		// 25/07/2020 ECU changed the logic to store index directly
		// 28/07/2020 ECU changed from 'static'
		// -------------------------------------------------------------------------
		// 07/10/2016 ECU now add the entry into the group list
		// -------------------------------------------------------------------------
		activities.add (theActivityIndex);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void ClearActivities ()
	{
		// -------------------------------------------------------------------------
		// 28/07/2020 ECU clear the list of stored activities
		// -------------------------------------------------------------------------
		activities =  new ArrayList<Integer> ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String [] getActivityTitles (Context theContext,int theGroup)
	{
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU return the legends of the activities in this group
		// 18/04/2017 ECU added the context as an argument
		// -------------------------------------------------------------------------
		String [] localStrings = new String [PublicData.storedData.groupLists.get(theGroup).activities.size ()];
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU now build up the array
		// 19/10/2016 ECU changed because of the definition of 'activities'
		// 01/04/2017 ECU changed to use new Legend method
		// -------------------------------------------------------------------------
		for (int index=0; index < localStrings.length; index++)
		{
			localStrings [index] = getGridImage (PublicData.storedData.groupLists.get(theGroup).activities.get (index)).Legend (theContext);
		}
		// -------------------------------------------------------------------------
		return localStrings;
	}
	// =============================================================================
	@SuppressWarnings("rawtypes")
	public static GridImages getGridImage (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 19/10/2016 ECU created to return the array entry corresponding to the index
		// -------------------------------------------------------------------------
		return GridActivity.originalGridImages [theIndex];
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String [] getTitles ()
	{
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU returns the names of the defined groups
		// -------------------------------------------------------------------------
		if (PublicData.storedData.groupLists != null &&
			PublicData.storedData.groupLists.size () > 0)
		{
			String [] localNames = new String [PublicData.storedData.groupLists.size ()];
			// ---------------------------------------------------------------------
			// 08/10/2016 ECU copy the information across
			// ---------------------------------------------------------------------
			for (int index = 0; index < localNames.length; index++)
			{
				localNames [index] = PublicData.storedData.groupLists.get(index).groupListName;
			}
			// ---------------------------------------------------------------------
			// 08/10/2016 ECU now copy across the generated array
			// ---------------------------------------------------------------------
			return localNames;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 08/10/2016 ECU nothing has been defined so indicate that fact
			// ---------------------------------------------------------------------
			return null;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	@SuppressWarnings("rawtypes")
	public static GridImages [] returnArray (int theGroup)
	{
		// -------------------------------------------------------------------------
		// 08/10/2016 ECU created to return the images associated with the specified
		//                group
		// -------------------------------------------------------------------------
		// 04/11/2016 ECU had an error where the group was not being initialise so
		//                if that happens then default to the first group
		// -------------------------------------------------------------------------
		if (theGroup == StaticData.NO_RESULT)
			theGroup = 0;
		// -------------------------------------------------------------------------
		// 08/10/2016 ECU returns the specified group list as an array
		// -------------------------------------------------------------------------
		if (PublicData.storedData.groupLists != null &&
			PublicData.storedData.groupLists.size () > 0 && 
			PublicData.storedData.groupLists.get (theGroup).activities.size () > 0)
		{
			// ---------------------------------------------------------------------
			// 05/11/2016 ECU on the Samsung after a fresh install got an exception
			//                trying to cast from int to GridImage - believe that
			//                this is due to the changes made on 19/10/2016 (see
			//                head of class). Should not be an ongoing issue but
			//                try and catch the exception 'just in case'.
			// ---------------------------------------------------------------------
			try
			{
				GridImages [] localArray = new GridImages [PublicData.storedData.groupLists.get(theGroup).activities.size ()];
				// -----------------------------------------------------------------
				// 08/10/2016 ECU copy the information across
				// -----------------------------------------------------------------
				for (int index = 0; index < localArray.length; index++)
				{
					localArray [index] = getGridImage (PublicData.storedData.groupLists.get(theGroup).activities.get (index));
				}
				// -----------------------------------------------------------------
				// 08/10/2016 ECU now copy across the generated array
				// -----------------------------------------------------------------
				return localArray;
				// -----------------------------------------------------------------
			}
			catch (Exception theException)
			{
				// -----------------------------------------------------------------
				// 05/11/2016 ECU log the error and then report problem back to caller
				// -----------------------------------------------------------------
				Utilities.LogToProjectFile (TAG,"Exception : " + theException);
				// -----------------------------------------------------------------
				return null;
				// -----------------------------------------------------------------
				
			}
		}
		else
		{
			return null;
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}
// =================================================================================

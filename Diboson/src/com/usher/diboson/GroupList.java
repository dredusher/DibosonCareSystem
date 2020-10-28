package com.usher.diboson;

import java.io.Serializable;
import java.util.List;

import android.content.Context;
import android.widget.Toast;

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
	public static void Add (Context theContext,int theGroup,int theActivityImage)
	{
		// -------------------------------------------------------------------------
		// 07/10/2016 ECU created to add a new entry into a group list
		// 08/10/2016 ECU added the group
		// 18/04/2017 ECU added the context as an argument
		// -------------------------------------------------------------------------
		// 19/10 2016 ECU get the index corresponding to the image
		// -------------------------------------------------------------------------
		int localIndex = getIndex (theActivityImage);
		// -------------------------------------------------------------------------
		// 19/10/2016 ECU if not found then just exit
		// -------------------------------------------------------------------------
		if (localIndex == StaticData.NO_RESULT)
			return;
		// -------------------------------------------------------------------------
		// 07/10/2016 ECU check if the current id is already in the list
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.storedData.groupLists.get(theGroup).activities.size(); theIndex++)
		{
			if (PublicData.storedData.groupLists.get(theGroup).activities.get (theIndex) == localIndex)
			{
				// -----------------------------------------------------------------
				// 07/10/2016 ECU indicate that image already added
				// 01/04/2017 ECU changed to use new Legend method
				// -----------------------------------------------------------------
				Utilities.popToast ("'" + getGridImage (localIndex).Legend (theContext) + 
												"'\nhas already been added to the group",true,Toast.LENGTH_SHORT);
				// -----------------------------------------------------------------
				return;
			}
		}
		// -------------------------------------------------------------------------
		// 07/10/2016 ECU now add the entry into the group list
		// -------------------------------------------------------------------------
		PublicData.storedData.groupLists.get(theGroup).activities.add (localIndex);
		// -----------------------------------------------------------------
		// 07/10/2016 ECU indicate that the image has been added
		// 01/04/2017 ECU changed to use new Legend method
		// 18/04/2017 ECU added the context as an argument
		// -----------------------------------------------------------------
		Utilities.popToast ("'" + getGridImage (localIndex).Legend (theContext) + 
										"'\nhas been added to the group",true,Toast.LENGTH_SHORT);
		// ------------------------------------------------------------------
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
	static int getIndex (int theResourceId)
	{
		// -------------------------------------------------------------------------
		// 19/10/2016 ECU created to find the entry in 'original...' which has the
		//                specified resource ID. Returns the array index or NO_RESULT
		//                if no entry found
		// -------------------------------------------------------------------------
		for (int index = 0; index < GridActivity.originalGridImages.length; index++)
		{
			// ---------------------------------------------------------------------
			// 19/10/2016 ECU check for the specified resource
			// ---------------------------------------------------------------------
			if (theResourceId == GridActivity.originalGridImages [index].imageId)
			{
				// -----------------------------------------------------------------
				// 19/10/2016 ECU have found the index in the array so return it
				// -----------------------------------------------------------------
				return index;
			}
		}
		// -------------------------------------------------------------------------
		// 19/10/2016 ECU get here if no match found so indicate that fact
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
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
			// -------------------------------------------------------------------------
			// 08/10/2016 ECU copy the information across
			// -------------------------------------------------------------------------
			for (int index = 0; index < localNames.length; index++)
			{
				localNames [index] = PublicData.storedData.groupLists.get(index).groupListName;
			}
			// -------------------------------------------------------------------------
			// 08/10/2016 ECU now copy across the generated array
			// -------------------------------------------------------------------------
			return localNames;
		}
		else
		{
			return null;
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
				Utilities.LogToProjectFile(TAG,"Exception : " + theException);
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

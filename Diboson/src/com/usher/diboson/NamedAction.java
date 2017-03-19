package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;

public class NamedAction implements Serializable
{
	// =============================================================================
	// 03/08/2016 ECU created to enable the storing of 'named' actions - the real
	//                reason for creating this is so that actions can be nested in
	//                a simple way
	// -----------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	String	actions;
	String	name;
	// =============================================================================
	
	// =============================================================================
	public NamedAction (String theName,String theActions)
	{
		// -------------------------------------------------------------------------
		// 03/08/2016 ECU public constructor for setting the initial data
		// -------------------------------------------------------------------------
		actions		= theActions;
		name		= theName;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public static void Add (NamedAction theNamedAction)
	{
		// -------------------------------------------------------------------------
		// 03/08/2016 ECU created to add a new named action object if the same name
		//                does not already exist or replace that entry
		// -------------------------------------------------------------------------
		if (PublicData.namedActions.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 03/08/2016 ECU check if the name already exists
			// ---------------------------------------------------------------------
			for (int index = 0; index < PublicData.namedActions.size(); index++)
			{
				if (PublicData.namedActions.get(index).name.equalsIgnoreCase(theNamedAction.name))
				{
					// -------------------------------------------------------------
					// 03/08/2016 ECU the specified name already exists so just replace
					//                the current entry
					// -------------------------------------------------------------
					PublicData.namedActions.set (index, theNamedAction);
					// -------------------------------------------------------------------------
					Utilities.popToastAndSpeak (MainActivity.activity.getString (R.string.entry_updated));
					// -------------------------------------------------------------
					// 03/08/2016 ECU and just exit
					// -------------------------------------------------------------
					return;
					// -------------------------------------------------------------
				}
			}
		}
		// -------------------------------------------------------------------------
		// 03/08/2016 ECU a new entry needs to be added
		// -------------------------------------------------------------------------
		PublicData.namedActions.add (theNamedAction);
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak (MainActivity.activity.getString (R.string.new_entry_created));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String getActions (String theName)
	{
		// -------------------------------------------------------------------------
		// 03/08/2016 ECU created to return the actions associated with the 'name'
		//                or null if the name does not exist
		// -------------------------------------------------------------------------
		if (PublicData.namedActions.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 03/08/2016 ECU check if the name already exists
			// ---------------------------------------------------------------------
			for (int index = 0; index < PublicData.namedActions.size(); index++)
			{
				if (PublicData.namedActions.get(index).name.equalsIgnoreCase(theName))
				{
					// -------------------------------------------------------------
					// 03/08/2016 ECU the specified name already exists so just replace
					//                the current entry
					// -------------------------------------------------------------
					return PublicData.namedActions.get (index).actions;
					// -------------------------------------------------------------
				}
			}
		}
		return null;
	}
	// =============================================================================
	public static String [] getNames ()
	{
		// -------------------------------------------------------------------------
		// 04/08/2016 ECU created to return the list of names or 'null' if none exist
		// -------------------------------------------------------------------------
		if (PublicData.namedActions.size () > 0)
		{
			// ---------------------------------------------------------------------
			// 04/08/2016 ECU define the array to receive the names and then populate
			//                it
			// ----------------------------------------------------------------------
			String [] localNames = new String [PublicData.namedActions.size()];
			for (int index = 0; index < PublicData.namedActions.size(); index++)
			{
				localNames [index] = PublicData.namedActions.get(index).name;
			}
			// ---------------------------------------------------------------------
			// 04/08/2016 ECU return the list of names
			// ---------------------------------------------------------------------
			return localNames;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 04/08/2016 ECU there are no stored named actions
			// ---------------------------------------------------------------------
			return null;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean hasEntries ()
	{
		// -------------------------------------------------------------------------
		// 22/10/2016 ECU created to indicate whether there are any entries (true)
		//                or none (false) in the named actions list
		// -------------------------------------------------------------------------
		// 22/10/2016 ECU first of all check if the list is defined at all - should
		//                not normally happen except during development
		// -------------------------------------------------------------------------
		if (PublicData.namedActions == null)
		{
			// ---------------------------------------------------------------------
			// 22/10/2016 ECU the list has never been defined so create a new list
			// ---------------------------------------------------------------------
			PublicData.namedActions	= new ArrayList<NamedAction> ();
			// ---------------------------------------------------------------------
			// 22/10/2016 ECU indicate that the list is empty
			// ---------------------------------------------------------------------
			return false;
		}
		// -------------------------------------------------------------------------
		// 22/10/2016 ECU check if there are any entries in the list
		// -------------------------------------------------------------------------
		if (PublicData.namedActions.size() == 0)
			return false;							// indicate no entries
		else
			return true;							// indicate entries
		// -------------------------------------------------------------------------		
	}
	// =============================================================================
}

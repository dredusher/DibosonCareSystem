package com.usher.diboson;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class DatabaseActivity extends DibosonActivity 
{
	// =============================================================================
	// 15/11/2015 ECU created to handle database utilities, mainly contacts
	// 15/11/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// =============================================================================
	
	// =============================================================================
	public 	static Context	context;
	// =============================================================================
	
	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 15/11/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			setContentView (R.layout.activity_database);
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			ContactName (null);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 15/11/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	
	// =============================================================================
	public static void Cancel (String theText)
	{
    	// -------------------------------------------------------------------------
    	// 26/09/2015 ECU just cancel - finish this activity
       	// -------------------------------------------------------------------------
		((Activity) context).finish ();
		// ------------------------------------------------------------------------- 	
	}
	// =============================================================================
	public static void ContactName (String theContactName)
	{
    	// -------------------------------------------------------------------------
    	// 26/09/2015 ECU get details for the contact name
       	// -------------------------------------------------------------------------
		if (theContactName != null)
			Utilities.popToast (theContactName + "\n" + DatabaseUtilities.summary (context,DatabaseUtilities.findName(context, theContactName)));
    	// -------------------------------------------------------------------------
		DialogueUtilities.textInput (context,
									 "Contact Details",
									 "Please enter the name you are looking for",
									 Utilities.createAMethod (DatabaseActivity.class,"ContactName",""),
									 Utilities.createAMethod (DatabaseActivity.class,"Cancel",""));
		// ------------------------------------------------------------------------
	}
	// =============================================================================
}

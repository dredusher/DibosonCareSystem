package com.usher.diboson;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

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
	public  static EditText searchStringField;
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
			// 21/12/2017 ECU get the field that the search string is entered into
			// ---------------------------------------------------------------------
			searchStringField = (EditText) findViewById (R.id.search_string);
			// ---------------------------------------------------------------------
			// 21/12/2017 ECU set up the click listener for the button
			// ---------------------------------------------------------------------
			((Button) findViewById (R.id.search_button)).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick (View view) 
				{	
					// -------------------------------------------------------------
					// 21/12/2017 ECU get the string that has been entered
					//--------------------------------------------------------------
					String searchString = searchStringField.getText ().toString();
					// -------------------------------------------------------------
					// 21/12/2017 ECU decide if the search string needs to be 
					//                modified
					// -------------------------------------------------------------
					int selectedId = ((RadioGroup) findViewById(R.id.search_options)).getCheckedRadioButtonId();
					// -------------------------------------------------------------
					// 21/12/2017 ECU decide the action to be taken
					// -------------------------------------------------------------
					switch (selectedId)
					{
						// ---------------------------------------------------------
						case R.id.search_beginning:
							searchString = searchString + "%";
							break;
						// ---------------------------------------------------------
						case R.id.search_end:
							searchString = "%" + searchString;
							break;
						// ---------------------------------------------------------
						case R.id.search_exact:
							break;
						// ---------------------------------------------------------
						case R.id.search_middle:
							searchString = "%" + searchString + "%";
							break;
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 21/12/2017 ECU now perform the search
					// 22/12/2017 ECU pass through the context as an argument
					//            ECU indicate that no action is required if
					//                only one entry found
					// -------------------------------------------------------------
					ContactName (context,searchString,null);
					// -------------------------------------------------------------
				}
			});
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
	public static void ContactName (Context theContext,String theContactName,Method theUniqueMethod)
	{
    	// -------------------------------------------------------------------------
    	// 26/09/2015 ECU get details for the contact name
		// 22/12/2017 ECU added the context as an argument
		//            ECU add the method definition if a unique match found
       	// -------------------------------------------------------------------------
		DatabaseUtilities.BuildTheContactsList (theContext,theContactName);
		// -------------------------------------------------------------------------
		// 21/12/2017 ECU check if anything found
		// -------------------------------------------------------------------------
		if (SelectorUtilities.selectorParameter.listItems.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 22/12/2017 ECU check if an action is to be taken if there is a unique
			//                match
			// ---------------------------------------------------------------------
			if ((theUniqueMethod != null) && (SelectorUtilities.selectorParameter.listItems.size() == 1))
			{
				// -----------------------------------------------------------------
				// 22/12/2017 ECU call up the method that will handle the unique
				//                match
				// -----------------------------------------------------------------
				try
				{
					// -------------------------------------------------------------
					// 22/12/2017 ECU envoke the method passed through as an argument
					// -------------------------------------------------------------
					theUniqueMethod.invoke (null, new Object [] {SelectorUtilities.selectorParameter.listItems.get (0)});
					// -------------------------------------------------------------
				}
				catch (Exception theException)
				{
					
				}
				// -----------------------------------------------------------------
			}
			else
			{
				SelectorUtilities.selectorParameter.rowLayout 				= R.layout.contacts_row;
				SelectorUtilities.selectorParameter.classToRun 				= DatabaseActivity.class;
				SelectorUtilities.selectorParameter.type 					= StaticData.OBJECT_CONTACTS;
				SelectorUtilities.selectorParameter.finishOnSelect 			= true;
				SelectorUtilities.selectorParameter.editMethodDefinition 	
						= new MethodDefinition<DatabaseActivity> (DatabaseActivity.class,"MakePhoneCall");
				SelectorUtilities.selectorParameter.customLegend			= theContext.getString (R.string.text);
				SelectorUtilities.selectorParameter.customMethodDefinition 	
						= new MethodDefinition<DatabaseActivity> (DatabaseActivity.class,"SendTextMessage");
				SelectorUtilities.selectorParameter.selectMethodDefinition 	
						= new MethodDefinition<DatabaseActivity> (DatabaseActivity.class,"SelectMethod");
				// ---------------------------------------------------------------------
				SelectorUtilities.StartSelector (theContext,StaticData.OBJECT_CONTACTS);
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 21/12/2017 ECU indicate that nothing matches
			// ---------------------------------------------------------------------
			Utilities.popToast (String.format (theContext.getString(R.string.search_string_none_format),theContactName),true);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	
	
	// =============================================================================
    public static void MakePhoneCall (int theContactSelected)
    {
    	// -------------------------------------------------------------------------
    	// 20/12/2017 ECU create to handle the phone call button
    	// -------------------------------------------------------------------------
    	String [] phoneNumber = SelectorUtilities.selectorParameter.listItems.get(theContactSelected).summary.split(StaticData.NEWLINE);
    	Utilities.popToast ("Contact Selected " + phoneNumber [0]);
    	Utilities.makePhoneCall(context, phoneNumber[0]);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void SelectMethod (int theContactSelected)
    {
    	// -------------------------------------------------------------------------
    	// 21/12/2017 ECU create to handle the selection of a contact
    	// -------------------------------------------------------------------------
 
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void SendTextMessage (int theContactSelected)
    {
    	// -------------------------------------------------------------------------
    	// 20/12/2017 ECU create to handle the text button
    	// -------------------------------------------------------------------------
    	String [] phoneNumber = SelectorUtilities.selectorParameter.listItems.get(theContactSelected).summary.split(StaticData.NEWLINE);
    	Utilities.popToast ("Contact Selected " + phoneNumber [0]);
    	Utilities.sendSMSMessage (context, phoneNumber[0],"contacts test");
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
}

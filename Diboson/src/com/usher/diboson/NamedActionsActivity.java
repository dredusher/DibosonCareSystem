package com.usher.diboson;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NamedActionsActivity extends DibosonActivity 
{
	// =============================================================================
	// 03/08/2016 ECU created to handle 'named' actions  which was added to provide
	//                a very simple form of nesting and to shorten the data that
	//                may need to be stored on items like an NFC tag
	// 12/09/2016 ECU added the data changed bit to reduce the writes to disk
	//            ECU changed to use hash code instead of data changed
	// 20/04/2018 ECU changed to use ListViewSelector and reduce the use of 'statics'
	// 12/04/2019 ECU changed the actioning of the 'named action' from a long press
	//                to pressing the 'process' button
	// =============================================================================
	
	// =============================================================================
	Activity		 activity;
	Context			 context;
	int				 initialHashCode;						// 12/09/2016 ECU added
	ListViewSelector listViewSelector;
	EditText		 nameEditText;
	// -----------------------------------------------------------------------------
	EditText		 actionsEditText;
	// =============================================================================
	
	// =============================================================================
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU created to handle the registration and processing of
		//                liquids
		// -------------------------------------------------------------------------
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null) 
		{
			// ---------------------------------------------------------------------
			// 30/05/2016 ECU check if this device has an ambient light sensor
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			// ---------------------------------------------------------------------
			// 03/08/2016 ECU remember the context
			// ---------------------------------------------------------------------
			activity = this;
			context  = this;
			// ---------------------------------------------------------------------
			// 22/10/2016 ECU use the method rather than '== 0' check
			// ---------------------------------------------------------------------
			if (!NamedAction.hasEntries())
			{
				// -----------------------------------------------------------------
				// 03/08/2016 ECU there are currently no named actions
				// -----------------------------------------------------------------
				displayLayout ();
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 20/04/2018 ECU initialise the display of named actions
				// -----------------------------------------------------------------
				initialiseDisplay (activity);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 12/09/2016 ECU initialise the hashcode
			// 22/10/2016 ECU moved here
			// ---------------------------------------------------------------------
			initialHashCode = PublicData.namedActions.hashCode();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 18/05/2016 ECU the activity has been recreated after being destroyed
			//                by Android
			// ---------------------------------------------------------------------
			finish ();
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	@Override
	public void onBackPressed () 
	{
	    // -------------------------------------------------------------------------
		// 03/08/2016 ECU terminate this activity
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
		// 03/08/2016 ECU now call the super for this method
		// -------------------------------------------------------------------------
		super.onBackPressed();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
    public void onDestroy()
    {
		// -------------------------------------------------------------------------
		// 03/08/2016 ECU added
		// -------------------------------------------------------------------------
		// 03/08/2016 ECU update the data on disk
		// 12/09/2016 ECU only update the disk if data has changed
		// 22/10/2016 ECU changed to use the method - 'false' to indicate that only
		//                write if the hash code has changed
		// -------------------------------------------------------------------------
		updateTheDisk (false);
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
    }
	// =============================================================================
	public void AddAction (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 03/08/2016 ECU add a new named action
		// -------------------------------------------------------------------------
		// 03/08/2016 ECU display the layout for the input data
		// -------------------------------------------------------------------------
		displayLayout ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	private View.OnClickListener buttonListener = new View.OnClickListener() 
	{
		// -------------------------------------------------------------------------
		@Override
		public void onClick(View view) 
		{	
			//----------------------------------------------------------------------
			// 17/03/2015 ECU now process depending on which button pressed
			//----------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.named_actions_define_button:
					// -------------------------------------------------------------
					// 03/08/2016 ECU get the data ready to define the 'named action'
					// 20/09/2016 ECU added the 'trim' to remove leading/trailing
					//                spaces
					// -------------------------------------------------------------
					String inputActions = actionsEditText.getText().toString().trim();
					String inputName 	= nameEditText.getText().toString().trim();
					// -------------------------------------------------------------
					// 03/08/2016 ECU check if the data has been input
					// -------------------------------------------------------------
					if (inputActions.equalsIgnoreCase (StaticData.BLANK_STRING))
					{
						// ---------------------------------------------------------
						// 03/08/2016 ECU no name has been entered
						// 20/09/2016 ECU changed to use the resource
						// ---------------------------------------------------------
						Utilities.popToastAndSpeak (activity.getString (R.string.named_action_no_actions));
						// ---------------------------------------------------------
						break;
					}
					if (inputName.equalsIgnoreCase (StaticData.BLANK_STRING))
					{
						// ---------------------------------------------------------
						// 03/08/2016 ECU no name has been entered
						// 20/09/2016 ECU changed to use the resource
						// ---------------------------------------------------------
						Utilities.popToastAndSpeak (activity.getString (R.string.named_action_no_name));
						// ---------------------------------------------------------
						break;
					}
					// -------------------------------------------------------------
					// 03/08/2016 ECU now add the stored data
					// -------------------------------------------------------------
					NamedAction.Add (new NamedAction (inputName,inputActions));
					// -------------------------------------------------------------
					// 22/10/2016 ECU make sure that the disk is updated irrespective
					//                of the hash code
					// -------------------------------------------------------------
					updateTheDisk (true);
					// -------------------------------------------------------------
					// 20/04/2018 ECU refresh the display
					// -------------------------------------------------------------
					initialiseDisplay (activity);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.named_actions_actions_wizard_button:
					// -------------------------------------------------------------
					// 03/08/2016 ECU start up the wizard to try and help define
					//                the actions string
					// 21/04/2018 ECU pass through the activity because the return
					//                method is non-static
					// -------------------------------------------------------------
					ActionCommandUtilities.SelectCommand (context,
														  activity,
														  Utilities.createAMethod 
														  	(NamedActionsActivity.class,"SetActionsMethod",StaticData.BLANK_STRING));
					// -------------------------------------------------------------
					break;
					// -------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	public void displayLayout ()
	{
		// -------------------------------------------------------------------------
		// 22/10/2016 ECU Note - display the format to create a new entry
		// -------------------------------------------------------------------------
		setContentView (R.layout.activity_named_actions);
		// -------------------------------------------------------------------------
					actionsEditText	= (EditText) activity.findViewById (R.id.named_actions_actions);
		Button		defineButton    = (Button)   activity.findViewById (R.id.named_actions_define_button);
					nameEditText	= (EditText) activity.findViewById (R.id.named_actions_name);
		Button		wizardButton    = (Button)   activity.findViewById (R.id.named_actions_actions_wizard_button);
		// -------------------------------------------------------------------------
		// 03/08/2016 ECU now set up the listeners
		// -------------------------------------------------------------------------
		defineButton.setOnClickListener (buttonListener);
		wizardButton.setOnClickListener (buttonListener);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
   	public void NoMethod (Object theSelection)
   	{
   	}
   	// =============================================================================
 	public void ProcessAction (int theIndex)
 	{
 		// -------------------------------------------------------------------------
 		// 05/08/2016 ECU process the 'long select' action
 		// 04/05/2017 ECU just confirm that the action is being processed
 		// -------------------------------------------------------------------------
 		Utilities.popToast (String.format (getString (R.string.named_action_beging_processed_format),
 											PublicData.namedActions.get (theIndex).name),true);
 		// -------------------------------------------------------------------------
 		// 08/08/2016 ECU add the final 'true' to flush any actions that may be queued
 		// -------------------------------------------------------------------------
 		Utilities.actionHandler (context,PublicData.namedActions.get (theIndex).actions,true);
 		// -------------------------------------------------------------------------
 	}
 	// =============================================================================
  	public void SelectAction (int theIndex)
  	{
  		// -------------------------------------------------------------------------
  		// 03/08/2016 ECU process the swipe action
  		// -------------------------------------------------------------------------
  		displayLayout ();
  		// -------------------------------------------------------------------------
  		// 03/08/2016 ECU prefill the fields
  		// -------------------------------------------------------------------------
  		actionsEditText.setText (PublicData.namedActions.get (theIndex).actions);
  		nameEditText.setText (PublicData.namedActions.get (theIndex).name);
  		// -------------------------------------------------------------------------
  	}
	// =============================================================================
	public void SetActionsMethod (String theActionCommands)
	{
		// -------------------------------------------------------------------------
		// 24/01/2016 ECU created to take the action commands and store away
		//            ECU check if something already exists then need to have the
		//                leading separator
		// 20/09/2016 ECU add the trim
		// -------------------------------------------------------------------------
		String currentEntry = actionsEditText.getText().toString().trim();
		
		if (!currentEntry.equalsIgnoreCase (StaticData.BLANK_STRING))
			actionsEditText.append (StaticData.ACTION_SEPARATOR);
		
		actionsEditText.append (theActionCommands);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void SwipeAction (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 03/08/2016 ECU process the swipe action
		// 20/09/2016 ECU changed to use the format resource
		// 07/06/2019 ECU changed from 'named_action_delete_format'
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.yesNo (context,
										  activity,
										  "Item Deletion",
										  String.format (getString (R.string.delete_confirmation_format), 
												  PublicData.namedActions.get (theIndex).name),
										  (Object) theIndex,
										  Utilities.createAMethod (NamedActionsActivity.class,"YesMethod",(Object) null),
										  Utilities.createAMethod (NamedActionsActivity.class,"NoMethod",(Object) null)); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void updateTheDisk (boolean theAlwaysFlag)
	{
		// -------------------------------------------------------------------------
		// 22/10/2016 ECU update the disk if data has changed
		//            ECU added the always flag to force a write even if the hash
		//                codes match
		// -------------------------------------------------------------------------
		if ((initialHashCode != PublicData.namedActions.hashCode()) || theAlwaysFlag)
		{
			AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + 
					getString (R.string.named_actions_file),PublicData.namedActions);
		}
	}
   	// =============================================================================
   	public void YesMethod (Object theSelection)
   	{
   		// -------------------------------------------------------------------------
   		// 03/08/2016 ECU the selected item can be deleted
   		// -------------------------------------------------------------------------
   		int localSelection = (Integer) theSelection;
   		PublicData.namedActions.remove (localSelection);
   		// -------------------------------------------------------------------------
   		// 03/08/2016 ECU check if everything has been deleted
   		// -------------------------------------------------------------------------
   		if (PublicData.namedActions.size () > 0)
   		{
   			// ---------------------------------------------------------------------
   			// 03/08/2016 ECU rebuild and then display the updated list view
   			// ---------------------------------------------------------------------
   			refreshDisplay ();
   			// ----------------------------------------------------------------------
   		}
   		else
   		{
   			// ---------------------------------------------------------------------
   			// 03/08/2016 ECU everything has been deleted
   			// 20/09/2016 ECU changed to use resource
   			// ---------------------------------------------------------------------
   			Utilities.popToastAndSpeak (getString (R.string.named_actions_all_deleted));
   			// ---------------------------------------------------------------------
   			// 03/08/2016 ECU terminate this activity
   			// ---------------------------------------------------------------------
   			finish ();
   			// ---------------------------------------------------------------------
   		}
   		// -------------------------------------------------------------------------
   	}
   	// =============================================================================
   	
  	// =============================================================================
  	// =============================================================================
  	// ListViewSelector
  	// ================
  	//
  	//		Declare methods associated with the use of ListViewSelector
  	//
  	// ============================================================================
  	// ============================================================================
  	
	// =============================================================================
	void initialiseDisplay (Activity theActivity)
	{
		// -------------------------------------------------------------------------
		// 20/04/2018 ECU created to generate the display of stored documents
		// -------------------------------------------------------------------------
		listViewSelector = new ListViewSelector (theActivity,
												 R.layout.named_actions_row,
				   								 "PopulateTheList",
				   								 true,
				   								"SelectAction",
				   								 null,
				   								 "ProcessAction",
				   								 getString (R.string.add),
				   								 "AddAction",
				   								 null,
				   								 "SwipeAction");
		// -------------------------------------------------------------------------
		// 20/04/2018 ECU display an initial information message about long press
		// 12/04/2019 ECU changed the name to 'press_process'
		// -------------------------------------------------------------------------
		Utilities.DisplayADrawable (context,R.drawable.press_process);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public ArrayList<ListItem> PopulateTheList ()
	{
		// -------------------------------------------------------------------------
		// 13/04/2018 ECU created to build a list of the currently named actions
		// -------------------------------------------------------------------------
		ArrayList<ListItem> listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.namedActions.size(); theIndex++)
		{
			// ---------------------------------------------------------------------
			// 03/08/2016 ECU populate the list that will be displayed
			// ---------------------------------------------------------------------
			listItems.add (new ListItem (StaticData.BLANK_STRING,
										 PublicData.namedActions.get(theIndex).name,
										 PublicData.namedActions.get(theIndex).actions,
										 StaticData.BLANK_STRING,
										 theIndex));
			// ---------------------------------------------------------------------
		}
		return listItems;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void refreshDisplay ()
	{
		// -------------------------------------------------------------------------
		// 13/04/2018 ECU created to refresh the display if it exists or create the
		//                display if not
		// -------------------------------------------------------------------------
		if (listViewSelector == null)
		{
			// ---------------------------------------------------------------------
			// 13/04/2018 ECU need to build the display
			// ---------------------------------------------------------------------
			initialiseDisplay (this);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 13/04/2018 ECU display already initialised so just refresh it
			// ---------------------------------------------------------------------
			listViewSelector.refresh ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
   	
}

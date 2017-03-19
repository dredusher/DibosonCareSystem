package com.usher.diboson;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
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
	//            ECU changd to use hash code instead of data changed
	// =============================================================================
	
	// =============================================================================
	static EditText	actionsEditText;
	static Activity	activity;
	static int		initialHashCode;						// 12/09/2016 ECU added
	static EditText	nameEditText;
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
				// 03/08/2016 ECU there are already a number of named actions
				// -----------------------------------------------------------------
				// 05/08/2016 ECU initialise the selector data
				// -----------------------------------------------------------------
				SelectorUtilities.Initialise();
				// -----------------------------------------------------------------
				// 04/08/2016 ECU added the 'back' method
				// 05/08/2016 ECU added the 'long select' method
				// -----------------------------------------------------------------
				BuildTheNamedActionsList ();
				SelectorUtilities.selectorParameter.rowLayout 					= R.layout.named_actions_row;
				SelectorUtilities.selectorParameter.classToRun 					= NamedActionsActivity.class;
				SelectorUtilities.selectorParameter.type 						= StaticData.OBJECT_NAMED_ACTIONS;
				SelectorUtilities.selectorParameter.sort						= false;
				SelectorUtilities.selectorParameter.backMethodDefinition 		= new MethodDefinition<NamedActionsActivity> (NamedActionsActivity.class,"BackKeyAction");
				SelectorUtilities.selectorParameter.customMethodDefinition 		= new MethodDefinition<NamedActionsActivity> (NamedActionsActivity.class,"AddAction");
				SelectorUtilities.selectorParameter.customLegend 				= getString (R.string.add);
				SelectorUtilities.selectorParameter.longSelectMethodDefinition 	= new MethodDefinition<NamedActionsActivity> (NamedActionsActivity.class,"TestAction");
				SelectorUtilities.selectorParameter.selectMethodDefinition 		= new MethodDefinition<NamedActionsActivity> (NamedActionsActivity.class,"SelectAction");
				SelectorUtilities.selectorParameter.swipeMethodDefinition 		= new MethodDefinition<NamedActionsActivity> (NamedActionsActivity.class,"SwipeAction");
				// -----------------------------------------------------------------
				// 05/08/2016 ECU specify the drawable that will give additional help
				//                to the user
				// -----------------------------------------------------------------
				SelectorUtilities.selectorParameter.drawableInitial = R.drawable.long_press;
				// -----------------------------------------------------------------
				SelectorUtilities.StartSelector (this,StaticData.OBJECT_NAMED_ACTIONS);
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
	public static void AddAction (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 03/08/2016 ECU add a new named action
		// -------------------------------------------------------------------------
		// 03/08/2016 ECU terminate the selector activity
		// -------------------------------------------------------------------------
		Selector.Finish ();
		// -------------------------------------------------------------------------
		// 03/08/2016 ECU display the layout for the input data
		// -------------------------------------------------------------------------
		displayLayout ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void BackKeyAction (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 04/08/2016 ECU created to be called when the back key pressed
		//            ECU just 'finish' this activity
		// -------------------------------------------------------------------------
		activity.finish ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static ArrayList<ListItem> BuildTheNamedActionsList ()
	{
		// -------------------------------------------------------------------------
		// 03/08/2016 ECU build up the list of named actions
		// -------------------------------------------------------------------------
		SelectorUtilities.selectorParameter.listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.namedActions.size(); theIndex++)
		{
			// ---------------------------------------------------------------------
			// 03/08/2016 ECU populate the list that will be displayed
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.listItems.add (new ListItem (
									"",
									PublicData.namedActions.get(theIndex).name,
									PublicData.namedActions.get(theIndex).actions,
									"",
									theIndex));
			// ---------------------------------------------------------------------
		}
		return SelectorUtilities.selectorParameter.listItems;
	}
	// =============================================================================
	private static View.OnClickListener buttonListener = new View.OnClickListener() 
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
					if (inputActions.equalsIgnoreCase (""))
					{
						// ---------------------------------------------------------
						// 03/08/2016 ECU no name has been entered
						// 20/09/2016 ECU changed to use the resource
						// ---------------------------------------------------------
						Utilities.popToastAndSpeak (activity.getString (R.string.named_action_no_actions));
						// ---------------------------------------------------------
						break;
					}
					if (inputName.equalsIgnoreCase(""))
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
					// 03/08/2016 ECU exit this activity
					// -------------------------------------------------------------
					activity.finish ();
					// -------------------------------------------------------------
					// 20/09/2016 ECU restart this activity
					// -------------------------------------------------------------
					Intent localIntent = activity.getIntent ();
					localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
					activity.startActivity (localIntent);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.named_actions_actions_wizard_button:
					// -------------------------------------------------------------
					// 03/08/2016 ECU start up the wizard to try and help define
					//                the actions string
					// -------------------------------------------------------------
					ActionCommandUtilities.SelectCommand (activity,
							Utilities.createAMethod (NamedActionsActivity.class,"SetActionsMethod",""));
					// -------------------------------------------------------------
					break;
					// -------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	public static void displayLayout ()
	{
		// -------------------------------------------------------------------------
		// 22/10/2016 ECU Note - display the format to create a new entry
		// -------------------------------------------------------------------------
		activity.setContentView (R.layout.activity_named_actions);
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
   	public static void NoMethod (Object theSelection)
   	{
   	}
   	// =============================================================================
 	public static void SelectAction (int theIndex)
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
	public static void SetActionsMethod (String theActionCommands)
	{
		// -------------------------------------------------------------------------
		// 24/01/2016 ECU created to take the action commands and store away
		//            ECU check if something already exists then need to have the
		//                leading separator
		// 20/09/2016 ECU add the trim
		// -------------------------------------------------------------------------
		String currentEntry = actionsEditText.getText().toString().trim();
		
		if (!currentEntry.equalsIgnoreCase (""))
			actionsEditText.append (StaticData.ACTION_SEPARATOR);
		
		actionsEditText.append (theActionCommands);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SwipeAction (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 03/08/2016 ECU process the swipe action
		// 20/09/2016 ECU changed to use the format resource
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (Selector.context,"Item Deletion",
				   				 String.format (Selector.context.getString (R.string.named_action_delete_format), 
						   				PublicData.namedActions.get (theIndex).name),
						   		 (Object) theIndex,
						   		 Utilities.createAMethod (NamedActionsActivity.class,"YesMethod",(Object) null),
						   		 Utilities.createAMethod (NamedActionsActivity.class,"NoMethod",(Object) null)); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void TestAction (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 05/08/2016 ECU process the 'long select' action
		// 08/08/2016 ECU add the final 'true' to flush any actions that may be queued
		// -------------------------------------------------------------------------
		Utilities.actionHandler (activity,PublicData.namedActions.get (theIndex).actions,true);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void updateTheDisk (boolean theAlwaysFlag)
	{
		// -------------------------------------------------------------------------
		// 22/10/2016 ECU update the disk if data has changed
		//            ECU added the always flag to force a write even if the hash
		//                codes match
		// -------------------------------------------------------------------------
		if ((initialHashCode != PublicData.namedActions.hashCode()) || theAlwaysFlag)
		{
			AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + 
					MainActivity.activity.getString (R.string.named_actions_file),PublicData.namedActions);
		}
	}
   	// =============================================================================
   	public static void YesMethod (Object theSelection)
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
   			Selector.Rebuild();
   			// ----------------------------------------------------------------------
   		}
   		else
   		{
   			// ---------------------------------------------------------------------
   			// 03/08/2016 ECU everything has been deleted
   			// 20/09/2016 ECU changed to use resource
   			// ---------------------------------------------------------------------
   			Utilities.popToastAndSpeak (activity.getString (R.string.named_actions_all_deleted));
   			// ---------------------------------------------------------------------
   			// 03/08/2016 ECU finish the selector activity
   			// ---------------------------------------------------------------------
   			Selector.Finish();
   			// ---------------------------------------------------------------------
   			// 03/08/2016 ECU terminate this activity
   			// ---------------------------------------------------------------------
   			activity.finish ();
   			// ---------------------------------------------------------------------
   		}
   		// -------------------------------------------------------------------------
   	}
   	// =============================================================================
}

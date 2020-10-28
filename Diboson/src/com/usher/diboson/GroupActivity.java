package com.usher.diboson;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import com.usher.diboson.utilities.SelectAnActivity;

import java.util.ArrayList;


public class GroupActivity extends Activity 
{
	// =============================================================================
	// 25/07/2020 ECU change the way groups are defined using a dialogue instead of
	//                using GridActivity
	// =============================================================================
		   int				  action;					// 11/10/2016 ECU added
		   Context			  context;					// 09/10/2016 ECU added
	static int				  currentGroup;
	       Button			  groupCreateButton;
	       Button			  groupDeleteGroupButton;
	       Button			  groupDeleteGroupsButton;
	       Button			  groupDisplayButton;
	       Button			  groupEnableButton;
	       Button			  groupRedefineButton;
		   Object             underlyingObject;
	// -----------------------------------------------------------------------------
	public static GroupMessageHandler groupMessageHandler;
	// =============================================================================
	// =============================================================================
	
	// -----------------------------------------------------------------------------
	// 06/10/2016 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// -----------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 06/10/2016 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 06/10/2016 ECU set to full screen before displaying the layout
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_group);
			// ---------------------------------------------------------------------
			// 09/10/2016 ECU remember the context for future use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 22/03/2018 ECU remember the underlying object
			// ---------------------------------------------------------------------
			underlyingObject = this;
			// ---------------------------------------------------------------------
			groupMessageHandler = new GroupMessageHandler ();
			// ---------------------------------------------------------------------
			// 11/10/2016 ECU define the buttons
			// ---------------------------------------------------------------------
			groupCreateButton 		= (Button) findViewById (R.id.group_create_button);
			groupDisplayButton 		= (Button) findViewById (R.id.group_display_button);
			groupDeleteGroupButton 	= (Button) findViewById (R.id.group_delete_button);
			groupDeleteGroupsButton = (Button) findViewById (R.id.group_delete_all_button);
			groupEnableButton 		= (Button) findViewById (R.id.group_enable_button);
			groupRedefineButton 	= (Button) findViewById (R.id.group_redefine_button);
			// ---------------------------------------------------------------------
			// 11/10/2016 ECU set up the listeners for the various buttons
			// ---------------------------------------------------------------------
			groupCreateButton.setOnClickListener (buttonListener);
			groupDisplayButton.setOnClickListener (buttonListener);
			groupDeleteGroupButton.setOnClickListener (buttonListener);
			groupDeleteGroupsButton.setOnClickListener (buttonListener);
			groupEnableButton.setOnClickListener (buttonListener);
			groupRedefineButton.setOnClickListener (buttonListener);
			// ---------------------------------------------------------------------
			// 11/10/2016 ECU set the visibility of the buttons
			// ---------------------------------------------------------------------
			displayButtons ();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 06/10/2016 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
			
	}
	// =============================================================================
    private View.OnClickListener buttonListener = new View.OnClickListener() 
	{
    	// -------------------------------------------------------------------------
		@Override
		public void onClick(View view) 
		{	
			//----------------------------------------------------------------------
			// 11/10/2016 ECU now process depending on which button pressed
			// ---------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.group_create_button:
					// -------------------------------------------------------------
					// 11/10/2016 ECU added to define a group
					// -------------------------------------------------------------
					defineAGroup (context,StaticData.NO_RESULT);
					// -------------------------------------------------------------	 
					break;
				// -----------------------------------------------------------------
				case R.id.group_delete_button:
					// -------------------------------------------------------------
					// 11/10/2016 ECU added to delete a group
					// -------------------------------------------------------------
					selectAGroup (StaticData.GROUP_DELETE);
					// -------------------------------------------------------------	 
					break;
				// -----------------------------------------------------------------
				case R.id.group_delete_all_button:
					// -------------------------------------------------------------
					// 09/10/2016 ECU added to remove all stored groups
					// -------------------------------------------------------------
					DialogueUtilitiesNonStatic.yesNo (context,
													  underlyingObject,
													  "Delete All Groups",
													  "Do you really want to delete all groups",
													  0,
													  Utilities.createAMethod (GroupActivity.class,"YesMethod",(Object) null),
													  null); 
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.group_display_button:
					// -------------------------------------------------------------
					// 11/10/2016 ECU added to display a group
					// -------------------------------------------------------------
					selectAGroup (StaticData.GROUP_DISPLAY);
					// -------------------------------------------------------------	 
					break;
				// -----------------------------------------------------------------
				case R.id.group_enable_button:
					// -------------------------------------------------------------
					// 11/10/2016 ECU toggle the enable state
					// -------------------------------------------------------------
					PublicData.storedData.groupActivities = !PublicData.storedData.groupActivities;
					// -------------------------------------------------------------
					// 04/11/2016 ECU default to the first group
					// 23/09/2017 ECU changed to use the method for initialisation
					// -------------------------------------------------------------
					initialiseGrouping (getBaseContext ());
					// -------------------------------------------------------------
					// 11/10/2016 ECU update the buttons
					// -------------------------------------------------------------
					displayButtons ();
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString (R.string.group_restart),true);
					// -------------------------------------------------------------
					// 11/10/2016 ECU finish and then restart this app - the 'true'
					//                indicates that data MUST be written to disk
					// -------------------------------------------------------------
					groupMessageHandler.sendEmptyMessageDelayed (StaticData.MESSAGE_RESTART,7000);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.group_redefine_button:
					// -------------------------------------------------------------
					// 11/10/2016 ECU added to display a group
					// -------------------------------------------------------------
					selectAGroup (StaticData.GROUP_REDEFINE);
					// -------------------------------------------------------------	 
					break;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	public void CancelMethod (String theName)
	{
		// -------------------------------------------------------------------------
		// 25/07/2020 ECU want to cancel the current 'group definition
		// -------------------------------------------------------------------------
		PublicData.storedData.groupLists.remove (currentGroup);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void defineAGroup (Context theContext,int theGroup)
	{
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU check if this is a new group or an existing one
		// -------------------------------------------------------------------------
		currentGroup = theGroup;
		// -------------------------------------------------------------------------
		// 06/10/2016 ECU set up a list of items
		// -------------------------------------------------------------------------
		if (theGroup == StaticData.NO_RESULT)
		{
			// ---------------------------------------------------------------------
			// 11/10/2016 ECU a new group is to be created
			// ---------------------------------------------------------------------
			if (PublicData.storedData.groupLists == null)
			{
				PublicData.storedData.groupLists = new ArrayList<GroupList> ();
			}
			// ---------------------------------------------------------------------
			// 08/10/2016 ECU remember the current size
			// 28/07/2020 ECU change the order in which the current group is set
			// ---------------------------------------------------------------------
			currentGroup = PublicData.storedData.groupLists.size ();
			// ---------------------------------------------------------------------
			// 28/07/2020 ECU set a default name for this group
			// ---------------------------------------------------------------------
			PublicData.storedData.groupLists.add (new GroupList ("Group " + currentGroup));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 28/11/2014 ECU ask the GridActivity to return the selected intent
		// 25/07/2020 ECU change to use a dialogue rather than using GridActivity
		// 28/07/2020 ECU add the 'activities' as an arguments
		// -------------------------------------------------------------------------
		SelectAnActivity.ChooseActivities (theContext,Utilities.createAMethod
								(GroupActivity.class,"DefineActivitiesMethod",new int [1]),
								PublicData.storedData.groupLists.get (currentGroup).activities);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void DefineActivitiesMethod (int [] theChosenActivities)
	{
		// -------------------------------------------------------------------------
		// 28/07/2020 ECU want to clear the existing list
		// -------------------------------------------------------------------------
		PublicData.storedData.groupLists.get (currentGroup).ClearActivities ();
		// -------------------------------------------------------------------------
		// 25/07/2020 ECU add the designated activities into the current group
		// -------------------------------------------------------------------------
		for (int index = 0; index < theChosenActivities.length; index++)
		{
			PublicData.storedData.groupLists.get (currentGroup).AddActivity (theChosenActivities [index]);
		}
		// ------------------------------------------------------------------------
		// 25/07/2020 ECU now get the name of the group
		// ------------------------------------------------------------------------
		groupMessageHandler.sendEmptyMessage (StaticData.MESSAGE_FINISH);
		// ------------------------------------------------------------------------
	}
	// =============================================================================
	void deleteAGroup (int theGroup)
	{
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU created to delete the specified group
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU tell the user what has happened
		// -------------------------------------------------------------------------
		Utilities.popToast (String.format(context.getString(R.string.group_delete_format),
					PublicData.storedData.groupLists.get (theGroup).groupListName));
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU now delete the group
		// -------------------------------------------------------------------------
		PublicData.storedData.groupLists.remove (theGroup);
		// ---------------------------------------------------------------------
		// 11/10/2016 ECU set the visibility of the buttons
		// ---------------------------------------------------------------------
		displayButtons ();
		// ---------------------------------------------------------------------
	}
	// =============================================================================
	void displayButtons ()
	{
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU check on visibility state required
		// -------------------------------------------------------------------------
		int localVisibility = View.VISIBLE;
		if ((PublicData.storedData.groupLists == null) || (PublicData.storedData.groupLists.size() == 0))
		{
			localVisibility = View.INVISIBLE;
		}
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU apply the generated visibility
		// -------------------------------------------------------------------------
		groupDeleteGroupButton.setVisibility (localVisibility);
		groupDeleteGroupsButton.setVisibility (localVisibility);
		groupDisplayButton.setVisibility (localVisibility);
		groupEnableButton.setVisibility (localVisibility);
		groupRedefineButton.setVisibility (localVisibility);
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU check the legend wanted on the enable button
		// -------------------------------------------------------------------------
		groupEnableButton.setText (PublicData.storedData.groupActivities ? "Disable Grouping" : "Enable Grouping");			
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void displayAGroup (int theGroup)
	{
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU created to display the contents of a group list
		// 18/04/2017 ECU added context as argument to ....Titles
		// 22/03/2018 ECU changed to specify the underlying object
		// 28/07/2020 ECU change the message on the button from '.cancel'
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.listChoice (context,
											   underlyingObject,
											   String.format (context.getString(R.string.group_list_format),PublicData.storedData.groupLists.get(theGroup).groupListName),
											   GroupList.getActivityTitles (context,theGroup),
											   null,
											   context.getString (R.string.remove_this_message),
											   null);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	class GroupMessageHandler extends Handler
	{
		// -------------------------------------------------------------------------
		// 04/10/2016 ECU created to handle any screen updates
		// -------------------------------------------------------------------------
		@Override
		public void handleMessage (Message theMessage) 
		{   
			// ---------------------------------------------------------------------
			// 04/10/2016 ECU change to switch on the type of message received
			//                which is in '.what'
			// ---------------------------------------------------------------------
			switch (theMessage.what)
			{
	        	// -----------------------------------------------------------------	
				case StaticData.MESSAGE_FINISH:
					// -------------------------------------------------------------
					// 04/10/2016 ECU stop the refresh
					// -------------------------------------------------------------
					// 09/10/2016 ECU request the name of this group
					// 20/03/2017 ECU added the HINT
					//            ECU Note - the following Dialogue does not display
					//                       a cursor - is this because not on the
					//                       UI ?? Not sure this is a big issue.
					// 25/07/2020 ECU added the cancel method
					// 28/07/2020 ECU display the current group list name
					// -------------------------------------------------------------
					DialogueUtilitiesNonStatic.textInput (context,
													      underlyingObject,
													      context.getString (R.string.title_group_list_name),
													      context.getString (R.string.enter_group_list_name),
													      PublicData.storedData.groupLists.get(currentGroup).groupListName,
													      Utilities.createAMethod (GroupActivity.class,"GroupNameMethod",StaticData.BLANK_STRING),
														  Utilities.createAMethod (GroupActivity.class,"CancelMethod",StaticData.BLANK_STRING));
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_RESTART:
					// -------------------------------------------------------------
					// 11/10/2016 ECU finish and then restart this app - the 'true'
					//                indicates that data MUST be written to disk
					// -------------------------------------------------------------
					Utilities.FinishAndRestartApp (context,true);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
	};
	// =============================================================================
	public void GroupNameMethod (String theName)
	{
		// -------------------------------------------------------------------------
		// 20/03/2017 ECU if no name is specified then ask for it again
		// 06/01/2019 ECU changed to use 'emptyString'
		// -------------------------------------------------------------------------
		if (Utilities.emptyString(theName))
		{
			// ---------------------------------------------------------------------
			// 08/10/2016 ECU created to input the group list name
			// ---------------------------------------------------------------------
			PublicData.storedData.groupLists.get (currentGroup).groupListName = theName;
			// ---------------------------------------------------------------------
			// 11/10/2016 ECU now display the newly created group
			// ---------------------------------------------------------------------
			displayAGroup (currentGroup);
			// ---------------------------------------------------------------------
			// 11/10/2016 ECU set the visibility of the buttons
			// ---------------------------------------------------------------------
			displayButtons ();
			// ---------------------------------------------------------------------
			// 23/01/2016 ECU indicate that the command is complete
			// 11/10/2016 ECU removed
			// ---------------------------------------------------------------------
			//((Activity) context).finish();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 20/03/2017 ECU tell the user that a name needs to be entered
			// ---------------------------------------------------------------------
			MessageHandler.popToastAndSpeak (context.getString (R.string.group_no_name));
			// ---------------------------------------------------------------------
			// 20/03/2017 ECU request the group name again
			// ---------------------------------------------------------------------
			groupMessageHandler.sendEmptyMessage (StaticData.MESSAGE_FINISH);
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static void initialiseGrouping (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 23/09/2017 ECU created to initialise variables when grouping is switched
		//                on
		// -------------------------------------------------------------------------
		PublicData.storedData.groupListCurrent = 0;
		// -------------------------------------------------------------------------
		// 23/09/2017 ECU 'sort by usage' is not compatible with 'grouping' so switch
		//                it off
		// -------------------------------------------------------------------------
		if (PublicData.storedData.groupActivities)
		{
			// ---------------------------------------------------------------------
			// 23/09/2017 ECU switch off 'sort by usage' if on and tell the user
			// ---------------------------------------------------------------------
			if (PublicData.storedData.sortByUsage)
			{
				PublicData.storedData.sortByUsage = false;
				Utilities.popToastAndSpeak (theContext.getString (R.string.group_usage_off),true);
			}
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	void selectAGroup (int theAction)
	{
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU created to remember the action and request the selection
		//                of a group
		// -------------------------------------------------------------------------
		action = theAction;
		// -------------------------------------------------------------------------
		// 22/03/2018 ECU change to specify the underlying object
		// 28/07/2020 ECU change button legend from '.cancel'
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.listChoice (context,
										       underlyingObject,
										       "Select a Group",
										       GroupList.getTitles (),
										       Utilities.createAMethod (GroupActivity.class,"SelectGroupMethod",0),
										       context.getString (R.string.remove_this_message),
										       null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void SelectGroupMethod (int theGroup)
	{
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU created to receive the selected group name
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU decide what to do based on the 'action'
		// -------------------------------------------------------------------------
		switch (action)
		{
			// ---------------------------------------------------------------------
			case StaticData.GROUP_DELETE:
				// -----------------------------------------------------------------
				// 11/10/2016 ECU delete the specified group
				// -----------------------------------------------------------------
				deleteAGroup (theGroup);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case StaticData.GROUP_DISPLAY:
				// -----------------------------------------------------------------
				// 11/10/2016 ECU display the specified group
				// -----------------------------------------------------------------
				displayAGroup (theGroup);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case StaticData.GROUP_REDEFINE:
		        // -----------------------------------------------------------------
				// 11/10/2016 ECU redefine an existing group
				// -----------------------------------------------------------------
				defineAGroup (context,theGroup);
				// -----------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
  	public void YesMethod (Object theSelection)
  	{
  		// -------------------------------------------------------------------------
  		// 11/10/2016 ECU the groups can be deleted
  		// -------------------------------------------------------------------------
  		PublicData.storedData.groupLists = null;
		// -------------------------------------------------------------
		// 09/10/2016 ECU tell the user what has happened
		// -------------------------------------------------------------
		Utilities.popToastAndSpeak ("All groups have been removed",true);
  		// -------------------------------------------------------------------------
		// 11/10/2016 ECU set the visibility of the buttons
		// ---------------------------------------------------------------------
		displayButtons ();
		// ---------------------------------------------------------------------
  	}
  	// =============================================================================
}

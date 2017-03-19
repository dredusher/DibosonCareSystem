package com.usher.diboson;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;


public class GroupActivity extends Activity 
{
	// =============================================================================
		   static int				  action;					// 11/10/2016 ECU added
		   static Context			  context;					// 09/10/2016 ECU added
	       static int				  currentGroup;
	       static Button			  groupCreateButton;
	       static Button			  groupDeleteGroupButton;
	       static Button			  groupDeleteGroupsButton;
	       static Button			  groupDisplayButton;
	       static Button			  groupEnableButton;
	       static Button			  groupRedefineButton;
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
					DialogueUtilities.yesNo (context,"Delete All Groups",
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
					// -------------------------------------------------------------
					PublicData.storedData.groupListCurrent = 0;
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
	static void defineAGroup (Context theContext,int theGroup)
	{
		Utilities.popToastAndSpeak (theContext.getString (R.string.group_select_activities),true);
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
			PublicData.storedData.groupLists.add (new GroupList ());
			// ---------------------------------------------------------------------
			// 08/10/2016 ECU remember the current size
			// ---------------------------------------------------------------------
			currentGroup = PublicData.storedData.groupLists.size () - 1;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 19/10/2016 ECU changed from GridImages to Integer
		// -------------------------------------------------------------------------
		PublicData.storedData.groupLists.get (currentGroup).activities = new ArrayList <Integer> ();
		// -------------------------------------------------------------------------
		// 28/11/2014 ECU ask the GridActivity to return the selected intent
		// -------------------------------------------------------------------------
		Intent intent = new Intent (theContext,GridActivity.class);
		intent.putExtra (StaticData.PARAMETER_GROUP,true);
		theContext.startActivity (intent);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void deleteAGroup (int theGroup)
	{
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU created to delete the specified group
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU tell the user what has happened
		// -------------------------------------------------------------------------
		Utilities.popToast (String.format(context.getString(R.string.group_delete_format),PublicData.storedData.groupLists.get (theGroup).groupListName));
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
	static void displayButtons ()
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
	static void displayAGroup (int theGroup)
	{
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU created to display the contents of a group list
		// -------------------------------------------------------------------------
		DialogueUtilities.listChoice (context,
				  					  String.format (context.getString(R.string.group_list_format),PublicData.storedData.groupLists.get(theGroup).groupListName),
				  					  GroupList.getActivityTitles (theGroup),
				  					  null,
				  					  context.getString (R.string.cancel),
				  					  null);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	static class GroupMessageHandler extends Handler
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
				case StaticData.MESSAGE_DATA:
					// -------------------------------------------------------------
					// 04/10/2016 ECU stop the refresh
					// -------------------------------------------------------------
					GroupList.Add (currentGroup,theMessage.arg1);
					// -------------------------------------------------------------
					break;
	        	// -----------------------------------------------------------------	
				case StaticData.MESSAGE_FINISH:
					// -------------------------------------------------------------
					// 04/10/2016 ECU stop the refresh
					// -------------------------------------------------------------
					// 09/10/2016 ECU request the name of this group
					// -------------------------------------------------------------
					DialogueUtilities.textInput (context,
		   					 					 context.getString (R.string.title_group_list_name),
		   					 					 context.getString (R.string.enter_group_list_name),
		   					 					 "",
		   					 					 Utilities.createAMethod (GroupActivity.class,"GroupNameMethod",""),
		   					 					 null);
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
	public static void GroupNameMethod (String theName)
	{
		// -------------------------------------------------------------------------
		// 08/10/2016 ECU created to input the group list name
		// -------------------------------------------------------------------------
		PublicData.storedData.groupLists.get (currentGroup).groupListName = theName;
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU now display the newly created group
		// -------------------------------------------------------------------------
		displayAGroup (currentGroup);
		// ---------------------------------------------------------------------
		// 11/10/2016 ECU set the visibility of the buttons
		// ---------------------------------------------------------------------
		displayButtons ();
		// -------------------------------------------------------------------------
		// 23/01/2016 ECU indicate that the command is complete
		// 11/10/2016 ECU removed
		// -------------------------------------------------------------------------
		//((Activity) context).finish();
		// -------------------------------------------------------------------------
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
		DialogueUtilities.listChoice (context,
									  "Select a Group",
									  GroupList.getTitles (),
									  Utilities.createAMethod (GroupActivity.class,"SelectGroupMethod",0),
									  context.getString (R.string.cancel),
									  null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SelectGroupMethod (int theGroup)
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
  	public static void YesMethod (Object theSelection)
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
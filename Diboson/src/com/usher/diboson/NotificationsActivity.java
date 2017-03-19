package com.usher.diboson;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;

// =================================================================================
// 13/07/2016 ECU created to handle local notifications
// =================================================================================

// =================================================================================
public class NotificationsActivity extends DibosonActivity 
{
	// =============================================================================
	static Activity activity;
	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			Utilities.SetUpActivity (this);
			// ---------------------------------------------------------------------
			// 13/07/2016 ECU remember the activity for later use
			// ---------------------------------------------------------------------
			activity = this;
			// ---------------------------------------------------------------------
			setContentView(R.layout.activity_notifications);
			// ---------------------------------------------------------------------
			// 05/08/2016 ECU initialise the selector data
			// ---------------------------------------------------------------------
			SelectorUtilities.Initialise();
			// ---------------------------------------------------------------------
			// 26/06/2016 ECU add the 'sort' flag
			// ---------------------------------------------------------------------
			BuildTheNotificationsList ();
			SelectorUtilities.selectorParameter.rowLayout 				= R.layout.notifications_row;
			SelectorUtilities.selectorParameter.backMethodDefinition 	
				= new MethodDefinition<NotificationsActivity> (NotificationsActivity.class,"BackAction");
			SelectorUtilities.selectorParameter.selectMethodDefinition 	
				= new MethodDefinition<NotificationsActivity> (NotificationsActivity.class,"BackAction");
			SelectorUtilities.selectorParameter.classToRun 				= NotificationsActivity.class;
			SelectorUtilities.selectorParameter.type 					= StaticData.OBJECT_NOTIFICATIONS;
			SelectorUtilities.selectorParameter.sort					= false;
			SelectorUtilities.selectorParameter.customMethodDefinition 	
				= new MethodDefinition<NotificationsActivity> (NotificationsActivity.class,"AcknowledgeAction");
			SelectorUtilities.selectorParameter.customLegend 			= getString (R.string.acknowledge);
			// ---------------------------------------------------------------------
			SelectorUtilities.StartSelector (this,StaticData.OBJECT_SELECTED_CHANNELS);
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/11/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
    public static void AcknowledgeAction (int theNotification)
    {
    	// -------------------------------------------------------------------------
    	// 13/07/2016 ECU created to be actioned when the user acknowledges the
    	//                notification
    	// -------------------------------------------------------------------------
    	NotificationMessage.Delete (theNotification);
    	// -------------------------------------------------------------------------
    	// 13/12/2016 ECU check if there are any notifications left
    	// -------------------------------------------------------------------------
    	if (PublicData.storedData.notificationMessages.size() > 0)
    	{
    		// ---------------------------------------------------------------------
    		// 13/12/2016 ECU there are still notifications so update the current
    		//                display
    		// ---------------------------------------------------------------------
    		Selector.Rebuild();
    		// ---------------------------------------------------------------------
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 13/12/2016 ECU all of the notifications have been processed
    		// ---------------------------------------------------------------------
    		Utilities.popToastAndSpeak (activity.getString (R.string.notifications_acknowledged),true);
    		// ---------------------------------------------------------------------
    		// 13/12/2016 ECU now finish the activities
    		// ---------------------------------------------------------------------
    		Selector.Finish ();
    		activity.finish ();
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void BackAction (int theDummyNotification)
    {
    	// -------------------------------------------------------------------------
    	// 13/07/2016 ECU created to be actioned when the user presses the back key
    	// -------------------------------------------------------------------------
    	activity.finish ();
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
	public static ArrayList<ListItem> BuildTheNotificationsList ()
	{
		SelectorUtilities.selectorParameter.listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 18/09/2015 ECU add in the check on size
		// -------------------------------------------------------------------------  
		for (int theIndex = 0; theIndex < PublicData.storedData.notificationMessages.size(); theIndex++)
		{
			// ---------------------------------------------------------------------
			// 18/09/2015 ECU added the index as an argument
			// 23/06/2016 ECU added the channel's ID
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.listItems.add (new ListItem (
							"",
							PublicData.storedData.notificationMessages.get(theIndex).CreationTime (),
							"     " + PublicData.storedData.notificationMessages.get(theIndex).message,
							"",
							theIndex));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		return SelectorUtilities.selectorParameter.listItems;
	}
	// =============================================================================
}
// =================================================================================

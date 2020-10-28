package com.usher.diboson;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Collections;

// =================================================================================
// 13/07/2016 ECU created to handle local notifications
// 10/04/2018 ECU changed to use ListViewSelector rather than Selector
// 17/07/2020 ECU use 'notificationOrder' to determine how the notifications are
//                to be displayed
// =================================================================================

// =================================================================================
public class NotificationsActivity extends DibosonActivity 
{
	// =============================================================================
	// 17/07/2020 ECU declare 'listItems' here because 'SelectAction' needs to access
	//                the 'index' in each item so that the order of display becomes
	//                irrelevant
	// -----------------------------------------------------------------------------
	ArrayList<ListItem> listItems;
	ListViewSelector 	listViewSelector;
	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 10/04/2018 ECU added the 'full screen' flag
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_notifications);
			// ---------------------------------------------------------------------
			// 10/04/2018 ECU set up the display
			// ---------------------------------------------------------------------
			initialiseDisplay (this);
			// ---------------------------------------------------------------------
			// 18/07/2020 ECU tell the user of the order of the display
			//            ECU but only if there is more than one notification
			// ---------------------------------------------------------------------
			if (listItems.size() > 1)
			{
				Utilities.popToastAndSpeak(String.format (getString (R.string.notification_order_format),
											getString (PublicData.storedData.notificationOrder ? R.string.first
											                                                   : R.string.last)));
			}
			// ---------------------------------------------------------------------
			// 18/07/2020 ECU if displaying 'the latest entry last' then position
			//                the display to show the latest notification
			// ---------------------------------------------------------------------
			if (!PublicData.storedData.notificationOrder)
			{
				// -----------------------------------------------------------------
				// 18/07/2020 ECU in 'the latest entry last' mode so position to the
				//                last entry
				// -----------------------------------------------------------------
				listViewSelector.positionTo (listItems.size() - 1);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
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
    public void AcknowledgeAction (int theNotification)
    {
    	// -------------------------------------------------------------------------
    	// 13/07/2016 ECU created to be actioned when the user acknowledges the
    	//                notification
    	// 10/04/2018 ECU changed to use the listViewSelector and remove static
    	// -------------------------------------------------------------------------
    	NotificationMessage.Delete (theNotification);
    	// -------------------------------------------------------------------------
    	// 13/12/2016 ECU check if there are any notifications left
    	// -------------------------------------------------------------------------
    	if (PublicData.storedData.notificationMessages.size() > 0)
    	{
    		// ---------------------------------------------------------------------
    		// 10/04/2018 ECU redisplay the modified information
    		// ---------------------------------------------------------------------
    		listViewSelector.refresh ();
    		// ---------------------------------------------------------------------
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 13/12/2016 ECU all of the notifications have been processed
    		// ---------------------------------------------------------------------
    		Utilities.popToastAndSpeak (getString (R.string.notifications_acknowledged),true);
    		// ---------------------------------------------------------------------
    		// 13/12/2016 ECU now finish the activities
    		// ---------------------------------------------------------------------
    		finish ();
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
	void initialiseDisplay (Activity theActivity)
	{
		// -------------------------------------------------------------------------
		// 09/04/2018 ECU changed to use 'ListViewSelector' object
		// 10/04/2018 ECU changed the name to make easier to understand
		// 28/09/2020 ECU added the 'LongClickAction'
		// -------------------------------------------------------------------------
		listViewSelector = new ListViewSelector (theActivity,
												 R.layout.notifications_row,
				   								 Utilities.createAMethod (NotificationsActivity.class, "PopulateTheList"),
				   								 false,
				   								 Utilities.createAMethod (NotificationsActivity.class, "SelectAction",0),
												 Utilities.createAMethod (NotificationsActivity.class, "LongClickAction",0),
				   								 StaticData.NO_HANDLING_METHOD,
				   								 getString (R.string.acknowledge),
				   								 Utilities.createAMethod (NotificationsActivity.class, "AcknowledgeAction",0),
				   								 StaticData.NO_HANDLING_METHOD,
												 Utilities.createAMethod (NotificationsActivity.class, "SwipeAction",0)
				   								);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public ArrayList<ListItem> PopulateTheList ()
	{
		// -------------------------------------------------------------------------
		// 10/04/2018 ECU created to build the list of notifications
		// 17/07/2020 ECU decide the order in which the list is built
		//            ECU remove the declaration of listItems here - do at start of class
		// -------------------------------------------------------------------------
		listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 25/11/2018 ECU use a dummy listItem so that colour can be set
		// -------------------------------------------------------------------------
		ListItem localListItem;
		// -------------------------------------------------------------------------
		// 19/07/2020 ECU remember the size for later use
		// -------------------------------------------------------------------------
		int                 localListItemSize = PublicData.storedData.notificationMessages.size();
		NotificationMessage localNotificationMessage;
		// -------------------------------------------------------------------------
		// 10/04/2018 ECU add in the check on size
		// -------------------------------------------------------------------------  
		for (int theIndex = 0; theIndex < localListItemSize; theIndex++)
		{
			// ---------------------------------------------------------------------
			// 19/07/2020 ECU get the current notification
			// ---------------------------------------------------------------------
			localNotificationMessage = PublicData.storedData.notificationMessages.get (theIndex);
			// ---------------------------------------------------------------------
			// 25/11/2018 ECU create a local object for this notification
			// 15/07/2020 ECU remove the indent on the message line and included
			//                the title
			// 19/07/2020 ECU changed to use 'localNot...'
			// 05/08/2020 ECU added the 'click message'
			// ---------------------------------------------------------------------
			localListItem = new ListItem (StaticData.BLANK_STRING,
										  localNotificationMessage.CreationTime () +
												  ((localListItemSize > 1) ? String.format (getString (R.string.notification_number_format),theIndex + 1,localListItemSize)
												                           : StaticData.BLANK_STRING),
										  localNotificationMessage.title,
					                      localNotificationMessage.message +
					                      	((localNotificationMessage.clickMessage != null) ? localNotificationMessage.clickMessage
					                      	                                                 : StaticData.BLANK_STRING),
					 					  theIndex);
			// ---------------------------------------------------------------------
			// 25/11/2018 ECU set the colour for this entry
			//----------------------------------------------------------------------
			localListItem.colour = localNotificationMessage.colour;
			// ---------------------------------------------------------------------
			// 25/11/2018 ECU add the local object into the chain
			// ---------------------------------------------------------------------
			listItems.add (localListItem);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 17/07/2020 ECU decide whether the order is to be reversed
		// -------------------------------------------------------------------------
		if (PublicData.storedData.notificationOrder)
		{
			// ---------------------------------------------------------------------
			// 17/07/2020 ECU device has been configured to show the most recent
			//                notification first
			// ---------------------------------------------------------------------
			Collections.reverse (listItems);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 10/04/2018 ECU return the generated items
		// -------------------------------------------------------------------------
		return listItems;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void LongClickAction (int theNotification)
	{
		// -------------------------------------------------------------------------
		// 28/09/2020 ECU created to handle the long click on a notification
		// -------------------------------------------------------------------------
		// 28/09/2020 ECU because the display is changeable to show either 'in order
		//                of receipt' or 'latest first' then need to use the 'index'
		//                that is stored in the 'listItem' entry
		// -------------------------------------------------------------------------
		theNotification = listItems.get (theNotification).index;
		// -------------------------------------------------------------------------
		switch (PublicData.storedData.notificationMessages.get (theNotification).type)
		{
			// ---------------------------------------------------------------------
			case NotificationMessage.NOTIFICATION_TYPE_TRACKED:
			case NotificationMessage.NOTIFICATION_TYPE_TRACKED_CONTACT:
				// -----------------------------------------------------------------
				// 27/07/2020 ECU process the embedded object
				// 03/08/2020 ECU added '...CONTACT'
				// -----------------------------------------------------------------
				NotificationMessage localMessage = PublicData.storedData.notificationMessages.get (theNotification);
				if (localMessage.object != null)
				{
					((BluetoothTrackingData) localMessage.object).ProcessNotification (getBaseContext());
				}
				// -----------------------------------------------------------------
				break;
			// -----------------------------------------------------------------
			default:
				// -----------------------------------------------------------------
				// 17/07/2020 ECU indicate that there is no more information
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (getString (R.string.notification_no_information),true);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void SelectAction (int theNotification)
	{
		// -------------------------------------------------------------------------
		// 10/04/2018 ECU created to handle the select on an item
		//            ECU just want to exit this activity
		// -------------------------------------------------------------------------
		// 17/07/2020 ECU because the display is changeable to show either 'in order
		//                of receipt' or 'latest first' then need to use the 'index'
		//                that is stored in the 'listItem' entry
		// -------------------------------------------------------------------------
		theNotification = listItems.get (theNotification).index;
		// -------------------------------------------------------------------------
		// 28/09/2020 ECU remove the 'bluetooth tracking' notifications which now
		//                require a 'long click'
		// -------------------------------------------------------------------------
		switch (PublicData.storedData.notificationMessages.get (theNotification).type)
		{
			// ---------------------------------------------------------------------
			case NotificationMessage.NOTIFICATION_TYPE_DEBUG:
				// -----------------------------------------------------------------
				// 25/04/2020 ECU this is a 'debug' notification so display more
				//                information for the user
				// -----------------------------------------------------------------
				Intent localIntent = new Intent (getBaseContext(),DisplayAMessage.class);
				localIntent.putExtra (StaticData.PARAMETER_LAYOUT,R.layout.display_debug_information);
				localIntent.putExtra (StaticData.PARAMETER_MESSAGE,Utilities.readRawResource (this,R.raw.debug_message));
				localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity (localIntent);
				// -----------------------------------------------------------------
				break;
				// -----------------------------------------------------------------
			default:
				// -----------------------------------------------------------------
				// 17/07/2020 ECU indicate that there is no more information
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (getString (R.string.notification_no_information),true);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 17/07/2020 ECU remove the finish so that the use can handle more than
		//                one notification
		// -------------------------------------------------------------------------
		// 17/07/2020 ECU finish ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void SwipeAction (int theNotification)
	{
		// -------------------------------------------------------------------------
		// 18/07/2020 ECU created to handle the 'long' click
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.yesNo (this,
				                          this,
										  getString (R.string.title_notifications),
				                          getString (R.string.question_notifications),
				                          null,
				                          Utilities.createAMethod (NotificationsActivity.class,"YesMethod",(Object) null),
				                          null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void YesMethod (Object theSelection)
	{
		// -------------------------------------------------------------------------
		// 18/07/2020 ECU try and acknowledge everything
		//                this may look clumsy but directly deleting from the
		//                array list does not give the desired effect
		//                i.e. '..clear ()' empties the data does not delete the
		//                     records, 'remmoveAll (...)' also not what was wanted
		// 19/07/2020 ECU after a rethink decide to use 'clear' as quick and the
		//                'null'ed array can be garbage collected
		//            ECU changed to use the new method
		// -------------------------------------------------------------------------
		NotificationMessage.DeleteAll() ;
		// -------------------------------------------------------------------------
		// 19/07/2020 ECU there are no notifications so finish this activity
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}
// =================================================================================

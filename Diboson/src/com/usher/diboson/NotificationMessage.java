package com.usher.diboson;

import android.os.Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NotificationMessage implements Serializable
{
	// =============================================================================
	// 13/07/2016 ECU created to be the object that will constitute the notification
	//                message used by the care system
	// 27/07/2020 ECU added the 'object'
	// 05/08/2020 ECU added the 'click message'
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================

	// =============================================================================
	public static final int NOTIFICATION_TYPE_DEBUG 			= 0;
	public static final int NOTIFICATION_TYPE_NORMAL 			= 1;
	public static final int NOTIFICATION_TYPE_TRACKED 			= 2;
	public static final int NOTIFICATION_TYPE_TRACKED_CONTACT 	= 3;
	// ----------------------------------------------------------------------------
	public static final boolean NOTIFICATION_MULTIPLE_ENTRY		= false;
	public static final boolean NOTIFICATION_SINGLE_ENTRY		= true;
	// =============================================================================
	
	// =============================================================================
	// 13/07/2016 ECU declare the static data - unique to class
	// -----------------------------------------------------------------------------
	static 	boolean	dataChanged					= false;
	static  boolean notificationButtonUpdated 	= false;
	// =============================================================================
	
	// =============================================================================
	// 13/07/2016 ECU declare the object's data
	// 25/11/2018 ECU added the colour
	// 25/04/2020 ECU added the type
	// 17/05/2020 ECU added the title
	// 05/08/2020 ECU added the 'click message'
	// -----------------------------------------------------------------------------
	String	clickMessage;					// the click message, optional
	int		colour;							// colour of the entry
	long	creationTime;					// when the notification was created
	String	message;						// the message in the notification
	Object	object;							// associated object
	String  title;							// the title for the message
	int		type;							// notifcation type
	// =============================================================================
	
	// =============================================================================
	// 13/07/2016 ECU declare the constructors
	// -----------------------------------------------------------------------------
	public NotificationMessage (String 	theTitle,
							    String 	theMessage,
							    int 	theMessageColour,
							    String 	theClickMessage,
							    int 	theType,
							    Object 	theObject)
	{
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU store the message
		// -------------------------------------------------------------------------
		message			= theMessage;
		// -------------------------------------------------------------------------
		// 05/08/2020 ECU check if the 'click message' is to be added
		// -------------------------------------------------------------------------
		clickMessage 	= theClickMessage;
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU store when this notification added
		// -------------------------------------------------------------------------
		creationTime	= Utilities.getAdjustedTime (true);
		// -------------------------------------------------------------------------
		// 25/11/2018 ECU default to indicate the colour wanted
		// -------------------------------------------------------------------------
		colour = theMessageColour;
		// -------------------------------------------------------------------------
		// 15/07/2020 ECU added the title
		// -------------------------------------------------------------------------
		title = theTitle;
		// -------------------------------------------------------------------------
		// 25/04/2020 ECU added the type of notification
		// -------------------------------------------------------------------------
		type = theType;
		// -------------------------------------------------------------------------
		// 27/07/2020 ECU added the 'object'
		// -------------------------------------------------------------------------
		object = theObject;
		// -------------------------------------------------------------------------
		// 03/08/2020 ECU display an OS notification
		// 12/08/2020 ECU changed to use the black and white icon
		// -------------------------------------------------------------------------
		Utilities.notification (R.drawable.notifications_notification_icon_bw,
								theTitle,
								theMessage,
								StaticData.NOTIFICATION_NOTIFICATION);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	public NotificationMessage (String 	theTitle,
							    String 	theMessage,
							    int 	theMessageColour,
							    int 	theType,
							    Object 	theObject)
	{
		// -------------------------------------------------------------------------
		// 05/08/2020 ECU this was the main 'construct' until 'the click message' was
		//                added
		// -------------------------------------------------------------------------
		this (theTitle,theMessage,theMessageColour,null,theType,theObject);
		// --------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public NotificationMessage (String 	theTitle,
								String 	theMessage,
								int 	theMessageColour,
								int 	theType)
	{
		// -------------------------------------------------------------------------
		this (theTitle,theMessage,theMessageColour,theMessageColour,null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public NotificationMessage (String 	theTitle,
								String 	theMessage,
								int 	theMessageColour)
	{
		// -------------------------------------------------------------------------
		// 25/04/2020 ECU added a 'normal' type of notification. This is the old
		//                main constructor
		// 15/07/2020 ECU added the title
		// -------------------------------------------------------------------------
		this (theTitle,theMessage,theMessageColour,NOTIFICATION_TYPE_NORMAL);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	// 13/07/2016 ECU declare local methods
	// =============================================================================
	public String CreationTime ()
	{
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU return the creation time of this notification
		// -------------------------------------------------------------------------
		return PublicData.dateFormatterFull.format (creationTime);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	// 13/07/2016 ECU declare the public methods
	// =============================================================================
	public static void Add (String theTitle,String theMessage,int theMessageColour,int theType,boolean theSingleEntryFlag,String theClickMessage,Object theObject)
	{
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU created to add a message into the change
		// 25/11/2018 ECU added the message colour
		// 25/04/2020 ECU added the message type
		// 15/07/2020 ECU added the title
		// 27/07/2020 ECU added the object
		// 05/08/2020 ECU added the 'click message'
		// 29/09/2020 ECU added the 'single entry flag'
		// -------------------------------------------------------------------------
		if (theSingleEntryFlag)
		{
			// ---------------------------------------------------------------------
			// 29/09/2020 ECU want to ensure that this is the only message of
			//                this type
			// ---------------------------------------------------------------------
			DeleteSelected (theType);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		PublicData.storedData.notificationMessages.add (new NotificationMessage
						(theTitle,theMessage,theMessageColour,theClickMessage,theType,theObject));
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU need to display the button 'blinking' - this is indicated
		//                by the 'true' as an argument
		// 15/07/2016 ECU put in check on 'null'
		// -------------------------------------------------------------------------
		if (GridActivity.gridRefreshHandler != null)
		{
			// ---------------------------------------------------------------------
			Message localMessage = GridActivity.gridRefreshHandler.obtainMessage (StaticData.MESSAGE_NOTIFICATION_START,true);
			GridActivity.gridRefreshHandler.sendMessage (localMessage);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 15/07/2016 ECU tell the user what is happening
		// 24/02/2019 ECU put in the try / catch - seem to get a rare error if the
		//                attempt to contact the NTP server fails and want to
		//                notify the user - seems as if there is a timing issue
		//                which means that 'MainActivity.activity' is not set
		// -------------------------------------------------------------------------
		try
		{
			MessageHandler.popToastAndSpeak (MainActivity.activity.getString (R.string.notification_added));
		}
		catch (Exception theException)
		{
			
		}
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void Add (String theTitle,String theMessage,int theMessageColour,int theType,boolean theSingleEntryFlag)
	{
		// -------------------------------------------------------------------------
		// 27/07/2020 ECU was the main method until the 'object' added
		// 29/09/2020 ECU added the 'single entry flag'
		// -------------------------------------------------------------------------
		Add (theTitle,theMessage,theMessageColour,theType,theSingleEntryFlag,null,null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void Add (String theTitle,String theMessage,int theMessageColour)
	{
		// -------------------------------------------------------------------------
		// 25/04/2020 ECU add a 'normal' notification
		// 15/07/2020 ECU added the title
		// 29/09/2020 ECU added the 'false' to indicate not a single entry
		// -------------------------------------------------------------------------
		Add (theTitle,theMessage,theMessageColour,NOTIFICATION_TYPE_NORMAL,false);
		//--------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void Add (String theTitle,String theMessage,int theMessageColour,Object theObject)
	{
		// -------------------------------------------------------------------------
		// 25/04/2020 ECU add a 'normal' notification
		// 15/07/2020 ECU added the title
		// 27/07/2020 ECU added the object
		// 05/08/2020 ECU added the 'null' to indicate no 'click message'
		// 29/09/2020 ECU added the 'false' to indicate not a 'single entry'
		// -------------------------------------------------------------------------
		Add (theTitle,theMessage,theMessageColour,NOTIFICATION_TYPE_NORMAL,false,null,theObject);
		//--------------------------------------------------------------------------
	}
	// =============================================================================
	public static void Check ()
	{
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU created to be called on a timed basis to decide if the
		//                user is to be informed of a new notification
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU first of all ensure that the data has been correctly
		//                initialised
		// -------------------------------------------------------------------------
		if (PublicData.storedData.notificationMessages == null)
			PublicData.storedData.notificationMessages	= new ArrayList<NotificationMessage>();
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU check if the button needs to be updated
		// -------------------------------------------------------------------------
		if (!notificationButtonUpdated || dataChanged)
		{
			// ---------------------------------------------------------------------
			// 13/07/2016 ECU make sure that the button is initially set
			// ---------------------------------------------------------------------
			notificationButtonUpdated = true;
			// ---------------------------------------------------------------------
			// 13/07/2016 ECU make sure data change is in the correct state
			// ---------------------------------------------------------------------
			dataChanged = false;
			// ---------------------------------------------------------------------
			// 13/07/2016 ECU decide if button is to be displayed
			// ---------------------------------------------------------------------
			if (PublicData.storedData.notificationMessages.size() > 0)
			{
				// -----------------------------------------------------------------
				// 13/07/2016 ECU need to display the button 'steady' - this is indicated
				//                by the 'false' as an argument
				// -----------------------------------------------------------------
				Message localMessage = GridActivity.gridRefreshHandler.obtainMessage (StaticData.MESSAGE_NOTIFICATION_START,false);
				GridActivity.gridRefreshHandler.sendMessage (localMessage);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 13/07/2016 ECU there are no notifications left
				// -----------------------------------------------------------------
				GridActivity.gridRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_NOTIFICATION_END);
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void Delete (int theNotificationIndex)
	{
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU created to cause the specified object to be deleted from
		//                the list
		// -------------------------------------------------------------------------
		PublicData.storedData.notificationMessages.remove (theNotificationIndex);
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU indicate that the data has changed
		// -------------------------------------------------------------------------
		NotificationMessage.dataChanged = true;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void DeleteAll ()
	{
		// -------------------------------------------------------------------------
		// 19/07/2020 ECU created to delete all of the notifications
		// -------------------------------------------------------------------------
		PublicData.storedData.notificationMessages.clear ();
		// -------------------------------------------------------------------------
		// 18/07/2020 ECU indicate that the data has changed
		// -------------------------------------------------------------------------
		NotificationMessage.dataChanged = true;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void DeleteSelected (int theType)
	{
		// -------------------------------------------------------------------------
		// 29/09/2020 ECU create to delete all notifications of the specified type
		// -------------------------------------------------------------------------
		if (PublicData.storedData.notificationMessages.size() > 0)
		{
			// ---------------------------------------------------------------------
			List<NotificationMessage> deletedNotificationMessages	= new ArrayList<NotificationMessage>();
			// ---------------------------------------------------------------------
			for (NotificationMessage notificationMessage : PublicData.storedData.notificationMessages)
			{
				// -----------------------------------------------------------------
				// 29/09/2020 ECU check if this message is of the specified type
				// -----------------------------------------------------------------
				if (notificationMessage.type == theType)
				{
					// -------------------------------------------------------------
					// 29/09/2020 ECU want to delete this entry
					// -------------------------------------------------------------
					deletedNotificationMessages.add (notificationMessage);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 29/09/2020 ECU check if there are any messages to delete
			// ---------------------------------------------------------------------
			if (deletedNotificationMessages.size() > 0)
			{
				// -----------------------------------------------------------------
				// 29/09/2020 ECU there are messages to delete
				// -----------------------------------------------------------------
				for (NotificationMessage notificationMessage : deletedNotificationMessages)
				{
					// -------------------------------------------------------------
					// 29/09/2020 ECU delete this message
					// -------------------------------------------------------------
					PublicData.storedData.notificationMessages.remove (notificationMessage);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// --------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

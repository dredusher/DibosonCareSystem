package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;

import android.os.Message;

public class NotificationMessage implements Serializable
{
	// =============================================================================
	// 13/07/2016 ECU created to be the object that will constitute the notification
	//                message used by the care system
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	// 13/07/2016 ECU declare the static data - unique to class
	// -----------------------------------------------------------------------------
	static 	boolean	dataChanged					= false;
	static  boolean notificationButtonUpdated 	= false;
	// =============================================================================
	
	// =============================================================================
	// 13/07/2016 ECU declare the object's data
	// -----------------------------------------------------------------------------
	long	creationTime;					// when the notification was created
	String	message;						// the message in the notification
	// =============================================================================
	
	// =============================================================================
	// 13/07/2016 ECU declare the constructor
	// -----------------------------------------------------------------------------
	public NotificationMessage (String theMessage)
	{
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU store the message
		// -------------------------------------------------------------------------
		message			= theMessage;
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU store when this notification added
		// -------------------------------------------------------------------------
		creationTime	= Utilities.getAdjustedTime (true);
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
		return PublicData.dateFormatterFull.format(creationTime);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	// 13/07/2016 ECU declare the public methods
	// =============================================================================
	public static void Add (String theMessage)
	{
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU created to add a message into the change
		// -------------------------------------------------------------------------
		PublicData.storedData.notificationMessages.add (new NotificationMessage (theMessage));
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU need to display the button 'blinking' - this is indicated
		//                by the 'true' as an argument
		// 15/07/2016 ECU put in check on 'null'
		// -------------------------------------------------------------------------
		if (GridActivity.gridRefreshHandler != null)
		{
			Message localMessage = GridActivity.gridRefreshHandler.obtainMessage (StaticData.MESSAGE_NOTIFICATION_START,true);
			GridActivity.gridRefreshHandler.sendMessage (localMessage);
		}
		// -------------------------------------------------------------------------
		// 15/07/2016 ECU tell the user what is happening
		// -------------------------------------------------------------------------
		MessageHandler.popToastAndSpeak (MainActivity.activity.getString (R.string.notification_added));
		// -------------------------------------------------------------------------
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
}

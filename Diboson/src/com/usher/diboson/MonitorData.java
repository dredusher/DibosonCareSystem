package com.usher.diboson;

import android.content.Context;

import java.io.Serializable;

public class MonitorData implements Serializable
{
	// =============================================================================
	// 26/04/2016 ECU created to contain data which will be sent from the device
	//                being monitored 
	// 30/03/2017 ECU previously the index into the 'activeImages' array in GridActivity
	//                was being passed but this array can differ between devices
	//                so now pass 'R.drawable....' corresponding to the item being
	//                selected.
	// 05/10/2020 ECU change to use 'Utilities.startASpecifiedActivity' rather than
	//                starting up 'GridActivity.class'
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	Object	dataObject;
	int		dataType;
	// =============================================================================
	
	// =============================================================================
	public MonitorData (int theType,Object theObject)
	{
		// -------------------------------------------------------------------------
		// 26/04/2016 ECU create an object from the supplied parameters
		// -------------------------------------------------------------------------
		dataObject	=	theObject;
		dataType	=	theType;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public static void receiveMonitorData (Context theContext,String theMonitoredAddress,Object theMonitorDataAsObject)
	{
		// -------------------------------------------------------------------------
		// 26/04/2016 ECU created to process an incoming 'monitor data' object
		// 28/04/2016 ECU added the sender's address as an argument
		// -------------------------------------------------------------------------
		// 26/04/2016 ECU get the 'type' of data being received
		// -------------------------------------------------------------------------
		MonitorData localMonitorData = (MonitorData) theMonitorDataAsObject;
		// -------------------------------------------------------------------------
		// 26/04/2016 ECU now switch depending on the type of data in the object
		// 28/04/2016 ECU decide how to handle the data
		// -------------------------------------------------------------------------
		if (!PublicData.monitorDataAction)
		{
			// ---------------------------------------------------------------------
			// 28/04/2016 ECU just display the received data
			// ---------------------------------------------------------------------
			String localMessage = "Monitored data from " +  
									Utilities.GetDeviceName (theMonitoredAddress) + 
										StaticData.NEWLINE + StaticData.NEWLINE;
			// ---------------------------------------------------------------------
			switch (localMonitorData.dataType)
			{	
				// -----------------------------------------------------------------
				case StaticData.MONITOR_DATA_ACTIONS:
					// -------------------------------------------------------------
					localMessage += "actions taken are " + (String) localMonitorData.dataObject;
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MONITOR_DATA_ACTIVITY:
					// -------------------------------------------------------------
					// 30/03/2017 ECU changed so that the object contains the 'R.drawable'
					//                rather than a position because devices may differ in
					//                how the images are displayed
					//            ECU include the legend
					// 01/04/2017 ECU changed to use new Legend method
					// 18/04/2017 ECU added ...activity as an argument
					// 19/04/2017 ECU did not like edit of 18/04 so changed the
					//                Legend method
					// -------------------------------------------------------------
					int localIndex = GridActivity.positionInActiveImages ((Integer) localMonitorData.dataObject);
					localMessage += "the activity selected is" + StaticData.NEWLINE + 
									   localIndex + "   '" + GridActivity.gridImages [localIndex].Legend () + "'";
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MONITOR_DATA_BATTERY:
					localMessage += "battery charge is " + (Integer) localMonitorData.dataObject + " %";
					break;
				// -----------------------------------------------------------------
				case StaticData.MONITOR_DATA_KEYSTROKE:
					localMessage += "keystroke " + (Integer) localMonitorData.dataObject;
					break;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 28/04/2016 ECU now display the message
			// ---------------------------------------------------------------------
			MessageHandler.popToastAndSpeak (localMessage);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 28/04/2016 ECU process the monitored data
			// ---------------------------------------------------------------------
			switch (localMonitorData.dataType)
			{	
				// -----------------------------------------------------------------
				case StaticData.MONITOR_DATA_ACTIONS:
					Utilities.actionHandler (theContext,(String) localMonitorData.dataObject);
					break;
				// -----------------------------------------------------------------
				case StaticData.MONITOR_DATA_ACTIVITY:
					// -------------------------------------------------------------
					// 05/10/2020 ECU changed from :-
					//					Intent localIntent = new Intent (theContext,GridActivity.class);
					//					localIntent.putExtra (StaticData.PARAMETER_POSITION,GridActivity.positionInActiveImages((Integer) localMonitorData.dataObject));
					//					localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
					//					theContext.startActivity (localIntent);
					//				  to the new method
					// -------------------------------------------------------------
					Utilities.startASpecficActivity (GridActivity.positionInActiveImages((Integer) localMonitorData.dataObject));
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MONITOR_DATA_BATTERY:
					break;
				// -----------------------------------------------------------------
				case StaticData.MONITOR_DATA_KEYSTROKE:
					break;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void sendMonitorData (Context theContext,int theType,Object theObject)
	{
		// -------------------------------------------------------------------------
		// 26/04/2016 ECU created to send monitor data - should the IP address be
		//                configured
		// -------------------------------------------------------------------------
		if (PublicData.monitorIPAddress != null)
		{
			// ---------------------------------------------------------------------
			// 26/04/2016 ECU add the data to the queue
			// ---------------------------------------------------------------------
			PublicData.monitorData.add (new MonitorData (theType,theObject));
			// ---------------------------------------------------------------------
			// 26/04/2016 ECU try and initiate a transmission
			// ---------------------------------------------------------------------
			PublicData.messageHandler.sendEmptyMessage (StaticData.MESSAGE_MONITOR);
			// ---------------------------------------------------------------------
		}
	}
	// -----------------------------------------------------------------------------
	public static void sendMonitorData (Context theContext,int theType,int theValue)
	{
		// -------------------------------------------------------------------------
		// 27/04/2016 ECU created to send an integer value
		// -------------------------------------------------------------------------
		sendMonitorData (theContext,theType,(Object) theValue);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void sendMonitorData (Context theContext,int theType,String theValue)
	{
		// -------------------------------------------------------------------------
		// 28/04/2016 ECU created to send an String value
		// -------------------------------------------------------------------------
		sendMonitorData (theContext,theType,(Object) theValue);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

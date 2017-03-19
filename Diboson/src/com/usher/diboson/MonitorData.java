package com.usher.diboson;

import java.io.Serializable;

import android.content.Context;
import android.content.Intent;

public class MonitorData implements Serializable
{
	// =============================================================================
	// 26/04/2016 ECU created to contain data which will be sent from the device
	//                being monitored 
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
			String localMessage = "Monitored data from " +  Utilities.GetDeviceName (theMonitoredAddress) + "\n\n";
			// ---------------------------------------------------------------------
			switch (localMonitorData.dataType)
			{	
				// -----------------------------------------------------------------
				case StaticData.MONITOR_DATA_ACTIONS:
					localMessage += "actions taken are " + (String) localMonitorData.dataObject;
					break;
				// -----------------------------------------------------------------
				case StaticData.MONITOR_DATA_ACTIVITY:
					localMessage += "activity selected is " + (Integer) localMonitorData.dataObject;
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
					Intent localIntent = new Intent (theContext,GridActivity.class);
					localIntent.putExtra (StaticData.PARAMETER_POSITION,(Integer) localMonitorData.dataObject);
					localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					theContext.startActivity (localIntent);
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
			PublicData.messageHandler.sendEmptyMessage(StaticData.MESSAGE_MONITOR);
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

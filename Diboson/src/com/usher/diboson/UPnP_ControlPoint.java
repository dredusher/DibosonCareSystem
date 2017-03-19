package com.usher.diboson;

import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.device.NotifyListener;
import org.cybergarage.upnp.device.SearchResponseListener;
import org.cybergarage.upnp.event.EventListener;
import org.cybergarage.upnp.ssdp.SSDPPacket;

public class UPnP_ControlPoint extends ControlPoint implements NotifyListener, EventListener, SearchResponseListener
{
	// =============================================================================
	public UPnP_ControlPoint ()
	{
		// -------------------------------------------------------------------------
		// 07/09/2016 ECU created to act as the control point for UPnP actions
		// -------------------------------------------------------------------------
		// 07/09/2016 ECU add the listeners for the various events, etc..
		// -------------------------------------------------------------------------
		addEventListener          (this);
		addNotifyListener         (this);
		addSearchResponseListener (this);
		// -------------------------------------------------------------------------
	}
	// =============================================================================    
	public void deviceNotifyReceived (SSDPPacket thePacket)
	{
		// -------------------------------------------------------------------------
		// 07/09/2016 ECU created to be called when a notification comes in from a device
		// -------------------------------------------------------------------------
		if (thePacket.isDiscover ()) 
		{
		}
		else
		// -------------------------------------------------------------------------
		if (thePacket.isAlive ()) 
		{
			// ---------------------------------------------------------------------
			String usn 	= thePacket.getUSN ();
			// ---------------------------------------------------------------------
			// 09/09/2016 ECU if the device is not already registered so add it
			// ---------------------------------------------------------------------
			if (usn.endsWith ("rootdevice") && !UPnPDevice.UDNexists (usn))
			{
				UPnP_Activity.messageHandler.sendEmptyMessage (StaticData.MESSAGE_NOTIFICATION_START);
			}
			// ---------------------------------------------------------------------
		}
		else 
		// -------------------------------------------------------------------------
		if (thePacket.isByeBye ()) 
		{ 
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void deviceSearchResponseReceived (SSDPPacket thePacket)
	{
		// -------------------------------------------------------------------------
		// 07/09/2016 ECU created to be called when a search response is received
		//                from a device
		// 12/09/2016 ECU send the message to cause a display
		//            ECU comment out while testing
		// -------------------------------------------------------------------------
		//UPnP_Activity.messageHandler.sendEmptyMessage (StaticData.MESSAGE_DISPLAY);
		// -------------------------------------------------------------------------
	}
	// =============================================================================	
	public void eventNotifyReceived (String theUUID, long theSequenceNumber, String theEventName, String theValue)
	{
		// -------------------------------------------------------------------------
		// 07/09/2016 ECU created to be called when notifications occur as the result
		//                of an event
		// --------------------------------------------------------------------------
		// 07/09/2016 ECU set the state of the specified device
		// --------------------------------------------------------------------------
		try
		{
			int localValue = Integer.parseInt (theValue);
			
			if ((localValue == 0) || (localValue == 1))
			{
				UPnPDevice.setState (this.getSubscriberService (theUUID).getDevice().getFriendlyName(),localValue,false);
				// -----------------------------------------------------------------
				// 07/09/2016 ECU request a display update
				// -----------------------------------------------------------------
				UPnP_Activity.messageHandler.sendEmptyMessage (StaticData.MESSAGE_REFRESH);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
		}
	}
	// =============================================================================
}

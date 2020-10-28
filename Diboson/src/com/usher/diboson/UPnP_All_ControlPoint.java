package com.usher.diboson;

import org.cybergarage.upnp.device.NotifyListener;
import org.cybergarage.upnp.device.SearchResponseListener;
import org.cybergarage.upnp.event.EventListener;
import org.cybergarage.upnp.ssdp.SSDPPacket;
import android.os.Message;
import com.usher.diboson.UPnP.ControlPoint;


public class UPnP_All_ControlPoint extends ControlPoint implements NotifyListener, EventListener, SearchResponseListener
{
	// =============================================================================
	public UPnP_All_ControlPoint ()
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
		String localInformation = thePacket.getRemoteAddress () + StaticData.NEWLINEx2;
		// -------------------------------------------------------------------------
		if (thePacket.isDiscover ()) 
		{
			localInformation += "isDiscover";
		}
		else
		// -------------------------------------------------------------------------
		if (thePacket.isAlive ()) 
		{
			localInformation += "isAlive";
			// ---------------------------------------------------------------------
		}
		else 
		// -------------------------------------------------------------------------
		if (thePacket.isByeBye ()) 
		{ 
			localInformation += "isByeBye";
		}
		// -------------------------------------------------------------------------
		Message localMessage = UPnP_Activity_All.displayHandler.obtainMessage (UPnP_Activity_All.MESSAGE_DISPLAY,
																				localInformation);
		UPnP_Activity_All.displayHandler.sendMessage (localMessage);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void deviceSearchResponseReceived (SSDPPacket thePacket)
	{
		// -------------------------------------------------------------------------
		// 07/09/2016 ECU created to be called when a search response is received
		//                from a device
		// 12/09/2016 ECU send the message to cause a display
		// -------------------------------------------------------------------------
		UPnP_Activity_All.displayHandler.sendEmptyMessage (UPnP_Activity_All.MESSAGE_DEVICES);
		// -------------------------------------------------------------------------
	}
	// =============================================================================	
	public void eventNotifyReceived (String theUUID, long theSequenceNumber, String theEventName, String theValue)
	{
		// -------------------------------------------------------------------------
		// 07/09/2016 ECU created to be called when notifications occur as the result
		//                of an event
		// --------------------------------------------------------------------------
		try
		{
			Message localMessage = UPnP_Activity_All.displayHandler.obtainMessage(UPnP_Activity_All.MESSAGE_DISPLAY,
					"eventNotifyReceived\n\n" + 
							this.getSubscriberService(theUUID).getDevice().getFriendlyName() + StaticData.NEWLINE + 
							theEventName + " " + theValue);
			UPnP_Activity_All.displayHandler.sendMessage (localMessage);
		}
		catch (Exception theException)
		{
			
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

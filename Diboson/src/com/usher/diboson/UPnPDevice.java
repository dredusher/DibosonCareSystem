package com.usher.diboson;

import android.graphics.Bitmap;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.Device;

import java.io.Serializable;

public class UPnPDevice implements Serializable
{
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	String			deviceDescription;				// 05/09/2016 ECU changed from List<String>
	String			deviceFriendlyName;				// 07/09/2016 ECU added
	Bitmap			deviceImageBitmap;
	String 			deviceImageURL;					// 06/09/2016 ECU added
	int				deviceState;					// 07/09/2016 ECU added
	String			deviceUDN;						// 09/09/2016 ECU added
	String			deviceURL;						// 06/09/2016 ECU added
	String			location;
	Device			upnpDevice;
	// =============================================================================
	
	// =============================================================================
	public UPnPDevice (Device theDevice)
	{
		// -------------------------------------------------------------------------
		// 26/08/2016 ECU copy across the defining 'location'
		// -------------------------------------------------------------------------
		deviceDescription	=	null;
		deviceFriendlyName	=	theDevice.getFriendlyName();
		deviceImageBitmap	= 	null;				// 06/09/2016 ECU preset to null		
		deviceImageURL		= 	null;				// 07/09/2016 ECU added
													// 09/09/2016 ECU set to NO_RESULT
		upnpDevice			=	theDevice;			// 06/09/2016 ECU added
		// -------------------------------------------------------------------------
		// 07/09/2016 ECU set up associated data
		// -------------------------------------------------------------------------
		deviceImageURL = upnpDevice.getLogoURLFromDevice();
		// -------------------------------------------------------------------------
		// 07/09/2016 ECU try and get the icon URL
		// -------------------------------------------------------------------------
		deviceImageBitmap = Utilities.getWebImage (deviceImageURL);
		// -------------------------------------------------------------------------
		// 09/09/2016 ECU store the unique device name of this device
		// -------------------------------------------------------------------------
		deviceUDN = upnpDevice.getUDN ();
		// -------------------------------------------------------------------------
		// 13/09/2016 ECU set up the initial state of the device
		// -------------------------------------------------------------------------
		deviceState = getState ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public String Print ()
	{
		String localString = "Friendly Name : " + deviceFriendlyName + StaticData.NEWLINE;
		localString += "Model Name : " + upnpDevice.getModelName() + StaticData.NEWLINE;
		localString += "Base URL : " + deviceURL + StaticData.NEWLINE;
		localString += "Image URL : " + deviceImageURL + StaticData.NEWLINE;
		localString += "State : " + deviceState + StaticData.NEWLINE;
				
		return localString;
	}
	// =============================================================================
	
	// =============================================================================
	void changeState (boolean theRecursiveState)
	{
		 // ------------------------------------------------------------------------
		 // 07/09/2016 ECU toggle the state of this device
		 // 13/09/2016 ECU added the theRecursiveState -- true if called recursively
		 // ------------------------------------------------------------------------
		 Action action = this.upnpDevice.getAction ("SetBinaryState"); 
		 action.setArgumentValue ("BinaryState", (1 - deviceState)); 
		 String actionResponse = action.postControlAction ();
		 // ------------------------------------------------------------------------
		 // 13/09/2016 ECU check if an error has occurred
		 //            ECU only allow only 'recursion'
		 // 12/09/2020 ECU added the check on null
		 // ------------------------------------------------------------------------
		 if ((actionResponse != null) && actionResponse.contains ("Error") && !theRecursiveState)
		 {
			 // --------------------------------------------------------------------
			 // 13/09/2016 ECU it appears that an error occurred when performing the
			 //                   action
			 // --------------------------------------------------------------------
			 // 13/09/2016 ECU reset the device's state
			 // --------------------------------------------------------------------
			 deviceState = getState ();
			 // --------------------------------------------------------------------
			 // 13/09/2016 ECU do a recursive call to get the state changed - the
			 //                the 'true' is to prevent an 'endless loop' situation
			 // --------------------------------------------------------------------
			 changeState (true);
			 // --------------------------------------------------------------------
		 }
		 // ------------------------------------------------------------------------
	}
	// =============================================================================
	int getState ()
	{
		// -------------------------------------------------------------------------
		// 13/09/2016 ECU get the state of the current device
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 13/09/2016 ECU convert the supplied state into the required integer
			//                form
			// ----------------------------------------------------------------------
			return Integer.parseInt (upnpDevice.getBinaryStateFromDevice());
			// ----------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 13/09/2016 ECU an error in conversion occurred
			// ---------------------------------------------------------------------
			return StaticData.NO_RESULT;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static void setState (String theFriendlyName,int theState,boolean theCheckFlag)
	{
		// -------------------------------------------------------------------------
		// 07/09/2016 ECU set the state of the UPnP device whose friendly name is
		//                supplied
		// 13/09/2016 ECU added the check flag
		// -------------------------------------------------------------------------
		if (PublicData.upnpDevices.size() > 0)
		{
			UPnPDevice upnpDevice;

			for (int index = 0; index < PublicData.upnpDevices.size(); index++)
			{
				upnpDevice = PublicData.upnpDevices.get(index);
				
				if (upnpDevice.deviceFriendlyName.equalsIgnoreCase (theFriendlyName))
				{
					if (upnpDevice.deviceState != theState && !theCheckFlag)
					{
						upnpDevice.deviceState = theState;
					
						PublicData.upnpDevices.set (index,upnpDevice);
						// ---------------------------------------------------------
						// 08/09/2016 ECU tell the user of the state change
						// ---------------------------------------------------------
						Utilities.SpeakAPhrase (UPnP_Activity.context,"The " + theFriendlyName + " is " + ((theState == 0) ? "off" : "on"));
						// ----------------------------------------------------------
					}
				}
			}
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean UDNexists (String theUDN)
	{
		if (PublicData.upnpDevices.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 09/09/2016 ECU loop through the stored devices - looking for a match
			// ---------------------------------------------------------------------
			for (int index = 0; index < PublicData.upnpDevices.size(); index++)
			{
				if (theUDN.startsWith (PublicData.upnpDevices.get (index).deviceUDN))
				{
					// -------------------------------------------------------------
					// 09/09/2016 ECU a matching device has been found so indicate
					//                the fact
					// -------------------------------------------------------------
					return true;
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
			// 09/09/2016 ECU no matching device
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
		else
			return false;
	}
	// =============================================================================
}

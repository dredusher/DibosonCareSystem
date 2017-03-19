package com.usher.diboson;

import java.io.Serializable;

import android.content.Context;
import android.os.Build;

public class Devices implements Serializable
{
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	int			apiLevel;				// 07/01/2016 ECU added - the API level
	boolean 	compatible;				// 19/08/2013 ECU indicates if device is compatible
	String		IPAddress;
	String		macAddress;				// 20/03/2015 ECU added - MAC address of this device
	String  	name;					// 06/08/2013 ECU added - plain text name
	boolean		phone;
	boolean		remoteController;		// 26/02/2016 ECU added - if device has access to 
										//                bluetooth remote controller
	String  	response;
	String		serialNumber;			// 20/03/2015 ECU added - serial number of this device
	boolean		wemo;					// 18/03/2015 ECU added - whether device can
										//                control Belkin WeMo devices
	/* ============================================================================= */
	public void Initialise (String theIPAddress,
							String theMACAddress,
							String theSerialNumber,
							boolean thePhone,
							String theResponse,
							boolean theCompatibilityFlag,
							boolean theWeMo,
							int theAPILevel,
							boolean theRemoteController)
	{
		apiLevel			= theAPILevel;					// 07/01/2016 ECU added
		compatible 			= theCompatibilityFlag;
		IPAddress 			= theIPAddress;
		macAddress			= theMACAddress;				// 20/03/2015 ECU added
		phone      			= thePhone;
		remoteController	= theRemoteController;			// 26/02/2016 ECU added
		response  			= theResponse;
		serialNumber		= theSerialNumber;				// 20/03/2015 ECU added
		wemo				= theWeMo;						// 18/03/2015 ECU added
	}
	// -----------------------------------------------------------------------------
	public void Initialise (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 20/03/2015 ECU created to initialise the class using local information
		// -------------------------------------------------------------------------
		compatible			= 	true;
		IPAddress			= 	Utilities.getIPAddress (theContext);
		macAddress			=   Utilities.getMACAddress (theContext);
		name				= 	android.os.Build.MODEL;
		phone				=	(Utilities.getPhoneNumber (theContext) != null);
		response			=   "initialisation";
		serialNumber		=	android.os.Build.SERIAL;
		// -------------------------------------------------------------------------
		// 20/03/2015 ECU initially was checking 'WeMoActivity.serviceRunning' but
		//                there was a delay which caused a wrong value so changed
		// -------------------------------------------------------------------------
		wemo			= 	PublicData.storedData.wemoHandling;
		// -------------------------------------------------------------------------
		// 07/01/2016 ECU store the API level
		// -------------------------------------------------------------------------
		apiLevel	    = 	Build.VERSION.SDK_INT;
		// -------------------------------------------------------------------------
		// 26/02/2016 ECU added to indicate if this device has access to a bluetooth
		//                remote controller
		// -------------------------------------------------------------------------
		remoteController	= 	PublicData.blueToothService;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public String Print ()
	{
		// -------------------------------------------------------------------------
		return "IP Address = " + IPAddress + 
				 (compatible ?
								("\nMAC Address = " + macAddress +		// 20/03/2015 ECU added
								 "\nName = " + name +
								 "\nSerial Number = " + serialNumber +	// 20/03/2015 ECU added
								 "\nAPI Level = " + apiLevel +			// 07/01/2016 ECU added
								 "\nPhone = " + phone + 
								 "\nRemote Controller = " + remoteController + 
						 												// 26/02/2016 ECU added
								 "\nBelkin WeMo Control = " + wemo +	// 18/03/2015 ECU added
								 "\nLast Response = " + response +
								 "\nCompatible = " + compatible) 
						     : "\nNon-compatible Device");
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public String PrintName ()
	{
		// -------------------------------------------------------------------------
		// 15/03/2015 ECU print the name and IPAddress of this device
		// -------------------------------------------------------------------------
		return String.format ("%-20s%s",name,"(" + IPAddress + ")");
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static int returnAPILevel (String theIPAddress)
	{
		// -------------------------------------------------------------------------
		// 07/01/2016 ECU created to return the API level associated with the IP
		//                address
		// -------------------------------------------------------------------------
		if (PublicData.deviceDetails.size() > 0)
		{
			for (int localDevice = 0; localDevice < PublicData.deviceDetails.size(); localDevice++)
			{
				if (PublicData.deviceDetails.get(localDevice).IPAddress.equalsIgnoreCase(theIPAddress))
				{
					return PublicData.deviceDetails.get(localDevice).apiLevel;
				}
			}
		}
		// -------------------------------------------------------------------------
		// 07/01/2016 ECU return the fact that no API level found
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String returnIPAddress (String theFormattedAddress)
	{
		String [] theParts = theFormattedAddress.split("[(]");
		
		return theParts [1].replace (")","").replaceAll(" ","");
	}
	// =============================================================================
	public static String returnMACAddress (String theIPAddress)
	{
		// -------------------------------------------------------------------------
		// 04/01/2017 ECU created to return the MAC address associated with the IP
		//                address
		// -------------------------------------------------------------------------
		if (PublicData.deviceDetails.size() > 0)
		{
			for (int localDevice = 0; localDevice < PublicData.deviceDetails.size(); localDevice++)
			{
				if (PublicData.deviceDetails.get(localDevice).IPAddress.equalsIgnoreCase(theIPAddress))
				{
					return PublicData.deviceDetails.get(localDevice).macAddress;
				}
			}
		}
		// -------------------------------------------------------------------------
		// 04/01/2017 ECU return the fact that no MAC address found
		// -------------------------------------------------------------------------
		return null;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String returnName (String theFormattedAddress)
	{
		String [] theParts = theFormattedAddress.split("[(]");
		// -------------------------------------------------------------------------
		// 16/06/2016 ECU remove any leading/trailing spaces
		// -------------------------------------------------------------------------
		return theParts [0].trim ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void sendHelloMessage ()
	{
		// -------------------------------------------------------------------------
		// 20/03/2015 ECU broadcast the 'hello' message to get all devices to request
		//                this device's details
		// 09/04/2016 ECU changed name from 'multicastMessage'
		// -------------------------------------------------------------------------
		PublicData.broadcastMessage = StaticData.BROADCAST_MESSAGE_HELLO;
		// -------------------------------------------------------------------------
		// 22/03/2015 ECU put entry into project log to indicate the fact
		// -------------------------------------------------------------------------
		Utilities.LogToProjectFile ("Devices","send the hello message");
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void writeToDisk (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 20/03/2015 ECU create to write the array of device details to disk
		// -------------------------------------------------------------------------
		AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + theContext.getString (R.string.devices_file),
														PublicData.deviceDetails);
	}
	// =============================================================================
}
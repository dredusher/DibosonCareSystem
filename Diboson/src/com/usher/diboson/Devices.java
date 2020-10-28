package com.usher.diboson;

import android.content.Context;
import android.os.Build;

import java.io.Serializable;

public class Devices implements Serializable
{
	// =============================================================================
	// 06/09/2017 ECU Note - contains the details of the device which is interchanged
	//                       between devices on the network to determine compatability
	//                       and facilities available on the device
	// 06/09/2017 ECU added the patientName because devices on the network may be
	//                handling different patients and want it to be that devices
	//                are only compatible if they have the same patient name.
	// 28/03/2019 ECU added 'nameOriginal' to be the original name of the device
	//                - 'name' can be set by the user
	// 27/04/2019 ECU added smartDevice
	// =============================================================================
	
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	int			apiLevel;				// 07/01/2016 ECU added - the API level
	boolean 	compatible;				// 19/08/2013 ECU indicates if device is compatible
	String		IPAddress;
	String		macAddress;				// 20/03/2015 ECU added - MAC address of this device
	String  	name;					// 06/08/2013 ECU added - plain text name
	String  	nameOriginal;			// 28/03/2019 ECU added - original plain text name
	String		patientName;			// 06/09/2017 ECU added
	boolean		phone;
	boolean		remoteController;		// 26/02/2016 ECU added - if device has access to 
										//                bluetooth remote controller
	String  	response;
	String		serialNumber;			// 20/03/2015 ECU added - serial number of this device
	boolean		smartDevice;			// 27/04/2019 ECU added - indicates if device is a smart device
	boolean		wemo;					// 18/03/2015 ECU added - whether device can
										//                control Belkin WeMo devices
	/* ============================================================================= */
	public void Initialise (String 	theIPAddress,
							String 	theMACAddress,
							String 	theSerialNumber,
							boolean thePhone,
							String 	theResponse,
							boolean theCompatibilityFlag,
							boolean theWeMo,
							int 	theAPILevel,
							boolean theRemoteController,
							String	thePatientName)
	{
		// -------------------------------------------------------------------------
		apiLevel			= theAPILevel;					// 07/01/2016 ECU added
		compatible 			= theCompatibilityFlag;
		IPAddress 			= theIPAddress;
		macAddress			= theMACAddress;				// 20/03/2015 ECU added
		patientName			= thePatientName;				// 06/09/2017 ECU added
		phone      			= thePhone;
		remoteController	= theRemoteController;			// 26/02/2016 ECU added
		response  			= theResponse;
		serialNumber		= theSerialNumber;				// 20/03/2015 ECU added
		smartDevice			= false;						// 27/04/2019 ECU added
		wemo				= theWeMo;						// 18/03/2015 ECU added
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public void Initialise (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 20/03/2015 ECU created to initialise the class using local information
		// 28/03/2019 ECU initialise 'nameOriginal'
		// 16/11/2019 ECU change nameOriginal initialisation from 'null'
		// -------------------------------------------------------------------------
		compatible			= 	true;
		IPAddress			= 	Utilities.getIPAddress (theContext);
		macAddress			=   Utilities.getMACAddress (theContext);
		name				= 	android.os.Build.MODEL;
		nameOriginal		=   name;
		phone				=	(Utilities.getPhoneNumber (theContext) != null);
		response			=   "initialisation";
		serialNumber		=	android.os.Build.SERIAL;
		// -------------------------------------------------------------------------
		// 20/03/2015 ECU initially was checking 'WeMoActivity.serviceRunning' but
		//                there was a delay which caused a wrong value so changed
		// -------------------------------------------------------------------------
		wemo				= 	PublicData.storedData.wemoHandling;
		// -------------------------------------------------------------------------
		// 07/01/2016 ECU store the API level
		// -------------------------------------------------------------------------
		apiLevel	    	= 	Build.VERSION.SDK_INT;
		// -------------------------------------------------------------------------
		// 26/02/2016 ECU added to indicate if this device has access to a bluetooth
		//                remote controller
		// -------------------------------------------------------------------------
		remoteController	= 	PublicData.blueToothService;
		// -------------------------------------------------------------------------
		// 06/09/2017 ECU set up the patient's full name
		// -------------------------------------------------------------------------
		patientName			= 	PublicData.patientDetails.Name ();
		// -------------------------------------------------------------------------
		// 28/03/2019 ECU having initialised the details - want to check if there
		//                is some information from the stored device chain that
		//                needs to be copied across
		// -------------------------------------------------------------------------
		Devices localDevice = returnDeviceInformation (name);
		// -------------------------------------------------------------------------
		// 28/03/2019 ECU check if there is a stored entry
		// -------------------------------------------------------------------------
		if (localDevice != null)
		{
			// ---------------------------------------------------------------------
			// 28/03/2019 ECU copy across any stored information that is required
			// ---------------------------------------------------------------------
			name		 = localDevice.name;
			nameOriginal = localDevice.nameOriginal;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	boolean matchDeviceName (String theName)
	{
		// -------------------------------------------------------------------------
		// 29/03/2019 ECU only check if a 'compatible' device
		// -------------------------------------------------------------------------
		if (compatible)
		{
			// ---------------------------------------------------------------------
			// 29/03/2019 ECU check if the supplied name matches the 'original name'
			//                of this device
			// 18/11/2019 ECU Note - the check on 'null' is now not relevant but
			//                       leave in just in case an old device is in use
			// ---------------------------------------------------------------------
			if (theName.equals ((nameOriginal == null) ? name : nameOriginal))
			{
				// ---------------------------------------------------------
				// 28/03/2019 ECU indicate that a match has been found
				// ---------------------------------------------------------
				return true;
				// ---------------------------------------------------------
			}	
		}
		// ---------------------------------------------------------------------
		// 29/03/2019 ECU either this device is 'not compatible' or the supplied
		//                name does not match the 'original name'
		// ---------------------------------------------------------------------
		return false;
		// ---------------------------------------------------------------------
	}
	/* ============================================================================= */
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 06/09/2017 ECU created to print a summary of the current device
		// 27/04/2019 ECU for non-compatible devices check if there is a response
		//                - if there is then display it
		// -------------------------------------------------------------------------
		return "IP Address = " + IPAddress + 
				 ((compatible || (patientName != null))?
								("\nMAC Address = " + macAddress +		// 20/03/2015 ECU added
								 "\nName = " + name + 
		// -------------------------------------------------------------------------
		// 28/03/2019 ECU check if the name has been changed - if so then display
		//                the original name
		// 18/11/2019 ECU added the 'equalI...' check
		// -------------------------------------------------------------------------
								 (((nameOriginal == null) || name.equalsIgnoreCase(nameOriginal))
								 						? StaticData.BLANK_STRING
										                : "  (Originally : " + nameOriginal + ")") +
		// -------------------------------------------------------------------------
								 "\nPatient\'s Name = " + patientName + // 06/09/2017 ECU added
								 "\nSerial Number = " + serialNumber +	// 20/03/2015 ECU added
								 "\nAPI Level = " + apiLevel +			// 07/01/2016 ECU added
								 "\nPhone = " + phone + 
								 "\nRemote Controller = " + remoteController + 
						 												// 26/02/2016 ECU added
								 "\nBelkin WeMo Control = " + wemo +	// 18/03/2015 ECU added
								 "\nLast Response = " + response +
								 "\nCompatible = " + compatible) 
						     : ("\n" +
								 (!smartDevice ? "Non-compatible Device" : ("Smart Device\n" + response))));
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
	public static boolean returnCompatibility (String thePatientName)
	{
		// -------------------------------------------------------------------------
		// 06/09/2017 ECU created to indicate whether the device is 'compatible'
		//                depending on the specified patient name
		// -------------------------------------------------------------------------
		// 06/09/2017 ECU if no local patient details have been set then any incoming
		//                patient name in a device is to be deemed as 'not compatible'
		// -------------------------------------------------------------------------
		if (PublicData.patientDetails != null)
		{
			// ---------------------------------------------------------------------
			// 06/09/2017 ECU the local details of the patient hve been set so if
			//                the stored name matches that being specified then the
			//                device is compatible, otherwise the device is for another
			//                patient and is 'not compatible'
			// ---------------------------------------------------------------------
			return (PublicData.patientDetails.Name ().equalsIgnoreCase (thePatientName));
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 06/09/2017 ECU because there is no locally held patient details then
			//                other devices will be 'not compatible'
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public Devices returnDeviceInformation (String theName)
	{
		// -------------------------------------------------------------------------
		// 28/03/2019 ECU return the devices entry corresponding to the specified
		//                IP address and has the correct name
		// -------------------------------------------------------------------------
		if (PublicData.deviceDetails != null)
		{			
			for (int theDevice = 0; theDevice < PublicData.deviceDetails.size (); theDevice++)
			{
				// -----------------------------------------------------------------
				// 28/03/2019 ECU only interested in compatible devices
				// -----------------------------------------------------------------
				if (PublicData.deviceDetails.get (theDevice).matchDeviceName (theName))
				{
					// -------------------------------------------------------------
					// 29/03/2019 ECU a matching device has been found so return its
					//                details
					// -------------------------------------------------------------
					return PublicData.deviceDetails.get (theDevice);
					// -------------------------------------------------------------	
				}
			}
		}
		// -------------------------------------------------------------------------
		// 28/03/2019 ECU indicate that nothing found
		// -------------------------------------------------------------------------
		return null;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String returnIPAddress (String theFormattedAddress)
	{
		String [] theParts = theFormattedAddress.split("[(]");
		// -------------------------------------------------------------------------
		// 16/11/2019 ECU changed from 'replaceAll' to 'replace' because
		//                the former requires a REGEX so not sure why it ever
		//				  worked
		// -------------------------------------------------------------------------
		return theParts [1].replace (")",StaticData.BLANK_STRING).replace (StaticData.SPACE_STRING,StaticData.BLANK_STRING);
		// -------------------------------------------------------------------------
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
		String [] theParts = theFormattedAddress.split ("[(]");
		// -------------------------------------------------------------------------
		// 16/06/2016 ECU remove any leading/trailing spaces
		// -------------------------------------------------------------------------
		return theParts [0].trim ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void sendHelloMessage (int theDelay)
	{
		// -------------------------------------------------------------------------
		// 20/03/2015 ECU broadcast the 'hello' message to get all devices to request
		//                this device's details
		// 09/04/2016 ECU changed name from 'multicastMessage'
		// 25/05/2020 ECU changed to use new method for initiating the transmission
		//                of a broadcast message
		//            ECU added the delay argument
		// -------------------------------------------------------------------------
		BroadcastUtilities.sendBroadcastMessage (StaticData.BROADCAST_MESSAGE_HELLO,theDelay);
		// -------------------------------------------------------------------------
		// 22/03/2015 ECU put entry into project log to indicate the fact
		// -------------------------------------------------------------------------
		Utilities.LogToProjectFile ("Devices","send the hello message");
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void sendHelloMessage ()
	{
		// -------------------------------------------------------------------------
		// 25/05/2020 ECU send the 'hello' message immediately
		// -------------------------------------------------------------------------
		sendHelloMessage (0);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void setName (String theName)
	{
		// -------------------------------------------------------------------------
		// 28/03/2019 ECU created to change the name and, if not already set, then
		//                to remember the 'original name'
		// 18/11/2019 ECU Note - the null check should not occur now but just leave
		//                       for the time being
		// -------------------------------------------------------------------------
		if (nameOriginal == null)
		{
			// ---------------------------------------------------------------------
			// 28/03/2019 ECU the original name has never been set so store the current
			//                name
			// ---------------------------------------------------------------------
			nameOriginal = name;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 28/03/2019 ECU set the current name to that supplied
		// -------------------------------------------------------------------------
		name = theName;
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
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}
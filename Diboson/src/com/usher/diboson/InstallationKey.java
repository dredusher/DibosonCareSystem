package com.usher.diboson;

import java.io.Serializable;

public class InstallationKey implements Serializable
{
	// -----------------------------------------------------------------------------
	// 15/11/2014 ECU this class is used to hold the data that forms the installation
	//                key to indicate that the current installation is valid
	// -----------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;
	// -----------------------------------------------------------------------------
	long	date;						// date of the installation
	String	preset;						// a preset header
	String	serialNumber;				// serial number of the device
	// -----------------------------------------------------------------------------
	public String print ()
	{
		return "Installation Key\n" +
			   " Preset : " + preset + "\n" +
			   " Device : " + serialNumber + "\n" + 
			   " Date : " + PublicData.dateFormatter.format (date);
	}
	// -----------------------------------------------------------------------------
}

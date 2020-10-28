package com.usher.diboson;

import java.io.File;

import android.content.Context;
import android.content.Intent;

public class Installation 
{
	// -----------------------------------------------------------------------------
	// 15/11/2014 ECU declare any variables that are required by this class
	// -----------------------------------------------------------------------------
	static	InstallationKey		installationKey;
	// -----------------------------------------------------------------------------
	
	// -----------------------------------------------------------------------------
	public static boolean check (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 15/11/2014 ECU this method will check whether the current installation
		//                is valid for this device
		// -------------------------------------------------------------------------
		String keyFileName = PublicData.projectFolder + 
									theContext.getString (R.string.InstallationFile);
		// -------------------------------------------------------------------------
		// 15/11/2014 ECU want to first get the key from disk
		// -------------------------------------------------------------------------
		installationKey = (InstallationKey) readFile (keyFileName);
		// -------------------------------------------------------------------------
		// 15/11/2014 ECU check whether the file exists
		// -------------------------------------------------------------------------
		if (installationKey != null)
		{
			// ---------------------------------------------------------------------
			// 15/11/2014 ECU file exists so do other tests
			// ---------------------------------------------------------------------
			// 15/11/2014 ECU check the preset
			// ---------------------------------------------------------------------
			if (!installationKey.preset.equals (theContext.getString(R.string.app_name)))
			{
				// -----------------------------------------------------------------
				// 15/11/2014 ECU the preset is wrong
				// -----------------------------------------------------------------
				Utilities.popToast ("Preset data is wrong");
				// -----------------------------------------------------------------
				return false;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 15/11/2014 ECU check if serial number is correct
			// ---------------------------------------------------------------------
			if (!installationKey.serialNumber.equals(android.os.Build.SERIAL))
			{
				// -----------------------------------------------------------------
				// 15/11/2014 ECU the serial number is wrong
				// -----------------------------------------------------------------
				Utilities.popToast ("Serial number is wrong");
				// -----------------------------------------------------------------
				return false;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 15/11/2014 ECU check the date of the file against the stored entry
			// 01/09/2015 ECU changed to use StaticData
			// ---------------------------------------------------------------------
			File file = new File (keyFileName);
			if ((file.lastModified() / StaticData.MILLISECONDS_PER_HOUR) !=
					(installationKey.date / StaticData.MILLISECONDS_PER_HOUR))
			{
				Utilities.popToast ("Stored date of installation is inconsistent");
				return false;
			}
			// ---------------------------------------------------------------------
			// 15/11/2014 ECU everything seems to be OK
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 15/11/2014 ECU file does not exist so check fails
			//            ECU want to start the activity which will request
			//                the activation key
			// ---------------------------------------------------------------------
	    	Intent localIntent = new Intent (theContext,ActivationActivity.class);
			theContext.startActivity (localIntent);
	    	// -----------------------------------------------------------------
			return true;
		}
	}
	// =============================================================================
	public static void delete (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 19/07/2015 ECU created to delete the file that holds the installation
		//                key
		// -------------------------------------------------------------------------
		try
		{
			(new File (PublicData.projectFolder + theContext.getString (R.string.InstallationFile))).delete();
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 19/07/2015 ECU just catch any problems that occur
			// ---------------------------------------------------------------------
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void initialise (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 15/11/2014 ECU this method will create the installation key for the
		//                current device
		// -------------------------------------------------------------------------
		installationKey = new InstallationKey ();
		// -------------------------------------------------------------------------
		// 15/11/2014 ECU get the current time which will be used for date 
		// -------------------------------------------------------------------------
		installationKey.date = (Utilities.getAdjustedTime(true));
		// -------------------------------------------------------------------------
		// 15/11/2014 ECU create a preset for the key
		// -------------------------------------------------------------------------
		installationKey.preset = theContext.getString (R.string.app_name);
		// -------------------------------------------------------------------------
		// 15/11/2014 ECU get the serial number of this device
		// -------------------------------------------------------------------------
		installationKey.serialNumber = android.os.Build.SERIAL;
		// -------------------------------------------------------------------------
		// 15/11/2014 ECU write the key to disk
		// -------------------------------------------------------------------------
		writeFile (PublicData.projectFolder + 
				theContext.getString (R.string.InstallationFile));
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static InstallationKey readFile (String theFileName)
	{
		//--------------------------------------------------------------------------
		// 15/11/2014 ECU this method will read the InstallationKey from the
		//                relevant file which is in the project folder
		// -------------------------------------------------------------------------
		return (InstallationKey) Utilities.readObjectFromDisk (theFileName);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void writeFile (String theFileName)
	{
		//--------------------------------------------------------------------------
		// 15/11/2014 ECU this method will write the InstallationKey to the
		//                relevant file which is in the project file
		// -------------------------------------------------------------------------
		Utilities.writeObjectToDisk (theFileName,installationKey);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
}

package com.usher.diboson;

import java.io.File;
import java.lang.reflect.Method;


public class FileToSynchronise 
{
	/* ============================================================================= */
	// 02/02/2014 ECU created to contain details of a carer
	// 18/12/2018 ECU added the method to be called when a file that is in the
	//                'synchronised' list has been received
	// 25/10/2016 ECU added the 'forceSynchronise' to force the synchronise irrespective
	//                of whether the modification time has changed
	/* ============================================================================= */
	public String	fileName;
	public boolean	forceSynchronise;		// 25/10/2016 ECU added
	public long		lastModified = 0;
	public Method	methodOnSynchronise;	// 18/12/2015 ECU added
	public int		resourceID;				// 04/02/2013 ECU added
	/* ============================================================================= */
	public FileToSynchronise (String theFileName,int theResourceID,Method theMethodOnSynchronise)
	{
		// -------------------------------------------------------------------------
		// 18/12/2015 ECU constructor changed to define the method on synchronise
		// -------------------------------------------------------------------------
		fileName 	= theFileName;
		resourceID	= theResourceID;		// 04/02/2014 ECU added
		// -------------------------------------------------------------------------
		// 02/02/2014 ECU get additional details to store
		// -------------------------------------------------------------------------
		File localFile = new File (theFileName);
		// -------------------------------------------------------------------------
		if (localFile != null)
			lastModified = localFile.lastModified();
		// -------------------------------------------------------------------------
		// 18/12/2015 ECU store the method to be actioned on receipt
		// -------------------------------------------------------------------------
		methodOnSynchronise	= theMethodOnSynchronise;
		// -------------------------------------------------------------------------
		// 25/10/2016 ECU preset the force flag
		// -------------------------------------------------------------------------
		forceSynchronise = false;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public FileToSynchronise (String theFileName,int theResourceID)
	{
		// -------------------------------------------------------------------------
		// 18/12/2015 ECU old constructor when no method needed
		// -------------------------------------------------------------------------
		this (theFileName,theResourceID,null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public FileToSynchronise (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 25/10/2016 ECU constructor added when the force of a synchronise wanted
		//                without checking the modification details
		// -------------------------------------------------------------------------
		fileName 	= theFileName;
		resourceID	= StaticData.NO_RESULT;

		File localFile = new File (theFileName);
		// -------------------------------------------------------------------------
		if (localFile != null)
			lastModified = localFile.lastModified();
		// -------------------------------------------------------------------------
		// 18/12/2015 ECU store the method to be actioned on receipt
		// -------------------------------------------------------------------------
		methodOnSynchronise	= null;
		// -------------------------------------------------------------------------
		// 25/10/2016 ECU want to force a synchronise
		// -------------------------------------------------------------------------
		forceSynchronise = true;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void Add (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 25/10/2016 ECU created to force the synchronisation of the specified
		//                file - if it is not already in the list
		// 26/10/2016 ECU moved here from Utilities and recoded
		// -------------------------------------------------------------------------
		// 26/10/2016 ECU check if the 'file name' is in the list and if not then add
		//                it
		// -------------------------------------------------------------------------
		if (PublicData.filesToSynchronise.size() > 0)
		{
			for (int theIndex = 0; theIndex < PublicData.filesToSynchronise.size(); theIndex++)
			{
				if (PublicData.filesToSynchronise.get (theIndex).fileName.equalsIgnoreCase(theFileName))
				{
					// -------------------------------------------------------------
					// 25/10/2016 ECU the file is already in the list so just exit
					// -------------------------------------------------------------
					return;
					// -------------------------------------------------------------
				}
			}
		}
		// -------------------------------------------------------------------------
		// 25/10/2016 ECU the file is not in the list so add it and this will cause 
		//                a 'force' when synchronisation next occurs
		// -------------------------------------------------------------------------
		PublicData.filesToSynchronise.add (new FileToSynchronise (theFileName));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	/* ============================================================================= */
	public String Print ()
	{
		return "File : " + fileName + " " + PublicData.dateFormatterFull.format (lastModified);
	}
	/* ============================================================================= */
}

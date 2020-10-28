package com.usher.diboson;

public class FileOnDisk 
{
	/* ================================================================= */
	// 03/02/2014 ECU created to handle files stored on disk
	/* ================================================================= */
	public int 		resourceID;
	public boolean 	synchroniseFlag;
	/* ================================================================= */
	public FileOnDisk (int theResourceID,boolean theSyncFlag)
	{
		resourceID		= theResourceID;
		synchroniseFlag	= theSyncFlag;
	}
	/* ================================================================= */
}

package com.usher.diboson;

import java.io.File;
import java.io.Serializable;

public class FileTransferBlock implements Serializable
{
	// =============================================================================
	// 06/01/2016 ECU created for use when transfer blocks during file transfer
	// 10/01/2016 ECU added the 'allFiles' flag
	// 11/01/2016 ECU added the 'sizeOfBuffer'
	//            ECU change to use 'buffer' and 'fileDetails'
	// 14/01/2016 ECU added the project folder as a variable
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	boolean	allFiles			= true;
	byte [] buffer				= null;
	int		counter				= 0;
	File	fileDetails			= null;
	int		numberOfFiles		= 0;					// 18/01/2016 ECU added
	String	projectFolder		= null;
	String	serverAddress		= null;
	int		sizeOfBuffer		= 0;
	int		type				= 0;
	// =============================================================================
	
	// =============================================================================
	public FileTransferBlock (String theServerAddress,int theSizeOfBuffer,boolean theAllFilesFlag)
	{
		// -------------------------------------------------------------------------
		// 11/01/2016 ECU created to initialise the internal variables
		// -------------------------------------------------------------------------
		allFiles     	= theAllFilesFlag;
		buffer       	= new byte [theSizeOfBuffer];
		fileDetails 	= null;
		serverAddress	= theServerAddress;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public FileTransferBlock (String theServerAddress,boolean theAllFilesFlag)
	{
		// -------------------------------------------------------------------------
		// 18/01/2016 ECU created to initialise the internal variables but as this
		//                will be used for the control block then set the buffer to
		//                'null'
		// -------------------------------------------------------------------------
		allFiles     	= theAllFilesFlag;
		buffer       	= null;
		fileDetails 	= null;
		serverAddress	= theServerAddress;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// =============================================================================
	
	// =============================================================================
	String Print (String theTitle)
	{
		// -------------------------------------------------------------------------
		// 11/01/2016 ECU created to return a summary of the block
		// -------------------------------------------------------------------------
		return theTitle + StaticData.NEWLINE +
			   "Type : " + type + StaticData.NEWLINE +
			   "All Files Flag : "  + allFiles + StaticData.NEWLINE +
			   "Counter : " + counter + StaticData.NEWLINE +
			   ((fileDetails != null) ? "File : " + fileDetails.getName() + StaticData.NEWLINE : StaticData.BLANK_STRING) +
			   "Project Folder : " + projectFolder + StaticData.NEWLINE +
			   "Size of Buffer : " + sizeOfBuffer;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

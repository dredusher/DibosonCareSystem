package com.usher.diboson;

import java.io.File;
import java.io.Serializable;

public class FileDetails implements Serializable
{
	/* ========================================================================== */
	// 02/02/2014 ECU created - when passing a File object between devices it
	//                may be that the projectFolder is in a different place
	/* ========================================================================== */
	private static final long serialVersionUID = 1L;
	/* ========================================================================== */
	public String	fileName;
	public long		fileSize;							// 07/04/2014 ECU added
	public long		lastModified;
	public byte []  fileContents;
	/* ========================================================================== */
	public FileDetails (String theProjectFolder,File theFile)
	{
		fileName		=   theFile.getPath().replaceAll (theProjectFolder,"");
		fileSize		=   theFile.length();			// 07/04/2014 ECU added
		lastModified 	= 	theFile.lastModified();
	}
	/* ========================================================================== */
	public String GetFileName ()
	{
		// ----------------------------------------------------------------------
		// 02/02/2014 ECU return the full path of the file which is assumed to be in
		//                the project folder
		// ----------------------------------------------------------------------
		return PublicData.projectFolder + fileName;
	}
	/* ========================================================================== */
	public String Print ()
	{
		return "File Name " + GetFileName ();
	}
	// =============================================================================
}

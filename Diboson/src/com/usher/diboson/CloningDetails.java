package com.usher.diboson;

import java.io.Serializable;

public class CloningDetails implements Serializable 
{
	// =============================================================================
	// 09/01/2015 ECU created to declare variables associated
	//                with cloning
	//
	//					filesFlag  		= true	clone all files
	//                             		= false  clone only new files
	//                  numberOfFiles	= the number of files to
	//                                    be cloned - the actual
	//                                    number can be less if
	//                                    filesFlag = false
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	public boolean	filesFlag;			
	public int		numberOfFiles;	    
	// =============================================================================
	public CloningDetails (boolean theFilesFlag,int theNumberOfFiles)
	{
		filesFlag		= 	theFilesFlag;
		numberOfFiles	=	theNumberOfFiles;
	}
	// =============================================================================
	public String Print ()
	{
		return (filesFlag ? ("All of the " + numberOfFiles + " files will be cloned") :
				            ("Of the " + numberOfFiles + " files only those which do not exist will be cloned")); 
	}
	// =============================================================================
}

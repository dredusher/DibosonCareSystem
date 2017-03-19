package com.usher.diboson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class FileOptions implements Comparable<FileOptions>
{
	/* =============================================================================== */
	private File	details;  
	private boolean	parent;
	/* =============================================================================== */
	public FileOptions(File theDetails)  
	{  
		details = theDetails;
		parent  = false;
	}  
	/* =============================================================================== */
	public FileOptions(File theDetails, boolean theFlag)  
	{  
		details = theDetails;
		parent  = theFlag;
	}  
	/* =============================================================================== */
	public String getData ()
	{
		if (parent)
			return "parent";
		else
		if (details.isDirectory())
			return "folder";
		else
			return "File : " + details.length() + " bytes";
	}
	// =============================================================================
	public String getFullFileName ()
	{
		// -------------------------------------------------------------------------
		// 31/10/2015 ECU created to return the full file name
		// -------------------------------------------------------------------------
		return details.getAbsolutePath();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String getModificationTime ()
	{
		// -------------------------------------------------------------------------
		// 10/11/2014 ECU changed to use Locale.getDefault instead of Locale.UK
		// -------------------------------------------------------------------------
		SimpleDateFormat dateFormat = new SimpleDateFormat ("EEEE dd MMM yyyy",Locale.getDefault());
		SimpleDateFormat timeFormat = new SimpleDateFormat ("HH:mm:ss   ",Locale.getDefault());
		
		return timeFormat.format (details.lastModified()) + dateFormat.format (details.lastModified());
	}
	/* =============================================================================== */
	public String getName()  
	{ 
		if (parent)
			return ("..");
		else
			return details.getName();  
	} 
	/* =============================================================================== */
	public String getPath()  
	{  
		return details.getPath();  
	}
	/* =============================================================================== */ 
	@Override  
	public int compareTo (FileOptions theFileOptions) 
	{  
		if(this.getName() != null)  
			return this.getName().toLowerCase().compareTo(theFileOptions.getName().toLowerCase());   
		else  
			throw new IllegalArgumentException();  
	}  
	/* =============================================================================== */
	public boolean isDirectory ()
	{
		return details.isDirectory();
	}
	/* =============================================================================== */
	public boolean isParent ()
	{
		return parent;
	}
	/* =============================================================================== */
	public String Print ()
	{
		return this.getName() + "\n" + this.getPath() + "\n" + this.getModificationTime();
	}
	/* =============================================================================== */
} 

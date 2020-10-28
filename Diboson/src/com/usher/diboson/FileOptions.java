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
	public FileOptions (File theDetails)  
	{  
		details = theDetails;
		parent  = false;
	}  
	/* =============================================================================== */
	public FileOptions (File theDetails, boolean theParentFlag)  
	{  
		details = theDetails;
		parent  = theParentFlag;
	}  
	/* =============================================================================== */
	public String getData ()
	{
		// --------------------------------------------------------------------------
		// 09/10/2017 ECU Note - check if this entry is a parent, folder, ....
		//            ECU add the check whether the contents are readable or not
		// --------------------------------------------------------------------------
		if (parent)
			return "parent"  + (details.canRead() ? StaticData.BLANK_STRING : " (contents not readable)");
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
		return details.getAbsolutePath ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String getModificationTime ()
	{
		// -------------------------------------------------------------------------
		// 10/11/2014 ECU changed to use Locale.getDefault instead of Locale.UK
		// 24/07/2017 ECU changed to use ALARM
		// -------------------------------------------------------------------------
		SimpleDateFormat dateFormat = new SimpleDateFormat ("EEEE dd MMM yyyy",Locale.getDefault());
		SimpleDateFormat timeFormat = new SimpleDateFormat (StaticData.ALARM_TIME_FORMAT + "   ",Locale.getDefault());
		// -------------------------------------------------------------------------
		return timeFormat.format (details.lastModified()) + dateFormat.format (details.lastModified());
		// ---------------------------------------------------------------------------
	}
	/* =============================================================================== */
	public String getName ()  
	{ 
		// ---------------------------------------------------------------------------
		if (parent)
			return ("..");
		else
			return details.getName(); 
		// ---------------------------------------------------------------------------
	} 
	/* =============================================================================== */
	public String getPath()  
	{  
		return details.getPath();  
	}
	// ===============================================================================
	@Override  
	public int compareTo (FileOptions theFileOptions) 
	{  
		if(this.getName() != null)  
			return this.getName().toLowerCase().compareTo(theFileOptions.getName().toLowerCase());   
		else  
			throw new IllegalArgumentException ();  
	}  
	/* =============================================================================== */
	public boolean isDirectory ()
	{
		return details.isDirectory ();
	}
	/* =============================================================================== */
	public boolean isParent ()
	{
		return parent;
	}
	/* =============================================================================== */
	public String Print ()
	{
		return this.getName() + StaticData.NEWLINE + this.getPath() + StaticData.NEWLINE + this.getModificationTime();
	}
	/* =============================================================================== */
} 

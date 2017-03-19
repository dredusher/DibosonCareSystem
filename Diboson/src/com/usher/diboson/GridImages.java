package com.usher.diboson;

import java.io.Serializable;
import java.lang.reflect.Method;

public class GridImages <T> implements Serializable,Comparable <GridImages <T>>
{
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	// 19/01/2014 ECU created
	// 18/01/2015 ECU changed to be serializable
	// =============================================================================
	public int		imageId;		// the R.drawable number
	public String	legend;			// 26/01/2014 ECU legend to display
	public boolean  longPress;		// 27/01/2015 ECU added to indicate that entry
									//                can be long pressed
	public boolean	mode;			// true  = development and normal mode
									// false = normal mode only
	public int		usage;			// 18/01/2015 ECU added - the number of calls
	public Class <T>validationClass;
	public String	validationMethod;
									// 09/03/2015 ECU added - the method to provide validation
	// =============================================================================
	public GridImages (int theImageId,
					   String theLegend,
					   boolean theMode,
					   boolean theLongPress,
					   Class <T> theValidationClass,
					   String theValidationMethod)
	{
		imageId		= theImageId;
		legend		= theLegend;	// 26/01/2014 ECU added
		longPress	= theLongPress;	// 27/01/2015 ECU added
		mode		= theMode;
		// -------------------------------------------------------------------------
		// 18/01/2015 ECU initialise the number of times this image has been called
		// -------------------------------------------------------------------------
		usage       = 0;
		// -------------------------------------------------------------------------
		// 09/03/2015 ECU get the Method that will be used for validation
		// -------------------------------------------------------------------------
		validationClass		= theValidationClass;
		validationMethod 	= theValidationMethod;
	}
	// -----------------------------------------------------------------------------
	public GridImages (int theImageId,String theLegend,boolean theMode,boolean theLongPress)
	{
		// -------------------------------------------------------------------------
		// 09/03/2015 ECU call the new master constructor
		// -------------------------------------------------------------------------
		this (theImageId,theLegend,theMode,theLongPress,null,null);
	}
	// -----------------------------------------------------------------------------
	public GridImages (int theImageId,String theLegend,boolean theMode)
	{
		// -------------------------------------------------------------------------
		// 27/01/2015 ECU changed to call modified method above
		// -------------------------------------------------------------------------
		this (theImageId,theLegend,theMode,false,null,null);
	}
	// =============================================================================
	public int compareTo (GridImages<T> gridImage) 
	{
		// -------------------------------------------------------------------------
		// 18/01/2015 ECU use to compare entries
		// -------------------------------------------------------------------------
		int compareUsage = ((GridImages <T>) gridImage).GetUsage(); 
		// -------------------------------------------------------------------------
		//ascending order
		//return this.usage - compareUsage;
		// -------------------------------------------------------------------------
		//descending order
		// -------------------------------------------------------------------------
		return compareUsage - this.usage;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public int GetUsage ()
	{
		// -------------------------------------------------------------------------
		// 18/01/2015 ECU returns the stored usage of an identified image
		// -------------------------------------------------------------------------
		return usage;
	}
	// =============================================================================
	String Print ()
	{
		// -------------------------------------------------------------------------
		// 10/02/2014 ECU print details of the object
		// -------------------------------------------------------------------------
		return String.format ("%30s",legend) + " Usage = " + usage;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public boolean Validate ()
	{
		// -------------------------------------------------------------------------
		// 09/03/2015 ECU called to use the method to determine if this entry is 
		//                valid. If the method is null then return a true
		// -------------------------------------------------------------------------
		try 
		{
			if (validationMethod != null)
			{
				Method localValidationMethod = Utilities.createAMethod (validationClass,validationMethod,0);
				return ((Boolean) localValidationMethod.invoke(null,new Object [] {0}));
			}
			else
				return true;
		} 
		catch (Exception theException) 
		{
			theException.printStackTrace();
			// ---------------------------------------------------------------------
			// 09/03/2015 ECU error occurred so indicate 'valid'
			// ---------------------------------------------------------------------
			return true;
		} 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

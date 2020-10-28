package com.usher.diboson;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import android.content.Context;

public class GridImages <T> implements Serializable,Comparable <GridImages <T>>
{
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	// 19/01/2014 ECU created
	// 18/01/2015 ECU changed to be serializable
	// 01/04/2017 ECU changed so that the resource ID of the legend is stored rather
	//                than the literal string
	//            ECU added the Legend () method to return the string corresponding
	//                to the stored legendId
	// 19/07/2017 ECU added the 'legendLongId' which is the text that can be optionally
	//                displayed to show what the long press does. If set to
	//                StaticData.NO_RESULT then no legend will be displayed
	// 07/02/2018 ECU added methods for handling requests for information on
	//                'long press' processing
	// =============================================================================
	public int		imageId;		// the R.drawable number
	public int		legendId;		// 26/01/2014 ECU legend to display
									// 01/04/2017 ECU changed from String
	public int		legendLongId;	// 19/07/2017 ECU legend to display - long press
	public boolean  longPress;		// 27/01/2015 ECU added to indicate that entry
									//                can be long pressed
	public boolean	mode;			// true  = development and normal mode
									// false = normal mode only
	public int		usage;			// 18/01/2015 ECU added - the number of calls
	// -----------------------------------------------------------------------------
	// 19/07/2017 ECU Note the following 'validation..' variables will be used to
	//                     define the method that will be invoked to perform the
	//                     validation that is required. Done this way because want
	//                     this object to be serializable so that it can be stored
	//                     to disk
	// -----------------------------------------------------------------------------
	public Class	<T>validationClass;
	public String	validationMethod;
									// 09/03/2015 ECU added - the method to provide validation
	// =============================================================================
	public GridImages (int 			theImageId,
					   int			theLegendId,
					   boolean 		theMode,
					   boolean 		theLongPress,
					   int			theLegendLongId,
					   Class <T> 	theValidationClass,
					   String		theValidationMethod)
	{
		// -------------------------------------------------------------------------
		// 01/04/2017 ECU changed to use the resource ID of the legend rather than
		//                the literal string
		// 19/07/2017 ECU added the setting of 'legendLongId'
		// -------------------------------------------------------------------------
		imageId			= theImageId;
		legendId		= theLegendId;	// 26/01/2014 ECU added
		legendLongId	= theLegendLongId;
		longPress		= theLongPress;	// 27/01/2015 ECU added
		mode			= theMode;
		// -------------------------------------------------------------------------
		// 18/01/2015 ECU initialise the number of times this image has been called
		// -------------------------------------------------------------------------
		usage       	= 0;
		// -------------------------------------------------------------------------
		// 09/03/2015 ECU get the Method that will be used for validation
		// -------------------------------------------------------------------------
		validationClass		= theValidationClass;
		validationMethod 	= theValidationMethod;
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	public GridImages (int 			theImageId,
			   		   int			theLegendId,
			   		   boolean 		theMode,
			   		   boolean 		theLongPress,
			   		   Class <T> 	theValidationClass,
			   		   String		theValidationMethod)
	{
		// -------------------------------------------------------------------------
		// 19/07/2017 ECU this used to be the main constructor until the 'legendLongId'
		//                variable was added
		// -------------------------------------------------------------------------
		this (theImageId,theLegendId,theMode,theLongPress,StaticData.NO_RESULT,theValidationClass,theValidationMethod);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	public GridImages (int theImageId,int theLegendId,boolean theMode,boolean theLongPress)
	{
		// -------------------------------------------------------------------------
		// 09/03/2015 ECU call the new master constructor
		// -------------------------------------------------------------------------
		this (theImageId,theLegendId,theMode,theLongPress,null,null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	public GridImages (int theImageId,int theLegendId,boolean theMode,boolean theLongPress,int theLegendLongId)
	{
		// -------------------------------------------------------------------------
		// 19/07/2017 ECU created to handle when the 'legendLongId' is to be set
		// 09/03/2015 ECU call the new master constructor
		// -------------------------------------------------------------------------
		this (theImageId,theLegendId,theMode,theLongPress,theLegendLongId,null,null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	public GridImages (int theImageId,int theLegendId,boolean theMode)
	{
		// -------------------------------------------------------------------------
		// 27/01/2015 ECU changed to call modified method above
		// -------------------------------------------------------------------------
		this (theImageId,theLegendId,theMode,false,null,null);
		// -------------------------------------------------------------------------
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
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static int GetUsage (int theImageId)
	{
		// -------------------------------------------------------------------------
		// 28/12/2018 ECU created to return the usage of the entry which has the
		//                image ID within it
		// -------------------------------------------------------------------------
		for (int theEntry = 0; theEntry < GridActivity.gridImages.length; theEntry++)
		{
			// ---------------------------------------------------------------------
			// 28/12/2018 ECU check if this is the required entry
			// ---------------------------------------------------------------------
			if (GridActivity.gridImages[theEntry].imageId == theImageId)
			{
				// -----------------------------------------------------------------
				// 28/12/2018 ECU the required entry has been foound so return its
				//                usage
				// -----------------------------------------------------------------
				return GridActivity.gridImages[theEntry].usage;
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 28/12/2018 ECU nothing was found which matches so indicate this fact
		// 02/01/2019 ECU changed from NO_RESULT
		// -------------------------------------------------------------------------
		return 0;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Legend ()
	{
		// -------------------------------------------------------------------------
		// 19/04/2017 ECU created so that methods which do not have easy access to
		//                the context can make use of ..activity. But found a situation
		//                where could be called with a 'null' value - hence the use of
		//                the try/catch.
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 19/04/2017 ECU return the associated resource string
			// ---------------------------------------------------------------------
			return MainActivity.activity.getString (legendId);
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 19/04/2017 ECU appears the ...activity may be null so just return
			//                an empty string
			// ---------------------------------------------------------------------
			return StaticData.BLANK_STRING;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public String Legend (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 01/04/2017 ECU created to return the string legend of the object
		// 17/04/2017 ECU got an NPE - suggesting that ...activity has not been set
		//                correctly so add a try/catch and return a blank string if
		//                an exception occurs
		// 18/04/2017 ECU because of the issues of 17/04/2017 then decided to pass
		//                through the context as an argument
		// 19/04/2017 ECU remove the try/catch
		// -------------------------------------------------------------------------
		return theContext.getString (legendId);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String LegendLong (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 19/07/2017 ECU created to return the 'long press' legend
		//            ECU NOTE - the comments against Legend () are relevant
		//            ECU need to check if the ID has been set
		// -------------------------------------------------------------------------
		if (legendLongId != StaticData.NO_RESULT)
		{
			// ---------------------------------------------------------------------
			// 19/07/2017 ECU a valid ID has been specified so get the relevant 
			//                resource string
			// ---------------------------------------------------------------------
			return theContext.getString (legendLongId);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 19/07/2017 ECU no legend for a 'long press' is required
			// ---------------------------------------------------------------------
			return null;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String LegendLong ()
	{
		// -------------------------------------------------------------------------
		// 07/02/2018 ECU created so that methods which do not have easy access to
		//                the context can make use of ..activity. But found a situation
		//                where could be called with a 'null' value - hence the use of
		//                the try/catch.
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 07/02/2018 ECU return the associated resource string
			// ---------------------------------------------------------------------
			return MainActivity.activity.getString (legendLongId);
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 07/02/2018 ECU appears the ...activity may be null so just return
			//                an empty string
			// ---------------------------------------------------------------------
			return StaticData.BLANK_STRING;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	String Print (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 10/02/2014 ECU print details of the object
		// 01/04/2017 ECU changed to use the legend ID
		// 17/04/2017 ECU got an NPE - suggesting that ...activity has not been set
		//                correctly so add a try/catch and return a blank string if
		//                an exception occurs
		// 18/04/2017 ECU because of the issues of 17/04/2017 then decided to pass
		//                through the context as an argument
		// 19/04/2017 ECU remove try/catch
		// 03/03/2019 ECU changed the format
		// -------------------------------------------------------------------------
		return String.format ("%-25s%10d",theContext.getString (legendId),usage);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String PrintAll (Context theContext,String theTitle,boolean theSortFlag)
	{
		// -------------------------------------------------------------------------
		// 26/12/2018 ECU created to return the contents of all of the stored items
		// 27/12/2018 ECU add the title
		// 04/03/2019 ECU add the sort flag
		//            ECU because there is a need to manipulate the source array then
		//                copy the data into a local array
		// -------------------------------------------------------------------------
		GridImages <?> [] localGridImages = Arrays.copyOf (GridActivity.gridImages,GridActivity.gridImages.length);
		// -------------------------------------------------------------------------
		// 04/03/2019 ECU check if the data needs to be sorted by usage before the
		//                data is sorted
		// -------------------------------------------------------------------------
		if (theSortFlag)
		{
			// ---------------------------------------------------------------------
			// 04/03/2019 ECU the data needs sorting by 'usage'
			// ---------------------------------------------------------------------
			Arrays.sort (localGridImages);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 04/03/2019 ECU now display the resultant data
		// -------------------------------------------------------------------------
		String localResultString = theTitle + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 26/12/2018 ECU look through all entries in the array
		// 04/03/2019 ECU changed to use the local copy of the data
		// -------------------------------------------------------------------------
		for (int theItem = 0; theItem < localGridImages.length; theItem++)
		{
			// ---------------------------------------------------------------------
			// 26/12/2018 ECU add details of each item into the list
			// ---------------------------------------------------------------------
			localResultString += localGridImages [theItem].Print (theContext) + StaticData.NEWLINE;
			// ---------------------------------------------------------------------	
		}
		// -------------------------------------------------------------------------
		// 26/12/2018 ECU return the generated string
		// -------------------------------------------------------------------------
		return localResultString;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String [] returnLegends (@SuppressWarnings("rawtypes") GridImages [] theGridImages)
	{
		// -------------------------------------------------------------------------
		// 14/06/2017 ECU created to return an array of legends associated with the
		//                grid images
		// -------------------------------------------------------------------------
		if (theGridImages != null && theGridImages.length != 0)
		{
			// ---------------------------------------------------------------------
			// 14/06/2017 ECU declare the array that will contain the legends
			// ---------------------------------------------------------------------
			String [] legends = new String [theGridImages.length];
			// ---------------------------------------------------------------------
			// 14/06/2017 ECU now loop through the grid images - populating the
			//                legend array
			// ---------------------------------------------------------------------
			for (int theIndex = 0; theIndex < theGridImages.length; theIndex++)
			{
				legends [theIndex] = theGridImages [theIndex].Legend();
			}
			// ---------------------------------------------------------------------
			// 14/06/2017 ECU return the generated list
			// ---------------------------------------------------------------------
			return legends;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 14/06/2017 ECU there is nothing to process so indicate that to the 
			//                caller
			// ---------------------------------------------------------------------
			return null;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String [] returnLegendsLong (@SuppressWarnings("rawtypes") GridImages [] theGridImages)
	{
		// -------------------------------------------------------------------------
		// 07/02/2018 ECU created to return an array of legends associated with the
		//                grid images when the long press is used
		// -------------------------------------------------------------------------
		if (theGridImages != null && theGridImages.length != 0)
		{
			// ---------------------------------------------------------------------
			// 07/02/2018 ECU a working string
			// ---------------------------------------------------------------------
			String legends = StaticData.BLANK_STRING;
			// ---------------------------------------------------------------------
			// 07/02/2018 ECU now loop through the grid images - build up the legends
			//                string
			// ---------------------------------------------------------------------
			for (int theIndex = 0; theIndex < theGridImages.length; theIndex++)
			{
				// -----------------------------------------------------------------
				// 07/02/2018 ECU check if this entry has a 'long press' option
				// -----------------------------------------------------------------
				if (theGridImages [theIndex].longPress)
				{
					// -------------------------------------------------------------
					// 07/02/2018 ECU this entry has a 'long press' option so add it to
					//                string and use NEWLINE as a delimiter
					// -------------------------------------------------------------
					legends += theGridImages [theIndex].LegendLong () + StaticData.NEWLINE;
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 07/02/2018 ECU want to return a string array based on the legends string
			// ---------------------------------------------------------------------
			return legends.split (StaticData.NEWLINE);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 14/06/2017 ECU there is nothing to process so indicate that to the 
			//                caller
			// ---------------------------------------------------------------------
			return null;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static int returnPosition (@SuppressWarnings("rawtypes") GridImages [] theGridImages,String theLegend)
	{
		// -------------------------------------------------------------------------
		// 14/06/2017 ECU created to return the position in the array which
		//                corresponds to the specified legend
		// -------------------------------------------------------------------------
		if (theGridImages != null && theGridImages.length != 0)
		{
			// ---------------------------------------------------------------------
			// 14/06/2017 ECU now loop through the grid images - populating the
			//                legend array
			// ---------------------------------------------------------------------
			for (int theIndex = 0; theIndex < theGridImages.length; theIndex++)
			{
				// -----------------------------------------------------------------
				// 14/06/2017 ECU check if this entry matches the supplied legend
				// -----------------------------------------------------------------
				if (theGridImages [theIndex].Legend().equals (theLegend))
				{
					// -------------------------------------------------------------
					// 14/06/2017 ECU the legend matches an entry so return the index
					// -------------------------------------------------------------
					return theIndex;
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
			// 14/06/2017 ECU nothing in the array matched the specified legend
			// ---------------------------------------------------------------------
			return StaticData.NO_RESULT;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 14/06/2017 ECU there is nothing to match against
			// ---------------------------------------------------------------------
			return StaticData.NO_RESULT;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static int returnPositionLong (@SuppressWarnings("rawtypes") GridImages [] theGridImages,String theLegend)
	{
		// -------------------------------------------------------------------------
		// 07/02/2018 ECU created to return the position in the array which
		//                corresponds to the specified legend (long press)
		// -------------------------------------------------------------------------
		if (theGridImages != null && theGridImages.length != 0)
		{
			// ---------------------------------------------------------------------
			// 07/02/2018 ECU now loop through the grid images - populating the
			//                legend array
			// ---------------------------------------------------------------------
			for (int theIndex = 0; theIndex < theGridImages.length; theIndex++)
			{
				// -----------------------------------------------------------------
				// 08/02/2018 ECU only interested in entries which have a 'long press'
				//                option
				// -----------------------------------------------------------------
				if (theGridImages [theIndex].longPress)
				{
					// -------------------------------------------------------------
					// 08/02/2018 ECU this entry has a 'long press' option
					// -------------------------------------------------------------
					// 07/02/2018 ECU check if this entry matches the supplied legend
					// -------------------------------------------------------------
					if (theGridImages [theIndex].LegendLong().equals (theLegend))
					{
						// ---------------------------------------------------------
						// 07/02/2018 ECU the legend matches an entry so return the 
						//                index
						// ---------------------------------------------------------
						return theIndex;
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 07/02/2018 ECU nothing in the array matched the specified legend
			// ---------------------------------------------------------------------
			return StaticData.NO_RESULT;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 07/02/2018 ECU there is nothing to match against
			// ---------------------------------------------------------------------
			return StaticData.NO_RESULT;
			// ---------------------------------------------------------------------
		}
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

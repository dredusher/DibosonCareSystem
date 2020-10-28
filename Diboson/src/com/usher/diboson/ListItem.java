package com.usher.diboson;

import java.io.Serializable;

public class ListItem implements Serializable,Comparable<ListItem>
{
	/* ========================================================================== */
	private static final long serialVersionUID = 1L;
	/* ========================================================================== */
	// 05/02/2014 ECU created to contain the item that will be displayed in
	//				  a custom grid view
	// 04/10/2016 ECU added the 'colour'
	// 06/05/2017 ECU added 'imageURL'
	// 09/05/2019 ECU added customLegend
	// 10/05/2019 ECU added itemType and itemState which are used by DevicesActivity
	/* ========================================================================== */
	int		colour;			// 04/10/2016 ECU added
	String  customLegend;	// 09/05/2019 ECU added
	String  extras;			// 06/02/2014 ECU added for any extras
	String	imagePath;		// 05/02/2014 ECU path to the image
	int		imageResourceId;// 19/12/2015 ECU resource id of an image
	String  imageURL;		// 06/05/2017 ECU URL to an image
	int		index;			// 30/03/2014 ECU index to source records
	boolean itemState;		// 10/05/2019 ECU item state
	int		itemType;		// 10/05/2019 ECU item type
	String	legend;			// 05/02/2014 ECU legend for the image
	boolean	selected;		// 30/03/2014 ECU if item selected
	String  summary;		// 06/02/2014 ECU added for summary
	/* ========================================================================== */
	public ListItem (String theImagePath,String theLegend,String theSummary,String theExtras,int theIndex)
	{
		// ----------------------------------------------------------------------
		extras		= theExtras;
		imagePath	= theImagePath;
		index		= theIndex;					// 30/03/2014 ECU added
		legend		= theLegend;
		summary		= theSummary;
		// ----------------------------------------------------------------------
		// 30/03/2014 ECU default private variables that are not supplied
		// ----------------------------------------------------------------------
		selected = false;
		// ----------------------------------------------------------------------
		// 19/12/2015 ECU set the image resource id to indicate nothing
		// ----------------------------------------------------------------------
		imageResourceId	= StaticData.NO_RESULT;
		// ----------------------------------------------------------------------
		// 04/10/2016 ECU set the colour
		// ----------------------------------------------------------------------
		colour = StaticData.NO_RESULT;
		// ----------------------------------------------------------------------
		// 06/05/2017 ECU URL to an image
		// ----------------------------------------------------------------------
		imageURL = null;
		// ----------------------------------------------------------------------
		// 09/05/2019 ECU custom legend
		// ----------------------------------------------------------------------
		customLegend = null;
		// ----------------------------------------------------------------------
	}
	// --------------------------------------------------------------------------
	public ListItem (int theImageResourceId,String theLegend,String theSummary,String theExtras,int theIndex)
	{
		// -------------------------------------------------------------------------
		// 19/12/2015 ECU created this method to set the id of the required image
		// -------------------------------------------------------------------------
		extras		= theExtras;
		imagePath	= null;
		// -------------------------------------------------------------------------
		// 19/12/2015 ECU set the resource ID for required image
		// -------------------------------------------------------------------------
		imageResourceId = theImageResourceId;
		// -------------------------------------------------------------------------
		index		= theIndex;					// 30/03/2014 ECU added
		legend		= theLegend;
		summary		= theSummary;
		// ----------------------------------------------------------------------
		// 30/03/2014 ECU default private variables that are not supplied
		// ----------------------------------------------------------------------
		selected = false;
		// ----------------------------------------------------------------------
		// 04/10/2016 ECU set the colour
		// ----------------------------------------------------------------------
		colour = StaticData.NO_RESULT;
		// ----------------------------------------------------------------------
		// 06/05/2017 ECU URL to an image
		// ----------------------------------------------------------------------
		imageURL = null;
		// ----------------------------------------------------------------------
	}
	/* -------------------------------------------------------------------------- */
	public ListItem (String theImagePath,String theLegend,String theSummary,String theExtras,int theIndex,boolean theSelectedFlag)
	{
		extras		= theExtras;
		imagePath	= theImagePath;
		index		= theIndex;					// 30/03/2014 ECU added
		legend		= theLegend;
		selected 	= theSelectedFlag;			// 31/03/2014 ECU added
		summary		= theSummary;
		// ----------------------------------------------------------------------
		// 19/12/2015 ECU set the image resource id to indicate nothing
		// ----------------------------------------------------------------------
		imageResourceId	= StaticData.NO_RESULT;
		// ----------------------------------------------------------------------
		// 04/10/2016 ECU set the colour
		// ----------------------------------------------------------------------
		colour = StaticData.NO_RESULT;
		// ----------------------------------------------------------------------
		// 06/05/2017 ECU URL to an image
		// ----------------------------------------------------------------------
		imageURL = null;
		// ----------------------------------------------------------------------
	}
	// --------------------------------------------------------------------------
	public ListItem (String theLegend,String theSummary,String theExtras,int theIndex)
	{
		// ----------------------------------------------------------------------
		// 27/02/2015 ECU added this method to handle situation when no image
		//                path is required so that the default resource will be 
		//                used
		// ----------------------------------------------------------------------
		extras		= theExtras;
		imagePath	= null;						// 27/02/2015 ECU added the preset
		index		= theIndex;					// 30/03/2014 ECU added
		legend		= theLegend;
		summary		= theSummary;
		// ----------------------------------------------------------------------
		// 30/03/2014 ECU default private variables that are not supplied
		// ----------------------------------------------------------------------
		selected = false;
		// ----------------------------------------------------------------------
		// 19/12/2015 ECU set the image resource id to indicate nothing
		// ----------------------------------------------------------------------
		imageResourceId	= StaticData.NO_RESULT;
		// ----------------------------------------------------------------------
		// 04/10/2016 ECU set the colour
		// ----------------------------------------------------------------------
		colour = StaticData.NO_RESULT;
		// ----------------------------------------------------------------------
		// 06/05/2017 ECU URL to an image
		// ----------------------------------------------------------------------
		imageURL = null;
		// ----------------------------------------------------------------------
	}
	/* ========================================================================== */
	@Override
	public int compareTo (ListItem listItem) 
	{
		String theLegend = listItem.legend;
		
		return this.legend.compareToIgnoreCase(theLegend);
	}
	// =============================================================================
	public int GetColour ()
	{
		// -------------------------------------------------------------------------
		// 04/10/2016 ECU created to return the colour
		// -------------------------------------------------------------------------
		return colour;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String GetCustomLegend ()
	{
		// -------------------------------------------------------------------------
		// 09/05/2019 ECU created to return the custom legend
		// -------------------------------------------------------------------------
		return customLegend;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public String GetImagePath ()
	{
		// -------------------------------------------------------------------------
		// 26/01/2014 ECU returns the resource ID of the stored image
		// -------------------------------------------------------------------------
		return imagePath;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public String GetExtras ()
	{
		// ----------------------------------------------------------------------
		// 06/02/2014 ECU returns the stored extras
		// ----------------------------------------------------------------------
		return extras;
	}
	/* ========================================================================== */
	public int GetIndex ()
	{
		// ----------------------------------------------------------------------
		// 30/03/2014 ECU returns the stored legend
		// ----------------------------------------------------------------------
		return index;
	}
	/* ========================================================================== */
	public String GetLegend ()
	{
		// ----------------------------------------------------------------------
		// 26/01/2014 ECU returns the stored legend
		// ----------------------------------------------------------------------
		return legend;
	}
	/* ========================================================================== */
	public String GetSummary ()
	{
		// 26/01/2014 ECU returns the stored summary
		
		return summary;
	}
	/* ========================================================================== */
	public boolean ToggleSelected ()
	{
		// ----------------------------------------------------------------------
		// 30/03/2014 ECU toggle the state of the 'selected' flag and return
		//                the new state
		// ----------------------------------------------------------------------
		selected = !selected;
		
		return selected;
	}
	/* ========================================================================== */
}

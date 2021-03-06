package com.usher.diboson;

public class GridItem 
{
	/* ============================================================================= */
	// 26/01/2014 ECU created to contain the item that will be displayed in
	//				  a custom grid view
	// 19/07/2017 ECU added the 'long press' legend
	/* ============================================================================= */
	int		image;		// 26/01/2014 ECU resource id of the image
	String	legend;		// 26/01/2014 ECU legend for the image
	String  legendLong;	// 19/07/2017 ECU legend for long press of the image
	boolean longPress;	// 27/01/2015 ECU indicates if long press can be used
	/* ============================================================================= */
	public GridItem (int theImageId,String theLegend,boolean theLongPress,String theLegendLong)
	{
		image	= theImageId;
		legend	= theLegend;
		// -------------------------------------------------------------------------
		// 27/01/2015 ECU added the flag to do with whether a 'long press' is
		//                valid on this entry
		// -------------------------------------------------------------------------
		longPress = theLongPress;
		// -------------------------------------------------------------------------
		// 19/07/2017 ECU added the legend when a 'long press' done on the image
		// -------------------------------------------------------------------------
		legendLong	= theLegendLong;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	public GridItem (int theImageId,String theLegend,boolean theLongPress)
	{
		// -------------------------------------------------------------------------
		// 19/07/2017 ECU call the main constructor but with no 'long press' legend
		// -------------------------------------------------------------------------
		this (theImageId,theLegend,theLongPress,null);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public int GetImage ()
	{
		// -------------------------------------------------------------------------
		// 26/01/2014 ECU returns the resource ID of the stored image
		// -------------------------------------------------------------------------
		return image;
	}
	/* ============================================================================= */
	public String GetLegend ()
	{
		// -------------------------------------------------------------------------
		// 26/01/2014 ECU returns the stored legend
		// -------------------------------------------------------------------------
		return legend;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public String GetLegendLong ()
	{
		// -------------------------------------------------------------------------
		// 19/07/2017 ECU returns the stored legend for 'long press'
		// -------------------------------------------------------------------------
		return legendLong;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public boolean GetLongPress ()
	{
		// -------------------------------------------------------------------------
		// 27/01/2015 ECU returns the stored long press flag
		// -------------------------------------------------------------------------
		return longPress;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
}

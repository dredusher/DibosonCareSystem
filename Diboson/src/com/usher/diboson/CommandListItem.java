package com.usher.diboson;

import java.io.Serializable;

public class CommandListItem implements Serializable,Comparable<CommandListItem>
{
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	// 15/01/2015 ECU created to contain the legend and position within gridImages
	//				  in the GridActivity
	// =============================================================================
	String  legend;			// 15/01/2015 ECU legend of the command
	int		number;			// 15/01/2015 ECU number associated with the command
	// =============================================================================
	public CommandListItem (String theLegend,int theNumber)
	{
		legend		= theLegend;
		number		= theNumber;
	}
	// =============================================================================
	@Override
	public int compareTo (CommandListItem commandListItem) 
	{
		String theLegend = commandListItem.legend;
		
		return this.legend.compareToIgnoreCase (theLegend);
	}
	// =============================================================================
	public String GetLegend ()
	{
		// -------------------------------------------------------------------------
		// 15/01/2015 ECU returns the stored legend
		// -------------------------------------------------------------------------
		return legend;
	}
	// =============================================================================
	public int GetNumber ()
	{
		// -------------------------------------------------------------------------
		// 15/01/2015 ECU returns the command number
		// -------------------------------------------------------------------------
		return number;
	}
	// =============================================================================
}

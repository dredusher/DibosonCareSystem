package com.usher.diboson;

import java.io.Serializable;

public class SearchParameters implements Serializable
{
	// =============================================================================
	// 10/04/2015 ECU created to contain search string and options
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	public	boolean [] 	searchOptions;
	public	String		searchString;
	// =============================================================================
	public SearchParameters ()
	{
		
	}
	// =============================================================================
	public SearchParameters (int theNumberOfOptions)
	{
		// -------------------------------------------------------------------------
		searchOptions 	= 	new boolean [theNumberOfOptions];
		searchString	= 	"";
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print ()
	{
		String localString = "";
		for (int theOption=0; theOption<searchOptions.length; theOption++)
		{
			localString += " " + searchOptions [theOption];
		}
		return localString + "\nSearchString = " + searchString;
	}
	// =============================================================================
}

package com.usher.diboson;

public class SearchStringAndReplace 
{
	/* ============================================================================= */
	// 14/03/2014 ECU created
	//            ECU used for search and replacing strings
	/* ============================================================================= */
	String		replaceString;
	String		searchString;
	/* ============================================================================= */
	public SearchStringAndReplace (String theSearchString,String theReplaceString)
	{
		// -------------------------------------------------------------------------
		// 14/03/2014 ECU copy the arguments into local variables
		// -------------------------------------------------------------------------
		replaceString	= theReplaceString;
		searchString	= theSearchString;
	}
	/* ============================================================================= */
	public String UpdatedString (String theString)
	{
		// -------------------------------------------------------------------------
		// 14/03/2014 ECU amends the string supplied by the argument by replacing
		//                any occurrences of 'searchString' with 'replaceString'
		// -------------------------------------------------------------------------
		return theString.replaceAll (searchString,replaceString);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
}

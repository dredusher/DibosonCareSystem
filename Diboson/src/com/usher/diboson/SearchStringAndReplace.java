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
		// 16/11/2019 ECU changed from 'replaceAll' to 'replace' because
		//                the former requires a REGEX so not sure why it ever
		//				  worked
		// -------------------------------------------------------------------------
		return theString.replace (searchString,replaceString);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
}

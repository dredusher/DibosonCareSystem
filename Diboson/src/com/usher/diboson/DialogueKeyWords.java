package com.usher.diboson;

public class DialogueKeyWords 
{
	public int			action;				// 24/11/2013 ECU added
	public int			importance;
	public String       preferred;
	public int			theme;
	public int          type;				// 22/11/2013 ECU added
	public String [] 	word;
	/* ============================================================================= */
	public DialogueKeyWords (String [] theWord, String thePreferredString,int theImportance,int theType,int theTheme,int theAction)
	{
		// -------------------------------------------------------------------------
		// 23/11/2013 ECU added method so that the theme can be set (if required)
		// 24/11/2013 ECU added theAction as an argument
		// -------------------------------------------------------------------------
		action      = theAction;			// 24/11/2013 ECU added
		importance 	= theImportance;
		preferred   = thePreferredString;
		theme		= theTheme;				// 23/11/2013 ECU added
		type        = theType;				// 22/11/2013 ECU added		
		word 		= theWord;
	}
	/* ============================================================================= */
	public DialogueKeyWords (String [] theWord, String thePreferredString,int theImportance,int theType)
	{
		// -------------------------------------------------------------------------
		// 23/11/2013 ECU this constructor is words which are relevant to all themes
		// -------------------------------------------------------------------------
		this (theWord,thePreferredString,theImportance,theType,Dialogue.THEME_ALL,Dialogue.ACTION_NONE);
	}
	/* ============================================================================= */
	public DialogueKeyWords (String [] theWord, String thePreferredString,int theImportance,int theType,int theTheme)
	{
		// -------------------------------------------------------------------------
		// 23/11/2013 ECU this constructor is words which are relevant to all themes
		// 24/11/2013 ECU added this method to set the theme
		// -------------------------------------------------------------------------
		this (theWord,thePreferredString,theImportance,theType,theTheme,Dialogue.ACTION_NONE);
	}
	/* ============================================================================= */
}

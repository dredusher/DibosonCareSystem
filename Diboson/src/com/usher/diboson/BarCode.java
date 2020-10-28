package com.usher.diboson;

import java.io.Serializable;

public class BarCode implements Serializable 
{
	// =============================================================================
	// 07/02/2014 ECU created to declare the bar code data
	// 13/06/2016 ECU added the 'actions' that will be processed when the bar code
	//                is read successfully
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	public String   actions;			// the actions to be taken
	public String	barCode;			// the actual barcode
	public String	description;	    // the associated description
	// =============================================================================
	public BarCode (String theBarCode,String theDescription,String theActions)
	{
		// -------------------------------------------------------------------------
		// 13/06/2016 ECU created to create the object from the supplied arguments
		// -------------------------------------------------------------------------
		actions		= 	theActions;
		barCode		=	theBarCode;
		description	=	theDescription;
		// -------------------------------------------------------------------------
		// 13/06/2016 ECU just check if need to adjust the fields
		// 20/03/2017 ECU changed to use BLANK_STRING
		// -------------------------------------------------------------------------
		if (actions.equals (StaticData.BLANK_STRING))
			actions = null;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 13/06/2016 ECU added the 'actions'
		// 09/04/2018 ECU optionally display any stored actions
		// -------------------------------------------------------------------------
		return 	"Description : " + description + StaticData.NEWLINE +
				"Bar Code : " + barCode + 
				((actions == null) ? StaticData.BLANK_STRING : (StaticData.NEWLINE + "Actions : " + actions));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

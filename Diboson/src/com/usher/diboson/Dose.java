package com.usher.diboson;

import java.io.Serializable;

public class Dose implements Serializable
{
	/* ============================================================================= */
	// 29/05/2013 ECU created
	/* ============================================================================= */	
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	float	amount;					// size of dose
	String	units;					// units of the 'amount'
	/* ============================================================================= */
	public String Print (String theInset)
	{
		// -------------------------------------------------------------------------
		// 28/11/2017 ECU use 'printF..' method to tidy up the print so that if the
		//                amount is a whole number then '.0' is not printed
		// -------------------------------------------------------------------------
		return theInset + "Amount : " + Utilities.printFloatNumber (amount) + " " + units ;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
}

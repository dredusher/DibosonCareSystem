package com.usher.diboson;

import java.io.Serializable;

public class Dose implements Serializable
{
	/* ==================================================================== */
	// 29/05/2013 ECU created
	/* ==================================================================== */	
	private static final long serialVersionUID = 1L;
	/* ==================================================================== */
	float	amount;					// size of dose
	String	units;					// units of the 'amount'
	/* ==================================================================== */
	public String Print ()
	{
		return "               Amount : " + amount + " " + units ;
	}
	/* ==================================================================== */
}

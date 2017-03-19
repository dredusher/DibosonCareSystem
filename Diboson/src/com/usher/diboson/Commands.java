package com.usher.diboson;

public class Commands 
{
	/* ============================================================================= */
	// 09/07/2013 ECU created - for telnet commands
	// 03/03/2014 ECU changed to have a constructor rather than an 'Initialise' 
	//                method
	/* ============================================================================= */
	public String	commandString;
	public int		numberOfParameters;
	/* ============================================================================= */
	public Commands (String theString,int theNumberOfParameters)
	{
		commandString 		= theString;
		numberOfParameters 	= theNumberOfParameters;
	}
	/* ============================================================================= */
}

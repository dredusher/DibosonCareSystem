package com.usher.diboson;

public class ActionCommand 
{
	// =============================================================================
	// 11/12/2015 ECU created to hold action command data
	// =============================================================================
	
	// =============================================================================
		   String			command;
           String [][]		parameters;
	// =============================================================================
	
	// =============================================================================
	public ActionCommand (String theCommand,String [][] theParameters)
	{
		// -------------------------------------------------------------------------
		// 11/12/2015 ECU declare structure for initialising the private variables
		// -------------------------------------------------------------------------
		command		= theCommand;
		parameters	= theParameters;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public static String [] ReturnCommands ()
	{
		// -------------------------------------------------------------------------
		// 11/12/2015 ECU return a list of the commands
		// -------------------------------------------------------------------------
		String [] localCommands = new String [StaticData.ACTION_COMMANDS.length];
		// -------------------------------------------------------------------------
		// 
		for (int localCommand = 0; localCommand < localCommands.length; localCommand++)
		{
			localCommands [localCommand] = StaticData.ACTION_COMMANDS [localCommand].command;
		}
		// -------------------------------------------------------------------------
		// 11/12/2015 ECU return the generated commands
		// -------------------------------------------------------------------------
		return localCommands;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String [] ReturnParameters ()
	{
		// -------------------------------------------------------------------------
		// 11/12/2015 ECU return the parameters for this object
		// -------------------------------------------------------------------------
		// 11/12/2015 ECU create an array for the number of sets of parameters
		// -------------------------------------------------------------------------
		String [] parameterSets = new String [parameters.length];
		// -------------------------------------------------------------------------
		// 11/12/2015 ECU for each set of parameters then just generate a printable
		//                string
		// -------------------------------------------------------------------------
		for (int theSet = 0; theSet < parameters.length; theSet++)
		{
			// ---------------------------------------------------------------------
			// 11/12/2015 ECU initialise the return string
			// ---------------------------------------------------------------------
			parameterSets [theSet] = StaticData.BLANK_STRING;
			// ---------------------------------------------------------------------
			for (int theParameter=0; theParameter < parameters [theSet].length; theParameter++)
			{
				parameterSets [theSet] += parameters [theSet][theParameter];
				// -----------------------------------------------------------------
				// 11/12/2015 ECU add a delimiter for each parameter except the last
				// ------------------------------------------------------------------
				if (theParameter != (parameters [theSet].length - 1))
					parameterSets [theSet] += StaticData.ACTION_DELIMITER;
				// ------------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 11/12/2015 ECU return the generated parameter sets
		// -------------------------------------------------------------------------
		return parameterSets;
		// -------------------------------------------------------------------------
	}
	// =============================================================================	
}

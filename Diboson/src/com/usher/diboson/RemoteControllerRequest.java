package com.usher.diboson;

import java.io.Serializable;

public class RemoteControllerRequest implements Serializable
{
	// =============================================================================
	// 27/02/2016 ECU created for passing information to the remote controller server
	// 28/02/2016 ECU set the type to NO_RESULT if not supplied as an argument 
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	String	command;						// the command to be sent to the controller
	int		type;							// type of remote controller
	// =============================================================================
	
	// =============================================================================
	public RemoteControllerRequest (int theType,String theCommand)
	{
		command 	= theCommand;
		type		= theType;
	}
	// =============================================================================
	public RemoteControllerRequest (String theCommandString)
	{
		// -------------------------------------------------------------------------
		// 27/02/2016 ECU called when the type and command is combined in a single
		//                string - the string contains raw codes for the remote
		//                controller
		// -------------------------------------------------------------------------
		command 	= theCommandString;
		type		= StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

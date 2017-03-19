package com.usher.diboson;

import java.io.Serializable;

public class DeviceStatus implements Serializable
{
	// =============================================================================
	// 02/02/2015 ECU created to contain information about the current status
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	// 02/02/2015 ECU confirm the address to which this status relates
	// -----------------------------------------------------------------------------
	public  String				IPAddress;
	// =============================================================================
	public	boolean 			cloneMode			= 	false;
	public	boolean				remoteMusicMode		=	false;
	// =============================================================================
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 05/05/2015 ECU changed to use the method rather than the actual strings
		// -------------------------------------------------------------------------
		return "IP Address : " 			+ IPAddress + "\n" +
				"Clone Mode : " 		+ Utilities.booleanAsString (cloneMode) + "\n" +
			   "Remote Music Mode : " 	+ Utilities.booleanAsString (remoteMusicMode);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

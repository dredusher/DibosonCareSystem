package com.usher.diboson;

public class RemoteController 
{
	/* ============================================================================= */
	public		InfraredCodes	codes;				// 11/05/2015 ECU added
	public		IRFunction []	layout;				// 11/05/2015 ECU added
	/* ============================================================================= */
	public RemoteController (InfraredCodes theCodes,IRFunction [] theLayout)
	{
		// -------------------------------------------------------------------------
		// 11/05/2015 ECU created
		// -------------------------------------------------------------------------
		codes		= theCodes;
		layout		= theLayout;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
}

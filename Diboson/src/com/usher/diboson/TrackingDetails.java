package com.usher.diboson;

import java.io.Serializable;

public class TrackingDetails implements Serializable
{
	// =============================================================================
	// 25/02/2016 ECU created to hold details required when tracking is to take place
	// -----------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	// 25/02/2016 ECU variables :-
	//
	//				enabled ........... true  - tracking in progress
	//                                  false - tracking no in progress
	//              fileName .......... where tracking information is being written
	//              logToFileTimer .... the time between writes to file
	// -----------------------------------------------------------------------------
	public boolean  enabled			= false;
	public String	fileName		= null;
	public int		logToFileTimer	= 0;
	// =============================================================================
}

package com.usher.diboson;

import java.io.Serializable;

public class MetaBroadcast implements Serializable
{
	// =============================================================================
	// 09/07/2016 ECU created to hold data and methods relavant to the interface
	//                to MetaBroadcast's Atlas system for EPG data
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	String	apiKey;
	String	channelURL;
	String	channelGroupsURL;
	// =============================================================================
	
	// =============================================================================
	public MetaBroadcast (String theAPIKey,String theChannelGroupsURL,String theChannelURL)
	{
		// -------------------------------------------------------------------------
		// 09/07/2016 ECU constructor to initialise data with supplied arguments
		// -------------------------------------------------------------------------
		apiKey				= theAPIKey;
		channelURL			= theChannelURL;
		channelGroupsURL	= theChannelGroupsURL;
		// -------------------------------------------------------------------------
	}
	// =============================================================================

}

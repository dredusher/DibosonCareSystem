package com.usher.diboson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// =================================================================================
public class WiFiStateChange extends BroadcastReceiver
{
	// =============================================================================
	// 05/02/2020 ECU added the check on 'wifiStateChange'
	// =============================================================================

	// =============================================================================
	private static final String ACTION_MONITORED = "android.net.conn.CONNECTIVITY_CHANGE";
	// =============================================================================

	// =============================================================================
	@Override
	public void onReceive (Context theContext, Intent theIntent)
	{
		// -------------------------------------------------------------------------
		// 17/08/2019 ECU am only interest in the one action
		// 05/02/2020 ECU added the check on 'wifi...'
		// -------------------------------------------------------------------------
		if (theIntent.getAction().equals (ACTION_MONITORED) && PublicData.storedData.wifiStateChange)
		{
			// ---------------------------------------------------------------------
			// 17/08/2019 ECU now check if the WiFi is on or off
			// ---------------------------------------------------------------------
			if (Utilities.checkForNetwork (theContext))
			{
				// -----------------------------------------------------------------
				// 18/11/2019 ECU the device has connected to the network
				// -----------------------------------------------------------------
				MessageHandler.popToastAndSpeak (String.format (theContext.getString (R.string.wifi_connected_format),
													WiFiInformation.getSSID (theContext)));
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 18/11/2019 ECU the device has disconnected from the network
				// -----------------------------------------------------------------
				MessageHandler.popToastAndSpeak (theContext.getString (R.string.wifi_disconnected));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// ==============================================================================
}
// ==================================================================================
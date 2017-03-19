package com.usher.diboson;

import java.io.IOException;
import java.net.InetAddress;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

public class BroadcastUtilities 
{
	// =============================================================================
	// 16/02/2015 ECU created to have all of the multicast handling in one place
	// 09/04/2016 ECU changed name to broadcast which is really what is being dealt with
	// =============================================================================
	//private final static String TAG	= "BroadcastUtilities";
	// =============================================================================
	
	// =============================================================================
	public static InetAddress getBroadcastAddress (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 22/08/2013 ECU returns the broadcast address that is appropriate to the
		//                device's wireless network IP address
		// -------------------------------------------------------------------------
		InetAddress inetAddress = null;
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 22/08/2013 ECU get the WiFi manager as need the DHCP information
			// ---------------------------------------------------------------------
			WifiManager wifi = (WifiManager) theContext.getSystemService(Context.WIFI_SERVICE);
			// ---------------------------------------------------------------------
			// 22/08/2013 ECU get the addresses from the DHCP information
			// ---------------------------------------------------------------------
			DhcpInfo dhcp = wifi.getDhcpInfo();
			// ---------------------------------------------------------------------
			int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
			// ---------------------------------------------------------------------
			// 22/08/2013 ECU now work out the 32 bit IP address
			// ---------------------------------------------------------------------
			byte[] octets = new byte[4];
	    
			for (int octetIndex = 0; octetIndex < octets.length; octetIndex++)
				octets[octetIndex] = (byte) ((broadcast >> (octetIndex * 8)) & 0xFF);
			// ---------------------------------------------------------------------
			// 22/08/2013 ECU return the broadcast address in InetAddress format
			// ---------------------------------------------------------------------
			inetAddress = InetAddress.getByAddress(octets);
			// ---------------------------------------------------------------------
		}
		catch (IOException theException)
		{
		}
		// -------------------------------------------------------------------------
		// 22/08/2013 ECU return actual address or 'null' if a problem occurred
		// -------------------------------------------------------------------------
		return inetAddress;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

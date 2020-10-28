package com.usher.diboson;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Message;

import java.io.IOException;
import java.net.InetAddress;

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
			DhcpInfo dhcp = wifi.getDhcpInfo ();
			// ---------------------------------------------------------------------
			int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
			// ---------------------------------------------------------------------
			// 22/08/2013 ECU now work out the 32 bit IP address
			// ---------------------------------------------------------------------
			byte [] octets = new byte [4];
	    	// ---------------------------------------------------------------------
			for (int octetIndex = 0; octetIndex < octets.length; octetIndex++)
				octets[octetIndex] = (byte) ((broadcast >> (octetIndex * 8)) & 0xFF);
			// ---------------------------------------------------------------------
			// 22/08/2013 ECU return the broadcast address in InetAddress format
			// ---------------------------------------------------------------------
			inetAddress = InetAddress.getByAddress (octets);
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
	public static void sendBroadcastMessage (String theMessage,int theDelay)
	{
		// -------------------------------------------------------------------------
		// 25/05/2020 ECU created to enable the sending of a broadcast message
		// -------------------------------------------------------------------------
		if (BroadcastServerThread.broadcastRefreshHandler != null)
		{
			// ---------------------------------------------------------------------
			// 25/05/2020 ECU send the SEND message with the data
			// ---------------------------------------------------------------------
			Message localMessage
				= BroadcastServerThread.broadcastRefreshHandler.obtainMessage(StaticData.MESSAGE_SEND,theMessage);
			// ---------------------------------------------------------------------
			// 25/05/2020 ECU check whether an initial delay is wanted
			// ---------------------------------------------------------------------
			if (theDelay == 0)
			{
				// -----------------------------------------------------------------
				// 25/05/2020 ECU send the message immediately
				// -----------------------------------------------------------------
				BroadcastServerThread.broadcastRefreshHandler.sendMessage (localMessage);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 25/05/2020 ECU send the message delayed
				// -----------------------------------------------------------------
				BroadcastServerThread.broadcastRefreshHandler.sendMessageDelayed (localMessage,theDelay);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void sendBroadcastMessage (String theMessage)
	{
		// -------------------------------------------------------------------------
		// 25/05/2020 ECU send the specified message immediately
		// -------------------------------------------------------------------------
		sendBroadcastMessage (theMessage,0);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

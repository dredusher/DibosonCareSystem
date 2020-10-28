package com.usher.diboson;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;

import java.io.File;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

public class APIIssues 
{
	/* ============================================================================= */
	// 06/03/2014 ECU created to contain any methods that try to sort out issues
	//                with different API levels - those issues are detailed in the
	//                Notes file
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	/* ============================================================================= */
	public static void Fix001 (Context theContext,int theAPILevel)
	{
		// -------------------------------------------------------------------------
		// 06/03/2014 ECU added to solve a problem with the speaking clock where
		//                the TTS phrases seem to be batched (see Notes file)
		// 09/03/2014 ECU the name was changed from 'SpeakingClock' because
		//                it is used elsewhere
		// 19/07/2017 ECU added theContext as an argument see 'raw/documentation_bugs'
		// -------------------------------------------------------------------------
		if (theAPILevel >= 19)
		{
			// ---------------------------------------------------------------------
			// 19/07/2017 ECU changed the initial argument from 'null' to context
			// ---------------------------------------------------------------------
			MediaPlayer mediaPlayer 
				= MediaPlayer.create (theContext, Uri.fromFile (new File(PublicData.projectFolder + "KitKat.wav")));
			// ---------------------------------------------------------------------
			// 10/08/2013 ECU check if it was possible to create the mediaPlayer
			// ---------------------------------------------------------------------
			if (mediaPlayer != null)
			{
				// -----------------------------------------------------------------
				mediaPlayer.start();
				mediaPlayer.stop ();
				// -----------------------------------------------------------------
				// 11/03/2014 ECU added the '.reset' because was getting warnings
				//                about the media player being released with
				//                unhandled events
				// -----------------------------------------------------------------
				mediaPlayer.reset ();
				// -----------------------------------------------------------------
				mediaPlayer.release();
				// -----------------------------------------------------------------
			}
		}
	}
	// =============================================================================
	private static String GenerateSubnetMask (int theNetworkLength)
	{
		// -------------------------------------------------------------------------
		// 19/01/2015 ECU convert the length to an IPv4 (32 bit) mask as a string
		// -------------------------------------------------------------------------
		String	mask = StaticData.BLANK_STRING;
		int 	octet;
		// -------------------------------------------------------------------------
		// 19/01/2015 ECU loop through the 4 octets that make up the IPv4 32 bit
		//                address
		//            ECU some of the masking is because I'm using an int rather
		//                than a char ... & 0xFF) - it works so leave till later
		// -------------------------------------------------------------------------
		for (int theIndex = 3; theIndex >= 0; theIndex--)
		{
			octet  = ((theNetworkLength >= 8) ? 8 : theNetworkLength);	
			mask += ((0xFF << (8 - octet)) & 0xFF) + ((theIndex > 0) ? "." : StaticData.BLANK_STRING);	
			theNetworkLength -= 8;		
		}
		return mask;
		// -------------------------------------------------------------------------
		// 20/01/2015 ECU an alternative to the above which has been tested OK
		// -------------------------------------------------------------------------
		//	theNetworkLength = 0xFFFFFFFF << (32 - theNetworkLength);
		//
		//	return  ((theNetworkLength & 0xFF000000) >>> 24) + "." +
		//			((theNetworkLength & 0x00FF0000) >>> 16) + "." +
		//			((theNetworkLength & 0x0000FF00) >>> 8)  + "." +
		//			((theNetworkLength & 0x000000FF));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean CheckPermission (String thePermission)
	{
		// -------------------------------------------------------------------------
		// 14/08/2020 ECU check for any permissions that are introduced at a
		//                particular API level and which could have 'permission
		//                denied' and cannot be changed
		// -------------------------------------------------------------------------
		// 14/08/2020 ECU at the moment the issue is :-
		//
		//                  >= Oreo          FOREGROUND+SERVICE
		// -------------------------------------------------------------------------
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
		{
			// ---------------------------------------------------------------------
			// 14/08/2020 ECU on pre Oreo check for the fore ground service
			// ---------------------------------------------------------------------
			if (thePermission.equals ("android.permission.FOREGROUND_SERVICE"))
			{
				// -----------------------------------------------------------------
				// 14/08/2020 ECU indicate that, although the permission has been
				//                denied, it is a normal permission that the user
				//                cannot do anything about
				// -----------------------------------------------------------------
				// 14/08/2020 ECU indicate that the permission is to be left as it is
				//                with no user intervention
				// -----------------------------------------------------------------
				return true;
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 14/08/2020 ECU allow the user to grant/deny permission on this
				//                item
				// ------------------------------------------------------------------
				return false;
				// ------------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 14/08/2020 ECU indicate that there are no issues and normal processing
			//                should take place
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
    public static void NetworkOnMainUIThread (int theAPILevel)
    {
    	// -------------------------------------------------------------------------
    	// 25/01/2014 ECU bodge to get around the problem that networking should
    	//                not be run on the main UI thread (see Notes file)
    	// -------------------------------------------------------------------------
    	if (theAPILevel > 9) 
		{
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy (policy);
		}
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static String SubnetMask (int theAPILevel,String theIPAddress,String theCurrentNetworkMask)
    {
    	// -------------------------------------------------------------------------
    	// 19/01/2015 ECU this is to overcome a problem with Lollipop (API 21)
    	//                which is returning a subnet mask of 0 when obtained in
    	//                the normal way
    	// -------------------------------------------------------------------------
    	if (theAPILevel >= 21)
    	{
    		// ---------------------------------------------------------------------
    		// 19/01/2015 ECU use InterfaceAddress to get the mask
    		// ---------------------------------------------------------------------
    		try 
    		{
    			Enumeration<NetworkInterface> networkInterfaces;
    			networkInterfaces = NetworkInterface.getNetworkInterfaces();
    		
    			while (networkInterfaces.hasMoreElements()) 
    			{
    				NetworkInterface networkInterface = networkInterfaces.nextElement();
    				List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
    				for (InterfaceAddress interfaceAddress : interfaceAddresses) 
    				{
    					if (interfaceAddress.getAddress().toString().substring(1).equalsIgnoreCase(theIPAddress))
    					{
    						// -----------------------------------------------------
    						// 19/01/2015 ECU convert the subnet mask length to the
    						//                required string
    						// -----------------------------------------------------
    						return GenerateSubnetMask (interfaceAddress.getNetworkPrefixLength());
    					}
    				}
    			}	
    		} 
    		catch (SocketException theException) 
    		{
    		}
    		// ---------------------------------------------------------------------
    		// 19/01/2015 ECU just set to the default
    		// ---------------------------------------------------------------------
    		return StaticData.NETWORK_MASK;
    		// ---------------------------------------------------------------------
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 19/01/2015 ECU for all 'non-lollipop' then the original subnet mask
    		//                should be OK
    		// ---------------------------------------------------------------------
    		return theCurrentNetworkMask;
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
}

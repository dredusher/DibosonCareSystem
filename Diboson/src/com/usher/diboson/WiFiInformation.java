package com.usher.diboson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// =================================================================================
public class WiFiInformation
{
	// =============================================================================

	// =============================================================================
	// =============================================================================

	// =============================================================================
	static	boolean			gpsNeeded	= false;			// 18/12/2019 ECU added
	static 	Method			scanFinishedMethod;
	static 	int				sortFlag;						// 22/02/2019 ECU added
	static 	WifiManager 	wifiManager;
	// =============================================================================
	
	// =============================================================================
	// 19/02/2019 ECU created to handle the retrieval of information about the WiFi
	//                channels
	// 13/03/2019 ECU changed to use spannable rather than HTML
	// =============================================================================
	// =============================================================================
	public static String getSSID (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 08/01/2019 ECU created to return the SSID of the currently connected
		//                wireless connection
		// 19/02/2019 ECU Note - this method was in Utilities and then moved here
		// -------------------------------------------------------------------------
		// 18/12/2019 ECU because there are possible issues with various versions
		//                of Android then just inform the user of this
		// -------------------------------------------------------------------------
		WiFiSSIDIssues(theContext);
		// -------------------------------------------------------------------------
		// 08/01/2019 ECU get the WiFi manager
		// -------------------------------------------------------------------------
		WifiManager wifiManager = (WifiManager) theContext.getSystemService (Context.WIFI_SERVICE);
		// -------------------------------------------------------------------------
		// 08/01/2019 ECU return the SSID
		// -------------------------------------------------------------------------
		return wifiManager.getConnectionInfo ().getSSID ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static double getStrength (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 19/02/2019 ECU created to return the strength of the currently connected
		//                WiFi SSID
		// -------------------------------------------------------------------------
		WifiManager wifiManager = (WifiManager) theContext.getSystemService (Context.WIFI_SERVICE);
		// -------------------------------------------------------------------------
		// 19/02/2019 ECU get the signal strength and return it
		// -------------------------------------------------------------------------
		WifiInfo wifiInfo = wifiManager.getConnectionInfo ();
		// -------------------------------------------------------------------------
		// 20/02/2019 ECU get the strength in terms of the 'interval'
		// 19/02/2020 ECU when testing on the HTC Wildfire was getting a 'divide by 0'
		//                error so add the tray...catch
		// -------------------------------------------------------------------------
		try
		{
			int localStrength = WifiManager.calculateSignalLevel (wifiInfo.getRssi(),StaticData.WIFI_STRENGTH_LEVELS);
			// ---------------------------------------------------------------------
			// 20/02/2019 ECU localStrength will have a value 0 to (StaticData.WIFI_STRENGTH_LEVELS - 1)
			//                - convert to a percentage
			// ---------------------------------------------------------------------
			return (((float) (localStrength) / (float) (StaticData.WIFI_STRENGTH_LEVELS - 1)) * 100.0);
			// ---------------------------------------------------------------------
		}
		catch (ArithmeticException theException)
		{
			// ---------------------------------------------------------------------
			// 19/02/2020 ECU have received a 'divide by 0' exception - indicate this
			//                to the caller
			// ---------------------------------------------------------------------
			return StaticData.NOT_SET;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	private static int getWiFiChannel (int theFrequency)
	{
		// -------------------------------------------------------------------------
		// 12/03/2019 ECU convert the frequency to the associated channel using
		//                the values defined in IEEE 802.11a
		// -------------------------------------------------------------------------
		// 12/03/2019 ECU 2.4 GHz channels are normally 5 MHz apart. Channel 14 which
		//                        is not used in either Europe or North America is
		//                        an exception
		// -------------------------------------------------------------------------
		if (theFrequency == 2484)
			return 14;
		// -------------------------------------------------------------------------
		// 12/03/2019 ECU check the other 2.4 GHz channels
		// -------------------------------------------------------------------------
		if (theFrequency < 2484)
			return (theFrequency - 2407) / 5;
		// -------------------------------------------------------------------------
		// 12/03/2019 ECU now handle the 5 GHz channels
		// -------------------------------------------------------------------------
		return (theFrequency / 5) - 1000;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void getWiFiChannels (Context theContext,Method theScanFinishedMethod,int theSortFlag)
	{
		// -------------------------------------------------------------------------
		// 20/02/2019 ECU remember the method that will be invoked when the scan 
		//                finishes
		// -------------------------------------------------------------------------
		scanFinishedMethod = theScanFinishedMethod;
		sortFlag		   = theSortFlag;
		// -------------------------------------------------------------------------
		// 19/02/2019 ECU set up the broadcast receiver that will receive the results
		// -------------------------------------------------------------------------
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		theContext.registerReceiver(wifiScanReceiver, intentFilter);
		// -------------------------------------------------------------------------
		wifiManager = (WifiManager) theContext.getSystemService (Context.WIFI_SERVICE);
		// -------------------------------------------------------------------------
		// 19/02/2019 ECU now want to initiate the scan
		// -------------------------------------------------------------------------
		wifiManager.startScan ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void WiFiAbortScan (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 14/03/2019 ECU created to 'abort' the current scan. The ongoing scan
		//                cannot be stopped but by setting the method to 'null'
		//                will cause no actions to be taken when the scan finishes
		//            ECU only need to do if a scan has been initiated
		// -------------------------------------------------------------------------
		if (scanFinishedMethod != null)
		{
			// ---------------------------------------------------------------------
			// 14/03/2019 ECU it appears that there is a scan in progress so carry
			//                on and try to stop things
			// ---------------------------------------------------------------------
			scanFinishedMethod = null;
			// ---------------------------------------------------------------------
			// 14/03/2019 ECU cannot actually stop the scan but just 'unregister' the
			//                receiver - this is a bit of 'belt and braces'
			// ---------------------------------------------------------------------
			try
			{
				theContext.unregisterReceiver (wifiScanReceiver);
			}
			catch (IllegalArgumentException theException)
			{ 
				// -----------------------------------------------------------------
				// 14/03/2019 ECU the receiver is not registered
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void WiFiSSIDIssues (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 18/12/2019 ECU with the introduction of Oreo (API 26) then GPS needs to
		//                be 'active' in order that the correct SSID is returned. If
		//                not enabled then '<unknown SSID>' will be returned as the
		//                SSID.
		// -------------------------------------------------------------------------
		// 18/12/2019 ECU only want to do for >= Oreo (API 26)
		// -------------------------------------------------------------------------
		if (android.os.Build.VERSION.SDK_INT >= 26)
		{
			// ---------------------------------------------------------------------
			// 18/12/2019 ECU only want to the check once
			// ---------------------------------------------------------------------
			if (!gpsNeeded)
			{
				// -----------------------------------------------------------------
				// 18/12/2019 ECU the method has not previously been called - indicate
				//                that has now.
				// -----------------------------------------------------------------
				gpsNeeded = true;
				// -----------------------------------------------------------------
				// 18/12/2019 ECU now check if GPS is currently enabled
				// -----------------------------------------------------------------
				if (!Utilities.checkForLocationServices (theContext))
				{
					// -------------------------------------------------------------
					// 18/12/2019 ECU services are not enabled
					// -------------------------------------------------------------
					MessageHandler.popToastAndSpeak (theContext.getString (R.string.ssid_enable_location_services));
					// -------------------------------------------------------------
					// 19/12/2019 ECU send a 'notification' so that there is a more
					//                permanent reminder
					// 15/07/2020 ECU added the title
					// -------------------------------------------------------------
					NotificationMessage.Add (theContext.getString (R.string.title_wifi),
											 theContext.getString (R.string.ssid_enable_location_services),
											 StaticData.NOTIFICATION_COLOUR_ERROR);
					// -------------------------------------------------------------

				}
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static WiFiStrengthData wifiLevelMeaning (Context theContext,int theLevel)
	{
		// -------------------------------------------------------------------------
		// 20/02/2019 ECU display the status of the WiFi network based on its signal
		//                level in dbm
		//
		//                <  -70 dbm             weak
		//                >= -70 && < -60 dbm    fair
		//                >= -60 && < -50 dbm    good
		//                >  -50 dbm             excellent
		// 12/03/2019 ECU changed the logic to return String [2]
		//	                  0 ............... HTML command for required colour
		//                    1 ............... the original string strength
		// -------------------------------------------------------------------------
		// 12/03/2019 ECU changed the logic
		// -------------------------------------------------------------------------
		WiFiStrengthData localData = new WiFiStrengthData ();
		// -------------------------------------------------------------------------
		if (theLevel < -70)
		{
			return localData.setData (Color.RED,theContext.getString (R.string.wifi_weak));
		}
		else
		if ((theLevel >= -70) && (theLevel < -60))
		{
			return localData.setData (Color.CYAN,theContext.getString (R.string.wifi_fair));
		}
		else
		if ((theLevel >= -60) && (theLevel < -50))
		{
			return localData.setData (Color.LTGRAY,theContext.getString (R.string.wifi_good));
		}
		else
		{
			return localData.setData (Color.GREEN,theContext.getString (R.string.wifi_excellent));
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static BroadcastReceiver wifiScanReceiver = new BroadcastReceiver () 
	{
		// -------------------------------------------------------------------------
		@Override
		public void onReceive (Context theContext,Intent theIntent) 
		{
			// ---------------------------------------------------------------------
			// 14/03/2019 ECU if the method, that would normally be invoked when this
			//                receiver is activated, is 'null' then do no processing
			// ---------------------------------------------------------------------
			if (scanFinishedMethod != null)
			{
				// -----------------------------------------------------------------
				// 14/03/2019 ECU processing is required
				// -----------------------------------------------------------------
				WiFiScanResults (theContext);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	static void WiFiScanResults (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 20/02/2019 ECU generate a 'displayable' string from the returned results
		// -------------------------------------------------------------------------
		// 13/03/2019 ECU declare the string that will contain the results
		// -------------------------------------------------------------------------
		SpannableStringBuilder localWiFiDetails = new SpannableStringBuilder ();
		// -------------------------------------------------------------------------
		// 12/03/2019 ECU added the initial 'break'
		// -------------------------------------------------------------------------
		String localEntryString = theContext.getString (R.string.wifi_header);
		// -------------------------------------------------------------------------
		localWiFiDetails.append (localEntryString);
		// -------------------------------------------------------------------------
		// 12/03/2019 ECU declare the string array for strength details
		// -------------------------------------------------------------------------
		int					localIndex 			= localEntryString.length();
		int					localIndexTemp;
		WiFiStrengthData 	localStrength;
		// -------------------------------------------------------------------------
		// 21/02/2019 ECU get the results of the scan
		// -------------------------------------------------------------------------
	    List<ScanResult> scanResults = wifiManager.getScanResults();
	    // -------------------------------------------------------------------------
	    // 21/02/2019 ECU set up the comparator to sort by signal strength
	    // -------------------------------------------------------------------------
		Comparator<ScanResult> strengthComparator = new Comparator<ScanResult>() 
		{
			// ---------------------------------------------------------------------
			@Override
			public int compare (ScanResult lhs, ScanResult rhs) 
			{
		            return (rhs.level < lhs.level ? -1 : (lhs.level == rhs.level ? 0 : 1));
			}
			// ---------------------------------------------------------------------
		};
		 // -------------------------------------------------------------------------
	    // 21/02/2019 ECU set up the comparator to sort by SSID
		// -------------------------------------------------------------------------
		Comparator<ScanResult> networkComparator = new Comparator<ScanResult>() 
		{
			// ---------------------------------------------------------------------
			@Override
			public int compare (ScanResult lhs, ScanResult rhs) 
			{
				 return ((lhs.SSID).compareTo (rhs.SSID));
			}
			// ---------------------------------------------------------------------
		};
		// -------------------------------------------------------------------------
		// 21/02/2019 ECU now sort the list
		// 22/02/2019 ECU decide how to sort
		// -------------------------------------------------------------------------
		switch (sortFlag)
		{
			// ---------------------------------------------------------------------
			case StaticData.WIFI_SORT_NETWORK:
				Collections.sort (scanResults,networkComparator);
				break;
			// ---------------------------------------------------------------------
			case StaticData.WIFI_SORT_STRENGTH:
				Collections.sort (scanResults,strengthComparator);
				break;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		for (ScanResult localResult : scanResults) 
		{
			// ---------------------------------------------------------------------
			// 12/03/2019 ECU get the strength details
			// ---------------------------------------------------------------------
			localStrength = wifiLevelMeaning (theContext,localResult.level);
			// ---------------------------------------------------------------------
			// 12/03/2019 ECU added the channel number
			// 12/03/2019 ECU changed to include HTML formatting
			//            ECU for some reason "-<captial letter> causes problems
			//                hence the change to '_'
			// 13/03/2019 ECU HTML removed with new use of spannable
			//            ECU the channel width only became available at API 23
			// 29/02/2020 ECU changed some of the formatting arguments
			// ---------------------------------------------------------------------
			localEntryString = String.format ("%-20.20s %6d %3d  %-5s %-5d %-10s %-20s\n",
											(localResult.SSID),
											localResult.frequency,
											getWiFiChannel (localResult.frequency),
											((android.os.Build.VERSION.SDK_INT >= 23) ? Integer.toString (localResult.channelWidth) : "n/a"),
											localResult.level,
											localStrength.meaning,
										    Utilities.ifContains (localResult.capabilities, new String [] {"WPA","WPA2","WPS","ESS"}));
			// ---------------------------------------------------------------------
			// 12/03/2019 ECU add the entry string into the total string
			// ---------------------------------------------------------------------
			localWiFiDetails.append (localEntryString);
			// ---------------------------------------------------------------------
			// 13/03/2019 ECU set up the colour for this entry
			// ---------------------------------------------------------------------
			localIndexTemp = localIndex + localEntryString.length();
			localWiFiDetails.setSpan (new BackgroundColorSpan (localStrength.colour),localIndex,localIndexTemp,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			localIndex = localIndexTemp;
			// ----------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 20/02/2019 ECU invoke the method that will handle the generated string
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 03/03/2019 ECU changed from 'null' to '...getDeclaring....' so that
			//                called method does not have to be static
			//					scanFinishedMethod.invoke (null,new Object [] {localString});
			// 13/03/2019 ECU changed the object being passed
			// ---------------------------------------------------------------------
			Utilities.invokeMethod (scanFinishedMethod,new Object [] {localWiFiDetails});
			// ---------------------------------------------------------------------
		} 
		catch (Exception theException) 
		{
			
		} 
		// ------------------------------------------------------------------------
	}
	// =============================================================================
}
// =================================================================================

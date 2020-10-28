package com.usher.diboson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;

public class BluetoothUtilities extends Activity
{
	/* ============================================================================= */
	//private static final String TAG					  = "BluetoothUtilities";
	/* ============================================================================= */
	// 06/09/2013 ECU created - to handle all bluetooth utilities
	// 25/01/2015 ECU changed to use the bluetooth details held in the Carer's records
	// 26/08/2015 ECU moved the notification manager here
	// 29/12/2015 ECU major rewrite to use alarms to initiate discovery rather than
	//                a handler
	/* ============================================================================= */
	private static final int NOTIFICATION_ID 			= 666;
	private static final int REQUEST_BLUETOOTH_ENABLE 	= 123;
	/* ============================================================================= */
	// private data
	// ------------
	static 	Activity					activity;						// 08/09/2013 ECU added
	static	PendingIntent 				alarmPendingIntent	= null;		// 01/01/2016 ECU moved here from set an alarm method
			boolean						announceFlag		= false;	// 25/08/2015 ECU added - whether events are spoken
			List<BluetoothDevice>       discoveredDevices;				// 25/01/2015 ECU changed from String
			NotificationManager 		notificationManager = null;
	/* ----------------------------------------------------------------------------- */
	// public data
	// -----------
	BluetoothAdapter			bluetoothAdapter 	= null;				// 21/01/2015 ECU changed to static
	List<BluetoothDevice>		bluetoothDevices 	= new ArrayList<BluetoothDevice>();
																		// 06/09/2013 ECU added - list of discovered bluetooth devices
																		// 25/01/2015 ECU changed from String to BluetoothDevice
	List<String>				deviceDetails		= new ArrayList<String>();
																		// 25/01/2015 ECU added for display details
	/* ============================================================================= */
	public BluetoothUtilities (Activity theActivity)
	{
		// -------------------------------------------------------------------------
		// 06/09/2013 ECU created - constructor
		// -------------------------------------------------------------------------
		// 08/09/2013 ECU remember the calling activity
		// -------------------------------------------------------------------------
		activity = theActivity;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() 
	{
	    public void onReceive(Context theContext, Intent theIntent)
	    {
	    	// ---------------------------------------------------------------------
	    	// 07/09/2013 ECU created to handle events associated with the bluetooth
	    	//                adapter
	    	// ---------------------------------------------------------------------
	        String localAction = theIntent.getAction ();
	        // ---------------------------------------------------------------------
	        // 07/09/2013 ECU When discovery finds a device
	        // ---------------------------------------------------------------------
	        if (BluetoothDevice.ACTION_FOUND.equals (localAction)) 
	        {
	        	// -----------------------------------------------------------------
	            // Get the BluetoothDevice object from the Intent
	        	// -----------------------------------------------------------------
	            BluetoothDevice bluetoothDevice = theIntent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
	            // -----------------------------------------------------------------
	            // 25/01/2015 ECU changed because 'devices' is now of BluetoothDevice
	            //                instead of String
	            // -----------------------------------------------------------------
	            bluetoothDevices.add (bluetoothDevice);	
	            // -----------------------------------------------------------------
	            // 25/01/2015 ECU indicate the number of devices found
	            // 26/08/2015 ECU put in the check on 'null'
				// 30/12/2015 ECU update the status message using new method
				// -----------------------------------------------------------------
				updateStatusMessage (BlueToothActivity.bluetoothStatus,
						"Number of discovered devices = " + bluetoothDevices.size());
				// -----------------------------------------------------------------
				// 30/12/2015 ECU tell the activity about the newly found device
				// -----------------------------------------------------------------
				if (BlueToothActivity.bluetoothStatus != null)
					BlueToothActivity.announceDevice (theContext, bluetoothDevice);
	            // -----------------------------------------------------------------
	            // 25/01/2015 ECU add in the device details for display
	            // -----------------------------------------------------------------
	            deviceDetails.add (Print(bluetoothDevice));
	            // -----------------------------------------------------------------
	            // 21/01/2015 ECU try and display the device name
	            // 23/01/2015 ECU change to handle the new ListView
	            // -----------------------------------------------------------------
	        	if (BlueToothActivity.arrayAdapter != null)
	        		BlueToothActivity.arrayAdapter.notifyDataSetChanged();
	            // -----------------------------------------------------------------
	        }
	        else
	        // ---------------------------------------------------------------------
	        // 06/09/2013 ECU action when discovery finished
	        // ---------------------------------------------------------------------
	        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals (localAction)) 
	        {
	        	// -----------------------------------------------------------------
	        	// 25/08/2015 ECU make the speaking of the phrase optional
	        	// -----------------------------------------------------------------
	        	if (announceFlag)
	        		Utilities.SpeakAPhrase(theContext,"end of discovery");
	        	// -----------------------------------------------------------------
				// 30/12/2015 ECU update the status message
				// -----------------------------------------------------------------
				updateStatusMessage (BlueToothActivity.bluetoothStatus,"bluetooth discovery has finished");
	        	// -----------------------------------------------------------------
	        	// 06/09/2013 ECU copy the working device list into the discovered list
				// 25/11/2016 ECU changed from 
				//                     discoveredDevices = bluetoothDevices;
	        	// -----------------------------------------------------------------
	        	discoveredDevices = new ArrayList<BluetoothDevice> (bluetoothDevices);
	        	// -----------------------------------------------------------------
	        	// 09/01/2014 ECU call up the carer handler
	        	// -----------------------------------------------------------------
	        	CarerActivity.CarerDetection (theContext,discoveredDevices);
	        	// -----------------------------------------------------------------
	        	// 26/01/2015 ECU if notification of the discovered devices is
	        	//                wanted then uncomment the following line
	        	// -----------------------------------------------------------------
	        	if (notificationManager != null)
	        		notificationUpdate (theContext,notificationManager,"Bluetooth Discovered Devices",bluetoothDevices);
	        	// -----------------------------------------------------------------
	        	// 29/12/2015 ECU now set up the alarm to get the next discovery
	        	// -----------------------------------------------------------------
	        	BluetoothUtilities.SetAlarm (theContext,StaticData.BLUETOOTH_DISCOVERY_TIME, StaticData.ALARM_ID_BLUETOOTH_DISCOVERY);
	        	// -----------------------------------------------------------------
	        }
	    }
	};
	// =============================================================================
	public void Discover (boolean theStartStopFlag)
	{
		// -------------------------------------------------------------------------
		// 21/01/2015 ECU added the flag
		//                     theStartStopFlag  =  true   start discovery
		//                                       =  false  stop discovery
		//	------------------------------------------------------------------------
		if (theStartStopFlag)
		{
			// ---------------------------------------------------------------------
			// 21/01/2015 ECU start discovery
			// ---------------------------------------------------------------------
			// 29/12/2015 ECU if the adapter isn't currently discovering then tell
			//                it to start
			// ---------------------------------------------------------------------
			if (!bluetoothAdapter.isDiscovering())
			{
				// -----------------------------------------------------------------
				// 21/01/2015 ECU tell the user what is going on
				// 25/08/2015 ECU optional announce that discovery starting
				// -----------------------------------------------------------------
				if (announceFlag)
					Utilities.SpeakAPhrase (activity, "blue tooth discovery is starting");
				// -----------------------------------------------------------------
				// 30/12/2015 ECU update the status message
				// -----------------------------------------------------------------
				updateStatusMessage (BlueToothActivity.bluetoothStatus,"bluetooth discovery is starting");
				// -----------------------------------------------------------------
				if (notificationManager != null)
					notificationManager.cancel (NOTIFICATION_ID);
				// -----------------------------------------------------------------
				// 06/09/2013 ECU initialise the devices list
				// -----------------------------------------------------------------
				bluetoothDevices.clear (); 
				// -----------------------------------------------------------------
				// 25/01/2015 ECU clear the details
				// -----------------------------------------------------------------
				deviceDetails.clear ();
				// -----------------------------------------------------------------
				// 23/01/2015 ECU reflect the removal of devices in the ListView
				// -----------------------------------------------------------------
				if (BlueToothActivity.arrayAdapter != null)
					BlueToothActivity.arrayAdapter.notifyDataSetChanged();
				// -----------------------------------------------------------------
				// 06/09/2013 ECU start the actual bluetooth discovery
				// -----------------------------------------------------------------
				bluetoothAdapter.startDiscovery();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 21/01/2015 ECU cancel any discovery
			// ---------------------------------------------------------------------
			if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering())
			{
				bluetoothAdapter.cancelDiscovery();
			}
			// ---------------------------------------------------------------------
		}	 	 
	}
	// =============================================================================
	public BluetoothAdapter getBluetoothAdapter ()
	{
		// -------------------------------------------------------------------------
		// 24/01/2015 ECU called to return the bluetooth adapter - if necessary it 
		//                will enable bluetooth. If there is a problem then a 'null'
		//                is returned.
		// 29/12/2015 ECU changed the name
		// -------------------------------------------------------------------------
		BluetoothAdapter localBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// -------------------------------------------------------------------------
		// 25/01/2015 ECU if the device does not support bluetooth then a 'null' is
		//                returned
		// -------------------------------------------------------------------------
		if (localBluetoothAdapter != null)
		{
			// ---------------------------------------------------------------------
			// 06/09/2013 ECU bluetooth is supported so make sure it is enabled
			// ---------------------------------------------------------------------
			if (!localBluetoothAdapter.isEnabled()) 
			{
				// -----------------------------------------------------------------
				// 06/09/2013 ECU bluetooth is not enabled so request that it is
				// 03/12/2013 ECU change the context independent popToast
				// 30/11/2014 ECU had to include 'activity.' so as to avoid getting
				//                an NPE
				// -----------------------------------------------------------------
				Utilities.popToast (activity.getString (R.string.bluetooth_not_enabled));
				// -----------------------------------------------------------------
				// 29/06/2013 ECU want to try and enable the bluetooth
				// -----------------------------------------------------------------
				try
				{
					// -------------------------------------------------------------
					// 08/09/2013 ECU the following generates a null exception because
					//                of invalid context 
					//            ECU changed to use 'activity.' which seems to fix 
					//                the null point exception
					// -------------------------------------------------------------
					Intent enableBluetoothIntent = new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
					activity.startActivityForResult (enableBluetoothIntent,REQUEST_BLUETOOTH_ENABLE);
					// -------------------------------------------------------------
					// 24/01/2015 ECU if no exception occurs then it has been 
					//                possible to enable bluetooth
					// -------------------------------------------------------------
					return localBluetoothAdapter;
					// -------------------------------------------------------------
				}
				catch (Exception theException)
				{
					//--------------------------------------------------------------
					// 24/01/2015 ECU problem occur when trying to enable bluetooth
					//                so reflect this fact
					// --------------------------------------------------------------
					return null;
				}
			}
			else
			{
				// -----------------------------------------------------------------
				// 24/01/2015 ECU bluetooth is enabled
				// -----------------------------------------------------------------
				return localBluetoothAdapter;
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 24/01/2015 ECU device does not support bluetooth
			// ---------------------------------------------------------------------
			return null;
		}					
	}
	// =============================================================================
	public static void handleAlarm (Context theContext,int theAlarmType)
	{
		// -------------------------------------------------------------------------
		// 29/12/2015 ECU created to handle the alarm associated with bluetooth
		//                discovery. On receipt it will start up the discover
		//                process
		// 31/12/2015 ECU put a check in just in case the alarm comes in before
		//                initialisation is complete
		// -------------------------------------------------------------------------
		if (PublicData.bluetoothUtilities != null)
			PublicData.bluetoothUtilities.Discover (true);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public boolean Initialise (Context theContext,boolean theAnnounceFlag)
	{
		// -------------------------------------------------------------------------
		// 06/09/2013 ECU created - initialises various aspects
		// 29/12/2015 ECU added the announce flag argument
		// -------------------------------------------------------------------------
		// 29/12/2015 ECU remember the announce flag
		// -------------------------------------------------------------------------
		announceFlag = theAnnounceFlag;
		// -------------------------------------------------------------------------
		// 06/09/2013 ECU get the bluetooth adapter
		// 24/01/2015 ECU changed to use new method
		// -------------------------------------------------------------------------
		bluetoothAdapter = getBluetoothAdapter ();
		// -------------------------------------------------------------------------
		// 24/01/2015 ECU if adapter is non-null then can continue
		// -------------------------------------------------------------------------
		if (bluetoothAdapter != null)
		{
			// ---------------------------------------------------------------------
			// 26/08/2015 ECU create the notification manager
			// ---------------------------------------------------------------------
		    notificationManager 
				= (NotificationManager) theContext.getSystemService (NOTIFICATION_SERVICE);
	   		// ---------------------------------------------------------------------
			// 29/06/2013 ECU register the broadcast receiver
	   		// 25/01/2015 ECU changed to use new method
	   		// --------------------------------------------------------------------- 
			registerBroadcastReceiver (theContext,bluetoothReceiver, 
					new String [] {BluetoothDevice.ACTION_FOUND,BluetoothAdapter.ACTION_DISCOVERY_FINISHED}); 
			// ---------------------------------------------------------------------
			// 06/09/2013 ECU indicate that bluetooth is supported and should be ready to use
			// ---------------------------------------------------------------------
			return true;
		}
		// -------------------------------------------------------------------------
		// 06/09/2013 ECU indicate that bluetooth is not available
		// -------------------------------------------------------------------------	
		return false;
	}
	// =============================================================================
	public static void notificationUpdate (Context theContext,
										   NotificationManager theNotificationManager,
										   String theTitle,
										   List<BluetoothDevice> theDevices)
	{
		// -------------------------------------------------------------------------
		// 26/01/2015 ECU generate the notification builder
		// -------------------------------------------------------------------------
		NotificationCompat.Builder notificationBuilder =
		        new NotificationCompat.Builder (theContext)
		        .setSmallIcon (R.drawable.bluetooth_notification_icon)
		        .setContentTitle ("BlueTooth Discovery")
		        .setContentText ("Waiting for Discovery to start");
		// -------------------------------------------------------------------------
		// 26/01/2015 ECU set up the style that will be used for the data
		// -------------------------------------------------------------------------
		NotificationCompat.InboxStyle inboxStyle =
		        new NotificationCompat.InboxStyle();
		
		inboxStyle.setBigContentTitle (theTitle);
		// -------------------------------------------------------------------------
		// 26/01/2015 ECU populate the notification with the data
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < theDevices.size(); theIndex++)
		{
			inboxStyle.addLine (theDevices.get(theIndex).getName());
		}
		// -------------------------------------------------------------------------
		notificationBuilder.setStyle (inboxStyle);
		// -------------------------------------------------------------------------
		// 26/01/2015 ECU if an activity is to be started when the notification is
		//                clicked then do something similar to what is below
		// -------------------------------------------------------------------------
		//Intent intent = new Intent (theContext,SystemInfoActivity.class);
		//intent.setFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP|
		//				 Intent.FLAG_ACTIVITY_SINGLE_TOP);
		//PendingIntent pendingIntent = PendingIntent.getActivity(theContext, 66,
		//								intent, 0);
		//notificationBuilder.setContentIntent(pendingIntent);
		// -------------------------------------------------------------------------
		// 26/01/2015 ECU tell the manager to action the notification
		// -------------------------------------------------------------------------
		theNotificationManager.notify (NOTIFICATION_ID, notificationBuilder.build());
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 21/01/2015 ECU changed to use the method which takes an argument
		// -------------------------------------------------------------------------
		return (Print (discoveredDevices));
	}
	// -----------------------------------------------------------------------------
	public String Print (BluetoothDevice theDevice)
	{
		// -------------------------------------------------------------------------
		// 25/01/2015 ECU added to print details of the bluetooth device
		// -------------------------------------------------------------------------
		return theDevice.getName() + "\n" +
			   "     " + theDevice.getAddress() + "\n" +
			   "          " + typeAsString (theDevice.getBluetoothClass().getMajorDeviceClass());
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public String Print (List<BluetoothDevice> theList)
	{
		// -------------------------------------------------------------------------
		// 06/09/2013 ECU will print details of discovered devices
		// 21/01/2015 ECU add the argument
		// -------------------------------------------------------------------------
		if (theList != null)
		{
			String discoveryString = "Bluetooth Devices found " + theList.size();
			for(BluetoothDevice theDevice : theList)
				discoveryString += "\n" + theDevice.getName();
			
			return discoveryString;
		}
		return "no bluetooth devices";
	}
	// =============================================================================
	void registerBroadcastReceiver (Context theContext,BroadcastReceiver theReceiver,String [] theActions)
	{
		// -------------------------------------------------------------------------
		// 25/01/2015 ECU set up the filter
		// -------------------------------------------------------------------------
		IntentFilter filter = (new IntentFilter (theActions [0]));
		// -------------------------------------------------------------------------
		// 25/01/2015 ECU the first action was added above - now add any additional
		//                actions
		// -------------------------------------------------------------------------
		for (int theIndex = 1; theIndex < theActions.length; theIndex++)
			filter.addAction (theActions [theIndex]);
		// -------------------------------------------------------------------------
		// 25/01/2015 ECU register the required receiver and the associated filter
		// -------------------------------------------------------------------------
		theContext.registerReceiver (theReceiver, filter); 
	}
	// =============================================================================
	public static void SetAlarm (Context theContext,long theFutureTime,int theAlarmType)
	{
		// -------------------------------------------------------------------------
		// 29/12/2015 ECU created to set the bluetooth discovery alarm
		// -------------------------------------------------------------------------
		// 01/12/2015 ECU now set the alarm
		// -------------------------------------------------------------------------	
		AlarmManager theAlarmManager = (AlarmManager)theContext.getSystemService (ALARM_SERVICE);
		Intent alarmIntent = new Intent (theContext, AlarmManagerReceiver.class);
		alarmIntent.putExtra (StaticData.PARAMETER_ALARM_ID,StaticData.ALARM_ID_BLUETOOTH_DISCOVERY);
		// -------------------------------------------------------------------------
		// 02/12/2015 ECU store the mode into the intent
		// -------------------------------------------------------------------------
		alarmIntent.putExtra (StaticData.PARAMETER_ALARM_TYPE,theAlarmType);
		// -------------------------------------------------------------------------
		alarmIntent.putExtra (StaticData.PARAMETER_ALARM_ID,StaticData.ALARM_ID_BLUETOOTH_DISCOVERY);
		// -------------------------------------------------------------------------
		// 14/03/2019 ECU added FLAG_UPDATE_CURRENT
		// -------------------------------------------------------------------------
		alarmPendingIntent = PendingIntent.getBroadcast (theContext,
				StaticData.ALARM_ID_BLUETOOTH_DISCOVERY,alarmIntent,Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_UPDATE_CURRENT);  
		// -------------------------------------------------------------------------
		// 24/12/2015 ECU changed to use the new method
		// -------------------------------------------------------------------------
		Utilities.SetAnExactAlarm (theAlarmManager,(Calendar.getInstance()).getTimeInMillis() + theFutureTime, alarmPendingIntent);
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
	public void Terminate (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 06/09/2013 ECU created - clean up before main app terminates
		// 01/01/2016 ECU added the context as an argument
		// -------------------------------------------------------------------------
		// 06/09/2013 ECU if this device supports bluetooth then stop things
		// -------------------------------------------------------------------------
		if (bluetoothAdapter != null)
		{
			// ---------------------------------------------------------------------
			// 08/09/2013 ECU if in discovery mode then cancel it
			// 21/01/2015 ECU changed to use the method
			// 25/08/2015 ECU added the second 'false' flag
			// 27/08/2015 ECU added the final '200' argument which is not used in
			//                this case
			// 29/12/2015 ECU rearrange because of the redefinition of the Discover
			//                method
			// ---------------------------------------------------------------------
			if (bluetoothAdapter.isDiscovering())
			{
				Discover (false);
			}
			// ---------------------------------------------------------------------
			// 06/09/2013 ECU unregister the receiver for the bluetooth events
			// 23/01/2015 ECU include the try ... catch
			// ---------------------------------------------------------------------
			try
			{
				unregisterReceiver (bluetoothReceiver);
			}
			catch (Exception theException)
			{
				// -----------------------------------------------------------------
				// 23/01/2015 ECU just in case trying to unregister a receiver
				//                which is no longer registered
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 01/01/2016 ECU cancel any alarm that may be active
			// 03/11/2016 ECU add the false to indicate no pending intent cancel
			// ---------------------------------------------------------------------
			Utilities.cancelAnAlarm (theContext,alarmPendingIntent,false);
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static String typeAsString (int theBluetoothType)
	{
		// -------------------------------------------------------------------------
		// 25/01/2015 ECU return as a string a description of the bluetooth device
		//                whose Bluetooth.Device.Major class is given as the
		//                argument
		// -------------------------------------------------------------------------
		switch (theBluetoothType)
		{
			// ---------------------------------------------------------------------
			case BluetoothClass.Device.Major.AUDIO_VIDEO:
							return "audio video";
			// ---------------------------------------------------------------------
			case BluetoothClass.Device.Major.COMPUTER:
							return "computer";
			// ---------------------------------------------------------------------
			case BluetoothClass.Device.Major.HEALTH:
							return "health";
			// ---------------------------------------------------------------------
			case BluetoothClass.Device.Major.IMAGING:
							return "imaging";
			// ---------------------------------------------------------------------
			case BluetoothClass.Device.Major.MISC:
							return "miscellaneous";
			// ---------------------------------------------------------------------
			case BluetoothClass.Device.Major.NETWORKING:
							return "networking";
			// ---------------------------------------------------------------------
			case BluetoothClass.Device.Major.PERIPHERAL:
							return "peripheral";
			// ---------------------------------------------------------------------
			case BluetoothClass.Device.Major.PHONE:
							return "phone";
			// ---------------------------------------------------------------------
			case BluetoothClass.Device.Major.TOY:
							return "toy";
			// ---------------------------------------------------------------------
			case BluetoothClass.Device.Major.WEARABLE:
							return "wearable";
			// ---------------------------------------------------------------------
			case BluetoothClass.Device.Major.UNCATEGORIZED:
							return "uncategorised";
			// ---------------------------------------------------------------------
		    default:
		    				return "unknown device";				
			// ---------------------------------------------------------------------
		}	
	}
	// =============================================================================
	void updateStatusMessage (TextView theTextView,String theStatusMessage)
	{
		// -------------------------------------------------------------------------
		// 30/12/2015 ECU created to update the status message if text view is
		//                defined
		// -------------------------------------------------------------------------
		if (theTextView != null)
			theTextView.setText (theStatusMessage);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

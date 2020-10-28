package com.usher.diboson;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class BluetoothUtilities extends Activity
{
	/* ============================================================================= */
	private static final String TAG					  = "BluetoothUtilities";
	/* ============================================================================= */
	// 06/09/2013 ECU created - to handle all bluetooth utilities
	// 25/01/2015 ECU changed to use the bluetooth details held in the Carer's records
	// 26/08/2015 ECU moved the notification manager here
	// 29/12/2015 ECU major rewrite to use alarms to initiate discovery rather than
	//                a handler
	// 20/11/2019 ECU On some devices, notably Samsung, the same device seems to be
	//                discovered many times so only want to process the first time
	//                found
	// 11/05/2020 ECU changed to use StaticData.REQUEST....
	// 05/08/2020 ECU remove the notification code - use the method in Utilities
	/* ============================================================================= */

	/* ============================================================================= */
	// private data
	// ------------
	static 	Activity					activity;						// 08/09/2013 ECU added
	static	PendingIntent 				alarmPendingIntent	= null;		// 01/01/2016 ECU moved here from set an alarm method
			boolean						announceFlag		= false;	// 25/08/2015 ECU added - whether events are spoken
			List<BluetoothDevice>       discoveredDevices;				// 25/01/2015 ECU changed from String
	static	boolean                     discoveryState		= true;		// 17/08/2020 ECU added
			int							signalStrength;					// 18/04/2020 ECU signal strength
	/* ----------------------------------------------------------------------------- */
	// public data
	// -----------
	BluetoothAdapter			bluetoothAdapter 	= null;				// 21/01/2015 ECU changed to static
	List<BluetoothDevice>		bluetoothDevices 	= new ArrayList<BluetoothDevice>();
																		// 06/09/2013 ECU added - list of discovered bluetooth devices
																		// 25/01/2015 ECU changed from String to BluetoothDevice
	ArrayList<String>			deviceDetails		= new ArrayList<String>();
																		// 25/01/2015 ECU added for display details
																		// 26/04/2020 ECU changed from List<String>
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
		// -------------------------------------------------------------------------
		// 19/11/2019 ECU added @Override, which I had forgotten, - without it was
		//                getting a NPE when trying to unregister in Terminate. This
		//                was detected by a LogCat entry to do with
		//                  'MainActivity has leaked IntentReceiver ...... are you
		//                  missing a call to unregisterReceiver ()'
		// -------------------------------------------------------------------------
		@Override
	    public void onReceive (Context theContext, Intent theIntent)
	    {
	    	// ---------------------------------------------------------------------
	    	// 07/09/2013 ECU created to handle events associated with the bluetooth
	    	//                adapter
	    	// ---------------------------------------------------------------------
	        String localAction = theIntent.getAction ();
	        // ---------------------------------------------------------------------
	        // 07/09/2013 ECU when discovery finds a device
	        // ---------------------------------------------------------------------
	        if (BluetoothDevice.ACTION_FOUND.equals (localAction)) 
	        {
	        	// -----------------------------------------------------------------
	            // 07/09/2013 ECU get the BluetoothDevice object from the Intent
	        	// -----------------------------------------------------------------
	            BluetoothDevice bluetoothDevice = theIntent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
	            // -----------------------------------------------------------------
	            // 18/04/2020 ECU get the signal strength for the 'found' device
	            // -----------------------------------------------------------------
	            signalStrength = theIntent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
	            // -----------------------------------------------------------------
	            // 25/01/2015 ECU changed because 'devices' is now of BluetoothDevice
	            //                instead of String
	            // 20/11/2019 ECU On some devices, notably Samsung, the same device
	            //                seems to be 'found' multiple times so want to
	            //                store only one.
	            // -----------------------------------------------------------------
	            if (!bluetoothDevices.contains (bluetoothDevice))
				{
					// -------------------------------------------------------------
					// 20/11/2019 ECU this found device is not already stored so add
					//                it
					// -------------------------------------------------------------
	            	bluetoothDevices.add (bluetoothDevice);
	            	// -------------------------------------------------------------
	            	// 25/01/2015 ECU indicate the number of devices found
	            	// 26/08/2015 ECU put in the check on 'null'
					// 30/12/2015 ECU update the status message using new method
					// 18/04/2020 ECU changed to use resource
					// -------------------------------------------------------------
					updateStatusMessage (BluetoothActivity.bluetoothStatus,
							theContext.getString (R.string.bluetooth_discovered_devices_number) + bluetoothDevices.size());
					// -------------------------------------------------------------
					// 30/12/2015 ECU tell the activity about the newly found device
					// -------------------------------------------------------------
					if (BluetoothActivity.bluetoothStatus != null)
						BluetoothActivity.announceDevice (theContext, bluetoothDevice);
	            	// -------------------------------------------------------------
	            	// 25/01/2015 ECU add in the device details for display
	            	// 18/04/2020 ECU added the signal strength
	           		// -------------------------------------------------------------
	            	deviceDetails.add (Print (bluetoothDevice,signalStrength));
	            	// -------------------------------------------------------------
	            	// 21/01/2015 ECU try and display the device name
	            	// 23/01/2015 ECU change to handle the new ListView
	            	// -------------------------------------------------------------
	        		if (BluetoothActivity.arrayAdapter != null)
	        			BluetoothActivity.arrayAdapter.notifyDataSetChanged ();
	            	// -------------------------------------------------------------
				}
	        }
	        else
	        // ---------------------------------------------------------------------
	        // 06/09/2013 ECU action when discovery finished
	        // ---------------------------------------------------------------------
	        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals (localAction)) 
	        {
	        	// -----------------------------------------------------------------
	        	// 25/08/2015 ECU make the speaking of the phrase optional
	        	// 18/04/2020 ECU changed to use resource
	        	// -----------------------------------------------------------------
	        	if (announceFlag)
	        		Utilities.SpeakAPhrase (theContext,theContext.getString (R.string.bluetooth_discovery_finished));
	        	// -----------------------------------------------------------------
				// 30/12/2015 ECU update the status message
				// 18/04/2020 ECU changed to use resource
				// -----------------------------------------------------------------
				updateStatusMessage (BluetoothActivity.bluetoothStatus,theContext.getString (R.string.bluetooth_discovery_finished));
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
	        	// 15/04/2020 ECU if needed then call up the bluetooth tracking
	        	//                method
	        	// -----------------------------------------------------------------
	        	if (StaticData.BLUETOOTH_TRACKING)
				{
					// -------------------------------------------------------------
					// 15/04/2020 ECU pass the devices to the tracking method
					// 17/04/2020 ECU added the check on '.bluetoothTracking'
					// -------------------------------------------------------------
					if (PublicData.storedData.bluetoothTracking)
					{
						// ---------------------------------------------------------
						// 17/04/2020 ECU system has been configured to use
						//                bluetooth tracking
						// ---------------------------------------------------------
						BluetoothTracking.DiscoveredDevices (discoveredDevices);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
	        	// -----------------------------------------------------------------
	        	// 26/01/2015 ECU if notification of the discovered devices is
	        	//                wanted then uncomment the following line
	        	// 18/04/2020 ECU changed to use resource
	        	// 05/08/2020 ECU method to use the updated method which does not
	        	//                need 'notification manager' to be supplied as an
	        	//                argument
	        	// -----------------------------------------------------------------
	        	notificationUpdate (theContext,theContext.getString (R.string.bluetooth_discovered_devices),bluetoothDevices);
	        	// -----------------------------------------------------------------
	        	// 29/12/2015 ECU now set up the alarm to get the next discovery
	        	// 23/07/2020 ECU changed to use 'gap'
	        	// -----------------------------------------------------------------
	        	if (discoveryState)
				{
					// -------------------------------------------------------------
					// 17/08/2020 ECU only set the next alarm if 'discovery mode' is
					//                on
					// -------------------------------------------------------------
	        		BluetoothUtilities.SetAlarm (theContext,PublicData.storedData.bluetoothDiscoveryGap,
	        									 StaticData.ALARM_ID_BLUETOOTH_DISCOVERY);
	        		// ------------------------------------------------------------
				}
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
		// 17/11/2019 ECU rearrange the code so that the check on 'null' applies to
		//                all of the code
		//	------------------------------------------------------------------------
		// 17/08/2020 ECU remember the status of the discovery
		// -------------------------------------------------------------------------
		discoveryState = theStartStopFlag;
		// -------------------------------------------------------------------------
		if (bluetoothAdapter != null)
		{
			if (theStartStopFlag)
			{
				// -----------------------------------------------------------------
				// 21/01/2015 ECU start discovery
				// -----------------------------------------------------------------
				// 29/12/2015 ECU if the adapter isn't currently discovering then tell
				//                it to start
				// -----------------------------------------------------------------
				if (!bluetoothAdapter.isDiscovering())
				{
					// -------------------------------------------------------------
					// 21/01/2015 ECU tell the user what is going on
					// 25/08/2015 ECU optional announce that discovery starting
					// -------------------------------------------------------------
					if (announceFlag)
						Utilities.SpeakAPhrase (activity, GetString (R.string.bluetooth_discovery_starting));
					// -------------------------------------------------------------
					// 30/12/2015 ECU update the status message
					// 18/04/2020 ECU change to use resource
					// -------------------------------------------------------------
					updateStatusMessage (BluetoothActivity.bluetoothStatus,GetString (R.string.bluetooth_discovery_starting));
					// -------------------------------------------------------------
					// 06/09/2013 ECU initialise the devices list
					// -------------------------------------------------------------
					bluetoothDevices.clear ();
					// -------------------------------------------------------------
					// 25/01/2015 ECU clear the details
					// -------------------------------------------------------------
					deviceDetails.clear ();
					// -------------------------------------------------------------
					// 23/01/2015 ECU reflect the removal of devices in the ListView
					// -------------------------------------------------------------
					if (BluetoothActivity.arrayAdapter != null)
					{
						// ---------------------------------------------------------
						// 26/04/2020 ECU indicate that the 'locate' button is not
						//                to be shown
						// ---------------------------------------------------------
						BluetoothActivity.arrayAdapter.setLocateButton (false);
						// ---------------------------------------------------------
						BluetoothActivity.arrayAdapter.notifyDataSetChanged ();
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 06/09/2013 ECU start the actual bluetooth discovery
					// -------------------------------------------------------------
					bluetoothAdapter.startDiscovery ();
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 21/01/2015 ECU cancel any discovery
				// -----------------------------------------------------------------
				if (bluetoothAdapter.isDiscovering ())
				{
					bluetoothAdapter.cancelDiscovery ();
				}
			// ---------------------------------------------------------------------
			}
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
				Utilities.popToast (GetString (R.string.bluetooth_not_enabled));
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
					// 11/05/2020 ECU changed to use 'StaticData....'
					// -------------------------------------------------------------
					Intent enableBluetoothIntent = new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
					activity.startActivityForResult (enableBluetoothIntent,StaticData.REQUEST_BLUETOOTH_ENABLE);
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
					// --------------------------------------------------------------
				}
			}
			else
			{
				// -----------------------------------------------------------------
				// 24/01/2015 ECU bluetooth is enabled
				// -----------------------------------------------------------------
				return localBluetoothAdapter;
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 24/01/2015 ECU device does not support bluetooth
			// ---------------------------------------------------------------------
			return null;
			// ---------------------------------------------------------------------
		}					
	}
	// =============================================================================
	static String GetName (BluetoothDevice theDevice)
	{
		// -------------------------------------------------------------------------
		// 21/08/2020 ECU created to return the name of the bluetooth device, if it
		//                is set, or a message if not
		// -------------------------------------------------------------------------
		if (theDevice.getName() != null)
		{
			// ---------------------------------------------------------------------
			// 21/08/2020 ECU a name has been set so return it
			// ---------------------------------------------------------------------
			return theDevice.getName ();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 21/08/2020 ECU no name has been set so indicate this to the caller
			// ----------------------------------------------------------------------
			return StaticData.BLUETOOTH_NO_NAME;
			// ----------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static String [] getNamesOfBondedDevices ()
	{
		// -------------------------------------------------------------------------
		// 12/04/2020 ECU return the names of the bonded devices
		// -------------------------------------------------------------------------
		String [] localNames = null;
		// -------------------------------------------------------------------------
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		// -------------------------------------------------------------------------
		// 12/04/2020 ECU check if this device supports bluetooth
		// -------------------------------------------------------------------------
		if (adapter != null)
		{
			Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
			// ---------------------------------------------------------------------
			// 12/04/2020 ECU check if there are any devices
			//----------------------------------------------------------------------
			if (bondedDevices != null)
			{
				// -----------------------------------------------------------------
				// 12/04/2020 ECU there are devices so generate the names
				// -----------------------------------------------------------------
				// 12/04/2020 ECU initialise the array to contain the names
				// -----------------------------------------------------------------
				localNames = new String [bondedDevices.size()];
				int localIndex = 0;
				// -----------------------------------------------------------------
				// 25/01/2015 ECU changed the logic now that bondedNames is a
				//                List<String> rather than String []
				// -----------------------------------------------------------------
				for (BluetoothDevice device:bondedDevices)
				{
					// -------------------------------------------------------------
					// 12/04/2020 ECU store the device name
					// -------------------------------------------------------------
					localNames [localIndex++] = device.getName();
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 12/04/2020 ECU return the generate names or null if no bluetooth or no
		//                bonded devices
		// -------------------------------------------------------------------------
		return localNames;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	private static String GetString (int theResourceID)
	{
		// -------------------------------------------------------------------------
		// 18/04/2020 ECU created to return the string identified by the resource
		//                id. Did this because there are a lot of calls that are from
		//                a static method and therefore context needs to be used
		// -------------------------------------------------------------------------
		return activity.getString (theResourceID);
		// -------------------------------------------------------------------------
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
		if (PublicData.bluetoothUtilities != null && discoveryState)
		{
			PublicData.bluetoothUtilities.Discover (true);
		}
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
		// 05/08/2020 ECU remove the definition of a local 'notification manager'
		// -------------------------------------------------------------------------
		if (bluetoothAdapter != null)
		{
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
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public boolean isDiscovering ()
	{
		// -------------------------------------------------------------------------
		// 27/08/2020 ECU returns the state of 'discovery' if the adapter has been
		//                set up
		// -------------------------------------------------------------------------
		if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering())
		{
			// ---------------------------------------------------------------------
			// 27/08/2020 ECU adapter has been defined and discovery is in progress
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 27/08/2020 ECU either the adapter has not been set up or discovery is
			//                not in progress
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void notificationUpdate (Context theContext,
										   String theTitle,
										   List<BluetoothDevice> theDevices)
	{
		// -------------------------------------------------------------------------
		// 05/08/2020 ECU changed to use the method in Utilities rather than having
		//                the code in-line here
		// --------------------------------------------------------------------------
		// 05/08/2020 ECU build up the list of discovered devices - needed for the
		//                notification
		// --------------------------------------------------------------------------
		String deviceString = StaticData.BLANK_STRING;

		for (int theIndex = 0; theIndex < theDevices.size(); theIndex++)
		{
			// ---------------------------------------------------------------------
			// 21/08/2020 ECU changed to use the method to get the name to take
			//                account of devices which have no name set
			// ---------------------------------------------------------------------
			deviceString += GetName (theDevices.get(theIndex)) + StaticData.NEWLINE;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 05/08/2020 ECU invoke the general notification method
		// 12/08/2020 ECU change to use the black and white icon
		// -------------------------------------------------------------------------
		Utilities.notification (theContext,
								R.drawable.bluetooth_notification_icon_bw,
								null,
								theTitle,
								deviceString,
								true,
								StaticData.NOTIFICATION_BLUETOOTH);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 21/01/2015 ECU changed to use the method which takes an argument
		// -------------------------------------------------------------------------
		return (Print (discoveredDevices));
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public String Print (BluetoothDevice theDevice)
	{
		// -------------------------------------------------------------------------
		// 25/01/2015 ECU added to print details of the bluetooth device
		// 18/04/2020 ECU if the name is a 'null' then print this as an empty
		//                string. Also change to use StaticData entries
		// 21/08/2020 ECU changed to use the method to return the device name
		//                - takes into account devices with no name set
		// -------------------------------------------------------------------------
		return GetName (theDevice) + StaticData.NEWLINE +
			   StaticData.INDENT + theDevice.getAddress () + StaticData.NEWLINE +
			   StaticData.INDENTx2 + typeAsString (theDevice.getBluetoothClass ().getMajorDeviceClass());
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public String Print (BluetoothDevice theDevice,int theSignalStrength)
	{
		// -------------------------------------------------------------------------
		// 18/04/2020 ECU print the details of the device together with it's signal
		//                strength
		// 18/04/2020 ECU change to use resource
		// -------------------------------------------------------------------------
		return Print (theDevice) + StaticData.NEWLINE +
						StaticData.INDENTx3 + String.format (GetString (R.string.bluetooth_signal_strength_format),theSignalStrength) ;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public String Print (List<BluetoothDevice> theList)
	{
		// -------------------------------------------------------------------------
		// 06/09/2013 ECU will print details of discovered devices
		// 21/01/2015 ECU add the argument
		// 21/08/2020 ECU changed to use the 'GetName' method to take account
		//                of those devices which have no name set
		// -------------------------------------------------------------------------
		if (theList != null)
		{
			String discoveryString = GetString (R.string.bluetooth_discovered_devices_number) + theList.size();
			for(BluetoothDevice theDevice : theList)
				discoveryString += StaticData.NEWLINE + GetName (theDevice);
			
			return discoveryString;
		}
		// -------------------------------------------------------------------------
		return GetString (R.string.bluetooth_no_devices);
		// -------------------------------------------------------------------------
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
		// -------------------------------------------------------------------------
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
		// 09/05/2020 ECU changed to use 'ALARM....FLAGS'
		// -------------------------------------------------------------------------
		alarmPendingIntent = PendingIntent.getBroadcast (theContext,
									StaticData.ALARM_ID_BLUETOOTH_DISCOVERY,
									alarmIntent,
									StaticData.ALARM_PENDING_INTENT_FLAGS);
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
			// 19/11/2019 ECU added theContext to the unreg... - without was getting
			//                a NPE
			// ---------------------------------------------------------------------
			try
			{
				theContext.unregisterReceiver (bluetoothReceiver);
			}
			catch (Exception theException)
			{
				// -----------------------------------------------------------------
				// 23/01/2015 ECU just in case trying to unregister a receiver
				//                which is no longer registered
				// -----------------------------------------------------------------
				Utilities.LogToProjectFile (TAG,"Exception : " + theException);
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

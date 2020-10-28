package com.usher.diboson;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

public class BluetoothActivity extends DibosonActivity
{
	// ===============================================================================
	// 29/06/2013 ECU created
	// 26/08/2015 ECU placed the notification manager into BluetoothUtilities
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 24/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 30/12/2015 ECU changed the role of the activity to be basically a monitor
	//                of the bluetooth utilities which, optionally, can be initialised when
	//                the app is started to monitor for carer visits
	// 20/11/2019 ECU This activity stands in it's own right but if the 'initiate
	//                discovery ...' in settings then this activity needs to be aware
	//                because the 'broadcast receiver' will already have been
	//                registered
	// 24/04/2020 ECU added the 'tracked' devices button
	// 26/04/2020 ECU changed to use BluetoothListAdapter
	// 11/05/2020 ECU changed to use StaticData.REQUEST....
	// 17/08/2020 ECU take out the 'Find Bluetooth Server' button and connect to
	//                a bluetooth device by doing a 'long click' on the selected
	//                device
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//================================================================================
	/* =============================================================================== */
	//private static final String TAG					  = "BlueToothActivity";
	/* =============================================================================== */
	/* ======================================================================= */
	private static final String 		SERVER_NAME = "DIBOSON-DELL-LA";
	/* ======================================================================= */
	public static   BluetoothListAdapter
										arrayAdapter;					// 23/01/2015 ECU added
	private			ListView 			bluetoothDevices;				// 21/01/2015 ECU added
																		// 23/01/2015 ECU changed to ListView
	private         boolean             bluetoothDiscovery	= false;	// 30/12/2015 ECU placed here - used to be
																		//                in public data
	public static 	TextView 			bluetoothStatus		= null;
																		// 26/08/2015 ECU add setting to null
	private 		Button 				bondedDevicesButton;
	private static 	boolean 			connectionStatus 	= false;
	private     	BluetoothHandler	bluetoothHandler    = new BluetoothHandler ();
	private static	BluetoothDevice 	serverDevice	 	= null;
	private 		Button 				trackedDevicesButton;
	/* ======================================================================= */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 24/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 28/11/2016 ECU call method to set the screen up
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true,true,false);
			// ---------------------------------------------------------------------	
			setContentView (R.layout.activity_blue_tooth);
			// ---------------------------------------------------------------------
			// 20/04/2014 ECU include the bonded devices button
			// ---------------------------------------------------------------------
			bondedDevicesButton = (Button)findViewById 	(R.id.bluetooth_bonded_devices);
			bondedDevicesButton.setOnClickListener 		(buttonOnClickListener);
			// ---------------------------------------------------------------------
			// 24/04/2020 ECU include the 'tracked'' devices button
			// ---------------------------------------------------------------------
			trackedDevicesButton = (Button)findViewById 	(R.id.bluetooth_tracked_devices);
			trackedDevicesButton.setOnClickListener 		(buttonOnClickListener);
			// ---------------------------------------------------------------------    
			bluetoothStatus  = (TextView)findViewById (R.id.bluetooth_status);
			// ---------------------------------------------------------------------
			// 21/01/2015 ECU added the view for devices
			// ---------------------------------------------------------------------
			bluetoothDevices  	= (ListView) findViewById (R.id.bluetooth_device_list);
			arrayAdapter 		= new BluetoothListAdapter (this,
															PublicData.bluetoothUtilities.deviceDetails,
															R.layout.bluetooth_list_adapter_row);
			bluetoothDevices.setAdapter (arrayAdapter);
			// ---------------------------------------------------------------------
			// 17/08/2020 ECU set the 'on item' click listener
			// ---------------------------------------------------------------------
			bluetoothDevices.setOnItemLongClickListener (itemLongClickListener);
			// ---------------------------------------------------------------------
			// 06/09/2013 ECU enable bluetooth if not already enabled
			// 30/12/2015 ECU check if bluetooth adapter has been obtained already
			//            ECU by default it is assumed that the bluetooth adapter and
			//                the event listener will have already been set up
			// ---------------------------------------------------------------------
			if (PublicData.bluetoothUtilities.bluetoothAdapter == null)
			{	
				// -----------------------------------------------------------------
				// 30/12/2015 ECU the bluetooth utilities are not running yet
				// 27/08/2020 ECU changed to use the resource
				// -----------------------------------------------------------------
				Utilities.popToast (getString (R.string.bluetooth_manually_initialise));
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 30/12/2015 ECU indicate that want to have events announced
				// -----------------------------------------------------------------
				PublicData.bluetoothUtilities.announceFlag = true;
				// -----------------------------------------------------------------
				// 18/08/2020 ECU tell user about trying to connect to a bluetooth
				//                device
				// -----------------------------------------------------------------
				Utilities.SpeakAPhraseAndDisplay (getString (R.string.bluetooth_click_to_connect));
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 24/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}	
	}
	/* ============================================================================= */
	 private Button.OnClickListener buttonOnClickListener = new Button.OnClickListener()
	 {
	 	// -------------------------------------------------------------------------
		 @Override
		 public void onClick(View view) 
		 {
			switch (view.getId())
			{
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case R.id.bluetooth_bonded_devices:
					// -------------------------------------------------------------
					Intent intent = new Intent (getBaseContext(),BondedDevicesActivity.class);
	      			startActivityForResult (intent,0);
	      			// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case R.id.bluetooth_tracked_devices:
					// -------------------------------------------------------------
					// 24/04/2020 ECU want to displayed the 'tracked devices'
					// -------------------------------------------------------------
					displayTrackedDevices ();
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				default:
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
			}			
		}
	};
	// =============================================================================
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// -------------------------------------------------------------------------
		getMenuInflater().inflate (R.menu.bluetooth, menu);
		// -------------------------------------------------------------------------
		// 05/06/2013 ECU used the method to build menu
		// -------------------------------------------------------------------------
		return true;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
    public void onDestroy()
    {
		// -------------------------------------------------------------------------
		// 06/09/2013 ECU if this device supports bluetooth then stop things
		// -------------------------------------------------------------------------
		// 23/01/2015 ECU try and terminate any other tasks
		// 01/01/2016 ECU changed to pass through the context
		// 19/11/2019 ECU unless the broadcast receiver was registered using
		//                options in this activity then the Terminate should
		//                not be called
		// 20/11/2019 ECU if this activity has not registered the broadcast
		//                receiver then do not try and unregister it
		// -------------------------------------------------------------------------
		if (!PublicData.storedData.bluetoothDiscovery)
		{
			PublicData.bluetoothUtilities.Terminate (this);
		}
		// -------------------------------------------------------------------------
		// 30/12/2015 ECU indicate that do not want to have events announced
		// -------------------------------------------------------------------------
		PublicData.bluetoothUtilities.announceFlag = false;
		// -------------------------------------------------------------------------
        super.onDestroy();
        // -------------------------------------------------------------------------
    }
	/* ============================================================================= */
	private AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener()
	{
		// -------------------------------------------------------------------------
		@Override
		public boolean onItemLongClick (AdapterView<?> parent, View view,int position, long id)
		{
			// ---------------------------------------------------------------------
			// 17/08/2020 ECU indicate try and connect to the device that was 'long
			//                clicked'
			// 27/08/2020 ECU only allow if the adapter is not discovering
			// ---------------------------------------------------------------------
			if (!PublicData.bluetoothUtilities.isDiscovering())
			{
				// -----------------------------------------------------------------
				// 27/08/2020 ECU not in discovery mode so can try and connect
				// -----------------------------------------------------------------
				BluetoothRun bluetoothRun = new BluetoothRun (position);
				new Thread (bluetoothRun).start ();
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 27/08/2020 ECU discovering so warn the user
				// ------------------------------------------------------------------
				Utilities.popToastAndSpeak (getString (R.string.bluetooth_discovering));
				// ------------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	public boolean onOptionsItemSelected (MenuItem item)
	{
		// -------------------------------------------------------------------------
		// 21/01/2015 ECU added
		// -------------------------------------------------------------------------
		switch (item.getItemId())
		{
			case R.id.bluetooth_discover:
				// -----------------------------------------------------------------			
				bluetoothDiscovery = !bluetoothDiscovery;
				// -----------------------------------------------------------------
				// 06/09/2013 ECU try and get bluetooth discovery started if required
				// 29/12/2015 ECU added the true flag to indicate that want announcements
				//                of actions that are being taken
			    // -----------------------------------------------------------------
				if (bluetoothDiscovery)
				{
				  	if (PublicData.bluetoothUtilities.Initialise (getBaseContext(),true))
				  	{
				   		// ---------------------------------------------------------
				   		// 21/01/2015 ECU now start a discovery
				  		// 25/08/2015 ECU added second 'true' to announce discovery
				  		//                events
				  		// 27/08/2015 ECU added the final '200' which is the delay
				  		//                in milliSeconds before the discovery starts
						// ---------------------------------------------------------
						// 29/12/2015 ECU set up the alarm that will start the bluetooth
						//                discovery
						// 23/07/2020 ECU changed to use stored gap
						// ---------------------------------------------------------
						BluetoothUtilities.SetAlarm (this,PublicData.storedData.bluetoothDiscoveryGap,
														 StaticData.ALARM_ID_BLUETOOTH_DISCOVERY);
						// ---------------------------------------------------------
				   		
				   	}
				}
				else
				{
					// -------------------------------------------------------------
					// 21/01/2015 ECU stop the discovery
					// 23/11/2016 ECU Note - the false indicates that the discovery
					//                       is to stop
					// -------------------------------------------------------------
					PublicData.bluetoothUtilities.Discover (false);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				return true;
			// ---------------------------------------------------------------------
		}
		return true;
	}
	// =============================================================================
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) 
	{
		// -------------------------------------------------------------------------
		// 28/05/2013 ECU clear the displayed menu
		// -------------------------------------------------------------------------
		menu.clear();
		// -------------------------------------------------------------------------
		// 20/11/2019 ECU if the 'initiate discover' option is on then do not add the
		//                discovery option
		// -------------------------------------------------------------------------
		if (!PublicData.storedData.bluetoothDiscovery)
		{
			menu.add (0,R.id.bluetooth_discover,0,(!bluetoothDiscovery ? R.string.start_bluetooth_discovery
					                                                   : R.string.stop_bluetooth_discovery));
		}
		// -------------------------------------------------------------------------	
		return true;
	}
	// =============================================================================
	public static void announceDevice (Context theContext,BluetoothDevice theBluetoothDevice)
	{
		// -------------------------------------------------------------------------
		// 30/12/2015 ECU created to be called from the utilities to announce the
		//                finding of a bluetooth device
        // -------------------------------------------------------------------------
        // 21/01/2015 ECU check if the server has been found
		// 24/07/2016 ECU put in the check on null
        // -------------------------------------------------------------------------
        if ((theBluetoothDevice.getName() != null) && theBluetoothDevice.getName().equalsIgnoreCase (SERVER_NAME))
        {
        	// ---------------------------------------------------------------------
        	// 05/11/2013 ECU used the standard popToast
        	// 11/05/2020 ECU changed to use format
        	// ---------------------------------------------------------------------
			Utilities.popToast (String.format (theContext.getString (R.string.bluetooth_discovery_format),
												theContext.getString (R.string.server_found),
												theBluetoothDevice.getName (),
												theBluetoothDevice.getAddress ()));
        	// ---------------------------------------------------------------------
        	serverDevice = theBluetoothDevice;
        	// ---------------------------------------------------------------------
        }
	}
	/* ============================================================================= */
	private void checkPairedDevices (Context theContext,BluetoothAdapter theBluetoothAdapter,String theServer)
	{
		// -------------------------------------------------------------------------
		// 11/05/2020 ECU changed to use 'format'
		// -------------------------------------------------------------------------
		bluetoothStatus.setText (String.format (theContext.getString (R.string.bluetooth_checking_format),SERVER_NAME));
		// -------------------------------------------------------------------------
		Set<BluetoothDevice> pairedDevices = theBluetoothAdapter.getBondedDevices();
		// -------------------------------------------------------------------------
		if (pairedDevices.size () > 0)
		{
			// ---------------------------------------------------------------------
			// 29/06/2013 ECU loop through the paired devices looking for the required server
			// ---------------------------------------------------------------------
			for (BluetoothDevice device : pairedDevices) 
			{
				// -----------------------------------------------------------------
				// 06/11/2013 ECU use the standard toast
				// 11/05/2020 ECU changed to use '.format'
				// -----------------------------------------------------------------
				Utilities.popToast (String.format (getString (R.string.bluetooth_discovery_format),
													getString(R.string.paired_device_discovered),
													device.getName(),
													device.getAddress()));
				// ------------------------------------------------------------------
				if (device.getName().equalsIgnoreCase(theServer))
				{
					// -------------------------------------------------------------
					// 06/11/2013 ECU use the standard toast
					// -------------------------------------------------------------
					Utilities.popToast (String.format (getString (R.string.bluetooth_discovery_format),
														getString(R.string.server_found),
														device.getName(),
														device.getAddress()));
					// -------------------------------------------------------------
			        serverDevice = device;
			        // -------------------------------------------------------------
			        // 11/05/2020 ECU changed to use resource
			        // -------------------------------------------------------------
					bluetoothStatus.setText (SERVER_NAME + getString (R.string.bluetooth_already_paired));
			        // -------------------------------------------------------------
			        // 29/06/2013 ECU have what we want so can stop looping
			        // -------------------------------------------------------------
			        break;
			        // -------------------------------------------------------------
				}
			}
		}		
	}
	/* ============================================================================= */
	private void connectToBluetoothDevice (Context theContext,BluetoothDevice theBluetoothDevice)
	{
		// -------------------------------------------------------------------------
		BluetoothSocket theBluetoothSocket;
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
		 	// 11/05/2020 ECU changed to use resources
			// ---------------------------------------------------------------------
			BluetoothMessage (theContext.getString (R.string.bluetooth_server_connecting) + theBluetoothDevice.getName());
			// ---------------------------------------------------------------------
			Method localMethod  = theBluetoothDevice.getClass().getMethod ("createRfcommSocket", new Class[] {int.class});
	         
	        theBluetoothSocket = (BluetoothSocket) localMethod.invoke (theBluetoothDevice, 1);
	         	         
	        theBluetoothSocket.connect ();
	         	        
	        BluetoothMessage (theContext.getString (R.string.bluetooth_server_connected) + theBluetoothDevice.getName());
	        // ---------------------------------------------------------------------
	        // 29/06/2013 ECU indication already connected
	        // ---------------------------------------------------------------------
	        connectionStatus = true;
	        // ---------------------------------------------------------------------
	        // 31/12/2013 ECU added the next two statements
	        // 12/05/2020 ECU changed to use a resource
	      	// --------------------------------------------------------------------
	        writeDataToSocket (theContext,theBluetoothSocket,theContext.getString (R.string.bluetooth_message));
	        // ---------------------------------------------------------------------
	        theBluetoothSocket.close ();
	        // ---------------------------------------------------------------------
	     } 
		 catch (Exception theException) 
	     {
	     	// ---------------------------------------------------------------------
			BluetoothMessage (String.format
			 		(theContext.getString (R.string.bluetooth_failed_to_connect_format),
			 			theBluetoothDevice.getAddress()));
			// ---------------------------------------------------------------------
	     }
	     // ------------------------------------------------------------------------
	}
	// =============================================================================
	void displayTrackedDevices ()
	{
		// -------------------------------------------------------------------------
		// 24/04/2020 ECU created to display the currently stored 'tracked' devices
		// -------------------------------------------------------------------------
		if ((PublicData.storedData.storedBluetoothDevices != null) &&
				(PublicData.storedData.storedBluetoothDevices.size() > 0))
		{
			arrayAdapter.clear ();
			// ---------------------------------------------------------------------
			// 26/04/2020 ECU tell the adapter to show 'locate' button
			// ---------------------------------------------------------------------
			arrayAdapter.setLocateButton (true);
			// ---------------------------------------------------------------------
			for (BluetoothTrackingData trackedDevice : PublicData.storedData.storedBluetoothDevices)
			{
				arrayAdapter.add (trackedDevice.Print ());
			}
			// ---------------------------------------------------------------------
			// 24/04/2020 ECU now want to display the status
			// ---------------------------------------------------------------------
			bluetoothStatus.setText (getString (R.string.bluetooth_tracked_devices));
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 24/04/2020 ECU there are no stored 'tracked' devices
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (getString (R.string.bluetooth_no_tracked_devices),true);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	private void writeDataToSocket (Context theContext,BluetoothSocket theSocket,String theString)
	{
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			OutputStream outputStream = theSocket.getOutputStream();
			outputStream.write(theString.getBytes());
			outputStream.close();
			// ---------------------------------------------------------------------
		}
		catch (IOException theException) 
		{ 
			 BluetoothMessage (theContext.getString (R.string.writeDataToSocket) + StaticData.NEWLINE + theException);
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */

	// =============================================================================
	class BluetoothHandler extends Handler
	{
		// -------------------------------------------------------------------------
		// 17/08/2020 ECU created to update display data
		// -------------------------------------------------------------------------
		@Override
		public void handleMessage (Message theMessage)
		{
			// ---------------------------------------------------------------------
			// 17/08/2020 ECU change to switch on the type of message received
			//                which is in '.what'
			// ---------------------------------------------------------------------
			switch (theMessage.what)
			{
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_DISPLAY:
					bluetoothStatus.setText ((String) theMessage.obj);
					break;
				// -----------------------------------------------------------------
				default:
					break;
				// -----------------------------------------------------------------
			}
		}
	};
	// =============================================================================
	void BluetoothMessage (String theMessage)
	{
		// -------------------------------------------------------------------------
		Message bluetoothMessage = bluetoothHandler.obtainMessage(StaticData.MESSAGE_DISPLAY,theMessage);
		bluetoothHandler.sendMessage (bluetoothMessage);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	class BluetoothRun implements Runnable
	{
		// -------------------------------------------------------------------------
		// 17/08/2020 ECU remember the position of the device that is to be
		//                connected to
		// -------------------------------------------------------------------------
		int devicePosition;
		// -------------------------------------------------------------------------
		BluetoothRun (int theDevicePosition)
		{
			// ---------------------------------------------------------------------
			// 17/08/2020 ECU remember the device position for later use
			// ---------------------------------------------------------------------
			this.devicePosition = theDevicePosition;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		public void run()
		{
			// ---------------------------------------------------------------------
			// 17/08/2020 ECU try and connect to the device being pointed to
			// ---------------------------------------------------------------------
			connectToBluetoothDevice (getBaseContext(),
					PublicData.bluetoothUtilities.bluetoothDevices.get (devicePosition));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

package com.usher.diboson;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

public class BlueToothActivity extends DibosonActivity 
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
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//================================================================================
	/* =============================================================================== */
	//private static final String TAG					  = "BlueToothActivity";
	/* =============================================================================== */
	/* ======================================================================= */
	private static final int 			REQUEST_BLUETOOTH_ENABLE = 123;
	private static final String 		SERVER_NAME = "EDUSHER_LAPTOP";
	/* ======================================================================= */
	public static	ArrayAdapter<String>
										arrayAdapter;					// 23/01/2015 ECU added
	private 		Button 				bluetoothButton;
	private			ListView 			bluetoothDevices;				// 21/01/2015 ECU added
																		// 23/01/2015 ECU changed to ListView
	private         boolean             bluetoothDiscovery	= false;	// 30/12/2015 ECU placed here - used to be
																		//                in public data
	public static 	TextView 			bluetoothStatus		= null;
																		// 26/08/2015 ECU add setting to null
	private 		Button 				bondedDevicesButton;
	private static 	boolean 			connectionStatus 	= false;
	private static	BluetoothDevice 	serverDevice	 	= null;
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
			setContentView(R.layout.activity_blue_tooth);
			// ---------------------------------------------------------------------
			// 29/06/2013 ECU set up the button to connect to the server
			// ---------------------------------------------------------------------
			bluetoothButton = (Button)findViewById (R.id.bluetooth_server_connect);
			bluetoothButton.setOnClickListener (buttonOnClickListener);		
			// ---------------------------------------------------------------------
			// 20/04/2014 ECU include the bonded devices button
			// ---------------------------------------------------------------------
			bondedDevicesButton = (Button)findViewById (R.id.bluetooth_bonded_devices);
			bondedDevicesButton.setOnClickListener (buttonOnClickListener);	
			// ---------------------------------------------------------------------    
			bluetoothStatus  = (TextView)findViewById (R.id.bluetooth_status);
			// ---------------------------------------------------------------------
			// 21/01/2015 ECU added the view for devices
			// ---------------------------------------------------------------------
			bluetoothDevices  	= (ListView) findViewById (R.id.bluetooth_device_list);
			arrayAdapter 		= new ArrayAdapter<String> (this,android.R.layout.simple_list_item_1, 
															PublicData.bluetoothUtilities.deviceDetails);
			bluetoothDevices.setAdapter (arrayAdapter);
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
				// -----------------------------------------------------------------
				Utilities.popToast("Manually initialise using the menu option");
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 30/12/2015 ECU indicate that want to have events announced
				// -----------------------------------------------------------------
				PublicData.bluetoothUtilities.announceFlag = true;
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
		 @Override
		 public void onClick(View view) 
		 {	 
			switch (view.getId())
			{
				// -----------------------------------------------------------------
				case R.id.bluetooth_bonded_devices:
					Intent intent = new Intent (getBaseContext(),BondedDevicesActivity.class);
	      			startActivityForResult (intent,0);
					break;
				// -----------------------------------------------------------------
				case R.id.bluetooth_server_connect:
					if (connectionStatus)
					{
						bluetoothStatus.setText ("Already connected");
					}
					else
					{
						// ---------------------------------------------------------
						// 01/01/2016 ECU added the checks on null
						// ---------------------------------------------------------
						if ((PublicData.bluetoothUtilities != null) && (PublicData.bluetoothUtilities.bluetoothAdapter != null))
						{
							if (!PublicData.bluetoothUtilities.bluetoothAdapter.isEnabled()) 
							{ 
								// -------------------------------------------------
								// 06/11/2013 ECU use the standard toast
								// -------------------------------------------------
								Utilities.popToast (getString (R.string.bluetooth_is_not_enabled));
								// ------------------------------------------------- 
								// 29/06/2013 ECU want to try and enable the bluetooth
								// -------------------------------------------------  
								Intent enableBluetoothIntent = new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
								startActivityForResult (enableBluetoothIntent,REQUEST_BLUETOOTH_ENABLE);
								// -------------------------------------------------
							}
							else
							{
								// -------------------------------------------------
								// 06/11/2013 ECU use the standard toast
								// -------------------------------------------------	
								Utilities.popToast ( "bluetooth is enabled\n" +
													 "name : " + PublicData.bluetoothUtilities.bluetoothAdapter.getName() + StaticData.NEWLINE +
													 "address : " + PublicData.bluetoothUtilities.bluetoothAdapter.getAddress());
								// -------------------------------------------------  
								// 29/06/2013 ECU set the name of this device - needs 
								//                BLUETOOTH_ADMIN permission in manifest
								// -------------------------------------------------  
								PublicData.bluetoothUtilities.bluetoothAdapter.setName ("Diboson");
								// -------------------------------------------------
								// 29/06/2013 ECU want to find the server on bluetooth 
								//                check if it is already paired before 
								//                doing a discovery
								// ------------------------------------------------- 
								checkPairedDevices (getBaseContext(),PublicData.bluetoothUtilities.bluetoothAdapter,SERVER_NAME);
						  
								if (serverDevice == null)
								{
									// ---------------------------------------------
									// 29/06/2013 ECU the server is not paired so try and discover it
									// ---------------------------------------------  
									PublicData.bluetoothUtilities.bluetoothAdapter.startDiscovery();
								
									Utilities.popToast(getBaseContext(), "starting to discover");
							  
									bluetoothStatus.setText ("Trying to discover " + SERVER_NAME);
								}
								else
								{
									// ---------------------------------------------
									// 29/06/2013 ECU have the server device so connect
									//            to it
									// --------------------------------------------- 
									connectToBluetoothDevice (getBaseContext(),serverDevice);	  
								}
							}
						}
					}
					break;
				// -----------------------------------------------------------------
			}			
		}
	};
	// =============================================================================
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.bluetooth, menu);
		// -------------------------------------------------------------------------
		// 05/06/2013 ECU used the method to build menu
		// -------------------------------------------------------------------------
		return true;
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
		// -------------------------------------------------------------------------
		PublicData.bluetoothUtilities.Terminate (this);
		// -------------------------------------------------------------------------
        super.onDestroy();
    }
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
						// ---------------------------------------------------------
						BluetoothUtilities.SetAlarm (this,StaticData.BLUETOOTH_DISCOVERY_TIME, StaticData.ALARM_ID_BLUETOOTH_DISCOVERY);
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
		menu.add (0,R.id.bluetooth_discover,0,(!bluetoothDiscovery ? R.string.start_bluetooth_discovery 
				                                                   : R.string.stop_bluetooth_discovery));
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
        	// ---------------------------------------------------------------------
        	Utilities.popToast (theContext.getString(R.string.server_found) + StaticData.NEWLINE +
        						"name : " + theBluetoothDevice.getName() + StaticData.NEWLINE +
        						"address : " + theBluetoothDevice.getAddress());
        	// ---------------------------------------------------------------------
        	serverDevice = theBluetoothDevice;
        	// ---------------------------------------------------------------------
        }
	}
	/* ============================================================================= */
	private void checkPairedDevices (Context theContext,BluetoothAdapter theBluetoothAdapter,String theServer)
	{
		
		bluetoothStatus.setText ("Checking if " + SERVER_NAME + " is already paired");
		
		Set<BluetoothDevice> pairedDevices = theBluetoothAdapter.getBondedDevices();
		
		if (pairedDevices.size() > 0) 
		{
			// ---------------------------------------------------------------------
			// 29/06/2013 ECU loop through the paired devices looking for the required server
			// ---------------------------------------------------------------------
			for (BluetoothDevice device : pairedDevices) 
			{
				// -----------------------------------------------------------------
				// 06/11/2013 ECU use the standard toast
				// -----------------------------------------------------------------
				Utilities.popToast (getString(R.string.paired_device_discovered) + StaticData.NEWLINE +
	                       "name : " + device.getName() + StaticData.NEWLINE +
	                       "address : " + device.getAddress());
				
				if (device.getName().equalsIgnoreCase(theServer))
				{
					// -------------------------------------------------------------
					// 06/11/2013 ECU use the standard toast
					// -------------------------------------------------------------
					Utilities.popToast (getString(R.string.server_found) + StaticData.NEWLINE +
		                       			"name : " + device.getName() + StaticData.NEWLINE +
		                       			"address : " + device.getAddress());
					
			        serverDevice = device;
			        
					bluetoothStatus.setText (SERVER_NAME + " is already paired");
			        // -------------------------------------------------------------
			        // 29/06/2013 ECU have what we want so can stop looping
			        // -------------------------------------------------------------
			        break;
				}
			}
		}		
	}
	/* ============================================================================= */
	private static void connectToBluetoothDevice (Context theContext,BluetoothDevice theBluetoothDevice)
	{
		BluetoothSocket theBluetoothSocket;
		
		 try 
		 {
			 // --------------------------------------------------------------------
			 bluetoothStatus.setText ("Connecting to server " + theBluetoothDevice.getName());
			 // --------------------------------------------------------------------
			 Method myMethod  = theBluetoothDevice.getClass().getMethod ("createRfcommSocket", new Class[] {int.class});
	         
	         theBluetoothSocket = (BluetoothSocket) myMethod.invoke(theBluetoothDevice, 1);
	         	         
	         theBluetoothSocket.connect();
	         	        
	         bluetoothStatus.setText ("Connected to server " + theBluetoothDevice.getName());
	         // --------------------------------------------------------------------
	         // 29/06/2013 ECU indication already connected
	         // --------------------------------------------------------------------
	         connectionStatus = true;
	         // --------------------------------------------------------------------
	         // 31/12/2013 ECU added the next two statements
	      	 // --------------------------------------------------------------------       
	         writeDataToSocket (theContext,theBluetoothSocket,"hello there");
	         
	         theBluetoothSocket.close ();
	     } 
		 catch (Exception theException) 
	     { 
			 Utilities.popToast (theContext.getString(R.string.connect_exception) + StaticData.NEWLINE + theException);	    	 
	     } 
	}
	/* ============================================================================= */
	private static void writeDataToSocket (Context theContext,BluetoothSocket theSocket,String theString)
	{
		try
		{
			OutputStream outputStream = theSocket.getOutputStream();
			outputStream.write(theString.getBytes());
			outputStream.close();
		}
		catch (IOException theException) 
		{ 
			 Utilities.popToast (theContext.getString(R.string.writeDataToSocket) + StaticData.NEWLINE + theException);	   
		}
	}
	/* ======================================================================= */
}

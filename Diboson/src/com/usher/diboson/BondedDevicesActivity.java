package com.usher.diboson;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BondedDevicesActivity extends DibosonActivity 
{
	// =============================================================================
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 24/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 10/05/2020 ECU if bluetooth is not enabled then enable it because bonded
	//                devices are only displayed when enabled
	// =============================================================================
	
	/* ============================================================================= */
	//private static final String TAG = "BondedDevicesActivity";
	/* ============================================================================= */
	BluetoothAdapter			adapter = null;
	ArrayAdapter<String>		arrayAdapter;
	Set<BluetoothDevice>		bondedDevices;
	ListView					bondedList;						// 25/01/2015 ECU changed from
	                                                            //                String []
	List<String>				bondedNames = new ArrayList<String>();
	List<BluetoothDevice>		devices = new ArrayList<BluetoothDevice>();
	TextView					discoveredDevices;
	/* ============================================================================= */
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
			setContentView (R.layout.activity_bonded_devices);
			// ---------------------------------------------------------------------
			discoveredDevices = (TextView) findViewById (R.id.discovered_devices);
			// ---------------------------------------------------------------------
			discoveredDevices.setText (getString (R.string.bluetooth_remote_controller_click));
			// --------------------------------------------------------------------
			bondedList = (ListView) findViewById (R.id.bonded_listview);
     		// ---------------------------------------------------------------------
			// 17/04/2014 ECU get the default bluetooth adapter
			// ---------------------------------------------------------------------
			adapter = BluetoothAdapter.getDefaultAdapter();
			// ---------------------------------------------------------------------
			// 17/04/2014 ECU check if this device supports bluetooth
			// ---------------------------------------------------------------------
			if (adapter != null)
			{
				// -----------------------------------------------------------------
				// 10/05/2020 ECU check if bluetooth is enabled - if not then enable it
				// -----------------------------------------------------------------
				if (!adapter.isEnabled ())
				{
					// -------------------------------------------------------------
					// 10/05/2020 ECU the adapter is currently disabled - enable it
					//                Do it via the intent because there is a delay
					//                between the request to enable and when the
					//                adapter is enabled.
					// -------------------------------------------------------------
					Intent enableBluetoothIntent = new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult (enableBluetoothIntent,StaticData.REQUEST_BLUETOOTH_ENABLE);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 10/05/2020 ECU the adapter is enabled so display the devices
					// -------------------------------------------------------------
					DisplayBondedDevices ();
					// -------------------------------------------------------------
				}
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
	// =============================================================================
	@Override
	public void onActivityResult (int theRequestCode, int theResultCode, Intent theIntent)
	{
		// -------------------------------------------------------------------------
		// 10/05/2020 ECU handle the result from the bluetooth enablement
		// -------------------------------------------------------------------------
		if (theRequestCode == StaticData.REQUEST_BLUETOOTH_ENABLE)
		{
			// ---------------------------------------------------------------------
			// 10/05/2020 ECU check if the user agreed to enable bluetooth
			// ---------------------------------------------------------------------
			if (theResultCode == RESULT_OK)
			{
				// -----------------------------------------------------------------
				// 10/05/2020 ECU bluetooth should be up and running so get and display
				//                the 'bonded devices'
				// -----------------------------------------------------------------
				DisplayBondedDevices ();
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 10/05/2020 ECU the user did not agree to enable bluetooth so just
				//                terminate this activity
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return true;
	}
	/* ============================================================================ */
	@Override
    public void onDestroy()
    {		
		super.onDestroy();
    }
	/* ============================================================================= */
	private OnItemClickListener clickListener = new OnItemClickListener() 
	{
		@Override
	    public void onItemClick (AdapterView<?> parent, View view,int position, long id)
	    {
			PublicData.storedData.remoteMACAddress = devices.get (position).getAddress ();
			// ---------------------------------------------------------------------
	        Utilities.popToast(getString (R.string.bluetooth_remote_controller) +
	        		PublicData.storedData.remoteMACAddress,true); 
	        // ---------------------------------------------------------------------
	        // 20/04/2014 ECU just finish the activity
	        // ---------------------------------------------------------------------
	        finish ();
	        // ---------------------------------------------------------------------   
	    }
	};
	/* ============================================================================= */
	void DisplayBondedDevices ()
	{
		// -----------------------------------------------------------------
		bondedDevices = adapter.getBondedDevices();
		// -----------------------------------------------------------------
		if (bondedDevices.size() > 0)
		{
			// -------------------------------------------------------------
			// 25/01/2015 ECU changed the logic now that bondedNames is a
			//                List<String> rather than String []
			// -------------------------------------------------------------
			for (BluetoothDevice device : bondedDevices)
			{
				// ---------------------------------------------------------
				// 25/01/2015 ECU changed to use ..typeAsString rather than a
				//                local method
				// 10/05/2020 ECU changed to use static
				// ---------------------------------------------------------
				bondedNames.add (device.getName() + StaticData.NEWLINE +
						StaticData.INDENT + device.getAddress() + StaticData.NEWLINE +
						StaticData.INDENTx2 + BluetoothUtilities.typeAsString (device.getBluetoothClass().getMajorDeviceClass()));
				// ---------------------------------------------------------
				// 17/04/2014 ECU add the device into the list
				// ---------------------------------------------------------
				devices.add (device);
				// ---------------------------------------------------------
			}
			// -------------------------------------------------------------
			arrayAdapter = new ArrayAdapter<String> (this,android.R.layout.simple_list_item_1, bondedNames);

			bondedList.setAdapter (arrayAdapter);

			bondedList.setOnItemClickListener (clickListener);
			// -------------------------------------------------------------
		}
	}
	// =============================================================================
}

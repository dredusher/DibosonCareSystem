package com.usher.diboson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BondedDevicesActivity extends DibosonActivity 
{
	// =============================================================================
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 24/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
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
			setContentView(R.layout.activity_bonded_devices);
		
			discoveredDevices = (TextView) findViewById (R.id.discovered_devices);
		
			discoveredDevices.setText ("Click the device connected to remote controller");

			bondedList = (ListView) findViewById (R.id.bonded_listview);
     
			// -------------------------------------------------------------------------
			// 17/04/2014 ECU get the default bluetooth adapter
			// -------------------------------------------------------------------------
			adapter = BluetoothAdapter.getDefaultAdapter();
			// -------------------------------------------------------------------------
			// 17/04/2014 ECU check if this device supports bluetooth
			// -------------------------------------------------------------------------
			if (adapter != null)
			{
				bondedDevices = adapter.getBondedDevices();
    		
				if (bondedDevices.size() > 0)
				{
					// -----------------------------------------------------------------
					// 25/01/2015 ECU changed the logic now that bondedNames is a
					//                List<String> rather than String []
					// -----------------------------------------------------------------   			
					for (BluetoothDevice device : bondedDevices)
					{
						// -------------------------------------------------------------
						// 25/01/2015 ECU changed to use ..typeAsString rather than a
						//                local method
						// -------------------------------------------------------------
						bondedNames.add (device.getName() + "\n  " + 
											device.getAddress() + "\n    " + 
											BluetoothUtilities.typeAsString (device.getBluetoothClass().getMajorDeviceClass()));   
						// -------------------------------------------------------------
						// 17/04/2014 ECU add the device into the list
						// -------------------------------------------------------------
						devices.add (device);
					}
					// -----------------------------------------------------------------
					arrayAdapter = new ArrayAdapter<String> (this,android.R.layout.simple_list_item_1, bondedNames);

					bondedList.setAdapter(arrayAdapter);
    	        
					bondedList.setOnItemClickListener (clickListener);
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
			PublicData.storedData.remoteMACAddress = devices.get(position).getAddress();
			// ---------------------------------------------------------------------
	        Utilities.popToast("Remote controller MAC address has been set to\n" + 
	        		PublicData.storedData.remoteMACAddress,true); 
	        // ---------------------------------------------------------------------
	        // 20/04/2014 ECU just finish the activity
	        // ---------------------------------------------------------------------
	        finish ();
	        // ---------------------------------------------------------------------   
	    }
	};
	/* ============================================================================= */
}

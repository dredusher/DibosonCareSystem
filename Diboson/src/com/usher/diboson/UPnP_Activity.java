package com.usher.diboson;

import java.util.ArrayList;
import java.util.List;

import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.DeviceList;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.ServiceList;
import org.cybergarage.upnp.UPnP;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class UPnP_Activity extends DibosonActivity implements OnItemClickListener
{
	// =============================================================================
	// 07/09/2016 ECU create to interface with CyberGarage to control UPnP devices
	//            ECU IMPORTANT IMPORTANT IMPORTANT IMPORTANT IMPORTANT IMPORTANT
	//                ========= ========= ========= ========= ========= =========
	//                This activity is using 'cybergarage' classes but these are
	//                supplied by the Belkin WeMo SDK and I believe it has been
	//                modified to only detect WeMo devices.
	// -----------------------------------------------------------------------------
	//private final static String		TAG					= "UPnP_Actvity";
	// =============================================================================
	private final static String SERVICE_WANTED				= "basicevent1";
	// =============================================================================

	// =============================================================================
			static  Adapter			adapter			= null;
	public  static  Context			context;
					ControlPoint	controlPoint;
					DeviceList		deviceList;
			static 	ListView 		listView 		= null;
	public 	static 	UPnP_MessageHandler	
									messageHandler;
	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// -------------------------------------------------------------------------
			// 28/11/2016 ECU added the call to the method to set full screen
			// -------------------------------------------------------------------------
			Utilities.SetUpActivity (this,true,true,false);
			// ---------------------------------------------------------------------
			// 07/09/2016 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_we_mo);
			// ---------------------------------------------------------------------
			// 08/09/2016 ECU remember the context for later use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			listView 		= (ListView) findViewById (R.id.list_wemo);
			// ---------------------------------------------------------------------
			// 07/09/2016 ECU set up the adapter for the listview field
			// ---------------------------------------------------------------------
			adapter = new Adapter (getApplicationContext(),0,PublicData.upnpDevices);
			listView.setAdapter (adapter);
			// ---------------------------------------------------------------------
			// 07/09/2016 ECU set up the listeners
			// ---------------------------------------------------------------------
			listView.setOnItemClickListener (this);
			// ---------------------------------------------------------------------
	        // 07/09/2016 ECU set up the message handler
	        // ---------------------------------------------------------------------
	        messageHandler = new UPnP_MessageHandler ();
	        // ---------------------------------------------------------------------
	        // 07/09/2016 ECU indicate only using IPv4
	        // ---------------------------------------------------------------------
	        UPnP.setEnable (UPnP.USE_ONLY_IPV4_ADDR);
	        // ---------------------------------------------------------------------
	        // 07/09/2016 ECU start up the task which will create and start the
	        //                control point
	        // ---------------------------------------------------------------------
	        new ControlPointTask().execute ();
	        // ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 07/09/2016 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	@Override
	public void onBackPressed ()
	{
		// -------------------------------------------------------------------------
		// 09/09/2016 ECU just finish this activity
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
		// 09/09/2016 ECU now call the super for this method
		// -------------------------------------------------------------------------
		super.onBackPressed();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
    public void onDestroy()
    {
		// -------------------------------------------------------------------------
		// 09/09/2016 ECU added
		// -------------------------------------------------------------------------
		// 09/09/2016 ECU stop the UPnP control point
		// -------------------------------------------------------------------------
		controlPoint.stop ();
		// -------------------------------------------------------------------------
		super.onDestroy();
    }
	// =============================================================================
	@Override
	public void onItemClick (AdapterView<?> list, View view, int position, long id) 
	{
		// -------------------------------------------------------------------------
		// 06/09/2016 ECU get the device at the clicked position
		// -------------------------------------------------------------------------
		//UPnPDevice upnpDevice = ((UPnPListItem)view).getDevice();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public class Adapter extends ArrayAdapter<UPnPDevice> 
	{
		// -------------------------------------------------------------------------
		private List<UPnPDevice> adapterDevices;
		// -------------------------------------------------------------------------
		public Adapter (Context theContext, int theResource, List<UPnPDevice> theDevices) 
		{
			super (theContext,theResource,theDevices);
			adapterDevices = theDevices;
		}
		// -------------------------------------------------------------------------
		public View getView (int thePosition, View theConvertView, ViewGroup theParent) 
		{
			if (theConvertView == null) 
			{
				theConvertView = new UPnPListItem (getApplicationContext());
			}
			((UPnPListItem) theConvertView).setDevice (adapterDevices.get(thePosition));
			
			return theConvertView;
		}
		// -------------------------------------------------------------------------
		public void Refresh (List<UPnPDevice> theDevices)
		{
			clear ();
			// ---------------------------------------------------------------------
			// 10/11/2014 ECU the following 'addAll' came in at API 11
			// ---------------------------------------------------------------------
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
			{
				addAll (theDevices);	
			}
			else
			{
				// -----------------------------------------------------------------
				// 10/11/2014 ECU cannot use the 'addAll' method so add each item one
				//                at a time
				// -----------------------------------------------------------------
				for (int theIndex  = 0; theIndex < theDevices.size(); theIndex++)
					add (theDevices.get(theIndex));
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------	
	}
	// =============================================================================
    private class ControlPointTask extends AsyncTask <Object, Object, Object>
    {
		@Override
		protected Object doInBackground (Object... arg0) 
		{
			// ---------------------------------------------------------------------
			// 07/09/2016 ECU create the control point for the UPnP devices
			// ---------------------------------------------------------------------
			controlPoint = new UPnP_ControlPoint ();
		    // ---------------------------------------------------------------------
			// 07/09/2016 ECU start up the control point
			// ---------------------------------------------------------------------
			controlPoint.start ();
			// ---------------------------------------------------------------------
			// 12/09/2016 ECU indicate that searching for devices
			// ---------------------------------------------------------------------
			MessageHandler.popToastAndSpeak (context.getString (R.string.scanning));
			// ---------------------------------------------------------------------
			// 07/09/2016 ECU can now update the display in 5 secs
			// ---------------------------------------------------------------------
			messageHandler.sendEmptyMessageDelayed (StaticData.MESSAGE_DISPLAY,(5 * 1000));
			// ---------------------------------------------------------------------
		 	return null;
		 	// ---------------------------------------------------------------------
		}
    }
    // =============================================================================
	@SuppressLint("HandlerLeak")
	class UPnP_MessageHandler extends Handler
	{
		// -------------------------------------------------------------------------
		@Override
		public void handleMessage (Message theMessage) 
		{   
			// ---------------------------------------------------------------------
			// 05/05/2015 ECU change to switch on the type of message received
			//                which is in '.what'
			// ---------------------------------------------------------------------
			switch (theMessage.what)
			{
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_DISPLAY:
					// -------------------------------------------------------------
					// 07/09/2016 ECU get the list of devices from CyberGarage
					// -------------------------------------------------------------
					deviceList = controlPoint.getDeviceList ();
			        // -------------------------------------------------------------
					// 07/09/2016 ECU build up the objects
					// -------------------------------------------------------------
					if (deviceList.size() > 0)
					{
						PublicData.upnpDevices = new ArrayList<UPnPDevice> ();
						
						for (int index = 0; index < deviceList.size(); index++) 
				        {
				        	PublicData.upnpDevices.add (new UPnPDevice (deviceList.getDevice(index)));
				        } 
						// ---------------------------------------------------------
						// 08/09/2016 ECU now switch on all notifications
						// ---------------------------------------------------------
						switchOnNotifications ();
						// ---------------------------------------------------------
						// 08/09/2016 ECU update the display
						// ---------------------------------------------------------
						adapter.Refresh (PublicData.upnpDevices);
						// ---------------------------------------------------------
					}
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_REFRESH:
					// -------------------------------------------------------------
					// 07/09/2016 ECU the data has changed so refresh the data
					// -------------------------------------------------------------
					if (PublicData.upnpDevices.size() > 0)
					{
						// ---------------------------------------------------------
						adapter.Refresh (PublicData.upnpDevices);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_NOTIFICATION_START:
					// -------------------------------------------------------------
					// 09/09/2016 ECU the data has changed so refresh the data
					// -------------------------------------------------------------
					controlPoint.search ();
					// -------------------------------------------------------------
					this.sendEmptyMessageDelayed (StaticData.MESSAGE_DISPLAY,(5 * 1000));
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		}
		/* ------------------------------------------------------------------------- */
	};
	// =============================================================================
	void switchOnNotifications ()
	{
		if (PublicData.upnpDevices.size () > 0)
		{
			for (int index = 0; index < PublicData.upnpDevices.size (); index++)
			{
				Device device = PublicData.upnpDevices.get (index).upnpDevice; 
				// -----------------------------------------------------------------
				ServiceList serviceList = device.getServiceList();
				// -----------------------------------------------------------------
				// 07/09/2016 ECU loop through all returned services
				// -----------------------------------------------------------------
				for (int indexx = 0; indexx < serviceList.size(); indexx++)
				{
					final Service service = serviceList.getService (indexx);
					// -------------------------------------------------------------
					// 08/09/2016 ECU check whether the subscription is already on
					// -------------------------------------------------------------
					if ((service.getControlURL().contains (SERVICE_WANTED)) && !service.isSubscribed())
					{
						// ---------------------------------------------------------
						// 07/09/2016 ECU create a thread to perform the subscribe 
						//			  request
						// ---------------------------------------------------------
						Thread subscribeThread = new Thread ()
						{
							// -----------------------------------------------------
							@Override
							public void run ()
							{
								// -------------------------------------------------
								// 07/09/2016 ECU ask the control point to subscribe
								//  			  to the events for the service
								// -------------------------------------------------
								controlPoint.subscribe (service);
								// -------------------------------------------------
							}
						};
						// ---------------------------------------------------------
						// 07/09/2016 ECU start up the thread to cause the subscription
						// ---------------------------------------------------------
						subscribeThread.start ();
					}
					// -------------------------------------------------------------
				}
			}
		}
	}
	// =============================================================================
}

package com.usher.diboson;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.belkin.wemo.localsdk.WeMoDevice;
import com.belkin.wemo.localsdk.WeMoSDKContext;
import com.belkin.wemo.localsdk.WeMoSDKContext.NotificationListener;

import java.util.ArrayList;

public class WeMoService extends Service implements NotificationListener
{
	// =============================================================================
	// 16/02/2015 ECU created as a service to provide an interface between the WeMo
	//                devices and the rest of the app
	//
	//                The service is based on the SDK supplied by Belkin and which
	//				  provides four static methods, as follows :-
	//
	//				  refreshListOfWemoDevicesOnLAN
	//						This method will refresh the list of all devices on the
	//						LAN. It will generate a notification when the list is
	//						completed.
	// 				  getListOfWeMoDevicesOnLan
	//						This method will return the list of WeMo devices which
	//						have already been discovered.
	//				  getDeviceState
	//						This method will return the state of the specified device
	//				  setDeviceState
	//						This method will enable the state of the specified device
	//                      to be set.
	//
	//				  To make use of the SDK it is necessary to :-
	//
	//						1) initialise the SDK (WeMoSDKContext)
	// 						2) add a listener for notifications provided by the SDK
	//                      3) process notifications from the SDK
	//                      4) optionally request a refresh of WeMo devices on the
	//                         LAN
	// =============================================================================
	
	// =============================================================================
	//private final static String	TAG = "WeMoService";
	// =============================================================================
	
	// =============================================================================
	private final static int REFRESH_PERIOD	= 5 * 60 * 1000;	// 5 minutes in milliseconds
	private final static int SLEEP_PERIOD 	= 30 * 1000;		// 30 secs in milliseconds
	// =============================================================================
	
	// =============================================================================
	// 17/02/2015 ECU declare all required variables
	// -----------------------------------------------------------------------------
	private static  boolean 				activityListening	= false;	
			static  String					lastSpokenMessage	= null;		// 22/06/2015 ECU added
	private static 	WeMoSDKContext 			mWeMoSDKContext 	= null;
					RefreshHandler			refreshHandler;
					int						refreshHandlerLoops	= 60;
	private static  WeMoDevice 				wemoDevice;						// 05/02/2016 ECU added
	private static	ArrayList<WeMoDevice> 	wemoDevices 		= new ArrayList<WeMoDevice> (); 
	// =============================================================================
	
	// =============================================================================
	@Override
	public IBinder onBind (Intent arg0) 
	{
		// -------------------------------------------------------------------------
		// 17/02/2015 ECU called to return the communication channel to the service
		// -------------------------------------------------------------------------
		return null;
	}
	// ============================================================================= 
	@Override
	public void onCreate ()
	{
		// -------------------------------------------------------------------------
		// 17/02/2015 ECU called the main onCreate
		// -------------------------------------------------------------------------
		super.onCreate ();
		// -------------------------------------------------------------------------
		// 17/02/2015 ECU called when the service is first created
		// -------------------------------------------------------------------------
		// 16/02/2015 ECU set up the WeMo SDK and add the listener
		// -------------------------------------------------------------------------
		mWeMoSDKContext = new WeMoSDKContext (getApplicationContext());	
		// -------------------------------------------------------------------------
		// 17/02/2015 ECU add the listener that will receive WeMo events
		// -------------------------------------------------------------------------
		mWeMoSDKContext.addNotificationListener (this);
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
	@Override
	public int onStartCommand (Intent intent,int flags, int startId) 
	{
		// -------------------------------------------------------------------------
		// 17/02/2015 ECU called when the service is started
		// -------------------------------------------------------------------------
		// 17/02/2015 ECU start a refresh of WeMo devices on the network
		// 23/02/2015 ECU changed to use the method
		// -------------------------------------------------------------------------
		requestARefresh ();
		// -------------------------------------------------------------------------
		// 20/02/2015 ECU start up the refresh handler to check for any devices
		// 22/06/2015 ECU changed to use 'delayedMessage' instead of 'sleep'
		// -------------------------------------------------------------------------
		refreshHandler = new RefreshHandler ();
		refreshHandler.delayedMessage (StaticData.MESSAGE_SLEEP,SLEEP_PERIOD);
		// -------------------------------------------------------------------------
		// 20/02/2015 ECU indicate to the activity that it can run
		// -------------------------------------------------------------------------
		WeMoActivity.serviceRunning = true;
		// -------------------------------------------------------------------------
	    return Service.START_STICKY;
	    // -------------------------------------------------------------------------
	}
	// ============================================================================= 
	@Override
	public void onDestroy() 
	{
		// -------------------------------------------------------------------------
		// 17/02/2015 ECU called when the service is destroyed
		// -------------------------------------------------------------------------
		// 17/02/2015 ECU stop the actions associated with the WeMo context
		// -------------------------------------------------------------------------
		mWeMoSDKContext.stop ();
		// -------------------------------------------------------------------------
		// 24/02/2015 ECU indicate that the service has stopped running
		// -------------------------------------------------------------------------
		WeMoActivity.serviceRunning = false;
		// -------------------------------------------------------------------------
		super.onDestroy();
	}
	// =============================================================================
	@Override
	public void onNotify (final String theEvent,final String theUDN)
	{
		// -------------------------------------------------------------------------
		// 17/02/2015 ECU this method is called when an event happens with the
		//                Belkin WeMo devices
		// -------------------------------------------------------------------------
		// 16/02/2015 ECU handle each type of event
		// -------------------------------------------------------------------------
		if (theEvent.equals (WeMoSDKContext.REFRESH_LIST)) 
		{
			// ---------------------------------------------------------------------
			// 16/02/2015 ECU get the list of device 'udns' found on the 
			//                network
			// 23/02/2015 ECU changed to call the method
			// ---------------------------------------------------------------------
			wemoDevices = getDevices ();
			// ---------------------------------------------------------------------
			// 22/06/2015 ECU check if any devices found - if not then reset the
			//                'last spoken phrase'
			// ----------------------------------------------------------------------
			if (wemoDevices.size() == 0)
				lastSpokenMessage = null;
			// ---------------------------------------------------------------------
			// 20/02/2015 ECU if appropriate then let the activity know about
			//                the refresh event
			// ---------------------------------------------------------------------
			if (activityListening)
			{
				WeMoActivity.notifyChange (theEvent,wemoDevices);
			}
			// ---------------------------------------------------------------------
		} 
		// -------------------------------------------------------------------------
		// 05/02/2016 ECU if the associated WeMo device is null then will not need
		//                to process subsequent notifications
		//            ECU other than the refresh notification all others have an
		//                associated device
		// ------------------------------------------------------------------------
		else 
		{
			// ---------------------------------------------------------------------
			// 05/02/2016 ECU get the associated device from the UDN
			// ---------------------------------------------------------------------
			wemoDevice = mWeMoSDKContext.getWeMoDeviceByUDN (theUDN);
			// ---------------------------------------------------------------------
			// 05/02/2016 ECU just in case check that the device exists and check
			//                the type of notification, yet again, just in case
			// ---------------------------------------------------------------------
			if (wemoDevice != null)
			{
				if (theEvent.equals (WeMoSDKContext.ADD_DEVICE) || 
					theEvent.equals (WeMoSDKContext.REMOVE_DEVICE) ||
					theEvent.equals (WeMoSDKContext.CHANGE_STATE) || 
					theEvent.equals (WeMoSDKContext.SET_STATE)) 
				{
					// -------------------------------------------------------------
					// 05/02/2016 ECU everything seems O so process the event
					// -------------------------------------------------------------
					ProcessTheEvent (theEvent,theUDN);
					// -------------------------------------------------------------
				} 
				// -----------------------------------------------------------------
			}
		}
	}
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class RefreshHandler extends Handler
    {
		@Override
        public void handleMessage (Message theMessage) 
        {  
			switch (theMessage.what)
        	{
    			// -----------------------------------------------------------------
    			case StaticData.MESSAGE_REFRESH:
    				//--------------------------------------------------------------
    				// 22/06/2015 ECU process a WeMo refresh
    				// -------------------------------------------------------------
    				mWeMoSDKContext.refreshListOfWeMoDevicesOnLAN ();
    				// -------------------------------------------------------------
    				delayedMessage (StaticData.MESSAGE_REFRESH,REFRESH_PERIOD);
    				// -------------------------------------------------------------
    				break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_SLEEP:
        			// -------------------------------------------------------------
        			// 20/02/2015 ECU if there are no devices then initiate a refresh every
        			//                minute until devices are found or until the maximum
        			//                number of loops
        			// -------------------------------------------------------------
        			if (wemoDevices.size() == 0 && refreshHandlerLoops-- > 0)
        			{
        				// ---------------------------------------------------------
        				// 21/02/2015 ECU if the activity is running then indicate what
        				//                is going on
        				// ---------------------------------------------------------
        				if (activityListening)
        					WeMoActivity.message ("Still trying to discover devices (" + refreshHandlerLoops + ")");
        				// ---------------------------------------------------------
        				// 20/02/2015 ECU start a discover
        				// ---------------------------------------------------------
        				mWeMoSDKContext.refreshListOfWeMoDevicesOnLAN ();
        				// ---------------------------------------------------------
        				delayedMessage (StaticData.MESSAGE_SLEEP,SLEEP_PERIOD);
        				// ---------------------------------------------------------
        			}
        			else
        			{
        				//----------------------------------------------------------
        				// 22/06/2015 ECU now set up a repeatable 'refresh' loop
        				// ---------------------------------------------------------
        				delayedMessage (StaticData.MESSAGE_REFRESH,REFRESH_PERIOD);
        				// ---------------------------------------------------------
        			}
        			break;
        	}
        }
        // -------------------------------------------------------------------------
        public void delayedMessage (int theMessageType,long delayMillis)
        {
            this.removeMessages (theMessageType);
            sendMessageDelayed (obtainMessage(theMessageType), delayMillis);
        }
        // -------------------------------------------------------------------------
    }
	// =============================================================================
	public static void activityListeningState (boolean theState)
	{
		// -------------------------------------------------------------------------
		// 18/02/2015 ECU indicates if the WeMo activity is to receive notifications
		// -------------------------------------------------------------------------
		activityListening = theState;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static ArrayList<WeMoDevice> getDevices ()
	{
		// ---------------------------------------------------------------------
		// 16/02/2015 ECU get the list of device 'udns' found on the 
		//                network
		// 23/02/2015 ECU turn into a method that can be called from the activity
		// ---------------------------------------------------------------------
		ArrayList<String> devicesOnLAN = mWeMoSDKContext.getListOfWeMoDevicesOnLAN ();
		// ---------------------------------------------------------------------
		// 16/02/2015 ECU initialise the array which will contain the 
		//                details of all WeMo devices found
		// ---------------------------------------------------------------------
		ArrayList<WeMoDevice> 	localWeMoDevices = new ArrayList<WeMoDevice> (); 
		// ---------------------------------------------------------------------
		// 16/02/2015 ECU loop for all discovered devices and build up
		//                the list
		// ---------------------------------------------------------------------
		for (String deviceUDN : devicesOnLAN) 
		{
			WeMoDevice listDevice = mWeMoSDKContext.getWeMoDeviceByUDN (deviceUDN);
			// -----------------------------------------------------------------
			// 16/02/2015 ECU check that the device is present and that
			//                it is available
			// -----------------------------------------------------------------
			if (listDevice != null && listDevice.isAvailable ()) 
			{
				// -------------------------------------------------------------
				// 16/02/2015 ECU add the device details into the list
				// -------------------------------------------------------------
				localWeMoDevices.add (listDevice);
				// -------------------------------------------------------------
			}
		}
		return localWeMoDevices;
	}
	// =============================================================================
	public static WeMoSDKContext getWeMoSDKContext ()
	{
		// -------------------------------------------------------------------------
		// 18/02/2015 ECU return the context for the WeMo SDK
		// -------------------------------------------------------------------------
		return mWeMoSDKContext;
	}
	// =============================================================================
	void ProcessTheEvent (String theEvent,String theUDN)
	{
		// -------------------------------------------------------------------------
		// 05/02/2015 ECU get the current list of devices on the LAN
		// -------------------------------------------------------------------------
		wemoDevices = getDevices ();
		// -------------------------------------------------------------------------
		// 16/02/2015 ECU a device has changed its state so make sure
		//                that the display reflects this
		// 05/02/2016 ECU scan the list to find the entry for the one causing this
		//                event
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < wemoDevices.size(); theIndex++)
		{
			// ---------------------------------------------------------------------
			// 05/02/2016 ECU get a device from the list
			// ---------------------------------------------------------------------
			wemoDevice = wemoDevices.get (theIndex);
			// ---------------------------------------------------------------------
			// 05/02/2016 ECU check that the UDN's match just in case
			// ---------------------------------------------------------------------
			if (wemoDevice.getUDN ().equals (theUDN))
			{
				// -------------------------------------------------------------
				// 18/02/2015 ECU if appropriate then let the activity know about
				//                the status change
				// --------------------------------------------------------------
				if (activityListening)
				{
					WeMoActivity.notifyChange (theEvent,wemoDevice);							
				}
				// -------------------------------------------------------------
				// 18/02/2015 ECU only speak if the state is changed
				// 22/06/2015 ECU added 'SET_STATE' in the check
				// 05/02/2016 ECU removed the 'if' check because theEvent is filtered
				//                by the 'case'
				// -------------------------------------------------------------
				String localMessage = "the " + wemoDevice.getFriendlyName() + " is " +
						((wemoDevice.getState().equalsIgnoreCase ("0") ? "off" : "on"));
				// ---------------------------------------------------------
				// 22/06/2015 ECU check if the message is the same as last 
				//                one spoken or has not been defined yet
				// ---------------------------------------------------------
				if (lastSpokenMessage == null || !lastSpokenMessage.equals (localMessage))
				{
					// -----------------------------------------------------
					// 22/06/2015 ECU speak this new message
					// -----------------------------------------------------
					Utilities.SpeakAPhrase (getBaseContext(),localMessage);
					// -----------------------------------------------------
					// 22/06/2015 ECU remember this message
					// -----------------------------------------------------
					lastSpokenMessage = localMessage;
					// -----------------------------------------------------
				}
				// ---------------------------------------------------------
				break;
			}
		}
	}
	// =============================================================================
	public static void requestARefresh ()
	{
		// -------------------------------------------------------------------------
		// 20/02/2015 ECU called by the activity to request a refresh of devices
		// -------------------------------------------------------------------------
		// 23/02/2015 ECU now start the actual refresh
		// -------------------------------------------------------------------------
		mWeMoSDKContext.refreshListOfWeMoDevicesOnLAN ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static ArrayList<WeMoDevice> returnDevices ()
	{
		// -------------------------------------------------------------------------
		// 18/02/2015 ECU return the current list of WeMo devices
		// -------------------------------------------------------------------------
		return wemoDevices;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean SetDeviceState (String theFriendlyName,boolean theState)
	{
		// -------------------------------------------------------------------------
		// 18/02/2015 ECU set the device state 
		//                   true  =  ON  =  WeMoDevice.WEMO_DEVICE_ON
		//                   false =  OFF =  WeMoDevice.WEMO_DEVICE_OFF
		// -------------------------------------------------------------------------
		for (int theDevice = 0; theDevice < wemoDevices.size(); theDevice++)
		{
			// ---------------------------------------------------------------------
			if (wemoDevices.get(theDevice).getFriendlyName().equalsIgnoreCase (theFriendlyName))
			{
				mWeMoSDKContext.setDeviceState ((theState ? WeMoDevice.WEMO_DEVICE_ON : WeMoDevice.WEMO_DEVICE_OFF), 
													wemoDevices.get(theDevice).getUDN ());
				return true;
				// -----------------------------------------------------------------
			}
		}
		return false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================

	// ==============================================================================
	public static boolean validation (int theArgument)
	{
		// ------------------------------------------------------------------------
		// 11/08/2020 ECU created to check if this service is allowed to run
		// ------------------------------------------------------------------------
		return PublicData.storedData.wemoHandling && WeMoActivity.validation(0);
		// ------------------------------------------------------------------------
	}
	// =============================================================================
}

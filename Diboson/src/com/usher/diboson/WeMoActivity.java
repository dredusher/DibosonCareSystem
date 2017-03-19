package com.usher.diboson;

import java.util.ArrayList;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.belkin.wemo.localsdk.WeMoDevice;
import com.belkin.wemo.localsdk.WeMoSDKContext;

public class WeMoActivity extends DibosonActivity implements OnItemClickListener
{
	// ============================================================================= 
	//private final static String TAG = "WeMoActivity";
	// =============================================================================
	// 16/02/2015 ECU created to handle WeMo devices
	// 09/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// =============================================================================
	private static  String			ALL_DEVICES		= "everything";
	private static  int				REFRESH_TIME	= 100 * 1000;	// 05/02/2016 ECU added
	private static  String			SWITCH_OFF		= "off";
	private static  String			SWITCH_ON		= "on";
	// -----------------------------------------------------------------------------
	private static  Adapter			adapter;
	private static	String			event;
	private static 	ListView 		listView 		= null;
	private static	boolean			refreshDisplay	= false;
			static  RefreshHandler	refreshHandler;
	public  static  boolean         serviceRunning	= false;		// 20/02/2015 ECU added
			static 	WeMoDevice		wemoDevice;
	// =============================================================================
	
	// =============================================================================
	static ArrayList<WeMoDevice>	wemoDevices = new ArrayList<WeMoDevice> (); 
	// =============================================================================
	
	// =============================================================================
	private static WeMoSDKContext 	mWeMoSDKContext = null;
	// =============================================================================
	
	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity(this);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_we_mo);
			// ---------------------------------------------------------------------
			// 20/02/2015 ECU only continue if the service is running
			// ---------------------------------------------------------------------
			if (serviceRunning)
			{
				// -----------------------------------------------------------------
				listView 		= (ListView) findViewById (R.id.list_wemo);
				// -----------------------------------------------------------------
				// 16/02/2015 ECU set up the listeners
				// -----------------------------------------------------------------
				listView.setOnItemClickListener (this);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 20/02/2015 ECU the service isn't running so just finish
				// 21/06/2015 ECU changed to use resource string
				// 16/06/2016 ECU added the 'true'
				// -----------------------------------------------------------------
				Utilities.popToast (getString (R.string.cannot_start_no_service),true);
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been recreated after having been
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
		// 18/02/2015 ECU terminate this activity
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// -------------------------------------------------------------------------
		// Inflate the menu; this adds items to the action bar if it is present.
		// -------------------------------------------------------------------------
		getMenuInflater().inflate (R.menu.we_mo, menu);
		return true;
	}
	// =============================================================================
	@Override
	protected void onDestroy() 
	{
		// -------------------------------------------------------------------------
		// 18/02/2015 ECU tell the service that activity is no longer ready to receive
		//                notifications
		// -------------------------------------------------------------------------
		WeMoService.activityListeningState (false);
		// -------------------------------------------------------------------------
		super.onDestroy();
	}
	// =============================================================================
	@Override
	public void onItemClick (AdapterView<?> list, View view, int position, long id) 
	{
		// -------------------------------------------------------------------------
		// 17/02/2015 ECU get the WeMo device at the clicked position
		// -------------------------------------------------------------------------
		WeMoDevice device = ((WeMoListItem)view).getDevice(); 
		// -------------------------------------------------------------------------
		// 17/02/2015 ECU get the type of WeMo device and its state
		// -------------------------------------------------------------------------
		String deviceType 	= device.getType();
		String deviceState 	= device.getState().split("\\|")[0];
		// -------------------------------------------------------------------------
		// 17/02/2015 ECU only handling switches, insight, and light switches
		// -------------------------------------------------------------------------
		if (deviceType.equals (WeMoDevice.SWITCH) ||
			deviceType.equals (WeMoDevice.LIGHT_SWITCH) ||
			deviceType.equals (WeMoDevice.INSIGHT)) 
		{
			String newState = WeMoDevice.WEMO_DEVICE_ON;
			
			if (deviceState.equals (WeMoDevice.WEMO_DEVICE_ON) ||
				deviceState.equals (WeMoDevice.WEMO_DEVICE_STAND_BY)) 
			{
				newState = WeMoDevice.WEMO_DEVICE_OFF;
			}
			// ---------------------------------------------------------------------
			// 17/02/2015 ECU set the state of the device
			// ---------------------------------------------------------------------
			mWeMoSDKContext.setDeviceState (newState,device.getUDN());
			// ---------------------------------------------------------------------
			// 17/02/2015 ECU default the display to undefined as it will be updated
			//                when the notification is received
			// ---------------------------------------------------------------------
			((WeMoListItem)view).setState (WeMoDevice.WEMO_DEVICE_UNDEFINED);
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public boolean onOptionsItemSelected (MenuItem item)
	{
				
		switch (item.getItemId())
		{
			// =====================================================================
			case R.id.request_a_refresh:
				// -----------------------------------------------------------------
				WeMoService.requestARefresh ();
				// -----------------------------------------------------------------
				break;
			// =====================================================================
			case R.id.reset_all_timers:
				// -----------------------------------------------------------------
				// 25/02/2015 ECU clear all of the timers
				// -----------------------------------------------------------------
				PublicData.storedData.wemoTimers = new ArrayList<WeMoTimer>();
				// -----------------------------------------------------------------
				break;
			// =====================================================================
		}	
		return true;
	}
	// =============================================================================
	@Override
	protected void onStart() 
	{	
		// -------------------------------------------------------------------------
		// 18/02/2015 ECU get the context from the service
		// -------------------------------------------------------------------------
		mWeMoSDKContext = WeMoService.getWeMoSDKContext();
		// -------------------------------------------------------------------------
		// 18/02/2015 ECU get the WeMo devices which have been discovered
		// -------------------------------------------------------------------------
		wemoDevices = WeMoService.returnDevices ();
		// -------------------------------------------------------------------------
		adapter = new Adapter (getApplicationContext(),0,wemoDevices);
		listView.setAdapter (adapter);			
		// -------------------------------------------------------------------------
		// 18/02/2015 ECU start up the refresh handler
		// -------------------------------------------------------------------------
		refreshHandler = new RefreshHandler ();
		refreshHandler.sleep (1000);
		// -------------------------------------------------------------------------
		// 18/02/2015 ECU tell the service that activity is ready to receive
		//                notifications
		// -------------------------------------------------------------------------
		WeMoService.activityListeningState (true);
		// -------------------------------------------------------------------------
		super.onStart();
	}
	// =============================================================================
	public class Adapter extends ArrayAdapter<WeMoDevice> 
	{
		// -------------------------------------------------------------------------
		private ArrayList<WeMoDevice> devices;
		// -------------------------------------------------------------------------
		public Adapter (Context theContext, int theResource, ArrayList<WeMoDevice> theDevices) 
		{
			super (theContext,theResource,theDevices);
			devices = theDevices;
		}
		// -------------------------------------------------------------------------
		public View getView (int thePosition, View theConvertView, ViewGroup theParent) 
		{
			if (theConvertView == null) 
			{
				theConvertView = new WeMoListItem (getApplicationContext());
			}
			((WeMoListItem) theConvertView).setDevice (devices.get(thePosition));
			
			return theConvertView;
		}
		// -------------------------------------------------------------------------
		public void Refresh (ArrayList<WeMoDevice> theWeMoDevices)
		{
			clear ();
			// ---------------------------------------------------------------------
			// 10/11/2014 ECU the following 'addAll' came in at API 11
			// ---------------------------------------------------------------------
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
			{
				addAll (theWeMoDevices);	
			}
			else
			{
				// -----------------------------------------------------------------
				// 10/11/2014 ECU cannot use the 'addAll' method so add each item one
				//                at a time
				// -----------------------------------------------------------------
				for (int theIndex  = 0; theIndex < theWeMoDevices.size(); theIndex++)
					add (theWeMoDevices.get(theIndex));
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------	
	}
	// =============================================================================
	
	
	// =============================================================================
	// 16/02/2015 ECU declare all public methods
	// =============================================================================
	public static String DisplayAllDevices ()
	{
		String display = "" ;
		
		for (int theDevice = 0; theDevice < wemoDevices.size(); theDevice++)
		{
			display += "Friendly Name : " + wemoDevices.get(theDevice).getFriendlyName() + "\n" +
					         "State : " + wemoDevices.get(theDevice).getState() + 
					         "\n===========================\n";		
		}
		// -------------------------------------------------------------------------
		// 16/02/2015 ECU return the generated string
		// -------------------------------------------------------------------------
		return display;
		// -------------------------------------------------------------------------
	}

	// =============================================================================
	public static String GetDeviceState (String theFriendlyName)
	{
		for (int theDevice = 0; theDevice < wemoDevices.size (); theDevice++)
		{
			if (wemoDevices.get(theDevice).getFriendlyName().equalsIgnoreCase(theFriendlyName))
			{
				return (wemoDevices.get(theDevice).getState());
			}
		}
		return null;
	}
	// =============================================================================
	public static String [] getWeMoCommands ()
	{
		String [] wemoCommands = null;
		// -------------------------------------------------------------------------
		// 22/02/2015 ECU created to handle any voice commands
		// -------------------------------------------------------------------------
		// 22/02/2015 ECU make sure have the latest list of devices
		// -------------------------------------------------------------------------
		wemoDevices = WeMoService.returnDevices ();
		// -------------------------------------------------------------------------
		// 21/02/2015 ECU check if any devices returned
		// -------------------------------------------------------------------------
		if (wemoDevices.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 22/02/2015 create the results array - the '* 2' is to accommodate on
			//            and off
			// ---------------------------------------------------------------------
			wemoCommands = new String [wemoDevices.size() * 2];
			// ---------------------------------------------------------------------
			for (int theIndex = 0; theIndex < wemoDevices.size(); theIndex++)
			{
				wemoCommands [(theIndex * 2)] 		= wemoDevices.get (theIndex).getFriendlyName() + " on";
				wemoCommands [(theIndex * 2) + 1]	= wemoDevices.get (theIndex).getFriendlyName() + " off";
			}
		}	
		// ------------------------------------------------------------------------
		return wemoCommands;
	}
	// =============================================================================
	public static void message (String theMessage)
	{
		// -------------------------------------------------------------------------
		// 21/02/2015 ECU this is a message passed from the WeMo service
		// -------------------------------------------------------------------------
		Utilities.popToast (theMessage);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void notifyChange (String theEvent,WeMoDevice theWeMoDevice)
	{
		// -------------------------------------------------------------------------
		// 23/02/2015 ECU check the type event
		// -------------------------------------------------------------------------
		if (theEvent.equals (WeMoSDKContext.CHANGE_STATE) || theEvent.equals (WeMoSDKContext.SET_STATE)) 
		{
			// ---------------------------------------------------------------------
			// 23/02/2015 ECU check if the list view has been defined - if it has then
			//                request that the display is refreshed
			// ---------------------------------------------------------------------
			if (listView != null)
			{
				event			= theEvent;
				wemoDevice 		= theWeMoDevice;
				refreshDisplay 	= true; 
				// -----------------------------------------------------------------
				// 05/02/2016 ECU change to use REFRESH rather than 0
				// -----------------------------------------------------------------
				refreshHandler.removeMessages (StaticData.MESSAGE_REFRESH);
				refreshHandler.sendEmptyMessage (StaticData.MESSAGE_REFRESH);
				// -----------------------------------------------------------------
			}
		}
		else if ((theEvent.equals (WeMoSDKContext.ADD_DEVICE)) || (theEvent.equals (WeMoSDKContext.REMOVE_DEVICE)))
		{
			// ---------------------------------------------------------------------
			// 23/02/2015 ECU called when a device has been added
			// ---------------------------------------------------------------------
			// 21/02/2015 ECU make sure have the latest list of devices
			// ---------------------------------------------------------------------
			wemoDevices = WeMoService.getDevices ();
			// ---------------------------------------------------------------------
			// 23/02/2015 ECU cause a refresh of the display
			// ---------------------------------------------------------------------
			notifyChange (WeMoSDKContext.REFRESH_LIST,wemoDevices);
			// ---------------------------------------------------------------------
		}
	}
	// -----------------------------------------------------------------------------
	public static void notifyChange (String theEvent,ArrayList<WeMoDevice> theWeMoDevices)
	{
		// -------------------------------------------------------------------------
		// 20/02/2015 ECU called when a 'refresh' event has happened
		// -------------------------------------------------------------------------
		if (listView != null)
		{
			event			= theEvent;
			wemoDevices 	= theWeMoDevices;
			refreshDisplay 	= true; 
			// ---------------------------------------------------------------------
			// 05/02/2016 ECU changed to use MESSAGE_REFRESH instead of 0
			// ---------------------------------------------------------------------
			refreshHandler.removeMessages (StaticData.MESSAGE_REFRESH);
			refreshHandler.sendEmptyMessage (StaticData.MESSAGE_REFRESH);
		}
	}
	// =============================================================================
	static class RefreshHandler extends Handler
    {
        @Override
        public void handleMessage (Message theMessage) 
        {  
        	switch (theMessage.what)
        	{
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_REFRESH:
        			// -------------------------------------------------------------
        			// 18/02/2015 ECU check if the display is to be refreshed
        			// -------------------------------------------------------------
        			if (refreshDisplay)
        			{
        				refreshDisplay = false;
        				// ---------------------------------------------------------
        				// 20/02/2015 ECU decide which action is to be taken
        				// ---------------------------------------------------------
        				if (event.equals (WeMoSDKContext.REFRESH_LIST))
        				{
        					// -----------------------------------------------------
        					// 20/02/2015 ECU need to refresh the whole display
        					// -----------------------------------------------------
        					adapter.Refresh (wemoDevices);
        					// -----------------------------------------------------
        				}
        				else
        				{
        					// ----------------------------------------------------- 
        					// 18/02/2015 ECU change the display to reflect the state of the
        					//                device which caused the update
        					// -----------------------------------------------------
        					for (int index = 0;  
        							 index <= listView.getLastVisiblePosition() - listView.getFirstVisiblePosition(); 
        							 index++) 
        					{
        						WeMoListItem listItem = (WeMoListItem)listView.getChildAt(index);
        						if (listItem.getDevice() == wemoDevice) 
        						{
        							// ---------------------------------------------
        							listItem.setState (wemoDevice.getState().split ("\\|")[0]);
        							// ---------------------------------------------
        							break;
        						}
        					}
        				}
        				// ---------------------------------------------------------
        			}
        			// -------------------------------------------------------------
        			// 18/02/2015 ECU now sleep for REFRESH_TIME
        			sleep (REFRESH_TIME);
        			// -------------------------------------------------------------
        			break;
        	}
        }
        // -------------------------------------------------------------------------
        public void sleep (long delayMillis)
        {
            this.removeMessages (StaticData.MESSAGE_REFRESH);
            sendMessageDelayed (obtainMessage(StaticData.MESSAGE_REFRESH), delayMillis);
        }
        // -------------------------------------------------------------------------
    }
	// =============================================================================
	public static boolean validation (int theArgument)
	{
		// -------------------------------------------------------------------------
		// 09/03/2015 ECU this is called up by GridActivity to determine whether
		//                this activity is valid on this device
		//            ECU have been having some problems with the WeMo SDK on the
		//                CnM tablet which is on API 15 so prevent the activity
		//                from running
		// 09/03/2016 ECU changed from JELLY_BEAN (4.1 API 16) to ICE_CREAM_SANDWICH
		//                because the SDK says that this is the lowest level of Android
		//                that it supports; however with this level am getting
		//                'an illegal argument' exception within the SDK.
		//                This is for information to explain why JELLY_BEAN is used.
		// -------------------------------------------------------------------------
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return true;
		else
			return false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static int voiceCommands (String theCommand)
	{
		// -------------------------------------------------------------------------
		// 21/02/2015 ECU added to take a string command to process
		// 07/03/2015 ECU check for a string of 'no length' and split the string into
		//                individual commands
		// 08/03/2015 ECU put in the 'null' check which is a 'belt and braces' check
		// -------------------------------------------------------------------------
		if ((theCommand != null) && (theCommand.length() > 0))
		{
			String [] theCommands = theCommand.split(";");
			// ---------------------------------------------------------------------
			// 07/03/2015 ECU now create an ArrayList from the input commands
			// ---------------------------------------------------------------------
			for (int theIndex = 0; theIndex < theCommands.length; theIndex++)
			{
				ArrayList<String> commands = new ArrayList<String> ();
				// -----------------------------------------------------------------
				// add each command into the array list
				// -----------------------------------------------------------------
				commands.add (theCommands [theIndex]);
				// -----------------------------------------------------------------
				// 07/03/2015 ECU pass over each command for processing
				// -----------------------------------------------------------------
				voiceCommands (commands);
			}
		}
		// -------------------------------------------------------------------------
		// 07/03/2015 ECU just indicate something was done
		// -------------------------------------------------------------------------
		return 0;
	}
	// -----------------------------------------------------------------------------
	public static int voiceCommands (ArrayList<String> theInputMatches)
	{
		// -------------------------------------------------------------------------
		// 21/02/2015 ECU created to handle any voice commands
		// -------------------------------------------------------------------------
		// 21/02/2015 ECU make sure have the latest list of devices
		// -------------------------------------------------------------------------
		wemoDevices = WeMoService.returnDevices ();
		// -------------------------------------------------------------------------
		// 21/02/2015 ECU check if any devices returned
		// -------------------------------------------------------------------------
		if (wemoDevices.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 21/02/2015 ECU there are devices so now do some processing
			// ---------------------------------------------------------------------
			// 21/02/2015 ECU assume that the command is of the form
			//                     <device friendly name> <state>
			//                e.g. 'lamp on'
			// ---------------------------------------------------------------------
			// 21/02/2015 ECU loop for each matching voice command
			// ---------------------------------------------------------------------
			for (int theMatch=0; theMatch < theInputMatches.size(); theMatch++)
			{
				String [] inputWords = theInputMatches.get (theMatch).split (" ");
				// -----------------------------------------------------------------
				// 21/02/2015 ECU at this point inputWords [0] should be device name
				//                and inputWords [1] should be desired state
				// -----------------------------------------------------------------
				if (inputWords.length >= 2)
				{
					// -------------------------------------------------------------
					// 23/02/2015 ECU only commands SWITCH_ON or SWITCH_OFF are 
					//                allowed
					// 19/10/2015 ECU added the ignore case
					// -------------------------------------------------------------
					if (inputWords [1].equalsIgnoreCase (SWITCH_OFF) || inputWords [1].equalsIgnoreCase (SWITCH_ON))
					{
						// ---------------------------------------------------------
						// 21/02/2015 ECU now loop through the devices looking for a 
						//                match
						// ---------------------------------------------------------
						for (int theDevice = 0; theDevice < wemoDevices.size(); theDevice++)
						{
							// -----------------------------------------------------
							// 23/02/2015 ECU now check whether the first word
							//                corresponds to a friendly name or
							//                ALL_DEVICES
							// -----------------------------------------------------
							if (wemoDevices.get (theDevice).getFriendlyName ().equalsIgnoreCase (inputWords[0]) ||
									inputWords [0].equalsIgnoreCase (ALL_DEVICES))
							{
								// -------------------------------------------------
								// 21/02/2015 ECU have found a matched friendly name
								//                so try and action
								// -------------------------------------------------
								WeMoService.SetDeviceState (wemoDevices.get (theDevice).getFriendlyName (),
															  (inputWords [1].equalsIgnoreCase(SWITCH_ON)));	
								// -------------------------------------------------
								// 21/02/2015 ECU indicate that the device has been
								//                found and the command actioned
								// -------------------------------------------------
								if (!inputWords [0].equalsIgnoreCase(ALL_DEVICES))
									return theMatch;
								// -------------------------------------------------
							}
						}
						if (inputWords [0].equalsIgnoreCase(ALL_DEVICES))
							return 0;
						// ---------------------------------------------------------
					}
				}
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 21/02/2015 ECU indicate that have not been able to process the voice
		//                commands
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
	}
	// =============================================================================
}
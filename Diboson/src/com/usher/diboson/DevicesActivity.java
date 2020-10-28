package com.usher.diboson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.belkin.wemo.localsdk.WeMoDevice;

import java.util.ArrayList;

public class DevicesActivity extends DibosonActivity 
{
	// =============================================================================
	// DevicesActivity
	// ===============
	// 02/04/2019 ECU created to display details of all devices on the network
	// 25/12/2019 ECU added 'SelectLongAction'
	// =============================================================================

	// =============================================================================
	// 10/05/2019 ECU declare the types of item
	// -----------------------------------------------------------------------------
	private final static int ITEM_TYPE_NORMAL	= 0;
	private final static int ITEM_TYPE_KASA		= 1;
	private final static int ITEM_TYPE_TUYA		= 2;
	private final static int ITEM_TYPE_WEMO		= 3;
	// -----------------------------------------------------------------------------
	public static RefreshHandler refreshHandler;
	// -----------------------------------------------------------------------------
	Context					context;
	ArrayList<ListItem> 	listItems; 
	ListViewSelector 		listViewSelector;
	SmartDevices 			smartDevice;
	ArrayList<WeMoDevice>	wemoDevices;
	// -----------------------------------------------------------------------------

	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 02/04/2019 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			setContentView (R.layout.activity_devices);
			// ---------------------------------------------------------------------
			// 02/04/2019 ECU created to generate the display of devices
			// 09/05/2019 ECU added SelectButton
			// ---------------------------------------------------------------------
			listViewSelector = new ListViewSelector (this,
						 		  					 R.layout.devices_row,
						 		  					 "PopulateTheList",
						 		  					 false,
						 		  					 "SelectAction",
						 		  					 "SelectLongAction",
						 		  					 null,
						 		  					 null,
						 		  					 "SelectButton",
						 		  					 null,
						 		  					 null);
			// ---------------------------------------------------------------------
			// 26/04/2019 ECU declare the handler for screen refreshes
			// ---------------------------------------------------------------------
			refreshHandler = new RefreshHandler ();
			// ---------------------------------------------------------------------
			// 06/10/2019 ECU remember the context for later use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 26/04/2019 ECU ask if smart devices are to be included
			//                the first 'null' is the selection which is not needed
			//                the second 'null' is the method for the 'No' option
			// 				  which does nothing in this case
			// ---------------------------------------------------------------------
			DialogueUtilitiesNonStatic.yesNo (this,
											  this,
											  getString (R.string.title_smart_devices),
											  getString (R.string.question_smart_devices),
											  null,					
											  Utilities.createAMethod (DevicesActivity.class,"YesMethod",(Object) null),
											  null);				
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/04/2019 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	@Override
	public void onBackPressed()
	{
		// -------------------------------------------------------------------------
		// 28/04/2019 ECU created to handled the 'back' key
		// -------------------------------------------------------------------------
		// 28/04/2019 ECU check if monitoring smart devices
		// -------------------------------------------------------------------------
		if (smartDevice != null)
		{
			// ---------------------------------------------------------------------
			// 28/04/2019 ECU monitoring is on so stop that action
			// ---------------------------------------------------------------------
			smartDevice.terminate ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 28//04/2019 ECU terminate this activity
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
		// 28/04/2019 ECU call the main handler
		// -------------------------------------------------------------------------
		super.onBackPressed();			// 08/06/2016 ECU removed
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public ArrayList<ListItem> PopulateTheList ()
	{
		// -------------------------------------------------------------------------
		// 02/04/2019 ECU created to build the list of items that are to be displayed
		// -------------------------------------------------------------------------
		int					deviceIndex = 0;
		Devices             localDevice;
		ListItem			localListItem;
		// -------------------------------------------------------------------------
		// 10/05/2019 ECU preset the list that contains the device details
		// -------------------------------------------------------------------------
		listItems = new ArrayList<ListItem> ();
		// -------------------------------------------------------------------------
		// 02/04/2019 ECU build up the list of devices
		// 18/11/2019 ECU added the 'equalsIg....' test
		// -------------------------------------------------------------------------
		for (int theDevice = 0; theDevice < PublicData.deviceDetails.size(); theDevice++)
		{
			localDevice = PublicData.deviceDetails.get (theDevice);
			localListItem = new ListItem (R.drawable.devices,
											((localDevice.name != null) ? (localDevice.name + 
													(((localDevice.nameOriginal == null) || localDevice.name.equalsIgnoreCase (localDevice.nameOriginal))
																						? StaticData.BLANK_STRING
															                            : (" (" + localDevice.nameOriginal + ")")))
															             : StaticData.BLANK_STRING),
											 localDevice.patientName,
											 localDevice.IPAddress,
											 deviceIndex++);
			// -------------------------------------------------------------
			// 10/05/2019 ECU set the type to a normal device
			// -------------------------------------------------------------
			localListItem.itemType = ITEM_TYPE_NORMAL;
			// -------------------------------------------------------------
			// 29/01/2020 ECU default to not show the custom button
			// -------------------------------------------------------------
			localListItem.visibilityCustom = false;
			// -------------------------------------------------------------
			// 02/04/2019 ECU change the colour depending on the device's
			//                compatibility
			// -------------------------------------------------------------
			if (localDevice.compatible)
			{
				localListItem.colour 			= R.color.green_background;
				// ---------------------------------------------------------
				// 25/12/2019 ECU if not 'this device' then display web
				//                access message
				// ---------------------------------------------------------
				if (!localDevice.IPAddress.equalsIgnoreCase(PublicData.ipAddress))
				{
					localListItem.extras			+= getString (R.string.long_press_for_web_access);
				}
				// ---------------------------------------------------------
			}
			else
			{
				if ((localDevice.name != null) && (localDevice.name.equals (SmartDevices.NAME_KASA) || 
						                           localDevice.name.equals (SmartDevices.NAME_TUYA)))
				{
					// -------------------------------------------------------------
					localListItem.colour 			= R.color.light_gray;
					localListItem.imageResourceId 	= R.drawable.smart_switch;
					// -------------------------------------------------------------
					// 30/04/2019 ECU further modify the KASA state dependent on the
					//                relay state
					// -------------------------------------------------------------
					if (localDevice.name.equals (SmartDevices.NAME_KASA))
					{
						// ---------------------------------------------------------
						// 05/10/2019 ECU set the correct image
						// ---------------------------------------------------------
						localListItem.imageResourceId 	= R.drawable.smart_switch_tp_link;
						// ---------------------------------------------------------
						// 30/04/2019 ECU for the TP-Link device then set the colour
						//                depending on the state of the relay
						// ---------------------------------------------------------
						// 10/05/2019 ECU set the type to a Kasa device
						// ---------------------------------------------------------
						localListItem.itemType = ITEM_TYPE_KASA;
						// ---------------------------------------------------------
						// 10/05/2019 ECU set the legend to the stored 'alias' of the
						//                device
						// ---------------------------------------------------------
						localListItem.legend = SmartDevices.getJSONKey (localDevice.response,"alias");
						// ---------------------------------------------------------
						// 30/04/2019 ECU process the JSON data to get the current
						//                state
						// ---------------------------------------------------------
						int localRelayState = SmartDevices.getJSONRelayState (localDevice.response);
						// ---------------------------------------------------------
						// 30/04/2019 ECU now set the colour
						// 09/05/2019 ECU set up the button that allows switching
						// 10/05/2019 ECU use itemState to remember the state of the
						//                switch
						// 29/01/2020 ECU changeed to use SetCustomLegend
						// ---------------------------------------------------------
						if (localRelayState == SmartDevices.RELAY_STATE_OFF)
						{
							localListItem.colour 		= R.color.red;
							localListItem.SetCustomLegend (getString (R.string.On));
							localListItem.itemState 	= false;
						}
						else
						if (localRelayState == SmartDevices.RELAY_STATE_ON)
						{
							localListItem.colour 		= R.color.light_green;
							localListItem.SetCustomLegend (getString (R.string.Off));
							localListItem.itemState 	= true;
						}
						// ---------------------------------------------------------
					}
					else
					if (localDevice.name.equals (SmartDevices.NAME_TUYA))
					{
						// ---------------------------------------------------------
						// 05/10/2019 ECU set the correct image
						// ---------------------------------------------------------
						localListItem.imageResourceId 	= R.drawable.smart_switch_eveready;
						// ---------------------------------------------------------
						// 10/05/2019 ECU set the type to a Tuya device
						// ---------------------------------------------------------
						localListItem.itemType = ITEM_TYPE_TUYA;
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
				else
				{
					localListItem.colour 			= R.color.light_gray;
					localListItem.imageResourceId 	= R.drawable.notification;
				}
			}
			if (localDevice.phone)
				localListItem.imageResourceId = R.drawable.phone_icon;
			// ---------------------------------------------------------------------
			// 04/10/2016 ECU Note - add the new record to the list
			// ---------------------------------------------------------------------
			listItems.add (localListItem);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 27/04/2019 ECU check if there are any WeMo devices to include
		// -------------------------------------------------------------------------
		if (PublicData.storedData.wemoHandling)
		{
			// ---------------------------------------------------------------------
			// 27/04/2019 ECU get the list of WeMo devices from the service
			// 10/05/2019 ECU changed the definition to be at the head of the class
			// ---------------------------------------------------------------------
			wemoDevices = WeMoService.getDevices ();
			// ---------------------------------------------------------------------
			// 27/04/2019 ECU if there are entries then add them to the list
			// ---------------------------------------------------------------------
			if (wemoDevices.size() > 0)
			{
				for (WeMoDevice wemoDevice : wemoDevices) 
				{
					// -------------------------------------------------------------
					// 27/04/2019 ECU add details of the WeMo device into the list
					// 10/05/2019 ECU changed to getUDN from getLogoURL
					// -------------------------------------------------------------
					localListItem = new ListItem (R.drawable.wemo,
												   wemoDevice.getFriendlyName (),
												   wemoDevice.getSerialNumber (),
												   wemoDevice.getUDN(),
												   deviceIndex++);
					// -------------------------------------------------------------
					// 10/05/2019 ECU set the type to a WeMovice
					// -------------------------------------------------------------
					localListItem.itemType = ITEM_TYPE_WEMO;
					// -------------------------------------------------------------
					// 27/04/2019 ECU make it a different colour
					// 10/05/2019 ECU remember the state of the switch
					// 29/01/2020 ECU changed to use SetCustomLegend
					// -------------------------------------------------------------
					if (wemoDevice.getState().equals (WeMoDevice.WEMO_DEVICE_ON))
					{
						localListItem.colour 		= R.color.light_green;
						localListItem.SetCustomLegend (getString (R.string.Off));
						localListItem.itemState 	= true;
					}
					else
					{
						localListItem.colour 		= R.color.red;
						localListItem.SetCustomLegend (getString (R.string.On));
						localListItem.itemState 	= false;
					}
					// -------------------------------------------------------------
					// 27/04/2019 ECU add the local record to the list
					// -------------------------------------------------------------
					listItems.add (localListItem);
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		return listItems;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class RefreshHandler extends Handler
	{
		// -------------------------------------------------------------------------
		@Override
	    public void handleMessage (Message theMessage) 
	    {
	    	// ---------------------------------------------------------------------
			listViewSelector.refresh ();
			// ---------------------------------------------------------------------
	    }
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	public void SelectAction (int thePosition)
	{
		// -------------------------------------------------------------------------
		// 06/10/2019 ECU decide if there are any actions to take
		// -------------------------------------------------------------------------
		switch (listItems.get(thePosition).imageResourceId)
		{
			// ---------------------------------------------------------------------
			case R.drawable.smart_switch_tp_link:
				// -----------------------------------------------------------------
				// 06/10/2019 ECU handle the actions for a TP-Link (Kasa) device
				// -----------------------------------------------------------------
				Utilities.actionHandler (context,PublicData.storedData.smart_device_kasa_actions);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case R.drawable.smart_switch_eveready:
				// -----------------------------------------------------------------
				// 06/10/2019 ECU handle the actions for a Eveready (Tuya) device
				// -----------------------------------------------------------------
				Utilities.actionHandler (context,PublicData.storedData.smart_device_tuya_actions);	
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case R.drawable.wemo:
				// -----------------------------------------------------------------
				// 06/10/2019 ECU handle the actions for a Belkin (WeMo) device
				// -----------------------------------------------------------------
				Utilities.actionHandler (context,PublicData.storedData.smart_device_wemo_actions);	
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			default:
				// -----------------------------------------------------------------
				// 02/04/2019 ECU display details of the selected device
				//            ECU changed the way the information is displayed so that the
				//                user has to clear
				// -----------------------------------------------------------------
				Utilities.popToast (findViewById (android.R.id.content).getRootView(),
									PublicData.deviceDetails.get (thePosition).Print (),
									getString (R.string.press_to_clear),
									60000,
									false);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void SelectButton (int thePosition)
	{
		// -------------------------------------------------------------------------
		// 09/05/2019 ECU created to handle the clicking on the custom button
		// 10/05/2019 ECU get the details of the item that the button relates to
		// -------------------------------------------------------------------------
		ListItem localListItem = listItems.get (thePosition);
		// -------------------------------------------------------------------------
		// 10/05/2019 ECU decide what to do depending on the type of device
		// -------------------------------------------------------------------------
		switch (localListItem.itemType)
		{
			// ---------------------------------------------------------------------
			case ITEM_TYPE_NORMAL:
				break;
			// ---------------------------------------------------------------------
			case ITEM_TYPE_KASA:
				// -----------------------------------------------------------------
				// 10/05/2019 ECU this is a Kasa device whose IP address is stored
				//                in extras
				// ----------------------------------------------------------------- 
				try
				{
					// -------------------------------------------------------------
					// 10/05/2019 ECU create a smart device with correct IP address and
					//                port
					// 05/10/2019 ECU changed to use the stored port
					// -------------------------------------------------------------
					SmartDevices localSmartDevice = new SmartDevices (localListItem.extras,PublicData.storedData.smart_device_kasa_tcp_port);
					// -------------------------------------------------------------
					// 10/05/2019 ECU decide if the switch is currently on or off
					// -------------------------------------------------------------
					if (localListItem.itemState)
					{
						// ---------------------------------------------------------
						// 10/05/2019 ECU the switch is currently on so switch it off
						// ---------------------------------------------------------
						localSmartDevice.switchOff ();
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 10/05/2019 ECU the switch is currently off so switch it on
						// ---------------------------------------------------------
						localSmartDevice.switchOn ();
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 10/05/2019 ECU try and get an immediate refresh of the display
					// -------------------------------------------------------------
					smartDevice.initiateImmediateRefresh ();
					// -------------------------------------------------------------
				}
				catch (Exception theException)
				{
					
				}
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case ITEM_TYPE_TUYA:
				break;
			// ---------------------------------------------------------------------
			case ITEM_TYPE_WEMO:
				// -----------------------------------------------------------------
				// 10/05/2019 ECU toggle the state of the WeMo device whose friendly
				//                name is supplied in the 'legend'
				// -----------------------------------------------------------------
				WeMoService.SetDeviceState (localListItem.GetLegend(),!localListItem.itemState);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 10/05/2019 ECU force an immediate refresh
		// -------------------------------------------------------------------------
		refreshHandler.removeMessages  (StaticData.MESSAGE_REFRESH);
		refreshHandler.sendEmptyMessage (StaticData.MESSAGE_REFRESH);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void SelectLongAction (int thePosition)
	{
		// -------------------------------------------------------------------------
		// 25/12/2019 ECU decide if there are any actions to take
		//            ECU only interest in a compatible device
		//			  ECU add check on IP address
		// -------------------------------------------------------------------------
		if (PublicData.deviceDetails.get (thePosition).compatible &&
					!PublicData.deviceDetails.get (thePosition).IPAddress.equalsIgnoreCase(PublicData.ipAddress))
		{
			// ---------------------------------------------------------------------
			// 25/12/2019 ECU want to connect via the web browser
			// ---------------------------------------------------------------------
			Intent localIntent = new Intent (getBaseContext(),DisplayURL.class);
			// ---------------------------------------------------------------------
			// 25/12/2019 ECU pass through the URL that is to be displayed
			// ---------------------------------------------------------------------
			localIntent.putExtra (StaticData.PARAMETER_URL,
									String.format ("%s%s:%d%s",
										StaticData.URL_INTRO,
										PublicData.deviceDetails.get (thePosition).IPAddress,
										PublicData.socketNumberForWeb,
										getString (R.string.home_page)));
			// ---------------------------------------------------------------------
			// 25/12/2019 ECU now start up the activity
			// ---------------------------------------------------------------------
			startActivity (localIntent);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
	public void YesMethod (Object theSelection)
  	{
		// -------------------------------------------------------------------------
		// 26/04/2019 ECU the user has decided to include smart devices into
		//                the displayed list - theSelection is of no interest
		// -------------------------------------------------------------------------
		smartDevice = new SmartDevices ();
		smartDevice.initialise (this);
		// -------------------------------------------------------------------------
  	}
	// =============================================================================
}

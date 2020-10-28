
package com.usher.diboson;

import java.util.ArrayList;
import java.util.Collections;
import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.ActionList;
import org.cybergarage.upnp.Argument;
import org.cybergarage.upnp.ArgumentList;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.DeviceList;
import org.cybergarage.upnp.Icon;
import org.cybergarage.upnp.IconList;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.ServiceList;
import org.cybergarage.upnp.StateVariable;
import org.cybergarage.upnp.UPnP;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

// =================================================================================
// 15/12/2017 ECU changed to extend DibosonActivity
// 18/04/2018 ECU changed to use ListViewSelector and reduce number of static's
// =================================================================================

// =================================================================================
public class UPnP_Activity_All extends DibosonActivity 
{
	// -----------------------------------------------------------------------------
	public 	static final int	MESSAGE_DEVICES	=	0;
	public 	static final int	MESSAGE_DISPLAY	=	1;
	// -----------------------------------------------------------------------------
	private static final int	BUILD_ACTIONS	=	0;
	private static final int	BUILD_DEVICES	=	1;
	private static final int	BUILD_SERVICES	=	2;
	// -----------------------------------------------------------------------------
	Action   			actionSelected;
	Activity			activity;
	int					buildType;
	Context				context;
	boolean         	debugUPnP 			= false;
	DeviceList			deviceList			= null;
	String [] 			deviceListString;
	Device				deviceSelected;
	String				iconURLAddress;
	ArgumentList		inputArgumentList;
	int					inputArgumentPointer;
	ListViewSelector	listViewSelector;
	ServiceList 		servicesList;
	Service				serviceSelected;
	// -----------------------------------------------------------------------------
	// 17/04/2018 ECU declare any variables that are accessed remotely
	// -----------------------------------------------------------------------------
	public static UPnP_All_ControlPoint 	controlPoint;
	public static DisplayHandler			displayHandler;	
	// -----------------------------------------------------------------------------
	@Override
    protected void onCreate(Bundle savedInstanceState) 
    {
		// -------------------------------------------------------------------------
        super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
        // 15/12/2017 ECU check if the activity is being restarted by the Android OS
        // -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 17/04/2018 ECU added the full screen option
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			// ---------------------------------------------------------------------
			// 06/05/2017 ECU initialise any static variables
			// ---------------------------------------------------------------------
			activity	  = this;
			context       = this;
			// ---------------------------------------------------------------------
			// 06/04/2017 ECU start up the user interface
			// ---------------------------------------------------------------------
			HandleDevices (this);
			// ---------------------------------------------------------------------
			// 23/08/2016 ECU set up the message handler
			// ---------------------------------------------------------------------
			displayHandler = new DisplayHandler ();
			// ---------------------------------------------------------------------  
			UPnP.setEnable (UPnP.USE_ONLY_IPV4_ADDR);
			// ---------------------------------------------------------------------
			// 06/05/2017 ECU start up the control point task that will acquire device
			//                information
			// ---------------------------------------------------------------------
			new ControlPointTask().execute ();
			// ---------------------------------------------------------------------
			// 06/05/2017 ECU indicate that scanning is in progress
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (getString (R.string.scanning),true);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 15/12/2017 ECU the activity has been recreated after having been
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
		// 17/04/2018 ECU created to handle the BACK key
		//            ECU check if any local actions are required - if none then a
		//                false will be returned
		// -------------------------------------------------------------------------
		if (!BackAction())
		{
			// ---------------------------------------------------------------------
			// 17/04/2018 ECU no local processing is required so can just terminate
			//                this activity
			// ---------------------------------------------------------------------
			finish ();
			// ---------------------------------------------------------------------
			// 17/04/2018 ECU now pass to the 'super' handler
			// ---------------------------------------------------------------------
			super.onBackPressed ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
    // -----------------------------------------------------------------------------
    private class ControlPointTask extends AsyncTask <Object, Object, Object>
    {
		@Override
		protected Object doInBackground (Object... arg0) 
		{
			// ---------------------------------------------------------------------
			// 08/05/2017 ECU Note - define a control point and initiate it so that
			//                       discovery begins
			// ---------------------------------------------------------------------
			controlPoint = new UPnP_All_ControlPoint ();    
			controlPoint.start ();
			// ---------------------------------------------------------------------
		 	return null;
		 	// ---------------------------------------------------------------------
		}
    }
	// =============================================================================
	boolean BackAction ()
	{
		// -------------------------------------------------------------------------
		// 17/04/2018 ECU called up when the 'back' key pressed to decide which
		//                display is wanted (return true) or whether the activity is 
		//                to be terminated (return false)
		// -------------------------------------------------------------------------
		// 22/11/2015 ECU just finish the activity
		// -------------------------------------------------------------------------
		switch (buildType)
		{
			// ---------------------------------------------------------------------
			case BUILD_ACTIONS:
				// -----------------------------------------------------------------
				// 17/04/2018 ECU display the services
				// -----------------------------------------------------------------
				HandleServices (context);
				return true;
			// ---------------------------------------------------------------------
			case BUILD_DEVICES:
				// -----------------------------------------------------------------
				// 17/04/2018 ECU terminate the activity
				// -----------------------------------------------------------------
				return false;
			// ---------------------------------------------------------------------
			case BUILD_SERVICES:
				// -----------------------------------------------------------------
				// 17/04/2018 ECU display the devices
				// -----------------------------------------------------------------
				HandleDevices (context);
				return true;
			// ---------------------------------------------------------------------
			default:
				return false;
			// ---------------------------------------------------------------------
		}
		//--------------------------------------------------------------------------
	}
	// =============================================================================
	public void ArgumentMethod (String theInputText)
	{
		// -------------------------------------------------------------------------
		// 13/05/2017 ECU created to handle an input argument
		// -------------------------------------------------------------------------	
		if (theInputText == null)
		{
			inputArgumentPointer = 0;
		}
		else
		{
			actionSelected.setArgumentValue (inputArgumentList.getArgument (inputArgumentPointer).getName(), theInputText);
			
			inputArgumentPointer++;
		}
		// -------------------------------------------------------------------------
		if (inputArgumentPointer < inputArgumentList.size())
		{
			// ---------------------------------------------------------------------
			// 13/05/2017 ECU get the argument pointer to
			// ---------------------------------------------------------------------
			Argument inputArgument = inputArgumentList.getArgument (inputArgumentPointer);
		
			DialogueUtilitiesNonStatic.textInput (context,
												  activity,
												  "Argument : " + inputArgument.getName(),
												  "Please enter the data for \'" + inputArgument.getName() + "\'",
												  serviceSelected.getStateVariable(inputArgument.getRelatedStateVariableName()).getDefaultValue(),
												  Utilities.createAMethod (UPnP_Activity_All.class,"ArgumentMethod",StaticData.BLANK_STRING),
												  null);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 13/05/2017 ECU have all of the required data
			// ---------------------------------------------------------------------
		   	// 10/05/2017 ECU now cause the action to be taken
	    	// 13/05/2017 ECU PROBLEM - with the latest version of CyberGarage the
	    	//                =======   '.postControlAction' method returns a boolean
	    	//                          rather than a string - may have been changed for
	    	//                          WeMo devices
	    	// ---------------------------------------------------------------------	
	    	String actionResponse = actionSelected.postControlAction ();
	       	// ---------------------------------------------------------------------
	    	// 13/05/2017 ECU confirm the selection to the user
	    	// 24/02/2018 ECU added the call to XMLFormatter to get a more 'pretty'
	    	//                display of the XML data
	    	//            ECU check if the response is null
	    	// ---------------------------------------------------------------------
	    	Utilities.popToast (findViewById(android.R.id.content),
	    							"Friendly Name : " + deviceSelected.getFriendlyName() + StaticData.NEWLINE + StaticData.NEWLINE +
	    							actionSelected.getName() + StaticData.NEWLINE + StaticData.NEWLINE + 
	    							argumentsInformation (actionSelected) + StaticData.NEWLINE + StaticData.NEWLINE +
	    							"Response : " + StaticData.NEWLINE + StaticData.NEWLINE + 
	    							((actionResponse == null) ? StaticData.BLANK_STRING 
	    									                  : Utilities.XMLformatter (actionResponse)),true);
	    	// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    public String argumentsInformation (Action theAction)
    {
    	// -------------------------------------------------------------------------
    	// 13/05/2017 ECU generate the details of the arguments associated with the
    	//                specified action
    	// -------------------------------------------------------------------------
    	String argumentsString = StaticData.BLANK_STRING;
    	// -------------------------------------------------------------------------
    	// 13/05/2017 ECU summarise each of the arguments
    	// -------------------------------------------------------------------------
    	ArgumentList argumentList = theAction.getArgumentList ();
    	// -------------------------------------------------------------------------
    	for (int theArgument = 0; theArgument < argumentList.size(); theArgument++)
    	{
    		// ---------------------------------------------------------------------
    		// 13/05/2017 ECU get the particular argument
    		// ---------------------------------------------------------------------
    		Argument argument = argumentList.getArgument (theArgument);
    		// ---------------------------------------------------------------------
    		// 13/05/2017 ECU get the state variable that is associated with this
    		//                argument
    		// ---------------------------------------------------------------------
    		StateVariable stateVariable = serviceSelected.getStateVariable (argument.getRelatedStateVariableName ());
    		// ---------------------------------------------------------------------
    		// 13/05/2017 ECU generate details of the argument
    		// ---------------------------------------------------------------------
    		argumentsString += "Argument : " + argument.getName() + StaticData.NEWLINE;
    		argumentsString += "     Direction : " + argument.getDirection() + StaticData.NEWLINE;
    		argumentsString += "     State Variable : " + stateVariable.getName() + StaticData.NEWLINE;
    		argumentsString += "     Data Type : " + stateVariable.getDataType() + StaticData.NEWLINE;
    		argumentsString += "     Value : " + argument.getValue() + StaticData.NEWLINE;
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    	// 13/05/2017 ECU return the generated string
    	// -------------------------------------------------------------------------
    	return argumentsString;
    	// -------------------------------------------------------------------------
    	
    }
    // =============================================================================
    public void BrowseMethod (int theDevice)
    {
    	// -------------------------------------------------------------------------
    	// 13/12/2017 ECU created to handle the browse button - basically display
    	//                the XML page which gives all of the device's details
    	// 14/12/2017 ECU changed to use the method which has the code that used to be
    	//                here
    	// -------------------------------------------------------------------------
    	Utilities.displayAWebPage (context,deviceList.getDevice (theDevice).getLocation ());
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
	ArrayList<ListItem> BuildTheActionsList ()
	{
		ArrayList<ListItem> listItems = new ArrayList<ListItem>();
		// ---------------------------------------------------------------------
    	ActionList actionsList = serviceSelected.getActionList(); 
		// ---------------------------------------------------------------------
		if (actionsList.size() > 0)
		{
			for (int theIndex = 0; theIndex < actionsList.size(); theIndex++)
			{
				// -------------------------------------------------------------
				// 30/03/2014 ECU added the index as an argument
				// 31/01/2016 ECU do not add carers which have been deleted
				// -------------------------------------------------------------
				ListItem localListItem = new ListItem (null,
													   actionsList.getAction (theIndex).getName(),
													   StaticData.BLANK_STRING,
													   StaticData.BLANK_STRING,
													   theIndex);
				// -------------------------------------------------------------
				listItems.add (localListItem);
				// -------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		return listItems;
		// -------------------------------------------------------------------------
	}	
	// =============================================================================
	ArrayList<ListItem> BuildTheDevicesList ()
	{
		ArrayList<ListItem> listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 06/05/2017 ECU check if the control point has been defined yet
		// -------------------------------------------------------------------------
		if (controlPoint != null)
		{
			// ---------------------------------------------------------------------
			// 18/10/2016 ECU add in the check on size
			// --------------------------------------------------------------------- 
			deviceList = controlPoint.getDeviceList ();
			// ---------------------------------------------------------------------
			if (deviceList.size() > 0)
			{
				for (int theIndex = 0; theIndex < deviceList.size(); theIndex++)
				{
					// -------------------------------------------------------------
					// 30/03/2014 ECU added the index as an argument
					// 31/01/2016 ECU do not add carers which have been deleted
					// -------------------------------------------------------------
					ListItem localListItem = new ListItem (null,
														   deviceList.getDevice (theIndex).getFriendlyName (),
														   deviceList.getDevice (theIndex).getUDN (),
														   deviceList.getDevice (theIndex).getManufacture() + "\n    " +
																   deviceList.getDevice (theIndex).getLocation(),
														   theIndex);
					// -------------------------------------------------------------
					// 06/05/2017 ECU add in the URL to the image
					// 17/12/2017 ECU the log for the device can be obtained either
					//                from the 'getLogoURLFromDevice' or from the
					//                icon list
					// -------------------------------------------------------------
				 	String iconURLAddress = deviceList.getDevice (theIndex).getLogoURLFromDevice ();	
				 	// -------------------------------------------------------------
				 	// 17/12/2017 ECU if there is no logo URL then a blank string
				 	//                will be returned
				 	// -------------------------------------------------------------
				 	if (iconURLAddress.equalsIgnoreCase (StaticData.BLANK_STRING))
				 	{
				 		// ---------------------------------------------------------
				 		// 17/12/2017 ECU there is no logo URL so scan the returned
				 		//                icon list to find the one with the
				 		//                largest width
				 		// ---------------------------------------------------------
				 		IconList icons = deviceList.getDevice (theIndex).getIconList();
				 		// ---------------------------------------------------------
				 		// 17/12/2017 ECU if there are some stored icons then scan
				 		//                looking for the one with the largest width
				 		// ---------------------------------------------------------
				    	if (icons.size() > 0)
				    	{
				    		// -----------------------------------------------------
				    		// 12/12/2017 ECU Note - just use the first icon listed
				    		// 12/12/2017 ECU changed to use last entry to get better 
				    		//                quality
				    		// 17/12/2017 ECU try and find the best resolution icon
				    		// -----------------------------------------------------
				    		int iconWidth = 0;
				    		int iconIndex = 0;
				    		// -----------------------------------------------------
				    		// 17/12/2017 ECU now loop through the icons
				    		// -----------------------------------------------------
				    		for (int index = 0; index < icons.size (); index++)
				    		{
				    			// -------------------------------------------------
				    			// 17/12/2017 ECU check if this icon's width is bigger
				    			//                than that set
				    			// -------------------------------------------------
				    			if (icons.getIcon(index).getWidth() >= iconWidth)
				    			{
				    				// ---------------------------------------------
				    				// 17/12/2017 ECU store this icon as the 'biggest'
				    				// ---------------------------------------------
				    				iconWidth = icons.getIcon (index).getWidth ();
				    				iconIndex = index;
				    				// ---------------------------------------------
				    			}
				    		}
				    		// -----------------------------------------------------
				    		// 17/12/2017 ECU set the icon that has the biggest icon
				    		// -----------------------------------------------------
				    		Icon icon = icons.getIcon (iconIndex);
				    		// -----------------------------------------------------
			    			// 12/12/2017 ECU the icon URL can be complete or relative
			    			//                to the 'base'
			    			// -----------------------------------------------------
			    			// 12/12/2017 ECU get the URL that is in the 'icon'
			    			// -----------------------------------------------------
			    			iconURLAddress = icon.getURL ();
			    			// -----------------------------------------------------
			    			// 12/12/2017 ECU check if it is a 'complete URL'
			    			// -----------------------------------------------------
			    			if (!iconURLAddress.startsWith (StaticData.URL_INTRO))
			    			{
			    				// -------------------------------------------------
			    				// 12/12/2017 ECU the icon URL is partial
			    				// -------------------------------------------------
			    				String bits [] = deviceList.getDevice (theIndex).getLocation ().split ("/");
			    				String base = StaticData.URL_INTRO + bits [2];      
			    				if (!iconURLAddress.startsWith("/"))
			    					base += base + "/";
			    				iconURLAddress = base + iconURLAddress;
			    				// -------------------------------------------------
			    			}
				    	}
			    	}
				 	// -------------------------------------------------------------
				 	// 17/12/2017 ECU now store the image URL in the item
				 	// -------------------------------------------------------------
			    	localListItem.imageURL = iconURLAddress;
					// -------------------------------------------------------------
			    	// 17/12/2017 ECU add this item into the list
			    	// -------------------------------------------------------------
					listItems.add (localListItem);
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
			// 17/12/2017 ECU Note - sort the items by 'friendly name'
			// ---------------------------------------------------------------------
			Collections.sort (listItems);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		return listItems;
		// -------------------------------------------------------------------------
	}	
	// =============================================================================
	ArrayList<ListItem> BuildTheServicesList ()
	{
		ArrayList<ListItem> listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 18/10/2016 ECU add in the check on size
		// ------------------------------------------------------------------------- 
		servicesList = controlPoint.getDevice (deviceSelected.getFriendlyName()).getServiceList ();
		// -------------------------------------------------------------------------
		if (servicesList.size() > 0)
		{
			for (int theIndex = 0; theIndex < servicesList.size(); theIndex++)
			{
				 final Service service = servicesList.getService (theIndex);
		    	 String  baseURL = service.getRootDevice().getURLBase();
		    	 // ----------------------------------------------------------------
		    	 Thread thread = new Thread()
	    		 {
		    		 // ------------------------------------------------------------
	    			 @Override
	    			 public void run()
	    			 {
	    				 try 
	    				 {
	    					 controlPoint.subscribe (service);
	    				 }
	    				 catch(Exception theException)
	    				 {
	    		            
	    				 }       
	    			 }
	    		 };
	    		 thread.start();     
				// -------------------------------------------------------------
				// 30/03/2014 ECU added the index as an argument
				// 31/01/2016 ECU do not add carers which have been deleted
				// -------------------------------------------------------------
				ListItem localListItem = new ListItem (null,
													   service.getServiceType(),
													   baseURL,
													   StaticData.BLANK_STRING,
													   theIndex);
				// -------------------------------------------------------------
				listItems.add (localListItem);
				// -------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		return listItems;
		// -------------------------------------------------------------------------
	}	
	// =============================================================================
	public void DebugMethod (int theDummyArgument)
	{
		// -------------------------------------------------------------------------
		// 13/05/2017 ECU created to toggle the protocol debug flag
		// -------------------------------------------------------------------------
		debugUPnP = !debugUPnP;
		// -------------------------------------------------------------------------
		// 13/05/2017 ECU tell the user what is happening
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak ("Protocol debug mode is " + (debugUPnP ? "on" : "off"),true);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class DisplayHandler extends Handler
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
				case MESSAGE_DEVICES:
					// -------------------------------------------------------------
					// 17/04/2018 ECU refresh the display with the latest list of
					//                devices
					//            ECU clear to null to force a complete rebuild
					// -------------------------------------------------------------
					listViewSelector = null;
					refreshDisplay ();
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case MESSAGE_DISPLAY:
					if (debugUPnP)
						MessageHandler.popToast ((String) theMessage.obj);
					break;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		/* ------------------------------------------------------------------------ */
	};
    // =============================================================================
    void HandleActions (Context theContext)
	{
    	// -------------------------------------------------------------------------
    	// 17/04/2018 ECU Note - display the associated actions
    	// -------------------------------------------------------------------------
    	buildType = BUILD_ACTIONS;
		// -------------------------------------------------------------------------
		initialiseDisplay (activity);
		// -------------------------------------------------------------------------
	
	}
    // =============================================================================
    void HandleDevices (Context theContext)
	{
    	// -------------------------------------------------------------------------
    	// 17/04/2018 ECU Note - display the discovered devices
    	// -------------------------------------------------------------------------
        buildType  = BUILD_DEVICES;
		// -------------------------------------------------------------------------
        initialiseDisplay (activity);
		// -------------------------------------------------------------------------
	
	}
    // =============================================================================
    void HandleServices (Context theContext)
	{
    	// -------------------------------------------------------------------------
    	// 17/04/2018 ECU Note - display the services associated with the selected
    	//                       device
    	// -------------------------------------------------------------------------
    	buildType = BUILD_SERVICES;
    	// -------------------------------------------------------------------------
    	initialiseDisplay (activity);
		// -------------------------------------------------------------------------
	}
    // =============================================================================
    void processArguments (Action theAction)
    {
    	// -------------------------------------------------------------------------
    	// 13/05/2017 ECU created to request any data that the 'user' has to supply
    	//                arguments which have an 'in' direction need to be supplied
    	// -------------------------------------------------------------------------
    	inputArgumentList = theAction.getInputArgumentList ();
    	// -------------------------------------------------------------------------
    	// 13/05/2017 ECU check if there are any input parameters and then invoke
    	//                the action
    	// -------------------------------------------------------------------------
    	ArgumentMethod (null);  	
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public void SelectActionMethod (int theAction)
    {
    	// -------------------------------------------------------------------------
    	// 13/05/2017 ECU called when a particular action has been selected
    	// -------------------------------------------------------------------------
    	// 13/05/2017 ECU get the actual action that has been selected
    	// -------------------------------------------------------------------------
    	actionSelected = serviceSelected.getActionList().getAction (theAction);
    	// -------------------------------------------------------------------------
    	// 13/05/2017 ECU process any arguments that the user has to supply
    	// -------------------------------------------------------------------------
    	processArguments (actionSelected);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public void SelectDeviceMethod (int theDevice)
    {
    	// -------------------------------------------------------------------------
    	// 06/05/2017 ECU created to handle the selection of a device
    	// -------------------------------------------------------------------------
    	deviceSelected = deviceList.getDevice (theDevice);
    	// -------------------------------------------------------------------------
    	// 06/05/2017 ECU now display the services
    	// -------------------------------------------------------------------------
    	HandleServices (context);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public void SelectServiceMethod (int theService)
    {
    	// -------------------------------------------------------------------------
    	// 06/04/2017 ECU created to handle the selection of a service
    	// -------------------------------------------------------------------------
    	serviceSelected = servicesList.getService (theService);
    	// -------------------------------------------------------------------------
    	HandleActions (context);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    
    
    
	
  	// =============================================================================
  	// =============================================================================
  	// ListViewSelector
  	// ================
  	//
  	//		Declare methods associated with the use of ListViewSelector
  	//
  	// ============================================================================
  	// ============================================================================
  	
	// =============================================================================
	void initialiseDisplay (Activity theActivity)
	{
		// -------------------------------------------------------------------------
		// 13/04/2018 ECU created to generate the display of stored documents
		// -------------------------------------------------------------------------
		// 17/04/2018 ECU work out the build type dependencies
		// -------------------------------------------------------------------------
		int 	rowWanted 			= R.layout.upnp_row;
		String	selectMethodName 	= null;
		// -------------------------------------------------------------------------
		switch (buildType)
		{
			// ---------------------------------------------------------------------
			case BUILD_ACTIONS:
				rowWanted 		 = R.layout.upnp_details_row;
				selectMethodName = "SelectActionMethod";
				break;
			// ---------------------------------------------------------------------
			case BUILD_DEVICES:
				rowWanted 		 = R.layout.upnp_row;
				selectMethodName = "SelectDeviceMethod";
				break;
			// ---------------------------------------------------------------------
			case BUILD_SERVICES:
				rowWanted 		 = R.layout.upnp_details_row;
				selectMethodName = "SelectServiceMethod";
				break;
			// ---------------------------------------------------------------------
			default:
					break;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		listViewSelector = new ListViewSelector (theActivity,
				   								 rowWanted,
				   								 "PopulateTheList",
				   								 true,
				   								 selectMethodName,
				   								 "DebugMethod",
				   								 null,
				   								 getString (R.string.add),
				   								 null,
				   								 "BrowseMethod",
				   								 null
				   								);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public ArrayList<ListItem> PopulateTheList ()
	{
		// -------------------------------------------------------------------------
		switch (buildType)
		{
			// ---------------------------------------------------------------------
			case BUILD_ACTIONS:
				return BuildTheActionsList ();
			// ---------------------------------------------------------------------
			case BUILD_DEVICES:
				return BuildTheDevicesList ();
			// ---------------------------------------------------------------------
			case BUILD_SERVICES:
				return BuildTheServicesList ();
			// ---------------------------------------------------------------------
			default:
				return null;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	void refreshDisplay ()
	{
		// -------------------------------------------------------------------------
		// 13/04/2018 ECU created to refresh the display if it exists or create the
		//                display if not
		// -------------------------------------------------------------------------
		if (listViewSelector == null)
		{
			// ---------------------------------------------------------------------
			// 13/04/2018 ECU need to build the display
			// ---------------------------------------------------------------------
			initialiseDisplay (this);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 13/04/2018 ECU display already initialised so just refresh it
			// ---------------------------------------------------------------------
			listViewSelector.refresh ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

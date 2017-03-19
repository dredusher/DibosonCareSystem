package com.usher.diboson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class TCPActivity extends DibosonActivity 
{
	// =============================================================================
	// 17/07/2013 ECU created
	// 11/12/2013 ECU added the PickAFile for the fileName text file
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	// 11/12/2013 ECU some initial testing following changes to the fileName field
	// 16/10/2014 ECU include the clone mode
	// 16/03/2015 ECU take out the setting of the remoteMusicPlayer as this is now
	//                done from a 'menu' option in MusicPlayer
	//            ECU changed 'serverButton' to 'datagramButton'
	//            ECU change remote streaming destination to use custom dialogue
	// 17/03/2015 ECU have a final tidy up of the activity - trying to use dialogue
	//                methods rather than EditTtext fields
	//			  ECU changed so that there is only one buttonListener which makes
	//                the code much tidier.
	// 09/10/2015 ECU only allow this activity to run if newly created
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 05/04/2016 ECU add the 'send hello' message button
	// 26/04/2016 ECU add the 'monitor a device' button and associated code
	// 10/11/2016 ECU added 'request device details' options
	//==============================================================================
	/* ============================================================================= */
	private static final String TAG					  	= "TCPActivity";
	// =============================================================================
	private static final int	ACTION_DATAGRAM			=	0;
	private static final int	ACTION_DEVICE_NAME		= 	1;
	private static final int	ACTION_MONITOR_A_DEVICE	= 	2;
	private static final int	ACTION_REMOTE_STREAM	=	3;
	private static final int	ACTION_TCP_CLIENT		=	4;
	private static final int	ACTION_TRANSMIT_FILE	=	5;
	/* ============================================================================= */
	Button 		broadcastButton;							// 26/07/2013 ECU added
	Button 		clientButton;
	Button      cloneButton;								// 16/10/2014 ECU added
	Button      cloneTransmitButton;						// 16/10/2014 ECU added
	Button 		datagramButton;
	Button 		deviceNameButton;							// 06/08/2013 ECU added
	Button 		discoverButton;								// 29/07/2013 ECU added
	Button 		monitorADeviceButton;						// 26/04/2016 ECU added
	Button 		remoteStreamingButton;						// 22/09/2013 ECU added
	Button 		requestDeviceDetailsButton;					// 10/11/2016 ECU added
	Button 		sendHelloButton;							// 05/04/2016 ECU added
	Button 		transmitButton;								// 29/07/2013 ECU added
	// =============================================================================
	static		int			actionRequired	= StaticData.NO_RESULT;		// 17/03/2015 ECU added
	static 		Context		context;									// 17/03/2015 ECU added
	static		String		deviceIPAddress;							// 17/03/2015 ECU added
	static 		String []	devices			= null;						// 16/03/2015 ECU created
	static 		String		networkMask;								// 12/11/2016 ECU added
	// ==============================================================================
	
	/* ============================================================================= */
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null) 
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_tcp);
			// ---------------------------------------------------------------------
			// 17/03/2015 ECU remember the context
			// ---------------------------------------------------------------------
			context		= this;
			// ---------------------------------------------------------------------
			// 20/09/2013 ECU added to set portrait mode
			// ---------------------------------------------------------------------
			setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			// ---------------------------------------------------------------------
			// 25/02/2014 ECU make sure that the keyboard does not pop up
			// ---------------------------------------------------------------------
			getWindow().setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			// ---------------------------------------------------------------------
			// 20/09/2013 ECU set up the various views into the form
			// ---------------------------------------------------------------------
	    
			// ---------------------------------------------------------------------
			broadcastButton 		= (Button) findViewById	(R.id.tcp_broadcast_button);			// 26/07/2013 ECU added
			clientButton 			= (Button) findViewById (R.id.tcp_client_button);
			cloneButton 			= (Button) findViewById (R.id.tcp_clone_button);
			cloneTransmitButton 	= (Button) findViewById (R.id.tcp_clone_transmit_button);
			datagramButton 			= (Button) findViewById (R.id.tcp_datagram_button);
			deviceNameButton 		= (Button) findViewById (R.id.device_name_button);
			discoverButton 			= (Button) findViewById (R.id.tcp_discover_button);				// 29/07/2013 ECU added
			monitorADeviceButton 	= (Button) findViewById (R.id.tcp_monitor_a_device_button);		// 26/04/2016 ECU added
			remoteStreamingButton 	= (Button) findViewById (R.id.remote_streaming_button);			// 22/09/2013 ECU added
			requestDeviceDetailsButton 	
									= (Button) findViewById (R.id.tcp_request_device_details_button);
																									// 10/11/2016 ECU added
			sendHelloButton 	    = (Button) findViewById (R.id.tcp_send_hello_button);			// 05/04/2016 ECU added
			transmitButton 			= (Button) findViewById (R.id.tcp_transmit_button);				// 29/07/2013 ECU added
			// ---------------------------------------------------------------------
			// 17/03/2015 ECU set up the button listener
			// ---------------------------------------------------------------------
			broadcastButton.setOnClickListener (buttonListener);									// 26/07/2013 ECU added
			clientButton.setOnClickListener (buttonListener);
			cloneButton.setOnClickListener (buttonListener);	
			cloneTransmitButton.setOnClickListener (buttonListener);	
			datagramButton.setOnClickListener (buttonListener);	
			deviceNameButton.setOnClickListener (buttonListener);
			discoverButton.setOnClickListener (buttonListener);										// 29/07/2013 ECU added
			monitorADeviceButton.setOnClickListener (buttonListener);								// 26/04/2016 ECU added
			remoteStreamingButton.setOnClickListener (buttonListener);
			requestDeviceDetailsButton.setOnClickListener (buttonListener);							// 10/11/2016 ECU added
			sendHelloButton.setOnClickListener (buttonListener);									// 05/04/2016 ECU added
			transmitButton.setOnClickListener (buttonListener);										// 29/07/2013 ECU added
			// ---------------------------------------------------------------------
			// 11/11/2016 ECU set up the legend on the discovery button
			// ---------------------------------------------------------------------
			discoverButton.setText (Utilities.twoLineButtonLegend (this,
									getString (R.string.network_discovery), 
									getString (R.string.mask_network_discovery)));
			discoverButton.setOnLongClickListener (buttonListenerLong);
			// ---------------------------------------------------------------------
			// 25/01/2014 ECU indicate that want to network on main thread
			// 12/03/2014 ECU changed because method moved into APIIssues
			// --------------------------------------------------------------------- 	
			APIIssues.NetworkOnMainUIThread (android.os.Build.VERSION.SDK_INT);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been recreated after being destroyed
			//                by Android
			// ---------------------------------------------------------------------
			finish ();
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	@Override
	public boolean onCreateOptionsMenu (Menu menu) 
	{
		// -------------------------------------------------------------------------
		// Inflate the menu; this adds items to the action bar if it is present.
		// -------------------------------------------------------------------------
		return true;
	}
	// =============================================================================
    private View.OnClickListener buttonListener = new View.OnClickListener() 
	{
    	// -------------------------------------------------------------------------
		@Override
		public void onClick(View view) 
		{	
			//----------------------------------------------------------------------
			// 17/03/2015 ECU now process depending on which button pressed
			//----------------------------------------------------------------------
			Intent localIntent;
			// ---------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.device_name_button:
					// -------------------------------------------------------------
					// 17/03/2015 ECU set the name of a specified device
					//            ECU set up the action required
					//            ECU changed to use local method
					// -------------------------------------------------------------
					getIPAddressAndAction (ACTION_DEVICE_NAME, "Select the Device to be Renamed",true);
					// -------------------------------------------------------------	 
					break;
				// -----------------------------------------------------------------
				case R.id.remote_streaming_button:
					// -------------------------------------------------------------
					// 17/03/2015 ECU set the IP address of the remote streaming 
					//                device
					//            ECU set up the action required
					//            ECU changed to use local method
					// -------------------------------------------------------------
					getIPAddressAndAction (ACTION_REMOTE_STREAM, "Select the Remote Streaming Device",false);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.tcp_broadcast_button:
					// -------------------------------------------------------------
					// 17/03/2015 ECU broadcast a specified file
					// -------------------------------------------------------------
					// 26/07/2013 ECU added
					// 31/01/2015 ECU change the name of the method called
					// -------------------------------------------------------------
					Utilities.sendSocketMessageSendStringToAllDevices 
					       (getBaseContext(), PublicData.deviceDetails,PublicData.socketNumber, "command locate");	
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.tcp_client_button:
					//--------------------------------------------------------------
					// 17/03/2015 ECU request to connect to a TCP server
					//-------------------------------------------------------------- 
					// 22/09/2013 ECU tidy up to used stored values rather than literal
					// 16/03/2015 ECU changed to use the input IP address rather that the
					//                'LOCAL_HOST' variable
					// 17/03/2015 ECU changed to use the dialogue
					// -------------------------------------------------------------
					getIPAddressAndAction (ACTION_TCP_CLIENT, "Select the required TCP Server",true);
					// -------------------------------------------------------------		
					break;
				// -----------------------------------------------------------------
				case R.id.tcp_clone_button:
					// -------------------------------------------------------------
					// 17/03/2015 ECU put this device into 'clone' mode
					// -------------------------------------------------------------
					localIntent = new Intent (getBaseContext(),RunActivity.class);		
					Bundle bundle = new Bundle();  
					bundle.putSerializable (StaticData.PARAMETER_INTENT_DATA, 
							new IntentData ("CloneActivity",StaticData.REQUEST_CODE_FINISH,TCPActivity.class,"CloneComplete"));
					localIntent.putExtras (bundle);	
					// -------------------------------------------------------------
					// 17/10/2014 ECU change to feed through the correct request code
					// -------------------------------------------------------------
					startActivityForResult (localIntent,StaticData.REQUEST_CODE_FINISH);		
					break;
				// -----------------------------------------------------------------
				case R.id.tcp_clone_transmit_button:
					// -------------------------------------------------------------
					// 17/03/2015 ECU use this device to clone its files to a specified
					//                device
					// -------------------------------------------------------------
					localIntent = new Intent (getBaseContext(),ClonerActivity.class); 
					startActivityForResult (localIntent,0);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.tcp_datagram_button:
					// -------------------------------------------------------------
					// 17/03/2015 ECU send out a datagram
					//            ECU changed to use the dialogue
					// -------------------------------------------------------------
					getIPAddressAndAction (ACTION_DATAGRAM,"Select the device to receive datagram",true);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.tcp_discover_button:
					// -------------------------------------------------------------
					// 17/03/2015 ECU initiate the discovery activity
					// 10/11/2016 ECU indicate that can only discover Class C networks
					// -------------------------------------------------------------
					if (PublicData.networkMask.equalsIgnoreCase(StaticData.NETWORK_MASK_CLASS_C))
					{
						Intent myIntent = new Intent (getBaseContext(),DiscoverNetwork.class);
						startActivityForResult (myIntent,0);
					}
					else
					{
						// ---------------------------------------------------------
						// 10/11/2016 ECU indicate that cannot discover non class C networks
						// ---------------------------------------------------------
						Utilities.popToast (context.getString (R.string.networks_cannot_discover),true);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.tcp_monitor_a_device_button:
					// -------------------------------------------------------------
					// 26/04/2016 ECU created to monitor a device
					// -------------------------------------------------------------
					if (PublicData.monitoredIPAddress != null)
					{
						Utilities.popToastAndSpeak (String.format (context.getString (R.string.monitor_device_currently_monitoring,Utilities.GetDeviceName (PublicData.monitoredIPAddress))),true);
					}
					// -------------------------------------------------------------
					getIPAddressAndAction (ACTION_MONITOR_A_DEVICE, "Select the Device to be Monitored",true);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.tcp_request_device_details_button:
					// -------------------------------------------------------------
					// 10/11/2016 ECU handle a request for a device's details
					//                request the IP address
					// 12/11/2016 ECU changed to use the IPAdd... method
					// -------------------------------------------------------------
					DialogueUtilities.IPAddressInput (context,
				   			   					 	  context.getString (R.string.IP_address_request),
				   			   					 	  context.getString (R.string.enter_IP_address_summary),
				   			   					 	  PublicData.ipAddress,
				   			   					 	  Utilities.createAMethod (TCPActivity.class,"ConfirmIPAddress",""),
				   			   					 	  null);
					// -------------------------------------------------------------
					break;
				// -------------------------------------------------- ---------------
				case R.id.tcp_send_hello_button:
					// -------------------------------------------------------------
					// 05/04/2016 ECU indicate that the initial 'hello' message is
					//                being sent
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString (R.string.sending_hello_message),true);
					Devices.sendHelloMessage ();
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.tcp_transmit_button:
					// -------------------------------------------------------------
					// 17/03/2015 ECU set up the action required
					//            ECU changed to use local method
					// -------------------------------------------------------------
					getIPAddressAndAction (ACTION_TRANSMIT_FILE, "Select the Device to send the File to",true);
					break;
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	private View.OnLongClickListener buttonListenerLong = new View.OnLongClickListener () 
	{		
		@Override
		public boolean onLongClick(View view) 
		{	
			// ---------------------------------------------------------------------
			// 11/11/2016 ECU now process depending on which button pressed
			// ---------------------------------------------------------------------
			switch (view.getId()) 
			{
				// ------------------------------------------------------------------
				case R.id.tcp_discover_button: 
				{
					// -------------------------------------------------------------
					// 11/11/2016 ECU get network mask before discovering
					// -------------------------------------------------------------
					// 11/11/2016 ECU request the network mask before starting a 
					//                discovery
					// 12/11/2016 ECU changed to use the IPAdd... method
					// -------------------------------------------------------------
					DialogueUtilities.IPAddressInput (context,
				   			   					      context.getString (R.string.IP_mask_request),
				   			   					      context.getString (R.string.enter_IP_mask_summary),
				   			   					      PublicData.networkMask,
				   			   					      Utilities.createAMethod (TCPActivity.class,"ConfirmNetworkMask",""),
				   			   					      null);
					// -------------------------------------------------------------
					break;
				}		
			}
			// ---------------------------------------------------------------------
			return true;
		}
	};
	//==============================================================================
	 @Override
	 public void onActivityResult (int theRequestCode, int theResultCode, Intent theIntent) 
	 {
		 // ------------------------------------------------------------------------
		 // 11/12/2013 ECU called when an activity returns a result
		 // ------------------------------------------------------------------------	
		 // 30/08/2013 ECU check if the result of the barcode activity
		 // ------------------------------------------------------------------------
		 if (theRequestCode == StaticData.REQUEST_CODE_FILE)
		 {
			 // --------------------------------------------------------------------
			 // 06/12/2013 ECU added
			 // --------------------------------------------------------------------
			 if (theResultCode == RESULT_OK)
			 {
				transmitTheFile (deviceIPAddress,theIntent.getStringExtra(StaticData.PARAMETER_FILE_PATH));		  		
			 }
			 else 
		 	 if (theResultCode == RESULT_CANCELED) 
		 	 {
		 	 }
		 }
		 else
		 // ------------------------------------------------------------------------
		 // 17/10/2014 ECU check if the calling activity is to 'finish'
		 // ------------------------------------------------------------------------
	     if (theRequestCode == StaticData.REQUEST_CODE_FINISH)
	     {
	    	 // --------------------------------------------------------------------
	    	 // 17/10/2014 ECU confirm that a 'finish' is required
	    	 // --------------------------------------------------------------------
	    	 if (theResultCode == StaticData.RESULT_CODE_FINISH)
	    	 {
	    		 setResult (StaticData.RESULT_CODE_FINISH);
	    		 finish ();
	    	 }
	     }
	 }
	// =============================================================================
	 @Override
	 public void onDestroy()
	 {	
		 // -------------------------------------------------------------------------
		 // 23/10/2015 ECU added
		 // -------------------------------------------------------------------------
		 super.onDestroy();
		 // -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override 
	protected void onPause() 
	{
		super.onPause(); 
	} 
	/* ============================================================================= */
	@Override 
	protected void onResume() 
	{ 	
		super.onResume(); 
	}
	 /* ============================================================================ */
	 public static void clientSide (Context theContext,String theIPAddress,int thePort)
	 {
		 try 
		 {         
			 	PrintWriter output;     
			 	BufferedReader input;  
			 	String incomingMessage;
			 	char [] incomingBuffer = new char [1000];
			
			 	InetAddress serverAddress = InetAddress.getByName(theIPAddress);  
			 	                  
			 	Socket theSocket = new Socket (serverAddress, thePort);               
			 
			 	try 
			 	{             
			 		output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(theSocket.getOutputStream())), true);                                
			 		input = new BufferedReader(new InputStreamReader(theSocket.getInputStream()));  
			 		
			 		output.println ("GET /index.html HTTP/1.1\n\n");
			 		output.flush ();
			 		int charRead = input.read(incomingBuffer, 0, 1000);
			 		incomingMessage = new String (incomingBuffer);
			 		// --------------------------------------------------------------
			 		// 08/11/2013 ECU use the custom toast
					// --------------------------------------------------------------
					Utilities.popToast ("received : " + charRead + "\n" + incomingMessage);			              
				 } 
			 	 catch (Exception theException) 
			 	 {  
			 		 // ------------------------------------------------------------
			 		 // 08/11/2013 ECU use the custom toast
			 		 // ------------------------------------------------------------
			 		 Utilities.popToast ("Exception Writing : " + theException);            
				 } 
			 	 finally
			 	 {                
					 theSocket.close();             
				 }           
			 } 
		 	 catch (Exception theException)
			 { 
		 		 // ----------------------------------------------------------------
		 		 // 08/11/2013 ECU use the custom toast
		 		 // ----------------------------------------------------------------
		 		 Utilities.popToast ("Exception : " + theException);  
			 }   
	 }
	 /* ============================================================================ */
	 public static void CloneComplete (String theMessage)
	 {
		 Utilities.logMessage (TAG + " " + theMessage);
 	 }
	 /* ============================================================================ */
	 public static void Cancel (int theIndex)
	 {
	 }
	 // ============================================================================
	 public static void Confirm (int theIndex)
	 {
		 // ------------------------------------------------------------------------
		 // 17/03/2015 ECU switch on the action required
		 // ------------------------------------------------------------------------
		 switch (actionRequired)
		 {
		 	// ---------------------------------------------------------------------
		 	case ACTION_DATAGRAM:
		 		// -----------------------------------------------------------------
		 		Utilities.sendDatagramType (context, Devices.returnIPAddress (devices [theIndex]),StaticData.SOCKET_MESSAGE_PLAY);
			 	// -----------------------------------------------------------------
		 		break;
		 	// ---------------------------------------------------------------------
		 	case ACTION_DEVICE_NAME:
		 		// -----------------------------------------------------------------
		 		deviceIPAddress =  Devices.returnIPAddress (devices [theIndex]);
		 		DialogueUtilities.textInput(context,"New Device Name",
			    		   "Please enter the name that is to be associated with " + deviceIPAddress,
							Utilities.createAMethod (TCPActivity.class,"ConfirmDeviceName",""),
							Utilities.createAMethod (TCPActivity.class,"Cancel",""));
			 	// -----------------------------------------------------------------
		 		break;
		 	// ---------------------------------------------------------------------
		 	case ACTION_MONITOR_A_DEVICE:
		 		// -----------------------------------------------------------------
		 		// 28/04/2016 ECU check if monitored address has changed
		 		// -----------------------------------------------------------------
		 		if ((PublicData.monitoredIPAddress == null) || 
		 			 !PublicData.monitoredIPAddress.equalsIgnoreCase (Devices.returnIPAddress (devices [theIndex])))
		 		{
		 			// -------------------------------------------------------------
		 			// 28/04/2016 ECU if the address is already set and it has been
		 			//                changed then send a message to current monitored
		 			//                device to tell it to stop
		 			// -------------------------------------------------------------
		 			if (PublicData.monitoredIPAddress != null)
		 			{
		 				// ---------------------------------------------------------
		 				// 28/04/2016 ECU the device that is current being monitored
		 				//                needs to be told to stop
		 				// ---------------------------------------------------------
		 				Utilities.sendSocketMessageSendTheObject (context,
		 														  PublicData.monitoredIPAddress,
		 														  PublicData.socketNumberForData, 
		 														  StaticData.SOCKET_MESSAGE_MONITOR,
		 														  (Object) (false));
		 				// ---------------------------------------------------------
		 				// 28/04/2016 ECU tell the user which is going on
		 				// ---------------------------------------------------------
		 				Utilities.popToastAndSpeak (String.format (context.getString (R.string.monitor_device_finished,Utilities.GetDeviceName (PublicData.monitoredIPAddress))),true);
		 			}
		 			// -------------------------------------------------------------
			 		// 28/04/2016 ECU remember the address of the device to be monitored
			 		// -------------------------------------------------------------
		 			PublicData.monitoredIPAddress = Devices.returnIPAddress (devices [theIndex]);
		 			// -------------------------------------------------------------
		 			// 28/04/2016 ECU default to just displaying monitored data
		 			// -------------------------------------------------------------
		 			PublicData.monitorDataAction = false;
		 			// -------------------------------------------------------------
		 			// 28/04/2016 ECU ask whether the received data is to be actioned or
		 			//                not
		 			// -------------------------------------------------------------
		 			DialogueUtilities.yesNo (context,  
		 									 context.getString (R.string.monitor_data_action_title),
		 									 context.getString (R.string.monitor_data_action_summary),
		 									 null,
		 									 true,"Process",Utilities.createAMethod (TCPActivity.class,"ActionMonitorDataMethod",(Object) null),
		 									 true,"Display",null);
		 		}
		 		else
		 		{
		 			// -------------------------------------------------------------
		 			// 28/04/2016 ECU tell the user that monitoring is being switched off
		 			// -------------------------------------------------------------
		 			Utilities.popToastAndSpeak (String.format (context.getString (R.string.monitor_device_finished,Utilities.GetDeviceName (PublicData.monitoredIPAddress))),true);
		 			// -------------------------------------------------------------
		 			// 28/04/2016 ECU want to reset the address to indicate monitoring
		 			//                is cancelled
		 			// -------------------------------------------------------------
		 			PublicData.monitoredIPAddress = null;
		 			// -------------------------------------------------------------
		 		}
		 		// ------------------------------------------------------------------
		 		// 28/04/2016 ECU irrespective of the outcome of the above dialogue
		 		//                send off the message to the device to be monitored
		 		//            ECU changed to send the object indicating whether
		 		//                monitoring is to be switched on or off
		 		// ------------------------------------------------------------------
		 		Utilities.sendSocketMessageSendTheObject (context,
		 												  Devices.returnIPAddress (devices [theIndex]),
		 												  PublicData.socketNumberForData, 
		 												  StaticData.SOCKET_MESSAGE_MONITOR,
		 												  (Object) (!(PublicData.monitoredIPAddress == null)));
		 		// -----------------------------------------------------------------
		 		break;
		 	// ---------------------------------------------------------------------
		 	case ACTION_REMOTE_STREAM:
		 		// -----------------------------------------------------------------
		 		// 16/03/2015 ECU set the selected destination
		 		// -----------------------------------------------------------------
		 		PublicData.streamingDestination = Devices.returnIPAddress (devices [theIndex]);	
		 		// -----------------------------------------------------------------
		 		// 16/03/2015 ECU confirm the setting
		 		// -----------------------------------------------------------------
		 		Utilities.popToast ("Remote Stream Device set to " + PublicData.streamingDestination);
		 		// -----------------------------------------------------------------
		 		break;
		 	// ---------------------------------------------------------------------
		 	case ACTION_TCP_CLIENT:
		 		// -----------------------------------------------------------------
		 		clientSide (context,Devices.returnIPAddress (devices [theIndex]),PublicData.socketNumber);
		 		// -----------------------------------------------------------------
		 		break;
		 	// ---------------------------------------------------------------------
		 	case ACTION_TRANSMIT_FILE:
		 		// -----------------------------------------------------------------
		 		// 17/03/2015 ECU remember the IP address that has been selected
		 		// -----------------------------------------------------------------
		 		deviceIPAddress =  Devices.returnIPAddress (devices [theIndex]);
		 		// -----------------------------------------------------------------
		 		// 11/12/2013 ECU try and select the full pathname of the transmit file
		 		// 17/03/2015 ECU change to use the new method with the final 'non-null'
		 		//                argument to indicate that as soon as the file is
		 		//                selected then the activity will return
		 		// 18/12/2015 ECU last argument changed from "" to true
		 		// -----------------------------------------------------------------
		 		Utilities.PickAFile ((Activity)context,PublicData.projectFolder,context.getString(R.string.audio_file_extension),true);
		 		// -----------------------------------------------------------------
		 		break;
		 	// ---------------------------------------------------------------------
		 }	 
	 }
	 // ============================================================================
	 public static void ConfirmDeviceName (String theName)
	 {
		 // ------------------------------------------------------------------------
		 // 17/03/2015 ECU created to rename the device using dialogue methods
		 // ------------------------------------------------------------------------
		 Utilities.setDeviceName (context,deviceIPAddress,theName);
		 // ------------------------------------------------------------------------
		 Utilities.popToast ("Device " + deviceIPAddress + " has been renamed to " + theName);
	 }
	 // ============================================================================
	 public static void ConfirmIPAddress (String theIPAddress)
	 {
		 // ------------------------------------------------------------------------
		 // 10/11/2016 ECU created to request details from the device whose IP
		 //                address has been specified
		 // ------------------------------------------------------------------------
		 if (Utilities.validateIPAddress (theIPAddress))
		 {
			 // --------------------------------------------------------------------
			 // 10/11/2016 ECU the entered IP address was valid
			 // --------------------------------------------------------------------
			 PublicData.requestAddress = theIPAddress;
			 // --------------------------------------------------------------------
			 // 10/11/2016 ECU confirm the request
			 // --------------------------------------------------------------------
			 Utilities.popToast (String.format (context.getString (R.string.device_details_being_requested_format),theIPAddress),true);
			 // --------------------------------------------------------------------
		 }
		 else
		 {
			 // --------------------------------------------------------------------
			 // 10/11/2016 ECU invalid IP address entered
			 // --------------------------------------------------------------------
			 Utilities.popToast (context.getString (R.string.IP_address_invalid),true);
			 // --------------------------------------------------------------------
		 }
		 // ------------------------------------------------------------------------	
	 }
	 // ============================================================================ 
	 public static void ConfirmNetworkMask (String theNetworkMask)
	 {
		 // ------------------------------------------------------------------------
		 // 11/11/2016 ECU handle the input network mask
		 // ------------------------------------------------------------------------
		 if (Utilities.validateIPAddress (theNetworkMask))
		 {
			 // --------------------------------------------------------------------
			 // 11/11/2016 ECU the entered mask was valid
			 // -------------------------------------------------------------------=
			 // 12/11/2016 ECU remember the mask
			 // --------------------------------------------------------------------
			 networkMask = theNetworkMask;
			 // --------------------------------------------------------------------
			 // 12/11/2016 ECU request the timeout that will be used for the
			 //                discovery timeout
			 // --------------------------------------------------------------------
			 DialogueUtilities.sliderChoice (context,
					 						 context.getString (R.string.discovery_timeout_title),
					 						 context.getString (R.string.discovery_timeout_summary),
					 						 R.drawable.timer,
					 						 null,
					 						 StaticData.DISCOVERY_TIMEOUT,
					 						 1,
					 						 (StaticData.DISCOVERY_TIMEOUT * 2),
					 						 context.getString (R.string.confirm),
					 						 Utilities.createAMethod (TCPActivity.class,"SetDiscoveryTimeout",0),
					 						 null,
					 						 null);
			 // --------------------------------------------------------------------
		 }
		 else
		 {
			 // --------------------------------------------------------------------
			 // 10/11/2016 ECU invalid IP address entered
			 // --------------------------------------------------------------------
			 Utilities.popToast (context.getString (R.string.IP_address_invalid),true);
			 // --------------------------------------------------------------------
		 }
		 // ------------------------------------------------------------------------	
	 }
	 // ============================================================================
	 void getIPAddressAndAction (int theAction,String theTitle,boolean theAllDevicesFlag)
	 {
		 // ------------------------------------------------------------------------
		 // 17/03/2015 ECU created to use dialogue utilities to obtain a device
		 //                and take the appropriate action
		 //					theAllDevicesFlag = true  include 'this' device
		 //                                   = false exclude 'this' device
		 // ------------------------------------------------------------------------
		 actionRequired = theAction;
		 // ------------------------------------------------------------------------
		 // 16/03/2015 ECU changed to use dialogue option
		 // ------------------------------------------------------------------------
		 DialogueUtilities.singleChoice(TCPActivity.this, theTitle,
				 						(devices = Utilities.deviceListAsArray(theAllDevicesFlag)),0, 
				 						Utilities.createAMethod(TCPActivity.class,"Confirm",0),
				 						Utilities.createAMethod(TCPActivity.class,"Cancel",0));
		 // ------------------------------------------------------------------------	 
	 }
	 // ============================================================================
	 public static void SetDiscoveryTimeout (int theTimeout)
	 {
		 // ------------------------------------------------------------------------
		 // 12/11/2016 ECU created to get the discovery timeout and initiate the
		 //                discovery
		 // ------------------------------------------------------------------------
		 Intent myIntent = new Intent (context,DiscoverNetwork.class);
		 // ------------------------------------------------------------------------
		 // 11/11/2016 ECU add the parameters that are to be used for
		 //                the discovery
		 // ------------------------------------------------------------------------
		 myIntent.putExtra (StaticData.PARAMETER_IP_ADDRESS,PublicData.ipAddress);
		 myIntent.putExtra (StaticData.PARAMETER_NETWORK_MASK,networkMask);
		 myIntent.putExtra (StaticData.PARAMETER_TIMER,theTimeout);
		 // ------------------------------------------------------------------------
		 context.startActivity (myIntent);
		 // ------------------------------------------------------------------------	 
	 }
	 // ============================================================================
	 void transmitTheFile (String theIPAddress,String theFileName)
	 {		 
		 // --------------------------------------------------------------------	 
		 // --------------------------------------------------------------------
		 // 29/07/2013 ECU added
		 // 30/07/2013 ECU changed the code to use 'byte []' rather than 'char []'
		 // 31/07/2013 ECU change to use the file name entered rather than 
		 //                something held as a string value in resources
		 // 11/12/2013 ECU change to assume that the full path has been entered
		 // 15/01/2014 ECU just tidy up the file name code - just in case nothing entered
		 // --------------------------------------------------------------------
		 if (theFileName.equalsIgnoreCase (""))
		 {
			 Utilities.popToast ("You must enter the name of a file to transmit");
		 }
		 else
		 {
			 // ----------------------------------------------------------------
			 // 15/01/2014 ECU file exists
			 // ----------------------------------------------------------------
			 byte [] bigBuffer = Utilities.readAFile(theFileName);
			 // ----------------------------------------------------------------
			 // 15/01/2014 ECU check if got a proper response
			 // ----------------------------------------------------------------
			 if (bigBuffer != null)
			 {
				 // ------------------------------------------------------------
				 // 03/02/2015 ECU can only process 'wav' files of the correct
				 //                format so check this
				 // ------------------------------------------------------------
				 byte [] wavFileFormat = new byte [AudioRecorder.WAV_FILE_FORMAT.length];
				 System.arraycopy(bigBuffer,AudioRecorder.WAV_FILE_FORMAT_OFFSET,wavFileFormat,0,wavFileFormat.length);
				 if (Arrays.equals(wavFileFormat,AudioRecorder.WAV_FILE_FORMAT))
				 {
					 // --------------------------------------------------------
					 // 02/02/2015 ECU used to play the buffer locally before
					 //                transmission but not needed any more
					 // --------------------------------------------------------
					 // Utilities.playFromBuffer(getBaseContext(), bigBuffer, bigBuffer.length);
					 // --------------------------------------------------------
					 // 31/01/2015 ECU changed the method to .... SendTheBuffer
					 // 21/03/2015 ECU pass the message type as an argument
					 // --------------------------------------------------------
					 if (!theIPAddress.startsWith("all"))
					 {				
						 Utilities.socketMessagesSendTheBuffer 
						 	(getBaseContext(),
						 	 true,
						 	 theIPAddress,
						 	 PublicData.socketNumberForData,
						 	 StaticData.SOCKET_MESSAGE_WAV_FILE,
						 	 bigBuffer,
						 	 bigBuffer.length);
					 }
					 else
					 {
						 // ----------------------------------------------------
						 // 31/07/2013 ECU loop for all registered devices
						 // 21/03/2015 ECU pass the message type as an argument
						 // 22/03/2015 ECU changed the logic to reflect the change 
						 //                of deviceDetails to List<Devices>
						 // ----------------------------------------------------
						 if (PublicData.deviceDetails != null)
						 {
							 for (int theDevice = 0 ; theDevice < PublicData.deviceDetails.size(); theDevice++)
							 {
								 Utilities.socketMessagesSendTheBuffer
								 	(getBaseContext(),
								 	 true,
								 	 PublicData.deviceDetails.get(theDevice).IPAddress,
								 	 PublicData.socketNumberForData,
								 	 StaticData.SOCKET_MESSAGE_WAV_FILE,
								 	 bigBuffer,
								 	 bigBuffer.length);
							 }
						 }
					 }
				 }
				 else
				 {
					 // --------------------------------------------------------
					 // 03/02/2015 ECU the specified file is not a 'wav' file
					 // --------------------------------------------------------
					 Utilities.popToast (theFileName + " is not of 'wav' format");
					 // --------------------------------------------------------
				 }
			 }
			 else
			 {
				 // ------------------------------------------------------------
				 // 15/01/2014 ECU indicate that the file does not exist
				 // ------------------------------------------------------------
				 Utilities.popToast (theFileName + " does not exist");
				 // ------------------------------------------------------------
			 }
		 }			 
	 }
	 // ============================================================================
	 
	 // ============================================================================
	 public static void ActionMonitorDataMethod (Object theDummyArgument)
	 {
		 // ------------------------------------------------------------------------
		 // 28/04/2016 ECU created to set the actioning of monitored data
		 // ------------------------------------------------------------------------
		 PublicData.monitorDataAction = true;
		 // ------------------------------------------------------------------------
	 }
	 // ============================================================================
}

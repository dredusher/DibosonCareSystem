package com.usher.diboson;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BlueToothService extends Service 
{
	/* ============================================================================= */
	// 31/12/2013 ECU created to try and tidy up bluetooth functions
	// =============================================================================
	// Tested
	// ======
	/* ============================================================================= */
	private final static String TAG = "BlueToothService";
	/* ============================================================================= */
	// 01/01/2014 ECU declare a Universally Unique ID - the value below is that suggested
	//                in the 'BluetoothDevice' official documentation
	// -----------------------------------------------------------------------------
	private static final UUID 	   LOCAL_UUID 		= UUID.fromString ("00001101-0000-1000-8000-00805F9B34FB");
	// -----------------------------------------------------------------------------
	// 01/01/2014 ECU specify the MAC address of the JY_MCU device that is part of
	//                the infrared blaster. Easiest way to get this was to use a
	//                PC where the JY_MCU normally appears as 'HC-06', the address
	//                can be found under the 'bluetooth' tab of 'properties'
	// 11/04/2014 ECU added the MAC address for the new JY_MCU device
	// ----------------------------------------------------------------------------
	public  static final String    MAC_ADDRESS 		= "20:13:11:01:21:45";
	//private static final String    MAC_ADDRESS_NANO	= "20:13:12:05:10:82";
	/* ============================================================================ */
	static final int  	MESSAGE_REMOTE_CODE  = 1;
	static final int  	MESSAGE_REQUEST		 = 2;
	/* ============================================================================ */
						BluetoothAdapter	bluetoothAdapter 	= null;
						BluetoothDevice		bluetoothDevice 	= null;
						BluetoothSocket 	bluetoothSocket 	= null;	
			static		Context				context;						// 27/02/2018 ECU added
						Messenger			messenger			= null;
						OutputStream		outputStream		= null;
	public static 		TimerHandler 		timerHandler		= null;		// 15/10/2015 ECU added
																			// 24/07/2016 ECU preset to null
	/* ============================================================================= */
	@Override
	public IBinder onBind(Intent arg0) 
	{ 
		// -------------------------------------------------------------------------
		// 31/12/2013 ECU return an interface to our messenger
		// 15/02/2014 ECU check for a null 'messenger'
		// -------------------------------------------------------------------------
		if (messenger !=  null)
			return messenger.getBinder ();
		// -------------------------------------------------------------------------
		// 15/02/2014 ECU indicate not working - hope this is correct
		// -------------------------------------------------------------------------
		return null;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public void onCreate()
	{
		// -------------------------------------------------------------------------
		// 27/02/2018 ECU remember the context for subsequent use
		// -------------------------------------------------------------------------
		context = this;
		// -------------------------------------------------------------------------
		// 27/02/2018 ECU set up the timer handler (moved here from onStart)
		// -------------------------------------------------------------------------
		timerHandler = new TimerHandler ();
		// -------------------------------------------------------------------------
		// 12/10/2015 ECU added the super call
		// -------------------------------------------------------------------------
		super.onCreate();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public void onStart (Intent intent, int startId) 
	{
		// -------------------------------------------------------------------------
		// 31/12/2013 ECU called when the service is started
		// -------------------------------------------------------------------------
		// 27/02/2018 ECU request that a bluetooth connection to the remote device 
		//                be established
		// -------------------------------------------------------------------------
		timerHandler.sendEmptyMessage (StaticData.MESSAGE_BLUETOOTH_CONNECT);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public void onDestroy () 
	{
		// -------------------------------------------------------------------------
		// 31/12/2013 ECU close down issues associated with the bluetooth interface
        // -------------------------------------------------------------------------
    	try
    	{
    		// ---------------------------------------------------------------------
    		// 31/12/20`13 ECU close the output stream
    		// ---------------------------------------------------------------------
    		if (outputStream != null)
    			outputStream.close ();
    		// ---------------------------------------------------------------------
    		// 31/12/2013 ECU close the socket and hence the connection
    		// ---------------------------------------------------------------------
    		if (bluetoothSocket != null)
    			bluetoothSocket.close ();
    		// ---------------------------------------------------------------------
			// 02/03/2014 ECU indicate that service is no longer running
    		// 28/02/2018 ECU moved here from the handler
			// ---------------------------------------------------------------------
			PublicData.blueToothService = false;
			// ---------------------------------------------------------------------
    	}
    	catch (IOException theException)
    	{
    		// ---------------------------------------------------------------------
    		// 31/12/2013 ECU should only get here if either the stream or
    		//                socket has failed to open correctly
    		// ---------------------------------------------------------------------
    	}
	}	
	// =============================================================================
	final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() 
	{
		// -------------------------------------------------------------------------
		// 01/03/2018 ECU create to receive any 'disconnect' messages for the
		//                bluetooth device
		// -------------------------------------------------------------------------
		public void onReceive (Context theContext, Intent theIntent)
		{
			// ---------------------------------------------------------------------
			// 01/03/2018 ECU am only interested in 'disconnect' events
			// ---------------------------------------------------------------------
			if ((theIntent.getAction ()).equals (BluetoothDevice.ACTION_ACL_DISCONNECTED))
			{
				// -----------------------------------------------------------------
				// 01/03/2018 ECU tell the handler what has happened
				// -----------------------------------------------------------------
				timerHandler.sendEmptyMessage (StaticData.MESSAGE_BLUETOOTH_DISCONNECTED);
				// -----------------------------------------------------------------
		    }
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	class ConnectThread extends Thread 
	{	
		// -------------------------------------------------------------------------
		// 27/02/2018 ECU because the 'connect' operation has a fixed, unchangeable
		//                timeout of 10 seconds and 'blocks' then do it via this
		//                thread
		// -------------------------------------------------------------------------
		@Override
		public void run ()
		{
			try 
			{    
				// -----------------------------------------------------------------
				// 31/12/2013 ECU now try and connect to the bluetooth device
				// -----------------------------------------------------------------
				bluetoothSocket.connect ();
				// -----------------------------------------------------------------
				// 31/12/2013 ECU get an output stream for data
				// -----------------------------------------------------------------
				outputStream = bluetoothSocket.getOutputStream ();	
				// -----------------------------------------------------------------
				// 27/02/2018 ECU tell the handler that a connection has been
				//                established
				// -----------------------------------------------------------------
				timerHandler.sendEmptyMessage (StaticData.MESSAGE_BLUETOOTH_CONNECTED);
				// -----------------------------------------------------------------
			}
			catch (IOException theException)
			{    
				// -----------------------------------------------------------------
				// 26/02/2018 ECU Note - an exception normally occurs when a 'timeout'
				//                       occurs because a remote bluetooth device cannot
				//                 		 be connected to
				// 27/02/2018 ECU indicate that the timeout has occurred
				// -----------------------------------------------------------------
				timerHandler.sendEmptyMessage (StaticData.MESSAGE_BLUETOOTH_TIMEOUT);
				// -----------------------------------------------------------------
			}       
		}
	};
	// =============================================================================
	void connectionTasks ()
	{
		// -------------------------------------------------------------------------
		// 27/02/2018 ECU created to handle those tasks that are to be performed
		//                when a connection to the remote device has been established
		// -------------------------------------------------------------------------
		// 31/12/2013 ECU set up the message handler
		// 16/03/2014 ECU Create a new Messenger pointing to the given Handler. 
		//                Any Message objects sent through this Messenger will 
		//                appear in the Handler as if Handler.sendMessage(Message)
		//                had been called directly.
		// -------------------------------------------------------------------------
		messenger = new Messenger (new IncomingHandler());
		// -------------------------------------------------------------------------
		// 02/03/2014 ECU indicate that service seems to have stated OK
		// -------------------------------------------------------------------------
		PublicData.blueToothService = true;
		// -------------------------------------------------------------------------
		// 18/11/2017 ECU read in the preset TV channel data (held in the raw file)
		//                and then check if there are any changes required for the 
		//                TV channels
		// 27/02/2018 ECU changed 'this' to 'context'
		// -------------------------------------------------------------------------
		Television.initialiseTVChannels (context,PublicData.projectFolder + getString (R.string.tv_channels_file));
		// -------------------------------------------------------------------------
		// 01/03/2018 ECU set up the receiver to try and detect when the device
		//                becomes disconnected
		// -------------------------------------------------------------------------
		context.registerReceiver (bluetoothReceiver,
                new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
		// --------------------------------------------------------------------------
	}	
	/* ============================================================================= */
    private BluetoothSocket CreateBluetoothSocket (BluetoothDevice device) throws IOException 
	{
    	// -------------------------------------------------------------------------
		try 
		{
			final Method  method = device.getClass().getMethod ("createInsecureRfcommSocketToServiceRecord", 
										new Class[] { UUID.class });
	        return (BluetoothSocket) method.invoke (device, LOCAL_UUID);
	    }
		catch (Exception theException)
		{
			Utilities.popToast (getString (R.string.could_not_create_insecure_rfcomm_connection) + " " + theException);
	    }
	    // -------------------------------------------------------------------------
	    return  device.createRfcommSocketToServiceRecord (LOCAL_UUID);
	    // -------------------------------------------------------------------------
	}
    /* ============================================================================= */
	boolean GetBluetoothConnection ()
	{
		// -------------------------------------------------------------------------
		// 01/01/2014 ECU confirm that no stream has been opened
		// 27/02/2018 ECU this method will return true if bluetooth is enabled, 
		//                irrespective of whether a connection is made, or false if
		//                the device does not support bluetooth or it is disabled
		// -------------------------------------------------------------------------
		outputStream = null;
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 01/01/2014 ECU try and get the default adapter but do not try and
			//                start it if it is not already enabled
			// ---------------------------------------------------------------------
			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter ();
			// ---------------------------------------------------------------------
			// 03/01/2014 ECU check if there is an adapter on this device
			// ---------------------------------------------------------------------
			if (bluetoothAdapter != null)
			{
				// -----------------------------------------------------------------
				// 12/10/2015 ECU check if bluetooth is enabled on this device
				// -----------------------------------------------------------------
				if (bluetoothAdapter.isEnabled ())
				{
					// -------------------------------------------------------------
					// 01/01/2014 ECU adapter has been found so try and connect to the
					//                remote device. An exception will be thrown if
					//                unable to connect
					// 19/04/2014 ECU change to use the MAC address stored on disk
					// 20/04/2014 ECU if the stored MAC address is null then use the
					//                default HC-06 MAC address
					// 12/05/2015 ECU try and catch illegal arguments
					// -------------------------------------------------------------
					try
					{
						bluetoothDevice = bluetoothAdapter.getRemoteDevice
								((PublicData.storedData.remoteMACAddress != null) ? PublicData.storedData.remoteMACAddress 
						                                               			  : MAC_ADDRESS);
					}
					catch (Exception theException)
					{
						// ---------------------------------------------------------
						// 12/05/2015 ECU if an exception occurs (like an invalid
						//                MAC address) then return with an error
						// ---------------------------------------------------------
						return false;
						// ---------------------------------------------------------	
					}
					// -------------------------------------------------------------
					// 01/01/2014 ECU now try and create the socket that will be used
					//                for communication to the remote device
					// -------------------------------------------------------------
					bluetoothSocket = CreateBluetoothSocket (bluetoothDevice);
					// -------------------------------------------------------------
					// 31/12/2013 ECU cancel any discovery that may be going on
					// -------------------------------------------------------------
					bluetoothAdapter.cancelDiscovery ();   
					// -------------------------------------------------------------
					// 27/02/2018 ECU because the bluetooth connection can take a long
					//                time with a fixed timeout (about 10 seconds) and
					//                it 'blocks' then use a thread to do the connection
					// -------------------------------------------------------------
					(new ConnectThread ()).start ();
					// -------------------------------------------------------------
					// 27/02/2018 ECU indicate that bluetooth is present irrespective
					//                of whether the 'connection' succeeds or fails
					// -------------------------------------------------------------
					return true;
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 12/10/2015 ECU the device supports bluetooth but it is not 
					//                enabled
					// -------------------------------------------------------------
					Utilities.LogToProjectFile (TAG,getString (R.string.remote_enable_bluetooth));
					// -------------------------------------------------------------
					// 26/02/2018 ECU Note - indicate that connection failed to the
					//                       remote bluetooth device
					// -------------------------------------------------------------
					return false;
					// -------------------------------------------------------------
				}
			}
			else
			{
				// -----------------------------------------------------------------
				// 03/01/2014 ECU there is no adapter on this device
				// -----------------------------------------------------------------
				return false;	
				// -----------------------------------------------------------------
			}
		}
		catch (IOException theException)
		{
			// ---------------------------------------------------------------------
			// 26/02/2018 ECU Note - an exception normally occurs when a 'timeout'
			//                       occurs because a remote bluetooth device cannot
			//                 		 be connected to
			// 27/02/2018 ECU if a 'timeout' occurs during the connection then this
			//                is picked up the the 'connection thread'
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler 
	{
		// -------------------------------------------------------------------------
		// 31/12/2013 ECU handle incoming messages - only one wanted
		// -------------------------------------------------------------------------
		@Override
	    public void handleMessage (Message message) 
		{	
			// ---------------------------------------------------------------------
			switch (message.what) 
			{
				// -----------------------------------------------------------------
	            case MESSAGE_REMOTE_CODE:
	            	// -------------------------------------------------------------
	            	// 01/01/2014 ECU send the data to the remote IR blaster
	            	// -------------------------------------------------------------
	            	SendDataToRemote ((String) message.obj);
	            	// -------------------------------------------------------------
	                break;
	            // -----------------------------------------------------------------    
	            case MESSAGE_REQUEST:
	            	// -------------------------------------------------------------
	            	// 01/01/2014 ECU send the data to the remote IR blaster
	            	// -------------------------------------------------------------            	
	            	SendDataToRemote ((String) message.obj);	            	
	            	// -------------------------------------------------------------
	            	// 01/01/2014 ECU send the required response
	            	// -------------------------------------------------------------
	            	Message newMessage = Message.obtain (null,MESSAGE_REQUEST,message.obj + " --- OK");
	            	try 
	            	{
	            		message.replyTo.send (newMessage);
	            	} 
	            	catch (RemoteException theException) 
	            	{
					}
	                break;
	            // -----------------------------------------------------------------
	            default:
	            	// -------------------------------------------------------------
	            	// 01/01/2014 ECU if a message that is not of interest then let 
	            	//                the normal code process it
	            	// -------------------------------------------------------------
	                super.handleMessage(message);
	                // -------------------------------------------------------------
	            // -----------------------------------------------------------------
	        }
		}
	}
	// =============================================================================
	void restartThisService (Context theContext)
	{
		// -------------------------------------------------------------------------
		Intent restartIntent = new Intent (theContext,BlueToothService.class);
		PendingIntent restartPendingIntent 
			= PendingIntent.getService (theContext,8337,restartIntent,PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) theContext.getSystemService (Context.ALARM_SERVICE);
		// -------------------------------------------------------------------------
		// 24/12/2015 ECU changed to use the new method
		// -------------------------------------------------------------------------
		Utilities.SetAnExactAlarm (alarmManager,System.currentTimeMillis() + StaticData.RESTART_TIME,restartPendingIntent);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public void SendDataToRemote (String theMessage) 
    {
		// -------------------------------------------------------------------------
    	// 17/12/2013 ECU need to add a terminator to the string
		// -------------------------------------------------------------------------   	
    	String localString = theMessage;
    	// -------------------------------------------------------------------------
    	try
    	{
    		if (outputStream != null)
    		{
    			outputStream.write (localString.getBytes());
    		}
    		else
    		{
    			// -----------------------------------------------------------------
    			// 01/01/2014 ECU there is no route to bluetooth
    			// 11/04/2014 ECU change to use resource
    			// -----------------------------------------------------------------
    			Utilities.popToast (getString (R.string.unable_to_send) + " " + localString);
    			// ----------------------------------------------------------------
    			// 01/03/2018 ECU tell the user that need to reestablish communication
    			//                with the remote controller
    			// -----------------------------------------------------------------
    			Utilities.popToastAndSpeak (getString (R.string.remote_device_reestablish),true);
    			// -----------------------------------------------------------------
    			// 28/02/2018 ECU stop and restart this service
    			// -----------------------------------------------------------------
    			stopThisService (context,true);
    			// -----------------------------------------------------------------
    		}
    	}
    	catch (IOException theException)
    	{
    		// ---------------------------------------------------------------------
    		// 27/02/2018 ECU tell the handler that an error has occurred
    		// ---------------------------------------------------------------------
    		timerHandler.sendEmptyMessage (StaticData.MESSAGE_BLUETOOTH_ERROR);
    		// ---------------------------------------------------------------------
    		Utilities.popToast (getString (R.string.send_data_to_remote) + " : " + theException);
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
	void stopThisService (Context theContext,boolean theRestartFlag)
	{
		// -------------------------------------------------------------------------
		// 28/02/2018 ECU created to stop this service and then to optionally restart
		//                it
		// -------------------------------------------------------------------------
		((Service) theContext).stopSelf();
		// -------------------------------------------------------------------------
		// 28/02/2018 ECU check if this service is to be restarted
		// -------------------------------------------------------------------------
		if (theRestartFlag)
		{
			// ---------------------------------------------------------------------
			// 28/02/2018 ECU restart this service after a short time
			// ---------------------------------------------------------------------
			restartThisService (theContext);
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	@SuppressLint("HandlerLeak")
	class TimerHandler extends Handler 
	{
		// -------------------------------------------------------------------------
		// 15/10/2015 ECU handle created to check timer message
		// -------------------------------------------------------------------------
		Message localMessage;
		// -------------------------------------------------------------------------
		@Override
	    public void handleMessage (Message theMessage) 
		{	
			switch (theMessage.what) 
			{
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_BLUETOOTH_CONNECT:
					// -------------------------------------------------------------
					// 27/02/2018 ECU want to establish a connection to the remote 
					//                device
					// -------------------------------------------------------------
					// 27/02/2018 ECU try and connect to the remote device
					// -------------------------------------------------------------
					if (!GetBluetoothConnection ())
					{
						// ---------------------------------------------------------
						// 27/02/2018 ECU the device either does not support bluetooth
						//                or, if it does, then it is not enabled - in
						//                this case stop this service
						// 03/03/2018 ECU stop the service directly rather than
						//                sending a TIMEOUT message as do not want
						//                an 'unavailable' message
						// ---------------------------------------------------------
						stopThisService (context,false);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_BLUETOOTH_CONNECTED:
					// -------------------------------------------------------------
					// 27/02/2018 ECU a connection has been established to the remote
					//                device
					// -------------------------------------------------------------
					// 27/02/2018 ECU perform any tasks that need to be done when
					//                a connection has been established
					// -------------------------------------------------------------
					connectionTasks ();
					// -------------------------------------------------------------
					// 27/12/2018 ECU tell the user that the remote controller is
					//                available
					// 01/03/2018 ECU changed because need to pass through 'arg1'
					// -------------------------------------------------------------
					localMessage = obtainMessage (StaticData.MESSAGE_BLUETOOTH_MESSAGE,
															R.string.remote_device_available,StaticData.NOT_SET);
					sendMessage (localMessage);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_BLUETOOTH_DISCONNECTED:	
					// -------------------------------------------------------------
					// 01/03/2018 ECU called when the listener indicates that the
					//                bluetooth device has been disconnected
					// -------------------------------------------------------------
					Utilities.SpeakAPhrase (context, getString (R.string.remote_device_disconnected));
					// -------------------------------------------------------------
					// 01/03/2018 ECU trigger an 'error' so that the user is aware
					//                of the situation
					// -------------------------------------------------------------
					this.sendEmptyMessage (StaticData.MESSAGE_BLUETOOTH_ERROR);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_BLUETOOTH_ERROR:
					// -------------------------------------------------------------
					// 27/02/2018 ECU an error occurred when trying to communicate
					//                with the remote device
					// 28/02/2018 ECU Note - usually an error will occur if the
					//                       remote device has been switched off
					//                       in which case a 'broken pipe' exception
					//                       will occur - to recover just want to
					//                       re-establish the connection.
					// -------------------------------------------------------------
					Utilities.SpeakAPhrase (context, getString (R.string.remote_device_retry));
					// -------------------------------------------------------------
					// 28/02/2018 ECU put in a delay before trying to connect
					// -------------------------------------------------------------
					this.sendEmptyMessageDelayed (StaticData.MESSAGE_CHECK_DEVICE,StaticData.MILLISECONDS_PER_MINUTE);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------  
				case StaticData.MESSAGE_BLUETOOTH_MESSAGE:
					// -------------------------------------------------------------
					// 27/02/2018 ECU called to speak a message if the user interface
					//                is up and running
					// 03/03/2018 ECU only process the message if the app has been
					//                'started manually'
					// -------------------------------------------------------------
					if (PublicData.startedManually)
					{
						// ---------------------------------------------------------
						// 03/03/2018 ECU Note - wait until the user interface is
						//                       up and running
						// ---------------------------------------------------------
						if (PublicData.userInterfaceRunning)
						{
							// -----------------------------------------------------
							// 27/02/2018 ECU tell the user that the remote device is
							//				  available
							// 28/02/2018 ECU change to use Utilities... rather than
							//                TextToSpeechService....
							// 01/03/2018 ECU the 'arg1' contains the 'R.string...' 
							//                to be spoken
							// -----------------------------------------------------
							Utilities.SpeakAPhrase (context, getString (theMessage.arg1));
							// -----------------------------------------------------
						}
						else
						{
							// -----------------------------------------------------
							// 27/02/2018 ECU the TTS service is not ready yet so 
							//                just wait
							// 01/03/2018 ECU changed to send the message rather than
							//                just the type because the argument is 
							//                needed
							// -----------------------------------------------------
							localMessage = this.obtainMessage (theMessage.what,theMessage.arg1,theMessage.arg2);
							this.sendMessageDelayed (localMessage,StaticData.ONE_SECOND);
							// -----------------------------------------------------
						}
					}
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_BLUETOOTH_TIMEOUT:
					// -------------------------------------------------------------
					// 27/02/2018 ECU a timeout occurred when trying to establish a
					//                connection to the remote device
					// -------------------------------------------------------------
					// 01/03/2018 ECU indicate that a problem has occurred and that
					//                the remote controller is unavailable
					// -------------------------------------------------------------
					localMessage = this.obtainMessage (StaticData.MESSAGE_BLUETOOTH_MESSAGE, 
															R.string.remote_device_unavailable, StaticData.NOT_SET);
					sendMessage (localMessage);
					// -------------------------------------------------------------
					// 27/02/2018 ECU want to stop this service and indicate this
					//                fact
					// 28/02/2018 ECU changed to use the method rather than 'stopSelf'
					//                here
					// -------------------------------------------------------------
					stopThisService (context,false);
					// -------------------------------------------------------------
					break;	
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_CHECK_DEVICE:
					// -------------------------------------------------------------
					// 28/02/2018 ECU called to try and connect again to the remote
					//                controller
					// -------------------------------------------------------------
					// 27/02/2018 ECU want to stop this service and indicate this
					//                fact
					// 28/02/2018 ECU changed to use the method rather than 'stopSelf'
					//                here - the 'true' indicates that the service is
					//                to be restarted
					// -------------------------------------------------------------
					stopThisService (context,true);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_DATA:
					// -------------------------------------------------------------
					// 15/10/2015 ECU transmit the string that is held within the
					//                message
					// -------------------------------------------------------------
					SendDataToRemote ((String) theMessage.obj);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
	        }
		}
		// =========================================================================
	}
	// =============================================================================
}

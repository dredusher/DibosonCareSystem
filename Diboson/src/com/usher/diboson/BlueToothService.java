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
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

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
	Messenger			messenger			= null;
	OutputStream		outputStream		= null;
	public static TimerHandler		
						timerHandler		= null;			// 15/10/2015 ECU added
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
			return messenger.getBinder();
		// -------------------------------------------------------------------------
		// 15/02/2014 ECU indicate not working - hope this is correct
		// -------------------------------------------------------------------------
		return null;
	}
	/* ============================================================================= */
	@Override
	public void onCreate()
	{
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
		if (!GetBluetoothConnection ())
		{
			// ---------------------------------------------------------------------
			// 01/01/2014 ECU there is no connection so want to terminate this service
			// 15/02/2014 ECU added the attempt to stop the service itself
			// ---------------------------------------------------------------------
			this.stopSelf();
			// ---------------------------------------------------------------------
			// 02/03/2014 ECU indicate that service failed to start
			// ---------------------------------------------------------------------
			PublicData.blueToothService = false;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 31/12/2013 ECU set up the message handler
			// 16/03/2014 ECU Create a new Messenger pointing to the given Handler. 
			//                Any Message objects sent through this Messenger will 
			//                appear in the Handler as if Handler.sendMessage(Message)
			//                had been called directly.
			// ---------------------------------------------------------------------
			messenger = new Messenger (new IncomingHandler());
			// ---------------------------------------------------------------------
			// 02/03/2014 ECU indicate that service seems to have stated OK
			// ---------------------------------------------------------------------
			PublicData.blueToothService = true;
			// ---------------------------------------------------------------------
			// 15/10/2015 ECU start up the timer handler
			// ---------------------------------------------------------------------
			timerHandler = new TimerHandler ();
			timerHandler.sleep (60 * 1000);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 26/02/2016 ECU because this service may take some time to come up then
		//                make sure that the local device details has the up to date
		//                information
		// -------------------------------------------------------------------------
		PublicData.localDeviceDetails.remoteController = PublicData.blueToothService;
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
    			outputStream.close();
    		// ---------------------------------------------------------------------
    		// 31/12/2013 ECU close the socket and hence the connection
    		// ---------------------------------------------------------------------
    		if (bluetoothSocket != null)
    			bluetoothSocket.close();
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
	/* ============================================================================= */
    private BluetoothSocket CreateBluetoothSocket(BluetoothDevice device) throws IOException 
	{
    	// -------------------------------------------------------------------------
		try 
		{
			final Method  method = device.getClass().getMethod ("createInsecureRfcommSocketToServiceRecord", 
										new Class[] { UUID.class });
	        return (BluetoothSocket) method.invoke(device, LOCAL_UUID);
	    }
		catch (Exception theException)
		{
			Utilities.popToast (getString(R.string.could_not_create_insecure_rfcomm_connection) + " " + theException);
	    }
	    // -------------------------------------------------------------------------
	    return  device.createRfcommSocketToServiceRecord(LOCAL_UUID);
	    // -------------------------------------------------------------------------
	}
    /* ============================================================================= */
	boolean GetBluetoothConnection ()
	{
		// -------------------------------------------------------------------------
		// 01/01/2014 ECU confirm that no stream has been opened
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
				if (bluetoothAdapter.isEnabled())
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
					bluetoothAdapter.cancelDiscovery();
					// -------------------------------------------------------------
					// 31/12/2013 ECU now try and connect to the bluetooth device
					// -------------------------------------------------------------
					bluetoothSocket.connect();
					// -------------------------------------------------------------
					// 31/12/2013 ECU get an output stream for data
					// -------------------------------------------------------------
					outputStream = bluetoothSocket.getOutputStream();	
					// -------------------------------------------------------------
					return true;
				}
				else
				{
					// -------------------------------------------------------------
					// 12/10/2015 ECU the device supports bluetooth but it is not 
					//                enabled
					// -------------------------------------------------------------
					Utilities.LogToProjectFile (TAG,getString (R.string.remote_enable_bluetooth));
					// -------------------------------------------------------------
					return false;
				}
			}
			else
			{
				// -----------------------------------------------------------------
				// 03/01/2014 ECU there is no adapter on this device
				// -----------------------------------------------------------------
				return false;				
			}
		}
		catch (IOException theException)
		{
			// ---------------------------------------------------------------------
			return false;
			// ----------------------------------------------------------------------
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
	            	SendDataToRemote ((String)message.obj);	            	
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
    			Utilities.popToast (getString(R.string.unable_to_send) + " " + localString);
    			// -----------------------------------------------------------------
    		}
    	}
    	catch (IOException theException)
    	{
    		// ---------------------------------------------------------------------
    		Utilities.popToast (getString(R.string.send_data_to_remote) + " : " + theException);
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    }
	/* ============================================================================ */
	@SuppressLint("HandlerLeak")
	class TimerHandler extends Handler 
	{
		// -------------------------------------------------------------------------
		// 15/10/2015 ECU handle created to check timer message
		// -------------------------------------------------------------------------
		@Override
	    public void handleMessage (Message theMessage) 
		{	
			switch (theMessage.what) 
			{
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
		public void sleep (long delayMillis)
		{	
			// -------------------------------------------------------------------------
			// 05/05/2015 ECU changed to use MESSAGE_SLEEP instead of 0
			// -------------------------------------------------------------------------
			this.removeMessages (StaticData.MESSAGE_SLEEP);
			sendMessageDelayed (obtainMessage (StaticData.MESSAGE_SLEEP), delayMillis);
		}
	}
	// ============================================================================
}

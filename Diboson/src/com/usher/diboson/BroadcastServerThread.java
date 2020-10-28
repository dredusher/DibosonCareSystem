package com.usher.diboson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Handler;
import android.os.Message;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

// =================================================================================
public class BroadcastServerThread implements Runnable
{
	/* ============================================================================= */
	// 26/08/2013 ECU this thread created to handle incoming broadcast messages
	// 09/04/2016 ECU previously the comments related to 'multicast' which is not
	//                what is being used here. So have changed everything to be
	//                about 'broadcast' messages which are being used.
	// 25/05/2020 ECU rewrote the transmission side so as to use a handler rather
	//                than looping and checking is 'PublicData.broadcastMessage'
	//                is not null which indicates that a message was to be sent
	/* ============================================================================= */
	private final String TAG = "BroadcastServerThread";
	/* ============================================================================= */
					InetAddress		broadcastAddress = null;		// 27/08/2013 ECU added
	private static	int				broadcastCounter = 0;			// 23/03/2015 ECU counter included
																	//                in each message
	public  static  BroadcastRefreshHandler
									broadcastRefreshHandler;		// 25/05/2020 ECU make public
					String			broadcastMessage;				// 25/05/2020 ECU added
					boolean			keepRunning = true;				// 22/08/2013 ECU change to public
	private static 	Context			context;
					String 			incomingAddress = null;			// 20/03/2015 ECU added
					String 			incomingMessage = null;
					byte[] 			inputBuffer = new byte [1024];	// 08/04/2016 ECU moved here from thread
					MulticastLock 	multicastLock = null;
					DatagramPacket 	packet;							// 20/03/2015 ECU moved here
					int 			port;
					WifiManager		wifiManager;
	/* ============================================================================= */
	public BroadcastServerThread (Context theContext,int thePort) 
	{
		// -------------------------------------------------------------------------
		// 27/08/2013 ECU store the incoming arguments
		// -------------------------------------------------------------------------
		context = theContext;
		port    = thePort;
		// -------------------------------------------------------------------------
		// 15/02/2015 ECU allow thread to use network on main UI thread
		// -------------------------------------------------------------------------
		APIIssues.NetworkOnMainUIThread (android.os.Build.VERSION.SDK_INT);
		// -------------------------------------------------------------------------
		wifiManager = (WifiManager) context.getSystemService (Context.WIFI_SERVICE);
		// -------------------------------------------------------------------------
		// 12/09/2013 ECU include debug mode check
		// 10/03/2015 ECU changed to use 'debugMode in 'storedData'
		// -------------------------------------------------------------------------
		if (PublicData.storedData.debugMode)
		{
			// ---------------------------------------------------------------------
			// 08/11/2013 ECU use the custom toast
			// 24/02/2017 ECU changed to use resouce
			// ---------------------------------------------------------------------
			Utilities.popToast (context.getString (R.string.broadcast_thread_created));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 25/05/2020 ECU Note - get the socket that will be used for 'broadcast'
		//                       messages
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 09/04/2016 ECU changed the name from 'multicastSocket'
			//            ECU Note - the setBroadcast is required to enable this device
			//                       to send broadcast messages
			// ---------------------------------------------------------------------
			PublicData.datagramSocket = new DatagramSocket (port);
			PublicData.datagramSocket.setBroadcast (true);
			// ---------------------------------------------------------------------
			// 22/08/2013 ECU set the timeout to indicate how long to wait for
			//                an incoming message
			// 01/09/2015 ECU changed to use StaticData
			// 09/04/2016 ECU changed from MULTICAST_TIMEOUT
			// ---------------------------------------------------------------------
			PublicData.datagramSocket.setSoTimeout (StaticData.BROADCAST_TIMEOUT);
			// ---------------------------------------------------------------------
			// 27/08/2013 ECU set up the broadcast address
			// 16/02/2015 ECU changed as method moved to MulticastUtilities
			// 09/04/2016 ECU changed the name from Multicast
			// ---------------------------------------------------------------------
			broadcastAddress = BroadcastUtilities.getBroadcastAddress (theContext);
			// ---------------------------------------------------------------------
			// 27/08/2013 ECU set up the refresh handler which will be used to
			//                transmit any message that is waiting
			// ---------------------------------------------------------------------
		    broadcastRefreshHandler = new BroadcastRefreshHandler ();
		    // ---------------------------------------------------------------------
		    // 25/05/2020 ECU announce this device to the world - after a delay
		    // ---------------------------------------------------------------------
		    Devices.sendHelloMessage (10 * StaticData.ONE_SECOND);
		    // ---------------------------------------------------------------------
		    // 08/04/2016 ECU allow the thread to receive broadcast messages
		    // ---------------------------------------------------------------------
			// 22/08/2013 ECU put in the following locking code because 
			//                without it the HTC phone was not receiving 
			//                broadcast messages
			// ---------------------------------------------------------------------	
			multicastLock = wifiManager.createMulticastLock (TAG);
			// ---------------------------------------------------------------------	
			// 22/08/2013 ECU acquire the lock
			// ---------------------------------------------------------------------	
			multicastLock.acquire ();
		    // ---------------------------------------------------------------------
		}
		catch (IOException theException)
		{
			// ---------------------------------------------------------------------
			// 11/04/2015 ECU if an exception occurred then really cannot do anything
			// ---------------------------------------------------------------------
			PublicData.datagramSocket = null;
			// ---------------------------------------------------------------------
		}	
	}
	/* ============================================================================= */
	// 25/05/2020 ECU Note - set up the bit of the thread that is responsible for
	//                ====   receiving incoming broadcast messages
	// -----------------------------------------------------------------------------
	public void run ()
	{
		// -------------------------------------------------------------------------
		try
		{  
			// ---------------------------------------------------------------------
			// 12/04/2015 ECU put in the check on 'null' - just in case
			// 09/04/2016 ECU changed the name from 'multicastSocket'
			// ---------------------------------------------------------------------
			if (PublicData.datagramSocket != null)
			{
				// ------------------------------------------------------------------
				// 08/04/2016 ECU Note - keep looping to receive the incoming
				//                       broadcast messages
				// ------------------------------------------------------------------
				while (keepRunning) 
				{	
					// -------------------------------------------------------------
					// 08/04/2016 ECU Note - declare declare the packet that will receive
					//                       the incoming data.
					// -------------------------------------------------------------
					packet = new DatagramPacket (inputBuffer,inputBuffer.length);
					// -------------------------------------------------------------
					// 24/02/2017 ECU Note - the following 'receive' blocks until the
					//                       datagram is received which probably explains
					//                       why a 'socket closed' exception is received
					//                       when the app is closed even though 
					//                       'keepRunning' is set to false before
					//                       the socket is actually closed. Could
					//                       probably set a 'time out' but not a
					//                       major issue
					// -------------------------------------------------------------
					PublicData.datagramSocket.receive (packet);
					// -------------------------------------------------------------
					// 20/03/2015 ECU get the address of the sender
					// -------------------------------------------------------------
					incomingAddress = packet.getAddress().getHostAddress();
					// -------------------------------------------------------------	
					// 22/08/2013 ECU generate a string from the received data
					// -------------------------------------------------------------	
					incomingMessage = new String (inputBuffer,0,packet.getLength());
					// -------------------------------------------------------------
					// 24/03/2015 ECU create a thread to handle the received packet
					// 09/04/2016 ECU Note - it is unlikely that there will ever be
					//                       more than one of these threads running
					// -------------------------------------------------------------
					Thread receiverThread = new Thread ()
					{
						public void run ()
						{
							// -----------------------------------------------------
							// 24/03/2015 ECU process the incoming packer
							// -----------------------------------------------------
							processIncomingMessage (incomingAddress,incomingMessage);
							// -----------------------------------------------------
						}
					};
					// -------------------------------------------------------------
					// 01/07/2013 ECU start up the thread to handle the incoming message
					// -------------------------------------------------------------
					receiverThread.start ();  	   
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 08/04/2016 ECU Note - the loop within the thread has finished
				// -----------------------------------------------------------------			
				// 22/08/2013 ECU release the multicast lock
				// -----------------------------------------------------------------
				multicastLock.release();
				// -----------------------------------------------------------------
			}
		}
		catch (IOException theException)   
		{
			// ---------------------------------------------------------------------
			// 26/08/2013 ECU will get an exception if there is a timeout when trying to get an
			//                incoming broadcast datagram
			// 27/08/2013 ECU when the socket is closed by the refresh handler then this will
			//                generate this exception - the only thing to do is release the
			//				  lock
			// ---------------------------------------------------------------------
			multicastLock.release();	
			// ---------------------------------------------------------------------
			// 24/03/2015 ECU log the fact that exception has occurred
			// 28/04/2019 ECU when the thread is told to 'stop running' then a
			//                'socket closed' exception will occur - this is not really
			//                an error
			// ---------------------------------------------------------------------
			if (keepRunning)
			{
				// -----------------------------------------------------------------
				// 28/04/2019 ECU this is a genuine exception which needs to be reported
				// -----------------------------------------------------------------
				Utilities.LogToProjectFile (TAG,"received broadcast packet exception " + theException);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
	    }
	}
	/* ============================================================================= */	
	@SuppressLint("HandlerLeak")
	class BroadcastRefreshHandler extends Handler
	{
		// -------------------------------------------------------------------------
		// 25/05/2020 ECU tided up so that rather than looping for a message to be
		//                sent then the 'sender' sends a message directly for
		//                processing
		// -------------------------------------------------------------------------
		@Override
	    public void handleMessage (Message theMessage) 
	    {
	    	// ---------------------------------------------------------------------
	    	// 25/05/2020 ECU changed to check on the message type rather than just
	    	//                looping
	    	// ---------------------------------------------------------------------
	    	switch (theMessage.what)
			{
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_FINISH:
					// -------------------------------------------------------------
					// 25/05/2020 ECU called when processing is to be finished
					// -------------------------------------------------------------
					// 27/08/2013 ECU perform 'closing down' tasks
					// -------------------------------------------------------------
					PublicData.datagramSocket.close ();
					// -------------------------------------------------------------
					// 22/09/2013 ECU just indicate that the socket has been closed
					// -------------------------------------------------------------
					PublicData.datagramSocket = null;
					// -------------------------------------------------------------
					// 25/05/2020 ECU and indicate that the main thread is to stop
					// -------------------------------------------------------------
					keepRunning = false;
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_SEND:
					// -------------------------------------------------------------
					// 25/05/2020 ECU called when there is a message to send
					// -------------------------------------------------------------
					sendTheMessage ((String)theMessage.obj);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				default:
					break;
				// -----------------------------------------------------------------
			}
			//----------------------------------------------------------------------
	    }
	    // -------------------------------------------------------------------------
	};
	// =============================================================================
	void processIncomingMessage (String theIncomingAddress,String theIncomingMessage)
	{
		// -----------------------------------------------------------------
		// 22/03/2015 ECU log the fact that message is being processed
		// -----------------------------------------------------------------
		Utilities.LogToProjectFile (TAG,"processing broadcast packet '" + theIncomingMessage +
				"' from " + theIncomingAddress);
		// -------------------------------------------------------------------------
		// 24/03/2015 ECU created to have a self contained method to include the
		//                processing so that it can be called from a thread
		// 09/04/2016 ECU changed from MULTICAST_MESS....
		//            ECU Note - the 'cry' and 'laugh' options are just there for a
		//                       bit of fun.
		// -------------------------------------------------------------------------
		if (theIncomingMessage.startsWith (StaticData.BROADCAST_MESSAGE_CRY))
		{
			Utilities.PlayAFile(context, PublicData.projectFolder + "cry.wav");
		}
		else
		if (theIncomingMessage.startsWith (StaticData.BROADCAST_MESSAGE_LAUGH))
		{
			Utilities.PlayAFile(context, PublicData.projectFolder + "laugh.wav");
		}
		else
		// -------------------------------------------------------------------------
		// 20/03/2015 ECU check if a device has announced its presence on
		//				  the network
		// 09/04/2016 ECU changed from MULTICAST_MESSAGE_HELLO
		// -------------------------------------------------------------------------
		if (theIncomingMessage.startsWith (StaticData.BROADCAST_MESSAGE_HELLO))
		{
			// ---------------------------------------------------------------------
			// 23/03/2015 ECU this device will 'see' its own broadcast
			//                message - for this packet type these
			//                should be ignored
			// ---------------------------------------------------------------------
			if (!theIncomingAddress.equalsIgnoreCase(PublicData.ipAddress))
			{	
				// -----------------------------------------------------------------
				// 22/03/2015 ECU change to set a flag to delay the 
				//                request for information
				// 09/04/2016 ECU Note - set the flag which will indicate that this
				//                       device will send a normal message to the
				//                       sender of this packet indicating that it
				//                       should send, to this device, its 'device 
				//                       details'
				// -----------------------------------------------------------------
				PublicData.requestAddress = theIncomingAddress;
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void sendTheMessage (String theMessage)
	{
		// -------------------------------------------------------------------------
		// 25/05/2020 ECU create to send theMessage as a broadcast message
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 23/03/2015 ECU add in the counter to the message and then increment
			//                the counter
			// ---------------------------------------------------------------------
			broadcastMessage = theMessage + StaticData.SPACE_STRING + broadcastCounter++;
			// ---------------------------------------------------------------------
			// 22/03/2015 ECU log the fact that message has been sent
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG,"sent broadcast packet " + broadcastMessage);
			// ---------------------------------------------------------------------
			// 27/08/2013 ECU there is a message to be sent
			// 09/04/2016 ECU Note - create a packet and store the data to be
			//                       broadcast.
			// ---------------------------------------------------------------------
			DatagramPacket packet = new DatagramPacket (broadcastMessage.getBytes(),
														broadcastMessage.length(),
														broadcastAddress,
														port);
			PublicData.datagramSocket.send (packet);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 14/09/2020 ECU changed fro IOException to Exception because in an
		//                abnormal start up then it is possible that '..datagramSocket'
		//                is null
		// -------------------------------------------------------------------------
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 25/05/2020 ECU log the fact that exception has occurred
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG,"sendTheMessage exception" + theException);
			// ---------------------------------------------------------------------
		}
		// =========================================================================
	}
}

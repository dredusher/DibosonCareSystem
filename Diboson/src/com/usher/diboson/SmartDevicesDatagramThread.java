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
import java.net.SocketTimeoutException;
import java.util.Arrays;

// =============================================================================
public class SmartDevicesDatagramThread implements Runnable
{
	/* ========================================================================= */
	// 26/08/2013 ECU this thread created to handle incoming broadcast messages
	// 09/04/2016 ECU previously the comments related to 'multicast' which is not
	//                what is being used here. So have changed everything to be
	//                about 'broadcast' messages which are being used.
	/* ========================================================================= */
	private final String TAG = "SmartDevicesBroadcastServerThread";
	/* ========================================================================= */
			DatagramPacket 	broadcastPacket = null;			// 20/04/2019 ECU added
			int				broadcastPacketRepeat = StaticData.NOT_SET;
															// 30/04/2019 ECU added
	public	BroadcastRefreshHandler 		
							broadcastRefreshHandler;
			boolean			keepRunning = true;				// 22/08/2013 ECU change to public
			DatagramSocket	datagramSocket;
			byte[] 			inputBuffer = new byte [1024];	// 08/04/2016 ECU moved here from thread
			MulticastLock 	multicastLock = null;
			String			name;							// 20/04/2019 ECU identifier for the thread
			DatagramPacket 	packet;							// 20/03/2015 ECU moved here
			WifiManager		wifiManager;
	/* ============================================================================= */
	public SmartDevicesDatagramThread (Context theContext,String theName,int thePort,int theTimeOut) 
	{
		// -------------------------------------------------------------------------
		// 20/04/2019 ECU remember the identifier of this thread
		// 02/05/2019 ECU added the time out (in milliseconds) as an argument
		// -------------------------------------------------------------------------
		name = theName;
		// -------------------------------------------------------------------------
		// 15/02/2015 ECU allow thread to use network on main UI thread
		// 20/04/2019 ECU added the name to identify the thread
		// -------------------------------------------------------------------------
		APIIssues.NetworkOnMainUIThread (android.os.Build.VERSION.SDK_INT);
		// -------------------------------------------------------------------------
		wifiManager = (WifiManager) theContext.getSystemService (Context.WIFI_SERVICE);
		// -------------------------------------------------------------------------
		// 12/09/2013 ECU include debug mode check
		// 10/03/2015 ECU changed to use 'debugMode in 'storedData'
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 09/04/2016 ECU changed the name from 'multicastSocket'
			//            ECU Note - the setBroadcast is required to enable this device
			//                       to send broadcast messages
			// ---------------------------------------------------------------------
			datagramSocket = new DatagramSocket (thePort);
			datagramSocket.setBroadcast (true);
			// ---------------------------------------------------------------------
			// 22/08/2013 ECU set the timeout to indicate how long to wait for
			//                an incoming message
			// 01/09/2015 ECU changed to use StaticData
			// 09/04/2016 ECU changed from MULTICAST_TIMEOUT
			// 02/05/2019 ECU changed to use the supplied time out (in milliseconds)
			// ---------------------------------------------------------------------
			datagramSocket.setSoTimeout (theTimeOut);
			// ---------------------------------------------------------------------
			// 27/08/2013 ECU set up the refresh handler which will be used to
			//                transmit any message that is waiting
			// 30/04/2019 ECU changed to use the empty message rather than sleep
			// ---------------------------------------------------------------------
			broadcastRefreshHandler = new BroadcastRefreshHandler ();		
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
			Utilities.LogToProjectFile(TAG,"Exception : " + theException);
			// ---------------------------------------------------------------------
			// 11/04/2015 ECU if an exception occurred then really cannot do anything
			// ---------------------------------------------------------------------
			datagramSocket = null;
			// ---------------------------------------------------------------------
		}	
	}
	/* ============================================================================= */
	public void run () 
	{
		// -------------------------------------------------------------------------
		// 12/04/2015 ECU put in the check on 'null' - just in case
		// 09/04/2016 ECU changed the name from 'multicastSocket'
		// -------------------------------------------------------------------------
		if (datagramSocket != null)
		{
				// -----------------------------------------------------------------
				// 08/04/2016 ECU Note - declare the packet that will receive
				//                       the incoming data.
				// 20/04/2019 ECU moved here from within the 'keepRunning' loop
				// -----------------------------------------------------------------
				packet = new DatagramPacket (inputBuffer,inputBuffer.length);
				// ------------------------------------------------------------------
				// 08/04/2016 ECU Note - keep looping to receive the incoming
				//                       broadcast messages
				// ------------------------------------------------------------------
				while (keepRunning) 
				{	
					// -------------------------------------------------------------
					try
					{
						// ---------------------------------------------------------
						// 24/02/2017 ECU Note - the following 'receive' blocks until the
						//                       datagram is received which probably explains
						//                       why a 'socket closed' exception is received
						//                       when the app is closed even though 
						//                       'keepRunning' is set to false before
						//                       the socket is actually closed. Could
						//                       probably set a 'time out' but not a
						//                       major issue
						// ---------------------------------------------------------
						datagramSocket.receive (packet);
						// ---------------------------------------------------------
						// 24/03/2015 ECU create a thread to handle the received packet
						// 09/04/2016 ECU Note - it is unlikely that there will ever be
						//                       more than one of these threads running
						// ---------------------------------------------------------
						// 24/03/2015 ECU process the incoming packet
						// ---------------------------------------------------------
						processIncomingMessage (packet.getPort(),
												packet.getAddress().getHostAddress(),
												Arrays.copyOfRange (inputBuffer, 0, packet.getLength()));
						// ---------------------------------------------------------
					}
					catch (SocketTimeoutException theException)
					{
						// ---------------------------------------------------------
						// 02/05/2019 ECU the time out exception is not really an
						//                error that needs handling - just loop and
						//                try again for a packet
						// ---------------------------------------------------------
						// ---------------------------------------------------------
					}
					catch (IOException theException)
					{
						// ---------------------------------------------------------
						// 02/05/2019 ECU a general IO exception should cause the
						//                thread to terminate
						// ---------------------------------------------------------
						if (keepRunning)
						{
							Utilities.LogToProjectFile (TAG,name + "IO Exception : "+ theException);
							// -----------------------------------------------------
							// 02/05/2019 ECU indicate that the thread is to stop 
							//                running
							// -----------------------------------------------------
							keepRunning = false;
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------				
				}
				// -----------------------------------------------------------------
				// 22/08/2013 ECU release the multicast lock
				// -----------------------------------------------------------------
				multicastLock.release();
				// -----------------------------------------------------------------
			}
		// ------------------------------------------------------------------------
		// 08/04/2016 ECU Note - the loop within the thread has finished
		// 01/05/2019 ECU moved here from above
		// ------------------------------------------------------------------------
		Utilities.LogToProjectFile (TAG, name + " finished");
		// ------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@SuppressLint("HandlerLeak")
	class BroadcastRefreshHandler extends Handler
	{
		@Override
	    public void handleMessage (Message theMessage) 
	    {   		
			// ---------------------------------------------------------------------
			// 30/04/2019 ECU switch depending on the message type
			// ---------------------------------------------------------------------
			switch (theMessage.what)
			{
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_SEND:
					// -------------------------------------------------------------
					// 11/04/2015 ECU transmit the specified packet
					// -------------------------------------------------------------
					try
					{
						// ---------------------------------------------------------
						Utilities.LogToProjectFile (TAG,name + " Packet being Sent : " + broadcastPacket.getLength());
						// ---------------------------------------------------------
						// 19/04/2019 ECU a predefined packet is to be sent
						// ---------------------------------------------------------
						datagramSocket.send (broadcastPacket);
						// ---------------------------------------------------------
						// 30/04/2019 ECU now decide whether a repeat transmission
						//                is required
						// ---------------------------------------------------------
						if (broadcastPacketRepeat != StaticData.NOT_SET)
						{
							// -----------------------------------------------------
							// 30/04/2019 ECU set up a delayed message
							// -----------------------------------------------------
							this.sendEmptyMessageDelayed (StaticData.MESSAGE_SEND,broadcastPacketRepeat);
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
					}
					catch (IOException theException)
					{
						// ---------------------------------------------------------
						// 24/03/2015 ECU log the fact that exception has occurred
						// ---------------------------------------------------------
						Utilities.LogToProjectFile (TAG,name +" checkIfMessageToSend exception" + theException);
						// ---------------------------------------------------------
					}
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_FINISH:
					// -------------------------------------------------------------
					// 30/04/2019 ECU called when the thread is to close
					// -------------------------------------------------------------
					// 30/04/2019 ECU remove any queued 'transmission' messages
					// -------------------------------------------------------------
					removeMessages (StaticData.MESSAGE_SEND);
					// -------------------------------------------------------------
					// 30/04/2019 ECU tell the main thread to stop
					// -------------------------------------------------------------
					keepRunning = false;
					// -------------------------------------------------------------
					// 01/05/2019 ECU because the thread is blocked on a 'receive'
					//                then try and force an exception
					// -------------------------------------------------------------
					datagramSocket.close ();
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
			}
	    }
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	void processIncomingMessage (int thePort,String theIncomingAddress,byte [] theIncomingBytes)
	{
		// -------------------------------------------------------------------------
		// 19/04/2019 ECU decode and action the incoming packet
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 19/04/2019 ECU probably coming in from a smart device
			// 20/04/2019 ECU pass through the name
			// ---------------------------------------------------------------------
			(new SmartDevices()).processIncomingPacket (thePort,name,theIncomingAddress,theIncomingBytes);
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			Utilities.LogToProjectFile (TAG,name + " Process Exception : "+ theException);
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

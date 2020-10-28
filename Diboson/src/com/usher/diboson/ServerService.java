package com.usher.diboson;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import java.io.IOException;
import java.net.ServerSocket;

public class ServerService extends Service
{
	/* ============================================================================= */
	// 08/02/2914 ECU changed to try and run in the foreground to try and fix problems
	//                when the device goes into standby mode
	//            ECU make the above changes optional
	// 12/01/2016 ECU added the message handler facility
	// 26/05/2020 ECU tidied up to use resources rather than literal strings
	// =============================================================================
	final static String TAG = "ServiceService";
	// =============================================================================
	BroadcastServerThread 	broadcastServerThread;			// 27/08/2013 ECU added
	boolean 				keepRunning = true;
	boolean 				scanStarted = false;			// 31/07/2013 ECU added
	ServerSocket 			serverSocket = null;			// 22/07/2013 ECU added here
	ServerSocket 			serverSocketForData = null;		// 31/07/2013 ECU added
	ServerRefreshHandler	serverRefreshHandler;
	ServerThread 			serverThread;					// 22/08/2013 ECU added
	ServerThreadForData 	serverThreadForData;			// 22/08/2013 ECU added	
	/* ============================================================================= */
	
	// =============================================================================
		   static Context			context;				// 12/01/2016 ECU added
	public static MessageHandler	messageHandler;			// 12/01/2016 ECU		
	// =============================================================================
	
	@Override
	public IBinder onBind(Intent arg0) 
	{		
		// -------------------------------------------------------------------------
		return null;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public void onCreate()
	{ 
		// -------------------------------------------------------------------------
		super.onCreate();
		// -------------------------------------------------------------------------
		// 12/09/2013 ECU include debug mode check
		// 10/03/2015 ECU changed to use 'debugMode in 'storedData'
		// -------------------------------------------------------------------------
		if (PublicData.storedData.debugMode)
		{
			// ---------------------------------------------------------------------
			// 08/11/2013 ECU use the custom toast
			// ---------------------------------------------------------------------
			Utilities.popToast (getString (R.string.server_service_created), Toast.LENGTH_SHORT);
			// ---------------------------------------------------------------------
		}	
		// -------------------------------------------------------------------------
		// 12/01/2016 ECU create the handler for the message
		// -------------------------------------------------------------------------
		messageHandler = new MessageHandler ();
		// -------------------------------------------------------------------------
		// 12/01/2016 ECU save the context to be used in message handler
		// -------------------------------------------------------------------------
		context = this;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public int onStartCommand (Intent intent, int flags, int startId) 
	{
		// -------------------------------------------------------------------------
		// 08/04/2014 ECU altered from 'onStart'
		// -------------------------------------------------------------------------
		// 12/09/2013 ECU include debug mode check
		// 10/03/2015 ECU changed to use 'debugMode in 'storedData'
		// -------------------------------------------------------------------------
		if (PublicData.storedData.debugMode)
		{
			// ---------------------------------------------------------------------
			// 08/11/2013 ECU use the custom toast
			// 26/05/2020 ECU changed to use resource
			// ---------------------------------------------------------------------
			Utilities.popToast (getString (R.string.server_service_started), Toast.LENGTH_SHORT);
		}
		// -------------------------------------------------------------------------
		// 25/07/2013 ECU get the socket that will be used for TCP communication
		// -------------------------------------------------------------------------
		PublicData.socketNumber = this.getResources().getInteger (R.integer.TCP_port_number);
		// -------------------------------------------------------------------------
		// 30/06/2013 ECU start up the TCP server listening on specified port
		// -------------------------------------------------------------------------
		serverActions (PublicData.socketNumber);
		// -------------------------------------------------------------------------
		// 31/07/2013 ECU start up the TCP server that will handle data
		// -------------------------------------------------------------------------
		serverActionsForData (PublicData.socketNumberForData);
		// -------------------------------------------------------------------------
		// 26/08/2013 ECU start up the TCP server that will handle multicast messages
		// 01/09/2015 ECU changed to use StaticData
		// 09/04/2016 ECU changed from using the term multicast to broadcast as the
		//                former was misleading
		// -------------------------------------------------------------------------
		serverActionsForBroadcastMessages (StaticData.BROADCAST_PORT);
		// -------------------------------------------------------------------------
		// 25/07/2013 ECU set up the refresh handler
		// -------------------------------------------------------------------------
	    serverRefreshHandler = new ServerRefreshHandler ();
	    serverRefreshHandler.sleep (10000);			// 25/07/2013 ECU initial wait is 10 seconds
		// -------------------------------------------------------------------------
		// 25/07/2013 ECU indicate to keep running
		// -------------------------------------------------------------------------
	    keepRunning = true;    
	    // -------------------------------------------------------------------------
	    // 08/04/2014 ECU try and get the service to run in the foreground
	    //            ECU make it optional
	    // 01/09/2015 ECU changed to use StaticData
	    // -------------------------------------------------------------------------
	    if (StaticData.SERVER_SERVICE_FOREGROUND)
	    	StartServiceInForeground ();
		// -------------------------------------------------------------------------
		// 08/04/2014 ECU want to try and bring back in memory
		// -------------------------------------------------------------------------
		return Service.START_STICKY;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public void onDestroy () 
	{
		// -------------------------------------------------------------------------
		// 12/09/2013 ECU include debug mode check
		// 10/03/2015 ECU changed to use 'debugMode in 'storedData'
		// -------------------------------------------------------------------------
		if (PublicData.storedData.debugMode)	
		{
			// ---------------------------------------------------------------------
			// 08/11/2013 ECU use the custom toast
			// ---------------------------------------------------------------------
			Utilities.popToast (getString (R.string.server_service_destroyed));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 22/08/2013 ECU make sure that the server threads are ended
		// 12/09/2013 ECU include debug mode check
		// 10/03/2015 ECU changed to use 'debugMode in 'storedData'
		// -------------------------------------------------------------------------
		if (PublicData.storedData.debugMode)
		{
			// ---------------------------------------------------------------------
			// 08/11/2013 ECU use the custom toast
			// 26/05/2020 ECU changed to use the resource
			// ---------------------------------------------------------------------
			Utilities.popToast (getString (R.string.server_service_threads_destroyed));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 10/03/2015 ECU indicate that the threads are to stop
		// 15/10/2017 ECU put in the checks on null
		// -------------------------------------------------------------------------
		if (serverThread != null)
			serverThread.keepRunning 		= false;
		if (serverThreadForData != null)
			serverThreadForData.keepRunning = false;
		// -------------------------------------------------------------------------
		// 26/08/2013 ECU added the multicast server thread
		// 09/04/2016 ECU changed the name to broadcast
		// 15/10/2017 ECU put in the check on null
		// -------------------------------------------------------------------------
		if (broadcastServerThread != null)
		{
			// ---------------------------------------------------------------------
			// 25/05/2020 ECU tell the thread to close via a message rather than a
			//                'keepRunning' flag
			// ---------------------------------------------------------------------
			broadcastServerThread.broadcastRefreshHandler.sendEmptyMessage(StaticData.MESSAGE_FINISH);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 22/07/2013 ECU check if the socket is to be closed
		// -------------------------------------------------------------------------
		if (serverSocket != null)
		{
			try
			{
				serverSocket.close();
			}
			catch (IOException theException)
			{	
			}			
		}	
		// -------------------------------------------------------------------------
		// 02/08/2013 ECU check if the socket for data is to be closed
		// -------------------------------------------------------------------------
		if (serverSocketForData != null)
		{
			try
			{
				serverSocketForData.close();
			}
			catch (IOException theException)
			{
			}			
		}	
		// -------------------------------------------------------------------------
		// 22/09/2013 ECU check if the socket for data is to be closed
		// 09/04/2016 ECU changed name from 'multicastSocket'
		// -------------------------------------------------------------------------
		if (PublicData.datagramSocket != null)
		{
			PublicData.datagramSocket.close();		
		}	
		// -------------------------------------------------------------------------
		// 25/07/2013 ECU indicate that timing mechanism must stop
		// -------------------------------------------------------------------------
		keepRunning = false;
		// -------------------------------------------------------------------------
		// 11/04/2015 ECU indicate that the service has finished - if this 
		//                happened during initialisation then the main app should
		//                terminate immediately
		// -------------------------------------------------------------------------
		PublicData.errorSoFinishApp = getString (R.string.serverservice_destroyed);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private void processServerCommands (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 06/01/2016 ECU changed to use a list rather than a single string
		// -------------------------------------------------------------------------
		if (PublicData.stringsToProcess.size () > 0)
		{
			// ---------------------------------------------------------------------
			// 24/07/2013 ECU call up the activity to process the command
			// 06/01/2016 ECU process the string at the top of the queue
			// 19/10/2019 ECU changed to use Static....
			// ---------------------------------------------------------------------
			Intent localIntent = new Intent();
			localIntent.setClass (this,ServerCommands.class);
			localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
			localIntent.putExtra (StaticData.PARAMETER_COMMAND_STRING,PublicData.stringsToProcess.get (0));
			startActivity     (localIntent);
			// ---------------------------------------------------------------------
			// 24/07/2013 ECU indicate that everything has been done
			// 06/01/2016 ECU remove the entry at the top of the list
			// ---------------------------------------------------------------------
			PublicData.stringsToProcess.remove (0);
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	private void serverActions (int thePort)
	{
		try 
		{
			// ---------------------------------------------------------------------
			// 12/09/2013 ECU include debug mode check
			// 10/03/2015 ECU changed to use 'debugMode in 'storedData'
			// ---------------------------------------------------------------------
			if (PublicData.storedData.debugMode)	
			{
				// -----------------------------------------------------------------
				// 08/11/2013 ECU use the custom toast
				// 26/05/2020 ECU changed to use resource
				// -----------------------------------------------------------------
				Utilities.popToast (getString (R.string.server_service_port) + thePort, Toast.LENGTH_SHORT);
				// -----------------------------------------------------------------
			}
			
			serverSocket = new ServerSocket (thePort);
			// ---------------------------------------------------------------------
			// 30/06/2013 ECU start up the server thread which will deal with client connections
			// 22/08/2013 ECU changed to use serverThread
			// ---------------------------------------------------------------------
			serverThread = new ServerThread (serverSocket,this);
			new Thread (serverThread).start();
			// ---------------------------------------------------------------------
	     
		} 
		catch (Exception theException) 
		{
			// ---------------------------------------------------------------------
			// 08/11/2013 ECU use the custom toast
			// 11/04/2015 ECU make into 'any' exception so that the service can
			//                be stopped at this point
			// ---------------------------------------------------------------------
			Utilities.popToast ("Exception : " + theException);
			// ---------------------------------------------------------------------
			// 11/04/2015 ECU stop this service
			// ---------------------------------------------------------------------
			stopSelf ();
			// ---------------------------------------------------------------------
      	}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private void serverActionsForData (int thePort)
	{
		try 
		{
			// ---------------------------------------------------------------------
			// 12/09/2013 ECU include debug mode check
			// 10/03/2015 ECU changed to use 'debugMode in 'storedData'
			// ---------------------------------------------------------------------
			if (PublicData.storedData.debugMode)
			{
				// -----------------------------------------------------------------
				// 08/11/2013 ECU use the custom toast
				// 26/05/2020 ECU changed to use resource
				// -----------------------------------------------------------------
				Utilities.popToast (getString (R.string.server_service_data_port) + thePort,Toast.LENGTH_SHORT);
				// -----------------------------------------------------------------
			}
			serverSocketForData = new ServerSocket(thePort);
			// ---------------------------------------------------------------------
			// 30/06/2013 ECU start up the server thread which will deal with client connections	
			// 22/08/2013 ECU changed to use serverThreadForData
			// ---------------------------------------------------------------------
			serverThreadForData = new ServerThreadForData (serverSocketForData,this);
			new Thread (serverThreadForData).start();
	     
		} 
		catch (Exception theException) 
		{
			// ---------------------------------------------------------------------
			// 08/11/2013 ECU use the custom toast
			// 11/04/2015 ECU changed to general exception so that can easily
			//                stop the service
			// ---------------------------------------------------------------------
			Utilities.popToast ("Exception : " + theException);
			// ---------------------------------------------------------------------
			// 11/04/2015 ECU stop the service
			// ---------------------------------------------------------------------
			stopSelf ();
			// ---------------------------------------------------------------------
      	}
	}
	/* ============================================================================= */
	private void serverActionsForBroadcastMessages (int thePort)
	{
		// -------------------------------------------------------------------------
		// 12/09/2013 ECU include debug mode check
		// 10/03/2015 ECU changed to use 'debugMode in 'storedData'
		// -------------------------------------------------------------------------
		if (PublicData.storedData.debugMode)
		{
			// ---------------------------------------------------------------------
			// 08/11/2013 ECU use the custom toast
			// 26/05/2020 ECU changed to use the resource
			// ---------------------------------------------------------------------
			Utilities.popToast (getString (R.string.server_service_broadcast_port) + thePort, Toast.LENGTH_SHORT);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------	
		// 26/08/2013 ECU start up the thread that will process incoming broadcast datagrams
		// -------------------------------------------------------------------------
		broadcastServerThread = new BroadcastServerThread (this,thePort);
		// -------------------------------------------------------------------------
		// 11/04/2015 ECU only start if successfully bound to socket
		//            ECU the returned socket is held in
		//					'PublicData.datagramSocket'
		// 09/04/2016 ECU changed name from 'multicastSocket'
		// -------------------------------------------------------------------------
		if (PublicData.datagramSocket != null)
		{
			// ---------------------------------------------------------------------
			// 26/08/2013 ECU start up the broadcast thread
			// ---------------------------------------------------------------------
			new Thread (broadcastServerThread).start ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */	
	@SuppressLint("HandlerLeak")
	class ServerRefreshHandler extends Handler
	{
		@Override
	    public void handleMessage (Message theMessage) 
	    {   
			// ---------------------------------------------------------------------
			// 01/08/2013 ECU try and find a phone server on devices in the network
			// ---------------------------------------------------------------------			
			if (PublicData.deviceDetails != null)
			{
				// -----------------------------------------------------------------
				// 26/07/2013 ECU find a phone server on the network
				// -----------------------------------------------------------------
				if (PublicData.phoneServer == null)
					PublicData.phoneServer = Utilities.findPhoneServer ();
				// -----------------------------------------------------------------
				// 26/02/2016 ECU find a device that can control bluetooth remote
				//				  controller
				// -----------------------------------------------------------------
				if (PublicData.remoteControllerServer == null)
					PublicData.remoteControllerServer = Utilities.findRemoteControllerServer ();
				// -----------------------------------------------------------------
				// 18/03/2015 ECU find a device that can control the Belkin WeMo
				//                switches
				// -----------------------------------------------------------------
				if (PublicData.wemoServer == null)
					PublicData.wemoServer = Utilities.findWeMoServer ();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------	
			// 22/06/2013 ECU if we are to keep running then wait till next minute
			// ---------------------------------------------------------------------
			if (keepRunning)
			{
				// -----------------------------------------------------------------
				// 24/07/2013 ECU check for requests for TCP services
				// -----------------------------------------------------------------
				processServerCommands (getBaseContext());			
				// -----------------------------------------------------------------
				sleep (1000);
				// -----------------------------------------------------------------
			}
	    }
	    /* ------------------------------------------------------------------------ */
	    public void sleep(long delayMillis)
	    {		
	    	// --------------------------------------------------------------------
	        this.removeMessages(0);
	        sendMessageDelayed(obtainMessage(0), delayMillis);
	        // ---------------------------------------------------------------------
	    }
	};
	/* ============================================================================= */
	void StartServiceInForeground ()
	{
		// -------------------------------------------------------------------------
		// 09/02/2014 ECU try and place service in the foreground so that it runs
		//                even when the device is in standby mode
		// 28/07/2016 ECU following the change to Marshmallow then have to use
		//                the NotificationCompat builder instead of just setting
		//                Notification
		// -------------------------------------------------------------------------
		// 28/07/2016 ECU Note - declare the activity to be actioned when the
		//                       notification is clicked
		// -------------------------------------------------------------------------
		Intent intent = new Intent(this,ClonerActivity.class);
		intent.setFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent, 0);
		// -------------------------------------------------------------------------
		Notification note = new NotificationCompat.Builder (this)
										.setWhen (System.currentTimeMillis())
										.setSmallIcon (R.drawable.text_icon)
										.setAutoCancel (true)
										.setTicker ("Server Service Started")
										.setContentIntent (pendingIntent).build ();
		// -------------------------------------------------------------------------
		// 09/02/2014 ECU put the service into the foreground
		// -------------------------------------------------------------------------
		startForeground (310117, note);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	
	// =============================================================================
	@SuppressLint("HandlerLeak")
	static class MessageHandler extends Handler
    {
		// -------------------------------------------------------------------------
		// 05/01/2016 ECU created as a message handler
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
        		case StaticData.MESSAGE_FTP_CLIENT:
        			// -----------------------------------------------------------------
        			// 09/01/2016 ECU added to handle a message from an FTP server
        			//                activity so that this device can establish a
        			//                channel for communication
        			//            ECU the server's IP address is in the message
        			// 11/01/2016 ECU changed the object from string to that of a
        			//                file transfer block
        			// 12/01/2016 ECU moved here from MessageHandler
        			// -----------------------------------------------------------------
        			FileTransferUtilities.ClientInitialise (context,(FileTransferBlock) theMessage.obj);
        			// -----------------------------------------------------------------
        			break;
        		// ---------------------------------------------------------------------
        		case StaticData.MESSAGE_FTP_SERVER:
        			// -----------------------------------------------------------------
        			// 12/01/2016 ECU moved here from MessageHandler
        			// 15/01/2016 ECU leave here but is not needed
        			// -----------------------------------------------------------------
        			// -----------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        	}
        }
        // -------------------------------------------------------------------------
    }
	// =============================================================================
}

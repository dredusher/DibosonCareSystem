package com.usher.diboson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


// =================================================================================
// 15/09/2017 ECU created to handle all aspects of 'video streaming'
//            ECU Note - this activity will only be called if StaticData.VIDEO_STREAMING
//                		 is set 'true'
//            ECU put in the check as to whether the activity has been created
//                anew or is being recreated after having been destroyed by
//                the Android OS
// 24/01/2018 ECU IMPORTANT IMPORTANT IMPORTANT
//                =============================
//				  Everything logically looks correct but when the remote device
//                starts recording then an 'illegalStateException' is thrown. If the
//                OutputFileName is changed for the 'file descriptor' to a physical
//                file then everything is fine. Searching the internet indicates that
//                this is a known issue because when writing to a socket via a file
//                descriptor then this is not 'seekable' which is what the media
//                recorder wants.
//
//			      A more concise explanation is that the MediaRecorder writes to
//                its output file using 'atoms' where each atom starts with its size.
//                This means that it needs to wait until the video is finished in
//                order to write the headers that need to appear at the beginning
//                of the output file. Since a 'socket' is not 'seekable' then the
//				  headers cannot be written at the correct location.
// =================================================================================

// =================================================================================
// =================================================================================

// =================================================================================
public class VideoStreamingActivity extends DibosonActivity 
{
	// =============================================================================
			static 	Context 			context;
			static 	String [] 			devices;
	public 	static	FileDescriptor 		fileDescriptor;
			static 	ServerSocket 		serverSocket;
					String 				videoRequestor;
					VideoStreamHandler	videoStreamHandler;
	// =============================================================================

	// =============================================================================
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		// 18/09/2017 ECU remember the context for later
		// -------------------------------------------------------------------------
		context = this;
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 15/09/2017 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_video_streaming);
			// ---------------------------------------------------------------------
			// 27/09/2017 ECU allow access to the network from the UI thread
			// ---------------------------------------------------------------------
			APIIssues.NetworkOnMainUIThread (android.os.Build.VERSION.SDK_INT);
			// ---------------------------------------------------------------------
			// 15/09/2017 ECU check if any parameters have been passed through
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 15/09/2017 ECU get any IP address being passed through
				// -----------------------------------------------------------------
				videoRequestor = extras.getString (StaticData.PARAMETER_IP_ADDRESS);
				// -----------------------------------------------------------------
				Utilities.popToast ("Requestor is " + videoRequestor);
				// -----------------------------------------------------------------
				// 18/09/2017 ECU now start up the streaming
				// -----------------------------------------------------------------
				videoStreamHandler = new VideoStreamHandler ();
				videoStreamHandler.sendEmptyMessage (StaticData.MESSAGE_VIDEO_STREAM_START);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 15/09/2017 ECU get the IP address of the device which will be the
				//                source of the video
				// 18/09/2017 ECU changed the argument from 'true' to 'false' so that
				//                this device is not displayed in the list
				// -----------------------------------------------------------------
				DialogueUtilities.singleChoice(this,"Select the Source of the Video",
						(devices = Utilities.deviceListAsArray (false)),0, 
						Utilities.createAMethod (VideoStreamingActivity.class,"ConfirmMethod",0),
						Utilities.createAMethod (VideoStreamingActivity.class,"CancelMethod",0));
				// -----------------------------------------------------------------
			}
		}
		else
		{

			// ---------------------------------------------------------------------
			// 15/09/2017 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onBackPressed ()
	{
		// -------------------------------------------------------------------------
		// 15/09/2017 ECU created to handle the 'back key' is pressed
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
		// 15/09/2017 ECU just terminate this activity
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
		// 15/09/2017 ECU now call the super for this method
		// -------------------------------------------------------------------------
		super.onBackPressed();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class VideoStreamHandler extends Handler 
	{
		// -------------------------------------------------------------------------
		// 18/09/2017 ECU handle incoming messages
		// -------------------------------------------------------------------------
		@Override
	    public void handleMessage (Message message) 
		{	
			switch (message.what) 
			{
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_VIDEO_STREAM_START:
					// -------------------------------------------------------------
					try 
					{
						// ---------------------------------------------------------
						// 18/09/2017 ECU try and connect to the requestor
						// ---------------------------------------------------------
						Socket socket = new Socket (InetAddress.getByName (videoRequestor),StaticData.VIDEO_STREAM_PORT);
						Utilities.popToastAndSpeak ("Socket : " + socket.getLocalPort(),true);
						// ---------------------------------------------------------
						// 18/09/2017 ECU get the file descriptor that will be used
						//                to transmit the video stream
						//            ECU would like to pass the file descriptor as
						//                a parameter to the VideoRecorder activity
						//                but this does not seem possible - hence
						//                use the 'public static' variable
						// ---------------------------------------------------------
						fileDescriptor = (ParcelFileDescriptor.fromSocket (socket)).getFileDescriptor();
						// ---------------------------------------------------------
						// 18/09/2017 ECU how start up the recorder and send the data
						//                to the socket
						// ---------------------------------------------------------
						Intent localIntent = new Intent (context,VideoRecorder.class);
						// -------------------------------------------------------------
						// 18/09/2017 ECU indicate that a new task is required
						// -------------------------------------------------------------
						localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
						// -------------------------------------------------------------
						// 18/09/2017 ECU set up any required parameters and start the
						//                activity
						//            ECU NOTE - see the comment made above about the file
						//                       descriptor
						// -------------------------------------------------------------	
						localIntent.putExtra (StaticData.PARAMETER_VIDEO_STREAM,true);	
						context.startActivity (localIntent);
						// ---------------------------------------------------------
					} 
					catch (Exception theException) 
					{
					} 
					// -------------------------------------------------------------
					break;
	            // -----------------------------------------------------------------
	            default:
	            	// -------------------------------------------------------------
	            	// 18/09/2017 ECU ignore any 'non specified' messages
	            	// -------------------------------------------------------------
	            	break;
	            // -----------------------------------------------------------------
	        }
		}
	}
	// =============================================================================
	public static void CancelMethod (int theIndex)
	{
		 // ------------------------------------------------------------------------
		 // 15/09/2017 ECU called when the user 'cancels' the action
		 // ------------------------------------------------------------------------
	}
	// =============================================================================
	public static void ConfirmMethod (int theIndex)
	{
		 // ------------------------------------------------------------------------
		 // 15/09/2017 ECU called when the user 'confirms' the action
		 // ------------------------------------------------------------------------
		 String deviceIPAddress =  Devices.returnIPAddress (devices [theIndex]);
		 // ------------------------------------------------------------------------
		 // 15/09/2017 ECU want to set up a socket that receives the video stream
		 // ------------------------------------------------------------------------
		 try
		 {
			 serverSocket = new ServerSocket (StaticData.VIDEO_STREAM_PORT);
			 // --------------------------------------------------------------------
			 Thread clientThread = new Thread()
			 {
				@Override
				public void run()
				{
					Socket clientSocket;
					try 
					{
						clientSocket = serverSocket.accept();
						MessageHandler.popToastAndSpeak ("Client connection : " + clientSocket.getInetAddress().getHostAddress());
						InputStream  input 	= clientSocket.getInputStream ();
						byte [] localBuffer = new byte [StaticData.BUFFER_SIZE_MAX];
						int 	localNumberOfBytesRead;
						
						while ((localNumberOfBytesRead = input.read(localBuffer)) > 0)
						{
							Utilities.LogToProjectFile ("VideoStreamingActivity","Read : " + localNumberOfBytesRead);
						}
					} 
					catch (IOException theException) 
					{
					}
					
				}
			 };	
			 clientThread.start();   
			 // --------------------------------------------------------------------
			 Utilities.sendDatagramType (context,deviceIPAddress,StaticData.SOCKET_MESSAGE_VIDEO_STREAM_START);
			 // --------------------------------------------------------------------		 
		 }
		 catch (Exception theException)
		 {
			 // --------------------------------------------------------------------
			 // 15/09/2017 ECU problems creating the socket
			 // --------------------------------------------------------------------
			 // --------------------------------------------------------------------
		 }
		 
	}
	// =============================================================================
}

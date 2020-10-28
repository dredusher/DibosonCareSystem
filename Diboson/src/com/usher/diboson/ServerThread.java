package com.usher.diboson;

import android.app.Activity;
import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Activity implements Runnable 
{
	// =========================================================================
	// ServerThread
	// ============
	// This thread, which is started by the ServerService, constitutes a very
	// simple 'telnet server' that listens on 'PublicData.socketNumberForData'
	// (by default 8012).
	//
	// It is just added as a demonstration and would need further work if it
	// were to put into any practical use.
	// =========================================================================
	
	// =========================================================================
	// 01/06/2013 ECU created
	// 10/01/2015 ECU changed to avoid using array's or ArrayList's
	/* ========================================================================= */
	//final static String TAG = "ServerThread";
	/* ========================================================================= */
	int						commandNumber;				// 22/10/2019 ECU added
	private static Context 	context;
	public boolean 			keepRunning = true;			// 22/08/2013 ECU change to public
	ServerSocket   			serverSocket;
	/* ========================================================================= */
	public ServerThread (ServerSocket theServerSocket,Context theContext) 
	{
		serverSocket 	= theServerSocket;
		context 		= theContext;
		// ---------------------------------------------------------------------
		// 12/09/2013 ECU include debug mode check
		// 10/01/2015 ECU changed to use new Method
		// ---------------------------------------------------------------------
		Utilities.debugPopToast ("ServerThread created");
		// ---------------------------------------------------------------------
    }
	/* ============================================================================= */
	public void run() 
	{
		try
		{  
			while(keepRunning) 
			{
				// -----------------------------------------------------------------
				// 01/07/2013 ECU wait for a client to connect
				// -----------------------------------------------------------------
				final Socket clientSocket = serverSocket.accept();
	            // -----------------------------------------------------------------
				// 01/07/2013 ECU create a thread to handle this connection
	            // -----------------------------------------------------------------
				Thread clientThread = new Thread ()
				{
					public void run ()
					{
						// ---------------------------------------------------------
						// 30/06/2013 ECU process commands from the connected client
						// 04/07/2013 ECU add in the bit to increment/decrement the number of attached client
	            		// ---------------------------------------------------------
						processClientConnection (clientSocket);
						// ---------------------------------------------------------
					}
				};
				// -----------------------------------------------------------------
				clientThread.start();  
				// -----------------------------------------------------------------
			}
		}
		catch(IOException theException)   
		{	
			// ---------------------------------------------------------------------
			// 22/07/2013 ECU if the serverSocket is closed then a SocketException 
			//                will be generated
			// 18/01/2015 ECU took out the printing of the stack because if
			//                the 'accept' is interrupted then it will throw a
			//                'socket closed' exception which is fine
			// ---------------------------------------------------------------------
	    }
	}
	/* ============================================================================= */
	private void processClientConnection (Socket theSocket)
	{
		try
		{
			// ---------------------------------------------------------------------
			// 10/01/2015 ECU get the streams that will be used for communication on
			//                the socket. The 'true' on the output means 'autoflush'
			// 20/10/2019 ECU provide a tidied up version to make it easier for the
			//                user
			// 22/01/2020 ECU in doing the changes at 20/10/2019 forgot that this
			//                was the mechanism for passing a command to the 'phone
			//                server' to make a call and send a SMS message - had
			//                to put these back as 'legacy commands'
			// ---------------------------------------------------------------------
			BufferedReader input 	= new BufferedReader (new InputStreamReader (theSocket.getInputStream()));
			PrintWriter    output 	= new PrintWriter (theSocket.getOutputStream (),true);
			// ---------------------------------------------------------------------
			// 16/03/2015 ECU added the IP address of the server
			// 19/10/2019 ECU changed to use the welcome format
			// ---------------------------------------------------------------------
			output.println (String.format (context.getString (R.string.tcp_server_welcome_format),PublicData.ipAddress) + 
								StaticData.CARRIAGE_RETURN);
			// ---------------------------------------------------------------------
			// 22/10/2019 ECU display a list of commands available
			//            ECU check that the server is ready
			// ---------------------------------------------------------------------
			if (PublicData.userInterfaceRunning)
				output.println (WebUtilities.commandList (StaticData.NEWLINE + StaticData.CARRIAGE_RETURN));
			// ---------------------------------------------------------------------
			String inputString = StaticData.BLANK_STRING;
			// ---------------------------------------------------------------------
			// 30/06/2013 ECU loop processing the incoming data until the user exits
			//                or the connection is lost
			// 22/10/2019 ECU check that the server is running (...userInterfaceRunning)
			// 23/10/2019 ECU check 'exit' check to startsWith
			// ---------------------------------------------------------------------
			while (PublicData.userInterfaceRunning)
			{	
				// -----------------------------------------------------------------
				// 19/10/2019 ECU changed to use resource
				// -----------------------------------------------------------------
				output.println (context.getString (R.string.tcp_server_enter_command) + StaticData.CARRIAGE_RETURN);
				// -----------------------------------------------------------------
				// 23/10/2019 ECU read in input from the client
				// -----------------------------------------------------------------
				inputString = input.readLine();
				// -----------------------------------------------------------------
				if ((inputString != null) && !inputString.equalsIgnoreCase(StaticData.BLANK_STRING))
				{
					// -------------------------------------------------------------
					// 22/10/2019 ECU at this point only accept a number or 'exit'
					// -------------------------------------------------------------
					try
					{
						// ---------------------------------------------------------
						// 23/10/2019 ECU check for the exit command
						// ---------------------------------------------------------
						if (inputString.startsWith ("exit"))
						{
							// -----------------------------------------------------
							// 23/10/2019 ECU user has requested that the server close
							// -----------------------------------------------------
							break;
							// -----------------------------------------------------
						}
						else
						if (inputString.startsWith (StaticData.SERVER_COMMAND))
						{
							// -----------------------------------------------------
							// 22/01/2020 ECU check for any legacy commands
							// -----------------------------------------------------
							Utilities.processCommandStringLegacy (context,inputString,input,output);
							// -----------------------------------------------------
							break;
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
						// 22/10/2019 ECU read in the user's data
						// ---------------------------------------------------------
						// 22/10/2019 ECU only command numbers are required
						// ---------------------------------------------------------
						commandNumber = Integer.parseInt (inputString);
						// ---------------------------------------------------------
						if ((commandNumber >= 0) && (commandNumber < GridActivity.gridImages.length))
						{
							// -----------------------------------------------------
							// 22/10/2019 ECU start up the activity which has been entered
							// 24/06/2020 ECU change the logic so that a message is
							//                sent to the message handler in GridActivity
							//                rather than starting the activity which
							//                could cause a problem if 'launchmode' has
							//                been specified in the manifest
							// -----------------------------------------------------
							//	Intent localIntent = new Intent (context,GridActivity.class);
							//	localIntent.putExtra (StaticData.PARAMETER_POSITION,commandNumber);
							//	localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							//	context.startActivity (localIntent);
							// -----------------------------------------------------
							// 24/06/2020 ECU instead of starting GridActivity, which
							//                is already running, just pass it a message
							//                to start the specified activity
							// 25/06/2020 ECU changed to use the new class
							// -----------------------------------------------------
							Utilities.startASpecficActivity (commandNumber);
							// -----------------------------------------------------
							// 20/10/2019 ECU tell the user was has been done
							// -----------------------------------------------------
							output.println ("Activity '" + commandNumber + 
									"' has been started" + StaticData.CARRIAGE_RETURN);
							// -----------------------------------------------------
						}
						else
						{
							// -----------------------------------------------------
							// 20/10/2019 ECU indicate that an invalid number was entered
							// -----------------------------------------------------
							output.println (String.format ("Only a number between 0 and %d is allowed", 
									(GridActivity.gridImages.length - 1)) + StaticData.CARRIAGE_RETURN);
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
					}
					catch (Exception theException) 
					{
						// ---------------------------------------------------------
						output.println ("Invalid data entered" + StaticData.CARRIAGE_RETURN);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 26/07/2013 ECU send a 'ready' prompt
				// 19/10/2019 ECU changed to use the resource
				// -----------------------------------------------------------------
				output.println (context.getString (R.string.tcp_server_ready) + StaticData.CARRIAGE_RETURN);
				// -----------------------------------------------------------------
				// 26/07/2013 ECU make sure the output is flushed
				// -----------------------------------------------------------------
				output.flush ();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 19/10/2019 ECU Note - the client has indicated that it wants this
			//                       server to close the connection
			// 19/10/2019 ECU changed to use resource
			// ---------------------------------------------------------------------
			output.println (context.getString (R.string.tcp_server_connection_closing) + StaticData.CARRIAGE_RETURN);
			theSocket.close ();
			// ---------------------------------------------------------------------		
		}
		catch (IOException theException)
		{
			theException.printStackTrace();
		}
	}
	/* ============================================================================================== */
}

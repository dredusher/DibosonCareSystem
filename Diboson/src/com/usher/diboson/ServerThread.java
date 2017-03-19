package com.usher.diboson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import android.app.Activity;
import android.content.Context;

public class ServerThread extends Activity implements Runnable 
{
	// =========================================================================
	// 01/06/2013 ECU created
	// 10/01/2015 ECU changed to avoid using array's or ArrayList's
	/* ========================================================================= */
	//final static String TAG = "ServerThread";
	/* ========================================================================= */
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
					}
				};
				clientThread.start();  	            		            
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
			// ---------------------------------------------------------------------
			BufferedReader input 	= new BufferedReader(new InputStreamReader(theSocket.getInputStream()));
			PrintWriter output 		= new PrintWriter(theSocket.getOutputStream(),true);
			// ---------------------------------------------------------------------
			// 16/03/2015 ECU added the IP address of the server
			// ---------------------------------------------------------------------
			output.println ("Welcome to the Diboson TCP Server on " + PublicData.ipAddress + "\r");
			// ---------------------------------------------------------------------
			String inputString = "";
			// ---------------------------------------------------------------------
			// 30/06/2013 ECU loop processing the incoming data until the user exits
			//                or the connection is lost
			// ---------------------------------------------------------------------
			while (inputString != null && !inputString.equalsIgnoreCase("exit"))
			{	
				output.println ("Enter command : \r");
				inputString = input.readLine();
				// -----------------------------------------------------------------
				// 02/07/2013 ECU process incoming command
				// -----------------------------------------------------------------
				if (inputString != null)
					Utilities.processCommandString (context,inputString,input,output);
				// -----------------------------------------------------------------
				// 26/07/2013 ECU send a 'ready' prompt
				// -----------------------------------------------------------------
				output.println ("ready\r");
				// -----------------------------------------------------------------
				// 26/07/2013 ECU make sure the output is flushed
				// -----------------------------------------------------------------
				output.flush ();
			}
			// ---------------------------------------------------------------------
			output.println ("connection closing\r");
			theSocket.close ();
			// ---------------------------------------------------------------------		
			// 04/07/2013 ECU clear the entry for this client
			// ---------------------------------------------------------------------
		}
		catch (IOException theException)
		{
			theException.printStackTrace();
		}
	}
	/* ============================================================================================== */
}

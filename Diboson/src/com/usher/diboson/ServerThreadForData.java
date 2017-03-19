package com.usher.diboson;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Message;

public class ServerThreadForData extends Activity implements Runnable 
{
	// =============================================================================
	// 19/08/2013 ECU changed the logic so that the receiving file name is
	//                set by the bit in the incoming byte
	// 31/07/2016 ECU have a good tidy up of the code
	/* ============================================================================= */
	private static final String TAG = "ServerThreadForData";
	/* ============================================================================= */
	// 21/04/2015 ECU declared the position of information in the message header
	// -----------------------------------------------------------------------------
	private static final int	MESSAGE_TYPE_POSITION		= 0;
	private static final int	MESSAGE_DATA_POSITION		= 1;
	// =============================================================================
	private static Context context;
	// -----------------------------------------------------------------------------
	Socket 			clientSocket;
	String 			fileHeader 			= "";						// 03/08/2013 ECU added
	String 			fileToPlay 			= "";						// 07/08/2013 ECU added - will remember 
	                                                                //                the name of the file to be played
	public boolean 	keepRunning = true;								// 22/08/2013 ECU changed to public
	int 			lastGoodMessageType = StaticData.NO_RESULT;		// 07/08/2013 ECU remember the last good message
	boolean 		playBeingProcessed 	= false;					// 14/08/2013 ECU added - indicate that the play 
	                                                                //                command is being processed
	ReceiveFile		receiveFile 		= null;						// 07/04/2014 ECU added
	ServerSocket 	serverSocket;	
	// ============================================================================= 
	public ServerThreadForData (ServerSocket theServerSocket,Context theContext) 
	{
		// -------------------------------------------------------------------------
		// 02/08/2016 ECU Note - save some variables needed later on
		// -------------------------------------------------------------------------
		serverSocket 	= theServerSocket;
		context 		= theContext;
		// -------------------------------------------------------------------------
		// 12/09/2013 ECU include debug mode check
		// 10/03/2015 ECU changed to use 'debugMode in 'storedData'
		// -------------------------------------------------------------------------
		if (PublicData.storedData.debugMode)
		{
			// ---------------------------------------------------------------------
			// 08/11/2013 ECU use the custom toast
			// 31/07/2016 ECU took out the Toast.LENGTH_SHORT
			// ---------------------------------------------------------------------
			Utilities.popToast ("ServerThreadForData created");
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
    }
	// ============================================================================= 
	public void run() 
	{
		try
		{  
			while (keepRunning) 
			{
				// -----------------------------------------------------------------
				// 01/07/2013 ECU wait until a connection occurs
				// -----------------------------------------------------------------
				clientSocket = serverSocket.accept();
	            // -----------------------------------------------------------------
				// 01/07/2013 ECU create a thread to handle this connection
	            // -----------------------------------------------------------------
				Thread clientThread = new Thread ()
				{
					public void run ()
					{
						// ---------------------------------------------------------
						// 30/06/2013 ECU process commands from the connected client
						// 04/07/2013 ECU add in the bit to increment/decrement the 
						//                number of attached client
						// ---------------------------------------------------------
						processClientConnection (clientSocket);
						// ---------------------------------------------------------
					}
				};
				// -----------------------------------------------------------------
				// 01/07/2013 ECU start up the thread to handle the incoming message
				// -----------------------------------------------------------------
				clientThread.start ();  
				// -----------------------------------------------------------------
			}
		}
		catch(IOException theException)   
		{
			// ---------------------------------------------------------------------
			// 22/07/2013 ECU if the serverSocket is closed then a SocketException
			//                will be generated
			// ---------------------------------------------------------------------
	    }
	}
	/* ============================================================================= */
	private void processClientConnection (Socket theSocket)
	{
		// -------------------------------------------------------------------------
		// 02/08/2013 ECU order of things is
		//					1 ..... read in the type of message being sent
		//					2 ..... send a message to say ready to receive associated message
		//					3 ..... read in the rest of message depending on the type
		// -------------------------------------------------------------------------
		byte [] 	messageDetails;
		String		messageSender = null;				// 31/07/2016 ECU the IP address of the message sender
		int			typeOfMessage = 99;	
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 01/07/2013 ECU declare streams for communication on the socket
			// ---------------------------------------------------------------------
			InputStream  input 	= theSocket.getInputStream ();
			OutputStream output = theSocket.getOutputStream ();		
			// ---------------------------------------------------------------------
			// 10/08/2013 ECU note that the following read only obtains a byte which
			//                will limit the types of messages to be transferred - 
			//                keep them less than 127
			// 11/08/2013 ECU change so that type is obtained from method rather than
			//                just input.read()
			// 19/08/2013 ECU changed to handle the fact that socketMessageType 
			//                returns an array
			// 31/01/2015 ECU changed method name
			// ---------------------------------------------------------------------
			messageDetails = Utilities.socketReceiveMessageType (input);
			// ---------------------------------------------------------------------
			// 03/08/2013 ECU now handle the rest of the message
			// 10/08/2013 ECU set the buffer size from the variable rather than 
			//                literally (was 1000000)
			// 01/09/2015 ECU changed to use StaticData
			// 01/08/2016 ECU changed name from 'theBuffer' and 'theNumber....'
			// ---------------------------------------------------------------------
			byte [] localBuffer = new byte [StaticData.BUFFER_SIZE_MAX];
			int 	localNumberOfBytesRead;
			// ---------------------------------------------------------------------
			// 31/07/2016 ECU store the IP address of the sender of this message
			// ---------------------------------------------------------------------
			messageSender	=	theSocket.getInetAddress().getHostAddress();
			// ---------------------------------------------------------------------
			// 23/03/2015 ECU details of the message received
			// 21/04/2015 ECU changed to use static position
			// 31/07/2016 ECU changed to use 'messageSender'
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG,"received message type " +  
											messageDetails [MESSAGE_TYPE_POSITION] +
											" from " + messageSender);
			// ---------------------------------------------------------------------
			// 08/01/2014 ECU switch depending on the type of message
			// 21/04/2015 ECU changed to use the static position
			// ---------------------------------------------------------------------
			switch (messageDetails [MESSAGE_TYPE_POSITION])
			{
				// =================================================================
				case StaticData.SOCKET_MESSAGE_ACTIONS:
					// -------------------------------------------------------------
					// 15/06/2016 ECU created to process the actions that are
					//                included within the message
					// 17/06/2016 ECU added the final 'true' to place this set of
					//                actions at the top of the queue
					// -------------------------------------------------------------
					Utilities.actionHandler (context,(String) Utilities.socketMessagesReadObject (context,theSocket),true);  
					// -------------------------------------------------------------
					break;
				// =================================================================
				case StaticData.SOCKET_MESSAGE_ARE_YOU_THERE:
					// -------------------------------------------------------------
					// 17/06/2016 ECU created to process a coming request to determine
					//                if this device is running
					// 31/07/2016 ECU changed to use 'messageSender'
					// -------------------------------------------------------------
					Utilities.socketMessagesSendMessageType (context,
															 messageSender,
															 PublicData.socketNumberForData,
															 StaticData.SOCKET_MESSAGE_I_AM_HERE);
					// -------------------------------------------------------------
					break;
				/* ================================================================= */
				case StaticData.SOCKET_MESSAGE_CHUNK_ACK:
					PublicData.chunkResponse = ChunkDetails.ACK;
					break;
				/* ================================================================= */
				case StaticData.SOCKET_MESSAGE_CHUNK_NAK:
					PublicData.chunkResponse = ChunkDetails.NAK;
					break;
				// =================================================================
				case StaticData.SOCKET_MESSAGE_CLONE_FINISH:
					// -------------------------------------------------------------
					// 06/04/2014 ECU indicate that cloning has finished
					// -------------------------------------------------------------
					PublicData.cloningInProgress = false;
					// -------------------------------------------------------------
					break;
				/* ================================================================= */	
				case StaticData.SOCKET_MESSAGE_CLONE_START:
					// -------------------------------------------------------------
					// 09/01/2015 ECU get the cloning details which will be passed
					//                with the command
					// 31/01/2015 ECU changed the method name
					// -------------------------------------------------------------
					ClonerActivity.cloningDetails  
					= (CloningDetails) Utilities.socketMessagesReadObject (context,theSocket);
					// -------------------------------------------------------------
					// 06/04/2014 ECU indicate that cloning is being started
					// -------------------------------------------------------------
					PublicData.cloningInProgress = true;
					// -------------------------------------------------------------
					// 10/01/2016 ECU store the address of the 'cloner' device
					// 31/07/2016 ECU changed to use 'messageSender'
					// -------------------------------------------------------------
					PublicData.clonerIPAddress = messageSender;
					// -------------------------------------------------------------
					break;
				/* ================================================================= */	
				case StaticData.SOCKET_MESSAGE_DATAGRAM:
					// -------------------------------------------------------------
					// 31/01/2015 ECU changed the method name
					// -------------------------------------------------------------
					PublicData.datagram = (Datagram) Utilities.socketMessagesReadObject (context,theSocket);
					// -------------------------------------------------------------
					// 19/10/2014 ECU if the location activity is running then tell it that
					//                the datagram has been updated
					// -------------------------------------------------------------
					PublicData.datagramChanged = true;
					// -------------------------------------------------------------
					break;
				/* ================================================================= */
				case StaticData.SOCKET_MESSAGE_DATAGRAM_ACTION:
					// -------------------------------------------------------------
					// 31/01/2015 ECU changed the method name
					// -------------------------------------------------------------
					PublicData.datagram = (Datagram) Utilities.socketMessagesReadObject (context,theSocket);
					// -------------------------------------------------------------
					// 03/08/2013 ECU indicate that the datagram is to be actioned
					// -------------------------------------------------------------
					PublicData.datagramToAction = true;
					
					break;					
				/* ================================================================= */
				case StaticData.SOCKET_MESSAGE_FILE:
					// -------------------------------------------------------------
					// 31/01/2015 ECU this cause the received bytes to be placed in
					//                the specified file
					// 01/08/2016 ECU changed the name from 'theBuffer' and 'theNumber...'
					// -------------------------------------------------------------
					localNumberOfBytesRead 
						= Utilities.socketMessages (context,theSocket,localBuffer,localBuffer.length,
							PublicData.projectFolder + context.getString (R.string.temp_file_socket));
					break;
				/* ================================================================= */
				case StaticData.SOCKET_MESSAGE_FILE_ACK:
					PublicData.fileTransferStatus = StaticData.SOCKET_MESSAGE_FILE_ACK;
					break;
				/* ================================================================= */
				case StaticData.SOCKET_MESSAGE_FILE_CHUNKS:
					// -------------------------------------------------------------
					// 14/08/2013 ECU do not process the command until the play 
					//                command has been processed
					// -------------------------------------------------------------
					if (playBeingProcessed)
					{
						Utilities.debugMessage (TAG, "waiting for play to be processed");
						// ---------------------------------------------------------
						// 14/08/2013 ECU wait a short time before trying again
						// ---------------------------------------------------------
						while (playBeingProcessed)
						{
							// -----------------------------------------------------
							// 14/08/2013 ECU 'sleep' a bit - initially had tried to 
							//                use 'wait ()' but got IllegalMonitorStateException
							// -----------------------------------------------------
							try 
							{
								Thread.sleep (500);
							} 
							catch (InterruptedException theException) 
							{
								Utilities.debugMessage(TAG, "Exception:" + theException);
							}
						}
						
						Utilities.debugMessage (TAG, "finished waiting for play command to finish");
					}
					// -------------------------------------------------------------
					//            ECU set the file header dependent on the toggle flag
					// 19/08/2013 ECU select the destination file dependent on the incoming
					//				  data byte
					// 21/04/2015 ECU changed to use the strings in resources
					//            ECU changed to use static position
					// -------------------------------------------------------------
					if (Utilities.bitHandler (messageDetails[MESSAGE_DATA_POSITION],StaticData.SOCKET_DATA_FILE))
						fileHeader = context.getString (R.string.received_file_1);
					else
						fileHeader = context.getString (R.string.received_file_2);
					// -------------------------------------------------------------
					//            ECU now toggle the file header flag
					//
					//MainActivity.socketFileToggle = !MainActivity.socketFileToggle;
					// -------------------------------------------------------------
					//            ECU now pull in the file which will be sent in chunks
					// 31/01/2015 ECU changed the method name
					// 01/09/2015 ECU changed to use StaticData
					// ------------------------------------------------------------- 
					Utilities.socketMessagesReadFileInChunks (context,theSocket,PublicData.projectFolder +
							fileHeader + context.getString(R.string.temp_file_socket),StaticData.SOCKET_CHUNK_SIZE);	
					
					break;
				/* ================================================================= */
				case StaticData.SOCKET_MESSAGE_FILE_DETAILS:
					// -------------------------------------------------------------
					// 02/02/2014 ECU added to receive the 'FileDetails' object associated with a file
					// 31/01/2015 ECU changed the method name
					// -------------------------------------------------------------
					FileDetails localFileDetails = (FileDetails) Utilities.socketMessagesReadObject (context,theSocket);
					// -------------------------------------------------------------
					// 02/02/2014 ECU process the received File object
					// 28/03/2016 ECU put in the check on returned object
					// 31/07/2016 ECU changed to use 'messageSender'
					// -------------------------------------------------------------
					if (localFileDetails != null)
						Utilities.HandleFileDetails (context, localFileDetails,messageSender);
					// -------------------------------------------------------------
					break;
				/* ================================================================= */
				case StaticData.SOCKET_MESSAGE_FILE_INFO:
					// -------------------------------------------------------------
					// 19/03/2014 ECU added to handle incoming 'File' details
					// 31/01/2015 ECU changed the method name
					// 31/07/2016 ECU changed to use 'messageSender'
					// -------------------------------------------------------------
					FileDetails infoFileDetails = (FileDetails) Utilities.socketMessagesReadObject (context,theSocket);
					
					ClonerActivity.RequestFile (context,infoFileDetails,messageSender);	
					// -------------------------------------------------------------
					break;
				/* ================================================================= */
				case StaticData.SOCKET_MESSAGE_FILE_NAK:
					
					PublicData.fileTransferStatus = StaticData.SOCKET_MESSAGE_FILE_NAK;
					// -------------------------------------------------------------
					// 14/08/2013 ECU make it look as if the previous track has finished
					// -------------------------------------------------------------
					PublicData.musicPlayerRemote = false;
					// -------------------------------------------------------------
					break;
				// =================================================================
				case StaticData.SOCKET_MESSAGE_FILE_TRANSFER:
					// -------------------------------------------------------------
					// 09/01/2016 ECU created to handle the call for the server to
					//                start a file transfer
					//            ECU send a message to the general handler with
					//                the address of the server
					// 11/01/2016 ECU change the object to that of a transfer block
					// 12/01/2016 ECU changed to use message handler in ServerService
					// -------------------------------------------------------------
					Message localMessage = ServerService.messageHandler.obtainMessage (StaticData.MESSAGE_FTP_CLIENT,
							          Utilities.socketMessagesReadObject (context,theSocket));
					ServerService.messageHandler.sendMessage (localMessage);
					// -------------------------------------------------------------
					break;
				// =================================================================
				case StaticData.SOCKET_MESSAGE_I_AM_HERE:
					// -------------------------------------------------------------
					// 17/06/2016 ECU remote device has indicated it is present
					// -------------------------------------------------------------
					PublicData.messageHandler.sendEmptyMessage (StaticData.MESSAGE_CHECK_DEVICE_RESP);
					// -------------------------------------------------------------
					break;
				// =================================================================
				case StaticData.SOCKET_MESSAGE_MONITOR:
					// -------------------------------------------------------------
					// 26/04/2016 ECU created to put this device into monitor mode so
					//                that key information will be sent to the sender
					//                of this message.
					//            ECU store the address where the monitored data is
					//                to be sent
					// 27/04/2016 ECU toggle the address if received from the same device
					// 28/04/2016 ECU get the state of monitoring that is contained in
					//                the message
					// 31/07/2016 ECU changed to use 'messageSender'
					// -------------------------------------------------------------
					String  localSendingAddress = messageSender;
					String  localDeviceName     = Utilities.GetDeviceName (localSendingAddress);
					boolean localMonitorState  	= (Boolean) Utilities.socketMessagesReadObject (context,theSocket);
					// -------------------------------------------------------------
					if (localMonitorState)
					{
						// ---------------------------------------------------------
						// 27/04/2016 ECU this device is not being monitored so
						//                switch on for the sending device
						// 28/04/2016 ECU changed to use the supplied state rather
						//                than using the monitorIPAddress
						// ---------------------------------------------------------
						PublicData.monitorIPAddress = localSendingAddress;
						// ---------------------------------------------------------
						// 27/04/2016 ECU tell the user what is going on
						// ----------------------------------------------------------
						MessageHandler.popToastAndSpeak (context.getString (R.string.device_will_be_monitored) + localDeviceName);
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 28/04/2016 ECU switch off the monitoring
						// ---------------------------------------------------------
						PublicData.monitorIPAddress = null;
						// ---------------------------------------------------------
						// 27/04/2016 ECU tell the user what has happened
						// ---------------------------------------------------------
						MessageHandler.popToastAndSpeak (context.getString (R.string.device_will_not_be_monitored) + localDeviceName);
						// -----------------------------------------------------
					}
					break;
				// =================================================================
				case StaticData.SOCKET_MESSAGE_MONITOR_DATA:
					// -------------------------------------------------------------
					// 26/04/2016 ECU created to received the data from a device that
					//                is being monitored
					// 28/04/2016 ECU added the sender's address as an argument
					// 31/07/2016 ECU changed to use 'messageSender'
					// -------------------------------------------------------------
					MonitorData.receiveMonitorData (context,
												    messageSender,
												    Utilities.socketMessagesReadObject (context,theSocket));
					// -------------------------------------------------------------
					break;
				// =================================================================
				case StaticData.SOCKET_MESSAGE_OBJECT:
					// -------------------------------------------------------------
					// 27/02/2016 ECU created to process incoming objects
					// -------------------------------------------------------------
					Utilities.ProcessTheObject (context,Utilities.socketMessagesReadObject (context,theSocket));
					// -------------------------------------------------------------
					break;
				// ================================================================= 
				case StaticData.SOCKET_MESSAGE_PLAY:
					// -------------------------------------------------------------
					// 19/08/2013 ECU select the destination file dependent on the incoming
					//				  data byte
					// 21/04/2015 ECU changed to use the strings in resources
					//            ECU changed to use static position
					// -------------------------------------------------------------
					if (Utilities.bitHandler (messageDetails [MESSAGE_DATA_POSITION], StaticData.SOCKET_DATA_FILE))
						fileHeader = context.getString (R.string.received_file_1);
					else
						fileHeader = context.getString (R.string.received_file_2);
					// -------------------------------------------------------------
					// 14/08/2013 ECU indicate that the play command is being processed
					// ------------------------------------------------------------- 
					playBeingProcessed = true;
					// -------------------------------------------------------------
					// 20/08/2013 ECU indicate PLAY command received so stop any timeouts
					// -------------------------------------------------------------
					PublicData.playTimeout = StaticData.NO_RESULT;
					// -------------------------------------------------------------
					// 14/08/2013 ECU set up the file to play
					// -------------------------------------------------------------
					String fileToPlay = PublicData.projectFolder + fileHeader + context.getString (R.string.temp_file_socket);
					// -------------------------------------------------------------
					// 10/08/2013 ECU increment the number of tracks played and include in the debug message
					// --------------------------------------------------------------
					PublicData.remoteTrackCounter++;
					// --------------------------------------------------------------
					Utilities.debugMessage(TAG,"file to play " + fileToPlay + 
							" Track Counter = " + PublicData.remoteTrackCounter);
					// -------------------------------------------------------------
					// 14/08/2013 ECU check that being asked to play the last good 
					//				  file that was received
					// 19/08/2013 ECU take the file name check out because file 
					//                toggling should not be an issue now
					// -------------------------------------------------------------
					Utilities.PlayAFile (context,fileToPlay);
					// -------------------------------------------------------------
					// 03/08/2013 ECU indicate that the file is being played
					// -------------------------------------------------------------
					PublicData.trackBeingPlayed = true;
					// -------------------------------------------------------------
					// 03/08/2013 ECU remember the IP address of the music server
					// 01/08/2016 ECU use 'messageSender'
					// -------------------------------------------------------------
					PublicData.musicServer = messageSender;	
					// -------------------------------------------------------------
					// 14/08/2013 ECU indicate that the play command has been processed
					// -------------------------------------------------------------
					playBeingProcessed = false;
					// -------------------------------------------------------------
					// 13/04/2015 ECU if track scrolling is on then handle
					//            ECU changed to use the method
					// 07/05/2016 ECU ensure that marquee text is always displayed
					// -------------------------------------------------------------
					// 13/04/2015 ECU try and get the name of the music server
					// -------------------------------------------------------------
					String deviceName = Utilities.GetDeviceName (PublicData.musicServer);
					// -------------------------------------------------------------
					// 13/04/2015 ECU if no name can be found then a 'null' is
					//                returned - in which case display the IP
					//                address
					// 07/05/2016 ECU add the 'true' flag to indicate that always want
					//                the marquee text displayed
					// -------------------------------------------------------------
					MusicPlayer.setMarqueeText (context.getString (R.string.received_from) + 
							((deviceName == null) ? PublicData.musicServer : deviceName) + " - " +
							TrackDetails.trackInformation (fileToPlay),true);
					// -------------------------------------------------------------
					// 02/05/2015 ECU indicate that the remote player is not
					//                available
					//            ECU use the method
					// -------------------------------------------------------------
					MusicPlayer.setStatus (true);
					// -------------------------------------------------------------
					break;
				/* ================================================================= */
				case StaticData.SOCKET_MESSAGE_PLAYED:
					// -------------------------------------------------------------
					// 03/08/2013 ECU indicate that the remote device has finished playing music
					// -------------------------------------------------------------
					if (!PublicData.sendingFile)
					{
						// ---------------------------------------------------------
						// 13/08/2013 ECU there is no file in transmission so can tell remote device
						//				  to play the next file
						// ---------------------------------------------------------
						PublicData.musicPlayerRemote = false;
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 13/08/2013 ECU the receiver has indicated that it has finished playing the last
						//                track but it has not received this file yet
						// ---------------------------------------------------------
						Utilities.debugMessage (TAG, "track played but still sending file");
						// ---------------------------------------------------------
					}
					break;
				// =================================================================
				case StaticData.SOCKET_MESSAGE_REQUEST_DETAILS:
					// -------------------------------------------------------------
					// 22/03/2015 ECU log the fact that message has been sent
					// 31/07/2016 ECU changed to use 'messageSender'
					// -------------------------------------------------------------
					Utilities.LogToProjectFile (TAG,"REQUEST_DETAILS message received from " + 
													messageSender);
					// -------------------------------------------------------------
					// 20/03/2015 ECU incoming message to request details of this 
					//                device which will be sent as an object
					// 21/03/2015 ECU pass through the message type as an argument
					// 31/07/2016 ECU changed to use 'messageSender'
					// -------------------------------------------------------------
					Utilities.sendSocketMessageSendTheObject(context,
															 messageSender,
															 PublicData.socketNumberForData,
															 StaticData.SOCKET_MESSAGE_SENT_DETAILS,
															 (Object) PublicData.localDeviceDetails);
					// -------------------------------------------------------------
					// 23/03/2015 ECU log the fact that message has been sent
					// 31/07/2016 ECU changed to use 'messageSender'
					// -------------------------------------------------------------
					Utilities.LogToProjectFile (TAG,"SENT_DETAILS message sent to " + 
													messageSender);
					// -------------------------------------------------------------
					break;
				// =================================================================
				case StaticData.SOCKET_MESSAGE_REQUEST_DETAILS_RAW:	
					// -------------------------------------------------------------
					// 20/01/2016 ECU send the device details directly as an object
					// -------------------------------------------------------------
					ObjectOutputStream objectOutputStream = new ObjectOutputStream (output);
					objectOutputStream.writeObject ((Object)PublicData.localDeviceDetails);
					// -------------------------------------------------------------
					// 20/01/2016 ECU flush out and close the stream
					// -------------------------------------------------------------
					objectOutputStream.flush ();
					objectOutputStream.close ();
					// -------------------------------------------------------------
					break;
				/* ================================================================= */
				case StaticData.SOCKET_MESSAGE_REQUEST_FILE:
					// -------------------------------------------------------------
					// 02/02/2014 ECU added to receive the 'FileDetails' object associated with a file
					// 31/01/2015 ECU changed the method name
					// 31/07/2016 ECU changed to use 'messageSender'
					// -------------------------------------------------------------
					FileDetails fileDetails = (FileDetails) Utilities.socketMessagesReadObject (context,theSocket);
					Utilities.HandleFileRequest (context,fileDetails,messageSender);
					// -------------------------------------------------------------
					break;
				// =================================================================
				case StaticData.SOCKET_MESSAGE_REQUEST_STATUS:
					// -------------------------------------------------------------
					// 02/05/2015 ECU created - a remote device is requesting that
					//                this device sends its current status object
					// -------------------------------------------------------------
					// 02/05/2015 ECU set the IP address as a check
					// -------------------------------------------------------------
					PublicData.status.IPAddress = PublicData.ipAddress;
					// -------------------------------------------------------------
					// 31/07/2016 ECU changed to use 'messageSender'
					// -------------------------------------------------------------
					Utilities.sendSocketMessageSendTheObject(context,
							 messageSender,
							 PublicData.socketNumberForData,
							 StaticData.SOCKET_MESSAGE_STATUS_RESPONSE,
							 (Object) PublicData.status);
					// -------------------------------------------------------------
					break;
				/* ================================================================= */
				case StaticData.SOCKET_MESSAGE_REQUESTED_FILE:
					// -------------------------------------------------------------
					// 02/02/2014 ECU added to receive the 'FileDetails' object associated with a file
					// 19/03/2014 ECU add the sender's IP address as argument
					// 31/01/2015 ECU changed the method name
					// 31/07/2016 ECU changed to use 'messageSender'
					// -------------------------------------------------------------
					FileDetails receivedFileDetails 
						= (FileDetails) Utilities.socketMessagesReadObject (context,theSocket);
					
					Utilities.HandleRequestedFile (context,receivedFileDetails,messageSender);
					
					break;
				// =================================================================
				case StaticData.SOCKET_MESSAGE_REQUESTED_FILE_ERROR:
					// -------------------------------------------------------------
					// 14/12/2016 ECU created to handle an error message from the cloner
					// -------------------------------------------------------------
					FileDetails receivedFileDetailsError 
						= (FileDetails) Utilities.socketMessagesReadObject (context,theSocket);
				
					Utilities.HandleRequestedFileError (context,receivedFileDetailsError,messageSender);
					// -------------------------------------------------------------
					break;
				/* ================================================================= */	
				case StaticData.SOCKET_MESSAGE_SEND_CHUNK:
					// -------------------------------------------------------------
					// 07/04/2014 ECU indicate chunk being received
					// 31/01/2015 ECU changed the method name
					// 31/07/2016 ECU changed to use 'messageSender'
					// -------------------------------------------------------------
					ChunkDetails localChunk = (ChunkDetails) Utilities.socketMessagesReadObject (context,theSocket);
					if (receiveFile != null)
						receiveFile.ProcessChunk (context,messageSender,localChunk);
					break;
				/* ================================================================= */
				case StaticData.SOCKET_MESSAGE_SEND_CHUNK_END:
					// -------------------------------------------------------------
					// 07/04/2014 ECU indicate chunk sending finished
					// -------------------------------------------------------------
					if (receiveFile != null)
					{
						receiveFile.Close();
						receiveFile = null;
					}
					// -------------------------------------------------------------
					break;
				/* ================================================================= */	
				case StaticData.SOCKET_MESSAGE_SEND_CHUNKS:
					// --------------------------------------------------------------
					// 07/04/2014 ECU indicate chunks will be received
					// 31/01/2015 ECU changed the method name
					// 31/07/2016 ECU changed to use 'messageSender'
					// -------------------------------------------------------------
					FileDetails chunkFileDetails 
						= (FileDetails) Utilities.socketMessagesReadObject (context,theSocket);
					receiveFile 
						= new ReceiveFile (context,messageSender,chunkFileDetails.GetFileName());
					break;		
				// =================================================================
				case StaticData.SOCKET_MESSAGE_SENT_DETAILS:
					// -------------------------------------------------------------
					// 22/03/2015 ECU log the fact that message has been sent
					// 31/07/2016 ECU changed to use 'messageSender'
					// -------------------------------------------------------------
					Utilities.LogToProjectFile (TAG,"SENT_DETAILS message received from " + 
													      messageSender);
					// -------------------------------------------------------------
					// 20/03/2015 ECU this message contains details of the sending
					//                device
					// -------------------------------------------------------------
					Devices localDevice = (Devices) Utilities.socketMessagesReadObject (context,theSocket);
					// -------------------------------------------------------------
					// 20/03/2015 ECU update the response to indicate 'received'
					// -------------------------------------------------------------
					localDevice.response = "received " + PublicData.dateTimeString;
					// -------------------------------------------------------------
					// 20/03/2015 ECU now process the received information
					// -------------------------------------------------------------
					Utilities.processDetailsReceived (context,localDevice);
					// -------------------------------------------------------------
					break;
				// =================================================================
				case StaticData.SOCKET_MESSAGE_SPEAK_A_PHRASE:
					// -------------------------------------------------------------
					// 28/01/2015 ECU speak the phrase passed through in the message
					// 31/01/2015 ECU changed the method name
					// -------------------------------------------------------------
					String phraseToSpeak 
						= (String) Utilities.socketMessagesReadObject (context,theSocket);
					Utilities.SpeakAPhrase(context, phraseToSpeak);
					break;
				/* ================================================================= */
				case StaticData.SOCKET_MESSAGE_START_STREAM:
					// -------------------------------------------------------------
					// 06/08/2013 ECU tells this receiving device to start audio streaming to the
					//			      sending device
					// 06/08/2013 ECU set the streaming destination to the sender of this message
					// 01/08/2016 ECU use 'messageSender'
					// -------------------------------------------------------------
					PublicData.streamingDestination = messageSender;
					// -------------------------------------------------------------
					// 06/08/2013 ECU want to start the streaming activity on this
					//                device - do from the timer service
					// -------------------------------------------------------------
					PublicData.startStreaming = true;
					// -------------------------------------------------------------
					break;
				// =================================================================
				case StaticData.SOCKET_MESSAGE_STATUS_RESPONSE:
					// -------------------------------------------------------------
					// 02/05/2015 ECU created to receive the status of a remote
					//                device
					// -------------------------------------------------------------
					PublicData.receivedStatus = (DeviceStatus) Utilities.socketMessagesReadObject (context,theSocket);
					// -------------------------------------------------------------
					break;
				// =================================================================
				case StaticData.SOCKET_MESSAGE_STOP_STREAM:
					// -------------------------------------------------------------					
					// 06/08/2013 ECU want to stop the streaming activity on this device 
					// -------------------------------------------------------------
					PublicData.stopStreaming = true;
					
					break;
				/* ================================================================= */	
				case StaticData.SOCKET_MESSAGE_STREAMING:
					// -------------------------------------------------------------
					// 31/07/2016 ECU Note - process audio data that is being stream
					//                       to this device
					// -------------------------------------------------------------
					@SuppressWarnings("deprecation")
					AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
				    		AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
				    		AudioTrack.getMinBufferSize(44100,AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT),
				    		AudioTrack.MODE_STREAM);
				    // -------------------------------------------------------------
					// 31/07/2016 ECU Note - start up the audio player
					// -------------------------------------------------------------
				    audioTrack.play();
			        // -------------------------------------------------------------
				    // 31/07/2016 ECU Note - pass through the data to be played
				    // 01/08/2016 ECU changd buffer name from 'theBuffer' and 'theNumber...'
				    // -------------------------------------------------------------
					while ((localNumberOfBytesRead = input.read (localBuffer, 0, 4096) ) > 0)
					{
						audioTrack.write (localBuffer, 0,localNumberOfBytesRead);   
					}
					// -------------------------------------------------------------
					// 05/08/2013 ECU free the audiotrack
					// -------------------------------------------------------------
					audioTrack.stop ();
			        audioTrack.release();
			        // -------------------------------------------------------------
					break;
				/* ================================================================= */
				case StaticData.SOCKET_MESSAGE_WAV_FILE:
					// -------------------------------------------------------------
					// 31/01/2015 ECU changed the method name
					// 01/08/2016 ECU changd buffer name from 'theBuffer' and 'theNumber...'
					// -------------------------------------------------------------
					localNumberOfBytesRead 
						= Utilities.socketMessagesReadIntoBuffer(context,theSocket,localBuffer,localBuffer.length);
					Utilities.playFromBuffer (context,localBuffer,localNumberOfBytesRead);
					break;
				/* ================================================================= */
				default:
					// -------------------------------------------------------------
					// 07/08/2013 ECU an invalid message type has been received
					// -------------------------------------------------------------
					Utilities.debugMessage (TAG,"invalid message type = " + typeOfMessage,true);
					// -------------------------------------------------------------
					// 07/08/2013 ECU want to indicate a bad message
					// -------------------------------------------------------------
					typeOfMessage = StaticData.NO_RESULT;
					// -------------------------------------------------------------
					// 07/08/2013 ECU try and recover depending on the last message 
					//                that was handled
					// -------------------------------------------------------------
					switch (lastGoodMessageType)
					{
						case StaticData.SOCKET_MESSAGE_FILE_CHUNKS:
							// -----------------------------------------------------
							// 07/08/2013 ECU try and get the file resent
							// -----------------------------------------------------
							// MainActivity.trackBeingPlayed = true;
							// ------------------------------------------------------
							break;
						default:
							break;	
					}
					break;
				/* ================================================================= */
					
			}
			// ---------------------------------------------------------------------
			// 07/08/2013 ECU remember the last good message handled
			// ---------------------------------------------------------------------
			if (typeOfMessage != StaticData.NO_RESULT)
			{
				lastGoodMessageType = typeOfMessage;
			}	
			// ---------------------------------------------------------------------
			// 03/08/2013 ECU close the streams associated with the socket
			// ---------------------------------------------------------------------
			input.close ();
			output.close ();
			// ----------------------------------------------------------------------
		}
		catch (IOException theException)
		{
		}
	}
	/* ============================================================================= */
}

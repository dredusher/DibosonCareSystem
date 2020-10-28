package com.usher.diboson;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

public class FileTransferUtilities 
{
	// =============================================================================
	// 09/01/2016 ECU created to hold methods that will be used in the file transfer
	//                process
	// 18/01/2016 ECU everything in this class was thoroughly tested and works
	//                logically but a major issue is that 'control' messages are
	//                being sent with a 'blockSize' of data which is not relevant.
	//				  Therefore decided to change to have to types of transfer block
	//                a 'control' block which has everything the same as a 'data'
	//                block but with a 'null' data buffer rather than the actual
	//                data buffer
	// =============================================================================
	
	// =============================================================================
	private static final int    FTP_DATA			= 0;
	private static final int    FTP_DATA_ACK		= 1;
	private static final int    FTP_DATA_END		= 2;
	private static final int    FTP_DATA_END_ACK	= 3;
	private static final int	FTP_DATA_NAK		= 4;
	private static final int	FTP_READY_TO_GO		= 5;
	private static final int	FTP_START			= 6;
	private static final int	FTP_START_ACK		= 7;
	private static final int	FTP_STOP			= 8;	
	// -----------------------------------------------------------------------------
	public  static final int	BLOCK_SIZE 			= (10 * StaticData.MTU);
	// =============================================================================
	
	// =============================================================================
	static int					blockSize			= BLOCK_SIZE;	// 17/01/2016 ECU added
	static FileTransferBlock	controlTransferBlock= null;			// 18/01/2016 ECU added
	static int					counterClient;
	static Message				counterMessage;						// 14/12/2016 ECU added
	static FileTransferBlock	dataTransferBlock	= null;			// 18/01/2016 ECU added
	static int					filePointer;
	static boolean				includeFile;
	static RandomAccessFile 	inputFile;
	static InputStream			inputStream;
	static List<File> 			listOfFiles;
	static File					localFileDetails;
	static String				localFileName       = null;			// 16/01/2016 ECU added
	static Message				message;
	static MessageHandler		messageHandler		= null;			// 05/01/2016 ECU added
	static int					numberOfBlocks;						// 16/12/2016 ECU added
	static ObjectInputStream	objectInputStream;
	static ObjectOutputStream 	objectOutputStream;
	static RandomAccessFile 	outputFile;
	static OutputStream 		outputStream;
	static ServerSocket 		serverSocket;
	static boolean				terminateAfterSend	= false;
	static int					totalBytesRead;
	static Socket				transferSocket;
	// =============================================================================

	// =============================================================================
	// S E R V E R
	// ===========
	// All methods associated with the server side of the transfer will be placed here
	// =============================================================================
	// =============================================================================
	public static void ServerInitialise (Context theContext,
										 String theClientIPAddress,
										 List<File> theFiles,
										 boolean theAllFilesFlag,
										 int theBlockSize)
	{
		// -------------------------------------------------------------------------
		// 09/01/2016 ECU created to initialise the server side of the transfer
		// 10/01/2016 ECU added 'theAllFilesFlag' which controls which files are
		//                to be updated on the client
		//                     true ......... the client is to update all files
		//                     false ........ the client is only to update files which
		//                                    do not exists or which have a modification
		//                                    date/time earlier than the file being
		//                                    transferred
		// 17/01/2016 ECU changed to use blockSize
		// -------------------------------------------------------------------------
		dataTransferBlock = new FileTransferBlock (PublicData.ipAddress,theBlockSize,theAllFilesFlag);
		// -------------------------------------------------------------------------
		// 18/01/2016 ECU create the control block
		// -------------------------------------------------------------------------
		controlTransferBlock = new FileTransferBlock (PublicData.ipAddress,theAllFilesFlag);
		// -------------------------------------------------------------------------
		// 18/01/2016 ECU store the number of files being transferred
		// -------------------------------------------------------------------------
		controlTransferBlock.numberOfFiles 	= theFiles.size();
		dataTransferBlock.numberOfFiles		= theFiles.size();
		// -------------------------------------------------------------------------
		// 11/01/2016 ECU send the block with information in
		// 18/01/2016 ECU changed to send the control block
		// -------------------------------------------------------------------------
		Utilities.sendSocketMessageSendTheObject (theContext,
												 theClientIPAddress,
												 PublicData.socketNumberForData, 
												 StaticData.SOCKET_MESSAGE_FILE_TRANSFER,
												 (Object) controlTransferBlock);
		// -------------------------------------------------------------------------
		// 09/01/2016 ECU start up the server socket to receive the transfer
		// -------------------------------------------------------------------------
		try
		{
			serverSocket = new ServerSocket (PublicData.socketNumberForFTP);
			// ---------------------------------------------------------------------
			// 09/01/2016 ECU try and accept an incoming client connection
			// ---------------------------------------------------------------------
			Socket clientSocket = serverSocket.accept ();
			// ---------------------------------------------------------------------
			// 09/01/2016 ECU store the streams used in the transfer
			// ---------------------------------------------------------------------
			inputStream  = clientSocket.getInputStream ();
			outputStream = clientSocket.getOutputStream ();
			// ---------------------------------------------------------------------
			// 09/01/2016 ECU set up the object streams
			//            ECU very important to define the out stream first otherwise
			//                can get a deadlock situation
			// ---------------------------------------------------------------------
			objectOutputStream = new ObjectOutputStream (outputStream);
			objectInputStream  = new ObjectInputStream 	(inputStream);
			// ---------------------------------------------------------------------
			// 11/01/2016 ECU create the transfer block that will be used
			// ---------------------------------------------------------------------

			// ---------------------------------------------------------------------
			// 05/01/2016 ECU created the handler of messages
			// ---------------------------------------------------------------------
			messageHandler = new MessageHandler ();
			// ---------------------------------------------------------------------
			// 09/01/2016 ECU initialise any pointers
			// 17/01/2016 ECU added the block size
			// ---------------------------------------------------------------------
			blockSize			= theBlockSize;			
			filePointer			= 0;
			listOfFiles			= theFiles;
			terminateAfterSend	= false;
			// --------------------------------------------------------------------
			// 09/01/2016 ECU want to pick up the first message from the client
			// ---------------------------------------------------------------------
			messageHandler.sendEmptyMessage (StaticData.MESSAGE_RECEIVE);
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
		}
	}
	// =============================================================================
	static void ServerDisplayMessage (String theDisplayMessage)
	{
		// -------------------------------------------------------------------------
		// 16/01/2016 ECU created to handle the new master mathod of this name
		// -------------------------------------------------------------------------
		ServerDisplayMessage (theDisplayMessage,StaticData.NO_RESULT,StaticData.NO_RESULT);
	}
	// -----------------------------------------------------------------------------
	static void ServerDisplayMessage (String theDisplayMessage,int theCurrentFile,int theNumberOfFiles)
	{
		//--------------------------------------------------------------------------
		// 15/01/2016 ECU created to display a message on the screen
		// 16/01/2016 ECU renamed from DisplayMessage
		//            ECU include the file pointer and size of files as arguments
		// 14/12/2016 ECU changed to use NEWLINE
		// -------------------------------------------------------------------------
		Message localMessage = ClonerActivity.fileRefreshHandler.obtainMessage
				(StaticData.MESSAGE_DISPLAY,theDisplayMessage + StaticData.NEWLINE);
		// -------------------------------------------------------------------------
		// 16/01/2016 ECU provide details of the file processing status
		// -------------------------------------------------------------------------
		localMessage.arg1 = theCurrentFile;
		localMessage.arg2 = theNumberOfFiles;
		// -------------------------------------------------------------------------
		ClonerActivity.fileRefreshHandler.sendMessage (localMessage);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void ServerTerminate ()
	{
		// -------------------------------------------------------------------------
		// 09/01/2016 ECU created to close everything down after transfer is complete
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 09/01/2016 ECU close the streams and the socket
			// ---------------------------------------------------------------------
			objectInputStream.close();
			objectOutputStream.close();
			inputStream.close();
			outputStream.close();
			// ---------------------------------------------------------------------
			serverSocket.close();
			// ---------------------------------------------------------------------
			// 16/01/2016 ECU reflect the termination on the display
			// ---------------------------------------------------------------------
			ClonerActivity.fileRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_FINISH);
			// ---------------------------------------------------------------------
		} 
		catch (Exception theException) 
		{
		}
	}
	// =============================================================================
	
	
	// =============================================================================
	// C L I E N T
	// ===========
	// All methods associated with the client side of the transfer will be placed here
	// =============================================================================
	// =============================================================================
	public static void ClientInitialise (Context theContext,FileTransferBlock theTransferBlock)
	{
		// -------------------------------------------------------------------------
		// 09/01/2016 ECU try and establish a link with the server
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 11/01/2016 ECU store the incoming transfer block
			// 18/01/2016 ECU the block coming in is the control block
			// ---------------------------------------------------------------------
			controlTransferBlock = theTransferBlock;
			// ---------------------------------------------------------------------
			InetAddress serverAddress = InetAddress.getByName (controlTransferBlock.serverAddress);    
			transferSocket = new Socket (serverAddress,PublicData.socketNumberForFTP);	
			// ---------------------------------------------------------------------
			// 09/01/2016 ECU store the streams used in the transfer
			// ---------------------------------------------------------------------
			inputStream  = transferSocket.getInputStream ();
			outputStream = transferSocket.getOutputStream ();
			// ---------------------------------------------------------------------
			// 09/01/2016 ECU set up the object streams
			//            ECU very important to define the out stream first otherwise
			//                can get a deadlock situation
			// ---------------------------------------------------------------------
			objectOutputStream 	= new ObjectOutputStream (outputStream);
			objectInputStream 	= new ObjectInputStream  (inputStream);
			// ---------------------------------------------------------------------
			// 09/01/2016 ECU initialise any flags
			// ---------------------------------------------------------------------
			filePointer = 0;
			// ---------------------------------------------------------------------
			// 09/01/2016 ECU created the handler of messages
			// ---------------------------------------------------------------------
			messageHandler = new MessageHandler ();
			// ---------------------------------------------------------------------
			SendTransferBlock (FTP_READY_TO_GO,controlTransferBlock);
			// ---------------------------------------------------------------------
			// 16/01/2016 ECU try and start up the clone activity so that information
			//                can be displayed
			// ---------------------------------------------------------------------
			Intent localIntent = new Intent (theContext,CloneActivity.class);
			localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			theContext.startActivity (localIntent);
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void ClientDisplayMessage (String theDisplayMessage)
	{
		// -------------------------------------------------------------------------
		// 16/01/2016 ECU created to handle the new master mathod of this name
		// -------------------------------------------------------------------------
		ClientDisplayMessage (theDisplayMessage,StaticData.NO_RESULT,StaticData.NO_RESULT);
	}
	// -----------------------------------------------------------------------------
	static void ClientDisplayMessage (String theDisplayMessage,int theCurrentFile,int theNumberOfFiles)
	{
		//--------------------------------------------------------------------------
		// 15/01/2016 ECU created to display a message on the screen
		// 16/01/2016 ECU renamed from DisplayMessage
		//            ECU include the file pointer and size of files as arguments
		// -------------------------------------------------------------------------
		if (CloneActivity.cloneRefreshHandler != null)
		{
			Message localMessage = CloneActivity.cloneRefreshHandler.obtainMessage
					(StaticData.MESSAGE_DISPLAY,theDisplayMessage + "\n");
			// ---------------------------------------------------------------------
			// 16/01/2016 ECU provide details of the file processing status
			// ---------------------------------------------------------------------
			localMessage.arg1 = theCurrentFile;
			localMessage.arg2 = theNumberOfFiles;
			// ---------------------------------------------------------------------
			CloneActivity.cloneRefreshHandler.sendMessage(localMessage);
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void ClientTerminate ()
	{
		// -------------------------------------------------------------------------
		// 09/01/2016 ECU created to close everything down after transfer is complete
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 09/01/2016 ECU close the streams and the socket
			// ---------------------------------------------------------------------
			objectInputStream.close ();
			objectOutputStream.close ();
			inputStream.close ();
			outputStream.close ();
			// ---------------------------------------------------------------------
			// 09/01/2016 ECU and close the socket
			// ---------------------------------------------------------------------
			transferSocket.close();
			// ---------------------------------------------------------------------
			// 16/01/2016 ECU reflect the termination on the display
			// ---------------------------------------------------------------------
			CloneActivity.cloneRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_FINISH);
			// ---------------------------------------------------------------------
		} 
		catch (Exception theException) 
		{
		}
	}
	// =============================================================================
	
	// =============================================================================
	static void ProcessTransferBlock (FileTransferBlock theTransferBlock)
	{
		// -------------------------------------------------------------------------
		// 09/01/2016 ECU created to process incoming messages
		// 18/01/2016 ECU changed to feed through the transfer block as an argument
		// -------------------------------------------------------------------------
		switch (theTransferBlock.type)
		{
			// ---------------------------------------------------------------------
			case FTP_DATA:
				// -----------------------------------------------------------------
				// 09/01/2016 ECU created to handle an incoming data in a transfer
				//                block
				// 11/01/2016 ECU changed to use 'sizeOfBuffer'
				// -----------------------------------------------------------------
				// 11/01/2016 ECU at this point the counter in the block should match
				//                the counterClient
				// -----------------------------------------------------------------
				if (theTransferBlock.counter == counterClient)
				{
					// -------------------------------------------------------------
					// 12/01/2016 ECU this block is in sequence so accept it
					// -------------------------------------------------------------
					totalBytesRead += theTransferBlock.sizeOfBuffer;
					counterClient++;
					// -------------------------------------------------------------
					// 10/01/2016 ECU write the data to file
					// -------------------------------------------------------------
					try
					{
						// ---------------------------------------------------------
						// 10/01/2016 ECU write the received block to the file
						// ---------------------------------------------------------
						outputFile.write (theTransferBlock.buffer,0,theTransferBlock.sizeOfBuffer);
						// ---------------------------------------------------------
						SendTransferBlock (FTP_DATA_ACK,controlTransferBlock);
					}
					catch (Exception theException)
					{
						// ---------------------------------------------------------
						// 12/01/2016 ECU indicate that an error happened
						// ---------------------------------------------------------
						TransmissionError ();
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 12/01/2016 ECU handle a bad block
					// -------------------------------------------------------------
					TransmissionError ();
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case FTP_DATA_ACK:
				// -----------------------------------------------------------------
				// 09/01/2016 ECU this is the ack from a client that the data has been
				//                received 
				// 11/01/2016 ECU changed to reflect new style transfer block
				// -----------------------------------------------------------------
				try
				{
					// -------------------------------------------------------------
					// 11/01/2016 ECU increment the block counter
					// 18/01/2016 ECU changed to use the data block
					// -------------------------------------------------------------
					dataTransferBlock.counter++;
					// -------------------------------------------------------------
					// 14/12/2016 ECU send message to display the updated block counter.
					//                The final NO_RESULT is not used.
					// 16/12/2016 ECU use the second argument to pass across the
					//                number of blocks in this file, but only if
					//                there are more than 0. Add 1 to the argument
					//                because the last block will be a partial one
					// -------------------------------------------------------------
					if (numberOfBlocks > 0)
					{
						counterMessage = ClonerActivity.fileRefreshHandler.obtainMessage
											(StaticData.MESSAGE_DISPLAY_COUNTER,dataTransferBlock.counter,(numberOfBlocks + 1));
						ClonerActivity.fileRefreshHandler.sendMessage (counterMessage);
					}
					// -------------------------------------------------------------
					// 11/01/2016 ECU read the next block from the file
					// -------------------------------------------------------------
					dataTransferBlock.sizeOfBuffer = inputFile.read (dataTransferBlock.buffer);
					// -------------------------------------------------------------
					// 10/01/2016 ECU check if the last block of the file has been
					//                read
					// -------------------------------------------------------------
					if (dataTransferBlock.sizeOfBuffer == blockSize)
					{
						SendTransferBlock (FTP_DATA,dataTransferBlock);
					}
					else
					{
						SendTransferBlock (FTP_DATA_END,dataTransferBlock);
					}
					// -------------------------------------------------------------
				}
				catch (Exception theException)
				{
					// -------------------------------------------------------------
					// 14/01/2016 ECU if an IO error occurs then log the fact so that
					//                can decide what is the best thing to do
					// -------------------------------------------------------------
					Utilities.LogToProjectFile ("FTP_DATA_ACK","Exception : " + theException);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case FTP_DATA_END:
				// -----------------------------------------------------------------
				// 09/01/2016 ECU this is the ack from a client that the data has been
				//                received and it is the last block
				// 11/01/2016 ECU changed to use the size in the block
				// -----------------------------------------------------------------
				// 11/01/2016 ECU at this point the counter in the block should match
				//                the counterClient
				// -----------------------------------------------------------------
				if (theTransferBlock.counter == counterClient)
				{
					totalBytesRead += theTransferBlock.sizeOfBuffer;
					counterClient++;
					// -------------------------------------------------------------
					// 10/01/2016 ECU write the data to file
					// -------------------------------------------------------------
					try
					{
						// ---------------------------------------------------------
						// 14/01/2016 ECU put in the check in case a zero-length file
						//                is being transferred
						// ---------------------------------------------------------
						if (theTransferBlock.sizeOfBuffer > 0)
							outputFile.write (theTransferBlock.buffer,0,theTransferBlock.sizeOfBuffer);
						// ---------------------------------------------------------
						// 14/01/2016 ECU Note - transfer of the file is complete so
						//                close the output stream
						// ---------------------------------------------------------
						outputFile.close ();
						// ---------------------------------------------------------
						// 14/01/2016 ECU Note - tell the server that the final block
						//                has been received
						// 18/01/2016 ECU change to use the control block
						// ---------------------------------------------------------
						SendTransferBlock (FTP_DATA_END_ACK,controlTransferBlock);
						// ---------------------------------------------------------
						// 10/01/2016 ECU this should be the end of the current file being
						//                sent
						// ---------------------------------------------------------
					}
					catch (Exception theException)
					{
						// ---------------------------------------------------------
						// 12/01/2016 ECU indicate that an error happened
						// ---------------------------------------------------------
						TransmissionError ();
						// ---------------------------------------------------------
					}
				}
				else
				{
					// -------------------------------------------------------------
					// 12/01/2016 ECU handle a bad block
					// -------------------------------------------------------------
					TransmissionError ();
					// -------------------------------------------------------------
				}				
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case FTP_DATA_END_ACK:
				// -----------------------------------------------------------------
				// 09/01/2016 ECU received by the server to indicate that the client
				//                has received the last block
				// -----------------------------------------------------------------
				try 
				{
					inputFile.close();
				} 
				catch (Exception theException) 
				{
				}
				// -----------------------------------------------------------------
				// 10/01/2016 ECU decide whether there are more files to be sent
				// -----------------------------------------------------------------
				filePointer++;
				// -----------------------------------------------------------------
				if (filePointer >= listOfFiles.size())
				{
					// -------------------------------------------------------------
					// 10/01/2016 ECU all files have been processed so tell the client
					//                to finish
					// 18/01/2016 ECU changed to use the control block
					// -------------------------------------------------------------
					SendTransferBlock (FTP_STOP,controlTransferBlock);
					// -------------------------------------------------------------
					// 1001/2016 ECU close down the server side of the transfer
					//                after it has been transmitted
					// -------------------------------------------------------------
					terminateAfterSend = true;
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 10/01/2016 ECU have the next file to send
					// -------------------------------------------------------------
					SendFileDetails ();
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case FTP_DATA_NAK:
				// -----------------------------------------------------------------
				// 12/01/2016 ECU the client has indicated that an error occurred
				//                with the last block sent so want to resend the
				//                current file which is indicated by the value
				//                in 'filePointer'
				// -----------------------------------------------------------------
				// 12/01/2016 ECU first of all close the input file
				// -----------------------------------------------------------------
				try 
				{
					inputFile.close();
				} 
				catch (Exception theException) 
				{
				}
				// -----------------------------------------------------------------
				// 12/01/2016 ECU now re-send the details of the existing file
				// -----------------------------------------------------------------
				SendFileDetails ();
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case FTP_READY_TO_GO:
				// -----------------------------------------------------------------
				// 09/01/2016 ECU the client is ready so send the file details
				// -----------------------------------------------------------------
				// 09/01/2016 ECU get a file from the list
				// 10/01/2016 ECU changed to use method which contains the code that
				//                was here
				// -----------------------------------------------------------------
				SendFileDetails ();
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case FTP_START:
				// -----------------------------------------------------------------
				// 09/01/2016 ECU this message indicates that a file is to be
				//                transfered - the stored object is the File object
				// 11/01/2016 ECU changed to use the 'fileDetails' in block
				// -----------------------------------------------------------------
				// 10/01/2016 ECU decide if want this file
				// -----------------------------------------------------------------
				try
				{
					// -------------------------------------------------------------
					// 10/01/2016 ECU decide whether this file is to be handled
					//            ECU changed to use flag in the object 
					// -------------------------------------------------------------
					// 14/01/2016 ECU the file details being sent will be based on
					//                the project folder which could be different
					//                to the client and therefore cause errors so
					//                try and adjust accordingly
					// -------------------------------------------------------------
					if (theTransferBlock.projectFolder.equalsIgnoreCase(PublicData.projectFolder))
					{
						// ---------------------------------------------------------
						// 14/01/2016 ECU the project folder is the same on server
						//                and client 
						// ---------------------------------------------------------
						localFileDetails = new File (theTransferBlock.fileDetails.getAbsolutePath());
					}
					else
					{
						
						// ---------------------------------------------------------
						// 14/01/2016 ECU the project folder is different so need to
						//                adjust accordingly
						// ---------------------------------------------------------
						// 14/01/2016 ECU the project folder needs to be sorted out
						// ---------------------------------------------------------
						localFileName = theTransferBlock.fileDetails.getAbsolutePath();
						localFileName = PublicData.projectFolder + localFileName.replace (theTransferBlock.projectFolder, StaticData.BLANK_STRING);
						// ---------------------------------------------------------
						localFileDetails = new File (localFileName);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 16/01/2016 ECU decide whether the file is to be requested
					// -------------------------------------------------------------
					if (theTransferBlock.allFiles)
					{
						includeFile = true;
					}
					else
					{
						// ---------------------------------------------------------
						if (!localFileDetails.exists() || 
							theTransferBlock.fileDetails.lastModified() > localFileDetails.lastModified())
						{
							// -----------------------------------------------------
							// 17/01/2016 ECU make sure all of the path is created
							// -----------------------------------------------------
							if (!localFileDetails.exists())
							{
								// -------------------------------------------------
								// 17/01/2016 ECU try and create all directories in
								//                the parent if it does not exist
								//            ECU the following method will return
								//                'false' if the directory already
								//                exists or 'true' if it is created
								//                correctly - so don't need to
								//                check in advance as it does not
								//                throw an exception
								// -------------------------------------------------
								localFileDetails.getParentFile().mkdirs();
								// ------------------------------------------------
							}
							// -----------------------------------------------------
							includeFile = true;
						}
						else
						{
							includeFile = false;
						}
					}
					// -------------------------------------------------------------
					// 10/01/2016 ECU handle whether the file is to be included
					// -------------------------------------------------------------
					if (includeFile)
					{
						// ---------------------------------------------------------
						// 10/01/2016 ECU this is a file to be transferred
						// 17/01/2016 ECU changed from transferBlock.fileDetails
						//                to localFileDetails which was causing a
						//                problem when the project folder was
						//                different on server and client
						// 18/01/2016 ECU change to use the control block
						// ---------------------------------------------------------
						outputFile = new RandomAccessFile (localFileDetails, "rw");
						outputFile.setLength (0);
						SendTransferBlock (FTP_START_ACK,controlTransferBlock);
						// ---------------------------------------------------------
						// 11/01/2016 ECU initialise variables relevant to the file
						// ---------------------------------------------------------
						totalBytesRead = 0;
						counterClient  = 0;
						// ---------------------------------------------------------
						// 16/01/2016 ECU update the display
						// 17/01/2016 ECU display the absolute path with the project
						//                folder removed
						// ---------------------------------------------------------
						ClientDisplayMessage ("requested ---- " + 
											  localFileDetails.getAbsolutePath().replace (PublicData.projectFolder, StaticData.BLANK_STRING),
											  filePointer,
											  theTransferBlock.numberOfFiles);
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 10/01/2016 ECU this file is not to be transferred
						// 18/01/2016 ECU changed to use the control block
						// ---------------------------------------------------------
						SendTransferBlock (FTP_DATA_END_ACK,controlTransferBlock);
						// ---------------------------------------------------------
						// 17/01/2016 ECU update the display
						// 17/01/2016 ECU display the absolute path with the project
						//                folder removed
						// ---------------------------------------------------------
						ClientDisplayMessage ("not needed --- " + 
											  localFileDetails.getAbsolutePath().replace (PublicData.projectFolder, StaticData.BLANK_STRING),
											  filePointer,
											  theTransferBlock.numberOfFiles);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 18/01/2016 ECU increment the file pointer
					// -------------------------------------------------------------
					filePointer++;
					// -------------------------------------------------------------
				}
				catch (Exception theException)
				{
					ClientDisplayMessage ("Exception --- " + theException);
				}
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case FTP_START_ACK:
				// -----------------------------------------------------------------
				// 09/01/2016 ECU sent by the client to indicate that file data can be
				//                transferred
				// -----------------------------------------------------------------
				try
				{
					// -------------------------------------------------------------
					// 10/01/2016 ECU initialise the blocks counter
					// -------------------------------------------------------------
					dataTransferBlock.counter = 0;
					// -------------------------------------------------------------
					// 11/01/2016 ECU read in a block of data
					// -------------------------------------------------------------
					dataTransferBlock.sizeOfBuffer = inputFile.read (dataTransferBlock.buffer);
					// -------------------------------------------------------------
					// 10/01/2016 ECU check if the whole file fits into the first
					//                block. It is the same code as at FTP_DATA_ACK
					//                but don't want to create a method
					// 17/01/2016 ECU change to use 'blockSize'
					// -------------------------------------------------------------
					if (dataTransferBlock.sizeOfBuffer == blockSize)
					{
						SendTransferBlock (FTP_DATA,dataTransferBlock);
					}
					else
					{
						SendTransferBlock (FTP_DATA_END,dataTransferBlock);
					}
					// -------------------------------------------------------------
				}
				catch (Exception theException)
				{
				}
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case FTP_STOP:
				// -----------------------------------------------------------------
				// 10/01/2016 ECU incoming message from the server to tell this client
				//                to finish
				// -----------------------------------------------------------------
				ClientTerminate ();
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void SendFileDetails ()
	{
		// -------------------------------------------------------------------------
		// 10/01/2016 ECU created as method to send the details of the file being
		//                pointed at
		// 18/01/2016 ECU changed to store the data in the 'control' block
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 09/01/2016 ECU get the relevant file details from the list
			// ---------------------------------------------------------------------
			controlTransferBlock.fileDetails = listOfFiles.get (filePointer);
			// ---------------------------------------------------------------------
			// 14/01/2016 ECU set the project folder to the arguments
			// ---------------------------------------------------------------------
			controlTransferBlock.projectFolder = PublicData.projectFolder;
			// ---------------------------------------------------------------------
			// 10/01/2016 ECU open up the file that is to be transfer
			// ---------------------------------------------------------------------
			inputFile   = new RandomAccessFile (controlTransferBlock.fileDetails, "r");
			// ---------------------------------------------------------------------
			// 16/12/2016 ECU get the size of the file and work out how many blocks
			//                this file contains
			// ---------------------------------------------------------------------
			numberOfBlocks = (int) (inputFile.length() / (long) blockSize);
			// ---------------------------------------------------------------------
			// 09/01/2016 ECU send these details to the client
			// ---------------------------------------------------------------------
			SendTransferBlock (FTP_START,controlTransferBlock);
			// ---------------------------------------------------------------------
			// 16/01/2016 ECU update the displayed information
			// 17/01/2016 ECU changed to display the absolute path without the
			//                leading project folder
			// 19/01/2016 ECU just tidied up the message by removing "details ----"
			// 16/11/2016 ECU changed to use the new method
			// ---------------------------------------------------------------------
			ServerDisplayMessage (Utilities.getRelativeFileName (controlTransferBlock.fileDetails.getAbsolutePath()),
			                      filePointer,
			                      listOfFiles.size());
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{	
		}
	}
	// =============================================================================
	static void SendTransferBlock (int theType,FileTransferBlock theFileTransferBlock)
	{
		// -------------------------------------------------------------------------
		// 09/01/2016 ECU create to send the transfer block via the message handler
		// 10/01/2016 ECU include the 'all files' flag
		// 11/01/2016 ECU change to handle the new style transfer block
		// -------------------------------------------------------------------------
		theFileTransferBlock.type 		= theType;
		// -------------------------------------------------------------------------
		// 09/01/2016 ECU send the block as an object
		// 11/01/2015 ECU as the object is already in the transfer block then just
		//                send an empty message
		// 18/01/2016 ECU reinstate the block as an argument
		// -------------------------------------------------------------------------
		message = messageHandler.obtainMessage (StaticData.MESSAGE_SEND,theFileTransferBlock);
		messageHandler.sendMessage (message);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void TransmissionError ()
	{
		// -------------------------------------------------------------------------
		// 12/01/2016 ECU created to handle errors on receiving a block which could
		//                be caused by :-
		//                  1) the client counter differs from the counter in the 
		//                     block
		//                  2) an exception occurred when writing the block to
		//                     disc
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 12/01/2016 ECU try and close the output stream
			// ---------------------------------------------------------------------
			outputFile.close();
			// ---------------------------------------------------------------------
			// 12/01/2016 ECU try and delete the input file
			// ---------------------------------------------------------------------
			dataTransferBlock.fileDetails.delete();
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			
		}
		// -------------------------------------------------------------------------
		// 12/01/2016 ECU tell the server of the problem
		// 18/01/2016 ECU change to send the control block
		// -------------------------------------------------------------------------
		SendTransferBlock (FTP_DATA_NAK,controlTransferBlock);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
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
        		case StaticData.MESSAGE_FINISH:
        			// -------------------------------------------------------------
        			// 10/01/2016 ECU a local message to get the server terminated
        			// -------------------------------------------------------------
        			ServerTerminate ();
        			// -------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_SEND:
        			try
        			{
        				// ---------------------------------------------------------
        				// 09/01/2016 ECU transmit this block
        				// 18/01/2016 ECU the block to be send is supplied as the
        				//                object in the message
        				// ---------------------------------------------------------
        				objectOutputStream.writeObject (theMessage.obj);
        				objectOutputStream.flush ();
        				// ---------------------------------------------------------
        				// 14/01/2016 ECU was having a problem with memory being
        				//                'eaten up' as objects were being written
        				//                to the stream. After some research on the
        				//                various forums it appears that the 'reset'
        				//                command may help
        				// ---------------------------------------------------------
        				objectOutputStream.reset ();
        				// ---------------------------------------------------------
        				// 09/01/2016 ECU ack expected so wait a bit before
        				//                trying to read it
        				// ---------------------------------------------------------
        				if (!terminateAfterSend)
        				{
        					// -----------------------------------------------------
        					// 10/01/2016 ECU wait for a response in the normal way
        					// -----------------------------------------------------
        					waitForResponse (20);
        					// -----------------------------------------------------
        				}
        				else
        				{
        					// -----------------------------------------------------
        					// 10/01/2016 ECU an FTP_STOP has been sent to tell the
        					//                client to stop - wait a while before
        					//                terminating the server side
        					// -----------------------------------------------------
        					sendMessageDelayed (obtainMessage (StaticData.MESSAGE_FINISH),1000);
        					// -----------------------------------------------------
        				}
        				// ---------------------------------------------------------
        			}
        			catch (Exception theException)
        			{	
        			}
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_RECEIVE:
        			// -------------------------------------------------------------
        			// 09/01/2016 ECU called when a message is to be received
        			// -------------------------------------------------------------
        			try
        			{
         				// ---------------------------------------------------------
        				// 09/01/2016 ECU process the received block
        				// 18/01/2016 ECU pass the received block through to be processed
        				// ---------------------------------------------------------
        				ProcessTransferBlock ((FileTransferBlock) objectInputStream.readObject ());
        				// ---------------------------------------------------------
        			}
        			catch (Exception theException)
        			{	
        			}
        			break;
        		// -----------------------------------------------------------------
        	}
        	// ---------------------------------------------------------------------
        }
        /* ------------------------------------------------------------------------- */
        public void waitForResponse (long delayMillis)
        {	
        	// ---------------------------------------------------------------------
        	// 06/01/2016 ECU changed to use MESSAGE_SLEEP instead of 0
        	// ---------------------------------------------------------------------
            this.removeMessages (StaticData.MESSAGE_RECEIVE);
            sendMessageDelayed(obtainMessage (StaticData.MESSAGE_RECEIVE), delayMillis);
        }
        // -------------------------------------------------------------------------
    };
	// =============================================================================
}

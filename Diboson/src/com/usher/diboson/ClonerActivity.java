package com.usher.diboson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ClonerActivity extends DibosonActivity 
{
	/* ============================================================================= */
	// 19/03/2014 ECU created
	// 03/05/2015 ECU changed the whole structure to use preference dialogue methods and
	//           	  the use of the device's 'status' to check modes - hopefully makes
	//            	  the logic much clear
	// 09/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 16/01/2016 ECU added the FTP mode and its handling
	// -----------------------------------------------------------------------------
	//final static String TAG = "ClonerActivity";
	// -----------------------------------------------------------------------------
	final static int	ACK_DELAY 		= 100;
	final static int	ACK_TIMEOUT     = (30 * 1000) / ACK_DELAY;
															// 20/03/2014 ECU timeout
	// -----------------------------------------------------------------------------
	public static boolean 		cloneMode		 	 = false;      
															// 06/04/2014 ECU added
															//      false = not in clone mode
															//      true  = in clone mode
	public static CloningDetails 
								cloningDetails;			// 09/01/2015 ECU added
	public static FileRefreshHandler 	
								fileRefreshHandler; 
	// -----------------------------------------------------------------------------
	static	int					ackCounter 			= 0;			// 20/03/2014 ECU added
	static  boolean             allFilesFlag		= false;		// 17/01/2016 ECU added
	static  int					blockSize			= FileTransferUtilities.BLOCK_SIZE;
																	// 17/01/2016 ECU added
	static  int					cloneDeviceAPI		= StaticData.NO_RESULT;	
																	// 07/01/2016 ECU added
	static	Button   			cloneFiles;
	static 	boolean             cloningInProgress	= false;
	static 	Context				context;							// 08/01/2015 ECU added
	static 	String []			devices				= null;			// 03/05/2015 ECU added
	static	boolean				exitActivity		= false;		// 03/05/2015 ECU added
	static	int					filePointer 		= 0;
	static 	TextView   			filesList;
	static	TextView			fileStatus;
	static	boolean				ftpMode				= false;		// 15/01/2016 ECU added
			static String		IPaddress;
			String []           IPAddresses;
			static List<File> 	listOfFiles;
	/* ============================================================================= */

	// =============================================================================
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_cloner_activity);
			// ---------------------------------------------------------------------
			// 08/01/2015 ECU save the current context
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 16/01/2016 ECU reset the ftp mode
			// ---------------------------------------------------------------------
			ftpMode = false;
			// ---------------------------------------------------------------------
			// 16/01/2016 ECU reset other variables
			// ---------------------------------------------------------------------
			cloningInProgress = false;
			exitActivity 	  = false;
			// ---------------------------------------------------------------------
			// 15/01/2016 ECU declare the message handler
			// ---------------------------------------------------------------------
			fileRefreshHandler = new FileRefreshHandler ();
			// ---------------------------------------------------------------------
			// 21/03/2014 ECU set the various standard aspects of the activity
			// 14/10/2014 ECU changed to keep screen on during the activity
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity	(this,!StaticData.ACTIVITY_FULL_SCREEN,StaticData.ACTIVITY_SCREEN_ON);
			// ---------------------------------------------------------------------
			// 19/03/2014 ECU set up the views in the appropriate modes
			// ---------------------------------------------------------------------
			cloneFiles 			= (Button)   findViewById (R.id.button_clone_files);
			filesList 			= (TextView) findViewById (R.id.file_list);	
			fileStatus 			= (TextView) findViewById (R.id.file_summary);	
			// ---------------------------------------------------------------------
			// 03/05/2015 ECU default the 'clone' button to be invisible
			// ---------------------------------------------------------------------
			cloneFiles.setVisibility (View.INVISIBLE);
			// ---------------------------------------------------------------------
			// 20/03/2014 ECU indicate what will be displayed in the status window
			// 14/12/2016 ECU changed to use BLANK_STRING
			// ---------------------------------------------------------------------
			filesList.setText (StaticData.BLANK_STRING);
			filesList.setHint (R.string.file_list_hint);
			// ---------------------------------------------------------------------
			// 07/01/2016 ECU make sure the clone file size is set - if this is a
			//                new device
			// ---------------------------------------------------------------------
			if (PublicData.storedData.cloneFileSize == 0)
			{
				PublicData.storedData.cloneFileSize = StaticData.CLONE_FILE_SIZE;
			}
			// ---------------------------------------------------------------------
			// 03/05/2015 ECU get the list of devices, excluding this one
			// ---------------------------------------------------------------------
			devices = Utilities.deviceListAsArray (false);
			// ---------------------------------------------------------------------
			// 03/05/2015 ECU check if there are any devices to clone
			// 23/05/2015 ECU put in the check on null
			// ---------------------------------------------------------------------
			if ((devices != null) && (devices.length > 0))
			{
				// -----------------------------------------------------------------
				// 03/05/2015 ECU request the IP address of the device to be cloned
				// -----------------------------------------------------------------
				DialogueUtilities.singleChoice (this, 
									   	   		getString (R.string.title_device_to_clone),
									   	   		devices,
									   	   		0, 
									   	   		Utilities.createAMethod (ClonerActivity.class,"ConfirmAddress",0),
									   	   		Utilities.createAMethod (ClonerActivity.class,"CancelAddress",0));
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 14/10/2014 ECU no addresses to handle
				// 14/12/2016 ECU use resource and add centring
				// -----------------------------------------------------------------
				Utilities.popToast (getString (R.string.no_addresses),true);
				finish ();
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// -------------------------------------------------------------------------
		// Inflate the menu; this adds items to the action bar if it is present.
		// -------------------------------------------------------------------------
		getMenuInflater().inflate (R.menu.disk_handling, menu);
		return true;
	}
	/* ============================================================================= */
	private static View.OnClickListener buttonListener = new View.OnClickListener() 
	{
		// -------------------------------------------------------------------------
		@Override
		public void onClick(View view) 
		{	
			// ---------------------------------------------------------------------
			
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.button_clone_files: 
				{
					if (!exitActivity)
					{
						if (!cloningInProgress)
						{
							// ------------------------------------------------------
							// 08/01/2015 ECU ask the user which files are to be 
							//                cloned
							// 03/02/2015 ECU change to use resource
							// -----------------------------------------------------
							Utilities.popToast (view,context.getString (R.string.clone_only_new_files),
												Utilities.createAMethod(ClonerActivity.class,"StartCloningNew",0),
												Utilities.createAMethod(ClonerActivity.class,"StartCloningAll",0));
							// -----------------------------------------------------	
						}
						else
						{
							// -----------------------------------------------------
							// 20/03/2014 ECU cloning is already in progress
							// 03/05/2015 ECU changed to include 'context' now method
							//				  is 'static'
							// -----------------------------------------------------
							Utilities.popToast (ClonerActivity.context.getString (R.string.cloning_in_progress));
							// -----------------------------------------------------
						}
					}
					else
					{
						// ---------------------------------------------------------
						// 03/05/2015 ECU exit this activity
						// ---------------------------------------------------------
						Exit ();
						// ---------------------------------------------------------
					}
					break;
				}
				// -----------------------------------------------------------------	
			}
		}
	};
	/* ============================================================================= */
	public boolean onOptionsItemSelected (MenuItem item)
	{	
		switch (item.getItemId())
		{
			// ---------------------------------------------------------------------
			case R.id.async_copy:
				
				try 
				{
					// --------------------------------------------------------------
					// 21/04/2015 ECU changed to use the strings in resources
					// 01/09/2015 ECU changed to use StaticData
					// --------------------------------------------------------------
					boolean result = new AsyncSendFile (getBaseContext(),IPaddress,
						new FileDetails(PublicData.projectFolder,new File 
								(PublicData.projectFolder + getString(R.string.received_file_1) + getString(R.string.temp_file_socket))),
						StaticData.SOCKET_CHUNK_SIZE).execute().get();
				
						filesList.append (" Result " + result + "\n");
				} 
				catch (Exception theException) 
				{
				} 				
				return true;
			// ---------------------------------------------------------------------
		}		
		return true;
	}
	// =============================================================================
	public static void Exit ()
	{
		// -------------------------------------------------------------------------
		// 03/05/2015 ECU created to just exit this activity from static method
		// -------------------------------------------------------------------------
		((ClonerActivity) context).finish ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void RequestFile (Context theContext,FileDetails theFileDetails,String theRequestor)
	{
		// -------------------------------------------------------------------------
		// 19/03/2014 ECU check if the file already exists on this device
		// 06/04/2014 ECU include the check on 'cloningInProgress'
		// 09/01/2015 ECU add in the flag contained in the object passed when
		//                cloning was started
		// -------------------------------------------------------------------------
		if (!(new File (theFileDetails.GetFileName()).exists()) || 
				(PublicData.cloningInProgress && ClonerActivity.cloningDetails.filesFlag))
		{
			// ---------------------------------------------------------------------
			// 19/03/2014 ECU want to make sure all of the folders are created
			// ---------------------------------------------------------------------
			new File (theFileDetails.GetFileName()).getParentFile().mkdirs();
			// ---------------------------------------------------------------------
			// 31/01/2015 ECU changed the name of the method called
			// 21/03/2015 ECU pass through the message type as an argument
			// ---------------------------------------------------------------------
			Utilities.sendSocketMessageSendTheObject (theContext,
													  theRequestor,
													  PublicData.socketNumberForData,
													  StaticData.SOCKET_MESSAGE_REQUEST_FILE,
													  (Object) theFileDetails);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 19/03/2014 ECU the file already exists so just acknowledge the fact
			// ---------------------------------------------------------------------
			Utilities.sendDatagramType(theContext,theRequestor,StaticData.SOCKET_MESSAGE_FILE_ACK);
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	void StartCloning (boolean theAllFlag)
	{
		// -------------------------------------------------------------------------
		// 08/01/2014 ECU the cloning will be started using 'theAllFlag' to 
		//                indicate which files are to be cloned
		//                   theAllFlag = true    clone all files
		//                              = false   clone only new files
		// 03/05/2015 ECU changed so that this method can only be called if the
		//                remote device can be contacted and is in 'clone mode'
		//                so no need to do any checking here
		// -------------------------------------------------------------------------
		// 20/03/2014 ECU indicate cloning in progress
		//            ECU changed to use the new method
		// -------------------------------------------------------------------------
		ResetDisplay (true);
		// -------------------------------------------------------------------------
		// 20/03/2014 ECU initialise the list of files
		// -------------------------------------------------------------------------
		listOfFiles 		= new ArrayList<File>();
		// -------------------------------------------------------------------------
		// 19/03/2014 ECU build up a list of all files in the specified
		//                directory
		// -------------------------------------------------------------------------
		FilesInDirectory (PublicData.projectFolder); 
		// -------------------------------------------------------------------------
		// 15/01/2016 ECU decide which type of cloning is to be used, either
		//                normal or using the file transfer utilities
		// -------------------------------------------------------------------------
		if (!ftpMode)
		{
			// ---------------------------------------------------------------------
			// 15/01/2016 ECU normal cloning is wanted
			// ---------------------------------------------------------------------
			// 09/01/2015 ECU initialise the cloning details which will be passed to
			//                the device being cloned
			// ---------------------------------------------------------------------
			cloningDetails = new CloningDetails (theAllFlag, listOfFiles.size());
			// ---------------------------------------------------------------------
			// 03/05/2015 ECU tell the remote device about the files to be cloned
			// ---------------------------------------------------------------------
			Utilities.sendSocketMessageSendTheObject 
										(getBaseContext(),
										IPaddress,
										PublicData.socketNumberForData, 
										StaticData.SOCKET_MESSAGE_CLONE_START,
										(Object) cloningDetails);
			// ---------------------------------------------------------------------					
			// 19/03/2014 ECU start up the refresh handler
			// ---------------------------------------------------------------------
			filePointer = 0;
			PublicData.fileTransferStatus = StaticData.SOCKET_MESSAGE_FILE_ACK;
			fileRefreshHandler.sleep (1000);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 17/01/2016 ECU default some variables
			// ---------------------------------------------------------------------
			allFilesFlag = theAllFlag;
			// ---------------------------------------------------------------------
			// 17/01/2016 ECU request the block size to be used
			// 06/03/2016 ECU add '0' to represent the minimum value
			// 14/12/2016 ECU change to use resources on title and summary
			// ---------------------------------------------------------------------
			DialogueUtilities.sliderChoice (context,
											getString (R.string.mtu_title),
											getString (R.string.mtu_summary),
											R.drawable.file,
											null,
											(FileTransferUtilities.BLOCK_SIZE/StaticData.MTU),
											0,
											1000,
											getString (R.string.click_to_set_buffer_size),
											Utilities.createAMethod (ClonerActivity.class,"BufferSize",0),
											getString (R.string.default_buffer_size),
											Utilities.createAMethod (ClonerActivity.class,"BufferSizeDefault",0));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void StartCloningAll (int theOption)
	{
		((ClonerActivity) context).StartCloning (true);
	}
	/* ============================================================================= */
	public static void StartCloningNew (int theOption)
	{
		((ClonerActivity) context).StartCloning (false);
	}
	/* ============================================================================= */
	void FilesInDirectory (String theDirectory)
	{
		// -------------------------------------------------------------------------
		// 19/03/2014 ECU build up the list of files - this method will be called
		//                recursively
		// 07/01/2016 ECU if the API level of the device being cloned is less than
		//                HONEYCOMB then only include files below the cloneFileSize
		// 15/01/2016 ECU the hONEYCOMB test is only for normal cloning - NOT when
		//                using FTP utilities
		// -------------------------------------------------------------------------
		File [] files = new File (theDirectory).listFiles ();
		// -------------------------------------------------------------------------
		// 19/03/2014 ECU loop for all files/directories in the current directory
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < files.length; theIndex++)
		{
			// ---------------------------------------------------------------------
			// 19/03/2014 ECU add this file/directory into the list
			// ---------------------------------------------------------------------
			if (files[theIndex].isFile())
			{
				// -----------------------------------------------------------------
				// 07/01/2016 ECU decide whether the API level needs the file size 
				//                to be limited
				// 15/01/2016 ECU put in the check on the FTP mode
				// -----------------------------------------------------------------
				if (!ftpMode && (cloneDeviceAPI < Build.VERSION_CODES.HONEYCOMB))
				{
					// -------------------------------------------------------------
					// 07/01/2016 ECU only files below the specified size are to be 
					//                included
					// -------------------------------------------------------------
					if (files [theIndex].length() < (long) PublicData.storedData.cloneFileSize)
					{
						listOfFiles.add (files[theIndex]);
					}
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 07/01/2016 ECU all files are to be cloned
					// -------------------------------------------------------------
					listOfFiles.add (files[theIndex]);
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
			// 19/03/2014 ECU if this 'file' is of a directory then get the files
			//                in that directory
			// ---------------------------------------------------------------------
			if (files[theIndex].isDirectory())
			{
				FilesInDirectory (files[theIndex].getPath());
			}	
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	static class FileRefreshHandler extends Handler
	{
		@Override
	    public void handleMessage(Message theMessage) 
	    {   
			// ---------------------------------------------------------------------
			// 15/01/2016 ECU switch depending on the type of message
			// ---------------------------------------------------------------------
			switch (theMessage.what)
			{
			    // -----------------------------------------------------------------
				case StaticData.MESSAGE_SLEEP:
					// -------------------------------------------------------------
					// 03/05/2015 ECU handle the file transfer
					// -------------------------------------------------------------	
					if (PublicData.fileTransferStatus == StaticData.SOCKET_MESSAGE_FILE_ACK)
					{
						if (filePointer < listOfFiles.size ())
						{
							// -----------------------------------------------------
							PublicData.fileTransferStatus = StaticData.NO_RESULT;
							// -----------------------------------------------------
							// 20/03/2014 ECU set up a counter so that do not wait
							//                indefinitely for a file to be transferred
							// -----------------------------------------------------
							ackCounter = ACK_TIMEOUT;
							// -----------------------------------------------------
							SendOutFileDetails (context,
												IPaddress,
												listOfFiles.get(filePointer));
							// -----------------------------------------------------
							// 22/03/2014 ECU change so that remove the path to the project
							//                folder from the displayed string
							// 16/11/2016 ECU changed to use the new method
							// -----------------------------------------------------
							filesList.append (Utilities.getRelativeFileName(listOfFiles.get(filePointer).getAbsolutePath()) + StaticData.NEWLINE);
							// -----------------------------------------------------
							filePointer++;
							// -----------------------------------------------------
							// 14/12/2016 ECU use resource and format
							// ------------------------------------------------------
							fileStatus.setText (String.format (context.getString (R.string.cloning_transferring_format),filePointer,listOfFiles.size()));
							// ------------------------------------------------------
							sleep (100);
							// -----------------------------------------------------
						}
						else
						{
							// -----------------------------------------------------
							// 06/04/2014 ECU tell the destination that cloning has
							//                finished
							// -----------------------------------------------------
							Utilities.sendDatagramType (context,
									IPaddress,StaticData.SOCKET_MESSAGE_CLONE_FINISH);
							// -----------------------------------------------------
							fileStatus.setText (R.string.all_files_transferred);
							// -----------------------------------------------------
							// 03/05/2015 ECU indicate that time to exit
							// -----------------------------------------------------
							exitActivity = true;
							// -----------------------------------------------------
							// 20/03/2014 ECU indicate cloning completed
							//		            ECU changed to use the method
							// -----------------------------------------------------
							ResetDisplay (false);
							// -----------------------------------------------------
						}
					}
					else
					{
						// ---------------------------------------------------------
						// 20/03/2014 ECU check if reached the time limit waiting for an ACK
						// ---------------------------------------------------------
						if (ackCounter-- > 0)
						{
							// -----------------------------------------------------
							// 20/03/2014 ECU still waiting for the ACK
							// -----------------------------------------------------
							((Activity)context).setTitle ("ACK Counter " + ackCounter);
							sleep (ACK_DELAY);
							// -----------------------------------------------------
						}
						else
						{
							// -----------------------------------------------------
							// 20/03/2014 ECU a problem must have occurred
							// 03/05/2015 ECU changed to use resource
							// -----------------------------------------------------
							fileStatus.setText (R.string.timeout_waiting_for_act);
							// -----------------------------------------------------
							// 20/03/2014 ECU indicate cloning completed
							//            ECU changed to use the method
							// -----------------------------------------------------
							ResetDisplay (false);
							// -----------------------------------------------------
						}
					}
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_DISPLAY:
					// -------------------------------------------------------------
					// 15/01/2016 ECU created to append a message to the displayed
					//                textview
					// -------------------------------------------------------------
					filesList.append ((String)theMessage.obj);
					// -------------------------------------------------------------
					// 16/01/2016 ECU check whether the status of the files is to be
					//                displayed
					// -------------------------------------------------------------
					if (theMessage.arg1 != StaticData.NO_RESULT)
					{
						// ---------------------------------------------------------
						// 16/01/2016 ECU display the status of the file transfer
						//                arg1 contains the pointer into the
						//                     list of files - so add 1 to make it
						//                     a counter
						//                arg2 contains the size of the list of files
						// 14/12/2016 ECU changed to use resource and format
						// ---------------------------------------------------------
						fileStatus.setText (String.format (context.getString (R.string.cloning_transferring_format),(theMessage.arg1 + 1),theMessage.arg2));
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				    break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_DISPLAY_COUNTER:
					// -------------------------------------------------------------
					// 14/12/2016 ECU created to display the block counter which is 
					//                passed as argument 1
					// 16/12/2016 ECU pass the total number of blocks in argument 2
					//            ECU changed to use resource format
					// -------------------------------------------------------------
					fileStatus.setText (String.format (context.getString (R.string.transfer_block_format),theMessage.arg1,theMessage.arg2));
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_FINISH:
					// -------------------------------------------------------------
					// 16/01/2016 ECU created to be called by the server to indicate 
					//                that the transfer has finished
					// -------------------------------------------------------------
					ResetDisplay (false);
					// -------------------------------------------------------------
					// 16/01/2016 ECU update the files transferred message
					// -------------------------------------------------------------
					fileStatus.setText (R.string.all_files_transferred);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}	
		}
	    /* ------------------------------------------------------------------------ */
	    public void sleep (long delayMillis)
	    {		
	        this.removeMessages (StaticData.MESSAGE_SLEEP);
	        sendMessageDelayed(obtainMessage (StaticData.MESSAGE_SLEEP), delayMillis);
	    }
	};
	/* ============================================================================= */
	static void ResetDisplay (boolean theFlag)
	{
		// -------------------------------------------------------------------------
		// 20/03/2014 ECU set various fields depending on the state of the cloning
		//				     theFlag = true ..... cloning has started
		//                           = false .... cloning has finished or timeout
		// -------------------------------------------------------------------------
		if (theFlag)
		{
			// ---------------------------------------------------------------------
			// 20/03/2014 ECU cloning has started
			// 14/12/2016 ECU changed to use resource format
			// ---------------------------------------------------------------------
			cloningInProgress = true;
			cloneFiles.setText (String.format (context.getString(R.string.cloning_to_format),IPaddress));
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 20/03/2014 ECU cloning has finished or a timeout happened
			// 03/05/2015 ECU put in the check on exit
			// ---------------------------------------------------------------------
			((Activity)context).setTitle (R.string.title_activity_disk_handling);
			cloningInProgress = false;
			cloneFiles.setText (exitActivity ? R.string.exit_this_activity 
											 : R.string.clone_files);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	static void SendOutFileDetails (Context theContext,String theIPAddress,File theFile)
	{
		FileDetails fileDetails = new FileDetails (PublicData.projectFolder,theFile);	
		// -------------------------------------------------------------------------
		// 31/01/2015 ECU change the name of the method called
		// 21/03/2015 ECU pass through the message type as an argument
		// -------------------------------------------------------------------------
		Utilities.sendSocketMessageSendTheObject (theContext,
												  theIPAddress,
												  PublicData.socketNumberForData,
												  StaticData.SOCKET_MESSAGE_FILE_INFO,
												  (Object) fileDetails);
	}
	/* ============================================================================= */
	
	
	
	// =============================================================================
	// =============================================================================
	// 03/05/2015 ECU declare the methods that are passed as arguments
	// =============================================================================
	// =============================================================================
	public static void BufferSize (int theMTUMultiplier)
	{
		// -------------------------------------------------------------------------
		// 17/01/2016 ECU set the size of the transmit buffer
		// -------------------------------------------------------------------------
		// 17/01/2016 ECU check for a zero entry
		// -------------------------------------------------------------------------
		if (theMTUMultiplier == 0)
		{
			Utilities.popToast ("0 is not valid so using 1");
			theMTUMultiplier = 1;
		}
		// -------------------------------------------------------------------------
		// 15/01/2016 ECU cloning is to be achieved using ftp
		// 17/01/2016 ECU set the buffer size
		// ---------------------------------------------------------------------
		FileTransferUtilities.ServerInitialise (context, IPaddress,listOfFiles,allFilesFlag,(theMTUMultiplier * StaticData.MTU));
		// ---------------------------------------------------------------------
	}
	// =============================================================================
	public static void BufferSizeDefault (int theDummyArgument)
	{
		// -------------------------------------------------------------------------
		// 17/01/2016 ECU created to start the cloning with a default buffer size
		// -------------------------------------------------------------------------
		BufferSize ((FileTransferUtilities.BLOCK_SIZE/StaticData.MTU));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void CancelAddress (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 03/05/2015 ECU called when the user cancels the operation to select a
		//                device
		// -------------------------------------------------------------------------
		// 03/05/2015 ECU just exit this activity
		// -------------------------------------------------------------------------
		Exit ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void ConfirmAddress (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 16/03/2015 ECU now send the music to the remote device
		// 02/05/2015 ECU want to check if the device is present and in the correct mode
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak (ClonerActivity.context.getString (R.string.trying_to_communicate),true,Toast.LENGTH_SHORT);
		// -------------------------------------------------------------------------
		// 03/05/2015 ECU remember the chosen address
		// -------------------------------------------------------------------------
		IPaddress = Devices.returnIPAddress (devices [theIndex]);
		// -------------------------------------------------------------------------
		// 07/01/2016 ECU remember the API address of the chosen device
		// -------------------------------------------------------------------------
		cloneDeviceAPI = Devices.returnAPILevel (IPaddress);
		// -------------------------------------------------------------------------
		// 15/01/2016 ECU if normal cloning is wanted then will need to check that
		//                the destination device is in 'cloning' mode
		// -------------------------------------------------------------------------
		if (!ftpMode)
		{
			DeviceStatusHandler deviceStatusHandler 
					= new DeviceStatusHandler (DialogueUtilities.context,
											   Utilities.createAMethod (ClonerActivity.class,"StatusSuccessMethod"),
											   Utilities.createAMethod (ClonerActivity.class,"StatusFailureMethod"));
			deviceStatusHandler.initiate (IPaddress);
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
 	public static void ExitMethod (Object theSelection)
  	{
 		// -------------------------------------------------------------------------
  		// 15/01/2016 ECU user has indicated that FTP is not to be tried
  		// -------------------------------------------------------------------------
 		Exit ();
 		// -------------------------------------------------------------------------
  	}
	// =============================================================================
	public static void StatusFailureMethod ()
	{
		// -------------------------------------------------------------------------
		// 03/05/2015 ECU unable to communicate with the remote device tell the user
		//                and then ignore the request
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak (ClonerActivity.context.getString (R.string.unable_to_communicate),true,Toast.LENGTH_SHORT);
		// -------------------------------------------------------------------------
		// 03/05/2015 ECU just exit the activity
		// -------------------------------------------------------------------------
		Exit ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void StatusSuccessMethod ()
	{
		// -------------------------------------------------------------------------
		// 02/05/2015 ECU have obtained the status so check if the remote device is
		//                capable of being cloned
		// -------------------------------------------------------------------------
		// 03/05/2015 ECU have successfully received the status from the remote
		//                device so check if it is in 'clone' mode
		// -------------------------------------------------------------------------
		if (PublicData.receivedStatus.cloneMode)
		{
			// ---------------------------------------------------------------------
			// 03/05/2015 ECU the remote device is ready for cloning
			// ---------------------------------------------------------------------
			// 03/05/2015 ECU make the button visible
			// ---------------------------------------------------------------------
			cloneFiles.setVisibility (View.VISIBLE);
			// -------------------------------------------------------------------------
			// 07/01/2016 ECU if below HONEYCOMB then restrict file size
			// -------------------------------------------------------------------------
			if (cloneDeviceAPI < Build.VERSION_CODES.HONEYCOMB)
			{
				// ---------------------------------------------------------------------
				// 07/01/2016 ECU indicate that file size is relevant
				// 16/12/2016 ECU changed to use format resource
				// ----------------------------------------------------------------------
				Utilities.popToast (String.format (context.getString (R.string.cloner_default_file_size_format),
						                                     PublicData.storedData.cloneFileSize),true);
				// ---------------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 20/03/2014 ECU declare and set the spinner's adapter
			// 03/05/2015 ECU moved here from main body of code
			// -------------------------------------------------------------------------	
			cloneFiles.setOnClickListener (buttonListener);
			filesList.setMovementMethod (new ScrollingMovementMethod());
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 15/01/2016 ECU the selected device is not in clone mode so ask if
			//                FTP is to be tried
			// ---------------------------------------------------------------------
			// 03/05/2015 ECU the device is not in 'clone' mode so just exit after
			//                telling the user
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (ClonerActivity.context.getString (R.string.not_in_clone_mode),true,Toast.LENGTH_SHORT);
			// ---------------------------------------------------------------------
			// 15/01/2016 ECU ask whether FTP is to be tried
			// 14/12/2016 ECU changed to use resources
			// ---------------------------------------------------------------------
			DialogueUtilities.yesNo (context,  
					   				 context.getString (R.string.cloning_to) + IPaddress,
					   				 context.getString (R.string.cloning_wrong_mode),
					   				 null,
					   				 Utilities.createAMethod (ClonerActivity.class,"UseFTPMethod",(Object) null),
					   				 Utilities.createAMethod (ClonerActivity.class,"ExitMethod",(Object) null)); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
 	public static void UseFTPMethod (Object theSelection)
  	{
 		// -------------------------------------------------------------------------
  		// 15/01/2016 ECU user has indicated that FTP is to be tried
  		// -------------------------------------------------------------------------
 		ftpMode = true;
 		// -------------------------------------------------------------------------
 		// 15/01/2016 ECU change the legend on the button and make it visibl
 		// -------------------------------------------------------------------------
 		cloneFiles.setText (context.getString (R.string.clone_files_ftp));
 		cloneFiles.setVisibility (View.VISIBLE);
 		// -------------------------------------------------------------------------
 		// 15/01/2016 ECU set the click listener for the button and the scrolling
 		//                mode for the list of files
 		// -------------------------------------------------------------------------
 		cloneFiles.setOnClickListener (buttonListener);
		filesList.setMovementMethod (new ScrollingMovementMethod());
		// -------------------------------------------------------------------------
  	}
 	// =============================================================================
}

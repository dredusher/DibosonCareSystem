package com.usher.diboson;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.widget.TextView;

public class DiscoverNetwork extends DibosonActivity 
{
	/* ============================================================================= */
	// 23/07/2013 ECU created
	// 07/08/2013 ECU make sure that an entry is made for this device
	// 20/09/2013 ECU change the logic to speed it up using a thread pool
	// 09/12/2013 ECU terminate this activity if there is no network to discover
	// 22/12/2013 ECU discovery only works for class C networks (255.255.255.0
	//                mask because other classes cause two many threads.
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 29/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 20/01/2016 ECU general tidy up and change logic to work with device details
	/* ============================================================================= */
	private static final String TAG		= "DiscoverNetwork";
	/* ============================================================================= */
	final static int KEEP_ALIVE_TIME 	= 10;
											// 20/09/2013 ECU added - when the number of 
											//                threads is greater than the core,
											//                this is the maximum time that excess
										    //                idle threads will wait for new tasks 
	                                        //	 			  before terminating. Units are supplied.
	final static int MAXIMUM_THREADS    = 300;
											// 13/11/2013 ECU max threads to run at a go
	/* ============================================================================= */
	File				addressesFile;				// 11/11/2016 ECU added
	TextView 			addressView;				// 01/08/2013 ECU added
	TextView			discoveredAddressView;		// 20/09/2013 ECU added
	int					discoveryTimeout;			// 12/11/2016 ECU added
	String				networkIPAddress = "";		// 11/11/2016 ECU added
	String				networkMask = "";			// 11/11/2016 ECU added
	int                 numberOfRunningThreads;		// 11/11/2016 ECU added
	ThreadPoolExecutor 	threadPoolExecutor = null;	// 20/09/2013 ECU added
	TextView 			threadsRunningNumber;		// 12/11/2013 ECU added
	TextView 			workingAddress;				// 11/11/2016 ECU added
	/* ============================================================================= */
	public static TCPRefreshHandler 	tcpRefreshHandler;		// 01/08/2013 ECU added
	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// ---------------------------------------------------------------------
			// 09/12/2013 ECU check if there is a network to discover
			// ---------------------------------------------------------------------
			if (Utilities.checkForNetwork (this))
			{
				// -----------------------------------------------------------------
				// 09/12/2013 ECU there is a network to discover so continue
				// -----------------------------------------------------------------
				// 11/11/2016 ECU default the parameters just in case none supplied
				// -----------------------------------------------------------------
				discoveryTimeout	= StaticData.DISCOVERY_TIMEOUT * 1000;
				networkIPAddress	= PublicData.ipAddress;
				networkMask 		= PublicData.networkMask;
				// -----------------------------------------------------------------
				// 11/11/2016 ECU get the parameters that were passed across
				// -----------------------------------------------------------------
				Bundle extras = getIntent().getExtras();
				if (extras != null) 
				{
					// -------------------------------------------------------------
					// 11/11/2016 ECU get the network address and network mask
					// -------------------------------------------------------------
					discoveryTimeout	= extras.getInt    (StaticData.PARAMETER_TIMER) * 1000;
					networkIPAddress	= extras.getString (StaticData.PARAMETER_IP_ADDRESS);
					networkMask 		= extras.getString (StaticData.PARAMETER_NETWORK_MASK);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				setContentView (R.layout.activity_discover_network);	
				setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				// -----------------------------------------------------------------
				addressView   = (TextView) findViewById (R.id.discover_address_view);
				// -----------------------------------------------------------------
				// 11/11/2016 ECU display details of the network that is being
				//                discovered
				// -----------------------------------------------------------------
				addressView.setText (networkIPAddress  + "\nSubnet Mask : " + networkMask);
				// -----------------------------------------------------------------
				// 20/09/2013 ECU add the discovered address view
				// -----------------------------------------------------------------
				discoveredAddressView   = (TextView)findViewById (R.id.discovered_address_view);
				// -----------------------------------------------------------------
				// 12/11/2013 ECU get the view for the number of threads that are running
				// -----------------------------------------------------------------
				threadsRunningNumber = (TextView) findViewById (R.id.threads_number_view);
				workingAddress 		 = (TextView) findViewById (R.id.working_address);
				// -----------------------------------------------------------------
				// 25/02/2014 ECU just log the network details that are being used
				// -----------------------------------------------------------------
				Utilities.LogToProjectFile (TAG,"Network : " + networkIPAddress + " (" + networkMask + ")");
				// -----------------------------------------------------------------
				// 11/11/2016 ECU clear all existing devices
				// -----------------------------------------------------------------
				PublicData.deviceDetails = new ArrayList<Devices>();;
				// -----------------------------------------------------------------
				// 01/08/2013 ECU declare the message handler
				// -----------------------------------------------------------------
				tcpRefreshHandler = new TCPRefreshHandler ();		  
				// -----------------------------------------------------------------
				// 11/11/2016 ECU set up the pool of threads to be used
				// -----------------------------------------------------------------
				final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(MAXIMUM_THREADS);
				// -----------------------------------------------------------------
				threadPoolExecutor = new ThreadPoolExecutor(MAXIMUM_THREADS, 
															MAXIMUM_THREADS,
															KEEP_ALIVE_TIME,
															TimeUnit.SECONDS, queue);
				// -----------------------------------------------------------------
				// 11/11/2016 ECU start up the discovery process
				// -----------------------------------------------------------------
				tcpRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_START);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 09/12/2013 ECU there is no network to be discovered
				// -----------------------------------------------------------------
				Utilities.popToast (getString(R.string.no_network_to_discover),true);
				// -----------------------------------------------------------------
				// 09/12/2013 ECU finish this activity
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been recreated after having been
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
		return true;
	}
	// =============================================================================
	void discoveryEnd ()
	{
		// -------------------------------------------------------------------------
		// 11/11/2016 ECU called when the discovery has been completed
		// -------------------------------------------------------------------------
		// 20/09/2013 ECU close down the thread pool
		// -------------------------------------------------------------------------
		threadPoolExecutor.shutdown ();
		// -------------------------------------------------------------------------
		// 18/03/2015 ECU make sure any servers are updated
		// 26/02/2016 ECU added the remote controller server
		// -------------------------------------------------------------------------
		PublicData.phoneServer	= null;
		PublicData.remoteControllerServer = null;
		PublicData.wemoServer		= null;
		// -------------------------------------------------------------------------
		// 25/07/2013 ECU write details of devices to disk
		// 26/07/2013 ECU edit to use devices_file rather than actual string
		// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
		// -------------------------------------------------------------------------
		AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + getBaseContext().getString (R.string.devices_file),PublicData.deviceDetails);
		// -------------------------------------------------------------------------
		// 11/11/2016 ECU now finish with this activity
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@SuppressWarnings("unchecked")
	public static void generateAddressesOnNetwork (File theWriteFile,String theIPAddress,String theNetworkMask)
	{
		// -------------------------------------------------------------------------
		// 11/11/2016 ECU created to generate all valid addresses on the specified
		//                network and write the details to the specified file
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 11/11/2016 ECU create the necessary stream for writing
			// ----------------------------------------------------------------------
			BufferedWriter bufferedWriter = new BufferedWriter (new FileWriter (theWriteFile));
			// ---------------------------------------------------------------------
			// 11/11/2016 ECU write the header information
			// ---------------------------------------------------------------------
			bufferedWriter.write (StaticData.COMMENT_INTRODUCER + theIPAddress + "\n");
			bufferedWriter.write (StaticData.COMMENT_INTRODUCER + theNetworkMask + "\n");
			// ---------------------------------------------------------------------
			// 11/11/2016 ECU now generate the addresses
			// ---------------------------------------------------------------------
			int [][] octets = Utilities.getNetworkAddress (theIPAddress,theNetworkMask);
			// ---------------------------------------------------------------------
			// 10/11/2016 ECU want to generate the valid addresses for each octet
			// ---------------------------------------------------------------------
			List<Integer>[] addresses = (List<Integer>[])new List[4];
				
			for (int index=0; index<4; index++)
			{
				addresses [index] = Utilities.validAddresses(octets[1][index],!(index == 3));
			}	
			// ---------------------------------------------------------------------
			// 10/11/2016 ECU now loop through the generated octets to build the list
			//                to be returned
			// ---------------------------------------------------------------------
			int address1,address2,address3,address4;
			// ---------------------------------------------------------------------
			for (int first = 0; first < addresses[0].size(); first++)
			{
				address1 = octets [0][0] + addresses[0].get (first);
				 
				for (int second = 0; second < addresses[1].size(); second++)
				{
					address2 = octets [0][1] + addresses[1].get (second);
					 
					for (int third = 0; third < addresses[2].size(); third++)
					{
						address3 = octets [0][2] + addresses[2].get (third);
						 
						for (int fourth = 0; fourth < addresses[3].size(); fourth++)
						{
							address4 = octets [0][3] + addresses[3].get (fourth);
							// -----------------------------------------------------
							// 10/11/2016 ECU add into the list of addresses
							// -----------------------------------------------------
							bufferedWriter.write ("" + address1 + "." + address2 + "." + address3 + "." + address4 + "\n");
							// -----------------------------------------------------
						}	
					}
				}
			}
			// ---------------------------------------------------------------------
			// 11/11/2016 ECU everything done so flush and close the output stream
			// ---------------------------------------------------------------------
			bufferedWriter.flush();
			bufferedWriter.close ();
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
		}
	}
	// =============================================================================
	void getDeviceDetails (String theIPAddress)
	{
		// -------------------------------------------------------------------------
		// 11/11/2016 ECU created to request and set the details of the specified
		//                device
		// -------------------------------------------------------------------------
		// 19/08/2013 ECU shift the logic about so that I only register devices running my app
		// 31/01/2015 ECU change the name of the method called
		// 29/11/2015 ECU change to use StaticData
		// -----------------------------------------------------------------	
		Devices localDevice = (Devices) Utilities.requestObjectFromDevice (getBaseContext (),theIPAddress,
				  							PublicData.socketNumberForData,StaticData.SOCKET_MESSAGE_REQUEST_DETAILS_RAW);
		// -----------------------------------------------------------------
		// 19/08/2013 ECU check if this is a device of interest
		// 20/01/2016 ECU changed the logic
		// -----------------------------------------------------------------
		if (localDevice == null)	
		{
			// -------------------------------------------------------------
			// 20/01/2016 ECU this is an incompatible device
			// 26/02/2016 ECU added the final 'false' to indicate no remote
			//                controller
			// -------------------------------------------------------------
			localDevice = new Devices ();
			localDevice.Initialise (theIPAddress,
					                "Mac Address",
					                "Serial Number",
					                false,
					                "non-compatable device",
					                false,
					                false,
					                StaticData.NO_RESULT,
					                false);
		}
		// -----------------------------------------------------------------
		// 22/03/2015 ECU add the new device into the list
		// -----------------------------------------------------------------
		PublicData.deviceDetails.add (localDevice);		
		// -----------------------------------------------------------------
	}
	// =============================================================================
	public static File openIPAddressesFile (Context theContext,String theIPAddress,String theNetworkMask,boolean theCreateFlag)
	{
		// -----------------------------------------------------------------------
		// 11/11/2016 ECU created to open to the file that holds the IP addresses
		//                that will be used for the discovery. If the file does
		//                not exist, or 'theCreateFlag' is true, then the file will
		//                be created and the relevant IP addresses generated
		// ------------------------------------------------------------------------
		File localFile = new File (PublicData.projectFolder + StaticData.IP_ADDRESSES_FILE);
		// ------------------------------------------------------------------------
		// 11/11/2016 ECU check if the file exists - if not, or 'theCreateFlag' is
		//                true, then create it and generate the relevant data
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 11/11/2016 ECU check if 'theCreateFlag' needs processing
			// ---------------------------------------------------------------------
			if (localFile.exists())
			{
				if (theCreateFlag)
				{
					localFile.delete();
				}
				else
				{
					// -------------------------------------------------------------
					// 11/11/2016 ECU want to check if the file is relevant to
					//                the input parameters. The entries are stored
					//                as comments
					// -------------------------------------------------------------
					BufferedReader bufferedReader = new BufferedReader (new FileReader (localFile));
					String localAddress = bufferedReader.readLine().replace(StaticData.COMMENT_INTRODUCER,"");
					String localMask 	= bufferedReader.readLine().replace(StaticData.COMMENT_INTRODUCER,"");
					bufferedReader.close ();
					// -------------------------------------------------------------
					// 11/11/2016 ECU check if relevant - if the address or mask is
					//                different then want to recreate
					// -------------------------------------------------------------
					if (!theIPAddress.equalsIgnoreCase(localAddress) || !theNetworkMask.equalsIgnoreCase(localMask))
					{
						localFile.delete();
					}
					// -------------------------------------------------------------
				}
			}	
			// ---------------------------------------------------------------------
			// 11/11/2016 ECU now check if the file exists
			// ---------------------------------------------------------------------
			if (!localFile.exists())
			{
				// -----------------------------------------------------------------
				// 11/11/2016 ECU file does not exist so create it
				// -----------------------------------------------------------------
				localFile.createNewFile();
				// -----------------------------------------------------------------
				// 11/11/2016 ECU now need to generate the contents
				// -----------------------------------------------------------------
				generateAddressesOnNetwork (localFile,theIPAddress,theNetworkMask);
				// -----------------------------------------------------------------
			} 
			// ---------------------------------------------------------------------
			// 11/11/2016 ECU the file exists so return the associated file
			// ---------------------------------------------------------------------
			return localFile;
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 11/11/2016 ECU indicate to the caller that a problem occurred
			// ---------------------------------------------------------------------
			return null;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@SuppressLint("HandlerLeak")
	class TCPRefreshHandler extends Handler
	{
		// -------------------------------------------------------------------------
		// 11/11/2016 ECU declare any local variables
		// -------------------------------------------------------------------------
		BufferedReader  bufferedReader;
		boolean			endOfFile	= false;
		String			lineRead;
		// -------------------------------------------------------------------------
		
		@Override
	    public void handleMessage(Message theMessage) 
	    { 
			// ---------------------------------------------------------------------
			// 20/01/2016 ECU changed to switch on the type of message
			// ---------------------------------------------------------------------
			switch (theMessage.what)
			{
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_PROCESS_FINISHED:
					// -------------------------------------------------------------
					// 11/11/2016 ECU called when a discovery thread ends
					// -------------------------------------------------------------
					// 11/11/2016 ECU get the device details for the device
					// -------------------------------------------------------------
					if (theMessage.arg1 != StaticData.NO_RESULT)
					{
						// ---------------------------------------------------------
						// 11/11/2016 ECU update the display
						// ---------------------------------------------------------
						discoveredAddressView.append ((String) theMessage.obj + StaticData.NEWLINE);
						// ---------------------------------------------------------
						// 11/11/2016 ECU now get details of the device
						// ---------------------------------------------------------
						getDeviceDetails ((String) theMessage.obj);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 11/11/2016 ECU decrement the number of threads running
					// -------------------------------------------------------------
					numberOfRunningThreads--;
					// -------------------------------------------------------------
					// 11/11/2016 ECU update the number of threads running
					// -------------------------------------------------------------
					threadsRunningNumber.setText(getBaseContext().getString (R.string.number_of_threads_running_label) + " "  + numberOfRunningThreads);
					// -------------------------------------------------------------
					// 11/11/2016 ECU check if everything has been done
					// -------------------------------------------------------------
					if (numberOfRunningThreads == 0)
					{
						// ---------------------------------------------------------
						// 11/11/2016 ECU tidy up things at the end of the discovery
						// ---------------------------------------------------------
						discoveryEnd ();
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 11/11/2016 ECU check if there is more to be read from the file
					// -------------------------------------------------------------
					this.sendEmptyMessage (StaticData.MESSAGE_REFRESH);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_REFRESH:
					// -------------------------------------------------------------
					// 11/11/2016 ECU start processing the IP addresses file
					// -------------------------------------------------------------
					try
					{
						// ---------------------------------------------------------
						// 11/11/2016 ECU check if it is possible to start a thread
						//                if needed
						//            ECU put in the end of file check just in case
						//                there are any messages floating about due
						//                to threads finishing
						// ---------------------------------------------------------
						if (!endOfFile && (numberOfRunningThreads < MAXIMUM_THREADS))
						{
							lineRead = bufferedReader.readLine();
						
							if (lineRead != null)
							{
								if (!lineRead.startsWith(StaticData.COMMENT_INTRODUCER))
								{
									// ---------------------------------------------
									// 11/11/2016 ECU not a comment line so process
									//            ECU don't check the number of threads
									//                because this is done at the top
									// 12/11/2016 ECU added the 'discovery....'
									// ---------------------------------------------
									DiscoverThread thread 
										= new DiscoverThread (getBaseContext(),lineRead,PublicData.socketNumber,discoveryTimeout);
									threadPoolExecutor.execute (thread.DiscoveryThread());
									// ---------------------------------------------
									// 11/11/2016 ECU increment the number of threads
									// ---------------------------------------------
									numberOfRunningThreads++;
									// ---------------------------------------------
									// 11/11/2016 ECU update the working address
									// ---------------------------------------------
									workingAddress.setText (lineRead);
									// ---------------------------------------------
									// 11/11/2016 ECU get another address
									// ---------------------------------------------
									this.sendEmptyMessage (StaticData.MESSAGE_REFRESH);
									// ---------------------------------------------
								}
								else
								{
									// ---------------------------------------------
									// 11/11/2016 ECU a comment line so ignore
									// ---------------------------------------------
									this.sendEmptyMessage (StaticData.MESSAGE_REFRESH);
									// ---------------------------------------------
								}
							}
							else
							{
								// -------------------------------------------------
								// 11/11/2016 ECU close the input stream
								// -------------------------------------------------
								bufferedReader.close();
								// -------------------------------------------------
								// 11/11/2016 ECU indicate that the end of file has 
								//                been reached
								// ------------------------------------------------
								endOfFile = true;
								// -------------------------------------------------
							}
						}
					}
					catch (Exception theException)
					{
					}
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_START:
					// -------------------------------------------------------------
					// 11/11/2016 ECU start processing the IP addresses file
					// -------------------------------------------------------------
					try
					{
						// ---------------------------------------------------------
						// 11/11/2016 ECU open to the file that contains the IP 
						//                addresses - if it doesn't exist then it will
						//                be created and the list of addresses generated
						// 12/11/2016 ECU code moved here from onCreate
						// ---------------------------------------------------------
						addressesFile = openIPAddressesFile (getBaseContext (),networkIPAddress,networkMask,false);
						// ---------------------------------------------------------
						// 11/11/2016 ECU open the stream ready to read the file
						// ----------------------------------------------------------
						bufferedReader = new BufferedReader (new FileReader (addressesFile));
						// ---------------------------------------------------------
						// 11/11/2016 ECU default the number of running threads
						// ---------------------------------------------------------
						numberOfRunningThreads = 0;
						// ---------------------------------------------------------
						// 11/11/2016 ECU start the processing of lines
						// ---------------------------------------------------------
						this.sendEmptyMessage (StaticData.MESSAGE_REFRESH);
						// ---------------------------------------------------------
					}
					catch (Exception theException)
					{
					}
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}	
	    }
	};
	// ============================================================================= 
}

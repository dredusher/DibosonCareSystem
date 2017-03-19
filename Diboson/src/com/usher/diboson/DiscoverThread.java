package com.usher.diboson;

import java.net.InetAddress;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;

public class DiscoverThread extends DibosonActivity implements Runnable
{
	/* ====================================================== */
	// 01/08/2013 ECU created
	// 22/10/2015 ECU changed to 'extends DibosonActivity'	
	/* ====================================================== */
		   Context	 context;
	public String 	 IPAddress;
	 	   int    	 port;
	public boolean	 result 	= false;
	public boolean 	 finished 	= false; 
	       int       timeout;				// 12/11/2016 ECU added
	/* ====================================================== */	
	public DiscoverThread (Context theContext,String theIPAddress,int thePort,int theTimeout)
	{
		context  	 	= theContext;
		finished  		= false;
		IPAddress 		= theIPAddress;
		port      		= thePort;	
		timeout			= theTimeout;
	}
	/* ============================================================================= */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
	}
	/* ============================================================================= */
	public void run() 
	{
		// -------------------------------------------------------------------------

	}
	/* ============================================================================= */
	public Runnable DiscoveryThread ()
	{
		Thread thread = new Thread()
		{
			// ---------------------------------------------------------------------
			@Override
		    public void run()
		    {
		       	try 
		        {
		       		// -------------------------------------------------------------
		       		// 07/08/2013 ECU if thread is running for this device then result = true
		       		// -------------------------------------------------------------
		       		if (PublicData.ipAddress.equalsIgnoreCase (IPAddress))
		       		{
		       			// ---------------------------------------------------------
		       			// 07/08/2013 ECU running on this device so must be true
						// ---------------------------------------------------------
		       			result = true;	 
		       		}
		       		else
		       		{
		       			// ---------------------------------------------------------
		       			// 07/08/2013 ECU not this device so need to check via ICMP
		       			// 01/09/2015 ECU changed to use StaticData
		       			// 12/11/2016 ECU changed to use 'timeout' rather than 
		       			//                DISCOVERY_TIMEOUT
		       			// ---------------------------------------------------------
		       			InetAddress serverAddress = InetAddress.getByName (IPAddress); 
		       			result	 = serverAddress.isReachable (timeout);
		       		}
		       		// ------------------------------------------------------------- 
		       		// 05/08/2013 ECU optionally display entry in log
		       		// -------------------------------------------------------------
		       		Utilities.debugMessage ("Network","Discovery " + IPAddress + " Result = " + result);
		       		// -------------------------------------------------------------
		       		// 01/08/2013 ECU indicate that the thread has finished
		       		// -------------------------------------------------------------
		       		finished = true;
		       		// -------------------------------------------------------------
					// 11/11/2016 ECU tell the caller that have finished
					// -------------------------------------------------------------
		       		Message localMessage = DiscoverNetwork.tcpRefreshHandler.obtainMessage (StaticData.MESSAGE_PROCESS_FINISHED, (result ? 1 : StaticData.NO_RESULT),0,IPAddress);
		       		DiscoverNetwork.tcpRefreshHandler.sendMessage (localMessage);
					// -------------------------------------------------------------
		        }
		       	catch(Exception theException)
		       	{                    
		       	}       
		    }
		 };
		 // ------------------------------------------------------------------------
		 return thread;      
	}
	/* ============================================================================= */
}

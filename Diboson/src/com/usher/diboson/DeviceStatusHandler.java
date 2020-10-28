package com.usher.diboson;

import java.lang.reflect.Method;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class DeviceStatusHandler extends Handler
{
	// =============================================================================
	// 02/05/2015 ECU created to handle the request of device information
	// 05/05/2015 ECU the message type were originally defined here but because
	//                they should be more generally available then put them in
	//                MainActivity
	// =============================================================================
	private static final int	WAIT_COUNT		= 10;
	private static final int	WAIT_DELAY		= 500;			// milliseconds
	// =============================================================================
	private Context				context;
	private Method				failureMethod;
	private Method				successMethod;
	private int					waitCounter;
	// =============================================================================
	public DeviceStatusHandler (Context theContext,
								Method 	theSuccessMethod,
								Method 	theFailureMethod)
	{
		// -------------------------------------------------------------------------
		// 02/05/2015 ECU created this construct so that the context can be set for
		//                the handler
		//            ECU add the success/failure methods
		// -------------------------------------------------------------------------
		context			= theContext;
		failureMethod	= theFailureMethod;
		successMethod	= theSuccessMethod;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void initiate (String theIPAddress)
	{
		// -------------------------------------------------------------------------
		// 02/05/2015 ECU this initiates the send to the specified address
		// 05/05/2015 ECU changed to use StaticData.M ....
		// -------------------------------------------------------------------------
		Message message = this.obtainMessage (StaticData.MESSAGE_SEND);
		message.obj 	= theIPAddress;
		this.sendMessage (message);
		// ------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void handleMessage (Message theMessage) 
	{    
		// -------------------------------------------------------------------------
		// 02/05/2015 ECU switch depending on the type of message received
		// -------------------------------------------------------------------------
		switch (theMessage.what)
		{
			// =====================================================================
			case StaticData.MESSAGE_SEND:
				// -----------------------------------------------------------------
				// 02/05/2015 ECU send out the request to the device whose IP address is
				//                supplied in the message
				// -----------------------------------------------------------------
				// 02/05/2015 ECU reset the response that is expected
				// -----------------------------------------------------------------
				PublicData.receivedStatus = null;
				// -----------------------------------------------------------------
				//  02/05/2015 ECU the destination IP address is in the message
				// -----------------------------------------------------------------
				Utilities.sendDatagramType (context,
										   (String) theMessage.obj,
										    StaticData.SOCKET_MESSAGE_REQUEST_STATUS);
				// -----------------------------------------------------------------
				// 02/05/2015 ECU indicate how long to wait for a reply
				// -----------------------------------------------------------------
				waitCounter	= WAIT_COUNT;
				// -----------------------------------------------------------------
				// 02/05/2015 ECU wait a bit before checking for a response
				// -----------------------------------------------------------------
				sleep (WAIT_DELAY);
				// -----------------------------------------------------------------
				break;
			// =====================================================================
			case StaticData.MESSAGE_SLEEP:
				// -----------------------------------------------------------------
				// 02/05/2015 ECU this message is received at the end of a 'sleep'
				//                period
				//            ECU the response is held in 'MainActivity.receivedStatus'
				//                it will only be non-null when a valid response is
				//                received
				// -----------------------------------------------------------------
				if (PublicData.receivedStatus != null)
				{
					// -------------------------------------------------------------
					// 02/05/2015 ECU a good response has been received so invoke
					//                the 'success' method
					// -------------------------------------------------------------
					try 
					{ 
						successMethod.invoke (null);
					} 
					catch (Exception theException) 
					{	
					} 
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 02/05/2015 ECU still have not received a response so check if
					//                still need to wait
					// -------------------------------------------------------------
					if (waitCounter-- > 0)
					{
						// ---------------------------------------------------------
						// 02/05/2015 ECU still waiting so just wait a bit
						// ---------------------------------------------------------
						sleep (WAIT_DELAY);
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 02/05/2015 ECU have waited long enough so just invoke the
						//                'failure' method
						// ---------------------------------------------------------
						try 
						{ 
							failureMethod.invoke (null);
						} 
						catch (Exception theException) 
						{	
						}
						// ---------------------------------------------------------
					}	
				}	
				// -----------------------------------------------------------------
				break;
		}
	}
	// -----------------------------------------------------------------------------
	public void sleep (long delayMillis)
	{		
	    this.removeMessages (StaticData.MESSAGE_SLEEP);
	    sendMessageDelayed (obtainMessage(StaticData.MESSAGE_SLEEP), delayMillis);
	}
	// =============================================================================
}

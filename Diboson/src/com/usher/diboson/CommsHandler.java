package com.usher.diboson;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class CommsHandler extends Handler
{
	// =============================================================================
	// 26/03/2017 ECU created to handle messages that are related to the communication
	//                between devices - used to be in MessageHandler but they may
	//                take a longer time
	// =============================================================================
	
	// =============================================================================
	Context 	context;
	// =============================================================================

	// =============================================================================
	public CommsHandler (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 26/03/2017 ECU initialise any variables that may be needed when the
		//                messages are processed. Initially there there was a single
		//                message (.._SYNCHRONISE) which used to be in messageHandler
		// -------------------------------------------------------------------------
		context	=	theContext;
		// -------------------------------------------------------------------------
	}
	// =============================================================================

	// =============================================================================
	@Override
	public void handleMessage (Message theMessage) 
	{   
		// -------------------------------------------------------------------------
		// 05/05/2015 ECU change to switch on the type of message received
		//                which is in '.what'
		// -------------------------------------------------------------------------
		switch (theMessage.what)
		{
			// =====================================================================
			case StaticData.MESSAGE_SYNCHRONISE:
				// -----------------------------------------------------------------
				// 25/03/2017 ECU called up to trigger file synchronisaton
				//                Although SychroniseFiles is not very time consuming
				//                it was put into a thread 'just in case'
				// -----------------------------------------------------------------
				Thread thread = new Thread()
				{
					@Override
					public void run()
					{
						try 
						{
							Utilities.SynchroniseFiles (MainActivity.activity);        
						}
						catch(Exception theException)
						{ 	                 
						}       
					}
				};
				// -----------------------------------------------------------------
				// 25/03/2017 ECU start up the thread
				// -----------------------------------------------------------------
				thread.start();        	
				// -----------------------------------------------------------------
				break;
			// =====================================================================
		}		
	}
	// =============================================================================
}

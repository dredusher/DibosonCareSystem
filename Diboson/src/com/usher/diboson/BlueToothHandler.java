package com.usher.diboson;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class BlueToothHandler 
{
	/* ============================================================================= */
	// 01/03/2014 ECU created to enable an activity to bind to the bluetooth service
	//                so that it can send commands
	/* ============================================================================= */
	Context				context;
	Messenger 			messenger = null;					// 15/02/2014 ECU added initialisation
	Messenger			messengerService;
	/* ============================================================================= */
	public BlueToothHandler (Context theContext)
	{
		// -------------------------------------------------------------------------
	   	// 01/03/2014 ECU link to the bluetooth service
		// -------------------------------------------------------------------------  
	    theContext.bindService (new Intent(theContext,BlueToothService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	          
	    messenger 	= new Messenger (new IncomingHandler());
	    
	    context		= theContext;
	}
	/* ============================================================================= */
	public boolean CheckMessenger ()
	{
		// -------------------------------------------------------------------------
		// 01/03/2014 ECU check the state of the 'messenger' so that caller can take
		//                any actions
		// -------------------------------------------------------------------------
		if (messenger == null)
			return false;
		else
			return true;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	static class IncomingHandler extends Handler 
	{
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU changed to 'static'
		// -------------------------------------------------------------------------
        @Override
        public void handleMessage(Message message) 
        {
            switch (message.what) 
            {
                case BlueToothService.MESSAGE_REMOTE_CODE:
                    Utilities.popToast ("MESSAGE_REMOTE_CODE : " + message.obj);
                    break;
                case BlueToothService.MESSAGE_REQUEST:
                    Utilities.popToast ("MESSAGE_REQUEST : " + message.obj);
                    break;
                default:
                    super.handleMessage(message);
            }
        }
    }
	/* ============================================================================= */
	public void SendMessage (String theString)
	{
	   	try 
	   	{
	   		Message message = Message.obtain (null,BlueToothService.MESSAGE_REMOTE_CODE, theString);
	   		message.replyTo = messenger;
	        // ---------------------------------------------------------------------	 	
	   		// 15/02/2014 ECU put in the null check
	        // ---------------------------------------------------------------------	 	
	        if (messengerService != null)
	        {
	        	messengerService.send (message);
	        }
	        // ---------------------------------------------------------------------
	    } 
	   	catch (RemoteException theException) 
	   	{	
	   	}
	}
	/* ============================================================================= */
	private ServiceConnection serviceConnection = new ServiceConnection() 
	{
		// -------------------------------------------------------------------------
        public void onServiceConnected(ComponentName className,IBinder service) 
        {
             messengerService = new Messenger(service); 
        }
        // -------------------------------------------------------------------------
		@Override
		public void onServiceDisconnected(ComponentName arg0)
		{
					
		}
		// -------------------------------------------------------------------------
	 };
	/* ============================================================================= */
	public void UnBind ()
	{
		// -------------------------------------------------------------------------
		// 01/03/2014 ECU unbind from the bluetooth service
		// -------------------------------------------------------------------------
		
		context.unbindService (serviceConnection);
	}
	/* ============================================================================= */
}

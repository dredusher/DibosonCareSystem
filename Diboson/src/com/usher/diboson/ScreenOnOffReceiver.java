package com.usher.diboson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ScreenOnOffReceiver
{
	/* ============================================================================= */
	// 07/03/2014 ECU created to handle screen on/off tasks
	/* ============================================================================= */
	public static void Register (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 07/03/2014 ECU register the receiver for screen on / off actions. Called
		//                when MainActivity is created
		// -------------------------------------------------------------------------
		
		theContext.registerReceiver(screenOnOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
	    theContext.registerReceiver(screenOnOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}
	/* ============================================================================= */
	public static void Unregister (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 07/03/2014 ECU unregister the screen on / off receiver. Called from
		//                MainActivity when it is being destroyed
		// -------------------------------------------------------------------------
		
		theContext.unregisterReceiver(screenOnOffReceiver);
	}
	/* ============================================================================= */
	private final static BroadcastReceiver screenOnOffReceiver = new BroadcastReceiver() 
	{
		// -------------------------------------------------------------------------
		// 07/03/2014 ECU created to receive screen on / off actions
		// -------------------------------------------------------------------------
	    public void onReceive(Context context, Intent intent)
	    { 	
	    	// ---------------------------------------------------------------------
	    	if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
	        {
	    		// -----------------------------------------------------------------
	    		// 07/03/2014 ECU screen on - take any actions
	    		// -----------------------------------------------------------------
	           
	            // -----------------------------------------------------------------
	        }
	    	// ---------------------------------------------------------------------
	        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
	        {
	        	// -----------------------------------------------------------------
	        	// 07/03/2014 ECU screen off - take any actions
	        	// -----------------------------------------------------------------
	        	
	            // -----------------------------------------------------------------
	        } 
	        // ---------------------------------------------------------------------
	    }
	};
	/* ============================================================================= */
}
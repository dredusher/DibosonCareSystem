package com.usher.diboson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ScreenOnOffReceiver
{
	/* ============================================================================= */
	// 07/03/2014 ECU created to handle screen on/off tasks
	// 04/10/2017 ECU added the actions for screen off / on
	/* ============================================================================= */
	public static void Register (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 07/03/2014 ECU register the receiver for screen on / off actions. Called
		//                when MainActivity is created
		// 10/12/2019 ECU added try/catch 'just in case'
		// -------------------------------------------------------------------------
		try
		{
			theContext.registerReceiver (screenOnOffReceiver, new IntentFilter (Intent.ACTION_SCREEN_ON));
	    	theContext.registerReceiver (screenOnOffReceiver, new IntentFilter (Intent.ACTION_SCREEN_OFF));
		}
		catch (Exception theException)
		{
		}
	    // -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void Unregister (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 07/03/2014 ECU unregister the screen on / off receiver. Called from
		//                MainActivity when it is being destroyed
		// 10/12/2019 ECU added try/catch 'just in case'
		// -------------------------------------------------------------------------
		try
		{
			theContext.unregisterReceiver (screenOnOffReceiver);
		}
		catch (Exception theException)
		{
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private final static BroadcastReceiver screenOnOffReceiver = new BroadcastReceiver() 
	{
		// -------------------------------------------------------------------------
		// 07/03/2014 ECU created to receive screen on / off actions
		// -------------------------------------------------------------------------
	    public void onReceive (Context context, Intent intent)
	    {
	    	// ---------------------------------------------------------------------
	    	if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
	        {
	    		// -----------------------------------------------------------------
	    		// 07/03/2014 ECU screen on - take any actions
	    		// 04/10/2017 ECU handle any stored actions - no need to check if
	    		//                any exist as the handler does this
	    		// -----------------------------------------------------------------
	    		Utilities.actionHandler (context,PublicData.storedData.screenOnActions);
	            // -----------------------------------------------------------------
	        }
	    	// ---------------------------------------------------------------------
	        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
	        {
	        	// -----------------------------------------------------------------
	        	// 07/03/2014 ECU screen off - take any actions
	        	// 04/10/2017 ECU handle any stored actions - no need to check if
	    		//                any exist as the handler does this
	    		// -----------------------------------------------------------------
	    		Utilities.actionHandler (context,PublicData.storedData.screenOffActions);
	        	// -----------------------------------------------------------------
	        } 
	        // ---------------------------------------------------------------------
	    }
	};
	/* ============================================================================= */
}
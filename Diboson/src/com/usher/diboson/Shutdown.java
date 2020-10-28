package com.usher.diboson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Shutdown extends BroadcastReceiver 
{
	/* ============================================================================= */
	//final static String TAG = "Shutdown";
	/* ============================================================================= */
	@Override
	public void onReceive (Context theContext, Intent theIntent)
	{
		// -------------------------------------------------------------------------
		// 22/09/2013 ECU check for the shutdown intent even though
		//                this should be the only one coming through
		// -------------------------------------------------------------------------
		String actionString = theIntent.getAction ();
		// -------------------------------------------------------------------------
		if (Intent.ACTION_SHUTDOWN.equals(actionString))
		{
			// ---------------------------------------------------------------------
			// 18/03/2017 ECU Note - the shutdown action has been received
			// ---------------------------------------------------------------------
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
}

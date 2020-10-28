package com.usher.diboson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

// =================================================================================
// 04/03/2017 ECU name changed from AlarmService because class is of a receiver
//                     rather than a Service
// =================================================================================
public class AlarmReceiver extends BroadcastReceiver
{
	/* ============================================================================= */
	@Override
	public void onReceive (Context context, Intent intent) 
	 {
		// -------------------------------------------------------------------------
		// 09/02/2015 ECU rather than having the individual fields pull in the whole
		//                object
		// -------------------------------------------------------------------------
		AlarmData alarmData = new AlarmData ();
		// -------------------------------------------------------------------------
		// 18/06/2013 ECU get the information that should have been passed in the intent
		// 07/10/2017 ECU because of an issue that was highlighted in Nougat (7.0)
		//                using 'intent.getExtras()' was leading to a 'null' value
		//                when retrieving 'getSerializable'
		// -------------------------------------------------------------------------
	    Bundle extras = intent.getBundleExtra (StaticData.PARAMETER_ALARM_BUNDLE);
	    // -------------------------------------------------------------------------
	   	if (extras != null)
	    {
	   		// ---------------------------------------------------------------------
	   		// 08/02/2015 ECU changed to use PARAMETER_
	   		//            ECU changed to use local action and alarmID
	   		// 09/02/2015 ECU pull in the data for the specified alarm
	   		// ---------------------------------------------------------------------
	   		alarmData = (AlarmData) extras.getSerializable (StaticData.PARAMETER_ALARM_DATA);
	   		// ---------------------------------------------------------------------
	    }
	    else
	    {
	    	// ---------------------------------------------------------------------
	    	// 18/06/2013 ECU just indicate that no action is needed
	    	// 08/02/2015 ECU changed to use local 'action'
	    	// 09/02/2015 ECU set the action the the object
	    	// ---------------------------------------------------------------------
	    	alarmData.action = StaticData.ALARM_ACTION_NONE;
	    	// ---------------------------------------------------------------------
	    }
	   	// -------------------------------------------------------------------------
	   	// 04/03/2017 ECU check whether the app is running correctly
	   	// -------------------------------------------------------------------------
		if ((PublicData.storedData != null) && PublicData.storedData.initialised)
		{
			// ---------------------------------------------------------------------
			// 04/03/2017 ECU the app is running so can process the alarm normally
			// ---------------------------------------------------------------------
			// 18/06/2013 ECU start up the actioning class and indicate the action to take
			// 08/02/2015 ECU use associatedData instead of medicationIndex
			//            ECU include the action, alarm ID as parameters
			// 09/02/2015 ECU added the message
			//            ECU change to pass the whole object
			// 04/03/2017 ECU when defined Intent change 'context' to 'context.get.....'
			// ---------------------------------------------------------------------
			Intent localIntent = new Intent (context.getApplicationContext(),AlarmActions.class);
			localIntent.putExtra (StaticData.PARAMETER_ALARM_DATA,alarmData);
			localIntent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity (localIntent); 
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 04/03/2017 ECU the app is not running so try and start it automatically
			// ---------------------------------------------------------------------
			// 04/03/2017 ECU there is no need to set a new alarm because when the
			//                app restarts it will set it up again properly
			// ---------------------------------------------------------------------
			// 04/03/2017 ECU restart the app and use 'true' to indicate no user
			//                input
			// ---------------------------------------------------------------------
			MainActivity.restartThisApp (context,true);
			// ---------------------------------------------------------------------
		}
	} 
	/* ============================================================================= */
}

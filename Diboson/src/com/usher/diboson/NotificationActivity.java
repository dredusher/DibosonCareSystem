package com.usher.diboson;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
// =================================================================================
public class NotificationActivity extends DibosonActivity 
{
	// =============================================================================
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 02/11/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 20/03/2017 ECU add a button to exit this activity
	// 10/11/2017 ECU added the medication notification
	// =============================================================================
	
	// =============================================================================
	Intent		notificationIntent;							// 11/11/2017 ECU added
	int			notificationType 	= StaticData.NO_RESULT;	// 01/06/2017 ECU added
	// =============================================================================
	
	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		// 21/08/2020 ECU added the check on being 'initialised'
		// -------------------------------------------------------------------------
		if (savedInstanceState == null && PublicData.initialised)
		{
			// ---------------------------------------------------------------------
			// 01/06/2017 ECU check for the parameter being passed through
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 01/06/2017 ECU get associated parameter
				// -----------------------------------------------------------------
				notificationType = extras.getInt (StaticData.PARAMETER_NOTIFICATION, StaticData.NO_RESULT);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 02/11/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_notification);
			// ---------------------------------------------------------------------
			// 11/11/2017 ECU declare the button to clear the dialogue
			// ---------------------------------------------------------------------
	    	((Button) findViewById (R.id.clear_button)).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick (View view) 
				{	
					// -------------------------------------------------------------
					// 11/11/2017 ECU just terminate the activity
					// -------------------------------------------------------------
					finish ();
					// -------------------------------------------------------------
				}
			});
	    	// ---------------------------------------------------------------------
	    	// 11/11/2017 ECU show what caused the notification
	    	// ---------------------------------------------------------------------
	    	((TextView) findViewById (R.id.notification_type)).setText ("Notification Type = " + notificationType);
			// ---------------------------------------------------------------------
			// 01/06/2017 ECU decide what to do with the notification
	    	// 11/11/2017 ECU added the '_NEW_TASK' flag
			// ---------------------------------------------------------------------
			switch (notificationType)
			{
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case StaticData.NOTIFICATION_BLUETOOTH:
					// -------------------------------------------------------------
					// 14/08/2020 ECU added to start the bluetooth activity
					// -------------------------------------------------------------
					notificationIntent = new Intent (this,BluetoothActivity.class);
					notificationIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity (notificationIntent);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case StaticData.NOTIFICATION_MEDICATION:
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case StaticData.NOTIFICATION_NOTIFICATION:
					// -------------------------------------------------------------
					// 03/08/2020 ECU activity the activity which handles 'app
					//                notifications'
					// -------------------------------------------------------------
					notificationIntent = new Intent (this,NotificationsActivity.class);
					notificationIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity (notificationIntent);
					break;
				// -----------------------------------------------------------------
				//------------------------------------------------------------------
				case StaticData.NOTIFICATION_PANIC_ALARM:
					notificationIntent = new Intent (this,PanicAlarmActivity.class);
					notificationIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); 
					startActivity (notificationIntent);
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case StaticData.NOTIFICATION_TIMER_SERVICE:
					// -------------------------------------------------------------
					// 14/08/2020 ECU indicate the current time
					// -------------------------------------------------------------
					Utilities.SpeakingClock (this,true);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case StaticData.NOTIFICATION_TTS_SERVICE:
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				default:
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 01/06/2017 ECU under all circumstances then 'finish' this activity
			// ---------------------------------------------------------------------
			finish ();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/11/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */

}

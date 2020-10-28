package com.usher.diboson;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

@SuppressLint("DefaultLocale")
public class MonitorService extends Service
{
	/* ============================================================================= */
	// 11/10/2013 ECU Note - this service is used to monitor background noises and if
	//                       a noise is above a set level then the sound recorder is
	//                       started for a set period of time. A file of the recorded
	//                       sound can be, optionally, sent to the nominated email
	//                       addresses
	// 05/12/2019 ECU Note - was getting a 'peer reset' exception when sending emails
	//                       which had largish attachments. I believe the problem is
	//                       in GridActivity - onPause stops this service and if this
	//                       is done when an email is being transmitted then the
	//                       device is causing a 'reset'.
	// 06/12/2019 ECU IMPORTANT
	//                =========
	//                This service can be stopped by GridActivity.onPause just in
	//                case a newly activated activity has need of the microphone
	//                which this service uses.
	// 17/12/2019 ECU REWRITE
	//				  =======
	//				  With the creation of the MonitorHandler, which does all of the
	//                work, then this service has been rewritten so that the handler
	//                is created when the service is started and then terminated
	//                when the service is destroyed
	/* ============================================================================= */
	final static String TAG		=	"MonitorService";
	/* ============================================================================= */

	/* ============================================================================= */
	@Override
	public IBinder onBind(Intent arg0)
	{
		// -------------------------------------------------------------------------
		return null;
		// --------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public void onCreate()
	{
		// -------------------------------------------------------------------------
		super.onCreate();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public int onStartCommand (Intent intent,int flags, int startId) 
	{
		// -------------------------------------------------------------------------
		// 23/11/2014 ECU changed from onStart and return with the appropriate
		//                STICKY bit to try and handle issues when the device is
		//                in sleep mode
		// -------------------------------------------------------------------------
		// 17/12/2019 ECU indicate that the service is up and running
		// -------------------------------------------------------------------------
		PublicData.monitorServiceRunning = true;
		// -------------------------------------------------------------------------
		// 20/11/2014 ECU log the fact that the service has started
		// 25/11/2014 ECU print out the monitoring data
		// -------------------------------------------------------------------------
		Utilities.LogToProjectFile (TAG, "onStartCommand\n" +
										PublicData.storedData.monitor.print());
		// -------------------------------------------------------------------------
		// 17/12/2019 ECU create the monitoring handler
		// 23/12/2019 ECU trap any exceptions that could occur
		// -------------------------------------------------------------------------
		try
        {
            // ---------------------------------------------------------------------
            // 23/12/2019 ECU get a new instance of the handler that will do all of
            //                the monitoring
            // ---------------------------------------------------------------------
		    PublicData.monitorHandler = new MonitorHandler (this);
		    // ---------------------------------------------------------------------
        }
        catch (Exception theException)
        {
            // ---------------------------------------------------------------------
            // 23/12/2019 ECU log any exception for further investigation
            // ---------------------------------------------------------------------
            Utilities.LogToProjectFile (TAG,"Exception : " + theException);
            // ---------------------------------------------------------------------
        }
		// -------------------------------------------------------------------------
		return Service.START_STICKY;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public void onDestroy ()
	{
		// -------------------------------------------------------------------------
		// 17/12/2019 ECU indicate that the service is 'down'
		// -------------------------------------------------------------------------
		PublicData.monitorServiceRunning = false;
		// -------------------------------------------------------------------------
		// 17/12/2019 ECU tell the handler to 'stop'
		// -------------------------------------------------------------------------
		MonitorHandler.Terminate ();
		// -------------------------------------------------------------------------
		Utilities.LogToProjectFile (TAG, "onDestroy");
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
	}
    // =============================================================================
}

package com.usher.diboson;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.usher.diboson.utilities.LifeCycle;

// =================================================================================
public class DibosonService extends Service
{
    // =============================================================================
    // 24/08/2020 ECU create so that the life cycle of services can be logged
    //            ECU it is NOT a service in it's own right
    // =============================================================================

    // =============================================================================
    @Override
    public IBinder onBind (Intent intent)
    {
        LifeCycleCheckMessage ("onBind");
        return null;
    }
    // =============================================================================
    @Override
    public void onCreate ()
    {
        // -------------------------------------------------------------------------
        LifeCycleCheckMessage ("onCreate");
        super.onCreate ();
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public int onStartCommand (Intent intent, int flags, int startId)
    {
        // -------------------------------------------------------------------------
        LifeCycleCheckMessage ("onStartCommand");
        return Service.START_STICKY;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public void onDestroy()
    {
        // -------------------------------------------------------------------------
        LifeCycleCheckMessage ("onDestroy");
        super.onDestroy ();
        // -------------------------------------------------------------------------
    }
    // =============================================================================


    // =============================================================================
    void LifeCycleCheckMessage (String theMessage)
    {
        // -------------------------------------------------------------------------
        // 24/08/2020 ECU log the message along with the name of the service's class
        // -------------------------------------------------------------------------
        LifeCycle.LogMessage (getClass().getSimpleName(),theMessage);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
}
// =================================================================================

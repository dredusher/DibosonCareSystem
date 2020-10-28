package com.usher.diboson;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

public class BluetoothA2dpRequester implements BluetoothProfile.ServiceListener
{
    // =============================================================================
    //private static final String TAG = "BluetoothA2dpRequester";
    // =============================================================================
    private Callback callback;
    // =============================================================================

    // =============================================================================
    public BluetoothA2dpRequester (Callback theCallback)
    {
        // -------------------------------------------------------------------------
        callback = theCallback;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void request (Context theContext, BluetoothAdapter theAdapter)
    {
        // -------------------------------------------------------------------------
        // 04/04/2020 ECU get the profile proxy object associated with the profile.
        // -------------------------------------------------------------------------
        try
        {
            // ---------------------------------------------------------------------
            // 04/04/2020 ECU get the profile proxy for '...A2DP'
            // ---------------------------------------------------------------------
            theAdapter.getProfileProxy (theContext, this, BluetoothProfile.A2DP);
            // ---------------------------------------------------------------------
        }
        catch (Exception theException)
        {
            // ---------------------------------------------------------------------
            // ---------------------------------------------------------------------
        }
    }
    // =============================================================================
    @Override
    public void onServiceConnected (int theProfile, BluetoothProfile theProxy)
    {
        // -------------------------------------------------------------------------
        // 04/04/2020 ECU report that the proxy has been connected to the service
        // -------------------------------------------------------------------------
        if (callback != null)
        {
            // ---------------------------------------------------------------------
            // 04/04/2020 ECU pass the information back to the main activity
            // ---------------------------------------------------------------------
            callback.onA2DPProxyReceived ((BluetoothA2dp) theProxy);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public void onServiceDisconnected(int theProfile)
    {
       // --------------------------------------------------------------------------
       // 05/04/2020 ECU report that the service has been disconnected
       //                do nothing at this time
       // --------------------------------------------------------------------------
       // --------------------------------------------------------------------------
    }
    // =============================================================================
    public static interface Callback
    {
        // -------------------------------------------------------------------------
        public void onA2DPProxyReceived (BluetoothA2dp proxy);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
}
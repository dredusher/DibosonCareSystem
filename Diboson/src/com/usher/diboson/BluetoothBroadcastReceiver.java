package com.usher.diboson;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothBroadcastReceiver extends BroadcastReceiver
{
    // =============================================================================
    private static final String TAG = "BluetoothBroadcastReceiver";
    // =============================================================================

    // =============================================================================
    private Callback callback;
    // =============================================================================

    // =============================================================================
    public BluetoothBroadcastReceiver (Callback theCallback)
    {
        // -------------------------------------------------------------------------
        callback = theCallback;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public void onReceive (Context theContext, Intent theIntent)
    {
        // -------------------------------------------------------------------------
        // 06/04/2020 ECU check if state change on the adapter
        // -------------------------------------------------------------------------
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals (theIntent.getAction()))
        {
            // ---------------------------------------------------------------------
            int state = theIntent.getIntExtra (BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            // ---------------------------------------------------------------------
            switch (state)
            {
                // -----------------------------------------------------------------
                case BluetoothAdapter.STATE_ON:
                case BluetoothAdapter.STATE_CONNECTED:
                    // -------------------------------------------------------------
                    // 04/04/2020 ECU have a connection to the adapter so indicate this
                    // -------------------------------------------------------------
                    // 10/04/2020 ECU optionally display debug message
                    // -------------------------------------------------------------
                    Utilities.debugMessage (TAG,"Adapter Connected");
                    // -------------------------------------------------------------
                    fireOnBluetoothConnected ();
                    // --------------------------------------------------------------
                    break;
                // -----------------------------------------------------------------
                case BluetoothAdapter.ERROR:
                    // -------------------------------------------------------------
                    // 04/04/2020 ECU an error has been reported
                    // -------------------------------------------------------------
                    // 10/04/2020 ECU optionally display debug message
                    // -------------------------------------------------------------
                    Utilities.debugMessage (TAG,"Adapter Error");
                    // -------------------------------------------------------------
                    fireOnBluetoothError ();
                    // -------------------------------------------------------------
                    break;
                // -----------------------------------------------------------------
                default:
                    // -------------------------------------------------------------
                    // 04/04/2020 ECU have received an event that we are not handling
                    // -------------------------------------------------------------
                    break;
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        }
        else
        // -------------------------------------------------------------------------
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals (theIntent.getAction()))
        {
            // ---------------------------------------------------------------------
            // 04/04/2020 ECU a device has connected - check if it is the one being
            //                looked for
            // ---------------------------------------------------------------------
            BluetoothDevice bluetoothDevice = theIntent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
            // ---------------------------------------------------------------------
            // 10/04/2020 ECU optionally display debug message
            // ---------------------------------------------------------------------
            Utilities.debugMessage (TAG,"Connected : " + bluetoothDevice.getName());
            // ---------------------------------------------------------------------
            // 04/04/2020 ECU check the names
            // 09/04/2020 ECU only handle if there is a connection request in
            //                progress
            // ---------------------------------------------------------------------
            if ((callback.onBluetoothConnectionData () != null) &&
                    (bluetoothDevice.getName ()).equals (callback.onDeviceName()))
            {
                // -----------------------------------------------------------------
                // 04/04/2020 ECU the specified device has been connected
                // -----------------------------------------------------------------
                // 09/04/2020 ECU confirm the connection so that the timeout message
                //                can be cancelled
                // -----------------------------------------------------------------
                callback.onConnectionObtained ();
                // -----------------------------------------------------------------
                // 08/04/2020 ECU now ask the 'connect' method to be actioned after
                //                the specified delay
                // -----------------------------------------------------------------
                BluetoothConnectorService.connectionHandler.sendEmptyMessageDelayed (BluetoothConnectorService.MESSAGE_ACTION_CONNECT_METHOD,
                        callback.onBluetoothConnectionData().connectionDelay);
                // -----------------------------------------------------------------
            }
        }
        else
        if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals (theIntent.getAction()))
        {
            // ---------------------------------------------------------------------
            // 04/04/2020 ECU a bluetooth device has requested a disconnection
            // ---------------------------------------------------------------------
            // ---------------------------------------------------------------------
        }
        else
        if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals (theIntent.getAction()))
        {
            // ---------------------------------------------------------------------
            // 04/04/2020 ECU a bluetooth device has disconnected
            // ---------------------------------------------------------------------
            BluetoothDevice bluetoothDevice = theIntent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
            // ---------------------------------------------------------------------
            // 10/04/2020 ECU optionally display debug message
            // ---------------------------------------------------------------------
            Utilities.debugMessage (TAG,"Disconnected : " + bluetoothDevice.getName());
            // ---------------------------------------------------------------------
            // 09/04/2020 ECU only handle if there is a connection request in
            //                progress
            // ---------------------------------------------------------------------
            if ((callback.onBluetoothConnectionData () != null) &&
                (bluetoothDevice.getName ()).equals (callback.onDeviceName()))
            {
                // -----------------------------------------------------------------
                // 04/04/2020 ECU the specified device has been disconnected
                // -----------------------------------------------------------------
                // 09/04/2020 ECU confirm the disconnection so that the timeout message
                //                can be cancelled
                // ----------------------------------------------------------------
                callback.onDisconnectionObtained ();
                // -----------------------------------------------------------------
                // 19/04/2020 ECU now call up the disconnection after the specified
                //                delay
                // -----------------------------------------------------------------
                BluetoothConnectorService.connectionHandler.sendEmptyMessageDelayed (BluetoothConnectorService.MESSAGE_ACTION_DISCONNECT_METHOD,
                        callback.onBluetoothConnectionData().disconnectionDelay);
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        }
        else
        {
            // ---------------------------------------------------------------------
            // 04/04/2020 ECU event has been received which is not being actioned
            // ---------------------------------------------------------------------
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    private void fireOnBluetoothConnected ()
    {
        // -------------------------------------------------------------------------
        // 04/04/2020 ECU tell the main activity that connection has occurred
        // -------------------------------------------------------------------------
        if (callback != null)
        {
            callback.onBluetoothConnected ();
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    private void fireOnBluetoothError ()
    {
        // -------------------------------------------------------------------------
        // 04/04/2020 ECU tell the main activity that an error occurred
        // --------------------------------------------------------------------------
        if (callback != null)
        {
            callback.onBluetoothError ();
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static interface Callback
    {
        // -------------------------------------------------------------------------
        public BluetoothConnectionData  onBluetoothConnectionData ();
        public void                     onBluetoothConnected ();
        public void                     onBluetoothError ();
        public void                     onConnectionObtained ();
        public void                     onDisconnectionObtained ();
        public String                   onDeviceName ();
        public void                     onFinishActivity ();
        // ------------------------------------------------------------------------
    }
    // =============================================================================
}
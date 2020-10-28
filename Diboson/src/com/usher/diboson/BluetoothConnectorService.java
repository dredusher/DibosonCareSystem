package com.usher.diboson;

import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BluetoothConnectorService extends Service implements BluetoothBroadcastReceiver.Callback, BluetoothA2dpRequester.Callback
{
    // =============================================================================
    // 06/04/2020 ECU this service handles requests to connect to a 'named' bluetooth
    //                     device.
    //
    //                The service will :-
    //                  1) enable bluetooth if it is not already enabled
    //                  2) register the receiver for the various actions
    // 09/04/2020 ECU added timeout when trying to connect
    // 10/05/2020 ECU make changes so that if the service is not running when a call
    //                is made to process a 'ACTION_DESTINATION_BLUETOOTH' action then
    //                the service will be started before processing the action.
    // 18/05/2020 ECU added 'connectedDevicesCheck'  so that when the app is started then
    //                can check if there are any connected bluetooth devices (called
    //                from MainActivity)
    // =============================================================================
    private static final String TAG = "BluetoothConnectorService";
    // =============================================================================

    // =============================================================================
    public  static  ConnectionHandler   connectionHandler;
    // =============================================================================
    private static final int CONNECTION_DELAY                   = 2 * 1000;   // mS
    private static final int CONNECTION_TIMEOUT                 = 10 * 1000;  // mS
    public  static final int MESSAGE_ACTION_CONNECT_METHOD      = 0;
    public  static final int MESSAGE_ACTION_DISCONNECT_METHOD   = 1;
    public  static final int MESSAGE_CONNECT_TIME_OUT           = 2;
    public  static final int MESSAGE_DISCONNECT_TIME_OUT        = 3;
    // =============================================================================
    // 18/04/2020 ECU Note - declare the events that to be notified to the broadcast
    //                ----   receiver
    // -----------------------------------------------------------------------------
    private         String []           actions  =   {
                                                        // -------------------------
                                                        BluetoothAdapter.ACTION_STATE_CHANGED,
                                                        BluetoothDevice.ACTION_ACL_CONNECTED,
                                                        BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED,
                                                        BluetoothDevice.ACTION_ACL_DISCONNECTED
                                                        // -------------------------
                                                     };
    private         int                     audioManagerCurrentMode = StaticData.NOT_SET;
    private         BluetoothAdapter        bluetoothAdapter;
    private         boolean                 bluetoothState;
    private         BroadcastReceiver       broadcastReceiver;
    private         Method                  connectDeviceMethod;
    private         boolean                 connectedDevicesCheck;
    private         BluetoothDevice         bluetoothDevice;
    private  static BluetoothConnectionData bluetoothConnectionData;
    private         Method                  disconnectDeviceMethod;
    private         BluetoothA2dp           profileProxy;
    // =============================================================================

    // =============================================================================
    public BluetoothConnectorService()
    {
        // -------------------------------------------------------------------------
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public IBinder onBind(Intent intent)
    {
        // ------------------------------------------------------------------------
        return null;
        // ------------------------------------------------------------------------
    }
    /* ============================================================================= */
    @Override
    public void onCreate()
    {
        // -------------------------------------------------------------------------
        super.onCreate();
        // -------------------------------------------------------------------------
        // 06/04/2020 ECU declare the handler to process messages from the 'client'
        // -------------------------------------------------------------------------
        connectionHandler = new ConnectionHandler ();
        // -------------------------------------------------------------------------
        // 06/04/2020 ECU declare the broadcast receiver to handle 'events' that are
        //                of interest
        // -------------------------------------------------------------------------
        broadcastReceiver = new BluetoothBroadcastReceiver (this);
        // -------------------------------------------------------------------------
        // 04/04/2020 ECU get the method that will be used for the connection to
        //                the A2DP profile
        // 08/04/2020 ECU changed to use new method
        // -------------------------------------------------------------------------
        connectDeviceMethod     = getA2dpMethod ("connect");
        disconnectDeviceMethod  = getA2dpMethod ("disconnect");
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    @Override
    public int onStartCommand (Intent intent,int flags, int startId)
    {
        // -------------------------------------------------------------------------
        Utilities.debugMessage (TAG,"onStartCommand");
        // -------------------------------------------------------------------------
        // 10/05/2020 ECU check if any data has been passed across in the intent
        // -------------------------------------------------------------------------
        Bundle extras = intent.getExtras ();
        // -------------------------------------------------------------------------
        if (extras != null)
        {
            // ---------------------------------------------------------------------
            // 25/03/2016 ECU MainActivity passes through whether it was started
            //                by an alarm interrupt - if it has then do not want to
            //                speak the welcome message
            // ---------------------------------------------------------------------
            bluetoothConnectionData = (BluetoothConnectionData) extras.getSerializable (StaticData.PARAMETER_DATA);
            // ---------------------------------------------------------------------
            // 18/05/2020 ECU see if being asked to return 'connected' devices only
            // ---------------------------------------------------------------------
            connectedDevicesCheck = extras.getBoolean (StaticData.PARAMETER_CONNECTED_DEVICES,false);
            // ----------------------------------------------------------------------
        }
        else
        {
            // ---------------------------------------------------------------------
            // 10/05/2020 ECU just clear the data - belt and braces
            // ---------------------------------------------------------------------
            bluetoothConnectionData = null;
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 06/04/2020 ECU register the receiver with 'this' as the 'callback'
        // -------------------------------------------------------------------------
        registerReceiver (broadcastReceiver,getFilter ());
        // -------------------------------------------------------------------------
        // 08/04/2020 ECU check if bluetooth is enabled, or has been enabled
        // -------------------------------------------------------------------------
        if (!enableBluetooth ())
        {
            // ---------------------------------------------------------------------
            // 08/04/2020 ECU the bluetooth was disabled and it was not possible to
            //                enable it so cannot continue
            // ---------------------------------------------------------------------
            stopSelf ();
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        return Service.START_STICKY;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public void onDestroy ()
    {
        // -------------------------------------------------------------------------
        // 07/04/2020 ECU unregister the broadcast receiver
        // -------------------------------------------------------------------------
        unregisterReceiver (broadcastReceiver);
        // -------------------------------------------------------------------------
        // 07/04/2020 ECU disconnect the profile because without it then was getting
        //                a 'leak' error
        //
        //                  ... has leaked ServiceConnection android.bluetooth.BluetoothA2dp$ .....
        // -------------------------------------------------------------------------
        if (profileProxy != null)
            bluetoothAdapter.closeProfileProxy (BluetoothProfile.A2DP,profileProxy);
        // -------------------------------------------------------------------------
        // 08/04/2020 ECU check if the bluetooth was enabled by this service
        // -------------------------------------------------------------------------
        if (!bluetoothState)
        {
            // ---------------------------------------------------------------------
            // 08/04/2020 ECU bluetooth was 'disabled' on entry so put it back to that
            //                state
            // ---------------------------------------------------------------------
            bluetoothAdapter.disable ();
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 06/04/2020 ECU called when service being stopped
        // -------------------------------------------------------------------------
        super.onDestroy ();
        // -------------------------------------------------------------------------
    }

    // =============================================================================
    // =============================================================================
    // Callback
    // ========
    // Declare the methods used for the callback by other classes
    // =============================================================================
    // =============================================================================
    @Override
    public void onA2DPProxyReceived (BluetoothA2dp theProfileProxy)
    {
        // -------------------------------------------------------------------------
        // 04/04/2020 ECU called when the proxy has been connected to the adapter
        // -------------------------------------------------------------------------
        // 07/04/2020 ECU remember the proxy
        // -------------------------------------------------------------------------
        profileProxy = theProfileProxy;
        // -------------------------------------------------------------------------
        // 10/05/2020 ECU the service is 'ready to go' so check if there is any data
        //                to be processed
        // -------------------------------------------------------------------------
        if (bluetoothConnectionData != null)
        {
            // ---------------------------------------------------------------------
            Message localMessage
                    = connectionHandler.obtainMessage (StaticData.MESSAGE_BLUETOOTH_CONNECT,
                                                       bluetoothConnectionData);
            // ---------------------------------------------------------------------
            // 10/05/2020 ECU reset the data to indicate that there is currently
            //                no connection
            // ---------------------------------------------------------------------
            bluetoothConnectionData = null;
            // ---------------------------------------------------------------------
            // 10/05/2020 ECU send the message to the handler for processing
            // ---------------------------------------------------------------------
            connectionHandler.sendMessage (localMessage);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 18/05/2020 ECU check if this service is just being called to indicate
        //                if there are any connected bluetooth devices
        // -------------------------------------------------------------------------
        if (connectedDevicesCheck)
        {
            // ---------------------------------------------------------------------
            // 18/05/2020 ECU get the list of connected bluetooth devices
            // ---------------------------------------------------------------------
            PublicData.connectedBluetoothDevices = profileProxy.getConnectedDevices ();
            // ---------------------------------------------------------------------
            // 18/05/2020 ECU nothing else to do so stop this service
            // ---------------------------------------------------------------------
            stopSelf ();
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public void onBluetoothConnected ()
    {
        // -------------------------------------------------------------------------
        // 04/04/2020 ECU trigger the request for the A2DP profile
        // 16/07/2020 ECU whilst testing on HTC Wildfire (API 10) got an
        //                exception so added the try/catch
        // -------------------------------------------------------------------------
        try
        {
            // ---------------------------------------------------------------------
            new BluetoothA2dpRequester (this).request (this,bluetoothAdapter);
            // ---------------------------------------------------------------------
        }
        catch (NoClassDefFoundError theException)
        {
            // ---------------------------------------------------------------------
            // 16/07/2020 ECU do not do anything because at the moment this was only
            //                added for the HTC Wildfire (API 10)
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public void onBluetoothError ()
    {
        // -------------------------------------------------------------------------
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public BluetoothConnectionData onBluetoothConnectionData ()
    {
        // -------------------------------------------------------------------------
        // 08/04/2020 ECU return the connection data
        // -------------------------------------------------------------------------
        return bluetoothConnectionData;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public void onConnectionObtained ()
    {
        // -------------------------------------------------------------------------
        // 09/04/2020 ECU called when a connection is made - called immediately
        //                because there is an inbuilt delay, that is configurable
        //                in BluetoothConnectorData, before the 'connect' method
        //                is called
        // -------------------------------------------------------------------------
        connectionHandler.removeMessages (MESSAGE_CONNECT_TIME_OUT);
        // -------------------------------------------------------------------------
        // 11/04/2020 ECU get the speaker system into the correct state
        // -------------------------------------------------------------------------
        setSpeakerMode (true);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public void onDisconnectionObtained ()
    {
        // -------------------------------------------------------------------------
        // 09/04/2020 ECU called when a disconnection is confirmed - called immediately
        //                because there is an inbuilt delay, that is configurable
        //                in BluetoothConnectorData, before the 'disconnect' method
        //                is called
        // -------------------------------------------------------------------------
        connectionHandler.removeMessages (MESSAGE_DISCONNECT_TIME_OUT);
        // -------------------------------------------------------------------------
        // 11/04/2020 ECU get the speaker system into the correct state
        // -------------------------------------------------------------------------
        setSpeakerMode (false);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public String onDeviceName ()
    {
        //--------------------------------------------------------------------------
        // 04/04/2020 ECU return the name of the device that is being looked for
        // 09/04/2020 ECU changed to use the name in the data class
        // 11/04/2020 ECU put in the check on null
        // -------------------------------------------------------------------------
        if (bluetoothConnectionData != null)
            return bluetoothConnectionData.deviceName;
        else
            return StaticData.BLANK_STRING;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public void onFinishActivity ()
    {
        // -------------------------------------------------------------------------
        // 04/04/2020 ECU finish this activity
        // -------------------------------------------------------------------------
        stopSelf ();
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void ConnectDevice (String theDeviceName,int theTimeout)
    {
        // -------------------------------------------------------------------------
        // 06/04/2020 ECU created to connect to the specified device
        // 09/04/2020 ECU added the timeout
        // -------------------------------------------------------------------------
        if (profileProxy != null)
        {
            // ---------------------------------------------------------------------
            // 08/04/2020 ECU check if this device is already connected
            // ---------------------------------------------------------------------
            if (isDeviceConnected (profileProxy,theDeviceName))
            {
                // -----------------------------------------------------------------
                // 08/04/2020 ECU the device is already connected so invoke the
                //                required actions
                //                PROBLEM - the audio is not being routed properly
                // -----------------------------------------------------------------
                invokeConnectMethod ();
                // -----------------------------------------------------------------
            }
            else
            {
                // -----------------------------------------------------------------
                // 04/04/2020 ECU get details of the device to which we want to connect
                // -----------------------------------------------------------------
                bluetoothDevice = findBondedDeviceByName (bluetoothAdapter, theDeviceName);
                // -----------------------------------------------------------------
                if (connectDeviceMethod != null && bluetoothDevice != null)
                {
                    // -------------------------------------------------------------
                    // 09/04/2020 ECU if the following command fails to get the
                    //                connection then haven't yet found a way of
                    //                detecting this so use a timeout
                    // -------------------------------------------------------------
                    connectionHandler.sendEmptyMessageDelayed (MESSAGE_CONNECT_TIME_OUT,
                                                                theTimeout);
                    // -------------------------------------------------------------
                    // 09/04/2020 ECU now try and perform the connection
                    // -------------------------------------------------------------
                    try
                    {
                        // ---------------------------------------------------------
                        connectDeviceMethod.setAccessible (true);
                        connectDeviceMethod.invoke (profileProxy, bluetoothDevice);
                        // ---------------------------------------------------------
                    }
                    catch (InvocationTargetException ex)
                    {
                    }
                    catch (IllegalAccessException ex)
                    {
                    }
                    // -------------------------------------------------------------
                }
                else
                {
                    // -------------------------------------------------------------
                    // 09/04/2020 ECU a device that isn't paired has been specified
                    //                or the connect method has not been validated
                    //                so reset everything
                    // -------------------------------------------------------------
                    resetConnectionData ();
                    // -------------------------------------------------------------
                }
                // -----------------------------------------------------------------
            }
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void debugMessage (String theMessage)
    {
        // -------------------------------------------------------------------------
        // 10/04/2020 ECU optionally display debug message
        // 18/04/2020 ECU added the check on 'null'
        // -------------------------------------------------------------------------
        if (bluetoothConnectionData != null)
        {
            // ---------------------------------------------------------------------
            // 18/04/2020 ECU have data so display the device name
            // ---------------------------------------------------------------------
            Utilities.debugMessage (TAG,String.format ("%s \'%s\'",theMessage,bluetoothConnectionData.deviceName));
            // ---------------------------------------------------------------------
        }
        else
        {
            // ---------------------------------------------------------------------
            // 18/04/2020 ECU no data so just display the message
            // ---------------------------------------------------------------------
            Utilities.debugMessage (TAG,theMessage);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    boolean enableBluetooth ()
    {
        // -------------------------------------------------------------------------
        // 06/04/2020 ECU created to check if the bluetooth adapter is enabled - if
        //                not then it enables it.
        //
        //                returns
        //                      true ............... bluetooth is enabled or has
        //                                           been enabled
        //                      false .............. failed to enable bluetooth
        //--------------------------------------------------------------------------
        // 06/04/2020 ECU get the current bluetooth adapter
        // -------------------------------------------------------------------------
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // -------------------------------------------------------------------------
        // 08/04/2020 ECU remember the state of the bluetooth
        // -------------------------------------------------------------------------
        bluetoothState = bluetoothAdapter.isEnabled ();
        // -------------------------------------------------------------------------
        // 06/04/2020 ECU check if the bluetooth adapter is enabled
        // -------------------------------------------------------------------------
        if (!bluetoothAdapter.isEnabled ())
        {
            // ---------------------------------------------------------------------
            // 06/04/2020 ECU the adapter is not enabled so enable it
            // ---------------------------------------------------------------------
            if (!bluetoothAdapter.enable ())
            {
                // -----------------------------------------------------------------
                // 06/04/2020 ECU cannot enable so return this fact
                // -----------------------------------------------------------------
                return false;
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        }
        else
        {
            // ---------------------------------------------------------------------
            // 08/04/2020 ECU the adapter is already enabled so want to call the
            //                method that would be called when a 'state on' event is
            //                received
            // ---------------------------------------------------------------------
            onBluetoothConnected ();
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 06/04/2020 ECU bluetooth is enabled
        // -------------------------------------------------------------------------
        return true;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    private static BluetoothDevice findBondedDeviceByName (BluetoothAdapter theAdapter, String theName)
    {
        // -------------------------------------------------------------------------
        // 04/04/2020 ECU search the 'bonded devices' for the specified on
        // -------------------------------------------------------------------------
        for (BluetoothDevice device : getBondedDevices (theAdapter))
        {
            // ---------------------------------------------------------------------
            if (theName.matches (device.getName()))
            {
                // -----------------------------------------------------------------
                // 04/04/2020 ECU the required device has been found so return the
                //                corresponding 'bluetooth device'
                // -----------------------------------------------------------------
                return device;
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 04/04/2020 ECU indicate that the device cannot be found
        // -------------------------------------------------------------------------
        return null;
        // ------------------------------------------------------------------------
    }
    // =============================================================================
    private Method getA2dpMethod (String theRequiredMethod)
    {
        // -------------------------------------------------------------------------
        // 07/04/2020 ECU get the reflection method that is used to perform the
        //                disconnection
        // -------------------------------------------------------------------------
        try
        {
            // ---------------------------------------------------------------------
            // 07/04/2020 ECU return the method to perform the connection
            // ----------------------------------------------------------------------
            return BluetoothA2dp.class.getDeclaredMethod (theRequiredMethod, BluetoothDevice.class);
            // ----------------------------------------------------------------------
        }
        catch (NoSuchMethodException theException)
        {
            // ---------------------------------------------------------------------
            // 04/04/2020 ECU the required method cannot be found so indicate this
            // ---------------------------------------------------------------------
            return null;
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    private static Set<BluetoothDevice> getBondedDevices (BluetoothAdapter adapter)
    {
        // -------------------------------------------------------------------------
        // 04/04/2020 ECU return the list of bonded devices - if none found then
        //                return a 'hashset' so that the caller does not have
        //                to check for 'null'.
        // -------------------------------------------------------------------------
        Set<BluetoothDevice> results = adapter.getBondedDevices();
        // -------------------------------------------------------------------------
        if (results == null)
        {
            // ---------------------------------------------------------------------
            // 04/04/2020 ECU created a empty set so that the caller does not have
            //                to check for 'null'
            // --------------------------------------------------------------------
            results = new HashSet<BluetoothDevice>();
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 04/04/2020 ECU return the retrieved, or generate, set of bonded devices
        // -------------------------------------------------------------------------
        return results;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void invokeConnectMethod ()
    {
        // -------------------------------------------------------------------------
        // 08/04/2020 ECU invoke the 'connection actions'
        // -------------------------------------------------------------------------
        bluetoothConnectionData.invokeConnectMethod ();
        //  -----------------------------------------------------------------------
    }
    // =============================================================================
    public static void invokeConnectFailedMethod ()
    {
        // -------------------------------------------------------------------------
        // 09/04/2020 ECU invoke the 'connection actions'
        // -------------------------------------------------------------------------
        bluetoothConnectionData.invokeConnectFailedMethod();
        //  -----------------------------------------------------------------------
    }
    // =============================================================================
    public static void invokeDisconnectMethod ()
    {
        // -------------------------------------------------------------------------
        // 08/04/2020 ECU invoke the 'disconnection actions'
        // 09/04/2020 ECU check that there is a connection to disconnect
        // -------------------------------------------------------------------------
        if (bluetoothConnectionData != null)
        {
            bluetoothConnectionData.invokeDisconnectMethod ();
            // ---------------------------------------------------------------------
            // 09/04/2020 ECU indicate that device has been disconnected
            // ---------------------------------------------------------------------
            resetConnectionData ();
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void invokeDisconnectFailedMethod ()
    {
        // -------------------------------------------------------------------------
        // 09/04/2020 ECU invoke the 'connection actions'
        // -------------------------------------------------------------------------
        bluetoothConnectionData.invokeDisconnectFailedMethod();
        //  -----------------------------------------------------------------------
    }
    // =============================================================================
    private boolean isDeviceConnected (BluetoothA2dp theProfileProxy,String theDeviceName)
    {
        // -------------------------------------------------------------------------
        // 08/04/2020 ECU check if the specified device is already connected to
        //                the profile
        // -------------------------------------------------------------------------
        List<BluetoothDevice> devices = theProfileProxy.getConnectedDevices ();
        // -------------------------------------------------------------------------
        // 08/04/2020 ECU loop through to see if the device is connected
        // -------------------------------------------------------------------------
        for (BluetoothDevice device : devices)
        {
            // ---------------------------------------------------------------------
            if (theDeviceName.equals(device.getName()))
            {
                // -----------------------------------------------------------------
                // 08/04/2020 ECU the device is connected
                // -----------------------------------------------------------------
                return true;
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 08/04/2020 ECU device is not connected
        // -------------------------------------------------------------------------
        return false;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    private IntentFilter getFilter ()
    {
        // -------------------------------------------------------------------------
        // 04/04/2020 ECU set up the filter for the receiver to indicate which
        //                events are of interest
        // -------------------------------------------------------------------------
        IntentFilter localFilter = new IntentFilter ();
        // -------------------------------------------------------------------------
        for (String action : actions)
        {
            localFilter.addAction (action);
        }
        // -------------------------------------------------------------------------
        // 04/04/2020 ECU return the filter to be used
        // -------------------------------------------------------------------------
        return localFilter;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void setSpeakerMode (boolean theConnectFlag)
    {
        // -------------------------------------------------------------------------
        // 11/04/2020 ECU created to set the speaker system into the correct mode
        //                a connection by this service and to restore the existing
        //                mode when disconnection occurs
        //
        //                  theConnectFlag   true .............. connect
        //                                   false ............. disconnect
        // ------------------------------------------------------------------------
        AudioManager audioManager = (AudioManager) getSystemService (Context.AUDIO_SERVICE);
        // ------------------------------------------------------------------------
        // 11/04/2020 ECU check whether called for a connection or disconnection
        // ------------------------------------------------------------------------
        if (theConnectFlag)
        {
            // --------------------------------------------------------------------
            // 11/04/2020 ECU connection so want the current mode set after saving
            //                the current setting
            // --------------------------------------------------------------------
            // 11/04/2020 ECU remember the current state of the speaker system
            // --------------------------------------------------------------------
            audioManagerCurrentMode = audioManager.getMode ();
            // --------------------------------------------------------------------
            // 11/04/2020 ECU now set the system to the required state if not
            //                already in that mode
            // --------------------------------------------------------------------
            if (audioManagerCurrentMode == AudioManager.MODE_NORMAL)
            {
                // ----------------------------------------------------------------
                // 11/04/2020 ECU the system is in the required state so no need
                //                to do anything other than rest the saved mode
                //                to let the 'disconnect' side know that nothing
                //                is to be done at that stage
                // ----------------------------------------------------------------
                audioManagerCurrentMode = StaticData.NOT_SET;
                // ----------------------------------------------------------------
            }
            else
            {
                // ----------------------------------------------------------------
                // 11/04/2020 ECU the mode is not as wanted so set it to it
                // ----------------------------------------------------------------
                audioManager.setMode (AudioManager.MODE_NORMAL);
                // ----------------------------------------------------------------
            }
        }
        else
        {
            // --------------------------------------------------------------------
            // 11/04/2020 ECU disconnection so want the audio manager set back to
            //                the saved value
            // ---------------------------------------------------------------------
            // 11/04/2020 ECU check if need to do anything
            // ---------------------------------------------------------------------
            if (audioManagerCurrentMode != StaticData.NOT_SET)
            {
                // -----------------------------------------------------------------
                // 11/04/2020 ECU need to set the mode back to that saved
                // -----------------------------------------------------------------
                audioManager.setMode (audioManagerCurrentMode);
                // -----------------------------------------------------------------
                // 11/04/2020 ECU and rest the stored value to indicate 'done'
                // -----------------------------------------------------------------
                audioManagerCurrentMode = StaticData.NOT_SET;
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void resetConnectionData ()
    {
        // --------------------------------------------------------------------------
        // 09/04/2020 ECU reset any data associated with the connection
        // --------------------------------------------------------------------------
        bluetoothConnectionData = null;
        // --------------------------------------------------------------------------
    }
    // ==============================================================================
    public static interface Callback
    {
        // -------------------------------------------------------------------------
        public void     onBluetoothConnected ();
        public void     onBluetoothError ();
        public String   onDeviceName ();
        public void     onFinishActivity ();
        // ------------------------------------------------------------------------
    }
    // ============================================================================

    // =============================================================================
    class ConnectionHandler extends Handler
    {
        // -------------------------------------------------------------------------
        // 06/04/2020 ECU created to handle communication with this service
        // -------------------------------------------------------------------------
        @Override
        public void handleMessage (Message theMessage)
        {
            // ---------------------------------------------------------------------
            // 06/04/2020 ECU process the message
            // ---------------------------------------------------------------------
            switch (theMessage.what)
            {
                // ----------------------------------------------------------------
                case StaticData.MESSAGE_BLUETOOTH_CONNECT:
                    // ------------------------------------------------------------
                    // 06/04/2020 ECU connect to the specified device
                    // 08/04/2020 ECU store for later use
                    // 09/04/2020 ECU check that able to connect
                    //            ECU added the timeout
                    // -------------------------------------------------------------
                    if (bluetoothConnectionData == null)
                    {
                        // --------------------------------------------------------
                        bluetoothConnectionData = (BluetoothConnectionData) theMessage.obj;
                        // --------------------------------------------------------
                        // 10/04/2020 ECU optionally display debug message
                        // --------------------------------------------------------
                        debugMessage ("Connect : connect to");
                        // --------------------------------------------------------
                        ConnectDevice (bluetoothConnectionData.deviceName,bluetoothConnectionData.connectTimeout);
                        // --------------------------------------------------------
                    }
                    else
                    {
                        // --------------------------------------------------------
                        // 10/04/2020 ECU optionally display debug message
                        // --------------------------------------------------------
                        debugMessage ("Connect : already connected to");
                        // --------------------------------------------------------
                        MessageHandler.popToastAndSpeak ("Already connected to " + bluetoothConnectionData.deviceName);
                        // --------------------------------------------------------
                        // 11/04/2020 ECU although could not take any actions -
                        //                still need to indicate that the 'action'
                        //                has finished
                        // ---------------------------------------------------------
                        IndicateActionFinished ();
                        // ---------------------------------------------------------
                    }
                    break;
                // ----------------------------------------------------------------
                case StaticData.MESSAGE_BLUETOOTH_DISCONNECT:
                    // ------------------------------------------------------------
                    // 07/04/2020 ECU disconnect the currently connected device
                    // ------------------------------------------------------------
                    if (disconnectDeviceMethod != null && bluetoothConnectionData != null)
                    {
                        // --------------------------------------------------------
                        // 10/04/2020 ECU optionally display debug message
                        // --------------------------------------------------------
                        debugMessage ("Disconnect : disconnect from");
                        // -------------------------------------------------------------
                        // 10/04/2020 ECU if the following command fails to get the
                        //                disconnection then haven't yet found a way of
                        //                detecting this so use a timeout
                        // -------------------------------------------------------------
                        connectionHandler.sendEmptyMessageDelayed (MESSAGE_DISCONNECT_TIME_OUT,
                                                                   bluetoothConnectionData.disconnectionTimeout);
                        // --------------------------------------------------------
                        try
                        {
                            // ----------------------------------------------------
                            disconnectDeviceMethod.setAccessible (true);
                            disconnectDeviceMethod.invoke (profileProxy, bluetoothDevice);
                            // ---------------------------------------------------
                        }
                        catch (InvocationTargetException ex)
                        {
                        }
                        catch (IllegalAccessException ex)
                        {
                        }
                        // ---------------------------------------------------------
                    }
                    else
                    {
                        // ---------------------------------------------------------
                        // 11/04/2020 ECU although could not take any actions -
                        //                still need to indicate that the 'action'
                        //                has finished
                        // ---------------------------------------------------------
                        IndicateActionFinished ();
                        // ---------------------------------------------------------
                    }
                    // -------------------------------------------------------------
                    break;
                // -----------------------------------------------------------------
                case MESSAGE_ACTION_CONNECT_METHOD:
                    // -------------------------------------------------------------
                    // 10/04/2020 ECU optionally display debug message
                    // -------------------------------------------------------------
                    debugMessage ("Connection Method");
                    // -------------------------------------------------------------
                    // 08/04/2020 ECU invoke the connection mode
                    // -------------------------------------------------------------
                    invokeConnectMethod ();
                    // -------------------------------------------------------------
                    break;
                // -----------------------------------------------------------------
                case MESSAGE_ACTION_DISCONNECT_METHOD:
                    // --------------------------------------------------------
                    // 10/04/2020 ECU optionally display debug message
                    // --------------------------------------------------------
                    debugMessage ("Disconnection Method");
                    // -------------------------------------------------------------
                    // 08/04/2020 ECU invoke the disconnection mode
                    // -------------------------------------------------------------
                    invokeDisconnectMethod ();
                    // -------------------------------------------------------------
                    break;
                // -----------------------------------------------------------------
                case MESSAGE_CONNECT_TIME_OUT:
                    // -------------------------------------------------------------
                    // 09/04/2020 ECU this message is received if the connection
                    //                attempt failed
                    // -------------------------------------------------------------
                    // 10/04/2020 ECU optionally display debug message
                    // -------------------------------------------------------------
                    debugMessage ("Connection timeout for");
                    // -------------------------------------------------------------
                    // 09/04/2020 ECU invoke the associated method
                    // -------------------------------------------------------------
                    invokeConnectFailedMethod ();
                    // -------------------------------------------------------------
                    // 09/04/2020 ECU reset connection data
                    // -------------------------------------------------------------
                    resetConnectionData ();
                    // -------------------------------------------------------------
                    break;
                // -----------------------------------------------------------------
                // -----------------------------------------------------------------
                case MESSAGE_DISCONNECT_TIME_OUT:
                    // -------------------------------------------------------------
                    // 09/04/2020 ECU this message is received if the disconnection
                    //                attempt failed
                    // -------------------------------------------------------------
                    // 10/04/2020 ECU optionally display debug message
                    // -------------------------------------------------------------
                    debugMessage ("Disconnection timeout for");
                    // -------------------------------------------------------------
                    // 09/04/2020 ECU invoke the associated method
                    // -------------------------------------------------------------
                    invokeDisconnectFailedMethod ();
                    // -------------------------------------------------------------
                    // 09/04/2020 ECU reset connection data
                    // -------------------------------------------------------------
                    resetConnectionData ();
                    // -------------------------------------------------------------
                    break;
                // -----------------------------------------------------------------
                default:
                    break;
                // -----------------------------------------------------------------
            }
        }
    }
    // =============================================================================

    // =============================================================================
    // Externally called static methods
    // =============================================================================
    public static void ProcessAction (Context theContext)
    {
        // -------------------------------------------------------------------------
        // 09/04/2020 ECU created to disconnect from the currently connected
        //                speaker
        // 10/05/2020 ECU if the service is not running then take no action
        // -------------------------------------------------------------------------
        if (Utilities.isServiceRunning (theContext,BluetoothConnectorService.class))
        {
            // ---------------------------------------------------------------------
            // 10/05/2020 ECU the service is running so can process the command
            // ---------------------------------------------------------------------
            BluetoothConnectorService.connectionHandler.sendEmptyMessage (StaticData.MESSAGE_BLUETOOTH_DISCONNECT);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void ProcessAction (Context theContext,String theSpeakerName)
    {
        // -------------------------------------------------------------------------
        // 09/04/2020 ECU created to connect to the specified speaker
        // -------------------------------------------------------------------------
        // 09/04/2020 ECU call the master method and indicate that the default delay
        //                is to be used
        // -------------------------------------------------------------------------
        ProcessAction (theContext,theSpeakerName,null,null);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void ProcessAction (Context theContext,String theSpeakerName,String theDelay)
    {
        // -------------------------------------------------------------------------
        // 09/04/2020 ECU created to connect to the specified speaker
        // -------------------------------------------------------------------------
        // 09/04/2020 ECU call the master method and indicate that the default delay
        //                is to be used
        // -------------------------------------------------------------------------
        ProcessAction (theContext,theSpeakerName,theDelay,null);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void ProcessAction (Context theContext,String theSpeakerName,String theDelay,String theTimeout)
    {
        // -------------------------------------------------------------------------
        // 09/04/2020 ECU created to connect to the specified speaker
        // -------------------------------------------------------------------------
        // 09/04/2020 ECU set up the delay for the connection method
        // -------------------------------------------------------------------------
        int localDelay = Utilities.getIntFromString (theDelay,CONNECTION_DELAY);
        // -------------------------------------------------------------------------
        // 09/04/2020 ECU set up the connection timeout
        // -------------------------------------------------------------------------
        int localTimeout = Utilities.getIntFromString (theTimeout,CONNECTION_TIMEOUT);
        // -------------------------------------------------------------------------
        BluetoothConnectionData deviceData = new BluetoothConnectionData (theSpeakerName,
                new MethodDefinition<BluetoothConnectorService> (BluetoothConnectorService.class,"Connected"),localDelay,
                new MethodDefinition<BluetoothConnectorService> (BluetoothConnectorService.class,"ConnectFailure"),localTimeout,
                new MethodDefinition<BluetoothConnectorService> (BluetoothConnectorService.class,"Disconnected"),0,
                new MethodDefinition<BluetoothConnectorService> (BluetoothConnectorService.class,"DisconnectFailure"),localTimeout);
        // -------------------------------------------------------------------------
        // 10/05/2020 ECU need to check if the service is running and that there is
        //                a message handler ready to process this request
        // -------------------------------------------------------------------------
        if (Utilities.isServiceRunning (theContext,BluetoothConnectorService.class))
        {
            // ---------------------------------------------------------------------
            // 10/05/2020 ECU the service is up and running so the command can be
            //                processed
            // ---------------------------------------------------------------------
            Message localMessage
                = BluetoothConnectorService.connectionHandler.obtainMessage (StaticData.MESSAGE_BLUETOOTH_CONNECT,deviceData);
            BluetoothConnectorService.connectionHandler.sendMessage (localMessage);
            // ---------------------------------------------------------------------
        }
        else
        {
            // ---------------------------------------------------------------------
            // 10/05/2020 ECU need to start the service in order that the action
            //                can be processed
            // ---------------------------------------------------------------------
            Intent localIntent = new Intent (theContext,BluetoothConnectorService.class);
            // ---------------------------------------------------------------------
            // 10/05/2020 ECU store the data that is to be processed when the service
            //                is up and running
            // ---------------------------------------------------------------------
            localIntent.putExtra (StaticData.PARAMETER_DATA,deviceData);
            // ---------------------------------------------------------------------
            theContext.startService (localIntent);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void Connected (int theDummy)
    {
        // -------------------------------------------------------------------------
        // 09/04/2020 ECU indicate that the 'action' has been processed
        // 11/04/2020 ECU changed to use new method
        // -------------------------------------------------------------------------
        IndicateActionFinished ();
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void ConnectFailure (int theDummy)
    {
        // -------------------------------------------------------------------------
        // 09/04/2020 ECU indicate that the 'action' has been processed
        // 11/04/2020 ECU changed to use new method
        // -------------------------------------------------------------------------
        IndicateActionFinished ();
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void Disconnected (int theDummy)
    {
        // -------------------------------------------------------------------------
        // 09/04/2020 ECU indicate that the 'action' has been processed
        // 11/04/2020 ECU changed to use new method
        // -------------------------------------------------------------------------
        IndicateActionFinished ();
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void DisconnectFailure (int theDummy)
    {
        // -------------------------------------------------------------------------
        // 10/04/2020 ECU indicate that the 'action' has been processed
        // 11/04/2020 ECU changed to use new method
        // -------------------------------------------------------------------------
        IndicateActionFinished ();
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    static void IndicateActionFinished ()
    {
        // -------------------------------------------------------------------------
        // 11/04/2020 ECU created to indicate that the 'action' is finished
        // 19/05/2020 ECU changed to use new method
        // -------------------------------------------------------------------------
        Utilities.actionIsFinished ();
        // -------------------------------------------------------------------------
    }
    // =============================================================================

    // ==============================================================================
    public static Intent updateIntent (Object theArgument)
    {
        // ------------------------------------------------------------------------
        // 11/08/2020 ECU created to modify the supplied Intent
        // ------------------------------------------------------------------------
        return ((Intent) theArgument).putExtra (StaticData.PARAMETER_CONNECTED_DEVICES,true);
        // ------------------------------------------------------------------------
    }
    // ==============================================================================
    public static boolean validation (int theArgument)
    {
        // ------------------------------------------------------------------------
        // 11/08/2020 ECU created to check if this service is allowed to run
        // ------------------------------------------------------------------------
        return StaticData.BLUETOOTH_CONNECTOR_SERVICE;
        // ------------------------------------------------------------------------
    }
    // =============================================================================
}

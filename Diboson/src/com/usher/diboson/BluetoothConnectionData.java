package com.usher.diboson;

import java.io.Serializable;
import java.lang.reflect.Method;

public class BluetoothConnectionData implements Serializable
{
    // =============================================================================
    private static final long serialVersionUID = 1L;
    // =============================================================================
    // 06/04/2020 ECU declare the name of the bluetooth device to connect to
    // -----------------------------------------------------------------------------
    public String               deviceName                      = null;
    // -----------------------------------------------------------------------------
    // 06/04/2020 ECU declare the definitions that will be called when the device
    //                is connected
    // 09/04/2020 ECU added 'connectFailed....'
    // 10/04/2020 ECU added 'disconnectFailed....'
    // -----------------------------------------------------------------------------
    public MethodDefinition<?>  connectMethodDefinition 	        = null;
    public MethodDefinition<?>  connectFailedMethodDefinition 	    = null;
    public MethodDefinition<?>  disconnectMethodDefinition 	        = null;
    public MethodDefinition<?>  disconnectFailedMethodDefinition    = null;
    // -----------------------------------------------------------------------------
    // 08/04/2020 ECU set up the delays when the event happens (in mS)
    // 09/04/2020 ECU added the timeout to detect connection failures (in mS)
    // 10/04/2020 ECU added the timeout to detect disconnection failures (in mS)
    // -----------------------------------------------------------------------------
    public int                  connectionDelay                 = StaticData.NOT_SET;
    public int                  connectTimeout                  = StaticData.NOT_SET;
    public int                  disconnectionDelay              = StaticData.NOT_SET;
    public int                  disconnectionTimeout            = StaticData.NOT_SET;
    // =============================================================================

    // =============================================================================
    public BluetoothConnectionData (String                  theDeviceName,
                                    MethodDefinition<?>     theConnectMethodDefinition,
                                    int                     theConnectionDelay,
                                    MethodDefinition<?>     theConnectFailedMethodDefinition,
                                    int                     theConnectTimeout,
                                    MethodDefinition<?>     theDisconnectMethodDefinition,
                                    int                     theDisconnectionDelay,
                                    MethodDefinition<?>     theDisconnectFailedMethodDefinition,
                                    int                     theDisconnectionTimeout)
    {
        // -------------------------------------------------------------------------
        // 06/04/2020 ECU public constructor - copy across the data
        // 09/04/2020 ECU set up the connect timeout
        // -------------------------------------------------------------------------
        deviceName                              = theDeviceName;
        connectionDelay                         = theConnectionDelay;
        connectMethodDefinition                 = theConnectMethodDefinition;
        connectFailedMethodDefinition           = theConnectFailedMethodDefinition;
        connectTimeout                          = theConnectTimeout;
        disconnectionDelay                      = theDisconnectionDelay;
        disconnectMethodDefinition              = theDisconnectMethodDefinition;
        disconnectFailedMethodDefinition        = theDisconnectFailedMethodDefinition;
        disconnectionTimeout                    = theDisconnectionTimeout;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void invokeConnectMethod ()
    {
        // -------------------------------------------------------------------------
        // 16/05/2020 ECU added the check on null
        // -------------------------------------------------------------------------
        if (connectMethodDefinition != null)
        {
            // ---------------------------------------------------------------------
            Method localMethod = connectMethodDefinition.ReturnMethod (0);
            // ---------------------------------------------------------------------
            // 08/04/2020 ECU invoke the 'connection actions'
            // ---------------------------------------------------------------------
            try
            {
                Utilities.invokeMethod (localMethod,new Object [] {0});
            }
            catch (Exception theException)
            {
            }
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void invokeConnectFailedMethod ()
    {
        // -------------------------------------------------------------------------
        // 16/05/2020 ECU added the check on null
        // -------------------------------------------------------------------------
        if (connectFailedMethodDefinition != null)
        {
            // ---------------------------------------------------------------------
            Method localMethod = connectFailedMethodDefinition.ReturnMethod (0);
            // ---------------------------------------------------------------------
            // 09/04/2020 ECU invoke the 'connection failed actions'
            // ---------------------------------------------------------------------
            try
            {
                Utilities.invokeMethod (localMethod,new Object [] {0});
            }
            catch (Exception theException)
            {
            }
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void invokeDisconnectMethod ()
    {
        // -------------------------------------------------------------------------
        // 16/05/2020 ECU added the check on null
        // -------------------------------------------------------------------------
        if (disconnectMethodDefinition != null)
        {
            // ---------------------------------------------------------------------
            Method localMethod = disconnectMethodDefinition.ReturnMethod (0);
            // ---------------------------------------------------------------------
            // 08/04/2020 ECU invoke the 'disconnection actions'
            // ---------------------------------------------------------------------
            try
            {
                Utilities.invokeMethod (localMethod,new Object [] {0});
            }
            catch (Exception theException)
            {
            }
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void invokeDisconnectFailedMethod ()
    {
        // -------------------------------------------------------------------------
        // 16/05/2020 ECU added the check on null
        // -------------------------------------------------------------------------
        if (disconnectFailedMethodDefinition != null)
        {
            // ---------------------------------------------------------------------
            Method localMethod = disconnectFailedMethodDefinition.ReturnMethod (0);
            // ---------------------------------------------------------------------
            // 10/04/2020 ECU invoke the 'disconnection failed actions'
            // ---------------------------------------------------------------------
            try
            {
                Utilities.invokeMethod (localMethod,new Object [] {0});
            }
            catch (Exception theException)
            {
            }
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
}

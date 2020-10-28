package com.usher.diboson;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;

public class BluetoothTracking
{
    // =============================================================================
    // 15/04/2020 ECU created to monitor the bluetooth devices that are detected by
    //                this device
    // 17/04/2020 ECU changed to use 'PublicData.storedData.storedBluetoothDevices'
    //                rather than 'storedDevices'
    // =============================================================================
    private static final String TAG = "BluetoothTracking";
    // =============================================================================

    // =============================================================================
    private static final int    LOCATION_SERVICES_DELAY     =   3 * StaticData.ONE_SECOND;  // 3 seconds
    private static final int    MINIMUM_DISTANCE            =   5;
    private static final int    MINIMUM_TIME                =   10000;
    private static final long   REANNOUNCEMENT_TIME         =   StaticData.MILLISECONDS_PER_HOUR;
    // =============================================================================

    // =============================================================================
    // 15/04/2020 ECU declare the required data
    // -----------------------------------------------------------------------------
    private static  long                            currentTime;
    public  static  boolean                         initialised         = false;
    private static  Location                        location;
    private static  LocationListener                locationListener;
    private static  LocationManager                 locationManager;
    // =============================================================================

    // =============================================================================
    public static void DiscoveredDevices (List<BluetoothDevice> theDevices)
    {
        // -------------------------------------------------------------------------
        // 15/04/2020 ECU created to process bluetooth devices which have been
        //                discovered
        // -------------------------------------------------------------------------
        if (initialised)
        {
            // ---------------------------------------------------------------------
            // 15/04/2020 ECU store the current time
            // ---------------------------------------------------------------------
            currentTime = Utilities.getAdjustedTime (true);
            // ---------------------------------------------------------------------
            for (BluetoothDevice discoveredDevice : theDevices)
            {
                // -----------------------------------------------------------------
                // 15/04/2020 ECU process the device
                // 19/09/2020 ECU put in the check on null because on a restart of
                //                the app by the OS then was getting an uncaught
                //                exception
                // -----------------------------------------------------------------
                if (discoveredDevice != null)
                    ProcessTheDevice (discoveredDevice);
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
            // 22/07/2020 ECU want to check if any of the stored devices have
            //                disappeared from the 'discovered list'
            //            ECU define the time after the last time it appeared in the
            //                list when contact is deemed to have been lost
            // ---------------------------------------------------------------------
            long lostContactTime = PublicData.storedData.bluetoothTrackingLostContactTime * StaticData.MILLISECONDS_PER_MINUTE;
            // ----------------------------------------------------------------------
            for (BluetoothTrackingData device : PublicData.storedData.storedBluetoothDevices)
            {
                // -----------------------------------------------------------------
                // 22/07/2020 ECU only interested in devices which are still in
                //                contact
                // -----------------------------------------------------------------
                if (device.contactMade)
                {
                    // -------------------------------------------------------------
                    boolean deviceInList = false;
                    // -------------------------------------------------------------
                    for (BluetoothDevice discoveredDevice : theDevices)
                    {
                        // ---------------------------------------------------------
                        if (device.macAddress.equals (discoveredDevice.getAddress()))
                        {
                            // -----------------------------------------------------
                            // 22/07/2020 ECU the device is still in the list
                            // -----------------------------------------------------
                            deviceInList = true;
                            break;
                            // -----------------------------------------------------
                        }
                        // ---------------------------------------------------------
                    }
                    // -------------------------------------------------------------
                    // 22/07/2020 ECU check if the device has been found
                    // -------------------------------------------------------------
                    if (!deviceInList)
                    {
                        // ---------------------------------------------------------
                        // 22/07/2020 ECU want to increment the number of missed
                        //                discoveries
                        // ---------------------------------------------------------
                        device.HandleMissedDiscoveries (currentTime,lostContactTime);
                        // ---------------------------------------------------------
                    }
                    // -------------------------------------------------------------
                }
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void Finish ()
    {
        // -------------------------------------------------------------------------
        // 15/04/2020 ECU tidy up before finishing
        // -------------------------------------------------------------------------
        if (locationManager != null)
        {
            locationManager.removeUpdates (locationListener);
        }
        // -------------------------------------------------------------------------
        // 16/04/2020 ECU record being finished in the log
        // -------------------------------------------------------------------------
        Utilities.LogToProjectFile (TAG,"Finished");
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void Initialise (Context theContext)
    {
        // -------------------------------------------------------------------------
        // 15/04/2020 ECU initialise the number of stored devices
        // 17/04/2020 ECU changed to use the stored devices
        // -------------------------------------------------------------------------
        if (PublicData.storedData.storedBluetoothDevices == null)
            PublicData.storedData.storedBluetoothDevices = new ArrayList<BluetoothTrackingData> ();
        // -------------------------------------------------------------------------
        // 15/04/2020 ECU set up the listener for location updates
        // -------------------------------------------------------------------------
        locationListener = new locationListener ();
        // -------------------------------------------------------------------------
        // 15/04/2020 ECU get the location manager
        // -------------------------------------------------------------------------
        locationManager = ((LocationManager) theContext.getSystemService (Context.LOCATION_SERVICE));
        // -------------------------------------------------------------------------
        // 15/04/2020 ECU indicate which updates are to be listened for
        // -------------------------------------------------------------------------
        locationManager.requestLocationUpdates (LocationManager.NETWORK_PROVIDER,
                                    MINIMUM_TIME, MINIMUM_DISTANCE, locationListener);
        // -------------------------------------------------------------------------
        // 12/09/2020 ECU put in the check on whether this device has GPS - a problem
        //                highlighted when testing the Fire 7
        // -------------------------------------------------------------------------
        if (locationManager.isProviderEnabled (LocationManager.GPS_PROVIDER))
        {
            locationManager.requestLocationUpdates (LocationManager.GPS_PROVIDER,
                                        MINIMUM_TIME, MINIMUM_DISTANCE, locationListener);
        }
        // -------------------------------------------------------------------------
        // 19/04/2020 ECU check if location services are enabled
        // -------------------------------------------------------------------------
        if (!Utilities.checkForLocationServices (theContext))
        {
            // ---------------------------------------------------------------------
            // 19/04/2020 ECU tell the user that 'location services' need to be
            //                enabled to help with bluetooth tracking
            //            ECU have a bit of a delay before putting up the warning
            //                message so that it doesn't get cleared by the
            //                starting activity
            // ---------------------------------------------------------------------
            MessageHandler.popToastAndSpeakDelayed (theContext.getString (R.string.bluetooth_location_services),
                                                        LOCATION_SERVICES_DELAY);
            // ---------------------------------------------------------------------
            //  19/04/2020 ECU activate settings so that user can enable location
            //                 services
            // ---------------------------------------------------------------------
            Intent settingsIntent = new Intent (Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            theContext.startActivity (settingsIntent);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 15/04/2020 ECU indicate that class is 'good to go'
        // -------------------------------------------------------------------------
        initialised = true;
        // -------------------------------------------------------------------------
        // 16/04/2020 ECU record initialisation in the log
        // -------------------------------------------------------------------------
        Utilities.LogToProjectFile (TAG,BluetoothTrackingData.PrintAll ("Initialised"));
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public static class locationListener implements LocationListener
    {
        // -------------------------------------------------------------------------
        // 12/07/2013 ECU only interested in changes to the location
        // -------------------------------------------------------------------------
        @Override
        public void onLocationChanged (Location theLocation)
        {
            // ---------------------------------------------------------------------
            // 12/06/2013 ECU process the new location
            // ---------------------------------------------------------------------
            location = theLocation;
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        @Override
        public void onProviderDisabled (String provider)
        {
        }
        // -------------------------------------------------------------------------
        @Override
        public void onProviderEnabled (String provider)
        {
        }
        // -------------------------------------------------------------------------
        @Override
        public void onStatusChanged (String provider, int status, Bundle extras)
        {
        }
        // =========================================================================
    }
    // =============================================================================
    static void ProcessTheDevice (BluetoothDevice theDevice)
    {
        // -------------------------------------------------------------------------
        // 15/04/2020 ECU process the newly discovered bluetooth device
        // -------------------------------------------------------------------------
        // 15/04/2020 ECU at this stage only interest if 'phones'
        // -------------------------------------------------------------------------
        if (theDevice.getBluetoothClass().getMajorDeviceClass () == BluetoothClass.Device.Major.PHONE)
        {
            // ---------------------------------------------------------------------
            // 19/04/2020 ECU if 'location services' are not enabled then 'location'
            //                will not be updated
            // ---------------------------------------------------------------------
            BluetoothTrackingData localData = new BluetoothTrackingData (theDevice.getAddress(),
                                                                         theDevice.getName(),
                                                                        (location != null ? location.getLatitude()
                                                                                          : StaticData.NOT_SET),
                                                                        (location != null ? location.getLongitude()
                                                                                          : StaticData.NOT_SET),
                                                                        currentTime);
            // ---------------------------------------------------------------------
            // 15/04/2020 ECU check if the device is already in the list
            // ---------------------------------------------------------------------
            if (PublicData.storedData.storedBluetoothDevices.size() > 0)
            {
                // -----------------------------------------------------------------
                // 22/07/2020 ECU store the time that defines how long the bluetooth
                //                device is detected before the 'contact' is confirmed
                // -----------------------------------------------------------------
                long localContactTime = PublicData.storedData.bluetoothTrackingContactTime * StaticData.MILLISECONDS_PER_MINUTE;
                // -----------------------------------------------------------------
                for (int index = 0; index < PublicData.storedData.storedBluetoothDevices.size(); index++)
                {
                    // -----------------------------------------------------------------
                    // 17/04/2020 ECU do the check using MAC address rather than the
                    //                name which can be changed by the user
                    // -----------------------------------------------------------------
                    if ((PublicData.storedData.storedBluetoothDevices.get (index).macAddress).equals (theDevice.getAddress()))
                    {
                        // ---------------------------------------------------------
                        // 17/04/2020 ECU check whether there is a need to warn that a
                        //                a phone has reappeared
                        // ---------------------------------------------------------
                        if ((currentTime - PublicData.storedData.storedBluetoothDevices.get (index).time) >=
                                REANNOUNCEMENT_TIME)
                        {
                            // -----------------------------------------------------
                            // 17/04/2020 ECU announce the reappearance of a device
                            // 23/07/2020 ECU changed to use the new method
                            // -----------------------------------------------------
                            localData.Notify ("Phone has been Redetected");
                            // -----------------------------------------------------
                        }
                        // ---------------------------------------------------------
                        // 15/04/2020 ECU this device is already so just need to update
                        //                the stored entry
                        // 22/07/2020 ECU changed to use 'Update'
                        // ---------------------------------------------------------
                        PublicData.storedData.storedBluetoothDevices.get (index).Update (localData,localContactTime);
                        // ---------------------------------------------------------
                        // 15/04/2020 ECU nothing more to do
                        // ---------------------------------------------------------
                        return;
                        // ---------------------------------------------------------
                    }
                }
            }
            // ---------------------------------------------------------------------
            // 15/04/2020 ECU the device is not stored so add it
            // ---------------------------------------------------------------------
            PublicData.storedData.storedBluetoothDevices.add (localData);
            // ---------------------------------------------------------------------
            // 15/04/2020 ECU notify the server about the new entry
            // 23/07/2020 ECU changed to use the new method
            // ---------------------------------------------------------------------
            localData.Notify ("New Phone Detected");
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------

        // -------------------------------------------------------------------------
    }
    // =============================================================================
}

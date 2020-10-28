package com.usher.diboson;

import android.content.Context;

import java.io.Serializable;

public class BluetoothTrackingData implements Serializable
{
    // =============================================================================
    private static final long serialVersionUID = 1L;
    // =============================================================================
    // private static final String TAG = "BluetoothTrackingData";
    // =============================================================================

    // =============================================================================
    // =============================================================================

    // =============================================================================
    public boolean  contactConfirmed;
    public boolean  contactMade;
    public double   latitude;
    public double   longitude;
    public String   macAddress;
    public String   name;
    public long     time;
    public long     timeFirstContact;
    // =============================================================================

    // =============================================================================
    // 15/04/2020 ECU declare the data structure that is used for the tracking
    // -----------------------------------------------------------------------------
    public BluetoothTrackingData (String theMacAddress,String theName,double theLatitude,double theLongitude,long theTime)
    {
        // ------------------------------------------------------------------------
        latitude    =   theLatitude;
        longitude   =   theLongitude;
        macAddress  =   theMacAddress;
        name        =   theName;
        time        =   theTime;
        // -------------------------------------------------------------------------
        // 22/07/2020 ECU preset some other variables
        // -------------------------------------------------------------------------
        timeFirstContact = theTime;
        // -------------------------------------------------------------------------
        // 22/07/2020 ECU reset the number of 'missed dDiscoveries' and indicate that
        //                the contact has not exceeded 'CONTACT_TIME'
        // -------------------------------------------------------------------------
        contactConfirmed    = false;
        contactMade         = true;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void displayLocation (Context theContext)
    {
        // -------------------------------------------------------------------------
        // 26/04/2020 ECU created to display the location of this device
        // -------------------------------------------------------------------------
        if (latitude != StaticData.NOT_SET)
        {
            // ---------------------------------------------------------------------
            // 26/04/2020 ECU display the map but only if a location has been set
            // 27/04/2020 ECU changed to use the new method - MAP_ZOOM is the zoom, at
            //                the moment a pin is displayed but no label - do not
            //                know why
            // 14/10/2020 ECU changed to use StaticData.MAP_ZOOM
            // ---------------------------------------------------------------------
            Utilities.displayMap (theContext,latitude,longitude,StaticData.MAP_ZOOM,name);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void HandleMissedDiscoveries (long theCurrentTime,long theLostContactTime)
    {
        // -------------------------------------------------------------------------
        // 22/07/2020 ECU check the time when the contact is deemed to have been lost
        // -------------------------------------------------------------------------
        if ((theCurrentTime - time) >= theLostContactTime)
        {
            // ---------------------------------------------------------------------
            // 22/07/2020 ECU indicate that contact has been lost
            // 03/08/2020 ECU added the resetting of 'confirmed'
            // ---------------------------------------------------------------------
            contactConfirmed    = false;
            contactMade         = false;
            // ---------------------------------------------------------------------
            // 22/07/2020 ECU 'contact has been lost'
            // 23/07/2020 ECU changed to use the new method
            // ---------------------------------------------------------------------
            Notify ("Contact Lost");
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void Notify (String theTitle)
    {
        // -------------------------------------------------------------------------
        // 23/07/2020 ECU created to generate a notification
        // 27/07/2020 ECU added the object
        // 03/08/2020 ECU added the switch on 'contact confirmed'
        // 05/08/2020 ECU changed so that the location can be displayed
        // 29/09/2020 ECU added the 'multiple entry' flag
        // -------------------------------------------------------------------------
        NotificationMessage.Add (theTitle,
                                 Print(),
                                 contactConfirmed ? StaticData.NOTIFICATION_COLOUR_CONTACT
                                                  : StaticData.NOTIFICATION_COLOUR_TRACK,
                                 contactConfirmed ? NotificationMessage.NOTIFICATION_TYPE_TRACKED_CONTACT
                                                  : NotificationMessage.NOTIFICATION_TYPE_TRACKED,
                                 NotificationMessage.NOTIFICATION_MULTIPLE_ENTRY,
                                 StaticData.NEWLINEx2 + MainActivity.activity.getString (R.string.bluetooth_show_location),
                                 this);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public String Print ()
    {
        // -------------------------------------------------------------------------
        // 24/04/2020 ECU remove the terminating 'new line'
        // -------------------------------------------------------------------------
        return "MAC Address : "                 + macAddress    + StaticData.NEWLINE +
                "Name : "                       + name          + StaticData.NEWLINE +
                "Latitude : "                   + latitude      + StaticData.NEWLINE +
                "Longitude : "                  + longitude     + StaticData.NEWLINE +
                "Time of First Contact : "      + PublicData.dateFormatterFull.format (timeFirstContact)
                                                                + StaticData.NEWLINE +
                "Time : "                       + PublicData.dateFormatterFull.format (time)
                                                                + StaticData.NEWLINE +
                "Contact Made : "               + contactMade   + StaticData.NEWLINE +
                "Contact Confirmed : "          + contactConfirmed;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static String PrintAll (String theTitle)
    {
        // -------------------------------------------------------------------------
        // 17/04/2020 ECU print all stored bluetooth devices
        // -------------------------------------------------------------------------
        String localResults = theTitle + StaticData.NEWLINE;
        // -------------------------------------------------------------------------
        if (PublicData.storedData.storedBluetoothDevices.size () > 0)
        {
            // ---------------------------------------------------------------------
            // 17/04/2020 ECU print out the list of discovered devices
            // ---------------------------------------------------------------------
            for (BluetoothTrackingData device : PublicData.storedData.storedBluetoothDevices)
            {
                // -----------------------------------------------------------------
                // 17/04/2020 ECU log this device's details to file
                // 24/04/2020 ECU added the new line
                // -----------------------------------------------------------------
                localResults += device.Print () + StaticData.NEWLINE + StaticData.SEPARATOR;
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        }
        else
        {
            // ---------------------------------------------------------------------
            // 17/04/2020 ECU there are no stored devices
            // ---------------------------------------------------------------------
            localResults += "There are no stored devices";
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 17/04/2020 ECU return the generated results
        // -------------------------------------------------------------------------
        return localResults;
        // -------------------------------------------------------------------------
    }
    // ============================================================================
    public void ProcessNotification (Context theContext)
    {
        // ------------------------------------------------------------------------
        // 27/07/2020 ECU created to handle a click on the notification
        // ------------------------------------------------------------------------
        displayLocation (theContext);
        // ------------------------------------------------------------------------
    }
    // =============================================================================
    public void Update (BluetoothTrackingData theNewData,long theContactTime)
    {
        // -------------------------------------------------------------------------
        // 22/07/2020 ECU created to update the current record with that supplied
        //                and take any actions
        // -------------------------------------------------------------------------
        latitude    = theNewData.latitude;
        longitude   = theNewData.longitude;
        time        = theNewData.time;
        // -------------------------------------------------------------------------
        // 22/07/2020 ECU check whether this is the 'first contact'
        // -------------------------------------------------------------------------
        if (!contactMade)
        {
            // ---------------------------------------------------------------------
            // 22/07/2020 ECU confirm that contact has been made
            // ---------------------------------------------------------------------
            contactMade         = true;
            // ---------------------------------------------------------------------
            // 22/07/2020 ECU record the time of first contact
            // ---------------------------------------------------------------------
            timeFirstContact    = time;
            // ---------------------------------------------------------------------
            // 22/07/2020 ECU indicate that actions will be taken when the 'contact
            //                interval' is exceeded
            // ---------------------------------------------------------------------
            contactConfirmed    = false;
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 22/07/2020 ECU now decide what action is to be taken
        //            ECU added the '....ContactTime'
        // -------------------------------------------------------------------------
        if (!contactConfirmed && ((time - timeFirstContact) >= theContactTime))
        {
            // ---------------------------------------------------------------------
            // 22/07/2020 ECU indicate that required action has been taken
            // ---------------------------------------------------------------------
            contactConfirmed = true;
            // ---------------------------------------------------------------------
            // 22/07/2020 ECU this 'phone' has been in 'contact' for longer than
            //                the defined time so tact action
            // ---------------------------------------------------------------------
            Notify ("Contact Confirmed");
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // ==============================================================================
}

package com.usher.diboson;

import android.content.Context;

import java.io.Serializable;

public class DeviceName implements Serializable
{
    // =============================================================================
    // 16/11/2019 ECU created to be able to add a meaningful name to a device
    // =============================================================================

    // =============================================================================
    private static final long serialVersionUID = 1L;
    // =============================================================================
    private String name;
    private String nameOriginal;
    private String serialNumber;
    // =============================================================================

    // =============================================================================
    // =============================================================================

    // =============================================================================
    public DeviceName (String theCurrentName,String theSerialNumber,String theNewName)
    {
        // -------------------------------------------------------------------------
        // 16/11/2019 ECU public class to create a new record if the specified
        //                details are not already registered
        // --------------------------------------------------------------------------
        name            =   theNewName;
        nameOriginal    =   theCurrentName;
        serialNumber    =   theSerialNumber;
        // --------------------------------------------------------------------------
    }
    // ==============================================================================

    // ==============================================================================
    static int checkForName (String theName,String theSerialNumber)
    {
        // --------------------------------------------------------------------------
        // 16/11/2019 ECU created to check if the current device has a name recorded
        // --------------------------------------------------------------------------
        if (PublicData.deviceNames != null)
        {
            for (int index = 0; index < PublicData.deviceNames.size(); index++)
            {
                // -----------------------------------------------------------------
                // 29/11/2019 ECU it is possible that on old versions of the software
                //                that 'nameOriginal' or 'serialNumber' could be null
                // ------------------------------------------------------------------
                if (PublicData.deviceNames.get (index).nameOriginal != null &&
                    PublicData.deviceNames.get (index).serialNumber != null)
                {
                    // --------------------------------------------------------------
                    // 29/11/2019 ECU now check the name and seial number
                    // --------------------------------------------------------------
                    if (PublicData.deviceNames.get (index).nameOriginal.equalsIgnoreCase(theName) &&
                        PublicData.deviceNames.get (index).serialNumber.equalsIgnoreCase(theSerialNumber))
                    {
                        // ---------------------------------------------------------
                        // 16/11/2019 ECU a match has been found so return the record
                        // ---------------------------------------------------------
                        return index;
                        // ---------------------------------------------------------
                    }
                    // -------------------------------------------------------------
                }
                // -----------------------------------------------------------------
            }
        }
        // -------------------------------------------------------------------------
        // 16/11/2019 ECU indicate that the name is not registered
        // -------------------------------------------------------------------------
        return StaticData.NO_RESULT;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static String getName (String theName,String theSerialNumber)
    {
        // -------------------------------------------------------------------------
        // 16/11/2019 ECU check if the device has a new name registered and if so
        //                then return it. If not then just return the specified
        //                name in 'theName'
        // -------------------------------------------------------------------------
        int localResult = checkForName (theName,theSerialNumber);
        if (localResult != StaticData.NO_RESULT)
        {
            // ---------------------------------------------------------------------
            // 16/11/2019 ECU the name is registered so return it
            // ---------------------------------------------------------------------
                return PublicData.deviceNames.get (localResult).name;
            // ---------------------------------------------------------------------
        }
        else
        {
            // ---------------------------------------------------------------------
            // 16/11/2019 ECU the name is not registered so indicate this fact
            // ---------------------------------------------------------------------
            return theName;
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    String Print ()
    {
        // -------------------------------------------------------------------------
        // 16/11/2019 ECU print details of this record
        // -------------------------------------------------------------------------
        return "Name : "            + name + StaticData.NEWLINE +
               "Original Name : "   + nameOriginal + StaticData.NEWLINE +
               "Serial Number : "   + serialNumber;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static String PrintAll ()
    {
        // -------------------------------------------------------------------------
        // 16/11/2019 ECU print details of all records
        // -------------------------------------------------------------------------
        String localResult = StaticData.BLANK_STRING;
        // -------------------------------------------------------------------------
        if (PublicData.deviceNames != null)
        {
            // ---------------------------------------------------------------------
            for (DeviceName deviceName : PublicData.deviceNames)
            {
                localResult += deviceName.Print() + StaticData.NEWLINE;
            }
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 16/11/2019 ECU return the generated string
        // -------------------------------------------------------------------------
        return localResult;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void refresh (Context theContext)
    {
        // -------------------------------------------------------------------------
        // 18/11/2019 ECU created to refresh the current name of compatible devices
        //                if another attached device has changed a name and this
        //                device has received the updated file with the new names
        //                in
        // ------------------------------------------------------------------------
        // 18/11/2019 ECU scan through all registered and compatible devices
        // ------------------------------------------------------------------------
        if (PublicData.deviceDetails != null)
        {
            Devices localDevice;
            int     localNameIndex;
            // --------------------------------------------------------------------
            // 18/11/2019 ECU loop through each device
            // --------------------------------------------------------------------
            for (int index = 0; index < PublicData.deviceDetails.size(); index++)
            {
                // ----------------------------------------------------------------
                // 18/11/2019 ECU get a local copy of the indexed device
                // ----------------------------------------------------------------
                localDevice = PublicData.deviceDetails.get (index);
                // ----------------------------------------------------------------
                // 18/11/2019 ECU check if this is a compatible device
                // ----------------------------------------------------------------
                if (localDevice.compatible)
                {
                    // ------------------------------------------------------------
                    // 18/11/2019 ECU this is a compatible device so check if its
                    //                name needs to be updated
                    // ------------------------------------------------------------
                    localNameIndex = checkForName (localDevice.nameOriginal,
                                                   localDevice.serialNumber);
                    // ------------------------------------------------------------
                    // 18/11/2019 ECU check if the name has been found - if so then
                    //                change the indexed device name
                    // ------------------------------------------------------------
                    if (localNameIndex != StaticData.NO_RESULT)
                    {
                        // --------------------------------------------------------
                        // 18/11/2019 ECU update the indexed device name
                        // --------------------------------------------------------
                        localDevice.name = PublicData.deviceNames.get (localNameIndex).name;
                        // --------------------------------------------------------
                        // 18/11/2019 ECU update the device record
                        // --------------------------------------------------------
                        PublicData.deviceDetails.set (index,localDevice);
                        // --------------------------------------------------------
                    }
                }
                // ----------------------------------------------------------------
            }
            // --------------------------------------------------------------------
            // 18/11/2019 ECU all devices have been processed so update the disk
            //                version
            // ---------------------------------------------------------------------
            Devices.writeToDisk (theContext);
            // ---------------------------------------------------------------------
        }
        // ------------------------------------------------------------------------
    }
    // =============================================================================
    public static void register (Context theContext, String theName, String theSerialNumber, String theNewName)
    {
        // -------------------------------------------------------------------------
        // 16/11/2019 ECU created to register the new details
        // -------------------------------------------------------------------------
        int deviceNameIndex = checkForName (theName,theSerialNumber);
        // --------------------------------------------------------------------------
        if (deviceNameIndex != StaticData.NO_RESULT)
        {
            // ---------------------------------------------------------------------
            // 16/11/2019 ECU the name is already registered
            // ---------------------------------------------------------------------
            PublicData.deviceNames.get (deviceNameIndex).name = theNewName;
            // ---------------------------------------------------------------------
        }
        else
        {
            // ---------------------------------------------------------------------
            // 16/11/2019 ECU the name is not already registered
            // ---------------------------------------------------------------------
            PublicData.deviceNames.add (new DeviceName (theName,theSerialNumber,theNewName));
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 16/11/2019 ECU write the details to disc
        // -------------------------------------------------------------------------
        AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + theContext.getString (R.string.device_names_file),PublicData.deviceNames);
        // ------------------------------------------------------------------------
    }
    // =============================================================================
}

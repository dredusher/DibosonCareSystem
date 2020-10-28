package com.usher.diboson;

public class TVChannelString
{
    // =============================================================================
    private static final int CHANNEL_NAME   =   0;
    private static final int CHANNEL_NUMBER =   1;
    // =============================================================================
    String  channelName;
    int     channelNumber;
    // =============================================================================

    // =============================================================================
    public TVChannelString (String theString)
    {
        // -------------------------------------------------------------------------
        // 06/07/2020 ECU split the string into it's components
        //
        //                 format is :-
        //                    <channel name><ACTION_DELIMITER><channel number>
        //
        //                  e.g. 'BBC ONE:1'
        // -------------------------------------------------------------------------
        String [] components = theString.split (StaticData.ACTION_DELIMITER);
        // -------------------------------------------------------------------------
        // 06/07/2020 ECU check if the format is correct
        // -------------------------------------------------------------------------
        if (components.length == 2)
        {
            // ---------------------------------------------------------------------
            // 06/07/2020 ECU the format is correct so store the components
            // ---------------------------------------------------------------------
            channelName     = components [CHANNEL_NAME];
            // ---------------------------------------------------------------------
            // 06/07/2020 ECU make sure that there are no exceptions caused by the
            //                number format
            // ---------------------------------------------------------------------
            try
            {
                channelNumber   = Integer.parseInt (components [CHANNEL_NUMBER]);
            }
            catch (NumberFormatException theException)
            {
                // -----------------------------------------------------------------
                // 06/07/2020 ECU the format of the number was invalid
                // -----------------------------------------------------------------
                channelNumber = StaticData.NOT_SET;
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        }
        else
        {
            // --------------------------------------------------------------------
            // 06/07/2020 ECU the format is wrong so flag that fact
            // --------------------------------------------------------------------
            channelName     = null;
            channelNumber   = StaticData.NOT_SET;
            // ---------------------------------------------------------------------
        }
    }
    // =============================================================================
    boolean validFormat ()
    {
        // -------------------------------------------------------------------------
        // 06/07/2020 ECU return whether the input string was of the correct format
        // -------------------------------------------------------------------------
        return (channelNumber != StaticData.NOT_SET);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
}

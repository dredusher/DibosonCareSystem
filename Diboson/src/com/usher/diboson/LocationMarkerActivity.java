package com.usher.diboson;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import java.util.ArrayList;

public class LocationMarkerActivity extends DibosonActivity
{
    // ==============================================================================
    ListViewSelector 	listViewSelector;
    TextView            markerDetails;
    int                 markerIndex;
    boolean             paused = false;
    // ==============================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // -------------------------------------------------------------------------
        if (savedInstanceState == null &&
                (PublicData.storedData.locationActions != null) &&
                    (PublicData.storedData.locationActions.size() > 0))
        {
            // ---------------------------------------------------------------------
            // 12/10/2020 ECU the activity has been created anew
            // ---------------------------------------------------------------------
            Utilities.SetUpActivity (this,true);
            // ---------------------------------------------------------------------
            listViewSelector = new ListViewSelector (this,
                                                     R.layout.location_marker_row,
                                                     Utilities.createAMethod (LocationMarkerActivity.class, "PopulateTheList"),
                                                     false,
                                                     StaticData.NO_HANDLING_METHOD,
                                                     Utilities.createAMethod (LocationMarkerActivity.class, "ProcessActions",0),
                                                     Utilities.createAMethod (LocationMarkerActivity.class, "Editor",0),
                                                     getString (R.string.actions),
                                                     Utilities.createAMethod (LocationMarkerActivity.class, "AddActions",0),
                                                     Utilities.createAMethod (LocationMarkerActivity.class, "DisplayMap",0),
                                                     Utilities.createAMethod (LocationMarkerActivity.class, "SwipeAction",0));
            // --------------------------------------------------------------------
        }
        else
        {
            // ---------------------------------------------------------------------
            // 12/10/2020 ECU the activity has been recreated after having been
            //                destroyed by the Android OS
            // ---------------------------------------------------------------------
            finish ();
            // ---------------------------------------------------------------------
        }
    }
    // =============================================================================
    @Override
    protected void onPause()
    {
        // -------------------------------------------------------------------------
        // 15/10/2020 ECU indicate that the activity has been paused
        // -------------------------------------------------------------------------
        paused = true;
        // -------------------------------------------------------------------------
        // 05/04/2015 ECU pause the activity
        // -------------------------------------------------------------------------
        super.onPause ();
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    protected void onResume()
    {
        if (paused)
        {
            paused = false;
            // -------------------------------------------------------------------------
            listViewSelector.refresh();
        }
        // -------------------------------------------------------------------------
        super.onResume();
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void AddActions (int theIndex)
    {
        // -------------------------------------------------------------------------
        markerIndex = theIndex;
        // -------------------------------------------------------------------------
        DialogueUtilitiesNonStatic.multilineTextInput (LocationMarkerActivity.this,
                                                       LocationMarkerActivity.activity,
                                                       getString (R.string.location_marker_actions_title),
                                                       getString (R.string.action_command_summary),
                                                       5,
                                                       PublicData.storedData.locationActions.get(theIndex).actions,
                                                       Utilities.createAMethod (LocationMarkerActivity.class,"SetActions",StaticData.BLANK_STRING),
                                                       null,
                                                       StaticData.NO_RESULT,
                                                       getString (R.string.press_to_define_command));
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static String CheckNewLocation (Context theContext,
                                           double theLatitude,
                                           double theLongitude,
                                           double theCheckDistance,
                                           double theOutOfRangeDistance)
    {
        // -------------------------------------------------------------------------
        String      distancesString     = null;
        boolean     inRange             = false;              // 19/10/2020 ECU added
        // -------------------------------------------------------------------------
        if ((PublicData.storedData.locationActions != null) &&
                (PublicData.storedData.locationActions.size() > 0))
        {
            // ---------------------------------------------------------------------
            // 21/10/2020 ECU want to calculate the distances for each marker
            // ---------------------------------------------------------------------
            for (LocationActions locationActions : PublicData.storedData.locationActions)
            {
                locationActions.distance (theLatitude,theLongitude);
            }
            // ---------------------------------------------------------------------
            // 21/10/2020 ECU at this point the records should contain the latest
            //                distance calculations. So sort those records by
            //                distance
            // ---------------------------------------------------------------------
            LocationActions.SortedByDistance ();
            // ---------------------------------------------------------------------
            // 21/10/2020 ECU now generate the information to be displayed
            // ---------------------------------------------------------------------
            distancesString = "Marker Distances in metres" + StaticData.NEWLINEx2;
            // ---------------------------------------------------------------------
            // 13/10/2020 ECU scan through all of the stored 'location actions'
            // ---------------------------------------------------------------------
            int recordIndex = 0;
            double distance;
            // ---------------------------------------------------------------------
            for (LocationActions locationActions : PublicData.storedData.locationActions)
            {
                // -----------------------------------------------------------------
                if (locationActions.distance < theCheckDistance)
                {
                    // -------------------------------------------------------------
                    // 13/10/2020 ECU this marker is within the specified distance
                    // -------------------------------------------------------------
                    if (!locationActions.triggered)
                    {
                        // ---------------------------------------------------------
                        // 13/10/2020 ECU process the actions associated with this
                        //                marker
                        // 26/10/2020 ECU check if any actions have been defined
                        // ---------------------------------------------------------
                        if (Utilities.emptyString(locationActions.actions))
                        {
                            // -----------------------------------------------------
                            // 26/10/2020 ECU there are actions so process them
                            // -----------------------------------------------------
                            Utilities.actionHandler (theContext,locationActions.actions);
                            // -----------------------------------------------------
                        }
                        else
                        {
                            // -----------------------------------------------------
                            // 26/10/2020 ECU there are no actions so just tell the
                            //                user of the fact
                            // -----------------------------------------------------
                            Utilities.SpeakAPhrase (theContext,String.format (theContext.getString (R.string.location_detected),locationActions.marker));
                            // -----------------------------------------------------
                        }
                        // ---------------------------------------------------------
                        // 13/10/2020 ECU indicate that the actions have been
                        //                triggered
                        // ---------------------------------------------------------
                        locationActions.triggered = true;
                        PublicData.storedData.locationActions.set (recordIndex,locationActions);
                        // ---------------------------------------------------------
                    }
                    // -------------------------------------------------------------
                    // 19/10/2020 ECU indicate 'in range'
                    // -------------------------------------------------------------
                    inRange = true;
                    // -------------------------------------------------------------
                }
                else
                {
                    // -------------------------------------------------------------
                    // 13/10/2020 ECU if previously triggered then reset it
                    // -------------------------------------------------------------
                    if (locationActions.triggered)
                    {
                        // ---------------------------------------------------------
                        // 19/10/2020 ECU check if to reset the trigger
                        // ---------------------------------------------------------
                        if (locationActions.distance >= theOutOfRangeDistance)
                        {
                            locationActions.triggered = false;
                            PublicData.storedData.locationActions.set (recordIndex,locationActions);
                        }
                        // ---------------------------------------------------------
                    }
                    // -------------------------------------------------------------
                    // 19/10/2020 ECU indicate not 'in range'
                    // -------------------------------------------------------------
                    inRange = false;
                    // -------------------------------------------------------------
                }
                // -----------------------------------------------------------------
                // 15/10/2020 ECU add the message with the information
                // 19/10/2020 ECU added the 'triggered' and 'in range' states
                // -----------------------------------------------------------------
                distancesString += String.format ("%-17.17s %s%s %8.2f\n",locationActions.marker,
                                                    (inRange ? "R" : StaticData.SPACE_STRING),
                                                    (locationActions.triggered ? "T" : StaticData.SPACE_STRING),
                                                    locationActions.distance);
                // -----------------------------------------------------------------
                // 13/10/2020 ECU increment the index
                // -----------------------------------------------------------------
                recordIndex++;
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 15/10/2020 ECU return the string with the distances
        // -------------------------------------------------------------------------
        return distancesString;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void DisplayMap (int theIndex)
    {
        // -------------------------------------------------------------------------
        // 14/10/2020 ECU display the map for the specified marker
        // -------------------------------------------------------------------------
        Utilities.displayMap (LocationMarkerActivity.this,
                              PublicData.storedData.locationActions.get(theIndex).latitude,
                              PublicData.storedData.locationActions.get(theIndex).longitude,
                              StaticData.MAP_ZOOM,
                              PublicData.storedData.locationActions.get(theIndex).marker);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void Editor (int theIndex)
    {
        // -------------------------------------------------------------------------
        Intent intent = new Intent (getBaseContext(),LocationActionsActivity.class);
        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra (StaticData.PARAMETER_DATA,theIndex);
        startActivity (intent);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public ArrayList<ListItem> PopulateTheList ()
    {
        // -------------------------------------------------------------------------
        // 24/09/2016 ECU build up the list of liquids
        // -------------------------------------------------------------------------
        ArrayList<ListItem> listItems = new ArrayList<ListItem> ();
        // -------------------------------------------------------------------------
        // 21/10/2020 ECU sort the list sorted by the marker
        // -------------------------------------------------------------------------
        LocationActions.SortedByMarker ();
        // -------------------------------------------------------------------------
        int recordIndex = 0;
        for (LocationActions locationActions : PublicData.storedData.locationActions)
        {
            // ---------------------------------------------------------------------
            // 12/10/2020 ECU populate the list
            // ---------------------------------------------------------------------
            ListItem localListItem;
            // ---------------------------------------------------------------------
            localListItem = new ListItem  (R.drawable.location_gps,
                                           "Latitude : "     + locationActions.latitude +
                                           "   Longitude : " + locationActions.longitude,
                                           locationActions.marker,
                                           locationActions.actions,
                                           recordIndex++);
            // ---------------------------------------------------------------------
            // 13/10/2020 ECU set the colour to indicate if triggered
            // ---------------------------------------------------------------------
            if (locationActions.triggered)
            {
                localListItem.colour = R.color.green_yellow;
                localListItem.extras += StaticData.NEWLINEx2 + String.format ("Distance : %.2f metres",locationActions.distance);
            }
            else
            {
                localListItem.colour = R.color.light_gray;
            }
            // ---------------------------------------------------------------------
            // 29/01/2020 ECU add the item into the list
            // ---------------------------------------------------------------------
            listItems.add (localListItem);
            // ---------------------------------------------------------------------
        }
        return listItems;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void ProcessActions (int theIndex)
    {
        // -------------------------------------------------------------------------
        Utilities.actionHandler (LocationMarkerActivity.this,PublicData.storedData.locationActions.get(theIndex).actions);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void SetActions (String theActionCommands)
    {
        // -------------------------------------------------------------------------
        // 24/01/2016 ECU created to take the action commands and store away
        //            ECU check if something already exists then need to have the
        //                leading separator
        // 20/09/2016 ECU add the trim
        // -------------------------------------------------------------------------
         PublicData.storedData.locationActions.get(markerIndex).actions = theActionCommands;
        // -------------------------------------------------------------------------
        listViewSelector.refresh ();
        // -------------------------------------------------------------------------
    }
    // ==============================================================================
    public void SwipeAction (int thePosition)
    {
        // -------------------------------------------------------------------------
        // 18/10/2016 ECU created to confirm the deletion
        // 13/04/2018 ECU changed to use the non-static version
        // -------------------------------------------------------------------------
        DialogueUtilitiesNonStatic.yesNo (LocationMarkerActivity.this,
                                          LocationMarkerActivity.activity,
                                         "Item Deletion",
                                          String.format (getString (R.string.delete_confirmation_format),PublicData.storedData.locationActions.get (thePosition).marker),
                                          (Object) thePosition,
                                          Utilities.createAMethod (LocationMarkerActivity.class,"YesMethod",(Object) null),
                                          null);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void YesMethod (Object thePosition)
    {
        int recordToDelete = (Integer) thePosition;
        // -------------------------------------------------------------------------
        // 13/10/2020 ECU delete the specified receord
        // -------------------------------------------------------------------------
        PublicData.storedData.locationActions.remove (recordToDelete);
        // -------------------------------------------------------------------------
        if ( PublicData.storedData.locationActions.size () > 0)
        {
            // ---------------------------------------------------------------------
            // 13/10/2020 ECU still some records so refresh the display
            // ---------------------------------------------------------------------
            listViewSelector.refresh ();
            // ---------------------------------------------------------------------
        }
        else
        {
            // --------------------------------------------------------------------
            // 13/10/2020 ECU no more records so just exit the activity
            // ---------------------------------------------------------------------
           finish ();
           // ----------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
}
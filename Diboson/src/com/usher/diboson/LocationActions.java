package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LocationActions implements Serializable
{
    // =============================================================================
    // 13/10/2020 ECU created to store a 'location marker' and the actions to be
    //                taken the stored location is reached - works in conjunation
    //                with LocationActivity
    // 26/10/2020 ECU added ResetAllTriggers to reset the 'trigger' state for all
    //                of the currently stored location markers
    // =============================================================================

    // =============================================================================
    private static final long serialVersionUID = 1L;
    // =============================================================================

    // =============================================================================
    String      actions;
    double      distance;               // distance from coords when triggered
    double      latitude;
    double      longitude;
    String      marker;
    boolean     triggered;
    // =============================================================================

    // =============================================================================
    public LocationActions ()
    {
        // -------------------------------------------------------------------------
        actions     = StaticData.BLANK_STRING;
        distance    = 0.0;
        latitude    = 0.0;
        longitude   = 0.0;
        marker      = StaticData.BLANK_STRING;
        triggered   = false;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public LocationActions (double theLatitude,double theLongitude,String theMarker,String theActions)
    {
        // -------------------------------------------------------------------------
        latitude    = theLatitude;
        longitude   = theLongitude;
        marker      = theMarker;
        // -------------------------------------------------------------------------
        actions     = theActions;
        // -------------------------------------------------------------------------
        // 13/10/2020 ECU indicate whether the actions have been triggered
        // -------------------------------------------------------------------------
        triggered   = false;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public LocationActions (double theLatitude,double theLongitude,String theMarker)
    {
        // -------------------------------------------------------------------------
        this (theLatitude,theLongitude,theMarker,StaticData.BLANK_STRING);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    static Comparator<LocationActions> distanceComparator = new Comparator <LocationActions>()
    {
        // -------------------------------------------------------------------------
        // 21/10/2020 ECU used to sort the records by the distance - the lowest
        //                distance being first
        // -------------------------------------------------------------------------
        @Override
        public int compare (LocationActions lhs, LocationActions rhs)
        {
            return (lhs.distance < rhs.distance ? -1 : (lhs.distance == rhs.distance ? 0 : 1));
        }
        // -------------------------------------------------------------------------
    };
    // =============================================================================
    static Comparator<LocationActions> markerComparator = new Comparator<LocationActions>()
    {
        // -------------------------------------------------------------------------
        // 21/10/2020 ECU used to sort the record by 'marker' name
        // -------------------------------------------------------------------------
        @Override
        public int compare (LocationActions lhs, LocationActions rhs)
        {
            return ((lhs.marker).compareTo (rhs.marker));
        }
        // -------------------------------------------------------------------------
    };
    // =============================================================================
    public static void SortedByDistance ()
    {
        // -------------------------------------------------------------------------
        // 21/10/2020 ECU sort the records by distance
        // -------------------------------------------------------------------------
        Collections.sort (PublicData.storedData.locationActions,distanceComparator);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void SortedByMarker ()
    {
        // -------------------------------------------------------------------------
        // 21/10/2020 ECU sort the records alphabetically using the 'marker'
        // -------------------------------------------------------------------------
        Collections.sort (PublicData.storedData.locationActions,markerComparator);
    }
    // =============================================================================
    void distance (double theLatitude, double theLongitude)
    {
        // -------------------------------------------------------------------------
        // 21/10/2020 ECU calculate the distance and store in the record
        // -------------------------------------------------------------------------
        distance = LocationActivity.DistanceUsingHaversine (theLatitude,theLongitude,latitude,longitude) * 1000;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void newLocation (double theLatitude,double theLongitude,String theMarker)
    {
        if (PublicData.storedData.locationActions == null)
        {
            PublicData.storedData.locationActions = new ArrayList<LocationActions>();
        }
        // -------------------------------------------------------------------------
        // 14/10/2020 ECU check if the marker is empty then set up a default marker
        //                based on the co-ordinates
        // -------------------------------------------------------------------------
        if (!Utilities.emptyString (theMarker))
        {
            // ---------------------------------------------------------------------
            theMarker = String.format ("Marker_%.6f_%.6f",theLatitude,theLongitude);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        PublicData.storedData.locationActions.add (new LocationActions (theLatitude,theLongitude,theMarker));
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    String Print ()
    {
        return "Latitude : " + latitude + "  Longitude : " + longitude + "  Marker : " + marker;
    }
    // =============================================================================
    public static void ResetAllTriggers ()
    {
        // -------------------------------------------------------------------------
        // 26/10/2020 ECU reset the triggers for all defined markers
        // -------------------------------------------------------------------------
        if (PublicData.storedData.locationActions != null)
        {
            for (int index = 0; index < PublicData.storedData.locationActions.size (); index++)
            {
                PublicData.storedData.locationActions.get (index).triggered = false;
            }
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
 }

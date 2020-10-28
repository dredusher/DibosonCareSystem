package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;

public class MusicPlayerData implements Serializable
{
	// -----------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;
	// -----------------------------------------------------------------------------
	// 03/02/2015 ECU added deviceID to identify which device this data belongs to
	// 04/04/2015 ECU 'artists' and 'tracks' were moved here from MusicPlayer
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	public	ArrayList<TrackArtist> 	
							artists 		= null;			// 04/04/2015 ECU added
	private String			deviceID		= null;			// 03/02/2015 ECU added
	public  boolean			repeatMode		= false;		// 28/03/2015 ECU added
	public  String			rootFolder		= null;			// 06/04/2015 ECU added
	public  boolean         shuffleMode     = false;
	public	ArrayList<TrackDetails> 	
							savedTracks		= null;			// 05/04/2015 ECU added
	public  int             trackNumber	  	= 0;
	public	ArrayList<TrackDetails> 	
							tracks			= null;			// 04/04/2015 ECU added
	public  int         	trackPlaying	= StaticData.NO_RESULT;
															// 05/04/2015 ECU changed from String
	public  boolean         tracksPlaying 	= false;
	public  int             trackPosition	= 0;
	public  int				volume			= 60;			// 03/04/2015 ECU added
	// =============================================================================
	public boolean validateData (String theDeviceID)
	{
		// -------------------------------------------------------------------------
		// 03/02/2015 ECU checks whether the data is valid for the device whose ID
		//                is specified
		// -------------------------------------------------------------------------
		if (deviceID == null || (!deviceID.equals (theDeviceID)))
		{
			// ---------------------------------------------------------------------
			// 03/02/2015 ECU initialise the data
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 03/02/2015 ECU it seems that the data can be used
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public boolean AnyTracks ()
	{
		// -------------------------------------------------------------------------
		// 28/10/2020 ECU return whether there are any tracks
		//					true ..... there are music tracks
		//                  false .... there are no music tracks
		// -------------------------------------------------------------------------
		return ((tracks != null) && (tracks.size() > 0));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void setDeviceID (String theDeviceID)
	{
		// -------------------------------------------------------------------------
		// 03/02/2015 ECU copy across the device ID which will be used to validate
		// 				  this block of data
		// -------------------------------------------------------------------------
		deviceID = theDeviceID;
	}
	// =============================================================================
	public void SavedTracks ()
	{
		// -------------------------------------------------------------------------
		// 04/01/2017 ECU copy the current tracks to the saved array
		// -------------------------------------------------------------------------
		savedTracks = new ArrayList <TrackDetails> ();
		// -------------------------------------------------------------------------
		// 04/01/2018 ECU if there are any tracks then copy across
		// -------------------------------------------------------------------------
		if ((tracks != null) && (tracks.size() > 0))
		{
			for (int index = 0; index < tracks.size(); index++)
				savedTracks.add(tracks.get(index));
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void Tracks ()
	{
		// -------------------------------------------------------------------------
		// 04/01/2017 ECU copy the current tracks to the saved array
		// -------------------------------------------------------------------------
		tracks = new ArrayList <TrackDetails> ();
		// -------------------------------------------------------------------------
		// 04/01/2018 ECU if there are any tracks then copy across
		// -------------------------------------------------------------------------
		if ((savedTracks != null) && (savedTracks.size() > 0))
		{
			for (int index = 0; index < savedTracks.size(); index++)
				tracks.add(savedTracks.get(index));
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public String Print (String theTitle)
	{
		// -------------------------------------------------------------------------
		// 04/01/2018 ECU added the title and tidied up
		// -------------------------------------------------------------------------
		return theTitle 							+ StaticData.NEWLINE +
			   "Root Folder    : " + rootFolder     + StaticData.NEWLINE +
			   "Track Number   : " + trackNumber 	+ StaticData.NEWLINE +
	           "Track Playing  : " + trackPlaying 	+ StaticData.NEWLINE +
			   "Tracks Playing : " + tracksPlaying 	+ StaticData.NEWLINE +
			   ((tracks != null) ? 
					   "Total Tracks   : " + tracks.size() 	    + StaticData.NEWLINE : StaticData.BLANK_STRING) +
	           ((savedTracks != null) ? 
	        		   "Saved Tracks   : " + savedTracks.size() + StaticData.NEWLINE : StaticData.BLANK_STRING) +
			   "Track Position : " + trackPosition 	+ StaticData.NEWLINE +
			   "Shuffle Mode   : " + shuffleMode 	+ StaticData.NEWLINE +
			   "Repeat Mode    : " + repeatMode 	+ StaticData.NEWLINE +
			   "Device ID      : " + deviceID       + 
			   							"(" + PublicData.deviceID + ")";	// 03/02/2015 ECU added
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */

}

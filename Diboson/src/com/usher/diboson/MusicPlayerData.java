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
	public void setDeviceID (String theDeviceID)
	{
		// -------------------------------------------------------------------------
		// 03/02/2015 ECU copy across the device ID which will be used to validate
		// 				  this block of data
		// -------------------------------------------------------------------------
		deviceID = theDeviceID;
	}
	/* ============================================================================= */
	public String Print ()
	{
		return "Track Number   : " + trackNumber + "\n" +
	           "Track Playing  : " + trackPlaying + "\n" +
			   "Tracks Playing : " + tracksPlaying + "\n" +
	           "Total Tracks   : " + tracks.size() + "\n" +
			   "Track Position : " + trackPosition + "\n" +
			   "Shuffle Mode   : " + shuffleMode + "\n" +
			   "Device ID      : " + deviceID;						// 03/02/2015 ECU added
	}
	/* ============================================================================= */

}

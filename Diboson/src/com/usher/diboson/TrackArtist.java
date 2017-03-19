package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;


public class TrackArtist  implements Serializable,Comparable<TrackArtist>
{
	// =============================================================================
	// 31/03/2015 ECU created to hold information about a particular artist
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	private static final String	TAG	=	"TrackArtist";
	// =============================================================================
	String 				name;
	ArrayList<Integer> 	tracks	= new ArrayList <Integer> ();
	// =============================================================================
	public TrackArtist (String theName,int theFirstTrack)
	{
		name	=	theName;
		tracks	= 	new ArrayList <Integer> ();
		tracks.add (theFirstTrack);
	}
	// =============================================================================
	public void addTrack (int theTrackNumber)
	{
		tracks.add (theTrackNumber);
	}
	// =============================================================================
	@Override
	public int compareTo (TrackArtist theTrackArtist) 
	{
		// -------------------------------------------------------------------------
		/// 01/04/2015 ECU took out 'toLowerCase'
		// -------------------------------------------------------------------------
		return this.name.compareTo (theTrackArtist.name);   
	}
	// =============================================================================
	public String Print ()
	{
		String localString = "Artist : " + name + "\n";
		
		for (int theTrack = 0; theTrack < tracks.size(); theTrack++)
		{
			localString += "Track " + tracks.get(theTrack) + "\n";	
		}
		return localString;
	}
	// =============================================================================
	public void PrintToLog ()
	{
		// -------------------------------------------------------------------------
		Utilities.LogToProjectFile 
				(TAG,"==========================================\nArtist : " + name);
		
		for (int theTrack = 0; theTrack < tracks.size(); theTrack++)
		{
			Utilities.LogToProjectFile 
					(TAG,"Track " + tracks.get(theTrack) + "\n" +
							PublicData.musicPlayerData.tracks.get(tracks.get(theTrack)).Print() +	"\n");	
		}
	}
	// =============================================================================
	public static String [] returnArtists (ArrayList<TrackArtist> theArtists)
	{
		// -------------------------------------------------------------------------
		// 31/03/2015 ECU return the artists as a String []
		// -------------------------------------------------------------------------
		String [] localArtists = new String [theArtists.size()];
		
		for (int theIndex = 0; theIndex < theArtists.size(); theIndex++)
		{
			localArtists [theIndex] = theArtists.get (theIndex).name;
		}
		// -------------------------------------------------------------------------
		return localArtists;
	}
	// =============================================================================
}

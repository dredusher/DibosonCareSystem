package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;
import android.media.MediaMetadataRetriever;

public class TrackDetails implements Serializable,Comparable<TrackDetails>
{
	// =============================================================================
	// 01/04/2015 ECU created to hold metadata about tracks
	// 05/04/2015 ECU removed 'trackNumber'
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	private static final String	TAG	=	"TrackDetails";
	// =============================================================================
	public 	String		album			=  StaticData.UNKNOWN;;
	public 	String		artist			=  StaticData.UNKNOWN;;
	public 	String		composer		=  StaticData.UNKNOWN;;
	public  int			duration		=  StaticData.NO_RESULT;	// 02/05/2015 ECU added
	public 	String		fileName;
	public  int			genre			=  StaticData.NO_RESULT;	// 19/04/2015 ECU added
	public  boolean		populated		=  false;
	public	String		title			=  StaticData.UNKNOWN;;
	// =============================================================================
	public TrackDetails (String theTrackFile)
	{
		// -------------------------------------------------------------------------
		// 04/04/2015 ECU constructor when only the file name is to be set
		// -------------------------------------------------------------------------
		fileName	= theTrackFile;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public TrackDetails (TrackDetails theTrackDetails)
	{
		// -------------------------------------------------------------------------
		// 05/04/2015 ECU copy across the details from the specified record
		// 19/04/2015 ECU added genre
		// 02/05/2015 ECU added 'duration'
		// -------------------------------------------------------------------------
		album		=	theTrackDetails.album;
		artist		= 	theTrackDetails.artist;
		composer 	=	theTrackDetails.composer;
		duration	=   theTrackDetails.duration;
		fileName	=	theTrackDetails.fileName;
		genre		=   theTrackDetails.genre;
		populated	=	theTrackDetails.populated;
		title		=	theTrackDetails.title;
	}
	// =============================================================================
	@Override
	public int compareTo (TrackDetails theTrackDetails) 
	{
		// -------------------------------------------------------------------------
		/// 01/04/2015 ECU took out 'toLowerCase'
		// -------------------------------------------------------------------------
		return this.title.compareTo (theTrackDetails.title);   
	}
	// =============================================================================
	String genreAsString (int theGenre)
	{
		// -------------------------------------------------------------------------
		// 19/04/2015 ECU return the genre as a string unless it was never set in
		//                which case just return the 'unknown' string
		// -------------------------------------------------------------------------
		if (theGenre != StaticData.NO_RESULT)
		{
			String [] genres = MainActivity.activity.getResources().getStringArray (R.array.genres);
			// ---------------------------------------------------------------------
			// 02/04/2016 ECU check against the length of the array because was 
			//                getting an 'index out of range exception for some files
			//                the stored information has not been set up
			// ---------------------------------------------------------------------
			if (theGenre < genres.length)
				return genres [theGenre];
			else
				return StaticData.UNKNOWN;
			// ---------------------------------------------------------------------
		}
		else
		{
			return StaticData.UNKNOWN;
		}
	}
	// =============================================================================
	public void populate ()
	{
		// -------------------------------------------------------------------------
		// 01/04/2015 ECU store the file for future use - and the index
		// 04/04/2015 ECU changed from a constructor to method
		// 05/04/2015 ECU IMPORTANT in the original version I was creating
		//                   MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
		//			      in this method but occasional was getting an error
		//  				 E/AndroidRuntime( 3247): Caused by: java.lang.RuntimeException: 
		//                       setDataSource failed: status = 0xFFFFFFED
		//                I then declared the retriever in MusicPlayer and all seems
		//                well (see Notes)
		// 19/04/2015 ECU took out the track number as an argument
		// 02/05/2015 ECU added the duration (in millisecs)
		// -------------------------------------------------------------------------
		try
		{
			// -------------------------------------------------------------------------
			// 01/04/2015 ECU get the metadata from the file with the track
			// 05/04/2015 ECU change to use the metadata retriever in MusicPlayer
			// ---------------------------------------------------------------------
			MusicPlayer.mediaMetadataRetriever.setDataSource (fileName);
			// ---------------------------------------------------------------------
			album 		=  MusicPlayer.mediaMetadataRetriever.extractMetadata (MediaMetadataRetriever.METADATA_KEY_ALBUM);
			artist 		=  MusicPlayer.mediaMetadataRetriever.extractMetadata (MediaMetadataRetriever.METADATA_KEY_ARTIST);
			composer 	=  MusicPlayer.mediaMetadataRetriever.extractMetadata (MediaMetadataRetriever.METADATA_KEY_COMPOSER);
			title		=  MusicPlayer.mediaMetadataRetriever.extractMetadata (MediaMetadataRetriever.METADATA_KEY_TITLE);
			// ---------------------------------------------------------------------
			// 01/04/2015 ECU the above methods can return a 'null' so change to UNKNOWN
			// 01/09/2015 ECU changed to use StaticData
			// ---------------------------------------------------------------------
			if (album == null) 
				album = StaticData.UNKNOWN;
			if (artist == null)
				artist = StaticData.UNKNOWN;
			if (composer == null)
				composer = StaticData.UNKNOWN;
			if (title == null)
				title = StaticData.UNKNOWN;
			// ---------------------------------------------------------------------
			// 19/04/2015 ECU try and sort out the genre
			//            ECU found that if I just read the key without the extract 
			//                then the wrong value was being returned
			// ---------------------------------------------------------------------
			String localGenre 	
			   =  MusicPlayer.mediaMetadataRetriever.extractMetadata (MediaMetadataRetriever.METADATA_KEY_GENRE);
			// ---------------------------------------------------------------------
			// 09/01/2017 ECU Note - some of the tracks return a 'genre' which is not
			//                       in the required format and is just a text string.
			//                       This will generate a 'parseInt' exception which
			//                       in itself is not a problem but just seems messy
			//            ECU Added the 'startsWith' check to prevent exceptions
			// ---------------------------------------------------------------------
			if (localGenre == null || !localGenre.startsWith (StaticData.MUSIC_GENRE_START))
			{
				genre = StaticData.NO_RESULT;
			}
			else
			{
				// -----------------------------------------------------------------
				// 19/04/2015 ECU the format of localGenre is '(<number>)'
				// -----------------------------------------------------------------
				genre = Integer.parseInt (localGenre.replace(StaticData.MUSIC_GENRE_START,StaticData.BLANK_STRING).replace(StaticData.MUSIC_GENRE_END,StaticData.BLANK_STRING));
				// -----------------------------------------------------------------	
			}
			// ---------------------------------------------------------------------
			// 02/05/2015 ECU now try and set the duration in milliseconds
			// ---------------------------------------------------------------------
			String localDuration 	
			   =  MusicPlayer.mediaMetadataRetriever.extractMetadata (MediaMetadataRetriever.METADATA_KEY_DURATION);
			if (localDuration != null)
			{
			    duration = Integer.parseInt (localDuration);
			}
			// ---------------------------------------------------------------------
			// 05/04/2015 ECU indicate that the metadata has been set
			// ---------------------------------------------------------------------
			populated = true;
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 05/04/2015 ECU log the exception
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG, "Exception " + theException);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String [] returnStringArray (ArrayList<TrackDetails> theTrackDetails)
	{
		String [] results = new String [theTrackDetails.size()];
		// -------------------------------------------------------------------------
		// 01/04/2015 ECU loop through the array building the string array
		// -------------------------------------------------------------------------
		for (int theTrack = 0; theTrack < theTrackDetails.size(); theTrack++)
		{
			// ---------------------------------------------------------------------
			// 01/04/2015 ECU add the entry summary into the array
			// ---------------------------------------------------------------------
			results [theTrack] = theTrackDetails.get(theTrack).summary();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		return results;
	}
	// =============================================================================
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 01/04/2015 ECU return details of the track
		// 19/04/2015 ECU added 'genre'
		// 02/05/2015 ECU added 'duration'
		// -------------------------------------------------------------------------
		return 	"Title : " 		+ title + "\n" +
				"Artist : " 	+ artist + "\n" +
				"Album : " 		+ album + "\n" +
				"Composer : " 	+ composer + "\n" +
				"File : " 		+ fileName + "\n" +
				"Genre : " 		+ genre + "\n" + 
				"Duration : "   + duration;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void PrintAll (ArrayList<TrackDetails> theTrackDetails)
	{
		// -------------------------------------------------------------------------
		// 01/04/2015 ECU print out all of the track details
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < theTrackDetails.size(); theIndex++)
		{
			Utilities.LogToProjectFile (TAG,"=====================================\n");
			Utilities.LogToProjectFile (TAG,theTrackDetails.get(theIndex).Print() + "\n");
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String trackInformation ()
	{
		// -------------------------------------------------------------------------
		// 12/04/2015 ECU created to retain a one line summary of the track for
		//                use when displaying the listview in GridActivity
		// -------------------------------------------------------------------------
		return "'" + title + 
				"' on '" + album + 
				"' by '" + artist + 
				"' composed by '" + composer + "'" +
				((genre == StaticData.NO_RESULT) ? "" : (" genre '" + genreAsString (genre) + "'"));
	}
	// -----------------------------------------------------------------------------
	public static String trackInformation (String theFilename)
	{
		// -------------------------------------------------------------------------
		// 13/04/2015 ECU created to return the track information from a specified
		//                file
		// -------------------------------------------------------------------------
		// 13/04/2015 ECU create a new record which is then populated (the argument
		//                of '0' is irrelevant as not used, and then the composite
		//                information is returned
		// 19/04/2015 ECU took out the argument
		// -------------------------------------------------------------------------
		if (MusicPlayer.mediaMetadataRetriever == null)
			MusicPlayer.mediaMetadataRetriever = new MediaMetadataRetriever();
		// -------------------------------------------------------------------------
		TrackDetails trackDetails = new TrackDetails (theFilename);
		trackDetails.populate ();
		return trackDetails.trackInformation();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String summary ()
	{
		// -------------------------------------------------------------------------
		// 01/04/2015 ECU return a summary of the track
		//	              format 
		//				     <title> (<artist>)
		// -------------------------------------------------------------------------
		return title + " (" + artist + ")";
		// -------------------------------------------------------------------------
	}
	
	// =============================================================================
}

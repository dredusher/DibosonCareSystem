package com.usher.diboson;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class PlayRawFile
{
	// =============================================================================
	// 05/09/2017 ECU created to play the specified raw file. The code used to
	//                be in Utilities but it was felt cleaner to have a separate
	//                class
	//                NOTE - the 'edit dates' relate to when the code was in Utilities
	// 15/03/2019 ECU There was a problem in that if there wasn't any music already
	//                playing then when this file 'completed' then the resources
	//                were not released and this caused an error when the garbage 
	//                collector ran - fixed by just changing the point at which 'stop'
	//                was called.
	// =============================================================================
	
	// =============================================================================
	// 05/09/2017 ECU declare any public variables
	// =============================================================================
	
	// =============================================================================
	// 05/09/2017 ECU declare any local variables
	// -----------------------------------------------------------------------------
	private int			loopCounter;					// 11/03/2017 ECU added
	private MediaPlayer mediaPlayer = null;				// 04/09/2017 ECU moved here
	private boolean		mediaPlayerPaused;				// 10/03/2017 ECU added
	// =============================================================================
	
	// =============================================================================
	public PlayRawFile (Context theContext,
			                    int theResourceID,
			                    int theLoopCounter,
			                    final int theLoopPosition)
	{
		// -------------------------------------------------------------------------
		// 10/03/2017 ECU created to play the specified 'raw' resource using the
		//                media player
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 11/03/2017 ECU remember the loop counter for future use
			//            ECU a loop counter of 0 indicates 'no looping'. If the counter
			//                is > 0 then it needs to be decremented by 1 because
			//                the file is played before being 'looped' - this actually 
			//                means that a loop counter of 1 is in fact equivalent
			//                to 'no looping'
			//            ECU looping will be from the specified position
			// ---------------------------------------------------------------------
			loopCounter = theLoopCounter - 1;
			// ---------------------------------------------------------------------
			// 10/03/2017 ECU get the asset file descriptor for the 'raw' resource
			// ---------------------------------------------------------------------
			AssetFileDescriptor afd = theContext.getResources().openRawResourceFd (theResourceID);
			// ---------------------------------------------------------------------
			// 10/03/2017 ECU check if the global media player is active
			// ---------------------------------------------------------------------
			if (PublicData.mediaPlayer != null && PublicData.mediaPlayer.isPlaying())
			{
				// -----------------------------------------------------------------
				// 10/03/2017 ECU pause the global media player while this one plays
				// -----------------------------------------------------------------
				MusicPlayer.playOrPause (false);
				// -----------------------------------------------------------------
				// 10/03/2017 ECU remember that it has been paused
				// -----------------------------------------------------------------
				mediaPlayerPaused = true;
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 10/03/2017 ECU indicate that media player has not been paused
				// -----------------------------------------------------------------
				mediaPlayerPaused = false;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 04/09/2017 ECU the declaration was here 'final MediaPlayer' but moved
			//                above because need to have it public
			// ---------------------------------------------------------------------
			mediaPlayer = new MediaPlayer ();
			// ---------------------------------------------------------------------
			// 10/03/2017 ECU specify the source of the music
			// ---------------------------------------------------------------------
			mediaPlayer.setDataSource (afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
	        // ---------------------------------------------------------------------
	        // 10/03/2017 ECU set up a listener to be called when the music has
	        //                'completed' playing
	        // ---------------------------------------------------------------------
	        mediaPlayer.setOnCompletionListener(new OnCompletionListener() 
			{
				@Override
				public void onCompletion (MediaPlayer theMediaPlayer) 
				{
					// -------------------------------------------------------------
					// 11/03/2017 ECU check if any looping is needed
					// -------------------------------------------------------------
					if (loopCounter <= 0)
					{
						// ---------------------------------------------------------
						// 11/03/2017 ECU everything done so just check on the global 
						//                media player
						// ---------------------------------------------------------
						// 10/03/2017 ECU check if the global media player needs to be
						//                'resumed'
						// 15/03/2019 ECU whether there is a 'paused' media or not
						//                it is necessary to release the resources
						//                used in this class
						// ---------------------------------------------------------
						if (mediaPlayerPaused)
						{
							// -----------------------------------------------------
							// 10/03/2017 ECU the global media player needs to be resumed
							// -----------------------------------------------------
							MusicPlayer.playOrPause (true);
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
						// 04/09/2017 ECU make sure the resources are released
						// 05/09/2017 ECU changed to use the local method
						// 15/03/2019 ECU changed to always release resources
						// ---------------------------------------------------------
						stop ();
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 11/03/2017 ECU check about loops
						// ---------------------------------------------------------
						mediaPlayer.seekTo (theLoopPosition);
						mediaPlayer.start ();
						// ---------------------------------------------------------
						// 11/03/2017 ECU decrement the loop counter
						// ---------------------------------------------------------
						loopCounter--;
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
			});
	        // ---------------------------------------------------------------------
	        // 10/03/2017 ECU get ready and then start the media player
	        // ---------------------------------------------------------------------
	        mediaPlayer.prepare();
	        mediaPlayer.start();
	        // ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 10/03/2017 ECU something went wrong but no need to do anything
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public PlayRawFile (Context theContext,int theResourceID)
	{
		// -------------------------------------------------------------------------
		// 11/03/2017 ECU call the master method and indicate no loops needed
		// -------------------------------------------------------------------------
		this (theContext,theResourceID,0,0);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void stop ()
	{
		// -------------------------------------------------------------------------
		// 04/09/2017 ECU created to stop the media player associated with the raw
		//                resource, if playing, and release the resources
		// -------------------------------------------------------------------------
		if (mediaPlayer != null)
		{
			// ---------------------------------------------------------------------
			// 04/09/2017 ECU check if playing - if so then stop it
			// ---------------------------------------------------------------------
			if (mediaPlayer.isPlaying())
				mediaPlayer.stop();
			// ---------------------------------------------------------------------
			// 04/09/2017 ECU release associated resources
			// ---------------------------------------------------------------------
			mediaPlayer.release();
			// ---------------------------------------------------------------------
			// 04/09/2017 ECU indicate that the media player is no longer used
			// ---------------------------------------------------------------------
			mediaPlayer = null;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static void Stop (PlayRawFile thePlayRawFile)
	{
		// -------------------------------------------------------------------------
		// 05/09/2017 ECU created to be called externally to stop the raw player
		//                if it exists - if the object has already been deleted
		//                then do nothing
		// -------------------------------------------------------------------------
		if (thePlayRawFile != null)
		{
			// ---------------------------------------------------------------------
			// 05/09/2017 ECU the object exists so can perform the stop
			// ---------------------------------------------------------------------
			thePlayRawFile.stop ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

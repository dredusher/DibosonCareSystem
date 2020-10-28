package com.usher.diboson;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import java.io.File;

public class PlayAFileGapless
{
    // =============================================================================
    private static final int MESSAGE_START  = 0;
    private static final int MESSAGE_NEXT   = 1;
    private static final int MESSAGE_END    = 2;
    private static final int TIME_END       = 200;
    private static final int TIME_NEXT      = 100;
    // =============================================================================

    // =============================================================================
    int	                activeMediaPlayer;
    MediaPlayer[]       mediaPlayers = new MediaPlayer [2];
    MediaPlayerHandler  mediaPlayerHandler;
    int                 timeNext;
    int                 timeEnd;
    // =============================================================================

    // =============================================================================
    public PlayAFileGapless (Context theContext, String theFileName)
    {
        // -------------------------------------------------------------------------
        // 09/11/2019 ECU declare the handler that will process the messages
        // -------------------------------------------------------------------------
        mediaPlayerHandler = new MediaPlayerHandler();
        // -------------------------------------------------------------------------
        // 09/11/2019 ECU declare the two media players that will be needed
        // -------------------------------------------------------------------------
        mediaPlayers [0] = MediaPlayer.create (theContext, Uri.fromFile(new File(theFileName)));
        mediaPlayers [1] = MediaPlayer.create (theContext, Uri.fromFile(new File(theFileName)));
        // -------------------------------------------------------------------------
        // 09/11/2019 ECU initialise the pointer to the active media player
        // -------------------------------------------------------------------------
        activeMediaPlayer = 0;
        // -------------------------------------------------------------------------
        // 16/09/2013 ECU check if media player created correctly - if so then start
        //                up the players
        // -------------------------------------------------------------------------
        if ((mediaPlayers [0] != null) && (mediaPlayers [1] != null))
        {
            // ---------------------------------------------------------------------
            // 09/11/2019 ECU work out the times when the next media player is to be
            //                started and when the current media player is to be
            //                stopped
            // ----------------------------------------------------------------------
            timeEnd = mediaPlayers [0].getDuration() - TIME_END;
            timeNext = timeEnd - TIME_NEXT;
            // ---------------------------------------------------------------------
            // 09/11/2019 ECU send the message to start up the active media player
            // ---------------------------------------------------------------------
            mediaPlayerHandler.sendEmptyMessage(MESSAGE_START);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void terminate ()
    {
        // -------------------------------------------------------------------------
        // 09/11/2019 ECU delete any messages that may be queued
        // -------------------------------------------------------------------------
        mediaPlayerHandler.removeMessages (MESSAGE_START);
        mediaPlayerHandler.removeMessages (MESSAGE_NEXT);
        mediaPlayerHandler.removeMessages (MESSAGE_END);
        // -------------------------------------------------------------------------
        // 09/11/2019 ECU called to free up the media player resources after having
        //                stopped them
        // 10/11/2019 ECU just change the for loop from an 'index type'
        // -------------------------------------------------------------------------
        for (MediaPlayer mp : mediaPlayers)
        {
            if (mp != null)
            {
                mp.stop ();
                mp.release ();
            }
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================


    // =============================================================================
    class MediaPlayerHandler extends Handler
    {
        @Override
        public void handleMessage(Message theMessage)
        {
            // ---------------------------------------------------------------------
            switch (theMessage.what)
            {
                // -----------------------------------------------------------------
                // 09/11/2019 ECU start up the active player
                // -----------------------------------------------------------------
                case MESSAGE_START:
                    mediaPlayers [activeMediaPlayer].start();
                    // -------------------------------------------------------------
                    // 09/11/2019 ECU send message to indicate when the active
                    //                player is to be toggled
                    // -------------------------------------------------------------
                    this.sendEmptyMessageDelayed (MESSAGE_NEXT,timeNext);
                    // -------------------------------------------------------------
                    // 09/11/2019 ECU send message to indicate when this player
                    //                is to be stopped
                    // -------------------------------------------------------------
                    this.sendEmptyMessageDelayed (MESSAGE_END,timeEnd);
                    // -------------------------------------------------------------
                    break;
                // -----------------------------------------------------------------
                case MESSAGE_NEXT:
                    // -------------------------------------------------------------
                    // 09/11/2019 ECU toggle the active player
                    // -------------------------------------------------------------
                    activeMediaPlayer = (1 - activeMediaPlayer);
                    // -------------------------------------------------------------
                    // 09/11/2019 ECU send the message to start the newly active
                    //                player
                    // -------------------------------------------------------------
                    this.sendEmptyMessage (MESSAGE_START);
                    // -------------------------------------------------------------
                    break;
                // -----------------------------------------------------------------
                case MESSAGE_END:
                    // -------------------------------------------------------------
                    // 09/11/2019 ECU reset the player that is being ended so that it
                    //                is ready for the next time it will be started
                    // -------------------------------------------------------------
                    mediaPlayers [1 - activeMediaPlayer].pause ();
                    mediaPlayers [1 - activeMediaPlayer].seekTo (0);
                    // -------------------------------------------------------------
                    break;
                // -----------------------------------------------------------------
                default:
                    break;
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        }
    };
}

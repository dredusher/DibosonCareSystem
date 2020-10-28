package com.usher.diboson;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

public class Metronome
{
    // =============================================================================
    // 16/03/2020 ECU created to provide a metronome facility
    // 22/03/2020 ECU change the names to tie in with 'time signature'.
    //                The top number in the signature is the number of beats in the
    //                measure (bar). The bottom number is the note value assigned
    //                to each beeat.
    // =============================================================================

    // =============================================================================
    private static final int    BEEP_TIME       = 10;           // in mS
    private static final int    BEEP_TIME_LONG  = 50;           // in mS
    private static final int    IMAGE_TIME      = 50;           // in mS
    private static final int    MESSAGE_BEAT    = 0;
    private static final int    MESSAGE_IMAGE   = 1;
    // =============================================================================

    // =============================================================================
    //private static final int    CROCHET         = 2;
    //private static final int    MINIM           = 1;
    //private static final int    QUAVER          = 3;
    //private static final int    SEMIBREVE       = 0;
    //private static final int    SEMIQUAVER      = 4;
    // -----------------------------------------------------------------------------
    private static final int   [] NOTE_IMAGES   = {
                                                    R.drawable.metronome_whole_note,
                                                    R.drawable.metronome_half_note,
                                                    R.drawable.metronome_quarter_note,
                                                    R.drawable.metronome_eighth_note,
                                                    R.drawable.metronome_sixteenth_note
                                                  };
    // -----------------------------------------------------------------------------
    public static final String [] NOTE_VALUES   = {"whole note (semibreve)",
                                                   "half note (minim)",
                                                   "quarter note (crochet)",
                                                   "eighth note (quaver)",
                                                   "sixteenth note (semiquaver)"};
    // =============================================================================

    // =============================================================================
    View                imageView;
    MetronomeHandler    metronomeHandler;
    int                 noteValuePerBeat;
    ToneGenerator       toneGenerator;
    // =============================================================================

    // =============================================================================
    public Metronome (int theTempo,int theBeatsPerMeasure,int theNoteValuePerBeat)
    {
        // -------------------------------------------------------------------------
        // 16/03/2020 ECU the main constructor class
        //                      theTempo ........ the tempo in beats per minute
        // -------------------------------------------------------------------------
        toneGenerator    = new ToneGenerator (AudioManager.STREAM_MUSIC,100);
        // -------------------------------------------------------------------------
        noteValuePerBeat = theNoteValuePerBeat;
        // -------------------------------------------------------------------------
        metronomeHandler = new MetronomeHandler (theTempo,theBeatsPerMeasure,theNoteValuePerBeat);
        // -------------------------------------------------------------------------
        // 18/03/2020 ECU clear the view that will be flashed
        // -------------------------------------------------------------------------
        imageView        = null;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void imageVisibility (boolean theState)
    {
        // -------------------------------------------------------------------------
        // 18/03/2020 ECU handle the 'visibility' of current image, if defined
        //                  theState ......... true ........... visible
        //                                     false .......... invisible
        // -------------------------------------------------------------------------
        if (imageView != null)
        {
            // ---------------------------------------------------------------------
            imageView.setVisibility (theState ? View.VISIBLE
                                              : View.INVISIBLE);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void setView (View theView)
    {
        // -------------------------------------------------------------------------
        // 17/03/2020 ECU store the image that is to be flashed with the beat
        // -------------------------------------------------------------------------
        imageView = theView;
        // -------------------------------------------------------------------------
        // 22/03/2020 ECU decide which image is to be displayed
        // -------------------------------------------------------------------------
        ((ImageView) imageView).setImageResource (NOTE_IMAGES[noteValuePerBeat]);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void soundBeat (int theDuration)
    {
        // -------------------------------------------------------------------------
        // 22/03/2020 ECU added the duration as an argument
        // -------------------------------------------------------------------------
        try
        {
            // ---------------------------------------------------------------------
            // 05/03/2020 ECU sound a short beep
            // ---------------------------------------------------------------------
            toneGenerator.startTone (ToneGenerator.TONE_CDMA_PIP,theDuration);
            // ---------------------------------------------------------------------
        }
        catch (Exception theException)
        {
        }
    }
    // =============================================================================
    void stop ()
    {
        // -------------------------------------------------------------------------
        // 16/03/2020 ECU stop the metronome
        // -------------------------------------------------------------------------
        metronomeHandler.removeMessages (MESSAGE_BEAT);
        // -------------------------------------------------------------------------
    }
    // =============================================================================

    // =============================================================================
    class MetronomeHandler extends Handler
    {
        // -------------------------------------------------------------------------
        int noteInBar;
        int notePeriod;
        int notesPerMeasure;
        int noteValuePerBeat;
        // -------------------------------------------------------------------------
        public MetronomeHandler (int theTempo,int theNotesPerMeasure,int theNoteValuePerBeat)
        {
            // ---------------------------------------------------------------------
            // 16/03/2020 ECU set up the gap between beats
            // ---------------------------------------------------------------------
            notesPerMeasure     = theNotesPerMeasure;
            noteValuePerBeat    = theNoteValuePerBeat;
            // ---------------------------------------------------------------------
            // 16/03/2020 ECU set the period between notes
            // ---------------------------------------------------------------------
            notePeriod = (StaticData.MILLISECONDS_PER_MINUTE / theTempo);
            // ---------------------------------------------------------------------
            noteInBar = notesPerMeasure;
            // ---------------------------------------------------------------------
            // 16/03/2020 ECU now start the handler
            // ---------------------------------------------------------------------
            sendEmptyMessage (MESSAGE_BEAT);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        @Override
        public void handleMessage (Message theMessage)
        {
            // ---------------------------------------------------------------------
            switch (theMessage.what)
            {
                // -----------------------------------------------------------------
                case MESSAGE_BEAT:
                    // -------------------------------------------------------------
                    // 16/03/2020 ECU send message to get next note
                    // -------------------------------------------------------------
                    sendEmptyMessageDelayed (MESSAGE_BEAT,notePeriod);
                    // -------------------------------------------------------------
                    // 16/03/2020 ECU now check if it is time to start playing notes
                    // -------------------------------------------------------------
                    if (--noteInBar == 0)
                    {
                        // ---------------------------------------------------------
                        // 16/03/2020 ECU now reset the notes to sound
                        // ---------------------------------------------------------
                        noteInBar           = notesPerMeasure;
                        // ---------------------------------------------------------
                        // 22/03/2020 ECU sound the beat but emphasise that this is
                        //                end of the measure (bar)
                        // ---------------------------------------------------------
                        soundBeat (BEEP_TIME_LONG);
                        // ---------------------------------------------------------
                    }
                    else
                    {
                        // ---------------------------------------------------------
                        // 22/03/2020 ECU 'beep' the beat
                        // ---------------------------------------------------------
                        soundBeat (BEEP_TIME);
                        // ---------------------------------------------------------
                    }
                    // -------------------------------------------------------------
                    // 17/03/2020 ECU show the image
                    // 18/03/2020 ECU change to use the method
                    // -------------------------------------------------------------
                    imageVisibility (true);
                    // -------------------------------------------------------------
                    sendEmptyMessageDelayed (MESSAGE_IMAGE,IMAGE_TIME);
                    // -------------------------------------------------------------
                    break;
                // -----------------------------------------------------------------
                case MESSAGE_IMAGE:
                    // -------------------------------------------------------------
                    // 17/03/2020 ECU hide the image
                    // 18/03/2020 ECU change to use the method
                    // -------------------------------------------------------------
                    imageVisibility( false);
                    // -------------------------------------------------------------
                    break;
                    // -------------------------------------------------------------
                // -----------------------------------------------------------------
                default:
                    break;
                // -----------------------------------------------------------------
            }
        }
    };
    // =============================================================================
}

package com.usher.diboson;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class HandleProgressBar 
{
	// =============================================================================
	static 	Activity		activity;
	static  TextView		messageField;				// 29/11/2016 ECU added
	static  ImageButton		pauseResumeButton;			// 29/11/2016 ECU added
	static	ProgressHandler	progressHandler;
	static 	SeekBar 		progressSeekBar;
	static 	TextView 		progressSeekBarTitle;
	static  int				scaleFactor;
	static  RelativeLayout	seekbarLayout;				// 29/11/2016 ECU added
	static  boolean			updateProgressBar;
	// -----------------------------------------------------------------------------
	static  boolean			progressBarVisible;
	// =============================================================================
	
	// =============================================================================
	public HandleProgressBar (Activity theActivity,int theScaleFactor,RelativeLayout theSeekbarLayout,TextView theMessageField)
	{
		// -------------------------------------------------------------------------
		// 24/10/2016 ECU constructor - set variables to default values
		// 29/11/2016 ECU added the layout and TextView arguments which are stored
		//                locally
		// -------------------------------------------------------------------------
		activity 			= theActivity;
		messageField		= theMessageField;
		progressHandler 	= new ProgressHandler ();
		progressBarVisible	= false;
		scaleFactor			= theScaleFactor;
		seekbarLayout		= theSeekbarLayout;
		updateProgressBar	= false;
		// -------------------------------------------------------------------------
		// 24/10/2016 ECU set up the progress bar
		// -------------------------------------------------------------------------
		initiateProgressBar ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public void initiateProgressBar ()
	{
		// -------------------------------------------------------------------------
		// 24/10/2016 ECU create a progress bar on the screen with the custom
		//                characteristics
		// -------------------------------------------------------------------------
		progressSeekBar = (SeekBar) activity.findViewById (R.id.play_file_seekbar);
		progressSeekBar.setProgressDrawable (activity.getResources().getDrawable (R.drawable.custom_seek_bar));
		// -------------------------------------------------------------------------
		// 24/10/2016 ECU title for the seekbar
		// -------------------------------------------------------------------------
		progressSeekBarTitle = (TextView) activity.findViewById (R.id.seekbar_textview);
		// -------------------------------------------------------------------------
		// 24/10/2016 ECU set up the event listener for the seekbar
		// -------------------------------------------------------------------------
		progressSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() 
		{   
			// ---------------------------------------------------------------------
			@Override       
			public void onStopTrackingTouch (SeekBar theSeekBar) 
			{     
				// -----------------------------------------------------------------
				// 08/04/2015 ECU when the user 'lifts off' of the seek bar then
				//                the currently playing track will be played from
				//                that position
				// -----------------------------------------------------------------
				PublicData.mediaPlayer.seekTo (theSeekBar.getProgress() * scaleFactor);
				// -----------------------------------------------------------------
			}       
			// ---------------------------------------------------------------------
			@Override       
			public void onStartTrackingTouch (SeekBar theSeekBar) 
			{   	
			}       
			// ---------------------------------------------------------------------
			@Override       
			public void onProgressChanged (SeekBar seekBar, int progress,boolean fromUser) 
			{     
			}
			// ---------------------------------------------------------------------
		}); 
		// -------------------------------------------------------------------------
		pauseResumeButton = (ImageButton) activity.findViewById (R.id.seekbar_pause);
		pauseResumeButton.setOnClickListener (new View.OnClickListener()
		{
			@Override
			public void onClick (View view) 
			{	
				// -----------------------------------------------------------------
				// 25/10/2016 ECU toggle the music playing bit
				// -----------------------------------------------------------------
				if (PublicData.mediaPlayer.isPlaying())
				{
					PublicData.mediaPlayer.pause();
					pauseResumeButton.setImageDrawable (activity.getResources ().getDrawable (R.drawable.music_play));
					PublicData.mediaPlayerPaused = true;
				}
				else
				{
					PublicData.mediaPlayer.start ();
					pauseResumeButton.setImageDrawable (activity.getDrawable (R.drawable.music_pause));
					PublicData.mediaPlayerPaused = false;
				}
				// -----------------------------------------------------------------
			}
		});
	}
	// =============================================================================
	public void startProgressBarUpdate ()
	{
		//--------------------------------------------------------------------------
		// 24/10/2016 ECU make the progress bar start its updating
		// -------------------------------------------------------------------------
		// 24/10/2016 ECU set the flag to indicate update wanted then start the
		//                updating
		// -------------------------------------------------------------------------
		updateProgressBar = true;
		progressHandler.sendEmptyMessage (StaticData.MESSAGE_PROGRESS);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void finishProgressBarUpdate ()
	{
		// -------------------------------------------------------------------------
		// 24/10/2016 ECU called when the audio has stopped to hide the progress bar
		//                and to stop it being updated
		// -------------------------------------------------------------------------
		updateProgressBar = false;
		progressHandler.removeMessages (StaticData.MESSAGE_PROGRESS);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void progressBarUpdate ()
	{
		// -------------------------------------------------------------------------
		// 24/10/2016 ECU update the progress bar using the media player information
		// -------------------------------------------------------------------------
		if (PublicData.mediaPlayer != null)
		{
			if (PublicData.mediaPlayer.isPlaying())
			{
				if (!progressBarVisible)
				{
					progressBarVisible = true;
					// -------------------------------------------------------------
					// 24/10/2016 ECU make sure the range is set for the current file
					// -------------------------------------------------------------
					progressSeekBar.setMax (PublicData.mediaPlayer.getDuration() / scaleFactor);
					// -------------------------------------------------------------
					// 25/10/2016 ECU make the seekbar layout visible
					// 29/11/2016 ECU changed to use local variable
					// -------------------------------------------------------------
					seekbarLayout.setVisibility (View.VISIBLE);
					// -------------------------------------------------------------
					// 25/10/2016 ECU set the colour of the message field
					// -------------------------------------------------------------
					messageField.setTextColor (activity.getResources().getColor (R.color.gainsboro));
					// -------------------------------------------------------------
					pauseResumeButton.setImageDrawable (activity.getResources().getDrawable (R.drawable.music_pause));
					PublicData.mediaPlayerPaused = false;
					// -------------------------------------------------------------	
				}
				// -----------------------------------------------------------------
				// 24/10/2016 ECU update the status
				// -----------------------------------------------------------------
				progressSeekBar.setProgress (PublicData.mediaPlayer.getCurrentPosition() / scaleFactor);
				// -----------------------------------------------------------------
			}
			else
			{
				if (progressBarVisible && !PublicData.mediaPlayerPaused)
				{
					// -------------------------------------------------------------
					// 25/10/2016 ECU set the colour of the message field
					// 25/10/2016 ECU added the check on paused
					// -------------------------------------------------------------
					messageField.setTextColor (activity.getResources().getColor (R.color.black));
					// -------------------------------------------------------------
					// 25/10/2016 ECU hide the seekbar layout
					// -------------------------------------------------------------
					seekbarLayout.setVisibility (View.INVISIBLE);
					progressBarVisible = false;
					// --------------------------------------------------------------
				}
			}
		}
	}
	// =============================================================================
	
	// =============================================================================
	static class ProgressHandler extends Handler
    {
        @Override
        public void handleMessage (Message theMessage) 
        {  
        	// ---------------------------------------------------------------------
        	switch (theMessage.what)
        	{
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_PROGRESS:
        			// -------------------------------------------------------------
        			// 24/10/2016 ECU update the progress bar
        			// -------------------------------------------------------------
        			progressBarUpdate ();
        			// -------------------------------------------------------------
        			// 24/10/2016 ECU remove any queued messages
        			// -------------------------------------------------------------
        			removeMessages (StaticData.MESSAGE_PROGRESS);
        			// -------------------------------------------------------------
        			// 24/10/2016 ECU initiate a refresh if required
        			// 09/01/2018 ECU add the 'playOrPause' check
        			// -------------------------------------------------------------
        			if (!MusicPlayer.playOrPause())
        			{
        				if (updateProgressBar)
        					sendMessageDelayed (obtainMessage(StaticData.MESSAGE_PROGRESS),500);
        			}
        			// -------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        	} 	
        }
    }
	// =============================================================================
}

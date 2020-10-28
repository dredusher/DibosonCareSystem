package com.usher.diboson;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class UserInterface 
{
	// =============================================================================
	// 31/12/2016 ECU created to contain methods that affect the user interface
	//            ECU NOTE - the comments are as they were when in their original
	//                ====   position in CustomGridViewAdapter and GridActivity
	// =============================================================================
	
	// =============================================================================
	public static void activityUpdate (Context theContext,
									   int theResourceID,
									   ImageView theImageView,
									   TextView theLegendTextView,
									   TextView theTextView)
	{
		// -------------------------------------------------------------------------
		// 31/12/2016 ECU created to modify the displayed information about an
		//                activity
		// 04/06/2017 ECU added the context as an argument
		// 15/01/2018 ECU added theLegendTextView
		// -------------------------------------------------------------------------
		// 31/12/2016 ECU switch according to activity that is referenced
		// -------------------------------------------------------------------------
		switch (theResourceID)
		{
			// ---------------------------------------------------------------------
			case R.drawable.audio_analyser:
				// -----------------------------------------------------------------
				// 16/12/2019 ECU check the handler
				// 17/12/2019 ECU change to use new method Up...
				// 23/12/2019 ECU changed to use class rather than object
				// -----------------------------------------------------------------
				MonitorHandler.UpdateImageView (theImageView);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case R.drawable.audio_streaming:
				// -----------------------------------------------------------------
				// 15/01/2018 ECU created to handle aspects of the streaming of audio
				//                from a remote device
				// -----------------------------------------------------------------
				if ((PublicData.streamingDestination != null) && (theLegendTextView != null))
				{
					// -------------------------------------------------------------
					// 18/01/2018 ECU work out the required legend
					// -------------------------------------------------------------
					theLegendTextView.setText (Utilities.threeLineButtonLegend (theContext,
                            						theContext.getString(R.string.legend_audio_streaming),
                            						theContext.getResources().getColor (R.color.black),
                            						theContext.getString (R.string.long_press),
                            						theContext.getResources().getColor (R.color.gray),
                            						StaticData.LEGEND_INDENT + String.format (theContext.getString (R.string.audio_streaming_format),
                            																	(PublicData.audioStreaming ? "Stop" : "Start"),
                            																	Utilities.GetDeviceName (PublicData.streamingDestination)),
                            						theContext.getResources().getColor (R.color.dark_slate_gray)));
				}
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case R.drawable.music:
				// -----------------------------------------------------------------
				// 20/01/2015 ECU change the image to the album art if music is playing
				// 13/04/2015 ECU added 'subtitle' as argument
				// 31/12/2016 ECU if theTextView is null then this will be checked
				//                by the update... method
				// 03/06/2017 ECU check if the music player is waiting until the
				//                current actions have completed
				// -----------------------------------------------------------------
				if (PublicData.actionIntent == null)
				{
					// -------------------------------------------------------------
					// 03/06/2017 ECU Note - the music player is able to run
					//                       immediately
					// -------------------------------------------------------------
					MusicPlayer.updateImageView (theImageView,theTextView);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 03/06/2017 ECU display some useful information for the user
					// -------------------------------------------------------------
					theTextView.setText (theContext.getString (R.string.unable_to_start_music_player_actions));
					// -------------------------------------------------------------
					// 04/06/2017 ECU Note - make sure the view is visible and 'select'
					//                       it so that the marquee effect will start
					// -------------------------------------------------------------
					theTextView.setVisibility (View.VISIBLE);
					theTextView.setSelected (true);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			break;	
			// ---------------------------------------------------------------------
			case R.drawable.panic_alarm:
				// -----------------------------------------------------------------
				// 28/11/2015 ECU call the activity to update the scrolling text
				// 31/12/2016 ECU only need to do if the text view is defined
				// -----------------------------------------------------------------
				if (theTextView != null)
					PanicAlarmActivity.updateScrollingTextView (theTextView);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case R.drawable.theft:
				// -----------------------------------------------------------------
				// 06/10/2017 ECU handle the theft icon
				// -----------------------------------------------------------------
				TheftActivity.UpdateIcon (theImageView);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case R.drawable.timer:
				// -----------------------------------------------------------------
				// 05/02/2018 ECU take account of the countdown timer
				// -----------------------------------------------------------------
				if (theTextView != null)
					CountdownTimerActivity.updateText (theTextView);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case R.drawable.tone_generator:
				//------------------------------------------------------------------
				// 08/08/2019 ECU check if 'mosquito' mode is to be handled
				// -----------------------------------------------------------------
				int localLongLegendID;
				if ((PublicData.storedData.mosquitoFrequency == 0) ||
					(PublicData.storedData.mosquitoFrequency == StaticData.NOT_SET))
				{
					localLongLegendID = R.string.legend_tone_generator_long;
				}
				else
				{
					localLongLegendID = R.string.legend_tone_generator_long_mosquito;
				}
				// -----------------------------------------------------------------
				theLegendTextView.setText (Utilities.threeLineButtonLegend (theContext,
						theContext.getString(R.string.legend_tone_generator),
						theContext.getResources().getColor (R.color.black),
						theContext.getString (R.string.long_press),
						theContext.getResources().getColor (R.color.gray),
						StaticData.LEGEND_INDENT + theContext.getString (localLongLegendID),											
						theContext.getResources().getColor (R.color.dark_slate_gray)));
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case R.drawable.torch:
				// -----------------------------------------------------------------
				// 20/01/2015 ECU change the image to the album art if music is playing
				// 29/12/2016 ECU changed from Utilities. 
				// -----------------------------------------------------------------
				FlashLight.flashLightUpdateImageView (theImageView);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			default:
				break;
			// ---------------------------------------------------------------------
		}
	}
	// -----------------------------------------------------------------------------
	public static void activityUpdate (Context theContext,int theResourceID,ImageView theImageView)
	{
		// -------------------------------------------------------------------------
		// 31/12/2016 ECU created to handle the situation when there is no associated
		//                textview
		// 04/06/2017 ECU added the context as an argument
		// 15/01/2018 ECU added the second null with the addition of the legend's
		//                textview into the main method
		// -------------------------------------------------------------------------
		activityUpdate (theContext,theResourceID,theImageView,null,null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void activityUpdate (Context theContext,int theResourceID,ImageView theImageView,TextView theTextView)
	{
		// -------------------------------------------------------------------------
		// 15/01/2018 ECU created to be called from GridActivity where the legend
		//                is not to be changed, hence the supplied 'null'
		// -------------------------------------------------------------------------
		activityUpdate (theContext,theResourceID,theImageView,null,theTextView);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

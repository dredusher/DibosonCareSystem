package com.usher.diboson;

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
	public static void activityUpdate (int theResourceID,ImageView theImageView,TextView theTextView)
	{
		// -------------------------------------------------------------------------
		// 31/12/2016 ECU created to modify the displayed information about an
		//                activity
		// -------------------------------------------------------------------------
		// 31/12/2016 ECU switch according to activity that is referenced
		// -------------------------------------------------------------------------
		switch (theResourceID)
		{
			// ---------------------------------------------------------------------
			case R.drawable.audio_analyser:
				// -----------------------------------------------------------------
				// 20/01/2015 ECU change the image to the album art if music is playing
				// -----------------------------------------------------------------
				MonitorService.updateImageView (theImageView);
				// ---------------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case R.drawable.music:
				// -----------------------------------------------------------------
				// 20/01/2015 ECU change the image to the album art if music is playing
				// 13/04/2015 ECU added 'subtitle' as argument
				// 31/12/2016 ECU if theTextView is null then this will be checked
				//                by the update... method
				// -----------------------------------------------------------------
				MusicPlayer.updateImageView (theImageView,theTextView);
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
	public static void activityUpdate (int theResourceID,ImageView theImageView)
	{
		// -------------------------------------------------------------------------
		// 31/12/2016 ECU created to handle the situation when there is no associated
		//                textview
		// -------------------------------------------------------------------------
		activityUpdate (theResourceID,theImageView,null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

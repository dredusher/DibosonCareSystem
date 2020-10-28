package com.usher.diboson;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;

// =================================================================================
public class MusicPlayerTimerActivity extends DibosonActivity 
{
	// =============================================================================
	// 02/01/2018 ECU put in the check as to whether the activity has been created
	//	              anew or is being recreated after having been destroyed by
	//	              the Android OS
	// =============================================================================
	
	// =============================================================================
	int 			duration = 0;
	NumberPicker 	durationPicker;
	// =============================================================================

	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 03/01/2018 ECU set up some particular activity characteristics
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// ---------------------------------------------------------------------
			// 02/01/2018 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_music_player_timer);
			// ---------------------------------------------------------------------
			// 03/01/2018 ECU try and prevent the initial soft keyboard
			// ---------------------------------------------------------------------
			getWindow().setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			// ---------------------------------------------------------------------
						 durationPicker = (NumberPicker) findViewById (R.id.duration_picker);
			NumberPicker hoursPicker 	= (NumberPicker) findViewById (R.id.hours_picker);
			NumberPicker minutesPicker 	= (NumberPicker) findViewById (R.id.minutes_picker);
			NumberPicker secondsPicker 	= (NumberPicker) findViewById (R.id.seconds_picker);
			// ---------------------------------------------------------------------
			// 02/01/2018 ECU set up the minimum and maximum values for the pickers
			// ---------------------------------------------------------------------
			durationPicker.setMinValue 	(0);
			durationPicker.setMaxValue	(300);
			hoursPicker.setMinValue   	(0);
			hoursPicker.setMaxValue   	(23);
			minutesPicker.setMinValue 	(0);
			minutesPicker.setMaxValue 	(59);
			secondsPicker.setMinValue 	(0);
			secondsPicker.setMaxValue 	(59);
			// ---------------------------------------------------------------------
			// 03/01/2018 ECU set up the default values - they will be '0' if never
			//                set up
			// ---------------------------------------------------------------------
			durationPicker.setValue (PublicData.storedData.musicTimerDuration);
			hoursPicker.setValue    (PublicData.storedData.musicTimerHours);
			minutesPicker.setValue  (PublicData.storedData.musicTimerMinutes);
			secondsPicker.setValue  (PublicData.storedData.musicTimerSeconds);
			// ---------------------------------------------------------------------
			// 02/01/2018 ECU set up the listeners to detect the numbers being 
			//                changed
			// ---------------------------------------------------------------------
			durationPicker.setOnValueChangedListener (new NumberPicker.OnValueChangeListener () 
			{
				@Override
				public void onValueChange (NumberPicker picker, int oldValue, int newValue)
				{
					PublicData.storedData.musicTimerDuration = newValue;
			    }
			});
		    hoursPicker.setOnValueChangedListener (new NumberPicker.OnValueChangeListener () 
		    {
		    	@Override
		    	public void onValueChange (NumberPicker picker, int oldValue, int newValue)
		    	{
		    		PublicData.storedData.musicTimerHours = newValue;
		    		calculateDurationInSeconds ();
		        }
		    });
		    minutesPicker.setOnValueChangedListener (new NumberPicker.OnValueChangeListener () 
		    {
		    	@Override
		    	public void onValueChange (NumberPicker picker, int oldValue, int newValue)
		    	{
		    		PublicData.storedData.musicTimerMinutes = newValue;
		    		calculateDurationInSeconds ();
		    	}
		    });
		    secondsPicker.setOnValueChangedListener (new NumberPicker.OnValueChangeListener () 
		    {
		    	@Override
		    	public void onValueChange (NumberPicker picker, int oldValue, int newValue)
		    	{
		    		PublicData.storedData.musicTimerSeconds = newValue;
		    		calculateDurationInSeconds ();
		    	}
		    });
		    // ---------------------------------------------------------------------
			((Button) findViewById (R.id.start_music_player_button)).setOnClickListener (new View.OnClickListener ()
			{
				@Override
				public void onClick (View view) 
				{	
					// -------------------------------------------------------------
					// 02/01/2018 ECU convert the input data to seconds and start
					//                the music player
					// -------------------------------------------------------------
					startMusicPlayer ((PublicData.storedData.musicTimerHours * 60 *60) + 
							          	(PublicData.storedData.musicTimerMinutes * 60) + PublicData.storedData.musicTimerSeconds,
							          		PublicData.storedData.musicTimerDuration);
					// -------------------------------------------------------------
					// 02/01/2018 ECU terminate this activity
					// -------------------------------------------------------------
					finish ();
					// -------------------------------------------------------------
				}
			});
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/01/2018 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	void calculateDurationInSeconds ()
	{
		// -------------------------------------------------------------------------
		// 03/01/2018 ECU work out the duration in seconds based on the set components
		// -------------------------------------------------------------------------
		duration = (PublicData.storedData.musicTimerHours * 60 *60) + 
						(PublicData.storedData.musicTimerMinutes * 60) + 
							PublicData.storedData.musicTimerSeconds;
		// -------------------------------------------------------------------------
		// 03/01/2018 ECU change the range that is allowed for the volume decrease
		// -------------------------------------------------------------------------
		durationPicker.setMaxValue	(duration);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void startMusicPlayer (long theDuration,long theDecreaseVolumeStart)
	{
		// -------------------------------------------------------------------------
		// 02/01/2018 ECU start up the music player with the 'set' values
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (this,MusicPlayer.class);
		localIntent.putExtra (StaticData.PARAMETER_MUSIC_DURATION,theDuration);
		localIntent.putExtra (StaticData.PARAMETER_MUSIC_VOLUME,theDecreaseVolumeStart);
		startActivity (localIntent);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}
// =================================================================================

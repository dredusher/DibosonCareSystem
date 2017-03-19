package com.usher.diboson;

import java.io.File;

import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/* ============================================================================== */
public class Track extends DibosonActivity implements OnGestureListener 
{
	/* ========================================================================== */
	// 22/09/2013 ECU moved the trackFolder into the MainActivity activity
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 02/11/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	/* ========================================================================== */
	TextView audioFileName;
	TextView latitudeCoordinate;
	TextView longitudeCoordinate;
	String	 photoPath;							// 20/02/2017 ECU added
	Button	 trackButton;						// 02/01/2014 ECU added
	/* ========================================================================== */
	private GestureDetector gestureScanner;
	/* ========================================================================== */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 02/11/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 28/11/2016 ECU call method to set the screen up
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_track);
 
			gestureScanner = new GestureDetector(Track.this,this);
		
			latitudeCoordinate  = (TextView)findViewById (R.id.current_latitude);
			longitudeCoordinate = (TextView)findViewById (R.id.current_longitude);
			audioFileName       = (TextView)findViewById (R.id.audio_filename);
			// ---------------------------------------------------------------------
			// 02/01/2014 ECU pick up the track button
			// ---------------------------------------------------------------------
			trackButton = (Button) findViewById (R.id.track_button);
			// ---------------------------------------------------------------------
			// 02/01/2014 ECU set the legend
			// ---------------------------------------------------------------------
			trackButton.setText (getString (R.string.tracking_switch) + 
					                         (PublicData.trackingMode ? getString (R.string.Off) 
							                                          : getString (R.string.On)));
			// ---------------------------------------------------------------------
			// 19/02/2017 ECU sort out the record note/image buttons
			// ---------------------------------------------------------------------
			((ImageButton) findViewById (R.id.record_image_button)).setOnClickListener(btnClick);
			((ImageButton) findViewById (R.id.record_note_button)).setOnClickListener(btnClick);
			// ---------------------------------------------------------------------
			// 02/01/2014 ECU set up the listeners for the various buttons
			// ---------------------------------------------------------------------
			setButtonHandlers ();
			// ---------------------------------------------------------------------
			// 22/09/2013 ECU move folder name into MainActivity
			// 19/10/2014 ECU use inbuilt voice rather than playing a file
			// 21/02/2017 ECU changed to use resources
			// ---------------------------------------------------------------------
			Utilities.SpeakAPhrase(this,new String [] {getString (R.string.tracking_started),
													   getString (R.string.tracking_record)});
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/11/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	private View.OnClickListener btnClick = new View.OnClickListener() 
	{
		@Override
		public void onClick (View theView) 
		{
			switch (theView.getId())
			{
				case R.id.track_button:
				{
					// -------------------------------------------------------------
					// 02/01/2014 ECU change to switch tracking mode on/off
			    	// -------------------------------------------------------------
					PublicData.trackingMode = !PublicData.trackingMode;
			    	// -------------------------------------------------------------   
					// 02/01/2014 ECU change the legend of the button
					// 21/02/2017 ECH change to use resources
			    	// -------------------------------------------------------------  
					trackButton.setText (getString (R.string.tracking_switch) + 
	                         				(PublicData.trackingMode ? getString (R.string.Off) 
	                         										 : getString (R.string.On)));
			   		// -------------------------------------------------------------  
					// 22/09/2013 ECU build up location details from the
					//                files in the tracking folder tracking mode on then scan file folder
					// 03/01/2014 ECU if in tracking mode only
					// -------------------------------------------------------------	
					if (PublicData.trackingMode)
						Utilities.scanFilesInFolder (getBaseContext(),PublicData.trackFolder);
			    	   
					break;
				}
				case R.id.accuracy_down:
				{
					LocationActivity.track_accuracy = LocationActivity.track_accuracy / 10;
			    	// -------------------------------------------------------------  
					// 22/09/2013 ECU changed to use the custom toast view
			    	// -------------------------------------------------------------  
					Utilities.popToast (getString(R.string.new_accuracy) + " " + LocationActivity.track_accuracy,true,Toast.LENGTH_SHORT);
			    	// -------------------------------------------------------------  
					break;
				}
				case R.id.accuracy_up:
				{
					LocationActivity.track_accuracy = LocationActivity.track_accuracy * 10;
			    	// -------------------------------------------------------------  
					// 22/09/2013 ECU changed to use the custom toast view
			    	// ------------------------------------------------------------- 
					Utilities.popToast (getString(R.string.new_accuracy) + " " + LocationActivity.track_accuracy,true,Toast.LENGTH_SHORT);
			    	// -------------------------------------------------------------   
					break;
				} 
				// -----------------------------------------------------------------
				case R.id.record_image_button:
					// -------------------------------------------------------------
					// 19/02/2017 ECU created to record an image for the current
					//                location
					// 20/02/2017 ECU use the intent to capture the pictire into
					//                the specified file
					// -------------------------------------------------------------
					// 20/02/2017 ECU set up the destination file and if it already
					//                exists then delete it
					// -------------------------------------------------------------
					photoPath = getFileName (getString (R.string.image_file_format));
					File photoFile = new File (photoPath);
					// -------------------------------------------------------------
					// 20/02/2017 ECU check if the file already exists
					// -------------------------------------------------------------
					if (photoFile.exists())
					{
						// ---------------------------------------------------------
						// 20/02/2017 ECU the file exists so delete it
						// ---------------------------------------------------------
						photoFile.delete ();
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 20/02/2017 ECU set up the intent, set up the destination file 
					//                and then start the activity
					// -------------------------------------------------------------
		            Intent localIntent = new Intent (android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		            localIntent.putExtra (MediaStore.EXTRA_OUTPUT, Uri.fromFile (photoFile));
		            startActivityForResult(localIntent,StaticData.REQUEST_CODE_CAMERA);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.record_note_button:
					// -------------------------------------------------------------
					// 19/02/2017 ECU created to record a note for the current
					//                location
					// -------------------------------------------------------------
					actionsToTake ();
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}		 
		}
	};
	// =============================================================================
	@SuppressWarnings("static-access")
	protected void onActivityResult(int theRequestCode, int theResultCode, Intent data) 
	{
		// -------------------------------------------------------------------------
		// 20/02/2017 ECU check if the called intent returned 'OK'
		// -------------------------------------------------------------------------
		if (theResultCode == RESULT_OK)
		{
			// ---------------------------------------------------------------------
			// 20/02/2017 ECU determine which intent returned the result
			// ---------------------------------------------------------------------
			if (theRequestCode == StaticData.REQUEST_CODE_CAMERA) 
			{
				// -----------------------------------------------------------------
				//20/02/2016 ECU the media store intent has returned indicating that
				//               a photo has been taken
				// -----------------------------------------------------------------
				PublicData.messageHandler.popToastAndSpeakwithPhoto ("Photo taken",photoPath);
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onConfigurationChanged (Configuration theNewConfiguration) 
	{
		// -------------------------------------------------------------------------
		// 03/10/2015 ECU created - called when a configuration change occurs
		// -------------------------------------------------------------------------
		super.onConfigurationChanged (theNewConfiguration);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) 
	{
		return gestureScanner.onTouchEvent(motionEvent);
	}
	/* ============================================================================= */
	@Override
	public boolean onDown(MotionEvent motionEvent) 
	{	
		return false;
	}
	/* ============================================================================= */
	@Override
	public boolean onFling(MotionEvent motionEvent0, MotionEvent motionEvent1, float arg2,
								float arg3) 
	{
		return false;
	} 
	/* ============================================================================= */
	@Override
	public void onLongPress(MotionEvent motionEvent) 
	{
		// -------------------------------------------------------------------------
		// 14/11/2016 ECU Note - perform any required actions
		// -------------------------------------------------------------------------	
		actionsToTake ();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public boolean onScroll(MotionEvent motionEvent0, MotionEvent motionEvent1, float arg2,
							float arg3) 
	{
		return false;
	}
	/* ============================================================================= */
	@Override
	public void onShowPress(MotionEvent motionEvent) 
	{
						
	}
	/* ============================================================================= */
	@Override
	public boolean onSingleTapUp(MotionEvent motionEvent) 
	{
		return false;
	}
	/* ============================================================================= */
	private void actionsToTake ()
	{
		// -------------------------------------------------------------------------
		// display the current coordinates 
		// -------------------------------------------------------------------------
		latitudeCoordinate.setText ("Latitude\n" 
				+ Location.convert (LocationActivity.currentLatitude,Location.FORMAT_SECONDS)+"\n"+LocationActivity.currentLatitude);
		longitudeCoordinate.setText ("Longitude\n" 
				+ Location.convert (LocationActivity.currentLongitude,Location.FORMAT_SECONDS)+"\n"+LocationActivity.currentLongitude);
		// -------------------------------------------------------------------------
		// 19/10/2014 ECU tell the user
		// 21/02/2017 ECU change to use resources
		// -------------------------------------------------------------------------
		Utilities.SpeakAPhrase (this,new String [] {
													getString (R.string.tracking_record_start),
													getString (R.string.tracking_record_stop)});	
		// -------------------------------------------------------------------------
		// need to work out the file name to use. Want it to be 
		//	message_<latitude>_<longitude)_<accuracy>
		// but need to remove the decimal point
		// 19/10/2014 ECU change to use the string format from values
		// 20/02/2017 ECU changed to use the new method to get the file name
		// -------------------------------------------------------------------------		
		String newAudioFileName = getFileName (getString (R.string.tracking_file_format));
		audioFileName.setText (newAudioFileName);
		// -------------------------------------------------------------------------
		// try and play the specified audio file or record a new one if one
		// does not exist 
		// -------------------------------------------------------------------------
		Utilities.PlayAFile (this,newAudioFileName);
	}
	// =============================================================================
	String getFileName (String theFileFormat)
	{
		// -------------------------------------------------------------------------
		// 20/02/2017 ECU created to return the file name base on the specified
		//                file format
		// -------------------------------------------------------------------------
		return  PublicData.trackFolder +
					String.format (theFileFormat,
							(int)Math.round(LocationActivity.currentLatitude*(double)LocationActivity.track_accuracy),
							(int)Math.round(LocationActivity.currentLongitude*(double)LocationActivity.track_accuracy),
							LocationActivity.track_accuracy);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private void setButtonHandlers() 
	{
		// -------------------------------------------------------------------------
		// 02/01/2014 ECU changed to use the definition of trackButton from above
		// -------------------------------------------------------------------------
		(trackButton).setOnClickListener(btnClick);
		((Button)findViewById(R.id.accuracy_up)).setOnClickListener(btnClick);
		((Button)findViewById(R.id.accuracy_down)).setOnClickListener(btnClick);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

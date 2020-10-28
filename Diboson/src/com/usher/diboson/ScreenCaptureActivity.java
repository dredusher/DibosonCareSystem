package com.usher.diboson;

import android.os.Bundle;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ScreenCaptureActivity extends DibosonActivity 
{
	// =============================================================================
	// Revision History
	// ================
	// 13/04/2015 ECU created
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 29/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	// =============================================================================
	Bitmap		bitmapOfScreen;
	int			counter = 0;
	String		screenCaptureFile;
	ImageView	screenCaptureImageView;
	TextView	screenCaptureTextView;
	TextView	tellUserTextView;
	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been created anew
			// 14/11/2016 ECU added the 'true' for full screen working
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_screen_capture);
			// ---------------------------------------------------------------------
			// 13/04/2015 ECU set up the ImageView which will hold the captured image
			//                after clicking on the image
			// ---------------------------------------------------------------------
			screenCaptureImageView 	= (ImageView) findViewById (R.id.screen_capture_imageview);
			screenCaptureTextView 	= (TextView)  findViewById (R.id.screen_capture_textview);
			// ---------------------------------------------------------------------
			// 26/03/2019 ECU update the screen to tell the user what to do
			// ---------------------------------------------------------------------
			screenCaptureTextView.setText (String.format (getString (R.string.screen_capture_format),
														PublicData.emailDetails.recipients));
			// ---------------------------------------------------------------------
			// 26/03/2019 ECU Note - set up the listener for clicking on the image
			// ---------------------------------------------------------------------
			screenCaptureImageView.setOnClickListener (new OnClickListener ()
			{
				// -----------------------------------------------------------------
				@Override
				public void onClick(View theView) 
				{
					// -------------------------------------------------------------
					// 26/03/2019 ECU Note - generate an image of the screen
					// -------------------------------------------------------------
					bitmapOfScreen = Utilities.screenCapture (theView.getRootView(),screenCaptureFile);
					// -------------------------------------------------------------
					// 26/03/2019 ECU Note - change the displayed image to what has
					//                       been captured
					// -------------------------------------------------------------
					screenCaptureImageView.setImageBitmap (bitmapOfScreen);
					// -------------------------------------------------------------
					screenCaptureTextView.setText ("Counter " + counter++);
					// -------------------------------------------------------------
					// 24/04/2015 ECU send the email
					// -------------------------------------------------------------
					Utilities.SendEmailMessage (ScreenCaptureActivity.this,
												"Screen Capture",
												"The attached image is the last screen shot.",
												null,
												screenCaptureFile);
					// -------------------------------------------------------------
					// 26/03/2019 ECU tell the user what has happened
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (String.format (getString (R.string.screen_capture_done_format),
																PublicData.emailDetails.recipients));
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			});
			// ---------------------------------------------------------------------
			// 24/04/2015 ECU set up the name of the capture file
			// ---------------------------------------------------------------------
			screenCaptureFile = PublicData.projectFolder + "screenShot.png";
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		
		return true;
	}
	// =============================================================================
}

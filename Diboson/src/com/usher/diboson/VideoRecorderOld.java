package com.usher.diboson;

import java.io.IOException;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
	
	public class VideoRecorderOld extends DibosonActivity implements SurfaceHolder.Callback
	{
		// =============================================================================
		// V E R Y   I M P O R T A N T
		// ===========================
		// 06/11/2015 ECU this activity will run in 'landscape' orientation - this must
		//                be set in the manifest. If this is not done then this activity
		//                will be called twice - once when it is initially created and
		//                again when the orientation is changed programmatically. This
		//                will cause problems with the check on whether the
		//                savedInstanceState is null - which is there to detect when
		//                the activity is recreated after being destroyed by the 
		//                Android OS.
		// =============================================================================
		
		// =========================================================================
		// 11/10/2015 ECU put in the check as to whether the activity has been created
		//                anew or is being recreated after having been destroyed by
		//                the Android OS (IMPORTANT - see comment at 05/11/2015)
		// 22/10/2015 ECU changed to 'extends DibosonActivity'
		// 05/11/2015 ECU removed the code of 11/10/2015 because under normal use
		//                the activity is called twice - the first time with saved...
		//                == null, the second time with it != null. I believe this is
		//                because the configuration changes (going from portrait to
		//                landscape).
		// 06/11/2015 ECU IMPORTANT - reinstate the edits of 11/10/2015 (see Notes)
		// =========================================================================
		
		// -------------------------------------------------------------------------
		Button 				button;
		int					cameraSelected = StaticData.CAMERA_FRONT;
		String 				destinationFileName;
		MediaRecorder 		mediaRecorder;
		int					numberOfCameras	= Camera.getNumberOfCameras();
		int 				recording;
		SurfaceHolder 		surfaceHolder;
		// =========================================================================
		@SuppressWarnings("deprecation")
		@Override
	    public void onCreate (Bundle savedInstanceState) 
		{
	        super.onCreate(savedInstanceState);
	        // ---------------------------------------------------------------------
			if (savedInstanceState == null)
			{
				// -----------------------------------------------------------------
				// 11/10/2015 ECU the activity has been created anew
				// -----------------------------------------------------------------
				setContentView(R.layout.activity_video_recorder_old);
				// -----------------------------------------------------------------  
				// 18/07/2013 ECU set up the destination video folder
				// -----------------------------------------------------------------
				destinationFileName = PublicData.projectFolder + "video/myvideo.mp4";
				// -----------------------------------------------------------------
				recording = StaticData.VIDEO_RECORDER_RECORD;
				// -----------------------------------------------------------------
				mediaRecorder = new MediaRecorder();
	        	
				if (numberOfCameras > 0)
				{
					if (numberOfCameras == 1)
						initMediaRecorder (CamcorderProfile.get (0,CamcorderProfile.QUALITY_LOW));
					else
						initMediaRecorder (CamcorderProfile.get (CamcorderProfile.QUALITY_HIGH));
            	
					setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    	        
					SurfaceView myVideoView = (SurfaceView) findViewById(R.id.videoview);
					surfaceHolder = myVideoView.getHolder();
					surfaceHolder.addCallback(this);
					surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	        
					button = (Button)findViewById(R.id.videoviewbutton);
					button.setOnClickListener(myButtonOnClickListener);
				}
				else
				{
					// -------------------------------------------------------------
					// 10/02/2015 ECU indicate that there are no cameras
					// -------------------------------------------------------------
					Utilities.popToast ("This device is not capable of recording video",true);
					finish ();
				}
			}
			else
			{
				// -----------------------------------------------------------------
				// 11/10/2015 ECU the activity has been recreated after having been
				//                destroyed by the Android OS
				// -----------------------------------------------------------------
				finish (); 
				// -----------------------------------------------------------------
			}
		}
		/* ========================================================================= */
	    private Button.OnClickListener myButtonOnClickListener 
	    = new Button.OnClickListener(){

			@Override
			public void onClick(View arg0) 
			{
				switch (recording)
				{
					// -------------------------------------------------------------
					case StaticData.VIDEO_RECORDER_RECORD:
						startVideoRecording ();
						break;
					// -------------------------------------------------------------
					case StaticData.VIDEO_RECORDER_STOP:
						stopVideoRecording ();
						break;
					// -------------------------------------------------------------
					case StaticData.VIDEO_RECORDER_PLAY:
						// ---------------------------------------------------------
						// 18/06/2013 ECU call the activity to play the specified
						//                video
						// ---------------------------------------------------------
						Intent intent = new Intent (getBaseContext(),VideoViewer.class);
						intent.putExtra (StaticData.PARAMETER_FILE_NAME,destinationFileName);
						startActivityForResult (intent,0);   
						// ---------------------------------------------------------
						// 18/06/2013 ECU finish this activity because all is done
						// ---------------------------------------------------------
						finish ();
					// -------------------------------------------------------------
				}
			}};
	    // -------------------------------------------------------------------------
		@Override
		public void surfaceChanged (SurfaceHolder arg0, int arg1, int arg2, int arg3) 
		{	
		}
		// -------------------------------------------------------------------------
		@Override
		public void surfaceCreated (SurfaceHolder arg0) 
		{
			prepareMediaRecorder();
		}
		// -------------------------------------------------------------------------
		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) 
		{
		}
		/* ============================================================================= */
		@Override
		public boolean onPrepareOptionsMenu (Menu menu) 
		{
			// -------------------------------------------------------------------------
			// 28/05/2013 ECU clear the displayed menu
			// -------------------------------------------------------------------------
			menu.clear ();
			// -------------------------------------------------------------------------	
			// 05/06/2013 ECU used the method to build menu
			// -------------------------------------------------------------------------	
			buildTheMenu (menu);
			// -------------------------------------------------------------------------	
			return true;
		}
		/* ============================================================================= */
		@Override
		public boolean onCreateOptionsMenu(Menu menu) 
		{
			getMenuInflater().inflate(R.menu.main, menu);
			// -------------------------------------------------------------------------
			// 05/06/2013 ECU used the method to build menu
			// -------------------------------------------------------------------------
			buildTheMenu (menu);
			
			return true;
		}
		/* ============================================================================= */
		public boolean onOptionsItemSelected (MenuItem item)
		{
			// -------------------------------------------------------------------------
			// 16/06/2013 ECU take the actions depending on which menu is selected
			// 30/11/2014 ECU tidy up as the menu no longer used to start activities
			// -------------------------------------------------------------------------
	
			switch (item.getItemId())
			{
				case StaticData.CAMERA_FRONT:
					cameraSelected = StaticData.CAMERA_FRONT;
					mediaRecorder = new MediaRecorder ();
					initMediaRecorder (CamcorderProfile.get (0,CamcorderProfile.QUALITY_LOW));
					prepareMediaRecorder ();
					return true;
				case StaticData.CAMERA_REAR:
					cameraSelected = StaticData.CAMERA_REAR;
					mediaRecorder = new MediaRecorder ();
					initMediaRecorder (CamcorderProfile.get (CamcorderProfile.QUALITY_HIGH));
					prepareMediaRecorder ();
					return true;
			}
			return true;
		}
		/* ============================================================================= */
		private void buildTheMenu (Menu menu)
		{
			if (numberOfCameras > 1)
			{
				menu.add (0,StaticData.CAMERA_FRONT,0,R.string.front_facing_camera);
				menu.add (0,StaticData.CAMERA_REAR,0,R.string.rear_facing_camera);
			}
		}
		/* ========================================================================= */
		private boolean initMediaRecorder (CamcorderProfile theCamcorderProfile)
		{
			mediaRecorder.setAudioSource (MediaRecorder.AudioSource.DEFAULT);
	        mediaRecorder.setVideoSource (MediaRecorder.VideoSource.DEFAULT);
	        CamcorderProfile camcorderProfile = theCamcorderProfile;
	        // ---------------------------------------------------------------------
	        // 23/08/2013 ECU if there is no backward facing camera then the previous statement will
	        //                return a null
	        // ---------------------------------------------------------------------
	        if (camcorderProfile != null)
	        {
	        	mediaRecorder.setProfile (camcorderProfile);	
	        	mediaRecorder.setOutputFile (destinationFileName);
	          	mediaRecorder.setMaxDuration (60 * 1000 * 10); // Set max duration 600 sec.
	          												
	          	mediaRecorder.setMaxFileSize (50000000); // Set max file size 50M
	          	// ------------------------------------------------------------------
	          	return true;
	        }
	        else
	        {
	        	// -----------------------------------------------------------------
	        	// 04/04/2014 ECU use the custom popToast
	        	// -----------------------------------------------------------------
	        	Utilities.popToast ("This device is not capable of recording video",true);
	        	// -----------------------------------------------------------------
	        	// 23/08/2013 ECU do not want to continue
	        	// ------------------------------------------------------------------
	        	return false;
	        }
		}
		/* =========================================================================================== */
		private void prepareMediaRecorder()
		{
			mediaRecorder.setPreviewDisplay (surfaceHolder.getSurface());
			try 
			{
				mediaRecorder.prepare();
			} 
			catch (IllegalStateException theException) 
			{
				theException.printStackTrace();
			} 
			catch (IOException theException) 
			{
				theException.printStackTrace();
			}
		}
		/* ==================================================================== */
		private void startVideoRecording ()
		{
			mediaRecorder.start();
			recording = StaticData.VIDEO_RECORDER_STOP;
			button.setText ("Stop Recording");
		}
		/* ==================================================================== */
		private void stopVideoRecording ()
		{
			mediaRecorder.stop();
			mediaRecorder.release();
			recording = StaticData.VIDEO_RECORDER_PLAY;
			button.setText ("Play the Video");
		}
		/* ==================================================================== */
	}

	

package com.usher.diboson;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.InputType;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class VideoRecorder extends DibosonActivity implements SurfaceHolder.Callback 
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
	
	// =============================================================================
	// 11/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS (IMPORTANT - see 05/11/2015 below)
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 05/11/2015 ECU removed the code of 11/10/2015 because under normal use
	//                the activity is called twice - the first time with saved...
	//                == null, the second time with it != null. I believe this is
	//                because the configuration changes (going from portrait to
	//                landscape).
	// 06/11/2015 ECU IMPORTANT - reinstate the edits of 11/10/2015 (see Notes)
	// 25/05/2016 ECU changed so that it no longer uses the CameraPreview class
	// 29/05/2016 ECU chaned to work in 'portrait' mode
	// =============================================================================
	
	// =============================================================================
	static	Camera			camera;
	static  int				cameraSelected 		= StaticData.NO_RESULT;
	static  ImageButton		changeCameraButton;
	static	Context			context;		
	static 	String 			destinationFileName;
	static  MediaRecorder 	mediaRecorder;
			int				numberOfCameras		= Camera.getNumberOfCameras();
	static  ImageButton 	recordButton;
	static  boolean			recording			= false;
	static  int 			recordingMode 		= StaticData.VIDEO_RECORDER_RECORD;
	static  int				rotation			= StaticData.VIDEO_ROTATION_BACK;
	static  int				rotationHint		= StaticData.VIDEO_ROTATION_BACK_HINT;
	static  SurfaceHolder 	surfaceHolder;
			SurfaceView		surfaceView;
	// =============================================================================

	// =============================================================================
	@Override
	public void onCreate (Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 03/05/2015 ECU cannot use the normal Utilities.SetUpActivity because of
			//                the orientation and trying to keep the screen on
			//            ECU updated the SetUpActivity so that orientation can be
			//                passed through
			// 29/05/2016 ECU change to use 'portrait' mode
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
									 this,
									 StaticData.ACTIVITY_FULL_SCREEN,
									 !StaticData.ACTIVITY_SCREEN_ON);
			// ---------------------------------------------------------------------
			// 29/05/2016 ECU remember the context for later use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 11/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_video_recorder);
			surfaceView = (SurfaceView) findViewById (R.id.camera_surface_view);
			// ---------------------------------------------------------------------
			// 25/05/2016 ECU set up the surface that will be used for the camera
			// ---------------------------------------------------------------------
			surfaceHolder = surfaceView.getHolder(); 
			surfaceHolder.addCallback (this); 
			surfaceHolder.setType (SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			// ---------------------------------------------------------------------
			// 25/05/2016 ECU set up the buttons and their listeners
			// ---------------------------------------------------------------------
			changeCameraButton = (ImageButton) findViewById (R.id.video_camera);
			changeCameraButton.setOnClickListener (cameraChangeOnClickListener);
			recordButton = (ImageButton) findViewById (R.id.video_record_button);
			recordButton.setOnClickListener (buttonOnClickListener);
			// ---------------------------------------------------------------------
			// 29/05/2016 ECU preset some variables
			// ---------------------------------------------------------------------
			recording 		= false;
			recordingMode 	= StaticData.VIDEO_RECORDER_RECORD;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 11/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// ============================================================================= 
	public void onBackPressed() 
	{
		// -------------------------------------------------------------------------
		// 29/05/2016 ECU finish this activity
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
		super.onBackPressed();
	}
	// =============================================================================
	@Override
	protected void onPause() 
	{
		super.onPause ();
		// -------------------------------------------------------------------------
		// 10/02/2015 ECU release the current camera
		// -------------------------------------------------------------------------
		releaseCamera ();
	}
	// =============================================================================
	public void onResume() 
	{
		super.onResume();
		// -------------------------------------------------------------------------
		if (camera == null) 
		{
			cameraSelected = Utilities.cameraIndex (CameraInfo.CAMERA_FACING_BACK);
			// ---------------------------------------------------------------------
			// 09/09/2015 ECU check if the FRONT camera found
			// ---------------------------------------------------------------------
			if (cameraSelected == StaticData.NO_RESULT)
			{
				cameraSelected = Utilities.cameraIndex (CameraInfo.CAMERA_FACING_FRONT);
			}
			// ---------------------------------------------------------------------
			// 09/09/2015 ECU if a camera found then do some initialisation
			// ----------------------------------------------------------------------
			if (cameraSelected != StaticData.NO_RESULT)
			{
				camera = Camera.open (cameraSelected);
				refreshCamera ();
			}	
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		// 05/11/2015 ECU get ready for configuration
		// -------------------------------------------------------------------------
	    savedInstanceState.putBoolean (StaticData.PARAMETER_CONFIGURATION, true);
	    // -------------------------------------------------------------------------
	    super.onSaveInstanceState(savedInstanceState);
	}
	// =============================================================================
	private Button.OnClickListener buttonOnClickListener = new Button.OnClickListener ()
	{
		@Override
		public void onClick (View arg0) 
		{
			switch (recordingMode)
			{
				// -------------------------------------------------------------
				case StaticData.VIDEO_RECORDER_RECORD:
					// ---------------------------------------------------------
					// 25/05/2016 ECU Note - start the recording process
					// ---------------------------------------------------------
					// 29/05/2016 ECU request the name of the output file
					// 30/05/2016 ECU include the input type
					// ---------------------------------------------------------
			 		DialogueUtilities.textInput (context,
			 								     context.getString (R.string.output_file_name_title),
			 								     context.getString (R.string.output_file_name_summary),
			 								     Utilities.createAMethod (VideoRecorder.class,"OutputFileNameMethod",""),
			 								     null,
			 								     InputType.TYPE_CLASS_TEXT);
					// ---------------------------------------------------------
					break;
				// -------------------------------------------------------------
				case StaticData.VIDEO_RECORDER_STOP:
					// ---------------------------------------------------------
					// 11/02/2015 ECU check if recording has started - if so then
					//                stop it
					// ---------------------------------------------------------
					if (recording)
					{
						stopVideoRecording ();
						recording = false;
					}
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
		}
	};
	// =============================================================================
	private Button.OnClickListener cameraChangeOnClickListener = new Button.OnClickListener ()
	{
		@Override
		public void onClick (View theArgument) 
		{
			if (!recording)
			{
				// ---------------------------------------------------------------------
				// 25/05/2016 ECU select the type of camera view wanted
				// 29/05/2016 ECU set up the appropriate rotation
				// ---------------------------------------------------------------------
				if (cameraSelected == Utilities.cameraIndex (CameraInfo.CAMERA_FACING_FRONT))
				{
					cameraSelected = Utilities.cameraIndex (CameraInfo.CAMERA_FACING_BACK);
					rotation	   = StaticData.VIDEO_ROTATION_BACK;
					rotationHint   = StaticData.VIDEO_ROTATION_BACK_HINT;
				}
				else
				{
					cameraSelected =  Utilities.cameraIndex (CameraInfo.CAMERA_FACING_FRONT);
					rotation	   = StaticData.VIDEO_ROTATION_FRONT;
					rotationHint   = StaticData.VIDEO_ROTATION_FRONT_HINT;
				}
				// ---------------------------------------------------------------------
				// 10/02/2015 ECU release the current camera and then chose that selected
				// ---------------------------------------------------------------------
				releaseCamera ();
				chooseCamera (cameraSelected);	
			}
		}
	};
	// =============================================================================
	public void chooseCamera (int theCameraIndex) 
	{
		// -------------------------------------------------------------------------
		// 10/02/2015 ECU open the specified camera and get a refresh of the
		//                displayed preview
		// -------------------------------------------------------------------------
		camera = Camera.open (theCameraIndex);
		refreshCamera ();		
	}
	// =============================================================================
	public static void OutputFileNameMethod (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 29/05/2016 ECU created to specify the name for the video
		// 30/05/2016 ECU check if a file name has been supplied
		// -------------------------------------------------------------------------
		if (!theFileName.equals(""))
		{
			// ---------------------------------------------------------------------
			destinationFileName = PublicData.projectFolder + 
								    context.getString (R.string.video_directory) +  
								      theFileName + StaticData.EXTENSION_VIDEO;
			// ---------------------------------------------------------------------
			// 29/05/2016 ECU confirm the output file name
			// ---------------------------------------------------------------------
			Utilities.popToast (context.getString (R.string.output_file_name_confirmation) + destinationFileName,true);
			// ---------------------------------------------------------------------
			prepareMediaRecorder ();
			startVideoRecording ();
			recording = true;
			// ---------------------------------------------------------------------
		}
		else
		{
			Utilities.popToast (context.getString (R.string.file_name_needed),true);
		}
	}
	// =============================================================================
	private static boolean prepareMediaRecorder() 
	{
		mediaRecorder = new MediaRecorder();
		// -------------------------------------------------------------------------
		// 29/05/2016 ECU because now working in 'portrait' mode then store the hint
		//                so that the subsequent viewer knows how to display the
		//                video in the correct orientation.
		//            ECU Note - not all video players will take note of this 'hint'
		// -------------------------------------------------------------------------
		mediaRecorder.setOrientationHint (rotationHint);
		// -------------------------------------------------------------------------
		// 10/02/2015 ECU unlock the camera so that someone else can use it
		// -------------------------------------------------------------------------
		camera.unlock();
		mediaRecorder.setCamera (camera);
		// -------------------------------------------------------------------------
		mediaRecorder.setAudioSource (MediaRecorder.AudioSource.CAMCORDER);
		mediaRecorder.setVideoSource (MediaRecorder.VideoSource.CAMERA);
		// -------------------------------------------------------------------------
		// 10/02/2015 ECU note that the QUALITY parameter is important, for example
		//                using QUALITY_720P on the front camera causes a NPE. May
		//                 be worth trying _HIGH
		// 11/02/2015 ECU changed QUALITY to _HIGH
		// -------------------------------------------------------------------------
		mediaRecorder.setProfile (CamcorderProfile.get (cameraSelected,CamcorderProfile.QUALITY_HIGH));
		// -------------------------------------------------------------------------
		// 25/05/2016 ECU changed to use static variables
		// -------------------------------------------------------------------------
		mediaRecorder.setOutputFile  (destinationFileName);
		mediaRecorder.setMaxDuration (StaticData.VIDEO_MAX_DURATION); 	// Set max duration 60 sec.
		mediaRecorder.setMaxFileSize (StaticData.VIDEO_MAX_FILE_SIZE); 	// Set max file size 50M
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 09/09/2015 ECU set up the preview screen
			// ---------------------------------------------------------------------
			mediaRecorder.setPreviewDisplay (surfaceHolder.getSurface());
			// ---------------------------------------------------------------------
			// 10/02/2015 ECU prepare for the recording process
			// ---------------------------------------------------------------------
			mediaRecorder.prepare();
		} 
		catch (Exception theException) 
		{
			// ---------------------------------------------------------------------
			// 10/02/2015 ECU if an error occurs then release the media recorder
			//                and indicate the fact
			// ---------------------------------------------------------------------
			releaseMediaRecorder();
			return false;
		} 
		// -------------------------------------------------------------------------
		// 10/02/2015 ECU everything went well so indicate the fact
		// -------------------------------------------------------------------------
		return true;
	}
	// =============================================================================
	public void refreshCamera () 
	{ 
		// -------------------------------------------------------------------------
		// 25/05/2016 ECU if the surface exists refresh it
		// -------------------------------------------------------------------------
        if (surfaceHolder.getSurface() != null) 
        { 
        	try 
        	{ 
        		// -----------------------------------------------------------------
        		// 29/05/2016 ECU because the activity is running in 'portrait' mode
        		//                then set the orientation
        		// -----------------------------------------------------------------
		        camera.setDisplayOrientation (rotation);
		        // -----------------------------------------------------------------
        		camera.stopPreview (); 
        		camera.setPreviewDisplay (surfaceHolder); 
        		camera.startPreview (); 
        	}	 
        	catch (Exception theException) 
        	{ 
        	}
        }
        // -------------------------------------------------------------------------
    } 
	// =============================================================================
	private void releaseCamera() 
	{
		// -------------------------------------------------------------------------
		// 10/02/2015 ECU if the camera has been defined then release it and indicate
		//                the fact
		// -------------------------------------------------------------------------
		if (camera != null) 
		{
			camera.release();
			camera = null;
		}
	}
	// =============================================================================
	private static void releaseMediaRecorder () 
	{
		// -------------------------------------------------------------------------
		// 10/02/2015 ECU if the media recorder record exists then reset and release it
		//                and indicate the fact
		// -------------------------------------------------------------------------
		if (mediaRecorder != null) 
		{
			mediaRecorder.reset(); 		
			mediaRecorder.release(); 
			mediaRecorder = null;
			// ---------------------------------------------------------------------
			// 10/02/2015 ECU lock the camera so that no other process can get it
			// ---------------------------------------------------------------------
			camera.lock (); 
		}
	}
	// =============================================================================
	private static void startVideoRecording ()
	{
		// -------------------------------------------------------------------------
		// 26/05/2016 ECU want to start recording - hide the 'change camera' button
		//                before all other tasks
		// -------------------------------------------------------------------------
		changeCameraButton.setVisibility (View.INVISIBLE);
		// -------------------------------------------------------------------------
		mediaRecorder.start ();
		recordingMode = StaticData.VIDEO_RECORDER_STOP;
		recordButton.setImageResource(R.drawable.video_stop);
	}
	// =============================================================================
	private void stopVideoRecording ()
	{
		mediaRecorder.stop ();
		mediaRecorder.release ();
		recordingMode = StaticData.VIDEO_RECORDER_PLAY;
		// -------------------------------------------------------------------------
		// 30/05/2016 ECU change the displayed icon to indicate ability to 'play'
		//                current video
		// -------------------------------------------------------------------------
		recordButton.setImageResource (R.drawable.video_play);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void surfaceChanged (SurfaceHolder theSurfaceHolder, int theFormat, int theWidth, int theHeight) 
	{
		// -------------------------------------------------------------------------
		// 25/05/2016 ECU the surface details have changed so refresh the preview
		// -------------------------------------------------------------------------
		refreshCamera ();	
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void surfaceCreated (SurfaceHolder theSurfaceHolder) 
	{
		try 
		{ 
			// ---------------------------------------------------------------------
			// 25/05/2016 ECU open to the BACK camera and set up and start the preview
			// ---------------------------------------------------------------------
            camera = Camera.open(Utilities.cameraIndex (CameraInfo.CAMERA_FACING_BACK)); 
            camera.setPreviewDisplay (theSurfaceHolder); 
            camera.startPreview(); 
            // ---------------------------------------------------------------------
        } 
		catch (Exception theException) 
		{ 
        } 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void surfaceDestroyed (SurfaceHolder theSurfaceHolder) 
	{
		// -------------------------------------------------------------------------
		// 25/05/2016 ECU stop the preview and release the camera
		// -------------------------------------------------------------------------
		if (camera != null)
		{
			camera.stopPreview(); 
			releaseCamera ();
		}
        // -------------------------------------------------------------------------
	}
	// =============================================================================
}
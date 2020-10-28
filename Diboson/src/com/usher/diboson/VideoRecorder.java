package com.usher.diboson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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
	final static String 	TAG = "VideoRecorder";
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
	// 29/05/2016 ECU changed to work in 'portrait' mode
	// 16/06/2017 ECU changed the code to use the settings in 'cameraSettings'
	// 17/06/2017 ECU added handling of 'elapsed timer' and 'hide view'
	// 18/06/2017 ECU change the way that the surface view is hidden because
	//                setting it to invisible did not work (see note below)
	// 18/09/2017 ECU added video streaming
	// 24/01/2018 ECU IMPORTANT IMPORTANT IMPORTANT
	//				  =============================
	//				  Important to read the comments on video streaming in 
	//                VideoStreamingActivity
	// =============================================================================
	
	// =============================================================================
	static	Camera			camera;
	static  int				cameraSelected 		= StaticData.NO_RESULT;
	static  ImageButton		changeCameraButton;
	static	Context			context;		
	static 	String 			destinationFileName;
	static  TextView		hideSurfaceView;
	static  MediaRecorder 	mediaRecorder;
			int				numberOfCameras		= Camera.getNumberOfCameras();
	static  ImageButton 	recordButton;
	static	MessageHandler	recordHandler;
	static  boolean			recording			= false;
	static  int 			recordingMode 		= StaticData.VIDEO_RECORDER_RECORD;
	static  int				rotation			= StaticData.VIDEO_ROTATION_BACK;
	static  int				rotationHint		= StaticData.VIDEO_ROTATION_BACK_HINT;
	static  SurfaceHolder 	surfaceHolder;
	static	SurfaceView		surfaceView;				// 17/06/2017 ECU changed to static
	static  TextView		timerTextView;
	static  boolean			videoStreaming;				// 18/09/2017 ECU added
	// =============================================================================

	// =============================================================================
	@Override
	public void onCreate (Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		// 16/06/2017 ECU check if the camera settings have been set - if not then
		//                create the object
		// -------------------------------------------------------------------------
		if (PublicData.storedData.cameraSettings == null)
		{
			// ---------------------------------------------------------------------
			// 16/06/2017 ECU create a new instance of the settings
			// ---------------------------------------------------------------------
			PublicData.storedData.cameraSettings = new CameraSettings ();
			// ---------------------------------------------------------------------
		}
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
			// 15/06/2017 ECU initialise any static variables
			// 18/09/2017 ECU added video streaming
			// ---------------------------------------------------------------------
			destinationFileName	= null;
			videoStreaming		= false;
			// ---------------------------------------------------------------------
			// 15/06/2017 ECU create the handler that will process the record actions
			//                when a destination file has been specified and the
			//                recorder is to be started automatically
			// ---------------------------------------------------------------------
			recordHandler		= new MessageHandler ();
			// ---------------------------------------------------------------------
			// 15/06/2017 ECU check if a destination file name has been fed through
			//                which indicates that recording is to start automatically
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
			// ---------------------------------------------------------------------
			if (extras != null)
			{
				// -----------------------------------------------------------------
				// 15/06/2017 ECU check if file name passed through
				// -----------------------------------------------------------------
				destinationFileName = extras.getString (StaticData.PARAMETER_FILE_NAME);
				// -----------------------------------------------------------------
				// 18/09/2017 ECU check if video streaming has been requested
				// -----------------------------------------------------------------
				videoStreaming = extras.getBoolean (StaticData.PARAMETER_VIDEO_STREAM, false);
				// -----------------------------------------------------------------
				// 27/09/2017 ECU if video streaming is on then allow access to
				//                the network from the UI
				// -----------------------------------------------------------------
				if (videoStreaming)
					APIIssues.NetworkOnMainUIThread (android.os.Build.VERSION.SDK_INT);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 11/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_video_recorder);
			surfaceView = (SurfaceView) findViewById (R.id.camera_surface_view);
			// ---------------------------------------------------------------------
			// 17/06/2017 ECU get details of the timer
			// ---------------------------------------------------------------------
			timerTextView = (TextView) findViewById (R.id.video_timer);
			// ---------------------------------------------------------------------
			// 18/06/2017 ECU declare the text view that will be used to hide the
			//                surface view
			// ----------------------------------------------------------------------
			hideSurfaceView = (TextView) findViewById (R.id.video_hide_view);
			// ---------------------------------------------------------------------
			// 25/05/2016 ECU set up the surface that will be used for the camera
			// ---------------------------------------------------------------------
			surfaceHolder = surfaceView.getHolder(); 
			surfaceHolder.addCallback (this); 
			surfaceHolder.setType (SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			// ---------------------------------------------------------------------
			// 25/05/2016 ECU set up the buttons and their listeners
			// ---------------------------------------------------------------------
			changeCameraButton	= (ImageButton) findViewById (R.id.video_camera);
			recordButton 		= (ImageButton) findViewById (R.id.video_record_button);
			
			changeCameraButton.setOnClickListener (cameraChangeOnClickListener);
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
	public void onBackPressed () 
	{
		// -------------------------------------------------------------------------
		// 29/05/2016 ECU finish this activity
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
		// 27/07/2017 ECU make sure any messages to the handler are cleared
		//            ECU this is just in case the key is pressed during the delay
		//                before the START message is processed
		// -------------------------------------------------------------------------
		if (recordHandler.hasMessages (StaticData.MESSAGE_VIDEO_START))
		{
			recordHandler.removeMessages (StaticData.MESSAGE_VIDEO_START);
		}
		// -------------------------------------------------------------------------
		super.onBackPressed ();
		// -------------------------------------------------------------------------
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
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void onResume() 
	{
		super.onResume();
		// -------------------------------------------------------------------------
		// 27/07/2017 ECU moved the code that was here into the new method
		// -------------------------------------------------------------------------
		initialiseCamera ();
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
					// 15/06/2017 ECU check if the destination file has alread been
					//                supplied
					// 19/09/2017 ECU add the video streaming option
					// ---------------------------------------------------------
					if (destinationFileName == null && !videoStreaming)
					{
						// -----------------------------------------------------
						// 30/05/2016 ECU include the input type
						// ---------------------------------------------------------
						DialogueUtilities.textInput (context,
													 context.getString (R.string.output_file_name_title),
													 context.getString (R.string.output_file_name_summary),
													 Utilities.createAMethod (VideoRecorder.class,"OutputFileNameMethod",StaticData.BLANK_STRING),
													 null,
													 InputType.TYPE_CLASS_TEXT);
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 15/06/2017 ECU already have the destination file so use
						//                that
						// ---------------------------------------------------------
						OutputFileNameMethod (destinationFileName);
						// ---------------------------------------------------------
					}
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
				// -----------------------------------------------------------------
				// 25/05/2016 ECU select the type of camera view wanted
				// 29/05/2016 ECU set up the appropriate rotation
				// -----------------------------------------------------------------
				// 16/06/2017 ECU remember the existing camera
				// -----------------------------------------------------------------
				int savedCameraSelected = cameraSelected;
				// -----------------------------------------------------------------
				// 16/06/2017 ECU toggle to the other camera
				// -----------------------------------------------------------------
				if (cameraSelected == Utilities.cameraIndex (CameraInfo.CAMERA_FACING_FRONT))
				{
					cameraSelected = Utilities.cameraIndex (CameraInfo.CAMERA_FACING_BACK);
				}
				else
				{
					cameraSelected = Utilities.cameraIndex (CameraInfo.CAMERA_FACING_FRONT);
					
				}
				// -----------------------------------------------------------------
				// 16/06/2017 ECU now decide whether the selected camera can support
				//                the currently set quality
				// -----------------------------------------------------------------
				if (CameraSettings.validateProfile (cameraSelected,PublicData.storedData.cameraSettings.Quality ()))
				{
					// -------------------------------------------------------------
					// 16/06/2017 ECU the newly selected camera can support the set
					//                quality so set other parameters
					// -------------------------------------------------------------		
					if (cameraSelected == Utilities.cameraIndex (CameraInfo.CAMERA_FACING_BACK))
					{
						rotation	   = StaticData.VIDEO_ROTATION_BACK;
						rotationHint   = StaticData.VIDEO_ROTATION_BACK_HINT;
					}
					else
					{
						rotation	   = StaticData.VIDEO_ROTATION_FRONT;
						rotationHint   = StaticData.VIDEO_ROTATION_FRONT_HINT;
					}
					// -------------------------------------------------------------
					// 10/02/2015 ECU release the current camera and then chose that 
					//                selected
					// -------------------------------------------------------------
					releaseCamera ();
					chooseCamera (cameraSelected);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 16/06/2017 ECU the chosen camera does not support the chosen
					//                quality
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (String.format (context.getString (R.string.camera_quality_not_supported_format),
							                                        PublicData.storedData.cameraSettings.qualityLegend()),true);
					// -------------------------------------------------------------
					// 16/06/2017 ECU restore the currently selected camera
					// -------------------------------------------------------------
					cameraSelected = savedCameraSelected;
					// -------------------------------------------------------------
				}
				
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
	static void initialiseCamera ()
	{
		// -------------------------------------------------------------------------
		// 27/07/2017 ECU create to contain the code that used to in-line in 'onResume'
		//                because called from more than one place
		// -------------------------------------------------------------------------
		if (camera == null) 
		{
			// ---------------------------------------------------------------------
			// 16/06/2017 ECU changed to used the stored default
			// ---------------------------------------------------------------------
			cameraSelected = Utilities.cameraIndex (PublicData.storedData.cameraSettings.camera);
			// ---------------------------------------------------------------------
			// 09/09/2015 ECU check if the FRONT camera found
			// ---------------------------------------------------------------------
			if (cameraSelected == StaticData.NO_RESULT)
			{
				cameraSelected = Utilities.cameraIndex (CameraInfo.CAMERA_FACING_FRONT);
			}
			// ---------------------------------------------------------------------
			// 09/09/2015 ECU if a camera found then do some initialisation
			// ---------------------------------------------------------------------
			if (cameraSelected != StaticData.NO_RESULT)
			{
				camera = Camera.open (cameraSelected);
				// ------------------------------------------------------------------
				refreshCamera ();
			}	
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void OutputFileNameMethod (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 29/05/2016 ECU created to specify the name for the video
		// 30/05/2016 ECU check if a file name has been supplied
		// 19/09/2017 ECU added the video streaming
		// -------------------------------------------------------------------------
		if (videoStreaming || !theFileName.equals (StaticData.BLANK_STRING))
		{
			if (!videoStreaming)
			{	
				// -----------------------------------------------------------------
				destinationFileName = PublicData.projectFolder + 
										context.getString (R.string.video_directory) +  
										theFileName + StaticData.EXTENSION_VIDEO;
				// -----------------------------------------------------------------
				// 29/05/2016 ECU confirm the output file name
				// -----------------------------------------------------------------
				Utilities.popToast (context.getString (R.string.output_file_name_confirmation) + destinationFileName,true);
				// ------------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 27/07/2017 ECU need to take account of the status returned by the prepare..
			//                method
			// ---------------------------------------------------------------------
			if (prepareMediaRecorder ())
			{
				// -----------------------------------------------------------------
				// 27/07/2017 ECU everything seems OK so start the recorder
				// -----------------------------------------------------------------
				startVideoRecording ();
				recording = true;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			Utilities.popToast (context.getString (R.string.file_name_needed),true);
		}
	}
	// =============================================================================
	private static boolean prepareMediaRecorder () 
	{
		// -------------------------------------------------------------------------
		// 17/01/2018 ECU added the try...catch 'just in case'
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 27/07/2017 ECU there seems to be a situation when this method can be
			//                called with 'camera' set to null
			// ---------------------------------------------------------------------
			if (camera == null)
			{
				initialiseCamera ();
				// -----------------------------------------------------------------
				// 27/07/2017 ECU if the camera is still null then return this fact
				// -----------------------------------------------------------------
				if (camera == null)
					return false;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			mediaRecorder = new MediaRecorder();
			// ---------------------------------------------------------------------
			// 15/06/2017 ECU declare the listener for any information that the media 
			//                recorder may return
			// ---------------------------------------------------------------------
			mediaRecorder.setOnInfoListener (new OnInfoListener() 
			{
				@Override
				public void onInfo (MediaRecorder theMediaRecorder, int theWhat, int theExtra) 
				{
					// -------------------------------------------------------------
					// 15/06/2017 ECU the media recorder has some information to be
					//                handled
					// -------------------------------------------------------------
					switch (theWhat)
					{
						// ---------------------------------------------------------
						case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
						// ---------------------------------------------------------
						case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
						// ---------------------------------------------------------
						case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
							// -----------------------------------------------------
							// 15/06/2017 ECU in all cases then just stop the recording
							// -----------------------------------------------------
							recordHandler.sendEmptyMessage (StaticData.MESSAGE_VIDEO_STOP);
							// -----------------------------------------------------
							break;
						// ---------------------------------------------------------
					}
				}
			});
			// ---------------------------------------------------------------------
			// 15/06/2017 ECU declare the listener for any errors that may occur
			// ---------------------------------------------------------------------
			mediaRecorder.setOnErrorListener (new OnErrorListener() 
			{
				@Override
				public void onError (MediaRecorder theMediaRecorder, int theWhat, int theExtra) 
				{
					// -------------------------------------------------------------
					// 15/06/2017 ECU if an error occurs just stop the recording
					// -------------------------------------------------------------
					recordHandler.sendEmptyMessage (StaticData.MESSAGE_VIDEO_STOP);
					// -------------------------------------------------------------
				}
			});
			// ---------------------------------------------------------------------
			// 29/05/2016 ECU because now working in 'portrait' mode then store the
			//                hint so that the subsequent viewer knows how to display
			//                the video in the correct orientation.
			//            ECU Note - not all video players will take note of this 'hint'
			// ---------------------------------------------------------------------
			mediaRecorder.setOrientationHint (rotationHint);
			// ---------------------------------------------------------------------
			// 10/02/2015 ECU unlock the camera so that someone else can use it
			// ---------------------------------------------------------------------
			camera.unlock ();
			mediaRecorder.setCamera (camera);
			// ---------------------------------------------------------------------
			mediaRecorder.setAudioSource (MediaRecorder.AudioSource.CAMCORDER);
			mediaRecorder.setVideoSource (MediaRecorder.VideoSource.CAMERA);
			// ---------------------------------------------------------------------
			try 
			{	
				// -----------------------------------------------------------------
				// 10/02/2015 ECU note that the QUALITY parameter is important, for
				//                example using QUALITY_720P on the front camera 
				//                causes a NPE. Maybe worth trying _HIGH
				// 11/02/2015 ECU changed QUALITY to _HIGH
				// 16/06/2017 ECU changed to use stored quality
				// -----------------------------------------------------------------
				mediaRecorder.setProfile (CamcorderProfile.get (cameraSelected,PublicData.storedData.cameraSettings.Quality ()));
				// -----------------------------------------------------------------
				// 25/05/2016 ECU changed to use static variables
				// 16/06/2017 ECU changed to use stored duration and file size
				// 19/09/2017 ECU added the check on video streaming
				//            ECU Note - the comment as to why the static variable
				//                       is used rather than using a parameter is 
				//                       mentioned in VideoStreamingActivity
				// -----------------------------------------------------------------
				if (!videoStreaming)
				{
					mediaRecorder.setOutputFile  (destinationFileName);
					// -------------------------------------------------------------
					mediaRecorder.setMaxDuration (PublicData.storedData.cameraSettings.duration * StaticData.MILLISECONDS_PER_MINUTE); 	
					// -------------------------------------------------------------
					// 22/06/2017 ECU use the new method to get the correct file size
					//                to use
					// 21/09/2017 ECU added the 'true' flag so that if the size is 
					//                changed to the email attachment size then a 
					//                warning will be displayed
					// -------------------------------------------------------------
					mediaRecorder.setMaxFileSize (PublicData.storedData.cameraSettings.FileSize (true)); 
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 24/01/2018 ECU Note - the 'fileDescriptor' was set up by the
					//                       VideoStreamingActivity based on the
					//                       socket that is being used to communicate
					//                       to the 'requesting' device. 
					//                IMPORTANT - if the output file is set to one
					//                            on disk then all is well but does
					//                            not work with the file descriptor
					//                            probably because the 'socket' is
					//                            not 'seekable'
					// 30/06/2020 ECU IMPORTANT - continue the investigation and find
					//                            that if use the file descriptor than
					//                            get an 'IllegalState' exception when
					//                            press the 'record' button, i.e. when
					//                            'mediaRecorder.start ()' is action
					// -------------------------------------------------------------
					mediaRecorder.setOutputFile (VideoStreamingActivity.fileDescriptor);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 09/09/2015 ECU set up the preview screen
				// -----------------------------------------------------------------
				mediaRecorder.setPreviewDisplay (surfaceHolder.getSurface());
				// -----------------------------------------------------------------
				// 10/02/2015 ECU prepare for the recording process
				// -----------------------------------------------------------------
				mediaRecorder.prepare ();
				// -----------------------------------------------------------------
				// 10/02/2015 ECU everything went well so indicate the fact
				// -----------------------------------------------------------------
				return true;
				// -----------------------------------------------------------------
			
			} 
			catch (Exception theException) 
			{
				// ---------------------------------------------------------------------
				// 01/07/2020 ECU Log the error
				// ---------------------------------------------------------------------
				Utilities.LogToProjectFile (TAG, "Prepare Exception : " + theException);
				// -----------------------------------------------------------------
				// 10/02/2015 ECU if an error occurs then release the media recorder
				//                and indicate the fact
				// -----------------------------------------------------------------
				releaseMediaRecorder();
				// ------------------------------------------------------------------
				return false;
				// -----------------------------------------------------------------
			} 
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 01/07/2020 ECU Log the error
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG, "prepareMediaRecorder Exception : " + theException);
			// ---------------------------------------------------------------------
			// 17/01/2018 ECU just show the user why a problem has occurred
			// ---------------------------------------------------------------------
			Utilities.popToast ("Exception : " + theException,true);
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static void refreshCamera () 
	{ 
		// -------------------------------------------------------------------------
		// 25/05/2016 ECU if the surface exists refresh it
		// 27/07/2017 ECU changed to static
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
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	private static void startVideoRecording ()
	{
		try
		{
			// ---------------------------------------------------------------------
			// 26/05/2016 ECU want to start recording - hide the 'change camera' button
			//                before all other tasks
			// ---------------------------------------------------------------------
			changeCameraButton.setVisibility (View.INVISIBLE);
			// ---------------------------------------------------------------------
			mediaRecorder.start ();
			recordingMode = StaticData.VIDEO_RECORDER_STOP;
			recordButton.setImageResource (R.drawable.video_stop);
			// ---------------------------------------------------------------------
			// 17/06/2017 ECU start up the elapsed timer after making it visible
			//            ECU make the elapsed timer dependent on settings
			// ---------------------------------------------------------------------
			if (PublicData.storedData.cameraSettings.elapsedTimer)
			{
				timerTextView.setVisibility (View.VISIBLE);
				Utilities.elapsedTimeDisplay (timerTextView,1000);
			}
			// ---------------------------------------------------------------------
			// 17/06/2017 ECU decide if the surface view is to be hidden
			// 18/06/2017 ECU setting the surface view to INVISIBLE did not seem to
			//                work as the video, although it seemed to continue recording,
			//                was in fact not recording any video. Now have a text view
			//                that can hide the surface view.
			// ---------------------------------------------------------------------
			if (PublicData.storedData.cameraSettings.hideView)
			{
				hideSurfaceView.setVisibility (View.VISIBLE);
			}
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			Utilities.LogToProjectFile(TAG, "Start Exception : " + theException);
		}
	}
	// =============================================================================
	private void stopVideoRecording ()
	{
		try
		{
			mediaRecorder.stop ();
			mediaRecorder.release ();
			recordingMode = StaticData.VIDEO_RECORDER_PLAY;
			// ---------------------------------------------------------------------
			// 30/05/2016 ECU change the displayed icon to indicate ability to 'play'
			//                current video
			// ---------------------------------------------------------------------
			recordButton.setImageResource (R.drawable.video_play);
			// ---------------------------------------------------------------------
			// 17/06/2017 ECU stop the elapsed timer and then hide the view
			//            ECU make the elapsed timer dependent on settings
			// ---------------------------------------------------------------------
			if (PublicData.storedData.cameraSettings.elapsedTimer)
			{
				Utilities.elapsedTimeDisplay (timerTextView,StaticData.NO_RESULT);
				timerTextView.setVisibility (View.INVISIBLE);
			}
			// ---------------------------------------------------------------------
			// 17/06/2017 ECU decide if the surface view is hidden - make visible
			// 18/06/2017 ECU see the note in 'startVideoRecording' as to why trying
			//                to use a text view to hide the surface view.
			// ---------------------------------------------------------------------
			if (PublicData.storedData.cameraSettings.hideView)
			{
				hideSurfaceView.setVisibility (View.INVISIBLE);
			}
			// --------------------------------------------------------------------
			// 22/06/2017 ECU decide whether the completed video is to be emailed
			//                to the designated recipient
			// --------------------------------------------------------------------
			if (PublicData.storedData.cameraSettings.emailVideo)
			{
				Utilities.SendEmailMessage (context,
											context.getString (R.string.legend_video_recorder),
											context.getString (R.string.video_attachment),
											null,
											destinationFileName);
			}
			// --------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// --------------------------------------------------------------------
			// 22/06/2017 ECU log any exception
			// --------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG,"Exception : " + theException);
			// --------------------------------------------------------------------
		}
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
			// 17/06/2017 ECU changed to use the default camera (assigned in settings)
			// ---------------------------------------------------------------------
            camera = Camera.open (Utilities.cameraIndex (PublicData.storedData.cameraSettings.camera)); 
            camera.setPreviewDisplay (theSurfaceHolder); 
            camera.startPreview (); 
            // ---------------------------------------------------------------------
        } 
		catch (Exception theException) 
		{ 
        } 
		 // ------------------------------------------------------------------------
        // 15/06/2017 ECU check if an output file has been specified - if so then
        //                start the recorder
		// 18/09/2017 ECU I M P O R T A N T
		//                =================
		//                The following code used to be in the 'try...catch' but it
		//                seemed that was getting a 'connection failure on the camera
		//                service' (wording not exactly right) which meant that this
		//                code was not being actioned. However even with the exception
		//                it seemed to work OK. Needs investigation.
		// 19/09/2017 ECU added the video streaming check
        // -------------------------------------------------------------------------
        if (destinationFileName != null || videoStreaming)
        {
        	// ---------------------------------------------------------------------
        	// 15/06/2017 ECU 'simulate' a click event to get the recorder to
        	//                start
        	//            ECU send message to trigger the record operation
        	//            ECU leave a delay to ensure everything is up and
        	//                running correctly
        	// ---------------------------------------------------------------------
        	recordHandler.sendEmptyMessageDelayed (StaticData.MESSAGE_VIDEO_START,5000);
        	// ---------------------------------------------------------------------
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
	
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class MessageHandler extends Handler
	{
		@Override
		public void handleMessage (Message theMessage) 
		{  
			switch (theMessage.what)
			{
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_VIDEO_START:
					// -------------------------------------------------------------
					// 15/06/2017 ECU trigger the recording to start
					// -------------------------------------------------------------
					recordButton.performClick ();
					// -------------------------------------------------------------
					break;
	        	// -----------------------------------------------------------------
				case StaticData.MESSAGE_VIDEO_STOP:
					// -------------------------------------------------------------
					// 15/06/2017 ECU want the recording to be stopped
					// -------------------------------------------------------------
					recordButton.performClick ();
					// -------------------------------------------------------------
					break;
	        }
	        // ---------------------------------------------------------------------
	    }
	}
	// =============================================================================
}
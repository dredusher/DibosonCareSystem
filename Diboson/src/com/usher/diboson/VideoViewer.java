package com.usher.diboson;

import java.lang.reflect.Method;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;
 
public class VideoViewer extends DibosonActivity 
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
	// 29/05/2016 ECU The above notes are no longer relevant but have been kept
	//                as part of the revision history
	// =============================================================================
	
	// =============================================================================
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 02/11/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS (IMPORTANT - see comment at 05/11/2015)
	// 05/11/2015 ECU removed the code of 02/11/2015 because under normal use
	//                the activity is called twice - the first time with saved...
	//                == null, the second time with it != null. I believe this is
	//                because the configuration changes (going from portrait to
	//                landscape).
	// 06/11/2015 ECU IMPORTANT - reinstate the edits of 02/11/2015 (see Notes)
	// 29/05/2016 ECU changed to work in 'portrait' orientation
	// 01/06/2016 ECU add listeners for completion and touch and the FINISH parameter
	// 03/06/2016 ECU added the method when the video finishes
	// 24/12/2016 ECU added the onBack method
	// =============================================================================
	
	// -----------------------------------------------------------------------------
			boolean		finishActivity	= false;
			Method		finishMethod	= null;			// 03/06/2016 ECU added
			String 		videoFileName;
	static 	VideoView 	videoView		= null;			// 28/12/2016 ECU changed to
														//                static
	// -----------------------------------------------------------------------------
	
	// =============================================================================
	@Override
	public void onCreate (Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 02/11/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 18/06/2013 ECU get the name of the file to be played
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();		
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 10/02/2015 ECU get the name of the file to be played
				// -----------------------------------------------------------------
				videoFileName = extras.getString (StaticData.PARAMETER_FILE_NAME); 	
				// -----------------------------------------------------------------
				// 01/06/2016 ECU check if activity is to finish at the end of the video
				// -----------------------------------------------------------------
				finishActivity = extras.getBoolean (StaticData.PARAMETER_FINISH,false);
				// -----------------------------------------------------------------
				// 03/06/2016 ECU check whether a finish method is to be defined
				// -----------------------------------------------------------------
				MethodDefinition<?> methodDefinition = (MethodDefinition<?>) extras.getSerializable (StaticData.PARAMETER_METHOD);
 	   			// -----------------------------------------------------------------
 	   			if (methodDefinition != null)
 	   				finishMethod					 =  methodDefinition.ReturnMethod ();
 	   			// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 25/05/2016 ECU Note - set the orientation
			// 29/05/2016 ECU changed to use portrait orientation
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
					 				 this,
					 				 StaticData.ACTIVITY_FULL_SCREEN,
					 				 !StaticData.ACTIVITY_SCREEN_ON);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_video_viewer);
			// ---------------------------------------------------------------------
			// 18/06/2013 ECU now try and play the specified video
			// ---------------------------------------------------------------------
			videoView = (VideoView) findViewById (R.id.surface_view);
			videoView.setVideoURI (Uri.parse (videoFileName));
			videoView.setMediaController (new MediaController(this));
			videoView.requestFocus ();
			// ---------------------------------------------------------------------
			// 01/06/2016 ECU tell the user to touch the screen to control the video
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (getString (R.string.video_control),true);
			// ---------------------------------------------------------------------
			// 01/06/2016 ECU set up a listener to detect when the video has finished
			// ---------------------------------------------------------------------
			videoView.setOnCompletionListener (new MediaPlayer.OnCompletionListener() 
			{
			    public void onCompletion (MediaPlayer theMediaPlayer) 
			    {
			        // -------------------------------------------------------------
			    	// 01/06/2016 ECU indicate that the video has finished
			    	// -------------------------------------------------------------
			    	Utilities.popToastAndSpeak (getString (R.string.video_finished),true);
			    	// -------------------------------------------------------------
			    	// 03/06/2016 ECU check whether there is a method that needs to
			    	//                be called
			    	// 24/12/2016 ECU use the method to invoke the 'finish method'
			    	//                - the method checks if it has been defined
			    	//            ECU add the 'false' argument
			    	// -------------------------------------------------------------
			    	invokeFinishMethod (false);
			    	// -------------------------------------------------------------
			    	// 01/06/2016 ECU just finish this activity if required
			    	// -------------------------------------------------------------
			    	if (finishActivity)
			    		finish ();
			    	// -------------------------------------------------------------
			    }
			});
			// ---------------------------------------------------------------------
			// 01/06/2016 ECU set up the 'touch' listener
			// ---------------------------------------------------------------------
			videoView.setOnTouchListener (new View.OnTouchListener () 
			{
				@SuppressLint ("ClickableViewAccessibility")
				@Override
				public boolean onTouch (View theView, MotionEvent theMotionEvent) 
				{
					return false;
				}
			});
			// ---------------------------------------------------------------------
			// 01/06/2016 ECU Note - now start the video
			// ---------------------------------------------------------------------
			videoView.start ();
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
	// =============================================================================
	@Override
	public void onBackPressed ()
	{
		// -------------------------------------------------------------------------
		// 24/12/2016 ECU created to handle the BACK key
		// -------------------------------------------------------------------------
		// 24/12/2016 ECU invoke the 'finish method' if defined
		//            ECU add the 'true' argument
		// -------------------------------------------------------------------------
		invokeFinishMethod (true);
		// -------------------------------------------------------------------------
		// 24/12/2016 ECU finish this activity
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
		// 24/12/2016 ECU now call the super for this method
		// -------------------------------------------------------------------------
		super.onBackPressed();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public static void closeVideoPlayer ()
	{
		// -------------------------------------------------------------------------
		// 28/12/2016 ECU created to close any video that is being played
		// -------------------------------------------------------------------------
		if (videoView != null)
		{
			// ---------------------------------------------------------------------
			// 28/12/2016 ECU check if anything is being played - if so then stop it
			// ---------------------------------------------------------------------
			if (videoView.isPlaying())
			{
				// -----------------------------------------------------------------
				// 28/12/2016 ECU if playing then stop it. Remember if 'paused' then
				//                it will be shown as not playing
				// -----------------------------------------------------------------
				videoView.stopPlayback ();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------	
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void invokeFinishMethod (boolean theMessageFlag)
	{
		// -------------------------------------------------------------------------
		// 24/12/2016 ECU invoke the 'finish' method if one is defined
		// -------------------------------------------------------------------------
		if (finishMethod != null)
		{
			// ---------------------------------------------------------------------
			// 24/12/2016 ECU check whether the 'cancel message' is to be displayed
			// ---------------------------------------------------------------------
			if (theMessageFlag)
				Utilities.popToastAndSpeak (getString (R.string.video_cancelled),true);
			// ---------------------------------------------------------------------
			try 
			{
				// -----------------------------------------------------------------
				// 24/12/2016 ECU invoke the 'finish' method that was passed
				// -----------------------------------------------------------------
				finishMethod.invoke (null);
				// -----------------------------------------------------------------
			}
			catch (Exception theException) 
			{
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void videoFinished ()
	{
		// -------------------------------------------------------------------------
		// 03/06/2016 ECU created to be called when the video finishes playing
		// -------------------------------------------------------------------------
		PublicData.messageHandler.sendEmptyMessage (StaticData.MESSAGE_PROCESS_FINISHED);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

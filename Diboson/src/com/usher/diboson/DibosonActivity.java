package com.usher.diboson;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;

public class DibosonActivity extends Activity
{
	// =============================================================================
	// 22/10/2015 ECU created as a master activity to intercept various methods
	// 04/11/2015 ECU added the onKeyDown and onKeyLongPress methods
	// 22/12/2016 ECU decide on screen capture versus test documentation using
	//                a dialogue - previously screen capture was achieved using the
	//                repeated use of the 'volume up' button but found this very
	//                confusing
	// =============================================================================
	//private final static String TAG = "DibosonActivity";
	// =============================================================================
	static	Activity	activity;
	// =============================================================================
	
	// =============================================================================
	@Override 
	protected void onCreate (Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		// 22/12/2016 ECU call up the main method
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		// 22/12/2016 ECU remember the activity for later use
		// -------------------------------------------------------------------------
		activity = this;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onBackPressed ()
	{
		// -------------------------------------------------------------------------
		// 22/10/2015 ECU reset the flag to indicate that the user has 'exited' the
		//                activity and that it has not been killed by the Android OS
		// 24/10/2015 ECU changed to use the method
		// -------------------------------------------------------------------------
		PublicData.storedData.setLastActivity (StaticData.NO_RESULT);
		// -------------------------------------------------------------------------
		// 22/10/2015 ECU now call the super for this method
		// -------------------------------------------------------------------------
		super.onBackPressed();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override 
	protected void onPause() 
	{
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU unregister the accelerometer listener
		// -------------------------------------------------------------------------
		super.onPause (); 
	} 
	/* ============================================================================= */
	@Override 
	protected void onResume() 
	{ 
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU register the listener again
		// -------------------------------------------------------------------------
		// -------------------------------------------------------------------------
		super.onResume(); 
	} 
	// =============================================================================
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) 
	{
	    super.onSaveInstanceState(savedInstanceState);
	}
	// =============================================================================
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) 
	{
	    super.onRestoreInstanceState(savedInstanceState);
	}
	// =============================================================================
	@Override
	public boolean onKeyDown (int theKeyCode,KeyEvent theKeyEvent) 
	{
		// -------------------------------------------------------------------------
		// 04/11/2015 ECU created to handle the key being pressed
		//            ECU if BACK pressed then switch on tracking as want to detect
		//                the long press
		//            ECU only do in development mode
		// -------------------------------------------------------------------------
		if (PublicData.storedData.developmentMode)
		{
			// ---------------------------------------------------------------------
			// 14/12/2015 ECU in development mode so can check the keys
			// ---------------------------------------------------------------------
			if (theKeyCode == KeyEvent.KEYCODE_BACK)
			{
				// -----------------------------------------------------------------
				// 04/11/2015 ECU if BACK key pressed then switch on tracking
				// -----------------------------------------------------------------
				theKeyEvent.startTracking ();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 14/12/2015 ECU pass through for handling by the OS
		// -------------------------------------------------------------------------
	    return super.onKeyDown (theKeyCode, theKeyEvent);
	}
	// =============================================================================
	@Override
	public boolean onKeyLongPress (int theKeyCode, KeyEvent theKeyEvent) 
	{
		// -------------------------------------------------------------------------
		// 04/11/2015 ECU created to detect the long press of a key - am only 
		//                interested in the BACK key
		//            ECU only handle documentation if in development mode
		// -------------------------------------------------------------------------
		if (PublicData.storedData.developmentMode && theKeyCode == KeyEvent.KEYCODE_BACK)
		{
			// ---------------------------------------------------------------------
			// 04/11/2015 ECU just handle the BACK key
			// ---------------------------------------------------------------------
			// 22/12/2016 ECU put up the dialogue that gives the user the option
			//                to add documentation or do a screen capture
			// ---------------------------------------------------------------------
			DialogueUtilities.yesNo (activity,  
					 getString (R.string.documentation_title),
					 getString (R.string.documentation_summary),
					 null,
					 true,getString (R.string.documentation),Utilities.createAMethod   (DibosonActivity.class,"DocumentationMethod",(Object) null),
					 true,getString (R.string.screen_capture), Utilities.createAMethod (DibosonActivity.class,"ScreenCaptureMethod", (Object) null)); 
			// ---------------------------------------------------------------------
			// 04/11/2015 ECU indicate that the key has been handled
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	    return super.onKeyLongPress (theKeyCode, theKeyEvent);
	}
	// =============================================================================
	@Override
	public void onRestart () 
	{
		// -------------------------------------------------------------------------
		super.onStop();   
	}
	// =============================================================================
	
	// =============================================================================
	public static void DocumentationMethod (Object theDummyArgument)
	{
		// -------------------------------------------------------------------------
		// 22/12/2016 ECU add notes to the testing documentation
		// -------------------------------------------------------------------------
		// 11/11/2015 ECU see if there is 'testing_level' metadata for the 
		//                activity
		// -------------------------------------------------------------------------
		int localTestingLevel = StaticData.NO_RESULT;
		try 
		{
			ActivityInfo activityInfo
							  = activity.getPackageManager ().getActivityInfo (activity.getComponentName(), PackageManager.GET_META_DATA);
			localTestingLevel = (activityInfo.metaData).getInt (StaticData.PARAMETER_TESTING_LEVEL,StaticData.NO_RESULT);
		} 
		catch (Exception theException) 
		{
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 04/11/2015 ECU start up the documentation activity
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (activity,DocumentationActivity.class);
		localIntent.putExtra (StaticData.PARAMETER_ACTIVITY, activity.getClass().getSimpleName());
		localIntent.putExtra (StaticData.PARAMETER_TESTING_LEVEL, localTestingLevel);
		activity.startActivity (localIntent);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void ScreenCaptureMethod (Object theDummyArgument)
	{
		// -------------------------------------------------------------------------
		// 22/12/2016 ECU do a capture of the current screen
		// -------------------------------------------------------------------------
		Utilities.screenCapture (activity.getWindow().getDecorView().getRootView(),
                PublicData.projectFolder + activity.getString (R.string.screen_capture_directory) + StaticData.SCREEN_CAPTURE_FILE + 
                 "_" + (new SimpleDateFormat ("ddMMyyyyHHmmss",Locale.getDefault())).format (Utilities.getAdjustedTime(true)) + 
                 StaticData.EXTENSION_PHOTOGRAPH);
		// -------------------------------------------------------------------------
		// 14/12/2015 ECU indicate that the screen has been captured
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak (activity.getString (R.string.image_captured));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
}

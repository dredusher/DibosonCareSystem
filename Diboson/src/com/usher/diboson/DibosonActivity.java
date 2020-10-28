package com.usher.diboson;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.usher.diboson.utilities.LifeCycle;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DibosonActivity extends Activity
{
	// =============================================================================
	// 22/10/2015 ECU created as a master activity to intercept various methods
	// 04/11/2015 ECU added the onKeyDown and onKeyLongPress methods
	// 22/12/2016 ECU decide on screen capture versus test documentation using
	//                a dialogue - previously screen capture was achieved using the
	//                repeated use of the 'volume up' button but found this very
	//                confusing
	// 14/05/2020 ECU a good writeup on tasks and the stack is at :-
	//					https://developer.android.com/guide/components/activities/tasks-and-back-stack
	//                a good writeup of an activity's lifecycle can be seen at
	//            		https://developer.android.com/guide/components/activities/activity-lifecycle
	// =============================================================================
	private final static String TAG = "DibosonActivity";
	// =============================================================================

	// =============================================================================
	private final static boolean BACK_KEY_BUTTON	=	true;
	// =============================================================================

	// =============================================================================
	static	Activity	activity;
			int			backKey_X;
			int         backKey_Y;
	// =============================================================================
	
	// =============================================================================
	@Override 
	protected void onCreate (Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		// 24/04/2020 ECU display the event to help with monitoring the life cycle
		// -------------------------------------------------------------------------
		LifeCycleCheckMessage ("onCreate");
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
		// 24/04/2020 ECU display the event to help with monitoring the life cycle
		// -------------------------------------------------------------------------
		LifeCycleCheckMessage ("onBackPressed");
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
	// =============================================================================
	@Override
	protected void onDestroy ()
	{
		// -------------------------------------------------------------------------
		// 24/04/2020 ECU display the event to help with monitoring the life cycle
		// -------------------------------------------------------------------------
		LifeCycleCheckMessage ("onDestroy");
		// -------------------------------------------------------------------------
		// 21/04/2020 ECU added
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	protected void onNewIntent (Intent theIntent)
	{
		// -------------------------------------------------------------------------
		// 24/04/2020 ECU display the event to help with monitoring the life cycle
		// -------------------------------------------------------------------------
		LifeCycleCheckMessage ("onNewIntent");
		// -------------------------------------------------------------------------
		// 21/04/2020 ECU added to investigate the launch mode of the activity
		// -------------------------------------------------------------------------
		super.onNewIntent (theIntent);
		// -------------------------------------------------------------------------
		//setIntent (theIntent);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override 
	protected void onPause () 
	{
		// -------------------------------------------------------------------------
		// 24/04/2020 ECU display the event to help with monitoring the life cycle
		// -------------------------------------------------------------------------
		LifeCycleCheckMessage ("onPause");
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU Note - take actions when this activity loses focus
		// -------------------------------------------------------------------------
		super.onPause (); 
		// -------------------------------------------------------------------------
	} 
	/* ============================================================================= */
	@Override 
	protected void onResume () 
	{
		// -------------------------------------------------------------------------
		// 24/04/2020 ECU display the event to help with monitoring the life cycle
		// -------------------------------------------------------------------------
		LifeCycleCheckMessage ("onResume");
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU Note - take actions when this activity regains focus
		// -------------------------------------------------------------------------
		super.onResume (); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onSaveInstanceState (Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		// 24/04/2020 ECU display the event to help with monitoring the life cycle
		// -------------------------------------------------------------------------
		LifeCycleCheckMessage ("onSaveInstanceState");
		// -------------------------------------------------------------------------
	    super.onSaveInstanceState (savedInstanceState);
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onRestoreInstanceState (Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		// 24/04/2020 ECU display the event to help with monitoring the life cycle
		// -------------------------------------------------------------------------
		LifeCycleCheckMessage ("onRestoreInstanceState");
		// -------------------------------------------------------------------------
	    super.onRestoreInstanceState (savedInstanceState);
	    // -------------------------------------------------------------------------
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
	    // -------------------------------------------------------------------------
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
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onRestart () 
	{
		// -------------------------------------------------------------------------
		// 24/04/2020 ECU display the event to help with monitoring the life cycle
		// -------------------------------------------------------------------------
		LifeCycleCheckMessage ("onRestart");
		// -------------------------------------------------------------------------
		// 13/03/2018 ECU seemed to be a typing error was 'onStop' 
		// -------------------------------------------------------------------------
		super.onRestart (); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void setContentView (final int theLayoutID)
	{
		// -------------------------------------------------------------------------
		// 20/05/2020 ECU added to allow the layout to be modified
		// -------------------------------------------------------------------------
		super.setContentView (theLayoutID);
		// -------------------------------------------------------------------------
		// 20/05/2020 ECU decide whether the 'visual back key' is to be displayed
		// 21/05/2020 ECU added the check on 'backKeyDisplay'
		// -------------------------------------------------------------------------
		if (BACK_KEY_BUTTON && PublicData.storedData.backKeyDisplay)
		{
			// ---------------------------------------------------------------------
			// 20/05/2020 ECU want to display the more visual 'back key'
			// ---------------------------------------------------------------------
			// 20/05/2020 ECU get the view that contains the 'back button'
			// ---------------------------------------------------------------------
			View 		backButtonView 	= View.inflate (this, R.layout.back_key_button,null);
			// ---------------------------------------------------------------------
			// 02/07/2020 ECU add the cast
			// ---------------------------------------------------------------------
			ImageButton backButton 		= (ImageButton) backButtonView.findViewById (R.id.backKeyButton);
			// ---------------------------------------------------------------------
			// 23/05/2020 ECU decide where to display the 'back key' icon
			// ---------------------------------------------------------------------
			RelativeLayout.LayoutParams layoutParams
					= (RelativeLayout.LayoutParams)backButton.getLayoutParams ();
			// ---------------------------------------------------------------------
			// 23/05/2020 ECU if not changed by the user then use the default
			//                would normally set 'StaticData.NOT_SET' but instead
			//                check the default for an integer, which is 0.
			// ---------------------------------------------------------------------
			if (PublicData.storedData.backKeyDisplayX == 0)
			{
				// -----------------------------------------------------------------
				// 23/05/2020 ECU add the rules for the default position
				//                ideally want to set this in XML but would
				//                need to use 'removeRule' if the position is
				//                mooved and this only came in at API 17
				// -----------------------------------------------------------------
				layoutParams.addRule (RelativeLayout.ALIGN_PARENT_LEFT);
				layoutParams.addRule (RelativeLayout.ALIGN_PARENT_BOTTOM);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 23/05/2020 ECU user has changed the position
				// -----------------------------------------------------------------
				layoutParams.leftMargin = PublicData.storedData.backKeyDisplayX;
				layoutParams.topMargin  = PublicData.storedData.backKeyDisplayY;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 23/05/2020 ECU now position the icon
			// ---------------------------------------------------------------------
			backButton.setLayoutParams (layoutParams);
			// ---------------------------------------------------------------------
			// 20/05/2020 ECU set up the click listener for the 'back button'
			// ---------------------------------------------------------------------
			backButton.setOnClickListener(new View.OnClickListener()
			{
				// -----------------------------------------------------------------
				@Override
				public void onClick (View theView)
				{
					// -------------------------------------------------------------
					// 20/05/2020 ECU dispatch a 'back key' event - need to include
					//                both 'down' and 'up' to get working
					// -------------------------------------------------------------
					dispatchKeyEvent (new KeyEvent (KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
					dispatchKeyEvent (new KeyEvent (KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			});
			// ---------------------------------------------------------------------
			// 23/05/2020 ECU set up the 'long click' listener for the 'back button'
			// ---------------------------------------------------------------------
			backButton.setOnLongClickListener (new View.OnLongClickListener ()
			{
				// -----------------------------------------------------------------
				@Override
				public boolean onLongClick (View view)
				{
					// -------------------------------------------------------------
					// 07/01/2014 ECU tell the user about dragging the icon
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString (R.string.back_key_drag),true);
					// -------------------------------------------------------------
					// 23/05/2020 ECU now define the 'touch listener' to enable the
					//                icon to be 'dragged'
					// -------------------------------------------------------------
					view.setOnTouchListener (new View.OnTouchListener()
					{
						// ---------------------------------------------------------
						@Override
						public boolean onTouch (View theView, MotionEvent theEvent)
						{
							// -----------------------------------------------------
							switch (theEvent.getAction())
							{
								// -------------------------------------------------
							 	// -------------------------------------------------
								case MotionEvent.ACTION_DOWN:
									// ---------------------------------------------
								 	// 23/05/2020 ECU no action is required
								 	// ---------------------------------------------
								 	break;
								// -------------------------------------------------
								// -------------------------------------------------
								case MotionEvent.ACTION_MOVE:
								 	// ---------------------------------------------
								 	// 23/05/2020 ECU try and get the icon to follow
								 	//                the movement
								 	// ----------------------------------------------
									backKey_X = (int) theEvent.getRawX();
									backKey_Y = (int) theEvent.getRawY();
									// ---------------------------------------------
									// 23/05/2020 ECU now check if this is a valid movement
									// ---------------------------------------------
									if ((backKey_X > 0 && (backKey_X < (PublicData.screenWidth  - theView.getWidth ()))) &&
									    (backKey_Y > 0 && (backKey_Y < (PublicData.screenHeight - theView.getHeight ()))))
									{
									 	// -----------------------------------------
									 	// 23/05/2020 ECU this is a valid move
									 	// -----------------------------------------
										 theView.setX (backKey_X);
										 theView.setY (backKey_Y);
										 // ----------------------------------------
									}
									// ---------------------------------------------
								 	break;
								// -------------------------------------------------
								// -------------------------------------------------
								case MotionEvent.ACTION_UP:
									// ---------------------------------------------
									// 23/05/2020 ECU store the current position
									// ---------------------------------------------
									PublicData.storedData.backKeyDisplayX
										= (int) theEvent.getRawX () ;
									PublicData.storedData.backKeyDisplayY
										= (int) theEvent.getRawY ();
									// ---------------------------------------------
									// 23/05/2020 ECU want to switch off the
									//                listener - cannot find
									//                another way
									// ---------------------------------------------
									theView.setOnTouchListener (null);
									// ---------------------------------------------
								 	break;
								// -------------------------------------------------
							 }
							// -----------------------------------------------------
							// 23/05/2020ECU indicate that the event has been handled
							// -----------------------------------------------------
						 	return true;
						 	// -----------------------------------------------------
						}
					});
					// -------------------------------------------------------------
					return true;
				}
				// -----------------------------------------------------------------
			});
			// ---------------------------------------------------------------------
			// 20/05/2020 ECU now add the button to the main view
			// 02/07/2020 ECU add the cast
			// ---------------------------------------------------------------------
			ViewGroup rootLayout = (ViewGroup) findViewById(android.R.id.content);
			rootLayout.addView (backButtonView);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
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
			// ---------------------------------------------------------------------
			ActivityInfo activityInfo
							  = activity.getPackageManager ().getActivityInfo (activity.getComponentName(), PackageManager.GET_META_DATA);
			localTestingLevel = (activityInfo.metaData).getInt (StaticData.PARAMETER_TESTING_LEVEL,StaticData.NO_RESULT);
			// ---------------------------------------------------------------------
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
	void LifeCycleCheckMessage (String theMessage)
	{
		// -------------------------------------------------------------------------
		// 21/04/2020 ECU added when checking the lifecycle
		// 24/04/2020 ECU put in the check on 'debug'
		// 23/08/2020 ECU put in the check on null
		// 24/08/2020 ECU changed to use the new method
		// -------------------------------------------------------------------------
		LifeCycle.LogMessage (getLocalClassName(),theMessage);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void ScreenCaptureMethod (Object theDummyArgument)
	{
		// -------------------------------------------------------------------------
		// 22/12/2016 ECU do a capture of the current screen
		// -------------------------------------------------------------------------
		Utilities.screenCapture (activity.getWindow ().getDecorView ().getRootView (),
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

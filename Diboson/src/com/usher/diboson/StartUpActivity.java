package com.usher.diboson;

import android.os.Bundle;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;
import android.widget.TextView;

public class StartUpActivity extends DibosonActivity implements OnGestureListener
{
	// =============================================================================
	// 03/06/2013 ECU include OnGestureListener so that just touching the screen
	//                will finish this activity
	// 14/01/2014 ECU general tidy up with some commenting
	// 11/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// =============================================================================
	//final static String TAG = "StartUpActivity";
	// =============================================================================
	private GestureDetector gestureScanner;
	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);	
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 11/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 16/02/2014 ECU set up common activity features
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_start_up);
			// ---------------------------------------------------------------------	
			// 30/05/2013 ECU put version details in the title
			// 16/06/2013 ECU used stored strings rather than literal
			// 08/08/2013 ECU use 'lastUpdateTime' instead of '..build_date'
			// 12/09/2013 ECU store the copyright/build details in copyrightMessage so
			//                it can be used elsewhere
			// 03/06/2014 ECU changed to use the version from the manifest
			// 11/10/2015 ECU the setting up of the copyright message used to be here but
			//                this is now in MainActivity
			// 02/11/2015 ECU took out the title information because too much information
			//                is being displayed
			// ---------------------------------------------------------------------				
			// 02/11/2015 ECU setTitle (Utilities.Version (this) + " - " + getString(R.string.build_copyright) + 
			// 02/11/2015 ECU 				" - " + PublicData.lastUpdateDate);
			// ---------------------------------------------------------------------
			// 01/11/2015 ECU update the text view with the  copyright information
			// ---------------------------------------------------------------------
			((TextView) findViewById (R.id.start_up_textview)).setText (Utilities.Version (this) + " - " + 
							                                            getString(R.string.build_copyright) + "\n" +
							                                            PublicData.lastUpdateTime + " on " + PublicData.lastUpdateDate);
			// ---------------------------------------------------------------------	
			// 03/06/2013 ECU indicate gesture handling
			// ---------------------------------------------------------------------	
			gestureScanner = new GestureDetector (this,this);
			// ---------------------------------------------------------------------
			// 30/05/2013 ECU changed to use resource value
			// ---------------------------------------------------------------------	
			waitABit (getResources().getInteger (R.integer.start_up_delay));
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
	@Override
	public boolean onCreateOptionsMenu (Menu menu) 
	{
		return true;
	}
	// =============================================================================
	@Override
	public void onWindowFocusChanged (boolean hasFocus)
	{
		// -------------------------------------------------------------------------
		// 06/11/2013 ECU hasFocus = true  - view visible to the user
		//                           false - view no longer visible to user
		// -------------------------------------------------------------------------	
	}
	// =============================================================================
	private void waitABit (final int theWaitTime)
	{
		Thread thread = new Thread()
		 {
			@Override
		    public void run()
		    {
				try 
				{
					synchronized (this)
					{
						// ---------------------------------------------------------
						// 03/11/2013 ECU wait the specified amount of time
						// ---------------------------------------------------------
						wait (theWaitTime);
						// ---------------------------------------------------------
						// 03/11/2013 ECU just exit this activity
						// ---------------------------------------------------------
						finish ();
					}
				}
				catch (InterruptedException theException)
				{                    
				}       
		    }
		 };
		 // ------------------------------------------------------------------------
		 // 03/11/2013 ECU start the thread
		 // ------------------------------------------------------------------------
		 thread.start();        
	}
	// =============================================================================
	@Override
	public boolean onTouchEvent (MotionEvent motionEvent) 
	{
		return gestureScanner.onTouchEvent (motionEvent);
	}
	// =============================================================================
	@Override
	public void onDestroy() 
	{
	    // -------------------------------------------------------------------------
	    super.onDestroy();
	}
	// =============================================================================
	@Override
	public boolean onDown (MotionEvent motionEvent) 
	{
		return false;
	}
	// =============================================================================
	@Override
	public boolean onFling (MotionEvent motionEvent1,
							MotionEvent motionEvent2, 
							float velocityX,
							float velocityY) 
	{
		// ------------------------------------------------------------------------
		// 18/01/2015 ECU included just in case need to get to settings early
		// 11/09/2015 ECU add in the check on speed of swipe
		// ------------------------------------------------------------------------
		if (velocityX > StaticData.STARTUP_SWIPE_VELOCITY)
		{
			Intent localIntent = new Intent (getBaseContext(),SettingsActivity.class);
			startActivity (localIntent);
		}
	    // ------------------------------------------------------------------------
		return false;
	}
	// =============================================================================
	@Override
	public void onLongPress (MotionEvent motionEvent) 
	{
	}
	// =============================================================================
	@Override
	public boolean onScroll (MotionEvent motionEvent1, 
							 MotionEvent motionEvent2,
							 float distanceX, 
							 float distanceY) 
	{
		return false;
	}
	// =============================================================================
	@Override
	public void onShowPress (MotionEvent motionEvent) 
	{
	}
	// =============================================================================
	@Override
	public boolean onSingleTapUp (MotionEvent motionEvent) 
	{
		// -------------------------------------------------------------------------
		// 03/06/2013 ECU just finish this activity 
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------	
		return false;
	}
	// =============================================================================
	@Override 
	protected void onPause() 
	{
		// -------------------------------------------------------------------------
	   	super.onPause (); 
	} 
	// ============================================================================ 
	@Override
	public void onRestart () 
	{
		// -------------------------------------------------------------------------
		super.onStop();   
	}
	// ============================================================================ 
	@Override
	public void onStart () 
	{
		// -------------------------------------------------------------------------
		super.onStop();   
	}
	// ============================================================================ 
	@Override
	public void onStop() 
	{
		// -------------------------------------------------------------------------
		super.onStop();
	}
	// ============================================================================= 
}
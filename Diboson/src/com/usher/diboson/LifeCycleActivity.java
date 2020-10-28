package com.usher.diboson;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class LifeCycleActivity extends Activity
{
	
	// =============================================================================
	// 13/03/2018 ECU created to be an empty activity that it is used to monitor and
	//                understand the 'life cycle' of an android activity
	// =============================================================================
	static final String TAG = "LifeCycle";
	// =============================================================================
	
	// =============================================================================
	@Override 
	protected void onCreate (Bundle savedInstanceState) 
	{
		Log.i (TAG,"onCreate");
		// -------------------------------------------------------------------------
		// 13/03/2018 ECU this callback fires when the system first creates this
		//                activity - the activity enters the 'created state'
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
	}
	// ============================================================================
	
	// =============================================================================
	@Override 
	protected void onStart () 
	{
		Log.i (TAG,"onStart");
		// -------------------------------------------------------------------------
		// 13/03/2018 ECU when the activity enters the 'started' state then the
		//                system fires this callback
		// -------------------------------------------------------------------------
		super.onStart ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	@Override 
	protected void onResume() 
	{ 
		Log.i (TAG,"onResume");
		// -------------------------------------------------------------------------
		// 13/03/2018 ECU when the activity enters the 'resumed' state then the
		//                system fires this callback
		// -------------------------------------------------------------------------
		super.onResume(); 
	} 
	// =============================================================================
	
	// ============================================================================= 
	@Override 
	protected void onPause () 
	{
		Log.i (TAG,"onPause");
		// -------------------------------------------------------------------------
		// 13/03/2018 ECU the system fires this callback at the first indication that
		//                the user is leaving the activity - it does not always mean 
		//                that the activity is being destroyed
		// -------------------------------------------------------------------------
		super.onPause (); 
		// -------------------------------------------------------------------------
	} 
	// =============================================================================
	
	// ============================================================================= 
	@Override 
	protected void onStop () 
	{
		Log.i (TAG,"onStop");
		// -------------------------------------------------------------------------
		// 13/03/2018 ECU when the activity is no longer visible to the user then it
		//                has entered the 'stopped' state and the system will fire
		//                this callback
		// -------------------------------------------------------------------------
		super.onStop (); 
		// -------------------------------------------------------------------------
	} 
	// =============================================================================
	
	// ============================================================================= 
	@Override 
	protected void onDestroy () 
	{
		Log.i (TAG,"onDestroy");
		// -------------------------------------------------------------------------
		// 13/03/2018 ECU the system will fire this callback before the activity is
		//                'destroyed'. This is the final call that the activity
		//                receives. The system either invokes this callback because
		//                the activity is finishing due to to 'finish {}' being
		//                called or because the system is temporarily destroying the
		//                process containing the activity to save space. It is possible
		//                to distinguish between the two by checking 'isFinishing ()'.
		//
		//				  This callback releases all resources that have not yet been
		//                released by earlier callbacks, such as 'onStop ()'
		// -------------------------------------------------------------------------
		super.onDestroy (); 
		// -------------------------------------------------------------------------
	} 
	// =============================================================================

	// =============================================================================
	@Override
	public void onRestart () 
	{
		Log.i (TAG,"onRestart");
		// -------------------------------------------------------------------------
		// 13/03/2018 ECU If the activity is currently in the 'stopped' state and
		//                comes back to interact with the user then the system will
		//                fire this callback. If the activity has finished running
		//                then the 'onDestroy ()' callback will be called
		// -------------------------------------------------------------------------
		super.onRestart (); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	
	// =============================================================================
	@Override
	public void onSaveInstanceState (Bundle savedInstanceState) 
	{
		Log.i (TAG,"onSaveInstanceState");
		// -------------------------------------------------------------------------
		// 13/03/2018 ECU this callback is fired when the activity is temporarily 
		//                destroyed.
		//
		//                Normally called before the 'onPause' callback is fired
		// -------------------------------------------------------------------------
	    super.onSaveInstanceState (savedInstanceState);
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onRestoreInstanceState (Bundle savedInstanceState) 
	{
		Log.i (TAG,"onRestoreInstanceState");
		// -------------------------------------------------------------------------
		// 13/03/2018 ECU this callback will only be fired if an instance has
		//                previously been saved from the 'onSaveInstanceState'
		//                callback
		// -------------------------------------------------------------------------
	    super.onRestoreInstanceState (savedInstanceState);
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
	
}

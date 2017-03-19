package com.usher.diboson;

import android.os.Bundle;
import android.view.Menu;
// =================================================================================
public class TimerEventActivity extends DibosonActivity 
{
	// =============================================================================
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 02/11/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// =============================================================================
	
	// =============================================================================
	final static int MILLISECONDS_TO_WAIT	= (30 * 1000);
	/* ============================================================================= */
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
			setContentView(R.layout.activity_timer_event);
			// ---------------------------------------------------------------------
			// 28/06/2013 ECU added the delay after which the activity will finish
			// ---------------------------------------------------------------------
			waitABit (MILLISECONDS_TO_WAIT);
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
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return true;
	}
	/* ============================================================================= */
	private void waitABit (final int theWaitTime)
	{
		Thread thread = new Thread()
		 {
			@Override
		    public void run()
		    {
				try 
		        {
					synchronized(this)
		            {
						// ---------------------------------------------------------
						// 28/06/2013 ECU wait for the specified number of milliseconds
						//                and then stop this activity
						// ---------------------------------------------------------
						wait(theWaitTime);
		                finish ();
		                // ----------------------------------------------------------
		            }
		        }
		        catch(InterruptedException theException)
		        {                    
		        }       
		    }
		 };
		 // ------------------------------------------------------------------------
		 // 28/06/2013 ECU start up this thread
		 // ------------------------------------------------------------------------
		 thread.start();        
	}
	/* ============================================================================= */

}

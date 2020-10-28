package com.usher.diboson;

import android.os.Bundle;

// =================================================================================
// 22/10/2015 ECU changed to 'extends DibosonActivity'
// 24/10/2015 ECU put in the check as to whether the activity has been created
//                anew or is being recreated after having been destroyed by
//                the Android OS
// ================================================================================

public class MapActivity extends DibosonActivity 
{
	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 24/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			setContentView(R.layout.activity_map);
		}
		else
		{
			// ---------------------------------------------------------------------
			// 24/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
}

package com.usher.diboson;

import android.os.Bundle;
import android.widget.TextView;

public class InvadeActivity extends DibosonActivity 
{
	// =============================================================================
	// 29/10/2014 ECU created. This activity enables a remote device to take over
	//                control of this device.
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 29/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	//==============================================================================
	
	// -----------------------------------------------------------------------------
	TextView	phoneDetails;
	String		phoneNumber = null;
	// -----------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			setContentView(R.layout.activity_invade);
		
			Bundle extras = getIntent().getExtras();
		
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 29/10/2014 ECU try and get the phone number
				// -----------------------------------------------------------------
				phoneNumber = extras.getString(StaticData.PARAMETER_PHONE_NUMBER);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 29/10/2014 ECU display details of the invading phone
			// ---------------------------------------------------------------------
			phoneDetails  	= (TextView)findViewById (R.id.invade_text);
			// ---------------------------------------------------------------------
			// 29/10/2014 ECU now display the text
			// ---------------------------------------------------------------------
			phoneDetails.setText ("This device has been invaded by " +
					((phoneNumber != null) ? "a phone with the number '" + phoneNumber + "'" : "an unknown phone"));
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// -----------------------------------------------------------------------------
}

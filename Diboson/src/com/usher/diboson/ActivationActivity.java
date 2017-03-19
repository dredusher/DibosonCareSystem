package com.usher.diboson;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ActivationActivity extends DibosonActivity 
{
	/* ============================================================================= */
	// =============================================================================
	// 15/11/2014 ECU created to obtain the activation key and to perform the 
	//                necessary validation
	// 21/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	//==============================================================================
	/* ============================================================================= */
	//final static String TAG = "ActivationActivity";
	/* ============================================================================= */
	
	// =============================================================================
	String		activationKey;
	TextView	activationKeyTextView;
	Button		activationKeyButton;
	String		expectedActivationKey;
	// =============================================================================
	
	//==============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 21/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 15/11/2014 ECU set up the activity defaults
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this);
			// ---------------------------------------------------------------------
			setContentView(R.layout.activity_activation);
			// ---------------------------------------------------------------------
			// 15/11/2014 ECU set up the button
			// ---------------------------------------------------------------------
			activationKeyButton = ((Button)findViewById(R.id.activation_key_button));
			activationKeyButton.setOnClickListener(buttonListener);
			// ---------------------------------------------------------------------
			activationKeyTextView = (TextView) findViewById (R.id.activation_key_edittext);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 21/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	
	// ============================================================================= 
	private View.OnClickListener buttonListener = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{	
			// ---------------------------------------------------------------------
			// 15/11/2014 ECU handle the button
			// ---------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.activation_key_button: 
				{
					validateActivationKey (activationKeyTextView.getText().toString());
					break;
				}
			}
		}
	};
	// =============================================================================
	
	// =============================================================================
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		// -------------------------------------------------------------------------
		// 15/11/2014 ECU added to process the back key
		// -------------------------------------------------------------------------
	    if( keyCode == KeyEvent.KEYCODE_BACK) 
	    {	    	
	       	// ---------------------------------------------------------------------
	       	// 15/11//2014 ECU indicate that the key has been processed
	       	// ---------------------------------------------------------------------
	       	Utilities.popToastAndSpeak ("This key is not allowed",true);
	       	// ---------------------------------------------------------------------
	    	return true;
	    }
	    else
	    {
	        return super.onKeyDown(keyCode, event);
	    }
	}
	// =============================================================================
	
	// =============================================================================
	void validateActivationKey (String theEnteredKey)
	{
		// -------------------------------------------------------------------------
		// 15/11/2014 ECU this method processes the entered key
		// -------------------------------------------------------------------------
		if (theEnteredKey != null && theEnteredKey.length() != 0)
		{
			expectedActivationKey = getBaseContext().getString (R.string.app_name) +
			            (new SimpleDateFormat("ddMMyyyyHH",Locale.getDefault()).format(Utilities.getAdjustedTime(true)));
			
			if (theEnteredKey.equals(expectedActivationKey))
			{
				// -----------------------------------------------------------------
				// 15/11/2014 ECU the correct activation key has been entered
				//            ECU first of all initialise the activation key file
				//                and then exit this activity
				// -----------------------------------------------------------------
				Installation.initialise(this);
				finish ();
				// -----------------------------------------------------------------
			}
			else
			{
				Utilities.popToastAndSpeak ("You have not entered a valid key",true);
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 15/11/2014 ECU a null key has been entered
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak ("You have not entered a key",true);
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
}

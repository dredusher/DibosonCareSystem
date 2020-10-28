package com.usher.diboson;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LocationActionsActivity extends Activity
{
	// =============================================================================
	EditText 		actions;
	EditText 		altitude;
	EditText 		latitude;
	LocationActions locationActions;
	int 			locationActionsIndex;
	EditText 		longitude;
	EditText 		marker;
	// =============================================================================
	@Override 
	protected void onCreate (Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
		// -------------------------------------------------------------------------
		setContentView (R.layout.location_marker_edit);
		// -------------------------------------------------------------------------
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			// -----------------------------------------------------------------
			// 06/07/2019 ECU check for activity restart
			// -----------------------------------------------------------------
			locationActionsIndex = extras.getInt(StaticData.PARAMETER_DATA,StaticData.NOT_SET);
			// -----------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		actions   = (EditText) findViewById (R.id.input_actions);
		altitude  = (EditText) findViewById (R.id.input_altitude);
		latitude  = (EditText) findViewById (R.id.input_latitude);
		longitude = (EditText) findViewById (R.id.input_longitude);
		marker    = (EditText) findViewById (R.id.input_location_marker);
		// -------------------------------------------------------------------------
		// 16/10/2020 ECU set the 'actions' field to be scrollable
		// -------------------------------------------------------------------------
		actions.setMovementMethod (new ScrollingMovementMethod());
		// -------------------------------------------------------------------------
		locationActions = new LocationActions ();
		// -------------------------------------------------------------------------
		if (locationActionsIndex != StaticData.NOT_SET)
		{
			// ---------------------------------------------------------------------
			locationActions = PublicData.storedData.locationActions.get (locationActionsIndex);
			// ---------------------------------------------------------------------
			actions.setText   (locationActions.actions);
			latitude.setText  (Double.toString (locationActions.latitude));
			longitude.setText (Double.toString (locationActions.longitude));
			marker.setText    (locationActions.marker);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		((Button) findViewById (R.id.confirm_button)).setOnClickListener (new View.OnClickListener()
		{
			// ---------------------------------------------------------------------
			@Override
			public void onClick (View view)
			{
				// -----------------------------------------------------------------
				// 15/10/2020 ECU set the data from the input information
				// -----------------------------------------------------------------
				PublicData.storedData.locationActions.set (locationActionsIndex,getScreenData ());
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		});
		// -------------------------------------------------------------------------
		((Button) findViewById (R.id.new_entry_button)).setOnClickListener (new View.OnClickListener()
		{
			// ---------------------------------------------------------------------
			@Override
			public void onClick (View view)
			{

				// -----------------------------------------------------------------
				// 15/10/2020 ECU set the data from the input information
				// -----------------------------------------------------------------
				PublicData.storedData.locationActions.add (getScreenData());
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		});
		// -------------------------------------------------------------------------
		((Button) findViewById (R.id.actions_button)).setOnClickListener (new View.OnClickListener()
		{
			// ---------------------------------------------------------------------
			@Override
			public void onClick (View view)
			{

				// -----------------------------------------------------------------
				// 15/10/2020 ECU set the data from the input information
				// -----------------------------------------------------------------
				DialogueUtilitiesNonStatic.multilineTextInput (LocationActionsActivity.this,
															   LocationActionsActivity.this,
						                                       getString (R.string.location_marker_actions_title),
						                                       getString (R.string.action_command_summary),
						                                       5,
						                                       locationActions.actions,
						                                       Utilities.createAMethod (LocationActionsActivity.class,"LocationActionsActions",StaticData.BLANK_STRING),
						                                       null,
						                                       StaticData.NO_RESULT,
						                                       getString (R.string.press_to_define_command));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		});
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	protected void onDestroy ()
	{
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	LocationActions getScreenData ()
	{
		// -------------------------------------------------------------------------
		// 15/10/2020 ECU create a record from the data entered on the screen
		// -------------------------------------------------------------------------
		return (new LocationActions (
									Double.parseDouble (latitude.getText().toString()),
									Double.parseDouble (longitude.getText().toString()),
									marker.getText().toString (),
				                    actions.getText().toString ()
									));
		// ------------------------------------------------------------------------
	}
	// =============================================================================
	public void LocationActionsActions (String theActions)
	{
		// -------------------------------------------------------------------------
		// 16/10/2020 ECU created to store commands that are required for the selected
		//                selection
		// -------------------------------------------------------------------------
		actions.setText (theActions);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

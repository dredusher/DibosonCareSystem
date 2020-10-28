package com.usher.diboson;

import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class DoseActivity extends DibosonActivity 
{
	// ===============================================================================
	// 17/01/2014 ECU created
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 24/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//==============================================================================
	//private static final String TAG = "DoseActivity";
	/* ============================================================================= */
	EditText 	amount;	
	int 		dayIndex;									// 27/11/2014 ECU added
	Button		deleteDoseButton;							// 27/11/2014 ECU added
	int			doseIndex = 0;								// 27/11/2014 ECU added
	DoseDaily	doseDaily;
	DoseTime 	doseTime = new DoseTime ();					// 27/11/2014 ECU added
	int			medicationIndex = StaticData.NO_RESULT;	// 27/11/2014 ECU added
	TextView 	notes;
	TimePicker 	timePicker;
	TextView 	units;
	/* ============================================================================= */
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
			// 16/02/2014 ECU call up routine to set common activity features
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this);
			setContentView(R.layout.dose_time);
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU make sure that the soft keyboard does not pop up
			// ---------------------------------------------------------------------
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			// ---------------------------------------------------------------------
			// 17/01/2014 ECU the activity could be called with the index of the day 
			// 				  being processed only needed for updating the title
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
		
			if (extras !=null) 
			{
				dayIndex = extras.getInt (StaticData.PARAMETER_DAY);
				// -----------------------------------------------------------------
				// 17/01/2014 ECU set up the title
				// 11/12/2016 ECU change to use daysOfWeek
				// -----------------------------------------------------------------
				setTitle ("Doses for Day : " + PublicData.daysOfTheWeek [dayIndex]);  
				// -----------------------------------------------------------------
				// 27/11/2014 ECU feed through the index to medication details
				// -----------------------------------------------------------------
				medicationIndex = extras.getInt (StaticData.PARAMETER_SELECTION);
				// -----------------------------------------------------------------		
			}
			// ---------------------------------------------------------------------			
			// 17/01/2014 ECU set up pointers to the various views
			// 27/11/2014 ECU added the delete button
			// ---------------------------------------------------------------------
			((Button)findViewById(R.id.dose_time_enter)).setOnClickListener(buttonListener);
			((Button)findViewById(R.id.dose_time_finish)).setOnClickListener(buttonListener);
			// ---------------------------------------------------------------------
			// 27/11/2014 ECU set up the delete dose button which may or may not be
			//                visible
			// ---------------------------------------------------------------------
			deleteDoseButton = (Button) findViewById (R.id.dose_time_delete);
			deleteDoseButton.setOnClickListener(buttonListener);
			// ---------------------------------------------------------------------
			// 27/11/2014 ECU make sure that it is initially invisible
			// ---------------------------------------------------------------------
			deleteDoseButton.setVisibility (View.INVISIBLE);
			// ---------------------------------------------------------------------
			timePicker = (TimePicker)findViewById (R.id.dose_time_timePicker);
			amount     = (EditText)findViewById (R.id.dose_time_amount);
			units      = (TextView)findViewById (R.id.dose_time_units);
			notes      = (TextView)findViewById (R.id.dose_time_notes);
			// ---------------------------------------------------------------------
			// 16/01/2014 ECU set 24 hour format on time picker
			// ---------------------------------------------------------------------
			timePicker.setIs24HourView(true);
			// ---------------------------------------------------------------------
			// 17/01/2014 ECU set up the object that will contain input data
			// ---------------------------------------------------------------------
			doseDaily = new DoseDaily ();
			// ---------------------------------------------------------------------
			// 27/11/2014 ECU check if a particular medical record is being changed
			// ---------------------------------------------------------------------
			if (medicationIndex != StaticData.NO_RESULT && (PublicData.medicationDetails.size() > medicationIndex))
			{
				// -----------------------------------------------------------------
				// 27/11/2014 ECU put in the check on 'null'
				// -----------------------------------------------------------------
				if (PublicData.medicationDetails.get(medicationIndex).dailyDoseTimes [dayIndex] != null)
				{
					doseDaily = PublicData.medicationDetails.get(medicationIndex).dailyDoseTimes [dayIndex];
					// -------------------------------------------------------------
					// 27/11/2014 ECU preset some fields
					// -------------------------------------------------------------
					ClearTheForm ();
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
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
	/* ============================================================================= */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		
		return true;
	}
	/* ============================================================================= */
	private View.OnClickListener buttonListener = new View.OnClickListener() 
	{
		
		@Override
		public void onClick(View view) 
		{	
			// 07/01/2014 ECU now process depending on which button pressed
			
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.dose_time_enter:
				{
					// -------------------------------------------------------------
					// 17/01/2014 ECU get the data from the screen
					// -------------------------------------------------------------
					EnterDoseData ();
					// -------------------------------------------------------------
					// 17/01/2014 ECU reset the display - ready for next input
					// -------------------------------------------------------------
					ClearTheForm ();
					// -------------------------------------------------------------
					break;
				}
				// -----------------------------------------------------------------
				case R.id.dose_time_finish:
				{
					FinishInputOfData ();
					
					break;
				}
				// -----------------------------------------------------------------
				case R.id.dose_time_delete:
				{
					// -------------------------------------------------------------
					// 27/11/2014 ECU delete the displayed dose
					// -------------------------------------------------------------
					doseDaily.doseTimes.remove(doseIndex);
					// -------------------------------------------------------------
					ClearTheForm ();
					// -------------------------------------------------------------
					break;
				}
				// -----------------------------------------------------------------
			}
		}
	};
	/* ============================================================================= */
	void ClearTheForm ()
	{
		// -------------------------------------------------------------------------
		// 17/01/2014 ECU clear the input fields in the form - only those that may
		//                change between inputs
		// 27/11/2014 ECU preset the data if entry already exists
		// -------------------------------------------------------------------------
		if (doseDaily.doseTimes.size() > doseIndex)
		{
			amount.setText(Float.toString(doseDaily.doseTimes.get(doseIndex).dose.amount));
			units.setText(doseDaily.doseTimes.get(doseIndex).dose.units);
			notes.setText(doseDaily.doseTimes.get(doseIndex).notes);
			// ---------------------------------------------------------------------
			timePicker.setCurrentHour(doseDaily.doseTimes.get(doseIndex).hours);
			timePicker.setCurrentMinute(doseDaily.doseTimes.get(doseIndex).minutes);
			// ---------------------------------------------------------------------
			deleteDoseButton.setVisibility(View.VISIBLE);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 27/11/2014 ECU just clear the fields
			// ---------------------------------------------------------------------
			amount.setText 	("1.0");
			units.setText	(StaticData.BLANK_STRING);
			notes.setText 	(StaticData.BLANK_STRING);
			// ---------------------------------------------------------------------
			deleteDoseButton.setVisibility(View.INVISIBLE);
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	void EnterDoseData ()
	{
		// -------------------------------------------------------------------------
		// 17/01/2014 ECU call clearFocus to ensure that if time entered from keyboard
		//                then it is picked up correctly
		// -------------------------------------------------------------------------			
		timePicker.clearFocus();
		// -------------------------------------------------------------------------			
		// 16/01/2014 ECU changed to reflect use of List<>
		// 27/11/2014 ECU check if adding or just replacing an existing entry
		// -------------------------------------------------------------------------
		if (doseDaily.doseTimes.size() > doseIndex)
		{
			// ---------------------------------------------------------------------
			// 27/11/2014 ECU an entry already exists so just update
			// ---------------------------------------------------------------------
			doseTime = doseDaily.doseTimes.get(doseIndex);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 27/11/2014 ECU need a new entry
			// ---------------------------------------------------------------------			
			doseTime = new DoseTime ();			
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 27/11/2014 ECU update the stored information
		// -------------------------------------------------------------------------
		doseTime.hours   = timePicker.getCurrentHour();
		doseTime.minutes = timePicker.getCurrentMinute();
		doseTime.notes   = notes.getText ().toString();
		doseTime.dose    = new Dose ();
					
		doseTime.dose.amount = Float.parseFloat(amount.getText ().toString());
		doseTime.dose.units = units.getText ().toString();
		// -------------------------------------------------------------------------
		// 17/01/2014 ECU add into the chain of dose details
		// -------------------------------------------------------------------------
		if (doseDaily.doseTimes.size() > doseIndex)
		{ 
			// ---------------------------------------------------------------------
			// 27/11/2014 ECU replace an existing entry
			// ---------------------------------------------------------------------
			doseDaily.doseTimes.set (doseIndex,doseTime);
		}
		else
		{
			// ---------------------------------------------------------------------
			// 27/11/2014 ECU add a new entry
			// ---------------------------------------------------------------------
			doseDaily.doseTimes.add(doseTime);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 27/11/2014 ECU get ready for another dose
		// -------------------------------------------------------------------------
		doseIndex++;
		// -------------------------------------------------------------------------
	}	
	/* ============================================================================= */
	void FinishInputOfData ()
	{
		// 17/01/2014 ECU have input all of the required doses
		
		// 17/01/2014 ECU check whether any details have been entered
		
		if (doseDaily.doseTimes.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 17/01/2014 ECU now try and pass the dose information to the calling activity
			//            ECU do not want to get into Parcelable so copy into into shared
			//                data
			// ---------------------------------------------------------------------
			Intent resultIntent = new Intent();
			resultIntent.putExtra(StaticData.PARAMETER_DOSE,doseDaily);
			setResult(RESULT_OK, resultIntent);
			// ---------------------------------------------------------------------
			// 17/01/2014 ECU just finish the activity
    		// ---------------------------------------------------------------------
			finish ();
		}
		else
		{
			// 17/01/2014 ECU no details have been entered so reject the finish
			
			Utilities.popToast (getString(R.string.no_details_entered));
		}
	}
	/* ============================================================================= */
}

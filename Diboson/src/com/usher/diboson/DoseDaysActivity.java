package com.usher.diboson;

import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class DoseDaysActivity extends DibosonActivity 
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
	//private static final String TAG = "DoseDaysActivity";
	/* ============================================================================= */
	DoseDaily []		dailyDoses		= new DoseDaily [StaticData.DAYS_PER_WEEK];
	int					dayIndex		= 0;
	Boolean [] 			daysChosen 		= new Boolean [] {false,false,false,false,false,false,false};
	MedicationDetails	medicationDetails = new MedicationDetails ();	// 27/11/2014 ECU added here
	int					medicationIndex = StaticData.NO_RESULT;	// 27/11/2014 ECU added
	/* ============================================================================= */
	// 27/11/2014 ECU changed to have an array rather than individual check box
	// -----------------------------------------------------------------------------
	CheckBox [] dayCheckBoxes = new CheckBox [StaticData.DAYS_PER_WEEK];
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
				
			setContentView(R.layout.dose_daily);
			// ---------------------------------------------------------------------
			// 27/11/2014 ECU check if a parameter has been supplied
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
 		
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 27/11/2014 ECU get the possible index of the associated medication
				//                index
				// -----------------------------------------------------------------
				medicationIndex = extras.getInt(StaticData.PARAMETER_SELECTION, StaticData.NO_RESULT);
				// -----------------------------------------------------------------
			}
 		
			// ---------------------------------------------------------------------
			// 17/01/2014 ECU set up pointers to the various views
			// ---------------------------------------------------------------------
			dayCheckBoxes[0]  = (CheckBox) findViewById (R.id.dose_daily_monday);
			dayCheckBoxes[1]  = (CheckBox) findViewById (R.id.dose_daily_tuesday);
			dayCheckBoxes[2]  = (CheckBox) findViewById (R.id.dose_daily_wednesday);
			dayCheckBoxes[3]  = (CheckBox) findViewById (R.id.dose_daily_thursday);
			dayCheckBoxes[4]  = (CheckBox) findViewById (R.id.dose_daily_friday);
			dayCheckBoxes[5]  = (CheckBox) findViewById (R.id.dose_daily_saturday);
			dayCheckBoxes[6]  = (CheckBox) findViewById (R.id.dose_daily_sunday);
			// ---------------------------------------------------------------------
			// 17/01/2014 ECU set up appropriate listeners
			// ---------------------------------------------------------------------
			((Button)findViewById(R.id.dose_daily_enter)).setOnClickListener(processEnteredDays);
			// ---------------------------------------------------------------------
			// 27/11/2014 ECU change to accommodate the new checkbox array
			// ---------------------------------------------------------------------
			for (int theDay = 0; theDay < StaticData.DAYS_PER_WEEK; theDay++)
					(dayCheckBoxes[theDay]).setOnClickListener(checkDays);
			// ---------------------------------------------------------------------
			// 27/11/2014 ECU check if the fields need to be preset
			// ---------------------------------------------------------------------
			if (medicationIndex != StaticData.NO_RESULT && 
						(PublicData.medicationDetails.size() > medicationIndex))
			{
				// -----------------------------------------------------------------
				// 27/11/2014 ECU get the currently set medication details
				// -----------------------------------------------------------------
				medicationDetails = PublicData.medicationDetails.get(medicationIndex);
				// -----------------------------------------------------------------
				// 27/11/2014 ECU make sure that the correct listener bits are set
				// -----------------------------------------------------------------
				for (int theDay = 0; theDay < StaticData.DAYS_PER_WEEK; theDay++)
				{
					// -------------------------------------------------------------
					// 27/11/2014 ECU preset the check box and the associated listener
					// -------------------------------------------------------------
					dayCheckBoxes [theDay].setChecked(medicationDetails.DoseExists(theDay));
					daysChosen [theDay] = medicationDetails.DoseExists(theDay);
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
	private View.OnClickListener checkDays = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{
			// ---------------------------------------------------------------------
			// 17/01/2014 ECU set the array elements to the state of corresponding check
			//                box
			// 27/11/2014 ECU change to accommodate the new array
			// ---------------------------------------------------------------------
			for (int theDay = 0; theDay < StaticData.DAYS_PER_WEEK; theDay++)
				daysChosen [theDay] = dayCheckBoxes [theDay].isChecked();
			// ----------------------------------------------------------------------
		}
	};
	/* ============================================================================== */
	private View.OnClickListener processEnteredDays = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{
			dayIndex = GetNextDayToProcess (0);
			
			if (dayIndex == StaticData.NO_RESULT)
			{
				Utilities.popToast	("You have not selected any days");
			}
			else
			{
				GetDoseInformationForADay (dayIndex);
			}
		}
	};
	/* ============================================================================= */
	@Override
	public void onActivityResult(int theRequestCode, int theResultCode, Intent theIntent) 
	{
		// -------------------------------------------------------------------------
		// 30/08/2013 ECU called when an activity returns a result
		// -------------------------------------------------------------------------
		// 30/08/2013 ECU check if the result of the barcode activity
		// -------------------------------------------------------------------------
		if (theRequestCode == StaticData.REQUEST_CODE_DOSE)
		{
			if (theResultCode == RESULT_OK)
			{
				dailyDoses [dayIndex] = (DoseDaily) theIntent.getSerializableExtra(StaticData.PARAMETER_DOSE);
				// -----------------------------------------------------------------	
				// 17/01/2014 ECU now try and get the next day
				// -----------------------------------------------------------------
				dayIndex++;
				
				dayIndex = GetNextDayToProcess (dayIndex);
				
				if (dayIndex == StaticData.NO_RESULT)
				{
					// -------------------------------------------------------------
					Utilities.popToast	("All days have been processed");
					// -------------------------------------------------------------
					// 17/01/2014 ECU now want to pass the results back
					// 27/11/2014 ECU took out the definition of medicationDetails
					//                from here and put at the top of class
					// -------------------------------------------------------------
					medicationDetails.dailyDoseTimes = dailyDoses;
					Intent resultIntent = new Intent();
					resultIntent.putExtra(StaticData.PARAMETER_DOSE,medicationDetails);
					setResult(RESULT_OK, resultIntent);			
					// -------------------------------------------------------------
					// 17/01/2014 ECU can finish this activity
					// -------------------------------------------------------------
					finish ();
				}
				else
				{	// -------------------------------------------------------------
					GetDoseInformationForADay (dayIndex);
					// -------------------------------------------------------------
				}
			}
		}    
	}
	/* ===================================================================================== */
	void GetDoseInformationForADay (int theDayIndex)
	{
		// ---------------------------------------------------------------------------------
		// 17/01/2014 ECU start up the activity to acquire the dose information for each day
		// ---------------------------------------------------------------------------------
		Intent myIntent = new Intent (getBaseContext(),DoseActivity.class); 
		myIntent.putExtra (StaticData.PARAMETER_DAY,theDayIndex);
		// ---------------------------------------------------------------------------------
		// 27/11/2014 ECU pass through the index to the medication details being changed
		// ---------------------------------------------------------------------------------
		myIntent.putExtra (StaticData.PARAMETER_SELECTION,medicationIndex);
		// ---------------------------------------------------------------------------------
		startActivityForResult (myIntent,StaticData.REQUEST_CODE_DOSE); 
		// ---------------------------------------------------------------------------------
	}
	/* ===================================================================================== */
	int GetNextDayToProcess (int theCurrentIndex)
	{
		if (theCurrentIndex < daysChosen.length)
		{
			if (daysChosen [theCurrentIndex])
			{
				return theCurrentIndex;
			}
			else
			{
				return GetNextDayToProcess (++theCurrentIndex);
			}
		}
		else
		{
			return StaticData.NO_RESULT;
		}
	}
	/* ===================================================================================== */
}

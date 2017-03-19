package com.usher.diboson;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MedicationInput extends DibosonActivity implements TextWatcher
{
	// ===============================================================================
	// 23/06/2013 ECU created
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	// 04/12/2013 ECU same issue as with AlarmActivity when a time is entered using
	//                the keyboard - need to call clearFocus() on the picker in order
	//                to get the input value
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 02/11/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 30/03/2016 ECU NOTE - have been adding checks on hashcode's to check if the
	//                data has changed but the logic, at the moment, does not require
	//                it for medication details because just browsing the data does
	//                not cause a write to disk.
	//==============================================================================
	//private static final String TAG = "MedicationInput";
	/* ============================================================================= */

	/* ============================================================================= */
			Context		context;							// 14/12/2015 ECU added
			Boolean		keepExistingDoseTimes  	= false;	// 16/01/2014 ECU added
			Button		medicationDeleteButton;				// 16/01/2014 ECU added
			MedicationDetails	
						medicationDetails;					// 16/01/2014 ECU added 
			TextView 	medicationDescription;
			RadioGroup 	medicationForm;	
			int			medicationIndex 		= StaticData.NO_RESULT;	
															// 16/01/2014 ECU added
			TextView 	medicationName;
	static 	TextView 	medicationPhoto;					// 23/06/2013 ECU added	
			Button		medicationResetButton;				// 16/01/2014 ECU added
	// =============================================================================
	
	// =============================================================================
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
			// 28/11/2016 ECU call method to set the screen up
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true,true,false);
			// ---------------------------------------------------------------------
			setContentView (R.layout.medication_details);
			// ---------------------------------------------------------------------
			setTitle ("Enter Medication Details");
			// ---------------------------------------------------------------------
			// 14/12/2015 ECU save the context for later use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU make sure that the soft keyboard does not pop up
			// ---------------------------------------------------------------------
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			// ---------------------------------------------------------------------
			// 30/05/2013 ECU check if already have details on disk
			// 05/01/2014 ECU added in the debugMode check
			// 16/01/2014 ECU took out the code for reading from disk
			// ---------------------------------------------------------------------		
			// 29/05/2013 ECU loop for medication
			// ---------------------------------------------------------------------
			PublicData.gettingMedication = true;			// 22/06/2013 moved to MainActivity
		
			medicationName 			= (TextView) findViewById (R.id.medication_name);
			medicationDescription	= (TextView) findViewById (R.id.medication_description);
			medicationPhoto			= (TextView) findViewById (R.id.medication_photo);
			medicationForm 			= (RadioGroup) findViewById(R.id.medication_form_radiogroup);
			// ---------------------------------------------------------------------
			// 16/01/2014 ECU set up the delete button
			// ---------------------------------------------------------------------
			medicationDeleteButton = (Button) findViewById (R.id.medication_delete_button);
			medicationDeleteButton.setOnClickListener(deleteMedication);
			// ---------------------------------------------------------------------
			// 16/01/2014 ECU set up the reset timer button
			// ---------------------------------------------------------------------
			medicationResetButton = (Button) findViewById (R.id.medication_reset_button);
			medicationResetButton.setOnClickListener(resetMedication);
			// ---------------------------------------------------------------------
			// 29/05/2013 ECU put up the medication detail request
			// ---------------------------------------------------------------------
			((Button) findViewById (R.id.medication_form_enter)).setOnClickListener (enterMedicationDetails);
			((Button) findViewById (R.id.medication_form_finish)).setOnClickListener (finishMedicationDetails);
				
			medicationForm = (RadioGroup) findViewById (R.id.medication_form_radiogroup);
			// ---------------------------------------------------------------------		
			// 10/12/2013 ECU try and set clickable events for the photo field
			// ---------------------------------------------------------------------		
			medicationPhoto.setClickable (true);
			medicationPhoto.setOnClickListener (GetMedicationPhoto);	
			// ---------------------------------------------------------------------		
			// 16/01/2014 ECU try and picked up text changes in name field
			// ---------------------------------------------------------------------
			medicationName.addTextChangedListener(this);
			// ---------------------------------------------------------------------
			// 24/03/2014 ECU check whether the activity has been called with the index
			//                to a record of medication details
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
		    
			if (extras != null)
			{
	 	   		// -----------------------------------------------------------------
				// 23/03/2014 ECU get the medication index from the intent
				// 24/03/2014 ECU changed to use _SELECTION
				// -----------------------------------------------------------------
	 	   		medicationIndex	 = extras.getInt (StaticData.PARAMETER_SELECTION,StaticData.NO_RESULT);  
	 	   		// ----------------------------------------------------------------- 
	 	   		// 24/03/2014 ECU now try and display the specified record
	 	   		// -----------------------------------------------------------------
	 	   		DisplayTheRecord (medicationIndex);
	 	   		// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
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
	private View.OnClickListener deleteMedication = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{
			if (medicationIndex != StaticData.NO_RESULT)
			{
				// -----------------------------------------------------------------
				// 16/01/2014 ECU can delete this medication
				// -----------------------------------------------------------------
				PublicData.medicationDetails.remove(medicationIndex);
				
				medicationIndex = StaticData.NO_RESULT;
				
				DisplayTheRecord (medicationIndex);
			}
		}
	};
	/* ============================================================================= */
	private View.OnClickListener enterMedicationDetails = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{
			// ---------------------------------------------------------------------
			// 29/05/2013 ECU get main details of the medication
			// 16/01/2014 ECU changed to reflect the use of List<>
			// ---------------------------------------------------------------------
			medicationDetails = new MedicationDetails ();
			
			medicationDetails.name = medicationName.getText ().toString();
			// ---------------------------------------------------------------------
			// 16/01/2014 ECU check that a name has been given
			// ---------------------------------------------------------------------
			if (!medicationDetails.name.equalsIgnoreCase(""))
			{
				medicationDetails.description = medicationDescription.getText ().toString();
				// -----------------------------------------------------------------
				// 24/03/2014 ECU change so that only the path relative to the project
				//                folder is stored
				// 16/11/2016 ECU changed to use 'getRela...' rather than 'Rela...'
				// -----------------------------------------------------------------
				medicationDetails.photo = Utilities.getRelativeFileName (medicationPhoto.getText ().toString());	// 23/06/2013 ECU added
			
				int medicationFormID = medicationForm.getCheckedRadioButtonId();
				RadioButton inputForm = (RadioButton) findViewById (medicationFormID);
				medicationDetails.form = inputForm.getText ().toString();
				// -----------------------------------------------------------------
				// 16/01/2014 ECU check if the dose administration times are to changes
				// -----------------------------------------------------------------
				if (!keepExistingDoseTimes || medicationIndex == StaticData.NO_RESULT)
				{ 
					// -------------------------------------------------------------
					// 29/05/2013 ECU now get the details of doses for this medication
					// -------------------------------------------------------------
					GetDoseData ();
				}
				else
				{
					// 16/01/2014 ECU copy across the stored details ready for update - bit clumsy
					
					medicationDetails.dailyDoseTimes = PublicData.medicationDetails.get(medicationIndex).dailyDoseTimes;
					
					// 16/01/2014 ECU as not going through dose handling then update the record
					
					PublicData.medicationDetails.set(medicationIndex, medicationDetails);
				}
			}
			else
			{
				// 16/01/2014 ECU indicate that certain fields need to be completed
				
				Utilities.popToast	("You must specify the medication's name");
			}
		}
	};
	/* ============================================================================= */
	@Override
	public void onActivityResult (int theRequestCode, int theResultCode, Intent theIntent) 
	{
		// -------------------------------------------------------------------------
		// 30/08/2013 ECU called when an activity returns a result
		// -------------------------------------------------------------------------
		// 30/08/2013 ECU check if the result of the barcode activity
		// 14/12/2015 ECU removed the code for picking a photo
		// -------------------------------------------------------------------------
	    if (theRequestCode == StaticData.REQUEST_CODE_DOSE)
		{
			if (theResultCode == RESULT_OK)
			{
				MedicationDetails returnedMedicationDetails 
					= (MedicationDetails) theIntent.getSerializableExtra(StaticData.PARAMETER_DOSE);
				
				medicationDetails.dailyDoseTimes = returnedMedicationDetails.dailyDoseTimes;
				// -----------------------------------------------------------------
				// 16/01/2014 ECU as not going through dose handling then update the record
				// 17/01/2014 ECU check whether to add or replace an existing record
				// -----------------------------------------------------------------
				if (medicationIndex == StaticData.NO_RESULT)
					PublicData.medicationDetails.add (medicationDetails);
				else
					PublicData.medicationDetails.set(medicationIndex, medicationDetails);
				// -----------------------------------------------------------------
				// 17/01/2014 ECU clear the form ready for the next input
				// -----------------------------------------------------------------
				DisplayTheRecord (StaticData.NO_RESULT);
				// -----------------------------------------------------------------
			}
		}
	}
	/* ============================================================================= */
	private View.OnClickListener finishMedicationDetails = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{
			// ---------------------------------------------------------------------
			// 29/05/2013 ECU check if any doses have been entered for today
			// ---------------------------------------------------------------------
			PublicData.gettingMedication = false;			// 22/06/2013 ECU move to MainActivity
			// ---------------------------------------------------------------------
			// 30/05/2013 ECU before finishing write all details to disk
			// 16/01/2014 ECU changed to use method in Utilities
			// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities
			// 30/03/2016 ECU changed to use the new method
			// ---------------------------------------------------------------------
			WriteDataToDisk (getBaseContext());
			// ---------------------------------------------------------------------
			// 14/12/2015 ECU want the DailyScheduler to do a refresh
			// ---------------------------------------------------------------------
			DailyScheduler.ProcessMedicationDetails (getBaseContext());
			// ---------------------------------------------------------------------
			// 29/05/2013 ECU terminate this activity
			// ---------------------------------------------------------------------			
			finish ();
		}
	};
	/* ============================================================================= */
	private View.OnClickListener resetMedication = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{
			// ---------------------------------------------------------------------
			// 16/01/2014 ECU indicate that the timers are to be re-input
			// ---------------------------------------------------------------------
			keepExistingDoseTimes = false;
		}		
	};
	/* ============================================================================= */
	private View.OnClickListener GetMedicationPhoto = new View.OnClickListener() 
	{
		@Override
		public void onClick(View theView) 
		{
			// ---------------------------------------------------------------------
			// 10/12/2013 ECU try and select a photo
			// 01/09/2015 ECU changed to use StaticData
			// 14/12/2015 ECU changed to use dialogue rather than Utilities.PickAFile
			// 15/12/2015 ECU added 'true' to indicate show image
			// 17/12/2015 ECU changed to use FileChooser.displayImage to determine
			//                if images are to be displayed
			//            ECU changed to use new selectAFile method
			// ---------------------------------------------------------------------
			Utilities.selectAFile (context,StaticData.EXTENSION_PHOTOGRAPH,
					new MethodDefinition <MedicationInput> (MedicationInput.class,"SelectedPhotograph"));
			// ---------------------------------------------------------------------
		}		
	};
	// =============================================================================
	@Override
    public void onDestroy()
    {
		// -------------------------------------------------------------------------
		// 14/12/2015 ECU added
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
    }
	/* ============================================================================= */
	@Override
	public void afterTextChanged(Editable theData) 
	{
		CheckForMedication (theData.toString());
	}
	/* ============================================================================= */
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,	int after) 
	{
	}
	/* ============================================================================= */
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) 
	{	
	}
	/* ============================================================================= */
	void CheckForMedication (String theInputString)
	{	
		if (PublicData.medicationDetails.size() > 0)
		{

			for (int theIndex = 0; theIndex < PublicData.medicationDetails.size(); theIndex++)
			{
				MedicationDetails localMedicationDetails = PublicData.medicationDetails.get(theIndex);
				
				if (!theInputString.equalsIgnoreCase("") && localMedicationDetails.name.startsWith(theInputString))
				{
					// -------------------------------------------------------------
					// 16/01/2014 ECU remember the index of this medication
					// -------------------------------------------------------------
					medicationIndex = theIndex;
					// -------------------------------------------------------------
					// 16/01/2014 ECU enable the delete and reset buttons button
					// -------------------------------------------------------------
					medicationDeleteButton.setVisibility(View.VISIBLE);
					medicationResetButton.setVisibility(View.VISIBLE);
					// -------------------------------------------------------------	
					// 16/01/2014 ECU disable the listener
					// -------------------------------------------------------------	
					medicationName.removeTextChangedListener(this);
						
					medicationName.setText (localMedicationDetails.name);
					medicationDescription.setText (localMedicationDetails.description);
					medicationPhoto.setText (localMedicationDetails.photo);

				    for (int index = 0; index < medicationForm.getChildCount(); index++)
				    {	
				    	if (((RadioButton)medicationForm.getChildAt (index)).getText().toString().equalsIgnoreCase(localMedicationDetails.form))
				    	{
				    		((RadioButton)medicationForm.getChildAt (index)).setChecked(true);
				    	}		       
				    }
				    // -------------------------------------------------------------
				    // 16/01/2014 ECU indicate that existing times are to be kept
				    // -------------------------------------------------------------
				    keepExistingDoseTimes = true;
				    // -------------------------------------------------------------
				    // 16/01/2014 ECU tell user was is happening (true means centred)
				    // --------------------------------------------------------------
				    Utilities.popToast ("The existing administration times will\nbe kept unless you press the 'reset' button",true);
				}
			}			
		}
	}
	/* ============================================================================= */
	void DisplayTheRecord (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 27/11/2014 ECU add in the check on size
		// -------------------------------------------------------------------------
		if (theIndex != StaticData.NO_RESULT && (theIndex < PublicData.medicationDetails.size()))
		{
			MedicationDetails localMedicationDetails = PublicData.medicationDetails.get(theIndex);
			// ---------------------------------------------------------------------
			// 16/01/2014 ECU enable the delete button
			// ---------------------------------------------------------------------
			medicationDeleteButton.setVisibility(View.VISIBLE);
			// ---------------------------------------------------------------------
			// 16/01/2014 ECU disable the listener
			// ---------------------------------------------------------------------	
			medicationName.removeTextChangedListener(this);
				
			medicationName.setText (localMedicationDetails.name);
			medicationDescription.setText (localMedicationDetails.description);
			medicationPhoto.setText (localMedicationDetails.photo);

		    for (int index = 0; index < medicationForm.getChildCount(); index++)
		    {	
		    	if (((RadioButton)medicationForm.getChildAt (index)).getText().toString().equalsIgnoreCase(localMedicationDetails.form))
		    	{
		    		((RadioButton)medicationForm.getChildAt (index)).setChecked(true);
		    	}		       
		    }
		}
		else
		{
			// ---------------------------------------------------------------------
			// 16/01/2014 ECU enable the delete button
			//            ECU added the Reset button
			// ---------------------------------------------------------------------
			medicationDeleteButton.setVisibility(View.INVISIBLE);
			medicationResetButton.setVisibility(View.INVISIBLE);
			// ---------------------------------------------------------------------
			// 16/01/2014 ECU clear fields
			// ---------------------------------------------------------------------				
			medicationName.setText ("");
			medicationDescription.setText ("");
			medicationPhoto.setText ("");
			
			((RadioButton)medicationForm.getChildAt (0)).setChecked(true);
			// ---------------------------------------------------------------------
			// 16/01/2014 ECU indicate that a new record needs times entered
			// ---------------------------------------------------------------------
			keepExistingDoseTimes = false;
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	void GetDoseData ()
	{
		// -------------------------------------------------------------------------
		// 17/01/2014 ECU get the data associated with this medication
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (getBaseContext(),DoseDaysActivity.class); 
		// -------------------------------------------------------------------------
		// 27/11/2014 ECU try and pass through the index to the medication that
		//                is being created or edited
		// -------------------------------------------------------------------------
		localIntent.putExtra(StaticData.PARAMETER_SELECTION,medicationIndex);
		// -------------------------------------------------------------------------
		startActivityForResult (localIntent,StaticData.REQUEST_CODE_DOSE);
	}
    // =============================================================================
    public static void WriteDataToDisk (Context theContext)
    {
    	// -------------------------------------------------------------------------
    	// 30/03/2016 ECU created to write the data to disk
    	// -------------------------------------------------------------------------
    	AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + 
				theContext.getString (R.string.medication_details_file),PublicData.medicationDetails);
    	// -------------------------------------------------------------------------	
    }
    // =============================================================================
	
    // =============================================================================
    // =============================================================================
    // 30/03/2016 ECU Note - decalre the methods used in the various dialogues
	// =============================================================================
    // =============================================================================
	public static void SelectedPhotograph (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 14/12/2015 ECU created to be called when a file is selected in the dialogue
		// -------------------------------------------------------------------------
		medicationPhoto.setText (theFileName); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SynchronisedFile (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 18/12/2015 ECU created to be called when the medication details file
		//                has been synchronised from another device
		// -------------------------------------------------------------------------
		DailyScheduler.ProcessMedicationDetails (theContext);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SwipeHandler (int theMedicationIndex)
	{
		// -------------------------------------------------------------------------
		// 13/12/2015 ECU created to handle the swipe gesture
		//            ECU request confirmation of the deletion
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (Selector.context,"Medication Deletion",
			    		   "Do you really want to delete the entry for '" + PublicData.medicationDetails.get(theMedicationIndex).name + "'",
			    		   (Object) theMedicationIndex,
			    		   Utilities.createAMethod (MedicationInput.class,"YesMethod",(Object) null),
			    		   null); 
		// -------------------------------------------------------------------------
	}
   	// =============================================================================
   	public static void YesMethod (Object theMedicationIndex)
   	{
   		// -------------------------------------------------------------------------
   		// 13/12/2015 ECU confirmation of the deletion has been received
   		// -------------------------------------------------------------------------
   		int localIndex = (Integer) theMedicationIndex;
   		PublicData.medicationDetails.remove (localIndex);
   		// -------------------------------------------------------------------------
   		// 13/12/2015 ECU make sure the data is updated on disc
   		// 30/03/2016 ECU changed to use the new method
   		// -------------------------------------------------------------------------
   		WriteDataToDisk (Selector.context);
   		// -------------------------------------------------------------------------
   		// 13/12/2015 ECU rebuild and then display the updated list view
   		// -------------------------------------------------------------------------
   		Selector.Rebuild();
   		// -------------------------------------------------------------------------
   	}
   	// =============================================================================
}

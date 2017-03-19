package com.usher.diboson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.os.Bundle;
import android.os.Message;
import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MedicationActivity extends DibosonActivity implements OnGestureListener 
{
	// ===============================================================================
	// 23/06/2013 ECU created
	// 16/01/2014 ECU major changes to reflect that MainActivity.medicationDetails
	//                      has changed from a [] to a List<>
	// 11/02/2014 ECU changed to use text-to-speech 
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 24/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//================================================================================
	private static final String TAG	=	"MedicationActivity";
	/* ====================================================================== */
	private GestureDetector gestureScanner;
	/* ====================================================================== */
	static Context		context;					// 20/12/2015 ECU added
		   int 			theHour;
		   int 			theMinute;
		   int 			theMedication;
		   int 			theDailyDose;
		   int 			theDose;
		   DoseTime		doseTime;					// 09/03/2016 ECU added
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
			setContentView (R.layout.activity_medication);
			// ---------------------------------------------------------------------
			// 20/12/2015 ECU save the context for later use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 23/06/2013 ECU set up various views
			// 15/01/2014 ECU just change the variable names
			// 16/01/2014 ECU changed to reflect the use of List<>
			// 24/03/2014 ECU added the 'description'
			// ---------------------------------------------------------------------
			ImageView medicationImageView 	= (ImageView)findViewById(R.id.medicationImageView2);
			TextView medicationName 	 	= (TextView)findViewById (R.id.medication_namex);
			TextView medicationAmount 	 	= (TextView)findViewById (R.id.medication_amountx);
			TextView medicationDescription	= (TextView)findViewById (R.id.medication_descriptionx);
			TextView medicationNotes 	  	= (TextView)findViewById (R.id.medication_notesx);
			// ---------------------------------------------------------------------
			// 19/03/2014 ECU add the new text view field to show date and time
			// ---------------------------------------------------------------------
			TextView medicationDate 	  = (TextView)findViewById (R.id.medication_date);
			TextView medicationDay  	  = (TextView)findViewById (R.id.medication_day);
			TextView medicationTime 	  = (TextView)findViewById (R.id.medication_time);
			// ---------------------------------------------------------------------
			// 23/06/2013 ECU added
			// ---------------------------------------------------------------------
			Bundle medicationExtras = getIntent().getExtras();
	    
			if (medicationExtras != null)
			{
				// -----------------------------------------------------------------
				// 13/12/2015 ECU have a problem that when the app is restarted and
				//                a prompt for a dose of medication has to be actioned
				//                then get an 'index out of bounds' exception. Do not
				//                know what is happening but in the short-term just put
				//                in a try/catch
				// -----------------------------------------------------------------
				try
				{
					// -------------------------------------------------------------
					// 15/01/2014 ECU changed to use PARAMETER_ from MainActivity
					// -------------------------------------------------------------
 	   				theHour 				= getIntent().getIntExtra (StaticData.PARAMETER_HOUR,0); 	
 	   				theMinute 				= getIntent().getIntExtra (StaticData.PARAMETER_MINUTE,0); 
 	   				theMedication		 	= getIntent().getIntExtra (StaticData.PARAMETER_MEDICATION,0); 
 	   				theDailyDose 			= getIntent().getIntExtra (StaticData.PARAMETER_DOSE_TIME,0);
 	   				theDose 				= getIntent().getIntExtra (StaticData.PARAMETER_DOSE,0);
 	   				// -------------------------------------------------------------
 	   				// 09/03/206 ECU try and get dose time
 	   				// -------------------------------------------------------------
 	   				doseTime 				= (DoseTime) medicationExtras.getSerializable (StaticData.PARAMETER_OBJECT);
 	   				// -------------------------------------------------------------	
 	   				// 24/06/2013 ECU added
 	   				// -------------------------------------------------------------	
 	   				setTitle (String.format ("Medication to be taken at %02d:%02d",theHour,theMinute));
 	   				// -------------------------------------------------------------
 	   				// 23/06/2013 ECU make major changes
 	   				// 10/12/2013 ECU change because want to use the full path
 	   				// 17/01/2014 ECU added the amount in the display
 	   				//			  ECU change the call to displayAnImage so that a default
 	   				//				  'drawable' image can be displayed if the requested 
 	   				//				   image cannot be found
 	   				// 24/03/2014 ECU display the description
 	   				//            ECU change to used AbsolutePath
 	   				// 01/09/2015 ECU changed to use StaticData
 	   				// -------------------------------------------------------------
 	   				Utilities.displayAnImage(medicationImageView,Utilities.AbsoluteFileName(PublicData.medicationDetails.get(theMedication).photo),StaticData.IMAGE_SAMPLE_SIZE,R.drawable.medication);
		
 	   				medicationName.setText	("Medication : " 	+ PublicData.medicationDetails.get(theMedication).name);
 	   				// -------------------------------------------------------------
 	   				// 28/11/2014 ECU check if the required data exists
 	   				// 09/03/2016 ECU check for the 'object' option
 	   				// -------------------------------------------------------------
 	   				if (doseTime == null)
 	   				{
 	   					if (PublicData.medicationDetails.get(theMedication).dailyDoseTimes[theDailyDose] != null &&
 	   							PublicData.medicationDetails.get(theMedication).dailyDoseTimes[theDailyDose].doseTimes.size() > theDose)
 	   					{
 	   						medicationAmount.setText("Amount : " 		+ PublicData.medicationDetails.get(theMedication).dailyDoseTimes[theDailyDose].doseTimes.get(theDose).dose.amount +
 	   								" " + PublicData.medicationDetails.get(theMedication).dailyDoseTimes[theDailyDose].doseTimes.get(theDose).dose.units);
 	   						medicationNotes.setText	("Notes : " 		+ PublicData.medicationDetails.get(theMedication).dailyDoseTimes[theDailyDose].doseTimes.get(theDose).notes);
 	   					}
 	   					else
 	   					{
 	   						// -----------------------------------------------------
 	   						// 28/11/2014 ECU preset some fields
 	   						// -----------------------------------------------------
 	   						medicationAmount.setText ("No Amount Specified");
 	   						medicationNotes.setText ("No Notes Specified");
 	   						// -----------------------------------------------------
 	   					}
 	   				}
 	   				else
 	   				{
 	   					// ---------------------------------------------------------
 	   					// 09/03/2016 ECU display the information from dose time
 	   					// ---------------------------------------------------------
 	   					medicationAmount.setText("Amount : " 		+ doseTime.dose.amount + " " + doseTime.dose.units);
						medicationNotes.setText	("Notes : " 		+ doseTime.notes);
						// ---------------------------------------------------------
 	   				}
 	   				// -------------------------------------------------------------
 	   				medicationDescription.setText	("Description : " 		+ PublicData.medicationDetails.get(theMedication).description);
 	   				// -------------------------------------------------------------
 	   				// 19/03/2014 ECU display details of the date and time
 	   				// 10/11/2014 ECU changed to use Locale.getDefault instead of Locale.UK
 	   				// 27/10/2016 ECU changed to use dateSimpleFormat
 	   				// -------------------------------------------------------------
 	   				medicationDate.setText (Utilities.getAdjustedTime (PublicData.dateSimpleFormat));
 	   				medicationDay.setText (Utilities.getAdjustedTime(new SimpleDateFormat ("EEEE",Locale.getDefault())));	
 	   				medicationTime.setText (Utilities.getAdjustedTime(new SimpleDateFormat ("HH:mm:ss",Locale.getDefault())));
 	   				// -------------------------------------------------------------	   
 	   				// 15/01/2014 ECU re-arranged the code
 	   				// -------------------------------------------------------------
 	   				gestureScanner = new GestureDetector(this,this);
 	   				// -------------------------------------------------------------
 	   				// 23/06/2013 ECU add button handling here
 	   				// -------------------------------------------------------------
 	   				((Button)findViewById(R.id.dose_confirm)).setOnClickListener(confirmDose);
 	   				((Button)findViewById(R.id.dose_reject)).setOnClickListener(rejectDose);
 	   				// -------------------------------------------------------------
 	   				// 21/12/2015 ECU start up the prompt mechanism
 	   				// -------------------------------------------------------------
 	   				PromptForUserResponse (true);     
 	   				// -------------------------------------------------------------
				}
				catch (Exception theException)
				{
					// -------------------------------------------------------------
					// 13/12/2015 ECU log the exception
					// -------------------------------------------------------------
					Utilities.LogToProjectFile(TAG,"Exception " + theException);
					// -------------------------------------------------------------
				}
			}
			else
			{
				// -----------------------------------------------------------------
				// 15/01/2014 ECU nothing to do so just exit this activity
				// -----------------------------------------------------------------
				finish ();
			}
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
    public boolean onTouchEvent(MotionEvent me) 
	 {
	        return gestureScanner.onTouchEvent(me);
	 }
    // =============================================================================
 	@Override
 	public void onBackPressed () 
 	{
 	    // -------------------------------------------------------------------------
 		// 21/12/2015 ECU try and prevent the use of the 'back' key
 		// 10/04/2016 ECU let the user know what is happening
 		// -------------------------------------------------------------------------
 		Utilities.BackKeyNotAllowed (this);
 		// -------------------------------------------------------------------------
 	}
	// =============================================================================
	@Override
    public void onDestroy()
    {
		// -------------------------------------------------------------------------
		// 20/12/2015 ECU added
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
    }
	/* ============================================================================= */
	@Override
	public boolean onDown(MotionEvent arg0) 
	{
		return false;
	}
	/* ============================================================================= */
	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) 
	{
		return false;
	}
	/* ====================================================================== */
	@Override
	public void onLongPress(MotionEvent arg0) 
	{
		finish ();
	}
	/* ====================================================================== */
	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) 
	{
		return false;
	}
	/* ====================================================================== */
	@Override
	public void onShowPress(MotionEvent arg0) 
	{
	}
	/* ====================================================================== */
	@Override
	public boolean onSingleTapUp(MotionEvent arg0) 
	{
		return false;
	}
	/* ====================================================================== */
	private View.OnClickListener confirmDose = new View.OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			// ---------------------------------------------------------------------
			// 29/05/2013 ECU terminate this activity
			// 08/11/2013 ECU use the custom toast
			// ---------------------------------------------------------------------
			Utilities.popToast (getString(R.string.dose_given));
			// ---------------------------------------------------------------------
			// 20/12/2015 ECU indicate that the user has acted
			// ---------------------------------------------------------------------
			PromptForUserResponse (false);
			// ---------------------------------------------------------------------
			finish ();
		}
	};
	/* ========================================================================== */
	private View.OnClickListener rejectDose = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{
			// ---------------------------------------------------------------------
			// 29/05/2013 ECU terminate this activity
			// 08/11/2013 ECU use the custom toast
			// ---------------------------------------------------------------------
			Utilities.popToast(getString(R.string.dose_not_given));
			// ---------------------------------------------------------------------
			// 05/01/2014 ECU request a reason from the user and then process
			// ---------------------------------------------------------------------
			Intent myIntent = new Intent (getBaseContext(),GetMessage.class);	
			startActivityForResult (myIntent,StaticData.REQUEST_CODE_FILE);
			// ---------------------------------------------------------------------
			// 20/12/2015 ECU indicate that the user has acted
			// ---------------------------------------------------------------------
			PromptForUserResponse (false);
			// ---------------------------------------------------------------------
		}
	};
	/* ========================================================================== */
	@Override
	public void onActivityResult (int theRequestCode, int theResultCode, Intent theIntent) 
	{
		// -------------------------------------------------------------------------
		// 30/08/2013 ECU called when an activity returns a result
		// -------------------------------------------------------------------------
		// 30/08/2013 ECU check if the result of the barcode activity
		// -------------------------------------------------------------------------
	    if (theRequestCode == StaticData.REQUEST_CODE_FILE)
	    {
	    	// ---------------------------------------------------------------------
	    	// 06/12/2013 ECU added
	    	// ---------------------------------------------------------------------
	    	if (theResultCode == RESULT_OK)
	    	{
	    		String message   = theIntent.getStringExtra(StaticData.PARAMETER_MESSAGE);	
	    		// -----------------------------------------------------------------
	    		// 04/01/2014 ECU send an email to indicate that medication not taken
	    		// 07/01/2014 ECU change the alignment
	    		// 28/11/2014 ECU put in the check of whether doses exist
	    		//            ECU change the method called because getting errors
	    		//                with network on UI thread
				// -----------------------------------------------------------------
				PublicData.emailDetails.TimedEmail(getBaseContext(), "Medication Not Taken",
							"Time         : " + theHour + ":" + theMinute + "\n" + 
	 	   					"Medication   : " + PublicData.medicationDetails.get(theMedication).name +"\n" +
	 	   					"Description  : " + PublicData.medicationDetails.get(theMedication).description + "\n" +
	 	   					"Form         : " + PublicData.medicationDetails.get(theMedication).form + "\n" +
	 	   					"Photo        : " + PublicData.medicationDetails.get(theMedication).photo + "\n" +
	 	   					((PublicData.medicationDetails.get(theMedication).dailyDoseTimes[theDailyDose] != null &&
	 	   						PublicData.medicationDetails.get(theMedication).dailyDoseTimes[theDailyDose].doseTimes.size() > theDose)
	 	   					 ? ("Amount       : " + PublicData.medicationDetails.get(theMedication).dailyDoseTimes[theDailyDose].doseTimes.get(theDose).dose.amount + "\n" +
	 	   							 "Units        : " + PublicData.medicationDetails.get(theMedication).dailyDoseTimes[theDailyDose].doseTimes.get(theDose).dose.units + "\n" +
	 	   							 "Notes        : " + PublicData.medicationDetails.get(theMedication).dailyDoseTimes[theDailyDose].doseTimes.get(theDose).notes) : ""),
	 	   					"Reason Given for the Dose not being given is\n" + message);
				// -----------------------------------------------------------------
				finish ();
	    	}
	    	else 
	 	    if (theResultCode == RESULT_CANCELED) 
	 	    {
	 	    	// -----------------------------------------------------------------
	 	    	// Handle cancel
	 	    	// -----------------------------------------------------------------
	 	    }
	    }
	}
	// =============================================================================
	public static String dosesPerDay (int theDayOfWeek)
	{
		// -------------------------------------------------------------------------
		// 11/12/2016 ECU created to generate a list of doses, irrespective of medication,
		//				  that are to be taken on the specified day
		// 28/02/2017 ECU changed to use the new method
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU generate a list of relevant medication for this day
		// -------------------------------------------------------------------------
		List <MedicationDetails> medicationDetails =  medicationByDay (theDayOfWeek);
		// -------------------------------------------------------------------------
		DoseDaily 			localDoseDaily;
		String				localDoseSummary = "";
		MedicationDetails 	localMedicationDetails;
		String				localSeparator = "";
		// -------------------------------------------------------------------------
		// 11/12/2016 ECU declare the summary string and initialise to the day being
		//                processed
		// 11/12/2016 ECU changed to use daysOfTheWeek
		// 02/03/2017 ECU changed to use the StaticData separator rather than SystemInfoActivity
		// -------------------------------------------------------------------------
		String				localSummary = StaticData.SEPARATOR + 
				                           PublicData.daysOfTheWeek [theDayOfWeek] + StaticData.NEWLINE +
				                           StaticData.SEPARATOR;
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU changed to use the local 'medicationDetails' which has
		//                filtered only relevant objects
		// -------------------------------------------------------------------------
		if (medicationDetails.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 11/12/2016 ECU loop through all stored medications
			// 28/02/2017 ECU changed to scan local 'medicationDetails'
			// ---------------------------------------------------------------------
			for (int medication = 0; medication < medicationDetails.size(); medication++)
			{
				localMedicationDetails = medicationDetails.get (medication);
				// -----------------------------------------------------------------
				// 11/12/2016 ECU now loop through any doses against this medication
				// -----------------------------------------------------------------
				if ((localMedicationDetails.dailyDoseTimes != null) &&
					(localMedicationDetails.dailyDoseTimes [theDayOfWeek] != null))
				{
					// -------------------------------------------------------------
					// 11/12/2016 ECU there seem to be doses on this day for the
					//                medication being checked
					// -------------------------------------------------------------
					localDoseDaily = localMedicationDetails.dailyDoseTimes [theDayOfWeek];
					// -------------------------------------------------------------
					// 11/12/2016 ECU now scan through the doses
					// -------------------------------------------------------------
					if ((localDoseDaily.doseTimes != null) && (localDoseDaily.doseTimes.size() > 0))
					{
						// -----------------------------------------------------
						// 11/12/2016 ECU add a separator between medications
						// 02/03/2017 ECU changed to use StaticData SEPARATOR
						//                rather than SystemInfoActivity
						// -----------------------------------------------------
						localDoseSummary   += localSeparator;
						localSeparator 		= StaticData.SEPARATOR_LOWER;
						// ---------------------------------------------------------
						// 11/12/2016 ECU print the details of the medication to
						//                which this dose applies
						// ---------------------------------------------------------
						localDoseSummary += localMedicationDetails.PrintMedication ();
						// ---------------------------------------------------------
						// 11/12/2016 ECU loop through the doses
						// ---------------------------------------------------------
						for (int dose = 0; dose < localDoseDaily.doseTimes.size(); dose++)
						{
							// -----------------------------------------------------
							// 11/12/2016 ECU this is a dose that is of interest
							// -----------------------------------------------------
							localDoseSummary += "Dose Time    : " + localDoseDaily.doseTimes.get(dose).Print() + StaticData.NEWLINE;
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 11/12/2016 ECU need to merge strings
		// -------------------------------------------------------------------------
		if (!Utilities.emptyString (localDoseSummary))
			localDoseSummary = "no doses today";
		localSummary += localDoseSummary;
		// -------------------------------------------------------------------------
		// 11/12/2016 ECU return the generated string
		// -------------------------------------------------------------------------
		return localSummary;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void EmailMedicationMethod (Object theDummyArgument)
	{
		// -------------------------------------------------------------------------
		// 11/12/2016 ECU email the medication details sorted by medication
		// -------------------------------------------------------------------------
		MedicationDetails.EmailMedicationDetails (true);
		// -------------------------------------------------------------------------	
	}
	// =============================================================================
	public static void EmailMedicationByDayMethod (Object theDummyArgument)
	{
		// -------------------------------------------------------------------------
		// 11/12/2016 ECU email the medication details sorted by day
		// -------------------------------------------------------------------------
		MedicationDetails.EmailMedicationDetails (false);
		// -------------------------------------------------------------------------	
	}
	// =============================================================================
	public static List<MedicationDetails> generateSummary (long theDate)
	{
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU created to return a summary of medication for the specified
		//                date
		//            ECU change to return the retrieved list
		// -------------------------------------------------------------------------
		return medicationByDay (Utilities.DayOfWeek (theDate));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static List<MedicationDetails> medicationByDay (int theDay)
	{
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU created to return a list of medication which have doses on
		//                the specified day
		// -------------------------------------------------------------------------
		DoseDaily 				localDoseDaily;
		List<MedicationDetails> medicationDetails = new ArrayList<MedicationDetails>();
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU loop through all stored medication
		// --------------------------------------------------------------------------
		if ((PublicData.medicationDetails != null) && (PublicData.medicationDetails.size() > 0))
		{
			// ---------------------------------------------------------------------
			// 28/02/2017 ECU loop through all stored medications
			// ---------------------------------------------------------------------
			for (int medication = 0; medication < PublicData.medicationDetails.size(); medication++)
			{
				// -----------------------------------------------------------------
				// 28/02/2017 ECU now check if there are any doses for this medication
				//                on the specified day
				// -----------------------------------------------------------------
				localDoseDaily = PublicData.medicationDetails.get (medication).dailyDoseTimes [theDay];
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				// 28/02/2017 ECU check for doses
				// -----------------------------------------------------------------
				if ((localDoseDaily != null) && (localDoseDaily.doseTimes != null) && (localDoseDaily.doseTimes.size() > 0))
				{
					// -------------------------------------------------------------
					// 28/02/2017 ECU have medication of interest so add it in
					// -------------------------------------------------------------
					medicationDetails.add (PublicData.medicationDetails.get(medication));
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU return the generated list
		// -------------------------------------------------------------------------
		return medicationDetails;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void PromptForUserResponse (boolean theRequiredState)
	{
		// -------------------------------------------------------------------------
		// 21/12/2015 ECU created to handle the starting/stopping of the prompt
		//                that tells the user that they have to either accept or reject
		//                the displayed dose
		//
		//                theRequiredState  = true		start the mechanism
		// 								    = false		end the mechanism
		// ------------------------------------------------------------------------
		if (theRequiredState)
		{
			// --------------------------------------------------------------------
			// 21/12/2015 ECU start the mechanism after setting up the arguments
			// --------------------------------------------------------------------
			Message localMessage = PublicData.messageHandler.obtainMessage ();
			localMessage.what = StaticData.MESSAGE_PROMPT_DOSE_START;
			localMessage.arg1 = StaticData.DOSE_RESPONSE_PROMPT;
			localMessage.obj  = (Object) (new String [] {context.getString(R.string.time_for_medication),
														 context.getString(R.string.prompt_for_dose)});
			// ---------------------------------------------------------------------
			// 21/12/2015 ECU now send the message to the handler
			// ---------------------------------------------------------------------
			PublicData.messageHandler.sendMessage (localMessage);
			// ---------------------------------------------------------------------
		}
		else
		{
			// --------------------------------------------------------------------
			// 21/12/2015 ECU stop the mechanism
			// --------------------------------------------------------------------
			PublicData.messageHandler.sendEmptyMessage(StaticData.MESSAGE_PROMPT_DOSE_END);
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	
}

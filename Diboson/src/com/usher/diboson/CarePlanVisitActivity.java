package com.usher.diboson;

import java.util.Arrays;
import java.util.Calendar;

import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

public class CarePlanVisitActivity extends DibosonActivity

{
	/* =============================================================================== */
	// ===============================================================================
	// 12/01/2014 ECU created
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 24/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 06/02/2016 ECU changed the order of the carer and agency in the layout and add
	//                a listener to the carer spinner so that the displayed agency will
	//                correspond to that stored in the carer object. However the agency
	//                can be changed if, for example, a carer is working for more than
	//                one agency or is acting privately. Only do this in create mode.
	// 20/03/2017 ECU changed "" to StaticData.BLANK_STRING
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//================================================================================
	//final static String TAG = "CarePlanVisitActivity";
	/* =============================================================================== */
	final static int	MODE_EDIT		=	0;
	final static int	MODE_CREATE		=	1;
	/* =============================================================================== */
	Spinner 		agencySpinner;				// 08/01/2014 ECU added - when reminders will start
	RelativeLayout	carePlanLayout;				// 13/01/2014 ECU added
	ScrollView		carePlanVisitScrollView;	// 13/01/2014 ECU added
	Spinner 		carerSpinner;	
	int				dayOfWeek = 0;				// 13/01/2014 ECU day of week (0 = Monday)
	SeekBar 		durationSeekBar;			// 31/01/2016 ECU added
	TextView		durationView;
	boolean			ignoreFirstOnItemSelected;	// 06/02/2015 ECU added
	int				mode = MODE_CREATE;			// 13/01/2014 ECU added
	TimePicker		startTimeView;
	int				type	= StaticData.NO_RESULT;
												// 29/08/2015 ECU added
	int				visitIndex = 0;				// 13/01/2014 ECU added - index to visit
	Button			visitAcceptButton;
	Button			visitCancelButton;
	// ===============================================================================
	static MultiSelectionSpinner 		
					tasksSpinner;				// 13/01/2014 ECU added - tasks to be performed	
	/* =============================================================================== */
	
	/* =============================================================================== */
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
			// ---------------------------------------------------------------------		
			setContentView(R.layout.activity_care_plan_visit);
			// ---------------------------------------------------------------------
			// 13/01/2014 ECU check if a day supplied
			// 29/08/2015 ECU check for other data fed through
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
		
			if (extras !=null) 
			{
				dayOfWeek  = extras.getInt (StaticData.PARAMETER_DAY);  
				// -----------------------------------------------------------------
				// 29/08/2015 ECU try and get the visit index from the intent
				// -----------------------------------------------------------------
				visitIndex  = extras.getInt (StaticData.PARAMETER_SELECTION,StaticData.NO_RESULT);
				// -----------------------------------------------------------------
				// 29/08/2015 ECU try and get the visit index from the intent
				// -----------------------------------------------------------------
				type  = extras.getInt (StaticData.PARAMETER_TYPE,StaticData.NO_RESULT);
		    
				if (type != StaticData.NO_RESULT)
				{
					dayOfWeek = (Integer) SelectorUtilities.selectorParameter.dataObject;
				}
				// -----------------------------------------------------------------
				// 30/08/2015 ECU decide the starting mode
				// -----------------------------------------------------------------
				if (visitIndex != StaticData.NO_RESULT)
				{
					mode = MODE_EDIT;
				}
				else
					visitIndex = 0;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 06/02/2016 ECU in edit mode then want to ignore the first 'on item select'
			//                which will occur when the record to be edited is displayed
			// ---------------------------------------------------------------------
			ignoreFirstOnItemSelected = true;
			// ---------------------------------------------------------------------
			// 13/01/2014 ECU now set the fields that will obtain the user data
			// ---------------------------------------------------------------------
			carePlanLayout = (RelativeLayout) findViewById (R.id.careplanvisit_relativelayout);
		
			durationView 	= (TextView) findViewById (R.id.input_careplanvisit_duration);		
			startTimeView	= (TimePicker) findViewById (R.id.input_careplanvisit_starttime);
		
			visitAcceptButton 	= ((Button)findViewById (R.id.careplan_visit_accept_button));
			visitCancelButton 	= ((Button)findViewById (R.id.careplan_visit_cancel_button));
		
			startTimeView.setIs24HourView (true);
			
			visitAcceptButton.setOnClickListener (buttonListener);
			visitCancelButton.setOnClickListener (buttonListener);	
			
			agencySpinner 	= (Spinner) findViewById (R.id.input_careplanvisit_agency);
			carerSpinner 	= (Spinner) findViewById (R.id.input_careplanvisit_carer);
			// ---------------------------------------------------------------------
			// 13/01/2014 ECU add spinner for selecting tasks
			// ---------------------------------------------------------------------
			tasksSpinner 	= (MultiSelectionSpinner) findViewById (R.id.input_careplanvisit_tasks);
			// ---------------------------------------------------------------------
			// 12/01/2014 ECU declare and set the spinner's adapter
			// ---------------------------------------------------------------------
			ArrayAdapter<String> agencyAdapter 
				= new ArrayAdapter<String>(this, R.layout.spinner_row, R.id.spinner_textview,GetAgencyNames());
								
			agencySpinner.setAdapter (agencyAdapter);
			// ---------------------------------------------------------------------		
			// 08/01/2014 ECU set for carers
			// ---------------------------------------------------------------------		
			ArrayAdapter<String> carerAdapter = new ArrayAdapter<String>(
								this, R.layout.spinner_row, R.id.spinner_textview,GetCarerNames());
		
			carerSpinner.setAdapter (carerAdapter);
			// ---------------------------------------------------------------------
			// 06/02/2016 ECU set up the selection listener
			// ---------------------------------------------------------------------
			carerSpinner.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener() 
			{
				// -----------------------------------------------------------------
				@Override
				public void onItemSelected (AdapterView<?> theParent, View theView, int thePosition, long theID) 
				{
					// --------------------------------------------------------------
					// 06/02/2016 ECU set the agency spinner to correspond to the
					//                value in the carer object - but it can be
					//                changed
					//            ECU if in edit mode then want to ignore the 'first
					//                selection' because it may not be the one that
					//                is stored in the carer record
					// --------------------------------------------------------------
					if (mode == MODE_CREATE || !ignoreFirstOnItemSelected)
						agencySpinner.setSelection (PublicData.carers.get(thePosition).agencyIndex);
					else
						ignoreFirstOnItemSelected = false;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				@Override
				public void onNothingSelected (AdapterView<?> theParent) 
				{
				}	
				// -----------------------------------------------------------------
			});
			// ---------------------------------------------------------------------
			// 13/01/2014 ECU set for tasks to be performed this visit
			// ---------------------------------------------------------------------
			// 13/01/2014 before setting up the strings then set the preferred name
			// 06/12/2016 ECU changed to use the PublicData variable rather than a
			//                local 'literal' array
			// ---------------------------------------------------------------------
			tasksSpinner.setItems (PublicData.tasksToDo);
			// ---------------------------------------------------------------------
			// 10/01/2014 ECU set listener for time picker 
			// ---------------------------------------------------------------------
			carePlanVisitScrollView = (ScrollView) findViewById (R.id.careplanvisit_scrollview);
			// ---------------------------------------------------------------------
			// 31/01/2016 ECU set up the duration seekbar
			// ---------------------------------------------------------------------
			durationSeekBar = (SeekBar) findViewById (R.id.careplan_visit_seekbar);
			durationSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() 
			{
				// -----------------------------------------------------------------
	            @Override
	            public void onStopTrackingTouch(SeekBar seekBar) 
	            {

	            }
	            // -----------------------------------------------------------------
	            @Override
	            public void onStartTrackingTouch(SeekBar seekBar) {

	            }
	            // -----------------------------------------------------------------
	            @Override
	            public void onProgressChanged (SeekBar seekBar,int progress,boolean fromUser) 
	            {
	            	durationView.setText(Integer.toString (progress));
	            }
	            // -----------------------------------------------------------------
	        });
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU try and pick up touch events
			// ---------------------------------------------------------------------
			// 13/01/2014 ECU if there are existing visits then allow to view and edit
			//                - if not then go straight into CREATE mode
			// ---------------------------------------------------------------------
			if (mode == MODE_EDIT)
			{
				// -----------------------------------------------------------------
				// 29/08/2015 ECU changed from '0' to 'visitIndex'
				// -----------------------------------------------------------------
				DisplayACarePlanVisit (PublicData.carePlan.visits[dayOfWeek].get (visitIndex));
				// -----------------------------------------------------------------
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
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// -------------------------------------------------------------------------
		// Inflate the menu; this adds items to the action bar if it is present.
		// -------------------------------------------------------------------------
		getMenuInflater().inflate (R.menu.care_plan_visit, menu);
		return true;
	}
	/* ============================================================================= */
	private View.OnClickListener buttonListener = new View.OnClickListener() 
	{
		
		@Override
		public void onClick(View view) 
		{	
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU now process depending on which button pressed
			// ---------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.careplan_visit_cancel_button: 
				{
					Intent resultIntent = new Intent();
			    	setResult (RESULT_CANCELED, resultIntent);
					finish ();
					break;
				}
				// ------------------------------------------------------------------
				case R.id.careplan_visit_accept_button: 
				{
					// --------------------------------------------------------------
					// 06/01/2014 ECU accept button pressed so handle
					//                visit from entered details
					// --------------------------------------------------------------
					// 12/01/2014 ECU prepare to return the results
					// --------------------------------------------------------------
					// 04/12/2013 ECU call clearFocus to ensure that if time entered from keyboard
					//                then it is picked up correctly
					// -------------------------------------------------------------
					startTimeView.clearFocus ();
					// -------------------------------------------------------------
					// 31/01/2016 ECU generate a local object from the information on
					//                the screen
					// -------------------------------------------------------------
					String localDuration = durationView.getText().toString();
					CarePlanVisit localVisit 
						= new CarePlanVisit (Utilities.ConvertTime (startTimeView.getCurrentHour(),startTimeView.getCurrentMinute()),
											(localDuration.equals (StaticData.BLANK_STRING) ? 0 : Integer.parseInt (localDuration)),
											 agencySpinner.getSelectedItemPosition (),
											 carerSpinner.getSelectedItemPosition (),
											 tasksSpinner.getSelection ());
					// -------------------------------------------------------------
					// 31/01/2016 ECU Note - check whether creating or editing a
					//                       record
					// -------------------------------------------------------------
					if (mode == MODE_CREATE)
					{
						// ---------------------------------------------------------
						// 29/08/2015 ECU create the visit directly
						// 31/01/2016 ECU check for a duplicate visit
						// ---------------------------------------------------------
						if (CheckForDuplicateVisit (dayOfWeek,localVisit,StaticData.NO_RESULT))
						{
							// -----------------------------------------------------
							// 31/01/2016 ECU the visit seems to be a duplicate
							// -----------------------------------------------------
							Utilities.popToastAndSpeak (getString (R.string.visit_clash),true);
							// -----------------------------------------------------
						}
						else
						{
							// -----------------------------------------------------
							// 31/01/2016 ECU this visit is not a duplicate so can
							//                add it
							// -----------------------------------------------------
							PublicData.carePlan.visits[dayOfWeek].add (localVisit);
							// -----------------------------------------------------
							// 31/01/2016 ECU exit this activity
							// -----------------------------------------------------
							finish ();
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------	
					}
					else
					{
						// ---------------------------------------------------------
						// 13/01/2014 ECU in edit mode so just update the record 
						//                being pointed at
						// 31/01/2016 ECU check if the record has been edited so that
						//                it now clashes with other records - the
						//                final argument prevents the software detecting
						//                the object being edited which would cause a
						//                clash
						// ---------------------------------------------------------
						if (CheckForDuplicateVisit (dayOfWeek,localVisit,visitIndex))
						{
							// -----------------------------------------------------
							// 31/01/2016 ECU the visit seems to be a duplicate
							// -----------------------------------------------------
							Utilities.popToastAndSpeak (getString (R.string.visit_clash),true);
							// -----------------------------------------------------
						}
						else
						{
							PublicData.carePlan.visits[dayOfWeek].set (visitIndex,localVisit);
							// -----------------------------------------------------
							Intent resultIntent = new Intent ();
							setResult (RESULT_CANCELED, resultIntent);
							// -----------------------------------------------------
							// 31/01/2016 ECU exit this activity
							// -----------------------------------------------------
							finish ();
							// -----------------------------------------------------
						}
					}
				    
					break;
				}
			}
		}
	};
	// =============================================================================
	@Override
    public void onDestroy()
    {
		// -------------------------------------------------------------------------
		// 29/08/2015 ECU added the method here
		// -------------------------------------------------------------------------
		CarePlanActivity.WriteCarePlanToDisk (this);
		// -------------------------------------------------------------------------
		// 27/05/2013 ECU get the main method processed
		// -------------------------------------------------------------------------
        super.onDestroy();
        // -------------------------------------------------------------------------
    }
	// =============================================================================
	public boolean onOptionsItemSelected (MenuItem item)
	{
		// -------------------------------------------------------------------------
		// 06/12/2016 ECU created to handle menu options
		// -------------------------------------------------------------------------
		// 06/12/2016 ECU switch on menu item selected
		// -------------------------------------------------------------------------
		switch (item.getItemId())
		{
			// =====================================================================
			case R.id.tasks_to_do:
				// -----------------------------------------------------------------
				// 03/04/2016 ECU set the list of tasks to do
				// -----------------------------------------------------------------
				DialogueUtilities.multilineTextInput (this,
						  							  getString (R.string.tasks_to_do_message),
						  							  getString (R.string.enter_tasks_to_do_message),
						  							  30,
						  							  TasksToDoAsString (),
						  							  Utilities.createAMethod (CarePlanVisitActivity.class,"TasksToDoMethod",StaticData.BLANK_STRING),
						  							  null);
				return true;
				// -----------------------------------------------------------------
		}
		return true;
	}
	/* ============================================================================= */
	boolean CheckForDuplicateVisit (int theDayOfWeek,CarePlanVisit thePlanVisit,int theEditIndex)
	{
		// -------------------------------------------------------------------------
		// 31/01/2016 ECU created to check whether the defined visit is a duplicate
		// -------------------------------------------------------------------------
		if (PublicData.carePlan.visits [theDayOfWeek].size() > 0)
		{
			// ---------------------------------------------------------------------
			// 31/01/2016 ECU loop through all of the defined visits
			// ---------------------------------------------------------------------
			for (int theVisit = 0; theVisit < PublicData.carePlan.visits [theDayOfWeek].size(); theVisit++)
			{
				CarePlanVisit localVisit = PublicData.carePlan.visits [theDayOfWeek].get (theVisit);
				// -----------------------------------------------------------------
				// 31/01/2016 ECU check if the carer is the same
				//            ECU check the edit index because do not want to check
				//                against the record being edited because this would
				//                obviously clash. When being checked for a new
				//                entry then theEditIndex will be StaticData.NO_RESULT
				//                which will never match
				// -----------------------------------------------------------------
				if (thePlanVisit.carerIndex == localVisit.carerIndex &&
					theVisit != theEditIndex)
				{
					// -------------------------------------------------------------
					// 31/01/2016 ECU now check the start time
					// -------------------------------------------------------------
					if (thePlanVisit.startTime >= localVisit.startTime &&
						thePlanVisit.startTime <  localVisit.EndTime())
					{
						// ---------------------------------------------------------
						// 31/01/2016 ECU appear to have a duplicate
						// ---------------------------------------------------------
						return true;
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 31/01/2016 ECU now check the end time
					// -------------------------------------------------------------
					if (thePlanVisit.EndTime() >= localVisit.startTime &&
						thePlanVisit.EndTime() <  localVisit.EndTime())
					{
						// ---------------------------------------------------------
						// 31/01/2016 ECU appear to have a duplicate
						// ---------------------------------------------------------
						return true;
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 31/01/2016 ECU check for a complete overlap
					// -------------------------------------------------------------
					if (thePlanVisit.startTime < localVisit.startTime &&
						thePlanVisit.EndTime() > localVisit.EndTime())
					{
						return true;
					}
					// -------------------------------------------------------------
				}
			}
		}
		// -------------------------------------------------------------------------
		// 31/01/2016 ECU indicate that the visit is not a duplicate
		// -------------------------------------------------------------------------
		return false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void DisplayACarePlanVisit (CarePlanVisit theCarePlanVisit)
	{
		// -------------------------------------------------------------------------
		// 13/01/2014 ECU display the supplied data in the current layout
		// -------------------------------------------------------------------------
		durationView.setText (Integer.toString(theCarePlanVisit.duration));		
		// -------------------------------------------------------------------------
		// 31/01/2016 ECU set the initial position of the seekbar
		// -------------------------------------------------------------------------
		durationSeekBar.setProgress (theCarePlanVisit.duration);
		// -------------------------------------------------------------------------
		Calendar calendar = Calendar.getInstance ();
		calendar.setTimeInMillis (theCarePlanVisit.startTime);
		
		startTimeView.setCurrentHour(calendar.get (Calendar.HOUR_OF_DAY));
		startTimeView.setCurrentMinute(calendar.get (Calendar.MINUTE));

		// -------------------------------------------------------------------------
		// 06/02/2016 ECU swap around so that care is set before agency because
		//                the carer spinner has an 'on select' event to set
		//                the agency which may differ from the one that the user
		//                has set. NOTE - this didn't work so leave it to ignore
		//                the first time 'on select' is run when in edit mode
		// -------------------------------------------------------------------------
		carerSpinner.setSelection (theCarePlanVisit.carerIndex);
		agencySpinner.setSelection (theCarePlanVisit.agencyIndex);
		// -------------------------------------------------------------------------
		// 13/01/2014 ECU add spinner for selecting tasks
		// -------------------------------------------------------------------------
		tasksSpinner.setSelection (theCarePlanVisit.tasks);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static String [] GetAgencyNames ()
	{
		String [] localNames = new String [PublicData.agencies.size()];
		
		for (int theIndex = 0; theIndex < PublicData.agencies.size (); theIndex++)
		{
			localNames [theIndex] = PublicData.agencies.get(theIndex).name;
		}
			
		return localNames;
	}
	/* ============================================================================= */
	String [] GetCarerNames ()
	{
		String [] localNames = new String [PublicData.carers.size()];
					
		for (int theIndex = 0; theIndex < PublicData.carers.size (); theIndex++)
		{
			localNames [theIndex] = PublicData.carers.get(theIndex).name;
		}
			
		return localNames;
	}
	/* ============================================================================= */
	public static String [] TasksToDo (String thePatientName,String [] theRawData)
	{
		// -------------------------------------------------------------------------
		// 06/12/2016 ECU changed to use the resources values
		// -------------------------------------------------------------------------
		if (theRawData == null)
		{
			PublicData.tasksToDoRaw = MainActivity.activity.getResources ().getStringArray (R.array.care_visit_tasks);
			// ----------------------------------------------------------------------
			// 06/12/2016 ECU copy across the default array
			// ----------------------------------------------------------------------
			theRawData = Arrays.copyOf (PublicData.tasksToDoRaw,PublicData.tasksToDoRaw.length);
			// ----------------------------------------------------------------------
		}
		// --------------------------------------------------------------------------
		String [] localTasksToDo = new String [theRawData.length];
		// -------------------------------------------------------------------------
		// 05/10/2016 ECU change the patient name in the array
		// --------------------------------------------------------------------------
		for (int theTask = 0; theTask < theRawData.length; theTask++)
		{
			localTasksToDo [theTask] = theRawData [theTask].replaceFirst ("PATIENT", thePatientName);
		}
		// -------------------------------------------------------------------------
		// 05/10/2016 ECU return the modified array
		// -------------------------------------------------------------------------
		return localTasksToDo;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	String TasksToDoAsString ()
	{
		// -------------------------------------------------------------------------
		// 06/12/2016 ECU created to return a string generated from the tasks to do
		//                which is stored as a string array
		// -------------------------------------------------------------------------
		String localString = StaticData.BLANK_STRING;
		for (int index = 0; index < PublicData.tasksToDo.length; index++)
		{
			localString += PublicData.tasksToDoRaw [index] + StaticData.NEWLINE;
		}
		// -------------------------------------------------------------------------
		// 06/12/2016 ECU return the generated string
		// -------------------------------------------------------------------------
		return localString;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void TasksToDoMethod (String theTasks)
	{
		// -------------------------------------------------------------------------
		// 06/12/2016 ECU created to input the email subject
		// -------------------------------------------------------------------------
		// 06/12/2016 ECU split the string into lines - need to eliminate blank lines
		// -------------------------------------------------------------------------
		String [] inputLines = theTasks.split (StaticData.NEWLINE);
		// -------------------------------------------------------------------------
		// 07/12/2016 ECU changed from using a StringBuilder
		// -------------------------------------------------------------------------
		String localString = StaticData.BLANK_STRING;  
		for (int index = 0; index < inputLines.length; index++)
		{
			// ---------------------------------------------------------------------
			// 06/12/2016 ECU only store 'non empty' lines
			// 07/12/2016 ECU added 'trim' to remove leading and trailing white
			//                space
			// ---------------------------------------------------------------------
			if (Utilities.emptyString (inputLines [index]))
			{
				localString += (inputLines [index]).trim() + StaticData.NEWLINE;
			}
		}
		// -------------------------------------------------------------------------
		// 06/12/2016 ECU now generate the relevant raw data string array
		// 07/12/2016 ECU changed with the removal of StringBuilder
		// -------------------------------------------------------------------------
		PublicData.tasksToDoRaw = localString.split (StaticData.NEWLINE);
		// -------------------------------------------------------------------------
		// 06/12/2016 ECU write the object to disk
		// -------------------------------------------------------------------------
		AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + MainActivity.activity.getString (R.string.care_visit_tasks_file),PublicData.tasksToDoRaw);
		// -------------------------------------------------------------------------
		// 06/12/2016 ECU now regenerate the 'task to do' array that is used
		// -------------------------------------------------------------------------
		PublicData.tasksToDo = TasksToDo (PublicData.patientDetails.preferredName,PublicData.tasksToDoRaw);
		// -------------------------------------------------------------------------
		// 25/03/2017 ECU make sure all existing visits are modified to the new
		//                taks that have been entered/deleted
		// -------------------------------------------------------------------------
		CarePlanVisit.tasksAdjustmentAll ();
		// -------------------------------------------------------------------------
		// 07/12/2016 ECU remind the user to check existing visits
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak (MainActivity.activity.getString (R.string.visit_task_reminder),true);
		// -------------------------------------------------------------------------
		// 07/12/2016 ECU try and refresh the tasks spinner to reflect the changes
		// -------------------------------------------------------------------------
		tasksSpinner.setItems (PublicData.tasksToDo);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

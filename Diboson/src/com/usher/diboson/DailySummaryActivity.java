package com.usher.diboson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DailySummaryActivity extends DibosonActivity
{
	// =============================================================================
	// 27/02/2017 ECU created to provide the user with a summary of tasks that are
	//                to be performed on particular days of the week
	// =============================================================================
	
	// =============================================================================
	static  Activity			activity;
	static  TextView 			appointmentsTextView;
	static  TextView 			carerVisitsTextView; 
	static 	Context				context;
	static  long				dateStart;
	static  long []				dates = new long [StaticData.DAYS_PER_WEEK];
	static  SimpleDateFormat	dayFormatter;
	static  SimpleDateFormat	dateFormatter;
	static  TextView			medicationTextView; 
	static 	TextView 			timerAlarmsTextView;
	// -----------------------------------------------------------------------------
			boolean				dateSelect	= false;
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
			// 27/02/2017 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 27/02/2017 ECU call method to set the screen up
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_daily_summary);
			// ---------------------------------------------------------------------
			// 27/02/2017 ECU save the context for future use
			// 01/03/2017 ECU save the activity
			// ---------------------------------------------------------------------
			activity	= this;
			context 	= this;
			// ---------------------------------------------------------------------
			// 27/02/2017 ECU store the required format for the date
			// ---------------------------------------------------------------------
			dateFormatter 		= new SimpleDateFormat ("dd MMM yyyy",Locale.getDefault());
			dayFormatter 		= new SimpleDateFormat ("EEEE",Locale.getDefault());
			// ---------------------------------------------------------------------
			// 27/02/2017 ECU check if a parameter has been passed through
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
			if (extras !=null) 
			{
				// -----------------------------------------------------------------
				// 27/02/2017 ECU set up the title
				// -----------------------------------------------------------------
				dateSelect = extras.getBoolean (StaticData.PARAMETER_DAY);
				// -----------------------------------------------------------------			
			}
			// ---------------------------------------------------------------------
			// 27/02/2017 ECU initialise the stored data
			// ---------------------------------------------------------------------
			SelectorUtilities.Initialise ();
			// ---------------------------------------------------------------------
			// 27/02/2017 ECU now decide the action to take
			// ---------------------------------------------------------------------
			if (dateSelect)
			{
				// -----------------------------------------------------------------
				// 27/02/2017 ECU request the date from which the summaries will be 
				//                generated
				// -----------------------------------------------------------------
				DialogueUtilities.getDate(this, 
										  getString (R.string.date_picker_title), 
										  getString (R.string.date_picker_subtitle), 
										  getString (R.string.select),
										  Utilities.createAMethod (DailySummaryActivity.class,"GetDateMethod",
												  								(Object) null));
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 27/02/2017 ECU use 'today' as the start day
				// -----------------------------------------------------------------
				dateStart = Utilities.getAdjustedTime (false);
				// -----------------------------------------------------------------
				// 27/02/2017 ECU start up the initial display
				// -----------------------------------------------------------------
				showTheDays (this);
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 27/02/2017 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================

	// =============================================================================
	public static void BackAction (int theDummyArgument)
	{
		// -------------------------------------------------------------------------
		// 27/02/2017 ECU called from Selector when the 'back' key is presed
		// -------------------------------------------------------------------------
		// 27/02/2017 ECU just finish the activity
		// -------------------------------------------------------------------------
		((Activity)context).finish ();
		//--------------------------------------------------------------------------
	}
	// =============================================================================
	public static ArrayList<ListItem> BuildTheDaysList ()
	{
		// -------------------------------------------------------------------------
		SelectorUtilities.selectorParameter.listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 27/02/2017 ECU build up the list of days
		// -------------------------------------------------------------------------
		for (int theDay = 0; theDay < StaticData.DAYS_PER_WEEK; theDay++)
		{
			ListItem localListItem = new ListItem (
					R.drawable.daily_summary,
					dayFormatter.format (dates [theDay]),
					dateFormatter.format (dates [theDay]),
					"",
					theDay);
			// -------------------------------------------------------------
			// 04/10/2016 ECU Note - add the new record to the list
			// -------------------------------------------------------------
			SelectorUtilities.selectorParameter.listItems.add (localListItem);
			// -------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		return SelectorUtilities.selectorParameter.listItems;
		// -------------------------------------------------------------------------
	} 
	// =============================================================================
	private static View.OnClickListener buttonListener = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{	
			// ---------------------------------------------------------------------
			// 01/03/2017 ECU handle the button
			// ---------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.appointments_button: 
					// -------------------------------------------------------------
					// 01/03/2017 ECU toggle the associated text view
					// -------------------------------------------------------------
					if (appointmentsTextView.getVisibility() == View.GONE)
						appointmentsTextView.setVisibility (View.VISIBLE);
					else
						appointmentsTextView.setVisibility (View.GONE);
					break;
				// -----------------------------------------------------------------
				case R.id.carer_visits_button:
					// -------------------------------------------------------------
					// 01/03/2017 ECU toggle the associated text view
					// -------------------------------------------------------------
					if (carerVisitsTextView.getVisibility() == View.GONE)
						carerVisitsTextView.setVisibility (View.VISIBLE);
					else
						carerVisitsTextView.setVisibility (View.GONE);
					break;
				// -----------------------------------------------------------------
				case R.id.medication_button:
					// -------------------------------------------------------------
					// 01/03/2017 ECU toggle the associated text view
					// -------------------------------------------------------------
					if (medicationTextView.getVisibility() == View.GONE)
						medicationTextView.setVisibility (View.VISIBLE);
					else
						medicationTextView.setVisibility (View.GONE);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.timer_alarms_button:
					// -------------------------------------------------------------
					// 01/03/2017 ECU toggle the associated text view
					// -------------------------------------------------------------
					if (timerAlarmsTextView.getVisibility() == View.GONE)
						timerAlarmsTextView.setVisibility (View.VISIBLE);
					else
						timerAlarmsTextView.setVisibility (View.GONE);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}
		}
	};
	// =============================================================================
	static void generateSummaries (long theDate)
	{
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU created to generate summaries for the specified date
		//
		//                Summaries will be generated for :-
		//
		//						1) medication
		//						2) care plans
		//						3) appointments
		// 						4) timer alarms
		//
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU first generate the medication summary
		// -------------------------------------------------------------------------
		List<MedicationDetails> medication = MedicationActivity.generateSummary (theDate);
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU generate the care plan summary
		// -------------------------------------------------------------------------
		List<CarePlanVisit> visits = CarePlanActivity.generateSummary (theDate);
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU generate the appointments summary
		// -------------------------------------------------------------------------
		List<AppointmentDetails> appointments = AppointmentsActivity.generateSummary (theDate);
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU generate the timer alarms summary
		// -------------------------------------------------------------------------
		List<AlarmData> alarms = TimerActivity.generateSummary (theDate);
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU temporarily just show the returned sizes
		// -------------------------------------------------------------------------
		Utilities.popToast ("Medication : " 	+ medication.size() 	+ StaticData.NEWLINE +
				            "Visits : " 		+ visits.size() 		+ StaticData.NEWLINE +
				            "Appointments : " 	+ appointments.size() 	+ StaticData.NEWLINE +
				            "Alarms : " 		+ alarms.size());
		// -------------------------------------------------------------------------
		// 02/03/2017 ECU check if there is anything to be displayed
		// -------------------------------------------------------------------------
		if ((medication.size () > 0) || (visits.size () > 0) || 
			(appointments.size () > 0) || (alarms.size () > 0))
		{
			// ---------------------------------------------------------------------
			// 01/03/2017 ECU close the 'selector' activity
			// ---------------------------------------------------------------------
			Selector.Finish ();
			// ---------------------------------------------------------------------
			// 01/03/2017 ECU now display the summary
			// ---------------------------------------------------------------------
			activity.setContentView (R.layout.daily_summary);
			// ---------------------------------------------------------------------
			// 01/03/2017 ECU set up the textviews
			// ---------------------------------------------------------------------
			appointmentsTextView 	= (TextView) activity.findViewById (R.id.appointments_textview);
			carerVisitsTextView 	= (TextView) activity.findViewById (R.id.carer_visits_textview);
			medicationTextView 		= (TextView) activity.findViewById (R.id.medication_textview);
			timerAlarmsTextView 	= (TextView) activity.findViewById (R.id.timer_alarms_textview);
			// ---------------------------------------------------------------------
			// 01/03/2017 ECU set up the buttons
			// ---------------------------------------------------------------------
			Button appointmentsButton 		= (Button) activity.findViewById (R.id.appointments_button);
			Button carerVisitsButton 		= (Button) activity.findViewById (R.id.carer_visits_button);
			Button medicationButton 		= (Button) activity.findViewById (R.id.medication_button);
			Button timerAlarmsButton 		= (Button) activity.findViewById (R.id.timer_alarms_button);
			// ---------------------------------------------------------------------
			// 01/03/2017 ECU set up the button listeners
			// ---------------------------------------------------------------------
			appointmentsButton.setOnClickListener (buttonListener);
			carerVisitsButton.setOnClickListener  (buttonListener);
			medicationButton.setOnClickListener   (buttonListener);
			timerAlarmsButton.setOnClickListener  (buttonListener);
			// ---------------------------------------------------------------------
			// 01/03/2017 ECU depending on the returned results then modify the display
			// ---------------------------------------------------------------------
			if (medication.size() > 0)
			{
				medicationTextView.setText(MedicationDetails.printSelected(medication, Utilities.DayOfWeek (theDate)));
			}
			else
			{
				medicationButton.setVisibility  (View.GONE);
				medicationTextView.setVisibility (View.GONE);
			}
			// ---------------------------------------------------------------------
			if (visits.size() > 0)
			{
				carerVisitsTextView.setText (CarePlanVisit.printSelected (visits));
			}
			else
			{
				carerVisitsButton.setVisibility  (View.GONE);
				carerVisitsTextView.setVisibility (View.GONE);
			}
			// ---------------------------------------------------------------------
			if (alarms.size() > 0)
			{
				timerAlarmsTextView.setText(AlarmData.printSelected (context,alarms));
			}
			else
			{
				timerAlarmsButton.setVisibility  (View.GONE);
				timerAlarmsTextView.setVisibility (View.GONE);
			}
			// ---------------------------------------------------------------------
			if (appointments.size() > 0)
			{
				appointmentsTextView.setText(AppointmentDetails.printSelected(context, appointments));
			}
			else
			{
				appointmentsButton.setVisibility  (View.GONE);
				appointmentsTextView.setVisibility (View.GONE);
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/03/2017 ECU there is nothing happening on this day
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (context.getString (R.string.daily_summary_nothing_scheduled));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void GetDateMethod (Object theObject)
	{
		// -------------------------------------------------------------------------
		// 27/02/2017 ECU created to handle the 'selected' date
		// -------------------------------------------------------------------------
		int [] date = (int []) theObject;
		// -------------------------------------------------------------------------
		// 27/02/2017 ECU now get the required date in milliseconds
		// -------------------------------------------------------------------------
		dateStart = Utilities.ConvertDate (date[0], date[1], date[2]);
		// -------------------------------------------------------------------------
		// 27/02/2017 ECU now handle the display
		// -------------------------------------------------------------------------
		showTheDays (context);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void setDates ()
	{
		// -------------------------------------------------------------------------
		// 27/02/2017 ECU set up the array of working days
		// -------------------------------------------------------------------------
		for (int theDay = 0; theDay < StaticData.DAYS_PER_WEEK; theDay++)
		{
			dates [theDay] = dateStart + (StaticData.MILLISECONDS_PER_DAY * theDay);
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void showTheDays (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 27/02/2017 ECU build the array of dates to be used
		// -------------------------------------------------------------------------
		setDates ();
		// -------------------------------------------------------------------------
		// 27/02/2017 ECU created to set up the initial display of days
		// -------------------------------------------------------------------------
		BuildTheDaysList ();
		// -------------------------------------------------------------------------
		// 27/02/2017 ECU set the variables which dictate how the display responds
		//                to user input
		// -------------------------------------------------------------------------
		SelectorUtilities.selectorParameter.backMethodDefinition 	
				= new MethodDefinition<DailySummaryActivity> (DailySummaryActivity.class,"BackAction");
		SelectorUtilities.selectorParameter.finishOnSelect			= false;
		SelectorUtilities.selectorParameter.rowLayout 				= R.layout.daily_summary_row;
		SelectorUtilities.selectorParameter.sort 					= false;
		// -------------------------------------------------------------------------
		// 27/02/2017 ECU now display the days
		// 02/03/2017 ECU changed to use _DAILY_....
		// -------------------------------------------------------------------------
		SelectorUtilities.StartSelector (theContext,
				new MethodDefinition<DailySummaryActivity> (DailySummaryActivity.class,"SelectDay"),
				StaticData.OBJECT_DAILY_SUMMARIES);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
    // ============================================================================= 
    public static void SelectDay (int theDay)
    {
    	// -------------------------------------------------------------------------
    	// 27/02/2017 ECU created to handle the selection of a day
    	// 28/02/2017 ECU call the method that will generate the summaries
    	// -------------------------------------------------------------------------
    	generateSummaries (dates [theDay]);
    	// -------------------------------------------------------------------------   
    }
    // =============================================================================
}

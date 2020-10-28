package com.usher.diboson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;


public class AppointmentsActivity extends DibosonActivity
{
	/* =============================================================================== */
	// ===============================================================================
	// 06/01/2014 ECU created
	// 07/01/2014 ECU added gesture handling for scroll view
	// 08/01/2014 ECU major changes to use local string arrays rather
	//                than string arrays stored in the resources
	// 21/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 19/12/2015 ECU general tidy up
	//            ECU changed to use the Selector mechanism for item selection
	// 30/03/2016 ECU use hashcode's to determine whether the data has changed and should
	//                be written to disk. Am aware that this is not perfect but good 
	//                first attempt.
	// 31/10/2016 ECU IMPORTANT - this is an early activity which is a bit of a mess
	//                =========   but it is working so leave until later when a rewrite
	//                            would seem appropriate
	//            ECU all MODE_ changed to StaticData.APPOINTMENT_MODE_
	// 20/03/2017 ECU changed from "" to BLANK....
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//================================================================================
	/* =============================================================================== */
	final static String TAG = "AppointmentsActivity";
	/* =============================================================================== */
	
	/* =============================================================================== */
	// 08/01/2014 ECU keep these arrays together and adjust accordingly if any changes
	//                are made
	// 31/10/2016 ECU IMPORTANT the following array must tie in with the
	// 				  ========= appointment_type_values array stored in the resources
	// -------------------------------------------------------------------------------
	final static int [] APPOINTMENT_IMAGES = {
													R.drawable.hospital,
													R.drawable.doctor,
													R.drawable.foot_clinic,
													R.drawable.hairdresser,
													R.drawable.dentist,
													R.drawable.optician,
													R.drawable.appointments
	   										};
	//---------------------------------------------------------------------------------
	
	/* =============================================================================== */
	
	// ===============================================================================
	TextView		appointmentAddress;
	Button			appointmentAcceptButton;
	Button			appointmentCancelButton;
	TextView		appointmentContactName;
	DatePicker		appointmentDate;
	Button			appointmentDeleteButton;	// 07/01/2014 ECU added	
	Button			appointmentEditButton;		// 07/01/2014 ECU added
	Button			appointmentFinishedButton;	// 11/03/2014 ECU added
	TextView		appointmentName;
	Button			appointmentNewButton;		// 07/01/2014 ECU added
	TextView		appointmentNotes;
	TextView		appointmentPhoneNumber;
	ScrollView		appointmentScrollView;
	TimePicker		appointmentTime;
	Spinner 		appointmentType;
	static int		initialHashCodeData;		// 30/03/2016 ECU added
												// 07/10/2016 ECU changed to static
	static int		initialHashCodeTypes;		// 30/03/2016 ECU added
												// 07/10/2016 ECU changed to static
	TimePicker		reminderPreferredTime;		// 08/01/2014 ECU added - preferred time for
	//											//                first reminder
	Spinner 		reminderTime;				// 08/01/2014 ECU added - when reminders will start
	Spinner 		reminderRepeatTime;			// 08/01/2014 ECU added - when reminders will repeat
	long			reminderNextGap;			// 08/01/2014 ECU added
	long			reminderNextTime;			// 08/01/2014 ECU added
	ScrollView		scrollView;
	String			selectType = StaticData.BLANK_STRING;			
												// 07/01/2014 ECU added
												// 20/03/2017 ECU changed to use BLANK...
	// =============================================================================
	static int				appointmentIndex = 0;	// 07/01/2014 ECU added - record index
	static RelativeLayout	appointmentLayout;		// 07/01/2014 ECU added
	static boolean			appointmentsDisplay;	// 29/11/2016 ECU added
	static Context			context;				// 19/12/2015 ECU added
	static TextView			details;				// 29/11/2016 ECU added
	static HandleProgressBar 
							handleProgressBar;		// 31/10/2016 ECU moved here
	static int				mode = StaticData.APPOINTMENT_MODE_VIEW;		
													// 07/01/2014 ECU added
	static RelativeLayout	seekbarLayout;			// 29/11/2016 ECU added
	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 21/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 16/02/2014 ECU call up routine to set common activity features
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this);
			// ---------------------------------------------------------------------
			// 19/12/2015 ECU set up the context for later use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 29/11/2016 ECU initialise any static variables
			// ---------------------------------------------------------------------
			appointmentsDisplay = false;
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU check if any incoming parameters
			// ---------------------------------------------------------------------
			Bundle extras = getIntent ().getExtras ();
	
			if (extras != null) 
			{	
				// -----------------------------------------------------------------
				// 29/11/2016 ECU check for parameter that indicates if appointments
				//                are only to be displayed
				// -----------------------------------------------------------------
				appointmentsDisplay = extras.getBoolean(StaticData.PARAMETER_APPOINTMENTS);
				selectType 			= extras.getString (StaticData.PARAMETER_SELECT);
			}  
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_appointments);
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU make sure that the soft keyboard does not pop up
			// ---------------------------------------------------------------------
			getWindow().setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU get the id of the associated LinearLayout
			// ---------------------------------------------------------------------
			appointmentLayout = (RelativeLayout) findViewById (R.id.appointment_relativelayout);
			// ---------------------------------------------------------------------
			// 06/01/2014 ECU set up the various views
			// ---------------------------------------------------------------------
			appointmentAddress  	= (TextView)   findViewById (R.id.input_appointment_address);
			appointmentContactName  = (TextView)   findViewById (R.id.input_appointment_contactname);
			appointmentDate			= (DatePicker) findViewById (R.id.input_appointment_date);
			appointmentName 		= (TextView)   findViewById (R.id.input_appointment_name);
			appointmentNotes 		= (TextView)   findViewById (R.id.input_appointment_notes);
			appointmentPhoneNumber 	= (TextView)   findViewById (R.id.input_appointment_phone_number);
			appointmentTime			= (TimePicker) findViewById (R.id.input_appointment_time);
			// ---------------------------------------------------------------------
			// 08/01/2014 ECU added the reminder preferred time
			// ---------------------------------------------------------------------
			reminderPreferredTime	= (TimePicker) findViewById (R.id.input_appointment_preferred_time);
			// ---------------------------------------------------------------------
			// 06/01/2014 ECU set up the button listeners
			// ---------------------------------------------------------------------
			appointmentAcceptButton 	= ((Button) findViewById (R.id.appointment_accept_button));
			appointmentCancelButton 	= ((Button) findViewById (R.id.appointment_cancel_button));
			appointmentDeleteButton 	= ((Button) findViewById (R.id.appointment_delete_button));
			appointmentEditButton 		= ((Button) findViewById (R.id.appointment_edit_button));
			appointmentFinishedButton	= ((Button) findViewById (R.id.appointment_finished_button));
			appointmentNewButton 		= ((Button) findViewById (R.id.appointment_new_button));
			// ---------------------------------------------------------------------
			appointmentAcceptButton.setOnClickListener   (buttonListener);
			appointmentCancelButton.setOnClickListener   (buttonListener);
			appointmentDeleteButton.setOnClickListener   (buttonListener);
			appointmentEditButton.setOnClickListener     (buttonListener);
			appointmentFinishedButton.setOnClickListener (buttonListener);	// 11/03/2014 ECU added
			appointmentNewButton.setOnClickListener      (buttonListener);
			// ---------------------------------------------------------------------
			// 24/10/2016 ECU the notes field is scrollable within a scroll view
			//                so try and get this to work properly
			// ---------------------------------------------------------------------
			appointmentNotes.setMovementMethod (new ScrollingMovementMethod ());
			
			scrollView = (ScrollView) findViewById (R.id.appointments_scrollview);
			// ---------------------------------------------------------------------
			// 24/10/2016 ECU when in the scroll view then want it to be scrolled
			//                as normal
			// ---------------------------------------------------------------------
			scrollView.setOnTouchListener (new OnTouchListener()
			{ 
				@Override
				public boolean onTouch (View theView, MotionEvent theEvent) 
				{
					theView.performClick ();
					appointmentNotes.getParent ().requestDisallowInterceptTouchEvent (false);
					return false;
				}
			});
			// ---------------------------------------------------------------------
			// 24/10/2016 ECU when in the notes field then want to prevent scrolling
			//                of the scroll view
			// ---------------------------------------------------------------------
			appointmentNotes.setOnTouchListener (new OnTouchListener() 
			{
				@Override
				public boolean onTouch (View theView, MotionEvent theEvent) 
				{
					theView.performClick ();
					appointmentNotes.getParent ().requestDisallowInterceptTouchEvent (true);
					return false;
				}
			});
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU set time pickers to 24 hour format
			// ---------------------------------------------------------------------
			appointmentTime.setIs24HourView (true);
			reminderPreferredTime.setIs24HourView (true);
			// ---------------------------------------------------------------------
			// 06/01/2014 ECU set up spinner information
			// 08/01/2014 ECU added reminderTime and reminderRepeatTime
			// ---------------------------------------------------------------------
			appointmentType 	= (Spinner) findViewById (R.id.input_appointment_type);
			reminderTime   		= (Spinner) findViewById (R.id.input_appointment_remindertime);
			reminderRepeatTime  = (Spinner) findViewById (R.id.input_appointment_repeat_time);
			// ---------------------------------------------------------------------
			// 06/01/2014 ECU declare and set the spinner's adapter
			// 31/10/2016 ECU changed to use the resource array
			// ---------------------------------------------------------------------
			ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, 
																	 R.layout.spinner_row, 
																	 R.id.spinner_textview,
																	 getResources().getStringArray (R.array.appointment_type_values));
					
			appointmentType.setAdapter (adapter);
			// ---------------------------------------------------------------------
			// 08/01/2014 ECU set for reminder time
			// 31/10/2016 ECU changed to use the resource array
			// ---------------------------------------------------------------------
			ArrayAdapter<String> reminderAdapter = new ArrayAdapter<String> (this,
																			 R.layout.spinner_row,
																			 R.id.spinner_textview,
																			 getResources().getStringArray (R.array.appointment_reminder_start_values));
					
			reminderTime.setAdapter (reminderAdapter);
			// ---------------------------------------------------------------------
			// 08/01/2014 ECU set for reminder repeat time
			// 31/10/2016 ECU changed to use the resource array
			// ---------------------------------------------------------------------	
			ArrayAdapter<String> reminderRepeatAdapter = new ArrayAdapter<String> (this,
																				   R.layout.spinner_row, 
																				   R.id.spinner_textview,
																				   getResources().getStringArray(R.array.appointment_reminder_gap_values));
							
			reminderRepeatTime.setAdapter(reminderRepeatAdapter);
			// ---------------------------------------------------------------------
			// 06/01/2014 ECU set up the listener when an item in the list is selected
			// ---------------------------------------------------------------------
			appointmentType.setOnItemSelectedListener (new localOnItemSelectedListener());
			// ---------------------------------------------------------------------
			// 06/01/2014 ECU make sure the first item in the list is selected
			// ---------------------------------------------------------------------
			appointmentType.setSelection(0);
			appointmentScrollView = (ScrollView) findViewById (R.id.appointments_scrollview);
			// ---------------------------------------------------------------------
			// 30/03/2016 ECU get the initial hash codes
			// ---------------------------------------------------------------------
			initialHashCodeData 	= PublicData.appointments.hashCode ();
			initialHashCodeTypes 	= PublicData.appointmentTypes.hashCode ();
			// ---------------------------------------------------------------------
			if ((PublicData.appointments != null) && (PublicData.appointments.size() > 0))
			{
				// -----------------------------------------------------------------
				// 14/02/2014 ECU display existing appointments
				// 21/11/2015 ECU changed to use selector class
				// -----------------------------------------------------------------
				selectAppointment ();
				// -----------------------------------------------------------------
			}
			else
			{

				// -----------------------------------------------------------------
				// 07/01/2014 ECU default to displaying the first appointment in protected mode
				//            ECU if there are no appointments then start in create mode
				// -----------------------------------------------------------------
				if (PublicData.appointments.size () == 0)
				{
					// -------------------------------------------------------------
					// 31/10/2016 ECU Note - want to create an initial appointment
					// -------------------------------------------------------------
					SetAllFields (appointmentLayout,false);
				
					mode = StaticData.APPOINTMENT_MODE_CREATE;
				}
				else
				{
					SetAllFields (appointmentLayout,true);
					DisplayAppointment (PublicData.appointments.get (appointmentIndex));
				}
			}
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
				case R.id.appointment_accept_button: 
				{
					// -------------------------------------------------------------
					// 06/01/2014 ECU accept button pressed so create
					//                appointment from entered details
					// 31/10/2016 ECU add the context
					// -------------------------------------------------------------
					CreateAppointment (getBaseContext ());	
					// -------------------------------------------------------------
					break;
				}
				// -----------------------------------------------------------------
				case R.id.appointment_cancel_button:
				{
					// -------------------------------------------------------------
					// 06/01/2014 ECU cancel button pressed
					// -------------------------------------------------------------
					finish ();
					break;
				}
				// -----------------------------------------------------------------
				case R.id.appointment_delete_button:
				{
					// -------------------------------------------------------------
					// 07/01/2014 ECU delete button pressed
					// 09/03/2014 ECU added the last 'true' argument so that the 
					//                message is sent in the background and will
					//                switch on the wifi network if not enabled
					// 20/12/2015 ECU changed to use deletion method
					// -------------------------------------------------------------
					DeleteAppointment (getBaseContext(),appointmentIndex);
					// -------------------------------------------------------------  
					// 07/01/2014 ECU check if all of the appointments have been deleted
					// -------------------------------------------------------------
					if (PublicData.appointments.size() == 0)
					{
						Utilities.popToast (getString(R.string.all_appointments_deleted));
						
						finish ();
					}
					else
					{
						// ---------------------------------------------------------
						// 10/01/2014 ECU delay the record at this index
						// 26/11/2014 ECU problem occurs if have 2 records and delete
						//                the 2nd record. In general the problem will
						//                occur if deleting the last record
						// ---------------------------------------------------------
						if (appointmentIndex <= PublicData.appointments.size())
							appointmentIndex = PublicData.appointments.size() - 1;
						// ---------------------------------------------------------
						// 07/01/2014 ECU display the record which is at the current
						//                position
						// ---------------------------------------------------------
						DisplayAppointment (PublicData.appointments.get(appointmentIndex));
					}
					break;
				}
				// -----------------------------------------------------------------
				case R.id.appointment_edit_button:
				{
					// -------------------------------------------------------------
					// 09/01/2014 ECU indicate the mode
					// -------------------------------------------------------------
					setTitle ("In Edit Mode");
					// -------------------------------------------------------------
					// 07/01/2014 ECU indicate edit mode
					// -------------------------------------------------------------
					mode = StaticData.APPOINTMENT_MODE_EDIT;
					// -------------------------------------------------------------
					// 07/01/2014 ECU edit button pressed
					// -------------------------------------------------------------
					SetAllFields (appointmentLayout,false);
					break;
				}
				// -----------------------------------------------------------------
				case R.id.appointment_finished_button:
				{
					// -------------------------------------------------------------
					// 11/03/2014 ECU everything done so just exit
					// -------------------------------------------------------------
					finish ();
					// -------------------------------------------------------------
				}
					break;
				// -----------------------------------------------------------------
				case R.id.appointment_new_button:
				{
					// -------------------------------------------------------------
					// 07/01/2014 ECU create button pressed
					// -------------------------------------------------------------
					// 09/01/2014 ECU indicate the mode
					// -------------------------------------------------------------
					setTitle ("Creating a New Appointment");
					
					mode = StaticData.APPOINTMENT_MODE_CREATE;
					// -------------------------------------------------------------
					// 07/01/2014 ECU put the fields into create mode
					// -------------------------------------------------------------
					SetAllFields (appointmentLayout,false);
					
					appointmentType.setSelection(0);
					
					RefreshFields (0);
					// -------------------------------------------------------------
					// 07/01/2014 ECU display date and time
					// -------------------------------------------------------------		
					Calendar calendar = Calendar.getInstance();
					
					appointmentDate.init(calendar.get (Calendar.YEAR), 
											calendar.get (Calendar.MONTH), 
											 calendar.get (Calendar.DAY_OF_MONTH),null);
							
					appointmentTime.setCurrentHour(calendar.get (Calendar.HOUR_OF_DAY));
					appointmentTime.setCurrentMinute(calendar.get (Calendar.MINUTE));
					// -------------------------------------------------------------
					// 08/01/2014 ECU set the reminder time
					// -------------------------------------------------------------
					reminderPreferredTime.setCurrentHour(calendar.get (Calendar.HOUR_OF_DAY));
					reminderPreferredTime.setCurrentMinute(calendar.get (Calendar.MINUTE));
					// -------------------------------------------------------------				
					break;				
				}
			}
		}
	};
	// =============================================================================
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// -------------------------------------------------------------------------
		// Inflate the menu; this adds items to the action bar if it is present.
		// -------------------------------------------------------------------------
		return true;
	}
	/* ============================================================================= */
	@Override
	public void onDestroy()
	{	
		// -------------------------------------------------------------------------
		// 21/09/2013 ECU added
		// 06/01/2014 ECU before finishing write the data to disk
		// 07/10/2016 ECU added the context
		//            ECU added false to indicate hash checking only wanted on appointments
		// -------------------------------------------------------------------------
		WriteDataToDisk (getBaseContext(),false);
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU make sure that alarms are regenerated
		// -------------------------------------------------------------------------
		GenerateAlarms (getBaseContext());
		// -------------------------------------------------------------------------
		// 09/01/2018 ECU make sure the progress bar is stopped
		// 08/02/2018 ECU just check that the progress bar has been defined
		// -------------------------------------------------------------------------
		if (handleProgressBar != null)
			handleProgressBar.finishProgressBarUpdate ();
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override 
	protected void onPause() 
	{
		// ---------------------------------------------------------------------------
		// 11/03/2014 ECU if the soft keyboard was on the screen when the 'standby' 
		//                key was pressed then got a warning 'getTextBeforeCursor on 
		//                inactive InputConnection'. Following some searching on the
		//                internet then the following seems to remove the warning
		// ---------------------------------------------------------------------------
	    InputMethodManager inputMethodManager 
	    	= (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(appointmentTime.getWindowToken(),0);
	    // ---------------------------------------------------------------------------

	    super.onPause(); 
	} 
	/* =============================================================================== */
	@Override 
	protected void onResume() 
	{ 	
	   	super.onResume(); 
	}
	/* ============================================================================= */
	public static void AppointmentReminder (Context theContext,int theIndex)
	{
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU an appointment reminder has arrived
		// 31/10/2016 ECU added context to the prints
		// -------------------------------------------------------------------------
		// 20/12/2015 ECU as a belt and braces approach check that the appointments
		//                have been read correctly as this can be called by the
		//                DailyScheduler and was having issues with 'index out of bounds'
		//                when the app is started up - same as in AppointmentTime
		// -------------------------------------------------------------------------
		if (PublicData.appointments.size() > theIndex)
		{
			// ---------------------------------------------------------------------
			// 07/03/2014 ECU added the final 'true' flag to ensure that the wireless
			//                is switched on if necessary
			// ---------------------------------------------------------------------
			Utilities.SendEmailMessage (theContext,"Reminder Time",PublicData.appointments.get(theIndex).Print(theContext,true),true);
			// ---------------------------------------------------------------------
			// 27/03/2016 ECU try and display a reminder message and await a user
			//                response
			// ----------------------------------------------------------------------
			DisplayAMessage (theContext,
							 theContext.getString (R.string.reminder_of_appointment),
							 PublicData.appointments.get(theIndex).PrintHTML (theContext,true));
			// ---------------------------------------------------------------------
			// 04/03/2014 ECU work out the next reminder time - if this is after
			//                the appointment time then do nothing otherwise
			//                set up a new reminder
			// ---------------------------------------------------------------------	
			if (PublicData.appointments.get(theIndex).reminderNextGap > 0l && 
					PublicData.appointments.get(theIndex).reminderNextTime != 0)
			{
				// -----------------------------------------------------------------
				// 03/04/2016 ECU if the app is not running when the reminders occur
				//                and the 'restart app on alarm' is not set then when
				//                the app is restarted then the reminders will occur
				//                in one go which is annoying
				// 31/10/2016 ECU for some reason was using 30000l instead of
				//                the reminderNextGap - took it out
				// -----------------------------------------------------------------
				while ((PublicData.appointments.get(theIndex).reminderNextTime 
						+= PublicData.appointments.get(theIndex).reminderNextGap) < Utilities.getAdjustedTime(false))
				{
					// -------------------------------------------------------------
					// 03/04/2016 ECU check if, during scanning, we have passed the 
					//                appointment time then just break out of the loop
					// -------------------------------------------------------------
					if (PublicData.appointments.get(theIndex).reminderNextTime >=
							PublicData.appointments.get(theIndex).dateTime)
						break;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 09/03/2014 ECU check if the appointment time has been passed
				// -----------------------------------------------------------------
				if (PublicData.appointments.get(theIndex).reminderNextTime <
						PublicData.appointments.get(theIndex).dateTime)
				{
					// -------------------------------------------------------------
					// 09/03/2014 ECU add the '+ 10' into the request code to try and
					//                make the alarm unique
					// 19/12/2015 ECU changed to use ID in static
					// 03/11/2016 ECU changed to use '+ theIndex' instead of '+ 10'
					// -------------------------------------------------------------
					DailyScheduler.CreateAppointmentAlarm (theContext,
							StaticData.ALARM_ID_APPOINTMENT_REMINDER + theIndex,
							StaticData.ALARM_ID_APPOINTMENT_REMINDER,theIndex);
				}
				// -----------------------------------------------------------------
				// 04/03/2014 ECU store the data on disk
				// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
				// -----------------------------------------------------------------
				AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + 
						theContext.getString (R.string.appointments_file),PublicData.appointments);
				// -----------------------------------------------------------------
			}
		}
	}
	/* ============================================================================= */
	public static void AppointmentTime (Context theContext,int theIndex)
	{
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU the time of an appointment has arrived
		// 07/03/2014 ECU added the final 'true' in the call so that if the wireless
		//                network is off then it will be switched on temporarily.
		// 03/03/2015 ECU put in a final check on index
		// 31/10/2016 ECU added context to the prints
		// -------------------------------------------------------------------------
		if (PublicData.appointments.size () > theIndex)
		{
			Utilities.SendEmailMessage(theContext, "Appointment Time",PublicData.appointments.get(theIndex).Print(theContext,true),true);	
			// -------------------------------------------------------------------------
			// 27/03/2016 ECU try and display a reminder message and await a user
			//                response
			// ----------------------------------------------------------------------
			DisplayAMessage (theContext,
					 		 theContext.getString(R.string.appointment_time_has_come),
							 PublicData.appointments.get(theIndex).PrintHTML(theContext,true));
			// -------------------------------------------------------------------------
			//04/03/2014 ECU indicate that appointment no longer active
			// -------------------------------------------------------------------------
			PublicData.appointments.get(theIndex).active = false;
			// -------------------------------------------------------------------------
			// 04/03/2014 ECU update the disk copy
			// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
			// -------------------------------------------------------------------------
			AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + 
					theContext.getString (R.string.appointments_file),PublicData.appointments);
		}
	}
	// =============================================================================
	public static ArrayList<ListItem> BuildTheAppointmentsList ()
	{
		// -------------------------------------------------------------------------
		// 19/12/2015 ECU created to build the appointments list to be used with the
		//                custom adapter
		// -------------------------------------------------------------------------
		SelectorUtilities.selectorParameter.listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 19/12/2015 ECU add in the check on size
		// 30/10/2016 ECU try and speed things up a bit
		//            ECU   use localSize and localAppointment
		// ------------------------------------------------------------------------- 
		int	localSize = PublicData.appointments.size();	
		if (localSize > 0)
		{
			// ---------------------------------------------------------------------
			// 30/10/2016 ECU create a temporary 'working' appointment
			//            ECU added ListItem here
			// ---------------------------------------------------------------------
			AppointmentDetails 	localAppointment;
			ListItem 			localListItem;
			// ---------------------------------------------------------------------
			for (int theIndex = 0; theIndex < localSize; theIndex++)
			{
				// -----------------------------------------------------------------
				localAppointment = PublicData.appointments.get (theIndex);
				// -----------------------------------------------------------------
				// 21/11/2015 ECU added the index as an argument
				// 28/10/2016 ECU changed to use dateFormatDDMMYY
				// 30/10/2016 ECU changed to use localAppointment
				// -----------------------------------------------------------------
				localListItem = new ListItem (APPOINTMENT_IMAGES [localAppointment.type],
											  PublicData.dateSimpleFormatDDMMYYHHMM.format(localAppointment.dateTime),
											  localAppointment.name,
											  localAppointment.contactName,
											  theIndex);
				// -----------------------------------------------------------------
				// 11/10/2016 ECU check if need to change the colour
				// 27/10/2016 ECU change to use .anyNotes
				// 30/10/2016 ECU changed to use localAppointment
				// 02/11/2016 ECU show active appointments using green - loght if
				//                no notes, dark if with notes
				// -----------------------------------------------------------------
				if (localAppointment.active)
				{
					// -------------------------------------------------------------
					// 02/11/2016 ECU the appointment is active show in green
					// -------------------------------------------------------------
					if (localAppointment.anyNotes())
						localListItem.colour = R.color.dark_green;
					else
						localListItem.colour = R.color.light_green;
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 02/11/2016 ECU the appointment is inactive show in gray
					// -------------------------------------------------------------
					if (localAppointment.anyNotes())
						localListItem.colour = R.color.gray;
					else
						localListItem.colour = StaticData.DEFAULT_BACKGROUND_COLOUR;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				SelectorUtilities.selectorParameter.listItems.add (localListItem);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 21/11/2015 ECU sort the items by the description
			// ---------------------------------------------------------------------
			Collections.sort (SelectorUtilities.selectorParameter.listItems);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 21/11/2015 ECU return the list of barcodes that have been generated
		// -------------------------------------------------------------------------
		return SelectorUtilities.selectorParameter.listItems;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	static boolean CheckAppointment (Context theContext,int theAppointmentIndex,long theCurrentTime)
	{	
		// -------------------------------------------------------------------------
		// 08/01/2014 ECU only process active appointments
		// 31/10/2016 ECU added context to the prints
		// -------------------------------------------------------------------------
		if (!PublicData.appointments.get(theAppointmentIndex).active)
				return false;
		// -------------------------------------------------------------------------
		// 08/01/2013 ECU created to check if any action needs to be taken on the current
		//                appointment
		// -------------------------------------------------------------------------
		boolean		dataChanged = false;
		// -------------------------------------------------------------------------
		// 08/01/2014 ECU work in minutes but this is really clumsy
		// -------------------------------------------------------------------------
		long	currentTime 	= theCurrentTime / (long) StaticData.MILLISECONDS_PER_MINUTE;
		long	storedTime  	= PublicData.appointments.get(theAppointmentIndex).dateTime / (long) StaticData.MILLISECONDS_PER_MINUTE;
		long    reminderTime 	= PublicData.appointments.get(theAppointmentIndex).reminderNextTime / (long) StaticData.MILLISECONDS_PER_MINUTE;
		
		if (currentTime >= storedTime)
		{
			// ---------------------------------------------------------------------
			// 08/01/2014 ECU the appointment time has been reached
			// 09/03/2014 ECU added the last 'true' argument so that the message is 
			//                sent in the background and will switch on the wifi 
			//                network if not enabled
			// ---------------------------------------------------------------------
			
			Utilities.SendEmailMessage(theContext, "Appointment Time",
					PublicData.appointments.get(theAppointmentIndex).Print(theContext,true),true);	
			// ---------------------------------------------------------------------
			// 08/01/2014 ECU indicate that appointment no longer active
			// ---------------------------------------------------------------------
			PublicData.appointments.get(theAppointmentIndex).active = false;
			// ---------------------------------------------------------------------
			// 08/01/2014 ECU indicate a change in the record
			// ---------------------------------------------------------------------
			dataChanged = true;
		}
		else
		if (reminderTime != 0 && currentTime >= reminderTime)
		{
			// ---------------------------------------------------------------------
			// 08/01/2014 ECU the reminder time has been reached
			// 09/03/2014 ECU added the last 'true' argument so that the 
			//                message is sent in the background and will
			//                switch on the wifi network if not enabled
			// ---------------------------------------------------------------------
			Utilities.SendEmailMessage(theContext, "Reminder Time",PublicData.appointments.get(theAppointmentIndex).Print(theContext,true),true);	
			// ---------------------------------------------------------------------
			// 08/01/2014 ECU set up the next reminder
			// ---------------------------------------------------------------------
			if (PublicData.appointments.get(theAppointmentIndex).reminderNextGap > 0l)
			{
				PublicData.appointments.get(theAppointmentIndex).reminderNextTime 
							+= PublicData.appointments.get(theAppointmentIndex).reminderNextGap;
				// -----------------------------------------------------------------
				// 08/01/2014 ECU indicate data has changed
				// -----------------------------------------------------------------
				dataChanged = true;
			}
		}
		return dataChanged;
	}
	/* ============================================================================ */
	void CreateAppointment (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 06/01/2014 ECU make sure information in pickers is obtained correctly
		//                if type in
		// 31/10/2016 ECU added context as an argument
		// -------------------------------------------------------------------------
		appointmentDate.clearFocus();
		appointmentTime.clearFocus();
			
		AppointmentDetails appointmentDetails = new AppointmentDetails ();
		
		appointmentDetails.type			= appointmentType.getSelectedItemPosition();
		appointmentDetails.address 		= appointmentAddress.getText().toString();
		appointmentDetails.contactName 	= appointmentContactName.getText().toString();
		appointmentDetails.name 		= appointmentName.getText().toString(); 
		appointmentDetails.notes 		= appointmentNotes.getText().toString(); 
		appointmentDetails.phoneNumber 	= appointmentPhoneNumber.getText().toString();
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.set(appointmentDate.getYear(), 
					 appointmentDate.getMonth(), 
					 appointmentDate.getDayOfMonth(),
					 appointmentTime.getCurrentHour(), 
					 appointmentTime.getCurrentMinute(), 0);
		
		appointmentDetails.dateTime = calendar.getTimeInMillis();
		// -------------------------------------------------------------------------
		// 08/01/2014 ECU add the reminder details
		// -------------------------------------------------------------------------
		appointmentDetails.reminderTrigger	= reminderTime.getSelectedItemPosition();
		appointmentDetails.reminderRepeat	= reminderRepeatTime.getSelectedItemPosition();
		// -------------------------------------------------------------------------
		// 10/01/2014 ECU work out reminderTime - without using calendar
		// 09/03/2014 ECU alter as 'reminderTime' changed to store the hour and minutes
		//                separately
		// -------------------------------------------------------------------------
		appointmentDetails.reminderTimeHour 	= reminderPreferredTime.getCurrentHour();
		appointmentDetails.reminderTimeMinute 	= reminderPreferredTime.getCurrentMinute();
		// -------------------------------------------------------------------------
		GenerateReminderStart (appointmentDetails,reminderTime.getSelectedItemPosition(),reminderRepeatTime.getSelectedItemPosition());
		
		appointmentDetails.reminderNextGap  = reminderNextGap;
		appointmentDetails.reminderNextTime = reminderNextTime;
		// -------------------------------------------------------------------------
		// 07/01/2014 ECU do various time checking before creating the appointment
		// -------------------------------------------------------------------------
		boolean clashFlag = false;
		
		calendar = Calendar.getInstance();
		
		long currentTime = calendar.getTimeInMillis();
		// -------------------------------------------------------------------------
		// 08/01/2014 ECU try and do the following check in minutes
		// -------------------------------------------------------------------------
		currentTime = currentTime / (long) StaticData.MILLISECONDS_PER_MINUTE;
		long inputDateTime = appointmentDetails.dateTime / (long) StaticData.MILLISECONDS_PER_MINUTE;
		// -------------------------------------------------------------------------
		if (inputDateTime < currentTime)
		{
			Utilities.popToast (getString(R.string.appointment_earlier));
			
			clashFlag = true;
		}
		// -------------------------------------------------------------------------
		// 06/01/2014 ECU add into the stored appointments
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.appointments.size(); theIndex++)
		{
			// ---------------------------------------------------------------------
			// 06/01/2014 ECU this is not working OK - works with
			// 08/01/2014 ECU change to work in minutes
			// ---------------------------------------------------------------------
			if (inputDateTime 
					== (PublicData.appointments.get(theIndex).dateTime/(long) StaticData.MILLISECONDS_PER_MINUTE))
			{	
				// -----------------------------------------------------------------
				// 07/01/2014 ECU but if in edit mode and it is this record then accept the clash
				// -----------------------------------------------------------------
				if (mode == StaticData.APPOINTMENT_MODE_CREATE) 
				{
					// -------------------------------------------------------------
					// 07/01/2014 ECU in create mode so clash always
					// -------------------------------------------------------------
					clashFlag = true;
				}
				else
				{
					// -------------------------------------------------------------
					// 07/01/2014 ECU in edit mode so accept if this is the record being
					//                edited
					// -------------------------------------------------------------
					if (appointmentIndex != theIndex)
						clashFlag = true;
				}
				
				if (clashFlag)
				{
					Utilities.popToast(getString(R.string.already_have_appointment) + "\n\n" +
											PublicData.appointments.get(theIndex).Print(theContext));	
				}
			}
		}
		
		if (!clashFlag)
		{
			// ---------------------------------------------------------------------
			// 08/01/2014 ECU indicate that the appointment is active
			// ---------------------------------------------------------------------
			appointmentDetails.active = true;
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU add or amend the appoint with the new details
			// ---------------------------------------------------------------------
			if (mode == StaticData.APPOINTMENT_MODE_CREATE)
			{
				// -----------------------------------------------------------------
				// 07/01/2014 ECU new appointment is being created
				// -----------------------------------------------------------------
				PublicData.appointments.add(appointmentDetails);
				// -----------------------------------------------------------------
				// 09/03/2014 ECU added the last 'true' argument so that the message 
				//				  is sent in the background and will switch on the 
				//                wifi network if not enabled
				// -----------------------------------------------------------------
				Utilities.SendEmailMessage(getBaseContext(), "New Appointment Details",
											appointmentDetails.Print(theContext,true),true);	
				// -----------------------------------------------------------------
				// 04/11/2016 ECU tell the user that the appointment has been created
				// ------------------------------------------------------------------
				Utilities.popToastAndSpeak (getString (R.string.appointment_created),true);
				// ------------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 07/01/2014 ECU replacing an existing appointment
				// -----------------------------------------------------------------
				// 04/11/2016 ECU need to copy over any recorded notes
				// -----------------------------------------------------------------
				if (PublicData.appointments.get(appointmentIndex).recordedNotes != null)
					appointmentDetails.recordedNotes = new ArrayList<RecordedNote> (PublicData.appointments.get(appointmentIndex).recordedNotes);
				// -----------------------------------------------------------------
				Utilities.SendEmailMessage(theContext, "Amended Appointment Details",
						appointmentDetails.Print(theContext,true),
						"Original Details\n================\n" +
						PublicData.appointments.get(appointmentIndex).Print(theContext,true));				
				// -----------------------------------------------------------------
				PublicData.appointments.set (appointmentIndex, appointmentDetails);	
				// -----------------------------------------------------------------
				// 04/11/2016 ECU only do one amendment at a time
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------		
			// 06/01/2014 ECU check whether a new type entry is to be created
			// ---------------------------------------------------------------------
			AppointmentTypeDetails appointmentTypeDetails = GetAppointmentType (appointmentDetails.type);
			
			if (appointmentTypeDetails == null)
			{
				appointmentTypeDetails = new AppointmentTypeDetails ();
				
				appointmentTypeDetails.type			= appointmentDetails.type;
				appointmentTypeDetails.name 		= appointmentDetails.name;;
				appointmentTypeDetails.address 		= appointmentDetails.address;
				appointmentTypeDetails.contactName 	= appointmentDetails.contactName;
				appointmentTypeDetails.phoneNumber 	= appointmentDetails.phoneNumber;
				
				PublicData.appointmentTypes.add (appointmentTypeDetails);
			}
			
			// ---------------------------------------------------------------------
			// 09/03/2014 ECU take out the 'finish' so that a number of appointments
			//                can be set in one go
			// ---------------------------------------------------------------------
			//finish ();
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static void DeleteAppointment (Context theContext,int theAppointmentIndex)
	{
		// -------------------------------------------------------------------------
		// 20/12/2015 ECU created to delete the appointment at the specified position
		// 31/10/2016 ECU added context to the prints
		// 03/11/2016 ECU IMPORTANT - the current code leaves a 'phantom' alarm
		//                =========   because the array shrinks leaving the alarm
		//                            in the last position 'floating'. This does
		//                            not cause a problem but to clarify the situation
		//                            consider the following example. Three appointments
		//                            are configured A, B and C so that the list
		//                            elements 0(A), 1(B) and 2(C) and three alarms
		//                            are generated. At some point appointment
		//                            B is deleted so that the list becomes
		//                            0(A) and 1(C) and two alarms are generated but
		//                            the alarm generated by 2(C) is still active.
		//                            There is no problem because this 'phantom'
		//                            2(C) will generate an index of 2 which does
		//                            not exist and will be ignored. If a new
		//                            appointment, call it D, is defined after the
		//                            delete then the list becomes 0(A), 1(B) and
		//                            2(D) and there will no longer be a problem.
		//                            
		//                            Get around this 'non problem' by deleting the
		//                            alarms of the last element provided it isn't
		//                            the one being deleted.
		// -------------------------------------------------------------------------
		// 03/11/2016 ECU Note - send a confirmation email to the designated
		//                       recipients
		// -------------------------------------------------------------------------
		Utilities.SendEmailMessage (theContext,"Appointment Deletion",
									PublicData.appointments.get (theAppointmentIndex).Print(theContext,true),
									true);
		// -------------------------------------------------------------------------
		// 03/11/2016 ECU cancel any alarms associated with this appointment
		// -------------------------------------------------------------------------
		AppointmentDetails.cancelAlarms (theContext,theAppointmentIndex);
		// -------------------------------------------------------------------------
		// 03/11/2016 ECU delete the last element provided it isn't the one
		//                currently selected
		// -------------------------------------------------------------------------
		int localLastElement = PublicData.appointments.size () - 1;
		if (theAppointmentIndex != localLastElement)
		{
			AppointmentDetails.cancelAlarms (theContext,localLastElement);
		}
		// -------------------------------------------------------------------------
		// 07/01/2014 ECU now delete the entry
		// -------------------------------------------------------------------------
		PublicData.appointments.remove (theAppointmentIndex);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void DisplayAMessage (Context theContext,String theSpokenPhrase,String theMessage)
	{
		// -------------------------------------------------------------------------
		// 27/03/2016 ECU created to display a message about an appointment and
		//                to expect the user to accept it
		// 28/03/2016 ECU change to use MESSAGE_HTML
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (theContext,DisplayAMessage.class);
		localIntent.putExtra (StaticData.PARAMETER_SPOKEN_PHRASE,theSpokenPhrase);
		localIntent.putExtra (StaticData.PARAMETER_START_TIME,theSpokenPhrase);
		localIntent.putExtra (StaticData.PARAMETER_MESSAGE_HTML,theMessage);
		localIntent.putExtra (StaticData.PARAMETER_SPEAK,true);
		localIntent.putExtra (StaticData.PARAMETER_LAYOUT,R.layout.display_an_appointment_message);
		localIntent.putExtra (StaticData.PARAMETER_TIMER,(1000 * 20));
		localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		theContext.startActivity (localIntent);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void DisplayAnAppointment (final int theAppointmentIndex)
	{
		// -------------------------------------------------------------------------
		// 29/11/2016 ECU display an appointment in 'view' mode
		// -------------------------------------------------------------------------
		final Activity localActivity = ((Activity) Selector.context);
		localActivity.setContentView (R.layout.display_appointment_details);
	
				 details		= (TextView) localActivity.findViewById (R.id.appointment_details);
		TextView active  		= (TextView) localActivity.findViewById (R.id.appointment_active);
		Button   play    		= (Button)   localActivity.findViewById (R.id.notes_play_button);
				 seekbarLayout 	= (RelativeLayout) localActivity.findViewById(R.id.seekbar_layout);
		Button   speak   		= (Button)   localActivity.findViewById (R.id.notes_speak_button);
		// -------------------------------------------------------------------------
		// 02/11/2016 ECU make the details field scrollable
		// -------------------------------------------------------------------------
		details.setMovementMethod (new ScrollingMovementMethod ()); 
		// -------------------------------------------------------------------------
		// 29/11/2016 ECU decide if the buttons are to be displayed or note
		// -------------------------------------------------------------------------
		if (!PublicData.appointments.get (theAppointmentIndex).anyWrittenNotes())
			speak.setVisibility (View.INVISIBLE);	
		if (!PublicData.appointments.get (theAppointmentIndex).anyAudioNotes())
			play.setVisibility (View.INVISIBLE);
		// -------------------------------------------------------------------------
		// 31/10/2016 ECU added ...activity to act as the context
		// -------------------------------------------------------------------------
		details.setText (PublicData.appointments.get (theAppointmentIndex).Print (MainActivity.activity,true));
		active.setText ((PublicData.appointments.get (theAppointmentIndex).active ? "active" : "inactive"));
		// -------------------------------------------------------------------------
	   	speak.setOnClickListener(new View.OnClickListener()
		{
	   		@Override
	   		public void onClick (View view) 
	   		{	
	   			// -----------------------------------------------------------------
	   			// 29/11/2016 ECU speak the notes contained in this document
	   			// -----------------------------------------------------------------
	   			Utilities.SpeakAPhrase (Selector.context,PublicData.appointments.get (theAppointmentIndex).notes);
	   			// -----------------------------------------------------------------
			}
		});
	   	// -------------------------------------------------------------------------
		play.setOnClickListener(new View.OnClickListener()
		{
	   		@Override
	   		public void onClick (View view) 
	   		{	
	   			// -----------------------------------------------------------------
	   			// 29/11/2016 ECU start the dialogue to select which  audio note is
	   			//                to be played
	   			// -----------------------------------------------------------------
	   			PlayRecordedNotesMethod (localActivity,theAppointmentIndex);
	   			// -----------------------------------------------------------------
			}
		});
	   	// -------------------------------------------------------------------------
		((Button) localActivity.findViewById (R.id.notes_finish_button)).setOnClickListener (new View.OnClickListener()
		{
	   		@Override
	   		public void onClick (View view) 
	   		{	
	   			// -----------------------------------------------------------------
	   			// 29/11/2016 ECU remove this display 
	   			// -----------------------------------------------------------------
	   			localActivity.finish (); 
	   			// -----------------------------------------------------------------
	   			// 29/11/2016 ECU give the option to select another appointment
	   			// -----------------------------------------------------------------
	   			selectAppointment ();
	   			// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	void DisplayAppointment (AppointmentDetails theAppointment)
	{
		// -------------------------------------------------------------------------
		// 07/01/2014 ECU display the specified appointment
		// -------------------------------------------------------------------------
		// 07/01/2014 ECU just set up the title
		// 09/01/2014 ECU added the check on ...active
		// 10/11/2014 ECU changed to use Locale.getDefault instead of Locale.UK
		// 28/10/2016 ECU changed to use dateFormatDDMMYY
		// 30/10/2016 ECU changed to use Simple.....MMYY
		// -------------------------------------------------------------------------
		setTitle (PublicData.dateSimpleFormatHHMMDDMMYY.format(theAppointment.dateTime) + 
					"     Appointment " + (appointmentIndex + 1) + " of " + PublicData.appointments.size() +
					(theAppointment.active ? StaticData.BLANK_STRING : " actioned"));
		// -------------------------------------------------------------------------
		// 07/01/2014 ECU try and sort the spinner
		// 08/01/2014 ECU adjusted the code
		// -------------------------------------------------------------------------
		appointmentType.setSelection (theAppointment.type);
		// -------------------------------------------------------------------------
		// 07/01/2014 ECU display rest of the fields
		// -------------------------------------------------------------------------
		appointmentAddress.setText (theAppointment.address);
		appointmentContactName.setText (theAppointment.contactName);
		appointmentName.setText (theAppointment.name);
		appointmentNotes.setText (theAppointment.notes);
		appointmentPhoneNumber.setText (theAppointment.phoneNumber);
		// -------------------------------------------------------------------------
		// 07/01/2014 ECU display date and time
		// -------------------------------------------------------------------------
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis (theAppointment.dateTime);

		appointmentDate.init(calendar.get (Calendar.YEAR), 
							 calendar.get (Calendar.MONTH), 
							 calendar.get (Calendar.DAY_OF_MONTH),null);
		
		appointmentTime.setCurrentHour(calendar.get (Calendar.HOUR_OF_DAY));
		appointmentTime.setCurrentMinute(calendar.get (Calendar.MINUTE));
		
		// -------------------------------------------------------------------------
		// 08/01/2014 ECU display the reminder details
		// 09/03/2014 ECU changed following the way the 'reminderTime' is
		//                now stored - separately hours and minutes
		// -------------------------------------------------------------------------	
		reminderPreferredTime.setCurrentHour(theAppointment.reminderTimeHour);
		reminderPreferredTime.setCurrentMinute(theAppointment.reminderTimeMinute);
		// -------------------------------------------------------------------------
		reminderTime.setSelection (theAppointment.reminderTrigger);
		reminderRepeatTime.setSelection (theAppointment.reminderRepeat);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void GenerateAlarms (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU called to set alarms with the AlarmManager
		// -------------------------------------------------------------------------
		if (PublicData.appointments != null && PublicData.appointments.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 04/03/2014 ECU loop for all stored appointments
			// ---------------------------------------------------------------------
			for (int theIndex = 0; theIndex < PublicData.appointments.size(); theIndex++)
			{
				// -----------------------------------------------------------------
				// 04/03/2014 ECU only interested in appointments that are still active
				// -----------------------------------------------------------------
				if (PublicData.appointments.get(theIndex).active)
				{
					Utilities.LogToProjectFile (TAG, "\n==================================\n" + 
													 PublicData.appointments.get(theIndex).Print (theContext) +
						                        	 "\n==================================\n");
					// ------------------------------------------------------------
					// 04/03/2014 ECU generate an alarm for the appointment time
					// 09/03/2014 ECU changed around because an alarm is unique
					//                via the intent and request code
					//            ECU add in 'theIndex' to get a unique alarm - used
					//                to use an 'appointmentsCounter' variable
					// 19/12/2015 ECU changed to use ID in static
					// -------------------------------------------------------------
					DailyScheduler.CreateAppointmentAlarm (theContext,
							StaticData.ALARM_ID_APPOINTMENT_TIME + theIndex,
							StaticData.ALARM_ID_APPOINTMENT_TIME, theIndex);
					// -------------------------------------------------------------
					// 04/03/2014 ECU check an alarm for the next reminder, if enabled
					// -------------------------------------------------------------
					if (PublicData.appointments.get(theIndex).reminderTrigger != 0 && 
						PublicData.appointments.get(theIndex).reminderNextTime != 0)
					{
						// ---------------------------------------------------------
						// 04/03/2014 ECU sort out the reminders
						// 09/03/2014 ECU add the 'appointmentCounter' into the request
						//                code to try and create a unique alarm
						// 19/12/2015 ECU changed to use ID in static
						// ---------------------------------------------------------
						DailyScheduler.CreateAppointmentAlarm (theContext, 
								StaticData.ALARM_ID_APPOINTMENT_REMINDER + theIndex,
								StaticData.ALARM_ID_APPOINTMENT_REMINDER,theIndex);
						// ---------------------------------------------------------
					}
				}		
			}
		}
	}
	// =============================================================================
	public static List<AppointmentDetails> generateSummary (long theDate)
	{
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU created to return a summary of medication for the specified
		//                date
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU adjust 'theDate' to have only the date bit
		// -------------------------------------------------------------------------
		theDate = theDate /StaticData.MILLISECONDS_PER_DAY;
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU declare a working list for appointments that match the date
		// -------------------------------------------------------------------------
		List<AppointmentDetails> appointments = new ArrayList<AppointmentDetails> ();
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU now loop through the stored appointments
		// -------------------------------------------------------------------------
		for (int appointment = 0; appointment < PublicData.appointments.size(); appointment++)
		{
			// ---------------------------------------------------------------------
			// 28/02/2017 ECU check if this appointment is on the specified date
			// ---------------------------------------------------------------------
			if ((PublicData.appointments.get (appointment).dateTime / StaticData.MILLISECONDS_PER_DAY) == theDate)
			{
				// -----------------------------------------------------------------
				// 28/02/2017 ECU add this appointment into the list
				// -----------------------------------------------------------------
				appointments.add(PublicData.appointments.get (appointment));
				// -----------------------------------------------------------------	
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU at this point any appointments for the specified date are
		//                held in the 'appointments' list
		// -------------------------------------------------------------------------
		return appointments;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    public static void DisplaySwipeAction (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 29/11/2016 ECU created to initiate the swipe display of appointments
    	// -------------------------------------------------------------------------	
		Selector.context.startActivity (new Intent (Selector.context,AppointmentSwipeActivity.class));
		// -------------------------------------------------------------------------  
    }
	/* ============================================================================= */
	void GenerateReminderStart (AppointmentDetails theAppointmentDetails,int theStartIndex,int theRepeatIndex)
	{
		// -------------------------------------------------------------------------
		// 01/11/2016 ECU Note - theStartIndex is the index into the array which
		//                ====   specifies when the reminder is to start.
		//						 theRepeatIndex is the index into the array which
		//                       specifies the gap between subsequent reminders
		// -------------------------------------------------------------------------
		// 08/01/2014 ECU start by working out the start time and gap for reminder
		// 08/01/2014 ECU work out the time when the first reminder is to start
		// 08/01/2014 ECU default to preferred time not relevant
		// 20/12/2015 ECU rearrange to get the 'first' reminder correct
		// -------------------------------------------------------------------------
		// 01/11/2016 ECU Note - when setting up a reminder it is possible to set
		//                ====   the preferred time when reminders will be generated.
		//                       The reason for this is that an appointment may be,
		//                       for example 0700 on a particular day with reminders
		//                       to start a day before then, however 0700 may not be
		//                       a convenient time for reminders so it is possible
		//                       to specify a more convenient time. This preferred
		//                       time is only used for generating the first reminder
		//                       because all subsequent reminders (via the reminder
		//                       gap) will be based on that one.
		//
		//						 The preferred time is only used when the first reminder
		//                       is at least 1 day in advance of the appointment.
		// -------------------------------------------------------------------------
		boolean preferredTimeRelevant = false;
		// -------------------------------------------------------------------------
		// 01/11/2016 ECU Note - determine when the reminder is to start
		//                ====
		// -------------------------------------------------------------------------
		switch (theStartIndex)
		{
			// ---------------------------------------------------------------------
			// 31/10/2016 ECU changed to use StaticData.APPOINTMENT_...
			// ---------------------------------------------------------------------
			case	StaticData.APPOINTMENT_NO_REMINDER:			// no reminder
					reminderNextTime = 0;
					break;
			// ---------------------------------------------------------------------
			case	StaticData.APPOINTMENT_START_1_DAY:			// -1 day
					reminderNextTime = theAppointmentDetails.dateTime - StaticData.MILLISECONDS_PER_DAY;
					// -------------------------------------------------------------
					// 09/03/2014 ECU added the ability to set a reminder
					// -------------------------------------------------------------
					preferredTimeRelevant = true;
					// -------------------------------------------------------------
					break;
			// ---------------------------------------------------------------------		
			case	StaticData.APPOINTMENT_START_2_DAYS:		// -2 days
					reminderNextTime = theAppointmentDetails.dateTime - StaticData.MILLISECONDS_PER_DAY * 2l;
					preferredTimeRelevant = true;
					break;
			// ---------------------------------------------------------------------
			case    StaticData.APPOINTMENT_START_1_WEEK:		// - 7 days
					reminderNextTime = theAppointmentDetails.dateTime - (StaticData.MILLISECONDS_PER_DAY * 7l);
					preferredTimeRelevant = true;
					break;
			// ---------------------------------------------------------------------		
			case	StaticData.APPOINTMENT_START_30_DAYS:		// - 1 month
					reminderNextTime = theAppointmentDetails.dateTime - (StaticData.MILLISECONDS_PER_DAY * 30l);
					preferredTimeRelevant = true;
					break;
			// ---------------------------------------------------------------------		
			case 	StaticData.APPOINTMENT_START_2_HOURS:		// - 2 hours
					// -------------------------------------------------------------
					// 09/01/2014 ECU added more for testing purposes
					// 01/11/2016 ECU changed from _PER_MINUTE to _PER_HOUR
					// -------------------------------------------------------------
					reminderNextTime = theAppointmentDetails.dateTime - (StaticData.MILLISECONDS_PER_HOUR * 2l);
					break;
			// ---------------------------------------------------------------------		
			default:
					// -------------------------------------------------------------
					// 01/11/2016 ECU just in case
					// -------------------------------------------------------------
					reminderNextTime = 0l;
					break;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 10/01/2014 ECU check if need to use the preferred time
		// -------------------------------------------------------------------------
		if (preferredTimeRelevant)
		{
			// ---------------------------------------------------------------------
			// 01/11/2016 ECU IMPORTANT - read the notes at the start of this
			//                =========   method.
			// ---------------------------------------------------------------------
			// 10/01/2014 ECU just get the base day in milliseconds
			// ---------------------------------------------------------------------
			reminderNextTime = (reminderNextTime / StaticData.MILLISECONDS_PER_DAY) * StaticData.MILLISECONDS_PER_DAY;
			// ---------------------------------------------------------------------
			// 10/01/2014 ECU add in the preferred time
			// 09/03/2014 ECU change to reflect the new definition of reminderTime
			// ---------------------------------------------------------------------	
			reminderNextTime += theAppointmentDetails.ReminderTime ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 08/01/2014 ECU work out the gap in milliseconds between reminders
		// 20/12/2016 ECU reset the next gap
		// -------------------------------------------------------------------------
		reminderNextGap = 0l;
		// -------------------------------------------------------------------------
		switch (theRepeatIndex)
		{	
			// ---------------------------------------------------------------------
			// 31/10/2016 ECU changed from GAP_ to StaticData.APPOINTMENT_GAP_
			// ---------------------------------------------------------------------
			case	StaticData.APPOINTMENT_GAP_HOUR:		// every hour
					reminderNextGap = ((long) StaticData.MILLISECONDS_PER_MINUTE * 60l);
					break;
			// ---------------------------------------------------------------------
			case	StaticData.APPOINTMENT_GAP_DAY:			// every day
					reminderNextGap = StaticData.MILLISECONDS_PER_DAY;
					break;
			// ---------------------------------------------------------------------	
			case	StaticData.APPOINTMENT_GAP_WEEK:		// every hour
					reminderNextGap = ((long) StaticData.MILLISECONDS_PER_DAY * 7l);
					break;
			// ---------------------------------------------------------------------		
			case	StaticData.APPOINTMENT_GAP_10_MINUTES:	// every ten minutes
					// -------------------------------------------------------------
					// 09/01/2014 ECU added more for testing purposes
					// -------------------------------------------------------------
					reminderNextGap = ((long) StaticData.MILLISECONDS_PER_MINUTE * 10l);
					break;
			// ---------------------------------------------------------------------		
			default:
					break;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 08/01/2014 ECU if the first reminder is before the current time
		//                in which case treat as no reminders
		// -------------------------------------------------------------------------
		Calendar localCalendar 		= Calendar.getInstance();
		long 	 localCurrentTime 	= localCalendar.getTimeInMillis();
		// -------------------------------------------------------------------------
		if (reminderNextTime != 0 && reminderNextTime < localCurrentTime)
		{
			// ---------------------------------------------------------------------
			// 08/01/2014 ECU the first reminder is before the current time
			// 09/04/2014 ECU changed to use resource
			// 20/12/2015 ECU if there is no next gap then set the reminder for
			//                'now' + 1 minute otherwise add the increment until
			//                it is later than the current time
			// ---------------------------------------------------------------------
			Utilities.popToast (getString (R.string.first_reminder_passed));
			
			if (reminderNextGap == 0)
			{
				// -----------------------------------------------------------------
				// 20/12/2015 ECU reset the first reminder time to now + 1 minute
				// -----------------------------------------------------------------
				reminderNextTime = localCurrentTime + StaticData.MILLISECONDS_PER_MINUTE;
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 20/12/2015 ECU want to adjust the 'first' time using the 'gap' until
				//                it is after the current time
				// -----------------------------------------------------------------
				while (reminderNextTime < localCurrentTime)
				{
					// -------------------------------------------------------------
					// 20/12/2015 ECU increment the reminder by the gap to next reminder
					// -------------------------------------------------------------
					reminderNextTime += reminderNextGap;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
		}
	}
	/* ============================================================================= */
	AppointmentTypeDetails GetAppointmentType (int theWantedType)
	{
		for (int theIndex = 0; theIndex < PublicData.appointmentTypes.size (); theIndex++)
		{
			if (PublicData.appointmentTypes.get (theIndex).type == theWantedType)
			{
				return PublicData.appointmentTypes.get (theIndex);
			}	
		}
		return null;
	}
	/* ============================================================================ */
	public void RefreshFields (int theSelectData)
	{
		// -------------------------------------------------------------------------
		// 07/01/2014 ECU only refresh the fields if in create mode
		// -------------------------------------------------------------------------
		if (mode == StaticData.APPOINTMENT_MODE_CREATE)
		{
			AppointmentTypeDetails appointmentTypeDetails = GetAppointmentType (theSelectData);
		
			if (appointmentTypeDetails != null)
			{
				appointmentAddress.setText 		(appointmentTypeDetails.address);
				appointmentContactName.setText 	(appointmentTypeDetails.contactName);
				appointmentName.setText    		(appointmentTypeDetails.name);
				appointmentPhoneNumber.setText  (appointmentTypeDetails.phoneNumber);
				// -----------------------------------------------------------------
				// 07/01/2014 ECU just clear the field
				// -----------------------------------------------------------------
				appointmentNotes.setText (StaticData.BLANK_STRING);
			}	
		}
	}
	/* ============================================================================ */
	void SetAllFields (RelativeLayout theLayout,boolean theEditFlag)
	{
		// ------------------------------------------------------------------------	
		// 31/10/2016 ECU Note - created to put the fields into a mode dependent on
		//                       the edit mode 
		//							theEditFlag = true ...... edit mode
		//                                      = false ..... display mode
		// -------------------------------------------------------------------------
		if (theEditFlag)
		{
			// ---------------------------------------------------------------------
			// 31/10/2016 ECU Note - in edit mode so make fields visible and
			//                       enabled
			// ---------------------------------------------------------------------
			for ( int index = 0; index < theLayout.getChildCount();  index++ )
			{
		 	 
				View view = theLayout.getChildAt(index);
				view.setEnabled(false);
			}
			// ---------------------------------------------------------------------
			appointmentEditButton.setEnabled (true);
			appointmentDeleteButton.setEnabled (true);
			appointmentNewButton.setEnabled (true);
			appointmentDeleteButton.setVisibility (View.VISIBLE);
	    	appointmentEditButton.setVisibility (View.VISIBLE);
	    	appointmentNewButton.setVisibility (View.VISIBLE);
	    	// ---------------------------------------------------------------------
	    	appointmentType.setEnabled (false);
	    	appointmentAcceptButton.setEnabled (false);
	    	appointmentCancelButton.setEnabled (false);
	    	appointmentFinishedButton.setEnabled (false);				// 11/03/2014 ECU added
			appointmentAcceptButton.setVisibility (View.INVISIBLE);
	    	appointmentCancelButton.setVisibility (View.INVISIBLE);
	    	appointmentFinishedButton.setVisibility (View.INVISIBLE);	// 11/03/2014 ECU added
	    	// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 31/10/2016 ECU Note - in non-edit mode so change fields to
			//                       visible and enabled or invisible and
			//                       disabled
			// ---------------------------------------------------------------------
			for ( int index = 0; index < theLayout.getChildCount();  index++ )
			{
		 	 
				View view = theLayout.getChildAt (index);
				view.setEnabled (true);
			}
			// ---------------------------------------------------------------------
			appointmentType.setEnabled(true);
			
			appointmentAcceptButton.setEnabled (true);
			appointmentCancelButton.setEnabled (true);
			appointmentFinishedButton.setEnabled(true);					// 11/03/2014 ECU added
			appointmentAcceptButton.setVisibility (View.VISIBLE);
	    	appointmentCancelButton.setVisibility (View.VISIBLE);
	    	appointmentFinishedButton.setVisibility (View.VISIBLE);		// 11/03/2014 ECU added
	    	// ---------------------------------------------------------------------
	    	appointmentDeleteButton.setEnabled (false);
			appointmentEditButton.setEnabled (false);
			appointmentNewButton.setEnabled (false);
			appointmentDeleteButton.setVisibility (View.INVISIBLE);
	    	appointmentEditButton.setVisibility (View.INVISIBLE);
	    	appointmentNewButton.setVisibility (View.INVISIBLE);
	    	// ---------------------------------------------------------------------
	    	// 10/01/2014 ECU set listener for time picker 
	    	// ---------------------------------------------------------------------
			appointmentTime.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() 
			{
				public void onTimeChanged(TimePicker view, int hourOfDay, int minute) 
	            {
					// -------------------------------------------------------------
					// 10/01/2014 ECU if appointment time changes then change
					//                preferred time
					// -------------------------------------------------------------
					reminderPreferredTime.setCurrentHour(hourOfDay);
					reminderPreferredTime.setCurrentMinute(minute);
	            }
	        });	
			// ---------------------------------------------------------------------
		}	
	}
	/* ============================================================================= */
	public static void WriteDataToDisk (Context theContext,boolean theAlwaysFlag)
	{
		// -------------------------------------------------------------------------
		// 06/01/2014 ECU write out the data to disk
		// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
		// 30/03/2016 ECU use the hash codes to check if the data has changed and
		//                whether the disk version needs to be updated
		// 07/10/2016 ECU changed to static and added the context
		//            ECU added the always flag
		// -------------------------------------------------------------------------
		if ((initialHashCodeData != PublicData.appointments.hashCode()) || theAlwaysFlag)
			AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + 
				theContext.getString (R.string.appointments_file),PublicData.appointments);
		if (initialHashCodeTypes != PublicData.appointmentTypes.hashCode())
			AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + 
				theContext.getString (R.string.appointment_types_file),PublicData.appointmentTypes);
	}
	/* ============================================================================= */
	/* ============================================================================= */
	// 06/01/2014 ECU put this in because when had implements OnSelect.... on
	//                activity then methods did not seem to be called
	/* ============================================================================= */
	/* ============================================================================= */
	public class localOnItemSelectedListener implements OnItemSelectedListener 
	{
		public void onItemSelected(AdapterView<?> parent, View view, int position,long id) 
		{
			// ---------------------------------------------------------------------
			// 06/01/2014 ECU get other fields updated
			// ---------------------------------------------------------------------
			RefreshFields (position);
		}
		/* ------------------------------------------------------------------------ */
		@Override
		public void onNothingSelected(AdapterView<?> arg0) 
		{
		}
	}
	/* ============================================================================ */
	// 04/03/2014 ECU removed now that the AlarmManager is being used
	// ----------------------------------------------------------------------------
	//public static void ProcessAppointments (Context theContext)
	//{
	//	// 07/01/2014 ECU decrement the counter to check on how often to process
	//	//                the appointments in minutes
	//	
	//	// 08/01/2014 ECU get current time just to be sure
	//	
	//	Calendar currentTime = Calendar.getInstance();
	//	boolean updateDisk = false;
	//	
	//	if (--MainActivity.appointmentsCounter <= 0)
	//	{	
	//		for (int theIndex = 0; theIndex < MainActivity.appointments.size(); theIndex++)
	//		{
	//			
	//			// 08/01/2014 ECU if the following returns true it indicates that some
	//			//                data has changed so the disk copy should be updated
	//			
	//			if (CheckAppointment (theContext,theIndex,currentTime.getTimeInMillis()))
	//				updateDisk = true;
	//		}
	//		
	//		// 08/01/2014 ECU check if the disk copy needs updating
	//		
	//		if (updateDisk)
	//		{
	//			Utilities.writeObjectToDisk (PublicData.projectFolder + 
	//					theContext.getString (R.string.appointments_file),MainActivity.appointments);
	//		}
	//		
	//		// 07/01/2014 ECU reset the counter
	//		
	//		MainActivity.appointmentsCounter = MainActivity.storedData.appointmentsCounter;
	//	}
	//}
	// end of 04/03/2014 ECU removal

	// =============================================================================
	static void selectAppointment ()
	{
		// -------------------------------------------------------------------------
		// 19/12/2015 ECU before activating the Selector activity then need to set up
		//                various parameters
		// -------------------------------------------------------------------------
		// 19/12/2015 ECU initialise the stored arguments
		// -------------------------------------------------------------------------
		SelectorUtilities.Initialise ();
		// -------------------------------------------------------------------------
		//19/12/2015 ECU build up the list of items that will be displayed
		// -------------------------------------------------------------------------
		BuildTheAppointmentsList ();
		// -------------------------------------------------------------------------
		// 19/12/2015 ECU set up the variables that control the interaction with
		//                the Selector activity
		// 29/11/2016 ECU check whether appointments being displayed or available
		//                for adding or editing
		// -------------------------------------------------------------------------
		SelectorUtilities.selectorParameter.type 		= StaticData.OBJECT_APPOINTMENTS;
		SelectorUtilities.selectorParameter.classToRun 	= AppointmentsActivity.class;
		// -------------------------------------------------------------------------
		// 29/11/2016 ECU declare common methods
		// --------------------------------------------------------------------------
		SelectorUtilities.selectorParameter.backMethodDefinition 	
			= new MethodDefinition<AppointmentsActivity> (AppointmentsActivity.class,"BackAction");
		// -------------------------------------------------------------------------
		// 04/11/2016 ECU declare the method to handle the help key
		// -------------------------------------------------------------------------
		SelectorUtilities.selectorParameter.helpMethodDefinition 	
			= new MethodDefinition<AppointmentsActivity> (AppointmentsActivity.class,"HelpAction");
		// -------------------------------------------------------------------------
		// 29/11/2016 ECU now decide whether displaying only or providing edit/add
		//                functions
		// -------------------------------------------------------------------------
		if (!appointmentsDisplay)
		{
			// ---------------------------------------------------------------------
			// 29/11/2016 ECU want to be able to add or edit appointments
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.rowLayout 		= R.layout.selector_appointments_row;
			SelectorUtilities.selectorParameter.customLegend 	= context.getString (R.string.add);
			// ---------------------------------------------------------------------
			// 19/12/2015 ECU declare the methods that will be called by the Selector
			//                activity when certain key strokes occur
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.customMethodDefinition 	
				= new MethodDefinition<AppointmentsActivity> (AppointmentsActivity.class,"AddAppointment");
			// ---------------------------------------------------------------------
			// 04/11/2016 ECU added the edit method and removed the long select method
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.editMethodDefinition 	
				= new MethodDefinition<AppointmentsActivity> (AppointmentsActivity.class,"AddNote");
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.selectMethodDefinition 	
				= new MethodDefinition<AppointmentsActivity> (AppointmentsActivity.class,"SelectAction");
			SelectorUtilities.selectorParameter.swipeMethodDefinition	
				= new MethodDefinition<AppointmentsActivity> (AppointmentsActivity.class,"SwipeAction");
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 29/11/2016 ECU am only displaying appointments
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.rowLayout	= R.layout.display_appointments_row;
			SelectorUtilities.selectorParameter.selectMethodDefinition 	
				= new MethodDefinition<AppointmentsActivity> (AppointmentsActivity.class,"AddNote");
			SelectorUtilities.selectorParameter.swipeMethodDefinition	
				= new MethodDefinition<AppointmentsActivity> (AppointmentsActivity.class,"DisplaySwipeAction");
			// ---------------------------------------------------------------------
			// 29/11/2016 ECU if an appointment is selected then just display it
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.selectMethodDefinition 	
				= new MethodDefinition<AppointmentsActivity> (AppointmentsActivity.class,"DisplayAnAppointment");
			// ---------------------------------------------------------------------
			// 29/11/2016 ECU indicate that the selector activity is not to finish
			//                when the an appointment is selected
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.finishOnSelect = false;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 19/12/2015 ECU now start up the Selector activity which will be
		//                configured using the arguments in the
		//                'SelectorUtilities.selectorParameter'
		// -------------------------------------------------------------------------
		SelectorUtilities.StartSelector (context,StaticData.OBJECT_APPOINTMENTS);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void setPlayButton ()
	{
		// -------------------------------------------------------------------------
		// 29/11/2016 ECU change to reflect whether just displaying the appointment
		//                or not
		// -------------------------------------------------------------------------
		if (!appointmentsDisplay)
		{
			// ---------------------------------------------------------------------
			// 29/11/2016 ECU this is the normal 'edit' mode
			// ---------------------------------------------------------------------
			// 24/10/2016 ECU change the button's legend and the state
			// ---------------------------------------------------------------------
			GetMessage.setPlayButtonLegend (false);
			// ---------------------------------------------------------------------
			// 24/10/2016 ECU define and start the update of the progress bar
			// ---------------------------------------------------------------------
			handleProgressBar = new HandleProgressBar ((Activity) GetMessage.context,
					                                   1000,
					                                   GetMessage.seekBarLayout,
					                                   GetMessage.messageView);   					                               
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 29/11/2016 ECU this is the 'display only' mode
			// ---------------------------------------------------------------------
			handleProgressBar = new HandleProgressBar ((Activity) Selector.context,
													   1000,
													   seekbarLayout,
													   details);
			// ---------------------------------------------------------------------
		}
		handleProgressBar.startProgressBarUpdate ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
    public static void AddAppointment (int theAppointmentSelected)
    {
    	// -------------------------------------------------------------------------
    	//19/12/2015 ECU create to handle the selection of an appointment
    	//           ECU need to terminate the 'selector' activity before starting
    	//               the creation process
    	// -------------------------------------------------------------------------
    	Selector.Finish();
    	mode			 = StaticData.APPOINTMENT_MODE_CREATE;
    	((AppointmentsActivity) context).SetAllFields (appointmentLayout,false);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void AddNote (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 07/10/2016 ECU created to handle long select on a list view item
    	// -------------------------------------------------------------------------
    	// 05/01/2014 ECU request a reason from the user and then process
    	// -------------------------------------------------------------------------
    	Intent localIntent = new Intent (((AppointmentsActivity) context),GetMessage.class);	
    	// -------------------------------------------------------------------------
    	// 07/10/2016 ECU display any notes already stored in the appointment
    	// 09/10/2016 ECU add the NOTES to request different format
    	// 23/10/2016 ECU added recorded notes
    	// 26/10/2016 ECU recorded notes .... changed to putExtra
    	// -------------------------------------------------------------------------
    	localIntent.putExtra (StaticData.PARAMETER_MESSAGE,PublicData.appointments.get (thePosition).notes);
    	localIntent.putExtra (StaticData.PARAMETER_DATA,thePosition);
    	localIntent.putExtra (StaticData.PARAMETER_NOTES,true);	
    	localIntent.putExtra (StaticData.PARAMETER_RECORDED_NOTES,(ArrayList<RecordedNote>) PublicData.appointments.get (thePosition).recordedNotes);
    	localIntent.putExtra (StaticData.PARAMETER_METHOD,new MethodDefinition<AppointmentsActivity> (AppointmentsActivity.class,"NotesMethod"));
    	localIntent.putExtra (StaticData.PARAMETER_SPEAK_METHOD,new MethodDefinition<AppointmentsActivity> (AppointmentsActivity.class,"PlayRecordedNotesMethod"));
		localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
    	((AppointmentsActivity) context).startActivityForResult (localIntent,StaticData.REQUEST_CODE_FILE);
		// -------------------------------------------------------------------------  
    }
    // =============================================================================
 	public static void BackAction (int theDummyArgument)
 	{
 		// -------------------------------------------------------------------------
 		// 22/11/2015 ECU created to handle the BACK key when called from the
 		//                Selector activity. The argument is included but is not 
 		//                needed.
 		// -------------------------------------------------------------------------
 		// 22/11/2015 ECU just finish the activity
 		// -------------------------------------------------------------------------
 		((Activity)context).finish ();
 		//--------------------------------------------------------------------------
 	}
   // =============================================================================
    public static void HelpAction (int theAppointmentIndex)
    {
    	// -------------------------------------------------------------------------
    	// 04/11/2016 ECU called up to handle the help key on an appointment
    	// ------------------------------------------------------------------------
    	Utilities.DisplayADrawable (Selector.context,R.drawable.appointment_long_press);
    	// -------------------------------------------------------------------------
    	// 05/11/2016 ECU give particular information on this appointment
		// -------------------------------------------------------------------------
    	Utilities.popToastAndSpeak ("This appointment is " + 
    				(PublicData.appointments.get (theAppointmentIndex).active ? StaticData.BLANK_STRING : "in") + "active with " +
    				(PublicData.appointments.get (theAppointmentIndex).anyNotes() ? StaticData.BLANK_STRING : "no ") + "notes",true); 
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
 	public static void NoMethod (Object theSelection)
   	{
   	}
 	// =============================================================================
 	public static void NotesMethod (Object [] theObjects)
 	{
 		// -------------------------------------------------------------------------
 		// 07/10/2016 ECU created to handle the message returned from GetMessage
 		// -------------------------------------------------------------------------
 		int localIndex = (Integer) theObjects [0];
 		// -------------------------------------------------------------------------
 		PublicData.appointments.get(localIndex).notes = (String) theObjects [1];
 		// -------------------------------------------------------------------------
 		// 23/10/2016 ECU store the path to any recorded notes
 		// 26/10/2016 ECU changed to use RecordedNote (not String)
 		// -------------------------------------------------------------------------
 		RecordedNote localRecordedNotes = PublicData.appointments.get (localIndex).lastRecordedNotes ();
 		// -------------------------------------------------------------------------
 		// 23/10/2016 ECU check if additional notes have been added
 		// 26/10/2016 ECU changed from String to RecordedNote
 		// -------------------------------------------------------------------------
 		RecordedNote localReturnedNotes = (RecordedNote) theObjects [2];
 		// -------------------------------------------------------------------------
 		// 23/10/2016 ECU check if any recorded notes have been returned
 		// -------------------------------------------------------------------------
 		if (localReturnedNotes != null)
 		{
 			if (localRecordedNotes == null)
 			{
 				PublicData.appointments.get(localIndex).recordedNotes.add (localReturnedNotes);
 				// -----------------------------------------------------------------
 				// 25/10/2016 ECU make sure the new audio file is synchronised
 				// 26/10/2016 ECU changed to use '.Add' method
 				//            ECU changed to use .fileName
 				// -----------------------------------------------------------------
 				FileToSynchronise.Add (PublicData.projectFolder + localReturnedNotes.fileName);
 				// -----------------------------------------------------------------
 			}
 			else
 			{
 				
 				if (!localRecordedNotes.equals (localReturnedNotes))
 				{
 					PublicData.appointments.get(localIndex).recordedNotes.add (localReturnedNotes);
 					// -----------------------------------------------------------------
 	 				// 25/10/2016 ECU make sure the new audio file is synchronised
 					// 26/10/2016 ECU changed to use '.Add' method
 					//            ECU changed to use .fileName
 	 				// -----------------------------------------------------------------
 					FileToSynchronise.Add (PublicData.projectFolder + localReturnedNotes.fileName);
 	 				// -----------------------------------------------------------------
 				}
 			}
 			// ---------------------------------------------------------------------
 		}
 		// -------------------------------------------------------------------------
 		// 07/10/2016 ECU added the context
 		//            ECU add true to always write to disk irrespective of hash code
 		//                checking
 		// -------------------------------------------------------------------------
		WriteDataToDisk (context,true);
		// -------------------------------------------------------------------------
 	}
 	// =============================================================================
 	public static void playingFinished ()
 	{
 		// -------------------------------------------------------------------------
 		// 24/10/2016 ECU created to be called from the MessageHandler when the
 		//                audio and all outstanding actions have been processed
		// -------------------------------------------------------------------------
 		GetMessage.setPlayButtonLegend (true);
 		// ---------------------------------------------------------------------
 		// 24/10/2016 ECU remove the seekbar
 		// ---------------------------------------------------------------------
 		handleProgressBar.finishProgressBarUpdate ();
 		// ---------------------------------------------------------------------
 	}
 	// =============================================================================
 	public static RecordedNote playLastNotes (int theAppointmentIndex)
 	{
 		// -------------------------------------------------------------------------
 		// 24/10/2016 ECU play the most current note for the specified appointment
 		// 25/10/2016 ECU a 'null' is returned if nothing exists
 		// 26/10/2016 ECU changed to use and return RecordedNote
 		// -------------------------------------------------------------------------
 		RecordedNote lastNotes = PublicData.appointments.get (theAppointmentIndex).lastRecordedNotes();
 		// -------------------------------------------------------------------------
 		// 25/10/2016 ECU check if there is anything to play
 		// -------------------------------------------------------------------------
 		if (lastNotes != null)
 		{
 			// ---------------------------------------------------------------------
 			// 24/10/2016 ECU if the notes exist then play them
 			// ---------------------------------------------------------------------
			// 26/10/2016 ECU tell the user what is going on and then play the file
			// ---------------------------------------------------------------------
 			AppointmentDetails.playAFile (context.getString (R.string.notes_playing_latest),
 										  PublicData.projectFolder + lastNotes.fileName);
 			// ---------------------------------------------------------------------
 			// 24/10/2016 ECU set the button legend and initiate the progress bar
 			// ---------------------------------------------------------------------
 			setPlayButton ();
 			// ---------------------------------------------------------------------
 		}
 		// -------------------------------------------------------------------------
 		// 25/10/2016 ECU return with the name
 		// -------------------------------------------------------------------------
 		return lastNotes;
 		// -------------------------------------------------------------------------
 	}
 	// =============================================================================
 	public static void PlayRecordedNoteMethod (int theRecordedNote)
 	{
 		// -------------------------------------------------------------------------
 		// 23/10/2016 ECU created to play an individual record note or all of them
 		// -------------------------------------------------------------------------
 		if (theRecordedNote == 0)
 		{
 			PublicData.appointments.get (appointmentIndex).playAllRecordedNotes ();
 		}
 		else
 		{
 			// ---------------------------------------------------------------------
 			// 25/10/2016 ECU add in the project folder as recordedNotes is now
 			//                a path relative to it
 			// 26/10/2016 ECU changed to use .fileName
 			// ---------------------------------------------------------------------
 			Utilities.PlayAFile (context,PublicData.projectFolder + PublicData.appointments.get (appointmentIndex).recordedNotes.get (theRecordedNote - 1).fileName);
 		}
 		// -------------------------------------------------------------------------
 		// 24/10/2016 ECU set the button legend and initiate the progress bar
 		// -------------------------------------------------------------------------
 		setPlayButton ();
 		// -------------------------------------------------------------------------
 	}
 	// =============================================================================
 	public static void PlayRecordedNotesMethod (Context theContext,int theAppointmentIndex)
 	{
 		// -------------------------------------------------------------------------
 		// 23/10/2016 ECU created to play all of the stored recorded notes for the
 		//                specified appointment
 		// 29/11/2016 ECU added the context as an argument to make it more flexible
 		// -------------------------------------------------------------------------
 		appointmentIndex = theAppointmentIndex;
 		// -------------------------------------------------------------------------
 		// 23/10/2016 ECU request the entry to be played
 		// 29/11/2016 ECU make sure the correct context is selected
 		// -------------------------------------------------------------------------
 		DialogueUtilities.singleChoice (theContext,
 										"Select Notes to Play",
 										PublicData.appointments.get(theAppointmentIndex).returnRecordedNotesTitles(),
 										0, 
 										Utilities.createAMethod (AppointmentsActivity.class,"PlayRecordedNoteMethod",0),
 										null);
 		// -------------------------------------------------------------------------
 	}
 	// =============================================================================
 	public static void PlayRecordedNotesMethod (int theAppointmentIndex)
 	{
 		// -------------------------------------------------------------------------
 		// 29/11/2016 ECU created to be called from GetMessage and to call the
 		//                master message
 		// -------------------------------------------------------------------------
 		PlayRecordedNotesMethod (GetMessage.context,theAppointmentIndex);
 		// -------------------------------------------------------------------------
 	}
	// =============================================================================
    public static void SelectAction (int theAppointmentSelected)
    {
    	// -------------------------------------------------------------------------
    	//19/12/2015 ECU create to handle the selection of an appointment
    	// -------------------------------------------------------------------------
    	appointmentIndex = theAppointmentSelected;
    	mode			 = StaticData.APPOINTMENT_MODE_EDIT;
    	((AppointmentsActivity) context).SetAllFields (appointmentLayout,false);
		((AppointmentsActivity) context).DisplayAppointment (PublicData.appointments.get (theAppointmentSelected));
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void SwipeAction (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 19/12/2015 ECU created to handle swipe actions on a list view item
    	// -------------------------------------------------------------------------
		// 10/06/2015 ECU created to initiate the dialogue
    	// 28/10/2016 ECU changed to use dateFormatDDMMYY
    	// 30/10/2016 ECU changed to use Simple....MMYY
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (Selector.context,"Appointment Deletion",
	    		   "Do you really want to delete the entry for '" + PublicData.dateSimpleFormatHHMMDDMMYY.format(PublicData.appointments.get (thePosition).dateTime) + "'",
	    		   (Object) thePosition,
	    		   Utilities.createAMethod (AppointmentsActivity.class,"YesMethod",(Object) null),
	    		   Utilities.createAMethod (AppointmentsActivity.class,"NoMethod",(Object) null)); 
		// -------------------------------------------------------------------------  
    }
  	// =============================================================================
  	public static void YesMethod (Object theSelection)
  	{
  		// -------------------------------------------------------------------------
  		// 19/12/2015 ECU the selected item can be deleted
  		// -------------------------------------------------------------------------
  		int localSelection = (Integer) theSelection;
  		// -------------------------------------------------------------------------
  		DeleteAppointment (AppointmentsActivity.context,localSelection);
  		// -------------------------------------------------------------------------
  		// 20/12/2015 ECU check if all appointments have been deleted
  		// -------------------------------------------------------------------------
  		if (PublicData.appointments.size() > 0 )
  		{
  			// ---------------------------------------------------------------------
  			// 20/12/2015 ECU still have some appointments so rebuild the selector
  			//                display
  			// ---------------------------------------------------------------------
  			// 19/12/2015 ECU rebuild and then display the updated list view
  			// ---------------------------------------------------------------------
  			Selector.Rebuild ();
  			// ---------------------------------------------------------------------
  		}
  		else
  		{
  			// ---------------------------------------------------------------------
  			// 201/12/2015 ECU all of the appointments have been deleted
  			// ---------------------------------------------------------------------
  			Utilities.popToast ("All of the appointments have been deleted");
  			// ---------------------------------------------------------------------
  			// 20/12/2015 ECU finish the selector interface
  			// ---------------------------------------------------------------------
  			Selector.Finish();
  			// ---------------------------------------------------------------------
  			// 20/12/2015 ECU and delete this activity
  			// ---------------------------------------------------------------------
  			((Activity)AppointmentsActivity.context).finish ();
  			// ---------------------------------------------------------------------
  		}
  		// -------------------------------------------------------------------------
  	}
  	// =============================================================================
}

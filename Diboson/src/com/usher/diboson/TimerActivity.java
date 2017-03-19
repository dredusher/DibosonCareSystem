package com.usher.diboson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TimePicker;

public class TimerActivity extends DibosonActivity 
{
	// ===============================================================================
	// 18/06/2013 ECU created
	// 04/12/2013 ECU removed the Initialise method of AlarmData and used the
	//                constructor to pass across the data
	//            ECU added option menu to display the currently stored alarms. This
	//                required adding the method DisplayCurrentAlarms
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	// 03/12/2013 ECU Putting the time in using the keyboard caused the alarm to be
	// 				  actioned immediately however using the picker worked fine. 
	//                Investigation showed that needed to call the 'clearFocus()'
	//                method for the picker in order to get the keyboard value.
	// 04/12/2013 ECU the mods seemed to check out OK
	// 28/11/2014 ECU added 'activity' processing
	// 08/02/2015 ECU changed all references of MainActivity.requiredAlarmAction and
	//                MainActivity.requiredAlarmID to the local action and alarmID
	// 05/06/2015 ECU took out alarmManager and general tidy up
	// 14/07/2015 ECU added the time email message
	// 09/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 26/01/2017 ECU remove 'menu' handling
	//================================================================================
	/* =============================================================================== */
	//private static final String 	TAG					  	  = "TimerActivity";
	/* =============================================================================== */
	//private static final int 	 MEDICATION_RESULT_CODE  = 4455;
	/* =============================================================================== */
	private         int			 action = StaticData.ALARM_ACTION_NONE;
																// 08/02/2015 ECU added
																// 11/02/2015 ECU set
	private 		CheckBox 	 activityCheckBox;				// 28/11/2014 ECU added
	private			int			 alarmIndex = StaticData.NO_RESULT;
																// 07/02/2015 ECU added
	private static	int			 associatedData = StaticData.NO_RESULT;
																// 08/03/2015 ECU changed to static 
	private static  Context		 context;						// 09/03/2016 ECU added
	private			CheckBox     emailMessageCheckBox;			// 14/07/2015 ECU added
	private         String		 message = null;				// 09/02/2015 ECU added
	private			CheckBox     messageCheckBox;				// 09/02/2015 ECU added
	private			Calendar     myCalendar;
	private 		DatePicker   myDatePicker;
	private 		TimePicker   myTimePicker;
	private			CheckBox     phoneCheckBox;
	private	static	int 		 requestCode	=	0;			// 06/02/2015 ECU added		
	private			CheckBox     slideshowCheckBox;
	private			CheckBox     tabletCheckBox;
	private static  Object       timerObject = null;			// 09/03/2016 ECU added
	// =============================================================================

	/* ============================================================================= */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// -------------------------------------------------------------------------
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 16/02/2014 ECU set up common activity features
			// 08/04/2014 ECU changed to use the variable
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity(this,StaticData.ACTIVITY_FULL_SCREEN);

			setContentView (R.layout.activity_timer);
			// ---------------------------------------------------------------------
			// 09/03/2016 ECU remember the context for later use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 07/02/2015 ECU check if any parameters have fed through
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();		
			if(extras != null) 
			{
				// -----------------------------------------------------------------
				// 07/02/2015 ECU get the index of the alarm that is to be edited
				// -----------------------------------------------------------------
				alarmIndex = extras.getInt(StaticData.PARAMETER_TIMER,StaticData.NO_RESULT); 	
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 18/06/2013 ECU set up the pointers to the various displayed fields
			// ---------------------------------------------------------------------
			((Button) findViewById (R.id.alarmButton)).setOnClickListener (buttonSetAlarm);
			// ---------------------------------------------------------------------
			// 28/11/2014 ECU added the 'activity' stuff
			// 09/02/2015 ECU added the message check box
			// 14/07/2015 ECU added the email message check box
			// ---------------------------------------------------------------------
			activityCheckBox 		= (CheckBox) findViewById (R.id.alarmCheckBox4);
			emailMessageCheckBox	= (CheckBox) findViewById (R.id.alarmCheckBox6);
			messageCheckBox			= (CheckBox) findViewById (R.id.alarmCheckBox5);
			phoneCheckBox 			= (CheckBox) findViewById (R.id.alarmCheckBox1);
			slideshowCheckBox 		= (CheckBox) findViewById (R.id.alarmCheckBox3);
			tabletCheckBox 			= (CheckBox) findViewById (R.id.alarmCheckBox2);
			// ---------------------------------------------------------------------
			// 09/02/2015 ECU declare the click listeners
			// ---------------------------------------------------------------------
			activityCheckBox.setOnClickListener (checkBoxAlarm);
			emailMessageCheckBox.setOnClickListener (checkBoxAlarm);
			messageCheckBox.setOnClickListener (checkBoxAlarm);
			phoneCheckBox.setOnClickListener (checkBoxAlarm);
			slideshowCheckBox.setOnClickListener (checkBoxAlarm);
			tabletCheckBox.setOnClickListener (checkBoxAlarm);
			// ---------------------------------------------------------------------
			//          ECU indicate in the intent what this alarm is for 
			// ---------------------------------------------------------------------	
			myCalendar = Calendar.getInstance ();
			// ---------------------------------------------------------------------
			//          ECU pick up the date and time that has been entered 
			// ---------------------------------------------------------------------    
			myDatePicker = (DatePicker) findViewById (R.id.alarmDatePicker);
			myTimePicker = (TimePicker) findViewById (R.id.alarmTimePicker);
			// ---------------------------------------------------------------------
			// 07/02/2015 ECU indicate the use of a 24 hour clock
			// ---------------------------------------------------------------------
			myTimePicker.setIs24HourView(true);
			// ---------------------------------------------------------------------
			// 18/06/2013 ECU default to no action to take
			// ---------------------------------------------------------------------
			action = StaticData.ALARM_ACTION_NONE; 
			// ---------------------------------------------------------------------
			// 07/02/2015 ECU if in edit mode then set the display to the stored values
			// ---------------------------------------------------------------------
			if (alarmIndex != StaticData.NO_RESULT)
				displayTheTimer (PublicData.alarmData.get (alarmIndex));
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been recreated after having been
			//                destroyed by Android
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	private View.OnClickListener buttonSetAlarm = new View.OnClickListener () 
	{
		@Override
		public void onClick (View view) 
		{
			// ---------------------------------------------------------------------
			// 03/12/2013 ECU if the date or time is modified using the keyboard then
			//                the following commands makes sure that the modified
			//                values are picked up.
			// ---------------------------------------------------------------------
			myDatePicker.clearFocus ();
			myTimePicker.clearFocus ();
			// ---------------------------------------------------------------------
			// 03/12/2013 ECU set up a calendar structure using the input values
			// ---------------------------------------------------------------------
			myCalendar.set (myDatePicker.getYear (),
        		       		myDatePicker.getMonth (),
        		       		myDatePicker.getDayOfMonth (),
        		       		myTimePicker.getCurrentHour (),
        		       		myTimePicker.getCurrentMinute (),0);
			// ---------------------------------------------------------------------
			// 18/06/2013 ECU initialise the alarm data - use Calendar as the alarm ID
	        // 04/12/2013 ECU use the constructor rather than the old Initialise method
	        // 06/02/2015 ECU changed to use the new array list
	        //            ECU use the size as the requestCode in the alarm data
			//            ECU changed to use the local requestCode
			//            ECU include the activityIntent which is only set to non-null
			//                if the required action is ALARM_ACTION_ACTIVITY
			// 09/02/2015 ECU added the message
			// 14/07/2015 ECU added the email message
			// 09/03/2016 ECU changed to use the object rather than email message
	        // ---------------------------------------------------------------------
			AlarmData alarmData = new AlarmData (action,
												 myCalendar,
												 myCalendar.getTimeInMillis(),
												 requestCode++,
												 associatedData,
												 message,
												 timerObject);
			// ---------------------------------------------------------------------
			// 07/02/2015 ECU decide whether the alarm is to be added or the data is
			//                to replace an existing alarm
			// ---------------------------------------------------------------------
			if (alarmIndex == StaticData.NO_RESULT)
				PublicData.alarmData.add (alarmData);
			else
			{
				// -----------------------------------------------------------------
				// 08/02/2015 ECU an existing timer has been updated so make sure
				//                then any outstanding alarm is cancelled
				// 29/01/2017 ECU changed from 'deleteAlarm'
				// -----------------------------------------------------------------
				PublicData.alarmData.get (alarmIndex).cancelAlarm (getBaseContext ());
				// -----------------------------------------------------------------
				// 08/02/2015 ECU now update the alarm data
				// -----------------------------------------------------------------
				PublicData.alarmData.set (alarmIndex, alarmData);
			}
			// ---------------------------------------------------------------------
			// 18/06/2013 ECU use alarmCounter as a unique request code - currently not used
			// 06/02/2015 ECU changed to use size+1 as ID needs changing
	        //            ECU changed to use requestCode
			// 08/02/2015 ECU changed to use method embedded in the data
			// ---------------------------------------------------------------------
			alarmData.createAlarm (getBaseContext());
	        // ---------------------------------------------------------------------
	        // 04/12/2013 ECU display the input alarm as confirmation
	        // 06/02/2015 ECU changed to use local alarmData
			// 03/04/2016 ECU added the context
	        // ---------------------------------------------------------------------
	        Utilities.popToast ("Just to confirm the alarm details \n\n" + alarmData.Print(getBaseContext()));
	        // ---------------------------------------------------------------------
	        // 18/06/2013 ECU write the alarm data to disk
	        // 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
	        // 08/02/2015 ECU changed to use ...alarmFileName
	        // ---------------------------------------------------------------------
	        AsyncUtilities.writeObjectToDisk (PublicData.alarmFileName,PublicData.alarmData);
	        // ---------------------------------------------------------------------
	        // 18/06/2013 ECU just do this one action so finish this activity
	        // ---------------------------------------------------------------------
	        finish ();
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
		// 08/03/2016 ECU took out MEDICATION_RESULT_CODE
		// -------------------------------------------------------------------------
	    // -------------------------------------------------------------------------
	    // 28/11/2014 ECU added the 'activity' code
	    // -------------------------------------------------------------------------
	    if (theRequestCode == StaticData.RESULT_CODE_INTENT)
	    {
	    	 if (theResultCode == RESULT_OK) 
		     {     	
	    		 // ----------------------------------------------------------------
	    		 // 28/11/2014 ECU remember the intent that needs to be used
	    		 //                when starting the activity
	    		 // ----------------------------------------------------------------
	    		 associatedData   = theIntent.getIntExtra (StaticData.PARAMETER_POSITION,StaticData.NO_RESULT);
	    		 // ----------------------------------------------------------------   		
		     } 
		     else 
		     if (theResultCode == RESULT_CANCELED) 
		     {
		    	 // ----------------------------------------------------------------
		    	 // 28/11/2014 ECU added
		    	 // ----------------------------------------------------------------
		     }
	    }
	    // -------------------------------------------------------------------------
	    if (theRequestCode == StaticData.REQUEST_CODE_FILE)
	    {
	    	// ---------------------------------------------------------------------
	    	// 09/02/2015 ECU added
	    	// ---------------------------------------------------------------------
	    	if (theResultCode == RESULT_OK)
	    	{
	    		// -----------------------------------------------------------------
	    		// 09/02/2015 ECU store the received message
	    		// -----------------------------------------------------------------
	    		message   = theIntent.getStringExtra (StaticData.PARAMETER_MESSAGE);	
	    		// -----------------------------------------------------------------    		
	    	}
	    	else 
	 	    if (theResultCode == RESULT_CANCELED) 
	 	    {
	 	    	// -----------------------------------------------------------------
	 	    	// Handle cancel
	 	    	// -----------------------------------------------------------------
	 	    }
	    }
	    // -------------------------------------------------------------------------
	    // 14/07/2015 ECU check for the email message coming back
	    // -------------------------------------------------------------------------
	    if (theRequestCode == StaticData.REQUEST_CODE_EMAIL_MESSAGE)
	    {
	    	// ---------------------------------------------------------------------
	    	// 14/07/2015 ECU added
	    	// ---------------------------------------------------------------------
	    	if (theResultCode == RESULT_OK)
	    	{
	    		// -----------------------------------------------------------------
	    		// 14/07/2015 ECU store the received message
	    		// 09/03/2016 ECU changed to store in the object
	    		// -----------------------------------------------------------------
	    		timerObject   = theIntent.getSerializableExtra (StaticData.PARAMETER_EMAIL_MESSAGE);
	    		// -----------------------------------------------------------------    		
	    	}
	    	else 
	 	    if (theResultCode == RESULT_CANCELED) 
	 	    {
	 	       // Handle cancel
	 	    }
	    }
	}
	/* ============================================================================= */
	private View.OnClickListener checkBoxAlarm = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{
			// ---------------------------------------------------------------------
			// 11/02/2015 ECU changed the logic to correctly reflect the setting /
			//                resetting of items and to ensure that actions are
			//                not taken on items whose checkbox is not being changed
			// ---------------------------------------------------------------------
			// 11/02/2015 ECU took out the resetting of action
			// ---------------------------------------------------------------------
			//action = StaticData.ALARM_ACTION_NONE;
			// ---------------------------------------------------------------------
			// 18/06/2013 ECU add in the actions - more than one can occur
			// 11/02/2015 ECU check if already set and handle if already set
			// ---------------------------------------------------------------------
			if (phoneCheckBox.isChecked())
			{
				if (!checkAction (StaticData.ALARM_ACTION_PHONE_CALL))
					action += StaticData.ALARM_ACTION_PHONE_CALL;
			}
			else
			{
				if (checkAction (StaticData.ALARM_ACTION_PHONE_CALL))
					action -= StaticData.ALARM_ACTION_PHONE_CALL;	
			}
			// ---------------------------------------------------------------------
			if (tabletCheckBox.isChecked ())
			{
				if (!checkAction (StaticData.ALARM_ACTION_TABLET_REMINDER))
				{
					action += StaticData.ALARM_ACTION_TABLET_REMINDER;
					// -------------------------------------------------------------
					// 11/02/2015 ECU start the method to select the required
					//                medication
					// 07/03/2016 ECU user MEDICATION_TIMER
					// 08/03/2016 ECU changed to use the selector method
					// -------------------------------------------------------------
					SelectorUtilities.Initialise ();
					SelectorUtilities.selectorParameter.rowLayout 				= R.layout.selector_medication_row;
					SelectorUtilities.selectorParameter.type 					= StaticData.OBJECT_SELECTOR;
					SelectorUtilities.selectorParameter.customMethodDefinition 	= new MethodDefinition<TimerActivity> (TimerActivity.class,"SelectMedicationMethod");
					SelectorUtilities.selectorParameter.customLegend 			= getString (R.string.select);
					SelectorUtilities.selectorParameter.listItems				= MedicationDetails.BuildList();	
					SelectorUtilities.selectorParameter.newTask					= true;
					SelectorUtilities.StartSelector (getBaseContext(),StaticData.OBJECT_SELECTOR);
					// -------------------------------------------------------------
				}	
			}
			else
			{
				if (checkAction (StaticData.ALARM_ACTION_TABLET_REMINDER))
					action -= StaticData.ALARM_ACTION_TABLET_REMINDER;
				
			}
			// ---------------------------------------------------------------------
			if (slideshowCheckBox.isChecked ())
			{
				if (!checkAction (StaticData.ALARM_ACTION_SLIDESHOW))
					action += StaticData.ALARM_ACTION_SLIDESHOW;
			}
			else
			{
				if (checkAction (StaticData.ALARM_ACTION_SLIDESHOW))
					action -= StaticData.ALARM_ACTION_SLIDESHOW;
				
			}
			// ---------------------------------------------------------------------
			if (activityCheckBox.isChecked ())
			{
				// -----------------------------------------------------------------
				if (!checkAction (StaticData.ALARM_ACTION_ACTIVITY))
				{
					action += StaticData.ALARM_ACTION_ACTIVITY;
					// -------------------------------------------------------------
					// 02/04/2016 ECU ask the user to select the activity
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getBaseContext().getString (R.string.select_activity));
					// -------------------------------------------------------------
					// 28/11/2014 ECU ask the GridActivity to return the selected intent
					// -------------------------------------------------------------
					Intent intent = new Intent (getBaseContext(),GridActivity.class);
					intent.putExtra (StaticData.PARAMETER_INTENT,false);
					startActivityForResult (intent,StaticData.RESULT_CODE_INTENT);
					// -------------------------------------------------------------
				}
			}
			else
			{
				if (checkAction (StaticData.ALARM_ACTION_ACTIVITY))
					action -= StaticData.ALARM_ACTION_ACTIVITY;	
			}
			// ---------------------------------------------------------------------
			// 09/02/2015 ECU handle the message check box
			// ---------------------------------------------------------------------
			if (messageCheckBox.isChecked ())
			{
				if (!checkAction (StaticData.ALARM_ACTION_MESSAGE))
				{
					// -------------------------------------------------------------
					action += StaticData.ALARM_ACTION_MESSAGE;
					// -------------------------------------------------------------
					// 09/02/2015 ECU request the message
					// -------------------------------------------------------------
					Intent intent = new Intent (getBaseContext(),GetMessage.class);	
					startActivityForResult (intent,StaticData.REQUEST_CODE_FILE);
					// -------------------------------------------------------------
				}
			}
			else
			{
				if (checkAction (StaticData.ALARM_ACTION_MESSAGE))
					action -= StaticData.ALARM_ACTION_MESSAGE;	
			}
			// ---------------------------------------------------------------------
			// 14/07/2015 ECU handle the email message check box
			// ---------------------------------------------------------------------
			if (emailMessageCheckBox.isChecked ())
			{
				if (!checkAction (StaticData.ALARM_ACTION_EMAIL_MESSAGE))
				{
					// -------------------------------------------------------------
					action += StaticData.ALARM_ACTION_EMAIL_MESSAGE;
					// -------------------------------------------------------------
					// 14/07/2015 ECU request the message
					// -------------------------------------------------------------
					Intent intent = new Intent (getBaseContext(),EMailActivity.class);	
					// -------------------------------------------------------------
					// 14/07/2015 ECU indicate that an email message is not to be sent
					//                when the activity 'finish' - es
					// --------------------------------------------------------------
					intent.putExtra (StaticData.PARAMETER_SEND_ON_EXIT,false);
					// --------------------------------------------------------------
					startActivityForResult (intent,StaticData.REQUEST_CODE_EMAIL_MESSAGE);
					// -------------------------------------------------------------
				}
			}
			else
			{
				if (checkAction (StaticData.ALARM_ACTION_EMAIL_MESSAGE))
						action -= StaticData.ALARM_ACTION_EMAIL_MESSAGE;	
			}
			// ---------------------------------------------------------------------
		}	
	};
	// =============================================================================
	public static void AlarmSelect (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 07/02/2015 ECU call this activity to edit the specified alarm, theIndex
		//                is the specifier
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (MainActivity.activity,TimerActivity.class);
		localIntent.putExtra (StaticData.PARAMETER_TIMER,theIndex);
		localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		MainActivity.activity.startActivity (localIntent);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	private boolean checkAction (int theAction)
	{
		// -------------------------------------------------------------------------
		// 11/02/2015 ECU checks whether the specified action has been set
		// -------------------------------------------------------------------------
		return ((action & theAction) == theAction);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private static void createAlarm (Context theContext,AlarmData theAlarmData)
	{
		// -------------------------------------------------------------------------
		// 08/02/2015 ECU changed to use the method embedded in the data
		// -------------------------------------------------------------------------
		theAlarmData.createAlarm (theContext);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@SuppressWarnings("unchecked")
	public static void actionStoredAlarms (Context theContext)
	{
		// ------------------------------------------------------------------------
		// 18/06/2013 ECU created
		// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
		// 06/02/2015 ECU change the format of the data read
		// 08/02/2015 ECU changed to use ....alarmFileName
		// -------------------------------------------------------------------------
		 PublicData.alarmData 
		    = (List<AlarmData>) AsyncUtilities.readObjectFromDisk (theContext,PublicData.alarmFileName);
		 // ------------------------------------------------------------------------
		 // 06/02/2015 ECU check if any data obtained
		 // ------------------------------------------------------------------------
		 if (PublicData.alarmData != null)
		 {
			 // --------------------------------------------------------------------
			 // 18/06/2013 ECU check the retrieved data
			 // 06/02/2015 ECU changed to use the new list array structure
			 // 03/04/2016 ECU added the context to Print
			 // --------------------------------------------------------------------
			 for (int alarmIndex = 0; alarmIndex < PublicData.alarmData.size(); alarmIndex++)
			 {
				 Utilities.popToast ("Alarm Number = " + alarmIndex + StaticData.NEWLINE + 
		 					 PublicData.alarmData.get (alarmIndex).Print (theContext)); 
				 // ----------------------------------------------------------------
				 // 04/12/2013 ECU create an appropriate alarm from the stored data
				 // ----------------------------------------------------------------
				 createAlarm (theContext,PublicData.alarmData.get(alarmIndex));
				 // ----------------------------------------------------------------
			 }
		 }
		 else
		 {
			 // --------------------------------------------------------------------
			 // 06/02/2015 ECU nothing on disk so just create an empty list
			 // --------------------------------------------------------------------
			 PublicData.alarmData = new ArrayList<AlarmData>();
		 }
	}
	// =============================================================================
	public static void DeleteTheTimer (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 08/02/2015 ECU before deleting the data make sure the alarm is cancelled
		// 29/01/2017 ECU changed from 'deleteAlarm'
		// -------------------------------------------------------------------------
		PublicData.alarmData.get (theIndex).cancelAlarm (MainActivity.activity);
		// -------------------------------------------------------------------------
		// 07/02/2015 ECU delete this timer
		// -------------------------------------------------------------------------
		PublicData.alarmData.remove (theIndex);
		// -------------------------------------------------------------------------
		// 08/02/2015 ECU make sure that the disk copy of the data is updated
		// -------------------------------------------------------------------------
		AsyncUtilities.writeObjectToDisk (PublicData.alarmFileName,PublicData.alarmData);
		// -------------------------------------------------------------------------
		// 08/02/2015 ECU just get the list rebuilt - not happy with this
		// -------------------------------------------------------------------------
		Selector.customListViewAdapter.RebuildList (AlarmData.BuildList());
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void DisplayCurrentAlarms ()
	{
		// -------------------------------------------------------------------------
		// 04/12/2013 ECU created to display the currently set alarms
		// 06/02/2015 ECU changed to use the new array list structure
		// -------------------------------------------------------------------------
		if (PublicData.alarmData.size() > 0)
		{
			for (int alarmIndex = 0; alarmIndex < PublicData.alarmData.size(); alarmIndex++)
			{
				// ---------------------------------------------------------------------
				// 04/12/2013 ECU use the normal context-free popToast
				// 03/04/2016 ECU added the context to Print
				// ---------------------------------------------------------------------		 
				Utilities.popToast ("Alarm Number = " + alarmIndex + StaticData.NEWLINE + 
									PublicData.alarmData.get (alarmIndex).Print (getBaseContext())); 
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 04/12/2013 ECU check if any alarms have been set
			// ---------------------------------------------------------------------
			Utilities.popToast ("There are no stored alarms");
		 }
	}
	// =============================================================================
	void displayTheTimer (AlarmData alarmData)
	{
		// -------------------------------------------------------------------------
		// 07/01/2014 ECU display date and time
		// -------------------------------------------------------------------------	
		myTimePicker.setCurrentHour  (alarmData.calendar.get (Calendar.HOUR_OF_DAY));
		myTimePicker.setCurrentMinute(alarmData.calendar.get (Calendar.MINUTE));
		
		myDatePicker.init (alarmData.calendar.get (Calendar.YEAR), 
						   alarmData.calendar.get (Calendar.MONTH), 
						   alarmData.calendar.get (Calendar.DAY_OF_MONTH),null);
		
		if ((alarmData.action & StaticData.ALARM_ACTION_PHONE_CALL) == StaticData.ALARM_ACTION_PHONE_CALL)
			phoneCheckBox.setChecked(true);
		if ((alarmData.action & StaticData.ALARM_ACTION_TABLET_REMINDER) == StaticData.ALARM_ACTION_TABLET_REMINDER)
			tabletCheckBox.setChecked(true);
		if ((alarmData.action & StaticData.ALARM_ACTION_SLIDESHOW) == StaticData.ALARM_ACTION_SLIDESHOW)
			slideshowCheckBox.setChecked(true);
		if ((alarmData.action & StaticData.ALARM_ACTION_ACTIVITY) == StaticData.ALARM_ACTION_ACTIVITY)
			activityCheckBox.setChecked(true);	
		// ------------------------------------------------------------------------
		// 08/02/2015 ECU there are a couple of variables that need to be set from
		//                the stored data
		// ------------------------------------------------------------------------
		action 				= alarmData.action;
		associatedData   	= alarmData.associatedData;
		// ------------------------------------------------------------------------
	}
	// =============================================================================
	public static void DoseAmountMethod (String theDoseAmount)
	{
		// -------------------------------------------------------------------------
		// 09/03/2016 ECU created to handle the chosen dose amount
		// -------------------------------------------------------------------------
		try
		{
			((DoseTime)timerObject).dose.amount =  Float.parseFloat (theDoseAmount);
			// ---------------------------------------------------------------------
			// 26/01/2017 ECU changed to use resources
			// ---------------------------------------------------------------------
			DialogueUtilities.textInput (context,
										 context.getString (R.string.dose_units),
										 context.getString (R.string.dose_units_request),
										 context.getString (R.string.tablets),
										 Utilities.createAMethod (TimerActivity.class,"DoseUnitsMethod",""),
										 null);
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{ 
			
		}
	}
	// =============================================================================
	public static void DoseNotesMethod (String theDoseNotes)
	{
		// -------------------------------------------------------------------------
		// 09/03/2016 ECU created to handle the chosen dose amount
		// -------------------------------------------------------------------------
		((DoseTime)timerObject).notes = theDoseNotes;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void DoseUnitsMethod (String theDoseUnits)
	{
		// -------------------------------------------------------------------------
		// 09/03/2016 ECU created to handle the chosen dose amount
		// -------------------------------------------------------------------------
		((DoseTime)timerObject).dose.units = theDoseUnits;
		// -------------------------------------------------------------------------
		// 26/01/2017 ECU changed to use resources
		// -------------------------------------------------------------------------
		DialogueUtilities.multilineTextInput (context,
											  context.getString (R.string.dose_notes),
											  context.getString (R.string.dose_notes_request),
											  3,
											  StaticData.HINT + context.getString (R.string.dose_notes_enter),
											  Utilities.createAMethod (TimerActivity.class,"DoseNotesMethod",""),
											  null);
	}
	// =============================================================================
	public static List<AlarmData> generateSummary (long theDate)
	{
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU created to return a summary of alarms for the specified
		//                date
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU adjust 'theDate' to have only the date bit
		// -------------------------------------------------------------------------
		theDate = theDate /StaticData.MILLISECONDS_PER_DAY;
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU declare a working list for appointments that match the date
		// -------------------------------------------------------------------------
		List<AlarmData> alarms = new ArrayList<AlarmData> ();
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU now loop through the stored appointments
		// -------------------------------------------------------------------------
		for (int alarm = 0; alarm < PublicData.alarmData.size(); alarm++)
		{
			// ---------------------------------------------------------------------
			// 28/02/2017 ECU check if this alarm is on the specified date
			// ---------------------------------------------------------------------
			if ((PublicData.alarmData.get(alarm).calendar.getTimeInMillis() / StaticData.MILLISECONDS_PER_DAY) == theDate)
			{
				// -----------------------------------------------------------------
				// 28/02/2017 ECU add this alarm into the list
				// -----------------------------------------------------------------
				alarms.add(PublicData.alarmData.get (alarm));
				// -----------------------------------------------------------------	
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU at this point any alarms for the specified date are
		//                held in the 'alarms' list
		// -------------------------------------------------------------------------
		return alarms;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    public static void SelectMedicationMethod (int theMedicationSelected)
    {
    	// -------------------------------------------------------------------------
    	// 08/03/2016 ECU created to handle the chosen medication
    	// -------------------------------------------------------------------------
    	associatedData = theMedicationSelected;
    	// -------------------------------------------------------------------------
    	// 08/03/2016 ECU close the selector dialogue
    	// -------------------------------------------------------------------------
    	Selector.Finish ();
    	// -------------------------------------------------------------------------
    	// 09/03/2016 ECU create the object that will receive the data
    	//			  ECU changed to use timer object
    	// -------------------------------------------------------------------------
    	timerObject         			= new DoseTime ();
    	((DoseTime) timerObject).dose 	= new Dose ();
    	// -------------------------------------------------------------------------
    	// 09/03/2016 ECU request the size of the dose
    	// 26/01/2017 ECU changed to use resources
    	// -------------------------------------------------------------------------
    	DialogueUtilities.textInput (context,
    								 context.getString (R.string.dose_amount),
    								 context.getString (R.string.dose_amount_request),
    								 "1",
    								 Utilities.createAMethod (TimerActivity.class,"DoseAmountMethod",""),
    								 null,
    								 InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
}


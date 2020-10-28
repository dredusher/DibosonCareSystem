package com.usher.diboson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.usher.diboson.utilities.SelectAnActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
	// 30/04/2017 ECU added handling of _ACTIONS
	// 17/05/2017 ECU added the 'repeat alarm' - use the current arguments to set a
	//                different time.
	//            ECU there used to be a 'private Calendar myCalendar' that was only
	//                used in 'onClick' - a problem was being caused with today's 
	//                changes so a fresh instance of the Calendar has been created 
	//                in 'onClick'
	// 23/09/2017 ECU when staring an activity then specify its legend (which will be
	//                stored in the alarm's 'message') rather than its current position
	//                (which was stored in 'associatedData'). This change was necessary
	//                to accommodate when the 'sort by usage' option has been selected
	// 24/11/2017 ECU fix a fault to do with getting an 'index out of range' after
	//                editing a timer - cause because the alarmIndex was not being
	//                initialised
	// 27/11/2017 ECU try and use 'workingAlarmData' rather than individual variables
	//                because there were so many changes then not all changes have been
	//                commented
	//================================================================================
	// =============================================================================== 
	private static final String 	TAG					  	  = "TimerActivity";
	// =============================================================================== 
	
	// =============================================================================== 
	private			CheckBox     actionsCheckBox;				// 30/04/2017 ECU added
	private 		CheckBox 	 activityCheckBox;				// 28/11/2014 ECU added
	private static	int			 alarmIndex;					// 07/02/2015 ECU added
	private static  Context		 context;						// 09/03/2016 ECU added
	private 		DatePicker   datePicker;
	private			CheckBox     emailMessageCheckBox;			// 14/07/2015 ECU added
	private			CheckBox     messageCheckBox;				// 09/02/2015 ECU added
	private			CheckBox     phoneCheckBox;
	private			CheckBox     slideshowCheckBox;
	private			CheckBox     tabletCheckBox;
	private 		TimePicker   timePicker;
	        static	AlarmData	 workingAlarmData;				// 27/11/2017 ECU added
	// -----------------------------------------------------------------------------
	// 26/11/2017 ECU Note - the use of static is so that 'requestCode' is not reset
	//                       on subsequent calls to this activity
	// -----------------------------------------------------------------------------
	private	static	int 		 requestCode;					// 06/02/2015 ECU added		
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
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);

			setContentView (R.layout.activity_timer);
			// ---------------------------------------------------------------------
			// 09/03/2016 ECU remember the context for later use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 30/04/2017 ECU preset any of the static variables
			// 24/11/2017 ECU default the alarmIndex which is static
			// ---------------------------------------------------------------------
			alarmIndex		= StaticData.NO_RESULT;
			requestCode		= 0;
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
			// 27/11/2017 ECU decide whether the working alarm data is to be a fresh
			//                object or a copy of what has currently been stored
			// ---------------------------------------------------------------------
			if (alarmIndex == StaticData.NO_RESULT)
			{
				// -----------------------------------------------------------------
				// 27/11/2017 ECU set up the working alarm data
				// -----------------------------------------------------------------
				workingAlarmData = new AlarmData ();
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 27/11/2017 ECU copy across the item currently in the list
				//                do it via the 'clone' otherwise only a pointer will
				//                be copied
				// -----------------------------------------------------------------
				workingAlarmData = PublicData.alarmData.get (alarmIndex).Clone ();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 17/05/2017 ECU set up two line text to give information
			// ---------------------------------------------------------------------
			((Button) findViewById (R.id.alarmRepeatButton)).setText (Utilities.twoLineButtonLegend (this, 
						getString (R.string.set_another_alarm_top_line),getString (R.string.set_another_alarm_bottom_line)));
			// ---------------------------------------------------------------------
			// 18/06/2013 ECU set up the pointers to the various displayed fields
			// 17/05/2017 ECU added the 'repeat' button
			// 18/05/2017 ECU added the 'repeated' button
			// ---------------------------------------------------------------------
			((Button) findViewById (R.id.alarmButton)).setOnClickListener (buttonSetAlarm);
			((Button) findViewById (R.id.alarmRepeatButton)).setOnClickListener (buttonSetAlarm);
			((Button) findViewById (R.id.alarmRepeatedButton)).setOnClickListener (buttonSetAlarm);
			// ---------------------------------------------------------------------
			// 28/11/2014 ECU added the 'activity' stuff
			// 09/02/2015 ECU added the message check box
			// 14/07/2015 ECU added the email message check box
			// 30/04/2017 ECU added 'actions' check box
			// ---------------------------------------------------------------------
			activityCheckBox 		= (CheckBox) findViewById (R.id.alarmCheckBox4);
			actionsCheckBox 		= (CheckBox) findViewById (R.id.alarmCheckBox_actions);
			emailMessageCheckBox	= (CheckBox) findViewById (R.id.alarmCheckBox6);
			messageCheckBox			= (CheckBox) findViewById (R.id.alarmCheckBox5);
			phoneCheckBox 			= (CheckBox) findViewById (R.id.alarmCheckBox1);
			slideshowCheckBox 		= (CheckBox) findViewById (R.id.alarmCheckBox3);
			tabletCheckBox 			= (CheckBox) findViewById (R.id.alarmCheckBox2);
			// ---------------------------------------------------------------------
			// 09/02/2015 ECU declare the click listeners
			// ---------------------------------------------------------------------
			activityCheckBox.setOnClickListener    	(checkBoxAlarm);
			actionsCheckBox.setOnClickListener      (checkBoxAlarm);
			emailMessageCheckBox.setOnClickListener (checkBoxAlarm);
			messageCheckBox.setOnClickListener 		(checkBoxAlarm);
			phoneCheckBox.setOnClickListener 		(checkBoxAlarm);
			slideshowCheckBox.setOnClickListener 	(checkBoxAlarm);
			tabletCheckBox.setOnClickListener 		(checkBoxAlarm);
			// ---------------------------------------------------------------------
			//          ECU pick up the date and time that has been entered 
			// ---------------------------------------------------------------------    
			datePicker = (DatePicker) findViewById (R.id.alarmDatePicker);
			timePicker = (TimePicker) findViewById (R.id.alarmTimePicker);
			// ---------------------------------------------------------------------
			// 21/07/2020 ECU try and scale the size of the pickers - scale to 75 %
			// ---------------------------------------------------------------------

			// ---------------------------------------------------------------------
			// 07/02/2015 ECU indicate the use of a 24 hour clock
			// ---------------------------------------------------------------------
			timePicker.setIs24HourView (true);
			// ---------------------------------------------------------------------
			// 07/02/2015 ECU if in edit mode then set the display to the stored values
			// 26/11/2017 ECU changed to use 'work..' rather than 'PublicData.alarm...'
			// ---------------------------------------------------------------------
			if (alarmIndex != StaticData.NO_RESULT)
				displayTheTimer (workingAlarmData);
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
			//20/07/2020 ECU check if any actions have been defined
			// ---------------------------------------------------------------------
			if (workingAlarmData.action == 0)
			{
				// -----------------------------------------------------------------
				// 20/07/2020 ECU tell the user that no timer will be set
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (getString (R.string.timer_no_actions),true);
				// -----------------------------------------------------------------
				// 20/07/2020 ECU take no further actions
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 03/12/2013 ECU if the date or time is modified using the keyboard then
				//                the following commands makes sure that the modified
				//                values are picked up.
				// 17/05/2017 ECU changed to handle the additional button which allows
				//                additional alarms with the same parameters but a
				//                different date/time
				// -----------------------------------------------------------------
				datePicker.clearFocus ();
				timePicker.clearFocus ();
				// -----------------------------------------------------------------
				// 17/05/2017 ECU get a fresh instance for setting date because when
				//                creating another alarm with same parameters but different
				//                date/time then '....getTimeInMillis ()' was remembering
				//                the first instance of Calendar
				//            ECU removed 'myCalendar' that was declared as private at
				//                top of class
				// -----------------------------------------------------------------
				Calendar localCalendar = Calendar.getInstance ();
				// -----------------------------------------------------------------
				// 03/12/2013 ECU set up a calendar structure using the input values
				// -----------------------------------------------------------------
				localCalendar.set (datePicker.getYear (),
        			       		   datePicker.getMonth (),
        		    	   		   datePicker.getDayOfMonth (),
        		       			   timePicker.getCurrentHour (),
        		       			   timePicker.getCurrentMinute (),0);
				// -----------------------------------------------------------------
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
				// 30/04/2017 ECU added 'actions' as an argument
				// 17/05/2017 ECU changed to use localCalendar
				// 18/05/2017 ECU changed 'alarmData' to be 'class wide' rather than
				//                being declared here
	        	// -----------------------------------------------------------------
				workingAlarmData.calendar 		= localCalendar;
				workingAlarmData.id				= localCalendar.getTimeInMillis();
				workingAlarmData.requestCode 	= requestCode++;
				// -----------------------------------------------------------------
				// 18/05/2017 ECU decide how the acquired data alarm data is to be handled
				// -----------------------------------------------------------------
				switch (view.getId ())
				{
					// -------------------------------------------------------------
					case R.id.alarmButton:
						// ---------------------------------------------------------
						// 18/05/2017 ECU create a single alarm and then finish the
						//                activity
						// ---------------------------------------------------------
						// 06/10/2020 ECU ensure that this is not a repeat alarm
						//                - an issue if an existing alarm is being
						//                edited
						// ---------------------------------------------------------
						workingAlarmData.repeatInterval = StaticData.NO_RESULT;
						// ----------------------------------------------------------
						storeTheAlarmData ();
						// ---------------------------------------------------------
						// 20/03/2019 ECU Note - close this activity
						// ---------------------------------------------------------
						finish ();
						// ---------------------------------------------------------
						break;
						// ---------------------------------------------------------
					// -------------------------------------------------------------
					case R.id.alarmRepeatButton:
						// ---------------------------------------------------------
						// 18/05/2017 ECU define an alarm from the existing data and give
						//                the ability to define another alarm with the
						//                same data but different date/time
						// ---------------------------------------------------------
						storeTheAlarmData ();
						// ---------------------------------------------------------
						// 27/11/2017 ECU want to clone a new copy
						// ---------------------------------------------------------
						workingAlarmData = workingAlarmData.Clone ();
						// ---------------------------------------------------------
						// 27/11/2017 ECU make sure 'edit mode' is switched off
						// ---------------------------------------------------------
						alarmIndex = StaticData.NO_RESULT;
						// ---------------------------------------------------------
						break;
						// ---------------------------------------------------------
					// -------------------------------------------------------------
					case R.id.alarmRepeatedButton:
						// ---------------------------------------------------------
		        		// 18/05/2017 ECU want to get the 'repeat' arguments
		        		// ---------------------------------------------------------
		        		getRepeatArguments (context);
		        		// ---------------------------------------------------------
		        		break;
		        		// ---------------------------------------------------------
					// -------------------------------------------------------------
				}
	        	// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
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
	    // 24/07/2020 ECU remove the code now that a dialogue is being used
	    // -------------------------------------------------------------------------
	    // 24/07/2020 ECU if (theRequestCode == StaticData.RESULT_CODE_INTENT)
		// 24/07/2020 ECU {
		// 24/07/2020 ECU 	 if (theResultCode == RESULT_OK)
		// 24/07/2020 ECU      {
		// 24/07/2020 ECU 		 // ----------------------------------------------------------------
		// 24/07/2020 ECU 		 // 28/11/2014 ECU remember the intent that needs to be used
		// 24/07/2020 ECU 		 //                when starting the activity
		// 24/07/2020 ECU 		 // 23/09/2017 ECU changed to store the legend of the selected
		// 24/07/2020 ECU 		 //                activity rather than its current position which
		// 24/07/2020 ECU 		 //                could change if 'sort by usage' is in use
		// 24/07/2020 ECU 		 // ----------------------------------------------------------------
		// 24/07/2020 ECU 		 workingAlarmData.message = theIntent.getStringExtra (StaticData.PARAMETER_LEGEND);
		// 24/07/2020 ECU 		 // ----------------------------------------------------------------
		// 24/07/2020 ECU 	 }
		// 24/07/2020 ECU      else
		// 24/07/2020 ECU      if (theResultCode == RESULT_CANCELED)
		// 24/07/2020 ECU      {
		// 24/07/2020 ECU     	 // ----------------------------------------------------------------
		// 24/07/2020 ECU     	 // 28/11/2014 ECU added
		// 24/07/2020 ECU     	 // ----------------------------------------------------------------
		// 24/07/2020 ECU      }
		// 24/07/2020 ECU }
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
	    		workingAlarmData.message   = theIntent.getStringExtra (StaticData.PARAMETER_MESSAGE);	
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
	    		// 08/10/2020 ECU changed to use 'emailMessage'
	    		// -----------------------------------------------------------------
	    		workingAlarmData.emailMessage = (EmailMessage) theIntent.getSerializableExtra (StaticData.PARAMETER_EMAIL_MESSAGE);
	    		// -----------------------------------------------------------------    		
	    	}
	    	else 
	 	    if (theResultCode == RESULT_CANCELED) 
	 	    {
	 	    	// -----------------------------------------------------------------
	 	       // Handle cancel
	 	       // -------------------------------------------------------------------
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
			// 30/04/2017 ECU added the handling of 'actions'
			// 08/10/2020 ECU set associated objects to null on 'uncheck'
			// ---------------------------------------------------------------------
			if (phoneCheckBox.isChecked())
			{
				if (!checkAction (StaticData.ALARM_ACTION_PHONE_CALL))
				{
					workingAlarmData.action += StaticData.ALARM_ACTION_PHONE_CALL;
					// -------------------------------------------------------------
					// 06/10/2020 ECU now ask for the phone number
					// -------------------------------------------------------------
					DialogueUtilities.textInput (context,
							                     getString (R.string.phone_number),
							                     getString (R.string.enter_phone_number),
							                     StaticData.HINT + getString (R.string.type_in_phone_number),
							                     Utilities.createAMethod (TimerActivity.class,"PhoneNumberMethod",StaticData.BLANK_STRING),
							                     null,
							                     InputType.TYPE_CLASS_PHONE);
					// -------------------------------------------------------------
				}
			}
			else
			{
				if (checkAction (StaticData.ALARM_ACTION_PHONE_CALL))
				{
					workingAlarmData.action -= StaticData.ALARM_ACTION_PHONE_CALL;
					workingAlarmData.phoneNumber = null;
				}
			}
			// ---------------------------------------------------------------------
			if (tabletCheckBox.isChecked ())
			{
				if (!checkAction (StaticData.ALARM_ACTION_TABLET_REMINDER))
				{
					workingAlarmData.action += StaticData.ALARM_ACTION_TABLET_REMINDER;
					// -------------------------------------------------------------
					// 11/02/2015 ECU start the method to select the required
					//                medication
					// 07/03/2016 ECU user MEDICATION_TIMER
					// 08/03/2016 ECU changed to use the selector method
					// 25/01/2020 ECU changed to use help method
					// 06/10/2020 ECU removed the help method - do not know why it
					//                was added
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
				{
					workingAlarmData.action -= StaticData.ALARM_ACTION_TABLET_REMINDER;
					workingAlarmData.doseTime = null;
				}
			}
			// ---------------------------------------------------------------------
			if (slideshowCheckBox.isChecked ())
			{
				if (!checkAction (StaticData.ALARM_ACTION_SLIDESHOW))
					workingAlarmData.action += StaticData.ALARM_ACTION_SLIDESHOW;
			}
			else
			{
				if (checkAction (StaticData.ALARM_ACTION_SLIDESHOW))
					workingAlarmData.action -= StaticData.ALARM_ACTION_SLIDESHOW;
				
			}
			// ---------------------------------------------------------------------
			if (activityCheckBox.isChecked ())
			{
				// -----------------------------------------------------------------
				if (!checkAction (StaticData.ALARM_ACTION_ACTIVITY))
				{
					workingAlarmData.action += StaticData.ALARM_ACTION_ACTIVITY;
					// -------------------------------------------------------------
					// 02/04/2016 ECU ask the user to select the activity
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getBaseContext().getString (R.string.select_activity));
					// -------------------------------------------------------------
					// 28/11/2014 ECU ask the GridActivity to return the selected intent
					// 24/07/2020 ECU change to use the way the activity is selected
					// -------------------------------------------------------------
					// 24/07/2020 ECU Intent intent = new Intent (getBaseContext(),GridActivity.class);
					// 24/07/2020 ECU intent.putExtra (StaticData.PARAMETER_INTENT,false);
					// 24/07/2020 ECU startActivityForResult (intent,StaticData.RESULT_CODE_INTENT);
					// -------------------------------------------------------------
					// 24/07/2020 ECU select the required activity using a dialogue
					// -------------------------------------------------------------
					SelectAnActivity.ChooseAnActivity (context,
							Utilities.createAMethod (TimerActivity.class,"SelectedActivity",StaticData.BLANK_STRING));
					// -------------------------------------------------------------
				}
			}
			else
			{
				if (checkAction (StaticData.ALARM_ACTION_ACTIVITY))
					workingAlarmData.action -= StaticData.ALARM_ACTION_ACTIVITY;	
			}
			// ---------------------------------------------------------------------
			// 30/04/2017 ECU handle the 'actions' option
			// ---------------------------------------------------------------------
			if (actionsCheckBox.isChecked ())
			{
				// -----------------------------------------------------------------
				if (!checkAction (StaticData.ALARM_ACTION_ACTIONS))
				{
					workingAlarmData.action += StaticData.ALARM_ACTION_ACTIONS;
					// -------------------------------------------------------------
					// 30/04/2017 ECU start the wizard to define the actions
					// -------------------------------------------------------------
					ActionCommandUtilities.SelectCommand (context,
							Utilities.createAMethod (TimerActivity.class,"SetActionsMethod",StaticData.BLANK_STRING));
					// -------------------------------------------------------------
				}
			}
			else
			{
				if (checkAction (StaticData.ALARM_ACTION_ACTIONS))
					workingAlarmData.action -= StaticData.ALARM_ACTION_ACTIONS;	
			}
			// ---------------------------------------------------------------------
			// 09/02/2015 ECU handle the message check box
			// ---------------------------------------------------------------------
			if (messageCheckBox.isChecked ())
			{
				if (!checkAction (StaticData.ALARM_ACTION_MESSAGE))
				{
					// -------------------------------------------------------------
					workingAlarmData.action += StaticData.ALARM_ACTION_MESSAGE;
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
					workingAlarmData.action -= StaticData.ALARM_ACTION_MESSAGE;	
			}
			// ---------------------------------------------------------------------
			// 14/07/2015 ECU handle the email message check box
			// ---------------------------------------------------------------------
			if (emailMessageCheckBox.isChecked ())
			{
				if (!checkAction (StaticData.ALARM_ACTION_EMAIL_MESSAGE))
				{
					// -------------------------------------------------------------
					workingAlarmData.action += StaticData.ALARM_ACTION_EMAIL_MESSAGE;
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
				{
						workingAlarmData.action -= StaticData.ALARM_ACTION_EMAIL_MESSAGE;
						workingAlarmData.emailMessage = null;
				}
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
		return ((workingAlarmData.action & theAction) == theAction);
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
			 // 27/11/2017 ECU added 'true' to Print to get fuller print
			 // --------------------------------------------------------------------
			 for (int alarmIndex = 0; alarmIndex < PublicData.alarmData.size(); alarmIndex++)
			 {
				 Utilities.popToast ("Alarm Number = " + alarmIndex + StaticData.NEWLINE + 
		 					 PublicData.alarmData.get (alarmIndex).Print (theContext,true)); 
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
		// 20/07/2020 ECU check if all of the timers have been deleted
		// -------------------------------------------------------------------------
		if (PublicData.alarmData.size() >0)
		{
			// ---------------------------------------------------------------------
			// 08/02/2015 ECU just get the list rebuilt - not happy with this
			// ---------------------------------------------------------------------
			Selector.customListViewAdapter.RebuildList (AlarmData.BuildList());
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 20/07/2020 ECU all of the timers have been deleted
			// ----------------------------------------------------------------------
			Utilities.popToastAndSpeak (context.getString (R.string.timers_all_deleted));
			// ----------------------------------------------------------------------
			// 20/07/2020 ECU just terminate this activity
			// ----------------------------------------------------------------------
			Selector.Finish ();
			// ----------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void displayTheTimer (AlarmData alarmData)
	{
		// -------------------------------------------------------------------------
		// 07/01/2014 ECU display date and time
		// -------------------------------------------------------------------------	
		timePicker.setCurrentHour  (alarmData.calendar.get (Calendar.HOUR_OF_DAY));
		timePicker.setCurrentMinute(alarmData.calendar.get (Calendar.MINUTE));
		
		datePicker.init (alarmData.calendar.get (Calendar.YEAR), 
						 alarmData.calendar.get (Calendar.MONTH), 
						 alarmData.calendar.get (Calendar.DAY_OF_MONTH),null);
		// -------------------------------------------------------------------------
		// 24/11/2017 ECU Note - set the check boxes
		//			  ECU use the new method
		// -------------------------------------------------------------------------
		setCheckBox (actionsCheckBox, 		StaticData.ALARM_ACTION_ACTIONS);
		setCheckBox (activityCheckBox,		StaticData.ALARM_ACTION_ACTIVITY);	
		setCheckBox (messageCheckBox,		StaticData.ALARM_ACTION_MESSAGE);
		setCheckBox (emailMessageCheckBox,	StaticData.ALARM_ACTION_EMAIL_MESSAGE);
		setCheckBox (phoneCheckBox,			StaticData.ALARM_ACTION_PHONE_CALL);
		setCheckBox (slideshowCheckBox,		StaticData.ALARM_ACTION_SLIDESHOW);
		setCheckBox (tabletCheckBox,		StaticData.ALARM_ACTION_TABLET_REMINDER);
		// ------------------------------------------------------------------------
		// 08/02/2015 ECU there are a couple of variables that need to be set from
		//                the stored data
		// 25/11/2017 ECU added 'actions' and 'timerObject'
		// 27/11/2017 ECU remove move of the variables now that 'workingAlarmData'
		//                is used throughout
		// ------------------------------------------------------------------------
		requestCode	= alarmData.requestCode;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void DoseAmountMethod (String theDoseAmount)
	{
		// -------------------------------------------------------------------------
		// 09/03/2016 ECU created to handle the chosen dose amount
		// 08/10/2020 ECU changed to use 'doseTime'
		// -------------------------------------------------------------------------
		try
		{
			workingAlarmData.doseTime.dose.amount =  Float.parseFloat (theDoseAmount);
			// ---------------------------------------------------------------------
			// 26/01/2017 ECU changed to use resources
			// ---------------------------------------------------------------------
			DialogueUtilities.textInput (context,
										 context.getString (R.string.dose_units),
										 context.getString (R.string.dose_units_request),
										 context.getString (R.string.tablets),
										 Utilities.createAMethod (TimerActivity.class,"DoseUnitsMethod",StaticData.BLANK_STRING),
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
		// 08/10/2020 ECU changed to use 'doseTime'
		// -------------------------------------------------------------------------
		workingAlarmData.doseTime.notes = theDoseNotes;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void DoseUnitsMethod (String theDoseUnits)
	{
		// -------------------------------------------------------------------------
		// 09/03/2016 ECU created to handle the chosen dose amount
		// 08/10/2020 ECU changed to use 'doseTime'
		// -------------------------------------------------------------------------
		workingAlarmData.doseTime.dose.units = theDoseUnits;
		// -------------------------------------------------------------------------
		// 26/01/2017 ECU changed to use resources
		// -------------------------------------------------------------------------
		DialogueUtilities.multilineTextInput (context,
											  context.getString (R.string.dose_notes),
											  context.getString (R.string.dose_notes_request),
											  3,
											  StaticData.HINT + context.getString (R.string.dose_notes_enter),
											  Utilities.createAMethod (TimerActivity.class,"DoseNotesMethod",StaticData.BLANK_STRING),
											  null);
		// -------------------------------------------------------------------------
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
		theDate = theDate / StaticData.MILLISECONDS_PER_DAY;
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
				alarms.add (PublicData.alarmData.get (alarm));
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
	static void getRepeatArguments (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 18/05/2017 ECU created to initiate the request for the 'repeat' parameters
		// -------------------------------------------------------------------------
		// 18/05/2017 ECU request the interval between repeats
		// -------------------------------------------------------------------------
		DialogueUtilities.sliderChoice (theContext,
										theContext.getString (R.string.repeated_alarm_interval),
										theContext.getString (R.string.repeated_alarm_interval_summary),
										R.drawable.timer,
										null,
										1,
										1,
										60 * 24,
										theContext.getString (R.string.set_time),
										Utilities.createAMethod (TimerActivity.class,"RepeatIntervalMethod",0),
										theContext.getString (R.string.cancel_operation));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void PhoneNumberMethod (String thePhoneNumber)
	{
		// -------------------------------------------------------------------------
		// 08/03/2016 ECU created to handle the chosen medication
		// 08/10/2020 ECU changed to store the number in 'phoneNumber'
		// -------------------------------------------------------------------------
		workingAlarmData.phoneNumber = thePhoneNumber;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void RepeatIntervalMethod (int theInterval)
	{
		// -------------------------------------------------------------------------
		// 18/05/2017 ECU receive the entered 'interval' and then request the 'number
		//                of repeats'
		// -------------------------------------------------------------------------
		// 18/05/2017 ECU store the interval in the existing alarm data - remember
		//                that the argument is in minutes 
		// -------------------------------------------------------------------------
		workingAlarmData.repeatInterval = theInterval;
		// -------------------------------------------------------------------------
		// 18/05/2017 ECU now request the number of repeats that are required
		// -------------------------------------------------------------------------
		DialogueUtilities.sliderChoice (context,
										context.getString (R.string.repeated_alarm_number),
										context.getString (R.string.repeated_alarm_number_summary),
										R.drawable.number,
										null,
										1,
										1,
										100,
										context.getString (R.string.set_number),
										Utilities.createAMethod (TimerActivity.class,"RepeatNumberMethod",0),
										context.getString (R.string.cancel_operation));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void RepeatNumberMethod (int theNumber)
	{
		// -------------------------------------------------------------------------
		// 18/05/2017 ECU created to store the repeat number, update the record
		//                and then terminate this activity
		// -------------------------------------------------------------------------
		workingAlarmData.repeatNumber = theNumber;
		// -------------------------------------------------------------------------
		// 18/05/2017 ECU update the store alarm data record
		// -------------------------------------------------------------------------
		storeTheAlarmData ();
		// -------------------------------------------------------------------------
		// 18/05/2017 ECU terminate this activity
		// -------------------------------------------------------------------------
		((Activity)context).finish ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SelectedActivity (String theActivity)
	{
		// -------------------------------------------------------------------------
		// 24/07/2020 ECU store the activity
		// -------------------------------------------------------------------------
		workingAlarmData.message = theActivity;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    public static void SelectMedicationMethod (int theMedicationSelected)
    {
    	// -------------------------------------------------------------------------
    	// 08/03/2016 ECU created to handle the chosen medication
    	// -------------------------------------------------------------------------
    	workingAlarmData.associatedData = theMedicationSelected;
    	// -------------------------------------------------------------------------
    	// 08/03/2016 ECU close the selector dialogue
    	// -------------------------------------------------------------------------
    	Selector.Finish ();
    	// -------------------------------------------------------------------------
    	// 09/03/2016 ECU create the object that will receive the data
    	//			  ECU changed to use timer object
    	// 08/10/2020 ECU changed to use 'doseTime'
    	// -------------------------------------------------------------------------
    	workingAlarmData.doseTime       			= new DoseTime ();
    	workingAlarmData.doseTime.dose 	= new Dose ();
    	// -------------------------------------------------------------------------
    	// 09/03/2016 ECU request the size of the dose
    	// 26/01/2017 ECU changed to use resources
    	// -------------------------------------------------------------------------
    	DialogueUtilities.textInput (context,
    								 context.getString (R.string.dose_amount),
    								 context.getString (R.string.dose_amount_request),
    								 "1",
    								 Utilities.createAMethod (TimerActivity.class,"DoseAmountMethod",StaticData.BLANK_STRING),
    								 null,
    								 InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
 	public static void SetActionsMethod (String theActionCommands)
 	{
 		// -------------------------------------------------------------------------
 		// 30/04/2017 ECU set up the input actions against the alarm data
 		// -------------------------------------------------------------------------
 		workingAlarmData.actions = theActionCommands;
 		// -------------------------------------------------------------------------
 	}
 	// =============================================================================
 	void setCheckBox (CheckBox theCheckBox,int theAlarmType)
 	{
 		// -------------------------------------------------------------------------
 		// 24/11/2017 ECU created to set the checkbox if the particular alarm is set
 		// -------------------------------------------------------------------------
 		if ((workingAlarmData.action & theAlarmType) == theAlarmType)
			theCheckBox.setChecked (true);
 		// -------------------------------------------------------------------------
 	}
 	// =============================================================================
 	static void storeTheAlarmData ()
 	{
 		// -------------------------------------------------------------------------
 		// 18/05/2017 ECU created to store the created alarm
 		// -------------------------------------------------------------------------
 		if (alarmIndex == StaticData.NO_RESULT)
 		{
			PublicData.alarmData.add (workingAlarmData);
 		}
		else
		{
			// ---------------------------------------------------------------------
			// 08/02/2015 ECU an existing timer has been updated so make sure
			//                that any outstanding alarm is cancelled
			// 29/01/2017 ECU changed from 'deleteAlarm'
			// ---------------------------------------------------------------------
			PublicData.alarmData.get (alarmIndex).cancelAlarm (context);
			// ---------------------------------------------------------------------
			// 08/02/2015 ECU now update the alarm data
			// ---------------------------------------------------------------------
			PublicData.alarmData.set (alarmIndex,workingAlarmData);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 18/06/2013 ECU use alarmCounter as a unique request code - currently 
		//                not used
		// 06/02/2015 ECU changed to use size+1 as ID needs changing
        //            ECU changed to use requestCode
		// 08/02/2015 ECU changed to use method embedded in the data
		// -------------------------------------------------------------------------
		workingAlarmData.createAlarm (context);
		// -------------------------------------------------------------------------
		// 08/06/2017 ECU log details of the new alarm
		// 27/11/2017 ECU added 'true' to Print to get fuller print
		// -------------------------------------------------------------------------
		Utilities.LogToProjectFile (TAG,"SetAnAlarm " + " " + workingAlarmData.Print (context,true));
        // -------------------------------------------------------------------------
        // 04/12/2013 ECU display the input alarm as confirmation
        // 06/02/2015 ECU changed to use local alarmData
		// 03/04/2016 ECU added the context
		// 30/04/2017 ECU changed to use resource
		// 27/11/2017 ECU added 'true' to Print to get fuller print
        // -------------------------------------------------------------------------
        Utilities.popToast (context.getString (R.string.alarm_confirmation) + workingAlarmData.Print (context,true));
        // -------------------------------------------------------------------------
        // 18/06/2013 ECU write the alarm data to disk
        // 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
        // 08/02/2015 ECU changed to use ...alarmFileName
        // -------------------------------------------------------------------------
        AsyncUtilities.writeObjectToDisk (PublicData.alarmFileName,PublicData.alarmData);
        // -------------------------------------------------------------------------
 	}
 	// =============================================================================
}


package com.usher.diboson;

import java.lang.reflect.Method;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.Drawable;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

public class PanicAlarmActivity extends DibosonActivity 
{
	// =============================================================================
	// 25/11/2015 ECU created to handle 'panic alarm' tasks
	//            ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 26/11/2015 ECU added the acknowledge button
	// 29/11/2015 ECU moved the deviceAdminEnabled into PublicData
	// 01/02/2015 ECU started to add the use of alarm manager
	// 03/12/2015 ECU add security aspects
	// 10/12/2015 ECU when checking for shakes use variables in the object rather
	//                than the static items
	//            ECU put in wallpaper handling
	// 11/12/2015 ECU added the response prompts button
	// 26/12/2015 ECU handle PARAMETER_PANIC_INITIALISE to be called by MainActivity
	//                to re-instate the state of the panic alarm when the app was
	//                stopped
	// =============================================================================
	//private final static String TAG = "PanicAlarmActivity";
	// =============================================================================
	
	// =============================================================================
	// 21/02/2016 ECU declare the number of lines for use into the action commands
	//                field
	// 11/03/2016 ECU changed from '3'
	// -----------------------------------------------------------------------------
	private final static int	ACTION_PROMPT_LINES	= 5;
	// =============================================================================
	
	// =============================================================================
			Button		actionsButton;	
	static	PendingIntent
						alarmPendingIntent;
			ImageButton	clubsButton;
	static  ComponentName	
						componentName;
	static	Context		context;
			ImageButton	diamondsButton;
	static 	Drawable	existingWallPaper 	= null;
			Button		enableButton;
			Button		endTimeButton;
			ImageButton	heartsButton;
			boolean     initialise			= false;	// 26/12/2015 ECU added
			Button		intervalButton;
			boolean		promptMode;
	static	int			panicAlarmCounter;
	static 	PanicAlarmHandler 
						panicAlarmHandler 	= null;	
	static	RelativeLayout
						panicAlarmLayout;
	static	String		panicAlarmStatus 	= null;
	static	TextView	panicAlarmTimer;
			Button		promptsButton;
			Button		responseButton;
			Button		responseActionsButton;
			Button		securityButton;
			String		securityInput;
			boolean		securityPassed;
	static	TableRow	securityRow;
	static	int			securityTries;
	static	int 		shakeCount			= 0;
	static	long 		shakeLastTimestamp 	= 0;
	static	long 		shakeStartTimestamp = 0;
	static	long 		shakeTimestamp 		= 0;
			ImageButton	spadesButton;
			Button		startTimeButton;
	static 	DibosonTime	time 				= null;
	static 	boolean		timeSet 			= false;
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
			// 14/11/2016 ECU set up activity with full screen
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// ---------------------------------------------------------------------
			// 25/11/2015 ECU remember the context for future use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 26/11/2015 ECU default the setup mode
			// ---------------------------------------------------------------------
			promptMode = false;
			// ---------------------------------------------------------------------
			// 26/11/2015 ECU check whether any parameters have been supplied
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
			
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 26/11/2015 ECU check if being called to set prompt
				// -----------------------------------------------------------------
				promptMode = extras.getBoolean (StaticData.PARAMETER_PROMPT,false);
				// -----------------------------------------------------------------
				// 27/11/2015 ECU check if an immediate panic action is to be taken
				// -----------------------------------------------------------------
				if (extras.getBoolean (StaticData.PARAMETER_PANIC,false))
				{
					// -------------------------------------------------------------
					// 27/11/2015 ECU take the necessary actions
					// 26/12/2015 ECU put in the check to ensure that the panic
					//                alarm has been defined
					// -------------------------------------------------------------
					if (PublicData.storedData.panicAlarm != null)
					{
						// ---------------------------------------------------------
						// 26/12/2015 ECU the alarm has been defined so action its
						//                contents
						// ---------------------------------------------------------
						Actions (this);
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 26/12/2015 ECU the alarm hasn't been set up yet
						// 26/03/2016 ECU changed to use the resource
						// ---------------------------------------------------------
						Utilities.popToast (getString (R.string.panic_alarm_not_defined));
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 27/11/2015 ECU terminate the activity
					// -------------------------------------------------------------
					finish ();
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 26/12/2015 ECU check if activity being called to initialise at the
				//                app startup - this should re-instate the panic
				//                alarm to the state it was in when the app was
				//                previously stopped
				// -----------------------------------------------------------------
				initialise = extras.getBoolean (StaticData.PARAMETER_PANIC_INITIALISE,false);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 25/11/2015 ECU just in case the stored data is not set then preset
			//                the panic alarm data
			// ---------------------------------------------------------------------
			if (PublicData.storedData.panicAlarm == null)
				PublicData.storedData.panicAlarm = new PanicAlarm ();
			// ---------------------------------------------------------------------
			// 26/11/2015 ECU now decide which mode we are in
			// ---------------------------------------------------------------------
			if (!promptMode)
			{
				if (!initialise)
				{
					// -------------------------------------------------------------
					// 26/11/2015 ECU in setup mode
					// -------------------------------------------------------------
					// 25/11/2015 ECU the activity has been created anew
					// -------------------------------------------------------------
					setContentView (R.layout.activity_panic_alarm);
					// -------------------------------------------------------------
					// 25/11/2015 ECU request the start time for the panic alarm period
					// -------------------------------------------------------------
					// 25/11/2015 ECU set up the buttons
					// 27/11/2015 ECU add 'prompts' button
					// 03/12/2015 ECU add 'security' button
					// -----------------------------------------------------------------
					actionsButton 			= (Button) findViewById (R.id.panic_alarm_actions_button);
					enableButton 			= (Button) findViewById (R.id.panic_alarm_enable_button);
					endTimeButton 			= (Button) findViewById (R.id.panic_alarm_end_button);
					intervalButton			= (Button) findViewById (R.id.panic_alarm_interval_button);
					promptsButton			= (Button) findViewById (R.id.panic_alarm_prompts_button);
					responseButton 			= (Button) findViewById (R.id.panic_alarm_response_button);
					responseActionsButton	= (Button) findViewById (R.id.panic_alarm_response_actions_button);
					securityButton 			= (Button) findViewById (R.id.panic_alarm_security_button);
					startTimeButton 		= (Button) findViewById (R.id.panic_alarm_start_button);
					// -------------------------------------------------------------
					// 25/11/2015 ECU set up the listeners for the buttons
					// -------------------------------------------------------------
					actionsButton.setOnClickListener (buttonListener);	
					enableButton.setOnClickListener (buttonListener);	
					endTimeButton.setOnClickListener (buttonListener);
					intervalButton.setOnClickListener (buttonListener);	
					promptsButton.setOnClickListener (buttonListener);	
					responseButton.setOnClickListener (buttonListener);	
					responseActionsButton.setOnClickListener (buttonListener);	
					securityButton.setOnClickListener (buttonListener);	
					startTimeButton.setOnClickListener (buttonListener);
					// -------------------------------------------------------------
					// 26/03/2016 ECU register the long click for check the action 
					//				  commands
					// -------------------------------------------------------------
					actionsButton.setOnLongClickListener (buttonListenerLong);
					promptsButton.setOnLongClickListener (buttonListenerLong);	
					responseActionsButton.setOnLongClickListener (buttonListenerLong);
					// -------------------------------------------------------------
					// 25/11/2015 ECU adjust any legends
					// -------------------------------------------------------------
					enableButton.setText (PublicData.storedData.panicAlarm.enabled ? R.string.panic_alarm_disable 
																				   : R.string.panic_alarm_enable);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 26/12/2015 ECU called up at app restart to start up the alarm
					// 27/12/2015 ECU Note - the Initialise method checks that the
					//                alarm is defined so no checking needed here
					// -------------------------------------------------------------
					// 14/03/2016 ECU check the status of the alarm
					// -------------------------------------------------------------
					if (!CheckAlarmState (this))
					{
						// ---------------------------------------------------------
						// 14/03/2016 ECU if the method returns 'false' then further
						//                processing of the panic alarm data is
						//                required
						// ---------------------------------------------------------
						EnablePanicAlarm (this);
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 14/03/2016 ECU now finish this activity
						// ---------------------------------------------------------
						finish ();
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
			}
			else
			{
				// -----------------------------------------------------------------
				// 28/11/2015 ECU set to full screen
				// -----------------------------------------------------------------
				Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
				// -----------------------------------------------------------------
				// 26/11/2015 ECU in prompt mode
				// -----------------------------------------------------------------
				setContentView (R.layout.activity_panic_alarm_prompt);
    			// -----------------------------------------------------------------
    			// 28/11/2015 ECU make sure the screen is on - moved here from
				//                the handler on receipt of the REQUEST_MESSAGE
				//                message.
    			// -----------------------------------------------------------------
    			screenLock (false);
    			// -----------------------------------------------------------------
    			// 03/12/2015 ECU check whether the security input is required -
    			//                by default it is 'gone'
    			// -----------------------------------------------------------------
    			if (PublicData.storedData.panicAlarm.security != null)
    			{
    				securityRow = (TableRow) findViewById (R.id.panic_alarm_table);
    				securityRow.setVisibility (View.VISIBLE);
				
    				// -------------------------------------------------------------
    				// 03/12/2015 ECU set up the security buttons
    				// -------------------------------------------------------------
    				clubsButton 		= (ImageButton) findViewById (R.id.panic_alarm_clubs);
    				diamondsButton 		= (ImageButton) findViewById (R.id.panic_alarm_diamonds);
    				heartsButton 		= (ImageButton) findViewById (R.id.panic_alarm_hearts);
    				spadesButton 		= (ImageButton) findViewById (R.id.panic_alarm_spades);
    				// -------------------------------------------------------------
    				// 25/11/2015 ECU set up the listeners for the buttons
    				// -------------------------------------------------------------
    				clubsButton.setOnClickListener (securityButtonListener);	
    				diamondsButton.setOnClickListener (securityButtonListener);
    				heartsButton.setOnClickListener (securityButtonListener);
    				spadesButton.setOnClickListener (securityButtonListener);
    				// -------------------------------------------------------------
    				// 03/12/2015 ECU indicate that security checking is required
    				// -------------------------------------------------------------
    				securityPassed = false;
    				// -------------------------------------------------------------
    				// 03/12/2015 ECU and clear the incoming string
    				// -------------------------------------------------------------
    				securityInput = "";
    				// -------------------------------------------------------------
    				securityTries = StaticData.PANIC_ALARM_MAX_TRIES;
    				// -------------------------------------------------------------
    			}
    			else
    			{
    				// -------------------------------------------------------------
    				// 03/12/2015 ECU no security is required so default to true
    				// -------------------------------------------------------------
    				securityPassed = true;
    				// -------------------------------------------------------------
    			}
				// -----------------------------------------------------------------
				// 26/11/2015 ECU set click listener for the background
				// -----------------------------------------------------------------
				panicAlarmLayout = (RelativeLayout) findViewById (R.id.panic_alarm_layout);
				panicAlarmLayout.setOnClickListener (buttonListener);
				// -----------------------------------------------------------------
				// 26/11/2015 ECU set up the timer display
				// -----------------------------------------------------------------
				panicAlarmTimer = (TextView) findViewById (R.id.panic_alarm_timer);
				// -----------------------------------------------------------------
				// 27/11/2015 ECU action the alarm to prompt for a user response
				// 11/12/2015 ECU rename prompts to promptCommands
				// -----------------------------------------------------------------
				Utilities.actionHandler (this,PublicData.storedData.panicAlarm.promptCommands);
				// -----------------------------------------------------------------
				// 26/11/2015 ECU indicate that user has not responded yet
				// -----------------------------------------------------------------
				PublicData.storedData.panicAlarm.userResponse = false;
				// -----------------------------------------------------------------
				// 26/11/2015 ECU set up the handler
				// -----------------------------------------------------------------
				panicAlarmHandler = new PanicAlarmHandler ();
				// -----------------------------------------------------------------
				// 26/11/2015 ECU now set up the timer for the user response
				// -----------------------------------------------------------------
				panicAlarmHandler.sendEmptyMessage (StaticData.MESSAGE_REQUEST);
				// -----------------------------------------------------------------	
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 25/11/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	 protected void onActivityResult (int requestCode, int resultCode, Intent data) 
	 {   
		 switch (requestCode) 
		 {   
		 	// ---------------------------------------------------------------------
		 	case StaticData.REQUEST_CODE_DEVICE_ADMIN:   
		 		if (resultCode == Activity.RESULT_OK) 
		 		{   
		 			// -------------------------------------------------------------
		 			// 28/11/2015 ECU indicate all is OK
		 			// -------------------------------------------------------------
		 			PublicData.deviceAdminEnabled = true;
		 			// -------------------------------------------------------------
		 		} 
		 		else 
		 		{   
		 			// -------------------------------------------------------------
		 			// 28/11/2015 ECU indicate failed to set permissions
		 			// -------------------------------------------------------------
		 			PublicData.deviceAdminEnabled = false;
		 			// -------------------------------------------------------------
			 		// 28/11/2015 ECU tell the user the result
		 			// 26/03/2016 ECU changed to use the resource
			 		// -------------------------------------------------------------
			 		Utilities.popToast (getString (R.string.admin_permission_failed));
		 		}  
		 		// -----------------------------------------------------------------
		 		// 26/12/2015 ECU decide if the activity is to be terminated
		 		// -----------------------------------------------------------------
		 		if (initialise)
		 			finish ();
		 		// -----------------------------------------------------------------
		 		return;   
		 } 
		 // ------------------------------------------------------------------------
		 super.onActivityResult(requestCode, resultCode, data);   
	}  
	// =============================================================================
	@Override
	public void onBackPressed ()
	{
		// -------------------------------------------------------------------------
		// 28/11/2015 ECU created to handle the BACK key. If in prompt mode then
		//                ignore the key.
		// -------------------------------------------------------------------------
		if (!promptMode)
		{
			// ---------------------------------------------------------------------
			// 28/11/2015 ECU set up mode so process
			// ---------------------------------------------------------------------
			super.onBackPressed();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    private View.OnClickListener buttonListener = new View.OnClickListener() 
	{
    	// -------------------------------------------------------------------------
		@Override
		public void onClick(View view) 
		{	
			//----------------------------------------------------------------------
			// 25/11/2015 ECU now process depending on which button pressed
			//----------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.panic_alarm_actions_button:
					// -------------------------------------------------------------
					// 27/11/2015 ECU added to handle the actions button
					// 11/12/2015 ECU rename to alarmCommands
					// 22/01/2016 ECU added the final true to indicate an action command
					// 23/01/2016 ECU changed to specify the help text
					// 21/02/2016 ECU changed number of lines from 5 to ACTION_PROMPT_LINES
					// -------------------------------------------------------------
					DialogueUtilities.multilineTextInput (context,
														  context.getString (R.string.panic_alarm_actions_title),
														  context.getString (R.string.action_command_summary),
														  ACTION_PROMPT_LINES,
														  PublicData.storedData.panicAlarm.alarmCommands,
														  Utilities.createAMethod (PanicAlarmActivity.class,"AlarmCommands",""),
														  null,
														  StaticData.NO_RESULT,
														  context.getString (R.string.press_to_define_command));
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.panic_alarm_enable_button:
					// -------------------------------------------------------------
					// 25/11/2015 ECU toggle the enable/disable flag and adjust the
					//                text
					// -------------------------------------------------------------
					PublicData.storedData.panicAlarm.enabled = !PublicData.storedData.panicAlarm.enabled;
					// -------------------------------------------------------------
					// 25/11/2015 ECU now adjust the legend
					// -------------------------------------------------------------
					enableButton.setText (PublicData.storedData.panicAlarm.enabled ? R.string.panic_alarm_disable 
																				   : R.string.panic_alarm_enable);
					// -------------------------------------------------------------
					// 25/11/2015 ECU if the alarm is being enabled then set the
					//                working variables
					// 26/11/2015 ECU changed to use DibosonTime
					// -------------------------------------------------------------
					PublicData.storedData.panicAlarm.active = false;
					// -------------------------------------------------------------
					// 28/11/2015 ECU device administration
					// -------------------------------------------------------------
					if (PublicData.storedData.panicAlarm.enabled)
					{
						// ---------------------------------------------------------
						// 28/11/2015 ECU request activation
						// 26/12/2015 ECU changed to use the method
						// ---------------------------------------------------------
						EnablePanicAlarm (context);
						// ---------------------------------------------------------
						// 04/12/2015 ECU finally decide if the 'shake' option is 
						//                required
						// 26/03/2016 ECU changed to use resources
						// ---------------------------------------------------------
						DialogueUtilities.yesNo (context,
												 context.getString (R.string.panic_alarm_shake),
												 context.getString (R.string.panic_alarm_shake_config),
												 null,
												 Utilities.createAMethod (PanicAlarmActivity.class,"ShakeYesMethod",(Object) null),
												 Utilities.createAMethod (PanicAlarmActivity.class,"ShakeNoMethod",(Object) null)); 
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 28/11/2015 ECU remove the device administrator activation
						// 25/03/2016 ECU make sure that the component name is set
						//                correctly
						// ---------------------------------------------------------
						componentName = new ComponentName (context,DeviceAdministrator.class);   
						((DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE)).removeActiveAdmin(componentName);
						// ---------------------------------------------------------
						// 02/12/2015 ECU want to cancel any outstanding alarms
						// ----------------------------------------------------------
						AlarmManager alarmManager = (AlarmManager) context.getSystemService (ALARM_SERVICE);
						alarmManager.cancel (alarmPendingIntent);
						// ---------------------------------------------------------
						// 05/12/2015 ECU if 'shake' is enabled then unregister the
						//                listener
						// ---------------------------------------------------------
						if (PublicData.storedData.panicAlarm.shake)
							accelerometerControl (false);
						// ---------------------------------------------------------
						// 10/12/2015 ECU make sure the wallpaper is restored
						// ---------------------------------------------------------
						setWallPaper (false);
						// ---------------------------------------------------------
						// 28/11/2015 ECU update the status information
						// ---------------------------------------------------------
						updateStatusMessage (null);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------						
					break;
				// -----------------------------------------------------------------
				case R.id.panic_alarm_end_button:
					// -------------------------------------------------------------
					// 25/11/2015 ECU set the end time for the active period
					// 26/03/2016 ECU changed to use the resource
					// -------------------------------------------------------------
					getATime (context,
							  getString (R.string.panic_alarm_end_time),
							  PublicData.storedData.panicAlarm.endTime,
							  Utilities.createAMethod (PanicAlarmActivity.class,"SetEndTime",(Object) null));
					// -------------------------------------------------------------	 
					break;
				// -----------------------------------------------------------------
				case R.id.panic_alarm_interval_button:
					// -------------------------------------------------------------
					// 25/11/2015 ECU set the time between prompts in minutes
					// 06/03/2016 add the '1' to be the minimum value
					// 10/03/2016 ECU changed from '60 * 10'
					// 26/03/2016 ECU changed to use resources
					// -------------------------------------------------------------
					DialogueUtilities.sliderChoice (context,
        											getString (R.string.select_the_prompt_interval),
        											getString (R.string.panic_alarm_prompt_interval),
        											R.drawable.timer,
        											null,
        											PublicData.storedData.panicAlarm.intervalTime,
        											1,
        											60 * 5,
        											getString (R.string.click_to_set_prompt_interval),
        											Utilities.createAMethod (PanicAlarmActivity.class,"IntervalTime",0),
        											getString (R.string.cancel_operation));
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.panic_alarm_layout:
					// -------------------------------------------------------------
					// 26/11/2015 ECU added to handle the acknowledge key
					// -------------------------------------------------------------
					// 03/12/2015 ECU added the security check
					// -------------------------------------------------------------
					if (securityPassed)
					{
						// ---------------------------------------------------------
						// 03/12/2015 ECU the security test has been passed so can
						//                action the click
						// ---------------------------------------------------------
						PublicData.storedData.panicAlarm.userResponse = true;
						// ---------------------------------------------------------
						TextToSpeechService.Flush ();
						// ---------------------------------------------------------
						// 26/11/2015 ECU display a message
						// 11/12/2015 ECU check if any actions are to be taken
						// ---------------------------------------------------------
						if (PublicData.storedData.panicAlarm.responseCommands == null)
						{
							// -----------------------------------------------------
							// 11/12/2015 ECU nothing has been defined so just use the
							//                default
							// 26/03/2016 ECU change to use the resource
							// -----------------------------------------------------
							Utilities.SpeakAPhrase (context,getString (R.string.glad_you_are_ok));
						}
						else
						{
							// -----------------------------------------------------
							// 11/12/2015 ECU action the defined commands
							// 06/06/2016 ECU add the final 'true' to indicate that
							//                if a list of actions is being processed
							//                then 'finish it immediately' before 
							//				  processing this list
							// -----------------------------------------------------
							Utilities.actionHandler (context,PublicData.storedData.panicAlarm.responseCommands,true);
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
						// 26/11/2015 ECU and finish this activity
						// ---------------------------------------------------------
						finish ();
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 03/12/2015 ECU the security check has not been passed yet
						// 26/03/2016 ECU changed to use the resource
						// ---------------------------------------------------------
						Utilities.popToastAndSpeak (getString (R.string.security_code_not_entered));
						// ---------------------------------------------------------
					}
					break;
				// -----------------------------------------------------------------
				case R.id.panic_alarm_prompts_button:
					// -------------------------------------------------------------
					// 27/11/2015 ECU added to handle the actions button
					// 22/01/2016 ECU added the final true to indicate an action command
					// 23/01/2016 ECU changed to specify the help text
					// 21/02/2016 ECU changed number of lines from 5 to  ACTION_PROMPT_LINES
					// -------------------------------------------------------------
					DialogueUtilities.multilineTextInput (context,
														  context.getString (R.string.panic_alarm_prompts_title),
													      context.getString (R.string.action_command_summary),
													      ACTION_PROMPT_LINES,
													      PublicData.storedData.panicAlarm.promptCommands,
													      Utilities.createAMethod (PanicAlarmActivity.class,"PromptCommands",""),
													      null,
													      StaticData.NO_RESULT,
													      context.getString (R.string.press_to_define_command));
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.panic_alarm_response_button:
					// -------------------------------------------------------------
					// 25/11/2015 ECU set the time in which the user must respond
					//                in seconds
					// 06/03/2016 ECU add the '10' as the minimum value
					// -------------------------------------------------------------
					DialogueUtilities.sliderChoice (context,
        											getString (R.string.panic_alarm_response_title),
        											getString (R.string.panic_alarm_response_summary),
        											R.drawable.timer,
        											null,
        											PublicData.storedData.panicAlarm.responseTime,
        											10,
        											60 * 10,
        											getString (R.string.click_to_set_response_time),
        											Utilities.createAMethod (PanicAlarmActivity.class,"ResponseTime",0),
        											getString (R.string.cancel_operation));
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.panic_alarm_response_actions_button:
					// -------------------------------------------------------------
					// 11/12/2015 ECU created to get the actions associated with
					//                the response from the user
					// 22/01/2016 ECU added the final true to indicate an action command
					// 23/01/2016 ECU changed to specify the help text
					// 21/02/2016 ECU changed number of lines from 5 to ACTION_PROMPT_LINES
					// -------------------------------------------------------------
					DialogueUtilities.multilineTextInput (context,
														  context.getString (R.string.panic_alarm_response_actions_title),
														  context.getString (R.string.action_command_summary),
														  ACTION_PROMPT_LINES,
														  PublicData.storedData.panicAlarm.responseCommands,
														  Utilities.createAMethod (PanicAlarmActivity.class,"ResponseCommands",""),
														  null,
														  StaticData.NO_RESULT,
														  context.getString (R.string.press_to_define_command));
					// ------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.panic_alarm_security_button:
					// -------------------------------------------------------------
					// 03/12/2015 ECU set security string
					// 06/12/2015 ECU change to use SecurityInput
					// -------------------------------------------------------------
					DialogueUtilities.securityInput (context,
													 context.getString (R.string.panic_alarm_security_title),
													 PublicData.storedData.panicAlarm.security,
													 context.getString (R.string.set_security_code),
													 Utilities.createAMethod (PanicAlarmActivity.class,"ConfirmSecurityString",""),
													 context.getString (R.string.switch_off_security),
													 Utilities.createAMethod (PanicAlarmActivity.class,"CancelSecurityString",""));
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.panic_alarm_start_button:
					// -------------------------------------------------------------
					// 25/11/2015 ECU set the start time for the active period
					// -------------------------------------------------------------
					getATime (context,
							  context.getString(R.string.panic_alarm_start_time),
							  PublicData.storedData.panicAlarm.startTime,
							  Utilities.createAMethod (PanicAlarmActivity.class,"SetStartTime",(Object) null));
					// -------------------------------------------------------------	 
					break;
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	private View.OnLongClickListener buttonListenerLong = new View.OnLongClickListener() 
	{		
		@Override
		public boolean onLongClick(View view) 
		{	
			// ---------------------------------------------------------------------
			// 26/03/2016 ECU now process depending on which button pressed
			//            ECU added to enable the entered commands to be tested
			// ---------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.panic_alarm_actions_button:
				{
					// -------------------------------------------------------------
					// 26/03/2016 ECU action the 'alarm' commands
					// -------------------------------------------------------------
					Utilities.actionHandler (getBaseContext(),PublicData.storedData.panicAlarm.alarmCommands);
					// -------------------------------------------------------------
					break;
				}
				case R.id.panic_alarm_prompts_button:
				{
					// -------------------------------------------------------------
					// 26/03/2016 ECU action the 'prompt' commands
					// -------------------------------------------------------------
					Utilities.actionHandler (getBaseContext(),PublicData.storedData.panicAlarm.promptCommands);
					// -------------------------------------------------------------
					break;
				}	
				case R.id.panic_alarm_response_actions_button:
				{	
					// -------------------------------------------------------------
					// 26/03/2016 ECU action the 'response' commands
					// -------------------------------------------------------------
					Utilities.actionHandler (getBaseContext(),PublicData.storedData.panicAlarm.responseCommands);
					// -------------------------------------------------------------
					break;
				}		
			}
			// ---------------------------------------------------------------------
			return true;
		}
	};
	// =============================================================================
    private View.OnClickListener securityButtonListener = new View.OnClickListener() 
	{
    	// -------------------------------------------------------------------------
		@Override
		public void onClick(View view) 
		{	
			//----------------------------------------------------------------------
			// 25/11/2015 ECU now process depending on which button pressed
			//----------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.panic_alarm_clubs:
					// -------------------------------------------------------------
					// 03/12/2015 ECU handle 'clubs' button
					// -------------------------------------------------------------
					securityInput += StaticData.PANIC_ALARM_CLUBS;
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case R.id.panic_alarm_diamonds:
					// -------------------------------------------------------------
					// 03/12/2015 ECU handle 'clubs' button
					// -------------------------------------------------------------
					securityInput += StaticData.PANIC_ALARM_DIAMONDS;
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case R.id.panic_alarm_hearts:
					// -------------------------------------------------------------
					// 03/12/2015 ECU handle 'clubs' button
					// -------------------------------------------------------------
					securityInput += StaticData.PANIC_ALARM_HEARTS;;
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case R.id.panic_alarm_spades:
					// -------------------------------------------------------------
					// 03/12/2015 ECU handle 'spades' button
					// -------------------------------------------------------------
					securityInput += StaticData.PANIC_ALARM_SPADES;
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 03/12/2015 ECU check if correct string entered
			// ---------------------------------------------------------------------
			if (securityInput.length() == PublicData.storedData.panicAlarm.security.length())
			{
				// -----------------------------------------------------------------
				// 03/12/2015 ECU check if the strings match
				// -----------------------------------------------------------------
				if (PublicData.storedData.panicAlarm.security.equalsIgnoreCase(securityInput))
				{
					// --------------------------------------------------------------
					// 03/12/2015 ECU the security is OK so indicate the fact
					// --------------------------------------------------------------
					securityPassed = true;
					// --------------------------------------------------------------
					securityRow.setVisibility(View.GONE);
					// --------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 03/12/2015 ECU user has entered the wrong code
					// 26/03/2016 ECU change to use resource
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString (R.string.security_code_wrong),true);
					// -------------------------------------------------------------
					// 03/12/2015 ECU check the retries
					// -------------------------------------------------------------
					if (--securityTries > 0)
					{
						// ---------------------------------------------------------
						// 03/12/2015 ECU can still retry so reset the parameters
						// ---------------------------------------------------------
						securityInput = "";
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					else
					{
						// ---------------------------------------------------------
						// 03/12/2015 ECU exhausted the number of tries so reduce the timer
						//                to get an immediate alarm
						// ---------------------------------------------------------
						if (panicAlarmCounter > 3)
							panicAlarmCounter = 3;
						// ---------------------------------------------------------
					}
				}
			}
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	static void getATime (Context theContext,
						  final String theTitle,
						  final DibosonTime theTime,
						  final Method theSetMethod)
	{
		int		hour;
		int		minute;
		// -------------------------------------------------------------------------
		// 25/11/2015 ECU created to get a time
		// -------------------------------------------------------------------------
		if (theTime != null)
		{
			// ---------------------------------------------------------------------
			// 04/03/2014 ECU set to the actual time wanted
			// ---------------------------------------------------------------------		
			hour 	=   theTime.hour;
			minute	=	theTime.minute;
			// ---------------------------------------------------------------------
		}
		else
		{
			Calendar calendar = Calendar.getInstance();
			// ---------------------------------------------------------------------
			// 27/02/2015 ECU not editing an existing timer so display the current
			//                time
			// ---------------------------------------------------------------------
			hour	 = calendar.get (Calendar.HOUR_OF_DAY);
			minute	 = calendar.get (Calendar.MINUTE);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 01/03/2015 ECU indicate that the time has not been set yet
		// -------------------------------------------------------------------------
		timeSet	= false;
		// -------------------------------------------------------------------------
		TimePickerDialog timePicker;
		timePicker = new TimePickerDialog (theContext, new TimePickerDialog.OnTimeSetListener() 
		{
			@Override
			public void onTimeSet (TimePicker timePicker, int selectedHour, int selectedMinute) 
			{
				// -----------------------------------------------------------------
				// 08/03/2015 ECU for some reason on the Moto G (not Nexus or CnM)
				//                this was being called twice so put in the check on
				//                'timeSet' to prevent this
				// -----------------------------------------------------------------
				if (!timeSet)
				{
					time = new DibosonTime (selectedHour,selectedMinute);
					// -------------------------------------------------------------
					// 01/03/2015 ECU indicate that the time has been set
					// -------------------------------------------------------------
					timeSet = true;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
		}, hour, minute, true);
		// -------------------------------------------------------------------------
		// 01/03/2015 ECU try and intercept the cancel button
		// -------------------------------------------------------------------------
		timePicker.setOnDismissListener (new OnDismissListener() 
		{
			public void onDismiss (DialogInterface dialog) 
			{
				// -----------------------------------------------------------------
				// 01/03/2015 ECU exit 'edit mode'
				// 25/11/2015 ECU invoke the method to set the time
				// -----------------------------------------------------------------
				try 
				{
					theSetMethod.invoke (null, new Object [] {time});
				}
				catch (Exception theException) 
				{
				}
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		// 01/03/2015 ECU set the title and display the dialogue
		// -------------------------------------------------------------------------
		timePicker.setTitle (theTitle);
		timePicker.show();
	}
	// =============================================================================
	
	// =============================================================================
	static void accelerometerControl (boolean theActionFlag)
	{
		// -------------------------------------------------------------------------
		// 04/12/2015 ECU created to handle the enabling or disabling of the
		//                accelerometer and its listener
		//
		//				  theActionFlag = true    configure and register the
		//                                        listener
		//                                false   unregister the listener
		// -------------------------------------------------------------------------
		if (theActionFlag)
		{
			// ---------------------------------------------------------------------
			// 04/12/2015 ECU set up and register
			// 15/02/2016 ECU added the SENSOR... which is the rate that the sensor
			//                will supply data
			// ---------------------------------------------------------------------
			SensorService.accelerometerEnablement (true,
												   Utilities.createAMethod (PanicAlarmActivity.class,"accelerometerHandler",(Object) null),
												   SensorManager.SENSOR_DELAY_NORMAL);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 04/12/2015 ECU unregister
			// 15/02/2016 ECU added the final 0 (refers to data rate) but it is not
			//                used
			// ---------------------------------------------------------------------
			SensorService.accelerometerEnablement (false,null,0);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    public static void accelerometerHandler (Object theSensorEventAsObject)
    {
    	// -------------------------------------------------------------------------
    	// 18/11/2015 ECU created to be called by the SensorService to handle 
    	//                accelerometer events
    	// -------------------------------------------------------------------------
    	// 04/12/2015 ECU cast the argument to local event data
    	// -------------------------------------------------------------------------
    	SensorEvent localSensorEvent = (SensorEvent) theSensorEventAsObject;
    	// -------------------------------------------------------------------------
    	// 05/12/2015 ECU it is worth including some of the documentation so as to
    	//                understand the code the entries in the SensorEvent
    	//             
    	//						values[0]: Acceleration minus Gx on the x-axis 
    	//						values[1]: Acceleration minus Gy on the y-axis 
    	//						values[2]: Acceleration minus Gz on the z-axis 
    	//
    	//				  all values are m/s^2.
    	//
    	//				  A sensor of this type measures the acceleration applied to the 
    	//				  device (Ad). Conceptually, it does so by measuring forces applied
    	//				  to the sensor itself (Fs) using the relation: 
    	//
    	//						Ad = - (sum of) Fs / mass
    	//
    	//				  In particular, the force of gravity is always influencing the 
    	//                measured acceleration: 
    	//
    	//						Ad = -g - (sum of) F / mass
    	//
    	//				  For this reason, when the device is sitting on a table (and 
    	//				  obviously not accelerating), the accelerometer reads a 
    	//				  magnitude of g = 9.81 m/s^2 
    	// -------------------------------------------------------------------------
		// 04/12/2015 ECU gForce will be close to 1 when there is no movement - this
    	//                is because of the comment above namely acceleration at rest
    	//                will be 'g' - so dividing by 'g' gives '1' - here endeth
    	//                the mathematics lesson
		// -------------------------------------------------------------------------
		float gForce = ((float) Math.sqrt ((localSensorEvent.values [0] * localSensorEvent.values [0]) + 
										   (localSensorEvent.values [1] * localSensorEvent.values [1]) + 
										   (localSensorEvent.values [2] * localSensorEvent.values [2]))) / SensorManager.GRAVITY_EARTH;
		// -------------------------------------------------------------------------
		// 10/12/2015 ECU changed to use the threshold stored in the object rather
		//                than a set static
		// -------------------------------------------------------------------------
		if (gForce > PublicData.storedData.panicAlarm.shakeThreshold) 
		{
			// ---------------------------------------------------------------------
			// 05/12/2015 ECU get the current timestamp from the event
			// ---------------------------------------------------------------------
			shakeTimestamp = localSensorEvent.timestamp;
			// ---------------------------------------------------------------------
			// 05/12/2015 ECU check if this is the start of a 'shake sequence'
			// 			  ECU check if there is too long a gap between this
			//                shake and the previous one in which case start
			//                a new sequence with this latest shake
			// 10/12/2015 ECU changed to use the reset period stored in the object
			//                rather than a static value
			// ---------------------------------------------------------------------
			if (shakeCount == 0 || ((shakeTimestamp - shakeStartTimestamp) > PublicData.storedData.panicAlarm.shakeResetPeriod))
			{
				// -----------------------------------------------------------------
				// 05/12/2015 ECU am at the start of the sequence so store timestamp
				//                and increment the counter
				// -----------------------------------------------------------------
				shakeCount 			= 1;
				shakeStartTimestamp = shakeTimestamp;	
				shakeLastTimestamp 	= shakeTimestamp;
				// -----------------------------------------------------------------
			}	
			else
			{
				// -----------------------------------------------------------------
				// 05/12/2015 ECU just check if this reading is 'too quick' so
				//                shouldn't be regarded as a true movement
				// 10/12/2015 ECU changed to use the ignore period stored in the
				//                object rather than a static value
				// -----------------------------------------------------------------
				if ((shakeTimestamp - shakeLastTimestamp) > PublicData.storedData.panicAlarm.shakeIgnorePeriod)
				{
					// -------------------------------------------------------------
					// 05/12/2015 ECU movement should be considered
					// -------------------------------------------------------------
					shakeCount++;
					// -------------------------------------------------------------
					// 05/12/2015 ECU and remember this time stamp
					// -------------------------------------------------------------
					shakeLastTimestamp = shakeTimestamp;
					// -------------------------------------------------------------
					// 05/12/2015 ECU check if the number of shakes indicates
					//                the need to action the panic alarm
					// 10/12/2015 ECU changed to use the shake number held in the
					//                object rather than a static value
					// -------------------------------------------------------------
					if (shakeCount == PublicData.storedData.panicAlarm.shakeNumber)
					{
						// ---------------------------------------------------------
						// 05/12/2015 ECU it is time to action the panic alarm
						// ---------------------------------------------------------
						Actions (SensorService.context);
						// ---------------------------------------------------------
						// 05/12/2015 ECU was resetting 'shakeCount' to 0 here but
						//                if the user continued shaking then it could
						//                continue to generate the alarm
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
		}
    }
	// =============================================================================
	public static void Actions (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 26/11/2015 ECU created to handle actions when the user has problems
		// -------------------------------------------------------------------------
		// 27/11/2015 ECU action the stored messages
		// 06/06/2016 ECU add the final 'true' to indicate that
		//                if a list of actions is being processed
		//                then 'finish it immediately' before processing this 
		//                list
		// -------------------------------------------------------------------------
		Utilities.actionHandler (theContext,PublicData.storedData.panicAlarm.alarmCommands,true);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static boolean CheckAlarmState (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 14/03/2016 ECU called up on a restart to check what the correct state of
		//                the panic alarm should be
		//            ECU changed to boolean to reflect whether this process is
		//                doing all the work
		//                    true ............. this method handles everything
		//                    false ............ more processing wanted
		// -------------------------------------------------------------------------
		long startTime = PublicData.storedData.panicAlarm.startTime.MilliSeconds();
		long endTime   = PublicData.storedData.panicAlarm.endTime.MilliSeconds();
		// -------------------------------------------------------------------------
		// 14/03/2016 ECU check if the period spans midnight - if so then adjust
		// -------------------------------------------------------------------------
		if (startTime > endTime)
		{
			// ---------------------------------------------------------------------
			// 14/03/2016 ECU the period spans midnight so set the 'end' to the next
			//                day
			// ---------------------------------------------------------------------
			endTime += StaticData.MILLISECONDS_PER_DAY;
		}
		// -------------------------------------------------------------------------
		// 14/03/2016 ECU check if the current time is in the active period
		// -------------------------------------------------------------------------
		long currentTime = (Calendar.getInstance()).getTimeInMillis();
		// -------------------------------------------------------------------------
		// 14/03/2016 ECU now check the 'active' period
		// -------------------------------------------------------------------------
		if ((currentTime > startTime) && (currentTime <= endTime))
		{
			// ---------------------------------------------------------------------
			// 14/03/2016 ECU this is the active day so check if it needs to be
			//                activated
			// ---------------------------------------------------------------------
			if (!PublicData.storedData.panicAlarm.active)
			{
				// -----------------------------------------------------------------
				// 14/03/2016 ECU the panic alarm needs to be activated
				// -----------------------------------------------------------------
				handleActivate (theContext);
				// -----------------------------------------------------------------
				// 14/03/2016 ECU indicate that processing has been done
				// -----------------------------------------------------------------
				return true;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}	
		// -------------------------------------------------------------------------
		// 14/03/2016 ECU if the alarm is active then decide if a prompt is needed or
		//                if it is to deactivated
		// -------------------------------------------------------------------------
		if (PublicData.storedData.panicAlarm.active)
		{
			// ---------------------------------------------------------------------
			// 14/03/2016 ECU check if gone beyond the active period
			// ---------------------------------------------------------------------
			if (currentTime > endTime)
			{
				// -----------------------------------------------------------------
				// 14/03/2016 ECU the alarm should be deactivated
				// -----------------------------------------------------------------
				handleDeactivate (theContext);
				// -----------------------------------------------------------------
				// 14/03/2016 ECU indicate that processing has been done
				// -----------------------------------------------------------------
				return true;
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 14/03/2016 ECU now check which prompt is to be processed
				// -----------------------------------------------------------------
				long promptTime = startTime;
				// -----------------------------------------------------------------
				// 14/03/2016 ECU loop until the current time is less than the next 
				//                prompt time or the deactivation time is reached
				// 16/03/2016 ECU change the logic to look for the prompt which precedes
				//                the current time. 
				// -----------------------------------------------------------------
				while (promptTime < currentTime)
				{
					// -------------------------------------------------------------
					// 14/03/2016 ECU still before the current time so step to the next
					//                prompt time. The interval is stored in minutes
					//                but the calculation requires it in milliseconds
					// 16/03/2016 ECU use 'startTime' as a working variable to remember
					//                the prompt time before the increment
					// -------------------------------------------------------------
					startTime	= promptTime;
					promptTime += PublicData.storedData.panicAlarm.intervalTime * StaticData.MILLISECONDS_PER_MINUTE;
					// -------------------------------------------------------------
					// 14/03/2016 ECU it is possible that have moved outside of the active 
					//                period in which case the alarm will need to be 
					//                deactivated
					// -------------------------------------------------------------
					if (promptTime > endTime)
					{
						// ---------------------------------------------------------
						// 14/03/2016 ECU the alarm needs to deactivated
						// ---------------------------------------------------------
						PublicData.storedData.panicAlarm.nextPromptTime = PublicData.storedData.panicAlarm.endTime;
						// ---------------------------------------------------------
						// 16/03/2016 ECU get the prompt handled but without user
						//                input
						// ---------------------------------------------------------
						handlePrompt (theContext,false);
						// ---------------------------------------------------------
						return true;
						// ---------------------------------------------------------
					}
					else
					if (promptTime >= currentTime)
					{
						// ---------------------------------------------------------
						// 14/03/2016 ECU have reached the prompt that will next 
						//                need to be processed
						// 16/03/2016 ECU set the prompt time to that just prior to
						//                the current time. promptTime will be the
						//                one after the current time
						// ---------------------------------------------------------
						PublicData.storedData.panicAlarm.nextPromptTime = new DibosonTime (startTime);
						// ---------------------------------------------------------
						// 16/03/2016 ECU get the correct handler which should then
						//                set the correct 'next' prompt
						// ---------------------------------------------------------
						handlePrompt (theContext,true);
						// ---------------------------------------------------------
						// 16/03/2016 ECU indicate that this method has handled
						//                everything
						// ---------------------------------------------------------
						return true;
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 14/03/2016 ECU am skipping this particular prompt
						// ---------------------------------------------------------
					}
				}
			}
		}
		// -------------------------------------------------------------------------
		// 14/03/2016 ECU indicate further processing wanted
		// -------------------------------------------------------------------------
		return false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void EnablePanicAlarm (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 26/12/2015 ECU created to set the functions required when the
		//                alarm is enabled
		// -------------------------------------------------------------------------
		// 28/11/2015 ECU device administration
		// -------------------------------------------------------------------------
		// 28/11/2015 ECU request activation
		// -------------------------------------------------------------------------
		componentName = new ComponentName (context,DeviceAdministrator.class);   
		Intent intent = new Intent (DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);   
		intent.putExtra (DevicePolicyManager.EXTRA_DEVICE_ADMIN,componentName);     
		((Activity) theContext).startActivityForResult (intent,StaticData.REQUEST_CODE_DEVICE_ADMIN);
		// -------------------------------------------------------------------------
		// 28/11/2015 ECU update the status information
		// -------------------------------------------------------------------------
		// 26/12/2015 ECU check if the alarm is already active or not
		// -------------------------------------------------------------------------
		if (!PublicData.storedData.panicAlarm.active)
		{
			updateStatusMessage (theContext.getString (R.string.panic_alarm_enabled) + 
								 PublicData.storedData.panicAlarm.startTime.Print());
			// ---------------------------------------------------------------------
			// 02/12/2015 ECU set up an alarm which will start almost
			//                immediately to kick off the ACTIVATE
			//                alarm. See Notes against this date
			// ---------------------------------------------------------------------
			Calendar calendar = Calendar.getInstance ();
			SetAlarm (context,
					(calendar.getTimeInMillis() + (StaticData.MILLISECONDS_PER_MINUTE/10)),
					StaticData.PANIC_ALARM_IMMEDIATE);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 26/12/2015 ECU the alarm is already active so decide what to do
			// ---------------------------------------------------------------------
			// 27/12/2015 ECU change the wallpaper to indicate that the panic alarm is
			//                active
			// 25/03/2016 ECU changed to use the new local method
			// ---------------------------------------------------------------------
			setWallPaper (true);
			// ---------------------------------------------------------------------
			// 27/12/2015 ECU decide whether the accelerometer is to be handled
			// ---------------------------------------------------------------------
			if (PublicData.storedData.panicAlarm.shake)
				accelerometerControl (true);
			// ---------------------------------------------------------------------
			// 26/12/2015 ECU generate the appropriate prompt
			// ---------------------------------------------------------------------
			handlePrompt (context,false);
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	static long getTimeInMilliSeconds (DibosonTime theTime)
	{
		// -------------------------------------------------------------------------
		// 0/12/2015 ECU created to convert theTime which consists of hours and
		//               minutes into real milliseconds
		// -------------------------------------------------------------------------
		Calendar calendar = Calendar.getInstance ();
		
		long currentTime = calendar.getTimeInMillis();
		
		calendar.set (Calendar.HOUR_OF_DAY,theTime.hour);
		calendar.set (Calendar.MINUTE, theTime.minute);
		calendar.set (Calendar.SECOND, 0);
		calendar.set (Calendar.MILLISECOND, 0);
			
		long localTime = calendar.getTimeInMillis();
		// -------------------------------------------------------------------------
		// 01/12/2015 ECU determine if the alarm is for today or tomorrow
		// -------------------------------------------------------------------------
		if (localTime < currentTime)
		{
			// ---------------------------------------------------------------------
			// 01/12/2015 ECU alarm will start tomorrow
			// ---------------------------------------------------------------------
			localTime += StaticData.MILLISECONDS_PER_DAY;
		}
		// -------------------------------------------------------------------------
		// 02/12/2/2015 ECU remove the seconds bit
		// -------------------------------------------------------------------------
		localTime = (localTime / StaticData.MILLISECONDS_PER_MINUTE) * StaticData.MILLISECONDS_PER_MINUTE;
		// -------------------------------------------------------------------------
		// 02/12/2015 ECU return the corrected time in milliseconds
		// -------------------------------------------------------------------------
		return localTime;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void handleAlarm (Context theContext,int theAlarmType)
	{
		// -------------------------------------------------------------------------
		// 01/12/2015 ECU created to be called by the receiver called by the alarm
		//                manager to handle panic alarm events
		//            ECU set next alarm 60 seconds in the future
		// -------------------------------------------------------------------------
		// 02/12/2015 ECU switch on the alarm type
		// -------------------------------------------------------------------------
		switch (theAlarmType)
		{
			// ---------------------------------------------------------------------
			case StaticData.PANIC_ALARM_ACTIVATE:
				// -----------------------------------------------------------------
				// 02/12/2015 ECU called up the method to handle the activation
				// -----------------------------------------------------------------
				handleActivate (theContext);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case StaticData.PANIC_ALARM_DEACTIVATE:
				// -----------------------------------------------------------------
				// 02/12/2015 ECU called up the method to handle the deactivation
				// -----------------------------------------------------------------
				handleDeactivate (theContext);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case StaticData.PANIC_ALARM_IMMEDIATE:
				// -----------------------------------------------------------------
				// 02/12/2015 ECU have received the 'initialisation' alarm so now
				//                set an alarm for the panic alarm's 'start time'
				// -----------------------------------------------------------------
				SetAlarm (theContext,getTimeInMilliSeconds (PublicData.storedData.panicAlarm.startTime),StaticData.PANIC_ALARM_ACTIVATE);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case StaticData.PANIC_ALARM_PROMPT:
				// -----------------------------------------------------------------
				// 02/12/2015 ECU check the current time
				// 26/12/2015 ECU added the argument 'true' to indicate that a user
				//                prompt is required
				// -----------------------------------------------------------------
				handlePrompt (theContext,true);
				// ------------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void handleActivate (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 02/12/2015 ECU created to handle the activation of the panic alarm
		// -------------------------------------------------------------------------
		// 25/11/2015 ECU set the alarm to active
		// ------------------------------------------------------------------------
		PublicData.storedData.panicAlarm.active = true;
		PublicData.storedData.panicAlarm.nextPromptTime 
			= new DibosonTime (PublicData.storedData.panicAlarm.startTime);
		// -------------------------------------------------------------------------
		// 04/12/2015 ECU if 'shake' mode is required then enable the accelerometer
		//                listener
		// -------------------------------------------------------------------------
		if (PublicData.storedData.panicAlarm.shake)
			accelerometerControl (true);
		// -------------------------------------------------------------------------
		// 10/12/2015 ECU change the wallpaper to indicate that the panic alarm is
		//                active
		// 19/03/2016 ECU changed to use theContext rather than context
		// 25/03/2016 ECU changed to use the new local method
		// -------------------------------------------------------------------------
		setWallPaper (true);
		// -------------------------------------------------------------------------
		// 02/12/2015 ECU handle a prompt
	    // 26/12/2015 ECU added 'true' argument to indicate a user prompt is required
		// -------------------------------------------------------------------------
		handlePrompt (theContext,true);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void handleDeactivate (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 02/12/2015 ECU created to handle the deactivation of the panic alarm
		// -------------------------------------------------------------------------
		PublicData.storedData.panicAlarm.active = false;
		// -------------------------------------------------------------------------
		// 26/11/2015 ECU indicate that the alarm is inactive
		// 26/03/2016 ECU changed to use resource
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak (theContext.getString (R.string.panic_alarm_deactivated));
		// -------------------------------------------------------------------------
		// 01/12/2015 ECU update the status message
		// 26/03/2016 ECU changed to use resource
		// -------------------------------------------------------------------------
		updateStatusMessage (theContext.getString (R.string.panic_alarm_reactivated) + 
								PublicData.storedData.panicAlarm.startTime.Print());
		// -------------------------------------------------------------------------
		// 04/12/2015 ECU if 'shake' mode is required then unregister the accelerometer
		//                listener
		// -------------------------------------------------------------------------
		if (PublicData.storedData.panicAlarm.shake)
			accelerometerControl (false);
		// -------------------------------------------------------------------------
		// 10/12/2015 ECU restore the original wallpaper
		// 31/01/2016 ECU check for a null
		// 25/03/2016 ECU changed to use the new local method
		// -------------------------------------------------------------------------
		setWallPaper (false);
		// -------------------------------------------------------------------------
		// 02/12/2015 ECU need to set an interrupt for start time tomorrow
		//            ECU because the start ime will bow be before the current time
		//                then the getTine ..... will add in the number of milliseconds
		//                to move to the next day
		// -------------------------------------------------------------------------
		long nextAlarm = getTimeInMilliSeconds (PublicData.storedData.panicAlarm.startTime);
		SetAlarm (theContext,(nextAlarm),StaticData.PANIC_ALARM_ACTIVATE);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void handlePrompt (Context theContext,boolean thePromptFlag)
	{
		// -------------------------------------------------------------------------
		// 02/12/2015 ECU created to handle the the panic alarm prompt
		// 26/12/2015 ECU added thePromptFlag argument
		// -------------------------------------------------------------------------
		// 26/11/2015 ECU ask user to confirm that they are OK
		// 26/12/2015 ECU add the check on whether the user prompt is required
		// -------------------------------------------------------------------------
		if (thePromptFlag)
		{
			// ---------------------------------------------------------------------
			// 26/11/2015 ECU start up activity to get user response
			// ---------------------------------------------------------------------
			Intent localIntent = new Intent (theContext,PanicAlarmActivity.class);
			localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// ---------------------------------------------------------------------
			// 26/11/2015 ECU indicate that want to put the activity into prompt mode
			// ---------------------------------------------------------------------
			localIntent.putExtra (StaticData.PARAMETER_PROMPT, true);
			theContext.startActivity (localIntent);
			// ---------------------------------------------------------------------
			// 25/11/2015 ECU set up the next prompt time
			// ---------------------------------------------------------------------
			PublicData.storedData.panicAlarm.nextPromptTime.adjustTime (PublicData.storedData.panicAlarm.intervalTime);
			// ---------------------------------------------------------------------
			// 25/03/2016 ECU make sure that the wallpaper reflects the state
			// ---------------------------------------------------------------------
			setWallPaper (true);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 02/12/2015 ECU set up an interrupt for the next prompt
		// ------------------------------------------------------------------------
		long nextPromptTime = getTimeInMilliSeconds (PublicData.storedData.panicAlarm.nextPromptTime);
		long endTime 		= getTimeInMilliSeconds (PublicData.storedData.panicAlarm.endTime);
		// -------------------------------------------------------------------------
		if (nextPromptTime < endTime)
		{
			// ---------------------------------------------------------------------
			// 02/12/2015 ECU update the status message
			// ---------------------------------------------------------------------
			updateStatusMessage (theContext.getString (R.string.panic_alarm_next_prompt) + 
									PublicData.storedData.panicAlarm.nextPromptTime.Print());
			// ---------------------------------------------------------------------
			// 02/12/2015 ECU set an alarm to occur when the prompt is to occur
			// ---------------------------------------------------------------------
			SetAlarm (theContext,nextPromptTime,StaticData.PANIC_ALARM_PROMPT);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/12/2015 ECU update the status message
			// ---------------------------------------------------------------------
			updateStatusMessage (theContext.getString (R.string.panic_alarm_deactivation) + 
										PublicData.storedData.panicAlarm.endTime.Print());
			// ---------------------------------------------------------------------
			// 02/12/2015 ECU set an alarm to occur when the deactivation is to occur
			// ---------------------------------------------------------------------
			SetAlarm (theContext,endTime,StaticData.PANIC_ALARM_DEACTIVATE);
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static void Initialise (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 26/12/2015 ECU created to be called when the app is restarted to decide
		//                what to do with the panic alarm
		// -------------------------------------------------------------------------
		if (PublicData.storedData.panicAlarm != null)
		{
			// ---------------------------------------------------------------------
			// 26/12/2015 ECU the panic alarm has been defined so just decide what
			//                to do
			// ---------------------------------------------------------------------
			if (PublicData.storedData.panicAlarm.enabled)
			{
				// -----------------------------------------------------------------
				// 26/12/2015 ECU the alarm has been enabled so need to sort out
				//                the activities
				// -----------------------------------------------------------------
				Intent localIntent = new Intent (theContext,PanicAlarmActivity.class);
				localIntent.putExtra (StaticData.PARAMETER_PANIC_INITIALISE, true);
				localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				theContext.startActivity (localIntent);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void locationUpdate (Location theNewLocation)
	{
		// -------------------------------------------------------------------------
		// 28/11/2015 ECU created to be called when the tracking software detects
		//                a location change
		// -------------------------------------------------------------------------
		Utilities.SendEmailMessage (context,
						  			PublicData.storedData.panicAlarm.trackingEmail,
						  			"PANIC ALARM - Tracking Information", 
						  			"Current position is \n\n" + 
						  			"<a href=\"" +
						  			"https://www.google.co.uk/maps/place/" + 
						  				theNewLocation.getLatitude() + "," + 
						  				theNewLocation.getLongitude() + 
						  			"\">Click here for Google map</a>",
						  			null,null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class PanicAlarmHandler extends Handler
    {
		// -------------------------------------------------------------------------
		// 26/11/2015 ECU created to enable a thread or activity to request that
		// -------------------------------------------------------------------------
        @Override
        public void handleMessage (Message theMessage) 
        {   
        	// ---------------------------------------------------------------------
        	// 05/05/2015 ECU change to switch on the type of message received
        	//                which is in '.what'
        	// ---------------------------------------------------------------------
        	switch (theMessage.what)
        	{
        		// ----------------------------------------------------------------- 		
        		case StaticData.MESSAGE_REQUEST:
        			// -------------------------------------------------------------
 	            	// 26/11/2015 ECU wait for the response time
	            	// -------------------------------------------------------------
        			panicAlarmCounter = PublicData.storedData.panicAlarm.responseTime;
	                sleep (1000);
	                // -------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_SLEEP:
        			// -------------------------------------------------------------
        			// 05/05/2015 ECU put any code that is the result of a 'sleep'
        			//                here
        			// -------------------------------------------------------------
	                // 26/11/2015 ECU check if user has responded
	                // -------------------------------------------------------------
	                if (!PublicData.storedData.panicAlarm.userResponse)
	                {
	                	// ---------------------------------------------------------
	                	// 26/11/2015 ECU decrement the counter and display the new
	                	//                value
	                	// ---------------------------------------------------------
	                	panicAlarmCounter--;
	                	panicAlarmTimer.setText ("" + panicAlarmCounter );
	                	// ---------------------------------------------------------
	                	// 26/11/2015 ECU check if it is time to action any alarms
	                	// ---------------------------------------------------------
	                	if (panicAlarmCounter <= 0)
	                	{
	                		// -----------------------------------------------------
	                		// 26/11/2015 ECU need to action the alarms
	                		// -----------------------------------------------------
	                		Actions (context);
	                		// -----------------------------------------------------
	                		// 26/11/2015 ECU and exit this activity
	                		// -----------------------------------------------------
	                		finish ();
	                	}
	                	else
	                	{
	                		sleep (1000);
	                	}
	                	// ---------------------------------------------------------
	                }
        			break;
        		// -----------------------------------------------------------------
        	}
        	// ---------------------------------------------------------------------
        }
        /* ------------------------------------------------------------------------- */
        public void sleep(long delayMillis)
        {	
        	// ---------------------------------------------------------------------
        	// 05/05/2015 ECU changed to use MESSAGE_SLEEP instead of 0
        	// ---------------------------------------------------------------------
            this.removeMessages (StaticData.MESSAGE_SLEEP);
            sendMessageDelayed (obtainMessage (StaticData.MESSAGE_SLEEP), delayMillis);
        }
    };
	// =============================================================================
    static void screenLock (boolean theLockFlag)
    {
    	// -------------------------------------------------------------------------
    	// 28/11/2015 ECU created to handle screen locking / unlocking
    	//
    	//                theLockFlag = true      lock the screen
    	//                            = false     unlock the screen
    	//            ECU only check if the administration is allowed
    	// -------------------------------------------------------------------------
    	if (PublicData.deviceAdminEnabled)
    	{	
    		if (theLockFlag)
    		{
    			// -----------------------------------------------------------------
    			// 28/11/2015 ECU lock the screen
    			// -----------------------------------------------------------------
    			((DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE)).lockNow(); 
    			// -----------------------------------------------------------------
    		}
    		else
    		{
    			// -----------------------------------------------------------------
    			// 28/11/2015 ECU unlock the screen
    			// -----------------------------------------------------------------
    			((Activity)context).getWindow().addFlags (WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    			((Activity)context).getWindow().addFlags (WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    			((Activity)context).getWindow().addFlags (WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    			((Activity)context).getWindow().addFlags (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    			// -----------------------------------------------------------------
    		}
    	}
    }
    // =============================================================================
    static void SetAlarm (Context theContext,long theTime,int theAlarmType)
    {
		// -------------------------------------------------------------------------	
		// 01/12/2015 ECU now set the alarm
    	// 03/11/2016 ECU changed to use the global alarm manager
		// -------------------------------------------------------------------------	
		Intent alarmIntent = new Intent (theContext, AlarmManagerReceiver.class);
		alarmIntent.putExtra (StaticData.PARAMETER_ALARM_ID,StaticData.ALARM_ID_PANIC_ALARM);
		// -------------------------------------------------------------------------
		// 02/12/2015 ECU store the mode into the intent
		// -------------------------------------------------------------------------
		alarmIntent.putExtra (StaticData.PARAMETER_ALARM_TYPE,theAlarmType);
		// -------------------------------------------------------------------------
		alarmIntent.putExtra (StaticData.PARAMETER_ALARM_ID,StaticData.ALARM_ID_PANIC_ALARM);
		alarmPendingIntent = PendingIntent.getBroadcast (theContext,
				StaticData.ALARM_ID_PANIC_ALARM,alarmIntent,Intent.FLAG_ACTIVITY_NEW_TASK);  
		// -------------------------------------------------------------------------
		// 24/12/2015 ECU changed to use the new method
		// 03/11/2016 ECU changed to use the global alarm manager
		// -------------------------------------------------------------------------
		Utilities.SetAnExactAlarm (PublicData.alarmManager,theTime, alarmPendingIntent);
		// -------------------------------------------------------------------------
    }
	// =============================================================================
	static void SetNotification (Context theContext,String theNotificationMessage)
	{
		// -------------------------------------------------------------------------
		// 12/03/2016 ECU created in case a notification is needed in the future
		// -------------------------------------------------------------------------
		NotificationManager notificationManager 
				= (NotificationManager) theContext.getSystemService (NOTIFICATION_SERVICE);
		// -------------------------------------------------------------------------
		// 12/03/2016 ECU there is a manager so set up the required data
		// -------------------------------------------------------------------------
		if (notificationManager != null)
		{
			// ---------------------------------------------------------------------
			// 12/03/2016 ECU set the components of the notification
			// 16/03/2016 ECU chenged to use the 'big text style'
			// ---------------------------------------------------------------------
			NotificationCompat.Builder notificationBuilder =
				    new NotificationCompat.Builder (theContext)
				    						.setSmallIcon (R.drawable.timer_icon_on)
				    						.setContentTitle ("Panic Alarm")
				    						.setStyle(new NotificationCompat.BigTextStyle().bigText(theNotificationMessage));
	        // -------------------------------------------------------------------------
	        // 26/06/2013 ECU specify the activity to be started if the user selects it
	        //                from the list of notifications
	        // -------------------------------------------------------------------------
			PendingIntent resultPendingIntent =
								PendingIntent.getActivity (theContext,
														   0,
														   new Intent (theContext,PanicAlarmActivity.class),
														   PendingIntent.FLAG_UPDATE_CURRENT);
			notificationBuilder.setContentIntent(resultPendingIntent);
			// ---------------------------------------------------------------------
			// 12/03/2016 ECU now get the manager to action
			// ---------------------------------------------------------------------
			notificationManager.notify (StaticData.NOTIFICATION_PANIC_ALARM,notificationBuilder.build());
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	static void setWallPaper (boolean theState)
	{
		// -------------------------------------------------------------------------
		// 25/03/2016 ECU created to set or reset the wallpaper depeneding on the
		//                supplied state
		//				    theState = true   set the wallpaper to the panic alarm
		//                                    version after saving the current one
		//                             false  restore the wallpaper to that which
		//                                    was saved by the 'true' state
		// -------------------------------------------------------------------------
		if (theState)
		{
			// ---------------------------------------------------------------------
			// 25/03/2015 ECU set the panic alarm wallpaper
			// ---------------------------------------------------------------------
			if (existingWallPaper == null)
				existingWallPaper = Utilities.setWallPaper (context,R.drawable.panic_alarm);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 25/03/2016 ECU restore the saved wallpaper, if one exists
			// ---------------------------------------------------------------------
			if (existingWallPaper != null)
			{
				Utilities.setWallPaper(context,existingWallPaper);
				// -----------------------------------------------------------------
				// 25/03/2016 ECU clear the wallpaper drawable to indicate 'done'
				// -----------------------------------------------------------------
				existingWallPaper = null;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
	}
    // =============================================================================
    public static void tidyUp (Context theContext)
    {
    	// -------------------------------------------------------------------------
    	// 27/12/2015 ECU created to be called when the MainActivity is being destroyed
    	//                to tidy up any aspects to do with the panic alarm system
    	// -------------------------------------------------------------------------
    	// 27/12/2015 ECU make sure the wallpaper is restored, if necessary
    	// -------------------------------------------------------------------------
    	if (existingWallPaper != null)
    	{
    		Utilities.setWallPaper (theContext,existingWallPaper);
    	}
    	// -------------------------------------------------------------------------
    	// 27/12/2015 ECU disable the 'shake' handling - if enabled
    	// -------------------------------------------------------------------------
		if (PublicData.storedData.panicAlarm.shake)
			accelerometerControl (false);
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
	public static void updateScrollingTextView (TextView theSubTitleTextView)
	{
		// -------------------------------------------------------------------------
		// 28/11/2015 ECU create to update the scrolling text on the list view
		// 30/11/2015 ECU put in check on null
		// -------------------------------------------------------------------------
		if (PublicData.storedData.panicAlarm != null)
		{
			if (!PublicData.storedData.panicAlarm.enabled)
			{
				// -----------------------------------------------------------------
				// 12/04/2015 ECU make sure that the field is 'gone'
				// -----------------------------------------------------------------
				if (theSubTitleTextView != null && (theSubTitleTextView.getVisibility() != View.GONE))
				{
					theSubTitleTextView.setVisibility (View.GONE);
				}
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 28/11/2015 ECU the panic alarm is enabled
				// -----------------------------------------------------------------
				// 28/11/2015/2015 ECU and change the subtitle field, if required
				// -----------------------------------------------------------------
				if (theSubTitleTextView != null)
				{
					if (panicAlarmStatus != null)
					{
						if (theSubTitleTextView.getVisibility() != View.VISIBLE)
							theSubTitleTextView.setVisibility (View.VISIBLE);
						theSubTitleTextView.setText (panicAlarmStatus);
						theSubTitleTextView.setSelected (true);
					}
					else
					{
						// ---------------------------------------------------------
						// 28/11/2015 ECU if null then this is an indication to hide 
						//                the text view
						// ---------------------------------------------------------
						theSubTitleTextView.setVisibility (View.GONE);
						// ---------------------------------------------------------
					}
				}
			}
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void updateStatusMessage (String theMessage)
	{
		// -------------------------------------------------------------------------
		// 28/11/2015 ECU created to set the status message and to request a display
		//                refresh
		//            ECU check if message has changed
		// -------------------------------------------------------------------------
		if ((panicAlarmStatus == null) || !panicAlarmStatus.equalsIgnoreCase(theMessage))
		{
			panicAlarmStatus = theMessage;
			// ---------------------------------------------------------------------
			// 28/11/2015 ECU request the refresh
			// ---------------------------------------------------------------------
			MusicPlayer.refreshImageAdapter ();
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
    public static void AlarmCommands (String theCommands)
    {
    	// -------------------------------------------------------------------------
    	// 27/11/2015 ECU created to store commands that are required for the panic
    	//                alarm
    	// -------------------------------------------------------------------------
    	PublicData.storedData.panicAlarm.alarmCommands = theCommands;
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void CancelSecurityString (String theSecurityString)
    {
    	// -------------------------------------------------------------------------
    	// 03/12/2015 ECU called to clear the security string
    	// -------------------------------------------------------------------------
    	PublicData.storedData.panicAlarm.security = null;
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
    public static void ConfirmSecurityString (String theSecurityString)
    {
    	// -------------------------------------------------------------------------
    	// 03/12/2015 ECU called to set up the security string
    	// -------------------------------------------------------------------------
    	PublicData.storedData.panicAlarm.security = ((theSecurityString.equalsIgnoreCase ("")) ? null 
    																		    			   : theSecurityString);
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
	public static void IntervalTime (int theTime)
	{
		// -------------------------------------------------------------------------
		// 25/11/2015 ECU set the interval between prompts in minutes
		// -------------------------------------------------------------------------
		PublicData.storedData.panicAlarm.intervalTime = theTime;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    public static void PromptCommands (String theCommands)
    {
    	// -------------------------------------------------------------------------
    	// 27/11/2015 ECU created to store commands that are required for the panic
    	//                alarm when a response is required from the user
    	// -------------------------------------------------------------------------
    	PublicData.storedData.panicAlarm.promptCommands = theCommands;
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void ResponseCommands (String theCommands)
    {
    	// -------------------------------------------------------------------------
    	// 11/12/2015 ECU created to store commands that are required for the panic
    	//                alarm when a response is received from the user
    	// -------------------------------------------------------------------------
    	PublicData.storedData.panicAlarm.responseCommands = ((theCommands.equalsIgnoreCase ("")) ? null 
 			    																				 : theCommands);
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
	public static void ResponseTime (int theTime)
	{
		// -------------------------------------------------------------------------
		// 25/11/2015 ECU set the time in which the user must respond in seconds
		// -------------------------------------------------------------------------
		PublicData.storedData.panicAlarm.responseTime = theTime;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SetEndTime (Object theEndTime)
	{
		// -------------------------------------------------------------------------
		// 25/11/2015 ECU store the start time in the object
		// -------------------------------------------------------------------------
		PublicData.storedData.panicAlarm.endTime = new DibosonTime ((DibosonTime) theEndTime);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
  	public static void SetStartTime (Object theStartTime)
  	{
  		// -------------------------------------------------------------------------
  		// 25/11/2015 ECU store the start time in the object
  		// -------------------------------------------------------------------------
  		PublicData.storedData.panicAlarm.startTime = new DibosonTime ((DibosonTime) theStartTime);
  		// -------------------------------------------------------------------------
  	}
  	// =============================================================================
  	public static void ShakeNoMethod (Object theSelection)
   	{
  		// -------------------------------------------------------------------------
  		// 04/12/2015 ECU created to switch off the 'shake' mode
  		// -------------------------------------------------------------------------
  		PublicData.storedData.panicAlarm.shake = false;
   	}
	// =============================================================================
 	public static void ShakeYesMethod (Object theSelection)
  	{
 		// -------------------------------------------------------------------------
  		// 04/12/2015 ECU created to switch on the 'shake' mode
  		// -------------------------------------------------------------------------
 		PublicData.storedData.panicAlarm.shake = true;
  	}
 	// =============================================================================
}

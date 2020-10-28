package com.usher.diboson;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;

// =================================================================================
public class CountdownTimerActivity extends DibosonActivity 
{
	// =============================================================================
	// 04/02/2018 ECU created to handle the 'countdown timer'
	//            ECU IMPORTANT most of thois activity could be achieved with
	//                ========= alarms but wrote it this way just to play with
	//                          the in-built countdown timer
	// 06/02/2018 ECU IMPORTANT seems to be a problem that occasionally when the
	//                ========= layout is displayed the 'minutes' number picker is
	//                          not visible. It works fine but not sure what is
	//                          causing the problem.
	//
	//                          By all accounts this is a known problem within
	//                          the Android OS - there are some workarounds but just
	//                          leave it as it is for the moment.
	// 07/02/2018 ECU Following on from the comment above - noticed that the 'minutes'
	//                number picker does appear but a little later
	// =============================================================================
	
	
	// =============================================================================
	static  boolean		 absoluteTimeValue			= false;
	static 	String		 actionsExpiry				= StaticData.BLANK_STRING;
	static	String		 actionsReminder			= StaticData.BLANK_STRING;
	static	Context      context;
	static	CountDownTimer
						 countdownTimer			    = null;
	static  boolean		 countdownTimerActive		= false;
	static 	int			 hoursPickerValue			= 0;
	static 	int			 minutesPickerValue			= 0;
	static 	int			 secondsPickerValue			= 0;
	static 	int			 hoursReminderPickerValue	= 0;
	static 	int			 minutesReminderPickerValue	= 0;
	static 	int			 secondsReminderPickerValue	= 0;
	static  String       timerStatus				= StaticData.BLANK_STRING;
	// -----------------------------------------------------------------------------
			CheckBox	 absoluteTimeCheckBox;
			NumberPicker hoursPicker, 			hoursReminderPicker;	
			NumberPicker minutesPicker,			minutesReminderPicker; 
			NumberPicker secondsPicker,			secondsReminderPicker; 
	// =============================================================================

	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 04/02/2018 ECU set up some particular activity characteristics
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// -----------------------------------------------------------------
			// 04/02/2018 ECU remember the context for later use
			// -----------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 10/03/2018 ECU check if any parameters have been passed across
			// ---------------------------------------------------------------------
			Bundle extras = getIntent ().getExtras ();
			// ---------------------------------------------------------------------
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 26/11/2015 ECU check if being called to set prompt
				// -----------------------------------------------------------------
				boolean timerRunning = extras.getBoolean (StaticData.PARAMETER_COUNTDOWN_TIMER,false);
				// -----------------------------------------------------------------
				// 10/03/2018 ECU check if there is an outstanding timer
				// -----------------------------------------------------------------
				if (timerRunning)
				{
					// -------------------------------------------------------------
					// 10/03/2018 ECU need to reinstate the running timer
					// -------------------------------------------------------------
					StartCountdownTimer ();
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 10/03/2018 ECU just exit this activity
					// -------------------------------------------------------------
					finish ();
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				setContentView (R.layout.activity_countdown_timer);
				// -----------------------------------------------------------------
				// 10/03/2018 ECU just create an empty data object
				// -----------------------------------------------------------------
				if (PublicData.storedData.countdownTimerData != null)
				{
					// -------------------------------------------------------------
					// 10/03/2018 ECU a timer is already running
					// -------------------------------------------------------------
					// 06/02/2018 ECU tell the user of the situation
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString (R.string.countdown_timer_only_one), true);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 10/03/2018 ECU a timer has previously been cancelled so
					//                just create a blank entry
					// -------------------------------------------------------------
					PublicData.storedData.countdownTimerData = new CountdownTimerData ();
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 06/02/2018 ECU set up the 'absolute time' checkbox
				// -----------------------------------------------------------------
				absoluteTimeCheckBox = (CheckBox) findViewById (R.id.absolute_time);
				absoluteTimeCheckBox.setChecked (absoluteTimeValue);
				// -----------------------------------------------------------------
				// 04/02/2018 ECU declare the time components
				// -----------------------------------------------------------------
				hoursPicker 	= (NumberPicker) findViewById (R.id.hours_picker);
				minutesPicker 	= (NumberPicker) findViewById (R.id.minutes_picker);
				secondsPicker 	= (NumberPicker) findViewById (R.id.seconds_picker);
				// -----------------------------------------------------------------
				// 02/01/2018 ECU set up the minimum and maximum values for the 
				//                pickers
				// -----------------------------------------------------------------
				hoursPicker.setMinValue   	(0);
				hoursPicker.setMaxValue   	(23);
				minutesPicker.setMinValue 	(0);
				minutesPicker.setMaxValue 	(59);
				secondsPicker.setMinValue 	(0);
				secondsPicker.setMaxValue 	(59);
				// -----------------------------------------------------------------
				// 04/02/2018 ECU declare the time components
				// -----------------------------------------------------------------
				hoursReminderPicker 	= (NumberPicker) findViewById (R.id.tick_hours_picker);
				minutesReminderPicker 	= (NumberPicker) findViewById (R.id.tick_minutes_picker);
				secondsReminderPicker 	= (NumberPicker) findViewById (R.id.tick_seconds_picker);
				// -----------------------------------------------------------------
				// 04/02/2018 ECU set up the minimum and maximum values for the 
				//                pickers
				// -----------------------------------------------------------------
				hoursReminderPicker.setMinValue   	(0);
				hoursReminderPicker.setMaxValue   	(23);
				minutesReminderPicker.setMinValue 	(0);
				minutesReminderPicker.setMaxValue 	(59);
				secondsReminderPicker.setMinValue 	(0);
				secondsReminderPicker.setMaxValue 	(59);
				// -----------------------------------------------------------------
				// 04/02/2018 ECU set up the initial values
				// -----------------------------------------------------------------
				hoursPicker.setValue 			(hoursPickerValue);
				minutesPicker.setValue 			(minutesPickerValue);
				secondsPicker.setValue 			(secondsPickerValue);
				hoursReminderPicker.setValue 	(hoursReminderPickerValue);
				minutesReminderPicker.setValue 	(minutesReminderPickerValue);
				secondsReminderPicker.setValue 	(secondsReminderPickerValue);
				// -----------------------------------------------------------------
				((Button) findViewById (R.id.start_countdown_timer_button)).setOnClickListener (new View.OnClickListener ()
				{
					@Override
					public void onClick (View view) 
					{	
						// ---------------------------------------------------------
						//06/02/2018 ECU check if there is already a timer running
						// ---------------------------------------------------------
						if (countdownTimer != null)
						{
							// -----------------------------------------------------
							// 06/02/2018 ECU cancel the existing timer
							// -----------------------------------------------------
							countdownTimer.cancel ();
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
						// 04/02/2018 ECU set up the timer
						// ---------------------------------------------------------
						hoursPickerValue 			= hoursPicker.getValue ();
						minutesPickerValue 			= minutesPicker.getValue ();
						secondsPickerValue 			= secondsPicker.getValue ();
						hoursReminderPickerValue 	= hoursReminderPicker.getValue ();
						minutesReminderPickerValue 	= minutesReminderPicker.getValue ();
						secondsReminderPickerValue 	= secondsReminderPicker.getValue ();
						// ---------------------------------------------------------
						long timerDuration;
						// ---------------------------------------------------------
						// 06/02/2018 ECU check if the entered time is absolute or 
						//                not
						// ---------------------------------------------------------
						if ((absoluteTimeValue = absoluteTimeCheckBox.isChecked()))
						{
							// -----------------------------------------------------
							// 06/02/2018 ECU the entered time is 'absolute'
							// -----------------------------------------------------
							long currentTime = Utilities.getAdjustedTime (false);
							long inputTime   = Utilities.getTime(hoursPickerValue,minutesPickerValue,secondsPickerValue);
							// -----------------------------------------------------
							// 06/02/2018 ECU check if the time is for 'tomorrow'
							// -----------------------------------------------------
							if (inputTime <= currentTime)
							{
								inputTime += StaticData.MILLISECONDS_PER_DAY;
							}
							// -----------------------------------------------------
							// 06/02/2018 ECU work out the time till the 'alarm' is wanted
							// -----------------------------------------------------
							timerDuration = inputTime - currentTime;
							// -----------------------------------------------------
						}
						else
						{
							// -----------------------------------------------------
							// 06/02/2018 ECU the timer is 'relative'
							// -----------------------------------------------------
							timerDuration 		= 	hoursPickerValue   * StaticData.MILLISECONDS_PER_HOUR +
													minutesPickerValue * StaticData.MILLISECONDS_PER_MINUTE +
													secondsPickerValue * 1000;
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
						long reminderDuration 	= 	hoursReminderPickerValue   * StaticData.MILLISECONDS_PER_HOUR +
													minutesReminderPickerValue * StaticData.MILLISECONDS_PER_MINUTE +
													secondsReminderPickerValue * 1000;
						// ---------------------------------------------------------
						// 10/03/2018 ECU create the countdown data
						// ---------------------------------------------------------
						PublicData.storedData.countdownTimerData = new CountdownTimerData (timerDuration,
																						   reminderDuration,
																						   actionsExpiry,
																						   actionsReminder);
						// ----------------------------------------------------------
						// 10/03/2018 ECU restart the timer
						// ----------------------------------------------------------
						StartCountdownTimer ();
						// ----------------------------------------------------------
					}
				});
				// ------------------------------------------------------------------
				((Button) findViewById (R.id.define_countdown_timer_actions_button)).setOnClickListener (new View.OnClickListener ()
				{
					@Override
					public void onClick (View view) 
					{	
						// ---------------------------------------------------------
						// 04/02/2018 ECU define the actions to take when the timer 
						//                expires
						// 10/03/2018 ECU changed to show the actions stored in the 
						//                object
						// ---------------------------------------------------------
						DialogueUtilities.multilineTextInput (context,
															  getString (R.string.countdown_timer_expiry_title),
															  getString (R.string.action_command_summary),
															  5,
															  PublicData.storedData.countdownTimerData.actionsExpiration,
															  Utilities.createAMethod (CountdownTimerActivity.class,
							  									                   "SetExpiryActions",
							  									                   StaticData.BLANK_STRING),
							  							      null,
							  							      StaticData.NO_RESULT,
							  							      getString (R.string.press_to_define_command));
						// ---------------------------------------------------------
					}
				});
				// -----------------------------------------------------------------
				((Button) findViewById (R.id.define_countdown_timer_tick_actions_button)).setOnClickListener (new View.OnClickListener ()
				{
					@Override
					public void onClick (View view) 
					{	
						// ---------------------------------------------------------
						// 04/02/2018 ECU define the actions to be taken when the 
						//                reminder for the countdown timer occurs
						// 10/03/2018 ECU changed to show the actions stored in the 
						//                object
						// ---------------------------------------------------------
						DialogueUtilities.multilineTextInput (context,
															  getString (R.string.countdown_timer_reminder_title),
															  getString (R.string.action_command_summary),
															  5,
															  PublicData.storedData.countdownTimerData.actionsBetween,
															  Utilities.createAMethod (CountdownTimerActivity.class,
							  									                   "SetReminderActions",
							  									                   StaticData.BLANK_STRING),
							  								  null,
							  								  StaticData.NO_RESULT,
							  								  getString (R.string.press_to_define_command));
						// ---------------------------------------------------------
					}
				});
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 04/02/2018 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	@Override
	public void onBackPressed ()
	{
		// -------------------------------------------------------------------------
		// 06/02/2018 ECU created to just finish this activity
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
		// 06/02/2018 ECU now call the super for this method
		// -------------------------------------------------------------------------
		super.onBackPressed();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void updateText (TextView theTextView)
	{
		// -------------------------------------------------------------------------
		// 05/02/2018 ECU update the list view adapter text
		// -------------------------------------------------------------------------
		if (countdownTimerActive)
		{
			// ---------------------------------------------------------------------
			// 05/02/2018 ECU show the status of the timer
			// ---------------------------------------------------------------------
			theTextView.setText (timerStatus);
			// ---------------------------------------------------------------------
			// 05/02/2018 ECU if necessary make the field visible
			// ---------------------------------------------------------------------
			if (theTextView.getVisibility() != View.VISIBLE)
			{
				// -----------------------------------------------------------------
				theTextView.setVisibility (View.VISIBLE);
				theTextView.setSelected (true);
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 05/02/2018 ECU if necessary hide the text view
			// ---------------------------------------------------------------------
			if (theTextView.getVisibility() != View.GONE)
			{
				// -----------------------------------------------------------------
				// 05/02/2018 ECU reset the displayed text and hide the field
				// -----------------------------------------------------------------
				theTextView.setText (StaticData.BLANK_STRING);
				theTextView.setVisibility (View.GONE);
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		
	}
	// =============================================================================
	
	// =============================================================================
	public static void CheckIfRunning (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 10/03/2018 ECU check if a timer was running when the app was stopped
		// -------------------------------------------------------------------------
		if (PublicData.storedData.countdownTimerData != null)
		{
			// ---------------------------------------------------------------------
			// 10/03/2018 ECU a 'countdown timer' was running when the app was closed
			//                but need to decide if the timer expired while the app
			//                was 'down'
			// ---------------------------------------------------------------------
			if (PublicData.storedData.countdownTimerData.stillValid ())
			{
				// -----------------------------------------------------------------
				// 10/03/2018 ECU it looks like there was a timer running so start up
				//                the activity
				// -----------------------------------------------------------------
				Intent localIntent = new Intent (theContext,CountdownTimerActivity.class);
				localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
				// -----------------------------------------------------------------
				// 10/03/2018 ECU indicate that want timer running
				// -----------------------------------------------------------------
				localIntent.putExtra (StaticData.PARAMETER_COUNTDOWN_TIMER, true);
				theContext.startActivity (localIntent);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 10/03/2018 ECU the countdown timer that was running 'expired'
				//                while the app was 'down' - let the user know
				//                about this
				// 25/11/2018 ECU added the message colour
				// 15/07/2020 ECU added the title
				// -----------------------------------------------------------------
				NotificationMessage.Add (theContext.getString (R.string.countdown_timer),
									     String.format (theContext.getString (R.string.countdown_timer_expired_format),
									     PublicData.dateFormatterCurrent.format (PublicData.storedData.countdownTimerData.timeExpiration)),
										 StaticData.NOTIFICATION_COLOUR_ERROR);
				// -----------------------------------------------------------------
				// 10/03/2018 ECU clear the data object
				// -----------------------------------------------------------------
				PublicData.storedData.countdownTimerData = null;
				//------------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void StartCountdownTimer ()
	{
		// -------------------------------------------------------------------------
		// 10/03/2018 ECU created to start/restart the countdown timer based on the
		//                stored data
		// -------------------------------------------------------------------------
		// 04/02/2018 ECU now set up the timer with associated methods that will be 
		//                invoked when the event happens
		// 06/02/2018 ECU changed to handle the fact that the method now returns the
		//                countdown time object
		// 10/03/2018 ECU changed to use data in the 'countdownTimerData' object
		// -------------------------------------------------------------------------
		countdownTimer = Utilities.countdownTimer (PublicData.storedData.countdownTimerData.timeTillExpiration,
												   PublicData.storedData.countdownTimerData.timeBetweenTicks, 
												   Utilities.createAMethod (CountdownTimerActivity.class,
		      										   					"TimerReminderMethod",(long) 0), 
		      									   Utilities.createAMethod (CountdownTimerActivity.class,
		      										   					"TimerExpiryMethod"));
		// -------------------------------------------------------------------------
		// 06/02/2018 ECU check if everything is OK
		// -------------------------------------------------------------------------
		if (countdownTimer !=null)
		{
			// ---------------------------------------------------------------------
			// 06/02/2018 ECU start up the countdown timer
			// ---------------------------------------------------------------------
			countdownTimer.start ();
			// ---------------------------------------------------------------------
			// 05/02/2018 ECU indicate countdown timer is active
			// ---------------------------------------------------------------------
			countdownTimerActive = true;
			// ---------------------------------------------------------------------
			// 05/02/2018 ECU try and refresh the display adapter
			// ---------------------------------------------------------------------
			GridActivity.gridRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_ADAPTER); 
			// ---------------------------------------------------------------------
			// 02/01/2018 ECU terminate this activity
			// ---------------------------------------------------------------------
			finish ();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 04/02/2018 ECU indicate there was a problem
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (context.getString (R.string.countdown_timer_error),true);
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	
	// =============================================================================
	public static void SetExpiryActions (String theActionCommands)
	{
		// -------------------------------------------------------------------------
		// 04/02/2018 ECU created to take the action commands and store away
		// -------------------------------------------------------------------------
		actionsExpiry = theActionCommands;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SetReminderActions (String theActionCommands)
	{
		// -------------------------------------------------------------------------
		// 04/02/2018 ECU created to take the action commands and store away
		// -------------------------------------------------------------------------
		actionsReminder = theActionCommands;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void TimerExpiryMethod ()
	{
		// -------------------------------------------------------------------------
		// 05/02/2018 ECU indicate countdown timer has expired
		// -------------------------------------------------------------------------
		countdownTimerActive = false;
		// -------------------------------------------------------------------------
		// 06/02/2018 ECU reset the stored countdown timer
		// -------------------------------------------------------------------------
		countdownTimer = null;
		// -------------------------------------------------------------------------
		// 05/02/2018 ECU try and refresh the display adapter
		// -------------------------------------------------------------------------
		GridActivity.gridRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_ADAPTER); 
		// -------------------------------------------------------------------------
		// 04/02/2018 ECU process the actions that exist for when the timer expires
		// 10/03/2018 ECU changed to use the data stored in the object
		// -------------------------------------------------------------------------
		Utilities.actionHandler (context,PublicData.storedData.countdownTimerData.actionsExpiration);
		// -------------------------------------------------------------------------
		// 10/03/2018 ECU and clear the stored details
		// -------------------------------------------------------------------------
		PublicData.storedData.countdownTimerData = null;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void TimerReminderMethod (long theTimeLeft)
	{
		// -------------------------------------------------------------------------
		// 04/02/2018 ECU process the actions that exist for when the reminder occurs
		// 10/03/2018 ECU changed to use the actions stored in the object
		// -------------------------------------------------------------------------
		Utilities.actionHandler (context,PublicData.storedData.countdownTimerData.actionsBetween);
		// -------------------------------------------------------------------------
		timerStatus = "Countdown Timer : " + Utilities.printTime (theTimeLeft / 1000);
		// -------------------------------------------------------------------------
		GridActivity.gridRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_ADAPTER); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

package com.usher.diboson;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SpeakingClockActivity extends DibosonActivity 
{
	/* =============================================================================== */
	// ===============================================================================
	// 09/02/2014 ECU created
	// 11/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 01/02/2017 ECU add the 'show text' so that the time will be displayed as well as
	//                spoken
	// 10/03/2017 ECU added the Westminster Chimes
	// 14/03/2017 ECU add a seek bar to help with the input of the 'interval'
	// 25/07/2017 ECU added the Westminster chime end handling
	// 26/07/2017 ECU added the checkbox listener to initiate the chime end dialogue
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//================================================================================
	/* =============================================================================== */
	final static String TAG = "SpeakingClockActivity";
	/* =============================================================================== */
	
	/* =============================================================================== */
	static	PendingIntent 		alarmPendingIntent;						// 23/02/2014 ECU added
			Context				context;
			CheckBox			enable;
			Button				confirmDetails;
			TextView			interval;
			SeekBar				intervalSeekBar;						// 14/03/2017 ECU added
			CheckBox			showText;								// 01/02/2017 ECU added
			TimePicker			startTime;	
			TimePicker			stopTime;
			CheckBox			westminsterChimes;						// 10/03/2017 ECU added
	static  boolean				westminsterChimesEnd;					// 26/07/2017 ECU added
	/* ============================================================================= */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 11/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 28/11/2016 ECU call method to set the screen up
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true,true,false);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_speaking_clock);
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			enable				= (CheckBox)   findViewById (R.id.speaking_clock_checkbox);
			interval			= (TextView)   findViewById (R.id.input_speaking_clock_edittext);
			showText			= (CheckBox)   findViewById (R.id.speaking_clock_text_checkbox);
			startTime			= (TimePicker) findViewById (R.id.input_speaking_clock_start);
			stopTime			= (TimePicker) findViewById (R.id.input_speaking_clock_stop);
			westminsterChimes	= (CheckBox)   findViewById (R.id.speaking_clock_westminster_checkbox);
			// ---------------------------------------------------------------------	
			// 09/02/2014 ECU set time pickers to 24 hour format
			// ---------------------------------------------------------------------
			startTime.setIs24HourView (true);
			stopTime.setIs24HourView (true);
			// ---------------------------------------------------------------------
			// 09/02/2014 ECU set up the button an its listener
			// ---------------------------------------------------------------------
			confirmDetails = ((Button) findViewById (R.id.speaking_clock_button));
		
			confirmDetails.setOnClickListener (buttonListener);
		
			enable.setOnCheckedChangeListener (enableListener);
			// ---------------------------------------------------------------------
			// 14/03/2017 ECU set up the seekbar for the interval
			// ---------------------------------------------------------------------
			intervalSeekBar = (SeekBar) findViewById (R.id.input_speaking_clock_seekbar);
			intervalSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() 
			{
				// -----------------------------------------------------------------
	            @Override
	            public void onStopTrackingTouch (SeekBar seekBar) 
	            {

	            }
	            // -----------------------------------------------------------------
	            @Override
	            public void onStartTrackingTouch (SeekBar seekBar) 
	            {

	            }
	            // -----------------------------------------------------------------
	            @Override
	            public void onProgressChanged (SeekBar seekBar,int progress,boolean fromUser) 
	            {
	            	// -------------------------------------------------------------
	            	// 14/03/2017 ECU accept any value except 0
	            	// -------------------------------------------------------------
	            	if (progress != 0)
	            		interval.setText(Integer.toString (progress));
	            	// -------------------------------------------------------------
	            }
	            // -----------------------------------------------------------------
	        });
			// ---------------------------------------------------------------------
			// 26/07/2017 ECU preset the current setting for the chime end
			// ---------------------------------------------------------------------
			westminsterChimesEnd = PublicData.storedData.speakingClock.westminsterChimeEnd;
			// ---------------------------------------------------------------------
			// 26/07/2017 ECU add the listener for the Westminster chimes
			// ---------------------------------------------------------------------
			westminsterChimes.setOnClickListener(new OnClickListener() 
			{
				// -----------------------------------------------------------------
	            @Override
	            public void onClick (View theView) 
	            {
	                if (westminsterChimes.isChecked())
	                {
	                	// ---------------------------------------------------------
	                	// 26/07/2017 ECU ask the user when the chimes are to start 
	                	//                or end
	                	// ---------------------------------------------------------
	                	DialogueUtilities.yesNo (context,
								 				 getString (R.string.chime_title),
								 				 getString (R.string.chime_summary),
								 				 0, 
								 				 true,
								 				 getString (R.string.chime_end), 
								 				 Utilities.createAMethod (SpeakingClockActivity.class,"ChimeEndMethod",(Object) null),
								 				 true,
								 				 getString (R.string.chime_start), 
								 				 Utilities.createAMethod (SpeakingClockActivity.class,"ChimeStartMethod",(Object) null));
	                }
	                // -------------------------------------------------------------
	            }
	            // -----------------------------------------------------------------
	        });
			// ---------------------------------------------------------------------
			DisplayCurrentValues ();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 11/10/2015 ECU the activity has been recreated after having been
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
				case R.id.speaking_clock_button: 
				{
					// -------------------------------------------------------------
					// 06/01/2014 ECU accept button pressed so create
					//                appointment from entered details
					// -------------------------------------------------------------
					UpdateDetails ();
					// -------------------------------------------------------------
					InitialiseTheAlarm (getBaseContext());
					// -------------------------------------------------------------
					// 09/02/2014 ECU terminate this activity
					// -------------------------------------------------------------
					finish ();
					// -------------------------------------------------------------		
					break;
				}	
			}
		}
	};
	/* =============================================================================== */
	@Override
    protected void onDestroy() 
    {
        super.onDestroy();
    }
	/* =============================================================================== */
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

	    inputMethodManager.hideSoftInputFromWindow (startTime.getWindowToken(),0);
	    inputMethodManager.hideSoftInputFromWindow (stopTime.getWindowToken(),0);
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
	private CompoundButton.OnCheckedChangeListener enableListener = new CompoundButton.OnCheckedChangeListener() 
	{		
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) 
		{
			// ---------------------------------------------------------------------
			// 09/02/2014 ECU display / hide the fields depending on whether the clock is enabled
			//                or not
			// 13/02/2014 ECU placed the code in new method
			// ---------------------------------------------------------------------
			HideDisplayFields (isChecked);
			// ---------------------------------------------------------------------
		}
	};
	/* ============================================================================= */
	public static void CancelAlarm (Context theContext)
	{
		// ---------------------------------------------------------------------------
		// 24/02/2014 ECU created - called to cancel the alarm associated with the
		//                speaking clock
		// 01/01/2016 ECU changed to use the new method
		// 03/11/2016 ECU add the false to indicate no pending intent cancel
		// 21/07/2017 ECU could get here with a null intent so check. This could happen
		//                if the onDestroy method in MainActivity is called up by an error
		//                condition before the speaking clock has been initialise 
		//                properly
		// ---------------------------------------------------------------------------
		if (alarmPendingIntent != null)
			Utilities.cancelAnAlarm (theContext,alarmPendingIntent,false);
		// ---------------------------------------------------------------------------
	}
	/* =============================================================================== */
	void DisplayCurrentValues ()
	{
		// 09/02/2014 ECU display the current values in the form
		
		interval.setText(Integer.toString(PublicData.storedData.speakingClock.gap));
		// -------------------------------------------------------------------------
		// 13/03/2017 ECU reflevt the value of 'gap' on the seek bar
		// -------------------------------------------------------------------------
		intervalSeekBar.setProgress (PublicData.storedData.speakingClock.gap);
		// -------------------------------------------------------------------------
		startTime.setCurrentHour(PublicData.storedData.speakingClock.startHour);
		startTime.setCurrentMinute(PublicData.storedData.speakingClock.startMinute);
		
		stopTime.setCurrentHour(PublicData.storedData.speakingClock.stopHour);
		stopTime.setCurrentMinute(PublicData.storedData.speakingClock.stopMinute);
		
		enable.setChecked (PublicData.storedData.speakingClock.enabled);
		// -------------------------------------------------------------------------
		// 01/02/2017 ECU display the show text status
		// -------------------------------------------------------------------------
		showText.setChecked (PublicData.storedData.speakingClock.showText);
		// -------------------------------------------------------------------------
		// 10/03/2017 ECU display the Westminster chimes option
		// -------------------------------------------------------------------------
		westminsterChimes.setChecked (PublicData.storedData.speakingClock.westminsterChime);
		// -------------------------------------------------------------------------
		// 13/02/2014 ECU hide any fields which are not required unless the clock
		//                is enabled
		// -------------------------------------------------------------------------
		HideDisplayFields (PublicData.storedData.speakingClock.enabled);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	void HideDisplayFields (boolean theShowFlag)
	{
		// -------------------------------------------------------------------------
		// 13/02/2014 ECU create to have all the visibility bits in one place
		// 13/11/2016 ECU changed from INVISIBLE to GONE on time picker fields
		// 01/02/2017 ECU added showText
		// 10/03/2017 ECU added westminsterChimes
		// -------------------------------------------------------------------------
		((TextView) findViewById (R.id.title_speaking_clock_start)).setVisibility (theShowFlag ? View.VISIBLE : View.INVISIBLE);
		((TextView) findViewById (R.id.title_speaking_clock_stop)).setVisibility (theShowFlag ? View.VISIBLE : View.INVISIBLE);
		((TextView) findViewById (R.id.title_speaking_clock_interval)).setVisibility (theShowFlag ? View.VISIBLE : View.INVISIBLE);
		interval.setVisibility  (theShowFlag ? View.VISIBLE : View.INVISIBLE);
		intervalSeekBar.setVisibility  (theShowFlag ? View.VISIBLE : View.INVISIBLE);
		showText.setVisibility  (theShowFlag ? View.VISIBLE : View.GONE);
		startTime.setVisibility (theShowFlag ? View.VISIBLE : View.GONE);
		stopTime.setVisibility  (theShowFlag ? View.VISIBLE : View.GONE);
		westminsterChimes.setVisibility  (theShowFlag ? View.VISIBLE : View.GONE);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	static void InitialiseTheAlarm (Context theContext)
	{
		// ------------------------------------------------------------------------
		// 23/02/2014 ECU make sure that any alarm is cancelled
		// 24/02/2014 ECU changed to use the method
		// ------------------------------------------------------------------------
		CancelAlarm (theContext);
		// -------------------------------------------------------------------------
		// 23/02/2014 ECU initialise the first alarm for the speaking clock
		// -------------------------------------------------------------------------
		if (PublicData.storedData.speakingClock.enabled)
		{
			// ---------------------------------------------------------------------
			// 23/02/2014 ECU only create an alarm if the clock is enabled
			// ---------------------------------------------------------------------
			Calendar calendar = Calendar.getInstance ();
			
			long currentTime = calendar.getTimeInMillis();
			
			calendar.set (Calendar.HOUR_OF_DAY,PublicData.storedData.speakingClock.startHour);
			calendar.set (Calendar.MINUTE, PublicData.storedData.speakingClock.startMinute);
			calendar.set (Calendar.SECOND, 0);
			
			long alarmTime = calendar.getTimeInMillis();
			// ---------------------------------------------------------------------
			// 23/02/2014 ECU determine if the alarm is for today or tomorrow
			// ---------------------------------------------------------------------
			if (alarmTime < currentTime)
			{
				// -----------------------------------------------------------------
				// 23/02/2014 ECU alarm will start tomorrow
				// 01/09/2015 ECU changed to use StaticData
				// -----------------------------------------------------------------
				alarmTime += StaticData.MILLISECONDS_PER_DAY;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 23/02/2014 ECU now set the alarm
			// 11/03/2017 ECU add the alarm ID as argument
			// ---------------------------------------------------------------------
			SetAnAlarm (theContext,alarmTime,StaticData.ALARM_ID_SPEAKING_CLOCK);
			// ---------------------------------------------------------------------
			// 11/03/2017 ECU check if need to initialise the Westminster Chimes
			// 13/03/2017 ECU set a Westminster Chime - the method checks if it
			//                necessary
			// ------------------------------------------------------------------
			setWestminsterChimeAlarm (theContext,alarmTime);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 23/02/2014 ECU clock not enabled - cancel any outstanding alarms
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	public static void ReInstateAlarm (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 23/02/2014 ECU check if the speaking clock was active when the system was
		//                closed - if so then try and restart the relevant alarm
		// 24/02/2014 ECU take into account that the stored alarm may be earlier than
		//                the current time
		// -------------------------------------------------------------------------
		if (PublicData.storedData.speakingClock.enabled && 
				PublicData.storedData.speakingClock.nextAlarmTime != 0)
		{
			// ---------------------------------------------------------------------
			// 24/02/2014 ECU check that the stored alarm is in the future
			// ---------------------------------------------------------------------
			Calendar calendar = Calendar.getInstance ();
			
			long currentTime = calendar.getTimeInMillis();
			
			if (PublicData.storedData.speakingClock.nextAlarmTime > currentTime)
			{
				// -----------------------------------------------------------------
				// 24/02/2014 ECU the stored alarm is in the future so action it
				// -----------------------------------------------------------------
				// 23/02/2013 ECU try and recreate the alarm
				// 11/03/2017 ECU add the alarm ID as an argument
				// ------------------------------------------------------------------
				SetAnAlarm (theContext,PublicData.storedData.speakingClock.nextAlarmTime,StaticData.ALARM_ID_SPEAKING_CLOCK);
				// ------------------------------------------------------------------
				// 13/03/20117 ECU set the Westminster chime if needed
				// ------------------------------------------------------------------
				setWestminsterChimeAlarm (theContext,PublicData.storedData.speakingClock.nextAlarmTime);
				// ------------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 24/02/2014 ECU the alarm has expired so try and work out what to do
				// 01/09/2015 ECU changed to use StaticData
				// -----------------------------------------------------------------
				long nextAlarmTime = PublicData.storedData.speakingClock.nextAlarmTime;
				int  alarmGap      = (PublicData.storedData.speakingClock.gap * StaticData.MILLISECONDS_PER_MINUTE);
				
				while (nextAlarmTime <= currentTime)
				{
					// -------------------------------------------------------------
					// 24/02/2014 ECU add in the gap until past the current time
					// -------------------------------------------------------------
					nextAlarmTime  += alarmGap;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 24/02/2014 ECU have a time that is in the future - check if it is
				//                in the active period - if so set it as an alarm
				//                if not then reinitialise to the start time the
				//                following day
				//
				// 24/02/2014 ECU before calling RepeatTheAlarm need to step back
				//                '1 * alarmGap' because that method, which is a
				//                standard method, increments it before doing the tests
				// -----------------------------------------------------------------
				PublicData.storedData.speakingClock.nextAlarmTime = nextAlarmTime - alarmGap;
				// -----------------------------------------------------------------
				// 24/02/2014 ECU call the method which will check if in the correct range and
				//                take the appropriate action
				// -----------------------------------------------------------------
				RepeatTheAlarm (theContext);
				// -----------------------------------------------------------------
				// 13/03/2017 ECU set up the Westminster chime
				// -----------------------------------------------------------------
				setWestminsterChimeAlarm (theContext,PublicData.storedData.speakingClock.nextAlarmTime);
				// -----------------------------------------------------------------
			}
		}
	}
	/* ============================================================================= */
	public static void RepeatTheAlarm (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 23/02/2014 ECU called because speaking clock has finished one of its repeats
		//                check if the period for the speaking clock has finished
		//                if not then set a repeat timer
		//                if yes then set alarm for the start time
		// 24/02/2014 ECU rearrange because the time when an alarm is actioned can drift
		//                (particularly with KitKat) so base the nextAlarmTime on the time
		//                when the original alarm was initialised this is held in
		//				  PublicData.storedData.speakingClock.nextAlarmTime
		// -------------------------------------------------------------------------
		Calendar calendar = Calendar.getInstance ();
		// -------------------------------------------------------------------------
		// 23/02/2014 ECU get the time for the next possible alarm
		// 01/09/2015 ECU changed to use StaticData
		// -------------------------------------------------------------------------
		long nextAlarmTime = PublicData.storedData.speakingClock.nextAlarmTime  + 
				(PublicData.storedData.speakingClock.gap * StaticData.MILLISECONDS_PER_MINUTE);
		// -------------------------------------------------------------------------
		// 23/02/2014 ECU set 'calendar' so that it can be used later
		// -------------------------------------------------------------------------
		calendar.setTimeInMillis (nextAlarmTime);
		// -------------------------------------------------------------------------
		// 23/02/2014 ECU want to check if the next reminder is in the active period
		// -------------------------------------------------------------------------
		if (PublicData.storedData.speakingClock.IsActive (calendar.get (Calendar.HOUR_OF_DAY),
				                                          calendar.get (Calendar.MINUTE)))
		{
			// ---------------------------------------------------------------------
			// 23/02/2014 ECU speaking clock is still active to do a repeat alarm
			// 11/03/2017 ECU add the alarm ID as an argument
			// ---------------------------------------------------------------------
			SpeakingClockActivity.SetAnAlarm (theContext,nextAlarmTime,StaticData.ALARM_ID_SPEAKING_CLOCK);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 23/02/2014 ECU outside the active period so set an alarm for the start 
			//                time
			// ---------------------------------------------------------------------
			InitialiseTheAlarm (theContext);
			// ---------------------------------------------------------------------
		}	
	}
    /* ============================================================================= */
	public static void SetAnAlarm (Context theContext,long theTime,int theAlarmID)
	{
		// -------------------------------------------------------------------------
		// 23/02/2014 ECU added to create an alarm with the system manager
		// 10/11/2014 ECU changed to use Locale.getDefault instead of Locale.UK
		// 01/12/2015 ECU moved the alarm id SPEAKING_CLOCK into StaticData and rename
		//                ALARM_ID_SPEAKING_CLOCK
		// 28/10/2016 ECU changed to use dateFormatDDMMYY
		// 11/03/2017 ECU add the alarm ID as an argument and change the code accordingly
		// 24/07/2017 ECU changed to ALARM....
		// -------------------------------------------------------------------------
		Utilities.LogToProjectFile (TAG,"SetAnAlarm : " + theAlarmID + " " + new SimpleDateFormat (StaticData.ALARM_TIME_FORMAT +
				                          " " + PublicData.dateFormatDDMMYY,Locale.getDefault()).format(theTime));
		// --------------------------------------------------------------------------
		AlarmManager theAlarmManager = (AlarmManager)theContext.getSystemService(ALARM_SERVICE);
		Intent alarmIntent = new Intent(theContext, AlarmManagerReceiver.class);
		alarmIntent.putExtra (StaticData.PARAMETER_ALARM_ID,theAlarmID);
		// -------------------------------------------------------------------------
		// 26/07/2017 ECU because the alarms are not guaranteed to be received at
		//                exactly the right time then include the time in the intent
		// -------------------------------------------------------------------------
		alarmIntent.putExtra (StaticData.PARAMETER_ALARM_TIME,theTime);
		// -------------------------------------------------------------------------
		// 18/06/2013 ECU use alarmCounter as a unique request code - currently not used
		// 15/03/2019 ECU added PendingIntent.FLAG_UPDATE_CURRENT
		// 09/05/2020 ECU changed to use 'ALARM.....FLAGS'
		// -------------------------------------------------------------------------
		alarmPendingIntent = PendingIntent.getBroadcast (theContext,
														 theAlarmID,
														 alarmIntent,
														 StaticData.ALARM_PENDING_INTENT_FLAGS);
		// -------------------------------------------------------------------------
		// 24/12/2015 ECU changed to use the new method
		// -------------------------------------------------------------------------
		Utilities.SetAnExactAlarm (theAlarmManager,theTime, alarmPendingIntent);
		// -------------------------------------------------------------------------   
		// 23/02/2014 ECU store the time in case the program closes
		// 11/03/2017 ECU only do this if the alarm id indicates the speaking clock
		// -------------------------------------------------------------------------
		if (theAlarmID == StaticData.ALARM_ID_SPEAKING_CLOCK)
			PublicData.storedData.speakingClock.nextAlarmTime = theTime;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void setWestminsterChimeAlarm (Context theContext,long theTime)
	{
		// -------------------------------------------------------------------------
		// 13/03/2017 ECU created to set the Westminster chime corresponding to the
		//                specified time
		// -------------------------------------------------------------------------
		if (PublicData.storedData.speakingClock.westminsterChime)
		{
			// ---------------------------------------------------------------------
			// 13/03/2017 ECU want the minute that the specified time corresponds
			//                to
			// ---------------------------------------------------------------------
			Calendar calendar = Calendar.getInstance ();
			calendar.setTimeInMillis (theTime);
			int minute = calendar.get (Calendar.MINUTE);
			// ---------------------------------------------------------------------
			// 11/03/2017 ECU now work out which 'quarter' is to be used
			//                initially
			// ---------------------------------------------------------------------
			int minutesTillNextChime = ((minute / StaticData.WESTMINSTER_CHIME_GAP) * StaticData.WESTMINSTER_CHIME_GAP) +
					StaticData.WESTMINSTER_CHIME_GAP - minute;
			// ---------------------------------------------------------------------
			// 16/03/2019 ECU add remember this chime
			// ---------------------------------------------------------------------
			PublicData.westminsterChimeLast = theTime +  (long)(minutesTillNextChime * StaticData.MILLISECONDS_PER_MINUTE);
			// ---------------------------------------------------------------------
			// 11/03/2017 ECU get the time for the first chime
			// ---------------------------------------------------------------------
			// 11/03/2017 ECU set an alarm for the first chime
			// 16/03/2019 ECU changed to use 'westminsterChimeLast'
			// ---------------------------------------------------------------------
			SetAnAlarm (theContext,
						PublicData.westminsterChimeLast,
					    StaticData.ALARM_ID_WESTMINSTER_CHIME);
			// ---------------------------------------------------------------------		
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SpeakingClock (Context theContext,long theTime)
	{
		// -------------------------------------------------------------------------
		// 26/07/2017 ECU created to be called from the alarm receiver when the 
		//                current time is to be 'spoken'
		// -------------------------------------------------------------------------
		if (theTime == StaticData.NOT_SET)
		{
			// ---------------------------------------------------------------------
			// 26/07/2017 ECU a time has not been provided so use the current time
			// ---------------------------------------------------------------------
			Utilities.SpeakingClock (theContext);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 26/07/2017 ECU a time has been provided so base the spoken time on that
			// ---------------------------------------------------------------------
			Calendar localCalendar = Calendar.getInstance ();
			localCalendar.setTimeInMillis (theTime);
			// ---------------------------------------------------------------------
			// 26/07/2017 ECU speak the time based on that supplied
			// ---------------------------------------------------------------------
			Utilities.SpeakingClock (theContext,localCalendar);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	void UpdateDetails ()
	{
		startTime.clearFocus();
		stopTime.clearFocus();
		// -------------------------------------------------------------------------	
		// 09/02/2014 ECU pull in the new settings
		// 01/02/2017 ECU added the showText
		// 10/03/2017 ECU added the westminsterChimes
		// 25/07/2017 ECU added the westminsterChimesEnd
		// -------------------------------------------------------------------------
		PublicData.storedData.speakingClock 
							= new SpeakingClock (enable.isChecked (),
												 startTime.getCurrentHour (),
												 startTime.getCurrentMinute (),
												 stopTime.getCurrentHour (), 
												 stopTime.getCurrentMinute (),
												 Integer.parseInt (interval.getText().toString ()),
												 showText.isChecked (),
												 westminsterChimes.isChecked (),
												 westminsterChimesEnd);
		// -------------------------------------------------------------------------
		// 27/07/2017 ECU make sure that the 'time of last chime' is reset to 'not
		//                set' so that the previous speaking clock is not remembered
		// -------------------------------------------------------------------------
		PublicData.westminsterChimeLast = StaticData.NOT_SET;
		// -------------------------------------------------------------------------
		// 09/02/2014 ECU print details to confirm details
		// 22/02/2014 ECU added argument to centre the text
		// -------------------------------------------------------------------------
		Utilities.popToast (PublicData.storedData.speakingClock.Print(),true);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void westminsterChime (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 11/03/2017 ECU called up when an alarm has been received for the Westminster
		//                Chime
		// -------------------------------------------------------------------------
		// 11/03/2017 ECU before sounding this chime make sure the alarm is set
		//                for the next one, if the speaking clock is still active
		// -------------------------------------------------------------------------
		Calendar calendar = Calendar.getInstance ();
		// -------------------------------------------------------------------------
		// 25/07/2017 ECU if an adjustment was applied to the time to allow for the
		//                length of the westminster chime then use the 'unmodified'
		//                time
		// 27/07/2017 ECU changed check from NO_RESULT
		// -------------------------------------------------------------------------
		if (PublicData.westminsterChimeLast != StaticData.NOT_SET)
		{
			// ---------------------------------------------------------------------
			// 25/07/2017 ECU reset the time to that stored
			//            ECU added the 'clear' as was getting issues when checking
			//                the 'minute' lower down
			// ---------------------------------------------------------------------
			calendar.clear ();
			calendar.setTimeInMillis (PublicData.westminsterChimeLast);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 11/03/2017 ECU remember the hour which will be used later
		// -------------------------------------------------------------------------
		int hour 	= calendar.get (Calendar.HOUR);
		int minute	= calendar.get (Calendar.MINUTE);
		// -------------------------------------------------------------------------
		// 11/03/2017 ECU adjust because midnight and noon are '0' and we want 12
		//                hour 'gongs'
		// -------------------------------------------------------------------------
		if (hour == 0)
		{
			hour = 12;
		}
		// -------------------------------------------------------------------------
		// 11/03/2017 ECU get the time for the next chime
		// -------------------------------------------------------------------------
		long nextChimeTime = calendar.getTimeInMillis() + StaticData.WESTMINSTER_CHIME_GAP_MS;
		// -------------------------------------------------------------------------
		// 11/03/2017 ECU now check if the speaking clock is active
		// -------------------------------------------------------------------------
		calendar.setTimeInMillis (nextChimeTime);
		// -------------------------------------------------------------------------
		if (PublicData.storedData.speakingClock.IsActive (calendar.get(Calendar.HOUR_OF_DAY),calendar.get (Calendar.MINUTE)))
		{
			
			// ---------------------------------------------------------------------
			// 11/03/2017 ECU the speaking clock will still be active when the next
			//                chime will happen so set an alarm
			// 24/07/2017 ECU subtract the length of the chime so that it finishes,
			//                rather than starts, at the specified time.
			// 25/07/2017 ECU put in the check on ...ChimeEnd
			// ---------------------------------------------------------------------
			SetAnAlarm (theContext,nextChimeTime - 
					(PublicData.storedData.speakingClock.westminsterChimeEnd ? westminsterChimeLength (nextChimeTime) : 0),StaticData.ALARM_ID_WESTMINSTER_CHIME);
			// ---------------------------------------------------------------------
			// 25/07/2017 ECU remember the 'unmodified' next chime time
			// ---------------------------------------------------------------------
			PublicData.westminsterChimeLast = nextChimeTime;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 10/03/2017 ECU want Westminster chimes so decide which one to do
		// 25/07/2017 ECU changed to use 'minute' rather than getting from Calendar
		// -------------------------------------------------------------------------
		switch (minute)
		{	
			// ---------------------------------------------------------------------
			case 0:
				// -----------------------------------------------------------------
				// 11/03/2017 ECU the hour part of the raw file, which starts at
				//                WESTM.... position in the raw file, is to be repeated
				//                'hour' times
				// 12/03/2017 ECU changed to use MessageHandler because using the
				//                methods in Utilities directly seemed to interrupt
				//                the file being played randomly
				// -----------------------------------------------------------------
				MessageHandler.playRawResource (R.raw.westminster_hour,hour,StaticData.WESTMINSTER_CHIME_HOUR_POSITION);
				break;
			// ---------------------------------------------------------------------
			case 15:
				MessageHandler.playRawResource (R.raw.westminster_quarter_hour);
				break;
			// ---------------------------------------------------------------------
			case 30:
				MessageHandler.playRawResource (R.raw.westminster_half_hour);
				break;
			// ---------------------------------------------------------------------
			case 45:
				MessageHandler.playRawResource (R.raw.westminster_three_quarter_hour);
				break;
			// ---------------------------------------------------------------------
			default:
				break;
			// ---------------------------------------------------------------------
		}
		// ---------------------------------------------------------------------
	}
	// =============================================================================
	static int westminsterChimeLength (long theTime)
	{
		// -------------------------------------------------------------------------
		// 24/07/2017 ECU created to return the length of the Westminster Chime at
		//				  the specified time
		// -------------------------------------------------------------------------
		Calendar calendar = Calendar.getInstance ();
		// -------------------------------------------------------------------------
		// 24/07/2017 ECU set the calendar to the specified time
		// -------------------------------------------------------------------------
		calendar.setTimeInMillis (theTime);
		// -------------------------------------------------------------------------
		// 24/07/2017 ECU remember the 'hour' for later use
		// -------------------------------------------------------------------------
		int hour 	= calendar.get (Calendar.HOUR);
		// -------------------------------------------------------------------------
		// 24/07/2017 ECU adjust because midnight and noon are '0' and we want 12
		//                hour 'gongs'
		// -------------------------------------------------------------------------
		if (hour == 0)
		{
			hour = 12;
		}
		// -------------------------------------------------------------------------
		// 10/03/2017 ECU want Westminster chimes so decide which one to do
		// -------------------------------------------------------------------------
		switch (calendar.get (Calendar.MINUTE))
		{	
			// ---------------------------------------------------------------------
			case 0:
				// -----------------------------------------------------------------
				// 24/07/2017 ECU for the hour chime then the length depends on the
				//                number of hours
				// 26/07/2017 ECU take off the 'tail' (cannot do in StaticData because
				//                the position of the start of the 'hour gong' is
				//                hard coded and this would mess this up
				// -----------------------------------------------------------------
				return StaticData.WESTMINSTER_CHIME_HOUR + (calendar.get (Calendar.HOUR) * StaticData.WESTMINSTER_CHIME_PER_HOUR) - StaticData.WESTMINSTER_CHIME_TAIL_HOUR;
			// ---------------------------------------------------------------------
			case 15:
				return StaticData.WESTMINSTER_CHIME_QUARTER;
			// ---------------------------------------------------------------------
			case 30:
				return StaticData.WESTMINSTER_CHIME_HALF;
			// ---------------------------------------------------------------------
			case 45:
				return StaticData.WESTMINSTER_CHIME_THREE_QUARTER;
			// ---------------------------------------------------------------------
			default:
				break;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 24/07/2017 ECU if get here then do not return any length
		// -------------------------------------------------------------------------
		return 0;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public static void ChimeEndMethod (Object theObject)
	{
		// -------------------------------------------------------------------------
		// 26/07/2017 ECU indicate that the chimes are to end on the quarter hour
		// -------------------------------------------------------------------------
		westminsterChimesEnd = true;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void ChimeStartMethod (Object theObject)
	{
		// -------------------------------------------------------------------------
		// 26/07/2017 ECU indicate that the chimes are to start on the quarter hour
		// -------------------------------------------------------------------------
		westminsterChimesEnd = false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

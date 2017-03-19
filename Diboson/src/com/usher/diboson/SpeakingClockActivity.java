package com.usher.diboson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.os.Bundle;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.SeekBar.OnSeekBarChangeListener;

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
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//================================================================================
	/* =============================================================================== */
	final static String TAG = "SpeakingClockActivity";
	/* =============================================================================== */
	
	/* =============================================================================== */
	static	PendingIntent 		alarmPendingIntent;						// 23/02/2014 ECU added
			CheckBox			enable;
			Button				confirmDetails;
			TextView			interval;
			SeekBar				intervalSeekBar;						// 14/03/2017 ECU added
			CheckBox			showText;								// 01/02/2017 ECU added
			TimePicker			startTime;	
			TimePicker			stopTime;
			CheckBox			westminsterChimes;						// 10/03/2017 ECU added
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
			setContentView(R.layout.activity_speaking_clock);
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
					
					InitialiseTheAlarm (getBaseContext());
					// -------------------------------------------------------------
					// 09/02/2014 ECU terminate this activity
					// -------------------------------------------------------------
					finish ();
							
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

	    inputMethodManager.hideSoftInputFromWindow(startTime.getWindowToken(),0);
	    inputMethodManager.hideSoftInputFromWindow(stopTime.getWindowToken(),0);
	    // ---------------------------------------------------------------------------

	    super.onPause(); 
	} 
	/* =============================================================================== */
	@Override 
	protected void onResume() 
	{ 	
	   	super.onResume(); 
	}
	/* =============================================================================== */
	private CompoundButton.OnCheckedChangeListener enableListener = new CompoundButton.OnCheckedChangeListener() 
	{		
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) 
		{
			// 09/02/2014 ECU display / hide the fields depending on whether the clock is enabled
			//                or not
			// 13/02/2014 ECU placed the code in new method
			
			HideDisplayFields (isChecked);
		}
	};
	/* =============================================================================== */
	public static void CancelAlarm (Context theContext)
	{
		// ---------------------------------------------------------------------------
		// 24/02/2014 ECU created - called to cancel the alarm associated with the
		//                speaking clock
		// 01/01/2016 ECU changed to use the new method
		// 03/11/2016 ECU add the false to indicate no pending intent cancel
		// ---------------------------------------------------------------------------
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
			// 24/02/2014 ECU check that the staored alarm is in the future
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
		//				  MainActivity.storedData.speakingClock.nextAlarmTime
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
		calendar.setTimeInMillis( nextAlarmTime);
		// -------------------------------------------------------------------------
		// 23/02/2014 ECU want to check if the next reminder is in the active period
		// -------------------------------------------------------------------------
		if (PublicData.storedData.speakingClock.IsActive (calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE)))
		{
			// ---------------------------------------------------------------------
			// 23/02/2014 ECU speaking clock is still active to do a repeat alarm
			// 11/03/2017 ECU add the alarm ID as an argument
			// ---------------------------------------------------------------------
			SpeakingClockActivity.SetAnAlarm (theContext,nextAlarmTime,StaticData.ALARM_ID_SPEAKING_CLOCK);
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
		// -------------------------------------------------------------------------
		Utilities.LogToProjectFile (TAG,"SetAnAlarm : " + theAlarmID + " " + new SimpleDateFormat("HH:mm:ss " + PublicData.dateFormatDDMMYY,Locale.getDefault()).format(theTime));
		// --------------------------------------------------------------------------
		AlarmManager theAlarmManager = (AlarmManager)theContext.getSystemService(ALARM_SERVICE);
		Intent alarmIntent = new Intent(theContext, AlarmManagerReceiver.class);
		alarmIntent.putExtra (StaticData.PARAMETER_ALARM_ID,theAlarmID);
		// -------------------------------------------------------------------------
		// 18/06/2013 ECU use alarmCounter as a unique request code - currently not used
		// -------------------------------------------------------------------------
		alarmPendingIntent = PendingIntent.getBroadcast (theContext,
									theAlarmID,alarmIntent,Intent.FLAG_ACTIVITY_NEW_TASK); 
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
			// 11/03/2017 ECU get the time for the first chime
			// ---------------------------------------------------------------------
			// 11/03/2017 ECU set an alarm for the first chime
			// ---------------------------------------------------------------------
			SetAnAlarm (theContext,
					    theTime +  (long)(minutesTillNextChime * StaticData.MILLISECONDS_PER_MINUTE),
					    StaticData.ALARM_ID_WESTMINSTER_CHIME);
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
		// -------------------------------------------------------------------------
		PublicData.storedData.speakingClock 
			= new SpeakingClock (enable.isChecked(),
							     startTime.getCurrentHour(),startTime.getCurrentMinute(),
					             stopTime.getCurrentHour(),stopTime.getCurrentMinute(),
					             Integer.parseInt (interval.getText().toString()),
					             showText.isChecked(),
					             westminsterChimes.isChecked());
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
		// 11/03/2017 ECU remember the hour which will be used later
		// -------------------------------------------------------------------------
		int hour = calendar.get (Calendar.HOUR);
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
		if (PublicData.storedData.speakingClock.IsActive (calendar.get(Calendar.HOUR_OF_DAY),calendar.get (Calendar.MINUTE)))
		{
			// ---------------------------------------------------------------------
			// 11/03/2017 ECU the speaking clock will still be active when the next
			//                chime will happen so set an alarm
			// ---------------------------------------------------------------------
			SetAnAlarm (theContext,nextChimeTime,StaticData.ALARM_ID_WESTMINSTER_CHIME);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 10/03/2017 ECU want Westminster chimes so decide which one to do
		// -------------------------------------------------------------------------
		switch ((Calendar.getInstance()).get (Calendar.MINUTE))
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
}

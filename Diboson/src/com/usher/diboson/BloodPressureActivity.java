package com.usher.diboson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

//----------------------------------------------------------------------------------
// 27/08/2017 ECU The following suppress was added because the class uses Gravity.LEFT
//                and this generates a warning stating that Gravity.START should be
//                used. However '.START' came in at API 14 and this is later than the
//                lowest API that this app is to support
//----------------------------------------------------------------------------------
@SuppressLint ("RtlHardcoded") 
// ---------------------------------------------------------------------------------

public class BloodPressureActivity extends DibosonActivity 
{
	// =============================================================================
	// 26/08/2017 ECU created to handle all aspects of the blood pressure activity
	//            ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 31/08/2017 ECU added the 'countdown timer'
	// 05/09/2017 ECU changed the code to use the new PlayRawFile class rather than
	//                methods that used to be in Utilities
	// 28/09/2017 ECU warn user that BMI cannot be generated without a height
	// 23/03/2019 ECU display the accepted BMI ranges
	// =============================================================================
	
	// =============================================================================
	public final static int NUMBER_OF_READINGS		=   3;
	// -----------------------------------------------------------------------------
	final static int COUNTDOWN_TIMER_LENGTH	=   30;
	final static int DISPLAY_INTERVAL		=	1000;
	final static int TIME_BETWEEN_READINGS	=	3 * StaticData.MILLISECONDS_PER_MINUTE;
	final static int VALIDATE_CMS			=	0;
	final static int VALIDATE_DIASTOLIC		=	1;
	final static int VALIDATE_FEET			=	2;
	final static int VALIDATE_HEART_RATE	=	3;
	final static int VALIDATE_INCHES		=	4;
	final static int VALIDATE_KILOGRAMS		=	5;
	final static int VALIDATE_POUNDS		=	6;
	final static int VALIDATE_STONES		=	7;
	final static int VALIDATE_SYSTOLIC		=	8;
	// -----------------------------------------------------------------------------
	final static int DIASTOLIC_LOW			=   30;
	final static int DIASTOLIC_HIGH			=   110;
	final static int SYSTOLIC_LOW			=   60;
	final static int SYSTOLIC_HIGH			=   210;
	// =============================================================================
	
	// =============================================================================
	TextView				bloodPressureCounter;
	BloodPressureHandler	bloodPressureHandler;
	int						heightCms;
	int						heightFeet;
	int						heightInches;
	int						diastolic;
	int						heartRate;
	PlayRawFile				playRawFile;
	boolean					processTextChange;
	int						systolic;
	int						reading;
	BloodPressureAndWeight	weight;
	// ----------------------------------------------------------------------------
	TextView				bmiField;
	TextView				bmiRangesField;			// 23/03/2019 ECU added
	EditText 				cmsField;
	EditText 				feetField;
	EditText 				inchesField;
	EditText 				kilogramsField;
	EditText 				poundsField;
	EditText 				stonesField;
	// =============================================================================
	
	// =============================================================================
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 26/08/2017 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			super.onCreate (savedInstanceState);
			// ---------------------------------------------------------------------
			// 26/08/2017 ECU set the screen up for this activity
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			// ---------------------------------------------------------------------
			// 27/08/2017 ECU check if any arguments have been supplied
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
			// ---------------------------------------------------------------------
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 06/10/2016 ECU check for grouping of activities
				// -----------------------------------------------------------------
				if (extras.getBoolean (StaticData.PARAMETER_DISPLAY,false))
				{
					// -------------------------------------------------------------
					// 27/08/2017 ECU have a request to show the currently stored
					//                values
					// 05/09/2017 ECU changed to use dedicated layout
					// -------------------------------------------------------------
					setContentView (R.layout.blood_pressure_summary);
					// -------------------------------------------------------------
					final TextView summaryTextview  = (TextView) findViewById (R.id.summary_textview);
					// -------------------------------------------------------------
					// 27/08/2017 ECU set the maximum lines of the information field
					//            ECU set monospacing and left adjusted
					// 05/09/2017 ECU set up the scrolling
					// -------------------------------------------------------------
					summaryTextview.setMaxLines (StaticData.SYSTEM_INFO_MAX_LINES);
					summaryTextview.setTypeface (Typeface.MONOSPACE); 
					summaryTextview.setGravity  (Gravity.LEFT);
					summaryTextview.setMovementMethod (new ScrollingMovementMethod ());
					// -------------------------------------------------------------
					// 30/08/2017 ECU adjust the font size
					// -------------------------------------------------------------
					summaryTextview.setTextSize (TypedValue.COMPLEX_UNIT_PX,
			                 getResources ().getDimension (R.dimen.data_font));
					// -------------------------------------------------------------
					// 27/08/2017 ECU read the contents of the summary file
					// -------------------------------------------------------------
					final byte [] bytesRead = Utilities.readLinesFromEndOfFile (PublicData.projectFolder + getString (R.string.blood_pressure_file) ,StaticData.SYSTEM_INFO_MAX_LINES);
					// -------------------------------------------------------------
					// 27/08/2017 ECU check if there are any entries and give correct
					//                display
					// -------------------------------------------------------------
					if (bytesRead != null)
					{
						// ---------------------------------------------------------
						// 27/08/2017 ECU display the contents of the file
						// 30/09/2020 ECU scroll to the end of the displayed data
						//                used to be just :-
						//                  summaryTextview.setText (new String (bytesRead));
						// ---------------------------------------------------------
						SpannableString spannableData = new SpannableString (new String (bytesRead));
						Selection.setSelection (spannableData, spannableData.length());
						summaryTextview.setText (spannableData, TextView.BufferType.SPANNABLE);
						// ---------------------------------------------------------
						// 05/09/2017 ECU decide whether the 'mail button' is to be
						//                displayed
						// ---------------------------------------------------------
						ImageButton sendMailButton= (ImageButton) findViewById (R.id.sendMailButton);
						if (PublicData.emailDetails.enabled)
						{
							// -----------------------------------------------------
							// 05/09/2017 ECU the button by default is visible so
							//                just process it
							// -----------------------------------------------------
							// 05/09/2017 ECU tell the user about the mail button
							// 30/09/2020 ECU changed from 'popToast'
							// -----------------------------------------------------
							Utilities.SpeakAPhraseAndDisplay (String.format (getString (R.string.blood_pressure_email_format),
										PublicData.emailDetails.recipients));
							// ----------------------------------------------------
							// 01/10/2020 ECU warn about needing to do a 'long click'
							//                to send the readings
							// -----------------------------------------------------
							sendMailButton.setOnClickListener (new View.OnClickListener()
							{
								@Override
								public void onClick (View theView)
								{
									// ---------------------------------------------
									Utilities.SpeakAPhraseAndDisplay (getString (R.string.blood_pressure_mail_warning));
									// ---------------------------------------------
								}
							});
							// -----------------------------------------------------
							// 05/09/2017 ECU check on the handling of the send mail
							//                button
							// -----------------------------------------------------
							sendMailButton.setOnLongClickListener (new View.OnLongClickListener() 
							{
								@Override
								public boolean onLongClick (View view) 
								{	
									// ---------------------------------------------
									// 05/09/2017 ECU check if there is a network
									//                available
									// ---------------------------------------------
									if (Utilities.checkForNetwork (getBaseContext ()))
									{
										// -----------------------------------------
										// 05/09/2017 ECU there is a network
										// -----------------------------------------
										// 05/09/2017 ECU send the email
										// -----------------------------------------
										Utilities.SendEmailMessage (getBaseContext (),
												getString (R.string.blood_pressure_subject),new String (bytesRead),true);	
										// -----------------------------------------
										// 05/09/2017 ECU confirm the send of the email
										// -----------------------------------------
										Utilities.popToast(String.format (getString (R.string.blood_pressure_email_confirmation_format),
												PublicData.emailDetails.recipients),true);
										// -----------------------------------------
									}
									else
									{
										// -----------------------------------------
										// 05/09/2017 ECU there is no network available
										// -----------------------------------------
										Utilities.popToastAndSpeak (getString (R.string.email_no_network));
										// -----------------------------------------
									}
									return true;
								}
							});
							// -----------------------------------------------------
						}
						else
						{
							// -----------------------------------------------------
							// 05/09/2017 ECU the email system is disabled so hide
							//                the 'send mail' button
							// -----------------------------------------------------
							sendMailButton.setVisibility (View.INVISIBLE);
							// -----------------------------------------------------
						}
					}
					else
					{
						// ---------------------------------------------------------
						// 27/08/2017 ECU tell the user that there are no stored readings
						// ---------------------------------------------------------
						Utilities.popToastAndSpeak(getString (R.string.blood_pressure_file_empty), true);
						// ---------------------------------------------------------
						// 27/08/2017 ECU just terminate this activity
						// ---------------------------------------------------------
						finish ();
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				setContentView (R.layout.activity_blood_pressure);
				// -----------------------------------------------------------------
				// 26/08/2017 ECU tell the user what to do
				// -----------------------------------------------------------------
				Utilities.SpeakAPhrase (this,getString (R.string.blood_pressure_summary));
				// -----------------------------------------------------------------
				// 26/08/2017 ECU set up the listeners for the buttons
				// 27/08/2017 ECU added 'weight only'
				// 11/09/2017 ECU added 'height only'
				// -----------------------------------------------------------------
				((Button) findViewById (R.id.blood_pressure_cancel)).setOnClickListener (buttonListener);
				((Button) findViewById (R.id.blood_pressure_continue)).setOnClickListener (buttonListener);
				((Button) findViewById (R.id.height_only)).setOnClickListener (buttonListener);
				((Button) findViewById (R.id.weight_only)).setOnClickListener (buttonListener);
				// -----------------------------------------------------------------
				// 28/08/2017 ECU initialise the handler
				// -----------------------------------------------------------------
				bloodPressureHandler = new BloodPressureHandler ();
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 26/08/2017 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}				
	}
	// =============================================================================
	
	// =============================================================================
	@Override
	public void onBackPressed ()
	{
		// -------------------------------------------------------------------------
		// 26/08/2017 ECU created to detect the BACK key and to finish the current
		//                activity
		// -------------------------------------------------------------------------
		// 04/09/2017 ECU try and stop the handler processing
		// 05/09/2017 ECU put in the check on 'null' because could get here
		//                from the 'long press'
		// -------------------------------------------------------------------------
		if (bloodPressureHandler != null)
			bloodPressureHandler.removeMessages (StaticData.MESSAGE_PROCESS);
		// -------------------------------------------------------------------------
		// 04/09/2017 ECU stop any 'raw music' that may be being played
		// 05/09/2017 ECU changed to use the new class
		//            ECU changed to use new method which checks if the object
		//                exists
		// -------------------------------------------------------------------------
		PlayRawFile.Stop (playRawFile);
		// -------------------------------------------------------------------------
		// 02/09/2017 ECU flush any speech that may be 'playing' or queued to 'play'
		// -------------------------------------------------------------------------
		TextToSpeechService.Flush ();
		finish ();
		// -------------------------------------------------------------------------
		// 26/08/2017 ECU now call the super for this method
		// -------------------------------------------------------------------------
		super.onBackPressed();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	private View.OnClickListener buttonListener = new View.OnClickListener () 
	{
		// -------------------------------------------------------------------------
		@Override
		public void onClick (View view) 
		{	
			// ----------------------------------------------------------------------
			// 28/08/2017 ECU stop any speaking that is occurring
			// ----------------------------------------------------------------------
			TextToSpeechService.Flush ();
			//----------------------------------------------------------------------
			// 07/01/2014 ECU now process depending on which button pressed
			//----------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.blood_pressure_cancel:
				{
					//--------------------------------------------------------------
					// 26/08/2017 ECU just finish this activity
					// -------------------------------------------------------------
					finish ();
					// -------------------------------------------------------------		
					break;
				}
				// -----------------------------------------------------------------
				case R.id.blood_pressure_continue:
				{
					// -------------------------------------------------------------
					// 27/08/2017 ECU indicate that we want the first reading
					// -------------------------------------------------------------
					reading = 0;
					// -------------------------------------------------------------
					// 27/08/2017 ECU indicate that text change listener is to process
					// -------------------------------------------------------------
					processTextChange = true;
					// -------------------------------------------------------------
					// 27/08/2017 ECU create object for the input data
					// -------------------------------------------------------------
					weight = new BloodPressureAndWeight ();
					// -------------------------------------------------------------
					// 26/08/2017 ECU start getting the readings
					// -------------------------------------------------------------
					getTheBloodPressure ();
					// -------------------------------------------------------------
					break;
				}
				// -----------------------------------------------------------------
				case R.id.enter_height:
					// -------------------------------------------------------------
					// 11/09/2017 ECU just exit this activity after storing the
					//                entered height
					// -------------------------------------------------------------
					PublicData.storedData.height = heightCms;
					finish ();
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.enter_reading:
				{
					// -------------------------------------------------------------
					// 26/08/2017 ECU called when blood pressure has been read
					// -------------------------------------------------------------
					weight.systolic  [reading] = systolic;
					weight.diastolic [reading] = diastolic;
					weight.heartRate [reading] = heartRate;
					// -------------------------------------------------------------
					// 27/08/2017 ECU check if all readings received yet
					// -------------------------------------------------------------
					reading++;
					if (reading < NUMBER_OF_READINGS)
					{
						// ---------------------------------------------------------
						// 27/08/2017 ECU give warning about another reading
						// ---------------------------------------------------------
						bloodPressureReadingNext ();
						// ---------------------------------------------------------
 					}
					else
					{
						getTheWeight ();
					}
					// -------------------------------------------------------------
					break;
				}
				// -----------------------------------------------------------------
				case R.id.enter_weight:
				{
					// -------------------------------------------------------------
					// 26/08/2017 ECU start getting the readings
					// -------------------------------------------------------------
					Utilities.AppendToFile (PublicData.projectFolder + getString (R.string.blood_pressure_file), 
						    				Utilities.getAdjustedTime(PublicData.dateFormatterFull) + "  " + 
						    				weight.Print () + StaticData.NEWLINE);
					// -------------------------------------------------------------
					// 27/08/2017 ECU tell the user what has happened
					// 28/08/2017 ECU put in the check on whether the pressure has been read
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString (R.string.blood_pressure_finished) +
							(weight.pressureSupplied() ? getString (R.string.blood_pressure_finished_cuff) : "."),true);
					// -------------------------------------------------------------
					// 27/08/2017 ECU can now finish the activity
					// -------------------------------------------------------------
					finish ();
					// -------------------------------------------------------------
					break;
				}
				// -----------------------------------------------------------------
				case R.id.height_only:
					// -------------------------------------------------------------
					// 11/09/2017 ECU the user only wants to supply the height
					// -------------------------------------------------------------
					// 11/09/2017 ECU get the user to input the height
					// -------------------------------------------------------------
					getTheHeight ();
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.weight_only:
					// -------------------------------------------------------------
					// 27/08/2017 ECU the user only wants to supply the weight
					// -------------------------------------------------------------
					// 27/08/2017 ECU indicate that text change listener is to process
					// -------------------------------------------------------------
					processTextChange = true;
					// -------------------------------------------------------------
					// 27/08/2017 ECU create object for the input data
					// -------------------------------------------------------------
					weight = new BloodPressureAndWeight ();
					// -------------------------------------------------------------
					// 27/08/2017 ECU indicate that no weights have been given
					// -------------------------------------------------------------
					weight.diastolic = null;
					// -------------------------------------------------------------
					// 27/08/2017 ECU get the user to input the weight
					// -------------------------------------------------------------
					getTheWeight ();
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class BloodPressureHandler extends Handler 
	{
		// -------------------------------------------------------------------------
		// 28/08/2017 ECU handle incoming messages
		// -------------------------------------------------------------------------
		@Override
	    public void handleMessage (Message message) 
		{	
			switch (message.what) 
			{
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_PROCESS:
					// -------------------------------------------------------------
					// 28/08/2017 ECU get the current counter
					// -------------------------------------------------------------
					int counter = message.arg1 - DISPLAY_INTERVAL;
					// -------------------------------------------------------------
					// 28/08/2017 ECU check if still a counter to display
					// -------------------------------------------------------------
					if (counter > 0)
					{
						// ---------------------------------------------------------
						// 28/08/2017 ECU now ask the counter to be displayed
						// ---------------------------------------------------------
						Message localMessage = obtainMessage (StaticData.MESSAGE_PROCESS,counter,StaticData.NOT_SET);
						sendMessageDelayed (localMessage,DISPLAY_INTERVAL);
						// ---------------------------------------------------------
						// 28/08/2017 ECU display the counter - in seconds
						// ---------------------------------------------------------
						counter = counter / 1000;
						bloodPressureCounter.setText (String.format (getString (R.string.blood_pressure_counter_format),counter) +
																								Utilities.AddAnS (counter));
						// ---------------------------------------------------------
						// 31/08/2017 ECU check if time to start the countdown timer
						// 04/09/2017 ECU removed as it is incorporated in
						//                'blood_pressure_music'
						// ---------------------------------------------------------
						//if (counter == COUNTDOWN_TIMER_LENGTH)
						//	Utilities.playRawResource (getBaseContext (),R.raw.countdown_timer);
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
		            	// 28/08/2017 ECU it is time to get the next set of readings
		            	// ---------------------------------------------------------
						// 27/08/2017 ECU ready for the next reading
						// ---------------------------------------------------------
						getTheBloodPressureReady ();
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					break;
	            // -----------------------------------------------------------------
	            default:
	            	// -------------------------------------------------------------
	            	// 28/08/2017 ECU ignore any 'non specified' messages
	            	// -------------------------------------------------------------
	            	break;
	            // -----------------------------------------------------------------
	        }
		}
	}
	// =============================================================================
	void bloodPressureReadingNext ()
	{
		// -------------------------------------------------------------------------
		// 27/08/2017 ECU created to tell the user that another reading needs to be
		//                taken after a short delay
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak(String.format (getString (R.string.blood_pressure_next_format),
									getString ((reading != (NUMBER_OF_READINGS - 1)) ? R.string.reading_another 
															  						 : R.string.reading_last)),true);
		// -------------------------------------------------------------------------
		//28/08/2017 ECU remove the keyboard from the screen
		// -------------------------------------------------------------------------
		((InputMethodManager) getSystemService (Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		// -------------------------------------------------------------------------
		// 28/08/2017 ECU display a 'wait and relax' screen
		// -------------------------------------------------------------------------
		setContentView (R.layout.blood_pressure_input_wait);
		bloodPressureCounter = (TextView) findViewById (R.id.blood_pressure_counter);
		// -------------------------------------------------------------------------
		// 28/08/2017 ECU set up a display of the time to wait
		// -------------------------------------------------------------------------
		Message message = bloodPressureHandler.obtainMessage (StaticData.MESSAGE_PROCESS,TIME_BETWEEN_READINGS,StaticData.NOT_SET);
		bloodPressureHandler.sendMessage (message);
		// -------------------------------------------------------------------------
		// 03/09/2017 ECU start playing some relaxing music
		//            ECU this music should stop before the 'countdown timer' begins.
		//                Could combine them but leave as is because makes more
		//                sense logically
		// 04/09/2017 ECU decided to combine the relaxing music and countdown timer
		//                into one piece because if music is playing in the background
		//                when this activity is started then it will be 'resumed' in
		//                the gap between 'relaxing music' finishing and the 'countdown
		//                timer' starting which does not sound very good.
		// 05/09/2017 ECU change to use the new class rather than the method that
		//                used to be in Utilities
		// -------------------------------------------------------------------------
		playRawFile = new PlayRawFile (getBaseContext (),R.raw.blood_pressure_music);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	float BMI (float theWeightKilos,int theHeightCms)
	{
		// -------------------------------------------------------------------------
		// 11/09/2017 ECU return the BMI which is defined as :-
		//
		//       (weight in kilos) /(height in metres squared)
		// -------------------------------------------------------------------------
		float localHeight = (float) theHeightCms / 100.0f;
		// -------------------------------------------------------------------------
		// 11/09/2017 ECU return the calculated BMI
		// -------------------------------------------------------------------------
		return theWeightKilos / (localHeight * localHeight);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void bmiDisplay ()
	{
		// -------------------------------------------------------------------------
		// 28/09/2017 ECU changed to '>0' rather than '!= ....NOT_SET'
		// -------------------------------------------------------------------------
		if (PublicData.storedData.height > 0)
		{
			// ---------------------------------------------------------------------
			// 11/09/2017 ECU show the calculated BMI
			// ---------------------------------------------------------------------
			bmiField.setText (String.format ("BMI : %.2f",BMI (weight.kilograms,PublicData.storedData.height)));
			// ---------------------------------------------------------------------
			// 23/03/2019 ECU show the BMI ranges
			// ---------------------------------------------------------------------
			bmiRangesField.setVisibility (View.VISIBLE);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 28/09/2017 ECU tell user that height needed in order to obtain BMI
			// ---------------------------------------------------------------------
			bmiField.setTextSize (TypedValue.COMPLEX_UNIT_DIP,getResources().getDimension(R.dimen.default_font_size_smaller));
			bmiField.setText (getString (R.string.bmi_height_needed));
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	void getTheBloodPressure ()
	{
		// -------------------------------------------------------------------------
		// 27/08/2017 ECU tell the user to relax, ....
		// -------------------------------------------------------------------------
		if (reading == 0)
			Utilities.popToastAndSpeak (getString (R.string.blood_pressure_warning),true);
		// -------------------------------------------------------------------------
		setContentView (R.layout.blood_pressure_input);
		// -------------------------------------------------------------------------
		final EditText diastolic = (EditText) findViewById(R.id.diastolic);
		final EditText heartRate = (EditText) findViewById(R.id.heart_rate);
		final EditText systolic  = (EditText) findViewById(R.id.systolic);
		// -------------------------------------------------------------------------
		// 27/08/2017 ECU try and force the display of the keyboard which was not
		//                happening until the field was 'touched'
		// --------------------------------------------------------------------------
		((InputMethodManager) getSystemService (Context.INPUT_METHOD_SERVICE)).showSoftInput (systolic,InputMethodManager.SHOW_IMPLICIT);
		// -------------------------------------------------------------------------
		((Button) findViewById (R.id.enter_reading)).setOnClickListener (buttonListener);
		// -------------------------------------------------------------------------
		diastolic.addTextChangedListener(new TextWatcher() 
		{ 
			@Override
			public void afterTextChanged(Editable theData) 
			{
				if (processTextChange)
				{
					if (validate (VALIDATE_DIASTOLIC,theData))
						heartRate.requestFocus();
				}
			}
			// ---------------------------------------------------------------------
			@Override
			public void beforeTextChanged (CharSequence s, int start, int count,int after) 
			{
			}
			// ---------------------------------------------------------------------
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{	
			}
			// ---------------------------------------------------------------------	
        });
		heartRate.addTextChangedListener(new TextWatcher() 
		{ 
			@Override
			public void afterTextChanged(Editable theData) 
			{
				if (processTextChange)
				{
					validate (VALIDATE_HEART_RATE,theData);
				}
			}
			// ---------------------------------------------------------------------
			@Override
			public void beforeTextChanged (CharSequence s, int start, int count,int after) 
			{
			}
			// ---------------------------------------------------------------------
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{	
			}
			// ---------------------------------------------------------------------	
        });
		systolic.addTextChangedListener(new TextWatcher() 
		{ 
			@Override
			public void afterTextChanged(Editable theData) 
			{
				if (processTextChange)
				{
					if (validate (VALIDATE_SYSTOLIC,theData))
						diastolic.requestFocus();
				}
			}
			// ---------------------------------------------------------------------
			@Override
			public void beforeTextChanged (CharSequence s, int start, int count,int after) 
			{
			}
			// ---------------------------------------------------------------------
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{	
			}
			// ---------------------------------------------------------------------	
        });
	}
	// =============================================================================
	void getTheBloodPressureReady ()
	{
		// -------------------------------------------------------------------------
		// 27/08/2017 ECU the next reading is ready to be taken
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak (getString (R.string.blood_pressure_next_notice),true);
		Utilities.popToastAndSpeak (getString (R.string.blood_pressure_warning),true);
		// -------------------------------------------------------------------------
		// 28/08/2017 ECU now request the next reading
		// -------------------------------------------------------------------------
		getTheBloodPressure ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void getTheHeight ()
	{
		// -------------------------------------------------------------------------
		setContentView (R.layout.height_input);
		// -------------------------------------------------------------------------
		cmsField 	 = (EditText) findViewById (R.id.height_cms_input);
		feetField 	 = (EditText) findViewById (R.id.height_feet_input);
		inchesField  = (EditText) findViewById (R.id.height_inches_input);
		// -------------------------------------------------------------------------
		// 11/09/2017 ECU initialise the fields
		// -------------------------------------------------------------------------
		if (PublicData.storedData.height != StaticData.NOT_SET)
		{
			heightCms = PublicData.storedData.height;
			cmsField.setText (StaticData.BLANK_STRING + heightCms);
			setHeight (heightCms);
		}
		// -------------------------------------------------------------------------
		// 11/09/2017 ECU try and force the display of the keyboard which was not
		//                happening until the field was 'touched'
		// --------------------------------------------------------------------------
		((InputMethodManager) getSystemService (Context.INPUT_METHOD_SERVICE)).showSoftInput 
								(feetField,InputMethodManager.SHOW_IMPLICIT);
		// -------------------------------------------------------------------------
		((Button)findViewById (R.id.enter_height)).setOnClickListener (buttonListener);
		// -------------------------------------------------------------------------
		// 11/09/2017 ECU indicate that text change listener is to process
		// -------------------------------------------------------------------------
		processTextChange = true;
		// -------------------------------------------------------------------------
		cmsField.addTextChangedListener(new TextWatcher() 
		{ 
			@Override
			public void afterTextChanged(Editable theData) 
			{
				if (processTextChange)
				{
					validate (VALIDATE_CMS,theData);
				}
			}
			// ---------------------------------------------------------------------
			@Override
			public void beforeTextChanged (CharSequence s, int start, int count,int after) 
			{
			}
			// ---------------------------------------------------------------------
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{	
			}
			// ---------------------------------------------------------------------	
		});
		feetField.addTextChangedListener(new TextWatcher() 
		{ 
			@Override
			public void afterTextChanged(Editable theData) 
			{			
				if (processTextChange)
				{
					validate (VALIDATE_FEET,theData);
				}
			}
			// ---------------------------------------------------------------------
			@Override
			public void beforeTextChanged (CharSequence s, int start, int count,int after) 
			{
			}
			// ---------------------------------------------------------------------
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{	
			}
			// ---------------------------------------------------------------------	
		});
		inchesField.addTextChangedListener(new TextWatcher() 
		{ 
			@Override
			public void afterTextChanged(Editable theData) 
			{
				if (processTextChange)
				{
					validate (VALIDATE_INCHES,theData);
				}
			}
			// ---------------------------------------------------------------------
			@Override
			public void beforeTextChanged (CharSequence s, int start, int count,int after) 
			{
			}
			// ---------------------------------------------------------------------
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{	
			}
			// ---------------------------------------------------------------------	
		});
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void getTheWeight ()
	{
		// -------------------------------------------------------------------------
		// 30/08/2017 ECU tell the user what is going on
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak(getString (R.string.weight_message),true);
		// -------------------------------------------------------------------------
		setContentView (R.layout.weight_input);
		// -------------------------------------------------------------------------
		// 23/03/2019 ECU added bmi ranges
		// -------------------------------------------------------------------------
		bmiField		 = (TextView) findViewById (R.id.bmi);
		bmiRangesField	 = (TextView) findViewById (R.id.bmi_ranges);
		kilogramsField 	 = (EditText) findViewById (R.id.weight_kilograms_input);
		poundsField 	 = (EditText) findViewById (R.id.weight_pounds_input);
		stonesField 	 = (EditText) findViewById (R.id.weight_stones_input);
		// -------------------------------------------------------------------------
		// 27/08/2017 ECU set the focus to the required field
		// -------------------------------------------------------------------------
		if (PublicData.storedData.weight_metric)
			kilogramsField.requestFocus ();
		else
			stonesField.requestFocus ();
		// -------------------------------------------------------------------------
		// 27/08/2017 ECU try and force the display of the keyboard which was not
		//                happening until the field was 'touched'
		// --------------------------------------------------------------------------
		((InputMethodManager) getSystemService (Context.INPUT_METHOD_SERVICE)).showSoftInput 
				((PublicData.storedData.weight_metric ? kilogramsField : stonesField),InputMethodManager.SHOW_IMPLICIT);
		// -------------------------------------------------------------------------
		((Button)findViewById (R.id.enter_weight)).setOnClickListener (buttonListener);
		// -------------------------------------------------------------------------
		// -------------------------------------------------------------------------
		kilogramsField.addTextChangedListener(new TextWatcher() 
		{
			// ---------------------------------------------------------------------
			@Override
			public void afterTextChanged (Editable theData) 
			{
				// -----------------------------------------------------------------
				if (processTextChange)
				{
					validate (VALIDATE_KILOGRAMS,theData);
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			@Override
			public void beforeTextChanged (CharSequence s, int start, int count,int after) 
			{
			}
			// ---------------------------------------------------------------------
			@Override
			public void onTextChanged (CharSequence s, int start, int before, int count)
			{	
			}
			// ---------------------------------------------------------------------	
        });
        // -------------------------------------------------------------------------
        // -------------------------------------------------------------------------
		poundsField.addTextChangedListener (new TextWatcher ()
		{
			// ---------------------------------------------------------------------
			@Override
			public void afterTextChanged (Editable theData)
			{	
				// -----------------------------------------------------------------
				if (processTextChange)
				{
					validate (VALIDATE_POUNDS,theData);
				}
				// ------------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			@Override
			public void beforeTextChanged (CharSequence s, int start, int count,int after) 
			{
			}
			// ---------------------------------------------------------------------
			@Override
			public void onTextChanged (CharSequence s, int start, int before, int count)
			{
			}
			// ---------------------------------------------------------------------	
        });
        // -------------------------------------------------------------------------
		// -------------------------------------------------------------------------
		stonesField.addTextChangedListener (new TextWatcher()
		{ 
			@Override
			public void afterTextChanged (Editable theData)
			{
				// -----------------------------------------------------------------
				if (processTextChange)
				{
					validate (VALIDATE_STONES,theData);
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			@Override
			public void beforeTextChanged (CharSequence s, int start, int count,int after) 
			{
			}
			// ---------------------------------------------------------------------
			@Override
			public void onTextChanged (CharSequence s, int start, int before, int count)
			{	
			}
			// ---------------------------------------------------------------------	
        });
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void setHeight (int theCms)
	{
		// -------------------------------------------------------------------------
		// 11/09/2017 ECU convert from metric to imperial
		// -------------------------------------------------------------------------
		int localInches = (int) Math.round ((float) theCms / 2.54);
		// -------------------------------------------------------------------------
		// 11/09/2017 ECU now set the main variables accordingly
		// -------------------------------------------------------------------------
		heightCms		= theCms;
		heightFeet		= localInches / 12;
		heightInches	= localInches - (heightFeet * 12);
		// -------------------------------------------------------------------------
		// 11/09/2017 ECU set the values in the displayed fields
		// -------------------------------------------------------------------------
		updateField (feetField,StaticData.BLANK_STRING + heightFeet);
		updateField (inchesField,StaticData.BLANK_STRING + heightInches);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	void setHeight (int theFeet,int theInches)
	{
		// -------------------------------------------------------------------------
		// 11/09/2017 ECU convert from imperial to metric
		// -------------------------------------------------------------------------
		heightCms 		= (int) Math.round ((float)((theFeet * 12) + theInches) * 2.54);
		heightFeet		= theFeet;
		heightInches	= theInches;
		// -------------------------------------------------------------------------
		// 11/09/2017 ECU set the value in the displayed field
		// -------------------------------------------------------------------------
		updateField (cmsField,StaticData.BLANK_STRING + heightCms);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void updateField (EditText theField,String theValueString)
	{
		// -------------------------------------------------------------------------
		// 11/09/2017 ECU created to update the field after switching off the 
		//                listener. After updating the listener is switched back on
		// -------------------------------------------------------------------------
		processTextChange = false;
		theField.setText (theValueString);
		processTextChange = true;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	boolean validate (int theType,Editable theData)
	{
		// -------------------------------------------------------------------------
		// 27/08/2017 ECU created to validate user input
		// -------------------------------------------------------------------------
		int		inputInt	= 0;
		float	inputFloat	= 0f;
		// -------------------------------------------------------------------------
		// 27/08/2017 ECU most input data formats an integer
		// -------------------------------------------------------------------------
		try
		{
			inputInt = Integer.parseInt (theData.toString());
			// ---------------------------------------------------------------------
			// 27/08/2017 ECU just in case the kilograms were entered without a
			//                decimal point then it would be seen as an integer
			// ---------------------------------------------------------------------
			inputFloat = (float) inputInt;
			// ---------------------------------------------------------------------
		}
		catch (NumberFormatException theException)
		{
			try
			{
				inputFloat = Float.parseFloat(theData.toString());
			}
			catch (NumberFormatException theException2)
			{
				return false;
			}
		}
		// -------------------------------------------------------------------------
		switch (theType)
		{
			// ---------------------------------------------------------------------
			case VALIDATE_CMS:
				// -----------------------------------------------------------------
				// 11/09/2017 ECU the user has entered the height in cms
				// -----------------------------------------------------------------
				setHeight (inputInt);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case VALIDATE_DIASTOLIC:
				// -----------------------------------------------------------------
				if (theData.length() >= 2)
				{
					if (inputInt >= DIASTOLIC_LOW && inputInt <= DIASTOLIC_HIGH)
					{				
						diastolic = inputInt;
						return true;
					}
					else
					{
						if (theData.length () > 2)
							Utilities.popToastAndSpeak (getString (R.string.value_wrong), true);
					}
				}
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case VALIDATE_FEET:
				// -----------------------------------------------------------------
				// 11/09/2017 ECU accept any feet
				// -----------------------------------------------------------------
				heightFeet = inputInt;
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case VALIDATE_HEART_RATE:
				heartRate = inputInt;
				break;
			// ---------------------------------------------------------------------
			case VALIDATE_INCHES:
				if (inputInt >= 0 && inputInt <= 11)
				{
					// -------------------------------------------------------------
					setHeight (heightFeet,inputInt);
					// -------------------------------------------------------------
					return true;
				}
				else
				{
					Utilities.popToastAndSpeak (getString (R.string.value_wrong), true);
				}
				break;
			// ---------------------------------------------------------------------
			case VALIDATE_KILOGRAMS:
					if (theData.length() >= 2)
					{
						// ---------------------------------------------------------
						weight.setWeight (inputFloat);
						// ---------------------------------------------------------
						// 27/08/2017 ECU indicate that the user has entered the
						//                metric weight - save for future use
						// ---------------------------------------------------------
						PublicData.storedData.weight_metric = true;
						// ---------------------------------------------------------
						updateField (stonesField,StaticData.BLANK_STRING + weight.stones);
						updateField (poundsField,StaticData.BLANK_STRING + weight.pounds);
						// ---------------------------------------------------------
						// 11/09/2017 ECU display the BMI
						// ---------------------------------------------------------
						bmiDisplay ();
						// ---------------------------------------------------------
					}
				break;
			// ---------------------------------------------------------------------
			case VALIDATE_POUNDS:
				// -----------------------------------------------------------------
				// 27/08/2017 ECU clearly the pounds can only be 0 to 13
				// -----------------------------------------------------------------
				if (inputInt >= 0 && inputInt <= 13)
				{
					// -------------------------------------------------------------
					weight.setWeight (weight.stones,inputInt);
					// -------------------------------------------------------------
					// 30/09/2020 ECU redisplay the pounds field to remove any
					//                leading zeroes. Position the cursor at the
					//                end of the field
					// -------------------------------------------------------------
					updateField (poundsField,String.format ("%d",weight.pounds));
					poundsField.setSelection (poundsField.getText().length());
					// -------------------------------------------------------------
					updateField (stonesField,String.format ("%d",weight.stones));
					updateField (kilogramsField,String.format ("%3.1f",weight.kilograms));
					// -------------------------------------------------------------
					// 11/09/2017 ECU display the BMI
					// -------------------------------------------------------------
					bmiDisplay ();
					// -------------------------------------------------------------
					return true;
					// -------------------------------------------------------------
				}
				else
				{
					Utilities.popToastAndSpeak (getString (R.string.value_wrong), true);
				}
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case VALIDATE_STONES:
				// -----------------------------------------------------------------
				// 27/08/2017 ECU allow anything for stones
				// -----------------------------------------------------------------
				if (inputInt > 6)
				{
					// -------------------------------------------------------------
					// 27/08/2017 ECU indicate that the user has entered the
					//                imperial weight - save for future use
					// -------------------------------------------------------------
					PublicData.storedData.weight_metric = false;
					// -------------------------------------------------------------
					weight.stones = inputInt;
					// -------------------------------------------------------------
					weight.setWeight (inputInt,weight.pounds);
					// -------------------------------------------------------------
					updateField (poundsField,String.format ("%d",weight.pounds));
					updateField (kilogramsField,String.format ("%3.1f",weight.kilograms));
					// -------------------------------------------------------------
					// 11/09/2017 ECU display the BMI
					// -------------------------------------------------------------
					bmiDisplay ();
					// -------------------------------------------------------------
					return true;
				}
				break;
			// ---------------------------------------------------------------------
			case VALIDATE_SYSTOLIC:
				if (theData.length() >= 2)
				{
					if (inputInt >= SYSTOLIC_LOW && inputInt <= SYSTOLIC_HIGH)
					{				
						systolic = inputInt;
						return true;
					}
					else
					{
						if (theData.length () > 2)
							Utilities.popToastAndSpeak (getString (R.string.value_wrong), true);
					}
				}
				break;
			//----------------------------------------------------------------------		
		}
		// -------------------------------------------------------------------------
		// 27/08/2017 ECU default to carry on
		// -------------------------------------------------------------------------
		return false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

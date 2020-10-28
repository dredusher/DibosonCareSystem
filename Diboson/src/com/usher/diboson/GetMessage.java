package com.usher.diboson;

import java.lang.reflect.Method;
import java.util.List;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GetMessage extends DibosonActivity 
{
	/* ==================================================================== */
	// ====================================================================
	// 05/01/2014 ECU created
	// 22/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 07/10/2016 ECU modified to handle appointment notes as well as normal
	//                messages
	// 31/10/2016 ECU for notes make sure that the soft keyboard does not
	//                pop up immediately
	// --------------------------------------------------------------------
	// Testing
	// =======
	//=====================================================================
	/* ==================================================================== */
	String      	defaultMessage;				// 09/10/2016 ECU added
	boolean     	notesDisplay;				// 09/10/2016 ECU added
	Button			messageButton;
	int				parameter;					// 07/10/2016 ECU added
	Method			playButtonMethod;			// 23/10/2016 ECU added
	List<RecordedNote>	
					recordedNotesList;			// 26/10/2016 ECU changed from String
	Method			returnMethod;				// 07/10/2016 ECU added
	/* ============================================================================= */
	static	Context			context;					// 23/10/2016 ECU added
	static	TextView		messageView;				// 25/10/2016 ECU made static
	static	String			playButtonLegend;			// 23/10/2016 ECU added
	static	Button			playRecordedNotesButton;	// 23/10/2016 ECU added
	static  boolean			playState;					// 24/10/2016 ECU added
	static	RecordedNote	recordedNotes	= null;		// 23/10/2016 ECU added
														// 26/10/2016 ECU changed from String
														//            ECU changed to static
	static  RelativeLayout	seekBarLayout;				// 25/10/2016 ECU added
	// =============================================================================
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 24/10/2016 ECU set up some particular activity characteristics
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// ---------------------------------------------------------------------
			// 22/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 07/10/2016 ECU check if there is a default message to display
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
			// ---------------------------------------------------------------------
			// 07/10/2016 ECU default the parameter to 'no set'
			// 09/10/2016 ECU added setting of notes display and default message
			// 23/10/2016 ECU reset the path to any recorded notes
			//            ECU added the playButtonMethod
			// 24/10/2016 ECU added the recordState (true = record, false = stop)
			// ---------------------------------------------------------------------
			context				= this;
			defaultMessage		= null;
			notesDisplay		= false;
			parameter 			= StaticData.NO_RESULT;
			playButtonLegend 	= null;
			playButtonMethod	= null;
			playState			= true;
			recordedNotes		= null;
			returnMethod		= null;
		    // ---------------------------------------------------------------------
			if (extras != null)
			{
				// -----------------------------------------------------------------
				// 07/10/2016 ECU check if there is a default message to display
				// 09/10/206 ECU changed to use default message
				// -----------------------------------------------------------------
				defaultMessage = extras.getString (StaticData.PARAMETER_MESSAGE);
				// -----------------------------------------------------------------
				// 07/10/2016 ECU check if a parameter has been passed
				// -----------------------------------------------------------------
				parameter = extras.getInt (StaticData.PARAMETER_DATA,StaticData.NO_RESULT);
				// -----------------------------------------------------------------
				// 07/10/2016 ECU check if a return method has been specified
				// -----------------------------------------------------------------
				MethodDefinition<?> methodDefinition = (MethodDefinition<?>) extras.getSerializable (StaticData.PARAMETER_METHOD);
				if (methodDefinition != null)
				{
					returnMethod = methodDefinition.ReturnMethod (new Object [] {0,0});
				}
				// -----------------------------------------------------------------
				// 23/10/2016 ECU check if a return method has been specified
				// -----------------------------------------------------------------
				methodDefinition = (MethodDefinition<?>) extras.getSerializable (StaticData.PARAMETER_SPEAK_METHOD);
				if (methodDefinition != null)
				{
					playButtonMethod =  methodDefinition.ReturnMethod (0);
				}
				// -----------------------------------------------------------------
				// 09/10/2016 ECU check if the 'note display' is wanted
				// -----------------------------------------------------------------
				notesDisplay = extras.getBoolean (StaticData.PARAMETER_NOTES,false);
				// -----------------------------------------------------------------
				// 23/10/2016 ECU check if there are recorded notes
				// 26/10/2016 ECU changed to reflect use of RecordedNote
				// -----------------------------------------------------------------
				recordedNotesList = (List<RecordedNote>) getIntent().getSerializableExtra (StaticData.PARAMETER_RECORDED_NOTES);
				// -----------------------------------------------------------------
				// 23/10/2016 ECU work out the last entry
				// -----------------------------------------------------------------
				if ((recordedNotesList != null) && (recordedNotesList.size() > 0))
				{
					// -------------------------------------------------------------
					// 23/10/2016 ECU change the play legend to indicate how many
					//                recorded notes there current are
					// -------------------------------------------------------------
					playButtonLegend = String.format(getString (R.string.press_to_play_notes_format),
										((recordedNotesList.size() == 1) ? "1 entry" : (recordedNotesList.size() + " entries")));
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 09/10/2016 ECU decide which layout is to be displayed		
			// ---------------------------------------------------------------------
			setContentView (notesDisplay ? R.layout.get_notes : R.layout.activity_get_message);
		
			messageView = (TextView) findViewById (R.id.input_message_view);
			// ---------------------------------------------------------------------
			// 09/10/2016 ECU check if there is a default message to display
			// ---------------------------------------------------------------------
			if (defaultMessage != null && !defaultMessage.equals(StaticData.BLANK_STRING))
			{
				messageView.append (defaultMessage + StaticData.NEWLINE);
				// -----------------------------------------------------------------
				// 11/10/2016 ECU if notes display then append current date and time
				// -----------------------------------------------------------------
				if (notesDisplay)
				{
					messageView.append (Utilities.getAdjustedTime (PublicData.dateFormatterFull) + " ");
				}
				//------------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			((Button)findViewById(R.id.input_message_button)).setOnClickListener (acceptMessage);
			// ---------------------------------------------------------------------
			// 11/10/2016 ECU set up the button to speak the displayed message
			// 23/10/2016 ECU add additional buttons
			// ---------------------------------------------------------------------
			if (notesDisplay)
			{
				// -----------------------------------------------------------------
				// 25/10/2016 ECU set up the layout for future use
				// -----------------------------------------------------------------
				seekBarLayout = (RelativeLayout) findViewById(R.id.seekbar_layout);
				// ---------------------------------------------------------------------
				// 31/10/2016 ECU make sure that the soft keyboard does not pop up
				// ---------------------------------------------------------------------
				getWindow().setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				// -----------------------------------------------------------------
				((Button) findViewById(R.id.speak_message_button)).setOnClickListener (speakMessage);
				// -----------------------------------------------------------------
				// 23/10/2016 ECU add the email, record and play back buttons
				// -----------------------------------------------------------------
				((Button) findViewById (R.id.record_message_button)).setOnClickListener (notesButtonListener);
				// -----------------------------------------------------------------
				// 23/10/2016 ECU check if there are any recorded notes
				// -----------------------------------------------------------------
				playRecordedNotesButton = (Button) findViewById (R.id.play_message_button);
				playRecordedNotesButton.setOnClickListener (notesButtonListener);
				// -----------------------------------------------------------------
				playRecordedNotesButton.setOnLongClickListener (new View.OnLongClickListener() 
				{
					@Override
					public boolean onLongClick (View theView) 
					{
						// ---------------------------------------------------------
                		// 23/10/2016 ECU call the method if defined
						// 24/10/2016 ECU add playState check
                		// ---------------------------------------------------------
						if (playButtonMethod != null && playState)
						{
							// -----------------------------------------------------
							// 07/10/2016 ECU have a method associated with the acceptance
							// -----------------------------------------------------
							try 
							{
								// -------------------------------------------------
								playButtonMethod.invoke (null, new Object [] {parameter});
								// -------------------------------------------------
							}
							catch (Exception theException) 
							{	
							}
							// -----------------------------------------------------
						}
						return true;
					}
				});
				// ----------------------------------------------------------------
				// 26/10/2016 ECU put in the check on ....List
				// ----------------------------------------------------------------
				if (recordedNotes != null || (recordedNotesList != null) && (recordedNotesList.size() > 0))
				{
					playRecordedNotesButton.setVisibility (View.VISIBLE);
					// -------------------------------------------------------------
					// 23/10/2016 ECU change the legend on the 'play' button
					// -------------------------------------------------------------
					playRecordedNotesButton.setText (Utilities.twoLineButtonLegend (this,
							    getString (R.string.press_to_play_notes), playButtonLegend));
					// -------------------------------------------------------------
				}
				else
				{
					playRecordedNotesButton.setVisibility (View.INVISIBLE);
				}
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 22/10/2015 ECU the activity has been recreated after having been
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
		// Inflate the menu; this adds items to the action bar if it is present.
		
		return true;
	}
	/* ============================================================================= */
	private View.OnClickListener acceptMessage = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{
			// ---------------------------------------------------------------------
			// 04/01/2014 ECU set the values from the input data
			// ---------------------------------------------------------------------
			String	 message 	= messageView.getText ().toString();
			// ---------------------------------------------------------------------
			// 07/10/2016 ECU check if a return method has been specified
			// ---------------------------------------------------------------------
			if (returnMethod == null)
			{
				// -----------------------------------------------------------------
				// 05/01/2014 ECU store the entered message and return that all OK
				// -----------------------------------------------------------------
				Intent resultData = new Intent ();	
				resultData.putExtra (StaticData.PARAMETER_MESSAGE,message);
				// -----------------------------------------------------------------
				// 07/10/2016 ECU check if the parameter is to be returned
				// -----------------------------------------------------------------
				if (parameter != StaticData.NO_RESULT)
				{
					resultData.putExtra (StaticData.PARAMETER_DATA,parameter);
				}
				// -----------------------------------------------------------------
				setResult (RESULT_OK,resultData);
			}
			else
			{
				// -----------------------------------------------------------------
				// 07/10/2016 ECU have a method associated with the acceptance
				// -----------------------------------------------------------------
				try 
				{
					// -------------------------------------------------------------
					returnMethod.invoke (null, new Object [] {new Object [] {parameter,message,recordedNotes}});
					// -------------------------------------------------------------
					// 24/10/2016 ECU cancel any audio
					// -------------------------------------------------------------
					Utilities.stopAllAudio ();
					// -------------------------------------------------------------
				}
				catch (Exception theException) 
				{	
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			finish ();
		}
	};
	// =============================================================================

	// =============================================================================
	private View.OnClickListener notesButtonListener = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{	
			// ---------------------------------------------------------------------
			// 23/10/2016 ECU switch according to the button
			// ---------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.play_message_button:
					// -------------------------------------------------------------
					// 24/10/2016 ECU check the state of the button
					// -------------------------------------------------------------
					if (playState)
					{
						// ---------------------------------------------------------
						// 24/10/2016 ECU in play mode
						//            ECU changed to use play.... method
						// 25/10/2016 ECU check if a note exists
						//            ECU add in the project folder as recordedNotes
						//                is now relative to that
						// 26/10/2016 ECU added the .fileName
						//            ECU rearranged the logic
						// ---------------------------------------------------------
						if (recordedNotes != null)
						{
							// -----------------------------------------------------
							// 26/10/2016 ECU tell the user what is going on before
							//                playing the file
							// -----------------------------------------------------
							AppointmentDetails.playAFile (context.getString (R.string.notes_playing_record),
														  PublicData.projectFolder + recordedNotes.fileName);
							// -----------------------------------------------------
							// 26/10/2016 ECU set up the seekbar, etc..
							// -----------------------------------------------------
							AppointmentsActivity.setPlayButton ();
							// -----------------------------------------------------
						}
						else
						if (AppointmentsActivity.playLastNotes (parameter) == null)
						{
							
						}
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 24/10/2016 ECU this is the cancel option
			 			//            ECU stop anything that is playing or queueing to be played
						//            ECU add the flush to tts to stop any speech
						//			  ECU changed to use the new method
			 			// ---------------------------------------------------------
						Utilities.stopAllAudio ();
			 			// ---------------------------------------------------------
			 			// 24/10/2016 ECU set the legend to the correct state
			 			// ---------------------------------------------------------
			 			setPlayButtonLegend (true);
			 	 		// ---------------------------------------------------------	
			 		}
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.record_message_button:
					// -------------------------------------------------------------
					// 25/10/2016 ECU changed to use sub folder because want everything
					//                to be relative to the project folder
					// 26/10/2016 ECU changed to reflect the use of RecordedNote
					// 14/11/2016 ECU added 'false' to indicate not to append to an
					//                existing file
					// -------------------------------------------------------------
					recordedNotes = new RecordedNote (PublicData.appointmentsSubFolder + Utilities.getAUniqueFileName (StaticData.APPOINTMENT_HEADER));
					Utilities.recordAFile (getBaseContext (),
										   PublicData.projectFolder + recordedNotes.fileName,
										   new MethodDefinition<GetMessage> (GetMessage.class,"StopRecordingMethod"),
										   false);
					// -------------------------------------------------------------
					// 23/10/2016 ECU make the playback button visible
					// -------------------------------------------------------------
					playRecordedNotesButton.setVisibility (View.VISIBLE);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}
		}
	};
	// =============================================================================
	public static void setPlayButtonLegend (boolean theState)
	{
		// -------------------------------------------------------------------------
		// 24/10/2016 ECU created to set the legend on the 'play' button depending
		//                on 'theState'.
		//
		//					theState  =  true ..... set legend to reflect 'play'
		//                            =  false .... set legend to reflect 'cancel'
		// 09/01/2018 ECU put in the check for null on 'playRecordedNotesButton'
		// -------------------------------------------------------------------------
		if (playRecordedNotesButton != null)
		{
			if (theState)
			{ 
				// -----------------------------------------------------------------
				// 26/10/2016 ECU do the check on null
				// -----------------------------------------------------------------
				if (playButtonLegend == null)
					playRecordedNotesButton.setText (context.getString (R.string.press_to_play_notes));
				else	
					playRecordedNotesButton.setText (Utilities.twoLineButtonLegend (context,
														context.getString (R.string.press_to_play_notes),playButtonLegend));	
				// -----------------------------------------------------------------
			}
			else
			{
				playRecordedNotesButton.setText (context.getString (R.string.stop_playing_notes));
				// -----------------------------------------------------------------
				// 24/10/2016 ECU start up a timer to check when everything processed
				// -----------------------------------------------------------------
				PublicData.messageHandler.sendEmptyMessage (StaticData.MESSAGE_ANYTHING_PLAYING);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 24/10/2016 ECU remember the new state
			// ---------------------------------------------------------------------
			playState = theState;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	private View.OnClickListener speakMessage = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{	
			// ---------------------------------------------------------------------
			// 11/10/2016 ECU speak the text message - if not 'empty'
			// ---------------------------------------------------------------------
			String localString = messageView.getText().toString();
			// ---------------------------------------------------------------------
			if (Utilities.emptyString (localString))
			{
				Utilities.SpeakAPhrase (getBaseContext(),localString);
			}
			// ---------------------------------------------------------------------
		}
	};
	// =============================================================================
	public static void StopRecordingMethod ()
	{
		// -------------------------------------------------------------------------
		// 26/10/2016 ECU created to store the end time
		// -------------------------------------------------------------------------
		if (recordedNotes != null)
		{
			recordedNotes.setEndTime ();
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

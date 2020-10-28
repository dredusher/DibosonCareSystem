package com.usher.diboson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class VoiceRecognition extends DibosonActivity implements OnInitListener
{
	/* ============================================================================= */
	// 15/06/2013 ECU created
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 30/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 21/05/2016 ECU add the stored spoken phrases
	// 04/06/2016 ECU changes to enable the editing of existing spoken phrases
	// 09/06/2016 ECU added the 'initialMessage' to tell the user about the long
	//                clicking on a list item to define a spoken phrase. Made it static
	//                because don't want to have it coming up too often.
	//            ECU IMPORTANT - see note below at 10/06/2016
	// 10/06/2016 ECU moved the initial message display from onCreate to onClick
	//                because want to use view which was having difficulty with
	//            ECU pass through parameter to indicate that activity has been
	//                restarted by itself so no need to have initialMessage as static
	/* ============================================================================= */
	private final static String 	TAG = "VoiceRecognition";
	// -----------------------------------------------------------------------------
	private final static int		PREDICTION_LENGTH = 3;					// 04/06/2016 ECU added
	/* ============================================================================= */
						Activity   		activity;							// 21/11/2013 ECU added
						Context			context;							// 21/05/2016 ECU added
						int				incomingCommandNumber 	= StaticData.NO_RESULT;	
						 													// 03/09/2013 ECU added
						boolean    		initialMessage		  	= true;		// 09/06/2016 ECU added
						ArrayList<String> matches;
				static	boolean			matchPhrase;						// 04/06/2016 ECU added
				static	List<int []> 	phraseMatches;						// 04/06/2016 ECU added
				static	String []  		phraseMatchesList;					// 04/06/2016 ECU added
						String			phraseToSpeak 			= null;		// 12/09/2013 ECU added
						boolean			restarted				= false;	// 10/06/2016 ECU added
	private 			ListView 		resultList;
						Button 			speakButton;
				static	EditText		spokenPhraseActions;				// 21/05/2016 ECU added
				static	EditText		spokenPhraseTerms;					// 21/05/2016 ECU added
						TextToSpeech 	textToSpeech		  	= null;
				static	VoiceHandler	voiceHandler;
	/* ============================================================================= */
	@Override
	public void onCreate (Bundle savedInstanceState)
	{
		// -------------------------------------------------------------------------
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 30/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 21/11/2013 ECU store activity for later
			// ---------------------------------------------------------------------
			activity = (Activity) this;
			// ---------------------------------------------------------------------
			// 21/05/2016 ECU store the context for future use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 03/09/2013 ECU check if any arguments have been passed in the intent
			// ---------------------------------------------------------------------  
			Bundle extras = getIntent().getExtras();
				
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 03/09/2013 ECU check if there is an incoming command to process
				// -----------------------------------------------------------------
				// 10/06/2016 ECU check if this activity has been restarted by itself
				// -----------------------------------------------------------------
				// 10/06/2016 ECU if restarted by itself then do not need the initial
				//                message
				// ------------------------------------------------------------------
				restarted = extras.getBoolean (StaticData.PARAMETER_RESTART,false);
				if (restarted)
				{
					initialMessage = false;
				}
				else
				{
					// -------------------------------------------------------------
					// 06/03/2014 ECU changed to use PARAMETER.....
					// 25/03/2016 ECU set the default value - NO_RESULT
					// -------------------------------------------------------------
					incomingCommandNumber = extras.getInt (StaticData.PARAMETER_COMMAND_NUMBER,StaticData.NO_RESULT);
					// -------------------------------------------------------------
					// 12/09/2013 ECU check if there is a phrase to speak
					// 11/02/2014 ECU changed to use PARAMETER_PHRASE
					// -------------------------------------------------------------
					phraseToSpeak = extras.getString (StaticData.PARAMETER_PHRASE);
					// -------------------------------------------------------------
					// 11/04/2015 ECU if for some reason a null value is passed through
					//                then just change it to a nothing string of "" - don't
					//                do it through the default value because the parameter
					//                will have been passed check on 'incoming....'
					// 11/10/2015 ECU put in the
					// -------------------------------------------------------------
					if (phraseToSpeak == null && (incomingCommandNumber == StaticData.NO_RESULT))
					{
						// ---------------------------------------------------------
						// 11/04/2015 ECU as this is a problem that I'm trying to detect then
						//                put an entry into the project log
						// ---------------------------------------------------------
						Utilities.LogToProjectFile (TAG, getString (R.string.null_phrase));
						// ---------------------------------------------------------
						phraseToSpeak = "null phrase received";
						// ---------------------------------------------------------
					}
				}
				// ------------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			if (restarted || extras == null)
			{
				// -----------------------------------------------------------------
				// 28/11/2016 ECU call method to set the screen up
				// -----------------------------------------------------------------
				Utilities.SetUpActivity(this,true,true,false);
				// -----------------------------------------------------------------
				setContentView (R.layout.activity_voice_recognition);
				// -----------------------------------------------------------------
				resultList = (ListView) findViewById(R.id.speakList);
				speakButton = (Button) findViewById (R.id.speakButton);
				// -----------------------------------------------------------------
				// 16/06/2013 ECU disable the button if there is no recognition service present
				// -----------------------------------------------------------------
				PackageManager thePackageManager = getPackageManager();
				
				List<ResolveInfo> activities 
					= thePackageManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),0);
		
				if (activities.size() == 0)
				{	
					speakButton.setEnabled( false);
					// -------------------------------------------------------------
					// 08/11/2013 ECU use the custom toast
					// -------------------------------------------------------------
					Utilities.popToast ("Voice Recogniser Not Found");
				}
				// -----------------------------------------------------------------
				// 16/06/2013 ECU set up the listeners
				// -----------------------------------------------------------------
				speakButton.setOnClickListener (buttonClick); 
				resultList.setOnItemClickListener (listViewClick);
				// -----------------------------------------------------------------
				// 09/06/2016 ECU added the long click
				// -----------------------------------------------------------------
				resultList.setOnItemLongClickListener (listViewLongClick);
				// -----------------------------------------------------------------
				// 10/06/2016 ECU declare the local message handler
				// -----------------------------------------------------------------
				voiceHandler = new VoiceHandler ();
				// -----------------------------------------------------------------	
			}
			// --------------------------------------------------------------------- 
			// 15/06/2013 ECU create the text to speech object
			// ---------------------------------------------------------------------
			textToSpeech = new TextToSpeech (this,this);
			// ----------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 30/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	private View.OnClickListener buttonClick = new View.OnClickListener() 
	{
	   @Override
	   public void onClick (View theView) 
	   {
		// -----------------------------------------------------------------
			// 09/06/2016 ECU tell the user about phrase definition
			//            ECU only do once
			// -----------------------------------------------------------------
			if (initialMessage)
			{
				// -------------------------------------------------------------
				// 09/06/2016 ECU indicate that no need to action the message again
				// 10/06/2016 ECU moved here from the onCreate method
				// -------------------------------------------------------------
				initialMessage = false;
				// -------------------------------------------------------------
				// 09/06/2016 ECU now inform the user of the long click option
				// -------------------------------------------------------------
				Utilities.popToastAndSpeak ((Activity) context,
											theView,
											getString (R.string.spoken_phrase_click_define),
											Utilities.createAMethod (VoiceRecognition.class,"ButtonClickedMethod"));
				// -------------------------------------------------------------
			}
			else
			{
				Utilities.startVoiceRecognitionActivity (activity,textToSpeech);  
			}
	   }
	};
	// =============================================================================
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate (R.menu.spoken_phrase, menu);
		return true;
	}
	/* ============================================================================= */
	public void onDestroy() 
	{
		// -------------------------------------------------------------------------
        // 15/06/2013 ECU close down the text to speech object if created
		// -------------------------------------------------------------------------
        if (textToSpeech != null) 
        {
            textToSpeech.stop ();
            textToSpeech.shutdown ();
            // ---------------------------------------------------------------------
        }
        super.onDestroy();
    }
	// =============================================================================
	public boolean onOptionsItemSelected (MenuItem item)
	{
		// -------------------------------------------------------------------------
		// 21/05/2016 ECU created to handle menu items
		// -------------------------------------------------------------------------
		switch (item.getItemId())
		{
			// ---------------------------------------------------------------------
			case R.id.spoken_phrase_define:
				// -----------------------------------------------------------------
				// 21/05/2016 ECU added
				// -----------------------------------------------------------------
				spokenPhraseDefinition (this);
				// -----------------------------------------------------------------
				return true;
			// ---------------------------------------------------------------------
			case R.id.spoken_phrase_display:
				// -----------------------------------------------------------------
				// 31/05/2016 ECU added to display the stored values
				// 06/06/2016 ECU check if there are any stored phrases
				// -----------------------------------------------------------------
				if (PublicData.voiceCommandPhrases.size() == 0)
					Utilities.popToast (getString (R.string.spoken_phrases_none));
				else
					Utilities.popToast (findViewById(android.R.id.content),VoiceCommandPhrases.PrintAll(),true);
				// -----------------------------------------------------------------
				return true;
			// ---------------------------------------------------------------------			
		}
		
		return true;
	}
	/* ============================================================================= */
	private OnItemClickListener listViewClick = new OnItemClickListener() 
	{
        public void onItemClick (AdapterView<?> parent, View view,
                					int position, long id) 
        {
        	// ---------------------------------------------------------------------
            // 15/06/2013 ECU speak the string back
        	// ---------------------------------------------------------------------
            textToSpeech.speak (matches.get(position), TextToSpeech.QUEUE_FLUSH, null);            
        }
    };
    // =============================================================================
    private OnItemLongClickListener listViewLongClick = new OnItemLongClickListener() 
	{
    	// -------------------------------------------------------------------------
    	// 09/06/2016 ECU created to handle the long click on a listed item which
    	//                will be used to define a spoken phrase from the specified
    	//                item
    	// -------------------------------------------------------------------------
		@Override
		public boolean onItemLongClick (AdapterView<?> parent, 
										View view,
										int position, 
										long id) 
		{
			// ---------------------------------------------------------------------
			// 09/06/2016 ECU the result that has been 'long clicked' is at
			//                matches.get(position)
			// ---------------------------------------------------------------------
			spokenPhraseDefinition (context,matches.get(position));
			// ---------------------------------------------------------------------
			return false;
		}  
    };
	/* ============================================================================= */
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data)
	{
		// -------------------------------------------------------------------------
		// 21/11/2013 ECU changed to use request code in MainActivity rather than local
		// 01/09/2015 ECU changed to use StaticData
		// -------------------------------------------------------------------------
		if (requestCode == StaticData.VOICE_REQUEST_CODE && resultCode == RESULT_OK)
		{
			matches = data.getStringArrayListExtra (RecognizerIntent.EXTRA_RESULTS);
			// ---------------------------------------------------------------------
			// 21/11/2013 ECU display the phrases heard
			// ---------------------------------------------------------------------
			resultList.setAdapter (new ArrayAdapter<String> (this,android.R.layout.simple_list_item_1,
									matches));
			// ---------------------------------------------------------------------
			// 16/06/2013 ECU check if command found in the string
			// ---------------------------------------------------------------------
		    int result =  Utilities.parseTheString (context,matches,PublicData.voiceCommands);
		    // ---------------------------------------------------------------------
		    // 21/05/2016 ECU if the method returns USER_DEFIN..... then a user
		    //                defined spoken phrase has been spoken
		    // ---------------------------------------------------------------------
		    if (result != StaticData.USER_DEFINED_SPOKEN_PHRASE)
		    {	
		    	if (result != StaticData.NO_RESULT)
		    	{
		    		Utilities.processVoiceCommand (this,PublicData.voiceCommands[result],textToSpeech);  
		    		// -------------------------------------------------------------
		    		// 17/06/2013 ECU remember the last command processed
		    		// -------------------------------------------------------------
		    		PublicData.lastVoiceCommand = result;
		    		// -------------------------------------------------------------
		    	}
		    	else
		    	{
		    		// -------------------------------------------------------------
		    		// 21/02/2015 ECU check if any WeMo devices to be handled
		    		// -------------------------------------------------------------
		    		result = WeMoActivity.voiceCommands (matches);
		    		// -------------------------------------------------------------
		    		// 18/06/2013 ECU tell the user that no match found	
		    		// -------------------------------------------------------------
		    		if (result == StaticData.NO_RESULT)
		    			speakAndWait (getString(R.string.no_match_found),10);   				
		    	}
		    }
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	/* ============================================================================= */
	public void onInit (int status) 
	{
		// -------------------------------------------------------------------------
        //15/06/2013 ECU check status of Text to Speech engine
		//03/09/2013 ECU take into account the incoming command
		// -------------------------------------------------------------------------
        if (status == TextToSpeech.SUCCESS) 
        {
        	// ---------------------------------------------------------------------
        	//            ECU Setting speech language
        	// 10/11/2014 ECU changed to use Locale.getDefault instead of Locale.UK
        	// ---------------------------------------------------------------------
        	int result = textToSpeech.setLanguage (Locale.getDefault());
        	// ---------------------------------------------------------------------
        	//If your device doesn't support language you set above
        	// ---------------------------------------------------------------------
        	if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) 
        	{
        		// -----------------------------------------------------------------
        		// 08/11/2013 ECU use the custom toast
        		// -----------------------------------------------------------------
        		Utilities.popToast ("Language '" + Locale.getDefault() + "' is not supported");
        	} 
        	else
        	{
        	   if (phraseToSpeak == null)
        	   {
        		   // --------------------------------------------------------------
        		   // 03/09/2013 ECU process a command if supplied in extras
        		   // --------------------------------------------------------------
        		   if (incomingCommandNumber != StaticData.NO_RESULT)
        		   {
        			   Utilities.processVoiceCommand (this, PublicData.voiceCommands [incomingCommandNumber],textToSpeech);
            	   
        			   waitForSpeechToEnd (200);
        		   }
        		   else
        		   {
        			   // ----------------------------------------------------------
        			   // 03/09/2013 ECU now request speech input
        			   // ----------------------------------------------------------
        			   textToSpeech.speak (getString (R.string.clickButtonToSpeak), TextToSpeech.QUEUE_FLUSH, null);
        		   }
        	   }
        	   else
        	   {
        		   // --------------------------------------------------------------
        		   // 12/09/2013 ECU just speak the phrase that has been supplied
        		   // --------------------------------------------------------------
        		   speakAndWait (phraseToSpeak,200);
        	   }
           }   
        } 
        else 
        {
        	// ---------------------------------------------------------------------
        	// 08/11/2013 ECU use the custom toast
        	// 09/06/2016 ECU use the resource
        	// ---------------------------------------------------------------------
			Utilities.popToast (getString (R.string.tts_initialisation_failed));
			// ---------------------------------------------------------------------
        }
    }
	// =============================================================================
	public static void ButtonClickedMethod ()
	{
		// -------------------------------------------------------------------------
		// 10/06/2016 ECU created as the method to be called when the user clicks
		//                the button to start the voice recognition system
		//            ECU start up the voice recognition system
		// ------------------------------------------------------------------------
		voiceHandler.sendEmptyMessage (StaticData.MESSAGE_START);
		// -------------------------------------------------------------------------
	}		
	// =============================================================================
	public static void wPhrase (int thePhraseIndex)
	{
		// -------------------------------------------------------------------------
		// 05/06/2016 ECU created to create a new phrase
		// -------------------------------------------------------------------------
		matchPhrase = false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void selectAPhrase (int thePhraseIndex)
	{
		// -------------------------------------------------------------------------
		// 04/06/2016 ECU created to handle the selection of a phrase
		// -------------------------------------------------------------------------
		int matchedPhrase = phraseMatches.get (thePhraseIndex) [0];
		// -------------------------------------------------------------------------
		// 04/06/2016 ECU switch off the matching and fill the fields
		// -------------------------------------------------------------------------
		matchPhrase = false;
		spokenPhraseTerms.setText (PublicData.voiceCommandPhrases.get(matchedPhrase).phrasesAsString());
		spokenPhraseActions.setText (PublicData.voiceCommandPhrases.get(matchedPhrase).actions);
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
	private void speakAndWait (String theString,final int theWaitTime)
	{	
		// -------------------------------------------------------------------------
		// 18/06/2013 ECU speak out the input string
		// -------------------------------------------------------------------------
		textToSpeech.speak(theString,TextToSpeech.QUEUE_FLUSH,null); 
		// -------------------------------------------------------------------------
		// 18/06/2013 ECU now wait until finish talking
		// -------------------------------------------------------------------------
		Thread thread = new Thread()
		 {
		        @Override
		        public void run()
		        {
		            try 
		            {
		                synchronized(this)
		                {
		                	// -----------------------------------------------------
		                	// 07/02/2014 ECU wait until the speech starts (with break out)
		                	// -----------------------------------------------------
		                	int breakoutCounter = 0;
		                	
		                	while (!textToSpeech.isSpeaking())
		                	{
		                		wait (theWaitTime); 
		                	
		                		if (++breakoutCounter > 10)
		                			break;
		                	}
		                	// -----------------------------------------------------
		                	// 18/06/2013 ECU wait until the current speech has 
		                	//                finished
		                	// -----------------------------------------------------
		                	while (textToSpeech.isSpeaking())
		                	{
		                         wait(theWaitTime);
		                	}
		                	// -----------------------------------------------------
		                	// 12/09/2013 ECU if not speaking a phrase then reactivate
		                	//                the voice recognition
		                	// -----------------------------------------------------
		                	if (phraseToSpeak == null)
		                	{
		                		// -------------------------------------------------
		                		// 18/06/2013 ECU reactivate the voice recognition
		                		// -------------------------------------------------
		                		Utilities.startVoiceRecognitionActivity (activity,textToSpeech);
		                	}
		                	else
		                	{
		                		// -------------------------------------------------
		                		// 12/09/2013 ECU phrase has been spoken so just exit
		                		// -------------------------------------------------
		                		finish ();
		                	}
		                }
		            }
		            catch(InterruptedException theException)
		            {  
		            	// ---------------------------------------------------------
		            	// 03/12/2013 ECU optionally display a debug message
		            	// ---------------------------------------------------------
		            	Utilities.debugMessage(TAG,"Exception:" + theException);
		            }       
		        }
		    };

		    thread.start();        
	}
	// =============================================================================
    public static void SpokenPhraseActions (String theCommands)
    {
    	// -------------------------------------------------------------------------
    	// 21/05/2016 ECU created to store commands that are required for the spoken
    	//                phrase
    	// -------------------------------------------------------------------------
    	spokenPhraseActions.setText (theCommands);
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
	public void spokenPhraseDefinition (Context theContext,String theInputPhrase)
	{
		// -------------------------------------------------------------------------
		// 21/05/2016 ECU called to try and define spoken phrase and actions
		// 09/06/2016 ECU added 'theInputPhrase' to preset the spoken phrase
		// -------------------------------------------------------------------------
		setContentView (R.layout.define_spoken_phrase);
		// -------------------------------------------------------------------------
		// 21/05/2016 ECU specify the listener for the button
		// -------------------------------------------------------------------------
		((Button) findViewById (R.id.spoken_phrase_actions_button)).setOnClickListener (spokenPhraseListener);
		((Button) findViewById (R.id.spoken_phrase_define_button)).setOnClickListener (spokenPhraseListener);
		// -------------------------------------------------------------------------
		spokenPhraseActions = (EditText) findViewById (R.id.spoken_phrase_actions);
		spokenPhraseTerms	= (EditText) findViewById (R.id.spoken_phrase_enter);
		// -------------------------------------------------------------------------
		// 09/06/2016 ECU check whether need to preset the phrase field
		//			  ECU make sure no matching takes place
		// -------------------------------------------------------------------------
		if (theInputPhrase != null)
		{
			matchPhrase = false;
			spokenPhraseTerms.setText (theInputPhrase);
		}
		// -------------------------------------------------------------------------
		// 04/06/2016 ECU indicate that matching is required
		// -------------------------------------------------------------------------
		matchPhrase = true;
		// -------------------------------------------------------------------------
		// 04/06/2016 ECU indicate to the user when prediction will occur
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak (String.format (getString (R.string.prediction_message),PREDICTION_LENGTH),true);
		// -------------------------------------------------------------------------
		spokenPhraseTerms.addTextChangedListener (new TextWatcher() 
		{
			// ---------------------------------------------------------------------
			// 04/06/2016 ECU IMPORTANT if 'textNoSuggestions' is not included in
			//                ========= the textInput definition for the field then
			//                          the listener will be triggered more than once
			//                          as the text is automatically changed
			// ---------------------------------------------------------------------
			@Override
			public void onTextChanged (CharSequence theInputCharacters, int theStart, int theBefore,int theCount) 
			{
				// -----------------------------------------------------------------
				// 04/06/2016 ECU check if looking for a match
				// -----------------------------------------------------------------
				if (matchPhrase && (theInputCharacters.length() >= PREDICTION_LENGTH))
				{
					// -------------------------------------------------------------
					// 04/06/2016 ECU check if what has been typed matches something
					//                that has been stored
					// -------------------------------------------------------------
					phraseMatches = VoiceCommandPhrases.checkAllForAMatch(theInputCharacters.toString());
					// -------------------------------------------------------------
					// 04/06/2016 ECU check for a unique match
					// -------------------------------------------------------------
					if (phraseMatches.size () == 1)
					{
						// ---------------------------------------------------------
						// 04/06/2016 ECU a match found so update the fields - but
						//                only once - that's why setting false needs
						//                to be done first so that setText doesn't
						//                cause infinite loop
						// ---------------------------------------------------------
						int matchedPhrase = phraseMatches.get(0)[0];
						matchPhrase = false;
						spokenPhraseTerms.setText (PublicData.voiceCommandPhrases.get(matchedPhrase).phrasesAsString());
						spokenPhraseActions.setText (PublicData.voiceCommandPhrases.get(matchedPhrase).actions);
						// ---------------------------------------------------------
					}
					else
					// -------------------------------------------------------------
					// 04/06/2016 ECU check if more than one match
					// -------------------------------------------------------------
					if (phraseMatches.size () > 1)
					{
						// ---------------------------------------------------------
						// 06/06/2016 ECU tell the user what is happening
						// ---------------------------------------------------------
						Utilities.popToastAndSpeak (String.format (getString (R.string.spoken_phrase_matches),phraseMatches.size ()),true);
						// ---------------------------------------------------------
						// 04/06/2016 ECU build up a list of phrases
						// ---------------------------------------------------------
						phraseMatchesList = new String [phraseMatches.size()];
						for (int localMatch = 0; localMatch < phraseMatches.size(); localMatch++)
						{
							phraseMatchesList [localMatch] = PublicData.voiceCommandPhrases.get(phraseMatches.get(localMatch)[0]).Print (phraseMatches.get(localMatch)[1]);
						}
						// ---------------------------------------------------------
						// 04/06/2016 ECU start the dialogue so that user can select
						//                the required phrase, if desired
						// ---------------------------------------------------------
						DialogueUtilities.listChoice (context,
								  					  getString (R.string.select_phrase),
								  					  phraseMatchesList,
								  					  Utilities.createAMethod (VoiceRecognition.class,"selectAPhrase",0),
								  					  getString (R.string.create_new_spoken_phrase),
								  					  Utilities.createAMethod (VoiceRecognition.class,"wPhrase",0));
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
            }
			// ---------------------------------------------------------------------
			@Override
			public void beforeTextChanged (CharSequence cs, int arg1, int arg2,int arg3) 
			{
			}
			// ---------------------------------------------------------------------
			@Override
			public void afterTextChanged (Editable arg0) 
			{
			}
			// ---------------------------------------------------------------------
		});
	}
	// =============================================================================
	public void spokenPhraseDefinition (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 09/06/2016 ECU created to call the main method when no default phrase
		//                is specified
		// -------------------------------------------------------------------------
		spokenPhraseDefinition (theContext,null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	private View.OnClickListener spokenPhraseListener = new View.OnClickListener() 
	{
	   @Override
	   public void onClick (View view) 
	   {
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.spoken_phrase_actions_button:
					DialogueUtilities.multilineTextInput (context,
							  context.getString (R.string.spoken_phrase_actions_title),
							  context.getString (R.string.action_command_summary),
							  3,
							  StaticData.BLANK_STRING,
							  Utilities.createAMethod (VoiceRecognition.class,"SpokenPhraseActions",StaticData.BLANK_STRING),
							  null,
							  StaticData.NO_RESULT,
							  context.getString (R.string.press_to_define_command));
					break;
				// -----------------------------------------------------------------
				case R.id.spoken_phrase_define_button:
					// -------------------------------------------------------------
					// 21/05/2016 ECU get the information for the screen
					// -------------------------------------------------------------
					String localSpokenActions = spokenPhraseActions.getText().toString();
					String localSpokenPhrases = spokenPhraseTerms.getText().toString();
					// -------------------------------------------------------------
					// 21/05/2016 ECU maske sure that both fields have been completed
					// -------------------------------------------------------------
					if (localSpokenActions.equalsIgnoreCase(StaticData.BLANK_STRING) || localSpokenPhrases.equalsIgnoreCase(StaticData.BLANK_STRING))
					{
						Utilities.popToastAndSpeak (getString (R.string.phrase_actions_needed),true);
					}
					else
					{
						// ---------------------------------------------------------
						// 04/06/2016 ECU want to check if need to replace an existing
						//                entry
						// ---------------------------------------------------------
						boolean phraseFound = false;
						if (PublicData.voiceCommandPhrases.size() > 0)
						{
							// -----------------------------------------------------
							// 04/06/2016 ECU there are phrases to check
							// -----------------------------------------------------
							for (int phraseIndex = 0; phraseIndex < PublicData.voiceCommandPhrases.size(); phraseIndex++)
							{
								if (PublicData.voiceCommandPhrases.get(phraseIndex).checkIfAllExists (localSpokenPhrases))
								{
									// ---------------------------------------------
									// 04/06/2016 ECU match found so flag the fact 
									//                and tell the user
									// ---------------------------------------------
									phraseFound = true;
									// ---------------------------------------------
									// 04/06/2016 ECU replace the existing entry
									// ---------------------------------------------
									PublicData.voiceCommandPhrases.set (phraseIndex,new VoiceCommandPhrases (localSpokenPhrases,localSpokenActions));
									// ---------------------------------------------
									Utilities.popToastAndSpeak (getString (R.string.spoken_phrase_replaced),true);
									// ---------------------------------------------
									break;
								}
							}
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
						// 21/05/2016 ECU add the new command and update the disk copy
						// 04/06/2016 ECU only add if an exiting entry has not been
						//                replaced
						// ---------------------------------------------------------
						if (!phraseFound)
						{
							PublicData.voiceCommandPhrases.add (new VoiceCommandPhrases (localSpokenPhrases,localSpokenActions));
							Utilities.popToastAndSpeak (getString (R.string.spoken_phrase_added),true);
						}
						// ---------------------------------------------------------
						VoiceCommandPhrases.WriteToDisk (context);
						// ---------------------------------------------------------
						// 21/05/2016 ECU restart this activity
						// ---------------------------------------------------------
						Intent localIntent = getIntent ();
						// ---------------------------------------------------------
						// 10/06/2016 ECU indicate that the activity is being restarted
						// ---------------------------------------------------------
						localIntent.putExtra (StaticData.PARAMETER_RESTART,true);
						// ---------------------------------------------------------
					    finish ();
					    startActivity (localIntent);
						// ---------------------------------------------------------
					}
					break;
				// -----------------------------------------------------------------
			}
		  
	   }
	};
	// =============================================================================
	// 10/06/2016 ECU IMPORTANT - handler should be static to avoid memory leaks but
	//                =========   at this stage do NOT want textToSpeech to be static
	// -----------------------------------------------------------------------------
	@SuppressLint("HandlerLeak") 
	class VoiceHandler extends Handler
    {
		// -------------------------------------------------------------------------
        @Override
        public void handleMessage(Message theMessage) 
        {  
        	switch (theMessage.what)
        	{
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_START:
        			// -------------------------------------------------------------
        			// 10/06/2016 ECU start the recognition system
        			// -------------------------------------------------------------
        			Utilities.startVoiceRecognitionActivity (activity,textToSpeech); 
        			// -------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        	}
        }
        // -------------------------------------------------------------------------
    }
	// =============================================================================
	private void waitForSpeechToEnd (final int theWaitTime)
	{	
		// -------------------------------------------------------------------------
		// 18/06/2013 ECU now wait until finish talking
		// -------------------------------------------------------------------------
		Thread thread = new Thread()
		 {
		        @Override
		        public void run()
		        {
		            try 
		            {
		                synchronized(this)
		                {
		                	// -----------------------------------------------------
		                	// 18/06/2013 ECU wait until the current speech has 
		                	//                finished
		                	// -----------------------------------------------------
		                	while (textToSpeech.isSpeaking())
		                         wait(theWaitTime);
		                	
		                	finish ();
		                }
		            }
		            catch(InterruptedException theException)
		            {   
		            	// ---------------------------------------------------------
		            	// 03/12/2013 ECU optionally display a debug message
		            	// ---------------------------------------------------------
		            	Utilities.debugMessage(TAG,"Exception:" +theException);
		            }       
		        }
		    };
		    // ---------------------------------------------------------------------
		    thread.start();        
	}
	/* ============================================================================= */
}

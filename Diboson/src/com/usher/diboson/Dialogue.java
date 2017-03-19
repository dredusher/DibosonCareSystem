package com.usher.diboson;

import java.util.ArrayList;
import java.util.Locale;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.TextView;

public class Dialogue extends DibosonActivity implements OnInitListener
{
	/* ============================================================ */
	// 20/11/2013 ECU created to try and establish a dialogue with
	//                the user
	// 20/12/2013 ECU put in some code to handle television commands
	//                - only for temporary use
	// 01/03/2014 ECU rearranged so that television commands do not
	//                cause the Television activity to be invoked.
	//                Instead calls are made to methods which have
	//                now been made public
	// 12/05/2015 ECU changed the way a string of digits are sent to
	//                a remote controller so that there is a delay 
	//                between each digit
	// 09/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 14/10/2015 ECU changed to use BlueToothServiceUtilities
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 01/06/2016 ECU check whether any of the stored spoken phrases
	//                are being used
	/* ============================================================ */
	//private final static String 	TAG = "Dialogue";
	/* ============================================================ */
	// 24/11/2013 ECU added - actions that could be taken
	/* ------------------------------------------------------------ */
	public static final int	ACTION_NONE			= StaticData.NO_RESULT;
	final static int		ACTION_PHONE		= 0;
	/* ============================================================ */
	final static String		DEFAULT_NOUN		= "ed";
	final static String		DEFAULT_VERB		= "phone";
	/* ============================================================ */
	public static final String FINISH			= "finish";
	/* ============================================================================= */
	// 25/11/2013 ECU put the phrases here for the time being -
	//                later they will be put into files
	//            ECU started to put the phrases into text files
	//                in the dialogue directory and accessed using
	//                the local method ReadAFile
	// -----------------------------------------------------------
	final static String		PHRASE_DO_YOU_WANT 
								= "Do you want me to ";
	final static String		PHRASE_FEEL_GOOD 
								= "It's really good that you feel ";
	final static String		PHRASE_FEEL_UNWELL 
								= "I'm so sorry that you do not feel ";
	final static String		PHRASE_GLAD 
								= "I'm glad that ";
//	final static String		PHRASE_GOODBYE 
//								= "It seems that you don't want to talk to me anymore ,,,,,, enjoy the rest of your day";
//	final static String		PHRASE_GREETING 
//								= "Sorry but you've asked me a question on something I know nothing about";
//	final static String		PHRASE_NICE_DAY 
//								= "It seems to be a really nice day to day ,,,,,,,  sunny but cold";
	final static String     PHRASE_NO_QUESTION 
								= "Sorry I didn't understand why you said that,,,, what do you want";
	final static String     PHRASE_NO_REMOTE 
								= "unfortunately there is no attached remote controller";
	final static String     PHRASE_RESPONSE_NO 
								= "OK but if you need me to do something later then just tell me";
//	final static String		PHRASE_SORRY 
//								= "Sorry but you've asked me a question on something I know nothing about";
	final static String	    PHRASE_TELEVISION_OFF
								= "You have left the television mode ,,,,,, just say television again to re enter";
	final static String	    PHRASE_TELEVISION_ON
								= "You have entered the television mode ,,,,,, just say television to exit it";
	final static String		PHRASE_WHAT_DO_YOU_WANT 
								= " ,,,,,,what do you want me to do";
	/* ============================================================ */
	// 23/11/2013 ECU added - the idea behind themes is to limit
	//                the extent to which certain words relate, e.g
	//                words like 'sick', 'ill' would normally be associated 
	//                with health
	//
	//                by default words are relevant in all themes 
	//                indicated by THEME_ALL
	/* ----------------------------------------------------------- */
	public static final int THEME_ALL			= StaticData.NO_RESULT;
	final static int		THEME_PERSONAL		= 0;
	final static int		THEME_HEALTH		= 1;
	final static int		THEME_CARE			= 2;
	final static int		THEME_WEATHER		= 3;
	final static int		THEME_ENTERTAINMENT	= 4;
	final static int		THEME_CHITCHAT		= 5;
	/* ============================================================ */
	final static int		TYPE_NOUN			= 0;
	final static int		TYPE_VERB			= 1;
	final static int		TYPE_PRONOUN		= 2;
	final static int    	TYPE_PREPOSITION	= 3;
	final static int        TYPE_STRESS			= 4;	// 22/11/2013 ECU extra
	final static int		TYPE_SUBJECT		= 5;	// 22/11/2013 ECU extra
	final static int        TYPE_QUESTION		= 6;	// 22/11/2013 ECU extra
	final static int        TYPE_TERMINATE      = 7;    // 22/11/2013 ECU extra
	final static int		TYPE_NEGATIVE		= 8;	// 23/11/2013 ECU extra
	final static int        TYPE_ADJECTIVE      = 9;    // 23/11/2013 ECU added
	final static int        TYPE_ADJECTIVE_NEG	= 10;	// 23/11/2013 ECU added - this
														//    is a negative adjective - like ill,sick
	final static int        TYPE_YES			= 11;	// 24/11/2013 ECU added
	final static int        TYPE_NO             = 12;   // 24/11/2013 ECU added
	/* ============================================================ */
	final static String		PARAM_INTRODUCER = "%%";
	/* ============================================================ */
	static Activity		activity;						// 21/11/2013 ECU added
	String				changedPhrase;
	DialogueKeyWords [] dialogueKeyWords    = {
												new DialogueKeyWords (new String [] {"ed","eddie","8"},"ed",1,TYPE_NOUN),
												new DialogueKeyWords (new String [] {"phyl","phyllis","phil"},"phyllis",1,TYPE_NOUN),
												new DialogueKeyWords (new String [] {"call","get","phone","telephone"},"phone",2,TYPE_VERB,THEME_ALL,ACTION_PHONE),
												new DialogueKeyWords (new String [] {"alarm","emergency","help"},"emergency",90,TYPE_STRESS),
												new DialogueKeyWords (new String [] {"gas","electricity","sewer","toilet","water"},null,3,TYPE_SUBJECT),
												new DialogueKeyWords (new String [] {"time","date"},"time",3,TYPE_SUBJECT),
												new DialogueKeyWords (new String [] {"age","old","birthday"},"age",3,TYPE_SUBJECT),
												new DialogueKeyWords (new String [] {"what","when","why","where","how","whats","what's"},null,10,TYPE_QUESTION),
												new DialogueKeyWords (new String [] {"finish","halt","end","terminate"},"finish",20,TYPE_TERMINATE),
												new DialogueKeyWords (new String [] {"not","don't","can't","cannot"},"not",20,TYPE_NEGATIVE),
												/* ============================================================ */
												// 24/11/2013 ECU answers to questions
												// ------------------------------------------------------------
												new DialogueKeyWords (new String [] {"no","nah"},"no",20,TYPE_NO),
												new DialogueKeyWords (new String [] {"yes","ok","yeh"},"yes",20,TYPE_YES),
												/* ============================================================ */
												// 23/11/2013 ECU declare any theme based words
												// ------------------------------------------------------------
												// THEME_ENTERTAINMENT
												// ===================
												new DialogueKeyWords (new String [] {"begin",
																					"play",
																					"start"},
																		"start",20,TYPE_VERB,THEME_ENTERTAINMENT),
												new DialogueKeyWords (new String [] {"pause","paul's",
																					"resume"},
																		"pauseresume",20,TYPE_VERB,THEME_ENTERTAINMENT),
												new DialogueKeyWords (new String [] {"end",
																					"stop"},
																		"stop",20,TYPE_VERB,THEME_ENTERTAINMENT),
												new DialogueKeyWords (new String [] {"music",
																					"radio",
																					"television"},
																		null,20,TYPE_SUBJECT,THEME_ENTERTAINMENT),
												new DialogueKeyWords (new String [] {"photos",
																					"photograph"},
																		"photos",20,TYPE_SUBJECT,THEME_ENTERTAINMENT),
												// ------------------------------------------------------------
												// THEME_HEALTH
												// ============
												new DialogueKeyWords (new String [] {"fine",
																					"healthy",
																					"well"},
																null,60,TYPE_ADJECTIVE,THEME_HEALTH),
												new DialogueKeyWords (new String [] {"ill",
																					"sick",
																					"unwell"},
																	"well",60,TYPE_ADJECTIVE_NEG,THEME_HEALTH),					
												// ------------------------------------------------------------
												// THEME_WEATHER
												// =============
												new DialogueKeyWords (new String [] {"weather",
																					"rain",
																					"raining",
																					"sun",
																					"sunny"},
																		"weather",3,TYPE_SUBJECT,THEME_WEATHER),
												// ------------------------------------------------------------
											};
	/* ============================================================================= */
	DialogueParameter [] dialogueParameters = {
												new DialogueParameter ("name","Ed"),
												new DialogueParameter ("address","57, kingsdown road, leytonstone"),
												new DialogueParameter ("date",PublicData.dateTimeString)
											};
	/* ============================================================================= */
	int					action = ACTION_NONE;	// 24/11/2013 ECU added 
	String  			adjective = "";
	BlueToothServiceUtilities	
						blueToothServiceUtilities;
												// 14/10/2015 ECU added
	String				currentPhrase = "";		// 23/11/2013 ECU added
	TextView			dialogueEnteredView;	// 21/12/2013 ECU added
	TextView			dialogueMatchedView;	// 21/12/2013 ECU added
	TextView			dialogueStatusView;		// 21/12/2013 ECU added
	String				introducer = "";
	boolean				negate = false;			// 23/11/2013 ECU added
	String 				noun = DEFAULT_NOUN;
	ArrayList<String>	phrases;
	String 				preposition = "";
	String 				pronoun = "";
	boolean				question = false;
	DialogueQuestion	questionData = null;	// 24/11/2013 ECU added
	int					remoteDevicex;			// 01/03/2014 ECU added - id of remote device
	String 				stress = "";
	String 				subject = "";
	boolean				televisionCommand = false;
												// 20/12/2013 ECU added
	String 				terminate = "";
	boolean				terminating = false;
	static TextToSpeech	textToSpeech;
	int					theme = THEME_ALL;
	String 				verb = DEFAULT_VERB;
	/* ============================================================================= */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{	
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 28/11/2016 ECU call method to set the screen up
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true,true,false);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_dialogue);
			// ---------------------------------------------------------------------
			// 21/11/2013 ECU set up the activity
			// ---------------------------------------------------------------------
			activity = (Activity) this;
			// ---------------------------------------------------------------------
			// 21/12/2013 ECU set up various text views
			// ---------------------------------------------------------------------
			dialogueEnteredView 	= (TextView) findViewById (R.id.dialogueTextViewEntered);
			dialogueMatchedView  	= (TextView) findViewById (R.id.dialogueTextViewMatched);		
			dialogueStatusView 		= (TextView) findViewById (R.id.dialogueTextView);
			// ---------------------------------------------------------------------
			// 15/06/2013 ECU create the text to speech object
			// ---------------------------------------------------------------------
			textToSpeech = new TextToSpeech (this, this);
		}
		else
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been recreated after having been
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
		// 03/06/2016 ECU just finish this activity
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
		// 03/06/2016 ECU now call the super for this method
		// -------------------------------------------------------------------------
		super.onBackPressed();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public void onDestroy() 
	{
		// -------------------------------------------------------------------------
        // 15/06/2013 ECU close down the text to speech object if created
		// -------------------------------------------------------------------------
        if (textToSpeech != null) 
        {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
	/* ============================================================================= */
	public void onInit (int status) 
	{
		// -------------------------------------------------------------------------
        // 15/06/2013 ECU check status of Text to Speech engine
		// 03/09/2013 ECU take into account the incoming command
		// -------------------------------------------------------------------------
        if (status == TextToSpeech.SUCCESS) 
        {
        	// ---------------------------------------------------------------------
        	//            ECU Setting speech language
        	// 10/11/2014 ECU changed to use Locale.getDefault instead of Locale.UK
        	// ---------------------------------------------------------------------
        	int result = textToSpeech.setLanguage (Locale.getDefault());
            // ---------------------------------------------------------------------
        	//            ECU If your device doesn't support language you set above
        	// ---------------------------------------------------------------------
        	if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) 
        	{
        		// -----------------------------------------------------------------
        		// 08/11/2013 ECU use the custom toast
				// -----------------------------------------------------------------
        		Utilities.popToast (getString(R.string.locale_not_supported));
        		finish ();
        		// -----------------------------------------------------------------
        	}  
        	else
        	{
        		StartTheDialogue (getBaseContext(),textToSpeech);
        	}         
        } 
        else 
        {
        	// ---------------------------------------------------------------------
        	// 08/11/2013 ECU use the custom toast
			// ---------------------------------------------------------------------
			Utilities.popToast (getString(R.string.tts_initialisation_failed));
			// ---------------------------------------------------------------------
			finish ();
        }
    }
	/* ============================================================================= */
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data)
	{
		// -------------------------------------------------------------------------
		// 21/11/2013 ECU changed to use request code in MainActivity rather than local
		// -------------------------------------------------------------------------
		if (requestCode == StaticData.VOICE_REQUEST_CODE)
		{
			if (resultCode == RESULT_OK)
			{
				// -----------------------------------------------------------------
				// 21/11/2013 ECU get the results from the voice recognition software
				// -----------------------------------------------------------------
				phrases = data.getStringArrayListExtra (RecognizerIntent.EXTRA_RESULTS);
				// -----------------------------------------------------------------
				// 01/06/2016 ECU before parsing the data check if there are any matches
				//                with user-defined spoken phrases
				// -----------------------------------------------------------------
				if (!VoiceCommandPhrases.SearchAllPhrasesForMatch (getBaseContext(),phrases))
				{
					// -------------------------------------------------------------
					// 01/06/2016 ECU there was no match with the 'user-defined'
					//                phrases so do normal parsing
					// -------------------------------------------------------------
					ParseThePhrases (phrases);
					// ------------------------------------------------------------		
					// 22/11/2013 ECU if not in the terminating phase then get the next phrase
					// ------------------------------------------------------------
					if (!terminating)
						Utilities.GetAPhrase (activity,textToSpeech);
				}
			}
			
		}
		// -------------------------------------------------------------------------
		super.onActivityResult (requestCode, resultCode, data);
	}
	/* ============================================================================= */
	String AnswerAQuestion (boolean theQuestion,String theSubject)
	{
		String theAnswer = ReadAFile ("PHRASE_SORRY");
		
		if (theSubject.equalsIgnoreCase("weather"))
		{
			theAnswer = ReadAFile ("PHRASE_NICE_DAY");
		}
		else
		if (theSubject.equalsIgnoreCase("time"))
		{
			theAnswer = PublicData.dateTimeString;
		}
		else
		if (theSubject.equalsIgnoreCase("age"))
		{
			// ---------------------------------------------------------------------
			// 22/04/2015 ECU changed to use the patient's date of birth
			// ---------------------------------------------------------------------
			theAnswer = Utilities.workOutAge (getBaseContext(),
											  PublicData.patientDetails.dateOfBirth,
											  getBaseContext().getString(R.string.age_message));
			// ---------------------------------------------------------------------
		}
		
		return theAnswer;
	}
	// =============================================================================
	String ParseTheFile (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 14/02/2014 ECU changed to use the local ReadAFile
		// -------------------------------------------------------------------------
		String theParsedString = "";
		String theMessage = new String (ReadAFile(theFileName));
		
		String [] theWords = theMessage.split("[ ]");
		
		for (int index=0; index < theWords.length; index++)
		{
			if (theWords[index].startsWith (PARAM_INTRODUCER))
				theWords[index] = ParseTheParameter (theWords[index].replaceFirst("[%%][%%]", ""));
			
			theParsedString += theWords[index] + " ";
		}

		return theParsedString;
	}
	/* ============================================================================= */
	String ParseTheParameter (String theParameter)
	{
		// -------------------------------------------------------------------------
		// 26/11/2013 ECU changed so that the dialogue parameters can be passed
		//                as a parameter
		// -------------------------------------------------------------------------
		return ParseTheParameter (theParameter,dialogueParameters);
	}
	/* ----------------------------------------------------------------------------- */
	String ParseTheParameter (String theParameter, DialogueParameter [] theDialogueParameters)
	{
		// -------------------------------------------------------------------------
		// 26/11/2013 ECU created to allow the passing of dynamic dialogue parameters
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < theDialogueParameters.length; theIndex++)
		{
			if (theParameter.equals(theDialogueParameters[theIndex].input))
				return theDialogueParameters[theIndex].output;
		}
		
		return "";
	}
	/* ============================================================================= */
	int ParseThePhrase (String thePhrase)
	{
		int theImportance = 0;
		
		if (thePhrase != null)
		{
			// ---------------------------------------------------------------------
			// 21/11/2013 ECU split the phrase into words
			// ---------------------------------------------------------------------
			String [] theWords = thePhrase.split("[ ]");
					
			for (int theWord = 0; theWord < theWords.length; theWord++)
			{				
				for (int theKey = 0; theKey < dialogueKeyWords.length; theKey++)
				{
					for (int theOption = 0; theOption < dialogueKeyWords[theKey].word.length; theOption++)
					{
						if (theWords[theWord].equalsIgnoreCase(dialogueKeyWords[theKey].word[theOption]))
						{
							theImportance += dialogueKeyWords[theKey].importance;
							
							break;
						}
					}
				}
			}
		}
		
		return theImportance;
	}
	/* ============================================================================= */
	void ParseThePhrases (ArrayList<String> theList)
	{
		// -------------------------------------------------------------------------
		// 21/11/2013 ECU theList consists of an array of strings provided by the
		//                voice recognition software. Normally to scan it you would
		//                do :-
		//
		//		for (int index=0; index < theList.size(); index++)
		//		{		
		//			Utilities.popToast(theList.get(index));
		//		}
		//  
		// 				  for the moment I will assume that the first entry is the
		//                best one to use
		// -------------------------------------------------------------------------
		int theImportance = -1;
		int theImportanceMax = -1;
		int theSavedIndex = 0;
		
		for (int index=0; index < theList.size(); index++)
		{		
			if ((theImportance = ParseThePhrase (theList.get(index))) > theImportanceMax)
			{
				theImportanceMax = theImportance;
				theSavedIndex = index;
			}
		}
		// -------------------------------------------------------------------------
		// 21/12/2013 ECU display the phrase that was input
		// -------------------------------------------------------------------------
		dialogueEnteredView.setText ("Phrase Entered : " + theList.get(theSavedIndex));
		// -------------------------------------------------------------------------
		// 21/12/2013 ECU initially clear the matching field
		// -------------------------------------------------------------------------
		dialogueMatchedView.setText ("");
		// -------------------------------------------------------------------------
		// 20/12/2013 ECU check if a television command received
		// -------------------------------------------------------------------------
		if (!televisionCommand)
			RespondToThePhrase (theList.get(theSavedIndex));
		else
		{
			// ---------------------------------------------------------------------
			// 20/12/2013 ECU process a television command
			// ---------------------------------------------------------------------
			ProcessTelevisionCommand (theList.get(theSavedIndex));
		}
	}
	 /* ============================================================================ */	
	void ProcessTelevisionCommand (String thePhrase)
	{
		// -------------------------------------------------------------------------
		// 20/12/2013 ECU this method will try and process television commands
		// -------------------------------------------------------------------------
		
		if (!thePhrase.startsWith("television"))
		{
			// ---------------------------------------------------------------------
			// 14/10/2015 ECU if a match was found then display some useful information
			//                otherwise check for the other options
			// ---------------------------------------------------------------------
			if (blueToothServiceUtilities.processPhrase(thePhrase))
			{
				dialogueMatchedView.setText ("Television Command : " + thePhrase);
			}
			else
			{
				if (thePhrase.equalsIgnoreCase ("hitachi"))
				{
					// -------------------------------------------------------------
					// 14/10/2015 ECU changed to use ....Utilities
					// -------------------------------------------------------------
					blueToothServiceUtilities.setRemoteDevice (Television.REMOTE_HITACHI_TV);
					dialogueMatchedView.setText ("Changed device for a Hitachi television");
				}
				else
				if (thePhrase.equalsIgnoreCase("samsung"))
				{
					// -------------------------------------------------------------
					// 14/10/2015 ECU changed to use ....Utilities
					// -------------------------------------------------------------
					blueToothServiceUtilities.setRemoteDevice (Television.REMOTE_SAMSUNG_TV);
					dialogueMatchedView.setText ("Changed device for a Samsung television");
				}
				else
				if (TelevisionChannel (thePhrase))
				{
					// -------------------------------------------------------------
					// 01/03/2014 ECU have found a television channel and the 
					//				  necessary processing is within the method so 
					//                no need to do anything here
					// -------------------------------------------------------------
				}
				else
				{
					dialogueMatchedView.setText ("Television Command : No match for '" + thePhrase + "'");
				}
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 21/12/2013 ECU speak out a message and update the status text field
			// ---------------------------------------------------------------------
			Utilities.SpeakAPhrase (getBaseContext(),PHRASE_TELEVISION_OFF,textToSpeech);
			dialogueStatusView.setText ("Television Mode Off");
			// ---------------------------------------------------------------------
			// 20/12/2013 ECU indicate out of text mode
			// ---------------------------------------------------------------------
			televisionCommand = false;
			// ---------------------------------------------------------------------
			// 01/03/2014 ECU remember to 'unbind' from the bluetooth connection
			// 14/10/2015 ECU changed to use .......Utilities
			// ---------------------------------------------------------------------
			blueToothServiceUtilities.unBind();
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	String ReadAFile (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 25/11/2013 ECU reads the relevant file in the dialogue 
		//                folder and returns it as a string
		// 14/02/2014 ECU read into a local string so that some checking can
		//                take place as to whether the file exists
		// -------------------------------------------------------------------------
		byte [] stringRead = Utilities.readAFile (PublicData.dialogueFolder + theFileName + ".txt");
		
		if (stringRead == null)
		{
			// ---------------------------------------------------------------------
			// 14/02/2014 ECU indicate that the file is missing
			// ---------------------------------------------------------------------
			return "File " + theFileName + " does not exist";
		}
		
		return new String (stringRead);
	}
	/* ============================================================================= */
	String ReadAFile (String theFileName, DialogueParameter [] theDialogueParameters)
	{
		// -------------------------------------------------------------------------
		//  26/11/2013 ECU created to extend the method to allow the replacement of
		//                 embedded parameters
		// -------------------------------------------------------------------------
		String theParsedString = "";
		
		String [] theWords = ReadAFile (theFileName).split("[ ]");
		
		for (int index=0; index < theWords.length; index++)
		{
				
			if (theWords[index].startsWith(PARAM_INTRODUCER))
				theWords[index] = ParseTheParameter (theWords[index].replaceFirst("[%%][%%]", ""),theDialogueParameters);
			
			theParsedString += theWords[index] + " ";
		}

		return theParsedString;
	}
	/* ============================================================================= */
	void RespondToQuestion (DialogueQuestion theQuestion,int theResponse)
	{
		// -------------------------------------------------------------------------
		// 24/11/2013 ECU check if there is an outstanding question to answer
		// -------------------------------------------------------------------------
		if (theQuestion == null)
		{
			// ---------------------------------------------------------------------
			// 24/11/2013 ECU there is no question to answer
			// ---------------------------------------------------------------------
			Utilities.SpeakAPhrase (getBaseContext(),PHRASE_NO_QUESTION,textToSpeech);
		}
		else
		{
			switch (theResponse)
			{
				case TYPE_YES:
					// -------------------------------------------------------------
					// 24/11/2013 ECU there is a question to be responded to
					// -------------------------------------------------------------
					switch (questionData.action)
					{
						case ACTION_PHONE:
							Utilities.SpeakAPhrase (getBaseContext(),"OK I will phone " + questionData.noun + " about the " + questionData.subject,textToSpeech); 
							
							Utilities.makePhoneCall(getBaseContext(), (questionData.noun.startsWith("phyllis") ? getString(R.string.phone_number_phyllis) : getString(R.string.phone_number_ed)));
							break;
						default:
							break;
					}
					
					break;
					
				case TYPE_NO:
						Utilities.SpeakAPhrase (getBaseContext(),PHRASE_RESPONSE_NO,textToSpeech);
					break;
			}
		}
		// -------------------------------------------------------------------------
		// 24/11/2013 ECU reset the question
		// -------------------------------------------------------------------------
		questionData = null;
	}
	/* ============================================================================= */
	void RespondToThePhrase (String thePhrase)
	{
		// -------------------------------------------------------------------------
		// 23/11/2013 ECU remember the phrase being analysed
		// -------------------------------------------------------------------------
		currentPhrase = thePhrase;
		
		if (thePhrase != null)
		{
			action		= ACTION_NONE;		// 24/11/2013 ECU added
			adjective 	= "";
			introducer 	= "";
			negate 		= false;			// 23/11/2013 ECU added
			noun 		= DEFAULT_NOUN;
			preposition = "";
			pronoun 	= "";
			question 	= false;
			stress 		= "";
			subject 	= "";
			terminate 	= "";
			theme 		= THEME_ALL;
			verb 		= DEFAULT_VERB;
			// ---------------------------------------------------------------------
			// 21/11/2013 ECU split the phrase into words
			// ---------------------------------------------------------------------
			String [] theWords = thePhrase.split("[ ]");
			
			String word = "";
		
			for (int theWord = 0; theWord < theWords.length; theWord++)
			{
				word = theWords [theWord];
				
				for (int theKey = 0; theKey < dialogueKeyWords.length; theKey++)
				{
					for (int theOption = 0; theOption < dialogueKeyWords[theKey].word.length; theOption++)
					{
						if (theWords[theWord].equalsIgnoreCase(dialogueKeyWords[theKey].word[theOption]))
						{
							
							word = (dialogueKeyWords[theKey].preferred == null) ? theWords[theWord] : dialogueKeyWords[theKey].preferred;
							// -----------------------------------------------------
							// 23/11/2013 ECU change the theme if necessary - but 
							//                only once at the moment
							// -----------------------------------------------------
							if (theme == THEME_ALL)
							{
								theme = dialogueKeyWords[theKey].theme;
							}
							// -----------------------------------------------------
							// 24/11/2013 ECU check if action is to be remembered
							// -----------------------------------------------------
							if (action == ACTION_NONE)
							{
								action = dialogueKeyWords[theKey].action;
							}
							
							switch (dialogueKeyWords[theKey].type)
							{
								case TYPE_ADJECTIVE:
									// ---------------------------------------------
									// 23/11/2013 ECU added
									// ---------------------------------------------
									adjective = word;
									break;
								case TYPE_ADJECTIVE_NEG:
									// ---------------------------------------------
									// 23/11/2013 ECU this is an adjective which 
									//                has a negative connotation - like
									//                ill, sick, ...
									// ---------------------------------------------
									adjective = word;
									negate = true;
									break;
								case TYPE_NEGATIVE:
									// ---------------------------------------------
									// 23/11/2013 ECU added
									// ---------------------------------------------
									negate = true;
									break;
								case TYPE_NO:
									RespondToQuestion (questionData,TYPE_NO);
									return;
								case TYPE_NOUN:
									noun = (noun.equalsIgnoreCase(DEFAULT_NOUN) ? word : noun + " and " + word);
									break;
								case TYPE_VERB:
									verb = (verb.equalsIgnoreCase(DEFAULT_VERB) ? word : verb + " and " + word);
									break;
								case TYPE_PRONOUN:
									pronoun = (pronoun.equalsIgnoreCase("") ? word : pronoun + " and " + word);
									break;
								case TYPE_PREPOSITION:
									preposition = (preposition.equalsIgnoreCase("") ? word : preposition + " and " + word);
									break;
								case TYPE_QUESTION:
									question = true;
								case TYPE_STRESS:
									stress = (stress.equalsIgnoreCase("") ? word : stress + " and " + word);
									break;
								case TYPE_SUBJECT:
									subject = (subject.equalsIgnoreCase("") ? word : subject + " and " + word);
									break;
								case TYPE_TERMINATE:
									terminate = word;
									break;
								case TYPE_YES:
									RespondToQuestion (questionData,TYPE_YES);
									return;
							}
							
							break;
						}
					}
				}
			}
			// ---------------------------------------------------------------------
			// 22/11/2013 ECU check if a question has been asked
			// ---------------------------------------------------------------------
			if (!terminate.equals (""))
			{
				// -----------------------------------------------------------------
				// 22/11/2013 ECU indicate that the activity is closing down
				// -----------------------------------------------------------------
				terminating = true;
				
				Utilities.SpeakAPhrase (getBaseContext(),
						ReadAFile("PHRASE_GOODBYE",new DialogueParameter [] {
								new DialogueParameter ("name1","ed usher. "),
								new DialogueParameter ("name2","phyllis o'donnell. "),
								new DialogueParameter ("address","57, kingsdown road, leytonstone, london e11 3lw"),
								new DialogueParameter ("phone","0208 555 5256"),	
																			}),
						textToSpeech);
				waitForSpeechToEnd (500);
			}
			else
			{
				if (!question)
				{
					// -------------------------------------------------------------
					// 23/11/2013 ECU check the context of the phrase
					// -------------------------------------------------------------
					switch (theme)
					{
						case THEME_ENTERTAINMENT:
							
							changedPhrase = ThemeEntertainment ();
							break;
							
						case THEME_HEALTH:
							
							changedPhrase = ThemeHealth ();
							break;
							
						case THEME_WEATHER:
							
							changedPhrase = ThemeWeather ();
							break;
						
						default:
							
							changedPhrase = PHRASE_DO_YOU_WANT + verb + " " + noun + " about ";  

							if (!stress.equalsIgnoreCase(""))
							{
								changedPhrase += "an " + stress + " ";
								introducer = "with";
							}
								
							if (!subject.equalsIgnoreCase(""))
							{
								changedPhrase += introducer + " the " +  subject;
							}	
							// -----------------------------------------------------
							// 24/11/2013 ECU create a new question
							// -----------------------------------------------------
							if (action != ACTION_NONE)
								questionData = new DialogueQuestion (action,noun,subject);
							
							break;
					}	
				}
				else
				{
					changedPhrase = AnswerAQuestion (question,subject);
				}
				// -----------------------------------------------------------------
				// 24/11/2013 ECU put in the check for null
				// -----------------------------------------------------------------
				if (changedPhrase != null)
					Utilities.SpeakAPhrase (getBaseContext(),changedPhrase,textToSpeech);
			}
		}
	}
	/* ============================================================================= */
	void StartTheDialogue (Context theContext,TextToSpeech theTextToSpeech)
	{
		// -------------------------------------------------------------------------
		// 21/11/2013 ECU speak a welcoming message
		// 14/02/2014 ECU changed to use local ReadAFile
		// -------------------------------------------------------------------------
    	Utilities.SpeakAPhrase (theContext,ParseTheFile ("start"),theTextToSpeech); 
    	// -------------------------------------------------------------------------
    	// 21/11/2013 ECU start the voice recognition
    	// -------------------------------------------------------------------------
    	Utilities.GetAPhrase (activity,theTextToSpeech);
	}
	/* ============================================================================= */
	boolean TelevisionChannel (String theChannelName)
	{
		// -------------------------------------------------------------------------
		// 01/03/2014 ECU checks whether the spoken phrase corresponds to a 
		//                television channel name
		// 14/10/2015 ECU changed to use ....Utilities
		// -------------------------------------------------------------------------
		if (blueToothServiceUtilities.processChannelName(theChannelName))
		{
			// ---------------------------------------------------------------------
			dialogueMatchedView.setText ("Found Channel " + theChannelName);
			// ---------------------------------------------------------------------
			return true;
		}
		// -------------------------------------------------------------------------
		// 01/03/2014 ECU no matches found so indicate that fact
		// -------------------------------------------------------------------------
		return false;
	}
	/* ============================================================================= */
	String ThemeEntertainment ()
	{		
		String theResponse = "";
		
		if (subject.startsWith("music"))
		{
			if (verb.equalsIgnoreCase("start"))
			{
				Intent myIntent = new Intent (getBaseContext(),MusicPlayer.class);
				myIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); 
				myIntent.putExtra(FINISH,FINISH);
				getBaseContext().startActivity (myIntent);
			}
			else
			if (verb.equalsIgnoreCase("stop"))
			{
				if (PublicData.mediaPlayer != null && PublicData.mediaPlayer.isPlaying())
				{
					PublicData.mediaPlayer.stop();
					PublicData.musicPlayerData.tracksPlaying = false;
					PublicData.stopmpPlayer = true;
				}	
			}
			else
			if (verb.equalsIgnoreCase("pauseresume"))
			{
				if (PublicData.mediaPlayer != null)
				{
					if (!PublicData.mediaPlayerPaused)
					{
						PublicData.mediaPlayer.pause();
						PublicData.mediaPlayerPaused = true;
					}
					else
					{
						PublicData.mediaPlayer.start();
						PublicData.mediaPlayerPaused = false;
					}
				}
			}
		}
		else
		if (subject.startsWith("photos"))
		{
			Intent myIntent = new Intent (getBaseContext(),SlideShowActivity.class);
			myIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); 
			getBaseContext().startActivity (myIntent);
		}
		else
		if (subject.startsWith ("television"))
		{
			// ---------------------------------------------------------------------
			// 01/03/2014 ECU used to be a check on whether MainActivity.currentTelevision
			//                but now that methods in Television are called directly rather
			//                than by invoking the activity this is not necessary
			//            ECU added MainActivity.deviceCode as an argument
			// 02/03/2014 ECU added the check on whether the underlying bluetooth
			//                service is working correctly
			// ---------------------------------------------------------------------
			
			if (PublicData.blueToothService)
			{
				televisionCommand = true;
				// -----------------------------------------------------------------
				// 21/12/2013 ECU speak out a message and update the status field
				// -----------------------------------------------------------------				
				dialogueStatusView.setText ("Television Mode On");
				// -----------------------------------------------------------------
				// 01/03/2014 ECU initialise link to the bluetooth service
				// 14/10/2015 ECU changed to use ....Utilities
				// -----------------------------------------------------------------
				blueToothServiceUtilities = new BlueToothServiceUtilities (getBaseContext());
				// -----------------------------------------------------------------		
				Utilities.SpeakAPhrase (getBaseContext(),PHRASE_TELEVISION_ON,textToSpeech);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 02/03/2014 ECU cannot control external devices because there is
				//                no service to handle commands
				// -----------------------------------------------------------------
				
				Utilities.SpeakAPhrase (getBaseContext(),PHRASE_NO_REMOTE,textToSpeech);
			}
		}
		
		return theResponse;
	}
	/* ============================================================================= */
	String ThemeHealth ()
	{
		String theResponse = "";
		
		if (negate)
		{
			theResponse = PHRASE_FEEL_UNWELL + adjective + PHRASE_WHAT_DO_YOU_WANT;
		}
		else
		{
			theResponse = PHRASE_FEEL_GOOD + adjective;
		}
		
		return theResponse;
	}
	/* ============================================================================= */
	String ThemeWeather ()
	{
		String theResponse = "";
		
		if (question)
		{
			// ---------------------------------------------------------------------
			// 23/11/2013 ECU seems to be a question about the weather
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 23/11/2013 ECU seems to be a statement about the weather
			// ---------------------------------------------------------------------
			theResponse = PHRASE_GLAD + currentPhrase;
		}
		
		return theResponse;
	}
	/* ============================================================================= */
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
		                	// 18/06/2013 ECU wait until the current speech has finished
		                	// -----------------------------------------------------
		                	while (textToSpeech.isSpeaking())
		                         wait(theWaitTime);
		                	
		                	finish ();
		                }
		            }
		            catch(InterruptedException ex){                    
		            }       
		        }
		    };

		    thread.start();        
	}
	/* ============================================================================= */
}

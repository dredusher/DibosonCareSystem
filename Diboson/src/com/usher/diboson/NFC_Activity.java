package com.usher.diboson;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class NFC_Activity extends DibosonActivity 
{
	/* ============================================================================= */
	// 01/09/2013 ECU created
	//            ECU TERMINOLOGY
	//                ===========
	//				  NFC  = near field communication
	//				  NDEF = NFC Data Exchange Format
	// 18/03/2015 ECU changed to use the MainActivity.ACTION_DESTINATION_.. variables
	//                rather than having variables in this activity
	// 03/09/2015 ECU changed to use StaticData
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 11/06/2016 ECU major changes to enable the manual writing of NFC tags
	// 12/06/2016 ECU continue tidying up and use resources instead of literal strings
	// 15/06/2016 ECU add the remote processor button
	/* ============================================================================= */
	//final static String TAG = "NFC_Activity";
	// =============================================================================
	// 22/02/2015 ECU added the following constants
	// -----------------------------------------------------------------------------
	final static byte [] 	LANGUAGE_CODE	= {'e','n'};
	/* ============================================================================= */
			NfcAdapter		adapter;					// 12/06/2016 ECU added
			Button 			commandActionsButton;		// 11/06/2016 ECU added
	static	TextView 		commandDataTextView;		// 12/06/2016 ECU added
			TextView 		commandDataTitleTextView;	// 12/06/2016 ECU added
			Button 			commandNumberButton;
	static  Button 			commandRemoteButton;		// 15/06/2016 ECU added
			Spinner			commandSpinner;
	static	String			commandString;				// 22/02/2015 ECU added
			TextView 		commandTextView;
	static  Context			context;					// 11/06/2016 ECU added
			Tag 			detectedTag;
	static 	String []		devices			= null;		// 15/06/2016 ECU created
			TextView 		nfcView;
			PendingIntent	pendingIntent;				// 12/06/2016 ECU added
	static	boolean			startedManually	= false;	// 12/06/2016 ECU added
			boolean			waitingToWrite;				// 12/06/2016 ECU added
	private boolean 		writeProtect = false;  		// 02/09/2013 ECU set to true if you want
														//                the tag set to read only after
														//                writing data to it
			IntentFilter	writeTagFilters[];			// 12/06/2016 ECU added
	// =============================================================================
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		// 09/10/2015 ECU check whether the activity has been activated from new or
		//                just recreated after being destroyed by Android
		// 11/06/2016 ECU put in check on whether the user interface is running
		// 12/06/2016 ECU added started manually
		// -------------------------------------------------------------------------
		if ((savedInstanceState == null) && 
			(PublicData.userInterfaceRunning == true) && 
			!startedManually)
		{
			// ---------------------------------------------------------------------
			// 14/11/2016 ECU added to set up the acticity and full screen
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// ---------------------------------------------------------------------
			// 12/06/2016 ECU check if any parameters passed through
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 12/06/2016 ECU check if called from GridActivity
				// -----------------------------------------------------------------
				startedManually = extras.getBoolean (StaticData.PARAMETER_RESTART, false);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_nfc);
			// ---------------------------------------------------------------------
			// 11/06/2016 ECU save the context for later use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 02/09/2013 ECU set up various components of the user interface
			// ---------------------------------------------------------------------
			commandNumberButton = (Button) findViewById(R.id.command_button);
			commandNumberButton.setOnClickListener (buttonOnClickListener);	
			// ---------------------------------------------------------------------
			// 11/06/2016 ECU handle the button for defining actions
			// ----------------------------------------------------------------------
			commandActionsButton = (Button) findViewById(R.id.command_actions_button);
			commandActionsButton.setOnClickListener (buttonOnClickListener);
			// ---------------------------------------------------------------------
			// 15/06/2016 ECU handle the setting/resetting of a remote processor
			// ----------------------------------------------------------------------
			commandRemoteButton = (Button) findViewById(R.id.command_remote_button);
			commandRemoteButton.setOnClickListener (buttonOnClickListener);
			// ---------------------------------------------------------------------
			// 15/06/2016 ECU adjust the text to show current setting
			//            ECU display the current status
			// ---------------------------------------------------------------------
			updateRemoteButtonLegend (this);
			// ---------------------------------------------------------------------
			// 11/06/2016 ECU Note - set up input/output fields
			// ---------------------------------------------------------------------
			commandTextView = (TextView) findViewById (R.id.command_view);
			commandSpinner 	= (Spinner) findViewById (R.id.command_spinner);
			// ---------------------------------------------------------------------
			// 12/06/2016 ECU set the fields associated with data
			// ---------------------------------------------------------------------
			commandDataTextView 	 = (TextView) findViewById (R.id.command_write_data);
			commandDataTitleTextView = (TextView) findViewById (R.id.command_write_title);
			// ---------------------------------------------------------------------
			// 03/09/2013 ECU populate the spinner with the available commands
			// ---------------------------------------------------------------------
			List<String> list = new ArrayList<String>();
			// ---------------------------------------------------------------------
			// 11/06/2016 ECU put in checks on length opf the list
			// ---------------------------------------------------------------------
			if (PublicData.voiceCommands != null && PublicData.voiceCommands.length > 0)
			{
				for (int theIndex = 0; theIndex < PublicData.voiceCommands.length; theIndex++)
				{
					list.add (theIndex + " " + PublicData.voiceCommands[theIndex].Print ());
				}
			}
			// ---------------------------------------------------------------------
			// 22/02/2015 ECU get the WeMo commands
			// ---------------------------------------------------------------------
			String [] wemoCommands = WeMoActivity.getWeMoCommands();
			// ---------------------------------------------------------------------
			// 22/02/2015 ECU if there are commands then add these to the list
			// ---------------------------------------------------------------------
			if (wemoCommands != null)
			{
				for (int theIndex = 0; theIndex < wemoCommands.length; theIndex++)
					list.add (StaticData.ACTION_DESTINATION_WEMO + " " + wemoCommands [theIndex]);
			}
			// ---------------------------------------------------------------------
			// 15/06/2016 ECU changed to use local simple_... instead of
			//					android.R.layout.simple_spinner_item
			// 				  and remove
			//					dataAdapter.setDropDownViewResource (android.R.layout.simple_spinner_dropdown_item);
			//                this was to get a bigger font size for the list
			// ---------------------------------------------------------------------
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
													R.layout.simple_list_entry,list); 
	
			commandSpinner.setAdapter (dataAdapter);
			// ---------------------------------------------------------------------
			// 11/06/2016 ECU initialise the command string
			// 12/06/2016 ECU initialise the 'waiting..' flag
			// ---------------------------------------------------------------------
			commandString 	= null; 
			waitingToWrite 	= false;
			// ---------------------------------------------------------------------
			// 12/06/2016 ECU check how started
			// ---------------------------------------------------------------------
			if (startedManually)
			{
				adapter = NfcAdapter.getDefaultAdapter(this);
				// -----------------------------------------------------------------
				// 12/06/2016 ECU check if device has a reader
				// -----------------------------------------------------------------
				if (adapter != null)
				{
					pendingIntent = PendingIntent.getActivity (this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
					IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
					tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
					writeTagFilters = new IntentFilter[] { tagDetected };
				}
				else
				{
					// -------------------------------------------------------------
					// 12/06/2016 ECU indicate that there is no reader then finish
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString (R.string.nfc_no_reader),true);
					startedManually = false;
					finish ();
					// -------------------------------------------------------------
				}
			}
			else
			{
				// -----------------------------------------------------------------
				// 02/09/2013 ECU at this stage do not want the fields to be visible
				// 11/06/2016 ECU added the actions button
				// -----------------------------------------------------------------
				commandActionsButton.setVisibility (View.INVISIBLE);
				commandDataTextView.setVisibility (View.INVISIBLE);
				commandDataTitleTextView.setVisibility (View.INVISIBLE);
				commandNumberButton.setVisibility (View.INVISIBLE);
				commandTextView.setVisibility (View.INVISIBLE);
				commandSpinner.setVisibility (View.INVISIBLE);
				// -----------------------------------------------------------------
				// 02/09/2013 ECU now process the intent received
				// -----------------------------------------------------------------
				processTheIntent (getIntent());
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been recreated after being destroyed so
			//                just finish
			// ---------------------------------------------------------------------
			finish ();
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	@Override
	public void onBackPressed()
	{
		// -------------------------------------------------------------------------
		// 12/06/2016 ECU created to handled the 'back' key
	    // -------------------------------------------------------------------------
		// 12/06/2016 ECU reset the 'started manually' flag
		// -------------------------------------------------------------------------
		startedManually = false;
		// -------------------------------------------------------------------------
		// 12/06/2016 ECU terminate the activity
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
	    super.onBackPressed();			// 08/06/2016 ECU removed
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	 @Override
	 public void onNewIntent (Intent intent) 
	 {
		 if (PublicData.userInterfaceRunning && !startedManually)
		 {
			 // --------------------------------------------------------------------
			 // 02/09/2013 ECU get the new intent
			 // --------------------------------------------------------------------
			 setIntent (intent);
			 // --------------------------------------------------------------------
		 }
		 // ------------------------------------------------------------------------
		 // 02/09/2013 ECU now do the necessary actioning
		 // ------------------------------------------------------------------------
		 processTheIntent (intent);
		 // ------------------------------------------------------------------------
	 }	
	 // ============================================================================
	 @Override
	 public void onPause ()
	 {
		 // ------------------------------------------------------------------------
		 super.onPause();
		 // ------------------------------------------------------------------------
		 // 12/06/2016 ECU disable the despatch of an intent when an NFC tag is read
		 //                when the activity is in the foreground
		 // ------------------------------------------------------------------------
		 if (startedManually)
			 adapter.disableForegroundDispatch (this);
		 // ------------------------------------------------------------------------
	 }
	 // ============================================================================
	 @Override
	 public void onResume ()
	 {
		 // ------------------------------------------------------------------------
		 super.onResume();
		 // ------------------------------------------------------------------------
		 // 12/06/2016 ECU disable the despatch of an intent when an NFC tag is read
		 //                when the activity is in the foreground
		 // ------------------------------------------------------------------------
		 if (startedManually)
			 adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
		 // ------------------------------------------------------------------------
	 }
	 /* ============================================================================ */
	 private Button.OnClickListener buttonOnClickListener = new Button.OnClickListener()
	 {
		 // ------------------------------------------------------------------------
		 // 02/09/2013 ECU added to listen for when the button is clicked
		 // ------------------------------------------------------------------------
		 @Override
		 public void onClick(View theView) 
		 {	
			switch (theView.getId ())
			{
				case R.id.command_button:
					// -------------------------------------------------------------
					// 11/06/2016 ECU check if string to write has already been defined
					// -------------------------------------------------------------
					if (commandString == null)
					{
						// ---------------------------------------------------------
						// 03/09/2013 ECU get the index of the spinner entry
						// ---------------------------------------------------------
						String [] theWords = String.valueOf (commandSpinner.getSelectedItem()).split(" ");
						// ---------------------------------------------------------
						// 22/02/2015 ECU set up the command string that will be written 
						//                to the tag
						// ---------------------------------------------------------
						if (theWords [0].equalsIgnoreCase(StaticData.ACTION_DESTINATION_WEMO))
						{
							commandString = getPackageName() + ":" + StaticData.ACTION_DESTINATION_WEMO + "=" + theWords [1] + " " + theWords [2];	 
						}
						else
						{
							// -----------------------------------------------------
							// 03/09/2013 ECU the first word is the index
							// -----------------------------------------------------				
							int inputCommandNumber = Integer.parseInt (theWords[0]);
							// -----------------------------------------------------
							// 22/02/2015 ECU build the command string
							// -----------------------------------------------------
							commandString = getPackageName() +
											StaticData.ACTION_DELIMITER + 
											StaticData.ACTION_DESTINATION_VOICE + 
											"=" + inputCommandNumber;
							// -----------------------------------------------------
						}
					}
					// -------------------------------------------------------------
					// 11/06/2016 ECU write the generated string to the tag
					// -------------------------------------------------------------
					writeToNFCTag (commandString);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.command_actions_button:
					// -------------------------------------------------------------
					// 11/06/2016 ECU added to obtain the actions that are to be 
					//                written to the tag
					// -------------------------------------------------------------
					DialogueUtilities.multilineTextInput (context,
														  context.getString (R.string.nfc_actions_title),
														  context.getString (R.string.action_command_summary),
														  3,
														  "",
														  Utilities.createAMethod (NFC_Activity.class,"ActionsMethod",""),
														  null,
														  StaticData.NO_RESULT,
														  context.getString (R.string.press_to_define_command));
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.command_remote_button:
					// -------------------------------------------------------------
					// 15/06/2016 ECU create to enable a remote processor to be set
					//                if required or to switch it off
					// -------------------------------------------------------------
					 DialogueUtilities.singleChoice (context,
							 						 context.getString (R.string.nfc_set_remote_processor),
							 						 (devices = Utilities.deviceListAsArray(false)),0, 
							 						 context.getString (R.string.process_remotely),
							 						 Utilities.createAMethod (NFC_Activity.class,"SetRemoteMethod",0),
							 						 context.getString (R.string.process_locally),
							 						 Utilities.createAMethod (NFC_Activity.class,"ResetRemoteMethod",0));
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}
		 }
	 };
	 // =============================================================================
	 public static void ActionsMethod (String theActions)
	 {
		 // ------------------------------------------------------------------------
		 // 11/06/2016 ECU created to store the actions of the tag
		 // ------------------------------------------------------------------------
		 commandString = context.getPackageName() + 
				 		 StaticData.ACTION_DELIMITER + 
				 		 StaticData.ACTION_INTRODUCER + 
				 		 "=" + theActions;
		 // ------------------------------------------------------------------------
		 // 12/06/2016 ECU display the data on the screen
		 // ------------------------------------------------------------------------
		 commandDataTextView.setText (commandString);
		 // ------------------------------------------------------------------------
	 }
	 // ============================================================================
	 public static void checkDeviceResponse (int theResponse)
	 {
		 // ------------------------------------------------------------------------
		 // 17/06/2016 ECU create to respond to the response to a 'are you there'
		 //                message
		 // ------------------------------------------------------------------------
		 if (theResponse != StaticData.NO_RESULT)
		 {
			 // --------------------------------------------------------------------
			 // 17/06/2016 ECU the device that was selected has confirmed its
			 //                presence
			 // --------------------------------------------------------------------
			 // 15/06/2016 ECU tell the user what is happening
			 // --------------------------------------------------------------------
			 Utilities.popToastAndSpeak (String.format (context.getString (R.string.nfc_will_be_processed),(" by " + Utilities.GetDeviceName(PublicData.storedData.nfcRemoteProcessor))),true);
			 // --------------------------------------------------------------------
			 // 15/06/2016 ECU update the button legend
			 // --------------------------------------------------------------------
			 updateRemoteButtonLegend (context);
			 // --------------------------------------------------------------------
		 }
		 else
		 {
			 // --------------------------------------------------------------------
			 // 17/06/2016 ECU the device that was selected has not responded to
			 //                a 'are you there' message
			 // --------------------------------------------------------------------
			 Utilities.popToastAndSpeak(context.getString (R.string.nfc_set_remote_processor_timeout));
			 // --------------------------------------------------------------------
			 // 17/06/2016 ECU reset to local processing
			 // --------------------------------------------------------------------
			 ResetRemoteMethod (0);
			 // --------------------------------------------------------------------
		 }
	 }
	 /* ============================================================================ */
	 @SuppressLint("InlinedApi")
	 NdefMessage[] getNdefMessages (Intent intent) 
	 {
		 // ------------------------------------------------------------------------
		 // 02/09/2013 ECU parse the intent that was sent when the NFC tag is read
		 //                and read any NDEF messages held on the NFC tag
		 // ------------------------------------------------------------------------
	     NdefMessage [] messages = null;
	     // ------------------------------------------------------------------------
	     // 02/09/2013 ECU get the action that is to be taken
	     // ------------------------------------------------------------------------
	     String action = intent.getAction ();
	     // ------------------------------------------------------------------------
	     // 02/09/2013 ECU want to process when a tag is detected or when a valid formatted
	     //                message is found
	     // ------------------------------------------------------------------------
	     if (action.equals (NfcAdapter.ACTION_TAG_DISCOVERED) || 
	    	 action.equals (NfcAdapter.ACTION_NDEF_DISCOVERED)) 
	     {
	    	 // --------------------------------------------------------------------
	    	 // 12/06/2016 ECU check if this activity was started automtically by
	    	 //                the tag being read
	    	 // --------------------------------------------------------------------
	    	 if (!startedManually)
	    	 {
	    		 // ----------------------------------------------------------------
	    		 // 12/06/2016 ECU launched automatically on a read
	    		 // ----------------------------------------------------------------
	    		 // 02/09/2013 ECU get the raw messages from the intent
	    		 // ----------------------------------------------------------------
	    		 Parcelable[] rawMessages = 
	    				 intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
	    		 // ----------------------------------------------------------------
	    		 // 02/09/2013 ECU check if there are any to process
	    		 // ----------------------------------------------------------------  
	    		 if (rawMessages != null) 
	    		 {
	    			 // ------------------------------------------------------------
	    			 // 02/09/2013 ECU in reality there should only be one message
	    			 //                but accommodate all that could be passed
	    			 // ------------------------------------------------------------
	    			 messages = new NdefMessage[rawMessages.length];
	                
	    			 for (int theIndex = 0; theIndex < rawMessages.length; theIndex++)
	    			 {
	    				 messages[theIndex] = (NdefMessage) rawMessages[theIndex];
	    			 }
	    		 }
	    	 }
	    	 else
	    	 {
	    		 Utilities.popToast (getString (R.string.nfc_write_data));
	    	 }
	     } 
	     // ------------------------------------------------------------------------
	     // 02/09/2013 ECU return the messages that have been obtained;
	     //                null will be returned if there are issues
	     // ------------------------------------------------------------------------
	     return messages;
	 }
	 // =============================================================================
	 private boolean isTagWritable (Tag theTag) 
	 {  
		 // ------------------------------------------------------------------------
		 // 02/09/2013 ECU called up to check whether the detected tag is writable (true) 
		 //                or not (false)
		 // 11/06/2016 ECU changed the name
		 // ------------------------------------------------------------------------
		 try 
		 {  
			 // --------------------------------------------------------------------
			 // 02/09/2013 ECU check if the tag supports NDEF 
			 // --------------------------------------------------------------------
			 Ndef ndef = Ndef.get (theTag);  
			 // --------------------------------------------------------------------
			 // 02/09/2013 ECU if null return then tag cannot be used because it does
			 //				   not support NDEF
			 // --------------------------------------------------------------------
			 if (ndef != null) 
			 {  
				 // ----------------------------------------------------------------
				 // 02/09/2013 ECU connect to the tag
				 // ----------------------------------------------------------------
				 ndef.connect ();  
				 // ----------------------------------------------------------------
				 // 02/09/2013 ECU check if the tag is read only - if it is then cannot be used
				 // ----------------------------------------------------------------
				 if (!ndef.isWritable()) 
				 {  
					 // ------------------------------------------------------------
					 // 08/11/2013 ECU the tag is not writable so tell the user
					 // ------------------------------------------------------------	
					 Utilities.popToast (getString (R.string.nfc_read_only));  
					 // ------------------------------------------------------------
					 // 11/06/2016 ECU Note - close the connection and indicate the
					 //                failure to write
					 // ------------------------------------------------------------
					 ndef.close();   
					 return false;  
					 // ------------------------------------------------------------
				 } 
				 // ----------------------------------------------------------------
				 // 11/06/2016 ECU Note - close the connection and indicate that the
				 //                tag is writable
				 // ----------------------------------------------------------------
				 ndef.close();  
				 return true; 
				 // ----------------------------------------------------------------
			 }   
		 } 
		 catch (Exception theException) 
		 {  
			 // --------------------------------------------------------------------
			 // 08/11/2013 ECU use the custom toast
			 // 12/06/2016 ECU indicate that the tag must be read
			 // --------------------------------------------------------------------
			 Utilities.popToastAndSpeak (getString (R.string.nfc_place_on_reader),true);  
			 // --------------------------------------------------------------------
			 // 12/06/2016 ECU indicate that am waiting for the tag to be read
			 // --------------------------------------------------------------------
			 waitingToWrite = true;
			 // --------------------------------------------------------------------
		 }  
		 return false;  
	 } 
	 // ============================================================================
	 @SuppressLint ("InlinedApi")
	 private NdefMessage getTagAsNdef () 
	 {  
		 // ------------------------------------------------------------------------
		 // 02/09/2013 ECU build up the NDEF data that will be written to the tag
		 // 22/02/2015 ECU changed to use the command string
		 // ------------------------------------------------------------------------
		 byte[] commandStringAsBytes = commandString.getBytes (Charset.forName ("US-ASCII"));  
		 // ------------------------------------------------------------------------
		 // 02/09/2013 ECU the payload has a 3 byte header - see the comments
		 //                in processTheIntent for the reasons
		 // ------------------------------------------------------------------------
		 byte[] payload = new byte [commandStringAsBytes.length + LANGUAGE_CODE.length + 1];   
		 // ------------------------------------------------------------------------
		 // 02/09/2013 ECU store the language at head of the payload
		 // ------------------------------------------------------------------------
		 payload[0] = (byte) LANGUAGE_CODE.length;   
		 payload[1] = LANGUAGE_CODE [0];
		 payload[2] = LANGUAGE_CODE [1];
		 // ------------------------------------------------------------------------
		 // 02/09/2013 ECU copy the message into the payload
		 // ------------------------------------------------------------------------
		 System.arraycopy (commandStringAsBytes,0,payload,LANGUAGE_CODE.length + 1,commandStringAsBytes.length);  
		 // ------------------------------------------------------------------------
		 // 02/09/2013 ECU create a NDEF record for the payload
		 // ------------------------------------------------------------------------
		 NdefRecord ndefRecord = new NdefRecord (NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload); 
		 // ------------------------------------------------------------------------
		 // 02/09/2013 ECU return a new message with the record that contains the payload
		 // ------------------------------------------------------------------------
		 return new NdefMessage (new NdefRecord[] {ndefRecord});    
	 }  
	 // ============================================================================
	 @SuppressLint("InlinedApi")
	 void processTheIntent(Intent intent) 
	 {
		 // ------------------------------------------------------------------------
		 // 02/09/2013 ECU parse the information in the intent
		 // ------------------------------------------------------------------------
		 if (intent.getAction ().equals (NfcAdapter.ACTION_NDEF_DISCOVERED)) 
	     {
			 // --------------------------------------------------------------------
			 // 02/09/2013 ECU get the NDEF messages from the intent
			 // --------------------------------------------------------------------
	         NdefMessage[] messages = getNdefMessages (getIntent());
	           
	         try
	         {
	        	 // ----------------------------------------------------------------
	        	 // 02/09/2013 ECU get the payload from the record - only one is on the tag
	        	 //
	        	 // 		payload[0] contains the "Status Byte Encodings" field, per the
	             // 		NFC Forum "Text Record Type Definition" section 3.2.1.
	             //
	             // 		bit7 is the Text Encoding Field.
	             //
	             // 		if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1):
	             // 		The text is encoded in UTF16
	             //
	             // 		Bit_6 is reserved for future use and must be set to zero.
	             //
	             // 		Bits 5 to 0 are the length of the IANA language code.
	             //
	        	 // ----------------------------------------------------------------
	        	 byte[] payload = messages[0].getRecords()[0].getPayload();
	        	 // ----------------------------------------------------------------
	        	 // 02/09/2013 ECU get the text encoding
	        	 // ----------------------------------------------------------------
	        	 String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
	        	 // ----------------------------------------------------------------
	        	 // 02/09/2013 ECU get the language code
	        	 // ----------------------------------------------------------------
	        	 int languageCodeLength = payload[0] & 0077;
	        	 // ----------------------------------------------------------------
	        	 // 02/09/2013 ECU get the text from the payload
	        	 // ----------------------------------------------------------------
	        	 String text = new String (payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
	        	 // ----------------------------------------------------------------
	        	 // 13/09/2013 ECU change the format of the toast message
	        	 //            ECU add the true option to indicate text is to be centred
	        	 // 22/02/2015 ECU commented out the display of information read
	        	 // ----------------------------------------------------------------
	        	 //String languageCode = new String (payload, 1, languageCodeLength, "US-ASCII");
	        	 //Utilities.popToast ("Data Read from the tag\n\n" + text + " (" + languageCode + ")",true);
	        	 // ----------------------------------------------------------------
	        	 // 02/09/2013 ECU try and decode the incoming message which should have the following format
	        	 //                <package name>:command:<command number>
	        	 // ----------------------------------------------------------------
	        	 String [] theWords = text.split ("[:]");
	        	 // ----------------------------------------------------------------
	        	 // 22/02/2015 ECU check if tag is for this application
	        	 // ----------------------------------------------------------------
	        	 if ((theWords[0]).equalsIgnoreCase (getPackageName()))
	        	 {
	        		 // ------------------------------------------------------------
	        		 // 22/02/2015 ECU the tag is for this app so process either
	        		 //                  a voice command
	        		 // ------------------------------------------------------------
	        		 theWords = theWords [1].split ("=");
	        		 
	        		 if (theWords[0].equals (StaticData.ACTION_DESTINATION_VOICE))
	        		 {
	        			 int theCommandNumber = Integer.parseInt(theWords[1]);
	        			 // --------------------------------------------------------
	        			 // 21/02/2015 ECU changed the upper limit '<=' to '<'
	        			 // --------------------------------------------------------
	        			 if (theCommandNumber >= 0 && theCommandNumber < PublicData.voiceCommands.length)
	        			 {
	        				 // ----------------------------------------------------
	        				 // 03/09/2013 ECU start the voice recognition activity
	        				 // ----------------------------------------------------
	        				 Intent myIntent = new Intent (getBaseContext(),VoiceRecognition.class);
	        				 // ----------------------------------------------------
	        				 // 03/09/2013 ECU store the required command number in the intent
	        				 // 06/03/2014 ECU change to use PARAMETER....
	        				 // ----------------------------------------------------
	        				 myIntent.putExtra (StaticData.PARAMETER_COMMAND_NUMBER,theCommandNumber);
	        				 startActivityForResult (myIntent,0);
	        				 // ----------------------------------------------------
	        				 finish ();
	        			 }
	        		 }
	        		 else
	        	     if (theWords[0].equals(StaticData.ACTION_DESTINATION_WEMO))
	        	     {
	        	    	 // --------------------------------------------------------
	        	    	 // 21/02/2015 ECU temporary bodge for WeMo
	        	    	 // --------------------------------------------------------
	        	    	 WeMoActivity.voiceCommands (theWords[1]);
	        	    	 // --------------------------------------------------------
	        	    	 finish ();
	        	     }
	        	     else
	        	     if (theWords[0].equals (StaticData.ACTION_INTRODUCER))
	        	     {
	        	    	 // --------------------------------------------------------
	        	    	 // 11/06/2016 ECU the rest of the message contains actions
	        	    	 //                that are to be processed
	        	    	 //            ECU strip off the start of the message so
	        	    	 //                that am left with the actions to be 
	        	    	 //                processed
	        	    	 // 12/06/2016 ECU add final true to interrupt existing
	        	    	 //                actions
	        	    	 // --------------------------------------------------------
	        	    	 text = text.replace(getPackageName() + StaticData.ACTION_DELIMITER + StaticData.ACTION_INTRODUCER + "=","");
	        	    	 // --------------------------------------------------------
	        	    	 // 15/06/2016 ECU decide whether to process locally or not
	        	    	 // --------------------------------------------------------
	        	    	 if (PublicData.storedData.nfcRemoteProcessor == null)
	        	    	 {
	        	    		 // ----------------------------------------------------
	        	    		 // 15/06/2016 ECU actions are to be processed locally
	        	    		 // ----------------------------------------------------
	        	    		 Utilities.actionHandler (context,text,true);
	        	    		 // ----------------------------------------------------
	        	    	 }
	        	    	 else
	        	    	 {
	        	    		 // ----------------------------------------------------
	        	    		 // 15/06/2016 ECU the actions are to be processed by
	        	    		 //                the previously specified remote device
	        	    		 // ----------------------------------------------------
	        	    		 Utilities.sendSocketMessageSendTheObject (context,
	        														   PublicData.storedData.nfcRemoteProcessor,
	        														   PublicData.socketNumberForData,
	        														   StaticData.SOCKET_MESSAGE_ACTIONS,text);
	        	    		 // ---------------------------------------------------
	        	    	 }
	        	    	 // --------------------------------------------------------
	        	    	 // 11/06/2016 ECU finish this activity
	        	    	 // --------------------------------------------------------
	        	    	 finish ();
	        	    	 // --------------------------------------------------------
	        	     }
	        	     else
	        		 {	 
	        			// 08/11/2013 ECU use the custom toast
	     				
	     				Utilities.popToast (getString (R.string.nfc_invalid_command) + text);
	        		 }
	        	 }
	         }
	         catch (Exception theException)
	         {        	 
	         }
	     }
		 else
		 {
			 // --------------------------------------------------------------------
			 // 02/09/2013 ECU remember the tag has been detected
			 // --------------------------------------------------------------------
			 detectedTag = intent.getParcelableExtra (NfcAdapter.EXTRA_TAG);  
			 // --------------------------------------------------------------------
			 // 02/09/2013 ECU want to write to tag so show the necessary fields
			 // 11/06/2016 ECU added the actions button
			 // 12/06/2016 ECU added 'commandData'....
			 // --------------------------------------------------------------------
			 commandActionsButton.setVisibility (View.VISIBLE);
			 commandDataTextView.setVisibility (View.VISIBLE);
			 commandDataTitleTextView.setVisibility (View.VISIBLE);
			 commandNumberButton.setVisibility (View.VISIBLE);
			 commandTextView.setVisibility (View.VISIBLE);
			 commandSpinner.setVisibility (View.VISIBLE); 
			 // --------------------------------------------------------------------
		 }
		 // ------------------------------------------------------------------------
		 // 12/06/2016 ECU check if waiting to write
		 // ------------------------------------------------------------------------
		 if (waitingToWrite)
		 {
			 waitingToWrite = false;
			 Utilities.popToastAndSpeak (String.format (getString(R.string.nfc_press_write_key),getString (R.string.write_command_to_tag)),true);
		 }	 
		 // ------------------------------------------------------------------------
	 }	 
	 // ============================================================================
	 public static void ResetRemoteMethod (int theIndex)
	 {
		 // ------------------------------------------------------------------------
		 // 15/06/2016 ECU created to reset the remote processor
		 // ------------------------------------------------------------------------
		 PublicData.storedData.nfcRemoteProcessor = null;
		 // ------------------------------------------------------------------------
		 // 15/06/2016 ECU tell the user what is happening
		 // ------------------------------------------------------------------------
		 Utilities.popToastAndSpeak (String.format (context.getString(R.string.nfc_will_be_processed)," locally"),true);
		 // ------------------------------------------------------------------------
		 // 15/06/2016 ECU update the button legend
		 // ------------------------------------------------------------------------
		 updateRemoteButtonLegend (context);
		 // ------------------------------------------------------------------------
	 }
	 // ============================================================================
	 public static void SetRemoteMethod (int theIndex)
	 {
		 // ------------------------------------------------------------------------
		 // 15/06/2016 ECU created to set the remote processor
		 // ------------------------------------------------------------------------
		 PublicData.storedData.nfcRemoteProcessor = Devices.returnIPAddress (devices [theIndex]);
		 // ------------------------------------------------------------------------
		 // 17/06/2016 ECU check if can communicate with the remote device
		 // ------------------------------------------------------------------------
		 Utilities.popToastAndSpeak (context.getString (R.string.nfc_set_remote_processor_checking),true);
		 // ------------------------------------------------------------------------
		 // 17/06/2016 ECU send a message to check that the device is available
		 // ------------------------------------------------------------------------
		 MessageHandler_Message localNFCMessage = new MessageHandler_Message (PublicData.storedData.nfcRemoteProcessor,
				 									  new MethodDefinition<NFC_Activity> (NFC_Activity.class,"checkDeviceResponse"),
				 									  StaticData.NFC_TIMEOUT);
		 Message localMessage = PublicData.messageHandler.obtainMessage (StaticData.MESSAGE_CHECK_DEVICE,localNFCMessage);
		 PublicData.messageHandler.sendMessage (localMessage);
		 // ------------------------------------------------------------------------
	 }
	 // ============================================================================
	 static void updateRemoteButtonLegend (Context theContext)
	 {
		 // ------------------------------------------------------------------------
		 // 15/06/2016 ECU created because called from a couple of places
		 // ------------------------------------------------------------------------
		 // 15/06/2016 ECU change the legend on the button
		 // 16/06/2016 ECU changed to use the new method which returns spannable
		 // ------------------------------------------------------------------------
		 commandRemoteButton.setText (Utilities.twoLineButtonLegend (theContext,
				 theContext.getString (R.string.nfc_remote_processor),	 
				 (PublicData.storedData.nfcRemoteProcessor == null) ? String.format (theContext.getString (R.string.nfc_current_processor)," locally")
						 										    : String.format (theContext.getString (R.string.nfc_current_processor)," by " + Utilities.GetDeviceName (PublicData.storedData.nfcRemoteProcessor))));
		 // ------------------------------------------------------------------------
	 }
	 // ============================================================================
	 public static boolean validation (int theArgument)
	 {
		 // ------------------------------------------------------------------------
		 // 12/06/2016 ECU created to check if device has an enabled NFC reader
		 // ------------------------------------------------------------------------
		 NfcManager manager = (NfcManager) MainActivity.activity.getSystemService(Context.NFC_SERVICE);
		 NfcAdapter adapter = manager.getDefaultAdapter();
		 if (adapter != null && adapter.isEnabled()) 
		 {
			 return true;
		 }
		 else
		 {
			 return false;
		 }
		 // ------------------------------------------------------------------------
	 }
	// =============================================================================
	private class WriteResponse 
	{
		// -------------------------------------------------------------------------
		// 11/06/2016 ECU Note - declare local variables
		// -------------------------------------------------------------------------
		String 		message; 
		boolean 	status; 				// false ..... error occurred 
		// -------------------------------------------------------------------------
		WriteResponse (boolean Status,String Message) 
		{  
			// ---------------------------------------------------------------------
			// 11/06/2016 ECU Note - constructor to set the private variables
			// ---------------------------------------------------------------------
			this.status  = Status;  	
			this.message = Message;  
		}  
		// -------------------------------------------------------------------------
		public String Print ()
		{
			// ---------------------------------------------------------------------
			// 11/06/2016 ECU print summary of the object
			// ---------------------------------------------------------------------
			return (status ? "Success: " : "Failed: ") + message; 
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}  
	// =============================================================================
	public WriteResponse writeTag (NdefMessage theMessage, Tag theTag) 
	{  
		// -------------------------------------------------------------------------
		// 02/09/2013 ECU get the length of the message to be written
		// 11/06/2016 ECU Note - want to perform the write to the tag and then
		//                return with the 'operation status' and associated message
		//            ECU changed calls to WriteResponse to use 'false' to indicate
		//                an error, or 'true' if operation succeeded
		// -------------------------------------------------------------------------
		int size = theMessage.toByteArray ().length;  
		// -------------------------------------------------------------------------
		try 
		{  
			// ---------------------------------------------------------------------
			// 11/06/2016 ECU get an instance of the NDEF instance
			// ---------------------------------------------------------------------
			Ndef ndef = Ndef.get (theTag);
			// ---------------------------------------------------------------------
			// 02/09/2013 ECU check if the tag can accept NDEF
			// ---------------------------------------------------------------------
			if (ndef != null) 
			{  
				// -----------------------------------------------------------------
				// 02/09/2013 ECU tag is NDEF acceptable so connect to the tag
				// -----------------------------------------------------------------
				ndef.connect ();
				// -----------------------------------------------------------------
				// 02/09/2013 ECU Check if tag is not writable
				// -----------------------------------------------------------------
				if (!ndef.isWritable ()) 
				{  
					// -------------------------------------------------------------
					// 02/09/2013 ECU cannot write to the detected tag
					// -------------------------------------------------------------
					return new WriteResponse (false,getString (R.string.nfc_read_only));  
					// -------------------------------------------------------------
				} 
				// -----------------------------------------------------------------
				// 02/09/2013 ECU check of there is space on the tag for the data
				// -----------------------------------------------------------------
				if (ndef.getMaxSize() < size) 
				{  
					// -------------------------------------------------------------
					// 02/09/2013 ECU there is insufficient space on the tag for the message
					// -------------------------------------------------------------
					return new WriteResponse (false,String.format (getString (R.string.nfc_too_big),ndef.getMaxSize(),size));  
				}  
				// -----------------------------------------------------------------
				// 02/09/2013 ECU can now write out the message in NDEF
				// -----------------------------------------------------------------
				ndef.writeNdefMessage (theMessage); 
				// -----------------------------------------------------------------
				// 02/09/2013 ECU check if need to make the tag 'read only'
				// -----------------------------------------------------------------
				if (writeProtect) 
					ndef.makeReadOnly ();  
				// -----------------------------------------------------------------
				return new WriteResponse (true,getString (R.string.nfc_written_formatted_tag)); 
				// -----------------------------------------------------------------
			}
			else 
			{  
				NdefFormatable format = NdefFormatable.get (theTag); 
				// -----------------------------------------------------------------
				// 02/09/2013 ECU check if the tag is formattable
				// -----------------------------------------------------------------
				if (format != null) 
				{  
					// -------------------------------------------------------------
					// 02/09/2013 ECU tag is formattable so perform the format
					// -------------------------------------------------------------
					try 
					{  
						// ---------------------------------------------------------
						// 02/09/2013 ECU connect to the tag, format it and then
						//                write the message to it
						// ---------------------------------------------------------
						format.connect ();  
						format.format (theMessage); 
						// ---------------------------------------------------------
						return new WriteResponse (true,getString (R.string.nfc_formatted_then_written));  
					} 
					catch (IOException theException) 
					{  
						return new WriteResponse (false,getString (R.string.nfc_format_failed));  
					}  
				}   
				else 
	    		{  
	    			 return new WriteResponse (false,getString (R.string.nfc_ndef_no_support));  
	    		}  
	    	 }  
	     } 
	     catch (Exception theException) 
	     {  
	    	 return new WriteResponse (false,getString (R.string.nfc_write_failed));  
	   	}  
	 }
	 /* ============================================================================ */
	 void writeToNFCTag (String theMessageToWrite)
	 {
		 // ------------------------------------------------------------------------
		 // 11/06/2016 ECU created to write the specified message to the tag
		 // ------------------------------------------------------------------------
		 // 22/02/2015 ECU now write out the information
		 // ------------------------------------------------------------------------
		 // check if tag is writable (to the extent that we can)  
		 // ------------------------------------------------------------------------
		 if (isTagWritable (detectedTag)) 
		 {  
			 // --------------------------------------------------------------------
			 // 02/09/2013 ECU if the tag is writable then write out the data
			 // --------------------------------------------------------------------
			 WriteResponse writeResponse = writeTag (getTagAsNdef(),detectedTag);  
			 // --------------------------------------------------------------------
			 // 08/11/2013 ECU use the custom toast
			 // 11/06/2016 ECU use the new print method
			 // --------------------------------------------------------------------
			 Utilities.popToast (writeResponse.Print()); 
			 // --------------------------------------------------------------------
			 // 02/09/2013 ECU just want to exit this application
			 // 12/06/2016 ECU reset the manually started flag
			 // --------------------------------------------------------------------
			 startedManually = false;
			 finish ();
			 // --------------------------------------------------------------------
		 } 
		 // ------------------------------------------------------------------------
	 }
	 // ============================================================================
}

package com.usher.diboson;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

public class EMailActivity extends DibosonActivity 
{
	// =============================================================================
	// 11/07/2015 ECU created. This activity enables an email to be generated and
	//                sent manually
	// 14/07/2015 ECU changed to use the 'EmailMessage' class
	// 22/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	//==============================================================================
	//private static final String TAG = "EMailActivity";
	// =============================================================================
	// =============================================================================
	// 11/07/2015 ECU declare any required variables
	// -----------------------------------------------------------------------------
	static 	Context			context;
	static  EmailMessage	emailMessage;				// 14/07/2015 ECU added
	static  MessageHandler 	messageHandler;
	static  boolean 		sendOnExit	= true;			// 14/07/2015 ECU added
	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 22/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 11/07/2015 ECU set up some common features for this activity
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this);

			setContentView(R.layout.activity_email);
			// ---------------------------------------------------------------------
			// 11/07/2015 ECU remember the current context
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 14/07/2015 ECU check if any parameters fed across
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();		
			if(extras != null) 
			{
				// -----------------------------------------------------------------
				// 1407/2015 ECU check whether an email should be sent on exit
				// -----------------------------------------------------------------
				sendOnExit = extras.getBoolean (StaticData.PARAMETER_SEND_ON_EXIT); 	
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 14/07/2015 ECU create the object that will hold the email details
			// ---------------------------------------------------------------------
			emailMessage = new EmailMessage ();
			// ---------------------------------------------------------------------
			// 12/07/2015 ECU declare the message handler
			// ---------------------------------------------------------------------
			messageHandler = new MessageHandler ();
			// ---------------------------------------------------------------------
			// 11/07/2015 ECU request the recipients for the email
			// ---------------------------------------------------------------------
			DialogueUtilities.textInput (context,"Email Recipients",
	    		   "Please enter the address(es) that are to receive the email that you are entering",
	    		   PublicData.emailDetails.recipients,
	    		   Utilities.createAMethod (EMailActivity.class,"EmailRecipients",StaticData.BLANK_STRING),
	    		   Utilities.createAMethod (EMailActivity.class,"Cancel",StaticData.BLANK_STRING));
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
	// =============================================================================
	
	// =============================================================================
	// =============================================================================
	// 11/07/2015 ECU declare the methods used by the dialogue
	// =============================================================================
	// =============================================================================
	 public static void Cancel (String theArgument)
	 {
		 // ------------------------------------------------------------------------
		 // 11/07/2015 ECU created to handle any cancellation options
		 // ------------------------------------------------------------------------
		 
		 // ------------------------------------------------------------------------
	 }
	 // ============================================================================
	 public static void EmailRecipients (String theRecipients)
	 {
		 // ------------------------------------------------------------------------
		 // 11/07/2015 ECU created to handle the entered recipient addresses
		 // 14/07/2015 ECU changed to use the 'emailMessage' object
		 // ------------------------------------------------------------------------
		 emailMessage.recipients = theRecipients;
		 // ------------------------------------------------------------------------
		 // 11/07/2015 ECU request the subject of the email
		 // 03/10/2015 ECU indicate that the text is to be used as a HINT
		 // -------------------------------------------------------------------------
		 DialogueUtilities.textInput (context,"Email Subject",
		    		   "Please enter the subject of this email",
		    		   StaticData.HINT + "Enter subject",
		    		   Utilities.createAMethod (EMailActivity.class,"EmailSubject",StaticData.BLANK_STRING),
		    		   Utilities.createAMethod (EMailActivity.class,"Cancel",StaticData.BLANK_STRING));
		 // ------------------------------------------------------------------------
	 }
	 // ============================================================================
	 public static void EmailSubject (String theSubject)
	 {
		 // ------------------------------------------------------------------------
		 // 11/07/2015 ECU created to handle the entered subject
		 // 14/07/2015 ECU changed to use the 'emailMessage' object
		 // ------------------------------------------------------------------------
		 emailMessage.subject = theSubject;
		 // ------------------------------------------------------------------------
		 // 11/07/2015 ECU now request the body of the message
		 // 03/10/2015 ECU indicate that a hint is required
		 // ------------------------------------------------------------------------
		 DialogueUtilities.multilineTextInput (context,
				 							  "Email Message",
				 							  "Please enter the body of this email",
				 							  25,
				 							  StaticData.HINT + "Enter any text that you want to send",
				 							  Utilities.createAMethod (EMailActivity.class,"EmailMessage",StaticData.BLANK_STRING),
				 							  Utilities.createAMethod (EMailActivity.class,"Cancel",StaticData.BLANK_STRING));
		 // ------------------------------------------------------------------------
	 }
	 // ============================================================================
	 public static void EmailMessage (String theMessage)
	 {
		 // ------------------------------------------------------------------------
		 // 11/07/2015 ECU created to handle the entered message
		 // 14/07/2015 ECU store the body in the emailMessage object and then send
		 //                the message
		 // ------------------------------------------------------------------------
		 emailMessage.message = theMessage;
		 // ------------------------------------------------------------------------
		 // 14/07/2015 ECU optionally send the message
		 // ------------------------------------------------------------------------
		 if (sendOnExit)
			 emailMessage.Send (context);
		 // ------------------------------------------------------------------------
		 // 12/07/2015 ECU finish this activity
		 // ------------------------------------------------------------------------
		 messageHandler.sendEmptyMessage (StaticData.MESSAGE_FINISH);
		 // ------------------------------------------------------------------------
	}
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class MessageHandler extends Handler
	{
		@Override
		public void handleMessage (Message theMessage) 
		{  
			switch (theMessage.what)
			{
			// ---------------------------------------------------------------------
			case StaticData.MESSAGE_FINISH:
				// -----------------------------------------------------------------
				// 14/07/2015 ECU store the email message in the intent to pass back
				// -----------------------------------------------------------------
				Bundle resultData = new Bundle();
				resultData.putSerializable (StaticData.PARAMETER_EMAIL_MESSAGE,emailMessage);
				Intent resultIntent = new Intent();
		    	resultIntent.putExtras (resultData);
		    	setResult (RESULT_OK, resultIntent);
				//------------------------------------------------------------------
				// 12/07/2015 ECU finish the activity
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
	    		break;
	        	// -----------------------------------------------------------------
	        }
	        // ---------------------------------------------------------------------
	    }
	}
	// =============================================================================
}

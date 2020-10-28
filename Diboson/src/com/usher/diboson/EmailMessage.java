package com.usher.diboson;

import android.content.Context;
import java.io.Serializable;
import java.util.Arrays;

public class EmailMessage implements Serializable
{
	// =============================================================================
	// 14/07/2015 ECU created to hold the details of an email message
	//
	//				  recipients .... the email address(es) to which the message
	//                                is to be sent
	//				  subject ....... the 'subject' of this email message
	//				  message ....... the body of the email message
	//                attachments ... any attachments      (04/12/2019 added)
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	private static final String TAG = "EmailMessage";
	// =============================================================================
	// 05/12/2019 ECU meanings :-
	// 					EMAIL_DELAY ........ the delay in mS after the result of a
	// 									     transmission before triggering the next
	//									     scan of any queued messages
	//                  EMAIL_MAX_RETRIES .. the maximum retries following the
	//                                       failure of a transmission
	//                  EMAIL_RETRY_DELAY .. the delay after a failure before trying
	//                                       to resend the current message
	// -----------------------------------------------------------------------------
	private static final int EMAIL_DELAY 		= 	10 * 1000;
	private static final int EMAIL_MAX_RETRIES	=	3;
	private static final int EMAIL_RETRY_DELAY	=	30 * 1000;
	// -----------------------------------------------------------------------------
	private static		 int retries;
	// =============================================================================
	public String []	attachments;
	public String       extras;
	public String		message;
	public String		recipients;
	public String		subject;
	//==============================================================================

	// =============================================================================
	public EmailMessage ()
	{
		// -------------------------------------------------------------------------
		// 05/12/2019 ECU preset any variables
		// -------------------------------------------------------------------------
		retries = EMAIL_MAX_RETRIES;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public EmailMessage (String 	theRecipients,
	                     String 	theSubject,
	                     String 	theMessage,
	                     String     theExtras,
	                     String [] 	theAttachments)
	{
		// -------------------------------------------------------------------------
		// 04/12/2019 ECU copy across the data
		// -------------------------------------------------------------------------
		if (theAttachments != null)
			attachments = Arrays.copyOf (theAttachments,theAttachments.length);
		else
			attachments = null;
		extras		= theExtras;
		message		= theMessage;
		recipients	= theRecipients;
		subject		= theSubject;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    public EmailMessage (String 	theSubject,
                         String 	theMessage,
                         String     theExtras,
                         String [] 	theAttachments)
    {
        // -------------------------------------------------------------------------
        // 05/12/2019 ECU created when no recipient is specified - eventually it will
        //                be sent to the 'nominated' recipient(s) - set in settings
        // -------------------------------------------------------------------------
        this (null,theSubject,theMessage,theExtras,theAttachments);
        // -------------------------------------------------------------------------
    }
	// =============================================================================

	// =============================================================================
	public void Send ()
	{
		// -------------------------------------------------------------------------
		// 04/12/2019 ECU created to send a full email message - called to send a
		//                'stored' message
		//            ECU log the fact - just for monitoring
		// 05/12/2019 ECU decide how to send depending on whether the recipient(s)
		//                have been specified
		// -------------------------------------------------------------------------
		Utilities.LogToProjectFile (TAG,"Send : " + StaticData.NEWLINE + Print());
		// -------------------------------------------------------------------------
		// 04/12/2019 ECU now send the message
		// -------------------------------------------------------------------------
		if (recipients == null)
        {
            // ---------------------------------------------------------------------
            // 05/12/2019 ECU because the recipient(s) has not been specified then this
            //                message is destined for the 'nominated' addressees
            // ---------------------------------------------------------------------
		    Utilities.SendEmailMessage (MainActivity.activity,
			    						subject,
				    					message,
					    				extras,
						    			attachments);
		    // ---------------------------------------------------------------------
        }
        else
        {
            // ---------------------------------------------------------------------
            // 05/12/2019 ECU recipients have been specified so use the normal
            //                method to send the message to them
            // ---------------------------------------------------------------------
            Send (MainActivity.activity);
            // ---------------------------------------------------------------------
        }
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void Send (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 14/07/2015 ECU public method to send the defined email message
		// -------------------------------------------------------------------------
		Utilities.SendEmailMessage (theContext,
				 					recipients,
				 					subject,
				 					message,
				 					null,
				 					(String []) null);
		// -------------------------------------------------------------------------
		
	}
	// =============================================================================
	public static void TransmissionComplete (boolean theResult)
	{
		// -------------------------------------------------------------------------
		// 11/12/2019 ECU created to have a generalised 'completion' method which
		//                resets flags dependent on the result
		//					 theResult .. true ... transmission successfully completed
		//                             ...false... transmission failed - reason will
		//                                         logged elsewhere
		// -------------------------------------------------------------------------
		if (theResult)
		{
			// ---------------------------------------------------------------------
			// 11/12/2019 ECU logged the fact that transmission was successful
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG,"Transmission Successful");
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 11/12/2019 ECU reset the flags - no matter whether successful or not
		// -------------------------------------------------------------------------
		// 05/12/2019 ECU created when Utilities.SendEmailMessage has successfully
		//                transmitted an email
		// -------------------------------------------------------------------------
		// 05/12/2019 ECU indicate that all is done
		// -------------------------------------------------------------------------
		PublicData.emailDetails.sending = false;
		// -------------------------------------------------------------------------
		// 05/12/2019 ECU reset the number of retries
		// -------------------------------------------------------------------------
		retries = EMAIL_MAX_RETRIES;
		// -------------------------------------------------------------------------
		// 04/12/2019 ECU indicate that an email has been sent
		// 06/12/2019 ECU changed to 'emailHandler' from 'messageHandler'
		// -------------------------------------------------------------------------
		PublicData.emailHandler.sendEmptyMessageDelayed (StaticData.MESSAGE_EMAIL_SENT,EMAIL_DELAY);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void TransmissionFailure (EmailMessage theMessage)
	{
		// -------------------------------------------------------------------------
		Utilities.LogToProjectFile (TAG,"Transmission Failed   Retries : " + retries +
										StaticData.NEWLINE + theMessage.Print());
		// -------------------------------------------------------------------------
		// 05/12/2019 ECU called when there has been a transmission failure
		// -------------------------------------------------------------------------
		// 05/12/2019 ECU check if the number of retries has been exhausted
		// -------------------------------------------------------------------------
		if (retries > 0)
		{
			// ---------------------------------------------------------------------
			// 05/12/2019 ECU can still try to repeat the transmission
			// ---------------------------------------------------------------------
			// 05/12/2019 ECU add the specified message into the queue
			// 13/12/2019 ECU the message that has failed was 'first' so put it
			//                back in that position rather than adding to the queue
			// ---------------------------------------------------------------------
			PublicData.emailMessages.add (0,theMessage);
			// ---------------------------------------------------------------------
			// 05/12/2019 ECU indicate that the previous transmission is over
			// ---------------------------------------------------------------------
			PublicData.emailDetails.sending = false;
			// ---------------------------------------------------------------------
			// 05/12/2019 ECU decrement the number of tries
			// ---------------------------------------------------------------------
			retries--;
			// ---------------------------------------------------------------------
			// 13/12/2019 ECU indicate to the user that an error occurred
			// 16/12/2019 ECU tell the handler that an error has occurred
			// 17/12/2019 ECU changed to use new method in MonitorHandler
			// ---------------------------------------------------------------------
			MonitorHandler.TransmissionFailure ();
			// ---------------------------------------------------------------------
			// 04/12/2019 ECU indicate that an email has been sent
			// 06/12/2019 ECU changed to use emailHandler from messageHandler
			// ---------------------------------------------------------------------
			PublicData.emailHandler.sendEmptyMessageDelayed (StaticData.MESSAGE_EMAIL_SENT,EMAIL_RETRY_DELAY);
			// --------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 05/12/2019 ECU have exhausted the number of retries so must have
			//                a major issue
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG,"Maximum retries exceeded");
			// ---------------------------------------------------------------------
			// 05/12/2019 ECU clear everything so that other emails can progress
			// 11/12/2019 ECU changed to use ...Complete method
			// ---------------------------------------------------------------------
			TransmissionComplete (false);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void TransmissionSuccess ()
	{
		// -------------------------------------------------------------------------
		// 05/12/2019 ECU created when Utilities.SendEmailMessage has successfully
		//                transmitted an email
		// 11/12/2019 ECU changed to use the ...Complete method
		// -------------------------------------------------------------------------
		TransmissionComplete (true);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	String Print ()
	{
		// -------------------------------------------------------------------------
		// 28/11/2017 ECU print the details of the email message
		// 17/12/2019 ECU add details of any attachments
		// -------------------------------------------------------------------------
		String emailString =  "Recipients : " 	+ recipients + StaticData.NEWLINE +
							  "Subject : " 		+ subject + StaticData.NEWLINE +
							  "Message : " 		+ message;
		// -------------------------------------------------------------------------
		// 17/12/2019 ECU check if there are any attachments
		// -------------------------------------------------------------------------
		if (attachments != null)
		{
			// ---------------------------------------------------------------------
			emailString += StaticData.NEWLINE + "Attachments : ";
			// ---------------------------------------------------------------------
			// 17/12/2019 ECU loop through all attachments
			// ---------------------------------------------------------------------
			for (String attachment : attachments)
			{
				emailString += StaticData.NEWLINE + StaticData.INDENT + attachment;
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 17/12/2019 ECU return the summary string
		// -------------------------------------------------------------------------
		return emailString;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

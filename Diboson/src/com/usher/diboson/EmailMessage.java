package com.usher.diboson;

import java.io.Serializable;

import android.content.Context;

public class EmailMessage implements Serializable
{
	// =============================================================================
	// 14/07/2015 ECU created to hold the details of an email message
	//
	//				  recipients .... the email address(es) to which the message
	//                                is to be sent
	//				  subject ....... the 'subject' of this email message
	//				  message ....... the body of the email message
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	public String	message;			
	public String	recipients;
	public String	subject;
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
	String Print ()
	{
		// -------------------------------------------------------------------------
		// 28/11/2017 ECU print the details of the email message
		// -------------------------------------------------------------------------
		return "Recipients : " 	+ recipients + StaticData.NEWLINE +
			   "Subject : " 	+ subject + StaticData.NEWLINE +
			   "Message : " 	+ message;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

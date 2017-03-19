package com.usher.diboson;

import java.io.Serializable;

import android.content.Context;

public class EmailDetails implements Serializable
{
	/* ============================================================================= */
	// 04/01/2014 ECU created
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	public boolean	enabled;			// whether email is being used
	public String 	recipients;			// who will receive emails
	public String	SMTPPort;			// port used for SMTP communication
	public String	SMTPServer;			// name of SMTP server
	public String	SMTPUserName;		// user name used for logging in
	public String 	SMTPPassword;		// password associated with user name
	public String   signature;			// signature attached to all outgoing emails
	// -----------------------------------------------------------------------------
	// 28/11/2014 ECU added the following fields which are the basic fields
	//                of an email message. Added here to facilitate the timed
	//                transmission of a message from the TimerService
	// -----------------------------------------------------------------------------
	public String	extras	= null;
	public String	message	= null;
	public String	subject	= null;
	public boolean	timed  	= false;
	/* ============================================================================= */
	public EmailDetails (String theServer,String thePort,String theUserName,String thePassword,String theRecipients,boolean theEnabledFlag)
	{
		// -------------------------------------------------------------------------
		// 04/01/2014 copy across the data
		// -------------------------------------------------------------------------
		enabled			=   theEnabledFlag;
		recipients		= 	theRecipients;
		// -------------------------------------------------------------------------
		// 04/01/2014 ECU set SMTP details
		// -------------------------------------------------------------------------
		SMTPPort		= 	thePort;
		SMTPServer		=	theServer;
		SMTPUserName	=	theUserName;
		SMTPPassword	= 	thePassword;	
	}
	/* ----------------------------------------------------------------------------- */
	public EmailDetails ()
	{
		// 03/02/2014 ECU dummy constructor for use by later methods
	}
	/* ============================================================================= */
	 public boolean CheckForChanges (EmailDetails theEmailDetails)
	    {
	    	// ---------------------------------------------------------------------
	    	// 03/02/2014 ECU created - checks whether supplied record differs from
	    	//                the current one
	    	// ---------------------------------------------------------------------
	    	if ((theEmailDetails.enabled != enabled) ||
	    			(!theEmailDetails.recipients.equalsIgnoreCase(recipients)) ||
	    			(!theEmailDetails.signature.equalsIgnoreCase(signature)) ||
	    			(!theEmailDetails.SMTPPort.equalsIgnoreCase(SMTPPort)) ||
	    			(!theEmailDetails.SMTPServer.equalsIgnoreCase(SMTPServer)) ||
	    			(!theEmailDetails.SMTPUserName.equalsIgnoreCase(SMTPUserName)) ||
	    			(!theEmailDetails.SMTPPassword.equalsIgnoreCase(SMTPPassword)))
	    		return false;
	    	else
	    		return true;
	    }
	    /* ========================================================================= */
	public String Print ()
	{
		return "SMTP Server : " + SMTPServer + "\n" +
			    "SMTP Port : " + SMTPPort + "\n" +
				"SMTP User Name : " + SMTPUserName + "\n" +
			    "SMTP Password : " + SMTPPassword + "\n" +
				"Recipients : " + recipients + "\n" +
			    "Signature : " + signature;
	}
	/* ============================================================================= */
	public EmailDetails ReturnEmailDetails ()
	{
		// -------------------------------------------------------------------------
		// 03/02/2014 ECU created
		// -------------------------------------------------------------------------
		EmailDetails emailDetails = new EmailDetails ();
		
		emailDetails.enabled 		= enabled;
		emailDetails.recipients		= recipients;
		emailDetails.signature	 	= signature;
		emailDetails.SMTPPassword 	= SMTPPassword;
		emailDetails.SMTPPort 		= SMTPPort;
		emailDetails.SMTPServer 	= SMTPServer;
		emailDetails.SMTPUserName 	= SMTPUserName;
		
		return emailDetails;
	}
	// ==========================================================================
	public String Signature (Context theContext)
	{
		// ----------------------------------------------------------------------
		// 27/03/2016 ECU created to return the signature if it has been set or
		//                to set it if it hasn't
		// ----------------------------------------------------------------------
		if (signature == null || signature.equalsIgnoreCase(""))
			signature = theContext.getString(R.string.email_signature);
		// ----------------------------------------------------------------------
		return signature;
		// ----------------------------------------------------------------------
	}
	/* ========================================================================== */
	public void TimedEmail (Context theContext,String theSubject,String theMessage,String theExtras)
	{
		// ----------------------------------------------------------------------
		// 28/11/2014 ECU added to set up the details of a timed email message
		// ----------------------------------------------------------------------
		if (enabled)
		{
			// ------------------------------------------------------------------
			// 28/11/2014 ECU set up the fields in the message
			// ------------------------------------------------------------------
			extras	= theExtras;
			message = theMessage;
			subject = theSubject;
			// ------------------------------------------------------------------
			// 28/11/2014 ECU want to indicate that the message is to be sent
			// ------------------------------------------------------------------
			timed	= true;
		}
		// ----------------------------------------------------------------------
	}
	// --------------------------------------------------------------------------
	public void TimedEmail (Context theContext)
	{
		// ----------------------------------------------------------------------
		// 28/11/2014 ECU send the timed email message and reset
		// ----------------------------------------------------------------------
		if (timed)
		{
			// ------------------------------------------------------------------
			// 28/11/2014 ECU there is an email message to send
			//                1) indicate that it has been processed
			//                2) actually do the transmission
			// ------------------------------------------------------------------
			timed = false;
			Utilities.SendEmailMessage(theContext,subject,message,extras);
			// ------------------------------------------------------------------
		}
	}
	/* ========================================================================== */
}

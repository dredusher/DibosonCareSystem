package com.usher.diboson;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SMTPFunctions extends javax.mail.Authenticator 
{
	/* ============================================================================= */
	// 04/01/2014 ECU created
	// 24/04/2015 ECU updated to use mulitple parts so that attachments can be handled
	/* ============================================================================= */
	    private String 	SMTPServer;
	    private String 	user;
	    private String 	password;
	    private Session session;
	/* ============================================================================= */	 
	static 
	{
		// -------------------------------------------------------------------------
		// 04/01/2013 ECU add the provider for 'secure socket'
		// -------------------------------------------------------------------------
		Security.addProvider(new JSSEProvider());
	}
	/* ============================================================================= */
	public SMTPFunctions (String theSMTPServer,
						  String theSMTPPort,
						  String theUser, 
						  String thePassword)
	{
		// -------------------------------------------------------------------------
		// 04/01/2014 ECU store local variables
		// -------------------------------------------------------------------------
	    this.SMTPServer = theSMTPServer;	    	
	    this.user     	= theUser;
	    this.password 	= thePassword;
	    // -------------------------------------------------------------------------
	    // 04/01/2014 ECU build up the properties required when sending the email
	    // -------------------------------------------------------------------------
	    Properties properties 	= new Properties();
	    properties.setProperty	("mail.transport.protocol", "smtp");
	    properties.setProperty	("mail.host", SMTPServer);
	    properties.put			("mail.smtp.auth", "true");
	    properties.put			("mail.smtp.port", theSMTPPort);
	    properties.put			("mail.smtp.starttls.enable","true");   
	    properties.setProperty	("mail.smtp.quitwait", "false");
	    // -------------------------------------------------------------------------
	    // 04/01/2014 ECU establish a session using the defined properties
	    // -------------------------------------------------------------------------
	    session = Session.getDefaultInstance(properties, this);
	}
	/* ============================================================================= */	 
	protected PasswordAuthentication getPasswordAuthentication() 
	{
		return new PasswordAuthentication (user, password);
	}
	/* ============================================================================= */ 
	public synchronized void sendMail (String subject, 
									   String body, 
									   String sender, 
									   String recipients,
									   String [] attachments) throws Exception 
	{
		// -------------------------------------------------------------------------
		// 04/01/2014 ECU created to send an email with the specified content
		// 24/04/2015 ECU added 'attachment' as an argument. The name of the
		//                file to be attached or 'null' if there is no attachment
		// 25/04/2015 ECU changed to be able to have a number of attachments
		// -------------------------------------------------------------------------
		MimeMessage message = new MimeMessage (session);
	    message.setSender  (new InternetAddress(sender));
	    message.setSubject (subject);
	    // -------------------------------------------------------------------------
	    // 04/01/2014 ECU handle one or multiple recipients
	    // -------------------------------------------------------------------------
	    if (recipients.indexOf(',') > 0)
	    	message.setRecipients (Message.RecipientType.TO, InternetAddress.parse(recipients));
	    else
	    	message.setRecipient (Message.RecipientType.TO, new InternetAddress(recipients));
	    // -------------------------------------------------------------------------
	    // 24/04/2015 ECU set up the parts of the message, first the supplied 'body'
	    //                then the attachment, if supplied
    	// ---------------------------------------------------------------------
    	Multipart multiPart = new MimeMultipart ();
    	// ---------------------------------------------------------------------
    	MimeBodyPart messageBodyPart = new MimeBodyPart(); 
    	messageBodyPart.setContent (body,"text/html"); 
    	multiPart.addBodyPart (messageBodyPart); 
	    // -------------------------------------------------------------------------
	    // 24/04/2015 ECU check if there is a required attachment
	    // -------------------------------------------------------------------------
	    if (attachments != null)
	    {
	    	// ---------------------------------------------------------------------
	    	// 24/04/2015 ECU add the attachment as the next part of the multi part
	    	//                message
	    	// 25/04/2015 ECU changed to handle the array of attachments
	    	// ---------------------------------------------------------------------
	    	for (int theAttachment=0; theAttachment < attachments.length; theAttachment++)
	    	{
	    		// -----------------------------------------------------------------
	    		// 25/04/2015 ECU add each attachment
	    		// -----------------------------------------------------------------
	    		addAttachment (multiPart,attachments [theAttachment]);
	    		// -----------------------------------------------------------------
	    	}
	    	// ---------------------------------------------------------------------
	    }
	    // -------------------------------------------------------------------------
	    // 24/04/2015 ECU now set the message content to the multiple parts
	    // -------------------------------------------------------------------------
	    message.setContent (multiPart); 
	    // -------------------------------------------------------------------------
	    // 04/01/2014 ECU now send the message using the transport layer
	    // -------------------------------------------------------------------------
	    Transport.send(message);
	    // -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public synchronized void sendMail (String subject, 
									   String body, 
									   String sender, 
									   String recipients) throws Exception 
	{
		// -------------------------------------------------------------------------
		// 24/04/2015 ECU create to call the master sendMail method but indicating
		//                that there is no attachment
		// -------------------------------------------------------------------------
		sendMail (subject,body,sender,recipients,(String [])null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public synchronized void sendMail (String subject, 
			 						   String body, 
			 						   String sender, 
			 						   String recipients,
			 						   String attachment) throws Exception 
	{
		// -------------------------------------------------------------------------
		// 25/04/2015 ECU create to call the master sendMail method but indicating
		//                that there is a single attachment
		// -------------------------------------------------------------------------
		sendMail (subject,body,sender,recipients,new String [] {attachment});
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void addAttachment (Multipart theMultiPart,String theFileName) throws Exception 
	{ 
		// -------------------------------------------------------------------------
		// 24/04/2015 ECU created to enable a file to be attached to an email
		// -------------------------------------------------------------------------
		BodyPart messageBodyPart 	= new MimeBodyPart(); 
		DataSource source 			= new FileDataSource (theFileName); 
		messageBodyPart.setDataHandler (new DataHandler(source)); 
		messageBodyPart.setFileName (theFileName); 
		theMultiPart.addBodyPart (messageBodyPart); 
	} 
	/* ============================================================================= */
	public class ByteArrayDataSource implements DataSource 
	{
		// -------------------------------------------------------------------------
	    private byte[] data;
	    private String type;
	    // -------------------------------------------------------------------------
	    public ByteArrayDataSource(byte[] data, String type)
	    {
	        super();
	        this.data = data;
	        this.type = type;
	    }
	    // -------------------------------------------------------------------------
	    public ByteArrayDataSource(byte[] data)
	    {
	        super();
	        this.data = data;
	    }
	    // -------------------------------------------------------------------------
	    public void setType(String type) 
	    {
	        this.type = type;
	    }
	    // -------------------------------------------------------------------------
	    public String getContentType() 
	    {
	        if (type == null)
	            return "application/octet-stream";
	        else
	            return type;
	    }
	    // -------------------------------------------------------------------------
	    public InputStream getInputStream() throws IOException 
	    {
	        return new ByteArrayInputStream (data);
	    }
	    // -------------------------------------------------------------------------
	    public String getName() 
	    {
	        return "ByteArrayDataSource";
	    }
	    // -------------------------------------------------------------------------
	    public OutputStream getOutputStream() throws IOException 
	    {
	    	throw new IOException ("Not Supported");
	    }
	    // -------------------------------------------------------------------------
	}
	/* ============================================================================= */
}

package com.usher.diboson;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class EmailDetailsActivity extends DibosonActivity 
{
	// =============================================================================
	// 04/01/2014 ECU created
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 29/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	//==============================================================================
	/* ============================================================================= */
	Button		SMTPAcceptView;
	TextView	SMTPPasswordView;
	TextView	SMTPPortView;
	TextView	SMTPServerView;
	TextView	SMTPUserNameView;
	TextView	SMTPRecipientsView;
	Button		SMTPSetDefaultsView;
	/* ============================================================================= */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 16/02/2014 ECU call up routine to set common activity features
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this);
			setContentView(R.layout.activity_email_details);
			// ---------------------------------------------------------------------
			// 04/01/2014 ECU set up the various views
			// ---------------------------------------------------------------------
			SMTPPasswordView	=	(TextView) findViewById (R.id.SMTPPasswordInput);
			SMTPPortView		=	(TextView) findViewById (R.id.SMTPPortInput);
			SMTPRecipientsView	=	(TextView) findViewById (R.id.SMTPRecipientsInput);
			SMTPServerView		=	(TextView) findViewById (R.id.SMTPServerInput);
			SMTPUserNameView	=	(TextView) findViewById (R.id.SMTPUserNameInput);
			// ---------------------------------------------------------------------
			// 04/01/2014 ECU set up the method to handle the button
			// ---------------------------------------------------------------------
			((Button)findViewById(R.id.SMTPButton)).setOnClickListener(acceptSMTPDetails);
			((Button)findViewById(R.id.SMTPSetDefaultsButton)).setOnClickListener(setSMTPDefaults);
		}
		else
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been recreated after having been
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
		// -------------------------------------------------------------------------
		// Inflate the menu; this adds items to the action bar if it is present.
		// -------------------------------------------------------------------------
		return true;
	}
	/* ============================================================================= */
	private View.OnClickListener acceptSMTPDetails = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{
			// ---------------------------------------------------------------------
			// 04/01/2014 ECU set the values from the input data
			// ---------------------------------------------------------------------
			String	 SMTPPassword 	= SMTPPasswordView.getText ().toString();
			String	 SMTPPort 		= SMTPPortView.getText ().toString();
			String   SMTPRecipients = SMTPRecipientsView.getText ().toString();
			String	 SMTPServer 	= SMTPServerView.getText ().toString();
			String	 SMTPUserName 	= SMTPUserNameView.getText ().toString();
			
			
			PublicData.emailDetails = new EmailDetails (SMTPServer,SMTPPort,
					  						SMTPUserName,SMTPPassword,SMTPRecipients,false);
			// ---------------------------------------------------------------------
			// 04/01/2014 ECU write the object to disk
			// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
			// ---------------------------------------------------------------------
			AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + getBaseContext().getString (R.string.email_details_file),PublicData.emailDetails);
			// ---------------------------------------------------------------------	
			// 04/01/2014 ECU just confirm the input
			// ---------------------------------------------------------------------
			Utilities.popToast(PublicData.emailDetails.Print());
			
			finish ();
		}
	};
	/* ============================================================================= */
	private View.OnClickListener setSMTPDefaults = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{
			// ---------------------------------------------------------------------
			// 04/01/2014 ECU set the values to defaults
			// ---------------------------------------------------------------------
			SMTPPasswordView.setText (StaticData.BLANK_STRING);
			SMTPPortView.setText (R.string.smtp_port_number);
			SMTPRecipientsView.setText (R.string.smtp_email_receiver);
			SMTPServerView.setText (R.string.smtp_server);
			SMTPUserNameView.setText (R.string.smtp_user_name);
		}
	};
	/* ============================================================================= */
}

package com.usher.diboson;

import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DisplayAMessage extends DibosonActivity 
{
	// =============================================================================
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 29/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 27/03/2016 ECU added the handle of a specified LAYOUT and a SPOKEN_PHRASE
	// =============================================================================
	
	// =============================================================================
	Button		confirmButton;
	String		htmlMessageToDisplay	= null;
	int			layoutID				= R.layout.activity_display_a_message;
														// 27/03/2016 ECU added
	TextView	messageTextView;
	String 		messageToDisplay		= "";
	boolean		speakTheMessage			= false;
	int			speakTimer				= 1000 * 60;
	String		spokenPhrase			= null;				// 27/03/2016 ECU added
	String		time					= "";
	TextView	timeTextView;
	// =============================================================================	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been created anew
			// 01/11/2016 ECU added the 'true' to indicate full screen
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// ---------------------------------------------------------------------
			// 09/02/2015 ECU get the parameters that will need to be displayed
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
			if (extras != null)
			{
				// -----------------------------------------------------------------
				// 09/02/2015 ECU get the data from the intent
				// 27/03/2016 ECU added handling of LAYOUT and SPOKEN_PHRASE
				// 28/03/2016 ECU added the _HTML option
				// -----------------------------------------------------------------
				htmlMessageToDisplay 	= extras.getString 	(StaticData.PARAMETER_MESSAGE_HTML,null);
				layoutID				= extras.getInt		(StaticData.PARAMETER_LAYOUT,R.layout.activity_display_a_message);
				messageToDisplay 		= extras.getString 	(StaticData.PARAMETER_MESSAGE,null);
				speakTheMessage			= extras.getBoolean (StaticData.PARAMETER_SPEAK,false);
				speakTimer				= extras.getInt 	(StaticData.PARAMETER_TIMER,1000 * 60);
				spokenPhrase	 		= extras.getString 	(StaticData.PARAMETER_SPOKEN_PHRASE);
				time			 		= extras.getString 	(StaticData.PARAMETER_START_TIME);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 27/03/2016 ECU display the appropriate layout
			// ---------------------------------------------------------------------
			setContentView (layoutID);
			// ---------------------------------------------------------------------
			// 09/02/2015 ECU set up the necessary views
			// 27/03/2016 ECU added the Title...
			// ---------------------------------------------------------------------
			confirmButton 			= (Button) 	 findViewById (R.id.messageConfirmButton);
			messageTextView 		= (TextView) findViewById (R.id.messageTextView);
			timeTextView 			= (TextView) findViewById (R.id.messageTimeTextView);
			// ---------------------------------------------------------------------
			// 09/02/2015 ECU set up any listeners
			// ---------------------------------------------------------------------
			confirmButton.setOnClickListener(confirmMessage);
			// ---------------------------------------------------------------------
			// 09/02/2015 ECU set the message for display
			//			   ECU added the time
			// 28/03/2016 ECU decide whether to use html or normal
			// ---------------------------------------------------------------------
			if (htmlMessageToDisplay != null)
				messageTextView.setText (Html.fromHtml (htmlMessageToDisplay));
			else
				messageTextView.setText (messageToDisplay);
			timeTextView.setText 	(time);
			// ---------------------------------------------------------------------
			// 09/02/2015 ECU start speaking the message if requested
			// ---------------------------------------------------------------------
			if (speakTheMessage)
				speakTheMessageThread.start();
			// ---------------------------------------------------------------------
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
	// =============================================================================
	@Override
	public void onBackPressed () 
	{
	    // -------------------------------------------------------------------------
		// 09/02/2015 ECU try and prevent the use of the 'back' key
		// 10/04/2016 ECU call the method to tell the user what is happening
		// -------------------------------------------------------------------------
		Utilities.BackKeyNotAllowed (this);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public boolean onCreateOptionsMenu (Menu menu) 
	{
		// -------------------------------------------------------------------------
		// 09/02/2015 ECU no message to display
		// -------------------------------------------------------------------------
		return true;
	}
	// =============================================================================
	private View.OnClickListener confirmMessage = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{
			// ---------------------------------------------------------------------
			// 09/02/2015 ECU message has been confirmed so just exit
			// ---------------------------------------------------------------------
			finish ();
		}
	};
	// =============================================================================
	@Override
	public void onDestroy() 
	{
		// -------------------------------------------------------------------------
		// 09/02/2015 ECU indicate that the thread is to stop
		// -------------------------------------------------------------------------
		if (speakTheMessage)
			speakTheMessageThread.interrupt();
		// -------------------------------------------------------------------------	
		super.onDestroy();
	}
	// =============================================================================
	Thread speakTheMessageThread = new Thread()
	{	
		@Override
		public void run()
		{
			try 
			{          	
				while (!this.isInterrupted())
		        {
					// -------------------------------------------------------------
					// 09/02/2015 ECU speak the message
					// 27/03/2016 ECU handle the spoken phrase option
					// -------------------------------------------------------------
					if (spokenPhrase == null)
						Utilities.SpeakAPhrase (getBaseContext(),messageToDisplay);
					else
						Utilities.SpeakAPhrase (getBaseContext(),spokenPhrase);
					// -------------------------------------------------------------
					// 09/02/2015 ECU wait the specified time
					// -------------------------------------------------------------
					sleep (speakTimer);     
				}
			}
			catch(InterruptedException theException)
			{    
				// -----------------------------------------------------------------
				// 04/04/2014 ECU Restore interrupt flag after catching 
				//				  InterruptedException to make loop condition false
				// -----------------------------------------------------------------
				Thread.currentThread().interrupt();
				// -----------------------------------------------------------------
			}       
		}
	};
	// =============================================================================
}

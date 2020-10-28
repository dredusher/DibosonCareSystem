package com.usher.diboson;

import android.os.Bundle;
import android.view.Menu;

public class ServerCommands extends DibosonActivity 
{
	/* ================================================================================= */
	//            ECU handles commands received on the socket connection
	// 18/09/2013 ECU just tidied up
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 29/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	/* ================================================================================== */
	final static String TAG = "ServerCommands";			// 18/09/2013 ECU added
	/* ================================================================================== */
	String	commandString;
	/* ================================================================================== */
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
			// 24/07/2013 ECU check for the command string in the intent
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras ();
			// ---------------------------------------------------------------------
			if (extras !=null) 
			{
				// -----------------------------------------------------------------
				// 19/10/2019 ECU changed to use Static....
				// -----------------------------------------------------------------
				commandString = extras.getString (StaticData.PARAMETER_COMMAND_STRING);
				// -----------------------------------------------------------------
				if (commandString != null)
				{
					// -------------------------------------------------------------
					// 18/09/2013 ECU log a useful message in debug mode
					// -------------------------------------------------------------
					Utilities.debugMessage (TAG,commandString);
					// -------------------------------------------------------------
					// 24/07/2013 ECU parse the incoming command string
					//                word 0 must be "command"
					//                word 1 can be  "phone" in which case word 2 is the number to call
					//                               "message" in which case the rest of the string is the message
					// 18/03/2015 ECU				 "WeMo" in which case the rest is an action to process
					// 19/10/2019 ECU changed to use Sta...STRING
					// -------------------------------------------------------------
					String [] theWords = commandString.split (StaticData.SPACE_STRING);
			
					if (theWords[0].equalsIgnoreCase (StaticData.SERVER_COMMAND))
					{
						// ---------------------------------------------------------
						// 03/07/2013 ECU check for a command
						// ---------------------------------------------------------
						if (theWords[1].equalsIgnoreCase(StaticData.SERVER_COMMAND_PHONE))
						{
							Utilities.makePhoneCall (this,theWords [2]);
						}
						// ---------------------------------------------------------
						// 29/11/2015 ECU check for cancel phone call
						// ---------------------------------------------------------
						if (theWords[1].equalsIgnoreCase(StaticData.SERVER_COMMAND_CANCEL_CALL))
						{
							Utilities.cancelPhoneCall (this);
						}
						// ---------------------------------------------------------
						if (theWords [1].equalsIgnoreCase (StaticData.SERVER_COMMAND_MESSAGE))
						{
							// -----------------------------------------------------
							// 06/01/2016 ECU tidy up the actual message
							//            ECU replace the substitute string back into spaces
							//            ECU 'theWords[2]' has the format
							//                <phone number><delimiter><message>
							//                where any spaces in the message have been
							//                been substituted
							// 16/11/2019 ECU changed from 'replaceAll' to 'replace' because
							//                the former requires a REGEX so not sure why it ever
							//				  worked
							// 22/01/2020 ECU added the replacement of the newline
							// -----------------------------------------------------
							String [] localWords = theWords [2].split(StaticData.ACTION_DELIMITER);
							Utilities.sendSMSMessage (this,localWords [0],
									localWords[1].replace (StaticData.SPACE_REPLACEMENT,StaticData.SPACE_STRING).replace (StaticData.NEWLINE_REPLACEMENT,StaticData.NEWLINE));
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
						if (theWords[1].equalsIgnoreCase (StaticData.ACTION_DESTINATION_WEMO))
						{
							// -----------------------------------------------------
							// 18/03/2015 ECU pass through the command to be processed
							//                  theWords [2] = device friendly name
							//                  theWords [3] = action to take (on/off)
							// -----------------------------------------------------
							WeMoActivity.voiceCommands (theWords[2] + " " + theWords [3]);
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
					}	
				}
			}
			// ---------------------------------------------------------------------
			// exit from this activity
			// ---------------------------------------------------------------------
			finish ();
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
	/* ============================================================================================= */
	@Override
	public boolean onCreateOptionsMenu (Menu menu) 
	{
		return true;
	}
	/* ============================================================================================= */
}

package com.usher.diboson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

/* ================================================================================= */
public class SMSHandler extends BroadcastReceiver
{
	// -----------------------------------------------------------------------------
	// 29/10/2014 ECU declare any local variables
	// -----------------------------------------------------------------------------
	String incomingPhoneNumber;
	// -----------------------------------------------------------------------------
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// -------------------------------------------------------------------------
		// 30/05/2013 ECU created
		// -------------------------------------------------------------------------
		Bundle bundle = intent.getExtras();
		// -------------------------------------------------------------------------
		// 30/05/2013 ECU get the package name
		// -------------------------------------------------------------------------
		String thePackageName = context.getPackageName();
			
		SmsMessage [] SMSmessages = null;
		// -------------------------------------------------------------------------
		// 30/05/2013 ECU check if anything sent across with the intent
		// -------------------------------------------------------------------------
		if (bundle != null)
		{
			// ---------------------------------------------------------------------
			// 30/05/2013 ECU check for SMS (protocol description unit)
			// ---------------------------------------------------------------------
			Object[] pdus = (Object[]) bundle.get ("pdus");
			
			// 30/05/2013 ECU get the number of messages to handle
			
			SMSmessages = new SmsMessage [pdus.length];
				
			// 30/05/2013 ECU process each incoming SMS message
				
			for (int index=0; index < SMSmessages.length; index++)
			{
				// -----------------------------------------------------------------
				// 30/05/2013 ECU create the message from the incoming data
				// -----------------------------------------------------------------
				SMSmessages [index] = SmsMessage.createFromPdu ((byte[])pdus[index]);
				// -----------------------------------------------------------------
				// 29/10/2014 ECU get phone number from the message
				// -----------------------------------------------------------------
				incomingPhoneNumber = SMSmessages [index].getOriginatingAddress();
				// -----------------------------------------------------------------
				// 30/05/2013 ECU process the message in some way
				// 29/10/2014 ECU use the variable 'incomingPhoneNumber'
				// -----------------------------------------------------------------
				String theMessage = "SMS from " + incomingPhoneNumber + " \n";
				String theBody = SMSmessages [index].getMessageBody().toString();
				theMessage += theBody + "\n";;
				// -----------------------------------------------------------------
				// 30/05/2013 ECU display the contents of the message
				// 08/11/2013 ECU use the custom toast
				// 26/10/2014 ECU add the option to log the incoming message
				// -----------------------------------------------------------------
				Utilities.popToastAndLog(theMessage);
				// -----------------------------------------------------------------
				// 30/05/2013 ECU decide if any action is to be taken
				//
				// 					Check for 'Command<space><activity to run>
				// 29/10/2014 ECU change to ignore case on 'Command' check
				//            ECU include the 'Invade' bit
				// -----------------------------------------------------------------
				String [] theWords = theBody.split(" ");
				// -----------------------------------------------------------------
				if (theWords[0].equalsIgnoreCase("Invade"))
				{
					// -------------------------------------------------------------
					// 04/11/2014 ECU indicate to the 'invade' program which phone
					//                number sent in the command
					// -------------------------------------------------------------
	      			Intent localIntent = new Intent (context,InvadeActivity.class);
	      			localIntent.putExtra (StaticData.PARAMETER_PHONE_NUMBER,incomingPhoneNumber);
	      			localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	  				context.startActivity (localIntent);
	  				// -------------------------------------------------------------
				}
				else
				// -----------------------------------------------------------------
				if (theWords[0].equalsIgnoreCase("Command"))
				{
					// -------------------------------------------------------------
					// 30/05/2013 ECU this is only a test and needs more testing
					//                of incoming parameters
					// -------------------------------------------------------------
					// 03/07/2013 ECU the class will be started using the name that is
					//                supplied in theWords[1], i.e. what comes in is
					//
					//                command <name of class> <name of optional parameter> <data associated with parameter>
					// -------------------------------------------------------------
					Intent localIntent = new Intent();
					// -------------------------------------------------------------
					// 29/10/2014 ECU check that parameters exist 
					// -------------------------------------------------------------
					if (theWords.length >= 2)
						localIntent.setClassName(thePackageName, thePackageName + "." + theWords[1]);
					else
					{
						// ---------------------------------------------------------
						// 29/10 2014 ECU no class was supplied so set the intent
						//                to the default activity
						// 01/09/2015 ECU changed to use StaticData
						// ---------------------------------------------------------
						localIntent.setClassName (thePackageName, thePackageName + "." + StaticData.COMMAND_DEFAULT_CLASS);
						// ---------------------------------------------------------
						// 29/10/2014 ECU feed through the phone number
						// ---------------------------------------------------------
						localIntent.putExtra (StaticData.PARAMETER_PHONE_NUMBER,incomingPhoneNumber);
						// ---------------------------------------------------------
					}
					localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					// -------------------------------------------------------------
					// 30/05/2013 ECU if length is >2 then pass the third word across as
					//                an extra
					// 26/10/2014 ECU theWords [2] .............. the name of the parameter
					//                theWords [3] .............. the value of that
					//                                            parameter
					// -------------------------------------------------------------
					if (theWords.length == 4)
					{
						localIntent.putExtra (theWords[2],theWords[3]);
					}
					// -------------------------------------------------------------
					// 26/10/2014 ECU now start the activity
					// -------------------------------------------------------------
					context.startActivity(localIntent);
					// -------------------------------------------------------------
				}
			}
		}
	}
}
/* ================================================================================= */


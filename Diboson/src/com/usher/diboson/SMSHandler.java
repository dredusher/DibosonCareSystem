package com.usher.diboson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

// =================================================================================
// 18/10/2019 ECU Up until this point this jandler was just 'for fun' and waa
//                poorly written. It is not likely to be of any real use but today
//                have just put it into a better shape.
//
//                Messages of interest are introduced by a command :-
//
//			      StaticData.SMS_COMMAND
//                ======================
//                  The receiving phone will process the associated string as actions.
//					For example :-
//                    "Command Speak:hello there;Notification:new notification"
//				  StaticData.SMS_INVADE
// 				  =====================
//                  The receiving phone will be put into a 'pseudo invaded' mode by
//                  activating InvadeActivity. This is just to demonstrate how easy
//                  it is to take over a device
//				  StaticData.SMS_BROADCAST
// 				  ========================
//					The receiving phone will broadcast the associated string to all
//                  of the devices that it finds on its wireless network, including
//                  itself. On receipt each device will process the received string
//                  as actions.
//					For example :-
//					  "Broadcast Speak:hello there;Notification:new notification"
// =================================================================================

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
		SmsMessage [] SMSmessages = null;
		// -------------------------------------------------------------------------
		// 30/05/2013 ECU check if anything sent across with the intent
		// -------------------------------------------------------------------------
		if (bundle != null)
		{
			// ---------------------------------------------------------------------
			// 30/05/2013 ECU check for SMS (protocol description unit)
			// ---------------------------------------------------------------------
			Object[] pdus = (Object []) bundle.get ("pdus");
			// ---------------------------------------------------------------------
			// 30/05/2013 ECU get the number of messages to handle
			// ---------------------------------------------------------------------
			SMSmessages = new SmsMessage [pdus.length];
			// ---------------------------------------------------------------------	
			// 30/05/2013 ECU process each incoming SMS message
			// ----------------------------------------------------------------------	
			for (int index=0; index < SMSmessages.length; index++)
			{
				// -----------------------------------------------------------------
				// 30/05/2013 ECU create the message from the incoming data
				// -----------------------------------------------------------------
				SMSmessages [index] = SmsMessage.createFromPdu ((byte[])pdus [index]);
				// -----------------------------------------------------------------
				// 29/10/2014 ECU get phone number from the message
				// -----------------------------------------------------------------
				incomingPhoneNumber = SMSmessages [index].getOriginatingAddress ();
				// -----------------------------------------------------------------
				// 30/05/2013 ECU process the message in some way
				// 29/10/2014 ECU use the variable 'incomingPhoneNumber'
				// -----------------------------------------------------------------
				String theMessage = "SMS from " + incomingPhoneNumber + " \n";
				String theBody = SMSmessages [index].getMessageBody ().toString ();
				theMessage += theBody + StaticData.NEWLINE;
				// -----------------------------------------------------------------
				// 30/05/2013 ECU display the contents of the message
				// 08/11/2013 ECU use the custom toast
				// 26/10/2014 ECU add the option to log the incoming message
				// -----------------------------------------------------------------
				Utilities.popToastAndLog (theMessage);
				// -----------------------------------------------------------------
				// 18/10/2019 ECU check to see if phone is being put into 'invade'
				//                mode - just a 'jokey' thing
				// -----------------------------------------------------------------
				if (theBody.startsWith (StaticData.SMS_INVADE))
				{
					// -------------------------------------------------------------
					// 04/11/2014 ECU indicate to the 'invade' program which phone
					//                number sent in the command
					// -------------------------------------------------------------
	      			Intent localIntent = new Intent (context,InvadeActivity.class);
	      			localIntent.putExtra (StaticData.PARAMETER_PHONE_NUMBER,incomingPhoneNumber);
	      			localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
	  				context.startActivity (localIntent);
	  				// -------------------------------------------------------------
				}
				else
				// -----------------------------------------------------------------
				// 18/10/2019 ECU check if the SMS message contains a string of
				//                'actions' to be processed
				//
				//                 <SMS_COMMAND><the string containing the actions>
				// -----------------------------------------------------------------
				if (theBody.startsWith (StaticData.SMS_COMMAND))
				{
					// -------------------------------------------------------------
					// 18/10/2019 ECU remove 'SMS_COMMAND' before doing the actual
					//                processing
					// -------------------------------------------------------------
					Utilities.actionHandler (context,theBody.replaceFirst (StaticData.SMS_COMMAND,StaticData.BLANK_STRING));
					// -------------------------------------------------------------
				}
				else
				// -----------------------------------------------------------------
				// 18/10/2019 ECU check if the SMA message contains a string of
				//                'actions' to be broadcast
				//
				//                 <SMS_BROADCAST><the string containing the actions>
				// -----------------------------------------------------------------
				if (theBody.startsWith (StaticData.SMS_BROADCAST))
				{
					// -------------------------------------------------------------
					// 18/10/2019 ECU remove 'SMS_BROADCAST' before doing the actual
					//                processing
					// -------------------------------------------------------------
					Utilities.sendSocketMessageSendObjectToAllDevices
									(context,
									 PublicData.deviceDetails,
									 PublicData.socketNumberForData,
									 StaticData.SOCKET_MESSAGE_ACTIONS,
									 theBody.replaceFirst (StaticData.SMS_BROADCAST,StaticData.BLANK_STRING),
									 true);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
		}
	}
}
/* ================================================================================= */


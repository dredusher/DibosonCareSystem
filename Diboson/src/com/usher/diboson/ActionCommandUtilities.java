package com.usher.diboson;

import java.lang.reflect.Method;

import android.content.Context;
import android.os.Vibrator;
import android.text.InputType;

public class ActionCommandUtilities 
{
	// =============================================================================
	// 23/01/2016 ECU change literal strings to resource strings
	// =============================================================================
	
	// =============================================================================
	static String	actionString;
	static Context	context;
	static Method	returnMethod;
	// =============================================================================
	
	// =============================================================================
	public static void ActionCommandComplete ()
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU created to be called when an action command is complete
		// 24/01/2016 ECU an individual action command has been completed - ask
		//                if another one is to be defined
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (context,  
								 context.getString (R.string.action_command_complete),
								 context.getString (R.string.another_action_command),
								 null,
								 true,"Yes",Utilities.createAMethod (ActionCommandUtilities.class,"ActionCommandMethod",(Object) null),
								 true,"No",Utilities.createAMethod  (ActionCommandUtilities.class,"ActionCommandsCompleteMethod",(Object) null)); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void ActionCommandMethod (Object theDummyArgument)
	{
		// -------------------------------------------------------------------------
		// 24/01/2016 ECU called to start adding a new action command
		// -------------------------------------------------------------------------
		DialogueUtilities.listChoice (context, 
									  context.getString (R.string.select_action_command),
									  ActionCommand.ReturnCommands(),
									  Utilities.createAMethod (ActionCommandUtilities.class,"SelectedParameter",0),
									  context.getString (R.string.cancel),
									  null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void ActionCommandsCompleteMethod (Object theDummyArgument)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU created to be called when an action command is complete
		// -------------------------------------------------------------------------
		try 
		{ 
			// ---------------------------------------------------------------------
			// 10/04/2015 ECU call the method that will handle the cancellation
			// ---------------------------------------------------------------------
			returnMethod.invoke(null,new Object [] {actionString});
			// ---------------------------------------------------------------------
		} 
		catch (Exception theException) 
		{	
		} 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SelectCommand (Context theContext,Method theReturnMethod)
	{
		// -------------------------------------------------------------------------
		// 21/01/2016 ECU created to handle the dialogue to acquire the action
		//                commands
		// 24/01/2016 ECU initialise the action string
		// -------------------------------------------------------------------------
		actionString		= null;
		context 			= theContext;
		returnMethod		= theReturnMethod;
		// -------------------------------------------------------------------------
		// 24/01/2016 ECU called to define an action command
		// -------------------------------------------------------------------------
		ActionCommandMethod (null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public static void EmailMessageMethod (String theEmailMessage)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU created to input the email subject
		// -------------------------------------------------------------------------
		actionString += theEmailMessage;
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU the command is complete
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void EmailAddressMethod (String theEmailAddress)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU created to input the email address(es)
		// -------------------------------------------------------------------------
		actionString += theEmailAddress + StaticData.ACTION_DELIMITER;
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU request the email subject
		// -------------------------------------------------------------------------
		DialogueUtilities.textInput (context,
				   					 context.getString (R.string.email_subject),
				   					 context.getString (R.string.enter_email_subject),
				   					 StaticData.HINT + context.getString (R.string.type_in_email_subject),
				   					 Utilities.createAMethod (ActionCommandUtilities.class,"EmailSubjectMethod",""),
				   					 null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void EmailSubjectMethod (String theEmailSubject)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU created to input the email subject
		// -------------------------------------------------------------------------
		actionString += theEmailSubject + StaticData.ACTION_DELIMITER;
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU request the email message
		// 23/01/2016 ECU changed to multiline input
		// -------------------------------------------------------------------------
		DialogueUtilities.multilineTextInput (context,
											  context.getString (R.string.email_message),
											  context.getString (R.string.enter_email_message),
											  3,
											  StaticData.HINT + context.getString (R.string.type_in_email_message),
											  Utilities.createAMethod (ActionCommandUtilities.class,"EmailMessageMethod",""),
											  null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void NamedActionsNameMethod (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 04/08/2016 ECU created to handle the selection of the name of stored
		//                actions
		// -------------------------------------------------------------------------
		actionString += PublicData.namedActions.get(theIndex).name;
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU the command is complete
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void NotificationMethod (String theNotificationMessage)
	{
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU add the phone number to the action string
		// -------------------------------------------------------------------------
		actionString += theNotificationMessage;
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU the command is complete
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void OffMethod (Object theSelection)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU add the option
		// -------------------------------------------------------------------------
		actionString += "off";
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU the command is complete
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void OnMethod (Object theSelection)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU add the option
		// -------------------------------------------------------------------------
		actionString += "on";
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU the command is complete
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void PhoneNumberMethod (String thePhoneNumber)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU add the phone number to the action string
		// -------------------------------------------------------------------------
		actionString += thePhoneNumber;
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU the command is complete
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void PlayFile (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU add the phone number to the action string
		// 16/11/2016 ECU changed to use the new method
		// -------------------------------------------------------------------------
		actionString += Utilities.getRelativeFileName (theFileName);
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SelectedParameter (int theCommandIndex)
	{
		// -------------------------------------------------------------------------
		// 21/01/2016 ECU process depending on the command that was entered - check
		//                the 'name' of the command rather than the index, just in
		//                case the array in StaticData is edited and the order is
		//                changed
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU reset the action string to the selected value
		// 24/01/2016 ECU decide whether this is the first command or not
		// -------------------------------------------------------------------------
		if (actionString == null)
		{
			// ---------------------------------------------------------------------
			// 24/01/2016 ECU this is the first command 
			// ---------------------------------------------------------------------
			actionString = StaticData.ACTION_COMMANDS[theCommandIndex].command;
		}
		else
		{
			// ---------------------------------------------------------------------
			// 24/01/2016 ECU this is not the first command
			// ---------------------------------------------------------------------
			actionString += StaticData.ACTION_SEPARATOR + 
					        StaticData.ACTION_COMMANDS[theCommandIndex].command;
		}
		// -------------------------------------------------------------------------
		// 02/05/2016 ECU decide if a delimiter is to be added - check if there are
		//                any parameters `d
		// -------------------------------------------------------------------------
		if (StaticData.ACTION_COMMANDS[theCommandIndex].parameters != null ) 
		{
			actionString += StaticData.ACTION_DELIMITER;
		}
		// -------------------------------------------------------------------------
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_EMAIL))
		{
			// ---------------------------------------------------------------------
			// 23/01/2016 ECU ORed the EMAIL_ADDRESS with TYPE_CLASS_TEXT to get correct
			//                keyboard layout
			// ---------------------------------------------------------------------
			DialogueUtilities.textInput (context,
										 context.getString (R.string.email_addresses),
										 context.getString (R.string.enter_email_addresses),
										 StaticData.HINT + context.getString (R.string.type_in_email_addresses),
										 Utilities.createAMethod (ActionCommandUtilities.class,"EmailAddressMethod",""),
										 null,
										 InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_NAMED_ACTION))
		{
			// ---------------------------------------------------------------------
			// 04/08/2016/2016 ECU added to handle named actions
			// ---------------------------------------------------------------------
			String [] localNames = NamedAction.getNames ();
			// ---------------------------------------------------------------------
			// 04/08/2016 ECU check if any names exist
			// ---------------------------------------------------------------------
			if (localNames != null)
			{
				DialogueUtilities.singleChoice (context,
											    context.getString (R.string.named_actions_name_title),
											    localNames,
											    0, 
											    Utilities.createAMethod (ActionCommandUtilities.class,"NamedActionsNameMethod",0),
											    null);
			}
			else
			{
				// ------------------------------------------------------------------
				// 22/10/2016 ECU there are no named actions defined
				// ------------------------------------------------------------------
				Utilities.popToastAndSpeak (context.getString (R.string.named_action_none_defined),true);
				// ------------------------------------------------------------------
			}
			// ----------------------------------------------------------------------
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_NOTIFICATION))
		{
			// ---------------------------------------------------------------------
			// 13/07/2016 ECU added to handle notifications
			// ---------------------------------------------------------------------
			DialogueUtilities.multilineTextInput (context,
					                     	      context.getString (R.string.notification_message),
					                     	      context.getString (R.string.enter_notification_message),
					                     	      2,
					                     	      StaticData.HINT + context.getString (R.string.type_in_notification_message),
					                     	      Utilities.createAMethod (ActionCommandUtilities.class,"NotificationMethod",""),
					                     	      null);
			// ----------------------------------------------------------------------
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_PHONE))
		{
			// ---------------------------------------------------------------------
			// 22/01/2016 ECU the phone number just has the number as the argument
			// ---------------------------------------------------------------------
	 		DialogueUtilities.textInput (context,
	 									 context.getString (R.string.phone_number),
	 									 context.getString (R.string.enter_phone_number),
					   				 	 StaticData.HINT + context.getString (R.string.type_in_phone_number),
					   				 	 Utilities.createAMethod (ActionCommandUtilities.class,"PhoneNumberMethod",""),
					   				 	 null,
					   				 	 InputType.TYPE_CLASS_PHONE);
	 		// ---------------------------------------------------------------------
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_PLAY))
		{
			// ---------------------------------------------------------------------
			// 22/01/2016 ECU the play command only has a file as an argument
			// ---------------------------------------------------------------------
			Utilities.selectAFile (context,StaticData.EXTENSION_MUSIC,
					new MethodDefinition <ActionCommandUtilities> (ActionCommandUtilities.class,"PlayFile"));
			// ----------------------------------------------------------------------
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_SCREEN))
		{
			// ---------------------------------------------------------------------
			// 22/01/2016 ECU the command only accepts 'on' and 'off' as arguments
			// ---------------------------------------------------------------------
			DialogueUtilities.yesNo (context,  
					 				 context.getString (R.string.screen_command),
					   				 context.getString (R.string.on_off_arguments),
					   				 null,
					   				 true,"On",Utilities.createAMethod (ActionCommandUtilities.class,"OnMethod",(Object) null),
					   				 true,"Off",Utilities.createAMethod (ActionCommandUtilities.class,"OffMethod",(Object) null)); 
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_SMS))
		{
			// ---------------------------------------------------------------------
			// 22/01/2016 ECU created to set up the SMS command
			// ---------------------------------------------------------------------
			DialogueUtilities.textInput (context,
					 					 context.getString (R.string.sms_phone_number),
					   					 context.getString (R.string.enter_phone_number),
					   					 StaticData.HINT +  context.getString (R.string.type_in_phone_number),
					   					 Utilities.createAMethod (ActionCommandUtilities.class,"SMSPhoneNumberMethod",""),
					   					 null,
					   					 InputType.TYPE_CLASS_PHONE);
			// ----------------------------------------------------------------------
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_SPEAK))
		{
			// ---------------------------------------------------------------------
			DialogueUtilities.listChoice (context, 
										  context.getString (R.string.select_speak_option),
										  StaticData.ACTION_COMMANDS [theCommandIndex].ReturnParameters(),
										  Utilities.createAMethod (ActionCommandUtilities.class,"SpeakMethod",0),
										  context.getString (R.string.cancel),
										  null);
			// ----------------------------------------------------------------------

		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_TIME))
		{
			// ---------------------------------------------------------------------
			// 02/05/2016 ECU added to handle the speaking clock - there are no
			//                associated parameters 
			// ---------------------------------------------------------------------
			ActionCommandComplete ();
			// ---------------------------------------------------------------------
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_TRACK))
		{
			// ---------------------------------------------------------------------
			// 22/01/2016 ECU the command only accepts 'on' and 'off' as arguments
			// 23/01/2016 ECU change because if 'on' is selected then an email
			//                address will be requested
			// ---------------------------------------------------------------------
			DialogueUtilities.yesNo (context,  
									 context.getString (R.string.tracking_command),
									 context.getString (R.string.on_off_arguments),
					    		   	 null,
					    		     true,"On",Utilities.createAMethod (ActionCommandUtilities.class,"TrackOnMethod",(Object) null),
					    		     true,"Off",Utilities.createAMethod (ActionCommandUtilities.class,"OffMethod",(Object) null)); 
			// ----------------------------------------------------------------------
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_VIBRATE))
		{
			// ---------------------------------------------------------------------
			// 11/03/2016 ECU create to get the duration of the vibrate
			// 16/03/2016 ECU check if this device supports a vibrator
			// ---------------------------------------------------------------------
			if (((Vibrator) context.getSystemService (Context.VIBRATOR_SERVICE)).hasVibrator ())
			{
				DialogueUtilities.sliderChoice (context,
												context.getString (R.string.vibrate_duration),
												context.getString (R.string.vibrate_duration_summary),
												R.drawable.timer,
												null,
												1,
												1,
												30,
												context.getString (R.string.vibrate_duration_click),
												Utilities.createAMethod (ActionCommandUtilities.class,"VibrateDurationMethod",0),
												context.getString (R.string.cancel_operation));
			}
			else
			{
				// ------------------------------------------------------------------
				// 16/03/2016 ECU the device does not have a vibrator
				// ------------------------------------------------------------------
				Utilities.popToastAndSpeak (context.getString (R.string.no_vibrator),true);
				// ------------------------------------------------------------------
			}
			// ----------------------------------------------------------------------
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_VIDEO))
		{
			// ---------------------------------------------------------------------
			// 21/05/2016 ECU the video command only has a file as an argument
			// ---------------------------------------------------------------------
			Utilities.selectAFile (context,StaticData.EXTENSION_VIDEO,
					new MethodDefinition <ActionCommandUtilities> (ActionCommandUtilities.class,"PlayFile"));
			// ----------------------------------------------------------------------
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_WEMO))
		{
			DialogueUtilities.textInput (context,
					 					 context.getString (R.string.wemo_device_to_control),
					 					 context.getString (R.string.wemo_friendly_name),
										 StaticData.HINT + context.getString (R.string.type_in_wemo_friendly_name),
										 Utilities.createAMethod (ActionCommandUtilities.class,"WeMoMethod",""),
										 null);
		}
	}
	// =============================================================================
	public static void SMSMessageMethod (String theMessage)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU created to received the SMS message
		// -------------------------------------------------------------------------
		actionString += theMessage;
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU indicate that the command is complete
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SMSPhoneNumberMethod (String thePhoneNumber)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU add the phone number to the action string
		// -------------------------------------------------------------------------
		actionString += thePhoneNumber + StaticData.ACTION_DELIMITER;
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU the command is complete
		// 23/01/2016 ECU changed to multiline input
		// -------------------------------------------------------------------------
		DialogueUtilities.multilineTextInput (context,
											  context.getString (R.string.sms_message),
											  context.getString (R.string.enter_message_to_send),
											  2,
											  StaticData.HINT + context.getString (R.string.type_in_sms_message),
											  Utilities.createAMethod (ActionCommandUtilities.class,"SMSMessageMethod",""),
											  null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SpeakDelayMethod (int theDelay)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU created to set the speak delay and then request the phrase
		// -------------------------------------------------------------------------
		actionString += "" + (theDelay * 1000) + StaticData.ACTION_DELIMITER;
		// -------------------------------------------------------------------------
		// 07/03/2016 ECU changed to use multiline text input
		// -------------------------------------------------------------------------
		DialogueUtilities.multilineTextInput (context,
									 		  context.getString (R.string.phrase_to_be_spoken),
									 		  context.getString (R.string.enter_phrase_to_be_spoken),
									 		  2,
									 		  StaticData.HINT + context.getString (R.string.type_in_phrase_to_be_spoken),
									 		  Utilities.createAMethod (ActionCommandUtilities.class,"SpeakPhraseMethod",""),
									 		  null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SpeakMethod (int theSpeakOption)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU handle the options 
		// -------------------------------------------------------------------------
		switch (theSpeakOption)
		{
			// ---------------------------------------------------------------------
			case 0:
				// -----------------------------------------------------------------
				// 22/01/2016 ECU enter phrase to be spoken
				// 07/03/2016 ECU changed to use multiline input
				// -----------------------------------------------------------------
				DialogueUtilities.multilineTextInput (context,
											 		  context.getString (R.string.phrase_to_be_spoken),
											 		  context.getString (R.string.enter_phrase_to_be_spoken),
											 		  2,
											 		  StaticData.HINT + context.getString (R.string.type_in_phrase_to_be_spoken),
											 		  Utilities.createAMethod (ActionCommandUtilities.class,"SpeakPhraseMethod",""),
											 		  null);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case 1:
				// -----------------------------------------------------------------
				// 06/03/2016 ECU add the '1' as a minimum value
				// -----------------------------------------------------------------
				DialogueUtilities.sliderChoice (context,
    										    context.getString (R.string.delay_before_speaking),
    										    context.getString (R.string.initial_interval),
    										    R.drawable.timer,
    										    null,
    										    5,
    										    1,
    										    60,
    										    context.getString (R.string.click_to_set_delay),
    										    Utilities.createAMethod (ActionCommandUtilities.class,"SpeakDelayMethod",0),
    										    context.getString (R.string.cancel_operation));
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
				
			
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SpeakPhraseMethod (String thePhrase)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU add the phrase to be spoken
		// -------------------------------------------------------------------------
		actionString += thePhrase;
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU indicate that the command is complete
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void TrackEmailAddressMethod (String theEmailAddress)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU created to input the email address(es)
		// -------------------------------------------------------------------------
		actionString += theEmailAddress;
		// -------------------------------------------------------------------------
		// 23/01/2016 ECU indicate that the command is complete
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void TrackOnMethod (Object theSelection)
	{
		// -------------------------------------------------------------------------
		// 23/01/2016 ECU add the option
		// -------------------------------------------------------------------------
		actionString += "on" + StaticData.ACTION_DELIMITER;
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU the command is complete
		// 23/01/2016 ECU ORed the EMAIL_ADDRESS with TYPE_CLASS_TEXT to get correct
		//                keyboard layout
		// -------------------------------------------------------------------------
		DialogueUtilities.textInput (context,
				 					 context.getString (R.string.email_addresses),
				 					 context.getString (R.string.enter_email_addresses),
				 					 StaticData.HINT + context.getString (R.string.type_in_email_addresses),
				 					 Utilities.createAMethod (ActionCommandUtilities.class,"TrackEmailAddressMethod",""),
				 					 null,
				 					 InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void VibrateDurationMethod (int theDuration)
	{
		// -------------------------------------------------------------------------
		// 11/03/2016 ECU created to set the speak delay and then request the phrase
		// -------------------------------------------------------------------------
		actionString += "" + (theDuration * 1000);
		// -------------------------------------------------------------------------
		// 11/03/2016 ECU indicate that the command is complete
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void WeMoMethod (String theDeviceName)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU handle the WeMo device
		// -------------------------------------------------------------------------
		actionString += theDeviceName + " ";
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (context,  
								 context.getString(R.string.wemo_device_command),
								 context.getString(R.string.on_off_arguments),
								 null,
								 true,"On",Utilities.createAMethod (ActionCommandUtilities.class,"OnMethod",(Object) null),
								 true,"Off",Utilities.createAMethod (ActionCommandUtilities.class,"OffMethod",(Object) null)); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

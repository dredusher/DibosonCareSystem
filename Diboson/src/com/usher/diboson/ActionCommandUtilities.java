package com.usher.diboson;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.text.InputType;

public class ActionCommandUtilities 
{
	// =============================================================================
	// 23/01/2016 ECU change literal strings to resource strings
	// 21/04/2018 ECU added the 'activity' because want the option of invoking
	//                either a static or non-static method
	// =============================================================================
	
	// =============================================================================
	static String		actionString;
	static String  [] 	activities;
	static Context		context;
	static Method		returnMethod;
	static Object		underlyingObject;
	// =============================================================================
	
	// =============================================================================
	public static void ActionCommandComplete ()
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU created to be called when an action command is complete
		// 24/01/2016 ECU an individual action command has been completed - ask
		//                if another one is to be defined
		// 08/06/2019 ECU changed to use resources rather than "Yes" and "No"
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (context,  
								 context.getString (R.string.action_command_complete),
								 context.getString (R.string.another_action_command),
								 null,
								 true,context.getString (R.string.yes),
								 	Utilities.createAMethod (ActionCommandUtilities.class,"ActionCommandMethod",(Object) null),
								 true,context.getString (R.string.no),
								 	Utilities.createAMethod  (ActionCommandUtilities.class,"ActionCommandsCompleteMethod",(Object) null)); 
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
									  ActionCommand.ReturnCommands (),
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
			// 23/03/2018 ECU changed to use the new invoke method
			// 21/04/2018 ECU pass through the underlying method
			// ---------------------------------------------------------------------
			Utilities.invokeMethod ((Activity) underlyingObject,returnMethod,new Object [] {actionString});
			// ---------------------------------------------------------------------
		} 
		catch (Exception theException) 
		{	
		} 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void ActivitySelectionMethod (int theActivity)
	{
		// -------------------------------------------------------------------------
		// 14/06/2017 ECU created to handle the selection of a specific activity
		// -------------------------------------------------------------------------
		// 14/06/2017 ECU created to handle the selection of the name of stored
		//                actions
		// -------------------------------------------------------------------------
		actionString += activities [theActivity];
		// -------------------------------------------------------------------------
		// 14/06/2017 ECU the command is complete
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SelectCommand (Context theContext,Object theUnderlyingObject,Method theReturnMethod)
	{
		// -------------------------------------------------------------------------
		// 21/01/2016 ECU created to handle the dialogue to acquire the action
		//                commands
		// 24/01/2016 ECU initialise the action string
		// 21/04/2018 ECU added the underlying object as an argument so that the
		//                return method can be static or non-static.
		//
		//				  theUnderlyingObject
		//                ===================
		//                StaticData.STATIC_METHOD			return method is static
		//                not StaticData.STATIC_METHOD	    return method is non-static
		// -------------------------------------------------------------------------
		actionString		= null;
		context 			= theContext;
		returnMethod		= theReturnMethod;
		underlyingObject	= theUnderlyingObject;
		// -------------------------------------------------------------------------
		// 24/01/2016 ECU called to define an action command
		// -------------------------------------------------------------------------
		ActionCommandMethod (null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void SelectCommand (Context theContext,Method theReturnMethod)
	{
		// -------------------------------------------------------------------------
		// 21/04/2018 ECU created to handle the situation where the return method
		//                is static
		// -------------------------------------------------------------------------
		SelectCommand (theContext,StaticData.STATIC_METHOD,theReturnMethod);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public static void AlexaMethod (String theAlexaCommands)
	{
		// -------------------------------------------------------------------------
		// 10/04/2019 ECU add the phone number to the action string
		// -------------------------------------------------------------------------
		actionString += theAlexaCommands;
		// -------------------------------------------------------------------------
		// 10/04/2019 ECU the command is complete
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void DelayMethod (int theDelay)
	{
		// -------------------------------------------------------------------------
		// 03/05/2017 ECU created to store the delay in the action string
		// -------------------------------------------------------------------------
		actionString += StaticData.BLANK_STRING + theDelay;
		// -------------------------------------------------------------------------
		// 11/03/2016 ECU indicate that the command is complete
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
		// -------------------------------------------------------------------------
	}
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
				   					 Utilities.createAMethod (ActionCommandUtilities.class,"EmailSubjectMethod",StaticData.BLANK_STRING),
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
											  Utilities.createAMethod (ActionCommandUtilities.class,"EmailMessageMethod",StaticData.BLANK_STRING),
											  null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void NamedActionsMethod (int theOption)
	{
		// -------------------------------------------------------------------------
		// 03/05/2017 ECU created to handle the options associated with using
		//                named actions
		// -------------------------------------------------------------------------
		switch (theOption)
		{
			// ---------------------------------------------------------------------
			case 0:
				// -----------------------------------------------------------------
				// 03/05/2017 ECU just the named action is to be selected
				// -----------------------------------------------------------------
				DialogueUtilities.singleChoice (context,
											    context.getString (R.string.named_actions_name_title),
											    NamedAction.getNames (),
											    0, 
											    Utilities.createAMethod (ActionCommandUtilities.class,"NamedActionsNameMethod",0),
											    null);
				// -----------------------------------------------------------------
				break;
			case 1:
				// -----------------------------------------------------------------
				// 03/05/2017 ECU the number of repeats is to be obtained before
				//                getting the 'named action'
				// -----------------------------------------------------------------
				DialogueUtilities.sliderChoice (context,
												context.getString (R.string.named_actions_repeat),
												context.getString (R.string.named_actions_repeat_summary),
												R.drawable.timer,
												null,
												1,
												1,
												100,
												context.getString (R.string.named_action_repeat_click),
												Utilities.createAMethod (ActionCommandUtilities.class,"NamedActionsRepeatMethod",0),
												context.getString (R.string.cancel_operation));
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------	
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void NamedActionsNameMethod (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 04/08/2016 ECU created to handle the selection of the name of stored
		//                actions
		// -------------------------------------------------------------------------
		actionString += PublicData.namedActions.get (theIndex).name;
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU the command is complete
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void NamedActionsRepeatMethod (int theRepeats)
	{
		// -------------------------------------------------------------------------
		// 03/05/2017 ECU store the number of repeats in the message
		// 07/02/2018 ECU changed to use StaticData
		// -------------------------------------------------------------------------
		actionString += StaticData.BLANK_STRING + theRepeats + StaticData.ACTION_DELIMITER;
		// -------------------------------------------------------------------------
		// 03/05/2017 ECU now request the 'named action' that is required
		// -------------------------------------------------------------------------
		NamedActionsMethod (0);
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
		// 08/06/2019 ECU changed to use resource
		// -------------------------------------------------------------------------
		actionString += context.getString (R.string.off);
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
		// 08/06/2019 ECU changed to use resource
		// -------------------------------------------------------------------------
		actionString += context.getString (R.string.on);
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
	public static void PackageNameMethod (String thePackageName)
	{
		// -------------------------------------------------------------------------
		// 12/08/2018 ECU called when a package name has been selected
		// -------------------------------------------------------------------------
		actionString += thePackageName;
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
		// 29/11/2018 ECU use the new method to replace the must root folder, if
		//                initialised, with a static marker
		// -------------------------------------------------------------------------
		actionString += Utilities.musicLibraryReplacement (theFileName,true);
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
		// 10/04/2019 ECU added 'alexa'
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU reset the action string to the selected value
		// 24/01/2016 ECU decide whether this is the first command or not
		// -------------------------------------------------------------------------
		if (actionString == null)
		{
			// ---------------------------------------------------------------------
			// 24/01/2016 ECU this is the first command 
			// ---------------------------------------------------------------------
			actionString = StaticData.ACTION_COMMANDS [theCommandIndex].command;
		}
		else
		{
			// ---------------------------------------------------------------------
			// 24/01/2016 ECU this is not the first command
			// ---------------------------------------------------------------------
			actionString += StaticData.ACTION_SEPARATOR + 
					        StaticData.ACTION_COMMANDS [theCommandIndex].command;
		}
		// -------------------------------------------------------------------------
		// 02/05/2016 ECU decide if a delimiter is to be added - check if there are
		//                any parameters 
		// -------------------------------------------------------------------------
		if (StaticData.ACTION_COMMANDS [theCommandIndex].parameters != null ) 
		{
			actionString += StaticData.ACTION_DELIMITER;
		}
		// -------------------------------------------------------------------------
		if (StaticData.ACTION_COMMANDS [theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_EMAIL))
		{
			// ---------------------------------------------------------------------
			// 23/01/2016 ECU ORed the EMAIL_ADDRESS with TYPE_CLASS_TEXT to get correct
			//                keyboard layout
			// ---------------------------------------------------------------------
			DialogueUtilities.textInput (context,
										 context.getString (R.string.email_addresses),
										 context.getString (R.string.enter_email_addresses),
										 StaticData.HINT + context.getString (R.string.type_in_email_addresses),
										 Utilities.createAMethod (ActionCommandUtilities.class,"EmailAddressMethod",StaticData.BLANK_STRING),
										 null,
										 InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		}
		else
		// -------------------------------------------------------------------------
		if (StaticData.ACTION_COMMANDS [theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_ACTIVITY))
		{
			// ---------------------------------------------------------------------
			// 14/06/2017 ECU handle the selection of an activity
			// ---------------------------------------------------------------------
			// 14/06/2017 ECU generate an array of activity legends
			// 01/01/2019 ECU changed from PublicData.storedData.gridImages
			// ---------------------------------------------------------------------
			activities = GridImages.returnLegends (GridActivity.originalGridImages);
			// ---------------------------------------------------------------------
			// 14/06/2017 ECU now ask the user to select an activity
			// ---------------------------------------------------------------------
			DialogueUtilities.singleChoice (context,
										    context.getString (R.string.select_activity_title),
										    activities,
										    0, 
										    Utilities.createAMethod (ActionCommandUtilities.class,"ActivitySelectionMethod",0),
										    null);
			// ---------------------------------------------------------------------
		}
		else
		// -------------------------------------------------------------------------
		if (StaticData.ACTION_COMMANDS [theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_ACTIVITY_LONG))
		{
			// ---------------------------------------------------------------------
			// 07/02/2018 ECU handle the selection of an activity with long press
			// ---------------------------------------------------------------------
			// 07/02/2018 ECU generate an array of activity legends
			// 01/01/2019 ECU changed from PublicData.storedData.gridImages
			// ---------------------------------------------------------------------
			activities = GridImages.returnLegendsLong (GridActivity.originalGridImages);
			// ---------------------------------------------------------------------
			// 07/02/2018 ECU now ask the user to select an activity
			// ---------------------------------------------------------------------
			DialogueUtilities.singleChoice (context,
											context.getString (R.string.select_activity_long_title),
											activities,
											0, 
											Utilities.createAMethod (ActionCommandUtilities.class,"ActivitySelectionMethod",0),
											null);
			// ---------------------------------------------------------------------
		}
		else
		// -------------------------------------------------------------------------
		if (StaticData.ACTION_COMMANDS [theCommandIndex].command.equalsIgnoreCase(StaticData.ACTION_DESTINATION_ALEXA))
		{
			// ---------------------------------------------------------------------
			// 10/04/2019 ECU add the commands associated with communicating to an
			//                'alexa' device
			// ---------------------------------------------------------------------
			DialogueUtilities.multilineTextInput (context,
												  context.getString (R.string.alexa_commands),
												  context.getString (R.string.enter_alexa_commands),
												  5,
												  StaticData.HINT + context.getString (R.string.type_in_alexa_commands),
												  Utilities.createAMethod (ActionCommandUtilities.class,"AlexaMethod",StaticData.BLANK_STRING),
												  null);
			// ---------------------------------------------------------------------
		}
		else
		// -------------------------------------------------------------------------
		if (StaticData.ACTION_COMMANDS [theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_APP))
		{
			// ---------------------------------------------------------------------
			// 12/02/2018 ECU handle the selection of an app and specify the method
			//                when the selection has been made
			// ---------------------------------------------------------------------
			Utilities.PickAPackage (context, Utilities.createAMethod (ActionCommandUtilities.class,"PackageNameMethod",StaticData.BLANK_STRING));
			// ---------------------------------------------------------------------
		}
		else
		// -------------------------------------------------------------------------
		if (StaticData.ACTION_COMMANDS [theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_DELAY))
		{
			// ---------------------------------------------------------------------
			// 03/05/2017 ECU added to process the 'delay' option which just has
			//                the delay specified in milliseconds
			// ---------------------------------------------------------------------
			DialogueUtilities.sliderChoice (context,
				    						context.getString (R.string.delay_request),
				    						context.getString (R.string.delay_initial_interval),
				    						R.drawable.timer,
				    						null,
				    						1,
				    						1,
				    						((int) StaticData.MILLISECONDS_PER_HOUR) / 1000,
				    						context.getString (R.string.click_to_set_delay),
				    						Utilities.createAMethod (ActionCommandUtilities.class,"DelayMethod",0),
				    						context.getString (R.string.cancel_operation));
			// ---------------------------------------------------------------------
		}	
		// -------------------------------------------------------------------------
		else
		if (StaticData.ACTION_COMMANDS [theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_FLUSH))
		{
			// ---------------------------------------------------------------------
			// 20/05/2017 ECU added to handle the 'flush' command - there are no
			//                associated parameters 
			//            ECU the 'flush'command will only have an effect if it is
			//                the first command entered
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (context.getString (actionString.startsWith (StaticData.ACTION_DESTINATION_FLUSH) ? R.string.action_flush_start
					                                                                                                     : R.string.action_flush_not_start), true);
			// ---------------------------------------------------------------------
			ActionCommandComplete ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_NAMED_ACTION))
		{
			// ---------------------------------------------------------------------
			// 04/08/2016 ECU check if any names exist
			// 03/05/2017 ECU changed to use the method
			// ---------------------------------------------------------------------
			if (NamedAction.getNames () != null)
			{
				// ---------------------------------------------------------------------
				DialogueUtilities.listChoice (context, 
											  context.getString (R.string.select_named_actions_option),
											  StaticData.ACTION_COMMANDS [theCommandIndex].ReturnParameters(),
											  Utilities.createAMethod (ActionCommandUtilities.class,"NamedActionsMethod",0),
											  context.getString (R.string.cancel),
											  null);
				// ----------------------------------------------------------------------
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
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_NOTIFICATION))
		{
			// ---------------------------------------------------------------------
			// 13/07/2016 ECU added to handle notifications
			// ---------------------------------------------------------------------
			DialogueUtilities.multilineTextInput (context,
					                     	      context.getString (R.string.notification_message),
					                     	      context.getString (R.string.enter_notification_message),
					                     	      2,
					                     	      StaticData.HINT + context.getString (R.string.type_in_notification_message),
					                     	      Utilities.createAMethod (ActionCommandUtilities.class,"NotificationMethod",StaticData.BLANK_STRING),
					                     	      null);
			// ----------------------------------------------------------------------
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_PHONE))
		{
			// ---------------------------------------------------------------------
			// 22/01/2016 ECU the phone number just has the number as the argument
			// ---------------------------------------------------------------------
	 		DialogueUtilities.textInput (context,
	 									 context.getString (R.string.phone_number),
	 									 context.getString (R.string.enter_phone_number),
					   				 	 StaticData.HINT + context.getString (R.string.type_in_phone_number),
					   				 	 Utilities.createAMethod (ActionCommandUtilities.class,"PhoneNumberMethod",StaticData.BLANK_STRING),
					   				 	 null,
					   				 	 InputType.TYPE_CLASS_PHONE);
	 		// ---------------------------------------------------------------------
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_PLAY))
		{
			// ---------------------------------------------------------------------
			// 22/01/2016 ECU the play command only has a file as an argument
			// ---------------------------------------------------------------------
			Utilities.selectAFile (context,StaticData.EXTENSION_MUSIC,
					new MethodDefinition <ActionCommandUtilities> (ActionCommandUtilities.class,"PlayFile"));
			// ----------------------------------------------------------------------
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_SCREEN))
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
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_SMS))
		{
			// ---------------------------------------------------------------------
			// 22/01/2016 ECU created to set up the SMS command
			// ---------------------------------------------------------------------
			DialogueUtilities.textInput (context,
					 					 context.getString (R.string.sms_phone_number),
					   					 context.getString (R.string.enter_phone_number),
					   					 StaticData.HINT +  context.getString (R.string.type_in_phone_number),
					   					 Utilities.createAMethod (ActionCommandUtilities.class,"SMSPhoneNumberMethod",StaticData.BLANK_STRING),
					   					 null,
					   					 InputType.TYPE_CLASS_PHONE);
			// ----------------------------------------------------------------------
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_SPEAK))
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
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_TIME))
		{
			// ---------------------------------------------------------------------
			// 02/05/2016 ECU added to handle the speaking clock - there are no
			//                associated parameters 
			// ---------------------------------------------------------------------
			ActionCommandComplete ();
			// ---------------------------------------------------------------------
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_TRACK))
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
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_VIBRATE))
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
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_VIDEO))
		{
			// ---------------------------------------------------------------------
			// 21/05/2016 ECU the video command only has a file as an argument
			// 23/05/2017 ECU changed to use EXTENSIONS_VIDEO rather than EXTENSION_VIDEO
			//                to accommodate all videos that are being searched for
			// ---------------------------------------------------------------------
			Utilities.selectAFile (context,StaticData.EXTENSIONS_VIDEO,
					new MethodDefinition <ActionCommandUtilities> (ActionCommandUtilities.class,"PlayFile"));
			// ----------------------------------------------------------------------
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_WEMO))
		{
			DialogueUtilities.textInput (context,
					 					 context.getString (R.string.wemo_device_to_control),
					 					 context.getString (R.string.wemo_friendly_name),
										 StaticData.HINT + context.getString (R.string.type_in_wemo_friendly_name),
										 Utilities.createAMethod (ActionCommandUtilities.class,"WeMoMethod",StaticData.BLANK_STRING),
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
											  Utilities.createAMethod (ActionCommandUtilities.class,"SMSMessageMethod",StaticData.BLANK_STRING),
											  null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SpeakDelayMethod (int theDelay)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU created to set the speak delay and then request the phrase
		// -------------------------------------------------------------------------
		actionString += StaticData.BLANK_STRING + (theDelay * 1000) + StaticData.ACTION_DELIMITER;
		// -------------------------------------------------------------------------
		// 07/03/2016 ECU changed to use multiline text input
		// -------------------------------------------------------------------------
		DialogueUtilities.multilineTextInput (context,
									 		  context.getString (R.string.phrase_to_be_spoken),
									 		  context.getString (R.string.enter_phrase_to_be_spoken),
									 		  2,
									 		  StaticData.HINT + context.getString (R.string.type_in_phrase_to_be_spoken),
									 		  Utilities.createAMethod (ActionCommandUtilities.class,"SpeakPhraseMethod",StaticData.BLANK_STRING),
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
											 		  Utilities.createAMethod (ActionCommandUtilities.class,"SpeakPhraseMethod",StaticData.BLANK_STRING),
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
				 					 Utilities.createAMethod (ActionCommandUtilities.class,"TrackEmailAddressMethod",StaticData.BLANK_STRING),
				 					 null,
				 					 InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void VibrateDurationMethod (int theDuration)
	{
		// -------------------------------------------------------------------------
		// 11/03/2016 ECU created to set the speak delay and then request the phrase
		// 20/03/2017 ECU changed to use BLANK....
		// -------------------------------------------------------------------------
		actionString += StaticData.BLANK_STRING + (theDuration * 1000);
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
		// 08/06/2019 ECU chnged to use Static...
		// -------------------------------------------------------------------------
		actionString += theDeviceName + StaticData.SPACE_STRING;
		// -------------------------------------------------------------------------
		// 08/06/2019 ECU changed to use resources for On/Off
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (context,  
								 context.getString(R.string.wemo_device_command),
								 context.getString(R.string.on_off_arguments),
								 null,
								 true,context.getString (R.string.On),
								 	Utilities.createAMethod (ActionCommandUtilities.class,"OnMethod",(Object) null),
								 true,context.getString (R.string.Off),
								 	Utilities.createAMethod (ActionCommandUtilities.class,"OffMethod",(Object) null)); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

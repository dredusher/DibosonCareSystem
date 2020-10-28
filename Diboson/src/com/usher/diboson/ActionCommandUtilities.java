package com.usher.diboson;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.text.InputType;

import java.lang.reflect.Method;

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
	static String  [] 	bluetoothDevices;
	static String  [] 	namedActions;
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
	public static void ActivityLongSelectionMethod (int theActivity)
	{
		// -------------------------------------------------------------------------
		// 20/07/2020 ECU created to handle the 'long' selection of a specific activity
		// -------------------------------------------------------------------------
		activities = activities [theActivity].split (StaticData.LEGEND_SEPARATOR);
		// -------------------------------------------------------------------------
		// 20/07/2020 ECU at this point
		//					activities [0] contains 'legend'
		//                  activities [1] contains 'long legend'
		// -------------------------------------------------------------------------
		// 14/06/2017 ECU created to handle the selection of the name of stored
		//                actions
		// -------------------------------------------------------------------------
		actionString += activities [1];
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
	// =============================================================================
	public static void ActionFile (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 11/04/2020 ECU added to define the file that contains actions to be
		//                processed
		// -------------------------------------------------------------------------
		actionString += theFileName;
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
		// -------------------------------------------------------------------------
	}
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
	// =============================================================================
	public static void BluetoothMethod (int theBluetoothOption)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU handle the options
		// -------------------------------------------------------------------------
		switch (theBluetoothOption)
		{
			// ---------------------------------------------------------------------
			case 0:
				// -----------------------------------------------------------------
				// 12/04/2020 ECU select a paired device to connect to
				// -----------------------------------------------------------------
				bluetoothDevices = BluetoothUtilities.getNamesOfBondedDevices ();
				// -----------------------------------------------------------------
				// 12/04/2020 ECU check if there are any devices
				// -----------------------------------------------------------------
				if (bluetoothDevices != null)
				{
					// -------------------------------------------------------------
					// 12/04/2020 ECU there are devices so let the user select one
					// --------------------------------------------------------------
					DialogueUtilities.singleChoice (context,
													context.getString (R.string.select_bluetooth_title),
													bluetoothDevices,
													0,
													Utilities.createAMethod (ActionCommandUtilities.class,"BluetoothSelectionMethod",0),
													null);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 12/04/2020 ECU either no bluetooth adapter or no bonded devices
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (context.getString (R.string.bluetooth_no_bonded_devices),true);
					// -------------------------------------------------------------
					// 12/04/2020 ECU make sure that the action string is tidied up
					//                at this point the action string ends
					//                with 'bluetooth:" which needs to be removed
					// -------------------------------------------------------------
					actionString = actionString.substring(0,actionString.length()
						- (StaticData.ACTION_DESTINATION_BLUETOOTH.length() + StaticData.ACTION_DELIMITER.length()));
					// -------------------------------------------------------------
					ActionCommandComplete ();
					// -------------------------------------------------------------

				}
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case 1:
				// -----------------------------------------------------------------
				// 12/04/2020 ECU disconnect from currently connected device
				//            ECU need to remove the trailing delimiter which was
				//                initially added
				//                the "$" means the end of the line as 'replaceAll'
				//                uses REGEX - so this will delete the last delimiter
				//                in the action string
				// -----------------------------------------------------------------
				actionString = actionString.replaceAll (StaticData.ACTION_DELIMITER + "$",StaticData.BLANK_STRING);
				// -----------------------------------------------------------------
				// 12/04/2020 ECU indicate that this action is complete
				// -----------------------------------------------------------------
				ActionCommandComplete ();
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void BluetoothSelectionMethod (int theDevice)
	{
		// -------------------------------------------------------------------------
		// 12/04/2020 ECU created to handle the selection of a specific device
		// -------------------------------------------------------------------------
		actionString += bluetoothDevices [theDevice];
		// -------------------------------------------------------------------------
		// 14/06/2017 ECU the command is complete
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
		namedActions = NamedAction.getNames ();
		// -------------------------------------------------------------------------
		switch (theOption)
		{
			// ---------------------------------------------------------------------
			case 0:
				// -----------------------------------------------------------------
				// 03/05/2017 ECU just the named action is to be selected
				// 13/04/2020 ECU changed to use 'namedActions' which has the data
				//                sorted alphabetically
				// -----------------------------------------------------------------
				DialogueUtilities.singleChoice (context,
											    context.getString (R.string.named_actions_name_title),
											    namedActions,
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
		// 13/04/2020 ECU changed to use 'namedActions' which has the data sorted
		//                alphabetically
		// -------------------------------------------------------------------------
		actionString += namedActions [theIndex];
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
	public static void PhotographMethod (int thePhotographOption)
	{
		// -------------------------------------------------------------------------
		// 15/10/2020 ECU handle the options
		// -------------------------------------------------------------------------
		switch (thePhotographOption)
		{
			// ---------------------------------------------------------------------
			// ---------------------------------------------------------------------
			case 0:
				// -----------------------------------------------------------------
				// 15/10/2020 ECU select the path to the photograph
				// -----------------------------------------------------------------
				Utilities.selectAFile (context,StaticData.EXTENSION_PHOTOGRAPH,
						new MethodDefinition <ActionCommandUtilities> (ActionCommandUtilities.class,"PlayFile"));
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			// ---------------------------------------------------------------------
			case 1:
				// -----------------------------------------------------------------
				// 15/10/2020 ECU add the '0' as a minimum value
				// -----------------------------------------------------------------
				DialogueUtilities.sliderChoice (context,
											    context.getString (R.string.photograph_time_to_display_title),
												context.getString (R.string.photograph_time_to_display),
												R.drawable.timer,
												null,
												0,
												0,
												120,
												context.getString (R.string.click_to_set_delay),
												Utilities.createAMethod (ActionCommandUtilities.class,"PhotographDelayMethod",0),
												context.getString (R.string.cancel_operation));
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void PhotographDelayMethod (int theDelay)
	{
		// -------------------------------------------------------------------------
		// 15/10/2020 ECU created to set the time the photograph is displayed
		// -------------------------------------------------------------------------
		actionString += StaticData.BLANK_STRING + (theDelay * 1000) + StaticData.ACTION_DELIMITER;
		// -------------------------------------------------------------------------
		// 07/03/2016 ECU changed to use multiline text input
		// -------------------------------------------------------------------------
		Utilities.selectAFile (context,StaticData.EXTENSION_PHOTOGRAPH,
				new MethodDefinition <ActionCommandUtilities> (ActionCommandUtilities.class,"PlayFile"));
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
		// 14/02/2020 ECU add the conversion for any embedded delimiters or
		//                separator
		// -------------------------------------------------------------------------
		actionString += Utilities.musicLibraryReplacement (actionStringReplacement (theFileName,true),true);
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
			// 25/07/2020 ECU indicate that sorting is not required
			// ---------------------------------------------------------------------
			activities = GridImages.returnLegends (GridActivity.originalGridImages,false);
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
											Utilities.createAMethod (ActionCommandUtilities.class,"ActivityLongSelectionMethod",0),
											null);
			// ---------------------------------------------------------------------
		}
		else
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_ACTIONFILE))
		{
			// ---------------------------------------------------------------------
			// 11/04/2020 ECU added to get the action file
			// ---------------------------------------------------------------------
			Utilities.selectAFile (context,StaticData.EXTENSION_ACTIONS,
					new MethodDefinition <ActionCommandUtilities> (ActionCommandUtilities.class,"ActionFile"));
			// ----------------------------------------------------------------------
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
		if (StaticData.ACTION_COMMANDS [theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_BLUETOOTH))
		{
			// ---------------------------------------------------------------------
			// 12/04/2020 ECU connect to a 'paired' (bonded) bluetooth device and
			//                provide option to disconnect it
			// ---------------------------------------------------------------------
			DialogueUtilities.listChoice (context,
					context.getString (R.string.select_speak_bluetooth),
					StaticData.ACTION_COMMANDS [theCommandIndex].ReturnParameters(),
					Utilities.createAMethod (ActionCommandUtilities.class,"BluetoothMethod",0),
					context.getString (R.string.cancel),
					null);
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
		else
			// --------------------------------------------------------------------------
			if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_DOCUMENT))
			{
				// ---------------------------------------------------------------------
				// 12/10/2020 ECU select a document
				// ---------------------------------------------------------------------
				Utilities.selectAFile (context,StaticData.EXTENSION_DOCUMENT,
						new MethodDefinition <ActionCommandUtilities> (ActionCommandUtilities.class,"PlayFile"));
				// ----------------------------------------------------------------------
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
											  StaticData.ACTION_COMMANDS [theCommandIndex].ReturnParameters (),
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
		// --------------------------------------------------------------------------
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_PHOTOGRAPH))
		{
			// ---------------------------------------------------------------------
			// 12/10/2020 ECU select a photograph
			// ---------------------------------------------------------------------
			DialogueUtilities.listChoice (context,
										  context.getString (R.string.select_photograph_option),
					                      StaticData.ACTION_COMMANDS [theCommandIndex].ReturnParameters(),
					                      Utilities.createAMethod (ActionCommandUtilities.class,"PhotographMethod",0),
					                      context.getString (R.string.cancel),
					                      null);
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
			// ---------------------------------------------------------------------
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
		if (StaticData.ACTION_COMMANDS[theCommandIndex].command.equalsIgnoreCase (StaticData.ACTION_DESTINATION_SPEAKANDDISPLAY))
		{
			// ---------------------------------------------------------------------
			// 11/08/2020 ECU handle the definition of a 'phrase'to speak and
			//                display
			// ---------------------------------------------------------------------
			DialogueUtilities.multilineTextInput (context,
					                              context.getString (R.string.phrase_to_be_spoken_and_displayed),
												  context.getString (R.string.enter_phrase_to_be_spoken_and_displayed),
					  							  2,
												  StaticData.HINT + context.getString (R.string.type_in_phrase_to_be_spoken),
												  Utilities.createAMethod (ActionCommandUtilities.class,"SpeakPhraseMethod",StaticData.BLANK_STRING),
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
	public static void SpeakFile (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 13/10/2020 ECU create to set up the file to be spoken
		// -------------------------------------------------------------------------
		actionString += StaticData.ACTION_FILE + StaticData.ACTION_DELIMITER + Utilities.musicLibraryReplacement (actionStringReplacement (theFileName,true),true);
		// -------------------------------------------------------------------------
		ActionCommandComplete ();
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
			// ---------------------------------------------------------------------
			case 2:
				// -----------------------------------------------------------------
				// 13/10/2020 ECU the option to have the option to speak the
				//                contents of the specified file
				// -----------------------------------------------------------------
				Utilities.selectAFile (context,StaticData.EXTENSION_TEXT,
						new MethodDefinition <ActionCommandUtilities> (ActionCommandUtilities.class,"SpeakFile"));
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
		// 08/06/2019 ECU changed to use Static...
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

	// =============================================================================
	public static String actionStringReplacement (String theString,boolean theDirection)
	{
		// -------------------------------------------------------------------------
		// 12/04/2020 ECU when defining the data associated with an action it is
		//                possible that a 'separator' or 'delimiter' can be included
		//                within the data and this will cause problems when the
		//                action is subsequently processed - there parser will produce
		//                the wrong results, e.g.
		//
		//                    play:music folder;the track;speak:hello there
		//                    |       action 1            |    action 2
		//
		//                but because of the ';' in the data the parser will see
		//
		//                    play:music folder;the track;speak:hello there
		//                    |     action 1    |action 2 |  action 3
		//
		//                which is clearly wrong.
		//
		//                This method aims to replace any embedded delimiters or
		//                separators with replacement strings which can be converted
		//                back prior to the data being actioned.
		//
		//                The direction variable indicates :-
		//
		//                   true ................ convert any delimiters or separators
		//                                         to their replacement strings
		//                   false ............... convert any replacement strings back
		//                                         to delimiters or separators
		// -------------------------------------------------------------------------
		// 12/04/2020 ECU decide which way around the conversion is to take place
		// -------------------------------------------------------------------------
		if (theDirection)
		{
			// ---------------------------------------------------------------------
			// 12/04/2020 ECU convert to the replacement strings
			// ---------------------------------------------------------------------
			return (theString.replace(StaticData.ACTION_DELIMITER,StaticData.ACTION_DELIMITER_REPLACEMENT))
						.replace(StaticData.ACTION_SEPARATOR,StaticData.ACTION_SEPARATOR_REPLACEMENT);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 12/04/2020 ECU convert from the replacement strings
			// ---------------------------------------------------------------------
			return (theString.replace(StaticData.ACTION_DELIMITER_REPLACEMENT,StaticData.ACTION_DELIMITER))
					.replace(StaticData.ACTION_SEPARATOR_REPLACEMENT,StaticData.ACTION_SEPARATOR);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

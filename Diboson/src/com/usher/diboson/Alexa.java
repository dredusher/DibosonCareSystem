package com.usher.diboson;

import android.content.Context;
import android.media.AudioManager;

public class Alexa 
{
	// =============================================================================
	// 09/04/2019 ECU created to handle actions required to communicate with the
	//                Alexa device
	// 13/04/2019 ECU remember the current mode of the audio manager
	// 15/05/2020 ECU added 'initialised'
	// =============================================================================
	private final static String TAG = "Alexa";
	// =============================================================================
	
	// =============================================================================
	private final static String	INTRODUCTION_FORMAT = "speak:%s.......";
	private final static String	DELAY_INPUT         = "/";
	private final static String	DELAY_OUTPUT        = ".......";
	// =============================================================================
	
	// =============================================================================
	public static boolean commandIssued		 	= false;
		   static int	  currentMode;
	public static boolean enabledOnBluetooth 	= false;
	       static boolean initialised		 	= false;
	// =============================================================================
	
	// =============================================================================
	Context	context;
	String	ipAddress;
	// =============================================================================
	
	// =============================================================================
	public Alexa (Context theContext,String theIPAddress)
	{
		// -------------------------------------------------------------------------
		// 10/04/2019 ECU create a new instance of the utilities and copy across any
		//                data
		// -------------------------------------------------------------------------
		context		= theContext;
		// -------------------------------------------------------------------------
		ipAddress 	= theIPAddress;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public static void finish (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 13/04/2019 ECU created to perform any actions needed when the app is
		//                finishing
		// 15/05/2020 ECU added the check on 'initialised'
		// -------------------------------------------------------------------------
		if (initialised && enabledOnBluetooth)
		{
			// ---------------------------------------------------------------------
			// 13/04/2019 ECU want to restore the state of the AudioManager
			// ---------------------------------------------------------------------
			AudioManager audioManager = (AudioManager) theContext.getSystemService (Context.AUDIO_SERVICE);
			// ---------------------------------------------------------------------
			audioManager.setMode (currentMode);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void initialise (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 11/04/2019 ECU create to do any initialisation that may be required
		// 15/05/2020 ECU put in the check on 'initialised' - just 'belt & braces'
		// -------------------------------------------------------------------------
		if (!initialised)
		{
			Utilities.debugMessage (TAG,"initialise");
			// ---------------------------------------------------------------------
			// 11/04/2019 ECU make sure that the device uses its own speaker
			// ---------------------------------------------------------------------
			if (Utilities.isBluetoothEnabled ())
			{
				// -----------------------------------------------------------------
				// 13/04/2019 ECU remember the current state of the device
				// 18/05/2020 ECU check if there is a 'connected' bluetooth device
				//                that can be used for the 'Alexa' commands
				// -----------------------------------------------------------------
				if ((PublicData.connectedBluetoothDevices != null) &&
					(PublicData.connectedBluetoothDevices.size() > 0))
				{
					// -------------------------------------------------------------
					// 18/05/2020 ECU Note - want to direct commands to the connected
					//                       bluetooth speaker and normal speech will
					//                       be output to the device's speaker
					// -------------------------------------------------------------
					currentMode = Utilities.SelectBluetoothSpeaker (theContext,false);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				commandIssued		= false;
				enabledOnBluetooth 	= true;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 15/05/2020 ECU indicate that initialisation has occurred
			// ---------------------------------------------------------------------
			initialised = true;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	private static String introduction ()
	{
		// -------------------------------------------------------------------------
		// 13/02/2020 ECU change the introduction depending on the 'wake' word that
		//                may have been set up
		// -------------------------------------------------------------------------
		return String.format (INTRODUCTION_FORMAT,
								(PublicData.storedData.alexaWakeWord == null) ? StaticData.ALEXA_WAKE_WORD
										                                      : PublicData.storedData.alexaWakeWord);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void processCommands (Context theContext,String theCommands)
	{
		// -------------------------------------------------------------------------
		// 15/05/2020 ECU added the check on initialisation - used to be done in
		//                MainActivity but no need to always do - only do if an
		//                'Alexa' command is issued
		// -------------------------------------------------------------------------
		if (!initialised)
		{
			// ---------------------------------------------------------------------
			// 15/05/2020 ECU not yet initialised so perform that first
			// ---------------------------------------------------------------------
			initialise (theContext);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 10/04/2019 ECU pre-process the commands before 'sending' to the device
		//            ECU make sure that there is a device that can receive the command
		// -------------------------------------------------------------------------
		if (!enabledOnBluetooth)
		{
			// ---------------------------------------------------------------------
			// 11/04/2019 ECU need to try and access the Alexa echo by wireless
			// 19/05/2020 ECU change to use the 'empty' method rather than have the
			// 				  code here
			// ---------------------------------------------------------------------
			if (Utilities.emptyString (PublicData.storedData.alexaDeviceIPAddress))
			{
				// -----------------------------------------------------------------
				// 16/11/2019 ECU changed from 'replaceAll' to 'replace' because
				//                the former requires a REGEX so not sure why it ever
				//				  worked
				// -----------------------------------------------------------------
				Alexa alexaDevice = new Alexa (theContext,PublicData.storedData.alexaDeviceIPAddress);
				alexaDevice.transmit (theCommands.replace (DELAY_INPUT,DELAY_OUTPUT));
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 10/04/2019 ECU indicate that no device has been specified
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (theContext.getString (R.string.alexa_no_device),true);
				// ----------------------------------------------------------------- 
			}
			// ---------------------------------------------------------------------
			// 19/05/2020 ECU indicate that the action has been processed
			// ---------------------------------------------------------------------
			Utilities.actionIsFinished ();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 11/04/2019 ECU can try and access the Alexa Echo by bluetooth
			// ---------------------------------------------------------------------
			// 11/04/2019 ECU select the bluetooth speaker
			// ---------------------------------------------------------------------
			Utilities.SelectBluetoothSpeaker (theContext,true);
			// ---------------------------------------------------------------------
			// 11/04/2019 ECU indicate that a command has been issued
			// ---------------------------------------------------------------------
			commandIssued = true;
			// ---------------------------------------------------------------------
			// 11/04/2019 ECU now process the commands
			// 16/11/2019 ECU changed from 'replaceAll' to 'replace' because
			//                the former requires a REGEX so not sure why it ever
			//				  worked
			// 13/02/2020 ECU change to use the 'introduction' method
			// 19/05/2020 ECU changed from
			//					Utilities.actionHandler
			//						(theContext,introduction () + theCommands.replace (DELAY_INPUT,DELAY_OUTPUT));
			//                because this added the generated action into the
			//                existing list which, at this point, may not be empty.
			//                Actually need the generated action to be processed
			//                immediately and for it to indicate that the action
			//                has finished so that the next action in the list
			//                can be processed.
			// ---------------------------------------------------------------------
			Utilities.processAnAction (theContext,(introduction () + theCommands.replace (DELAY_INPUT,DELAY_OUTPUT)));
			// ---------------------------------------------------------------------
		}	
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void transmit (String theTransmission)
	{
		// -------------------------------------------------------------------------
		// 10/04/2019 ECU transmit the data to the handling device
		// 13/02/2020 ECU changed to use the 'introduction' method
		// -------------------------------------------------------------------------
		Utilities.sendSocketMessageSendTheObject(context,
				   								 ipAddress,
				   								 PublicData.socketNumberForData,
				   								 StaticData.SOCKET_MESSAGE_ACTIONS,
				   								 introduction () + theTransmission);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void utteranceComplete (Context theContext)
	{
		// ------------------------------------------------------------------------
		// 11/04/2019 ECU created to be called by TextToSpeechService to indicate
		//                that a spoken phrase has been completed
		// ------------------------------------------------------------------------
		if (Alexa.enabledOnBluetooth)
		{
			if (Alexa.commandIssued)
			{
				// ----------------------------------------------------------------
				// 11/04/2019 ECU need to do some tidying up
				// ----------------------------------------------------------------
				Alexa.commandIssued = false;
				// ----------------------------------------------------------------
				// 11/04/2019 ECU now set the path for audio back to the device
				// ----------------------------------------------------------------
				Utilities.SelectBluetoothSpeaker (theContext,false);
				// ----------------------------------------------------------------
			}
		}
		// ------------------------------------------------------------------------
	}
	// =============================================================================
	
	
	// =============================================================================
}

package com.usher.diboson;

import android.content.Context;
import android.media.AudioManager;

public class Alexa 
{
	// =============================================================================
	// 09/04/2019 ECU created to handle actions required to communicate with the
	//                Alexa device
	// 13/04/2019 ECU remember the current mode of the audio manager
	// =============================================================================
	
	// =============================================================================
	private final static String	INTRODUCTION  = "speak:alexa.......";
	private final static String	DELAY_INPUT   = "/";
	private final static String	DELAY_OUTPUT  = ".......";
	// =============================================================================
	
	// =============================================================================
	public static boolean commandIssued		 = false;
		   static int	  currentMode;
	public static boolean enabledOnBluetooth = false;
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
		// -------------------------------------------------------------------------
		if (enabledOnBluetooth)
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
		// -------------------------------------------------------------------------
		// 11/04/2019 ECU make sure that the device uses its own speaker
		// -------------------------------------------------------------------------
		if (Utilities.isBluetoothEnabled ())
		{
			// ---------------------------------------------------------------------
			// 13/04/2019 ECU remember the current state of the device
			// ---------------------------------------------------------------------
			currentMode = Utilities.SelectBluetoothSpeaker (theContext,false);
			// ---------------------------------------------------------------------
			commandIssued		= false;
			enabledOnBluetooth 	= true;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void processCommands (Context theContext,String theCommands)
	{
		// -------------------------------------------------------------------------
		// 10/04/2019 ECU pre-process the commands before 'sending' to the device
		//            ECU make sure that there is a device that can receive the command
		// -------------------------------------------------------------------------
		if (!enabledOnBluetooth)
		{
			// ---------------------------------------------------------------------
			// 11/04/2019 ECU need to try and access the Alexa echo by wireless
			// ---------------------------------------------------------------------
			if (PublicData.storedData.alexaDeviceIPAddress != null && 
					!PublicData.storedData.alexaDeviceIPAddress.equalsIgnoreCase (StaticData.BLANK_STRING))
			{
				// -----------------------------------------------------------------
				Alexa alexaDevice = new Alexa (theContext,PublicData.storedData.alexaDeviceIPAddress);
				alexaDevice.transmit (theCommands.replaceAll (DELAY_INPUT,DELAY_OUTPUT));
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
			// ---------------------------------------------------------------------
			Utilities.actionHandler (theContext,INTRODUCTION + theCommands.replaceAll (DELAY_INPUT,DELAY_OUTPUT));
			// ---------------------------------------------------------------------
		}	
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void transmit (String theTransmission)
	{
		// -------------------------------------------------------------------------
		// 10/04/2019 ECU transmit the data to the handling device
		// -------------------------------------------------------------------------
		Utilities.sendSocketMessageSendTheObject(context,
				   								 ipAddress,
				   								 PublicData.socketNumberForData,
				   								 StaticData.SOCKET_MESSAGE_ACTIONS,
				   								 INTRODUCTION + theTransmission);
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

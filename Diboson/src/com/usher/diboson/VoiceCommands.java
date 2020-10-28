package com.usher.diboson;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

public class VoiceCommands
{
	// -----------------------------------------------------------------------------
	// 17/06/2013 ECU major changes so that commandWords is now a 2 dimensional array
	//                so that alternative command words for the same command can be specified
	//                easily
	// 07/09/2015 ECU remove the Initialise method and instead use multiple
	//                constructors
	// -----------------------------------------------------------------------------
	String [][] 	commandWords;
	int				commandToRun;
	String			commandData;	
	/* =========================================================================== */
	public VoiceCommands (String [][] theCommands, int theCommandNumber,String theCommandData)
	{
		commandWords 	= theCommands;
		commandToRun   	= theCommandNumber;
		commandData     = theCommandData;
	}
	/* =========================================================================== */
	public VoiceCommands (String [] theCommands, int theCommandNumber,String theCommandData)
	{
		commandWords        = new String [1][theCommands.length];
		commandWords [0]	= theCommands;
		commandToRun 	  	= theCommandNumber;
		commandData   		= theCommandData;
	}
	/* =========================================================================== */
	public VoiceCommands (String [][] theCommands, int theCommandNumber)
	{
		this (theCommands,theCommandNumber,null);
	}
	/* =========================================================================== */
	public VoiceCommands (String [] theCommands, int theCommandNumber)
	{
		this (theCommands,theCommandNumber,null);
	}
	// =============================================================================
	public void Print (Context theContext)
	{
		Print (theContext, (TextToSpeech) null);
	}
	/* =========================================================================== */
	public void Print (Context theContext, TextToSpeech theTextToSpeech)
	{
		String theEntry = "Command = " + commandToRun + "\nCommand Data = " + commandData + StaticData.NEWLINE;
		
		for (int entry=0; entry < commandWords.length; entry++)
		{
			theEntry += "Entry " + entry + "     ";
			
			for (int index = 0; index < commandWords[entry].length; index++)
			{
				theEntry += commandWords[entry][index] + " ";
			}
			theEntry += StaticData.NEWLINE;	
		}
		// -------------------------------------------------------------------------
		// 08/11/2013 ECU use the custom toast
		// 22/02/2014 ECU add the arguments to centre the text for a short period
		// -------------------------------------------------------------------------
		Utilities.popToast (theEntry,true,Toast.LENGTH_SHORT);
		// -------------------------------------------------------------------------
		// 17/06/2013 ECU speak the command if so required
		// -------------------------------------------------------------------------
		if (theTextToSpeech != null)
			theTextToSpeech.speak (theEntry,TextToSpeech.QUEUE_ADD, null);
	}
	/* ============================================================================= */
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 03/09/2013 ECU added - although a command can have multiple command words
		//                only print the first entry
		// -------------------------------------------------------------------------
		String theString = StaticData.BLANK_STRING;
		
		for (int theIndex = 0; theIndex < commandWords[0].length; theIndex++)
			theString += commandWords[0][theIndex] + " ";
		
		return theString;
	}
	/* ============================================================================= */
}

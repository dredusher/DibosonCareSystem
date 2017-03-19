package com.usher.diboson;

import java.util.ArrayList;
import java.util.Locale;
import android.content.Context;
import android.os.Message;

public class BlueToothServiceUtilities 
{
	// =============================================================================
	// 14/10/2015 ECU created to provide methods that can be used by classes that
	//                want to access the bluetooth service
	// 15/10/2015 ECU added 'usePublicData' because a service set as a broadcast
	//                receiver cannot use bind to send messages. Not happy with this
	//                but get things working
	// =============================================================================
	
	// =============================================================================
	// 14/10/2015 ECU declare data that the class uses
	// -----------------------------------------------------------------------------
	public BlueToothHandler 	blueToothHandler;
	// -----------------------------------------------------------------------------
	private int					remoteDevice		= StaticData.NO_RESULT;
																	// 15/10/2015 ECU added the preset
	private ArrayList<String> 	televisionMeanings 	= null;
	private boolean				useTimerHandler		= false;		// 15/10/2015 ECU added
	// =============================================================================
	
	// =============================================================================
	public BlueToothServiceUtilities (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU main constructor
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU create the handler that will be used for communicating with
		//                the bluetooth service
		// -------------------------------------------------------------------------
		blueToothHandler	= new BlueToothHandler (theContext);
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU set the initial remote device and create the meanings
		//                list
		// -------------------------------------------------------------------------
		setRemoteDevice (Television.REMOTE_SAMSUNG_TV);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public BlueToothServiceUtilities (Context theContext,String thePhrase)
	{
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU a constructor that will automatically process the
		//                specified phrase having been created
		// -------------------------------------------------------------------------
		this (theContext);
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU now process the specified phrase
		// -------------------------------------------------------------------------
		processPhrase (thePhrase);
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU now 'unbind' from the service
		// -------------------------------------------------------------------------
		unBind ();
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public BlueToothServiceUtilities (Context theContext,int theRemoteDevice,String theChannelName)
	{
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU a constructor that will automatically process the
		//                specified phrase having been created
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU set up the arrays for the specified remote device
		// -------------------------------------------------------------------------
		setRemoteDevice (theRemoteDevice);
		// -------------------------------------------------------------------------
		// 15/10/2015 ECU indicate that information is to be passed using PublicData
		//                rather than a message which is not allowed for broadcast
		//                receiver which is what is used by AlarmService (as of
		//                04/03/2017 ECU name is now AlarmReceiver)
		// -------------------------------------------------------------------------
		useTimerHandler = true;
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU now process the specified phrase
		// -------------------------------------------------------------------------
		processChannelName (theChannelName);
		// -------------------------------------------------------------------------
	} 
	// =============================================================================
	public boolean processChannelName (String theChannelName)
	{
		// -------------------------------------------------------------------------
		// 14/10/2014 ECU created to try and locate the channel in the registered
		//                list and then to send associated data to the remote device for 
		//                actioning
		// -------------------------------------------------------------------------
		// 24/07/2016 ECU change from a compare to a starts with and user lower
		//                case
		// -------------------------------------------------------------------------
		theChannelName = theChannelName.toLowerCase (Locale.getDefault());
		// -------------------------------------------------------------------------
		// 24/07/2016 ECU change the check around because the input channel name
		//                will be longer than that stored
		// -------------------------------------------------------------------------
		for (int theChannel = 0; theChannel < Television.televisionChannels.length; theChannel++)
		{
			if (theChannelName.startsWith (Television.televisionChannels[theChannel].channelName))
			{
				// -----------------------------------------------------------------
				// 02/03/2014 ECU have found a match for the spoken channel so now
				//                try and get the remote device to select that channel.
				//                Each digit will need to be sent individually
				// 12/05/2015 ECU put in the call to the method which sends out the 
				//                digits with a slight delay between each one
				// 14/10/2015 ECU changed to use ....Utilities
				// -----------------------------------------------------------------
				sendToRemoteController (TelevisionChannel.ReturnChannel (Television.televisionChannels[theChannel].channel),StaticData.INTER_DIGIT_DELAY);
				// -----------------------------------------------------------------			
				return true;
			}
		}
		// ------------------------------------------------------------------------
		// 14/10/2015 ECU no match was found
		// ------------------------------------------------------------------------
		return false;
		// ------------------------------------------------------------------------
	}
	// =============================================================================
	public boolean processPhrase (String thePhrase)
	{
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU created to see whether the specified phrase has an entry
		//                in the meanings array - if so then the associated
		//                code is transmitted to bluetooth service and this method
		//                returns true. If no phrase is found then the method returns
		//                false and no action is taken
		//            ECU note that this code used to be in Dialogue.java
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < televisionMeanings.size(); theIndex++)
		{
			if (thePhrase.equals(televisionMeanings.get(theIndex)))
			{
				// -----------------------------------------------------------------
				// 14/10/2015 a matching phrase has been found so send the
				//            associated code to the service
				// -----------------------------------------------------------------
				sendMessage (Television.remoteControllers [remoteDevice].codes.type + "," + 
				   Television.remoteControllers [remoteDevice].codes.ReturnTheCode(Television.remoteControllers [remoteDevice].codes.codes [theIndex].function.function) + 
				   						Television.MESSAGE_TERMINATOR);	
				// -----------------------------------------------------------------
				// 14/10/2015 ECU indicate that a match was found
				// -----------------------------------------------------------------
				return true;
			}
		}
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU indicate that a match was not found
		// -------------------------------------------------------------------------
		return false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void sendMessage (String theString)
	{
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU created to pass the string to the service so that it can be
		//                send to the remote controller
		// 15/10/2015 ECU determine what to do based on the public data flag
		// -------------------------------------------------------------------------
		if (!useTimerHandler)
			blueToothHandler.SendMessage (theString);
		else
		{
			// ---------------------------------------------------------------------
			// 15/10/2015 ECU send a message to the bluetooth service for
			//                transmission to the remote controller
			// 24/07/2016 ECU put in the check on null
			// ----------------------------------------------------------------------
			if (BlueToothService.timerHandler != null)
			{
				Message localMessage = BlueToothService.timerHandler.obtainMessage (StaticData.MESSAGE_DATA);
				localMessage.obj 	= theString;
				BlueToothService.timerHandler.sendMessage(localMessage);
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void sendToRemoteController (final ArrayList<Integer> theDigits,final int theDelay)
	{
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU created to send a string of digits to the remote controller
		//                with a delay between each transmission
		//            ECU the code used to be in Dialogue.java
		// -------------------------------------------------------------------------
		Thread remoteThread = new Thread()
		{
			@Override
			public void run()
			{
				try 
				{
					synchronized(this)
					{
						for (int theDigit = 0; theDigit < theDigits.size(); theDigit++)
						{
							// -----------------------------------------------------
							// 02/03/2014 ECU changed to use the CHANNEL_DIGITS array
							// 11/05/2015 ECU changed to use the remote controller objects
							// 14/10/2015 ECU changed to use ....Utilities
							// -----------------------------------------------------
							sendMessage (Television.remoteControllers [remoteDevice].codes.type + "," + 
									     Television.remoteControllers [remoteDevice].codes.ReturnTheCode(Television.CHANNEL_DIGITS [theDigits.get(theDigit)]) +
							   		     Television.MESSAGE_TERMINATOR);	 
							// -----------------------------------------------------
							// 12/05/2015 ECU wait a bit between each digit
							// -----------------------------------------------------
							sleep (theDelay);
							// -----------------------------------------------------
						}
					}
				}
				catch(InterruptedException theException)
				{                    
				}       
			}
		};
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU start up the defined thread
		// -------------------------------------------------------------------------
		remoteThread.start(); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void setRemoteDevice (int theRemoteDevice)
	{
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU created to copy across the remote device into local
		//                variable
		// -------------------------------------------------------------------------
		remoteDevice = theRemoteDevice;
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU build the television meanings that correspond to the
		//                specified device
		// -------------------------------------------------------------------------
		televisionMeanings = Television.BuildMeaningsList (remoteDevice);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void unBind ()
	{
		// -------------------------------------------------------------------------
		// 14/10/2015 ECU created to 'unbind' from the bluetooth service
		// -------------------------------------------------------------------------
		blueToothHandler.UnBind();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

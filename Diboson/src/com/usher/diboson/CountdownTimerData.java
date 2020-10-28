package com.usher.diboson;

import java.io.Serializable;

public class CountdownTimerData implements Serializable
{
	// =============================================================================
	// 10/03/2018 ECU this class contains the data associated with a countdown timer
	//            ECU IMPORTANT cannot declare CountDownTimer in here because it is
	//                          NOT serializable and therefore cannot be stored in
	//                          'storedData'
	// 			  ECU IMPORTANT most of thois activity could be achieved with
	//                ========= alarms but wrote it this way just to play with
	//                          the in-built countdown timer
	// -----------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	// 10/03/2018 ECU declare the associated data
	// -----------------------------------------------------------------------------
	// timeTillDone ................. the time in milliseconds before the timer expires
	// timeBetweenTicks ............. the time in milliseconds between 'ticks'
	// actionsBetween ............... the actions to be performed when a 'tick' occurs
	// actionsDone .................. the actions to be performed when the timer expires
	// -----------------------------------------------------------------------------
	String			actionsBetween;
	String			actionsExpiration;
	long			timeBetweenTicks;					// in milliseconds
	long			timeExpiration;
	long			timeTillExpiration;					// in milliseconds
	// =============================================================================
	
	// =============================================================================
	public CountdownTimerData ()
	{
		// -------------------------------------------------------------------------
		// 10/03/2018 ECU basic constructor to clear the data
		// -------------------------------------------------------------------------
		actionsBetween 		= StaticData.BLANK_STRING;
		actionsExpiration 	= StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public CountdownTimerData (long 	theTimeTillDone,
			                   long		theTimeBetweenTicks,
			                   String 	theDoneActions,
			                   String 	theBetweenActions)
	{
		// -------------------------------------------------------------------------
		// 10/03/2018 ECU created to be the constructor using the specified arguments
		// -------------------------------------------------------------------------
		actionsBetween 		= theBetweenActions;
		actionsExpiration	= theDoneActions;
		timeBetweenTicks 	= theTimeBetweenTicks;
		timeTillExpiration 	= theTimeTillDone;
		// -------------------------------------------------------------------------
		// 10/03/2018 ECU now store the actual time when the countdown timer will
		//                expire
		// -------------------------------------------------------------------------
		timeExpiration = Utilities.getAdjustedTime (false) + timeTillExpiration;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	
	// =============================================================================
	public boolean stillValid ()
	{
		// -------------------------------------------------------------------------
		// 10/03/2018 ECU created to check whether the current countdown timer
		//                has already expired
		// -------------------------------------------------------------------------
		long currentTime = Utilities.getAdjustedTime (false);
		if (timeExpiration > currentTime)
		{
			// ---------------------------------------------------------------------
			// 10/03/2018 ECU the timer is still valid so return this fact after
			//                resetting the 'time left' to run
			// ---------------------------------------------------------------------
			timeTillExpiration = timeExpiration - currentTime;
			return true;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 10/03/2018 ECU the timer has already expired
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
}

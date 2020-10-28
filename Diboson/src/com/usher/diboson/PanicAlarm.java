package com.usher.diboson;

import java.io.Serializable;

public class PanicAlarm implements Serializable
{
	// =============================================================================
	// 25/11/2015 ECU created to hold details of the panic alarm
	// 26/11/2015 ECU changed to use DibosonTime
	// 27/11/2015 ECU added actions
	//            ECU added prompts
	// 28/11/2015 ECU added tracking and trackingEmail
	// 03/12/2015 ECU added 'security'
	// 04/12/2015 ECU add 'shake', which, if true, will cause an immediate panic
	//                alarm if the device is shaken
	// 10/12/2015 ECU added shake.. parameters
	// 11/12/2015 ECU added the actions associated with a user's response
	//            ECU rename actions -> alarmCommands, prompts -> promptCommands
	// 06/06/2017 ECU changed "\n" to StaticData.NEWLINE
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	String		alarmCommands		= null;
	boolean		enabled				= false;
	DibosonTime	endTime				= null;
	int			intervalTime		= 60;
	String		promptCommands		= null;
	String		responseCommands	= null;
	int			responseTime		= 60;
	String		security			= null;
	boolean		shake				= false;
	long		shakeIgnorePeriod	= StaticData.PANIC_ALARM_IGNORE_PERIOD;
	int			shakeNumber			= StaticData.PANIC_ALARM_SHAKE_NUMBER;
	long		shakeResetPeriod	= StaticData.PANIC_ALARM_RESET_PERIOD;
	float		shakeThreshold		= StaticData.PANIC_ALARM_SHAKE_THRESHOLD;
	DibosonTime	startTime			= null;
	boolean		tracking			= false;
	String		trackingEmail		= null;
	// -----------------------------------------------------------------------------
	// 25/11/2015 ECU declare those variables which are temporary and which control
	//                the active side of the panic alarm
	// -----------------------------------------------------------------------------
	boolean		active			= false;
	DibosonTime	nextPromptTime	= null;
	boolean		userResponse	= false;
	// =============================================================================
	
	// =============================================================================
	public PanicAlarm ()
	{
		// -------------------------------------------------------------------------
		// 23/01/2016 ECU added this constructor to initialise the startTime and
		//                endTime because was getting some issues with them being
		//                set to null
		// -------------------------------------------------------------------------
		endTime 	= new DibosonTime (0,0);
		startTime 	= new DibosonTime (0,0);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public String Print (String theTitle)
	{
		return theTitle + StaticData.NEWLINE +
			   "Enabled : " 			+ enabled + StaticData.NEWLINE +
			   "Active : " 				+ active + StaticData.NEWLINE +
			   "Start Time : " 			+ ((startTime == null) ? "unset" : startTime.Print ()) + StaticData.NEWLINE +
			   "End Time : " 			+ ((endTime == null) ? "unset" : endTime.Print ()) + StaticData.NEWLINE +
			   "Next Prompt Time : " 	+ ((nextPromptTime == null) ? "unset" : nextPromptTime.Print ()) + StaticData.NEWLINE +
			   "Interval : " 			+ intervalTime + StaticData.NEWLINE +
			   "Response : " 			+ responseTime + StaticData.NEWLINE +
			   "Prompt Commands : "     + promptCommands + StaticData.NEWLINE +
			   "Response Commands : "   + responseCommands + StaticData.NEWLINE +
			   "Alarm Commands : "		+ alarmCommands + StaticData.NEWLINE +
			   "Security : "            + security + StaticData.NEWLINE +
			   "Shake : "               + shake + StaticData.NEWLINE +
			   "Shake Number : " 		+ shakeNumber + StaticData.NEWLINE +
		 	   "Shake Threshold : " 	+ shakeThreshold + StaticData.NEWLINE +
		 	   "Shake Ignore Period : " + shakeIgnorePeriod + StaticData.NEWLINE +
		 	   "Shake Reset Period : " 	+ shakeResetPeriod;
		 
	}
	// =============================================================================
}

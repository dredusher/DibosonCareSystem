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
		return theTitle + "\n" +
			   "Enabled : " 			+ enabled + "\n" +
			   "Active : " 				+ active + "\n" +
			   "Start Time : " 			+ ((startTime == null) ? "unset" : startTime.Print ()) + "\n" +
			   "End Time : " 			+ ((endTime == null) ? "unset" : endTime.Print ()) + "\n" +
			   "Next Prompt Time : " 	+ ((nextPromptTime == null) ? "unset" : nextPromptTime.Print ()) + "\n" +
			   "Interval : " 			+ intervalTime + "\n" +
			   "Response : " 			+ responseTime + "\n" +
			   "Prompt Commands : "     + promptCommands + "\n" +
			   "Response Commands : "   + responseCommands + "\n" +
			   "Alarm Commands : "		+ alarmCommands + "\n" +
			   "Security : "            + security + "\n" +
			   "Shake : "               + shake + "\n" +
			   "Shake Number : " 		+ shakeNumber + "\n" +
		 	   "Shake Threshold : " 	+ shakeThreshold + "\n" +
		 	   "Shake Ignore Period : " + shakeIgnorePeriod + "\n" +
		 	   "Shake Reset Period : " 	+ shakeResetPeriod;
		 
	}
	// =============================================================================
}

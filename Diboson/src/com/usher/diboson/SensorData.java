package com.usher.diboson;

import java.io.Serializable;

public class SensorData implements Serializable
{
	// =============================================================================
	// 14/03/2015 ECU created to hold the data that is associated with a sensor
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	public 	int		lowerTrigger;
	public 	String	lowerTriggerActions;
	public 	boolean	triggered;
	public	int		upperTrigger;
	public 	String	upperTriggerActions;
	// =============================================================================
	public SensorData ()
	{
		// -------------------------------------------------------------------------
		// 14/03/2015 ECU declare the general constructor and set the variables
		// -------------------------------------------------------------------------
		lowerTrigger			=	StaticData.NO_RESULT;
		lowerTriggerActions		= 	null;
		triggered				= 	false;
		upperTrigger			=	StaticData.NO_RESULT;
		upperTriggerActions		= 	null;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 14/03/2015 ECU return a summary of the sensor data
		// --------------------------------------------------------------------------
		return "Lower Trigger = " + lowerTrigger + "\n" +
				"Upper Trigger = " + upperTrigger + "\n" +
				"Lower Trigger Actions = " + lowerTriggerActions + "\n" +
				"Upper Trigger Actions = " + upperTriggerActions + "\n" +
				"Trigger State = " + triggered;	
	}
	// =============================================================================
}

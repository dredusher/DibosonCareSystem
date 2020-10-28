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
	// -----------------------------------------------------------------------------
	public SensorData (int theLowerTrigger,int theUpperTrigger)
	{
		// -------------------------------------------------------------------------
		// 23/06/2017 ECU created when want to set initial triggers are to be set
		// -------------------------------------------------------------------------
		lowerTrigger			=	theLowerTrigger;
		lowerTriggerActions		= 	null;
		triggered				= 	false;
		upperTrigger			=	theUpperTrigger;
		upperTriggerActions		= 	null;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 14/03/2015 ECU return a summary of the sensor data
		// --------------------------------------------------------------------------
		return "Lower Trigger = " + lowerTrigger + StaticData.NEWLINE +
				"Upper Trigger = " + upperTrigger + StaticData.NEWLINE +
				"Lower Trigger Actions = " + lowerTriggerActions + StaticData.NEWLINE +
				"Upper Trigger Actions = " + upperTriggerActions + StaticData.NEWLINE +
				"Trigger State = " + triggered;	
	}
	// =============================================================================
}

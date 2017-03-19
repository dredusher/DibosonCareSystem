package com.usher.diboson;

import java.io.Serializable;

public class Liquid implements Serializable,Comparable<Liquid>
{
	// =============================================================================
	// 18/05/2016 ECU created to hold information about liquids
	// 19/09/2016 ECU added the path to an associated photograph
	// -----------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	String	actions;
	float	ambientLightLevelScaled;
	String	name;
	String  photographPath;								// 19/09/2016 ECU added
	// =============================================================================
	
	// =============================================================================
	public Liquid (float theLiquidLightLevel,
			       float theDefaultLightLevel,
			       String theName,
			       String theActions,
			       String thePhotographPath)
	{
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU created to copy across the values into the object
		// -------------------------------------------------------------------------
		actions					= theActions;
		ambientLightLevelScaled = theLiquidLightLevel / theDefaultLightLevel;
		name					= theName;
		photographPath			= thePhotographPath;			// 19/09/2016 ECU added
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public int compareTo(Liquid liquidItem) 
	{		
		return this.name.compareToIgnoreCase(liquidItem.name);
	}
	// =============================================================================
	float getLightLevel (float theDefaultLightLevel)
	{
		return ambientLightLevelScaled * theDefaultLightLevel;
	}
	// =============================================================================
	public String Print ()
	{
		// -------------------------------------------------------------------------
		return "Liquid Name : " + name + "\n" +
			   "Scaled Light Level : " + ambientLightLevelScaled + "\n" +
				"Actions : " + actions;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

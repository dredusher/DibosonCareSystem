package com.usher.diboson;

public class Validation 
{
	// =============================================================================
	// 25/08/2015 ECU created to include any validation checks used by GridActivity
	// =============================================================================
	
	// =============================================================================
	public static boolean torchValidation (int theArgument)
	{
		// -------------------------------------------------------------------------
		// 25/08/2015 ECU created to indicate if device has flash LED
		// 29/12/2016 ECU changed from Utilities.
		// -------------------------------------------------------------------------
		return FlashLight.flashLightCheck ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}
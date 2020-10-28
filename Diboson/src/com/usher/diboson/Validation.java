package com.usher.diboson;

public class Validation 
{
	// =============================================================================
	// 25/08/2015 ECU created to include any validation checks used by GridActivity
	// =============================================================================
	
	// =============================================================================
	public static boolean musicPlayerValidation ()
	{
		// -------------------------------------------------------------------------
		// 02/06/2017 ECU check if any actions are being processed
		// -------------------------------------------------------------------------
		return ((PublicData.actions == null) || (PublicData.actions.size() == 0));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean torchValidation (int theArgument)
	{
		// -------------------------------------------------------------------------
		// 25/08/2015 ECU created to indicate if device has flash LED
		// 29/12/2016 ECU changed from Utilities.
		// 14/06/2019 ECU added the 'false' to indicate not to release the back camera
		// -------------------------------------------------------------------------
		return FlashLight.flashLightCheck (false);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

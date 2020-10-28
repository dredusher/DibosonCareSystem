package com.usher.diboson;

public class BloodPressureAndWeight 
{
	// -----------------------------------------------------------------------------
	float   kilograms;
	int		pounds;
	int		stones;
	// -----------------------------------------------------------------------------
	// 27/08/2017 ECI if 'diastolic' is set to null then there are no readings
	// -----------------------------------------------------------------------------
	int		diastolic [];
	int		heartRate [];
	int		systolic  [];
	// =============================================================================
	public BloodPressureAndWeight ()
	{
		diastolic = new int [BloodPressureActivity.NUMBER_OF_READINGS];
		heartRate = new int [BloodPressureActivity.NUMBER_OF_READINGS];
		systolic  = new int [BloodPressureActivity.NUMBER_OF_READINGS];
		// -------------------------------------------------------------------------
		// 27/08/2017 ECU if the 'stones' is NOT_SET then weight not set yet
		// -------------------------------------------------------------------------
		pounds	=	0;
		stones 	= 	StaticData.NOT_SET;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void setWeight (int theStones,int thePounds)
	{
		pounds	= thePounds;
		// -------------------------------------------------------------------------
		// 11/09/2017 ECU check if the stones is currently not set - if so then set
		//                to 0
		// -------------------------------------------------------------------------
		if (theStones == StaticData.NOT_SET)
			stones = 0;
		else
			stones	= theStones;
		// -------------------------------------------------------------------------
		kilograms = (float)((stones * 14) + pounds) / StaticData.POUNDS_PER_KILO;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void setWeight (float theKilograms)
	{
		// -------------------------------------------------------------------------
		kilograms = theKilograms;
		
		float localPounds = theKilograms * StaticData.POUNDS_PER_KILO;
		stones = (int) (localPounds / 14.0f);
		pounds = Math.round (localPounds - (stones * 14.0f));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	
	// =============================================================================
	String Print ()
	{
		String printString = StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
		// 27/08/2017 ECU print out the blood pressure and heart rate
		// -------------------------------------------------------------------------
		for (int index = 0; index < BloodPressureActivity.NUMBER_OF_READINGS; index++)
		{
			// ---------------------------------------------------------------------
			// 27/08/2017 ECU check if there are any pressure readings - if not then 
			//                pad the string
			// ---------------------------------------------------------------------
			if (diastolic != null)
				printString += String.format("%3d/%-3d (%3d)  ", systolic [index],diastolic [index], heartRate [index]);
			else
				printString += "               ";
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 27/08/2017 ECU now add the weight
		//            ECU check if a weight has been supplied
		// -------------------------------------------------------------------------
		if (stones != StaticData.NOT_SET)
			printString += String.format("%3d:%02d  %3.1f", stones,pounds,kilograms);
		// -------------------------------------------------------------------------
		return printString;
	}
	// =============================================================================
	boolean pressureSupplied ()
	{
		// -------------------------------------------------------------------------
		// 28/08/2017 ECU created to indicate whether the blood pressure has been
		//                supplied
		// -------------------------------------------------------------------------
		return !(diastolic == null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

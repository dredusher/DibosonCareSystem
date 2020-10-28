package com.usher.diboson;

import android.widget.Toast;

public class InfraredCodes 
{
	/* ============================================================================= */
	int	  				  type;
	public 		IRCode [] codes;
	public      String    description;				// 21/12/2013 ECU added
	/* ============================================================================= */
	public InfraredCodes (int theType,IRCode [] theCodes,String theDescription)
	{
		// -------------------------------------------------------------------------
		// 21/12/2013 ECU added description
		// -------------------------------------------------------------------------
		codes		 = theCodes;
		description  = theDescription;
		type 		 = theType;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public long ReturnTheCode (int theFunction,boolean theDisplayFlag)
	{
		// -------------------------------------------------------------------------
		// 01/03/2014 ECU added 'theDisplayFlag' to give the option of displaying
		//                the retrieved function code
		// -------------------------------------------------------------------------
		
		for (int theIndex = 0; theIndex < codes.length; theIndex++)
		{
			// 20/12/2013 ECU change because IRCode has changed
			
			if (codes [theIndex].function.function == theFunction)
			{
				// 19/12/2013 ECU print the code retrieved
				// 01/03/2014 ECU include the option to display the retrieved code
				
				if (theDisplayFlag)
					Utilities.popToast (String.format ("InfraredCodes\nFunction = %d\nReturned Code : 0x%x",theFunction,codes [theIndex].code),true,Toast.LENGTH_SHORT);
				
				return codes [theIndex].code;
			}
		}
		
		// 31/12/2013 ECU indicate no code found
		// 01/03/2014 ECU include the option to display error message
		
		if (theDisplayFlag)
			Utilities.popToast ("no code found for " + theFunction);
		
		// 17/12/2013 ECU no valid function found
		
		return Television.IR_NO_CODE;
	}
	/* ============================================================================= */
	public long ReturnTheCode (int theFunction)
	{
		// -------------------------------------------------------------------------
		// 01/03/2014 ECU modified so that it calls the new method but indicates that
		//                it does not wish to display the retrieved code
		// -------------------------------------------------------------------------
		
		return ReturnTheCode (theFunction,false);
	}
	/* ============================================================================= */
	public int ReturnTheType ()
	{
		return type;
	}
	/* ============================================================================= */
	public void Print ()
	{
		for (int theIndex = 0; theIndex < codes.length; theIndex++)
		{
			// ---------------------------------------------------------------------
			// 05/07/2020 ECU changed first parameter to be '.function'
			// ---------------------------------------------------------------------
			String localString = String.format("%d %x", codes[theIndex].function.function,codes[theIndex].code);
			// ---------------------------------------------------------------------
			// 18/03/2017 ECU changed for the log entry
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile ("InfraredCodes",localString);
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
}

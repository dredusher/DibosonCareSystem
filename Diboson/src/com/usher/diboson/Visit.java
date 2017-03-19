package com.usher.diboson;

import java.io.Serializable;

public class Visit implements Serializable
{
	// =============================================================================
	// 11/01/2014 ECU created to contain details of a carer
	// 31/01/2016 ECU changed the visitor details from the name to the index to the
	//                carer object
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	public 	long	start;			// start of visit
	public 	long	end;			// end of visit
	public	int		visitorIndex;	// name of the visitor	
	// =============================================================================
	public Visit (int theVisitorIndex,long theStart,long theEnd)
	{
		// -------------------------------------------------------------------------
		// 11/01/2014 ECU copy across the variables
		// 31/01/2016 ECU changed to use the carer index
		// -------------------------------------------------------------------------
		visitorIndex		= theVisitorIndex;
		// -------------------------------------------------------------------------
		// 29/01/2016 ECU store the information to nearest second
		// -------------------------------------------------------------------------
		start		= ((theStart + 500) / 1000) * 1000;
		end			= ((theEnd   + 500) / 1000) * 1000;
		// -------------------------------------------------------------------------
		// 11/01/2014 ECU put in actions when a new visit is added
		// -------------------------------------------------------------------------
		LogTheVisit ();	
		// -------------------------------------------------------------------------
		// 11/01/2014 ECU send an email to confirm the visit
		// -------------------------------------------------------------------------	
	}
	// =============================================================================
	void LogTheVisit ()
	{
		// -------------------------------------------------------------------------
		// 11/01/2014 ECU log the new visit
		// -------------------------------------------------------------------------
	    CarerActivity.LogTheData (Print());
	}
	// =============================================================================
	public String Print ()
	{
		return  "Visitor : " 	+ PublicData.carers.get (visitorIndex).name + 
				" Visit : " 	+ PublicData.dateFormatterFull.format (start) + 
   		     	" to " 			+  PublicData.dateFormatterFull.format (end);
	}
	// -----------------------------------------------------------------------------
	public String Print (boolean formatFlag)
	{
		// -------------------------------------------------------------------------
		// 29/01/2016 ECU change the order and work out the duration
		// 31/01/2016 ECU changed the visitor name to get using the index
		// 31/03/2016 ECU decide whether to add 's' for minutes and seconds when
		//                generating the display string
		//            ECU changed to use the method to optionally add a trailing 's'
		// -------------------------------------------------------------------------
		long localDuration	= (end - start);
		long localMinutes 	= (localDuration / StaticData.MILLISECONDS_PER_MINUTE);
		long localSeconds 	= (localDuration - (localMinutes * StaticData.MILLISECONDS_PER_MINUTE)) / 1000;
		// -------------------------------------------------------------------------
		return  "Visit Started  : " + PublicData.dateFormatterFull.format (start) +"\n" +
   		        "Visit Ended    : " + PublicData.dateFormatterFull.format (end) + "\n" +
				"Visit Duration : " + localMinutes + " min" + Utilities.AddAnS (localMinutes) + " " +
   		                              localSeconds + " sec" + Utilities.AddAnS (localSeconds) + "\n" +
   		        "Visitor        : " + PublicData.carers.get (visitorIndex).name +
		// -------------------------------------------------------------------------
		// 05/10/2016 ECU add the tasks performed
		// -------------------------------------------------------------------------
   		     PublicData.carers.get (visitorIndex).TasksPerformed ("Tasks Done     : ");
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String PrintAll ()
	{
		// -------------------------------------------------------------------------
		// 27/08/2015 ECU created to return all of the entries currently stored in the
		//                'Visits' object
		// -------------------------------------------------------------------------
		String returnString = "";
		
		for (int theVisit = 0; theVisit < PublicData.visits.size(); theVisit++)
		{
			returnString += PublicData.visits.get (theVisit).Print (true) + 
								"\n====================================\n";
		}
		// -------------------------------------------------------------------------
		// 27/08/2015 ECU return the total string
		// -------------------------------------------------------------------------
		return returnString;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}
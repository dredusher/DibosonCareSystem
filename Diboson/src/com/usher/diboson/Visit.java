package com.usher.diboson;

import android.content.Context;

import com.usher.diboson.utilities.PrintFormatting;

import java.io.Serializable;

public class Visit implements Serializable
{
	// =============================================================================
	// 11/01/2014 ECU created to contain details of a carer
	// 31/01/2016 ECU changed the visitor details from the name to the index to the
	//                carer object
	// 15/07/2017 ECU I M P O R T A N T
	//				  =================
	//  			  Issues were highlighted when deleting carer details in an
	//                uncontrolled way (during testing) which meant that storing the
	//                carer index was invalidated if the carer was deleted. Rather
	//                than storing the visits as a list in memory and copying this
	//                to disk was changed so that the full details of a visit are
	//                written to disk and nothing is held in memory - the list was
	//                never used other than displaying it
	// 16/07/2017 ECU added manualFlag to indicate why the visit was generate
	// 				      manualFlag ..... true    carer has manually clicked
	//                                     false   have used the carer's bluetooth
	//            ECU added the context as an argument but no need to store it
	//            ECU have a good tidy up to use resource strings rather than
	//                literals
	// =============================================================================
	
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	public 	long	start;			// start of visit
	public 	long	end;			// end of visit
		    boolean	manualFlag;		// how visit created
	public	int		visitorIndex;	// name of the visitor	
	// =============================================================================
	public Visit (Context theContext,int theVisitorIndex,long theStart,long theEnd,boolean theManualFlag)
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
		// 16/07/2017 ECU remember how the visit was created
		// -------------------------------------------------------------------------
		manualFlag	= theManualFlag;
		// -------------------------------------------------------------------------
		// 11/01/2014 ECU put in actions when a new visit is added
		// 15/07/2017 ECU Note - firstly log the times of a carer visit (no details)
		// 16/07/2017 ECU pass through the context as an argument
		// -------------------------------------------------------------------------
		LogTheVisit (theContext);	
		// -------------------------------------------------------------------------
		// 15/07/2017 ECU log the full details of the log
		// 16/07/2017 ECU pass through the context as an argument
		// -------------------------------------------------------------------------
		LogTheVisitFull (theContext); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void LogTheVisit (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 11/01/2014 ECU log the new visit
		// 16/07/2017 ECU changed to use the more general method rather than an old
		//                method that used to be in CarerActivity
		//            ECU add the context as an argument
		// -------------------------------------------------------------------------
		Utilities.AppendToFile (PublicData.carerLogFile,StaticData.NEWLINE + StaticData.SEPARATOR  + Print (theContext));
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
	void LogTheVisitFull (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 15/07/2017 ECU created to log all of the details into the 'visit log'
		// 16/07/2017 ECU add the context as an argument
		// -------------------------------------------------------------------------
		Utilities.AppendToFile (PublicData.visitLogFile,StaticData.NEWLINE + StaticData.SEPARATOR  + Print (theContext,true));
		// -------------------------------------------------------------------------
		// 15/07/2017 ECU indicate that the file is to be synchronised with other
		//                devices
		// -------------------------------------------------------------------------
		FileToSynchronise.Add (PublicData.visitLogFile);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 16/07/2017 ECU print an abbreviated summary of the visit and tidied the
		//                layout out
		//            ECU pass through the context as an argument
		// -------------------------------------------------------------------------
		return  "Visitor : " 	+ PublicData.carers.get (visitorIndex).name 	+ StaticData.NEWLINE +
				"  Visit : " 	+ PublicData.dateFormatterFull.format (start) 	+ StaticData.NEWLINE +
   		     	"     to   " 	+ PublicData.dateFormatterFull.format (end);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public String Print (Context theContext,boolean formatFlag)
	{
		// -------------------------------------------------------------------------
		// 29/01/2016 ECU change the order and work out the duration
		// 31/01/2016 ECU changed the visitor name to get using the index
		// 31/03/2016 ECU decide whether to add 's' for minutes and seconds when
		//                generating the display string
		//            ECU changed to use the method to optionally add a trailing 's'
		// 16/07/2017 ECU pass through the context as an argument
		// 07/07/2020 ECU with the duration take account of hours
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			return  visitLegend (theContext,R.string.visit_started) + 
							PublicData.dateFormatterFull.format (start) 			+ StaticData.NEWLINE +
					visitLegend (theContext,R.string.visit_ended) + 
							PublicData.dateFormatterFull.format (end) 				+ StaticData.NEWLINE +
					visitLegend (theContext,R.string.visit_duration) + 
							visitDurationMessage (end - start) + StaticData.NEWLINE +
					visitLegend (theContext,R.string.visit_visitor) + 
							PublicData.carers.get (visitorIndex).name 				+ StaticData.NEWLINE +
			// ---------------------------------------------------------------------
			// 16/07/2017 ECU indicate how the visit was created
			// ---------------------------------------------------------------------
					visitLegend (theContext,R.string.visit_created) + theContext.getString	(manualFlag ? R.string.visit_manually 
																										: R.string.visit_bluetooth) +
			// ---------------------------------------------------------------------
			// 05/10/2016 ECU add the tasks performed
			// 16/07/2017 ECU changed to use the method to set the header
			// ---------------------------------------------------------------------
					PublicData.carers.get (visitorIndex).TasksPerformed (visitLegend (theContext,R.string.visit_tasks));
			// ---------------------------------------------------------------------
		}
		catch (Exception localException)
		{
			// ---------------------------------------------------------------------
			// 14/07/2017 ECU return the exception to help in debugging
			// ---------------------------------------------------------------------
			return localException.toString();
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	String visitDurationMessage (long theDuration)
	{
		// -------------------------------------------------------------------------
		// 07/07/2020 ECU generate the message to display the duration for this visit
		// 08/07/2020 ECU change the way of generating the components
		// 10/07/2020 ECU cast to 'int'
		// -------------------------------------------------------------------------
		int localSeconds = (int) ((theDuration / StaticData.MILLISECONDS_PER_SECOND) % 60);
		int localMinutes = (int) ((theDuration / StaticData.MILLISECONDS_PER_MINUTE) % 60);
		int localHours   = (int) (theDuration / StaticData.MILLISECONDS_PER_HOUR);
		// -------------------------------------------------------------------------
		// 07/07/2020 ECU have the components so generate the message
		// 10/07/2020 ECU change to use PrintFormmating
		// -------------------------------------------------------------------------
		return  ((localHours > 0)   ? PrintFormatting.NumberWithTail (localHours,"hour") + StaticData.SPACE_STRING
		                            : StaticData.BLANK_STRING) +
				((localMinutes > 0) ? PrintFormatting.NumberWithTail (localMinutes,"min")  + StaticData.SPACE_STRING
									: StaticData.BLANK_STRING) +
			      PrintFormatting.NumberWithTail (localSeconds,"sec");
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	String visitLegend (Context theContext,int theLegendID)
	{
		// -------------------------------------------------------------------------
		// 16/07/2017 ECU created to handle the formatting of the visit legends
		//            ECU changed to use the ID rather than pass a string
		// -------------------------------------------------------------------------
		return String.format (theContext.getString(R.string.visit_format),theContext.getString (theLegendID));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}
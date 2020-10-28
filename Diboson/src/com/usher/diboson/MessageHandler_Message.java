package com.usher.diboson;

import java.io.Serializable;
import java.lang.reflect.Method;

public class MessageHandler_Message implements Serializable
{
	// =============================================================================
	// 17/06/2016 ECU created for passing data and a 'processing' method via the
	//                MessageHandler
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	public String			   deviceIPAddress;
	public MethodDefinition<?> processorMethodDefinition 	= null;
	public int				   timeout						= StaticData.NFC_TIMEOUT;
	// =============================================================================
	
	// =============================================================================
	public MessageHandler_Message (String theDeviceIPAddress,MethodDefinition<?> theProcessorMethodDefinition,int theTimeout)
	{
		// -------------------------------------------------------------------------
		// 17/06/2016 ECU copy across the variables
		// -------------------------------------------------------------------------
		deviceIPAddress				= theDeviceIPAddress;
		processorMethodDefinition   = theProcessorMethodDefinition;
		timeout					    = theTimeout;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void process (boolean theResult)
	{
		// -------------------------------------------------------------------------
		// 17/06/2016 ECU check if a method definition has been supplied and then
		//                call it with the appropriate argument
		//            ECU NOTE - ideally would like to pass 'theResult' as a boolean
		//                       to the method being invoked but have had some problems
		//                       getting it to work.
		// -------------------------------------------------------------------------
		if (processorMethodDefinition != null)
		{
			Method processor = processorMethodDefinition.ReturnMethod (0);
			
			if (processor != null)
	    	{
	    		try 
	    		{
	    			// -------------------------------------------------------------
	    			// 28/02/2015 ECU action the defined method
	    			// -------------------------------------------------------------
					processor.invoke (null, new Object [] {(theResult ? 0 : StaticData.NO_RESULT)});
					// -------------------------------------------------------------
				} 
	    		catch (Exception theException) 
	    		{
				} 
	    	}
		}
	}
	// =============================================================================
}

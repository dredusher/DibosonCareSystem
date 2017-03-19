package com.usher.diboson;

import java.io.Serializable;
import java.lang.reflect.Method;

public class MethodDefinition <T> implements Serializable
{	/* ============================================================================= */
	// 25/03/2014 ECU created to provide an easy mechanism for passing details of
	//                a Method which will be invoked sometime later
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	Class <T>	className;
	String		methodName;
	/* ============================================================================= */
	public MethodDefinition (Class <T> theClassName,String theMethodName)
	{
		// -------------------------------------------------------------------------
		// 25/03/2014 ECU copy across the input arguments
		// -------------------------------------------------------------------------
		className	= theClassName;
		methodName	= theMethodName;
	}
	/* ============================================================================= */
	public Method ReturnMethod ()
	{
		// -------------------------------------------------------------------------
		// 19/12/2015 ECU modified to create a method with no arguments
		// -------------------------------------------------------------------------
		return Utilities.createAMethod (className,methodName);
	}
	// -----------------------------------------------------------------------------
	public Method ReturnMethod (int theInteger)
	{
		// -------------------------------------------------------------------------
		// 25/03/2014 ECU used the stored variables to create a Method that will be
		//                returned 
		// 17/12/2015 ECU note - this method should only be used when the method
		//                being created has an integer argument.
		// -------------------------------------------------------------------------
		return Utilities.createAMethod (className,methodName,0);
	}
	// -----------------------------------------------------------------------------
	public Method ReturnMethod (String theString)
	{
		// -------------------------------------------------------------------------
		// 17/12/2015 ECU created this method with a string argument for use with
		//                those methods which will be called with that argument
		//                like CarerActivity and MedicationInput
		// -------------------------------------------------------------------------
		return Utilities.createAMethod (className,methodName,theString);
	}
	// -----------------------------------------------------------------------------
	public Method ReturnMethod (Object [] theObjects)
	{
		// -------------------------------------------------------------------------
		// 23/10/2016 ECU created to pass through an array of objects
		// -------------------------------------------------------------------------
		return Utilities.createAMethod (className,methodName,theObjects);
	}
	/* ============================================================================= */
}

package com.usher.diboson;

import java.io.Serializable;

public class IntentData implements Serializable
{
	// -----------------------------------------------------------------------------
	// 16/10/2014 ECU wanted to define the Method here but unfortunately it is not
	//                serializable
	//			  ECU added the 'restartApp' flag so that when the result of an
	//                activity is recieved then determine whether the app is to be
	//                restarted
	// -----------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;
	// -----------------------------------------------------------------------------
	public String		intentActivity;
	public String		intentMethodName;
	public int			intentResultCode;
	public Class <?>	intentClass;
	// -----------------------------------------------------------------------------
	public IntentData (String theIntentActivity,int theIntentResultCode,Class <?> theIntentClass,String theIntentMethodName)
	{
		intentActivity 		= theIntentActivity;
		intentClass 		= theIntentClass;
		intentMethodName	= theIntentMethodName;
		intentResultCode	= theIntentResultCode;
	}
	// -----------------------------------------------------------------------------
}

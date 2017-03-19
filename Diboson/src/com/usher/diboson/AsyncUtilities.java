package com.usher.diboson;

import android.content.Context;

public class AsyncUtilities 
{
	/* ============================================================================= */
	// 03/04/2014 ECU created to provide methods, that normally exist in Utilities
	//                to be run from within an Async Task
	//            ECU put in the '.get' because do not want the caller to proceed
	//                until the async task has completed - want to investigate the
	//                need for .get on writing
	/* ============================================================================= */
	//private static final String TAG = "AsyncUtilities";
	/* ============================================================================= */
	// =============================================================================
	public static Object readObjectFromDisk (Context theContext,String theFileName)
	{
		Object localObject;

		try 
		{
			localObject = new AsyncClass (Utilities.createAMethod (Utilities.class,"readObjectFromDisk",theContext,""),
					theContext,theFileName).execute().get();
		} 
		catch (Exception theException) 
		{
			localObject = null;
		} 
		
		return localObject;
	}
	/* ============================================================================= */
	public static boolean writeObjectToDisk (String theFileName,Object theObject)
	{
		try
		{
			new AsyncClass (Utilities.createAMethod (Utilities.class, "writeObjectToDisk","",(Object)""),
					theFileName,theObject).execute().get();
	
			return true;
		}
		catch (Exception theException)
		{
			return false;
		}
	}
	// =============================================================================
}

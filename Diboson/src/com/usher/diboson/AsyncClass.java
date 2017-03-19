package com.usher.diboson;

import java.lang.reflect.Method;

import android.content.Context;
import android.os.AsyncTask;

class AsyncClass extends AsyncTask <Void, Integer, Object>
{
	/* ============================================================================= */
	//private static final String TAG = "AsyncClass";
	/* ============================================================================= */
	private static final int TYPE_READ_FROM_DISK	= 0;
	private static final int TYPE_WRITE_TO_DISK		= 1;
	/* ============================================================================= */
	Context				context;
	String 				fileName;
	Method				method;
	Object				object;
	int     			type;
	/* ============================================================================= */
	public AsyncClass (Method theMethod,String theFileName,Object theObject)
	{
		type		= TYPE_WRITE_TO_DISK;
		fileName 	= theFileName;
		method 		= theMethod;
		object 		= theObject;
	}
	/* ----------------------------------------------------------------------------- */
	public AsyncClass (Method theMethod,Context theContext,String theFileName)
	{
		type		= TYPE_READ_FROM_DISK;
		context		= theContext;
		fileName 	= theFileName;
		method 		= theMethod;
	}
	/* ============================================================================= */	
    protected void onPreExecute ()
    {
        
    }
    /* ============================================================================= */	
    protected Object doInBackground(Void...arg0) 
    {
        try 
		{
        	switch (type)
        	{
        	// ---------------------------------------------------------------------
        		case TYPE_READ_FROM_DISK:
      				object =  method.invoke (null, new Object [] {context,fileName});
      				break;
      			// ---------------------------------------------------------------------
        		case TYPE_WRITE_TO_DISK:
        			method.invoke (null, new Object [] {fileName,object});
      				break;
      			// ---------------------------------------------------------------------
        	}    				
		}
		catch (Exception theException) 
		{
		}

        return object;
    }
    /* ============================================================================= */	
    protected void onProgressUpdate(Integer...theArguments)
    {
        
    }
    /* ============================================================================= */	
    protected void onPostExecute(Object theObject) 
    {
    	
    }
    /* ============================================================================= */  
}

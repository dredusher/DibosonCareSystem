package com.usher.diboson;

import android.content.Context;
import android.os.AsyncTask;

class AsyncDisplayImage extends AsyncTask<Void, Void, Void>
{
	Context 		context;
	int				drawableResourceID;
	/* ============================================================================= */
	public AsyncDisplayImage (Context theContext,int theDrawableID)
	{
		context		 		= theContext;
		drawableResourceID	= theDrawableID;
	}
	/* ============================================================================= */	
    protected void onPreExecute ()
    {
        
    }
    /* ============================================================================= */	
    protected Void doInBackground(Void...arg0) 
    {
    	
		Utilities.DisplayADrawable(context,drawableResourceID, 0, false);
		
    	return null;
    }
    /* ============================================================================= */	
    protected void onProgressUpdate(Integer...theArguments)
    {
    }
    /* ============================================================================= */	
    protected void onPostExecute(Void theVoid) 
    {
    	//Utilities.DisplayADrawable(context,drawableResourceID, 0, false);
    }
    /* ============================================================================= */  
}

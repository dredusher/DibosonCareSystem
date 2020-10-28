package com.usher.diboson;

import java.lang.reflect.Method;
import android.os.Bundle;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.view.Menu;

public class RunActivity extends Activity 
{
	// =============================================================================
	// 29/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// =============================================================================
	
	// -----------------------------------------------------------------------------
	final static String TAG = "RunActivity";
	// -----------------------------------------------------------------------------
	IntentData	intentData;
	Method		requestCodeMethod;
	// -----------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 15/10/2014 ECU do not want to change the layout
			// ---------------------------------------------------------------------
			//setContentView(R.layout.activity_run_activity);
			// ---------------------------------------------------------------------
			Bundle receivedData = getIntent().getExtras();
			intentData = (IntentData) receivedData.getSerializable (StaticData.PARAMETER_INTENT_DATA);
				
			requestCodeMethod 	= Utilities.createAMethod(intentData.intentClass,intentData.intentMethodName,StaticData.BLANK_STRING);
			// ---------------------------------------------------------------------
			// 15/10/2014 ECU now activity the required activity
			// ---------------------------------------------------------------------
			Intent localIntent = new Intent ();
			localIntent.setComponent(new ComponentName(getPackageName(),getPackageName() + "." + intentData.intentActivity));
			startActivityForResult (localIntent,intentData.intentResultCode);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// -----------------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return true;
	}
	// -----------------------------------------------------------------------------
	@Override
	public void onActivityResult(int theRequestCode, int theResultCode, Intent theIntent) 
	{
		// 30/08/2013 ECU called when an activity returns a result
		
	    if (theRequestCode == intentData.intentResultCode)
	    {
	    	if (theResultCode == StaticData.RESULT_CODE_FINISH)
	    	{
	    		try 
	    		{
	    			Utilities.LogToProjectFile (TAG,"onActivityResult - OK");
	    			// -------------------------------------------------------------
	    			// 16/10/2014 ECU now invoke the method on success
	    			// -------------------------------------------------------------
	    			requestCodeMethod.invoke(null, new Object [] {"successful completion of " + intentData.intentActivity});
	    			// -------------------------------------------------------------
	    			// 17/10/2014 ECU set the result for the calling activity
	    			// -------------------------------------------------------------
	    			setResult (StaticData.RESULT_CODE_FINISH);
	    			// -------------------------------------------------------------
	    			finish ();
	    		} 
	    		catch (Exception theException) 
	    		{
	    		} 
	    	}
	    	else
	    	if (theResultCode == RESULT_CANCELED)
	    	{
	    		Utilities.LogToProjectFile (TAG,intentData.intentActivity + "was cancelled");
	    		// -----------------------------------------------------------------
	    		finish ();
	    	}
	    } 
	    // -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
}

package com.usher.diboson;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

public class RuntimePermissions 
{

	// =============================================================================
	// 09/11/2016 ECU as of API 23 the way that runtime permissions changed. Rather
	//                than just taking the values in the manifest the settings
	//                are enabled in the settings part of the 'app manager' on
	//                the device, i.e. where you go when the 'Settings' app is run 
	//                and the 'Apps' option selected.
	// =============================================================================
	
	// =============================================================================
	@SuppressLint("InlinedApi")
	// -----------------------------------------------------------------------------
	// 09/11/2016 ECU suppress added because READ_... intoduced at API 16
	// -----------------------------------------------------------------------------
	// 09/11/2016 ECU the following array contains the permissions to be checked -
	//                there are more than this, dealing with phone, location, SMS,
	//                microphone, etc., but just want to trigger a prompt to get
	//                the permissions set via 'Settings' to be set
	// -----------------------------------------------------------------------------
	private static final String [] permissionsToCheck 
		= new String [] {
							Manifest.permission.READ_EXTERNAL_STORAGE,		
							Manifest.permission.WRITE_EXTERNAL_STORAGE
						};
	// =============================================================================
	
	// =============================================================================
	public static boolean check (Activity theActivity)
	{
		// -------------------------------------------------------------------------
		// 09/11/2016 ECU only do real checking if the installed API >= Marshmallow
		// -------------------------------------------------------------------------
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			// ---------------------------------------------------------------------
			// 09/11/2016 ECU loop through all of the permissions of interest
			// ---------------------------------------------------------------------
			for (int index = 0; index < permissionsToCheck.length; index++)
			{
				// -----------------------------------------------------------------
				// 09/11/2016 ECU if a permission is denied then return immediately
				// -----------------------------------------------------------------
				if (!checkPermission (theActivity,permissionsToCheck [index]))
				{
					return false;
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 09/11/2016 ECU all of the permissions seem OK so indicate the fact
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 09/11/2016 ECU for all other API's just return true
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	
	// =============================================================================
	@TargetApi(23) 
	static boolean checkPermission (Activity theActivity,String thePermission)
	{
		// -------------------------------------------------------------------------
		// 09/11/2016 ECU called to check the state of a specified permission
		// -------------------------------------------------------------------------
		return (theActivity.checkSelfPermission (thePermission) == PackageManager.PERMISSION_GRANTED);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@TargetApi(23) 
	public static void enablePermission (Activity theActivity,String thePermission)
	{
		// -------------------------------------------------------------------------
		// 10/11/2016 ECU created to request the permission 
		//                
		//                This is not the best way to handle it because it should
		//                be done asynchronously and use a callback but am not 
		//                expecting the app to run.
		//
		//				  Proper code should look like :-
		// 
	    //           		if (theActivity.shouldShowRequestPermissionRationale(thPermission)
	    //            		{
		//						// should do a request asynchronously
		//  		  		} 
		//            		else 
		//            		{
		//	       				theActivity.requestPermissions(new String[]{thePermission},<request code>);
	    //			 		}
	    //
		//          	  The call back would be like :-
		//
		//					@Override
		//					public void onRequestPermissionsResult (int requestCode,String permissions[],int[] grantResults)
		//
		// -------------------------------------------------------------------------
        theActivity.requestPermissions (new String[]{thePermission},StaticData.REQUEST_CODE_SETTINGS);
        // -------------------------------------------------------------------------
	}
	// =============================================================================
}

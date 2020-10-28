package com.usher.diboson;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageInfo;
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
	// 12/11/2019 ECU tidy up so that the user is correctly asked to grant the
	//                runtime permissions that this app requires.
	// =============================================================================
	
	// =============================================================================
	@SuppressLint ("InlinedApi")
	// -----------------------------------------------------------------------------
	// 09/11/2016 ECU suppress added because READ_... introduced at API 16
	// -----------------------------------------------------------------------------
	// 09/11/2016 ECU the following array contains the permissions to be checked -
	//                there are more than this, dealing with phone, location, SMS,
	//                microphone, etc., but just want to trigger a prompt to get
	//                the permissions set via 'Settings' to be set
	// 12/11/2019 ECU update the permissions to be checked
	//            ECU wanted to check all of the permissions returned by 'getPermissions'
	//                but got a problem with "android.permission.READ_LOGS" so leave as is
	// 13/11/2019 ECU the same problem happens with "android.permission.WRITE_SETTINGS"
	//                see raw/documentation_notes
	//            ECU in the manifest then comment out any permission which is only
	//                relevant 'system apps' which this isn't
	// -----------------------------------------------------------------------------
	//					private final static String [] permissionsToCheck
	//								= new String [] {
	//									// -----------------------------------------------------
	//									"android.permission.CAMERA",
	//									"android.permission.READ_CONTACTS",
	//									"android.permission.ACCESS_FINE_LOCATION",
	//									"android.permission.RECORD_AUDIO",
	//									"android.permission.CALL_PHONE",
	//									"android.permission.RECEIVE_SMS",
	//									"android.permission.WRITE_EXTERNAL_STORAGE"
	//									// -----------------------------------------------------
	//												};
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
			// 13/11/2019 ECU change to check all permissions in the manifest -
			//                have made sure that no 'system app' permissions are
			//                included in the manifest
			// ---------------------------------------------------------------------
			for (String permission : getPermissions (theActivity))
			{
				// -----------------------------------------------------------------
				// 09/11/2016 ECU if a permission is denied then return immediately
				// -----------------------------------------------------------------
				if (!checkPermission (theActivity,permission))
				{
					// -------------------------------------------------------------
					// 14/08/2020 ECU check if there are any API issues with this
					//                permission
					// -------------------------------------------------------------
					if (!APIIssues.CheckPermission (permission))
					{
						// ---------------------------------------------------------
						// 14/08/2020 ECU there are no API issues so ask the user if
						//                permission is to be greanted
						// ---------------------------------------------------------
						// 12/11/2019 ECU ask the user to enable grant the permission
						// ---------------------------------------------------------
						enablePermission (theActivity,permission);
						// -------------------------------------------------------------
						return false;
						// -------------------------------------------------------------
					}
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
	static String [] getPermissions (Activity theActivity)
	{
		// -------------------------------------------------------------------------
		// 12/11/2019 ECU get the permissions that have been declared for this package
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 12/11/2019 ECU return the permissions that have been declared for
			//                this package
			// ---------------------------------------------------------------------
			PackageInfo packageInfo = theActivity.getPackageManager().getPackageInfo (theActivity.getPackageName(),PackageManager.GET_PERMISSIONS);
			return packageInfo.requestedPermissions;
			// ---------------------------------------------------------------------
		}
		catch (PackageManager.NameNotFoundException theException)
		{
			// ---------------------------------------------------------------------
			// 12/11/2019 ECU an exception so indicate this to the caller
			// ---------------------------------------------------------------------
			return null;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================


}

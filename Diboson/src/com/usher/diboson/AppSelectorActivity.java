package com.usher.diboson;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import java.util.ArrayList;

// =================================================================================
// AppSelectorActivity
// ===================
// 01/01/2020 ECU Created to act as a 'launcher' for any of the installed apps
// =================================================================================

// =================================================================================
public class AppSelectorActivity extends DibosonActivity
{
    // =============================================================================
    static ArrayList<ApplicationInfo> appList;
    // =============================================================================

    // =============================================================================
    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        // -------------------------------------------------------------------------
        super.onCreate(savedInstanceState);
        // -------------------------------------------------------------------------
        if (savedInstanceState == null)
        {
            // ---------------------------------------------------------------------
            Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
            // ---------------------------------------------------------------------
            // 01/01/2020 ECU set up the display which will be a 'list view'
            // ---------------------------------------------------------------------
            new ListViewSelector (this,
                                  R.layout.apps_row,
                                  Utilities.createAMethod (AppSelectorActivity.class,"PopulateTheList"),
                                  StaticData.SORT_LIST,
                                  Utilities.createAMethod (AppSelectorActivity.class,"SelectApp",0),
                                  StaticData.NO_HANDLING_METHOD,
                                  StaticData.NO_HANDLING_METHOD,
                                  StaticData.BLANK_STRING,
                                  StaticData.NO_HANDLING_METHOD,
                                  StaticData.NO_HANDLING_METHOD,
                                  StaticData.NO_HANDLING_METHOD);
            // ---------------------------------------------------------------------
        }
        else
        {
            // ---------------------------------------------------------------------
            // 01/01/2020 ECU the activity has been recreated after having been
            //                destroyed by the Android OS
            // ---------------------------------------------------------------------
            finish ();
            // ---------------------------------------------------------------------
        }
    }
    // =============================================================================
    @Override
    public void onBackPressed ()
    {
        // -------------------------------------------------------------------------
        // 01/01/2020 ECU just finish this activity
        // -------------------------------------------------------------------------
        finish ();
        // -------------------------------------------------------------------------
        // 01/01/2020 ECU now call the super for this method
        // -------------------------------------------------------------------------
        super.onBackPressed();
        // -------------------------------------------------------------------------
    }
    // =============================================================================

    // =============================================================================
    public ArrayList<ListItem> PopulateTheList ()
    {
        // -------------------------------------------------------------------------
        // 01/01/2020 ECU created to build the list of notifications
        // -------------------------------------------------------------------------
        ArrayList<ListItem> listItems = new ArrayList<ListItem> ();
        // -------------------------------------------------------------------------
        // 01/01/2020 ECU use a dummy listItem so that colour can be set
        // -------------------------------------------------------------------------
        ListItem localListItem;
        // -------------------------------------------------------------------------
        // 01/01/2020 ECU add in the check on size
        // -------------------------------------------------------------------------
        // 01/01/2020 ECU the stored names array has not been set up yet
        // -------------------------------------------------------------------------
        PackageManager packageManager 	= getPackageManager ();
        // -------------------------------------------------------------------------
        // 01/01/2020 ECU check for 'launchable' apps
        // -------------------------------------------------------------------------
        // 01/01/2020 ECU only build the list if not already done
        // -------------------------------------------------------------------------
        if (appList == null)
        {
            // ---------------------------------------------------------------------
            // 01/01/2020 ECU set up the list to receive the app information
            // 02/01/2020 ECU changed to use the new method
            // ---------------------------------------------------------------------
            appList = Utilities.getAppList (this);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 01/01/2020 ECU now build the information for display
        // -------------------------------------------------------------------------
        for (int theIndex = 0; theIndex < appList.size(); theIndex++)
        {
            // ---------------------------------------------------------------------
            // 01/01/2020 ECU create a local object for this notification
            // ---------------------------------------------------------------------
            localListItem = new ListItem (null,
                                          (String) appList.get (theIndex).loadLabel (packageManager).toString (),
                                          StaticData.BLANK_STRING,
                                          appList.get(theIndex).packageName,
                                          theIndex);
            // ---------------------------------------------------------------------
            // 01/01/2020 ECU set the colour for this entry
            //----------------------------------------------------------------------
            localListItem.imageDrawable = Utilities.getApplicationIcon (getBaseContext(),
                                                        appList.get (theIndex).packageName);
            // ---------------------------------------------------------------------
            // 25/11/2018 ECU add the local object into the chain
            // ---------------------------------------------------------------------
            listItems.add (localListItem);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 01/01/2020 ECU return the generated items
        // -------------------------------------------------------------------------
        return listItems;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void SelectApp (int theAppIndex)
    {
        // -------------------------------------------------------------------------
        // 01/01/2020 ECU action the app that has been selected
        // -------------------------------------------------------------------------
        Intent localIntent = getPackageManager ().getLaunchIntentForPackage
                                    (appList.get (theAppIndex).packageName);
        if (localIntent != null)
            startActivity (localIntent);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
}
// =================================================================================

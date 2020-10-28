package com.usher.diboson;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

@SuppressWarnings("deprecation")
public class NavigationDrawerActivity extends DibosonActivity
{
	// =============================================================================
	// Revision History
	// ================
	// 13/01/2015 ECU created
	// 			  ECU voice recognition bits added
	//            ECU it would seem that if the onPostCreate and  
	//				  onConfigurationChanged methods are not included then the drawer
	//                icon (ic_drawer) is not displayed on the action bar and the
	//                'left arrow' will stay displayed
	//            ECU to be able to show the drawer by clicking on the action bar
	//                then need to include the onCreateOptionsMenu and 
	//				  onPrepareOptionsMenu methods
	// 15/01/2015 ECU changed so that the drawer displays the commands in alphabetical
	//                order
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 24/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 05/10/2020 ECU change to use 'Utilities.startASpecifiedActivity' rather than
	//                starting up 'GridActivity.class'
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	// =============================================================================
	
	// =============================================================================
	List<CommandListItem>	commandsList;					// 15/01/2015 ECU added
	ListView				drawerList;
	ActionBarDrawerToggle	drawerToggle;
	DrawerLayout			drawerLayout;
	String [] 				listTitles;
	// =============================================================================
	
	// =============================================================================
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 24/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 28/11/2016 ECU call method to set the screen up
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true,true,false);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_navigation_drawer);
			// ---------------------------------------------------------------------
			drawerLayout 	= (DrawerLayout) findViewById (R.id.drawer_layout);
			drawerList 		= (ListView) 	 findViewById (R.id.left_drawer);
			// ---------------------------------------------------------------------
			// 13/01/2015 ECU set a custom shadow that overlays the main content when 
			//                the drawer opens
			// ---------------------------------------------------------------------
			drawerLayout.setDrawerShadow (R.drawable.drawer_shadow,GravityCompat.START);
			// ---------------------------------------------------------------------
			// 15/01/2015 ECU build up a sorted list of the commands
			// ---------------------------------------------------------------------
			commandsList = WebUtilities.SortedCommands();
			// ---------------------------------------------------------------------
			// 13/01/2015 ECU set up the drawer's list view with items and click listener
			//            ECU the array of strings is generated from the legends used
			//                in the GridActivity
			// 15/01/2015 ECU change to use the sorted array
			// ---------------------------------------------------------------------
			listTitles = new String [commandsList.size()];
			for (int theIndex = 0; theIndex < commandsList.size(); theIndex++)
			{
				listTitles [theIndex] = commandsList.get (theIndex).legend;
			}
			// ---------------------------------------------------------------------
			drawerList.setAdapter (new ArrayAdapter<String>(this,R.layout.drawer_list_item, listTitles));
			// ---------------------------------------------------------------------
			drawerList.setOnItemClickListener (new DrawerItemClickListener());
			// ---------------------------------------------------------------------
			// 13/01/2015 ECU enable ActionBar app icon to behave as action to toggle 
			//                navigation drawer
			// ---------------------------------------------------------------------
			getActionBar().setDisplayHomeAsUpEnabled (true);
			getActionBar().setHomeButtonEnabled (true);
			// ---------------------------------------------------------------------
			// 13/01/2015 ECU ActionBarDrawerToggle ties together the the proper interactions
			// 				  between the sliding drawer and the action bar app icon
			// ---------------------------------------------------------------------
			drawerToggle = new ActionBarDrawerToggle (
											this,                 	// the host Activity 
											drawerLayout,        	// the DrawerLayout object 
											R.drawable.ic_drawer,  	// image to replace 'left' caret 
											R.string.drawer_open,  	// "open drawer" description for accessibility
											R.string.drawer_close  	// "close drawer" description for accessibility
													) 
			{
				// -----------------------------------------------------------------
				public void onDrawerClosed(View theView) 
				{
					// -------------------------------------------------------------
					// 13/01/2015 ECU request a call to onPrepareOptionsMenu
					// -------------------------------------------------------------
					invalidateOptionsMenu (); 
				}
				// -----------------------------------------------------------------
				public void onDrawerOpened(View theDrawerView) 
				{
					// -------------------------------------------------------------
					// 13/01/2015 ECU request a call to onPrepareOptionsMenu
					// -------------------------------------------------------------
					invalidateOptionsMenu ();
				}
			};
			// ---------------------------------------------------------------------
			// 13/01/2015 ECU declare the listener for drawer actions
			// ---------------------------------------------------------------------
			drawerLayout.setDrawerListener (drawerToggle);
        // -------------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 24/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------	
		}
	}
	// =============================================================================
    @Override
    public boolean onPrepareOptionsMenu (Menu theMenu) 
    {
        return super.onPrepareOptionsMenu (theMenu);
    }
    // =============================================================================
    @Override
    public boolean onOptionsItemSelected (MenuItem theMenuItem) 
    {
        if (drawerToggle.onOptionsItemSelected(theMenuItem)) 
        {
            return true;
        }
        // -------------------------------------------------------------------------
        // 13/01/2015 ECU Handle the action buttons
        // -------------------------------------------------------------------------
        return super.onOptionsItemSelected(theMenuItem);
    }
    // =============================================================================
	@Override
	protected void onPostCreate(Bundle savedInstanceState) 
	{
	    super.onPostCreate (savedInstanceState);
	    drawerToggle.syncState ();
	}
	// =============================================================================
	@Override
	public void onConfigurationChanged (Configuration theConfiguration) 
	{
		super.onConfigurationChanged (theConfiguration);
		drawerToggle.onConfigurationChanged (theConfiguration);
	}
	// =============================================================================
    private class DrawerItemClickListener implements ListView.OnItemClickListener 
    {
        @Override
        public void onItemClick(AdapterView<?> theParent, View theView, int thePosition, long theIdentifier) 
        {
        	// ---------------------------------------------------------------------
        	// 13/01/2015 ECU start the activity that has been selected
        	// 15/01/2015 ECU changed to use the 'commandsList' list
        	// ---------------------------------------------------------------------
        	// 05/10/2020 ECU changed from
        	// 					Intent localIntent = new Intent (getBaseContext(),GridActivity.class);
			//					localIntent.putExtra (StaticData.PARAMETER_POSITION,commandsList.get (thePosition).number);
			//					localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//					getBaseContext().startActivity (localIntent);
			// ---------------------------------------------------------------------
			Utilities.startASpecficActivity (commandsList.get (thePosition).number);
			// ---------------------------------------------------------------------
        }
    }
    // =============================================================================
}

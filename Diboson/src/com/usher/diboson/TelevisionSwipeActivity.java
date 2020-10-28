package com.usher.diboson;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

// ==================================================================================
@SuppressWarnings("deprecation")
public class TelevisionSwipeActivity extends FragmentActivity implements ActionBar.TabListener
{
	// =============================================================================
	// 08/05/2015 ECU created
	// 			  ECU handle television with swipe action to select appropriate
	//                adapter
	// 11/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	//==============================================================================
	//private final static String TAG = "TelevisionSwipeActivity";
	// =============================================================================
	private static final String REMOTE_CONTROLLER = "remote_controller";
	// =============================================================================
	
	// =============================================================================
	// 08/05/2015 ECU declare any variables that are needed
	// -----------------------------------------------------------------------------
	SectionsPagerAdapter 	sectionsPagerAdapter;
	ViewPager 				viewPager;
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
			// 11/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 12/05/2015 ECU set up standard features of the activity
			//            ECU tried to use Utilities.SettUpActivity but this caused a NPE
			//            ECU setting to no title also throws a NPE
			// ---------------------------------------------------------------------
			setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			// ---------------------------------------------------------------------
			setContentView(R.layout.activity_television_swipe);
			// ---------------------------------------------------------------------
			// 11/05/2015 ECU initialise the hardware
			// ---------------------------------------------------------------------
			if (Television.InitialiseHardwareInterface (this))
			{
				// -----------------------------------------------------------------
				// 08/05/2015 ECU set up the action bar
				// -----------------------------------------------------------------
				final ActionBar actionBar = getActionBar();
				actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
				// -----------------------------------------------------------------
				// 08/05/2015 ECU create the adapter that will return a fragment for each
				//                primary section of this activity
				// -----------------------------------------------------------------
				sectionsPagerAdapter = new SectionsPagerAdapter (getSupportFragmentManager());
				// -----------------------------------------------------------------
				// 08/05/2015 ECU set up theViewPager with the screens adapter
				// -----------------------------------------------------------------
				viewPager = (ViewPager) findViewById(R.id.pager);
				viewPager.setAdapter(sectionsPagerAdapter);
				// -----------------------------------------------------------------
				// 08/05/2015 ECU set up the listener for when the page changes
				// -----------------------------------------------------------------
				viewPager.setOnPageChangeListener (new ViewPager.SimpleOnPageChangeListener () 
				{
					@Override
					public void onPageSelected (int thePage) 
					{
						actionBar.setSelectedNavigationItem (thePage);
					}
				});
				// -----------------------------------------------------------------
				// 08/05/2015 ECU add a tab for each of the sections in this activity
				// -----------------------------------------------------------------
				for (int theSection = 0; theSection < sectionsPagerAdapter.getCount(); theSection++) 
				{
					// -------------------------------------------------------------
					// 11/05/2015 ECU Create a tab with text corresponding to the page 
					//                title defined by the adapter. Also specify this 
					//                Activity object, which implements the TabListener 
					//                interface, as the callback (listener) for when
					//                this tab is selected.
					// -------------------------------------------------------------
					actionBar.addTab (actionBar.newTab()
										.setText (sectionsPagerAdapter.getPageTitle(theSection))
										.setTabListener (this));
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 15/02/2014 ECU the hardware did not initialise so terminate this 
				//                activity
				// -----------------------------------------------------------------
				
				finish ();
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 11/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
		
	}
	// =============================================================================
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return true;
	}
	// =============================================================================
	@Override
	public void onTabSelected(ActionBar.Tab tab,FragmentTransaction fragmentTransaction) 
	{
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		viewPager.setCurrentItem (tab.getPosition());
		PublicData.currentRemoteController = tab.getPosition();
	}
	//	============================================================================
	@Override
	public void onTabUnselected(ActionBar.Tab tab,FragmentTransaction fragmentTransaction) 
	{
	}
	// =============================================================================
	@Override
	public void onTabReselected(ActionBar.Tab tab,FragmentTransaction fragmentTransaction) 
	{
	}
	// =============================================================================
	public class SectionsPagerAdapter extends FragmentPagerAdapter 
	{
		// -------------------------------------------------------------------------
		public SectionsPagerAdapter (FragmentManager theFragmentManager) 
		{
			super (theFragmentManager);
		}
		// -------------------------------------------------------------------------
		@Override
		public Fragment getItem (int theFragment) 
		{
			// ---------------------------------------------------------------------
			// 08/05/2015 ECU called up to create a fragment for the given page.
			// ---------------------------------------------------------------------
			Fragment fragment = new TelevisionSectionFragment();
			// ---------------------------------------------------------------------
			// 08/05/2015 ECU create the bundle that will contain the argument to
			//                be passed
			// ---------------------------------------------------------------------
			Bundle arguments = new Bundle ();
			arguments.putInt (REMOTE_CONTROLLER,theFragment);
			fragment.setArguments(arguments);
			return fragment;
		}
		// -------------------------------------------------------------------------
		@Override
		public int getCount() 
		{
			// ---------------------------------------------------------------------
			// 08/05/2015 ECU this method returns the number of different remote
			//                controllers
			// ---------------------------------------------------------------------
			return Television.remoteControllers.length;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		@Override
		public CharSequence getPageTitle (int thePage) 
		{
			// ---------------------------------------------------------------------
			// 10/05/2015 ECU return the name of the specified remote controller -
			//                'thePage' starts from 0
			// ---------------------------------------------------------------------
			return Television.remoteControllers [thePage].codes.description;
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static class TelevisionSectionFragment extends Fragment 
	{
		// -------------------------------------------------------------------------
		public TelevisionSectionFragment() 
		{
		}
		// -------------------------------------------------------------------------
		@Override
		public View onCreateView (LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) 
		{
			View rootView = inflater.inflate (R.layout.television, container, false);
			// ----------------------------------------------------------------------
			// 11/05/2015 ECU initialise the display which includes the image
			//                adapters and the listeners
			// 12/05/2015 ECU added the false flag to indicate that the legend
			//                for the controller is not displayed
			// ----------------------------------------------------------------------
			Television.initialiseTheDisplay (getActivity(),rootView,getArguments().getInt (REMOTE_CONTROLLER),false);
			// ----------------------------------------------------------------------
			return rootView;
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

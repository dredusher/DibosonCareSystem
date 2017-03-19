package com.usher.diboson;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NewSystemInfoActivity extends FragmentActivity 
{
	/* ============================================================================= */
	// Revision History
	// ================
	// 22/11/2014 ECU created
	// 29/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	/* ============================================================================= */
	//final static String TAG = "NewSystemInfoActivity";
	/* ============================================================================= */
	
	/* ============================================================================= */
	public static final String	ARGUMENT_SECTION_NUMBER = "section_number";
	/* ============================================================================= */
	static Context					context;	
	SectionsPagerAdapter 			sectionsPagerAdapter;
	ViewPager 						viewPager;
	// =============================================================================
	String [] titles;
	// -----------------------------------------------------------------------------
	public static final int FRAGMENT_SYSTEM_INFO		= 0;
	public static final int FRAGMENT_PROJECT_LOG		= 1;
	public static final int FRAGMENT_LOGCAT				= 2;
	public static final int FRAGMENT_REVERSE_LOGCAT		= 3;
	/* ============================================================================= */
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
			// 22/11/2014 ECU set up standard activity features
			// 03/05/2015 ECU use ACTI.... for clarity
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity(this,StaticData.ACTIVITY_FULL_SCREEN);
			// ---------------------------------------------------------------------
			// 22/11/2014 ECU remember the context
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 22/11/2014 ECU get the titles from the resource file
			// ---------------------------------------------------------------------
			titles = getResources().getStringArray (R.array.fragment_titles); 
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_new_settings);
			// ---------------------------------------------------------------------
			// 22/04/2014 ECU Create the adapter that will return a fragment for each of
			//			      the primary sections of the app.
			// ---------------------------------------------------------------------
			sectionsPagerAdapter = new SectionsPagerAdapter (getSupportFragmentManager());
			// ---------------------------------------------------------------------
			// 22/11/2014 ECU Set up the ViewPager with the sections adapter.
			// ---------------------------------------------------------------------
			viewPager = (ViewPager) findViewById(R.id.pager);
			viewPager.setAdapter(sectionsPagerAdapter);
			// ---------------------------------------------------------------------
			PagerTitleStrip titleStrip = (PagerTitleStrip) findViewById (R.id.pager_title_strip);	
			// ---------------------------------------------------------------------
			// 22/11/2014 ECU set font size depending on stored value
			// ---------------------------------------------------------------------
			titleStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
					(int) getResources().getDimension(R.dimen.default_big_font_size) 
						/ getResources().getDisplayMetrics().density);
			// ---------------------------------------------------------------------
			// 22/11/2014 ECU tell the user to touch the screen for an immediate
			//                refresh
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (getString (R.string.touch_screen_for_refresh),true);
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
	/* ============================================================================= */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return true;
	}
	/* ============================================================================= */
	public class SectionsPagerAdapter extends FragmentPagerAdapter 
	{
		/* ========================================================================= */
		public SectionsPagerAdapter(FragmentManager fragmentManager) 
		{
			super(fragmentManager);
		}
		/* ========================================================================= */
		@Override
		public Fragment getItem(int position) 
		{
			// ---------------------------------------------------------------------
			// 22/04/2014 ECU getItem is called to instantiate the fragment for the 
			//   			  given page. Return a DummySectionFragment (defined as 
			//                a static inner class below) with the page number as 
			// 				  its lone argument. 
			// ---------------------------------------------------------------------
			Fragment fragment	 = new SwipeSectionFragment ();
			Bundle arguments 	 = new Bundle();
			// ---------------------------------------------------------------------
			// 22/11/2014 ECU stored the section number so that it can be picked up
			//                by other methods
			// ---------------------------------------------------------------------
			arguments.putInt (ARGUMENT_SECTION_NUMBER, position);
			fragment.setArguments(arguments);
			// ---------------------------------------------------------------------
			return fragment;
			// ---------------------------------------------------------------------
		}
		/* ========================================================================= */
		@Override
		public int getCount() 
		{
			return titles.length;
		}
		/* ========================================================================= */
		@Override
		public CharSequence getPageTitle(int position) 
		{
			return titles [position];
		}
		/* ========================================================================= */
	}
	/* ============================================================================= */
	public static class SwipeSectionFragment extends Fragment 
	{
		/* ========================================================================= */
		public SwipeSectionFragment() 
		{
		}
		/* ========================================================================= */
		@Override
		public View onCreateView (LayoutInflater inflater,ViewGroup viewGroup,
									Bundle savedInstanceState) 
		{
			// ---------------------------------------------------------------------
			// 22/11/2014 ECU get layout for the display
			// ---------------------------------------------------------------------
			View rootView 
					= inflater.inflate(R.layout.activity_system_info,viewGroup, false);
			// ---------------------------------------------------------------------
			// 22/11/2014 ECU get the TextView where the data will be displayed
			// ---------------------------------------------------------------------
			TextView sectionTextView 
					= (TextView) rootView.findViewById(R.id.system_info_textview);
			sectionTextView.setClickable(true);
			// ---------------------------------------------------------------------
			sectionTextView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick (View view) 
				{	
					// -------------------------------------------------------------
					// 22/11/2014 ECU update the data on the currently clicked view
					// -------------------------------------------------------------
					refreshTextView (getArguments().getInt (ARGUMENT_SECTION_NUMBER),(TextView)view);
					// -------------------------------------------------------------
					// 22/11/2014 ECU tell the user what is going on
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString(R.string.data_refreshed));
					// -------------------------------------------------------------
				}
			});
			// ---------------------------------------------------------------------
			// 22/11/2014 ECU build up the display depending on which fragment is
			//				  being created
			//			  ECU change to call the method rather than having the code
			//				  inline
			// ---------------------------------------------------------------------
			refreshTextView (getArguments().getInt (ARGUMENT_SECTION_NUMBER),sectionTextView);
			// ---------------------------------------------------------------------
			return rootView;
		}
		/* ========================================================================= */
		@Override
		public void onResume() 
		{ 
		   	super.onResume(); 
		}
		// =========================================================================
	}
	/* ============================================================================= */
	
	// =============================================================================
	static void refreshTextView (int theFragmentNumber,TextView theTextView)
	{
		switch (theFragmentNumber)
		{
			// -----------------------------------------------------------------
			case FRAGMENT_SYSTEM_INFO:
				theTextView.setGravity (Gravity.CENTER_HORIZONTAL);
				theTextView.setText (SystemInfoActivity.projectData(context));
				break;
			// -----------------------------------------------------------------
			case FRAGMENT_PROJECT_LOG:
				theTextView.setGravity (Gravity.LEFT);
				theTextView.setText (SystemInfoActivity.projectLogData(context));
				break;
			// -----------------------------------------------------------------
			case FRAGMENT_LOGCAT:
				theTextView.setGravity (Gravity.LEFT);
				theTextView.setText (Utilities.GetLogCatEntries(null));
				break;
			// -----------------------------------------------------------------
			case FRAGMENT_REVERSE_LOGCAT:
				theTextView.setGravity (Gravity.LEFT);
				theTextView.setText (Utilities.GetLogCatEntries(true,null));
				break;
			// -----------------------------------------------------------------
			default:
				break;
			// -----------------------------------------------------------------	
		}		
	}
	// =============================================================================
}

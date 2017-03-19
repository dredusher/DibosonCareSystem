package com.usher.diboson;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AppointmentSwipeActivity extends FragmentActivity 
{
	// =============================================================================
	// 21/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// =============================================================================
	
	// -----------------------------------------------------------------------------
	SectionsPagerAdapter 	sectionsPagerAdapter;
	ViewPager 				viewPager;
	// -----------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 21/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 22/04/2014 ECU set up standard activity features
			// 03/05/2015 ECU changed to use ACT ..... for clarity
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			setContentView (R.layout.activity_appointment_swipe);
			// ---------------------------------------------------------------------
			// Create the adapter that will return a fragment for each of the three
			// primary sections of the app.
			// ---------------------------------------------------------------------
			sectionsPagerAdapter = new SectionsPagerAdapter (getSupportFragmentManager());
			// ---------------------------------------------------------------------
			// Set up the ViewPager with the sections adapter.
			// ---------------------------------------------------------------------
			viewPager = (ViewPager) findViewById (R.id.pager);
			viewPager.setAdapter (sectionsPagerAdapter);
		}
		else
		{
			// ---------------------------------------------------------------------
			// 21/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	@Override
	public boolean onCreateOptionsMenu (Menu menu) 
	{
		return true;
	}
	// =============================================================================
	public class SectionsPagerAdapter extends FragmentPagerAdapter 
	{
		// -------------------------------------------------------------------------
		public SectionsPagerAdapter (FragmentManager frameManager) 
		{
			super (frameManager);
		}
		// -------------------------------------------------------------------------
		@Override
		public Fragment getItem (int position) 
		{
			Fragment fragment = new DummySectionFragment ();
			Bundle args = new Bundle ();
			args.putInt (DummySectionFragment.ARG_SECTION_NUMBER, position);
			fragment.setArguments (args);
			return fragment;
		}
		// =========================================================================
		@Override
		public int getCount() 
		{
			// ---------------------------------------------------------------------
			// 27/01/2015 ECU return the number of appointments that have been set
			// ---------------------------------------------------------------------
			return PublicData.appointments.size();
		}
		// =========================================================================
		@Override
		public CharSequence getPageTitle(int position) 
		{
			return "Appointment " + (position + 1) + " of " + PublicData.appointments.size();
		}
		// =========================================================================
	}
	// =============================================================================
	public static class DummySectionFragment extends Fragment 
	{
		// -------------------------------------------------------------------------
		public static final String ARG_SECTION_NUMBER = "section_number";
		// -------------------------------------------------------------------------
		public DummySectionFragment() 
		{
		}
		// -------------------------------------------------------------------------
		@Override
		public View onCreateView (LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) 
		{
			int sectionNumber = getArguments().getInt (ARG_SECTION_NUMBER);
			
			View rootView = inflater
					.inflate (R.layout.activity_appointment_details,
							container, false);
			
			TextView details = (TextView) rootView.findViewById (R.id.appointment_details);
			TextView active = (TextView) rootView.findViewById (R.id.appointment_active);
			// ---------------------------------------------------------------------
			// 02/11/2016 ECU make the details field scrollable
			// ---------------------------------------------------------------------
			details.setMovementMethod (new ScrollingMovementMethod ()); 
			// ---------------------------------------------------------------------
			// 31/10/2016 ECU added ...activity to act as the context
			// ---------------------------------------------------------------------
			details.setText (PublicData.appointments.get (sectionNumber).Print (MainActivity.activity,true));
			active.setText ((PublicData.appointments.get (sectionNumber).active ? "active" : "inactive"));
			
			return rootView;
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

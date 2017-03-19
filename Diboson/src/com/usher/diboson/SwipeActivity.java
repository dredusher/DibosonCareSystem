package com.usher.diboson;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class SwipeActivity extends FragmentActivity 
{
	/* ============================================================================= */
	// 21/04/2014 ECU created to test swipe actions
	// 11/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	/* ============================================================================= */
	public static final String	ARGUMENT_SECTION_NUMBER = "section_number";
	/* ============================================================================= */
	static Context			context;				// 10/11/2014 ECU added
	SectionsPagerAdapter 	sectionsPagerAdapter;
	ViewPager 				viewPager;
	/* ============================================================================= */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 11/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 22/04/2014 ECU set up standard activity features
			// 03/05/2015 ECU added ...ACT... for clarity
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity(this,StaticData.ACTIVITY_FULL_SCREEN);
			// ---------------------------------------------------------------------
			// 10/11/2014 ECU remember the context
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_swipe);
			// ---------------------------------------------------------------------
			// 22/04/2014 ECU Create the adapter that will return a fragment for each of
			//			      the primary sections of the app.
			// ---------------------------------------------------------------------
			sectionsPagerAdapter = new SectionsPagerAdapter (getSupportFragmentManager());
			// ---------------------------------------------------------------------
			// 22/04/2014 ECU Set up the ViewPager with the sections adapter.
			// ---------------------------------------------------------------------
			viewPager = (ViewPager) findViewById(R.id.pager);
			viewPager.setAdapter (sectionsPagerAdapter);
		
			PagerTitleStrip titleStrip = (PagerTitleStrip) findViewById (R.id.pager_title_strip);	
			// ---------------------------------------------------------------------
			// 23/04/2014 ECU set font size depending on stored value
			// ---------------------------------------------------------------------
			titleStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
							(int) getResources().getDimension(R.dimen.default_big_font_size) 
										/ getResources().getDisplayMetrics().density);
			// ---------------------------------------------------------------------
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
		public Fragment getItem (int position) 
		{
			// ---------------------------------------------------------------------
			// 22/04/2014 ECU getItem is called to instantiate the fragment for the 
			//   			  given page. Return a DummySectionFragment (defined as 
			//                a static inner class below) with the page number as 
			// 				  its lone argument. 
			// ---------------------------------------------------------------------
			Fragment fragment = new SwipeSectionFragment();
			
			Bundle args = new Bundle();
			args.putInt (ARGUMENT_SECTION_NUMBER, position);
			fragment.setArguments (args);
			return fragment;
			// ---------------------------------------------------------------------
		}
		/* ========================================================================= */
		@Override
		public int getCount() 
		{
			return GridActivity.gridImages.length;
		}
		/* ========================================================================= */
		@Override
		public CharSequence getPageTitle(int position) 
		{
			return GridActivity.gridImages[position].legend;
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
		public View onCreateView (LayoutInflater inflater,ViewGroup container,
									Bundle savedInstanceState) 
		{
			View rootView = inflater.inflate(R.layout.fragment_swipe_dummy,
											container, false);
			TextView sectionTextView = (TextView) rootView.findViewById(R.id.section_label);
			ImageView sectionImageView = (ImageView) rootView.findViewById(R.id.section_image);
			
			final int sectionNumber = getArguments().getInt(ARGUMENT_SECTION_NUMBER);
			
			sectionImageView.setImageResource (GridActivity.gridImages[sectionNumber].imageId);
			
			// ---------------------------------------------------------------------
			// 23/04/2014 ECU just display any help that may be associated with this
			//                section
			// ---------------------------------------------------------------------		
			String theHelpFileName = PublicData.projectFolder + getString (R.string.help_directory) + 
					"Grid_" + getResources().getResourceEntryName(GridActivity.gridImages[sectionNumber].imageId);
			
			byte [] theBytes  = Utilities.readAFile (theHelpFileName);
			
			if (theBytes != null)
			{
				// -----------------------------------------------------------------
				// 28/11/2016 ECU help text exists but decide whether to use HTML
				//                format or not
				// -----------------------------------------------------------------
				String localHelpText = new String(theBytes);
				// -----------------------------------------------------------------
				// 28/11/2016 ECU all text to be displayed in HTML format must start
				//                with the introducer
				// -----------------------------------------------------------------
				if (localHelpText.startsWith (StaticData.HTML_INTRODUCER))
				{
					// -------------------------------------------------------------
					// 28/11/2016 ECU the file has HTML formatted text
					// -------------------------------------------------------------
					sectionTextView.setText (Html.fromHtml (localHelpText));
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 28/11/2016 ECU just display the text 'as is'
					// -------------------------------------------------------------
					sectionTextView.setText (localHelpText);
					// -------------------------------------------------------------
				}	
			}
			else
			{
				// -----------------------------------------------------------------
				// 28/11/2016 ECU changed to use resource rather than literal
				// -----------------------------------------------------------------
				sectionTextView.setText (getString (R.string.no_help_exists));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			sectionImageView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view) 
				{	
					Intent localIntent = new Intent (context,GridActivity.class);
      				localIntent.putExtra (StaticData.PARAMETER_POSITION,sectionNumber);
      				startActivityForResult (localIntent,0);
				}
			});
			return rootView;
		}
		/* ========================================================================= */
	}
	/* ============================================================================= */
}

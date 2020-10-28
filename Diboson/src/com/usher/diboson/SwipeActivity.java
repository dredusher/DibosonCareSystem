package com.usher.diboson;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Base64;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

public class SwipeActivity extends FragmentActivity 
{
	// =============================================================================
	// 21/04/2014 ECU created to test swipe actions
	// 11/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 14/12/2018 ECU remove the 'static' context and some tidying up
	// 30/10/2019 ECU AndroidStudio - changed SwipeSectionFragment to static which
	//                required that context be static
	// 05/10/2020 ECU change to use 'Utilities.startASpecifiedActivity' rather than
	//                starting up 'GridActivity.class'
	// =============================================================================
	public static final String	ARGUMENT_SECTION_NUMBER = "section_number";
	// =============================================================================
	static Context			context;				// 10/11/2014 ECU added
													// 30/10/2019 ECU changed to static
	SectionsPagerAdapter 	sectionsPagerAdapter;
	ViewPager 				viewPager;
	// =============================================================================
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 11/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 22/04/2014 ECU set up standard activity features
			// 03/05/2015 ECU added ...ACT... for clarity
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
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
			viewPager = (ViewPager) findViewById (R.id.pager);
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
	// =============================================================================
	@Override
	public boolean onCreateOptionsMenu (Menu menu) 
	{
		// -------------------------------------------------------------------------
		return true;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public class SectionsPagerAdapter extends FragmentPagerAdapter 
	{
		// =========================================================================
		public SectionsPagerAdapter (FragmentManager fragmentManager) 
		{
			// ---------------------------------------------------------------------
			super (fragmentManager);
			// ---------------------------------------------------------------------
		}
		// =========================================================================
		@Override
		public Fragment getItem (int position) 
		{
			// ---------------------------------------------------------------------
			// 22/04/2014 ECU getItem is called to instantiate the fragment for the 
			//   			  given page. Return a DummySectionFragment (defined as 
			//                a static inner class below) with the page number as 
			// 				  its lone argument. 
			// ---------------------------------------------------------------------
			Fragment fragment = new SwipeSectionFragment ();
			
			Bundle args = new Bundle ();
			args.putInt (ARGUMENT_SECTION_NUMBER, position);
			fragment.setArguments (args);
			return fragment;
			// ---------------------------------------------------------------------
		}
		// =========================================================================
		@Override
		public int getCount() 
		{
			// ---------------------------------------------------------------------
			return GridActivity.gridImages.length;
			// ---------------------------------------------------------------------
		}
		// =========================================================================
		@Override
		public CharSequence getPageTitle (int position) 
		{
			// ---------------------------------------------------------------------
			// 01/04/2017 ECU changed to use new Legend method
			// 18/04/2017 ECU add the context as an argument
			// ---------------------------------------------------------------------
			return GridActivity.gridImages[position].Legend (context);
			// ---------------------------------------------------------------------
		}
		// =========================================================================
	}
	// =============================================================================
	public static class SwipeSectionFragment extends Fragment
	{
		// =========================================================================
		// 14/12/2018 ECU changed from 'static'
		// =========================================================================
		public SwipeSectionFragment ()
		{
		}
		// =========================================================================
		@Override
		public View onCreateView (LayoutInflater 	inflater,
								  ViewGroup 		container,
								  Bundle 			savedInstanceState) 
		{
			// ---------------------------------------------------------------------
			// 25/09/2020 ECU added the 'web view' which initially has a visibility
			//                of 'gone'
			// ---------------------------------------------------------------------
			View rootView 				= inflater.inflate (R.layout.fragment_swipe_dummy,container, false);
			TextView sectionTextView 	= (TextView) rootView.findViewById (R.id.section_label);
			WebView sectionWebView 		= (WebView)  rootView.findViewById (R.id.web_view);
			ImageView sectionImageView 	= (ImageView) rootView.findViewById (R.id.section_image);
			// ---------------------------------------------------------------------
			final int sectionNumber = getArguments().getInt (ARGUMENT_SECTION_NUMBER);
			// ---------------------------------------------------------------------
			sectionImageView.setImageResource (GridActivity.gridImages[sectionNumber].imageId);
			// ---------------------------------------------------------------------
			// 23/04/2014 ECU just display any help that may be associated with this
			//                section
			// ---------------------------------------------------------------------
			// 10/11/2017 ECU change to use the changes of 05/06/2017 when the help
			//                text was set as a 'raw resource'
			// ---------------------------------------------------------------------
			String helpText = Utilities.gridHelpData (context, GridActivity.gridImages[sectionNumber].imageId);
			// ---------------------------------------------------------------------		
			if (helpText != null)
			{
				// -----------------------------------------------------------------
				// 25/09/2020 ECU NOTE - the text to be displayed can be in one of
				//                ====   three formats.
				//                       1) text which has no embedded html tags
				//                       2) text which contains a limited number of
				//                          html tags which are supported using
				//                          setText. The text will be started with
				//                          StaticData.HTML_INTRODUCER.
				//                       3) text which forms a valid web page with
				//                          all of it's html tags.The text will be
				//                          started by StaticData.HTNL_TAG.
				// -----------------------------------------------------------------
				// 28/11/2016 ECU help text exists but decide whether to use HTML
				//                format or not
				// 25/09/2020 ECU check for a proper 'html page' which will
				//                require a 'webview' to display properly
				// -----------------------------------------------------------------
				if (!helpText.startsWith (StaticData.HTML_TAG))
				{
					// -------------------------------------------------------------
					// 28/11/2016 ECU all text to be displayed in HTML format must
					//                start with the introducer
					// -------------------------------------------------------------
					if (helpText.startsWith (StaticData.HTML_INTRODUCER))
					{
						// ---------------------------------------------------------
						// 28/11/2016 ECU the file has HTML formatted text
						// ---------------------------------------------------------
						sectionTextView.setText (Html.fromHtml (helpText));
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 28/11/2016 ECU just display the text 'as is'
						// ---------------------------------------------------------
						sectionTextView.setText (helpText);
						// ---------------------------------------------------------
					}
				}
				else
				{
					// -------------------------------------------------------------
					// 25/09/2020 ECU this looks as if the text is a valid 'html'
					//                web page so treat accordingly
					// -------------------------------------------------------------
					// 25/09/2020 ECU want to remove the 'text view' field and then
					//                make the 'web view' visible
					// -------------------------------------------------------------
					sectionTextView.setVisibility (View.GONE);
					sectionWebView.setVisibility  (View.VISIBLE);
					// -------------------------------------------------------------
					// 25/09/2020 ECU want to display the 'html page' using
					//                base64 encoding
					// -------------------------------------------------------------
					sectionWebView.loadData (Base64.encodeToString (helpText.getBytes(),Base64.DEFAULT),
							StaticData.HTML_MIME_TYPE,StaticData.HTML_ENCODING_BASE64);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
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
			sectionImageView.setOnClickListener (new View.OnClickListener()
			{
				// -----------------------------------------------------------------
				@Override
				public void onClick (View view) 
				{
					// -------------------------------------------------------------
					// 05/10/2020 ECU changed from:-
					//					Intent localIntent = new Intent (context,GridActivity.class);
      				//					localIntent.putExtra (StaticData.PARAMETER_POSITION,sectionNumber);
      				//					--------------------------------------------
      				//                  Note - may need to check on this 'sort inhibit'
      				//					--------------------------------------------
					//					localIntent.putExtra (StaticData.PARAMETER_SORT_INHIBIT,true);
      				//					startActivityForResult (localIntent,0);
      				// -------------------------------------------------------------
      				Utilities.startASpecficActivity (sectionNumber);
      				// -------------------------------------------------------------
				}
			});
			return rootView;
			// ---------------------------------------------------------------------
		}
		// =========================================================================
	}
	// =============================================================================
}

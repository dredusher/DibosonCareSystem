package com.usher.diboson;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

@SuppressWarnings("deprecation")
public class TVChannelFragment extends Fragment
{
	// =============================================================================
	// 03/07/2016 ECU Note - this fragment is associated with a tab which is set up
	//                for each date in the EPG
	// =============================================================================

	// =============================================================================

	// =============================================================================
    public static TVChannelFragment newInstance () 
    {
        return new TVChannelFragment ();
    }
    // =============================================================================
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) 
    {
    	// -------------------------------------------------------------------------
    	// 03/07/2016 ECU Note - a ViewPager is used as an easy way of scrolling
    	//                data left/right - the data will be associated with each
    	//                TV channel that has been selected for the EPG
    	//
    	//				  It also incorporates a PagerTabStrip which will contain
    	//                the names of the TV channels.
    	// -------------------------------------------------------------------------
        View root = inflater.inflate (R.layout.epg_viewpager, container,false);

        final ViewPager viewPager = (ViewPager) root.findViewById (R.id.viewPager);
        
        viewPager.setAdapter (new TVChannelAdapter (getChildFragmentManager()));
        // -------------------------------------------------------------------------
        // 14/02/2017 ECU put in a listener to detect when a TV channel is selected
        // -------------------------------------------------------------------------
        viewPager.setOnPageChangeListener (new OnPageChangeListener() 
        {
        	// ---------------------------------------------------------------------
        	public void onPageSelected (int theChannel) 
        	{
        		// -----------------------------------------------------------------
        		// 14/02/2017 ECU at this point have detected that a TV channel has
        		//                been selected and its 'channel number' is specified
        		//                in the argument.
        		//            ECU really want to scroll the displayed page to tie in
        		//                with ShowEPGActivity.scrolledTime
        		// 17/02/2017 ECU the code that was to be placed here is now in
        		//                TVEPGFragment at method 'setUserVisibleHint'
        		// -----------------------------------------------------------------
        	}
        	// ---------------------------------------------------------------------
        	public void onPageScrolled (int arg0, float arg1, int arg2) 
        	{        
        	}
        	// ---------------------------------------------------------------------
        	public void onPageScrollStateChanged (int arg0) 
        	{     
        	}
        	// ---------------------------------------------------------------------
        });
        // ------------------------------------------------------------------------- 
        return root;
    }
    // =============================================================================
    public class TVChannelAdapter extends FragmentPagerAdapter 
    {
    	// -------------------------------------------------------------------------
    	// 13/11/2017 ECU remove the 'static' definition
    	// -------------------------------------------------------------------------
        public TVChannelAdapter (FragmentManager fragmentManager) 
        {
            super (fragmentManager);
        }
        // -------------------------------------------------------------------------
        @Override
        public int getCount () 
        {
        	// ---------------------------------------------------------------------
        	// 01/07/2016 ECU Note - return the number of views available
        	// ---------------------------------------------------------------------
        	// 14/10/2015 ECU put in the check on null
        	// 16/11/2017 ECU changed to use 'OnDisplay' rather than 'Selected'
        	// ---------------------------------------------------------------------
        	if (ShowEPGActivity.TVChannelsOnDisplay == null)
        		return 0;
        	else
        		return ShowEPGActivity.TVChannelsOnDisplay.size ();
        	// ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        @Override
        public Fragment getItem (int thePosition) 
        {
        	// ---------------------------------------------------------------------
        	// 01/07/2016 ECU Note - returns the fragment associated with the
        	//                specified position
        	// ---------------------------------------------------------------------
            Bundle arguments = new Bundle();
            arguments.putInt (StaticData.PARAMETER_POSITION, thePosition);
            return TVEPGFragment.newInstance (arguments);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        @Override
        public CharSequence getPageTitle (int thePosition) 
        {
        	// ---------------------------------------------------------------------
        	// 01/07/2016 ECU Note - returns the title of the page pointed to by
        	//                the argument
        	// 16/11/2017 ECU changed to use 'OnDisplay' rather than 'Selected'
        	// ---------------------------------------------------------------------
            return ShowEPGActivity.TVChannelsOnDisplay.get (thePosition).channelName;
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
}

package com.usher.diboson;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

public class TVEPGFragment extends Fragment
{
	// =============================================================================
	// 21/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// =============================================================================
	
	// =============================================================================
	public			ArrayList<EPGEntry> 		epgInformation;			// 14/02/2017 ECU added
	public 			EPGListViewAdapter 			listViewAdapter;
	public static	boolean						showDetails;
	public static	EPGListViewAdapter 			staticListViewAdapter;
	// =============================================================================
	
	// =============================================================================
	AdapterHandler	adapterHandler		= new AdapterHandler ();
	int				channel;
	Button			detailsButton		= null;
	ListView		listView;										// 17/02/2016 ECU added
	long			scrolledTime;									// 14/02/2017 ECU added
																	// 17/02/2017 ECU changed from String
	Button			searchButton		= null;						// 28/06/2016 ECU added
	ImageButton		timersButton		= null;						// 07/07/2016 ECU added
	// =============================================================================

    // =============================================================================
    public static TVEPGFragment newInstance (Bundle theArguments) 
    {
    	// -------------------------------------------------------------------------
    	// 01/07/2016 ECU Note - static factory method that takes a bundle parameter,
        // 				  initialises the fragment's arguments, and returns the
        //				  new fragment to the caller.
        // -------------------------------------------------------------------------
        TVEPGFragment fragment = new TVEPGFragment ();
        fragment.setArguments (theArguments);
        return fragment;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
  
    // =============================================================================
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
   		View root = inflater.inflate (R.layout.fragment_channel_epg, container, false);
    	// -------------------------------------------------------------------------
    	if (savedInstanceState == null) 
    	{
    		// ---------------------------------------------------------------------
    		// 21/10/2015 ECU the activity has been created anew
    		// ---------------------------------------------------------------------
    		int currentTab = ShowEPGActivity.fragmentTabHost.getCurrentTab();
    		// ---------------------------------------------------------------------
    		// 03/07/2016 ECU Note - the selected TV channel is in the bundle
    		// ---------------------------------------------------------------------
    		channel = getArguments().getInt (StaticData.PARAMETER_POSITION);
    		// ---------------------------------------------------------------------
    		// 28/09 2015 ECU set up the details button
    		// 07/07/2016 ECU add the timers button
    		// ---------------------------------------------------------------------
    		detailsButton 	= (Button)      root.findViewById (R.id.epg_details);
    		searchButton 	= (Button)      root.findViewById (R.id.epg_search);
    		timersButton 	= (ImageButton) root.findViewById (R.id.epg_timer_button);
    		// ---------------------------------------------------------------------
    		// 27/09/2015 ECU set up the button widths
    		// ---------------------------------------------------------------------
    		detailsButton.setWidth (PublicData.screenWidth/2);
    		searchButton.setWidth  (PublicData.screenWidth/2);
    		// ---------------------------------------------------------------------
    		// 27/09/2015 ECU set up the button listener
    		// 07/07/2016 ECU set up the 'timers' button
    		// ---------------------------------------------------------------------
    		detailsButton.setOnClickListener (buttonListener);
    		searchButton.setOnClickListener  (buttonListener);
    		timersButton.setOnClickListener  (buttonListener);
    		// ---------------------------------------------------------------------
    		// 07/07/2016 ECU check the initial visibility of the timer button
    		// ---------------------------------------------------------------------
    		timersButton.setVisibility (ShowEPGActivity.checkTimerButtonVisibility ());
    		// ---------------------------------------------------------------------
    		// 28/06/2016 ECU set up the long click listener for search button
    		// 17/02/2017 ECU set up the details button
    		// ---------------------------------------------------------------------
    		detailsButton.setOnLongClickListener (buttonListenerLong);
    		searchButton.setOnLongClickListener  (buttonListenerLong);
    		// ---------------------------------------------------------------------
    		// 28/06/2016 ECU set up the two line legend for the search button
    		// ---------------------------------------------------------------------
    		searchButton.setText (Utilities.twoLineButtonLegend (MainActivity.activity, 
    															 getString (R.string.epg_search), 
    															 getString (R.string.epg_search_long_press)));
    		// ---------------------------------------------------------------------
    		// 17/02/2017 ECU set as two line button so that a long press will cause 
    		//                the programs to line up by time
    		// ---------------------------------------------------------------------
    		setDetailsButton ();
    		// ---------------------------------------------------------------------
    		listView = (ListView) root.findViewById (R.id.epg_listview);
    		// ---------------------------------------------------------------------
    		// 14/02/2017 ECU get the EPG information that is relevant for this
    		//                fragment's display
    		// ---------------------------------------------------------------------
    		epgInformation = ShowEPGActivity.TVChannelsSelected.get (channel).EPGentriesByDate (ShowEPGActivity.EPGdates.get(currentTab));
    		// ---------------------------------------------------------------------
    		// 26/09/2015 ECU pass through the TV channel number (in 'position') to
    		//                which the adapter applies
    		// ---------------------------------------------------------------------
    		listViewAdapter = new EPGListViewAdapter (getActivity(), 
    												  R.layout.epg_entry_details,
	    									          channel,
	    										      epgInformation);	
    		listView.setAdapter (listViewAdapter);
    		// ---------------------------------------------------------------------
	   	 	listView.setOnItemLongClickListener (new OnItemLongClickListener()
	   	 	{
	   	 		@Override
	   	 		public boolean onItemLongClick (AdapterView<?> theParent, 
	   	 										View theView,
	   	 										int thePosition,
	   	 										long id) 
	   	 		{
	   	 			// -------------------------------------------------------------
	   	 			// 26/09/2015 ECU use the long press to select/deselect the item
	   	 			// -------------------------------------------------------------
	   	 			String currentDate 	= ShowEPGActivity.EPGdates.get (ShowEPGActivity.fragmentTabHost.getCurrentTab());
	   	 			// -------------------------------------------------------------
	   	 			// 26/09/2015 ECU try and find the selected entry
	   	 			//            ECU pass through the channel held in the adapter
	   	 			// 28/09/2015 ECU changed to search on start time rather than
	   	 			//                position
	   	 			//            ECU changed to pass through the EPG entry
	   	 			// -------------------------------------------------------------
	   	 			ShowEPGActivity.selectEPGEntry (listViewAdapter.channel,
	   	 											currentDate,
	   	 											listViewAdapter.data.get (thePosition));
	   	 			// -------------------------------------------------------------
            		// 07/07/2016 ECU check the initial visibility of the timer button
	   	 			// 08/07/2016 ECU put in the 'null' check
            		// -------------------------------------------------------------
	   	 			if (timersButton != null)
	   	 				timersButton.setVisibility (ShowEPGActivity.checkTimerButtonVisibility());
	   	 			// -------------------------------------------------------------
	   	 			// 03/07/2016 ECU Note - make sure the display is updated
	   	 			// -------------------------------------------------------------
	   	 			listViewAdapter.notifyDataSetChanged ();
	   	 			// -------------------------------------------------------------
	   	 			return true;
	   	 			// -------------------------------------------------------------
	   	 		}
	   	 	} );
	   	 	// ---------------------------------------------------------------------
	   	 	listView.setOnItemClickListener (new OnItemClickListener() 
			{
				// -----------------------------------------------------------------
				@Override
				public void onItemClick (AdapterView<?> theParent, 
						                 View theView, 
						                 int thePosition, 
						                 long theID) 
				{
					// --------------------------------------------------------------
					// 24/07/2016 ECU added to handle an item being selected
					// --------------------------------------------------------------
					Utilities.popToast (theView,listViewAdapter.data.get (thePosition).PrintAll (),true);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			});
	   	 	// ---------------------------------------------------------------------
	   	 	// 14/02/2017 ECU include the listener to monitor when the listview is
	   	 	//                scrolled
	   	 	// ---------------------------------------------------------------------
	   	 	listView.setOnScrollListener (new OnScrollListener() 
	   	 	{ 
	   	 		// -----------------------------------------------------------------
	   	 		// 17/02/2017 ECU remember what caused the scroll so that 'idle'
	   	 		//                states can be ignored
	   	 		// -----------------------------------------------------------------
	   	 		int scrollState;							// 17/02/2017 ECU added
	   	 		// -----------------------------------------------------------------
	   	 		@Override
	   	 		public void onScrollStateChanged (AbsListView view, int theScrollState) 
	   	 		{
	   	 			// -------------------------------------------------------------
	   	 			// 17/02/2017 ECU remember the current scroll state
	   	 			// -------------------------------------------------------------
	   	 			scrollState = theScrollState;
	   	 			// -------------------------------------------------------------
	   	 		}
	   	 		// -----------------------------------------------------------------
	   	 		@Override
	   	 		public void onScroll (AbsListView view, 
	   	 							  int firstVisibleItem,
	   	 							  int visibleItemCount, 
	   	 							  int totalItemCount) 
	   	 		{
	   	 			// -------------------------------------------------------------
	   	 			// 17/02/2017 ECU only check for scrolling if it is the appropriate
	   	 			//                state
	   	 			//            ECU added check on 'tvChannelsPositioning'
	   	 			// -------------------------------------------------------------
	   	 			if (PublicData.storedData.tvChannelsPositioning && scrollState != SCROLL_STATE_IDLE)
	   	 			{
	   	 				// ---------------------------------------------------------
	   	 				// 14/02/2017 ECU get the start time for the the first visible
	   	 				//                item
	   	 				// 15/02/2017 ECU changed to use new method
	   	 				// 17/02/2017 ECU hanged to use milliseconds
	   	 				// ---------------------------------------------------------
	   	 				scrolledTime = epgInformation.get (firstVisibleItem).getStartTimeMillisecs ();
	   	 				// ---------------------------------------------------------
	   	 				// 14/02/2017 ECU check if need to remember the scroll time
	   	 				// 17/02/2017 ECU change with the type change from String to
	   	 				//                Long
	   	 				// ---------------------------------------------------------
	   	 				if ((ShowEPGActivity.scrolledTime == null) || 
	   	 						(ShowEPGActivity.scrolledTime.compareTo(scrolledTime) != 0))
	   	 				{
	   	 					// -----------------------------------------------------
	   	 					// 14/02/2016 ECU remember the new scroll time
	   	 					// -----------------------------------------------------
	   	 					ShowEPGActivity.scrolledTime = scrolledTime;
	   	 					// -----------------------------------------------------
	   	 					// 19/02/2017 ECU remember the components
	   	 					// -----------------------------------------------------
	   	 					epgInformation.get (firstVisibleItem).setStartHourMinute ();
	   	 					// -----------------------------------------------------
	   	 					// 19/02/2017 ECU indicate that an announcement is wanted
	   	 					// -----------------------------------------------------
	   	 					ShowEPGActivity.scrolledTimeAnnouncement = true;
	   	 					// -----------------------------------------------------
	   	 				}
	   	 				// ---------------------------------------------------------	
	   	 			}
	   	 			// -------------------------------------------------------------
	   	 		}
	   	 		// -----------------------------------------------------------------
	   	 	});
	   	 	// ---------------------------------------------------------------------
    	}
	    // -------------------------------------------------------------------------
        return root;
    }
    // =============================================================================
	private View.OnClickListener buttonListener = new View.OnClickListener() 
	{
		// -------------------------------------------------------------------------
		@Override
		public void onClick (View theView) 
		{	
			// -----------------------------------------------------------------
			// 27/09/2015 ECU get the adapter of the ListView on the same fragment
			//                as the buttons
			// 07/07/2016 ECU Note - if the following statement is uncommented
			//                then the display is not updated correctly - needs
			//                investigation.
			//            ECU 'listViewAdapter' is set up in onCreateView so
			//                really does not need to be redefined here
			// -----------------------------------------------------------------
			//listViewAdapter = (EPGListViewAdapter) ((ListView)theView.getRootView().findViewById(R.id.epg_listview)).getAdapter();
			staticListViewAdapter = listViewAdapter;
			//------------------------------------------------------------------
			// 18/09/2015 ECU now process depending on which button pressed
			//------------------------------------------------------------------
			switch (theView.getId()) 
			{
				// -------------------------------------------------------------
				case R.id.epg_details:
					// ---------------------------------------------------------
					// 18/09/2015 ECU toggle the bit that indicates whether or
					//                not details of each EPG entry is (or not)
					//                to be displayed
					// ---------------------------------------------------------
		          	listViewAdapter.toggleShowDetails ();
		          	// ---------------------------------------------------------
		          	// 03/07/2016 ECU Note - update the display
		          	// ---------------------------------------------------------
	            	listViewAdapter.notifyDataSetChanged ();
	            	// ---------------------------------------------------------
	            	// 28/09/2015 ECU change the button text
	            	// 17/02/2017 ECU changed to use the new method
	            	// ---------------------------------------------------------
	            	setDetailsButton ();
					// --------------------------------------------------------- 
					break;
				// -------------------------------------------------------------
				case R.id.epg_search:
					// ---------------------------------------------------------
					// 18/09/2015 ECU select the channels that are to be used
					//                for EPG
					// ---------------------------------------------------------
		      		// ---------------------------------------------------------
            		// 27/09/2015 ECU start up the dialogue to get the search 
            		//                string
            		// ---------------------------------------------------------
            		DialogueUtilities.textInput (ShowEPGActivity.context,"Search String",
        					"Please enter the text to search for",
        					Utilities.createAMethod (TVEPGFragment.class,"SearchConfirm",""),
        					Utilities.createAMethod (TVEPGFragment.class,"SearchCancel",""));
            		// ---------------------------------------------------------
					break;
				// -------------------------------------------------------------
				case R.id.epg_timer_button:
					// ---------------------------------------------------------
					// 07/07/2016 ECU created to handle the display of current
					//                timers
					// ---------------------------------------------------------
					ShowEPGActivity.EPGCheckForAlarms ();
					// ---------------------------------------------------------
					break;
				// -------------------------------------------------------------
				}	
		}
		// -------------------------------------------------------------------------
	}; 
	// =============================================================================
	private View.OnLongClickListener buttonListenerLong = new View.OnLongClickListener() 
	{		
		@Override
		public boolean onLongClick(View view) 
		{	
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU now process depending on which button pressed
			// ---------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.epg_details:
					// -------------------------------------------------------------
					// 17/02/2017 ECU toggle the alignment flag
					// -------------------------------------------------------------
					PublicData.storedData.tvChannelsPositioning = !PublicData.storedData.tvChannelsPositioning;
					// -------------------------------------------------------------
					// 17/02/2017 ECU adjust the legend on the button
					//            ECU changed to use new method
					// -------------------------------------------------------------
					setDetailsButton ();
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.epg_search: 
				{
					// -------------------------------------------------------------
					// 28/06/2016 ECU display the list of channels to select from
					// 29/06/2016 ECU use the channel names rather than do a rebuild
					// 02/07/2016 ECU added the method for the cancel task
					// -------------------------------------------------------------
					DialogueUtilities.listChoice (ShowEPGActivity.context, 
												  ShowEPGActivity.context.getString (R.string.epg_select_channel),
												  ShowEPGActivity.channelNames,
												  Utilities.createAMethod (TVEPGFragment.class,"SelectedChannelMethod",0),
												  ShowEPGActivity.context.getString (R.string.epg_select_all_channels),
												  Utilities.createAMethod (TVEPGFragment.class,"SelectAllChannelsMethod",0),
												  ShowEPGActivity.context.getString (R.string.cancel),
												  Utilities.createAMethod (TVEPGFragment.class,"SelectChannelCancelMethod",0));
					// -------------------------------------------------------------
					break;
				}		
			}
			// ---------------------------------------------------------------------
			return true;
		}
	};
	// =============================================================================
	@Override
	public void onDestroy()
	{	
		// -------------------------------------------------------------------------
		// 02/10/2015 ECU added
		//            ECU make sure that the handler stops looping
		// -------------------------------------------------------------------------
		adapterHandler.sendEmptyMessage (StaticData.MESSAGE_FINISH);
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onStop()
	{	
		// -------------------------------------------------------------------------
		// 02/10/2015 ECU added
		// -------------------------------------------------------------------------
		// 02/10/2015 ECU do not need to do any additional actions because 'onDestroy'
		//                will be doing them
		// -------------------------------------------------------------------------
		super.onStop();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void checkForPositioning ()
	{
		// -------------------------------------------------------------------------
		// 17/02/2017 ECU created to check if the listview is to be positioned to
		//                a specified point
		// -------------------------------------------------------------------------
		if (ShowEPGActivity.scrolledTime != null)
		{
			int firstEntry = EPGEntry.getFirstProgram (epgInformation,ShowEPGActivity.scrolledTime);
			// ---------------------------------------------------------------------
			// 17/02/2017 ECU if the listview exists and there is a valid entry then
			//                position the listview
			// ---------------------------------------------------------------------
			if ((listView != null) && (firstEntry != StaticData.NO_RESULT))
			{
				listView.setSelection (firstEntry);
				// -----------------------------------------------------------------
				// 19/02/2017 ECU tell the user the situation
				//            ECU only do if requested
				// -----------------------------------------------------------------
				if (ShowEPGActivity.scrolledTimeAnnouncement)
				{
					Utilities.SpeakAPhrase (MainActivity.activity,
											MainActivity.activity.getString (R.string.program_alignment) +
												Utilities.SpeakingClockConvert (ShowEPGActivity.scrolledTimeHour,
																				ShowEPGActivity.scrolledTimeMinute));
					// -------------------------------------------------------------
					// 19/02/2017 ECU indicate announcement made
					// -------------------------------------------------------------
					ShowEPGActivity.scrolledTimeAnnouncement = false;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SearchCancel (String theText)
	{
    	// ---------------------------------------------------------------------
    	// 26/09/2015 ECU need some way of refreshing the current adapter
       	// ---------------------------------------------------------------------
    	
	}
	// =============================================================================
	public static void SearchConfirm (String theText)
	{
		// -------------------------------------------------------------------------
		// 27/09/2015 ECU sets the string to be used for searching the TV EPG data
		// 24/07/2016 ECU changed because filter moved to Show....
		// -------------------------------------------------------------------------
		if (!theText.equalsIgnoreCase(""))
		{
			ShowEPGActivity.filterString = theText;
		}
		else
		{
    		ShowEPGActivity.filterString = null;
		}
		// -------------------------------------------------------------------------
		// 27/09/2015 ECU want to be able to refresh the current view
		// -------------------------------------------------------------------------
		staticListViewAdapter.RebuildList (null);
		// -------------------------------------------------------------------------
		// 24/07/2016 ECU tell the listeners that the data has changed
		// -------------------------------------------------------------------------
		staticListViewAdapter.notifyDataSetChanged();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SelectAllChannelsMethod (int theChannelIndex)
	{
		// -------------------------------------------------------------------------
		// 28/06/2016 ECU created to handle when reset for all channels
		// 30/06/2016 ECU refreshEPG moved
		// -------------------------------------------------------------------------
		ShowEPGActivity.refreshEPG (StaticData.NO_RESULT);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SelectedChannelMethod (int theChannelIndex)
	{
		// -------------------------------------------------------------------------
		// 28/06/2016 ECU created to handle a channel being selected
		// 30/06/2016 ECU refreshEPG moved
		// -------------------------------------------------------------------------
		ShowEPGActivity.refreshEPG (theChannelIndex);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SelectChannelCancelMethod (int theChannelIndex)
	{
		// -------------------------------------------------------------------------
		// 02/07/2016 ECU created to handle the cancellation of the 'select channel(s)'
		//                process
		// -------------------------------------------------------------------------
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void setDetailsButton ()
	{
		// -------------------------------------------------------------------------
		// 17/02/2017 ECU created to set the legend on the 'details' button - created
		//                because it is called from a number of places
		// -------------------------------------------------------------------------
		detailsButton.setText (Utilities.twoLineButtonLegend (MainActivity.activity, 
				  showDetails ? getString (R.string.epg_hide_details) 
                              : getString (R.string.epg_details), 
				  PublicData.storedData.tvChannelsPositioning ? getString (R.string.epg_details_long_press_off)
						  									  : getString (R.string.epg_details_long_press_on)));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    @Override
    public void setUserVisibleHint (boolean theVisibilityFlag)
    {
    	super.setUserVisibleHint (theVisibilityFlag);
    	// -------------------------------------------------------------------------
    	// 28/09/2015 ECU theVisibilityFlag = true ....... becoming visible
    	//                                  = false ...... becoming hidden
    	// -------------------------------------------------------------------------
    	if (theVisibilityFlag)
    	{
    		if (listViewAdapter != null)
    			listViewAdapter.RebuildList (null);
    		// ---------------------------------------------------------------------
        	// 28/09/2015 ECU refresh the button text
    		// 17/02/2017 ECU changed to use the new method
        	// ---------------------------------------------------------------------
    		if (detailsButton != null)
    		{
    			setDetailsButton ();
    		}
    		// ---------------------------------------------------------------------
    		// 17/02/2017 ECU check if the display is to be scrolled to the required
    		//                time
    		//            ECU put in the check on the enablement flag
    		// ---------------------------------------------------------------------
    		if (PublicData.storedData.tvChannelsPositioning)
    		{
    			checkForPositioning ();
    		}
        	// ---------------------------------------------------------------------
    		// 02/10/2015 ECU start the handler monitoring
    		// ---------------------------------------------------------------------
    		adapterHandler.sendEmptyMessage (StaticData.MESSAGE_REFRESH);
    		// ---------------------------------------------------------------------
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 02/10/2015 ECU tell the handler to stop looping
    		// ---------------------------------------------------------------------
    		adapterHandler.sendEmptyMessage (StaticData.MESSAGE_FINISH);
    		// ---------------------------------------------------------------------
    	}
    }
	// =============================================================================
    
	// =============================================================================
	@SuppressLint ("HandlerLeak")
	public class AdapterHandler extends Handler
	{
    	boolean	keepRunning = false;
		// -------------------------------------------------------------------------
		// 02/10/2015 ECU created to provide an adapter handling facility
		// -------------------------------------------------------------------------
	    @Override
	    public void handleMessage (Message theMessage) 
	    {   
	       	// ---------------------------------------------------------------------
	    	// 02/10/2015 ECU change to switch on the type of message received
	    	//                which is in '.what'
	    	// ---------------------------------------------------------------------
	    	switch (theMessage.what)
	    	{
	    		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_FINISH:
        			// -------------------------------------------------------------
        			keepRunning = false;
        			sleep (500);
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_REFRESH:
        			keepRunning = true;
        			sleep (500);
        			break;
	        	// -----------------------------------------------------------------
	        	case StaticData.MESSAGE_SLEEP:
	        		// -------------------------------------------------------------
	        		if (keepRunning)
	        		{
	        			// ---------------------------------------------------------
	            		// 07/07/2016 ECU check the initial visibility of the timer button
	        			// 08/07/2016 ECU moved here from MESSAGE_REFRESH
	        			//            ECU put in the 'null' check
	            		// ---------------------------------------------------------
	        			if (timersButton != null)
	        				timersButton.setVisibility (ShowEPGActivity.checkTimerButtonVisibility());
	        			// ---------------------------------------------------------
	        			// 02/10/2015 ECU indicate that screen is to be refreshed
	        			// ---------------------------------------------------------
	        			listViewAdapter.notifyDataSetChanged ();
	        			// ---------------------------------------------------------
	        			// 02/10/2015 ECU wait a bit before checking again
	        			// ---------------------------------------------------------
	        			sleep (5000);
	        			// ---------------------------------------------------------
	        		}
	        		break;
	        	// -----------------------------------------------------------------
	        }
	    }
	    // =========================================================================
	    public void sleep (long delayMillis)
	    {	
	    	// ---------------------------------------------------------------------
	    	// 02/10/2015 ECU changed to use MESSAGE_SLEEP instead of 0
	    	// ---------------------------------------------------------------------
	    	this.removeMessages (StaticData.MESSAGE_SLEEP);
	    	sendMessageDelayed (obtainMessage (StaticData.MESSAGE_SLEEP), delayMillis);
	    	// ---------------------------------------------------------------------
	    }
	};
	// =============================================================================
}

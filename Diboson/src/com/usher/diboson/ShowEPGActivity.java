package com.usher.diboson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

public class ShowEPGActivity extends FragmentActivity 
{
	// =============================================================================
	// 22/09/2015 ECU created to display the current contents of the EPG
	// 09/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 31/10/2015 ECU a problem happened if the app wasn't running when an
	//                advance warning for a program should have been displayed.
	//                When the app started up the actioning of the alarm caused
	//                a NPE because it said trying to getSystemService on a null
	//                object at AlarmData.createAlarm. Put in check to catch the NPE
	//                and report the fact back.
	// 24/12/2015 ECU added the sorting of the TV channels before displaying
	// 19/02/2016 ECU sort out an 'endless loop' that could occur if this activity
	//                finds that the EPG is out of date and needs to be refreshed.
	//                If this were to cause a problem then the code could loop
	//                ad infinitum.
	// 22/01/2017 ECU try and maintain the 'selected' TV programs following a rebuild
	//                of the EPG data
	// 24/01/2017 ECU put in the 'long click' on the 'current tab' to display or not
	//                display the current date and time and to keep it updated
	// 14/02/2017 ECU try and remember where the user has scrolled to so that different
	//                programs can be sync-ed to this time when selected
	// 19/03/2017 ECU added the dialogue to remove a reminder if the user clicks on
	//                the 'alarm clock' icon
	// =============================================================================
	private final static String TAG = "ShowEPGActivity";
	// =============================================================================
	public static   Activity						activity;				// 28/06/2016 ECU added
	public static   int								alarmIndex;				// 17/10/2015 ECU added
	public static	String []						channelNames;			// 29/06/2016 ECU added
	public static   Context							context;
	public static   int								currentChannel;			// 26/09/2015 ECU added
	public static   boolean                 		dataChanged = false;	// 26/09/2015 ECU added
	public static 	ArrayList <String>				EPGdates;
	public static   String							filterString = null;	// 24/07/2016 ECU moved here from
																			//                EPGListViewAdapter
	public static   FragmentTabHost 				fragmentTabHost;
	public static   Long							scrolledTime = null;	// 14/02/2017 ECU added
																			// 17/02/2017 ECU changed from string
	public static   boolean							scrolledTimeAnnouncement = false;
																			// 19/02/2017 ECU added
	public static   int								scrolledTimeHour;		// 20/02/2017 ECU added	
	public static   int								scrolledTimeMinute;		// 20/02/2017 ECU added	
	public static   int								selectedAlarmIndex;		// 19/03/2017 ECU added
					int								selectedChannel;		// 28/06/2016 ECU added
	public static   String							selectedDate;			// 18/10/2015 ECU added
	public static   int                             selectedDateIndex;		// 18/10/2015 ECU added
	public static   int                             selectedEntryIndex;		// 18/10/2015 ECU added
	public static   int								selectedTVChannelIndex;	// 18/10/2015 ECU added
	public static  	ArrayList <TVChannel>			TVChannelsSelected = null;
																			// 14/10/2015 ECU set to 'null'
					ViewPager 						viewPager;
	// =============================================================================

	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			// ---------------------------------------------------------------------
			// 27/09/2015 ECU remember the context for future use
			// 28/06/2016 ECU added 'activity'
			// ---------------------------------------------------------------------
			activity	= this;
			context 	= this;
			// ---------------------------------------------------------------------
			// 30/09/2015 ECU indicate that data has not changed
			// 28/06/2016 ECU added the selected channel
			// 04/07/2016 ECU added the resetting of selected channels (see the
			//                KnownBugs file)
			// 24/07/2016 ECU added filterString
			// ---------------------------------------------------------------------
			dataChanged 		= false;
			filterString		= null;
			selectedChannel		= StaticData.NO_RESULT;
			TVChannelsSelected	= null;
			// ---------------------------------------------------------------------
			// 19/02/2016 ECU check whether this activity was reactivated 
			//                following an attempted refresh
			// 28/06/2016 ECU moved from lower down
			// ---------------------------------------------------------------------
			Bundle localExtras = getIntent().getExtras();
			// ---------------------------------------------------------------------
			// 28/06/2016 ECU check for any supplied parameters
			// ---------------------------------------------------------------------
			if (localExtras != null) 
			{
				// -----------------------------------------------------------------
				// 19/02/2016 ECU check if an EPG refresh was requested
				// -----------------------------------------------------------------
			    selectedChannel	= localExtras.getInt (StaticData.PARAMETER_CHANNEL,StaticData.NO_RESULT); 
			    // -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 23/09/2015 ECU get the currently selected TV channels
			// ---------------------------------------------------------------------
			TVChannelsSelected = TVChannelsActivity.getTVChannelsSelected (this); 
			// ---------------------------------------------------------------------
			// 24/12/2015 ECU sort the retrieved channels
			// 26/06/2016 ECU added the condition on ...Sorted
			// ---------------------------------------------------------------------
			if (PublicData.storedData.tvChannelsSorted)
				Collections.sort (TVChannelsSelected);
			// ---------------------------------------------------------------------
			// 29/06/2016 ECU remember the shortened names of the channels
			// ---------------------------------------------------------------------
			channelNames = buildChannelsList ();
			// ---------------------------------------------------------------------
			// 28/06/2016 ECU check if a particular channel is to be displayed
			// ---------------------------------------------------------------------
			if (selectedChannel != StaticData.NO_RESULT)
			{
				TVChannel localSelectedChannel = TVChannelsSelected.get (selectedChannel);
				TVChannelsSelected.clear();
				TVChannelsSelected.add (localSelectedChannel);
			}
			// ---------------------------------------------------------------------
			// 02/10/2015 ECU if there are no 'selected' TV channels then cannot continue
			// ---------------------------------------------------------------------
			if (TVChannelsSelected.size() > 0)
			{
				// -----------------------------------------------------------------
				// 16/10/2015 ECU at this point the channels do not have any EPG data
				//                so get that data
				// -----------------------------------------------------------------
				for (int theChannel = 0; theChannel < TVChannelsSelected.size(); theChannel++)
				{
					// -------------------------------------------------------------
					// 16/10/2015 ECU get the EPG data from disk and store in the object
					// -------------------------------------------------------------
					TVChannelsSelected.get (theChannel).readFromDisk (this);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 22/01/2017 ECU check if there are any stored alarms
				//            ECU run as a thread so as not to hold things up
				// -----------------------------------------------------------------
				Thread thread = new Thread()
				 {
					@Override
					public void run()
					{
						// ---------------------------------------------------------
						EPGCheckForExistingAlarms (true);
						// ---------------------------------------------------------
					}
				 };
				 thread.start();  
				// -----------------------------------------------------------------
				// 25/09/2015 ECU get the dates to which the EPG relates - the '0' 
				//                indicates to use the first entry in the EPG array
				// 20/02/2016 ECU work out which of the EPG channels has the most dates
				// -----------------------------------------------------------------
				EPGdates = new ArrayList <String> ();
				// -----------------------------------------------------------------
				for (int theChannel = 0; theChannel < TVChannelsSelected.size(); theChannel++)
				{
					// -------------------------------------------------------------
					// 20/02/2016 ECU check if this channel has more entries than
					//                that stored - if yes then store in the array
					//                which will be used for the display
					//--------------------------------------------------------------
					if (getEPGDatesSize (theChannel) > EPGdates.size())
					{
						// ---------------------------------------------------------
						// 20/02/2016 ECU set the EPG dates that will be used for the
						//                display to the channel with the most dates
						//                stored
						// ---------------------------------------------------------
						EPGdates = getEPGDates (theChannel);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 20/02/2016 ECU decide whether the current EPG is up to date
				// -----------------------------------------------------------------
				// 19/10/2015 ECU get the current date in the correct format
				// 27/10/2016 ECU changed to use dateSimpleFormat rather than creating
				//                 a new one
				// -----------------------------------------------------------------
				String localCurrentDate = PublicData.dateSimpleFormat.format (Calendar.getInstance().getTimeInMillis());
				// -----------------------------------------------------------------
				boolean localEPGRefreshNeeded = false;
				// -----------------------------------------------------------------
				// 20/02/2016 ECU Check whether today's date can be found in the
				//				  stored EPG entries. If it is after the last
				//           	  entry then a refresh is required. If it is
				//                before the first entry then display a warning 
				//                and then display the EPG entries 'as is'
				// -----------------------------------------------------------------
				long localCurrentTime = Utilities.getTime (localCurrentDate);
				// -----------------------------------------------------------------
				if (EPGdates.size() > 0)
				{
					long localFirstDate   = Utilities.getTime (EPGdates.get (0));
					long localLastDate    = Utilities.getTime (EPGdates.get (EPGdates.size()-1));
					// -------------------------------------------------------------
					// 20/02/2016 ECU now check if the current EPG data can be used
					// -------------------------------------------------------------		
					if (localCurrentTime < localFirstDate)
					{
						// ---------------------------------------------------------
						// 20/02/2016 ECU the current date is before the stored EPG data
						//                so do nothing other than to get the first date
						//                displayed
						// ---------------------------------------------------------
						localCurrentDate = EPGdates.get (0);
						// ---------------------------------------------------------
					}
					else
					if (localCurrentTime > localLastDate)
					{
						// ---------------------------------------------------------
						// 20/02/2016 ECU indicate that a refresh is required
						// ---------------------------------------------------------
						localEPGRefreshNeeded = true;
						// ---------------------------------------------------------
					}
				}
				else
				{
					// -------------------------------------------------------------
					// 20/02/2016 ECU the selected channel(s) have no EPG entries so
					//                try a refresh
					// -------------------------------------------------------------
					localEPGRefreshNeeded = true;
					// -------------------------------------------------------------
					// 20/02/2016 ECU tell the user what is going on
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString (R.string.epg_selected_channels_have_no_epg_entries),true);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 25/09/2015 ECU declare the 'tab host' that will be used to display the
				//                dates available in the EPG
				// -----------------------------------------------------------------
				fragmentTabHost = new FragmentTabHost (this);
				// -----------------------------------------------------------------
				// 25/09/2015 ECU display the tabs
				// -----------------------------------------------------------------
				setContentView (fragmentTabHost);
				// -----------------------------------------------------------------
				fragmentTabHost.setup (this,getSupportFragmentManager (),R.layout.epg_tabhost);
				// -----------------------------------------------------------------
				// 25/09/2015 ECU populate the tabs with the available dates and use those
				//                dates as the 'tags'
				// -----------------------------------------------------------------
				String localTabDate;

				for (int date = 0; date < EPGdates.size(); date++)
				{
					// -------------------------------------------------------------
					// 19/10/2015 ECU get the current date for the tab
					// -------------------------------------------------------------
					localTabDate = EPGdates.get (date);
					// -------------------------------------------------------------
					// 19/10/2015 ECU now set the text on the tab
					// -------------------------------------------------------------
					fragmentTabHost.addTab (fragmentTabHost.newTabSpec (localTabDate).setIndicator (Utilities.getDayOfWeek (localTabDate) + 
												StaticData.NEWLINE + 
													EPGdates.get (date)),
											TVChannelFragment.class, 
											null);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 14/02/2017 ECU put in a listener to detect when a different date
				//                if selected
				// -----------------------------------------------------------------
				fragmentTabHost.setOnTabChangedListener (new OnTabChangeListener()
				{
					@Override
					public void onTabChanged (String tabId) 
					{
						// ---------------------------------------------------------
						// 14/02/2017 ECU the date has been changed
						// ---------------------------------------------------------
						// ---------------------------------------------------------
					}
				});
				// -----------------------------------------------------------------
				// 19/10/2015 ECU if the current date is not in the stored EPG then
				//                will need to get a new copy
				// 20/02/2016 ECU Note - this is not strictly true because a refresh
				//                should only be requested if the current date is
				//                after the last one stored as an EPG entry
				//            ECU changed to use the new logic based on checking
				//                the current date against the first and last stored
				//                dates
				// -----------------------------------------------------------------
				if (!localEPGRefreshNeeded)
				{
					// -------------------------------------------------------------
					// 19/10/2015 ECU the EPG is valid for the current date
					// -------------------------------------------------------------
					// 25/09/2015 ECU try and get the tabs to scroll horizontally
					// -------------------------------------------------------------
					TabWidget tabWidget = (TabWidget) findViewById (android.R.id.tabs);
					LinearLayout linearLayout = (LinearLayout) tabWidget.getParent ();
					// -------------------------------------------------------------
					// 25/01/2017 ECU set up the textview that will, if required, display
					//                and update the current date and time
					// -------------------------------------------------------------
					TextView currentTimeTextView = new TextView (this);
					// -------------------------------------------------------------
					// 25/01/2017 ECU set the required characteristics of the text view
					// -------------------------------------------------------------
					currentTimeTextView.setTextSize (TypedValue.COMPLEX_UNIT_PX,
							                         getResources ().getDimension (R.dimen.default_font_size));
					currentTimeTextView.setGravity (Gravity.CENTER);
					currentTimeTextView.setText (getString (R.string.epg_long_click));
					currentTimeTextView.setLayoutParams (new FrameLayout.LayoutParams (FrameLayout.LayoutParams.MATCH_PARENT,
							                                                           FrameLayout.LayoutParams.WRAP_CONTENT));
					currentTimeTextView.setBackgroundDrawable(getResources().getDrawable(R.drawable.textview_shape));
					// -------------------------------------------------------------
					// 26/01/2017 ECU add the text view into the overall layout
					// -------------------------------------------------------------
					linearLayout.addView (currentTimeTextView,0);
					// -------------------------------------------------------------
					// 25/01/2017 ECU make sure that the date and time is updated
					// -------------------------------------------------------------
					Utilities.dateTimeDisplay (currentTimeTextView, 
							   				   StaticData.EPG_TAB_DATE_FORMAT,
							   				   5000,
							   				   1000,
							   				   false); 
					// -------------------------------------------------------------
					// 25/01/2017 ECU set up the long click listener for the text view
					// -------------------------------------------------------------
					currentTimeTextView.setOnLongClickListener (new View.OnLongClickListener() 
					{
						@Override
						public boolean onLongClick (View theView) 
						{
							// -----------------------------------------------------
							// 24/01/2017 ECU check if the date is to be displayed or not
							// -----------------------------------------------------
							if (PublicData.messageHandler.dateTimeTextView != null)
							{
								// -------------------------------------------------
								// 24/01/2017 ECU stop the display of the update
								//                date and time
								// 25/01/2017 ECU changed so that to pass through
								//                the required visibility
								// -------------------------------------------------
								Message localMessage = PublicData.messageHandler.obtainMessage (StaticData.MESSAGE_DISPLAY_DATE_STOP);
								localMessage.arg1 	 = View.GONE;
								PublicData.messageHandler.sendMessage (localMessage);
								// -------------------------------------------------
							}
							// -----------------------------------------------------
							return true;
						}
					});
					// -----------------------------------------------------------------
					HorizontalScrollView horizontalScrollView = new HorizontalScrollView (this);
					horizontalScrollView.setLayoutParams (new FrameLayout.LayoutParams (
														 FrameLayout.LayoutParams.MATCH_PARENT,
														 FrameLayout.LayoutParams.WRAP_CONTENT));
					// -----------------------------------------------------------------
					// 25/01/2017 ECU changed from 0
					// -----------------------------------------------------------------
					linearLayout.addView (horizontalScrollView,1);			
					linearLayout.removeView (tabWidget);
					// -----------------------------------------------------------------
			        horizontalScrollView.addView (tabWidget);
					horizontalScrollView.setHorizontalScrollBarEnabled (false);
					horizontalScrollView.setBackgroundColor (Color.YELLOW);
					// -------------------------------------------------------------
					// 25/09/2015 ECU want to set todays tab as current
					// 19/10/2015 ECU changed to use local variable
					// 22/02/2016 ECU Note - the correct tab is selected but it is
					//                not brought into view if not already there. Do
					//                NOT know how to do this - have tried scrolling
					//                with no success.
					// 20/01/2017 ECU the textview that corresponds to the text on
					//                the selected tab is :-
					//					(TextView) fragmentTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title)
					// -------------------------------------------------------------
					fragmentTabHost.setCurrentTabByTag (localCurrentDate);
					// -------------------------------------------------------------
					// 19/02/2016 ECU if there is no access to a remote controller
					//                then inform the user
					// 28/06/2016 ECU put in the check on 'localExtras' so that do
					//                not annoy the user with too many messages
					// -------------------------------------------------------------
					if (!PublicData.blueToothService && (localExtras == null))
					{	
						// ---------------------------------------------------------
						// 19/02/2016 ECU there is no remote controller
						// ---------------------------------------------------------
						if (PublicData.remoteControllerServer == null)
							Utilities.popToastAndSpeak (getString (R.string.epg_no_remote_controller), true);
						else
						{
							// -----------------------------------------------------
							// 27/02/2016 ECU this device cannot access a remote
							//                controller but there is a server so
							//                tell the user
							// -----------------------------------------------------
							Utilities.popToastAndSpeak (String.format(getString (R.string.epg_no_remote_controller_but_server),
														Utilities.GetDeviceName(PublicData.remoteControllerServer)),true);
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 19/02/2016 ECU the current EPG seems to be out of date so
					//                want to regenerate it by calling the
					//                TVChannelsActivity which will reactivate this
					//                activity. There is the possibility that this 
					//                could cause an 'endless' loop so try and detect
					//                and handle this situation
					// -------------------------------------------------------------
					boolean reactivatedByEPG = false;
					// -------------------------------------------------------------
					if (localExtras != null) 
					{
						// ---------------------------------------------------------
						// 19/02/2016 ECU check if an EPG refresh was requested
						// ---------------------------------------------------------
					    reactivatedByEPG	= localExtras.getBoolean (StaticData.PARAMETER_EPG_REFRESH); 
					    // ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 19/02/2016 ECU at this point 'reactivatedByEPG will be false
					//                if not being reactivated by the TVChannelsActivity
					//                and true if it has
					// -------------------------------------------------------------
					// 19/02/2016 ECU set up the intent for activity to be started
					// -------------------------------------------------------------
			    	Intent localIntent = new Intent (getBaseContext(),TVChannelsActivity.class);
			    	// -------------------------------------------------------------
			    	// 19/02/2016 ECU decide what the activity is to do
			    	// -------------------------------------------------------------
			    	if (reactivatedByEPG)
			    	{
			    		// ---------------------------------------------------------
			    		// 19/02/2016 ECU being reactivated by EPG so break the
			    		//                'endless loop'
			    		// ---------------------------------------------------------
			    		Utilities.popToastAndSpeak (getString (R.string.epg_needs_refreshing_problem),true);
			    		// ---------------------------------------------------------
			    	}
			    	else
			    	{
			    		// ---------------------------------------------------------
			    		// 19/02/2016 ECU indicate that trying for a refresh for the
			    		//                first time
			    		// ---------------------------------------------------------
			    		Utilities.popToastAndSpeak (getString (R.string.epg_needs_refreshing),true);
			    		// ---------------------------------------------------------
						// 19/10/2015 ECU indicate that a refresh of the EPG is required
						// ---------------------------------------------------------
						localIntent.putExtra (StaticData.PARAMETER_EPG_REFRESH,true);
						// ---------------------------------------------------------
			    	}
			    	// -------------------------------------------------------------
					// 19/10/2015 ECU start up the activity and finish this
			    	//                one
					// -------------------------------------------------------------
			    	startActivity (localIntent);
					// -------------------------------------------------------------
			    	finish ();
			    	// -------------------------------------------------------------
				}
			}
			else
			{
				// -----------------------------------------------------------------
				// 02/10/2015 ECU indicate there are no channels and then finish this
				//                activity
				// -----------------------------------------------------------------
				Utilities.popToast (getString (R.string.epg_no_channels));
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	@Override
	public void onBackPressed()
	{
		// -------------------------------------------------------------------------
		// 24/07/2016 ECU created to handled the 'back' key
	    // -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
	    super.onBackPressed();			
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onDestroy()
	{	
		// -------------------------------------------------------------------------
		// 26/09/2015 ECU added
		//            ECU write the 'selected' channels to file
		// -------------------------------------------------------------------------
		if (dataChanged)
		{
			//----------------------------------------------------------------------
			Thread thread = new Thread()
			 {
				@Override
				public void run()
				{
					// -------------------------------------------------------------
					// 30/09/2015 ECU write updated data to disk
					// 02/10/2015 ECU changed to use the new method
					// -------------------------------------------------------------
					updateDiskVersion (context);
					// -------------------------------------------------------------
				}
			 };
			 thread.start();  
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 21/01/2017 ECU make sure the date updating stops
		// -------------------------------------------------------------------------
		PublicData.messageHandler.sendEmptyMessage (StaticData.MESSAGE_DISPLAY_DATE_STOP);
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void EPGActionAlarm (Context theContext,EPGAlarm theEPGAlarm)
	{
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU this is called up when the EPG alarm is actioned
		// -------------------------------------------------------------------------
		Utilities.LogToProjectFile (TAG,theEPGAlarm.EPGEntry.Print());
		// -------------------------------------------------------------------------
		// 30/10/2015 ECU inform the user that the program has started
		// --------------------------------------------------------------------------
		Utilities.popToastAndSpeak ("The program\n\n" + theEPGAlarm.EPGEntry.PrintProgram() + 
										" on " + theEPGAlarm.TVChannelName + 
										"\n\n has started",true);
		// -------------------------------------------------------------------------
		// 01/10/2015 ECU check if the data is set in the list
		// -------------------------------------------------------------------------
		if (TVChannelsSelected == null || TVChannelsSelected.size () == 0)
		{
			// ---------------------------------------------------------------------
			// 01/10/2015 ECU do not appear to have the data so get it from disk
			// ---------------------------------------------------------------------
			TVChannelsSelected = TVChannelsActivity.getTVChannelsSelected (theContext); 
			// ---------------------------------------------------------------------
			// 17/10/2015 ECU at this point just have the basic framework - need to
			//                get the actual EPG entries
			// ----------------------------------------------------------------------
			for (int theChannel = 0; theChannel < TVChannelsSelected.size(); theChannel++)
			{
				// -----------------------------------------------------------------
				// 16/10/2015 ECU get the EPG data from disk and store in the object
				// -----------------------------------------------------------------
				TVChannelsSelected.get(theChannel).readFromDisk (theContext);
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 18/10/2015 ECU use the alarm data to deselect the program and to send
		//                the codes to the remote device
		// -------------------------------------------------------------------------
		updateEPGEntryFromAlarmData (theContext,theEPGAlarm,false,true);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void EPGAdvanceWarning (Context theContext,EPGAlarm theEPGAlarm)
	{
		// -------------------------------------------------------------------------
		// 15/10/2015 ECU created to handle the alarm that is set
		//                StaticData.EPG_ADVANCE_WARNING milliseconds before an
		//                impending alarm for a selected EPG entry
		//            ECU changed to use PrintProgram
		// 30/10/2015 ECU change the message
		// 31/10/2015 ECU have minute or minutes depending on the advanceCounter
		// 31/03/2016 ECU use the method to optionally add a trailing 's'
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak ("The program\n\n" + theEPGAlarm.EPGEntry.PrintProgram() + 
										" on " + theEPGAlarm.TVChannelName + 
										"\n\n will start in " + theEPGAlarm.advanceCounter + " minute" +
										Utilities.AddAnS (theEPGAlarm.advanceCounter),true);
		// -------------------------------------------------------------------------
		// 30/10/2015 ECU check if the warning alarm needs to be repeated
		// -------------------------------------------------------------------------
		if (theEPGAlarm.advanceCounter > 1)
		{
			// ---------------------------------------------------------------------
			// 30/10/2015 ECU work out the time for the next reminder
			//            ECU changed to use the method
			// 26/01/2017 ECU added the true flag to indicate that the advance
			//                counter is to be decremented
			// ---------------------------------------------------------------------
			long theCurrentTime = theEPGAlarm.NextWarningTime (true);
			// ---------------------------------------------------------------------
			AlarmData alarmData = new AlarmData (StaticData.ALARM_ADVANCE_EPG,
					 							 AlarmData.getCalendar (theCurrentTime),
					 							 theCurrentTime,
					 							 StaticData.REQUEST_CODE_EPG,
					 							 (Object) theEPGAlarm);
			// ---------------------------------------------------------------------
			// 31/10/2015 ECU put in the check on whether the alarm was created
			//            correctly
			// ---------------------------------------------------------------------
			if (alarmData.createAlarm (context))
			{
				// -----------------------------------------------------------------
				// 29/09/2015 ECU now add the alarm into the stored list
				// -----------------------------------------------------------------
				PublicData.alarmData.add (alarmData);
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void EPGAlarmHandler (int theTVChannel,String theDate,EPGEntry theEPGEntry)
	{
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU called to handle alarms depending on the 'select' state
		//                in the EPG entry
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU sort out any associated alarms
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU get the date and time from the EPG which will be used as
		//                the ID
		// -------------------------------------------------------------------------
		long localDate = Utilities.getTime (theEPGEntry.fields [StaticData.EPG_DATE],theEPGEntry.fields [StaticData.EPG_START_TIME]);
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU the processing time means that the time in milliseconds
		//                derived from the same parameters may vary by a few milliseconds
		//                so just coarsen the date - only significant to alarms
		// 01/10/2015 ECU took out the coarsen bit because adding 'clear' command
		//                in the 'getTime' method returns a consistent value
		// -------------------------------------------------------------------------
		//localDate = ((localDate / 1000) * 1000) + Utilities.getRandomNumber (500);
		// -------------------------------------------------------------------------
		// 30/09/2015 ECU set up the object that will be passed to the alarm
		//                manager
		// -------------------------------------------------------------------------
		EPGAlarm EPGAlarm = new EPGAlarm (theEPGEntry,TVChannelsSelected.get (theTVChannel).channelName);
		// -------------------------------------------------------------------------
		// 18/10/2015 ECU if selected is now false then want to set an alarm
		//                if true then program has been deselected
		// -------------------------------------------------------------------------
		if (!theEPGEntry.selected)
		{
			// ---------------------------------------------------------------------
			// 17/10/2015 ECU check whether there is already an alarm set at the
			//                same time
			// 22/01/2017 ECU added the _EPG argument
			// ---------------------------------------------------------------------
			alarmIndex = AlarmActions.checkForAnAlarm (context,localDate,StaticData.ALARM_ACTION_EPG);
			// ---------------------------------------------------------------------
			if (alarmIndex == StaticData.NO_RESULT)
			{
				// -----------------------------------------------------------------
				// 17/10/2015 ECU this alarm is unique so can just add it and its
				//                warning
				// -----------------------------------------------------------------
				setAlarms (localDate,EPGAlarm);
				// -----------------------------------------------------------------
				// 18/10/2015 ECU now indicate that program has been selected
				// -----------------------------------------------------------------
				theEPGEntry.selected = true;
				// -----------------------------------------------------------------
				// 18/10/2015 ECU need to update the data
				// -----------------------------------------------------------------
				updateEPGEntry (theTVChannel,theDate,selectedEntryIndex,theEPGEntry);
				// ------------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 17/10/2015 ECU there is already an alarm at this date and time
				//                tell the user and let them make the decision
				//                as to whether to create this alarm or not
				// -----------------------------------------------------------------
				EPGAlarm localEPGAlarm = (EPGAlarm) PublicData.alarmData.get (alarmIndex).object;
				// -----------------------------------------------------------------
				// 17/10/2015 ECU let the user decide
				// 26/01/2017 ECU changed to use resources and format
				// -----------------------------------------------------------------
				DialogueUtilities.yesNo (context,
										 context.getString (R.string.duplicate_alarm),
										 String.format (context.getString (R.string.duplicate_alarm_format),localEPGAlarm.Print()),
										 (Object) EPGAlarm,
										 Utilities.createAMethod (ShowEPGActivity.class,"YesMethod",(Object) null),
										 Utilities.createAMethod (ShowEPGActivity.class,"NoMethod",(Object) null)); 
				// -----------------------------------------------------------------
			}
		}	
		else
		{
			// ---------------------------------------------------------------------
			// 29/09/2015 ECU want to cancel any alarm for this entry
			//            ECU add 'true' so that the alarm is cancelled as well
			// 01/10/2015 ECU the '+ theTVChannel' is there as it makes the alarm
			//                ID unique
			// 17/10/2015 ECU remove the '+ theTVChannel' as there is no overlap
			//                between channels
			// ---------------------------------------------------------------------
			AlarmActions.deleteAlarmFromList (context,localDate,true);
			// ---------------------------------------------------------------------
			// 16/10/2015 ECU and delete the advance warning
			// 26/01/2017 ECU added _REMINDER_GAP see notes above when setting the alarm
			//            ECU changed to use Next.... with false to select the current
			//                advance warning
			// ---------------------------------------------------------------------
			AlarmActions.deleteAlarmFromList (context,EPGAlarm.NextWarningTime (false),true);
			// -----------------------------------------------------------------
			// 18/10/2015 ECU now indicate that program has been deselected
			// -----------------------------------------------------------------
			theEPGEntry.selected = false;
			// ---------------------------------------------------------------------
			// 18/10/2015 ECU need to update the data
			// ---------------------------------------------------------------------
			updateEPGEntry (theTVChannel,theDate,selectedEntryIndex,theEPGEntry);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	String [] buildChannelsList ()
	{
		// -------------------------------------------------------------------------
		// 28/06/2016 ECU created to return a list of the select channels
		// -------------------------------------------------------------------------
		String [] localNames = new String [TVChannelsSelected.size()];
		// -------------------------------------------------------------------------
		for (int theChannel = 0; theChannel < TVChannelsSelected.size(); theChannel++)
			localNames [theChannel] = TVChannelsSelected.get (theChannel).channelName;
		// -------------------------------------------------------------------------
		// 28/06/2016 ECU return the generated list
		// -------------------------------------------------------------------------
		return localNames;
		// -------------------------------------------------------------------------
	} 
	// =============================================================================
	public static int checkTimerButtonVisibility ()
	{
		// -------------------------------------------------------------------------
		// 07/07/2016 ECU created to set the visibility of the timer button
		// -------------------------------------------------------------------------
		if (PublicData.alarmData.size() > 0)
		{
			for (int alarmIndex = 0; alarmIndex < PublicData.alarmData.size(); alarmIndex++)
			{
				if (PublicData.alarmData.get(alarmIndex).action == StaticData.ALARM_ACTION_EPG)
				{
					// -------------------------------------------------------------
					// 07/07/2016 ECU have found a stored EPG alarm
					// -------------------------------------------------------------
					return View.VISIBLE;
					// -------------------------------------------------------------
				}
			}
		} 
		// -------------------------------------------------------------------------
		// 07/07/2016 ECU at this point then the button must be invisible
		// -------------------------------------------------------------------------
		return View.INVISIBLE;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void EPGCheckForAlarms ()
	{
		// -------------------------------------------------------------------------
		// 06/07/2016 ECU check if there are any EPG alarms outstanding
		// -------------------------------------------------------------------------
		if (PublicData.alarmData.size() > 0)
		{
			ArrayList<ListItem> 	listItems = new ArrayList<ListItem>();
			
			for (int alarmIndex = 0; alarmIndex < PublicData.alarmData.size(); alarmIndex++)
			{
				if (PublicData.alarmData.get(alarmIndex).action == StaticData.ALARM_ACTION_EPG)
				{
					// -------------------------------------------------------------
					// 06/07/2016 ECU get the EPG details from the stored alarm
					// -------------------------------------------------------------
					EPGAlarm localAlarm = (EPGAlarm) PublicData.alarmData.get (alarmIndex).object;
					// -------------------------------------------------------------
					// 07/07/2016 ECU build up the list needed by the adapter
					// -------------------------------------------------------------
					listItems.add (new ListItem (null,
							                     localAlarm.TVChannelName,
							                     localAlarm.EPGEntry.ProgramName(),
							                     localAlarm.EPGEntry.ProgramDetails(),
							                     alarmIndex));
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
			// 06/07/2016 ECU decide if there are any alarms to display
			// ---------------------------------------------------------------------
			if (listItems.size() > 0)
			{
				// -----------------------------------------------------------------
				// 06/07/2016 ECU there are timers so display them but take no action
				// -----------------------------------------------------------------
				DialogueUtilities.adapterListChoice (context,
						 							 R.layout.epg_alarm_row,
						 							 String.format (context.getString(R.string.epg_timer_title_format),listItems.size()),
											         listItems,
											         Utilities.createAMethod (ShowEPGActivity.class,"SelectedEPGAlarm",0),
											         context.getString (R.string.cancel),
											         null);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 06/07/2016 ECU tell the user that there are no EPG timer alarms
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (context.getString (R.string.epg_no_outstanding_timers),true);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 06/07/2016 ECU tell there user that there no general alarms
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (context.getString (R.string.epg_no_stored_alarms),true);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void deleteEPGAlarms (EPGAlarm theEPGAlarm)
	{
		// -------------------------------------------------------------------------
		// 19/03/2017 ECU created to use the specified EPG alarm to deselect the
		//                specified EPG entry and to cancel the associated alarms
		// -------------------------------------------------------------------------
		updateEPGEntryFromAlarmData (context,theEPGAlarm,false,false);
		// -------------------------------------------------------------------------
		// 19/03/2017 ECU delete the alarms from the list - the 'true' indicates that
		//                existing alarms are to be cancelled
		// -------------------------------------------------------------------------
		AlarmActions.deleteAlarmFromList (context,theEPGAlarm.date,true);
		AlarmActions.deleteAlarmFromList (context,theEPGAlarm.NextWarningTime (false),true);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void EPGCheckForExistingAlarms (boolean theStateToSet)
	{
		// -------------------------------------------------------------------------
		// 22/01/2017 ECU created to scan through the existing alarms and to set
		//                the associated EPG entry to the required state
		// -------------------------------------------------------------------------
		if (PublicData.alarmData.size() > 0)
		{
			for (int alarmIndex = 0; alarmIndex < PublicData.alarmData.size(); alarmIndex++)
			{
				if (PublicData.alarmData.get(alarmIndex).action == StaticData.ALARM_ACTION_EPG)
				{
					// -------------------------------------------------------------
					// 22/01/2017 ECU have found a stored EPG alarm
					// -------------------------------------------------------------
					updateEPGEntryFromAlarmData (context,(EPGAlarm) PublicData.alarmData.get (alarmIndex).object,theStateToSet,false);
					// -------------------------------------------------------------
				}
			}
		} 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	ArrayList<String> getEPGDates (int theChannelToUse)
	{
		// -------------------------------------------------------------------------
		// 25/09/2015 ECU created to return a list of the dates covered by the
		//                current EPG. Will assume that all selected channels
		//                have the same date range so use the first entry
		// -------------------------------------------------------------------------
		ArrayList <String>	localDates = new ArrayList<String> ();
		// -------------------------------------------------------------------------
		// 30/09/2015 ECU put in check on '... ted != null'
		// 16/10/2015 ECU use the updated code
		// 20/02/2016 ECU use the ...Size method to return the size of dates stored
		//                - this method does error checking
		// -------------------------------------------------------------------------
		int localEPGEntriesSize = getEPGDatesSize (theChannelToUse);
		// -------------------------------------------------------------------------
		for (int entry = 0; entry < localEPGEntriesSize; entry++)
		{
			// ---------------------------------------------------------
			// 25/09/2015 ECU date has changed so store in the list
			// ---------------------------------------------------------
			try
			{
				localDates.add (TVChannelsSelected.get(theChannelToUse).EPGEntries.get(entry).get(0).fields [StaticData.EPG_DATE]);
			}
			catch (Exception theException)
			{
			}
			// ---------------------------------------------------------							
		}
		// -------------------------------------------------------------------------
		return localDates;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	int getEPGDatesSize (int theChannelToUse)
	{
		// -------------------------------------------------------------------------
		// 20/02/2016 ECU created to return the number of dates stored for an EPG
		//                channel
		// -------------------------------------------------------------------------
		// 30/09/2015 ECU put in check on '... ted != null'
		// 16/10/2015 ECU use the updated code
		// -------------------------------------------------------------------------
		if (TVChannelsSelected != null &&
			TVChannelsSelected.size () > 0 &&
			TVChannelsSelected.get (theChannelToUse).EPGEntries != null && 
			TVChannelsSelected.get (theChannelToUse).EPGEntries.size() > 0)
		{
			return TVChannelsSelected.get(theChannelToUse).EPGEntries.size ();
		}
		// -------------------------------------------------------------------------
		return 0;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void refreshEPG (int theChannel)
	{
		// -------------------------------------------------------------------------
		// 19/10/2015 ECU terminate this activity
		// 29/06/2016 ECU moved the 'finish' from after the restart
		// 30/06/2016 ECU the code here used to be in TVEPGFragment
		// -------------------------------------------------------------------------
		activity.finish ();
		// -------------------------------------------------------------------------
		// 28/06/2016 ECU restart the EPG with the selected channel
		// 29/06/2016 ECU indicate NEW_TASK
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (ShowEPGActivity.context,ShowEPGActivity.class);
		localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		localIntent.putExtra (StaticData.PARAMETER_CHANNEL,theChannel);
		ShowEPGActivity.context.startActivity (localIntent);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void remoteController (Context theContext,int theChannel)
	{
		// -------------------------------------------------------------------------
		// 19/02/2016 ECU created to select the required channel using the remote
		//                controller - if it is available
		// -------------------------------------------------------------------------
		// 18/10/2015 ECU decide whether to send data to the remote
		//                device
		// -------------------------------------------------------------------------
		// 18/10/2015 ECU want to send data to remote device if
		//                the service is running
		// -------------------------------------------------------------------------
		if (PublicData.blueToothService)
		{
			// ---------------------------------------------------------------------
			// 12/10/2015 ECU have a device so send it the data
			// ---------------------------------------------------------------------
			new BlueToothServiceUtilities (theContext,Television.REMOTE_SAMSUNG_TV,
					TVChannelsSelected.get (theChannel).channelName);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 27/02/2016 ECU check if there is a remote controller server defined
			// ---------------------------------------------------------------------
			if (PublicData.remoteControllerServer != null)
			{	
				// -----------------------------------------------------------------
				// 27/02/2016 ECU send a request to the defined server
				// -----------------------------------------------------------------
				Utilities.sendSocketMessageSendTheObject (theContext,
														  PublicData.remoteControllerServer, 
														  PublicData.socketNumberForData,
														  StaticData.SOCKET_MESSAGE_OBJECT, 
														  new RemoteControllerRequest (Television.REMOTE_SAMSUNG_TV,
																                       TVChannelsSelected.get(theChannel).channelName));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static boolean selectEPGEntry (int theChannel,String theDate,EPGEntry theEPGEntry)
	{
		// -------------------------------------------------------------------------
		// 26/09/2015 ECU created to find a particular EPG entry. Don't do any
		//                validation at this point
		// 28/09/2015 ECU changed to search on theStartTime rather than an index
		//                because issues when a string has been searched for which
		//                messed up the entry
		//            ECU changed to pass the whole EPG entry so that more checking can
		//                be done
		// 19/02/2016 ECU when checking the program against the current time then
		//                the code could clearly be speeded up but as it is the user
		//                who has invoked the selection so should not be an issue
		// -------------------------------------------------------------------------
		EPGEntry localEPGEntry;
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 18/10/2015 ECU remember the TV channel index
			//			  ECU and date
			// ---------------------------------------------------------------------
			selectedDate			= theDate;
			selectedTVChannelIndex 	= theChannel;
			// ---------------------------------------------------------------------
			// 16/10/2015 ECU get the index that corresponds to specified date
			// ---------------------------------------------------------------------
			selectedDateIndex = TVChannelsSelected.get (theChannel).getDateIndex (theDate);
			// ---------------------------------------------------------------------
			// 16/10/2015 ECU if the date does not exist then ...NO_RESULT is returned
			//                otherwise the index is returned
			// ---------------------------------------------------------------------
			if (selectedDateIndex != StaticData.NO_RESULT)
			{
				for (selectedEntryIndex = 0; 
					 selectedEntryIndex < TVChannelsSelected.get (theChannel).EPGEntries.get(selectedDateIndex).size(); 
					 selectedEntryIndex++)
				{
					// -------------------------------------------------------------
					// 26/09/2015 ECU only interested in entries of the particular date
					// -------------------------------------------------------------
					localEPGEntry = TVChannelsSelected.get(theChannel).EPGEntries.get(selectedDateIndex).get(selectedEntryIndex);
					// -------------------------------------------------------------
					// 26/09/2015 ECU check date, start time and program title
					// 16/10/2015 ECU removed date as this is now indexed
					// -------------------------------------------------------------
					if ((localEPGEntry.fields.length == StaticData.EPG_FIELD_COUNT) && 
						(localEPGEntry.fields [StaticData.EPG_START_TIME].equalsIgnoreCase(theEPGEntry.fields[StaticData.EPG_START_TIME])) &&
						(localEPGEntry.fields [StaticData.EPG_PROGRAM_TITLE].equalsIgnoreCase(theEPGEntry.fields[StaticData.EPG_PROGRAM_TITLE])))
					{
						// ---------------------------------------------------------
						// 19/02/2016 ECU want to check the timings to see if
						//					1) the program has already been shown
						// 					2) the program is current being shown
						//                  3) the program is in the future in
						//                     which case an alarm should be set
						// ---------------------------------------------------------
						long currentTime      = Utilities.getAdjustedTime (true);
						long programStartTime = Utilities.getTime(theEPGEntry.fields[StaticData.EPG_DATE],theEPGEntry.fields[StaticData.EPG_START_TIME]);
						long programEndTime   = Utilities.getTime(theEPGEntry.fields[StaticData.EPG_DATE],theEPGEntry.fields[StaticData.EPG_END_TIME]);
						// ---------------------------------------------------------
						// 24/07/2016 ECU if the ..EndTime < ..StartTime then
						//                midnight has occurred
						// ---------------------------------------------------------
						if (programEndTime < programStartTime)
						{
							// -----------------------------------------------------
							// 24/07/2016 ECU midnight has occurred so adjust the
							//                EndTime
							// -----------------------------------------------------
							programEndTime += StaticData.MILLISECONDS_PER_DAY;
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
						// 19/02/2016 ECU check if the program has already happened
						// ---------------------------------------------------------
						if (currentTime > programEndTime)
						{
							// -----------------------------------------------------
							// 19/02/2016 ECU the program has already been shown
							// -----------------------------------------------------
							Utilities.popToastAndSpeak (context.getString (R.string.epg_program_already_shown),true);
							// -----------------------------------------------------
							// 19/02/2016 ECU indicate that display does need to change
							// -----------------------------------------------------
							return false;
							// -----------------------------------------------------
						}
						else
						if ((currentTime > programStartTime) && (currentTime < programEndTime))
						{
							// -----------------------------------------------------
							// 19/02/2016 ECU the program is currently being shown so
							//                want to change the channel
							// -----------------------------------------------------
							Utilities.popToastAndSpeak (context.getString ((PublicData.blueToothService ? R.string.epg_channel_will_be_changed
									                                                                    : R.string.epg_program_showing_now)),true);
							// -----------------------------------------------------
							// 19/02/2016 ECU now want to select the program using
							//                the remote controller
							// -----------------------------------------------------
							remoteController (context,theChannel);
							// -----------------------------------------------------
							// 19/02/2016 ECU indicate that display does need to change
							// -----------------------------------------------------
							return false;
							// -----------------------------------------------------
						}
						else
						{
							// -----------------------------------------------------
							// 19/02/2016 ECU the program is in the future so can set
							//                an alarm
							// -----------------------------------------------------
							// 26/09/2015 ECU have found the data so check if found the required
							//                entry
							// -----------------------------------------------------
							// 29/09/2015 ECU sort out any associated alarms
							// -----------------------------------------------------
							EPGAlarmHandler (theChannel,theDate,localEPGEntry);
							// -----------------------------------------------------
							// 19/02/2016 ECU indicate that display needs to change
							// -----------------------------------------------------
							return true;
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
			// 26/09/2015 ECU indicate that no match found
			// ---------------------------------------------------------------------
			return false;
		}
		catch (Exception theException)
		{
			return false;
		}
	}
	// =============================================================================
	public static void SelectedEPGAlarm (int theAlarmIndex)
	{
		// -------------------------------------------------------------------------
		// 07/07/2016 ECU created to handle the selection of a displayed EPG timer
		//                alarm
		// 19/03/2017 ECU give the option to delete this timer
		// -------------------------------------------------------------------------
		EPGAlarm localEPGAlarm = (EPGAlarm) PublicData.alarmData.get (theAlarmIndex).object;
		// -------------------------------------------------------------------------
		// 19/03/2017 ECU remember the index of this selected reminder
		// -------------------------------------------------------------------------
		selectedAlarmIndex	   = theAlarmIndex;
		// -------------------------------------------------------------------------
		// 19/03/2017 ECU ask user if the reminder is to be removed
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (context,
								 context.getString (R.string.epg_cancel_reminder),
								 String.format (context.getString (R.string.epg_cancel_alarm_format),localEPGAlarm.Print()),
								 (Object) localEPGAlarm,
								 Utilities.createAMethod (ShowEPGActivity.class,"SelectedYesMethod",(Object) null),
								 Utilities.createAMethod (ShowEPGActivity.class,"SelectedNoMethod",(Object) null));
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void SelectedNoMethod (Object theSelection)
  	{
		// -------------------------------------------------------------------------
		// 19/03/2017 ECU indicate that the chosen reminder has not been cancelled
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak (context.getString (R.string.epg_reminder_not_cancelled),true);
		// -------------------------------------------------------------------------
  	}
	// =============================================================================
	public static void SelectedYesMethod (Object theSelection)
  	{
		// -------------------------------------------------------------------------
		// 19/03/2017 ECU the selected reminder is to be cancelled
		// -------------------------------------------------------------------------
		deleteEPGAlarms ((EPGAlarm) theSelection);
		// -------------------------------------------------------------------------
		// 19/03/2017 ECU confirm the deletion of the reminder
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak (context.getString (R.string.epg_reminder_cancelled),true);
		// -------------------------------------------------------------------------
  	}
	// =============================================================================
	static void setAlarms (long theDate,EPGAlarm theEPGAlarm)
	{
		// -------------------------------------------------------------------------
		// 17/10/2015 ECU created to set alarms for the program who details are
		//                specified in the argument
		// -------------------------------------------------------------------------
		// 17/10/2015 ECU this alarm is unique so can just add it and its
		//                warning
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU want to set an alarm for this entry
		// 30/09/2015 ECU change to pass EPGAlarm
		// 01/10/2015 ECU add 'theTVChannel' to try and make the ID unique for
		//                programmes at the same time on different channels
		// 17/10/2015 ECU took out the '+ theTVChannel' because overlaps between
		//                channels is no longer allowed
		// -------------------------------------------------------------------------
		AlarmData alarmData = new AlarmData (StaticData.ALARM_ACTION_EPG,
											 AlarmData.getCalendar (theDate),
											 theDate,
											 StaticData.REQUEST_CODE_EPG,
											 (Object) theEPGAlarm);
		// -------------------------------------------------------------------------
		alarmData.createAlarm (context);
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU now add the alarm into the stored list
		// -------------------------------------------------------------------------
		PublicData.alarmData.add (alarmData);
		// -------------------------------------------------------------------------
		// 15/10/2015 ECU set up the advance warning for the above alarm - the
		//                warning will happen StaticData.EPG_ADVANCE_WARNING
		//                milliseconds before the above alarm
		// 26/01/2017 ECU a reminder could clash with a preceding 'start alarm' if
		//                the previous program is only EPG_ADVANCE_WARNING minutes
		//                long - so us _REMINDER_GAP to resolve any issues
		//            ECU use NextWarningTime and false to indicate that the
		//                advance counter should not be decremented
		// -------------------------------------------------------------------------
		long advanceTime = theEPGAlarm.NextWarningTime (false);
		// ---------------------------------------------------------------------
		alarmData = new AlarmData (StaticData.ALARM_ADVANCE_EPG,
				 				   AlarmData.getCalendar (advanceTime),
				 				   advanceTime,
				 				   StaticData.REQUEST_CODE_EPG,
				 				   (Object) theEPGAlarm);
		// -------------------------------------------------------------------------
		alarmData.createAlarm (context);
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU now add the alarm into the stored list
		// -------------------------------------------------------------------------
		PublicData.alarmData.add (alarmData);
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU make sure data is update on disk
		// -------------------------------------------------------------------------
		AsyncUtilities.writeObjectToDisk (PublicData.alarmFileName,PublicData.alarmData);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void updateDiskVersion (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 02/10/2015 ECU created to provide a method for general use to update the
		//                disk version of the 'selected TV channels' file
		// 16/10/2015 ECU change to write out the data 'by television channel'
		// -------------------------------------------------------------------------
		for (int theChannel = 0; theChannel < TVChannelsSelected.size(); theChannel++)
			TVChannelsSelected.get(theChannel).writeToDisk();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void updateEPGEntry (int theTVChannel,String theDate,int theEntryIndex,EPGEntry theEPGEntry)
	{
		// -------------------------------------------------------------------------
		// 18/10/2015 ECU created to update the EPG entry
		// -------------------------------------------------------------------------
		// 16/10/2015 ECU get the index that corresponds to the specified date for
		//                the specified TV channel
		// -------------------------------------------------------------------------
		int localDateIndex = TVChannelsSelected.get (theTVChannel).getDateIndex (theDate);
		// -------------------------------------------------------------------------
		// 18/10/2015 ECU now update the actual entry
		// -------------------------------------------------------------------------
		TVChannelsSelected.get (theTVChannel).EPGEntries.get(localDateIndex).set (theEntryIndex,theEPGEntry);
		// -------------------------------------------------------------------------
		// 18/10/2015 ECU indicate that the data has changed
		// -------------------------------------------------------------------------
		dataChanged = true;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void updateEPGEntryFromAlarmData (Context theContext,EPGAlarm theEPGAlarm,boolean theSelectedFlag,boolean theRemoteDevice)
	{
		// -------------------------------------------------------------------------
		// 18/10/2015 ECU created to use the alarm data to modify the selected flag
		// -------------------------------------------------------------------------
		for (int theChannel = 0; theChannel < TVChannelsSelected.size(); theChannel++)
		{
			// ---------------------------------------------------------------------
			// 30/09/2015 ECU check if name found
			// ---------------------------------------------------------------------
			if (TVChannelsSelected.get (theChannel).channelName.equalsIgnoreCase (theEPGAlarm.TVChannelName))
			{
				// -----------------------------------------------------------------
				// 16/10/2015 ECU get the entries for the specified date
				// -----------------------------------------------------------------
				int localDateIndex = TVChannelsSelected.get (theChannel).getDateIndex (theEPGAlarm.EPGEntry.fields[StaticData.EPG_DATE]);
				// -----------------------------------------------------------------
				// 22/01/2017 ECU if there are issues with the date index then
				//                NO_RESULT will be returned
				// -----------------------------------------------------------------
				if (localDateIndex != StaticData.NO_RESULT)
				{
					EPGEntry localEPGEntry;
					// -------------------------------------------------------------
					// 30/09/2015 ECU now look for the relevant entry
					// -------------------------------------------------------------
					for (int theEntry = 0; theEntry < TVChannelsSelected.get(theChannel).EPGEntries.get (localDateIndex).size (); theEntry++)
					{
						// ---------------------------------------------------------
						// 30/09/2015 ECU pull in the entry
						// ---------------------------------------------------------
						localEPGEntry = TVChannelsSelected.get(theChannel).EPGEntries.get(localDateIndex).get (theEntry);
						// ---------------------------------------------------------
						// 30/09/2015 ECU now check match on
						//                     date, start time, and program title
						// ---------------------------------------------------------
						if (localEPGEntry.fields [StaticData.EPG_DATE].equalsIgnoreCase(theEPGAlarm.EPGEntry.fields [StaticData.EPG_DATE]) &&
							localEPGEntry.fields [StaticData.EPG_START_TIME].equalsIgnoreCase(theEPGAlarm.EPGEntry.fields [StaticData.EPG_START_TIME]) &&
							localEPGEntry.fields [StaticData.EPG_PROGRAM_TITLE].equalsIgnoreCase(theEPGAlarm.EPGEntry.fields [StaticData.EPG_PROGRAM_TITLE]))
						{
							// -----------------------------------------------------
							// 30/09/2015 ECU have found a matching entry so set the
							//                flag
							// -----------------------------------------------------
							localEPGEntry.selected = theSelectedFlag;
							// -----------------------------------------------------
							// 30/09/2015 ECU update the object
							// -----------------------------------------------------
							TVChannelsSelected.get(theChannel).EPGEntries.get (localDateIndex).set (theEntry,localEPGEntry);
							// -----------------------------------------------------
							// 18/10/2015 ECU update the disk version
							// -----------------------------------------------------
							TVChannelsSelected.get(theChannel).writeToDisk (); 
							// -----------------------------------------------------
							// 18/10/2015 ECU decide whether to send data to the remote
							//                device
							// -----------------------------------------------------
							if (theRemoteDevice)
							{
								// -------------------------------------------------
								// 18/10/2015 ECU want to send data to remote device if
								//                the service is running
								// -------------------------------------------------
								remoteController (theContext,theChannel);
							}
							// -----------------------------------------------------
							// 30/09/2015 ECU do no more
							// -----------------------------------------------------
							break;
							// -----------------------------------------------------
						}
					}	
				// -----------------------------------------------------------------
				}
			}
		}
	}
	// =============================================================================
	
	
	
	// =============================================================================
  	// 17/10/2015 ECU declare the methods used for the dialogue
  	// -----------------------------------------------------------------------------
  	public static void NoMethod (Object theSelection)
  	{
  		// -------------------------------------------------------------------------
  		// 18/10/2015 ECU this option means that the user wants to retain the previous
  		//                alarm which is at the same time as this new one. Just need
  		//                to make sure the current program is not selected
  		// 26/01/2017 ECU changed to use resource
  		// -------------------------------------------------------------------------
  		Utilities.popToast (context.getString (R.string.reminder_retain),true);
  		// -------------------------------------------------------------------------
  	}
  	// =============================================================================
  	public static void YesMethod (Object theSelection)
  	{
  		// -------------------------------------------------------------------------
  		// 18/10/2015 ECU this option means that the current program is to replace
  		//                the previously define program which has to be deselected
  		//                and then alarms must be set up for this current program
  		//
  		//                The previous alarm information is held in the alarm list
  		//                at 'alarmIndex'
  		// 26/01/2017 ECU changed to use the resource
  		// -------------------------------------------------------------------------
  		EPGAlarm localEPGAlarm = (EPGAlarm) theSelection;
  		// -------------------------------------------------------------------------
  		Utilities.popToast (context.getString (R.string.reminder_overwrite) + localEPGAlarm.Print(),true);
  		// -------------------------------------------------------------------------
  		// 18/10/2015 ECU indicate that the program has been selected
  		// -------------------------------------------------------------------------
  		localEPGAlarm.EPGEntry.selected = true;
  		// -------------------------------------------------------------------------
		// 18/10/2015 ECU need to update the data
		// -------------------------------------------------------------------------
		updateEPGEntry (selectedTVChannelIndex,selectedDate,selectedEntryIndex,localEPGAlarm.EPGEntry);
		// -------------------------------------------------------------------------
		// 18/10/2015 ECU now want to deselect the original 'selected' program and 
		//                remove its entry from the alarm list
		// -------------------------------------------------------------------------
		updateEPGEntryFromAlarmData (context,(EPGAlarm) PublicData.alarmData.get (alarmIndex).object,false,false);
		// -------------------------------------------------------------------------
		// 18/10/2015 ECU delete the alarms from the list
		// 26/01/2017 ECU changed to use NextWar.....
		// -------------------------------------------------------------------------
		AlarmActions.deleteAlarmFromList (context,localEPGAlarm.date,false);
		AlarmActions.deleteAlarmFromList (context,localEPGAlarm.NextWarningTime (false),false);
		// -------------------------------------------------------------------------
		// 18/10/2015 ECU set alarms for this program
		// -------------------------------------------------------------------------
		setAlarms (localEPGAlarm.date,localEPGAlarm);
		// -------------------------------------------------------------------------
  	}
	// =============================================================================
  	
}

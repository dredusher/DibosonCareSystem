package com.usher.diboson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.usher.diboson.utilities.LifeCycle;

import java.util.Locale;

// -----------------------------------------------------------------------------------
// 17/03/2017 ECU The following suppress was added because the class uses Gravity.LEFT
//                and this generates a warning stating that Gravity.START should be
//                used. However '.START' came in at API 14 and this is later than the
//                lowest API that this app is to support
// -----------------------------------------------------------------------------------
@SuppressLint ("RtlHardcoded") 
// -----------------------------------------------------------------------------------
public class SystemInfoActivity extends DibosonActivity
{
	/* =============================================================================== */
	// ===============================================================================
	// 16/07/2013 ECU created
	// 24/02/2014 ECU tidied up and defined the 'final static' and replaced
	//                old literals
	// 27/10/2014 ECU display phone number if appropriate
	// 22/11/2014 ECU added the handling of gestures
	// 23/11/2014 ECU took out the gesture handling which was not working correctly
	//                instead use a clickable TextView
	// 24/03/2015 ECU added auto scrolling for 'project log' and 'unsorted LogCat'
	// 			      displays
	// 27/03/2015 ECU change the menu to be specific to the type of display
	// 07/10/2015 ECU added the filtering options
	// 09/10/2015 ECU put in the check that the activity can only be run if it is
	//                created from new rather than recreating after being destroyed
	//                by Android
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 16/04/2016 ECU moved NEWLINE variable into StaticData
	// 15/12/2016 ECU use values_folder to indicate which of the 'values' folders are
	//                being used by this device
	// 02/03/2016 ECU SEPARATOR and SEPARATOR_LOWER moved from here to StaticData
	// 08/03/2017 ECU put a check on whether another instance of the app is running
	// 11/03/2017 ECU put in display of the 'raw' files for bugs and notes
	// 17/03/2017 ECU to make the code more readable changed all calls to 'sleep' 
	//                to the new method 'refresh' in the UpdateRefreshHandler and
	//                use MESSAGE_REFRESH rather than MESSAGE_SLEEP
	// 18/03/2017 ECU added the 'debug' flag
	// 07/08/2018 ECU remove local 'autoScroll' and used the variable in 'stored data'
	// 08/08/2018 ECU added the display of the 'up time'
	// 20/02/2019 ECU add WiFi information
	// 27/02/2019 ECU add the external IP address
	// 03/03/2019 ECU tidy up the 'phone' setting
	// 03/03/2019 ECU try and remove some of the static's
	// 14/03/2019 ECU IMPORTANT When WiFi monitoring is requested then this will trigger
	//                ========= a 'scan' which can take several seconds. During this 
	//                          period the user can change what is being monitored. 
	//                          The 'WiFiStopAnyActions' method is used to 'abort' any
	//                          actions when the scan completes - cannot actually
	//                          stop the ongoing scan.
	// 13/08/2020 ECU provide the ability to display the status of the installed
	//                services
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//================================================================================
	/* =============================================================================== */
	private final static int	PROJECT_LOG_REFRESH	= 30;	
														// 25/02/2014 ECU added
	/* ============================================================================== */
	static boolean			devicesShowAll		= true;			// 01/04/2019 ECU added
	boolean					lifeCycleDisplay 	= false;		// 22/08/2020 ECU added
	boolean					logcatDisplay 		= false;		// 04/02/2014 ECU added
	boolean					logcatLatest 		= false;		// 11/03/2014 ECU added
	boolean					monospacedFont		= false;		// 24/04/2019 ECU added
	boolean					projectLogDisplay	= false;		// 25/02/2014 ECU added
	boolean					servicesDisplay		= false;		// 13/08/2020 ECU added
	boolean					sortByUsage			= false;		// 04/03/2019 ECU added
	ScrollView				systemInformationScrollView;		// 24/03/2015 ECU added
	TextView 				systemInformationTextview;
	boolean                 textviewViewAdjustRequiredState	
												= false;		// 16/03/2017 ECU added
																// 24/02/2019 ECU changed to 'false' to ensure
																//                the correct initialisation
	boolean					wifiDisplay			= false;		// 20/02/2019 ECU added
	static int				wifiSortFlag		= StaticData.WIFI_SORT_NONE;
																// 22/02/2019 ECU added
	// ==============================================================================
	// 03/03/2019 ECU Note - need to declare as 'public static' so that WiFiScanFinished
	//                       works correctly - if not 'public static' then updateRefreshHandler
	//                       comes across as 'null'
	// ------------------------------------------------------------------------------
	public 	static UpdateRefreshHandler 	
									updateRefreshHandler;		// 01/04/2017 ECU changed to
																//                static
																// 20/02/2019 ECU made public
			static long				upTime;						// 04/10/2015 ECU added
	/* ============================================================================= */
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		// 09/10/2015 ECU check whether this is a new creation of the activity or
		//                a recreation
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 28/11/2016 ECU call method to set the screen up
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true,true,false);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_system_info);
			// ---------------------------------------------------------------------
			systemInformationTextview  = (TextView) findViewById (R.id.system_info_textview);
			// ---------------------------------------------------------------------
			// 16/04/2016 ECU set the maximum lines of the information field
			// 24/12/2019 ECU long entries can wrap across more than one line meaning
			//                that not all of the data is displayed - by adding the
			//                '500' then the problem is resolved.
			// ---------------------------------------------------------------------
			systemInformationTextview.setMaxLines (StaticData.SYSTEM_INFO_MAX_LINES + 500);
			// ---------------------------------------------------------------------
			// 24/03/2015 ECU get scroll view information
			// ---------------------------------------------------------------------
			systemInformationScrollView = (ScrollView) findViewById (R.id.system_info_scrollview);
			// ---------------------------------------------------------------------
			// 23/02/2019 ECU make sure the textview is in the correct state
			// 04/03/2019 ECU do not pass 'this' as an argument
			// ---------------------------------------------------------------------
			textViewAdjust (systemInformationTextview,true,false);
			// ---------------------------------------------------------------------
			// 23/11/2014 ECU if the text view is clicked then update the data
			// ---------------------------------------------------------------------
			systemInformationTextview.setOnClickListener (new View.OnClickListener()
			{
				@Override
				public void onClick (View view) 
				{	
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString (R.string.data_refreshed),true);
					updateRefreshHandler.refresh (1);
					// -------------------------------------------------------------
				}
			});
			// ---------------------------------------------------------------------
			updateRefreshHandler = new UpdateRefreshHandler ();
			// ---------------------------------------------------------------------
			// 20/08/2013 ECU wait for the first update
			// 01/09/2015 ECU changed to use StaticData
			// 18/10/2015 ECU shorten the initial delay  (.../10)
			// ---------------------------------------------------------------------
			updateRefreshHandler.refresh (StaticData.SYSTEM_INFO_REFRESH_RATE / 10);
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
			// 09/10/2015 ECU the activity has been recreated after being destroyed
			//                by Android so just finish
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
		// 14/03/2019 ECU prevent any actions when the WiFi scan completes
		//            ECU added the context
		// -------------------------------------------------------------------------
		WiFiStopAnyActions (this);
		// -------------------------------------------------------------------------
		// 21/02/2019 ECU created to handled the 'back' key
	    // -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
		// 21/02/2019 ECU pass to the main handler
		// -------------------------------------------------------------------------
	    super.onBackPressed ();			
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public boolean onCreateOptionsMenu (Menu menu) 
	{
		// -------------------------------------------------------------------------
		// Inflate the menu; this adds items to the action bar if it is present.
		// -------------------------------------------------------------------------
		getMenuInflater ().inflate (R.menu.system_info, menu);
		// -------------------------------------------------------------------------
		return true;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) 
	{
		// -------------------------------------------------------------------------
		// 24/03/2015 ECU put menu options here because of the dynamic change
		//                on scrolling
		// -------------------------------------------------------------------------
		menu.clear ();
		// -------------------------------------------------------------------------
		// 24/03/2015 ECU add the individual menu items
		// 05/10/2015 ECU add option to write to file
		// 03/04/2016 ECU added the alarm data display
		// 11/03/2017 ECU added notes and bugs
		// 02/01/2019 ECU added usage
		// 20/02/2019 ECU added WiFi information
		// 01/04/2019 ECU added ability to select which devices are to be shown
		// 24/04/2019 ECU add font handling
		// -------------------------------------------------------------------------
		menu.add (0,R.id.menu_system_info,0,R.string.menu_system_info);
		menu.add (0,R.id.menu_project_log,0,R.string.menu_project_log);
		menu.add (0,R.id.menu_logcat,0,R.string.menu_logcat);
		menu.add (0,R.id.menu_logcat_latest,0,R.string.menu_logcat_latest);
		menu.add (0,R.id.menu_logcat_write,0,R.string.menu_logcat_write);
		menu.add (0,R.id.menu_logcat_filter,0,R.string.menu_logcat_filter);
		menu.add (0,R.id.menu_alarm_data,0,R.string.menu_alarm_data);
		// -------------------------------------------------------------------------
		// 04/03/2019 ECU adjust the 'usage' entry according to whether 'sorting' is
		//                on
		// -------------------------------------------------------------------------
		menu.add (0,R.id.menu_usage,0,getString (R.string.menu_usage) + " - " +
					(sortByUsage ? getString (R.string.sorted) 
							     : getString (R.string.unsorted)));
		// -------------------------------------------------------------------------
		menu.add (0,R.id.menu_wifi,0,R.string.menu_wifi);
		// -------------------------------------------------------------------------
		// 13/08/2020 ECU added 'services'
		// -------------------------------------------------------------------------
		menu.add (0,R.id.menu_services,0,R.string.menu_services);
		// -------------------------------------------------------------------------
		menu.add (0,R.id.menu_font,0,(monospacedFont ? R.string.menu_font_proportional 
						  							 : R.string.menu_font_mono));	 
		// -------------------------------------------------------------------------
		menu.add (0,R.id.menu_devices,0,(devicesShowAll ? getString (R.string.menu_devices_compatible) 
				                                        : getString (R.string.menu_devices_all)));
		// -------------------------------------------------------------------------
		menu.add (0,R.id.menu_notes,0,R.string.menu_notes);
		menu.add (0,R.id.menu_bugs,0,R.string.menu_bugs);
		// -------------------------------------------------------------------------
		// 22/08/2020 ECU added the 'life cycle log'
		// -------------------------------------------------------------------------
		menu.add (0,R.id.menu_lifecycle,0,R.string.menu_lifecycle);
		// -------------------------------------------------------------------------
		// 27/03/2015 ECU add any extra menu items depending on the display type
		// -------------------------------------------------------------------------
		if (!logcatDisplay && !projectLogDisplay)
		{
			// ---------------------------------------------------------------------
			// 27/03/2015 ECU displaying system information
			// ---------------------------------------------------------------------
		}
		else
		if (!logcatDisplay && projectLogDisplay)
		{
			// ---------------------------------------------------------------------
			// 27/03/2015 ECU displaying Project Log
			// 07/08/2018 ECU changed the 'autoScroll' variable
			// ---------------------------------------------------------------------
			menu.add (0,R.id.menu_project_log_clear,0,R.string.menu_project_log_clear);	
			menu.add (0,R.id.menu_auto_scroll,0,(PublicData.storedData.autoScroll ? R.string.auto_scroll_disable 
																				  : R.string.auto_scroll_enable));	
			// ---------------------------------------------------------------------
		}
		else
		if (logcatDisplay && !projectLogDisplay)
		{
			// ---------------------------------------------------------------------
			// 27/03/2015 ECU displaying LogCat
			// 07/08/2018 ECU changed the 'autoScroll' variable
			// ---------------------------------------------------------------------
			menu.add (0,R.id.menu_logcat_clear,0,R.string.menu_logcat_clear);
			menu.add (0,R.id.menu_auto_scroll,0,(PublicData.storedData.autoScroll ? R.string.auto_scroll_disable 
																				  : R.string.auto_scroll_enable));	
			// ---------------------------------------------------------------------	
		}
		// -------------------------------------------------------------------------
		return true;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class UpdateRefreshHandler extends Handler
	{
		// -------------------------------------------------------------------------
		// 17/03/2017 ECU changed the 'sleep' method to 'refresh' and MESSAGE_SLEEP
		//                to MESSAGE_REFRESH and added the 'noRefresh' method. Did
		//                this to make everything more readable
		// -------------------------------------------------------------------------
		@Override
	    public void handleMessage (Message theMessage) 
	    {  
			// ---------------------------------------------------------------------
			// 17/03/2017 ECU switch depending on the type of message
			// ---------------------------------------------------------------------
			switch (theMessage.what)
			{
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_DISPLAY:
					// -------------------------------------------------------------
					// 20/02/2019 ECU display the string which is passed as an object
					//                argument
					// 12/03/2019 ECU changed because text will now be in HTML format
					// 13/03/2019 ECU changed to use spannable
					// -------------------------------------------------------------
					systemInformationTextview.setText ((SpannableStringBuilder) theMessage.obj);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_MONITOR:
					// -------------------------------------------------------------
					// 20/02/2019 ECU trigger another WiFi scan
					// 13/03/2019 ECU changed to use Spannable.....
					// -------------------------------------------------------------
					WiFiInformation.getWiFiChannels (getBaseContext (),
							Utilities.createAMethod (SystemInfoActivity.class,"WiFiScanFinished",new SpannableStringBuilder ()),
							wifiSortFlag);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_REFRESH:
					// -------------------------------------------------------------
					// 20/08/2013 ECU update information on the screen
					// 04/02/2014 ECU put in the LogCat option
					// 25/02/2014 ECU add in the 'projectLogEnabled' check
					// 26/02/2014 ECU just rearrange, following changes to menu item 
					//				  handling
					// -------------------------------------------------------------
					if (logcatDisplay)
					{
						// ---------------------------------------------------------
						// 04/02/2014 ECU show text left adjusted
						// ---------------------------------------------------------
						systemInformationTextview.setGravity (Gravity.LEFT);
						// ---------------------------------------------------------
						// 04/02/2014 ECU display the LogCat entries
						// 11/03/2014 ECU include the reverse order of LogCat
						// 07/10/2015 ECU add the filtering option
						// 17/11/2019 ECU changed to use '....logcatFilter'
						// ---------------------------------------------------------
						systemInformationTextview.setText ((logcatLatest ? Utilities.GetLogCatEntries (true,PublicData.storedData.logcatFilter)
																		 : Utilities.GetLogCatEntries (PublicData.storedData.logcatFilter)));
						// ---------------------------------------------------------
						// 17/11/2019 ECU let the user know if the displayed is being filtered
						// ---------------------------------------------------------
						if (PublicData.storedData.logcatFilter != null)
						{
							Utilities.popToast (String.format (getString (R.string.logcat_filter_format),PublicData.storedData.logcatFilter),true);
						}
						// ---------------------------------------------------------
						// 24/03/2015 ECU check for auto scroll but only when the standard
						//                LogCat is being displayed
						// 07/08/2018 ECU changed the 'autoScroll' variable
						// ---------------------------------------------------------
						if (!logcatLatest && PublicData.storedData.autoScroll)
						{
							systemInformationScrollView.post (new Runnable ()
							{
								public void run()
								{
									systemInformationScrollView.fullScroll (View.FOCUS_DOWN);
								}
							});
						}
						// ---------------------------------------------------------
						// 26/02/2014 ECU redisplay the log every so often
						// ---------------------------------------------------------
						refresh (PROJECT_LOG_REFRESH * 1000);
						// ---------------------------------------------------------
					}
					else
					// -------------------------------------------------------------
					if (lifeCycleDisplay)
					{
						// ---------------------------------------------------------
						// 22/08/2020 ECU display the contents of the 'life cycle' log
						// ---------------------------------------------------------
						//22/08/2020 ECU show text left adjusted
						// ---------------------------------------------------------
						systemInformationTextview.setGravity (Gravity.LEFT);
						// ---------------------------------------------------------
						// 22/08/2020 ECU display the contents of the file
						// ---------------------------------------------------------
						systemInformationTextview.setText (LifeCycle.LogData (getBaseContext()));
						// ---------------------------------------------------------
						// 22/08/2020 ECU scroll, if necessary, to the end of the
						//                display
						// ---------------------------------------------------------
						if (PublicData.storedData.autoScroll)
						{
							systemInformationScrollView.post (new Runnable ()
							{
								public void run()
								{
									systemInformationScrollView.fullScroll (View.FOCUS_DOWN);
								}
							});
						}
						// ---------------------------------------------------------
						// 25/02/2014 ECU redisplay the log every so often
						// ---------------------------------------------------------
						refresh (PROJECT_LOG_REFRESH * 1000);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					else
					// -------------------------------------------------------------
					if (projectLogDisplay)
					{
						// ---------------------------------------------------------
						// 04/02/2014 ECU show text left adjusted
						// ---------------------------------------------------------
						systemInformationTextview.setGravity (Gravity.LEFT);
						// ---------------------------------------------------------
						// 26/02/2014 ECU check if there are any entries and give correct
						//                display
						// ---------------------------------------------------------
						systemInformationTextview.setText (projectLogData (getBaseContext ()));
						// ---------------------------------------------------------
						// 24/03/2015 ECU always try and show the end of the text view
						//            ECU only do if 'autoScroll' is true
						// 07/08/2018 ECU changed the 'autoScroll' variable
						// ---------------------------------------------------------
						if (PublicData.storedData.autoScroll)
						{
							systemInformationScrollView.post (new Runnable ()
							{
								public void run()
								{
									systemInformationScrollView.fullScroll (View.FOCUS_DOWN);
								}
							});
						}
						// ---------------------------------------------------------
						// 25/02/2014 ECU redisplay the log every so often
						// ---------------------------------------------------------
						refresh (PROJECT_LOG_REFRESH * 1000);
						// ---------------------------------------------------------
					}
					else
					// -------------------------------------------------------------
					if (servicesDisplay)
					{
						// ---------------------------------------------------------
						// 13/08/2020 ECU display information on the 'services'
						// 16/08/2020 ECU split time from the basic information
						//                which is generated using spannable
						// ---------------------------------------------------------
						systemInformationTextview.setGravity (Gravity.LEFT);
						systemInformationTextview.setText (getBaseContext ().getString (R.string.current_time) +
															Utilities.getAdjustedTime () +
															StaticData.NEWLINEx2);
						systemInformationTextview.append (ServiceControl.Status (getBaseContext()));
						refresh (StaticData.SYSTEM_INFO_REFRESH_RATE);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					else
					{
						// ---------------------------------------------------------
						// 04/02/2014 ECU show the text centred
						// ---------------------------------------------------------
						systemInformationTextview.setGravity (Gravity.CENTER);
						// ---------------------------------------------------------
						// 04/02/2014 ECU display the system information
						// 22/11/2014 ECU change as method now returns a String
						// ---------------------------------------------------------
						systemInformationTextview.setText (projectData (getBaseContext ()));
						// ---------------------------------------------------------
						refresh (StaticData.SYSTEM_INFO_REFRESH_RATE);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}
	    }
		// -------------------------------------------------------------------------
		public void noRefresh ()
		{
			// ---------------------------------------------------------------------
			// 17/03/2017 ECU created to prevent any refreshes
			// ---------------------------------------------------------------------
			this.removeMessages (StaticData.MESSAGE_REFRESH);
			// ---------------------------------------------------------------------
		}
	    /* ------------------------------------------------------------------------- */
	    public void refresh (long theDelayInMillisecs)
	    {	
	    	// ---------------------------------------------------------------------
	    	// 17/03/2017 ECU this method used to be called 'sleep' - changed to be
	    	//                more readable
	    	// ---------------------------------------------------------------------
	        this.removeMessages (StaticData.MESSAGE_REFRESH);
	        sendMessageDelayed (obtainMessage (StaticData.MESSAGE_REFRESH),theDelayInMillisecs);
	        // ---------------------------------------------------------------------
	    }
	    // -------------------------------------------------------------------------
	};
	/* ============================================================================= */
	public static String projectData (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 16/07/2013 ECU display the required system information
		// 20/08/2013 ECU added separators
		// 28/02/2016 ECU changed to use resources
		// 19/03/2017 ECU take out the header because the title shows the information
		// -------------------------------------------------------------------------
		String systemInformation = StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
		// 10/11/2014 ECU add version details and language
		// -------------------------------------------------------------------------
		systemInformation += Utilities.Version(theContext) + 
								theContext.getString (R.string.language) + 
									Locale.getDefault() + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 24/02/2014 ECU display the API level
		// 03/03/2019 ECU changed to use the format and display release info
		// -------------------------------------------------------------------------
		systemInformation += String.format (theContext.getString (R.string.api_level_format), 
				             					android.os.Build.VERSION.SDK_INT, 
				             					android.os.Build.VERSION.RELEASE) +
				             						StaticData.NEWLINE;	
		// -------------------------------------------------------------------------
		// 18/03/2017 ECU decide whether the app is in debug or release mode
		//            ECU in API 17 BuildConfig.DEBUG can be used but need to work
		//                with releases earlier than this
		// 19/03/2017 ECU changed to use variable which is set in MainActivity
		// -------------------------------------------------------------------------
		systemInformation += (PublicData.debuggable
									? theContext.getString (R.string.debug_version) 
									: theContext.getString (R.string.release_version)) + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 27/10/2014 ECU display the current strict mode setting
		// 01/09/2015 ECU changed to use StaticData
		// 19/03/2017 ECU moved here from lower down
		// -------------------------------------------------------------------------
		systemInformation += theContext.getString (R.string.strict_mode_info) + 
												StaticData.STRICT_MODE + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 08/08/2013 ECU added - put out the time when this package was last updated
		// -------------------------------------------------------------------------
		systemInformation += String.format (theContext.getString (R.string.package_last_updated),
												PublicData.lastUpdateTime,PublicData.lastUpdateDate) + 
													StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 17/10/2015 ECU indicate when and how the app was started
		// -------------------------------------------------------------------------
		systemInformation += PublicData.startUpMessage + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 08/03/2017 ECU check if another instance of the app is already running
		// 31/03/2020 ECU changed to use the method
		// -------------------------------------------------------------------------
		if (!Utilities.OneInstanceCheck ())
		{
			// ---------------------------------------------------------------------
			// 08/03/2017 ECU indicate that there is already another instance of this
			//                app running
			// ---------------------------------------------------------------------
			systemInformation += theContext.getString (R.string.another_instance) +
									PublicData.dateSimpleFormatDDMMYYHHMM.format (PublicData.keyValue)  + 
									StaticData.NEWLINE;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 20/08/2013 ECU display the current time adjusted for NTP
		// 11/11/2014 ECU change to use the method
		// -------------------------------------------------------------------------
		systemInformation += theContext.getString (R.string.current_time) +  
								Utilities.getAdjustedTime () +
									StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 08/08/2018 ECU display the app 'up time'
		// -------------------------------------------------------------------------
		systemInformation += theContext.getString (R.string.up_time) +
								Utilities.convertMillisecondsToDDHHMMSS (Utilities.getAdjustedTime (true) - PublicData.startUpTime)
								 	+ StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 11/11/2019 ECU display details of the NTP server if enabled
		// -------------------------------------------------------------------------
		if (PublicData.storedData.ntpEnabled)
		{
			// ---------------------------------------------------------------------
			// 11/11/2019 ECU display details of NTP server and adjustment
			//            ECU indicate the next refresh time
			// ---------------------------------------------------------------------
			systemInformation += StaticData.SEPARATOR
									+ String.format (theContext.getString (R.string.ntp_details_format),
														PublicData.storedData.ntpServer,
														PublicData.ntpRefreshedStatus ? String.format (theContext.getString(R.string.ntp_success_format),
																											PublicData.currentTimeAdjustment)
							           									              : theContext.getString (R.string.ntp_failed_format))
					 				+ String.format (theContext.getString (R.string.ntp_check_format),PublicData.ntp_counter,Utilities.AddAnS (PublicData.ntp_counter))
					 				+ StaticData.NEWLINE;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 08/08/2018 ECU include a separator
		// -------------------------------------------------------------------------
		systemInformation += StaticData.SEPARATOR;
		// -------------------------------------------------------------------------
		// 14/03/2015 ECU display the current battery level
		// -------------------------------------------------------------------------
		systemInformation += 
				String.format (theContext.getString (R.string.current_battery_level),
										SensorService.batteryLevel) +
		// -------------------------------------------------------------------------
		// 21/01/2016 ECU indicate if the battery is being charged or not
		// -------------------------------------------------------------------------
		((SensorService.chargeStatus == BatteryManager.BATTERY_STATUS_CHARGING) ? " (" : " (not ")
											+ "charging)" + StaticData.NEWLINE + StaticData.SEPARATOR;
		// -------------------------------------------------------------------------
		// 16/07/2013 ECU get the screen size
		// -------------------------------------------------------------------------		
		DisplayMetrics displayMetrics = new DisplayMetrics();
		MainActivity.activity.getWindowManager ().getDefaultDisplay ().getMetrics (displayMetrics);
				
		systemInformation += String.format (theContext.getString (R.string.screen_size_info),
											displayMetrics.widthPixels,
											displayMetrics.heightPixels) + StaticData.NEWLINE;
		// -------------------------------------------------------------------------		
		// 16/07/2013 ECU get the screen density
		// -------------------------------------------------------------------------	
		systemInformation += String.format (theContext.getString (R.string.screen_density), 
								displayMetrics.densityDpi,displayMetrics.density) + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 15/12/2016 ECU try and indicate which 'values' folder is being used
		// -------------------------------------------------------------------------
		systemInformation += theContext.getString (R.string.values_folder_title) + 
								theContext.getString (R.string.values_folder) + StaticData.NEWLINE;
		// -------------------------------------------------------------------------		
		// 23/07/2013 ECU display the width in 'dp'
		// -------------------------------------------------------------------------		
		systemInformation += String.format (theContext.getString(R.string.screen_width_info),
								(float) displayMetrics.widthPixels / displayMetrics.density) + StaticData.NEWLINE;
		// -------------------------------------------------------------------------		
		// 24/07/2013 ECU add telephony information
		// 27/10/2014 ECU changed to use the stored value rather than calling
		//                Utilities,getPhoneNumber
		// 03/03/2019 ECU tidy up the display
		// -------------------------------------------------------------------------
		systemInformation += theContext.getString (R.string.phone_number_info) + 
									((PublicData.phoneNumber != null) ? PublicData.phoneNumber
											                          : theContext.getString (R.string.no_phone)) 
											                          	+ StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 25/07/2013 ECU add IP address (WiFi)
		// 25/02/2014 ECU include the network mask
		// 01/04/2019 ECU indicate when there is no network
		// -------------------------------------------------------------------------
		if (Utilities.checkForNetwork (theContext))
		{
			systemInformation += theContext.getString (R.string.ip_address_info) + PublicData.ipAddress + 
								" (" + PublicData.networkMask + ")"+ StaticData.NEWLINE;
			// ---------------------------------------------------------------------
			// 27/02/2019 ECU check if have the public IP address
			// 18/07/2019 ECU publicIpAddress moved into storedData
			// ---------------------------------------------------------------------
			if (PublicData.storedData.publicIpAddress != null)
			{
				systemInformation += theContext.getString (R.string.public_ip_address_info) + 
											PublicData.storedData.publicIpAddress + StaticData.NEWLINE;
			}
			// ---------------------------------------------------------------------
			// 31/12/2014 ECU show the way of accessing this device remotely
			// ---------------------------------------------------------------------
			systemInformation += String.format (theContext.getString (R.string.public_url_info),
											PublicData.storedData.dynamicIPAddress, 
											PublicData.socketNumberForWeb) + StaticData.NEWLINE;
			// ---------------------------------------------------------------------
			// 08/01/2019 ECU display the SSID of the wireless network
			// 19/02/2019 ECU changed because 'getSSID' method moved into WiFiInformation
			// 20/02/2019 ECU added the strength
			// 19/02/2020 ECU adjust the strength display in case the 'strength
			//                method' returns an error
			// ---------------------------------------------------------------------
			double localStrength = WiFiInformation.getStrength (theContext);
			systemInformation += theContext.getString (R.string.wireless_name_info) +
											WiFiInformation.getSSID (theContext)  +
					((localStrength != StaticData.NOT_SET) ? String.format (theContext.getString (R.string.wifi_strength_format),localStrength)
					                                       : StaticData.BLANK_STRING);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 01/04/2019 ECU indicate that the device is not connected to a network
			// ---------------------------------------------------------------------
			systemInformation += theContext.getString (R.string.device_no_network);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		systemInformation += StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 19/03/2014 ECU display the project folder
		// -------------------------------------------------------------------------
		systemInformation += theContext.getString (R.string.project_folder_info) + 
								PublicData.projectFolder + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 30/11/2018 ECU display the path to the external SD card - if appropriate
		// -------------------------------------------------------------------------
		if (PublicData.externalSDCard != null)
		{
			systemInformation += theContext.getString (R.string.external_sd_card_path) + 
								PublicData.externalSDCard + StaticData.NEWLINE;
		}
		// -------------------------------------------------------------------------		
		// 25/07/2013 ECU print out devices on the network
		// 22/03/2015 ECU change the logic to reflect new List<Devices>
		// 01/04/2019 ECU take into account the compatibility/all display option
		// -------------------------------------------------------------------------		
		if (PublicData.deviceDetails != null)
		{
			systemInformation += StaticData.SEPARATOR;
			for (int index=0; index < PublicData.deviceDetails.size(); index++)
			{
				// -----------------------------------------------------------------
				if (PublicData.deviceDetails.get (index).compatible || devicesShowAll)
				{
					systemInformation += String.format (theContext.getString (R.string.device_info),index, 
										PublicData.deviceDetails.get (index).Print()) + 
											StaticData.NEWLINE + StaticData.SEPARATOR;
				}
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------		
		// 26/07/2013 ECU display the phone server on the network
		// -------------------------------------------------------------------------		
		systemInformation +=  theContext.getString (R.string.phone_server) + 
								((PublicData.phoneServer != null) ? PublicData.phoneServer 
												  				  : theContext.getString (R.string.none_found)) + 
									StaticData.NEWLINE;
		// -------------------------------------------------------------------------		
		// 26/07/2013 ECU display the remote controller server on the network
		// -------------------------------------------------------------------------		
		systemInformation += theContext.getString (R.string.remote_controller_server) + 
								((PublicData.remoteControllerServer != null) ? PublicData.remoteControllerServer 
																	   		 : theContext.getString (R.string.none_found)) + 
									StaticData.NEWLINE;
		// -------------------------------------------------------------------------		
		// 26/07/2013 ECU display the Belkin WeMo controller on the network
		// -------------------------------------------------------------------------		
		systemInformation +=  theContext.getString (R.string.belkin_wemo_controller) + 
								((PublicData.wemoServer != null) ? PublicData.wemoServer 
											     				 : theContext.getString (R.string.none_found)) + 
								    StaticData.NEWLINE;
		// -------------------------------------------------------------------------		
		// 02/08/2013 ECU add details of the datagram
		// -------------------------------------------------------------------------		
		systemInformation += PublicData.datagram.Print ();
		// -------------------------------------------------------------------------
		// 06/09/2013 ECU display any bluetooth devices discovered
		// 30/12/2015 ECU changed to use the adapter setting
		// -------------------------------------------------------------------------
		systemInformation += StaticData.NEWLINE + 
								StaticData.SEPARATOR + 
									((PublicData.bluetoothUtilities.bluetoothAdapter != null) ? PublicData.bluetoothUtilities.Print() 
											                                                  : theContext.getString (R.string.bluetooth_discovery_disabled));
		// -------------------------------------------------------------------------
		// 16/07/2013 ECU return the generated system information
		// -------------------------------------------------------------------------		
		return systemInformation;	
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	
	// =============================================================================
	public static String projectLogData (Context theContext)
	{	
		// -------------------------------------------------------------------------
		// 04/02/2014 ECU display the LogCat entries
		// 16/04/2016 ECU because the size of the project log can be more than the
		//                displayable textview then change the method from
		//					byte [] bytesRead = Utilities.readAFile (PublicData.projectLogFile);
		// 23/09/2020 ECU this used to be the main method but, because of an
		//                'uncaught exception' with the swipe version, a new method
		//                was added where the number of lines to be retrieved was
		//                created
		// -------------------------------------------------------------------------
		return projectLogData (theContext,StaticData.SYSTEM_INFO_MAX_LINES);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static String projectLogData (Context theContext,int theNumberOfLines)
	{
		// -------------------------------------------------------------------------
		// 23/09/2020 ECU created to retrieve a specified number of lines from
		//                the project log - this was added for the swipe version of
		//                the 'system information' activity where an exception was
		//                being caused if the project log got too big
		// -------------------------------------------------------------------------
		byte [] bytesRead = Utilities.readLinesFromEndOfFile (PublicData.projectLogFile,theNumberOfLines);
		// -------------------------------------------------------------------------
		// 26/02/2014 ECU check if there are any entries and give correct
		//                display
		// -------------------------------------------------------------------------
		return ((bytesRead != null) ? new String (bytesRead)
									: theContext.getString (R.string.project_log_is_empty));
		// -------------------------------------------------------------------------
	}
	// ==============================================================================
		
	// ==============================================================================
	public boolean onOptionsItemSelected (MenuItem item)
	{
		// -------------------------------------------------------------------------
		// 14/03/2019 ECU prevent any actions when the WiFi scan completes
		//            ECU added the context
		// -------------------------------------------------------------------------
		WiFiStopAnyActions (this);
		// -------------------------------------------------------------------------
		// 16/03/2017 ECU make sure that the text view is in the correct state
		// 21/02/2019 ECU added 'false' to indicate normal font
		// 04/03/2019 ECU do not pass 'this' as an argument
		// -------------------------------------------------------------------------
		textViewAdjust (systemInformationTextview,true,false);
		// -------------------------------------------------------------------------
		// 24/03/2015 ECU switch on menu item selected
		// -------------------------------------------------------------------------
		switch (item.getItemId())
		{
			// =====================================================================
			case R.id.menu_alarm_data:
				// -----------------------------------------------------------------
				// 03/04/2016 ECU added to displayed any stored alarms
				// -----------------------------------------------------------------
				// 17/03/2017 ECU update the title to show what is being displayed
				// -----------------------------------------------------------------
				this.setTitle (getString (R.string.title_stored_alarms));
				// -----------------------------------------------------------------
				systemInformationTextview.setGravity (Gravity.LEFT);
				// -----------------------------------------------------------------
				// 17/03/2017 ECU changed to use resource
				// 19/03/2017 ECU took out the header string
				// -----------------------------------------------------------------
				systemInformationTextview.setText (AlarmData.PrintAll (this));
				// -----------------------------------------------------------------
				// 18/03/2017 ECU changed to use 'noRefresh' method
				// -----------------------------------------------------------------
				updateRefreshHandler.noRefresh ();
				// -----------------------------------------------------------------
				return true;
				// -----------------------------------------------------------------
			// =====================================================================
			case R.id.menu_auto_scroll:
				// -----------------------------------------------------------------
				// 24/03/2015 ECU added to toggle the scrolling flag
				// 07/08/2018 ECU changed the 'autoScroll' variable
				// -----------------------------------------------------------------
				PublicData.storedData.autoScroll = !PublicData.storedData.autoScroll;
				// -----------------------------------------------------------------
				// 24/03/2015 ECU indicate what is going on
				// 17/03/2017 ECU changed to use resources
				// 07/08/2018 ECU changed the 'autoScroll' variable
				//            ECU call the method to inform the user of the status
				// -----------------------------------------------------------------
				scrollingStatus (true);
				// -----------------------------------------------------------------
				break;
			// =====================================================================
			case R.id.menu_bugs:
				// -----------------------------------------------------------------
				// 11/03/2017 ECU display the contents of the 'known bugs' file
				// -----------------------------------------------------------------
				// 17/03/2017 ECU display the info on the title
				// -----------------------------------------------------------------
				setTitle (getString (R.string.title_bugs));
				// -----------------------------------------------------------------
				// 21/02/2019 ECU added 'true' to indicate very small font
				// 04/03/2019 ECU do not pass 'this' as an argument
				// -----------------------------------------------------------------
				textViewAdjust (systemInformationTextview,false,true);
				// -----------------------------------------------------------------
				// 11/03/2017 ECU get the 'bugs' from the raw file
				// 07/06/2017 ECU changed to use TAB
				// 16/11/2019 ECU changed from replaceAll
				// -----------------------------------------------------------------
				systemInformationTextview.setText (Utilities.readRawResource (this, R.raw.documentation_bugs).replace (StaticData.TAB,"    "));
				// -----------------------------------------------------------------
				// 11/03/2017 ECU indicate no screen refresh is wanted
				// -----------------------------------------------------------------
				updateRefreshHandler.noRefresh ();
				// -----------------------------------------------------------------
				return true;
			// =====================================================================
			case R.id.menu_devices:
				// -----------------------------------------------------------------
				// 01/04/2019 ECU toggle the compatibility/all devices flag
				// -----------------------------------------------------------------
				devicesShowAll = !devicesShowAll;
				// -----------------------------------------------------------------
				break;
			// =====================================================================
			case R.id.menu_font:
				// -----------------------------------------------------------------
				// 24/04/2019 ECU toggle the font between monospaced and 
				//                proportional
				// -----------------------------------------------------------------
				monospacedFont = !monospacedFont;
				// -----------------------------------------------------------------
				// 24/04/2019 ECU now change the font for the system
				// -----------------------------------------------------------------
				if (monospacedFont)
					systemInformationTextview.setTypeface (Typeface.MONOSPACE);
				else
					systemInformationTextview.setTypeface (Typeface.DEFAULT,Typeface.BOLD); 
				// -----------------------------------------------------------------
				// 25/04/2019 ECU changed from break
				// -----------------------------------------------------------------
				return true;
			// =====================================================================
			case R.id.menu_lifecycle:
				// -----------------------------------------------------------------
				// 22/08/2020 ECU display entries in the 'life cycle' log
				// -----------------------------------------------------------------
				this.setTitle (getString (R.string.display_life_cycle_log));
				// -----------------------------------------------------------------
				// 22/08/2020 ECU clear all other display flags
				// -----------------------------------------------------------------
				resetDisplayFlags ();
				lifeCycleDisplay	= true;
				// -----------------------------------------------------------------
				// 22/08/2020 ECU inform the user of the auto scrolling status
				// -----------------------------------------------------------------
				scrollingStatus (false);
				// -----------------------------------------------------------------
				break;
			// =====================================================================
			case R.id.menu_logcat:
				// -----------------------------------------------------------------			
				// 25/02/2014 ECU change the app title to reflect what is being
				//                monitored
				// 17/03/2017 ECU changed to use resource
				// -----------------------------------------------------------------
				this.setTitle (getString (R.string.title_logcat));
				// -----------------------------------------------------------------
				// 04/02/2014 ECU toggle the LogCat display
				// 26/02/2014 ECU rearrange because toggling not used anymore
				// 22/08/2020 ECU use new method to reset display flags
				// -----------------------------------------------------------------
				resetDisplayFlags ();
				logcatDisplay = true;
				logcatLatest  = false;
				// -----------------------------------------------------------------
				// 07/08/2018 ECU inform the user of the auto scrolling status
				// -----------------------------------------------------------------
				scrollingStatus (false);
				// -----------------------------------------------------------------
				break;
			// =====================================================================	
			case R.id.menu_logcat_clear:
				// -----------------------------------------------------------------
				// 11/03/2014 ECU clear the LogCat log
				// -----------------------------------------------------------------
				Utilities.LogCatClear (); 
				// -----------------------------------------------------------------
				// 11/03/2014 ECU indicate that the log has been cleared
				// -----------------------------------------------------------------
				Utilities.popToast (getString (R.string.logcat_cleared),true);
				// -----------------------------------------------------------------
				break;
			// =====================================================================
			case R.id.menu_logcat_filter:
				// -----------------------------------------------------------------
				// 07/10/2015 ECU added to set up the LogCat filter
				// 17/03/2017 ECU changed to use resources
				// 01/04/2017 ECU added the existing filter as the default text
				// 03/03/2019 ECU changed to use '...NonStatic....
				// 17/11/2019 ECU changed to use '....logcatFilter'
				// -----------------------------------------------------------------
				DialogueUtilitiesNonStatic.textInput (this,
											 		 (Object) this,				// the underlying object
											 		 getString (R.string.enter_logcat_filter),
											 		 getString (R.string.enter_logcat_filter_summary),
											 		                ((PublicData.storedData.logcatFilter == null) ? StaticData.BLANK_STRING
											 		                		          							  : PublicData.storedData.logcatFilter),
											 		 Utilities.createAMethod (SystemInfoActivity.class,"ConfirmFilter",StaticData.BLANK_STRING),
											 		 Utilities.createAMethod (SystemInfoActivity.class,"CancelFilter",StaticData.BLANK_STRING));
				// -----------------------------------------------------------------
				break;
			// =====================================================================				
			case R.id.menu_logcat_latest:
				// -----------------------------------------------------------------
				// 11/03/2014 ECU displays LogCat with latest entries first
				// -----------------------------------------------------------------		
				// 25/02/2014 ECU change the app title to reflect what is being monitored
				// 17/03/2017 ECU changed to use resource
				// -----------------------------------------------------------------
				this.setTitle (getString (R.string.title_logcat_latest_first));
				// -----------------------------------------------------------------
				// 22/08/2020 ECU use new method to reset the flags
				// -----------------------------------------------------------------
				resetDisplayFlags ();
				logcatDisplay = true;
				logcatLatest  = true;
				// -----------------------------------------------------------------
				// 07/08/2018 ECU inform the user of the auto scrolling status
				// -----------------------------------------------------------------
				scrollingStatus (false);
				// -----------------------------------------------------------------
				break;	
			// =====================================================================
			case R.id.menu_logcat_write:
				// -----------------------------------------------------------------
				// 04/10/2015 ECU created to write the contents of LogCat to file
				// 17/11/2019 ECU changed to use '....logcatFilter'
				// -----------------------------------------------------------------
				Utilities.saveLogCatEntries (PublicData.projectFolder + StaticData.LOGCAT_OUTPUT_FILE,PublicData.storedData.logcatFilter);
				// -------------------------------------------------------------------------
				// 05/10/2015 ECU indicate where the data has been written
				// 17/03/2017 ECU change to use resources
				// 17/11/2019 ECU change to use '....logcatFilter'
				// -------------------------------------------------------------------------
				Utilities.popToast (getString (R.string.logcat_written_to) + 
						             PublicData.projectFolder + StaticData.LOGCAT_OUTPUT_FILE + StaticData.NEWLINE +
										((PublicData.storedData.logcatFilter == null) ? getString (R.string.filter_none)
														  : (String.format (getString (R.string.filter_format),PublicData.storedData.logcatFilter))),true);
				// -----------------------------------------------------------------
				break;
			// =====================================================================
			case R.id.menu_notes:
				// -----------------------------------------------------------------
				// 11/03/2017 ECU display the contents of the 'notes' file
				// -----------------------------------------------------------------
				// 17/03/2017 ECU display the info on the title
				// -----------------------------------------------------------------
				setTitle (getString (R.string.title_notes));
				// -----------------------------------------------------------------
				// 21/02/2019 ECU added 'true' to indicate very small font wanted
				// 04/03/2019 ECU do not pass 'this' as an argument
				// -----------------------------------------------------------------
				textViewAdjust (systemInformationTextview,false,true);
				// -----------------------------------------------------------------
				// 11/03/2017 ECU read the 'notes' raw file and display in the
				//                text view
				// 07/06/2017 ECU changed to use TAB
				// 16/11/2019 ECU changed from 'replaceAll' to 'replace' because
				//                the former requires a REGEX so not sure why it ever
				//				  worked
				// -----------------------------------------------------------------
				systemInformationTextview.setText (Utilities.readRawResource (this,R.raw.documentation_notes).replace (StaticData.TAB,"    "));
				// -----------------------------------------------------------------
				// 11/03/2017 ECU indicate no screen refresh is wanted
				// -----------------------------------------------------------------
				updateRefreshHandler.noRefresh ();
				// -----------------------------------------------------------------
				return true;
			// =====================================================================					
			case R.id.menu_project_log:
				// -----------------------------------------------------------------
				// 25/02/2014 ECU handle display of the project log
				// -----------------------------------------------------------------
				// 25/02/2014 ECU change the app title to reflect what is being monitored
				// 17/03/2017 ECU took out the refresh rate message and use resource
				// -----------------------------------------------------------------
				this.setTitle (getString (R.string.title_project_log));
				// -----------------------------------------------------------------
				// 26/02/2014 ECU rearrange because toggling not used anymore
				// 22/08/2020 ECU use new method to reset display flags
				// -----------------------------------------------------------------
				resetDisplayFlags ();
				projectLogDisplay = true;
				// -----------------------------------------------------------------
				// 07/08/2018 ECU inform the user of the auto scrolling status
				// -----------------------------------------------------------------
				scrollingStatus (false);
				// -----------------------------------------------------------------
				break;
			// =====================================================================	
			case R.id.menu_system_info:
				// -----------------------------------------------------------------
				// 25/02/2014 ECU display the normal system information
				// -----------------------------------------------------------------
				this.setTitle (getString (R.string.title_activity_system_info));
				// -----------------------------------------------------------------
				// 24/03/2015 ECU switch off the LogCat and 'project log' displays
				// 13/08/2020 ECU added 'services' display
				// 22/08/2020 ECU added 'life cycle' display
				//            ECU changed to use new method
				// -----------------------------------------------------------------
				resetDisplayFlags ();
				// -----------------------------------------------------------------
				break;
			// =====================================================================
			case R.id.menu_project_log_clear:
				// -----------------------------------------------------------------
				// 23/11/2014 ECU clear the project log
				// -----------------------------------------------------------------
				Utilities.ProjectLogClear ();
				// -----------------------------------------------------------------
				// 23/11/2014 ECU indicate that the log has been cleared
				// -----------------------------------------------------------------
				Utilities.popToast (getString (R.string.project_log_cleared),true);
				// -----------------------------------------------------------------				
				break;
			// =====================================================================
			case R.id.menu_services:
				// -----------------------------------------------------------------
				// 13/08/2020 ECU display 'service' information
				// -----------------------------------------------------------------
				this.setTitle (getString (R.string.title_service_information));
				// -----------------------------------------------------------------
				// 22/08/2020 ECU set up the flags for display
				// -----------------------------------------------------------------
				resetDisplayFlags ();
				servicesDisplay 	= true;
				// -----------------------------------------------------------------
				break;
			// =====================================================================
			case R.id.menu_usage:
				// -----------------------------------------------------------------
				// 02/01/2019 ECU added to displayed the activity usage
				// -----------------------------------------------------------------
				// 02/01/2019 ECU update the title to show what is being displayed
				// -----------------------------------------------------------------
				this.setTitle (getString (R.string.title_usage));
				// -----------------------------------------------------------------
				// 03/03/2019 ECU changed to use the method to set the display
				// 04/03/2019 ECU do not pass 'this' as an argument
				// -----------------------------------------------------------------
				textViewAdjust (systemInformationTextview,false,false);
				// -----------------------------------------------------------------
				// 02/01/2019 ECU display the usage information
				// 04/03/2019 ECU added the 'true' to sort the data before display
				//            ECU changed to use 'sortByUsage'
				// -----------------------------------------------------------------
				systemInformationTextview.setText (GridImages.PrintAll (this,StaticData.BLANK_STRING,sortByUsage));
				// -----------------------------------------------------------------
				// 04/03/2019 ECU now toggle the sort flag
				// -----------------------------------------------------------------
				sortByUsage = !sortByUsage;
				// -----------------------------------------------------------------
				// 02/01/2019 ECU indicate no auto refresh
				// -----------------------------------------------------------------
				updateRefreshHandler.noRefresh ();
				// -----------------------------------------------------------------
				return true;
			// =====================================================================
			case R.id.menu_wifi:
				// -----------------------------------------------------------------
				// 20/02/2019 ECU added to display information about the WiFi 
				//                networks that are being seen by this device
				// -----------------------------------------------------------------
				// 22/02/2019 ECU it is necessary to have location services running
				// -----------------------------------------------------------------
				if (Utilities.checkForLocationServices (this))
				{
					this.setTitle (getString (R.string.title_wifi));
					// -------------------------------------------------------------
					// 21/02/2019 ECU change the display - the final 'false' indicates
					//                that the default font is required
					// 04/03/2019 ECU do not pass 'this' as an argument
					// -------------------------------------------------------------
					textViewAdjust (systemInformationTextview,false,false);
					// -------------------------------------------------------------
					// 21/02/2019 ECU clear the screen
					// -------------------------------------------------------------
					systemInformationTextview.setText (getString (R.string.wifi_obtaining));
					// -------------------------------------------------------------
					// 22/02/2019 ECU give the option of sorting the displayed data
					// -------------------------------------------------------------
					DialogueUtilities.listChoice (this,
												  getString (R.string.wifi_sort_title),
												  getResources().getStringArray (R.array.wifi_sort_types),
												  Utilities.createAMethod (SystemInfoActivity.class,"WiFiSortSelect",0),
												  getString (R.string.cancel),
												  null);
					// -------------------------------------------------------------
					// 22/02/2019 ECU added the 'sort flag'
					// 13/03/2019 ECU changed to use Spannable....
					// -------------------------------------------------------------
					WiFiInformation.getWiFiChannels (getBaseContext (),
												     Utilities.createAMethod (SystemInfoActivity.class,
												    		 				  "WiFiScanFinished",new SpannableStringBuilder ()),
												     wifiSortFlag);
					// -------------------------------------------------------------
					// 11/03/2017 ECU indicate no screen refresh is wanted
					// -------------------------------------------------------------
					updateRefreshHandler.noRefresh ();
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 22/02/2019 ECU tell the use that location services are required
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString (R.string.wifi_require_location),true);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				return true;
			// =====================================================================
		}
		// -------------------------------------------------------------------------
		// 04/02/2014 ECU restart the update handler
		// 01/09/2015 ECU changed to use StaticData
		// 21/10/2015 ECU changed to speed up the initial refresh
		// -------------------------------------------------------------------------
		updateRefreshHandler.refresh (StaticData.SYSTEM_INFO_REFRESH_RATE / 10);
		// -------------------------------------------------------------------------	
		return true;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void resetDisplayFlags ()
	{
		// -------------------------------------------------------------------------
		// 22/08/2020 ECU created to reset all of the display flags
		// -------------------------------------------------------------------------
		servicesDisplay 	= false;
		lifeCycleDisplay    = false;
		logcatDisplay 		= false;
		projectLogDisplay 	= false;
		wifiDisplay         = false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void scrollingStatus (boolean theHasBeenOrIsFlag)
	{
		// -------------------------------------------------------------------------
		// 07/08/2018 ECU created to indicate the scrolling status
		//            ECU if theHasBeenOrIsFlag .... true   display 'has been'
		//                                      .... false  display 'is'
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak (String.format (getString (R.string.auto_scrolling_format),
											(theHasBeenOrIsFlag ? getString (R.string.has_been) 
															    : getString (R.string.is))) + 
											(PublicData.storedData.autoScroll ? getString (R.string.enabled) 
																			  : getString (R.string.disabled)));
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	void textViewAdjust (TextView theTextView,boolean theRequiredState,boolean theSmallFontFlag)
	{
		// -------------------------------------------------------------------------
		// 11/03/2017 ECU created to adjust the textview to required state
		//
		//					theRequiredState  = true     set default values
		//                                    = false    small font, mono
		// 21/02/2019 ECU added theSmallFontFlag
		//                                    = true     very small font
		//                                    = false    set default values
		// -------------------------------------------------------------------------
		// 16/03/2017 ECU check if the state has changed
		// 04/03/2019 ECU remove 'theContext' as an argument
		// -------------------------------------------------------------------------
		if (theRequiredState != textviewViewAdjustRequiredState)
		{
			// ---------------------------------------------------------------------
			// 16/03/2017 ECU the state has changed so need to do something
			// ---------------------------------------------------------------------
			if (theRequiredState)
			{
				// -----------------------------------------------------------------
				// 16/03/2017 ECU clear the text field 
				// -----------------------------------------------------------------
				theTextView.setText (StaticData.BLANK_STRING);
				// -----------------------------------------------------------------
				theTextView.setTypeface (Typeface.DEFAULT,Typeface.BOLD); 
				// -----------------------------------------------------------------
				// 16/03/2017 ECU want to be able to click on the text view
				// -----------------------------------------------------------------
				theTextView.setClickable (true);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 15/03/2017 ECU set the font and required alignment
				// -----------------------------------------------------------------
				theTextView.setTypeface (Typeface.MONOSPACE); 
				theTextView.setGravity (Gravity.LEFT);
				// -----------------------------------------------------------------
				// 16/03/2017 ECU do not want to click on the text view
				// -----------------------------------------------------------------
				theTextView.setClickable (false);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 21/02/2019 ECU adjust the font according to the 'font flag'
			// ---------------------------------------------------------------------
			if (theSmallFontFlag)
			{
				// -----------------------------------------------------------------
				// 15/03/2017 ECU changed to use 'very small font'
				// -----------------------------------------------------------------
				theTextView.setTextSize (TypedValue.COMPLEX_UNIT_PX,
						                 getResources ().getDimension (R.dimen.very_small_font));
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 16/03/2017 ECU change to use system_info... font
				// -----------------------------------------------------------------
				theTextView.setTextSize (TypedValue.COMPLEX_UNIT_PX,
										 getResources ().getDimension (R.dimen.system_info_default_font_size));
				// -----------------------------------------------------------------
			}	
			// ---------------------------------------------------------------------
			// 16/03/2017 ECU remember the state so that don't reset the field unnecessarily
			// ---------------------------------------------------------------------
			textviewViewAdjustRequiredState = theRequiredState;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// ==============================================================================
	
	// ==============================================================================
	public void CanceFilter (String theText)
	{
		// -------------------------------------------------------------------------
		// 03/03/2019 ECU changed from static
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void ConfirmFilter (String theText)
	{
		// -------------------------------------------------------------------------
		// 07/10/2015 ECU set the filter that will be used for selecting particular
		//                LogCat entries
		// 03/03/2019 ECU changed from static
		// 17/11/2019 ECU changed to use '...logcatFilter' rather than a local variable
		// -------------------------------------------------------------------------
		PublicData.storedData.logcatFilter = theText;
		// -------------------------------------------------------------------------
		// 07/10/2015 ECU check if the filter needs clearing
		// 17/03/2017 ECU changed to use the StaticData
		// -------------------------------------------------------------------------
		if (PublicData.storedData.logcatFilter.equalsIgnoreCase(StaticData.BLANK_STRING))
			PublicData.storedData.logcatFilter  = null;
		// -------------------------------------------------------------------------
		// 01/04/2017 ECU want to do an immediate refresh of the display
		// -------------------------------------------------------------------------
		updateRefreshHandler.refresh (1);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void WiFiScanFinished (SpannableStringBuilder theResultString)
	{
		// -------------------------------------------------------------------------
		// 20/02/2019 ECU display the WiFi network information
		// 03/03/2019 ECU changed from 'static'
		// 13/03/2019 ECU changed to used Spannable...
		// -------------------------------------------------------------------------
		Message localMessage = updateRefreshHandler.obtainMessage (StaticData.MESSAGE_DISPLAY,theResultString);
		updateRefreshHandler.sendMessage (localMessage);
		// -------------------------------------------------------------------------
		// 20/02/2019 ECU trigger another scan of WiFi networks
		// -------------------------------------------------------------------------
		updateRefreshHandler.sendEmptyMessageDelayed (StaticData.MESSAGE_MONITOR,5 * 1000);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void WiFiSortSelect (int theChoice)
	{
		// -------------------------------------------------------------------------
		wifiSortFlag = theChoice;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void WiFiStopAnyActions (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 14/03/2019 ECU created to abort any actions that are taken when the WiFi 
		//                scan completes - see comments at head of this class
		//            ECU added the context
		// -------------------------------------------------------------------------
		WiFiInformation.WiFiAbortScan (theContext);
		// -------------------------------------------------------------------------
		// 21/02/2019 ECU remove any outstanding messages
		// -------------------------------------------------------------------------
		updateRefreshHandler.removeMessages (StaticData.MESSAGE_MONITOR);
		updateRefreshHandler.removeMessages (StaticData.MESSAGE_REFRESH);
		// --------------------------------------------------------------------------
	}
	// ==============================================================================
}

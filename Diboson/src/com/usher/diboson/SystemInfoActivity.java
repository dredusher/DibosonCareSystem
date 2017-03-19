package com.usher.diboson;

import java.util.Locale;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

// -----------------------------------------------------------------------------------
// 17/03/2017 ECU The following suppress was added because the class uses Gravity.LEFT
//                and this generates a warning stating that Gravity.START should be
//                used. However '.START' came in at API 14 and this is later than the
//                lowest API that this app is to support
// -----------------------------------------------------------------------------------
@SuppressLint("RtlHardcoded") 
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
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//================================================================================
	/* =============================================================================== */
	private final static int	PROJECT_LOG_REFRESH	= 30;	
														// 25/02/2014 ECU added
	/* ============================================================================== */
	boolean					autoScroll			= false;		// 24/03/2015 ECU added
	boolean					logcatDisplay 		= false;		// 04/02/2014 ECU added
	boolean					logcatLatest 		= false;		// 11/03/2014 ECU added
	boolean					projectLogDisplay	= false;		// 25/02/2014 ECU added
	ScrollView				systemInformationScrollView;		// 24/03/2015 ECU added
	TextView 				systemInformationTextview;
	boolean                 textviewViewAdjustRequiredState	
												= true;		// 16/03/2017 ECU added
	UpdateRefreshHandler 	updateRefreshHandler;
	// ==============================================================================
	static String			filter				= null;			// 07/10/2015 ECU added
	static long				upTime;								// 04/10/2015 ECU added
	/* ============================================================================= */
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
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
			// ---------------------------------------------------------------------
			systemInformationTextview.setMaxLines (StaticData.SYSTEM_INFO_MAX_LINES);
			// ---------------------------------------------------------------------
			// 24/03/2015 ECU get scroll view information
			// ---------------------------------------------------------------------
			systemInformationScrollView = (ScrollView) findViewById (R.id.system_info_scrollview);
			// ---------------------------------------------------------------------
			// 23/11/2014 ECU make the text view clickable so that an immediate
			//                update on click can be implemented
			// ---------------------------------------------------------------------
			systemInformationTextview.setClickable (true);
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
	/* ============================================================================= */
	@Override
	public boolean onCreateOptionsMenu (Menu menu) 
	{
		// -------------------------------------------------------------------------
		// Inflate the menu; this adds items to the action bar if it is present.
		// -------------------------------------------------------------------------
		getMenuInflater().inflate (R.menu.system_info, menu);
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
		// -------------------------------------------------------------------------
		menu.add (0,R.id.menu_system_info,0,R.string.menu_system_info);
		menu.add (0,R.id.menu_project_log,0,R.string.menu_project_log);
		menu.add (0,R.id.menu_logcat,0,R.string.menu_logcat);
		menu.add (0,R.id.menu_logcat_latest,0,R.string.menu_logcat_latest);
		menu.add (0,R.id.menu_logcat_write,0,R.string.menu_logcat_write);
		menu.add (0,R.id.menu_logcat_filter,0,R.string.menu_logcat_filter);
		menu.add (0,R.id.menu_alarm_data,0,R.string.menu_alarm_data);
		menu.add (0,R.id.menu_notes,0,R.string.menu_notes);
		menu.add (0,R.id.menu_bugs,0,R.string.menu_bugs);
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
			// ---------------------------------------------------------------------
			menu.add (0,R.id.menu_project_log_clear,0,R.string.menu_project_log_clear);	
			menu.add (0,R.id.menu_auto_scroll,0,(autoScroll ? R.string.auto_scroll_disable 
															: R.string.auto_scroll_enable));	
			// ---------------------------------------------------------------------
		}
		else
		if (logcatDisplay && !projectLogDisplay)
		{
			// ---------------------------------------------------------------------
			// 27/03/2015 ECU displaying LogCat
			// ---------------------------------------------------------------------
			menu.add (0,R.id.menu_logcat_clear,0,R.string.menu_logcat_clear);
			menu.add (0,R.id.menu_auto_scroll,0,(autoScroll ? R.string.auto_scroll_disable 
															: R.string.auto_scroll_enable));	
			// ---------------------------------------------------------------------	
		}
		// -------------------------------------------------------------------------
		return true;
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
						// ---------------------------------------------------------
						systemInformationTextview.setText ((logcatLatest ? Utilities.GetLogCatEntries (true,filter) 
																		 : Utilities.GetLogCatEntries (filter)));
						// ---------------------------------------------------------
						// 24/03/2015 ECU check for auto scroll but only when the standard
						//                LogCat is being displayed
						// ---------------------------------------------------------
						if (!logcatLatest && autoScroll)
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
						systemInformationTextview.setText (projectLogData (getBaseContext()));
						// ---------------------------------------------------------
						// 24/03/2015 ECU always try and show the end of the text view
						//            ECU only do if 'autoScroll' is true
						// ---------------------------------------------------------
						if (autoScroll)
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
					{
						// ---------------------------------------------------------
						// 04/02/2014 ECU show the text centred
						// ---------------------------------------------------------
						systemInformationTextview.setGravity (Gravity.CENTER);
						// ---------------------------------------------------------
						// 04/02/2014 ECU display the system information
						// 22/11/2014 ECU change as method now returns a String
						// ---------------------------------------------------------
						systemInformationTextview.setText (projectData (getBaseContext()));
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
	        sendMessageDelayed (obtainMessage(StaticData.MESSAGE_REFRESH),theDelayInMillisecs);
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
		// -------------------------------------------------------------------------
		systemInformation += theContext.getString (R.string.api_level) + 
				             	android.os.Build.VERSION.SDK_INT + 
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
		// -------------------------------------------------------------------------
		if (PublicData.keyValue != StaticData.SHARED_PREFERENCES_DEFAULT)
		{
			// ---------------------------------------------------------------------
			// 08/03/2017 ECU indicate that there is already another instance of this
			//                app running
			// ---------------------------------------------------------------------
			systemInformation += theContext.getString (R.string.another_instance) +
									PublicData.dateSimpleFormatDDMMYYHHMM.format(PublicData.keyValue)  + 
									StaticData.NEWLINE;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 20/08/2013 ECU display the current time adjusted for NTP
		// 11/11/2014 ECU change to use the method
		// -------------------------------------------------------------------------
		systemInformation += theContext.getString (R.string.current_time) +  Utilities.getAdjustedTime () +
									StaticData.NEWLINE + StaticData.SEPARATOR;
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
		MainActivity.activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
				
		systemInformation += String.format (theContext.getString(R.string.screen_size_info),
											displayMetrics.widthPixels,
											displayMetrics.heightPixels) + StaticData.NEWLINE;
		// -------------------------------------------------------------------------		
		// 16/07/2013 ECU get the screen density
		// -------------------------------------------------------------------------	
		systemInformation += String.format (theContext.getString(R.string.screen_density), 
								displayMetrics.densityDpi,displayMetrics.density) + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 15/12/2016 ECU try and indicate which 'values' folder is being used
		// -------------------------------------------------------------------------
		systemInformation += theContext.getString (R.string.values_folder_title) + theContext.getString (R.string.values_folder) + StaticData.NEWLINE;
		// -------------------------------------------------------------------------		
		// 23/07/2013 ECU display the width in 'dp'
		// -------------------------------------------------------------------------		
		systemInformation += String.format (theContext.getString(R.string.screen_width_info),
					(float) displayMetrics.widthPixels / displayMetrics.density) + StaticData.NEWLINE;
		// -------------------------------------------------------------------------		
		// 24/07/2013 ECU add telephony information
		// 27/10/2014 ECU changed to use the stored value rather than calling
		//                Utilities,getPhoneNumber
		// -------------------------------------------------------------------------
		systemInformation += theContext.getString (R.string.phone_number_info) + 
									PublicData.phoneNumber + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 25/07/2013 ECU add IP address (WiFi)
		// 25/02/2014 ECU include the network mask
		// -------------------------------------------------------------------------
		systemInformation += theContext.getString (R.string.ip_address_info) + PublicData.ipAddress + 
								" (" + PublicData.networkMask + ")"+ StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 31/12/2014 ECU show the way of accessing this device remotely
		// -------------------------------------------------------------------------
		systemInformation += String.format (theContext.getString (R.string.public_url_info),
											PublicData.storedData.dynamicIPAddress, 
											PublicData.socketNumberForWeb) + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 19/03/2014 ECU display the project folder
		// -------------------------------------------------------------------------
		systemInformation += theContext.getString (R.string.project_folder_info) + 
								PublicData.projectFolder + StaticData.NEWLINE;
		// -------------------------------------------------------------------------		
		// 25/07/2013 ECU print out devices on the network
		// 22/03/2015 ECU change the logic to reflect new List<Devices>
		// -------------------------------------------------------------------------		
		if (PublicData.deviceDetails != null)
		{
			systemInformation += StaticData.SEPARATOR;;
			for (int index=0; index < PublicData.deviceDetails.size(); index++)
			{
				systemInformation += String.format (theContext.getString (R.string.device_info),index, 
										PublicData.deviceDetails.get (index).Print()) + 
											StaticData.NEWLINE + StaticData.SEPARATOR;;	
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
		// -------------------------------------------------------------------------
		byte [] bytesRead = Utilities.readLinesFromEndOfFile (PublicData.projectLogFile,StaticData.SYSTEM_INFO_MAX_LINES);
		// -------------------------------------------------------------------------
		// 26/02/2014 ECU check if there are any entries and give correct
		//                display
		// -------------------------------------------------------------------------
		return ((bytesRead != null) ? new String (bytesRead) 
								    : theContext.getString (R.string.project_log_is_empty));
		// --------------------------------------------------------------------------
	}
	// ==============================================================================
		
	// ==============================================================================
	public boolean onOptionsItemSelected (MenuItem item)
	{
		// -------------------------------------------------------------------------
		// 26/02/2014 ECU default to switching off both flags
		// 24/03/2015 ECU took this out and handled in each case
		// -------------------------------------------------------------------------
		// 16/03/2017 ECU make sure that the text view is in the correct state
		// -------------------------------------------------------------------------
		textViewAdjust (this,systemInformationTextview,true);
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
				// -----------------------------------------------------------------
				autoScroll = !autoScroll;
				// -----------------------------------------------------------------
				// 24/03/2015 ECU indicate what is going on
				// 17/03/2017 ECU changed to use resources
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (getString (R.string.auto_scrolling) + 
												(autoScroll ? getString (R.string.enabled) 
														    : getString (R.string.disabled)));
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
				textViewAdjust (this,systemInformationTextview,false);
				// -----------------------------------------------------------------
				// 11/03/2017 ECU get the 'bugs' from the raw file
				// -----------------------------------------------------------------
				systemInformationTextview.setText (Utilities.readRawResource(this, R.raw.documentation_bugs).replaceAll("\t","    "));
				// -----------------------------------------------------------------
				// 11/03/2017 ECU indicate no screen refresh is wanted
				// -----------------------------------------------------------------
				updateRefreshHandler.noRefresh ();
				// -----------------------------------------------------------------
				return true;
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
				// -----------------------------------------------------------------
				logcatDisplay = true;
				// -----------------------------------------------------------------
				// 24/03/2015 ECU switch off the project log display
				// -----------------------------------------------------------------
				projectLogDisplay = false;
				// -----------------------------------------------------------------
				// 11/03/2014 ECU display the normal LogCat order
				// -----------------------------------------------------------------
				logcatLatest  = false;
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
				// -----------------------------------------------------------------
				DialogueUtilities.textInput(this,getString (R.string.enter_logcat_filter),
											getString (R.string.enter_logcat_filter_summary),
											Utilities.createAMethod (SystemInfoActivity.class,"ConfirmFilter",""),
											Utilities.createAMethod (SystemInfoActivity.class,"CancelFilter",""));
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
				logcatDisplay = true;
				// -----------------------------------------------------------------
				// 24/03/2015 ECU switch off the project log display
				// -----------------------------------------------------------------
				projectLogDisplay = false;
				// -----------------------------------------------------------------
				// 11/03/2014 ECU display the latest LogCat entries first
				// -----------------------------------------------------------------
				logcatLatest  = true;
				// -----------------------------------------------------------------
				break;	
			// =====================================================================
			case R.id.menu_logcat_write:
				// -----------------------------------------------------------------
				// 04/10/2015 ECU created to write the contents of LogCat to file
				// -----------------------------------------------------------------
				Utilities.saveLogCatEntries (PublicData.projectFolder + StaticData.LOGCAT_OUTPUT_FILE,filter);
				// -------------------------------------------------------------------------
				// 05/10/2015 ECU indicate where the data has been written
				// 17/03/2017 ECU change to use resources
				// -------------------------------------------------------------------------
				Utilities.popToast (getString (R.string.logcat_written_to) + 
						             PublicData.projectFolder + StaticData.LOGCAT_OUTPUT_FILE + StaticData.NEWLINE +
										((filter == null) ? getString (R.string.filter_none) 
														  : (String.format (getString (R.string.filter_format),filter))),true);
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
				textViewAdjust (this,systemInformationTextview,false);
				// -----------------------------------------------------------------
				// 11/03/2017 ECU read the 'notes' raw file and display in the
				//                text view
				// -----------------------------------------------------------------
				systemInformationTextview.setText (Utilities.readRawResource(this, R.raw.documentation_notes).replaceAll("\t","    "));
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
				// -----------------------------------------------------------------
				projectLogDisplay = true;
				// -----------------------------------------------------------------
				// 24/03/2015 ECU switch off the LogCat display
				// -----------------------------------------------------------------
				logcatDisplay 	 = false;
				// -----------------------------------------------------------------
				break;
			// =====================================================================	
			case R.id.menu_system_info:
				// -----------------------------------------------------------------
				// 25/02/2014 ECU display the normal system information
				// -----------------------------------------------------------------
				this.setTitle (getString(R.string.title_activity_system_info));
				// -----------------------------------------------------------------
				// 24/03/2015 ECU switch off the LogCat and 'project log' displays
				// -----------------------------------------------------------------
				projectLogDisplay 	= false;
				logcatDisplay 		= false;
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
		}
		// --------------------------------------------------------------------------
		// 04/02/2014 ECU restart the update handler
		// 01/09/2015 ECU changed to use StaticData
		// 21/10/2015 ECU changed to speed up the initial refresh
		// --------------------------------------------------------------------------
		updateRefreshHandler.refresh (StaticData.SYSTEM_INFO_REFRESH_RATE / 10);
		// --------------------------------------------------------------------------
		return true;
	}
	/* ============================================================================== */
	void textViewAdjust (Context theContext,TextView theTextView,boolean theRequiredState)
	{
		// -------------------------------------------------------------------------
		// 11/03/2017 ECU created to adjust the textview to required state
		//
		//					theRequiredState  = true     set default values
		//                                    = false    small font, mono
		// -------------------------------------------------------------------------
		// 16/03/2017 ECU check if the state has changed
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
				// 16/03/2017 ECU change to use system_info... font
				// -----------------------------------------------------------------
				theTextView.setTextSize (TypedValue.COMPLEX_UNIT_PX,
										 getResources ().getDimension (R.dimen.system_info_default_font_size));
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
				theTextView.setTypeface(Typeface.MONOSPACE); 
				theTextView.setGravity (Gravity.LEFT);
				// -----------------------------------------------------------------
				// 15/03/2017 ECU changed to use 'very small font'
				// -----------------------------------------------------------------
				theTextView.setTextSize (TypedValue.COMPLEX_UNIT_PX,
						                 getResources ().getDimension (R.dimen.very_small_font));
				// -----------------------------------------------------------------
				// 16/03/2017 ECU do not want to click on the text view
				// -----------------------------------------------------------------
				theTextView.setClickable (false);
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
	public static void CanceFilter (String theText)
	{
	}
	// =============================================================================
	public static void ConfirmFilter (String theText)
	{
		// -------------------------------------------------------------------------
		// 07/10/2015 ECU set the filter that will be used for selecting particular
		//                LogCat entries
		// -------------------------------------------------------------------------
		filter = theText;
		// -------------------------------------------------------------------------
		// 07/10/2015 ECU check if the filter needs clearing
		// 17/03/2017 ECU changed to use the StaticData
		// -------------------------------------------------------------------------
		if (filter == StaticData.BLANK_STRING)
			filter = null;
		// -------------------------------------------------------------------------
	}
	// ==============================================================================
}

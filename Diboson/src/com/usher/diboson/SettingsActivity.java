package com.usher.diboson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

/**
 * This class provides the mechanism for configuring various
 * project settings.
 * <p>
 * Rather than relying on values stored in the SharedPreferences
 * some of the fields are populated by variables that are stored
 * in various disk files, e.g. email details, patient details, ..
 * 
 * @author Ed Usher
 * 
 */

public class SettingsActivity extends PreferenceActivity 
{
	// =============================================================================
	// see the Notes file for useful information
	//
	// The structure of an Android app is that configurable variables that adjust
	// the way that it operates are held in 'shared preferences' and accessed using a
	// key. However for various reason I have decided to store information in
	// 'PublicData.storedData' as this is easier to propagate to devices that are
	// being cloned and easier from a programming point of view - I may be wrong
	// but will not change the structure unless there is a very good reason.
	// =============================================================================
	// Revision History
	// ================
	// 05/01/2014 ECU created
	// 31/01/2014 ECU because the code has to work on API 9 then am using
	//                methods that are deprecated. As of API 11 the approach is to
	//                use fragments.
	// 12/05/2015 ECU provide validation on the MAC address to remote controller
	// 22/05/2015 ECU try and tidy up the sorting preferences (legend and usage) so that
	//                only one is allowed
	// 12/09/2015 ECU just start grouping some of the code to match the order in
	//				  which they are declared in 'diboson_settings.xml'
	// 21/10/2015 ECU make sure that devices that cannot support WeMo handling do
	//                not have the option of enabling it
	// 23/02/2016 ECU Note - played around with the use of a theme to change the
	//                display - have define styles and a theme (in style.xml) and
	//                declare the theme for SettingsActivity in the manifest to
	//                'PreferenceTheme.Custom'. This currently will give a black
	//                background with white lettering but not sure if I like this.
	// 24/02/2016 ECU Note - sorted out the style issues mentioned above.
	// 23/03/2016 ECU remove the preferences for sensors that do not exist on this
	//                device
	// 08/10/2016 ECU add grouping of activities
	// 12/12/2016 ECU put in checks on the battery levels being set (i.e. the lower
	//                trigger must be below the upper trigger
	//            ECU tidied up the layout of the code
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	/* ============================================================================= */
	//final static String TAG = "SettingsActivity";
	/* ============================================================================= */
	// 31/01/2014 ECU declare keys used within the settings code
	// 17/02/2014 ECU added the ambient light parameters
	// 04/03/2014 ECU added SCHEDULER.....
	// 19/04/2014 ECU added REMOTE......
	// 28/10/2014 ECU added ANNOUNCE.......
	// 18/11/2014 ECU added MONITOR.......
	// 19/11/2014 ECU added MONITOR_START_TIME and MONITOR_STOP_TIME
	// 21/11/2014 ECU added MONITOR_INACTIVE_PERIOD
	// 24/11/2014 ECU added MONITOR_TIMED and MONITOR_DURATION
	// 30/12/2014 ECU added ACQUIRE_STATISTICS
	// 31/12/2014 ECU added DYNAMIC_IP_ADDRESS
	// 16/01/2015 ECU added DEVELOPMENT_FLAG
	// 21/02/2015 ECU added .... WEMO_HANDLING
	// 07/03/2015 ECU added .... DARKNESS_OFF/ON_ACTIONS
	// 14/03/2015 ECU added .... BATTERY options
	// 26/04/2015 ECU added .... SORT_BY_LEGEND
	// 27/04/2015 ECU added .... MONITOR_EMAIL
	// 15/07/2015 ECU added .... PROJECT_LOG_CLEAR
	// 27/08/2015 ECU added .... BLUETOOTH_DISCOVERY
	// 11/09/2015 ECU added .... DRAWABLE_OPACITY
	// 06/11/2015 ECU added .... DEFAULT_DEVELOPER_NAME
	// 01/01/2016 ECU added .... CARER_VISIT_END
	// 02/01/2016 ECU added .... VISIT_START_ACTIONS and VISIT_END_ACTIONS
	// 07/01/2016 ECU added .... CLONE_FILE_SIZE
	// 25/01/2016 ECU added .... CLICK_COUNTER
	// 27/01/2016 ECU added .... CONTACTS and MAIL_PACKAGE_NAME
	// 18/02/2016 ECU added .... ACCELEROMETER_SAMPLE_RATE
	// 22/03/2016 ECU added .... PROXIMITY_FAR and PROXIMITY_NEAR_ACTIONS
	// 23/03/2016 ECU added .... added the CATEGORY keys
	// 27/03/2016 ECU added .... added ALARMS_RESTART_APP
	// 18/05/2016 ECU added .... added START_ACTIVITY_AUTOMATICALLY
	// 09/07/2016 ECU added .... METABROADCAST parameters
	// 22/07/2016 ECU added .... SCHEDULES_DIRECT parameters
	// 08/10/2016 ECU added .... GROUP_ACTIVITIES
	// 08/12/2016 ECU added .... VISIT_..._WARNING_ACTIONS
	// =============================================================================
	private final static String		KEY_CATEGORY_AMBIENT_LIGHT			= "ambient_light_category";
	private final static String		KEY_CATEGORY_SCHEDULES_DIRECT		= "schedules_direct";
	private final static String		KEY_CATEGORY_SENSORS				= "sensors";
	// =============================================================================
	private final static String		KEY_ACCELEROMETER_SAMPLE_RATE	    = "accelerometer_sample_rate";
	private final static String		KEY_ACQUIRE_STATISTICS				= "acquire_statistics";
	private final static String		KEY_ALARMS_RESTART_APP				= "alarms_restart_app";
	private final static String		KEY_AMBIENT_LIGHT_PERIOD			= "ambient_light_period";
	private final static String		KEY_AMBIENT_LIGHT_REARM				= "ambient_light_rearm";
	private final static String     KEY_AMBIENT_LIGHT_TRIGGER			= "ambient_light_trigger";
	private final static String     KEY_ANNOUNCE_FLAG					= "announce_flag";
	private final static String     KEY_BATTERY_LOWER_TRIGGER			= "battery_lower_trigger";
	private final static String     KEY_BATTERY_LOWER_TRIGGER_ACTIONS	= "battery_lower_trigger_actions";
	private final static String     KEY_BATTERY_UPPER_TRIGGER			= "battery_upper_trigger";
	private final static String     KEY_BATTERY_UPPER_TRIGGER_ACTIONS	= "battery_upper_trigger_actions";
	private final static String		KEY_BLUETOOTH_DISCOVERY				= "bluetooth_discovery";
	private final static String		KEY_CARER_VISIT_END					= "carer_visit_end";
	private final static String		KEY_CLICK_COUNTER					= "click_counter";
	private final static String		KEY_CLONE_FILE_SIZE					= "clone_file_size";
	private final static String		KEY_CONTACTS_PACKAGE_NAME			= "contacts_package_name";
	private final static String		KEY_DARKNESS_OFF_ACTIONS			= "darkness_off_actions";
	private final static String		KEY_DARKNESS_ON_ACTIONS				= "darkness_on_actions";
	private final static String		KEY_DEBUG_FLAG						= "debug_flag";
	private final static String		KEY_DEFAULT_DEVELOPER_NAME			= "default_developer_name";
	private final static String		KEY_DEVELOPMENT_FLAG				= "development_flag";
	private final static String		KEY_DRAWABLE_OPACITY				= "drawable_opacity";
	private final static String		KEY_DYNAMIC_IP_ADDRESS				= "dynamic_IP_address";
	private final static String		KEY_EMAIL_FLAG						= "email_flag";
	private final static String		KEY_EMAIL_RECIPIENTS				= "email_recipients";
	private final static String		KEY_EMAIL_SIGNATURE					= "email_signature";
	private final static String		KEY_GROUP_ACTIVITIES				= "group_activities";
	private final static String		KEY_MAIL_PACKAGE_NAME				= "mail_package_name";
	private final static String		KEY_METABROADCAST					= "metabroadcast";
	private final static String		KEY_METABROADCAST_API_KEY			= "metabroadcast_api_key";
	private final static String		KEY_METABROADCAST_CHANNEL_GROUPS_URL
																		= "metabroadcast_channel_groups_URL";
	private final static String		KEY_METABROADCAST_CHANNEL_URL		= "metabroadcast_channel_URL";
	private final static String		KEY_MONITOR_DURATION				= "monitor_duration";
	private final static String		KEY_MONITOR_EMAIL					= "monitor_email";
	private final static String		KEY_MONITOR_ENABLE					= "monitor_enable";
	private final static String     KEY_MONITOR_INACTIVE_PERIOD			= "monitor_inactive_period";
	private final static String     KEY_MONITOR_START_TIME				= "monitor_start_time";
	private final static String     KEY_MONITOR_STOP_TIME				= "monitor_stop_time";
	private final static String		KEY_MONITOR_TIMED					= "monitor_timed";
	private final static String		KEY_MONITOR_TRIGGER					= "monitor_trigger";
	private final static String		KEY_PANIC_ALARM_SHAKE_IGNORE_PERIOD	= "panic_alarm_shake_ignore_period";
	private final static String		KEY_PANIC_ALARM_SHAKE_NUMBER		= "panic_alarm_shake_number";
	private final static String		KEY_PANIC_ALARM_SHAKE_RESET_PERIOD	= "panic_alarm_shake_reset_period";
	private final static String		KEY_PANIC_ALARM_SHAKE_THRESHOLD		= "panic_alarm_shake_threshold";
	private final static String		KEY_PATIENT_ADDRESS					= "patient_address";
	private final static String		KEY_PATIENT_DOB						= "patient_dob";
	private final static String		KEY_PATIENT_NAME					= "patient_name";
	private final static String		KEY_PATIENT_NHS_NUMBER				= "patient_nhs_number";
	private final static String		KEY_PATIENT_PHONE_NUMBER			= "patient_phone_number";
	private final static String		KEY_PATIENT_PREFERRED_NAME			= "patient_preferred_name";
	private final static String     KEY_PROJECT_LOG_CLEAR				= "project_log_clear";
	private final static String     KEY_PROJECT_LOG_ENABLE				= "project_log_enable";
	private final static String     KEY_PROXIMITY_FAR_ACTIONS			= "proximity_far_actions";
	private final static String     KEY_PROXIMITY_NEAR_ACTIONS			= "proximity_near_actions";
	private final static String     KEY_REMOTE_ALWAYS					= "remote_always";
	private final static String     KEY_REMOTE_MAC_ADDRESS				= "remote_MAC_address";	
	private final static String		KEY_SCHEDULER_TIME					= "scheduler_time";
	private final static String		KEY_SCHEDULES_DIRECT_DAYS			= "schedules_direct_days";
	private final static String		KEY_SCHEDULES_DIRECT_LINEUP			= "schedules_direct_lineup";
	private final static String		KEY_SCHEDULES_DIRECT_PASSWORD		= "schedules_direct_password";
	private final static String		KEY_SCHEDULES_DIRECT_URL			= "schedules_direct_url";
	private final static String		KEY_SCHEDULES_DIRECT_USERNAME		= "schedules_direct_username";
	private final static String     KEY_SORT_BY_LEGEND					= "sort_by_legend";
	private final static String     KEY_SORT_BY_USAGE					= "sort_by_usage";
	private final static String		KEY_START_ACTIVITY_AUTOMATICALLY	= "start_activity_auto";
	private final static String		KEY_USER_VIEW						= "user_view";
	private final static String		KEY_VISIT_END_ACTIONS				= "visit_end_actions";
	private final static String		KEY_VISIT_END_WARNING_ACTIONS		= "visit_end_warning_actions";
	private final static String		KEY_VISIT_START_ACTIONS				= "visit_start_actions";
	private final static String		KEY_VISIT_START_WARNING_ACTIONS		= "visit_start_warning_actions";
	private final static String		KEY_WEMO_HANDLING					= "wemo_handling";
	/* ============================================================================= */
	// =============================================================================
	static SharedPreferences 	sharedPreferences;
	// -----------------------------------------------------------------------------
	static Activity				activity;								// 18/05/2016 ECU added
	// -----------------------------------------------------------------------------
	// 22/05/2015 ECU declare the check boxes that are used to select sorting and which
	//                will be updated dynamically
	// -----------------------------------------------------------------------------
	static CheckBoxPreference 	sortByLegendCheckBox;					// 22/05/2015 ECU added
	static CheckBoxPreference 	sortByUsageCheckBox;					// 22/05/2015 ECU added
	// =============================================================================
	@SuppressWarnings("deprecation")
	@Override 
	public void onCreate (Bundle savedInstanceState) 
	{     
		// -------------------------------------------------------------------------
		// 28/11/2016 ECU set full screen - needed to add this for Sony XA
		// -------------------------------------------------------------------------
		this.getWindow().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN,
				                   WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// -------------------------------------------------------------------------
		// 17/03/2016 ECU do not display a title
		// -------------------------------------------------------------------------
		this.requestWindowFeature (Window.FEATURE_NO_TITLE);
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);  
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU remember the current context
		// -------------------------------------------------------------------------
		activity = this;
		// -------------------------------------------------------------------------
		// 24/02/2014 ECU set display to portrait mode
		// -------------------------------------------------------------------------
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// -------------------------------------------------------------------------
		// 04/02/2014 ECU get the currently stored preferences
		// -------------------------------------------------------------------------
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences (this); 
		// -------------------------------------------------------------------------
		// 04/02/2014 ECU create an editor for the preferences
		// -------------------------------------------------------------------------
		Editor editor = sharedPreferences.edit ();
		// -------------------------------------------------------------------------
		
		// =========================================================================
		// P A T I E N T Preference Category
		// =================================
		// Settings concerning the patient being handled on this device
		// =========================================================================
		
		// -------------------------------------------------------------------------
		// 05/01/2014 ECU if patient details exist then preset the preferences
		// -------------------------------------------------------------------------
		if (PublicData.patientDetails != null)
		{
			editor.putString  (KEY_PATIENT_NAME, 			PublicData.patientDetails.Name());
			editor.putString  (KEY_PATIENT_PREFERRED_NAME, 	PublicData.patientDetails.preferredName);
			editor.putString  (KEY_PATIENT_DOB,				PublicData.patientDetails.dateOfBirth);
			editor.putString  (KEY_PATIENT_ADDRESS,			PublicData.patientDetails.address);
			editor.putString  (KEY_PATIENT_PHONE_NUMBER, 	PublicData.patientDetails.phoneNumber);
			editor.putString  (KEY_PATIENT_NHS_NUMBER, 		PublicData.patientDetails.referenceNumber);
		}	
		   
		// =========================================================================
		// =========================================================================
		
		// =========================================================================
		// P R O J E C T Preference Category
		// =================================
		// Settings concerning the overall control of the project
		// =========================================================================
		// -------------------------------------------------------------------------
		// 27/08/2015 ECU option to initiate a bluetooth discovery
		// -------------------------------------------------------------------------
		editor.putBoolean (KEY_BLUETOOTH_DISCOVERY, PublicData.storedData.bluetoothDiscovery);
		// -------------------------------------------------------------------------
		// 10/01/2015 ECU show the state of the debugMode flag
		// 10/03/2015 ECU changed to use 'debugMode in 'storedData'
		// -------------------------------------------------------------------------
		editor.putBoolean (KEY_DEBUG_FLAG, PublicData.storedData.debugMode);
		// -------------------------------------------------------------------------
		// 16/01/2015 ECU show the state of the developmentMode flag
		// 10/03/2015 ECU changed to use 'developmentMode' in 'storedData'
		// -------------------------------------------------------------------------
		editor.putBoolean (KEY_DEVELOPMENT_FLAG, PublicData.storedData.developmentMode);
		// -------------------------------------------------------------------------
		// 06/11/2015 ECU set the default developer name
		// -------------------------------------------------------------------------
		editor.putString  (KEY_DEFAULT_DEVELOPER_NAME,PublicData.storedData.developerName);
		// -------------------------------------------------------------------------
		// 28/01/2014 ECU set the checkbox for the user's view
		// -------------------------------------------------------------------------
		editor.putBoolean (KEY_USER_VIEW, PublicData.storedData.userView);
		// -------------------------------------------------------------------------
		// 08/10/2016 ECU set the checkbox for the grouping of activities
		// -------------------------------------------------------------------------
		editor.putBoolean (KEY_GROUP_ACTIVITIES, PublicData.storedData.groupActivities);
		// -------------------------------------------------------------------------   
		// 25/02/2014 ECU set the checkbox for the 'writing to project log'
		// -------------------------------------------------------------------------  
		editor.putBoolean (KEY_PROJECT_LOG_ENABLE, PublicData.storedData.projectLogEnabled);
		// -------------------------------------------------------------------------   
		// 15/07/2015 ECU set the checkbox for 'clearing the project log'
		// -------------------------------------------------------------------------  
		editor.putBoolean (KEY_PROJECT_LOG_CLEAR, PublicData.storedData.clearProjectLog);
		// -------------------------------------------------------------------------  
		// 04/03/2014 ECU add the scheduler bits
		// 07/03/2015 ECU change to use correct time preference which is customised
		// -------------------------------------------------------------------------  
		editor.putString  (KEY_SCHEDULER_TIME,DateTime.returnTime (PublicData.storedData.schedulerHour,
																	PublicData.storedData.schedulerMinute)); 
		// -------------------------------------------------------------------------
		// 28/10/2014 ECU add the announce flag
		// -------------------------------------------------------------------------
		editor.putBoolean (KEY_ANNOUNCE_FLAG, PublicData.storedData.announceFlag);
		// -------------------------------------------------------------------------
		// 30/12/2014 ECU add the acquire statistics flag
		// -------------------------------------------------------------------------
		editor.putBoolean (KEY_ACQUIRE_STATISTICS, PublicData.storedData.acquireStatistics);
		// -------------------------------------------------------------------------
		// 12/09/2015 ECU for some reason the dunamic IP address was not being set
		//                from the stored data (just from preference) so added this
		// -------------------------------------------------------------------------
		editor.putString  (KEY_DYNAMIC_IP_ADDRESS,PublicData.storedData.dynamicIPAddress);
		// -------------------------------------------------------------------------
		// 21/02/2015 ECU show the state of WeMo handling
		// -------------------------------------------------------------------------
		editor.putBoolean (KEY_WEMO_HANDLING, PublicData.storedData.wemoHandling);
		// -------------------------------------------------------------------------
		// 11/09/2015 ECU display opacity for 'slide in' drawables - used
		//                in DisplayADrawableActivity
		// -------------------------------------------------------------------------
		editor.putInt     (KEY_DRAWABLE_OPACITY,PublicData.storedData.drawableOpacity);
		// -------------------------------------------------------------------------
		// 07/01/2016 ECU display the clone file size
		// -------------------------------------------------------------------------
		editor.putInt     (KEY_CLONE_FILE_SIZE,PublicData.storedData.cloneFileSize);
		// -------------------------------------------------------------------------
		// 25/01/2016 ECU display the click counter
		// -------------------------------------------------------------------------
		editor.putInt     (KEY_CLICK_COUNTER,PublicData.storedData.clickCounter);
		// -------------------------------------------------------------------------
		// 27/03/2016 ECU indicate if alarms restart the app
		// -------------------------------------------------------------------------
		editor.putBoolean (KEY_ALARMS_RESTART_APP, PublicData.storedData.alarmsRestartApp);
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU indicate if activity is to be automatically started
		// -------------------------------------------------------------------------
		editor.putBoolean (KEY_START_ACTIVITY_AUTOMATICALLY, PublicData.storedData.activityOnStart);
		// =========================================================================
		// =========================================================================
		
		// =========================================================================
		// S O R T I N G Preference Category
		// =================================
		// Settings concerning the sorting of the user interface
		// =========================================================================
		
		// -------------------------------------------------------------------------
		// 10/02/2014 ECU set the checkbox for the 'sort by usage'
		// -------------------------------------------------------------------------
		editor.putBoolean (KEY_SORT_BY_USAGE, PublicData.storedData.sortByUsage);
		// -------------------------------------------------------------------------
		// 26/04/2015ECU set the checkbox for the 'sort by legend'
		// -------------------------------------------------------------------------  
		editor.putBoolean (KEY_SORT_BY_LEGEND, PublicData.storedData.sortByLegend);
		// =========================================================================
		// =========================================================================
		
		// =========================================================================
		// A M B I E N T  L I G H T  Preference Category
		// =============================================
		// Settings concerning the ambient light monitoring
		// =========================================================================
		
		// -------------------------------------------------------------------------  
		// 17/02/2014 ECU add the ambient light bits
		// 21/11/2014 ECU changed to use the seek bar
		// 02/03/2015 ECU added the _COUNTER
		// 06/03/2015 ECU changed the _COUNTER to be _PERIOD
		// -------------------------------------------------------------------------   
		editor.putInt     (KEY_AMBIENT_LIGHT_PERIOD,PublicData.storedData.ambient_light_period);
		editor.putInt	  (KEY_AMBIENT_LIGHT_REARM,PublicData.storedData.ambient_light_rearm);
		editor.putInt     (KEY_AMBIENT_LIGHT_TRIGGER,PublicData.storedData.ambient_light_trigger);
		// -------------------------------------------------------------------------
		// 07/03/2015 ECU display the actions for darkness off and on
		// -------------------------------------------------------------------------
		editor.putString  (KEY_DARKNESS_OFF_ACTIONS,PublicData.storedData.darknessOffActions);
		editor.putString  (KEY_DARKNESS_ON_ACTIONS,PublicData.storedData.darknessOnActions);
		// =========================================================================
		// =========================================================================
		
		// =========================================================================
		// B A T T E R Y  C H A R G E  Preference Category
		// ===============================================
		// Settings concerning the battery charge monitoring
		// =========================================================================
		// -------------------------------------------------------------------------
		// 14/03/2015 ECU added to configure battery options
		// 26/04/2015 ECU added the actions
		// -------------------------------------------------------------------------
		editor.putInt     (KEY_BATTERY_LOWER_TRIGGER,PublicData.storedData.battery.lowerTrigger);
		editor.putInt     (KEY_BATTERY_UPPER_TRIGGER,PublicData.storedData.battery.upperTrigger);
		editor.putString  (KEY_BATTERY_LOWER_TRIGGER_ACTIONS,PublicData.storedData.battery.lowerTriggerActions);
		editor.putString  (KEY_BATTERY_UPPER_TRIGGER_ACTIONS,PublicData.storedData.battery.upperTriggerActions);
		// =========================================================================
		// =========================================================================
		
		// =========================================================================
		// E M A I L   Preference Category
		// ===============================
		// Settings concerning the email use
		// =========================================================================
		// -------------------------------------------------------------------------
		// 05/01/2014 ECU if email details exist then preset the preferences
		// 12/09/2015 ECU for some reason 'enabled' was being set used on the
		//                app's preference data
		// 27/03/2016 ECU changed to use the signature method
		// -------------------------------------------------------------------------
		if (PublicData.emailDetails != null)
		{
			editor.putBoolean (KEY_EMAIL_FLAG, PublicData.emailDetails.enabled);
			editor.putString  (KEY_EMAIL_SIGNATURE, PublicData.emailDetails.Signature (this));
			editor.putString  (KEY_EMAIL_RECIPIENTS, PublicData.emailDetails.recipients);
		}
		// =========================================================================
		// =========================================================================
		
		
		// =========================================================================
		// R E M O T E  C O N T R O L L E R  Preference Category
		// =====================================================
		// Settings concerning the remote controller
		// =========================================================================
		// -------------------------------------------------------------------------
		// 19/04/2014 ECU add the remote controller bits
		// -------------------------------------------------------------------------
		editor.putString  (KEY_REMOTE_MAC_ADDRESS,PublicData.storedData.remoteMACAddress);
		editor.putBoolean (KEY_REMOTE_ALWAYS, PublicData.storedData.remoteAlways);
		// =========================================================================
		// =========================================================================
		
		// =========================================================================
		// M O N I T O R I N G  Preference Category
		// ========================================
		// Settings concerning the monitoring system
		// =========================================================================
		// -------------------------------------------------------------------------
		// 18/11/2014 ECU add the monitor enablement flag and trigger level
		// 20/11/2014 ECU changed to use the triggerLevel as an integer
		// 27/04/2015 ECU added email option
		// -------------------------------------------------------------------------
		editor.putBoolean (KEY_MONITOR_EMAIL, PublicData.storedData.monitor.email);
		editor.putBoolean (KEY_MONITOR_ENABLE, PublicData.storedData.monitor.enabled);
		editor.putInt  	  (KEY_MONITOR_TRIGGER,PublicData.storedData.monitor.triggerLevel);
		editor.putString  (KEY_MONITOR_START_TIME,PublicData.storedData.monitor.returnStartTime());
		editor.putString  (KEY_MONITOR_STOP_TIME,PublicData.storedData.monitor.returnStopTime());
		editor.putInt  	  (KEY_MONITOR_INACTIVE_PERIOD,PublicData.storedData.monitor.inactivePeriod);
		// -------------------------------------------------------------------------
		// 24/11/2014 ECU added the options to enable the sound recorder to be
		//                started if a noise is detected
		// -------------------------------------------------------------------------
		editor.putBoolean (KEY_MONITOR_TIMED, PublicData.storedData.monitor.timedRecording);
		editor.putInt  	  (KEY_MONITOR_DURATION,PublicData.storedData.monitor.duration);
		// =========================================================================
		// =========================================================================
		
		// =========================================================================
		// =========================================================================
		// P A N I C   A L A R M   S H A K E  Preference Category
		// ======================================================
		// Settings concerning the shaking facility associated with the Panic Alarm
		// 10/12/2015 ECU the 'periods' are stored as nanoseconds
		// 14/01/2016 ECU put in the check on null
		// =========================================================================
		if (PublicData.storedData.panicAlarm == null)
		{
			PublicData.storedData.panicAlarm = new PanicAlarm ();
		}
		// -------------------------------------------------------------------------
		editor.putInt  	 (KEY_PANIC_ALARM_SHAKE_IGNORE_PERIOD,(int) (PublicData.storedData.panicAlarm.shakeIgnorePeriod/StaticData.NANOSECONDS_PER_MILLISECOND));
		editor.putInt  	 (KEY_PANIC_ALARM_SHAKE_NUMBER,PublicData.storedData.panicAlarm.shakeNumber);
		editor.putInt  	 (KEY_PANIC_ALARM_SHAKE_RESET_PERIOD,(int) (PublicData.storedData.panicAlarm.shakeResetPeriod/StaticData.NANOSECONDS_PER_SECOND));
		editor.putInt 	 (KEY_PANIC_ALARM_SHAKE_THRESHOLD,(int) (PublicData.storedData.panicAlarm.shakeThreshold * StaticData.PANIC_ALARM_SCALE));
		// =========================================================================
		
		// =========================================================================
		// CARER VISIT  Preference Category
		// ================================
		// Settings concerning the visit of a carer
		// =========================================================================
		editor.putInt  	  (KEY_CARER_VISIT_END,PublicData.storedData.visit_end_minutes);
		// -------------------------------------------------------------------------
		// 02/01/2016 ECU add the actions to be taken at the start and end of the
		//                visit
		// -------------------------------------------------------------------------
		editor.putString  (KEY_VISIT_END_ACTIONS,PublicData.storedData.visit_end_actions);
		editor.putString  (KEY_VISIT_START_ACTIONS,PublicData.storedData.visit_start_actions);
		// -------------------------------------------------------------------------
		// 08/12/2016 ECU add the actions to be taken when the warnings of the 
		//                start and end of a visit occur
		// -------------------------------------------------------------------------
		editor.putString  (KEY_VISIT_END_WARNING_ACTIONS,PublicData.storedData.visit_end_warning_actions);
		editor.putString  (KEY_VISIT_START_WARNING_ACTIONS,PublicData.storedData.visit_start_warning_actions);
		// =========================================================================
		
		// =========================================================================
		// SENSORS  Preference Category
		// ============================
		// Settings associated with the in-built sensors
		// =========================================================================
		editor.putInt  	  (KEY_ACCELEROMETER_SAMPLE_RATE,PublicData.storedData.accelerometerSampleRate);
		// -------------------------------------------------------------------------
		// 22/03/2016 ECU added the actions associated with the proximity sensor
		// -------------------------------------------------------------------------
		editor.putString  (KEY_PROXIMITY_NEAR_ACTIONS,PublicData.storedData.proximityNearActionCommands);
		editor.putString  (KEY_PROXIMITY_FAR_ACTIONS,PublicData.storedData.proximityFarActionCommands);
		// =========================================================================
		
		// =========================================================================
		// METABROADCAST  Preference Category
		// ==================================
		// Settings associated with the interface to MetaBroadcast's ATLAS system
		// =========================================================================
		// 09/07/2016 ECU check if it has been initialised
		// -------------------------------------------------------------------------
		if (TVChannelsActivity.METABROADCAST)
		{
			if (!TVChannelsActivity.XMLTV && (PublicData.storedData.metaBroadcast == null))
			{
				PublicData.storedData.metaBroadcast 
						= new MetaBroadcast (getString (R.string.api_key),
											 getString (R.string.channel_groups_url),
											 getString (R.string.channel_url));
			}
			// ---------------------------------------------------------------------
			editor.putString  (KEY_METABROADCAST_API_KEY,PublicData.storedData.metaBroadcast.apiKey);
			editor.putString  (KEY_METABROADCAST_CHANNEL_GROUPS_URL,PublicData.storedData.metaBroadcast.channelGroupsURL);
			editor.putString  (KEY_METABROADCAST_CHANNEL_URL,PublicData.storedData.metaBroadcast.channelURL);
		}	
		// =========================================================================
		
		// =========================================================================
		// SCHEDULES DIRECT  Preference Category
		// =====================================
		// Settings associated with the interface to the Schedules Direct system
		// =========================================================================
		// 22/07/2016 ECU check if it has been initialised
		// -------------------------------------------------------------------------
		if (TVChannelsActivity.SCHEDULES_DIRECT)
		{
			if (!TVChannelsActivity.XMLTV && (PublicData.storedData.schedulesDirectData == null))
			{
				PublicData.storedData.schedulesDirectData 
						= new SchedulesDirectData (getString (R.string.schedules_direct_username_default),
											       getString (R.string.schedules_direct_password_default));
			}
			// ---------------------------------------------------------------------
			editor.putString  (KEY_SCHEDULES_DIRECT_USERNAME,PublicData.storedData.schedulesDirectData.userName);
			editor.putString  (KEY_SCHEDULES_DIRECT_PASSWORD,PublicData.storedData.schedulesDirectData.password);
			editor.putInt  	  (KEY_SCHEDULES_DIRECT_DAYS,PublicData.storedData.schedulesDirectData.numberOfDays);
			editor.putString  (KEY_SCHEDULES_DIRECT_URL,PublicData.storedData.schedulesDirectData.baseURL);
			editor.putString  (KEY_SCHEDULES_DIRECT_LINEUP,PublicData.storedData.schedulesDirectData.lineUp);
			// ---------------------------------------------------------------------
		}
		// =========================================================================
		
		// -------------------------------------------------------------------------
		// 05/01/2014 ECU commit the values
		// -------------------------------------------------------------------------
		editor.commit();
		// -------------------------------------------------------------------------
		// 04/02/2014 ECU set up the display from the XML file
		// -------------------------------------------------------------------------
		addPreferencesFromResource (R.xml.diboson_settings);
		// -------------------------------------------------------------------------
		// 04/02/2014 ECU make sure that the summary reflects the changing
		//                data.
		// 31/12/2014 ECU added the dynamic IP address
		// -------------------------------------------------------------------------
		bindPreferenceSummaryToValue (findPreference (KEY_PATIENT_NAME));
		bindPreferenceSummaryToValue (findPreference (KEY_PATIENT_PREFERRED_NAME));
		bindPreferenceSummaryToValue (findPreference (KEY_PATIENT_DOB));
		bindPreferenceSummaryToValue (findPreference (KEY_PATIENT_ADDRESS));
		bindPreferenceSummaryToValue (findPreference (KEY_PATIENT_PHONE_NUMBER));
		bindPreferenceSummaryToValue (findPreference (KEY_PATIENT_NHS_NUMBER));
		bindPreferenceSummaryToValue (findPreference (KEY_EMAIL_SIGNATURE));
		bindPreferenceSummaryToValue (findPreference (KEY_EMAIL_RECIPIENTS));
		bindPreferenceSummaryToValue (findPreference (KEY_DYNAMIC_IP_ADDRESS));
		// -------------------------------------------------------------------------
		// 17/02/2014 ECU added ambient light variables
		// 21/11/2014 ECU changed to use ...Integer method
		// 02/03/2015 ECU included _COUNTER
		// 06/03/2015 ECU changed to _PERIOD from _COUNTER
		// 26/04/2015 ECU added the 'darkness' actions
		// -------------------------------------------------------------------------
		bindPreferenceSummaryToValueInteger 	(findPreference (KEY_AMBIENT_LIGHT_PERIOD));
		bindPreferenceSummaryToValueInteger 	(findPreference (KEY_AMBIENT_LIGHT_REARM));
		bindPreferenceSummaryToValueInteger 	(findPreference (KEY_AMBIENT_LIGHT_TRIGGER));
		bindPreferenceSummaryToValue 			(findPreference (KEY_DARKNESS_ON_ACTIONS));	
		bindPreferenceSummaryToValue			(findPreference (KEY_DARKNESS_OFF_ACTIONS));
		// -------------------------------------------------------------------------
		// 04/05/2016 ECU try and monitor changes to the trigger
		// -------------------------------------------------------------------------
		findPreference (KEY_AMBIENT_LIGHT_TRIGGER).setOnPreferenceChangeListener (UpdateAmbientLightTrigger);
		// -------------------------------------------------------------------------
		// 06/11/2015 ECU bind the default developer name
		// -------------------------------------------------------------------------
		bindPreferenceSummaryToValue (findPreference (KEY_DEFAULT_DEVELOPER_NAME));
		// -------------------------------------------------------------------------
		// 19/04/2014 ECU remote device - MAC address
		// -------------------------------------------------------------------------
		bindPreferenceSummaryToValue (findPreference (KEY_REMOTE_MAC_ADDRESS));
		// -------------------------------------------------------------------------
		// 18/11/2014 ECU bind to monitor trigger
		// -------------------------------------------------------------------------
		bindPreferenceSummaryToValueInteger (findPreference (KEY_MONITOR_TRIGGER));	   
		// -------------------------------------------------------------------------
		// 19/11/2014 ECU bind to monitor the start and stop times
		// -------------------------------------------------------------------------
		bindPreferenceSummaryToValue (findPreference (KEY_MONITOR_START_TIME));	  
		bindPreferenceSummaryToValue (findPreference (KEY_MONITOR_STOP_TIME));	
		// -------------------------------------------------------------------------
		// 21/11/2014 ECU include the inactive period after 'noise' heard
		// -------------------------------------------------------------------------
		bindPreferenceSummaryToValueInteger (findPreference (KEY_MONITOR_INACTIVE_PERIOD));	
		// -------------------------------------------------------------------------
		// 24/11/2014 ECU include the duration for timed recording
		// -------------------------------------------------------------------------
		bindPreferenceSummaryToValueInteger (findPreference (KEY_MONITOR_DURATION));	
		// -------------------------------------------------------------------------
		// 11/10/2014 ECU try and bind the user view
		// -------------------------------------------------------------------------
		findPreference (KEY_USER_VIEW).setOnPreferenceChangeListener (UpdateGridDisplay);
		// -------------------------------------------------------------------------	
		// 16/01/2015 ECU try and pick up change on development mode
		// -------------------------------------------------------------------------
		findPreference (KEY_DEVELOPMENT_FLAG).setOnPreferenceChangeListener (UpdateGridDisplay);
		// -------------------------------------------------------------------------	
		// 22/05/2015 ECU added sorting by usage and legend
		// -------------------------------------------------------------------------
		findPreference (KEY_SORT_BY_LEGEND).setOnPreferenceChangeListener (UpdateGridDisplay);
		findPreference (KEY_SORT_BY_USAGE).setOnPreferenceChangeListener (UpdateGridDisplay);
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU activity to be started automatically needs a listener
		// -------------------------------------------------------------------------
		findPreference (KEY_START_ACTIVITY_AUTOMATICALLY).setOnPreferenceChangeListener (UpdateStartActivityAutomatically);
		// -------------------------------------------------------------------------
		// 21/10/2015 ECU check the state of the WeMo handling checkbox
		// -------------------------------------------------------------------------
		findPreference (KEY_WEMO_HANDLING).setOnPreferenceChangeListener (UpdateGridDisplay);
		// -------------------------------------------------------------------------	
		// 12/05/2015 ECU try and pick up change MAC address
		// -------------------------------------------------------------------------
		findPreference (KEY_REMOTE_MAC_ADDRESS).setOnPreferenceChangeListener (UpdateMACAddress);
		// -------------------------------------------------------------------------
		// 14/03/2015 ECU added to configure battery options
		// 26/04/2015 ECU added the actions
		// -------------------------------------------------------------------------
		bindPreferenceSummaryToValueInteger 	(findPreference (KEY_BATTERY_LOWER_TRIGGER));
		bindPreferenceSummaryToValueInteger 	(findPreference (KEY_BATTERY_UPPER_TRIGGER));
		bindPreferenceSummaryToValue 			(findPreference (KEY_BATTERY_LOWER_TRIGGER_ACTIONS));
		bindPreferenceSummaryToValue			(findPreference (KEY_BATTERY_UPPER_TRIGGER_ACTIONS));
		// -------------------------------------------------------------------------
		// 02/01/2016 ECU add the actions associated with visits
		// -------------------------------------------------------------------------
		bindPreferenceSummaryToValue 			(findPreference (KEY_VISIT_END_ACTIONS));	
		bindPreferenceSummaryToValue			(findPreference (KEY_VISIT_START_ACTIONS));
		// -------------------------------------------------------------------------
		// 08/12/2016 ECU add the actions associated with warnings of visits
		// -------------------------------------------------------------------------
		bindPreferenceSummaryToValue 			(findPreference (KEY_VISIT_END_WARNING_ACTIONS));	
		bindPreferenceSummaryToValue			(findPreference (KEY_VISIT_START_WARNING_ACTIONS));
		// -------------------------------------------------------------------------
		// 22/05/2015 ECU set up check boxes that change dynamically
		// -------------------------------------------------------------------------
		sortByLegendCheckBox	= (CheckBoxPreference)(findPreference (KEY_SORT_BY_LEGEND));
		sortByUsageCheckBox 	= (CheckBoxPreference)(findPreference (KEY_SORT_BY_USAGE));
		
		// =========================================================================
		// PACKAGE NAME  Preference Category
		// =================================
		// Settings concerning the visit of a carer
		// =========================================================================
		ListPreference contactsListPreference = (ListPreference) findPreference (KEY_CONTACTS_PACKAGE_NAME);
		ListPreference mailListPreference     = (ListPreference) findPreference (KEY_MAIL_PACKAGE_NAME);
		// -------------------------------------------------------------------------
		// 27/01/2016 ECU now populate the data
		// -------------------------------------------------------------------------
		CharSequence [] prefData = (CharSequence []) Utilities.PickAPackagePopulate(getBaseContext());
		// -------------------------------------------------------------------------
		// 27/01/2016 ECU and set into the preference
		// -------------------------------------------------------------------------
		contactsListPreference.setEntries (prefData);
		contactsListPreference.setEntryValues (prefData);
		mailListPreference.setEntries (prefData);
		mailListPreference.setEntryValues (prefData);
		// -------------------------------------------------------------------------
		// 27/01/2016 ECU set the default value if set
		// -------------------------------------------------------------------------
		if (PublicData.storedData.contactsPackageName != null)
			contactsListPreference.setDefaultValue (PublicData.storedData.contactsPackageName);
		if (PublicData.storedData.mailPackageName != null)
			contactsListPreference.setDefaultValue (PublicData.storedData.mailPackageName);
		// -------------------------------------------------------------------------
		// 23/03/2016 ECU remove any preferences which relate to hardware which
		//                is not on this device
		// -------------------------------------------------------------------------
		if (SensorService.proximitySensor == null)
		{
			removePreference (KEY_CATEGORY_SENSORS,KEY_PROXIMITY_FAR_ACTIONS);
			removePreference (KEY_CATEGORY_SENSORS,KEY_PROXIMITY_NEAR_ACTIONS);
		}
		if (SensorService.lightSensor == null)
		{
			removePreference (KEY_CATEGORY_AMBIENT_LIGHT);
		}
		if (!TVChannelsActivity.METABROADCAST)
		{
			removePreference (KEY_METABROADCAST);
		}
		if (!TVChannelsActivity.SCHEDULES_DIRECT)
		{
			removePreference (KEY_CATEGORY_SCHEDULES_DIRECT);
		}
		// -------------------------------------------------------------------------
		// 27/01/2016 ECU set up the listeners
		// -------------------------------------------------------------------------
		contactsListPreference.setOnPreferenceChangeListener (listPreferenceChangeListener);
		mailListPreference.setOnPreferenceChangeListener (listPreferenceChangeListener);
		// =========================================================================
	} 
	// =========================================================================
	@Override
	public void onActivityResult (int theRequestCode, int theResultCode, Intent theIntent) 
	{
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU called when an activity returns a result
		// -------------------------------------------------------------------------
	    if (theRequestCode == StaticData.RESULT_CODE_INTENT)
	    {
	    	 if (theResultCode == RESULT_OK) 
		     {     	
	    		 // ----------------------------------------------------------------
	    		 // 28/11/2014 ECU remember the intent that needs to be used
	    		 //                when starting the activity
	    		 // ----------------------------------------------------------------
	    		 PublicData.storedData.activityOnStartNumber 
	    		 	= theIntent.getIntExtra (StaticData.PARAMETER_POSITION,StaticData.NO_RESULT);
	    		 // ----------------------------------------------------------------  
		     } 
		     else 
		     if (theResultCode == RESULT_CANCELED) 
		     {
		    	 // ----------------------------------------------------------------
		    	 // 28/11/2014 ECU added
		    	 // ----------------------------------------------------------------
		     }
	    }
	}
	// =============================================================================
	@Override
	public void onBackPressed () 
	{
		// -------------------------------------------------------------------------
		// 12/12/2016 ECU check for any validation that may prevent the activity from
		//                finishing
		// -------------------------------------------------------------------------
		if (validateBatteryTriggers ())
		{
			// ---------------------------------------------------------------------
			// 12/12/2016 ECU validation passed so can finish this activity
			// ---------------------------------------------------------------------
			finish ();
			super.onBackPressed ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override 
	public boolean onCreateOptionsMenu(Menu menu) 
	{ 
		return super.onCreateOptionsMenu(menu);
	} 
	// =============================================================================
	@Override
	public void onDestroy()
	{	
		// -------------------------------------------------------------------------
		// 05/01/2014 ECU set other variables from the preferences
		// 10/03/2015 ECU changed to use 'debugMode in 'storedData'
		// 22/05/2015 ECU removed 'sortByLegend' and 'sortByUsage' which are now
		//                handled in UpdateFridDisplay
		// -------------------------------------------------------------------------
		// 27/08/2015 ECU set bluetooth discovery option
		// -------------------------------------------------------------------------
		PublicData.storedData.bluetoothDiscovery
				= sharedPreferences.getBoolean (KEY_BLUETOOTH_DISCOVERY, false);
		// -------------------------------------------------------------------------
		PublicData.storedData.debugMode	= sharedPreferences.getBoolean (KEY_DEBUG_FLAG, false);
		// -------------------------------------------------------------------------
		// 28/01/2014 ECU added to select the user view
		// -------------------------------------------------------------------------
		PublicData.storedData.userView	= sharedPreferences.getBoolean (KEY_USER_VIEW, false);
		// -------------------------------------------------------------------------
		// 08/10/2016 ECU added to select grouping of activities
		// -------------------------------------------------------------------------
		boolean localValue = sharedPreferences.getBoolean (KEY_GROUP_ACTIVITIES, false);
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU check if grouping has changed and tell the use if it has
		// -------------------------------------------------------------------------
		if (PublicData.storedData.groupActivities != localValue)
		{
			PublicData.storedData.groupActivities	= localValue;
			// ---------------------------------------------------------------------
			// 11/10/2016 ECU tell the user about restarting the app
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (getString (R.string.group_restart),true);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 08/10/2016 ECU reset the group pointer to the first one
		// -------------------------------------------------------------------------
		if (PublicData.storedData.groupActivities)
				PublicData.storedData.groupListCurrent = 0;
		// -------------------------------------------------------------------------	
		// 05/01/2014 ECU set patient details
		// 03/02/2014 ECU change the logic so that changes can be detected
		// 23/02/2014 ECU added the check on length == 1
		// -------------------------------------------------------------------------
		PatientDetails patientDetails = new PatientDetails ();
		
		String localName = sharedPreferences.getString (KEY_PATIENT_NAME, "");
		// -------------------------------------------------------------------------
		// 09/11/2016 ECU check if a patients name has been entered
		// -------------------------------------------------------------------------
		if (Utilities.emptyString(localName))
		{
			String [] localNames = localName.split("[ ]");
			
			if (localNames.length == 1)
			{
				patientDetails.foreName		= localNames [0];
				patientDetails.middleName 	= "";
				patientDetails.surname 		= "";
			}
			else
			if (localNames.length == 2)
			{
				patientDetails.foreName		= localNames [0];
				patientDetails.middleName 	= "";
				patientDetails.surname 		= localNames [1];
			}
			else
			{
				patientDetails.foreName	= localNames [0];
				
				String localMiddleName = "";
				
				for (int theIndex = 1; theIndex < localNames.length - 1; theIndex++)
					localMiddleName += localNames [theIndex] + " ";
				// ----------------------------------------------------------------
				// 06/01/2014 ECU added the trim to removing leading/trailing spaces
				// -----------------------------------------------------------------
				patientDetails.middleName 	= localMiddleName.trim();
				patientDetails.surname 		= localNames [localNames.length - 1];
			}
		}
		// ------------------------------------------------------------------------
		patientDetails.preferredName 	= sharedPreferences.getString (KEY_PATIENT_PREFERRED_NAME, "");
		patientDetails.dateOfBirth 		= sharedPreferences.getString (KEY_PATIENT_DOB, "");
		patientDetails.address     		= sharedPreferences.getString (KEY_PATIENT_ADDRESS, "");
		patientDetails.phoneNumber 		= sharedPreferences.getString (KEY_PATIENT_PHONE_NUMBER, "");
		patientDetails.referenceNumber	= sharedPreferences.getString (KEY_PATIENT_NHS_NUMBER, "");
		// -------------------------------------------------------------------------
		// 03/02/2014 ECU want to check if any of the patient fields has altered
		// 21/02/2014 ECU add in the 'null' check
		// -------------------------------------------------------------------------
		if (PublicData.patientDetails == null || !PublicData.patientDetails.CheckForChanges(patientDetails))
		{
			// ---------------------------------------------------------------------
			// 03/02/2014 ECU something within the patients record has changed
			// ---------------------------------------------------------------------
			PublicData.patientDetails = patientDetails;
			// ---------------------------------------------------------------------
			// 04/01/2014 ECU write the object to disk
			// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
			// ---------------------------------------------------------------------
			AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + getBaseContext().getString (R.string.patient_details_file),
												PublicData.patientDetails);
		}				
		// -------------------------------------------------------------------------
		// 05/01/2014 ECU set up email details
		// 03/02/2014 ECU change the logic to pick up changes. Copy existing record first
		//                because not all fields are changed by settings
		// 06/04/2014 ECU put in the check for null
		// -------------------------------------------------------------------------			
		if (PublicData.emailDetails != null)
		{
			EmailDetails emailDetails = new EmailDetails ();
			
			emailDetails = PublicData.emailDetails.ReturnEmailDetails();
			
			emailDetails.enabled 	= sharedPreferences.getBoolean (KEY_EMAIL_FLAG, false);
			emailDetails.signature  = sharedPreferences.getString  (KEY_EMAIL_SIGNATURE, "");
			emailDetails.recipients = sharedPreferences.getString  (KEY_EMAIL_RECIPIENTS, "");
			// ---------------------------------------------------------------------
			// 03/02/2014 ECU check for any changes
			// ---------------------------------------------------------------------
			if (!PublicData.emailDetails.CheckForChanges(emailDetails))
			{	
				// -----------------------------------------------------------------
				// 03/02/2014 ECU a change has occurred so update the record and 
				//				    write to disk
				// -----------------------------------------------------------------
				PublicData.emailDetails = emailDetails;
				// -----------------------------------------------------------------
				// 04/01/2014 ECU write the object to disk
				// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
				// -----------------------------------------------------------------
				AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + 
													getBaseContext().getString (R.string.email_details_file),
													PublicData.emailDetails);
				// -----------------------------------------------------------------		
			}
		}
		// -------------------------------------------------------------------------
		// 06/11/2015 ECU set the default developer name
		// -------------------------------------------------------------------------
		PublicData.storedData.developerName = sharedPreferences.getString (KEY_DEFAULT_DEVELOPER_NAME,StaticData.DEFAULT_DEVELOPER_NAME);
		// -------------------------------------------------------------------------
		// 17/02/2014 ECU get ambient light variables
		// 21/11/2014 ECU changed to reflect use of seek bar
		// 02/03/2015 ECU added _COUNTER
		// 06/03/2015 ECU changed _COUNTER to _PERIOD
		// -------------------------------------------------------------------------
		PublicData.storedData.ambient_light_period   
				= sharedPreferences.getInt (KEY_AMBIENT_LIGHT_PERIOD, 0);
		PublicData.storedData.ambient_light_rearm   
				= sharedPreferences.getInt (KEY_AMBIENT_LIGHT_REARM, 0);
		PublicData.storedData.ambient_light_trigger 
				= sharedPreferences.getInt(KEY_AMBIENT_LIGHT_TRIGGER, 0);
		// -------------------------------------------------------------------------
		// 07/03/2015 ECU store the actions associated with 'darkness'
		// -------------------------------------------------------------------------
		PublicData.storedData.darknessOffActions  = sharedPreferences.getString  (KEY_DARKNESS_OFF_ACTIONS, "");
		PublicData.storedData.darknessOnActions   = sharedPreferences.getString  (KEY_DARKNESS_ON_ACTIONS, "");
		// -------------------------------------------------------------------------
		// 14/03/2015 ECU set the battery variables
		// -------------------------------------------------------------------------
		// 14/03/2015 ECU make sure that the battery object exists - could be
		//                null after changes in 'storedData' during program
		//                development - won't happen whilst running
		// -------------------------------------------------------------------------
		if (PublicData.storedData.battery == null)
			PublicData.storedData.battery = new SensorData ();
		// -------------------------------------------------------------------------
		// 12/12/2016 ECU the XML settings have been changed from '0 to 100' to
		//                '1 to 99'
		// -------------------------------------------------------------------------
		PublicData.storedData.battery.lowerTrigger  = sharedPreferences.getInt (KEY_BATTERY_LOWER_TRIGGER,1);
		PublicData.storedData.battery.upperTrigger  = sharedPreferences.getInt (KEY_BATTERY_UPPER_TRIGGER,99);
		// -------------------------------------------------------------------------
		// 14/03/2015 ECU store the actions associated with 'the triggers
		// -------------------------------------------------------------------------
		PublicData.storedData.battery.lowerTriggerActions 
				= sharedPreferences.getString  (KEY_BATTERY_LOWER_TRIGGER_ACTIONS, "");
		PublicData.storedData.battery.upperTriggerActions 
				= sharedPreferences.getString  (KEY_BATTERY_UPPER_TRIGGER_ACTIONS, "");
		// -------------------------------------------------------------------------
		// 25/02/2014 ECU set the 'project log' flag
		// -------------------------------------------------------------------------
		PublicData.storedData.projectLogEnabled 
				= sharedPreferences.getBoolean (KEY_PROJECT_LOG_ENABLE, false);
		// -------------------------------------------------------------------------
		// 15/07/2015 set the 'clear project log' flag
		// -------------------------------------------------------------------------
		PublicData.storedData.clearProjectLog 
				= sharedPreferences.getBoolean (KEY_PROJECT_LOG_CLEAR, true);
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU get scheduler bits
		// -------------------------------------------------------------------------
		// 06/03/2014 ECU remember the existing time for the daily schedule
		// -------------------------------------------------------------------------
		int theHour 	= PublicData.storedData.schedulerHour;
		int theMinute 	= PublicData.storedData.schedulerMinute;
		// -------------------------------------------------------------------------
		// 07/03/2015 ECU get the correct time for the daily scheduler
		// -------------------------------------------------------------------------
		DateTime dateTime = DateTime.returnTime (sharedPreferences.getString (KEY_SCHEDULER_TIME, "00:00"));
		// -------------------------------------------------------------------------
		// 07/03/2015 ECU now copy the components across
		// -------------------------------------------------------------------------
		PublicData.storedData.schedulerHour  	= dateTime.hour;
		PublicData.storedData.schedulerMinute 	= dateTime.minute;	
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU make sure the daily scheduler is reconfigured 
		// 06/03/2014 ECU added the check that only reinitialise the schedule
		//                if the time has changed
		// -------------------------------------------------------------------------	
		if ((PublicData.storedData.schedulerHour != theHour) || 
				(PublicData.storedData.schedulerMinute != theMinute))
		{		
			// ---------------------------------------------------------------------
			// 06/03/2014 ECU re-initialise the schedule to the new time
			// ---------------------------------------------------------------------
			DailyScheduler.Initialise (getBaseContext(),
									   PublicData.storedData.schedulerHour, 
									   PublicData.storedData.schedulerMinute);		
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 19/04/2014 ECU add the remote controller bits
		// -------------------------------------------------------------------------
		PublicData.storedData.remoteAlways 
				= sharedPreferences.getBoolean (KEY_REMOTE_ALWAYS, false);
		PublicData.storedData.remoteMACAddress  = sharedPreferences.getString (KEY_REMOTE_MAC_ADDRESS, "");
		// -------------------------------------------------------------------------
		// 28/10/2014 ECU set the announcement flag
		// -------------------------------------------------------------------------
		PublicData.storedData.announceFlag = sharedPreferences.getBoolean(KEY_ANNOUNCE_FLAG, false);
		// -------------------------------------------------------------------------
		//18/11/2014 ECU set the monitor enablement flag
		// -------------------------------------------------------------------------
		PublicData.storedData.monitor.enabled = sharedPreferences.getBoolean(KEY_MONITOR_ENABLE, false);
		PublicData.storedData.monitor.triggerLevel   
				= sharedPreferences.getInt(KEY_MONITOR_TRIGGER,10000);
		// -------------------------------------------------------------------------
		// 19/11/2014 ECU set the start and stop time
		// -------------------------------------------------------------------------
		PublicData.storedData.monitor.setStartTime (sharedPreferences.getString (KEY_MONITOR_START_TIME, "08:00"));
		PublicData.storedData.monitor.setStopTime  (sharedPreferences.getString (KEY_MONITOR_STOP_TIME , "10:00"));
		// -------------------------------------------------------------------------
		// 21/11/2014 ECU set the inactive period after a noise heard
		// -------------------------------------------------------------------------
		PublicData.storedData.monitor.inactivePeriod 
				= sharedPreferences.getInt(KEY_MONITOR_INACTIVE_PERIOD,10);
		// -------------------------------------------------------------------------
		// 24/11/2014 ECU set up the options associated with timed recordings
		// -------------------------------------------------------------------------
		PublicData.storedData.monitor.timedRecording 
				= sharedPreferences.getBoolean (KEY_MONITOR_TIMED, false);
		PublicData.storedData.monitor.duration 
				= sharedPreferences.getInt (KEY_MONITOR_DURATION,2);
		// -------------------------------------------------------------------------
		// 11/09/2015 ECU set up the opacity for 'slide in' drawables - used
		//                in DisplayADrawableACtivity
		// -------------------------------------------------------------------------
		PublicData.storedData.drawableOpacity   
				= sharedPreferences.getInt (KEY_DRAWABLE_OPACITY,255);
		// -------------------------------------------------------------------------
		// 07/01/2016 ECU get the clone file size
		// -------------------------------------------------------------------------
		PublicData.storedData.cloneFileSize   
				= sharedPreferences.getInt (KEY_CLONE_FILE_SIZE,StaticData.CLONE_FILE_SIZE);
		// -------------------------------------------------------------------------
		// 25/01/2016 ECU set the click counter
		// -------------------------------------------------------------------------
		PublicData.storedData.clickCounter  
				= sharedPreferences.getInt (KEY_CLICK_COUNTER,1);
		// -------------------------------------------------------------------------
		// 27/03/2016 ECU reset the restart on alarm flag
		// -------------------------------------------------------------------------
		PublicData.storedData.alarmsRestartApp 
				= sharedPreferences.getBoolean (KEY_ALARMS_RESTART_APP, false);
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU store the flag that indicates if an activity is to be
		//                started automatically when the user interface is
		//                started
		// -------------------------------------------------------------------------
		PublicData.storedData.activityOnStart 
				= sharedPreferences.getBoolean (KEY_START_ACTIVITY_AUTOMATICALLY, false);
		// -------------------------------------------------------------------------
		// 27/04/2015 ECU added the check for monitor email
		// -------------------------------------------------------------------------
		PublicData.storedData.monitor.email = sharedPreferences.getBoolean(KEY_MONITOR_EMAIL, false);
		// -------------------------------------------------------------------------
		// 30/12/2014 ECU set the acquire statistics flag
		// -------------------------------------------------------------------------
		PublicData.storedData.acquireStatistics = sharedPreferences.getBoolean(KEY_ACQUIRE_STATISTICS, false);
		// -------------------------------------------------------------------------
		// 31/12/2014 ECU set up the 'remote access' dynamic IP address
		// -------------------------------------------------------------------------
		PublicData.storedData.dynamicIPAddress  
				= sharedPreferences.getString (KEY_DYNAMIC_IP_ADDRESS, PublicData.ipAddress); 
		// -------------------------------------------------------------------------
		// 10/12/2015 ECU set up parameters for panic alarm shake facility
		//            ECU the 'periods' are stored in the object in nano
		//                seconds and the threshold is stored as 'float'
		// -------------------------------------------------------------------------
		PublicData.storedData.panicAlarm.shakeIgnorePeriod 
				= sharedPreferences.getInt (KEY_PANIC_ALARM_SHAKE_IGNORE_PERIOD,(int)(StaticData.PANIC_ALARM_IGNORE_PERIOD/StaticData.NANOSECONDS_PER_MILLISECOND)) 
												* StaticData.NANOSECONDS_PER_MILLISECOND; 
		PublicData.storedData.panicAlarm.shakeNumber 
				= sharedPreferences.getInt (KEY_PANIC_ALARM_SHAKE_NUMBER, StaticData.PANIC_ALARM_SHAKE_NUMBER); 
		PublicData.storedData.panicAlarm.shakeResetPeriod 
				= sharedPreferences.getInt (KEY_PANIC_ALARM_SHAKE_RESET_PERIOD,(int)(StaticData.PANIC_ALARM_RESET_PERIOD/StaticData.NANOSECONDS_PER_SECOND))
												* StaticData.NANOSECONDS_PER_SECOND;
		PublicData.storedData.panicAlarm.shakeThreshold 
				= (float) (sharedPreferences.getInt (KEY_PANIC_ALARM_SHAKE_THRESHOLD,(int) (StaticData.PANIC_ALARM_SHAKE_THRESHOLD/StaticData.PANIC_ALARM_SCALE)))
												/ StaticData.PANIC_ALARM_SCALE; 
		// -------------------------------------------------------------------------
		// 01/01/2016 ECU set up parameters associated with the carer visit
		// -------------------------------------------------------------------------
		PublicData.storedData.visit_end_minutes
				= sharedPreferences.getInt (KEY_CARER_VISIT_END, StaticData.VISIT_END_MINUTES); 
		PublicData.storedData.visit_end_milliseconds 
				= ((long) StaticData.MILLISECONDS_PER_MINUTE * (long) PublicData.storedData.visit_end_minutes);
		// -------------------------------------------------------------------------
		// 02/01/2016 ECU add the actions associated with carer visits
		// -------------------------------------------------------------------------
		PublicData.storedData.visit_end_actions 
				= sharedPreferences.getString  (KEY_VISIT_END_ACTIONS,"");
		PublicData.storedData.visit_start_actions 
				= sharedPreferences.getString  (KEY_VISIT_START_ACTIONS,"");
		// -------------------------------------------------------------------------
		// 08/12/2016 ECU add the actions associated with warnings of carer visits
		// -------------------------------------------------------------------------
		PublicData.storedData.visit_end_warning_actions 
				= sharedPreferences.getString  (KEY_VISIT_END_WARNING_ACTIONS,"");
		PublicData.storedData.visit_start_warning_actions 
				= sharedPreferences.getString  (KEY_VISIT_START_WARNING_ACTIONS,"");
		// -------------------------------------------------------------------------
		// 18/02/2016 ECU get the sensor settings
		// -------------------------------------------------------------------------
		PublicData.storedData.accelerometerSampleRate 
				= sharedPreferences.getInt (KEY_ACCELEROMETER_SAMPLE_RATE,0);
		// -------------------------------------------------------------------------
		// 22/03/2016 ECU set the actions associated with the proximity sensor
		// -------------------------------------------------------------------------
		PublicData.storedData.proximityFarActionCommands 
				= sharedPreferences.getString  (KEY_PROXIMITY_FAR_ACTIONS,"");
		PublicData.storedData.proximityNearActionCommands 
				= sharedPreferences.getString  (KEY_PROXIMITY_NEAR_ACTIONS,"");
		// -------------------------------------------------------------------------
		// 09/07/2016 ECU set up the MetaBroadcast parameters
		// 26/07/2016 ECU only do if META.. is enabled
		// -------------------------------------------------------------------------
		if (TVChannelsActivity.METABROADCAST)
		{
			PublicData.storedData.metaBroadcast.apiKey  
					= sharedPreferences.getString (KEY_METABROADCAST_API_KEY,getString (R.string.api_key)); 
			PublicData.storedData.metaBroadcast.channelGroupsURL  
					= sharedPreferences.getString (KEY_METABROADCAST_CHANNEL_GROUPS_URL,getString (R.string.channel_groups_url)); 
			PublicData.storedData.metaBroadcast.channelURL  
					= sharedPreferences.getString (KEY_METABROADCAST_CHANNEL_URL,getString (R.string.channel_url));
		}
		// -------------------------------------------------------------------------
		// 22/07/2016 ECU set up the Schedules Direct parameters
		// -------------------------------------------------------------------------
		if (TVChannelsActivity.SCHEDULES_DIRECT)
		{
			PublicData.storedData.schedulesDirectData.userName
					= sharedPreferences.getString (KEY_SCHEDULES_DIRECT_USERNAME,getString (R.string.schedules_direct_username_default));
			// ---------------------------------------------------------------------
			// 22/07/2016 ECU the password cannot be stored directly because the
			//                encrypted version needs to be generated
			// ---------------------------------------------------------------------
			PublicData.storedData.schedulesDirectData.setExtras (sharedPreferences.getString (KEY_SCHEDULES_DIRECT_PASSWORD,getString (R.string.schedules_direct_password_default)));
			PublicData.storedData.schedulesDirectData.numberOfDays 
					= sharedPreferences.getInt (KEY_SCHEDULES_DIRECT_DAYS,1);
			PublicData.storedData.schedulesDirectData.baseURL
					= sharedPreferences.getString (KEY_SCHEDULES_DIRECT_URL,getString (R.string.schedules_direct_base_url));
			PublicData.storedData.schedulesDirectData.lineUp
					= sharedPreferences.getString (KEY_SCHEDULES_DIRECT_LINEUP,getString (R.string.schedules_direct_lineup_default));
		}
		// -------------------------------------------------------------------------
		// 10/01/2015 ECU tell the user if debugMode has been enabled
		// 10/03/2015 ECU changed to use 'debugMode in 'storedData'
		// 15/09/2015 ECU changed to use resource string rather than literal
		// -------------------------------------------------------------------------
		if (PublicData.storedData.debugMode)
		{
			Utilities.popToast (this.getString (R.string.debug_mode_enabled));
		}
		// -------------------------------------------------------------------------
		// 21/02/2015 ECU set the WeMo handling flag
		// 24/02/2015 ECU check if the value has changed
		// -------------------------------------------------------------------------
		boolean currentWeMoHandling = PublicData.storedData.wemoHandling;
		PublicData.storedData.wemoHandling
					= sharedPreferences.getBoolean (KEY_WEMO_HANDLING, false);
		// -------------------------------------------------------------------------
		// 24/02/2015 ECU take the appropriate WeMo action if the value has
		//                been changed
		// -------------------------------------------------------------------------
		if (PublicData.storedData.wemoHandling != currentWeMoHandling)
		{
			// ---------------------------------------------------------------------
			// 24/02/2015 ECU the state of the WeMo handling has changed
			// ---------------------------------------------------------------------
			if (PublicData.storedData.wemoHandling)
			{
				// -----------------------------------------------------------------
				// 24/02/2015 ECU WeMo handling has been enabled so if the service
				//                is not running then start it
				// -----------------------------------------------------------------
				if (!WeMoActivity.serviceRunning)
					startService (new Intent (this,WeMoService.class));
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 24/02/2015 ECU WeMo handling has been switched off so if the
				//                service is running then stop it
				// -----------------------------------------------------------------
				if (WeMoActivity.serviceRunning)
					stopService (new Intent (this,WeMoService.class));
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 27/05/2013 ECU get the main method processed	
		// -------------------------------------------------------------------------
		super.onDestroy(); 
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) 
	{ 
		return false; 
	} 
	/* ============================================================================= */
	private static Preference.OnPreferenceChangeListener UpdateAmbientLightTrigger
						= new Preference.OnPreferenceChangeListener() 
	{
		// -------------------------------------------------------------------------
		// 04/05/2016 ECU create to monitor the value of the ambient sensor and
		//                to tell the user when the value '0' is reached which
		//                in effect disables the monitoring
		// -------------------------------------------------------------------------
		@Override
		public boolean onPreferenceChange (Preference preference, Object value) 
		{	
			// ---------------------------------------------------------------------
			// 04/05/2016 ECU update the summary for this preference
			// ---------------------------------------------------------------------
			preference.setSummary (value.toString());
			// ---------------------------------------------------------------------
			// 04/05/2016 ECU if the 'ambient_light_trigger' is '0' then tell the user
			//                that effectively monitoring is disabled.
			// ---------------------------------------------------------------------
			if ((Integer) value == 0)
				Utilities.popToastAndSpeak (preference.getContext().getString(R.string.ambient_light_monitor_disabled),true);
			// ---------------------------------------------------------------------
			return true;
		}
	};
	/* ============================================================================= */
	private static Preference.OnPreferenceChangeListener listPreferenceChangeListener
										= new Preference.OnPreferenceChangeListener() 
	{
		@Override
		public boolean onPreferenceChange (Preference preference, Object value) 
		{
			// ---------------------------------------------------------------------
			if (preference.getKey().equalsIgnoreCase (KEY_CONTACTS_PACKAGE_NAME))
				PublicData.storedData.contactsPackageName = (String) value;
			else
			if (preference.getKey().equalsIgnoreCase (KEY_MAIL_PACKAGE_NAME))
				PublicData.storedData.mailPackageName = (String) value;	
			// ---------------------------------------------------------------------
			return true;
		}
	};
	// =============================================================================
	private static Preference.OnPreferenceChangeListener UpdateGridDisplay = new Preference.OnPreferenceChangeListener() 
	{
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) 
		{
			// ---------------------------------------------------------------------
			// 11/10/2014 ECU try and pick up a change in user view
			// ---------------------------------------------------------------------
			if (preference.getKey().equalsIgnoreCase (KEY_USER_VIEW))
			{
				PublicData.gridType = (Boolean) value;
				return true;
			}
			// ---------------------------------------------------------------------
			// 21/10/2015 ECU try and pick up a change in WeMo handling
			// ---------------------------------------------------------------------
			if (preference.getKey().equalsIgnoreCase (KEY_WEMO_HANDLING))
			{
				if ((Boolean) value)
				{
					// -------------------------------------------------------------
					// 21/10/2015 ECU check if WeMo handling is allowed on this device
					// -------------------------------------------------------------
					if (!WeMoActivity.validation (0))
					{
						// ---------------------------------------------------------
						// 21/10/2015 ECU this device does not support WeMo handling so
						//                force the handling off
						// ---------------------------------------------------------
						PublicData.storedData.wemoHandling = false;
						// ---------------------------------------------------------
						// 21/10/2015 ECU tell the user what is going on
						// ---------------------------------------------------------
						Utilities.popToastAndSpeak (preference.getContext().getString (R.string.wemo_cannot_support));
						// ---------------------------------------------------------
						return false;
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
				return true;
			}
			// ---------------------------------------------------------------------
			// 16/01/2015 ECU try and pick up a change in development mode
			// 10/03/2015 ECU changed to use 'developmentMode' in 'storedData'
			// ---------------------------------------------------------------------
			if (preference.getKey().equalsIgnoreCase (KEY_DEVELOPMENT_FLAG))
			{
				if ((Boolean) value != PublicData.storedData.developmentMode)
				{
					// -------------------------------------------------------------
					// 16/01/2015 ECU set the main copy of the developmentMode
					// -------------------------------------------------------------
					PublicData.storedData.developmentMode = (Boolean) value;
					// -------------------------------------------------------------
					// 15/09/2015 ECU changed to use the resource string
					// -------------------------------------------------------------
					Utilities.popToast (preference.getContext().getString (R.string.development_mode_has_been) +
										(PublicData.storedData.developmentMode ? " enabled" : " disabled"));
					// -------------------------------------------------------------
					// 16/01/2015 ECU indicate that the user view is to be rebuilt
					// -------------------------------------------------------------
					PublicData.gridRebuild = true;
					// -------------------------------------------------------------
				}
				return true;
			}
			// ---------------------------------------------------------------------
			// 22/05/2015 ECU added the sorting by usage and legend
			// ---------------------------------------------------------------------
			if (preference.getKey().equalsIgnoreCase (KEY_SORT_BY_LEGEND))
			{
				if ((Boolean) value != PublicData.storedData.sortByLegend)
				{
					// -------------------------------------------------------------
					// 22/05/2015 ECU store the value in appropriate place
					// -------------------------------------------------------------
					PublicData.storedData.sortByLegend = (Boolean) value;
					// -------------------------------------------------------------
					// 22/05/2015 ECU if this is set on then deselect the usage
					//                option
					// -------------------------------------------------------------
					if (PublicData.storedData.sortByLegend)
					{
						sortByUsageCheckBox.setChecked (false);
						PublicData.storedData.sortByUsage = false;
					}
					// -------------------------------------------------------------
					// 22/05/2015 ECU indicate that the user view is to be rebuilt
					// -------------------------------------------------------------
					PublicData.gridRebuild = true;
					// -------------------------------------------------------------
				}
				return true;
			}
			// ---------------------------------------------------------------------
			if (preference.getKey().equalsIgnoreCase (KEY_SORT_BY_USAGE))
			{
				if ((Boolean) value != PublicData.storedData.sortByUsage)
				{
					// -------------------------------------------------------------
					// 22/05/2015 ECU store the value in appropriate place
					// -------------------------------------------------------------
					PublicData.storedData.sortByUsage = (Boolean) value;
					// -------------------------------------------------------------
					// 22/05/2015 ECU if this is set on then deselect the legend
					//                option
					// -------------------------------------------------------------
					if (PublicData.storedData.sortByUsage)
					{
						sortByLegendCheckBox.setChecked (false);
						PublicData.storedData.sortByLegend = false;
					}
					// -------------------------------------------------------------
					// 22/05/2015 ECU indicate that the user view is to be rebuilt
					// -------------------------------------------------------------
					PublicData.gridRebuild = true;
					// -------------------------------------------------------------
				}
				return true;
			}
			// ---------------------------------------------------------------------
			return true;
		}
	};
	/* ============================================================================= */
	private static Preference.OnPreferenceChangeListener UpdateSummary = new Preference.OnPreferenceChangeListener() 
	{
		@Override
		public boolean onPreferenceChange (Preference preference, Object value) 
		{	
			String stringValue = value.toString();	
			// ---------------------------------------------------------------------
			preference.setSummary (stringValue);
			// ---------------------------------------------------------------------
			return true;
		}
	};
	/* ============================================================================= */
	private static Preference.OnPreferenceChangeListener UpdateMACAddress = new Preference.OnPreferenceChangeListener() 
	{
		@Override
		public boolean onPreferenceChange (Preference preference, Object value) 
		{	
			String stringValue = value.toString();	
			// ---------------------------------------------------------------------
			if (Utilities.validateMACAddress (stringValue))
			{
				preference.setSummary (stringValue);
				return true;
			}
			else
			{
				// -----------------------------------------------------------------
				// 15/09/2015 ECU changed to use the resource string
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (preference.getContext().getString (R.string.mac_address_wrong_format));
				return false;
			}
		}
	};
	// =============================================================================
	private static Preference.OnPreferenceChangeListener UpdateStartActivityAutomatically = new Preference.OnPreferenceChangeListener() 
	{
		@Override
		public boolean onPreferenceChange (Preference preference, Object value) 
		{	
			boolean booleanValue = (Boolean) value;	
			// ---------------------------------------------------------------------
			if (booleanValue)
			{
				// -----------------------------------------------------------------
				// 02/04/2016 ECU ask the user to select the activity
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (preference.getContext().getString (R.string.select_activity));
				// -----------------------------------------------------------------
				// 28/11/2014 ECU ask the GridActivity to return the selected intent
				// -----------------------------------------------------------------
				Intent intent = new Intent ((Context)activity,GridActivity.class);
				intent.putExtra (StaticData.PARAMETER_INTENT,false);
				activity.startActivityForResult (intent,StaticData.RESULT_CODE_INTENT);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			return true;
		}
	};
	// =============================================================================
	@Override 
	protected void onResume() 
	{ 
		// ------------------------------------------------------------------------
		// 21/11/2014 ECU added
		// ------------------------------------------------------------------------
		super.onResume(); 
	} 
	/* ============================================================================= */
	private static void bindPreferenceSummaryToValue (Preference preference) 
	{
		// -------------------------------------------------------------------------
		// Set the listener to watch for value changes.
		// -------------------------------------------------------------------------
		preference.setOnPreferenceChangeListener (UpdateSummary);
		// -------------------------------------------------------------------------
		// Trigger the listener immediately with the preference's
		// current value.
		// -------------------------------------------------------------------------
		UpdateSummary.onPreferenceChange(preference,
										 PreferenceManager.getDefaultSharedPreferences
										 (preference.getContext()).getString(preference.getKey(),""));
	}
	/* ============================================================================= */
	private static void bindPreferenceSummaryToValueInteger (Preference preference) 
	{
		// -------------------------------------------------------------------------
		// Set the listener to watch for value changes.
		// -------------------------------------------------------------------------
		preference.setOnPreferenceChangeListener (UpdateSummary);
		// -------------------------------------------------------------------------
		// Trigger the listener immediately with the preference's
		// current value.
		// -------------------------------------------------------------------------
		UpdateSummary.onPreferenceChange(preference,
										 PreferenceManager.getDefaultSharedPreferences
										 (preference.getContext()).getInt(preference.getKey(),0));
	}
	// =============================================================================
	void removePreference (String theCategory)
	{
		// -------------------------------------------------------------------------
		// 23/03/2016 ECU created to delete all preferences in the specified
		//                category
		// 24/03/2016 ECU changed so that the whole category (including heading)
		//                is removed
		// -------------------------------------------------------------------------
		getPreferenceScreen ().removePreference ((PreferenceCategory) findPreference (theCategory));
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	void removePreference (String theCategory,String thePreference)
	{
		// -------------------------------------------------------------------------
		// 23/03/2016 ECU created to a specified preference
		// -------------------------------------------------------------------------
		((PreferenceGroup) findPreference(theCategory)).removePreference(findPreference(thePreference));
		// -------------------------------------------------------------------------
	}
    // ============================================================================
	boolean validateBatteryTriggers ()
	{
		// -------------------------------------------------------------------------
		// 12/12/2016 ECU created to check if the input battery trigger levels are
		//                valid
		// -------------------------------------------------------------------------
		int batteryLowerTrigger  = sharedPreferences.getInt (KEY_BATTERY_LOWER_TRIGGER,1);
		int batteryUpperTrigger  = sharedPreferences.getInt (KEY_BATTERY_UPPER_TRIGGER,99);
		// -------------------------------------------------------------------------
		if (batteryLowerTrigger < batteryUpperTrigger)
		{
			// ---------------------------------------------------------------------
			// 12/12/2016 ECU the trigger levels are valid
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 12/12/2016 ECU the trigger levels are wrong
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (getString (R.string.battery_trigger_warning), true);
			return false;
			// ---------------------------------------------------------------------
		}
			
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}


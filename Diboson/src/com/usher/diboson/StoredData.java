package com.usher.diboson;

import android.content.Context;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class StoredData implements Serializable
{

	// ================================================================= 
	// 07/01/2014 ECU created to contain details of appointments
	// 18/01/2015 ECU took out CellUsage
	// 15/07/2015 ECU added 'clearProjectLog'
	// 27/08/2015 ECU added 'bluetoothDiscovery'
	// 23/10/2015 ECU ambientLightTriggered moved here from PublicData
	// ================================================================= 
	private static final long serialVersionUID = 1L;
	// ================================================================= 
	// 25/10/2015 ECU the 'initialised' flag is used to indicate whether
	//                the data has been successfully read from disk. In
	//                some ways the 'fileName' could be used in that it
	//                will be 'null' until the data is read from disk
	//                but this flag seems cleaner.
	// 14/01/2016 ECU removed the 'fileName' which was only added to
	//                be able to access the name of the file to which
	//                the data will be written without the need for a
	//                context to get the string from the resources
	// 27/01/2016 ECU added the package names for mail and contacts
	// 18/02/2016 ECU added the accelerometer sample rate
	// 26/02/2016 ECU added the tracking details (used by the LocationActivity)
	// 27/03/2016 ECU added alarmsRestartApp to indicate that should an
	//                alarm occur when the app is not running then
	//				  the app will be started
	// 18/05/2016 ECU added 'liquids'
	//            ECU added activityOnStart and ...Number
	// 15/06/2016 ECU added nfcRemoteProcessor
	// 26/06/2016 ECU added tvChannelsSorted
	// 09/07/2016 ECU added metaBroadcast
	// 21/07/2016 ECU added Schedules Direct
	// 06/10/2016 ECU added the group list
	// 18/10/2016 ECU added the documents list
	// 18/12/2016 ECU added the dexterityGameBackground
	// 17/02/2017 ECU added 'tvChannelsPositioning' which if 'true'
	//                will cause the programs to be aligned to the
	//                most recent 'scrolled to' program time
	// 16/06/2017 ECU added 'cameraSettings'
	// 17/06/2017 ECU added 'startPanicAlarm'
	// 22/06/2017 ECU added 'emailAttachmentMaxSize'
	// 19/07/2017 ECU added 'long press' legend
	// 27/08/2017 ECU added 'weight_metric'
	// 11/09/2017 ECU added 'height'
	// 04/10/2017 ECU added the actions associated with 'screen on / off'
	// 21/02/2018 ECU added 'introductionFile'
	// 10/02/2018 ECU added 'countdownTimerData'
	// 21/06/2018 ECU added 'randomEventActions' and 'randomEventPeriod'
	//            ECU changed to use RandomEvent class
	// 05/07/2018 ECU added 'positiveFeedback'
	// 07/08/2018 ECU added 'autoScroll'
	// 19/11/2018 ECU added 'watchdogTimer'
	// 23/11/2018 ECU added 'ntpEnabled' and 'ntpServer'
	// 05/01/2019 ECU added 'usageDisplay'
	// 25/01/2019 ECU added 'taskImageClick' and 'taskImageLongClick'
	// 10/04/2019 ECU added 'alexaDeviceIPAddress'
	// 25/06/2019 ECU added the dexterityGameBackgroundBlur
	// 18/07/2019 ECU publicIpAddress moved here from PublicData
	// 08/08/2019 ECU added 'mosquito' bits
	// 09/09/2019 ECU added 'brightness from camera' bit
	// 10/09/2019 ECU added 'navigationBar'
	// 06/10/2019 ECU added 'smart_devices....'
	// 17/11/2019 ECU added the 'logcatFilter'
	// 05/02/0202 ECU added 'wifiStateChange'
	// 13/02/2020 ECU added 'alexaWakeWord'
	// 14/03/2020 ECU added 'arithmeticData'
	// 17/04/2020 ECU added 'storedBluetoothDevices'
	// 07/05/2020 ECU added 'announcePhoneNumber'
	// 21/05/2020 ECU added 'backKeyDisplay'
	// 23/06/2020 ECU added the co-ords for the back key
	//						 'backKeyDisplayX' & 'backkeyDisplayY'
	// 17/07/2020 ECU added 'notifcationOrder'
	// 18/07/2020 ECU added 'spokenPhraseTimeout'
	// 22/07/2020 ECU added 'bluetoothTracking...'
	// 29/08/2020 ECU added 'toastDuration'
	// 19/09/2020 ECU added 'restartActivity'
	// 12/10/2020 ECU added 'locationActions'
	// 18/10/2020 ECU added 'locationManagerMin....'
	// -----------------------------------------------------------------
	public boolean  initialised				= false;
	// =================================================================
	public int		accelerometerSampleRate	= 0;		// 18/02/2016 ECU the rate at which the accelerometer
														//                provides its data
	public boolean	acquireStatistics		= false;	// 30/12/2014 ECU indicate if statistics are 
														//                to be saved in project log
	public boolean  activityOnStart			= false;	// 18/05/2016 ECU indicate to start an activity when
														//                GridActivity starts
	public String	activityOnStartLegend;				// 18/05/2016 ECU activity to start if the activityOnStart is
														//                true
														// 23/09/2017 ECU changed from 'Number' which was an 'int'
	public boolean  alarmsRestartApp		= false;		// 27/03/2016 ECU added
	public String	alexaDeviceIPAddress	= "";		// 10/04/2019 ECU added
	public String	alexaWakeWord			= null;		// 13/02/2020 ECU added
	public int		ambient_light_period	= 10;		// 02/03/2015 ECU the number of consequetive
	                                                    //                readings below ambient_light_trigger
														//                which deems that it is 'getting
														//                dark'
														// 06/03/2015 ECU changed to be a period in seconds
														//                because some devices only give a
														//                reading when the level changes
	public int		ambient_light_rearm		= 200;		// 17/02/2014 ECU when trigger rearmed
	public int		ambient_light_trigger	= 50;		// 17/02/2014 ECU when light warning appears
	public boolean	ambientLightTriggered 	= false;	// 12/09/2013 ECU added - indicates that the ambient light warning
														//                has been displayed
	public boolean  announceFlag			= false;	// 28/10/2014 ECU announce the device
	public String	announcePhoneNumber		= null;		// 07/05/2020 ECU number of phone that receives the 'announce'
														//                SMS message
	public ArithmeticData
					arithmeticData			= null;		// 14/03/2020 ECU arithmetic details
	public boolean  autoScroll				= false;	// 07/08/2018 ECU auto scroll project information
	public boolean  backKeyDisplay			= false;	// 21/05/2020 ECU if back key icon is to be displayed
	public int		backKeyDisplayX			= 0;		// 23/05/2020 ECU X co-ord of the 'back key'
	public int		backKeyDisplayY			= 0;		// 23/05/2020 ECU Y co-ord of the 'back key'
	public SensorData	
					battery					= new SensorData (10,90);
														// 14/03/2015 ECU added to contain battery details
														// 23/06/2017 ECU use the constructor which sets the triggers
	public boolean  bluetoothDiscovery		= false;	// 27/08/2015 ECU added to start a bluetooth discovery
	public int      bluetoothDiscoveryGap	= StaticData.BLUETOOTH_DISCOVERY_TIME;
														// 23/07/2020 ECU added to define gap bluetooth discovery
	public boolean  bluetoothTracking		= false;    // 16/04/2020 ECU added to start bluetooth device tracking
	public int  	bluetoothTrackingContactTime
											= 10;   	// 22/07/2020 ECU added to define the period that confirms a 'contact;
	public int  	bluetoothTrackingLostContactTime
											= 10;   	// 22/07/2020 ECU added to define the period that confirms a 'lost contact;
	public boolean	brightnessFromCamera	= true;		// 09/09/2019 ECU added to use the camera as a rough estimate of brightness
	public CameraSettings
					cameraSettings			= new CameraSettings ();
														// 16/06/2017 ECU added to contain camera settings
	public boolean	clearProjectLog			= true;		// 15/07/2015 ECU added to optionally clear the project log
														//                when the app is started
	public int      clickCounter			= 1;		// 25/01/2016 ECU added - click counter used in GridActivity
	public int		cloneFileSize			= StaticData.CLONE_FILE_SIZE;
														// 07/01/2016 ECU added - default clone file size - although will
														//                be checking against the length of a file which
														//                is a long the value will fit into an int
	public String	contactsPackageName		= null;		// 27/01/2016 ECU added - where the contacts package is stored
	public CountdownTimerData
					countdownTimerData		= null;		// 10/03/2018 ECU added - data assciated with the countdown timer
	public String	darknessOffActions		= "";		// 07/03/2015 ECU actions to be taken when 'darkness' is reset
	public String	darknessOnActions		= "";		// 07/03/2015 ECU actions to be taken when 'darkness' happens
	public boolean  debugMode 				= false;	// 05/06/2013 ECU be able to switch on/off debug messages
														// 10/03/2015 ECU moved here from MainActivity
	public String   developerName			= "ECU";	// 06/11/2015 ECU added to identify the default developer
	public boolean	developmentMode 		= false;	// 19/01/2014 ECU added - true = development mode
														// 10/03/2015 ECU moved here from MainActivity
														// 28/06/2017 ECU changed from 'true'
	public String   dexterityGameBackground	= null;		// 18/12/2016 ECU path to the file that will be use for the
	                                                    //                background
	public int	    dexterityGameBackgroundBlur	
											= 0;		// 25/06/2019 ECU how much blurring is wanted on the background
														//                0 = none
	public List<Document> 
					documents 				= new ArrayList<Document>();
														// 18/10/2016 ECU added - essential documents
	public int		drawableOpacity			= 255;		// 11/09/2015 ECU declare the opacity of the 'slide in'
														//                drawables
	public String	dynamicIPAddress		= "bt2994.myfoscam.org";
														// 31/12/2014 ECU when accessed from the public
														//                network then this is the
														//                IP address to access a specified
														//                device that is running this
														//                software
	public long		emailAttachmentMaxSize	= StaticData.EMAIL_MAX_ATTACHMENT_SIZE;
														// 22/06/2017 ECU maximum size of an email attachment
	public boolean 	epgDailyCheck			= false; 	// 29/07/2020 ECU whether the programs in the EPG are checked daily
	public String	fallsActionCommands		= null;		// 24/01/2016 ECU the actions to be taken when a fall
														//                is detected
	// -----------------------------------------------------------------------------
	// 09/03/2015 ECU 'rawtypes' issue arose after major changes in GridImages and
	//                GridActivity
	// -----------------------------------------------------------------------------
	public boolean  groupActivities			= false;	// 08/10/2016 ECU whether grouping is on/off
	public int      groupListCurrent		= StaticData.NO_RESULT;
														// 06/10/2016 ECU current group list
	public List<GroupList> 
					groupLists 				= new ArrayList<GroupList>();
														// 06/10/2016 ECU added - group lists
	// -----------------------------------------------------------------------------
	public int		height					= StaticData.NOT_SET;
														// 11/09/2017 ECU added - patient's height in cms
	public String	introductionFile		= null;		// 21/02/2018 ECU added - path to introduction file
	public int      lastActivity		    = StaticData.NO_RESULT;
														// 21/10/2015 ECU added - pointer to the last activity 
														//                actioned from GridActivity
	public List<Liquid> 
					liquids 				= new ArrayList<Liquid>();
														// 18/05/2016 ECU added - stored liquid information
	public float	liquidTolerance			= StaticData.LIQUID_TOLERANCE;
														// 31/05/2016 ECU added - tolerance when detecting liquids
	public List<LocationActions>
					locationActions			= new ArrayList<LocationActions>();
														// 12/10/2020 ECU added - location actions
	public int		locationManagerMinDistance
											= 10;		// 18/10/2020 ECU added - minimum distance between updates
	public int		locationManagerMinTime	= 5;		// 18/10/2020 ECU added - minimum time between updates
	public String	logcatFilter			= null;		// 17/11/2019 ECU added - the filter to apply to the logcat display
	public boolean  longPressLegend			= false;	// 19/07/2017 ECU added - legend associated with 'long press'
	public String	mailPackageName			= null;		// 27/01/2016 ECU added - where the mail package is stored
	public boolean	marquee					= false;	// 12/04/2015 ECU added
	public MetaBroadcast metaBroadcast		= null;		// 09/07/2016 ECU added
	public Monitor	monitor					= new Monitor (false,10000);
														// 18/11/2014 ECU added for monitor service
	// -----------------------------------------------------------------------------
	// 08/08/2019 ECU put in the parameters associated with the 'mosquito' feature
	//                that can be used to annoy people with good hearing, i.e. children
	// 21/06/2020 ECU added 'gap'
	// -----------------------------------------------------------------------------
	public int		mosquitoDuration		= StaticData.NOT_SET;
	public int		mosquitoFrequency		= StaticData.NOT_SET;
	public int		mosquitoGap				= StaticData.NOT_SET;
	// -----------------------------------------------------------------------------
	// 06/10/2017 ECU declare the parameters associated with 'theft protection'
	// -----------------------------------------------------------------------------
	public MovementParameters 
					movementParameters 		= null;
	// -----------------------------------------------------------------------------
	// 03/01/2018 ECU set up the parameters used for music player timings
	// -----------------------------------------------------------------------------
	public int      musicTimerDuration		= 60;		
	public int      musicTimerHours			= 0;
	public int      musicTimerMinutes		= 30;
	public int      musicTimerSeconds		= 0;
	// -----------------------------------------------------------------------------
	public boolean	navigationBar			= false;	// 10/09/2019 ECU added indicates if the navigation bar
	                                                    //                  is to be hidden
	public String	nfcRemoteProcessor		= null;		// 15/06/2016 ECU added for receiver of NFC actions
	public boolean  notificationOrder		= false;    // 17/07/2020 ECU the order in which notifications are
	                                                    //                displayed :-
	                                                    //                   false .... order of receipt
	                                                    //                   true ..... most recent first
	public boolean	ntpEnabled				= true;		// 23/11/2018 ECU added to indicate if ntp is to be used
	public String   ntpServer				= StaticData.NTP_SERVER;
														// 23/11/2018 ECU added the NTP server
	public List<NotificationMessage>
					notificationMessages	= new ArrayList<NotificationMessage>();
														// 13/07/2016 ECU added - internal notification messages
	public PanicAlarm
					panicAlarm				= new PanicAlarm ();
														// 25/11/2015 ECU hold panic alarm details
	public boolean	positiveFeedback		= false;	// 05/07/2018 ECU if positive feedback to user actions
	public String	positiveFeedbackEffect	= null;		// 05/07/2018 ECU the required effect
	public boolean	projectLogEnabled 		= true;		// 25/02/2014 ECU indicate if logging to the
														//                project log
														// 15/10/2020 ECU changed to true
	public String	proximityFarActionCommands	
											= null;		// 22/03/2016 ECU the actions to be taken when proximity
														//                sensor indicates 'far'
	public String	proximityNearActionCommands	
											= null;		// 22/03/2016 ECU the actions to be taken when proximity
														//                sensor indicates 'near'
	public String  	publicIpAddress			= null;		// 18/07/2019 ECU moved here from PublicData
	public RandomEvent
					randomEvent				= new RandomEvent ();
														// 21/06/2018 ECU create new 'random event' object
														// 18/10/2020 ECU changed initialisation from null
	public boolean  remoteAlways			= false;	// 19/04/2014 ECU display remote whether hardware or not
	public String	remoteMACAddress		= BluetoothService.MAC_ADDRESS;
														// 19/04/2014 ECU MAC address of remote controller
	public boolean	restartActivity			= true;		// 19/09/2020 ECU whether activity is restarted
	     												//                automatically
	public int		schedulerHour			= 0;		// 04/03/2014 ECU hour when scheduler is to run
	public int		schedulerMinute			= 0;		// 04/03/2014 ECU minute when scheduler is to run
	public SchedulesDirectData
					schedulesDirectData		= null;		// 21/07/2016 ECU added
	public String	screenOffActions		= null;		// 04/10/2017 ECU added
	public String	screenOnActions			= null;		// 04/10/2017 ECU added
	// -----------------------------------------------------------------------------
	// 05/10/2019 ECU add the smart device ports
	// 06/10/2019 ECU add the actions
	// 07/10/2019 ECU add 'WeMo' actions
	// -----------------------------------------------------------------------------
	public String	smart_device_kasa_actions
											= null;
	public int		smart_device_kasa_tcp_port
											= 0;
	public int		smart_device_kasa_udp_port
											= 0;
	public String	smart_device_tuya_actions
											= null;
	public int		smart_device_tuya_tcp_port
											= 0;
	public int		smart_device_tuya_udp_port
											= 0;
	public String	smart_device_wemo_actions
											= null;
	// -----------------------------------------------------------------------------
	public boolean  sortByLegend			= true;		// 26/04/2015 ECU sort the cells by legend
														// 23/06/2017 ECU changed to 'true'
	public boolean  sortByUsage				= false;	// 10/02/2014 ECU sort the grid cells by usage
	public SpeakingClock	
					speakingClock 			= new SpeakingClock (false,0,0,0,0,10,false,false,true);	
														// 09/02/2014 ECU replaced individual
														//                variables
														// 01/02/2017 ECU added the final false
														// 10/03/2017 ECU added another 'final false' to optionally trigger
														//                the Westminster chime
														// 25/07/2017 ECU added the final 'true'
	public int	    spokenPhraseTimeout		= 0;        // 18/07/2020 ECU added
	public boolean 	startPanicAlarm			= false;	// 17/06/2017 ECU added
	public List<BluetoothTrackingData>
					storedBluetoothDevices	= null;		// 17/04/2020
	public boolean 	taskImageClick			= false;	// 25/01/2019 ECU added
	public boolean	taskImageLongClick		= false;	// 25/01/2019 ECU added
	public int      toastDuration			= 0;		// 29/08/2020 ECU added time, in seconds, that the toast is to be
	                                                    //                displayed
	public int      trackingAccuracy		= StaticData.TRACKING_ACCURACY;
														// 13/10/2020 ECU added - the accuracy when checking location
														//                changes
	public int      trackingOutOfRange		= StaticData.TRACKING_ACCURACY * 4;
														// 19/10/2020 ECU added - the accuracy when checking location
														//                changes
	public TrackingDetails
					trackingDetails	= new TrackingDetails ();
														// 26/02/2016 ECU added
	public boolean  tvChannelsPositioning	= false;	// 17/02/2017 ECU added
	public boolean	tvChannelsSorted		= false;	// 26/06/2016 ECU added
	public boolean  usageDisplay			= false;	// 05/01/2019 ECU added - show usage of activities
	public boolean	userView				= true;		// 28/01/2014 ECU false = grid , true = list
														// 23/06/2017 ECU changed to 'true'
	public String	visit_end_actions		= null;		// 02/01/2016 ECU actions at the end of a visit
	public String	visit_end_warning_actions	
											= null;		// 08/12/2016 ECU actions on the warning of the end of a visit
	public long		visit_end_milliseconds	= StaticData.VISIT_END_MILLISECONDS;
														// 01/01/2016 ECU added
	public int		visit_end_minutes		= StaticData.VISIT_END_MINUTES;
														// 01/01/2016 ECU added
	public String	visit_start_actions		= null;		// 02/01/2016 ECU actions at the start of a visit
	public String	visit_start_warning_actions	
											= null;		// 08/12/2016 ECU actions on the warning of the start of a visit
	public WatchdogTimer watchdogTimer		= new WatchdogTimer ();
														// 19/11/2018 ECU parameters associated with the watchdog timer
														// 18/10/2020 ECU changed the initialisation
	public boolean  weight_metric			= false;	// 27/08/2017 ECU indicate how the weight is to be supplied
														//                   true ...... in kilograms
														//					 false ..... in stones and pounds
	public boolean  wemoHandling			= false;	// 21/02/2015 ECU added for WeMo handling
	public List<WeMoTimer> wemoTimers 		= new ArrayList<WeMoTimer>();
														// 25/02/2015 ECU added to hold WeMo timers
	public boolean  wifiStateChange			= false;	// 05/02/2020 ECU added - whether wifi changes are reported
	/* ============================================================================= */
	
	// =============================================================================
	// 24/10/2015 ECU declare any methods that apply to this class
	// =============================================================================

	// =============================================================================
	public static boolean CheckIfInitialised ()
	{
		// -------------------------------------------------------------------------
		// 08/05/2020 ECU created to just check that the data structure has been
		//                set up and that a valid copy has been read from disk
		//			  ECU do in this order so that the second check does not
		//                produce an NPE because '..storedData' is null
		// -------------------------------------------------------------------------
		if ((PublicData.storedData != null) && PublicData.storedData.initialised)
		{
			// ---------------------------------------------------------------------
			// 08/05/2020 ECU indicate that everything is 'good to go'
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 08/05/2020 ECU indicate that the data has not been set up yet
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print (String theInitialString)
	{
		// -------------------------------------------------------------------------
		// 25/10/2015 ECU created to log details of the class variables
		// 26/10/2015 ECU changed to return a string rather than putting to LogCat
		// -------------------------------------------------------------------------
		return theInitialString +
					"\ninitialised : " + initialised +
					"\nambientLightTriggered : " + ambientLightTriggered +
					"\nambient_light_period : " + ambient_light_period;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static int ReadFromDisk (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 14/10/2020 ECU created to check if the 'stored data' file exists and if
		//                so then read the contents. The result is returned
		// ------------------------------------------------------------------------
		String fileName = PublicData.projectFolder + theContext.getString (R.string.stored_data_file);
		// ------------------------------------------------------------------------
		// 14/10/2020 ECU check if the file exists
		// ------------------------------------------------------------------------
		if ((new File(fileName)).exists())
		{
			// --------------------------------------------------------------------
			// 14/10/2020 ECU the file exists so read it's contents
			// --------------------------------------------------------------------
			Object readObject = Utilities.readObjectFromDisk (fileName);
			// --------------------------------------------------------------------
			// 14/10/2020 ECU check if the data was read correctly
			// --------------------------------------------------------------------
			if (readObject != null)
			{
				// ----------------------------------------------------------------
				// 14/10/2020 ECU the data was read correctly
				// ----------------------------------------------------------------
				PublicData.storedData = (StoredData) readObject;
				return StaticData.FILE_READ_OK;
				// ----------------------------------------------------------------
			}
			else
			{
				// ----------------------------------------------------------------
				// 14/10/2020 ECU there was a problem in reading the data
				// ----------------------------------------------------------------
				return StaticData.FILE_READ_NOT_OK;
				// ----------------------------------------------------------------
			}
			// --------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 14/10/2020 ECU the file does not exist
			// ---------------------------------------------------------------------
			return StaticData.FILE_DOES_NOT_EXIST;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void  RestoreFromBackupCopy ()
	{
		// -------------------------------------------------------------------------
		// 30/08/2020 ECU read the copy of the stored data from disk
		// -------------------------------------------------------------------------
		StoredData localStoredData =
				(StoredData) Utilities.readObjectFromDisk ((PublicData.projectFolder +
					((Context) MainActivity.activity).getString (R.string.stored_data_file)) + StaticData.BACKUP_EXTENSION);
		// -------------------------------------------------------------------------
		// 30/08/2020 ECU check if the data was read correctly
		// -------------------------------------------------------------------------
		if (localStoredData != null)
		{
			// ---------------------------------------------------------------------
			// 30/08/2020 ECU the data was read without error
			// ---------------------------------------------------------------------
			// 30/08/2020 ECU check if the stored data was a properly initialised
			//                copy of 'storedData'
			// ---------------------------------------------------------------------
			if (localStoredData.initialised)
			{
				// -----------------------------------------------------------------
				// 30/08/2020 ECU log the fact as this is important
				// -------------------------------------------------------------------------
				Utilities.LogToProjectFile (getClass().getSimpleName(),"storedData has been restored from the backup",true);
				// -----------------------------------------------------------------
				// 30/08/2020 ECU the retrieved data seems to be valid
				//            ECU copy the data across
				// -----------------------------------------------------------------
				PublicData.storedData = localStoredData;
				// -------------------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void setAmbientLightTriggered (boolean theState)
	{
		// -------------------------------------------------------------------------
		// 24/10/2015 ECU set the state of the ambient light trigger
		// -------------------------------------------------------------------------
		ambientLightTriggered = theState;
		// -------------------------------------------------------------------------
		// 24/10/2015 ECU and write the data to disk
		// -------------------------------------------------------------------------
		writeToDisk ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================

	// =============================================================================
	public void setToInitialised ()
	{
		// -------------------------------------------------------------------------
		// 25/10/2015 ECU set the file name
		// 14/01/2016 ECU changed from SetFileName because the name of the file to
		//                which this data will be written will be generated when
		//                needed
		// -------------------------------------------------------------------------
		// 30/08/2020 ECU try and restore from the 'backup' copy
		// -------------------------------------------------------------------------
		RestoreFromBackupCopy ();
		// -------------------------------------------------------------------------
		// 25/10/2015 ECU also indicate that the data has been set correctly
		// -------------------------------------------------------------------------
		initialised = true;
		// -------------------------------------------------------------------------
		// 25/10/2015 ECU and write the data to disk
		// -------------------------------------------------------------------------
		writeToDisk ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void setLastActivity (int theValue)
	{
		// -------------------------------------------------------------------------
		// 24/10/2015 ECU set the lastActivity to the value specified
		// -------------------------------------------------------------------------
		lastActivity = theValue;
		// -------------------------------------------------------------------------
		// 24/10/2015 ECU and write the data to disk
		// -------------------------------------------------------------------------
		writeToDisk ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void writeToDisk ()
	{
		// -------------------------------------------------------------------------
		// 24/10/2015 ECU write the data to disk if the file name has been set
		// -------------------------------------------------------------------------
		if (initialised)
		{
			AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + ((Context)MainActivity.activity).getString (R.string.stored_data_file),PublicData.storedData);
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

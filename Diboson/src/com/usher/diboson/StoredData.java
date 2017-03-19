package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;


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
	// -----------------------------------------------------------------
	public boolean  initialised				= false;
	// =================================================================
	public int		accelerometerSampleRate	= 0;		// 18/02/2016 ECU the rate at which the accelerometer
														//                provides its data
	public boolean	acquireStatistics		= false;	// 30/12/2014 ECU indicate if statistics are 
														//                to be saved in project log
	public boolean  activityOnStart			= false;	// 18/05/2016 ECU indicate to start an activity when
														//                GridActivity starts
	public int		activityOnStartNumber;				// 18/05/2016 ECU activity to start if the activityOnStart is
														//                true
	public boolean  alarmsRestartApp		= true;		// 27/03/2016 ECU added
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
	public SensorData	
					battery					= new SensorData ();
														// 14/03/2015 ECU added to contain battery details
	public boolean  bluetoothDiscovery		= false;	// 27/08/2015 ECU added to start a bluetooth discovery
	public boolean	clearProjectLog			= true;		// 15/07/2015 ECU added to optionally clear the project log
														//                when the app is started
	public int      clickCounter			= 1;		// 25/01/2016 ECU added - click counter used in GridActivity
	public int		cloneFileSize			= StaticData.CLONE_FILE_SIZE;
														// 07/01/2016 ECU added - default clone file size - although will
														//                be checking against the length of a file which
														//                is a long the value will fit into an int
	public String	contactsPackageName		= null;		// 27/01/2016 ECU added - where the contacts package is stored
	public String	darknessOffActions		= "";		// 07/03/2015 ECU actions to be taken when 'darkness' is reset
	public String	darknessOnActions		= "";		// 07/03/2015 ECU actions to be taken when 'darkness' happens
	public boolean  debugMode 				= false;	// 05/06/2013 ECU be able to switch on/off debug messages
														// 10/03/2015 ECU moved here from MainActivity
	public String   developerName			= "ECU";	// 06/11/2015 ECU added to identify the default developer
	public boolean	developmentMode 		= true;		// 19/01/2014 ECU added - true = development mode
														// 10/03/2015 ECU moved here from MainActivity
	public String   dexterityGameBackground	= null;		// 18/12/2016 ECU path to the file that will be use for the
	                                                    //                background
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
	public String	fallsActionCommands		= null;		// 24/01/2016 ECU the actions to be taken when a fall
														//                is detected
	// -----------------------------------------------------------------------------
	// 09/03/2015 ECU 'rawtypes' issue arose after major changes in GridImages and
	//                GridActivity
	// -----------------------------------------------------------------------------
	@SuppressWarnings ("rawtypes")
	public GridImages [] gridImages			= null;		// 18/01/2015 ECU added (see GridActivity)
	public boolean  groupActivities			= false;	// 08/10/2016 ECU whether grouping is on/off
	public int      groupListCurrent		= StaticData.NO_RESULT;
														// 06/10/2016 ECU current group list
	public List<GroupList> 
					groupLists 				= new ArrayList<GroupList>();
														// 06/10/2016 ECU added - group lists
	// -----------------------------------------------------------------------------
	public int      lastActivity		    = StaticData.NO_RESULT;
														// 21/10/2015 ECU added - pointer to the last activity 
														//                actioned from GridActivity
	public List<Liquid> 
					liquids 				= new ArrayList<Liquid>();
														// 18/05/2016 ECU added - stored liquid information
	public float	liquidTolerance			= StaticData.LIQUID_TOLERANCE;
														// 31/05/2016 ECU added - tolerance when detecting liquids
	public String	mailPackageName			= null;		// 27/01/2016 ECU added - where the mail package is stored
	public boolean	marquee					= false;	// 12/04/2015 ECU added
	public MetaBroadcast metaBroadcast		= null;		// 09/07/2016 ECU added
	public Monitor	monitor					= new Monitor (false,10000);
														// 18/11/2014 ECU added for monitor service
	public String	nfcRemoteProcessor		= null;		// 15/06/2016 ECU added for receiver of NFC actions
	public List<NotificationMessage>
					notificationMessages	= new ArrayList<NotificationMessage>();
														// 13/07/2016 ECU added - internal notification messages
	public PanicAlarm
					panicAlarm				= new PanicAlarm ();
														// 25/11/2015 ECU hold panic alarm details
	public boolean	projectLogEnabled 		= false;	// 25/02/2014 ECU indicate if logging to the
														//                project log
	public String	proximityFarActionCommands	
											= null;		// 22/03/2016 ECU the actions to be taken when proximity
														//                sensor indicates 'far'
	public String	proximityNearActionCommands	
											= null;		// 22/03/2016 ECU the actions to be taken when proximity
														//                sensor indicates 'near'
	public boolean  remoteAlways			= false;	// 19/04/2014 ECU display remote whether hardware or not
	public String	remoteMACAddress		= BlueToothService.MAC_ADDRESS;	
														// 19/04/2014 ECU MAC address of remote controller
	public int		schedulerHour			= 0;		// 04/03/2014 ECU hour when scheduler is to run
	public int		schedulerMinute			= 0;		// 04/03/2014 ECU minute when scheduler is to run
	public SchedulesDirectData
					schedulesDirectData		= null;		// 21/07/2016 ECU added
	public boolean  sortByLegend			= false;	// 26/04/2015 ECU sort the cells by legend
	public boolean  sortByUsage				= false;	// 10/02/2014 ECU sort the grid cells by usage
	public SpeakingClock	
					speakingClock 			= new SpeakingClock (false,0,0,0,0,10,false,false);	
														// 09/02/2014 ECU replaced individual
														//                variables
														// 01/02/2017 ECU added the final false
														// 10/03/2017 ECU added another 'final false'
	public TrackingDetails trackingDetails	= new TrackingDetails ();
														// 26/02/2016 ECU added
	public boolean  tvChannelsPositioning	= false;	// 17/02/2017 ECU added
	public boolean	tvChannelsSorted		= false;	// 26/06/2016 ECU added
	public boolean	userView				= false;	// 28/01/2014 ECU false = grid , true = list
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
	public boolean  wemoHandling			= false;	// 21/02/2015 ECU added for WeMo handling
	public List<WeMoTimer> wemoTimers 		= new ArrayList<WeMoTimer>();
														// 25/02/2015 ECU added to hold WeMo timers
	/* ============================================================================= */
	
	// =============================================================================
	// 24/10/2015 ECU declare any methods that apply to this class
	// =============================================================================
	
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
	public void setBatteryTriggered (boolean theState)
	{
		// -------------------------------------------------------------------------
		// 24/10/2015 ECU set the state of the battery trigger
		// -------------------------------------------------------------------------
		battery.triggered = theState;
		// -------------------------------------------------------------------------
		// 24/10/2015 ECU and write the data to disk
		// -------------------------------------------------------------------------
		writeToDisk ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void setToInitialised ()
	{
		// -------------------------------------------------------------------------
		// 25/10/2015 ECU set the file name
		// 14/01/2016 ECU changed from SetFileName because the name of the file to
		//                which this data will be written will be generated when
		//                needed
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
	}
	// =============================================================================
}

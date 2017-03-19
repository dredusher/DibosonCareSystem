package com.usher.diboson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.InputType;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
 
// ==================================================================================
// 02/06/2013 ECU ***** BE CAREFUL *****
// =====================================
// =====================================
//
//	The arrangement of the cells, in particular the Music Player in cell 14, is important
//  so be careful if editing. This message may no longer be relevant - see 14/01/2015
//
// 16/09/2013 ECU try and change the grid size when the user selects an option
// 05/12/2013 ECU add in the call to the FileChooser
// 16/12/2013 ECU add in the call to the Television activity
// 31/12/2013 ECU add the Arduino activity using bluetooth 
// 03/01/2014 ECU add in LocationActivity
// 05/01/2014 ECU add in SettingsActivity
// 06/01/2014 ECU add in AppointmentsActivity
// 18/01/2014 ECU add in AudioAnalyser
// 09/02/2014 ECU add in SpeakingClock
// 16/02/2014 ECU changed to use iconSize rather than individual imageHeight and
//                imageWidth. Also change zooming to be according to icons per row
// 17/02/2014 ECU include the ambient light monitor here
// 03/03/2014 ECU tidied up the cell usage handling
// 11/10/2014 ECU changed various aspects so that the layout of the grid changes if
//                the user makes changes in the settings
// 20/10/2014 ECU have an 'activityCounter' which can be used by activities that may
//                restart themselves
// 09/11/2014 ECU put in gesture handling so that I can try and detect left/right
//                swipe
// 10/11/2014 ECU put the swiping into the GridView to be consistent
//            ECU also provide the facility whereby the activity can be started with
//                a parameter indicating which activity is to be started
// 28/11/2014 ECU major changes so that GridActivity can either action a task or
//                just return the intent that would be actioned
// 30/11/2014 ECU there is an issue when Monitoring is running and an activity runs
//                which tries to 'get' the microphone. So want to disable the monitor,
//                if running, whilst the activity is running (onPause) and then to
//                re-enable it when the activity completes (onResume)
// 02/12/2014 ECU added the Radar activity
// 13/01/2015 ECU added the NavigationDrawer activity
// 14/01/2015 ECU rearrange the entries in 'switch' to be alphabetical. This will not
//                affect the order in which the icons are displayed which is governed
//                by the position in the 'gridImages' array
// 16/01/2015 ECU put in the checks on 'gridRebuild'
// 18/01/2015 ECU changed activeImages to be a 2d array with [0][..] being the resourceID
//                of the image and [1][..] being the index into the gridImages
//                array
// 11/02/2015 ECU added UPnP activity
// 20/02/2015 ECU added WeMo activity and its 'long press' option to the timers
// 13/04/2015 ECU added ScreenCaptureActivity
// 05/05/2015 ECU changed the GridRefreshHandler so that it switches on the message
//                type (held in message.what) and uses any parameters held in the
//                message
// 22/05/2015 ECU save a copy of the 'unsorted' images array so that when sorting
//                options are switched off then the original array can be displayed
// 11/07/2015 ECU put in a 'shift email' to enable the manual sending of an e-mail,
//                i.e. it requests recipient,subject, message ....
// 15/08/2015 ECU added the ToneGenerator activity
// 16/09/2015 ECU added the TV channels activity
// 09/10/2015 ECU don't put up starting messages if the app has not been started
//                manually
// 15/11/2015 ECU added the DatabaseActivity
// 25/11/2015 ECU added the PanicAlarmActivity
// 19/12/2015 ECU change the handling of AppointmentsActivity to use the Selector
//                class
// 25/01/2016 ECU added the long press on the speaking clock to give the current time
// 03/03/2016 ECU tidied up so that use the 'context' variable instead of calling
//                getBaseContext () - this was used when activating the various
//                activities
// 18/05/2016 ECU add the LiquidActivity
//            ECU add the code to start an activity automatically if requested
// 30/05/2016 ECU put in the validation on LiquidActivity
// 12/07/2016 ECU put in informative message for click counter
//            ECU optionally handle a notification button
// 03/08/2016 ECU added the 'named actions' activity
// 08/09/2016 ECU added the UPnP task
// 28/11/2016 ECU swap around the shifted and unshifted activation of the carer
//                system
// 30/12/2016 ECU put in long press option for the flashlight (torch)
// 01/01/2017 ECU modify the BaseAdapter so that text can be displayed with an image
// 27/02/2017 ECU added daily summary activity
// ==================================================================================

public class GridActivity extends Activity implements SensorEventListener,OnGestureListener
{
	/* ============================================================================= */
	// Revision History
	// ================
	// 09/06/2013 ECU added the call to the mail app
	// 29/09/2014 ECU changed every occurrence of 'myIntent' to
	//                be 'localIntent'
	/* ============================================================================= */
	final static String TAG = "GridActivity";
	/* ============================================================================= */
	final static int    	BACK_KEY_COUNTER	= 5;		// 20/02/2014 ECU number of times the 'back' key must
															//                be pressed before the activity is
															//                exited
	final static int    	COLUMNS_DEFAULT		= 2;		// 16/02/2014 ECU added
	final static int    	DOSE_REQUEST_CODE	= 667;		// 25/03/2014 ECU added
	final static int    	FILE_REQUEST_CODE   = 666;		// 06/12/2013 ECU added
	/* ============================================================================= */
	// 18/01/2015 ECU declare the indices into the activeImages 2d array
	// -----------------------------------------------------------------------------
	public final static int IMAGE_INDEX			= 0;		// 18/01/2015 ECU added
	public final static int POSITION_INDEX		= 1;		// 18/01/2015 ECU added
	// =============================================================================
	final static int 		MENU_ZOOM_IN		= 0;		// 16/09/2013 ECU added - zoom in
	final static int    	MENU_ZOOM_OUT       = 1;		// 16/09/2013 ECU added - zoom out
	final static int    	MENU_ZOOM_RESTORE   = 2;		// 19/09/2013 ECU added - restore original size
	/* ============================================================================= */
	// 09/11/2014 ECU declare the thresholds for the 'swipe' event
	// -----------------------------------------------------------------------------
	final static int		SWIPE_DISTANCE_THRESHOLD	= 180;	
	final static int		SWIPE_VELOCITY_THRESHOLD	= 100;	
	// =============================================================================
	public static int		activityCounter = 0;		// 20/10/2014 ECU added
	public static Integer [][]
							activeImages;				// 19/01/2014 ECU added
														// 18/01/2015 ECU changed to 2d array
	public static Activity	activity = null;			// 04/05/2015 ECU added
	public static String    gridHelpFileHeader;			// 14/10/2014 ECU added - the header of help
														//                for a grid cell
	public static GridRefreshHandler 
							gridRefreshHandler = null;	// 13/04/2015 ECU added
														// 17/04/2015 ECU added preset to 'null'
	// =============================================================================
		   static Context	context;					// 03/03/2016 ECU added
	/* ============================================================================= */
	boolean					actionActivity	= true;		// 28/11/2014 ECU added
	int						backKeyCounter  = BACK_KEY_COUNTER;	
														// 20/02/2014 ECU added - running counter of
														//                back key counter
														// 06/02/2014 ECU made public
	// -----------------------------------------------------------------------------
	// 30/11/2014 ECU am using the 'beenPaused' so that I don't action a 'resume'
	//                unless a 'pause' has been actioned. This can happen when
	//                the activity is first started
	// -----------------------------------------------------------------------------
	int						activeImagePosition;		// 06/02/2015 ECU added
	boolean					activityStarted = false;	// 30/11/2014 ECU added
	boolean					beenPaused = false;			// 20/11/2014 ECU added
	int						clickCounter = 0;			// 25/01/2016 ECU added
	boolean					enableMonitor = false;		// 30/11/2014 ECU added
	GestureDetector 		gestureDetector;			// 09/11/2014 ECU added
	boolean					gridType = PublicData.gridType;
														// 11/10/2014 ECU remember the grid type
	GridView 				gridView;
	boolean					groupActivities = false;	// 06/10/2016 ECU added
	RelativeLayout			groupLayout;				// 18/10/2016 ECU added
	ImageButton				groupNextButton;			// 19/10/2016 ECU added
	ImageButton				groupPreviousButton;		// 19/10/2016 ECU added
	int						iconsPerRow = 2;			// 16/02/2014 ECU the number of icons per width
	int						iconSize;					// 16/02/2014 ECU use instead of
														//                imageWidth / imageHeight
														// 26/01/2014 ECU made public
	int						lastPosition = StaticData.NO_RESULT;
														// 25/01/2016 ECU remember the last position clicked
	Sensor 		   			lightSensor; 				// 17/02/2014 ECU moved here from MainActivity
	ListView				listView;					// 28/01/2014 ECU added
	Intent					localIntent;				// 10/11/2014 ECU added here
	ImageButton				notificationButton = null;	// 12/07/2016 ECU added
	int						positionToAction = StaticData.NO_RESULT;
														// 10/11/2014 ECU added
	SensorManager    		sensorManager = null; 		// 17/02/2014 ECU moved here from MainActivity
	boolean					volumeKey = false;			// 01/10/2014 ECU used to prevent multiple
														//                actioning of volume key when
	                                                    //				  zooming
	boolean					zoomed = false;				// 01/10/2014 ECU remember if zomming has happened
	/* ============================================================================= */
	// 19/01/2014 ECU created - if an entry is true then it will be shown in both
	//                development and normal mode. If false it will be shown only in
	//                normal mode
	//
	//				  If an entry is added then you will need to add
	//				  a case in gridView.setOnItemClickListener
	// 29/03/2014 ECU added Shopping
	// 22/04/2014 ECU change to static
	// 20/01/2015 ECU VERY IMPORTANT
	//                ==============
	//                do not change the drawable names because they could be used as 
	//                triggers by other classes and method. It is OK to add and
	//                delete entries. NOT all names are significant but just try
	//                and stick to this rule
	//                As of today the only one that must NOT be changed is 'music'
	// 27/01/2015 ECU added the final boolean on some images to indicate that there
	//                is a long press associated with this entry
	// 09/03/2015 ECU changed the GridImage class to include a 'validators' that
	//                determines if a particular activity is valid for this
	//                device
	// 08/05/2015 ECU changed Television entry to allow a 'long' press
	// 11/07/2015 ECU add a 'long press' on mail system to enable an email to be sent
	//                manually
	// 25/08/2015 ECU added validation to the 'torch' entry
	// 02/01/2016 ECU added long press on carer
	// 30/12/2016 ECU added long press on torch
	// -----------------------------------------------------------------------------
	// NOTES about the following 'gridImages' array
	// ===== 
	// -----------------------------------------------------------------------------
	// The following array defines a number of GridImages object which will be used for
	// laying out the user view - whether it be a grid or list.
	//
	// The structure of each entry is defined in the GridImages constructors but
	// for clarity the meaning is laid out here :-
	//
	// Element 1	Mandatory	the resource ID of the image to be displayed
	// Element 2	Mandatory	the text of the legend to be displayed - although not 
	//                          displayed when the user has a 'grid view', it needs
	//                          to be defined as it is used elsewhere in the system
	// Element 3	Mandatory	defines whether the entry is available in development
	//                          or production mode. 'true' the entry is only available
	//						    in production mode - 'false' means the entry is only
	//                          available in 'development' mode.
	// Element 4	Optional	Specifies whether a 'long press' is available on this entry.
	//                          The element can be 'true' or 'false' but omission of the
	//                          element implies 'false'
	// Element 5    Optional 	see Element 6
	// Element 6	Optional	Elements 5 and 6 together will help define a Method which
	//                          can be called to check whether an activity associated
	//  						with an element can run on this device, e.g. if the API
	//                          level is correct or does it have the necessary hardware.
	//                            Element 5 ..... is the class that contains the
	//                                            validation method
	//							  Element 6 ..... the name of the method within the
	//                                            class. It can be any name but
	//                                            prefer to standardise on "validation".
	//                          The defined validation Method, when called, must return
	//                          'true' if the activity associated with the element can
	//                          be started - 'false' if it cannot.
	// -----------------------------------------------------------------------------
	@SuppressWarnings("rawtypes")
	public static GridImages [] gridImages = {
								new GridImages<Object> 			(R.drawable.gameone,			"Visual Game",					true), 
								new GridImages<Object> 			(R.drawable.bouncing_ball,		"Dexterity Game",				true),	
								new GridImages<Object> 			(R.drawable.medication,			"Medication Details",			true,	true),
								new GridImages<Object> 			(R.drawable.photoalbum,			"Family Album",					true),
								new GridImages<Object> 			(R.drawable.exercise,			"Exercises",					true),	
								new GridImages<Object> 			(R.drawable.timer,				"Timers",						true),		
								new GridImages<Object> 			(R.drawable.carer,				"Carer Planning",				true,	true),		
								new GridImages<Object> 			(R.drawable.doctor,				"Doctor Appointments",			true,	true),	
								new GridImages<Object> 			(R.drawable.hospital,			"Hospital Appointments",		true,	true),	
								new GridImages<Object> 			(R.drawable.disk,				"Disk Utilities",				false),		
								new GridImages<Object> 			(R.drawable.microphone,			"Sound Recorder",				true),	
								new GridImages<Object> 			(R.drawable.video,				"Video Recorder",				true),		
								new GridImages<Validation> 		(R.drawable.torch,				"Torch",						true,	true, Validation.class, "torchValidation"),		
								new GridImages<Object> 			(R.drawable.internet,			"Browse the Internet",			true),
								new GridImages<Object> 			(R.drawable.music,				"Music Player",					true),  	
								new GridImages<Object> 			(R.drawable.compass_small,		"Compass",						true),	
								new GridImages<Object> 			(R.drawable.falling,			"Fall Detector",				true),
								new GridImages<Object> 			(R.drawable.mail,				"Mail System",					true,   true),	
								new GridImages<Object> 			(R.drawable.voice_recognition,	"Voice Commands",				true,	true),	
								new GridImages<Object> 			(R.drawable.bluetooth,			"Bluetooth",					false), 
								new GridImages<Object> 			(R.drawable.tcp,				"TCP Utilities",				false),	
								new GridImages<Object> 			(R.drawable.system_information,	"System Details",		   		false,	true),
								new GridImages<Object> 			(R.drawable.test,				"Test Facilities",				false),	
								new GridImages<Object> 			(R.drawable.audio_streaming,	"Audio Streaming",				true),	
								new GridImages<Object> 			(R.drawable.barcode,			"Barcode Reader",				true,	true),	
								new GridImages<Object> 			(R.drawable.dialogue,			"Start a Dialogue",				true),	
								new GridImages<Object> 			(R.drawable.files,				"File Explorer",				false),		
								new GridImages<Television> 		(R.drawable.television,			"Remote Controller",			true,	true, Television.class, "validation"),
								new GridImages<Object> 			(R.drawable.location,			"Tracking Facility",			true),	
								new GridImages<Object> 			(R.drawable.settings,			"System Settings",				true),		
								new GridImages<Object> 			(R.drawable.audio_analyser,		"Audio Analyser",				false),
								new GridImages<Object> 			(R.drawable.speaking_clock,   	"Speaking Clock",				true,	true),
								new GridImages<Object>			(R.drawable.shopping,   		"Shopping",						true),
								new GridImages<Object> 			(R.drawable.swipe,   			"Swipe Tests",					false),	
								new GridImages<Object> 			(R.drawable.radar,   			"Radar Security",				false),
								new GridImages<Object> 			(R.drawable.drawer,   			"Navigation Drawer",			false),
								new GridImages<WeMoActivity>	(R.drawable.wemo,   		 	"WeMo Tasks",					true,	true, WeMoActivity.class, "validation"),
								new GridImages<Object>			(R.drawable.screen_capture,		"Screen Capture",				true),
								new GridImages<Object>			(R.drawable.tone_generator,		"Tone Generator",				false,	true),
								new GridImages<Object>			(R.drawable.tv_guide,			"TV Program Guide",				true,	true),
								new GridImages<Object> 			(R.drawable.database,   		"Contacts",						true,	true),
								new GridImages<Object> 			(R.drawable.panic_alarm,   		"Panic Alarm",					true,	true),
								new GridImages<LiquidActivity> 	(R.drawable.liquid,   			"Liquid Selection",				true,	false, LiquidActivity.class, "validation"),
								new GridImages<NFC_Activity> 	(R.drawable.nfc,   			    "NFC Tags",						true,	false, NFC_Activity.class, "validation"),
								new GridImages<Object> 			(R.drawable.named_actions,		"Named Actions",				false),
								new GridImages<Object> 			(R.drawable.upnp,				"UPnP - WeMo",					false),
								new GridImages<Object> 			(R.drawable.groups,				"Group Activities",				false),
								new GridImages<Object> 			(R.drawable.documents,			"Documents",					false),
								new GridImages<Object> 			(R.drawable.daily_summary,		"Daily Summary",				true,	true)
						};
	// =============================================================================
	// 16/01/2015 ECU changed to public static
	// -----------------------------------------------------------------------------
	public static ArrayList<GridItem> gridItems = new ArrayList<GridItem>();
	// =============================================================================
	// 22/05/2015 ECU have an array to stored the 'unsorted' array
	// 19/10/2016 ECU do the prest to 'null'
	// -----------------------------------------------------------------------------
	@SuppressWarnings ("rawtypes")
	static GridImages [] originalGridImages = null; 
	/* ============================================================================= */
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		// 09/10/2015 ECU want to check if the activity is being newly created or
		//                just recreated having been destroyed by Android
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		// 06/10/2015 ECU check if this activity has been restarted automatically
		//                rather than through the main activity - if not then just 
		//                'finish'
		// 09/10/2015 ECU changed to use the saved instance state
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 04/05/2015 ECU set the activity for use by other activities
			// ---------------------------------------------------------------------
			activity = (Activity) this;
			// ---------------------------------------------------------------------
			// 03/03/2015 ECU save the context for later use (do not want to cast
			//                activity
			// ---------------------------------------------------------------------
			context	= this;
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 06/10/2016 ECU check for grouping of activities
				// -----------------------------------------------------------------
				groupActivities = extras.getBoolean (StaticData.PARAMETER_GROUP,false);
				// -----------------------------------------------------------------
				if (groupActivities)
				{
					// -------------------------------------------------------------
					// 08/10/2016 ECU want to get the full list of activities
					// -------------------------------------------------------------
					PublicData.storedData.gridImages = null;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 19/10/2016 ECU store the original images but only once - originally
			//                there was no check on null which caused issues with
			//                grouping activities
			// ---------------------------------------------------------------------
			if (originalGridImages == null)
				originalGridImages = Arrays.copyOf (gridImages,gridImages.length);
			// ---------------------------------------------------------------------
			// 18/01/2015 ECU check on whether the instance of gridImages in storedData
			//                is to be reset
			// 08/10/2016 ECU check on grouping
			// 09/10/2016 ECU add the null check on lists
			// ---------------------------------------------------------------------
			if (!PublicData.storedData.groupActivities || groupActivities || PublicData.storedData.groupLists == null)
			{
				if (PublicData.storedData.gridImages == null || 
						(PublicData.storedData.gridImages.length != gridImages.length) ||
						!PublicData.storedData.sortByUsage)
				{
					// -----------------------------------------------------------------
					// 18/01/2015 ECU need to reset the stored data with class data
					// -----------------------------------------------------------------
					PublicData.storedData.gridImages = gridImages;
					// -----------------------------------------------------------------
				}
				else
				{
					// -----------------------------------------------------------------
					// 18/01/2015 ECU the stored version is OK to be used
					// -----------------------------------------------------------------
					gridImages = PublicData.storedData.gridImages;
					// -----------------------------------------------------------------
				}
			}
			else
			{
				// -----------------------------------------------------------------
				// 08/10/2016 ECU group is on
				// -----------------------------------------------------------------
				gridImages = GroupList.returnArray (PublicData.storedData.groupListCurrent);
				// -----------------------------------------------------------------
				if (gridImages == null)
					gridImages = Arrays.copyOf (originalGridImages,originalGridImages.length);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 31/12/2014 ECU indicate that activity has been entered
			// ---------------------------------------------------------------------
			PublicData.gridActivityEntered = true;
			// ---------------------------------------------------------------------
			// 13/04/2015 ECU set up the refresh handler
			// ---------------------------------------------------------------------
			gridRefreshHandler = new GridRefreshHandler ();
			// ---------------------------------------------------------------------
			// 10/11/2014 ECU check if any parameter fed through in the Intent
			// ---------------------------------------------------------------------
			if (extras != null) 
			{
				positionToAction = extras.getInt(StaticData.PARAMETER_POSITION, StaticData.NO_RESULT);
				// -----------------------------------------------------------------
				// 30/12/2014 ECU make sure that the specified command is in range
				// -----------------------------------------------------------------
				if (positionToAction >= gridImages.length || positionToAction < 0)
				{
					// -------------------------------------------------------------
					// 30/12/2014 ECU invalid entry so indicate 'no parameter'
					// -------------------------------------------------------------
					positionToAction = StaticData.NO_RESULT;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 28/11/2014 ECU check if being asked to action or return intent
				//            ECU this is used by the TimerActivity when trying to
				//                schedule the starting of a particular activity
				// -----------------------------------------------------------------
				actionActivity = extras.getBoolean (StaticData.PARAMETER_INTENT,true);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 10/11/2014 ECU check whether the activity is being called with a
			//                parameter or not
			// ---------------------------------------------------------------------
			if (positionToAction == StaticData.NO_RESULT)
			{
				// -----------------------------------------------------------------
				// 10/11/2014 ECU activity called without a parameter
				// -----------------------------------------------------------------
 		
				// -----------------------------------------------------------------
				// 16/02/2014 ECU call up routine to set common activity features
				// 08/04/2014 ECU changed to use the variable
				//            ECU added the ...SCREEN_ON argument rather than having
				//                the code inline
				// 02/04/2016 ECU only keep the screen on if the activity is 
				//                not being started by the TimerActivity to selected
				//                but not action a task - this is indicated by 
				//                actionActivity being false.
				//                replaced StaticData.ACTIVITY_SCREEN_ON with
				//                actionActivity
				// 08/10/2016 ECU add the check on grouping
				// -----------------------------------------------------------------
				if (!PublicData.storedData.groupActivities)
				{
					Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN,actionActivity);
				}
				else
				{
					// -------------------------------------------------------------
					// 09/10/2016 ECU changed final argument from actionActivity to false
					//            ECU change back to always full screen because not showing
					//                group name on title
					// -------------------------------------------------------------
					Utilities.SetUpActivity (this,
											 StaticData.ACTIVITY_FULL_SCREEN,
											 false);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 19/01/2014 ECU set up the images depending on whether in development
				//                mode or not
				// 10/03/2015 ECU changed to use 'developmentMode' in 'storedData'
				// -----------------------------------------------------------------
				activeImages = GetImages (PublicData.storedData.developmentMode);
				// -----------------------------------------------------------------
				// 14/10/2014 ECU set up the header to the help files that correspond to
				//                grid cells
				// -----------------------------------------------------------------
				gridHelpFileHeader = PublicData.projectFolder + 
											this.getString (R.string.help_directory) + 
											this.getString (R.string.grid_help_file_header);
				// -----------------------------------------------------------------
				// 09/11/2014 ECU add in the gesture detector
				// -----------------------------------------------------------------
				gestureDetector = new GestureDetector (GridActivity.this,this);
				// -----------------------------------------------------------------
				// 11/10/2014 ECU display the appropriate user view - the code from the
				//                method used to be here
				// -----------------------------------------------------------------
				DisplayUserView (PublicData.storedData.userView);
				// -----------------------------------------------------------------	
				// 17/02/2014 ECU set up the sensor aspects
				// 02/03/2015 ECU put in the check on SENSOR_SERVICE
				// 01/09/2015 ECU changed to use StaticData
				// ----------------------------------------------------------------- 
				if (!StaticData.SENSOR_SERVICE)
				{
					sensorManager 	= (SensorManager) getSystemService(SENSOR_SERVICE); 
					lightSensor 	= sensorManager.getDefaultSensor (Sensor.TYPE_LIGHT); 
				}
				// -----------------------------------------------------------------
				// 11/09/2013 ECU put up a message about help options
				// 18/02/2014 ECU changed to use new method
				// 28/11/2014 ECU only display if actioning activities
				// 09/10/2015 ECU only display the drawable is app started manually
				// 06/03/2016 ECU changed the '0' wait time in the call to 
				//                DRAWABLE_WAIT_TIME
				// -----------------------------------------------------------------
				if (actionActivity)
				{
					if (PublicData.startedManually)
					{
						// ---------------------------------------------------------
						// 09/08/2016 ECU added the final 'true' to indicate that the
						//                image should be scaled to fit the screen
						// 08/10/2016 ECU put in the check on grouping
						// ---------------------------------------------------------
						if (!PublicData.storedData.groupActivities && !groupActivities)
							Utilities.DisplayADrawable (context,R.drawable.grid_help,StaticData.DRAWABLE_WAIT_TIME,false,true);
						// ---------------------------------------------------------
						// 12/07/2016 ECU put in the check on the number of clicks
						//                needed to start an activity
						// ---------------------------------------------------------
						clickCounterReminder ();
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 24/10/2015 ECU the app was automatically restarted
						// ---------------------------------------------------------
						// 22/10/2015 ECU check on the last activity that was actioned
						// ---------------------------------------------------------
						if (PublicData.storedData.lastActivity != StaticData.NO_RESULT)
						{
							// -----------------------------------------------------
							// 24/10/2015 ECU try and restart the activity that was
							//                interrupted by the Android OS
							// -----------------------------------------------------
							SwitchOnImage (PublicData.storedData.lastActivity,null);
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
					}
				}
				// -----------------------------------------------------------------
				// 28/11/2015 ECU at this point the user interface is running
				// -----------------------------------------------------------------
				PublicData.userInterfaceRunning = true;
				// -----------------------------------------------------------------
				// 06/03/2016 ECU action the stored alarms - this used to be done
				//                in MainActivity.InitialiseServices but do not want
				//                anything to start until the user interface is up 
				//                and running
				//            ECU make sure do not do if being called by the TimerActivity
				//                to select a specific activity
				// 16/03/2016 ECU moved the panic alarm initialise into here from
				//                the main activity
				// -----------------------------------------------------------------
				if (actionActivity)
				{
					TimerActivity.actionStoredAlarms (MainActivity.activity);
					PanicAlarmActivity.Initialise (MainActivity.activity);
				}
				// -----------------------------------------------------------------
				// 18/05/2016 ECU check if an activity is to be started automatically
				// -----------------------------------------------------------------
				if (PublicData.storedData.activityOnStart)
				{
					SwitchOnImage (PublicData.storedData.activityOnStartNumber,null);
				}
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 10/11/2014 ECU activity has been called with a position parameter so
				//                start up the required activity. Be careful the second
				//       		  argument is the view so 'null' may not be a good idea
				// -----------------------------------------------------------------
				SwitchOnImage (positionToAction,null);
				// -----------------------------------------------------------------
				// 10/11/2014 ECU and terminate this activity
				// -----------------------------------------------------------------
				finish ();
				// ----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 06/10/2015 ECU activity has been started automatically without
			//                the MainActivity being ready so just finish
			// ---------------------------------------------------------------------
			finish ();
			// ---------------------------------------------------------------------			
		}
	}
	/* ============================================================================= */
	public void onAccuracyChanged (Sensor sensor, int accuracy) 
	{ 
	  	if(sensor.getType() == Sensor.TYPE_LIGHT)
	   	{	
	  		// ---------------------------------------------------------------------
	   		//          ECU the accuracy of the sensor has changed
	  		// ---------------------------------------------------------------------
	   	} 
	}
	// =============================================================================
	@Override
	public void onConfigurationChanged (Configuration theNewConfiguration) 
	{
		// -------------------------------------------------------------------------
		// 03/10/2015 ECU created - called when a configuration change occurs
		// -------------------------------------------------------------------------
		super.onConfigurationChanged (theNewConfiguration);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
    public void onDestroy()
    {	
		// -------------------------------------------------------------------------
		// 21/09/2013 ECU added
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
    }
	/* ============================================================================ */
	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event) 
	{
		// -------------------------------------------------------------------------
		// 20/02/2014 ECU added to process the back key
		// -------------------------------------------------------------------------
	    if (keyCode == KeyEvent.KEYCODE_BACK) 
	    {	
	    	// ---------------------------------------------------------------------
	    	// 06/10/2016 ECU check if grouping of activities is on
	    	// ---------------------------------------------------------------------
	    	if (groupActivities)
	    	{
	    		// -----------------------------------------------------------------
	    		// 06/10/2016 ECU grouping of activities is switched on
	    		// -----------------------------------------------------------------
	    		GroupActivity.groupMessageHandler.sendEmptyMessage(StaticData.MESSAGE_FINISH);
	    		// -----------------------------------------------------------------
	    		// 06/10/2016 ECU finish this activity
	    		// -----------------------------------------------------------------
	    		finish ();
	    		// -----------------------------------------------------------------
	    		// 06/10/2016 ECU exit this activity
	    		// -----------------------------------------------------------------
	    		return true;
	    		// -----------------------------------------------------------------
	    	}
	    	// ---------------------------------------------------------------------
	    	// 01/10/2014 ECU if the screen has been 'zoomed' then reset it
	    	//            ECU added the 'zoomed' flag
	    	// ---------------------------------------------------------------------
	    	if (!PublicData.storedData.userView && zoomed)
	    	{
	    		ZoomDisplay (COLUMNS_DEFAULT);
	    	}	
	    	// ---------------------------------------------------------------------
	    	// 20/02/2014 ECU decrement the 'back' key counter - if it reaches
	    	//                zero then the activity can be exited
	    	// ---------------------------------------------------------------------
	    	if (--backKeyCounter > 0)
	    	{
	    		// -----------------------------------------------------------------
	    		// 20/02/2014 ECU it is not time to exit so just ignore this
	    		//                key
	    		// -----------------------------------------------------------------
	    		if (backKeyCounter == 1)
	    		{
	    			// -------------------------------------------------------------
	    			// 20/02/2014 ECU display a warning message
	    			// 08/04/2014 ECU use the resource
	    			// 11/09/2015 ECU add the 'true' for centring the text
	    			// -------------------------------------------------------------
	    			Utilities.popToast (getString(R.string.press_back_key),true);
	    			// -------------------------------------------------------------
	    		}
	    		// -----------------------------------------------------------------
	    		return true;	    		
	    	}
	    	else
	    	{
	    		// -----------------------------------------------------------------
	    		// 20/02/2014 ECU reached the stage where the activity should be
	    		//                exited
	    		// -----------------------------------------------------------------
	    		return super.onKeyDown (keyCode, event);
	    		// -----------------------------------------------------------------
	    	}
	    }
	    // -------------------------------------------------------------------------
	    // 01/10/2014 ECU try and pick up the volume keys
	    // -------------------------------------------------------------------------
	    else
	    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) 
	    {
	    	if (!PublicData.storedData.userView)
	    	{
	    		if (!volumeKey)
	    		{
	    			// -------------------------------------------------------------
	    			// 01/10/2014 ECU want to call ZoomDisplay with
	    			// 				    false ..... zoom in
	    			//                  true  ..... zoom out
	    			// -------------------------------------------------------------
	    			ZoomDisplay (keyCode == KeyEvent.KEYCODE_VOLUME_UP);
	    			// -------------------------------------------------------------
	    			// 01/10/2014 ECU indicate that the volume key has been pressed
	    			// -------------------------------------------------------------
	    			volumeKey = true;
	    			// -------------------------------------------------------------
	    			// 01/10/2014 ECU indicate that zooming has occurred
	    			// -------------------------------------------------------------
	    			zoomed = true;
	    			// -------------------------------------------------------------
	    		}
	    		return true;
	    	}
	    	else
	    		return super.onKeyDown(keyCode, event);
	    }   
	    // -------------------------------------------------------------------------
	    else
	    {
	    	 // --------------------------------------------------------------------
	    	 // 20/02/2014 ECU reset the 'back key' counter
			 // -------------------------------------------------------------------- 
			 backKeyCounter = BACK_KEY_COUNTER;
			 // --------------------------------------------------------------------
	         return super.onKeyDown(keyCode, event);
	    }
	}
	/* ============================================================================ */
	@Override
	public boolean onKeyUp (int keyCode, KeyEvent keyEvent) 
	{
	    // -------------------------------------------------------------------------
	    // 01/10/2014 ECU try and pick up the volume keys
	    // -------------------------------------------------------------------------
	    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) 
	    {
	    	if (!PublicData.storedData.userView)
	    	{
	    		// -----------------------------------------------------------------
	    		// 01/10/2014 ECU indicate that zooming can be used next time the
	    		//                volume key is pressed
	    		// -----------------------------------------------------------------
	    		volumeKey = false;
	    		// -----------------------------------------------------------------
	    	}    	
	    }
	    // -------------------------------------------------------------------------
	    // 01/10/2014 ECU get the key processed normally
	    // -------------------------------------------------------------------------
	    return super.onKeyUp (keyCode, keyEvent);
	    // -------------------------------------------------------------------------
	}	
	// =============================================================================
	private View.OnClickListener nextGroupButtonOnClickListener = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{
			if (PublicData.storedData.groupListCurrent < (PublicData.storedData.groupLists.size() - 1))
			{
				PublicData.storedData.groupListCurrent++;
				// -------------------------------------------------------------
            	// 08/10/2016 ECU restart this activity
        		// 09/10/2016 ECU moved here
            	// -------------------------------------------------------------
        		finish ();
            	localIntent = new Intent (context,GridActivity.class);
        		startActivityForResult (localIntent,0);
        		// -------------------------------------------------------------
			}
		}
	};
	// =============================================================================
	private View.OnClickListener notificationButtonOnClickListener = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{
			// ---------------------------------------------------------------------
			// 12/07/2016 ECU created to handle the notification button
			// ---------------------------------------------------------------------
			// 13/07/2016 ECU stop any animation
			// ---------------------------------------------------------------------
			notificationButton.clearAnimation ();
			// ---------------------------------------------------------------------
			// 13/07/2016 ECU start up the notifications activity
			// ---------------------------------------------------------------------
			localIntent = new Intent (context,NotificationsActivity.class);
			startActivity (localIntent);
			// ---------------------------------------------------------------------
		}
	};
	// =============================================================================
	@Override 
	protected void onPause () 
	{ 
		// -------------------------------------------------------------------------
		// 21/09/2013 ECU added
		// 17/02/2014 ECU added the sensor bit - unregister the listener
		// 02/03/2015 ECU put in the check on SENSOR_SERVICE
		// 01/09/2015 ECU changed to use StaticData
		// -------------------------------------------------------------------------	
		if (!StaticData.SENSOR_SERVICE)
		{
			if (sensorManager != null)
				sensorManager.unregisterListener (this); 
		}
		// -------------------------------------------------------------------------
		//11/10/2014 ECU remember the state of the grid display
		// -------------------------------------------------------------------------
		gridType = PublicData.gridType;
		// -------------------------------------------------------------------------
		// 16/01/2015 ECU indicate that display is not to be rebuilt
		// -------------------------------------------------------------------------
		PublicData.gridRebuild = false;
		// -------------------------------------------------------------------------
		// 30/11/2014 ECU indicate that the activity has been paused
		// -------------------------------------------------------------------------
		beenPaused = true;
		// -------------------------------------------------------------------------
		// 30/11/2014 ECU if the monitoring service is running then stop it
		//            ECU put in the activityStarted check because only want
		//                to action if an activity is started rather than the
		//                device going into 'sleep mode' for example.
		// -------------------------------------------------------------------------
		if (activityStarted && PublicData.monitorServiceRunning)
		{
			// ---------------------------------------------------------------------
			// 30/11/2014 ECU indicate that the service is not to be restarted
			// ---------------------------------------------------------------------
			PublicData.storedData.monitor.enabled = false;
			stopService (new Intent(this,MonitorService.class));		
			enableMonitor = true;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		activityStarted = false;
		// -------------------------------------------------------------------------
		// 25/01/2016 ECU reset the last click position and its counter
		// -------------------------------------------------------------------------
		clickCounter = 0;
		lastPosition = StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
		super.onPause(); 
	} 
	// =============================================================================
	private View.OnClickListener previousGroupButtonOnClickListener = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{
			if (PublicData.storedData.groupListCurrent > 0)
			{
				PublicData.storedData.groupListCurrent--;				
				// -------------------------------------------------------------
				// 08/10/2016 ECU restart this activity
				// 09/10/2016 ECU moved here
				// -------------------------------------------------------------
				finish ();
				localIntent = new Intent (context,GridActivity.class);
				startActivityForResult (localIntent,0);
				// -------------------------------------------------------------
			}
		}
	};
	/* ============================================================================ */
	@Override 
	protected void onResume() 
	{ 
		// -------------------------------------------------------------------------
		// 21/09/2013 ECU added
		// 17/02/2014 ECU moved the sensor stuff here - re-register the listener
		// 02/03/2015 ECU put in the check on SENSOR_SERVICE
		// 01/09/2015 ECU changed to use StaticData
		// -------------------------------------------------------------------------
		if (!StaticData.SENSOR_SERVICE)
		{
			if (sensorManager != null)
				sensorManager.registerListener (this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
		// -------------------------------------------------------------------------
		// 11/10/2014 ECU check if the display layout has changed
		// -------------------------------------------------------------------------
		if (gridType != PublicData.gridType) 
		{
			// ---------------------------------------------------------------------
			// 11/10/2014 ECU try and restart the current activity
			// ---------------------------------------------------------------------
			DisplayUserView (PublicData.gridType);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 16/01/2015 ECU added the check on 'gridRebuild'
		// 18/01/2015 ECU added the sortByUsage option
		// -------------------------------------------------------------------------
		if (PublicData.gridRebuild || PublicData.storedData.sortByUsage) 
		{
			// ---------------------------------------------------------------------
			// 16/01/2015 ECU make sure the rebuild flag is cleared
			// ---------------------------------------------------------------------
			PublicData.gridRebuild = false;
			// ---------------------------------------------------------------------
			// 22/05/2015 ECU reset the images array - back to its 'unsorted' state
			// ---------------------------------------------------------------------
			gridImages = Arrays.copyOf (originalGridImages,originalGridImages.length);
			// ---------------------------------------------------------------------
			// 16/01/2015 ECU rebuild the display
			// 10/03/2015 ECU changed to use 'developmentMode' in 'storedData'
			// ---------------------------------------------------------------------
			activeImages = GetImages (PublicData.storedData.developmentMode);
			DisplayUserView (PublicData.gridType);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		if (beenPaused)
		{
			// --------------------------------------------------------------------
			// 30/11/2014 ECU reset the flag that indicates an 'onPause' has
			//                happened
			// --------------------------------------------------------------------
			beenPaused = false;
			// --------------------------------------------------------------------
			// 30/11/2014 ECU reenable the monitor service if it was running
			//                when 'onPause' occurred
			// --------------------------------------------------------------------
			if (enableMonitor)
			{
				enableMonitor = false;
				PublicData.storedData.monitor.enabled = true;
			}
			// --------------------------------------------------------------------
		}
		// ------------------------------------------------------------------------
	   	super.onResume(); 
	   	// ------------------------------------------------------------------------
	} 
	/* ============================================================================= */
	public void onSensorChanged (SensorEvent sensorEvent) 
	{ 
		// -------------------------------------------------------------------------
		// 12/07/2013 ECU only interested in the ambient light sensor
		// 01/09/2015 ECU changed to use StaticData
		// -------------------------------------------------------------------------
	   	if (!StaticData.SENSOR_SERVICE)
	   	{
	   		if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT)
	   		{ 	  
	   			// -----------------------------------------------------------------
	   			// 02/03/2015 ECU call the method to check the light level. This
	   			//                method has been created from code that was here
	   			// -----------------------------------------------------------------
	   			Utilities.checkLightLevel (context, sensorEvent.values [0]);
	   			// -----------------------------------------------------------------
	   		}
	    } 
	}
	// =============================================================================
	@Override
	public void onRestart () 
	{
		// -------------------------------------------------------------------------
		super.onStop();   
	}
	// =============================================================================
	@Override
	public void onStart () 
	{
		// -------------------------------------------------------------------------
		super.onStop();   
	}
	// =============================================================================
    @Override
    public void onStop() 
    {
    	// -------------------------------------------------------------------------
    	super.onStop();
    }
	/* ============================================================================= */
	@Override
	public void onActivityResult (int theRequestCode, int theResultCode, Intent theIntent) 
	{
		// -------------------------------------------------------------------------
		// 20/06/2013 ECU just note that the activities above are normally started
		//                with a request code of '0' - those activities will not
		//                normally pass back a 'result' or an intent - however this
		//                method will still be called with a 'theRequestCode' of
		//                '0'
		// -------------------------------------------------------------------------
	    if (theRequestCode == FILE_REQUEST_CODE)
	    {
	    	// ---------------------------------------------------------------------
	    	// 06/12/2013 ECU added
	    	// 10/11/2014 ECU changed to use the PARAMETER.... variable
	    	// ---------------------------------------------------------------------
	    	if (theResultCode == RESULT_OK)
	    	{
	    		String fileName   = theIntent.getStringExtra(StaticData.PARAMETER_FILE_NAME);
	    		// -----------------------------------------------------------------
	    		// 08/04/2014 ECU changed to use resource
	    		// -----------------------------------------------------------------
	    		Utilities.popToast (getString(R.string.file_name_returned) + "\n" + fileName,true);
	    	}
	    	else 
	 	    if (theResultCode == RESULT_CANCELED) 
	 	    {
	 	       // Handle cancel
	 	    }
	    } 
	    // -------------------------------------------------------------------------
	    else
	    if (theRequestCode == StaticData.REQUEST_CODE_FINISH)
	    {
	    	// ---------------------------------------------------------------------
	    	// 17/10/2014 ECU check whether the activity is to be finished
	    	// ---------------------------------------------------------------------
	    	if (theResultCode == StaticData.RESULT_CODE_FINISH)
	    	{
	    		// -----------------------------------------------------------------
	    		// 17/10/2014 ECU just finish this activity
	    		//            ECU set the correct result code so that the calling
	    		//                activity will 'finish'
	    		// -----------------------------------------------------------------
	    		setResult (StaticData.RESULT_CODE_FINISH);
	    		finish ();
	    		// -----------------------------------------------------------------		
	    	}
	    	// ---------------------------------------------------------------------
	    }
	}
	/* ============================================================================= */
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) 
	{
		// -------------------------------------------------------------------------
		// 28/01/2018 ECU only inflate menu if in grid mode
		// -------------------------------------------------------------------------
		if (!PublicData.storedData.userView)
		{
			menu.clear ();
			// ---------------------------------------------------------------------
			// 05/06/2013 ECU used the method to build menu
			// ---------------------------------------------------------------------
			menu.add (0,MENU_ZOOM_IN,0,"Zoom in");
			menu.add (0,MENU_ZOOM_OUT,0,"Zoom out");
			menu.add (0,MENU_ZOOM_RESTORE,0,"Restore to Original");	
		}
		// -------------------------------------------------------------------------	
		return true;
	}
	/* ============================================================================= */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// -------------------------------------------------------------------------
		// 28/01/2018 ECU only inflate menu if in grid mode
		// -------------------------------------------------------------------------
		if (!PublicData.storedData.userView)
			getMenuInflater().inflate(R.menu.grid,menu);
		// -------------------------------------------------------------------------	
		return true;
	}
	/* ============================================================================= */
	public boolean onOptionsItemSelected (MenuItem item)
	{
		// -------------------------------------------------------------------------
		// 16/06/2013 ECU take the actions depending on which menu is selected
		// 16/02/2014 ECU rewrite to scale by 'icons per row'
		// -------------------------------------------------------------------------
		switch (item.getItemId())
		{
			// ---------------------------------------------------------------------
			case MENU_ZOOM_IN:
				if (iconsPerRow > 1)
					iconsPerRow--;
				break;
			// ---------------------------------------------------------------------
			case MENU_ZOOM_OUT:
				if (iconsPerRow < 10)
					iconsPerRow++;
				break;
			// ---------------------------------------------------------------------
			case MENU_ZOOM_RESTORE:
				iconsPerRow = COLUMNS_DEFAULT;
				break;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 16/02/2014 ECU now reset the sizes according to the number per row
		// -------------------------------------------------------------------------
		SetImageSizes (iconsPerRow);
		// -------------------------------------------------------------------------
		// 16/09/2013 ECU update some display variables before asking for a refresh
		// -------------------------------------------------------------------------		
		PublicData.imageAdapter.notifyDataSetChanged ();
		// -------------------------------------------------------------------------
		return true;
	}
	/* ============================================================================= */
	public class ImageAdapter extends BaseAdapter 
	{
		// -------------------------------------------------------------------------
		// 02/01/2017 ECU Note - declare the adapter that is used when the 'grid'
		//                       view is being used
		// -------------------------------------------------------------------------
	    private Context 		localContext;
	    private LayoutInflater 	localInflater = null;				// 01/01/2017 ECU added
	    // -------------------------------------------------------------------------
	    // 02/01/2017 ECU Note - keep a local copy of the image data
	    // -------------------------------------------------------------------------
	    public Integer[] localImages = activeImages [IMAGE_INDEX];
		/* ========================================================================= */
	    public ImageAdapter (Context theContext)
	    {
	        localContext = theContext;
	        // ---------------------------------------------------------------------
	        // 01/01/2017 ECU declare the inflater which will be used for the custom
	        //                layout for each cell
	        // ---------------------------------------------------------------------
	        localInflater = (LayoutInflater)localContext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
	        // ---------------------------------------------------------------------
	    }
		/* ========================================================================= */
	    @Override
	    public int getCount () 
	    {
	    	// ---------------------------------------------------------------------
	    	// 02/01/2017 ECU Note - return the length of the stored image data
	    	// ---------------------------------------------------------------------
	        return localImages.length;
	        // ---------------------------------------------------------------------
	    }
		/* ========================================================================= */
	    @Override
	    public Object getItem (int thePosition) 
	    {
	    	// ---------------------------------------------------------------------
	    	// 02/01/2017 ECU Note - return the 'image' object at the specified
	    	//                       position
	    	// ---------------------------------------------------------------------
	        return localImages [thePosition];
	        // ---------------------------------------------------------------------
	    }
		/* ========================================================================= */
	    @Override
	    public long getItemId (int thePosition)
	    {
	    	// ---------------------------------------------------------------------
	    	// 02/01/2017 ECU Note - return the ID of the specified 'image' - not
	    	//                       relevant here
	    	// ---------------------------------------------------------------------
	        return 0;
	        // ---------------------------------------------------------------------
	    }
	    // -------------------------------------------------------------------------
	    public class Holder
	    {
	    	// ---------------------------------------------------------------------
	    	// 01/01/2017 ECU declare a 'holder' for the views that exist in the cell
	    	// ---------------------------------------------------------------------
	    	ImageView imageView;
	        TextView  textView;  
	        // ----------------------------------------------------------------------
	    }
	    /* ------------------------------------------------------------------------- */
	    @Override
	    public View getView (int position, View convertView, ViewGroup parent) 
	    {
	    	View 	gridCellView 	= convertView;
	    	Holder 	localHolder 	= null;
	    	// ---------------------------------------------------------------------
	    	// 01/01/2017 ECU check if the view already exists
	    	// ---------------------------------------------------------------------
			if (gridCellView == null) 
			{
				// -----------------------------------------------------------------
				// 01/01/2017 ECU the view does not exist so create it
				// -----------------------------------------------------------------
				// 01/01/2017 ECU inflate the layout for the grid cell
				// -----------------------------------------------------------------
				gridCellView = localInflater.inflate (R.layout.grid_view_cell,parent,false);
				// -----------------------------------------------------------------
				// 01/01/2017 ECU create a holder for the cell's views and then 
				//                populate it
				// -----------------------------------------------------------------
				localHolder = new Holder ();
				localHolder.imageView = (ImageView) gridCellView.findViewById (R.id.grid_cell_imageview);
				localHolder.textView  = (TextView)  gridCellView.findViewById (R.id.grid_cell_textview);
				// -----------------------------------------------------------------
				// 01/01/2017 ECU set the tag for this view using the data that has
				//                been set
				// -----------------------------------------------------------------
				gridCellView.setTag (localHolder);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 01/01/2017 ECU the view already exists so set the data views
				//                from the tag that has been set
				// -----------------------------------------------------------------
				localHolder = (Holder) gridCellView.getTag ();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 01/01/2017 ECU 'localHolder' contains the views that need to be updated
	    	// ---------------------------------------------------------------------
	        localHolder.imageView.setImageResource (localImages [position]);
	        // ---------------------------------------------------------------------
	        // 01/01/2017 ECU clear the text view. This is necessary because views
	        //                get recycled so the wrong text can be displayed with an
	        //                image.
	        // ---------------------------------------------------------------------
	        localHolder.textView.setText ("");
	        // ---------------------------------------------------------------------
	        // 12/09/2013 ECU set image size using variables rather than actual values
	        // 16/02/2014 ECU use iconSize
	        // ---------------------------------------------------------------------
	        gridCellView.setLayoutParams (new GridView.LayoutParams (iconSize,iconSize));
	        // ---------------------------------------------------------------------
	        // 01/01/2017 ECU scale the text size
	        // ---------------------------------------------------------------------
	        localHolder.textView.setTextSize (TypedValue.COMPLEX_UNIT_PX,(getResources().getDimension(R.dimen.default_big_font_size) / iconsPerRow));
	        // ---------------------------------------------------------------------
	        // 02/06/2013 ECU want to remember the  ImageView for the music player
	        // 19/01/2014 ECU changed to use the activeImages
	        // ---------------------------------------------------------------------
	        // ---------------------------------------------------------------------
			// 20/01/2015 ECU VERY IMPORTANT
			//                ==============
			//                in the following list only R.drawable.music is correct
			//                - the others will fail if any scrolling occurs
	        // 21/01/2015 ECU took out references to compass, slide show, and torch
	        // 31/12/2016 ECU changed to use the new UserInterface method to modify
	        //                the displayed details of an activity
	        // 01/01/2017 ECU added the 'textview' for updating and changed to use
	        //                thumbIds rather than activeImages [IMAGE_INDEX]
			// ---------------------------------------------------------------------	
	        UserInterface.activityUpdate (localImages [position],localHolder.imageView,localHolder.textView);
	        // ---------------------------------------------------------------------
	        // 02/01/2017 ECU Note - return the view
	        // ---------------------------------------------------------------------
	        return gridCellView;
	        // ----------------------------------------------------------------------
	    }
	    /* ------------------------------------------------------------------------- */
	}
	// ==============================================================================
	ArrayList<GridItem> buildGridItemsList ()
	{
		// -------------------------------------------------------------------------
		// 03/03/2015 ECU method created to build the grid items array form
		//                the grid images array. Created so as not to have a
		//                separate copy for each user view
		// -------------------------------------------------------------------------
		ArrayList<GridItem> localGridItems = new ArrayList<GridItem>();
		// -------------------------------------------------------------------------	
		// 10/02/2014 ECU no sorting by usage
		// -------------------------------------------------------------------------	
		for (int theIndex = 0; theIndex < gridImages.length; theIndex++)
		{
			// ---------------------------------------------------------------------
			// 31/01/2014 ECU take into account the development mode
			// 10/03/2015 ECU changed to use 'developmentMode' in 'storedData'
			// ---------------------------------------------------------------------		
			if (PublicData.storedData.developmentMode)
			{
				// -----------------------------------------------------------------
				// 31/01/2014 ECU in development mode so include all images
				// 27/01/2015 ECU included the 'long press' argument
				// -----------------------------------------------------------------		
				localGridItems.add (new GridItem (gridImages [theIndex].imageId,gridImages [theIndex].legend,gridImages [theIndex].longPress));
			}
			else
			{
				// -----------------------------------------------------------------
				// 31/01/2014 ECU in normal mode so exclude development icons
				// 27/01/2015 ECU included the 'long press' argument
				// 09/03/2015 ECU include the validate option to check that the
				//                associated activity can run on this device
				// -----------------------------------------------------------------		
				if (gridImages [theIndex].mode && 
					gridImages [theIndex].Validate())
				{
					localGridItems.add (new GridItem (gridImages [theIndex].imageId,
							                          gridImages [theIndex].legend,
							                          gridImages [theIndex].longPress));
				}
			}
		}
		// -------------------------------------------------------------------------
		// 03/03/2015 return the array of generated items
		// -------------------------------------------------------------------------
		return localGridItems;
	}
	// =============================================================================
	void clickCounterReminder ()
	{
		// -------------------------------------------------------------------------
		// 12/07/2016 ECU created to check if a message needs to be displayed to
		//                indicate how many clicks are required to started an
		//                activity
		// -------------------------------------------------------------------------
		if (PublicData.storedData.clickCounter > 1)
		{
			// ---------------------------------------------------------------------
			// 12/07/2016 ECU more than one click so need a message
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (String.format(getString(R.string.click_reminder), 
					((PublicData.storedData.clickCounter == 2) ? "twice" 
															   : (PublicData.storedData.clickCounter + " times"))),true);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void displayGroupButtons ()
	{
		// -------------------------------------------------------------------------
		// 19/10/2016 ECU created to decide whether the buttons are to be displayed
		//                or not
		// --------------------------------------------------------------------------
		if (PublicData.storedData.groupListCurrent == 0)
			groupPreviousButton.setVisibility (View.INVISIBLE);
		else
			groupPreviousButton.setVisibility (View.VISIBLE);
		if (PublicData.storedData.groupListCurrent == (PublicData.storedData.groupLists.size() - 1))
			groupNextButton.setVisibility (View.INVISIBLE);
		else
			groupNextButton.setVisibility (View.VISIBLE);
		// --------------------------------------------------------------------------
	}
	/* ============================================================================= */
	void DisplayUserView (boolean theGridType)
	{
		// =========================================================================
		// 28/01/2014 ECU decide which user interface is to be used
		//                theGridType 	= false means a grid view
		//                         		= true  means a list view
		// 11/10/2014 ECU the following code used to be inline in the onCreate 
		//                method but was placed here so that it can be called by
		//                other methods, like onResume, if the display type needs
		//                to change
		// =========================================================================				
		if (!theGridType)
		{
			// =====================================================================
			// =====================================================================
			// 29/01/2014 ECU grid view wanted
			// =====================================================================
			// =====================================================================
			setContentView (R.layout.activity_grid);
			// ---------------------------------------------------------------------
			// 13/07/2016 ECU set up the notifications button even though it is
			//                initially 'GONE'
			// ---------------------------------------------------------------------
			notificationButton = (ImageButton) findViewById (R.id.notificationButton);
			// ---------------------------------------------------------------------
			gridView = (GridView) findViewById (R.id.gridView1);
			// ---------------------------------------------------------------------
			// 02/06/2013 ECU set up an adapter to handle the grid
			// ---------------------------------------------------------------------
			PublicData.imageAdapter = new ImageAdapter (this);
			gridView.setAdapter (PublicData.imageAdapter);			
			// ---------------------------------------------------------------------
			gridView.setOnItemClickListener (new OnItemClickListener() 
		    {
				@Override
				public void onItemClick (AdapterView<?> parent, 
										 View view,
										 int position, 
										 long id) 
		        {
					// -------------------------------------------------------------
					// 28/01/2014 ECU changed to have a separate method
					// 02/03/2014 ECU added the view as an argument
		            // -------------------------------------------------------------
		            SwitchOnImage (position,view);           
		        }
		    });
		    // ---------------------------------------------------------------------
			// 10/09/2013 ECU create the method for handling long click events
		    // ---------------------------------------------------------------------    
		    gridView.setOnItemLongClickListener(new OnItemLongClickListener() 
		    {
		    	@Override
		    	public boolean onItemLongClick (AdapterView<?> parent, 
		    								    View view,
		    								    int position, 
		    								    long id) 
		        {
		    		// ------------------------------------------------------------
		    		// 28/01/2014 ECU changed to use a self-contained method
		    		// 27/11/2014 ECU added 'view' as an argument
		    		// ------------------------------------------------------------        	
		            SwitchOnImageLong (position,view);
				    // ------------------------------------------------------------                       	
		            return true;
		        }
		    });
		    // ---------------------------------------------------------------------
		 	// 10/11/2014 ECU put in the listener to enable swipes to be detected
		 	// ---------------------------------------------------------------------
		 	gridView.setOnTouchListener(new OnTouchListener() 
		 	{
		 	    @Override
		 	    public boolean onTouch (View view, MotionEvent event) 
		 	    {
		 	    	// -------------------------------------------------------------
		 	    	// 25/01/2016 ECU added to correct a warning message
		 	    	// -------------------------------------------------------------
		 	    	view.performClick ();
		 	    	// -------------------------------------------------------------
		 		    return gestureDetector.onTouchEvent(event);
		 	    }				
		 	});   
		    // ---------------------------------------------------------------------
		    // 16/02/2014 ECU added the parameter for number of icons per row
		    // ---------------------------------------------------------------------
	        SetImageSizes (COLUMNS_DEFAULT);
	        // ---------------------------------------------------------------------
	        // 01/10/2014 ECU tell the user that zoom-ing is available
	        // ---------------------------------------------------------------------
	        Utilities.SpeakAPhrase (this,getString (R.string.zooming_available));
	        // ---------------------------------------------------------------------
	        // 03/03/2015 ECU build the items list from the images array
	        // ---------------------------------------------------------------------
	        gridItems = buildGridItemsList ();
	        // ---------------------------------------------------------------------
		}
		else
		{
			// =====================================================================
			// 29/01/2014 ECU list view wanted
			// 19/10/2016 ECU changed from 'activity_list'
			// =====================================================================						
			setContentView (R.layout.user_view_list);
			// ---------------------------------------------------------------------
			// 13/07/2016 ECU set up the notifications button even though it is
			//                initially 'GONE'
			// ---------------------------------------------------------------------
			notificationButton = (ImageButton) findViewById (R.id.notificationButton);
			// ---------------------------------------------------------------------
			listView = (ListView) findViewById (R.id.grid_list_view);
			// ---------------------------------------------------------------------		
			// 26/01/2014 ECU play around with a custom grid
			// 10/02/2014 ECU at this point 'activeImages' contains the images
			//                and will be sorted by usage if that option has been chosen
			// ---------------------------------------------------------------------
			// 11/10/2014 ECU reset the stored grid items otherwise it could just
			//                grow
			// 03/03/2015 ECU call the new method rather than having the code in-line
			//                here - the same code needed to be called in both forms
			//                of the user interface
			// ---------------------------------------------------------------------			
			gridItems = buildGridItemsList ();
			// ---------------------------------------------------------------------
			// 08/02/2014 ECU create a custom adapter with the required arguments
			// ---------------------------------------------------------------------
			PublicData.customGridViewAdapter 
					= new CustomGridViewAdapter (this, R.layout.grid_cell,gridItems);
					
			listView.setAdapter (PublicData.customGridViewAdapter);
			// ---------------------------------------------------------------------		
			listView.setOnItemClickListener (new OnItemClickListener() 
		    {
				@Override
				public void onItemClick (AdapterView<?> parent, 
										 View view,
										 int position, 
										 long id) 
		        {
					// -------------------------------------------------------------
					// 28/01/2014 ECU changed to have a separate method
				    // -------------------------------------------------------------
		            SwitchOnImage (position,view);   
		            // -------------------------------------------------------------
		        }
		    });
			// ---------------------------------------------------------------------
			// 10/09/2013 ECU create the method for handling long click events
			// ---------------------------------------------------------------------	        
			listView.setOnItemLongClickListener (new OnItemLongClickListener() 
		    {
		    	@Override
		    	public boolean onItemLongClick (AdapterView<?> parent, 
		    									View view,
		    									int position, 
		    									long id) 
		        {
		    		// -------------------------------------------------------------
		    		// 28/09/2014 ECU changed to use a self-contained method
		    		// 27/11/2014 ECU added view as the argument
				    // -------------------------------------------------------------
		            SwitchOnImageLong (position,view);
				    // -------------------------------------------------------------                      	
		            return true;
		        }
		    });
			// ---------------------------------------------------------------------
			// 09/11/2014 ECU put in the listener to enable swipes to be detected
			// ---------------------------------------------------------------------
			listView.setOnTouchListener (new OnTouchListener() 
			{
			    @Override
			    public boolean onTouch (View view, MotionEvent event) 
			    {
			    	// -------------------------------------------------------------
		 	    	// 25/01/2016 ECU added to correct a warning message
		 	    	// -------------------------------------------------------------
		 	    	view.performClick ();
		 	    	// -------------------------------------------------------------
				    return gestureDetector.onTouchEvent (event);
				    // -------------------------------------------------------------
			    }				
			});
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 18/10/2016 ECU get the layout of the group details
		// 19/10/2016 ECU moved here from within the 'list' option because want
		//                it to always be applicable
		// -------------------------------------------------------------------------
		groupLayout = (RelativeLayout) findViewById (R.id.group_layout);
		// -------------------------------------------------------------------------
		// 18/10/2016 ECU check if the layout is to be visible or not
		// -------------------------------------------------------------------------
		if (groupLayout != null)
		{
			if (PublicData.storedData.groupActivities)
			{
				groupLayout.setVisibility (View.VISIBLE);
				// -----------------------------------------------------------------
				TextView groupTitle = (TextView) findViewById (R.id.group_title);
				groupTitle.setText (PublicData.storedData.groupLists.get(PublicData.storedData.groupListCurrent).groupListName);
				// -----------------------------------------------------------------
				// 19/10/2016 ECU set the buttons
				// ------------------------------------------------------------------
				groupNextButton 	= (ImageButton) findViewById (R.id.group_next_button);
				groupPreviousButton = (ImageButton) findViewById (R.id.group_previous_button);
				// ------------------------------------------------------------------
				groupNextButton.setOnClickListener (nextGroupButtonOnClickListener);
				groupPreviousButton.setOnClickListener (previousGroupButtonOnClickListener);
				// -----------------------------------------------------------------
				groupTitle.setOnLongClickListener (new View.OnLongClickListener() 
				{
					@Override
					public boolean onLongClick (View theView) 
					{
						// ---------------------------------------------------------
                		// 08/10/2016 ECU start the settings activity
                		// ---------------------------------------------------------
                		localIntent = new Intent (context,SettingsActivity.class);
            			startActivityForResult (localIntent,0);
						// ---------------------------------------------------------
						return true;
					}
				});
				// -----------------------------------------------------------------
				// 19/10/2016 ECU check whether the buttons are to be displayed
				// ------------------------------------------------------------------
				displayGroupButtons ();
				// ------------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 19/10/2016 ECU hide the group layout
				// -----------------------------------------------------------------
				groupLayout.setVisibility (View.GONE);	
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	 void SetImageSizes (int theNumberOfIconsPerRow)
	 {
		 // ------------------------------------------------------------------------
		 // 15/01/2014 ECU just tried to tidy the code up
		 // 16/02/2014 ECU rewrite the code to not be switchable on screen size
		 // ------------------------------------------------------------------------
		 iconSize = PublicData.screenWidth / theNumberOfIconsPerRow;
		 // ------------------------------------------------------------------------
		 // 19/09/2013 ECU set the column width
	     // ------------------------------------------------------------------------   
		 gridView.setColumnWidth (iconSize);
		 // ------------------------------------------------------------------------
		 // 16/02/2014 ECU make sure that the number of columns is correct
		 // ------------------------------------------------------------------------
		 gridView.setNumColumns (theNumberOfIconsPerRow);
	     // ------------------------------------------------------------------------           
	 }
	 @SuppressWarnings("rawtypes")
	// =============================================================================
	 Integer [][] GetImages (boolean theMode)
	 {
		 // ------------------------------------------------------------------------
		 // 19/01/2014 ECU do a quick scan to see how many elements I need
		 // 09/03/2015 ECU added the check of validity of icons
		 // ------------------------------------------------------------------------
		 // 19/01/2014 ECU if theMode is true then the whole array is used
		 // 18/01/2015 ECU the array will contain the 'resource id' of the image
		 //                - NOT its index in the array
		 //            ECU changed to work with a 2d array
		 // ------------------------------------------------------------------------
		 // 18/01/2015 ECU check for sort mode
		 // ------------------------------------------------------------------------
		 if (PublicData.storedData.sortByUsage)
		 {
			 Arrays.sort (gridImages);	
		 }
		 // ------------------------------------------------------------------------
		 // 26/04/2015 ECU check if the grid images are to be sorted by the legend
		 // 17/09/2015 ECU changed to 'ignore the case'
		 // ------------------------------------------------------------------------
		 if (PublicData.storedData.sortByLegend)
		 {
			 Arrays.sort(gridImages,new Comparator<GridImages>() 
			 {
				 @Override
				 public int compare (GridImages gridImage0, GridImages gridImage1) 
				 {		
					 return gridImage0.legend.compareToIgnoreCase (gridImage1.legend);   	
				 }
			 });
		 } 
		 // ------------------------------------------------------------------------
		 // 09/03/2015 ECU add a list to hold required information
		 // ------------------------------------------------------------------------
		 ArrayList <Integer> resultsList = new ArrayList <Integer> ();
		 // ------------------------------------------------------------------------
		 // 09/03/2015 ECU loop for all entries in 'gridImages'
		 // ------------------------------------------------------------------------
		 for (int theIndex = 0; theIndex < gridImages.length; theIndex++)
		 {
			 // --------------------------------------------------------------------
			 // 09/03/2015 ECU if in 'development mode' then include everything
			 // --------------------------------------------------------------------
			 if (theMode)
			 {
				 // ----------------------------------------------------------------
				 // 09/03/2015 ECU development mode
				 // ----------------------------------------------------------------
				 resultsList.add (theIndex);
			 }
			 else
			 {
				 // ----------------------------------------------------------------
				 // 09/03/2015 ECU production mode
				 //            ECU added check on whether the activity is valid on
				 //                this device
				 // ----------------------------------------------------------------
				 if (gridImages [theIndex].mode == !theMode && gridImages [theIndex].Validate())
					 resultsList.add (theIndex);
			 }
		 }
		 // ------------------------------------------------------------------------
		 // 09/03/2015 ECU size the results array
		 // ------------------------------------------------------------------------
		 Integer [][] resultsArray = new Integer [2][resultsList.size()];
		 // ------------------------------------------------------------------------
		 // 09/03/2015 ECU build the results from the stored information
		 // ------------------------------------------------------------------------
		 for (int theIndex=0; theIndex < resultsList.size(); theIndex++)
		 {
			 resultsArray [IMAGE_INDEX][theIndex] 	 = gridImages [resultsList.get(theIndex)].imageId;
			 resultsArray [POSITION_INDEX][theIndex] = resultsList.get(theIndex);	 
		 }
		 // ------------------------------------------------------------------------	 
		 // 10/02/2014 ECU check if need to build the cell usage list
		 // 18/01/2015 ECU removed cellUsage code
		 // ------------------------------------------------------------------------   
		 // ------------------------------------------------------------------------
		 // 10/02/2014 ECU try and sort the images by usage if require
		 // 18/01/2015 ECU removed the cell usage code
		 // ------------------------------------------------------------------------
		 // ------------------------------------------------------------------------	
		 return resultsArray;	 	 
	 }
	 // ============================================================================
	 void startTheActivity (Intent theIntent)
	 {
		 // ------------------------------------------------------------------------
		 // 06/02/2015 ECU indicate that an activity has been selected
		 // ------------------------------------------------------------------------
		 startTheActivity (theIntent,StaticData.NO_RESULT);
	 }
	 // ----------------------------------------------------------------------------
	 void startTheActivity (Intent theIntent,int theResultCode)
	 {
		 // ------------------------------------------------------------------------
		 // 30/11/2014 ECU indicate that an activity has been selected
		 // ------------------------------------------------------------------------
		 activityStarted = true;
		 // ------------------------------------------------------------------------
		 // 28/11/2014 ECU starts the specified intent or just returns the intent
		 //                is requested
		 // ------------------------------------------------------------------------
		 if (actionActivity)
		 {
			 // --------------------------------------------------------------------
			 // 28/11/2014 ECU start the activity
			 // 06/02/2015 ECU put in the check on NO_RESULT
			 // --------------------------------------------------------------------
			 if (theResultCode == StaticData.NO_RESULT)
				 startActivity (theIntent);
			 else
				 startActivityForResult (theIntent,theResultCode);
		 }
		 else
		 {
			 // --------------------------------------------------------------------
			 // 28/11/2014 ECU just return the intent to be started
			 // 06/02/2015 ECU pass back the position that was selected
			 // --------------------------------------------------------------------
			 theIntent.putExtra(StaticData.PARAMETER_POSITION,activeImagePosition);
			 setResult (RESULT_OK,theIntent);	
			 finish ();
			 // --------------------------------------------------------------------
		 }
		
		 // ------------------------------------------------------------------------
	 }
	// =============================================================================
	// =============================================================================
	// P R E S S   O N   I M A G E
	// =============================================================================
	// =============================================================================
	void SwitchOnImage (int thePosition,View theView)
	{
		 // ------------------------------------------------------------------------
		 // 02/03/2014 ECU added the view as an argument
		 // 14/01/2015 ECU rearrange to be alphabetical
		 // ------------------------------------------------------------------------
		 // 25/01/2016 ECU check if the number of clicks has been actioned
		 //            ECU only if the limit is greater than 1 (on a new device when
		 //				   'storedData' has not been initialised then the limit will
		 //                be 0
		 // 06/03/2016 ECU put in the check on position... because when the Timer
		 //				   wants to start an activity then the click counter should
		 //                not be taken into account
		 // ------------------------------------------------------------------------
		 if (PublicData.storedData.clickCounter > 1 && (positionToAction == StaticData.NO_RESULT))
		 {
			 if ((lastPosition == StaticData.NO_RESULT) || (thePosition == lastPosition))
			 {
				 // ----------------------------------------------------------------
				 // 25/01/2016 ECU save the last position - overkill if already set 
				 //                but needed for the NO_RESULT
				 // ----------------------------------------------------------------
				 lastPosition = thePosition;
				 // ----------------------------------------------------------------
				 // 25/01/2016 ECU have clicked on the same button as last time
				 // ----------------------------------------------------------------
				 clickCounter++;
			 }
			 else
			 {
				 // ----------------------------------------------------------------
				 // 25/01/2016 ECU the current click position is different to the 
				 //                last one
				 // ----------------------------------------------------------------
				 lastPosition = thePosition;
				 // ----------------------------------------------------------------
				 // 25/01/2016 ECU reset the click counter
				 // ----------------------------------------------------------------
				 clickCounter = 1;
				 // ----------------------------------------------------------------
			 }
			 // --------------------------------------------------------------------
			 // 25/01/2016 ECU decide whether the click can be processed
			 // 19/08/2016 ECU put in check on actionActivity because when false
			 //                then do not worry about the clickCounter
			 // --------------------------------------------------------------------
			 if ((clickCounter < PublicData.storedData.clickCounter) && actionActivity) return;
			 // --------------------------------------------------------------------
		 }
		 // ------------------------------------------------------------------------
		 // 21/10/2015 ECU remember the position that is being actioned
		 // 24/10/2015 ECU changed to use the method
		 // ------------------------------------------------------------------------
		 PublicData.storedData.setLastActivity (thePosition);
		 // ------------------------------------------------------------------------
		 // 06/02/2015 ECU remember the position which may be passed back to the
		 //                TimerActivity
		 // ------------------------------------------------------------------------
		 activeImagePosition = thePosition;
		 // ------------------------------------------------------------------------
		 // 30/12/2014 ECU check if need to log statistics
		 // ------------------------------------------------------------------------
		 if (PublicData.storedData.acquireStatistics)
		 {
			 Utilities.LogToProjectFile (TAG,gridImages [thePosition].legend);
		 }
		 // ------------------------------------------------------------------------
		 // 28/04/2016 ECU handle any monitoring that has been enabled
		 // ------------------------------------------------------------------------
		 MonitorData.sendMonitorData (context,StaticData.MONITOR_DATA_ACTIVITY,thePosition);
		 // ------------------------------------------------------------------------ 
		 // 10/02/2014 ECU update the cell usage
		 // 18/01/2015 ECU changed to use the gridImages rather than cellUsage
		 // ------------------------------------------------------------------------
		 gridImages [activeImages [POSITION_INDEX][thePosition]].usage++;
		 // ------------------------------------------------------------------------
		 // 20/10/2014 ECU initialise the activity counter which can be used by
		 //                activities that may need to restart themselves
		 // ------------------------------------------------------------------------
		 activityCounter = 0;
		 // ------------------------------------------------------------------------
		 // 20/02/2014 ECU reset the 'back key' counter
		 // ------------------------------------------------------------------------
		 backKeyCounter = BACK_KEY_COUNTER;
		 // ------------------------------------------------------------------------
		 // 06/10/2016 ECU check on grouping of activities
		 // ------------------------------------------------------------------------
		 if (groupActivities)
		 {
			 // --------------------------------------------------------------------
			 // 06/10/2016 ECU grouping of activities is required
			 // --------------------------------------------------------------------
			 Message localMessage = GroupActivity.groupMessageHandler.obtainMessage(StaticData.MESSAGE_DATA);
			 localMessage.arg1 = activeImages [IMAGE_INDEX][thePosition];
			 GroupActivity.groupMessageHandler.sendMessage (localMessage);
			 // --------------------------------------------------------------------
			 // 06/10/2016 ECU just return because no action is required
			 // --------------------------------------------------------------------
			 return;
			 // --------------------------------------------------------------------
		 }
		 // ------------------------------------------------------------------------
         // 19/01/2014 ECU change to pick up the id of the chosen image
         //                rather than the position
         // ------------------------------------------------------------------------
		 switch (activeImages [IMAGE_INDEX][thePosition])
		 {	
		 	// =====================================================================
   			case R.drawable.audio_analyser:
   				// -----------------------------------------------------------------
   				// 18/01/2014 ECU added - audio analyser
   				// -----------------------------------------------------------------
   				localIntent = new Intent (context,AudioAnalyser.class);
   	    		startTheActivity (localIntent,0);
   	    		break;
   	    	// =====================================================================
   			case R.drawable.audio_streaming:
   				// -----------------------------------------------------------------
   				// 05/08/2013 ECU added - audio streaming
   				// -----------------------------------------------------------------
   				localIntent = new Intent (context,AudioStreamActivity.class);
   				startTheActivity (localIntent,0);
   				break;
   			// =====================================================================
   			case R.drawable.barcode:
   				// -----------------------------------------------------------------
   				// 30/08/2013 ECU added - bar code handling
   				//            ECU this will use the app developed by
   				//                the zxing (Zebra Crossing) project
   				// 07/02/2014 ECU change to use general bar code activity
   				// -----------------------------------------------------------------	
   				localIntent = new Intent (context,BarCodeActivity.class);
   				startTheActivity (localIntent,0);
   				break;
   			// =====================================================================
   			case R.drawable.bluetooth:
   				// -----------------------------------------------------------------
   				// 29/06/2013 ECU added
   				// -----------------------------------------------------------------
   				localIntent = new Intent (context,BlueToothActivity.class);
   				startTheActivity (localIntent,0);
   				break;
   			// =====================================================================
      		case R.drawable.bouncing_ball:
      			localIntent = new Intent (context,GameTwo.class);
  				startTheActivity (localIntent,0);
  				break;
  			// =====================================================================
      		case R.drawable.carer:
      			// -----------------------------------------------------------------
      			// 09/01/2014 ECU added
      			// 12/01/2014 ECU changed to CarerSystem
      			// 28/11/2016 ECU this used to be the shifted activation
      			// -----------------------------------------------------------------
  				localIntent = new Intent (context,CarerActivity.class);
  				localIntent.putExtra (StaticData.PARAMETER_CARER_VISIT,true);
  				startTheActivity (localIntent,0);
      			// -----------------------------------------------------------------
  				break;
  			// =====================================================================
      		case  R.drawable.compass_small:
      			localIntent = new Intent (context,CompassActivity.class);
      			startTheActivity (localIntent,0);
      			break;
      		// =====================================================================
      		case  R.drawable.daily_summary:
      			// -----------------------------------------------------------------
      			// 27/02/2017 ECU created to show a summary of tasks for the
      			//                selected day
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,DailySummaryActivity.class);
      			startTheActivity (localIntent,0);
      			// -----------------------------------------------------------------
      			break;
      		// =====================================================================
      		case R.drawable.database:
      			// -----------------------------------------------------------------
      			// 15/11/2015 ECU added
      			// 27/01/2016 ECU changed to use the embedded app
      			// -----------------------------------------------------------------
      			try
      			{
      				// -------------------------------------------------------------
      				localIntent = Utilities.getPackageIntent (context,StaticData.PACKAGE_TYPE_CONTACTS);
      				if (localIntent != null)
      					startTheActivity(localIntent);
      				else
      				{
      					// ---------------------------------------------------------
      					// 27/01/2016 ECU let the user select the package
      					// ---------------------------------------------------------
      					Utilities.PickAPackage (this,StaticData.PACKAGE_TYPE_CONTACTS);
      					// ---------------------------------------------------------
      				}
      			}
      			catch (Exception theException)
      			{
      				Utilities.popToast (TAG  + ":" + theException);
      			}
  				// -----------------------------------------------------------------
  				break;
      		// =====================================================================
      		case R.drawable.dialogue:
      			// -----------------------------------------------------------------
      			// 20/11/2013 ECU added - start a dialogue
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,Dialogue.class);
      			startTheActivity (localIntent,0);
      			break;	
      		// =====================================================================
      		case R.drawable.disk:
      			localIntent = new Intent (context,ClonerActivity.class); 
     			startTheActivity (localIntent,0);
     			setResult (StaticData.RESULT_CODE_FINISH);
  				break;
  			// =====================================================================
      		case R.drawable.doctor:
      		case R.drawable.hospital:
      			// -----------------------------------------------------------------
      			// 06/01/2014 ECU added AppointsActivity
      			// 10/11/2014 ECU changed to use variable PARAMETER.....
      			// 19/12/2015 ECU combine the cases and tailor using the parameter
      			// 29/11/2016 ECU supply APPOINTMENTS parameter
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,AppointmentsActivity.class);
      			localIntent.putExtra(StaticData.PARAMETER_SELECT,
      				(activeImages [IMAGE_INDEX][thePosition] == R.drawable.doctor) ? StaticData.PARAMETER_DOCTOR
      																			   : StaticData.PARAMETER_HOSPITAL);
      			localIntent.putExtra(StaticData.PARAMETER_APPOINTMENTS,true);
      			startTheActivity (localIntent,0);
      			// -----------------------------------------------------------------
  				break;
  			// =====================================================================
      		case R.drawable.documents:
      			// -----------------------------------------------------------------
      			// 18/10/2016 ECU created to handle essential documents
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,DocumentsActivity.class);
  				startTheActivity (localIntent,0);
  				// -----------------------------------------------------------------
      			break;
  			// =====================================================================
      		case R.drawable.drawer:
      			// -----------------------------------------------------------------
      			// 13/01/2015 ECU created to test navigation drawer
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,NavigationDrawerActivity.class);
      			startTheActivity (localIntent,0);
      			// -----------------------------------------------------------------
      			break;
      		// =====================================================================
      		case R.drawable.exercise:
      			// -----------------------------------------------------------------
      			// 21/03/2014 ECU added call to the exercise activity
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,ExerciseActivity.class);
  				startTheActivity (localIntent,0);
      			break;
      		// =====================================================================
      		case R.drawable.falling:
      			localIntent = new Intent (context,FallsActivity.class);
      			startTheActivity (localIntent,0);
      			break;
      		// =====================================================================
      		case R.drawable.files:
      			// -----------------------------------------------------------------
      			// 05/12/2013 ECU added - file choosing
      			// 06/12/2013 ECU changed to use FILE_REQUEST_CODE
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,FileChooser.class);
      			// -----------------------------------------------------------------
      			// 09/12/2013 ECU pass through any parameters
      			// 22/02/2014 ECU change to use PARAMETER_
      			// -----------------------------------------------------------------
      			localIntent.putExtra(StaticData.PARAMETER_FOLDER,PublicData.projectFolder);
      			startTheActivity (localIntent,StaticData.REQUEST_CODE_FILE);
      			break;
      		// =====================================================================
      		case R.drawable.gameone:
      			localIntent = new Intent (context,GameOne.class);
  				startTheActivity (localIntent,0);
  				break;
  			// =====================================================================
      		case R.drawable.groups:
      			localIntent = new Intent (context,GroupActivity.class);
      			startTheActivity (localIntent,StaticData.REQUEST_CODE_FINISH);
      			break;
      		// =====================================================================
      		case R.drawable.internet:
      			localIntent = new Intent (context,DisplayURL.class);
      			startTheActivity (localIntent,0);
      			break;
      		// =====================================================================
      		case R.drawable.liquid:
      			// -----------------------------------------------------------------
      			// 18/05/2016 ECU create to handle liquid selection
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,LiquidActivity.class);
      	    	startTheActivity (localIntent,0);
      	    	// -----------------------------------------------------------------
      			break;
      		// =====================================================================
      		case R.drawable.location:
      			// -----------------------------------------------------------------
      			// 03/01/2014 ECU added - location and tracking
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,LocationActivity.class);
      	    	startTheActivity (localIntent,0);
      	    	break;
      	    // =====================================================================
      		case R.drawable.mail:
      			// -----------------------------------------------------------------
      			// 09/06/2013 ECU added the call to the mail app
      			// -----------------------------------------------------------------
      			try
      			{
      				// -------------------------------------------------------------
      				// 08/11/2013 ECU try and access the htc mail application
      				// 09/11/2013 ECU use the method in Utilities
      				// 06/02/2015 ECU changed to startTheActivity
      				// -------------------------------------------------------------
      				localIntent = Utilities.getPackageIntent (context,StaticData.PACKAGE_TYPE_MAIL);
      				if (localIntent != null)
      					startTheActivity(localIntent);
      				else
      				{
      					// ---------------------------------------------------------
      					// 27/01/2016 ECU let the user select the package
      					// ---------------------------------------------------------
      					Utilities.PickAPackage (this,StaticData.PACKAGE_TYPE_MAIL);
      					// ---------------------------------------------------------
      				}
      			}
      			catch (Exception theException)
      			{
      				Utilities.popToast (TAG  + ":" + theException);
      			}
      			break;
      		// =====================================================================
      		case R.drawable.medication:
      			// -----------------------------------------------------------------
      			// 24/03/2014 ECU give the option to display existing
      			//                medication
      			// -----------------------------------------------------------------
      			if (PublicData.medicationDetails == null ||
      			    PublicData.medicationDetails.size() == 0)
      			{
      				// -------------------------------------------------------------
      				// 24/03/2014 ECU no medication is registered yet
      				// -------------------------------------------------------------
      				localIntent = new Intent (context,MedicationInput.class);
      				startTheActivity (localIntent,0);
      				// --------------------------------------------------------------
      			}
      			else
      			{
      				// -------------------------------------------------------------
      				// 24/03/2014 ECU medication details already exist
      				//            ECU indicate that the user will be allowed
      				//                to use the 'back key'
      				// 25/03/2014 ECU pass through the definition of the
      				//                method that will be actioned when
      				//                an item has been selected
      				// 29/03/2014 ECU changed to use _OBJECT_TYPE
      				// 13/12/2015 ECU added the swipe method
      				//            ECU indicate that I want to sort the mediation
      				// -------------------------------------------------------------
      				localIntent = new Intent (context,Selector.class);
      				localIntent.putExtra (StaticData.PARAMETER_OBJECT_TYPE,StaticData.OBJECT_MEDICATION);
      				localIntent.putExtra (StaticData.PARAMETER_BACK_KEY,true);
      				localIntent.putExtra (StaticData.PARAMETER_METHOD,new MethodDefinition<Utilities> 
      											(Utilities.class,"DoseSelect"));
      				// -------------------------------------------------------------
      				// 13/12/2015 ECU define the swipe method
      				// -------------------------------------------------------------
      				localIntent.putExtra (StaticData.PARAMETER_SWIPE_METHOD,new MethodDefinition<MedicationInput> 
						(MedicationInput.class,"SwipeHandler"));
      				// -------------------------------------------------------------
      				// 13/12/2015 ECU indicate that the list is to be sorted
      				// -------------------------------------------------------------
      				localIntent.putExtra(StaticData.PARAMETER_SORT,true);
      				// -------------------------------------------------------------
      				startTheActivity (localIntent,0);
      				// -------------------------------------------------------------
      			}
  				break;   
  			// =====================================================================
      		case R.drawable.microphone:
      			localIntent = new Intent (context,AudioRecorder.class);
  				startTheActivity (localIntent,0);
      			break;
      		// =====================================================================
      		case R.drawable.music:
      			// -----------------------------------------------------------------
      			// M U S I C   P L A Y E R
      			// =======================
      			// 31/05/2013 ECU start the music player
      			// 12/04/2015 ECU if a track is being played as a result of a
      			//                remote request then do not allow the command - just
      			//                inform the user
      			// -----------------------------------------------------------------
      			if (!PublicData.trackBeingPlayed)
      			{
      				// -------------------------------------------------------------
      				// 12/04/2015 ECU remote track not being played
      				//            ECU can start the music player
      				// -------------------------------------------------------------
      				localIntent = new Intent (context,MusicPlayer.class);
      				// -------------------------------------------------------------	
      				// 02/06/2013 ECU included the flags to try and resume an activity 
      				//                rather than start a new one
      				// -------------------------------------------------------------
      				localIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP); 
      				startTheActivity (localIntent);
      				// -------------------------------------------------------------
      			}
      			else
      			{
      				// -------------------------------------------------------------
      				// 12/04/2015 ECU inform user that cannot start the music
      				//                player
      				// --------------------------------------------------------------
      				Utilities.popToast (getString (R.string.unable_to_start_music_player),true);
      				// --------------------------------------------------------------
      			}
      			break;
      		// =====================================================================
      		case  R.drawable.named_actions:
      			// -----------------------------------------------------------------
      			// N A M E D  A C T I O N S
      			// ========================
      			// 03/08/2016 ECU start the named actions activity
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,NamedActionsActivity.class);
  				startTheActivity (localIntent,0);
  				break;
      		// =====================================================================
      		case R.drawable.nfc:
      			// -----------------------------------------------------------------
      			// N F C
      			// =====
      			// 12/06/2016 ECU NFC Tag handling
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,NFC_Activity.class);
      			localIntent.putExtra (StaticData.PARAMETER_RESTART,true);
  				startTheActivity (localIntent,0);
  				break;
      		// =====================================================================
      		case  R.drawable.panic_alarm:
      			// -----------------------------------------------------------------
      			// P A N I C  A L A R M
      			// ====================
      			// 25/11/2015 ECU start the panic alarm activity
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,PanicAlarmActivity.class);
  				startTheActivity (localIntent,0);
  				break;
  			// =====================================================================
      		case  R.drawable.photoalbum:
      			// -----------------------------------------------------------------
      			// S L I D E   S H O W
      			// ===================
      			// 25/06/2013 ECU start the music player
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,SlideShowActivity.class);
  				startTheActivity (localIntent,0);
  				break;
  			// =====================================================================
      		case R.drawable.radar:
      			// -----------------------------------------------------------------
      			// 02/12/2014 ECU created to test radar graphics
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,RadarActivity.class);
      			startTheActivity (localIntent,0);
      			break;
      		// =====================================================================
      		case R.drawable.screen_capture:
      			// -----------------------------------------------------------------
      			// S C R E E N  C A P T U R E
      			// ==========================
      			// 13/04/2015 ECU created to handle screen capture
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,ScreenCaptureActivity.class);
      			startTheActivity (localIntent,0);
      			// -----------------------------------------------------------------
      			break;
      		// =====================================================================
      		case R.drawable.settings:
      			// -----------------------------------------------------------------
      			// 04/01/2014 ECU added - settings
      			// 03/10/2014 ECU changed to use correct request code
      			// -----------------------------------------------------------------
      			if (PublicData.storedData.developmentMode)
      			{
      				// -------------------------------------------------------------
      				// 03/03/2016 ECU Note - in development mode so can start immediately
      				// --------------------------------------------------------------
      				localIntent = new Intent (context,SettingsActivity.class);
      				startTheActivity (localIntent,StaticData.REQUEST_CODE_SETTINGS);
      			}
      			else
      			{
      				// -------------------------------------------------------------
      				// 03/03/2016 ECU not in development mode so request the password
      				//                before allowing the settings activity to start
      				// --------------------------------------------------------------
      				DialogueUtilities.textInput (context,
							 context.getString (R.string.enter_password_title),
							 context.getString (R.string.enter_password_summary),
							 StaticData.HINT +  context.getString (R.string.enter_password_hint),
							 Utilities.createAMethod (GridActivity.class,"SettingsPassword",""),
							 null,
							 InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
      				// -------------------------------------------------------------
      			}
      	    	break;
      	    // =====================================================================
      		case R.drawable.shopping:
      			// -----------------------------------------------------------------
      			// 29/03/2014 ECU added - shopping
      			// -----------------------------------------------------------------			
      			localIntent = new Intent (context,ShoppingActivity.class);
      	    	startTheActivity (localIntent,0);
      	    	// -----------------------------------------------------------------
      	    	break;
      	    // =====================================================================
      		case R.drawable.speaking_clock:
      			// -----------------------------------------------------------------
      			// 09/02/2014 ECU added - speaking clock
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,SpeakingClockActivity.class);
      	    	startTheActivity (localIntent,0);
      	    	break;
      	    // =====================================================================
      		case R.drawable.swipe:
      			// -----------------------------------------------------------------
      			// 21/04/2014 ECU created to test swipe screens
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,SwipeActivity.class);
      			startTheActivity (localIntent,0);
      			break;
      		// =====================================================================
      		case R.drawable.system_information:
      			// -----------------------------------------------------------------
      			// 16/06/2013 ECU added
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,SystemInfoActivity.class);
      			startTheActivity (localIntent,0);
      			break;
      		// =====================================================================
      		case  R.drawable.tcp:
      			// -----------------------------------------------------------------
      			// 30/06/2013 ECU added
      			// 17/10/2014 ECU change request code so that can check if
      			//                this activity needs to reboot on completion
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,TCPActivity.class);
      			startTheActivity (localIntent,StaticData.REQUEST_CODE_FINISH);
      			break;
      		// =====================================================================
      		case R.drawable.television:
      			// -----------------------------------------------------------------
      			// 16/12/2013 ECU added - remote control of devices
      			// 21/12/2013 ECU this activity requires an API level >= 12
      			//                so check at this point - when bluetooth is
      			//                used then it can be removed
      			// 31/12/2013 ECU took out the checks on API level since bluetooth
      			//                seems OK for all of the devices
      			// 02/03/2014 ECU only activate the class if the underlying
      			//                bluetooth service appears to be working
      			//                correctly
      			// 19/04/2014 ECU put in the ability to run the activity even
      			//                if there is no hardware
      			// 27/02/2016 ECU inlcude the check on the remote controller server
      			// -----------------------------------------------------------------
      			if (PublicData.blueToothService || 
      				PublicData.storedData.remoteAlways ||
      				(PublicData.remoteControllerServer != null))
      			{
      				localIntent = new Intent (context,Television.class);
      				startTheActivity (localIntent,0);
      			}
      			else
      			{
      				// -------------------------------------------------------------
      				// 02/03/2014 ECU added the view to get a pop up window
      				// 18/03/2014 ECU add the legend for the button
      				// 08/04/2014 ECU use resources
      				// 31/05/2016 ECU added the 'false' to indicate no scrolling
      				// -------------------------------------------------------------
      				Utilities.popToast (theView,
      									getString (R.string.no_remote_controller),
      									getString (R.string.press_to_clear),
      									60000,
      									false);
      				// -------------------------------------------------------------
      			}
      	    	
      			break;
      		// =====================================================================
      		case R.drawable.test:
      			// -----------------------------------------------------------------
      			// 02/08/2013 ECU added - testing android
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,TestActivity.class);
      			startTheActivity (localIntent,0);
      			break;
      		// =====================================================================
      		case R.drawable.timer:
      			// -----------------------------------------------------------------
      			// 07/02/2015 ECU check if any timers already exist and handle
      			//                appropriately
      			// -----------------------------------------------------------------
      			if (PublicData.alarmData == null ||
  			    		PublicData.alarmData.size() == 0)
      			{
      				// -------------------------------------------------------------
      				// 24/03/2014 ECU no timer has been set yet
      				// -------------------------------------------------------------
      				localIntent = new Intent (context,TimerActivity.class);
  					startTheActivity (localIntent,0);
  					// --------------------------------------------------------------
      			}
      			else
      			{
      				// -------------------------------------------------------------
      				// 07/02/2015 ECU timers exist so show the list
      				// -------------------------------------------------------------
      				Intent localIntent = new Intent (context,Selector.class);
      				localIntent.putExtra (StaticData.PARAMETER_OBJECT_TYPE,StaticData.OBJECT_TIMER);
      				localIntent.putExtra (StaticData.PARAMETER_BACK_KEY,true);
      				localIntent.putExtra (StaticData.PARAMETER_METHOD,new MethodDefinition<TimerActivity> 
      											(TimerActivity.class,"AlarmSelect"));
      				startTheActivity (localIntent,0);
      				// -------------------------------------------------------------
      			}
  				break;
  			// =====================================================================
      		case  R.drawable.tone_generator:
      			// -----------------------------------------------------------------
      			// 15/08/2015 ECU added
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,ToneGeneratorActivity.class);
      			startTheActivity (localIntent,StaticData.REQUEST_CODE_FINISH);
      			break;
  			// =====================================================================
      		case R.drawable.torch:
      			// -----------------------------------------------------------------
      			// 13/06/2013 ECU try and toggle the torch
      			// 08/02/2015 ECU changed to use a direct call to the method
      			// 23/05/2015 ECU added the context as an argument
      			// 29/12/2016 ECU changed from Utilities.
      			// -----------------------------------------------------------------
  				FlashLight.flashLightToggle (context);
  				// -----------------------------------------------------------------
  				break;
  			// =====================================================================
      		case R.drawable.tv_guide:
      			// -----------------------------------------------------------------
      			// 16/09/2015 ECU display the TV channels guide
      			// 22/09/2015 ECU changed to ShowEPGActivity - TVChannelsActivity
      			//                is a shift operation
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,ShowEPGActivity.class);
      			startTheActivity (localIntent,0);
  				break;
  			// =====================================================================
      		case R.drawable.upnp:
      			// -----------------------------------------------------------------
      			// 08/09/2016 ECU WeMo control using CyberGarage directly - not
      			//                using Belkin's SDK
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,UPnP_Activity.class);
      			startTheActivity (localIntent,0);
  				break;
  			// =====================================================================
      		case R.drawable.video:
      			// -----------------------------------------------------------------
      			// 11/09/2015 ECU seemed to have problems with the latest video
      			//                recorder when run in really old versions of Android so
      			//                included the 'old' version for use on them - NOT happy
      			//                with this but just wanted to get things working
      			//                until further investigation can take place
      			// -----------------------------------------------------------------
      			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) 
      			{
      				localIntent = new Intent (context,VideoRecorder.class);
      			}
      			else
      			{
      				localIntent = new Intent (context,VideoRecorderOld.class);
      			}
      			// -----------------------------------------------------------------
  				startTheActivity (localIntent,0);
      			break;	
      		// =====================================================================
      		case R.drawable.voice_recognition:
      			// -----------------------------------------------------------------
      			// 16/06/2013 ECU added the network test
      			// -----------------------------------------------------------------
      			if (Utilities.checkForNetwork(context))
      			{
      				// -------------------------------------------------------------
      				// 15/06/2013 ECU added voice recognition
      				// -------------------------------------------------------------
      				localIntent = new Intent (context,VoiceRecognition.class);
      				startTheActivity (localIntent,0);
      				// -------------------------------------------------------------
      			}
      			else
      			{
      				// -------------------------------------------------------------
      				// 10/09/2013 ECU change message to indicate use of 
      				//                long click
      				// 11/09/2013 ECU remove getBaseContext as an argument
      				//                so as to get custom toast
      				// 08/04/2014 ECU changed to use resources
      				// -------------------------------------------------------------	
      				Utilities.popToast (getString(R.string.no_available_network) + "\n\n" +
      									getString(R.string.long_press_for_voice_recognition));
      				// --------------------------------------------------------------
      			}
      			break;
      		// =====================================================================
      		case R.drawable.wemo:
      			// -----------------------------------------------------------------
      			// 16/02/2015 ECU created to play with WeMo tasks
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,WeMoActivity.class);
  				startTheActivity (localIntent,0);
  				// -----------------------------------------------------------------
  				break;
  			// =====================================================================
		 }
	}
	// =============================================================================
	// =============================================================================
	// L O N G   P R E S S   O N   I M A G E
	// =============================================================================
	// =============================================================================
	void SwitchOnImageLong (int thePosition,View theView)
	{
		// -------------------------------------------------------------------------
		// 20/08/2015 ECU handle a 'long press' on a displayed image
		// -------------------------------------------------------------------------
		switch (activeImages [IMAGE_INDEX][thePosition])
		{
			// =====================================================================
			case R.drawable.barcode:
				// -----------------------------------------------------------------
				// 13/06/2016 ECU added here to try and start an immediate scan
				// -----------------------------------------------------------------	
				localIntent = new Intent (context,BarCodeActivity.class);
				localIntent.putExtra (StaticData.PARAMETER_BARCODE,true);
				startTheActivity (localIntent,0);
				break;
			// =====================================================================
  			case R.drawable.carer:
  				// -----------------------------------------------------------------
  				// 02/01/2016 ECU added
  				// 28/11/2016 ECU this used to be the unshifted activation
  				// -----------------------------------------------------------------
  				localIntent = new Intent (context,CarerSystemActivity.class);
  				startTheActivity (localIntent,0);
  				// -----------------------------------------------------------------
				break;
			// =====================================================================
  			case R.drawable.database:
				// -----------------------------------------------------------------
				// 28/01/2016 ECU custom database activity
				// -----------------------------------------------------------------	
				localIntent = new Intent (context,DatabaseActivity.class);
				startActivityForResult (localIntent,0);
				break;
			// =====================================================================
      		case  R.drawable.daily_summary:
      			// -----------------------------------------------------------------
      			// 27/02/2017 ECU created to show a summary of tasks for the
      			//                selected day
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,DailySummaryActivity.class);
      			localIntent.putExtra( StaticData.PARAMETER_DAY,true);
      			startTheActivity (localIntent,0);
      			// -----------------------------------------------------------------
      			break;
			// =====================================================================
			case R.drawable.doctor:
			case R.drawable.hospital:
				// -----------------------------------------------------------------
      			// 06/01/2014 ECU added AppointsActivity
      			// 10/11/2014 ECU changed to use variable PARAMETER.....
      			// 19/12/2015 ECU combine the cases and tailor using the parameter
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,AppointmentsActivity.class);
      			localIntent.putExtra(StaticData.PARAMETER_SELECT,
      				(activeImages [IMAGE_INDEX][thePosition] == R.drawable.doctor) ? StaticData.PARAMETER_DOCTOR
      																			   : StaticData.PARAMETER_HOSPITAL);
      			startTheActivity (localIntent,0);
				// -----------------------------------------------------------------
				break;
			// =====================================================================
			case R.drawable.system_information:
				// -----------------------------------------------------------------
				// 22/11/2014 ECU action the new version that is in development
				// -----------------------------------------------------------------	
				localIntent = new Intent (context,NewSystemInfoActivity.class);
				startActivityForResult (localIntent,0);
				break;
			// =====================================================================
     		case R.drawable.mail:
      			// -----------------------------------------------------------------
      			// 11/07/2015 ECU added to manually generate an email
     			// 02/05/2016 ECU only worth doing if 'email sending' is enabled
     			//                in settings
      			// -----------------------------------------------------------------
     			if (PublicData.emailDetails.enabled && Utilities.checkForNetwork (context))
     			{
     				localIntent = new Intent (context,EMailActivity.class);
     				startActivityForResult (localIntent,0);
     			}
     			else
     			{
     				// -------------------------------------------------------------
     				// 02/05/2016 ECU tell the user what is going on
     				// -------------------------------------------------------------
     				Utilities.popToast (context.getString(R.string.manual_email_not_available) +
     									(PublicData.emailDetails.enabled ? context.getString(R.string.network_no_access) 
     																	 : context.getString(R.string.settings_not_enabled)),true);
     				// -------------------------------------------------------------
     			}
				// -----------------------------------------------------------------
      			break;
			// =====================================================================
			case R.drawable.medication:
				// -----------------------------------------------------------------
				// 27/11/2014 ECU give the option to email the medication details
				// -----------------------------------------------------------------
				if (PublicData.medicationDetails.size() > 0)
				{
					// -------------------------------------------------------------
					// 11/12/2016 ECU changed to use 'yes/no' rather than a poptoast
					// -------------------------------------------------------------
					DialogueUtilities.yesNo (context,  
											 getString (R.string.medication_email_title),
											 getString (R.string.medication_email_summary),
											 null,
											 true,getString (R.string.medication_email_sort_medication),Utilities.createAMethod (MedicationActivity.class,"EmailMedicationMethod",(Object) null),
											 true,getString (R.string.medication_email_sort_day),       Utilities.createAMethod (MedicationActivity.class,"EmailMedicationByDayMethod",(Object) null));
					// ------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				break;
			// =====================================================================
      		case  R.drawable.panic_alarm:
      			// -----------------------------------------------------------------
      			// P A N I C  A L A R M
      			// ====================
      			// 25/11/2015 ECU start the panic alarm activity
      			// 27/11/2015 ECU start the 'panic' immediately
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,PanicAlarmActivity.class);
      			// -----------------------------------------------------------------
      			// 27/11/2015 ECU specify that the panic alarm action is to be
      			//                processed immediately
      			// -----------------------------------------------------------------
      			localIntent.putExtra (StaticData.PARAMETER_PANIC,true);
  				startTheActivity (localIntent,0);
  				break;
  			// =====================================================================
      		case R.drawable.speaking_clock:
      			// -----------------------------------------------------------------
      			// 25/01/2016 ECU added - speaking clock - the current time
      			// -----------------------------------------------------------------
      			Utilities.SpeakingClock ();
      			// -----------------------------------------------------------------
      	    	break;
 			// =====================================================================
      		case R.drawable.television:
      			// -----------------------------------------------------------------
      			// 08/05/2015 ECU created to use swipe to select different television
      			//                controllers
      			// 11/05/2015 ECU copy down the test on whether hardware exists
      			// -----------------------------------------------------------------
      			if (PublicData.blueToothService || PublicData.storedData.remoteAlways)
      			{
      				localIntent = new Intent (context,TelevisionSwipeActivity.class);
      				startTheActivity (localIntent,0);
      			}
      			else
      			{
      				// -------------------------------------------------------------
      				// 02/03/2014 ECU added the view to get a pop up window
      				// 18/03/2014 ECU add the legend for the button
      				// 08/04/2014 ECU use resources
      				// 31/05/2016 ECU add the 'false' to indicate no scrolling
      				// -------------------------------------------------------------
      				Utilities.popToast (theView,
      									getString (R.string.no_remote_controller),
      									getString (R.string.press_to_clear),
      									60000,
      									false);
      				// -------------------------------------------------------------
      			}
      			// -----------------------------------------------------------------
      			break;
      		// =====================================================================
      		case  R.drawable.tone_generator:
      			// -----------------------------------------------------------------
      			// 18/08/2015 ECU added
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,ToneGeneratorActivity.class);
      			Tone tones []	= {
      								// ---------------------------------------------
      								// 18/08/2015 ECU five notes from Close Encounters
      								//                Of The Third Kind
      								// ---------------------------------------------
      			  					new Tone ("G5",500),
      			  					new Tone ("A5",500),
      			  					new Tone ("F5",500),
      			  					new Tone ("F4",500),
      			  					new Tone ("C5",1000)
      			  					// ---------------------------------------------
      			  				};
      			localIntent.putExtra (StaticData.PARAMETER_TONES,new Tones (tones));
      			startTheActivity (localIntent,StaticData.REQUEST_CODE_FINISH);
      			break;
      		// =====================================================================
      		case R.drawable.torch:
      			// -----------------------------------------------------------------
      			// 30/12/2016 ECU added to handle delayed actions on the torch
      			// -----------------------------------------------------------------
  				FlashLight.flashLightDelayToggle (context);
  				// -----------------------------------------------------------------
  				break;
      		// =====================================================================
      		case R.drawable.tv_guide:
      			// -----------------------------------------------------------------
      			// 16/09/2015 ECU display the TV channels guide
      			// 22/09/2015 ECU moved here actioned on a shift
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,TVChannelsActivity.class);
      			startTheActivity (localIntent,0);
  				break;
  			// =====================================================================
 			case R.drawable.voice_recognition:
 				// -----------------------------------------------------------------
 				// 10/09/2013 ECU start voice recognition without checking for network
 				//                access - this will give access to offline voice
 				//				  recognition
 				// -----------------------------------------------------------------	
 				localIntent = new Intent (context,VoiceRecognition.class);
 				startActivityForResult (localIntent,0);
 				break;
 			// =====================================================================
 			case R.drawable.wemo:
 				// -----------------------------------------------------------------
 				// 16/02/2015 ECU start the UPnP activity
 				// -----------------------------------------------------------------	
 				localIntent = new Intent (context,WeMoTimerActivity.class);
 				startActivityForResult (localIntent,0);
 				break;
 			// =====================================================================
 			default:
 				// -----------------------------------------------------------------
 				// 11/09/2013 ECU added to try and display help for the selected icon
 				// 19/01/2014 ECU changed to use activeImages
 				// -----------------------------------------------------------------
 				Utilities.gridHelp (context,activeImages[IMAGE_INDEX][thePosition]);
 				break;
		}
	}
	// =============================================================================
	void ZoomDisplay (int theIconsPerRow)
	{
		// -------------------------------------------------------------------------
		iconsPerRow = theIconsPerRow;
		// -------------------------------------------------------------------------
		// 01/10/2014 ECU now do common things to zoom the display
		// -------------------------------------------------------------------------
		SetImageSizes (iconsPerRow);
		// -------------------------------------------------------------------------
		// 01/10/2014 ECU update some display variables before asking for a refresh
		// -------------------------------------------------------------------------
		PublicData.imageAdapter.notifyDataSetChanged();
		// -------------------------------------------------------------------------
	}
	/* ----------------------------------------------------------------------------- */
	void ZoomDisplay (boolean theZoomFlag)
	{
		// -------------------------------------------------------------------------
		// 01/10/2014 ECU put all the display zooming in one place 
		//
		//                theZoomFlag = true ..... zoom in
		//                            = false .... zoom out
		// -------------------------------------------------------------------------
		if (theZoomFlag)
		{	
			// ---------------------------------------------------------------------
			// 01/10/2014 ECU zoom in
			// ---------------------------------------------------------------------
			if (iconsPerRow > 1)
				iconsPerRow--;
		}
		else
		{
			// ---------------------------------------------------------------------
			// 01/10/2014 ECU zoom out
			// ---------------------------------------------------------------------
			if (iconsPerRow < 10)
				iconsPerRow++;
		}
		// -------------------------------------------------------------------------
		// 01/10/2014 ECU do the common aspects
		// -------------------------------------------------------------------------
		ZoomDisplay (iconsPerRow);
		// -------------------------------------------------------------------------
		// 01/10/2014 ECU tell the user how to reset the display
		// -------------------------------------------------------------------------
		Utilities.popToast (getString(R.string.zooming_reset),true,Toast.LENGTH_SHORT);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	
	// =============================================================================
	// 09/11/2014 ECU add the methods needed for handling gestures
	// -----------------------------------------------------------------------------
	@Override
	public boolean onDown(MotionEvent motionEvent) 
	{
		return false;
	}
	// -----------------------------------------------------------------------------
	@Override
	public boolean onFling (MotionEvent motionEvent1,
							MotionEvent motionEvent2, 
							float velocityX,
							float velocityY) 
	{
		// -------------------------------------------------------------------------
		// 09/11/2014 ECU get the check in coordinates
		// -------------------------------------------------------------------------	
        float changeInX = motionEvent2.getX() - motionEvent1.getX();
        float changeInY = motionEvent2.getY() - motionEvent1.getY();
        // -------------------------------------------------------------------------
        // 09/11/2014 ECU if moved more in a left/right direction rather than up/
        //                down
        // -------------------------------------------------------------------------
        if (Math.abs (changeInX) > Math.abs (changeInY)) 
        {
        	// ---------------------------------------------------------------------
        	// 09/11/2014 ECU user has swiped, apparently, so check direction
        	// ---------------------------------------------------------------------
            if (Math.abs (changeInX) > SWIPE_DISTANCE_THRESHOLD && 
            		Math.abs (velocityX) > SWIPE_VELOCITY_THRESHOLD) 
            {
            
            	if (changeInX > 0) 
            	{
            		// -------------------------------------------------------------
            		// 10/11/2014 ECU swipe right has been detected
            		// -------------------------------------------------------------
            		localIntent = new Intent (context,SwipeActivity.class);
            		startActivityForResult (localIntent,0);
            		// -------------------------------------------------------------
            	} 
            	else 
            	{
            		// -------------------------------------------------------------
            		// 10/11/2014 ECU swipe left has been detected
            		// -------------------------------------------------------------
            		localIntent = new Intent (context,SwipeActivity.class);
            		startActivityForResult (localIntent,0);
            		// -------------------------------------------------------------
            	}
            }
            // ---------------------------------------------------------------------
        } 
        // -------------------------------------------------------------------------
        // 09/11/2014 ECU always indicate that the event has been handled
        // -------------------------------------------------------------------------
		return true;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	@Override
	public void onLongPress (MotionEvent motionEvent) 
	{
	}
	// ============================================================================
	@Override
	public void onRestoreInstanceState (Bundle savedInstanceState)
	{
		super.onRestoreInstanceState (savedInstanceState);
	}	
	// ============================================================================
	@Override
	public void onSaveInstanceState (Bundle savedInstanceState)
	{
		super.onSaveInstanceState (savedInstanceState);
	}
	// -----------------------------------------------------------------------------
	@Override
	public boolean onScroll (MotionEvent motionEvent1, MotionEvent motionEvent2, 
							float distanceX,float distanceY) 
	{
		return false;
	}
	// -----------------------------------------------------------------------------
	@Override
	public void onShowPress (MotionEvent motionEvent)
	{
	}
	// -----------------------------------------------------------------------------
	@Override
	public boolean onSingleTapUp (MotionEvent motionEvent) 
	{
		return false;
	}
	// -----------------------------------------------------------------------------
	
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class GridRefreshHandler extends Handler
    {
		// -------------------------------------------------------------------------
		// 13/04/2015 ECU created to enable a thread or activity to request that
		//                the adapter be updated
		//
		//                currently used by ServerThreadForData to update information
		//                from a music track that has been received from a remote
		//                device
		// 05/05/2015 ECU changed to be switchable on 'what' in the message and use
		//                MESSAGE_SLEEP rather than 0
		// -------------------------------------------------------------------------
        @Override
        public void handleMessage (Message theMessage) 
        {   
        	// ---------------------------------------------------------------------
        	// 05/05/2015 ECU change to switch on the type of message received
        	//                which is in '.what'
        	// ---------------------------------------------------------------------
        	switch (theMessage.what)
        	{
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_ADAPTER:
        			// -------------------------------------------------------------
            		// 13/04/2015 ECU need to select the appropriate adapter to be
                	//                'refreshed'
            		// -------------------------------------------------------------
                	if (!PublicData.storedData.userView)
            		{
            			// ---------------------------------------------------------
                		// 13/04/2015 ECU 'grid' user view
                		// ---------------------------------------------------------
            			PublicData.imageAdapter.notifyDataSetChanged ();
            			// ---------------------------------------------------------
            		}
            		else
            		{
            			// ---------------------------------------------------------
                		// 13/04/2015 ECU 'list' user view
                		// ---------------------------------------------------------
            			PublicData.customGridViewAdapter.notifyDataSetChanged ();
            			// ---------------------------------------------------------
            		}		
                    // -------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_NOTIFICATION_END:
        			// -------------------------------------------------------------
        			// 13/07/2016 ECU created to hide the notifications button
        			// -------------------------------------------------------------
        			notificationButton.clearAnimation();
        			notificationButton.setVisibility (View.GONE);
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_NOTIFICATION_START:
        			// -------------------------------------------------------------
        			// 13/07/2016 ECU created to set up the notification button and
        			//                start the animation
        			//            ECU the object in the message indicates
        			//                   true ..... start animation
        			//                   false .... no animation
        			// -------------------------------------------------------------
        			if (notificationButton.getVisibility() == View.GONE)
        				notificationButton.setVisibility (View.VISIBLE);
        			// -------------------------------------------------------------
        			// 13/07/2016 ECU now decide if the animation is to start
        			// -------------------------------------------------------------
        			if ((Boolean) theMessage.obj)
        				Utilities.AnimateFlashImageView (notificationButton,1000,Animation.INFINITE);
        			// -------------------------------------------------------------
    				notificationButton.setOnClickListener (notificationButtonOnClickListener);	
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_PROMPT:
        			// -------------------------------------------------------------
        			// 21/12/2015 ECU created to handle a request to display a prompt
        			//                be an activity or service which does not have
        			//                access to the user interface
        			// -------------------------------------------------------------
        			Bundle localBundle = theMessage.getData();
        			// -------------------------------------------------------------
        			// 21/12/2015 ECU now start up the prompt dialogue
        			//            ECU some issues with the context being used as at
        			//                the moment the prompt will only be shown over
        			//                GridActivity view
        			// -------------------------------------------------------------
        			DialogueUtilities.prompt ((Context) activity,
        									  localBundle.getString(StaticData.PARAMETER_PROMPT_LEGEND),
        									  localBundle.getString(StaticData.PARAMETER_PROMPT_BODY),
        									  localBundle.getString(StaticData.PARAMETER_PROMPT_BUTTON)); 
        			// -------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_SCREEN:
        			// -------------------------------------------------------------
        			// 05/05/2015 ECU sets the screen state which is held in
        			//                the message
        			// -------------------------------------------------------------
        			Utilities.setTheScreenState ((Boolean) theMessage.obj);	
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_SLEEP:
        			// -------------------------------------------------------------
        			// 05/05/2015 ECU put any code that is the result of a 'sleep'
        			//                here
        			// -------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        	}
        	// ---------------------------------------------------------------------
        }
        /* ------------------------------------------------------------------------- */
        public void sleep(long delayMillis)
        {	
        	// ---------------------------------------------------------------------
        	// 05/05/2015 ECU changed to use MESSAGE_SLEEP instead of 0
        	// ---------------------------------------------------------------------
            this.removeMessages (StaticData.MESSAGE_SLEEP);
            sendMessageDelayed(obtainMessage (StaticData.MESSAGE_SLEEP), delayMillis);
        }
    };
	// =============================================================================
    
    // =============================================================================
    // =============================================================================
    // 03/03/2016 ECU declare methods used in dialogues
    // =============================================================================
    // =============================================================================
	public static void SettingsPassword (String thePassword)
	{
		// -------------------------------------------------------------------------
		// 03/03/2016 ECU created to check an entered password and to start the
		//                settings activity if it is valid
		// -------------------------------------------------------------------------
		if (thePassword.equalsIgnoreCase (StaticData.SETTINGS_PASSWORD))
		{
			// ---------------------------------------------------------------------
			// 03/03/2016 ECU start up the settings activity
			// ---------------------------------------------------------------------
			Intent localIntent = new Intent (context,SettingsActivity.class);
			localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity (localIntent);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 03/03/2016 ECU an invalid password was entered
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (context.getString (R.string.enter_password_error));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

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
// 20/03/2017 ECU when 'groupActivities' is true then ignore the click counter
// 01/04/2017 ECU changed GridImages to use the resource ID rather than a literal
//                string when displaying the legends of tasks. This also involved
//                changes to the GridImages class
// 05/06/2017 ECU added 'gridHelpIntro'
// 17/06/2017 ECU included the handling of 'startPanicAlarm'
// 19/07/2017 ECU handle the legends for 'long press' on images
// 26/08/2017 ECU added 'blood pressure' activity
// 01/09/2017 ECU when this activity is started then there are a number of phrases
//                that the TextToSpeechService is processing. If the user is quick then
//                this could be annoying - so which the user clicks on an icon then
//                tell the service to flush the queue after stopping what is being
//                spoken
// 15/09/2017 ECU added 'video streaming'
// 23/09/2017 ECU Note - 'groupActivities' is used to specify a number of activities
//                       which are to 'form a group'. It is set to true, via a
//                       parameter in the intent, by the GroupActivity.
// 06/10/2017 ECU added 'theft protection'
// 02/01/2018 ECU added MusicPlayerTimerActivity
// 04/02/2018 ECU added CountdownTimerActivity
// 05/07/2018 ECU provide 'positive feedback' facilities
// 17/08/2018 ECU handle the 'ignoring' of 'click','long click' and 'scroll' events
// 27/12/2018 ECU PROBLEM - 'sorting by usage' - see 'raw/documentation_bugs'
// 28/12/2018 ECU The problem of 27/12/2018 is caused by those actions which do not
//                require an activity to be called, e.g. torch. These actions will
//                not result on 'onResume' being envoked and therefore the screen
//                will not be updated correctly. To get around this, in a messy way,
//                'UpdateDisplay' has been declared and can be called after the actions
//                like torch to force the displayed to be updated correctly.
// 02/04/2019 ECU added 'devices'
// 13/06/2019 ECU added 'RevCounterActivity'
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
	public static String	gridHelpIntro;				// 05/06/2017 ECU added
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
	boolean					ignoreUserActions = false;	// 17/08/2018 ECU added to indicate when to action or not
														//                action user actions
	int						lastPosition = StaticData.NO_RESULT;
														// 25/01/2016 ECU remember the last position clicked
	Sensor 		   			lightSensor; 				// 17/02/2014 ECU moved here from MainActivity
	ListView				listView;					// 28/01/2014 ECU added
	Intent					localIntent;				// 10/11/2014 ECU added here
	ImageButton				notificationButton = null;	// 12/07/2016 ECU added
	int						positionToAction = StaticData.NO_RESULT;
														// 10/11/2014 ECU added
	SensorManager    		sensorManager = null; 		// 17/02/2014 ECU moved here from MainActivity
	boolean					sortInhibit	= false;		// 23/09/2017 ECU added
	View					switchImageView;			// 05/07/2018 ECU added
	Object					underlyingObject;			// 22/03/2018 ECU added
	boolean					volumeKey = false;			// 01/10/2014 ECU used to prevent multiple
														//                actioning of volume key when
	                                                    //				  zooming
	boolean					zoomed = false;				// 01/10/2014 ECU remember if zooming has happened
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
	// 19/07/2017 ECU added the long press legend on caring
	//            ECU added the long press legend on barcode reader
	//            ECU having tested the above then add 'long press' legends to most
	//                entries
	// 26/08/2017 ECU added 'blood pressure'
	// 27/09/2017 ECU make video streaming conditional on StaticData.VIDEO_STREAMING
	// 18/12/2017 ECU added the long legend for 'contacts'
	// 02/01/2018 ECU added the long press for 'music player'
	// 01/01/2019 ECU PROBLEM - because 'gridImages' contains elements 'R.drawable...'
	//                          and 'R.string...' then these can change between
	//							compilations so that the information held on disk will
	//                          no longer be valid. The only reason that the information 
	//                          is held on disk is to retain the usage.
	//            ECU Changed the logic so that the usage for each element is stored
	//                in an array which will be stored to disk.
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
	//
	//                          PLEASE NOTE - there is no particular order of this
	//                                        array - just grew this way as it was
	//                                        developed.
	//
	//										  01/01/2019 ECU arranged alphabetically by legend
	// -----------------------------------------------------------------------------
	public final static GridImages <?> [] originalGridImages = {
								new GridImages<Object> 			(R.drawable.audio_analyser,		R.string.legend_audio_analyser,				false),
								new GridImages<Object> 			(R.drawable.audio_streaming,	R.string.legend_audio_streaming,			true,	true,	R.string.legend_audio_streaming_long),
								new GridImages<Object> 			(R.drawable.barcode,			R.string.legend_barcode_reader,				true,	true,	R.string.legend_barcode_reader_long),
								new GridImages<Object> 			(R.drawable.blood_pressure,		R.string.legend_blood_pressure,				true,	true,	R.string.legend_blood_pressure_long),
								new GridImages<Object> 			(R.drawable.bluetooth,			R.string.legend_bluetooth,					false),
								new GridImages<Object> 			(R.drawable.internet,			R.string.legend_browse_the_internet,		true),
								new GridImages<Object> 			(R.drawable.carer,				R.string.legend_carer_planning,				true,	true,	R.string.legend_carer_planning_long),
								new GridImages<Object> 			(R.drawable.compass_small,		R.string.legend_compass,					true),	
								new GridImages<Object> 			(R.drawable.database,   		R.string.legend_contacts,					true,	true,	R.string.legend_contacts_long),
								new GridImages<Object> 			(R.drawable.daily_summary,		R.string.legend_daily_summary,				true,	true,	R.string.legend_daily_summary_long),
								new GridImages<Object> 			(R.drawable.bouncing_ball,		R.string.legend_dexterity_game,				true),	
								new GridImages<Object> 			(R.drawable.devices,			R.string.legend_devices,					false),	
								new GridImages<Object> 			(R.drawable.disk,				R.string.legend_disk_utilities,				false),	
								new GridImages<Object> 			(R.drawable.doctor,				R.string.legend_doctor_appointments,		true,	true,	R.string.legend_hospital_appointments_long),
								new GridImages<Object> 			(R.drawable.documents,			R.string.legend_documents,					false),
								new GridImages<Object> 			(R.drawable.exercise,			R.string.legend_exercises,					true),
								new GridImages<Object> 			(R.drawable.falling,			R.string.legend_fall_detector,				true),
								new GridImages<Object> 			(R.drawable.photoalbum,			R.string.legend_family_album,				true),
								new GridImages<Object> 			(R.drawable.files,				R.string.legend_file_explorer,				false),	
								new GridImages<Object> 			(R.drawable.groups,				R.string.legend_group_activities,			false),
								new GridImages<Object> 			(R.drawable.hospital,			R.string.legend_hospital_appointments,		true,	true,	R.string.legend_hospital_appointments_long),
								new GridImages<LiquidActivity> 	(R.drawable.liquid,   			R.string.legend_liquid_selection,			true,	false, 	LiquidActivity.class, "validation"),
								new GridImages<Object> 			(R.drawable.mail,				R.string.legend_mail_system,				true,   true,	R.string.legend_mail_system_long),
								new GridImages<Object> 			(R.drawable.medication,			R.string.legend_medication_details,			true,	true,	R.string.legend_medication_details_long),
								new GridImages<Object> 			(R.drawable.music,				R.string.legend_music_player,				true,	true,	R.string.legend_music_player_long),
								new GridImages<Object> 			(R.drawable.named_actions,		R.string.legend_named_actions,				true),
								new GridImages<Object> 			(R.drawable.drawer,   			R.string.legend_navigation_drawer,			false),
								new GridImages<NFC_Activity> 	(R.drawable.nfc,   			    R.string.legend_nfc_tags,					true,	false, 	NFC_Activity.class, "validation"),
								new GridImages<Object> 			(R.drawable.panic_alarm,   		R.string.legend_panic_alarm,				true,	true,	R.string.legend_panic_alarm_long),
								new GridImages<Object> 			(R.drawable.radar,   			R.string.legend_radar_security,				false),
								new GridImages<Television> 		(R.drawable.television,			R.string.legend_remote_controller,			true,	true,	R.string.legend_remote_controller_long, Television.class, "validation"),
								new GridImages<Object> 			(R.drawable.rev_counter,		R.string.legend_rev_counter,				false),	
								new GridImages<Object>			(R.drawable.screen_capture,		R.string.legend_screen_capture,				true),
								new GridImages<Object>			(R.drawable.shopping,   		R.string.legend_shopping,					true,	true,	R.string.legend_shopping_long),
								new GridImages<Object> 			(R.drawable.microphone,			R.string.legend_sound_recorder,				true),
								new GridImages<Object> 			(R.drawable.speaking_clock,   	R.string.legend_speaking_clock,				true,	true,	R.string.legend_speaking_clock_long),
								new GridImages<Object> 			(R.drawable.dialogue,			R.string.legend_start_a_dialogue,			true),
								new GridImages<Object> 			(R.drawable.swipe,   			R.string.legend_swipe_tests,				false),	
								new GridImages<Object> 			(R.drawable.system_information,	R.string.legend_system_details,		   		false,	true,	R.string.legend_system_details_long),
								new GridImages<Object> 			(R.drawable.settings,			R.string.legend_system_settings,			true),	
								new GridImages<Object> 			(R.drawable.tcp,				R.string.legend_tcp_utilities,				false),	
								new GridImages<Object> 			(R.drawable.test,				R.string.legend_test_facilities,			false),	
								new GridImages<Object> 			(R.drawable.theft,				R.string.legend_theft_protection,			true,	true,	R.string.legend_theft_protection_long),
								new GridImages<Object> 			(R.drawable.timer,				R.string.legend_timers,						true,	true,	R.string.legend_timers_long),
								new GridImages<Object>			(R.drawable.tone_generator,		R.string.legend_tone_generator,				false,	true,	R.string.legend_tone_generator_long),
								new GridImages<Validation> 		(R.drawable.torch,				R.string.legend_torch,						true,	true,	R.string.legend_torch_long, Validation.class, "torchValidation"),
								new GridImages<Object> 			(R.drawable.location,			R.string.legend_tracking_facility,			true),
								new GridImages<Object>			(R.drawable.tv_guide,			R.string.legend_tv_program_guide,			true,	true,	R.string.legend_tv_program_guide_long),
								new GridImages<Object> 			(R.drawable.upnp,				R.string.legend_upnp_wemo,					true,	true,	R.string.legend_upnp_wemo_long),
								new GridImages<Object> 			(R.drawable.video,				R.string.legend_video_recorder,				true,	StaticData.VIDEO_STREAMING,	R.string.legend_video_recorder_long),
								new GridImages<Object> 			(R.drawable.gameone,			R.string.legend_visual_game,				true),
								new GridImages<Object> 			(R.drawable.voice_recognition,	R.string.legend_voice_commands,				true,	true,	R.string.legend_voice_commands_long),
								new GridImages<WeMoActivity>	(R.drawable.wemo,   		 	R.string.legend_wemo_tasks,					true,	true,	R.string.legend_wemo_tasks_long, WeMoActivity.class, "validation")	
						};
	// =============================================================================
	// 16/01/2015 ECU changed to public static
	// -----------------------------------------------------------------------------
	public static ArrayList<GridItem> gridItems = new ArrayList<GridItem>();
	// =============================================================================
	public static GridImages <?> [] gridImages; 
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
			// 22/03/2018 ECU added as a listener for 'invokes'
			// ---------------------------------------------------------------------
			underlyingObject = this;
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 06/10/2016 ECU check for grouping of activities
				// -----------------------------------------------------------------
				groupActivities = extras.getBoolean (StaticData.PARAMETER_GROUP,false);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 01/01/2019 ECU before copying the original grid images then set the
			//                usages
			// ---------------------------------------------------------------------
			SetUsages ();
			// ---------------------------------------------------------------------
			// 01/01/2019 ECU copy the original images into the working array
			// ---------------------------------------------------------------------
			gridImages = Arrays.copyOf (originalGridImages,originalGridImages.length);
			// ---------------------------------------------------------------------
			// 18/01/2015 ECU check on whether the instance of gridImages in storedData
			//                is to be reset
			// 08/10/2016 ECU check on grouping
			// 09/10/2016 ECU add the null check on lists
			// ---------------------------------------------------------------------
			if (!PublicData.storedData.groupActivities || groupActivities || PublicData.storedData.groupLists == null)
			{
				// -----------------------------------------------------------------
				// 28/12/2018 ECU now decide how whether the images need sorting
				// -----------------------------------------------------------------
				// 28/12/2018 ECU first check for sorting by legend
				//            ECU added the groupActivities check
				// -----------------------------------------------------------------
				if (PublicData.storedData.sortByLegend || groupActivities)
				{
					SortGridImagesByLegend ();
				}
				else
				// -----------------------------------------------------------------
				// 28/12/2018 ECU check for sorting by usage
				// -----------------------------------------------------------------
				if (PublicData.storedData.sortByUsage)
				{
					 Arrays.sort (gridImages);	
				}
				// -----------------------------------------------------------------
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
			// 23/09/2017 ECU preset any variables
			// ---------------------------------------------------------------------
			sortInhibit 		= false;
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
				// 07/02/2018 ECU with the addition of the 'long press' then needed
				//                to change the checking for validity
				// -----------------------------------------------------------------
				if ((positionToAction & StaticData.ACTIVITY_LONG_OFFSET_MASK) >= gridImages.length || positionToAction < 0)
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
				// 23/09/2017 ECU check for the 'sort inhibit' parameter which
				//                indicates whether the image array is to be sorted 
				//                or not
				// -----------------------------------------------------------------
				sortInhibit = extras.getBoolean (StaticData.PARAMETER_SORT_INHIBIT,false);
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
				// 05/06/2017 ECU change to use gridHelpIntro
				// -----------------------------------------------------------------
				gridHelpIntro	   = this.getString (R.string.grid_help_file_header);
				gridHelpFileHeader = PublicData.projectFolder + 
											this.getString (R.string.help_directory) + 
											gridHelpIntro;
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
						// 17/06/2017 ECU put in the 'panic alarm' check
						// ---------------------------------------------------------
						if (!PublicData.storedData.groupActivities && 
								!groupActivities && 
								!PublicData.storedData.startPanicAlarm)
						{
							Utilities.DisplayADrawable (context,R.drawable.grid_help,StaticData.DRAWABLE_WAIT_TIME,false,true);
						}
						// ---------------------------------------------------------
						// 12/07/2016 ECU put in the check on the number of clicks
						//                needed to start an activity
						// 20/03/2017 ECU put in check on groupActivities
						// ---------------------------------------------------------
						if (!groupActivities)
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
					// -------------------------------------------------------------
					// 21/06/2018 ECU handle the random event
					// -------------------------------------------------------------
					PublicData.storedData.randomEvent.setAlarm (context);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 10/03/2018 ECU check if there is a countdown timer running
				// -----------------------------------------------------------------
				CountdownTimerActivity.CheckIfRunning (this);
				// -----------------------------------------------------------------
				// 18/05/2016 ECU check if an activity is to be started automatically
				// 23/09/2017 ECU changed to use 'Legend' rather than 'Number' because
				//                the position of an activity can change if 'sort by
				//                usage' is in use
				// -----------------------------------------------------------------
				if (PublicData.storedData.activityOnStart)
				{
					SwitchOnImage (GridImages.returnPosition (gridImages,PublicData.storedData.activityOnStartLegend),null);
				}
				// -----------------------------------------------------------------
				// 17/06/2017 ECU check if the 'panic alarm' is to be triggered
				// -----------------------------------------------------------------
				if (PublicData.storedData.startPanicAlarm)
				{
					PanicAlarmActivity.Actions (this);
				}
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 10/11/2014 ECU activity has been called with a position parameter so
				//                start up the required activity. Be careful the second
				//       		  argument is the view so 'null' may not be a good idea
				// 07/02/2018 ECU decide whether the 'long press' is to be actioned
				// -----------------------------------------------------------------
				if (positionToAction < StaticData.ACTIVITY_LONG_OFFSET)
				{
					// -------------------------------------------------------------
					// 07/02/2018 ECU treat as if the item just clicked
					// -------------------------------------------------------------
					SwitchOnImage (positionToAction,null);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 07/02/2018 ECU treat as if the 'long press' is to be actioned
					// -------------------------------------------------------------
					SwitchOnImageLong (positionToAction - StaticData.ACTIVITY_LONG_OFFSET,null);
					// -------------------------------------------------------------
				}
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
	    		GroupActivity.groupMessageHandler.sendEmptyMessage (StaticData.MESSAGE_FINISH);
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
	    		// 01/01/2019 ECU execute any tasks that need completing before
	    		//                this activity is 'destroyed'
	    		// -----------------------------------------------------------------
	    		TidyUpBeforeExiting ();
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
	    	// ---------------------------------------------------------------------
	    }   
	    // -------------------------------------------------------------------------
	    else
	    {
	    	 // --------------------------------------------------------------------
	    	 // 20/02/2014 ECU reset the 'back key' counter
			 // -------------------------------------------------------------------- 
			 backKeyCounter = BACK_KEY_COUNTER;
			 // --------------------------------------------------------------------
	         return super.onKeyDown(keyCode,event);
	         // --------------------------------------------------------------------
	    }
	}
	/* ============================================================================= */
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
	    	// ---------------------------------------------------------------------
	    	// 29/09/2018 ECU check if the 'theft activity' needs to know about the
	    	//                key
	    	// 20/08/2019 ECU return according to the state returned by Vol....
	    	// ---------------------------------------------------------------------
	    	if (TheftActivity.movement != null)
	    	{
	    		// -----------------------------------------------------------------
	    		// 20/08/2019 ECU check if the volume key has been processed locally
	    		// -----------------------------------------------------------------
	    		if (TheftActivity.movement.VolumeKeyPressed ())
	    		{
	    			// -------------------------------------------------------------
	    			// 20/08/2019 ECU the key has been processed so indicate this to 
	    			//                the caller
	    			// -------------------------------------------------------------
	    			return true;
	    			// -------------------------------------------------------------
	    		}
	    	}
	    	// ---------------------------------------------------------------------
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
				// -----------------------------------------------------------------
            	// 08/10/2016 ECU restart this activity
        		// 09/10/2016 ECU moved here
            	// -----------------------------------------------------------------
        		finish ();
            	localIntent = new Intent (context,GridActivity.class);
        		startActivityForResult (localIntent,0);
        		// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
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
				// -----------------------------------------------------------------
				// 08/10/2016 ECU restart this activity
				// 09/10/2016 ECU moved here
				// -----------------------------------------------------------------
				finish ();
				localIntent = new Intent (context,GridActivity.class);
				startActivityForResult (localIntent,0);
				// -----------------------------------------------------------------
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
		// 16/01/2015 ECU added the check on 'gridRe....'
		// 18/01/2015 ECU added the sortByUsage option
		// 27/09/2017 ECU split the 'sortByUsage' away from the 'gridRebuild' because
		//                do not want to copy the original grid images. The 'sort by
		//                usage' is here because the number of times an activity is
		//                called will possibly change the order of displayed 'rows'
		// -------------------------------------------------------------------------
		if (PublicData.gridRebuild || PublicData.storedData.sortByUsage) 
		{
			if (PublicData.gridRebuild)
			{
				// -----------------------------------------------------------------
				// 22/05/2015 ECU reset the images array - back to its 'unsorted' state
				// -----------------------------------------------------------------
				gridImages = Arrays.copyOf (originalGridImages,originalGridImages.length);
				// -----------------------------------------------------------------
				// 16/01/2015 ECU make sure the rebuild flag is cleared
				// -----------------------------------------------------------------
				PublicData.gridRebuild = false;
				// -----------------------------------------------------------------
			}
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
			// ---------------------------------------------------------------------
			// 30/11/2014 ECU reset the flag that indicates an 'onPause' has
			//                happened
			// ---------------------------------------------------------------------
			beenPaused = false;
			// ---------------------------------------------------------------------
			// 30/11/2014 ECU reenable the monitor service if it was running
			//                when 'onPause' occurred
			// ---------------------------------------------------------------------
			if (enableMonitor)
			{
				enableMonitor = false;
				PublicData.storedData.monitor.enabled = true;
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	   	super.onResume(); 
	   	// -------------------------------------------------------------------------
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
	    		Utilities.popToast (getString(R.string.file_name_returned) + StaticData.NEWLINE + fileName,true);
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
			// ---------------------------------------------------------------------
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
	        // 19/07/2017 ECU changed to use BLANK....
	        // ---------------------------------------------------------------------
	        localHolder.textView.setText (StaticData.BLANK_STRING);
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
	        // 04/06/2017 ECU added the context as an argument
	        // 20/08/2019 ECU do not process the activityUpdate if in grid mode
			// ---------------------------------------------------------------------	
	        if (PublicData.storedData.userView)
	        	UserInterface.activityUpdate (context,localImages [position],localHolder.imageView,localHolder.textView);
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
				// 01/04/2017 ECU changed to use new Legend method
				// 18/04/2017 ECU added context as an argument
				// 19/07/2017 ECU added the 'long press' legend
				// -----------------------------------------------------------------		
				localGridItems.add (new GridItem (gridImages [theIndex].imageId,
												  gridImages [theIndex].Legend (context),
												  gridImages [theIndex].longPress,
												  gridImages [theIndex].LegendLong (context)));
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 31/01/2014 ECU in normal mode so exclude development icons
				// 27/01/2015 ECU included the 'long press' argument
				// 09/03/2015 ECU include the validate option to check that the
				//                associated activity can run on this device
				// 18/04/2017 ECU added context as an argument
				// 19/07/2017 ECU added the 'long press' legend
				// -----------------------------------------------------------------		
				if (gridImages [theIndex].mode && 
					gridImages [theIndex].Validate())
				{
					localGridItems.add (new GridItem (gridImages [theIndex].imageId,
							                          gridImages [theIndex].Legend (context),
							                          gridImages [theIndex].longPress,
							                          gridImages [theIndex].LegendLong (context)));
				}
				// -----------------------------------------------------------------
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
		// -------------------------------------------------------------------------
		if (PublicData.storedData.groupListCurrent == 0)
			groupPreviousButton.setVisibility (View.INVISIBLE);
		else
			groupPreviousButton.setVisibility (View.VISIBLE);
		if (PublicData.storedData.groupListCurrent == (PublicData.storedData.groupLists.size() - 1))
			groupNextButton.setVisibility (View.INVISIBLE);
		else
			groupNextButton.setVisibility (View.VISIBLE);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
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
					// 17/08/2018 ECU added the ignore check
		            // -------------------------------------------------------------
					if (!ignoreUserActions)
					{
						SwitchOnImage (position,view);
					}
					// -------------------------------------------------------------
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
		    		// 17/08/2018 ECU added the ignore check
		    		// ------------------------------------------------------------  
		    		if (!ignoreUserActions)
		    		{
		    			SwitchOnImageLong (position,view);
		    		}
				    // ------------------------------------------------------------                       	
		            return true;
		        }
		    });
		    // ---------------------------------------------------------------------
		 	// 10/11/2014 ECU put in the listener to enable swipes to be detected
		 	// ---------------------------------------------------------------------
		 	gridView.setOnTouchListener (new OnTouchListener() 
		 	{
		 	    @Override
		 	    public boolean onTouch (View view, MotionEvent event) 
		 	    {
		 	    	// -------------------------------------------------------------
		 	    	// 17/08/2018 ECU decide if user actions are to be processed
		 	    	// -------------------------------------------------------------
		 	    	if (!ignoreUserActions)
					{
			    		// ---------------------------------------------------------
			    		// 25/01/2016 ECU added to correct a warning message
			    		// ---------------------------------------------------------
			    		view.performClick ();
			    		// ---------------------------------------------------------
			    		return gestureDetector.onTouchEvent (event);
			    		// ---------------------------------------------------------
					}
			    	else
			    	{
			    		// ---------------------------------------------------------
			    		// 17/08/2018 ECU indicate that the event is to be ignored
			    		// ---------------------------------------------------------
			    		return true;
			    		// ---------------------------------------------------------
			    	}
		 	    	// -------------------------------------------------------------
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
					// 17/08/2018 ECU added the ignore check
				    // -------------------------------------------------------------
					if (!ignoreUserActions)
					{
						SwitchOnImage (position,view); 
					}
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
		    		// 17/08/2018 ECU added the ignore check
				    // -------------------------------------------------------------
		    		if (!ignoreUserActions)
					{
		    			SwitchOnImageLong (position,view);
					}
				    // -------------------------------------------------------------                      	
		            return true;
		            // -------------------------------------------------------------
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
			    	if (!ignoreUserActions)
					{
			    		// ---------------------------------------------------------
			    		// 25/01/2016 ECU added to correct a warning message
			    		// ---------------------------------------------------------
			    		view.performClick ();
			    		// ---------------------------------------------------------
			    		return gestureDetector.onTouchEvent (event);
			    		// ---------------------------------------------------------
					}
			    	else
			    	{
			    		// ---------------------------------------------------------
			    		// 17/08/2018 ECU indicate that the event is to be ignored
			    		// ---------------------------------------------------------
			    		return true;
			    		// ---------------------------------------------------------
			    	}
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
	// =============================================================================
	public static int positionInActiveImages (int theResourceID)
	{
		// -------------------------------------------------------------------------
		// 30/03/2017 ECU loop through the array looking for the specified resource
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < activeImages [IMAGE_INDEX].length; theIndex++)
		{
			// ---------------------------------------------------------------------
			// 30/03/2017 ECU check for a match in the array
			// ---------------------------------------------------------------------
			if (activeImages [IMAGE_INDEX][theIndex] == theResourceID)
			{
				// -----------------------------------------------------------------
				// 30/03/2017 ECU return the index into the array
				// -----------------------------------------------------------------
				return theIndex;
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 30/03/2017 ECU there is no match in the array
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
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
	// =============================================================================
	Integer [][] GetImages (boolean theMode)
	{
		// -------------------------------------------------------------------------
		// 19/01/2014 ECU do a quick scan to see how many elements I need
		// 09/03/2015 ECU added the check of validity of icons
		// -------------------------------------------------------------------------
		// 19/01/2014 ECU if theMode is true then the whole array is used
		// 18/01/2015 ECU the array will contain the 'resource id' of the image
		//                - NOT its index in the array
		//            ECU changed to work with a 2d array
		// -------------------------------------------------------------------------
		// 18/01/2015 ECU check for sort mode
		// 23/09/2017 ECU check on 'sortInhibit' which, if true, will prevent the
		//                sort
		// 28/12/2018 ECU add the 'group..' check
		// -------------------------------------------------------------------------
		if (PublicData.storedData.sortByUsage && !sortInhibit && !groupActivities)
		{
			Arrays.sort (gridImages);	
		}	
		// -------------------------------------------------------------------------
		// 26/04/2015 ECU check if the grid images are to be sorted by the legend
		// 17/09/2015 ECU changed to 'ignore the case'
		// -------------------------------------------------------------------------
		if (PublicData.storedData.sortByLegend)
		{
			// ---------------------------------------------------------------------
			// 28/12/2018 ECU changed to call the method rather than having the code
			//                inline
			// ---------------------------------------------------------------------
			SortGridImagesByLegend ();
			// ---------------------------------------------------------------------
		} 	
		// -------------------------------------------------------------------------
		// 09/03/2015 ECU add a list to hold required information
		// -------------------------------------------------------------------------
		ArrayList <Integer> resultsList = new ArrayList <Integer> ();
		// -------------------------------------------------------------------------
		// 09/03/2015 ECU loop for all entries in 'gridImages'
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < gridImages.length; theIndex++)
		{
			// ---------------------------------------------------------------------
			// 09/03/2015 ECU if in 'development mode' then include everything
			// ---------------------------------------------------------------------
			if (theMode)
			{
				// -----------------------------------------------------------------
				// 09/03/2015 ECU development mode
				// -----------------------------------------------------------------
				resultsList.add (theIndex);
			}
			else
			{
				// -----------------------------------------------------------------
				// 09/03/2015 ECU production mode
				//            ECU added check on whether the activity is valid on
				//                this device
				// -----------------------------------------------------------------
				if (gridImages [theIndex].mode == !theMode && gridImages [theIndex].Validate())
					resultsList.add (theIndex);
			}
		}
		// -------------------------------------------------------------------------
		// 09/03/2015 ECU size the results array
		// -------------------------------------------------------------------------
		Integer [][] resultsArray = new Integer [2][resultsList.size()];
		// -------------------------------------------------------------------------
		// 09/03/2015 ECU build the results from the stored information
		// -------------------------------------------------------------------------
		for (int theIndex=0; theIndex < resultsList.size(); theIndex++)
		{
			resultsArray [IMAGE_INDEX][theIndex] 	 = gridImages [resultsList.get(theIndex)].imageId;
			resultsArray [POSITION_INDEX][theIndex] = resultsList.get(theIndex);	 
		}
		// -------------------------------------------------------------------------	 
		// 10/02/2014 ECU check if need to build the cell usage list
		// 18/01/2015 ECU removed cellUsage code
		// -------------------------------------------------------------------------   
		// -------------------------------------------------------------------------
		// 10/02/2014 ECU try and sort the images by usage if require
		// 18/01/2015 ECU removed the cell usage code
		// -------------------------------------------------------------------------
		// -------------------------------------------------------------------------
		return resultsArray;	
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void SortGridImagesByLegend ()
	{
		// -------------------------------------------------------------------------
		// 28/12/2018 ECU created to sort the 'gridImages' by legend - added the
		//                method because it is called more than once
		// -------------------------------------------------------------------------
		Arrays.sort (gridImages,new Comparator<GridImages <?>>() 
		{
			@Override
			public int compare (GridImages <?> gridImage0, GridImages <?> gridImage1) 
			{		
				// -----------------------------------------------------------------
				// 01/04/2017 ECU changed to use new Legend method
				// 18/04/2017 ECU added context as an argument
				// -----------------------------------------------------------------
				return gridImage0.Legend (context).compareToIgnoreCase (gridImage1.Legend (context));  
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void startTheActivity (Intent theIntent)
	{
		// -------------------------------------------------------------------------
		// 06/02/2015 ECU indicate that an activity has been selected
		// -------------------------------------------------------------------------
		startTheActivity (theIntent,StaticData.NO_RESULT);
		// -------------------------------------------------------------------------
	}	
	// -----------------------------------------------------------------------------
	void startTheActivity (Intent theIntent,int theResultCode)
	{
		// -------------------------------------------------------------------------
		// 30/11/2014 ECU indicate that an activity has been selected
		// -------------------------------------------------------------------------
		activityStarted = true;
		// -------------------------------------------------------------------------
		// 28/11/2014 ECU starts the specified intent or just returns the intent
		//                is requested
		// -------------------------------------------------------------------------
		if (actionActivity)
		{
			// ---------------------------------------------------------------------
			// 28/11/2014 ECU start the activity
			// 06/02/2015 ECU put in the check on NO_RESULT
			// 05/07/2018 ECU this is the point at which some action is to be taken
			//                so check for 'positive feedback'
			// ---------------------------------------------------------------------
			if (!PublicData.storedData.positiveFeedback)
			{
				if (theResultCode == StaticData.NO_RESULT)
					startActivity (theIntent);
				else
					startActivityForResult (theIntent,theResultCode);
			}
			else
			{
				// -----------------------------------------------------------------
				// 05/07/2018 ECU positive feedback is required
				// -----------------------------------------------------------------
				// 17/08/2018 ECU before starting the 'positive feedback' then indicate
				//                that any actions are ignored
				// -----------------------------------------------------------------
				ignoreUserActions = true;
				// -----------------------------------------------------------------
				// 05/07/2018 ECU now activate the 'feedback' animation
				// 24/01/2019 ECU pass through the 'positiveFeedback' flag
				// -----------------------------------------------------------------
				PositiveFeedback.UserAction (context,
											 PublicData.storedData.positiveFeedback,
											 switchImageView,theIntent,theResultCode);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 28/11/2014 ECU just return the intent to be started
			// 06/02/2015 ECU pass back the position that was selected
			// 23/09/2017 ECU rather than return the position of the activity, which
			//                could change if 'sort by usage' is in use, then return
			//                the 'legend' instead
			// ---------------------------------------------------------------------
			theIntent.putExtra (StaticData.PARAMETER_LEGEND,gridImages [activeImagePosition].Legend());
			setResult (RESULT_OK,theIntent);	
			finish ();
			// ---------------------------------------------------------------------
		}	
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void SetUsages ()
	{
		// -------------------------------------------------------------------------
		// 01/01/2019 ECU created to set the usages in the original grid image array
		//                to those retrieved from disk
		// -------------------------------------------------------------------------
		// 02/01/2019 ECU read the usages that were previously saved into a local 
		//                array for handling
		// -------------------------------------------------------------------------
		int [] localUsages = (int []) Utilities.readObjectFromDisk (PublicData.projectFolder + getString (R.string.usages_file));
		// -------------------------------------------------------------------------
		if ((localUsages != null) &&
			(localUsages.length == originalGridImages.length))
		{
			// ---------------------------------------------------------------------
			// 01/01/2019 ECU loop through the array
			// ---------------------------------------------------------------------
			for (int theItem = 0; theItem < originalGridImages.length; theItem++)
			{
				// -----------------------------------------------------------------
				// 01/01/2019 ECU set the usage for this element
				// -----------------------------------------------------------------
				originalGridImages [theItem].usage = localUsages [theItem];
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void StoreUsages ()
	{
		// -------------------------------------------------------------------------
		// 01/01/2019 ECU created to build an array of usages corresponding to the
		//                'original' grid images
		// -------------------------------------------------------------------------
		int [] localUsages = new int [GridActivity.originalGridImages.length];
		// -------------------------------------------------------------------------
		// 01/01/2019 ECU loop through the array building up the array of usages
		// -------------------------------------------------------------------------
		for (int theEntry = 0; theEntry < GridActivity.originalGridImages.length; theEntry++)
		{
			// ---------------------------------------------------------------------
			// 01/01/2019 ECU store the usage for the particular image
			// ---------------------------------------------------------------------
			localUsages [theEntry] 
				= GridImages.GetUsage (GridActivity.originalGridImages [theEntry].imageId);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 02/01/2019 ECU write the array to disk
		// -------------------------------------------------------------------------
		Utilities.writeObjectToDisk (PublicData.projectFolder + getString (R.string.usages_file),localUsages);
		// -------------------------------------------------------------------------
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
		 // 05/07/2018 ECU remember the view associated with this 'switch'
		 // ------------------------------------------------------------------------
		 switchImageView = theView;
		 // ------------------------------------------------------------------------
		 // 01/09/2017 ECU if the user is quick then the user could click an icon
		 //                whilst the system is speaking some informative phrases
		 //                which could be annoying - so just flush them out
		 // ------------------------------------------------------------------------
		 TextToSpeechService.Flush ();
		 // ------------------------------------------------------------------------
		 // 25/01/2016 ECU check if the number of clicks has been actioned
		 //            ECU only if the limit is greater than 1 (on a new device when
		 //				   'storedData' has not been initialised then the limit will
		 //                be 0
		 // 06/03/2016 ECU put in the check on position... because when the Timer
		 //				   wants to start an activity then the click counter should
		 //                not be taken into account
		 // 20/03/2017 ECU put in the check on groupActivities
		 // ------------------------------------------------------------------------
		 if (!groupActivities && PublicData.storedData.clickCounter > 1 && (positionToAction == StaticData.NO_RESULT))
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
		 // 01/04/2017 ECU chaned to use new Legend method
		 // 18/04/2017 ECU added context as an argument
		 // ------------------------------------------------------------------------
		 if (PublicData.storedData.acquireStatistics)
		 {
			 Utilities.LogToProjectFile (TAG,gridImages [thePosition].Legend (context));
		 }
		 // ------------------------------------------------------------------------
		 // 28/04/2016 ECU handle any monitoring that has been enabled
		 // 30/03/2017 ECU changed from the position
		 // ------------------------------------------------------------------------
		 MonitorData.sendMonitorData (context,StaticData.MONITOR_DATA_ACTIVITY,activeImages [IMAGE_INDEX][thePosition]);
		 // ------------------------------------------------------------------------ 
		 // 10/02/2014 ECU update the cell usage
		 // 18/01/2015 ECU changed to use the gridImages rather than cellUsage
		 // ------------------------------------------------------------------------
		 gridImages [activeImages [POSITION_INDEX][thePosition]].usage++;
		 // ------------------------------------------------------------------------
		 // 05/01/2019 ECU want to make sure that the display is updated if required
		 // ------------------------------------------------------------------------
		 if (PublicData.storedData.usageDisplay)
		 {
			 gridRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_ADAPTER);
		 }
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
   			case R.drawable.blood_pressure:
   				// -----------------------------------------------------------------
   				// 26/08/2017 ECU added
   				// -----------------------------------------------------------------
   				localIntent = new Intent (context,BloodPressureActivity.class);
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
      		case R.drawable.devices:
      			// -----------------------------------------------------------------
      			// 02/04/2019 ECU added - 'devices' activity
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,DevicesActivity.class);
      			startTheActivity (localIntent,0);
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
      			localIntent.putExtra (StaticData.PARAMETER_FOLDER,PublicData.projectFolder);
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
      			// -----------------------------------------------------------------
      			// 11/06/2017 ECU if started by the timer activity then want to tell
      			//                the activity to start the recorder
      			// -----------------------------------------------------------------
      			if (positionToAction != StaticData.NO_RESULT)
      			{
      				//--------------------------------------------------------------
      				// 11/06/2017 ECU pass through the command to get the activity
      				//                to start recording immediately
      				// -------------------------------------------------------------
      				localIntent.putExtra (StaticData.PARAMETER_RECORDER_START, true);
      				// -------------------------------------------------------------
      			}
      			// -----------------------------------------------------------------
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
  					// ---------------------------------------------------------
  					localIntent.addFlags (Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP); 
  					// -------------------------------------------------------------
      				// 02/06/2017 ECU check if there are any actions in progress
      				//                in which case do not allow the music player
  					// -------------------------------------------------------------
      				if (Validation.musicPlayerValidation ())
      				{
      					// ---------------------------------------------------------
      					// 02/06/2017 ECU start the music player immediately
      					// ---------------------------------------------------------
      					startTheActivity (localIntent);
      					// ---------------------------------------------------------
      				}
      				else
      				{
      					// ---------------------------------------------------------
      					// 02/06/2017 ECU there are actions in progress so do not
      					//                allow
      					// ---------------------------------------------------------
      					Utilities.popToast (getString (R.string.unable_to_start_music_player_actions),true);
      					// ---------------------------------------------------------
      					// 02/06/2017 ECU remember the intent so that it can be started
      					//                when the actions finish
      					// ---------------------------------------------------------
      					PublicData.actionIntent = localIntent;
      					// ---------------------------------------------------------
      					// 03/06/2017 ECU trigger the display of some information to
      					//                inform the user
      					// ---------------------------------------------------------
      					MusicPlayer.refreshImageAdapter ();
      					// ---------------------------------------------------------
      				}
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
      			// 25/06/2013 ECU start the photo album
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
      		case R.drawable.rev_counter:
      			// -----------------------------------------------------------------
      			// 13/06/2019 ECU added - 'rev counter' activity
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,RevCounterActivity.class);
      			startTheActivity (localIntent,0);
      			// -----------------------------------------------------------------
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
      				// --------------------------------------------------------------
      			}
      			else
      			{
      				// -------------------------------------------------------------
      				// 03/03/2016 ECU not in development mode so request the password
      				//                before allowing the settings activity to start
      				// 22/03/2018 ECU changed to use underlying object
      				// --------------------------------------------------------------
      				DialogueUtilitiesNonStatic.textInput (context,
      													  underlyingObject,
      													  context.getString (R.string.enter_password_title),
      													  context.getString (R.string.enter_password_summary),
      													  StaticData.HINT +  context.getString (R.string.enter_password_hint),
      													  Utilities.createAMethod (GridActivity.class,"SettingsPassword",StaticData.BLANK_STRING),
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
      			// 25/01/2016 ECU added - speaking clock - the current time
      			// 20/03/2017 ECU pass through the context as an argument
      			// 24/04/2018 ECU add the 'true' so that text is always displayed
      			//                irrespective of whether the configuration has been
      			//				  set.
      			// -----------------------------------------------------------------
      			Utilities.SpeakingClock (context,true);
      			// -----------------------------------------------------------------
      	    	break;
      	    // =====================================================================
      		case R.drawable.swipe:
      			// -----------------------------------------------------------------
      			// 21/04/2014 ECU created to test swipe screens
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,SwipeActivity.class);
      			startTheActivity (localIntent,0);
      			// -----------------------------------------------------------------
      			break;
      		// =====================================================================
      		case R.drawable.system_information:
      			// -----------------------------------------------------------------
      			// 16/06/2013 ECU added
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,SystemInfoActivity.class);
      			startTheActivity (localIntent,0);
      			// -----------------------------------------------------------------
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
      			// -----------------------------------------------------------------
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
      			// 27/02/2016 ECU include the check on the remote controller server
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
      			// -----------------------------------------------------------------
      			break;
      		// =====================================================================
      		case R.drawable.theft:
      			// -----------------------------------------------------------------
      			// 06/10/2017 ECU created to handle 'theft protection'
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,TheftActivity.class);
      			startTheActivity (localIntent,0);
      			// -----------------------------------------------------------------
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
      			// -----------------------------------------------------------------
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
  				// 28/12/2018 ECU because no activity was started it is necessary to
  				//                force the display to be redrawn correctly
  				// -----------------------------------------------------------------
  				UpdateDisplay ();
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
      			// -----------------------------------------------------------------
  				break;
  			// =====================================================================
      		case R.drawable.upnp:
      			// -----------------------------------------------------------------
      			// 08/09/2016 ECU WeMo control using CyberGarage directly - not
      			//                using Belkin's SDK
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,UPnP_Activity.class);
      			startTheActivity (localIntent,0);
      			// -----------------------------------------------------------------
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
      			// 11/06/2017 ECU if started by the timer activity then want to tell
      			//                the activity to start the recorder using the 
      			//                specified file name
      			// -----------------------------------------------------------------
      			if (positionToAction != StaticData.NO_RESULT)
      			{
      				//--------------------------------------------------------------
      				// 11/06/2017 ECU pass through the command to get the activity
      				//                to start recording immediately
      				// -------------------------------------------------------------
      				localIntent.putExtra (StaticData.PARAMETER_FILE_NAME,Utilities.getAUniqueFileName (StaticData.VIDEO_RECORDER_FILE_DEFAULT));
      				// -------------------------------------------------------------
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
		 // ------------------------------------------------------------------------
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
		// 05/07/2018 ECU remember the view associated with this 'switch'
		// -------------------------------------------------------------------------
		switchImageView = theView;
		// -------------------------------------------------------------------------
		// 01/09/2017 ECU if the user is quick then the user could click an icon
		//                whilst the system is speaking some informative phrases
		//                which could be annoying - so just flush them out
		// -------------------------------------------------------------------------
		TextToSpeechService.Flush ();
		// -------------------------------------------------------------------------
		switch (activeImages [IMAGE_INDEX][thePosition])
		{
			// =====================================================================
			case R.drawable.audio_streaming:
				// -----------------------------------------------------------------
				// 14/01/2018 ECU start audio streaming from the specified device
				// -----------------------------------------------------------------
				if (PublicData.streamingDestination == null)
				{
					// -------------------------------------------------------------
					// 15/01/2018 ECU no device has been allocated - see if one can
					//                be allocated automatically
					// -------------------------------------------------------------
					// 15/01/2018 ECU get a list of compatible devices - excluding
					//                this one
					// -------------------------------------------------------------
					String [] devices = Utilities.deviceListAsArray (false);
					// -------------------------------------------------------------
					if ((devices == null) || (devices.length == 0))
					{
						// ---------------------------------------------------------
						// 15/01/2018 ECU there are no devices to stream from
						// ---------------------------------------------------------
						Utilities.popToastAndSpeak (getString (R.string.no_device_to_listen_to),true);
						// ---------------------------------------------------------
					}
					else
					if (devices.length == 1)
					{
						// ---------------------------------------------------------
						// 15/01/2018 ECU there is only one device so assume that
						//                that will be the source of the audio stream
						// ---------------------------------------------------------
						PublicData.streamingDestination = Devices.returnIPAddress (devices [0]);	
					}
					else
					{
						// ---------------------------------------------------------
						// 15/01/2018 ECU there are a number of devices so tell the
						//                user to use 'TCP Utilities' to select the
						//                one that is required
						// ---------------------------------------------------------
						Utilities.popToastAndSpeak (getString (R.string.no_device_to_listen_to_use_tcp),true);
						// ---------------------------------------------------------
					}
				}
				// -----------------------------------------------------------------
				// 15/01/2017 ECU if the stream has been set then take the necessary
				//                action
				// -----------------------------------------------------------------
				if (PublicData.streamingDestination != null)
				{
					// -------------------------------------------------------------
					// 15/01/2018 ECU toggle the action to be taken
					// -------------------------------------------------------------
					PublicData.audioStreaming = !PublicData.audioStreaming;
					// -------------------------------------------------------------
					// 15/01/2018 ECU tell the remote device what to do
					// -------------------------------------------------------------
					Utilities.socketMessagesSendMessageType (this,
															 PublicData.streamingDestination,
															 PublicData.socketNumberForData, 
															 ((PublicData.audioStreaming) ? StaticData.SOCKET_MESSAGE_START_STREAM 
																	                      : StaticData.SOCKET_MESSAGE_STOP_STREAM));
					// -------------------------------------------------------------
					// 15/01/2018 ECU try and get the legends updated
					// -------------------------------------------------------------
					gridRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_ADAPTER);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				break;
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
			case R.drawable.blood_pressure:
				// -----------------------------------------------------------------
				// 27/08/2017 ECU want to show currently stored pressures and weights
				// -----------------------------------------------------------------
				localIntent = new Intent (context,BloodPressureActivity.class);
				localIntent.putExtra (StaticData.PARAMETER_DISPLAY,true);
				startTheActivity (localIntent,0);
				// -----------------------------------------------------------------
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
      			localIntent.putExtra (StaticData.PARAMETER_DAY,true);
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
      		case  R.drawable.music:
      			// -----------------------------------------------------------------
      			// MUSIC PLAYER TIMER
      			// ==================
      			// 02/01/2018 ECU start the activity to set timer for the music
      			//                player
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,MusicPlayerTimerActivity.class);
  				startTheActivity (localIntent,0);
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
      			// 09/02/2014 ECU added - speaking clock
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,SpeakingClockActivity.class);
      	    	startTheActivity (localIntent,0);
      			// -----------------------------------------------------------------
      	    	break;
      	    // =====================================================================
      		case R.drawable.shopping:
      			// -----------------------------------------------------------------
      			// 24/04/2018 ECU added - do some shopping
      			// -----------------------------------------------------------------			
      			localIntent = new Intent (context,ShoppingActivity.class);
      			localIntent.putExtra (StaticData.PARAMETER_SHOP,true);
      	    	startTheActivity (localIntent,0);
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
      		case R.drawable.theft:
      			// -----------------------------------------------------------------
      			// 06/10/2017 ECU created to handle 'theft protection' configuration
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,TheftActivity.class);
      			localIntent.putExtra (StaticData.PARAMETER_CONFIGURATION,true);
      			startTheActivity (localIntent,0);
      			break;
      		// =====================================================================
      		case R.drawable.timer:
      			// -----------------------------------------------------------------
      			// 04/02/2018 ECU created to handle 'countdown timer'
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,CountdownTimerActivity.class);
      			startTheActivity (localIntent,0);
      			break;
      		// =====================================================================
      		case  R.drawable.tone_generator:
      			// -----------------------------------------------------------------
      			// 18/08/2015 ECU added
      			// 08/08/2019 ECU handle the 'mosquito' option
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,ToneGeneratorActivity.class);
      			Tone [] tones 	= {
										// -----------------------------------------
										// 18/08/2015 ECU five notes from Close
      									//                Encounters Of The Third Kind
										// -----------------------------------------
										new Tone ("G5",500),
										new Tone ("A5",500),
										new Tone ("F5",500),
										new Tone ("F4",500),
										new Tone ("C5",1000)
										// -----------------------------------------
									};
      			// -----------------------------------------------------------------
      			localIntent.putExtra (StaticData.PARAMETER_TONES,new Tones (tones));
      			startTheActivity (localIntent,StaticData.REQUEST_CODE_FINISH);
      			// -----------------------------------------------------------------
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
      		case R.drawable.upnp:
      			// -----------------------------------------------------------------
      			// 16/04/2017 ECU added to process all UPnP devices
      			// -----------------------------------------------------------------
      			localIntent = new Intent (context,UPnP_Activity_All.class);
      			startTheActivity (localIntent,0);
      			// -----------------------------------------------------------------
  				break;
  			// =====================================================================
      		case R.drawable.video:
				// -----------------------------------------------------------------
				// 15/09/2017 ECU start up the video streaming activity
				// 27/09/2017 ECU add check on VIDEO_STREAMING
				// -----------------------------------------------------------------
				if (StaticData.VIDEO_STREAMING)
				{
					localIntent = new Intent (context,VideoStreamingActivity.class);
					startTheActivity (localIntent,0);
				}
				// -----------------------------------------------------------------
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
	void TidyUpBeforeExiting ()
	{
		// -------------------------------------------------------------------------
		// 02/01/2019 ECU created to perform any tasks that need to be completed
		//                before this activity ends
		// -------------------------------------------------------------------------
		// 02/01/2019 ECU generate the array with how the activities have been used
		// -------------------------------------------------------------------------
		StoreUsages ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void UpdateDisplay ()
	{
		// -------------------------------------------------------------------------
		// 28/12/2018 ECU created so that the 'onResume' event method can be called
		//                if sorting by usage is enabled
		// -------------------------------------------------------------------------
		if (PublicData.storedData.sortByUsage)
		{
			// ---------------------------------------------------------------------
			// 28/12/2018 ECU call the normal 'onResume' method
			// ---------------------------------------------------------------------
			onResume ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
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
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 01/10/2014 ECU zoom out
			// ---------------------------------------------------------------------
			if (iconsPerRow < 10)
				iconsPerRow++;
			// ---------------------------------------------------------------------
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
	public boolean onDown (MotionEvent motionEvent) 
	{
		// -------------------------------------------------------------------------
		// 17/08/2018 ECU Note - want the scroll action to take place
		// -------------------------------------------------------------------------
		return false;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	@Override
	public boolean onFling (MotionEvent motionEvent1,
							MotionEvent motionEvent2, 
							float velocityX,
							float velocityY) 
	{
		// -------------------------------------------------------------------------
		// 17/08/2018 ECU want the user action to be processed
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
	public boolean onScroll (MotionEvent motionEvent1, 
							 MotionEvent motionEvent2, 
							 float distanceX,
							 float distanceY) 
	{
		// -------------------------------------------------------------------------
		// 17/08/2018 ECU added the 'ignore' check
		// -------------------------------------------------------------------------
		// 17/08/2018 ECU Note - want the scroll action to take place
		// -------------------------------------------------------------------------
		return false;
		// -------------------------------------------------------------------------
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
		// -------------------------------------------------------------------------
		// 17/08/2018 ECU Note - want the scroll action to take place
		// -------------------------------------------------------------------------
		return false;
		// -------------------------------------------------------------------------
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
        			// 27/07/2017 ECU put in the check on whether the button exists
        			// -------------------------------------------------------------
        			if (notificationButton != null)
        			{
        				notificationButton.clearAnimation();
        				notificationButton.setVisibility (View.GONE);
        			}
        			// ------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_NOTIFICATION_START:
        			// -------------------------------------------------------------
        			// 13/07/2016 ECU created to set up the notification button and
        			//                start the animation
        			//            ECU the object in the message indicates
        			//                   true ..... start animation
        			//                   false .... no animation
        			// 11/06/2017 ECU put in the check on whether the button has been
        			//                defined yet
        			// -------------------------------------------------------------
        			if (notificationButton != null)
        			{
        				if (notificationButton.getVisibility() == View.GONE)
        					notificationButton.setVisibility (View.VISIBLE);
        				// ---------------------------------------------------------
        				// 13/07/2016 ECU now decide if the animation is to start
        				// ---------------------------------------------------------
        				if ((Boolean) theMessage.obj)
        					Utilities.AnimateFlashImageView (notificationButton,1000,Animation.INFINITE);
        				// ---------------------------------------------------------
        				notificationButton.setOnClickListener (notificationButtonOnClickListener);
        				// ---------------------------------------------------------
        			}
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
        		case StaticData.MESSAGE_USER_ACTIONS:
        			// -------------------------------------------------------------
        			// 17/08/2018 ECU indicate that 'positive feedback' animation
        			//				  has finished and that user actions can be
        			//                processed again
        			// -------------------------------------------------------------
        			ignoreUserActions = false;
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
            // ---------------------------------------------------------------------
        }
    };
	// =============================================================================
    
    // =============================================================================
    // =============================================================================
    // 03/03/2016 ECU declare methods used in dialogues
    // =============================================================================
    // =============================================================================
	public void SettingsPassword (String thePassword)
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

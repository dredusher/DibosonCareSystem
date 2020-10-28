package com.usher.diboson;

import android.app.PendingIntent;
import android.content.Intent;
import android.view.KeyEvent;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class StaticData 
{
	// =============================================================================
	// 01/09/2015 ECU created to add static-ally defined variables
	// =============================================================================
	
	// =============================================================================
	public static final int			ACTIONS_TIMEOUT				= 60 * 1000;
																			// 28/11/2016 ECU added
	public static final int			ACTIVITY_LONG_OFFSET		= 0x1000;	// 07/02/2018 ECU added
	public static final int			ACTIVITY_LONG_OFFSET_MASK	= 0x1FFF ^ ACTIVITY_LONG_OFFSET;
																			// 07/02/2018 ECU added
    public static final String		ALARM_DATE_FORMAT			= "EEEE dd MMM yyyy";
    																		// 04/03/2016 ECU added
    public static final int			ALARM_PENDING_INTENT_FLAGS	= Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_UPDATE_CURRENT;
    																		// 09/05/2020 ECU flags used when setting up the
    																		//                pending intent for an exact alarm
	public static final String		ALARM_TIME_FORMAT			= "HH:mm:ss";
																			// 04/03/2016 ECU added
	public static final String		ALEXA_WAKE_WORD				= "alexa";	// 13/02/2020 ECU added
	// -----------------------------------------------------------------------------
	// 10/02/2018 ECU the following are used when assigning package names for
	//                'contacts' and 'mail'
	// -----------------------------------------------------------------------------
	public static final String		APP_DELIMITER				= "\n      ";
																			// 10/02/2018 ECU added
	public static final int			APP_LABEL					= 0;
	public static final int         APP_PACKAGE_NAME_NEW		= APP_LABEL + 1;
	public static final int         APP_PACKAGE_NAME_OLD		= APP_LABEL;
	// -----------------------------------------------------------------------------
	public static final String		APPOINTMENT_HEADER			= "Appointment";
																			// 24/10/2016 ECU added - header used on appointment audio
																			//   					  notes
	public static final int    		BACK_KEY_COUNTER	= 5;				// 20/02/2014 ECU number of times the 'back' key must
																			//                be pressed before the activity is
																			//                exited
	public static final String		BACKUP_EXTENSION			= ".Backup";// 29/08/2020 ECU added - extension used when making a
																			//                        backup of a file
	public static final String		BLANK_STRING				= "";		// 14/12/2016 ECU added
	public static final boolean		BLUETOOTH_CONNECTOR_SERVICE = true;		// 07/04/2020 ECU added
	public static final int	  		BLUETOOTH_DISCOVERY_TIME	= (1000 * 60);
    																		// 10/01/2014 ECU added - delay between discoveries in
																			//                        milliseconds
	public static final int		    BLUETOOTH_DROPOUT_NUMBER	= 1;        // 25/11/2016 ECU added - see Note in Carer
	public static final String      BLUETOOTH_NO_NAME			= "<no name set>";
																			// 21/08/2020 ECU added - the name of a bluetooth device
																			//                        has not been set
	public static final boolean 	BLUETOOTH_SERVICE		 	= true; 	// 31/12/2013 ECU added - whether bluetooth service in use
	public static final boolean     BLUETOOTH_TRACKING			= true;		// 15/04/2020 ECU added - whether bluetooth tracking in use
	public static final int         BLUETOOTH_TRACKING_CONTACT_TIME
																= 10;		// 22/07/2020 ECU added - contact time
	public static final int			BLUETOOTH_TRACKING_LOST_CONTACT_TIME
																= 10;		// 22/07/2020 ECU added - contact time
	public static final int 		BROADCAST_PORT				= 8015;		// 22/08/2013 ECU added - for multicasts
																			// 09/04/2016 ECU changed the name from MULTICAST_
																			// 25/05/2020 ECU changed from 8014
	public static final int 		BROADCAST_TIMEOUT			= 0;		// 22/08/2013 ECU added - timeout in millisecs when waiting
																			//                for a multicast message. 0 means that there
																			//                is no time out
																			// 09/04/2016 ECU changed from MULTICAST_
	public static final int 		BUFFER_SIZE_MAX 			= 1000000;	// 10/08/2013 ECU added - maximum buffer size used in socket handling
	public static final int         CARE_VISIT_WARNING_PERIOD	= 5;    	// 02/10/2016 ECU advance warning in minutes of a carer visit
	public static final String		CARER_REPLACEMENT			= "<CARER>";// 30/03/2016 ECU carer replacement string
	public static final String		CARER_MAIN_REPLACEMENT		= "CARER_MAIN";	
																			// 22/02/2018 ECU carer string
	public static final long        CARER_VISIT_SLOP_TIME		= 30 * 60 * 1000;
																			// 05/10/2016 ECU this is the time in mS on either
																			//                side of a visit that it is
																			//                recognised as 'scheduled'
	public final static String 		CARRIAGE_RETURN				= "\r";		// 19/10/2019 ECU added
	public static final int			CLONE_FILE_SIZE				= 1000000;	// 07/01/2016 ECU added - default clone file size
	public static final int 		CLONE_REQUEST_CODE			= 4352;		// 06/04/2014 ECU added
	public final static int    		COLUMNS_DEFAULT				= 2;		// 16/02/2014 ECU added
																			// 17/09/2020 ECU moved here from GridActivity
	public static final String 		COMMAND_DEFAULT_CLASS	  	= "InvadeActivity";
																			// 29/10/2014 ECU added - see SMSHandler	
	public static final String		COMMENT_INTRODUCER			= "//";		// 11/11/2016 ECU added - start of a comment line
	public static final String      COPYRIGHT_CODE				= "\u00a9";	// 09/03/2017 ECU added - code used for copyright symbol
	public static final String      COPYRIGHT_CODE_HTML			= "&#169;";	// 09/03/2017 ECU added - html code used for copyright symbol
	public static final int			DAYS_PER_WEEK 				= 7;
	public static final int			DEFAULT_BACKGROUND_COLOUR	= R.color.silver;
																			// 04/10/2016 ECU used for displayed rows
	public static final String		DEFAULT_DATE				= "01/01/1900";
																			// 09/11/2015 ECU added
	public static final String		DEFAULT_DEVELOPER_NAME		= "ECU";	// 06/11/2015 ECU added
	public static final int 		DISCOVERY_TIMEOUT			= 10;		// 05/08/2013 ECU added - the timeout in millisecs
																			//                when doing a network discover
																			// 20/09/2013 ECU changed from 5000
																			// 12/11/2016 ECU changed to be in seconds
	public static final int			DRAWABLE_WAIT_TIME			= 10*1000;	// 06/03/2016 ECU the default time that a drawable
																			//                image will be displayed
	public static final int			DRAWABLE_WAIT_TIME_SHORT	= 1000;		// 09/10/2016 ECU the default short time that a drawable
																			//                image will be displayed
	public static final int			DOSE_RESPONSE_PROMPT		= 60*1000;	// 20/12/2015 ECU when the dose prompt is display then
		 																	//                a prompt will be given after this
																			//                delay to remind the user. This will
																			//                be repeated until the user responds.
	public static final String 		DYNAMIC_IP_ADDRESS		  	= "bt2994.myfoscam.org";
	                                                                		// 25/04/2015 ECU added 
	public static final long		EMAIL_MAX_ATTACHMENT_SIZE	= 10000000;	// 22/06/2017 ECU added
	public static final int			EPG_ADVANCE_COUNTER			= 5;		// 30/10/2015 ECU the number of minutes in advance
																			//                of EPG alarm that warning will
																			//                appear
	public static final long        EPG_ADVANCE_WARNING			= (EPG_ADVANCE_COUNTER * 60 * 1000);
																			// 15/10/2015 ECU the time (in mS) when an advance
																			//                warning of an impending EPG
																			//                'selection' occurs
																			// 30/10/2015 ECU changed to use ... COUNTER
	public static final String		EPG_CHANNELS_URL			= "http://xmltv.radiotimes.com/xmltv/channels.dat";
																			// 17/09/2015 ECU URL of channels used in the EPG
	public static final long		EPG_REMINDER_GAP			= 100;		// 26/01/2017 ECU added - see notes in ShowEPGActivity
	public static final String      EPG_TAB_DATE_FORMAT			= "EEEE    dd MMMM yyyy    HH:mm:ss";
																			// 21/01/2017 ECU added - date format on EPG tab
																			// 25/01/2017 ECU changed from
																			// 					"EEEE\ndd/MM/yyyy HH:mm:ss"
	public static final int			EPG_FIELD_COUNT				= 23;		// 18/09/2015 ECU the number of fields in each
																			//                EPG entry
	public static final int			EPG_RETRY_LIMIT				= 3;		// 27/06/2016 ECU number of times that try to
																			//                get EPG data for a channel
	public static final String		EPG__URL					= "http://xmltv.radiotimes.com/xmltv/%d.dat";
																			// 17/09/2015 ECU URL to get EPG for a particular
																			//                channel - %d will be changed by
																			//                the channel number
	public static final String		EXTENSION_ACTIONS			= ".actions";
																			// 11/04/2020 ECU added
	public static final String		EXTENSION_AUDIO				= ".wav";	// 14/11/2016 ECU added
	public static final String		EXTENSION_DOCUMENT			= ".pdf";	// 18/10/2016 ECU added
	public static final String		EXTENSION_MAP				= ".kml";	// 09/02/2016 ECU added
	public static final String 		EXTENSION_MUSIC			 	= ".mp3";	// 22/01/2016 ECU added
	public static final String 		EXTENSION_PHOTOGRAPH	 	= ".jpg";
	public static final String		EXTENSION_TEXT				= ".txt";	// 31/10/2015 ECU added
	public static final String		EXTENSION_VIDEO				= ".mp4";	// 21/05/2016 ECU added
	public static final String []	EXTENSIONS_VIDEO			= new String [] {EXTENSION_VIDEO,".wmv"};
																			// 23/05/2017 ECU added
	public static final String		EXTERNAL_SD_CARD			= "{EXTERNAL_SD_CARD}";
																			// 30/11/2018 ECU added
	public static final String		FAKE_MAC_ADDRESS			= "02:00:00:00:00";
																			// 27/07/2016 ECU the 'fake' MAC address - started
	                                                                        //                to be returned after version

																			//                MARSHMALLOW
	// ---------------------------------------------------------------------------------------------------------------------
	// 14/10/2020 ECU indicate the status of 'stored data' reads
	// ---------------------------------------------------------------------------------------------------------------------
	public static final int			FILE_DOES_NOT_EXIST			=  0;
	public static final int			FILE_READ_OK				=  FILE_DOES_NOT_EXIST + 1;
	public static final int			FILE_READ_NOT_OK			=  FILE_READ_OK + 1;
	// ---------------------------------------------------------------------------------------------------------------------
	// 16/11/2019 ECU added device_name_file
	// 14/10/2020 ECU removed 'stored_data_file'
	// ---------------------------------------------------------------------------------------------------------------------
	public static final FileOnDisk [] 
									FILES_TO_READ_FROM_DISK   	= {																
																	new FileOnDisk (R.string.agencies_file,true),
																	new FileOnDisk (R.string.appointments_file,true),
																	new FileOnDisk (R.string.appointment_types_file,true),
																	new FileOnDisk (R.string.bar_code_data,true),
																	new FileOnDisk (R.string.care_plan_file,true),
																	new FileOnDisk (R.string.carers_file,true),
																	new FileOnDisk (R.string.device_names_file,true),
																	new FileOnDisk (R.string.email_details_file,true),
																	new FileOnDisk (R.string.medication_details_file,true),
																	new FileOnDisk (R.string.music_player_data,false),
																	new FileOnDisk (R.string.named_actions_file,true),
																	new FileOnDisk (R.string.patient_details_file,true),
																	new FileOnDisk (R.string.shopping_file,true),
																	// --------------------------------------------------
																	// 14/10/2020 ECU removed the reading of 'stored data'
																	//                because this is performed elsewhere
																	// --------------------------------------------------
																	// new FileOnDisk (R.string.stored_data_file,false),
																	// --------------------------------------------------
															  		new FileOnDisk (R.string.voice_command_phrases_file,true),
															  		// --------------------------------------------------
															  		// 06/12/2016 ECU put files here which are dependent
															  		//                on other files being read
															  		// --------------------------------------------------
															  		new FileOnDisk (R.string.care_visit_tasks_file,true)
																	// --------------------------------------------------
																  };
																			// 03/02/2014 ECU added - list of files to read from
																			//                disk
																			// 01/05/2015 ECU added music player data which does
																			//                not need to be synchronised across
																			//                devices
																			// 21/05/2016 ECU added the voice command phrases file
																			// 03/08/2016 ECU added named actions
																			// 06/12/2016 ECU added care visit tasks
																			// 15/07/2017 ECU deleted
																			// 					new FileOnDisk (R.string.visits_file,true)
	public static final int			FIRST_IN_LIST				= 0;		// 25/04/2018 ECU indicate (for clarity) first position in a list
	public static final String		FLOAT						= "float";	// 10/12/2015 ECU indicate 'float' units
	public static final String		HINT						= "HINT";	// 03/10/2015 ECU added to indicate message for a TextView
																			//                is to regarded as a hint
	public static final int 		IMAGE_SAMPLE_SIZE			= 4; 		// 17/01/2014 ECU added
																			// 19/02/2020 ECU changed from 2 because of 'memory issues' on
																			//                low capacity devices like the HTC WildFire
	public static final String      INDENT						= "     ";	// 24/04/2019 ECU added
	public static final String      INDENTx2					= INDENT + INDENT;
																			// 18/04/2020 ECU added
	public static final String      INDENTx3					= INDENT + INDENT + INDENT;
																			// 18/04/2020 ECU added
	public static final String		INSET						= "               ";
																			// 29/11/2017 ECU added - used when printing medication detail
																			//                        to indicate how much a title is to
																			//                        be inset (indented)
	public static final int 		INTEGER_SIZE				= Integer.SIZE/Byte.SIZE;
																			// 10/08/2013 ECU added - number of bytes needed
																			//                to store an integer in byte []
	public static final int		    INTER_DIGIT_DELAY			= 500;		// 14/10/2015 ECU added - the delay (in mS) between
																			//                digits sent to the remote bluetooth
																			//                device
	public static final boolean     INVOKE_STATIC				= false;	// 23/03/2018 ECU this is worked in conjunction with
																			//                Utilities.invokeMethod ... if true
																			//                then the method being invoked must be
																			//                static. If false then does not have to
																			//                be static.
	public static final String		IP_ADDRESSES_FILE			= "IPAddressesFile";
																			// 11/11/2016 ECU added - holds IP addresses for discovery
	public static final String      LEGEND_INDENT				= "   ";	// 21/07/2017 ECU added - indent used on three line buttons
	public static final String      LEGEND_SEPARATOR			= StaticData.NEWLINE + StaticData.INDENT;
																			// 20/07/2020 ECU added - separator between legend and 'long' legend
	public static final boolean		LIFE_CYCLE					= true;		// 13/03/2018 ECU added - life cycle testing
	public static final int			LIGHT_PERIOD_DEFAULT		= 60;		// 02/03/2015 ECU initial light counter default
																			// 06/03/2015 ECU changed to be _PERIOD (see NOTES)
	public static final byte		LINE_TERMINATOR				= 0x0A;		// 16/04/2016 ECU text line terminator
	public static final float		LIQUID_TOLERANCE			= 10;		// 18/05/2016 ECU added
	public static final String		LOCAL_HOST				  	= "127.0.0.1";
																			// 22/09/2013 ECU added - TCP localhost
	public static final String 		LOGCAT_COMMAND			 	= "logcat -d -v time *:I";
																			// 22/03/2014 ECU added - LogCat command
	public static final String		LOGCAT_OUTPUT_FILE			= "LogCat";	// 05/10/2015 ECU added - output file for LogCat
	public static final int			MAP_ZOOM               	= 18;		// 14/10/2020 ECU added
	public static final int 		MAXIMUM_RETRIES				= 3;		// 15/08/2013 ECU added - max number of retransmissions
	public static final int			MEDICATION_INPUT_LENGTH		= 3;		// 26/03/2017 ECU added - number of characters after which
	       																	//                a check for existing medication will
																			//				  be checked
	public static final long 		MILLISECONDS_PER_DAY	 	= (60l * 1000l) * 60l * 24l;
																			// 08/01/2014 ECU added - milliseconds in a day
	public static final long 		MILLISECONDS_PER_HOUR		= (60l * 1000l) * 60l;
																			// 10/01/2014 ECU added - milliseconds per hour
	public static final int 		MILLISECONDS_PER_MINUTE		= (60 * 1000);
																			// 08/01/2014 ECU added - milliseconds in a minute
	public static final int			MILLISECONDS_PER_SECOND		= 1000;		// 08/07/2020 ECU milliseconds per second
	public static final int 		MINUTES_PER_DAY				= (24*60);	// 21/11/2014 ECU added - minutes in a day
	public static final String		MONO_SPACED					= "<MONO_SPACED>";
																			// 13/01/2017 ECU added - to introduce what follows is
																			//                        to be monospaced
	public static final int			MOSQUITO_REPEATS			= 200;		// 08/08/2019 ECU number of tone repeats
	public static final int			MTU							= 1480;		// 17/01/2016 ECU added - the maximum transmission unit
	public static final String		MUSIC_GENRE_END				= ")";		// 09/01/2017 ECU added - see TrackDetails class
	public static final String		MUSIC_GENRE_START			= "(";		// 09/01/2017 ECU added - see TrackDetails class
	public static final String		MUSIC_ROOT_DIRECTORY		= "/storage";
																			// 07/10/2017 ECU root for searching for music
	public static final long		NANOSECONDS_PER_MILLISECOND	= 1000000l;	// 10/12/2015 ECU added
	public static final long		NANOSECONDS_PER_SECOND		= 1000000000l;
																			// 10/12/2015 ECU added
	public static final boolean     NAVIGATION_BAR_SHOW	        = true;		// 03/01/2020 ECU added - if navigation bar is to be shown
	public static final String 		NETWORK_MASK				= "255.255.255.0";
																			// 19/01/2015 ECU added - default subnet mask
	public static final String		NETWORK_MASK_CLASS_A		= "255.0.0.0";
																			// 10/11/2016 ECU added
	public static final String		NETWORK_MASK_CLASS_B		= "255.255.0.0";
																			// 10/11/2016 ECU added
	public static final String		NETWORK_MASK_CLASS_C		= "255.255.255.0";
																			// 10/11/2016 ECU added
	public final static String 		NEWLINE	 					= "\n";		// 16/04/2016 ECU moved here from SystemInfoActivity
	public final static char 		NEWLINE_CHAR				= '\n';		// 27/07/2019 ECU specify the new line character
	public final static String 		NEWLINE_REPLACEMENT			= "<NEWLINE>";
																			// 22/01/2020 ECU specify the new line replacement
	public final static String 		NEWLINEx2 					= "\n\n";	// 13/05/2017 ECU added
	public static final int			NFC_TIMEOUT					= 5000;		// 17/06/2016 ECU timeout when checking presence of a device
	public static final	Method		NO_HANDLING_METHOD			= null;		// 09/04/2018 ECU indicate the action is not to handled by 
																			//                a specified method
	public static final int 		NO_RESULT                   = -1;		// 17/06/2013 ECU added as a default setting
	public static final int 		NOT_SET	                    = -1;		// 26/07/2017 ECU added to indicate a variable not set
	public static final String		NOTIFICATION_CHANNEL_ID		="DCS_id";	// 05/08/2020 ECU added - the channel ID for notifications
																			//                   needed for >= Oreo
	public static final CharSequence
									NOTIFICATION_CHANNEL_NAME	= "DCS_name";
																			// 05/08/2020 ECU added - the channel ID for notifications
																			//                   needed for >= Oreo
	public static final int 		NTP_REFRESH_TIME			= 60;		// 12/08/2013 ECU added - how often the current time
																			//                is refreshed in minutes
	public static final int 		NTP_TIMEOUT					= 10000;	// 12/08/2013 ECU the timeout in millisecs when trying
																			//                to contact the NTP server
	public static final String 		NTP_SERVER				  	= "time.google.com";
																			// 12/08/2013 ECU the NTP server
																			// 13/04/2020 ECU changed from 'uk.pool.ntp.org'
	public static final int 		ONE_SECOND				    = 1000;		// 22/02/2014 ECU one second as milliseconds
	// -----------------------------------------------------------------------------
	// 12/11/2016 ECU declare the REGEX for validating an IP address as it is
	//                input - used by DialogueUtilities
	// -----------------------------------------------------------------------------
	@SuppressWarnings("Annotator")
	public static final Pattern 	PARTIAl_IP_ADDRESS =
		          Pattern.compile ("^((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])\\.){0,3}"+
		                           "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])){0,1}$"); 
	// -----------------------------------------------------------------------------
	public static final String		PATIENT_REPLACEMENT			= "PATIENT";	// 22/02/2018 ECU patient string
	public static final int 		PLAY_TIMEOUT				=  10;  		// 20/08/2013 ECU added - time to wait when a music file has
																			//				  been received (in seconds)
	public static final float       POUNDS_PER_KILO				=  2.2046226218f;
																			// 26/08/2017 ECU added - pounds in a kilogram
	public static final String		PROJECT_FOLDER				= "{PROJECT_FOLDER}";
																			// 09/05/2020 ECU added
	public static final String 		PROJECT_FOLDER_DEFAULT      = "/storage/emulated/0/DibosonCareSystem/";
																			// 06/09/2020 ECU added
	public static final String		RESOURCE_RAW				= "raw";	// 05/06/2017 ECU added
	public static final int			RESTART_INTERRUPT			= 60000;	// 18/10/2014 ECU added - the time in milliseconds when
																			//                the interrupt will be actioned after the
																			//                app has been restarted
	public static final int			RESTART_TIME				= 10000;	// 18/10/2014 ECU added - the time in milliseconds when
																			//                the application will be restarted
																			// 03/10/2015 ECU changed to 10 seconds
	public static final String 		ROOT_DIRECTORY			  	= "/";		// 07/04/2015 ECU added - root directory of whole system
	public static final int	    	SCAN_LIMIT				 	= 15;     	// 31/07/2013 ECU limit when scanning the network
	public static final boolean 	SCAN_METHOD               	= false;  	// 31/07/2013 ECU added true = use ICMP check
																			//                      false = try socket connection to socketNumber
	public static final int			SCROLL_FIELD_LINES			= 10;		// 27/07/2019 ECU added - poptoast field length
	public static final int			SCROLL_FIELD_LINES_MAX		= 25;		// 27/07/2019 ECU added - poptoast maximum field length
	public static final String		SEARCH_ALL					= "";		// 18/11/2017 ECU added - used in TVEPGFragment and associated
																			//                classes
	public static final boolean		SENSOR_SERVICE		 	  	= true;   	// 02/03/2015 ECU added - if sensor service in use
	public final static String		SEPARATOR 					= "===================================\n";
																			// 02/03/2017 ECU moved here from SystemInforActivity
	public final static String		SEPARATOR_LOWER				= "-----------------------------------\n";
																			// 02/03/2017 ECU moved here from SystemInforActivity
	public static final boolean 	SERVER_SERVICE_FOREGROUND 	= false; 	// 08/04/2014 ECU whether SERVER thread is to be kept in
																			//                foreground
	public static final String		SETTINGS_PASSWORD			= "diboson";
																			// 03/03/2016 ECU added - password to gain entry
																			//                to the settings activity
	public static final long		SHARED_PREFERENCES_DEFAULT  = -1l;	
																			// 02/03/2017 ECU value used when the app is started
	public static final String		SHARED_PREFERENCES_START    = "SP_start";	
																			// 02/03/2017 ECU key used when the app is started
	public static final int			SLIDE_SHOW_DELAY			= 2000;		// 13/11/2016 ECU added - default delay between slides
	public static final int			SLIDE_SHOW_FLING			= 500;		// 13/11/2016 ECU added - change in Y that changes slide
																			//                speed
	public static final int			SLIDE_SHOW_INCREMENT		= 200;		// 13/11/2016 ECU added - increment in slide delay
	public static final int			SLIDE_SHOW_LOWER_LIMIT		= 200;		// 13/11/2016 ECU added - fastest that slides can be shown
	public static final String      SMS_BROADCAST				= "Broadcast ";
																			// 18/10/2019 ECU added - cause actions to be processed on All
	                                                                        //                        attached devices
	public static final String		SMS_COMMAND					= "Command ";
																			// 18/10/2019 ECU added - cause actions in an SMS message
																			//                        to be actioned
	public static final String		SMS_INVADE					= "Invade"; // 18/10/2019 ECU added - puts the received phone into
																			//                        'invaded' mode
	public static final int 		SOCKET_CHUNK_SIZE           = 10000;	// 30/07/2013 ECU added
	public static final int 		SOCKET_DATA_OFFSET			= 3;		// 19/08/2013 ECU added - this is the offset into
																			//				  the socketHeader array where
																			//                socket data is stored
	public static final int 		SOCKET_TYPE_OFFSET			= 2;		// 11/08/2013 ECU added - this is the offset into
																			// 				  the socketHeader array where the 
																			//				  message type will be stored
	public static final boolean		SORT_LIST					= true;		// 02/01/2020 ECU added - to indicate a list is to
	                                                                        //                sorted
	public static final String		SPACE_REPLACEMENT			= "<SPACE>";// 06/01/2016 ECU space replacement string
	public static final String		SPACE_STRING				= " ";		// 24/04/2019 ECU added
	public static final String		SPOKEN_PHRASE_COMPLETED		= "completed";
																			// 22/05/2016 ECU the tts engine has completed the
							
	public static final float		STARTUP_SWIPE_VELOCITY		= 2000;		// 11/09/2015 ECU velocity which will cause settings
																			//                to be displayed
																			// 05/11/2016 ECU changed from 8000
	public static final Object		STATIC_METHOD				= null;		// 21/03/2018 ECU added to indicate that a method to be
																			//                invoked is static
	public static final String		STORAGE_PATH				= "/storage";
																			// 30/11/2018 ECU path to main storage path
	// -----------------------------------------------------------------------------
	// 19/03/2017 ECU Note - The 'strict mode' was introduced at API 9 (see
	//                       documentation_notes in 'raw') to highlight programming
	//                       code which is deemed to be 'not best practice', e.g.
	//                       accessing the disk or network from the UI. This app
	//                       is 'user controlled' so that sometimes tasks are best 
	//   					 performed on the UI because the user cannot continue 
	//                       those tasks are complete. Hence the reason for having
	//                       the variable set 'false' as 'the norm'
	// -----------------------------------------------------------------------------
	public static final boolean 	STRICT_MODE				  	= false;	// 02/04/2014 ECU added for development checking
	public static final int			SYSTEM_INFO_MAX_LINES		= 3000;		// 16/04/2014 ECU added - max lines for system information
																			//                textview field
	public static final int 		SYSTEM_INFO_REFRESH_RATE	= 5000;		// 20/08/2013 ECU added - the rate at which the system info
																			//				  will be refreshed in millisecs
																			// 04/10/2015 ECU changed from 1000
	public static final String		TAB							= "\t";		// 07/06/2017 ECU added - the 'tab' character
	public static final String		TIME_FORMAT					= "%02d:%02d";
																			// 03/12/2019 ECU added
	public static final boolean     TITLE_BAR_SHOW		        = true;		// 03/01/2020 ECU added - if title bar is to be shown
	public static final int			TONE_DURATION_MAXIMUM		= 40000;	// 04/11/2015 ECU added - maximum duration available in the
																			//                tone generator activity
	public static final int			TONE_DURATION_MAXIMUM_MOSQUITO
																= 10000;		// 08/08/2019 ECU added - maximum duration available in the
																			//                tone generator activity when set as mosquito
	public static final int			TONE_FREQUENCY_MAXIMUM		= 18000;	// 04/11/2015 ECU added - maximum frequency available in the
																			//                tone generator activity
	public static final int			TONE_FREQUENCY_MINIMUM		= 100;		// 06/03/2016 ECU added - minimum frequency available in the
																			//                tone generator activity
																			// 08/01/2016 ECU changed from 12000
	public static final int		    TRACKING_ACCURACY			= 5;		// 13/10/2020 ECU tracking accuracy in metres
	public static final String		UNCAUGHT_EXCEPTION_FILE		= "UncaughtException";
																			// 06/09/2020 ECU added to hold any 'uncaught exception' trace
	public static final String		UNKNOWN					  	= "Unknown";
																			// 01/04/2015 ECU added - used by music player
	public static final boolean		UPnP						= true;		// 26/08/2016 ECU added - to enable UPnP actioning
	public static final String		UPnP_DISCOVER_ADDRESS		= "239.255.255.250";	
																			// 26/08/2016 ECU added - the discovery address
	public static final int			UPnP_DISCOVER_PORT 			= 1900;		// 26/08/2016 ECU added - the discover port
	public static final boolean     UPnP_SERVICE				= true;		// 28/08/2016 ECU added - if UPnP service is to be handled
	public static final String		URL_INTRO					= "http://";// 12/12/2017 ECU intro of URL
	public static final int 		USER_DEFINED_SPOKEN_PHRASE  = -2;		// 21/05/2016 ECU added - user defined spoken phrase
	public static final String		VIDEO_RECORDER_FILE_DEFAULT = "Auto_";	// 15/06/2017 ECU added
	public static final int			VIDEO_MAX_DURATION_MINUTES	= 100;		// 16/06/2017 ECU max duration of video in minutes
	public static final int			VIDEO_MAX_DURATION			= VIDEO_MAX_DURATION_MINUTES * MILLISECONDS_PER_MINUTE;
																			// 24/05/2016 ECU max duration of video in millisecs
	public static final long		VIDEO_MAX_FILE_SIZE			= 500 * 1000000;
																			// 24/05/2016 ECU maximum file size in bytes
	public static final int			VIDEO_ROTATION_BACK			= 90;		// 29/05/2016 ECU video rotation - portrait mode - back camera
	public static final int			VIDEO_ROTATION_BACK_HINT	= 90;		// 29/05/2016 ECU video rotation - portrait mode - back camera
	public static final int			VIDEO_ROTATION_FRONT		= 90;		// 29/05/2016 ECU video rotation - portrait mode - front camera
	public static final int			VIDEO_ROTATION_FRONT_HINT	= 270;		// 29/05/2016 ECU video rotation - portrait mode - front camera
	public static final int			VIDEO_STREAM_PORT			= 5553;		// 15/09/2017 ECU the port used for video streaming
	public static final boolean		VIDEO_STREAMING				= true;		// 27/09/2017 ECU indicate whether video streaming to be active
	public static final int   		VISIT_END_MINUTES			= 15;		// 01/01/2016 ECU added - period after the carer's bluetooth
																			//                has disappeared when the visit will be
	                                                                        //                classed as 'complete'									
	public static final long  		VISIT_END_MILLISECONDS		= ((long) MILLISECONDS_PER_MINUTE * (long) VISIT_END_MINUTES);
																			// 01/01/2016 ECU as above but in milliseconds
	public static final int 		VOICE_REQUEST_CODE		    = 1234;   	// 21/11/2013 ECU added - used with voice recognition intent
	public static final long        WAKELOCK_TIMEOUT			= (30 * 1000);
																			// 05/07/2020 ECI timeout used when acquiting the wakelock
	public static final int			WIFI_STRENGTH_LEVELS		= 100;		// 20/02/2019 ECU added - number of levels
	public static final String		WIRELESS_INTERFACE			= "wlan0";	// 27/07/2016 ECU interface for the wireless network
	public static final int			WIRELESS_RETRY_COUNTER		= 50;		// 24/11/2016 ECU the number of retries when trying
	                                                                        //                to start the wireless interface when
	                                                                        //                sending an email
	public static final int			WIRELESS_RETRY_DELAY		= 1000;		// 24/11/2016 ECU the delay in mS between each attempt
																			//                to check if the wireless network is
	                                                                        //                running
	public static final int			WIRELESS_TRANSMISSION_DELAY	= MILLISECONDS_PER_MINUTE;	
																			// 24/11/2016 ECU the delay after an email has been sent
																			//                before checking if the wireless network
																			//                needs to be switched off - if it was
																			//                before the method was called
	// =============================================================================
	
	// =============================================================================
	// 03/09/2015 ECU group together statics which are related
	// =============================================================================
	
	// -----------------------------------------------------------------------------
	// 18/03/2015 ECU declare variables needed by the 'action handler' as
	//                implemented in Utilities.actionHandler
	// 03/05/2015 ECU added SCREEN
	// 27/11/2015 ECU added PLAY and SPEAK and EMAIL and SILENCE
	// 28/11/2015 ECU added TRACK
	// 11/12/2015 ECU added SMS
	// 07/01/2016 ECU added API
	// 11/03/2016 ECU added VIBRATE
	// 02/05/2016 ECU added TIME
	// 21/05/2016 ECU added VIDEO
	// 11/06/2016 ECU added INTRODUCER
	// 13/07/2016 ECU added NOTIFICATION
	// 04/08/2016 ECU added NAMED_ACTION
	// 26/01/2017 ECU added DISPLAY_TOGETHER
	// 03/05/2017 ECU added DELAY
	// 20/05/2017 ECU added FLUSH
	// 14/06/2017 ECU added ACTIVITY
	// 07/02/2018 ECU added ACTIVITY_LONG
	// 12/02/2018 ECU added APP
	// 10/04/2019 ECU added ALEXA
	// 09/04/2020 ECU added BLUETOOTH
	// 11/04/2020 ECU added ACTIONFILE
	// 12/04/2020 ECU added ACTION....REPLACEMENT
	// 11/08/2020 ECU added SPEAKANDDISPLAY
	// 12/10/2020 ECU added PHOTOGRAPH
	// 13/10/2020 ECU added DOCUMENT and modify SPEAK to include 'file'
	// ----------------------------------------------------------------------------
	public static final String		ACTION_DELIMITER				= ":";
	public static final String      ACTION_DELIMITER_REPLACEMENT	= "{{D}}";
	public static final String  	ACTION_DESTINATION_ACTIONFILE	= "actionfile";
	public static final String  	ACTION_DESTINATION_ACTIVITY		= "activity";
	public static final String  	ACTION_DESTINATION_ACTIVITY_LONG= "activity (long press)";
	public static final String  	ACTION_DESTINATION_ALEXA		= "alexa";
	public static final String  	ACTION_DESTINATION_API			= "API";
	public static final String  	ACTION_DESTINATION_APP			= "app";
	public static final String  	ACTION_DESTINATION_BLUETOOTH	= "bluetooth";
	public static final String  	ACTION_DESTINATION_DELAY		= "delay";
	public static final String  	ACTION_DESTINATION_DOCUMENT		= "document";
	public static final String  	ACTION_DESTINATION_EMAIL		= "email";
	public static final String		ACTION_DESTINATION_FLUSH		= "flush";
	public static final String  	ACTION_DESTINATION_NAMED_ACTION	= "actions";
	public static final String  	ACTION_DESTINATION_NOTIFICATION	= "notification";
	public static final String  	ACTION_DESTINATION_PHONE		= "phone";
	public static final String  	ACTION_DESTINATION_PHOTOGRAPH	= "photograph";
	public static final String  	ACTION_DESTINATION_PLAY			= "play";
	public static final String  	ACTION_DESTINATION_SCREEN		= "screen";
	public static final String  	ACTION_DESTINATION_SMS			= "sms";
	public static final String  	ACTION_DESTINATION_SPEAK		= "speak";
	public static final String  	ACTION_DESTINATION_SPEAKANDDISPLAY
																	= "speakanddisplay";
	public static final String  	ACTION_DESTINATION_TIME			= "time";
	public static final String  	ACTION_DESTINATION_TRACK		= "track";
	public static final String  	ACTION_DESTINATION_VIBRATE		= "vibrate";
	public static final String  	ACTION_DESTINATION_VIDEO		= "video";
	public static final String  	ACTION_DESTINATION_VOICE		= "command";
	public static final String  	ACTION_DESTINATION_WEMO			= "WeMo";
	public static final String		ACTION_DISPLAY_TOGETHER         = " & ";
	public static final String  	ACTION_FILE						= "file";
	public static final String  	ACTION_INTRODUCER				= "Actions";
	public static final String		ACTION_SEPARATOR				= ";";
	public static final String      ACTION_SEPARATOR_REPLACEMENT	= "{{S}}";
	// -----------------------------------------------------------------------------
	// 11/12/2015 ECU declare the arrays used for validating the action commands
	// 11/03/2016 ECU added vibrate
	// 02/05/2016 ECU added TIME
	// 21/05/2016 ECU added VIDEO
	// 13/07/2016 ECU added NOTIFICATION
	// 04/08/2016 ECU added NAMED_ACTION
	// 03/05/2017 ECU added DELAY and added repeat option to 
	//                      _NAMED_ACTION
	// 20/05/2017 ECU added FLUSH
	// 14/06/2017 ECU added ACTIVITY
	// 07/02/2018 ECU added ACTIVITY_LONG
	// 12/02/2018 ECU added APP
	// 10/04/2019 ECU added ALEXA
	// 11/04/2020 ECU added ACTIONFILE
	// 12/04/2020 ECU added BLUETOOTH
	// 11/08/2020 ECU added SPEAKANDDISPLAY
	// 12/10/2020 ECU added PHOTOGRAPH
	// 13/10/2020 ECU added DOCUMENT and add 'file' to SPEAK
	// 15/10/2020 ECU added the time to display to PHOTOGRAPH
	// -----------------------------------------------------------------------------
	public static final ActionCommand [] ACTION_COMMANDS  
		= new ActionCommand [] {
				new ActionCommand (ACTION_DESTINATION_ACTIONFILE,  	new String [][] {{"file name"}}),
				new ActionCommand (ACTION_DESTINATION_NAMED_ACTION, new String [][] {{"name of actions"},
																					 {"number of repeats","name of actions"}}),
				new ActionCommand (ACTION_DESTINATION_ACTIVITY,		new String [][] {{"activity"}}),
				new ActionCommand (ACTION_DESTINATION_ACTIVITY_LONG,new String [][] {{"activity"}}),
				new ActionCommand (ACTION_DESTINATION_ALEXA,		new String [][] {{"commands"}}),
				new ActionCommand (ACTION_DESTINATION_APP,			new String [][] {{"app"}}),
				new ActionCommand (ACTION_DESTINATION_BLUETOOTH, 	new String [][] {{"connect to a paired device"},
																					 {"disconnect from connected device"}}),
				new ActionCommand (ACTION_DESTINATION_DELAY, 		new String [][] {{"delay in milliseconds"}}),
				new ActionCommand (ACTION_DESTINATION_DOCUMENT, 	new String [][] {{"file name"}}),
				new ActionCommand (ACTION_DESTINATION_EMAIL, 		new String [][] {{"address(es)","subject","message"}}),
				new ActionCommand (ACTION_DESTINATION_FLUSH, 		null),
				new ActionCommand (ACTION_DESTINATION_NOTIFICATION, new String [][] {{"message"}}),	
				new ActionCommand (ACTION_DESTINATION_PHONE, 		new String [][] {{"number"}}),
				new ActionCommand (ACTION_DESTINATION_PHOTOGRAPH, 	new String [][] {{"file name"},
																					 {"time to display","file name"}}),
				new ActionCommand (ACTION_DESTINATION_PLAY,  		new String [][] {{"file name"}}),
				new ActionCommand (ACTION_DESTINATION_SCREEN,		new String [][] {{"on/off"}}),	
				new ActionCommand (ACTION_DESTINATION_SMS,   		new String [][] {{"number","message"}}),	
				new ActionCommand (ACTION_DESTINATION_SPEAK, 		new String [][] {{"phrase"},
																					 {"delay","phrase"},
																					 {"file","file name"}}),
				new ActionCommand (ACTION_DESTINATION_SPEAKANDDISPLAY,
																	new String [][] {{"phrase"}}),
				new ActionCommand (ACTION_DESTINATION_TIME, 		null),
				new ActionCommand (ACTION_DESTINATION_TRACK, 		new String [][] {{"on/off","address(es)"}}),
				new ActionCommand (ACTION_DESTINATION_VIBRATE, 		new String [][] {{"duration"}}),
				new ActionCommand (ACTION_DESTINATION_VIDEO,  		new String [][] {{"file name"}}),
				new ActionCommand (ACTION_DESTINATION_WEMO,  		new String [][] {{"device on/off"}}),	
								};
	// -----------------------------------------------------------------------------
	// 08/04/2014 ECU add the 'activity' options
	// -----------------------------------------------------------------------------
	public static final boolean 	ACTIVITY_FULL_SCREEN 		= true;
	public static final boolean 	ACTIVITY_NORMAL_SCREEN 		= false;	// 08/10/2016 ECU added
	public static final boolean 	ACTIVITY_SCREEN_ON   		= true;
	// -----------------------------------------------------------------------------
	// The following actions can be joined together and will be checked using
	// bitmask - so initialise them to powers of 2
	//
	// IMPORTANT - if new entries are added then reflect this in the string array
	// =========   'alarm_activities'
	// -----------------------------------------------------------------------------
	public static final int 		ALARM_ACTION_NONE          	= 0;
	public static final int 		ALARM_ACTION_PHONE_CALL     = 0x0001;
	public static final int 		ALARM_ACTION_TABLET_REMINDER= 0x0002;
	public static final int 		ALARM_ACTION_SLIDESHOW      = 0x0004;
	public static final int 		ALARM_ACTION_ACTIVITY	    = 0x0008;	// 28/11/2014 ECU added
	public static final int 		ALARM_ACTION_MESSAGE		= 0x0010; 	// 08/02/2015 ECU added
	public static final int 		ALARM_ACTION_EMAIL_MESSAGE	= 0x0020;	// 14/07/2015 ECU added
	public static final int         ALARM_ACTION_EPG            = 0x0040;	// 29/09/2015 ECU added
	public static final int         ALARM_ADVANCE_EPG           = 0x0080;	// 15/10/2015 ECU added
	public static final int		    ALARM_ACTION_ACTIONS		= 0x0100;	// 30/04/2017 ECU added
	// -----------------------------------------------------------------------------
	// 01/12/2015 ECU added alarm id's here rather than in individual activities
	// 19/12/2015 ECU moved some of the ID's from DailyScheduler
	// 29/12/2015 ECU added bluetooth discovery
	// 11/03/2017 ECU added Westminster Chime
	// 21/06/2018 ECU added 'random event'
	// 19/11/2018 ECU added 'watchdog timer'
	// 20/12/2019 ECU added 'monitor alarm'
	// -----------------------------------------------------------------------------
	public final static int 		ALARM_ID_APPOINTMENT_REMINDER	
																= 6172;     // 04/03/2014 ECU appointment reminder
    																		// a = hex (61)
    																		// r = hex (72)
	public final static int			ALARM_ID_APPOINTMENT_TIME	= 4154;     // 04/03/2014 ECU appointment time
																			// A = hex (41)
    																		// T = hex (54)
	public final static int			ALARM_ID_BLUETOOTH_DISCOVERY
																= 4244;     // 29/12/2015 ECU bluetooth discovery
																			// B = hex (42)
																			// D = hex (44)
	public final static int			ALARM_ID_CARE_VISIT
																= 4356;     // 02/10/2016 ECU care visit
																			// C = hex (43)
																			// V = hex (56)
	public final static int 		ALARM_ID_DAILY_SCHEDULER	= 4453;	    // 04/03/2014 ECU alarm id
    																		// D = hex (44)
    																		// S = hex (53)
	public final static int 		ALARM_ID_DOSAGE_ALARM		= 4441;		// 04/03/2014 ECU dose alarm
    																		// D = hex (44)
    																		// A = hex (41)
	public final static int 		ALARM_ID_METHOD				= 4554;		// 19/12/2015 ECU alarm using defined method
																			//                (M = hex (4D) so don't use)
																			// E = hex (45)
																			// T = hex (54)
	public final static int 		ALARM_ID_MONITOR			= 4954;		// 20/12/2019 ECU handle 'monitor' prompts
																			//                (M = hex (4D) so don't use)
																			//				  (O = hex (4F) so don't use)
	                                                                        //                (N = hex (4E) so don't use)
																			// E = hex (45)
																			// T = hex (54)
	public final static int			ALARM_ID_PANIC_ALARM		= 5041;   	// 01/12/2015 ECU added
																			// P = hex (50)
																			// A = hex (41)
	public final static int			ALARM_ID_RANDOM_EVENT		= 5245;		// 21/06/2018 ECU added
																			// R = hex (52)
																			// E = hex (45)
	public final static int			ALARM_ID_SPEAKING_CLOCK		= 5343;   	// 23/02/2014 ECU added
    																		// S = hex (53)
																			// C = hex (43)
	public final static int         ALARM_ID_WATCHDOG_TIMER		= 5754;		// 19/11/2018 ECU added
																			// W = hex (57)
																			// T = hex (54)
	public final static int         ALARM_ID_WESTMINSTER_CHIME	= 5743;		// 11/03/2017 ECU added
																			// W = hex (57)
																			// C = hex (43)
	// -----------------------------------------------------------------------------
	// 31/10/2016 ECU declare variables used by AppointmentsActivity where they used
	//                to be
	// -----------------------------------------------------------------------------
	// 30/10/2016 ECU the following are pointers into arrays that are stored in
	//                resources so be carefule if editing this list to make sure
	//                the resource array is modified accordingly
	//------------------------------------------------------------------------------
	public final static int APPOINTMENT_GAP_HOUR				= 1;
	public final static int APPOINTMENT_GAP_DAY 				= 2;
	public final static int APPOINTMENT_GAP_WEEK 				= 3;
	public final static int APPOINTMENT_GAP_10_MINUTES			= 4;		// 09/01/2014 ECU added
	// -----------------------------------------------------------------------------
	public final static int	APPOINTMENT_MODE_VIEW				= 0;
	public final static int	APPOINTMENT_MODE_EDIT				= 1;
	public final static int	APPOINTMENT_MODE_CREATE				= 2;
	// -----------------------------------------------------------------------------
	public final static int APPOINTMENT_NO_REMINDER				= 0;		// 09/01/2014 ECU added
	public final static int APPOINTMENT_START_1_DAY 			= 1;
	public final static int APPOINTMENT_START_2_DAYS			= 2;
	public final static int APPOINTMENT_START_1_WEEK			= 3;
	public final static int APPOINTMENT_START_30_DAYS			= 4;
	public final static int APPOINTMENT_START_2_HOURS			= 5;		// 09/01/2014 ECU added
	// -----------------------------------------------------------------------------
	// 03/09/2015 ECU declare protocol variables
	// ------------------------------------------------------------------------------
	public static final byte 		ASCII_SOH					= 0x01;		// 11/08/2013 ECU added - ASCII start of header
	public static final byte 		ASCII_STX			 		= 0x02;		// 11/08/2013 ECU added - ASCII start of text
	public static final byte 		ASCII_ETX			 		= 0x03;  	// 11/08/2013 ECU added - ASCII end of text	
	// -----------------------------------------------------------------------------
	// 03/09/2015 ECU declare bit handling operations
	// -----------------------------------------------------------------------------
	public static final byte 		BIT_SET			 			= 0;		// 19/08/2013 ECU added - set a bit
	public static final byte 		BIT_UNSET			 		= 1;		// 19/08/2013 ECU added - reset a bit
	public static final byte 		BIT_TOGGLE			 		= 2;		// 19/08/2013 ECU added - toggle a bit
	// -----------------------------------------------------------------------------
	// 21/03/2018 ECU declare the identifiers of broadcast messages - only used
	//                if .._ENABLED is true
	// -----------------------------------------------------------------------------
	public static final boolean     BROADCAST_ENABLED			= false;
	public static final String		BROADCAST_TIMER_MINUTE		= "timer_minute";
	public static final String		BROADCAST_TIMER_SECOND		= "timer_second";
	public static final String		BROADCAST_WEMO_NOTIFY		= "wemo_notify";
	// -----------------------------------------------------------------------------
	// 20/03/2015 ECU declare various multicast messages that can be sent
	// 23/03/2015 ECU added 'cry' and 'laugh'
	// -----------------------------------------------------------------------------
	public static final String		BROADCAST_MESSAGE_CRY		= "cry";
	public static final String		BROADCAST_MESSAGE_HELLO		= "hello";
	public static final String		BROADCAST_MESSAGE_LAUGH		= "laugh";
	// -----------------------------------------------------------------------------
	// 11/09/2015 ECU declare the available cameras
	// -----------------------------------------------------------------------------
	public static final int 		CAMERA_FRONT 				= 0;
	public static final int 		CAMERA_REAR 				= 1;	
	// -----------------------------------------------------------------------------
	// 02/10/2016 ECU care visit arguments
	// -----------------------------------------------------------------------------
	public static final int			CARE_VISIT_ARRIVAL			= 0;
	public static final int			CARE_VISIT_DEPARTURE		= 1;
	public static final int         CARE_VISIT_WARNING			= 2;
	// -----------------------------------------------------------------------------
	// 25/11/2016 ECU declare the colours that are used to show the status of
	//                visits for a carer
	//                   ....STARTED ............... the visit has started
	//                   ....ENDING ................ the care appears to be ending
	//                                               the visit but the 'grace' period
	//                                               is running so that if the carer
	//                                               'reappears' then the current
	//                                               visit will be extended
	//                   ....ENDED ----------------- the 'grace' period has expired and
	//                                               the visit is deemed to have ended
	// -----------------------------------------------------------------------------
	public static final int			CARE_VISIT_STARTED			= R.color.green_yellow;
	public static final int			CARE_VISIT_ENDED			= StaticData.DEFAULT_BACKGROUND_COLOUR;
	public static final int			CARE_VISIT_ENDING			= R.color.orange;
	// -----------------------------------------------------------------------------
	// 21/10/2014 ECU declare the different types of datagram
	// -----------------------------------------------------------------------------
	public static final int  		DATAGRAM_COMPASS 	 		= 0;
	public static final int	 		DATAGRAM_FILENAME	 		= 1;
	public static final int	 		DATAGRAM_LOCATION	 		= 2;
	// -----------------------------------------------------------------------------
	// 18/09/2015 ECU declare the fields in an EPG entry - position within the
	//                fields entry
	// -----------------------------------------------------------------------------
	public static final int			EPG_BLACK_AND_WHITE			= 13;
	public static final int			EPG_CAST					= 5;
	public static final int			EPG_DATE					= 19;
	public static final int			EPG_DEAF_SIGNED				= 12;
	public static final int			EPG_DESCRIPTION				= 17;
	public static final int			EPG_DIRECTOR				= 4;
	public static final int			EPG_DURATION				= 22;
	public static final int			EPG_END_TIME				= 21;
	public static final int			EPG_EPISODE					= 2;
	public static final int			EPG_FILM					= 7;
	public static final int			EPG_FILM_CERTIFICATE		= 15;
	public static final int			EPG_FILM_START_RATING		= 14;
	public static final int			EPG_GENRE					= 16;
	public static final int			EPG_NEW_SERIES				= 11;
	public static final int			EPG_PREMIERE				= 6;
	public static final int			EPG_PROGRAM_TITLE			= 0;
	public static final int			EPG_RADIO_TIMES_CHOICE		= 18;
	public static final int			EPG_REPEAT					= 8;
	public static final int			EPG_START_TIME				= 20;
	public static final int			EPG_SUB_TITLE				= 1;	
	public static final int			EPG_SUBTITLES				= 9;
	public static final int			EPG_WIDESCREEN				= 10;
	public static final int			EPG_YEAR					= 3;
	// -----------------------------------------------------------------------------
	// 11/10/2016 ECU create itelm used by the GroupActivity
	// -----------------------------------------------------------------------------
	public static final int         GROUP_DEFINE				= 0;
	public static final int			GROUP_DELETE				= 1;
	public static final int			GROUP_DISPLAY				= 2;
	public static final int			GROUP_REDEFINE				= 3;
	// -----------------------------------------------------------------------------
	// 30/07/2017 ECU declare variables associated with HTML formating
	// 12/03/2019 ECU added green, white, blue and black
	// 13/07/2020 ECU added the HTML_TAG, MIME_TYPE and ENCODING
	// -----------------------------------------------------------------------------
	public static final String		HTML_BREAK					= "<br>";
	public static final String		HTML_COLOUR_BLACK			= "<font color=\"black\">";
	public static final String		HTML_COLOUR_BLUE			= "<font color=\"blue\">";
	public static final String		HTML_COLOUR_FUCHSIA			= "<font color=\"fuchsia\">";
	public static final String		HTML_COLOUR_GREEN			= "<font color=\"green\">";
	public static final String		HTML_COLOUR_OFF				= "</font>";
	public static final String		HTML_COLOUR_RED				= "<font color=\"red\">";
	public static final String		HTML_COLOUR_TEAL			= "<font color=\"teal\">";
	public static final String		HTML_COLOUR_WHITE			= "<font color=\"white\">";
	public static final String		HTML_COLOUR_YELLOW			= "<font color=\"yellow\">";
	public static final String		HTML_ENCODING				= "UTF-8";
	public static final String		HTML_ENCODING_BASE64		= "base64";
	public static final String		HTML_INTRODUCER				= "<";		// 01/04/2016 ECU HTML command introducer
	public static final String		HTML_MIME_TYPE				= "text/html; charset=utf-8";
	public static final String		HTML_PREFORMATTED_END		= "</pre>";
	public static final String		HTML_PREFORMATTED_START		= "<pre>";
	public static final String		HTML_TAG					= "<html>";
	public static final String		HTML_THEMATIC_BREAK			= "<hr>";
	// -----------------------------------------------------------------------------
	// 05/05/2015 ECU define the types of message used with the various
	//                handlers
	// 09/06/2015 ECU added MESSAGE_DELETE_ITEM
	// 22/06/2015 ECU added ....REFRESH
	// 12/07/2015 ECU added ....FINISH
	// 10/09/2015 ECU added ....VIDEO_START and ....VIDEO_STOP
	// 18/09/2015 ECU added ....WEB_PAGE and WEB_PAGE_RETRY
	// 15/10/2015 ECU added ....START and ....DATA
	// 21/12/2015 ECU added ....PROMPT...
	// 05/01/2016 ECU added ....CHUNKS_START, CHUNKS_NEXT, ACK and NAK
	// 09/01/2016 ECU added ....RECEIVE and FILE_TRANSFER
	// 12/01/2016 ECU added ....FTP_CLIENT and FTP_SERVER and removed FILE_TRANSFER
	// 10/02/2016 ECU added ....REMOTE_MONITOR
	// 05/04/2016 ECU added ....TOAST
	// 26/04/2016 ECU added ....MONITOR
	// 18/05/2016 ECU added ....AMBIENT_LIGHT
	// 03/06/2016 ECU added ....PROCESS_ACTION and PROCESS_ACTIONS
	//            ECU added ....ACTION_FINISHED and ...LIST_FINISHED
	// 17/06/2016 ECU added ....CHECK_DEVICE and ... RESP and ...TIMEOUT
	// 13/07/2016 ECU added .... NOTIFICATION_.. added
	// 20/08/2016 ECU added .... ELAPSED_TIME_ .... added
	// 26/08/2016 ECU added .... UPnP_ ..... added
	// 02/10/2016 ECU added .... TOAST_AND_PHOTO
	// 11/10/2016 ECU added .... RESTART
	// 24/10/2016 ECU added .... ANYTHING_PLAYING
	// 20/11/2016 ECU added .... PROCESS
	// 24/11/2016 ECU added .... TOAST_SPEAK and tidied up because
	//                                 ... TOAST 			just display text message
	//                                 ... TOAST_PHOTO		display a message with a photo
	//                                 ... TOAST_SPEAK		display and display a message
	// 28/11/2016 ECU added .... PROCESS_DELAYED
	//            ECU added .... ACTIONS_TIMEOUT
	// 14/12/2016 ECU added .... DISPLAY_COUNTER
	// 28/12/2016 ECU added .... METHOD
	// 21/01/2017 ECU added .... DISPLAY_DATE
	// 31/01/2017 ECU added .... DISPLAY_DRAWABLE
	//            ECU added .... DISPLAY_TEXT
	// 12/03/2017 ECU added .... PLAY_RAW
	// 21/03/2017 ECU added .... TRACK_COMPLETED and TIME_OUT and ARE_YOU_THERE....
	// 25/03/2017 ECU added .... SYNCHRONISE
	// 05/05/2017 ECU added .... FINISHED_DELAYED
	// 20/05/2017 ECU added .... PROCESS_ACTIONS_FLUSH
	// 15/09/2017 ECU added .... VIDEO_STREAM_....
	// 05/01/2018 ECU added .... PLAY_TRACK and DISPLAY_TIMED
	// 06/01/2018 ECU added .... REMOTE_PLAYER
	// 08/01/2018 ECU added .... REMOTE_TRACK_ENDED
	// 09/01/2018 ECU added .... MESSAGE_PAUSE_CHECK
	// 27/02/2018 ECU added .... MESSAGE_BLUETOOTH_....
	// 01/03/2018 ECU added .... NESSAGE_BLUETOOTH_MESSAGE and ....DISCONNECTED
	// 17/08/2018 ECU added .... MESSAGE_USER_ACTIONS
	// 19/11/2018 ECU added .... MESSAGE_WATCHDOG_TIMER
	// 11/09/2019 ECU added .... MESSAGE_TOAST_IMAGE
	// 04/12/2019 ECU added .... MESSAGE_EMAIL_SENT
	// 06/12/2019 ECU added .... MESSAGE_EMAIL_SEND
	// 22/12/2019 ECU added .... MESSAGE_SCREEN_BRIGHTNESS
	// 21/02/2020 ECU added .... MESSAGE_SPEAK_PHRASE
	// 30/03/2020 ECU added .... MESSAGE_NOTIFICATION_REFRESH
	// 07/04/2020 ECU added .... MESSAGE_BLUETOOTH_DISCONNECT
	// 24/06/2020 ECU added .... MESSAGE_START_ACTIVITY
	// -----------------------------------------------------------------------------
	public  static final int    	MESSAGE_ACTION_FINISHED		= 0;		// 03/06/2016 ECU added
	public  static final int    	MESSAGE_ACTION_FINISHED_DELAYED
																= 1;		// 05/05/2017 ECU added
	public  static final int    	MESSAGE_ADAPTER				= 2;		// 05/05/2015 ECU added
	public  static final int		MESSAGE_AMBIENT_LIGHT		= 3;		// 18/05/2016 ECU added
	public  static final int		MESSAGE_ANYTHING_PLAYING	= 4;		// 18/05/2016 ECU added
	public  static final int		MESSAGE_ARE_YOU_THERE		= 5;
	public  static final int		MESSAGE_ARE_YOU_THERE_RESP	= 6;	
	public  static final int		MESSAGE_ARE_YOU_THERE_TIMEOUT
																= 7;
	public  static final int		MESSAGE_BLUETOOTH_CONNECT	= 8;		// 27/02/2018 ECU added
	public  static final int		MESSAGE_BLUETOOTH_CONNECTED	= 9;		// 27/02/2018 ECU added
	public  static final int		MESSAGE_BLUETOOTH_DISCONNECT= 10;		// 07/04/2020 ECU added
	public  static final int		MESSAGE_BLUETOOTH_DISCONNECTED	
																= 11;		// 01/03/2018 ECU added
	public  static final int		MESSAGE_BLUETOOTH_ERROR		= 12;		// 27/02/2018 ECU added
	public  static final int		MESSAGE_BLUETOOTH_MESSAGE	= 13;		// 01/03/2018 ECU added
	public  static final int		MESSAGE_BLUETOOTH_TIMEOUT	= 14;		// 27/02/2018 ECU added
	public  static final int		MESSAGE_CHECK_DEVICE		= 15;		// 17/06/2016 ECU added
	public  static final int		MESSAGE_CHECK_DEVICE_RESP	= 16;		// 17/06/2016 ECU added
	public  static final int		MESSAGE_CHECK_DEVICE_TIMEOUT= 17;		// 17/06/2016 ECU added
	public  static final int    	MESSAGE_CHUNKS_ACK			= 18;		// 05/01/2016 ECU added
	public  static final int    	MESSAGE_CHUNKS_NAK			= 19;		// 05/01/2016 ECU added
	public  static final int    	MESSAGE_CHUNKS_NEXT			= 20;		// 05/01/2016 ECU added
	public  static final int    	MESSAGE_CHUNKS_START		= 21;		// 05/01/2016 ECU added
	public  static final int    	MESSAGE_CHUNKS_WAIT			= 22;		// 05/01/2016 ECU added
	public  static final int    	MESSAGE_DATA				= 23;		// 05/05/2015 ECU added
	public  static final int		MESSAGE_DELETE_ITEM 		= 24;		// 09/06/2015 ECU added
	public  static final int    	MESSAGE_DISPLAY				= 25;		// 05/05/2015 ECU added
	public  static final int    	MESSAGE_DISPLAY_COUNTER		= 26;		// 14/12/2016 ECU added
	public  static final int		MESSAGE_DISPLAY_DATE_REFRESH= 27;		// 21/01/2017 ECU added
	public  static final int		MESSAGE_DISPLAY_DATE_START  = 28;		// 21/01/2017 ECU added
	public  static final int		MESSAGE_DISPLAY_DATE_STOP	= 29;		// 21/01/2017 ECU added
	public 	static final int		MESSAGE_DISPLAY_DRAWABLE	= 30;		// 31/01/2017 ECU added
	public 	static final int		MESSAGE_DISPLAY_TEXT		= 31;		// 31/01/2017 ECU added
	public 	static final int		MESSAGE_DISPLAY_TIMED		= 32;		// 05/01/2018 ECU added
	public 	static final int		MESSAGE_DURATION			= 33;		// 15/08/2015 ECU added
	public 	static final int		MESSAGE_ELAPSED_TIME_REFRESH= 34;  		// 15/08/2015 ECU added
	public 	static final int		MESSAGE_ELAPSED_TIME_START	= 35;  		// 15/08/2015 ECU added
	public 	static final int		MESSAGE_ELAPSED_TIME_STOP	= 36;  		// 15/08/2015 ECU added
	public 	static final int		MESSAGE_EMAIL_SEND			= 37;  		// 04/12/2019 ECU added
	public 	static final int		MESSAGE_EMAIL_SENT			= 38;  		// 04/12/2019 ECU added
	public 	static final int		MESSAGE_FINISH				= 39;		// 12/07/2015 ECU added
	public 	static final int		MESSAGE_FTP_CLIENT			= 40;		// 12/01/2016 ECU added
	public 	static final int		MESSAGE_FTP_SERVER			= 41;		// 12/01/2016 ECU added
	public 	static final int		MESSAGE_LIST_FINISHED		= 42;		// 03/06/2016 ECU added
	public 	static final int		MESSAGE_METHOD				= 43;		// 28/12/2016 ECU added
	public 	static final int		MESSAGE_MONITOR				= 44;		// 26/04/2016 ECU added

	public  static final int		MESSAGE_NOTIFICATION_END	= 45;		// 13/07/2016 ECU added
	public  static final int		MESSAGE_NOTIFICATION_REFRESH
																= 46;		// 30/03/2020 ECU added

	public  static final int		MESSAGE_NOTIFICATION_START	= 47;		// 13/07/2016 ECU added
	public  static final int		MESSAGE_PAUSE_CHECK			= 48;		// 09/01/2018 ECU added
	public  static final int		MESSAGE_PLAY_RAW			= 49;		// 12/03/2017 ECU added
	public  static final int		MESSAGE_PLAY_TRACK			= 50;		// 05/01/2018 ECU added
	public  static final int		MESSAGE_PLAY_TRACK_CANCEL	= 51;		// 25/03/2019 ECU added
	public 	static final int		MESSAGE_PLAYING				= 52;		// 15/08/2015 ECU added
	public  static final int        MESSAGE_PROCESS				= 53;		// 20/11/2016 ECU added
	public 	static final int		MESSAGE_PROCESS_ACTION		= 54;		// 03/06/2016 ECU added
	public 	static final int		MESSAGE_PROCESS_ACTIONS		= 55;		// 03/06/2016 ECU added
	public 	static final int		MESSAGE_PROCESS_ACTIONS_FLUSH
																= 56;		// 20/05/2017 ECU added
	public 	static final int		MESSAGE_PROCESS_ACTIONS_IMMEDIATELY
																= 57;		// 04/08/2016 ECU added
	public 	static final int		MESSAGE_PROCESS_ACTIONS_TIMEOUT
																= 58;		// 28/11/2016 ECU added
	public 	static final int		MESSAGE_PROCESS_DELAYED		= 59;		// 28/11/2016 ECU added
	public 	static final int		MESSAGE_PROCESS_FINISHED	= 60;		// 03/06/2016 ECU added
	public  static final int		MESSAGE_PROGRESS			= 61;		// 24/10/2016 ECU added
	public 	static final int		MESSAGE_PROMPT				= 62;		// 21/12/2015 ECU added
	public 	static final int		MESSAGE_PROMPT_DOSE			= 63;		// 21/12/2015 ECU added
	public 	static final int		MESSAGE_PROMPT_DOSE_END		= 64;		// 21/12/2015 ECU added
	public 	static final int		MESSAGE_PROMPT_DOSE_START	= 65;		// 21/12/2015 ECU added
	public 	static final int		MESSAGE_RECEIVE				= 66;		// 09/01/2016 ECU added
	public  static final int    	MESSAGE_REFRESH				= 67;		// 22/06/2015 ECU added
	public  static final int    	MESSAGE_REMOTE_MONITOR		= 68;		// 10/02/2016 ECU added
	public  static final int    	MESSAGE_REMOTE_PLAYER		= 69;		// 10/02/2016 ECU added
	public  static final int    	MESSAGE_REMOTE_TRACK_ENDED	= 70;		// 08/01/2018 ECU added
	public  static final int    	MESSAGE_REQUEST				= 71;		// 25/11/2015 ECU added
	public  static final int    	MESSAGE_RESTART				= 72;		// 11/10/2016 ECU added
	public  static final int		MESSAGE_SEND				= 73;
	public  static final int    	MESSAGE_SCREEN				= 74;		// 05/05/2015 ECU added
	public  static final int    	MESSAGE_SCREEN_BRIGHTNESS	= 75;		// 22/12/2019 ECU added
	public  static final int		MESSAGE_SLEEP				= 76;
	public  static final int        MESSAGE_SPEAK_PHRASE		= 77;		// 21/02/2020 ECU added
	public  static final int    	MESSAGE_START				= 78;		// 15/10/2015 ECU added
	public  static final int    	MESSAGE_START_ACTIVITY		= 79;		// 24/06/2020 ECU added
	public  static final int    	MESSAGE_SYNCHRONISE			= 80;		// 25/03/2017 ECU added
	public 	static final int		MESSAGE_TIME_OUT			= 81;		// 21/03/2017 ECU added
	public 	static final int		MESSAGE_TOAST				= 82;		// 24/11/2016 ECU added
	public 	static final int		MESSAGE_TOAST_IMAGE			= 83;		// 11/09/2019 ECU added
	public 	static final int		MESSAGE_TOAST_SPEAK			= 84;		// 05/04/2016 ECU added
	public  static final int		MESSAGE_TOAST_PHOTO			= 85;		// 02/10/2016 ECU added
	public 	static final int		MESSAGE_TONE				= 86;		// 15/08/2015 ECU added
	public 	static final int		MESSAGE_TRACK_COMPLETED		= 87;		// 21/03/2017 ECU added
	public 	static final int		MESSAGE_UPnP_DISCOVER		= 88;		// 26/08/2015 ECU added
	public 	static final int		MESSAGE_USER_ACTIONS		= 89;		// 17/08/2018 ECU added
	public 	static final int		MESSAGE_VIDEO_START			= 90;		// 10/09/2015 ECU added
	public 	static final int		MESSAGE_VIDEO_STOP			= 91;		// 10/09/2015 ECU added
	public 	static final int		MESSAGE_VIDEO_STREAM		= 92;		// 15/09/2017 ECU added
	public 	static final int		MESSAGE_VIDEO_STREAM_START	= 93; 		// 15/09/2017 ECU added
	public 	static final int		MESSAGE_VIDEO_STREAM_STOP	= 94;		// 15/09/2017 ECU added
	public  static final int		MESSAGE_WATCHDOG_TIMER		= 95;		// 19/11/2018 ECU added
	public  static final int		MESSAGE_WEB_PAGE			= 96;		// 18/09/2015 ECU added
	public  static final int		MESSAGE_WEB_PAGE_RETRY		= 97;		// 18/09/2015 ECU added
	// -----------------------------------------------------------------------------
	// 26/04/2016 ECU created to identify various monitor types
	// -----------------------------------------------------------------------------
	public  static final int		MONITOR_DATA_ACTIONS		= 0;
	public  static final int		MONITOR_DATA_ACTIVITY		= 1;
	public  static final int		MONITOR_DATA_BATTERY		= 2;
	public  static final int		MONITOR_DATA_KEYSTROKE		= 3;
	// -----------------------------------------------------------------------------
	// 06/10/2017 ECU declare the defaults for the 'theft protection' parameters
	// -----------------------------------------------------------------------------
	public 	static final String		MOVEMENT_ACTIONS			= null;
	public  static final int		MOVEMENT_DURATION			= 60 * 1000;
	public  static final int		MOVEMENT_GAP				= 2 * 1000;
	public  static final int		MOVEMENT_INITIAL_DELAY		= 10 * 1000;
	public  static final float		MOVEMENT_TRIGGER			= 0.1f;
	// -----------------------------------------------------------------------------
	// 25/11/2018 ECU declare the colours used by the app's own notification system
	// 16/04/2020 ECU added _WARNING
	// 03/08/2020 ECU added CONTACT and TRACK
	// -----------------------------------------------------------------------------
	public  static final int    	NOTIFICATION_COLOUR_CONTACT = R.color.dark_blue_background_44;
	public  static final int    	NOTIFICATION_COLOUR_ERROR   = R.color.red;
	public  static final int    	NOTIFICATION_COLOUR_NORMAL  = DEFAULT_BACKGROUND_COLOUR;
	public  static final int    	NOTIFICATION_COLOUR_TRACK   = R.color.light_gray;
	public  static final int    	NOTIFICATION_COLOUR_WARNING = R.color.orange;
	// -----------------------------------------------------------------------------
	// 12/03/2016 ECU identify the various notifications that may be used
	//            ECU each entry must be unique
	// -----------------------------------------------------------------------------
	public static final int   		NOTIFICATION_BLUETOOTH  	= 5;		// 05/08/2020 ECU added
	public static final int   		NOTIFICATION_MEDICATION		= 4;		// 10/11/2017 ECU added
	public static final int			NOTIFICATION_NOTIFICATION	= 3;		// 03/08/2020 ECU added
	public static final int			NOTIFICATION_PANIC_ALARM	= 2;
	public static final int 		NOTIFICATION_TIMER_SERVICE	= 1;
	public static final int 		NOTIFICATION_TTS_SERVICE	= 250749;
	// -----------------------------------------------------------------------------
	public static final int  		OBJECT_AGENCIES 	 		= 0;		// 28/08/2015 ECU added
	public static final int			OBJECT_APPOINTMENTS			= 1;		// 19/12/2015 ECU added
	//public static final int		OBJECT_BARCODES				= 2;		// 21/11/2015 ECU added
																			// 09/04/2018 ECU removed
	public static final int  		OBJECT_CARE_PLANS	 		= 3;		// 29/08/2015 ECU added
	public static final int  		OBJECT_CARE_VISITS	 		= 4;		// 29/08/2015 ECU added
	public static final int  		OBJECT_CARER_VISITS	 		= 5;		// 25/01/2020 ECU added
	public static final int  		OBJECT_CARERS	 	 		= 6;		// 28/08/2015 ECU added
	//public static final int  		OBJECT_CHANNELS	 	 		= 7;		// 18/09/2015 ECU added
																			// 28/04/2018 ECU removed
	public static final int			OBJECT_CONTACTS				= 8;		// 20/12/2017 ECU added
	//public static final int  		OBJECT_DAILY_SUMMARIES 		= 9;		// 02/03/2017 ECU added
																			// 20/04/2018 ECU removed
	public static final int  		OBJECT_DAYS	 	 			= 10;		// 25/03/2014 ECU added
	//public static final int		OBJECT_DOCUMENTS			= 11;		// 18/10/2016 ECU added
																			// 13/04/2018 ECU removed
	public static final int  		OBJECT_DOSES	 	 		= 12;		// 25/03/2014 ECU added
	//public static final int  		OBJECT_LIQUIDS	 	 		= 13;		// 25/03/2014 ECU added
																			// 10/04/2018 ECU removed
	public static final int  		OBJECT_MEDICATION 	 		= 14;		// 24/09/2016 ECU added
	//public static final int  		OBJECT_NAMED_ACTIONS 		= 15;		// 03/08/2016 ECU added
																			// 20/04/2018 ECU removed
	//public static final int  		OBJECT_NOTIFICATIONS 		= 16;		// 13/07/2016 ECU added
																			// 10/04/2018 ECU removed
	//public static final int  		OBJECT_SELECTED_CHANNELS	= 17;		// 18/09/2015 ECU added
																			// 28/04/2018 ECU removed
	public static final int  		OBJECT_SELECTOR	 			= 18;		// 29/03/2014 ECU added
	public static final int  		OBJECT_SHOPPING	 			= 19;		// 11/10/2014 ECU added
	public static final int  		OBJECT_TIMER		 		= 20;		// 07/02/2015 ECU added
	//public static final int  		OBJECT_UPNP			 		= 21;		// 06/05/2017 ECU added
																			// 17/04/2018 ECU removed
	public static final int  		OBJECT_WEMO_TIMER	 		= 22;		// 25/02/2015 ECU added
	public static final int  		OBJECT_WEMO_TIMERS	 		= 23;		// 27/02/2015 ECU added
	// -----------------------------------------------------------------------------
	// 02/12/2015 ECU declare the phases in the Panic Alarm that is embedded in
	//                the alarm
	// -----------------------------------------------------------------------------
	public static final int			PANIC_ALARM_ACTIVATE		= 0;		
	public static final int			PANIC_ALARM_DEACTIVATE		= 1;
	public static final int			PANIC_ALARM_IMMEDIATE		= 2;
	public static final int			PANIC_ALARM_PROMPT			= 3;
	// -----------------------------------------------------------------------------
	// 03/12/2015 ECU declare the codes used in security checking
	// -----------------------------------------------------------------------------
	public static final String		PANIC_ALARM_CLUBS			= "C";
	public static final String		PANIC_ALARM_DIAMONDS		= "D";
	public static final String		PANIC_ALARM_HEARTS			= "H";
	public static final String		PANIC_ALARM_SPADES			= "S";
	public static final int			PANIC_ALARM_MAX_TRIES		= 3;
	// -----------------------------------------------------------------------------
	// 04/12/2015 ECU declare variables associated with the shake handling
	//
	//				  _IGNORE_PERIOD    multiple shakes within this period (in nanoSecs)
	//                                  will be treated as one
	//                _RESET_PERIOD		if no shakes are received after this period
	//                                  (in nanoSecs) then the 'shake counter' will be reset
	//                _SHAKE_NUMBER	    this number of shakes will trigger the alarm
	//                _SCALE			this is the scale factor used when setting the
	//                                  values in the SettingsActivity
	//                _SHAKE_THRESHOLD	this is the threshold of the 'gForce' that will
	//                                  define a shake
	// -----------------------------------------------------------------------------
	public static final long		PANIC_ALARM_IGNORE_PERIOD	= 100000000l;  // 100 mS in nanoSeconds
	public static final long		PANIC_ALARM_RESET_PERIOD	= 5000000000l; // 5 secs in nanoSeconds
	public static final int			PANIC_ALARM_SHAKE_NUMBER	= 4;
	public static final float		PANIC_ALARM_SCALE			= 10.0F;
	public static final float      	PANIC_ALARM_SHAKE_THRESHOLD	= 1.2F;
	// -----------------------------------------------------------------------------
	// 09/11/2013 ECU sometimes there is a need to start an application (like
	//                'mail') which requires a package to be started which may
	//                differ for different devices. The following variable
	//                will be used by Utilities.getPackageIntent to return
	//                the appropriate package for the device. Hope that makes 
	//                some sense
	// 27/01/2016 ECU added CONTACTS
	// -----------------------------------------------------------------------------
	public static final int 		PACKAGE_TYPE_CONTACTS 		= 0;
	public static final int 		PACKAGE_TYPE_MAIL	 		= 1;
	// -----------------------------------------------------------------------------
	// 09/12/2013 ECU specify the parameters which are passed between activities
	// 19/12/2013 ECU include device and code parameters for Infra Red control
	// 12/01/2014 ECU add parameters for Care Plan preparation
	// 24/03/2014 ECU added _BACK_KEY which will be associated with a boolean
	//                to indicate whether a receiving activity will action
	//                the depression of the 'back key'.
	// 09/06/2015 ECU added the SWIPE_METHOD
	// 13/06/2015 ECU added the IMAGE_HANDLER
	// 15/06/2015 ECU added the HELP_METHOD
	// 19/10/2015 ECU added the EPG_REFRESH
	// 04/11/2015 ECU added the ACTIVITY
	// 05/11/2015 ECU added the CONFIGURATION
	// 11/11/2015 ECU added the TESTING_LEVEL
	// 26/11/2015 ECU added the PROMPT and PANIC
	// 02/12/2015 ECU added ALARM_TYPE
	// 18/12/2015 ECU added METHOD_DEFINITION
	// 21/12/2015 ECU added PROMPT_.. 
	// 06/03/2016 ECU added ALARM_START
	// 27/03/2016 ECU added LAYOUT and SPOKEN_PHRASE
	// 28/03/2016 ECU added MESSAGE_HTML
	// 01/06/2016 ECU added FINISH
	// 10/06/2016 ECU added RESTART
	// 13/06/2016 ECU added BARCODE_ACTIONS
	// 28/06/2016 ECU added CHANNEL
	// 09/08/2016 ECU added SCALE
	// 06/10/2016 ECU added GROUP
	// 23/10/2016 ECU added RECORDED_NOTES and SPEAK_METHOD
	// 26/10/2016 ECU added AUDIO_FILE_NAME
	// 14/11/2016 ECU added APPEND
	// 29/11/2016 ECU added APPOINTMENTS
	// 19/12/2016 ECU added URL
	// 01/06/2017 ECU added NOTIFICATION
	// 11/06/2017 ECU added RECORDER_START
	// 23/06/2017 ECU added DISPLAY
	// 26/07/2017 ECU added ALARM_TIME
	// 18/09/2017 ECU added VIDEO_STREAM
	// 23/09/2017 ECU added LEGEND
	//			  ECU added SORT_INHIBIT
	// 07/10/2017 ECU added ALARM_BUNDLE
	// 02/01/2018 ECU added MUSIC_DURATION and MUSIC_VOLUME
	// 10/03/2018 ECU added COUNTDOWN_TIMER
	// 24/04/2018 ECU added SHOP
	// 19/10/2019 ECU added COMMAND_STRING
	// 06/01/2020 ECU added MENU
	// 18/01/2020 ECU added WEBVIEW_CLIENT
	// 25/03/2020 ECU added DAILY_CHECK
	// 05/04/2020 ECU added DEVICE_NAME
	// 13/05/2020 ECU added ALARM_SOURCE
	// 18/05/2020 ECU added CONNECTED_DEVICES
	// 25/07/2020 ECU removed
	//                    public static final String 		PARAMETER_INTENT		    = "INTENT";
	//					  public static final String 		PARAMETER_GROUP				= "GROUP";
	// 12/10/2020 ECU added FINISH_ACTION
	// -----------------------------------------------------------------------------
	public static final String 		PARAMETER_ACTIVITY			= "ACTIVITY";	// 04/11/2015 ECU added
	public static final String 		PARAMETER_ACTION			= "ACTION";		// 08/02/2015 ECU added
	public static final String 		PARAMETER_AGENCY			= "AGENCY";		// 12/01/2014 ECU added
	public static final String		PARAMETER_ALARM_BUNDLE		= "ALARM_BUNDLE";
																				// 07/10/2017 ECU added
	public static final String 		PARAMETER_ALARM_DATA		= "ALARM_DATA";	// 09/02/2015 ECU added
	public static final String 		PARAMETER_ALARM_ID			= "ALARM_ID";	// 23/02/2014 ECU added
	public static final String 		PARAMETER_ALARM_SOURCE		= "ALARM_SOURCE";
																				// 13/05/2020 ECU added
	public static final String 		PARAMETER_ALARM_START		= "ALARM_START";// 06/03/2016 ECU added
	public static final String 		PARAMETER_ALARM_TIME		= "ALARM_TIME";	// 26/07/2017 ECU added
	public static final String 		PARAMETER_ALARM_TYPE		= "ALARM_TYPE";	// 02/12/2015 ECU added
	public static final String 		PARAMETER_APPEND			= "append";		// 14/11/2016 ECU added
	public static final String 		PARAMETER_APPOINTMENTS     	= "appointments";
																				// 29/11/2016 ECU added
	public static final String 		PARAMETER_ARGUMENTS      	= "ARGUMENTS";	// 04/03/2014 ECU added
	public static final String      PARAMETER_AUDIO_FILE_NAME	= "audioFileName";
																				// 26/10/2016 ECU added
	public static final String 		PARAMETER_BACK_KEY			= "BACKKEY";	// 24/03/2014 ECU added
	public static final String 		PARAMETER_BACK_METHOD		= "BACKMETHOD";	// 28/02/2015 ECU added
	public static final String 		PARAMETER_BARCODE			= "BARCODE";	// 08/02/2014 ECU added
	public static final String 		PARAMETER_BARCODE_ACTIONS	= "ACTIONS";	// 13/06/2016 ECU added
	public static final String 		PARAMETER_BARCODE_DESC		= "DESCRIPTION";
																				// 08/02/2014 ECU added
	public static final String 		PARAMETER_BARCODE_ONLY		= "BARCODE_ONLY";
																				// 11/01/2020 ECU added
	public static final String 		PARAMETER_CARER				= "CARER";		// 12/01/2014 ECU added
	public static final String 		PARAMETER_CARER_VISIT		= "carer_visit";// 02/01/2016 ECU added
	public static final String 		PARAMETER_CHANNEL			= "channel";	// 28/06/2016 ECU added
	public static final String 		PARAMETER_COMMAND_NUMBER 	= "commandnumber";
																				// 06/03/2014 ECU added
	public static final String 		PARAMETER_COMMAND_STRING 	= "commandString";
																				// 19/10/2019 ECU added
	public static final String 		PARAMETER_CONFIGURATION 	= "configuration";
																				// 05/11/2015 ECU added
	public static final String 		PARAMETER_CONNECTED_DEVICES	= "connected_devices";
																				// 18/05/2020 ECU added
	public static final String 		PARAMETER_COUNTDOWN_TIMER 	= "countdown";
																				// 10/03/2018 ECU added
	public static final String 		PARAMETER_DAILY_CHECK		= "daily_check";
																				// 25/03/2020 ECU added
	public static final String 		PARAMETER_DATA				= "DATA";		// 08/02/2015 ECU added
	public static final String 		PARAMETER_DAY				= "DAY";		// 13/01/2014 ECU added
	public static final String 		PARAMETER_DEVICE_NAME		= "device_name";// 05/04/2020 ECU added
	public static final String		PARAMETER_DISPLAY			= "display";	// 23/06/2017 ECU added
	public static final String 		PARAMETER_DOCTOR			= "doctor";		// 11/10/2014 ECU added
	public static final String 		PARAMETER_DOSE				= "dose";		// 15/01/2014 ECU added
	public static final String 		PARAMETER_DOSE_TIME			= "dosetime";	// 15/01/2014 ECU added
	public static final String 		PARAMETER_DURATION			= "DURATION";	// 12/01/2014 ECU added
	public static final String 		PARAMETER_EMAIL_MESSAGE		= "email_message";
																				// 12/01/2014 ECU added
	public static final String 		PARAMETER_EPG_REFRESH		= "epg_refresh";// 19/10/2015 ECU added
	public static final String 		PARAMETER_FILTER			= "filter";
	public static final String 		PARAMETER_FOLDER			= "folder";
	public static final String 		PARAMETER_FILE_NAME      	= "FILE_NAME";
	public static final String 		PARAMETER_FILE_PATH			= "FILE_PATH";
	public static final String		PARAMETER_FINISH			= "finish";		// 01/06/2016 ECU added
	public static final String		PARAMETER_FINISH_ACTION		= "finish_action";
																				// 12/10/2020 ECU added
	public static final String 		PARAMETER_FLING_ENABLED		= "fling_enabled";
																				// 18/02/2014 ECU added
	public static final String 		PARAMETER_HELP_ID			= "HELP_ID";	// 17/02/2014 ECU added
	public static final String 		PARAMETER_HELP_METHOD		= "help_method";// 15/06/2015 ECU added
	public static final String 		PARAMETER_HELP_TEXT			= "HELP_TEXT";	// 13/02/2014 ECU added
	public static final String 		PARAMETER_HOSPITAL			= "hospital";	// 10/11/2014 ECU added
	public static final String 		PARAMETER_HOUR				= "hour";		// 15/01/2014 ECU added
	public static final String 		PARAMETER_ID				= "ID";			// 08/02/2015 ECU added
	public static final String 		PARAMETER_IMAGE_HANDLER		= "image_handler";
																				// 13/06/2015 ECU added
	public static final String 		PARAMETER_IMMEDIATE			= "immediate";	// 17/03/2015 ECU added
	public static final String 		PARAMETER_INITIAL_POSITION
																= "InitialPosition";
																				// 27/02/2015 ECU added
	public static final String 		PARAMETER_INTENT_DATA    	= "INTENT_DATA";// 16/10/2014 ECU added
	public static final String 		PARAMETER_IP_ADDRESS		= "ip_address";	// 11/11/2016 ECU added
	public static final String 		PARAMETER_IR_DEVICE			= "IR_DEVICE";
	public static final String 		PARAMETER_IR_CODE			= "IR_CODE";
	public static final String 		PARAMETER_LAYOUT			= "layout";		// 27/03/2016 ECU added
	public static final String		PARAMETER_LEGEND			= "legend";		// 23/09/2017 ECU added
	public static final String 		PARAMETER_LIST				= "LIST";		// 29/03/2014 ECU added
	public static final String 		PARAMETER_LOOPER 			= "looper";
	public static final String 		PARAMETER_MEDICATION		= "medication";	// 15/01/2014 ECU added
	public static final String 		PARAMETER_MENU				= "menu";		// 06/01/2020 ECU added
	public static final String 		PARAMETER_MESSAGE			= "MESSAGE";
	public static final String 		PARAMETER_MESSAGE_HTML		= "message_html";
																				// 28/03/2016 ECU added
	public static final String 		PARAMETER_METHOD			= "METHOD";		// 25/03/2014 ECU added
	public static final String 		PARAMETER_METHOD_DEFINITION	= "METHOD_DEF";	// 18/12/2015 ECU added
	public static final String 		PARAMETER_MINUTE			= "minute";		// 15/01/2014 ECU added
	public static final String 		PARAMETER_NETWORK_MASK		= "network_mask";	
																				// 11/11/2016 ECU added
	public static final String 		PARAMETER_NOTES				= "notes";		// 09/10/2016 ECU added
	public static final String 		PARAMETER_NOTIFICATION		= "notification";	
																				// 01/06/2017 ECU added
	public static final String 		PARAMETER_OBJECT		    = "OBJECT";		// 29/03/2014 ECU added
	public static final String 		PARAMETER_OBJECT_TYPE    	= "OBJECT_TYPE";// 24/03/2014 ECU added
																				// ... see OBJECT_ ...
																				// 29/03/2014 ECU changed name
	public static final String 		PARAMETER_MUSIC_DURATION	= "duration";	// 02/01/2018 ECU added
	public static final String 		PARAMETER_MUSIC_VOLUME		= "volume";		// 02/01/2018 ECU added
	public static final String 		PARAMETER_PANIC				= "panic";		// 27/11/2015 ECU added
	public static final String 		PARAMETER_PANIC_INITIALISE	= "panic_initialise";		
																				// 26/12/2015 ECU added
	public static final String 		PARAMETER_PHRASE			= "phrasetospeak";
																				// 11/02/2014 ECU added
	public static final String 		PARAMETER_PHONE_NUMBER		= "phonenumber";
																				// 29/10/2014 ECU added
	public static final String 		PARAMETER_POSITION			= "position";	// 10/11/2014 ECU added
	public static final String 		PARAMETER_PROMPT			= "PROMPT";		// 26/11/2015 ECU added
	public static final String 		PARAMETER_PROMPT_BODY		= "PROMPT_BODY";		
																				// 21/12/2015 ECU added
	public static final String 		PARAMETER_PROMPT_LEGEND		= "PROMPT_LEGEND";	
																				// 21/12/2015 ECU added
	public static final String 		PARAMETER_PROMPT_BUTTON		= "PROMPT_BUTTON";		
																				// 21/12/2015 ECU added
	public static final String 		PARAMETER_RECORDED_NOTES	= "recorded_notes";	
																				// 23/10/2016 ECU added
	public static final String 		PARAMETER_RECORDER_START	= "recorder_start";	
																				// 11/06/2017 ECU added
	public static final String 		PARAMETER_REMOTE			= "remote";		// 10/01/2015 ECU added
	public static final String 		PARAMETER_RESTART			= "restart";	// 10/06/2016 ECU added
	public static final String 		PARAMETER_SCALE				= "scale";		// 09/08/2016 ECU added
	public static final String 		PARAMETER_SELECT			= "SELECT";
	public static final String 		PARAMETER_SELECTION			= "SELECTION";	// 24/03/2014 ECU added
	public static final String 		PARAMETER_SELECTOR			= "SELECTOR";	// 29/03/2014 ECU added
	public static final String 		PARAMETER_SEND_ON_EXIT		= "send_on_exit";
																				// 14/07/2015 ECU added
	public static final String 		PARAMETER_SHOP				= "shop";		// 24/04/2018 ECU added
	public static final String 		PARAMETER_SORT				= "SORT";		// 30/03/2014 ECU added
	public static final String		PARAMETER_SORT_INHIBIT		= "SORT_INHIBIT";
																				// 23/09/2017 ECU added
	public static final String 		PARAMETER_SPEAK				= "SPEAK";		// 09/02/2015 ECU added
	public static final String 		PARAMETER_SPEAK_METHOD		= "speak_method";
																				// 23/10/2016 ECU added
	public static final String 		PARAMETER_SPOKEN_PHRASE		= "spoken_phrase";	
																				// 27/03/2016 ECU added
	public static final String 		PARAMETER_START_TIME		= "START_TIME";	// 12/01/2014 ECU added
	public static final String 		PARAMETER_SWIPE_METHOD		= "SWIPEMETHOD";// 09/06/2015 ECU added
	public static final String 		PARAMETER_TASKS				= "TASKS";		// 13/01/2014 ECU added
	public static final String 		PARAMETER_TESTING_LEVEL		= "testing_level";
																				// 11/11/2015 ECU added
	public static final String 		PARAMETER_TIMER				= "TIMER";		// 07/02/2015 ECU added
	public static final String 		PARAMETER_TONES				= "TONES";		// 18/08/2015 ECU added
	public static final String 		PARAMETER_TYPE				= "TYPE";		// 29/03/2014 ECU added
	public static final String		PARAMETER_URL				= "url";		// 19/12/2016 ECU added
	public static final String 		PARAMETER_USER_VIEW			= "USER_VIEW";	// 03/10/2014 ECU added
	public static final String 		PARAMETER_VIDEO_STREAM		= "video_stream";
																				// 18/09/2017 ECU added
	public static final String 		PARAMETER_VISIT				= "VISIT";		// 29/08/2015 ECU added
	public static final String 		PARAMETER_WAIT_TIME			= "waittime";	// 13/02/2014 ECU added
	public static final String      PARAMETER_WEBVIEW_CLIENT	= "client";		// 18/01/2020 ECU added
	public static final String 		PARAMETER_WEMO_DEVICE		= "wemo_device";// 21/03/2018 ECU added
	// -----------------------------------------------------------------------------
	// 10/12/2013 ECU declare any request codes
	// 10/05/2020 ECU added '..BLUETOOTH...'
	// -----------------------------------------------------------------------------
	public static final int 		REQUEST_BLUETOOTH_ENABLE 	= 4245;		// 10/05/2020 ECU added
																			//   B = 42, E = 45
	public static final int			REQUEST_CODE_CAMERA			= 4341;		// 20/02/2017 ECU added
																			//   C = 43, A = 41
	public static final int 		REQUEST_CODE_EMAIL_MESSAGE	= 6977;		// 14/07/2015 ECU added
																			//   E = 45, M = 4D
																			//   45=69, 4D=77
	public static final int 		REQUEST_CODE_EPG			= 6980;		// 29/09/2015 ECU added
																			//   E = 45, P = 50
																			//   45=69, 50=80
	public static final int 		REQUEST_CODE_FILE			= 666;
	public static final int 		REQUEST_CODE_DEVICE_ADMIN	= 6616;		// 27/11/2015 ECU added
	public static final int 		REQUEST_CODE_DOSE			= 1234;		// 17/01/2014 ECU added
	public static final int 		REQUEST_CODE_FINISH			= 4649;		// 17/10/2014 ECU added
	public static final int 		REQUEST_CODE_SETTINGS		= 3344;		// 03/10/2014 ECU added
	public static final int			REQUEST_CODE_VIDEO			= 5649;		// 12/10/2020 ECU added
																			//   V = 56, I = 49
	// -----------------------------------------------------------------------------
	// 17/10/2014 ECU declare a result code that requests a 'finish' of the
	//                calling activity
	// -----------------------------------------------------------------------------
	public static final int			RESULT_CODE_BARCODE			= 987;		// 20/11/2015 ECU added
	public static final int			RESULT_CODE_BARCODE_EDIT	= 9877;		// 09/04/2018 ECU added
	public static final int			RESULT_CODE_BARCODE_NEW		= 9876;		// 20/11/2015 ECU added
	public static final int 		RESULT_CODE_FINISH			= 4649;		// 17/10/2014 ECU added
	public static final int 		RESULT_CODE_INTENT			= 4948;		// 28/11/2014 ECU added
	public static final int 		RESULT_CODE_SHOPPING        = 5348;		// 11/09/2015 ECU added
	// -----------------------------------------------------------------------------
	// 14/12/2015 ECU declare the screen capture variables
	// -----------------------------------------------------------------------------
	public static final String		SCREEN_CAPTURE_FILE			= "ScreenCapture";
	public static final int			SCREEN_CAPTURE_KEY			= KeyEvent.KEYCODE_VOLUME_UP;
	public static final int			SCREEN_CAPTURE_KEY_COUNT	= 5;
	// -----------------------------------------------------------------------------
	// 29/11/2015 ECU declare the server commands
	// 20/01/2016 ECU removed CAPABILITY because network discovery is handled
	//                in a much cleaner way
	// -----------------------------------------------------------------------------
	public static final String		SERVER_COMMAND				= "command";
	public static final String		SERVER_COMMAND_CANCEL_CALL	= "cancelphonecall";
	public static final String		SERVER_COMMAND_LOCATE		= "locate";
	public static final String		SERVER_COMMAND_MESSAGE		= "message";
	public static final String		SERVER_COMMAND_PHONE		= "phone";
	// -----------------------------------------------------------------------------
	// 11/08/2020 ECU changed to use ServiceControl instead of just declaring the
	//                service class - this is so that validation can take place
	// 12/08/2020 ECU changed the name from SERVICES_TO_STOP
	//            ECU Note - the strings are the methods in the service class to
	//                       perform 'validation' or 'modifications to the intent'
	//                       that is passed to the service at 'onCreate'.
	//                       A 'null' indicates that 'no validation' is required
	//                       The initial 'boolean' indicates if the service is to be
	//                       started when the app begins
	// -----------------------------------------------------------------------------
	public static ServiceControl  [] SERVICES_TO_HANDLE =
														{
															// ---------------------
															new ServiceControl (TextToSpeechService.class,true,null,"updateIntent"),
															new ServiceControl (TimerService.class,true),
															new ServiceControl (ServerService.class,true),
															new ServiceControl (WebServerService.class,true),
															new ServiceControl (WeMoService.class,true,"validation"),
															new ServiceControl (SensorService.class,true,"validation"),
															new ServiceControl (MonitorService.class,false),
															new ServiceControl (BluetoothService.class,true,"validation"),
															new ServiceControl (BluetoothConnectorService.class,true,"validation","updateIntent")
															// ---------------------
														};
	// -----------------------------------------------------------------------------
	// 19/08/2013 ECU declare the bits in the byte of data sent with socket
	//                messages
	// ------------------------------------------------------------------------------
	public static final int 		SOCKET_DATA_FILE		 	= 0;		// 19/08/2013 ECU created - file toggle
	public static final int 		SOCKET_DATA_RETRY			= 1;		// 19/08/2013 ECU created - file transmission retry
	// =============================================================================
	// 10/01/2016 ECU NOTE - do NOT change the order of the following list because
	//                it relates to messages sent between devices - so if the list
	//                is changed then the software on ALL devices in the system
	//                will need to be updated
	// -----------------------------------------------------------------------------
	public static final int 		SOCKET_MESSAGE_UNSET		  = -1;		// 07/08/2013 ECU indicates message not set yet
	public static final int			SOCKET_MESSAGE_WAV_FILE		  = 0;		// 30/07/2013 ECU indicate wav file in buffer
	public static final int			SOCKET_MESSAGE_DATAGRAM		  = 1;		// 02/08/2013 ECU indicate a datagram
	public static final int 		SOCKET_MESSAGE_DATAGRAM_ACTION= 2;		// 03/08/2013 ECU indicate a datagram and then action
	public static final int 		SOCKET_MESSAGE_FILE           = 3;		// 03/08/2013 ECU indicate a file transfer
	public static final int 		SOCKET_MESSAGE_PLAY		 	  = 4;		// 03/08/2013 ECU indicate that the file send at 3 is to be played
	public static final int 		SOCKET_MESSAGE_PLAYED	      = 5;		// 03/08/2013 ECU indicates that a track being played has finished
	public static final int 		SOCKET_MESSAGE_FILE_CHUNKS    = 6;		// 04/08/2013 ECU as .._FILE but transfer is in chunks rather than whole
	                                                                //                useful for large files
	public static final int 		SOCKET_MESSAGE_STREAMING      = 7;		// 05/08/2013 ECU indicates audio streaming
	public static final int 		SOCKET_MESSAGE_START_STREAM	  = 8;      // 06/08/2013 ECU tells receiving device to start streaming
	public static final int 		SOCKET_MESSAGE_STOP_STREAM	  = 9;      // 06/08/2013 ECU tells receiving device to stop streaming
	public static final int 		SOCKET_MESSAGE_FILE_ACK		  = 10;		// 14/08/2013 ECU indicates file received OK
	public static final int 		SOCKET_MESSAGE_FILE_NAK		  = 11;     // 14/08/2013 ECU indicates file not received OK
	public static final int 		SOCKET_MESSAGE_FILE_DETAILS	  = 12;     // 02/02/2014 ECU added - transfer of FILE associated with a filename
	public static final int 		SOCKET_MESSAGE_REQUEST_FILE	  = 13;		// 02/02/2014 ECU added - request a specified file
	public static final int 		SOCKET_MESSAGE_REQUESTED_FILE = 14;		// 02/02/2014 ECU added - requested file
	public static final int 		SOCKET_MESSAGE_FILE_INFO	  = 15;		// 19/03/2014 ECU added - send 'File' details
	public static final int 		SOCKET_MESSAGE_CLONE_START	  = 16;		// 06/04/2014 ECU added - cloning is being started
	public static final int 		SOCKET_MESSAGE_CLONE_FINISH	  = 17;		// 06/04/2014 ECU added - cloning is being finished	
	//public static final int 		SOCKET_MESSAGE_CLONE_MODE	  = 18;		// 06/04/2014 ECU added - check clone mode
																			// 03/05/2015 ECU removed
	//public static final int 		SOCKET_MESSAGE_CLONE_ACK	  = 19;		// 06/04/2014 ECU added - can be cloned
																			// 03/05/2015 ECU removed
	//public static final int 		SOCKET_MESSAGE_CLONE_NAK	  = 20;		// 06/04/2014 ECU added - cannot be cloned	
																			// 03/05/2015 ECU removed
	public static final int 		SOCKET_MESSAGE_SEND_CHUNKS	  = 21;		// 07/04/2014 ECU added - send file in chunks
	public static final int 		SOCKET_MESSAGE_SEND_CHUNK	  = 22;     // 07/04/2014 ECU added - send a chunk
	public static final int 		SOCKET_MESSAGE_CHUNK_ACK	  = 23;     // 07/04/2014 ECU added - ack a chunk
	public static final int 		SOCKET_MESSAGE_CHUNK_NAK	  = 24;     // 07/04/2014 ECU added - nak a chunk
	public static final int 		SOCKET_MESSAGE_SEND_CHUNK_END = 25;     // 07/04/2014 ECU added - end of chunk send
	public static final int 		SOCKET_MESSAGE_SPEAK_A_PHRASE = 26;		// 28/01/2015 ECU added - speak a phrase
	public static final int 		SOCKET_MESSAGE_REQUEST_DETAILS= 27;		// 20/03/2015 ECU added - request device details
	public static final int 		SOCKET_MESSAGE_SENT_DETAILS	  = 28;		// 20/03/2015 ECU added - sent device details
	public static final int 		SOCKET_MESSAGE_REQUEST_STATUS = 29;		// 02/05/2015 ECU added - request device status
	public static final int 		SOCKET_MESSAGE_STATUS_RESPONSE= 30;		// 02/05/2015 ECU added - send device status
	//public static final int       SOCKET_MESSAGE_CHUNKS_HEADER  = 31;		// 04/01/2016 ECU added - use in FileSendDetails
																			// 10/01/2016 ECU removed
	//public static final int       SOCKET_MESSAGE_CHUNKS_DATA    = 32;		// 04/01/2016 ECU added - use in FileSendDetails
																			// 10/01/2016 ECU removed
	//public static final int       SOCKET_MESSAGE_CHUNKS_ACK     = 33;		// 04/01/2016 ECU added - use in FileSendDetails
																			// 10/01/2016 ECU removed
	//public static final int       SOCKET_MESSAGE_CHUNKS_NAK     = 34;		// 04/01/2016 ECU added - use in FileSendDetails	
																			// 10/01/2016 ECU removed
	public static final int			SOCKET_MESSAGE_FILE_TRANSFER  = 35;		// 09/01/2016 ECU added - use for FileTransfer
	public static final int			SOCKET_MESSAGE_REQUEST_DETAILS_RAW
																  = 36;		// 20/01/2016 ECU added - send device details directly
	public static final int			SOCKET_MESSAGE_OBJECT		  = 37;		// 27/02/2016 ECU added - send an object
	public static final int			SOCKET_MESSAGE_MONITOR		  = 38;		// 26/04/2016 ECU added - monitor receiving device
	public static final int			SOCKET_MESSAGE_MONITOR_DATA	  = 39;		// 26/04/2016 ECU added - monitor data
	public static final int			SOCKET_MESSAGE_ACTIONS		  = 40;		// 15/06/2016 ECU added - process actions
	public static final int			SOCKET_MESSAGE_ARE_YOU_THERE  = 41;		// 17/06/2016 ECU added - asking if device is present
	public static final int			SOCKET_MESSAGE_I_AM_HERE	  = 42;		// 17/06/2016 ECU added - device says it is present
	public static final int			SOCKET_MESSAGE_REQUESTED_FILE_ERROR 
																  = 43;		// 14/12/2016 ECU added - requested file error
	public static final int			SOCKET_MESSAGE_SYNC_ACK		  = 44;		// 25/03/2017 ECU added - sync on a file is complete
	public static final int			SOCKET_MESSAGE_VIDEO_STREAM	  = 45;		// 15/09/2017 ECU added - video streaming data
	public static final int			SOCKET_MESSAGE_VIDEO_STREAM_START
																  = 46;		// 15/09/2017 ECU added - start video streaming
	public static final int			SOCKET_MESSAGE_VIDEO_STREAM_STOP
																  = 47;		// 15/09/2017 ECU added - stop video streaming
	public static final int			SOCKET_MESSAGE_CANCEL_REMOTE_PLAY
	  															  = 48;		// 25/03/2019 ECU added - cancel remote playing
	public static final int			SOCKET_MESSAGE_PHONE_CAPABILITY
																  = 49;		// 24/01/2020 ECU added - request phone capability
	// -----------------------------------------------------------------------------
	// 31/10/2015 ECU add some commands used by the Text To Speech methods
	// -----------------------------------------------------------------------------
	public static final String		TTS_COMMENT					= "//";
	public static final String		TTS_SILENCE					= "<Silence>";
	// -----------------------------------------------------------------------------
	// 11/09/2015 ECU declare the states for the video recorder
	// -----------------------------------------------------------------------------
	public static final int 		VIDEO_RECORDER_PLAY 		= 1;
	public static final int 		VIDEO_RECORDER_RECORD 		= 2;
	public static final int 		VIDEO_RECORDER_STOP 		= 3;
	// -----------------------------------------------------------------------------
	// 03/09/2015 ECU voice commands - the order is IMPORTANT do NOT rearrange
	//                alphabetically
	// -----------------------------------------------------------------------------
	public static final int 		VOICE_COMMAND_PLAY_MUSIC	=	0;
	public static final int 		VOICE_COMMAND_SHOW_PHOTOS	=	1;
	public static final int 		VOICE_COMMAND_GAME_ONE		=	2;
	public static final int 		VOICE_COMMAND_GAME_TWO		=	3;
	public static final int 		VOICE_COMMAND_TIME			=   4;
	public static final int 		VOICE_COMMAND_PHONE			=	5;
	public static final int 		VOICE_COMMAND_MAIL			=	6;
	public static final int 		VOICE_COMMAND_INTERNET		=   7;
	public static final int 		VOICE_COMMAND_HELP          =   8;
	public static final int 		VOICE_COMMAND_COMPASS 		=   9;
	public static final int 		VOICE_COMMAND_GOOGLE        =  10;
	public static final int 		VOICE_COMMAND_REPEAT        =  11;
	public static final int 		VOICE_COMMAND_WHOAMI		=  12;
	public static final int 		VOICE_COMMAND_COMMANDS      =  13;
	public static final int 		VOICE_COMMAND_SPEAK			=  14;		// 18/06/2013 ECU added
	public static final int 		VOICE_COMMAND_BIRTHDAY      =  15;      // 18/06/2013 ECU added 
	public static final int 		VOICE_COMMAND_SMS			=  16;      // 20/06/2013 ECU added
	public static final int 		VOICE_COMMAND_READ_FILE     =  17;      // 20/06/2013 ECU read the specified file
	public static final int 		VOICE_COMMAND_LISTEN_START	=  18;		// 06/08/2013 ECU start audio streaming
	public static final int 		VOICE_COMMAND_LISTEN_STOP	=  19;		// 06/08/2013 ECU stop audio streaming
	public static final int 		VOICE_COMMAND_DEVICES		=  20;		// 06/08/2013 ECU lists the known devices
	public static final int 		VOICE_COMMAND_BROADCAST     =  21;		// 28/08/2013 ECU multicast option
																			// 09/04/2016 ECU changed to use term BROADCAST as this
																			//                is correct - multicast is not used
	public static final int 		VOICE_COMMAND_TELEVISION_ON	=  22;		// 19/12/2013 ECU television handler
	public static final int 		VOICE_COMMAND_LAMP_ON		=  23;		// 20/02/2015 ECU turn lamp on
	public static final int 		VOICE_COMMAND_LAMP_OFF		=  24;		// 20/02/2015 ECU turn lamp off
	public static final int 		VOICE_COMMAND_CANCEL_CALL	=  25;		// 29/11/2015 ECU cancel phone call
	// -----------------------------------------------------------------------------
	// 24/07/2017 ECU declare the lengths of the Westminster Chimes - clearly if
	//                the raw files change then so will these values which are in
	//                milliSeconds
	// 26/07/2017 ECU declare ..._TAIL and _TAIL_HOUR which is the 'tailing off' of 
	//                the chime and is deducted from the figures below to indicate 
	//                when the 'speaking clock' can announce the time.
	//
	//                The _HOUR is different because the number of 'hour gongs' needs
	//                to be calculated.
	// -----------------------------------------------------------------------------
	public static final int			WESTMINSTER_CHIME_TAIL		=  5000;
	public static final int			WESTMINSTER_CHIME_TAIL_HOUR	=  3000;
	public static final int			WESTMINSTER_CHIME_HALF		=  18103 - WESTMINSTER_CHIME_TAIL;
	public static final int			WESTMINSTER_CHIME_HOUR		=  32705 - 5045;
	public static final int			WESTMINSTER_CHIME_PER_HOUR	=  5045;
	public static final int			WESTMINSTER_CHIME_QUARTER	=  11520 - WESTMINSTER_CHIME_TAIL;
	public static final int			WESTMINSTER_CHIME_THREE_QUARTER
																=  24503 - WESTMINSTER_CHIME_TAIL;
	// -----------------------------------------------------------------------------
	// 24/07/2017 ECU Note - declare others variables associated with the chime
	// -----------------------------------------------------------------------------
	public static final int			WESTMINSTER_CHIME_GAP		= 15;		// 11/03/2017 ECU the gap between each Westminster Chime in minutes
	public static final long		WESTMINSTER_CHIME_GAP_MS	= (long) (WESTMINSTER_CHIME_GAP * MILLISECONDS_PER_MINUTE);		
																			// 11/03/2017 ECU the gap between each Westminster Chime in milliseconds
	public static final int			WESTMINSTER_CHIME_HOUR_POSITION
																= WESTMINSTER_CHIME_HOUR;	
																			// 11/03/2017 ECU this is the position in mS within the
																			//                'westminster_hour' raw file that the hour 'gong' starts
																			// 24/07/2017 ECU use the value from above rather than
																			//                recalculating it
	// =============================================================================
	// 22/02/2019 ECU declare the options for sorting the WiFi information
	// -----------------------------------------------------------------------------
	public static final int			WIFI_SORT_NONE				= 0;
	public static final int			WIFI_SORT_NETWORK			= 1;
	public static final int			WIFI_SORT_STRENGTH			= 2;
    // =============================================================================
}

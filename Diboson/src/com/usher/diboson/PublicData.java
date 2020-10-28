package com.usher.diboson;

import android.app.AlarmManager;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.ImageView;

import java.net.DatagramSocket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PublicData 
{
	// =============================================================================
	// 04/09/2015 ECU created to hold data which is public to all classes
	// 23/10/2015 ECU ambientLightTriggered moved into storedData
	// 20/02/2017 ECU removed data that was used by LocationActivity
	// 19/03/2017 ECU added 'debuggable' flag
	// 26/03/2017 ECU added commsHandler
	// 02/06/2017 ECU added actionIntent
	// 15/07/2017 ECU added 'visitLogFile' and delete
	//					public static List<Visit> visits = new ArrayList<Visit>();
	// 15/01/2018 ECU added audioStreaming
	// 30/11/2018 ECU added externalSDCard
	// 27/02/2019 ECU added publicIpAddress
	// 18/07/2019 ECU move publicIpAddress into stored data
	// 30/07/2019 ECU added scaleSecondLine and scaleThirdLine
	// 11/11/2019 ECU added ntpRefreshedStatus
	// 16/11/2019 ECU added deviceNames
	// 04/12/2019 ECU added emailMessages
	// 06/12/2019 ECU added emailHandler
	// 26/03/2020 ECU added lockFileData
	// 13/05/2020 ECU added startedByAlarmSource
	// 18/05/2020 ECU added connectedBluetoothDevices
	// 30/07/2020 ECU added initialised so that can flag when the variables have
	//                      been fully initialised
	// 03/09/2020 ECU added 'actionMediaPlayer' which is used by Utilities.PlayAFileAction
	// 17/09/2020 ECU removed
	// 						public static ImageAdapter 	  		imageAdapter;
	//						public static CustomGridViewAdapter	customGridViewAdapter;
	//				  which are now declared in GridActivity
	// =============================================================================
	public static Intent			actionIntent				= null;
	public static MediaPlayer       actionMediaPlayer			= null;
	public static List<String []>	actions						= new ArrayList<String []>();
																			// 03/05/2016 ECU list of 'actions' to be taken
	public static List<Agency>	  	agencies 					= new ArrayList<Agency>();
																			// 12/01/2014 ECU list of registered agencies	
	public static List<AlarmData> 	alarmData 					= new ArrayList<AlarmData>();
																			// 06/02/2015 ECU changed from a simple array
	public static SimpleDateFormat 	alarmDateFormat;						// 07/02/2015 ECU added 
	public static String		  	alarmFileName;							// 08/02/2015 ECU added - where alarms are stored
	public static AlarmManager		alarmManager;							// 03/11/2016 ECU added 
	public static SimpleDateFormat 	alarmTimeFormat;						// 07/02/2015 ECU added 
	public static List<AppointmentDetails>   
									appointments 				= new ArrayList<AppointmentDetails>();
																			// 06/01/2014 ECU added - appointments
	public static String			appointmentsSubFolder;					// 23/10/2016 ECU added - audio for appointments
																			// 25/10/2016 ECU changed to SubFolder
	public static List<AppointmentTypeDetails>
									appointmentTypes 			= new ArrayList<AppointmentTypeDetails>();
																			// 06/01/2013 ECU added - appointment types
	public static boolean			audioStreaming				= false;	// 15/01/2018 ECU added - if audio streaming is on or off
	public static List<BarCode>	  	barCodes 					= new ArrayList<BarCode>();	
																			// 15/09/2013 ECU added - declare the bar code data
																			// 07/02/2014 ECU changed to a list
																			// 03/02/2014 ECU added the initialisation
	public static boolean		  	blueToothService 			= false;	// 02/03/2014 ECU added - indicates if bluetooth
    																		//                service initialised correctly
	public static BluetoothUtilities	
									bluetoothUtilities			= null;		// 06/09/2013 ECU added - to handle bluetooth
																			//			      utilities
	//public static String         	broadcastMessage 			= null;		// 27/08/2013 ECU if non-null then indicates multicast message to
																			//                be sent
																			// 09/04/2016 ECU changed name from multicast..
																			// 25/05/2020 ECU removed following rewrite of BroadcastServerThread
	//public static boolean		  	broadcastMessages 			= false;	// 22/08/2013 ECU added - checks for multicast messages in TimerService
																			// 09/04/2016 ECU changed name from multicast...
																			// 25/05/2020 ECU removed following rewrite of BroadcastServerThread
	public static CarePlan		  	carePlan 					= new CarePlan ();	
																			// 12/01/2014 ECU added - care plan
	public static String		  	carerLogFile;							// 10/01/2014 ECU added - name of carer log file
	public static boolean			carerRefreshWanted			= false;	// 04/10/2016 ECU added - carer display refresh wanted
	public static List<Carer>	  	carers 						= new ArrayList<Carer>();
																			// 09/01/2014 ECU list of registered carers	
	public static boolean         	cellChange 					= false;
	public static int             	chunkResponse  				= StaticData.NO_RESULT;	
																			// 07/04/2014 ECU added
	public static String		  	clonerIPAddress 			= StaticData.BLANK_STRING;		
																			// 16/10/2014 ECU added - the IP address
																			//                of the cloner device
	public static boolean		  	cloningInProgress 			= false;	// 06/04/2014 ECU added - cloning is
																			//                happening
	public static CommsHandler		commsHandler;							// 26/03/2017 ECU added
	public static List <BluetoothDevice>
									connectedBluetoothDevices	= null;		// 18/05/2020 ECU added
	public static String    	  	copyrightMessage			= StaticData.BLANK_STRING;		
																			// 12/09/2013 ECU added - holds printable copyright message
																			// 09/03/2017 ECU change initialise
	public static int			  	currentRemoteController 	= 0;		// 11/05/2015 ECU added - the current remote controller
	public static long			  	currentTimeAdjustment 		= 0;		// 13/08/2013 ECU added - difference between current time
																			//				  and NTP time
	public static Datagram		  	datagram 					= null;		// 02/08/2013 ECU added
																			// 21/09/2013 ECU initialised to null
	public static boolean         	datagramChanged 			= false;	// 19/10/2014 ECU added - datagram has changed
	public static boolean         	datagramEnabled 			= true;		// 02/08/2013 ECU added - indicates if datagrams are to be transmitted
	public static String          	datagramIPAddress 			= null;     // 03/08/2013 ECU added - address for sending datagrams
	public static String		  	datagramReceiver 			= null;		// 02/08/2013 ECU added - receiver of datagrams
	public static DatagramSocket  	datagramSocket 				= null;		// 22/08/2013 ECU added - used for receiving multicast messages
																			// 09/04/2016 ECU changed from 'multicastSocket' as the name
																			//                is misleading - just using 'broadcasts'
	public static boolean         	datagramToAction 			= false;	// 03/08/2013 ECU added - indicate datagram is to be action
	public static boolean  	      	datagramToSend 				= false;	// 02/08/2013 ECU added - indicates if a datagram is to be sent
	public static int			  	datagramType 				= StaticData.NO_RESULT;		
																			// 21/03/2015 ECU added - the type of datagram being sent
	public static String			dateFormatDDMMYY;						// 28/10/2016 ECU added
	public static String			dateFormatDDMMYYYY;						// 28/10/2016 ECU added
	public static SimpleDateFormat	dateSimpleFormatDDMMYYHHMM;				// 30/10/2016 ECU added
	public static SimpleDateFormat	dateSimpleFormatHHMMDDMMYY;				// 30/10/2016 ECU added
	public static SimpleDateFormat 	dateFormatter;							// 13/08/2013 ECU added 
	public static SimpleDateFormat 	dateFormatterCurrent 		= null;		// 04/04/2014 ECU added 
	public static SimpleDateFormat 	dateFormatterFull;						// 11/01/2013 ECU added 
	public static SimpleDateFormat 	dateFormatterShort;						// 13/01/2013 ECU added 
	public static SimpleDateFormat 	dateSimpleFormat;						// 27/10/2016 ECU added 
	public static String          	dateTimeString 				= StaticData.BLANK_STRING;       
																			// 20/11/2013 ECU added - updated by TimerService
	public static boolean			debuggable					= true;		// 19/03/2017 ECU added
	                                                                        //                true .... debug version
																			//                false ... release version
	public static String []			daysOfTheWeek;							// 11/12/2016 ECU added - days of the week
	public static boolean			deviceAdminEnabled			= false;	// 29/11/2015 ECU added - moved here from PanicAlarmActivity
	public static int			  	deviceCode 					= StaticData.NO_RESULT;	
																			// 01/03/2014 ECU added - remote controller id
	public static List<Devices>	  	deviceDetails 				= null;		// 22/03/2015 ECU details of locally 'found' devices
																			//            ECU changed from Devices [] to List <Devices>
																			//            ECU preset to 'null' rather than new ArrayList<Devices>()
																			//                to get correct discovery is problems occur
																			//                on initialisation
	public static String          	deviceID 					= null;		// 03/02/2015 ECU added - unique (?) ID for the device
	public static List<DeviceName>	deviceNames 				= new ArrayList<DeviceName>();
	public static String		  	dialogueFolder;							// 16/11/2019 ECU added
	public static boolean         	dibosonViewOnDisplay 		= false;	// 15/10/2013 ECU added
	public static boolean		  	discover_always 			= false;	// 07/11/2013 ECU added
	public static String			epgFolder					= null;		// 16/10/2015
	public static EmailDetails   	emailDetails 				= null;		// 04/01/2014 ECU added
	public static EmailHandler		emailHandler;							// 06/12/2019 ECU added
	public static List<EmailMessage>
									emailMessages				= new ArrayList <EmailMessage>();
	// 02/02/2014 ECU list of files to synchronise
	public static String		  	errorSoFinishApp 			= null;		// 11/04/2015 ECU added
	public static boolean         	externalData 				= true;		// 18/06/2013 ECU added
	public static String			externalSDCard				= null;		// 30/11/2018 ECU added - path to external SD card
	public static int			  	fileTransferStatus 			= StaticData.NO_RESULT;	
																			// 14/08/2013 ECU indicates the file transfer state
	public static List<FileToSynchronise>   
								  	filesToSynchronise			= new ArrayList <FileToSynchronise>();
																			// 02/02/2014 ECU list of files to synchronise
	public static boolean         	gettingMedication 			= false;	// 22/06/2013 ECU user entering medication
	public static boolean		  	gridActivityEntered 		= false;	// 31/12/2014 ECU added to indicate that GridActivity
																			//                has been entered
	public static boolean         	gridRebuild 				= false;    // 16/01/2015 ECU added to indicate that GridActivity
																			//                must rebuild the display
	public static boolean		  	gridType 					= false;	// 11/10/2014 ECU remember state of displayed grid
	public static SocketMessageHeader 
								  	incomingSocketMessageHeader;			// 07/08/2013 ECU added
    public static boolean           initialised                 = false;    // 30/07/2020 ECU added
	public static String 		 	ipAddress					= null;		// 25/07/2013 ECU added
	public static long				keyValue					= StaticData.SHARED_PREFERENCES_DEFAULT;
																			// 08/03/2017 ECU added
	public static String    	 	lastUpdateDate 				= StaticData.BLANK_STRING;		
																			// the date when the APK was last updated
	public static String		 	lastUpdateTime 				= StaticData.BLANK_STRING;		
																			// the time when the APK was last updated
	public static int			  	lastVoiceCommand 			= StaticData.NO_RESULT;	
																			// 17/06/2013 ECU will remember the last voice command actioned
	public static Devices		  	localDeviceDetails 			= new Devices ();	
																			// 20/03/2015 ECU added to hold details of this device
																			// 28/02/2016 ECU initialise the variable
	public static LockFileData		lockFileData				= null;		// 26/03/2020 ECU 'lock file' data
	public static MediaPlayer     	mediaPlayer 				= null;
	public static boolean		  	mediaPlayerPaused 			= false; 	// 16/06/2013 ECU added
	public static List<MedicationDetails>   
								  	medicationDetails 			= new ArrayList<MedicationDetails>();
																			// 16/01/2014 ECU added - changed from []
	public static boolean         	medicationReadFromDisk 		= false; 
																			// 22/06/2013 ECU indicates if medication data has been read from disk
	public static MessageHandler	messageHandler;							// 18/09/2015 ECU added the master message handler
	public static List<MonitorData> monitorData					= new ArrayList<MonitorData>();
																			// 26/04/2016 ECU added - to hold monitor data
	public static boolean           monitorDataAction			= false;	// 28/04/2016 ECU added - whether incoming monitored data
																			//                is to actioned or not
	public static MonitorHandler	monitorHandler;							// 16/12/2019 ECU declare the handler for monitoring
	public static String			monitorIPAddress			= null;		// 26/04/2016 ECU added - this is the address where monitoring
																			//                data is sent
	public static String			monitoredIPAddress			= null;		// 28/04/2016 ECU added - this is the address where monitoring
																			//                data is sent
	public static int			  	monitorInactivityCounter 	= 0;		// 28/04/2015 ECU moved here from the monitor record
	public static boolean		  	monitorServiceRunning 		= false;	// 18/11/2014 ECU indicates if monitor service running
	public static ImageView	      	mpImageView;							// 02/06/2013 ECU used in the MusicPlayer class
	public static boolean		  	musicFileReadyToPlay 		= false; 	// 19/08/2013 ECU added
	public static MusicPlayerData 	musicPlayerData				= new MusicPlayerData ();	
																			// 21/04/2015 ECU added initialisation 
																			// 31/08/2017 ECU changed initialisation to new ...
	public static boolean         	musicPlayerRemote 			= false;	// 03/08/2013 ECU added - whether the remote player is running
	public static String		  	musicServer 				= null;		// 03/08/2013 ECU added - the IP address of the device supplying the music
																			// 21/09/2013 ECU preset to null
	public static List<NamedAction>   
  									namedActions 				= new ArrayList<NamedAction>();
																			// 03/08/2016 ECU added
	public static String	      	networkMask;							// 01/08/2013 ECU added - the network mask
																			// 12/11/2013 ECU changed from int to String
	public static int			  	ntp_counter 				= 2;		// 12/08/2013 ECU added - minute counter used in
																			//                conjunction with NTP_REFRESH_RATE
																			// 20/12/2015 ECU changed from 0 to 2 because
																			//                occasionally getting NPE on start up
	public static boolean			ntpRefreshedStatus			= false;	// 11/11/2019 ECU added to remember how the request to
																			//                the NTP server worked
	public static SocketMessageHeader 
									outgoingSocketMessageHeader;			// 07/08/2013 ECU added
	public static PatientDetails  	patientDetails 				= null;		// 05/01/2014 ECU added - patient details
																			// 03/02/2014 ECU added initialisation
	public static String          	phoneNumber 				= null;		// 27/10/2014 ECU added to store the phone number
																			//                of this device.
	public static String		  	phoneServer 				= null;		// 25/07/2012 ECU added
	public static String          	photosFolder;
	public static String	      	photosNotesFolder;						// 16/11/2016 ECU changed name from photosMusicFolder
	public static int			  	playTimeout 				= StaticData.NO_RESULT;	
																			// 20/08/2013 ECU added - time to wait for play command
																			//				  after a file received
	public static String          	projectFolder;
	public static String		  	projectLogFile;							// 12/02/2014 ECU added - name of project log file
	public static String		  	receivedFile 				= null;		// 14/08/2013 ECU added - the name of the last good file received
	public static DeviceStatus	  	receivedStatus 				= null;		// 02/02/2015 ECU added - the status of the remote device
	public static boolean 		  	receivingFile 				= false;	// 13/08/2013 ECU added - indicate that receiving a file
	public static String		  	remoteControllerServer 		= null;		// 26/02/2016 ECU added - remote controller server
	public static String		  	remoteMusicPlayer 			= null;		// 05/08/2013 ECU added - address of remote music player
	public static int			 	remoteTrackCounter 			= 0;		// 10/08/2013 ECU added - counter of remote tracks played
	public static String          	requestAddress 				= null;		// 22/03/2015 ECU added - to send details
	public static int			  	requestSecond;							// 22/03/2015 ECU added - send when requestAddress will be sent
	public static float				scaleSecondLine				= 0f;		// 30/07/2019 ECU added - used in Utilities.threeLine...
	public static float				scaleThirdLine				= 0f;		// 30/07/2019 ECU added - used in Utilities.threeLine...
	public static int			 	screenHeight;							// 07/07/2013 ECU added
	public static int			  	screenWidth;							// 07/07/2013 ECU added
	public static List<SearchStringAndReplace>
								  	searchStringAndReplace 		= new ArrayList<SearchStringAndReplace>();
																			// 14/03/2014 ECU added
	public static boolean		  	sendingFile 				= false;	// 13/08/2013 ECU indicate that file is being sent
	public static ShoppingData	  	shoppingData 				= new ShoppingData ();	
																			// 29/03/2014 ECU added - all shopping data
	public static byte []         	socketHeader 				= {StaticData.ASCII_STX,StaticData.ASCII_SOH,0,0,StaticData.ASCII_ETX};
																			// 11/08/2013 ECU added - this header will be used when the
																			//                socket message type is transmitted or 
																			//				  received - the message type is stored
																			//                in element SOCKET_TYPE_OFFSET
																			// 19/08/2013 ECU added - added a data byte accessed by
																			//				  SOCKET_DATA_OFFSET
																			// 03/09/2015 ECU changed to use StaticData
	public static byte			  	socketMessageData 			= 0;		// 19/08/2013 ECU added - default to no bits set
	public static int			  	socketNumber;							// 25/07/2013 ECU added
	public static int			  	socketNumberForData;					// 29/07/2013 ECU added
	public static int			  	socketNumberForFTP;						// 09/01/2016 ECU added - ftp port number
	public static int			  	socketNumberForWeb;						// 30/12/2014 ECU added - added for website
	public static boolean           startedByAlarm	     		= false;	// 02/04/2016 ECU added to indicate whether
																			//                the app was started by an alarm
																			//                true ... started by alarm
																			//                false .. started by user or OS
	public static String			startedByAlarmSource		= null;		// 13/05/2020 ECU the source of the alarm that started the app
	public static boolean           startedManually     		= false;	// 06/10/2015 ECU added to indicate how
																			//                the app was started
																			//                true ... by the user
																			//                false .. by Android OS following a destroy
	public static boolean         	startStreaming 				= false;    // 06/08/2013 ECU added - tells the timer service to start
																			//                audio streaming
	public static String			startUpMessage				= StaticData.BLANK_STRING;		
																			// 17/10/2015 ECU added - contains details of when
																			//                and how activity was started
	public static long				startUpTime					= 0;		// 08/08/2018 ECU start up time in milliseconds
	public static DeviceStatus	  	status 						= new DeviceStatus ();	
																			// 02/05/2015 ECU added - contains the active status of
																			//                this device
	public static boolean         	stopmpPlayer 				= false;
	public static boolean         	stopStreaming 				= false;    // 06/08/2013 ECU added - tells the timer service to stop
																			//                audio streaming
	public static StoredData	  	storedData 					= new StoredData ();
																			// 07/01/2014 ECU shared data stored on disk
																			// 03/02/2014 ECU added initialisation
	public static String		  	streamingDestination 		= null;		// 05/08/2013 ECU added - destination for streaming audio
	public static List<String>	  	stringsToProcess 			= new ArrayList<String>();
																			// 24/07/2013 ECU added
																			// 06/01/2016 ECU changed to list
	public static String []			tasksToDo;								// 06/12/2016 ECU added
	public static String []			tasksToDoRaw;							// 06/12/2016 ECU added
	public static boolean         	trackBeingPlayed 			= false;	// 03/08/2013 ECU added = indicates a track being played
	public static String		  	trackFolder 				= null;		// 22/09/2013 ECU moved here from track activity
	public static boolean         	trackingMode 				= false;
	public static List<UPnPDevice>	upnpDevices 				= new ArrayList<UPnPDevice>();
																			// 26/08/2016 ECU list of found UPnP devices
	public static boolean			userInterfaceRunning		= false;	// 28/11/2015 ECU added - to indicate that the user
																			//                interface (in GridActivity) is up
																			//                and running
	public static String			visitLogFile;							// 15/07/2017 ECU added - to hold a record of carer
																			//                visits
	public static List<VoiceCommandPhrases>   
									voiceCommandPhrases 		= new ArrayList<VoiceCommandPhrases>();
																			// 21/05/2016 ECU added - user defined voice commands
	public static VoiceCommands   	voiceCommands [];
	public static String		  	webFolder;								// 24/12/2014 ECU added - folder where
	            															//                html pages stored
	public static String		  	wemoServer 					= null;		// 18/03/2015 ECU added
	public static long				westminsterChimeLast		= StaticData.NOT_SET;	
																			// 25/07/2017 ECU added
																			// 27/07/2017 ECU changed from NO_RESULT
	public static boolean 		  	writeDataOnDestroy 			= true;  	// 06/04/2014 ECU write out any data
															   				//                when activity is
    																		//                destroyed
	   																		// 16/10/2014 ECU changed to static
	public static boolean			writeDataOnDestroyForced	= false;	// 11/10/2016 ECU add to force writing to disk
																			//                irrespective of the state of
																			//                'writeDataOnDestroy'
	// =============================================================================															
}

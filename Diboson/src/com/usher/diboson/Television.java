package com.usher.diboson;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Messenger;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class Television extends DibosonActivity
{
	// ============================================================================
	// 16/12/2013 ECU created
	//            ECU activity for communicating with the Arduino
	// 20/12/2013 ECU implemented the OnGestureListener
	// 11/05/2015 ECU changed the logic because some of the methods need to be 
	//                accessed by the TelevisionSwipeActivity as well as this one
	//                this involved making them public and static which caused a 
	//                few issues
	// 11/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// ----------------------------------------------------------------------------
	// Testing
	// =======
	//=============================================================================
	//private final static String TAG = "Television";
	/* ============================================================================ */
	private static final int ARDUINO_USB_VENDOR_ID 						= 0x2341;
    private static final int ARDUINO_UNO_USB_PRODUCT_ID 				= 0x01;
    private static final int ARDUINO_MEGA_2560_USB_PRODUCT_ID 			= 0x10;
    private static final int ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID 		= 0x42;
    private static final int ARDUINO_UNO_R3_USB_PRODUCT_ID 				= 0x43;
    private static final int ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID 	= 0x44;
    private static final int ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID 		= 0x3F;
    // ============================================================================
    // 06/07/2020 ECU declare the components that are used for entries stored
    //                in the 'raw' and 'text' files when a channel number needs
    //                to be changed
    // ----------------------------------------------------------------------------
    public static final int	CHANNEL_NAME	=	0;
	public static final int	CHANNEL_NUMBER	=	1;
    /* ============================================================================ */
    // 17/12/2013 ECU declare device types
    // ----------------------------------------------------------------------------
    public static final int IR_TYPE_SAMSUNG  		=  	1;
    public static final int IR_TYPE_HITACHI			=   2;
    public static final int IR_TYPE_SONY            = 	3;
    /* ============================================================================ */
    // 19/12/2013 ECU declare the device types
    // ----------------------------------------------------------------------------
    public static final int IR_DEVICE_TELEVISION	=	0;
    public static final int IR_DEVICE_MEDIA_PLAYER	=	1;
    public static final int IR_DEVICE_DVD_PLAYER	=	2;
    /* ============================================================================ */
    // 17/12/2013 ECU the following are the IR functions - these are filed
    //                alphabetically but be careful with new inserts because
    //                the number will need to be changed
    // 19/12/2013 ECU added more codes for Sony Media Player
    // 20/12/2013 ECU changed to use IRFunction so that meaning can be put to keys
    // ----------------------------------------------------------------------------
    public static final int 	   IR_NO_CODE  				=   -1;
    // ----------------------------------------------------------------------------
    public static final IRFunction IR_BLANK					= new IRFunction (IR_NO_CODE,"",R.drawable.button_blank);   
    // ----------------------------------------------------------------------------
    public static final IRFunction IR_ARROW_CENTRE			= new IRFunction (0,"centre",R.drawable.button_enter);      
    public static final IRFunction IR_ARROW_DOWN   			= new IRFunction (1,"down arrow",R.drawable.button_arrow_down);
    public static final IRFunction IR_ARROW_LEFT        	= new IRFunction (2,"left arrow",R.drawable.button_arrow_left);
    public static final IRFunction IR_ARROW_RIGHT       	= new IRFunction (3,"right arrow",R.drawable.button_arrow_right);
    public static final IRFunction IR_ARROW_UP          	= new IRFunction (4,"up arrow",R.drawable.button_arrow_up);
    public static final IRFunction IR_AUDIO_DESCRIPTION 	= new IRFunction (5,"audio description",R.drawable.button_blank);
    public static final IRFunction IR_CHANNEL_LIST      	= new IRFunction (6,"channel list",R.drawable.button_blank);
    public static final IRFunction IR_COLOUR_BLUE       	= new IRFunction (7,"blue",R.drawable.button_blue);
    public static final IRFunction IR_COLOUR_GREEN      	= new IRFunction (8,"green",R.drawable.button_green);
    public static final IRFunction IR_COLOUR_RED        	= new IRFunction (9,"red",R.drawable.button_red);
    public static final IRFunction IR_COLOUR_YELLOW     	= new IRFunction (10,"yellow",R.drawable.button_yellow);
    public static final IRFunction IR_DISPLAY				= new IRFunction (11,"display",R.drawable.button_blank);
    public static final IRFunction IR_DVD_START 			= new IRFunction (12,"beginning of DVD",R.drawable.button_blank);
    public static final IRFunction IR_DVD_STOP		 		= new IRFunction (13,"end of DVD",R.drawable.button_blank);
 	public static final IRFunction IR_END		 			= new IRFunction (14,"end",R.drawable.button_blank);
    public static final IRFunction IR_EXIT              	= new IRFunction (15,"exit",R.drawable.button_exit);
    public static final IRFunction IR_FAST_BACKWARD			= new IRFunction (16,"fast backward",R.drawable.button_blank);
    public static final IRFunction IR_FAST_FORWARD			= new IRFunction (17,"fast forward",R.drawable.button_blank);
    public static final IRFunction IR_FAVOURITES 			= new IRFunction (18,"favourites",R.drawable.button_blank);
    public static final IRFunction IR_GUIDE 				= new IRFunction (19,"guide",R.drawable.button_guide);
    public static final IRFunction IR_HOME					= new IRFunction (20,"home",R.drawable.button_blank);
    public static final IRFunction IR_INFO 					= new IRFunction (21,"information",R.drawable.button_info);
    public static final IRFunction IR_MENU 					= new IRFunction (22,"menu",R.drawable.button_blank);
    public static final IRFunction IR_MUTE 					= new IRFunction (23,"mute",R.drawable.button_mute);
    public static final IRFunction IR_NUMBER_1 				= new IRFunction (24,"1",R.drawable.button_1);
    public static final IRFunction IR_NUMBER_2 				= new IRFunction (25,"2",R.drawable.button_2);
    public static final IRFunction IR_NUMBER_3 				= new IRFunction (26,"3",R.drawable.button_3);
    public static final IRFunction IR_NUMBER_4 				= new IRFunction (27,"4",R.drawable.button_4);
    public static final IRFunction IR_NUMBER_5 				= new IRFunction (28,"5",R.drawable.button_5);
    public static final IRFunction IR_NUMBER_6 				= new IRFunction (29,"6",R.drawable.button_6);
    public static final IRFunction IR_NUMBER_7 				= new IRFunction (30,"7",R.drawable.button_7);
    public static final IRFunction IR_NUMBER_8 				= new IRFunction (31,"8",R.drawable.button_8);
    public static final IRFunction IR_NUMBER_9 				= new IRFunction (32,"9",R.drawable.button_9);
    public static final IRFunction IR_NUMBER_0 				= new IRFunction (33,"0",R.drawable.button_0);
    public static final IRFunction IR_OPTIONS 				= new IRFunction (34,"options",R.drawable.button_blank);
    public static final IRFunction IR_PLAY					= new IRFunction (35,"play",R.drawable.button_blank);
	public static final IRFunction IR_PAUSE					= new IRFunction (36,"pause",R.drawable.button_blank);
    public static final IRFunction IR_POWER 				= new IRFunction (37,"power",R.drawable.button_power);
    public static final IRFunction IR_PREVIOUS_CHANNEL		= new IRFunction (38,"previous channel",R.drawable.button_prev_chann);
    public static final IRFunction IR_PROGRAM_DOWN 			= new IRFunction (39,"previous program",R.drawable.button_program_down);
    public static final IRFunction IR_PROGRAM_UP			= new IRFunction (40,"next program",R.drawable.button_program_up);
    public static final IRFunction IR_RETURN_KEY			= new IRFunction (41,"return",R.drawable.button_return);
    public static final IRFunction IR_SEN					= new IRFunction (42,"sony entertainment network",R.drawable.button_blank);
    public static final IRFunction IR_SOURCE 				= new IRFunction (43,"source",R.drawable.button_blank);
	public static final IRFunction IR_STOP					= new IRFunction (44,"stop",R.drawable.button_blank);
    public static final IRFunction IR_SUBTITLE 				= new IRFunction (45,"subtitle",R.drawable.button_blank);
    public static final IRFunction IR_TOOLS 				= new IRFunction (46,"tools",R.drawable.button_blank);	
    public static final IRFunction IR_TV 					= new IRFunction (47,"TV",R.drawable.button_blank);
    public static final IRFunction IR_VOLUME_DOWN			= new IRFunction (48,"volume down",R.drawable.button_volume_down);
    public static final IRFunction IR_VOLUME_UP 			= new IRFunction (49,"volume up",R.drawable.button_volume_up);
    /* ============================================================================ */
    // 02/03/2014 ECU declare array of digits that are used when changing channels
    // ----------------------------------------------------------------------------
    public static final int []	   CHANNEL_DIGITS			= {
    															IR_NUMBER_0.function,	// 0
    															IR_NUMBER_1.function,	// 1
    															IR_NUMBER_2.function,	// 2
    															IR_NUMBER_3.function,	// 3
    															IR_NUMBER_4.function,	// 4
    															IR_NUMBER_5.function,	// 5
    															IR_NUMBER_6.function,	// 6
    															IR_NUMBER_7.function,	// 7
    															IR_NUMBER_8.function,	// 8
    															IR_NUMBER_9.function	// 9		
                                                              };
    /* ============================================================================ */
    public static final String     MESSAGE_TERMINATOR		= StaticData.NEWLINE;
    // ============================================================================
    // 11/05/2015 ECU declare the remote controllers supported
    // 12/05/2015 ECU added the total
    // ----------------------------------------------------------------------------
    private static final int	   REMOTE_TOTAL				= 	3;
    // ----------------------------------------------------------------------------
    public static final int        REMOTE_SAMSUNG_TV		=	0;
    public static final int        REMOTE_HITACHI_TV		=	1;
    public static final int        REMOTE_SONY_MP			= 	2;
    /* ============================================================================= */
	// 01/03/2014 ECU array of television channel details
	// 12/05/2015 ECU add extra channels
    // 07/03/2017 ECU added 'drama' channel
    // 18/11/2017 ECU changed for an array to a list
    //            ECU the initialisation of the array is performed in 'initialiseTVChannels'
	// -----------------------------------------------------------------------------
	public static List<TelevisionChannel> televisionChannels; 
    /* ============================================================================ */
    private static final boolean   USE_BLUETOOTH			= true;
    private static final boolean   USE_USB                  = false;
    /* ============================================================================ */
    static BluetoothHandler blueToothHandler;					// 01/03/2014 ECU added
    static InfraredCodes		codesHitachi;
    static InfraredCodes 		codesSamsung;
    static InfraredCodes 		codesSonyMediaPlayer;
    static boolean		  		hardwareInterface = USE_BLUETOOTH;	// 31/12/2013 ECU added
    static ImageAdapter [] 		imageAdapter = new ImageAdapter [REMOTE_TOTAL];	
    static int					keySize;							// 16/02/2014 ECU added
    static Messenger 			messenger = null;					// 15/02/2014 ECU added initialisation
	static Messenger			messengerService;
	static View					rootView;							// 11/05/2015 ECU added
    static GridView	 	 		televisionGridView;
    static TextView	  			televisionStatusView;				// 21/12/2013 ECU added
   /* ============================================================================ */
    
    // ============================================================================
    // I M P O R T A N T
    // =================
    // 17/12/2013 ECU when setting the codes remember to put in the terminating 'l'
    //                to indicate a long number otherwise an int number will be used
    // ============================================================================
    
    // ============================================================================ 
    // Hitachi Devices
    // ----------------------------------------------------------------------------
    // 17/12/2013 ECU set up the codes for the Hitachi television
    // ----------------------------------------------------------------------------
    static IRCode [] codeHitachi = {
								new IRCode (IR_ARROW_CENTRE, 		0x75l),
								new IRCode (IR_ARROW_DOWN, 			0x53l),
								new IRCode (IR_ARROW_LEFT, 			0x55l),
								new IRCode (IR_ARROW_RIGHT, 		0x56l),
								new IRCode (IR_ARROW_UP, 			0x54l),
								new IRCode (IR_COLOUR_BLUE,			0x74l),
								new IRCode (IR_COLOUR_GREEN,		0x76l),
								new IRCode (IR_COLOUR_RED,			0x77l),
								new IRCode (IR_COLOUR_YELLOW,		0x72l),
								new IRCode (IR_FAVOURITES,			0x68l),
								new IRCode (IR_GUIDE,				0x6Fl),
								new IRCode (IR_INFO,				0x52l),
								new IRCode (IR_MENU,				0x70l),
								new IRCode (IR_MUTE,				0x4Dl),
								new IRCode (IR_NUMBER_1,			0x41l),
								new IRCode (IR_NUMBER_2,			0x42l),
								new IRCode (IR_NUMBER_3,			0x43l),   
								new IRCode (IR_NUMBER_4,			0x44l),
								new IRCode (IR_NUMBER_5,			0x45l),
								new IRCode (IR_NUMBER_6,			0x46l),
								new IRCode (IR_NUMBER_7,			0x47l),
								new IRCode (IR_NUMBER_8,			0x48l),
								new IRCode (IR_NUMBER_9,			0x49l),
								new IRCode (IR_NUMBER_0,			0x40l),
								new IRCode (IR_POWER,				0x4Cl),
								new IRCode (IR_PREVIOUS_CHANNEL,	0x62l),
								new IRCode (IR_PROGRAM_DOWN,		0x61l),
								new IRCode (IR_PROGRAM_UP,			0x60l),
								new IRCode (IR_RETURN_KEY,			0x4Al),
								new IRCode (IR_SOURCE,				0x78l),
								new IRCode (IR_SUBTITLE,			0x6Fl),
								new IRCode (IR_TV,					0x7Fl),
								new IRCode (IR_VOLUME_DOWN,			0x51l),
								new IRCode (IR_VOLUME_UP,			0x50l)
							};
    // ----------------------------------------------------------------------------
    // 20/12/2013 ECU declare the screen layout
    // ----------------------------------------------------------------------------
    static IRFunction []	HitachiLayout = {
    									IR_POWER,
    									IR_NUMBER_1,
    									IR_NUMBER_2,
    									IR_NUMBER_3,
    									IR_GUIDE,
    									IR_NUMBER_4,
    									IR_NUMBER_5,
    									IR_NUMBER_6,
    									IR_INFO,
    									IR_NUMBER_7,
    									IR_NUMBER_8,
    									IR_NUMBER_9,
    									IR_PREVIOUS_CHANNEL,
    									IR_RETURN_KEY,	
    									IR_NUMBER_0,
    									IR_ARROW_CENTRE,
    									IR_ARROW_LEFT,
    									IR_ARROW_UP,
    									IR_ARROW_DOWN,
    									IR_ARROW_RIGHT,
    									IR_COLOUR_RED,
    									IR_COLOUR_GREEN,
    									IR_COLOUR_YELLOW,
    									IR_COLOUR_BLUE,
    									IR_VOLUME_UP,				// 31/12/2013 ECU added
    									IR_VOLUME_DOWN,				// 31/12/2013 ECU added
    									IR_PROGRAM_UP,				// 31/12/2013 ECU added
    									IR_PROGRAM_DOWN,			// 31/12/2013 ECU added
    									IR_MUTE						// 01/01/2014 ECU added
    							};
    // ============================================================================ 
    // Samsung Devices
    // ----------------------------------------------------------------------------
    // 17/12/2013 ECU set up the codes for the Samsung television
    // ----------------------------------------------------------------------------
    static IRCode [] codeSamsung = {
								new IRCode (IR_ARROW_CENTRE, 		0xE0E016E9l),
								new IRCode (IR_ARROW_DOWN, 			0xE0E08679l),
								new IRCode (IR_ARROW_LEFT, 			0xE0E0A659l),
								new IRCode (IR_ARROW_RIGHT, 		0xE0E046B9l),
								new IRCode (IR_ARROW_UP, 			0xE0E006F9l),
								new IRCode (IR_AUDIO_DESCRIPTION, 	0xE0E0E41Bl),
								new IRCode (IR_CHANNEL_LIST,		0xE0E0D629l),
								new IRCode (IR_COLOUR_BLUE,			0xE0E06897l),
								new IRCode (IR_COLOUR_GREEN,		0xE0E028D7l),
								new IRCode (IR_COLOUR_RED,			0xE0E036C9l),
								new IRCode (IR_COLOUR_YELLOW,		0xE0E0A857l),
								new IRCode (IR_EXIT,				0xE0E0B44Bl),
								new IRCode (IR_FAVOURITES,			0xE0E022DDl),
								new IRCode (IR_GUIDE,				0xE0E0F20Dl),
								new IRCode (IR_INFO,				0xE0E0F807l),
								new IRCode (IR_MENU,				0xE0E058A7l),
								new IRCode (IR_MUTE,				0xE0E0F00Fl),
								new IRCode (IR_NUMBER_1,			0xE0E020DFl),
								new IRCode (IR_NUMBER_2,			0xE0E0A05Fl),
								new IRCode (IR_NUMBER_3,			0xE0E0609Fl),   
								new IRCode (IR_NUMBER_4,			0xE0E010EFl),
								new IRCode (IR_NUMBER_5,			0xE0E0906Fl),
								new IRCode (IR_NUMBER_6,			0xE0E050AFl),
								new IRCode (IR_NUMBER_7,			0xE0E030CFl),
								new IRCode (IR_NUMBER_8,			0xE0E0B04Fl),
								new IRCode (IR_NUMBER_9,			0xE0E0708Fl),
								new IRCode (IR_NUMBER_0,			0xE0E08877l),
								new IRCode (IR_POWER,				0xE0E040BFl),
								new IRCode (IR_PREVIOUS_CHANNEL,	0xE0E0C837l),
								new IRCode (IR_PROGRAM_DOWN,		0xE0E008F7l),
								new IRCode (IR_PROGRAM_UP,			0xE0E048B7l),
								new IRCode (IR_RETURN_KEY,			0xE0E01AE5l),
								new IRCode (IR_SOURCE,				0xE0E0807Fl),
								new IRCode (IR_SUBTITLE,			0xE0E0A45Bl),
								new IRCode (IR_TOOLS,				0xE0E0D22Dl),
								new IRCode (IR_TV,					0xE0E0D827l),
								new IRCode (IR_VOLUME_DOWN,			0xE0E0D02Fl),
								new IRCode (IR_VOLUME_UP,			0xE0E0E01Fl)
							}; 
    // ----------------------------------------------------------------------------
    // 20/12/2013 ECU declare the screen layout
    // ----------------------------------------------------------------------------
    static IRFunction []	SamsungLayout = {
    									IR_POWER,
    									IR_NUMBER_1,
    									IR_NUMBER_2,
    									IR_NUMBER_3,
    									IR_GUIDE,
    									IR_NUMBER_4,
    									IR_NUMBER_5,
    									IR_NUMBER_6,
    									IR_INFO,
    									IR_NUMBER_7,
    									IR_NUMBER_8,
    									IR_NUMBER_9,
    									IR_PREVIOUS_CHANNEL,
    									IR_EXIT,	
    									IR_NUMBER_0,
    									IR_ARROW_CENTRE,
    									IR_ARROW_LEFT,
    									IR_ARROW_UP,
    									IR_ARROW_DOWN,
    									IR_ARROW_RIGHT,
    									IR_COLOUR_RED,
    									IR_COLOUR_GREEN,
    									IR_COLOUR_YELLOW,
    									IR_COLOUR_BLUE,
    									IR_VOLUME_UP,				// 31/12/2013 ECU added
    									IR_VOLUME_DOWN,				// 31/12/2013 ECU added
    									IR_PROGRAM_UP,				// 31/12/2013 ECU added
    									IR_PROGRAM_DOWN,			// 31/12/2013 ECU added
    									IR_MUTE						// 01/01/2014 ECU added
    							};
    // ============================================================================ 
    // Sony Devices
    // ----------------------------------------------------------------------------
    // 17/12/2013 ECU set up the codes for the Sony Media Player
    // ----------------------------------------------------------------------------
    static IRCode [] codeSonyMP = {
								new IRCode (IR_ARROW_CENTRE, 		0xD0BF6),
								new IRCode (IR_ARROW_DOWN, 			0x5EBF6),
								new IRCode (IR_ARROW_LEFT, 			0xDEBF6),
								new IRCode (IR_ARROW_RIGHT, 		0x3EBF6),
								new IRCode (IR_ARROW_UP, 			0x9EBF6),
								new IRCode (IR_COLOUR_BLUE,			0x66BEE),
								new IRCode (IR_COLOUR_GREEN,		0x16BEE),
								new IRCode (IR_COLOUR_RED,			0xE6BEE),
								new IRCode (IR_COLOUR_YELLOW,		0x96BEE),
								new IRCode (IR_DISPLAY,				0x2ABF6),
								new IRCode (IR_FAST_BACKWARD,		0x44BF6),
								new IRCode (IR_FAST_FORWARD,		0xC4BF6),
								new IRCode (IR_HOME,				0xCABF6),
								new IRCode (IR_OPTIONS,				0xE8BEE),
								new IRCode (IR_PAUSE,				0x9CBF6),
								new IRCode (IR_PLAY,				0x70BF6),
								new IRCode (IR_POWER,				0xA8BF6),
								new IRCode (IR_RETURN_KEY,			0x70BF6),
								new IRCode (IR_DVD_START,			0xCBF6),
								new IRCode (IR_DVD_STOP,			0x8CBF6),
								new IRCode (IR_SEN,					0x32BEE),
								new IRCode (IR_STOP,				0x1CBF6)				
							};
    // ----------------------------------------------------------------------------
    // 20/12/2013 ECU declare the screen layout
    // ----------------------------------------------------------------------------
   	static IRFunction []	SonyMPLayout = {
    									IR_POWER,
    									IR_COLOUR_RED,
    									IR_COLOUR_GREEN,
    									IR_COLOUR_YELLOW,
    									IR_COLOUR_BLUE,
    									IR_ARROW_CENTRE,
    									IR_ARROW_LEFT,
    									IR_ARROW_UP,
    									IR_ARROW_DOWN,
    									IR_ARROW_RIGHT
    							};
    // =============================================================================
    // 10/05/2015 ECU declare the codes for each of the remote controllers that
    //                this app will control
    // -----------------------------------------------------------------------------
	public static RemoteController []	remoteControllers =	{ 
			// ---------------------------------------------------------------------
			new RemoteController (new InfraredCodes (IR_TYPE_SAMSUNG,
								                     codeSamsung,
								                     "Samsung Television Remote Controller"),
								  SamsungLayout),
			// ---------------------------------------------------------------------
			new RemoteController (new InfraredCodes (IR_TYPE_HITACHI,
													 codeHitachi,
													 "Hitachi Television Remote Controller"),
								  HitachiLayout),
		    // ---------------------------------------------------------------------
			new RemoteController (new InfraredCodes (IR_TYPE_SONY,
											         codeSonyMP,
											         "Sony Media Player Controller"),
						          SonyMPLayout)
			// ---------------------------------------------------------------------
														    };
	// =============================================================================
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {   	
        super.onCreate(savedInstanceState);
        // -------------------------------------------------------------------------
        if (savedInstanceState == null)
        {
        	// ---------------------------------------------------------------------
        	// 11/10/2015 ECU the activity has been created anew
        	// ---------------------------------------------------------------------
           	// 20/12/2013 ECU set portrait mode with no title
        	// 28/02/2014 ECU changed to use the standard method
        	// 08/04/2014 ECU changed to use the variable
        	// ---------------------------------------------------------------------
        	Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);	
 
        	setContentView (R.layout.television);
        	// ---------------------------------------------------------------------
        	// 11/05/2015 ECU save the root view
        	// ---------------------------------------------------------------------
        	rootView = findViewById (android.R.id.content).getRootView();
        	// ---------------------------------------------------------------------
        	// 31/12/2013 ECU initialise the hardware interface
        	// 15/02/2014 ECU only continue if the hardware seems OK
        	// ---------------------------------------------------------------------
        	if (InitialiseHardwareInterface (this))
        	{
        		// -----------------------------------------------------------------
        		// 11/05/2015 ECU initialise the display for each of the controllers
        		// 12/05/2015 ECU added the 'true' argument to indicate that the text title
        		//                is required
        		// -----------------------------------------------------------------
        		for (int theController=0; theController < remoteControllers.length; theController++)
        			initialiseTheDisplay (this,rootView,theController,true);
        		// -----------------------------------------------------------------
        		// 11/05/2015 ECU set the currently displayed controller
        		//            ECU default to the Samsung TV
        		// -----------------------------------------------------------------
        		PublicData.currentRemoteController = REMOTE_SAMSUNG_TV;
        		// -----------------------------------------------------------------
        		televisionGridView.setAdapter (imageAdapter[PublicData.currentRemoteController]);
        		// -----------------------------------------------------------------
        	}
        	else
        	{
        		// -----------------------------------------------------------------
        		// 15/02/2014 ECU the hardware did not initialise so terminate this 
        		//                activity
        		// -----------------------------------------------------------------
        		finish ();
        	}
        }
        else
        {
        	// ---------------------------------------------------------------------
        	// 11/10/2015 ECU the activity has been recreated after having been
        	//                destroyed by the Android OS
        	// ---------------------------------------------------------------------
        	finish (); 
        	// ---------------------------------------------------------------------
        }
    }
    /* ============================================================================ */
    @Override
    protected void onNewIntent(Intent intent) 
    {
        super.onNewIntent(intent);

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.contains(intent.getAction())) 
        {
        	// ---------------------------------------------------------------------
            // 16/12/2013 ECU try and find the newly attached device
        	// ---------------------------------------------------------------------
            FindArduinoDevice(this);
        }
    }
    /* ============================================================================ */
    @Override
    protected void onDestroy() 
    {
        super.onDestroy();
        
        if (hardwareInterface == USE_USB)
        {
        	// ---------------------------------------------------------------------
        	// 16/12/2013 ECU unregister the receiver of data
        	// 21/12/2013 ECU put in a catch of 'IllegalArgumentException' which could
        	//                happen if the receiver is not registered. This would happen
        	//                if the activity is at the wrong API level
        	// ---------------------------------------------------------------------
        	try
        	{
        		unregisterReceiver (serialDataReceiver);
        	}
        	catch(IllegalArgumentException theException)
        	{ 
        		// -----------------------------------------------------------------
        		// 21/12/2013 ECU the receiver is not registered
        		// -----------------------------------------------------------------
        	}
        }
        else
        if (hardwareInterface == USE_BLUETOOTH)
        {
        	// ---------------------------------------------------------------------
        	// 31/12/2013 ECU close down issues associated with the bluetooth interface
        	// 01/01/2014 ECU took out a lot of code because using a bluetooth service
        	// 01/03/2014 ECU remember to unbind the bluetooth connection
        	// 15/01/2019 ECU had a timing issue with a NPE so add the check
        	// ---------------------------------------------------------------------
        	if (blueToothHandler != null)
        	{
        		blueToothHandler.UnBind();  
        	}
        	// ---------------------------------------------------------------------
        }
    }
    /* ============================================================================== */
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) 
	{
			menu.clear ();
			// ---------------------------------------------------------------------
			// 05/06/2013 ECU used the method to build menu
			// 11/05/2015 ECU changed the logic to use the remote controller objects
			// ---------------------------------------------------------------------
			for (int theIndex=0; theIndex < remoteControllers [PublicData.currentRemoteController].codes.codes.length; theIndex++)
				{
					menu.add(0,theIndex,0,remoteControllers [PublicData.currentRemoteController].codes.codes [theIndex].function.meaning);
				}
			// ---------------------------------------------------------------------
				
		    return true;
	}
	// =============================================================================
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.grid,menu);
			
		return true;
	}
    /* ============================================================================= */
    @Override
	public boolean onOptionsItemSelected (MenuItem item)
	{
    	// -------------------------------------------------------------------------
		// 16/06/2013 ECU take the actions depending on which menu is selected
    	// 11/05/2015 ECU added the context
    	// -------------------------------------------------------------------------
    	SendDataToRemote (getBaseContext(),remoteControllers [PublicData.currentRemoteController].codes.type + "," + 
    			remoteControllers [PublicData.currentRemoteController].codes.ReturnTheCode(remoteControllers [PublicData.currentRemoteController].codes.codes[item.getItemId()].function.function));
		
		return true;
	}
    /* ============================================================================== */
    public static ArrayList<String> BuildMeaningsList (int theRemoteController)
    {
    	// -------------------------------------------------------------------------
    	// 20/12/2013 ECU this method will build up a list of the meanings associated with
    	//                each IR function - this will be called by the voice recognition 
    	//                software
    	// 01/03/2014 ECU added 'theDevice' as an argument to make sure that the codes
    	//                object is initialised
    	// 11/05/2015 ECU changed to use theRemoteController rather than devices
    	// -------------------------------------------------------------------------
    	
    	ArrayList<String> meaningsList = null;
    	
    	meaningsList = new ArrayList<String>();
    	// -------------------------------------------------------------------------
    	// 11/05/2015 ECU changed the logic to use the remote controller objects
    	// -------------------------------------------------------------------------
		for (int theIndex=0; theIndex < remoteControllers [theRemoteController].codes.codes.length; theIndex++)
		{
			meaningsList.add (remoteControllers [theRemoteController].codes.codes[theIndex].function.meaning); 
		}
		// -------------------------------------------------------------------------
    	return meaningsList;
    }
	/* ============================================================================= */
    public static boolean InitialiseHardwareInterface (Context theContext)
    {
    	// -------------------------------------------------------------------------
    	// 31/12/2013 ECU created to do the initial bits of the interface to the 
    	//                infrared blaster
    	// 15/02/2014 ECU change to return a state as to whether the hardware has
    	//                been set up correctly
    	// -------------------------------------------------------------------------
        if (hardwareInterface == USE_USB)
        {
        	// ---------------------------------------------------------------------
        	// 31/12/2013 ECU using the USB manager
        	// ---------------------------------------------------------------------
        	// 16/12/2013 ECU register the handler for receive data
        	// ---------------------------------------------------------------------
            IntentFilter filter = new IntentFilter ();
            filter.addAction (ArduinoCommunicatorService.DATA_RECEIVED_INTENT);
            filter.addAction (ArduinoCommunicatorService.DATA_SENT_INTERNAL_INTENT);
            theContext.registerReceiver (serialDataReceiver, filter);
            // ---------------------------------------------------------------------
            // 16/12/2013 ECU try and find an Arduino card
            // 15/02/2014 ECU return with the state of the Arduino hardware
            // ---------------------------------------------------------------------
            return FindArduinoDevice (theContext);
            // ---------------------------------------------------------------------
        }
        else
        if (hardwareInterface == USE_BLUETOOTH)
        {
        	// ---------------------------------------------------------------------
        	// 31/12/2013 ECU using the bluetooth module
        	// 01/01/2014 ECU link to the bluetooth service
        	// 01/03/2014 ECU change to use the new class
        	// ---------------------------------------------------------------------
        	blueToothHandler = new BluetoothHandler(theContext);
        	// ---------------------------------------------------------------------
        	// 01/03/2014 ECU return with the state of the handler
        	// ---------------------------------------------------------------------
        	return blueToothHandler.CheckMessenger ();	
        	// ---------------------------------------------------------------------
        }
        else
        {
        	// ---------------------------------------------------------------------
        	// 15/02/2014 ECU if nothing else then indicate a failure
        	// ---------------------------------------------------------------------
        	return false;
        	// ---------------------------------------------------------------------
        }
    }

 	// =============================================================================
 	public static void initialiseTheDisplay (final Context theContext,
 											 View theView,
 											 final int theRemoteController,
 											 boolean theTextFlag)
 	{
 		// -------------------------------------------------------------------------
 		// 11/05/2015 ECU created to initialise the display and various handlers
 		// 12/05/2015 ECU theTextFlag added to indicate if the text legend is to be
 		//                displayed
		// -------------------------------------------------------------------------
 		// 15/02/2014 ECU try and scale images
		// 16/02/2014 ECU want to force the size of the key because was getting
		//                some gaps on smaller devices.
		//                The display should be 4 keys across to fill whole screen
		// -------------------------------------------------------------------------
		keySize = PublicData.screenWidth / 4;
		// -------------------------------------------------------------------------
		// 20/12/2013 ECU set up aspects of the grid
		// -------------------------------------------------------------------------
		televisionGridView = (GridView) theView.findViewById (R.id.televisionGridView);
		// -------------------------------------------------------------------------
		// 21/12/2013 ECU get the status text view
		// -------------------------------------------------------------------------
		televisionStatusView = (TextView) theView.findViewById (R.id.televisionStatusView);
		// -------------------------------------------------------------------------
		// 20/12/2013 ECU set up the image adapter for this grid
		// 12/05/2015 ECU added the Textflag
		// -------------------------------------------------------------------------
		imageAdapter [theRemoteController]= new ImageAdapter(theContext,theRemoteController,theTextFlag);
		televisionGridView.setAdapter (imageAdapter[theRemoteController]);
 		/* ------------------------------------------------------------------------- */
		televisionGridView.setOnItemClickListener (new OnItemClickListener() 
		{
			@Override
			public void onItemClick (AdapterView<?> parent,View view,int position, long id) 
			{
				// -----------------------------------------------------------------
				// 20/11/2013 ECU get the code to be actioned
				// 11/05/2015 ECU changed to use remote controller objects
				// -----------------------------------------------------------------
				long codeToAction = remoteControllers [PublicData.currentRemoteController].codes.ReturnTheCode(remoteControllers [PublicData.currentRemoteController].layout[position].function);
				// -----------------------------------------------------------------
				// 20/12/2013 ECU only transmit if it is a valid code
				// -----------------------------------------------------------------
				if (codeToAction != IR_NO_CODE)
				{
					// -------------------------------------------------------------
					// 20/12/2013 ECU code is valid so send it
					// -------------------------------------------------------------
					SendDataToRemote (theContext,remoteControllers[PublicData.currentRemoteController].codes.type + "," + codeToAction);
				}         
			}
		});
		/* ------------------------------------------------------------------------ */
		// 13/05/2015 ECU only have the long click if not the swipe version
		// ------------------------------------------------------------------------
		if (theTextFlag)
		{
			televisionGridView.setOnItemLongClickListener (new OnItemLongClickListener() 
			{
				@Override
				public boolean onItemLongClick (AdapterView<?> parent,View view,int position, long id) 
				{
					// -------------------------------------------------------------
					// 11/05/2015 ECU step to the next available controller or reset to 0
					//                if hit the last one
					// -------------------------------------------------------------
					PublicData.currentRemoteController++;
					// -------------------------------------------------------------
					if (PublicData.currentRemoteController >= remoteControllers.length)
						PublicData.currentRemoteController = 0;
					// -------------------------------------------------------------
					// 20/12/2013 ECU force the adapter to redraw all views, etc....
					// 11/05/2015 ECU not sure that both are needed but it is working
					// -------------------------------------------------------------
					televisionGridView.setAdapter (imageAdapter [PublicData.currentRemoteController]);
					imageAdapter [PublicData.currentRemoteController].notifyDataSetChanged();
					// -------------------------------------------------------------       	
					return true;
					// -------------------------------------------------------------
				}
			});
		}
 		// -------------------------------------------------------------------------
 	}
    /* ============================================================================ */
    static BroadcastReceiver serialDataReceiver = new BroadcastReceiver() 
    {
    	// ------------------------------------------------------------------------
    	// 16/12/2013 ECU declare the receiver of incoming serial data
    	// ------------------------------------------------------------------------
        private void handleTransferedData(Intent intent, boolean receiving) 
        {
        	// --------------------------------------------------------------------
            // 16/12/2013 ECU get the data from the service
        	// --------------------------------------------------------------------
            final byte[] newTransferedData = intent.getByteArrayExtra (ArduinoCommunicatorService.DATA_EXTRA);
                
            if (receiving)
            {
            	String receivedString = new String (newTransferedData);
            	Utilities.popToast(receivedString);
            }
        }
        /* ============================================================================ */
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            final String action = intent.getAction();

            if (ArduinoCommunicatorService.DATA_RECEIVED_INTENT.equals(action)) 
            {
                handleTransferedData(intent, true);
            } 
            else
            if (ArduinoCommunicatorService.DATA_SENT_INTERNAL_INTENT.equals(action))
            {
                handleTransferedData(intent, false);
            }
        }
    };
    /* ============================================================================ */
    private static boolean FindArduinoDevice (Context theContext) 
    {
    	// ------------------------------------------------------------------------
    	// 16/12/2013 ECU try and check the type of Arduino device attached
    	// 15/12/2013 ECU changed to reflect whether device found by returning
    	//                true (device found) or false (no device found)
    	// ------------------------------------------------------------------------
        UsbManager usbManager = (UsbManager) theContext.getSystemService (Context.USB_SERVICE);
        UsbDevice usbDevice = null;
        HashMap<String, UsbDevice> usbDeviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = usbDeviceList.values().iterator();
           
        if (deviceIterator.hasNext()) 
        {
        	//
            UsbDevice tempUsbDevice = deviceIterator.next();

            if (tempUsbDevice.getVendorId() == ARDUINO_USB_VENDOR_ID) 
            {
            	// -----------------------------------------------------------------
                // 16/12/2013 ECU now find the actual Arduino card detected
            	// -----------------------------------------------------------------
                switch (tempUsbDevice.getProductId()) 
                {
                case ARDUINO_UNO_USB_PRODUCT_ID:
                    Utilities.popToast ("Arduino Uno " + theContext.getString(R.string.found));
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_USB_PRODUCT_ID:
                	Utilities.popToast ("Arduino Mega 2560 " + theContext.getString(R.string.found));
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID:
                	Utilities.popToast ("Arduino Mega 2560 R3 " + theContext.getString(R.string.found));
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_UNO_R3_USB_PRODUCT_ID:
                	Utilities.popToast ("Arduino Uno R3 " + theContext.getString(R.string.found));
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID:
                	Utilities.popToast ("Arduino Mega 2560 ADK R3 " + theContext.getString(R.string.found));
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID:
                	Utilities.popToast ("Arduino Mega 2560 ADK " + theContext.getString(R.string.found));
                    usbDevice = tempUsbDevice;
                    break;
                }
            }
        }

        // 16/12/2013 ECU check if a device has been found
        
        if (usbDevice == null) 
        {
        	// --------------------------------------------------------------------
        	// 16/12/2013 ECU no Arduino device has been found
        	// --------------------------------------------------------------------
            Utilities.popToast (theContext.getString(R.string.no_device_found));
            // --------------------------------------------------------------------
            // 15/02/2014 ECU indicate no device found
            // --------------------------------------------------------------------
            return false;
        } 
        else 
        {
        	// --------------------------------------------------------------------
        	// 16/12/2013 ECU device found so can use the service
        	// --------------------------------------------------------------------
            Intent startIntent = new Intent(theContext.getApplicationContext(), ArduinoCommunicatorService.class);
            PendingIntent pendingIntent = PendingIntent.getService(theContext.getApplicationContext(), 0, startIntent, 0);
         	// --------------------------------------------------------------------
            // 16/12/2013 ECU request permission to use the found device
            // --------------------------------------------------------------------
            usbManager.requestPermission(usbDevice, pendingIntent);
            // --------------------------------------------------------------------
            // 15/02/2014 ECU indicate that device has been found
            // --------------------------------------------------------------------
            return true;
        }
    }
    /* ============================================================================= */
    public static class ImageAdapter extends BaseAdapter 
	{
    	// -------------------------------------------------------------------------
    	// 11/05/2015 ECU changed to 'static'
    	// 12/05/2015 ECU added the text flag
    	// -------------------------------------------------------------------------
	    private Context mContext;
	    private	int		mRemoteController;
	    private boolean	mTextFlag;
		// =========================================================================
	    public ImageAdapter(Context theContext,int theRemoteController,boolean theTextFlag)
	    {
	        mContext 			= theContext;
	        mRemoteController 	= theRemoteController;
	        mTextFlag			= theTextFlag;
	    }
		// =========================================================================
	    @Override
	    public int getCount() 
	    {
	    	// ---------------------------------------------------------------------
	    	// 20/12/2013 ECU set the title
	    	// 21/12/2013 ECU changed to use the status text view
	    	//            ECU changed to use the stored description
	    	// 12/05/2015 ECU added the check on TextFlag
	    	// ---------------------------------------------------------------------
	    	if (mTextFlag)
	    	{
	    		televisionStatusView.setText (remoteControllers[mRemoteController].codes.description);
	    	}
	    	else
	    	{
	    		// -----------------------------------------------------------------
	    		// 12/05/2015 ECU hide the text view
	    		// -----------------------------------------------------------------
	    		televisionStatusView.setVisibility (View.GONE);
	    	}
	        // ---------------------------------------------------------------------
	        return remoteControllers[mRemoteController].layout.length;
	    }
		// =========================================================================
	    @Override
	    public Object getItem(int position) 
	    {
	        return remoteControllers[mRemoteController].layout[position];
	    }
		/* ========================================================================= */
	    @Override
	    public long getItemId(int position)
	    {
	        return 0;
	    }
	    /* ------------------------------------------------------------------------- */
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) 
	    {
	        ImageView imageView = new ImageView(mContext);
	        imageView.setImageResource(remoteControllers[mRemoteController].layout[position].drawable);
	        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
	        // ---------------------------------------------------------------------
	        // 15/02/2014 ECU added - try to scale image to fit
	        // 16/02/2014 ECU changed to use 'keySize'
	        // ---------------------------------------------------------------------
	        imageView.setLayoutParams(new GridView.LayoutParams(keySize,keySize));
	         
	        return imageView;
	    }
	    /* ------------------------------------------------------------------------- */
	}	
    // =============================================================================	
    static void SendDataToRemote (Context theContext,String theMessage) 
    {
    	// -------------------------------------------------------------------------
    	// 17/12/2013 ECU need to add a terminator to the string
    	// 11/05/2015 ECU added the context as an argument
    	// -------------------------------------------------------------------------
    	String localString = theMessage + MESSAGE_TERMINATOR;
    	// -------------------------------------------------------------------------
    	// 31/12/2013 ECU decide which way to send the data
    	// -------------------------------------------------------------------------
    	if (hardwareInterface == USE_USB)
    	{
    		// ---------------------------------------------------------------------
    		// 16/12/2013 ECU now get the arduino service to process the data
    		// ----------------------------------------------------------------------
    		Intent intent = new Intent(ArduinoCommunicatorService.SEND_DATA_INTENT);
    		intent.putExtra (ArduinoCommunicatorService.DATA_EXTRA, localString.getBytes());
    		theContext.sendBroadcast (intent);
    	}
    	else
    	if (hardwareInterface == USE_BLUETOOTH)
    	{
    		// ---------------------------------------------------------------------
    		// 27/02/2016 ECU check if service is still running and also is there is
    		//                a registered remote controller server
    		// ---------------------------------------------------------------------
    		if (PublicData.blueToothService)
    		{
    			blueToothHandler.SendMessage (localString);
    		}
    		else
    		{
    			// -----------------------------------------------------------------
    			// 27/02/2016 ECU service not running on this device so check for a
    			//                server
    			// -----------------------------------------------------------------
    			if (PublicData.remoteControllerServer != null)
    			{
    				Utilities.sendSocketMessageSendTheObject (theContext,
							  PublicData.remoteControllerServer, 
							  PublicData.socketNumberForData,
							  StaticData.SOCKET_MESSAGE_OBJECT, 
							  new RemoteControllerRequest (localString));
    			}
    			// -----------------------------------------------------------------
    		}
    	}
    }
    // =============================================================================
 	public static boolean validation (int theArgument)
 	{
 		// -------------------------------------------------------------------------
 		// 09/03/2015 ECU this is called up by GridActivity to determine whether
 		//                this activity is valid on this device
 		// -------------------------------------------------------------------------
 		return true;
 		// -------------------------------------------------------------------------
 	}
 	// =============================================================================



	// =============================================================================
	public static void changeStoredTVChannels (Context theContext,String theChannelName,int theChannelNumber)
	{
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU change the stored data for the specified channel
		// -------------------------------------------------------------------------
		List<String> TVChannels = Television.getStoredTVChannels
				(theContext,PublicData.projectFolder + theContext.getString (R.string.tv_channels_file));
		// -------------------------------------------------------------------------
		String 	components [];
		boolean	entryFound = false;
		// -------------------------------------------------------------------------
		for (int index = 0; index < TVChannels.size (); index++)
		{
			// ---------------------------------------------------------------------
			components = (TVChannels.get(index)).split (StaticData.ACTION_DELIMITER);
			// ---------------------------------------------------------------------
			if ((components.length == 2) && (components [CHANNEL_NAME].equalsIgnoreCase (theChannelName)))
			{
				// -----------------------------------------------------------------
				// 06/07/2020 ECU the entry already exists so update the record
				// -----------------------------------------------------------------
				TVChannels.set (index,theChannelName + StaticData.ACTION_DELIMITER + theChannelNumber);
				// -----------------------------------------------------------------
				entryFound = true;
				// -----------------------------------------------------------------
				// 06/07/2020 ECU the record has been changed so can exit
				// -----------------------------------------------------------------
				break;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU the channel is not already in the list so add it
		// -------------------------------------------------------------------------
		if (!entryFound)
			TVChannels.add (theChannelName + StaticData.ACTION_DELIMITER + theChannelNumber);
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU write the data to disk - the 'true' flag indicates that
		//                blank entries will not be written to disk
		// -------------------------------------------------------------------------
		Utilities.writeAFile (PublicData.projectFolder + theContext.getString (R.string.tv_channels_file),TVChannels,true);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	 public static List<String> getStoredTVChannels (Context theContext,String theFileName)
	{
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU created to obtain the information from the 'raw' file and
		//                the 'text' file, with the 'text' file data taking
		//                precedence.
		//
		//                The reason for these files is that the channel number that
		//                is returned when the EPG is generated may not coincide with
		//                the channel number that the 'remote controller' needs to
		//                control the television
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU read in the data from 'raw' file
		// -------------------------------------------------------------------------
		List<String> rawTVChannels = Utilities.readRawResourceAsList (theContext,R.raw.tv_channels);
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU read in the data that is stored in the 'text' file
		// -------------------------------------------------------------------------
		List<String> textTVChannels = Utilities.readAFileAsList (theFileName);
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU want to combine both lists but the data in the 'text' file
		//                must take precedence and duplicates are not allowed
		//            ECU changed to use TVChannelString
		// -------------------------------------------------------------------------
		String	  		 rawChannelToDelete;
		TVChannelString  rawString;
		TVChannelString  textString;
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU scan through the data for the text file
		// -------------------------------------------------------------------------
		for (String textTVChannel : textTVChannels)
		{
			// ---------------------------------------------------------------------
			textString = new TVChannelString (textTVChannel);
			// ---------------------------------------------------------------------
			if (textString.validFormat ())
			{
				rawChannelToDelete = null;
				// -----------------------------------------------------------------
				for (String rawTVChannel : rawTVChannels)
				{
					// -------------------------------------------------------------
					rawString = new TVChannelString (rawTVChannel);
					// -------------------------------------------------------------
					if (rawString.validFormat ())
					{
						if ((textString.channelName).equalsIgnoreCase (rawString.channelName))
						{
							// -----------------------------------------------------
							// 06/07/2020 ECU there is an entry in both the 'text'
							//                and 'raw' data
							// -----------------------------------------------------
							rawChannelToDelete = rawTVChannel;
							// -----------------------------------------------------
						}
						// ----------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 06/07/2020 ECU check if there is a duplicated record to delete
				// -----------------------------------------------------------------
				if (rawChannelToDelete != null)
				{
					// -------------------------------------------------------------
					// 06/07/2020 ECU delete the duplicate entry
					// -------------------------------------------------------------
					rawTVChannels.remove (rawChannelToDelete);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU at this point need to combine the lists
		// -------------------------------------------------------------------------
		textTVChannels.addAll (rawTVChannels);
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU return the combined list
		// -------------------------------------------------------------------------
		return textTVChannels;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
 	public static void initialiseTVChannels (Context theContext,String theFileName)
 	{
 		// -------------------------------------------------------------------------
 		// 18/11/2017 ECU initialise the inbuilt channels
 		// -------------------------------------------------------------------------
 		televisionChannels =  new ArrayList<TelevisionChannel> ();
 		// -------------------------------------------------------------------------
 		// 18/11/2017 ECU set up the known channels
 		// -------------------------------------------------------------------------
 	 	// 06/07/2020 ECU previously :-
 	 	//
		//					List<String> presetTVChannels
		//						= Utilities.readRawResourceAsList (theContext,R.raw.tv_channels);
		//					List<String> inputTVChannels
		//						= Utilities.readAFileAsList (theFileName);
		//     				presetTVChannels.addAll (inputTVChannels);
		//
		//      		  but this did not allow for duplicated entries so change
 	 	// -------------------------------------------------------------------------
 	 	List <String> presetTVChannels = getStoredTVChannels (theContext,theFileName);
 	 	// -------------------------------------------------------------------------
 	 	// 18/11/2017 ECU check all of the data
 	 	// -------------------------------------------------------------------------
 	 	if (presetTVChannels.size () > 0)
 	 	{
 	 		// ---------------------------------------------------------------------
 	 		// 18/11/2017 ECU there is a file with something to process
 	 		// ---------------------------------------------------------------------
 	 		// 18/11/2017 ECU loop through all input lines
 	 		// ---------------------------------------------------------------------
 	 		boolean		channelFound;
 	 		String		channelName;
 	 		int 		channelNumber;
 	 		String [] 	components;
 	 		// ---------------------------------------------------------------------
 	 		for (int line = 0; line < presetTVChannels.size (); line++)
 	 		{
 	 			// -----------------------------------------------------------------
 	 			// 18/11/2017 ECU each line should have the format
 	 			//                 <channel name><ACTION_DELIMITER><channel number>
 	 			// -----------------------------------------------------------------
 	 			components = presetTVChannels.get (line).split (StaticData.ACTION_DELIMITER);
 	 			// -----------------------------------------------------------------
 	 			// 18/11/2017 ECU check if the two required components are present
 	 			// -----------------------------------------------------------------
 	 			if (components.length == 2)
 	 			{
 	 				try
 	 				{
 	 					// ---------------------------------------------------------
 	 					// 18/11/2017 ECU do any necessary conversions
 	 					//            ECU want to store the channel in lower case
 	 					// ---------------------------------------------------------
 	 					channelNumber	= Integer.parseInt (components [CHANNEL_NUMBER]);
 		 				channelName 	= components [CHANNEL_NAME].toLowerCase (Locale.getDefault());
 		 				// ---------------------------------------------------------
 		 				// 18/11/2017 ECU check whether this channel already exists
 		 				// ---------------------------------------------------------
 		 				channelFound = false;
 		 				for (int channel = 0; channel < televisionChannels.size (); channel++)
 		 				{
 		 					// -----------------------------------------------------
 		 					// 18/11/2017 ECU check if the name is found
 		 					// -----------------------------------------------------
 		 					if (televisionChannels.get (channel).channelName.equalsIgnoreCase(channelName))
 		 					{
 		 						// -------------------------------------------------
 		 						// 18/11/2017 ECU just change the channel number
 		 						// -------------------------------------------------
 		 						televisionChannels.get(channel).channel = channelNumber;
 		 						// -------------------------------------------------
 		 						// 18/11/2017 ECU break out of the loop after indicating
 		 						//                it has been found
 		 						// -------------------------------------------------
 		 						channelFound = true;
 		 						break;
 		 						// -------------------------------------------------
 		 					}
 		 					// -----------------------------------------------------
 		 				}
 		 				// ---------------------------------------------------------
 		 				// 18/11/2017 ECU check if channel found
 		 				// ---------------------------------------------------------
 		 				if (!channelFound)
 		 				{
 		 					// -----------------------------------------------------
 		 					// 18/11/2017 ECU channel not found so add into the array
 		 					// -----------------------------------------------------
 		 					televisionChannels.add (new TelevisionChannel (channelName,channelNumber));
 		 					// -----------------------------------------------------
 		 				}
 		 				// ---------------------------------------------------------
 	 				}
 	 				catch (Exception theException)
 	 				{
 	 					// ---------------------------------------------------------
 	 					// 18/11/2017 ECU have an invalid format for the number
 	 					// ---------------------------------------------------------
 	 					// ---------------------------------------------------------
 	 				}		
 	 			}
 	 			else
 	 			{
 	 				// -------------------------------------------------------------
 	 				// 18/11/2017 ECU the two components are not present
 	 				// -------------------------------------------------------------
 	 				
 	 				// -------------------------------------------------------------
 	 			}
 	 		}
 	 	}
 	}
	// =============================================================================
 }

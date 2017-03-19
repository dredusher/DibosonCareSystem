package com.usher.diboson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.usher.diboson.util.SystemUiHider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * This class contains a wide range of methods which provide
 * tasks for other activities and services in the system.
 * <p>
 * It is never run directly as an activity.
 *
 * @author Ed Usher
 * 
 * 
 */
// =================================================================================
public class Utilities extends Activity 
{
	// =============================================================================
	// 01/05/2013 ECU created - contains general methods used by other activities and
	//                services that form the Diboson project
	// 01/08/2013 ECU removed scanNetwork because now using DiscoverNetwork activity
	// 23/05/2015 ECU changed 'myCamera' to 'localCamera'
	// 29/12/2016 ECU changed 'localCamera' to 'frontCamera'
	//            ECU 'flash light' handling methods taken out and placed in new
	//                FlashLight class
	// =============================================================================
	// Testing
	// =======
	// 01/05/2013 ECU individual methods will be tested in conjunction with the calling
	//                software
	// =============================================================================
	
	/* ============================================================================= */
	private static final String TAG = "Utilities";
	/* ============================================================================= */
	private static String [] 	listWords;
	private static String [] 	lastListWords;
	private static int			medicationIndex;				// 25/03/2014 ECU added
	public  static String 		socketResponse 		= null;		// 25/07/2013 ECU added
	/* ============================================================================= */

	// =============================================================================
	public static void abortMedia ()
	{
		// -------------------------------------------------------------------------
		// 28/12/2016 ECU created to abort any media (audio and/or video) that may be
		//                running
		// -------------------------------------------------------------------------
		// 28/12/2016 ECU check the media player
		// -------------------------------------------------------------------------
		MusicPlayer.closeMediaPlayer ();
		// -------------------------------------------------------------------------
		// 28/12/2016 ECU check the video player
		// -------------------------------------------------------------------------
		VideoViewer.closeVideoPlayer ();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static String AbsoluteFileName (String theRelativePathName)
	{
		// -------------------------------------------------------------------------
		// 20/03/2014 ECU created to take a relative path name and to add the
		//                'project folder' name to give an absolute file path
		// -------------------------------------------------------------------------
		return PublicData.projectFolder + theRelativePathName;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void actionDatagram (Context theContext)
	{
		PlayAFile (theContext,PublicData.datagram.fileName);
	}
	// =============================================================================
	public static void actionHandler (Context theContext,String theActions)
	{
		// -------------------------------------------------------------------------
		// 18/03/2015 ECU created to handle actions that are supplied in a string.
		//                The string can contain a number of actions each of which
		//                is separated by MainACtivity.ACTION_SEPARATOR
		// 03/05/2015 ECU added the SCREEN handling
		// 27/11/2015 ECU added the phone handler
		//            ECU added the play handler
		//            ECU added the speak handler
		//            ECU added the silence handler
		// 11/12/2015 ECU added the SMS handler
		// 02/05/2016 ECU added the TIME handler
		// 21/05/2016 ECU added the VIDEO handler
		// 03/06/2016 ECU basic rewrite to use the message handler to try and make
		//                sure that an action is completed before the next one is
		//                started
		// -------------------------------------------------------------------------
		// 18/03/2015 ECU first check that the input parameter is valid
		// -------------------------------------------------------------------------
		if ((theActions != null) && (theActions.length() > 0))
		{
			// ---------------------------------------------------------------------
			// 28/04/2016 ECU check if this device is being monitored
			// ---------------------------------------------------------------------
			MonitorData.sendMonitorData (theContext,StaticData.MONITOR_DATA_ACTIONS,theActions);
			// ---------------------------------------------------------------------
			// 18/03/2015 ECU split the incoming argument into individual actions
			// 03/06/2016 ECU change to add the actions into a list to enable queueing
			//                of actions
			// ---------------------------------------------------------------------
			PublicData.actions.add (theActions.split (StaticData.ACTION_SEPARATOR));
			// ---------------------------------------------------------------------
			// 03/06/2016 ECU send an empty message because the list contains the data
			// ---------------------------------------------------------------------
			PublicData.messageHandler.sendEmptyMessage (StaticData.MESSAGE_PROCESS_ACTIONS);  
			// ---------------------------------------------------------------------
		}
	}
	// -----------------------------------------------------------------------------
	public static void actionHandler (Context theContext,String theActions,boolean theClearCurrentList)
	{
		// -------------------------------------------------------------------------
		// 06/06/2016 ECU created so that if there is a list of actions currently 
		//                being processed then try and 'finish it immediately' before
		//                processing the new list. By 'finish it' I mean that a
		//                LIST_FINISHED is sent to ignore existing actions and cause
		//                the top of the list to be deleted
		// -------------------------------------------------------------------------
		// 06/06/2016 ECU add the new actions into the list
		// -------------------------------------------------------------------------
		actionHandler (theContext,theActions);
		// -------------------------------------------------------------------------
		// 06/06/2016 ECU if there is already a list being processed then the size
		//                will be greater than 1
		// -------------------------------------------------------------------------
		if (PublicData.actions.size() > 1)
		{
			// -------------------------------------------------
			// 06/06/2016 ECU there is already a list in progress
			//                so try and terminate it
			// -------------------------------------------------
			PublicData.messageHandler.sendEmptyMessage (StaticData.MESSAGE_LIST_FINISHED);
			// -------------------------------------------------
		}
	}
	// =============================================================================
	public static String AddAnS (int theValue)
	{
		// -------------------------------------------------------------------------
		// 31/03/2016 ECU created to perform the trivial task be being called to
		//                decide whether to add an 's' depending on the value for
		//                display. e.g. if value is for minutes then 1 does not need
		//				  an 's', other values do - 1 minute, 2 minutes, .... yes
		//                it is trivial but useful
		// -------------------------------------------------------------------------
		if (theValue == 1)
			return "";
		else
			return "s";
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static String AddAnS (long theValue)
	{
		// -------------------------------------------------------------------------
		// 31/03/2016 ECU created to perform the trivial task be being called to
		//                decide whether to add an 's' depending on the value for
		//                display. e.g. if value is for minutes then 1 does not need
		//				  an 's', other values do - 1 minute, 2 minutes, .... yes
		//                it is trivial but useful
		// -------------------------------------------------------------------------
		if (theValue == 1l)
			return "";
		else
			return "s";
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@SuppressLint ("InlinedApi") 
	public static boolean airPlaneMode (Context theContext) 
	{
		// -------------------------------------------------------------------------
		// 23/04/2016 ECU created to return the state of 'airplane mode'
		//                  true ...... airplane mode is on
		//                  false ..... airplane mode is off
		// -------------------------------------------------------------------------
		 return (Settings.System.getInt (theContext.getContentResolver(),Global.AIRPLANE_MODE_ON, 0) != 0);
		 // ------------------------------------------------------------------------
	}
	// =============================================================================
	public static String AlarmActionsAsString (Context theContext,int theAlarmData)
	{
		// -------------------------------------------------------------------------
		// 03/04/2016 ECU created to return the alarm actions as a delimited string
		// -------------------------------------------------------------------------
		String localString = "";
		// -------------------------------------------------------------------------
		// 03/04/2016 ECU get a local copy of the stored activity strings
		// -------------------------------------------------------------------------
		String [] localActivities = theContext.getResources().getStringArray(R.array.alarm_activities);
		// -------------------------------------------------------------------------
		// 03/04/2016 ECU loop through all available actions
		// 26/01/2017 ECU changed from DELIMITER to DISPLAY_ - easier to read
		// -------------------------------------------------------------------------
		for (int theBit = 0; theBit < localActivities.length; theBit++)
		{
			if (Utilities.bitHandler(theAlarmData,theBit))
				localString += localActivities [theBit] + StaticData.ACTION_DISPLAY_TOGETHER;
		}
		// -------------------------------------------------------------------------
		// 03/04/2016 ECU return the completed activities after removing the last 
		//                character. If the programmer hasn't updated the strings
		//                array correctly then localString will not be set correctly
		//                so just return theAlarmData
		// 26/01/2017 ECU remove the final DISPLAY_TOGETHER
		// -------------------------------------------------------------------------
		if (localString.length() > 1)
			return localString.substring (0,localString.length() - StaticData.ACTION_DISPLAY_TOGETHER.length());
		else
			return Integer.toString (theAlarmData);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void AnimateAnImageView (final ImageView theImageView,
			int fadeInTime, int onScreenTime, int fadeOutTime, int repeatCount)
	{
		// -------------------------------------------------------------------------
		// 05/11/2013 ECU fade in the image view
		// -------------------------------------------------------------------------
		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator (new DecelerateInterpolator()); 
		fadeIn.setDuration (fadeInTime);
		// -------------------------------------------------------------------------
		// 05/11/2013 ECU fade out the image view
		// -------------------------------------------------------------------------
		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator (new AccelerateInterpolator());
		fadeOut.setStartOffset (fadeInTime + onScreenTime);
		fadeOut.setDuration (fadeOutTime);
		// -------------------------------------------------------------------------
		// 05/11/2013 ECU now build up the animation set
		// -------------------------------------------------------------------------
		AnimationSet animation = new AnimationSet(false); 
		
		animation.addAnimation (fadeIn);
		animation.addAnimation (fadeOut);
		animation.setRepeatCount (repeatCount);
		animation.setRepeatMode (Animation.RESTART);
		
		theImageView.setAnimation (animation);
		// -------------------------------------------------------------------------
		// 05/11/2013 ECU set up a listener
		// -------------------------------------------------------------------------
		animation.setAnimationListener (new AnimationListener() 
		{
			// ---------------------------------------------------------------------
			public void onAnimationEnd (Animation animation) 
			{	
				theImageView.startAnimation (animation);
			}
			// ---------------------------------------------------------------------
			public void onAnimationRepeat (Animation animation) 
			{
				
			}
			// ---------------------------------------------------------------------
			public void onAnimationStart (Animation animation) 
			{   
				
			}
			// ---------------------------------------------------------------------
		});	
		// -------------------------------------------------------------------------
		// 05/11/2013 ECU now start the animation
		// -------------------------------------------------------------------------
		theImageView.startAnimation (animation);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void AnimateATextView (final TextView theTextView,
										 final String theText,
										 int theTime,
										 final boolean theFadeFlag)
	{
		// -------------------------------------------------------------------------
		// 21/03/2014 ECU fade the textview depending on the flag
		//                    theFadeFlag  = true   fade in
		//                                 = false  fade out
		// --------------------------------------------------------------------------
		Animation fade = theFadeFlag ? (new AlphaAnimation(0, 1)) : (new AlphaAnimation(1, 0));
		fade.setInterpolator(new DecelerateInterpolator()); 
		fade.setDuration(theTime);
		// ---------------------------------------------------------------------------
		// 05/11/2013 ECU now build up the animation set
		// ---------------------------------------------------------------------------
		AnimationSet animation = new AnimationSet(false); 
		
		animation.addAnimation(fade);
				
		theTextView.setAnimation(animation);
		// -------------------------------------------------------------------------
		// 05/11/2013 ECU set up a listener
		// -------------------------------------------------------------------------
		animation.setAnimationListener(new AnimationListener() 
		{
			public void onAnimationEnd(Animation animation) 
			{
				if (!theFadeFlag)
					theTextView.setText(theText);
			}
			public void onAnimationRepeat(Animation animation) 
			{
				
			}
			public void onAnimationStart(Animation animation) 
			{   
				if (theFadeFlag)
					theTextView.setText(theText);
			}
		});	
		// -------------------------------------------------------------------------
		// 05/11/2013 ECU now start the animation
		// -------------------------------------------------------------------------
		theTextView.startAnimation(animation);
	}
	// =============================================================================
	public static void AnimateFlashImageView (final ImageView theImageView,
											  int theFlashDuration,
											  int theRepeatCount)
	{
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU created to cause the specified image to flash - the
		//                duration is in milliSeconds
		// -------------------------------------------------------------------------
		Animation localAnimation = new AlphaAnimation (1,0);
		localAnimation.setDuration (theFlashDuration);
		// -------------------------------------------------------------------------
		// 14/07/2016 ECU changed from LinearInterpolator
		// -------------------------------------------------------------------------
		localAnimation.setInterpolator (new DecelerateInterpolator ());
		localAnimation.setRepeatCount (theRepeatCount);
		localAnimation.setRepeatMode (Animation.REVERSE);
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU now start the animation
		// -------------------------------------------------------------------------
		theImageView.startAnimation (localAnimation);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void AnnounceDevice (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 28/10/2014 ECU called up at the start of the app to see whether the details
		//                of the device are to be announced. Only do this if the
		//                appropriate flag is enabled
		// -------------------------------------------------------------------------
		if (PublicData.storedData.announceFlag)
		{
			// ---------------------------------------------------------------------
			// 28/10/2014 ECU just add an entry in the log file
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG, "Announce Device");
			// ---------------------------------------------------------------------
			// 28/10/2014 ECU only concerned if this device is a Smart phone
			// ---------------------------------------------------------------------
			String phoneNumber = getPhoneNumber(theContext);
			if (phoneNumber != null)
			{
				// -----------------------------------------------------------------
				// 28/10/2014 ECU device is a smart phone so announce itself
				// -----------------------------------------------------------------
				sendSMSMessage (theContext.getString(R.string.mobile_number_ed),
						          "New device with phone number " + phoneNumber);
				// -----------------------------------------------------------------
				// 28/10/2014 ECU also send an email message with the number
				// -----------------------------------------------------------------
				SendEmailMessage (theContext,
								  "New Device",
								  "New device with phone number " + phoneNumber,
								  true);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 28/10/2014 ECU only want to do once so disable the flag whether any
			//                action was taken or not
			// ---------------------------------------------------------------------
			PublicData.storedData.announceFlag = false;
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	public static void AppendToFile (String theFileName,String theDataToWrite)
	{
		// -------------------------------------------------------------------------
		// 10/01/2014 ECU the method will write the data to the end of the specified
		//				  file
		// -------------------------------------------------------------------------
		FileWriter fileWriter;
		
		try 
		{
			// ---------------------------------------------------------------------
			// 10/01/2014 ECU open to the file in append mode
			// ---------------------------------------------------------------------
			fileWriter = new FileWriter (theFileName,true);
			
			fileWriter.write (theDataToWrite);
			// ---------------------------------------------------------------------
			// 10/01/2014 ECU flush out the data and close
			// ---------------------------------------------------------------------
			fileWriter.flush();
			fileWriter.close();
		}
		catch (IOException theException)
		{
			
		}
	}
	// =============================================================================
	public static void BackKeyNotAllowed (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 10/04/2016 ECU created to tell the user that the Back Key is not allowed
		// -------------------------------------------------------------------------
		popToastAndSpeak (theContext.getString (R.string.back_key_not_allowed));
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static byte bitHandler (byte theAction,byte theByte,int theBit)
	{
		// -------------------------------------------------------------------------
		// 19/08/2013 ECU created - provides various bit handling options
		// -------------------------------------------------------------------------
		
		byte localByte = theByte;;
		
		switch (theAction)
		{
			// ---------------------------------------------------------------------
			case StaticData.BIT_SET:
					localByte = (byte) (theByte | (1 << theBit));
					break;
			// ---------------------------------------------------------------------
			case StaticData.BIT_UNSET:
					localByte = (byte) (theByte & ~(1 << theBit));
					break;
			// ---------------------------------------------------------------------
			case StaticData.BIT_TOGGLE:
				 	localByte ^= 1 << theBit;
				 	break;
			// ---------------------------------------------------------------------
		}
		return localByte;
	}
	/* ----------------------------------------------------------------------------- */
	public static boolean bitHandler (byte theByte,int theBit)
	{
		// -------------------------------------------------------------------------
		// 19/08/2013 ECU created - return true if bit set or false if not
		// -------------------------------------------------------------------------
		if (((theByte >> theBit) & 1) == 0)
			return false;
		else
			return true;
	}
	/* ----------------------------------------------------------------------------- */
	public static boolean bitHandler (int theInteger,int theBit)
	{
		// -------------------------------------------------------------------------
		// 03/04/2016 ECU created - return true if bit set or false if not
		//            ECU added to handle integer
		// -------------------------------------------------------------------------
		if (((theInteger >> theBit) & 1) == 0)
			return false;
		else
			return true;
	}
	// =============================================================================
	public static String booleanAsString (boolean theItemToCheck)
	{
		// -------------------------------------------------------------------------
		// 05/05/2015 ECU created to check the specified argument and to return
		//                a string depending on its state
		// -------------------------------------------------------------------------
		return (MainActivity.activity.getString(theItemToCheck ? R.string.state_on 
				                                               : R.string.state_off));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static <T> void BroadcastReceiverState (Context theContext,Class <T> theClass,boolean theRequiredState)
	{
		// -------------------------------------------------------------------------
		// 23/12/2015 ECU created to enable or disable a broadcast receiver
		// -------------------------------------------------------------------------
		ComponentName localComponentName = new ComponentName (theContext, theClass);
		// -------------------------------------------------------------------------
		// 23/12/2015 ECU get the current package manager
		// -------------------------------------------------------------------------
		PackageManager packageManager = theContext.getPackageManager ();
		// -------------------------------------------------------------------------
		// 23/12/2015 ECU now set the required state for the defined receiver
		// -------------------------------------------------------------------------
		packageManager.setComponentEnabledSetting (localComponentName,
		        								   theRequiredState ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
		        										            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
		        								   PackageManager.DONT_KILL_APP);
		// ------------------------------------------------------------------------
	}
	// =============================================================================
	public static void BuildSearchStringAndReplace ()
	{
		// -------------------------------------------------------------------------
		// 14/03/2014 ECU created to build the search and replacement strings
		// -------------------------------------------------------------------------
		if (PublicData.patientDetails != null)
			PublicData.searchStringAndReplace.add(new SearchStringAndReplace 
					("PATIENT",PublicData.patientDetails.preferredName));
		
		if (PublicData.carers.size() > 0)
			PublicData.searchStringAndReplace.add(new SearchStringAndReplace 
					("CARER_MAIN",PublicData.carers.get(0).name));
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static int cameraIndex (int theRequiredCamera)
	{
		// -------------------------------------------------------------------------
		// 10/02/2015 ECU created to return the index of required camera
		// -------------------------------------------------------------------------

		// -------------------------------------------------------------------------
		// 10/02/2015 ECU try and find the required camera
		// -------------------------------------------------------------------------
		CameraInfo cameraInfo = new CameraInfo();
		for (int  cameraIndex = 0;cameraIndex < Camera.getNumberOfCameras(); cameraIndex++)
		{   
		    Camera.getCameraInfo (cameraIndex,cameraInfo);
		    if (cameraInfo.facing == theRequiredCamera)
		    {
		    	// -----------------------------------------------------------------
		    	// 10/02/2015 ECU have found the required camera so return its index
		    	// -----------------------------------------------------------------
		    	return cameraIndex;
		    }
		}
		// -------------------------------------------------------------------------
		// 10/02/2015 ECU indicate that required camera not found
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
	}
	// =============================================================================
	public static boolean checkIfAppInstalled (Context theContext,String theApp)
	{
		// -------------------------------------------------------------------------
		// 30/08/2013 ECU checks if the specified package / app is installed
		// -------------------------------------------------------------------------
	    try
	    {
	        theContext.getPackageManager().getApplicationInfo (theApp, 0);
	        return true;
	    } 
	    catch (PackageManager.NameNotFoundException theException)
	    {
	        return false;
	    }
	}
	/* ============================================================================= */
	public static boolean checkForLocationServices (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 18/10/2014 ECU this method checks whether the location services are running
		//                and returns the state. If 'theEnableFlag' is true then
		//                the location services will be enabled if not running
		// 20/10/2014 ECU in testing the software on the CnM tablet found that
		//                isProviderEnabled returns true for GPS even though the
		//                device does not have a GPS sensor - so added the check
		//                using the package manager
		// -------------------------------------------------------------------------
		boolean hasGPSSensor
			= theContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
		// -------------------------------------------------------------------------
		LocationManager locationManager 
			= (LocationManager) theContext.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		// -------------------------------------------------------------------------
		// 20/10/2014 ECU add in the hasGPSSensor test - see the comments above
		// -------------------------------------------------------------------------
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
				(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSSensor))
		{
			// ---------------------------------------------------------------------
			// 18/10/2014 ECU a location service, either network of GPS, is running
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------	
		}
		// -------------------------------------------------------------------------
		// 18/10/2014 ECU the location services are not running so indicate the fact
		// -------------------------------------------------------------------------
		return false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void checkBatteryTriggers (Context theContext,
											 int theBatteryChargeLevel,
											 boolean theChargeStatus)
	{
		// -------------------------------------------------------------------------
		// 14/03/2015 ECU created to check actions that may need to be taken if the
		//                battery charge level hits the triggers
		// 10/04/2015 ECU added the charge status flag which indicates
		//						true    .... device is charging
		// 					    false   .... device is not charging
		// -------------------------------------------------------------------------
		// 14/03/2015 ECU check the lower trigger but only if not previously triggered
		// 04/05/2015 ECU changed test from '<=' to '<'
		// -------------------------------------------------------------------------
		if (!PublicData.storedData.battery.triggered && 
				(theBatteryChargeLevel < PublicData.storedData.battery.lowerTrigger))
		{
			// ---------------------------------------------------------------------
			// 10/04/2015 ECU if the battery is already being charged then no point
			//                in taking action which is normally to switch on the
			//                WeMo device which has the charger
			//            ECU but indicate that the trigger has been actioned
			// 24/10/2015 ECU changed to use the method
			// ---------------------------------------------------------------------
			PublicData.storedData.setBatteryTriggered (true);
			// ---------------------------------------------------------------------
			if (!theChargeStatus)
			{
				// -----------------------------------------------------------------
				// 10/04/2015 ECU not charging so action
				// -----------------------------------------------------------------
				// 14/03/2015 ECU indicate that 'triggered' and then take the actions
				// 18/03/2015 ECU changed to use the local 'actionHandler' instead
				//                of WeMoActivity.voiceCommands
				// -----------------------------------------------------------------
				actionHandler (theContext,PublicData.storedData.battery.lowerTriggerActions);
				// -----------------------------------------------------------------
				// 14/03/2015 ECU tell the user what is going on
				// 16/03/2015 ECU change the message format
				// -----------------------------------------------------------------
				Utilities.SpeakAPhrase (theContext,
					String.format (theContext.getString(R.string.battery_lower_trigger_message),theBatteryChargeLevel,PublicData.storedData.battery.lowerTrigger));
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 10/04/2015 ECU below the threshold but already being charged
				//                so tell the user what is happening
				// -----------------------------------------------------------------
				Utilities.SpeakAPhrase (theContext,
						String.format(theContext.getString(R.string.battery_lower_trigger_message),theBatteryChargeLevel,PublicData.storedData.battery.lowerTrigger) +
						theContext.getString (R.string.no_action_already_charging));
				// -----------------------------------------------------------------
			}
		}
		else
		// -------------------------------------------------------------------------
		// 14/03/2015 ECU check the upper trigger but only if previously triggered
		// 04/05/2015 ECU cjange the check from '>=' to '>'
		// -------------------------------------------------------------------------
		if (PublicData.storedData.battery.triggered && 
				(theBatteryChargeLevel > PublicData.storedData.battery.upperTrigger))
		{
			// ---------------------------------------------------------------------
			// 14/03/2015 ECU indicate that 'no longer triggered' and then take
			//                actions
			// 18/03/2015 ECU changed to use the local 'actionHandler' instead
			//                of WeMoActivity.voiceCommands
			// 24/10/2015 ECU changed to use new method
			// ---------------------------------------------------------------------
			PublicData.storedData.setBatteryTriggered (false);
			actionHandler (theContext,PublicData.storedData.battery.upperTriggerActions);
			// ---------------------------------------------------------------------
			// 14/03/2015 ECU tell the user what is going on
			// 16/03/2015 ECU change the message format
			// ---------------------------------------------------------------------
			Utilities.SpeakAPhrase (theContext,
					String.format(theContext.getString(R.string.battery_upper_trigger_message),theBatteryChargeLevel,PublicData.storedData.battery.upperTrigger));
			// ---------------------------------------------------------------------			
		}	
	}
	// =============================================================================
	// 02/03/2015 ECU the 'lightCounter' is used to determine how many consequetive
	// 				  light levels are below the threshold before deeming that the
	//                light is 'too dark'
	// 01/09/2015 ECU changed to use StaticData
	// -----------------------------------------------------------------------------
	private static int 			lightPeriod = StaticData.LIGHT_PERIOD_DEFAULT;
	// -----------------------------------------------------------------------------
	public static void checkLightLevel (Context theContext,float theLightLevel)
	{
		// -------------------------------------------------------------------------
		// 02/03/2015 ECU this method has been created from code that used to be in
		//                GridActivity
		// -------------------------------------------------------------------------
   		// 			  ECU just do something to show that can check on light level 
   		// 12/09/2013 ECU changed to use the static variables and do the rearm
   		// 17/02/2014 ECU changed to use the 'storedData' value
		// 06/03/2015 ECU changed the logic to reflect the use of a period of time
		//                to trigger the darkness warning rather than a consequetive
		//                number of readings (see Notes)
		// 23/10/2015 ECU ..Triggered moved into storedData
   		// -------------------------------------------------------------------------
   		if (theLightLevel >= (float) PublicData.storedData.ambient_light_trigger 
   				&& !PublicData.storedData.ambientLightTriggered)
   		{
   			// ---------------------------------------------------------------------
   			// 02/03/2015 ECU reset the light counter to try and eliminate short
   			//                periods of 'darkness' which should not trigger the alarm
   			// 06/03/2015 ECU changed from using a 'counter' to being a 'period'
   			// ---------------------------------------------------------------------
   			lightPeriod = PublicData.storedData.ambient_light_period;
   			// ---------------------------------------------------------------------
   		}
   		// -------------------------------------------------------------------------
   		// 12/09/2013 ECU check if the trigger has to be re-armed
   		// 23/10/2015 ECU ..Triggered moved into storedData
   		// -------------------------------------------------------------------------
   		if (PublicData.storedData.ambientLightTriggered)
   		{
   			// ---------------------------------------------------------------------
   			// 12/09/2013 ECU if the low light level warning has previously been 
   			//                displayed then check for re-arming
   			// 17/02/2014 ECU changed to use the 'storedData' value
   			// ---------------------------------------------------------------------
   			if (theLightLevel >= (float) PublicData.storedData.ambient_light_rearm)
   			{
   			// -----------------------------------------------------------------
				// 08/03/2015 ECU the light level means that the 'darkness warning'
   				//                mechanism can rearmed.
   				// 15/07/2015 ECU only display image if screen if on 
   				// 31/01/2017 ECU changed to use new way of displaying the drawable
				// -----------------------------------------------------------------
				if (isTheScreenOn (theContext))
				{
					// -------------------------------------------------------------
					// 31/01/2017 ECU Note - this is the old way of displaying the
					//                       drawable. Keep the code for reference
					// --------------------------------------------------------------
					//Intent localIntent = new Intent (theContext,DisplayDrawableActivity.class);
					// -------------------------------------------------------------
					// 02/03/2015 ECU indicate that new task is to be started
					// -------------------------------------------------------------
					//localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					// -------------------------------------------------------------
					// 17/02/2014 ECU indicate that want a lightbulb image
					// 05/04/2014 ECU indicate that fling is not allowed
					// -------------------------------------------------------------	
					//localIntent.putExtra (StaticData.PARAMETER_FLING_ENABLED, false);
					//localIntent.putExtra (StaticData.PARAMETER_HELP_ID,R.drawable.darkness_off);	
					//theContext.startActivity (localIntent);
					// -------------------------------------------------------------
					// 31/01/2017 ECU add the new way of displaying the drawable
					// -------------------------------------------------------------
					MessageHandler.displayDrawable (R.drawable.darkness_off);
				}
				// -----------------------------------------------------------------
				// 12/09/2013 ECU re-arm the low light level monitoring
				// 23/10/2015 ECU ..Triggered moved into storedData
				// 24/10/2015 ECU changed to use the method
   				// -----------------------------------------------------------------
				PublicData.storedData.setAmbientLightTriggered (false);
				// -----------------------------------------------------------------
				// 16/02/2014 ECU inform the user that it is 'dark' according to the
				//                rules
				// 06/03/2015 ECU changed to use the resource
				// -----------------------------------------------------------------
				Utilities.SpeakAPhrase (theContext,theContext.getString (R.string.ambient_rearmed));
   				// -----------------------------------------------------------------
   				// 07/03/2015 ECU action any commands that have been set
				// 18/03/2015 ECU changed to use the local 'actionHandler' instead
				//                of WeMoActivity.voiceCommands
   				// -----------------------------------------------------------------
   				actionHandler (theContext,PublicData.storedData.darknessOffActions);
   				// -----------------------------------------------------------------
   			}
   		}
	}
	// -----------------------------------------------------------------------------
	public static void checkLightLevel (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 06/03/2015 ECU created to be called each second by the TimerService to
		//                take any action that is required. See issues detailed
		//				  in Notes
		//            ECU put in the check on NO_RESULT for those devices which
		//                do not have an ambient light sensor
		// 07/03/2015 ECU changed to check the presence of the sensor directly rather
		//                than checking the NO_RESULT which was clumsy
		// 23/10/2015 ECU ..Triggered moved into storedData
		// -------------------------------------------------------------------------
		if ((SensorService.ambientLightSensor) && 
				(SensorService.lightLevel < (float) PublicData.storedData.ambient_light_trigger) &&
   				 !PublicData.storedData.ambientLightTriggered)
   		{
			if (lightPeriod-- == 0)
			{
				// -----------------------------------------------------------------
				// 06/03/2015 ECU if the period of time when the light level is below the
				//                trigger level has been reached then display/speak the
				//                warning
				// 15/07/2015 ECU only display image if screen if on 
				// -----------------------------------------------------------------
				if (isTheScreenOn (theContext))
				{
					// -------------------------------------------------------------
					// 31/01/2017 ECU Note - this is the old way of displaying the
					//                       drawable. Keep for reference
					// -------------------------------------------------------------
					//Intent localIntent = new Intent (theContext,DisplayDrawableActivity.class);
					// -------------------------------------------------------------
					// 02/03/2015 ECU indicate that new task is to be started
					// 15/07/2015 ECU only display image if screen if on 
					// -------------------------------------------------------------
					//localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
					// -------------------------------------------------------------
					// 17/02/2014 ECU indicate that want a lightbulb image
					// 05/04/2014 ECU indicate that fling is not allowed
					// -------------------------------------------------------------	
					//localIntent.putExtra (StaticData.PARAMETER_FLING_ENABLED, false);
					//localIntent.putExtra (StaticData.PARAMETER_HELP_ID,R.drawable.lightbulb);	
					//theContext.startActivity (localIntent);
					// -------------------------------------------------------------
					// 31/01/2017 ECU add the new way of displaying the drawable
					// -------------------------------------------------------------
					MessageHandler.displayDrawable (R.drawable.lightbulb);
				}
				// -----------------------------------------------------------------
				// 12/09/2013 ECU indicate that the warning has been displayed
				// 23/10/2015 ECU ..Triggered moved into storedData
				// 24/10/2015 ECU changed to use the method
				// -----------------------------------------------------------------
				PublicData.storedData.setAmbientLightTriggered (true);
				// -----------------------------------------------------------------
				// 16/02/2014 ECU inform the user that it is 'dark' according to the
				//                rules
				// 06/03/2015 ECU changed to use the resource
				// -----------------------------------------------------------------
				Utilities.SpeakAPhrase (theContext,theContext.getString (R.string.getting_dark));
				// -----------------------------------------------------------------
   				// 07/03/2015 ECU action any commands that have been set for when
				//                darkness is declared
				// 18/03/2015 ECU changed to use the local 'actionHandler' instead
				//                of WeMoActivity.voiceCommands
   				// -----------------------------------------------------------------
   				actionHandler (theContext,PublicData.storedData.darknessOnActions);
   				// -----------------------------------------------------------------
			}
   		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static Method createAMethod (String theMethodName)
	{
		// -------------------------------------------------------------------------
		// 19/09/2013 ECU created - this will create a method for a method in
		//                this class that has no arguments
		// -------------------------------------------------------------------------
		return createAMethod (Utilities.class,theMethodName);	
	}
	/* ----------------------------------------------------------------------------- */
	public static Method createAMethod (String theMethodName,String theString)
	{
		// -------------------------------------------------------------------------
		// 19/09/2013 ECU created - this will create a method for a method in
		//                this class that has a string argument
		// -------------------------------------------------------------------------
		return createAMethod (Utilities.class,theMethodName,theString);
	}
	/* ----------------------------------------------------------------------------- */
	public static Method createAMethod (String theMethodName,int theInteger)
	{
		// -------------------------------------------------------------------------
		// 19/09/2013 ECU created - this will create a method for a method in
		//                this class that has a integer argument
		// -------------------------------------------------------------------------
		return createAMethod (Utilities.class,theMethodName,theInteger);
	}
	/* ----------------------------------------------------------------------------- */
	public static <T> Method createAMethod (Class<T> theClass,String theMethodName)
	{
		// --------------------------------------------------------------------------
		// 19/09/2013 ECU created - this will create a method and invoke it with
		//                no arguments
		// --------------------------------------------------------------------------
		try 
		{
			// ----------------------------------------------------------------------
			// 19/09/2013 ECU create the method from the supplied class, method and
			//                arguments (although there are none in this case)
			// ----------------------------------------------------------------------
			return theClass.getMethod (theMethodName,new Class [] {});	
		}
		catch (Exception theException) 
		{
			return null;
		} 
	}
	/* ----------------------------------------------------------------------------- */
	public static <T> Method createAMethod (Class<T> theClass,String theMethodName,int theInteger)
	{
		// -------------------------------------------------------------------------
		// 19/09/2013 ECU created - this will create a method and invoke it with
		// -------------------------------------------------------------------------		
		try 
		{
			// ---------------------------------------------------------------------
			// 19/09/2013 ECU create the method from the supplied class, method and
			//                arguments (although there are none in this case)
			// ---------------------------------------------------------------------
			return theClass.getMethod (theMethodName,new Class [] {int.class});
		}
		catch (Exception theException) 
		{
			return null;
		} 
	}
	/* ----------------------------------------------------------------------------- */
	public static <T> Method createAMethod (Class<T> theClass,String theMethodName,String theString)
	{
		// -------------------------------------------------------------------------
		// 19/09/2013 ECU created - this will create a method and invoke it with
		// -------------------------------------------------------------------------		
		try 
		{
			// ---------------------------------------------------------------------
			// 19/09/2013 ECU create the method from the supplied class, method and
			//                arguments (although there are none in this case)
			// ---------------------------------------------------------------------
			return theClass.getMethod(theMethodName,new Class [] {String.class});
		}
		catch (Exception theException) 
		{		
			return null;
		} 
	}
	// -----------------------------------------------------------------------------
	public static <T> Method createAMethod (Class<T> theClass,String theMethodName,boolean [] theOptions)
	{
		// -------------------------------------------------------------------------
		// 17/03/2015 ECU created - this will create a method with boolean [] as
		//                the argument
		// -------------------------------------------------------------------------		
		try 
		{
			// ---------------------------------------------------------------------
			// 17/03/2015 ECU create the method from the supplied class, method and
			//                arguments (although there are none in this case)
			// ---------------------------------------------------------------------
			return theClass.getMethod (theMethodName,new Class [] {boolean [].class});
		}
		catch (Exception theException) 
		{
			return null;
		} 
	}
	/* ----------------------------------------------------------------------------- */
	public static <T> Method createAMethod (Class<T> theClass,String theMethodName,ArrayList <String> theStringArray)
	{
		// -------------------------------------------------------------------------
		// 18/09/2015 ECU created - this will create a method with ArrayList <String> as
		//                the argument
		// -------------------------------------------------------------------------		
		try 
		{
			// ---------------------------------------------------------------------
			// 18/09/2015 ECU create the method from the supplied class, method and
			//                arguments (although there are none in this case)
			// ---------------------------------------------------------------------
			return theClass.getMethod (theMethodName,new Class [] {ArrayList.class});
		}
		catch (Exception theException) 
		{
			return null;
		} 
	}
	// -----------------------------------------------------------------------------
	public static <T> Method createAMethod (Class<T> theClass,String theMethodName,Object theObject)
	{
		// -------------------------------------------------------------------------
		// 10/04/2014 ECU declare the method that has an Object as arguments
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 10/04/2015 ECU create the method from the supplied class, method and
			//                arguments
			// ---------------------------------------------------------------------		
			return theClass.getMethod(theMethodName,new Class [] {Object.class});
		}
		catch (Exception theException) 
		{
			return null;
		} 
	}
	// -----------------------------------------------------------------------------
	public static <T> Method createAMethod (Class<T> theClass,String theMethodName,String theString,Object theObject)
	{
		// -------------------------------------------------------------------------
		// 03/04/2014 ECU declare the method that has a String and Object as
		//                arguments
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 19/09/2013 ECU create the method from the supplied class, method and
			//                arguments (although there are none in this case)
			// ---------------------------------------------------------------------		
			return theClass.getMethod(theMethodName,new Class [] {String.class,Object.class});
		}
		catch (Exception theException) 
		{
			return null;
		} 
	}
	// -----------------------------------------------------------------------------
	public static <T> Method createAMethod (Class<T> theClass,String theMethodName,Object [] theObjects)
	{
		// -------------------------------------------------------------------------
		// 07/10/2016 ECU declare the method that has an object array as arguments
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 07/10/2016 ECU create the method from the supplied class, method and
			//                arguments (although there are none in this case)
			// ---------------------------------------------------------------------		
			return theClass.getMethod (theMethodName,new Class [] {Object [] .class});
		}
		catch (Exception theException) 
		{
			return null;
		} 
	}
	/* ----------------------------------------------------------------------------- */
	public static <T> Method createAMethod (Class<T> theClass,String theMethodName,Context theContext)
	{
		// -------------------------------------------------------------------------
		// 18/12/2015 ECU declare the method that has Context as an argument
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 18/12/2015 ECU create the method from the supplied class, method and
			//                arguments
			// ---------------------------------------------------------------------		
			return theClass.getMethod(theMethodName,new Class [] {Context.class});
		}
		catch (Exception theException) 
		{
			return null;
		} 
	}
	/* ----------------------------------------------------------------------------- */
	public static <T> Method createAMethod (Class<T> theClass,String theMethodName,Context theContext,String theString)
	{
		// -------------------------------------------------------------------------
		// 03/04/2014 ECU declare the method that has a Context and String as
		//                arguments
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 19/09/2013 ECU create the method from the supplied class, method and
			//                arguments (although there are none in this case)
			// ---------------------------------------------------------------------		
			return theClass.getMethod(theMethodName,new Class [] {Context.class,String.class});
		}
		catch (Exception theException) 
		{
			return null;
		} 
	}
	/* ============================================================================= */
	public static <T> void createAndInvokeMethod (Class<T> theClass,String theMethodName)
	{
		// -------------------------------------------------------------------------
		// 19/09/2013 ECU created - this will create a method and invoke it with
		//                no arguments
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 19/09/2013 ECU create the method from the supplied class, method and
			//                arguments (although there are none in this case)
			// ---------------------------------------------------------------------
			Method theMethod = createAMethod ((Class <T>) theClass,theMethodName);
			// ---------------------------------------------------------------------
			// 19/09/2013 ECU now invoke the method
			// ---------------------------------------------------------------------
			if (theMethod != null)
				theMethod.invoke(null,(Object []) null);
		}
		catch (Exception theException) 
		{
		} 
	}
	/* ----------------------------------------------------------------------------- */
	public static <T> void createAndInvokeMethod (Class<T> theClass,String theMethodName,String theString)
	{
		// -------------------------------------------------------------------------
		// 19/09/2013 ECU created - this will create a method and invoke it with
		// -------------------------------------------------------------------------		
		try 
		{
			// ---------------------------------------------------------------------
			// 19/09/2013 ECU create the method from the supplied class, method and
			//                arguments (although there are none in this case)
			// ---------------------------------------------------------------------
			Method theMethod = createAMethod ((Class <T>) theClass,theMethodName,theString);
			// ---------------------------------------------------------------------		
			// 19/09/2013 ECU now invoke the method
			// ---------------------------------------------------------------------
			if (theMethod != null)
				theMethod.invoke(null, new Object [] {theString});
		}
		catch (Exception theException) 
		{
		} 
	}
	// =============================================================================
	static String dateAsText (Context theContext,int theDay,int theMonth,int theYear)
	{
		// -------------------------------------------------------------------------
		// 22/04/2015 ECU created to return the data as a string which can be spoken
		// -------------------------------------------------------------------------
		// 22/04/2015 ECU retrieve the array of months that are stored in the
		//                resource string array
		// -------------------------------------------------------------------------
		String[] localMonths = theContext.getResources().getStringArray (R.array.months);
		// -------------------------------------------------------------------------
		// 22/04/2015 ECU want to append a modifier to the day so that the speech is
		//                correct - i.e. 1 becomes '1st', 2 -> 2nd, 3 -> rd,  etc
		//				  The default is 'th' so handle the exceptions
		// -------------------------------------------------------------------------
		String localDayModifier = "th";
		// -------------------------------------------------------------------------
		// 22/04/2015 ECU handle the exceptions
		// -------------------------------------------------------------------------
		switch (theDay)
		{
			// ---------------------------------------------------------------------
			// 22/04/2015 ECU 1,21 and 31 have a proceeding 'st'
			// ---------------------------------------------------------------------
			case 1:
			case 21:
			case 31:
				localDayModifier = "st";
				break;
			// ---------------------------------------------------------------------
			// 22/04/2015 ECU 2 and 22 have a proceeding 'nd'
			// ---------------------------------------------------------------------
			case 2:
			case 22:
				localDayModifier = "nd";
				break;
			// ---------------------------------------------------------------------
			// 22/04/2015 ECU 3 and 23 have a proceeding 'rd'
			// ---------------------------------------------------------------------
			case 3:
			case 23:
				localDayModifier = "rd";
				break;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 22/04/2015 ECU now return the date in its 'speakable' form
		// -------------------------------------------------------------------------
		return " " + theDay + localDayModifier + 
			   " " + localMonths [theMonth -1] + 
			   " " + theYear;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static int DayOfWeek (int theDayOfWeek)
	{
		// -------------------------------------------------------------------------
		// 11/12/2016 ECU this method returns the specified day of the week using
		//                the following notation :-
		//                  0 = Monday, 1 = Tuesday, 2 = Wednesday, ......
		//                the DAY_OF_WEEK from the Calendar instance uses
		//                  1 = Sunday, 2 = Monday, 3 = Tuesday, ....
		// -------------------------------------------------------------------------
		// 11/12/2016 ECU adjust to the Diboson notation
		// -------------------------------------------------------------------------
		theDayOfWeek -= 2;
		return ((theDayOfWeek < 0) ? (theDayOfWeek + 7) : theDayOfWeek);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static int DayOfWeek ()
	{
		// -------------------------------------------------------------------------
		// 22/03/2014 ECU this method returns the current day of the week using
		//                the following notation :-
		//                  0 = Monday, 1 = Tuesday, 2 = Wednesday, ......
		//                the DAY_OF_WEEK from the Calendar instance uses
		//                  1 = Sunday, 2 = Monday, 3 = Tuesday, ....
		// 11/12/2016 ECU changed to use the master method above
		// -------------------------------------------------------------------------
		return DayOfWeek (Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static int DayOfWeek (long theDate)
	{
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU returns the day of the week for the specified date
		// -------------------------------------------------------------------------
		Calendar localCalendar = Calendar.getInstance ();
		
		// -------------------------------------------------------------------------
		localCalendar.setTimeInMillis (theDate);
		// -------------------------------------------------------------------------
		return DayOfWeek (localCalendar.get (Calendar.DAY_OF_WEEK));
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void debugMessage (String theTag, String theMessage)
	{
		// -------------------------------------------------------------------------
		// 04/08/2013 ECU added - will use Log to display a message if in debug
		//                mode
		// 10/03/2015 ECU changed to use 'debugMode in 'storedData'
		// -------------------------------------------------------------------------
		if (PublicData.storedData.debugMode)
		{
			// ---------------------------------------------------------------------
			// 04/08/2014 ECU use the 'info' option for the message
			// 13/08/2013 ECU as a temporary mod - add the adjusted time
			//				  to get a consistent time across devices
			// ---------------------------------------------------------------------
			Log.i (theTag,getAdjustedTime () + " : " + theMessage);
			// ---------------------------------------------------------------------
		}
	}
	/* ----------------------------------------------------------------------------- */
	public static void debugMessage (String theTag, String theMessage,boolean theFlag)
	{
		// -------------------------------------------------------------------------
		// 04/08/2013 ECU added - will use Log to display a message 
		// 04/08/2014 ECU use the 'info' option for the message
		// -------------------------------------------------------------------------	
		Log.i (theTag,theMessage);
		
	}
	// =============================================================================
	public static void debugPopToast (String theMessage)
	{
		// -------------------------------------------------------------------------
		// 10/01/2015 ECU created to display a 'toast' message if the program is
		//                in debug mode
		// 10/03/2015 ECU changed to use 'debugMode in 'storedData'
		// -------------------------------------------------------------------------
		if (PublicData.storedData.debugMode)
		{
			popToast (theMessage);
		}
	}
	// =============================================================================
	public static ArrayList<String> deviceList (boolean theOptionFlag)
	{
		// -------------------------------------------------------------------------
		// 15/03/2015 ECU create to return the list of available compatible devices
		//                if 'theOptionFlag' = true  then the calling device will
		//                                           be included
		//                                   = false then the calling device will
		//                                           not be included
		// 22/03/2015 ECU changed the logic following change to deviceDetails
		//                to List<Devices>
		// -------------------------------------------------------------------------
		ArrayList<String> localDeviceList = new ArrayList<String> ();
		
		if (PublicData.deviceDetails != null)
		{
			for (int theDevice = 0; theDevice < PublicData.deviceDetails.size(); theDevice++)
			{
				// -----------------------------------------------------------------
				// 15/03/2015 ECU loop through 'found' devices for those that are
				//                compatible
				// -----------------------------------------------------------------
				if (PublicData.deviceDetails.get(theDevice).compatible)
				{
					// -------------------------------------------------------------
					// 15/03/2015 ECU this is a compatible device - decide if it is
					//                'this' device and whether it should be included
					// -------------------------------------------------------------
					if (theOptionFlag)
					{
						// ---------------------------------------------------------
						// 15/03/2015 ECU ALL devices are to be included
						// 22/03/2015 ECU changed for List<Devices>
						// ---------------------------------------------------------
						localDeviceList.add(PublicData.deviceDetails.get(theDevice).PrintName());
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 15/03/2015 ECU do not include 'this' device
						// 22/03/2015 ECU changed for List<Devices>
						// ---------------------------------------------------------
						if (!PublicData.deviceDetails.get(theDevice).IPAddress.equalsIgnoreCase(PublicData.ipAddress))
						{
							// -----------------------------------------------------
							// 15/03/2015 ECU add details of the indexed device
							// 22/03/2015 ECU changed for List<Devices>
							// -----------------------------------------------------
							localDeviceList.add (PublicData.deviceDetails.get(theDevice).PrintName());
							// -----------------------------------------------------
						}
					}
				}	
			}
		}
		// -------------------------------------------------------------------------
		// 15/03/2015 ECU return the generated information
		// -------------------------------------------------------------------------
		return localDeviceList;		
	}
	// =============================================================================
	public static String [] deviceListAsArray (boolean theOptionFlag)
	{
		// -------------------------------------------------------------------------
		// 15/03/2015 ECU created, in conjunction with above deviceList, to return
		//                array with details of 'found' compatible devices.
		//                The meaning of 'theOptionFlag' is described in
		//				  above device list
		// -------------------------------------------------------------------------
		ArrayList<String> localDeviceList = deviceList (theOptionFlag);
		// -------------------------------------------------------------------------
		// 15/03/2015 ECU now generated the String array which is required
		// -------------------------------------------------------------------------
		if (localDeviceList.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 15/03/2015 ECU create an array of the correct size and then populate
			//                it
			// ---------------------------------------------------------------------
			String [] localDeviceArray = new String [localDeviceList.size()];
			for (int theDevice = 0; theDevice < localDeviceList.size(); theDevice++)
			{
				localDeviceArray [theDevice] = localDeviceList.get(theDevice);
			}
			// ---------------------------------------------------------------------
			// 15/03/2015 ECU return the array of 'found' devices
			// ---------------------------------------------------------------------
			return localDeviceArray;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 15/03/2015 ECU nothing was found so indicate the fact
			// ---------------------------------------------------------------------
			return null;
		}
		
	}
	// =============================================================================
	@SuppressLint("Wakelock") 
	public static void deviceLock (Context theContext,boolean theLockState)
	{
		// -------------------------------------------------------------------------
		// 27/11/2015 ECU created to lock or unlock the device
		//
		//                theLockState = true    lock the device
		//                             = false   unlock the device
		// -------------------------------------------------------------------------
		if (theLockState)
		{
			// ---------------------------------------------------------------------
			// 27/11/2015 ECU lock the device
			// ---------------------------------------------------------------------
			 DevicePolicyManager devicePolicyManager;

			 devicePolicyManager = (DevicePolicyManager) theContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
			 
			 devicePolicyManager.lockNow ();
		}
		else
		{
			// ---------------------------------------------------------------------
			// 27/11/2015 ECU unlock the device
			// ---------------------------------------------------------------------
			KeyguardManager keyguardManager = (KeyguardManager) theContext.getSystemService(Context.KEYGUARD_SERVICE); 
			final KeyguardManager.KeyguardLock keyguardLock = keyguardManager .newKeyguardLock("MyKeyguardLock"); 
			keyguardLock.disableKeyguard(); 

			PowerManager powerManager = (PowerManager) theContext.getSystemService(Context.POWER_SERVICE); 
			WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK
			                                 | PowerManager.ACQUIRE_CAUSES_WAKEUP
			                                 | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
			wakeLock.acquire();
		}
	}
	// =============================================================================
	public static void displayDocument (Context theContext,String theFileName)
	{
		// -------------------------------------------------------------------------
		// 18/10/2016 ECU created to display a specified document
		// -------------------------------------------------------------------------
		String localDataType = "";
		// -------------------------------------------------------------------------
		if (theFileName.toLowerCase(Locale.getDefault()).endsWith(StaticData.EXTENSION_DOCUMENT))
			localDataType = "application/pdf";
		else
		if (theFileName.toLowerCase(Locale.getDefault()).endsWith(StaticData.EXTENSION_PHOTOGRAPH))
			localDataType = "image/*";
		// -------------------------------------------------------------------------
		// 18/10/2016 ECU created to display the specified pdf file whose file
		//                name is specified
		// ------------------------------.-------------------------------------------
		Uri filePath = Uri.fromFile (new File (theFileName));
		Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
		pdfOpenintent.setFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP);
		pdfOpenintent.setDataAndType (filePath,localDataType);
		try 
		{
			theContext.startActivity (pdfOpenintent);
		}
		catch (ActivityNotFoundException theException) 
		{
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String findFirstDevice ()
	{
		// -------------------------------------------------------------------------
		// 06/08/2013 ECU will find the first device in the deviceDetails array
		//                which is not this device and will return its IP address
		//                or null if cannot find one
		// 22/03/2015 ECU changed the logic following redefinition of deviceDetails
		//                to List<Devices>
		// -------------------------------------------------------------------------
		String theIPAddress = null;
		
		if (PublicData.deviceDetails != null)
		{
			for (int theDevice = 0; theDevice < PublicData.deviceDetails.size(); theDevice++)
			{
				// -----------------------------------------------------------------
				// 06/08/2013 ECU check if the IP address in the element is for this device - if not
				//                then return with this address
				// 19/08/2013 ECU ensure that only looking for compatible devices
				// ------------------------------------------------------------------
				if (!PublicData.deviceDetails.get(theDevice).IPAddress.equalsIgnoreCase(PublicData.ipAddress) &&
						(PublicData.deviceDetails.get(theDevice).compatible))
				{
					theIPAddress = PublicData.deviceDetails.get(theDevice).IPAddress;
					break;
				}	
			}
		}
			
		return theIPAddress;				
	}
	/* ======================================================================= */
	public static void initialiseGPS ()
	{
	    
	}
	/* ======================================================================= */
	public static void waitMilliseconds (final int theWaitTime)
	{
		 Thread thread = new Thread()
		 {
		        @Override
		        public void run()
		        {
		            try 
		            {
		                synchronized(this)
		                {
		                    sleep(theWaitTime);
		                }
		            }
		            catch(InterruptedException ex)
		            { 
		            	                 
		            }       
		        }
		    };

		    thread.start();        		
	}
	/* ============================================================================= */
	public static double adjustByAccuracy (double theNumber, int theAccuracy)
	{
		double localResult;
		
		localResult = (Math.round(theNumber * (double) theAccuracy)) / (double)theAccuracy;
		
		return localResult;
	}
	/* ============================================================================= */
	public static String AdjustedNumber (int theNumber)
	{
		// -------------------------------------------------------------------------
		// 09/02/2014 ECU just return a string which has the number adjusted 
		//                within a 2 character field using a '0' fill
		// 09/03/2014 ECU moved here form the SpeakingClock activity
		// -------------------------------------------------------------------------
		
		return String.format (Locale.getDefault(),"%02d", theNumber);
	}
	/* ============================================================================= */
	public static String AdjustedTime (int theHour,int theMinute)
	{
		// -------------------------------------------------------------------------
		// 09/03/2014 ECU this method returns the supplied time in a formatted
		//                string - 'HH:MM' - in circumstances when SimpleDateFormat
		//                is not suitable
		// -------------------------------------------------------------------------
		return AdjustedNumber (theHour) + ":" + AdjustedNumber (theMinute);
	}
	/* ============================================================================= */
	public String [] BuildPlayList (Context theContext, String theRootDirectory,String theExtension)
	{	
		// -------------------------------------------------------------------------
		// 04/04/2015 ECU changed to return the results rather than setting a
		//                variable directly
		// -------------------------------------------------------------------------
		BuildListOfFilesClass buildClass = new BuildListOfFilesClass ();		
		return buildClass.Run (theContext,theRootDirectory,theExtension);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private class BuildListOfFilesClass
	{
		Vector<String> vectorString = new Vector<String>();
		// ------------------------------------------------------------------------- 
		// 31/05/2013 ECU created
		// 04/04/2015 ECU change to return a string array rather than setting a
		//                variable directly
		/* ------------------------------------------------------------------------- */
		private String [] Run (Context theContext,String theRootDirectory,String theExtension)
		{
			ProcessTheDirectory (new File (theRootDirectory),theExtension);
			// ---------------------------------------------------------------------
			// 04/04/2015 ECU return the results
			// ---------------------------------------------------------------------
		    return vectorString.toArray (new String[vectorString.size()]);    
		}
		/* ------------------------------------------------------------------------- */ 		
		private void ProcessTheDirectory (File theFile,String theExtension) 
		{
			// ---------------------------------------------------------------------
			// 31/05/2013 ECU check if the input file is a file or directory
			// 07/04/2015 ECU added the null check
			// ---------------------------------------------------------------------	
			if (theFile.isFile())
			{
				// -----------------------------------------------------------------
				if (theExtension == null || theFile.getName().endsWith (theExtension))
						vectorString.add (theFile.getAbsolutePath());
			}
			else 
			if (theFile.isDirectory()) 
			{
				// -----------------------------------------------------------------
				// 31/05/2013 ECU get list of files in this directory
				// ----------------------------------------------------------------- 
				File[] listOfFiles = theFile.listFiles();
				 
				if (listOfFiles!=null) 
				{
					// -------------------------------------------------------------
					// 31/05/2013 ECU if there are files then process them recursively
					// ------------------------------------------------------------- 
					for (int index = 0; index < listOfFiles.length; index++)
					{
						ProcessTheDirectory (listOfFiles[index],theExtension);
					}
				}
			}
		}
	}
	/* ============================================================================= */
	public void BuildListOfFiles (Context theContext,String theDirectory,String theExtension)
	{
		// -------------------------------------------------------------------------
		BuildListOfFilesClass buildClass = new BuildListOfFilesClass ();
		buildClass.Run (theContext,theDirectory,theExtension);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static int byteArrayToInteger (byte[] theByteArray) 
	{
		// -------------------------------------------------------------------------
		// 09/08/2013 ECU create - converts the supplied byte [] to an integer
		//                paired with integerToByteArray
		//			  ECU called the new method with 0 offset
		// -------------------------------------------------------------------------
	    return   byteArrayToInteger (theByteArray,0);
	}
	/* ----------------------------------------------------------------------------- */
	public static int byteArrayToInteger (byte [] theByteArray, int theOffset)
	{
		// -------------------------------------------------------------------------
		// 09/08/2013 ECU create - converts the supplied byte [] to an integer
		//                paired with integerToByteArray
		//			  ECU method added to handle offset into array
		// -------------------------------------------------------------------------
		return   theByteArray[theOffset + 3] & 0xFF |
		         (theByteArray[theOffset + 2] & 0xFF) << 8 |
		         (theByteArray[theOffset + 1] & 0xFF) << 16 |
		         (theByteArray[theOffset] & 0xFF) << 24;
	}
	// =============================================================================
	public static void cancelAnAlarm (Context theContext,PendingIntent thePendingIntent,boolean thePendingCancel)
	{
		// -------------------------------------------------------------------------
		// 01/01/2016 ECU created as a general method to cancel the alarm associated
		//                with the pending intent which is supplied as an argument
		// 03/11/2016 ECU changed to use the global alarm manager
		//            ECU added thePendingCancel to be able to cancel a currently
		//                active pending intent
		// -------------------------------------------------------------------------
		if (thePendingCancel)
		{
			thePendingIntent.cancel();
		}
		// -------------------------------------------------------------------------
		PublicData.alarmManager.cancel (thePendingIntent);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void cancelPhoneCall (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 28/11/2015 ECU created to cancel any phone call being made
		// -------------------------------------------------------------------------
		// 29/11/2015 ECU check if call is on this device
		// -------------------------------------------------------------------------
		if (getPhoneNumber (theContext) != null)
		{
			// ---------------------------------------------------------------------
			// 29/11/2015 ECU the call is on this device
			// ---------------------------------------------------------------------
			try 
			{
				// -----------------------------------------------------------------
				// 28/11/2015 ECU get the telephony manager
				// -----------------------------------------------------------------
			    TelephonyManager telephonyManager =
			                    (TelephonyManager) theContext.getSystemService (Context.TELEPHONY_SERVICE);
			    // -----------------------------------------------------------------
			    // 28/11/2015 ECU get the required method
			    // -----------------------------------------------------------------
			    Class<?> classTelephony = Class.forName (telephonyManager.getClass().getName());
			    Method methodGetITelephony = classTelephony.getDeclaredMethod ("getITelephony");
			    // -----------------------------------------------------------------
			    // 28/11/2015 ECU allow access
			    // -----------------------------------------------------------------
			    methodGetITelephony.setAccessible (true);
			    // -----------------------------------------------------------------
			    // 28/11/2015 ECU use the method to get the teephony interface
			    // -----------------------------------------------------------------
			    Object telephonyInterface = methodGetITelephony.invoke(telephonyManager);
			    // -----------------------------------------------------------------
			    // 28/11/2015 ECU from the interface get the 'end call' method
			    // -----------------------------------------------------------------
			    Class<?> telephonyInterfaceClass =  
			                    Class.forName(telephonyInterface.getClass().getName());
			    Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod ("endCall");
			    // -----------------------------------------------------------------
			    // 28/11/2015 ECU now invoke the method to end the call
			    // -----------------------------------------------------------------
			    methodEndCall.invoke(telephonyInterface);
			    // -----------------------------------------------------------------
			} 
			catch (Exception theException) 
			{ 
				// -----------------------------------------------------------------
				// 28/11/2015 ECU exception occurred
				// -----------------------------------------------------------------
			    // -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 29/11/2015 ECU the call must be on the phone server
			// ---------------------------------------------------------------------
			if (PublicData.phoneServer != null)
			{
				// -----------------------------------------------------------------
				// 29/11/2015 ECU there is a phone server so send cancel to it
				// -----------------------------------------------------------------
				if (!PublicData.phoneServer.equalsIgnoreCase (PublicData.ipAddress))
				{
					// -------------------------------------------------------------
					// 18/09/2013 ECU don't try and send a socket message to this device
					//                (see comment above)
					// -------------------------------------------------------------
					Utilities.sendSocketMessageSendStringAndWait (theContext,PublicData.phoneServer, 
							PublicData.socketNumber,"command " + StaticData.SERVER_COMMAND_CANCEL_CALL);	
					// -------------------------------------------------------------
				}
			}
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static boolean CheckDirectoryStructure (Context theContext,String theRootDirectory)
	{
		// -------------------------------------------------------------------------
		// 21/02/2014 ECU this method will check whether the root directory exists - if not
		//                it will create it and all other directories that are required
		//                within it
		// 01/05/2015 ECU changed the name from CreateDirectoryStructure because I
		//                want it called to check the structure to pick up if
		//                subfolders are missing or are new.
		// -------------------------------------------------------------------------
		// 21/02/2014 ECU the following array contains the resource ID's of the directories
		//                that exist in the root directory
		// 24/12/2014 ECU added the web page directory
		// 30/04/2015 ECU added the 'noises' directory
		// 16/10/2015 ECU added the 'EPG' directory
		// 04/11/2015 ECU added the documentation directory
		// 06/01/2016 ECU added the screen capture directory
		// 08/02/2016 ECU added the tracks storage directory
		// 22/07/2016 ECU added the Schedules Direct directory
		// 23/10/2016 ECU added the appointments directory
		// 16/11/2016 ECU changed name of photos_music... to photos_notes....
		// -------------------------------------------------------------------------
		int [] directoryIDs = new int [] {R.string.appointments_directory,
										  R.string.audio_directory,
										  R.string.carer_photos_directory,
										  R.string.dialogue_directory,
										  R.string.documentation_directory,
										  R.string.epg_directory,
										  R.string.help_directory,
										  R.string.noises_directory,
										  R.string.photos_directory,
										  R.string.photos_notes_directory,
										  R.string.schedules_direct_folder,
										  R.string.screen_capture_directory,
										  R.string.track_directory,
										  R.string.tracks_directory,
										  R.string.video_directory,
										  R.string.web_directory};
		// --------------------------------------------------------------------------
		// 01/05/2015 ECU CreateADirectory only creates a directory if it does not
		//                exist so there is no need to do that in this method
		// -------------------------------------------------------------------------
		if (CreateADirectory (theRootDirectory))
		{
			// ---------------------------------------------------------------------
			// 21/02/2014 ECU the root directory was created OK so lay out the sub-directories
			// ---------------------------------------------------------------------
			for (int theDirectory = 0; theDirectory < directoryIDs.length; theDirectory++)
			{
				if (!CreateADirectory (theRootDirectory + theContext.getString (directoryIDs [theDirectory])))
				{
					// ---------------------------------------------------------
					// 21/02/2014 ECU unable to create the directory so exit
					// ---------------------------------------------------------
					return false;
				}	
			}
			// -----------------------------------------------------------------
			// 21/02/2014 ECU everything seems OK
			// -----------------------------------------------------------------
			return true;
		}
		else
		{
			return false;
		}
	}
	/* ============================================================================= */
	public static boolean checkForNetwork (Context theContext)
	{
		// -------------------------------------------------------------------------
		//            ECU checks whether there is any access to the internet 
		// 03/02/2015 ECU just tidied up
		// -------------------------------------------------------------------------
		boolean haveConnectedWifi 	= false;
		boolean haveConnectedMobile = false;

		ConnectivityManager connectivityManager
			= (ConnectivityManager) theContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
		for (NetworkInfo ni : networkInfo) 
		{
		        if (ni.getTypeName().equalsIgnoreCase ("WIFI"))
		            if (ni.isConnected())
		                haveConnectedWifi = true;
		        if (ni.getTypeName().equalsIgnoreCase ("MOBILE"))
		            if (ni.isConnected())
		                haveConnectedMobile = true;
		}
		return haveConnectedWifi || haveConnectedMobile;
	}
	// =============================================================================
	public static long ConvertDate (int theDay,int theMonth,int theYear)
	{
		// -------------------------------------------------------------------------
		// 27/02/2017 ECU created to convert the specified date components into
		//                a single date which is return in milliSeconds
		// -------------------------------------------------------------------------
		Calendar localCalendar = Calendar.getInstance ();
		// -------------------------------------------------------------------------
		// 27/02/2017 ECU added the 'clear' to reset all fields
		// -------------------------------------------------------------------------
		localCalendar.clear ();
		// -------------------------------------------------------------------------
		localCalendar.set (theYear, theMonth, theDay);
		// -------------------------------------------------------------------------
		return localCalendar.getTimeInMillis();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static long ConvertTime (int theHour, int theMinute)
	{
		// -------------------------------------------------------------------------
		// 13/01/2014 ECU created to return the milliseconds for a given hour and minute. When the
		//                calculation (hour * 60 * 60 * 1000) + (minute * 60 *1000) was converted
		//                into printable form using SimpleDateFormat then there was a 1 hour 
		//                difference
		// -------------------------------------------------------------------------
		Calendar localCalendar = Calendar.getInstance ();
		// -------------------------------------------------------------------------
		// 25/11/2015 ECU added the 'clear' to reset all fields
		// -------------------------------------------------------------------------
		localCalendar.clear ();
		// -------------------------------------------------------------------------
		localCalendar.set (0, 0, 0, theHour, theMinute, 0);
		// -------------------------------------------------------------------------
		return localCalendar.getTimeInMillis();
	}
	/* ============================================================================= */
	public static boolean CreateADirectory (String theDirectoryName)
	{
		// -------------------------------------------------------------------------
		// 21/02/2014 ECU create the named directory and indicate if the operation 
		//                was a success
		// -------------------------------------------------------------------------
		File directory = new File(theDirectoryName);
		// ------------------------------------------------------------------------- 
		// 21/02/2014 Check if the directory exists (i.e. is not a file)
		// ------------------------------------------------------------------------- 
		if (!directory.exists()) 
		{
			// ---------------------------------------------------------------------
			// 21/02/2014 ECU the named directory does not exist so create it
			//            ECU use 'mkdir' because do not want to automatically
			//                create parent directories.
			// ---------------------------------------------------------------------
			return directory.mkdir ();
		}
		else
		{
			// ---------------------------------------------------------------------
			// 21/02/2014 ECU the directory already exists so treat as a
			//                successful creation
			// --------------------------------------------------------------------- 
			return true;
		}		 
	}
	// =============================================================================
	public static void dateTimeDisplay (TextView theTextView,
										String theDateTimeFormat,
										int theInitialDelay,
										int theUpdateInterval,
										boolean theTimeAdjustment)
	{
		// -------------------------------------------------------------------------
		// 21/01/2017 ECU created to display the current date and time in the specified
		//                text view using the specified format. The display is then
		//                updated after the interval which is specified in mS
		// 22/01/2017 ECU added the time adjustment
		// 25/01/2017 ECU added theInitialDelay
		// --------------------------------------------------------------------------
		// 20/08/2016 ECU want to start the display of the timer
		// 25/01/2017 ECU changed to a delayed send to give use chance to read
		//                original text before it is changed
		// ---------------------------------------------------------------------
		Message localMessage = PublicData.messageHandler.obtainMessage (StaticData.MESSAGE_DISPLAY_DATE_START,
									new DateTimeTextView (theTextView,theDateTimeFormat,theUpdateInterval,theTimeAdjustment));
		PublicData.messageHandler.sendMessageDelayed (localMessage,theInitialDelay);
		// --------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void DisplayADrawable (Context theContext,int theDrawableID)
	{
		// -------------------------------------------------------------------------
		// 18/02/2014 ECU created - this calls the activity which will display the specified
		//                drawable in such a way that it overlays the existing screen for
		//                a fixed period of time before removing it. The time is configurable
		//                and can be disabled if required
		//            ECU added the ..NEW_TASK flag
		// -------------------------------------------------------------------------
		Intent intent = new Intent (theContext,DisplayDrawableActivity.class);
		intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra (StaticData.PARAMETER_HELP_ID,theDrawableID);
		theContext.startActivity (intent);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void DisplayADrawable (Context theContext,int theDrawableID,int theWaitTime,boolean theFlingFlag)
	{
		// -------------------------------------------------------------------------
		// 18/02/2014 ECU created - an intermediate method to set all flags when no help text
		//                is required
		// -------------------------------------------------------------------------
		
		DisplayADrawable (theContext,theDrawableID,theWaitTime,theFlingFlag,null);
	}
	// -----------------------------------------------------------------------------
	public static void DisplayADrawable (Context theContext,int theDrawableID,int theWaitTime,boolean theFlingFlag,boolean theScaleFlag)
	{
		// -------------------------------------------------------------------------
		// 09/08/2016 ECU created - an intermediate method to set all flags when no help text
		//                is required and to add the scale flag
		// 11/10/2016 ECU added the NO_RESULT
		// -------------------------------------------------------------------------	
		DisplayADrawable (theContext,theDrawableID,theWaitTime,theFlingFlag,null,StaticData.NO_RESULT,theScaleFlag);
	}
	/* ----------------------------------------------------------------------------- */
	public static void DisplayADrawable (Context theContext,
			                             int theDrawableID,
			                             int theWaitTime,
			                             boolean theFlingFlag,
			                             String theHelpText,
			                             int theHelpTextLayout,
			                             boolean theScaleFlag)
	{
		// -------------------------------------------------------------------------
		// 18/02/2014 ECU created - this calls the activity which will display the specified
		//                drawable in such a way that it overlays the existing screen for
		//                a fixed period of time before removing it. The time is configurable
		//                and can be disabled if required.
		//                Also feed through the wait time and fling flag
		//            ECU added the ..NEW_TASK flag
		// 09/08/2016 ECU added theScaleFlag
		// 09/10/2016 ECU changed from R.drawable.grid_help to theDrawableID
		// 11/10/2016 ECU added the help text layout
		// -------------------------------------------------------------------------	
		Intent intent = new Intent (theContext,DisplayDrawableActivity.class);
		intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra (StaticData.PARAMETER_WAIT_TIME,theWaitTime);
		intent.putExtra (StaticData.PARAMETER_FLING_ENABLED,theFlingFlag);
	    intent.putExtra (StaticData.PARAMETER_HELP_ID,theDrawableID);	
	    if (theHelpText != null)
	    	intent.putExtra (StaticData.PARAMETER_HELP_TEXT,theHelpText);
	    if (theHelpTextLayout != StaticData.NO_RESULT)
	    	intent.putExtra (StaticData.PARAMETER_LAYOUT,theHelpTextLayout);
	    intent.putExtra (StaticData.PARAMETER_SCALE,theScaleFlag);
	    theContext.startActivity (intent);
	    // -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void DisplayADrawable (Context theContext,
            							 int theDrawableID,
            							 int theWaitTime,
            							 boolean theFlingFlag,
            							 String theHelpText)
	{
		// -------------------------------------------------------------------------
		// 09/08/2016 ECU this was the old mast method until 'theScaleFlag' was added
		// 11/10/2016 ECU added the NO_RESULT
		// -------------------------------------------------------------------------
		DisplayADrawable (theContext,theDrawableID,theWaitTime,theFlingFlag,theHelpText,StaticData.NO_RESULT,false);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void DisplayADrawable (Context theContext,
										 int theDrawableID,
										 int theWaitTime,
										 boolean theFlingFlag,
										 String theHelpText,
										 int theHelpTextLayoutID)
	{
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU created to be able to specify the layout to be used for
		//                the layout of help text
		// -------------------------------------------------------------------------
		DisplayADrawable (theContext,theDrawableID,theWaitTime,theFlingFlag,theHelpText,theHelpTextLayoutID,false);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void displayAnImage (ImageView theImageView,
									   String theImagePath,
									   int theSampleSize,
									   int theDefaultResource)
	{
		BitmapFactory.Options myOptions = new BitmapFactory.Options(); 
		// -------------------------------------------------------------------------
		// 17/01/2014 ECU added the method with theDefaultResource which will be 
		//                displayed if the specified file cannot be found
		// -------------------------------------------------------------------------
		// 02/06/2013 ECU changed the sample size to be a parameter 
		// -------------------------------------------------------------------------
		myOptions.inSampleSize = theSampleSize;  
		// -------------------------------------------------------------------------
		// 02/02/2014 ECU check whether the required image file exists
		// -------------------------------------------------------------------------
		File imageFile = new File (theImagePath);
		// -------------------------------------------------------------------------
		// 20/03/2014 ECU add the check that the imagePath must point to a file
		// -------------------------------------------------------------------------
		if (imageFile.exists() && imageFile.isFile())
		{
			Bitmap image = BitmapFactory.decodeFile(theImagePath,myOptions);
			// ---------------------------------------------------------------------
			// 17/01/2014 ECU check if the image exists (belt and braces)
			// ---------------------------------------------------------------------
			if (image != null)
				theImageView.setImageBitmap(image);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 17/01/2014 ECU the selected image does not exist so display a default
			//                image
			// ---------------------------------------------------------------------
			if (theDefaultResource != StaticData.NO_RESULT)
				theImageView.setImageResource (theDefaultResource);	
			// ---------------------------------------------------------------------
		}
	}
	/* ----------------------------------------------------------------------------- */
	public static void displayAnImage (ImageView theImageView,String theImagePath,int theSampleSize)
	{
		// -------------------------------------------------------------------------
		// 17/01/2014 ECU created this method following the change to the above method
		// -------------------------------------------------------------------------
		displayAnImage (theImageView,theImagePath,theSampleSize,StaticData.NO_RESULT);
	}
	/* ----------------------------------------------------------------------------- */
	public static void displayAnImage (Context theContext,
			 						   ImageView theImageView, 
									   String theImagePath, 
									   String theSoundFile)
	{
		displayAnImage (theImageView,theImagePath);
		// -------------------------------------------------------------------------
		//           ECU check if want to play anything 
		// -------------------------------------------------------------------------
		if (new File(theSoundFile).exists())
		{
			PlayAFile (theContext,theSoundFile);	
		}
	}
	/* ----------------------------------------------------------------------------- */
	public static void displayAnImage (ImageView theImageView,String theImagePath)
	{
		// -------------------------------------------------------------------------
		// 02/06/2013 ECU created
		// 17/01/2014 ECU changed to use IMAGE_SAMPLE_SIZE
		// 01/09/2015 ECU changed to use StaticData
		// -------------------------------------------------------------------------
		displayAnImage (theImageView,theImagePath,StaticData.IMAGE_SAMPLE_SIZE);
	}
	/* ----------------------------------------------------------------------------- */
	public static void displayAnImage (Context theContext,ImageView theImageView, 
				String theImagePath, String theSoundFile,ImageView theSecondImageView)
	{
		// -------------------------------------------------------------------------
		// 25/06/2013 ECU this method added to allow the second image view to be updated
		// -------------------------------------------------------------------------
		displayAnImage (theContext,theImageView,theImagePath,theSoundFile);
		displayAnImage (theSecondImageView,theImagePath,2);
	}
	/* ============================================================================= */
	public static float distanceBetwwenTwoPoints (double firstLongitude,double firstLatitude,
												  double secondLongitude, double secondLatitude)
	{
		// -------------------------------------------------------------------------
		// 22/08/2013 ECU work out the shortest distance between two points
		// -------------------------------------------------------------------------
		Location firstLocation = new Location("first");

		firstLocation.setLatitude(firstLatitude);
		firstLocation.setLongitude(firstLongitude);

		Location secondLocation = new Location("second");

		secondLocation.setLatitude(secondLatitude);
		secondLocation.setLongitude(secondLongitude);
		// -------------------------------------------------------------------------
		// 22/08/2013 ECU return the distance between the two points in metres
		// -------------------------------------------------------------------------
		return firstLocation.distanceTo (secondLocation);
	}
	/* ============================================================================= */
	public static void displayMap (Context theContext,double theLatitude,double theLongitude)
	{
		// -------------------------------------------------------------------------
		// 23/10/2014 ECU called to activate Google maps to display the
		//                map for the specified coordinates
		// --------------------------------------------------------------------------
		String uri = String.format(Locale.ENGLISH, "geo:%f,%f?z=20", (float) theLatitude, (float) theLongitude);
		Intent localIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		theContext.startActivity(localIntent);
		// --------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void DoseSelect (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 25/03/2014 ECU created
		//			  ECU use _METHOD to pass through details of method to be
		//				  actioned when an item is selected	
		// 29/03/2014 ECU changed to use _OBJECT_TYPE
		// -------------------------------------------------------------------------
		medicationIndex = theIndex;
		//--------------------------------------------------------------------------
		Intent intent = new Intent (MainActivity.activity,Selector.class);
		intent.putExtra (StaticData.PARAMETER_OBJECT_TYPE,StaticData.OBJECT_DAYS);
		intent.putExtra (StaticData.PARAMETER_BACK_KEY,true);
		intent.putExtra (StaticData.PARAMETER_MEDICATION,theIndex);
		intent.putExtra (StaticData.PARAMETER_METHOD,new MethodDefinition<Utilities> (Utilities.class,"DoseTimeSelect"));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		MainActivity.activity.startActivity (intent);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void DoseTimeSelect (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 25/03/2014 ECU created to enable the doses on a particular day to be 
		//                displayed and manipulated
		// 29/03/2014 ECU changed to use _OBJECT_TYPE
		// -------------------------------------------------------------------------
		if (PublicData.medicationDetails.get (medicationIndex).DoseExists(theIndex))
		{
			Intent intent = new Intent (MainActivity.activity,Selector.class);
			intent.putExtra (StaticData.PARAMETER_OBJECT_TYPE,StaticData.OBJECT_DOSES);
			intent.putExtra (StaticData.PARAMETER_BACK_KEY,true);
			intent.putExtra (StaticData.PARAMETER_MEDICATION,medicationIndex);
			intent.putExtra (StaticData.PARAMETER_DOSE,theIndex);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			MainActivity.activity.startActivity (intent);
		}
		else
		{
			popToast ("There are no doses on the selected day",true);
		}
	}
	// =============================================================================
	public static void elapsedTimeDisplay (TextView theTextView,int theUpdateTime)
	{
		// -------------------------------------------------------------------------
		// 20/08/2016 ECU created to initiate the display of an elapsed timer
		//                in the TextView field
		//				      theTextView ...... where the time will be displayed
		//                    theUpdateTime .... the time between updates in milliseconds
		//                                       if set to NO_RESULT then cancel
		// -------------------------------------------------------------------------
		if (theUpdateTime != StaticData.NO_RESULT)
		{
			// ---------------------------------------------------------------------
			// 20/08/2016 ECU want to start the display of the timer
			// ---------------------------------------------------------------------
			Message localMessage = PublicData.messageHandler.obtainMessage(StaticData.MESSAGE_ELAPSED_TIME_START);
			localMessage.arg1 = theUpdateTime;
			localMessage.obj  = theTextView;
			PublicData.messageHandler.sendMessage (localMessage);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 20/08/2016 ECU want to remove the display
			// ---------------------------------------------------------------------
			PublicData.messageHandler.sendEmptyMessage (StaticData.MESSAGE_ELAPSED_TIME_STOP);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean emptyString (String theString)
	{
		// -------------------------------------------------------------------------
		// 11/10/2016 ECU created to check if the string exists and whether it has
		//                non-blank data
		//                    true ....... string exists and is not empty
		//                    false ...... string does not exist or is empty
		// -------------------------------------------------------------------------
		if (theString != null && !theString.trim().equalsIgnoreCase(""))
			return true;
		else
			return false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean fileHandler (String theFileName,boolean theActionWanted)
	{
		// -------------------------------------------------------------------------
		// 06/10/2015 ECU created to handle some specific file checks
		//                  theActionWanted  ..... true
		//                                             try and create an empty file
		//                                             with the specified name.
		//                                             returns
		//                                                true .... all OK
		//                                                false ... file already
		//                                                          exists or an
		//                                                          error occurred
		//                                   ..... false
		//                                             delete the specified file
		//                                             always returns true
		// -------------------------------------------------------------------------
		File localFile = new File (theFileName);
		if (theActionWanted)
		{
			// ---------------------------------------------------------------------
			// 06/10/2015 ECU try and create the specified file
			// ---------------------------------------------------------------------
			try 
			{
				// -----------------------------------------------------------------
				// 06/10/2015 ECU the 'create...' returns the correct values
				// -----------------------------------------------------------------
				return localFile.createNewFile();
				// -----------------------------------------------------------------
			} 
			catch (IOException theException) 
			{
				return false;
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 06/10/2015 ECU delete the specified file - it returns true if OK
			//                false otherwise
			// ---------------------------------------------------------------------
			return localFile.delete();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static List<FileOptions> fileList (File theFileName, final String theExtensionWanted)
    {
    	// -------------------------------------------------------------------------
    	// 14/12/2015 ECU created to return a list of files from the argument
    	// -------------------------------------------------------------------------
    	// -------------------------------------------------------------------------
    	// 05/12/2013 ECU get list of files in the specified directory
    	// -------------------------------------------------------------------------
    	File [] fileList;
    	// -------------------------------------------------------------------------
    	// 14/12/2015 ECU generate the filter
    	// -------------------------------------------------------------------------
    	FileFilter filter = new FileFilter()
    	{
    		@Override
    		public boolean accept (File pathName) 
    		{
    			if (pathName.isDirectory())
    				return true;
    			else
    			{
    				// -----------------------------------------------------------------
    				// 10/11/2014 ECU added the Locale to the method call
    				//            ECU changed to use Locale.getDefault instead of Locale.UK
    				// -----------------------------------------------------------------
    				return pathName.getName().toLowerCase (Locale.getDefault()).endsWith (theExtensionWanted);
    			}
    		}
    	};
    	// -------------------------------------------------------------------------
    	// 09/12/2013 ECU check if any filtering is required
    	// -------------------------------------------------------------------------
    	fileList = theFileName.listFiles ((theExtensionWanted == null) ? null : filter); 
    	// -------------------------------------------------------------------------
    	// 05/12/2013 ECU set up the list of files and directories
    	// -------------------------------------------------------------------------
    	List<FileOptions> directories 	= new ArrayList<FileOptions>();
    	List<FileOptions> files 		= new ArrayList<FileOptions>();
    	// -------------------------------------------------------------------------
    	// 05/12/2013 ECU build up the lists
    	// -------------------------------------------------------------------------
		try
		{
			for (File fileIndex : fileList)
			{
				if (fileIndex.isDirectory())
				{
					// -------------------------------------------------------------
					// 05/12/2013 ECU file name is of directory so add to that list
					// -------------------------------------------------------------
					directories.add (new FileOptions(fileIndex));
				}
				else
				{
					// -------------------------------------------------------------
					// 05/12/2013 ECU file name is of a file so add to that list
					// -------------------------------------------------------------
					files.add (new FileOptions(fileIndex));
				}
			 }
		 }
		 catch(Exception theException)
		 {			 
		 }
		 // ------------------------------------------------------------------------
		 // 05/12/2013 ECU sort the lists into ascending order
		 // ------------------------------------------------------------------------
		 Collections.sort (directories);
		 Collections.sort (files);
		 // ------------------------------------------------------------------------
		 // 05/12/2013 ECU add the lists together
		 // ------------------------------------------------------------------------
		 directories.addAll (files);
		 // ------------------------------------------------------------------------
		 // 09/12/2013 ECU check if there is a parent
		 // ------------------------------------------------------------------------
		 if (theFileName.getParent() != null)
			 directories.add (0,new FileOptions(new File (theFileName.getParent()),true));	
		 // ------------------------------------------------------------------------
		 // 14/12/2015 ECU return the list of file names
		 // ------------------------------------------------------------------------
		 return directories;
		 // -------------------------------------------------------------------------
    }
	// =============================================================================
	public static String findPhoneServer ()
	{
		String thePhoneServer = null;
		// -------------------------------------------------------------------------
		// 26/07/2013 ECU added - search stored devices to find a phone server
		// 22/03/2015 ECU changed the logic to accommodate the redefination of
		//                deviceDetails to List<Devices>
		// -------------------------------------------------------------------------
		for (int theDevice = 0; theDevice < PublicData.deviceDetails.size(); theDevice++)
		{
			if (PublicData.deviceDetails.get(theDevice).phone)
			{
				thePhoneServer = PublicData.deviceDetails.get(theDevice).IPAddress;
				break;
			}
		}
		// -------------------------------------------------------------------------
		return thePhoneServer;
	}
	// =============================================================================
	public static String findRemoteControllerServer ()
	{
		String theRemoteControllerServer = null;
		// -------------------------------------------------------------------------
		// 26/02/2016 ECU added - search stored devices to find a remote controller server
		// -------------------------------------------------------------------------
		for (int theDevice = 0; theDevice < PublicData.deviceDetails.size(); theDevice++)
		{
			if (PublicData.deviceDetails.get(theDevice).remoteController)
			{
				theRemoteControllerServer = PublicData.deviceDetails.get(theDevice).IPAddress;
				break;
			}
		}
		// -------------------------------------------------------------------------
		return theRemoteControllerServer;
	}
	// =============================================================================
	public static String findWeMoServer ()
	{
		String theWeMoServer = null;
		// -------------------------------------------------------------------------
		// 18/03/2015 ECU added - search stored devices to find a Belkin WeMo server
		// 22/03/2015 ECU changed the logic to accommodate the redefination of
		//                deviceDetails to List<Devices>
		// -------------------------------------------------------------------------
		for (int theDevice = 0; theDevice < PublicData.deviceDetails.size(); theDevice++)
		{
			if (PublicData.deviceDetails.get(theDevice).wemo)
			{
				theWeMoServer = PublicData.deviceDetails.get(theDevice).IPAddress;
				break;
			}
		}
		// -------------------------------------------------------------------------
		return theWeMoServer;
	}
	// =============================================================================
	public static void FinishAndRestartApp (Context theContext,boolean theForceWriteFlag)
	{
		// -------------------------------------------------------------
		// 11/10/2016 ECU created to 'finish' the calling activity and
		//                then restart the app
		// -------------------------------------------------------------
		PublicData.writeDataOnDestroyForced = theForceWriteFlag;
		// -------------------------------------------------------------
		// 11/10/2016 ECU try and restart the app
		// -------------------------------------------------------------
		((Activity) theContext).setResult (StaticData.RESULT_CODE_FINISH);	
		// -------------------------------------------------------------
		// 11/10/2016 ECU just finish this activity
		// -------------------------------------------------------------
		((Activity) theContext).finish ();
		// -------------------------------------------------------------
	}
	/* ============================================================================== */
	public static String formatIPAddress (int theIPAddress)
	{
		// --------------------------------------------------------------------------
		// 12/11/2013 ECU added to convert an address from DHCP info into a
		//                string format
		// --------------------------------------------------------------------------
		return   (theIPAddress & 0xff) + "." +
	            ((theIPAddress >>>= 8) & 0xff) + "." +
	            ((theIPAddress >>>= 8) & 0xff) + "." +
	            ((theIPAddress>>>= 8) & 0xff);
	}
	// =============================================================================
	public static String getAdjustedTime (SimpleDateFormat theDateFormat)
	{
		// -------------------------------------------------------------------------
		// 25/02/2014 ECU created to be able to supply the format in which
		//                time is displayed
		// -------------------------------------------------------------------------
		
		return theDateFormat.format (getAdjustedTime(true));
	}
	/* ----------------------------------------------------------------------------- */
	public static String getAdjustedTime ()
	{
		// -------------------------------------------------------------------------
		// 13/08/2013 ECU will return the current, adjusted to NTP time, in printable format
		// 10/01/2014 ECU changed to have two separate methods
		// 13/08/2013 ECU return the adjust time in a printable form
		// -------------------------------------------------------------------------
		return PublicData.dateFormatter.format (getAdjustedTime(true));
	}
	/* ----------------------------------------------------------------------------- */
	public static long getAdjustedTime (boolean theAdjustmentFlag)
	{
		// -------------------------------------------------------------------------
		// 10/01/2014 ECU create to return the current date/time in milliseconds
		//                theFlag is there just to make it a unique method
		// 04/10/2015 ECU changed the meaning of the flag
		//                  true .... adjust the time
		//                  false ... do not adjust the time
		// -------------------------------------------------------------------------
		Calendar calendar = Calendar.getInstance();
		// -------------------------------------------------------------------------
		return (calendar.getTimeInMillis() - (theAdjustmentFlag ? PublicData.currentTimeAdjustment : 0l));
	}
	// =============================================================================
	public static String getAUniqueFileName (String theHeader)
	{
		// -------------------------------------------------------------------------
		// 23/10/2016 ECU created to generate a unique file name from the date and
		//                time and starting with the header
		// -------------------------------------------------------------------------
		return theHeader + (new SimpleDateFormat ("ddMMMyyyyHHmmss",Locale.getDefault())).format(getAdjustedTime(true));
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static String GetAPhrase (Activity theActivity,TextToSpeech theTextToSpeech)
	{
		// -------------------------------------------------------------------------
		// 21/11/2013 ECU created to get a voice input
		// -------------------------------------------------------------------------
		Utilities.startVoiceRecognitionActivity (theActivity,theTextToSpeech);
		// -------------------------------------------------------------------------
		return "got a phrase";
	}
	// =============================================================================
	public static float getBatteryLevel (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 03/02/2015 ECU created to return the battery level
		// -------------------------------------------------------------------------
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = theContext.registerReceiver (null,intentFilter);
		// -------------------------------------------------------------------------
		int level = batteryStatus.getIntExtra (BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra (BatteryManager.EXTRA_SCALE, -1);
		// -------------------------------------------------------------------------
		// 03/02/2015 ECU return the battery level as a percentage
		// -------------------------------------------------------------------------
		return ((float) level / (float) scale) * 100.0f;
	}
	// =============================================================================
	public static String getDayOfWeek (String theDateString)
	{
		// -------------------------------------------------------------------------
		// 26/09/2015 ECU created to return the weekday in English corresponding
		//                to the day string - supplied as 'dd/mm/yyyy' - assume
		//                string is valid so do no validation here.
		// -------------------------------------------------------------------------
		String [] fields = theDateString.split("[/]");
		// -------------------------------------------------------------------------
		// 26/09/2015 ECU at this point
		//					fields [0] ............ day
		//                  fields [1] ............ month
		//                  fields [2] ............ year
		// -------------------------------------------------------------------------
		// 26/09/2015 ECU the month will be one larger than that used by Java
		//                e.g. September will be '9' from the string but Java
		//                sees this as '8' because January = 0 (not 1) - hope
		//                that makes sense
		// -------------------------------------------------------------------------
		// 26/09/2015 ECU use Calendar to do the work
		// -------------------------------------------------------------------------
		Calendar localCalendar = Calendar.getInstance ();
		
		localCalendar.set (Integer.parseInt(fields[2]), (Integer.parseInt(fields[1]) - 1), Integer.parseInt(fields[0]));
		// -------------------------------------------------------------------------
		// 26/09/2015 ECU now return the day of week in 'English'
		// -------------------------------------------------------------------------
		return (new SimpleDateFormat("EEEE",Locale.getDefault()).format(localCalendar.getTimeInMillis()));
	}
	// =============================================================================
	public static String getDeviceID ()
	{
		// -------------------------------------------------------------------------
		// 03/02/2015 ECU created to return an ID which should be unique to the
		//                device. This seems to be a complicated issue so this may
		//                not be 100 % reliable - but good enough for what is needed
		//                here
		// -------------------------------------------------------------------------
		return android.os.Build.SERIAL;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static String GetDeviceIPAddress (String theName)
	{
		String theIPAddress = null;
		// -------------------------------------------------------------------------
		// 06/08/2013 ECU try and find the specified device name
		// 22/03/2015 ECU changed the logic to accommodate the redefinition of
		//                deviceDetails to List<Devices>
		// -------------------------------------------------------------------------
		if (PublicData.deviceDetails != null)
		{
			for (int theDevice = 0; theDevice < PublicData.deviceDetails.size(); theDevice++)
			{
				if (PublicData.deviceDetails.get(theDevice).name.equalsIgnoreCase(theName))
				{
					// -------------------------------------------------------------
					// 06/08/2013 ECU have found the specified device so set the name
					// -------------------------------------------------------------
					theIPAddress = PublicData.deviceDetails.get(theDevice).IPAddress;
					break;
					// -------------------------------------------------------------
				}
			}
		}
		
		return theIPAddress;
	}
	/* ============================================================================= */
	public static String GetDeviceName (String theIPAddress)
	{
		// -------------------------------------------------------------------------
		// 08/01/2014 ECU created to return the name of the device given the
		//                IP address
		// 22/03/2015 ECU changed the logic to accommodate the redefinition of
		//                deviceDetails to List<Devices>
		// -------------------------------------------------------------------------
		String theName = null;
		// -------------------------------------------------------------------------
		// 06/08/2013 ECU try and find the specified device name
		// -------------------------------------------------------------------------
		if (PublicData.deviceDetails != null)
		{
			for (int theDevice = 0; theDevice < PublicData.deviceDetails.size(); theDevice++)
			{
				if (PublicData.deviceDetails.get(theDevice).IPAddress.equalsIgnoreCase (theIPAddress))
				{
					// -------------------------------------------------------------
					// 06/08/2013 ECU have found the specified device so set the name
					// -------------------------------------------------------------
					theName = PublicData.deviceDetails.get (theDevice).name;
					// -------------------------------------------------------------
					break;
				}
			}
		}
		// -------------------------------------------------------------------------
		// 08/01/2014 ECU no device has been found - take different action if the
		//                the IP address is 'my' IP address
		// -------------------------------------------------------------------------
		if (theIPAddress.equalsIgnoreCase (PublicData.ipAddress))
		{
			if (theName == null || theName.equalsIgnoreCase (""))
			{
				theName = android.os.Build.MODEL;
			}
		}
		// -------------------------------------------------------------------------
		return theName;
	}
	/* ============================================================================= */
	public static void GetEmailDetails (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 03/02/2014 ECU took out the bit to read from disk
		// -------------------------------------------------------------------------
		if (PublicData.emailDetails == null)
		{
			// ---------------------------------------------------------------------
			// 04/01/2014 ECU no file so set from in-built values
			// ---------------------------------------------------------------------
			Intent localIntent = new Intent (theContext,EmailDetailsActivity.class);
			localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			theContext.startActivity (localIntent);
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	public static void GetPatientDetails (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 05/01/2014 ECU created
		// 03/02/2014 ECU took out the bit to read from disk
		// 21/02/2014 ECU changed so that instead of just showing a warning
		//                message, using popToast, the settings activity is
		//                started
		// -------------------------------------------------------------------------
		if (PublicData.patientDetails == null)
		{
			// ---------------------------------------------------------------------
			// 09/11/2015 ECU preset the patient details to make it easier in the
			//                settings activity
			// ---------------------------------------------------------------------
			PublicData.patientDetails = new PatientDetails ();
			// ---------------------------------------------------------------------
			// 21/02/2014 ECU no file activate settings to get values
			// ---------------------------------------------------------------------
			Intent myIntent = new Intent (theContext,SettingsActivity.class);
			myIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
			theContext.startActivity (myIntent);
			// ---------------------------------------------------------------------
			// 21/02/2014 ECU just put up a message to warn the user
			// 31/05/2016 ECU changed to use resource
			// ---------------------------------------------------------------------
			popToast (theContext.getString (R.string.patient_details_none));
		}
	}
	/* ================================================================================= */
	public static String getIPAddress(Context theContext)
	{
		// -----------------------------------------------------------------------------
		// 19/01/2015 ECU there seems to be a problem in version 5.0.2 (Lollipop) which
		//                means that the normal way of getting the subnet mask does not work
		//                so have to do things a different way.
		// -----------------------------------------------------------------------------
		String IPAddress;
		// -------------------------------------------------------------------------
		// 01/06/2013 ECU want to get the IP address for the wifi network
		// -------------------------------------------------------------------------
		WifiManager wifiManager = (WifiManager) theContext.getSystemService (WIFI_SERVICE);
		// -------------------------------------------------------------------------
		// 01/08/2013 ECU added networkMask
		// 12/11/2013 ECU changed to use local formatIPaddress method because
		//                Formatter.formatIpAddress(ipv4Address) has been
		//                deprecated
		// -------------------------------------------------------------------------		
		PublicData.networkMask = formatIPAddress (wifiManager.getDhcpInfo().netmask);
		// -------------------------------------------------------------------------
		// 19/01/2015 ECU there seems to be a problem with lollipop which seems to
		//                be returning 0 instead of the correct value
		// -------------------------------------------------------------------------	
		IPAddress =  formatIPAddress (wifiManager.getConnectionInfo().getIpAddress());
		// -------------------------------------------------------------------------
		// 19/01/2015 ECU now get the subnet mask as a string
		// -------------------------------------------------------------------------
		PublicData.networkMask 
			= APIIssues.SubnetMask (android.os.Build.VERSION.SDK_INT,IPAddress,PublicData.networkMask);
		// -------------------------------------------------------------------------
		return IPAddress;
	}
	/* ============================================================================= */
	public static String GetLogCatEntries (String theFilter)
	{
		// -------------------------------------------------------------------------
		// 04/01/2014 ECU created to return the current LogCat entries
		// 07/10/2015 ECU added 'theFilter' option
		// -------------------------------------------------------------------------
		// 04/02/2014 ECU declare builder to receive the entries
		// -------------------------------------------------------------------------
		StringBuilder logEntries = new StringBuilder ();
		
		try 
		{
			// ---------------------------------------------------------------------
			// 04/02/2014 ECU launch the process to get the entries
			//            ECU add '-v time' to get a timestamp on each entry
			//            ECU to show warnings add *:W, for errors *:E
			// 22/03/2014 ECU added the *:I option to try and remove debug
			//                messages - use LOGCAT_COMMAND
			// 01/09/2015 ECU changed to use StaticData
			// ---------------------------------------------------------------------
			Process process = Runtime.getRuntime().exec (StaticData.LOGCAT_COMMAND);
			// --------------------------------------------------------------------- 
			// 04/02/2014 ECU declare the reader for the process output
			// ---------------------------------------------------------------------
			BufferedReader bufferedReader 
				= new BufferedReader (new InputStreamReader (process.getInputStream()));
			// ---------------------------------------------------------------------
			String currentLine = "";
			// ---------------------------------------------------------------------
			// 18/03/2014 ECU loop for all entries in the log
			// ---------------------------------------------------------------------  
			while ((currentLine = bufferedReader.readLine()) != null) 
			{
				// -----------------------------------------------------------------
				// 04/02/2014 ECU add the current line into the current entries
				// 07/10/2015 ECU check on the filter
				// -----------------------------------------------------------------
				if ((theFilter == null) || (currentLine.contains(theFilter)))
						logEntries.append (currentLine + "\n");
				// -----------------------------------------------------------------
			}	  
		} 
		catch (IOException theException) 
		{
		}
		// -------------------------------------------------------------------------
		// 18/03/2014 ECU return the LogCat entries as a String to the caller
		// -------------------------------------------------------------------------
		return logEntries.toString();
	}
	/* ============================================================================= */
	public static String GetLogCatEntries (boolean theLatestFlag,String theFilter)
	{
		// -------------------------------------------------------------------------
		// 11/03/2014 ECU created to return the current LogCat entries with
		//                the most recent entry being presented first
		// 07/10/2015 ECU added 'theFilter' argument
		// -------------------------------------------------------------------------
		List<String> logEntries 		=  new ArrayList<String>();
		String		 logEntriesString 	= "";
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 04/02/2014 ECU launch the process to get the entries
			//            ECU add '-v time' to get a timestamp on each entry
			//            ECU to show warnings add *:W, for errors *:E
			// 22/03/2014 ECU use the command stored in LOGCAT_COMMAND
			// 01/09/2015 ECU changed to use StaticData
			// ---------------------------------------------------------------------
			Process process = Runtime.getRuntime().exec (StaticData.LOGCAT_COMMAND);
			// ---------------------------------------------------------------------
			// 04/02/2014 ECU declare the reader for the process output
			// ---------------------------------------------------------------------
			BufferedReader bufferedReader 
				= new BufferedReader(new InputStreamReader (process.getInputStream()));

			logEntriesString = "";
			  
			while ((logEntriesString = bufferedReader.readLine()) != null) 
			{
				// -----------------------------------------------------------------
				// 04/02/2014 ECU add the current line into the current entries - at 
				//                the head of the list
				// 07/10/2015 ECU check on the filter
				// -----------------------------------------------------------------
				if ((theFilter == null) || (logEntriesString.contains(theFilter)))
					logEntries.add (0,logEntriesString + "\n");
				// -----------------------------------------------------------------
			}	
			// ---------------------------------------------------------------------
			// 11/03/2014 ECU now build up the string to return
			// ---------------------------------------------------------------------
			logEntriesString = "";

			if (logEntries.size() > 0)
			{
				for (int theIndex = 0; theIndex < logEntries.size(); theIndex++)
				{
					logEntriesString += logEntries.get(theIndex);
				}
			}
			// ---------------------------------------------------------------------
		} 
		catch (IOException theException) 
		{
		}
		// -------------------------------------------------------------------------
		// 11/03/2014 ECU return the LogCat entries as a string - latest entry first
		// -------------------------------------------------------------------------
		return logEntriesString;
	}
	/* ============================================================================= */
	public static Intent getPackageIntent (Context theContext,int thePackageType)
	{
		// -------------------------------------------------------------------------
		// 09/11/2013 ECU get the intent for the required package
		// 15/02/2014 ECU really wanted to get the intent without the chooser - but
		//                looks as if need to go this root eventually
		// -------------------------------------------------------------------------
		Intent localIntent = null;
		
		switch (thePackageType)
		{
			// ---------------------------------------------------------------------
			case StaticData.PACKAGE_TYPE_CONTACTS:	
				// -----------------------------------------------------------------
				// 09/11/2913 ECU try and sort out the mail
				// 27/01/2016 ECU check the user supplied package name
				// -----------------------------------------------------------------
				if (PublicData.storedData.contactsPackageName != null)
				{
					localIntent = theContext.getPackageManager().getLaunchIntentForPackage (PublicData.storedData.contactsPackageName);
				}
				if (localIntent == null)
				{
					localIntent = theContext.getPackageManager().getLaunchIntentForPackage(theContext.getString(R.string.contacts_package_name_htc));
				}
				// -----------------------------------------------------------------
				// 08/11/2013 ECU if htc mail server not available then try android
				// -----------------------------------------------------------------
				if (localIntent == null)
				{
					// -------------------------------------------------------------
					// 08/11/2013 ECU try the alternative mail package name
					// -------------------------------------------------------------
					localIntent = theContext.getPackageManager().getLaunchIntentForPackage(theContext.getString(R.string.contacts_package_name_android));
				}
				// -----------------------------------------------------------------
				// 15/02/2014 ECU try the next location in android
				// -----------------------------------------------------------------
				if (localIntent == null)
				{
					// -------------------------------------------------------------
					// 15/02/2014 ECU try the alternative mail package name
					// -------------------------------------------------------------
					localIntent = theContext.getPackageManager().getLaunchIntentForPackage(theContext.getString(R.string.contacts_package_name_moto));
				}
				break;
			// ---------------------------------------------------------------------
			case StaticData.PACKAGE_TYPE_MAIL:
				
				// -----------------------------------------------------------------
				// 09/11/2913 ECU try and sort out the mail
				// 27/01/2016 ECU check the user supplied package name
				// -----------------------------------------------------------------
				if (PublicData.storedData.mailPackageName != null)
				{
					localIntent = theContext.getPackageManager().getLaunchIntentForPackage (PublicData.storedData.mailPackageName);
				}
				if (localIntent == null)
				{
					localIntent = theContext.getPackageManager().getLaunchIntentForPackage(theContext.getString(R.string.mail_package_name_htc));
				}
				// -----------------------------------------------------------------
				// 08/11/2013 ECU if htc mail server not available then try android
				// -----------------------------------------------------------------
				if (localIntent == null)
				{
					// -------------------------------------------------------------
					// 08/11/2013 ECU try the alternative mail package name
					// -------------------------------------------------------------
					localIntent = theContext.getPackageManager().getLaunchIntentForPackage(theContext.getString(R.string.mail_package_name_android));
				}
				// -----------------------------------------------------------------
				// 15/02/2014 ECU try the next location in android
				// -----------------------------------------------------------------
				if (localIntent == null)
				{
					// -------------------------------------------------------------
					// 15/02/2014 ECU try the alternative mail package name
					// -------------------------------------------------------------
					localIntent = theContext.getPackageManager().getLaunchIntentForPackage(theContext.getString(R.string.mail_package_name_moto));
				}
				
				break;
		}
		
		return localIntent;
	}
	/* ============================================================================= */
	public static void getTheLastUpdateTime (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 08/08/2013 ECU created - returns the time when this package was last updated
		// -------------------------------------------------------------------------
		PackageInfo packageInfo;
		// -------------------------------------------------------------------------
		try 
		{   
			// ---------------------------------------------------------------------
			// 15/09/2013 ECU added Locale.UK
			// 10/11/2014 ECU changed from Local.UK to Locale.getDefault ()
			// 27/10/2016 ECU changed to use dateSimpleFormat
			// ---------------------------------------------------------------------
			SimpleDateFormat timeFormatter = new SimpleDateFormat ("HH:mm:ss",Locale.getDefault()); 
			packageInfo = theContext.getPackageManager().getPackageInfo(theContext.getPackageName(), 0);
			// ---------------------------------------------------------------------
			// 09/03/2017 ECU Note - set the variables that are used throughout the app
			// ---------------------------------------------------------------------
			PublicData.lastUpdateDate = PublicData.dateSimpleFormat.format (packageInfo.lastUpdateTime);
			PublicData.lastUpdateTime = timeFormatter.format (packageInfo.lastUpdateTime);
			// ---------------------------------------------------------------------
		} 
		catch (NameNotFoundException theException) 
		{    	
		} 
	}
	// =============================================================================
	public static String getMACAddress (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 20/03/2015 ECU created to return the WiFi MAC address of this device
		// 27/07/2016 ECU as of the MARSHMALLOW version the normal code returns
		//                the 'fake' value of '02:00:00:00:00:00' so added the additional
		//                code - currently not using the MARSHMALLOW VERSION_CODES
		//                because the latest libraries have not been added
		// -------------------------------------------------------------------------
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) 
		{
			WifiManager manager = (WifiManager) theContext.getSystemService (WIFI_SERVICE);
			WifiInfo info = manager.getConnectionInfo();
			return info.getMacAddress();
		}
		else
		{
			try 
			{
				// -----------------------------------------------------------------
				// 27/07/2016 ECU get a list of interfaces
				// -----------------------------------------------------------------
				List<NetworkInterface> networkInterfaces = Collections.list (NetworkInterface.getNetworkInterfaces ());
				// -----------------------------------------------------------------
				// 27/07/2016 ECU loop through the interface looking for the
				//                wireless lan
				// -----------------------------------------------------------------
				for (NetworkInterface networkInterface : networkInterfaces)
				{
					if (networkInterface.getName().equalsIgnoreCase (StaticData.WIRELESS_INTERFACE))
					{
						// ---------------------------------------------------------
						// 27/07/2016 ECU now get the associated MAC address
						// ---------------------------------------------------------
						byte [] MACAddressBytes = networkInterface.getHardwareAddress ();
						// ---------------------------------------------------------
						// 27/07/2016 ECU check that one exists
						// ---------------------------------------------------------
						if (MACAddressBytes != null)
						{
							// -----------------------------------------------------
							// 27/07/2016 ECU now generate the correct format
							// -----------------------------------------------------
							StringBuffer returnMACAddress = new StringBuffer ();
							// -----------------------------------------------------
							// 27/07/2016 ECU loop through the bytes of the MAC address
							// -----------------------------------------------------
							for (byte byteMAC : MACAddressBytes)
							{
								returnMACAddress.append (String.format ("%02X:",byteMAC));
							}
							// -----------------------------------------------------
							// 27/07/2016 ECU there will be a trailing ':' to be removed
							// -----------------------------------------------------
							if (returnMACAddress.length() > 0)
							{
								returnMACAddress.deleteCharAt (returnMACAddress.length() - 1);
							}
							// -----------------------------------------------------
							// 27/07/2016 ECU now return the generated address
							// -----------------------------------------------------
							return returnMACAddress.toString ();
							// -----------------------------------------------------
						}
					}
				}
				// ------------------------------------------------------------------
			}
			catch (Exception theException)
			{
				
			}
			// ---------------------------------------------------------------------
			// 27/07/2016 ECU if get here then it was not possible to get the MAC
			//                address
			// ---------------------------------------------------------------------
			return StaticData.FAKE_MAC_ADDRESS;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static int getMessageType (Context theContext,Socket theSocket,boolean theAcknowledgeFlag)
	{
		int localResult = 0;
		
		try
		{	
			// ---------------------------------------------------------------------
			// 02/08/2013 ECU set up the input streams for normal and the object
			// ---------------------------------------------------------------------
			InputStream input = theSocket.getInputStream();
			localResult = input.read ();				
			input.close();
			// ---------------------------------------------------------------------
			// 02/08/2013 ECU check if need to acknowledge the message
			// ---------------------------------------------------------------------
			if (theAcknowledgeFlag)
			{
				// -----------------------------------------------------------------
				// 02/03/2013 ECU just want to acknowledge the message
				// -----------------------------------------------------------------
				OutputStream output = theSocket.getOutputStream();
				output.write (0);
				output.flush();
				output.close();
			}
		}
		catch (IOException theException)
		{	
		} 
		
		return localResult;	
	}
	/* ============================================================================= */
	public static String getPhoneNumber (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 24/07/2013 ECU add telephony information
		//                the returned string is null if there is no phone on the
		//                device or the phone is not available, i.e. if in airplane mode
		// 23/08/2013 ECU on the Nexus 7 (without a phone) the getLineNumber is returning a ""
		//				  rather than a null so adjust the logic a bit
		// -------------------------------------------------------------------------
		TelephonyManager telephonyManager = (TelephonyManager) theContext.getSystemService(Context.TELEPHONY_SERVICE);
				
		String phoneNumber = telephonyManager.getLine1Number();
		// -------------------------------------------------------------------------
		// 23/08/2013 ECU check for a "" string and return a null if it occurs
		// -------------------------------------------------------------------------
		if (phoneNumber == null || phoneNumber.length() == 0)
			return null;
		else
			return phoneNumber;
	}
	/* ============================================================================= */
	public static int [][] getNetworkAddress (String theIPAddress,String theSubnetMask)
	{
		// -------------------------------------------------------------------------
		// 12/11/2013 ECU this method returns (as octets) the network part
		//                of this address
		// -------------------------------------------------------------------------
		String [] addressOctets = theIPAddress.split("[.]");
		String [] maskOctets    = theSubnetMask.split("[.]");
		
		int [][] networkOctets = new int [2][addressOctets.length];
		
		for (int theIndex = 0; theIndex < networkOctets[0].length; theIndex++)
		{
			networkOctets [0][theIndex] = Integer.parseInt(addressOctets[theIndex]) & Integer.parseInt(maskOctets[theIndex]);
			networkOctets [1][theIndex] = Integer.parseInt(maskOctets[theIndex]);
		}
		// -------------------------------------------------------------------------
		// 12/11/2013 ECU return with the octets for the network
		// -------------------------------------------------------------------------
		return networkOctets;
	}
	// =============================================================================
	public static int getRandomNumber (int theRange)
	{
		// -------------------------------------------------------------------------
		// 01/10/2015 ECU created to return a random number in the range 0 -> (theRange)
		// -------------------------------------------------------------------------
		Random   random 			= new Random ();
		// -------------------------------------------------------------------------
		return random.nextInt (theRange);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String getRelativeFileName (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 16/11/2016 ECU created to return the file name relative to the project
		//                folder
		// -------------------------------------------------------------------------
		return theFileName.replaceFirst (PublicData.projectFolder,"");
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String getSocketMessage (Context theContext,Socket theSocket)
	{
		String  theIncomingMessage = "";
		String  theWantedString = null;
		
		try 
		{                                         
			BufferedReader input = new BufferedReader(new InputStreamReader(theSocket.getInputStream()));  
			// ---------------------------------------------------------------------
			// 26/07/2013 ECU change the way that all of the data is read in
			// ---------------------------------------------------------------------
			while (!theIncomingMessage.startsWith("ready"))
			{
				theIncomingMessage = input.readLine();
				
				if (theIncomingMessage.startsWith("="))
				{
					theWantedString = theIncomingMessage.replace("=","");
					break;
				}
			}	
		} 
		catch (Exception theException) 
		{                   
			PublicData.datagram.Message("getSocketMessage Exception  : " + theException);             
		}
		return theWantedString;
 	}
	// =============================================================================
	public static long getTime (String theDate,String theTime)
	{
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU created to return the date (as long) from the arguments
		//                which are supplied in the following format
		//                    theDate ....... dd/mm/yyyy
		//                    theTime ....... hh:mm
		// -------------------------------------------------------------------------
		String [] dateFields = theDate.split ("[/]");
		String [] timeFields = theTime.split ("[:]");
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU at this point
		//					dateFields [0] ............ day
		//                  dateFields [1] ............ month
		//                  dateFields [2] ............ year
		//
		//                  timeFields [0] ............ hours
		//                  timeFields [1] ............ minutes
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU the month will be one larger than that used by Java
		//                e.g. September will be '9' from the string but Java
		//                sees this as '8' because January = 0 (not 1) - hope
		//                that makes sense
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU use Calendar to do the work
		//            ECU final '0' is to indicate 0 seconds
		// 01/10/2015 ECU added the 'clear' command
		// -------------------------------------------------------------------------
		Calendar localCalendar = Calendar.getInstance ();
		
		localCalendar.clear();
			
		localCalendar.set (Integer.parseInt(dateFields[2]), (Integer.parseInt(dateFields[1]) - 1), Integer.parseInt(dateFields[0]),
						   Integer.parseInt(timeFields[0]),  Integer.parseInt(timeFields[1]),0);
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU now return the date in milliseconds
		// -------------------------------------------------------------------------
		return (localCalendar.getTimeInMillis());
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static long getTime (String theDate)
	{
		// -------------------------------------------------------------------------
		// 20/02/2016 ECU created to return time when no time is required
		// -------------------------------------------------------------------------
		return getTime (theDate,"00:00");
		// -------------------------------------------------------------------------
	} 
	// =============================================================================
	public static Bitmap getViewAsBitmap (View theView)
	{	
		// -------------------------------------------------------------------------
		// 13/04/2015 ECU created to return a 'screen shot' of the specified view
		// -------------------------------------------------------------------------
		theView.setDrawingCacheEnabled (true);
		return theView.getDrawingCache ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static Bitmap getWebImage (String theURL)
	{
		// -------------------------------------------------------------------------
		// 29/08/2016 ECU created to retrieve an image as a bitmap from its URL
		// -------------------------------------------------------------------------
		Bitmap localBitMap = null;
		try 
		{
			InputStream inputStream = new java.net.URL(theURL).openStream();
			localBitMap = BitmapFactory.decodeStream (inputStream);
         }
		catch (Exception theException) 
		{
		}
		// -------------------------------------------------------------------------
		// 28/08/2016 ECU return the retrieved bitmap or null
		// -------------------------------------------------------------------------
		return localBitMap;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static ArrayList<String> getWebPage (String theURL)
	{
		// -------------------------------------------------------------------------
		// 17/09/2015 ECU created to return the contents of the web page specified
		//                by the argument
		// 18/09/2015 ECU moved into this class from TVChannelsActivity
		// -------------------------------------------------------------------------
		ArrayList <String> localContents = new ArrayList<String> ();
		// -------------------------------------------------------------------------
		try
		{
			HttpClient localClient = new DefaultHttpClient ();
			HttpGet localRequest = new HttpGet (theURL);
			HttpResponse localResponse = localClient.execute (localRequest);
			
			InputStream inputStream = localResponse.getEntity().getContent();
			BufferedReader reader = new BufferedReader (new InputStreamReader (inputStream));
			
			String inputLine = null;
			
			while ((inputLine = reader.readLine ()) != null)
			{
				localContents.add (inputLine);
			}
			
			inputStream.close();
			// ---------------------------------------------------------------------
			// 18/09/2015 ECU return the information that has been read
			// ---------------------------------------------------------------------
			return localContents;
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{	
			// ---------------------------------------------------------------------
			// 18/09/2015 ECU indicate that an error occurred
			// ---------------------------------------------------------------------
			return null;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String getWebPageAsString (String theURL)
	{
		// -------------------------------------------------------------------------
		// 05/09/2016 ECU created to return the chosen web page as a string
		// 06/09/2016 ECU changed method name
		// -------------------------------------------------------------------------
		StringBuilder localContents = new StringBuilder ();
		try
		{
			HttpClient localClient = new DefaultHttpClient ();
			HttpGet localRequest = new HttpGet (theURL);
			HttpResponse localResponse = localClient.execute (localRequest);
			
			InputStream inputStream = localResponse.getEntity().getContent();
			BufferedReader reader = new BufferedReader (new InputStreamReader (inputStream));
			
			String inputLine = null;
			
			while ((inputLine = reader.readLine ()) != null)
			{
				localContents.append (inputLine + "\n");
			}
			
			inputStream.close();
			// ---------------------------------------------------------------------
			// 18/09/2015 ECU return the information that has been read
			// ---------------------------------------------------------------------
			return localContents.toString();
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{	
			// ---------------------------------------------------------------------
			// 18/09/2015 ECU indicate that an error occurred
			// ---------------------------------------------------------------------
			return null;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------	
	}
	// =============================================================================
	public static boolean gridHelp (Context theContext,int theDrawableId,boolean theCheck)
	{
		// -------------------------------------------------------------------------
		// 12/10/2014 ECU called to check if a help file exists
		// 14/10/2014 ECU change to use the static variable
		// -------------------------------------------------------------------------
		if (theCheck)
		{	
			// ---------------------------------------------------------------------
			// 12/10/2014 ECU returns 'true' if the file exists, 'false' if it doesn't
			// ---------------------------------------------------------------------
			return new File (GridActivity.gridHelpFileHeader + 
					theContext.getResources().getResourceEntryName(theDrawableId)).exists();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 12/10/2014 ECU always return that file exists
			// ---------------------------------------------------------------------
			return true;
		}
	}
	// -----------------------------------------------------------------------------
	public static void gridHelp (Context theContext,int theDrawableId)
	{
		// -------------------------------------------------------------------------
		// 11/09/2013 ECU created - called to try and display some help for
		//                icons displayed in the grid
		// 19/01/2013 ECU changed to use theDrawableId
		// 14/10/2014 ECU changed to use the static variable gridHelpFileHeader
		// -------------------------------------------------------------------------
		String theHelpFileName = GridActivity.gridHelpFileHeader + 
				theContext.getResources().getResourceEntryName (theDrawableId);
		// -------------------------------------------------------------------------
		byte [] theBytes  = readAFile (theHelpFileName);
		// -------------------------------------------------------------------------
		// 11/09/2013 ECU check if the specified file exists
		// -------------------------------------------------------------------------
		if (theBytes != null)
		{
			// ---------------------------------------------------------------------
			// 11/09/2013 ECU display the contents of the file as the displayed
			//				  help screen
			// 13/02/2014 ECU put up a message about help options
			// 18/02/2014 ECU use the method
			// ---------------------------------------------------------------------
			DisplayADrawable (theContext,R.drawable.help,0,false,new String (theBytes));
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 11/09/2013 ECU there is no help file for this grid command
			// 14/10/2014 ECU and speech option
			// 31/05/2016 ECU changed to use a resource
			// 16/08/2016 ECU add final 'rue' to centre the text
			// ---------------------------------------------------------------------
			popToastAndSpeak (theContext.getString (R.string.help_not_available),true);
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	public static void HandleFileDetails (Context theContext, 
									      FileDetails theFileDetails,
									      String theSender)
	{
		// -------------------------------------------------------------------------
		// 02/02/2014 ECU handle any received 'File' object
		// -------------------------------------------------------------------------
		// 02/02/2014 ECU because the storage may be different between devices then
		//                adjust the project folder
		// -------------------------------------------------------------------------
		String localFileName = PublicData.projectFolder + theFileDetails.fileName;
		// -------------------------------------------------------------------------
		// 20/03/2014 ECU log details of the request
		// -------------------------------------------------------------------------
		LogToProjectFile ("HandleFileDetails",theFileDetails.fileName);
		// -------------------------------------------------------------------------
		// 02/02/2014 ECU now check whether action needs to be taken on received details
		// -------------------------------------------------------------------------
		File localFile = new File (localFileName);
		// -------------------------------------------------------------------------
		// 02/02/2014 ECU action needs to be taken if :-
		//                 1) the file does not exist
		//                 2) the modification date is older then stored file
		//
		//                 NOTE - it may be that the receiving device has a more up
		//                 ====   to date version than that being sent so need to 
		//                        indicate this fact
		// 31/01/2015 ECU changed the method name
		// 21/03/2015 ECU pass the message type as an argument
		// -------------------------------------------------------------------------
		if (!localFile.exists() || theFileDetails.lastModified > localFile.lastModified())
		{
			// ---------------------------------------------------------------------
			// 29/09/2016 ECU Note - the file does not exists or the local file is
			//                older than the one being synchronised
			// ---------------------------------------------------------------------
			sendSocketMessageSendTheObject (theContext,
											theSender,
											PublicData.socketNumberForData, 
											StaticData.SOCKET_MESSAGE_REQUEST_FILE,
											(Object) theFileDetails);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/02/2014 ECU it would appear that the local copy is more up to date 
			//                than that being received
			//            ECU send my copy back to the sender
			// ----------------------------------------------------------------------
			HandleFileRequest (theContext,theFileDetails,theSender);
			// ----------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	public static void HandleFileRequest (Context theContext,
										  FileDetails theFileDetails,
										  String theSender)
	{
		// -------------------------------------------------------------------------
		// 20/03/2014 ECU log details of the request
		// 16/10/2014 ECU commented out the logging of details
		// -------------------------------------------------------------------------
		// LogToProjectFile ("HandleFileRequest",theFileDetails.fileName);
		// -------------------------------------------------------------------------		
		// 02/02/2014 ECU handle the request for a specific file
		// -------------------------------------------------------------------------
		theFileDetails.fileContents = readAFile (theFileDetails.GetFileName());
		
		if (theFileDetails.fileContents != null)
		{	
			// ---------------------------------------------------------------------
			// 31/01/2015 ECU changed the name of the method called
			// 21/03/2015 ECU pass the message type as an argument
			// ---------------------------------------------------------------------
			sendSocketMessageSendTheObject (theContext,
											theSender,
											PublicData.socketNumberForData, 
											StaticData.SOCKET_MESSAGE_REQUESTED_FILE,
											(Object) theFileDetails);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 14/12/2016 ECU the read file indicated an issue when accessing the
			//                file
			// ---------------------------------------------------------------------
			sendSocketMessageSendTheObject (theContext,
										    theSender,
										    PublicData.socketNumberForData, 
										    StaticData.SOCKET_MESSAGE_REQUESTED_FILE_ERROR,
										    (Object) theFileDetails);
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	public static void HandleRequestedFile (Context theContext,FileDetails theFileDetails,String theSender)
	{
		// --------------------------------------------------------------------------
		// 20/03/2014 ECU log details of the request
		// 16/10/2014 ECU commented out the logging
		// -------------------------------------------------------------------------
		// LogToProjectFile ("HandleRequestedFile",theFileDetails.fileName);
		// -------------------------------------------------------------------------
		// 06/04/2014 ECU if in cloning mode then pass the information
		// -------------------------------------------------------------------------
		if (PublicData.cloningInProgress)
		{
			CloneActivity.FileName (theFileDetails.fileName);
		}
		// -------------------------------------------------------------------------
		// 02/02/2014 ECU handle the receipt of a requested file
		// 19/03/2014 ECU include the sender's IP address as argument
		// -------------------------------------------------------------------------
		RandomAccessFile fileHandle = null;
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 03/02/2014 ECU get the file name
			// ---------------------------------------------------------------------
			String localFileName = theFileDetails.GetFileName();
			
			fileHandle = new RandomAccessFile (localFileName, "rw");
			// ---------------------------------------------------------------------
			// 03/08/2013 ECU make sure the file is empty
			// ---------------------------------------------------------------------	
			fileHandle.setLength (0);
			fileHandle.write (theFileDetails.fileContents,0,theFileDetails.fileContents.length);
			fileHandle.close ();	
			// ---------------------------------------------------------------------
			// 19/03/2014 ECU send an acknowledgement to the sender
			// ---------------------------------------------------------------------
			Utilities.sendDatagramType (theContext,theSender,StaticData.SOCKET_MESSAGE_FILE_ACK);		
			// ---------------------------------------------------------------------
			// 03/02/2014 ECU if this is a file being synchronised then want to make sure
			//                that it is not sent back
			// ---------------------------------------------------------------------
			if (PublicData.filesToSynchronise.size() > 0)
			{
				for (int theIndex = 0; theIndex < PublicData.filesToSynchronise.size(); theIndex++)
				{
					if (PublicData.filesToSynchronise.get(theIndex).fileName.equalsIgnoreCase (localFileName))
					{
						// ---------------------------------------------------------
						// 03/02/2014 ECU have found an entry in the list of files to synchronise
						// ---------------------------------------------------------
						FileToSynchronise fileToSynchronise = PublicData.filesToSynchronise.get(theIndex);
						
						File localFile = new File (localFileName);
						
						if (localFile != null)
						{
							// -----------------------------------------------------
							// 03/02/2014 ECU want to change the entry in the stored 
							//                record
							// -----------------------------------------------------
							fileToSynchronise.lastModified = localFile.lastModified();
							// -----------------------------------------------------
							PublicData.filesToSynchronise.set (theIndex, fileToSynchronise);
							// -----------------------------------------------------
							// 04/02/2014 ECU want to re-read the file into the data
							//                array - specify false' because do not 
							//                want to add into the synchronise list
							// 04/11/2016 ECU add the final 'true' to indicate that
							//                after being read from disk then the
							//                data is to be processed, e.g. processing
							//                the appointments data to generate associated
							//                alarms
							// -----------------------------------------------------
							ReadObjectFromDisk (theContext,fileToSynchronise.resourceID,false,true);
							// -----------------------------------------------------
							// 18/12/2015 ECU check whether there is a method that is
							//                to be actioned
							// -----------------------------------------------------
							if (PublicData.filesToSynchronise.get(theIndex).methodOnSynchronise != null)
							{
								// -------------------------------------------------
								// 18/12/2015 ECU there is a method to action
								// -------------------------------------------------
								try 
								{
									// --------------------------------------------
									// 18/12/2015 ECU invoke the defined method
									// -----------------------------------------------------
									PublicData.filesToSynchronise.get(theIndex).methodOnSynchronise.invoke (null, new Object [] {theContext});
									// -----------------------------------------------------
								}
								catch (Exception theException) 
								{
								}
								// -------------------------------------------------
							}
							// -----------------------------------------------------
						}
					}
				}
			}
		} 
		catch (Exception theException) 
		{
		}	
	}
	// =============================================================================
	public static void HandleRequestedFileError (Context theContext,FileDetails theFileDetails,String theSender)
	{
		// -------------------------------------------------------------------------
		// 14/12/2016 ECU created to handle errors reported by the cloner
		// -------------------------------------------------------------------------
		if (PublicData.cloningInProgress)
		{
			CloneActivity.FileName (theFileDetails.fileName,theContext.getString (R.string.cloner_error));
		}
		// -------------------------------------------------------------------------
		// 14/12/2016 ECU acknowledge the message so that cloning can continue
		// -------------------------------------------------------------------------
		Utilities.sendDatagramType (theContext,theSender,StaticData.SOCKET_MESSAGE_FILE_ACK);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static byte[] integerToByteArray(int theInteger)
	{
		// -------------------------------------------------------------------------
		// 09/08/2013 ECU create - converts an integer into a byte []
		//                paired with byteArrayToInteger
		// -------------------------------------------------------------------------
		return new byte[] {
	        (byte) ((theInteger >> 24) & 0xFF),
	        (byte) ((theInteger >> 16) & 0xFF),   
	        (byte) ((theInteger >> 8) & 0xFF),   
	        (byte) (theInteger & 0xFF)
	    };
	}
	/* ----------------------------------------------------------------------------- */
	public static void integerToByteArray (int theInteger, byte [] theBuffer, int theOffset)
	{
		// -------------------------------------------------------------------------
		// 09/08/2013 ECU method created
		// -------------------------------------------------------------------------
		theBuffer[theOffset]		= (byte) ((theInteger >> 24) & 0xFF);
		theBuffer[theOffset + 1]	= (byte) ((theInteger >> 16) & 0xFF);
		theBuffer[theOffset + 2]	= (byte) ((theInteger >> 8) & 0xFF);
		theBuffer[theOffset + 3]	= (byte) (theInteger & 0xFF);
	}
	// =============================================================================
	public static boolean isStringBlank (String theString)
	{
		// -------------------------------------------------------------------------
		// 13/06/2016 ECU created to check if a field is blank (true) or not (false)
		// -------------------------------------------------------------------------
		return !(theString.trim().length() > 0);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static boolean isNumberAPowerOfTwo (int theNumber)
	{
		// -------------------------------------------------------------------------
		// 24/10/2013 ECU indicates whether the supplied number is a power of
		//                2. Assumes that the input number is positive.
		// -------------------------------------------------------------------------
		return (theNumber != 0) && ((theNumber & (theNumber - 1)) == 0);
	}
	// =============================================================================
	public static boolean isServiceRunning(Context theContext,Class<?> serviceClass) 
	{
		// -------------------------------------------------------------------------
		// 06/10/2015 ECU created to indicate if a specified service is running
		// -------------------------------------------------------------------------
	    ActivityManager manager = (ActivityManager) theContext.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) 
	    {
	        if (serviceClass.getName().equals(service.service.getClassName())) 
	        {
	        	// -----------------------------------------------------------------
	        	// 06/10/2015 ECU found the required service
	        	// -----------------------------------------------------------------
	            return true;
	            // -----------------------------------------------------------------
	        }
	    }
	    // -------------------------------------------------------------------------
	    // 06/10/2015 ECU the service cannot be found
	    // -------------------------------------------------------------------------
	    return false;
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean isTheScreenOn (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 05/05/2015 ECU created to return whether the screen is on or not
		//                returns   true ......... screen is on
		//                          false ........ screen is off
		// -------------------------------------------------------------------------
		return (((PowerManager) theContext.getSystemService(Context.POWER_SERVICE)).isScreenOn());
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void LogCatClear ()
	{
		// -------------------------------------------------------------------------
		// 11/03/2014 ECU called to clear the LogCat log
		// -------------------------------------------------------------------------	
		try 
		{
			Runtime.getRuntime().exec("logcat -c");
		} 
		catch (IOException theException) 
		{		
		}
	}
	/* ============================================================================= */
	public static void logMessage (String theMessage)
	{
		// -------------------------------------------------------------------------
		// 19/09/2013 ECU added - will use Log to display a message 
		// -------------------------------------------------------------------------
		Log.i (TAG,theMessage);	
	}
	/* ============================================================================= */
	public static void LogToProjectFile (String theTag,String theString,boolean theAlwaysFlag)
	{
		// -------------------------------------------------------------------------
		// 12/02/2014 ECU add an entry into the project log - if enabled
		// 25/02/2014 ECU changed after the flag moved to 'storedData'
		// 06/03/2014 ECU added 'theAlwaysFlag'
		// 04/06/2016 ECU add try..catch because was occasionally getting a NPR
		//                on start up - think dur to ...dateFormatterFull being null
		// -------------------------------------------------------------------------
		if (PublicData.storedData.projectLogEnabled || theAlwaysFlag )
		{
			// ---------------------------------------------------------------------
		    // 12/02/2014 ECU write data to the project log file with timestamp
			// 25/02/2014 ECU added call the new method that enables the time
			//                format to be specified
			// 04/06/2016 ECU add try...catch
			// ---------------------------------------------------------------------
			try
			{
				Utilities.AppendToFile (PublicData.projectLogFile, 
									    Utilities.getAdjustedTime(PublicData.dateFormatterFull) + "\n   " + 
										theTag + " : " + theString + "\n");
			}
			catch (Exception theException)
			{
				// -----------------------------------------------------------------
				// 04/06/2016 ECU catch the exception but do not need to do anything
				// -----------------------------------------------------------------
			}
		}
	}
	/* ----------------------------------------------------------------------------- */
	public static void LogToProjectFile (String theTag,String theString)
	{
		// -------------------------------------------------------------------------
		// 06/03/2014 ECU created now that 'theAlwaysFlag' was added to always
		//                force out a message no matter what the state of the
		//                project logging flag
		// -------------------------------------------------------------------------
		LogToProjectFile (theTag,theString,false);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void makePhoneCall (Context theContext,String theNumberToCall)
	{
		// -------------------------------------------------------------------------
		// 24/07/2013 ECU check if this device is capable of making a phone call
		//                - if yes then do it otherwise will need to find some
		//                other device
		// -------------------------------------------------------------------------
		if (getPhoneNumber (theContext) != null)
		{
			try 
			{   
				// -----------------------------------------------------------------
			    // 24/07/2013 ECU now get the system to make the call
				// -----------------------------------------------------------------
				Intent callIntent = new Intent (Intent.ACTION_CALL);
				callIntent.setData(Uri.parse ("tel:" + theNumberToCall));
				// -----------------------------------------------------------------
				// 24/11/2013 ECU added the ...NEW_TASK flag
				// -----------------------------------------------------------------
				callIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
				theContext.startActivity (callIntent);
			}
			catch (ActivityNotFoundException activityException)
			{
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 13/09/2013 ECU this device is not capable of making a phone call so check
			//                whether another device has that capability - if not then
			//                inform the user
			// 18/09/2013 ECU add the check that to stop the device sending a socket message
			//                to itself which could be caused if the device was put into
			//                'airplane mode' after the app was started
			// ---------------------------------------------------------------------
			if (PublicData.phoneServer != null)
			{
				// -----------------------------------------------------------------
				// 24/07/2013 ECU this device is not capable of making a phone call
				// 13/09/2013 ECU a device has phone facilities so send a message to it
				// 06/02/2014 ECU when passing the number to the phone server then make sure
				//                that there are no embedded spaces
				// -----------------------------------------------------------------
				if (!PublicData.phoneServer.equalsIgnoreCase (PublicData.ipAddress))
				{
					// -------------------------------------------------------------
					// 18/09/2013 ECU don't try and send a socket message to this device
					//                (see comment above)
					// 31/01/2015 ECU change the name of the method called
					// 29/01/2016 ECU check that the message was sent correctly
					// -------------------------------------------------------------
					if (Utilities.sendSocketMessageSendStringAndWait (theContext, PublicData.phoneServer, 
							PublicData.socketNumber, "command phone " + theNumberToCall.replace (" ","")))
					{
						// ---------------------------------------------------------
						// 29/01/2016 ECU indicate that the server will be dealing
						//                with the call
						// ---------------------------------------------------------
						popToastAndSpeak (theContext.getString (R.string.phone_call_has_been_sent) + 
									      GetDeviceName (PublicData.phoneServer) + 
									      theContext.getString (R.string.for_processing));
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 29/01/2016 ECU indicate that the server could not send the
						//                message
						// ---------------------------------------------------------
						popToastAndSpeak (theContext.getString (R.string.unable_to_communicate_to_phone_server) + 
									      GetDeviceName (PublicData.phoneServer) + 
									      theContext.getString (R.string.acting_as_phone_server));
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
				else
				{
					popToastAndSpeak ("Cannot process the phone call to " + theNumberToCall + 
								" - check if device is in airplane mode");
				}
			}
			else
			{
				// -----------------------------------------------------------------
				// 13/09/2013 ECU unable to make a phone call so inform the user
				// -----------------------------------------------------------------
				popToastAndSpeak (theContext.getString (R.string.no_phone_server));
			}
		}
	}
	/* ============================================================================= */
    public static void OnPause (Context theContext,View theView)
    {
    	// -------------------------------------------------------------------------
    	// 12/03/2014 ECU called up by the 'onPause' in an activity when there is
    	//                the possibility of a soft keyboard being on display when
    	//                the device goes into standby mode - got a warning 'getTextBeforeCursor 
		//                on inactive InputConnection'. Following some searching on the
		//                internet then the following seems to remove the warning
    	// -------------------------------------------------------------------------
    	InputMethodManager inputMethodManager 
    		    	= (InputMethodManager)theContext.getSystemService (Context.INPUT_METHOD_SERVICE);
    	inputMethodManager.hideSoftInputFromWindow(theView.getWindowToken (),0);
    	// ---------------------------------------------------------------------------
    }
    /* ============================================================================= */
	public static int parseTheString (Context theContext,ArrayList<String> theList, VoiceCommands theVoiceCommands[])
	{
		// -------------------------------------------------------------------------
		// 17/06/2013 ECU this method checks whether the word strings picked up by 
		//				  the voice recognition software matches any of the commands
		// 21/05/2016 ECU added theContext as an argument
		// -------------------------------------------------------------------------
		boolean matchFound = false;
		// -------------------------------------------------------------------------
		// 17/06/2013 ECU remember the last words that were used
		// -------------------------------------------------------------------------
		lastListWords = listWords;
		
		for (int index=0; index < theList.size(); index++)
		{
			// ---------------------------------------------------------------------
			// 16/06/2013 ECU check if there is a match for this entry in the commands
			// ---------------------------------------------------------------------
			// 21/05/2016 ECU check if there is a match in the user defined spoken
			//                phrases
			// ---------------------------------------------------------------------
			if (VoiceCommandPhrases.SearchAllPhrasesForMatch(theContext,theList.get(index)))
			{
				// -----------------------------------------------------------------
				// 21/05/2016 ECU a user defined spoken phrase has been located and
				//                actioned so no need to go any further
				// -----------------------------------------------------------------
				return StaticData.USER_DEFINED_SPOKEN_PHRASE;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			listWords = theList.get(index).split (" ");
			// ---------------------------------------------------------------------
			// 17/06/2013 ECU change to accommodate all the different options for a command
			// ---------------------------------------------------------------------
			for (int commands = 0; commands < theVoiceCommands.length; commands++)
			{
				for (int indexEntries = 0; indexEntries < theVoiceCommands[commands].commandWords.length; indexEntries++)
				{
					for (int indexWords = 0; indexWords < theVoiceCommands[commands].commandWords[indexEntries].length; indexWords++)
					{
						matchFound = true;
						// ---------------------------------------------------------
						// 20/06/2013 ECU put in the check for .. indexWords >= listWords.length
						// ---------------------------------------------------------
						if (indexWords >= listWords.length || !listWords[indexWords].equalsIgnoreCase(theVoiceCommands[commands].commandWords[indexEntries][indexWords]))
						{
							matchFound = false;
							break;
						}
					}
					if (matchFound)
					{
						// ---------------------------------------------------------
						// 17/06/2013 ECU return with the index into the voice commands array
						// ---------------------------------------------------------
						return commands;
					}
				}
			}
		}
		// -------------------------------------------------------------------------
		// 17/06/2013 ECU indicate that no match was found 
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
	}
	// =============================================================================
	public static boolean retrievePendingIntent (Context theContext,int theRequestCode,Intent theIntent)
	{
		// -------------------------------------------------------------------------
		// 03/11/2016 ECU created to indicated whether the given parameters
		//                specify a pending intent is registered that will issue a
		//                broadcast.
		//                   true ..... pending intent exists
		//                   false .... pending intent does not exist - remember that
		//                              'null' is only return if the _NO_CREATE flag
		//                              is used
		// -------------------------------------------------------------------------
		return (!(PendingIntent.getBroadcast (theContext,theRequestCode, theIntent, PendingIntent.FLAG_NO_CREATE) == null));
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void PickAFile (Activity theActivity,String theFolder,String theExtension,boolean theImmediateFlag)
	{
		// -------------------------------------------------------------------------
		// 10/12/2013 ECU created - to start the activity which will enable a file
		//                to be selected
		// 17/03/2015 ECU added the 'theImmediateFlag' which, if not null, tells
		//                the activity to return as soon as the file is
		//                selected rather than when the 'back key' is pressed
		// 18/12/2015 ECU changed theImmediateFlag from String
		// ------------------------------------------------------------------------
		Intent myIntent = new Intent (theActivity,FileChooser.class);
		
		if (theFolder != null)
			myIntent.putExtra (StaticData.PARAMETER_FOLDER,theFolder);
		if (theExtension != null)
			myIntent.putExtra (StaticData.PARAMETER_FILTER,theExtension);
		// -------------------------------------------------------------------------
		// 17/03/2015 ECU handle the immediate flag
		// -------------------------------------------------------------------------
		if (theImmediateFlag)
			myIntent.putExtra (StaticData.PARAMETER_IMMEDIATE,theImmediateFlag);
		// -------------------------------------------------------------------------
		// 10/12/2013 ECU start up the activity to do the selection
		// -------------------------------------------------------------------------
		theActivity.startActivityForResult (myIntent,StaticData.REQUEST_CODE_FILE);
	}
	// -----------------------------------------------------------------------------
	public static void PickAFile (Activity theActivity,String theFolder,String theExtension)
	{
		// -------------------------------------------------------------------------
		// 17/03/2015 ECU change to call the master version of this method but
		//                with a final 'null' to indicate no need for an
		//                immediate response when the file is selected
		// 18/12/2015 ECU changed last argument from 'null'
		// --------------------------------------------------------------------------
		PickAFile (theActivity,theFolder,theExtension,false);
		// --------------------------------------------------------------------------
	
	}
	// =============================================================================
	public static String [] appNames;
	public static int		appType;
	// -----------------------------------------------------------------------------
	public static void PickAPackage (Context theContext,int thePackageType)
	{
		// -------------------------------------------------------------------------
		// 27/01/2016 ECU created to enable a user to select an install package
		// -------------------------------------------------------------------------
		appType = thePackageType;
		// -------------------------------------------------------------------------
		appNames = PickAPackagePopulate (theContext);
		// -------------------------------------------------------------------------
		// 27/01/2016 ECU enable the user to select the package
		// -------------------------------------------------------------------------
		DialogueUtilities.listChoice (theContext,
									  "Select Required Package",
									  appNames,
									  Utilities.createAMethod (Utilities.class,"PickAPackageName",0),
									  theContext.getString (R.string.cancel),
									  null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void PickAPackageName (int thePackageIndex)
	{
		// -------------------------------------------------------------------------
		// 27/01/2017 ECU created to set the package name
		// -------------------------------------------------------------------------
		switch (appType)
		{
			// ---------------------------------------------------------------------
			case StaticData.PACKAGE_TYPE_CONTACTS:
				PublicData.storedData.contactsPackageName = appNames [thePackageIndex];
				break;
			// ---------------------------------------------------------------------
			case StaticData.PACKAGE_TYPE_MAIL:
				PublicData.storedData.mailPackageName = appNames [thePackageIndex];
				break;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 27/01/2016 ECU clear the data because all done
		// -------------------------------------------------------------------------
		appNames = null;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static String [] PickAPackagePopulate (Context theContext)
	{
		List<ApplicationInfo> localAppInfo  = theContext.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
		String []             localAppNames = new String [localAppInfo.size()];
		// -------------------------------------------------------------------------
		for (int theApp = 0; theApp < localAppInfo.size(); theApp++)
		{
			localAppNames [theApp] = localAppInfo.get(theApp).packageName;
		}
		// -------------------------------------------------------------------------
		// 27/01/2016 ECU sort the list before displaying
		// -------------------------------------------------------------------------
		Arrays.sort (localAppNames);	
		// -------------------------------------------------------------------------
		return localAppNames;
		// -------------------------------------------------------------------------
	}
    /* ============================================================================= */	
	public static void PlayAFile (Context theContext,String theFileName,int thePosition,final boolean theActionFlag)
	{
		// -------------------------------------------------------------------------
		//            ECU play the specified if it exists - if not then record into
		//                that file
		// 03/06/2016 ECU add theActionFlag to indicate if this method has been
		//                called as the result of an action (true) or not (false)
		// -------------------------------------------------------------------------
		File file = new File(theFileName);

		if (file.exists()) 
		{
			// =====================================================================
			// the file exists so get the media player to play it
			// =====================================================================
			if (PublicData.mediaPlayer != null)
			{
				if (PublicData.mediaPlayer.isPlaying())
					PublicData.mediaPlayer.stop();
				// -----------------------------------------------------------------
				// 09/12/2014 ECU release the resources
				// -----------------------------------------------------------------
				PublicData.mediaPlayer.reset();
				PublicData.mediaPlayer.release();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			PublicData.mediaPlayer = MediaPlayer.create
						(null, Uri.fromFile (new File (theFileName)));
			// ---------------------------------------------------------------------
			// 10/08/2013 ECU check if it was possible to create the mediaPlayer
			// ---------------------------------------------------------------------
			if (PublicData.mediaPlayer != null)
			{
				// -----------------------------------------------------------------
				// 02/11/2016 ECU try and set the volume to maximum
				// -----------------------------------------------------------------
				PublicData.mediaPlayer.setVolume (1.0f,1.0f);
				// -----------------------------------------------------------------
				// 03/06/2016 ECU added the listener to detect when the media player
				//                has finished
				// -----------------------------------------------------------------
				PublicData.mediaPlayer.setOnCompletionListener(new OnCompletionListener() 
				{
					@Override
					public void onCompletion(MediaPlayer theMediaPlayer) 
					{
						// ---------------------------------------------------------
						// 03/06/2016 ECU send message to indicate the fact but only
						//                if called by an 'action'
						// ---------------------------------------------------------
						if (theActionFlag)
							PublicData.messageHandler.sendEmptyMessage (StaticData.MESSAGE_ACTION_FINISHED);
						// ---------------------------------------------------------
					}
				});
				// -----------------------------------------------------------------		
				// 19/06/2013 ECU check if positioning is required
				// -----------------------------------------------------------------
				if (thePosition != StaticData.NO_RESULT)
				{
					PublicData.mediaPlayer.seekTo(thePosition);
				}
				PublicData.mediaPlayer.start();
			}
			else
			{
				// -----------------------------------------------------------------
				// 10/08/2013 ECU indicate that a problem occurred
				// 14/08/2013 ECU change to debugMessage from logging to logcat
				// -----------------------------------------------------------------
				debugMessage (TAG,"PlayAFile:unable to create MediaPlayer for "+ theFileName);
				// -----------------------------------------------------------------
			}	
		}	
		else
		{
			// ---------------------------------------------------------------------
			// 31/05/2013 ECU only record the missing file if it is not part of
			//                a playlist which is being performed
			// 01/05/2015 ECU added the check on null
			// ---------------------------------------------------------------------
			if ((PublicData.musicPlayerData == null) || !PublicData.musicPlayerData.tracksPlaying)
			{
				// -----------------------------------------------------------------
				// 31/05/2013 ECU want to record a new file
				// -----------------------------------------------------------------
				recordAFile (theContext,theFileName);
			}
		}
	}
	/* ----------------------------------------------------------------------------- */
	public static void PlayAFile (Context theContext,String theFileName,boolean theWaitFlag)
	{
		PlayAFile (theContext,theFileName);
		// -------------------------------------------------------------------------
		// 13/05/2013 ECU now wait until the file finishes playing
		// -------------------------------------------------------------------------
		while (PublicData.mediaPlayer.isPlaying())
		{
			waitMilliseconds (200);
		}
	}
	/* ----------------------------------------------------------------------------- */
	public static void PlayAFile (Context theContext,String theFileName)
	{
		// -------------------------------------------------------------------------
		// 03/06/2016 ECU added the 'false' flag to indicate not an 'action'
		// -------------------------------------------------------------------------
		PlayAFile (theContext,theFileName,StaticData.NO_RESULT,false);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void playAVideo (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 16/06/2013 ECU play the specified video
		// -------------------------------------------------------------------------
		 VideoView videoView = (VideoView) findViewById(R.id.surface_view);
	     videoView.setVideoURI (Uri.parse(theFileName));
	     videoView.setMediaController (new MediaController(this));
	     videoView.requestFocus ();
	     videoView.start ();
	}
	/* ============================================================================= */
	@SuppressWarnings("deprecation")
	public static void  playFromBuffer (Context theContext,byte [] theBuffer,int theLength) 
	{  
		// -------------------------------------------------------------------------
		// 18/06/2013 ECU created to play a buffer which is in 'wav' format
		// 02/02/2015 ECU just revisit this method because it was written in 06/2013
		//                and is really in the 'bodge' category - only used once and
		//                not really part of the main care system
		// -------------------------------------------------------------------------
		AudioTrack audioTrack = new AudioTrack (AudioManager.STREAM_MUSIC, 44100,
	    		AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
	    		AudioTrack.getMinBufferSize(44100,AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT),
	    		AudioTrack.MODE_STREAM);
	    // -------------------------------------------------------------------------
		// 18/06/2013 ECU start up the player which will then play the data written
		//                to it
		// -------------------------------------------------------------------------
	    audioTrack.play ();
	    // -------------------------------------------------------------------------
	    // 18/06/2013 ECU want to jump over the 'wav' file header
	    // 02/02/2015 ECU changed to use AudioRecorder.WAV_..... rather than 44
	    // -------------------------------------------------------------------------
        audioTrack.write (theBuffer,AudioRecorder.WAV_FILE_HEADER_SIZE, theLength - AudioRecorder.WAV_FILE_HEADER_SIZE); 
        // -------------------------------------------------------------------------
        // 18/06/2013 ECU stop playing after the current buffer has been processed
        //                (because it was created with STREAM_MUSIC) and then
        //                release the resources
        // -------------------------------------------------------------------------
        audioTrack.stop ();
        audioTrack.release();
        // -------------------------------------------------------------------------
	}
	// =============================================================================
	private static int			loopCounter;					// 11/03/2017 ECU added
	private static boolean		mediaPlayerPaused;				// 10/03/2017 ECU added
	// -----------------------------------------------------------------------------
	public static void playRawResource (Context theContext,int theResourceID,int theLoopCounter,final int theLoopPosition)
	{
		// -------------------------------------------------------------------------
		// 10/03/2017 ECU created to play the specified 'raw' resource using the
		//                media player
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 11/03/2017 ECU remember the loop counter for future use
			//            ECU a loop counter of 0 indicates 'no looping'. If the counter
			//                is > 0 then it needs to be decremented by 1 because
			//                the file is played before being 'looped' - this actually 
			//                means that a loop counter of 1 is in fact equivalent
			//                to 'no looping'
			//            ECU looping will be from the specified position
			// ---------------------------------------------------------------------
			loopCounter = theLoopCounter - 1;
			// ---------------------------------------------------------------------
			// 10/03/2017 ECU get the asset file descriptor for the 'raw' resource
			// ---------------------------------------------------------------------
			AssetFileDescriptor afd = theContext.getResources().openRawResourceFd (theResourceID);
			// ---------------------------------------------------------------------
			// 10/03/2017 ECU check if the global media player is active
			// ---------------------------------------------------------------------
			if (PublicData.mediaPlayer != null && PublicData.mediaPlayer.isPlaying())
			{
				// -----------------------------------------------------------------
				// 10/03/2017 ECU pause the global media player while this one plays
				// -----------------------------------------------------------------
				MusicPlayer.playOrPause (false);
				// -----------------------------------------------------------------
				// 10/03/2017 ECU remember that it has been paused
				// -----------------------------------------------------------------
				mediaPlayerPaused = true;
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 10/03/2017 ECU indicate that media player has not been paused
				// -----------------------------------------------------------------
				mediaPlayerPaused = false;
			}
			// ---------------------------------------------------------------------
			final MediaPlayer mediaPlayer = new MediaPlayer ();
			// ---------------------------------------------------------------------
			// 10/03/2017 ECU specify the source of the music
			// ---------------------------------------------------------------------
			mediaPlayer.setDataSource (afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
	        // ---------------------------------------------------------------------
	        // 10/03/2017 ECU set up a listener to be called when the music has
	        //                'completed' playing
	        // ---------------------------------------------------------------------
	        mediaPlayer.setOnCompletionListener(new OnCompletionListener() 
			{
				@Override
				public void onCompletion (MediaPlayer theMediaPlayer) 
				{
					// -------------------------------------------------------------
					// 11/03/2017 ECU check if any looping is needed
					// -------------------------------------------------------------
					if (loopCounter <= 0)
					{
						// ---------------------------------------------------------
						// 11/03/2017 ECU everything done so just check on the global 
						//                media player
						// ---------------------------------------------------------
						// 10/03/2017 ECU check if the global media player needs to be
						//                'resumed'
						// ---------------------------------------------------------
						if (mediaPlayerPaused)
						{
							// -----------------------------------------------------
							// 10/03/2017 ECU the global media player needs to be resumed
							// -----------------------------------------------------
							MusicPlayer.playOrPause (true);
							// -----------------------------------------------------
						}
					}
					else
					{
						// ---------------------------------------------------------
						// 11/03/2017 ECU check about loops
						// ---------------------------------------------------------
						mediaPlayer.seekTo (theLoopPosition);
						mediaPlayer.start ();
						// ---------------------------------------------------------
						// 11/03/2017 ECU decrement the loop counter
						// ---------------------------------------------------------
						loopCounter--;
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
			});
	        // ---------------------------------------------------------------------
	        // 10/03/2017 ECU get ready and then start the media player
	        // ---------------------------------------------------------------------
	        mediaPlayer.prepare();
	        mediaPlayer.start();
	        // ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 10/03/2017 ECU something went wrong but no need to do anything
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void playRawResource (Context theContext,int theResourceID)
	{
		// -------------------------------------------------------------------------
		// 11/03/2017 ECU call the master method and indicate no loops needed
		// -------------------------------------------------------------------------
		playRawResource (theContext,theResourceID,0,0);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void recordAFile (Context theContext,
			                        String theFileName,
			                        MethodDefinition<?> theStopMethodDefinition,
			                        boolean theAppendFlag)
	{
		Intent myIntent = new Intent (theContext,AudioRecorder.class);
		//--------------------------------------------------------------------------
		// 10/08/2013 ECU added the NEW_TASK flag
		// 26/10/2016 ECU changed to use PARA....
		// 14/11/2016 ECU added theAppendFlag
		// -------------------------------------------------------------------------
		myIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); 
		myIntent.putExtra (StaticData.PARAMETER_AUDIO_FILE_NAME,theFileName);
		// -------------------------------------------------------------------------
		// 26/10/2016 ECU check if method definition to be added
		// -------------------------------------------------------------------------
		if (theStopMethodDefinition != null)
		{
			myIntent.putExtra (StaticData.PARAMETER_METHOD_DEFINITION,theStopMethodDefinition);
		}
		// -------------------------------------------------------------------------
		// 14/11/2016 ECU pass through the append flag - if set
		// -------------------------------------------------------------------------
		if (theAppendFlag)
			myIntent.putExtra (StaticData.PARAMETER_APPEND,true);
		// -------------------------------------------------------------------------
		theContext.startActivity (myIntent);
	}
	// -----------------------------------------------------------------------------
	public static void recordAFile (Context theContext,String theFileName)
	{
		// -------------------------------------------------------------------------
		// 26/10/2016 ECU was the old master but changed when method definition added
		// 14/11/2016 ECU added the 'false' because the master method has changed
		// -------------------------------------------------------------------------
		recordAFile (theContext,theFileName,null,false);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static Object requestObjectFromDevice (Context theContext,
												  String theIPAddress,
												  int thePort,
												  int theMessageType)
	{
		// -------------------------------------------------------------------------
		// 20/01/2016 ECU created to request an object from the specified device and
		//                wait for the object
		//                returns the object or 'null' if something happened
		// --------------------------------------------------------------------------
		try
		{   
			Socket localSocket = new Socket (InetAddress.getByName(theIPAddress),thePort);
			// ---------------------------------------------------------------------
			// 04/08/2013 ECU now get an output stream for the data to be transmitted
			// 19/08/2013 ECU changed to use buffering so use BufferedOutputStream rather
			//				  than just OutputStream
			// ---------------------------------------------------------------------
			BufferedOutputStream outputStream 	= new BufferedOutputStream (localSocket.getOutputStream());
			BufferedInputStream inputStream 	= new BufferedInputStream  (localSocket.getInputStream());
			// ---------------------------------------------------------------------
			socketSendMessageType (theMessageType, outputStream);
			// ---------------------------------------------------------------------
			// 20/01/2016 ECU try and read in the required object
			// ---------------------------------------------------------------------
			ObjectInputStream objectInputStream = new ObjectInputStream (inputStream);
			Object localObject = objectInputStream.readObject();
			objectInputStream.close();
			// ---------------------------------------------------------------------
			// 20/01/2016 ECU close down the streams
			// ---------------------------------------------------------------------
			outputStream.close();
			localSocket.close();
			// ---------------------------------------------------------------------
			// 20/01/2016 ECU return with the retrieved object
			// ---------------------------------------------------------------------
			return localObject;
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 20/01/2016 ECU an error so return a 'null' object
			// ---------------------------------------------------------------------
			return null;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	// 14/02/2014 ECU added the 'suppress' because was getting a 'dead code' warning
	//                on the 
	//                         else
	//                              return null;
	//                at the end of the method - cannot see why as if I delete it 
	//                then get an error that the method is missing a return
	//                statement
	// -----------------------------------------------------------------------------
	@SuppressWarnings ("unused")
	// -----------------------------------------------------------------------------
	public static String [] returnSubDirectories (String theDirectory,boolean theSortFlag,final String theExtension)
	{
		// -------------------------------------------------------------------------
		// 07/04/2015 ECU returns the names of the sub directories in this directory
		// -------------------------------------------------------------------------
		File 		directory = new File (theDirectory);
		// -------------------------------------------------------------------------
		if (directory != null)
		{
			String [] 	results = null;
			// ---------------------------------------------------------------------
			// 07/04/2015 ECU get list of sub directories in this directory
			// ---------------------------------------------------------------------
			File[] listOfFiles = directory.listFiles(new FileFilter ()		
													{
		         										@Override
		         											public boolean accept (File pathName) 
		         											{
		         												return pathName.isDirectory();
		        	
		         											}
													});
			// ---------------------------------------------------------------------
			// 07/04/2015 ECU build the results
			// ---------------------------------------------------------------------
			if (listOfFiles.length > 0)
			{ 
				// -----------------------------------------------------------------
				// 07/04/2015 ECU indicate how many entries have been removed
				// -----------------------------------------------------------------
				int numberDeleted = 0;
				// -----------------------------------------------------------------
				// 07/04/2015 ECU check if extension matching is required
				// -----------------------------------------------------------------
				if (theExtension != null)
				{
					File[] listOfContainedFiles;
					// -------------------------------------------------------------
					// 07/04/2015 ECU only want sub directories that contain at least
					//                one file with the required extension
					// -------------------------------------------------------------
					for (int theSubDir = 0; theSubDir<listOfFiles.length; theSubDir++)
					{
						listOfContainedFiles = listOfFiles [theSubDir].listFiles(new FileFilter ()		
									{
										@Override
										public boolean accept (File pathName) 
										{
											if (pathName.isDirectory())
											{
												return true;
											}
											else
											{
												// ---------------------------------
												// 10/11/2014 ECU added the Locale to
												//                the method call
												//            ECU changed to use Locale.getDefault
												//                instead of Locale.UK
												// ---------------------------------
												return pathName.getName().toLowerCase (Locale.getDefault()).endsWith (theExtension);
											}
										}
									});
						// ---------------------------------------------------------
						// 07/04/2015 ECU if the list of contained files contains
						//                anything then include this directory
						// ---------------------------------------------------------
						if (listOfContainedFiles == null || listOfContainedFiles.length == 0)
						{
							// -----------------------------------------------------
							// 07/04/2015 ECU ignore this directory
							// -----------------------------------------------------
							listOfFiles [theSubDir] = null;
							// -----------------------------------------------------
							// 07/04/2015 ECU and increment the number deleted
							// -----------------------------------------------------
							numberDeleted++;
							// -----------------------------------------------------
						}
										
					}
				}
				// -----------------------------------------------------------------
				// 07/04/2015 ECU declare the results array
				//            ECU adjust by the number deleted
				// -----------------------------------------------------------------
				results = new String [listOfFiles.length - numberDeleted];
				// -----------------------------------------------------------------
				// 07/04/2015 ECU handle depending on whether a sort is required
				// -----------------------------------------------------------------
				if (theSortFlag)
				{
					// -------------------------------------------------------------
					// 07/04/2015 ECU want the option of sorting the results so use a list
					// -------------------------------------------------------------
					ArrayList<String> fileNames = new ArrayList<String> ();
					for (int theFile = 0; theFile < listOfFiles.length; theFile++)
					{
						// ---------------------------------------------------------
						// 07/04/2015 ECU put in the check on a deleted element
						// ---------------------------------------------------------
						if (listOfFiles [theFile] != null)
							fileNames.add (listOfFiles [theFile].getPath());
					}
					// -------------------------------------------------------------
					Collections.sort(fileNames);
					// -------------------------------------------------------------
					for (int theFile = 0; theFile < fileNames.size(); theFile++)
					{
						results [theFile] = fileNames.get(theFile);
					}
				}
				else
				{
					// -------------------------------------------------------------
					// 07/04/2015 ECU return an unsorted array
					// -------------------------------------------------------------
					results = new String [listOfFiles.length];
					for (int theFile = 0; theFile < listOfFiles.length; theFile++)
					{
						results [theFile] = listOfFiles [theFile].getPath();
					}
				}
			}
			
			return results;
		}
		else
			return null;
	}
	/* ============================================================================= */
	public static void scanFilesInFolder (Context theContext,String theFolderName)
	{
		if (theFolderName != null)
		{
			// ---------------------------------------------------------------------
			// 19/10/2014 ECU declare any working variables
			// ---------------------------------------------------------------------
			int    accuracy;
			double latitude; 
			double longitude;		
			// ---------------------------------------------------------------------
			// 19/10/2014 ECU the structure of each file is defined by
			//			       R.string.tracking_file_format and its regex 
			//                pattern is in
			//                 R.string.tracking_file_format_pattern
			// 20/02/2017 ECU the tracking pattern changed to include both notes
			//                and images
			// ---------------------------------------------------------------------
			Pattern filePattern = Pattern.compile(theContext.getString (R.string.tracking_file_format_pattern));
			Matcher fileMatcher;
			// ---------------------------------------------------------------------
			// 20/02/2017 ECU declare the array that will hold the data that is on the
			//                disk
			// ---------------------------------------------------------------------
			LocationActivity.trackingData = new ArrayList<TrackingData>();
			// ---------------------------------------------------------------------
			File directory  = new File(theFolderName);
			File[] files 	= directory.listFiles();
			// ---------------------------------------------------------------------
			// 19/10/2014 ECU loop for all files in the specified path
			// ---------------------------------------------------------------------
			for (int fileIndex = 0; fileIndex < files.length; ++fileIndex) 
			{
				// -----------------------------------------------------------------
				// 19/10/2014 ECU check that the file is of the correct format
				// -----------------------------------------------------------------
				fileMatcher = filePattern.matcher (files[fileIndex].getName());
				// -----------------------------------------------------------------
				// 19/10/2014 ECU only process this entry if match is found
				// -----------------------------------------------------------------
			    if(fileMatcher.matches())
			    {	
					latitude  	= Double.parseDouble (fileMatcher.group(1)); 
					longitude 	= Double.parseDouble (fileMatcher.group(2));
					accuracy  	= Integer.parseInt   (fileMatcher.group(3));
					// -------------------------------------------------------------
					// adjust number in the filename by the stored accuracy 
					// -------------------------------------------------------------
					latitude 	= latitude  / (double) accuracy;
					longitude 	= longitude / (double) accuracy;   
					// -------------------------------------------------------------
					// 20/02/2017 ECU create a new tracking details object
					// -------------------------------------------------------------
					LocationActivity.trackingData.add (new TrackingData (latitude,longitude,accuracy,files[fileIndex].getName()));
					//--------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
			// 22/09/2013 ECU change to use the custom view toast
			// 31/05/2016 ECU changed to use resource
			// ---------------------------------------------------------------------
			popToast (theContext.getString (R.string.tracking_mode_is_on),true,Toast.LENGTH_SHORT);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 22/09/2013 ECU the specified folder name is uninitialised
			// 31/05/2016 ECU changed to use resource
			// ---------------------------------------------------------------------
			popToast (theContext.getString (R.string.tracking_name_null),true);
		}
	}
	/* ============================================================================= */
	public static void scanForAMatch (Context theContext,double theLatitude,double theLongitude)
	{
		// -------------------------------------------------------------------------
		// 20/02/2017 ECU changed the logic to use the TrackingData list rather than
		//                arrays that used to be held in PublicData
		// -------------------------------------------------------------------------
		double 		 workingLatitude;
		double 		 workingLongitude;
		TrackingData workingRecord;
		// -------------------------------------------------------------------------
		// 19/10/2014 ECU loop for all stored entries
		// 20/02/2017 ECU changed to use list
		// -------------------------------------------------------------------------
		for (int index=0; index<LocationActivity.trackingData.size(); index++)
		{	
			// ---------------------------------------------------------------------
			// 20/02/2017 ECU get a local copy of the indexed object
			// ---------------------------------------------------------------------
			workingRecord = LocationActivity.trackingData.get (index);
			// ---------------------------------------------------------------------
			// 			  ECU adjust the input arguments by the accuracy 
			// 04/09/2015 ECU changed to use PublicData
			// 20/02/2017 ECU change logic to use workingRecord
			// ---------------------------------------------------------------------
			workingLatitude  = adjustByAccuracy (theLatitude,workingRecord.accuracy);
			workingLongitude = adjustByAccuracy (theLongitude,workingRecord.accuracy);
			// ---------------------------------------------------------------------
			// 19/10/2014 ECU check for a match
			// ---------------------------------------------------------------------
			if ((workingLatitude  == workingRecord.latitude) &&
				(workingLongitude == workingRecord.longitude))
			{
				// -----------------------------------------------------------------
				// 20/02/2017 ECU at this point a match can be caused by a note
				//                or an image
				//            ECU add track folder
				//            ECU changed to use the type held in the working record
				// -----------------------------------------------------------------
				if (!workingRecord.type)
				{
					// -------------------------------------------------------------
					// 01/01/2014 ECU change to use the custom Toast
					// 02/01/2014 ECU print out the matching coordinates
					// 03/01/2014 ECU change to have a short delay for the message
					// -------------------------------------------------------------
					popToast ("Match Found" + "\n" + 
								"Latitude : " + workingLatitude + 
								"\nLongitude : " + workingLongitude + 
								"\n\nPlaying\n" + workingRecord.fileName,true,Toast.LENGTH_SHORT);
					// -------------------------------------------------------------
					// 03/01/2014 ECU alwaysPlay moved from MainActivity to LocationActivity
					// -------------------------------------------------------------
					if (!LocationActivity.lastNoteActioned.equals (workingRecord.fileName) 
										|| LocationActivity.alwaysPlay)
					{
						// ---------------------------------------------------------
						// 20/02/2017 ECU need to add the folder path
						// ---------------------------------------------------------
						PlayAFile (theContext,PublicData.trackFolder + workingRecord.fileName);
						// ---------------------------------------------------------
						// 20/06/2013 ECU remember the name of last file played
						// ---------------------------------------------------------
						LocationActivity.lastNoteActioned = workingRecord.fileName;
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 22/09/2013 ECU changed the folder reference from Track...
						// 19/10/2014 ECU changed to just speak a phrase
						// 20/02/2017 ECU changed to use resource
						// ---------------------------------------------------------
						Utilities.SpeakAPhrase (theContext,theContext.getString (R.string.location_not_moved));
						// ---------------------------------------------------------
					}
				}
				else
				{
					// -------------------------------------------------------------
					// 20/02/2017 ECU the record refers to a photograph
					// -------------------------------------------------------------
					if (!LocationActivity.lastImageActioned.equals (workingRecord.fileName) 
							|| LocationActivity.alwaysPlay)
					{
						// ---------------------------------------------------------
						// 20/02/2017 ECU need to add the folder path
						// ---------------------------------------------------------
						LocationActivity.showPhotograph (LocationActivity.imageView,PublicData.trackFolder + workingRecord.fileName);
						// ---------------------------------------------------------
						// 20/06/2013 ECU remember the name of last file played
						// ---------------------------------------------------------
						LocationActivity.lastImageActioned = workingRecord.fileName;
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 22/09/2013 ECU changed the folder reference from Track...
						// 19/10/2014 ECU changed to just speak a phrase
						// 20/02/2017 ECU changed to use resource
						// ---------------------------------------------------------
						Utilities.SpeakAPhrase (theContext,theContext.getString (R.string.location_not_moved));
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
		}	
	}
	/* ============================================================================= */
	public static void popToast (Context theContext,String theString)
	{
		Toast toast = Toast.makeText(theContext,theString,Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
		toast.show();
	}
	/* ----------------------------------------------------------------------------- */
	public static Toast popToast (String theString, boolean theCentringFlag)
	{
		// -------------------------------------------------------------------------
		// 22/09/2013 ECU use the new method with a long delay
		// 13/11/2015 ECU changed the 'burnt_toast_text' xml file to build the
		//                centring flag by default
		// -------------------------------------------------------------------------
		return popToast (theString,theCentringFlag,Toast.LENGTH_LONG);
	}
	/* ----------------------------------------------------------------------------- */
	public static Toast popToast (String theString, int theLengthOfDisplay)
	{
		// -------------------------------------------------------------------------
		// 22/09/2013 ECU new method to just change the length of Toast display
		// -------------------------------------------------------------------------
		return popToast (theString,false,theLengthOfDisplay);
	}
	/* ----------------------------------------------------------------------------- */
	public static Toast popToast (String theString, boolean theCentringFlag,int theLengthOfDisplay)
	{
		return popToast (theString,theCentringFlag,theLengthOfDisplay,null);
	}	
	/* ----------------------------------------------------------------------------- */
	public static Toast popToast (String theString,
			                      boolean theCentringFlag,
			                      int theLengthOfDisplay,
			                      String theNewImage)
	{
		// -------------------------------------------------------------------------
		// 11/09/2013 ECU created to try and display a customised toast layout
		//                do not like having to use MainActivity.activity but
		//                it works and can look at it again later
		// 13/09/2013 ECU add theCentringFlag
		//                         true ...... centre the text
		//                         false ..... leave text as is
		// 21/09/2013 ECU added the try ... catch. I was getting an error that implied that
		//                MainActivity.activity is null.
		// 22/09/2013 ECU added theLengthOfDisplay as an int to choose between 
		//                Toast.LENGTH_SHORT or Toast.LENGTH_LONG
		// -------------------------------------------------------------------------
		try
		{
			LayoutInflater  myInflator = (LayoutInflater)MainActivity.activity.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
		
			View layout = myInflator.inflate (R.layout.burnt_toast,
		                               (ViewGroup) MainActivity.activity.findViewById (R.id.burnt_toast_layout));
			// ---------------------------------------------------------------------
			// 12/09/2013 ECU display the copyright
			// ---------------------------------------------------------------------
			TextView copyright = (TextView) layout.findViewById (R.id.burnt_toast_copyright);
			copyright.setText (PublicData.copyrightMessage);
		
			TextView text = (TextView) layout.findViewById (R.id.burnt_toast_text);
			text.setText(theString);
			// ---------------------------------------------------------------------
			// 09/12/2013 ECU check if want to change the image
			// ---------------------------------------------------------------------
			if (theNewImage != null)
			{
				ImageView toastView = (ImageView) layout.findViewById (R.id.burnt_toast_image);
				
				final BitmapFactory.Options options = new BitmapFactory.Options();
				// -----------------------------------------------------------------
				// 09/12/2013 ECU scale the image to reduce memory used
				// -----------------------------------------------------------------
				options.inSampleSize = 4;

				Bitmap imageBitmap = BitmapFactory.decodeFile (theNewImage,options);	
				// -----------------------------------------------------------------
				// 06/02/2014 ECU change the layoutParams from '500,500'
				// -----------------------------------------------------------------
				LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(PublicData.screenWidth/4,PublicData.screenWidth/4);
				parms.gravity=Gravity.CENTER;

				toastView.setLayoutParams (parms);
				 
				toastView.setImageBitmap (imageBitmap);
			}
			// ---------------------------------------------------------------------
			// 13/09/2013 ECU check if centring is required
			// ---------------------------------------------------------------------
			if (theCentringFlag)
				text.setGravity (Gravity.CENTER);

			Toast toast = new Toast (MainActivity.activity);
			toast.setGravity (Gravity.CENTER_VERTICAL, 0, 0);
			// ---------------------------------------------------------------------
			// 22/09/2013 ECU set the length according to the parameter
			// ---------------------------------------------------------------------
			toast.setDuration (theLengthOfDisplay);
			
			toast.setView (layout);
			toast.show ();	
		
			return toast;
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 21/09/2013 ECU added
			// ---------------------------------------------------------------------
			return null;
		} 
	}
	/* ----------------------------------------------------------------------------- */
	public static Toast popToast (String theString)
	{
		// -------------------------------------------------------------------------
		// 13/09/2013 ECU indicate that the text is to be displayed 'as is'
		// -------------------------------------------------------------------------
		return popToast (theString,false);
	}
	/* --------------------------------------------------------------------- */
	public static void popToast (String theString,boolean theCentringFlag,int theLengthOfDisplay,int theNumberOfRepeats)
	{
		// -------------------------------------------------------------------------
		// 11/09/2013 ECU created to put up a toast message a number of times
		// 22/09/2013 ECU changed to add extra parameters
		// -------------------------------------------------------------------------
		popToastWait (popToast (theString,theCentringFlag,theLengthOfDisplay),theNumberOfRepeats);
	}
	/* ----------------------------------------------------------------------------- */
	@SuppressLint("InflateParams")
	public static void popToast (Activity 		theActivity,
								 View 			theView,
							     String 		theString,
							     String	 		theButtonText,
							     final int 		theTimeOut,
							     boolean 		theScrollableFlag,
							     final Method 	theClickMethod)
	{
		// -------------------------------------------------------------------------
		// 02/03/2014 ECU this method uses a 'popup' window to provide a
		//                'toast like' display which has a button for clearing
		//                it from the screen
		// 17/03/2014 ECU handle the timeout which is supplied in 
		//                'milliseconds'
		// 31/05/2016 ECU added the scrollable flag
		// 10/06/2016 ECU updated the 'main' method to have the activity and
		//                click method passed through as arguments
		// 13/01/2017 ECU if theString starts with MONO_SPACED then that is the way
		//                that the text will be displayed
		// -------------------------------------------------------------------------
		// 10/06/2016 ECU changed from MainActivity.activity
		// -------------------------------------------------------------------------
		LayoutInflater layoutInflater = (LayoutInflater) theActivity.getSystemService (LAYOUT_INFLATER_SERVICE);  
		// -------------------------------------------------------------------------
		// 31/05/2016 ECU decide on the layout dependent on the scrollable
		//                option
		// -------------------------------------------------------------------------
	    final View popupView = layoutInflater.inflate ((theScrollableFlag ? R.layout.popup_scrollable 
	    		                                                          : R.layout.popup), null);
	    // -------------------------------------------------------------------------
	    // 30/11/2016 ECU make the width dependent on the scrollable flag
	    // -------------------------------------------------------------------------
	    final PopupWindow popupWindow = new PopupWindow (popupView,
	    												 (theScrollableFlag ? LayoutParams.MATCH_PARENT 
	    														            : LayoutParams.WRAP_CONTENT),
	    												 LayoutParams.WRAP_CONTENT);  
	    // -------------------------------------------------------------------------        
	    // 02/03/2014 ECU define the views that will be updated
	    // -------------------------------------------------------------------------
	    		Button 		buttonDismiss 	= (Button)  popupView.findViewById (R.id.popup_button);
	    		TextView 	textView		= (TextView)popupView.findViewById (R.id.popup_text);
	    		TextView 	copyRight 		= (TextView)popupView.findViewById (R.id.popup_copyright);		
	    final 	TextView 	timeoutView		= (TextView)popupView.findViewById (R.id.popup_timeout);
	    // -------------------------------------------------------------------------
	    // 31/05/2016 ECU switch on scrolling
	    // -------------------------------------------------------------------------
	    if (theScrollableFlag)
	    {
	    	textView.setMovementMethod (new ScrollingMovementMethod());
	    }
	    // -------------------------------------------------------------------------
	    // 13/01/2017 ECU check if the text indicates the use of monospacing
	    // -------------------------------------------------------------------------
	    if (theString.startsWith (StaticData.MONO_SPACED))
	    {
	    	// ---------------------------------------------------------------------
	    	// 13/01/2017 ECU mono spacing is wanted
	    	// ---------------------------------------------------------------------
	    	textView.setTypeface (Typeface.MONOSPACE); 
	    	// ---------------------------------------------------------------------
	    	// 13/01/2017 ECU and strip out the introducer
	    	// ---------------------------------------------------------------------
	    	theString = theString.replace (StaticData.MONO_SPACED,StaticData.BLANK_STRING);
	    	// ---------------------------------------------------------------------
	    }
	    // -------------------------------------------------------------------------
	    // 02/03/2014 ECU update the text information
	    // -------------------------------------------------------------------------
	    textView.setText  (theString);
	    copyRight.setText (PublicData.copyrightMessage);    
	    // -------------------------------------------------------------------------
	    // 18/03/2014 ECU check if the legend of the button is to be changed to the 
	    //                argument
	    // -------------------------------------------------------------------------
	    if (theButtonText != null)
	    {
	    	buttonDismiss.setText (theButtonText);
	    }
	    // -------------------------------------------------------------------------
	    // 02/03/2014 ECU display the listener that will be used to remove the display
	    // -------------------------------------------------------------------------
	    buttonDismiss.setOnClickListener (new Button.OnClickListener()
	    {
	    	@Override
	    	public void onClick(View view) 
	    	{
	    		// -----------------------------------------------------------------
	    		// 02/03/2014 ECU remove the popup display from the screen
	    		// -----------------------------------------------------------------
	    		popupWindow.dismiss();
	    		// -----------------------------------------------------------------
	    		// 10/06/2016 ECU check if there is a method to be actioned
	    		// -----------------------------------------------------------------
	    		try 
	    		{
	    			if (theClickMethod != null)
	    			{
	    				// ---------------------------------------------------------
	    				// 10/06/2016 ECU invoke the method that has been passed through
	    				// ---------------------------------------------------------
	    				theClickMethod.invoke (null);
	    				// ---------------------------------------------------------
	    			}
	    		}
	    		catch (Exception theException) 
	    		{
	    			
	    		} 
	    		// -----------------------------------------------------------------
	    	}
	    });
	    // -------------------------------------------------------------------------
	    // 03/03/2014 ECU try and animate the way the window is displayed
	    // -------------------------------------------------------------------------
	    popupWindow.setAnimationStyle (R.style.PopupWindowAnimation);
	    // -------------------------------------------------------------------------
	    // 02/03/2014 ECU display the defined popup window in the middle of the
	    //                screen
	    // -------------------------------------------------------------------------
	    popupWindow.showAtLocation (theView,Gravity.CENTER,0,0);	
	    // -------------------------------------------------------------------------
	    // 17/03/2014 ECU handle the timeout option
	    // -------------------------------------------------------------------------
	    if (theTimeOut != StaticData.NO_RESULT)
	    {
	    	// ---------------------------------------------------------------------
	    	// 18/03/2014 ECU make the status field visible
	    	// ---------------------------------------------------------------------
	    	timeoutView.setVisibility (View.VISIBLE);
	    	// ---------------------------------------------------------------------
	    	// 18/03/2014 ECU display the initial status message
	    	// ---------------------------------------------------------------------
	    	timeoutView.setText ("\n"  + (theTimeOut/1000) + " seconds remaining");
	    	// ---------------------------------------------------------------------
		    // 02/03/2014 ECU display the defined popup window in the middle of the
		    //                screen
	    	// ---------------------------------------------------------------------
		    popupWindow.showAtLocation (theView,Gravity.CENTER,0,0);	
	    	// ---------------------------------------------------------------------
	    	// 17/03/2014 ECU set up a thread to handle the timeout
	    	// ---------------------------------------------------------------------
	        final Handler handler = new Handler();
		    
		    Runnable runnable = new Runnable() 
		    {
		    	// ---------------------------------------------------------------------
		    	// 17/03/2014 ECU the timeout is supplied in milliseconds but want to
		    	//                work in seconds. The '+ 1' is to get the initial display
		    	//                correct
		    	// ---------------------------------------------------------------------
		    	private int theSeconds = theTimeOut/1000 + 1;
		    	// ---------------------------------------------------------------------
		    	public void run() 
		    	{    	
		    		// -----------------------------------------------------------------
		    		// 17/03/2014 ECU loop for the whole timeout or until the user
		    		//                has pressed the button
		    		// -----------------------------------------------------------------
	          		while (theSeconds-- > 0 && popupWindow.isShowing()) 
	          		{	         		
	          			// -------------------------------------------------------------
	          			// 17/03/2014 ECU sleep for 1 second
	          			// -------------------------------------------------------------           			
	          			try
	          			{
	          				Thread.sleep (1000);
	          			}
	          			catch (InterruptedException theException)
	          			{         				
	          			}
	          			// -------------------------------------------------------------
	          			// 17/03/2014 ECU want to post the message to the queue
	          			// -------------------------------------------------------------
	          			handler.post (new Runnable ()             
	 	                {
	          				// ---------------------------------------------------------
	          				// 17/03/2014 ECU loop for the whole timeout period at which
	          				//                point the popupWindow is to be dismissed if
	          				//                it hasn't already been dismissed by the user
	          				//                pressing the button
	          				// ---------------------------------------------------------
	 	                    public void run() 
	 	                    {
	 	                    	if (theSeconds > 0)
	 	                    	{
	 	                    		// -------------------------------------------------
	 	                    		// 17/03/2014 ECU display the number of seconds
	 	                    		//                remaining
	 	                    		// 18/03/2014 ECU changed to use the textview rather
	 	                    		//                than the button
	 	                    		// -------------------------------------------------
	 	                    		timeoutView.setText ("\n"  + theSeconds + " second" +
	 	                    				((theSeconds > 1) ? "s" : "") +" remaining");
	 	                    		// -------------------------------------------------
	 	                    	}
	 	                    	else
	 	                    	{
	 	                    		// -------------------------------------------------
	 	                     		// 17/03/2014 ECU now act as if the user pressed the
	 	                    		//            	  button providing that that has not
	 	                    		//           	  already been done
	 	                    		// 05/12/2015 ECU was very occasionally getting a
	 	                    		//                NPE - no idea why so added the
	 	                    		//                try/catch
	 	                     		// -------------------------------------------------
	 	                    		try
	 	                    		{
	 	                    			if (popupWindow.isShowing())
	 	                    				popupWindow.dismiss();
	 	                    		}
	 	                    		catch (Exception theException)
	 	                    		{
	 	                    			
	 	                    		}
	 	                     		// -------------------------------------------------
	 	                    	}
	 	                    }
	 	                });
	          		}	  	            	
		        }
		    };
		    // -------------------------------------------------------------------------
		    // 17/03/2014 ECU start the thread
		    // -------------------------------------------------------------------------
		    new Thread (runnable).start();
		    // -------------------------------------------------------------------------
	    	// ---------------------------------------------------------------------
	    }
	    else
	    {
	    	// ---------------------------------------------------------------------
		    // 02/03/2014 ECU display the defined popup window in the middle of the
		    //                screen
	    	// ---------------------------------------------------------------------
		    popupWindow.showAtLocation(theView,Gravity.CENTER,0,0);	
		    // ---------------------------------------------------------------------	    
	    }
	    // -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void popToast (View theView,
		     					 String theString,
		     					 String theButtonText,
		     					 final int theTimeOut,
		     					 boolean theScrollableFlag)
	{
		// -------------------------------------------------------------------------
		// 10/06/2016 ECU was the old 'main' method but changed when context and
		//                method added to the main method (see above)
		// -------------------------------------------------------------------------
		popToast (MainActivity.activity,theView,theString,theButtonText,theTimeOut,theScrollableFlag,null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void popToast (View theView,String theString)
	{
		// -------------------------------------------------------------------------
		// 17/03/2014 ECU created to display a 'popToast' with key input and with
		//                no timeout
		// 18/03/2014 ECU include the 'null' argument as the button legend is not to
		//                be changed
		// 31/04/2016 ECU added false as the scrollable flag
		// -------------------------------------------------------------------------
		popToast (theView,theString,null,StaticData.NO_RESULT,false);
		// -------------------------------------------------------------------------
	}
	/* ----------------------------------------------------------------------------- */
	public static void popToast (View theView,String theString,boolean theScrollableFlag)
	{
		// -------------------------------------------------------------------------
		// 17/03/2014 ECU created to display a 'popToast' with key input and with
		//                no timeout
		// 18/03/2014 ECU include the 'null' argument as the button legend is not to
		//                be changed
		// 31/04/2016 ECU pass through the scrollable flag
		// -------------------------------------------------------------------------
		popToast (theView,theString,null,StaticData.NO_RESULT,theScrollableFlag);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void popToastFullScreen (int theDrawableID)
	{
		// -------------------------------------------------------------------------
		// 31/01/2017 ECU created to just display the drawable identified by the
		//                argument
		// -------------------------------------------------------------------------
		try
		{
			LayoutInflater  layoutInflator = (LayoutInflater)MainActivity.activity.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
		
			View layout = layoutInflator.inflate (R.layout.activity_help,
		                               (ViewGroup) MainActivity.activity.findViewById (R.id.help_layout));
			// ---------------------------------------------------------------------
			// 31/01/2017 ECU display the specified image
			// ---------------------------------------------------------------------
			((ImageView) layout.findViewById (R.id.help_image)).setImageResource (theDrawableID);
			// ---------------------------------------------------------------------
			// 31/01/2017 ECU get a fresh Toast that will be modified
			// ---------------------------------------------------------------------
			Toast toast = new Toast (MainActivity.activity);
			// ---------------------------------------------------------------------
			// 31/01/2017 ECU customise the Toast
			// ---------------------------------------------------------------------
			toast.setView (layout);
			toast.setDuration(Toast.LENGTH_LONG);	
			toast.show ();
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{	
		} 
	}
	// -----------------------------------------------------------------------------
	public static void popToastFullScreen (String theString)
	{
		// -------------------------------------------------------------------------
		// 31/01/2017 ECU created to display the specified text at the centre of the
		//                screen using the Android Toast utility
		// -------------------------------------------------------------------------
		try
		{
			LayoutInflater  myInflator = (LayoutInflater)MainActivity.activity.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
		
			View layout = myInflator.inflate (R.layout.poptoast_text,
		                               (ViewGroup) MainActivity.activity.findViewById (R.id.poptoast_text_layout));
			// ---------------------------------------------------------------------
			// 31/01/2017 ECU display the specified text
			// ---------------------------------------------------------------------
			((TextView) layout.findViewById (R.id.poptoast_textview)).setText(theString);
			// ---------------------------------------------------------------------
			Toast toast = new Toast (MainActivity.activity);
			toast.setGravity (Gravity.CENTER_VERTICAL, 0, 0);
			// ---------------------------------------------------------------------
			// 31/01/2017 ECU now invoke Toast to display the information
			// ---------------------------------------------------------------------
			toast.setView (layout);
			toast.show ();	
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
		} 
	}	
	/* ----------------------------------------------------------------------------- */
	/* ----------------------------------------------------------------------------- */
	@SuppressLint ("InflateParams") 
	public static void popToast (View theView,
			                     String theString,
			                     final Method theYesMethod,
			                     final Method theNoMethod)
	{
		// -------------------------------------------------------------------------
		// 02/03/2014 ECU this method uses a 'popup' window to provide a
		//                'toast like' display which has a button for clearing
		//                it from the screen
		// 17/03/2014 ECU handle the timeout which is supplied in 
		//                'milliseconds'
		// -------------------------------------------------------------------------
		
		LayoutInflater layoutInflater = (LayoutInflater)MainActivity.activity.getSystemService(LAYOUT_INFLATER_SERVICE);  
		
	    final View popupView = layoutInflater.inflate (R.layout.confirmation_popup, null);
	    
	    final PopupWindow popupWindow = new PopupWindow (popupView,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);  
	    // -------------------------------------------------------------------------         
	    // 02/03/2014 ECU define the views that will be updated
	    // -------------------------------------------------------------------------
	    		Button 		buttonNo	 	= (Button)  popupView.findViewById (R.id.popup_button_no);
	    		Button 		buttonYes	 	= (Button)  popupView.findViewById (R.id.popup_button_yes);
	    		TextView 	textView		= (TextView)popupView.findViewById (R.id.popup_text);
	    		TextView 	copyRight 		= (TextView)popupView.findViewById (R.id.popup_copyright);
	    // -------------------------------------------------------------------------	
	    // 02/03/2014 ECU update the text information
	    // -------------------------------------------------------------------------
	    textView.setText (theString);
	    copyRight.setText (PublicData.copyrightMessage);
	    // -------------------------------------------------------------------------
	    // 02/03/2014 ECU display the listener that will be used to remove the display
	    // -------------------------------------------------------------------------
	    // -------------------------------------------------------------------------
	    buttonNo.setOnClickListener(new Button.OnClickListener()
	    {
	    	@Override
	    	public void onClick(View view) 
	    	{
	    		try 
	    		{
	    			if (theNoMethod != null)
	    			{
	    				theNoMethod.invoke (null, new Object [] {0});
	    			}
	    		}
	    		catch (Exception theException) 
	    		{
	    			
	    		} 
	    		// -----------------------------------------------------------------
	    		// 02/03/2014 ECU remove the popup display from the screen
	    		// -----------------------------------------------------------------
	    		popupWindow.dismiss();
	    	}
	    });
	 // ----------------------------------------------------------------------------
	    buttonYes.setOnClickListener(new Button.OnClickListener()
	    {
	    	@Override
	    	public void onClick(View view) 
	    	{
	    		try 
	    		{
	    			if (theYesMethod != null)
	    			{
	    				theYesMethod.invoke (null, new Object [] {0});
	    			}
	    		}
	    		catch (Exception theException) 
	    		{
	    			
	    		} 
	    		// -----------------------------------------------------------------
	    		// 02/03/2014 ECU remove the popup display from the screen
	    		// -----------------------------------------------------------------
	    		popupWindow.dismiss ();
	    	}
	    });
	    // -------------------------------------------------------------------------
	    // 02/03/2014 ECU display the defined popup window in the middle of the
	    //                screen
	    // -------------------------------------------------------------------------
	    popupWindow.showAtLocation(theView,Gravity.CENTER,0,0);	   
	}
	/* ----------------------------------------------------------------------------- */
	/* ----------------------------------------------------------------------------- */
	private static void popToastWait (final Toast theToast,final int theNumberOfRepeats)
	{
		// -------------------------------------------------------------------------
		// 11/09/2013 ECU created 
		//            ECU will just repeatedly show 'toast' for the specified
		//                number of times
		// -------------------------------------------------------------------------
			
	    Thread thread = new Thread() 
	    {
	    	int theCounter = theNumberOfRepeats;
	    	
	    	public void run() 
	    	{
	            try 
	            {
	            	while (theCounter-- > 0) 
	                {
	            		// ---------------------------------------------------------
	            		// 11/09/2013 ECU display the supplied toast
	            		// ---------------------------------------------------------
	            		theToast.show();
	            		// ---------------------------------------------------------
	            		// 11/09/2013 ECU wait until the displayed toast disappears
	            		// ---------------------------------------------------------
	            		while (theToast.getView().isShown())
	            		{
	            			// -----------------------------------------------------
	            			// 11/09/2013 ECU just wait a short time
	            			// -----------------------------------------------------
	            			sleep (100);
	            		}
	                }
	            }
	            catch (Exception theException) 
	            {
	            	Log.e(TAG,"popToastWait:" + theException);
	            }
	    	}
	    };
	    // -------------------------------------------------------------------------
	    // 11/09/2013 ECU start up the thread
	    // -------------------------------------------------------------------------
	    thread.start();
	}
	/* ============================================================================= */
	public static void popToastAndLog (String theMessage)
	{
		// -------------------------------------------------------------------------
		// 26/10/2014 ECU added to display the toast and also log to the project
		//                file
		// -------------------------------------------------------------------------
		popToast (theMessage);
		// -------------------------------------------------------------------------
		// 26/10/2014 ECU log the message to the project log
		// -------------------------------------------------------------------------
		LogToProjectFile (TAG,theMessage,false);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void popToastAndSpeak (String theMessage)
	{
		// -------------------------------------------------------------------------
		// 14/10/2014 ECU this method will display the specified message via 'toast'
		//                and then speak the message
		// -------------------------------------------------------------------------
		popToastAndSpeak (theMessage,false);
	}
	// -----------------------------------------------------------------------------
	public static void popToastAndSpeak (String theMessage,boolean theFlag)
	{
		// -------------------------------------------------------------------------
		// 14/10/2014 ECU this method will display the specified message via 'toast'
		//                and then speak the message
		// -------------------------------------------------------------------------
		popToast (theMessage,theFlag);
		SpeakAPhrase (MainActivity.activity,theMessage);
	}
	// -----------------------------------------------------------------------------
	public static void popToastAndSpeak (String theMessage,boolean theCentringFlag,int theDuration)
	{
		// -------------------------------------------------------------------------
		// 02/05/2015 ECU this method will display the specified message via 'toast'
		//                and then speak the message
		// -------------------------------------------------------------------------
		popToast (theMessage,theCentringFlag,theDuration);
		SpeakAPhrase (MainActivity.activity,theMessage);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void popToastAndSpeak (Activity theActivity,View theView,String theMessage,Method theClickMethod)
	{
		// -------------------------------------------------------------------------
		// 09/06/2016 ECU created to display a 'toast' that the user needs to respond
		//                to and speak the message
		// 10/06/2016 ECU changed to add the activity and click method as arguments
		//                and to call the new 'main' method
		// -------------------------------------------------------------------------
		popToast (theActivity,theView,theMessage,null,StaticData.NO_RESULT,false,theClickMethod);
		// -------------------------------------------------------------------------
		// 09/06/2016 ECU speak the message
		// 10/06/2016 ECU changed from MainActivity.activity
		// -------------------------------------------------------------------------
		SpeakAPhrase (theActivity,theMessage);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean processAnAction (Context theContext,String [] theActionArguments)
	{
		// -------------------------------------------------------------------------
		// 03/06/2016 ECU created to process an individual action depending on
		//                the supplied arguments
		//
		//                  theActionArguments [0]      defines the action
		//                                     [1 .. n]	optional arguments needed
		// 												by the action
		//			  ECU IMPORTANT - the following code used to be in 'actionHandler'
		//                =========   so the comments are dated for that time
		//            ECU changed to boolean so that can indicate whether need to wait
		//                until activities started by this method have finished
		// 13/07/2016 ECU added NOTIFICATION
		// 04/08/2016 ECU added NAMED_ACTION
		// -------------------------------------------------------------------------
		boolean	finishAction	= false;
		// -------------------------------------------------------------------------
		if (theActionArguments [0].equalsIgnoreCase (StaticData.ACTION_DESTINATION_WEMO))
		{
			// ---------------------------------------------------------------------
			// 18/03/2015 ECU if this device cannot control WeMo devices
			//                then need to find a device that can
			// ---------------------------------------------------------------------
			if (WeMoActivity.serviceRunning)
			{
				// -----------------------------------------------------------------
				// 18/03/2015 ECU this device can control WeMo devices so
				//                can handle locally
				// -----------------------------------------------------------------
			
				// -----------------------------------------------------------------
				// 18/03/2015 ECU have commands that have to be processed by
				//                the Belkin WeMo software
				// -----------------------------------------------------------------
				WeMoActivity.voiceCommands (theActionArguments [1]);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 18/03/2015 ECU will need to send message to WeMo server
				//                to action
				// -----------------------------------------------------------------
				if (PublicData.wemoServer != null)
				{
					Utilities.sendSocketMessageSendStringAndWait (theContext, PublicData.wemoServer, 
							PublicData.socketNumber, "command " + StaticData.ACTION_DESTINATION_WEMO + " " + theActionArguments [1]);
				}
			}
		}
		// -------------------------------------------------------------------------
		else
		if (theActionArguments [0].equalsIgnoreCase (StaticData.ACTION_DESTINATION_SCREEN))
		{
			// ---------------------------------------------------------------------
			// 03/05/2015 ECU the only actions for the screen are 'on' or
			//                'off' - the action is held in theActionArguments [1]
			// ---------------------------------------------------------------------
			if (theActionArguments [1].equalsIgnoreCase ("on"))
			{
				// -----------------------------------------------------------------
				// 03/05/2015 ECU want to set screen to 'on' state
				// -----------------------------------------------------------------
				GridActivity.activity.getWindow().addFlags (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				// -----------------------------------------------------------------
			}
			else
			if (theActionArguments [1].equalsIgnoreCase ("off"))
			{
				// -----------------------------------------------------------------
				// 03/05/2015 ECU want to set screen to 'off' state
				// -----------------------------------------------------------------
				GridActivity.activity.getWindow().clearFlags (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		else
		if (theActionArguments [0].equalsIgnoreCase (StaticData.ACTION_DESTINATION_PHONE))
		{
			// ---------------------------------------------------------------------
			// 27/11/2015 ECU handle the 'phone' command - the required
			//                number follows the command
			// ---------------------------------------------------------------------
			makePhoneCall (theContext,theActionArguments [1]);
			// ---------------------------------------------------------------------
		}	
		else
		if (theActionArguments [0].equalsIgnoreCase (StaticData.ACTION_DESTINATION_PLAY))
		{
			// ---------------------------------------------------------------------
			// 06/06/2016 ECU indicate that want the music to finish before moving to
			//                the next action
			// ---------------------------------------------------------------------
			finishAction = true;
			// ---------------------------------------------------------------------
			// 27/11/2015 ECU handle the 'play' command - the required
			//                file, which must be in the project folder'
			//                follows the command
			// 03/06/2016 ECU add the 'true' to indicate that being called by an
			//                'action'
			// 04/06/2016 ECU check if need to add the 'project folder' ... if the
			//                argument starts with ROOT_DIRECTORY then it is the whole path -
			//                if not then it is relative to the project folder
			// 06/06/2016 ECU corrected an error because the arguments were wrong and
			//                the incorrect method was being called - the one that
			//                waits for the music to finish
			// ---------------------------------------------------------------------
			if (theActionArguments [1].startsWith (StaticData.ROOT_DIRECTORY))
				PlayAFile (theContext,theActionArguments [1],StaticData.NO_RESULT,true);
			else
				PlayAFile (theContext,PublicData.projectFolder + theActionArguments [1],StaticData.NO_RESULT,true);
			// ---------------------------------------------------------------------
		}	
		else
		if (theActionArguments [0].equalsIgnoreCase (StaticData.ACTION_DESTINATION_SPEAK))
		{
			// ---------------------------------------------------------------------
			// 27/11/2015 ECU handle the 'speak' command - the required
			//                phrase follows the command
			// ---------------------------------------------------------------------
			// 03/06/2016 ECU indicate that want to wait till this action has finished
			// ---------------------------------------------------------------------
			finishAction = true;
			// ---------------------------------------------------------------------
			// 27/11/2015 ECU flush out anything that is currently being spoken
			// ---------------------------------------------------------------------
			TextToSpeechService.Flush ();
			// ---------------------------------------------------------------------
			// 26/03/2016 ECU changed to use SpeakAPhraseWithDelays instead
			//                of SpeakAPhrase because the normal delay when
			//                a full stop in encountered was not long enough
			//                and 'sounded wrong'
			// ---------------------------------------------------------------------
			if (theActionArguments.length == 2)
			{
				// -----------------------------------------------------------------
				// 27/11/2015 ECU the first argument is the phrase to speak
				// 26/03/2016 ECU changed to ..Delays (see note above)
				// 03/06/2016 ECU added 'true' to indicate being called within an
				//                action
				// -----------------------------------------------------------------
				SpeakAPhraseWithDelays (theContext,theActionArguments [1],true);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 27/11/2015 ECU the first argument is the required silence
				//                period in milliseconds
				//                the second argument is the phrase to speak
				// -----------------------------------------------------------------
				try
				{
					// -------------------------------------------------------------
					// 27/11/2015 ECU parseInt can throw an exception if the
					//                conversion fails
					// -------------------------------------------------------------
					TextToSpeechService.Silence (Integer.parseInt(theActionArguments [1]));
					// -------------------------------------------------------------
				}
				catch (Exception theException)
				{
					
				}
				// -----------------------------------------------------------------
				// 18/03/2015 ECU speak the supplied phrase
				// 26/03/2016 ECU changed to ..Delays (see note above)
				// 03/06/2016 ECU added 'true' to indicate being called within an
				//                action
				// -----------------------------------------------------------------
				SpeakAPhraseWithDelays (theContext,theActionArguments [2],true);
				// -----------------------------------------------------------------
			}	
			// ---------------------------------------------------------------------
		}	
		else
		if (theActionArguments [0].equalsIgnoreCase (StaticData.ACTION_DESTINATION_EMAIL))
		{
			// ---------------------------------------------------------------------
			// 27/11/2015 ECU handle the 'email' command - the required
			//                destination follows the command
			//                1 ...... address(es)
			//                2 ...... subject
			//                3 ...... message
			// ---------------------------------------------------------------------
			SendEmailMessage (theContext,theActionArguments [1],theActionArguments [2], theActionArguments [3],null,null);
			// ---------------------------------------------------------------------
		}
		else
		if (theActionArguments [0].equalsIgnoreCase (StaticData.ACTION_DESTINATION_TIME))
		{
			// ---------------------------------------------------------------------
			// 02/05/2016 ECU created to speak the current time
			// ---------------------------------------------------------------------
			SpeakingClock ();
			// ---------------------------------------------------------------------
		}
		else
		if (theActionArguments [0].equalsIgnoreCase (StaticData.ACTION_DESTINATION_TRACK))
		{
			// ---------------------------------------------------------------------
			// 28/11/2015 ECU handle the 'track' command - the argument can
			//                be on or off
			// ---------------------------------------------------------------------
			if (theActionArguments [1].equalsIgnoreCase ("on"))
			{
				//------------------------------------------------------------------
				// 28/11/2015 ECU if 'on' then the next argument is the
				//                address(es) where data is sent
				// 06/12/2015 ECU add the check on length to ensure that
				//                the email address has been provided
				// -----------------------------------------------------------------
				if (theActionArguments.length == 3)
				{
					// -------------------------------------------------------------
					// 06/12/2015 ECU switch on tracking and set the email
					//                address
					// -------------------------------------------------------------
					PublicData.storedData.panicAlarm.tracking 		= true;
					PublicData.storedData.panicAlarm.trackingEmail 	= theActionArguments [2];
					// -------------------------------------------------------------
					// 28/11/2015 ECU and start up the tracking activity
					// -------------------------------------------------------------
					Intent localIntent = new Intent (theContext,LocationActivity.class);
					localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					theContext.startActivity (localIntent);
					// -------------------------------------------------------------
				}
      	    	// -----------------------------------------------------------------
			}
			else
			if (theActionArguments [1].equalsIgnoreCase ("off"))
			{
				// -----------------------------------------------------------------
				// 28/11/2015 ECU switch off the tracking
				// -----------------------------------------------------------------
				PublicData.storedData.panicAlarm.tracking 		= false;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}	
		else
		if (theActionArguments [0].equalsIgnoreCase (StaticData.ACTION_DESTINATION_SMS))
		{
			// ---------------------------------------------------------------------
			// 11/12/2015 ECU handle the 'SMS' command - the required
			//                destination follows the command
			//                1 ...... phone number
			//                2 ...... message
			//            ECU put in check on length
			// ---------------------------------------------------------------------
			if (theActionArguments.length == 3)
			{
				sendSMSMessage (theContext,theActionArguments[1],theActionArguments[2]);
			}
			// ---------------------------------------------------------------------
		}
		else
		if (theActionArguments [0].equalsIgnoreCase (StaticData.ACTION_DESTINATION_VIBRATE))
		{
			// ---------------------------------------------------------------------
			// 11/03/2016 ECU created to cause the device to vibrate for
			//                the specified period which is in milliseconds
			// 16/03/2016 ECU add the check on presence of vibrator
			// ---------------------------------------------------------------------
			try
			{
				Vibrator vibrator = (Vibrator) theContext.getSystemService (Context.VIBRATOR_SERVICE);
				// -----------------------------------------------------------------
				// 11/03/2016 ECU check if the vibrator service exists
				// 16/03/2016 ECU check that there is a physical vibrator
				// -----------------------------------------------------------------
				if (vibrator != null && vibrator.hasVibrator())
				{
					// -------------------------------------------------------------
					// 11/03/2016 ECU vibrator exists so can perform the
					//                action
					// -------------------------------------------------------------
					vibrator.vibrate (Integer.parseInt(theActionArguments [1]));
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			catch (Exception theException)
			{
				// -----------------------------------------------------------------
				// 11/03/2016 ECU only likely exception is if the parsing fails
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		else
		if (theActionArguments [0].equalsIgnoreCase (StaticData.ACTION_DESTINATION_VIDEO))
		{
			// ---------------------------------------------------------------------
			// 21/05/2016 ECU created to play the video whose file name is
			//                supplied as the argument
			// 01/06/2016 ECU include the FINISH parameter to tell the
			//                viewer to 'finish' at the end of the video
			// 03/06/2016 ECU indicate that processing of the video is to be completed
			//                before going to the next action
			// ---------------------------------------------------------------------
			finishAction = true;
			// ---------------------------------------------------------------------
			Intent localIntent = new Intent (theContext,VideoViewer.class);
			localIntent.putExtra (StaticData.PARAMETER_FILE_NAME,PublicData.projectFolder + theActionArguments [1]);
			localIntent.putExtra (StaticData.PARAMETER_FINISH,true);
			// ---------------------------------------------------------------------
			// 03/06/2016 ECU declare the method that will be called when the video
			//                finishes
			// ---------------------------------------------------------------------
			localIntent.putExtra (StaticData.PARAMETER_METHOD,new MethodDefinition<VideoViewer> (VideoViewer.class,"videoFinished"));
			// ---------------------------------------------------------------------
			localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
			theContext.startActivity (localIntent); 
			// ---------------------------------------------------------------------
		}
		else
		if (theActionArguments [0].equalsIgnoreCase (StaticData.ACTION_DESTINATION_NOTIFICATION))
		{
			// ---------------------------------------------------------------------
			// 13/07/2016 ECU created to handle the NOTIFICATION action
			// ---------------------------------------------------------------------
			NotificationMessage.Add (theActionArguments [1]);
			// ---------------------------------------------------------------------
		}
		else
		if (theActionArguments [0].equalsIgnoreCase (StaticData.ACTION_DESTINATION_NAMED_ACTION))
		{
			// ---------------------------------------------------------------------
			// 04/08/2016 ECU created to handle the NAMED_ACTION action - the name
			//                to be used iarguments element [1] of the 
			// ---------------------------------------------------------------------
			String localActions = NamedAction.getActions (theActionArguments [1]);
			// ---------------------------------------------------------------------
			// 04/08/2016 ECU the above method returns the actions to be obeyed
			//                or 'null' if the name does not exist
			// ---------------------------------------------------------------------
			if (localActions != null)
			{
				Message localMessage = PublicData.messageHandler.obtainMessage (StaticData.MESSAGE_PROCESS_ACTIONS_IMMEDIATELY, 
																				localActions.split (StaticData.ACTION_SEPARATOR));
				PublicData.messageHandler.sendMessage (localMessage);  
				// -----------------------------------------------------------------
				// 04/08/2016 ECU indicate that not finished yet
				// -----------------------------------------------------------------
				finishAction = true;
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 04/08/2016 ECU the requested 'named action' does not exist so
				//                log the fact and then do nothing
				// -----------------------------------------------------------------
				Utilities.LogToProjectFile (TAG, "NamedAction : '" + theActionArguments [1] + "' not found",true);
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 03/06/2016 ECU return with the 'finish action' state
		// -------------------------------------------------------------------------
		return finishAction;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void processCommandString (Context theContext,String theString,BufferedReader theInput,PrintWriter theOutput )
	{
		// -------------------------------------------------------------------------
		// 03/07/2013 ECU declare the commands - WILL MOVE WHEN WORKING
		// 24/07/2013 ECU add 'phone' as an option
		// 25/07/2013 ECU add 'message' as an option
		// 26/07/2013 ECU add 'locate' as an option
		// 29/07/2013 ECU add 'receive' and 'transmit' options
		// 03/03/2014 ECU changed the creation of the 'Commands' array
		// 18/03/2015 ECU added the WeMo command
		// 29/11/2015 ECU added the cancel phone call
		//            ECU changed to use StaticData
		// 20/01/2016 ECU took out COMPATABILITY because handled in a cleaner way
		//                now
		// -------------------------------------------------------------------------
		Commands [] theCommands = {
									new Commands ("get",1),
									new Commands ("help",0),
									new Commands (StaticData.SERVER_COMMAND_PHONE,1),
									new Commands (StaticData.SERVER_COMMAND_MESSAGE,1),
									new Commands (StaticData.SERVER_COMMAND_LOCATE,0),
									new Commands (StaticData.ACTION_DESTINATION_WEMO,2),
									new Commands (StaticData.SERVER_COMMAND_CANCEL_CALL,0)
								  };
		// -------------------------------------------------------------------------
		// 02/07/2013 ECU the structure of the incoming string is
		//				  COMMAND <command> <parameters associated with command>
		//
		// -------------------------------------------------------------------------
		String [] theWords = theString.split(" ");
		
		if (theWords[0].equalsIgnoreCase (StaticData.SERVER_COMMAND))
		{
			// ---------------------------------------------------------------------
			// 03/07/2013 ECU check for a command
			// 06/01/2016 ECU changed to use a string list rather than a single 
			//                string
			// ---------------------------------------------------------------------
			for (int theIndex = 0; theIndex < theCommands.length; theIndex++)
			{
				if ((theWords.length == theCommands[theIndex].numberOfParameters + 2) 
						&& theWords[1].equalsIgnoreCase(theCommands[theIndex].commandString))
				{
					switch (theIndex)
					{
						// ---------------------------------------------------------
						case 0:  	 // get
							theOutput.println ("You want to get file <" + PublicData.projectFolder + theWords[2] + ">\r");
							theOutput.println (new String(readAFile(PublicData.projectFolder + theWords[2])) + "\r");
							break;
						// ---------------------------------------------------------
						case 1:		// help
							theOutput.println ("Help for Telnet Commands\r");
							break;
						// ---------------------------------------------------------
						case 2:		// phone
							PublicData.stringsToProcess.add (theString);
							break;
						// ---------------------------------------------------------
						case 3:		// message
							PublicData.stringsToProcess.add (theString);
							break;
						// ---------------------------------------------------------
						case 4:		// locate
							PlayAFile (theContext,PublicData.projectFolder + theContext.getString(R.string.locate_sound_file));
							break;
						// ---------------------------------------------------------
						case 5:		// Belkin WeMo handling
							PublicData.stringsToProcess.add (theString);
							break;
						// ---------------------------------------------------------
						case 6:		// cancel phone call
							PublicData.stringsToProcess.add (theString);
							break;
						// ---------------------------------------------------------
					}
				}
			}
		}
		else
		if (theWords[0].equalsIgnoreCase ("class"))
		{
			// ---------------------------------------------------------------------
			// 03/07/2013 ECU will put the code here to directly run a class
			// ---------------------------------------------------------------------
			// 03/07/2013 ECU the class will be started using the name that is
			//                supplied in theWords[1], i.e. what comes in is
			//
			//                command <name of class> <name of optional parameter> <data associated with parameter>
			// ---------------------------------------------------------------------
			Intent myIntent = new Intent();
			String thePackageName = theContext.getPackageName ();
		    myIntent.setClassName (thePackageName, thePackageName + "." + theWords[1]);
		    myIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		    // ---------------------------------------------------------------------
		    // 30/05/2013 ECU if length is >2 then pass the third word across as
		    //                an extra
		    // ---------------------------------------------------------------------
		    if (theWords.length == 4)
		    {
		    	myIntent.putExtra (theWords[2],theWords[3]);
		    }
		    theContext.startActivity(myIntent);
		}
		else
		{
			theOutput.println ("nothing to process\r");
		}	
	}
	// =============================================================================
	public static void processDetailsReceived (Context theContext,Devices theDetailsReceived)
	{
		// -------------------------------------------------------------------------
		boolean localFound	= false;
		// -------------------------------------------------------------------------
		// 20/03/2015 ECU created to process received details of a device
		// 22/03/2015 ECU changed the logic to accommodate the redefinition of
		//                deviceDetails to List<Devices>
		// 20/01/2016 ECU took out the check on compatible as with this a device
		//                with diboson not running would be discovered and registered
		//                as 'non-compatible' but when the app is started and it
		//                announces itself a new entry was being created in the device
		//                list rather than changing the existing entry
		//            ECU instead of checking 'name' use IPAddress because non-
		//                compatible devices with have a 'null' value
		// -------------------------------------------------------------------------
		if (PublicData.deviceDetails != null)
		{
			for (int theDevice = 0; theDevice < PublicData.deviceDetails.size(); theDevice++)
			{
				if (PublicData.deviceDetails.get(theDevice).IPAddress.equalsIgnoreCase(theDetailsReceived.IPAddress))
				{
					// -------------------------------------------------------------
					// 20/03/2015 ECU have a match so update the entry
					// -------------------------------------------------------------
					PublicData.deviceDetails.set (theDevice,theDetailsReceived);
					// -------------------------------------------------------------
					// 20/03/2015 ECU indicate that the device has been found
					//                and updated
					// -------------------------------------------------------------
					localFound = true;
					// ---------------------------------------------------------
					break;
				}
			}
		}
		// -------------------------------------------------------------------------
		// 20/03/2015 ECU check if a device was found - if not then add an entry
		//                -- needs changing into an ArrayList at some stage
		// -------------------------------------------------------------------------
		if (!localFound)
		{
			// ---------------------------------------------------------------------
			// 22/03/2015 ECU changed the way to add the new details now that the
			//                deviceDetails is an List<Devices>
			// ---------------------------------------------------------------------
			if (PublicData.deviceDetails == null)
			{
				// -----------------------------------------------------------------
				// 22/03/2015 ECU no list exists so create one
				// -----------------------------------------------------------------
				PublicData.deviceDetails 		= new ArrayList<Devices>();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 22/03/2015 ECU changed the logic to accommodate the redefinition of
			//                deviceDetails to List<Devices>
			//            ECU add the new device into the array
			// ---------------------------------------------------------------------
			PublicData.deviceDetails.add (theDetailsReceived);	
			// ---------------------------------------------------------------------
			// 20/03/2015 ECU if a new device has been added then let this device
			//                issue a 'hello' to make sure every device is in sync
			// ---------------------------------------------------------------------
			Devices.sendHelloMessage ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 20/03/2015 ECU make sure the device details are written to disk
		// -------------------------------------------------------------------------
		Devices.writeToDisk (theContext);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	/* ===================================================================== */
	// 04/03/2014 ECU deleted following the change to use the AlarmManager
	// ---------------------------------------------------------------------
	//public static void processMedicationDetails (Context theContext)
	//{
	//	
	//	// 22/06/2013 ECU created - responsible for the timed processing of medication
	//	//                details
	//	// 23/06/2013 ECU put in debug mode
	//	// 16/01/2014 ECU changed to reflect the use of List<>
	//	
	//	if (MainActivity.debugMode)
	//		Toast.makeText(theContext, "processMedicationDetails", Toast.LENGTH_LONG).show();
	//	
	//	Calendar currentCalendar	= Calendar.getInstance();
	//	int theDay					= currentCalendar.get(Calendar.DAY_OF_WEEK);
	//
	//	// 22/06/2013 ECU adjust because I have Monday as 0 instead of Sunday as 1
	//	
	//	theDay -= 2;
	//	if (theDay < 0) theDay += 7;
	//	
	//	int theHour					= currentCalendar.get(Calendar.HOUR_OF_DAY);
	//	int theMinute               = currentCalendar.get(Calendar.MINUTE);
	//	   
	//	
	//	// 22/06/2013 ECU now process the medication details
	//	// 16/01/2014 ECU changed to reflect that medicationDetails altered to be List<>
	//		
	//	for (int medicationIndex=0; medicationIndex < MainActivity.medicationDetails.size(); medicationIndex++)
	//	{
	//		for (int doseDailyTimesIndex = 0; 
	//				 doseDailyTimesIndex < MainActivity.medicationDetails.get (medicationIndex).dailyDoseTimes.length; 
	//				 doseDailyTimesIndex++)
	//		{
	//			if (MainActivity.medicationDetails.get (medicationIndex).dailyDoseTimes[doseDailyTimesIndex] != null)
	//			for (int doseTimesIndex = 0; 
	//					doseTimesIndex < MainActivity.medicationDetails.get (medicationIndex).dailyDoseTimes[doseDailyTimesIndex].doseTimes.size(); 
	//					doseTimesIndex++)
	//			{
	//				if (MainActivity.medicationDetails.get (medicationIndex).dailyDoseTimes[doseDailyTimesIndex].doseTimes.get(doseTimesIndex) != null)
	//				{
	//					// 23/06/2013 ECU put in debug mode
	//							
	//					if (MainActivity.debugMode)
	//						popToast (theContext,"Dose Time on day " + doseDailyTimesIndex + " is " + 
	//							MainActivity.medicationDetails.get (medicationIndex).dailyDoseTimes[doseDailyTimesIndex].doseTimes.get(doseTimesIndex).hours + ":" +
	//							MainActivity.medicationDetails.get (medicationIndex).dailyDoseTimes[doseDailyTimesIndex].doseTimes.get(doseTimesIndex).minutes  + "\n\n" +
	//						    "Checking Against = " + theDay + "  " + theHour +":" + theMinute);
	//							
	//					if (doseDailyTimesIndex == theDay &&
	//						MainActivity.medicationDetails.get (medicationIndex).dailyDoseTimes[doseDailyTimesIndex].doseTimes.get(doseTimesIndex).hours == theHour &&
	//						MainActivity.medicationDetails.get (medicationIndex).dailyDoseTimes[doseDailyTimesIndex].doseTimes.get(doseTimesIndex).minutes == theMinute)
	//					{
	//						// 22/06/2013 ECU some medication needs to be administered
	//							
	//						TimeForMedication (theContext,theHour,theMinute,medicationIndex,doseDailyTimesIndex,doseTimesIndex);
	//					}							
	//				}
	//			}					
	//		}
	//	}		
	//}
	// ---------------------------------------------------------------------
	// end of the 04/03/2014 ECU deletion
	// =============================================================================
	public static void ProcessTheObject (Context theContext,Object theObject)
	{
		// -------------------------------------------------------------------------
		// 27/02/2016 ECU created to process the specified object
		// -------------------------------------------------------------------------
		// 27/02/2016 ECU check if the remote controller object
		// -------------------------------------------------------------------------
		if (theObject instanceof RemoteControllerRequest)
		{
			// ---------------------------------------------------------------------
			// 27/02/2016 ECU convert to the correct format - make sure the service
			//                if running
			// ---------------------------------------------------------------------
			if (PublicData.blueToothService)
			{
				RemoteControllerRequest localRequest = (RemoteControllerRequest) theObject;
				// -----------------------------------------------------------------
				// 27/02/2016 ECU decide how to process the object
				// 28/02/2016 ECU changed to use 'type' to determine how the command
				//                is to be processed
				// -----------------------------------------------------------------
				if (localRequest.type != StaticData.NO_RESULT)
				{
					// -------------------------------------------------------------
					// 27/02/2016 ECU this command contains the name of the channel
					//                to be selected
					// -------------------------------------------------------------
					new BlueToothServiceUtilities (theContext,localRequest.type,localRequest.command);
				}
				else
				{
					// -------------------------------------------------------------
					// 27/02/2016 ECU call the handler for raw codes
					// -------------------------------------------------------------
					Message localMessage = BlueToothService.timerHandler.obtainMessage (StaticData.MESSAGE_DATA);
					localMessage.obj 	 = localRequest.command;
					BlueToothService.timerHandler.sendMessage (localMessage);
					// -------------------------------------------------------------
				}
					
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void processVoiceCommand (Context theContext, VoiceCommands theVoiceCommands, TextToSpeech theTextToSpeech)
	{
			Intent myIntent;
			String myIPAddress = null;
		
			switch (theVoiceCommands.commandToRun)
			{
				case StaticData.VOICE_COMMAND_PLAY_MUSIC:
					// -------------------------------------------------------------
					// 16/06/2013 ECU start the music player
					// -------------------------------------------------------------
					if (theVoiceCommands.commandData.equalsIgnoreCase("start"))
					{
						myIntent = new Intent (theContext,MusicPlayer.class);
						myIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP); 
						theContext.startActivity (myIntent);
					}
					else
					if (theVoiceCommands.commandData.equalsIgnoreCase("stop"))
					{
						if (PublicData.mediaPlayer != null && PublicData.mediaPlayer.isPlaying())
						{
							PublicData.mediaPlayer.stop();
							PublicData.musicPlayerData.tracksPlaying = false;
							PublicData.stopmpPlayer = true;
						}
					}
					else
					if (theVoiceCommands.commandData.equalsIgnoreCase("pause"))
					{
						if (PublicData.mediaPlayer != null)
						{
							PublicData.mediaPlayer.pause();
							PublicData.mediaPlayerPaused = true;
						}
					}
					else
					if (theVoiceCommands.commandData.equalsIgnoreCase("resume"))
					{
						if (PublicData.mediaPlayer != null)
						{
							PublicData.mediaPlayer.start();
							PublicData.mediaPlayerPaused = false;
						}
					}
					break;		
				case StaticData.VOICE_COMMAND_SHOW_PHOTOS:
					// -------------------------------------------------------------
					// 16/06/2013 ECU start the slide show
					// -------------------------------------------------------------
					myIntent = new Intent (theContext,SlideShowActivity.class);
	    			theContext.startActivity (myIntent);
	    			break;	
				case StaticData.VOICE_COMMAND_GAME_ONE:
					// -------------------------------------------------------------
					// 16/06/2013 ECU start the game number one
					// -------------------------------------------------------------
					myIntent = new Intent (theContext,GameOne.class);
	    			theContext.startActivity (myIntent);
	    			break;	
				case StaticData.VOICE_COMMAND_GAME_TWO:
					// -------------------------------------------------------------
					// 16/06/2013 ECU start the game number two
					// -------------------------------------------------------------
					myIntent = new Intent (theContext,GameTwo.class);
	    			theContext.startActivity (myIntent);
	    			break;	
				case StaticData.VOICE_COMMAND_TIME:
					// -------------------------------------------------------------
					// 16/06/2013 ECU speak the current time
					// -------------------------------------------------------------
					Calendar c = Calendar.getInstance();
					// -------------------------------------------------------------
					// 15/09/2013 ECU added Locale.UK
					// 10/11/2014 ECU changed to use Locale.getDefault instead of Locale.UK
					// -------------------------------------------------------------
				    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss E dd MMM yyyy",Locale.getDefault());
				    String formattedDate = df.format(c.getTime());
				    theTextToSpeech.speak(formattedDate,TextToSpeech.QUEUE_FLUSH, null);			
	    			break;
	    		// =================================================================
				case StaticData.VOICE_COMMAND_PHONE:
					makePhoneCall (theContext,theVoiceCommands.commandData);
					break;
				// =================================================================
				case StaticData.VOICE_COMMAND_CANCEL_CALL:
					// -------------------------------------------------------------
					// 29/11/2015 ECU cancel any phone call
					// -------------------------------------------------------------
					Utilities.cancelPhoneCall (theContext);
					break;
				// =================================================================
				case StaticData.VOICE_COMMAND_MAIL:
					// -------------------------------------------------------------
					// 09/06/2013 ECU added the call to the mail app
					// 09/11/2013 ECU changed code to use the method for returning intent
					// -------------------------------------------------------------
        			myIntent = Utilities.getPackageIntent(theContext, StaticData.PACKAGE_TYPE_MAIL);
        			
        			if (myIntent != null)
        				theContext.startActivity(myIntent);
        			break;
        		// =================================================================
				case StaticData.VOICE_COMMAND_INTERNET:
					myIntent = new Intent (theContext,DisplayURL.class);
					// -------------------------------------------------------------
					// 16/06/2013 ECU pass the required URL through
					// 19/12/2016 ECU changed to use _URL
					// -------------------------------------------------------------
					myIntent.putExtra (StaticData.PARAMETER_URL,theVoiceCommands.commandData);
					theContext.startActivity (myIntent);		
        			break;
        		// =================================================================
				case StaticData.VOICE_COMMAND_HELP:
					// -------------------------------------------------------------
					// 31/10/2015 ECU added the 'false' flag to indicate not to use
					//                the TTS service
					// -------------------------------------------------------------
					readAFile (PublicData.projectFolder + theContext.getString(R.string.help_file),theTextToSpeech,false);
					break;
				// ==================================================================
				case StaticData.VOICE_COMMAND_COMPASS:
					// -------------------------------------------------------------
					// 17/06/2013 ECU start the compass display
					// -------------------------------------------------------------
					myIntent = new Intent (theContext,CompassActivity.class);
	    			theContext.startActivity (myIntent);
					break;
				// =================================================================
				case StaticData.VOICE_COMMAND_GOOGLE:
					// -------------------------------------------------------------
					// 17/06/2013 ECU the basic url and search parameter are passed across
					// -------------------------------------------------------------
					String theURL = theVoiceCommands.commandData;
					// -------------------------------------------------------------
					// 17/6/2013 ECU now add in the words from the matching line
					//               not perfect but good enough
					//
					//               ignore the first word which is 'google' - add
					//               every other word separating them with a '+'
					// -------------------------------------------------------------
					for (int index=1; index<listWords.length; index++)
					{
						theURL += listWords[index] + "+";
					}
					// -------------------------------------------------------------
					// 17/06/2013 ECU now ask for the URL to be processed
					// 19/12/2016 ECU changed to use _URL
					// -------------------------------------------------------------
					myIntent = new Intent (theContext,DisplayURL.class);
					myIntent.putExtra (StaticData.PARAMETER_URL,theURL);
					theContext.startActivity (myIntent);		
        			break;
        		// =================================================================
				case StaticData.VOICE_COMMAND_REPEAT:
					if (PublicData.lastVoiceCommand != StaticData.NO_RESULT)
					{
						listWords = lastListWords;
						processVoiceCommand (theContext, PublicData.voiceCommands[PublicData.lastVoiceCommand],theTextToSpeech);
					}
					break;
				// =================================================================
				case StaticData.VOICE_COMMAND_WHOAMI:
					// -------------------------------------------------------------
					// 31/10/2015 ECU added the 'false' flag to indicate not to use
					//                the TTS service
					// -------------------------------------------------------------
					readAFile (PublicData.projectFolder + theContext.getString(R.string.whoami),theTextToSpeech,false);
					break;
				// =================================================================
				case StaticData.VOICE_COMMAND_COMMANDS:
					for (int commandIndex = 0; commandIndex < PublicData.voiceCommands.length; commandIndex++)
							PublicData.voiceCommands [commandIndex].Print(theContext,theTextToSpeech);
					break;
				// =================================================================
				case StaticData.VOICE_COMMAND_SPEAK:
					theTextToSpeech.speak (theVoiceCommands.commandData,TextToSpeech.QUEUE_FLUSH, null);
					break;
				// =================================================================
				case StaticData.VOICE_COMMAND_BIRTHDAY:
					// -------------------------------------------------------------
					// 22/04/2015 ECU changed to use the patient's date of birth
					// -------------------------------------------------------------
					String theString = workOutAge (theContext,
							PublicData.patientDetails.dateOfBirth,
							theContext.getString(R.string.age_message));
					theTextToSpeech.speak(theString,TextToSpeech.QUEUE_FLUSH, null);
 					break;
 				// =================================================================
				case StaticData.VOICE_COMMAND_SMS:
					// -------------------------------------------------------------
					// 20/06/2013 ECU added
					// -------------------------------------------------------------
					String theMessage = "";
					
					for (int index = 1; index < listWords.length; index++)
						theMessage += listWords[index] + " ";
					// -------------------------------------------------------------
					// 25/07/2013 ECU added the method which includes theContext
					// -------------------------------------------------------------
					sendSMSMessage (theContext,theContext.getString(R.string.mobile_number_ed),theMessage);
					break;
				case StaticData.VOICE_COMMAND_READ_FILE:
					// -------------------------------------------------------------
					// 20/06/2013 ECU added
					// 31/10/2015 ECU added the 'false' flag to indicate not to use
					//                the TTS service
					// -------------------------------------------------------------
					readAFile (theVoiceCommands.commandData,theTextToSpeech,false);
					break;
				case StaticData.VOICE_COMMAND_LISTEN_START:
					// -------------------------------------------------------------
					// 06/08/2013 ECU added
					// 06/08/2013 ECU want to send a socket message to remote device
					//				  to tell it to start streaming to me
					// 31/01/2015 ECU changed the method name
					// 16/03/2015 ECU use the streamingDestination if set
					// 31/05/2016 ECU changed to use resource
					// -------------------------------------------------------------
					if ((myIPAddress = PublicData.streamingDestination) == null)
						myIPAddress = findFirstDevice ();
					
					if (myIPAddress != null)
						socketMessagesSendMessageType (theContext,myIPAddress,PublicData.socketNumberForData, StaticData.SOCKET_MESSAGE_START_STREAM);
					else
						popToast (theContext,theContext.getString (R.string.no_device_to_listen_to));
					
					break;
				case StaticData.VOICE_COMMAND_LISTEN_STOP:
					// -------------------------------------------------------------
					// 06/08/2013 ECU added
					// 06/08/2013 ECU want to send a socket message to remote device
					//				  to tell it to stop streaming to me
					// 31/01/2015 ECU change the method name
					// -------------------------------------------------------------
					myIPAddress = findFirstDevice ();
					
					if (myIPAddress != null)
						socketMessagesSendMessageType (theContext,myIPAddress,PublicData.socketNumberForData, StaticData.SOCKET_MESSAGE_STOP_STREAM);
					else
						popToast (theContext,"No Device To be Actioned");
					
					break;
				case StaticData.VOICE_COMMAND_DEVICES:
					// -------------------------------------------------------------
					// 06/08/2013 ECU added
					// 22/03/2015 ECU changed the logic to accommodate the redefinition of
					//                deviceDetails to List<Devices>
					// -------------------------------------------------------------
					if (PublicData.deviceDetails != null)
					{
						theTextToSpeech.speak (PublicData.deviceDetails.size() + " devices have been found",TextToSpeech.QUEUE_ADD, null);
						
						for (int theDevice = 0; theDevice < PublicData.deviceDetails.size(); theDevice++)
						{
							theTextToSpeech.speak ("Device    " + theDevice + 
									"      has an IP address of      " + 
									PublicData.deviceDetails.get(theDevice).IPAddress,TextToSpeech.QUEUE_ADD, null);
							
							if (PublicData.deviceDetails.get(theDevice).name != null)
							{
								theTextToSpeech.speak("      and a plain text name of      " + 
										PublicData.deviceDetails.get(theDevice).name,TextToSpeech.QUEUE_ADD, null);
							}
						}	
					}
					else
					{
						theTextToSpeech.speak ("no devices have been found",TextToSpeech.QUEUE_FLUSH, null);
					}
					break;
				case StaticData.VOICE_COMMAND_BROADCAST:
					// -------------------------------------------------------------
					// 28/08/2013 ECU added
					//            ECU add the commandData option into the message
					// 09/04/2016 ECU changed from 'multicastMessage' which was
					//                misleading - also the case to _BROADCAST
					// -------------------------------------------------------------
					PublicData.broadcastMessage = theVoiceCommands.commandData;	
					break;
				case StaticData.VOICE_COMMAND_TELEVISION_ON:
					// -------------------------------------------------------------
					// 19/12/2013 ECU added
					// -------------------------------------------------------------
					myIntent = new Intent (theContext,Television.class);
					// -------------------------------------------------------------
					// 16/06/2013 ECU pass the parameters
					// -------------------------------------------------------------
					myIntent.putExtra(StaticData.PARAMETER_IR_DEVICE,Television.IR_TYPE_SAMSUNG);
					myIntent.putExtra(StaticData.PARAMETER_IR_CODE,Television.IR_POWER.function);
					
					theContext.startActivity (myIntent);		
        			break;
        		// -----------------------------------------------------------------
				case StaticData.VOICE_COMMAND_LAMP_ON:
					// -------------------------------------------------------------
					// 20/02/2015 ECU called to turn lamp on
					// -------------------------------------------------------------
					WeMoService.SetDeviceState ("lamp",true);
					break;
				// -----------------------------------------------------------------
				case StaticData.VOICE_COMMAND_LAMP_OFF:
					// -------------------------------------------------------------
					// 20/02/2015 ECU called to turn lamp off
					// -------------------------------------------------------------
					WeMoService.SetDeviceState ("lamp",false);
					break;
				// -----------------------------------------------------------------
					
			}
	}
	/* ============================================================================= */
	public static void ProjectLogClear ()
	{
		// -------------------------------------------------------------------------
		// 22/11/2014 ECU called to clear the Project log
		// -------------------------------------------------------------------------
		(new File (PublicData.projectLogFile)).delete(); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void promptMessage (String thePromptLegend,String thePromptBody,String theButtonLegend)
	{
		// -------------------------------------------------------------------------
		// 21/12/2015 ECU created to work in conjunction with the 'prompt' in 
		//                DialogueUtilities to request that the gridRefreshHandler
		//                actions the dialogue. This is for those activities or services
		//                which do not hava access to the user interface
		// -------------------------------------------------------------------------
		Bundle localBundle = new Bundle ();
		// -------------------------------------------------------------------------
		// 21/12/2015 ECU now store the data in the bundle
		// -------------------------------------------------------------------------
		localBundle.putString (StaticData.PARAMETER_PROMPT_BODY,thePromptBody);
		localBundle.putString (StaticData.PARAMETER_PROMPT_BUTTON,theButtonLegend);
		localBundle.putString (StaticData.PARAMETER_PROMPT_LEGEND,thePromptLegend);
		// -------------------------------------------------------------------------
		// 21/12/2015 ECU get a message and store the bundle in it as well as 
		//                specifying the type of message
		// -------------------------------------------------------------------------    
		Message localMessage = GridActivity.gridRefreshHandler.obtainMessage ();
		localMessage.what = StaticData.MESSAGE_PROMPT;
		localMessage.setData(localBundle);
		// -------------------------------------------------------------------------
		// 21/12/2015 ECU now send the message to the handler
		// -------------------------------------------------------------------------
		GridActivity.gridRefreshHandler.sendMessage (localMessage);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void proxitySensor (Context theContext,float theProximityValue,float theMaximumRange)
	{
		// -------------------------------------------------------------------------
		// 22/03/2016 ECU created to handle events from the proximity sensor
		// -------------------------------------------------------------------------
		if (theProximityValue != SensorService.proximityValue)
		{
			// ---------------------------------------------------------------------
			// 22/03/2016 ECU the value has changed so store it for later use
			// ---------------------------------------------------------------------
			SensorService.proximityValue = theProximityValue;
			// ---------------------------------------------------------------------
			Utilities.actionHandler(theContext,
									(theProximityValue == theMaximumRange) ? PublicData.storedData.proximityFarActionCommands 
																		   : PublicData.storedData.proximityNearActionCommands);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void readAFile (String theFile, TextToSpeech theTextToSpeech, boolean theServiceFlag)
	{
		// -------------------------------------------------------------------------
		// 31/10/2015 ECU process incoming lines to see if there are any specific
		//                commands to be obeyed or just speak the line
		//            ECU check if can use the TTS service
		//            ECU added 'theServiceFlag' - if this is true then use the
		//                TextToSpeechService (if it is available) - if false then
		//                use the supplied TextToSpeech engine
		//            ECU made 'public'
		// 			  ECU put in the check on whether theTextToSpeech is null - just
		//                in case
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// open the file for reading
			// ---------------------------------------------------------------------
			InputStream inputStream = new FileInputStream (theFile);
			// ---------------------------------------------------------------------
			// if file the available for reading
			// ---------------------------------------------------------------------
			if (inputStream != null) 
			{
				// -----------------------------------------------------------------
				// prepare the file for reading
				// -----------------------------------------------------------------
				InputStreamReader inputReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputReader);

				String inputLine;
				// -----------------------------------------------------------------
				// read every line of the file into the line-variable, on line at the time
				// -----------------------------------------------------------------
				do 
				{
					inputLine = bufferedReader.readLine();
					// -------------------------------------------------------------
					// 31/10/2015 ECU check if there are any embedded commands in
					//                the line - these are stored in StaticData
					//                and start with TTS_....
					// -------------------------------------------------------------
					if (inputLine.startsWith (StaticData.TTS_COMMENT))
					{
						// ---------------------------------------------------------
						// 31/10/2015 ECU this line is just a comment so no need
						//                to do anything
						// ---------------------------------------------------------
						
					}
					else
					if (inputLine.startsWith(StaticData.TTS_SILENCE))
					{
						// ---------------------------------------------------------
						// 31/10/2015 ECU a period of silence is required - the
						//                period required follows the command
						//
						//                the format is
						//                     TTS_SILENCE<period in milliseconds>
						//
						//			  ECU if the parseInt gets a format error then it
						//                will throw an exception - if this happens
						//                then ignore the line
						// ---------------------------------------------------------
						try
						{
							int localPeriod = Integer.parseInt (inputLine.replace(StaticData.TTS_SILENCE,""));
							if (theServiceFlag && TextToSpeechService.ready)
							{
								// -------------------------------------------------
								// 31/10/2015 ECU use the service to handle the silence
								// --------------------------------------------------
								TextToSpeechService.Silence (localPeriod);
								// --------------------------------------------------
							}
							else
							{
								// -------------------------------------------------
								// 31/10/2015 ECU use the supplied TTS engine
								// -------------------------------------------------
								theTextToSpeech.playSilence (localPeriod,TextToSpeech.QUEUE_ADD, null);
								// -------------------------------------------------
							}
						}
						catch (Exception theException)
						{
							
						}
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 31/10/2015 ECU just speak the line that has been read
						// ---------------------------------------------------------
						if (theServiceFlag && TextToSpeechService.ready)
						{
							// -------------------------------------------------
							// 31/10/2015 ECU use the service to handle the silence
							// --------------------------------------------------
							TextToSpeechService.SpeakAPhrase (inputLine);
							// --------------------------------------------------
						}
						else
						{
							// -------------------------------------------------
							// 31/10/2015 ECU use the supplied TTS engine
							// -------------------------------------------------
							theTextToSpeech.speak (inputLine,TextToSpeech.QUEUE_ADD, null);
							// -------------------------------------------------
						}
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// do something with the line 
					// -------------------------------------------------------------
				} while (inputLine != null);

				inputStream.close();
			}
		} 
		catch (Exception theException) 
		{
			// ---------------------------------------------------------------------
		    // print stack trace.
			// ---------------------------------------------------------------------
		} 	
	}
	/* ============================================================================= */
	public static byte [] readAFile (String theFileName)
	{
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 02/07/2013 ECU open to the specified file in 'read' mode
			// ---------------------------------------------------------------------
			RandomAccessFile theFile = new RandomAccessFile (theFileName, "r");
			// ---------------------------------------------------------------------
			// Get and check length
		    // ---------------------------------------------------------------------   	
			long theLongLength 	= theFile.length();
            int theLength 		= (int) theLongLength;
		    // ---------------------------------------------------------------------        
            // 02/07/2013 ECU check if file >= 2 GB
            // ---------------------------------------------------------------------		            
            if (theLength != theLongLength)
            {
            	// -----------------------------------------------------------------
            	// 09/11/2015 ECU before throwing an exception then close the file
            	// -----------------------------------------------------------------
            	theFile.close();
            	// -----------------------------------------------------------------
            	throw new IOException ("File size >= 2 GB");
            }
		    // ---------------------------------------------------------------------       
            // 02/07/2013 ECU read all the data in
		    // ---------------------------------------------------------------------        
            byte[] inputData = new byte [theLength];
            theFile.readFully (inputData);
            // ---------------------------------------------------------------------
            // 27/02/2014 ECU close the file 
            // ---------------------------------------------------------------------
            theFile.close();
            
            return inputData;
            // ---------------------------------------------------------------------
        }
		catch (IOException theException)
		{
			PublicData.datagram.Message("Exception : " + theException);
		}
		// -------------------------------------------------------------------------
		// 13/12/2016 ECU added because for very large files, usually encountered
		//                during cloning, then there may not be enough memory
		//                available for reading the whole file into memory
		// -------------------------------------------------------------------------
		catch (OutOfMemoryError theError) 
		{
			// ---------------------------------------------------------------------
			// 13/12/2016 ECU log the filename to the project file
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG,"OutOfMemory : " + theFileName,true);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		return null;
	}
	/* ============================================================================= */
	public static String ReadAFile (String theFileName)
	{
		// -----------------------------------------------------------------
		// 14/03/2014 ECU created to read the specified file from disk
		//                and then to do any of the required replacements
		//			  ECU the files to be read are in the 'dialogue folder'
		//            ECU change to use List
		// -----------------------------------------------------------------
		byte [] bytesRead = readAFile (PublicData.dialogueFolder + theFileName);
		// -----------------------------------------------------------------
		// 14/03/2014 ECU if there were no problems then try and do the
		//                replacements
		// ------------------------------------------------------------------
		if (bytesRead != null)
		{
			// --------------------------------------------------------------
			// 14/03/2014 ECU converted the bytes read into a string
			// --------------------------------------------------------------
			String stringRead = new String (bytesRead);
			// --------------------------------------------------------------
			// 14/03/2014 ECU now try and do the replacements
			// --------------------------------------------------------------
			if (PublicData.searchStringAndReplace.size() > 0)
			{
				for (int theIndex = 0; theIndex < PublicData.searchStringAndReplace.size(); theIndex++)
				{
					stringRead = PublicData.searchStringAndReplace.get(theIndex).UpdatedString(stringRead);
				}
			}
			// --------------------------------------------------------------
			// 14/03/2014 ECU return the modified string
			// --------------------------------------------------------------
			return stringRead;
			// --------------------------------------------------------------
		}
		else
		{
			// --------------------------------------------------------------
			// 14/03/2014 ECU a problems seems to have occurred
			// --------------------------------------------------------------
			return null;
			// --------------------------------------------------------------
		}
	}
	// =============================================================================
	public static List<String> readAFileAsList (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 11/04/2016 ECU created to read in the file with each line being an entry
		//                in a list
		// -------------------------------------------------------------------------
		List <String> localLines = new ArrayList <String> ();
		// -------------------------------------------------------------------------
		// 11/04/2016 ECU open to the file
		// -------------------------------------------------------------------------
		try 
		{
			InputStream inputStream = new FileInputStream (theFileName);
			// ---------------------------------------------------------------------
			// 11/04/2016 ECU check if file opened successfully
			// ---------------------------------------------------------------------
			if (inputStream != null) 
			{
				// -----------------------------------------------------------------
				// 11/04/2016 ECU get the input stream into correct format
				// -----------------------------------------------------------------
				InputStreamReader inputreader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader (inputreader);
				// -----------------------------------------------------------------
				// 11/04/2016 ECU loop for all lines in the file - creating a new
				//                entry in the list for each line
				// -----------------------------------------------------------------
				String localLine;
				do 
				{
			    	localLine = bufferedReader.readLine ();
			    	if (localLine != null)
			    		localLines.add(localLine);
				} while (localLine != null);
				// -----------------------------------------------------------------
				// 11/04/2016 ECU finally close the input stream
				// -----------------------------------------------------------------
				inputStream.close ();
				// -----------------------------------------------------------------
			}
		} 
		catch (Exception theException) 
		{
			// ---------------------------------------------------------------------
			// 11/04/2016 ECU an exception occurred
			// ---------------------------------------------------------------------
		} 
		// -------------------------------------------------------------------------
		// 11/04/2016 ECU return the lines that have been read
		// -------------------------------------------------------------------------
		return localLines;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static byte [] readLinesFromEndOfFile (String theFileName,int theNumberOfLines)
	{
		// -------------------------------------------------------------------------
		// 16/04/2016 ECU created to read the specified number of lines from the end
		//                of the file; or the whole file if it has less lines than that
		//                specified
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 16/04/2016 ECU set up initial parameters
			// ---------------------------------------------------------------------
			File 	localFile			= new File (theFileName);
			// ---------------------------------------------------------------------
			byte	localByte;
			long	localPosition 		= localFile.length() - 1;
			int		localLineCounter	= 0;
			// ---------------------------------------------------------------------
    		// 16/04/2016 ECU need a random access file so that positioning can be
    		//                used
    		// ---------------------------------------------------------------------
    		RandomAccessFile inputFile 	= new RandomAccessFile (localFile, "r");
    		// ---------------------------------------------------------------------
    		// 16/04/2016 ECU set up pointers from the file information
    		// ---------------------------------------------------------------------
    		while (localPosition > 0)
    		{
    			inputFile.seek (localPosition);
    			localByte = inputFile.readByte ();
    			// -----------------------------------------------------------------
    			// 16/04/2015 ECU check if a line terminator has been found
    			// -----------------------------------------------------------------
    			if (localByte == StaticData.LINE_TERMINATOR) 
    			{
    				// -------------------------------------------------------------
    				// 16/04/2016 ECU terminator found so increment counter
    				// -------------------------------------------------------------
    				localLineCounter++;
    				// -------------------------------------------------------------
    				// 16/04/2016 ECU check if the required number of lines have been
    				//                found
    				// -------------------------------------------------------------
    				if (localLineCounter == theNumberOfLines)
    				{
    					// ---------------------------------------------------------
    					// 16/04/2016 ECU the required number of lines found so
    					//                break out of the 'while' loop
    					// ---------------------------------------------------------
    					// 16/04/2016 ECU at this point the file will be positioned
    					//                to the terminator so add 1 to jump over it
    					// ---------------------------------------------------------
    					localPosition++;
    					// ---------------------------------------------------------
    					break;
    					// ---------------------------------------------------------
    				}
    				// -------------------------------------------------------------
    			}
    			// -----------------------------------------------------------------
    			// 16/04/2016 ECU move back through the file
    			// -----------------------------------------------------------------
    			localPosition--;
    			// -----------------------------------------------------------------
    		}
    		// ---------------------------------------------------------------------
    		// 16/04/2016 ECU break out of the loop if the number of lines have been
    		//                found or the start of the file has been reached. Want
    		//                to re-position and then read the rest of the file into
    		//                the buffer
    		// ---------------------------------------------------------------------
    		inputFile.seek (localPosition);
    		// ---------------------------------------------------------------------
    		// 16/04/2016 ECU allocate a buffer and then read into it
    		// ---------------------------------------------------------------------
    		byte [] localBuffer = new byte [(int)(inputFile.length() - localPosition)];
    		inputFile.read(localBuffer);
    		// ----------------------------------------------------------------------
    		// 16/04/2016 ECU close the input file before returning the data
    		// ----------------------------------------------------------------------
    		inputFile.close ();
    		return localBuffer;
    		// ----------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 16/04/2016 ECU an exception occurred
			// ---------------------------------------------------------------------
			return null;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static Object readObjectFromDisk (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 15/11/2014 ECU created to read an object from the specified file.
		//                Return the object or 'null' if any errors occur
		// -------------------------------------------------------------------------
		Object theObject = null;
		try
		{
			// ---------------------------------------------------------------------
			// 15/11/2014 ECU read in the object - any errors will throw an exception
			//                which do not need to be processed - just need to return
			//                a 'null' object
			// 17/10/2015 ECU changed to use buffering
			// ---------------------------------------------------------------------
			ObjectInputStream inputStream = new ObjectInputStream (new BufferedInputStream(new FileInputStream (theFileName)));
			theObject = inputStream.readObject ();
			inputStream.close ();
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{	
			// ---------------------------------------------------------------------
			// 17/10/2015 ECU add the logging of the exception to try and solve an issue
			//                where am getting EOF errors when reading the file
			//                later
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile(TAG, "Exception on reading file " + theFileName + " " + theException);
			// ---------------------------------------------------------------------
		} 
		// ------------------------------------------------------------------------
		return theObject;
	}
	// =============================================================================
	public static Object readObjectFromDisk (Context theContext,String theFileName)
	{
		// -------------------------------------------------------------------------
		// 15/11/2014 ECU change to use previous method rather than include the code
		// -------------------------------------------------------------------------
		return readObjectFromDisk (theFileName);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@SuppressWarnings("unchecked")
	public static void ReadObjectFromDisk (Context theContext,
										   int theResourceID,
										   boolean theSyncFlag,
										   boolean theProcessFlag)
	{
		// -------------------------------------------------------------------------
		// 03/02/2014 ECU created - to read the specified file from disk
		//                and stored in the associated object. The file is
		//                assumed to be in the project folder.
		// 29/03/2014 ECU added ShoppingData entry
		// 01/05/2015 ECU added MusicPlayerData
		// 18/12/2015 ECU added the method on synchronise
		// 04/11/2016 ECU add the process flag so that any associated processing of
		//                the data read will take place, i.e. generating alarms from
		//                the appointments data
		// -------------------------------------------------------------------------
		Method	localMethodOnSynchronise = null;
		// -------------------------------------------------------------------------
		String localFileName = PublicData.projectFolder + theContext.getString (theResourceID);
		// -------------------------------------------------------------------------
		// 03/02/2014 ECU read a 'generic' object from the specified file then decide how to
		//				  use it
		// 03/04/2014 ECU change to use AsyncUtilities rather than Utilities
		// -------------------------------------------------------------------------
		Object localObject 
			= (Object)  AsyncUtilities.readObjectFromDisk (theContext,localFileName);
		// -------------------------------------------------------------------------
		// 03/02/2014 ECU if the object is valid then decide how to use it
		// -------------------------------------------------------------------------
		if (localObject != null)
		{
			// ---------------------------------------------------------------------
			// 03/02/2014 ECU now switch depending on which resource object is to be
			//                set
			// ---------------------------------------------------------------------
			switch (theResourceID)
			{
				// =================================================================
				case R.string.agencies_file:
					PublicData.agencies = (List<Agency>) localObject; 
					break;
				// =================================================================
				case R.string.appointments_file:
					PublicData.appointments = (List<AppointmentDetails>) localObject;
					// -------------------------------------------------------------
					// 04/11/2016 ECU the incoming file contains data that is used
					//                to generate appointment reminders and timers.
					//                If the process flag is true then this means that
					//                this method is being called by a file received
					//                on the network so call the method to generate
					//                the associated alarms
					// -------------------------------------------------------------
					if (theProcessFlag)
					{
						// ---------------------------------------------------------
						// 04/11/2016 ECU call the required method to process the
						//                file
						// ---------------------------------------------------------
						AppointmentsActivity.GenerateAlarms (theContext);	
						// ---------------------------------------------------------
					}
					//--------------------------------------------------------------
					break;
				// =================================================================
				case R.string.appointment_types_file:
					PublicData.appointmentTypes = (List<AppointmentTypeDetails>) localObject; 
					break;
				// =================================================================
				case R.string.bar_code_data:
					PublicData.barCodes = (List<BarCode>) localObject;
															// 07/02/2014 ECU changed to List
					break;
				// =================================================================
				case R.string.care_plan_file:
					PublicData.carePlan = (CarePlan) localObject; 
					break;
				// =================================================================
				case R.string.care_visit_tasks_file:
					// -------------------------------------------------------------
					// 06/12/2016 ECU get the raw data from the object read
					// -------------------------------------------------------------
					PublicData.tasksToDoRaw = (String []) localObject;
					// -------------------------------------------------------------
					// 06/12/2016 ECU process the raw data to replace PATIENT with
					//                the patient's preferred name
					// -------------------------------------------------------------
					PublicData.tasksToDo = CarePlanVisitActivity.TasksToDo (PublicData.patientDetails.preferredName,PublicData.tasksToDoRaw);
					// -------------------------------------------------------------
					break;
				// =================================================================
				case R.string.carers_file :
					PublicData.carers = (List<Carer>) localObject;
					break;
				// =================================================================
				case R.string.email_details_file:
					PublicData.emailDetails = (EmailDetails) localObject; 
					break;
				// =================================================================
				case R.string.medication_details_file :
					PublicData.medicationDetails = (List<MedicationDetails>) localObject;
					// -------------------------------------------------------------
					// 18/12/2015 ECU add in the method to call
					// -------------------------------------------------------------
					localMethodOnSynchronise = createAMethod (MedicationInput.class,"SynchronisedFile",theContext);
					// -------------------------------------------------------------
					break;
				// =================================================================
				case R.string.music_player_data:
					// -------------------------------------------------------------
					// 01/05/2015 ECU added so that any presets are available
					//                to all dependent activities
					// ------------------------------------------------------------
					PublicData.musicPlayerData = (MusicPlayerData) localObject;
					// ------------------------------------------------------------
					// 14/11/2016 ECU reset some flags that should not survive a
					//                restart
					// -------------------------------------------------------------
					PublicData.musicPlayerData.tracksPlaying = false;
					// -------------------------------------------------------------
					break;
				// =================================================================
				case R.string.named_actions_file:
					// -------------------------------------------------------------
					// 03/08/2016 ECU added to handle any stored named actions
					// -------------------------------------------------------------
					PublicData.namedActions = (List<NamedAction>) localObject;
					break;
				// =================================================================
				case R.string.patient_details_file:
					PublicData.patientDetails = (PatientDetails) localObject; 
					break;
				// =================================================================
				case R.string.shopping_file:
					PublicData.shoppingData = (ShoppingData) localObject;
					break;
				// =================================================================
				case R.string.stored_data_file:
					PublicData.storedData = (StoredData) localObject;
					break;
				// =================================================================
				case R.string.visits_file:
					PublicData.visits = (List<Visit>) localObject;
					break;
				// =================================================================
				case R.string.voice_command_phrases_file:
					// -------------------------------------------------------------
					// 21/05/2016 ECU added for reading the voice commands
					// -------------------------------------------------------------
					PublicData.voiceCommandPhrases = (List<VoiceCommandPhrases>) localObject;
					break;
				// =================================================================
			}
			// ---------------------------------------------------------------------
			// 03/02/2014 ECU now decide whether to include in the synchronisation list
			// 04/02/2014 ECU add the resource ID to the calling parameter
			// ---------------------------------------------------------------------
			if (theSyncFlag)
		    	PublicData.filesToSynchronise.add (new FileToSynchronise (localFileName,theResourceID,localMethodOnSynchronise));
		}
		else
		{
			// ---------------------------------------------------------------------
			// 06/12/2016 ECU created to handle issues when data cannot be read from
			//                the specified file
			// ---------------------------------------------------------------------
			switch (theResourceID)
			{
				// =================================================================
				case R.string.care_visit_tasks_file:
					// -------------------------------------------------------------
					// 06/12/2016 ECU no file so set the 'tasks to do' from the array
					//                held in resources
					// 13/12/2016 ECU put in the null check
					// -------------------------------------------------------------
					if (PublicData.patientDetails != null)
						PublicData.tasksToDo = CarePlanVisitActivity.TasksToDo (PublicData.patientDetails.preferredName,null);
					// -------------------------------------------------------------
					break;
				// =================================================================
			}
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String readRawResource (Context theContext,int theResourceID)
	{
		// -------------------------------------------------------------------------
		// 10/03/2017 ECU created to read the contents of a 'raw' resource and
		//                return as a string
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 10/03/2017 ECU open up an buffered reader stream to the required resource
			// ---------------------------------------------------------------------
			BufferedReader bufferedReader = new BufferedReader (new InputStreamReader (theContext.getResources().openRawResource (theResourceID)));
			// ---------------------------------------------------------------------
			// 10/03/2017 ECU set up a string for input lines
			// ---------------------------------------------------------------------
			String 		  inputLine = "";
			StringBuilder rawData = new StringBuilder ();
			// ---------------------------------------------------------------------
			// 10/03/2017 ECU now loop reading in each line from the file
			// ---------------------------------------------------------------------
			while ((inputLine = bufferedReader.readLine()) != null)
			{
				// -----------------------------------------------------------------
				// 10/03/2017 ECU add the input into the total reply
				// -----------------------------------------------------------------
				rawData.append (inputLine + StaticData.NEWLINE);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 10/03/2017 ECU close down the input stream before returning
			// ---------------------------------------------------------------------
			bufferedReader.close ();
			// ---------------------------------------------------------------------
			// 10/03/2017 ECU now return the generated string
			// ---------------------------------------------------------------------
			return rawData.toString();
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 10/03/2017 ECU something happened so indicate the error
			// ---------------------------------------------------------------------
			return null;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static boolean refreshCurrentTime (Context theContext)
	{
		// -------------------------------------------------------------------------
		long 	currentTime = 0;
		boolean ntpTimeUsed = false;
		// -------------------------------------------------------------------------		
		// 13/08/2013 ECU created
		//            ECU will try and synchronise the system clock with an NTP server
		//			      if it is time and if there is access to the internet
		// -------------------------------------------------------------------------
		// 13/08/2013 ECU check if have access to the internet - if not then just exit
		// -------------------------------------------------------------------------
		if (checkForNetwork (theContext))
		{
			// ---------------------------------------------------------------------
			// 13/08/2013 ECU decrement the counter to see if it is time to do the
			//				  resynchronise
			// ---------------------------------------------------------------------
			if (--PublicData.ntp_counter <= 0)
			{	
				// -----------------------------------------------------------------
				// 13/08/2013 ECU try and get the current time from NTP server
				// 01/09/2015 ECU changed to use StaticData
				// -----------------------------------------------------------------
				SntpClient client = new SntpClient ();
	    
				if (client.requestTime (StaticData.NTP_SERVER,StaticData.NTP_TIMEOUT)) 
				{
					Calendar calendar = Calendar.getInstance();
					long nowNTP = client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference();
					// -------------------------------------------------------------
					// 13/08/2013 ECU get the difference between current and NTP time
					// -------------------------------------------------------------
					currentTime = calendar.getTimeInMillis();
					PublicData.currentTimeAdjustment = currentTime - nowNTP;
					// -------------------------------------------------------------
					// 13/08/2013 ECU display information in local datagram
					//			  ECU changed to use the dateFormatter in MainActivity
					// 21/09/2013 ECU check if MainAcivity.datagram is 'null'
					// -------------------------------------------------------------
					if (PublicData.datagram != null)
					{
						PublicData.datagram.Message ("ntp time " + PublicData.dateFormatter.format(nowNTP) + "\n" +
												     "current time " + PublicData.dateFormatter.format(currentTime) + "\n" +
												     "adjusted time " + PublicData.dateFormatter.format (currentTime-PublicData.currentTimeAdjustment));
					}
					// -------------------------------------------------------------					
					// 13/08/2013 ECU indicate that time has been synchronised
					// -------------------------------------------------------------
					ntpTimeUsed = true;
				}
				// -----------------------------------------------------------------
				// 13/08/2013 ECU reset the counter - ready for next update
				// 01/09/2015 ECU changed to use StaticData
				// -----------------------------------------------------------------
				PublicData.ntp_counter = StaticData.NTP_REFRESH_TIME;
			}
		}
		// -------------------------------------------------------------------------
		// 13/08/2013 ECU return indicating whether time synchronised
		// -------------------------------------------------------------------------
		return ntpTimeUsed;
	}
	// =============================================================================
	public static void saveLogCatEntries (String theFileName,String theFilter)
	{
		// -------------------------------------------------------------------------
		// 07/10/2015 ECU created to write the current LogCat entries to the specified
		//                file
		// -------------------------------------------------------------------------
		try 
		{
			FileOutputStream outputStream = new FileOutputStream (theFileName);
			// ---------------------------------------------------------------------
			// 04/02/2014 ECU launch the process to get the entries
			//            ECU add '-v time' to get a timestamp on each entry
			//            ECU to show warnings add *:W, for errors *:E
			// 22/03/2014 ECU added the *:I option to try and remove debug
			//                messages - use LOGCAT_COMMAND
			// 01/09/2015 ECU changed to use StaticData
			// ---------------------------------------------------------------------
			Process process = Runtime.getRuntime().exec(StaticData.LOGCAT_COMMAND);
			// --------------------------------------------------------------------- 
			// 04/02/2014 ECU declare the reader for the process output
			// ---------------------------------------------------------------------
			BufferedReader bufferedReader 
				= new BufferedReader(new InputStreamReader (process.getInputStream()));

			String currentLine = "";
			// ---------------------------------------------------------------------
			// 18/03/2014 ECU loop for all entries in the log
			// ---------------------------------------------------------------------  
			while ((currentLine = bufferedReader.readLine()) != null) 
			{
				// -----------------------------------------------------------------
				// 04/02/2014 ECU add the current line into the current entries
				// -----------------------------------------------------------------
				if (theFilter == null || (currentLine.contains(theFilter)))
				{
					outputStream.write ((currentLine + "\n").getBytes());
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 07/10/2015 ECU now close the stream
			// ---------------------------------------------------------------------
			outputStream.close();
			// ---------------------------------------------------------------------
		} 
		catch (IOException theException) 
		{
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static Bitmap scaleBitMap (String theFilePath,int theWidth,int theHeight)
	{
		// -------------------------------------------------------------------------
		// 18/12/2016 ECU created to generated a bitmap from the specified file and
		//                then scale it to the required height and width whilst
		//                retaining the aspect ration
		// -------------------------------------------------------------------------
		Bitmap localBitmap = BitmapFactory.decodeFile (theFilePath); 
		// -------------------------------------------------------------------------
		// 18/12/2016 ECU if an error occurs thene a null is returned
		// -------------------------------------------------------------------------
		if (localBitmap != null)
		{
			// ---------------------------------------------------------------------
			// 18/12/2016 ECU set the origin for the final bitmap
			// ---------------------------------------------------------------------
			int localXOrigin = 0;
			int localYOrigin = 0;
			// ---------------------------------------------------------------------
			// 18/12/2016 ECU at this point have the required bitmap so work with its
			//                width and height
			// ---------------------------------------------------------------------
			int localSourceHeight   = localBitmap.getHeight();
			int localSourceWidth 	= localBitmap.getWidth();
			// ---------------------------------------------------------------------
			// 18/12/2016 ECU now copy these into final image size
			// ---------------------------------------------------------------------
			int localFinalHeight 	= localSourceHeight;
			int localFinalWidth		= localSourceWidth;
			// ---------------------------------------------------------------------
			// 18/12/2016 ECU now work out the scaling factors
			// ---------------------------------------------------------------------
			float localHeightScale 	= (float) theHeight / (float) localSourceHeight;
			float localWidthScale 	= (float) theWidth  / (float) localSourceWidth;
			// ---------------------------------------------------------------------
			// 18/12/2016 ECU if both scales are below 1 then no scaling is required else
			//                scale by the larger of the two
			// ---------------------------------------------------------------------
			if ((localHeightScale > 1.0f) || (localWidthScale > 1.0f))
			{
				// -----------------------------------------------------------------
				// 18/12/2016 ECU scaling is required so use the larger of the two -
				//                localHeightScale will hold this
				// -----------------------------------------------------------------
				if (localWidthScale > localHeightScale)
					localHeightScale = localWidthScale;
				// -----------------------------------------------------------------
				// 18/12/2016 ECU scale the bit map size
				//            ECU add the 0.5f so as to round up to the nearest pixel
				// -----------------------------------------------------------------
				localFinalHeight = (int) ((localHeightScale * (float) localSourceHeight) + 0.5f);
				localFinalWidth  = (int) ((localHeightScale * (float) localSourceWidth)  + 0.5f);
				// -----------------------------------------------------------------
				// 18/12/2016 ECU now scale the image
				// -----------------------------------------------------------------
				localBitmap = Bitmap.createScaledBitmap (localBitmap,localFinalWidth,localFinalHeight,false);
				// -----------------------------------------------------------------
				// 18/12/2016 ECU take the centre of the image
				// -----------------------------------------------------------------
				localXOrigin = (localFinalWidth  - theWidth)  / 2;
				localYOrigin = (localFinalHeight - theHeight) / 2;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 18/12/2016 ECU now return the bitmap of the required size
			//            ECU it is possible that an exception could occur if
			//                the origin + width or origin + height is greater than
			//                the size of the bitmap so try/catch on this
			// ---------------------------------------------------------------------
			try
			{
				return Bitmap.createBitmap (localBitmap,localXOrigin,localYOrigin,theWidth,theHeight);
			}
			catch (Exception theException)
			{
				// -----------------------------------------------------------------
				// 18/12/2016 ECU an error occurred so just tell the caller of it
				// -----------------------------------------------------------------
				return null;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 18/12/2016 ECU indicate that the chosen picture is not in the project
			//                folder
			// ---------------------------------------------------------------------
			return null;
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	public static void scanMediaFilesInFolder (Context theContext,String theFolderName,ImageView theImageView)
	{	
		File directory = new File(theFolderName);
		File[] files = directory.listFiles();
		
		/* loop for all files in the folder */
		for (int i = 0; i < files.length; ++i) 
		{
		    if (files[i].getName().endsWith (".jpg"))
		    {
		    	displayAnImage (theImageView,files[i].getAbsolutePath());	
		    }
		}
	}
	// =============================================================================
	public static Bitmap screenCapture (View theView,String theFileName)
	{
		// -------------------------------------------------------------------------
		// 14/12/2015 ECU created to get a 'screen shot' and then to write to the
		//                specified file
		// -------------------------------------------------------------------------
		Bitmap bitmapOfScreen = Utilities.getViewAsBitmap (theView);
		// -------------------------------------------------------------------------
		// 24/04/2015 ECU write the bitmap to file and then email it
		// -------------------------------------------------------------------------
		try
		{
			FileOutputStream fileOutputStream = new FileOutputStream(new File (theFileName));
			// ---------------------------------------------------------------------
			// 24/04/2015 ECU want to compress the bitmap into 'png' which is
			//                lossless so the quality of '85' will be ignored
			// ---------------------------------------------------------------------
			bitmapOfScreen.compress (Bitmap.CompressFormat.PNG,85,fileOutputStream);
			fileOutputStream.flush();
			fileOutputStream.close();
			// ---------------------------------------------------------------------
		}
		catch (Exception theException) 
		{
		}
		// -------------------------------------------------------------------------
		// 14/12/2015 ECU return the bitmap
		// -------------------------------------------------------------------------
		return bitmapOfScreen;
	}
	// =============================================================================
	public static void selectAFile (Context theContext,String theExtensionWanted,MethodDefinition <?> theSelectMethodDefinition)
	{
		// -------------------------------------------------------------------------
		// 17/12/2015 ECU created to use the FileChooser activity to select a
		//                displayed file
		//            ECU wanted to pass the MethodDefinition but although this
		//                was being passed OK was getting a 'null' when trying
		//                to return a method from the code
		// -------------------------------------------------------------------------
		Intent localIntent 			= new Intent (theContext,FileChooser.class);
		localIntent.putExtra (StaticData.PARAMETER_FILTER,theExtensionWanted);
		localIntent.putExtra (StaticData.PARAMETER_SELECT,theSelectMethodDefinition);
		localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		theContext.startActivity (localIntent);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void sendDatagram (Context theContext,String theIPAddress,int theMessageType,Datagram theDatagram)
	{
		// -------------------------------------------------------------------------
		// 03/08/2013 ECU added
		//                if theIPAddress = "all" then send the datagram to all registered devices
		//                else just send to the specified address
		// 31/01/2015 ECU changed the name of the method called
		// 21/03/2015 ECU added theMessageType as an argument
		//            ECU pass through the message type as an argument
		// 22/03/2015 ECU changed the logic to accommodate the redefinition of
		//                deviceDetails to List<Devices>
		// -------------------------------------------------------------------------
		if (theIPAddress.startsWith("all"))
		{	
			if (PublicData.deviceDetails != null)
			{
				for (int theDevice = 0; theDevice < PublicData.deviceDetails.size(); theDevice++)
				{
					sendSocketMessageSendTheObject(theContext,
												   PublicData.deviceDetails.get(theDevice).IPAddress,
							                       PublicData.socketNumberForData,
							                       StaticData.SOCKET_MESSAGE_DATAGRAM_ACTION,
							                       theDatagram);
				}
			}
		}
		else
		{
			sendSocketMessageSendTheObject (theContext,
											theIPAddress,
											PublicData.socketNumberForData,
											StaticData.SOCKET_MESSAGE_DATAGRAM_ACTION,
											theDatagram);
		}
	}
	/* ============================================================================= */
	public static void sendDatagramType (Context theContext,String theIPAddress,int theType)
	{
		// -------------------------------------------------------------------------
		// 03/08/2013 ECU added - just send a message with only the socket message type
		// 31/01/2015 ECU changed the name of the method called
		// 21/03/2015 ECU pass through the message type as an argument
		// -------------------------------------------------------------------------
		sendSocketMessageSendTheObject (theContext,
										theIPAddress,
										PublicData.socketNumberForData,
										theType);
		
	}
	/* ============================================================================= */
	public static void SendEmailMessage (final Context theContext,
										 final String theSubject,
										 final String theMessage,
										 final String theExtras,
										 final String [] theAttachments)
	{
		// -------------------------------------------------------------------------
		// 04/01/2014 ECU check if there is access to a network
		//            ECU added the enablement check
		//            ECU include patient name with subject and start message with the subject
		// 05/01/2014 ECU added the signature
		// 24/04/2015 ECU added 'theAttachment' as an argument
		// 25/04/2015 ECU changed to enable a number of attachments to be passed
		//            ECU changed to send the email as a thread
		// -------------------------------------------------------------------------
		if (PublicData.emailDetails.enabled && checkForNetwork (theContext))
		{
			Thread emailThread = new Thread()
			{
				@Override
				public void run()
				{
					// -------------------------------------------------------------
					// 04/01/2014 ECU initialise the SMTP connection before sending 
					//                the data
					//			  ECU changed to use the EmailDetails class
					// -------------------------------------------------------------	
					SMTPFunctions smtpFunctions 
						= new SMTPFunctions (PublicData.emailDetails.SMTPServer,
											 PublicData.emailDetails.SMTPPort,
											 PublicData.emailDetails.SMTPUserName,
											 PublicData.emailDetails.SMTPPassword);	
					// -------------------------------------------------------------
					try 
					{
						// ---------------------------------------------------------
						// 04/01/2014 ECU modify the subject to include the patient's
						//                name
						// 05/01/2014 ECU use the name from the patient's record
						// ---------------------------------------------------------
						String localSubject = PublicData.patientDetails.Name() + " - " + theSubject;
						// ---------------------------------------------------------
						// 04/01/2014 ECU now try and send the email
						//            ECU repeat the subject at the start of the message
						// 05/01/2014 ECU added the signature to the end of the message
						//			  ECU build up message body separately
						// 27/01/2015 ECU some mail clients, like GMail on Android, do not
						//                handle embedded "\n" in messages in <pre> .. </pre>
						//                blocks so replace that with "<br>"
						// 23/12/2015 ECU changed the order of <hr> and <pre> in the
						//                initial body because was getting font issues
						//                on Android email client - Windows client fine
						// ---------------------------------------------------------
						String messageBody = "<h3><b>" + 
									  		"<p align=\"center\">" +
									  			localSubject + 
									  		"</p></b></h3>" + 
									  		"\n\n" + 
									  		"<hr>" +
									  		"<pre>" + 
									  			theMessage.replaceAll ("\n","<br>") +
									  		"</pre>";
						// ---------------------------------------------------------
						// 05/01/2014 ECU check if any extras are to be added
						// 27/01/2015 ECU change "\n" to "<br>" - (see comment above)
						// ---------------------------------------------------------
						if (theExtras != null)
						{
							messageBody += "<hr>" +
										   "<pre>" +
												theExtras.replaceAll("\n","<br>") +
										   "</pre>";
						}
						// ---------------------------------------------------------
						// 05/01/2014 ECU add the signature
						// 27/03/2016 ECU changed to use signature method
						// ---------------------------------------------------------
						messageBody +=	"<hr>" +
										"<font face=\"Arial\">" +
										"<p align=\"center\">" +
											PublicData.emailDetails.Signature (theContext) +
										"</p>" + 
										"</font>" +
										"<hr>";
						// ---------------------------------------------------------
						// 08/01/2014 ECU try and display the name of the device 
						//                sending the email
						// ---------------------------------------------------------
						String theDeviceName = GetDeviceName (PublicData.ipAddress);
	
						if (theDeviceName != null)
						{
							messageBody += "<p align=\"center\"><i><b>Email sent from '" + 
											theDeviceName + "'</b></i></p>";
						}
						// ---------------------------------------------------------
						// 28/10/2014 ECU now actually send the email message
						// 24/04/2015 ECU added the attachment
						// 25/04/2015 ECU changed to send attachments
						// ---------------------------------------------------------
						smtpFunctions.sendMail(localSubject,
											   messageBody,
											   PublicData.emailDetails.SMTPUserName,
											   PublicData.emailDetails.recipients,
											   theAttachments);
						// ---------------------------------------------------------
					} 
					catch (Exception theException)
					{
						// ---------------------------------------------------------
						// 06/03/2014 ECU changed so that any error messages are put into
						//                project log - rather than just show a 'popToast'
						//            ECU add 'true' flag so that message is logged no matter
						//                whether 'project logging' is on or off
						// ---------------------------------------------------------				
						Utilities.LogToProjectFile (TAG, "SendMail " + theException,true);				
						// ---------------------------------------------------------
					}
				}
			};
			// ---------------------------------------------------------------------
			// 25/04/2015 ECU now run the thread
			// ---------------------------------------------------------------------
			emailThread.start();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 04/01/2014 ECU there is no access to a network to do anything
			// 28/10/2014 ECU tidy up to provide a more meaningful message
			// 24/11/2016 ECU because this method can be called by a non UI task
			//                then changed to use the MessageHandler
			//          ECU added the 'AndSpeak;
			// ---------------------------------------------------------------------
			MessageHandler.popToastAndSpeak (theContext.getString(R.string.email_cannot_send) +
						(PublicData.emailDetails.enabled ? theContext.getString (R.string.network_no_access) 
													     : theContext.getString (R.string.settings_not_enabled)));
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	public static void SendEmailMessage (Context theContext,
										 final String theRecipients,
										 final String theSubject,
										 final String theMessage,
										 final String theExtras,
										 final String [] theAttachments)
	{
		// -------------------------------------------------------------------------
		// 12/07/2015 ECU created to provide a generalised email sending facility
		// -------------------------------------------------------------------------
		if (PublicData.emailDetails.enabled && checkForNetwork (theContext))
		{
			Thread emailThread = new Thread()
			{
				@Override
				public void run()
				{
					// -------------------------------------------------------------
					// 12/07/2015 ECU initialise the SMTP connection before sending 
					//                the data
					//			  ECU changed to use the EmailDetails class
					// -------------------------------------------------------------	
					SMTPFunctions smtpFunctions 
						= new SMTPFunctions (PublicData.emailDetails.SMTPServer,
											 PublicData.emailDetails.SMTPPort,
											 PublicData.emailDetails.SMTPUserName,
											 PublicData.emailDetails.SMTPPassword);	
					// -------------------------------------------------------------
					try 
					{
						// ---------------------------------------------------------
						// 12/07/2015 ECU store the incoming message which could be
						//                modified
						// ---------------------------------------------------------
						String messageBody = theMessage.replaceAll ("\n","<br>");
						// ---------------------------------------------------------
						// 05/01/2014 ECU check if any extras are to be added
						// 27/01/2015 ECU change "\n" to "<br>" - (see comment above)
						// ---------------------------------------------------------
						if (theExtras != null)
						{
							messageBody += "<hr>" +
										   "<pre>" +
												theExtras.replaceAll("\n","<br>") +
										   "</pre>";
						}
						// ---------------------------------------------------------
						// 12/07/2015 ECU now use the SMTP functions to actually
						//                send the email
						// ---------------------------------------------------------
						smtpFunctions.sendMail(theSubject,
											   messageBody,
											   PublicData.emailDetails.SMTPUserName,
											   theRecipients,
											   theAttachments);
						// ---------------------------------------------------------
					} 
					catch (Exception theException)
					{
						// ---------------------------------------------------------
						// 06/03/2014 ECU changed so that any error messages are put into
						//                project log - rather than just show a 'popToast'
						//            ECU add 'true' flag so that message is logged no matter
						//                whether 'project logging' is on or off
						// ---------------------------------------------------------				
						Utilities.LogToProjectFile(TAG, "SendMail " + theException,true);				
						// ---------------------------------------------------------
					}
				}
			};
			// ---------------------------------------------------------------------
			// 25/04/2015 ECU now run the thread
			// ---------------------------------------------------------------------
			emailThread.start();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 04/01/2014 ECU there is no access to a network to do anything
			// 28/10/2014 ECU tidy up to provide a more meaningful message
			// 02/05/2016 ECU changed to use resources
			// 24/11/2016 ECU an email can be attempted by a task which is not on
			//                the user interface and this was generating an error
			//                so now use the MessageHandler to do the display of this
			//                error message
			//            ECU added the 'AndSpeak;
			// ---------------------------------------------------------------------
			MessageHandler.popToastAndSpeak (theContext.getString (R.string.email_cannot_send) +
						(PublicData.emailDetails.enabled ? theContext.getString(R.string.network_no_access) 
													     : theContext.getString(R.string.settings_not_enabled)));
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static void SendEmailMessage (Context theContext,
										 String theSubject,
										 String theMessage,
										 String theExtras)
	{
		// -------------------------------------------------------------------------
		// 24/04/2015 ECU created to call the master method now that an attachment
		//                argument has been added.
		// --------------------------------------------------------------------------
		SendEmailMessage (theContext,theSubject,theMessage,theExtras,(String []) null);
		// --------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void SendEmailMessage (Context theContext,String theSubject,String theMessage)
	{
		// -------------------------------------------------------------------------
		// 05/01/2014 ECU call the main method but indicate that there are no extras
		// 24/04/2015 ECU added the extra null to indicate 'no attachment'
		// -------------------------------------------------------------------------
		SendEmailMessage (theContext,theSubject,theMessage,null,(String []) null);
	}
	// =============================================================================
	public static void SendEmailMessage (Context theContext,
										 String  theSubject,
										 String  theMessage,
										 String  theExtras,
										 String  theAttachment)
	{
		// -------------------------------------------------------------------------
		// 25/04/2015 ECU created to send an email message with a single attachment
		// -------------------------------------------------------------------------
		SendEmailMessage (theContext,theSubject,theMessage,theExtras,new String [] {theAttachment});
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void SendEmailMessage (final Context theContext,
										 final String theSubject,
									     final String theMessage,
									     boolean theWirelessCheck)
	{
		// -------------------------------------------------------------------------
		// 07/03/2014 ECU created - handles the transmission of an email message
		//                having taken the necessary actions to ensure that the
		//                wireless network is up and running
		//
		//                As there are some delays required then the work is done via 
		//                a thread.
		// 28/10/2014 ECU make sure the email is sent irrespective of whether the
		//                email system is enabled or not - but do not alter the state
		// -------------------------------------------------------------------------
		final boolean localEmailEnabledFlag = PublicData.emailDetails.enabled;
		PublicData.emailDetails.enabled     = true;
		// -------------------------------------------------------------------------
		// 07/03/2014 ECU define the thread to be used
		// -------------------------------------------------------------------------
		Thread emailThread = new Thread()
		{
			// ---------------------------------------------------------------------
			// 07/03/2014 ECU declare the body of the thread
			// ---------------------------------------------------------------------
			@SuppressLint("Wakelock")
			@Override
			public void run()
			{
				// -----------------------------------------------------------------
				// 10/03/2014 ECU declare the 'wakeLock' that will allow 'sleep'
				//                to operate when the device is in 'standby' mode
				// -----------------------------------------------------------------
				PowerManager powerManager = (PowerManager) theContext.getSystemService(Context.POWER_SERVICE);
				PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,TAG);
				// -----------------------------------------------------------------
				try 
				{
					synchronized(this)
			        { 
						// ---------------------------------------------------------
						// 07/03/2014 ECU remember the current state of the wifi
						//                network so that it can be returned to this
						//                state on exit
						// ---------------------------------------------------------
						boolean currentWifiState = false;
						
						// ---------------------------------------------------------
						// 07/03/2014 ECU get the current state of the wifi network
						//                via the manager
						// ---------------------------------------------------------
						WifiManager wifiManager = (WifiManager)theContext.getSystemService(Context.WIFI_SERVICE);
						currentWifiState = wifiManager.isWifiEnabled();
						// ---------------------------------------------------------
						// 07/03/2014 ECU will want to wait until the connection
						//                to the access point is established.
						//                This information is provided by the
						//                'connection manager'.
						// ---------------------------------------------------------
						ConnectivityManager connectivityManager 
							= (ConnectivityManager) theContext.getSystemService(Context.CONNECTIVITY_SERVICE);
						
						// ---------------------------------------------------------
						// 07/03/2014 ECU do not like using wakelocks but if the 
						//				  device goes into 'sleep' mode then 
						//                without them any 'sleep' command does 
						//                not work.
						// ---------------------------------------------------------
						wakeLock.acquire();
						// ---------------------------------------------------------
						// 07/03/2014 ECU if the wifi network is currently disabled
						//                then enable it and wait for the connection
						//                to be established
						// ---------------------------------------------------------
						if (!currentWifiState)
						{
							// -----------------------------------------------------
							// 07/03/2014 ECU enable wifi
							// -----------------------------------------------------					
							wifiManager.setWifiEnabled (true);
							// -----------------------------------------------------
							// 07/03/2014 ECU loop until connection is established
							//                but have a counter to ensure that there
							//                is no 'infinite loop'
							// 24/11/2016 ECU used StaticData variables
							// -----------------------------------------------------
							int numberOfTries = StaticData.WIRELESS_RETRY_COUNTER;
							
							while (!connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected() && 
									(numberOfTries-- > 0))
							{
								sleep (StaticData.WIRELESS_RETRY_DELAY);
							}
							
							Utilities.LogToProjectFile(TAG, "number of Tries " + numberOfTries);
						}
						// ---------------------------------------------------------
						// 07/03/2014 ECU do a final check for a connection and
						//                then send the email message
						// ---------------------------------------------------------
						if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected())
						{
							// -----------------------------------------------------
							// 07/03/2014 ECU send the email message
							// -----------------------------------------------------
							Utilities.SendEmailMessage (theContext,theSubject,theMessage);	
							// -----------------------------------------------------
							// 07/03/2014 ECU wait some time for everything to be 
							//                transmitted
							// 28/10/2014 ECU changed to used the variable rather
							//                than the actual value
							// 01/09/2015 ECU changed to use StaticData
							// 24/11/2016 ECU changed to us WIRE... rather than literal
							// -----------------------------------------------------
							sleep (StaticData.WIRELESS_TRANSMISSION_DELAY);
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
						// 07/03/2014 ECU ensure that the wifi network is in the 
						//                state it was in when this method was
						//                entered
						// ---------------------------------------------------------
						if (!currentWifiState)
							wifiManager.setWifiEnabled (false);
						// ---------------------------------------------------------
			        }

			    }
			    catch (InterruptedException theException)
			    {                     
			    }
				// -----------------------------------------------------------------
				// 28/10/2014 ECU restore the state of the enablement flag
				// -----------------------------------------------------------------
				PublicData.emailDetails.enabled = localEmailEnabledFlag;
				//------------------------------------------------------------------
				// 07/03/2014 ECU release the wakelock
				// -----------------------------------------------------------------
				wakeLock.release();
				// -----------------------------------------------------------------               
			}
		};
		// -------------------------------------------------------------------------
		// 07/03/2014 ECU now do the actual work through the thread
		// -------------------------------------------------------------------------
		emailThread.start();  
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
	public static void SendFile (Context theContext,
								 String theIPAddress,
								 String theFileName)
	{
		// -------------------------------------------------------------------------
		// 03/08/2013 ECU added - transfer the whole of a file
		// -------------------------------------------------------------------------
		byte [] localBuffer = readAFile (theFileName);
		
		if (localBuffer != null)
		{
			// ---------------------------------------------------------------------
			// 31/01/2015 ECU changed the method name
			// 21/03/2015 ECU pass through the message type as an argument
			// ---------------------------------------------------------------------
			socketMessagesSendTheBuffer (theContext,
										 true,
										 theIPAddress,
										 PublicData.socketNumberForData,
										 StaticData.SOCKET_MESSAGE_FILE,
										 localBuffer,
										 localBuffer.length);
		}	
	}
	/* --------------------------------------------------------------------- */
	public static boolean SendFile (Context theContext,
								    String theIPAddress,
								    int thePort,
								    String theFileName,
								    int theChunkSize,
								    boolean theCheck)
	{
		// -------------------------------------------------------------------------
		// 15/08/2013 ECU created this method to try and retry the send should an exception occur
		//				  Note - theCheck is not used just defines particular method
		// 16/04/2015 ECU changed so that the method returns a boolean to indicate
		//                whether the operation was successful (true) or
		//                unsuccessful (false)
		// -------------------------------------------------------------------------
		// 15/08/2013 ECU default to a failed transmission to get the test loop working
		// -------------------------------------------------------------------------
		PublicData.sendingFile = true;
		// -------------------------------------------------------------------------
		// 15/08/2013 ECU keep looping until a successful transmission
		//            ECU have a retry counter
		// 01/09/2015 ECU changed to use StaticData
		// -------------------------------------------------------------------------
		int retryCounter = StaticData.MAXIMUM_RETRIES;
		
		while (PublicData.sendingFile && --retryCounter > 0)
		{
			debugMessage (TAG,"Sendfile with retry counter " + retryCounter); // temporary
			// ---------------------------------------------------------------------		
			// 15/08/2013 ECU try and send the file
			// ---------------------------------------------------------------------
			SendFile (theContext,theIPAddress,thePort,theFileName,theChunkSize);
			// ---------------------------------------------------------------------
			// 15/08/2013 ECU after the call to the method
			//					sendingFile = false ...... transmission succeeded
			//                              = true  ...... exception occurred so failure
			// ---------------------------------------------------------------------
		} 
		// -------------------------------------------------------------------------
		// 16/04/2015 ECU return with the status of whether the operation was or
		//                was not successful - this can be based on 'sendingFile'
		//                (see above note)
		// -------------------------------------------------------------------------
		return !PublicData.sendingFile;
		// -------------------------------------------------------------------------
	}
	/* ----------------------------------------------------------------------------- */
	public static boolean SendFile (Context theContext,
									String theIPAddress,
									int thePort,
									String theFileName,
									int theChunkSize)
	{
		// -------------------------------------------------------------------------
		// 19/08/2013 ECU changed the logic to use buffered output
		// 13/08/2013 ECU indicate that sending a file
		// 16/04/2015 ECU changed so that the method returns a boolean to indicate
		//                whether the operation was successful (true) or
		//                unsuccessful (false)
		// -------------------------------------------------------------------------
		PublicData.sendingFile = true;
		
		Utilities.debugMessage (TAG,"Starting to send " + theFileName);
		// -------------------------------------------------------------------------
		// 14/08/2013 ECU indicate that the state of the send is undefined
		// -------------------------------------------------------------------------
		PublicData.fileTransferStatus = StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------	
			// 02/07/2013 ECU open to the specified file in 'read' mode
			// ---------------------------------------------------------------------	
			RandomAccessFile theFile = new RandomAccessFile (theFileName, "r");
			// ---------------------------------------------------------------------
            // 02/07/2013 ECU read all the data in
			// ---------------------------------------------------------------------
			int numberOfBytesRead = 0;
            byte[] inputBuffer = new byte [theChunkSize];
            // ---------------------------------------------------------------------
            // 04/08/2013 ECU open up a socket to the specified destination
            // ---------------------------------------------------------------------
            InetAddress serverAddress = InetAddress.getByName(theIPAddress);    
			
			Socket theSocket = new Socket (serverAddress,thePort);
			// ---------------------------------------------------------------------
			// 04/08/2013 ECU now get an output stream for the data to be transmitted
			// 19/08/2013 ECU changed to use buffering so use BufferedOutputStream rather
			//				  than just OutputStream
			// ---------------------------------------------------------------------
			BufferedOutputStream outputStream = new BufferedOutputStream (theSocket.getOutputStream());
			// ---------------------------------------------------------------------
			// 04/08/2013 ECU write out the type of socket message being sent
			// ---------------------------------------------------------------------
			// 11/08/2013 ECU change so that transmission of message type is via
			//                the method rather than output.write and output.flush
			// 31/01/2015 ECU changed the method name
			// ---------------------------------------------------------------------
			Utilities.socketSendMessageType (StaticData.SOCKET_MESSAGE_FILE_CHUNKS, outputStream);
			// ---------------------------------------------------------------------
			// 10/08/2013 ECU want to send the size of the file
			// 01/09/2015 ECU changed to use StaticData
			// ---------------------------------------------------------------------
			outputStream.write (integerToByteArray ((int)theFile.length()),0,StaticData.INTEGER_SIZE);
			outputStream.flush ();
			// ---------------------------------------------------------------------	
			// 04/08/2013 ECU loop for all data in the file
			// ---------------------------------------------------------------------
            while ((numberOfBytesRead = theFile.read (inputBuffer,0,theChunkSize)) > 0)
            {
            	outputStream.write(inputBuffer,0,numberOfBytesRead);
            	outputStream.flush();
            }
            // ---------------------------------------------------------------------
            // 04/08/2013 ECU close the file being read
            // ---------------------------------------------------------------------
            theFile.close ();
            // ---------------------------------------------------------------------
            // 10/08/2013 ECU close the output stream
            // ---------------------------------------------------------------------
            outputStream.close ();
            // ---------------------------------------------------------------------
            // 04/08/2013 ECU close the socket that was used
            // ---------------------------------------------------------------------
            theSocket.close();
            // ---------------------------------------------------------------------
            // 13/08/2013 ECU indicate that the file has been sent
            // 14/08/2013 ECU moved here from end of method because if there
            //                is an exception then I want to take some remedial action
    		// ---------------------------------------------------------------------
    		PublicData.sendingFile = false;
    		
    		Utilities.debugMessage (TAG,"Finished sending " + theFileName);
	        // ---------------------------------------------------------------------
    		// 16/04/2015 ECU indicate that the file was sent successfully
    		// ---------------------------------------------------------------------
    		return true;
    		// ---------------------------------------------------------------------
        }
		catch (IOException theException)
		{
			// ---------------------------------------------------------------------
			// 14/08/2013 ECU changed to debugMessage from Log.e
			// ---------------------------------------------------------------------
			debugMessage (TAG,"SendFile:" + theException);
			// ---------------------------------------------------------------------
			// 16/04/2015 ECU indicate that a failure occurred
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}	
	}
	/* ============================================================================= */
	public static void SendFileDetails (Context theContext,String theFileName)
	{
		// -------------------------------------------------------------------------
		// 01/02/2014 ECU transmits the 'File' object for the specified file
		// 02/02/2014 ECU use the FileDetails class for transferring information
		// -------------------------------------------------------------------------
		File localFile = new File (theFileName);
		// -------------------------------------------------------------------------
		// 01/02/2014 ECU check if the specified file exists
		// -------------------------------------------------------------------------
		if (localFile.exists())
		{
			// ---------------------------------------------------------------------
			// 01/02/2014 ECU the file exists so set transmit the details 
			// 02/02/2014 ECU send the message to all discovered devices which are compatible
			// ---------------------------------------------------------------------
			if (PublicData.deviceDetails != null)
			{
				// -----------------------------------------------------------------
				// 02/02/2014 ECU create the class for transferring information
				// 22/03/2015 ECU changed the logic to accommodate the redefinition of
				//                deviceDetails to List<Devices>
				// -----------------------------------------------------------------
				FileDetails localFileDetails = new FileDetails (PublicData.projectFolder,localFile);
				
				for (int theDevice = 0; theDevice < PublicData.deviceDetails.size(); theDevice++)
				{
					// -------------------------------------------------------------
					// 02/02/2014 ECU check if compatible and not this device
					// 31/01/2015 ECU change the name of the method called
					// 21/03/2015 ECU pass through the message type as an argument
					// -------------------------------------------------------------
					if (!PublicData.deviceDetails.get(theDevice).IPAddress.equalsIgnoreCase(PublicData.ipAddress)
								&& PublicData.deviceDetails.get(theDevice).compatible)
					{
						sendSocketMessageSendTheObject (theContext,
														PublicData.deviceDetails.get(theDevice).IPAddress,
														PublicData.socketNumberForData, 
														StaticData.SOCKET_MESSAGE_FILE_DETAILS,
														(Object) localFileDetails);
					}
				}
			}
		}
	}
	/* ============================================================================= */
	public static void sendSMSMessage (Context theContext,String thePhoneNumber,String theMessage)
	{
		// -------------------------------------------------------------------------
		// called to send an SMS message using the telephony services
		// -------------------------------------------------------------------------
		if (getPhoneNumber(theContext) != null)
		{	
			// ---------------------------------------------------------------------
			sendSMSMessage (thePhoneNumber,theMessage);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 13/09/2013 ECU this device is not capable of sending an SMS message so check
			//                whether another device has that capability - if not then
			//                inform the user
			// 06/02/2014 ECU when sending to phone server then make sure there are no
			//                spaces in the phone number
			// ---------------------------------------------------------------------
			if (PublicData.phoneServer != null)
			{	
				// -----------------------------------------------------------------
				// 24/07/2013 ECU this device is not capable of making a phone call
				// 31/01/2015 ECU change the name of the method
				// 06/01/2016 ECU append the actual message and because of issues
				//                with embedded spaces then temporarily replace them
				// 29/01/2016 ECU changed to check if the SMS message was actually
				//                sent
				// -----------------------------------------------------------------	
				if (Utilities.sendSocketMessageSendStringAndWait (theContext, PublicData.phoneServer, 
						PublicData.socketNumber, "command message " + thePhoneNumber.replaceAll(" ","") + 
							StaticData.ACTION_DELIMITER + theMessage.replaceAll (" ",StaticData.SPACE_REPLACEMENT)))	
				{
					// ---------------------------------------------------------
					// 29/01/2016 ECU indicate that the server will be dealing
					//                with the call
					// ---------------------------------------------------------
					popToastAndSpeak (theContext.getString (R.string.phone_call_has_been_sent) + 
								      GetDeviceName (PublicData.phoneServer) + 
								      theContext.getString (R.string.for_processing));
					// ---------------------------------------------------------
				}
				else
				{
					// ---------------------------------------------------------
					// 29/01/2016 ECU indicate that the server could not send the
					//                message
					// ---------------------------------------------------------
					popToastAndSpeak (theContext.getString (R.string.unable_to_communicate_to_phone_server) + 
								      GetDeviceName (PublicData.phoneServer) + 
								      theContext.getString (R.string.acting_as_phone_server));
					// ---------------------------------------------------------
				}
									
			}
			else
			{
				// -----------------------------------------------------------------
				// 13/09/2013 ECU unable to make a phone call so inform the user
				// 03/02/2015 ECU changed to used the stored resource
				// -----------------------------------------------------------------			 
				popToast (theContext.getString(R.string.unable_to_send_sms));
			}
		}
	}
	/* ----------------------------------------------------------------------------- */
	public static void sendSMSMessage (String thePhoneNumber,String theMessage)
	{
		// -------------------------------------------------------------------------			
		// 20/06/2013 ECU created
		// -------------------------------------------------------------------------
		try 
		{
			SmsManager SMSManager = SmsManager.getDefault();
			SMSManager.sendTextMessage (thePhoneNumber, null, theMessage, null, null);
		} 
		catch (Exception theException) 
		{
		}	
	}
	/* ============================================================================= */
	public static void sendSocketMessageSendTheObject (Context theContext,String theIPAddress,int thePort,int theMessageType)
	{
		// -------------------------------------------------------------------------
		// 03/08/2013 ECU added - will just send the message type
		// 31/01/2015 ECU changed the name of the method called and of this method
		// 21/03/2015 ECU include the message type as an argument
		// -------------------------------------------------------------------------
		sendSocketMessageSendTheObject (theContext,theIPAddress,thePort,theMessageType,(Object) null);
	}
	/* ----------------------------------------------------------------------------- */
	public static void sendSocketMessageSendTheObject (Context theContext,String theIPAddress, int thePort,int theMessageType, Object theObject)
	{
		// -------------------------------------------------------------------------
		// 02/08/2013 ECU method added to handling the transmission / reception of Objects
		// 19/08/2013 ECU change to used buffered output
		// 31/01/2015 ECU changed the method name
		// 21/03/2015 ECU added the message type as an argument
		// -------------------------------------------------------------------------
		try
		{
			InetAddress serverAddress = InetAddress.getByName (theIPAddress);    
			
			Socket theSocket = new Socket (serverAddress,thePort); 

			try 
			{   
				// -----------------------------------------------------------------
				// 19/08/2013 ECU changed to use a buffered output stream
				// -----------------------------------------------------------------
				BufferedOutputStream output = new BufferedOutputStream (theSocket.getOutputStream());
				// -----------------------------------------------------------------
				// 30/07/2013 ECU write out the type of socket message being sent
				// 11/08/2013 ECU change so that transmission of message type is via
				//                the method rather than output.write and output.flush
				// 19/08/2013 ECU add the message type into the header message
				// 31/01/2015 ECU changed the method name
				// 21/03/2015 ECU pass the message type as an argument
				// -----------------------------------------------------------------
				Utilities.socketSendMessageType (theMessageType,PublicData.socketMessageData, output);
				// -----------------------------------------------------------------
				// 02/08/2013 ECU set up the object output stream and write out the object
				// 03/08/2013 ECU added - check for a null on the object
				// ------------------------------------------------------------------
				if (theObject != null)
				{
					ObjectOutputStream outputObject = new ObjectOutputStream (output);
				
					outputObject.writeObject (theObject);
					outputObject.flush ();				
							
					outputObject.close();
				}
				else
				{
					// -------------------------------------------------------------
					// 03/08/2013 ECU no object was sent so just close the stream
					// -------------------------------------------------------------
					output.close ();
				}
			} 
			catch (Exception theException) 
			{   
			} 
			finally
			{                
					theSocket.close();            
			} 
		}
		catch (IOException theException)
		{
		}
	}
	/* ----------------------------------------------------------------------------- */
	public static boolean sendSocketMessageSendStringAndWait (Context theContext,String theIPAddress, int thePort, String theMessage)
	{
		// -------------------------------------------------------------------------
		// send a socket message but do not worry about a response
		// 31/01/2015 ECU change the name of the method called
		// 29/01/2016 ECU changed to boolean to return the status of the 'sendSocket....'
		// -------------------------------------------------------------------------
		return sendSocketMessageSendStringAndWait (theContext,theIPAddress,thePort,theMessage,false);
		// --------------------------------------------------------------------------
	}
	/* ----------------------------------------------------------------------------- */
	public static void sendSocketMessageSendStringToAllDevices (Context theContext,List<Devices> theDevices,int thePort, String theMessage)
	{
		// -------------------------------------------------------------------------
		// 26/07/2013 ECU added to send the message to all registered devices except
		//                the sending device
		// 31/01/2015 ECU change the method name
		// 22/03/2015 ECU change the logic to reflect the redefinition of deviceDetails
		//                to List<Devices>
		// -------------------------------------------------------------------------
		if (theDevices != null)
		{
			for (int theDevice=0; theDevice < theDevices.size(); theDevice++)
			{
				if (!theDevices.get(theDevice).IPAddress.equalsIgnoreCase(PublicData.ipAddress))
				{
					// -------------------------------------------------------------
					// 31/01/2015 ECU change the name of the method called
					// -------------------------------------------------------------
					sendSocketMessageSendStringAndWait (theContext,theDevices.get(theDevice).IPAddress,thePort,theMessage);
				}
			}
		}
		// -------------------------------------------------------------------------
	}
	/* ----------------------------------------------------------------------------- */
	public static void sendSocketMessageSendObjectToAllDevices (Context theContext,
																List<Devices> theDevices,
																int thePort, 
																int theMessageType,
																Object theObject,
																boolean	theAllDevicesFlag)
	{
		// -------------------------------------------------------------------------
		// 21/10/2014 ECU added to send the object to all registered devices except
		//                the sending device and which are compatible
		//            ECU added in the compatibility check
		// 31/01/2015 ECU changed the method name
		// 21/03/2015 ECU the message type added as an argument
		// 22/03/2015 ECU change the logic to reflect the change of deviceDetails to
		//				  List<Devices>
		// 31/07/2016 ECU added theAllDevicesFlag which indicates that, if true, 
		//                indicates that the object is to be sen to all devices
		//                including this one
		// -------------------------------------------------------------------------
		if (theDevices != null)
		{
			for (int theDevice=0; theDevice < theDevices.size(); theDevice++)
			{
				if ((theAllDevicesFlag || !theDevices.get(theDevice).IPAddress.equalsIgnoreCase (PublicData.ipAddress)) &&
						theDevices.get(theDevice).compatible)
				{
					// -------------------------------------------------------------
					// 31/01/2015 ECU changed the name of the method called
					// 21/03/2015 ECU pass through the message type as an argument
					// -------------------------------------------------------------
					sendSocketMessageSendTheObject (theContext,
													theDevices.get(theDevice).IPAddress,
													thePort,
													theMessageType,
													theObject);
				}
			}
		}
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void sendSocketMessageSendObjectToAllDevices (Context theContext,
																List<Devices> theDevices,
																int thePort, 
																int theMessageType,
																Object theObject)
	{
		// -------------------------------------------------------------------------
		// 31/07/2016 ECU this was the old master method but changed when theAllDevicesFlag
		//                was added - so this method calls the master with the flag
		//                set to 'false'
		// -------------------------------------------------------------------------
		sendSocketMessageSendObjectToAllDevices (theContext,theDevices,thePort,theMessageType,theObject,false);
		// -------------------------------------------------------------------------
	}
	/* ----------------------------------------------------------------------------- */
	public static boolean sendSocketMessageSendStringAndWait (Context theContext,String theIPAddress,
							int thePort, String theMessage,boolean theResponseFlag)
	{
		// -------------------------------------------------------------------------
		// 31/01/2015 ECU changed the method name
		// 29/01/2016 ECU changed to boolean to indicate if able to send the message
		//				  return true ..... message sent correctly
		//                       false .... failed to send message
		// -------------------------------------------------------------------------
		try 
		{         
			// ---------------------------------------------------------------------
			PrintWriter output;   
			// ---------------------------------------------------------------------
			// 19/08/2013 ECU default to no response
			// ---------------------------------------------------------------------
			socketResponse = null;
				
			InetAddress serverAddress = InetAddress.getByName(theIPAddress);  
			
			Socket theSocket = new Socket (serverAddress,thePort);   
			
			try 
			{   
				output = new PrintWriter (new BufferedWriter(new OutputStreamWriter(theSocket.getOutputStream())), true); 
				output.println(theMessage + "\n");
				output.flush();
				// -----------------------------------------------------------------
				// 25/07/2013 ECU check for a response
				// -----------------------------------------------------------------
				if (theResponseFlag)
				{
					socketResponse = getSocketMessage (theContext,theSocket);
				}
				// -----------------------------------------------------------------
				// 29/01/2016 ECU indicate everything seems OK
				// -----------------------------------------------------------------
				return true;
				// -----------------------------------------------------------------
			} 
			catch (Exception theException) 
			{                   
				PublicData.datagram.Message("Exception Writing 1 : " + theException); 
				// -----------------------------------------------------------------
				// 29/01/2016 ECU indicate that a failure occurred
				// -----------------------------------------------------------------
				return false;
				// -----------------------------------------------------------------
			} 
			finally
			{                
				theSocket.close();             
			}           
		} 
		catch (Exception theException)
		{  
			PublicData.datagram.Message("Exception Writing  2 : " + theException); 
			// ---------------------------------------------------------------------
			// 29/01/2016 ECU indicate that an error occurred
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}   
	}
	// =============================================================================
	@SuppressLint("NewApi") 
	public static void SetAnExactAlarm (AlarmManager theAlarmManager,long theTime,PendingIntent thePendingIntent)
	{
		// -------------------------------------------------------------------------
		// 24/12/2015 ECU created to use the AlarmManager to set an 'exact' alarm
		// -------------------------------------------------------------------------
		// 24/12/2015 ECU with the introduction of KitKat the 'set' method produced
		//                an 'inexact' alarm which came up at roughly the time required
		//                but could be some time out - however in this app an
		//                exact time is wanted so use the new 'setExact' method
		// 23/11/2016 ECU put in try..catch because on one occasion got a NPE when
		//                the app was restarted after being 'swapped' out
		// -------------------------------------------------------------------------
		try
		{
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
			{
				// -----------------------------------------------------------------
				// 24/12/2015 ECU 'set' provides an exact time
				// -----------------------------------------------------------------
				theAlarmManager.set (AlarmManager.RTC_WAKEUP,theTime,thePendingIntent);
			}
			else
			{
				// -----------------------------------------------------------------
				// 24/12/2015 ECU 'setExact' provides an exact time
				// -----------------------------------------------------------------
				theAlarmManager.setExact (AlarmManager.RTC_WAKEUP,theTime,thePendingIntent);
			}
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 23/11/2016 ECU log the exception in an attempt to find the rare problem
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG,"SetAnExactAlarm : " + theException.toString(),true);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static String setTheDateTimeString ()
	{
		// -------------------------------------------------------------------------
		// 20/11/2013 ECU added to return the current time and date as a string
		// -------------------------------------------------------------------------
		Calendar calendar = Calendar.getInstance(); 
		// -------------------------------------------------------------------------
		// 04/04/2014 ECU changed to use the stored format
		// 05/01/2015 ECU put in the check on null
		// -------------------------------------------------------------------------
		if (PublicData.dateFormatterCurrent != null)
			return PublicData.dateFormatterCurrent.format (calendar.getTime());	
		else
			return null;
	}
	/* ============================================================================= */
	public static void setDeviceName (Context theContext,String theIPAddress,String theName)
	{
		// -------------------------------------------------------------------------
		// 06/08/2013 ECU try and find the specified device name
		// 22/03/2015 ECU changed the logic to accommodate the redefinition of
		//                deviceDetails to List<Devices>
		// -------------------------------------------------------------------------
		if (PublicData.deviceDetails != null)
		{
			for (int theDevice = 0; theDevice < PublicData.deviceDetails.size(); theDevice++)
			{
				if (PublicData.deviceDetails.get(theDevice).IPAddress.equalsIgnoreCase (theIPAddress))
				{
					// -------------------------------------------------------------
					// 06/08/2013 ECU have found the specified device so set the name
					// -------------------------------------------------------------
					PublicData.deviceDetails.get(theDevice).name = theName;
					// -------------------------------------------------------------
					// 10/11/2016 ECU just check if changing the name of 'this' device
					//                in which case update local copy
					// -------------------------------------------------------------
					if (theIPAddress.equalsIgnoreCase (PublicData.ipAddress))
						PublicData.localDeviceDetails.name = theName;
					// -------------------------------------------------------------
					// 06/08/2013 ECU make sure the disk is updated
					// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
					// -------------------------------------------------------------
					AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + theContext.getString (R.string.devices_file),PublicData.deviceDetails);
				}
			}
		}
	}
	// =============================================================================
	public static void setTheScreenState (boolean theState)
	{
		// -------------------------------------------------------------------------
		// 05/05/2015 ECU created to set the screen to the state specified
		//
		//                theState = true  ......... turn the screen on
		//                         = false ......... turn the screen off
		// -------------------------------------------------------------------------
		if (GridActivity.activity != null)
		{
			if (theState)
			{
				// -----------------------------------------------------------------
				// 05/05/2015 ECU turn the screen on
				// -----------------------------------------------------------------
				GridActivity.activity.getWindow().addFlags (WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 05/05/2015 ECU turn the screen off
				// -----------------------------------------------------------------
				GridActivity.activity.getWindow().clearFlags (WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
				// -----------------------------------------------------------------
			}
		}
	}
	// =============================================================================
	public static void SetUpActivity (Activity theActivity)
	{
		// -------------------------------------------------------------------------
		// 16/02/2014 ECU created when the caller does not require full
		//                screen working
		// -------------------------------------------------------------------------	
		SetUpActivity (theActivity,!StaticData.ACTIVITY_FULL_SCREEN);
	}
	/* ----------------------------------------------------------------------------- */
	public static void SetUpActivity (Activity theActivity,
			                          boolean theFullScreenFlag)
	{
		// -------------------------------------------------------------------------
		// 08/04/2014 ECU changed to use the new method below
		// -------------------------------------------------------------------------
		SetUpActivity (theActivity,
					   theFullScreenFlag,
					   !StaticData.ACTIVITY_SCREEN_ON);
	}
	// -----------------------------------------------------------------------------
	public static void SetUpActivity (Activity 	theActivity,
									  boolean 	theFullScreenFlag,
									  boolean 	theKeepScreenActiveFlag)
	{
		// -------------------------------------------------------------------------
		// 03/05/2015 ECU created - was the old master method now changed to call
		//                the new master method with PORTRAIT orientation
		// -------------------------------------------------------------------------
		SetUpActivity (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
					   theActivity,
					   theFullScreenFlag,
					   theKeepScreenActiveFlag);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void SetUpActivity (Activity 	theActivity,
									  boolean 	theFullScreenFlag,
									  boolean   theTitleFlag,
									  boolean 	theKeepScreenActiveFlag)
	{
		// -------------------------------------------------------------------------
		// 28/11/2016 ECU created to call the master method with control of
		//                the display or not of the title
		// -------------------------------------------------------------------------
		SetUpActivity (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
					   theActivity,
					   theFullScreenFlag,
					   theKeepScreenActiveFlag,
					   theTitleFlag);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void SetUpActivity (int		theOrientation,
			  						  Activity 	theActivity,
			  						  boolean 	theFullScreenFlag,
			  						  boolean 	theKeepScreenActiveFlag)
	{
		// -------------------------------------------------------------------------
		// 03/05/2015 ECU created - was the old master method now changed to call
		//                the new master method with PORTRAIT orientation
		// 28/11/2016 ECU this was the old 'master' method
		// -------------------------------------------------------------------------
		SetUpActivity (theOrientation,
					   theActivity,
					   theFullScreenFlag,
					   theKeepScreenActiveFlag,
					   false);
		// -------------------------------------------------------------------------
	}
	/* ----------------------------------------------------------------------------- */
	public static void SetUpActivity (int		theOrientation,
									  Activity 	theActivity,
									  boolean 	theFullScreenFlag,
									  boolean 	theKeepScreenActiveFlag,
									  boolean   theTitleFlag)
	{
		// -------------------------------------------------------------------------
		// 16/02/2014 ECU created to set up certain parameters that are 
		//                required by every activity
		//
		//				  1 .. set orientation (normally PORTRAIT)
		//                2 .. set 'full screen with no title' mode if requested
		//                3 .. whether the screen is to be kept active
		//                6 .. whether the title is to be displayed irrespective
		//                     of argument 2
		//
		// 08/04/2014 ECU added the 'theKeepScreenActiveFlag' argument
		// 03/05/2015 ECU added 'theOrientation' as an argument
		// 28/11/2016 ECU added 'theTitleFlag' as an argument
		// -------------------------------------------------------------------------
		theActivity.setRequestedOrientation (theOrientation);
		// -------------------------------------------------------------------------
		// 16/02/2014 ECU check if need to set full screen mode
		// -------------------------------------------------------------------------
		if (theFullScreenFlag)
		{
			theActivity.getWindow().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN,
											  WindowManager.LayoutParams.FLAG_FULLSCREEN);
			// ---------------------------------------------------------------------
			// 28/11/2016 ECU check if the title is to be displayed
			// ---------------------------------------------------------------------
			if (!theTitleFlag)
				theActivity.requestWindowFeature (Window.FEATURE_NO_TITLE);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 08/04/2014 ECU optionally set that the screen is to be kept active
		// 03/05/2015 ECU if the battery is below the lower monitoring point
		//                then do NOT keep the screen on. The check is present just
		//                in case this is called before the service is up and running
		//                and has received a status update for the battery
		// -------------------------------------------------------------------------
		if (theKeepScreenActiveFlag)
		{
			if (SensorService.batteryLevel != StaticData.NO_RESULT && 
				(SensorService.batteryLevel >= PublicData.storedData.battery.lowerTrigger))
			{
				// -----------------------------------------------------------------
				// 03/05/2015 ECU battery has enough charge so keep screen on
				// -----------------------------------------------------------------
				theActivity.getWindow().addFlags (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				// -----------------------------------------------------------------
				// 08/04/2014 ECU let the user know what is happening
				// 14/10/2014 ECU include the speak bit
				// 24/10/2015 ECU only display the message if the app has been 
				//                started manually
				// 02/04/2016 ECU added the check on startedByAlarm
				// -----------------------------------------------------------------
				if (PublicData.startedManually && !PublicData.startedByAlarm)
					Utilities.popToastAndSpeak (theActivity.getString(R.string.keep_screen_on),true);
				// -----------------------------------------------------------------
			}
		}
	}
	// =============================================================================
	public static Drawable setWallPaper (Context theContext,int theDrawableId)
	{
		// -------------------------------------------------------------------------
		// 10/12/2015 ECU created to set the drawable to that specified
		// -------------------------------------------------------------------------
		Drawable existingWallpaper = null;
		// -------------------------------------------------------------------------
		try
		{
			final WallpaperManager wallpaperManager = WallpaperManager.getInstance (theContext);
			// ---------------------------------------------------------------------
			// 10/12/2015 ECU get the current wallpaper
			// ---------------------------------------------------------------------
			existingWallpaper = wallpaperManager.getDrawable();
			// ---------------------------------------------------------------------
			wallpaperManager.setBitmap (((BitmapDrawable) theContext.getResources().getDrawable(theDrawableId)).getBitmap());
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
		}
		// -------------------------------------------------------------------------
		// 10/12/2015 ECU return the currently set wallpaper
		// -------------------------------------------------------------------------
		return existingWallpaper;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void setWallPaper (Context theContext,Drawable theDrawable)
	{
		// -------------------------------------------------------------------------
		// 10/12/2015 ECU created to set the drawable to that specified
		// -------------------------------------------------------------------------
		try
		{
			final WallpaperManager wallpaperManager = WallpaperManager.getInstance (theContext);
			// ---------------------------------------------------------------------
			wallpaperManager.setBitmap(((BitmapDrawable) theDrawable).getBitmap());
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	// =============================================================================
	// 31/01/2015 ECU NOTES ABOUT socketMessages
	//                ==========================
	//                These methods exist to handle communication on the data socket
	//                - they are a real mess but difficult to change at this stage
	//                and at least they seem to work
	//                Slowly changing the name to be more meaningful
	// =============================================================================
	// =============================================================================
	public static Object socketMessagesReadObject (Context theContext,Socket theSocket)
	{
		Object localObject = null;
		// -------------------------------------------------------------------------
		// 02/08/2013 ECU method added to read an object on the specified socket
		//
		// 06/01/2015 ECU changed to pick up all exceptions
		// -------------------------------------------------------------------------			
		try
		{	
			// ---------------------------------------------------------------------
			// 02/08/2013 ECU set up the input streams for normal and the object
			// ---------------------------------------------------------------------
			InputStream input = theSocket.getInputStream();
			ObjectInputStream inputObject = new ObjectInputStream (input);
			// ---------------------------------------------------------------------
			// 02/08/2013 ECU read in the object
			// ---------------------------------------------------------------------
			localObject = inputObject.readObject();
			// ---------------------------------------------------------------------
			// 02/08/2013 ECU close down the streams
			// ---------------------------------------------------------------------
			inputObject.close ();
		}
		catch (Exception theException)
		{	
			PublicData.datagram.Message("IOException 3 " + theException);
		} 
		return (Object) localObject;
	}
	/* ----------------------------------------------------------------------------- */
	public static int socketMessagesReadIntoBuffer (Context theContext,Socket theSocket, byte theBuffer[],int theLength)
	{
		// -------------------------------------------------------------------------
		// 31/01/2015 ECU changed the method name to indicate that data is being
		//                read into a buffer
		// -------------------------------------------------------------------------
		return socketMessages (theContext,theSocket,theBuffer,theLength,null);
	}
	/* ----------------------------------------------------------------------------- */
	public static int socketMessages (Context theContext,Socket theSocket, byte theBuffer[],int theLength,String theFileName)
	{
		// -------------------------------------------------------------------------
		// 31/07/2013 ECU method added
		// 12/08/2013 ECU removed theSocket.close because this method did not open it
		// -------------------------------------------------------------------------
		int numberOfBytesReceived = 0;
		RandomAccessFile fileHandle = null;
		
		try
		{
			
			if (theFileName != null)
			{
				fileHandle = new RandomAccessFile(theFileName, "rw");
				// -----------------------------------------------------------------
				// 03/08/2013 ECU make sure the file is empty
				// -----------------------------------------------------------------
				fileHandle.setLength(0);
			}
			InputStream input = theSocket.getInputStream();
			// ---------------------------------------------------------------------
			// 30/07/2013 ECU read in the whole buffer in chunks so that nothing is lost
			// ---------------------------------------------------------------------
			int theBytesReadThisChunk = 0;
			// ---------------------------------------------------------------------
			// 30/07/2013 ECU read in the chunks until the 'end of data' flag of '-1' is returned
			// 01/09/2015 ECU changed to use StaticData
			// ---------------------------------------------------------------------
			while ((theBytesReadThisChunk = input.read(theBuffer,numberOfBytesReceived,StaticData.SOCKET_CHUNK_SIZE))> 0)
			{
				// -----------------------------------------------------------------
				// 03/08/2013 ECU write to the file if required
				// -----------------------------------------------------------------
				if (fileHandle != null)
					fileHandle.write(theBuffer, numberOfBytesReceived,theBytesReadThisChunk);
				else
				{
					// -------------------------------------------------------------
					// 30/07/2013 ECU add into the running total
					// -------------------------------------------------------------
					numberOfBytesReceived += theBytesReadThisChunk;
				}
				// -----------------------------------------------------------------
				// 10/08/2013 ECU check if there is room for another chunk
				// 01/09/2015 ECU changed to use StaticData
				// -----------------------------------------------------------------
				if ((numberOfBytesReceived + StaticData.SOCKET_CHUNK_SIZE) >= theLength)
				{
					// -------------------------------------------------------------
					// 10/08/2013 ECU there is no more room in the buffer so break out of the loop
					// -------------------------------------------------------------
					break;
				}
			}
			// ---------------------------------------------------------------------
			// 03/08/2013 ECU close the file - if in use
			// ---------------------------------------------------------------------
			if (fileHandle != null)
				fileHandle.close();
			// ---------------------------------------------------------------------
			// 30/07/2013 ECU close the input stream
			// ---------------------------------------------------------------------
			input.close();	
		}
		catch (IOException theException)
		{
			
		}
		// -------------------------------------------------------------------------
		return numberOfBytesReceived;
	}
	/* ----------------------------------------------------------------------------- */
	public static void socketMessagesReadFileInChunks (Context theContext,Socket theSocket,String theFileName,int theChunkSize)
	{
		// -------------------------------------------------------------------------
		// 04/08/2013 ECU method added
		// 12/08/2013 ECU remove theSocket.close because this method did not open it
		// 31/01/2015 ECU this method is only used to receive a music file which will
		//                be sent in chunks
		//            ECU changed the method name
		// -------------------------------------------------------------------------
		int theBytesReadThisChunk;
		int incomingFileSize = 0;						// 10/08/2013 ECU size of the incoming file
		byte [] inputBuffer = new byte [theChunkSize];
		int numberOfBytesReceived = 0;					// 10/08/2013 ECU total number of bytes received
		String sendingIPAddress;						// 14/08/2013 ECU the address of the sending device
		// -------------------------------------------------------------------------
		// 14/08/2013 ECU preset the received file name to nothing in case an error occurs
		// -------------------------------------------------------------------------
		PublicData.receivedFile = "";
		// -------------------------------------------------------------------------
		// 14/08/2013 ECU store the IP address of the sending device
		// -------------------------------------------------------------------------
		sendingIPAddress = theSocket.getInetAddress().getHostAddress();
		// -------------------------------------------------------------------------
		// 13/08/2013 ECU indicate that a file is being received
		// -------------------------------------------------------------------------
		PublicData.receivingFile = true;
		
		Utilities.debugMessage(TAG,"Starting to receive " + theFileName);
		
		try
		{
			// ---------------------------------------------------------------------
			// open to the destination file - will create it if not there
			// ---------------------------------------------------------------------
			RandomAccessFile fileHandle = new RandomAccessFile(theFileName, "rw");
			// ---------------------------------------------------------------------	
			// 04/08/2013 ECU make sure the file is empty
			// ---------------------------------------------------------------------	
			fileHandle.setLength(0);
			// ---------------------------------------------------------------------
			// 10/08/2013 ECU change InputStream to BufferedInputStream
			// ---------------------------------------------------------------------
			BufferedInputStream inputStream = new BufferedInputStream (theSocket.getInputStream());	
			// ---------------------------------------------------------------------			
			// 10/08/2013 ECU get the size of the file that is being received - this is sent as a 
			//				  byte [] before the data for the file
			// 01/09/2015 ECU changed to use StaticData
			// ---------------------------------------------------------------------
			theBytesReadThisChunk = inputStream.read (inputBuffer,0,StaticData.INTEGER_SIZE);
			
			if (theBytesReadThisChunk == StaticData.INTEGER_SIZE)
			{
				incomingFileSize = byteArrayToInteger (inputBuffer);
			}
			// ---------------------------------------------------------------------
			// 10/08/2013 ECU reset the total number of bytes received
			// ---------------------------------------------------------------------
			numberOfBytesReceived = 0;
			// ---------------------------------------------------------------------
			// 30/07/2013 ECU read in the whole buffer in chunks so that nothing is lost
			// ---------------------------------------------------------------------
			theBytesReadThisChunk = 0;
			// ---------------------------------------------------------------------
			// 30/07/2013 ECU read in the chunks until the 'end of data' flag of '-1' is returned
			// ---------------------------------------------------------------------
			while ((theBytesReadThisChunk = inputStream.read(inputBuffer,0,theChunkSize))> 0)
			{
				// -----------------------------------------------------------------
				// 10/08/2013 ECU add the number into the total
				// -----------------------------------------------------------------
				numberOfBytesReceived += theBytesReadThisChunk;
				// -----------------------------------------------------------------
				// 03/08/2013 ECU write to the file if required
				// -----------------------------------------------------------------			
				fileHandle.write(inputBuffer,0,theBytesReadThisChunk);
			}
			// ---------------------------------------------------------------------
			// 03/08/2013 ECU close the file - if in use
			// ---------------------------------------------------------------------	
			fileHandle.close();
			// ---------------------------------------------------------------------
			// 30/07/2013 ECU close the input stream
			// ---------------------------------------------------------------------
			inputStream.close();
			// ---------------------------------------------------------------------
			// 10/08/2013 ECU just check if what was received was of the correct length
			// ---------------------------------------------------------------------
			if (incomingFileSize != numberOfBytesReceived)
			{
				// -----------------------------------------------------------------
				// 10/08/2013 ECU indicate that something was wrong
				//            ECU put in corrective action here later
				// 14/08/2013 ECU change to debugMessage from logging to logcat
				// -----------------------------------------------------------------
				debugMessage (TAG,"expected " + incomingFileSize + " received " + numberOfBytesReceived,true);
				// -----------------------------------------------------------------
				// 14/08/2013 ECU tell the server that the file has not been received correctly
				// -----------------------------------------------------------------
				Utilities.sendDatagramType (theContext,sendingIPAddress,StaticData.SOCKET_MESSAGE_FILE_NAK);
			}
			else
			{	
				// -----------------------------------------------------------------
				// 14/08/2013 ECU the file was received correctly so remember the files name
				// -----------------------------------------------------------------
				PublicData.receivedFile = theFileName;
				// -----------------------------------------------------------------
				// 14/08/2013 ECU tell the server that the file has been received OK
				// -----------------------------------------------------------------
				Utilities.sendDatagramType (theContext,sendingIPAddress,StaticData.SOCKET_MESSAGE_FILE_ACK);
				// -----------------------------------------------------------------
				// 20/08/2013 ECU set up the timer for receiving the associated PLAY command
				//				  but only if a file is not currently being played
				// 01/09/2015 ECU changed to use StaticData
				// -----------------------------------------------------------------
				if (!PublicData.trackBeingPlayed)
					PublicData.playTimeout = StaticData.PLAY_TIMEOUT;
			}
		}
		catch (IOException theException)
		{
			Utilities.debugMessage(TAG,"IOException:" + theException);		
			// ---------------------------------------------------------------------
			// 14/08/2013 ECU tell the server that the file has not been received correctly
			// ---------------------------------------------------------------------
			Utilities.sendDatagramType (theContext,sendingIPAddress,StaticData.SOCKET_MESSAGE_FILE_NAK);
		}
		// -------------------------------------------------------------------------
		// 13/08/2013 ECU indicate that a file has finished being received
		// -------------------------------------------------------------------------
		PublicData.receivingFile = false;
		
		Utilities.debugMessage(TAG,"Finished receiving " + theFileName);	
				
	}
	/* --------------------------------------------------------------------- */
	public static void socketMessagesSendMessageType (Context theContext,String theIPAddress, 
														int thePort, int theMessageType)
	{
		// ------------------------------------------------------------------
		// 06/08/2013 ECU created - just send a message with the specified message type
		// 31/01/2015 ECU changed the method name
		// 21/03/2015 ECU pass through the message type as an argument
		// ------------------------------------------------------------------
		socketMessagesSendTheBuffer (theContext,
									 true,
									 theIPAddress,
									 thePort,
									 theMessageType,
									 null,
									 0);
	}
	/* --------------------------------------------------------------------- */
	public static int socketMessagesSendTheBuffer (Context theContext,
			boolean theReceiveTransmitFlag,String theIPAddress, int thePort,int theMessageType,byte theBuffer[],int theLength)
	{
		// -------------------------------------------------------------------------
		// 29/07/2013 ECU added
		//                theReceiveTransmitFlag = false for reception
		//                                       = true  for transmission
		// 30/07/2013 ECU changed the arguments and code to use 'byte []' rather than 'char []'
		//                MainActivity.socketMessageType will contain the type of
		//                message to be sent
		// 19/08/2013 ECU changed to used a buffered output stream
		// 31/01/2015 ECU changed the method name
		// 21/03/2015 ECU include theMessageType as an argument
		// -------------------------------------------------------------------------
		int numberOfBytesReceived = 0;
		
		try
		{
			InetAddress serverAddress = InetAddress.getByName(theIPAddress);    
			// ---------------------------------------------------------------------
			// 03/08/2013 ECU decide whether transmission (true) or reception (false)
			// ---------------------------------------------------------------------
			if (theReceiveTransmitFlag)
			{
				// -----------------------------------------------------------------
				// 29/07/2013 ECU transmission
				// -----------------------------------------------------------------
				Socket theSocket = new Socket (serverAddress,thePort);  
				
				try 
				{   
					// -------------------------------------------------------------
					// 19/08/2013 ECU changed to use a buffered output stream
					// -------------------------------------------------------------
					BufferedOutputStream output = new BufferedOutputStream(theSocket.getOutputStream());
					// -------------------------------------------------------------
					// 30/07/2013 ECU write out the type of socket message being sent
					// 11/08/2013 ECU change so that transmission of message type is via
					//                the method rather than output.write and output.flush
					// 31/01/2015 ECU changed the method name
					// 21/03/2015 ECU 
					// -------------------------------------------------------------
					Utilities.socketSendMessageType (theMessageType, output);
					// -------------------------------------------------------------
					// 30/07/2013 ECU send out the actual buffer if it exists
					// 06/08/2013 ECU added the check on null
					// -------------------------------------------------------------
					if (theBuffer != null)
					{
						output.write(theBuffer,0,theLength);
						output.flush();
					}
					// -------------------------------------------------------------
					// 29/07/2013 ECU close the output stream
					// -------------------------------------------------------------
					output.close();
				} 
				catch (Exception theException) 
				{                   
					PublicData.datagram.Message("Exception Writing : " + theException);           
				} 
				finally
				{                
					theSocket.close();             
				} 
			}
			else
			{
				// -------------------------------------------------------------
				// 29/07/2013 ECU reception
				// 30/07/2013 ECU changed the code to read in chunks rather than trying to
				//                read in the whole buffer which caused only some of the
				//                buffer to be read
								
				// 29/07/2013 ECU wait for a connection of the specified port after
				//				  first creating it
				// -------------------------------------------------------------
				ServerSocket theServerSocket = new ServerSocket (thePort); 
				
				Socket theSocket = theServerSocket.accept();
				// -----------------------------------------------------------------
				// 31/07/2013 ECU added call to the new method
				// 31/01/2015 ECU changed the name to new method
				// -----------------------------------------------------------------
				numberOfBytesReceived = socketMessagesReadIntoBuffer (theContext,theSocket,theBuffer,theLength);
	
				theServerSocket.close();
			}
		}
		catch (IOException theException)
		{
			PublicData.datagram.Message("IOException 6 : " + theException); 
		}
		// -------------------------------------------------------------------------
		// 30/07/2013 ECU return the total number of bytes that have been read
		// -------------------------------------------------------------------------
		return numberOfBytesReceived;
		// -------------------------------------------------------------------------	
	}
	/* ============================================================================= */
	public static void socketSendMessageType (int theMessageType,BufferedOutputStream theOutputStream)
	{
		socketSendMessageType (theMessageType,PublicData.socketMessageData,theOutputStream);
	}
	/* ----------------------------------------------------------------------------- */
	public static void socketSendMessageType (int theMessageType,int theMessageData,BufferedOutputStream theOutputStream)
	{
		// -------------------------------------------------------------------------
		// 11/08/2013 ECU this method does the transmission
		// 19/08/2013 ECU changed the argument from OutputStream to BufferedOutputStream
		// 31/01/2015 ECU change method name
		// -------------------------------------------------------------------------
		try
		{
			Utilities.debugMessage (TAG,"Sending Message Type " + theMessageType);
			// ---------------------------------------------------------------------
			// 11/08/2013 ECU transmit the supplied message type
			// 01/09/2015 ECU changed to use StaticData
			// ---------------------------------------------------------------------
			PublicData.socketHeader [StaticData.SOCKET_TYPE_OFFSET] = (byte) theMessageType;
			// ---------------------------------------------------------------------
			// 19/08/2013 ECU set up the data byte to be sent
			// ---------------------------------------------------------------------
			PublicData.socketHeader [StaticData.SOCKET_DATA_OFFSET] = (byte) theMessageData;
			// ---------------------------------------------------------------------
			// 19/08/2013 ECU write out the data and flush it 'out'
			// ---------------------------------------------------------------------
			theOutputStream.write (PublicData.socketHeader);
			theOutputStream.flush ();
			// ---------------------------------------------------------------------
		}
		catch (IOException theException)
		{
		}
	}
	/* ----------------------------------------------------------------------------- */
	public static byte [] socketReceiveMessageType (InputStream theInputStream)
	{
		// -------------------------------------------------------------------------
		// 11/08/2013 ECU this method handles the reception
		// 19/08/2013 ECU altered to accommodate a data byte
		//			  ECU return the two bytes of message information
		//				  rather than just the message type
		// -------------------------------------------------------------------------
		byte [] incomingMessageDetails = {StaticData.NO_RESULT,StaticData.NO_RESULT};
		byte incomingHeader [] = new byte [PublicData.socketHeader.length];
		boolean haveValidHeader = false;
		int skippedBytes = -1;							// 14/08/2013 ECU count of bytes skipped looking
														//                for header
		
		try
		{
			// ---------------------------------------------------------------------
			// 11/08/2013 ECU the following is what is proposed
			// ---------------------------------------------------------------------
			// 11/08/2013 ECU wait for the introducing character
			// ---------------------------------------------------------------------
			while (!haveValidHeader)
			{
				// -----------------------------------------------------------------
				// 14/08/2013 ECU count bytes skipped
				// -----------------------------------------------------------------
				skippedBytes++;
				
				if ((incomingHeader[0] = (byte) theInputStream.read()) == PublicData.socketHeader[0])
				{
					// -------------------------------------------------------------
					// 11/08/2013 ECU have the first character so check the next few characters
					// -------------------------------------------------------------
					// 11/08/2013 ECU default to having a valid header
					// -------------------------------------------------------------
					haveValidHeader = true;
				
					for (int theIndex = 1; theIndex < PublicData.socketHeader.length; theIndex++)
					{
						incomingHeader [theIndex] = (byte) theInputStream.read ();
						// ---------------------------------------------------------
						// 19/08/2013 ECU added a test on SOCKET_DATA_OFFSET
						// 01/09/2015 ECU changed to use StaticData
						// ---------------------------------------------------------
						if ((theIndex != StaticData.SOCKET_TYPE_OFFSET) && (theIndex != StaticData.SOCKET_DATA_OFFSET))
						{
							if (incomingHeader[theIndex] != PublicData.socketHeader[theIndex])
							{
								// -------------------------------------------------
								// 11/08/2013 ECU have a mismatch so header is wrong
								// -------------------------------------------------
								haveValidHeader = false;
								// -------------------------------------------------
								// 12/08/2013 ECU check if the invalid character is the introducer
								// -------------------------------------------------
								if (incomingHeader[theIndex] == PublicData.socketHeader[0])
								{
									// ---------------------------------------------
									// 12/08/2013 ECU have a new introducer so want to force
									//                a recheck of incoming characters
									//				  don't like doing it this way but if it works !!
									// ---------------------------------------------
									theIndex = 0;
									// ---------------------------------------------
									// 12/08/2013 ECU default to having a valid header
									// ---------------------------------------------
									haveValidHeader = true;
								}
								else
								{
									// ---------------------------------------------
									// 12/08/2013 ECU invalid sequence so just break 
									//                out of the loop
									// ---------------------------------------------
									break;
								}		
							}
						}
					}
					// -------------------------------------------------------------
					// 11/08/2013 ECU check if we have a valid header - if so can return with the
					//                message type
					// 01/09/2015 ECU changed to use StaticData
					// -------------------------------------------------------------
					if (haveValidHeader)
					{
						incomingMessageDetails [0] = incomingHeader [StaticData.SOCKET_TYPE_OFFSET];
						incomingMessageDetails [1] = incomingHeader [StaticData.SOCKET_DATA_OFFSET];
					}
					// -------------------------------------------------------------
					break;
				}
			}
		}
		catch (IOException theException)
		{
			Utilities.debugMessage(TAG,"IOException " + theException);
		}
		// -------------------------------------------------------------------------
		// 14/08/2013 ECU included skippedBytes in debug message 
		// -------------------------------------------------------------------------
		Utilities.debugMessage(TAG,"Message Type Received " + incomingMessageDetails[0] + 
								"  Data " + incomingMessageDetails[1] + " skipped = " + skippedBytes);
		// -------------------------------------------------------------------------
		// 11/08/2013 ECU return the obtained message type or ...NO_RESULT
		//                if error occurred
		// 19/08/2013 ECU return the details in the message
		// -------------------------------------------------------------------------
		return incomingMessageDetails;
	}
	/* ============================================================================= */
	public static boolean socketTest (String theIPAddress,int thePort)
	{
		// -------------------------------------------------------------------------
		// 31/07/2013 ECU created
		// -------------------------------------------------------------------------
		boolean localResult = false;
		
		try
		{
			InetAddress serverAddress = InetAddress.getByName(theIPAddress);    
			Socket theSocket = new Socket (serverAddress,thePort); 
			theSocket.close();
			// ---------------------------------------------------------------------
			// 31/07/2013 ECU indicate that a connection was obtained
			// ---------------------------------------------------------------------
			localResult = true;
		}
		catch (IOException theException)
		{	
		}	
		// -------------------------------------------------------------------------
		return localResult;
	}
	// ============================================================================= 
	public static void SpeakAPhrase (Context theContext,String thePhrase)
	{
		// -------------------------------------------------------------------------
		// 12/09/2013 ECU created - will try and speak the specified phrase
		// 08/02/2014 ECU now that a text to speech engine has been included
		//                then use it if it is ready
		// 26/03/2016 ECU changed so that only the service is used
		//			  ECU the old code used to check the readiness of the service
		//                and if not ready then would use the following
		//					Intent localIntent = new Intent (theContext,VoiceRecognition.class);
		//					localIntent.putExtra (StaticData.PARAMETER_PHRASE,thePhrase);
		//					localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		//					theContext.startActivity (localIntent);
		//                which did not look correct
		// -------------------------------------------------------------------------
		TextToSpeechService.SpeakAPhrase (thePhrase);
		// -------------------------------------------------------------------------
	}
	/* ----------------------------------------------------------------------------- */
	/**
	 * This method passes each phrase in the array to the text-to-speech
	 * service for processing. 
	 * <p>
	 * The method will return immediately whilst the service will actually
	 * pass the phrases to the selected TTS engine.
	 *
	 * @author Ed Usher
	 * 
	 * @param  context  the calling context
	 * @param  phrases  String array of phrases to be processed
	 * 
	 * @see    TextToSpeechService
	 * 
	 * 
	 */
	public static void SpeakAPhrase (Context theContext,String [] thePhrases)
	{
		// -------------------------------------------------------------------------
		// 20/02/2014 ECU created to be able to speak a sequence of phrases
		//                with a slight delay between each phrase
		// -------------------------------------------------------------------------
		
		for (int thePhrase = 0; thePhrase < thePhrases.length; thePhrase++)
		{
			// ---------------------------------------------------------------------
			// 20/02/2014 ECU process the particular phrase
			// ---------------------------------------------------------------------
			SpeakAPhrase (theContext,thePhrases[thePhrase]);
		}
	}
	/* ----------------------------------------------------------------------------- */
	public static void SpeakAPhrase (Context theContext, String thePhrase,final TextToSpeech theTextToSpeech)
	{	
		// -------------------------------------------------------------------------
		// 20/11/2013 ECU created
		// -------------------------------------------------------------------------
		SpeakAPhrase (theContext,thePhrase,theTextToSpeech,0);
	}
	/* ----------------------------------------------------------------------------- */
	public static void SpeakAPhrase (Context theContext, String thePhrase,final TextToSpeech theTextToSpeech,int theSilence)
	{	
		// -------------------------------------------------------------------------
		// 20/11/2013 ECU speak out the input string
		// -------------------------------------------------------------------------
		theTextToSpeech.speak (thePhrase,TextToSpeech.QUEUE_ADD,null); 
		// -------------------------------------------------------------------------
		// 21/11/2013 ECU check if a period of silence is required
		// -------------------------------------------------------------------------
		if (theSilence > 0)
		{
			theTextToSpeech.playSilence(theSilence, TextToSpeech.QUEUE_ADD, null);
		}
	}
	// =============================================================================
	public static void SpeakAPhraseRemotely (Context theContext,String thePhrase,boolean theAllDevicesFlag)
	{
		// -------------------------------------------------------------------------
		// 28/01/2015 ECU just to speak a phrase on a remote device
		// 31/01/2015 ECU changed the name of the method called
		// 21/03/2015 ECU pass through the message type as an argument
		// 31/07/2016 ECU added theAllDevicesFlag
		// -------------------------------------------------------------------------
	    APIIssues.NetworkOnMainUIThread (android.os.Build.VERSION.SDK_INT);
	    // -------------------------------------------------------------------------
	    sendSocketMessageSendObjectToAllDevices (theContext,
	    										 PublicData.deviceDetails,
	    										 PublicData.socketNumberForData,
	    										 StaticData.SOCKET_MESSAGE_SPEAK_A_PHRASE,
	    										 (Object) thePhrase,
	    										 theAllDevicesFlag);
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
	public static void SpeakAPhraseWithDelays (Context theContext,String thePhrase,boolean theActionFlag)
	{
		// -------------------------------------------------------------------------
		// 26/03/2016 ECU created because the text to speech engine does not seem
		//                to have a long enough pause when it detects a '.'.
		// 03/06/2016 ECU added 'theActionFlag' to indicate whether being called by
		//                the action handler or not
		// -------------------------------------------------------------------------
		String [] localPhrases = thePhrase.split("[.]");
		// -------------------------------------------------------------------------
		// 26/03/2016 ECU check if there are any sub-phrases and then loop through
		//                them
		// -------------------------------------------------------------------------
		if (localPhrases.length > 0)
		{
			for (int phrase = 0; phrase < localPhrases.length; phrase++)
			{
				// -----------------------------------------------------------------
				// 26/03/2016 ECU first check if the phrase has a length - if so
				//                then process it
				// -----------------------------------------------------------------
				if (localPhrases [phrase].length() > 0)
				{
					// -------------------------------------------------------------
					// 26/03/2016 ECU now speak the local phrase
					// 03/06/2015 ECU include theActionFlag
					// -------------------------------------------------------------
					TextToSpeechService.SpeakAPhrase (localPhrases [phrase],theActionFlag);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 26/03/2016 ECU if there are simultaneous full stops then
					//                just speak a 'nothing' phrase to get another
					//                delay
					// 03/06/2015 ECU include theActionFlag
					// -------------------------------------------------------------
					TextToSpeechService.SpeakAPhrase (" ",theActionFlag);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void SpeakingClock ()
	{
		// -------------------------------------------------------------------------
		// 08/02/2014 ECU added to provide, optionally, a speaking clock
		// 09/02/2014 ECU check if in the range in which the clock is active
		// 23/02/2014 ECU change so that no longer check if in the active
		//                period - removed because using AlarmManager now
		// -------------------------------------------------------------------------
		Calendar currentCalendar = Calendar.getInstance();
		// -------------------------------------------------------------------------
		// 13/02/2014 ECU change to use SpeakingClockConvert
		// 19/02/2017 ECU added the string resource as an argument
		//            ECU change to make the initial message separate from convert
		//                method
		// -------------------------------------------------------------------------
		TextToSpeechService.SpeakAPhrase (MainActivity.activity.getString (R.string.speaking_clock_current_time) + 
											SpeakingClockConvert (currentCalendar.get (Calendar.HOUR_OF_DAY), 
																  currentCalendar.get (Calendar.MINUTE)));
		// -------------------------------------------------------------------------
		// 01/02/2017 ECU decide if the time is to be displayed as text
		// -------------------------------------------------------------------------
		if (PublicData.storedData.speakingClock.showText)
		{
			// ---------------------------------------------------------------------
			// 01/02/2017 ECU display the time as a Toast message
			// ---------------------------------------------------------------------
			MessageHandler.displayText (PublicData.dateFormatterShort.format (currentCalendar.getTime()));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	static String SpeakingClockConvert (int theHour,int theMinute)
	{
		// -------------------------------------------------------------------------
		// 13/02/2014 ECU try to convert the supplied time into a more
		//                'human' output that will be converted to speech
		//            ECU when speaking then use 12 hour clock
		//            ECU used to have a comma to build in a display but
		//                on some TTS engines the ',' is actually read out.
		//                Ivona gives a delay but Pico reads out the comma
		// 19/02/2017 ECU changed the initialisation of clockString from
		//                "The current time is " - because the method is
		//                now called from a couple of places that require a different
		//                initial message
		// -------------------------------------------------------------------------
		String clockString 	= "";
		int	   hour			= theHour;
		int    nextHour;							// 22/02/2014 ECU added
		// -------------------------------------------------------------------------
		// 13/02/2014 ECU adjust to a 12 hour clock for speaking
		// -------------------------------------------------------------------------
		if (hour > 12)
		{
			hour -= 12;
		}
		if (hour == 0)
			hour = 12;
		
		// -------------------------------------------------------------------------
		// 22/02/2014 ECU sort out the next hour
		// -------------------------------------------------------------------------
		
		nextHour = hour + 1;
		
		if (nextHour == 13)
			nextHour = 1;
		else
		if (theHour == 23)
			nextHour = 12;			// midnight
		
		switch (theMinute)
		{
			case 0:
				// -----------------------------------------------------------------
				// 11/03/2014 ECU handle midnight properly
				// -----------------------------------------------------------------
				clockString += (theHour == 0) ? "midnight" : hour + " o'clock";
				// -----------------------------------------------------------------
				break;
			
			case 1:
				clockString += "1 minute past " + hour;
				break;
			
			case 15:
				clockString += "quarter past " + hour;
				break;
			
			case 20:
				 clockString += "20 past " + hour;
				 break;
				 
			case 30:
				clockString += "half past " + hour;
				break;
				
			case 45:
				clockString += "quarter to " + nextHour;
				break;

			case 40:
			case 50:
			case 55:
				clockString += (60 - theMinute) + " to " + nextHour;
				break;	
			
			case 59:
				clockString += "1 minute to " + nextHour;
				break;
					
			default:
				if (theMinute < 45)
				{
					clockString += theMinute + " minutes past " + hour;
				}
				else
				{
					clockString += (60 - theMinute) + " minutes to " + nextHour;
				}
				
				break;
		}
		// -------------------------------------------------------------------------
		// 13/02/2014 ECU now return the converted string
		// -------------------------------------------------------------------------
		return clockString;
	}
	/* ============================================================================= */
	public static void SpeechSilence (int theDuration)
	{
		// -------------------------------------------------------------------------
		// 20/02/2014 ECU created to request that the text to speech
		//                service insert a period of silence in the
		//                the text phrases that it is processing
		// -------------------------------------------------------------------------
		
		TextToSpeechService.Silence(theDuration);
	}
	/* ============================================================================= */
	public static void startVoiceRecognitionActivity (final Activity theActivity,final TextToSpeech theTextToSpeech)
	{
		// -------------------------------------------------------------------------
		// 21/11/2013 ECU created 
		// -------------------------------------------------------------------------
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				try 
				{
					synchronized(this)
					{
						while (theTextToSpeech.isSpeaking())
						{
							// -----------------------------------------------------
							// 21/11/2013 ECU wait until anything being spoken
							//                finishes
							// -----------------------------------------------------
							sleep(200);
		                }
		                // ---------------------------------------------------------		
						// 21/11/2013 ECU start the recognition software
		                // ---------------------------------------------------------	
						Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
						intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
										RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		               	intent.putExtra (RecognizerIntent.EXTRA_PROMPT, "Diboson Voice Recognition ...");
		               	// ---------------------------------------------------------	
		               	// 21/12/2013 ECU added the next flag to try to avoid getting
		               	//				  Americanisms like 'favorites' instead of 
		               	//                'favourites'
		                // 01/09/2015 ECU changed to use StaticData
		               	// ---------------------------------------------------------	
		               	intent.putExtra (RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE,true);
		               	theActivity.startActivityForResult (intent,StaticData.VOICE_REQUEST_CODE);
		                // ---------------------------------------------------------	
					}
				}
				catch(InterruptedException theException)
				{ 
		            	                 
				}       
			}
		};
		// -------------------------------------------------------------------------
		// 21/11/2013 ECU start up the defined thread
		// -------------------------------------------------------------------------
		thread.start();        		
	}
	// =============================================================================
	public static void stopAllAudio ()
	{
		// -------------------------------------------------------------------------
		// 24/10/2016 ECU stop all current audio
		//					1) outstanding actions
		//					2) currently playing media
		//					3) flash out any spoken text
		// -------------------------------------------------------------------------
		PublicData.messageHandler.sendEmptyMessage (StaticData.MESSAGE_LIST_FINISHED);
		StopMediaPlayer (PublicData.mediaPlayer);
		TextToSpeechService.Flush ();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void StopMediaPlayer (MediaPlayer theMediaPlayer)
	{
		// -------------------------------------------------------------------------
		// 21/03/2014 ECU created - this method checks if the media player object
		//                exists and if it does and is currently playing something
		//                then it will be stopped
		// -------------------------------------------------------------------------
		if (theMediaPlayer != null && theMediaPlayer.isPlaying())
		{
			// ---------------------------------------------------------------------
			// 21/03/2014 ECU stop the media player
			// ---------------------------------------------------------------------
			theMediaPlayer.stop();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void StrictMode ()
	{
		// -------------------------------------------------------------------------
		// 02/04/2014 ECU created to set the strictmode policies for debugging
		// -------------------------------------------------------------------------
		// 19/03/2017 ECU Note - The strict mode is a developer tool (see documentation_notes
		//                       in 'raw') which tries to alert the developer to actions
		//                       being taken which are not 'best practice', e.g.
		//                       accessing the disk or network from the UI which
		//                       can result in an app Not Responding (ANR) warning.
		//
		//                       However in this app some taks, which should run
		//                       'non-UI' do in fact run on the UI because the user
		//                       cannot continue until they have completed.
		// -------------------------------------------------------------------------
		// 19/03/2017 ECU Note - this method is only called up from MainActivity
		//                       if the STRICT_MODE in StaticData is set to true
		// -------------------------------------------------------------------------
		// 19/03/2017 ECU Note - Sets the policy for what actions on the current thread 
		//                       should be detected, as well as the penalty if such actions
		//						 occur. 
		//						 Internally this sets a thread-local variable which is 
		//                       propagated across cross-process IPC calls, meaning you 
		//                       can catch violations when a system service or another 
		//                       process accesses the disk or network on your behalf.
		// -------------------------------------------------------------------------
		android.os.StrictMode.setThreadPolicy (new android.os.StrictMode.ThreadPolicy.Builder()
                								.detectDiskReads ()
                								.detectDiskWrites ()
                								.detectNetwork ()  
                								.penaltyLog ()
                								.build ());
		// -------------------------------------------------------------------------
		// 19/03/2017 ECU Note - Sets the policy for what actions in the VM process 
		//                       (on any thread) should be detected, as well as the 
		//                       penalty if such actions occur.
		// -------------------------------------------------------------------------
        android.os.StrictMode.setVmPolicy (new android.os.StrictMode.VmPolicy.Builder()
                								.detectLeakedSqlLiteObjects ()
                								.detectLeakedClosableObjects ()
                								.penaltyLog ()
                								.penaltyDeath ()
                								.build ());
        // -------------------------------------------------------------------------
	}
	// =============================================================================
	
	/* ============================================================================= */
	public static void SynchroniseFiles (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 02/02/2014 ECU called to synchronise files across the devices
		// 03/02/2014 ECU so as not to swamp the system just do one file
		//                each scan
		// 01/04/2014 ECU only continue if there is a network - either
		//                WiFi or mobile
		// -------------------------------------------------------------------------
		if (Utilities.checkForNetwork (theContext))
		{
			// ---------------------------------------------------------------------
			// 01/04/2014 ECU connected to a network so continue
			// ---------------------------------------------------------------------
			if (PublicData.filesToSynchronise.size() > 0)
			{
				for (int theIndex = 0; theIndex < PublicData.filesToSynchronise.size(); theIndex++)
				{
					FileToSynchronise fileToSynchronise = PublicData.filesToSynchronise.get (theIndex);
					// -------------------------------------------------------------
					// 02/02/2014 ECU check if a file has changed
					// -------------------------------------------------------------
					File localFile = new File (fileToSynchronise.fileName);
					// -------------------------------------------------------------
					// 25/10/2016 ECU check if the synchronise is being 'forced' on
					//                the file
					// -------------------------------------------------------------
					if ((localFile.lastModified() > fileToSynchronise.lastModified) || 
							fileToSynchronise.forceSynchronise)
					{	
						// ---------------------------------------------------------
						// 02/02/2014 ECU the file seems to have changed so update 
						//                the stored record
						// ---------------------------------------------------------
						fileToSynchronise.lastModified = localFile.lastModified();
						// ---------------------------------------------------------
						// 25/10/2016 ECU switch off the 'force' flag whether it was
						//                true or false
						// ---------------------------------------------------------
						fileToSynchronise.forceSynchronise = false;
						// ---------------------------------------------------------
						// 02/02/2014 ECU change the entry in the list
						// ---------------------------------------------------------
						PublicData.filesToSynchronise.set (theIndex,fileToSynchronise);
						// -------------------------------------------------------------
						// 20/03/2014 ECU enter some details in project log
						// 16/11/2016 ECU changed to use the new method
						// -------------------------------------------------------------
						LogToProjectFile ("SynchroniseFiles", 
							Utilities.getRelativeFileName (PublicData.filesToSynchronise.get(theIndex).fileName));
						// ---------------------------------------------------------
						// 02/02/2014 ECU now send the details out to other devices
						// ---------------------------------------------------------
						Utilities.SendFileDetails (theContext, PublicData.filesToSynchronise.get (theIndex).fileName);
						// ---------------------------------------------------------
						// 03/02/2014 ECU only want to do one per cycle 
						//            ECU after some thought took out the break from above
						// 25/10/2016 ECU put the break back in to try and reduce the
						//                network traffic
						// ---------------------------------------------------------
						break;
						// ---------------------------------------------------------
					}
				}
			}
		}
	}
	// =============================================================================
	public static void SynchroniseFilesInDirectory (Context theContext,String theDirectoryName)
	{
		// -------------------------------------------------------------------------
		// 25/10/2016 ECU synchronise all of the files in the specified directory
		//                - sub folders are not processed. Also assumed that the
		//                specified file is a directory but will check anyway
		// -------------------------------------------------------------------------
		File localDirectory = new File (theDirectoryName);
		// -------------------------------------------------------------------------
		// 25/10/2016 ECU check that the file is a directory
		// -------------------------------------------------------------------------
		if (localDirectory.isDirectory ())
		{
			// ---------------------------------------------------------------------
			// 25/10/2016 ECU have confirmed that the specified file is a directory
			// ---------------------------------------------------------------------
			// 25/10/2016 ECU get list of files in this directory
			// ---------------------------------------------------------------------
			File [] localFiles = localDirectory.listFiles ();
			String  localFileName;
			// ---------------------------------------------------------------------
			// 25/10/2016 ECU now loop through the list
			// ---------------------------------------------------------------------
			for (int index = 0; index < localFiles.length; index++)
			{
				// -----------------------------------------------------------------
				// 25/10/2016 ECU ensure that a file is being handled
				// -----------------------------------------------------------------
				if (localFiles [index].isFile())
				{
					localFileName = localFiles [index].getAbsolutePath();
					// -------------------------------------------------------------
					// 26/10/2016 ECU add the file into the list if it does not
					//                already exist in it
					// -------------------------------------------------------------
					FileToSynchronise.Add (localFileName);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	public static void TimeForMedication (Context theContext,
										  int theHour,
										  int theMinute,
										  int theMedicationDetails,
										  int theDailyDoseTime,
										  int theDose)
	{
		// -------------------------------------------------------------------------
		// 22/06/2013 ECU created - called when a dosage of medication is required
		// 23/06/2013 ECU put in debug mode
		// 10/12/2013 ECU change to use the custom popToast
		// 16/01/2014 ECU changed to reflect that dose_times changed to List<>
		// 17/01/2014 ECU removed the debug print
		// 04/03/2014 ECU made public
		// -------------------------------------------------------------------------
		
		// -------------------------------------------------------------------------
		// 23/06/2013 ECU added
		// 15/01/2014 ECU changed to use MainActivity.PARAMETER_
		// -------------------------------------------------------------------------
		Intent medicationIntent = new Intent (theContext,MedicationActivity.class);
		// -------------------------------------------------------------------------
		// 15/01/2014 ECU set up the variables to pass to the activity
		// -------------------------------------------------------------------------
		medicationIntent.putExtra (StaticData.PARAMETER_HOUR,theHour);
		medicationIntent.putExtra (StaticData.PARAMETER_MINUTE,theMinute);
		medicationIntent.putExtra (StaticData.PARAMETER_MEDICATION,theMedicationDetails);
		medicationIntent.putExtra (StaticData.PARAMETER_DOSE_TIME,theDailyDoseTime);
		medicationIntent.putExtra (StaticData.PARAMETER_DOSE,theDose);
		
		medicationIntent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		theContext.startActivity (medicationIntent);
					
	}
	// -----------------------------------------------------------------------------
	public static void TimeForMedication (Context theContext,
			  							  int theHour,
			  							  int theMinute,
			  							  int theMedicationDetails,
			  							  DoseTime theDoseTime)
	{
		// -------------------------------------------------------------------------
		// 09/03/2016 ECU added
		// -------------------------------------------------------------------------
		Intent medicationIntent = new Intent (theContext,MedicationActivity.class);
		// -------------------------------------------------------------------------
		// 09/03/2016 ECU set up the variables to pass to the activity
		// -------------------------------------------------------------------------
		medicationIntent.putExtra (StaticData.PARAMETER_HOUR,theHour);
		medicationIntent.putExtra (StaticData.PARAMETER_MINUTE,theMinute);
		medicationIntent.putExtra (StaticData.PARAMETER_MEDICATION,theMedicationDetails);
		medicationIntent.putExtra (StaticData.PARAMETER_OBJECT,theDoseTime);
		// -------------------------------------------------------------------------
		medicationIntent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		theContext.startActivity (medicationIntent);			
	}
	// =============================================================================
	public static void traceMessage (String theTag,String theMessage,boolean theLogCatFlag)
	{
		// -------------------------------------------------------------------------
		// 03/10/2016 ECU created to provide trace messages when trying to find 
		//                problems
		// -------------------------------------------------------------------------
		LogToProjectFile (theTag,theMessage,true);
		// -------------------------------------------------------------------------
		// 03/10/2016 ECU now decide if the message is to be entered into LogCat
		// -------------------------------------------------------------------------
		if (theLogCatFlag)
		{
			// ---------------------------------------------------------------------
			Log.i (theTag,theMessage);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static Spannable twoLineButtonLegend (Context theContext,String theTopLine,String theBottomLine)
	 {
		// -------------------------------------------------------------------------
		// 16/06/2016 ECU created to generate the spannable that enables two
		//				  lines of text (with different sizes) to be displayed. This
		//                method returns the spannable which can be used to 'setText'
		//                on the button
		// -------------------------------------------------------------------------
		// 16/06/2016 ECU create the spannable from the input arguments
		// -------------------------------------------------------------------------
		Spannable spannableLegend = new SpannableString (theTopLine + StaticData.NEWLINE + theBottomLine);
		// -------------------------------------------------------------------------
		// 15/06/2016 ECU set up the top line - want it to be the font size already
		//                set for the button
		// -------------------------------------------------------------------------- 
		spannableLegend.setSpan (new RelativeSizeSpan (1.0f),
								 0,theTopLine.length(),
								 Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// -------------------------------------------------------------------------
		// 15/06/2016 ECU set up the bottom line to be 60% of the size already set
		//				  for this button
		// --------------------------------------------------------------------------
		spannableLegend.setSpan (new RelativeSizeSpan (0.6f),
				 				 theTopLine.length(),(theTopLine.length()+theBottomLine.length()+1),
				 				 Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// -------------------------------------------------------------------------
		// 16/06/2016 ECU return the spannable which will be used for the button's 
		//                text
		// -------------------------------------------------------------------------
		return spannableLegend;	 
		// -------------------------------------------------------------------------
	 }
	// ============================================================================= 
	public static void UserInterface (Activity theActivity,View theView,boolean theHideShowFlag)
	{
		SystemUiHider systemUiHider = SystemUiHider.getInstance(theActivity,
								theView,SystemUiHider.FLAG_HIDE_NAVIGATION);
		systemUiHider.setup();
		// -------------------------------------------------------------------------
		// 24/03/2014 ECU decide what to do dependent on the flag
		//                  theHideShowFlag = true ..... show the UI
		//                                  = false .... hide the UI
		// -----------------------------------------------------------------
		if (theHideShowFlag)
			systemUiHider.show();
		else
			systemUiHider.hide();
		// -----------------------------------------------------------------
	}
	// =============================================================================
	public static boolean validateIPAddress (String theIPAddress)
	{
		// -------------------------------------------------------------------------
		// 10/11/2016 ECU create to validate the specified IP address
		// -------------------------------------------------------------------------
		return Pattern.compile ("(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))").matcher(theIPAddress).matches();
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean validateMACAddress (String theMACAddress)
	{
		// -------------------------------------------------------------------------
		// 12/05/2015 ECU created to validate that the specified MAC address has
		//                the correct format
		// -------------------------------------------------------------------------
		return theMACAddress.matches ("([0-9A-F]{2}[:-]){5}([0-9A-F]{2})");
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static List <Integer> validAddresses (int theNetworkMask,boolean theBroadcastFlag)
	{
		// -------------------------------------------------------------------------
		// 10/11/2016 ECU created to work with IP address and network network mask.
		//                In reality working with 'unsigned bytes' but believe that
		//                an 'int' is allocated so work with them.
		//
		//                In essence returns a list of the numbers that are valid
		//                according to the network mask - if the mask is 0xFF then
		//                no numbers are valid, if 0x00 then all numbers are valid
		//
		//                Omit '0' and '255' as these are usually used for
		//                broadcast addresses - unless theBroadcastFlag is true in
		//                which case '0' is included
		// -------------------------------------------------------------------------
		List<Integer> localNumbers = new ArrayList<Integer> ();
		// -------------------------------------------------------------------------
		// 10/11/2016 ECU loop for all numbers that can be stored in an unsigned
		//                byte - 1 to 254 (see note above)
		// -------------------------------------------------------------------------
		for (int index = (theBroadcastFlag ? 0 : 1); index < 255; index++)
		{
			// ---------------------------------------------------------------------
			// 10/11/2016 ECU check if this is a valid number - according to the
			//                mask
			// ---------------------------------------------------------------------
			if ((index & theNetworkMask) == 0)
			{
				// -----------------------------------------------------------------
				// 10/11/2016 ECU this number is allowed according to the mask
				// -----------------------------------------------------------------
				localNumbers.add (index);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 10/11/2016 ECU return the list of numbers
		// -------------------------------------------------------------------------
		return localNumbers;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String Version (Context theContext)
	{
		// -------------------------------------------------------------------
		// 03/06/2014 ECU created to return, as a string, the version name of
		//                this app - as defined in the manifest
		//            ECU include a heading label
		// -------------------------------------------------------------------
		try 
		{
			PackageInfo packageInfo = theContext.getPackageManager().getPackageInfo(theContext.getPackageName(), 0);
			return (theContext.getString (R.string.version_label) + packageInfo.versionName);
		} 
		catch (NameNotFoundException theException) 
		{
			return "";
		}
	}
	/* ============================================================================= */
	public static String workOutAge (Context theContext,String theBirthday,String theFormattedString)
	{
		// -------------------------------------------------------------------------
		// 18/06/2013 ECU theBirthday is fed through as <dd>/<mm>/<year>
		// -------------------------------------------------------------------------
		String [] theWords = theBirthday.split("/");
		
		int theDay   = Integer.parseInt(theWords[0]);
		int theMonth = Integer.parseInt(theWords[1]);
		int theYear  = Integer.parseInt(theWords[2]);
		// -------------------------------------------------------------------------
		// 18/06/2013 ECU theWords [3] should contain the actual speakable birthday
		// 22/04/2015 ECU changed to use the dateAsText method
		// -------------------------------------------------------------------------
		return String.format(theFormattedString,dateAsText (theContext,theDay,theMonth,theYear),workOutAge (theContext,theDay,theMonth,theYear));
	}
	/* ============================================================================= */
	public static int workOutAge (Context theContext,int theDay,int theMonth,int theYear)
	{
		Calendar theCurrentDate = Calendar.getInstance ();
		
		int theCurrentMonth = theCurrentDate.get(Calendar.MONTH);             // January = 0, ......
		int theCurrentYear 	= theCurrentDate.get(Calendar.YEAR);
		int theCurrentDay   = theCurrentDate.get(Calendar.DAY_OF_MONTH);	  // first day of month is 1
		// -------------------------------------------------------------------------
		// 18/06/2013 ECU adjust input month so that January = 0, .....
		// -------------------------------------------------------------------------
		theMonth--;
		
		int theAge = theCurrentYear - theYear;

		if (theMonth > theCurrentMonth) 
		{
			theAge--;
		}
		else
		if (theMonth == theCurrentMonth) 
		{
			if (theDay > theCurrentDay) 
		    {
				theAge--;
		    }
		}
		// -------------------------------------------------------------------------
		// 18/06/2013 ECU return the age in years
		// -------------------------------------------------------------------------
		return theAge;
	}
	/* ============================================================================= */
	public static void writeAFile (String theFileName,byte [] theBuffer)
	{
		// -------------------------------------------------------------------------
		// 03/08/2013 ECU added - write a buffer to a file which should be
		//				  created if it does not exist
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 03/08/2013 ECU open to the specified file in 'read/write' mode
			// ---------------------------------------------------------------------
			RandomAccessFile theFile = new RandomAccessFile(theFileName, "rw");
			
			theFile.write (theBuffer);	
			
			theFile.close();
           
        }
		catch (IOException theException)
		{	
		}
	}
	/* ============================================================================= */
	public static void writetMessageType (Context theContext,String theIPAddress,
			int thePort,int theMessageType,boolean theAcknowledgeFlag)
	{	
		try
		{	
			
			InetAddress serverAddress = InetAddress.getByName(theIPAddress);    
			
			Socket theSocket = new Socket (serverAddress,thePort);
			// ---------------------------------------------------------------------
			// 02/08/2013 ECU set up the input streams for normal and the object
			// 19/08/2013 ECU changed to used buffered output
			// ---------------------------------------------------------------------
			BufferedOutputStream output = new BufferedOutputStream (theSocket.getOutputStream());
			// ---------------------------------------------------------------------
			// 11/08/2013 ECU change so that transmission of message type is via
			//                the method rather than output.write and output.flush
			// 31/01/2015 ECU changed the method name
			// ---------------------------------------------------------------------
			Utilities.socketSendMessageType (theMessageType, output);
			// ---------------------------------------------------------------------
			// 02/08/2013 ECU check if need to acknowledge the message
			// ---------------------------------------------------------------------
			if (theAcknowledgeFlag)
			{
				// -----------------------------------------------------------------
				// 02/03/2013 ECU just want to check for acknowledgement
				// -----------------------------------------------------------------
				InputStream input = theSocket.getInputStream();
				input.read();
				input.close();
			}
			
			theSocket.close();
		}
		catch (IOException theException)
		{	
		} 
	}
	/* ============================================================================= */
	public static boolean writeObjectToDisk (String theFileName,Object theObject)
	{
		// -------------------------------------------------------------------------
		// 23/05/2013 ECU this method will write the supplied object to the
		//                specified file
		// 22/02/2014 ECU changed to boolean so that calling code can process the outcome
		// 17/10/2015 ECU changed to use buffering and issue a flush before closing
		//                the stream
		// -------------------------------------------------------------------------
		try
		{
			ObjectOutputStream outputStream = new ObjectOutputStream (new BufferedOutputStream(new FileOutputStream (theFileName)));
			outputStream.writeObject (theObject);
			// ---------------------------------------------------------------------
			// 17/10/2015 ECU flush out any pending data before closing the stream
			// ---------------------------------------------------------------------
			outputStream.flush();
			// ---------------------------------------------------------------------
			// 23/05/2013 ECU finally close the output stream
			// ---------------------------------------------------------------------
			outputStream.close ();
			// ---------------------------------------------------------------------
			// 22/02/2014 ECU indicate that everything was OK
			// ---------------------------------------------------------------------
			return true;
		}
		catch (IOException theException)
		{
			// ---------------------------------------------------------------------
			// 18/06/2013 ECU handle any exceptions that occur
			// 22/02/2014 ECU indicate that an error occurred
			// 17/10/2015 ECU add the logging of the exception to try and solve an issue
			//                where am getting EOF errors when reading the file
			//                later
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG, "Exception on writing file " + theFileName + " " + theException);
			// ---------------------------------------------------------------------
			return false;		
		}
	}
	/* ============================================================================= */
}

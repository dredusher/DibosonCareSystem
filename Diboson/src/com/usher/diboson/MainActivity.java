package com.usher.diboson;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.usher.diboson.utilities.UncaughtExceptionUtilities;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * This class constitutes the main activity of the Diboson Care System.
 * <p>
 * It is just used to display some start up information before
 * launching the user interface which is controlled by the GridActivity.
 * <p>
 * 	1.	Check whether the activity is being started afresh or restarted by the Android Operating System - remember this fact
 * <p>
 *	2.	Check whether the intent contains any parameters. If yes then retrieve and process them.
 * <p>
 *	3.	Display an initial information screen by activating the StartUpActivity
 * <p>
 *	4.	Check whether the required file system is mounted. If not then display a message and terminate immediately, otherwise carry on.
 * <p>
 *	5.	Initialise various tasks by calling the InitialiseTasks method within this class.
 * <p>
 *	6.	Check whether this is a valid installation - if not then display a message and terminate immediately
 * <p>
 *	7.	Check if any errors occurred during the initialisation of tasks. If not then carry on. If yes then terminate immediately and set an alarm to restart the app. (Note - this is normally only an issue when the app is restarted shortly after being stopped. Sometimes services have not had time to fully stop and this can generate a problem).
 * <p>
 *	8.	Start up the user interface.
 * <p>
 *
 * @author Ed Usher
 *
 * @see    GridActivity
 *
 *
 */

 // ================================================================================
 // ================================================================================
 // I M P O R T A N T - Please read this as it relates to the way the app operates
 // =================   - it should be read in conjunction with raw/documentation_notes
 //                     and the project documentation (which is not kept up to date).
 //
 // There are two 'exceptions' that need to be taken into account.
 //
 //      1) a second 'instance' of the app may be created by the Android system
 //      2) the app may be 'forcibly stopped' without it being able to perform the
 //         normal tasks that it would perform on being 'finished'.
 //
 // Exception 1 - More than one Instance
 // ====================================
 // Use is made of a variable stored in the 'shared preferences' to determine whether
 // the app is a 'new instance' of an 'already running instance' of this app. That
 // variable has a key of 'StaticData.SHARED_PREFERENCES_START' and will normally
 // contain 'StaticData.SHARED_PREFERENCES_DEFAULT' when the first instance of the
 // app is started - at which point the app will change the stored value to the
 // 'current time'. When the app is 'closed' then 'StaticData.SHARED_PREFERENCES_DEFAULT'
 // is written to the variable. If the app is started and it finds that
 // 'StaticData.SHARED_PREFERENCES_START' has a value other than
 // 'StaticData.SHARED_PREFERENCES_DEFAULT' then this is a 'new instance' of an
 // 'already running instance'.
 //
 // The value read using the key 'StaticData.SHARED_PREFERENCES_START' is stored
 // in 'PublicData.keyValue' so that it can be checked elsewhere in the app.
 //
 // Exception 2 - 'forcibly stopped'
 // ================================
 // If this app is 'forcibly stopped' then some of the tasks that will normally be
 // taken on closure.
 //
 // Use is made of a 'lock file', which is defined by 'R.string.lock_file', which will
 // contain 'recovery information' should the app be 'forcibly stopped'.
 //
 // When the app is start it will check to see if the 'lock file' exists - if it
 // does not then this app was terminated normally the last it ran. If the file is
 // found to exist then the app must have been 'forcibly stopped' and 'recovery
 // action' needs to be taken.
 // ================================================================================
 // ================================================================================
 // 21/04/2020 ECU change form 'extends Activity' to 'extends DibosonActivity'
 // ================================================================================


public class MainActivity extends DibosonActivity
{
	// =============================================================================
	// see the Notes file for useful information
	// =============================================================================
	// Revision History
	// ================
	// 28/04/2013 ECU created
	// 16/06/2013 ECU voice recognition bits added
	// 03/01/2014 ECU did a major rewrite to take out all of the tracking
	//                code into a separate activity (LocationActivity)
	// 10/01/2014 ECU try and use PARAMETERS_ for passing data between
	//				  activities
	// 15/02/2014 ECU put in phone status monitoring
	// 17/02/2014 ECU move the ambient light monitoring into the GridActivity
	// 29/09/2014 ECU proper use of manifest for revisions
	// 01/10/2014 ECU start doing major changes
	// 20/01/2015 ECU following a major tidy up of the Music Player have
	// 				  'ImageView  albumView = null' which was superfluous and
	//                causing problems
	// 21/01/2015 ECU following issues yesterday remove slideShowView,
	//                torchView, compassView
	// 08/02/2015 ECU removed requiredAlarmAction and requiredAlarmID which
	//                were used for passing information between the AlarmService
	//                (as of 04/03/2017 ECU the name is now AlarmReceiver)
	//                and AlarmActions - instead the data is passed using
	//                parameters in the intent extras which is the correct way.
	// 10/03/2015 ECU remove all use of the menu as the options have not been
	//                used for a long time
	//            ECU 'developmentMode' moved into 'storedData'
	// 			  ECU 'debugMode' moved into 'storedData'
	// 21/03/2015 ECU removed the 'socketMessageType' which was passing the type
	//                of message to be transmitted on a socket - felt that it
	//                could be corrupted as more than one app/service could
	//                access it at the same. There was no 'locking' mechanism on
	//                the variable.
	// 22/03/2015 ECU changed Devices from [] to ArrayList
	// 03/05/2015 ECU removed cloneMode as checking now uses 'status'
	// 11/05/2015 ECU added currentRemoteController and remove currentTelevision
	// 31/08/2015 ECU moved most of the 'final static' variables into
	//                the StaticData class
	// 03/09/2015 ECU finish up putting all final static variables into
	//                StaticData
	// 04/09/2015 ECU Place all public variables into PublicData - they had been
	//                in MainActivity
	// 02/04/2016 ECU the 'startedByAlarm' variables moved from local to PublicData
	// 09/11/2016 ECU put in the check of runtime permissions which really only
	//                became an issue at API >= 23
	// 02/03/2017 ECU put in the start up 'shared preference' handling
	// 05/04/2017 ECU took out implementation of OnGestureListener and associated
	//                methods and the variable 'gestureScanner'
	// 06/12/2019 ECU added the declaration of the 'emailHandler'
	// 07/01/2020 ECU changed the way the scaling factors are set up for use by
	//                the Utilities.threeLineButtonLegend
	// 06/05/2020 ECU the final 'acknowledgement text', that is displayed before the
	//                app is destroyed was changed so that if the file 'Acknowledgement.txt'
	//                file (R.string.acknowledgement_file) is not found in the project
	//                folder then 'raw/acknowledgement_string' is used.
	// 12/08/2020 ECU changed the way that services are started and stopped using
	//                methods in ServiceControl and data defined in StaticDate
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	/* ============================================================================= */
	private static final String TAG				 = "MainActivity";
	/* ============================================================================= */

	// ============================================================================
	// STATIC VARIABLES
	// ================
	//	All static variables are capitalised and held in class StaticData
	// =============================================================================

	// =============================================================================
	// PUBLIC VARIABLES
	// ================
	//  All public variables are held in class PublicData
	// =============================================================================

	// =============================================================================
	// LOCAL VARIABLES
	// ===============
	//  Only variables that are used within this class are declared here
	//
	//  EXCEPTION - activity is used throughout the system but it really does
	//  =========   make sense to declare it here rather than in PublicData
	// -----------------------------------------------------------------------------
	public 	static 	Activity		activity 			= null;			// 11/09/2013 ECU added
																		// 21/09/2013 ECU preset to null
				  	TextView		careDetails 		= null;			// 03/01/2014 ECU added
				  														// 11/04/2015 ECU initialised to null
				  	Button	      	continueButton 		= null;			// 03/01/2014 ECU added
				  														// 11/04/2015 ECU initialise to 'null'
				  	boolean         exitProgram 		= false;		// 03/01/2014 ECU added
				  	boolean			initialised			= false;		// 14/10/2020 ECU added to indicate if
				  														//                the initialisation worked
				  	MonitorPhoneStatus
						          	monitorPhoneStatus;	   				// 15/02/2014 ECU added - listener for
				                                               			//                phone state changes
		   	static	Intent			restartIntent;						// 08/08/2020 ECU added
			static	boolean     	restartThisApp		= false;		// 08/08/2020 ECU added - to indicate if
				  														//                this app is to be restarted
    // =============================================================================

	// =============================================================================
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		// -------------------------------------------------------------------------
		// 09/10/2015 ECU want to check if the activity is being newly created or
		//                just recreated having been destroyed by Android
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been newly created
			// ---------------------------------------------------------------------
			PublicData.startedManually = true;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been recreated after being destroyed
			//                by Android
			// ---------------------------------------------------------------------
			PublicData.startedManually = false;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 06/09/2020 ECU try and catch any 'unhandled' exceptions
		// -------------------------------------------------------------------------
		UncaughtExceptionUtilities.Initialise (this);
		// -------------------------------------------------------------------------
		// 28/11/2016 ECU added the call to the method to set full screen
		// -------------------------------------------------------------------------
		Utilities.SetUpActivity (this,true);
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
		setContentView (R.layout.activity_main);
		// -------------------------------------------------------------------------
		// 02/03/2017 ECU check if there is a currently set shared preference 'start
		//                up' key
		// 08/03/2017 ECU changed to use PublicData rather than a local value
		// -------------------------------------------------------------------------
		PublicData.keyValue = SharedPreferencesHandler (this,StaticData.SHARED_PREFERENCES_START);
		// -------------------------------------------------------------------------
		// 02/03/2017 ECU check if another copy of the app is running or not
		// 31/03/2020 ECU changed to use the method
		// -------------------------------------------------------------------------
		if (Utilities.OneInstanceCheck ())
		{
			// ---------------------------------------------------------------------
			// 02/03/2017 ECU nothing else is running so set the value as the
			//                current time
			// ---------------------------------------------------------------------
			this.SharedPreferencesHandler (this,StaticData.SHARED_PREFERENCES_START,Utilities.getAdjustedTime (false));
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/03/2017 ECU it would appear that an instance of this app is already
			//                running
			// ---------------------------------------------------------------------
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 09/11/2016 ECU before going any further check that the runtime permissions
		//                allow this app to run
		// -------------------------------------------------------------------------
		if (RuntimePermissions.check ((Activity) this))
		{
			// ---------------------------------------------------------------------
			// 09/11/2016 ECU the permissions are OK so continue
			// ---------------------------------------------------------------------
			// ---------------------------------------------------------------------
			// 04/03/2016 ECU check if any incoming parameters
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();

			if (extras != null)
			{
				// -----------------------------------------------------------------
				// 04/03/2016 ECU if restarted because an alarm was received when the
				//                app was not running - this caused an error and to try
				//                and get everything to restart cleanly then get the
				//                app to destroy itself and then restart
				// 05/03/2017 ECU not sure that the following is needed any more but
				//                leave until a bit more testing done
				//            ECU the ALARM_TYPE was being used by AlarmActions which
				//                is called by AlarmService - yesterday this was
				//                all changed so decided to remove the code
				// -----------------------------------------------------------------
				// 06/03/2016 ECU now check if this is the restart cause by the
				//                above
				// 05/03/2017 ECU Note - check whether the app has been restarted
				//                       by a 'receiver' which received a message
				//                       when the app was not running
				// 13/05/2020 ECU handle the source of the alarm
				// -----------------------------------------------------------------
				PublicData.startedByAlarm 		= extras.getBoolean (StaticData.PARAMETER_ALARM_START);
				PublicData.startedByAlarmSource = extras.getString (StaticData.PARAMETER_ALARM_SOURCE);
				// -----------------------------------------------------------------
				// 13/05/2020 ECU tidy up the source message for later use
				// -----------------------------------------------------------------
				if (PublicData.startedByAlarmSource != null)
				{
					// --------------------------------------------------------------
					// 13/05/2020 ECU produce a better layout for the source
					// --------------------------------------------------------------
					PublicData.startedByAlarmSource
						= String.format (getString (R.string.alarm_source_format),PublicData.startedByAlarmSource);
					// --------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 13/05/2020 ECU make if printable - but nothing to show
					// -------------------------------------------------------------
					PublicData.startedByAlarmSource = StaticData.BLANK_STRING;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 09/03/2017 ECU Note - the setting of the copyright message moved
			//                       to initialiseTasks
			// ---------------------------------------------------------------------
			// 20/06/2013 ECU display the start up screen for a while
			// 09/10/2015 ECU only show the start-up screen if started up as a new
			//                creation
			// 06/03/2016 ECU added the check on PublicData.startedByAlarm
			// 14/10/2020 ECU the start up activity was moved to initialisation
			//                check
			// ---------------------------------------------------------------------
			// 14/10/2020 ECU if (PublicData.startedManually && !PublicData.startedByAlarm)
			// 14/10/2020 ECU {
			// 14/10/2020 ECU 	// -------------------------------------------------
			// 14/10/2020 ECU 	Intent localIntent = new Intent (getBaseContext(),StartUpActivity.class);
			// 14/10/2020 ECU 	startActivityForResult (localIntent,0);
			// 14/10/2020 ECU 	// -------------------------------------------------
			// 14/10/2020 ECU }
			// ---------------------------------------------------------------------
			// 15/06/2013 ECU check if there is access to external storage - if not
			//                then the activity cannot continue
			// ---------------------------------------------------------------------
			if (!Environment.getExternalStorageState ().equals(Environment.MEDIA_MOUNTED))
			{
				// -----------------------------------------------------------------
				// 15/06/2013 ECU the SD card is not mounted so cannot continue
				// 04/01/2017 ECU changed to use resource
				// -----------------------------------------------------------------
				Toast.makeText (this,getString (R.string.sd_card_not_mounted),Toast.LENGTH_LONG).show();
				// -----------------------------------------------------------------
				PublicData.externalData = false;			// 18/06/2013 ECU not mounted
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 15/06/2013 ECU there is external storage so can continue
				// -----------------------------------------------------------------
				// 11/09/2013 ECU store the activity associated with this context
				// 15/09/2013 ECU moved here from after intialiseTasks
				// -----------------------------------------------------------------
				activity = (Activity) this;
				// -----------------------------------------------------------------
				// 22/07/2013 ECU initialised the tasks - if not already done
				// 14/10/2020 ECU if the initialisation fails then cannot start
				//                the app
				// -----------------------------------------------------------------
				if (initialiseTasks ())
				{
					// -------------------------------------------------------------
					// 14/10/2020 ECU indicate that the initialsation was OK
					// -------------------------------------------------------------
					initialised = true;
					// -------------------------------------------------------------
					// 02/04/2014 ECU set strict mode for development
					// 01/09/2015 ECU changed to use StaticData
					// -------------------------------------------------------------
					if (StaticData.STRICT_MODE)
						Utilities.StrictMode();
					// -------------------------------------------------------------
					// 15/11/2014 ECU before going any further then check if this is
					//                a valid installation
					// -------------------------------------------------------------
					if (!Installation.check (this))
					{
						// ---------------------------------------------------------
						// 15/11/2014 ECU seems to be an invalid installation
						// ---------------------------------------------------------
						finish ();
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 11/04/2015 ECU check if there has been any problems during
					// 				  initialisation that require this app to finish
					// 				  immediately. Reason is contained in
					// 				  'errorSoFinishApp'. Problem seems to arise if
					// 				  the app is restarted very quickly after it
					//                was destroyed - some resources, like ports,
					//                seem to be held for a bit of time
					// -------------------------------------------------------------
					if (PublicData.errorSoFinishApp != null)
					{
						// ---------------------------------------------------------
						// 11/04/2015 ECU something happened during initialisation
						//                means that the app should terminate
						//
						//				  Server cannot bind to ports
						// ---------------------------------------------------------
						finish ();
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 14/10/2020 ECU the start up activity moved here so that
						//                it is only actioned if the initialisation
						//                was successful
						// ---------------------------------------------------------
						if (PublicData.startedManually && !PublicData.startedByAlarm)
						{
							// -----------------------------------------------------------------
							Intent localIntent = new Intent (getBaseContext(),StartUpActivity.class);
							startActivityForResult (localIntent,0);
							// ----------------------------------------------------------------
						}
						// ---------------------------------------------------------
						// 07/10/2015 ECU if the app has been started automatically
						//                then start up the user interface
						// 06/03/2016 ECU added the check on PublicData.startedByAlarm
						// 17/06/2017 ECU check whether the 'panic alarm' is to be
						//                triggered
						// ---------------------------------------------------------
						if (!PublicData.startedManually ||
								PublicData.startedByAlarm ||
								PublicData.storedData.startPanicAlarm)
							startTheUserInterface ();
						// ---------------------------------------------------------
						// 15/07/2015 ECU log the fact that the application has started
						// ---------------------------------------------------------
						Utilities.LogToProjectFile (TAG,getString (R.string.app_initialised_and_started));
						// ---------------------------------------------------------
						// 15/12/2017 ECU log the 'start up' message
						// 16/11/2019 ECU changed replaceAll to replace
						// 06/08/2020 ECU change to use StaticData
						// ---------------------------------------------------------
						Utilities.LogToProjectFile (TAG,PublicData.startUpMessage.replace
											(StaticData.NEWLINE,StaticData.SPACE_STRING));
						// ---------------------------------------------------------
					}
				}
				else
				{
					// -------------------------------------------------------------
					// 14/10/2020 ECU the initialisation failed so exit this app
					// -------------------------------------------------------------
					initialised = false;
					finish ();
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 09/11/2016 ECU the current permissions do not allow this app to run
			// 12/11/2019 ECU change the layout so that the user is aware of
			//                issues with the runtime permissions
			// ---------------------------------------------------------------------
			setContentView (R.layout.permissions_request);
			// ---------------------------------------------------------------------
		}
     }
	/* ============================================================================== */
	@Override
	public void onActivityResult (int theRequestCode, int theResultCode, Intent theIntent)
	{
	    if (theRequestCode == StaticData.REQUEST_CODE_FINISH)
	    {
	    	// ---------------------------------------------------------------------
	    	// 17/10/2014 ECU handle the result that indicates that the calling
	    	//                activity is to 'finish' - this should only happen if
	    	//                action is confirmed by the result code.
	    	//                IMPORTANT - good idea to read the comments in 'Notes'
	    	//                =========   about restarting the application.
	    	// 03/09/2015 ECU changed to use StaticData
	    	// ---------------------------------------------------------------------
	    	if (theResultCode == StaticData.RESULT_CODE_FINISH)
	    	{
	    		// -----------------------------------------------------------------
	    		// 06/04/2014 ECU after cloning has finished just want to close the
	    		//                app and indicate that data is not to be written to
	    		//                disk
	    		// -----------------------------------------------------------------
	    		PublicData.writeDataOnDestroy = false;
	    		// -----------------------------------------------------------------
	    		// 15/11/2014 ECU because the files have been cloned then the
	    		//                activation key file will be invalid so
	    		//                need to create a new one
	    		// -----------------------------------------------------------------
	    		Installation.initialise (getBaseContext());
	    		// -----------------------------------------------------------------
	    		// 06/04/2014 ECU want to restart the app in a few seconds
	    		// 11/04/2015 ECU put into a self contained method
	    		// 07/03/2016 ECU pass through start.... as an argument
	    		// -----------------------------------------------------------------
	    		restartThisApp (getBaseContext (),PublicData.startedByAlarm);
	    		// -----------------------------------------------------------------
	    		finish ();
	    		// -----------------------------------------------------------------
	    	}
	    	// ---------------------------------------------------------------------
	    }
	}
	/* ============================================================================= */
	@Override
    public void onDestroy()
    {
		// -------------------------------------------------------------------------
		// 18/09/2013 ECU change to use the custom toast
		// 08/04/2014 ECU change to use resource
		// 11/04/2015 ECU put in the check for an abnormal closure due to
		//                initialisation errors
		// 09/11/2016 ECU if 'activity' is still null then the app has never run
		// 14/10/2020 ECU added the check on 'initialised'
		// -------------------------------------------------------------------------
		if (initialised && (activity != null))
		{
			// ---------------------------------------------------------------------
			// 09/11/2016 ECU the app has run
			// ---------------------------------------------------------------------
			if (PublicData.errorSoFinishApp == null)
			{
				// -----------------------------------------------------------------
				// 11/04/2015 ECU this is just a normal closure
				// 03/10/2015 ECU changed to use 'LogTo....' rather than using Toast
				// 12/10/2015 ECU put the Toast back as well as a useful reminder
				// 13/11/2016 ECU changed from '..destroyed' to '..closed'
				// -----------------------------------------------------------------
				Utilities.LogToProjectFile (TAG, getString (R.string.being_closed));
				// -----------------------------------------------------------------
				// 24/07/2017 ECU just put out more information.
				// -----------------------------------------------------------------
				Utilities.popToast (getString (R.string.start_up_title) +
										StaticData.NEWLINEx2 +
											getString (R.string.being_closed),true);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 11/04/2015 ECU closure is due to an initialisation problem so inform
				//                the user of the problem
				// 03/10/2015 ECU changed to use 'LogTo....'
				// -----------------------------------------------------------------
				Utilities.LogToProjectFile (TAG,getString (R.string.sorry_had_to_close)
													+ PublicData.errorSoFinishApp);
				// -----------------------------------------------------------------
				// 11/04/2015 ECU restart this app to try and get a proper initialisation
				// 07/03/2015 ECU pass through start... as an argument
				// -----------------------------------------------------------------
				restartThisApp (getBaseContext(),PublicData.startedByAlarm);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU write data to disk
			// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
			// 06/04/2014 ECU write out the data dependent on the flag
			// 11/10/2016 ECU added the check on ....Forced
			// 28/03/2020 ECU remove 'getBaseContext' on getString
			// 14/10/2020 ECU change back to 'Utilities' from 'AsyncUtilities'
			// ---------------------------------------------------------------------
			if (PublicData.writeDataOnDestroy || PublicData.writeDataOnDestroyForced)
			{
				// -----------------------------------------------------------------
				Utilities.writeObjectToDisk (PublicData.projectFolder +
												getString (R.string.stored_data_file),
												  	PublicData.storedData);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 24/02/2014 ECU check if there are any alarms then cancel them
			// ---------------------------------------------------------------------
			SpeakingClockActivity.CancelAlarm (this);
			// ---------------------------------------------------------------------
			// 21/06/2018 ECU cancel any outstanding 'random event' alarms
			//            ECU changed to use 'RandomEvent...'
			// ---------------------------------------------------------------------
			RandomEvent.cancelAlarm ();
			// ---------------------------------------------------------------------
			// ---------------------------------------------------------------------
			// 27/03/2016 ECU put in the check on whether alarms can restart the app
			// 09/05/2020 ECU Note - normally an outstanding 'alarm' will start up
			//                       the app when triggered.
			// ---------------------------------------------------------------------
			if (!PublicData.storedData.alarmsRestartApp)
			{
				// -----------------------------------------------------------------
				// 20/12/2015 ECU cancel any alarms associated with the daily scheduler
				// -----------------------------------------------------------------
				DailyScheduler.CancelAlarm (this);
				// -----------------------------------------------------------------
				// 29/01/2017 ECU cancel any stored alarms
				// -----------------------------------------------------------------
				AlarmData.cancelAllAlarms (this);
				// -----------------------------------------------------------------
				// 23/12/2015 ECU make sure that the broadcast receivers are disabled
				// 07/03/2016 ECU changed so that if the panic alarm is enabled then do not
				//                disable the Alarm... receiver
				// 27/03/2016 ECU put in the check on whether alarms can restart the app
				// -----------------------------------------------------------------
				if (!PublicData.storedData.panicAlarm.enabled)
					Utilities.BroadcastReceiverState (this, AlarmManagerReceiver.class,false);
				// -----------------------------------------------------------------
				// 27/03/2016 ECU Note - if 'daily scheduler' alarms are to restart the app then
				//                the following line should be commented out - this will
				//                leave the receiver active.
				//            ECU with the alarms.. check then this controls whether
				//                the app is restarted
				// -----------------------------------------------------------------
				Utilities.BroadcastReceiverState (this, DailyScheduler.class,false);
				// -----------------------------------------------------------------
				// 18/11/2019 ECU unregister the WiFi state change receiver
				// -----------------------------------------------------------------
				Utilities.BroadcastReceiverState (this, WiFiStateChange.class,false);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// ---------------------------------------------------------------------
			// 28/03/2020 ECU call the new method to stop the services - rather than
			//                have all the code inline here and stop each service
			//                one at a time
			// ---------------------------------------------------------------------
			// ---------------------------------------------------------------------
			StopTheServices ();
			// ---------------------------------------------------------------------
			// ---------------------------------------------------------------------
			// 07/03/2014 ECU unregister the screen on / off listeners
			// ---------------------------------------------------------------------
			ScreenOnOffReceiver.Unregister (this);
			// ---------------------------------------------------------------------
			// 20/04/2015 ECU check if anything needs to be tidied up in the MusicPlayer
			// ---------------------------------------------------------------------
			MusicPlayer.tidyUp (this);
			// ---------------------------------------------------------------------
			// 27/12/2015 ECU check if anything needs tidying up in the panic alarm
			//                handling
			// ---------------------------------------------------------------------
			PanicAlarmActivity.tidyUp (this);
			// ---------------------------------------------------------------------
			// 13/04/2019 ECU tidy up any aspects of Alexa
			// ---------------------------------------------------------------------
			Alexa.finish (this);
			// ---------------------------------------------------------------------
			// 31/12/2015 ECU make sure the bluetooth utilities are tidied up
			// 01/01/2016 ECU pass through the context as an argument
			// ---------------------------------------------------------------------
			if (PublicData.bluetoothUtilities != null)
				PublicData.bluetoothUtilities.Terminate (this);
			// ---------------------------------------------------------------------
			// 16/04/2020 ECU check if bluetooth tracking needs to be stopped
			// ---------------------------------------------------------------------
			if (StaticData.BLUETOOTH_TRACKING)
			{
				// -----------------------------------------------------------------
				// 16/04/2020 ECU check if configured to start
				// -----------------------------------------------------------------
				if (PublicData.storedData.bluetoothTracking)
				{
					// -------------------------------------------------------------
					// 16/04/2020 ECU tell bluetooth tracking to stop
					// -------------------------------------------------------------
					BluetoothTracking.Finish ();
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
			// 26/03/2020 ECU delete the 'lock file' to indicate that this is a
			//                'clean stop'
			// ---------------------------------------------------------------------
			LockFileData.LockFile (this,false);
			// ---------------------------------------------------------------------
			// 27/05/2013 ECU get the main method processed
			// ---------------------------------------------------------------------
			super.onDestroy ();
			// ---------------------------------------------------------------------
			// 18/09/2013 ECU try and exit completely
			// 26/03/2015 ECU changed from 1000
			// ---------------------------------------------------------------------
			waitABitThenDie (3000);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 09/11/2016 ECU just do a normal destroy
			// ---------------------------------------------------------------------
			super.onDestroy();
			// ---------------------------------------------------------------------
			// 08/08/2020 ECU check if this app needs to be restarted and that it is
			//                not being done by the alarmmanager
			// ---------------------------------------------------------------------
			if (restartThisApp)
			{
				// -----------------------------------------------------------------
				// 08/08/2020 ECU wait a short period of time before restarting
				// 25/09/2020 ECU changed to use StaticData
				// -----------------------------------------------------------------
				waitABitThenDie (StaticData.MILLISECONDS_PER_SECOND);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 02/03/2017 ECU make sure that shared preferences knows that the app has
		//                finished correctly
		// -------------------------------------------------------------------------
		this.SharedPreferencesHandler (this, StaticData.SHARED_PREFERENCES_START,StaticData.SHARED_PREFERENCES_DEFAULT);
		// -------------------------------------------------------------------------
    }
	/* ============================================================================= */
	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event)
	{
		// -------------------------------------------------------------------------
		// 18/09/2013 ECU added to process the back key
		// -------------------------------------------------------------------------
	    if( keyCode == KeyEvent.KEYCODE_BACK)
	    {
	    	// ---------------------------------------------------------------------
	    	// 06/10/2015 ECU want to indicate that the user is exiting the app
	    	//                - do this by deleting the 'start up file
	    	// ---------------------------------------------------------------------
	    	super.onKeyDown (keyCode,event);
	       	// ---------------------------------------------------------------------
	       	// 18/09/2013 ECU indicate that the key has been processed
	       	// ---------------------------------------------------------------------
	    	return true;
	    	// ---------------------------------------------------------------------
	    }
	    else
	    {
	    	// ---------------------------------------------------------------------
	        return super.onKeyDown(keyCode, event);
	        // ---------------------------------------------------------------------
	    }
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public boolean onPrepareOptionsMenu (Menu menu)
	{
		// -------------------------------------------------------------------------
		// 28/05/2013 ECU clear the displayed menu
		// 10/03/2015 ECU menu building removed
		// -------------------------------------------------------------------------
		return true;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onBackPressed ()
	{
		// -------------------------------------------------------------------------
		// 25/09/2020 ECU added	- want to ignore 'back key' presses if the user
		//                interface is still running
		// -------------------------------------------------------------------------
		if (PublicData.userInterfaceRunning)
		{
			// ---------------------------------------------------------------------
			// 25/09/2020 ECU tell the user that the key is being ignored
			// 28/10/2020 ECU just ignore the key stroke before telling the user
			//                was annoying
			//				  removed 'Utilities.BackKeyNotAllowed (this);'
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 25/09/2020 ECU action the back key to finish this activity
			//----------------------------------------------------------------------
			super.onBackPressed ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// -------------------------------------------------------------------------
		// 05/06/2013 ECU used the method to build menu
		// 10/03/2015 ECU removed all menu handling
		// -------------------------------------------------------------------------
		return true;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public boolean onOptionsItemSelected (MenuItem item)
	{
		// -------------------------------------------------------------------------
		// 16/06/2013 ECU take the actions depending on which menu is selected
		// 30/11/2014 ECU tidy up as the menu no longer used to start activities
		// 10/03/2015 ECU all menu handling removed
		// -------------------------------------------------------------------------
		return true;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private void initialiseDisplay ()
	{
		// -------------------------------------------------------------------------
		// 07/07/2013 ECU set up the screen size in pixels for later use
        // -------------------------------------------------------------------------
        DisplayMetrics metrics = new DisplayMetrics ();
		getWindowManager ().getDefaultDisplay ().getMetrics (metrics);
		// -------------------------------------------------------------------------
		PublicData.screenHeight = metrics.heightPixels;
		PublicData.screenWidth  = metrics.widthPixels ;
		// -------------------------------------------------------------------------
		// 30/07/2019 ECU set up the scale factors that are used in
		//				  Utilities.threeLineButtonLegend
		// 07/01/2020 ECU changed to use the stored resource value
		// -------------------------------------------------------------------------
		String secondLineScale = getString (R.string.second_line_scale);
		String thirdLineScale  = getString (R.string.third_line_scale);
		// -------------------------------------------------------------------------
		// 07/01/2020 ECU now convert to float format
		// -------------------------------------------------------------------------
		PublicData.scaleSecondLine = Float.parseFloat (secondLineScale);
		PublicData.scaleThirdLine  = Float.parseFloat (thirdLineScale);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private void initialiseFolders ()
	{
		// -------------------------------------------------------------------------
		// 22/07/2013 ECU added this method rather than having inline code
		// -------------------------------------------------------------------------
		// set up the various folder names
        // -------------------------------------------------------------------------
        File externalStorage = Environment.getExternalStorageDirectory ();
        // -------------------------------------------------------------------------
        PublicData.projectFolder = externalStorage.getAbsolutePath() +
        								getString (R.string.project_directory);
        // -------------------------------------------------------------------------
        // 21/02/2014 ECU check if the project folder exists - if not then
        //                create the necessary directory structure
        // 01/05/2015 ECU changed the name because want the directory structure to
        //                be checked at all times in case a subdirectory is missing
        // -------------------------------------------------------------------------
        Utilities.CheckDirectoryStructure (this,PublicData.projectFolder);
        // -------------------------------------------------------------------------
        // 21/02/2014 ECU moved the photos and music associated with the slide show into
        //                the project folder
        // -------------------------------------------------------------------------
        PublicData.photosFolder = PublicData.projectFolder +
        		getString (R.string.photos_directory);
        // -------------------------------------------------------------------------
        // 16/11/2016 ECU changed the name because NOT music just notes
        // -------------------------------------------------------------------------
        PublicData.photosNotesFolder = PublicData.projectFolder +
        		getString (R.string.photos_notes_directory);
        // -------------------------------------------------------------------------
        // 22/09/2013 ECU set up the folder using for tracking information
        //                used to be set up in Track.java
        // -------------------------------------------------------------------------
    	PublicData.trackFolder = PublicData.projectFolder +
    			getString (R.string.track_directory);
    	// -------------------------------------------------------------------------
    	// 20/11/2013 ECU set up the folder with dialogue files
    	// -------------------------------------------------------------------------
    	PublicData.dialogueFolder = PublicData.projectFolder +
    			getString (R.string.dialogue_directory);
    	// -------------------------------------------------------------------------
    	// 24/12/2014 ECU set up the directory that contains web pages
    	// -------------------------------------------------------------------------
    	PublicData.webFolder = PublicData.projectFolder +
    			getString (R.string.web_directory);
    	// -------------------------------------------------------------------------
    	// 16/10/2015 ECU set up the directory that EPG data
    	// -------------------------------------------------------------------------
    	PublicData.epgFolder = PublicData.projectFolder +
    			getString (R.string.epg_directory);
    	// -------------------------------------------------------------------------
    	// 23/10/2016 ECU set up the appointments folder
    	// 25/10/2016 ECU changed to sub folder because want it relative to the
    	//                project folder
    	// -------------------------------------------------------------------------
    	PublicData.appointmentsSubFolder = getString (R.string.appointments_directory);
    	// -------------------------------------------------------------------------
    	// 08/02/2015 ECU set up the file where the alarms are stored
    	// -------------------------------------------------------------------------
    	PublicData.alarmFileName = PublicData.projectFolder + getString (R.string.alarm_file_name);
    	// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private View.OnClickListener buttonClick = new View.OnClickListener()
	{
		// -------------------------------------------------------------------------
		// 03/01/2014 ECU added
		// -------------------------------------------------------------------------
		@Override
		public void onClick(View view)
		{
			// ---------------------------------------------------------------------
			switch (view.getId())
			{
				case R.id.continue_to_program:
			    {
			    	// -------------------------------------------------------------
			    	// 03/01/2014 ECU added
			    	// -------------------------------------------------------------
			    	if (!exitProgram)
			    	{
			    		// ---------------------------------------------------------
			    		// 07/10/2015 ECU everything is OK so start the user interface
			    		// ---------------------------------------------------------
			    		startTheUserInterface ();
			    		// ---------------------------------------------------------
			    	}
			    	else
			    	{
			    		// ---------------------------------------------------------
			    		// 03/01/2014 ECU want to exit the app
			    		// ---------------------------------------------------------
			    		finish ();
			    		// ---------------------------------------------------------
			    	}
					// -------------------------------------------------------------
			    	break;
			    	// -------------------------------------------------------------
				}
			  	// -----------------------------------------------------------------
		     }
		     // --------------------------------------------------------------------
	    }
	};
	/* ============================================================================= */
	private void initialiseServices ()
	{
		// -------------------------------------------------------------------------
		// 22/07/2013 ECU created this method rather than having inline code
		// -------------------------------------------------------------------------
		if (PublicData.externalData)
		{
			// ---------------------------------------------------------------------
			// 22/07/2013 ECU get the stored alarms from disk and action
			// 06/03/2016 ECU moved the actioning of stored alarms from here to
			//                GridActivity because want the user interface to be up
			//                and running before they are actioned
			// 11/08/2020 ECU changed to start the services as defined in the
			//                StaticData array
			// 12/08/2020 ECU changed to use the new method
			// ---------------------------------------------------------------------
			ServiceControl.StartAllServices(this);
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	@SuppressWarnings ("unchecked")
	// -----------------------------------------------------------------------------
	boolean initialiseTasks ()
	{
		// =========================================================================
		// Initialise all tasks required by this program
		// ------------------------------------------------------------------------
		// 14/10/2020 ECU changed to boolean to return the state of the initialisation
		// =========================================================================

		// -------------------------------------------------------------------------
		// 03/02/2015 ECU get the ID of this device
		// -------------------------------------------------------------------------
		PublicData.deviceID = Utilities.getDeviceID ();
		// -------------------------------------------------------------------------
		// 19/03/2017 ECU decide if this is a debug or release version
		// -------------------------------------------------------------------------
		PublicData.debuggable = (0 != (activity.getApplicationInfo ().flags & ApplicationInfo.FLAG_DEBUGGABLE));
		// -------------------------------------------------------------------------
		// 03/11/2016 ECU declare the global alarm manager
		// -------------------------------------------------------------------------
		PublicData.alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// -------------------------------------------------------------------------
		// 22/07/2013 ECU set up the system's folder structure
		// -------------------------------------------------------------------------
		initialiseFolders ();
		// -------------------------------------------------------------------------
		// 22/07/2013 ECU initialise voice commands
		// -------------------------------------------------------------------------
		initialiseVoiceCommands ();
		// -------------------------------------------------------------------------
        // 06/09/2013 ECU set up bluetooth utilities
        // -------------------------------------------------------------------------
        PublicData.bluetoothUtilities = new BluetoothUtilities (this);
		// -------------------------------------------------------------------------
	    // 22/07/2013 ECU start any services and things dependent on external memory
        // 09/02/2014 ECU move the InitialiseServices to the end of this method
        // -------------------------------------------------------------------------
	    initialiseDisplay ();
	    // -------------------------------------------------------------------------
	    // 25/07/2013 ECU get the IP address of this device
	    // -------------------------------------------------------------------------
	    PublicData.ipAddress = Utilities.getIPAddress (this);
	    // -------------------------------------------------------------------------
	    // 25/07/2013 ECU read device information from disk
	    // 26/07/2013 ECU use resource devices_file rather than a literal string
	    //			  ECU put the check on externalData in
	    // -------------------------------------------------------------------------
	    if (PublicData.externalData)
	    {
	    	// ---------------------------------------------------------------------
	    	// 03/04/2015 ECU changed to use the AsyncUtilities rather than Utilities
	    	// 22/03/2015 ECU changed because of new definition of deviceDetails
	    	//            ECU the very first time an exception could be caused
	    	//                because trying to cast the stored data which is a
	    	//                'Devices []' object to a 'List<Devices>' object
	    	// 27/03/2015 ECU when the object definition was changed on 22/03/15 there
	    	//                was a problem that the object stored on disk was different
	    	//                from the new definition. Initially found that a 'class
	    	//                exception' was being thrown but on the hudl2 found that
	    	//                this was not the case and an exception was being thrown
	    	//                in Utilities.processDetailsReceived - this is not a
	    	//                real problem but do not know what is happening (see
	    	//                Notes)
	    	// ---------------------------------------------------------------------
	    	try
	    	{
	    		// -----------------------------------------------------------------
	    		// 03/04/2015 ECU read the stored 'device details' from disk
	    		// -----------------------------------------------------------------
	    		PublicData.deviceDetails = (List<Devices>) AsyncUtilities.readObjectFromDisk (this,
	    				PublicData.projectFolder + getString (R.string.devices_file));
	    		// -----------------------------------------------------------------
	    		// 04/01/2017 ECU see if the addresses have changed
	    		// -----------------------------------------------------------------
	    		// 04/01/2017 ECU get the stored MAC address associated with the IP address
	    		//                on this device.
	    		// -----------------------------------------------------------------
	    		String localMACAddress = Devices.returnMACAddress (PublicData.ipAddress);
	    		// -----------------------------------------------------------------
	    		// 04/01/2017 ECU check whether the stored MAC address differs from the
	    		//                actual MAC address or a MAC address has not been
	    		//                stored for 'this' IP address. (The 'null' check
	    		//                is in case this device has never been stored)
	    		//
	    		//                This will pick up if this device has been reassigned
	    		//                an IP address
	    		// -----------------------------------------------------------------
	    		if (localMACAddress == null || (!localMACAddress.equalsIgnoreCase (Utilities.getMACAddress (this))))
	    		{
	    			// -------------------------------------------------------------
	    			// 04/01/2017 ECU it appears that the network has changed since
	    			//                this device last ran so force a 'discovery'
	    			// -------------------------------------------------------------
	    			PublicData.deviceDetails = null;
	    			// -------------------------------------------------------------
	    		}
	    		// -----------------------------------------------------------------
	    	}
	    	catch (Exception theException)
	    	{
	    		// -----------------------------------------------------------------
	    		// 22/03/2015 ECU a problem occurred so force the stored detail to
	    		//                null so that a discovery happens
	    		// -----------------------------------------------------------------
	    		PublicData.deviceDetails = null;
	    		// -----------------------------------------------------------------
	    	}
	    	// ---------------------------------------------------------------------
	    	// 01/08/2013 ECU discover the network if no record of any devices
	    	// 07/11/2013 ECU put in the check to see if always want to do a discovery on start
	    	// ---------------------------------------------------------------------
	    	if (PublicData.deviceDetails == null || PublicData.discover_always)
	    	{
	    		// -----------------------------------------------------------------
	    		// 01/08/2013 ECU start a network discovery
	    		// 04/01/2017 ECU changed to use 'localIntent'
	    		// -----------------------------------------------------------------
	    		Intent localIntent = new Intent (getBaseContext(),DiscoverNetwork.class);
	    		startActivityForResult (localIntent,0);
	    		// -----------------------------------------------------------------
	    	}
	    }
	    // -------------------------------------------------------------------------
	    // 29/07/2013 ECU get the port for the transmission / reception of buffered data
	    // -------------------------------------------------------------------------
	    PublicData.socketNumberForData = this.getResources ().getInteger (R.integer.TCP_data_port_number);
	    // -------------------------------------------------------------------------
	    // 30/12/2014 ECU get the port for the transmission / reception of web data
	    // -------------------------------------------------------------------------
	    PublicData.socketNumberForWeb = this.getResources ().getInteger (R.integer.TCP_web_port_number);
	    // -------------------------------------------------------------------------
	    // 09/01/2016 ECU get the port for the file transfer process
	    // -------------------------------------------------------------------------
	    PublicData.socketNumberForFTP = this.getResources ().getInteger (R.integer.TCP_ftp_port_number);
	    // -------------------------------------------------------------------------
	    // 02/08/2013 ECU added the creation and initialisation of the datagram
	    // 21/09/2013 ECU creation placed at the declaration
	    // -------------------------------------------------------------------------
	    PublicData.datagram = new Datagram ();
	    PublicData.datagram.Initialise (1,2,3);
	    // -------------------------------------------------------------------------
	    // 13/08/2013 ECU set up the format that I will use for formatted outputs of date/time
	    // 15/09/2013 ECU added Locale.UK
	    // 11/01/2014 ECU added the ...Full version
	    // 13/01/2014 ECU added the ...Short version
	    // 10/11/2014 ECU changed to use Locale.getDefault() instead of Locale.UK
	    // 27/10/2016 ECU changed to use the date_format stored in the resources
	    //                which will adapt to different languages (eg US versus UK)
	    //            ECU added dateSimpleFormat
	    // -------------------------------------------------------------------------
	    // 11/12/2016 ECU call up the method to initialise public variables that are
	    //                based on values stored in the resources - the actual code
	    //                used to be here
	    // -------------------------------------------------------------------------
	    setVariablesFromResources ();
		// -------------------------------------------------------------------------
		// 08/08/2013 ECU get the time when this package was last updated
		//                this will be held in PublicData.lastUpdateDate
		// 27/10/2016 ECU moved here from the onCreate method because it uses the
		//                date formatters
		// -------------------------------------------------------------------------
		Utilities.getTheLastUpdateTime (this);
		// -------------------------------------------------------------------------
		// 11/10/2015 ECU establish the copyright message that will be used
		//                throughout the rest of the application
		//            ECU NOTE - this code used to be in StartUp activity but with
		//                ====   the new checks on savedInstanceState it needed to
		//                       be placed here
		// ---------------------------------------------------------------------
		// 11/10/2015 ECU now create the copyright message which will be used
		//                throughout the rest of the application, notably on
		//                'popToast' messages
		// 09/03/2017 ECU the setting of the copyright message used to be
		//                just after the check on savedInstance.. but the lastUpdateDate
		//                was not set at that point so moved here (a NOTE has
		//                been left at the original position
		// ---------------------------------------------------------------------
		PublicData.copyrightMessage = getString (R.string.app_name) + " - " +
												 Utilities.Version (this) + " - " +
												 getString (R.string.build_copyright) + " - " +
												 PublicData.lastUpdateDate;
		// -------------------------------------------------------------------------
		// 17/10/2015 ECU initialise the start up time message
		// 02/04/2016 ECU indicate if started by an alarm
		//            ECU changed to use resources
		// 13/05/2020 ECU added the source of the alarm which has been preformatted
		//                elsewhere
		// -------------------------------------------------------------------------
		Calendar localCalendar = Calendar.getInstance();
		PublicData.startUpMessage = getString (R.string.app_started) +
										(PublicData.startedByAlarm ? getString (R.string.started_by_alarm) + PublicData.startedByAlarmSource
											 					   : (PublicData.startedManually ? getString (R.string.started_manually)
																	        					 : getString (R.string.started_automatically))) + "\nat " +
													PublicData.dateFormatterFull.format (localCalendar.getTime());
		// -------------------------------------------------------------------------
		// 08/08/2018 ECU remember the start up time
		// -------------------------------------------------------------------------
		PublicData.startUpTime = localCalendar.getTimeInMillis ();
		// -------------------------------------------------------------------------
		// 18/09/2015 ECU declare the master message handler
		// -------------------------------------------------------------------------
		PublicData.messageHandler = new MessageHandler ();
		// -------------------------------------------------------------------------
		// 06/12/2019 ECU declare the email message handler
		// -------------------------------------------------------------------------
		PublicData.emailHandler = new EmailHandler ();
		// -------------------------------------------------------------------------
		// 26/03/2017 ECU declare the comms message handler
		// -------------------------------------------------------------------------
		PublicData.commsHandler = new CommsHandler (this);
		// -------------------------------------------------------------------------
		// 14/10/2020 ECU read the 'storedData' from file
		// -------------------------------------------------------------------------
		if (StoredData.ReadFromDisk (this) == StaticData.FILE_READ_NOT_OK)
		{
			// ---------------------------------------------------------------------
			// 14/10/2020 ECU 'ReadFromDisk' can return three values
			//					StaticData.FILE_DOES_NOT_EXIST
			//					StaticData.FILE_READ_NOT_OK
			//					StaticData.FILE_READ_OK
			//                If the file does not exist then this is probably the
			//                first time the app has run so not really a problem
			//                - just use the data that is set by default
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
	    // -------------------------------------------------------------------------
	    // 11/01/2014 ECU put in the call to the method rather having the code
	    //                in line here
	    // -------------------------------------------------------------------------
	    ReadDataFromDisk ();
	    // -------------------------------------------------------------------------
	    // 30/11/2018 ECU get the path to the external SD card
	    // -------------------------------------------------------------------------
	    PublicData.externalSDCard = Utilities.getExternalSDCardPath ();
		// -------------------------------------------------------------------------
		// 14/03/2014 ECU build the search and replace strings
	    // 21/02/2018 ECU moved here so that the strings are available for the
	    //                services
		// -------------------------------------------------------------------------
		Utilities.BuildSearchStringAndReplace ();
	    // -------------------------------------------------------------------------
	    // 09/02/2014 ECU initialise any of the services - put here so that it can
	    //                use any data retrieved from disk
	    // -------------------------------------------------------------------------
	    initialiseServices ();
	    // -------------------------------------------------------------------------
	    // 03/01/2014 ECU set up the continue button
	    // -------------------------------------------------------------------------
	    continueButton = (Button) findViewById (R.id.continue_to_program);
		// -------------------------------------------------------------------------
		(continueButton).setOnClickListener (buttonClick);
		// -------------------------------------------------------------------------
		// 15/02/2014 ECU check if this device has a phone - if so then enable the listener
		// 27/10/2014 ECU store the phone number in appropriate variable
		// -------------------------------------------------------------------------
		if ((PublicData.phoneNumber = Utilities.getPhoneNumber (this)) != null)
		{
			// ---------------------------------------------------------------------
			// 15/02/2014 ECU set up the listener for changes in the the phone's state
			// ---------------------------------------------------------------------
			TelephonyManager telephonyManager	 = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
			monitorPhoneStatus 					 = new MonitorPhoneStatus (this);
			telephonyManager.listen (monitorPhoneStatus,PhoneStateListener.LISTEN_CALL_STATE);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 23/12/2015 ECU make sure that the broadcast receivers are enabled
		// 18/11/2019 ECU added the WiFi state change
		// -------------------------------------------------------------------------
		Utilities.BroadcastReceiverState (this, AlarmManagerReceiver.class,true);
		Utilities.BroadcastReceiverState (this, DailyScheduler.class,true);
		Utilities.BroadcastReceiverState (this, WiFiStateChange.class,true);
		// -------------------------------------------------------------------------
		// 23/02/2014 ECU check if there are any alarms that were registered
		//                with the AlarmManager
		// -------------------------------------------------------------------------
		SpeakingClockActivity.ReInstateAlarm (this);
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU initialise the daily scheduler
		// 08/02/2015 ECU changed to the method because on power up want
		//                an alarm in a minute from now
		// 01/09/2015 ECU changed to use StaticData
		// -------------------------------------------------------------------------
		DailyScheduler.Initialise (this,(Calendar.getInstance().getTimeInMillis() + StaticData.MILLISECONDS_PER_MINUTE));
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU generate any alarms for outstanding appointments
		// -------------------------------------------------------------------------
		AppointmentsActivity.GenerateAlarms (this);
		// -------------------------------------------------------------------------
		// 07/03/2014 ECU register the listeners for screen on / off
		// -------------------------------------------------------------------------
		ScreenOnOffReceiver.Register (this);
		// -------------------------------------------------------------------------
		// 19/11/2018 ECU check if watchdog timer needs initialising
		// -------------------------------------------------------------------------
		WatchdogTimer.Initialise (this);
		// -------------------------------------------------------------------------
		// 28/10/2014 ECU check whether the device is to be announced
		// -------------------------------------------------------------------------
		Utilities.AnnounceDevice (this);
		// -------------------------------------------------------------------------
		// 11/04/2019 ECU initialise any aspects of alexa handling
		// 15/05/2020 ECU took out because do not want to do until an 'alexa' action
		//                is performed
		//            ECU Note - if the device is 'connected' to a bluetooth device
		//                ====   when the app starts then audio output will be sent
		//                       to that device. However after an Alexa action then
		//                       the command will be sent to the bluetooth device
		//                       but other audio output will be to the device's own
		//                       speaker.
		// -------------------------------------------------------------------------
		// Alexa.initialise (this);
		// -------------------------------------------------------------------------
		// 20/03/2015 ECU build up the details of this device
		// -------------------------------------------------------------------------
		PublicData.localDeviceDetails = new Devices ();
		PublicData.localDeviceDetails.Initialise (this);
		// -------------------------------------------------------------------------
		Utilities.processDetailsReceived (this,PublicData.localDeviceDetails);
		// -------------------------------------------------------------------------
		// 22/03/2015 ECU set up the time, second within the minute, when
		//                the 'requestAddress' message will be sent
		// -------------------------------------------------------------------------
		PublicData.requestSecond = Calendar.getInstance().get (Calendar.SECOND);
		// -------------------------------------------------------------------------
		// 20/03/2015 ECU at this point want to announce 'to the world' that this
		//                device is 'up and running'
		// 25/05/2020 ECU the 'send...' placed in BroadcastServerThread
		// -------------------------------------------------------------------------
		// 25/05/2020 ECU Devices.sendHelloMessage ();
		// -------------------------------------------------------------------------
		// 26/08/2015 ECU try and start a bluetooth discovery
		// 27/08/2015 ECU changed to use the flag in stored data
		// 29/12/2015 ECU changed to start the discovery interrupt
		// -------------------------------------------------------------------------
		if (PublicData.storedData.bluetoothDiscovery)
		{
			// ---------------------------------------------------------------------
			// 27/08/2015 ECU initialise the bluetooth so that can get adapter and
			//                enable it if required.
			// 29/12/2015 ECU added the 'false' to indicate that do not want status
			//                messages about the bluetooth actions
			// ---------------------------------------------------------------------
			PublicData.bluetoothUtilities.Initialise (this,false);
			// ---------------------------------------------------------------------
			// 29/12/2015 ECU set up the alarm that will start the bluetooth
			//                discovery
			// 23/07/2020 ECU changed to use 'gap'
			// ---------------------------------------------------------------------
			BluetoothUtilities.SetAlarm (this,PublicData.storedData.bluetoothDiscoveryGap,
											StaticData.ALARM_ID_BLUETOOTH_DISCOVERY);
			// ---------------------------------------------------------------------
			// 17/04/2020 ECU Note - 'bluetooth tracking' used to be started here
			//                ----   if configured - now it is set by the
			//                       bluetooth discovery alarm
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 26/03/2020 ECU check on the 'lock file' - create it if it does not
		//                already exist
		// 31/03/2020 ECU only do the check if this is the 'first instance'
		// -------------------------------------------------------------------------
		if (Utilities.OneInstanceCheck ())
		{
			// ---------------------------------------------------------------------
			// 31/03/2020 ECU there is only one instance of this app
			// ---------------------------------------------------------------------
			if (!LockFileData.LockFile (this,true))
			{
				// -----------------------------------------------------------------
				// 26/03/2020 ECU the 'lock file' already exists which means that this
				//                app must have been 'forcibly stopped'
				// -----------------------------------------------------------------
				PublicData.lockFileData.Recover (this);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// -------------------------------------------------------------------------
		// 14/10/2020 ECU return the fact that all went well
		// -------------------------------------------------------------------------
		return true;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private void initialiseVoiceCommands ()
	{
		// -------------------------------------------------------------------------
		// 16/06/2013 ECU set up the voice commands
        // 17/06/2013 ECU change so that there is the option to have 2 dimensional arrays so
        //                that different alternatives for the same command can be stored
        //                in the VoiceCommands class
        // 28/06/2013 ECU tried to put all of the voice commands initialisation into a method in
        //                utilities but the program keeps crashing so put things back to normal
		// 06/08/2013 ECU added the listen option to cause remote listening
		//			  ECU added devices
		// 28/08/2013 ECU added multicast option
		//			  ECU changed the options on multicast option
		// 23/03/2015 ECU changed 'cry' and 'laugh' to use the MULTICAST_MESSAGE_...
		// 07/09/2015 ECU rearrange to use new constructors
		// 29/11/2015 ECU added cancel phone call
		// 09/04/2016 ECU changed from MULTICAST_ to BROADCAST_
		// -------------------------------------------------------------------------
		PublicData.voiceCommands = new VoiceCommands [] {
											new VoiceCommands (new String [] {"play","music"},
													StaticData.VOICE_COMMAND_PLAY_MUSIC,
													"start"),
											new VoiceCommands (new String [][] {{"show","photos"},
																				{"photos"}},
													StaticData.VOICE_COMMAND_SHOW_PHOTOS),
											new VoiceCommands (new String [] {"play","game","one"},
													StaticData.VOICE_COMMAND_GAME_ONE),
											new VoiceCommands (new String [][] {{"play","game","two"},
    																			{"play","game","too"},
    																			{"play","game","2"}},
    											  	StaticData.VOICE_COMMAND_GAME_TWO),
											new VoiceCommands (new String [] {"time"},
													StaticData.VOICE_COMMAND_TIME),
											new VoiceCommands (new String [][] {{"phone","phyllis"},
																				{"phyllis"}},
													StaticData.VOICE_COMMAND_PHONE,
													getString(R.string.phone_number_phyllis)),
											new VoiceCommands (new String [][] {{"phone","ed"},
    																			{"phone","8"},
    																			{"ed"}},
    												StaticData.VOICE_COMMAND_PHONE,
    												getString(R.string.phone_number_ed)),
											new VoiceCommands (new String [][] {
    												{"pause","music"},
    												{"paul's","music"}},
    												StaticData.VOICE_COMMAND_PLAY_MUSIC,"pause"),
											new VoiceCommands (new String [] {"resume","music"},
													StaticData.VOICE_COMMAND_PLAY_MUSIC,
													"resume"),
											new VoiceCommands (new String [] {"stop","music"},
													StaticData.VOICE_COMMAND_PLAY_MUSIC,
													"stop"),
											new VoiceCommands (new String [] {"mail"},
													StaticData.VOICE_COMMAND_MAIL),
											new VoiceCommands (new String [] {"bbc"},
													StaticData.VOICE_COMMAND_INTERNET,
													"http://www.bbc.co.uk/news"),
											new VoiceCommands (new String [] {"victims","voice"},
													StaticData.VOICE_COMMAND_INTERNET,
													"http://www.victimsvoice.co.uk"),
											new VoiceCommands (new String [] {"muscles"},
													StaticData.VOICE_COMMAND_INTERNET,
													"http://www.musclesforcharity.org"),
											new VoiceCommands (new String [] {"help"},
													StaticData.VOICE_COMMAND_HELP),
											new VoiceCommands (new String [] {"compass"},
													StaticData.VOICE_COMMAND_COMPASS),
											new VoiceCommands (new String [][] {{"google"},
    																			{"search"}},
    												StaticData.VOICE_COMMAND_GOOGLE,
    												"http://www.google.co.uk/search?q="),
											new VoiceCommands (new String [] {"repeat"},
													StaticData.VOICE_COMMAND_REPEAT),
											new VoiceCommands (new String [] {"who","am","i"},
													StaticData.VOICE_COMMAND_WHOAMI),
											new VoiceCommands (new String [] {"commands"},
													StaticData.VOICE_COMMAND_COMMANDS),
											new VoiceCommands (new String [][] {{"how","old","am","i"},
    																			{"what","is","my","age"},
    																			{"age"},
    																			{"birthday"},
    																			{"when","was","i","born"},
    																			{"when","is","my","birthday"}},
    												StaticData.VOICE_COMMAND_BIRTHDAY),
											new VoiceCommands (new String [][] {{"what","is","my","address"},
    																			{"address"}},
    												StaticData.VOICE_COMMAND_SPEAK,
    												getString(R.string.address_ed)),
											new VoiceCommands (new String [][] {{"message"},
    																			{"sms"}},
    												StaticData.VOICE_COMMAND_SMS),
											new VoiceCommands (new String [][] {{"medicine"},
    																			{"medication"}},
    												StaticData.VOICE_COMMAND_READ_FILE,
    												PublicData.projectFolder+getString(R.string.medication_file)),
											new VoiceCommands (new String [][] {{"start","listen"},
																				{"start","listening"}},
													StaticData.VOICE_COMMAND_LISTEN_START),
											new VoiceCommands (new String [][] {{"stop","listen"},
    																			{"stop","listening"}},
    												StaticData.VOICE_COMMAND_LISTEN_STOP),
											new VoiceCommands (new String [][] {{"device"},
																				{"devices"}},
													StaticData.VOICE_COMMAND_DEVICES),
											new VoiceCommands (new String [][] {{"cry"},
																				{"baby"}},
													StaticData.VOICE_COMMAND_BROADCAST,
													StaticData.BROADCAST_MESSAGE_CRY),
											new VoiceCommands (new String [][] {{"laugh"}},
													StaticData.VOICE_COMMAND_BROADCAST,
													StaticData.BROADCAST_MESSAGE_LAUGH),
											new VoiceCommands (new String [][] {{"television"}},
													StaticData.VOICE_COMMAND_TELEVISION_ON),
											new VoiceCommands (new String [] {"cancel","phone","call"},
													StaticData.VOICE_COMMAND_CANCEL_CALL),
											};
	}
	// =============================================================================
	void ReadDataFromDisk ()
	{
		// -------------------------------------------------------------------------
		// 11/01/2014 ECU created - read data from disk on start up
		// 03/02/2014 ECU changed to use the array of stored ID
		// 01/09/2015 ECU changed to use StaticData
		// 04/11/2016 ECU add the final 'false' flag to indicate that the retrieved
		//                file is not to be processed - e.g. the appointments
		//                file is not to be processed to generate associated alarms
		// -------------------------------------------------------------------------
		for (int theFileIndex = 0; theFileIndex < StaticData.FILES_TO_READ_FROM_DISK.length; theFileIndex++)
		{
			// ---------------------------------------------------------------------
			// 07/06/2017 ECU Note - read each indexed file from disk
			// ---------------------------------------------------------------------
			Utilities.ReadObjectFromDisk (this, StaticData.FILES_TO_READ_FROM_DISK [theFileIndex].resourceID,
												StaticData.FILES_TO_READ_FROM_DISK [theFileIndex].synchroniseFlag,
												false);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 12/02/2014 ECU set up the project log file
		// 30/08/2020 ECU moved here so that any 'initialisation' issues can be logged
		//                properly
		// -------------------------------------------------------------------------
		PublicData.projectLogFile = PublicData.projectFolder + this.getString (R.string.project_log_file);
		// -------------------------------------------------------------------------
		// 25/02/2014 ECU the 'project log' is only for the current session so delete
		//                it here
		// 23/11/2014 ECU changed to use the method
		// 15/07/2015 ECU make the 'clearing' optional
		// 30/08/2020 ECU moved here so that any 'initialisation' issues can be logged
		//                properly
		// -------------------------------------------------------------------------
		if (PublicData.storedData.clearProjectLog)
			Utilities.ProjectLogClear();
		// -------------------------------------------------------------------------
		// 24/10/2015 ECU set the file name where the stored data will be written
		// 25/10/2015 ECU added the check on 'initialised' - this is only the very
		//                first time the app is run after the variable has been added
		//                during development. The setFileName method will set both
		//                the file name and the initialised flag
		// 16/01/2014 ECU changed from setFileName - see the notes in 'StoredData'
		// -------------------------------------------------------------------------
		if (!PublicData.storedData.initialised)
		{
			PublicData.storedData.setToInitialised ();
		}
		// -------------------------------------------------------------------------
	    // 10/01/2014 ECU set up the carer log file
	    // -------------------------------------------------------------------------
	    PublicData.carerLogFile = PublicData.projectFolder + this.getString (R.string.carer_log_file);
	    // -------------------------------------------------------------------------
	    // 15/07/2017 ECU set up the visit log file
	    // -------------------------------------------------------------------------
	    PublicData.visitLogFile = PublicData.projectFolder + this.getString (R.string.visit_log_file);
	    // -------------------------------------------------------------------------
	    // 04/01/2014 ECU get the email details from disk or just set values
	    // -------------------------------------------------------------------------
	    Utilities.GetEmailDetails (this);
	    // -------------------------------------------------------------------------
	    // 05/01/2014 ECU get the patient details from disk or just set values
	    // -------------------------------------------------------------------------
	    Utilities.GetPatientDetails (this);
	    // -------------------------------------------------------------------------
	    // 03/01/2014 ECU display contents of care details file
	    // -------------------------------------------------------------------------
	    careDetails = (TextView) findViewById (R.id.care_details);
	    // -------------------------------------------------------------------------
	    // 06/06/2017 ECU want the welcome string for the client
	    // -------------------------------------------------------------------------
	    String clientWelcomeString = null;
	    // -------------------------------------------------------------------------
	    // 03/01/2014 ECU get the details from the file
	    // 03/06/2019 ECU pass through the context
	    // -------------------------------------------------------------------------
	    byte [] careDetailsText
	    	= Utilities.readAFile (this,PublicData.projectFolder + this.getString (R.string.care_details_file));
	    // -------------------------------------------------------------------------
	    // 06/06/2017 ECU check if anything was returned
	    // -------------------------------------------------------------------------
	    if (careDetailsText != null)
	    {
	    	clientWelcomeString = new String (careDetailsText);
	    }
	    else
	    {
	    	// ---------------------------------------------------------------------
	    	// 06/06/2017 ECU use the default raw resource
	    	// ---------------------------------------------------------------------
	    	clientWelcomeString = Utilities.readRawResource (this,R.raw.client_welcome_string);
	    	// ---------------------------------------------------------------------
	    }
	    // -------------------------------------------------------------------------
	    // 05/01/2014 ECU Try and personalise the display
	    // -------------------------------------------------------------------------
	    String welcomeString = getString (R.string.welcome_message);

	    if (PublicData.patientDetails != null)
	    {
	    	welcomeString = "Hello " + PublicData.patientDetails.preferredName + ",\n\n" + welcomeString;
	    }

	    if (clientWelcomeString != null)
	    {
	    	careDetails.setText (welcomeString + StaticData.NEWLINE + clientWelcomeString);
	    }
	    // -------------------------------------------------------------------------
	    // 11/10/2014 ECU make sure the gridType is initialised
	    // -------------------------------------------------------------------------
	    PublicData.gridType = PublicData.storedData.userView;
	    // -------------------------------------------------------------------------
	    // 18/11/2014 ECU make sure the monitor block is created if set to null
	    // -------------------------------------------------------------------------
	    if (PublicData.storedData.monitor == null)
	    	PublicData.storedData.monitor = new Monitor (false,10000);
	    // -------------------------------------------------------------------------
	    // 21/06/2018 ECU if there is no 'random event' object then create one
	    // -------------------------------------------------------------------------
	    if (PublicData.storedData.randomEvent == null)
	    	PublicData.storedData.randomEvent = new RandomEvent ();
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void restartThisApp (Context theContext,boolean theAlarmFlag,int theRestartDelay,String theAlarmSource)
	{
		// -------------------------------------------------------------------------
		// 11/04/2015 ECU created to contain the code to restart this app
		// 12/11/2019 ECU add theRestartDelay as an argument
		// 13/05/2020 ECU added the source of the restart
		// 08/08/2020 ECU 'restartIntent' is declared at the head of the class so
		//                remove it's declaration from here
		// -------------------------------------------------------------------------
		restartIntent = new Intent (theContext,MainActivity.class);
		// -------------------------------------------------------------------------
		// 06/03/2016 ECU if necessary remember that this activity is being restarted
		//                by an alarm that was received when the app was not running
		// 07/03/2016 ECU changed to used theAlarmFlag rather than checking the
		//                actual variable
		// -------------------------------------------------------------------------
		if (theAlarmFlag)
		{
			// ---------------------------------------------------------------------
			// 06/03/2016 ECU store the state in the intent so that it will be picked
			//                up by the app when it restarts
			// ---------------------------------------------------------------------
			restartIntent.putExtra (StaticData.PARAMETER_ALARM_START,true);
			// ---------------------------------------------------------------------
			// 13/05/2020 ECU store the source of the trigger
			// ---------------------------------------------------------------------
			if (theAlarmSource != null)
			{
				// -----------------------------------------------------------------
				// 13/05/2020 ECU store the source of the alarm in the intent
				// -----------------------------------------------------------------
				restartIntent.putExtra (StaticData.PARAMETER_ALARM_SOURCE,theAlarmSource);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 07/08/2020 ECU check on the OS level because sine Oreo then the alarm
		//                seems to be lost when the app closes - to do with the
		//                'broadcast receiver'
		// -------------------------------------------------------------------------
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
		{
			// ---------------------------------------------------------------------
			// 03/11/2016 ECU do not use the global alarm manager because it may not
			//                be set up yet
			// ---------------------------------------------------------------------
			PendingIntent restartPendingIntent
				= PendingIntent.getActivity (theContext,8337,restartIntent,PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager alarmManager = (AlarmManager) theContext.getSystemService (Context.ALARM_SERVICE);
			// ---------------------------------------------------------------------
			// 24/12/2015 ECU changed to use the new method
			// 12/11/2019 ECU changed to use theRestartDelay
			// ---------------------------------------------------------------------
			Utilities.SetAnExactAlarm (alarmManager,System.currentTimeMillis() + theRestartDelay,restartPendingIntent);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 07/08/2020 ECU come here when the OS (starting at Oreo) does not
			//                retain the alarm over the 'restart'
			// ---------------------------------------------------------------------
			// 08/08/2020 ECU indicate that the app is to be restarted immediately
			//                rather than use an alarm which does not seem to work
			//                for versions of Android >= Oreo (29)
			// ---------------------------------------------------------------------
			restartThisApp = true;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void restartThisApp (Context theContext,boolean theAlarmFlag,int theRestartDelay)
	{
		// -------------------------------------------------------------------------
		// 13/05/2020 ECU call the main method - the 'null' indicates that not
		//                interested in the source of the 'restart'
		// -------------------------------------------------------------------------
		restartThisApp (theContext,theAlarmFlag,theRestartDelay,null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void restartThisApp (Context theContext,boolean theAlarmFlag)
	{
		// -------------------------------------------------------------------------
		// 12/11/2019 ECU this used to be the old master method
		// -------------------------------------------------------------------------
		restartThisApp (theContext,theAlarmFlag,StaticData.RESTART_TIME);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void restartThisApp (Context theContext,boolean theAlarmFlag,String theAlarmSource)
	{
		// -------------------------------------------------------------------------
		// 13/05/2020 ECU created to handle the specification of the 'alarm source'
		// -------------------------------------------------------------------------
		restartThisApp (theContext,theAlarmFlag,StaticData.RESTART_TIME,theAlarmSource);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void setVariablesFromResources ()
	{
		// -------------------------------------------------------------------------
		// 11/12/2016 ECU created to set PublicData variables which are based on
		//                resource values and which are used often within the app
		// -------------------------------------------------------------------------
		PublicData.daysOfTheWeek = getResources().getStringArray (R.array.days_of_week);
		// -------------------------------------------------------------------------
	    // 28/10/2016 ECU get the day/month format from the resources
	    // -------------------------------------------------------------------------
	    String localDayMonth			= getString (R.string.day_month_format);
	    // -------------------------------------------------------------------------
	    // 28/10/2016 ECU now store the formats for date that may be used
	    // -------------------------------------------------------------------------
	    PublicData.dateFormatDDMMYY		= localDayMonth + "/yy";
	    PublicData.dateFormatDDMMYYYY	= localDayMonth + "/yyyy";
	    // -------------------------------------------------------------------------
	    // 30/10/2016 ECU added ....HHMMDDMMYY
	    // -------------------------------------------------------------------------
	    PublicData.dateFormatter 		= new SimpleDateFormat ("HH:mm:ss.SSS",Locale.getDefault());
	    PublicData.dateFormatterFull 	= new SimpleDateFormat (PublicData.dateFormatDDMMYYYY + " HH:mm:ss",Locale.getDefault());
	    PublicData.dateFormatterShort	= new SimpleDateFormat ("HH:mm",Locale.getDefault());
	    PublicData.dateSimpleFormat		= new SimpleDateFormat (PublicData.dateFormatDDMMYYYY,Locale.getDefault());
	    PublicData.dateSimpleFormatDDMMYYHHMM
										= new SimpleDateFormat (PublicData.dateFormatDDMMYY + " HH:mm",Locale.getDefault());
		PublicData.dateSimpleFormatHHMMDDMMYY
										= new SimpleDateFormat("HH:mm " + PublicData.dateFormatDDMMYY,Locale.getDefault());
	    // -------------------------------------------------------------------------
	    // 07/02/2015 ECU added other formats for alarm handling
	    // 04/09/2015 ECU added PublicData
	    // 04/03/2016 ECU changed to use the StaticData variables rather than literals
	    // -------------------------------------------------------------------------
	    PublicData.alarmDateFormat = new SimpleDateFormat (StaticData.ALARM_DATE_FORMAT,Locale.getDefault());
		PublicData.alarmTimeFormat = new SimpleDateFormat (StaticData.ALARM_TIME_FORMAT,Locale.getDefault());
	    // -------------------------------------------------------------------------
	    // 04/04/2014 ECU declare the format used when getting the current time
	    // 10/11/2014 ECU changed to use Locale.getDefault instead of Locale.UK
	    // -------------------------------------------------------------------------
		PublicData.dateFormatterCurrent = new SimpleDateFormat("HH:mm:ss E dd MMM yyyy",Locale.getDefault());
		// -------------------------------------------------------------------------
		// 30/07/2020 ECU now indicate that everything has been initialised
		// -------------------------------------------------------------------------
		PublicData.initialised = true;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	long SharedPreferencesHandler (Activity theActivity,String theKey)
	{
		// -------------------------------------------------------------------------
		// 02/03/2017 ECU created to handle some general reading / writing to the
		//                shared preferences area for this app
		// -------------------------------------------------------------------------
		SharedPreferences sharedPref = theActivity.getPreferences (Context.MODE_PRIVATE);
		// -------------------------------------------------------------------------
		// return the read value or the default
		// -------------------------------------------------------------------------
		return sharedPref.getLong (theKey, StaticData.SHARED_PREFERENCES_DEFAULT);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void SharedPreferencesHandler (Activity theActivity,String theKey,long theValue)
	{
		// -------------------------------------------------------------------------
		// 02/03/2017 ECU created to write the specified value against the key in
		//                shared preferences
		// -------------------------------------------------------------------------
		SharedPreferences sharedPref = theActivity.getPreferences (Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit ();
		editor.putLong (theKey,theValue);
		editor.commit ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void startTheUserInterface ()
	{
		// -------------------------------------------------------------------------
		// 07/10/2015 ECU create to start up the user interface
		// -------------------------------------------------------------------------
		// 17/10/2014 ECU added a proper request code
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (getBaseContext(),GridActivity.class);
		startActivityForResult (localIntent,StaticData.REQUEST_CODE_FINISH);
		// -------------------------------------------------------------------------
		// 03/01/2014 ECU indicate that the next press will close the app
		// -------------------------------------------------------------------------
		exitProgram = true;
		// -------------------------------------------------------------------------
		// 30/09/2020 ECU Note - tidy up the display
		// -------------------------------------------------------------------------
		continueButton.setText (R.string.press_to_exit);
		continueButton.setVisibility (View.INVISIBLE);
		careDetails.setVisibility    (View.INVISIBLE);
		//  ------------------------------------------------------------------------
		// 03/01/2014 ECU get the details from the file
		// 03/06/2019 ECU pass through the context
		// 06/05/2020 ECU if the text file does not exist then use the data stored in
		//                the raw directory
	    // -------------------------------------------------------------------------
	    byte [] careDetailsText = Utilities.readAFile (getBaseContext (),PublicData.projectFolder + getString (R.string.acknowledgement_file));
		// -------------------------------------------------------------------------
	    if (careDetailsText != null)
	    {
	    	careDetails.setText (new String (careDetailsText));
	    }
		else
		{
			// ---------------------------------------------------------------------
			// 06/05/2020 ECU use the default raw resource
			// ---------------------------------------------------------------------
			careDetails.setText (Utilities.readRawResource (this,R.raw.acknowledgement_string));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void StopTheServices ()
	{
		// -------------------------------------------------------------------------
		// 28/03/2020 ECU created to contain the code for stopping the services and
		//                make the code easier to read. This code used to be 'inline'
		//                in the 'onDestroy' method and used to have code to stop
		//                each service in turn
		//
		//				  The services to be stopped are (order not relevant) :-
		//
		//						TimerService
		//						ServerService
		//						WebServerService
		//                      WeMoService (if enabled)
		//						SensorService
		//						MonitorService
		//						BlueToothService
		//						TextToSpeechService
		//						BluetoothConnectorService
		//
		//				  There are probably ways of getting the list of services
		//                dynamically but this is clearer to me
		// 07/08/2020 ECU changed to use the StaticData array of classes
		// 11/08/2020 ECU changed to use 'ServiceControl'
		// -------------------------------------------------------------------------
		ServiceControl.StopAllServices (this);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	private void waitABitThenDie (final int theWaitTime)
	{
		// -------------------------------------------------------------------------
		// 18/09/2013 ECU created
		// -------------------------------------------------------------------------
		Thread thread = new Thread()
		{
			// ---------------------------------------------------------------------
			@Override
	        public void run()
	        {
	            try
	            {
	                synchronized (this)
	                {
	                	// ---------------------------------------------------------
	                	// 18/09/2013 ECU wait a while and then issue the command to
	                	//                kill this process - not happy with this
	                	// ----------------------------------------------------------
	                    sleep (theWaitTime);
	                    // ----------------------------------------------------------
	                    // 15/07/2015 ECU log the fact that the application has finished
	                    // ----------------------------------------------------------
	                    Utilities.LogToProjectFile (TAG, getString (R.string.app_finished));
	                    // ----------------------------------------------------------
	                    // 08/08/2020 ECU check if the app is to be restarted
	                    // ----------------------------------------------------------
	                    if (!restartThisApp)
						{
							// ------------------------------------------------------
							// 08/08/2020 ECU the restart will be performed by an alarm
							//                which will already have been set by this
							//                point
	                    	// ------------------------------------------------------
	                    	android.os.Process.killProcess(android.os.Process.myPid());
	                    	// ------------------------------------------------------
						}
						else
						{
							// -------------------------------------------------------
							// 08/08/2020 ECU the app is to be restarted - that has to
							//                be done here rather than using an alarm
							// -------------------------------------------------------
							Utilities.FinishAndRestartApp (getBaseContext(),restartIntent);
							// -------------------------------------------------------
						}
	                }
	            }
	            catch(InterruptedException theException)
	            {
	            }
	        }
	    };
	    // -------------------------------------------------------------------------
	    // 18/09/2013 ECU start this thread
	    // -------------------------------------------------------------------------
	    thread.start();
	    // -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	protected void onPause()
	{
		// -------------------------------------------------------------------------
	   	super.onPause ();
	   	// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onRestart ()
	{
		// ------------------------------------------------------------------------
		// 03/07/2020 ECU changed from 'onStop'
		// -------------------------------------------------------------------------
		super.onRestart ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onRestoreInstanceState (Bundle savedInstanceState)
	{
		// -------------------------------------------------------------------------
		super.onRestoreInstanceState (savedInstanceState);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	protected void onResume()
	{
		// -------------------------------------------------------------------------
	  	// 03/01/2014 ECU make the various UI components visible
		// 11/04/2015 ECU check that the UI elements exist
	  	// -------------------------------------------------------------------------
		if (continueButton != null)
			continueButton.setVisibility (View.VISIBLE);
		if (careDetails != null)
			careDetails.setVisibility (View.VISIBLE);
		// -------------------------------------------------------------------------
	   	super.onResume();
	}
	// =============================================================================
	@Override
	public void onSaveInstanceState (Bundle savedInstanceState)
	{
		// -------------------------------------------------------------------------
		super.onSaveInstanceState (savedInstanceState);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onStart ()
	{
		// ------------------------------------------------------------------------
		// 03/07/2020 ECU changed from 'onStop'
		// -------------------------------------------------------------------------
		super.onStart ();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public void onStop()
	{
		// -------------------------------------------------------------------------
		super.onStop();
		// -------------------------------------------------------------------------
	}
	// =============================================================================


	// =============================================================================
	// 12/11/2019 ECU the following method is called when the user has responded
	//                to the dialogue requesting the granting of a permission
	// =============================================================================
	@Override
	public void onRequestPermissionsResult (int requestCode,
											String [] permissions,
											int [] grantResults)
	{
		// -------------------------------------------------------------------------
		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
		{
			// ---------------------------------------------------------------------
			// 12/11/2019 ECU the user has granted permission
			// ---------------------------------------------------------------------
			if (RuntimePermissions.check (this))
			{
				// -----------------------------------------------------------------
				// 12/11/2019 ECU all of the required permissions have been granted
				//                so now restart the app after 1 second
				// -----------------------------------------------------------------
				restartThisApp (this,false,StaticData.ONE_SECOND);
				// -----------------------------------------------------------------
				// 12/11/2019 ECU now terminate this app
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 12/11/2019 ECU the user has not granted permission so terminate
			// ---------------------------------------------------------------------
			finish ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================

}
package com.usher.diboson;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimerService extends Service
{
	// =============================================================================
	// TimerService
	// ============
	// This class constitutes the 'timer service' which provides timing facilities
	// for the rest of the Diboson app
	// =============================================================================

	// =============================================================================
	// 03/06/2013 ECU created
	// 03/08/2013 ECU added code to handle remote music playing
	// 12/02/2014 ECU changed to use onStartCommand rather than onStart
	// 04/04/2014 ECU changed so that threads are used rather than handlers because
	//                the handlers seem to always run on the 'main' thread
	// 10/01/2015 ECU have a general tidy up and declare a number of new Methods to make
	//                the code more readable - not necessarily more efficient
	//            ECU took out the monitoring of client sockets
	// 05/12/2015 ECU took out code that caused the ProcessMinutes to call up
	//                PanicAlarmActivity
	// 28/04/2020 ECU just did a bit of tidying up to use resources rather than
	//                literal strings
	// 02/10/2020 ECU added 'minuteListeners'
	// =============================================================================
	// Tested
	// ======
	// =============================================================================
	final static String TAG = "TimerService";
	// =============================================================================
	// 22/02/2014 ECU if the device is in standby mode then slow down the second handler
	//                by adding in SCREEN_OFF_DELAY when putting the handler into 'sleep'
	// -----------------------------------------------------------------------------
	public final static int		SCREEN_OFF_DELAY	= (10 * StaticData.ONE_SECOND);
																	// 22/02/2014 added
																	// 28/04/2020 changed to use ONE_SECOND
	// =============================================================================
	private final static int	NOTIFICATION_COUNT	= 30;			// 04/04/2014 ECU added
																	// 09/01/2015 ECU changed from 10
	private static final boolean
								WAKELOCK_ENABLED 	= false;		// 03/05/2015 ECU moved here from variable
	// =============================================================================
	int 						datagramCounter 	= 0;			// 02/08/2013 ECU added
	static List<ListenerMethod> minuteListeners		= new ArrayList<ListenerMethod>();
	int 						notificationIcon 	= R.drawable.timer_icon_on;
	int							notificationCounter = NOTIFICATION_COUNT;
	NotificationManager 		notificationManager;	
	PowerManager				powerManager;						// 22/02/2014 ECU added
	int 						secondCounter 		= 0;
	PowerManager.WakeLock 		wakeLock;							// 12/02/2014 ECU added 
	// =============================================================================
	@Override
	public IBinder onBind (Intent arg0)
	{
		// -------------------------------------------------------------------------
		return null;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onCreate ()
	{
		// -------------------------------------------------------------------------
		// 12/10/2015 ECU call the super create
		// -------------------------------------------------------------------------
		super.onCreate ();
		// -------------------------------------------------------------------------
		// 23/06/2013 ECU put in debug mode
		// 10/01/2015 ECU changed to use the new Method
		// 28/04/2020 ECU changed to use resource
		// -------------------------------------------------------------------------
		Utilities.debugPopToast (getString (R.string.timer_service_created));
		// -------------------------------------------------------------------------
		// 26/06/2013 ECU add NotificationManager
		// -------------------------------------------------------------------------
		notificationManager 
			= (NotificationManager) this.getSystemService (NOTIFICATION_SERVICE);
		// -------------------------------------------------------------------------
		// 22/02/2014 ECU set up the power manager which will be used for monitoring
		//                the screen
		// -------------------------------------------------------------------------
		powerManager = (PowerManager) getSystemService (POWER_SERVICE);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public int onStartCommand (Intent intent,int flags, int startId) 
	{
		// -------------------------------------------------------------------------
		// 23/06/2013 ECU put in debug mode
		// 10/01/2015 ECU changed to use the new Method
		// 28/04/2020 ECU changed to use resource
		// -------------------------------------------------------------------------
		Utilities.debugPopToast (getString (R.string.timer_service_started));
		// -------------------------------------------------------------------------
		// 22/06/2013 ECU set up the timing mechanisms
		// 26/03/2015 ECU put in the check on whether the threads are already alive.
		//                This seems to happen if the app is restarted very quickly 
		//                after it has been destroyed
		// -------------------------------------------------------------------------
		if (!processMinutesThread.isAlive())
			processMinutesThread.start ();
		if (!processSecondsThread.isAlive())
			processSecondsThread.start ();
	    // -------------------------------------------------------------------------
	    // 12/02/2014 ECU try and get the wake lock
	    // 23/02/2014 ECU put in the check whether lock using is enabled
		// 03/05/2015 ECU changed to use WAKELOCK_ENABLED rather than a variable
	    // -------------------------------------------------------------------------
	    if (WAKELOCK_ENABLED)
	    {
	    	PowerManager powerManager = (PowerManager) getSystemService (Context.POWER_SERVICE);
	    	wakeLock = powerManager.newWakeLock (PowerManager.PARTIAL_WAKE_LOCK, TAG);
	    	// ---------------------------------------------------------------------
	    	// 12/02/2014 ECU acquire the lock
	    	// 05/07/2020 ECU added the timeout
	    	// ---------------------------------------------------------------------
	    	wakeLock.acquire (StaticData.WAKELOCK_TIMEOUT);
	    	// ---------------------------------------------------------------------
	    }
	    // -------------------------------------------------------------------------
	    // 12/02/2014 ECU return added 
	    // -------------------------------------------------------------------------
	    return Service.START_STICKY;
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onDestroy() 
	{
		// -------------------------------------------------------------------------
		// 23/06/2013 ECU put in debug mode
		// 10/01/2015 ECU changed to use the new Method
		// 28/04/2020 ECU changed to use resource
		// -------------------------------------------------------------------------
		Utilities.debugPopToast (getString (R.string.timer_service_destroyed));
		// -------------------------------------------------------------------------
		// 22/06/2013 ECU indicate that timing mechanism must stop
		// 04/04/2014 ECU modify to stop the threads using interrupt
		// -------------------------------------------------------------------------
		processMinutesThread.interrupt (); 
	    processSecondsThread.interrupt ();
		// -------------------------------------------------------------------------
		// 22/07/2013 ECU remove any notifications
		// -------------------------------------------------------------------------
		notificationManager.cancelAll ();
		// -------------------------------------------------------------------------
		// 12/02/2014 ECU release the wake lock
		// 23/02/2014 ECU put in the check as to whether wakelock use is enabled
		// 03/05/2015 ECU changed to use WAKELOCK_ENABLED rather than a variable
		// -------------------------------------------------------------------------
		if (WAKELOCK_ENABLED)
		{
			// ---------------------------------------------------------------------
			// 14/07/2020 ECU check if the lock is held
			// ---------------------------------------------------------------------
			if (wakeLock.isHeld ())
			{
				wakeLock.release ();
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static ListenerMethod addMinuteListener (Object theObject, Method theMethod)
	{
		// -------------------------------------------------------------------------
		// 02/10/2020 ECU add a listener for 'minutes'
		// -------------------------------------------------------------------------
		ListenerMethod listenerMethod = new ListenerMethod (theObject,theMethod);
		minuteListeners.add (listenerMethod);
		// -------------------------------------------------------------------------
		return listenerMethod;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void checkDatagramIssues ()
	{
		// -------------------------------------------------------------------------
		// 10/01/2015 ECU created to include the code that checks for various
		//                datagram issues
		// -------------------------------------------------------------------------
		if (PublicData.datagramToSend)
		{
			// ---------------------------------------------------------------------
			// 02/08/2013 ECU indicate that datagram processed
			// ---------------------------------------------------------------------
			PublicData.datagramToSend = false;
			// ---------------------------------------------------------------------
			// 02/08/2013 ECU now send the datagram
			// 03/08/2013 ECU added the ... _ACTION and change the layout a bit
			// 21/03/2015 ECU changed to datagramType from socketMessageType
			// ---------------------------------------------------------------------
			if (PublicData.datagramType == StaticData.SOCKET_MESSAGE_DATAGRAM)
			{
				// -----------------------------------------------------------------
				// 21/10/2014 ECU check if the datagram is to be broadcast or not
				// 31/01/2015 ECU change the name of the method called
				// 21/03/2015 ECU pass through the message type as an argument
				// -----------------------------------------------------------------
				if (!PublicData.datagram.broadcastFlag)
				{
					// -------------------------------------------------------------
					Utilities.sendSocketMessageSendTheObject
						(getBaseContext (),
						 PublicData.datagramReceiver,
						 PublicData.socketNumberForData,
						 StaticData.SOCKET_MESSAGE_DATAGRAM,
						 PublicData.datagram);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 31/01/2015 ECU change the name of the method called
					// 21/03/2015 ECU pass through the message type as an argument
					// -------------------------------------------------------------
					Utilities.sendSocketMessageSendObjectToAllDevices
						(getBaseContext (),
						 PublicData.deviceDetails,
						 PublicData.socketNumberForData,
						 StaticData.SOCKET_MESSAGE_DATAGRAM,
						 PublicData.datagram);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			else
			// ---------------------------------------------------------------------
			if (PublicData.datagramType == StaticData.SOCKET_MESSAGE_DATAGRAM_ACTION)
			{
				// -----------------------------------------------------------------
				Utilities.sendDatagram (getBaseContext (),
										PublicData.datagramIPAddress,
										StaticData.SOCKET_MESSAGE_DATAGRAM_ACTION,
										PublicData.datagram);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 03/08/2013 ECU check if there is a datagram to be processed
		// -------------------------------------------------------------------------
		if (PublicData.datagramToAction)
		{
			// ---------------------------------------------------------------------
			// 03/08/2013 ECU indicate that datagram processed
			// ---------------------------------------------------------------------
			PublicData.datagramToAction = false;
			// ---------------------------------------------------------------------
			// 03/08/2013 ECU action the received datagram
			// ---------------------------------------------------------------------
			Utilities.actionDatagram (getBaseContext());
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void checkMinuteListeners ()
	{
		// -------------------------------------------------------------------------
		// 02/10/2020 ECU check if there are registered listeners
		// -------------------------------------------------------------------------
		if (minuteListeners.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 02/10/2020 ECU get the current time
			// ---------------------------------------------------------------------
			long currentTime = Utilities.getAdjustedTime (true);
			// ---------------------------------------------------------------------
			// 02/10/2020 ECU loop through the registered listeners
			// ---------------------------------------------------------------------
			for (ListenerMethod listenerMethod : minuteListeners)
			{
				// -----------------------------------------------------------------
				// 02/10/2020 ECU invoke this listener
				// -----------------------------------------------------------------
				listenerMethod.Invoke (currentTime);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	void checkMusicIssues ()
	{
		// -------------------------------------------------------------------------
		// 10/01/2015 ECU created to include the code that checks for various
		//                music issues
		// -------------------------------------------------------------------------
		// 22/03/2017 ECU NOTE - there was some code here which was monitoring whether
		//                       the track from a remote server had finished playing.
		//                       This is now in the MessageHandler and triggered
		//                       by the media player calling a method on completion
		// -------------------------------------------------------------------------
		// 20/08/2013 ECU check if music player timeout is to be handled
		// 22/03/2017 ECU Note - the following code is set up to handle a timeout
		//                       which is set after the receipt of a music file. If
		//                       the timeout expires before a PLAY request is
		//                       received then this device will send a .._PLAYED
		//                       message to prompt the music server.
		// -------------------------------------------------------------------------
		if (PublicData.playTimeout != StaticData.NO_RESULT)
		{	
			// ---------------------------------------------------------------------
			if (--PublicData.playTimeout == 0)
			{
				// -----------------------------------------------------------------
				// 22/03/2017 ECU Note - the timeout has expired so try and prompt
				//                       the music server
				// -----------------------------------------------------------------
				// 20/08/2013 ECU try and prompt the server by sending a PLAYED socket message
				// -----------------------------------------------------------------
				Utilities.sendDatagramType (getBaseContext (),
										    PublicData.musicServer,
										    StaticData.SOCKET_MESSAGE_PLAYED);
				// -----------------------------------------------------------------
				// 20/08/2013 ECU indicate no more handling is required
				// -----------------------------------------------------------------
				PublicData.playTimeout = StaticData.NO_RESULT;
				// -----------------------------------------------------------------
			}
		}
	}
	// =============================================================================
	void checkStreamingIssues (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 10/01/2015 ECU created to include the code that checks for various
		//                streaming issues
		// -------------------------------------------------------------------------
		if (PublicData.startStreaming)
		{
			// ---------------------------------------------------------------------
			// 06/08/2013 ECU indicate that action taken
			// ---------------------------------------------------------------------
			PublicData.startStreaming = false;
			// ---------------------------------------------------------------------
			// 06/08/2013 ECU want to start audio streaming
			// 10/01/2015 ECU added use of PARAMETER_REMOTE
			// 15/09/2017 ECU changed to use BLANK_STRING
			// ---------------------------------------------------------------------
			Intent localIntent = new Intent (theContext,AudioStreamActivity.class);
			localIntent.putExtra (StaticData.PARAMETER_REMOTE,StaticData.BLANK_STRING);
			localIntent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
			theContext.startActivity (localIntent);	
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	private int millisecondsTillNextSecond ()
	{
		// -------------------------------------------------------------------------
		// 26/06/2013 ECU added
		// -------------------------------------------------------------------------
		Calendar currentCalendar	= Calendar.getInstance();
		int currentMilliseconds		= currentCalendar.get (Calendar.MILLISECOND);
		// -------------------------------------------------------------------------
		// 28/04/2020 ECU changed to use ONE_SECOND
		// -------------------------------------------------------------------------
		return (StaticData.ONE_SECOND - currentMilliseconds);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private int millisecondsTillNextMinute ()
	{
		// -------------------------------------------------------------------------
		// 26/06/2013 ECU added
		// -------------------------------------------------------------------------
		Calendar currentCalendar	= Calendar.getInstance();
		int currentSecond			= currentCalendar.get (Calendar.SECOND);
		int currentMilliseconds		= currentCalendar.get (Calendar.MILLISECOND);
		// -------------------------------------------------------------------------
		// 28/04/2020 ECU changed to use ONE_SECOND
		// -------------------------------------------------------------------------
		return ((60 - currentSecond) * StaticData.ONE_SECOND) - currentMilliseconds;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private void processMinuteEvents (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 22/06/2013 ECU do not check medication if the user is entering medication details
		// 04/03/2014 ECU took out the processing of medication details - now done using the
		//                alarm manager
		//
		//if (!MainActivity.gettingMedication)
		//	Utilities.processMedicationDetails(theContext);
		//
		// 06/01/2014 ECU check any appointments
		// 04/03/2014 ECU took out the processing of appointments - now done using the
		//                alarm manager
		//
		//AppointmentsActivity.ProcessAppointments (theContext);
		// -------------------------------------------------------------------------
		// 13/08/2013 ECU check if the time needs to be synchronised to time from NTP server
		// 23/11/2018 ECU only do if the system is enabled
		// -------------------------------------------------------------------------
		if (PublicData.storedData.ntpEnabled)
		{
			// ---------------------------------------------------------------------
			Utilities.refreshCurrentTime (theContext);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 08/02/2014 ECU speaking clock method
		// 23/02/2014 ECU taken out in favour of using the AlarmManager
		// -------------------------------------------------------------------------
		//Utilities.SpeakingClock ();
		// -------------------------------------------------------------------------
		// 02/02/2014 ECU synchronise files across devices
		// 25/03/2017 ECU changed to use a message to trigger the synchronisation
		//                process
		//            ECU call the method which has the message handling
		// -------------------------------------------------------------------------
		Utilities.synchroniseNow (0);
		// -------------------------------------------------------------------------
		// 25/02/2015 ECU check if any WeMo timers are to be actioned
		// -------------------------------------------------------------------------
		if (PublicData.storedData.wemoHandling)
			WeMoTimerActivity.checkTimers (theContext);
		// -------------------------------------------------------------------------
		// 21/03/2018 ECU broadcast the fact that a minute has passed
		// -------------------------------------------------------------------------
		if (StaticData.BROADCAST_ENABLED)
		{
			// ---------------------------------------------------------------------
			Intent intent = new Intent ();
			intent.setAction (StaticData.BROADCAST_TIMER_MINUTE); 
			sendBroadcast (intent);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 02/10/2020 ECU check if any listeners have been defined
	    // -------------------------------------------------------------------------
	    checkMinuteListeners ();
	    // -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	Thread processMinutesThread = new Thread()
	{
		// -------------------------------------------------------------------------
		@Override
		public void run()
		{
			try 
			{
				// -----------------------------------------------------------------
				while (!this.isInterrupted())
	            {
					// -------------------------------------------------------------
					processMinuteEvents (getBaseContext());
	                sleep (millisecondsTillNextMinute ());	
	                // -------------------------------------------------------------
	            }
	            // -----------------------------------------------------------------
			}
	        catch (InterruptedException theException)
	        {   
	        	// -----------------------------------------------------------------
				// 04/04/2014 ECU Restore interrupt flag after catching 
				//				  InterruptedException to make loop condition false
				// -----------------------------------------------------------------
				Thread.currentThread ().interrupt ();
				// -----------------------------------------------------------------
	        }       
		 }
	 };
	/* ============================================================================= */
	private void processSecondEvents (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 20/11/2013 ECU set the time that could be used later
		// -------------------------------------------------------------------------
		PublicData.dateTimeString = Utilities.setTheDateTimeString ();		
		// -------------------------------------------------------------------------
		// 26/06/2013 ECU update the status bar and alternate the icon (remember you 
		//                are working with integers not icon images.
		// 04/04/2014 ECU put in the counter so that it's not updated every second
		// 02/06/2017 ECU changed to display the current time
		// 11/11/2017 ECU changed to use new method in Utilities
		// -------------------------------------------------------------------------
		if (notificationCounter-- == 0)
		{
			// ---------------------------------------------------------------------
			// 28/04/2020 ECU changed to use TAG rather than "Timer Service"
			// ---------------------------------------------------------------------
			Utilities.notification (theContext, 
									notificationIcon,
									getString (R.string.project_name) + ":" + TAG,
									TAG,
									PublicData.dateTimeString,
									false,
									StaticData.NOTIFICATION_TIMER_SERVICE);
			// ---------------------------------------------------------------------
			notificationIcon = (R.drawable.timer_icon_on + R.drawable.timer_icon_off) - notificationIcon;
			// ---------------------------------------------------------------------
			notificationCounter = NOTIFICATION_COUNT;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 02/08/2013 ECU check if a datagram is to be sent
		// 10/01/2015 ECU changed to use the new Method
		// -------------------------------------------------------------------------
		checkDatagramIssues ();	
		// -------------------------------------------------------------------------
		// 03/08/2013 ECU check if a music track is being played
		// 10/01/2015 ECU changed to use the new Method
		// -------------------------------------------------------------------------
		checkMusicIssues ();	
		// -------------------------------------------------------------------------
		// 06/08/2013 ECU check whether audio streaming is to be started
		// 10/01/2015 ECU changed to use the new Method
		// -------------------------------------------------------------------------
		checkStreamingIssues (theContext);
		// -------------------------------------------------------------------------
		// 04/07/2013 ECU show any status messages if in debug mode
		// -------------------------------------------------------------------------
		// 07/09/2013 ECU check if any bluetooth messages to process
		// 21/09/2013 ECU put in the 'null' check
		// 25/01/2015 ECU bluetooth action taken out
		// -------------------------------------------------------------------------
		//if (MainActivity.bluetoothUtilities != null)
		//	MainActivity.bluetoothUtilities.ActionToTake (theContext);
		// -------------------------------------------------------------------------
		// 28/11/2014 ECU check if a delayed email message is to be sent
		// 05/01/2015 ECU put in the check on null
		// -------------------------------------------------------------------------
		if (PublicData.emailDetails != null)
			PublicData.emailDetails.TimedEmail (theContext);
		// -------------------------------------------------------------------------
		// 06/03/2015 ECU call up the light level monitor to check on periods of
		//                darkness which may require a warning to be displayed
		// 26/10/2015 ECU put in the check on whether the storedData has been
		//                initialised correctly
		// 08/05/2020 ECU changed to use 'Check...'
		// -------------------------------------------------------------------------
		if (StoredData.CheckIfInitialised ())
		{
			// ---------------------------------------------------------------------
			Utilities.checkLightLevel (theContext);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 22/03/2015 ECU check if a request for details has to be requested
		// -------------------------------------------------------------------------
		if (PublicData.requestAddress != null)
		{
			int localSecond = Calendar.getInstance().get(Calendar.SECOND);
			// ---------------------------------------------------------------------
			// 22/03/2015 ECU check if time to process the request
			// ---------------------------------------------------------------------
			if (localSecond >= PublicData.requestSecond)
			{
				// -----------------------------------------------------------------
				// 22/03/2015 ECU send the message to a device to request its details
				// -----------------------------------------------------------------
				Utilities.sendDatagramType (theContext,PublicData.requestAddress,StaticData.SOCKET_MESSAGE_REQUEST_DETAILS);
				// -----------------------------------------------------------------
				// 22/03/2015 ECU log the fact that request message has been sent
				// -----------------------------------------------------------------
				Utilities.LogToProjectFile (TAG,"REQUEST_DETAILS message sent to " + PublicData.requestAddress);
				// -----------------------------------------------------------------
				// 22/03/2015 ECU indicate that processing has been done
				// -----------------------------------------------------------------
				PublicData.requestAddress = null;
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 13/07/2016 ECU check if there have been any notifications to tell the user
		//                about
		//            ECU put in check on user interface
		// -------------------------------------------------------------------------
		if (PublicData.userInterfaceRunning)
			NotificationMessage.Check ();
		// -------------------------------------------------------------------------
		// 21/03/2018 ECU broadcast the fact that a second has passed
		// -------------------------------------------------------------------------
		if (StaticData.BROADCAST_ENABLED)
		{
			// ---------------------------------------------------------------------
			Intent intent = new Intent ();
			intent.setAction (StaticData.BROADCAST_TIMER_SECOND); 
			sendBroadcast (intent);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	Thread processSecondsThread = new Thread()
	{
		// -------------------------------------------------------------------------
		@Override
		public void run()
		{
			try 
			{
				// -----------------------------------------------------------------
				while (!this.isInterrupted())
	            {
					// -------------------------------------------------------------
					// 20/06/2013 ECU process any events on each second
					// -------------------------------------------------------------
					processSecondEvents (getBaseContext());
					// -------------------------------------------------------------
					// 26/06/2013 ECU if we are to keep running then wait till next second
					// 22/02/2014 ECU if the screen is off then have a longer delay
					//                 screen on .... next second
					//                 screen off ... next second + (SCREEN_OFF_DELAY)
					// -------------------------------------------------------------	
					sleep (millisecondsTillNextSecond() + (powerManager.isScreenOn() ? 0 : SCREEN_OFF_DELAY));
					// -------------------------------------------------------------
	            }
			}
	        catch (InterruptedException theException)
	        {  
	        	// -----------------------------------------------------------------
				// 04/04/2014 ECU Restore interrupt flag after catching 
				//				  InterruptedException to make loop condition false
				// -----------------------------------------------------------------
				Thread.currentThread ().interrupt ();
				// -----------------------------------------------------------------
	        }       
		 }
	};
	// =============================================================================
	public static void removeMinuteListener (ListenerMethod theListenerMethod)
	{
		// -------------------------------------------------------------------------
		// 02/10/2020 ECU created to remove the specified listener
		// -------------------------------------------------------------------------
		minuteListeners.remove (theListenerMethod);
		//--------------------------------------------------------------------------
	}
	// =============================================================================
}

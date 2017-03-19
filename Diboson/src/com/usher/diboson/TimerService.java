package com.usher.diboson;

import java.util.Calendar;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

public class TimerService extends Service
{
	/* ============================================================================= */
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
	// =============================================================================
	// Tested
	// ======
	/* ============================================================================= */
	final static String TAG = "TimerService";
	/* ============================================================================= */
	// 22/02/2014 ECU if the device is in standby mode then slow down the second handler
	//                by adding in SCREEN_OFF_DELAY when putting the handler into 'sleep'
	// -----------------------------------------------------------------------------
	public final static int		SCREEN_OFF_DELAY	= (10 * 1000); 	// 22/02/2014 added
	/* ============================================================================= */
	private final static int	NOTIFICATION_COUNT	= 30;			// 04/04/2014 ECU added
																	// 09/01/2015 ECU changed from 10
	private static final boolean
								WAKELOCK_ENABLED = false;			// 03/05/2015 ECU moved here from variable
	/* ============================================================================= */
	int 						datagramCounter 	= 0;			// 02/08/2013 ECU added
	int 						notificationIcon 	= R.drawable.timer_icon_on;
	int							notificationCounter = NOTIFICATION_COUNT;
	NotificationManager 		notificationManager;	
	PowerManager				powerManager;						// 22/02/2014 ECU added
	int 						secondCounter 		= 0;
	PowerManager.WakeLock 		wakeLock;							// 12/02/2014 ECU added 
	/* ============================================================================= */
	@Override
	public IBinder onBind(Intent arg0) 
	{	
		return null;
	}
	/* ============================================================================= */
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
		// -------------------------------------------------------------------------
		Utilities.debugPopToast ("Timer Service has been Created"); 
		// -------------------------------------------------------------------------
		// 26/06/2013 ECU add NotificationManager
		// -------------------------------------------------------------------------
		notificationManager 
			= (NotificationManager) this.getSystemService (NOTIFICATION_SERVICE);
		// -------------------------------------------------------------------------
		// 22/02/2014 ECU set up the power manager which will be used for monitoring
		//                the screen
		// -------------------------------------------------------------------------
		powerManager = (PowerManager) getSystemService(POWER_SERVICE);
	}
	/* ============================================================================= */
	@Override
	public int onStartCommand (Intent intent,int flags, int startId) 
	{
		// -------------------------------------------------------------------------
		// 23/06/2013 ECU put in debug mode
		// 10/01/2015 ECU changed to use the new Method
		// -------------------------------------------------------------------------
		Utilities.debugPopToast ("Timer Service has started");
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
	    	// ---------------------------------------------------------------------
	    	wakeLock.acquire();
	    }
	    // -------------------------------------------------------------------------
	    // 12/02/2014 ECU return added 
	    // -------------------------------------------------------------------------
	    return Service.START_STICKY;
	}
	/* ============================================================================= */
	@Override
	public void onDestroy() 
	{
		// -------------------------------------------------------------------------
		// 23/06/2013 ECU put in debug mode
		// 10/01/2015 ECU changed to use the new Method
		// -------------------------------------------------------------------------
		Utilities.debugPopToast ("Timer Service has been Destroyed"); 
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
			wakeLock.release();
		// -------------------------------------------------------------------------
		super.onDestroy();
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
					Utilities.sendSocketMessageSendTheObject
						(getBaseContext(),
						 PublicData.datagramReceiver,
						 PublicData.socketNumberForData,
						 StaticData.SOCKET_MESSAGE_DATAGRAM,
						 PublicData.datagram);
				}
				else
				{
					// -------------------------------------------------------------
					// 31/01/2015 ECU change the name of the method called
					// 21/03/2015 ECU pass through the message type as an argument
					// -------------------------------------------------------------
					Utilities.sendSocketMessageSendObjectToAllDevices
						(getBaseContext(),
						 PublicData.deviceDetails,
						 PublicData.socketNumberForData,
						 StaticData.SOCKET_MESSAGE_DATAGRAM,
						 PublicData.datagram);
				}
				// -----------------------------------------------------------------
			}
			else
			if (PublicData.datagramType == StaticData.SOCKET_MESSAGE_DATAGRAM_ACTION)
				Utilities.sendDatagram (getBaseContext(), 
										PublicData.datagramIPAddress,
										StaticData.SOCKET_MESSAGE_DATAGRAM_ACTION,
										PublicData.datagram);		
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
			
			Utilities.actionDatagram (getBaseContext());
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void checkMusicIssues ()
	{
		// -------------------------------------------------------------------------
		// 10/01/2015 ECU created to include the code that checks for various
		//                music issues
		// -------------------------------------------------------------------------
		if (PublicData.trackBeingPlayed)
		{
			// ---------------------------------------------------------------------
			// 09/08/2013 ECU added the check on ...mediaPlayer != null
			// ---------------------------------------------------------------------
			if (PublicData.mediaPlayer != null && !PublicData.mediaPlayer.isPlaying())
			{
				// -----------------------------------------------------------------
				// 12/08/2013 ECU include the track counter in the message
				// 08/11/2013 ECU use the custom toast
				// -----------------------------------------------------------------
				Utilities.popToast ("Track " + PublicData.remoteTrackCounter + 
						" has finished playing " + PublicData.musicServer);
				// -----------------------------------------------------------------
				// 13/08/2013 ECU check if the next file is not still being received
				// -----------------------------------------------------------------
				if (!PublicData.receivingFile)
				{
					// -------------------------------------------------------------
					// 03/08/2013 ECU it would appear that the music has stopped playing
					// -------------------------------------------------------------
					PublicData.trackBeingPlayed = false;
					// -------------------------------------------------------------
					// 03/08/2013 ECU tell the server about this
					// -------------------------------------------------------------
					Utilities.sendDatagramType (getBaseContext(),PublicData.musicServer,
							StaticData.SOCKET_MESSAGE_PLAYED);
					// -------------------------------------------------------------
					// 05/08/2013 ECU added the debug message call
					// -------------------------------------------------------------
					Utilities.debugMessage (TAG,"Track has Finished");
					// -------------------------------------------------------------
					// 13/04/2015 ECU at this point if there is track information
					//                being displayed then clear it
					// -------------------------------------------------------------
					MusicPlayer.setMarqueeText (null);
					// -------------------------------------------------------------
					// 02/05/2015 ECU indicate that the music player is now available
					//            ECU change to use method
					// -------------------------------------------------------------
					MusicPlayer.setStatus (false);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 13/08/2013 ECU the next file to be played is still being transmitted so wait
					// 08/11/2013 ECU use the custom toast
					// -------------------------------------------------------------
					Utilities.popToast ("waiting for the next file to be received");	
				}
			}
		}
		// -------------------------------------------------------------------------
		// 20/08/2013 ECU check if music player timeout is to be handled
		// -------------------------------------------------------------------------
		if (PublicData.playTimeout != StaticData.NO_RESULT)
		{	
			Utilities.debugMessage (TAG, "player timeout running " + PublicData.playTimeout);
			
			if (--PublicData.playTimeout == 0)
			{
				Utilities.debugMessage (TAG, "player timeout has occurred");
				// -----------------------------------------------------------------
				// 20/08/2013 ECU try and prompt the server by sending a PLAYED socket message
				// -----------------------------------------------------------------
				Utilities.sendDatagramType (getBaseContext(),PublicData.musicServer,
												StaticData.SOCKET_MESSAGE_PLAYED);
				// -----------------------------------------------------------------
				// 20/08/2013 ECU indicate no more handling is required
				// -----------------------------------------------------------------
				PublicData.playTimeout = StaticData.NO_RESULT;
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
			// ---------------------------------------------------------------------
			Intent localIntent = new Intent (theContext,AudioStreamActivity.class);
			localIntent.putExtra (StaticData.PARAMETER_REMOTE,"");
			localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			theContext.startActivity (localIntent);	
		}
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
		return (1000 - currentMilliseconds);
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
		return ((60 - currentSecond) * 1000) - currentMilliseconds;
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
		// -------------------------------------------------------------------------
		Utilities.refreshCurrentTime (theContext);
		// -------------------------------------------------------------------------
		// 08/02/2014 ECU speaking clock method
		// 23/02/2014 ECU taken out in favour of using the AlarmManager
		// -------------------------------------------------------------------------
		//Utilities.SpeakingClock ();
		// -------------------------------------------------------------------------
		// 02/02/2014 ECU synchronise files across devices
		// -------------------------------------------------------------------------
		Utilities.SynchroniseFiles (theContext);
		// -------------------------------------------------------------------------
		// 19/11/2014 ECU check whether the monitor service is to start or stop
		// -------------------------------------------------------------------------
		PublicData.storedData.monitor.checkTime (theContext);
		// -------------------------------------------------------------------------
		// 25/02/2015 ECU check if any WeMo timers are to be actioned
		// -------------------------------------------------------------------------
		if (PublicData.storedData.wemoHandling)
			WeMoTimerActivity.checkTimers (theContext);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	Thread processMinutesThread = new Thread()
	{
		@Override
		public void run()
		{
			try 
			{          	
				while (!this.isInterrupted())
	            {
					processMinuteEvents (getBaseContext());
	                sleep (millisecondsTillNextMinute ());	
	            }
			}
	        catch(InterruptedException theException)
	        {   
	        	// -----------------------------------------------------------------
				// 04/04/2014 ECU Restore interrupt flag after catching 
				//				  InterruptedException to make loop condition false
				// -----------------------------------------------------------------
				Thread.currentThread().interrupt();
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
		// -------------------------------------------------------------------------
		if (notificationCounter-- == 0)
		{
			updateStatusBar (notificationIcon,
							 getString (R.string.project_name) + ":Timer Service",
							 "Timer Service",
							 "Timer Event has occurred");
		
			notificationIcon = (R.drawable.timer_icon_on + R.drawable.timer_icon_off) - notificationIcon;
			
			notificationCounter = NOTIFICATION_COUNT;
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
		// -------------------------------------------------------------------------
		if (PublicData.storedData.initialised)
		{
			Utilities.checkLightLevel (theContext);
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
	}
	/* ============================================================================= */
	Thread processSecondsThread = new Thread()
	{
		@Override
		public void run()
		{
			try 
			{          	
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
	        catch(InterruptedException theException)
	        {  
	        	// -----------------------------------------------------------------
				// 04/04/2014 ECU Restore interrupt flag after catching 
				//				  InterruptedException to make loop condition false
				// -----------------------------------------------------------------
				Thread.currentThread().interrupt();
	        }       
		 }
	};
	/* ============================================================================= */
	private void updateStatusBar (int theIcon,
								  String theTickerText,
								  String theContentTitle,
								  String theContentText)
	{
		// -------------------------------------------------------------------------
		// 26/06/2013 ECU set up the notification mechanisms
		// 28/07/2016 ECU changed to use the NotificationCompat builder because
		//                'setLatestEventInfo' is not available from Marshmallow
		// -------------------------------------------------------------------------
		Context theContext 			= getApplicationContext();
        // -------------------------------------------------------------------------
        // 28/07/2016 ECU set the activity that will be activated when the
        //                notification is 'clicked'
        // --------------------------------------------------------------------------
        Intent notificationIntent = new Intent (theContext,TimerEventActivity.class);
        PendingIntent contentIntent 
        		= PendingIntent.getActivity (theContext, 0, notificationIntent, 0);
        // -------------------------------------------------------------------------
        // 28/07/2016 ECU changed with the move to MARSHMALLOW
        // -------------------------------------------------------------------------
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder (theContext)
        						.setWhen (System.currentTimeMillis())
        						.setContentText (theContentText)
        						.setContentTitle (theContentTitle)
        						.setSmallIcon (theIcon)
        						.setAutoCancel (true)
        						.setTicker (theTickerText)
        						.setContentIntent (contentIntent);
	    // -------------------------------------------------------------------------
        // 28/07/2016 ECU tell the manager about this notification
        // -------------------------------------------------------------------------
        notificationManager.notify (StaticData.NOTIFICATION_TIMER_SERVICE,notificationBuilder.build());
		// -------------------------------------------------------------------------
		// 12/02/2014 ECU try and start this service in the foreground
		// -------------------------------------------------------------------------
		// 12/02/2014 ECU took the following out because does not appear to be 
		//                    necessary and because the id's are different then 
		//                    get two entries
		//startForeground(310117,updateData);
		// -------------------------------------------------------------------------			      
	}
	/* ============================================================================= */
}

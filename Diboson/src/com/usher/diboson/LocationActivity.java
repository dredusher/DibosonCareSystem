package com.usher.diboson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class LocationActivity extends DibosonActivity implements OnGestureListener
{
	// =============================================================================
	// 03/01/2014 ECU created
	//            ECU IMPORTANT if device is not receiving any location
	//                ========= updates then check the 'location' option
	//                          in settings to ensure that you can use
	//                          WiFi for obtaining location.
	// 20/10/2014 ECU check GridActivity.activityCounter to see if the
	//                activity has restarted itself
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 02/11/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 08/02/2016 ECU if the location services have to be enabled to provide the
	//                tracking facility then when the BACK key is pressed then give
	//                the user the option to disable the service so as to conserve
	//                the battery
	// 26/02/2016 ECU changed to use the 'trackingDetails' that is stored on disk
	// 20/02/2017 ECU added the use of the TrackingDetails class to store data
	//                that has previously been written to disk
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	//==============================================================================
	
	/* ============================================================================= */
	private static final int 			MENU_ALWAYS_PLAY    = 0;
	private static final int			MENU_LOG			= 1;	// 09/02/2016 ECU added
	private static final int			MENU_MAP			= 2;	// 23/10/2014 ECU added
	private static final int 			MENU_TRACK_MODE	    = 3;
	// =============================================================================
	private static final String			KML_HEADER			= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
															  "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n" +
															  "<Folder>\n" +
															  "<Style id=\"track_line\">\n" +
															  "<LineStyle>\n" +
															  "<width>5</width>\n" +
															  "<color>ff33ccff</color>\n" +
															  "</LineStyle>\n" +
															  "</Style>\n" +
															  "<Placemark>\n" +
															  "<styleUrl>#track_line</styleUrl>\n" +
															  "<name>Ed Usher - Track</name>\n" +
															  "<gx:Track>\n";
																					// 23/02/2016 ECU added the style
	private static final String			KML_TAIL			= "</gx:Track>\n" +
															  "</Placemark>\n" +
															  "</Folder>\n" +
															  "</kml>\n";
	private static final double		    MILES_PER_KILOMETRE	= 0.621371;
																	// 23/02/2016 ECU miles per kilometre
	private static final int			MINIMUM_DISTANCE	= 5;	// 19/01/2014 ECU minimum distance in
																	//                metres between updates
																	// 12/02/2016 ECU changed to 5 from 1
	private static final int		    MINIMUM_TIME		= 10000;// 19/01/2014 ECU minimum time in milliseconds
																	//                between updates
	private static final String			TIMESTAMP			="yyyy-MM-dd'T'HH:mm:ss'Z'";
																	// 09/02/2016 ECU added
	/* ============================================================================= */
	public static	boolean     	    alwaysPlay = false;
	public static	double          	currentLongitude;
	public static 	double          	currentLatitude;
	public static	double          	currentAltitude;
	public static 	String		 		currentProvider;			// 02/01/2014 ECU added - provider of the location
	public static   ImageView           imageView;					// 20/02/2017 ECU added
	// -----------------------------------------------------------------------------
	// 20/02/2017 ECU the next two string must NOT be initialised to null
	// -----------------------------------------------------------------------------
	public static   String				lastImageActioned	= "";	// 20/02/2017 ECU added
	public static   String				lastNoteActioned	= "";	// 20/02/2017 ECU added
	// -----------------------------------------------------------------------------
	public static	double         		previousLongitude 	= 0;
	public static 	double          	previousLatitude 	= 0;
	public static 	double          	previousAltitude 	= 0;
	public static 	int          		track_accuracy 		= 1000000;
	public static   List<TrackingData>
										trackingData		= null;	// 20/02/2017 ECU added
	/* ============================================================================= */
		   static	long				adjustedTime;				// 21/10/2014 ECU added
		   static 	Context				context;					// 09/02/2016 ECU added
		   static   double				distance;					// 11/02/2016 ECU added
		   static   boolean				finishFlag = false;			// 10/02/2016 ECU added
					GestureDetector		gestureScanner = null;		// 21/10/2014 ECU preset to null
		   static	boolean             keepTimerHandlerRunning = true;
																	// 20/10/2014 ECU added
		   static   double				lastTrackLatitude	= StaticData.NO_RESULT;
		   															// 11/02/2016 ECU added
		   static   double				lastTrackLongitude	= StaticData.NO_RESULT;
		   															// 11/02/2016 ECU added
		   static	TextView   			locationDetails;			// 02/01/2014 ECU added
					LocationListener 	locationListener;			// 23/08/2013 ECU added - was declared locally
					LocationManager  	locationManager = null;		// 23/08/2013 ECU added - was declared locally
					ImageView		  	locationImage;				// 02/01/2014 ECU added
		   static 	boolean				logToFileTime = false;		// 09/02/2016 ECU added
		         	boolean				remoteMode = false;			// 21/10/2014 ECU added
		   static   String				senderDeviceDetails;		// 20/10/2014 ECU added
		   static	SimpleDateFormat	timestampFormat;			// 09/02/2016 ECU added
		   static   TextView        	updatedCoordinate;
	/* ============================================================================= */
		   static   TimerHandler 		timerHandler	= new TimerHandler();
	/* ============================================================================= */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 02/11/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 28/11/2016 ECU call method to set the screen up
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true,true,false);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_location);
			// ---------------------------------------------------------------------
			// 09/02/2016 ECU remember the context for future use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 26/02/2016 ECU initialise the stored tracking details which should only
			//                happen during development
			// ---------------------------------------------------------------------
			if (PublicData.storedData.trackingDetails == null)
			{
				PublicData.storedData.trackingDetails = new TrackingDetails ();
			}
			// ---------------------------------------------------------------------
			// 20/02/2017 ECU set up the imageview for displaying photographs
			// ---------------------------------------------------------------------
			imageView = (ImageView) findViewById (R.id.trackingPhoto);
			// ---------------------------------------------------------------------
			// 20/02/2017 ECU set up the click listener for the imageview
			// ---------------------------------------------------------------------
		 	imageView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick (View view) 
				{	
					// -------------------------------------------------------------
					// 23/03/2016 ECU try and define the action commands
					// -------------------------------------------------------------
					imageView.setVisibility (View.GONE);
					// -------------------------------------------------------------
				}
			});
			// ---------------------------------------------------------------------
			// 26/02/2016 ECU check whether tracking was already running when this
			//                activity was started which could happen if the start
			//                is after a 'destroy' by the Android OS
			// ---------------------------------------------------------------------
			if (PublicData.storedData.trackingDetails.enabled)
			{
				// -----------------------------------------------------------------
				// 26/02/2016 ECU logging to file is already enabled
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (getString (R.string.continuing_tracking));
				// -----------------------------------------------------------------
				// 26/02/2016 ECU indicate that data can be written to disk
				// -----------------------------------------------------------------
				logToFileTime = true;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 09/02/2015 ECU set up the timestamp format
			// ---------------------------------------------------------------------
			timestampFormat = new SimpleDateFormat (TIMESTAMP, Locale.getDefault());
			// ---------------------------------------------------------------------
			// 20/10/2014 ECU increment the activity counter to check for self restarting
			// ---------------------------------------------------------------------
			GridActivity.activityCounter++;
			// ---------------------------------------------------------------------
			// 03/01/2014 ECU add in gesture handling
			// ---------------------------------------------------------------------
			gestureScanner = new GestureDetector (LocationActivity.this,this);
			// ---------------------------------------------------------------------
			// 03/01/2014 ECU initialise the location issues
			// ---------------------------------------------------------------------
			if (!InitialiseGPS (true))
			{	
				// -----------------------------------------------------------------
				// 20/10/2014 ECU check to see the activity has already been restarted
				//                once in which case put the activity into a mode
				//                whereby it will monitor remote devices
				// -----------------------------------------------------------------
				if (GridActivity.activityCounter == 1)
				{
					// -------------------------------------------------------------
					// 18/10/2014 ECU unable to use location services so terminate
					// 21/02/2017 ECU changed to use resources
					// -------------------------------------------------------------
					Utilities.SpeakAPhrase (this,new String [] {getString (R.string.location_services),
																getString (R.string.location_remote_device)});
					// -------------------------------------------------------------
					//  18/10/2014 ECU activate settings so that user can enable location
					//                 services
					// -------------------------------------------------------------
					Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivityForResult (settingsIntent,StaticData.REQUEST_CODE_SETTINGS);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 20/10/2014 ECU the user has not enabled location services so
					//                just monitor remote devices
					// 21/02/2017 ECU changed to use resource
					// -------------------------------------------------------------
					Utilities.SpeakAPhrase (this,getString (R.string.location_chosen_remote));
					// -------------------------------------------------------------
					// 21/10/2014 ECU indicate that in remote monitoring mode
					// -------------------------------------------------------------
					remoteMode = true;
					// -------------------------------------------------------------
					// 20/10/2014 ECU initialise without checking for location services
					// -------------------------------------------------------------
					InitialiseGPS (false);
					// -------------------------------------------------------------
					// 09/02/2016 ECU because variable was made static and the
					//                activity has been restarted then need to indicate
					//                that the thread needs to keep running
					// -------------------------------------------------------------
					keepTimerHandlerRunning = true;
					// -------------------------------------------------------------
					// 20/10/2014 ECU start up the monitoring thread
					// 10/02/2016 ECU changed to use REMOTE_MONITOR
					// -------------------------------------------------------------
					timerHandler.sleep (StaticData.MESSAGE_REMOTE_MONITOR,10000);
					// -------------------------------------------------------------
				}
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/11/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	@Override
	public void onActivityResult(int theRequestCode, int theResultCode, Intent theIntent) 
	{
		// -------------------------------------------------------------------------
		// 30/08/2013 ECU called when an activity returns a result
		// -------------------------------------------------------------------------
		// 07/02/2014 ECU removed bar code code and placed in BarCodeActivity
		// -------------------------------------------------------------------------
	    if (theRequestCode == StaticData.REQUEST_CODE_SETTINGS)
	    {
	    	// ---------------------------------------------------------------------
	    	// 18/10/2014 ECU have return from settings so restart the current
	    	//                activity after finishing this one
	    	// ---------------------------------------------------------------------
	    	finish();
	    	startActivity (getIntent());
	    	// ---------------------------------------------------------------------
	    }
	}
	// =============================================================================
	@Override
	public void onBackPressed ()
	{
		// -------------------------------------------------------------------------
		// 08/02/2016 ECU created to handle the BACK key

		// -------------------------------------------------------------------------
		// 09/02/2016 ECU if not in remote mode then handle location services
		// -------------------------------------------------------------------------
		if (!remoteMode)
		{
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (getString (R.string.location_services_state),true);
			// ---------------------------------------------------------------------
			// 08/02/2016 ECU activate settings so that user can decide whether to
			//                leave location services or not
			// ---------------------------------------------------------------------
			Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity (settingsIntent);
			// ---------------------------------------------------------------------
			// 09/02/2016 ECU check whether details are being logged to file
			//            ECU the false indicates not to send an email
			// 10/02/2016 ECU changed the meaning of the flag to 'true' to indicate
			//                that the activity is to be 'finished' at the end of
			//                the dialogue
			// 26/02/2016 ECU changed to use the stored value
			// ---------------------------------------------------------------------
			if (PublicData.storedData.trackingDetails.enabled)
				DisableLogToFile (true);
			else
			{
				// -----------------------------------------------------------------
				// 08/02/2016 ECU now call the super for this method
				// -----------------------------------------------------------------
				super.onBackPressed();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// --------------------------------------------------------------------
			// 20/10/2014 ECU indicate that the timer handler should stop
			// --------------------------------------------------------------------
			keepTimerHandlerRunning = false;
			// --------------------------------------------------------------------
			// 08/02/2016 ECU now call the super for this method
			// --------------------------------------------------------------------
			super.onBackPressed();
			// --------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// -------------------------------------------------------------------------
		// Inflate the menu; this adds items to the action bar if it is present.
		// 09/02/2016 ECU added the MENU_LOG option
		// 26/02/2016 ECU changed to use the stored value
		// -------------------------------------------------------------------------
		menu.add (0,MENU_ALWAYS_PLAY,0,R.string.menu_always_play);
		menu.add (0,MENU_TRACK_MODE,0,PublicData.trackingMode ? R.string.tracking_mode_off : R.string.tracking_mode_on);
		menu.add (0,MENU_MAP,0,"Display Map");
		menu.add (0,MENU_LOG,0,PublicData.storedData.trackingDetails.enabled ? R.string.track_log_off : R.string.track_log_on);
		// -------------------------------------------------------------------------
		return true;
	}
	/* ============================================================================= */
	@Override
    public void onDestroy()
    {	
		// -------------------------------------------------------------------------
		// 23/08/2013 ECU remove the updates to the locationListener
		// 18/10/2014 ECU included the check on null
		// -------------------------------------------------------------------------
		if (locationManager != null)
			locationManager.removeUpdates(locationListener);
		// -------------------------------------------------------------------------
		// 27/05/2013 ECU get the main method processed
		// -------------------------------------------------------------------------
        super.onDestroy();
    }
	/* ============================================================================= */
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) 
	{
		// -------------------------------------------------------------------------
		// 28/05/2013 ECU clear the displayed menu
		// -------------------------------------------------------------------------
		menu.clear ();
		// -------------------------------------------------------------------------
		// 05/06/2013 ECU used the method to build menu
		// -------------------------------------------------------------------------	
		return onCreateOptionsMenu(menu);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public boolean onOptionsItemSelected (MenuItem item)
	{
		// -------------------------------------------------------------------------
		// 16/06/2013 ECU take the actions depending on which menu is selected
		// -------------------------------------------------------------------------
		switch (item.getItemId())
		{
			// ---------------------------------------------------------------------
			case MENU_ALWAYS_PLAY:
				LocationActivity.alwaysPlay = ! LocationActivity.alwaysPlay;
				return true;
			// ---------------------------------------------------------------------
			case MENU_LOG:
				// -----------------------------------------------------------------
				// 09/02/2016 ECU handle the logging of details to a file
				//            ECU don't toggle the flag at the start because do
				//                not want logging to start until everything has been
				//                set up
				// 26/02/2016 ECU changed to use the stored value
				// -----------------------------------------------------------------
				if (!PublicData.storedData.trackingDetails.enabled)
				{
					// -------------------------------------------------------------
					// 09/02/2016 ECU request the file where the data is to be
					//                logged
					// -------------------------------------------------------------
					DialogueUtilities.textInput (context,
		   					 context.getString (R.string.track_file),
		   					 context.getString (R.string.enter_track_file),
		   					 StaticData.HINT + context.getString (R.string.type_in_track_file_name),
		   					 Utilities.createAMethod (LocationActivity.class,"SetFileNameMethod",""),
		   					 null);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 09/02/2016 ECU indicate that logging is to stop
					//            ECU changed to use a common method
					//            ECU include the true flag to indicate email option
					// 10/02/2016 ECU changed the meaning of flag to be whether activity
					//                is to be finished
					// -------------------------------------------------------------
					DisableLogToFile (false);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				return true;
			// ---------------------------------------------------------------------
			case MENU_MAP:
				Utilities.displayMap (getBaseContext(), currentLatitude, currentLongitude);
				return true;
			// ---------------------------------------------------------------------
			case MENU_TRACK_MODE:
				// -----------------------------------------------------------------
				// 03/01/2014 ECU toggle the tracking mode
				// -----------------------------------------------------------------
				PublicData.trackingMode = !PublicData.trackingMode;
				
				if (PublicData.trackingMode)
				{
					// -------------------------------------------------------------
					// 22/09/2013 ECU build up location details from the
					//                files in the tracking folder
					// -------------------------------------------------------------
					Utilities.scanFilesInFolder (getBaseContext(),PublicData.trackFolder);
					// -------------------------------------------------------------
				}
				return true;
		}
		// -------------------------------------------------------------------------
		return true;
	}
	/* ============================================================================= */
	@Override
	public boolean onTouchEvent (MotionEvent motionEvent) 
	{
		return gestureScanner.onTouchEvent(motionEvent);
	}
	/* ============================================================================= */
	@Override
	public boolean onDown (MotionEvent motionEvent) 
	{
		return false;
	}
	/* ============================================================================= */
	@Override
	public boolean onFling (MotionEvent theMotionEvent1, MotionEvent theMotionEvent2, 
							float velocityX,float velocityY) 
	{
		return false;
	}
	/* ============================================================================= */
	@Override
	public void onLongPress (MotionEvent motionEvent) 
	{
	
	}
	/* ============================================================================= */
	@Override
	public boolean onScroll (MotionEvent theMotionEvent1, MotionEvent theMotionEvent2,
							 float distanceX,float distanceY) 
	{	
		return false;
	}
	/* ============================================================================= */
	@Override
	public void onShowPress (MotionEvent motionEvent) 
	{
	}
	/* ============================================================================= */
	@Override
	public boolean onSingleTapUp (MotionEvent motionEvent) 
	{
		// -------------------------------------------------------------------------
		// 21/10/2014 ECU can only start tracking if the device has location
		//                services enabled
		// -------------------------------------------------------------------------
		if (!remoteMode)
		{
			// ---------------------------------------------------------------------
			// 03/01/2014 ECU start the actually tracking task
			// ---------------------------------------------------------------------
			Intent myIntent = new Intent (getBaseContext(),Track.class);
			startActivityForResult (myIntent,0); 
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 21/10/2014 ECU tell the user that tracking cannot be started
			// 08/02/2016 ECU changed to use the string resource
			// ---------------------------------------------------------------------
			Utilities.SpeakAPhrase (getBaseContext(),getString (R.string.cannot_start_tracking));
			// ---------------------------------------------------------------------
		}
		return false;
	}
	// =============================================================================
	static void AppendToKMLFile ()
	{
		// -------------------------------------------------------------------------
		// 22/11/2014 ECU called to clear the Project log
		// -------------------------------------------------------------------------
		(new File(PublicData.storedData.trackingDetails.fileName)).delete(); 
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	static void AppendToKMLFile (double theLongitude,double theLatitude,double theAltitude)
	{
		// -------------------------------------------------------------------------
		// 09/02/2015 ECU provide a throttle on the rate at which data is written to 
		//                file
		// --------------------------------------------------------------------------
		if (logToFileTime)
		{
			// ----------------------------------------------------------------------
			// 09/02/2016 ECU set up the 'when' entry
			// ----------------------------------------------------------------------
			Utilities.AppendToFile(PublicData.storedData.trackingDetails.fileName, "<when>" + timestampFormat.format(adjustedTime) +  "</when>\n");
			// ----------------------------------------------------------------------
			Utilities.AppendToFile (PublicData.storedData.trackingDetails.fileName,
					"<gx:coord>" + theLongitude + " " + theLatitude + ((theAltitude == StaticData.NO_RESULT) ? "" : (" " + theAltitude)) + "</gx:coord>\n");
			// ---------------------------------------------------------------------
			// 11/02/2016 ECU work out the distance since the last update
			// ---------------------------------------------------------------------
			if (lastTrackLatitude != StaticData.NO_RESULT)
			{
				distance += DistanceUsingHaversine (lastTrackLatitude,lastTrackLongitude,theLatitude,theLongitude);
				Utilities.AppendToFile(PublicData.storedData.trackingDetails.fileName, "<description>" + distanceAsString (distance) +  "</description>\n");
			}
			// ---------------------------------------------------------------------
			// 11/02/2016 ECU remember this position
			// ---------------------------------------------------------------------
			lastTrackLatitude  = theLatitude;
			lastTrackLongitude = theLongitude;
			// ---------------------------------------------------------------------
			// 09/02/2016 ECU want to throttle back on the rate written to disk
			//            ECU set flag to 'false' to stop further writing until
			//                the timer has occurred
			// ---------------------------------------------------------------------
			if (PublicData.storedData.trackingDetails.logToFileTimer > 0)
			{
				logToFileTime = false;
				// -----------------------------------------------------------------
				// 09/02/2016 ECU now start the timer
				// -----------------------------------------------------------------
				timerHandler.sleep (StaticData.MESSAGE_RECEIVE,PublicData.storedData.trackingDetails.logToFileTimer*1000);
				// -----------------------------------------------------------------
			}
		}
	}
	// -----------------------------------------------------------------------------
	static void AppendToKMLFile (String theMessage)
	{
		Utilities.AppendToFile (PublicData.storedData.trackingDetails.fileName,theMessage);
	}
	// =============================================================================
	void DisableLogToFile (boolean theFinishFlag)
	{
		// -------------------------------------------------------------------------
		// 09/02/2016 ECU indicate that logging is to stop
		//            ECU include the email flag
		// 10/02/2016 ECU changed the flag to indicate that the activity is to finish
		// 26/02/2016 ECU changed to use the stored value
		// -------------------------------------------------------------------------
		PublicData.storedData.trackingDetails.enabled = false;
		// -------------------------------------------------------------------------
		// 08/02/2016 ECU write to the KML file
		// -------------------------------------------------------------------------
		AppendToKMLFile (KML_TAIL);
		// -------------------------------------------------------------------------
		// 10/02/2016 ECU store whether the activity is to be finished at the end
		// -------------------------------------------------------------------------
		finishFlag = theFinishFlag;
		// -------------------------------------------------------------------------
		// 09/02/2016 ECU ask whether the track is to be emailed
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (context,  
								 context.getString(R.string.track_email_title),
								 context.getString(R.string.track_email_summary),
								 null,
								 Utilities.createAMethod (LocationActivity.class,"SendEmailMethod",(Object) null),
								 Utilities.createAMethod (LocationActivity.class,"DoNotSendEmailMethod",(Object) null));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static String distanceAsString (double theDistanceInKilometres)
	{
		// -------------------------------------------------------------------------
		// 23/02/2016 ECU created to return the distance as km and miles
		// -------------------------------------------------------------------------
		return "Distance = " + String.format ("%.4f",theDistanceInKilometres) + " kM " +
							   String.format ("%.4f",(theDistanceInKilometres * MILES_PER_KILOMETRE)) + " miles";
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static double DistanceUsingHaversine (double theFirstLatitude,
								   		  double theFirstLongitude,
								   		  double theSecondLatitude,
								   		  double theSecondLongitude)
	{
		// -------------------------------------------------------------------------
		// 11/02/2016 ECU created to calculate the distance between two coordinates
		//                using the Haversine method which is not really needed here
		//                but just wanted to have a play
		//
		//				  The haversine formula is an equation important in navigation, 
		//                giving great-circle distances between two points on a sphere 
		//                from their longitudes and latitudes. It is a special case of 
		//			      a more general formula in spherical trigonometry, the law of 
		//                Haversine, relating the sides and angles of spherical triangles. 
		//                The first table of haversines in English was published by James
		//                Andrew in 1805.
		//
		//            ECU the arguments are given in degrees so first job is to
		//                convert to radians (remember 360 degrees = 2 (PI) radians
		// 12/02/2016 ECU changed to convert using the library method
		// -------------------------------------------------------------------------
		theFirstLatitude   = Math.toRadians (theFirstLatitude);
		theFirstLongitude  = Math.toRadians (theFirstLongitude);
		theSecondLatitude  = Math.toRadians (theSecondLatitude);
		theSecondLongitude = Math.toRadians (theSecondLongitude);
		// ------------------------------------------------------------------------
		Double latitudeDistance  = (theSecondLatitude - theFirstLatitude);
		Double longitudeDistance = (theSecondLongitude - theFirstLongitude);
		// ------------------------------------------------------------------------
		// 11/02/2016 ECU now do the actual haversine calculation (see Wiki for an
		//                explanation if interested
		// ------------------------------------------------------------------------
		Double a = (Math.sin (latitudeDistance / 2) * Math.sin (latitudeDistance / 2)) + 
	               (Math.cos (theFirstLatitude) * Math.cos (theSecondLatitude) * 
	                   Math.sin (longitudeDistance / 2) * Math.sin (longitudeDistance / 2));
		
	    Double c = 2 * Math.atan2 (Math.sqrt(a), Math.sqrt(1-a));
	    // ------------------------------------------------------------------------
	    // 11/02/2016 ECU in the following line 6371 is the radius of the earth in
	    //                Kilometers
	    // ------------------------------------------------------------------------
	    return (6371 * c);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void DoNotSendEmailMethod (Object theSelection)
	{
		// -------------------------------------------------------------------------
		// 09/02/2016 ECU indicate who will receive the email
		// -------------------------------------------------------------------------
		// 10/02/2016 ECU finish this activity if required
		// -------------------------------------------------------------------------
		if (finishFlag)
			((Activity) context).finish ();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private boolean InitialiseGPS (boolean theCheckFlag)
	{
		// -------------------------------------------------------------------------
		// 18/10/2014 ECU check if location services are running
		//            ECU changed to boolean
		// 20/10/2014 ECU include the parameter to handle when monitoring remote
		//                devices. If the flag is false then do not bother to check
		//                if the location services are running
		// -------------------------------------------------------------------------
		if (Utilities.checkForLocationServices (this) || !theCheckFlag)
		{
			// ---------------------------------------------------------------------
			//            ECU Declare any variables
			// 02/01/2014 ECU changed code to use locationDetails
			// ---------------------------------------------------------------------		
			locationDetails  	= (TextView)findViewById (R.id.location_details);
			updatedCoordinate   = (TextView)findViewById (R.id.updated);
			// ---------------------------------------------------------------------
			// 02/01/2014 ECU get the image that will change depending on the provider
			// ---------------------------------------------------------------------
			locationImage      = (ImageView) findViewById (R.id.location_image);
			// ---------------------------------------------------------------------
			// Acquire a reference to the system Location Manager
			// 23/08/2013 ECU declared locationManager and locationListener externally because they
			//				  need to be used in onDestroy
			// ---------------------------------------------------------------------
			locationManager = ((LocationManager) this.getSystemService (Context.LOCATION_SERVICE));
			// ---------------------------------------------------------------------
			// 23/08/2013 ECU declare the listener and make this the recipient for
			//                updates from both GPS and the network
			// 19/01/2014 ECU changed to use MINIMUM_ vales instead of 0
			// ---------------------------------------------------------------------
			locationListener = new localLocationListener();
				
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					MINIMUM_TIME, MINIMUM_DISTANCE, locationListener);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					MINIMUM_TIME, MINIMUM_DISTANCE, locationListener);
			// ---------------------------------------------------------------------
			
			return true;
		}
		else
		{
			return false;
		}
		
	}
	/* ============================================================================= */
	public class localLocationListener implements LocationListener  
	{ 
		// -------------------------------------------------------------------------
		// 12/07/2013 ECU only interested in changes to the location
		// -------------------------------------------------------------------------
		@Override
	    public void onLocationChanged (Location location)  
	    { 
			// ---------------------------------------------------------------------
			// 12/06/2013 ECU process the new location
			// ---------------------------------------------------------------------
			MakeUseOfNewLocation (location);
			// ---------------------------------------------------------------------
			// 28/11/2015 ECU check whether the tracking information is to be
			//                send as part of the panic alarm process
			// ---------------------------------------------------------------------
			if ((PublicData.storedData.panicAlarm != null) && PublicData.storedData.panicAlarm.tracking)
			{
				// -----------------------------------------------------------------
				// 28/11/2018 ECU call up the handler
				// -----------------------------------------------------------------
				PanicAlarmActivity.locationUpdate (location);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
	    } 
		// -------------------------------------------------------------------------
		@Override
    	public void onProviderDisabled(String provider)  
    	{ 
	    } 
		// -------------------------------------------------------------------------
		@Override
    	public void onProviderEnabled(String provider)  
	    { 
	    }
		// -------------------------------------------------------------------------
		@Override
	    public void onStatusChanged(String provider, int status, Bundle extras)  
	    { 	
    	} 
	}
	/* ============================================================================= */
	private void MakeUseOfNewLocation (Location location) 
	{
		// -------------------------------------------------------------------------	
		// 02/01/2014 ECU display an image to indicate where the location came from
		//            ECU do different things depending on the provider
		// -------------------------------------------------------------------------
		currentProvider = location.getProvider();
		// -------------------------------------------------------------------------
		if (currentProvider.equalsIgnoreCase (getString(R.string.network)))
		{
			// ---------------------------------------------------------------------
			// 02/01/2014 ECU the wireless router is the source of the location
			// ---------------------------------------------------------------------
			locationImage.setImageResource (R.drawable.location_wireless);
			// ---------------------------------------------------------------------
			// 02/01/2014 ECU altitude is irrelevant from the router
			// ---------------------------------------------------------------------
			currentAltitude = StaticData.NO_RESULT;
		}
		else
		if (currentProvider.equalsIgnoreCase(getString(R.string.gps)))
		{
			// ---------------------------------------------------------------------
			// 02/01/2014 ECU the gps is the source of the location
			// ---------------------------------------------------------------------
			locationImage.setImageResource (R.drawable.location_gps);
			// ---------------------------------------------------------------------
			// 02/01/2014 ECU get the altitude in metres above sea level
			// ---------------------------------------------------------------------
			currentAltitude  = (double) location.getAltitude();
		}
		// -------------------------------------------------------------------------
		// get the current coordinates */
		// -------------------------------------------------------------------------
		currentLongitude = (double) location.getLongitude();
		currentLatitude  = (double) location.getLatitude();	
		// -------------------------------------------------------------------------
		// 21/10/2014 ECU get the current adjusted time
		// -------------------------------------------------------------------------
		adjustedTime = Utilities.getAdjustedTime(true);
		// -------------------------------------------------------------------------
		// 08/02/2016 ECU optionally write to the KML file
		// 26/02/2016 ECU changed to use the stored value
		// -------------------------------------------------------------------------
		if (PublicData.storedData.trackingDetails.enabled)
			AppendToKMLFile (currentLongitude,currentLatitude,currentAltitude);
		// -------------------------------------------------------------------------
		// 02/08/2013 ECU send datagram indicating the change
		// --------------------------------------------------------------------------
		PublicData.datagram.UpdateLocation 
			(PublicData.ipAddress,currentLongitude, currentLatitude, currentAltitude,adjustedTime);
		// --------------------------------------------------------------------------
		// 			  ECU get the current date and time for display 
		// 19/10/2014 ECU use the predefined date format
		// 20/10/2014 ECU change to use the adjusted time
		// 21/10/2014 ECU changed to use the variable
		// --------------------------------------------------------------------------
	    String outputString = "Last Updated\n" + PublicData.dateFormatter.format(adjustedTime);
	    // --------------------------------------------------------------------------
	    // add in any options to the string 
	    // --------------------------------------------------------------------------
	    if (PublicData.trackingMode)
	    	outputString = outputString + "\nTracking Mode On\nAccuracy = " + LocationActivity.track_accuracy;
	    
	    if (LocationActivity.alwaysPlay)
	    	outputString = outputString + "\nAlways Play Recorded Message";
	    
	    updatedCoordinate.setText (outputString);
	    // -------------------------------------------------------------------------
		// display the current location 
	    // 02/01/2014 ECU only display the formatted latitude and longitude
		// -------------------------------------------------------------------------
		locationDetails.setText("Latitude\n" 
									+ Location.convert(currentLatitude,Location.FORMAT_SECONDS) + 
								"\n\nLongitude\n" 
								     + Location.convert(currentLongitude,Location.FORMAT_SECONDS) + 
								((currentAltitude == StaticData.NO_RESULT) ? "\n\n\n" : "\n\nAltitude\n" + String.format("%6.3f",currentAltitude) + " metres"));
		// -------------------------------------------------------------------------
		// check if tracking is on 
		// -------------------------------------------------------------------------
		if (PublicData.trackingMode)
		{
			// ---------------------------------------------------------------------
			// check if the coordinates have changed and if so check if there is a
			// stored message to be displayed
			// ---------------------------------------------------------------------
			if (currentLongitude != previousLongitude || currentLatitude != previousLatitude)
			{	
				// -----------------------------------------------------------------
				// 19/10/2014 ECU check for a match
				// -----------------------------------------------------------------
				Utilities.scanForAMatch (this,currentLatitude,currentLongitude);
			}
		}
		// -------------------------------------------------------------------------
		// save these coordinates as the previous coordinates 
		// -------------------------------------------------------------------------
		previousLatitude  = currentLatitude;
		previousLongitude = currentLongitude;
		previousAltitude  = currentAltitude;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SendEmailMethod (Object theSelection)
	{
		// -------------------------------------------------------------------------
		// 09/02/2016 ECU indicate who will receive the email
		// -------------------------------------------------------------------------
		Utilities.popToast ("The email will be sent to " + PublicData.emailDetails.recipients,true);
		// -------------------------------------------------------------------------
		// 09/02/2016 ECU created to send the current track to recipient
		// -------------------------------------------------------------------------
		Utilities.SendEmailMessage (context,
									"Track",
									"The attached image is the latest track.",
									null,
									PublicData.storedData.trackingDetails.fileName);
		// -------------------------------------------------------------------------
		// 10/02/2016 ECU finish this activity if required
		// -------------------------------------------------------------------------
		if (finishFlag)
			((Activity) context).finish ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SetFileNameMethod (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 09/02/2016 ECU created to set the name of the file where the details to 
		//                stored. The file will be in the tracks directory and
		//                the kml extension will be added
		// -------------------------------------------------------------------------
		PublicData.storedData.trackingDetails.fileName 
			= PublicData.projectFolder + context.getString (R.string.tracks_directory) + 
				       theFileName + StaticData.EXTENSION_MAP;
		// -------------------------------------------------------------------------
		// 08/02/2016 ECU optionally write to the KML file after clearing the file
		//                in case it already exists
		// -------------------------------------------------------------------------
		AppendToKMLFile ();
		AppendToKMLFile (KML_HEADER);
		// -------------------------------------------------------------------------
		// 11/02/2016 ECU now check if a delay is wanted between the data
		// -------------------------------------------------------------------------
		// 09/02/2016 ECU set the time in seconds between successive writes
		//                to the log file
		// 11/02/2016 ECU set the cancel method
		// 06/03/2016 ECU add the '0' to represent the miminum value
		// -------------------------------------------------------------------------
		DialogueUtilities.sliderChoice (context,
			    						context.getString (R.string.track_log_time),
			    						context.getString (R.string.track_log_time_summary),
			    						R.drawable.timer,
			    						null,
			    						PublicData.storedData.trackingDetails.logToFileTimer,
			    						0,
			    						300,
			    						context.getString (R.string.set_track_log_time),
			    						Utilities.createAMethod (LocationActivity.class,"SetLogFileTime",0),
			    						context.getString (R.string.cancel_operation),
			    						Utilities.createAMethod (LocationActivity.class,"SetLogFileTimeCancel",0));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SetLogFileTime (int theLogFileTime)
	{
		// -------------------------------------------------------------------------
		// 09/02/2016 ECU created to set the time between successive write to the
		//                track file
		// 26/02/2016 ECU changed to use the stored value
		// -------------------------------------------------------------------------
		PublicData.storedData.trackingDetails.logToFileTimer = theLogFileTime;
		// -------------------------------------------------------------------------
		// 09/02/2016 ECU indicate that data can be written to disk
		// -------------------------------------------------------------------------
		logToFileTime = true;
		// -------------------------------------------------------------------------
		// 09/02/2016 ECU everything is set up so enable the logging
		// 26/02/2016 ECU changed to use the stored value
		// -------------------------------------------------------------------------
		PublicData.storedData.trackingDetails.enabled = true;
		// -------------------------------------------------------------------------
		// 11/02/2016 ECU initialise the distance
		// -------------------------------------------------------------------------
		distance = 0;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SetLogFileTimeCancel (int theLogFileTime)
	{
		// -------------------------------------------------------------------------
		// 11/02/2016 ECU created to handle the cancel button - set the time to 0
		// -------------------------------------------------------------------------
		SetLogFileTime (0);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void showPhotograph (ImageView theImageView,String thePhotoPath)
	{
			// ---------------------------------------------------------------------
			// 20/02/2017 ECU set the imageview to the specified photo and make it visible
			// ---------------------------------------------------------------------
			Utilities.displayAnImage (theImageView, thePhotoPath);
			imageView.setVisibility (View.VISIBLE);
			// ---------------------------------------------------------------------
			// 20/02/2017 ECU tell the user how to remove the photograph
			// 21/02/2017 ECU speak a longer phrase which is in effect a number of
			//                phrases separated by '.' - each '.' will cause a delay
			// ---------------------------------------------------------------------
			Utilities.SpeakAPhraseWithDelays (context,
					                          context.getString (R.string.photo_location),
					                          false);
			// ---------------------------------------------------------------------
	}
	// =============================================================================
	static class TimerHandler extends Handler
	{
		@Override
		public void handleMessage(Message theMessage) 
		{   
			// ---------------------------------------------------------------------
			// 09/02/1026 ECU changed to switch on the message type
			// ---------------------------------------------------------------------
			switch (theMessage.what)
			{
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_REMOTE_MONITOR:
					// -------------------------------------------------------------
					// 19/10/2014 ECU check if remote data has changed
					// 21/10/2014 ECU added the check on the datagram type
					// 10/02/2016 ECU added the check on remote mode so that device
					//                does not end up displaying itself
					//            ECU make sure don't 'remote monitor' myself
					// -------------------------------------------------------------
					if (PublicData.datagramChanged && 
						PublicData.datagram.type == StaticData.DATAGRAM_LOCATION &&
						!PublicData.datagram.sender.equalsIgnoreCase(PublicData.ipAddress))
					{
						// ---------------------------------------------------------
						// 20/10/2014 ECU try and get the device name of the sender
						// ---------------------------------------------------------
						senderDeviceDetails = Utilities.GetDeviceName (PublicData.datagram.sender);
						// ---------------------------------------------------------
						// 23/10/2014 ECU set out the details to be displayed
						// ---------------------------------------------------------
						if (senderDeviceDetails != null)
						{
							// -----------------------------------------------------
							// 23/10/2014 ECU include device name with the IP address
							// -----------------------------------------------------
							senderDeviceDetails 
								= "'" + senderDeviceDetails + "' (" + PublicData.datagram.sender + ")";
							// -----------------------------------------------------
							
						}
						else
						{
							// -----------------------------------------------------
							// 23/10/2014 ECU no device name found so just use the 
							//                IP address
							// -----------------------------------------------------
							senderDeviceDetails = PublicData.datagram.sender;
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
						// 19/10/2014 ECU display the remote device's position
						// 20/10/2014 ECU change to use screen rather than toast
						// 23/10/2014 ECU changed to use senderDeviceDetails
						// ---------------------------------------------------------
						locationDetails.setText ("Remote Device\n" + 
												senderDeviceDetails + "\n\n" +
												"Latitude\n" +
												Location.convert(PublicData.datagram.latitude,Location.FORMAT_SECONDS) + 
												"\n\nLongitude\n" +
												Location.convert(PublicData.datagram.longitude,Location.FORMAT_SECONDS) +
												((PublicData.datagram.altitude == StaticData.NO_RESULT) ? 
													"\n\n\n" : "\n\nAltitude\n" + String.format("%6.3f",PublicData.datagram.altitude) + " metres"));
						// ---------------------------------------------------------
						// 20/10/2014 ECU indicate the update time
						//            ECU changed to use the adjusted time
						// 21/10/2014 ECU change to use the time in the datagram
						// ---------------------------------------------------------
						updatedCoordinate.setText ("Last Updated\n" + PublicData.dateFormatter.format(PublicData.datagram.time));
						// ---------------------------------------------------------
						// 20/10/2014 ECU just tell the user of message coming in
						// ---------------------------------------------------------
						Utilities.SpeakAPhrase(context,"device location has updated");
						// ---------------------------------------------------------
						// 19/10/2014 ECU indicate that datagram change has been handled
						// ---------------------------------------------------------
						PublicData.datagramChanged = false;
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 20/10/2014 ECU check if the handler is to keep running
					// -------------------------------------------------------------
					if (keepTimerHandlerRunning)
						sleep (StaticData.MESSAGE_REMOTE_MONITOR,1000);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_RECEIVE:
					// -------------------------------------------------------------
					// 09/02/2016 ECU reset the flag to indicate time to go
					// -------------------------------------------------------------
					logToFileTime = true;
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}
		}
		/* ------------------------------------------------------------------------ */
		public void sleep (int theMessageType,long delayMillis)
	    {
			this.removeMessages (theMessageType);
			sendMessageDelayed(obtainMessage (theMessageType), delayMillis);
	    }
		// -------------------------------------------------------------------------
	};
	/* ============================================================================= */
}
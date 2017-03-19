package com.usher.diboson;

import java.lang.reflect.Method;
import android.os.BatteryManager;
import android.os.IBinder;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorService extends Service implements SensorEventListener
{
	// =============================================================================
	// see the Notes file for useful information
	// =============================================================================
	// Revision History
	// ================
	// 02/03/2015 ECU created
	// 14/03/2015 ECU added the monitoring of the battery level
	// 18/11/2015 ECU added the accelerometer handling rather than having it in the
	//                FallsActivity
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	// =============================================================================
	//private final static String	TAG = "SensorService";
	// =============================================================================
	
	// =============================================================================
	// 02/03/2015 ECU declare all required variables
	// -----------------------------------------------------------------------------
	static	boolean			accelerometerEnabled	= false;		// 18/11/2015 ECU added
	static  Method			accelerometerMethod		= null;			// 04/12/2015 ECU added
	static  int				batteryLevelCurrent		= StaticData.NO_RESULT;
																	// 27/04/2016 ECU added
	static	Context			context;								// 18/11/2015 ECU added
	static  float			lightLevelCurrent		= StaticData.NO_RESULT;
	 																// 27/04/2016 ECU added
	static 	Sensor 		   	lightSensor; 	
	static 	Sensor 		   	proximitySensor  		= null;			// 22/03/2016 ECU added
	static	SensorManager   sensorManager 			= null;
	// -----------------------------------------------------------------------------
	// 05/03/2015 ECU declare any variables that want to be accessible to other
	//                activities
	// 07/03/2015 ECU include the 'ambientLightSensor' to be available to other
	//                activities
	// 14/03/2015 ECU include the level of the battery
	// 03/05/2015 ECU include 'chargeStatus' so available to other activities
	// 22/03/2016 ECU added the proximity value
	// -----------------------------------------------------------------------------
	public static boolean   ambientLightSensor	=	false;
	public static int		batteryLevel		= 	StaticData.NO_RESULT;
	public static int 		chargeStatus		= 	StaticData.NO_RESULT;
	public static float		lightLevel			= 	StaticData.NO_RESULT;
	public static float		proximityValue		=	StaticData.NO_RESULT;
	/* ============================================================================= */
	public void onAccuracyChanged (Sensor sensor, int accuracy) 
	{ 
	  	if (sensor.getType() == Sensor.TYPE_LIGHT)
	   	{ 
	  		// ---------------------------------------------------------------------
	  		// 02/03/2015 ECU accuracy of the sensor has been changed
	  		// ---------------------------------------------------------------------
	   	} 
	}
	// =============================================================================
	@Override
	public IBinder onBind (Intent theIntent) 
	{
		// -------------------------------------------------------------------------
		// 02/03/2015 ECU called to return the communication channel to the service
		// -------------------------------------------------------------------------
		return null;
	}
	// ============================================================================= 
	@Override
	public void onCreate ()
	{
		// -------------------------------------------------------------------------
		// 02/03/2015 ECU called the main onCreate
		// -------------------------------------------------------------------------
		super.onCreate();
		// -------------------------------------------------------------------------
	
	}
	// =============================================================================
	public void onSensorChanged (SensorEvent sensorEvent) 
	{ 
		// -------------------------------------------------------------------------
		// 02/03/2015 ECU only interested in the ambient light sensor
		//            ECU this code was originally in GridActivity
		// 25/10/2015 ECU only check if the 'storedData' has been initialises
		// 28/11/2015 ECU put in check to make sure that the user interface is up
		//                and running to prevent early messages
		// 22/03/2016 ECU added the proximity sensor
		// -------------------------------------------------------------------------
		if (PublicData.storedData.initialised && PublicData.userInterfaceRunning)
		{
			// ---------------------------------------------------------------------
			// 25/10/2015 ECU the sensor data has been initialised so can carry on with
			//                the monitoring
			// ---------------------------------------------------------------------
			if (sensorEvent.sensor.getType () == Sensor.TYPE_LIGHT)
			{ 	  
				// -----------------------------------------------------------------
				// 05/03/2015 ECU store the light level so that it can be accessed by
				//                other activities
				// 27/04/2016 ECU store in working variable
				// -----------------------------------------------------------------
				lightLevelCurrent	= sensorEvent.values [0];
				// -----------------------------------------------------------------
				// 27/04/2016 ECU check whether the level has changed before checking
				//                for triggers
				// -----------------------------------------------------------------
				if (lightLevelCurrent != lightLevel)
				{
					// -------------------------------------------------------------
					// 27/04/2016 ECU the level has changed so stored the new value
					// -------------------------------------------------------------
					lightLevel = lightLevelCurrent;
					// -------------------------------------------------------------
					// 02/03/2015 ECU call the method which checks the light level 
					// -------------------------------------------------------------
					Utilities.checkLightLevel (getBaseContext(),lightLevel);
					// -------------------------------------------------------------
				}
			}
			else
			if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			{
				// -----------------------------------------------------------------
				// 18/11/2015 ECU added to receive events from the accelerometer -
				//                no need to check 'accelerometerEnabled' flag
				// 04/12/2015 ECU changed to use the method
				// -----------------------------------------------------------------
				if (accelerometerMethod != null)
				{
					try 
					{ 
						// -------------------------------------------------------------
						// 16/03/2015 ECU call up the method that will handle the 
						//                input text
						// -------------------------------------------------------------
						accelerometerMethod.invoke (null,new Object [] {sensorEvent});
						// -------------------------------------------------------------
					} 
					catch (Exception theException) 
					{	
					} 
				}
				// -----------------------------------------------------------------
			}
			else
			if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY)
			{
				// -----------------------------------------------------------------
				// 22/03/2016 ECU created to handle any events associated with
				//                the proximity sensor
				// -----------------------------------------------------------------
				Utilities.proxitySensor (getBaseContext(),sensorEvent.values [0],proximitySensor.getMaximumRange());
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
	}
	// ============================================================================= 
	@Override
	public int onStartCommand (Intent intent,int flags, int startId) 
	{
		// -------------------------------------------------------------------------
		// 18/11/2015 ECU remember the context for subsequent use
		// -------------------------------------------------------------------------
		context = this;
		// -------------------------------------------------------------------------
		// 17/02/2014 ECU set up the sensor aspects
		// 22/03/2016 ECU added the proximity sensor
		// -------------------------------------------------------------------------
		sensorManager 	= (SensorManager) getSystemService (SENSOR_SERVICE); 
		lightSensor 	= sensorManager.getDefaultSensor (Sensor.TYPE_LIGHT); 
		proximitySensor = sensorManager.getDefaultSensor (Sensor.TYPE_PROXIMITY); 
		// -------------------------------------------------------------------------
		// 02/03/2015 ECU register this service to receive information about the
		//                'light sensor'
		// 03/03/2015 ECU try and register a listener. If the call returns 'true'
		//                then all is well - if 'false' then it is likely that
		//                the device does not have/support the sensor
		// 14/03/2015 ECU changed the logic because used to stop the service
		//                if there is no ambient light sensor but because am now
		//                handling the battery then need to keep it running
		// -------------------------------------------------------------------------
		if (sensorManager.registerListener (this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL))
		{
			// ---------------------------------------------------------------------
			// 07/03/2015 ECU indicate that the 'ambientLightSensor' exists
			// ---------------------------------------------------------------------
			ambientLightSensor = true;
		}
		// -------------------------------------------------------------------------
		// 22/03/2016 ECU added the proximity sensor
		//            ECU check if the sensor exists before registering
		// --------------------------------------------------------------------------
		if (proximitySensor != null)
			sensorManager.registerListener (this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
		// -------------------------------------------------------------------------
		// 14/03/2015 ECU make sure the battery checker is not 'triggered'
		// 16/03/2015 ECU put in a check on null for the 'very first' time that it
		//                is run
		// -------------------------------------------------------------------------
		if (PublicData.storedData.battery == null)
			PublicData.storedData.battery = new SensorData ();
		// -------------------------------------------------------------------------
		// 24/10/2015 ECU only set the trigger to false if the app has been started
		//                manually
		// -------------------------------------------------------------------------
		if (PublicData.startedManually)
		{
			PublicData.storedData.setBatteryTriggered (false);
		}
		// -------------------------------------------------------------------------
		// 14/03/2015 ECU register the listener for 'battery level changes'
		// -------------------------------------------------------------------------
		this.registerReceiver (this.batteryReceiver, new IntentFilter (Intent.ACTION_BATTERY_CHANGED));
		// -------------------------------------------------------------------------
	    return Service.START_STICKY;
	}
	// ============================================================================= 
	@Override
	public void onDestroy () 
	{
		// -------------------------------------------------------------------------
		// 02/03/2015 ECU unregister this service as a listener of sensor events
		// -------------------------------------------------------------------------
		sensorManager.unregisterListener (this); 
		// -------------------------------------------------------------------------
		// 14/03/2015 ECU unregister the receiver for battery level changes
		// -------------------------------------------------------------------------
		unregisterReceiver (batteryReceiver);
		// -------------------------------------------------------------------------
		super.onDestroy();
	}
	// =============================================================================
	private BroadcastReceiver batteryReceiver = new BroadcastReceiver ()
	{
		// -------------------------------------------------------------------------
		// 14/03/2015 ECU receiver updates from the battery manager
		// -------------------------------------------------------------------------
	    @Override
	    public void onReceive (Context theContext, Intent theIntent) 
	    {
	    	// ---------------------------------------------------------------------
	    	// 24/10/2015 ECU only want to monitor the battery if the stored data
	    	//                has been initialised correctly
	    	// ---------------------------------------------------------------------
	    	if (PublicData.storedData.initialised)
	    	{
	    		// -----------------------------------------------------------------
	    		// 14/03/2015 ECU obtain the current battery level
	    		// 27/04/2016 ECU check if the battery level has changed
	    		// -----------------------------------------------------------------
	    		batteryLevelCurrent = theIntent.getIntExtra (BatteryManager.EXTRA_LEVEL,0);
	    		// -----------------------------------------------------------------
	    		// 27/04/2016 ECU check for a change in the battery level value
	    		// -----------------------------------------------------------------
	    		if (batteryLevel != batteryLevelCurrent)
	    		{
	    			// -------------------------------------------------------------
	    			// 27/04/2016 ECU the battery level has changed so store the new
	    			//                value and take any required actions
	    			// -------------------------------------------------------------
	    			batteryLevel = batteryLevelCurrent;
	    			// -------------------------------------------------------------
	    			// 15/03/2015 ECU try and delay when any warnings are generated until
	    			//                the GridActivity activity has been started
	    			// 10/04/2015 ECU pass through the charging status after storing it
	    			//                locally
	    			// -------------------------------------------------------------
	    			chargeStatus = theIntent.getIntExtra (BatteryManager.EXTRA_STATUS, StaticData.NO_RESULT);
	    			// -------------------------------------------------------------
	    			if (PublicData.storedData.battery != null && PublicData.gridActivityEntered)
	    				Utilities.checkBatteryTriggers (getBaseContext(),
	    												batteryLevel,
	    												(chargeStatus == BatteryManager.BATTERY_STATUS_CHARGING));
	    			// -------------------------------------------------------------
	    			// 27/04/2016 ECU send any monitoring data
	    			// -------------------------------------------------------------
	    			MonitorData.sendMonitorData (getBaseContext(),StaticData.MONITOR_DATA_BATTERY,batteryLevel);
	    			// -------------------------------------------------------------
	    		}
	    		// -----------------------------------------------------------------
	    	}
	    }
	};
	// =============================================================================
	
	// =============================================================================
	public static void accelerometerEnablement (boolean theEnablementFlag,Method theHandlerMethod,int theSensorRate)
	{
		// -------------------------------------------------------------------------
		// 18/11/2015 ECU created to handle the enabling / disabling of the
		//                listener of the accelerometer
		//
		//				  theEnablementFlag = true  enable the listener
		//					                = false disable the listener
		//
		// 15/02/2016 ECU added theSensorRate as an argument
		// -------------------------------------------------------------------------
		if (theEnablementFlag)
		{
			// ---------------------------------------------------------------------
			// 18/11/2015 ECU enable the listener for the accelerometer - check if
			//                already enabled (belt and braces)
			// ---------------------------------------------------------------------
			if (!accelerometerEnabled)
			{
				// -----------------------------------------------------------------
				// 18/11/2015 ECU indicate that accelerometer has been enabled
				// -----------------------------------------------------------------
				accelerometerEnabled = true;
		        // -----------------------------------------------------------------
				// 18/11/2015 ECU register the listener
				// 15/02/2016 ECU use theSensorRate to set the rate that data is
				//                retrieved rather than using SensorManager.SENSOR_DELAY_NORMAL
				// -----------------------------------------------------------------
				sensorManager.registerListener ((SensorEventListener) context,
												sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
												theSensorRate);
				// -----------------------------------------------------------------
				// 04/12/2015 ECU set up the method for the handler
				// -----------------------------------------------------------------
				accelerometerMethod = theHandlerMethod;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 18/11/2015 ECU disable the listener for the accelerometer - check if
			//                already disabled (belt and braces)
			// ---------------------------------------------------------------------
			if (accelerometerEnabled)
			{
				// -----------------------------------------------------------------
				// 18/11/2015 ECU indicate that accelerometer has been disabled
				// -----------------------------------------------------------------
				accelerometerEnabled = false;
		        // -----------------------------------------------------------------
				// 18/11/2015 ECU unregister the listener
				// -----------------------------------------------------------------
				sensorManager.unregisterListener((SensorEventListener) context,
												 sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------	
	}
	// =============================================================================
}

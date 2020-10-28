package com.usher.diboson;


import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class CompassActivity extends DibosonActivity implements SensorEventListener 
{
	// ===============================================================================
	// 05/06/2013 ECU created
	// 23/10/2014 ECU need to check that the device has the appropriate sensors because
	//                some cheaper tablets, e.g. the CnM tablet, return true to
	//                the SensorManager commands
	// 09/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//==============================================================================
	//private static final String TAG = "CompassActivity";
	/* ============================================================================= */
	private static final int 			MENU_REMOTE  = 0;
	// =============================================================================
	double 				azimuth;
	double				azimuthCalibrated = StaticData.NO_RESULT;
	SensorManager 		sensorManager = null;			// 23/10/2014 ECU added preset to null
	ImageCompass 		imageCompass;
	boolean             keepTimerHandlerRunning = true;
	double				pitch;      
	boolean             remoteMonitor 			= false;
	double 				roll; 
	/* ==================================================================== */
	private Sensor		sensorAccelerometer;  
	private Sensor 		sensorMagneticField;      
	private float [] 	valuesAccelerometer;  
	private float [] 	valuesMagneticField;      
	private float [] 	matrixR;  
	private float [] 	matrixI;  
	private float [] 	matrixValues; 
	// ============================================================================= 
	TimerHandler 		timerHandler	= new TimerHandler();
	// =============================================================================
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 16/02/2014 ECU call up routine to set common activity features
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this);
			// ---------------------------------------------------------------------
			getWindow().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN,
								  WindowManager.LayoutParams.FLAG_FULLSCREEN);
			// ---------------------------------------------------------------------
			// requestWindowFeature(Window.FEATURE_NO_TITLE);
			// ---------------------------------------------------------------------
			// 05/06/2013 ECU set up the graphics view that will be updated
			// ---------------------------------------------------------------------
			imageCompass = new ImageCompass(this);
		
			setContentView (imageCompass);
			// ---------------------------------------------------------------------
			// 05/06/2013 ECU initialise storage variables
			// ---------------------------------------------------------------------
			valuesAccelerometer = new float[3];			// will store accelerator values
			valuesMagneticField = new float[3];   		// will store the magnetic field values
			matrixR             = new float[9]; 		// will store rotation matrix  
			matrixI             = new float[9];			// will store inclination matrix  
			matrixValues        = new float[3]; 
			// ---------------------------------------------------------------------
			// 23/10/2014 ECU try and determine if the device has a physical compass. Had
			//                to do this because of the issue about some cheaper tablets
			//                returning true when using SensorManager.getDefault... even
			//                though they physically do not have the sensors
			// ---------------------------------------------------------------------
			if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS))
			{
				// -----------------------------------------------------------------
				// 23/10/2014 ECU this device does have a physical compass so continue
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				// 05/06/2013 ECU set up the sensors and listeners
				// -----------------------------------------------------------------
				sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		
				sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);   
				sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
				sensorManager.registerListener (this,
						sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
						SensorManager.SENSOR_DELAY_NORMAL);
				sensorManager.registerListener (this,
						sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_NORMAL);
				// -----------------------------------------------------------------
				// 21/10/2014 ECU start up the thread which will check for remote updates
				// -----------------------------------------------------------------
				timerHandler.sleep (10000);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 23/10/2014 ECU the device does not have a physical sensor so let it
				//                just check for remote updates
				// -----------------------------------------------------------------
				Utilities.SpeakAPhrase(this,
						new String [] {"This device does not have a physical compass",
										"so it will try and monitor remote devices",
										"as it cannot be calibrated you will need to rotate this device to match the one being monitored"});
				// -----------------------------------------------------------------
				// 21/10/2014 ECU cannot calibrate the device so just set to 0
				// -----------------------------------------------------------------
				azimuthCalibrated = 0;
				// -----------------------------------------------------------------
				// 21/10/2014 ECU put the activity into remote monitor mode
				// -----------------------------------------------------------------
				remoteMonitor = true;
				// -----------------------------------------------------------------
				// 21/10/2014 ECU start up the thread which will check for remote updates
				// -----------------------------------------------------------------
				timerHandler.sleep(10000);
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	@Override
	public boolean onCreateOptionsMenu (Menu menu) 
	{
		// -------------------------------------------------------------------------
		// Inflate the menu; this adds items to the action bar if it is present.
		// -------------------------------------------------------------------------
		menu.add (0,MENU_REMOTE,0,"Set Remote Monitoring Mode");
		// -------------------------------------------------------------------------
		return true;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
    public void onDestroy()
    {	
		// -------------------------------------------------------------------------
		// 20/10/2014 ECU indicate that the timer handler should stop
		// -------------------------------------------------------------------------
		keepTimerHandlerRunning = false;
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
		// 21/10/2014 ECU clear the displayed menu
		// -------------------------------------------------------------------------
		menu.clear ();
		// -------------------------------------------------------------------------	
		// 21/10/2014 ECU used the method to build menu
		// -------------------------------------------------------------------------	
		return onCreateOptionsMenu (menu);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public boolean onOptionsItemSelected (MenuItem item)
	{
		// -------------------------------------------------------------------------
		// 21/10/2014 ECU take the actions depending on which menu is selected
		// -------------------------------------------------------------------------
		switch (item.getItemId())
		{
			case MENU_REMOTE:
				// -----------------------------------------------------------------
				// 21/10/2014 ECU set the current azimuth as the calibration point
				// -----------------------------------------------------------------
				azimuthCalibrated = azimuth;
				// -----------------------------------------------------------------
				// 21/10/2014 ECU put the activity into remote monitor mode
				// -----------------------------------------------------------------
				remoteMonitor = true;
				// -----------------------------------------------------------------
				return true;
			// ---------------------------------------------------------------------
			// 31/12/2017 ECU added the default
			// ---------------------------------------------------------------------
			default:
	            return super.onOptionsItemSelected(item);
	        // ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onResume() 
	{
		// -------------------------------------------------------------------------
		// 23/10/2014 ECU added the check on null
		// -------------------------------------------------------------------------
		if (sensorManager != null)
		{
			sensorManager.registerListener(this,sensorAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
			sensorManager.registerListener(this,sensorMagneticField,SensorManager.SENSOR_DELAY_NORMAL);
		}
		// -------------------------------------------------------------------------
		super.onResume();		
	}
	/* ============================================================================= */
	@Override
	public void onPause() 
	{
		// -------------------------------------------------------------------------
		// 23/10/2014 ECU added the check on null
		// -------------------------------------------------------------------------
		if (sensorManager != null)
		{
			sensorManager.unregisterListener(this,sensorAccelerometer);
			sensorManager.unregisterListener(this,sensorMagneticField);
		}
		// -------------------------------------------------------------------------
		super.onPause();
	}
	/* ============================================================================= */
	public void onSensorChanged(SensorEvent sensorEvent) 
	{
		// -------------------------------------------------------------------------
		// 05/06/2013 ECU save the values depending on the type of sensor
		// -------------------------------------------------------------------------
		switch(sensorEvent.sensor.getType())
		{   
			// ---------------------------------------------------------------------
			case Sensor.TYPE_ACCELEROMETER:   
				for(int i =0; i < 3; i++)
				{     
					valuesAccelerometer[i] = sensorEvent.values[i];    
				}    
				break;  
			// ---------------------------------------------------------------------	
			case Sensor.TYPE_MAGNETIC_FIELD:   
				for(int i =0; i < 3; i++)
				{    
					valuesMagneticField[i] = sensorEvent.values[i];   
				}    
				break;  
			// ---------------------------------------------------------------------
		} 
		// -------------------------------------------------------------------------
		// 05/06/2013 ECU get the relevant matrices
		// -------------------------------------------------------------------------
		boolean success = SensorManager.getRotationMatrix(matrixR,matrixI,valuesAccelerometer,valuesMagneticField);   
		// -------------------------------------------------------------------------
		// 05/06/2013 ECU if the matrices successfully obtained then proceed
		// -------------------------------------------------------------------------
		if (success)
		{    
			SensorManager.getOrientation(matrixR, matrixValues);
			
			azimuth = Math.toDegrees(matrixValues[0]); 

			// ---------------------------------------------------------------------
			// 05/06/2013 ECU calculate the pitch and roll although I am
			//                not using them at this stage
			// --------------------------------------------------------------------- 
			pitch   = Math.toDegrees(matrixValues[1]);    
			roll    = Math.toDegrees(matrixValues[2]);  
			// ---------------------------------------------------------------------
			// 21/10/2014 ECU only continue if not monitoring a remote device
			// ---------------------------------------------------------------------
			if (!remoteMonitor)
			{
				// -----------------------------------------------------------------
				// 05/06/2013 ECU update the image
				// -----------------------------------------------------------------
				setTitle (imageCompass.update ((float)azimuth,(float)pitch,(float)roll,remoteMonitor)); 
				// -----------------------------------------------------------------
				// 02/08/2013 ECU store the values in the datagram
				// -----------------------------------------------------------------
				PublicData.datagram.UpdateCompass (PublicData.ipAddress,azimuth,pitch,roll);
			}
			// ----------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{
				
	}
	/* ============================================================================= */
	@SuppressLint("HandlerLeak")
	class TimerHandler extends Handler
	{
		@Override
		public void handleMessage(Message message) 
		{   
			// ---------------------------------------------------------------------
			// 19/10/2014 ECU check if remote data has changed
			// 21/10/2014 ECU added the check on the datagram type
			// 21/10/2014 ECU added remoteMonitor check in 'if'
			// ---------------------------------------------------------------------
			if (remoteMonitor && PublicData.datagramChanged && PublicData.datagram.type == StaticData.DATAGRAM_COMPASS)
			{
				// -----------------------------------------------------------------
				// 21/10/2014 ECU add in "azimuth - azimuthCalibrated" which will
				//                take account the orientation of the monitoring
				//                device
				// -----------------------------------------------------------------
				setTitle(imageCompass.update ((float) (PublicData.datagram.azimuth + 
													(azimuth - azimuthCalibrated)),
											  (float) (PublicData.datagram.pitch),
											  (float) (PublicData.datagram.roll),
											  remoteMonitor)); 
				// -----------------------------------------------------------------
				// 21/10/2014 ECU indicate that the datagram has been processed
				// -----------------------------------------------------------------
				PublicData.datagramChanged = false;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 20/10/2014 ECU check if the handler is to keep running
			// ---------------------------------------------------------------------
			if (keepTimerHandlerRunning)
				sleep (1000);
			// ---------------------------------------------------------------------
		}
		/* ------------------------------------------------------------------------ */
		public void sleep(long delayMillis)
	    {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
	    }
	};
}
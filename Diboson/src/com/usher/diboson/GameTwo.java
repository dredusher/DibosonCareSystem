package com.usher.diboson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class GameTwo extends DibosonActivity implements SensorEventListener
{
	/* =============================================================================
	   Revision History
	   ================
	   23/05/2013 ECU originated
	   16/09/2013 ECU made some changes because was getting a null
		              point exception after exiting
	   20/02/2014 ECU tidy everything up
	   22/10/2015 ECU changed to 'extends DibosonActivity'
	   02/11/2015 ECU put in the check as to whether the activity has been created
	                  anew or is being recreated after having been destroyed by
	                  the Android OS
	   18/12/2016 ECU use 'long click' to change or reset the background image
	              ECU general tidy up of the code layout
	   ============================================================================= 
	   Testing
	   =======
	   ============================================================================= */
	
	/* ============================================================================= */
	//final static String TAG = "GameTwo";
	/* ============================================================================= */
	private	final 	static 	int		EARTH_SIZE			= 100;		// 16/09/2013 ECU size of the earth image 
	private final 	static	float 	FACTOR_BOUNCEBACK 	= 0.50f;	 
	private final 	static	float   FACTOR_FRICTION   	= 0.5f;    	// imaginary friction on the screen
	private final 	static	float   GRAVITY           	= SensorManager.GRAVITY_EARTH;    	
																	// acceleration of gravity
																	// 19/12/2016 ECU changed from 9.8f
	private final   static  int	    BALL_SIZE			= 10;
	private final 	static	float   TARGET_VELOCITY		= 5.0f;     // target velocity for landing
	/* ============================================================================= */
					float 			accelerationX;
					float			accelerationY;
					float			accelerationZ;
					Sensor 			accelerometer;
					Bitmap          background;
	static			Context			context;						// 18/12/2016 ECU added
					Bitmap          earth;
					float           earthX;	
					float           earthY;	
					boolean			fastMessage         = false;
					boolean         haveLanded         	= false;
					float			landingZoneXLower;
					float			landingZoneYLower;
					float			landingZoneXUpper;
					float			landingZoneYUpper;
					Paint			paint;
					SensorManager 	sensorManager;
					boolean			shapeThreadRunning	= false;
					ShapeView 		shapeView;
					int             ballX;					         // x position of ball
					int             ballY;					         // y position of ball
					float           timeInterval       	= 0.5f; 
					float			velocityX;
					float			velocityY;
	/* ============================================================================= */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 02/11/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 16/02/2014 ECU call up routine to set common activity features
			// 08/04/2014 ECU changed to use the variable
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			// ---------------------------------------------------------------------
			// 18/12/2016 ECU remember the context for later use
			// ---------------------------------------------------------------------
			context = this;
			// --------------------------------------------------------------------- 
			// initialise accelerometer
			// ---------------------------------------------------------------------
			sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
			accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
			// ---------------------------------------------------------------------
			// 20/02/2014 ECU set the position of the earth to be in the middle of the screen
			// ---------------------------------------------------------------------
			earthX = (float) (PublicData.screenWidth / 2);
			earthY = (float) (PublicData.screenHeight / 2);
			// ---------------------------------------------------------------------
			// 19/02/2014 ECU define the landing zone
			// ---------------------------------------------------------------------
			landingZoneXLower = earthX - (EARTH_SIZE/2 - BALL_SIZE);
			landingZoneXUpper = earthX + (EARTH_SIZE/2 - BALL_SIZE);
			landingZoneYLower = earthY - (EARTH_SIZE/2 - BALL_SIZE);
			landingZoneYUpper = earthY + (EARTH_SIZE/2 - BALL_SIZE);
			// ---------------------------------------------------------------------
			// 20/02/2014 ECU set up paint tool
			// ---------------------------------------------------------------------
			paint = new Paint  ();
			paint.setColor     (Color.WHITE);
			paint.setAlpha     (192);
			paint.setStyle     (Paint.Style.FILL);
			paint.setAntiAlias (true);
			// ---------------------------------------------------------------------
			// 20/02/2014 ECU define the view that will be used for rendering the ball 
			//            ECU set the initial position of the ball
			// ---------------------------------------------------------------------
			shapeView = new ShapeView (this);
			shapeView.setOvalCentre ((int)(PublicData.screenWidth / 2), (int)(PublicData.screenHeight - BALL_SIZE));
		 	// ---------------------------------------------------------------------   
			// 20/02/2014 ECU read in the images for the background and earth
			// 18/12/2016 ECU decide whether to use default or the contents of a file
			// ---------------------------------------------------------------------
			if (PublicData.storedData.dexterityGameBackground == null)
			{
				// -----------------------------------------------------------------
				// 18/12/2016 ECU just use the image stored as a resource
				// -----------------------------------------------------------------
				background = BitmapFactory.decodeResource (getResources(),R.drawable.galaxy); 
				background = Bitmap.createScaledBitmap (background,PublicData.screenWidth,PublicData.screenHeight,false); 
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 18/12/2016 ECU use the contents of a file as the background
				//            ECU the background image may not be in the project
				//                folder so the whole path is stored
				// -----------------------------------------------------------------
				background = Utilities.scaleBitMap (PublicData.storedData.dexterityGameBackground, PublicData.screenWidth, PublicData.screenHeight);
				// -----------------------------------------------------------------
				// 18/12/2016 ECU check if an error occurred
				// -----------------------------------------------------------------
				if (background == null)
				{
					// -------------------------------------------------------------
					// 18/12/2016 ECU reset the image and restart this activity
					// -------------------------------------------------------------
					ResetBackgroundMethod (null);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 19/12/2016 ECU Note - set up the bitmap for the planet
			// ---------------------------------------------------------------------
			earth = BitmapFactory.decodeResource (getResources(),R.drawable.earth);		 		 
			earth = Bitmap.createScaledBitmap (earth, EARTH_SIZE, EARTH_SIZE, false);
			// ---------------------------------------------------------------------
			// 20/02/2014 ECU set up the content view for display
			// ---------------------------------------------------------------------	
			setContentView (shapeView);
		 
			shapeView.setZOrderOnTop (true);
			shapeView.getHolder ().setFormat (PixelFormat.TRANSLUCENT);
			// ---------------------------------------------------------------------
			// 18/12/2016 ECU set up the 'long click' listener which will enable
			//                the background to be changed or reset
			// ---------------------------------------------------------------------
			shapeView.setOnLongClickListener (new View.OnLongClickListener() 
			{
				@Override
				public boolean onLongClick (View theView) 
				{
					// -------------------------------------------------------------
					// 18/12/2016 ECU ask the user if the background is to be reset
					//                or changed to another image
					// -------------------------------------------------------------
					DialogueUtilities.yesNo (context,  
											 context.getString (R.string.background_title),
											 context.getString (R.string.background_summary),
											 null,
											 true,context.getString (R.string.background_define),Utilities.createAMethod (GameTwo.class,"DefineBackgroundMethod",(Object) null),
											 true,context.getString (R.string.background_reset), Utilities.createAMethod (GameTwo.class,"ResetBackgroundMethod", (Object) null)); 
					// -------------------------------------------------------------
					return true;
				}
			});
			// ---------------------------------------------------------------------
			// 19/02/2014 ECU indicate the aim of the game
			// 18/12/2016 ECU use resource
			// ---------------------------------------------------------------------
			Utilities.SpeakAPhrase (this,getString (R.string.game_two_move));
			// ---------------------------------------------------------------------
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
	public void onAccuracyChanged (Sensor sensor, int accuracy) 
	{	 
	}
	/* ============================================================================= */
	@Override
    public void onDestroy()
    {	
		// -------------------------------------------------------------------------
		// 16/09/2013 ECU added this method and just reset a couple of variables for a clean
		//                exit
		// -------------------------------------------------------------------------
		haveLanded 			= true;
		shapeThreadRunning 	= false;
		// -------------------------------------------------------------------------
        super.onDestroy();
    }
	// =============================================================================
	@Override
	protected void onPause() 
	{
		super.onPause();
		// -------------------------------------------------------------------------
		// 16/09/2013 ECU stop sensor sensing
		// -------------------------------------------------------------------------
		sensorManager.unregisterListener(this);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	protected void onResume() 
	{
		super.onResume();
		// ------------------------------------------------------------------------- 
		// 16/09/2013 ECU start sensor sensing
		// -------------------------------------------------------------------------
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public void onSensorChanged (SensorEvent event) 
	{
		// -------------------------------------------------------------------------
		// 16/09/2013 ECU obtain the three accelerations from sensors
		// -------------------------------------------------------------------------
		accelerationX = event.values[0];
		accelerationY = event.values[1];
	 
		accelerationZ = event.values[2];
		// -------------------------------------------------------------------------
		// 16/09/2013 ECU taking into account the frictions
		// -------------------------------------------------------------------------
		accelerationX 
		   = Math.signum (accelerationX) * Math.abs(accelerationX) * (1 - FACTOR_FRICTION * Math.abs(accelerationZ) / GRAVITY);
		accelerationY 
		   = Math.signum (accelerationY) * Math.abs(accelerationY) * (1 - FACTOR_FRICTION * Math.abs(accelerationZ) / GRAVITY);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private class ShapeView extends SurfaceView implements SurfaceHolder.Callback
	{
		private RectF 		rectangle;
		private ShapeThread shapeThread;
	 	//-------------------------------------------------------------------------- 
		public ShapeView (Context context) 
		{
			// ---------------------------------------------------------------------
			super (context);
	 
			getHolder ().addCallback(this);
			// ---------------------------------------------------------------------
			// 20/02/2014 ECU define the thread to do the work
			// ---------------------------------------------------------------------
			shapeThread = new ShapeThread(getHolder(), this);
			
			setFocusable(true);
			// ---------------------------------------------------------------------
			// 20/02/2014 ECU define a rectangle that will be used for drawing
			//                the ball
			// ---------------------------------------------------------------------
			rectangle = new RectF ();
		}
		// -------------------------------------------------------------------------
		public boolean setOvalCentre(int x, int y)
		{
			// ---------------------------------------------------------------------
			// 20/02/2014 ECU set the position of the ball
			// ---------------------------------------------------------------------
			ballX 	= x;
			ballY 	= y;
			// ---------------------------------------------------------------------
			return true;
		}
		// ------------------------------------------------------------------------- 
		public boolean updateOvalCentre()
		{
			// ---------------------------------------------------------------------
			// 20/02/2014 ECU update the ball's position from the latest 
			//                sensor values
			// ---------------------------------------------------------------------
			MoveTheBall ();
			// ---------------------------------------------------------------------
			return true;
		}
		// =========================================================================
		protected void onDraw (Canvas canvas)
		{
			// ---------------------------------------------------------------------
			// 27/05/2013 ECU included the haveLanded check
			// 16/09/2013 ECU added the canvas check
			// ---------------------------------------------------------------------
			if (canvas != null && rectangle != null && !haveLanded)
			{	
				// -----------------------------------------------------------------
				// 20/02/2014 ECU get the co-ordinates of the ball ready for re-drawing
				// -----------------------------------------------------------------
				rectangle.set (ballX - BALL_SIZE, 
						       ballY - BALL_SIZE, 
						       ballX + BALL_SIZE,
						       ballY + BALL_SIZE);
				// -----------------------------------------------------------------
				// 26/05/2013 ECU clear the whole canvas to black
				// -----------------------------------------------------------------
				canvas.drawBitmap (background, 0, 0, paint);
				canvas.drawBitmap (earth,earthX-EARTH_SIZE/2,earthY-EARTH_SIZE/2,paint);
				// -----------------------------------------------------------------
				// 20/02/2014 ECU now draw the ball in its new position
				// -----------------------------------------------------------------
				canvas.drawOval(rectangle, paint);
				// -----------------------------------------------------------------
			}
		}
		// ========================================================================= 
		@Override
		public void surfaceChanged (SurfaceHolder holder, int format, 
											              int width,int height) 
		{
		}
		// =========================================================================
		@Override
		public void surfaceCreated(SurfaceHolder holder) 
		{
			// ---------------------------------------------------------------------
			// 20/02/2014 ECU set the thread running
			// 04/11/2015 ECU added a try/catch just in case
			// ---------------------------------------------------------------------
			try
			{
				shapeThread.setRunning (true);
				shapeThread.start ();
			}
			catch (Exception theException)
			{
				// -----------------------------------------------------------------
				// 04/11/2015 ECU seem to get this if exiting from the
				//                documentation activity
				//            ECU the error is that the "Thread already started"
				//                as this won't happen during normal running
				//                then investigate later
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
		}
		// =========================================================================
		@Override
		public void surfaceDestroyed (SurfaceHolder holder) 
		{
			boolean retry = true;
			// ---------------------------------------------------------------------
			// 20/02/2014 ECU stop the thread from running
			// ---------------------------------------------------------------------
			shapeThread.setRunning(false);
			
			while(retry)
			{
				try
				{
					// -------------------------------------------------------------
					// 20/02/2014 ECU block thread until receiver finishes
					//                execution
					// -------------------------------------------------------------
					shapeThread.join ();
					// -------------------------------------------------------------
					retry = false;
				} 
				catch (InterruptedException theException)
				{
				}
			}
		}
	}
	/* ============================================================================= */
	class ShapeThread extends Thread 
	{
		// -------------------------------------------------------------------------
		private SurfaceHolder 	surfaceHolder;
		private ShapeView 		shapeView;
		// -------------------------------------------------------------------------
		public ShapeThread (SurfaceHolder theSurfaceHolder, ShapeView theShapeView) 
		{
			surfaceHolder 	= theSurfaceHolder;
			shapeView 		= theShapeView;
		}
		// -------------------------------------------------------------------------
		public void setRunning (boolean run)
		{
			shapeThreadRunning = run;
		}
		// -------------------------------------------------------------------------
		public SurfaceHolder getSurfaceHolder() 
		{
			return surfaceHolder;
		}
		// -------------------------------------------------------------------------
		@Override
		public void run() 
		{
			Canvas canvas;
			
			while (shapeThreadRunning) 
			{
				shapeView.updateOvalCentre ();
				
				canvas = null;
				try 
				{
					canvas = surfaceHolder.lockCanvas (null);
					
					synchronized (surfaceHolder) 
					{
						// ---------------------------------------------------------
						// 16/09/2013 ECU changed from onDraw
						// --------------------------------------------------------- 
						shapeView.draw (canvas);
						// ---------------------------------------------------------
					}
				} 
				finally 
				{
					if (canvas != null) 
					{
						surfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void DefineBackgroundMethod (Object theDummyArgument)
	{
		// -------------------------------------------------------------------------
		// 18/12/2016 ECU define the background image
		// -------------------------------------------------------------------------
		Utilities.selectAFile (context,StaticData.EXTENSION_PHOTOGRAPH,
				new MethodDefinition <GameTwo> (GameTwo.class,"SelectedBackground"));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void ResetBackgroundMethod (Object theDummyArgument)
	{
		// -------------------------------------------------------------------------
		// 18/12/2016 ECU reset the background image
		// -------------------------------------------------------------------------
		PublicData.storedData.dexterityGameBackground = null;
		// -------------------------------------------------------------------------
		// 18/12/2016 ECU stop and then restart this activity
		// -------------------------------------------------------------------------
		restartThisActivity ();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	void MoveTheBall ()
	{
		// -------------------------------------------------------------------------
		// 20/02/2014 ECU recalculate the ball's position from the latest sensor readings
		// -------------------------------------------------------------------------
		velocityX -= accelerationX * timeInterval;
		velocityY += accelerationY * timeInterval;
		
		ballX += (int)(timeInterval * (velocityX + 0.5 * accelerationX * timeInterval));
		ballY += (int)(timeInterval * (velocityY + 0.5 * accelerationY * timeInterval));		 
		// -------------------------------------------------------------------------
		// 26/05/2013 ECU check if hit the target
		// 04/06/2013 ECU put in the tolerance option 
		// -------------------------------------------------------------------------
		if (((ballX >= landingZoneXLower) && (ballX <= landingZoneXUpper)) &&
			((ballY >= landingZoneYLower) && (ballY <= landingZoneYUpper)))
		{
			// --------------------------------------------------------------------- 
			// 04/06/2013 ECU include the velocity checks
			// --------------------------------------------------------------------- 
			if (!haveLanded && (Math.abs(velocityX) < TARGET_VELOCITY) && (Math.abs(velocityY) < TARGET_VELOCITY))
			{
				fastMessage = true;
				
				haveLanded = true;
			    // -----------------------------------------------------------------	
				// 17/02/2014 ECU pass through the image id as a parameter
				// 18/02/2014 ECU changed to use the new DisplayADrawable method
				// -----------------------------------------------------------------	
				Utilities.DisplayADrawable (getBaseContext(),R.drawable.moon);
				// -----------------------------------------------------------------
				// 18/02/2016 ECU changed to use context and resources
				// -----------------------------------------------------------------
				Utilities.SpeakAPhrase (context,context.getString (R.string.game_two_well_done));
				Utilities.SpeakAPhrase (context,context.getString (R.string.game_two_landed));
				// -----------------------------------------------------------------	
				finish ();
			}
			else
			{
				// -----------------------------------------------------------------
				// 19/02/2014 ECU in the correct area but too fast
				// 18/12/2016 ECU use resource
				// 19/12/2016 ECU changed to use 'context' instead of 'GameTwo.this'
				// -----------------------------------------------------------------
				if (!fastMessage)
				{
					Utilities.SpeakAPhrase (context,context.getString (R.string.game_two_too_fast));
					// -------------------------------------------------------------
					// 20/02/2014 ECU indicate no need to say the message again
					// -------------------------------------------------------------
					fastMessage = true;
					// -------------------------------------------------------------
				}
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 19/02/2014 ECU reset the fast message
			// ---------------------------------------------------------------------
			fastMessage = false;
		}
		// -------------------------------------------------------------------------	
		// 19/02/2014 ECU handle bouncing off of the edges
		// -------------------------------------------------------------------------
		if(ballX < BALL_SIZE)
		{
			ballX 		= BALL_SIZE;
			velocityX 	= -velocityX * FACTOR_BOUNCEBACK;
		}
		else
		if(ballX > (PublicData.screenWidth - BALL_SIZE))
		{
			ballX 		= PublicData.screenWidth - BALL_SIZE;
			velocityX 	= -velocityX * FACTOR_BOUNCEBACK;
		}
		
		if(ballY < BALL_SIZE)  
		{  
			ballY 		= BALL_SIZE;  
			velocityY 	= -velocityY * FACTOR_BOUNCEBACK;  
		}  
		else 
 		if(ballY > PublicData.screenHeight - (2 * BALL_SIZE))
		{
			ballY 		= PublicData.screenHeight - (2 * BALL_SIZE);
			velocityY 	= -velocityY * FACTOR_BOUNCEBACK;
		}
	}
	// =============================================================================
	public static void restartThisActivity ()
	{
		// -------------------------------------------------------------------------
		// 18/12/2016 ECU 'finish' this activity
		// -------------------------------------------------------------------------
		((Activity) GameTwo.context).finish ();
		// -------------------------------------------------------------------------
		// 18/12/2016 ECU restart this activity
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (GameTwo.context,GameTwo.class);
		localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		GameTwo.context.startActivity (localIntent);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void SelectedBackground (String theFileName)
 	{
 		// -------------------------------------------------------------------------
 		// 18/12/2016 ECU created to get the file that contains the image for the
		//                background
		//            ECU save the whole file path because a picture outside of the
		//                project folder may be chosen
 		// -------------------------------------------------------------------------
 		PublicData.storedData.dexterityGameBackground = theFileName; 	
 		// -------------------------------------------------------------------------
 		// 18/12/2016 ECU stop and restart this activity
 		// -------------------------------------------------------------------------
 		restartThisActivity ();
 		// -------------------------------------------------------------------------
 	}
	// =============================================================================
}

package com.usher.diboson;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.widget.ImageView;
import android.widget.Toast;

public class Accelerometer extends DibosonActivity implements SensorEventListener,OnGestureListener
{
	// =============================================================================
	// 22/10/2015 ECU changed to 'extends DibosonActivity"
	// =============================================================================
	
	/* ============================================================================= */
	//final static String 	TAG = "Accelerometer";
	/* ============================================================================= */
	final static int		BACKGROUND_COLOUR = Color.TRANSPARENT;
	final static int        MAIN_GAP = 35;
	final static int		MAXIMUM_VALUE = 10;
	final static int		NUMBER_OF_LAYERS = 4;
	final static int        REARM_TIME = 10000;
	final static int		STROKE_WIDTH = 3;
	final static int		X_COORD = 0;
	final static int        Y_COORD = 1;
	// -----------------------------------------------------------------------------
	static float			accelerometerXDirection;
	static float			accelerometerYDirection;
	static float 			accelerometerZDirection;
	static int []           ball = {50,30};			// 25/09/2013 ECU ball coordinates
	static float			changeAccelerometerXDirection;
	static float			changeAccelerometerYDirection;
	static float			changeAccelerometerZDirection;
	static int				deltaX = 5;
	static int				deltaY = 5;
	float					fallCriteria = 10f;
	GestureDetector 		gestureScanner;
	static ImageView		graphicsView;
	static ImageViewCustom	graphicsViewCustom;
	static int				graphicsXOrigin;
	static int				graphicsYOrigin;
	static Bitmap			layerBitmap;
	static Canvas 			layerCanvas;
	static LayerDrawable 	layerDrawable;
	static Paint            layerPaint = new Paint ();
	static Drawable[] 		layers;
	int						opacity = 50;
	static Paint   			paint;	
	static float			previousAccelerometerXDirection;
	static float			previousAccelerometerYDirection;
	static float			previousAccelerometerZDirection;
	static float			radius = 20;
	static float			radiusMain;
	RefreshHandler			refreshHandler;
	static Resources		resources;				// 24/09/2013 ECU added
	static float			scale;
	static float			scaleComponents;
	ScaleGestureDetector 	scaleGestureDetector;	// 16/09/2013 ECU added for creating pinch events
	SensorManager 			sensorManager;
	static boolean			triggerArmed = true;
	static Bitmap			windowBitmap;
	static Canvas 			windowCanvas;
	static int				windowHeight;	
	static int 				windowWidth;
	/* ============================================================================= */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		// 09/10/2015 ECU want to check if the activity is being newly created or
		//                just recreated having been destroyed by Android
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 16/02/2014 ECU set up common features
			// 08/04/2014 ECU changed to use the variable
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
	
			setContentView(R.layout.activity_accelerometer);
			// ---------------------------------------------------------------------
			// 24/09/2013 ECU get the resources which will be used with the layers
			// ---------------------------------------------------------------------
			resources = getResources();
			// ---------------------------------------------------------------------
			// 24/09/2013 ECU initialise the layers array
			// ---------------------------------------------------------------------
			layers = new Drawable[NUMBER_OF_LAYERS];
		
			layers[0] = resources.getDrawable(R.drawable.canvas_background);
			layers[2] = resources.getDrawable(R.drawable.grass_background);
			layers[2].setAlpha(opacity);
		
			graphicsView = (ImageView)findViewById (R.id.accelerometer_view);
			// ---------------------------------------------------------------------
			// 16/09/2013 ECU declare the detector for gestures that will be used to handle
			//                pinch open and close actions
			// ---------------------------------------------------------------------     
			scaleGestureDetector = 
					new ScaleGestureDetector(this,new onScaleGestureListener());
	    
			gestureScanner = new GestureDetector(Accelerometer.this,this);
			// --------------------------------------------------------------------- 
			// 06/06/2013 ECU indicate that we want to handle accelerator events
			// ---------------------------------------------------------------------   
			sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	        
			sensorManager.registerListener (this,
											sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
											SensorManager.SENSOR_DELAY_NORMAL);
			// ---------------------------------------------------------------------
			// 23/09/2013 ECU get the metrics for the display
			// ---------------------------------------------------------------------
			DisplayMetrics displayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

			windowWidth  = displayMetrics.widthPixels;
			windowHeight = displayMetrics.heightPixels;
		
			radiusMain = (windowWidth/2) - MAIN_GAP;
			// ---------------------------------------------------------------------	
			// 23/09/2013 ECU set up the scale factor for the accelerometer components
			// ---------------------------------------------------------------------	
			scale = radius/(float) MAXIMUM_VALUE;
			// ---------------------------------------------------------------------	
			// 25/09/2013 ECU set up the scale for the components
			// ---------------------------------------------------------------------	
			scaleComponents = windowWidth / 60;
			// ---------------------------------------------------------------------	
			// 23/09/2013 ECU set the origin for any graphs
			// ---------------------------------------------------------------------	
			graphicsXOrigin = windowWidth / 2;
			graphicsYOrigin = windowHeight - graphicsXOrigin;
			// ---------------------------------------------------------------------	
			// 23/09/2013 ECU create a bitmap for the whole screen
			// 24/09/2013 ECU changed from ARG_4444
			// ---------------------------------------------------------------------	
			windowBitmap = Bitmap.createBitmap(windowWidth,windowHeight, Config.ARGB_8888);
			// ---------------------------------------------------------------------		
			// 23/09/2013 ECU Create a new canvas to draw on, and link it to 
			//                the bitmap that we created above. Any drawing 
			//                operations performed on the canvas will have an immediate
			//                effect on the pixel data of the bitmap.
			// ---------------------------------------------------------------------
			windowCanvas = new Canvas(windowBitmap);
			// ---------------------------------------------------------------------	
			// Fill the entire canvas with a background colour.
			// ---------------------------------------------------------------------
			windowCanvas.drawColor (BACKGROUND_COLOUR);
			// ---------------------------------------------------------------------	
			// 24/09/2013 ECU create an identically sized bitmap for last layer
			// ---------------------------------------------------------------------	
			layerBitmap = Bitmap.createBitmap(windowWidth,windowHeight, Config.ARGB_8888);
			// ---------------------------------------------------------------------	
			// 23/09/2013 ECU Create a new canvas to draw on, and link it to 
			//                the bitmap that we created above. Any drawing 
			//                operations performed on the canvas will have an immediate
			//                effect on the pixel data of the bitmap.
			// ---------------------------------------------------------------------
			layerCanvas = new Canvas(layerBitmap);
			// ---------------------------------------------------------------------	
			// Fill the entire canvas with a background colour.
			// ---------------------------------------------------------------------
			layerCanvas.drawColor(BACKGROUND_COLOUR);
			// ---------------------------------------------------------------------			
			// 23/09/2013 ECU display the bitmap onto the view
			// ---------------------------------------------------------------------	
			graphicsView.setImageBitmap(windowBitmap);
			// ---------------------------------------------------------------------
			// 23/09/2013 ECU set up the paint object
			// ---------------------------------------------------------------------		
			paint = new Paint ();
		
			paint.setColor (Color.WHITE);
			drawACircle (windowCanvas,graphicsXOrigin,graphicsYOrigin,radiusMain,paint);
		
			paint.setColor (triggerArmed ? Color.GREEN : Color.RED);
			drawACircle (windowCanvas,graphicsXOrigin,graphicsYOrigin,20f,paint);
			// ---------------------------------------------------------------------
			// 23/09/2013 ECU declare and then start the refresh handler
			// ---------------------------------------------------------------------
			refreshHandler = new RefreshHandler ();
			refreshHandler.sleep(10000);
			// ---------------------------------------------------------------------
			//graphicsViewCustom = new ImageViewCustom (this);
			//setContentView (graphicsViewCustom);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been recreated after being
			//                destroyed by Android - sp just exit
			// ---------------------------------------------------------------------
			finish ();
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	@Override
	public void onConfigurationChanged (Configuration theNewConfiguration) 
	{
		// -------------------------------------------------------------------------
		// 03/10/2015 ECU created - called when a configuration change occurs
		// -------------------------------------------------------------------------
		super.onConfigurationChanged (theNewConfiguration);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
    public void onDestroy()
    {
		// -------------------------------------------------------------------------
		// 22/10/2015 ECU added
		// -------------------------------------------------------------------------
		super.onDestroy();
    }
	/* ============================================================================= */
	@Override 
	protected void onPause() 
	{
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU unregister the accelerometer listener
		// -------------------------------------------------------------------------
	   	sensorManager.unregisterListener (this); 
	   	super.onPause (); 
	} 
	/* ============================================================================= */
	@Override 
	protected void onResume() 
	{ 
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU register the listener again
		// -------------------------------------------------------------------------
		sensorManager.registerListener (this,
        		sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        		SensorManager.SENSOR_DELAY_NORMAL);
		// -------------------------------------------------------------------------
	   	super.onResume(); 
	} 
	/* ============================================================================ */
	@Override
	public boolean onDown(MotionEvent e)
	{
		
		return false;
	}
	/* ============================================================= */
	@Override
	public void onLongPress(MotionEvent e) 
	{
			
	}
	/* ============================================================= */
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) 
	{
		
		return false;
	}
	/* ============================================================= */
	@Override
	public void onShowPress(MotionEvent e) 
	{	
	}
	/* ============================================================= */
	@Override
	public boolean onSingleTapUp(MotionEvent e) 
	{	
		return false;
	};
	/* ============================================================= */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) 
	{
		int localOpacity = (int)Math.abs((velocityY/5000f)*255f);
		
		if (velocityY > 0)
		{
			opacity += localOpacity;
			if (opacity > 255)
				opacity = 255;
		}
		else
		{
			opacity -= localOpacity;
			
			if (opacity < 0)
				opacity = 0;
		}
				
		layers[2].setAlpha(opacity);
		graphicsView.invalidate();
		
		return false;
	}
	/* =============================================================== */
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
	     scaleGestureDetector.onTouchEvent(event);
	     
	     gestureScanner.onTouchEvent(event);
	     
	     return true;
	}
	/* ============================================================================= */
	public class onScaleGestureListener extends SimpleOnScaleGestureListener 
    {
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU change the defined safe area (a circle of radius
		//                fallCriteria)
		// -------------------------------------------------------------------------
		
		/* ========================================================================= */
		@Override
		public boolean onScale(ScaleGestureDetector detector) 
		{

			float scaleFactor = detector.getScaleFactor();
 
			if (scaleFactor > 1) 
			{
				radius += 10f;
				
				if (radius > radiusMain)
					radius = radiusMain;
			} 
			else 
			{
				radius -= 10f;
				
				if (radius < 0f)
					radius = 0f;
			}
			
			scale = radius/(float) MAXIMUM_VALUE;
			
			generateGraphics (windowCanvas);
				
			return true;
		}
		/* ========================================================================= */
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) 
		{
			return true;
		}
		/* ========================================================================= */
		@Override
		public void onScaleEnd(ScaleGestureDetector detector) 
		{
			
		}
    }
	/* ============================================================================= */
	public void onAccuracyChanged (Sensor sensor,int accuracy)
	{
	}
	/* ============================================================================= */
	public void onSensorChanged(SensorEvent theSensorEvent)
	{
		// -------------------------------------------------------------------------
		// 06/06/2013 ECU only interested in the accelerometer
		// -------------------------------------------------------------------------	
		if (theSensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
		{
			// ---------------------------------------------------------------------
			// 20/09/2013 ECU process the event 
			// ---------------------------------------------------------------------	
			processAccelerometerValues (theSensorEvent.values [0],		// X component
										theSensorEvent.values [1],		// Y component
										theSensorEvent.values [2]);		// Z component
		}
	}
	/* ============================================================================= */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return true;
	}
	/* ============================================================================= */
	private void checkForAFall (float changeX, float changeY, float changeZ)
	{
		// -------------------------------------------------------------------------
		// 06/06/2013 ECU just do a simple check for a fall
		// 23/09/2013 ECU change to use variable rather than static
		//            ECU indicate that the actioning has occurred
		// -------------------------------------------------------------------------
		if (triggerArmed && (changeX > fallCriteria || changeY > fallCriteria || changeZ > fallCriteria))
		{
			// ---------------------------------------------------------------------
			// 23/09/2013 ECU indicate that a fall has occurred
			// ---------------------------------------------------------------------
			Utilities.popToast (getString(R.string.fall_has_occurred),true,Toast.LENGTH_SHORT);
			// ---------------------------------------------------------------------
			// 23/09/2013 ECU indicate that a fall has been actioned
			// ---------------------------------------------------------------------
			triggerArmed = false;
			// ---------------------------------------------------------------------
			waitToRearm (REARM_TIME);
		}
	}
	/* ============================================================================= */
	static void drawACircle (Canvas theCanvas,float theXCentre, float theYCentre, float theRadius,Paint thePaint)
	{
		Paint localPaint = thePaint;
		
		localPaint.setStyle(Paint.Style.FILL);
		theCanvas.drawCircle(theXCentre,theYCentre,theRadius,localPaint);
		localPaint.setStyle(Paint.Style.STROKE);
		localPaint.setColor(Color.BLACK);
		theCanvas.drawCircle(theXCentre,theYCentre,theRadius,localPaint);
		localPaint.setStyle(Paint.Style.FILL);
		
	}
	/* ============================================================================= */
	static void drawALine (Canvas theCanvas,float theXStart,float theYStart,float theXEnd, float theYEnd,Paint thePaint)
	{
		theCanvas.drawLine(theXStart, theYStart, theXEnd, theYEnd, thePaint);
	}
	/* ============================================================================= */
	static void drawTheAcceleratorComponents ()
	{
		Paint localPaint = new Paint ();
		localPaint.setStrokeWidth(STROKE_WIDTH);

		localPaint.setColor(Color.BLACK);
		drawALine (windowCanvas,graphicsXOrigin,graphicsYOrigin,graphicsXOrigin+(changeAccelerometerXDirection*scale),graphicsYOrigin,localPaint);
		localPaint.setColor(Color.RED);
		drawALine (windowCanvas,graphicsXOrigin,graphicsYOrigin,graphicsXOrigin,graphicsYOrigin+(changeAccelerometerYDirection*scale),localPaint);
		localPaint.setColor(Color.BLUE);
		drawALine (windowCanvas,graphicsXOrigin,graphicsYOrigin,graphicsXOrigin+(changeAccelerometerZDirection*scale),graphicsYOrigin+(changeAccelerometerZDirection*scale),localPaint);
		
		float theSign = 1f;
		
		localPaint.setColor(Color.GREEN);
		if (accelerometerXDirection < 0)
		{
			localPaint.setColor(Color.RED);
			theSign = -1f;
		}
		drawACircle (windowCanvas,windowWidth/4,windowHeight/5,accelerometerXDirection*scaleComponents*theSign,localPaint);
		theSign = 1f;
		
		localPaint.setColor(Color.GREEN);
		if (accelerometerYDirection < 0)
		{
			localPaint.setColor(Color.RED);
			theSign = -1f;
		}
		drawACircle (windowCanvas,2*(windowWidth/4),windowHeight/5,accelerometerYDirection*scaleComponents*theSign,localPaint);
		theSign = 1f;
		
		localPaint.setColor(Color.GREEN);
		if (accelerometerZDirection < 0)
		{
			localPaint.setColor(Color.RED);
			theSign = -1f;
		}
		drawACircle (windowCanvas,3*(windowWidth/4),windowHeight/5,accelerometerZDirection*scaleComponents*theSign,localPaint);;
	
	}
	/* ============================================================================= */
	static void drawTheBall(int [] theCoords,int theColour)
	{
  		layerPaint.setColor (theColour);
		layerCanvas.drawCircle(theCoords [X_COORD],theCoords [Y_COORD],20,layerPaint);
		
		graphicsView.invalidate ();
	}
	/* ============================================================================= */
	static void generateGraphics (Canvas theCanvas)
	{		
		windowBitmap.eraseColor(BACKGROUND_COLOUR);
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU set up the paint object
		// -------------------------------------------------------------------------
		paint = new Paint ();
		
		paint.setColor (Color.WHITE);
		drawACircle (windowCanvas,graphicsXOrigin,graphicsYOrigin,radiusMain,paint);
		
		paint.setColor (triggerArmed ? Color.GREEN : Color.RED);
		drawACircle (windowCanvas,graphicsXOrigin,graphicsYOrigin,radius,paint);
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU draw the accelerometer components
		// -------------------------------------------------------------------------
		drawTheAcceleratorComponents ();
		
		layers[1] = new BitmapDrawable(resources,windowBitmap);
		layers[3] = new BitmapDrawable(resources,layerBitmap);
		
		
		layerDrawable = new LayerDrawable(layers);
		// -------------------------------------------------------------------------
		// 24/09/2013 ECU put the display on the physical screen
		// -------------------------------------------------------------------------
		graphicsView.setImageDrawable(layerDrawable);
		
	}
	/* ----------------------------------------------------------------------------- */
	static void generateGraphics (Canvas theCanvas,boolean theBallFlag)
	{
		layerDrawable = new LayerDrawable(layers);
		// -------------------------------------------------------------------------
		// 24/09/2013 ECU put the display on the physical screen
		// -------------------------------------------------------------------------
		graphicsView.setImageDrawable(layerDrawable);
	}
	/* ============================================================================= */
	static int [] moveTheBall (int [] theCoords)
	{
		int [] localBall = theCoords;
		// -------------------------------------------------------------------------
		// 25/09/2013 ECU clear the ball's layer - really sloppy
		// -------------------------------------------------------------------------
		layerBitmap.eraseColor(BACKGROUND_COLOUR);
		
		if (localBall [X_COORD] > windowWidth || localBall [X_COORD] < 0)
  			deltaX = -deltaX;
   		
  		if (localBall [Y_COORD] > windowHeight || localBall [Y_COORD] < 0)
  			deltaY = -deltaY;
  		
  		localBall [X_COORD] += deltaX;
  		localBall [Y_COORD] += deltaY;
  		// -------------------------------------------------------------------------
  		// 25/09/2013 ECU draw the ball in next position
  		// -------------------------------------------------------------------------
  		drawTheBall (localBall,Color.RED);
  		
  		layers[1] = new BitmapDrawable(resources,windowBitmap);
		layers[3] = new BitmapDrawable(resources,layerBitmap);
		
  		layerDrawable = new LayerDrawable(layers);
  		// -------------------------------------------------------------------------
		// 24/09/2013 ECU put the display on the physical screen
  		// -------------------------------------------------------------------------
		//graphicsView.setImageDrawable(layerDrawable);
  		// -------------------------------------------------------------------------
		return localBall;
	}
	/* ============================================================================= */
	void processAccelerometerValues (float theXDirection,float theYDirection,float theZDirection)
	{
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU store the components for future use
		// -------------------------------------------------------------------------
		accelerometerXDirection = theXDirection;
		accelerometerYDirection = theYDirection;
		accelerometerZDirection = theZDirection;
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU work out the change between current and previous values
		// -------------------------------------------------------------------------
		changeAccelerometerXDirection =  accelerometerXDirection - previousAccelerometerXDirection;
		changeAccelerometerYDirection =  accelerometerYDirection - previousAccelerometerYDirection;
		changeAccelerometerZDirection =  accelerometerZDirection - previousAccelerometerZDirection;
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU save the previous values
		// -------------------------------------------------------------------------
		previousAccelerometerXDirection = theXDirection;
		previousAccelerometerYDirection = theYDirection;
		previousAccelerometerZDirection = theZDirection;
		
		generateGraphics (windowCanvas);
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU check for a fall
		// -------------------------------------------------------------------------
		checkForAFall (changeAccelerometerXDirection,changeAccelerometerYDirection,changeAccelerometerZDirection);
	}
	/* ============================================================================= */
	static class RefreshHandler extends Handler
    {
        @Override
        public void handleMessage(Message theMessage) 
        {  
        	ball = moveTheBall (ball);
    		//generateGraphics (windowCanvas,true);
      		
        	sleep (30);
        }
        /* ------------------------------------------------------------------------ */
        public void sleep(long delayMillis)
        {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }
	/* ============================================================================= */
	static void waitToRearm (final int theWaitTime)
	{
		// -------------------------------------------------------------------------
		// 25/09/2013 ECU added to wait a time before rearming the detector
		// -------------------------------------------------------------------------
		 Thread thread = new Thread()
		 {
		        @Override
		        public void run()
		        {
		            try 
		            {
		                sleep(theWaitTime);
		                // ---------------------------------------------------------
		                // 23/09/2013 ECU rearm the fallActioned flag
		                // ---------------------------------------------------------
		                triggerArmed = true;
		                
		            }
		            catch(InterruptedException theException)
		            { 	                 
		            }       
		        }
		    };

		    thread.start();        		
	}
	/* ============================================================================= */
	
	
	/* ============================================================================= */
	public class ImageViewCustom extends ImageView 
	{  
		/* ------------------------------------------------------------------------- */
		// Constructor
		public ImageViewCustom (Context context) 
		{
			super(context);
		}
		/* ------------------------------------------------------------------------- */ 
		// Called back to draw the view. Also called by invalidate().
		@Override
		protected void onDraw(Canvas theCanvas) 
		{
		    generateGraphics(theCanvas);
		    
		    try 
		    {  
		         Thread.sleep(1000);
		    } 
		    catch (InterruptedException theException) 
		    { 
		    	
		    }
		      
		    invalidate();  // Force a re-draw
	
		}
		/* ------------------------------------------------------------------------- */  
		@Override
		public void onSizeChanged(int w, int h, int oldW, int oldH) 
		{    
		}
		/* ------------------------------------------------------------------------- */
	}
	/* ============================================================================= */
}

package com.usher.diboson;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FallsActivity extends DibosonActivity
{
	// =============================================================================
	// 06/06/2013 ECU created
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 02/11/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 18/11/2015 ECU changed to have the sensor control in SensorService rather than
	//                here
	// 24/01/2016 ECU added the button to define the action commands to be handled
	//                when a fall happens
	// =============================================================================
	//private static String TAG = "FallsActivity";
	/* ============================================================================= */
	private static final float FALL_CRITERIA 	= 15.0f;
	private static final int   GAP              = 20;		// 23/09/2013 ECU gap around
															//                safe zone
	private static final int   GRAPH_SCALE		= 5;		// 17/02/2016 ECU added
	private static final int   MAX_VALUES 		= 100;
	private static final int   REARM_TIME		= 30 *1000;	// 23/09/2013 ECU time in ms
															//                before rearmed
	private static final float ROUND_SIZE 		= 15;
	private static final int   STROKE_WIDTH		= 5;		// 23/09/2013 ECU width of pointer 
	private static final int   X_POINTER_COLOUR	= Color.BLACK;
	private static final int   Y_POINTER_COLOUR	= Color.RED;
	private static final int   Z_POINTER_COLOUR	= Color.BLUE;
	// =============================================================================

	/* ============================================================================= */	
			static	ImageView		accelerometerView;					// 20/09/2013 ECU added
			static  Rect 			accelerometerRectangle;
			static  RectF 			accelerometerRectCoordinates;
			static	float 			changeX;
			static	float 			changeY; 
			static	float 			changeZ;
			static	Context			context;							// 18/11/2015 ECU added
			static	boolean 		fallActioned 			= false;	// 23/09/2013 ECU added
	        static 	float 			fallCriteria 			= FALL_CRITERIA;		
	        															// 23/09/2013 ECU added
	private static 	Bitmap 			graphBitmap;
	private static 	Bitmap 			graphsBitmap;
			static	float			graphicsHalfHeight;
			static	float			graphicsHalfWidth;
			static 	int 			graphicsHeight;
			static 	int 			graphicsWidth;
			static 	float 			graphIncrement;
			static 	int 			graphsHeight;
			static  Rect 			graphsRectangle;
			static  RectF 			graphsRectCoordinates;
			static 	ImageView		graphsView;							// 20/09/2013 ECU added
			static 	int 			graphsWidth;
			static 	int 			graphicsXOrigin;
			static 	int 			graphicsYOrigin;
			static 	boolean			paused 					= false;	// 23/09/2013 ECU added - whether processing
																		//                required
																		// 18/11/2015 ECU changed to static
	private static  float	 		previousXValue;
	private static 	float 			previousYValue;
	private static	float 			previousZValue = 0f;
	private static 	int 			previousValuePointer 	= StaticData.NO_RESULT;
					ScaleGestureDetector 
									scaleGestureDetector;				// 16/09/2013 ECU added for creating pinch events
			static  float 			scaleFactor;
			static  SensorEvent 	sensorEvent;						// 17/02/2016 ECU addedf
			static 	TextView 		statusCoordinate;
	private static 	float			valuesX [] 				= new float [MAX_VALUES];
	private static	float 			valuesY [] 				= new float [MAX_VALUES];
	private static	float 			valuesZ [] 				= new float [MAX_VALUES];
	private static 	int 			valuePointer    	 	= 0;
			static	TextView		xCoordinate;
			static	TextView 		yCoordinate;
			static	TextView 		zCoordinate;
	private static 	Bitmap 			workingBitmap,graphsWorkingBitmap;
	private static 	Canvas 			workingCanvas,graphsWorkingCanvas;
	 		static	Paint 			workingPaint;
	/* ============================================================================= */
	
	
	/* ============================================================================= */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 02/11/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 16/02/2014 ECU call up routine to set common activity features
			// 24/01/2016 ECU add the 'true' to get a full screen
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// ---------------------------------------------------------------------		
			// 20/09/2013 ECU have a good tidy up
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_falls);
			setTitle (R.string.falls_version);
			// ---------------------------------------------------------------------
			// 18/11/2015 ECU save the context for future use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 06/06/2013 ECU get the TextView's which will be used for outputting data
			// ---------------------------------------------------------------------  
			xCoordinate       = (TextView) findViewById (R.id.xcoor);
			yCoordinate       = (TextView) findViewById (R.id.ycoor);
			zCoordinate       = (TextView) findViewById (R.id.zcoor);
			statusCoordinate  = (TextView) findViewById (R.id.falls_status);
			// ---------------------------------------------------------------------
			// 18/11/2015 ECU set up relevant colours
			// ---------------------------------------------------------------------
			xCoordinate.setTextColor (X_POINTER_COLOUR);
			yCoordinate.setTextColor (Y_POINTER_COLOUR);
			zCoordinate.setTextColor (Z_POINTER_COLOUR);
			// ---------------------------------------------------------------------
			// 24/01/2016 ECU handle the button for defining the falls action commands
			// ---------------------------------------------------------------------
			((Button) findViewById (R.id.falls_action_command)).setOnClickListener (new View.OnClickListener() 
			{
				@Override
				public void onClick (View theView) 
				{
					// -------------------------------------------------------------
					DialogueUtilities.multilineTextInput (context,
							  							  "Actions when a Fall Occurs",
							  							  context.getString (R.string.action_command_summary),
							  							  5,
							  							  PublicData.storedData.fallsActionCommands,
							  							  Utilities.createAMethod (FallsActivity.class,"SetFallsActionCommand",""),
							  							  null,
							  							  StaticData.NO_RESULT,
							  							  context.getString (R.string.press_to_define_command));
					// -------------------------------------------------------------
				}
			});
			// ---------------------------------------------------------------------
			((Button) findViewById (R.id.falls_action_command)).setOnLongClickListener (new View.OnLongClickListener() 
			{
				@Override
				public boolean onLongClick (View theView) 
				{
					// -------------------------------------------------------------
					try 
					{ 
						// ---------------------------------------------------------
						// 24/01/2016 ECU call up the method that will build the
						//                action command
						// ---------------------------------------------------------
						ActionCommandUtilities.SelectCommand (context,
								Utilities.createAMethod (FallsActivity.class,"SetFallsActionCommand",""));
						// ---------------------------------------------------------
					} 
					catch (Exception theException) 
					{	
					} 
					// -------------------------------------------------------------
					return true;
				}
			});
			// ---------------------------------------------------------------------
			// 20/09/2013 ECU declare the views to the graphics areas
			// --------------------------------------------------------------------- 
			accelerometerView = (ImageView) findViewById (R.id.graphics_image);
			graphsView		  = (ImageView) findViewById (R.id.graph_image);
			// ---------------------------------------------------------------------
			// 20/09/2013 ECU dynamically set up the graphics variables
			// ---------------------------------------------------------------------   
			graphicsWidth		= PublicData.screenWidth - (accelerometerView.getPaddingLeft()*4);
			graphicsHeight	 	= PublicData.screenHeight/3;
			graphicsXOrigin 	= graphicsWidth/2;
			graphicsYOrigin		= graphicsHeight/2;
			graphsWidth 		= PublicData.screenWidth - (graphsView.getPaddingLeft() * 4);
			graphsHeight 		= PublicData.screenHeight/3;
			graphIncrement 		= (float) graphsWidth/ (float) (MAX_VALUES-1);		// graph increment
		   	workingPaint 		= new Paint ();
		   	// ---------------------------------------------------------------------
		   	// 17/02/2016 ECU gewnerate some other variable just once
		   	// ---------------------------------------------------------------------
		   	graphicsHalfHeight = (float) (graphicsHeight/2);
		   	graphicsHalfWidth  = (float) (graphicsWidth/2);
			// ---------------------------------------------------------------------
			// 23/09/2013 ECU changed from 20f
			// --------------------------------------------------------------------- 
			scaleFactor         = (graphicsHeight - GAP) / (2 * fallCriteria);
			// ---------------------------------------------------------------------       
			// 20/09/2013 ECU create the graphics window that will be used for
			//                displaying accelerometer values - this is a
			//                working area
			// ---------------------------------------------------------------------	      
			workingBitmap = Bitmap.createBitmap (graphicsWidth,
												 graphicsHeight, 
												 Config.ARGB_8888); 
			// ---------------------------------------------------------------------
			// 17/02/2016 ECU move some of the variables that used to be created in
			//                the update values method
			// ---------------------------------------------------------------------
		   	accelerometerRectangle = new Rect(0, 0,workingBitmap.getWidth(),workingBitmap.getHeight());
		   	accelerometerRectCoordinates = new RectF (accelerometerRectangle); 
		   	// ---------------------------------------------------------------------
		   	// 17/02/2016 ECU Note - now generate the graphics
		   	// ---------------------------------------------------------------------
			graphBitmap = generateGraphics (workingBitmap);
		   	// ---------------------------------------------------------------------
			// 20/09/2013 ECU set up the graphs area for the graphs
			// ---------------------------------------------------------------------     
			graphsWorkingBitmap = Bitmap.createBitmap (graphsWidth,
													   graphsHeight, 
													   Config.ARGB_8888);
			// ---------------------------------------------------------------------  
			// 17/02/2016 ECU moved some of the variables here from the update values
			//                method
			// ---------------------------------------------------------------------
		   	graphsRectangle = new Rect(0, 0,graphsWorkingBitmap.getWidth(),	graphsWorkingBitmap.getHeight());
		   	graphsRectCoordinates = new RectF (graphsRectangle);  
		   	// ---------------------------------------------------------------------
		   	// 17/02/2016 ECU Note - now generate the graphics
		   	// ---------------------------------------------------------------------
			graphsBitmap = generateGraphsGraphics (graphsWorkingBitmap);
			// ---------------------------------------------------------------------
			// 16/09/2013 ECU declare the detector for gestures that will be used to handle
			//                pinch open and close actions
			// ---------------------------------------------------------------------   
			scaleGestureDetector = 
						new ScaleGestureDetector(this,new onScaleGestureListener());
			// ---------------------------------------------------------------------
			// 18/11/2015 ECU now tell the SensorService to enable the listener
			//                for accelerometer events and to pass through information
			//                about those events
			// 04/12/2015 ECU changed to pass across the method to be called
			// 15/02/2016 ECU pass through SensorManager.SENSOR_DELAY_NORMAL as the
			//                rate at which the sensor will retrieve data
			// 17/02/2016 ECU Note - instead of using the SENSOR_DELAY_ presets it
			//                       is possible to specify the delay in microseconds
			//                       so for 20 updates a second then use
			//                       (1000000/20) = 50000 as the delay but remember
			//                       that this is just an estimate
			// 18/02/2016 ECU changed to use the value stored unless that value is '0'
			//                in which case use the default value
			// ---------------------------------------------------------------------
			// 18/02/2016 ECU default to the basic sample rate
			// ---------------------------------------------------------------------
			int	sampleRateDelay = SensorManager.SENSOR_DELAY_NORMAL;
			// ---------------------------------------------------------------------
			// 18/02/2016 ECU if a non-default sample rate has been set then need to
			//                set the delay in microseconds which is 1000000/rate
			// ---------------------------------------------------------------------
			if (PublicData.storedData.accelerometerSampleRate > 0)
			{
				sampleRateDelay = (1000000 / PublicData.storedData.accelerometerSampleRate);
				// -----------------------------------------------------------------
				// 18/02/2016 ECU indicate to the user that a configured sample rate
				//                is being used
				// -----------------------------------------------------------------
				Utilities.popToast ("The accelerometer will provide data " + 
									PublicData.storedData.accelerometerSampleRate + 
									" times per second, approximately",true);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			SensorService.accelerometerEnablement (true,
												   Utilities.createAMethod (FallsActivity.class,"eventHandler",(Object) null),
												   sampleRateDelay);
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
	public void onBackPressed() 
	{
		super.onBackPressed();
	}
	// =============================================================================
	@Override
    public void onDestroy()
    {
		// -------------------------------------------------------------------------
		// 13/11/2015 ECU although the activity will be destroyed the 'listener'
		//                for the accelerometer will be left running so that a fall
		//                can be detected
		// 18/11/2015 ECU add the following line if you want to stop the listener
		// 04/12/2015 ECU for completeness added the 'null'
		// 15/02/2016 ECU added the 0 for completeness - refers to the data rate which
		//                is not used in this case
		// -------------------------------------------------------------------------
		//SensorService.accelerometerEnablement (false,null,0);
		// -------------------------------------------------------------------------
		// 22/10/2015 ECU added
		// -------------------------------------------------------------------------
		super.onDestroy();
    }
	/* ============================================================================= */	
	@Override 
	protected void onResume() 
	{ 
	   	super.onResume(); 
	} 
	/* ============================================================================= */
	@Override 
	protected void onPause() 
	{
	   	super.onPause(); 
	} 
	/* ============================================================================== */
	private static void checkForAFall (Context theContext,float changeX, float changeY, float changeZ)
	{
		// -------------------------------------------------------------------------
		// 06/06/2013 ECU just do a simple check for a fall
		// 23/09/2013 ECU change to use variable rather than static
		//            ECU indicate that the actioning has occurred
		// 18/11/2015 ECU changed to static
		//			  ECU added theContext as an argument
		// -------------------------------------------------------------------------
		if (!fallActioned && (changeX > fallCriteria || 
							  changeY > fallCriteria || 
							  changeZ > fallCriteria))
		{
			// ---------------------------------------------------------------------
			// 14/03/2014 ECU tell the user that the fall has happened -
			//                have some initial silence
			//            ECU read the appropriate file for reading
			// 24/01/2016 ECU check if any action commands have been stored
			// ---------------------------------------------------------------------
			if (PublicData.storedData.fallsActionCommands == null)
			{
				String fallenMessage = Utilities.ReadAFile ("FALLEN");
			
				if (fallenMessage != null)
				{
					Utilities.SpeechSilence (3000);
					Utilities.SpeakAPhrase  (theContext,fallenMessage);
				}								
				// -----------------------------------------------------------------
				// 20/09/2013 ECU take the phone number from resources
				// -----------------------------------------------------------------
				Utilities.makePhoneCall (theContext,theContext.getString(R.string.phone_number_ed));
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 24/01/2016 ECU action the commands which have been defined
				// -----------------------------------------------------------------
				Utilities.actionHandler(theContext, PublicData.storedData.fallsActionCommands);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------			
			// 23/09/2013 ECU indicate that a fall has occurred
			// 23/02/2015 ECU changed from Toast.SHORT to default
			// ---------------------------------------------------------------------
			Utilities.popToast (theContext.getString (R.string.fall_has_occurred),true);
			// ---------------------------------------------------------------------
			// 23/09/2013 ECU indicate that a fall has been actioned
			// ---------------------------------------------------------------------
			fallActioned = true;
			// ---------------------------------------------------------------------
			// 23/09/2013 ECU start the timer to rearm the detector
			// ---------------------------------------------------------------------
			waitToRearm (REARM_TIME);
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	static void drawACircle (Canvas theCanvas,float theRadius,int theColour)
	{
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU added to draw a circle of the specified radius
		// 18/11/2015 ECU added theColour as an argument
		// -------------------------------------------------------------------------
	    workingPaint.setColor (theColour);  
	        
	    theCanvas.drawCircle (graphicsXOrigin,graphicsYOrigin,theRadius,workingPaint);
	    
	}
	/* ============================================================================= */
	static void drawGraph (Canvas this_canvas,
			               float [] values,
			               int offset,
			               int colour,
			               int inputPointer)
	{
		workingPaint.setColor (colour);
		workingPaint.setStrokeWidth (STROKE_WIDTH);
		
		for (int i=0; i< (valuePointer-1); i++)
	   	{
	   		this_canvas.drawLine ((float) i*graphIncrement, 
	   							 offset - values [i] * GRAPH_SCALE,
	   							 (float) (i+1)*graphIncrement, 
	   							 offset - values [i+1] * GRAPH_SCALE,
	   							 workingPaint);	   		
	   	}
	}
	/* ============================================================================= */
	static void drawGraphsBackground ()
	{
		graphsWorkingCanvas.drawARGB (0, 0, 0, 0);
		workingPaint.setColor (Color.GRAY);
    	graphsWorkingCanvas.drawRect (graphsRectCoordinates,workingPaint);
	}
   	/* ============================================================================= */
	static void drawPointer (Canvas this_canvas, 
			                        float startX, float startY,
			                        float stopX, float stopY,
		                            int pointerColour)
	{	
		workingPaint.setColor (pointerColour);
		workingPaint.setStrokeWidth (STROKE_WIDTH);
				
		this_canvas.drawLine(graphicsXOrigin + startX, 
				             graphicsYOrigin + startY, 
				             graphicsXOrigin + stopX, 
				             graphicsYOrigin + stopY, 
				             workingPaint);
	}
	/* ============================================================================= */
	static void drawAxes (Canvas theCanvas)
	{
		// -------------------------------------------------------------------------
		// 20/09/2013 ECU display the X and Y axes
		// -------------------------------------------------------------------------
	    // 23/09/2013 ECU changed from WHITE
	    // ------------------------------------------------------------------------- 
		workingPaint.setStrokeWidth(2);
	    workingPaint.setColor(Color.BLACK);  
	    // -------------------------------------------------------------------------    
	    // 20/09/2013 ECU draw the X and Y axes
	    // -------------------------------------------------------------------------
	    theCanvas.drawLine (graphicsHalfWidth,(float)0.0,graphicsHalfWidth,(float)graphicsHeight,workingPaint);
	    theCanvas.drawLine ((float)0.0,graphicsHalfHeight,(float)graphicsWidth,graphicsHalfHeight,workingPaint);       
	}
	/* ============================================================================= */
    static void drawAccelerometerBackground ()
    {
    	// -------------------------------------------------------------------------
    	// 20/09/2013 ECU just draws the background of the two graphics windows
    	// -------------------------------------------------------------------------
	    // 20/09/2013 ECU fill the specified canvas with the colour
	    // -------------------------------------------------------------------------
	    workingCanvas.drawARGB (0, 0, 0, 0);
	    // -------------------------------------------------------------------------
	    // 20/09/2013 ECU set colour of the background
	    // -------------------------------------------------------------------------
	    workingPaint.setColor(Color.GRAY);
	    
	    workingCanvas.drawRoundRect(accelerometerRectCoordinates, ROUND_SIZE, ROUND_SIZE, workingPaint);
    }
    // =============================================================================
    public static void eventHandler (Object theSensorEventAsObject)
    {
    	// -------------------------------------------------------------------------
    	// 18/11/2015 ECU created to be called by the SensorService to handle 
    	//                accelerometer events
    	// 04/12/2015 ECU changed because data is passed as an object
    	// -------------------------------------------------------------------------
		// 06/06/2013 ECU only interested in the accelerometer
		// 23/09/2013 ECU include the 'paused' variable
		// -------------------------------------------------------------------------
		if (!paused)
		{
			// ---------------------------------------------------------------------
			// 04/12/2015 ECU cast the object
			// ---------------------------------------------------------------------
			sensorEvent = (SensorEvent) theSensorEventAsObject;
			// ---------------------------------------------------------------------
			// 06/06/2013 ECU process the input values
			// ---------------------------------------------------------------------
			updateTheValues (sensorEvent.values [0],sensorEvent.values [1],sensorEvent.values [2]);
			// ---------------------------------------------------------------------
		}
    	// -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    static Bitmap generateGraphics (Bitmap bitmap)
    {
    	// -------------------------------------------------------------------------
		// 20/09/2013 ECU code to get bitmap onto screen
	    // -------------------------------------------------------------------------
		Bitmap localBitmap = Bitmap.createBitmap (bitmap.getWidth(),
	    bitmap.getHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas (localBitmap);
	    
	    workingCanvas = canvas;
	 
	    final Paint paint = new Paint();
	    final Rect rect = new Rect (0, 0, 
	    							bitmap.getWidth(), 
	    							bitmap.getHeight());
	    // -------------------------------------------------------------------------
	    // 20/09/2013 ECU get the little rounded cornered outside
	    // -------------------------------------------------------------------------
	    drawAccelerometerBackground ();
	    
	    drawAxes (canvas);  
	 
	    canvas.drawBitmap (bitmap, rect, rect, paint);
	
  	    return localBitmap;
	   }
	/* ============================================================================= */
	Bitmap generateGraphsGraphics (Bitmap bitmap)
    {
		// -------------------------------------------------------------------------
		// 20/09/2013 ECU code to get bitmap onto screen
	    // -------------------------------------------------------------------------
		Bitmap localBitmap = Bitmap.createBitmap(bitmap.getWidth(),
	    bitmap.getHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(localBitmap);
	    
	    graphsWorkingCanvas = canvas;
	 
	    final Paint paint = new Paint();
	    final Rect rect = new Rect(0, 0, 
	    							bitmap.getWidth(), 
	    							bitmap.getHeight());
	    
	    final RectF rectF = new RectF(rect);   
	    // -------------------------------------------------------------------------
	    // 20/09/2013 ECU draw the rounded rectangle
	    // -------------------------------------------------------------------------
	    paint.setAntiAlias (true);
	    canvas.drawARGB (0, 0, 0, 0);
	    paint.setColor (Color.GRAY);
	    
	    canvas.drawRoundRect(rectF, ROUND_SIZE, ROUND_SIZE, paint);   
	    	
	    graphsWorkingCanvas.drawARGB(0, 0, 0, 0);
	    
	    graphsWorkingCanvas.drawRoundRect(rectF, ROUND_SIZE, ROUND_SIZE, paint);
	 
	    canvas.drawBitmap(bitmap, rect, rect, paint);
	
	    return localBitmap;
    }
	/* ============================================================================= */
	private static void storeValues (float theXValue, float theYValue, float theZValue)
	{
		// -------------------------------------------------------------------------
		// 06/06/2013 ECU remember where the value is stored in the array
		// -------------------------------------------------------------------------
		previousValuePointer = valuePointer;
		// -------------------------------------------------------------------------
		// 06/06/2013 ECU check if the array is full
		// -------------------------------------------------------------------------
		if ((valuePointer < MAX_VALUES))
		{
			// ---------------------------------------------------------------------
			// 06/06/2013 ECU OK just to put in the array and increase storage pointer
			// ---------------------------------------------------------------------
			valuesX [valuePointer] = theXValue;
			valuesY [valuePointer] = theYValue;
			valuesZ [valuePointer] = theZValue;
			
			valuePointer++;
		}
		else
		{
			// ---------------------------------------------------------------------
			// 06/06/2013 ECU the array is full so just shift the stored values down by
			//                one element - yes this is crap code but leave it until
			//                everything works
			// ---------------------------------------------------------------------	
			for (int index = 0; index < MAX_VALUES-1; index++)
			{
				valuesX[index] = valuesX[index+1];
				valuesY[index] = valuesY[index+1];
				valuesZ[index] = valuesZ[index+1];
			}
			
			valuesX [MAX_VALUES-1] = theXValue;
			valuesY [MAX_VALUES-1] = theYValue;
			valuesZ [MAX_VALUES-1] = theZValue;
			
			valuePointer = MAX_VALUES;
		}
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU indicate the falls status	
		// -------------------------------------------------------------------------
		statusCoordinate.setText ("Status : " + (fallActioned ? "Triggered" : "Armed") + 
								  "\nFall Criterion : " + String.format ("%.2f",fallCriteria));		
	}
	/* ============================================================================= */
	private static void updateTheValues (float theXValue, float theYValue, float theZValue)
	{
		// -------------------------------------------------------------------------
		// 06/06/2013 ECU work out the change since the last reading
		// 18/11/2015 ECU changed to static
		// -------------------------------------------------------------------------
		changeX = Math.round (theXValue - previousXValue); 
		changeY = Math.round (theYValue - previousYValue); 
		changeZ = Math.round (theZValue - previousZValue);
				
		if (previousValuePointer != StaticData.NO_RESULT)
		{
			xCoordinate.setText ("X Component : " + String.format ("%+9.4f",theXValue) + "    X Change : " + String.format ("%+3.1f",changeX));
			yCoordinate.setText ("Y Component : " + String.format ("%+9.4f",theYValue) + "    Y Change : " + String.format ("%+3.1f",changeY));
			zCoordinate.setText ("Z Component : " + String.format ("%+9.4f",theZValue) + "    Z Change : " + String.format ("%+3.1f",changeZ));
			// ---------------------------------------------------------------------
			// 06/06/2013 ECU check if the change in values may indicate a fall
			// ---------------------------------------------------------------------
			checkForAFall (context,changeX,changeY,changeZ); 
		}
		// -------------------------------------------------------------------------
		// 06/06/2013 ECU store the input values into the relevant arrays
		// -------------------------------------------------------------------------
		storeValues (theXValue,theYValue,theZValue);
		// -------------------------------------------------------------------------	 
		// 20/09/2013 ECU remember the values
		// -------------------------------------------------------------------------
		previousXValue = theXValue;
		previousYValue = theYValue;
		previousZValue = theZValue;
		// -------------------------------------------------------------------------
		// 06/06/2013 ECU draw the changes in the acceleration as pointers on a graph
		// -------------------------------------------------------------------------
		drawAccelerometerBackground ();
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU draw the safe area and co-ordinates
		// 18/11/2015 ECU add the colour as an argument
		// -------------------------------------------------------------------------
		drawACircle (workingCanvas,fallCriteria * scaleFactor,Color.WHITE);
		drawAxes (workingCanvas);
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU draw the actual pointers
		// 18/11/2015 ECU changed to use ?_POINTER_COLOUR statics
		// -------------------------------------------------------------------------		
		drawPointer (workingCanvas,0f,0f,changeX * scaleFactor,0f,X_POINTER_COLOUR);
		drawPointer (workingCanvas,0f,0f,0f,changeY*scaleFactor,Y_POINTER_COLOUR);		
		drawPointer (workingCanvas,0f,0f,changeZ*scaleFactor,changeZ*scaleFactor,Z_POINTER_COLOUR);
	    // -------------------------------------------------------------------------
		// 23/09/2013 ECU draw the background for the graphs
		// -------------------------------------------------------------------------
		drawGraphsBackground ();
		// -------------------------------------------------------------------------
	    // 20/09/2013 ECU now generate the graphs from the stored arrays
		// -------------------------------------------------------------------------
	    int graphsGap = graphsHeight / 4;
	    
		drawGraph (graphsWorkingCanvas,valuesX,graphsGap,  X_POINTER_COLOUR,valuePointer);
		drawGraph (graphsWorkingCanvas,valuesY,graphsGap*2,Y_POINTER_COLOUR,valuePointer);
		drawGraph (graphsWorkingCanvas,valuesZ,graphsGap*3,Z_POINTER_COLOUR,valuePointer);
		// -------------------------------------------------------------------------	   
		// 20/09/2013 ECU update the physical screen with the generated bitmaps
		// -------------------------------------------------------------------------
		accelerometerView.setImageBitmap (graphBitmap);
        graphsView.setImageBitmap (graphsBitmap);	
	}
	/* ============================================================================= */
	static void waitToRearm (final int theWaitTime)
	{
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU added to wait a time before rearming the detector
		// -------------------------------------------------------------------------
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				try 
				{
					sleep (theWaitTime);
					// -------------------------------------------------------------
					// 23/09/2013 ECU rearm the fallActioned flag
		            // -------------------------------------------------------------
					fallActioned = false;          
				}
				catch(InterruptedException theException)
				{ 	                 
				}       
			}
		};
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU start up the defined thread
		// -------------------------------------------------------------------------
		thread.start();        		
	}
	/* ============================================================================= */
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
	     scaleGestureDetector.onTouchEvent(event);
	     return true;
	}
	/* ============================================================================= */
	public class onScaleGestureListener extends SimpleOnScaleGestureListener 
    {
		// -------------------------------------------------------------------------
		// 23/09/2013 ECU change the defined safe area (a circle of radius
		//                fallCriteria)
		// -------------------------------------------------------------------------
		@Override
		public boolean onScale(ScaleGestureDetector detector) 
		{
			float scaleFactor = detector.getScaleFactor();
 
			if (scaleFactor > 1) 
			{
				// -----------------------------------------------------------------
				// 23/09/2013 ECU decrease the size of the safe area
				// -----------------------------------------------------------------
				if (fallCriteria < 30f)
					fallCriteria += 0.2f;
			} 
			else 
			{
				// -----------------------------------------------------------------
				// 23/09/2013 ECU increase the size of the safe area
				// -----------------------------------------------------------------
				if (fallCriteria > 5f)
					fallCriteria -= 0.2f;	
			}
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
	
	// =============================================================================
	public static void SetFallsActionCommand (String theActionCommands)
	{
		// -------------------------------------------------------------------------
		// 24/01/2016 ECU created to take the action commands and store away
		// -------------------------------------------------------------------------
		PublicData.storedData.fallsActionCommands = theActionCommands;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

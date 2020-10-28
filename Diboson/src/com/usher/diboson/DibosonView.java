package com.usher.diboson;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;

public class DibosonView extends View implements OnGestureListener
{
	/* ========================================================= */
	// 18/01/2014 ECU took out Scroller code which was here just
	//                for test purposes
	/* ========================================================= */
	//public static String TAG	=	"DibosonView";
	/* ========================================================= */
	public  static final int	EDGE 			= 3;
	public  static final int	GAP				= 50;		// 18/01/2014 ECU added - gap around
															//                viewing window
	public  static final int	MAXIMUM_SIZE 	= 40;		// 18/01/2014 ECU changed from 20
	private static final int 	RECTANGLE_BORDER  
												= 3 * GAP;	// 20/11/2016 ECU border around the rectangle 
	public  static int			TEXT_SCALE		= 15;		
	/* ========================================================= */
		   float 			adjustment;					// 18/01/2014 ECU moved here
		   Message			audioMessage;				// 20/11/2016 ECU added
		   int  			endIndex;					 
		   GestureDetector 	gestureDetector;			// 26/10/2013 ECU added
		   String			kHzString;					
		   int 				lengthOfIncomingData;		// 18/01/2014 ECU moved here
		   int			    maximumSlot = 0;
	// ---------------------------------------------------------	   
    // 20/10/2013 ECU declare the frequencies of the open strings of a guitar
	// 17/11/2016 ECU remove 'openStrings'
	// ----------------------------------------------------------	   
	//	   float []			openStrings = {	82.3f,110.0f,146.8f,196.0f,246.94f,329.63f};
		   Paint			paintBorder;				
		   Paint  			paintData;					
		   Paint			paintMarks;					
		   Paint			paintMax;					
		   Paint			paintRectangle;				
		   int 				rectangleWidth;				// 18/01/2014 ECU moved here
		   int 				rectangleHeight;			// 18/01/2014 ECU moved here
		   int 				rectangleXOrigin;			// 18/01/2014 ECU moved here
		   int				rectangleYOrigin;			// 18/01/2014 ECU moved here
		   int				startIndex;					// 18/01/2014 ECU moved here
		   int				textSize = 50;
		   double			volumeMaximum;				// 20/11/2016 ECU added
	static Bitmap			windowBitmap;
	static Canvas 			windowCanvas;
	static int				windowHeight;	
	static int 				windowWidth;	
		   float 			xCoordinate;				 
		   float            xIncrement;
		   float			xPosition;					
		   float 			yCoordinate;
		   double 			yMaximum;
		   float 			yScale;						// 18/01/2014 ECU added - scale factor for y
	// =============================================================================
	public DibosonView (Context context, AttributeSet attrs) 
	{
		super(context,attrs);
		// -------------------------------------------------------------------------
		// 18/01/2014 ECU add the various Paint definitions
		// -------------------------------------------------------------------------
		paintRectangle = new Paint ();
		paintRectangle.setStyle (Paint.Style.FILL);
		paintRectangle.setColor (Color.WHITE);
		
		paintBorder = new Paint ();
		paintBorder.setStyle(Paint.Style.STROKE);
		paintBorder.setStrokeWidth(EDGE);
		paintBorder.setColor(Color.BLACK);
		
		paintMarks = new Paint ();	
		paintMarks.setStyle (Paint.Style.FILL);		
		paintMarks.setStrokeWidth (2);					
		
		paintData = new Paint ();	
		paintData.setColor(Color.BLUE);
		
		paintMax = new Paint ();
		paintMax.setColor (Color.WHITE);
		paintMax.setStrokeWidth (3);
		paintMax.setStyle (Paint.Style.FILL);
		// -------------------------------------------------------------------------
		// 26/10/2013 ECU initialise the gesture detector
		// -------------------------------------------------------------------------
		gestureDetector = new GestureDetector(context,(OnGestureListener) this);
		// -------------------------------------------------------------------------

	}
	// =============================================================================
	@SuppressWarnings("SuspiciousNameCombination")
	@Override
	protected void onDraw (Canvas theCanvas) 
	{		
		// -------------------------------------------------------------------------
		// 18/10/2013 ECU called to draw the view - also called after an invalidate ();
		// 18/01/2014 ECU take out the setting of the canvas - just use the background
		//                defined in the layout
		// -------------------------------------------------------------------------
		// 19/10/2013 ECU draw a window in which the analysed frequencies will be
		//                displayed. In the x direction there will be a GAP/2 each side
		//				  and it will be centred vertically.
		// 20/11/2016 ECU changed to allow space for the TextView's that display frequency
		//                and volume
		// 21/11/2016 ECU changed to base the size of the rectangle on the height
		//                rather than the width because now need space below it
		//                for displaying information
		// 22/11/2016 ECU Note - the onDraw seems to take ~ 20 mS to perform - it
		//                       is somewhat dependent on the length of data lines
		//                       being drawn
		// 05/07/2020 ECU change 'theCanvas.getWidth / theCanvas.getHeight ()' to
		//                just 'getWidth / get Height ()'
		// -------------------------------------------------------------------------
		rectangleWidth		= (getHeight () * 2) /3;
		// -------------------------------------------------------------------------
		// 21/11/2016 ECU check if this exceeds the width
		// -------------------------------------------------------------------------
		if (rectangleWidth > getWidth())
		{
			rectangleWidth = getWidth();
		}
		// -------------------------------------------------------------------------
		// 21/11/2016 ECU now adjust the width to have a border
		// -------------------------------------------------------------------------
		rectangleWidth  	= rectangleWidth - RECTANGLE_BORDER;	
		// -------------------------------------------------------------------------
		// 21/11/2016 ECU Note - want a 'square'
		// -------------------------------------------------------------------------
		rectangleHeight 	= rectangleWidth;
		// -------------------------------------------------------------------------
		// 21/11/2016 ECU Note - set up the origin of the 'square'
		// -------------------------------------------------------------------------
		rectangleXOrigin 	= (getWidth() - rectangleWidth) / 2;
		rectangleYOrigin 	= RECTANGLE_BORDER/2; 
		// -------------------------------------------------------------------------
		// 18/11/2016 ECU draw a basic frame within which the data will be shown
		// -------------------------------------------------------------------------
		drawTheFrame (theCanvas);
		// -------------------------------------------------------------------------
		// 18/11/2016 ECU now display the data
		// -------------------------------------------------------------------------
		drawTheData (theCanvas);
		// -------------------------------------------------------------------------	
		// 20/10/2013 ECU do the normal onDraw
		// -------------------------------------------------------------------------
		super.onDraw (theCanvas);
		// -------------------------------------------------------------------------
		// 20/11/2016 ECU tell the caller to continue progressing
		// -------------------------------------------------------------------------
		audioMessage = AudioAnalyser.refreshHandler.obtainMessage (StaticData.MESSAGE_DATA,maximumSlot,0,volumeMaximum);
		AudioAnalyser.refreshHandler.sendMessage (audioMessage);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public boolean onTouchEvent (MotionEvent event) 
	{
		// -------------------------------------------------------------------------
		// 26/10/2013 ECU handle the motion event within this class
		// -------------------------------------------------------------------------
	    gestureDetector.onTouchEvent (event);
	     // ------------------------------------------------------------------------
	    return true;
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
	    super.onMeasure (widthMeasureSpec, heightMeasureSpec);
	}
	// =============================================================================
	@Override
    public void onSizeChanged (int w, int h, int oldw, int oldh)
	{
		// -------------------------------------------------------------------------
        super.onSizeChanged (w, h, oldw, oldh);
        // -------------------------------------------------------------------------
        windowWidth = w;
        windowHeight = h;
        // -------------------------------------------------------------------------
    }
	// =============================================================================
	@Override
	public boolean onDown (MotionEvent event) 
	{
		return false;
	}
	// =============================================================================
	@Override
	public boolean onFling (MotionEvent event1, 
							MotionEvent event2, 
							float velocityX,
							float velocityY) 
	{
		// -------------------------------------------------------------------------
		// 18/01/2014 ECU adjust the frequency depending on direction
		// -------------------------------------------------------------------------
		if ((event2.getX () - event1.getX ()) > 0)
		{
			AudioAnalyser.IncrementFrequency ();
		}
		else
		{
			AudioAnalyser.DecrementFrequency ();
		}
		// -------------------------------------------------------------------------
		// 26/10/2013 ECU cause a refresh of the view
		// -------------------------------------------------------------------------
		invalidate ();
		// -------------------------------------------------------------------------
		return false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onLongPress (MotionEvent event) 
	{
			
	}
	// =============================================================================
	@Override
	public boolean onScroll (MotionEvent e1, MotionEvent e2, float distanceX,
							 float distanceY) 
	{	
		return false;
	}
	// =============================================================================
	@Override
	public void onShowPress (MotionEvent e) 
	{				
	}
	// =============================================================================
	@Override
	public boolean onSingleTapUp (MotionEvent e) 
	{
		// -------------------------------------------------------------------------
		AudioAnalyser.resetScale = !AudioAnalyser.resetScale;
		Utilities.popToast ("Scale " + (AudioAnalyser.resetScale ? StaticData.BLANK_STRING : "not ") + "being reset");
		return false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void drawTheData (Canvas theCanvas)
	{
		// -------------------------------------------------------------------------
		// 20/10/2013 ECU only draw a graph if there is data to process
		// 18/11/2016 ECU see the ERROR above which also applies here
		//            ECU seems that is OK but the average frequency seems to be twice
		//                what it should be.
		// -------------------------------------------------------------------------
		if (AudioAnalyser.magnitudes != null)
		{
			// ---------------------------------------------------------------------
			// 19/10/2013 ECU now process the audio signal
			// 18/01/2014 ECU changed histogram colour from YELLOW
			// ---------------------------------------------------------------------
			// 18/01/2014 ECU work out the x increment so that the whole histogram
			//                can fit into the window
			// ---------------------------------------------------------------------
			lengthOfIncomingData =  AudioAnalyser.magnitudes.length; 
					
			xIncrement = (float) rectangleWidth / (float) lengthOfIncomingData;
			// ---------------------------------------------------------------------
			// 18/01/2014 ECU adjust because may be viewing smaller range of frequencies
			// ---------------------------------------------------------------------
			xIncrement = xIncrement * ((float)(AudioAnalyser.SAMPLING_RATE/2 / (float)(AudioAnalyser.frequencyEnd - AudioAnalyser.frequencyStart)));	
			// ---------------------------------------------------------------------
			// 18/01/2014 ECU make sure that there are no gaps between bars
			// ---------------------------------------------------------------------
			paintData.setStrokeWidth (xIncrement);
			// ---------------------------------------------------------------------
			// 20/10/2013 ECU find the maximum amplitude and hence the scale factor
			// ---------------------------------------------------------------------
			yMaximum = 0;
			// ---------------------------------------------------------------------
			// 20/11/2016 ECU remember the unscaled volume for this pass
			// ---------------------------------------------------------------------
			volumeMaximum = 0;
			// ---------------------------------------------------------------------
			// 18/01/2014 ECU added the use of start... and end....
			// ---------------------------------------------------------------------
			startIndex = (int)((float) AudioAnalyser.frequencyStart / AudioAnalyser.resolution);
			endIndex   = (int)((float) AudioAnalyser.frequencyEnd / AudioAnalyser.resolution);
					
			for (int theEntry = startIndex; theEntry < endIndex; theEntry++)
			{
				if (AudioAnalyser.magnitudes [theEntry] > yMaximum)
				{
					yMaximum = AudioAnalyser.magnitudes [theEntry];
					
					maximumSlot 	= theEntry - startIndex;
					// -------------------------------------------------------------
					// 20/11/2016 ECU remember the maximum volume this pass because
					//                yMaximum is scaled before it can be passed back
					//                to AudioAnalyser
					// -------------------------------------------------------------
					volumeMaximum 	= yMaximum;
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
			// 20/10/2013 ECU check whether auto scaling is required 
			// ---------------------------------------------------------------------
			if (yMaximum > AudioAnalyser.yMaximum)
				AudioAnalyser.yMaximum = yMaximum;
					
			if (!AudioAnalyser.resetScale)
				yMaximum = AudioAnalyser.yMaximum ;
			// ---------------------------------------------------------------------
			// 18/01/2014 ECU get ready to display the actual data
			// ---------------------------------------------------------------------
			yScale = (float) yMaximum / (float) rectangleHeight;
					
			xCoordinate = (float) rectangleXOrigin;
			// ---------------------------------------------------------------------
			// 18/01/2014 ECU now draw the actual incoming data
			// ---------------------------------------------------------------------
			for (int theEntry = startIndex; theEntry < endIndex ; theEntry++)
			{
				yCoordinate = (float)rectangleYOrigin + (float)rectangleHeight - ((float) AudioAnalyser.magnitudes[theEntry] / yScale);
						
				theCanvas.drawLine (xCoordinate,rectangleYOrigin+rectangleHeight,xCoordinate,yCoordinate,paintData);
					
				xCoordinate += xIncrement;
			}
			// ---------------------------------------------------------------------
			// 20/10/2013 ECU indicate the maximum value for this buffer
			// 22/10/2013 ECU change way maximum is highlighted
			// 18/01/2014 ECU changed colour from WHITE and increase size
			// ---------------------------------------------------------------------
			theCanvas.drawLine(rectangleXOrigin + maximumSlot*xIncrement,
							   rectangleYOrigin - (float)(2 * EDGE),
							   rectangleXOrigin + maximumSlot * xIncrement,
							   rectangleYOrigin - (float) ((2 * EDGE)+ MAXIMUM_SIZE),paintMax);
			// ---------------------------------------------------------------------		
			// 20/10/2013 ECU display the frequencies of a guitar's open strings
			// 24/10/2013 ECU sort out the resolution at this point rather than at the declaration
			// 18/01/2014 ECU take out the display of the guitar 'open' strings
			// ---------------------------------------------------------------------
			// 18/01/2014 ECU removed
			// ======================
			//paint.setColor (Color.BLACK);
			//paint.setStrokeWidth (2);					// 18/01/2014 ECU changed from 1
			//
			//for (int index=0; index < openStrings.length; index++)
			//{	
			//	theCanvas.drawLine (rectangleXOrigin + (openStrings[index]/AudioAnalyser.resolution)*xIncrement,
			//						rectangleYOrigin,rectangleXOrigin + (openStrings[index]/AudioAnalyser.resolution)*xIncrement,
			//						rectangleYOrigin+rectangleHeight,paint);
			//}
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	void drawTheFrame (Canvas theCanvas)
	{
		// -------------------------------------------------------------------------
		// 18/11/2016 ECU created to draw the basic frame for the data
		// -------------------------------------------------------------------------
		// 18/01/2014 ECU set text size for the lettering
		// -------------------------------------------------------------------------
		textSize = theCanvas.getWidth () / (TEXT_SCALE * 3);
		paintMarks.setTextSize (textSize); 
		// -------------------------------------------------------------------------	
		// 18/01/2014 ECU draw the actual window
		// -------------------------------------------------------------------------
		theCanvas.drawRect (rectangleXOrigin, 
				            rectangleYOrigin, 
				            rectangleXOrigin + rectangleWidth, 
				            rectangleYOrigin + rectangleHeight, paintRectangle);
		// -------------------------------------------------------------------------
		// 20/10/2013 ECU draw a border around the rectangle
		// -------------------------------------------------------------------------
		theCanvas.drawRect (rectangleXOrigin - EDGE, 
						    rectangleYOrigin - EDGE, 
						    rectangleXOrigin + rectangleWidth + EDGE, 
						    rectangleYOrigin + rectangleHeight + EDGE, paintBorder);
		// -------------------------------------------------------------------------
		// 18/01/2014 ECU display lines to mark frequencies
		// 18/01/2014 ECU want to display the KHz markers
		// 18/11/2016 ECU ERROR - this is wrong because it is not scaled to the screen
		//                        - do not understand why it has never shown up as an
		//                        error in the past
		// -------------------------------------------------------------------------
		int  numberOfSteps = (AudioAnalyser.frequencyEnd - AudioAnalyser.frequencyStart) / 1000;
		xIncrement = (rectangleWidth / numberOfSteps);
				
		for (int index = 0; index <= numberOfSteps; index++)
		{	
			// ---------------------------------------------------------------------
			// 18/01/2014 ECU draw a vertical line to mark the frequency - each KHz
			// ---------------------------------------------------------------------
			paintMarks.setColor (Color.YELLOW);
					
			xPosition = rectangleXOrigin + ((float)index * xIncrement);
					
			theCanvas.drawLine (xPosition,rectangleYOrigin,xPosition,rectangleYOrigin+rectangleHeight,paintMarks);
			// ---------------------------------------------------------------------
			// 18/01/2014 ECU mark the values of the KHz lines
			// ---------------------------------------------------------------------
			paintMarks.setColor (Color.BLACK);
			// ---------------------------------------------------------------------
			// 18/01/2014 ECU build in an adjustment to centre the text against the line
			// ---------------------------------------------------------------------
			kHzString = StaticData.BLANK_STRING + (AudioAnalyser.frequencyStart + (index * 1000)) / 1000;
					
			float [] width = new float [kHzString.length()];
			// ---------------------------------------------------------------------
			// 18/01/2014 ECU get the width of each character in the string
			// ---------------------------------------------------------------------
			paintMarks.getTextWidths(kHzString, width);
			// ---------------------------------------------------------------------
			// 18/01/2014 ECU now work out the required adjustment to try and centre the numbers
			// ---------------------------------------------------------------------
			adjustment = 0;
					
			for (int theIndex = 0; theIndex < width.length; theIndex++)
				adjustment += (width [theIndex] / 2f);
			// ---------------------------------------------------------------------
			// 18/01/2014 ECU after all this flapping around draw the text
			// ---------------------------------------------------------------------
			theCanvas.drawText (kHzString,
								xPosition - adjustment,
								rectangleYOrigin + rectangleHeight + (textSize + EDGE), paintMarks);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 18/01/2014 ECU display the KHz text
		// -------------------------------------------------------------------------
		theCanvas.drawText ("KHz",
							rectangleXOrigin + (rectangleWidth / 2),
							rectangleYOrigin + rectangleHeight + ((textSize + EDGE) * 2), paintMarks); 	
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

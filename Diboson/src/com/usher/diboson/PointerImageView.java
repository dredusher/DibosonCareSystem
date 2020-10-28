package com.usher.diboson;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.widget.ImageView;

public class PointerImageView extends ImageView 
{
	// =============================================================================
	// 13/12/2014 ECU created as the pointerimageView for the RadarActivity
	// 04/01/2015 ECU I M P O R T A N T see the information in Notes concerning
	//                ================= the use of 'matrix.postRotation' which
	//                                  did not seem to work on the Motorola and
	//                                  CnM tablets but was OK on the Nexus
	// 06/01/2015 ECU make the use of the matrix switchable on USE_MATRIX
	//            ECU changed to use a thread instead of AsyncTask
	//            ECU take out the USE_MATRIX bits
	// =============================================================================
	
	// =============================================================================
	boolean				autoMode		= false;
	boolean				direction 		= true;
	float				increment		= 1.0f;
	Matrix				matrix			= new Matrix ();
	OnDrawThread		onDrawThread	= new OnDrawThread ();
	float 				pointerAngle 	= 0.0f;
	boolean				running			= false;
	int					sleepTime		= 20;
	int					threadHeight	= 0;
	float				threadIncrement	= 0f;
	int					threadWidth		= 0;
	// =============================================================================
	
	// =============================================================================
	public PointerImageView (Context theContext) 
	{
		// -------------------------------------------------------------------------
		// 16/12/2014 ECU declare the default constructor
		// -------------------------------------------------------------------------
		super (theContext);
		// -------------------------------------------------------------------------
		// 16/12/2014 ECU default the displayed image to the 'circle'
		// -------------------------------------------------------------------------
		this.setImageResource (R.drawable.circle);
		// -------------------------------------------------------------------------		
	}
	// =============================================================================
	@SuppressWarnings("deprecation")
	// -----------------------------------------------------------------------------
	public PointerImageView (Context theContext,boolean theDirection,int theAlpha,boolean theAutoMode,
									int theTimeForARotation,float theIncrement) 
	{
		// -------------------------------------------------------------------------
		// 16/12/2014 ECU declare the constructor which enables more customisation
		//                of this ImageView
		// 02/01/2015 ECU 'theTimeForARotation' is the time in milliseconds for the image
		//                to make a complete 360 degree rotation
		// -------------------------------------------------------------------------
		super (theContext);
		// -------------------------------------------------------------------------
		// 14/12/2014 ECU save the supplied variables
		// -------------------------------------------------------------------------
		autoMode	= theAutoMode;
		direction	= theDirection;
		increment	= theIncrement;
		// -------------------------------------------------------------------------
		// 02/01/2015 ECU work out the 'sleepTime' to get the rotation time that is
		//                required - this assumes, wrongly, that the matrix
		//                rotation takes negligible time
		// -------------------------------------------------------------------------
		if (theTimeForARotation != StaticData.NO_RESULT)
			sleepTime = theTimeForARotation / ((int)(360.0 / theIncrement));
		else
			sleepTime = 20;		
		// -------------------------------------------------------------------------
		this.setAlpha(theAlpha);
		// -------------------------------------------------------------------------		
	}
	// =============================================================================
	public PointerImageView (Context theContext,boolean theDirection,
								int theAlpha,boolean theAutoMode,float theIncrement) 
	{	
		// -------------------------------------------------------------------------
		// 02/01/2015 ECU declare the constructor which enables more customisation
		//                of this ImageView through using the previously
		//                declared constructor
		//            ECU the NO_RESULT parameter means that the image rotation
		//                is not constrained to complete within a number of 
		//                milliseconds
		// -------------------------------------------------------------------------
		this (theContext,theDirection,theAlpha,theAutoMode,StaticData.NO_RESULT,theIncrement);
	}
	// ==============================================================================
	@Override
	public void onDraw (Canvas theCanvas) 
	{
		// -------------------------------------------------------------------------
		// 16/12/2014 ECU if in auto mode then rotate within the class
		// -------------------------------------------------------------------------
		if (autoMode)
		{	
			// ---------------------------------------------------------------------
			// 06/01/2015 ECU if the thread is not already running then set some variables
			//                and start the thread
			// ---------------------------------------------------------------------
			if (!running)
			{
				// -----------------------------------------------------------------
				// 06/01/2015 ECU store details about the canvas to be manipulated
				// 05/07/2020 ECU change 'theCanvas.get...' to 'get...'
				// -----------------------------------------------------------------
				threadHeight 		= getHeight() / 2;
				threadIncrement		= ((direction) ? -increment : increment);
				threadWidth  		= getWidth()  / 2;
				// -----------------------------------------------------------------
				// 06/01/2015 ECU now start the thread
				// -----------------------------------------------------------------
				onDrawThread.start();
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 06/01/2015 ECU the thread is already running so just adjust
				//                the canvas by the 'matrix'
				// -----------------------------------------------------------------
				theCanvas.concat (matrix);
				// -----------------------------------------------------------------
			}		
		}
		else
		{
			// ---------------------------------------------------------------------
			// 16/12/2014 ECU rotate the canvas directly 
			// ---------------------------------------------------------------------
			theCanvas.rotate(((direction) ? -pointerAngle : pointerAngle),(this.getWidth()/2),(this.getHeight()/2));	
		}
		// -------------------------------------------------------------------------       
		super.onDraw (theCanvas);	
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public void destroy ()
	{
		// -------------------------------------------------------------------------
		// 02/01/2015 ECU indicate that the async task is to stop. Try to do it this
		//                way rather than cancelling the task directly
		// -------------------------------------------------------------------------
		running = false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public void update (float theAngle) 
	{
		if (!autoMode)
		{
			// ---------------------------------------------------------------------
			// 13/12/2014 ECU copy across the supplied angle then redraw the image
			// ---------------------------------------------------------------------
			this.pointerAngle = theAngle;
			// ---------------------------------------------------------------------
			this.invalidate();
			// ---------------------------------------------------------------------
		}
	} 
	// =============================================================================

	// =============================================================================
	class OnDrawThread extends Thread
	{
		// -------------------------------------------------------------------------
	    @Override
	    public void run()
	    {
	    	try 
	        {
	            synchronized(this)
	            {    
	            	// -------------------------------------------------------------
	            	// 06/01/2015 ECU indicate that the thread is running
	            	// -------------------------------------------------------------
	            	running = true;
	            	// -------------------------------------------------------------
	            	// 06/01/2015 ECU keep looping until told to stop
	            	// -------------------------------------------------------------
	            	while (running)
	                {
	            		// ---------------------------------------------------------
	            		// 02/01/2015 ECU wait for a short time
	            		// ---------------------------------------------------------
	                	Thread.sleep (sleepTime);
	                	// ---------------------------------------------------------
	    				// 02/01/2015 ECU be aware that the 'postRotate' needs the 
	    			   	//				  increment since the last rotate so use 'increment'
	    			   	//                rather than 'pointerAngle'
	                	// ---------------------------------------------------------
	    			   	matrix.postRotate (threadIncrement,threadWidth,threadHeight);
	    			   	// ---------------------------------------------------------
	    			   	// 02/01/2015 ECU request an 'onDraw' action
	    			   	// ---------------------------------------------------------
	    	        	postInvalidate ();
	                	// ---------------------------------------------------------
	                }	
		        }
		    }
		    catch(Exception theException)
		    {                    
		    }       
		 };	 	    
	}
	// =============================================================================
}

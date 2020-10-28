package com.usher.diboson;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class RadarActivity extends DibosonActivity 
{
	// =========================================================================
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 29/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// -------------------------------------------------------------------------
	//final static String TAG = "RadarActivity";
	// -------------------------------------------------------------------------
	final static int EXPLOSION_SIZE_X	= 50;
	final static int EXPLOSION_SIZE_Y	= 30;
	final static int NUMBER_OF_CARDS	= 3;
	final static int OPACITY			= 100;
	final static int OPACITY_FULL		= 255;
	final static int OPACITY_TRANSPARENT	= 0;
	final static int POINTER_COLOUR		= Color.WHITE;
	final static int POINTER_LENGTH		= (PublicData.screenWidth/2) - 50;
	final static int RING_COLOUR		= Color.BLACK;
	final static int SCREEN_COLOUR		= Color.BLACK;
	// -------------------------------------------------------------------------
			static 	Bitmap   		cards []		= new Bitmap [NUMBER_OF_CARDS];
					PointerImageView	centreImageView;
	public 	static 	float 			centreX 		= (float) (PublicData.screenWidth / 2);
	public 	static 	float 			centreY 		= (float) (PublicData.screenHeight / 2);
			static 	Context			context;
			static 	float			deltaX;
			static 	float			deltaY;
					ExplosionHandler 	explosionHandler	= new ExplosionHandler ();
					int				explosionOpacity	= OPACITY_FULL;
					Bitmap			explosionBitMap;
					Canvas			explosionCanvas;
					ImageView		explosionImageView;
					Paint			explosionPaint;
					Bitmap			frontBitMap;
					Canvas			frontCanvas;
					ImageView		frontImageView;
			static 	int				image;
					boolean			keepRunning = true;
			RelativeLayout.LayoutParams layoutParams; 
			static 	float   	 	mathCos []      = new float [360];
			static 	float    		mathSin []      = new float [360];
			static 	float			oldX			= StaticData.NO_RESULT;
			static 	float			oldY			= StaticData.NO_RESULT;
			PointerImageView		outerRingImageView;
			static  boolean			paused = false;								// 07/01/2015 ECU added
			static 	Paint			paint 			= new Paint ();
			static	double			pointerAngle	= 0;
			static	Paint   		pointerOldPaint;
			static 	Paint    		pointerPaint;
					Bitmap			pointerBitMap;
					Canvas			pointerCanvas;
					RadarImageView	radarImageView;
					RelativeLayout	radarLayout;
					RefreshHandler 	refreshHandler	= new RefreshHandler ();
					Bitmap			screenBitMap;
					Canvas			screenCanvas;
					ImageView		screenImageView;
					PointerImageView	skyImageView;
	// -------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			setContentView(R.layout.activity_radar);
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			radarLayout 		= (RelativeLayout) findViewById (R.id.radarLayout);
			// ---------------------------------------------------------------------
			// 09/12/2014 ECU preset the maths functions
			// ---------------------------------------------------------------------
			for (int theIndex = 0; theIndex < 360; theIndex++)
			{
				mathCos [theIndex] = (float) (Math.cos(Math.toRadians(theIndex))); 
				mathSin [theIndex] = (float) (Math.sin(Math.toRadians(theIndex))); 	
			}
			// ---------------------------------------------------------------------
			// 16/12/2014 ECU declare the ImageView that will be used for the inner sky
			// ---------------------------------------------------------------------
			RelativeLayout.LayoutParams skyPosition 
				= new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			skyImageView = new PointerImageView (this,true,OPACITY_FULL,true,60000,0.2f);
			skyImageView.setLayoutParams(skyPosition);  
			skyImageView.setImageResource(R.drawable.small_sky);
			skyPosition.addRule(RelativeLayout.CENTER_IN_PARENT);
			radarLayout.addView(skyImageView,skyPosition);
			// ---------------------------------------------------------------------
			drawTheExplosions ();
			drawTheRadarScreen ();	
			// ---------------------------------------------------------------------
			// 03/01/2015 ECU set up the ImageView programmatically
			// ---------------------------------------------------------------------
			layoutParams = new RelativeLayout.LayoutParams (LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			radarImageView = new RadarImageView (this);
			radarImageView.setLayoutParams(layoutParams);  
			layoutParams.addRule (RelativeLayout.CENTER_IN_PARENT);
			radarLayout.addView (radarImageView,layoutParams);
			// ---------------------------------------------------------------------
			drawTheRadarPointer (POINTER_LENGTH,pointerAngle);
			drawTheFront ();
			// ---------------------------------------------------------------------
			outerRingImageView = new PointerImageView (this);	
			// ---------------------------------------------------------------------
			// 16/12/2014 ECU changed from WRAP_CONTENT
			// ---------------------------------------------------------------------
			// 16/12/2014 ECU declare the ImageView that will be used for the outer ring
			// ---------------------------------------------------------------------
			RelativeLayout.LayoutParams position 
				= new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			outerRingImageView = new PointerImageView (this,false,OPACITY_FULL,true,60000,0.2f);
			outerRingImageView.setLayoutParams (position);  
			outerRingImageView.setImageResource(R.drawable.circle);
			position.addRule(RelativeLayout.CENTER_IN_PARENT);
			radarLayout.addView(outerRingImageView,position);
			// ---------------------------------------------------------------------
			// 16/12/2014 ECU declare the ImageView that will be user for the centre image
			// ---------------------------------------------------------------------
			RelativeLayout.LayoutParams positionCentre = new RelativeLayout.LayoutParams (100,100);
			centreImageView = new PointerImageView (this,true,OPACITY_FULL,true,1.0f);	
			centreImageView.setLayoutParams (positionCentre);  
			centreImageView.setImageResource(R.drawable.engine);
			positionCentre.addRule (RelativeLayout.CENTER_IN_PARENT);
			radarLayout.addView (centreImageView,positionCentre);
			// ---------------------------------------------------------------------
			refreshHandler.sleep (10000);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	@Override
    public void onDestroy()
    {
		// -------------------------------------------------------------------------
		// 10/12/2014 ECU indicate that the scanning handler is to stop
		// -------------------------------------------------------------------------
		keepRunning = false;
		// -------------------------------------------------------------------------
		// 02/01/2015 ECU make sure any running tasks are stopped
		// -------------------------------------------------------------------------
		centreImageView.destroy ();
		outerRingImageView.destroy ();
		skyImageView.destroy ();
		// -------------------------------------------------------------------------
        super.onDestroy();
        // -------------------------------------------------------------------------
    }
	/* ============================================================================= */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		// -------------------------------------------------------------------------
		// 18/09/2013 ECU added to process the back key
		// -------------------------------------------------------------------------
	    if( keyCode == KeyEvent.KEYCODE_BACK) 
	    {	    	
	       	super.onKeyDown(keyCode, event);
	       	// ---------------------------------------------------------------------
	       	// 18/09/2013 ECU indicate that the key has been processed
	       	// ---------------------------------------------------------------------
	    	return true;
	    }
	    else
	    {
	        return super.onKeyDown(keyCode, event);
	    }
	}
	// =============================================================================
	@Override 
	protected void onResume() 
	{ 	
		// -------------------------------------------------------------------------
	  	//05/01/2015 ECU restart the scanning
	  	// -------------------------------------------------------------------------
		if (paused)
		{
			keepRunning = true;
			refreshHandler.sleep (1000);
			// ---------------------------------------------------------------------
			// 07/01/2015 ECU reset the paused state
			// ---------------------------------------------------------------------
			paused = false;
		}
		// -------------------------------------------------------------------------
	   	super.onResume(); 
	} 
	/* ============================================================================= */
	@Override 
	protected void onPause() 
	{
		// -------------------------------------------------------------------------
		// 05/01/2015 ECU stop the scanning whilst the display is off
		// -------------------------------------------------------------------------
		if (!paused)
		{
			keepRunning = false;
			// ---------------------------------------------------------------------
			// 07/01/2015 ECU reset the paused state
			// ---------------------------------------------------------------------
			paused = true;
		}
		// -------------------------------------------------------------------------
	   	super.onPause(); 
	} 
	/* ============================================================================ */
	@SuppressWarnings("deprecation")
	// ----------------------------------------------------------------------------
	ImageView addAnImageView (RelativeLayout theLayout,int theWidth,int theHeight,int theAlpha)
	{
		// ------------------------------------------------------------------------
		ImageView localImageView;
		// -------------------------------------------------------------------------
		// 03/01/2015 ECU method adds an ImageView programmatically
		// -------------------------------------------------------------------------
		layoutParams = new RelativeLayout.LayoutParams (theWidth,theHeight);
		localImageView = new ImageView (this);
		localImageView.setLayoutParams(layoutParams);  
		localImageView.setAlpha(theAlpha);
		layoutParams.addRule (RelativeLayout.CENTER_IN_PARENT);
		theLayout.addView (localImageView,layoutParams);
		// -------------------------------------------------------------------------
		return localImageView;
	}
	// =============================================================================
	void drawAnExplosion (float theLength, double theAngle)
	{
		float deltaX = (float)theLength * (float) (Math.cos(Math.toRadians(theAngle))); 
		float deltaY = (float)theLength * (float) (Math.sin(Math.toRadians(theAngle)));
		// -------------------------------------------------------------------------
		explosionCanvas.drawColor (Color.TRANSPARENT,Mode.MULTIPLY);
		// -------------------------------------------------------------------------
		explosionOpacity = OPACITY_FULL;
		explosionPaint.setAlpha(explosionOpacity);
		// -------------------------------------------------------------------------
		// 10/12/2014 ECU decide which image to display
		// -------------------------------------------------------------------------
		image = (int) (Math.random() * 2.3);
		explosionCanvas.drawBitmap (cards [image],centreX + deltaX - (EXPLOSION_SIZE_X/2),centreY + deltaY,explosionPaint);
		explosionHandler.sleep (10);
		// -------------------------------------------------------------------------
		// 10/12/2014 ECU song a 'sonic ping'
		// -------------------------------------------------------------------------
		Utilities.PlayAFile(context, PublicData.projectFolder + "ping.wav");
	}
	// =============================================================================
	void drawTheExplosions ()
	{
		// -------------------------------------------------------------------------
		// 03/01/2015 ECU set up the ImageView programmatically
		// -------------------------------------------------------------------------
		explosionImageView = addAnImageView (radarLayout,LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT,OPACITY);
		// -------------------------------------------------------------------------
		explosionBitMap = Bitmap.createBitmap (PublicData.screenWidth,PublicData.screenHeight, Config.ARGB_8888);
		explosionCanvas = new Canvas (explosionBitMap);
		explosionImageView.setImageBitmap (explosionBitMap);
		// -------------------------------------------------------------------------
		explosionPaint = new Paint ();
		// -------------------------------------------------------------------------
		// 10/12/2014 ECU set up the card images
		// -------------------------------------------------------------------------
		cards [0] = BitmapFactory.decodeResource(getResources(),R.drawable.american_express);		 		 
		cards [0] = Bitmap.createScaledBitmap(cards [0], EXPLOSION_SIZE_X, EXPLOSION_SIZE_Y, false);
		cards [1] = BitmapFactory.decodeResource(getResources(),R.drawable.barclaycard);		 		 
		cards [1] = Bitmap.createScaledBitmap(cards [1], EXPLOSION_SIZE_X, EXPLOSION_SIZE_Y, false);
		cards [2] = BitmapFactory.decodeResource(getResources(),R.drawable.master_card);		 		 
		cards [2] = Bitmap.createScaledBitmap(cards [2], EXPLOSION_SIZE_X, EXPLOSION_SIZE_Y, false);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void drawTheFront ()
	{	
		// -------------------------------------------------------------------------
		// 03/01/2015 ECU set up the ImageView programmatically
		// -------------------------------------------------------------------------
		frontImageView = addAnImageView (radarLayout,LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT,OPACITY_FULL);
		// -------------------------------------------------------------------------
		// 02/12/2014 ECU set up the bitmap and canvas
		// -------------------------------------------------------------------------
		frontBitMap = Bitmap.createBitmap (PublicData.screenWidth,PublicData.screenHeight, Config.ARGB_8888);
		frontCanvas	= new Canvas (frontBitMap);
		frontImageView.setImageBitmap (frontBitMap);
		// -------------------------------------------------------------------------
		// 02/12/2014 ECU draw the background
		// -------------------------------------------------------------------------
		paint.setStyle (Paint.Style.STROKE);
		paint.setColor (Color.YELLOW);
		paint.setAlpha (OPACITY);
		paint.setStrokeWidth (10);
		frontCanvas.drawCircle (centreX,centreY,POINTER_LENGTH, paint);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void drawTheRadarPointer (int theLength,double theAngle)
	{
		// -------------------------------------------------------------------------
		// 02/12/2014 ECU work out the end position
		// -------------------------------------------------------------------------
		deltaX = (float)theLength * (float) (Math.cos(Math.toRadians(theAngle))); 
		deltaY = (float)theLength * (float) (Math.sin(Math.toRadians(theAngle)));
		// -------------------------------------------------------------------------
		pointerBitMap = Bitmap.createBitmap(PublicData.screenWidth,PublicData.screenHeight, Config.ARGB_8888);
		pointerCanvas = new Canvas (pointerBitMap);
		radarImageView.setImageBitmap (pointerBitMap);
		// -------------------------------------------------------------------------
		pointerOldPaint = new Paint ();
		pointerOldPaint.setStyle(Paint.Style.FILL);
		pointerOldPaint.setColor (SCREEN_COLOUR);
		pointerOldPaint.setAlpha (OPACITY);
		pointerCanvas.drawCircle (centreX,centreY,POINTER_LENGTH, pointerOldPaint);
		// -------------------------------------------------------------------------
		// 13/12/2014 ECU set up the paint for clearing the old pointer position
		// -------------------------------------------------------------------------
		pointerOldPaint.setStyle(Paint.Style.STROKE);
		pointerOldPaint.setStrokeWidth (10);
		// --------------------------------------------------------------------------
		pointerPaint = new Paint ();
		pointerPaint.setStyle (Paint.Style.STROKE);
		pointerPaint.setStrokeWidth(10);
		pointerPaint.setColor (POINTER_COLOUR);
		pointerPaint.setAlpha (OPACITY);
		pointerCanvas.drawLine (centreX, centreY, centreX+deltaX,centreY+deltaY, pointerPaint);
		// -------------------------------------------------------------------------
		// 02/12/2014 ECU preset the saved coordinates
		// -------------------------------------------------------------------------
		oldX = centreX + deltaX;
		oldY = centreY + deltaY;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void drawTheRadarScreen ()
	{
		// -------------------------------------------------------------------------
		// 03/01/2015 ECU set up the ImageView programmatically
		// -------------------------------------------------------------------------
		screenImageView = addAnImageView (radarLayout,LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT,OPACITY);
		// -------------------------------------------------------------------------
		// 02/12/2014 ECU set up the bitmap and canvas
		// -------------------------------------------------------------------------
		screenBitMap = Bitmap.createBitmap(PublicData.screenWidth,PublicData.screenHeight, Config.ARGB_8888);
		screenCanvas = new Canvas(screenBitMap);
		screenImageView.setImageBitmap(screenBitMap);
		// -------------------------------------------------------------------------
		paint.setStyle (Paint.Style.FILL);
		paint.setColor (SCREEN_COLOUR);	
		paint.setAlpha (OPACITY_TRANSPARENT);
		screenCanvas.drawCircle (centreX,centreY,POINTER_LENGTH, paint);
		// -------------------------------------------------------------------------
		// 02/12/2014 ECU now draw some ranging rings
		// -------------------------------------------------------------------------
		paint.setStyle (Paint.Style.STROKE);
		paint.setStrokeWidth (2);
		paint.setColor (RING_COLOUR);
		paint.setAlpha (OPACITY);
		// -------------------------------------------------------------------------
		for (int theIndex = 50; theIndex <= POINTER_LENGTH; theIndex += 50)
			screenCanvas.drawCircle (centreX,centreY,theIndex, paint);
		// -----------------------------------------------------------------------
	}
	// =============================================================================
	public static void updateTheRadarPointer (Canvas theCanvas)
	{
		updateTheRadarPointer (theCanvas,POINTER_LENGTH,pointerAngle);
	}
	// =============================================================================
	static void updateTheRadarPointer (Canvas theCanvas,int theLength,double theAngle)
	{
		// -------------------------------------------------------------------------
		// 02/12/2014 ECU work out the end position
		// -------------------------------------------------------------------------
		deltaX = (float)theLength * mathCos [(int) theAngle];
		deltaY = (float)theLength * mathSin [(int) theAngle];
		// -------------------------------------------------------------------------
		// 02/12/2014 ECU remove the old pointer
		// -------------------------------------------------------------------------
		theCanvas.drawLine(centreX, centreY, oldX,oldY, pointerOldPaint);
		// -------------------------------------------------------------------------
		// 02/12/2014 ECU draw the new pointer after saving the new coords as the old
		// -------------------------------------------------------------------------
		oldX = centreX + deltaX;
		oldY = centreY + deltaY;
		// -------------------------------------------------------------------------
		theCanvas.drawLine(centreX, centreY,oldX,oldY, pointerPaint);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class RefreshHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) 
        {  
        	// ---------------------------------------------------------------------
        	// 09/12/2014 ECU just dummy up the detection of explosions
        	// ---------------------------------------------------------------------
        	if ((pointerAngle % (Math.random() * 90)) == 0)
        	{
        		drawAnExplosion ((float)(Math.random()*(POINTER_LENGTH - 50)),pointerAngle);
        	}
        	// ---------------------------------------------------------------------
        	// 09/12/2014 ECU increment the pointer angle and then reset after a
        	//                full circle
        	// ---------------------------------------------------------------------
        	pointerAngle += 1.0;
			if (pointerAngle == 360.0)
				pointerAngle = 0.0;
			// ---------------------------------------------------------------------
			// 09/12/2014 ECU refresh the display
			// ---------------------------------------------------------------------
			radarImageView.invalidate();
			// ---------------------------------------------------------------------
			//outerRingImageView.update ((float) pointerAngle);
			// ---------------------------------------------------------------------
			// 10/12/2014 ECU check if want to keep running
			// ---------------------------------------------------------------------
			if (keepRunning)
				sleep (25);
			// ---------------------------------------------------------------------
        }
        /* ------------------------------------------------------------------------ */
        public void sleep(long delayMillis)
        {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    };
    // =============================================================================
    @SuppressWarnings ("deprecation")
	@SuppressLint     ("HandlerLeak")
    // -----------------------------------------------------------------------------
	class ExplosionHandler extends Handler
    {
		@Override
        public void handleMessage(Message msg) 
        {  
			if (keepRunning)
			{
				if (explosionOpacity > OPACITY_TRANSPARENT)
				{
					explosionImageView.setAlpha(explosionOpacity);
        		
					explosionOpacity -= 10;
        		
					sleep (50);
				}
				else
				{
					explosionCanvas.drawColor (Color.TRANSPARENT,Mode.MULTIPLY);
				}
			}
        }
        /* ------------------------------------------------------------------------ */
        public void sleep(long delayMillis)
        {
            this.removeMessages (0);
            sendMessageDelayed (obtainMessage(0), delayMillis);
        }
    };
    // =============================================================================
}

package com.usher.diboson;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;


public class GraphicsActivity extends DibosonActivity implements OnTouchListener 
{
	// =============================================================================
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 02/11/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// =============================================================================
	
	// =============================================================================
	Canvas canvas;
	Canvas canvas2;
	ImageView myImageView;
	Paint paint;
	Paint paint2;
	Bitmap bitmap;
	Bitmap bitmap2;
	boolean toggleGraphics;
	RefreshHandler myRefreshHandler;
	int height,width;
	float x,y;
	int deltaX = 5;
	int deltaY = 5;
	/* ========================================================================= */
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
			// 08/04/2014 ECU changed to use the variable
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
				
			setContentView(R.layout.activity_graphics);
		
			myRefreshHandler = new RefreshHandler ();
 
			myImageView = (ImageView)findViewById (R.id.graphics_view_box);
		
			myImageView.setOnTouchListener (this);
			// ---------------------------------------------------------------------
			// We'll be creating an image that is 100 pixels wide and 200 pixels tall.
			// ----------------------------------------------------------------------
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			width 	= 	metrics.widthPixels;
			height 	=	metrics.heightPixels;
		
			x 		= 	width/2;
			y 		= 	height/2;
			// ---------------------------------------------------------------------
			// Create a bitmap with the dimensions we defined above, and with a 16-bit pixel format. We'll
			// get a little more in depth with pixel formats in a later post.
			// ---------------------------------------------------------------------
			bitmap 	= Bitmap.createBitmap(width, height, Config.RGB_565);
			bitmap2 = Bitmap.createBitmap (width,height, Config.RGB_565);
			// ---------------------------------------------------------------------
			// Create a paint object for us to draw with, and set our drawing colour to blue.
			// ---------------------------------------------------------------------
			paint = new Paint();
			paint.setColor(Color.BLUE);
		
			paint2 = new Paint ();
			paint2.setColor (Color.RED);
			// ---------------------------------------------------------------------
			// Create a new canvas to draw on, and link it to the bitmap that we created above. Any drawing
			// operations performed on the canvas will have an immediate effect on the pixel data of the
			// bitmap.
			// ---------------------------------------------------------------------
			canvas = new Canvas(bitmap);
			canvas2 = new Canvas (bitmap2);
			// ---------------------------------------------------------------------
			// Fill the entire canvas with a white colour.
			// ---------------------------------------------------------------------
			canvas.drawColor(Color.WHITE);
			canvas2.drawColor(Color.YELLOW);
			// ---------------------------------------------------------------------
			// Draw a rectangle inside our image using the paint object we defined above. The rectangle's
			// upper left corner will be at (25,50), and the lower left corner will be at (75,150). Since we set
			// the paint object's color above, this rectangle will be blue.
			// ---------------------------------------------------------------------
			canvas.drawRect(width/2,height/2,300,150, paint);
			//canvas2.drawCircle(width/2,height/2,100,paint2);
			// ---------------------------------------------------------------------
			// In order to display this image in our activity, we need to create a new ImageView that we
			// can display.
			// Set this ImageView's bitmap to the one we have drawn to.
			// ---------------------------------------------------------------------
			myImageView.setImageBitmap(bitmap);
		
			myRefreshHandler.sleep(10000);
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
	/* ============================================================================== */
     public boolean onTouch(View view, MotionEvent event) 
    {
         canvas.drawCircle (event.getX(),event.getY(),2,paint);
    	 myImageView.invalidate();
    	 
         return true;
    }
    /* ============================================================================== */
 	@SuppressLint("HandlerLeak")
	class RefreshHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) 
        {   
        	/* wait until any music has finished playing */ 
        
        	/* 24/05/2013 ECU rearranged the order to give an initial delay */
        	
        	toggleGraphics = false; 
        	if (toggleGraphics)
        	{
        		toggleGraphics = false;
        		myImageView.setImageBitmap(bitmap);
        	}
        	else
        	{
        		toggleGraphics = true;
        		
        		paint2.setColor (Color.YELLOW);
          		canvas2.drawCircle(x,y,5,paint2);
          	
          		if (x > width || x < 0)
          			deltaX = -deltaX;
          		if (y > height || y < 0)
          			deltaY = -deltaY;
          		x += deltaX;
          		y += deltaY;
          		
        		paint2.setColor (Color.RED);
        		canvas2.drawCircle(x,y,5,paint2);
        		myImageView.setImageBitmap(bitmap2);
        		myImageView.invalidate ();
        		
        	}
        	 	sleep (10);
        }
        /* ------------------------------------------------------------------------ */
        public void sleep(long delayMillis)
        {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    };
 
}



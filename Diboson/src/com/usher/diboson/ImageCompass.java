package com.usher.diboson;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.widget.ImageView;

public class ImageCompass extends ImageView 
{
	/* ========================================================================== */
	// 05/06/2013 ECU created as the imageView for the CompassActivity
	/* ========================================================================== */
	 float  azimuth = 0f;
	 Paint 	paint;
	 float  pitch   = 0f;
	 float  roll    = 0f;
	 /* ========================================================================== */
	 public ImageCompass(Context context) 
	 {
		 super(context);
		 paint = new Paint();
		 paint.setColor(Color.WHITE);
		 paint.setStrokeWidth(2);
		 paint.setStyle(Style.STROKE);

		 // 05/06/2013 ECU put up the image of the compass
		   
		 this.setImageResource(R.drawable.compass_image);
	 }
	 /* ========================================================================== */
	 @Override
	 public void onDraw(Canvas canvas) 
	 {
		 // 05/06/2013 ECU rotate the image inline with north
		 //                the - seems necessary to get the rotation correct
		 //                the rotation is about the centre of the screen
			 
		 canvas.rotate(-azimuth, this.getWidth() / 2, this.getHeight() / 2);
		       
		 super.onDraw(canvas);
	 }
	 /* ========================================================================== */
	 public String update (float azimuth,float pitch,float roll,boolean remoteFlag) 
	 {
		 // ----------------------------------------------------------------------
		 // 05/06/2013 ECU input is the azimuth
		 // 21/10/2014 ECU added the 'remoteFlag' switch
		 // ----------------------------------------------------------------------
		 this.azimuth = azimuth;
		 this.pitch   = pitch;
		 this.roll    = roll;
		 // ----------------------------------------------------------------------
		 // 05/06/2013 ECU the azimuth seems to be 0 for north, 90 for east,
		 //                180/-180 for south, -90 for west
		 //                want to change to 0 north, 90 east, 180 south, 270 west
		 // ----------------------------------------------------------------------
		 if (this.azimuth < 0.0f) 
			 this.azimuth = 360.0f + this.azimuth;
		 // ----------------------------------------------------------------------
		 // 05/06/2013 ECU get the graphics screen updated
		 // ----------------------------------------------------------------------
		 this.invalidate();
		 // ----------------------------------------------------------------------
		 // 05/06/2013 ECU just return the pitch and roll for display
		 // 21/10/2014 ECU add the remoteFlag switch
		 // 23/10/2014 ECU changed to return the device name rather than just the
		 //                IP address
	     // ----------------------------------------------------------------------
		 if (!remoteFlag)
			 return String.format (" Pitch = %5.1f    Roll = %5.1f    Azimuth = %5.1f",pitch,roll,azimuth);
		 else
			 return "Monitored device  '" + 
			 			Utilities.GetDeviceName(PublicData.datagram.sender) + "' (" + PublicData.datagram.sender + ")";
		 // ----------------------------------------------------------------------
	 } 
	 /* ========================================================================== */
}


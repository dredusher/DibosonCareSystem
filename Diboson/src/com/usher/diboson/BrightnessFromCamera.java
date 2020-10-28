package com.usher.diboson;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.Message;

import java.util.List;


// =================================================================================
public class BrightnessFromCamera 
{
	// =============================================================================
	// 09/09/2019 ECU created to handle all aspects of obtaining a 'rough' brightness
	//                from a specified camera
	// 05/08/2020 ECU make sure that the 'shutter sound' is disabled
	// =============================================================================
	
	// =============================================================================
	private static final int 	JPEG_QUALITY	= 10;
	private static final String	TAG				= "BrightnessFromCamera";
	// =============================================================================
	
	// =============================================================================
			int					brightnessCurrent;
			BrightnessHandler	brightnessHandler;
			int					camera;
			Camera 				cameraSelected;
			boolean				capturing;
			Context				context;
			Size				pictureSize;
			int					rate;
	// =============================================================================
	
	// =============================================================================
	public BrightnessFromCamera (Context theContext,int theCamera)
	{
		// -------------------------------------------------------------------------
		// 01/10/2019 ECU changed from debugPopToast
		// -------------------------------------------------------------------------
		Utilities.debugMessage (TAG,"Created");
		// -------------------------------------------------------------------------
		// 09/09/2019 ECU save any supplied arguments
		// 02/10/2019 ECU added cameraSelected
		// -------------------------------------------------------------------------
		brightnessCurrent	=	StaticData.NOT_SET;
		camera 				= 	theCamera;
		cameraSelected		=   null;
		context				=   theContext;
		pictureSize			=	null;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	class BrightnessHandler extends Handler
	{
		// -------------------------------------------------------------------------
		@Override
		public void handleMessage (Message theMessage) 
		{   
			switch (theMessage.what)
			{
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_AMBIENT_LIGHT:
							getBrightness ();
	        				break;
	        	// -----------------------------------------------------------------      			
			}			
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	void brightnessObtained (int theBrightness)
	{
		// -------------------------------------------------------------------------
		// 01/10/2019 ECU changed from debugPopToast
		// -------------------------------------------------------------------------
		Utilities.debugMessage (TAG,"brightnessObtained : " + theBrightness + " : " + capturing);
		// -------------------------------------------------------------------------
		// 08/09/2019 ECU if still monitoring then trigger the next 'capture'
		// -------------------------------------------------------------------------
		if (capturing)
		{
			//  --------------------------------------------------------------------
			//  08/09/2019 ECU send a delayed message for the next capture
			// ---------------------------------------------------------------------
			brightnessHandler.sendEmptyMessageDelayed (StaticData.MESSAGE_AMBIENT_LIGHT,rate);
			// ---------------------------------------------------------------------
			// 08/09/2019 ECU now call the required method
			// 09/09/2019 ECU check if the brightness has changed
			// ---------------------------------------------------------------------
			if (theBrightness != brightnessCurrent)
			{
				// -----------------------------------------------------------------
				// 09/09/2019 ECU the brightness has changed so after saving the
				//                current value then call the method that was
				//                provided
				// -----------------------------------------------------------------
				brightnessCurrent = theBrightness;
				// -----------------------------------------------------------------
				// 09/09/2019 ECU call up the method that takes any necessary
				//				  actions
				// -----------------------------------------------------------------
				Utilities.checkLightLevel (context, (float) theBrightness);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void getBrightness () 
	{
		// ------------------------------------------------------------------------
		// 06/09/2019 ECU created to work out the brightness using the pixels in
		//                a 'low quality' picture that is taken on the specified
		//                camera
		// 08/09/2019 ECU added the method to be called when brightness returned
		// 09/09/2019 ECU Note - no need to set the parameters each entry but leave
		//                       for the time being
		// 29/09/2019 ECU encase the whole method within a try/catch - just in case
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 01/10/2019 ECU changed from debugPopToast
			// ---------------------------------------------------------------------
			Utilities.debugMessage (TAG,"getBrightness");
			// ---------------------------------------------------------------------
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo ();
			// ---------------------------------------------------------------------
			// 05/09/2019 ECU select the required camera
			// ---------------------------------------------------------------------
			int cameraIndex = Utilities.cameraIndex (camera);
			// ---------------------------------------------------------------------
			Camera.getCameraInfo (cameraIndex, cameraInfo);
			// ---------------------------------------------------------------------
			// 02/10/2019 ECU before opening then check that previous use of the camera
			//                was 'released'
			//            ECU use the method which will only do the release if necessary
			// ---------------------------------------------------------------------
			releaseCamera ();
			// ---------------------------------------------------------------------
			// 05/09/2019 ECU now open up the selected camera
			// ---------------------------------------------------------------------
			cameraSelected = Camera.open (cameraIndex);
			// ---------------------------------------------------------------------
			// 05/08/2020 ECU check if can disable the shutter sound
			// ---------------------------------------------------------------------
			if (cameraInfo.canDisableShutterSound)
			{
				cameraSelected.enableShutterSound(false);
			}
			// ---------------------------------------------------------------------
			// 09/09/2019 ECU get the required picture size if not already initialised
			// ---------------------------------------------------------------------
			if (pictureSize == null)
			{ 		
				// -----------------------------------------------------------------
				// 06/09/2019 ECU get the list of sizes that are supported by this camera
				// -----------------------------------------------------------------
				List<Size> supportedPictureSizes = cameraSelected.getParameters().getSupportedPictureSizes ();
				// -----------------------------------------------------------------
				// 06/09/2019 ECU find the lowest supported picture size
				// -----------------------------------------------------------------
				int pictureSizeWanted = 0;
				int width = supportedPictureSizes.get(0).width;
				for (int index = 1; index < supportedPictureSizes.size(); index++)
				{
					// -------------------------------------------------------------
					// 06/09/2019 ECU check if this entry has a lower resolution
					// -------------------------------------------------------------
					if (supportedPictureSizes.get (index).width < width)
					{
						width = supportedPictureSizes.get (index).width;
						pictureSizeWanted = index;
					}
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 06/09/2019 ECU now adjust the camera to the smallest size with low
				//                quality jpeg (quality goes from 1 to 100)
				// -----------------------------------------------------------------
				pictureSize = supportedPictureSizes.get (pictureSizeWanted);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			Camera.Parameters parameters = cameraSelected.getParameters ();
			parameters.setJpegQuality (JPEG_QUALITY);
			parameters.setPictureSize (pictureSize.width,pictureSize.height);
			cameraSelected.setParameters (parameters);
			// ---------------------------------------------------------------------
			// 06/09/2019 ECU set up the dummy 'texture view'
			// ---------------------------------------------------------------------
			cameraSelected.setPreviewTexture (new SurfaceTexture(0));
			// ---------------------------------------------------------------------
			// 06/09/2019 ECU set up the preview for the selected camera
			// ---------------------------------------------------------------------
			cameraSelected.startPreview ();
			// ---------------------------------------------------------------------
			// 06/09/2019 ECU now take the photo
			// ---------------------------------------------------------------------
			cameraSelected.takePicture(null, null, new Camera.PictureCallback () 
			{
				// -----------------------------------------------------------------
				@Override
				public void onPictureTaken (byte[] theData, Camera theCamera) 
				{
					// -------------------------------------------------------------
					// 06/09/2019 ECU release the camera's resources
					// 02/10/2019 ECU changed to use cameraSelected from theCamera
					//                and set to null to confirm the release
					//            ECU use the method for the release
					// -------------------------------------------------------------
					releaseCamera ();
					// -------------------------------------------------------------
					// 06/09/2019 ECU now try and work out the brightness
					// -------------------------------------------------------------
					// 06/09/2019 ECU generate a bitmap from the received 'jpeg' data
					// -------------------------------------------------------------
					Bitmap bitmap = BitmapFactory.decodeByteArray (theData,0,theData.length);
					// -------------------------------------------------------------
					int bitmapHeight 	= bitmap.getHeight();
					int bitmapWidth 	= bitmap.getWidth ();
					// -------------------------------------------------------------
					int pixel;
					int totalBrightness = 0;
					// -------------------------------------------------------------
					// 06/09/2019 ECU now scan through the pixels in the bitmap to try
					//                and calculate a 'relative' brightness
					// -------------------------------------------------------------
					for (int h = 0; h < bitmapHeight; h++)
					{
						for (int w = 0; w < bitmapWidth; w++)
						{
							// -----------------------------------------------------
							// 06/09/2019 ECU get the pixel at the current position
							// -----------------------------------------------------
							pixel = bitmap.getPixel (w,h);
							// -----------------------------------------------------
							// 06/09/2019 ECU add into the 'total' brightness
							//
							//                 just add the values of (red + green + blue)
							//                 and then divide by 3 - this is only a rough
							//                 uncalibrated value which is in no way 'absolute'
							// -----------------------------------------------------
							totalBrightness += ((Color.red (pixel) + Color.green (pixel) + Color.blue (pixel)) / 3);
							// -----------------------------------------------------
						}
					}
					// -------------------------------------------------------------
					// 06/09/2019 ECU now work out an 'average' brightness
					// -------------------------------------------------------------
					int brightness = totalBrightness / (bitmapHeight * bitmapWidth);
					// -------------------------------------------------------------
					// 11/09/2019 ECU if in debug mode then show the image and brightness
					// -------------------------------------------------------------
					if (PublicData.storedData.debugMode)
					{
						MessageHandler.popToast ("Brightness : " + brightness, bitmap);
					}
					// -------------------------------------------------------------
					// 09/09/2019 ECU call the method to handle the brightness
					// -------------------------------------------------------------
					brightnessObtained (brightness);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			});
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 29/09/2019 ECU log the exception so that further investigation can 
			//                take place
			// 02/10/2019 ECU the likely cause of an exception is a 'runtime' one
			//                due to 'Fail to connect to camera service'. To restart
			//                things then do a 'stop' followed by a 'start'
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG,"Exception : " + 
											theException + StaticData.NEWLINE + 
												Utilities.stackTrace (theException));
			// ---------------------------------------------------------------------
			// 02/10/2019 ECU stop the capturing of the brightness
			// ---------------------------------------------------------------------
			stopCapture ();
			// ---------------------------------------------------------------------
			// 02/10/2019 ECU now try and restart the capture
			// ---------------------------------------------------------------------
			startCapture (rate);
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	
	// =============================================================================
	void startCapture (final int theRate)
	{
		// -------------------------------------------------------------------------
		// 01/10/2019 ECU changed from debugPopToast
		// -------------------------------------------------------------------------
		Utilities.debugMessage (TAG,"startCapture");
		// -------------------------------------------------------------------------
		// 08/09/2019 ECU created to instigate the obtaining of the brightness using
		//                the camera
		// -------------------------------------------------------------------------
		capturing		 	= true;
		rate				= theRate;
		// -------------------------------------------------------------------------
		// 08/09/2019 ECU declare the handler that will request the brightness
		// -------------------------------------------------------------------------
		brightnessHandler = new BrightnessHandler ();
		// -------------------------------------------------------------------------
		// 08/09/2019 ECU now start the monitor
		// 01/10/2019 ECU added the initial delay
		// -------------------------------------------------------------------------
		brightnessHandler.sendEmptyMessageDelayed (StaticData.MESSAGE_AMBIENT_LIGHT, 30*1000);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void releaseCamera ()
	{
		// -------------------------------------------------------------------------
		// 02/10/2019 ECU called up to release the camera - if necessary
		// -------------------------------------------------------------------------
		if (cameraSelected != null)
		{
			// ---------------------------------------------------------------------
			// 02/10/2019 ECU seems that the camera has not been released so do so
			// ---------------------------------------------------------------------
			cameraSelected.release ();
			// ---------------------------------------------------------------------
			// 02/10/2019 ECU indicate that the camera has been released
			// ---------------------------------------------------------------------
			cameraSelected = null;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void stopCapture ()
	{
		// -------------------------------------------------------------------------
		// 01/10/2019 ECU changed from debugPopToast
		// -------------------------------------------------------------------------
		Utilities.debugMessage (TAG,"stopCapture");
		// -------------------------------------------------------------------------
		// 02/10/2019 ECU release the camera just in case
		// -------------------------------------------------------------------------
		releaseCamera ();
		// -------------------------------------------------------------------------
		// 08/09/2019 ECU called with no arguments to stop the capturing
		// -------------------------------------------------------------------------
		capturing = false;
		// -------------------------------------------------------------------------
		// 09/09/2019 ECU make sure any queued messages are removed
		// -------------------------------------------------------------------------
		if (brightnessHandler != null)
		{
			brightnessHandler.removeMessages (StaticData.MESSAGE_AMBIENT_LIGHT);
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}
// =================================================================================

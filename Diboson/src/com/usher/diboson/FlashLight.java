package com.usher.diboson;

import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Message;
import android.widget.ImageView;

public class FlashLight 
{
	// =============================================================================
	// 29/12/2016 ECU created to hold methods which relate to the in-built 'flashlight'
	//                (or 'flash' LED) that may exist on the device on which the app
	//                is running
	//            ECU NOTE - usually the LED is on the back of the device and is 
	//                ====   associated with the camera along side it (this will
	//                       be called the 'backCamera'; the camera associated with
	//                       the main screen will be the 'frontCamera'.
	// =============================================================================
	
	// =============================================================================
	// 29/12/2016 ECU declare actions that will be passed in a message
	// -----------------------------------------------------------------------------
	private static final int	TORCH_OFF	= 0;
	private static final int	TORCH_ON	= 1;
	// =============================================================================
	
	// =============================================================================
	// 29/12/2016 ECU declare any variables needed within the class
	// -----------------------------------------------------------------------------
	private static Camera 		backCamera  = null;		// 29/12/2016 ECU changed name
	private static Context		context;
	// =============================================================================
	
	// =============================================================================
	public static void delayedFlashLightAction (Context theContext,boolean theRequiredState,int theDelay)
	{
		// -------------------------------------------------------------------------
		// 29/12/2016 ECU create to switch on/off the flashlight after a given delay
		//				  	theRequiredState	true	..... switch flashlight on
		//										false   ..... switch flashlight off
		//                  theDelay			required delay in milliseconds
		// -------------------------------------------------------------------------
		int localAction;
		// -------------------------------------------------------------------------
		// 29/12/2016 ECU decide on the action to take
		// -------------------------------------------------------------------------
		if (theRequiredState)
		{
			// ---------------------------------------------------------------------
			// 29/12/2016 ECU want to switch the flashlight on and then after the
			//                specified time then turn it off
			// ---------------------------------------------------------------------
			flashLightOn (theContext);
			// ---------------------------------------------------------------------
			// 29/12/2016 ECU indicate the action to be taken after the delay
			// ---------------------------------------------------------------------
			localAction = TORCH_OFF;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 29/12/2016 ECU want to switch the flashlight off and then after the
			//                specified time then turn it back on
			// ---------------------------------------------------------------------
			flashLightOff (theContext);
			// ---------------------------------------------------------------------
			// 29/12/2016 ECU indicate the action to be taken after the delay
			// ---------------------------------------------------------------------
			localAction = TORCH_ON;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 29/12/2016 ECU remember the context for future use
		// -------------------------------------------------------------------------
		context = theContext;
		// -------------------------------------------------------------------------
		// 29/12/2016 ECU now generate the delay message and send it
		// ------------------------------------------------------------------------- 
		Message localMessage = PublicData.messageHandler.obtainMessage (StaticData.MESSAGE_METHOD,
																		localAction,
																		StaticData.NO_RESULT,
													new MethodDefinition<FlashLight> (FlashLight.class,"PostDelayMethod"));
		PublicData.messageHandler.sendMessageDelayed (localMessage,theDelay);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean flashLightCheck (boolean theReleaseFlag)
	{
		// -------------------------------------------------------------------------
		// 25/08/2015 ECU created to check whether the device has an LED which is
		//                used as the camera's flash
		// 29/12/2016 ECU changed the name from 'localCamera'
		// 14/06/2019 ECU added the release flag
		// -------------------------------------------------------------------------
		backCamera = Camera.open();
		// -------------------------------------------------------------------------
		// 25/08/2015 ECU check if the forward facing camera exists
		// -------------------------------------------------------------------------
        if (backCamera == null) 
        {
	            return false;    
        }
        else
        {
        	// ---------------------------------------------------------------------
        	// 25/08/2015 ECU camera exists so get its parameters
        	// ---------------------------------------------------------------------
	        Camera.Parameters cameraParameters = backCamera.getParameters();
	        // ---------------------------------------------------------------------
	        // 25/08/2015 ECU if there is no flash mode then indicate this fact
	        // ---------------------------------------------------------------------
	        if (cameraParameters.getFlashMode() == null) 
	        {
	            return false;
	        }
	        else
	        {
	        	// -----------------------------------------------------------------
	        	// 25/08/2015 ECU get the list of supported flash modes
	        	// -----------------------------------------------------------------
	        	List<String> supportedFlashModes 
	        		= cameraParameters.getSupportedFlashModes();
	        	// -----------------------------------------------------------------
	        	// 25/08/2015 ECU check for modes that indicate 'no flash'
	        	// -----------------------------------------------------------------
	        	if (supportedFlashModes == null || 
	        		supportedFlashModes.isEmpty() || 
	        		(supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF))) 
	        			return false;
	        }
	        // ---------------------------------------------------------------------
	        // 14/06/2019 ECU release the camera if required
	        // ---------------------------------------------------------------------
	        if (theReleaseFlag)
	        	backCamera.release ();
	        // ---------------------------------------------------------------------
	        // 25/08/2015 ECU this device has a 'flash'
	        // ---------------------------------------------------------------------
	        return true;
	        // ---------------------------------------------------------------------
	    }
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void flashLightDelayToggle (Context theContext) 
	{
		// -------------------------------------------------------------------------
		// 30/12/2016 ECU created to handle the delayed toggling of the torch
		// -------------------------------------------------------------------------
		context = theContext;
		// -------------------------------------------------------------------------
		// 30/12/2016 ECU request the time that the operation, either torch on or off,
		//                will last
		// -------------------------------------------------------------------------
		DialogueUtilities.sliderChoice (context,
										theContext.getString (R.string.flashlight_time),
										theContext.getString (R.string.flashlight_summary) + 
											((backCamera == null) ? theContext.getString (R.string.on) 
													              : theContext.getString (R.string.off)),
										R.drawable.timer,
										null,
										30,
										1,
										60 * 5,
										theContext.getString (R.string.set_time),
										Utilities.createAMethod (FlashLight.class,"OperationTimeMethod",0),
										theContext.getString (R.string.cancel_operation));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void flashLightOn (Context theContext) 
	{
		// -------------------------------------------------------------------------
		// 10/02/2014 ECU try and switch a flash light on
		// 08/02/2015 ECU changed to static
		// 23/05/2015 ECU added theContext as an argument
		// 29/12/2016 ECU changed name from 'localCamera'
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 23/05/2015 ECU try and open to the first backward facing camera - returns
			//                'null' if there isn't one
			// ---------------------------------------------------------------------
			backCamera = Camera.open();
			// ---------------------------------------------------------------------
			// 10/02/2014 ECU if there is no backward facing camera then hence no torch
			// ---------------------------------------------------------------------
			if (backCamera != null)
			{
				// -----------------------------------------------------------------
				// 23/05/2015 ECU set it so that the LED is on constantly when the
				//                camera is in preview mode. Then start the capture
				// -----------------------------------------------------------------
				Parameters parameters = backCamera.getParameters ();
				parameters.setFlashMode (Parameters.FLASH_MODE_TORCH);
				backCamera.setParameters (parameters);
				backCamera.startPreview ();
				// -----------------------------------------------------------------
				// 29/12/2016 ECU make sure the 'torch icon' is updated
				// -----------------------------------------------------------------
				GridActivity.gridRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_ADAPTER); 
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 23/05/2015 ECU changed to use resource
				// -----------------------------------------------------------------
				Utilities.popToast (theContext.getString (R.string.no_backwards_camera));
				// -----------------------------------------------------------------
			}
		} 
		catch (Exception theException) 
		{
			theException.printStackTrace();
		}
	}
	/* ============================================================================= */
	public static void flashLightOff (Context theContext) 
	{
		// -------------------------------------------------------------------------
		// 10/02/2014 ECU try and switch a flash light off
		// 08/02/2015 ECU changed to static
		// 23/05/2015 ECU added theContext as an argument
		// 29/12/2016 ECU changed from 'localCamera'
		// -------------------------------------------------------------------------
		try 
		{
			if (backCamera != null)
			{
				// -----------------------------------------------------------------
				// 23/05/2015 ECU stop capture, release resources and then reset the
				//                variable to indicate camera not in use
				// -----------------------------------------------------------------
				backCamera.stopPreview ();
				backCamera.release ();
				backCamera = null;
				// -----------------------------------------------------------------
				// 29/12/2016 ECU make sure the 'torch icon' is updated
				// -----------------------------------------------------------------
				GridActivity.gridRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_ADAPTER); 
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 23/05/2015 ECU changed to use resource
				// -----------------------------------------------------------------
				Utilities.popToast (theContext.getString (R.string.no_backwards_camera));
				// -----------------------------------------------------------------
			}
        } 
		catch (Exception theException) 
        {
        	theException.printStackTrace();
	    }
	}
	/* ============================================================================= */
	public static void flashLightToggle (Context theContext) 
	{
		// -------------------------------------------------------------------------
		// 10/02/2014 ECU try and toggle the flashlight
		// 08/02/2015 ECU changed to static
		// 23/05/2015 ECU added theContext as argument
		// 29/12/2016 ECU changed from 'localCamera'
		// -------------------------------------------------------------------------
		if (backCamera == null)
			flashLightOn (theContext);
		else
			flashLightOff (theContext);
	}
	// ==============================================================================
	public static void flashLightUpdateImageView (ImageView theImageView)
	{
		// -------------------------------------------------------------------------
		// 29/12/2016 ECU created to update the image view depending on whether 
		//                the torch is on or not
		// -------------------------------------------------------------------------
		if (backCamera != null)
			theImageView.setImageResource (R.drawable.torch_on);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	// 29/12/2016 ECU declare any methods which are invoked via their definition
	//                that was passed as an argument
	// =============================================================================
	public static void OperationTimeMethod (int theDelay)
	{
		// -------------------------------------------------------------------------
		// 30/12/2016 ECU created to accept the delay time (in seconds) and set the
		//                required action - the delay will be in milliseconds
		// -------------------------------------------------------------------------
		delayedFlashLightAction (context,(backCamera == null),(theDelay * 1000));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void PostDelayMethod (int theAction)
	{
		// -------------------------------------------------------------------------
		// 29/12/2016 ECU created to be called after a delay to take the appropriate
		//                action
		// -------------------------------------------------------------------------
		switch (theAction)
		{	
			// ---------------------------------------------------------------------
			case TORCH_OFF:
				// -----------------------------------------------------------------
				// 29/12/2016 ECU switch the flashlight off
				// -----------------------------------------------------------------
				flashLightOff (context);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case TORCH_ON:
				// -----------------------------------------------------------------
				// 29/12/2016 ECU switch the flashlight on
				// -----------------------------------------------------------------
				flashLightOn (context);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			default:
				break;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================

}

package com.usher.diboson;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class PositiveFeedback 
{
	// =============================================================================
	// 05/07/2018 ECU this class contains methods associated with 'positive feedback'
	//                to user input
	// 08/01/2019 ECU added 'fade' as an effect
	// =============================================================================
	
	// =============================================================================
	public static void UserAction (final Context 	theContext,
								   final boolean    theCheckFlag,
								   final View 		theView,
								   final Intent 	theIntent,
								   final int 		theResultCode)
	{
		// -------------------------------------------------------------------------
		// 05/07/2018 ECU created to be called when a user action is taken
		// 24/01/2019 ECU added theCheckFlag
		// -------------------------------------------------------------------------
		// 05/07/2018 ECU only process if the flag is set
		// 24/01/2019 ECU changed to 'theCheckFlag' from 
		//				  'PublicData.storedData.positiveFeedback'
		// 18/92/2020 ECU the object animator only came in at API 11 so add to the check
		// -------------------------------------------------------------------------
		if (theCheckFlag && (android.os.Build.VERSION.SDK_INT > 10))
		{
			// ---------------------------------------------------------------------
			// 05/07/2018 ECU just do some animation as 'positive feedback'
			// ---------------------------------------------------------------------
			ObjectAnimator objectAnimator;
			// ---------------------------------------------------------------------
			if (PublicData.storedData.positiveFeedbackEffect == null ||
				PublicData.storedData.positiveFeedbackEffect.equalsIgnoreCase ("shakeX"))
			{
				objectAnimator = ObjectAnimator.ofFloat (theView, "translationX",0,25,-25,25,-25,15,-15,6,-6,0);	
			}
			else
			if (PublicData.storedData.positiveFeedbackEffect.equalsIgnoreCase ("shakeY"))
			{
				objectAnimator = ObjectAnimator.ofFloat (theView, "translationY",0,25,-25,25,-25,15,-15,6,-6,0);
			}
			else
			if (PublicData.storedData.positiveFeedbackEffect.equalsIgnoreCase ("rotation"))
			{
				objectAnimator = ObjectAnimator.ofFloat (theView,"rotation",0, 360 * 5);
			}
			else
			if (PublicData.storedData.positiveFeedbackEffect.equalsIgnoreCase ("rotationX"))
			{
				objectAnimator = ObjectAnimator.ofFloat (theView,"rotationX",0, 360 * 5);
			}
			else
			if (PublicData.storedData.positiveFeedbackEffect.equalsIgnoreCase ("scale"))
			{
				objectAnimator = ObjectAnimator.ofPropertyValuesHolder(theView, 
																	   PropertyValuesHolder.ofFloat ("scaleX", 0.6f),
																	   PropertyValuesHolder.ofFloat ("scaleY", 0.6f));
				// -----------------------------------------------------------------
				// 06/07/2018 ECU want to restore the view to its original size
				// -----------------------------------------------------------------
				objectAnimator.setRepeatCount (1);
				objectAnimator.setRepeatMode (ObjectAnimator.REVERSE);
				// -----------------------------------------------------------------
			}
			else
			if (PublicData.storedData.positiveFeedbackEffect.equalsIgnoreCase ("fade"))
			{
				// -----------------------------------------------------------------
				// 08/01/2019 ECU added the 'fade' option as positive feedback
				// -----------------------------------------------------------------
				objectAnimator = ObjectAnimator.ofFloat (theView, "alpha",1f,0f);
				// -----------------------------------------------------------------
				// 08/01/2019 ECU want to restore the view to its original state
				// -----------------------------------------------------------------
				objectAnimator.setRepeatCount (1);
				objectAnimator.setRepeatMode (ObjectAnimator.REVERSE);
				// -----------------------------------------------------------------
			}
			else
			{
				objectAnimator = ObjectAnimator.ofFloat (theView,"rotationY",0, 360 * 5);
			}
			// ---------------------------------------------------------------------
			// 05/07/218 ECU set the duration of the animation and the interpolation
			//               type
			// ---------------------------------------------------------------------
			objectAnimator.setDuration (1000);
			objectAnimator.setInterpolator (new AccelerateDecelerateInterpolator());
			// ---------------------------------------------------------------------
			// 05/07/2018 ECU add the listeners
			// ---------------------------------------------------------------------
			objectAnimator.addListener(new ObjectAnimator.AnimatorListener() 
			{
				// -----------------------------------------------------------------
				@Override
				public void onAnimationCancel (Animator arg0) 
				{
					
				}
				// -----------------------------------------------------------------
				@Override
				public void onAnimationEnd (Animator arg0) 
				{
					// -------------------------------------------------------------
					// 05/07/2018 ECU the animation has finished so now take the
					//                required action
					// 23/01/2019 ECU put in the check for 'null' if nothing is to
					//                done when the animation ends
					// -------------------------------------------------------------
					// 05/07/2018 ECU positive feedback is required
					// -------------------------------------------------------------
					if (theIntent != null)
					{
						if (theResultCode == StaticData.NO_RESULT)
							theContext.startActivity (theIntent);
						else
							((Activity)theContext).startActivityForResult (theIntent,theResultCode);
						// ---------------------------------------------------------
						// 17/08/2018 ECU indicate that user actions are to be actioned
						// ---------------------------------------------------------
						GridActivity.gridRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_USER_ACTIONS);
						// ---------------------------------------------------------
					}
				}
				// -----------------------------------------------------------------
				@Override
				public void onAnimationRepeat(Animator arg0) 
				{
						
				}
				// -----------------------------------------------------------------
				@Override
				public void onAnimationStart (Animator arg0) 
				{
					
				}
               	// -----------------------------------------------------------------
            });
			// ---------------------------------------------------------------------
			// 05/07/2018 ECU now start up the animation
			// ---------------------------------------------------------------------
			objectAnimator.start ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void UserAction (final Context 	theContext,
			   					   final View 		theView)
	{
		// -------------------------------------------------------------------------
		// 23/01/2019 ECU created when no actions are to be taken when the animation
		//                ends - indicated by specifying the 'null'
		// 24/01/2019 ECU added 'true' so that the effect happens irrespective of
		//                whether positive feedback is enabled or not
		// -------------------------------------------------------------------------
		UserAction (theContext,true,theView,null,StaticData.NOT_SET);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	// =============================================================================
}

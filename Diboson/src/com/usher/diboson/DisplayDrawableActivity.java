package com.usher.diboson;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;


public class DisplayDrawableActivity extends Activity implements OnGestureListener
{
	// =======================================================================
	// 30/05/2013 ECU created
	// 17/02/2014 ECU pass through the resource id of the required image instead
	//                of using MainActivity.helpID by default. This was causing
	//                a problem if two tasks, like the light sensor in GridActivity,
	//                both want to access the variable at the same time
	// 14/04/2015 ECU changed variable names helpID -> displayImage
	//                helpText -> displayText
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 29/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 08/08/2016 ECU Note - changed 'activity_help' so that the ImageView is
	//                       scaled using 'fitXY' to fill the screen. Needed
	//                       following the change of 07/08/2016 to use
	//                       'full screen'
	// 09/08/2016 ECU change the scaling to be 'settable' here rather than
	//                set in the XML file so remove the mods of 08/08/2016
	// 23/12/2016 ECU changed from DibosonActivity to Activity because a problem
	//                occurred when trying to start the dialogue to 'add test
	//                documentation or capture screen' (via a long press on the
	//				  BACK KEY) and, because this activity is transitory, the 
	//				  view used by the dialogue can disappear and create an error
	//
	//					FATAL EXCEPTION: main
	//						Process: com.usher.diboson, PID: 16679
	//							android.view.WindowManager$BadTokenException: Unable to add window -- token android.os.BinderProxy@290bd28c is not valid; is your activity running?
 	//							at android.view.ViewRootImpl.setView(ViewRootImpl.java:579)
 	//							at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:282)
 	//							at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:85)
 	//							at android.app.Dialog.show(Dialog.java:298)
 	//							at com.usher.diboson.DialogueUtilities.adjustFonts(DialogueUtilities.java:1835)
 	//							at com.usher.diboson.DialogueUtilities.yesNo(DialogueUtilities.java:1800)
 	//							at com.usher.diboson.DibosonActivity.onKeyLongPress(DibosonActivity.java:105)
 	//							at android.view.KeyEvent.dispatch(KeyEvent.java:2639)
 	//							at android.app.Activity.dispatchKeyEvent(Activity.java:2717)
 	//							at com.android.internal.policy.impl.PhoneWindow$DecorView.dispatchKeyEvent(PhoneWindow.java:2276)
 	//							at android.view.ViewRootImpl$ViewPostImeInputStage.processKeyEvent(ViewRootImpl.java:4090)
 	//							at android.view.ViewRootImpl$ViewPostImeInputStage.onProcess(ViewRootImpl.java:4052)
 	//							at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:3604)
 	//							at android.view.ViewRootImpl$InputStage.onDeliverToNext(ViewRootImpl.java:3657)
 	//							at android.view.ViewRootImpl$InputStage.forward(ViewRootImpl.java:3623)
 	//							at android.view.ViewRootImpl$AsyncInputStage.forward(ViewRootImpl.java:3740)
 	//							at android.view.ViewRootImpl$InputStage.apply(ViewRootImpl.java:3631)
 	//							at android.view.ViewRootImpl$AsyncInputStage.apply(ViewRootImpl.java:3797)
 	//							at android.view.ViewRootImpl$InputStage.deliver
	//
	//				  as this is not a 'user activity' the ability to add test documentation (via long press on 
	//                the BACK KEY is not necessary
	// -----------------------------------------------------------------------
	// Testing
	// =======
	//========================================================================
	/* ======================================================================= */
	   public static final int DEFAULT_WAIT_TIME = 5000;	// 11/09/2013 ECU default wait time in milliseconds
	   														//                before terminating the task -
	   														//                change by incoming extra
	/* ----------------------------------------------------------------------- */
	   int				displayImage 	= R.drawable.help;	// 17/02/2014 ECU added
	   String			displayText 	= null;				// 13/02/2014 ECU added
	   boolean          flingEnabled 	= true;				// 15/09/2013 ECU added
	   GestureDetector	gestureScanner;
	   ImageView		imageView;
	   boolean			scaleImage		= false;			// 09/08/2016 ECU added
	   int				textLayout		= StaticData.NO_RESULT;	
	   														// 11/10/2016 ECU added
	   TextView         textView;							// 13/02/2014 ECU added
	   int				waitTime		= DEFAULT_WAIT_TIME;
	/* ============================================================================= */
	@SuppressWarnings ("deprecation")
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 16/02/2014 ECU call up routine to set common activity features
			// 07/08/2016 ECU added the 'full screen' flag
			// 08/08/2016 ECU Note - see comment above re 'fitXY'
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			// ---------------------------------------------------------------------		 		
			setContentView (R.layout.activity_help);
			// ---------------------------------------------------------------------
			// 11/09/2013 ECU check if any parameters have been supplied
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
		
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 13/02/2014 ECU changed to use PARAMETER_ ......
				// -----------------------------------------------------------------
				waitTime = extras.getInt (StaticData.PARAMETER_WAIT_TIME,DEFAULT_WAIT_TIME);
				// -----------------------------------------------------------------
				// 15/09/2013 ECU added the fling enabling flag
				// 18/02/2014 ECU changed to use PARAMETER
				// -----------------------------------------------------------------
				flingEnabled = extras.getBoolean (StaticData.PARAMETER_FLING_ENABLED, true);
				// -----------------------------------------------------------------
				// 13/02/2014 ECU try and get the help text parameter
				// -----------------------------------------------------------------
				displayText = extras.getString (StaticData.PARAMETER_HELP_TEXT);
				// -----------------------------------------------------------------
				// 17/02/2014 ECU get help id or use a default
				// -----------------------------------------------------------------
				displayImage = extras.getInt (StaticData.PARAMETER_HELP_ID, R.drawable.help); 
				// -----------------------------------------------------------------
				// 09/08/2016 ECU check if scaling is required
				// -----------------------------------------------------------------
				scaleImage = extras.getBoolean (StaticData.PARAMETER_SCALE,false);
				// -----------------------------------------------------------------
				// 11/10/2016 ECU check if a text layout has been specified
				// -----------------------------------------------------------------
				textLayout = extras.getInt (StaticData.PARAMETER_LAYOUT, StaticData.NO_RESULT);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 17/02/2014 ECU change to use local 'helpID' instead of the one in 'MainActivity'
			// ---------------------------------------------------------------------
			imageView = (ImageView) this.findViewById (R.id.help_image);
			// ---------------------------------------------------------------------
			// 09/08/2016 ECU check if the image view is to be scaled
			// ---------------------------------------------------------------------
			if (scaleImage)
			{
				imageView.setScaleType (ScaleType.FIT_XY);
			}
			// ---------------------------------------------------------------------
			// 09/08/2016 ECU Note - now set the image
			// ---------------------------------------------------------------------
			imageView.setImageResource (displayImage);
			// ---------------------------------------------------------------------
			// 11/09/2015 ECU set up the opacity that is required
			// ---------------------------------------------------------------------
			imageView.setAlpha (PublicData.storedData.drawableOpacity);
			// ---------------------------------------------------------------------
			// 30/05/2013 ECU this activity will handle the gestures
			// ---------------------------------------------------------------------
			gestureScanner = new GestureDetector (this,this);
			// ---------------------------------------------------------------------
			// 12/02/2014 ECU determine if this is for text or an image
			// ---------------------------------------------------------------------
			if (displayText != null)
			{
				// -----------------------------------------------------------------
				// 13/02/2014 ECU now display the text
				// 09/10/2016 ECU changed from .help_text_layout
				// 11/10/2016 ECU check the layout to be used
				// -----------------------------------------------------------------
				if (textLayout == StaticData.NO_RESULT)
					setContentView (R.layout.help_text_layout);
				else
					setContentView (textLayout);
				// -----------------------------------------------------------------
				// 13/02/2014 ECU get text view
				// -----------------------------------------------------------------
				textView = (TextView) findViewById (R.id.help_textview);
				// -----------------------------------------------------------------
				// 01/04/2016 ECU decide whether the text is of HTML format or not
				//                - files requiring HTML must start with the HTML_INTRODUCER
				// ------------------------------------------------------------------
				if (displayText.startsWith (StaticData.HTML_INTRODUCER))
				{
					// -------------------------------------------------------------
					// 01/04/2016 ECU the file is has HTML commands
					// -------------------------------------------------------------
					textView.setText (Html.fromHtml (displayText));
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 01/04/2016 ECU just normal text
					// -------------------------------------------------------------
					textView.setText (displayText);
					// --------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 30/05/2013 ECU this activity will handle the gestures
				// -----------------------------------------------------------------
				gestureScanner = new GestureDetector(this,this);
			}
			// ---------------------------------------------------------------------
			// 11/09/2013 ECU check if need to wait before terminating this activity
			//                0 means that the activity stays until the user taps
			//                the screen
			// ---------------------------------------------------------------------
			if (waitTime > 0)
			{
				// -----------------------------------------------------------------
				// 30/05/2013 ECU wait a bit before exiting
				// -----------------------------------------------------------------
				waitABitThenFinish (waitTime);
			}
			else
			{
				// -----------------------------------------------------------------
				// 11/09/2013 ECU tell the user to touch the screen to remove the help
				//                screen
				// 22/09/2013 ECU add the parameters to centre and show for a short time
				// -----------------------------------------------------------------
				Utilities.popToast (getString (R.string.touch_to_clear_help),true,Toast.LENGTH_SHORT);
			}
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
	/* ================================================================================ */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return true;
	}
	/* ============================================================================ */
	@Override
    public void onDestroy()
    {	
        super.onDestroy();
    }
	/* ============================================================================ */
	@Override 
	protected void onResume() 
	{ 
	   	super.onResume(); 
	} 
	/* ============================================================================ */
	@Override 
	protected void onPause() 
	{ 
	   	super.onPause(); 
	} 
	/* ============================================================================ */
	private void waitABitThenFinish (final int theWaitTime)
	{
		// ------------------------------------------------------------------------
		// 13/09/2013 ECU this method creates a thread which will wait for a short
		//                period before causing the activity to 'finish'
		// ------------------------------------------------------------------------
		Thread thread = new Thread ()
		 {
		        @Override
		        public void run ()
		        {
		            try 
		            {
		                synchronized (this)
		                {
		                	// -----------------------------------------------------
		                	// 15/09/2013 ECU wait for the specified time
		                	// -----------------------------------------------------
		                    wait (theWaitTime);
		                    // -----------------------------------------------------
		                    // 15/09/2013 ECU 'finish' this activity
		                    // -----------------------------------------------------
		                    finish ();
		                    // -----------------------------------------------------
		                }
		            }
		            catch(InterruptedException theException)
		            {                    
		            }       
		        }
		    };
		    // ---------------------------------------------------------------------
		    // 15/09/2013 ECU start up the thread
		    // ---------------------------------------------------------------------
		    thread.start(); 
		    // ---------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
    public boolean onTouchEvent (MotionEvent motionEvent) 
	{
		// -------------------------------------------------------------------------
		// 15/09/2013 pass the event through to the gesture handler
		// -------------------------------------------------------------------------
		return gestureScanner.onTouchEvent (motionEvent);
		// -------------------------------------------------------------------------
	}
    /* ============================================================================= */
	@Override
	public boolean onDown (MotionEvent motionEvent) 
	{
		return false;
	}
	/* ============================================================================= */
	@Override
	public boolean onFling (MotionEvent motionEvent, 
							MotionEvent motionEvent2, 
							float velocityX, 
							float velocityY) 
	{
		// -------------------------------------------------------------------------
		// 15/09/2013 ECU check if enabled or not
		// -------------------------------------------------------------------------
		if (flingEnabled)
		{
			// ---------------------------------------------------------------------
			// 15/09/2013 ECU changed from album_art_view
			// ---------------------------------------------------------------------
			ImageView myImageView = (ImageView) this.findViewById (R.id.help_image);
			myImageView.setImageResource (R.drawable.help_page2);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		return false;
	}
	/* ============================================================================= */
	@Override
	public void onLongPress (MotionEvent motionEvent) 
	{
	}
	/* ============================================================================= */
	@Override
	public boolean onScroll (MotionEvent motionEvent1, 
							 MotionEvent motionEvent2,
							 float distanceX,
							 float distanceY) 
	{
		return false;
	}
	/* ============================================================================= */
	@Override
	public void onShowPress (MotionEvent motionEvent) 
	{

	}
	/* ============================================================================= */
	@Override
	public boolean onSingleTapUp (MotionEvent motionEvent) 
	{
		// -------------------------------------------------------------------------
		// 15/09/2013 ECU finish this activity
		// -------------------------------------------------------------------------
		finish ();
		return false;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override   
	public boolean dispatchTouchEvent (MotionEvent motionEvent) 
	{   
		// -------------------------------------------------------------------------
		// 13/02/2014 ECU added this in order to detect the single tape when the
		//                help text is displayed which uses a scroll view
		// -------------------------------------------------------------------------
		gestureScanner.onTouchEvent (motionEvent);   
		return super.dispatchTouchEvent (motionEvent);   
	} 
	/* ============================================================================= */
}

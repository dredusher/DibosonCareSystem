package com.usher.diboson;

import com.usher.diboson.util.SystemUiHider;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class ExerciseActivity extends DibosonActivity 
{
	/* ============================================================================= */
	// 21/03/2014 ECU created to handle interface to exercise activities
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 29/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	/* ============================================================================= */
	private static final boolean 	AUTO_HIDE 				= true;
	private static final int 		AUTO_HIDE_DELAY_MILLIS 	= 5000;
	private static final int 		FADE_TIME 				= 2000;
	private static final int 		HIDER_FLAGS 			= SystemUiHider.FLAG_HIDE_NAVIGATION;
	private static final boolean 	TOGGLE_ON_CLICK 		= true;
	/* ============================================================================= */
	private TextView 		contentView;
	private View 			controlsView;
	private SystemUiHider 	systemUiHider;
	/* ============================================================================= */
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 08/04/2014 ECU changed to use the variable
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			
			setContentView(R.layout.activity_exercise);
		
			controlsView = findViewById(R.id.fullscreen_content_controls);
			contentView  = (TextView)findViewById(R.id.fullscreen_content);
		
			contentView.setText ("");
			// ---------------------------------------------------------------------
			// 21/03/2014 ECU set up the listeners for the buttons
			// ---------------------------------------------------------------------
			findViewById(R.id.button_visual_game).setOnClickListener(buttonListener);
			findViewById(R.id.button_dexterity_game).setOnClickListener(buttonListener);
			// ---------------------------------------------------------------------
			// 21/03/2014 ECU set up an instance of the 'user interface' so that it can
			//                be controlled - this is based on 'contentView'
			// ---------------------------------------------------------------------
			systemUiHider = SystemUiHider.getInstance (this,contentView,HIDER_FLAGS);
			systemUiHider.setup();
			// ---------------------------------------------------------------------
			systemUiHider.setOnVisibilityChangeListener (new SystemUiHider.OnVisibilityChangeListener() 
			{
				// -----------------------------------------------------------------
				int controlsHeight;
				int shortAnimationTime;
				// -----------------------------------------------------------------
				@Override
				@TargetApi (Build.VERSION_CODES.HONEYCOMB_MR2)
				public void onVisibilityChange (boolean visible) 
				{
					// -------------------------------------------------------------
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) 
					{
						// ---------------------------------------------------------
						// 21/03/2014 ECU at this API level then an animator can be used
						// ---------------------------------------------------------
						if (controlsHeight == 0) 
							controlsHeight = controlsView.getHeight();
					
						if (shortAnimationTime == 0) 
							shortAnimationTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
						// ---------------------------------------------------------
						// 21/03/2014 ECU set up the required animation
						// ---------------------------------------------------------
						controlsView.animate().translationY (visible ? 0 : controlsHeight).setDuration(shortAnimationTime);
						// ---------------------------------------------------------
					} 
					else 
					{
						// ---------------------------------------------------------
						// 21/03/2014 ECU if the animator is not available then just
						//                toggle the visibility
						// ---------------------------------------------------------
						controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
						// ---------------------------------------------------------						
					}
					// -------------------------------------------------------------
					// 21/03/2014 ECU check if need to set up the auto-hide option for
					//                the controls
					// -------------------------------------------------------------
					if (visible && AUTO_HIDE) 
					{
						// ---------------------------------------------------------
						// 21/03/2014 ECU if the controls are being made visible then
						//                set up the timer to hide them
						// ---------------------------------------------------------
						delayedHide(AUTO_HIDE_DELAY_MILLIS);
						
						// ---------------------------------------------------------
						// 21/03/2014 ECU remove the text with a bit of animation
						// ---------------------------------------------------------
						Utilities.AnimateATextView (contentView,"",FADE_TIME,false);
					}
				}
			});
			// ---------------------------------------------------------------------
			contentView.setOnClickListener(new View.OnClickListener() 
			{
				// -----------------------------------------------------------------
				// 21/03/2014 ECU manually hide or display the system UI
				// -----------------------------------------------------------------
				@Override
				public void onClick(View view) 
				{
					if (TOGGLE_ON_CLICK)
					{
						systemUiHider.toggle();
					}
					else 
					{
						systemUiHider.show();
					}
				}
			});
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
	/* ============================================================================= */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) 
	{
		super.onPostCreate(savedInstanceState);
	
		delayedHide (AUTO_HIDE_DELAY_MILLIS);
	}
	/* ============================================================================= */
	Handler hideHandler = new Handler();
	Runnable hideRunnable = new Runnable() 
	{
		// -------------------------------------------------------------------------
		@Override
		public void run()
		{
			systemUiHider.hide();

			// --------------------------------------------------------------------
			// 21/03/2014 ECU display the text with a bit of animation
			// ---------------------------------------------------------------------
			Utilities.AnimateATextView (contentView,getString (R.string.exercise_content),FADE_TIME,true);		
		}
		// -------------------------------------------------------------------------
	};
	/* ============================================================================= */
	private void delayedHide(int delayMillis) 
	{
		hideHandler.removeCallbacks(hideRunnable);
		hideHandler.postDelayed(hideRunnable, delayMillis);
	}
	/* ============================================================================= */
	View.OnClickListener buttonListener = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{
			Intent intent;
			
			switch (view.getId())
			{
				// --------------------------------------------------------
				case R.id.button_dexterity_game:
	      			intent = new Intent (getBaseContext(),GameTwo.class);
	  				startActivityForResult (intent,0);
	  				break;
	  			// --------------------------------------------------------
				case R.id.button_visual_game:
	      			intent = new Intent (getBaseContext(),GameOne.class);
	  				startActivityForResult (intent,0);
	  				break;
	 			// --------------------------------------------------------
			}
		}
	};
	/* ============================================================================= */
}

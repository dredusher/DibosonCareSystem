package com.usher.diboson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;


public class SlideShowActivity extends DibosonActivity implements OnGestureListener
{
	/* ============================================================================= */
	// =============================================================================
	// 25/06/2013 ECU created to provide a slide show
	// 09/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 13/11/2016 ECU added the microphone icon and the handling of it
	// 14/12/2018 ECU removed the use of a 'static' context
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	//==============================================================================
	/* ============================================================================= */
	//final static String TAG = "SlideShowActivity";
	/* ============================================================================= */
					Context			context;				// 13/11/2016 ECU added
															// 14/12/2018 ECU changed from static
			static 	File			directory;
			static 	File[] 			files;
			static 	int 			filePointer		= StaticData.NO_RESULT;			
															// 26/06/2013 ECU changed from 0 
					GestureDetector gestureScanner;
	  				ImageView 		imageView;
	  				File			photoOnDisplay = null;	// 22/02/2014 ECU added
	  														// 03/03/2014 ECU initialise to null
	  				boolean			paused = false;			// 21/03/2014 ECU added
	  				ImageButton		recordButton;			// 13/11/2016 ECU added
	  				RefreshHandler 	refreshHandler	= new RefreshHandler();
	  				int				slideShowDelay;			// 13/11/2016 ECU placed here from PublicData
	  				String			soundFile;				// 13/11/2016 ECU added
	  				boolean 		stopShow 		= false;
    /* ============================================================================= */
    @Override
    protected void onCreate (Bundle savedInstanceState) 
	{
       	super.onCreate (savedInstanceState);
    	// -------------------------------------------------------------------------
    	if (savedInstanceState == null)
    	{
    		// ---------------------------------------------------------------------
    		// 09/10/2015 ECU the activity has been created anew
    		// ---------------------------------------------------------------------
    		// 16/02/2014 ECU set up common activity features
    		// 13/11/2016 ECU add the true to set full screen working
    		// ---------------------------------------------------------------------
    		Utilities.SetUpActivity (this,true);
    		// ---------------------------------------------------------------------
    		setContentView (R.layout.activity_slide_show);
    		// ---------------------------------------------------------------------
    		// 13/11/2016 ECU remember the context for later use
    		// ---------------------------------------------------------------------
    		context = this;
    		// ---------------------------------------------------------------------
    		// 15/06/2013 ECU want to pick up various gestures to control the show
    		// ---------------------------------------------------------------------
    		gestureScanner = new GestureDetector (SlideShowActivity.this,this);
    		// ---------------------------------------------------------------------
    		// 15/06/2013 ECU get the imageView where each image will be displayed
    		// ---------------------------------------------------------------------
    		imageView = (ImageView) this.findViewById (R.id.slideshowImageView);
    		// ---------------------------------------------------------------------
    		// 13/11/2016 ECU set up the record button and the associated listener
    		// ---------------------------------------------------------------------
    		recordButton = (ImageButton) findViewById (R.id.record_button);
    		recordButton.setOnClickListener (buttonListener);
    		// ---------------------------------------------------------------------
    		// 25/06/2013 ECU get File for the directory with photos in
    		// 04/03/2016 ECU check that the folder name exists
    		// ---------------------------------------------------------------------
    		if (PublicData.photosFolder != null)
    		{
    			directory = new File (PublicData.photosFolder);
    			// -----------------------------------------------------------------
    			// 21/03/2014 ECU only continue if the directory exists
    			// -----------------------------------------------------------------
    			if (directory.exists())
    			{
    				// -------------------------------------------------------------
    				// 25/06/2013 ECU get a list of the photos in the directory
    				// -------------------------------------------------------------  
    				files = directory.listFiles();
    				// -------------------------------------------------------------
    				// 21/03/2014 ECU check if there are any files in the directory
    				// -------------------------------------------------------------
    				if (files.length > 0)
    				{
    					// ---------------------------------------------------------
    					// 22/02/2014 ECU tell the user about adding a comment
    					// 14/01/2015 ECU changed to use the resource
    					// ---------------------------------------------------------
    					Utilities.SpeakAPhrase (this,getString (R.string.photo_add_comment));
    					// ---------------------------------------------------------
    					// 13/11/2016 ECU set the delay between slides to the default
    					// ---------------------------------------------------------
    					slideShowDelay = StaticData.SLIDE_SHOW_DELAY;
    					// ---------------------------------------------------------
    					// 26/06/2013 ECU initialisation here rather than in declaration
    					// ---------------------------------------------------------
    					filePointer = 0;
    					// ---------------------------------------------------------
    					// 22/02/2014 ECU have an initial delay
    					// 01/09/2015 ECU changed to use StaticData
    					// 14/11/2016 ECU changed to send empty message
    					// ---------------------------------------------------------
    					refreshHandler.sendEmptyMessageDelayed (StaticData.MESSAGE_START,StaticData.ONE_SECOND * 5);
    					// ---------------------------------------------------------
    				}
    				else
    				{
    					// ---------------------------------------------------------
    					// 21/03/2014 ECU indicate that there are no pictures to display
    					// 14/01/2015 ECU changed to use the resource
    					// ---------------------------------------------------------	
    					Utilities.SpeakAPhrase(this,getString (R.string.photo_unfortunately));
    					// ---------------------------------------------------------
    				}
    			}
    			else
    			{
    				// -------------------------------------------------------------
    				// 21/03/2014 ECU tell the user that there is no directory
    				// 14/01/2015 ECU changed to use the resource
    				// -------------------------------------------------------------
    				Utilities.SpeakAPhrase (this,getString (R.string.photo_unfortunately_no_folder));
	    			// -------------------------------------------------------------
    				Utilities.popToast (PublicData.photosFolder + " does not exist",true);
    				// -------------------------------------------------------------
    			}
    		}
    		else
    		{
    			// -----------------------------------------------------------------
    			// 04/03/2016 ECU this show was called while the app was 'closed' so
    			//                just finish this activity
    			// -----------------------------------------------------------------
    			finish ();
    			// -----------------------------------------------------------------
    		}
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 09/10/2015 ECU the activity has been recreated after having been
    		//                destroyed by the Android OS
    		// ---------------------------------------------------------------------
    		finish (); 
    		// ---------------------------------------------------------------------	
    	}
	}
	/* ============================================================================== */
	@Override 
	protected void onPause() 
	{
		// -------------------------------------------------------------------------
		// 21/03/2014 ECU indicate that the show is paused
		//            ECU added stopShow to prevent action when 'finish'-ing
		// -------------------------------------------------------------------------
		if (!paused && (photoOnDisplay != null) && !stopShow)
		{
			paused = true;	
			// ---------------------------------------------------------------------
			// 21/03/2014 ECU stop the media player - if it exists and is playing
			//                something
			// ---------------------------------------------------------------------
			Utilities.StopMediaPlayer (PublicData.mediaPlayer);
			// ---------------------------------------------------------------------
			// 21/03/2014 ECU tell the user what is happening
			// ---------------------------------------------------------------------
			Utilities.SpeakAPhrase (this,"the slide show has been paused");
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	   	super.onPause(); 
	} 
	/* ============================================================================= */
	@Override 
	protected void onResume() 
	{ 
		// -------------------------------------------------------------------------
		// 21/03/2014 ECU check if the show has previously been paused
		// -------------------------------------------------------------------------
		if (paused && (photoOnDisplay != null))
		{
			paused = false;
			
			// ---------------------------------------------------------------------
			// 21/03/2014 ECU prompt the display to continue
			// 14/11/2016 ECU checged from a 'sleep' way
			// ---------------------------------------------------------------------
			refreshHandler.sendEmptyMessageDelayed(StaticData.MESSAGE_DISPLAY,200);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	   	super.onResume(); 
	} 
	// =============================================================================
	private View.OnClickListener buttonListener = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{	
			// ---------------------------------------------------------------------
			// 13/11/2016 ECU now process depending on which button pressed
			// ---------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.record_button: 
				{
					// -------------------------------------------------------------
					// 13/11/2016 ECU created to handle the record button on a
					//                photo
					// -------------------------------------------------------------
					// 16/09/2013 ECU if the music is playing then stop the music
					// 14/11/2016 ECU use the method for stopping any music
					// ------------------------------------------------------------- 
					Utilities.StopMediaPlayer (PublicData.mediaPlayer);
					// -------------------------------------------------------------
					// 14/11/2016 ECU Note - now decide what to do depending on
					//                       whether the show has been stopped
					// -------------------------------------------------------------
					if (stopShow)
					{
						// ---------------------------------------------------------
						// 14/11/2016 ECU Note - the show is currently stopped
						//                       so resume it
						// ---------------------------------------------------------
						// 13/11/2016 ECU make sure the the symbol is restored
						// ---------------------------------------------------------
						recordButton.setImageResource (R.drawable.microphone_icon);
						// ---------------------------------------------------------
						// 16/09/2013 ECU changed to use custom toast
						// 13/11/2016 ECU changed to use resource
						// ---------------------------------------------------------
						Utilities.popToast (getString (R.string.slide_show_resumed));
						stopShow = false;
						// ---------------------------------------------------------
						// 14/11/2016 ECU resume the show by redisplaying the photo
						// ---------------------------------------------------------
						DisplayPhoto ();
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 14/11/2016 ECU Note - the show is running so stop it
						// ---------------------------------------------------------
						// 03/03/2014 ECU put in the check that the slide show is 
						//                running
						// ---------------------------------------------------------	
						if (photoOnDisplay != null)
						{
							stopShow = true;
							// -----------------------------------------------------
							// 16/09/2013 ECU changed to use custom toast
							// 13/11/2016 ECU changed to use the resource
							// -----------------------------------------------------
							Utilities.popToast (getString (R.string.slide_show_paused));
							// -----------------------------------------------------
							// 22/02/2014 ECU use 'photoOnDisplay'
							// 13/11/2016 ECU changed to use 'soundFile'
							// 14/11/2016 ECU add the final 'true' to indicate
							//                that the notes are to be added
							//                any that may already exist
							// -----------------------------------------------------
							Utilities.recordAFile (SlideShowActivity.this,
									               soundFile,
									               new MethodDefinition<SlideShowActivity> (SlideShowActivity.class,"CommentAddedMethod"),
									               true);
							// -----------------------------------------------------
							// 13/11/2016 ECU change the symbol
							// -----------------------------------------------------
							recordButton.setImageResource (R.drawable.microphone_resume_icon);
							// -----------------------------------------------------
						}
					}
					// -------------------------------------------------------------
					break;
				}
				// -----------------------------------------------------------------
			}
		}
	};
	/* ============================================================================= */
    @SuppressLint("HandlerLeak")
	class RefreshHandler extends Handler
    {
        @Override
        public void handleMessage(Message theMessage) 
        {   
        	switch (theMessage.what)
        	{
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_DISPLAY:
        			// -------------------------------------------------------------
                	// 13/11/2016 ECU Note - wait until any music has finished playing 
                	// -------------------------------------------------------------
                	if (PublicData.mediaPlayer != null && PublicData.mediaPlayer.isPlaying())
        			{
        				sendEmptyMessageDelayed (StaticData.MESSAGE_DISPLAY,200);
        			}
                	else
                	{
                		// ---------------------------------------------------------
                		// 21/03/2014 ECU check if the show has been paused
                		// ---------------------------------------------------------
                		if (!paused)
                		{
                			// -----------------------------------------------------
                			// 16/11/2016 ECU if the first slide than make sure the
                			//                record button is displayed
                			// -----------------------------------------------------
                			if (filePointer == 0)
                			{
                    			// -------------------------------------------------------------
                    			// 14/11/2016 ECU ensure that the round record button is made
                    			//                visible
                    			// -------------------------------------------------------------
                    			recordButton.setVisibility (View.VISIBLE);
                    			// -------------------------------------------------------------
                			}
                			// -----------------------------------------------------
                			SlideShowActivity.this.DisplayPhoto ();
                			filePointer++;
                		}
                		// ---------------------------------------------------------
                	}
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_START:
        			// -------------------------------------------------------------
        			// 14/11/2016 ECU call at the very start to set things up before
        			//                kicking off the slide show
        			// -------------------------------------------------------------
        			// 14/11/2016 ECU now start the slide show
        			// -------------------------------------------------------------
        			sendEmptyMessageDelayed (StaticData.MESSAGE_DISPLAY,200);
        			// -------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        	}
        	
        }
        // -------------------------------------------------------------------------
    };
    // =============================================================================
    public void DisplayPhoto()
    {
    	if (filePointer < files.length)
        {
    		if (!stopShow)
    		{
    			// -----------------------------------------------------------------
    			// 25/06/2013 ECU added the slideShowView activity - took out but if 
    			//                you want the image in the grid to be updated then 
    			//                just add ,MainActivity.slideShowView); - you might 
    			//                also need to stop the show from running when the 
    			//                BACK key is pressed
    			// 22/02/2014 ECU remember the name of the photo on display
    			// -----------------------------------------------------------------
    			photoOnDisplay = files [filePointer];
    			// -----------------------------------------------------------------
    			// 13/11/2016 ECU get the name of the sound file and remember it
    			// 16/11/2016 ECU change name from ..Music... to ...Notes...
    			// -----------------------------------------------------------------
    			soundFile = getSoundFile (PublicData.photosNotesFolder + photoOnDisplay.getName());
    			// -----------------------------------------------------------------
    			// 13/11/2016 ECU decide whether or not to display the microphone
    			//                icon
    			// 14/11/2016 ECU change to always show an icon but change which
    			//                one depending on if a file already exists
    			// -----------------------------------------------------------------
    			recordButton.setImageResource ((new File (soundFile)).exists() ? R.drawable.microphone_append_icon 
    																		   : R.drawable.microphone_icon);
    			// -----------------------------------------------------------------
    			// 13/11/2016 ECU Note - now request the display of the image and
    			//                       play the contents of the associated file
    			// -----------------------------------------------------------------
    			Utilities.displayAnImage (this,imageView, files[filePointer].getAbsolutePath(),
    										soundFile);
    			// -----------------------------------------------------------------
    			// 14/11/2016 ECU changed from using a 'sleep' method
    			// -----------------------------------------------------------------
    			refreshHandler.sendEmptyMessageDelayed (StaticData.MESSAGE_DISPLAY,slideShowDelay);
    			// -----------------------------------------------------------------
    		}
	    }
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 21/03/2014 ECU indicate that the show is stopping so that 'onPause'
    		//                works properly
    		// ---------------------------------------------------------------------
    		stopShow = true;
    		// ---------------------------------------------------------------------
    		finish ();
    	}
    }
	/* ============================================================================= */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return true;
	}
	/* ============================================================================== */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		// -------------------------------------------------------------------------
		// 21/03/2014 ECU try and intercept the 'back' key
		// -------------------------------------------------------------------------
	    if( keyCode == KeyEvent.KEYCODE_BACK) 
	    {
	    	// ---------------------------------------------------------------------
	    	// 15/06/2013 ECU sort out the media player which may be in use
	    	// 11/10/2013 ECU add the null test
	    	// ---------------------------------------------------------------------
	    	if (PublicData.mediaPlayer != null)
	    	{
	    		if (PublicData.mediaPlayer.isPlaying())
	    			PublicData.mediaPlayer.stop();
	    		// -----------------------------------------------------------------
	    		// 11/10/2013 ECU reset the object
	    		// -----------------------------------------------------------------
	    		PublicData.mediaPlayer = null;
	    	}
	    	// ---------------------------------------------------------------------
	    	// 15/06/2013 ECU indicate that the show has finished and then exit this
	    	//                activity
	    	// ---------------------------------------------------------------------
	    	stopShow = true;
	        finish ();
	        // ---------------------------------------------------------------------
	        return true;
	    }else
	    {
	        return super.onKeyDown(keyCode, event);
	    }
	}
	/* ============================================================================= */
	@Override
	public boolean onTouchEvent(MotionEvent me) 
	{
		// -------------------------------------------------------------------------
	    return gestureScanner.onTouchEvent (me);
	    // -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public void onDestroy() 
	{		
		super.onDestroy();
	}
	/* ============================================================================= */
	@Override
	public boolean onDown(MotionEvent arg0) 
	{
		return false;
	}
	/* ============================================================================= */
	@Override
	public boolean onFling(MotionEvent arg0, 
							MotionEvent arg1, 
							float arg2,
							float arg3) 
	{
		float changeY = arg0.getY() - arg1.getY();
		// -------------------------------------------------------------------------
		// 13/11/2016 ECU Note - check if the speed of the slide show is to change
		// -------------------------------------------------------------------------
		if (changeY > StaticData.SLIDE_SHOW_FLING)
		{
			if (slideShowDelay > StaticData.SLIDE_SHOW_LOWER_LIMIT)
			{
				Utilities.popToastAndSpeak (getString (R.string.slide_show_faster));
				slideShowDelay -= StaticData.SLIDE_SHOW_INCREMENT;
			}
		}
		else
		if (changeY < -StaticData.SLIDE_SHOW_FLING)
		{
			Utilities.popToastAndSpeak (getString (R.string.slide_show_slower));
			slideShowDelay += StaticData.SLIDE_SHOW_INCREMENT;
		}
		// -------------------------------------------------------------------------
		return false;
	}
	/* ============================================================================= */
	@Override
	public void onLongPress (MotionEvent arg0) 
	{
		
	}
	/* ============================================================================= */
	@Override
	public boolean onScroll (MotionEvent arg0, MotionEvent arg1, float arg2,
							 float arg3) 
	{
		return false;
	}
	/* ============================================================================= */
	@Override
	public void onShowPress (MotionEvent arg0) 
	{
	}
	/* ============================================================================= */
	@Override
	public boolean onSingleTapUp (MotionEvent theMotionEvent) 
	{	
		// -------------------------------------------------------------------------
		// 14/11/2016 ECU use the method for stopping any music
		// -------------------------------------------------------------------------
		Utilities.StopMediaPlayer (PublicData.mediaPlayer);
		// -------------------------------------------------------------------------
		// 14/11/2016 ECU go immediately to the next slide
		// -------------------------------------------------------------------------
		refreshHandler.removeMessages (StaticData.MESSAGE_DISPLAY);
		refreshHandler.sendEmptyMessage (StaticData.MESSAGE_DISPLAY);
		// -------------------------------------------------------------------------
		return false;
	}
	// =============================================================================
	public static void CommentAddedMethod ()
	{
		// -------------------------------------------------------------------------
		// 13/11/2016 ECU created to store the end time
		// 14/12/2018 ECU changed the way getString is accessed - it used to be via
		//                a 'static' context
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak (MainActivity.activity.getString (R.string.slide_show_continue));
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private String getSoundFile (String thePhotoFile)
	{
		// -------------------------------------------------------------------------
		// 23/07/2013 ECU want to change the photo extension with audio extension - seems
		//                to not be a problem with honeycomb but was for ice cream sandwich
		//				  Not happy with this code but seems to work OK.
		// -------------------------------------------------------------------------
		return thePhotoFile.replace ("jpg","wav").replace ("JPG","wav");
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
}

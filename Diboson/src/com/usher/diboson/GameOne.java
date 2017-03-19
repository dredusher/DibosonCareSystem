package com.usher.diboson;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.Window;
import android.view.GestureDetector.OnGestureListener;
import android.widget.TextView;

public class GameOne extends DibosonActivity implements OnGestureListener
{
	/* =============================================================================
	   Revision History
	   ================
	   23/05/2013 ECU originated
	   24/05/2013 ECU add interCardDelay and initial delay
	   11/02/2014 ECU changed to use text-to-speech rather than playing files
	   18/02/2014 ECU have a good tidy up
	   22/10/2015 ECU changed to 'extends DibosonActivity'
	   02/11/2015 ECU put in the check as to whether the activity has been created
	                  anew or is being recreated after having been destroyed by
	                  the Android OS
	   ============================================================================= 
	   Testing
	   =======
	   ============================================================================= */
	/* ============================================================================= */
	//final static String TAG = "GameOne";
	/* ============================================================================= */
	private static final String [] LETTERS = {"A","B","C","D","E","F","G","H","I","J","K","L","M",
            								  "N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
	private static int			   NUMBER_OF_LETTERS  	= 20;		// letters to pick from
	private static int			   ONE_SECOND 			= 1000;		// milliseconds
	private static int			   SPEED_FASTEST		= 600;		// milliseconds
	private static int             SPEED_INCREMENT		= 200;		// milliseconds
	/* ============================================================================= */
					boolean	 finishFlag			= false;		// 20/02/2014 ECU added
	private 		GestureDetector gestureScanner;
	              	int      letterCounter	 	= 0;
	              	int      letterDelay		= ONE_SECOND * 2;
	              												// 21/02/2014 ECU added - delay between each letter
	              												// 22/02/2014 ECU changed to 2 seconds
	              	boolean  letterPicked		= false;		// indicates if user has picked a card
	              	TextView letterTextView;
	private       	int      numberDealt;
	private       	int      requiredLetter;
					boolean	 terminateHandler	= false;		// 21/02/2014 ECU added - when need to 
																//                terminate activity
					int		 waitingCounter;
					boolean	 waitingToGo        = false;
	/* ============================================================================= */
	private int [] selectedLetters	= new int [NUMBER_OF_LETTERS];
	RefreshHandler refreshHandler	= new RefreshHandler();
	/* ============================================================================= */
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
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this);	
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		
			gestureScanner = new GestureDetector(this,this);
			// ---------------------------------------------------------------------
			// 23/05/2013 ECU initialise and start the game
			// 21/02/2014 ECU include the argument to delay the start for 5 seconds
			// ---------------------------------------------------------------------
			InitialiseGame (5);
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
	/* ====================================================================== */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return true;
	}
	/* ================================================================================ */
	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) 
	{
		return gestureScanner.onTouchEvent(motionEvent);
	}
	/* ============================================================================= */
	@Override
	public boolean onDown(MotionEvent motionEvent) 
	{
		// -------------------------------------------------------------------------
		// 19/02/2014 ECU put the user action into a method and call here rather
		//                than onSingleTapUp
		// -------------------------------------------------------------------------
		if (!letterPicked)
		{
			UserAction ();
			
			return true;
		}
		
		if (waitingToGo)
		{
			// ---------------------------------------------------------------------
			// 21/02/2014 ECU include the argument to delay the start for 7 seconds
			// ---------------------------------------------------------------------
			InitialiseGame (7);
			// ---------------------------------------------------------------------
			// 21/02/2014 ECU speed the game up a bit
			// 22/02/2014 ECU changed to use SPEED_ parameters
			// ---------------------------------------------------------------------
			if (letterDelay > SPEED_FASTEST)
				letterDelay -= SPEED_INCREMENT;
			// ---------------------------------------------------------------------
			// 21/02/2014 ECU tell the user that the game will be a bit faster
			// ---------------------------------------------------------------------
			Utilities.SpeakAPhrase (getBaseContext(),"The time between letters will be a bit less");		
		}
		
		return false;
	}
	/* ================================================================================ */
	@Override
	public boolean onFling(MotionEvent motionEvent1, MotionEvent motionEvent2, 
							float arg2,float arg3) 
	{
		return false;
	}
	/* ================================================================================ */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	    if( keyCode == KeyEvent.KEYCODE_BACK) 
	    {
	    	// ---------------------------------------------------------------------
	    	// 21/02/2014 ECU stop the handler from doing anything
	    	// ---------------------------------------------------------------------
	    	terminateHandler = true;
	    	// ---------------------------------------------------------------------
	    	finish ();
	        return true;
	    }else
	    {
	        return super.onKeyDown(keyCode, event);
	    }
	}
	/* ============================================================================= */
	@Override
	public void onLongPress(MotionEvent motionEvent) 
	{
		
	}
	/* ============================================================================= */
	@Override
	public boolean onScroll(MotionEvent motionEvent1, MotionEvent motionEvent2, float arg2,
			float arg3) 
	{
		return false;
	}
	/* ============================================================================= */
	@Override
	public void onShowPress(MotionEvent motionEvent) 
	{
		
	}
	/* ============================================================================= */
	@Override
	public boolean onSingleTapUp(MotionEvent motionEvent) 
	{
		// -------------------------------------------------------------------------
		// 19/02/2014 ECU moved the user action from here to 'onDown'
		// -------------------------------------------------------------------------
		return false;
	}
	/* ============================================================================= */
	void AskIfWantAnotherGo()
	{
		// -------------------------------------------------------------------------
		// 19/02/2014 ECU ask is the user wants to start another go
		// -------------------------------------------------------------------------
		Utilities.SpeakAPhrase  (this,"touch the screen if you want to have another go");

		this.refreshHandler.sleep(ONE_SECOND * 10);
			
		waitingToGo = true;	
		// -------------------------------------------------------------------------
		// 20/02/2014 ECU set up the countdown counter
		// -------------------------------------------------------------------------
		waitingCounter = 10;
		
	}
	/* ============================================================================= */
	private void InitialiseGame (int theSecondsBeforeStarting)
	{
		// -------------------------------------------------------------------------
		// 23/05/2013 ECU set up the display
		// -------------------------------------------------------------------------
		setContentView(R.layout.activity_game_one);
		letterTextView  = (TextView)findViewById (R.id.letterview);
		// -------------------------------------------------------------------------
		// 26/03/2015 ECU change the size of the TextView
		// -------------------------------------------------------------------------
		letterTextView.setTextSize (TypedValue.COMPLEX_UNIT_PX,(PublicData.screenWidth / 2));
		// -------------------------------------------------------------------------
		letterTextView.setText (" ");
		// -------------------------------------------------------------------------
		// 23/05/2013 ECU randomly select a letter to be looked for
		// 01/10/2015 ECU changed to use the method
		// -------------------------------------------------------------------------
		requiredLetter = Utilities.getRandomNumber (LETTERS.length - 1);
		
		SelectAllLetters ();
		// -------------------------------------------------------------------------
		// 23/05/2013 ECU want to insert the required letter
		// 01/10/2015 ECU changed to use the method
		// -------------------------------------------------------------------------
		selectedLetters [Utilities.getRandomNumber (numberDealt - 1)] = requiredLetter;
		// -------------------------------------------------------------------------
		// 23/05/2013 now scan the letters
		// -------------------------------------------------------------------------
		// 24/05/2013 ECU tell the user which letter we are looking for 
		// -------------------------------------------------------------------------  
		letterCounter = 0;
		// -------------------------------------------------------------------------
		// 19/02/2014 ECU indicate that awaiting user to pick a letter
		// -------------------------------------------------------------------------
		letterPicked = false;
		// -------------------------------------------------------------------------
		// 19/02/2014 ECU indicate that not waiting to start a new go
		// -------------------------------------------------------------------------
		waitingToGo = false;
		// -------------------------------------------------------------------------
		// 24/05/2013 ECU Want an initial delay and to clear the TextView
		// -------------------------------------------------------------------------
		letterTextView.setText(" ");
    	this.refreshHandler.sleep(ONE_SECOND * theSecondsBeforeStarting);
    	// -------------------------------------------------------------------------
        // 11/02/2014 ECU changed to use TTS
    	// 20/02/2014 ECU changed to use the new multiple phrase method
    	// -------------------------------------------------------------------------
		Utilities.SpeakAPhrase  (this,new String [] {"Please touch the screen when you see the letter",LETTERS[requiredLetter]});
	}
	/* ============================================================================= */
	private void NextLetter ()
	{
		// -------------------------------------------------------------------------
		// 23/05/2013 ECU display the nextselected letter
		// -------------------------------------------------------------------------
		letterTextView.setText(LETTERS[selectedLetters[letterCounter]]);
		// -------------------------------------------------------------------------
		// 23/05/2013 ECU scan through all of the letters that were deallt
		// -------------------------------------------------------------------------
		if (letterCounter < (numberDealt - 1))
    	{
			// ---------------------------------------------------------------------	
			// 21/02/2014 ECU change to use letterDelay
			// ---------------------------------------------------------------------
			this.refreshHandler.sleep(letterDelay);
			// ---------------------------------------------------------------------
			// 23/05/2013 ECU step to the next letter 
			// ---------------------------------------------------------------------
			letterCounter++;
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 27/05/2013 ECU disable the single tap event 
    		// ---------------------------------------------------------------------
    		setContentView(R.layout.game_one_notice);
    		letterTextView  = (TextView)findViewById (R.id.requiredLetterview);
    		letterTextView.setText(LETTERS[requiredLetter]);
    		// ---------------------------------------------------------------------	
    		// 11/02/2014 ECU changed to use TTS
    		// ---------------------------------------------------------------------		
    		Utilities.SpeakAPhrase (this,"Unfortunately you did not select a letter");
    		// ---------------------------------------------------------------------
    		// 19/02/2014 ECU indicate that letter has been picked to stop events
    		// ---------------------------------------------------------------------
    		letterPicked = true;
    		// ---------------------------------------------------------------------
    		// 19/02/2014 ECU check if want another go
    		// ---------------------------------------------------------------------
    		AskIfWantAnotherGo ();
    	}		
	}
	/* ============================================================================= */
	@SuppressLint("HandlerLeak")
	class RefreshHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg) 
		{   
			// ---------------------------------------------------------------------
			// 24/05/2013 ECU rearranged the order to give an initial delay 
			// 11/02/2014 ECU take out some code to do with media player -
			//                now use text-to-speech
			// ---------------------------------------------------------------------
			// 23/05/2013 ECU move to the next selected letter
			// 19/02/2014 ECU keep looping until user ends the round
			// 21/02/2014 ECU include terminate flag so that do
			//                not get odd messages because of delay in finish
			//                taking effect
			// ---------------------------------------------------------------------
			if (!terminateHandler)
			{
				
				if (!letterPicked)
				{
					NextLetter();
				}
				else
				if (waitingToGo)
				{
					if (waitingCounter > 0)
					{
						// 20/02/2014 ECU indicate the time to go
					
						letterTextView.setText (Integer.toString(waitingCounter));
					
						// 20/02/2014 ECU decrement the counter
						
						waitingCounter--;
					
						// 20/02/2014 ECU counting down to the end
					
						sleep (ONE_SECOND);
					}
					else
					{	
						if (!finishFlag)
						{
							// 20/02/2014 ECU clear the TextView
						
							letterTextView.setText (" ");
						
							Utilities.SpeakAPhrase (getBaseContext(),new String [] {"So you do not want to have another go","but I hope you found it useful"}); 
				
							// 20/02/2014 ECU indicate that 'finish' is to be actioned next time around
					
							finishFlag = true;
					
							// 20/02/2014 ECU have a delay before the actual finish happens
				
							sleep (ONE_SECOND * 5);
						}
						else
						{
							// 20/02/2014 ECU now finish the activity
					
							finish ();
						}
					}
				}
			}
		}
		/* ------------------------------------------------------------------------ */
		public void sleep(long delayMillis)
	    {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
	    }
	};
	/* ================================================================================ */
	private int SelectALetter ()
	{
		// 18/02/2014 ECU randomly deal a single card - ensuring that that card has not
		//                already been dealt
		
		int currentCard    = 0;
		boolean gotCard    = false;
		
		// 23/05/2013 ECU loop until get a card that can be used
		
		while (!gotCard)
		{
			// ---------------------------------------------------------------------
			// 23/05/2013 ECU randomly pick a card
			// 01/10/2015 ECU changed to use the method
			// ---------------------------------------------------------------------
			currentCard = Utilities.getRandomNumber (LETTERS.length - 1);
			
			// 23/05/2013 ECU check if that card has already been dealt
			
			// 23/05/2013 ECU default to indicate that card can be used
			
			gotCard = true;
			
			// 23/05/2013 ECU if already have some cards in the array then check if
			//                this new card has already been dealt
			
			if (numberDealt != 0)
			{
				for (int theIndex = 0; theIndex < numberDealt; theIndex++)
				{
					// 23/05/2013 ECU check if the card has already been dealt or is
					//                the card that is being looked for
					
					if (currentCard == selectedLetters [theIndex] || currentCard == requiredLetter)
					{
						// 23/05/2013 ECU this card should not be used so do another deal
						
						gotCard = false;
						break;
					}
				}
			}
		}
		
		// 23/05/2013 have a good card so can be used
		
		return currentCard;	
	}
	/* ================================================================================ */
	private void SelectAllLetters ()
	{
		// 18/02/2014 ECU randomly deal 'numberOfCards' cards - duplicates should not
		//                appear
		
		numberDealt = 0;
		
		for (int theIndex = 0; theIndex < NUMBER_OF_LETTERS; theIndex++)
		{
			selectedLetters [numberDealt++] = SelectALetter ();
		}
	}
	/* ================================================================================ */
	void UserAction ()
	{
		// 19/02/2014 ECU indicate that user has selected a card
		
		letterPicked = true;
		
		String displayedLetter = letterTextView.getText().toString();
		
		if (displayedLetter.equalsIgnoreCase(LETTERS[requiredLetter]))
		{
			// 17/02/2014 ECU pass through the image id as a parameter
			// 18/02/2014 ECU changed to use the new DisplayADrawable method
				
			Utilities.DisplayADrawable(getBaseContext(),R.drawable.right);
				
			// 11/02/2014 ECU changed to use TTS
				
			Utilities.SpeakAPhrase  (this,"well done, you selected the correct letter");
		}
		else
		{
			// 17/02/2014 ECU pass through the image id as a parameter
			// 18/02/2014 ECU changed to use the new DisplayADrawable method
				
			Utilities.DisplayADrawable(getBaseContext(),R.drawable.wrong);
				
			// 11/02/2014 ECU changed to use TTS
			// 20/02/2014 ECU change to use the new method that processes multiple phrases
				
			Utilities.SpeakAPhrase  (this,new String [] {"unfortunately you selected ",displayedLetter," instead of ",LETTERS[requiredLetter]});
   		}

		AskIfWantAnotherGo ();
	}
	/* ================================================================================ */
}

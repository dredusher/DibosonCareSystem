package com.usher.diboson;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

// =================================================================================
public class MusicPlayer extends DibosonActivity implements OnGestureListener
{
	/* ============================================================================= */
	// 31/05/2013 ECU Created
	// 20/01/2015 ECU lots of changes to make better use of the image adapters
	//                defined in GridActivity so that if the music player is
	//                running in the background then the album art can be shown
	//                to the user
	// 16/03/2015 ECU changed the way to switch on the remote user which is now
	//                through a dialogue rather than something being set up
	//                from TCPActivity
	// 28/03/2015 ECU add the 'play','repeat' and 'shuffle' buttons
	// 29/03/2015 ECU added 'information' and 'rebuild' buttons and change the
	//                layout
	// 03/04/2015 ECU added 'volume'
	// 20/04/2015 ECU changed 'getAlbumArt' method name to 'displayAlbumArt' as this
	//                makes more sense
	// 14/02/2016 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS. Do not understand why I didn't do this when
	//                the other activities were changed in November 2015,
	// =============================================================================
	// Testing
	// =======
	/* ============================================================================= */
	//private static final String TAG = "MusicPlayer";
	/* ============================================================================= */	
	private static final int 	MENU_REMOTE					= 1;
	private static final int	MENU_CHANGE_ROOT			= 2;			// 06/04/2015 ECU added
	private static final int	MENU_MARQUEE				= 3;			// 12/04/2015 ECU added
	// -----------------------------------------------------------------------------
	public static  final int 	SEARCH_PARAMETER_ALBUM		= 0;			// 10/04/2015 ECU added
	public static  final int 	SEARCH_PARAMETER_ARTIST		= 1;			// 10/04/2015 ECU added
	public static  final int 	SEARCH_PARAMETER_COMPOSER	= 2;			// 10/04/2015 ECU added
	public static  final int 	SEARCH_PARAMETER_TITLE		= 3;			// 10/04/2015 ECU added
	public static  final int 	SEARCH_PARAMETER_NUMBER		= 4;			// 10/04/2015 ECU added
	// -----------------------------------------------------------------------------
	private static final int	MAX_SCALED_VOLUME			= 100;			// 04/04/2015 ECU added - volume is normally 0.0 to 1.0
																			//                this is scaled by this value
	private static final String	MUSIC_EXTENSION				= "mp3";		// 07/04/2015 ECU added - files that can be played
	private static final int    PLAY_TIMEOUT				=  StaticData.MILLISECONDS_PER_MINUTE;
																			// 03/05/2015 ECU added - timeout period to check
																			//                if remote device has timed out
																			// 01/09/2015 ECU changed to use StaticData
	private static final int	SEEKBAR_SCALE				= 1000;			// 22/04/2015 ECU added - for seekbar progress
	private static final int	SHORT_DELAY					= 300;			// 01/04/2015 ECU added - short delay in milliseconds
	private static final int	SMALL_SCREEN_DISPLAY		= 750;			// 06/04/2015 ECU added - pixels
	private static final int	SMALL_SCREEN_LIMIT			= 500;			// 30/03/2015 ECU added - pixels
	private static final int	SHUFFLE_LIMIT				= 5;			// 01/04/2015 ECU added - number of tracks below
																			//                which shuffle mode makes no
																			//				  sense
	// =============================================================================
	public static	MediaMetadataRetriever mediaMetadataRetriever = null; 	// 05/04/2015 ECU added
																			// 13/04/2015 ECU added initialisation to null
	// =============================================================================
	static		 	String          	albumArtFile 		= null;			// 20/01/2015 ECU changed to public
	static 			TextView			albumDetailsArtist;					// 29/03/2015 ECU added
	static 			TextView			albumDetailsComposer;				// 29/03/2015 ECU added
	static 			TextView			albumDetailsName;					// 29/03/2015 ECU added
	static 			TableLayout			albumDetailsTable;					// 29/03/2015 ECU added
	static			TextView			albumDetailsTrackName;				// 29/03/2015 ECU added
					ImageButton 		buttonArtist;						// 31/03/2015 ECU added
					ImageButton 		buttonExit;							// 28/03/2015 ECU added
	static 			ImageButton 		buttonInformation;					// 29/03/2015 ECU added
					ImageButton 		buttonNextTrack;					// 28/03/2015 ECU added
					ImageButton 		buttonPlay;							// 28/03/2015 ECU added
	static 			ImageButton 		buttonProgressBar;					// 28/03/2015 ECU added
					ImageButton 		buttonRebuild;						// 29/03/2015 ECU added
	static 			ImageButton 		buttonRepeat;						// 28/03/2015 ECU added
	static 			ImageButton 		buttonShuffle;						// 28/03/2015 ECU added
	static 			ImageButton 		buttonTracks;						// 01/04/2015 ECU added
	static 			ImageButton 		buttonVolume;						// 03/04/2015 ECU added
	static			Context				context;							// 15/03/2015 ECU added
	static 			String []			devices				= null;			// 15/03/2015 ECU created
	static			DisplayHandler 		displayHandler;
	static 			boolean	       		displayMetadata 	= true;			// 24/06/2013 ECU added false
																			// 08/05/2015 ECU changed to true
	private     	GestureDetector 	gestureScanner;
	static			MusicRefreshHandler musicRefreshHandler ;
	static 			TextView			musicTrackTextView;					// 28/03/2015 ECU added
	static 			Thread 				populateThread		= null;			// 05/04/2015 ECU added
	static 			ProgressDialog		progressBar;						// 20/01/2014 ECU added
	static 			SeekBar				progressSeekBar;					// 03/04/2015 ECU added
	               	Random          	random       		= new Random ();
	public static	RecoveryHandler		recoveryHandler;					// 21/04/2015 ECU added
	static			boolean				recoveryMode		= false;		// 21/04/2015 ECU added
	static 			String          	remoteFileName		= null;			// 04/08/2013 ECU added - name of the next track to be played
	static 			boolean         	remoteFirst    		= false;		// 03/08/2013 ECU added - indicate remote mode just switched on
	static			boolean         	remoteMode     		= false;		// 03/08/2013 ECU added - to select remote player
	static         	int                 remoteTrackPlayingNow;				// 02/02/2015 ECU added
	static	       	int                 remoteTrackPlayingNext;				// 02/02/2015 ECU added
	static			String				returnedFile;						// 07/04/2015 ECU added
	static			File				returnedFileAsFile;					// 09/08/2016 ECU added
	static			String [] 			returnedFiles;						// 07/04/2015 ECU added
	static			boolean             showProgressBar 	= false;		// 20/01/2014 ECU added
	static 			boolean				smallScreen			= false;		// 30/03/2015 ECU added
	public static	String				trackInformation	= null;			// 12/04/2015 ECU added
	static         	int             	trackPosition;                  	// used when pausing/resuming tracks
	public static   boolean        	 	trackPaused    		= false;  
	private static 	Utilities       	utilities;
	private			int					waitingCounterRemote;				// 02/05/2015 ECU added
	/* ==================================================================== */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		// 31/05/2013 ECU created
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		// 14/02/2016 ECU check whether this is a fresh creation or a restart after
		//                the OS has destroyed it
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 25/11/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 16/02/2014 ECU call up routine to set common activity features
			// 13/11/2016 ECU add flag to indicate full screen
			// 28/11/2016 ECU changed to use new method which enables the title to
			//                be kept
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true,true,false);
			// ---------------------------------------------------------------------
			// 15/03/2015 ECU remember the context for later use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 06/04/2015 ECU choose the layout dependent on the screen size
			// ---------------------------------------------------------------------
			if (PublicData.screenWidth > SMALL_SCREEN_DISPLAY)
				setContentView (R.layout.activity_music_player);
			else
				setContentView (R.layout.activity_music_player_small_screen);
			// ---------------------------------------------------------------------
			// 05/04/2015 ECU added the retriever for use by the data classes
			// ---------------------------------------------------------------------
			mediaMetadataRetriever = new MediaMetadataRetriever();
			// ---------------------------------------------------------------------
			// 31/05/2013 ECU initialise the refresh handler
			// ---------------------------------------------------------------------
			musicRefreshHandler = new MusicRefreshHandler ();
			// ---------------------------------------------------------------------
			// 02/05/2015 ECU set the status to indicate that this device cannot
			//                accept music from a remote device
			// ---------------------------------------------------------------------
			setStatus (true);
			// ---------------------------------------------------------------------
			// 05/04/2015 ECU initialise the display handler
			// ---------------------------------------------------------------------
			displayHandler = new DisplayHandler ();
			// ---------------------------------------------------------------------
			PublicData.mpImageView = (ImageView)this.findViewById (R.id.album_art_view); 
			// ---------------------------------------------------------------------
			// 28/03/2015 ECU set up the buttons
			// 29/03/2015 ECU added 'rebuild' and 'information'
			// 31/03/2015 ECU added 'artist'
			// 01/04/2015 ECU added 'tracks'
			// 03/04/2015 ECU added 'volume'
			// ---------------------------------------------------------------------
			buttonArtist		= (ImageButton) findViewById	(R.id.music_player_button_artist);
			buttonExit			= (ImageButton) findViewById	(R.id.music_player_button_exit);
			buttonInformation	= (ImageButton) findViewById	(R.id.music_player_button_information);
			buttonNextTrack		= (ImageButton) findViewById	(R.id.music_player_button_next_track);
			buttonPlay			= (ImageButton) findViewById	(R.id.music_player_button_play);	
			buttonProgressBar	= (ImageButton) findViewById	(R.id.music_player_button_progress_bar);
			buttonRebuild		= (ImageButton) findViewById	(R.id.music_player_button_rebuild);
			buttonRepeat		= (ImageButton) findViewById	(R.id.music_player_button_repeat);	
			buttonShuffle		= (ImageButton) findViewById	(R.id.music_player_button_shuffle);	
			buttonTracks		= (ImageButton) findViewById	(R.id.music_player_button_tracks);	
			buttonVolume		= (ImageButton) findViewById	(R.id.music_player_button_volume);	
			// ---------------------------------------------------------------------
			// 28/03/2015 ECU set up the listener for the buttons
			// ---------------------------------------------------------------------
			buttonArtist.setOnClickListener (buttonListener);	
			buttonExit.setOnClickListener (buttonListener);	
			buttonInformation.setOnClickListener (buttonListener);
			buttonNextTrack.setOnClickListener (buttonListener);	
			buttonPlay.setOnClickListener (buttonListener);	
			buttonProgressBar.setOnClickListener (buttonListener);	
			buttonRebuild.setOnClickListener (buttonListener);	
			buttonRepeat.setOnClickListener (buttonListener);	
			buttonShuffle.setOnClickListener (buttonListener);	
			buttonTracks.setOnClickListener (buttonListener);
			buttonVolume.setOnClickListener (buttonListener);	
			// ---------------------------------------------------------------------
			// 28/03/2015 ECU set up the text view for track details
			// ---------------------------------------------------------------------
			musicTrackTextView = (TextView) findViewById (R.id.music_track_details);
			// ---------------------------------------------------------------------
			// 29/03/2015 ECU get the table that contains the album details
			// ---------------------------------------------------------------------
			albumDetailsTable  		= (TableLayout) findViewById (R.id.table_album_details);
			// ---------------------------------------------------------------------
			// 29/03/2015 ECU add the textview for album details
			// ---------------------------------------------------------------------
			albumDetailsArtist 		= (TextView)  findViewById (R.id.album_artist_data);
			albumDetailsComposer 	= (TextView)  findViewById (R.id.album_track_composer_data);
			albumDetailsName 		= (TextView)  findViewById (R.id.album_name_data);
			albumDetailsTrackName 	= (TextView)  findViewById (R.id.album_track_name_data);
			// ---------------------------------------------------------------------
			// 20/01/2014 ECU try and set up the progress bar
			// 28/03/2015 ECU try and position the progress bar
			// ---------------------------------------------------------------------
			progressBar = new ProgressDialog (this);
			progressBar.setProgressStyle (ProgressDialog.STYLE_HORIZONTAL);	
			progressBar.setCancelable (true);
			// ---------------------------------------------------------------------  
			// Get the Drawable custom_progress_bar 
			// ---------------------------------------------------------------------
			Drawable customDrawable = getResources().getDrawable (R.drawable.custom_progress_bar);
			// ---------------------------------------------------------------------
			// set the drawable as progress drawable
			// ---------------------------------------------------------------------
			progressBar.setProgressDrawable (customDrawable);
			// ---------------------------------------------------------------------
			// 03/04/2015 ECU get the seek bar that will be used for showing track
			//                progress
			// ---------------------------------------------------------------------
			progressSeekBar = (SeekBar) findViewById (R.id.album_track_progress_data);
			// ---------------------------------------------------------------------
			progressSeekBar.setProgressDrawable (getResources().getDrawable (R.drawable.custom_seek_bar));
			// ---------------------------------------------------------------------
			// 08/05/2015 ECU set up the event listener for the seekbar
			// ---------------------------------------------------------------------
			progressSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() 
			{   
				// -----------------------------------------------------------------
				@Override       
				public void onStopTrackingTouch (SeekBar theSeekBar) 
				{     
					// -------------------------------------------------------------
					// 08/04/2015 ECU when the user 'lifts off' of the seek bar then
					//                the currently playing track will be played from
					//                that position
					// -------------------------------------------------------------
					PublicData.mediaPlayer.seekTo (theSeekBar.getProgress() * SEEKBAR_SCALE);
					// -------------------------------------------------------------
				}       
				// -----------------------------------------------------------------
				@Override       
				public void onStartTrackingTouch (SeekBar theSeekBar) 
				{   
					
				}       
				// -----------------------------------------------------------------
				@Override       
				public void onProgressChanged (SeekBar seekBar, int progress,boolean fromUser) 
				{     
				}
				// -----------------------------------------------------------------
			});           
			// ---------------------------------------------------------------------
			// 03/04/2015 ECU try and hide the thumb (only available from JELLY BEAN
			// 08/05/2015 ECU commented out the following statements because want to
			//                position within the track
			// ---------------------------------------------------------------------
			// 08/05/2015 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) 
			// 08/05/2015	progressSeekBar.getThumb ().mutate ().setAlpha (0);
			// ---------------------------------------------------------------------
			// 02/06/2013 ECU set up the gesture handling
			// ---------------------------------------------------------------------
			gestureScanner = new GestureDetector (this,this);
			// ---------------------------------------------------------------------
			utilities = new Utilities ();
			// ---------------------------------------------------------------------
			// 30/03/2015 ECU check whether the screen is big enough to display all
			//                of the information
			//					  smallScreen true = small screen
			// ---------------------------------------------------------------------
			smallScreen = (PublicData.screenHeight < SMALL_SCREEN_LIMIT);
			// ---------------------------------------------------------------------
			// 29/03/2015 ECU adjust the size of the album art
			// 30/03/2015 ECU adjust for small screen
			// 08/05/2015 ECU moved here from end of onCreate
			// ---------------------------------------------------------------------
			setImageViewSize (PublicData.mpImageView,(PublicData.screenWidth * 2) / (3 + (smallScreen ? 1 : 0)));
			// ---------------------------------------------------------------------
			// 16/04/2015 ECU on entry to the application - always switch off remote 
			//                mode
			// ---------------------------------------------------------------------
			if (remoteMode)
			{
				CancelRemote (0);
			}
			// ---------------------------------------------------------------------
			// ---------------------------------------------------------------------
			// 21/04/2015 ECU initialise the data used by the music player - all held
			//                in the 'MainActivity.musicPlayerData' object
			// ---------------------------------------------------------------------
			if (initialiseData (this))
			{
				// -----------------------------------------------------------------
				// 21/04/2015 ECU the data has been initialised and there is music to
				// 				  play
				// -----------------------------------------------------------------
				// 29/03/2015 ECU changed to use the method
				// -----------------------------------------------------------------
				updateStatusIcons ();
				// -----------------------------------------------------------------
				// 21/04/2015 ECU check is already playing a list
				// -----------------------------------------------------------------
				if (!PublicData.musicPlayerData.tracksPlaying)
				{
					// -------------------------------------------------------------
					// 21/04/2015 ECU not playing a 'play list'
					// -------------------------------------------------------------
					// 21/04/2015 ECU check that the current track number is valid (
					//				  reset to 0 if not) and then start playing that 
					//                track
					// -------------------------------------------------------------
					if (PublicData.musicPlayerData.trackNumber < 0 ||
							PublicData.musicPlayerData.trackNumber >= PublicData.musicPlayerData.tracks.size ())
					{
						// ---------------------------------------------------------
						// 21/04/2015 ECU for some reason the track number is invalid
						//                so reset it to the start
						// ---------------------------------------------------------
						PublicData.musicPlayerData.trackNumber = 0;
					}
					// -------------------------------------------------------------
					// 21/04/2015 ECU indicate that a play list has started
					// -------------------------------------------------------------
					PublicData.musicPlayerData.tracksPlaying  = true;		
					// -------------------------------------------------------------
					// 21/04/2015 ECU start playing the track
					// -------------------------------------------------------------
					playMusicTrack (this,
									PublicData.musicPlayerData.trackNumber,
									PublicData.musicPlayerData.trackPosition);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 21/04/2015 ECU already playing a track so pick up from where
					//				  left off
					// -------------------------------------------------------------
					// 21/04/2015 ECU display the appropriate album art
					// -------------------------------------------------------------
					displayAlbumArt (this,PublicData.musicPlayerData.trackPlaying);
					// -------------------------------------------------------------
					// 20/04/2015 ECU if the media player has not been defined or has
					//                been defined but is not playing then play the
					//                current track at its current position
					// -------------------------------------------------------------
					if (PublicData.mediaPlayer == null || !PublicData.mediaPlayer.isPlaying())
					{	
						playMusicTrack (this,
										PublicData.musicPlayerData.trackPlaying,
										PublicData.musicPlayerData.trackPosition);
					}
					else
					{
						// ---------------------------------------------------------
						// 06/05/2015 ECU re-initialise the progress seekBar
						// ---------------------------------------------------------
						progressSeekBar.setMax (PublicData.mediaPlayer.getDuration()/SEEKBAR_SCALE);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 21/04/2015 ECU something failed with the initialisation so cannot
				//                continue with the activity
				// -----------------------------------------------------------------
				Utilities.popToast (getString (R.string.no_music_to_play),true);
				// -----------------------------------------------------------------
				// 11/04/2015 ECU reset any flags that are used
				// -----------------------------------------------------------------
				if (PublicData.musicPlayerData != null)
				{	
					// -------------------------------------------------------------
					// 21/04/2015 ECU come here because the data was successfully read
					//                from disk but apparently cannot find any music
					//                so request the user to locate where the music is
					// -------------------------------------------------------------
					PublicData.musicPlayerData.tracksPlaying = false;
					// -------------------------------------------------------------
					// 21/04/2015 ECU get user to locate where the music is
					//            ECU the 'true' flag indicates that in 'recovery mode'
					// -------------------------------------------------------------
					changeRootFolderForMusic (this,true);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 02/05/2015 ECU exit this activity
					// -------------------------------------------------------------
					finish ();
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
			// 04/04/2015 ECU set the volume to the stored value
			// -------------------------------------------------------------------------
			setVolume (PublicData.mediaPlayer,PublicData.musicPlayerData.volume,MAX_SCALED_VOLUME);
			// ---------------------------------------------------------------------
			// 31/05/2013 ECU indicate that everything is ready to go
			// ---------------------------------------------------------------------
			PublicData.stopmpPlayer = false;	           // indicate that have not stopped
			// ---------------------------------------------------------------------
			// 31/05/2013 ECU initiate a delay which will maintaining the playing
			//                of the tracks
			// 21/04/2015 ECU only start the handler if not in a 'recovery state'
			// ---------------------------------------------------------------------
			if (!recoveryMode)
				musicRefreshHandler.sleep (SHORT_DELAY);
			// ---------------------------------------------------------------------
			// 23/11/2013 ECU check if any parameters have been passed across
			// ---------------------------------------------------------------------  
			Bundle extras = getIntent().getExtras();
		
			if (extras !=null) 
			{
				// -----------------------------------------------------------------
				// 23/11/2013 ECU at the moment only looking for a 'finish' command
				// -----------------------------------------------------------------
				String inputCommand = extras.getString(Dialogue.FINISH);
		    
				if (inputCommand != null)
				{
					if (inputCommand.startsWith(Dialogue.FINISH))
					{
						// ---------------------------------------------------------
						// 23/11/2013 ECU just terminate this activity - as required
						//                be the Dialogue activity
						// ---------------------------------------------------------
						finish ();
					}
				}
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 14/02/2016 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public void onBackPressed() 
	{
		// -------------------------------------------------------------------------
		// 06/05/2015 ECU created to handle the pressing of the 'back' key
		// -------------------------------------------------------------------------
		super.onBackPressed();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public boolean onCreateOptionsMenu (Menu menu) 
	{
		// -------------------------------------------------------------------------
		// Inflate the menu; this adds items to the action bar if it is present.
		// -------------------------------------------------------------------------
		getMenuInflater().inflate(R.menu.music_player, menu);
		
		buildMenuOptions (menu);
		
		return true;
	}
	// =============================================================================
	@Override
	public void onDestroy ()
	{
		// -------------------------------------------------------------------------
		// 03/06/2013 ECU this activity is being destroyed so write the data to disk
		// -------------------------------------------------------------------------
		WriteToDisk (PublicData.projectFolder + getString (R.string.music_player_data));
		// -------------------------------------------------------------------------
		// 03/06/2013 ECU call the main inDestroy to do all other work
		// -------------------------------------------------------------------------
		super.onDestroy ();
	}
	/* ============================================================================= */
	@Override
	public boolean onDown (MotionEvent theMotionEvent) 
	{
		return false;
	}
	/* ============================================================================= */
	@Override
	public boolean onFling (MotionEvent arg0, MotionEvent arg1, float arg2,float arg3) 
	{
		// -------------------------------------------------------------------------
		// 02/06/2013 ECU cause the next track to be played
		// 01/04/2015 ECU the code to select next track taken out as it is done
		//                by a button
		// -------------------------------------------------------------------------
		return false;
	}
	// ============================================================================= 
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	    if( keyCode == KeyEvent.KEYCODE_BACK) 
	    {
	       	super.onKeyDown(keyCode, event);
	    	return true;
	    }else
	    {
	        return super.onKeyDown(keyCode, event);
	    }
	}
	// ============================================================================= 
	@Override
	public void onLongPress (MotionEvent arg0) 
	{
	
	}
	// =============================================================================
	public boolean onOptionsItemSelected (MenuItem item)
	{
		
		switch (item.getItemId())
		{
			// ---------------------------------------------------------------------
			case MENU_CHANGE_ROOT:
				// -----------------------------------------------------------------
				// 06/04/2015 ECU change the root folder for the music
				// 07/04/2015 ECU changed to use listChoice
				// 24/01/2015 ECU changed to use the method
				// -----------------------------------------------------------------
				changeRootFolderForMusic (this,false);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case MENU_MARQUEE: 
				// -----------------------------------------------------------------
				// 12/04/2015 ECU toggles the option that shows the track information
				//                in marquee format on the list view
				// 13/04/2015 ECU changed following move to 'storedData'
				// -----------------------------------------------------------------
				PublicData.storedData.marquee = !PublicData.storedData.marquee;
				// -----------------------------------------------------------------
				// 12/04/2015 ECU inform the user what is going on
				// 22/04/2015 ECU changed to use the resource
				// 05/05/2015 ECU changed to use method
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (getString (R.string.scrolling_track_information) + 
						                     Utilities.booleanAsString (PublicData.storedData.marquee));
				// -----------------------------------------------------------------
				// 12/04/2015 ECU try and action the request immediately
				// -----------------------------------------------------------------
				refreshGridCell (albumArtFile);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case MENU_REMOTE:
				// -----------------------------------------------------------------
				// 16/03/2015 ECU changed to use dialogue option
				// 07/05/2016 ECU used to toggle the mode here but now do it when
				//                the device is selected
				// -----------------------------------------------------------------
				// 16/03/2015 ECU if remote mode has been switched on then request
				//                the destination device
				// -----------------------------------------------------------------
				if (!remoteMode)
				{
					DialogueUtilities.singleChoice (this, 
													getString (R.string.title_remote_player),
													(devices = Utilities.deviceListAsArray(false)),0, 
													Utilities.createAMethod (MusicPlayer.class,"ConfirmRemote",0),
													Utilities.createAMethod (MusicPlayer.class,"CancelRemote",0));
				}
				else
				{
					// -------------------------------------------------------------
					// 07/05/2016 ECU indicate that remote mode is cancelled
					// -------------------------------------------------------------
					remoteMode = false;
					// -------------------------------------------------------------
					// 16/03/2015 ECU indicate that the remote player is no longer
					//                running and other flags associated with
					//				  remote access
					// 13/04/2015 ECU use the method, the '0' is not relevant
					// -------------------------------------------------------------
					CancelRemote (0);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				return true;
			// ---------------------------------------------------------------------
		}
		return true;
	}
	// =============================================================================
	@Override 
	protected void onPause() 
	{
		// -------------------------------------------------------------------------
		// 05/04/2015 ECU pause the activity
		// -------------------------------------------------------------------------
		super.onPause(); 
	} 
	// ============================================================================= 
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) 
	{
		menu.clear();

		buildMenuOptions (menu);

		return super.onPrepareOptionsMenu(menu);

	}
	// =============================================================================
	@Override 
	protected void onResume() 
	{ 	
		// -------------------------------------------------------------------------
		// 05/04/2015 ECU resume the activity
		// -------------------------------------------------------------------------
		super.onResume(); 
	} 
	// =============================================================================
	@Override
	public boolean onScroll (MotionEvent theMotionEvent0, MotionEvent theMotionEvent1, 
							 float arg2,float arg3) 
	{
		return false;
	}
	// ============================================================================= 
	@Override
	public void onShowPress (MotionEvent theMotionEvent) 
	{
	}
	// ============================================================================= 
	@Override
	public boolean onSingleTapUp (MotionEvent theMotionEvent) 
	{
		return false;
	}
	// ============================================================================= 
	@Override
	public boolean onTouchEvent (MotionEvent theMotionEvent) 
	{
	      return gestureScanner.onTouchEvent (theMotionEvent);
	}
	// =============================================================================
	private View.OnClickListener buttonListener = new View.OnClickListener() 
	{
		// -------------------------------------------------------------------------
		@Override
		public void onClick (View view) 
		{	
			//----------------------------------------------------------------------
			// 28/03/2015 ECU now process depending on which button pressed
			//----------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.music_player_button_artist:
					// -------------------------------------------------------------
					// 28/03/2015 ECU 'artist' button
					// -------------------------------------------------------------
					// 31/03/2015 ECU check if need to build the array
					//            ECU may be a delay so make the button invisible
					//                till ready
					// -------------------------------------------------------------
					if (PublicData.musicPlayerData.artists == null || PublicData.musicPlayerData.artists.size() == 0)
					{
						// ---------------------------------------------------------
						// 31/03/2015 ECU indicate that there will be a short delay
						// ---------------------------------------------------------
						Utilities.SpeakAPhrase (MusicPlayer.this,getString (R.string.build_delay));
						// ---------------------------------------------------------
						buildArtistList (true);
					}
					// -------------------------------------------------------------
					// 31/03/2015 ECU now select the required artists
					// 02/04/2015 ECU changed from singleChoice
					// -------------------------------------------------------------
					DialogueUtilities.listChoice (MusicPlayer.this, 
												  getString (R.string.select_artist),
												  TrackArtist.returnArtists (PublicData.musicPlayerData.artists), 
												  Utilities.createAMethod (MusicPlayer.class,"SelectedArtist",0),
												  getString (R.string.cancel_this_operation_rebuild),
												  Utilities.createAMethod (MusicPlayer.class,"CancelArtist",0));
					// ------------------------------------------------------------- 
					// 01/04/2015 ECU set the icon to 'pause' because the player
					//                will be started
					// -------------------------------------------------------------
					buttonPlay.setImageResource (R.drawable.music_pause);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.music_player_button_exit:
					// -------------------------------------------------------------
					// 28/03/2015 ECU 'EXIT' button
					// -------------------------------------------------------------
					Exit ();
					// ------------------------------------------------------------- 
					break;
				// -----------------------------------------------------------------
				case R.id.music_player_button_information:
					// -------------------------------------------------------------
					// 28/03/2015 ECU 'information (track details)' button
					// -------------------------------------------------------------
					displayMetadata = !displayMetadata;
					// -------------------------------------------------------------
					// 29/03/2015 ECU tell the user what is happening
					// 30/03/2015 ECU add the centre-ing flag
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString(displayMetadata ? R.string.track_details_on 
																		  : R.string.track_details_off),true);
					// -------------------------------------------------------------
					// 05/04/2015 ECU update the status icons
					// -------------------------------------------------------------
					updateStatusIcons ();
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.music_player_button_next_track:
					// -------------------------------------------------------------
					// 28/03/2015 ECU 'next track' button
					// -------------------------------------------------------------
					PublicData.mediaPlayer.stop ();
					// ------------------------------------------------------------- 
					break;
				// -----------------------------------------------------------------
				case R.id.music_player_button_play:
					// -------------------------------------------------------------
					// 28/03/2015 ECU 'play' button
					// -------------------------------------------------------------
					if (PublicData.mediaPlayer.isPlaying())
					{
						// ---------------------------------------------------------
						// 02/06/2013 ECU remember where I was (in milliSecs)
						// 22/05/2016 ECU changed to use new method
						// ---------------------------------------------------------
						playOrPause (false);
						// ---------------------------------------------------------
						// 28/03/2015 ECU change the image to indicate that pressing
						//                it will start playing
						// ---------------------------------------------------------
						buttonPlay.setImageResource (R.drawable.music_play);
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 01/04/2015 ECU ensure that the tracks will keep playing
						//                and position to start of the track
						// ---------------------------------------------------------
						if (!PublicData.musicPlayerData.tracksPlaying)
						{
							PublicData.musicPlayerData.tracksPlaying = true;
							PublicData.musicPlayerData.trackPosition = 0;
							// -----------------------------------------------------
							// 01/04/2015 ECU and start up the refresh handler
							// -----------------------------------------------------
							musicRefreshHandler.sleep (SHORT_DELAY);
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
						// 02/06/2013 ECU position the track to where I was when paused
						// 22/05/2016 ECU changed to use new method
						// ---------------------------------------------------------
						playOrPause (true);
						// ---------------------------------------------------------
						// 28/03/2015 ECU change the image to indicate that pressing
						//                it will pause the current track
						// ---------------------------------------------------------
						buttonPlay.setImageResource (R.drawable.music_pause);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------	 
					break;
				// -----------------------------------------------------------------
				case R.id.music_player_button_progress_bar:
					// -------------------------------------------------------------
					// 28/03/2015 ECU 'progress bar' button
					// -------------------------------------------------------------
					// 28/03/2015 ECU toggle the shuffle setting
					// -------------------------------------------------------------
					showProgressBar = !showProgressBar;
					// -------------------------------------------------------------
					// 28/03/2015 ECU change the image
					// -------------------------------------------------------------
					buttonProgressBar.setImageResource (showProgressBar ? R.drawable.music_progress_bar_on
																		: R.drawable.music_progress_bar_off);
					// -------------------------------------------------------------
					// 29/03/2015 ECU tell the user what is happening
					// 30/03/2015 ECU add the centre-ing flag
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak(getString (showProgressBar ? R.string.progress_bar_on 
							                                              : R.string.progress_bar_off),true);
					// -------------------------------------------------------------
					// 28/03/2015 ECU decide whether to show or hide the progress 
					//                bar
					// 04/04/2015 ECU changed to use 'tracks'
					// -------------------------------------------------------------
					if (showProgressBar)
					{
						ShowProgressBar (PublicData.musicPlayerData.tracks.get (PublicData.musicPlayerData.trackNumber).fileName);
					}
					else
					{
						progressBar.hide();		
					}
					// ------------------------------------------------------------- 
					break;
				// -----------------------------------------------------------------
				case R.id.music_player_button_rebuild:
					// -------------------------------------------------------------
					// 28/03/2015 ECU 'rebuild music list' button
					// -------------------------------------------------------------
					// 31/05/2013 ECU rebuild the music playlist
					// 04/04/2015 ECU changed to use the returned array
					// 			  ECU changed to use local method
					// -----------------------------------------------------------------			
					buildTrackInformation (MusicPlayer.context);
					// -------------------------------------------------------------
					// 29/03/2015 ECU tell the user what has happened
					// 30/03/2015 ECU add the centre-ing flag
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString(R.string.rebuild_play_list),true);
					// -------------------------------------------------------------
					// 31/05/2013 ECU initialise playlist flags
					// 31/03/2015 ECU set track number to -1 instead of 0 because
					//                it will be incremented before use
					// 05/04/2015 ECU switch off the display of information because
					//                the data is being updated and could cause an error
					// -------------------------------------------------------------
					displayMetadata							  = false;		
					PublicData.musicPlayerData.trackNumber    = -1;               // used to indicate which entry is playing
					PublicData.musicPlayerData.shuffleMode    = false;            // switch shuffle mode off
					PublicData.mediaPlayer.stop();
					// -------------------------------------------------------------
					// 29/03/2015 ECU make sure that the status icons are updated
					// -------------------------------------------------------------
					updateStatusIcons ();
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.music_player_button_repeat:
					// -------------------------------------------------------------
					// 28/03/2015 ECU 'repeat' button
					// -------------------------------------------------------------
					// 28/03/2015 ECU toggle the repeat setting
					// -------------------------------------------------------------
					PublicData.musicPlayerData.repeatMode = !PublicData.musicPlayerData.repeatMode;
					// -------------------------------------------------------------
					// 28/03/2015 ECU change the image
					// -------------------------------------------------------------
					buttonRepeat.setImageResource (PublicData.musicPlayerData.repeatMode ? R.drawable.music_repeat_on
																						 : R.drawable.music_repeat_off); 
					// -------------------------------------------------------------	
					// 29/03/2015 ECU tell the user what is happening
					// 30/03/2015 ECU add the centre-ing flag
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak(getString(PublicData.musicPlayerData.repeatMode ? R.string.repeat_mode_on 
							                                                                   : R.string.repeat_mode_off),true);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.music_player_button_shuffle:
					// -------------------------------------------------------------
					// 28/03/2015 ECU 'shuffle' button
					// -------------------------------------------------------------
					// 28/03/2015 ECU toggle the shuffle setting
					// 01/04/2015 ECU use the method
					// -------------------------------------------------------------
					toggleShuffleMode (true);
					// -------------------------------------------------------------
					// 28/03/2015 ECU change the image
					// -------------------------------------------------------------
					buttonShuffle.setImageResource (PublicData.musicPlayerData.shuffleMode ? R.drawable.music_shuffle_on
																						   : R.drawable.music_shuffle_off);
					// -------------------------------------------------------------	
					// 29/03/2015 ECU tell the user what is happening
					// 30/03/2015 ECU add the centre-ing flag
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString(PublicData.musicPlayerData.shuffleMode ? R.string.shuffle_mode_on 
																								 : R.string.shuffle_mode_off),true);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.music_player_button_tracks:
					// -------------------------------------------------------------
					// 01/04/2015 ECU 'tracks' button
					// -------------------------------------------------------------
					// 01/04/2015 ECU as a test just display the list of tracks
					// 02/04/2015 ECU change from singleChoice
					// 08/04/2015 ECU added the search button
					// -------------------------------------------------------------
					DialogueUtilities.listChoice (MusicPlayer.this, "Select the Required Track",
							TrackDetails.returnStringArray (PublicData.musicPlayerData.tracks), 
							Utilities.createAMethod(MusicPlayer.class,"SelectedTrack",0),
							getString (R.string.select_track_titles),
							Utilities.createAMethod(MusicPlayer.class,"SearchTracks",0),
							getString (R.string.cancel_this_operation),
							Utilities.createAMethod(MusicPlayer.class,"Cancel",0));
					break;
				// -----------------------------------------------------------------
				case R.id.music_player_button_volume:
					// -------------------------------------------------------------
					// 01/04/2015 ECU 'volume' button
					// 04/04/2015 ECU changed from 100 to MAX_SCALE_VOLUME
					// 26/07/2015 ECU use the resource to set subtitle
					// 07/05/2016 ECU change click.. to use resource
					// -------------------------------------------------------------
					DialogueUtilities.sliderChoice (MusicPlayer.this,
	  												getString (R.string.select_a_volume),
	  												getString (R.string.select_a_volume_summary),
	  												PublicData.mediaPlayer,
	  												PublicData.musicPlayerData.volume,
	  												MAX_SCALED_VOLUME,
	  												getString (R.string.click_to_set_value),
	  												Utilities.createAMethod (MusicPlayer.class,"VolumeChange",0));
					// ------------------------------------------------------------
					break;
				// -----------------------------------------------------------------	
			}
			// ---------------------------------------------------------------------
		}
	};	
	// =============================================================================
	static void buildArtistList (boolean theSortFlag)
	{
		boolean			  		artistFound		= false;
		int						artistIndex;
		int						trackIndex;
		// -------------------------------------------------------------------------
		// 04/04/2015 ECU initialise the array list
		// -------------------------------------------------------------------------
		PublicData.musicPlayerData.artists = new ArrayList<TrackArtist> ();
		// -------------------------------------------------------------------------	
		// 04/04/2015 ECU changed to use 'tracks'
		// -------------------------------------------------------------------------
		for (trackIndex = 0; trackIndex < PublicData.musicPlayerData.tracks.size(); trackIndex++)
		{
			// --------------------------------------------------------------------
			// 05/04/2015 ECU get the artists name from the record
			// --------------------------------------------------------------------
			String artistName = PublicData.musicPlayerData.tracks.get(trackIndex).artist;
			// --------------------------------------------------------------------
			// 01/04/2015 ECU if a 'null' artist name is returned then just set 
			//                UNKNOWN_ARTIST
			// 03/05/2015 ECU changed so as not to use local string definition
			//                of UNKNOWN
			// 01/09/2015 ECU changed to use StaticData
			// --------------------------------------------------------------------
			if (artistName == null)
				artistName = StaticData.UNKNOWN;
			// --------------------------------------------------------------------
			// 31/03/2015 ECU check if the artist has already been found
			// --------------------------------------------------------------------
			artistFound = false;
			for (artistIndex = 0; artistIndex < PublicData.musicPlayerData.artists.size (); artistIndex++)
			{
				if (PublicData.musicPlayerData.artists.get (artistIndex).name.equals(artistName))
				{
					artistFound = true;
					PublicData.musicPlayerData.artists.get(artistIndex).addTrack (trackIndex);
					break;
				}
			}
			// ---------------------------------------------------------------------
			// 31/03/2015 ECU check if artist found - if not then create entry
			// ---------------------------------------------------------------------
			if (!artistFound)
			{
				PublicData.musicPlayerData.artists.add (new TrackArtist (artistName,trackIndex));
			}
			// ---------------------------------------------------------------------
			// 31/03/2015 ECU add the track to the artists list
			// ---------------------------------------------------------------------
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 31/03/2015 ECU check whether the list is to be sorted
		// -------------------------------------------------------------------------
		if (theSortFlag)
			Collections.sort(PublicData.musicPlayerData.artists);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private void buildMenuOptions (Menu theMenu)
	{
		// -------------------------------------------------------------------------
		// 16/03/2015 ECU just tidy up menu options
		// 28/03/2015 ECU took the shuffle mode out as now on a button
		// 06/04/2015 ECU added the 'change root' option
		// 13/04/2015 ECU changed following move of 'marquee' to storedData
		// 05/05/2015 ECU changed to use booleanAs... method
		// -------------------------------------------------------------------------
		theMenu.add (0,MENU_REMOTE,		0,(remoteMode ? "Turn Remote Mode Off" :  "Send Music to a Remote Device"));
		theMenu.add (0,MENU_CHANGE_ROOT,0,"Change the root music folder");
		theMenu.add (0,MENU_MARQUEE,	0,(getString (R.string.scrolling_track) + Utilities.booleanAsString(!PublicData.storedData.marquee)));
		// -------------------------------------------------------------------------
		// 20/01/2014 ECU handle progress bar
		// 28/03/2015 ECU remove the progress bar which now has a button
		//            ECU take out exit as has a button
		// 29/03/2015 ECU rebuild taken out as now a button
		// -------------------------------------------------------------------------
	}
	 // =============================================================================
	static void buildTrackInformation (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 04/04/2015 ECU created to build the initial track information based
		//                on information held on disk
		// 06/04/2015 ECU use the root folder now held in 'musicPlayerData' but
		//                if never set then set it to the default
		// -------------------------------------------------------------------------
		if (PublicData.musicPlayerData.rootFolder == null)
			PublicData.musicPlayerData.rootFolder = theContext.getString (R.string.music_root_folder);
		// -------------------------------------------------------------------------
		// 07/04/2015 ECU the false is added to indicate that files are wanted
		// -------------------------------------------------------------------------
		String [] localTracks
			= utilities.BuildPlayList (theContext,PublicData.musicPlayerData.rootFolder,MUSIC_EXTENSION);
		// -------------------------------------------------------------------------
		// 04/04/2015 ECU now build the track details
		// -------------------------------------------------------------------------
		PublicData.musicPlayerData.tracks = new ArrayList <TrackDetails> ();
		// -------------------------------------------------------------------------
		// 04/04/2015 ECU now build the track information
		// -------------------------------------------------------------------------
		for (int theTrack = 0; theTrack < localTracks.length; theTrack++)
			PublicData.musicPlayerData.tracks.add (new TrackDetails (localTracks [theTrack]));
		// -------------------------------------------------------------------------
		// 04/04/2015 ECU populate the details of the tracks and artists in the background
		// -------------------------------------------------------------------------
		populateDetails (true);
		// ------------------------------------------------------------------------
	}
	// =============================================================================
	boolean changeIcons ()
	{
		// -------------------------------------------------------------------------
		// 05/04/2015 ECU if the artists database is being built then remove button
		//            ECU change to use populateThread rather than having separate
		//                threads for tracks and artists
		// -------------------------------------------------------------------------
		if (populateThread != null)
		{
			if (populateThread.isAlive())
			{
				// -----------------------------------------------------------------
				// 05/04/2015 ECU make the 'tracks' and 'artists' buttons invisible
				// -----------------------------------------------------------------
				if (buttonTracks.getVisibility() == View.VISIBLE)
					buttonTracks.setVisibility (View.INVISIBLE);
				
				if (buttonArtist.getVisibility() == View.VISIBLE)
					buttonArtist.setVisibility (View.INVISIBLE);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 05/04/2015 ECU make the 'tracks' and 'artists' buttons visible
				// ------------------------------------------------------------------
				if (buttonTracks.getVisibility() == View.INVISIBLE)
					buttonTracks.setVisibility (View.VISIBLE);
				
				if (buttonArtist.getVisibility() == View.INVISIBLE)
					buttonArtist.setVisibility (View.VISIBLE);
				// -----------------------------------------------------------------
				// 05/04/2015 ECU indicate nothing more to do
				// -----------------------------------------------------------------
				populateThread = null;
				// -----------------------------------------------------------------
			}	
		}
		// -------------------------------------------------------------------------
		// 05/04/2015 ECU check what to do with rebuild button
		// -------------------------------------------------------------------------
		if (populateThread != null)
		{
			// -----------------------------------------------------------------
			// 05/04/2015 ECU a thread is running so remove the button
			// 			  ECU added the information button
			// -----------------------------------------------------------------
			if (buttonRebuild.getVisibility() == View.VISIBLE)
					buttonRebuild.setVisibility (View.INVISIBLE);
			if (buttonInformation.getVisibility() == View.VISIBLE)
				buttonInformation.setVisibility (View.INVISIBLE);
			// -----------------------------------------------------------------
			// 05/04/2015 ECU tell the display handler of a change
			// -----------------------------------------------------------------
			return true;
		}
		else
		{
			// -----------------------------------------------------------------
			// 05/04/2015 ECU no thread is running so redisplay the button
			//            ECU add the information button
			// -----------------------------------------------------------------
			if (buttonRebuild.getVisibility() == View.INVISIBLE)
					buttonRebuild.setVisibility (View.VISIBLE);
			if (buttonInformation.getVisibility() == View.INVISIBLE)
					buttonInformation.setVisibility (View.VISIBLE);
			// -----------------------------------------------------------------
			// 05/04/2015 ECU tell the display handler that everything done
			// -----------------------------------------------------------------
			return false;
		}
	}
	// =============================================================================
	void changeRootFolderForMusic (Context theContext,boolean theRecoveryMode)
	{
		// -------------------------------------------------------------------------
		// 21/04/2015 ECU created to initialise the dialogue to get the root for
		//                music
		// -------------------------------------------------------------------------
		// 21/04/2015 ECU indicate whether the dialogue is as part of the 'recovery
		//                mode'
		// -------------------------------------------------------------------------
		recoveryMode = theRecoveryMode;
		// -------------------------------------------------------------------------
		// 21/04/2015 ECU if in recovery mode then define the associated handler
		// -------------------------------------------------------------------------
		if (recoveryMode)
			recoveryHandler = new RecoveryHandler ();
		// -------------------------------------------------------------------------
		returnedFiles = Utilities.returnSubDirectories (StaticData.ROOT_DIRECTORY,true,MUSIC_EXTENSION);
		// -------------------------------------------------------------------------
		DialogueUtilities.listChoice (theContext,
									  theContext.getString (R.string.title_root_folder),
									  returnedFiles,
									  Utilities.createAMethod (MusicPlayer.class,"BrowseForRootFolder",0),
									  theContext.getString (R.string.cancel_this_operation),
									  Utilities.createAMethod (MusicPlayer.class,"CancelRootFolder",0));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void closeMediaPlayer ()
	{
		// -------------------------------------------------------------------------
		// 28/12/2016 ECU created to close down the media player, is in use
		// -------------------------------------------------------------------------
		if (PublicData.mediaPlayer != null)
		{
			// ---------------------------------------------------------------------
			// 28/12/2016 ECU check if anything is being played - stop it if it is
			// ---------------------------------------------------------------------
			if (PublicData.mediaPlayer.isPlaying ())
				PublicData.mediaPlayer.stop ();
			// ---------------------------------------------------------------------
			// 28/12/2016 ECU release any resources associated with the player
			// ---------------------------------------------------------------------
			PublicData.mediaPlayer.release ();
			// ---------------------------------------------------------------------
			// 28/12/2016 ECU indicate that the player is not on use
			// ---------------------------------------------------------------------
			PublicData.mediaPlayer = null;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class DisplayHandler extends Handler
	{
		@Override
		public void handleMessage (Message theMessage) 
		{   
			// ---------------------------------------------------------------------
			if (changeIcons ())
			{
				// -----------------------------------------------------------------
				// 05/04/2015 ECU there is still activity so wait a bit then redo
				//				  otherwise just stop
				// -----------------------------------------------------------------
				sleep (500);
				// ---------------------------------------------------------------------
			}
		}
		/* ------------------------------------------------------------------------ */
		public void sleep(long delayMillis)
		{		
			this.removeMessages(0);
			// ---------------------------------------------------------------------
			// 05/04/2015 ECU only carry on if the user has not stopped the player
			// ---------------------------------------------------------------------
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};
	/* ============================================================================= */
	private static void displayAlbumArt (Context theContext,int theTrackNumber)
	{
		// -------------------------------------------------------------------------
		// 02/06/2013 ECU remember the track being played
		// 05/04/2015 ECU remember the track index - was the file name
		// -------------------------------------------------------------------------
		PublicData.musicPlayerData.trackPlaying = theTrackNumber;
		// -------------------------------------------------------------------------
		// 05/04/2015 ECU get the file name from the track details
		// -------------------------------------------------------------------------
		File theFile = new File (PublicData.musicPlayerData.tracks.get (theTrackNumber).fileName);
		// -------------------------------------------------------------------------
		// 21/04/2015 ECU look for the album art in the parent directory of this
		//                track, i.e. the album folder.
		// --------------------------------------------------------------------------
		albumArtFile = theFile.getParent() + "/" + theContext.getString (R.string.album_art_file);
		
		theFile = new File (albumArtFile);

		if (theFile.exists())
		{
			// ---------------------------------------------------------------------
			// 19/01/2014 ECU the '1' refers to the 'sample size'
			// ---------------------------------------------------------------------
			Utilities.displayAnImage (PublicData.mpImageView,albumArtFile,1);
		}
		else
		{
			 PublicData.mpImageView.setImageDrawable (theContext.getResources().getDrawable(R.drawable.music));
			 albumArtFile = null;
		}
		// -------------------------------------------------------------------------    
		// 02/06/2013 ECU update the image in the GridActivity 
		// -------------------------------------------------------------------------
		refreshGridCell (albumArtFile);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void Exit ()
	{
		//  ------------------------------------------------------------------------
		// 18/01/2015 ECU added as a separate method
		// -------------------------------------------------------------------------
		// 20/04/2015 ECU changed to use the new method
		// -------------------------------------------------------------------------
		tidyUp (MusicPlayer.this);
		// -------------------------------------------------------------------------
		// 10/02/2014 ECU try and remove the album art
		// -------------------------------------------------------------------------
		refreshGridCell (null);
		// -------------------------------------------------------------------------
		// 02/06/2013 ECU make sure the imageAdapter updates the view
		// 10/02/2014 ECU added userView check
		// 20/01/2015 ECU took out the
		//					MainActivity.customGridViewAdapter.notifyDataSetChanged();
		//                because this is done in the 'refreshGridCell' method
		//                the above also applies to
		//					MainActivity.imageAdapter.notifyDataSetChanged();
		// -------------------------------------------------------------------------
		// 02/05/2015 ECU indicate that this device is available to remote devices
		//                for their music
		// -------------------------------------------------------------------------
		setStatus (false);
		// -------------------------------------------------------------------------
		finish ();
	}
	// =============================================================================
	void handleRemotePlayer ()
	{
		// -------------------------------------------------------------------------
		// 02/05/2015 ECU the remote player uses a 'two file' cycle. While it is
		//                playing the first track it will download the second track
		//                which it will then play when the first one has finished.
		// -------------------------------------------------------------------------
		if (remoteTrackPlayingNow == StaticData.NO_RESULT)
		{
			// ---------------------------------------------------------------------
			// 02/05/2015 ECU this is the first time through so set the current
			//                track and reset the 'next'
			// ---------------------------------------------------------------------
			remoteTrackPlayingNow 	= 	PublicData.musicPlayerData.trackNumber;
			remoteTrackPlayingNext	=	StaticData.NO_RESULT;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/05/2015 ECU set the 'now' track to the stored 'next' providing
			//                that there is a value stored
			// ---------------------------------------------------------------------
			if (remoteTrackPlayingNext != StaticData.NO_RESULT)
				remoteTrackPlayingNow = remoteTrackPlayingNext;
			// ---------------------------------------------------------------------
			// 02/05/2015 ECU stored the current track to be played next
			// ---------------------------------------------------------------------
			remoteTrackPlayingNext 	=	PublicData.musicPlayerData.trackNumber;	
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 01/04/2015 ECU created to hold the code that used to be in RefreshHandler
		//                - just for clarity
		// -------------------------------------------------------------------------
		// 02/05/2015 ECU reset the waiting counter
		//            ECU get track duration in milliseconds
		// -------------------------------------------------------------------------
		int	trackDuration = PublicData.musicPlayerData.tracks.get (remoteTrackPlayingNow).duration;
		// -------------------------------------------------------------------------
		// 02/05/2015 ECU check if the duration has been set - if not then default to
		//                5 minutes. Where there is a duration then allow twice as long 
		//                to finish
		// 03/05/2015 ECU (*2) seemed to long so just add in PLAY_TIMEOUT
		// 01/09/2015 ECU changed to use StaticData
		// -------------------------------------------------------------------------
		if (trackDuration == StaticData.NO_RESULT)
			trackDuration = StaticData.MILLISECONDS_PER_MINUTE * 5;
		else
			trackDuration = trackDuration + PLAY_TIMEOUT;
		// -------------------------------------------------------------------------
		// 02/05/2015 ECU now set the counter - in units of the 'sleep' period
		// -------------------------------------------------------------------------
		waitingCounterRemote = trackDuration / SHORT_DELAY;
		// -------------------------------------------------------------------------
		// 14/08/2013 ECU check if last message processed OK
		// -------------------------------------------------------------------------
		if (PublicData.fileTransferStatus == StaticData.SOCKET_MESSAGE_FILE_ACK)
		{
			// -------------------------------------------------
			// 03/08/2013 ECU indicate that initial actions have 
			//                been taken
			// ---------------------------------------------------------------------
			remoteFirst = false;
			// ---------------------------------------------------------------------
			// 03/08/2013 ECU handle remotely tell it to play the
			//                last file loaded
			// ---------------------------------------------------------------------
			Utilities.sendDatagramType (MusicPlayer.this,PublicData.remoteMusicPlayer,StaticData.SOCKET_MESSAGE_PLAY);
			// ---------------------------------------------------------------------
			// 19/08/2013 ECU now that the last file has been 
			//                played then change to the alternative
			//                receiving file by toggling the appropriate bit
			// 03/09/2015 ECU change to use StaticData
			// ---------------------------------------------------------------------
			PublicData.socketMessageData 
				= Utilities.bitHandler (StaticData.BIT_TOGGLE, PublicData.socketMessageData, StaticData.SOCKET_DATA_FILE);
			// ---------------------------------------------------------------------
			// 04/08/2013 ECU change to use a version of SendFile 
			//                which transmits large files as chunks
			// 04/04/2015 ECU changed to use 'tracks'
			// ---------------------------------------------------------------------
			remoteFileName = PublicData.musicPlayerData.tracks.get (PublicData.musicPlayerData.trackNumber).fileName;
			// ---------------------------------------------------------------------
			// 20/04/2015 ECU set up the album art for this track
			// ---------------------------------------------------------------------
			displayAlbumArt (MusicPlayer.this,PublicData.musicPlayerData.trackNumber);
			// ---------------------------------------------------------------------
			// 15/08/2013 ECU added the 'true' argument to select a 
			//                method that will will try retransmission
			//				  in the event of a failure
			// 16/04/2015 ECU use the return from SendFile to determine whether
			//                the operation was or was not successful
			//            ECU change to use the common method
			// ---------------------------------------------------------------------
			if (SendFile (MusicPlayer.this,remoteFileName))
			{
				// -----------------------------------------------------------------
				// 03/08/2013 ECU indicate that the remote player is running
				// -----------------------------------------------------------------
				PublicData.musicPlayerRemote = true;
				// -----------------------------------------------------------------
			}
		}
		else
		if (PublicData.fileTransferStatus == StaticData.SOCKET_MESSAGE_FILE_NAK)
		{
			// ---------------------------------------------------------------------
			// 14/08/2013 ECU last file failed to be received so
			//                want to try again
			// ---------------------------------------------------------------------
			// 04/08/2013 ECU change to use a version of SendFile 
			//                which transmits large files as chunks
			// 04/04/2015 ECU changed to use 'tracks'
			// ---------------------------------------------------------------------
			remoteFileName 
				= PublicData.musicPlayerData.tracks.get(PublicData.musicPlayerData.trackNumber).fileName;
			// ---------------------------------------------------------------------
			// 15/08/2013 ECU added the 'true' argument to select 
			//                a method that will will try 
			// 			      retransmission in the event of a failure
			// 16/04/2015 ECU changed to use the common local method for sending a 
			//                file
			// ---------------------------------------------------------------------
			SendFile (MusicPlayer.this,remoteFileName);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	boolean initialiseData (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 21/04/2015 ECU created to initialise all aspects of the music player data
		//                and respond with success (true) or failure (false)
		// -------------------------------------------------------------------------
		// 21/04/2015 ECU first of all check if the data has already been initialised
		// -------------------------------------------------------------------------
		if (PublicData.musicPlayerData == null)
		{
			// ---------------------------------------------------------------------
			// 21/04/2015 ECU the data not initialised so try and get its copy from
			//                disk
			// ----------------------------------------------------------------------
			PublicData.musicPlayerData 
				= (MusicPlayerData) AsyncUtilities.readObjectFromDisk 
						(theContext,
						 PublicData.projectFolder + getString (R.string.music_player_data));
			
		}
		// -------------------------------------------------------------------------
		// 21/04/2015 ECU if the data fails to read then a 'null' is returned.
		//                If it reads successfully then need to check that it is
		//                for this device (this is because it could have been
		//                cloned).
		// 25/03/2016 ECU put in the check on trackNumber
		// -------------------------------------------------------------------------
		if ((PublicData.musicPlayerData == null) || 
			!PublicData.musicPlayerData.validateData (PublicData.deviceID) ||
			 PublicData.musicPlayerData.trackNumber == StaticData.NO_RESULT)
		{
			// ---------------------------------------------------------------------
			// 21/04/2015 ECU have been unable to read any information which is
			//                valid for this device
			// ---------------------------------------------------------------------
			// 21/04/2015 ECU create a new record with associated defaults
			// ---------------------------------------------------------------------
			PublicData.musicPlayerData = new MusicPlayerData ();
			// ---------------------------------------------------------------------
			// 21/04/2015 ECU build information based on music held on this
			//                device
			// ---------------------------------------------------------------------
			buildTrackInformation (theContext);
			// ---------------------------------------------------------------------		
			// 21/04/2015/2013 ECU initialise playlist to start from track 0
			// ---------------------------------------------------------------------	
			PublicData.musicPlayerData.trackNumber = 0; 
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 21/04/2015 ECU check whether the data shows that there is music to play
		// -------------------------------------------------------------------------
		if ((PublicData.musicPlayerData.tracks != null) &&
			(PublicData.musicPlayerData.tracks.size() > 0))
		{
			// ---------------------------------------------------------------------
			// 21/04/2015 ECU there seems to be music so can indicate 'success'
			// ---------------------------------------------------------------------
			return true;
		}
		else
		{
			// ---------------------------------------------------------------------
			// 21/04/2015 ECU does not appear to be any music so indicate 'failure'
			// ---------------------------------------------------------------------
			return false;
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@SuppressLint("HandlerLeak")
	class MusicRefreshHandler extends Handler
    {
		// -------------------------------------------------------------------------
		// -------------------------------------------------------------------------
		// 21/04/2015 ECU this handler performs the playing of tracks, both locally
		//                and remotely. It check the state of the media player to
		//                know ehen to move to the newxt track
		// -------------------------------------------------------------------------
		// -------------------------------------------------------------------------
        @Override
        public void handleMessage (Message theMessage) 
        {   
        	// ---------------------------------------------------------------------
        	//            ECU wait until any music has finished playing 
        	// 24/05/2013 ECU rearranged the order to give an initial delay 
        	// 02/06/2013 ECU include the check on trackPaused
        	// 16/06/2013 ECU include mediaPlayerPaused
        	// 03/08/2013 ECU include musicPlayerRemote
        	//                added remoteFirst
        	// 18/01/2015 ECU put in the check on mediaPlayer being null
        	// ---------------------------------------------------------------------
        	if (PublicData.mediaPlayer != null)
        	{   
        		// -----------------------------------------------------------------
        		// -----------------------------------------------------------------
        		// 20/04/2015 ECU the first check if to change some display information
        		//                if :-
        		//                	((the media player is playing) OR
        		//                   (the user has paused the track) OR
        		//                   (the media player has been paused by another 
        		//                    activity) OR
        		//                   (music is being sent to a remote device)) AND
        		//                  (not the first operations that need to be taken
        		//                   when music is sent to a remote device)
        		// -----------------------------------------------------------------
        		// -----------------------------------------------------------------
        		if ((PublicData.mediaPlayer.isPlaying() || 
        			 trackPaused || 
        			 PublicData.mediaPlayerPaused || 
        			 PublicData.musicPlayerRemote) 
        			 	&& !remoteFirst) 													
        		{
        			// -------------------------------------------------------------
        			// 02/05/2015 ECU if playing the music to a remote device then
        			//                try and detect if the device has become unavailable
        			// -------------------------------------------------------------
        			if (PublicData.musicPlayerRemote)
        			{
        				// ---------------------------------------------------------
        				// 02/05/2015 ECU if the counter reaches 0 then seem to have
        				//                lost contact
        				// ---------------------------------------------------------
        				if (--waitingCounterRemote <= 0)
        				{
        					// -----------------------------------------------------
        					// 02/05/2015 ECU contact has been lost so cancel
        					//                remote operation
        					// -----------------------------------------------------
        					Utilities.popToastAndSpeak (MusicPlayer.context.getString (R.string.lost_communication));
        					// -----------------------------------------------------
        					// 02/05/2015 ECU reset flags so that music is played
        					//                locally
        					// -----------------------------------------------------
        					CancelRemote (0);
        					// -----------------------------------------------------
        				}
        			}
        			// -------------------------------------------------------------
        			// -------------------------------------------------------------
        			// 01/04/2015 ECU the track is just playing so update the
        			//                artwork, if not already done, and also
        			//                update the progress bar, if enabled
        			// -------------------------------------------------------------
        			// -------------------------------------------------------------
        			// 02/06/2013 ECU check if need to refresh the album artwork
        			// -------------------------------------------------------------
        			if (PublicData.cellChange)
        			{
        				// ---------------------------------------------------------
        				// 02/06/2013 ECU indicate that action has been taken
        				// ---------------------------------------------------------
        				PublicData.cellChange = false;
        				// ---------------------------------------------------------
        				// 02/06/2013 ECU update the displayed image with the artwork
        				//                for the track being played, if it exists
        				// ---------------------------------------------------------
        				refreshGridCell (albumArtFile);
        				// ---------------------------------------------------------
        			}
        			// -------------------------------------------------------------
        			// 20/01/2014 ECU update the progress bar
        			// -------------------------------------------------------------
        			if (showProgressBar)
        				progressBar.setProgress (PublicData.mediaPlayer.getCurrentPosition()/SEEKBAR_SCALE);
        			// -------------------------------------------------------------
        			// 03/04/2015 ECU update the seekbar progress
        			//			  ECU put in check on metadata
        			// -------------------------------------------------------------
        			if (displayMetadata)
        				progressSeekBar.setProgress (PublicData.mediaPlayer.getCurrentPosition()/SEEKBAR_SCALE);
        			// -------------------------------------------------------------
        			// 20/01/2014 ECU wait a bit
        			// -------------------------------------------------------------
        			sleep (SHORT_DELAY);
        			// -------------------------------------------------------------
        		}
        		else
        		{
        			// -------------------------------------------------------------
        			// -------------------------------------------------------------
        			// 01/04/2015 ECU it appears that the current track has finished
        			//                playing so need to move to the next one in the
        			//                playlist
        			// -------------------------------------------------------------
         			// -------------------------------------------------------------
        			// 24/05/2013 ECU check if reached the endof the play list 
        			// 02/06/2013 ECU include the stopmpPlayer bit
        			// 04/04/2015 ECU changed to use 'tracks'
        			// 11/04/2015 ECU changed to check the null
        			// -------------------------------------------------------------
        			if ((PublicData.musicPlayerData.tracks != null) &&
        				(PublicData.musicPlayerData.trackNumber < (PublicData.musicPlayerData.tracks.size () - 1)) &&
        				!PublicData.stopmpPlayer)
        			{ 
        				// ---------------------------------------------------------
        				// ---------------------------------------------------------
        				// 01/04/2015 ECU there are still tracks to play so advance
        				//                to the next track
        				// ---------------------------------------------------------
        				// ---------------------------------------------------------
        				// 02/06/2013 ECU advance the track but take into account 
        				//                shuffle mode
        				// ---------------------------------------------------------			
        				if (!PublicData.musicPlayerData.shuffleMode)
        				{
        					// -----------------------------------------------------
        					// 02/06/2013 ECU not in shuffle mode so just step on 1
        					// -----------------------------------------------------
        					PublicData.musicPlayerData.trackNumber++;
        					// -----------------------------------------------------
        				} 
        				else
        				{
        					// -----------------------------------------------------
        					// 02/06/2013 ECU shuffle mode so randomly pick the next track
        					// 04/04/2015 ECU changed to use 'tracks'
        					// -----------------------------------------------------
        					PublicData.musicPlayerData.trackNumber 
        						= random.nextInt (PublicData.musicPlayerData.tracks.size() - 2);
        					// -----------------------------------------------------
        					// 02/03/2013 ECU the -2 is because if shuffle mode is 
        					//                switched off then I do not want to go 
        					//                beyond the array size
        					// -----------------------------------------------------
        				}
        			    // ---------------------------------------------------------
        				// 02/06/2013 ECU play the selected track
        				// ---------------------------------------------------------
        				if (!remoteMode)
        				{
         					// -----------------------------------------------------
        					// 03/08/2013 ECU the music player is for this device
        					// 05/04/2015 ECU changed to pass through track number
        					//                as the argument
        					// -----------------------------------------------------
        					playMusicTrack (MusicPlayer.this,PublicData.musicPlayerData.trackNumber);
        					// -----------------------------------------------------
        				}
        				else
        				{
        					// -----------------------------------------------------
        					// 01/04/2015 ECU put the code for remote access into
        					//                a separate method for clarity
        					// -----------------------------------------------------
        					handleRemotePlayer ();
        					// -----------------------------------------------------
        					
        				}
        				// ---------------------------------------------------------
        				// 03/08/2013 ECU wait a short period of time
        				// ---------------------------------------------------------
        				sleep (SHORT_DELAY);
        				// ---------------------------------------------------------
        			}
        			else
        			{
          				// ---------------------------------------------------------
        				// ---------------------------------------------------------
        				// 28/03/2015 ECU all of the tracks have been played so check
        				//                if repeat mode is on
        				// ---------------------------------------------------------
        				// ---------------------------------------------------------
        				if (PublicData.musicPlayerData.repeatMode)
        				{
        					// -----------------------------------------------------
        					// 28/03/2015 ECU repeat mode is on so just reset the
        					//                track to be played
        					// 31/03/2015 ECU set to -1 instead of 0 because it is
        					//                incremented before use
        					// -----------------------------------------------------
        					PublicData.musicPlayerData.trackNumber = -1;
        					// -----------------------------------------------------
        					// 28/03/2015 ECU and have a small delay
        					// -----------------------------------------------------
        					sleep (SHORT_DELAY);
        					// -----------------------------------------------------
        					
        				}
        				else
        				{
        					// -----------------------------------------------------
        					// 28/03/2015 ECU repeat mode is off so just finish
        					// -----------------------------------------------------
        					PublicData.musicPlayerData.tracksPlaying = false;
        					// ---------------------------------------------------------
    						// 01/04/2015 ECU change the image to indicate that pressing
    						//                it will start playing
    						// ---------------------------------------------------------
    						buttonPlay.setImageResource (R.drawable.music_play);
    						// ---------------------------------------------------------
        				}
        			}
        		}
        	}
        	else
        	{
        		// -----------------------------------------------------------------
        		// 18/01/2015 ECU just stop this activity
        		// -----------------------------------------------------------------
        		Exit ();
        		// -----------------------------------------------------------------
        	}
        		
        }
        /* ------------------------------------------------------------------------ */
        public void sleep(long delayMillis)
        {		
            this.removeMessages(0);
            
            if (!PublicData.stopmpPlayer)
            {
            	// -----------------------------------------------------------------
            	// 02/06/2013 ECU only carry on if the user has not stopped the player
            	// -----------------------------------------------------------------
            	sendMessageDelayed(obtainMessage(0), delayMillis);
            }
            else
            {
            	// -----------------------------------------------------------------
            	// 02/06/2013 ECU tidy up at the end
            	// -----------------------------------------------------------------
            	PublicData.musicPlayerData.tracksPlaying = false;
            	// -----------------------------------------------------------------
            }
        }
    };
    // =============================================================================
	private static void playMusicTrack (Context theContext,int theTrackNumber,int thePosition)
	{
		// -------------------------------------------------------------------------
		// 05/04/2015 ECU get the file name from the track number
		// -------------------------------------------------------------------------
		String fileName = PublicData.musicPlayerData.tracks.get (PublicData.musicPlayerData.trackNumber).fileName;
		// -------------------------------------------------------------------------
		// 20/01/2014 ECU check whether the file exists
		// -------------------------------------------------------------------------
		if ((new File (fileName)).exists())
		{
			// ---------------------------------------------------------------------
			// 03/06/2016 ECU added the false to indicate not an 'action'
			// ---------------------------------------------------------------------
			Utilities.PlayAFile (theContext,fileName,thePosition,false);
			// ---------------------------------------------------------------------
			// 02/06/2013 ECU check if there is any album art for this track
			// 05/04/2015 ECU changed to pass through the track index rather than
			//                the file name
			// 21/04/2015 ECU changed from theTrackNumber
			// ---------------------------------------------------------------------
			displayAlbumArt (theContext,PublicData.musicPlayerData.trackNumber);
			// ---------------------------------------------------------------------
			// 12/04/2015 ECU get the track information and store
			// ---------------------------------------------------------------------
			trackInformation = PublicData.musicPlayerData.tracks.get(PublicData.musicPlayerData.trackNumber).trackInformation();
			// ---------------------------------------------------------------------
			// 02/06/2013 ECU display the meta data
			// 01/04/2015 ECU removed the 'else' condition which just set  the 
			//                title
			// 05/04/2015 ECU took out the 'getMetadata' call and now use
			//                'showMetaData'
			// 25/03/2015 ECU changed from 'theTrackNumber' which was generating
			//                an 'out of bounds' exception
			// ---------------------------------------------------------------------
			if (displayMetadata)
				showMetadata (PublicData.musicPlayerData.trackNumber);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 20/01/2014 ECU for some reason the file no longer exists so do a rebuild
			// 04/04/2015 ECU changed to use the returned array
			//			  ECU changed to use local method
			// -----------------------------------------------------------------			
			buildTrackInformation (theContext);
			// ---------------------------------------------------------------------
			// 31/05/2013 ECU initialise playlist flags
			// ---------------------------------------------------------------------
			PublicData.musicPlayerData.trackNumber = 0;  
			// ---------------------------------------------------------------------
			// 20/01/2014 ECU and start playing from the beginning
			// 04/04/2015 ECU changed to use 'tracks'
			// 05/04/2015 ECU changed to use the index to the track
			// ---------------------------------------------------------------------
			playMusicTrack (theContext,0);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 20/01/2014 ECU set up the progress bar
		// -------------------------------------------------------------------------
		if (showProgressBar)
		{
			ShowProgressBar (fileName);
		}
		// ------------------------------------------------------------------------
		// 03/04/2015 ECU show the seek bar progress
		//			  ECU add the check on metadata
		// 06/05/2015 ECU take out the check on displayMetaData because always
		//                want to initialse the bar just so that it is ready
		//                for when the display is switched on
		// ------------------------------------------------------------------------
		progressSeekBar.setMax (PublicData.mediaPlayer.getDuration()/SEEKBAR_SCALE);
		// ------------------------------------------------------------------------
		// 28/03/2015 ECU display the track details
		// 31/03/2015 ECU added the '+ 1'
		// 04/04/2015 ECU changed to use 'tracks'
		// 05/04/2015 ECU removed from here and put into getAlbumArt
		// ------------------------------------------------------------------------
	}
	/* ----------------------------------------------------------------------------- */
	private static void playMusicTrack (Context theContext,int theTrackNumber)
	{
		// -------------------------------------------------------------------------
		// 05/04/2015 ECU changed to have track number as argument which is then
		//                passed through
		// -------------------------------------------------------------------------
		playMusicTrack (theContext,theTrackNumber,StaticData.NO_RESULT);
	}
    // =============================================================================
    public static void playOrPause (boolean theRequiredStatus)
    {
    	// -------------------------------------------------------------------------
    	// 22/05/2016 ECU created to either play or pause the current track. Made
    	//                public so that it is available to the 'text to speech' service
    	//
    	//					theRequiredStatus	=	true		play
    	//										=   false       pause
    	// 14/11/2016 ECU put in try/catch - just in case
    	// -------------------------------------------------------------------------
    	try
    	{
    		if (theRequiredStatus)
    		{
    			// -----------------------------------------------------------------
    			// 22/05/2016 ECU set to play (in fact a 'resume')
    			// -----------------------------------------------------------------
    			PublicData.mediaPlayer.seekTo (PublicData.musicPlayerData.trackPosition);
    			PublicData.mediaPlayer.start ();
    			// -----------------------------------------------------------------
    			trackPaused = false;
    			// -----------------------------------------------------------------	
    		}
    		else
    		{
    			// -----------------------------------------------------------------
    			// 22/05/2016 ECU pause playing the current track
    			// -----------------------------------------------------------------
    			PublicData.mediaPlayer.pause ();
    			PublicData.musicPlayerData.trackPosition = PublicData.mediaPlayer.getCurrentPosition();
    			trackPaused = true;
    			// -----------------------------------------------------------------
    		}
    	}
    	catch (Exception theException)
    	{
    		
    	}
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
	static void populateDetails (final boolean theSortFlag)
	{
		// -------------------------------------------------------------------------
		// 04/042015 ECU created to populate the lists associated with music
		// -------------------------------------------------------------------------
		// 04/04/2015 ECU first of all do the list of tracks
		// -------------------------------------------------------------------------
		populateThread = new Thread()
		{
			@Override
			public void run()
			{
				// -----------------------------------------------------------------
				// 04/04/2015 ECU loop for all stored tracks
				// -----------------------------------------------------------------
				for (int theTrack = 0; theTrack < PublicData.musicPlayerData.tracks.size(); theTrack++)
				{
					PublicData.musicPlayerData.tracks.get(theTrack).populate ();
				}
				// -----------------------------------------------------------------
				// 05/04/2015 ECU if necessary sort the list
				// -----------------------------------------------------------------
				if (theSortFlag)
					Collections.sort (PublicData.musicPlayerData.tracks); 
				// -----------------------------------------------------------------
				// 05/04/2015 ECU saved this initial list as it is subject to
				//                modification later
				// -----------------------------------------------------------------
				PublicData.musicPlayerData.savedTracks = PublicData.musicPlayerData.tracks;
				// -----------------------------------------------------------------
				// 05/04/2015 ECU have built tracks so build artist information
				// -----------------------------------------------------------------
				buildArtistList (true);
				// -----------------------------------------------------------------
			}
		};
		// -------------------------------------------------------------------------
		// 05.04/2015 ECU start up the thread
		// -------------------------------------------------------------------------
		populateThread.start();  
		// -------------------------------------------------------------------------
		// 05/04/2015 ECU start up the display handler
		// -------------------------------------------------------------------------
		displayHandler.sleep (SHORT_DELAY);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void rebuildTracks (Context theContext)
	{
		// -------------------------------------------------------------
		// 28/03/2015 ECU 'rebuild music list' button
		// -------------------------------------------------------------
		// 31/05/2013 ECU rebuild the music playlist
		// 04/04/2015 ECU changed to use the returned array
		// 			  ECU changed to use local method
		// -----------------------------------------------------------------			
		buildTrackInformation (theContext);
		// -------------------------------------------------------------
		// 29/03/2015 ECU tell the user what has happened
		// 30/03/2015 ECU add the centre-ing flag
		// -------------------------------------------------------------
		Utilities.popToastAndSpeak (theContext.getString(R.string.rebuild_play_list),true);
		// -------------------------------------------------------------
		// 31/05/2013 ECU initialise playlist flags
		// 31/03/2015 ECU set track number to -1 instead of 0 because
		//                it will be incremented before use
		// 05/04/2015 ECU switch off the display of information because
		//                the data is being updated and could cause an error
		// -------------------------------------------------------------
		displayMetadata								= false;		
		PublicData.musicPlayerData.trackNumber    = -1;               // used to indicate which entry is playing
		PublicData.musicPlayerData.shuffleMode    = false;            // switch shuffle mode off
		PublicData.mediaPlayer.stop();
		// -------------------------------------------------------------
		// 29/03/2015 ECU make sure that the status icons are updated
		// -------------------------------------------------------------
		updateStatusIcons ();
		// -------------------------------------------------------------
	}
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class RecoveryHandler extends Handler
    {
		// -------------------------------------------------------------------------
		// 21/04/2015 ECU created to handle recovery issues
		// -------------------------------------------------------------------------
        @Override
        public void handleMessage (Message theMessage) 
        {   
        	// ---------------------------------------------------------------------
        	// 21/04/2015 ECU rebuild the track list now that a root has been supplied
        	// ---------------------------------------------------------------------
        	buildTrackInformation (MusicPlayer.context);
        	// ---------------------------------------------------------------------
        	// 21/04/2015 ECU reset the track number
        	// ---------------------------------------------------------------------
        	PublicData.musicPlayerData.trackNumber = 0;
        	// ---------------------------------------------------------------------
        	playMusicTrack (MusicPlayer.context,0);
        	// ---------------------------------------------------------------------
        	// 21/04/2015 ECU start the refresh handler to get things going
        	// ---------------------------------------------------------------------
        	musicRefreshHandler.sleep (SHORT_DELAY);
        	// ---------------------------------------------------------------------
        }
        /* ------------------------------------------------------------------------- */
        public void sleep(long delayMillis)
        {		
            this.removeMessages (0);
            sendMessageDelayed(obtainMessage (0), delayMillis);
        }
    };
	// =============================================================================
	public static void refreshGridCell (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 02/06/2013 ECU update the image in the GridActivity if album art exists
		// 10/02/2014 ECU note that there may be issues if 'userView' indicates that
		//                a list view is being used in which case the albumView may
		//                cause a problem. Needs some more testing.
		// -------------------------------------------------------------------------
		// 20/01/2015 ECU reset the albumArtFile because it may be set to null to
		//                indicate that the music player has been exited
		// -------------------------------------------------------------------------
		albumArtFile = theFileName;
		// -------------------------------------------------------------------------
		// 20/01/2015 ECU need to handle differently depending on whether grid or
		//                list on display
		// 28/04/2015 ECU changed to use the method rather than have code inline
		// 05/05/2015 ECU changed to use new method
		// ---------------------------------------------------------------------
		refreshImageAdapter ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    public static void refreshImageAdapter ()
    {
    	//--------------------------------------------------------------------------
    	// 05/05/2015 ECU created to cause the currently active adapter, set up in
    	//                GridActivity, to be refreshed by issuing a 'notify data
    	//                changed' command.
    	//            ECU the check on 'null' was added just in case a timing
    	//                issue means that the handler is not set up yet
		// -------------------------------------------------------------------------
    	if (GridActivity.gridRefreshHandler != null)
    	{
    		// ---------------------------------------------------------------------
    		// 05/05/2015 ECU get a message for the handler and preset the 'what'
    		//                value to indicate that the image adapter is to be refreshed
    		// ---------------------------------------------------------------------
    		GridActivity.gridRefreshHandler.sendEmptyMessage (StaticData.MESSAGE_ADAPTER);
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void SearchTracks (int theFolderIndex)
    {
		 // ------------------------------------------------------------------------
		 // 08/04/2015 ECU created to initiate the search of tracks
		 // 10/04/2015 ECU changed to use new dialogue which contains options
		 //                as well as the search string
		 // ------------------------------------------------------------------------
		 DialogueUtilities.searchChoice (DialogueUtilities.context,
				 	DialogueUtilities.context.getString (R.string.title_search_string),
				 	DialogueUtilities.context.getString (R.string.confirm),
					Utilities.createAMethod (MusicPlayer.class,"ConfirmSearch",(Object)(new SearchParameters())),
					DialogueUtilities.context.getString (R.string.cancel),
					Utilities.createAMethod (MusicPlayer.class,"CancelSearch",(Object)(new SearchParameters())));	
		 // ------------------------------------------------------------------------
	}
	// =============================================================================
	static ArrayList<TrackDetails> searchForTracks (ArrayList<TrackDetails> theTracks,SearchParameters theSearchParameters)
	{
		// -------------------------------------------------------------------------
		// 08/04/2015 ECU created to raturn those tracks which contain the search
		//                string. I know that show use regex but want something that
		//                is easy to understand as speed not too important. Everything
		//                is compared in lower case
		// 10/04/2015 ECU add the search parameters as an argument
		// -------------------------------------------------------------------------
		String localSearchString = theSearchParameters.searchString.toLowerCase(Locale.getDefault());
		// -------------------------------------------------------------------------
		ArrayList<TrackDetails>	localTracks = new ArrayList<TrackDetails> ();
		// -------------------------------------------------------------------------
		// 08/04/2015 ECU loop through all current tracks
		// 10/04/2015 ECU changed to pick up the fields that are to be searched
		// -------------------------------------------------------------------------
		boolean			includeTrack;
		TrackDetails	track;
		// -------------------------------------------------------------------------
		for (int theTrack = 0; theTrack < theTracks.size(); theTrack++)
		{
			includeTrack	= false;
			track			= theTracks.get (theTrack);
			// ---------------------------------------------------------------------
			// 10/04/2015 ECU a track will be included in the results list if the
			//                search string matches any of the selected fields
			//                as defined in the options field of the search parameters
			//                object
			// ---------------------------------------------------------------------
			if (theSearchParameters.searchOptions [SEARCH_PARAMETER_ALBUM])
			{
				if ((track.album.toLowerCase(Locale.getDefault())).contains(localSearchString))
					includeTrack = true;
			}
			if (!includeTrack && theSearchParameters.searchOptions [SEARCH_PARAMETER_ARTIST])
			{
				if ((track.artist.toLowerCase(Locale.getDefault())).contains(localSearchString))
					includeTrack = true;
			}
			if (!includeTrack && theSearchParameters.searchOptions [SEARCH_PARAMETER_COMPOSER])
			{
				if ((track.composer.toLowerCase(Locale.getDefault())).contains(localSearchString))
					includeTrack = true;
			}
			if (!includeTrack && theSearchParameters.searchOptions [SEARCH_PARAMETER_TITLE])
			{
				if ((track.title.toLowerCase(Locale.getDefault())).contains(localSearchString))
					includeTrack = true;
			}
			// ---------------------------------------------------------------------
			if (includeTrack)
			{
				// -----------------------------------------------------------------
				// 08/04/2015 ECU add the track into results list
				// -----------------------------------------------------------------
				localTracks.add(theTracks.get(theTrack));
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 08/04/2015 ECU return the tracks found
		// -------------------------------------------------------------------------
		return localTracks;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static boolean SendFile (Context theContext,String theFileName)
	{
		// ------------------------------------------------------------------------
		// 16/04/2015 ECU create as a common method for sending a file and to 
		//                handle the return status
		// ------------------------------------------------------------------------
		if (Utilities.SendFile (theContext,
				PublicData.remoteMusicPlayer,
								PublicData.socketNumberForData,
								theFileName,
								StaticData.SOCKET_CHUNK_SIZE,true))
		{
			// ---------------------------------------------------------------------
			// 16/04/2015 ECU sendFile was successful
			// ---------------------------------------------------------------------
			// 13/04/2015 ECU indicate where the music is being sent
			// -------------------------------------------------------------------------
			String deviceName = Utilities.GetDeviceName (PublicData.remoteMusicPlayer);
			// ---------------------------------------------------------
			// 16/04/2015 ECU provide information about music that has been
			//                sent
			// ---------------------------------------------------------
			setMarqueeText ("Last Track sent to " + 
					((deviceName == null) ? PublicData.remoteMusicPlayer : deviceName) + " was " +
					TrackDetails.trackInformation (theFileName));
			// -------------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 16/04/2015 ECU sendFile was not successful
			// ---------------------------------------------------------------------
			// 16/04/2015 ECU cancel any remote operations
			// ---------------------------------------------------------------------
			CancelRemote (0);
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	private static void setImageViewSize (ImageView theImageView,int theSize)
	{
		// -------------------------------------------------------------------------
		// 29/03/2015 ECU set the size of the specified image view
		// -------------------------------------------------------------------------
		theImageView.getLayoutParams().height = theSize;
	    theImageView.getLayoutParams().width  = theSize; 
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void setMarqueeText (String theMarqueeText)
	{
		// -------------------------------------------------------------------------
		// 13/04/2015 ECU created to set the text that is wanted to show track
		//                information and to request that the adapter be refreshed
		//                to show the information immediately
		//
		//				  If theMarqueeText is 'null' then this will have the effect
		//                of removing any existing text
		// -------------------------------------------------------------------------
		if (PublicData.storedData.marquee)
		{
			// ---------------------------------------------------------
			MusicPlayer.trackInformation = theMarqueeText;
			// ---------------------------------------------------------
			// 13/04/2015 ECU request that the appropriate adapter be 
			//                updated
			// 17/04/2015 ECU put the check on null just in case there
			//                is a timing issue
			// 05/05/2015 ECU changed to use the new method to request
			//                that the appropriate image adapter be
			//                updated
			// ---------------------------------------------------------
			refreshImageAdapter ();
			// ---------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void setMarqueeText (String theMarqueeText,boolean theMarqueeState)
	{
		// -------------------------------------------------------------------------
		// 07/05/2016 ECU created to set the marquee state before calling the
		//                main method
		// -------------------------------------------------------------------------
		PublicData.storedData.marquee = theMarqueeState;
		setMarqueeText (theMarqueeText);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void setStatus (boolean theStatus)
	{
		// -------------------------------------------------------------------------
		// 02/05/2015 ECU created to set the availability of this device to
		//                accept remote music because it is already busy (true)
		//                or available (false)
		// -------------------------------------------------------------------------
		PublicData.status.remoteMusicMode = theStatus;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void setVolume (MediaPlayer theMediaPlayer,int theVolume,int theScaleFactor)
	{
		// -------------------------------------------------------------------------
		// 04/04/2015 ECU sets the MediaPlayer to the volume. MediaPlayer uses
		//                a volume between 0.0 to 1.0 - this app uses a colume
		//                scaled between 0 and theScaleFactor
		// ------------------------------------------------------------------------- 
		if (theMediaPlayer != null)
		{
			float adjustedVolume = (float) theVolume / (float) theScaleFactor;
			PublicData.mediaPlayer.setVolume(adjustedVolume, adjustedVolume);
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	static void showMetadata (int theTrackNumber)
	{
		// -------------------------------------------------------------------------
		// 05/04/2015 ECU created to display details held in a track
		// 02/04/2016 ECU try and validate the selected track number
		// -------------------------------------------------------------------------
		if (theTrackNumber >= 0 && theTrackNumber < PublicData.musicPlayerData.tracks.size())
		{
			TrackDetails trackDetails = PublicData.musicPlayerData.tracks.get (theTrackNumber);
 	   		// ---------------------------------------------------------------------
			// 30/03/2015 ECU if the table is visible then display the details otherwise
 	   		//                display the information as a 'popToast'
			// ---------------------------------------------------------------------
 	   		if (albumDetailsTable.getVisibility() == View.VISIBLE)
 	   		{
 	   			// -----------------------------------------------------------------
 	   			// 29/03/2015 ECU copy the data into the correct fields
 	   			// -----------------------------------------------------------------
 	   			albumDetailsArtist.setText (trackDetails.artist);
 	   			albumDetailsComposer.setText (trackDetails.composer);
 	   			albumDetailsName.setText (trackDetails.album); 
 	   			albumDetailsTrackName.setText (trackDetails.title); 
 	   			// -----------------------------------------------------------------
 	   		}
 	   		else
 	   		{
 	   			// -----------------------------------------------------------------
 	   			// 30/03/2015 ECU table is not on display so use toast with centre-ing
 	   			// -----------------------------------------------------------------
 	   			Utilities.popToast (MainActivity.activity.getString (R.string.album_name_textview)  	+ " : " + trackDetails.album + "\n" +
 	   								MainActivity.activity.getString (R.string.track_name_textview)	 	+ " : " + trackDetails.title + "\n" +
 	   								MainActivity.activity.getString (R.string.artist_textview) 			+ " : " + trackDetails.artist + "\n" +
 	   								MainActivity.activity.getString (R.string.track_composer_textview)	+ " : " + trackDetails.composer,true);
 	   			// -----------------------------------------------------------------
 	   		}
 	   		// ---------------------------------------------------------------------
 	   		// 05/04/2015 ECU update the number of tracks at the same time
 	   		// ---------------------------------------------------------------------
 	   		musicTrackTextView.setText("Playing track " + (theTrackNumber + 1) +
 	   									" of " + PublicData.musicPlayerData.tracks.size());
		}
 		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	static void ShowProgressBar (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 15/01/2014 ECU set up the maximum size of the progress bar
		// -------------------------------------------------------------------------
		progressBar.setMax (PublicData.mediaPlayer.getDuration()/SEEKBAR_SCALE);
		// -------------------------------------------------------------------------
		// 15/01/2014 ECU show the current track as a title
		// -------------------------------------------------------------------------
		MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever ();
		mediaMetadataRetriever.setDataSource (theFileName);
		progressBar.setTitle (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) + " by " +
								mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
		// ------------------------------------------------------------------------
		// 15/01/2014 ECU display the progress bar
		// ------------------------------------------------------------------------
		progressBar.show ();
		// ------------------------------------------------------------------------
		
	}
	// ============================================================================
	static void startRemoteMusicPlayer (String theIPAddress)
	{
		// ------------------------------------------------------------------------
		// 15/03/2015 ECU created to hold the code, previously against item selection,
		//                that starts up the music player on the specified device
		// 16/03/2015 ECU took out the check on 'remoteMode' which is done elsewhere
		// -------------------------------------------------------------------------
		// 15/03/2015 ECU copy address the IP address
		// -------------------------------------------------------------------------
		PublicData.remoteMusicPlayer = theIPAddress;
		// -------------------------------------------------------------------------
		// 15/03/2015 ECU want to put this device into remote mode with music
		//                being played on the specified device
		// -------------------------------------------------------------------------
		// 03/08/2013 ECU send the file currently being played
		// 04/08/2013 ECU change to use a version of SendFile which transmits large files as chunks
		//            ECU remember the file name being sent
		// -------------------------------------------------------------------------
		remoteFileName = PublicData.musicPlayerData.tracks.get (PublicData.musicPlayerData.trackNumber).fileName;
		// -------------------------------------------------------------------------
		// 15/08/2013 ECU added the 'true' argument to select a method that will will try retransmission
		//				  in the event of a failure
		// 16/04/2015 ECU use the return from SendFile to determine whether
		//                the operation was or was not successful
		// -------------------------------------------------------------------------
		if (SendFile (MusicPlayer.context,remoteFileName))
		{
			// ---------------------------------------------------------------------
			// 17/04/2015 ECU now that there is a good connection to the remote device
			//                then stop the music playing on this device
			// ---------------------------------------------------------------------
			PublicData.mediaPlayer.stop (); 
			// ---------------------------------------------------------------------
			// 03/08/2013 ECU indicate that some initial actions are required
			// ---------------------------------------------------------------------
			remoteFirst = true;
			// ---------------------------------------------------------------------
			// 02/05/2015 ECU preset some variables
			// ---------------------------------------------------------------------
			remoteTrackPlayingNow	=	StaticData.NO_RESULT;	
			remoteTrackPlayingNext	= 	StaticData.NO_RESULT;	
			//----------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 17/04/2015 ECU unable to connect to remote device
			// ---------------------------------------------------------------------
			Utilities.popToast (DialogueUtilities.context.getString (R.string.unable_to_connect));
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static void tidyUp (Context theContext)
	{
		//  ------------------------------------------------------------------------
		// 20/04/2015 ECU created to tidy up when exiting the activity or when
		//                called by MainActivity when it is destroyed
		// -------------------------------------------------------------------------
		// 04/06/2013 ECU write the current data to disk
		// 21/04/2015 ECU put in the check on null
		// -------------------------------------------------------------------------
		if (PublicData.musicPlayerData != null)
			WriteToDisk (PublicData.projectFolder + theContext.getString (R.string.music_player_data));
		// -------------------------------------------------------------------------
		// 04/06/2013 ECU stop the media player 
		// 18/01/2015 ECU put in the check on null
		// -------------------------------------------------------------------------
		if (PublicData.mediaPlayer != null)
			PublicData.mediaPlayer.stop();
		// -------------------------------------------------------------------------			
		// 02/06/2013 ECU indicate that the player has been stopped
		// -------------------------------------------------------------------------
		PublicData.stopmpPlayer = true;
		// -------------------------------------------------------------------------
		// 13/04/2015 ECU delete the track information. Setting to 'null' will cause
		//                the adapter to set the TextView to 'GONE'
		// -------------------------------------------------------------------------
		trackInformation = null; 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void toggleShuffleMode (boolean theToggleFlag)
	{
		// -------------------------------------------------------------------------
		// 01/04/2015 ECU toggle the shuffle mode
		// -------------------------------------------------------------------------
		if (theToggleFlag)
			PublicData.musicPlayerData.shuffleMode = !PublicData.musicPlayerData.shuffleMode;
		// -------------------------------------------------------------------------
		// 04/04/2015 ECU changed to use 'tracks'
		// -------------------------------------------------------------------------
		if (PublicData.musicPlayerData.shuffleMode && PublicData.musicPlayerData.tracks.size() < SHUFFLE_LIMIT)
		{
			// ---------------------------------------------------------------------
			// 01/04/2015 ECU force shuffle mode to be off
			// ---------------------------------------------------------------------
			PublicData.musicPlayerData.shuffleMode = false;
			// ---------------------------------------------------------------------	
		}	
		// -------------------------------------------------------------------------
		// 01/04/2015 ECU update the display status icons
		// -------------------------------------------------------------------------
		updateStatusIcons ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void updateImageView (ImageView theImageView,TextView theSubTitleTextView)
	{
		// -------------------------------------------------------------------------
		// 20/01/2015 ECU created so that it can be called by the adapters to change
		//                the album art
		// 12/04/2015 ECU added the subtitle text view
		// 31/12/2016 ECU Note - there used to be a method with only theImageView as
		//                ====   an argument - theSub... was passed as a 'null' -
		//                       with the introduction of UserInterface this was no
		//                       longer needed.
		// -------------------------------------------------------------------------
		if (albumArtFile != null)
			Utilities.displayAnImage (theImageView,MusicPlayer.albumArtFile);
		else
			theImageView.setImageResource (R.drawable.music);
		// -------------------------------------------------------------------------
		// 20/01/2015 ECU indicate that the image has been changed
		// 12/04/2015 ECU put in the MARQUEE check
		// 13/04/2015 ECU changed following move of marquee to storedData
		// -------------------------------------------------------------------------
		if (!PublicData.storedData.marquee)
		{
			// ---------------------------------------------------------------------
			// 12/04/2015 ECU this is required because this causes a call
			//                to refreshGridCell (via the handler) which in turn
			//                calls notifyDataSetChanged which seems to stop the
			//                marquee effect
			// ---------------------------------------------------------------------
			PublicData.cellChange = true;
			// ---------------------------------------------------------------------
			// 12/04/2015 ECU make sure that the field is 'gone'
			// ---------------------------------------------------------------------
			if (theSubTitleTextView != null && (theSubTitleTextView.getVisibility() != View.GONE))
			{
				theSubTitleTextView.setVisibility (View.GONE);
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 12/04/2015 ECU marquee processing is wanted on the subtitle field
			// ---------------------------------------------------------------------
			// 12/04/2015 ECU and change the subtitle field, if required
			// ---------------------------------------------------------------------
			if (theSubTitleTextView != null)
			{
				if (trackInformation != null)
				{
					// -------------------------------------------------------------
					// 12/04/2015 ECU if the text view is not visible then make it so
					// -------------------------------------------------------------
					if (theSubTitleTextView.getVisibility() != View.VISIBLE)
						theSubTitleTextView.setVisibility (View.VISIBLE);
					// -------------------------------------------------------------
					// 12/04/2015 ECU display the track information
					// -------------------------------------------------------------
					theSubTitleTextView.setText (trackInformation);
					// -------------------------------------------------------------
					// 12/04/2015 ECU make sure the text view is selected
					// -------------------------------------------------------------
					theSubTitleTextView.setSelected (true);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 13/04/2015 ECU if null then this is an indication to hide the
					//                text view
					// -------------------------------------------------------------
					theSubTitleTextView.setVisibility (View.GONE);
					// -------------------------------------------------------------
				}
			}
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	private static void updateStatusIcons ()
	{
		// -------------------------------------------------------------------------
		// 29/03/2015 ECU created because the icons can be updated from different
		//                places in the code
		// -------------------------------------------------------------------------
		// 29/03/2015 ECU set the image for the information button
		// -------------------------------------------------------------------------
		buttonInformation.setImageResource (displayMetadata ? R.drawable.music_information_off
															: R.drawable.music_information_on);
		// -------------------------------------------------------------------------
		// 29/03/2015 ECU check if table is to be displayed
		// 30/03/2015 ECU put in the small screen check because not
		//                enough space to display information -
		//                will use toast
		// -------------------------------------------------------------------------
		if (displayMetadata && !smallScreen)
		{
			albumDetailsTable.setVisibility(View.VISIBLE);
			// ---------------------------------------------------------------------
			// 29/03/2015 ECU adjust the size of the album art
		    // ---------------------------------------------------------------------
			setImageViewSize (PublicData.mpImageView,PublicData.screenWidth/2);
			// ---------------------------------------------------------------------
			// 29/03/2015 ECU get initial track details
			// 04/04/2015 ECU changed to use 'tracks'
			// 05/04/2015 ECU changed to 'showMetaData' because each track has the
			//                records in it
			// ---------------------------------------------------------------------
			showMetadata (PublicData.musicPlayerData.trackNumber);
			// ---------------------------------------------------------------------
			// 05/04/2015 ECU display the 'Playing track ..' text field
			// ---------------------------------------------------------------------
			musicTrackTextView.setVisibility (View.VISIBLE);
			// ---------------------------------------------------------------------
		}
		else
		{
			albumDetailsTable.setVisibility (View.GONE);
			// ---------------------------------------------------------------------
			// 29/03/2015 ECU adjust the size of the album art
			// 29/03/2015 ECU adjust for a small screen
		    // ---------------------------------------------------------------------
			setImageViewSize (PublicData.mpImageView,(PublicData.screenWidth * 2) / (3 + (smallScreen ? 1 : 0)));
			// ---------------------------------------------------------------------
			// 05/04/2015 ECU hide the 'Playing track ..' text field. Didn't use
			//                GONE because want to retain the layout
			// ---------------------------------------------------------------------
			musicTrackTextView.setVisibility (View.INVISIBLE);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 28/03/2015 ECU set the image for the progress bar button
		// -------------------------------------------------------------------------
		buttonProgressBar.setImageResource (showProgressBar ? R.drawable.music_progress_bar_on
															: R.drawable.music_progress_bar_off);
		// -------------------------------------------------------------------------
		// 28/03/2015 ECU set the image for the repeat button
		// -------------------------------------------------------------------------
		buttonRepeat.setImageResource (PublicData.musicPlayerData.repeatMode ? R.drawable.music_repeat_on
																			 : R.drawable.music_repeat_off); 
		// -------------------------------------------------------------------------
		// 28/03/2015 ECU set the image for the shuffle button
		// -------------------------------------------------------------------------
		buttonShuffle.setImageResource (PublicData.musicPlayerData.shuffleMode ? R.drawable.music_shuffle_on
																			   : R.drawable.music_shuffle_off);
		// -------------------------------------------------------------------------	 
	}
	/* ============================================================================= */
	private static void WriteToDisk (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 04/06/2013 ECU check if already written to disk
		// 19/06/2013 ECU use MainActivity.musicDataWritten instead of a local boolean
		// 19/01/2014 ECU change to use the Write method in Utilities
		// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
		// 18/01/2015 ECU put in check on null
		// -------------------------------------------------------------------------
		if (PublicData.mediaPlayer != null)
			PublicData.musicPlayerData.trackPosition = PublicData.mediaPlayer.getCurrentPosition();
		// -------------------------------------------------------------------------
		// 03/02/2015 ECU before writing to disk make sure that the data is stamped
		//                with the ID of this device
		// -------------------------------------------------------------------------
		PublicData.musicPlayerData.setDeviceID (PublicData.deviceID);
		// -------------------------------------------------------------------------
		// 31/03/2015 ECU reset the tracks if they've been changed
		// 04/04/2015 ECU changed to use 'tracks'
		// -------------------------------------------------------------------------
		//if (savedTracks != null)
		//		PublicData.musicPlayerData.tracks = savedTracks;
		// -------------------------------------------------------------------------
		AsyncUtilities.writeObjectToDisk (theFileName, PublicData.musicPlayerData);
		// -------------------------------------------------------------------------	
	}
	// =============================================================================
	
	
	// =============================================================================
	// =============================================================================
	// 08/04/2015 ECU this section contains methods used by the dialogue utilities
	// =============================================================================
	// =============================================================================
	
	// ============================================================================
	public static void AcceptRootFolder (int theFolderIndex)
	{
		// -------------------------------------------------------------------------
		// 06/04/2015 ECU created to change the root folder of the Music
		// -------------------------------------------------------------------------
		PublicData.musicPlayerData.rootFolder = returnedFile;
		// -------------------------------------------------------------------------
		Utilities.popToast (MusicPlayer.context.getString(R.string.root_folder_has_changed)+ PublicData.musicPlayerData.rootFolder);
		// -------------------------------------------------------------------------
		// 21/04/2015 ECU check whether in recovery mode
		// -------------------------------------------------------------------------
		if (recoveryMode)
		{
			// ---------------------------------------------------------------------
			// 21/04/2015 ECU in 'recovery mode' so take appropriate action
			// ---------------------------------------------------------------------
			// 21/04/2015 ECU switch off 'recovery mode'
			// ---------------------------------------------------------------------
			recoveryMode = false;
			// ---------------------------------------------------------------------
			// 21/04/2015 ECU tell the handler to do its work
			// ---------------------------------------------------------------------
			MusicPlayer.recoveryHandler.sleep (SHORT_DELAY);
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static void BrowseForRootFolder (int theFolderIndex)
	{
		// -------------------------------------------------------------------------
		// 09/08/2016 ECU Note - get the name of the file pinted to
		// -------------------------------------------------------------------------
		returnedFile = returnedFiles [theFolderIndex];
		// -------------------------------------------------------------------------
		// 09/08/2016 ECU get the File associated with this name
		// -------------------------------------------------------------------------
		returnedFileAsFile = new File (returnedFile);
		// -------------------------------------------------------------------------
		// 09/08/2016 ECU ensure that the selected file is a directory
		// -------------------------------------------------------------------------
		if (returnedFileAsFile.isDirectory())
		{
			// ---------------------------------------------------------------------
			// 09/08/2016 ECU Note - check if there is only a single file returned
			// ---------------------------------------------------------------------
			if (returnedFiles.length == 1)
			{
				// ----------------------------------------------------------------
				// 09/08/2016 ECU check if there are any sub directories in this directory
				// ----------------------------------------------------------------
				File[] listOfSubdirs = returnedFileAsFile.listFiles (new FileFilter ()		
				{
					// ------------------------------------------------------------
					// 10/08/2016 ECU filter out all files except directories
					// ------------------------------------------------------------
					@Override
					public boolean accept (File pathName) 
					{
						return pathName.isDirectory ();
					}
					// -------------------------------------------------------------
				});
				// -----------------------------------------------------------------
				// 09/08/2016 ECU check if there are any more folders
				// -----------------------------------------------------------------
				if (listOfSubdirs.length == 0)
				{
					// -------------------------------------------------------------
					// 07/04/2015 ECU this is the lowest level folder
					// 09/08/2016 ECU changed to use ...FileAsFile
					// -------------------------------------------------------------
					returnedFile = ((returnedFileAsFile.getParentFile()).getPath());
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (MusicPlayer.context.getString (R.string.no_more_folders));
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
			// 10/08/2016 ECU Note - get the list of sub directories that have music
			//                       files and get the list sorted
			// ---------------------------------------------------------------------
			returnedFiles = Utilities.returnSubDirectories (returnedFile,true,MUSIC_EXTENSION);
			// ---------------------------------------------------------------------
			// 10/08/2016 ECU check if any sub directories returned
			// ---------------------------------------------------------------------
			if (returnedFiles.length == 0)
			{
				// -----------------------------------------------------------------
				// 10/08/2016 ECU there are no sub directories so tell the user and
				//                go back to the parent
				// -----------------------------------------------------------------
				returnedFile = ((returnedFileAsFile.getParentFile()).getPath());
				returnedFiles = Utilities.returnSubDirectories (returnedFile,true,MUSIC_EXTENSION);
				// -----------------------------------------------------------------
				// 10/08/2016 ECU tell the user what is going on
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (MusicPlayer.context.getString (R.string.no_more_folders));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 10/08/2016 ECU there are more subfolders so carry on
			// ---------------------------------------------------------------------
			DialogueUtilities.listChoice (DialogueUtilities.context,
										  "Parent Directory is " + returnedFile,
										  returnedFiles,
										  Utilities.createAMethod (MusicPlayer.class,"BrowseForRootFolder",0),
										  "Accept '" + returnedFile + "' as the root",
										  Utilities.createAMethod (MusicPlayer.class,"AcceptRootFolder",0),
										  "Return to parent",
										  Utilities.createAMethod (MusicPlayer.class,"ParentRootFolder",0));
			// ---------------------------------------------------------------------
		}
	}
	// ============================================================================= 
	public static void Cancel (int theIndex)
	{
	}
	// =============================================================================
	public static void CancelArtist (int theArtist)
	{
		// -------------------------------------------------------------------------
		// 31/03/2015 ECU restore the saved playlist
		// 04/04/2015 ECU changed to use 'tracks'
		// 05/04/2015 ECU changed to use variable in 'musicPlayerData'
		// --------------------------------------------------------------------------
		PublicData.musicPlayerData.tracks = PublicData.musicPlayerData.savedTracks;
		// -------------------------------------------------------------------------
		// 31/03/2015 ECU now send the music to the remote device
		// -------------------------------------------------------------------------
		PublicData.musicPlayerData.trackNumber    = -1;      
		PublicData.mediaPlayer.stop();
		// -------------------------------------------------------------------------
		// 01/04/2015 ECU make sure the refresh handler is running
		// -------------------------------------------------------------------------
		musicRefreshHandler.sleep (SHORT_DELAY);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void CancelRemote (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 13/04/2015 ECU created to clear flags associated with remote music playing
		// -------------------------------------------------------------------------
		PublicData.musicPlayerRemote 	= false;
		remoteFirst 					= false;
		remoteMode						= false;
		// -------------------------------------------------------------------------
		// 16/04/2015 ECU try and get the correct actioning following the
		//                cancellation of the remote access
		// 17/04/2015 ECU removed
		// -------------------------------------------------------------------------
		//PublicData.mediaPlayer.stop ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void CancelRootFolder (int theFolderIndex)
	{
		// -------------------------------------------------------------------------
		// 06/04/2015 ECU created as cancel in the dialogue
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void CancelSearch (String theSearchString)
	{
		// -------------------------------------------------------------------------
		// 08/04/2015 ECU created as cancel in the dialogue
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void CancelSearch (SearchParameters theSearchParameters)
	{
	}
	// =============================================================================
	public static void ConfirmRemote (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 16/03/2015 ECU now send the music to the remote device
		// 02/05/2015 ECU want to check if the device is present and in the correct mode
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak (MusicPlayer.context.getString (R.string.trying_to_communicate),true,Toast.LENGTH_SHORT);
		// -------------------------------------------------------------------------
		DeviceStatusHandler deviceStatusHandler = new DeviceStatusHandler 
				(DialogueUtilities.context,
				 Utilities.createAMethod (MusicPlayer.class,"StatusSuccessMethod"),
				 Utilities.createAMethod (MusicPlayer.class,"StatusFailureMethod"));
		deviceStatusHandler.initiate (Devices.returnIPAddress (devices [theIndex]));
		// -------------------------------------------------------------------------
		// 10/02/2014 ECU try and remove the album art
		// -------------------------------------------------------------------------
		refreshGridCell (null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void ConfirmSearch (Object theSearchParameters)
	{
		// -------------------------------------------------------------------------
		// 08/04/2015 ECU created to use the string to selected tracks
		// 10/04/2015 ECU changed to use SearchParameters as the input argument
		// -------------------------------------------------------------------------
		// 10/04/2015 ECU get the search parameters that are to be used
		// -------------------------------------------------------------------------
		PublicData.musicPlayerData.tracks = searchForTracks (PublicData.musicPlayerData.savedTracks,
														((SearchParameters)theSearchParameters));
		// -------------------------------------------------------------------------
		if (PublicData.musicPlayerData.tracks.size() == 0)
		{
			// ---------------------------------------------------------------------
			// 08/04/2015 ECU nothing matched the search so reset the tracks and inform
			//                the user
			// ----------------------------------------------------------------------
			PublicData.musicPlayerData.tracks = PublicData.musicPlayerData.savedTracks;
			// ----------------------------------------------------------------------
			Utilities.popToastAndSpeak (MusicPlayer.context.getString (R.string.sorry_no_tracks));
		}
		// --------------------------------------------------------------------------
		DialogueUtilities.listChoice (DialogueUtilities.context,
									  DialogueUtilities.context.getString(R.string.select_track),
						              TrackDetails.returnStringArray (PublicData.musicPlayerData.tracks), 
						              Utilities.createAMethod(MusicPlayer.class,"SelectedTrack",0),
						              DialogueUtilities.context.getString (R.string.select_track_titles),
									  Utilities.createAMethod(MusicPlayer.class,"SearchTracks",0),
						              DialogueUtilities.context.getString(R.string.cancel_and_reset),
						              Utilities.createAMethod(MusicPlayer.class,"CancelSearch",0));
		// -------------------------------------------------------------------------
		
	}
	// =============================================================================
	public static void ParentRootFolder (int theFolderIndex)
 	{
 		// -------------------------------------------------------------------------
 		// 07/04/2015 ECU theFolderIndex is not relevant to this button but
 		//                returnedFile will be the subdirectory that was being viewed
 		// -------------------------------------------------------------------------
 		if (new File (returnedFile).isDirectory())
 		{
 			// ---------------------------------------------------------------------
 			// 07/04/2015 ECU get the parent of this folder
 			// ---------------------------------------------------------------------
 			File parentFile = (new File (returnedFile)).getParentFile();
 			// ---------------------------------------------------------------------
 			// 07/04/2015 ECU check if a parent exists
 			// ---------------------------------------------------------------------
 			if (parentFile != null)
 			{
 				returnedFile = parentFile.getPath();
 			
 				// ---------------------------------------------------------------------
 				returnedFiles = Utilities.returnSubDirectories (returnedFile,true,MUSIC_EXTENSION);
 				// ---------------------------------------------------------------------
 				DialogueUtilities.listChoice (DialogueUtilities.context,
 											  "Parent Directory is " + returnedFile,
 											  returnedFiles,
 											  Utilities.createAMethod (MusicPlayer.class,"BrowseForRootFolder",0),
 											  "Accept '" + returnedFile + "' as the root",
 											  Utilities.createAMethod (MusicPlayer.class,"AcceptRootFolder",0),
 											  "Return to parent",
 											  Utilities.createAMethod (MusicPlayer.class,"ParentRootFolder",0));
 				// -----------------------------------------------------------------
 			}
 			else
 			{
 				// -----------------------------------------------------------------
 				// 07/04/2015 ECU at the told of the tree
 				// -----------------------------------------------------------------
 				Utilities.popToastAndSpeak (MusicPlayer.context.getString(R.string.at_top_directory));
 				// -----------------------------------------------------------------
				// 06/04/2015 ECU change the root folder for the music
				// 07/04/2015 ECU changed to use listChoice
				// -----------------------------------------------------------------
				returnedFiles = Utilities.returnSubDirectories (StaticData.ROOT_DIRECTORY,true,MUSIC_EXTENSION);
				// -----------------------------------------------------------------
				DialogueUtilities.listChoice (DialogueUtilities.context,
											  DialogueUtilities.context.getString (R.string.title_root_folder),
											  returnedFiles,
											  Utilities.createAMethod (MusicPlayer.class,"BrowseForRootFolder",0),
											  DialogueUtilities.context.getString (R.string.cancel_this_operation),
											  Utilities.createAMethod (MusicPlayer.class,"CancelRootFolder",0));
				// -----------------------------------------------------------------
 			}
 		}
 	}
	// =============================================================================
	public static void SelectedArtist (int theArtist)
	{
		// -------------------------------------------------------------------------
		// 31/03/2015 ECU save the existing tracks but only once
		// 04/04/2015 ECU changed to use 'tracks'
		// -------------------------------------------------------------------------
		if (PublicData.musicPlayerData.savedTracks == null)
			PublicData.musicPlayerData.savedTracks = PublicData.musicPlayerData.tracks;
		// -------------------------------------------------------------------------
		// 31/03/2015 ECU now build the tracks for this artists
		// 04/04/2015 ECU changed to use 'tracks'
		// 05/04/2015 ECU changed to use the variable in 'musicPlayerData'
		// -------------------------------------------------------------------------
		PublicData.musicPlayerData.tracks = new ArrayList<TrackDetails> (); 

		for (int theTrack = 0; theTrack < PublicData.musicPlayerData.artists.get(theArtist).tracks.size(); theTrack++)
		{
			PublicData.musicPlayerData.tracks.add 
				(new TrackDetails (PublicData.musicPlayerData.savedTracks.get (PublicData.musicPlayerData.artists.get(theArtist).tracks.get (theTrack))));
		}
		// -------------------------------------------------------------------------
		// 05/04/2015 ECU sort the list of tracks
		// -------------------------------------------------------------------------
		Collections.sort (PublicData.musicPlayerData.tracks);
		// -------------------------------------------------------------------------
		// 16/03/2015 ECU now send the music to the remote device
		// -------------------------------------------------------------------------
		PublicData.musicPlayerData.trackNumber    = -1;               // used to indicate which entry is playing
		PublicData.mediaPlayer.stop (); 
		// -------------------------------------------------------------------------
		// 01/04/2015 ECU just check if there are enough tracks to make
		//                shuffle mode sensible - if set
		// -------------------------------------------------------------------------
		toggleShuffleMode (false);
		// -------------------------------------------------------------------------
		// 01/04/2015 ECU make sure the refresh handler is running
		// -------------------------------------------------------------------------
		musicRefreshHandler.sleep (SHORT_DELAY);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SelectedTrack (int theTrackIndex)
	{
		// -------------------------------------------------------------------------
		// 01/04/2015 ECU a track has been selected
		// -------------------------------------------------------------------------
		// 01/04/2015 ECU the following doesn't work if in shuffle mode so switch
		//                it off until have a solution
		// -------------------------------------------------------------------------
		// -------------------------------------------------------------------------
		// 01/04/2015 ECU set the required track - the '-1' is because the number 
		//                will be incremented before use
		// 04/04/2015 ECU changed from
		//					PublicData.musicPlayerData.tracks.get(theTrackIndex).trackNumber
		// -------------------------------------------------------------------------
		PublicData.musicPlayerData.trackNumber    = theTrackIndex - 1; 
		PublicData.mediaPlayer.stop (); 
		// -------------------------------------------------------------------------
		// 01/04/2015 ECU force shuffle mode off
		// -------------------------------------------------------------------------
		PublicData.musicPlayerData.shuffleMode = false;
		updateStatusIcons ();
		// -------------------------------------------------------------------------
		// 01/04/2015 ECU make sure the refresh handler is running
		// -------------------------------------------------------------------------
		musicRefreshHandler.sleep (SHORT_DELAY);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void StatusFailureMethod ()
	{
		// -------------------------------------------------------------------------
		// 02/05/2015 ECU unable to communicate with the remote device tell the user
		//                and then ignore the request
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak (MusicPlayer.context.getString (R.string.unable_to_communicate));
		// -------------------------------------------------------------------------
		// 02/05/2015 ECU cancel the request
		// -------------------------------------------------------------------------
		CancelRemote (0);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void StatusSuccessMethod ()
	{
		// -------------------------------------------------------------------------
		// 02/05/2015 ECU have obtained the status so check if the remote device is
		//                capable of playing music - it may already be playing for
		//                someone else
		// -------------------------------------------------------------------------
		if (!PublicData.receivedStatus.remoteMusicMode)
		{
			// ---------------------------------------------------------------------
			// 02/05/2015 ECU the remote device is capable of playing music
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (MusicPlayer.context.getString (R.string.remote_device_can_play_music),true,Toast.LENGTH_SHORT);
			// ---------------------------------------------------------------------
			// 07/05/2016 ECU indicate that remote mode is valid
			// ---------------------------------------------------------------------
			remoteMode = true;
			// ---------------------------------------------------------------------
			// 02/05/2015 ECU start up the remote music for the specified player
			// ---------------------------------------------------------------------
			startRemoteMusicPlayer (PublicData.receivedStatus.IPAddress);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/05/2015 ECU the remote device is not capable of playing music
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (MusicPlayer.context.getString (R.string.remote_device_cannot_play_music),true,Toast.LENGTH_SHORT);
			// ---------------------------------------------------------------------
			// 02/05/2015 ECU reset the mode
			// ---------------------------------------------------------------------
			CancelRemote (0);
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static void VolumeChange (int theSliderValue)
	{
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU set the volume to that returned
		// -------------------------------------------------------------------------
		PublicData.musicPlayerData.volume = theSliderValue;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

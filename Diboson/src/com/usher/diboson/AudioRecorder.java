package com.usher.diboson;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AudioRecorder extends DibosonActivity 
{
	// =============================================================================
	// 17/05/2013 ECU created
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 24/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 14/11/2016 ECU changed to use a random access file so that there is no
	//                need for a temporary file and gives the possibility of appending
	//                to an existing file
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	//==============================================================================
	/* ============================================================================= */
	//final static String TAG = "AudioRecorder";
	/* ============================================================================= */
	private static final int 		RECORDER_BPP 			= 16;  
    public  static final int 		RECORDER_SAMPLERATE 	= 44100;	// 27/04/2015 ECU changed to public
    private static final int 		RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final boolean	STEREO_MODE 			= false;
    // =============================================================================
    // 03/02/2015 ECU added WAV_FILE_FORMAT options
    // -----------------------------------------------------------------------------
    public  static final int		WAV_FILE_HEADER_SIZE	= 44;		// 02/02/2014 ECU added
    public  static final byte []    WAV_FILE_FORMAT         = {'W','A','V','E','f','m','t',' '};
    public  static final int        WAV_FILE_FORMAT_OFFSET	= 8;
	/* ============================================================================= */
    private static  boolean         appendToExistingFile;				// 14/11/2016 ECU added
	private static 	int 			bufferSize 				= 0;
	private static  Button			buttonPlay;							// 19/08/2016 ECU added
	private static	Context			context;							// 20/08/2016 ECU added
    private static 	String 			destinationAudioFolder;
    private static  RandomAccessFile	
    								destinationFile;					// 14/11/2016 ECU added
    private			TextView		elapsedTimeTextView;				// 20/08/2016 ECU added
    private 		boolean 		isRecording 			= false;
    private static 	String 			lastFileWritten 		= null;		// 19/08/2016 ECU changed from "" to null
    private static 	int    			numberOfChannels;
    private 		ProgressBar		progress_bar;
    private 		AudioRecord 	recorder 				= null;
    private static  int    			recorderChannels;
    private 		Thread 			recordingThread 		= null;
    private 		String 			specifiedFileToUse;					// 14/11/2016 ECU name changed from fileToUse
    private			MethodDefinition<?>	
    								stopMethodDefinition 	= null;		// 26/10/2016 ECU added
    /* =========================================================================== */  
    
    // ===========================================================================
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	// -------------------------------------------------------------------------
    	if (savedInstanceState == null)
    	{
    		// ---------------------------------------------------------------------
    		// 16/11/2016 ECU set up characteristics of the activity including
    		//                full screen
    		// ---------------------------------------------------------------------
    		Utilities.SetUpActivity (this,true);
    		// ---------------------------------------------------------------------
    		// 24/10/2015 ECU the activity has been created anew
    		// ---------------------------------------------------------------------
    		setContentView(R.layout.activity_audio_recorder);
    		// ---------------------------------------------------------------------
    		// 20/08/2016 ECU remember the context for later use
    		// ---------------------------------------------------------------------
    		context = this;
    		// ---------------------------------------------------------------------
    		// 20/08/2016 ECU reset any static variables
    		// ---------------------------------------------------------------------
    		appendToExistingFile	= false;
    		bufferSize 				= 0;
    		lastFileWritten			= null;
    		// ---------------------------------------------------------------------
    		// 19/08/2016 ECU set up any buttons
    		// ---------------------------------------------------------------------
    		buttonPlay = (Button) findViewById (R.id.btnFormat);
    		// ---------------------------------------------------------------------
    		// 19/08/2016 ECU initially set the play button to invisible
    		// ---------------------------------------------------------------------
    		buttonPlay.setVisibility (View.INVISIBLE);
    		// ---------------------------------------------------------------------
    		// 20/08/2016 ECU set up the elapsed timer text view
    		// ---------------------------------------------------------------------
    		elapsedTimeTextView = (TextView) findViewById (R.id.elapsed_time);
    		// ---------------------------------------------------------------------
    		// 11/12/2013 ECU set the buttons and enable them
    		// ---------------------------------------------------------------------
    		setButtonHandlers ();
    		enableButtons (false);
    		// ---------------------------------------------------------------------
    		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
       
    		progress_bar = (ProgressBar) findViewById (R.id.recording_progress);
    		// ---------------------------------------------------------------------
    		// 23/07/2013 ECU set up variables depending on whether in stereo or mono mode
    		// ---------------------------------------------------------------------
    		if (STEREO_MODE)
    		{
    			numberOfChannels = 2;
    			recorderChannels = AudioFormat.CHANNEL_IN_STEREO;
    		}
    		else
    		{
    			numberOfChannels = 1;
    			recorderChannels = AudioFormat.CHANNEL_IN_MONO;
    		}
    		// ---------------------------------------------------------------------     
    		bufferSize = AudioRecord.getMinBufferSize (RECORDER_SAMPLERATE,recorderChannels,RECORDER_AUDIO_ENCODING);
    		// ---------------------------------------------------------------------
    		// 11/12/2013 ECU set up the destination folder 
    		// 14/11/2016 ECU changed to use the resource
    		// ---------------------------------------------------------------------
    		destinationAudioFolder = PublicData.projectFolder + getString (R.string.audio_directory);
    		// ---------------------------------------------------------------------
    		// check if any arguments have been passed across 
    		// ---------------------------------------------------------------------
    		Bundle extras = getIntent ().getExtras ();
		
    		if (extras !=null) 
    		{
    			specifiedFileToUse = extras.getString (StaticData.PARAMETER_AUDIO_FILE_NAME);
		    
    			if (specifiedFileToUse != null)
    			{
    				// -------------------------------------------------------------
    				// 16/11/2016 ECU changed fro displaying file name in the title
    				//            ECU strip out the project bit of the file name
    				// -------------------------------------------------------------
    				((TextView) findViewById (R.id.file_being_recorded)).setText ("Recording to '" + Utilities.getRelativeFileName (specifiedFileToUse) + "'");
    				// -------------------------------------------------------------
    			}
    			// -----------------------------------------------------------------
    			// 26/10/2016 ECU check if there is a method to be called on 'stop'
    			// -----------------------------------------------------------------
    			stopMethodDefinition 
    				= (MethodDefinition<?>) extras.getSerializable (StaticData.PARAMETER_METHOD_DEFINITION);
    			// -----------------------------------------------------------------
    			// 14/11/2016 ECU add the append option to be able to add information
    			//                to an existing file
    			// -----------------------------------------------------------------
    			appendToExistingFile = extras.getBoolean (StaticData.PARAMETER_APPEND);
    			// ----------------------------------------------------------------- 
    			// 30/05/2013 ECU see if a start/stop command has been received
    			// 14/11/2016 ECU do not think that this bit is used any more
    			// -----------------------------------------------------------------
    			//String inputCommand = extras.getString ("Value");
    			//if (inputCommand != null)
    			//{
    			//	if (inputCommand.startsWith ("Start"))
    			//	{
    			//		startRecording ();
    			//	}
    			//	else
    			//	if (inputCommand.startsWith ("Stop"))
    			//	{
    			//		stopRecording ();
    			//	}
    			//} 
    			// -----------------------------------------------------------------
    		}
    		else
    		{
    			specifiedFileToUse = null;
    		}
    		// ---------------------------------------------------------------------
    		// 11/10/2013 ECU stop sound level monitoring
    		// 11/12/2013 ECU add the check if SECURITY_SERVICE
    		// 18/11/2014 ECU changed from using MainActivity.SECURITY_SERVICE
    		//            ECU changed to use monitorServiceRunning
    		// ---------------------------------------------------------------------
    		if (PublicData.monitorServiceRunning)
    			MonitorService.StopMonitoring ();	 
    		// ---------------------------------------------------------------------
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 24/10/2015 ECU the activity has been recreated after having been
    		//                destroyed by the Android OS
    		// ---------------------------------------------------------------------
    		finish (); 
    		// ---------------------------------------------------------------------
    	}
    }
    /* ============================================================================= */
    @Override
	public void onDestroy() 
	{
    	// -------------------------------------------------------------------------
    	// 11/10/2013 ECU restart the monitor
    	// 11/12/2013 ECU add the check if SECURITY_SERVICE
    	// 18/11/2014 ECU changed from using MainActivity.SECURITY_SERVICE
    	//     		  ECU changed to use monitorServiceRunning
		// -------------------------------------------------------------------------
    	if (PublicData.monitorServiceRunning)
    		MonitorService.StartMonitoring ();
    	// -------------------------------------------------------------------------
    	// 20/08/2016 ECU if recording then perform a stop
    	// -------------------------------------------------------------------------
    	if (isRecording)
    		stopRecording ();
        // -------------------------------------------------------------------------
		super.onDestroy();
	}
    /* ============================================================================= */
    private View.OnClickListener btnClick = new View.OnClickListener() 
    {
 	   	@Override
 	   	public void onClick(View view) 
 	   	{
 	   		switch(view.getId())
 	   		{
 		   		// -----------------------------------------------------------------
 		      	case R.id.btnStart:
 		      	{
 		      		// -------------------------------------------------------------
 		      		// 19/08/2016 ECU swap the next two lines around
 		      		// -------------------------------------------------------------
 		      		startRecording ();
 		      		enableButtons (true);
 		      		// -------------------------------------------------------------
 		      		// 20/08/2016 ECU make the play button invisible
 		      		// -------------------------------------------------------------
 		      		buttonPlay.setVisibility (View.INVISIBLE);
 		      		// -------------------------------------------------------------
 		      		// 20/08/2016 ECU start up the elapsed time display
 		      		// -------------------------------------------------------------
 		      		Utilities.elapsedTimeDisplay (elapsedTimeTextView,1000);
 		      		// -------------------------------------------------------------
 		      		break;
 		      	}
 		      	// -----------------------------------------------------------------
 		      	case R.id.btnStop:
 		      	{
 		      		// -------------------------------------------------------------
 		      		// 19/08/2016 ECU swap the next two lines around
 		      		// -------------------------------------------------------------
		      		stopRecording ();  
		      		// -------------------------------------------------------------
		      		enableButtons (false);
		      		// -------------------------------------------------------------
 		      		// 20/08/2016 ECU make the play button visible
 		      		// -------------------------------------------------------------
 		      		buttonPlay.setVisibility (View.VISIBLE);
 		      		// -------------------------------------------------------------
 		      		break;
 		      	}
 		      	// -----------------------------------------------------------------
 		      	case R.id.btnFormat:
 		      	{ 
 		      		PlayAFile (lastFileWritten);
 		      		break;
 		      	}
 		      	// -----------------------------------------------------------------
 	   		}		 
 	   	}
    };
    // =============================================================================
    public static void AudioFileNameCancel (String theFileName)
  	{
    	// -------------------------------------------------------------------------
     	// 14/11/2016 ECU the user has chosen to retain the default name of the
     	//                file so nothing needs to be done
     	// -------------------------------------------------------------------------
     	// 14/11/2016 ECU confirm the file name to the user
     	// -------------------------------------------------------------------------
     	confirmToUser (lastFileWritten);
     	// -------------------------------------------------------------------------
  	}
 	// =============================================================================
    public static void AudioFileNameConfirm (String theFileName)
  	{
    	// -------------------------------------------------------------------------
     	// 14/11/2016 ECU want to rename the file that was written
     	// -------------------------------------------------------------------------
     	File localFile = new File (lastFileWritten);
     	// -------------------------------------------------------------------------
     	// 14/11/2016 ECU now change the last file to the new name
     	// -------------------------------------------------------------------------
     	lastFileWritten = destinationAudioFolder + theFileName + StaticData.EXTENSION_AUDIO;
     	File newFile = new File (lastFileWritten);
     	// -------------------------------------------------------------------------
     	// 14/11/2016 ECU now do the rename action
     	// -------------------------------------------------------------------------
     	localFile.renameTo (newFile);
     	// -------------------------------------------------------------------------
     	// 14/11/2016 ECU confirm the file name to the user
     	// -------------------------------------------------------------------------
     	confirmToUser (lastFileWritten);
     	// -------------------------------------------------------------------------
  	}
    // ============================================================================
    static void confirmToUser (String theFileName)
    {
    	// --------------------------------------------------------------------------
  	   	// 14/11/2016 ECU close down the destination file
  	   	// --------------------------------------------------------------------------
  	   	// 11/12/2013 ECU confirm where the data has been written
  	   	// 11/04/2014 ECU change to use resource
  	   	// --------------------------------------------------------------------------
  	   	Utilities.popToast (context.getString (R.string.audio_data_written) + " \n" + theFileName,true);
  	   	// --------------------------------------------------------------------------
    }
    /* ============================================================================== */
    private void enableButton (int id,boolean isEnable)
    {
    	((Button)findViewById(id)).setEnabled (isEnable);
    	// -------------------------------------------------------------------------
    	// 19/08/2016 ECU decide if the button is to be visible or not
    	// ------------------------------------------------------------------------
    	((Button)findViewById(id)).setVisibility (isEnable ? View.VISIBLE : View.GONE);
    	// ------------------------------------------------------------------------
    }
    /* ============================================================================== */
    private void enableButtons (boolean isRecording) 
    {
    	// --------------------------------------------------------------------------
    	enableButton (R.id.btnStart,!isRecording);
    	enableButton (R.id.btnStop,isRecording);
    	// --------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public static byte [] GetWaveFileHeader (long totalAudioLength,
		   								     long totalDataLength, 
		   								     long sampleRate, 
		   								     int channels,
		   								     long byteRate)
    {
    	// -------------------------------------------------------------------------
    	//	The header of a WAV (RIFF) file is 44 bytes long and has the following format: 
    	//
    	//	 Positions 	 Sample Value 						 Description 
    	//    =========   ============                        ===========
    	//	   1 - 4  	"RIFF"  				Marks the file as a riff file. Characters are each 1 byte long.  
    	//	   5 - 8 	File size (integer)		Size of the overall file - 8 bytes, in bytes (32-bit integer). 
    	//										Typically, you'd fill this in after creation.  
    	//	   9 -12  	"WAVE" 					File Type Header. For our purposes, it always equals "WAVE".  
    	//	   13-16  	"fmt "  				Format chunk marker. Includes trailing null  
    	//	   17-20  	16 						Length of format data as listed above  
    	//	   21-22  	1  						Type of format (1 is PCM) - 2 byte integer  
    	//	   23-24  	2  						Number of Channels - 2 byte integer  
    	//	   25-28  	44100  					Sample Rate - 32 byte integer. Common values are 44100 (CD), 
    	//										48000 (DAT). Sample Rate = Number of Samples per second, or Herz.  
    	//	   29-32 	176400  				(Sample Rate * BitsPerSample * Channels) / 8.  
    	//	   33-34 	4  						(BitsPerSample * Channels) / 8.1 - 8 bit mono2 - 8 bit stereo/16
    	//										bit mono4 - 16 bit stereo  
    	//	   35-36  	16  					Bits per sample  
    	//	   37-40  	"data"  				"data" chunk header. Marks the beginning of the data section.  
    	//	   41-44 	File size (data)  		Size of the data section.  
    	//	   Sample values are given above for a 16-bit stereo source.  
    	// -------------------------------------------------------------------------

    	// -------------------------------------------------------------------------
    	// 25/11/2014 ECU allocate a byte array of correct size for the header
    	// -------------------------------------------------------------------------
    	byte[] header = InitialiseWaveFileHeader (); 
    	// ========================================================================= 
    	// -------------------------------------------------------------------------
    	// 25/11/2014 ECU set the elements in the header
    	// -------------------------------------------------------------------------
    	header [0] 	= 'R';  						// RIFF/WAVE header
    	header [1] 	= 'I';
    	header [2]	= 'F';
    	header [3] 	= 'F';
    	// -------------------------------------------------------------------------
    	header [4] 	= (byte)  (totalDataLength & 0xff);
    	header [5] 	= (byte) ((totalDataLength >> 8) & 0xff);
    	header [6] 	= (byte) ((totalDataLength >> 16) & 0xff);
    	header [7]	= (byte) ((totalDataLength >> 24) & 0xff);
    	// -------------------------------------------------------------------------
    	// 03/02/2015 ECU changed the code to use the WAV_FILE_... variables instead
    	//                of the following because I want to use those variables by
    	//                other methods
    	// -------------------------------------------------------------------------
    	//header [8] 	= 'W';
    	//header [9] 	= 'A';
    	//header [10] 	= 'V';
    	//header [11] 	= 'E';
    	// -------------------------------------------------------------------------
    	//header [12] 	= 'f';  						// 'fmt ' chunk
    	//header [13] 	= 'm';
    	//header [14] 	= 't';
    	//header [15] 	= ' ';
    	// -------------------------------------------------------------------------
    	System.arraycopy (WAV_FILE_FORMAT,0,header,WAV_FILE_FORMAT_OFFSET,WAV_FILE_FORMAT.length);
    	// -------------------------------------------------------------------------
    	header [16] 	= 16;  							// 4 bytes: size of 'fmt ' chunk
    	header [17] 	= 0;
    	header [18] 	= 0;
    	header [19] 	= 0;
    	// -------------------------------------------------------------------------
    	header [20]		= 1;  							// format = 1 (PCM)
    	header [21] 	= 0;
    	// -------------------------------------------------------------------------
    	header [22] 	= (byte) channels;
    	header [23] 	= 0;
    	// -------------------------------------------------------------------------
    	header [24] 	= (byte)  (sampleRate & 0xff);
    	header [25] 	= (byte) ((sampleRate >> 8) & 0xff);
    	header [26] 	= (byte) ((sampleRate >> 16) & 0xff);
    	header [27] 	= (byte) ((sampleRate >> 24) & 0xff);
    	// -------------------------------------------------------------------------
    	header [28] 	= (byte)  (byteRate & 0xff);
    	header [29] 	= (byte) ((byteRate >> 8) & 0xff);
    	header [30] 	= (byte) ((byteRate >> 16) & 0xff);
    	header [31] 	= (byte) ((byteRate >> 24) & 0xff);
    	// -------------------------------------------------------------------------
    	header [32] 	= (byte) (2 * 16 / 8);  		// block align
    	header [33] 	= 0;
    	// -------------------------------------------------------------------------
    	header [34] 	= RECORDER_BPP;  				// bits per sample
    	header [35] 	= 0;
    	// -------------------------------------------------------------------------
    	header [36] 	= 'd';
    	header [37] 	= 'a';
    	header [38] 	= 't';
    	header [39] 	= 'a';
    	// -------------------------------------------------------------------------
    	header [40] 	= (byte)  (totalAudioLength & 0xff);
    	header [41] 	= (byte) ((totalAudioLength >> 8) & 0xff);
    	header [42] 	= (byte) ((totalAudioLength >> 16) & 0xff);
    	header [43] 	= (byte) ((totalAudioLength >> 24) & 0xff);
    	// -------------------------------------------------------------------------
    	// 25/11/2014 ECU return the header, suitably updated
    	// -------------------------------------------------------------------------
    	return header;
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static byte [] InitialiseWaveFileHeader ()
    {
    	// -------------------------------------------------------------------------
    	// 25/11/2014 ECU method just initialise a byte array of the correct sized
    	//                for the '.wav' file header
    	// 02/02/2015 ECU changed to use WAV_... instead of 44
    	// -------------------------------------------------------------------------
    	return (new byte [WAV_FILE_HEADER_SIZE]);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    private void PlayAFile (String theFileName)
    {
    	MediaPlayer mediaPlayer = MediaPlayer.create(this, Uri.fromFile (new File (theFileName)));
    	// -------------------------------------------------------------------------
    	// 16/09/2013 ECU check if media player created correctly
    	// -------------------------------------------------------------------------
    	if (mediaPlayer != null)
    		mediaPlayer.start();
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    private void setButtonHandlers () 
    {
    	((Button) findViewById(R.id.btnStart)).setOnClickListener (btnClick);
	   	((Button) findViewById(R.id.btnStop)).setOnClickListener (btnClick);
	   	((Button) findViewById(R.id.btnFormat)).setOnClickListener (btnClick);
    }
    /* ============================================================================= */
    private static void setWavHeaderAndClose ()
    {
	   	// -------------------------------------------------------------------------
   		// 14/11/2016 ECU set the header in the destination file
   		// -------------------------------------------------------------------------
   		long totalAudioLen;
   		long totalDataLen;
   		// -------------------------------------------------------------------------
   		try 
   		{
   			// ---------------------------------------------------------------------
   			// 25/11/2014 ECU open up to the streams
   			// 14/11/2016 ECU use the size of the file to generate the wav header
   			// ---------------------------------------------------------------------
   			totalAudioLen 	= destinationFile.length() - WAV_FILE_HEADER_SIZE;
   			totalDataLen 	= totalAudioLen + (WAV_FILE_HEADER_SIZE - WAV_FILE_FORMAT_OFFSET);
   			// ---------------------------------------------------------------------
   			// 14/11/2016 ECU write the header to the start of the destination file
   			// ---------------------------------------------------------------------
   			destinationFile.seek (0);
   			destinationFile.write (GetWaveFileHeader (totalAudioLen, 
   													  totalDataLen,
   													  (long) RECORDER_SAMPLERATE, 
   													  numberOfChannels, 
   													  (long) (RECORDER_BPP * RECORDER_SAMPLERATE * numberOfChannels/8)));
   			// ---------------------------------------------------------------------
   			// 14/11/2016 ECU can now close the file
   			// ---------------------------------------------------------------------
   			destinationFile.close ();
   			// ---------------------------------------------------------------------
   		}
   		catch (Exception theException) 
   		{
   			theException.printStackTrace();
   		} 
    }
    /* ============================================================================= */   
    private void startRecording ()
    {
    	// -------------------------------------------------------------------------
 	   	// 14/11/2016 ECU set up the destination file
 	   	// -------------------------------------------------------------------------
 	   	try
 	   	{
 	   		// ---------------------------------------------------------------------
 	   		// 14/11/2016 ECU check whether a file name has been supplied
 	   		// ---------------------------------------------------------------------
 	   		if (specifiedFileToUse == null)
 	   		{
 	   			// -----------------------------------------------------------------
 	   			// 14/11/2016 ECU generate a unique file name based on the current
 	   			//                time
 	   			// -----------------------------------------------------------------
 	   			lastFileWritten = destinationAudioFolder + String.valueOf (System.currentTimeMillis()) + StaticData.EXTENSION_AUDIO;
 	   			// ------------------------------------------------------------------
 	   		}
 	   		else
 	   		{
 	   			// -----------------------------------------------------------------
 	   			// 14/11/2016 ECU take the supplied name 
 	   			// -----------------------------------------------------------------
 	   			lastFileWritten = specifiedFileToUse;
 	   			// -----------------------------------------------------------------
 	   		}
 	   		// ---------------------------------------------------------------------
 	   		// 15/11/2016 ECU because the new Ran... will create the file if it
 	   		//                does not exist then remember whether the file already
 	   		//                exists
 	   		// ---------------------------------------------------------------------
 	   		boolean localExists = new File (lastFileWritten).exists();
 	   		// ---------------------------------------------------------------------
 	   		// 14/11/2016 ECU use the specified file as the destination
 	   		// ---------------------------------------------------------------------
 	   		destinationFile = new RandomAccessFile (lastFileWritten,"rw");
 	   		// ---------------------------------------------------------------------
 	   		// 14/11/2016 ECU check if the file already exists
 	   		// 15/11/2016 ECU change to check the variable - see note above
 	   		// ---------------------------------------------------------------------
 	   		if (localExists)
 	   		{
 	   			// -----------------------------------------------------------------
 	   			// 14/11/2016 ECU the specified already exists so want the option to
 	   			//                append or overwrite
 	   			//            ECU decide whether to overwrite or append to the file
 	   			// -----------------------------------------------------------------
 	   			if (appendToExistingFile)
 	   			{
 	   				// -------------------------------------------------------------
 	   				// 14/11/2016 ECU the new data will be recorded at the end of
 	   				//                the file
 	   				
 	   				//            ECU the WAV header will already be written and this
 	   				//                will be followed by the data - position beyond 
 	   				//                this
 	   				// -------------------------------------------------------------
 	   				destinationFile.seek (destinationFile.length ());
 	   				// -------------------------------------------------------------
 	   				// 15/11/2016 ECU play a message which will end up on the note
 	   				// -------------------------------------------------------------
 	   				Utilities.SpeakAPhrase (context,context.getString(R.string.audio_recorder_message) + 
 	   						Utilities.getAdjustedTime (new SimpleDateFormat (PublicData.dateFormatDDMMYYYY + " 'at' HH:mm",Locale.getDefault())));
 	   				// -------------------------------------------------------------
 	   			}
 	   			else
 	   			{
 	   				// -------------------------------------------------------------
 	   				// 14/11/2016 ECU the existing data is to be overwritten
 	   				//            ECU just position after the existing WAV header
 	   				// -------------------------------------------------------------
 	   				destinationFile.seek (WAV_FILE_HEADER_SIZE);
 	   				// -------------------------------------------------------------
 	   			}
 	   		}
 	   		else
 	   		{
 	   			// -----------------------------------------------------------------
 	   			// 14/11/2016 ECU the specified file does not exist so start from the
 	   			//                just write the header
 	   			// -----------------------------------------------------------------
 	 	   		// 14/11/2016 ECU write a dummy header at the start of the file
 	 	   		//                which will contain the wav data
 	 	   		// -----------------------------------------------------------------
 	 	   		destinationFile.write (new byte [WAV_FILE_HEADER_SIZE]);
 	 	   		// -----------------------------------------------------------------
 	   		}
 	   	}
 	  	catch (Exception theException)
 	   	{
 		   
 	   	}
	   	// -------------------------------------------------------------------------
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
             RECORDER_SAMPLERATE, recorderChannels,RECORDER_AUDIO_ENCODING, bufferSize);
        // -------------------------------------------------------------------------
        progress_bar.setVisibility (View.VISIBLE);
        recorder.startRecording ();

        isRecording = true;
        // -------------------------------------------------------------------------
        // 18/05/2013 ECU declare and start the thread that will write the data to
        //                file
        // -------------------------------------------------------------------------
        recordingThread = new Thread(new Runnable() 
        {
        	@Override
        	public void run() 
        	{
        		writeAudioDataToFile();
        	}
        },"AudioRecorder Thread");
        // -------------------------------------------------------------------------
        // 19/08/2016 ECU Note -start up the thread
        // -------------------------------------------------------------------------
        recordingThread.start (); 
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    private void stopRecording()
    {
    	// -------------------------------------------------------------------------
    	// 20/08/2016 ECU remove the elapsed time display
    	// -------------------------------------------------------------------------
    	Utilities.elapsedTimeDisplay (elapsedTimeTextView,StaticData.NO_RESULT);
    	// -------------------------------------------------------------------------
    	// 19/08/2016 ECU Note - check that there is a recorder running
    	// -------------------------------------------------------------------------
    	if (recorder != null)
    	{
    		// ---------------------------------------------------------------------
    		// 19/08/2016 ECU Note - remove the progress bar and then stop and release
    		//                the resources associated with the recorder
    		// ---------------------------------------------------------------------
    		progress_bar.setVisibility (View.INVISIBLE);
    		isRecording 		= false;
    		recorder.stop ();
    		recorder.release ();
    		recorder 			= null;
    		recordingThread 	= null;
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    	// 14/11/2016 ECU update the header in the file and then close
   		//                the file
   		// -------------------------------------------------------------
   		setWavHeaderAndClose ();
    	// -------------------------------------------------------------------------
    	// 19/08/2016 ECU Note - sort out the name of the file that will receive the
    	//                recorder data
    	// -------------------------------------------------------------------------
    	if (specifiedFileToUse != null)
    	{
    		// ---------------------------------------------------------------------
    		// 20/08/2016 ECU write the audio data to the specified file
    		// ---------------------------------------------------------------------
    		specifiedFileToUse = null;
    		// ---------------------------------------------------------------------
    		// 26/10/2016 ECU check if there is a stop method to be called
    		// ---------------------------------------------------------------------
    		if (stopMethodDefinition != null)
    		{
    			// -----------------------------------------------------------------
				// 26/10/2016 ECU create a method from the definition
				// -----------------------------------------------------------------
				Method localMethod = stopMethodDefinition.ReturnMethod (); 
				// -----------------------------------------------------------------
				// 26/10/2016 ECU if a valid method has been defined then invoke
				//                it
				// -----------------------------------------------------------------
				try 
				{
					// -------------------------------------------------------------
					// 26/10/2016 ECU call the method that handles item selection
					// -------------------------------------------------------------
					localMethod.invoke (null);
					// -------------------------------------------------------------
				} 
				catch (Exception theException) 
				{		
				} 
				// -----------------------------------------------------------------
    		}
    		// ---------------------------------------------------------------------
    		// 20/08/2016 ECU finish this activity
    		// ---------------------------------------------------------------------
    		finish ();
    		// ---------------------------------------------------------------------
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 20/08/2016 ECU request the name of the file which will receive the data
    		// ---------------------------------------------------------------------
    		DialogueUtilities.textInput (context,
    									 context.getString (R.string.audio_file_name_title),
				   						 context.getString (R.string.audio_file_name_summary),
				   						 StaticData.HINT + context.getString (R.string.audio_file_name_hint),
				   						 Utilities.createAMethod (AudioRecorder.class,"AudioFileNameConfirm",""),
				   						 Utilities.createAMethod (AudioRecorder.class,"AudioFileNameCancel","")); 
			// ---------------------------------------------------------------------
    	} 	
    	// -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    private void writeAudioDataToFile ()
    {
    	// -------------------------------------------------------------------------
    	// 19/08/2016 ECU Note - read the audio data and write it to the temporary file
    	// 14/11/2016 ECU changed to write to the random access file
    	// -------------------------------------------------------------------------
    	byte data[] = new byte[bufferSize];
    	int read = 0;
    	// -------------------------------------------------------------------------
    	try 
    	{
    		// ---------------------------------------------------------------------
    		// 19/08/2016 ECU Note - keep looping while the device is 'recording'
    		// ---------------------------------------------------------------------
    		while (isRecording)
    		{
    			// -----------------------------------------------------------------
    			// 19/08/2016 ECU Note - read in a buffer of 'audio data'
    			// -----------------------------------------------------------------
    			read = recorder.read (data, 0, bufferSize);
    			// -----------------------------------------------------------------
    			// 19/08/2016 ECU Note - if there was no error then write the data
    			//                out to temporary file
    			// -----------------------------------------------------------------
    			if (read != AudioRecord.ERROR_INVALID_OPERATION)
    			{
    				// -------------------------------------------------------------
    				// 14/11/2016 ECU write the data to the destination file
    				// -------------------------------------------------------------
    				destinationFile.write (data);
    				// -------------------------------------------------------------
    			}
    			// -----------------------------------------------------------------
    		}
       	} 	
       	catch (Exception theException)
       	{
       	}
    }
    // =============================================================================
} 	// End of Class

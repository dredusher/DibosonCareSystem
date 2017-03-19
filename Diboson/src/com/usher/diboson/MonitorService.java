package com.usher.diboson;

import java.io.RandomAccessFile;
import java.util.Calendar;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.widget.ImageView;

@SuppressLint("DefaultLocale")
public class MonitorService extends Service
{
	/* ============================================================================= */
	// 11/10/2013 ECU created to handle any background security
	//				  issues
	/* ============================================================================= */
	final static String TAG		=	"MonitorService";
	/* ============================================================================= */
	final static int 		BUFFER_MULTIPLIER 		= 5;
	final static int    	INACTIVITY_WAIT_TIME	= 1000 * 10;			// milliseconds
	final static int		SAMPLE_NUMBER 			= 1024;
	final static int		SAMPLE_RATE 			= 8000;
	final static float		RESOLUTION 				= (float) (SAMPLE_RATE * 2) / (float) SAMPLE_NUMBER;
	/* ============================================================================= */
	static  Context			context;										// 25/11/2014 ECU added
	static  int             fileCounter         	= 0;					// 25/11/2014 ECU added
	static  int             iconID					= R.drawable.audio_analyser;
																			// 28/04/2015 ECU added
	static 	boolean 		keepRunning 			= true;
	static  String          noisesDirectory;								// 30/04/2015 ECU added
	static 	Thread 			soundMonitorThread		= null;					// 29/04/2015 ECU added
	static 	Thread 			timedRecordingThread	= null;					// 28/04/2015 ECU added
	/* ============================================================================= */
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}
	/* ============================================================================= */
	@Override
	public void onCreate()
	{
		// -------------------------------------------------------------------------
		super.onCreate();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public int onStartCommand (Intent intent,int flags, int startId) 
	{
		// -------------------------------------------------------------------------
		// 23/11/2014 ECU changed from onStart and return with the appropriate
		//                STICKY bit to try and handle issues when the device is
		//                in sleep mode
		// -------------------------------------------------------------------------
		// 18/11/2014 ECU indicate that the service is up and running
		// -------------------------------------------------------------------------
		PublicData.monitorServiceRunning = true;
		// -------------------------------------------------------------------------
		// 25/11/2014 ECU remember the context
		// -------------------------------------------------------------------------
		context = this;
		// -------------------------------------------------------------------------
		// 20/11/2014 ECU log the fact that the service has started
		// 25/11/2014 ECU print out the monitoring data
		// -------------------------------------------------------------------------
		Utilities.LogToProjectFile (TAG, "onStartCommand\n" +
										PublicData.storedData.monitor.print());
		// -------------------------------------------------------------------------
		// 30/04/2015 ECU set up the destination directory - the string contains the
		//                terminating '/'
		// -------------------------------------------------------------------------
		noisesDirectory = PublicData.projectFolder + getString (R.string.noises_directory);
		// -------------------------------------------------------------------------
		// 23/11/2014 ECU tell the securityHandler to keep running until told
		//                to stop
		// -------------------------------------------------------------------------
		keepRunning = true;
		// -------------------------------------------------------------------------
		// 28/04/2015 ECU update the icon
		// -------------------------------------------------------------------------
		updateImage (R.drawable.listen);
		// -------------------------------------------------------------------------
		// 11/10/2013 ECU start the sound monitor
		// 17/10/2013 ECU add the context
		// -------------------------------------------------------------------------
		StartMonitoring ();
		// -------------------------------------------------------------------------
		return Service.START_STICKY;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public void onDestroy() 
	{
		// -------------------------------------------------------------------------
		// 11/10/2013 ECU stop the refresh handler
		// -------------------------------------------------------------------------
		keepRunning = false;
		// -------------------------------------------------------------------------
		// 28/04/2015 ECU try and stop any recording if happening
		// -------------------------------------------------------------------------
		if (timedRecordingThread != null)
			timedRecordingThread.interrupt();
		// -------------------------------------------------------------------------
		// 29/04/2015 ECU try and stop any monitoring if happening
		// -------------------------------------------------------------------------
		if (soundMonitorThread != null)
			soundMonitorThread.interrupt();
		// -------------------------------------------------------------------------
		// 11/10/2013 ECU stop monitoring the sound
		// -------------------------------------------------------------------------
		StopMonitoring ();
		// -------------------------------------------------------------------------
		// 18/11/2014 ECU indicate that the service is no longer running
		// -------------------------------------------------------------------------
		PublicData.monitorServiceRunning = false;
		// -------------------------------------------------------------------------
		// 20/11/2014 ECU log the fact that the service has been destroyed
		// -------------------------------------------------------------------------
		Utilities.LogToProjectFile (TAG, "onDestroy");
		// -------------------------------------------------------------------------
		// 28/04/2015 ECU update the icon
		// -------------------------------------------------------------------------
		updateImage (R.drawable.audio_analyser);
		// -------------------------------------------------------------------------
		super.onDestroy();
	}
	// =============================================================================
	static boolean checkForANoise (int theNoiseThreshold)
	{
		// -------------------------------------------------------------------------
		// 29/04/2015 ECU created to wait until the noise threshold is reached
		// -------------------------------------------------------------------------
		// 29/04/2015 ECU declare any local data
		// -------------------------------------------------------------------------
		short[] 	localBuffer					= null;
		double		localNoiseLevelFound;
		int			localNumberOfShortsRead;
		// -------------------------------------------------------------------------
		// 29/04/2015 ECU work out the minimum size of buffer needed
		// -------------------------------------------------------------------------
		final int localMinimumBufferSize 
			= AudioRecord.getMinBufferSize (SAMPLE_RATE,
											AudioFormat.CHANNEL_IN_MONO, 
											AudioFormat.ENCODING_PCM_16BIT) * BUFFER_MULTIPLIER;
		// -------------------------------------------------------------------------
		// 29/04/2015 ECU create the audio record for receiving the data
		// -------------------------------------------------------------------------
		AudioRecord localAudioRecord 
			= new AudioRecord (MediaRecorder.AudioSource.MIC, 
							   SAMPLE_RATE,
							   AudioFormat.CHANNEL_IN_MONO,
							   AudioFormat.ENCODING_PCM_16BIT,
							   localMinimumBufferSize);
		// -------------------------------------------------------------------------
		// 29/04/2015 ECU if the record failed to initialise then reflect the fact
		// -------------------------------------------------------------------------
		if (localAudioRecord.getState() != AudioRecord.STATE_INITIALIZED)
			return false;
		// -------------------------------------------------------------------------
		// 29/04/2015 ECU declare a buffer to receive the data
		// -------------------------------------------------------------------------
		localBuffer = new short [localMinimumBufferSize];
		// -------------------------------------------------------------------------
		// 29/04/2015 ECU start recording
		// -------------------------------------------------------------------------
		localAudioRecord.startRecording();
		// -------------------------------------------------------------------------
		// 29/04/2015 ECU loop until a noise is heard
		// -------------------------------------------------------------------------
		while (keepRunning)
		{
			try
			{
				// -----------------------------------------------------------------
				// 29/04/2015 ECU read in a buffer of data
				// -----------------------------------------------------------------
				localNumberOfShortsRead = localAudioRecord.read (localBuffer, 0, localMinimumBufferSize);
				// -----------------------------------------------------------------
				// 29/04/2015 ECU check if an error occurred
				// -----------------------------------------------------------------
				if (localNumberOfShortsRead == AudioRecord.ERROR_INVALID_OPERATION)
					return false;
				// -----------------------------------------------------------------
				// 29/04/2015 ECU scan through the buffer to check if a level is above
				//                the threshold
				// -----------------------------------------------------------------
				for (int theIndex=0; theIndex < localNumberOfShortsRead; theIndex++) 
				{
					// -------------------------------------------------------------
					// 29/04/2015 ECU get the current noise level in this buffer
					// -------------------------------------------------------------
					localNoiseLevelFound = Math.abs (localBuffer [theIndex]);
					// -------------------------------------------------------------
					// 18/11/2014 ECU changed from NOISE_LEVEL to ... triggerLevel
					// -------------------------------------------------------------
					if (localNoiseLevelFound > theNoiseThreshold)
					{
						// ---------------------------------------------------------
						// 29/04/2015 ECU threshold reached so stop recording and
						//                release resources
						// ---------------------------------------------------------
						localAudioRecord.stop();
						localAudioRecord.release();
						// ---------------------------------------------------------
						// 29/04/2015 ECU return to indicate threshold hit
						// ---------------------------------------------------------
						return true;
					}
					// -------------------------------------------------------------
				}	
			}
			catch (Exception theException)
			{
				// -----------------------------------------------------------------
				// 29/04/2015 ECU a problem occurred so reflect this fact
				// -----------------------------------------------------------------
				return false;
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 29/04/2015 the looped has been stopped
		// -------------------------------------------------------------------------
		localAudioRecord.stop();
		localAudioRecord.release();
		// -------------------------------------------------------------------------
		// 2904/2015 ECU indicate that noise not found just an abnormal exception
		// -------------------------------------------------------------------------
		return false;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void SoundMonitor ()
	{
		// -------------------------------------------------------------------------
		// 11/10/2013 ECU this method checks for noise on the microphone
		// 29/04/2015 ECU do a rewrite because did not like what was there
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG, "Sound Monitor started");
			// ---------------------------------------------------------------------
			// 11/10/2013 ECU now declare a thread to process the data
			// ---------------------------------------------------------------------
			soundMonitorThread = new Thread()
			{
				@Override
				public void run()
				{
					// -------------------------------------------------------------
					// 29/04/2015 ECU wait for the inactive period to expire
					// -------------------------------------------------------------
					if (PublicData.monitorInactivityCounter > 0)
					{
						// ---------------------------------------------------------
						// 28/04/2015 ECU still in a period of inactivity
						//                so reflect this fact
						// ---------------------------------------------------------
						// 29/04/2015 ECU update the icon
						// ---------------------------------------------------------
						updateImage (R.drawable.listen_inactive);
						// ---------------------------------------------------------
						// 29/04/2015 ECU now wait until the inactivity period
						//                has expired
						//            ECU added the interrupted check
						// ---------------------------------------------------------
						while (keepRunning && PublicData.monitorInactivityCounter > 0)
						{
							// -----------------------------------------------------
							// 28/04/2015 ECU still in a period of inactivity
							//                so reflect this fact
							// -----------------------------------------------------
							try 
							{
								sleep (INACTIVITY_WAIT_TIME);
							}
							catch (InterruptedException theException) 
							{
							
							}
						// ---------------------------------------------------------
						}
					}
					// -------------------------------------------------------------
					// 21/11/2014 ECU check if in an inactive period
					// 28/04/2015 ECU changed to use the counter in MainActivity
					// 29/04/2015 ECU added the interrupted check
					// -------------------------------------------------------------
					if (keepRunning && PublicData.monitorInactivityCounter == 0)
					{	
						// ---------------------------------------------------------
						// 28/04/2015 ECU update the icon to indicate 
						//                actively listening
						// ---------------------------------------------------------
						updateImage (R.drawable.listen_listening);
						// ---------------------------------------------------------
						if (checkForANoise (PublicData.storedData.monitor.triggerLevel))
						{
							// -----------------------------------------------------
							// 29/04/2015 ECU noise has been detected
							// -----------------------------------------------------
							if (PublicData.storedData.monitor.timedRecording)
							{
								// -------------------------------------------------
								// 29/04/2015 ECU start up the timed recording if
								//                requested
								// -------------------------------------------------
								timedRecording (String.format("%s_%04d%s",
										context.getString(R.string.noise_file),
										fileCounter++,
										context.getString(R.string.audio_file_extension)));
								// -------------------------------------------------
							}
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
				}
				
			};
			// ---------------------------------------------------------------------
			// 11/10/2013 ECU start up the thread
			// 29/04/2015 ECU changed the name
			// ---------------------------------------------------------------------
			soundMonitorThread.start();
			// ---------------------------------------------------------------------	
		}
		catch (Exception theException)
		{ 
			// ---------------------------------------------------------------------
			// 23/11/2014 ECU want to log exception while checking what
			//                happens when device is 'sleeping'
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG,"SoundMonitor " + theException);
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	public static void StartMonitoring ()
	{
		// -------------------------------------------------------------------------
		// 11/10/2013 ECU start monitoring after a short delay
		// -------------------------------------------------------------------------
		SoundMonitor ();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void StopMonitoring ()
	{
		// -------------------------------------------------------------------------
		// 11/10/2013 ECU indicate that the thread must stop
		// -------------------------------------------------------------------------
		Utilities.LogToProjectFile (TAG, "Sound Monitor stopped");
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	
	// =============================================================================
    public static void timedRecording (final String theFileName)
    { 	
    	timedRecordingThread = new Thread()
    	{
    		@Override
    		public void run()
    		{
    			try 
    			{ 
    				// -------------------------------------------------------------
    				// 30/04/2015 ECU change to use 'noisesDirectory' instead of
    				//                the project folder
    				// -------------------------------------------------------------
    				RandomAccessFile randomAccessFile 
    					= new RandomAccessFile (noisesDirectory + theFileName,"rw");
    				// ------------------------------------------------------------
    				// 25/11/2014 ECU the file may already exists so reset its 
    				//                length
    				// ------------------------------------------------------------
    				randomAccessFile.setLength (0);
    				// ------------------------------------------------------------
    				// 25/11/2014 ECU initialise space in the file that will 
    				//                receive the '.wav' file header
    				// ------------------------------------------------------------
    				randomAccessFile.write (AudioRecorder.InitialiseWaveFileHeader());
    				// -------------------------------------------------------------		
    				final int minimumBufferSize 
							= AudioRecord.getMinBufferSize (AudioRecorder.RECORDER_SAMPLERATE,
															AudioFormat.CHANNEL_IN_MONO,
															AudioFormat.ENCODING_PCM_16BIT);
    				// -------------------------------------------------------------
    				// 25/11/2014 ECU create the audio record that will be used
    				//                for the recording
    				// 27/04/2015 ECU changed to use RECORDE_SAMPLERATE
    				// -------------------------------------------------------------
    				AudioRecord localAudioRecord 
							= new AudioRecord (MediaRecorder.AudioSource.MIC, 
											   AudioRecorder.RECORDER_SAMPLERATE,
											   AudioFormat.CHANNEL_IN_MONO,
											   AudioFormat.ENCODING_PCM_16BIT,
											   minimumBufferSize);
    				// -------------------------------------------------------------
    				// 27/04/2015 ECU check if the audio record initialised
    				//                successfully
    				// -------------------------------------------------------------
    				if (localAudioRecord.getState() == AudioRecord.STATE_INITIALIZED)
    				{
    					// -------------------------------------------------------------------------
    					// 28/04/2015 ECU update the icon
    					// -------------------------------------------------------------------------
    					updateImage (R.drawable.listen_recording);
    					// ---------------------------------------------------------
    					// 25/11/2014 ECU create the buffer to receive the audio
    					// ---------------------------------------------------------
    					byte [] bytes = new byte [minimumBufferSize];
						
    					Calendar calendar 	= Calendar.getInstance();
    					long startTime 		= calendar.getTimeInMillis();
    					long currentTime 	= startTime;
    					// ---------------------------------------------------------
    					// 11/10/2013 ECU start the recording process
    					// ---------------------------------------------------------
    					localAudioRecord.startRecording();
						// ---------------------------------------------------------	
    					Utilities.LogToProjectFile (TAG, "starting " + theFileName);
    					// ---------------------------------------------------------
    					// 28/04/2015 ECU put in the interrupted check
    					// 30/04/2015 ECU changed to check keepRunning rather than
    					//                interrupt
    					// ----------------------------------------------------------
    					while (keepRunning && 
    						  ((currentTime - startTime) < (PublicData.storedData.monitor.duration * 60 *1000)))
    					{
    						int numberOfBytesRead = localAudioRecord.read (bytes, 0, minimumBufferSize);
								
    						if (numberOfBytesRead != AudioRecord.ERROR_INVALID_OPERATION)
    						{
    							randomAccessFile.write (bytes, 0, numberOfBytesRead);
    						}
    						// -----------------------------------------------------
    						calendar 	= Calendar.getInstance();
    						currentTime = calendar.getTimeInMillis();
    						// -----------------------------------------------------
    					}
    					// ---------------------------------------------------------
    					// 27/04/2015 ECU changed to use ....WAV_FILE....
    					// ---------------------------------------------------------
    					int audioLength = (int) randomAccessFile.length () - AudioRecorder.WAV_FILE_HEADER_SIZE;
    					int dataLength  = audioLength + 36;
    					// ---------------------------------------------------------
    					// 25/11/2014 ECU position at the start of the file to update
    					//                the header
    					// ---------------------------------------------------------
    					randomAccessFile.seek (0);
    					// ---------------------------------------------------------
    					// 25/11/2014 ECU set up the '.wav' file header
    					// 27/04/2015 ECU changed to use RECORDER_SAMPLERATE
    					// ---------------------------------------------------------
    					randomAccessFile.write 
    						(AudioRecorder.GetWaveFileHeader (audioLength, 
															  dataLength,
															  AudioRecorder.RECORDER_SAMPLERATE,
															  1, 
															  (16 * AudioRecorder.RECORDER_SAMPLERATE * 1)/8));
    					// ---------------------------------------------------------
    					localAudioRecord.stop();
    					localAudioRecord.release();
    					// ---------------------------------------------------------
    					randomAccessFile.close();
    					// ---------------------------------------------------------
    					// 24/04/2015 ECU send the email - if required
    					// 30/04/2015 ECU changed to use 'noisesDirectory' instead of
    					//                the 'projectFolder'
    					// ---------------------------------------------------------
    					if (PublicData.storedData.monitor.email)
    					{
    						Utilities.SendEmailMessage (context,
    													"Sound Monitor",
    													"The attached file is the last sound recorded",
    													null,
    													noisesDirectory + theFileName);
    					}
    					// ---------------------------------------------------------
						// 29/04/2015 ECU initialise the inactive period
    					// 30/04/2015 ECU put in the check on keepRunning
						// ---------------------------------------------------------
    					if (keepRunning)
    					{
    						PublicData.monitorInactivityCounter 
								= PublicData.storedData.monitor.inactivePeriod;
    						// -----------------------------------------------------
    						// 25/11/2014 ECU restart the sound monitoring
    						// -----------------------------------------------------
							StartMonitoring ();
							// -----------------------------------------------------
    					}
    					// ---------------------------------------------------------
    					Utilities.LogToProjectFile (TAG,"all done");
    					// ---------------------------------------------------------
    				}
    				else
    				{
    					// ---------------------------------------------------------
    					// 27/04/2015 ECU come here if the audio record failed to
    					//                initialise
    					//            ECU changed to use resource
    					// ---------------------------------------------------------
    					Utilities.LogToProjectFile (TAG,context.getString (R.string.audio_record_failed_to_initialise));
    					// ---------------------------------------------------------
    				}
    			}
    			catch (Exception theException)
    			{ 
    				// -------------------------------------------------------------
    				// 23/11/2014 ECU want to log exception while checking what
    				//                happens when device is 'sleeping'
    				// -------------------------------------------------------------
    				Utilities.LogToProjectFile (TAG,"TimedRecording " + theException);
    				// -------------------------------------------------------------
    			} 
    		}
    	};
    	// -------------------------------------------------------------------------
    	// 11/10/2013 ECU start up the thread
    	// -------------------------------------------------------------------------
    	timedRecordingThread.start();
    	// -------------------------------------------------------------------------	
    }
    // =============================================================================
    static void updateImage (int theResourceID)
    {
    	// -------------------------------------------------------------------------
    	// 28/04/2015 ECU check if the image has changed
    	// -------------------------------------------------------------------------
    	if (theResourceID != iconID)
    	{
    		// ---------------------------------------------------------------------
    		// 28/04/2015 ECU save the new setting
    		// ---------------------------------------------------------------------
    		iconID = theResourceID;
    		// ---------------------------------------------------------------------
    		// 28/04/2015 ECU get the image refreshed immediately
    		// 05/05/2015 ECU changed to use new method
    		// ---------------------------------------------------------------------
			MusicPlayer.refreshImageAdapter ();
			// ---------------------------------------------------------------------	
    	}
    }
    /* ============================================================================= */
    public static void updateImageView (ImageView theImageView)
	{
   		// -------------------------------------------------------------------------
   		// 28/04/2015 ECU created to update the image view depending on whether 
   		//                the service is running or not
   		// -------------------------------------------------------------------------
   		theImageView.setImageResource (iconID);
   		// -------------------------------------------------------------------------
	}
    // =============================================================================
}

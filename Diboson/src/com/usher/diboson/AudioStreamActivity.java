package com.usher.diboson;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class AudioStreamActivity extends DibosonActivity 
{
	/* ====================================================	*/
	// 05/08/2013 ECU created
	// 21/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	/* ==================================================== */
	//private static final int 	RECORDER_BPP 			= 16;  
	private static final int 	RECORDER_SAMPLERATE 	= 44100;
	private static final int 	RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	/* ==================================================== */
	private	static		 Activity		activity;						// 06/05/2016 ECU added
	private 			 int 			bufferSize 			= 0;
	private	static 		 String []		devices				= null;		// 06/05/2016 ECU created

						 Button 		formatButton;
	private 			 boolean 		isRecording 		= false;
						 int    		numberOfChannels;				// 04/12/2013 ECU changed from private static
						 BufferedOutputStream 	
						 				outputStream 		= null;
						 												// 19/08/2013 ECU changed to BufferedOutputStream
						 												//				  from OutputStream
	private			  	 ProgressBar	progress_bar;
	private static       int    		recorderChannels;
	private 			 AudioRecord 	recorder 			= null;	
	private 			 Thread 		recordingThread		= null;
						 Boolean		remoteActivation 	= false;	// 06/08/2013 ECU added - indicates activated 
						 												//                by remote request              
						 Socket 		socket 				= null;
	private static		 Button 		startButton;
	private static final boolean		stereoMode 			= false;
						 Button 		stopButton;
/* ================================================================================= */  
@Override
public void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	// -----------------------------------------------------------------------------
	if (savedInstanceState == null)
	{
		// -------------------------------------------------------------------------
		// 21/10/2015 ECU the activity has been created anew
		// -------------------------------------------------------------------------
		// 16/02/2014 ECU call up routine to set common activity features
		// 14/11/2016 ECU added the 'true' to get full screen
		// -------------------------------------------------------------------------
		Utilities.SetUpActivity (this,true);
		// -------------------------------------------------------------------------
		// 06/05/2016 ECU remember this activity
		// -------------------------------------------------------------------------
		activity = this;
		// -------------------------------------------------------------------------
		setContentView (R.layout.activity_audio_recorder);
		// -------------------------------------------------------------------------
		// 06/08/2013 ECU check if any data has been supplied
		// -------------------------------------------------------------------------
		Bundle extras = getIntent().getExtras();
		
		if (extras !=null) 
		{
			// ---------------------------------------------------------------------
			// 10/01/2015 ECU added the use of PARAMETER_REMOTE
			// ---------------------------------------------------------------------
			if (extras.getString (StaticData.PARAMETER_REMOTE) != null)
			{
				// -----------------------------------------------------------------
				// 06/08/2013 ECU indicate that activity started by remote device
				// -----------------------------------------------------------------
				remoteActivation = true;
				// -----------------------------------------------------------------
				// 06/08/2013 ECU indicate that being remotely listed to
				// -----------------------------------------------------------------
				setTitle ("Being Listened to by " + PublicData.streamingDestination);	
			}
		}
		// -------------------------------------------------------------------------
		// 05/08/2013 ECU check if a receive has been specified
		// -------------------------------------------------------------------------
		// 05/08/2013 ECU set up the buttons and enable the appropriate ones
		// -------------------------------------------------------------------------
		setButtonHandlers ();
		enableButtons (false);
		// -------------------------------------------------------------------------
		progress_bar = (ProgressBar) findViewById(R.id.recording_progress);
		// -------------------------------------------------------------------------
		// 23/07/2013 ECU set up variables depending on whether in stereo or mono mode
		//				  found that CnM tablet was not working in stereo mode
		// -------------------------------------------------------------------------
		if (stereoMode)
		{
			numberOfChannels = 2;
			recorderChannels = AudioFormat.CHANNEL_IN_STEREO;
		}
		else
		{
			numberOfChannels = 1;
			recorderChannels = AudioFormat.CHANNEL_IN_MONO;
		}
		// -------------------------------------------------------------------------
		// 05/08/2013 ECU get the buffer size required by the audio parameters
		// -------------------------------------------------------------------------
		bufferSize = AudioRecord.getMinBufferSize (RECORDER_SAMPLERATE,recorderChannels,RECORDER_AUDIO_ENCODING);
		// -------------------------------------------------------------------------
		// 06/08/2013 ECU if started remotely then automatically start recoding
		// -------------------------------------------------------------------------
		if (remoteActivation)
		{
			startRecording ();
		}
		else
		{
			// ---------------------------------------------------------------------
			// 06/05/2016 ECU if no destination address is set then request it
			// ---------------------------------------------------------------------
			if (PublicData.streamingDestination == null)
			{
				// -----------------------------------------------------------------
				// 06/05/2016 ECU this is a manual start of the activity
				// -----------------------------------------------------------------
				DialogueUtilities.singleChoice (this, "Select the device which will receive the streamed audio",
												(devices = Utilities.deviceListAsArray(false)),0, 
												Utilities.createAMethod (AudioStreamActivity.class,"Confirm",0),
												Utilities.createAMethod (AudioStreamActivity.class,"Cancel",0));
				// -----------------------------------------------------------------	
			}
		}
	}
	else
	{
		// -------------------------------------------------------------------------
		// 21/10/2015 ECU the activity has been recreated after having been
		//                destroyed by the Android OS
		// -------------------------------------------------------------------------
		finish (); 
		// -------------------------------------------------------------------------			
	}
}
/* ================================================================================= */
private void setButtonHandlers () 
{
	startButton  = (Button)findViewById(R.id.btnStart);
	stopButton   = (Button)findViewById(R.id.btnStop);
	formatButton = (Button)findViewById(R.id.btnFormat);
	// -----------------------------------------------------------------------------
	// 05/08/2013 ECU change the text because using a generic layout from AudioRecorder 
	//                class use new method and resources
	// 06/05/2016 ECU change to use resources
	// -----------------------------------------------------------------------------
	startButton.setText (startTransmissionText());
	stopButton.setText  (activity.getString (R.string.stop_transmission));
	// -----------------------------------------------------------------------------
	// 05/08/2013 ECU make the AudioRecoder playback button invisible
	// -----------------------------------------------------------------------------
	formatButton.setVisibility (View.INVISIBLE);
	// -----------------------------------------------------------------------------
	// 05/08/2013 ECU declare the button listeners
	// -----------------------------------------------------------------------------
	startButton.setOnClickListener (buttonClick);
	stopButton.setOnClickListener (buttonClick);
	// -----------------------------------------------------------------------------
}
/* ================================================================================= */
private void enableButton (int id,boolean isEnable)
{
	((Button)findViewById(id)).setEnabled(isEnable);
}
/* ================================================================================= */
private void enableButtons(boolean isRecording) 
{
	enableButton (R.id.btnStart,!isRecording);
	enableButton (R.id.btnStop,isRecording);
}
/* ================================================================================= */       
private void startRecording ()
{
	// -----------------------------------------------------------------------------
	// 05/08/2013 open up to the socket to the destination device
	// -----------------------------------------------------------------------------
	try
	{
		// -------------------------------------------------------------------------
		// 05/08/2013 ECU open up a socket to the destiantion device and
		//				  then open a stream for writing the data
		// -------------------------------------------------------------------------
		InetAddress serverAddress = InetAddress.getByName(PublicData.streamingDestination);    
		socket = new Socket (serverAddress,PublicData.socketNumberForData); 
		// -------------------------------------------------------------------------
		// 19/08/2013 ECU change to use buffered output
		// -------------------------------------------------------------------------
		outputStream = new BufferedOutputStream	(socket.getOutputStream());
		// -------------------------------------------------------------------------
		// 05/08/2013 ECU declare a means of reading data from the microphone 
		// -------------------------------------------------------------------------
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				RECORDER_SAMPLERATE, recorderChannels,RECORDER_AUDIO_ENCODING, bufferSize);
		// -------------------------------------------------------------------------
		// 05/08/2013 ECU start the progress bar
		// -------------------------------------------------------------------------
		progress_bar.setVisibility(View.VISIBLE);
		// -------------------------------------------------------------------------
		// 05/08/2013 ECU get the device to start recording from the microphone
		// -------------------------------------------------------------------------
		recorder.startRecording();

		isRecording = true;
		// -------------------------------------------------------------------------
		// 05/08/2013 ECU send out the type of data that is to be sent over the stream - needed
		//                by the recipient
		// 12/08/2013 ECU change the way the message type is sent
		// 31/01/2015 ECU changed the method name
		// -------------------------------------------------------------------------
		Utilities.socketSendMessageType (StaticData.SOCKET_MESSAGE_STREAMING,outputStream);
		// -------------------------------------------------------------------------
		// 05/08/2013 ECU set up a thread which will read and transmit the data
		// -------------------------------------------------------------------------
		recordingThread = new Thread(new Runnable() 
		{
			@Override
			public void run() 
			{
				writeAudioDataToSocket();
			}

		},"AudioRecorder Thread");
		// -------------------------------------------------------------------------
		recordingThread.start();
		// -------------------------------------------------------------------------
	}
	catch (IOException theException)
	{	
		// -------------------------------------------------------------------------
		// 05/05/2016 ECU exception occurred so cannot continue
		// -------------------------------------------------------------------------
		Utilities.popToastAndSpeak (this.getString (R.string.audio_streaming_cannot_connect),true);
		// -------------------------------------------------------------------------
		// 05/05/2016 ECU exit this activity
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
	}
}
// =================================================================================
static String startTransmissionText ()
{
	// -----------------------------------------------------------------------------
	// 06/05/2016 ECU created to set the legend for the 'start transmission' button
	// -----------------------------------------------------------------------------
	return activity.getString (R.string.start_transmission) + 
								((PublicData.streamingDestination == null ) ? "" 
																			: " to '" + 
																				Utilities.GetDeviceName(PublicData.streamingDestination) + "'");
	// -----------------------------------------------------------------------------
}
/* ================================================================================= */
private void writeAudioDataToSocket()
{
	byte data[] = new byte[bufferSize];
	
	int numberOfBytesRead = 0;
	// -----------------------------------------------------------------------------
	// 05/08/2013 ECU while data is being recorded
	// 06/08/2013 ECU added the stopStreaming flag
	// -----------------------------------------------------------------------------
	while(isRecording)
	{	
		// -------------------------------------------------------------------------
		// 06/08/2013 ECU check if need to stop the recording
		// -------------------------------------------------------------------------
		if (PublicData.stopStreaming)
		{
			// ---------------------------------------------------------------------
			// 06/08/2013 ECU stop all recording
			// ---------------------------------------------------------------------
			stopRecording ();
		}
		else
		{	
			// ---------------------------------------------------------------------
			// 05/08/2013 ECU read in a chunk of data
			// ---------------------------------------------------------------------
			numberOfBytesRead = recorder.read(data, 0, bufferSize);
			// ---------------------------------------------------------------------
			// 05/08/2015 ECU while we haven't reached the end of file
			// ---------------------------------------------------------------------
			if(AudioRecord.ERROR_INVALID_OPERATION != numberOfBytesRead)
			{
				try
				{
					// -------------------------------------------------------------
					// 05/08/2013 ECU write the data out and flush to make sure it goes
					// -------------------------------------------------------------
					outputStream.write (data, 0, numberOfBytesRead);
					outputStream.flush ();
				}	 
				catch (IOException theException) 
				{
				}
			}
		}
	}
}
/* ================================================================================= */
private void stopRecording()
{
	// -----------------------------------------------------------------------------
	// 05/08/2013 ECU indicate that recording has stopped - this will cause the thread to end
	// -----------------------------------------------------------------------------
	isRecording = false;
	// -----------------------------------------------------------------------------
	// 05/08/2013 ECU stop the physical recording and release any resources
	// -----------------------------------------------------------------------------
	recorder.stop ();
	recorder.release ();
	// -----------------------------------------------------------------------------
	// 05/08/2013 ECU close the socket and output stream
	// -----------------------------------------------------------------------------
	try
	{
		outputStream.close ();
		socket.close ();
	}
	catch (IOException theException)
	{	
	}
	// -----------------------------------------------------------------------------
	// 06/08/2013 ECU check if called by remote listener
	// -----------------------------------------------------------------------------
	if (PublicData.stopStreaming)
	{
		// -------------------------------------------------------------------------
		// 06/08/2013 ECU indicate that action has been taken
		// -------------------------------------------------------------------------
		PublicData.stopStreaming = false;
		// -------------------------------------------------------------------------
		// 06/08/2013 ECU exit this task
		// -------------------------------------------------------------------------
		finish ();
	}
	else
	{
		// -------------------------------------------------------------------------
		// 05/08/2013 ECU hide the progress bar
		// -------------------------------------------------------------------------
		progress_bar.setVisibility(View.INVISIBLE);
	}
	
}
/* ================================================================================= */
private View.OnClickListener buttonClick = new View.OnClickListener() 
{
	@Override
	public void onClick(View theView) 
	{
		switch(theView.getId())
		{
			case R.id.btnStart:
  	       		enableButtons(true);
  	       		startRecording();
  	       		break;	
			case R.id.btnStop:
				enableButtons(false);
				stopRecording();    
				break;  
		}		 
	}
};
/* ============================================================================== */

//==================================================================================
public static void Cancel (int theIndex)
{
	// -----------------------------------------------------------------------------
	// 06/05/2016 ECU the user has cancelled the selection so just terminate this
	//                activity
	// -----------------------------------------------------------------------------
	activity.finish ();
	// -----------------------------------------------------------------------------
}
//==================================================================================
public static void Confirm (int theIndex)
{
	// -----------------------------------------------------------------------------
	// 06/05/2016 ECU indicate the device that is to receive the audio streaming
	// -----------------------------------------------------------------------------
	PublicData.streamingDestination = Devices.returnIPAddress (devices [theIndex]);	
	// -----------------------------------------------------------------------------
	// 06/05/2016 ECU update the button text
	// -----------------------------------------------------------------------------
	startButton.setText (startTransmissionText ());
	// -----------------------------------------------------------------------------
}
//==================================================================================



} // End of Class

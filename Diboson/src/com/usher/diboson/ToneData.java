package com.usher.diboson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class ToneData 
{
	// =============================================================================
	// 25/07/2015 ECU created to set up a 'fixed frequency' tone and to provide
	//                a public method to play the defined tone
	// =============================================================================
	
	// =============================================================================
	// =============================================================================
	
	// =============================================================================
	AudioTrack 	audioTrack			= 	null;
	byte 		generatedSound [];
	int			numberOfSamples		=	0;
	boolean 	played;
	Method		playFinishedMethod	= 	null;
	int			totalSamples		=	0;
	// =============================================================================

	// =============================================================================
	public ToneData (int theFrequency,int theDuration,Method theFinishedMethod)
	{
		// -------------------------------------------------------------------------
		// 25/07/2015 ECU create the construct with the frequency and duration as
		//                arguments
		//
		//						theFrequency 	frequency of tone in Hz
		//						theDuration		duration in milliseconds
		// -------------------------------------------------------------------------
		// 25/07/2015 ECU generate the data associated with the arguments
		// -------------------------------------------------------------------------
		playFinishedMethod 	= theFinishedMethod;
		// -------------------------------------------------------------------------
		generatedSound = GenerateSound (theFrequency,theDuration);
		// -------------------------------------------------------------------------
		// 18/08/2015 ECU remember the total samples
		// -------------------------------------------------------------------------
		totalSamples = numberOfSamples;
	    // -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public ToneData (Tone theTone,Method theFinishedMethod)
	{
		// -------------------------------------------------------------------------
		// 20/08/2015 ECU created method to handle the information in a single Tone
		//                object
		// -------------------------------------------------------------------------
		this (theTone.frequency,theTone.duration,theFinishedMethod);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public ToneData (Tones theTones,Method theFinishedMethod)
	{
		// -------------------------------------------------------------------------
		// 25/07/2015 ECU generate the data associated with the arguments
		// -------------------------------------------------------------------------
		playFinishedMethod 	= theFinishedMethod;
		// -------------------------------------------------------------------------
		// 18/08/2015 ECU preset the total number of samples
		// -------------------------------------------------------------------------
		totalSamples = 0;
		// -------------------------------------------------------------------------
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
	   
		for (int theTone = 0; theTone < theTones.tones.length; theTone++)
		{
			try 
			{
				outputStream.write (GenerateSound (theTones.tones[theTone].frequency,theTones.tones[theTone].duration));
				// -----------------------------------------------------------------
				// 18/08/2015 ECU add into the total number of samples
				// -----------------------------------------------------------------
				totalSamples += numberOfSamples;
				// -----------------------------------------------------------------
			} 
			catch (IOException theException) 
			{
			}
		} 
		generatedSound = outputStream.toByteArray();
	}
	// =============================================================================
	byte [] GenerateSound (int theFrequency,int theDuration)
	{
		// -------------------------------------------------------------------------
		// 18/08/2015 ECU changed to use AudioRecorder.RECORDER_SAMPLERATE rather
		//                y=than a local variable
		// -------------------------------------------------------------------------
		numberOfSamples 	= (AudioRecorder.RECORDER_SAMPLERATE * theDuration) / 1000;
		// -------------------------------------------------------------------------
		double samples [] = new double [numberOfSamples];
		// -------------------------------------------------------------------------
		// 25/07/2015 ECU generate the samples array based on a sine wave at the
		//                specified frequency
		// -------------------------------------------------------------------------
		for (int theSample = 0; theSample < numberOfSamples; theSample++) 
		{
			samples [theSample] = Math.sin (2 * Math.PI * theSample / (AudioRecorder.RECORDER_SAMPLERATE/theFrequency));
	    }
		// -------------------------------------------------------------------------
		// 25/07/2015 ECU now convert to 16 bit PCM sound array - assumes the
		//                'samples' buffer is normalised
		// -------------------------------------------------------------------------
		byte [] localGeneratedSound = new byte [2 * numberOfSamples];
		// -------------------------------------------------------------------------
	    int index = 0;
	    // -------------------------------------------------------------------------
	    // 25/07/2015 ECU scan the samples array and copy across
	    // -------------------------------------------------------------------------
	    for (final double sampleValue : samples) 
	    {
	    	// ---------------------------------------------------------------------
	    	// 25/07/2015 ECU scale to the maximum amplitude
	    	// ---------------------------------------------------------------------
	        final short scaledValue = (short) ((sampleValue * 32767));
	        // ---------------------------------------------------------------------
	        // 25/07/2015 ECU in 16 bit wav PCM - first byte is the low order byte
	        // ---------------------------------------------------------------------
	        localGeneratedSound [index++] = (byte) (scaledValue & 0x00ff);
	        localGeneratedSound [index++] = (byte) ((scaledValue & 0xff00) >>> 8);
	        // ---------------------------------------------------------------------
	    }
	    // -------------------------------------------------------------------------
        // 25/07/2015 ECU return the generated sound
        // -------------------------------------------------------------------------
        return localGeneratedSound;
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
	public void Play ()
	{
		// -------------------------------------------------------------------------
		// 25/07/2015 ECU created to play the stored tone data
		// -------------------------------------------------------------------------
		audioTrack 
			= new AudioTrack (AudioManager.STREAM_MUSIC,
							  AudioRecorder.RECORDER_SAMPLERATE, 
							  AudioFormat.CHANNEL_OUT_MONO,
							  AudioFormat.ENCODING_PCM_16BIT, 
							  generatedSound.length,
							  AudioTrack.MODE_STATIC);
		// -------------------------------------------------------------------------
		audioTrack.setNotificationMarkerPosition (totalSamples);
		// -------------------------------------------------------------------------
		// 26/07/2015 ECU indicate that not fully played yet
		// -------------------------------------------------------------------------
		played = false;
		// -------------------------------------------------------------------------
		audioTrack.write (generatedSound, 0, generatedSound.length);
		audioTrack.play ();
		// -------------------------------------------------------------------------
		// 25/07/2015 ECU now want to wait until the tone has finished playing
		// -------------------------------------------------------------------------
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				// -----------------------------------------------------------------
				// 26/07/2015 ECU set up the listener for when the track ends
				// -----------------------------------------------------------------
				audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() 
				{
					// -------------------------------------------------------------
					@Override
					public void onPeriodicNotification(AudioTrack track) 
					{
		            
					}
					// -------------------------------------------------------------
					@Override
					public void onMarkerReached (AudioTrack track) 
					{
						// ---------------------------------------------------------
						// 25/07/2015 ECU release resources
						// ---------------------------------------------------------
						audioTrack.stop ();
						audioTrack.release ();
						// ---------------------------------------------------------
						// 15/08/2015 ECU reset the variable
						// ---------------------------------------------------------
						audioTrack = null;
						// ---------------------------------------------------------
						try 
						{
							// -----------------------------------------------------
							if (playFinishedMethod != null)
								playFinishedMethod.invoke (null);
							// -----------------------------------------------------
							// 26/07/2015 ECU indicate everything done
							// -----------------------------------------------------
							played = true;
							// -----------------------------------------------------
						}
						catch (Exception theException) 
						{
						}
		            
					}
				});
				// -----------------------------------------------------------------
				while (!played)
				{
					try 
					{
						sleep (10);
					} 
					catch (Exception theException) 
					{
					}
				}
				// -----------------------------------------------------------------
			}
		};			
        thread.start();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void StopPlaying ()
	{
		// -------------------------------------------------------------------------
		// 15/08/2015 ECU created to stop the player
		// -------------------------------------------------------------------------
		if (audioTrack != null)
		{
			audioTrack.stop ();
			audioTrack.release ();
		}
		// -------------------------------------------------------------------------
		playFinishedMethod = null;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

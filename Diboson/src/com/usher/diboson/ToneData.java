package com.usher.diboson;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

public class ToneData 
{
	// =============================================================================
	// 25/07/2015 ECU created to set up a 'fixed frequency' tone and to provide
	//                a public method to play the defined tone
	// 08/08/2019 ECU added 'gap' and 'repeat'
	// 12/08/2019 ECU because of a 'click' at the end of a generated sound (caused
	//                by a sudden change in volume from maximum to nothing) then
	//                slowly range down the volume
	// =============================================================================
	
	// =============================================================================
	private static final int RAMP_STEPS = 50;
	// =============================================================================
	
	// =============================================================================
	AudioTrack 	audioTrack			= 	null;
	int			gap					=	StaticData.NOT_SET;
	byte 		generatedSound [];
	int			numberOfSamples		=	0;
	boolean 	played;
	Method		playFinishedMethod	= 	null;
	int			repeats				=   StaticData.NOT_SET;
	int			totalSamples		=	0;
	float		volumeCurrent;
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
	// -----------------------------------------------------------------------------
	public ToneData (int theFrequency,int theDuration,int theGap,int theRepeats,Method theFinishedMethod)
	{
		// -------------------------------------------------------------------------
		// 08/08/2019 ECU created to set up the tone data where
		//					theFrequency ..... the frequency of the tone
		//                  theDuration ...... how long the tone is to play in mS
		//                  theGap ........... the gap between successive plays in mS
		//					theRepeats ....... how many times the sequence is to be
		//                                     repeated
		// -------------------------------------------------------------------------
		// 08/08/2019 ECU generate the sound stream that is to be played
		// -------------------------------------------------------------------------
		this (theFrequency,theDuration,theFinishedMethod);
		// -------------------------------------------------------------------------
		// 08/08/2019 ECU now set the additional parameters
		// -------------------------------------------------------------------------
		gap		= theGap;
		repeats	= theRepeats;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	byte [] GenerateSound (int theFrequency,int theDuration)
	{
		// -------------------------------------------------------------------------
		// 07/08/2017 ECU Note - the frequency is supplied in Hz and 
		//                       the duration is in milliSeconds
		// 18/08/2015 ECU changed to use AudioRecorder.RECORDER_SAMPLERATE rather
		//                than a local variable - this is supplied in Hz.
		// 07/08/2017 ECU Note - the '/ 1000' is because the calculation is in seconds
		// -------------------------------------------------------------------------
		numberOfSamples 	= (AudioRecorder.RECORDER_SAMPLERATE * theDuration) / 1000;
		// -------------------------------------------------------------------------
		double samples [] = new double [numberOfSamples];
		// -------------------------------------------------------------------------
		// 25/07/2015 ECU generate the samples array based on a sine wave at the
		//                specified frequency
		// -------------------------------------------------------------------------
		for (int sample = 0; sample < numberOfSamples; sample++) 
		{
			samples [sample] = Math.sin ((2 * Math.PI * sample * theFrequency) / AudioRecorder.RECORDER_SAMPLERATE);
	    }
		// -------------------------------------------------------------------------
		// 25/07/2015 ECU now convert to 16 bit PCM sound array - assumes the
		//                'samples' buffer is normalised
		// -------------------------------------------------------------------------
		byte [] localGeneratedSound = new byte [2 * numberOfSamples];
		// -------------------------------------------------------------------------
		// 12/08/2019 ECU work out the ramp down period
		// -------------------------------------------------------------------------
		int rampPeriod = numberOfSamples / RAMP_STEPS;
		// -------------------------------------------------------------------------
		// 12/08/2019 ECU work out the change in volume per sample
		// -------------------------------------------------------------------------
		float volumeChange   = 32767.0f / (float) (rampPeriod);
		volumeCurrent  = 0.0f;
		// -------------------------------------------------------------------------
	    int index = 0;
	    // -------------------------------------------------------------------------
	    // 25/07/2015 ECU scan the samples array and copy across
	    // -------------------------------------------------------------------------
	    for (int theSample = 0; theSample < samples.length; theSample++) 
	    {
	    	// ---------------------------------------------------------------------
	    	// 25/07/2015 ECU scale to the maximum amplitude
	    	// ---------------------------------------------------------------------
	        final short scaledValue = (short) ((samples [theSample] * (short) volumeCurrent));
	        // ---------------------------------------------------------------------
	        // 25/07/2015 ECU in 16 bit wav PCM - first byte is the low order byte
	        // ---------------------------------------------------------------------
	        localGeneratedSound [index++] = (byte) (scaledValue & 0x00ff);
	        localGeneratedSound [index++] = (byte) ((scaledValue & 0xff00) >>> 8);
	        // ---------------------------------------------------------------------
	        // 12/08/2019 ECU adjust the volume - this is not very efficient but it
	        //                is easier to understand
	        // ---------------------------------------------------------------------
	        // 12/08/2019 ECU check if ramping up
	        // ---------------------------------------------------------------------
	        if (theSample < rampPeriod)
	        {
	        	volumeCurrent += volumeChange;	        	
	        }
	        else
	        // ---------------------------------------------------------------------
	        // 15/08/2019 ECU the volume should now be at the maximum
	        // ---------------------------------------------------------------------
	        if (theSample == rampPeriod)
	        {
	        	volumeCurrent  = 32767.0f;
	        }
	        else
	        // ---------------------------------------------------------------------
	        // 12/08/2019 ECU check if ramping down
	        // ---------------------------------------------------------------------
	        if (theSample >= (samples.length - rampPeriod))
	        {
	        	volumeCurrent -= volumeChange;  	
	        }
	        // --------------------------------------------------------------------
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
			public void run ()
			{
				// -----------------------------------------------------------------
				// 26/07/2015 ECU set up the listener for when the track ends
				// -----------------------------------------------------------------
				audioTrack.setPlaybackPositionUpdateListener (new AudioTrack.OnPlaybackPositionUpdateListener() 
				{
					// -------------------------------------------------------------
					@Override
					public void onPeriodicNotification (AudioTrack track) 
					{
						
					}
					// -------------------------------------------------------------
					@Override
					public void onMarkerReached (AudioTrack track) 
					{
						// ---------------------------------------------------------
						// 08/08/2019 ECU Note - make sure the audio track has been
						//						 stopped
						// 14/09/2020 ECU try and capture any 'illegal state' issues
						// ---------------------------------------------------------
						try
						{
							audioTrack.stop ();
							// -----------------------------------------------------
							// 08/08/2019 ECU check if the tone is to be repeated
							// -----------------------------------------------------
							if ((repeats == StaticData.NOT_SET) || (repeats == 0))
							{
								// -------------------------------------------------
								// 25/07/2015 ECU release resources
								// -------------------------------------------------
								audioTrack.release ();
								// -------------------------------------------------
								// 15/08/2015 ECU reset the variable
								// -------------------------------------------------
								audioTrack = null;
								// -------------------------------------------------
								try
								{
									// ---------------------------------------------
									if (playFinishedMethod != null)
										playFinishedMethod.invoke (null);
									// ---------------------------------------------
									// 26/07/2015 ECU indicate everything done
									// ---------------------------------------------
									played = true;
									// ---------------------------------------------
								}
								catch (Exception theException)
								{
								}
							}
							else
							{
								// -------------------------------------------------
								// 08/08/2019 ECU the defined tone needs to be repeated
								//                after the specified gap
								// -------------------------------------------------
								try
								{
									sleep (gap);
								}
								catch (InterruptedException theException)
								{
								}
								// -------------------------------------------------
								// 08/08/2019 ECU now replay the defined tone
								// -------------------------------------------------
								audioTrack.setPlaybackHeadPosition (0);
								audioTrack.play ();
								// -------------------------------------------------
								// 08/08/2019 ECU decrement the number of repeats
								// -------------------------------------------------
								repeats--;
								// -------------------------------------------------
							}
						}
						catch (IllegalStateException theException)
						{
							// -----------------------------------------------------
							// 14/09/2020 ECU the 'audiotrack' was in the wrong state
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
		            }
		            // -------------------------------------------------------------
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

package com.usher.diboson;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Locale;

// =================================================================================
// MonitorHandler
// ==============
// 15/12/2019 ECU created to handle messages associated with the 'monitoring'
//                facility
// 17/12/2019 ECU lots of changes so that all of the work is performed here
//                rather than in MonitorService
// 20/12/2019 ECU try to modify so that can use 'alarms' to start/stop - this did
//                not seem to work so abandon
// 21/12/2019 ECU tried again with a single alarm, received by MonitorReceiver,
//                that eventually send a message of type .....TIME_CHECK.
//                Tried to introduce other alarms for 'start' and 'stop' - did not
//                work so needs further investigation.
//            ECU more changes so that the process is started by a 'start alarm' - and
//                when the 'start message' is received it sets an alarm for the
//                'stop time'
// =================================================================================
public class MonitorHandler extends Handler
{
    // =============================================================================
    private static final String TAG = "MonitorHandler";
    // =============================================================================

    // =============================================================================
    private static final int MONITOR_MESSAGE_EMAIL           =   0;
    private static final int MONITOR_MESSAGE_EMAIL_SENDING   =   1;
    private static final int MONITOR_MESSAGE_EMAIL_WAITING   =   2;
    private static final int MONITOR_MESSAGE_FAILURE         =   3;
    private static final int MONITOR_MESSAGE_FINISHED        =   4;
    private static final int MONITOR_MESSAGE_INITIALISE      =   5;
    private static final int MONITOR_MESSAGE_LISTEN          =   6;
    private static final int MONITOR_MESSAGE_RECORD_START    =   7;
    private static final int MONITOR_MESSAGE_RECORD_STOP     =   8;
    private static final int MONITOR_MESSAGE_RESTART         =   9;
    private static final int MONITOR_MESSAGE_START           =   10;
    private static final int MONITOR_MESSAGE_STOP            =   11;
    private static final int MONITOR_MESSAGE_TRIGGERED       =   12;
    // -----------------------------------------------------------------------------
    // 18/12/2019 ECU printable messages - REMOVE WHEN FULLY TESTED
    // -----------------------------------------------------------------------------
    private static final String [] MONITOR_MESSAGE   = {"email enabled",
                                                        "Waiting for email to end",
                                                        "Waiting for email to start",
                                                        "email transmission failure",
                                                        "Processing has finished",
                                                        "Initialising",
                                                        "Listening for a noise",
                                                        "Starting to record",
                                                        "Stopping the recording",
                                                        "Restart",
                                                        "Start the processing",
                                                        "Stop the processing",
                                                        "Triggered by a noise"};
    // =============================================================================

    // =============================================================================
    private final static float      BRIGHTNESS_LOWEST       = 0f;
    private final static float      BRIGHTNESS_HIGHEST      = 0.75f;
    private final static int 		BUFFER_MULTIPLIER 		= 5;
    private final static int		SAMPLE_RATE 			= 8000;
    // =============================================================================

    // =============================================================================
    private         float               brightness;
    private         Context             context;
    public  static  SimpleDateFormat    dateFormat;
    private         boolean             doNotRecord;
    private static  int                 iconID			    = R.drawable.audio_analyser;
    private         boolean             initialised;
    private static  boolean             monitorActivated;
    private         Monitor             monitor;
    private         String              noisesDirectory;
    private         boolean             record;
    private         long                timeCurrent;
    private         long                timeStart;
    private         long                timeStop;
    // =============================================================================


    // =============================================================================
    public MonitorHandler (Context theContext)
    {
        // -------------------------------------------------------------------------
        // 15/12/2019 ECU remember any variables for later use
        // -------------------------------------------------------------------------
        context             =   theContext;
        dateFormat          =   new SimpleDateFormat (PublicData.dateFormatDDMMYYYY + " HH:mm:ss", Locale.getDefault());
        doNotRecord         =   false;
        // -------------------------------------------------------------------------
        // 22/12/2019 ECU get the brightness of the screen - returns 0 to 255
        // -------------------------------------------------------------------------
        int localBrightness = Utilities.getScreenBrightness (theContext);
        // -------------------------------------------------------------------------
        // 22/12/2019 ECU check if an error occurred
        // -------------------------------------------------------------------------
        if (localBrightness != StaticData.NOT_SET)
        {
            // ---------------------------------------------------------------------
            // 22/12/2019 ECU got a valid brightness (0 to 255) so scale it 0f to 1f
            // ---------------------------------------------------------------------
            brightness = (float) localBrightness / 255.0f;
            // ---------------------------------------------------------------------
        }
        else
        {
            // ---------------------------------------------------------------------
            // 22/12/2019 ECU error occurred so set to 'brightest'
            // ---------------------------------------------------------------------
            brightness = BRIGHTNESS_HIGHEST;
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 18/12/2019 ECU initialise some variables - method added because called
        //                from more than one location
        // -------------------------------------------------------------------------
        initialiseVariables ();
        // -------------------------------------------------------------------------
        // 16/12/2019 ECU set up the destination directory - the string contains the
        //                terminating '/'
        // -------------------------------------------------------------------------
        noisesDirectory = PublicData.projectFolder + theContext.getString (R.string.noises_directory);
        // -------------------------------------------------------------------------
        // 16/12/2019 ECU reset any email variables
        // -------------------------------------------------------------------------
        new EmailMessage ();
        // -------------------------------------------------------------------------
        // 16/12/2019 ECU now trigger everything
        // -------------------------------------------------------------------------
        sendEmptyMessage (MONITOR_MESSAGE_INITIALISE);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public void handleMessage (Message theMessage)
    {
        // -------------------------------------------------------------------------
        // 06/12/2019 ECU log the information
        // 24/12/2019 ECU changed to use the method to display the message name
        // -------------------------------------------------------------------------
        Utilities.LogToProjectFile (TAG,"Message : " + MessageName (theMessage.what));
        // -------------------------------------------------------------------------
        // 06/12/2019 ECU change to switch on the type of message received
        //                which is in '.what'
        // -------------------------------------------------------------------------
        switch (theMessage.what)
        {
            // =====================================================================
            case MONITOR_MESSAGE_EMAIL:
                // -----------------------------------------------------------------
                // 16/12/2019 ECU email the 'wav' file that contains the recorded
                //                'noise'
                // -----------------------------------------------------------------
                // 16/12/2019 ECU indicate in listening mode
                // -----------------------------------------------------------------
                updateImage (R.drawable.listen_sending);
                // -----------------------------------------------------------------
                PublicData.emailHandler.SendEmailMessage (null,
                                                          context.getString (R.string.sound_monitor_title),
                                                          context.getString (R.string.sound_monitor_attached),
                                                          null,
                                                          (String) theMessage.obj);
                // -----------------------------------------------------------------
                // 16/12/2019 ECU trigger the monitoring of the sending of the
                //                email message - firstly wait to it starts sending
                // -----------------------------------------------------------------
                sendEmptyMessage (MONITOR_MESSAGE_EMAIL_WAITING);
                // -----------------------------------------------------------------
                break;
            // =====================================================================
            case MONITOR_MESSAGE_EMAIL_SENDING:
                // -----------------------------------------------------------------
                // 16/12/2019 ECU wait until the email has been sent
                // -----------------------------------------------------------------
                if (PublicData.emailDetails.sending)
                {
                    // -------------------------------------------------------------
                    // 16/12/2019 ECU still sending so wait a bit
                    // -------------------------------------------------------------
                    sendEmptyMessageDelayed (MONITOR_MESSAGE_EMAIL_SENDING,StaticData.MILLISECONDS_PER_MINUTE);
                    // -------------------------------------------------------------
                }
                else
                {
                    // -------------------------------------------------------------
                    // 16/12/2019 ECU indicate that everything is done
                    // -------------------------------------------------------------
                    sendEmptyMessage (MONITOR_MESSAGE_FINISHED);
                    // -------------------------------------------------------------
                }
                // -----------------------------------------------------------------
                break;
            // =====================================================================
            case MONITOR_MESSAGE_EMAIL_WAITING:
                // -----------------------------------------------------------------
                // 16/12/2019 ECU wait until the email is being sent
                // -----------------------------------------------------------------
                if (!PublicData.emailDetails.sending)
                {
                    // -------------------------------------------------------------
                    // 16/12/2019 ECU still sending so wait a bit
                    // -------------------------------------------------------------
                    sendEmptyMessageDelayed (MONITOR_MESSAGE_EMAIL_WAITING,1000);
                    // -------------------------------------------------------------
                }
                else
                {
                    // -------------------------------------------------------------
                    // 16/12/2019 ECU indicate that email is being sent
                    // -------------------------------------------------------------
                    sendEmptyMessage (MONITOR_MESSAGE_EMAIL_SENDING);
                    // -------------------------------------------------------------
                }
                // -----------------------------------------------------------------
                break;
            // =====================================================================
            case MONITOR_MESSAGE_FAILURE:
                // -----------------------------------------------------------------
                // 16/12/2019 ECU called because the email being transmitted has
                //                failed
                //                initially the ..sending flag will be false so
                //                want to wait for this to go true - but delete
                //                any queued messages that are waiting for the
                //                ...sending flag to go false
                // -----------------------------------------------------------------
                removeMessages   (MONITOR_MESSAGE_EMAIL_SENDING);
                sendEmptyMessage (MONITOR_MESSAGE_EMAIL_WAITING);
                // -----------------------------------------------------------------
                // 16/12/2019 ECU indicate what is happening
                // -----------------------------------------------------------------
                updateImage (R.drawable.listen_transmission_error);
                // -----------------------------------------------------------------
                break;
            // =====================================================================
            case MONITOR_MESSAGE_FINISHED:
                // -----------------------------------------------------------------
                // 16/12/2019 ECU everything has processed so trigger the next scan
                //                after the 'inactivity delay'
                // -----------------------------------------------------------------
                if (monitorActivated)
                {
                    // -----------------------------------------------------------------
                    // 16/12/2019 ECU indicate in 'inactive' mode
                    // -----------------------------------------------------------------
                    updateImage (R.drawable.listen_inactive);
                    // -------------------------------------------------------------
                    // 16/12/2019 ECU if still 'activated' then trigger the next scan
                    // -------------------------------------------------------------
                    sendEmptyMessageDelayed (MONITOR_MESSAGE_LISTEN,
                        monitor.inactivePeriod * StaticData.MILLISECONDS_PER_MINUTE);
                }
                // -----------------------------------------------------------------
                break;
            // =====================================================================
            case MONITOR_MESSAGE_INITIALISE:
                // -----------------------------------------------------------------
                // 15/12/2019 ECU called to initialise any aspects of the 'monitoring'
                //                process
                // -----------------------------------------------------------------
                timeCurrent = Utilities.getAdjustedTime (true);
                // -----------------------------------------------------------------
                // 16/12/2019 ECU check if this is the first time through or a
                //                re-activation after a period of activity
                // -----------------------------------------------------------------
                if (!initialised)
                {
                    // -------------------------------------------------------------
                    // 16/12/2019 ECU this is the first activation
                    // -------------------------------------------------------------
                    timeStart   = Utilities.getAdjustedTimeHHMMSS (monitor.startTimeHour,monitor.startTimeMinute,0,true);
                    timeStop    = Utilities.getAdjustedTimeHHMMSS (monitor.stopTimeHour, monitor.stopTimeMinute, 0,true);
                    // -------------------------------------------------------------
                    // 23/12/2019 ECU check if the times span midnight
                    // -------------------------------------------------------------
                    if (monitor.spansMidnight)
                    {
                        // ---------------------------------------------------------
                        // 23/12/2019 ECU check if in an active period
                        // ---------------------------------------------------------
                        if (timeCurrent < timeStop)
                        {
                            // -----------------------------------------------------
                            // 23/12/2019 ECU adjust to previous day
                            // -----------------------------------------------------
                            timeStart -= StaticData.MILLISECONDS_PER_DAY;
                            // -----------------------------------------------------
                        }
                        else
                        {
                            // -----------------------------------------------------
                            // 15/12/2019 ECU the 'monitoring' period spans midnight
                            // -----------------------------------------------------
                            timeStop += StaticData.MILLISECONDS_PER_DAY;
                            // -----------------------------------------------------
                        }
                        // ---------------------------------------------------------
                    }
                    // -------------------------------------------------------------
                    // 16/12/2019 ECU want to set the messages up for 'tomorrow'
                    //            ECU indicate that first initialisation done
                    // -------------------------------------------------------------
                    initialised = true;
                    // -------------------------------------------------------------
                }
                else
                {
                    // -------------------------------------------------------------
                    // 16/12/2019 ECU this is re-activation for the next day
                    // -------------------------------------------------------------
                    timeStart   += StaticData.MILLISECONDS_PER_DAY;
                    timeStop    += StaticData.MILLISECONDS_PER_DAY;
                    // -------------------------------------------------------------
                }
                //------------------------------------------------------------------
                // 17/12/2019 ECU check on the 'stop' time
                // -----------------------------------------------------------------
                if (timeCurrent >= timeStop)
                {
                    // -------------------------------------------------------------
                    // 17/12/2019 ECU this means that the handler has been initialised
                    //                after the 'monitoring gap' for today so need
                    //                to adjust the gap for 'tomorrow'
                    // -------------------------------------------------------------
                    sendEmptyMessage (MONITOR_MESSAGE_INITIALISE);
                    // -------------------------------------------------------------
                }
                else
                {

                    // -------------------------------------------------------------
                    // 20/12/2019 ECU at this point have the correct start/stop
                    //                times - just log them
                    // -------------------------------------------------------------
                    Utilities.LogToProjectFile (TAG,"Start Time : " +   dateFormat.format (timeStart));
                    Utilities.LogToProjectFile (TAG,"Stop Time : "  +   dateFormat.format (timeStop));
                    // -------------------------------------------------------------
                    // 16/12/2019 ECU indicate activated
                    // -------------------------------------------------------------
                    updateImage (R.drawable.listen);
                    // -------------------------------------------------------------
                    // 21/12/2019 ECU set the alarm for the 'start' - if this is
                    //                before the current time then the alarm will trigger
                    //                immediately
                    // -------------------------------------------------------------
                    MonitorReceiver.SetAlarm (context,timeStart,
                                                StaticData.ALARM_ID_MONITOR,MONITOR_MESSAGE_START);
                    // -------------------------------------------------------------
                }
                // -----------------------------------------------------------------
                break;
            // =====================================================================
            case MONITOR_MESSAGE_LISTEN:
                // -----------------------------------------------------------------
                // 15/12/2019 ECU put the device into 'listen' mode
                // -----------------------------------------------------------------
                // 16/12/2019 ECU indicate in listening mode
                // -----------------------------------------------------------------
                updateImage (R.drawable.listen_listening);
                // -----------------------------------------------------------------
                // 16/12/2019 ECU changed to use a thread
                // -----------------------------------------------------------------
                Thread listenThread = new Thread()
                {
                    // -------------------------------------------------------------
                    @Override
                    public void run()
                    {
                        // ---------------------------------------------------------
                        // 16/12/2019 ECU check for a 'noise' - if found then
                        //                proceed to the next step
                        // ---------------------------------------------------------
                        if (checkForANoise (monitor.triggerLevel))
                            sendEmptyMessage (MONITOR_MESSAGE_TRIGGERED);
                        // ---------------------------------------------------------
                    }
                    // -------------------------------------------------------------
                };
                // -----------------------------------------------------------------
                // 16/12/2019 ECU start up the 'listen' thread
                // -----------------------------------------------------------------
                listenThread.start ();
                // -----------------------------------------------------------------
                break;
            // =====================================================================
            case MONITOR_MESSAGE_RECORD_START:
                // -----------------------------------------------------------------
                // 16/12/2019 ECU start up the 'timed recorder'
                // -----------------------------------------------------------------
                // 16/12/2019 ECU indicate in 'recording' mode
                // -----------------------------------------------------------------
                updateImage (R.drawable.listen_recording);
                // -----------------------------------------------------------------
                timedRecording (Utilities.getAUniqueFileName (context.getString (R.string.noise_file)) +
                                context.getString (R.string.audio_file_extension));
                // -----------------------------------------------------------------
                break;
            // =====================================================================
            case MONITOR_MESSAGE_RECORD_STOP:
                // -----------------------------------------------------------------
                // 16/12/2019 ECU indicate that it is time to stop the recorder
                // -----------------------------------------------------------------
                record           = false;
                // -----------------------------------------------------------------
                break;
            // =====================================================================
            case MONITOR_MESSAGE_RESTART:
                // -----------------------------------------------------------------
                // 18/12/2019 ECU restart the monitor after resetting some variables
                // -----------------------------------------------------------------
                initialised = false;
                monitor     = PublicData.storedData.monitor;
                // -----------------------------------------------------------------
                // 18/12/2019 ECU clear any queued messages
                // -----------------------------------------------------------------
                removeMessages (MONITOR_MESSAGE_START);
                removeMessages (MONITOR_MESSAGE_STOP);
                // -----------------------------------------------------------------
                // 18/12/2019 ECU now reinitialise this thread
                // -----------------------------------------------------------------
                sendEmptyMessage (MONITOR_MESSAGE_INITIALISE);
                // -----------------------------------------------------------------
                break;
            // =====================================================================
            case MONITOR_MESSAGE_START:
                // -----------------------------------------------------------------
                // 16/12/2019 ECU the monitoring has been started
                // -----------------------------------------------------------------
                monitorActivated = true;
                // -----------------------------------------------------------------
                // 22/12/2019 ECU reduce the brightness to save battery
                // -----------------------------------------------------------------
                setScreenBrightness (BRIGHTNESS_LOWEST);
                // -----------------------------------------------------------------
                // 15/12/2019 ECU put this device into 'listen' mode
                // -----------------------------------------------------------------
                sendEmptyMessage (MONITOR_MESSAGE_LISTEN);
                // -----------------------------------------------------------------
                // 21/12/2019 ECU set up the alarm to 'stop monitoring'
                // -----------------------------------------------------------------
                MonitorReceiver.SetAlarm (context,timeStop,
                        StaticData.ALARM_ID_MONITOR,MONITOR_MESSAGE_STOP);
                // -----------------------------------------------------------------
                break;
            // =====================================================================
            case MONITOR_MESSAGE_STOP:
                // -----------------------------------------------------------------
                // 16/12/2019 ECU it is time to stop the monitoring process
                // -----------------------------------------------------------------
                monitorActivated = false;
                // -----------------------------------------------------------------
                // 16/12/2019 ECU remove any messages that may be queued
                // 17/12/2019 ECU added ....SENDING and ....WAITING
                // -----------------------------------------------------------------
                this.removeMessages (MONITOR_MESSAGE_EMAIL_SENDING);
                this.removeMessages (MONITOR_MESSAGE_EMAIL_WAITING);
                this.removeMessages (MONITOR_MESSAGE_LISTEN);
                // -----------------------------------------------------------------
                // 16/12/2019 ECU indicate that monitoring has finished
                // -----------------------------------------------------------------
                updateImage (R.drawable.audio_analyser);
                // -----------------------------------------------------------------
                // 22/12/2019 ECU restore the screen brightness
                // -----------------------------------------------------------------
                setScreenBrightness (brightness);
                // -----------------------------------------------------------------
                // 16/12/2019 ECU if the monitoring is still enabled then want to
                //                set up the 'next days' times
                // -----------------------------------------------------------------
                if (PublicData.storedData.monitor.enabled)
                {
                    // -------------------------------------------------------------
                    // 16/12/2019 ECU now send the message to set up the correct times
                    // -------------------------------------------------------------
                    sendEmptyMessage (MONITOR_MESSAGE_INITIALISE);
                    // -------------------------------------------------------------
                }
                // -----------------------------------------------------------------
                break;
            // =====================================================================
            case MONITOR_MESSAGE_TRIGGERED:
                // -----------------------------------------------------------------
                // 16/12/2019 ECU a 'noise' that is above the trigger level has been
                //                heard so need to do the following :-
                //                  1 ..... process any actions that have been
                //                          defined
                //                  2 ..... start up the 'timed recorder' if enabled
                // -----------------------------------------------------------------
                // 16/12/2019 ECU process any defined actions
                // -----------------------------------------------------------------
                Utilities.actionHandler (context,PublicData.storedData.monitor.actions);
                // -----------------------------------------------------------------
                // 16/12/2019 ECU start up the 'timed recorder' if enabled
                // -----------------------------------------------------------------
                if (PublicData.storedData.monitor.timedRecording)
                {
                    // -------------------------------------------------------------
                    // 16/12/2019 ECU indicate that the recorder is to start
                    // 17/12/2019 ECU if an external activity has the microphone
                    //                then cannot start the 'recording'
                    // -------------------------------------------------------------
                    if (!doNotRecord)
                    {
                        // ---------------------------------------------------------
                        // 17/12/2019 ECU can record
                        // ---------------------------------------------------------
                        sendEmptyMessage (MONITOR_MESSAGE_RECORD_START);
                        // ---------------------------------------------------------
                    }
                    // -------------------------------------------------------------
                }
                else
                {
                    // -------------------------------------------------------------
                    // 16/12/2019 ECU nothing else to do so indicate 'finished'
                    // -------------------------------------------------------------
                    sendEmptyMessage (MONITOR_MESSAGE_FINISHED);
                    // -------------------------------------------------------------
                }
                // -----------------------------------------------------------------
                break;
            // =====================================================================
            default:
                break;
            // =====================================================================
        }
    }
    // =============================================================================

    // =============================================================================
    public static void AlarmReceived (int theType)
    {
        // -------------------------------------------------------------------------
        // 20/12/2019 ECU created to handle received alarms
        // -------------------------------------------------------------------------
        if (PublicData.monitorHandler != null)
        {
            // ---------------------------------------------------------------------
            // 20/12/2019 ECU send the message that is contained in the alarm
            // ---------------------------------------------------------------------
            PublicData.monitorHandler.sendEmptyMessage (theType);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    boolean checkForANoise (int theNoiseThreshold)
    {
        // -------------------------------------------------------------------------
        // 29/04/2015 ECU created to wait until the noise threshold is reached
        // -------------------------------------------------------------------------
        // 29/04/2015 ECU declare any local data
        // -------------------------------------------------------------------------
        short [] 	localBuffer					= null;
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
        localAudioRecord.startRecording ();
        // -------------------------------------------------------------------------
        // 29/04/2015 ECU loop until a noise is heard
        // 17/12/2019 ECU added the check on 'doNotRecord'
        // -------------------------------------------------------------------------
        while (monitorActivated && !doNotRecord)
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
                        localAudioRecord.stop ();
                        localAudioRecord.release ();
                        // ---------------------------------------------------------
                        // 29/04/2015 ECU return to indicate threshold hit
                        // ---------------------------------------------------------
                        return true;
                        // ---------------------------------------------------------
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
        // 29/04/2015 the looping has been stopped
        // -------------------------------------------------------------------------
        localAudioRecord.stop ();
        localAudioRecord.release ();
        // -------------------------------------------------------------------------
        // 2904/2015 ECU indicate that noise not found just an abnormal exception
        // -------------------------------------------------------------------------
        return false;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void ExternalRequestForResources (boolean theRequest)
    {
        // -------------------------------------------------------------------------
        if (PublicData.monitorHandler != null)
        {
            // ---------------------------------------------------------------------
            PublicData.monitorHandler.externalRequestForResources (theRequest);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void externalRequestForResources (boolean theRequest)
    {
        Utilities.LogToProjectFile (TAG,"externalRequestForResources : " + theRequest);
        // -------------------------------------------------------------------------
        // 17/12/2019 ECU an external activity wants to use resources being used
        //                by this handler
        //                  theRequest ...... true ..... want to obtain resources
        //                                    false .... want to release resource
        // -------------------------------------------------------------------------
        if (theRequest)
        {
            // ---------------------------------------------------------------------
            // 17/12/2019 ECU the only resource that an external activity may want
            //                is the microphone - this may be in use if this handler
            //                is recording
            // ---------------------------------------------------------------------
            // 17/12/2019 ECU tell the recorder to stop
            // ---------------------------------------------------------------------
            if (record)
            {
                // -----------------------------------------------------------------
                // 17/12/2019 ECU tell the recorder to stop
                // -----------------------------------------------------------------
                record = false;
                // -----------------------------------------------------------------
                // 17/12/2019 ECU remove any related messages
                // -----------------------------------------------------------------
                removeMessages (MONITOR_MESSAGE_RECORD_START);
                removeMessages (MONITOR_MESSAGE_RECORD_STOP);
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
            // 17/12/2019 ECU indicate that microphone is being used by another
            //                activity
            // ---------------------------------------------------------------------
            doNotRecord = true;
            // ---------------------------------------------------------------------
        }
        else
        {
            // ---------------------------------------------------------------------
            // 17/12/2019 ECU indicate that the external activity has released
            //                resources
            // ---------------------------------------------------------------------
            doNotRecord = false;
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void initialiseVariables ()
    {
        // -------------------------------------------------------------------------
        // 18/12/2019 ECU initialise some variables which have to be done from >1
        //                position
        // -------------------------------------------------------------------------
        initialised = false;
        monitor     = PublicData.storedData.monitor;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static String MessageName (int theMessage)
    {
        // -------------------------------------------------------------------------
        // 24/12/2019 ECU return the name of the specified message
        // -------------------------------------------------------------------------
        try
        {
            // ---------------------------------------------------------------------
            // 24/12/2019 ECU return the entry in the names array that corresponds
            //                to the message
            // ---------------------------------------------------------------------
            return MONITOR_MESSAGE [theMessage];
            // ---------------------------------------------------------------------
        }
        catch (ArrayIndexOutOfBoundsException theException)
        {
            // ---------------------------------------------------------------------
            // 24/12/2019 ECU an exception, e.g. index wrong, occurred so just
            //                return the input argument as a string
            // ---------------------------------------------------------------------
            return String.valueOf (theMessage);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void PauseMonitoring ()
    {
        // -------------------------------------------------------------------------
        // 17/12/2019 ECU pause monitoring if defined
        // -------------------------------------------------------------------------
        if (PublicData.monitorHandler != null)
        {
            PublicData.monitorHandler.pauseMonitoring ();
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void pauseMonitoring ()
    {
        // -------------------------------------------------------------------------
        // 17/12/2019 ECU pause any monitoring that is happening
        // -------------------------------------------------------------------------
        Utilities.LogToProjectFile (TAG,"pauseMonitoring");
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void Restart ()
    {
        // -------------------------------------------------------------------------
        // 17/12/2019 ECU create to be called when this handler is to be stopped
        // -------------------------------------------------------------------------
        if (PublicData.monitorHandler != null)
        {
            // ---------------------------------------------------------------------
            // 18/12/2019 ECU initialise some common variables
            // ---------------------------------------------------------------------
            PublicData.monitorHandler.initialiseVariables ();
            // ---------------------------------------------------------------------
            // 18/12/2019 ECU indicate that the handler is to be stopped
            // 20/12/2019 ECU the terminate will also cause a reinitialisation
            // ---------------------------------------------------------------------
            Terminate ();
            // ---------------------------------------------------------------------
            // 18/12/2019 ECU clear any outstanding messages
            // ---------------------------------------------------------------------
            PublicData.monitorHandler.removeMessages (MONITOR_MESSAGE_LISTEN);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void ResumeMonitoring ()
    {
        // -------------------------------------------------------------------------
        // 17/12/2019 ECU resume monitoring if defined
        // -------------------------------------------------------------------------
        if (PublicData.monitorHandler != null)
        {
            PublicData.monitorHandler.resumeMonitoring ();
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void resumeMonitoring ()
    {
        // -------------------------------------------------------------------------
        // 17/12/2019 ECU resume any monitoring that is happening
        // -------------------------------------------------------------------------
        Utilities.LogToProjectFile (TAG,"resumeMonitoring");
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void setScreenBrightness (float theBrightness)
    {
        // -------------------------------------------------------------------------
        // 22/12/2019 ECU set the brightness of the GridActivity activity
        //            ECU remember the brightness before being changed
        //            ECU put in the check on NOT_SET
        // -------------------------------------------------------------------------
        Utilities.setScreenBrightness (GridActivity.activity,theBrightness);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void Terminate ()
    {
        // -------------------------------------------------------------------------
        // 17/12/2019 ECU create to be called when this handler is to be stopped
        // -------------------------------------------------------------------------
        if (PublicData.monitorHandler != null)
        {
            // ---------------------------------------------------------------------
            PublicData.monitorHandler.sendEmptyMessage (MONITOR_MESSAGE_STOP);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void timedRecording (final String theFileName)
    {
        Thread timedRecordingThread = new Thread()
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
                    if (localAudioRecord.getState () == AudioRecord.STATE_INITIALIZED)
                    {
                        // ---------------------------------------------------------
                        // 25/11/2014 ECU create the buffer to receive the audio
                        // ---------------------------------------------------------
                        byte [] bytes = new byte [minimumBufferSize];
                        // ---------------------------------------------------------
                        // 11/10/2013 ECU start the recording process
                        // ---------------------------------------------------------
                        localAudioRecord.startRecording ();
                        // ---------------------------------------------------------
                        // 16/12/2019 ECU indicate that the recorder is to continue
                        //                until this flag is reset by ....STOP
                        // ---------------------------------------------------------
                        record = true;
                        // ---------------------------------------------------------
                        // 16/12/2019 ECU set up the message which will indicate when
                        //                the recorder is to stop
                        // ---------------------------------------------------------
                        sendEmptyMessageDelayed(MONITOR_MESSAGE_RECORD_STOP,
                            PublicData.storedData.monitor.duration * StaticData.MILLISECONDS_PER_MINUTE);
                        // ---------------------------------------------------------
                        // 10/12/2019 ECU log the state
                        // ---------------------------------------------------------
                        Utilities.LogToProjectFile (TAG,"Recording to \'" + theFileName + "\'");
                        // ---------------------------------------------------------
                        // 28/04/2015 ECU put in the interrupted check
                        // 30/04/2015 ECU changed to check keepRunning rather than
                        //                interrupt
                        // ----------------------------------------------------------
                        while (record)
                        {
                            int numberOfBytesRead = localAudioRecord.read (bytes, 0, minimumBufferSize);

                            if (numberOfBytesRead != AudioRecord.ERROR_INVALID_OPERATION)
                            {
                                randomAccessFile.write (bytes, 0, numberOfBytesRead);
                            }
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
                        // 03/07/2020 ECU removed a ' * 1'
                        // ---------------------------------------------------------
                        randomAccessFile.write
                                (AudioRecorder.GetWaveFileHeader (audioLength,
                                        dataLength,
                                        AudioRecorder.RECORDER_SAMPLERATE,
                                        1,
                                        (16 * AudioRecorder.RECORDER_SAMPLERATE)/8));
                        // ---------------------------------------------------------
                        // 11/12/2019 ECU Note - stop the recorder and release any resources
                        // ---------------------------------------------------------
                        localAudioRecord.stop();
                        localAudioRecord.release();
                        // ---------------------------------------------------------
                        // 11/12/2019 ECU Note - close the audio file
                        // ---------------------------------------------------------
                        randomAccessFile.close();
                        // ---------------------------------------------------------
                        // 16/12/2019 ECU check if the file is to emailed
                        // ---------------------------------------------------------
                        if (PublicData.storedData.monitor.email)
                        {
                            // -----------------------------------------------------
                            // 16/12/2019 ECU trigger the email and pass over the
                            //                name of file to be sent
                            // -----------------------------------------------------
                            Message localMessage = obtainMessage (MONITOR_MESSAGE_EMAIL,
                                                            noisesDirectory + theFileName);
                            sendMessage (localMessage);
                            // -----------------------------------------------------
                        }
                        else
                        {
                            // -----------------------------------------------------
                            // 16/12/2019 ECU indicate that everything is done
                            // -----------------------------------------------------
                            sendEmptyMessage (MONITOR_MESSAGE_FINISHED);
                            // -----------------------------------------------------
                        }
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
        timedRecordingThread.start ();
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void TransmissionFailure ()
    {
        // -------------------------------------------------------------------------
        // 16/12/2019 ECU the public 'transmission failure'
        // -------------------------------------------------------------------------
        if (PublicData.monitorHandler != null)
        {
            PublicData.monitorHandler.transmissionFailure ();
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void transmissionFailure ()
    {
        // -------------------------------------------------------------------------
        // 16/12/2019 ECU this is called when the email being sent has a transmission
        //                error - check if activated
        // -------------------------------------------------------------------------
        if (monitorActivated)
        {
            // ---------------------------------------------------------------------
            // 16/12/2019ECU indicate that a transmission failure has occurred
            // ---------------------------------------------------------------------
            sendEmptyMessage (MONITOR_MESSAGE_FAILURE);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void updateImage (int theResourceID)
    {
        // -------------------------------------------------------------------------
        // 28/04/2015 ECU check if the image has changed
        // 23/12/2019 ECU changed from static
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
    // =============================================================================
    public static void UpdateImageView (ImageView theImageView)
    {
        // -------------------------------------------------------------------------
        // 17/12/2019 ECU created to check if handler exists before updating the
        //                view
        // -------------------------------------------------------------------------
        if (PublicData.monitorHandler != null)
        {
            PublicData.monitorHandler.updateImageView (theImageView);
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void updateImageView (ImageView theImageView)
    {
        // -------------------------------------------------------------------------
        // 28/04/2015 ECU created to update the image view depending on whether
        //                the service is running or not
        // -------------------------------------------------------------------------
        theImageView.setImageResource (iconID);
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
}
// =================================================================================

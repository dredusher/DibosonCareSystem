package com.usher.diboson;

import android.content.Context;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class MonitorPhoneStatus extends PhoneStateListener
{
	/* ============================================================================= */
	// 15/02/2014 ECU created - monitor the state of the phone
	// 28/11/2016 ECU seem to be some issues with the Sony XA - see Notes
	/* ============================================================================= */
	AudioManager	 audioManager;
	Context		     context;
	TelephonyManager telephonyManager;    
	/* ============================================================================= */
	public MonitorPhoneStatus (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 15/02/2014 ECU copy across any supplied variables
		// -------------------------------------------------------------------------
		audioManager	 = (AudioManager) theContext.getSystemService (Context.AUDIO_SERVICE);  
		context			 =	theContext;
		telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)); 
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
    public void onCallStateChanged (int phoneState, String incomingNumber)
    {
    	// -------------------------------------------------------------------------
    	// 15/02/2014 ECU this method is called when the status of the phone changes
    	// -------------------------------------------------------------------------
    	// 15/02/2014 ECU initially call the main handler
    	// -------------------------------------------------------------------------
    	super.onCallStateChanged (phoneState, incomingNumber);
    	// -------------------------------------------------------------------------
    	// 15/02/2014 ECU the possible states are
    	// -------------------------------------------------------------------------
    	//                CALL_STATE_IDLE       No activity.
    	//				  CALL_STATE_OFFHOOK    Off-hook. At least one call exists that is dialling, 
    	//                                      active, or on hold, and no calls are ringing or waiting.
    	//				  CALL_STATE_RINGING	Ringing. A new call arrived and is ringing or waiting. 
    	//                                      In the latter case, another call is already active.
    	// -------------------------------------------------------------------------
    	// 15/02/2014 ECU depending on the state decide on the action to take
    	// 14/04/2019 ECU need to take into account the use Alexa which may be
    	//                trying to communicate to a connected bluetooth speaker
    	// -------------------------------------------------------------------------
        switch (phoneState) 
        {
        	// ------------------------------------------------------------------
        	case TelephonyManager.CALL_STATE_IDLE:
        		// --------------------------------------------------------------
        		// 15/02/2014 ECU want to switch the speaker phone off
        		// 14/04/2019 ECU if Alexa is trying to use communication to
        		//                the attached bluetooth speaker then do not change
        		//                the 'idle' state
        		// --------------------------------------------------------------
        		if (!Alexa.enabledOnBluetooth)
        		{
        			// ----------------------------------------------------------
        			// 14/04/2019 ECU only change state if Alexa has NOT configured
        			//                the audio manager
        			// ----------------------------------------------------------
        			audioManager.setMode (AudioManager.MODE_NORMAL); 
        			audioManager.setSpeakerphoneOn (false);
        			// ----------------------------------------------------------
        		}
        		// --------------------------------------------------------------
    			break;
            // ------------------------------------------------------------------
        	case TelephonyManager.CALL_STATE_OFFHOOK:
        		// --------------------------------------------------------------       		
        		// 15/02/2014 ECU want to force the speaker phone to be on
        		// --------------------------------------------------------------
        		audioManager.setMode (AudioManager.MODE_IN_CALL);
    			audioManager.setSpeakerphoneOn (true); 
    			// --------------------------------------------------------------
        		break;
            // ------------------------------------------------------------------
        	case TelephonyManager.CALL_STATE_RINGING:
        		// --------------------------------------------------------------
        		// 15/02/2014 ECU at this stage do not want to do anything for
        		//                incoming calls
        		// --------------------------------------------------------------
            	break;
            // ------------------------------------------------------------------
        }
    }
	/* ========================================================================== */
}


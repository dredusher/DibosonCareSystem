package com.usher.diboson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.widget.TextView;
import android.widget.Toast;

// =================================================================================
public class MessageHandler extends Handler
{
	// -----------------------------------------------------------------------------
	// 10/09/2015 ECU created to provide a general message handling facility
	// -----------------------------------------------------------------------------
	// 21/12/2015 ECU declare any variables that are required
	//            ECU changed to use String []
	//                in which element [0] = the initial prompt
	//                                 [1] = subsequent prompts
	// 03/06/2016 ECU add constructor to pass context as an argument so that can
	//                take out the use of MainActivity.activity
	// 17/06/2016 ECU added handling that is required by NFC activity
	// 20/08/2016 ECU added the elapsed time handler
	// 28/11/2016 ECU added the timeout message handler
	// 24/12/2016 ECU bit more testing of timeout code before commenting out
	// 28/12/2016 ECU added _METHOD
	// 21/01/2017 ECU added MESSAGE_DISPLAY_DATE_ ......
	// 31/01/2017 ECU added MESSAGE_DISPLAY_DRAWABLE
	//            ECU added MESSAGE_DISPLAY_TEXT
	// 12/03/2017 ECU added MESSAGE_PLAY_RAW and asociated methods for calling
	// -----------------------------------------------------------------------------
	String []  				actions;									// 03/06/2016 ECU added
	int						actionPointer		= StaticData.NO_RESULT;	// 03/06/2016 ECU added
	DateTimeTextView		dateTimeTextView	= null;					// 21/02/2017 ECU added
	String [] 				dosePrompts			= null;	
	int						dosePromptCounter	= 0;
	int     				dosePromptDelay;
	long					elapsedTime;								// 20/08/2016 ECU added
	SimpleDateFormat		elapsedTimeFormat;							// 20/08/2016 ECU added
	int						elapsedTimeInterval;						// 20/08/2016 ECU added
	TextView				elapsedTimeTextView	= null;					// 20/08/2016 ECU added
	MessageHandler_Message	nfcMessage			= null;					// 17/06/2016 ECU added
	// -----------------------------------------------------------------------------
    @Override
    public void handleMessage (Message theMessage) 
    {   
       	// -------------------------------------------------------------------------
    	// 05/05/2015 ECU change to switch on the type of message received
    	//                which is in '.what'
    	// -------------------------------------------------------------------------
    	switch (theMessage.what)
    	{
    		// =====================================================================
    		case StaticData.MESSAGE_ACTION_FINISHED:
    			// -----------------------------------------------------------------
    			// 03/06/2016 ECU created to be called when a speech phrase has
    			//                finished
    			// -----------------------------------------------------------------
    			if (actionPointer != StaticData.NO_RESULT)
    			{
    				sendEmptyMessage (StaticData.MESSAGE_PROCESS_FINISHED);
    			}
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_ANYTHING_PLAYING:
    			// -----------------------------------------------------------------
    			// 24/10/2016 ECU called to check if any actions are in existence or
    			//                if there is any audio playing
    			// 25/10/2016 ECU add the 'paused' check
    			// -----------------------------------------------------------------
    			if ((PublicData.mediaPlayer != null && PublicData.mediaPlayer.isPlaying()) ||
    				(PublicData.actions.size() > 0) ||
    				PublicData.mediaPlayerPaused)
    			{
    				sendMessageDelayed (obtainMessage (StaticData.MESSAGE_ANYTHING_PLAYING),500);
    			}
    			else
    			{
    				AppointmentsActivity.playingFinished ();
    			}
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_CHECK_DEVICE:
    			// -----------------------------------------------------------------
    			// 17/06/2016 ECU created to send a socket message to the specified
    			//                and wait for a response
    			// -----------------------------------------------------------------
    			nfcMessage = (MessageHandler_Message) theMessage.obj;
    			// -----------------------------------------------------------------
    			Utilities.socketMessagesSendMessageType (MainActivity.activity,
    													 nfcMessage.deviceIPAddress,
    													 PublicData.socketNumberForData,
    													 StaticData.SOCKET_MESSAGE_ARE_YOU_THERE);
    			// -----------------------------------------------------------------
    			// 17/06/2016 ECU start up the timeout - the time out in mS is
    			//                supplied in the message
    			// -----------------------------------------------------------------
    			this.removeMessages (StaticData.MESSAGE_CHECK_DEVICE_TIMEOUT);
				sendMessageDelayed (obtainMessage (StaticData.MESSAGE_CHECK_DEVICE_TIMEOUT),nfcMessage.timeout);
				// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_CHECK_DEVICE_RESP:
    			// -----------------------------------------------------------------
    			// 17/06/2016 ECU created to send a socket message to the specified
    			//                and wait for a response
    			// -----------------------------------------------------------------
    			// 17/06/2016 ECU cancel any timeout messages
    			// -----------------------------------------------------------------
    			this.removeMessages (StaticData.MESSAGE_CHECK_DEVICE_TIMEOUT);
    			// -----------------------------------------------------------------
    			// 17/06/2016 ECU indicate response received
    			// -----------------------------------------------------------------
    			nfcMessage.process (true);
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_CHECK_DEVICE_TIMEOUT:
    			// -----------------------------------------------------------------
    			// 17/06/2016 ECU created to respond to a timeout occurring
    			// -----------------------------------------------------------------
    			// 17/06/2016 ECU indicate response timeout occurred
    			// -----------------------------------------------------------------
    			nfcMessage.process (false);
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_DISPLAY_DATE_REFRESH:
    			// -----------------------------------------------------------------
    			// 21/01/2017 ECU created to cause a refresh of the displayed date
    			//                and time
    			// -----------------------------------------------------------------
    			dateTimeTextView.display ();
    			// -----------------------------------------------------------------
    			// 21/01/2017 ECU put in the delayed message to get the display refreshed
    			// -----------------------------------------------------------------
    			this.sendEmptyMessageDelayed (StaticData.MESSAGE_DISPLAY_DATE_REFRESH,dateTimeTextView.updateInterval);
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_DISPLAY_DATE_START:
    			// -----------------------------------------------------------------
    			// 21/01/2017 ECU created to initiate the display of the current
    			//                date and time in a specified text view
    			//            ECU check if the display is already in progress
    			// -----------------------------------------------------------------
    			if (dateTimeTextView == null)
    			{
    				// -------------------------------------------------------------
    				// 21/01/2017 ECU get the information from the incoming message
    				// -------------------------------------------------------------
    				dateTimeTextView = (DateTimeTextView) theMessage.obj;
    				// -------------------------------------------------------------
    				// 21/01/2017 ECU now start the display handling
    				// -------------------------------------------------------------
    				this.sendEmptyMessage (StaticData.MESSAGE_DISPLAY_DATE_REFRESH);
    				// -------------------------------------------------------------
    			}
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_DISPLAY_DATE_STOP:
    			// -----------------------------------------------------------------
    			// 21/01/2017 ECU created to stop the display of the date and time
    			// -----------------------------------------------------------------
    			if (dateTimeTextView != null)
    			{
    				// -------------------------------------------------------------
    				// 21/01/2017 ECU make sure that there are no more refreshes
    				// -------------------------------------------------------------
    				this.removeMessages (StaticData.MESSAGE_DISPLAY_DATE_REFRESH);
    				// -------------------------------------------------------------
    				// 21/01/2017 ECU redisplay the original text
    				// 25/01/2017 ECU arg1 contains the required visibility
    				// -------------------------------------------------------------
    				dateTimeTextView.originalDisplay (theMessage.arg1);
    				// -------------------------------------------------------------
    				// 21/01/2017 ECU indicate no more updating wanted
    				// -------------------------------------------------------------
    				dateTimeTextView = null;
    				// -------------------------------------------------------------
    			}
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_DISPLAY_DRAWABLE:
    			// -----------------------------------------------------------------
    			// 31/01/2017 ECU created to display a 'full screen' drawable image
    			// -----------------------------------------------------------------
    			Utilities.popToastFullScreen (theMessage.arg1);
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_DISPLAY_TEXT:
    			// -----------------------------------------------------------------
    			// 31/01/2017 ECU created to display a 'full screen' with text
    			// -----------------------------------------------------------------
    			Utilities.popToastFullScreen ((String) theMessage.obj);
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_ELAPSED_TIME_REFRESH:
    			// -----------------------------------------------------------------
    			// 20/08/2016 ECU created to update the elapsed time display
    			// -----------------------------------------------------------------
    			elapsedTimeTextView.setText (elapsedTimeFormat.format (SystemClock.elapsedRealtime () - elapsedTime));
    			// -----------------------------------------------------------------
    			// 20/08/2016 ECU now set up the next message
    			// -----------------------------------------------------------------
    			this.sendEmptyMessageDelayed (StaticData.MESSAGE_ELAPSED_TIME_REFRESH,elapsedTimeInterval);
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_ELAPSED_TIME_START:
    			// -----------------------------------------------------------------
    			// 20/08/2016 ECU created to start the display of an elapsed time
    			// -----------------------------------------------------------------
    			elapsedTimeInterval	= theMessage.arg1;
    			elapsedTimeTextView = (TextView) theMessage.obj;
    			// -----------------------------------------------------------------
    			// 20/08/2016 ECU set up the time format that will be used for display
    			// -----------------------------------------------------------------
    			elapsedTimeFormat = new SimpleDateFormat (StaticData.ALARM_TIME_FORMAT,Locale.getDefault());
    			elapsedTimeFormat.setTimeZone (TimeZone.getTimeZone ("UTC"));
    			// -----------------------------------------------------------------
    			// 20/08/2016 ECU store the initial time
    			// -----------------------------------------------------------------
    			elapsedTime = SystemClock.elapsedRealtime ();
    			// -----------------------------------------------------------------
    			// 20/08/2016 ECU now initiate the refresh of the display
    			// -----------------------------------------------------------------
    			this.sendEmptyMessage (StaticData.MESSAGE_ELAPSED_TIME_REFRESH);
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_ELAPSED_TIME_STOP:
    			// -----------------------------------------------------------------
    			// 20/08/2016 ECU created to stop the display of an elapsed time
    			// -----------------------------------------------------------------
    			if (elapsedTimeTextView != null)
    			{
    				elapsedTimeTextView.setText ("");
    				// -------------------------------------------------------------
    				// 20/08/2016 ECU make sure that there are no more refreshes
    				// -------------------------------------------------------------
    				this.removeMessages (StaticData.MESSAGE_ELAPSED_TIME_REFRESH);
    				// -------------------------------------------------------------
    				// 20/08/2016 ECU clear the text view
    				// -------------------------------------------------------------
    				elapsedTimeTextView = null;
    				// -------------------------------------------------------------
    			}
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_LIST_FINISHED:
    			// -----------------------------------------------------------------
    			// 03/06/2016 ECU created to handle processing when a 'list of
    			//                actions' is complete
    			// -----------------------------------------------------------------
				// 03/06/2016 ECU indicate all actions have been processed
				// -----------------------------------------------------------------
				actionPointer = StaticData.NO_RESULT;
				// -----------------------------------------------------------------
				// 03/06/2016 ECU remove the top of the list and if the list is 
				//                not empty then process the 'new top of the 
				//                list'
				// 24/10/2016 ECU do a check on size just in case the actions have
				//                been cleared
				// -----------------------------------------------------------------
				if (PublicData.actions.size () > 0)
					PublicData.actions.remove (0);
				// -----------------------------------------------------------------
				// 24/10/2016 ECU Note - check if there is anything left in the
				//                       actions list
				// -----------------------------------------------------------------
				if (PublicData.actions.size() > 0)
				{
					// -------------------------------------------------------------
					// 03/06/2016 ECU the list is not empty so restart the
					//                processing
					// -------------------------------------------------------------
					sendEmptyMessage (StaticData.MESSAGE_PROCESS_ACTIONS);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 03/06/2016 ECU all of the lists have been processed
					// -------------------------------------------------------------
	  				// 03/06/2016 ECU check if using the 'dialogue' activity - if so
    				//                then want to get the next phrase
					// 28/11/2016 ECU need a slight delay before telling the 'dialogue'
					//                to display the microphone
    				// -------------------------------------------------------------
    				if (Dialogue.textToSpeech != null)
    					this.sendEmptyMessageDelayed(StaticData.MESSAGE_PROCESS_DELAYED,5000);	
    				// -------------------------------------------------------------
				}
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_METHOD:
    			// -----------------------------------------------------------------
    			// 28/12/2016 ECU created to call the method that is passed as the
    			//                object in the message
    			//            ECU argument 1 is passed back, as received, in case it
    			//                is wanted as an identified
    			// -----------------------------------------------------------------
				try
				{
					// -------------------------------------------------------------
					// 22/12/2016 ECU invoke the method whose definition in passed
					//                as the object in the message
					// -------------------------------------------------------------
					(((MethodDefinition<?>) theMessage.obj).ReturnMethod (0)).invoke (null, new Object [] {theMessage.arg1});
					// -------------------------------------------------------------
				}
				catch (Exception theException)
				{
					
				}
				// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_MONITOR:
    			// -----------------------------------------------------------------
    			// 26/04/2016 ECU created to check if monitor data is to be sent
    			// -----------------------------------------------------------------
    			if (PublicData.monitorData.size() > 0)
    			{
    				Utilities.sendSocketMessageSendTheObject (MainActivity.activity,
    														  PublicData.monitorIPAddress,
    														  PublicData.socketNumberForData,
    														  StaticData.SOCKET_MESSAGE_MONITOR_DATA,
    														  PublicData.monitorData.get(0));
    				// -------------------------------------------------------------
    				// 26/04/2016 ECU remove the entry at the head of the list
    				// -------------------------------------------------------------
    				PublicData.monitorData.remove (0);
    				// -------------------------------------------------------------
    				// 26/04/2016 ECU wait a bit before trying again
    				// -------------------------------------------------------------
    				this.removeMessages (StaticData.MESSAGE_MONITOR);
    				sendMessageDelayed (obtainMessage (StaticData.MESSAGE_MONITOR),500);
    				// -------------------------------------------------------------
    			}
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_PLAY_RAW:
    			// -----------------------------------------------------------------
    			// 12/03/2017 ECU called to play the 'raw' file with parameters
    			//                supplied in the message
    			//
    			//                   arg1 ...... resource ID
    			//                   arg2 ...... loop counter
    			//                   obj  ...... loop position (int)
    			// -----------------------------------------------------------------
    			Utilities.playRawResource(MainActivity.activity,
    					                  theMessage.arg1, 
    					                  theMessage.arg2, 
    					                  (Integer)theMessage.obj);
    			// -----------------------------------------------------------------
    			break;
            // =====================================================================
    		case StaticData.MESSAGE_PROCESS_ACTION:
    			// -----------------------------------------------------------------
    			// 03/06/2016 ECU created to process an individual action with the
    			//                parameters specified in the message
    			// -----------------------------------------------------------------
    			String [] localElements = actions [actionPointer].split (StaticData.ACTION_DELIMITER);
    			// -----------------------------------------------------------------
    			// 03/06/2016 ECU the following method will return 'false' if can
    			//                continue immediately or 'true' if need to wait
    			//                for the last action to finish
    			// -----------------------------------------------------------------
    			if (!Utilities.processAnAction (MainActivity.activity,localElements))
    			{
    				// -------------------------------------------------------------
    				// 03/06/2016 ECU check if need to move to the next action if one
    				//                exists
    				// -------------------------------------------------------------
    				if (++actionPointer < actions.length)
    				{	
    					// ---------------------------------------------------------
    					// 03/06/2016 ECU process the next action
    					// ---------------------------------------------------------
    					sendEmptyMessage (StaticData.MESSAGE_PROCESS_ACTION);
    					// ---------------------------------------------------------
    				}
    				else
    				{
    					// ---------------------------------------------------------
    					// 03/06/2016 ECU indicate all actions have been processed
    					// ----------------------------------------------------------
    					sendEmptyMessage (StaticData.MESSAGE_LIST_FINISHED);
    					// ---------------------------------------------------------
    				}
    			}
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_PROCESS_ACTIONS:
    			// -----------------------------------------------------------------
    			// 03/06/2016 ECU called up to process the string of actions which
    			//                are supplied as an argument in the message
    			//            ECU changed because the data is now stored in a list
    			//                array to handle the receipt of a new set of actions
    			//                while a set is currently being processed
    			// -----------------------------------------------------------------
    			// 03/06/2016 ECU check if a 'set of actions' is currently being
    			//                processed - if processing is in progress then
    			//                no need to do anything
    			// -----------------------------------------------------------------
    			if (actionPointer == StaticData.NO_RESULT)
    			{
    				// -------------------------------------------------------------
    				// 03/06/2016 ECU no processing in place so start the set that is
    				//                at the top of the list
    				// -------------------------------------------------------------
    				actions 	    = PublicData.actions.get (0);
    				actionPointer   = 0;
    				// -------------------------------------------------------------
    				// 03/06/2016 ECU get the first action processed
    				// -------------------------------------------------------------
    				sendEmptyMessage (StaticData.MESSAGE_PROCESS_ACTION);
    				// -------------------------------------------------------------
    				// 28/11/2016 ECU start up the timeout monitor
    				//            ECU initialise the saved value of the action pointer
    				//            ECU commented out - not sure it is needed
    				// 24/12/2016 ECU did a bit more testing
    				// -------------------------------------------------------------
    				//if (!hasMessages (StaticData.MESSAGE_PROCESS_ACTIONS_TIMEOUT))
    				//{
    				//	savedActionPointer	= StaticData.NO_RESULT;
    				//	sendEmptyMessage (StaticData.MESSAGE_PROCESS_ACTIONS_TIMEOUT);
    				//}
    				// -------------------------------------------------------------
    			}
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_PROCESS_ACTIONS_IMMEDIATELY:
    			// -----------------------------------------------------------------
    			// 04/08/2016 ECU created to be called when a set of actions are to
    			//                be processed immediately taking precedence over
    			//                anything that is current queued
    			// -----------------------------------------------------------------
    			String [] localActions = (String []) theMessage.obj;
    			// -----------------------------------------------------------------
    			// 04/08/2016 ECU reconstruct the actions array
    			//            ECU the '+1' is to jump over the current one that
    			//                caused this message to be sent
    			// -----------------------------------------------------------------
    			String[] localRemainingActions = Arrays.copyOfRange (actions,actionPointer + 1,actions.length);
    			// -----------------------------------------------------------------
    			// 04/08/2016 ECU rebuild the 'actions' array
    			// -----------------------------------------------------------------
    			actions = new String [localActions.length + localRemainingActions.length];
    			// -----------------------------------------------------------------
    			System.arraycopy (localActions,          0, actions, 0,                   localActions.length);
    			System.arraycopy (localRemainingActions, 0, actions, localActions.length, localRemainingActions.length);
    			// -----------------------------------------------------------------
    			// 04/08/2016 ECU reset the pointer which will be increment by 1
    			//                before being used - see ...FINISHED below
    			// ------------------------------------------------------------------
    			actionPointer = -1;
    			// -----------------------------------------------------------------
    			// 04/08/2016 ECU now get the processing restarted
    			// -----------------------------------------------------------------
    			sendEmptyMessage (StaticData.MESSAGE_PROCESS_FINISHED);
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_PROCESS_ACTIONS_TIMEOUT:
    			//// -----------------------------------------------------------------
    			//// 28/11/2016 ECU created to check whether any of the actions being
    			////                processed has timed out
    			////            ECU commented out not sure it is needed
    			//// -----------------------------------------------------------------
    			//// 28/11/2016 ECU check if the processing has hung
    			//// -----------------------------------------------------------------
    			////if (savedActionPointer != StaticData.NO_RESULT)
    			//{
    			//	// -------------------------------------------------------------
    			//	// 28/11/2016 ECU check if the pointer is 'stuck'
    			//	// -------------------------------------------------------------
    			//	if ((actionPointer == savedActionPointer) && (PublicData.actions.size() > 0))
    			//	{
    			//		// ---------------------------------------------------------
    			//		// 28/11/2016 ECU may be that action handling has got stuck
    			//		//                need a bit more thought here
    			// 		// ---------------------------------------------------------
    			//		
    			//		// ---------------------------------------------------------
    			//	}
    			//	// -------------------------------------------------------------
    			//}
    			//// ---------------------------------------------------------------
    			//// 28/11/2016 ECU save the current action pointer
    			//// ---------------------------------------------------------------
    			//savedActionPointer = actionPointer;
    			//// ---------------------------------------------------------------
    			//// 28/11/2016 ECU just keep looping if there are still actions
    			//// ---------------------------------------------------------------
    			//if (PublicData.actions.size() > 0)
    			//{
    			//	this.sendEmptyMessageDelayed(StaticData.MESSAGE_PROCESS_ACTIONS_TIMEOUT, StaticData.ACTIONS_TIMEOUT);
    			//}
    			//// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_PROCESS_DELAYED:
    			// -----------------------------------------------------------------
    			// 28/11/2016 ECU now tell the 'dialogue' activity to display the
    			//                microphone and continue with the dialogue
    			// -----------------------------------------------------------------
    			Utilities.GetAPhrase (Dialogue.activity, Dialogue.textToSpeech);
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_PROCESS_FINISHED:
    			// -----------------------------------------------------------------
    			// 03/06/2016 ECU called up to process the string of actions which
    			//                are supplied as an argument in the message
    			// -----------------------------------------------------------------
    			if (++actionPointer >= actions.length)
				{	
    				// -------------------------------------------------------------
    				// 03/06/2016 ECU the last action in the set has been reached
    				// -------------------------------------------------------------
  
    				// -------------------------------------------------------------
					// 03/06/2016 ECU indicate all actions have been processed
					// -------------------------------------------------------------
					sendEmptyMessage (StaticData.MESSAGE_LIST_FINISHED);
					// -------------------------------------------------------------
				}
    			else
    			{
    				// -------------------------------------------------------------
    				// 03/06/2016 ECU process the next action - this will sort out
    				//                if the last action has been processed
    				// -------------------------------------------------------------
    				sendEmptyMessage (StaticData.MESSAGE_PROCESS_ACTION);
    				// -------------------------------------------------------------
    			}
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_PROMPT_DOSE:
    			// -----------------------------------------------------------------
    			// 21/12/2015 ECU this message speaks the prompt and, if necessary,
    			//                puts in a delayed message to repeat the prompt
    			// -----------------------------------------------------------------
    			// 21/12/2015 ECU if there are still queued prompts then put in the
    			//                delayed message after actioning the prompt
    			// -----------------------------------------------------------------
    			if (dosePromptCounter > 0)
    			{
    				// -------------------------------------------------------------
    				// 21/12/2015 ECU action the prompt
    				// -------------------------------------------------------------
    				Utilities.SpeakAPhrase (MainActivity.activity,dosePrompts [1]);
    				// -------------------------------------------------------------
    				// 21/12/2015 ECU clear any messages before putting in the
    				//                delayed message
    				// -------------------------------------------------------------
    				this.removeMessages (StaticData.MESSAGE_PROMPT_DOSE);
    				sendMessageDelayed (obtainMessage (StaticData.MESSAGE_PROMPT_DOSE),dosePromptDelay);
    			}
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_PROMPT_DOSE_END:
    			// -----------------------------------------------------------------
    			// 21/12/2015 ECU called when the user has responded to a prompt
    			//            ECU the '>0' check is 'just in case'
    			// -----------------------------------------------------------------
    			if (dosePromptCounter > 0)
     				dosePromptCounter--; 				
    			// -----------------------------------------------------------------
    			break;
    		// =====================================================================
    		case StaticData.MESSAGE_PROMPT_DOSE_START:
    			// -----------------------------------------------------------------
    			// 21/12/2015 ECU this a request to start up the prompt mechanism -
    			//                because these can be queued only start one
    			// -----------------------------------------------------------------
    			if (dosePromptCounter == 0)
    			{
    				// -------------------------------------------------------------
    				// 21/12/2015 ECU set up the variables from what is stored in
    				//                the message
    				// -------------------------------------------------------------
    				dosePromptDelay = theMessage.arg1;
    				dosePrompts		= (String []) theMessage.obj;
    				// -------------------------------------------------------------
    				// 21/12/2015 ECU action the initial prompt
    				// -------------------------------------------------------------
    				Utilities.SpeakAPhrase (MainActivity.activity,dosePrompts [0]);
    				// -------------------------------------------------------------
    				// 21/12/2015 ECU new set up a delayed message to get a prompt
    				//                after the appropriate time
    				// -------------------------------------------------------------
    				this.removeMessages (StaticData.MESSAGE_PROMPT_DOSE);
    				sendMessageDelayed  (obtainMessage (StaticData.MESSAGE_PROMPT_DOSE),dosePromptDelay);
    				// -------------------------------------------------------------
    			}
    			// -----------------------------------------------------------------
    			// 21/12/2015 ECU under all circumstances increment the counter
    			// -----------------------------------------------------------------
    			dosePromptCounter++;
    			// -----------------------------------------------------------------
    			break;
        	// =====================================================================
        	case StaticData.MESSAGE_SLEEP:
        		break;
        	// =====================================================================
        	case StaticData.MESSAGE_TOAST:
        		// -----------------------------------------------------------------
        		// 24/11/2016 ECU created to display a toast message from the 
        		//                information contained in this message
        		// -----------------------------------------------------------------
        		Utilities.popToast ((String)theMessage.obj,true);
        		// -----------------------------------------------------------------
        		break;
        	// =====================================================================
        	case StaticData.MESSAGE_TOAST_PHOTO:
        		// -----------------------------------------------------------------
        		// 02/10/2016 ECU created to display the toast message and associated
        		//                photo
        		// 27/11/2016 ECU try not to interrupt any actions that are in progress
        		//                A problem was highlighted when a carer visit is 
        		//                being announced manually - this message was occurring
        		//                after a list of actions was started and therefore
        		//                causing the processing of that list to hang because
        		//                it is waiting for a spoken phrase to complete -
        		//                so this makes little sense but does to me. The real
        		//                issue is this handler clashing with the TextToSpeech
        		//                service
        		// -----------------------------------------------------------------
        		if (PublicData.actions.size() == 0)
        		{
        			// -------------------------------------------------------------
        			// 02/10/2016 ECU get the message and photo path from the received
        			//                message
        			// -------------------------------------------------------------
        			String [] localStrings = (String []) theMessage.obj;
        			// -------------------------------------------------------------
        			// 02/10/2016 ECU the message is in the first argument, the photo
        			//                path is the second
        			// -------------------------------------------------------------
        			Utilities.popToast (localStrings [0],
        								true,
        								Toast.LENGTH_LONG,
        								localStrings [1]);
        			// -------------------------------------------------------------
        			// 02/10/2016 ECU and speak the message
        			// -------------------------------------------------------------
        			Utilities.SpeakAPhrase (MainActivity.activity,localStrings [0]);
        			// -------------------------------------------------------------
        		}
        		else
        		{
        			// -------------------------------------------------------------
        			// 27/11/2016 ECU created a new message, copy across the data
        			//                and then send a delayed message to check
        			//                again if any actions are running
        			// -------------------------------------------------------------
        			Message localMessage = this.obtainMessage(StaticData.MESSAGE_TOAST_PHOTO, theMessage.obj);
        			sendMessageDelayed (localMessage,500);
        			// -------------------------------------------------------------
        		}
        		break;
        	// =====================================================================
        	case StaticData.MESSAGE_TOAST_SPEAK:
        		// -----------------------------------------------------------------
        		// 05/04/2016 ECU created to display and speak a toast message from the 
        		//                information contained in this message
        		// -----------------------------------------------------------------
        		Utilities.popToastAndSpeak ((String)theMessage.obj,true);
        		// -----------------------------------------------------------------
        		break;
        	// =====================================================================
        	case StaticData.MESSAGE_WEB_PAGE:
        		// -----------------------------------------------------------------
        		// 18/09/2015 ECU added to return the contents of a specified web
        		//				  page
        		// -----------------------------------------------------------------
        		// 18/09/2015 ECU get the specified web page
        		// -----------------------------------------------------------------
        		ArrayList <String> 	displayString = Utilities.getWebPage(((URLMethod)theMessage.obj).URL);
        		// -----------------------------------------------------------------
        		if (displayString != null)
        		{
        			// -------------------------------------------------------------
        			// 18/09/2015 ECU try and invoke the associated method
        			// -------------------------------------------------------------
        			try 
    				{ 
    					// -------------------------------------------------------------
    					// 16/03/2015 ECU call up the method that will handle the 
    					//                input text
    					// -------------------------------------------------------------
    					((URLMethod) theMessage.obj).URLMethod.invoke (null,displayString);
    					// -------------------------------------------------------------
    				} 
    				catch (Exception theException) 
    				{	
    					theException.printStackTrace();
    				} 
        			// -------------------------------------------------------------
        		}
        		else
        		{
        			// -------------------------------------------------------------
        			// 18/09/2015 ECU failed to get the page so try again
        			// -------------------------------------------------------------
        			this.removeMessages (StaticData.MESSAGE_WEB_PAGE);
        			Message localMessage = obtainMessage (StaticData.MESSAGE_WEB_PAGE);
        			localMessage.obj = theMessage.obj;
        	    	sendMessageDelayed (localMessage,5000);
            		// -------------------------------------------------------------
        		}   		
        		// -----------------------------------------------------------------
        		break;
        	// =====================================================================
        	case StaticData.MESSAGE_WEB_PAGE_RETRY:
           		Utilities.popToastAndSpeak ("after delay " + (String) theMessage.obj);
        		break;
        	// =====================================================================
        }
    }
    // =============================================================================
    public void sleep (long delayMillis)
    {	
    	// -------------------------------------------------------------------------
    	// 05/05/2015 ECU changed to use MESSAGE_SLEEP instead of 0
    	// -------------------------------------------------------------------------
    	this.removeMessages (StaticData.MESSAGE_SLEEP);
    	sendMessageDelayed (obtainMessage (StaticData.MESSAGE_SLEEP), delayMillis);
    }
    // =============================================================================
    public static void displayDrawable (int theDrawable,int theRepeats)
    {
    	// -------------------------------------------------------------------------
    	// 31/01/2017 ECU created to cause a 'full screen' drawable image to be
    	//                displayed - this is used, for example, when warnings
    	//                about light levels are actioned
    	// -------------------------------------------------------------------------
    	for (int repeat = 0; repeat < theRepeats; repeat++)
    	{
    		// ---------------------------------------------------------------------
    		// 31/01/2017 ECU the '0' is just a dummy
    		// ---------------------------------------------------------------------
    		PublicData.messageHandler.sendMessage (PublicData.messageHandler.obtainMessage (StaticData.MESSAGE_DISPLAY_DRAWABLE,theDrawable,0));
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    }
    // -----------------------------------------------------------------------------
    public static void displayDrawable (int theDrawable)
    {
    	// -------------------------------------------------------------------------
    	// 31/01/2017 ECU created to display the specified drawable once (hence the 1)
    	// -------------------------------------------------------------------------
    	displayDrawable (theDrawable,1);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void displayText (String theText)
    {
    	// -------------------------------------------------------------------------
    	// 31/01/2017 ECU created to display text in the middle of a full screen
    	// -------------------------------------------------------------------------
    	Message localMessage = PublicData.messageHandler.obtainMessage (StaticData.MESSAGE_DISPLAY_TEXT,(Object)theText);
    	// -------------------------------------------------------------------------
    	// 31/01/2017 ECU now send the message
    	// -------------------------------------------------------------------------
    	PublicData.messageHandler.sendMessage (localMessage);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void playRawResource (int theResourceID,int theLoopCounter,int theLoopPosition)
    {
    	// -------------------------------------------------------------------------
    	// 12/03/2017 ECU created to use the message handler to play a 'raw' file
    	// -------------------------------------------------------------------------
    	Message localMessage = PublicData.messageHandler.obtainMessage (StaticData.MESSAGE_PLAY_RAW);
    	// -------------------------------------------------------------------------
    	// 12/03/2017 ECU store the arguments in the message
    	// -------------------------------------------------------------------------
    	localMessage.arg1	= theResourceID;
    	localMessage.arg2  	= theLoopCounter;
    	localMessage.obj	= theLoopPosition;
    	// -------------------------------------------------------------------------
    	// 12/03/2017 ECU now send the message
    	// -------------------------------------------------------------------------
    	PublicData.messageHandler.sendMessage (localMessage);
    	// -------------------------------------------------------------------------
    }
    // -----------------------------------------------------------------------------
    public static void playRawResource (int theResourceID)
    {
    	// -------------------------------------------------------------------------
    	// 12/03/2017 ECU method when the 'raw' file just needs to be played once
    	// -------------------------------------------------------------------------
    	playRawResource (theResourceID,0,0);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void popToast (String theMessage)
    {
    	// ------------------------------------------------------------------------
    	// 24/11/2016 ECU created to use the message handler to display a toast
    	//                message
    	// ------------------------------------------------------------------------
    	Message localMessage = PublicData.messageHandler.obtainMessage (StaticData.MESSAGE_TOAST,(Object)theMessage);
		// ------------------------------------------------------------------------
		// 24/11/2016 ECU now send the message to the handler
		// ------------------------------------------------------------------------
		PublicData.messageHandler.sendMessage (localMessage);
    }
    // =============================================================================
    public static void popToastAndSpeak (String theMessage)
    {
    	// ------------------------------------------------------------------------
    	// 05/04/2016 ECU created to use the message handler to display a toast
    	//                message
    	// 24/11/2016 ECU changed to use ....TOAST_SPEAK instead of TOAST
    	// ------------------------------------------------------------------------
    	Message localMessage = PublicData.messageHandler.obtainMessage (StaticData.MESSAGE_TOAST_SPEAK,(Object)theMessage);
		// ------------------------------------------------------------------------
		// 05/04/2016 ECU now send the message to the handler
		// ------------------------------------------------------------------------
		PublicData.messageHandler.sendMessage (localMessage);
    }
    // =============================================================================
    public static void popToastAndSpeakwithPhoto (String theMessage,String thePhotoPath)
    {
    	// ------------------------------------------------------------------------
    	// 02/10/2016 ECU created to use the message handler to display a toast
    	//                message - and specified photo
    	// ------------------------------------------------------------------------
    	Message localMessage = PublicData.messageHandler.obtainMessage (StaticData.MESSAGE_TOAST_PHOTO,(Object)(new String [] {theMessage,thePhotoPath}));
		// ------------------------------------------------------------------------
		// 05/04/2016 ECU now send the message to the handler
		// ------------------------------------------------------------------------
		PublicData.messageHandler.sendMessage (localMessage);
		// ------------------------------------------------------------------------
    }
    // =============================================================================
};
// =================================================================================



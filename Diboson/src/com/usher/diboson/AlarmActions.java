package com.usher.diboson;

import java.util.Calendar;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;

public class AlarmActions extends DibosonActivity 
{
	// ===============================================================================
	// 18/06/2013 ECU created
	// 21/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 04/03/2017 ECU remove the check on whether the app is running because this is
	//                done by AlarmReceiver which will not start this activity if the
	//                app is not running
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	// 04/12/2013 ECU difficult to test in isolation because it is at the end of the
	//                alarm chain started from TimerActivity
	//================================================================================
	/* =============================================================================== */
	//private static final String TAG					  = "AlarmActions";
	/* =============================================================================== */
	// 09/02/2015 ECU changed to use the whole object rather than the individual
	//                variables
	// -------------------------------------------------------------------------------
	AlarmData	alarmData;									// 09/02/2015 ECU added
	/* =============================================================================== */
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 21/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_alarm_actions);
		
			Bundle extras = getIntent().getExtras();
	    
			if (extras != null)
			{
				// -----------------------------------------------------------------
				// 23/03/2014 ECU get the medication index from the intent
				// 08/02/2015 ECU changed to use _DATA and associatedData instead
				//                of _MEDICATION and medicationIndex
				// 09/02/2015 ECU added the message
				//            ECU change to read in the whole object
				// 14/07/2015 ECU added the email message
				// -----------------------------------------------------------------
				alarmData = (AlarmData) extras.getSerializable (StaticData.PARAMETER_ALARM_DATA);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 18/06/2013 ECU take the appropriate action 
			// 04/03/2017 ECU used to have a check here on whether the app is running
			//                but put this check in AlarmReceiver
			// ---------------------------------------------------------------------
			if (alarmData.action != StaticData.ALARM_ACTION_NONE)
			{
				if ((alarmData.action & StaticData.ALARM_ACTION_PHONE_CALL) == StaticData.ALARM_ACTION_PHONE_CALL)
				{
					// -------------------------------------------------------------
					// 18/06/2013 ECU change from literal to stored value
					// 04/12/2013 ECU try and make a phone call
					// -------------------------------------------------------------
					Utilities.makePhoneCall (this,getString (R.string.phone_number_ed));
					// -------------------------------------------------------------
				}
				if ((alarmData.action & StaticData.ALARM_ACTION_TABLET_REMINDER) == StaticData.ALARM_ACTION_TABLET_REMINDER)
				{
					// -------------------------------------------------------------
					// 04/12/2013 ECU indicate that medication is to be taken
					// 22/03/2014 ECU as a dummy test that call the Utilities method
					// 08/02/2015 ECU change the following logic to use associatedData
					//                instead of medicationIndex
					// -------------------------------------------------------------
					Calendar calendar = Calendar.getInstance();
					
					if (alarmData.associatedData != StaticData.NO_RESULT)
					{
						// ---------------------------------------------------------
						// 23/03/2014 ECU this is just for testing so make sure
						//                things are set up correctly
						// 28/11/2014 ECU just tidy up the logic so that medication
						//                details always displayed
						// 09/03/2016 ECU changed to pass through the object
						// ---------------------------------------------------------
						if (PublicData.medicationDetails.get (alarmData.associatedData).dailyDoseTimes != null)
						{	
							Utilities.TimeForMedication (this,
														 calendar.get(Calendar.HOUR_OF_DAY),
														 calendar.get(Calendar.MINUTE),
														 alarmData.associatedData,
														 (DoseTime) alarmData.object);
						}
					}
					// -------------------------------------------------------------	
				}
				if ((alarmData.action & StaticData.ALARM_ACTION_SLIDESHOW) == StaticData.ALARM_ACTION_SLIDESHOW)
				{
					// -------------------------------------------------------------
					// 10/04/2013 ECU start the slide show
					// 04/03/2016 ECU check that PublicData is not null
					// -------------------------------------------------------------
					Intent myIntent = new Intent (getBaseContext(),SlideShowActivity.class);
					startActivity (myIntent);
				}
				// -----------------------------------------------------------------
				// 28/11/2014 ECU add in the'activity' option
				// -----------------------------------------------------------------
				if ((alarmData.action & StaticData.ALARM_ACTION_ACTIVITY) == StaticData.ALARM_ACTION_ACTIVITY)
				{
					// -------------------------------------------------------------
					// 28/11/2014 ECU start the specified activity
					// 06/02/2015 ECU changed to use the stored Intent
					// -------------------------------------------------------------
					for (int alarmIndex = 0; alarmIndex < PublicData.alarmData.size(); alarmIndex++)
					{
						if (PublicData.alarmData.get(alarmIndex).id == alarmData.id)
						{
							// -----------------------------------------------------
							// 06/02/2015 ECU have found the required alarm so 
							//                action the specified activity
							// 23/09/2017 ECU changed to use the stored 'message' rather
							//                than the index that was in 'associatedData'.
							//                The message contains the legend of the
							//                activity to be started - do it this way
							//                to adjust for when the 'sort by usage'
							//                is being used
							// -----------------------------------------------------
							Intent localIntent = new Intent (getBaseContext(),GridActivity.class);
							// -----------------------------------------------------
							// 23/09/2017 ECU get the position of the activity in
							//                the current array based on the legend
							//                that is stored in the alarm data
							// -----------------------------------------------------
							int activityPosition = GridImages.returnPosition (GridActivity.gridImages,
																				PublicData.alarmData.get(alarmIndex).message);
							// -----------------------------------------------------
							// 23/09/2017 ECU now activate the specified activity
							// -----------------------------------------------------
							localIntent.putExtra (StaticData.PARAMETER_POSITION,activityPosition);
							localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity (localIntent);
							// -----------------------------------------------------
							break;
						}
					}	 
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				if ((alarmData.action & StaticData.ALARM_ACTION_MESSAGE) == StaticData.ALARM_ACTION_MESSAGE)
				{
					// -------------------------------------------------------------
					// 09/02/2015 ECU display the message
					//            ECU use an activity to handle the message
					// -------------------------------------------------------------
					Intent localIntent = new Intent (getBaseContext(),DisplayAMessage.class);
					localIntent.putExtra (StaticData.PARAMETER_START_TIME,alarmData.Time());
					localIntent.putExtra (StaticData.PARAMETER_MESSAGE,alarmData.message);
					localIntent.putExtra (StaticData.PARAMETER_SPEAK,true);
					localIntent.putExtra (StaticData.PARAMETER_TIMER,(1000 * 20));
					startActivity (localIntent);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				if ((alarmData.action & StaticData.ALARM_ACTION_EMAIL_MESSAGE) == StaticData.ALARM_ACTION_EMAIL_MESSAGE)
				{
					// -------------------------------------------------------------
					// 14/07/2015 ECU action the stored email address
					// 09/03/2016 ECU changed to use the object
					// -------------------------------------------------------------
					((EmailMessage) alarmData.object).Send (this);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				if ((alarmData.action & StaticData.ALARM_ACTION_EPG) == StaticData.ALARM_ACTION_EPG)
				{
					// -------------------------------------------------------------
					// 29/09/2015 ECU action the EPG alarm
					// 30/09/2015 ECU change the cast to EPGAlarm
					// 03/04/2016 ECU put in the check on a 'null' object
					// -------------------------------------------------------------
					if (alarmData.object != null)
						ShowEPGActivity.EPGActionAlarm (this,(EPGAlarm)alarmData.object);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				if ((alarmData.action & StaticData.ALARM_ADVANCE_EPG) == StaticData.ALARM_ADVANCE_EPG)
				{
					// -------------------------------------------------------------
					// 15/10/2015 ECU the alarm that gives an advance warning of 
					//                an impending EPG alarm
					// 03/04/2016 ECU put in the check on a 'null' object
					// -------------------------------------------------------------
					if (alarmData.object != null)
						ShowEPGActivity.EPGAdvanceWarning (this,(EPGAlarm)alarmData.object);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				if ((alarmData.action & StaticData.ALARM_ACTION_ACTIONS) == StaticData.ALARM_ACTION_ACTIONS)
				{
					// -------------------------------------------------------------
					// 30/04/2017 ECU check for timed actions and action those
					//                which are contained as a message in the alarm
					//                data
					//            ECU changed from '.message' to '.actions'
					// -------------------------------------------------------------
					Utilities.actionHandler (getBaseContext (),alarmData.actions);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 04/12/2013 ECU default to no action to take
			// 18/05/2017 ECU removed the following line as no longer needed
			//            ECU highlighted when adding the 'repeat alarm' code
			// ---------------------------------------------------------------------
			//alarmData.action = StaticData.ALARM_ACTION_NONE;
			// ---------------------------------------------------------------------
			// 18/06/2013 ECU want to make sure that this alarm is removed from  
			//                the array - use id
			// 06/02/2015 ECU changed to used new array list
			// 29/09/2015 ECU changed to use the method - the code used to be 
			//				  here
			//            ECU feed through the id rather than the whole object
			//            ECU added the context as argument
			// ---------------------------------------------------------------------
			deleteAlarmFromList (this,alarmData.id,false);
			// ---------------------------------------------------------------------
			// 18/05/2017 ECU check if this alarm is a 'repeat' alarm
			// ---------------------------------------------------------------------
			if (alarmData.repeatInterval != StaticData.NO_RESULT)
			{
				// -----------------------------------------------------------------
				// 18/05/2017 ECU it appears that this is an alarm which is to be
				//                repeated
				// -----------------------------------------------------------------
				alarmData.setRepeatAlarm (this);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 18/06/2013 ECU write the alarm data to disk
			// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
			// 08/02/2015 ECU changed to use ...alarmFileName]
			// 29/09/2015 ECU the writing to disk now in the 'delete...' method
			// ---------------------------------------------------------------------
			//AsyncUtilities.writeObjectToDisk (PublicData.alarmFileName,PublicData.alarmData);
			// ---------------------------------------------------------------------
			// 18/06/2013 ECU just finish this activity
			// ---------------------------------------------------------------------
			finish ();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 21/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static int checkForAnAlarm (Context theContext,long theAlarmID,int theActionRequired)
	{
		// -------------------------------------------------------------------------
		// 17/10/2015 ECU created to check if the specified alarm is in the list
		//                if the alarm is found then returns the index within the list
		//                if the alarm is not found then returns NO_RESULT
		// 22/01/2017 ECU added theActionRequired (if not set to NO_RESULT) then only
		//                the required alarm will cause the correct response
		// -------------------------------------------------------------------------
		for (int alarmIndex = 0; alarmIndex < PublicData.alarmData.size(); alarmIndex++)
		{
			if (PublicData.alarmData.get (alarmIndex).id == theAlarmID)
			{
				// -----------------------------------------------------------------
				// 22/01/2017 ECU if required then check if the action is the one
				//                specified
				// -----------------------------------------------------------------
				if (theActionRequired == StaticData.NO_RESULT)
				{
					// -------------------------------------------------------------
					// 17/10/2015 ECU alarm found so return its index
					// -------------------------------------------------------------
					return alarmIndex;
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 22/01/2017 ECU check if the alarm with the correct id has the
					//                specified action
					//  ------------------------------------------------------------
					if (PublicData.alarmData.get (alarmIndex).action == theActionRequired)
					{
						return alarmIndex;
					}
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
		} 
		// -------------------------------------------------------------------------
		// 17/10/2015 ECU no alarm found
		//--------------------------------------------------------------------------
		return StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
	public static void deleteAlarmFromList (Context theContext,long theAlarmID,boolean theCancelAlarmFlag)
	{
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU created to remove the specified data from the list - the
		//                code used to be in the main body of the class
		// -------------------------------------------------------------------------
		AlarmData localAlarmData;
		// -------------------------------------------------------------------------
		for (int alarmIndex = 0; alarmIndex < PublicData.alarmData.size(); alarmIndex++)
		{
			localAlarmData = PublicData.alarmData.get (alarmIndex);
			
			if (localAlarmData.id == theAlarmID)
			{
				// -----------------------------------------------------------------
				// 29/09/2015 ECU check if the alarm is to be cancelled as well
				// -----------------------------------------------------------------
				if (theCancelAlarmFlag)
				{
					// -------------------------------------------------------------
					// 29/09/2015 ECU want to cancel the alarm with the manager
					// 29/01/2017 ECU changed from 'deleteAlarm'
					// -------------------------------------------------------------
					localAlarmData.cancelAlarm (theContext);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 06/02/2015 ECU have found the required alarm so delete its data
				// -----------------------------------------------------------------
				PublicData.alarmData.remove (alarmIndex);
				// -----------------------------------------------------------------
				// 29/09/2015 ECU update the copy of the data on disk
				// -----------------------------------------------------------------
				AsyncUtilities.writeObjectToDisk (PublicData.alarmFileName,PublicData.alarmData);
				// -----------------------------------------------------------------
				break;
			}
		} 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

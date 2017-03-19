package com.usher.diboson;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class DailyScheduler extends BroadcastReceiver
{
	/* ============================================================================= */
	// V E R Y  I M P O R T A N T
	// ==========================
	//	This class is an important part of the 'reminder' mechanism used within this
	//  system. It is intended that it is executed once a day, at a configurable
	//  time, to generate reminders for events that will occur on the day on which
	//  it is run.
	//
	//  It is recommended that the associated documentation is read in order to
	//  understand the handling of reminders and how this class is used.
	// -----------------------------------------------------------------------------
	// 04/03/2014 ECU created to handle various aspects of the daily scheduler
	// 19/12/2015 ECU edit following the move of the alarm ID's into StaticData
	// 29/01/2017 ECU Note - the reminders for appointments are generated on start up
	//                       or when the appointment is created / edited therefore 
	//                       there is nothing to be done on a 'daily' basis. For this 
	//                       reason the 'processing of appointments' have been commented
	//                       out of this class
	/* ============================================================================= */
	final static String TAG = "DailyScheduler";
	/* ============================================================================= */

	/* ============================================================================= */
	static	PendingIntent 		alarmPendingIntent; 
	/* ============================================================================= */
	public static void Initialise (Context theContext,int theHour,int theMinute)
	{
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU sets up the daily scheduler alarm
		// 14/12/2015 ECU add the 'true' flag to check if time is earlier than current
		//                time
		// -------------------------------------------------------------------------
		SetAnAlarm (theContext,StaticData.ALARM_ID_DAILY_SCHEDULER,
					StaticData.ALARM_ID_DAILY_SCHEDULER,GetTime (theHour,theMinute,true));
	}
	// -----------------------------------------------------------------------------
	public static void Initialise (Context theContext,long theTimeWanted)
	{
		// -------------------------------------------------------------------------
		// 08/02/2015 ECU created to set an alarm at the specified time
		// -------------------------------------------------------------------------
		SetAnAlarm (theContext,StaticData.ALARM_ID_DAILY_SCHEDULER,StaticData.ALARM_ID_DAILY_SCHEDULER,theTimeWanted);
		// -------------------------------------------------------------------------
	}
	/* =============================================================================== */
	public static void CancelAlarm (Context theContext)
	{
		// ---------------------------------------------------------------------------
		// 20/12/2015 ECU created - called to cancel alarms associated with the daily
		//                scheduler
		// 01/01/2016 ECU changed to use the new method
		// 03/11/2016 ECU add the false to indicate no pending intent cancel
		// ---------------------------------------------------------------------------
		Utilities.cancelAnAlarm (theContext,alarmPendingIntent,false);
		// ---------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void CreateAppointmentAlarm (Context theContext,int theAlarmID,int theParameterID,int theIndex)
	{
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU decide what needs doing based on the type
		// 09/03/2014 ECU change the switch to be on 'theParameterID' rather than
		//                'theAlarmID'
		// -------------------------------------------------------------------------
		switch (theParameterID)
		{
			// ---------------------------------------------------------------------
			case StaticData.ALARM_ID_APPOINTMENT_REMINDER:
				// -----------------------------------------------------------------
				// 04/03/2014 ECU set an alarm at the reminder time
				// -----------------------------------------------------------------
				SetAnAlarm (theContext,theAlarmID,theParameterID,PublicData.appointments.get(theIndex).reminderNextTime,theIndex);
				// -----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
			case StaticData.ALARM_ID_APPOINTMENT_TIME:
				// -----------------------------------------------------------------
				// 04/03/2014 ECU set an alarm at the appointment time
				// -----------------------------------------------------------------
				SetAnAlarm (theContext,theAlarmID,theParameterID,PublicData.appointments.get(theIndex).dateTime,theIndex);
				// ----------------------------------------------------------------
				break;
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	static long GetTime (int theHour,int theMinute,boolean theCurrentTimeCheck)
	{
		// ------------------------------------------------------------------------- 
		// 04/03/2014 ECU gets the time, today, in milliseconds of the supplied
		//                time. If the time is earlier than the current time
		//                then the time is for the next day
		// 14/12/2015 ECU added 'theCurrentTimeCheck' which if true then
		//                if the specified time is earlier than the current time
		//                then added 'a day's worth of milliseconds' - if false
		//                then just indicate the fact
		// -------------------------------------------------------------------------	
		Calendar calendar = Calendar.getInstance ();		
		long currentTime = calendar.getTimeInMillis ();		
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU set to the actual time wanted
		// -------------------------------------------------------------------------		
		calendar.set (Calendar.HOUR_OF_DAY,theHour);
		calendar.set (Calendar.MINUTE, theMinute);
		calendar.set (Calendar.SECOND, 0);
		// -------------------------------------------------------------------------
		long alarmTime = calendar.getTimeInMillis ();		
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU check if the alarm is before the current time
		// -------------------------------------------------------------------------		
		if (alarmTime <= currentTime)
		{
			// ---------------------------------------------------------------------
			// 14/12/2015 ECU check whether want to advance a day
			// ---------------------------------------------------------------------
			if (theCurrentTimeCheck)
			{
				// -----------------------------------------------------------------
				// 04/03/2014 ECU alarm time has passed so add in a day
				// 01/09/2015 ECU changed to use StaticData
				// -----------------------------------------------------------------		
				alarmTime += StaticData.MILLISECONDS_PER_DAY;		
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 14/12/2015 ECU just indicate time is earlier but do not adjust
				// -----------------------------------------------------------------
				alarmTime = StaticData.NO_RESULT;
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU return the adjust time
		// -------------------------------------------------------------------------
		return alarmTime;
	}
	// =============================================================================
	public static void ProcessCarePlanVisits (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 02/10/2016 ECU created to process the information stored in the care plan
		//                records
		// 30/01/2017 ECU tidied up the code so that use local variables rather than
		//                keeping referring to PublicData.carePlan.visits, etc..
		// -------------------------------------------------------------------------
		// 02/10/2016 ECU declare the id which will uniquely define this alarm within
		//                the care visit list
		// -------------------------------------------------------------------------
	    Calendar 	calendar;							// 08/12/2016 ECU moved here
	    int 		localAlarmId 	= StaticData.ALARM_ID_CARE_VISIT;
	    // -------------------------------------------------------------------------
	    // 02/10/2016 ECU get today's day which is the index into the care plan
	    // -------------------------------------------------------------------------
		int localDayOfWeek 	= Utilities.DayOfWeek ();
		// -------------------------------------------------------------------------
		// 30/01/2017 ECU load up any visits that are set for 'this' day
		//            ECU tidied up the code to use local variable rather than accessing
		//                PublicData....
		// -------------------------------------------------------------------------
		List<CarePlanVisit> localVisits = PublicData.carePlan.visits [localDayOfWeek];
		// -------------------------------------------------------------------------
		// 02/10/2016 ECU check if there are any visits for this day
		// -------------------------------------------------------------------------
		if (localVisits.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 02/10/2016 ECU there appear to be visits for this day
			// ---------------------------------------------------------------------
			// 30/01/2017 ECU declare any variables used in the following loop - until
			//                now the variables were being defined in the loop
			// ---------------------------------------------------------------------
			long 	currentTime;
			long	localAlarmTime;
			int 	localHour;
			int 	localMinute;
			// ---------------------------------------------------------------------
			// 02/10/2016 ECU loop through the visits for today
			// ---------------------------------------------------------------------
			for (int index = 0; index < localVisits.size(); index++)
			{
				// -----------------------------------------------------------------
				// 02/10/2016 ECU want to set up an alarm for this visit
				// -----------------------------------------------------------------
				calendar 	= Calendar.getInstance();	
				// -----------------------------------------------------------------
				// 02/10/2016 ECU set up the current time
				// -----------------------------------------------------------------
				currentTime 	= calendar.getTimeInMillis();
				// -----------------------------------------------------------------
				calendar.setTimeInMillis (localVisits.get (index).startTime);	
				// -----------------------------------------------------------------
				// 04/03/2014 ECU set to the actual time wanted
				// -----------------------------------------------------------------		
				localHour 			= calendar.get (Calendar.HOUR_OF_DAY);
				localMinute 		= calendar.get (Calendar.MINUTE);
				// -----------------------------------------------------------------
				// 08/12/2016 ECU change the argument from 'true' to 'false' because
				//                if the alarm time is in the past then do not step
				//                to the next day
				// -----------------------------------------------------------------
				localAlarmTime = GetTime (localHour,localMinute,false);
				// -----------------------------------------------------------------
				// 02/10/2016 ECU check if the alarm has passed
				// -----------------------------------------------------------------
				if (localAlarmTime != StaticData.NO_RESULT)
				{
					// -------------------------------------------------------------
					// 02/10/2016 ECU set up an alarm for this visit
					// -------------------------------------------------------------
					SetAnAlarm (theContext,
							    localAlarmId++,
							    StaticData.ALARM_ID_CARE_VISIT,
								localAlarmTime,
								new int [] {StaticData.CARE_VISIT_ARRIVAL,localDayOfWeek,index});
					// -------------------------------------------------------------
					// 02/10/2016 ECU set up the warning alarms
					// -------------------------------------------------------------
					for (int warning = StaticData.CARE_VISIT_WARNING_PERIOD; warning > 0; warning--)
					{
						long warningTime = localAlarmTime - (warning * StaticData.MILLISECONDS_PER_MINUTE);
						// ---------------------------------------------------------
						// 02/10/2016 ECU only set the warning alarm if it is in the
						//                future
						// ---------------------------------------------------------
						if (warningTime > currentTime)
						{
							SetAnAlarm (theContext,
										localAlarmId++,
										StaticData.ALARM_ID_CARE_VISIT,
										warningTime,
										new int [] {StaticData.CARE_VISIT_WARNING,
													localDayOfWeek,
													index,
													warning});
						}
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 02/10/2016 set the departure alarm
					// -------------------------------------------------------------
					SetAnAlarm (theContext,
						    localAlarmId++,
						    StaticData.ALARM_ID_CARE_VISIT,
							localAlarmTime + localVisits.get (index).duration * StaticData.MILLISECONDS_PER_MINUTE,
							new int [] {StaticData.CARE_VISIT_DEPARTURE,localDayOfWeek,index});
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 02/10/2016 ECU the care plan visit has passed
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/10/2016 ECU there are no visits for today
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void ProcessMedicationDetails (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU set up any alarms associated with the giving of medication
		// 14/12/2015 ECU changed to 'public static'
		// -------------------------------------------------------------------------
		Calendar calendar 	= Calendar.getInstance ();
		int dayOfWeek 		= calendar.get(Calendar.DAY_OF_WEEK);
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU dayOfWeek = 1 (Sunday), 2 (Monday), .......
		//
		//                this system uses 0 (Monday), 1 (Tuesday) .... ???? 
		// -------------------------------------------------------------------------
	    if ((dayOfWeek -= 2) < 0) dayOfWeek += 7;	
	    // -------------------------------------------------------------------------
	    // 04/03/2014 ECU now scan through the medication details to see what
	    //                actions need to be taken
	    // -------------------------------------------------------------------------
	    int 	localAlarmId 	= StaticData.ALARM_ID_DOSAGE_ALARM;
	    long	localAlarmTime	= StaticData.NO_RESULT;
	    
	    for (int medicationIndex = 0; medicationIndex < PublicData.medicationDetails.size(); medicationIndex++)
		{
			if (PublicData.medicationDetails.get (medicationIndex).dailyDoseTimes[dayOfWeek] != null)
			{
				for (int doseTimesIndex = 0; 
						 doseTimesIndex < PublicData.medicationDetails.get (medicationIndex).dailyDoseTimes[dayOfWeek].doseTimes.size(); 
						 doseTimesIndex++)
				{
					if (PublicData.medicationDetails.get (medicationIndex).dailyDoseTimes[dayOfWeek].doseTimes.get(doseTimesIndex) != null)
					{	
						// ---------------------------------------------------------
						// 14/12/2015 ECU added the 'false' argument because if the
						//                alarm is earlier than the current time
						//                then the alarm must not be set
						// ---------------------------------------------------------
						localAlarmTime = GetTime(PublicData.medicationDetails.get (medicationIndex).dailyDoseTimes[dayOfWeek].doseTimes.get(doseTimesIndex).hours,
												 PublicData.medicationDetails.get (medicationIndex).dailyDoseTimes[dayOfWeek].doseTimes.get(doseTimesIndex).minutes,
												 false);
						// ---------------------------------------------------------
						// 14/12/2015 ECU check if this is a valid alarm
						// ---------------------------------------------------------
						if (localAlarmTime != StaticData.NO_RESULT)
						{
							SetAnAlarm (theContext,(localAlarmId++),StaticData.ALARM_ID_DOSAGE_ALARM,
										localAlarmTime,
										new int [] {medicationIndex,dayOfWeek,doseTimesIndex});
						}
						// ---------------------------------------------------------
					}
				}					
			}
		}			
	}
	/* ============================================================================= */
	static void SetAnAlarm (Context theContext,int theAlarmID,int theParameterID,long theTime)
	{
		SetAnAlarm (theContext,theAlarmID,theParameterID,theTime,null);
	}
	/* ============================================================================= */
	static void SetAnAlarm (Context theContext,int theAlarmID,int theParameterID,long theTime,int theArgument)
	{
		SetAnAlarm (theContext,theAlarmID,theParameterID,theTime,new int [] {theArgument});
	}
	// =============================================================================
	static void SetAnAlarm (Context theContext,int theAlarmID,int theParameterID,long theTime,int [] theArguments)
	{
		// -------------------------------------------------------------------------
		// 19/12/2015 ECU this was the original master method but changed to use
		//                'null' as the method definition
		// -------------------------------------------------------------------------
		SetAnAlarm (theContext,theAlarmID,theParameterID,theTime,theArguments,null);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void SetAnAlarm (Context theContext,int theAlarmID,int theParameterID,long theTime,
							int [] theArguments,MethodDefinition<?> theMethodDefinition)
	{
		// -------------------------------------------------------------------------
		// 23/02/2014 ECU added to create an alarm with the system manager
		// 10/11/2014 ECU changed to use Locale.getDefault instead of Locale.UK
		// 19/12/2015 ECU added the method definition argument
		// 28/10/2016 ECU changed to use dateFormatDDMMYY
		// -------------------------------------------------------------------------
		Utilities.LogToProjectFile (TAG,"SetAnAlarm " + " " + theAlarmID + " " +
				new SimpleDateFormat ("HH:mm:ss " + PublicData.dateFormatDDMMYY,Locale.getDefault()).format(theTime));
		// -------------------------------------------------------------------------
		// 03/11/2016 ECU changed to use the global alarm manager
		// -------------------------------------------------------------------------
		Intent alarmIntent = new Intent (theContext,DailyScheduler.class);
		alarmIntent.putExtra (StaticData.PARAMETER_ALARM_ID,theParameterID);
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU check if some additional arguments are to be passed
		// -------------------------------------------------------------------------
		if (theArguments != null)
		{
			alarmIntent.putExtra (StaticData.PARAMETER_ARGUMENTS, theArguments);
		}
		// -------------------------------------------------------------------------
		// 19/12/2015 ECU check if the method definition is to be included
		// -------------------------------------------------------------------------
		if (theMethodDefinition != null)
		{
			alarmIntent.putExtra (StaticData.PARAMETER_METHOD_DEFINITION,theMethodDefinition);
		}
		// -------------------------------------------------------------------------
		// 18/06/2013 ECU use alarmCounter as a unique request code - currently not used
		// -------------------------------------------------------------------------
		alarmPendingIntent = PendingIntent.getBroadcast (theContext,
														theAlarmID,alarmIntent,Intent.FLAG_ACTIVITY_NEW_TASK); 
		// -------------------------------------------------------------------------
		// 24/12/2015 ECU changed to use the new method
		// 03/11/2016 ECU changed to use the global alarm manager
		// -------------------------------------------------------------------------
		Utilities.SetAnExactAlarm (PublicData.alarmManager,theTime, alarmPendingIntent);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public void onReceive (Context context, Intent intent) 
	{
		// -------------------------------------------------------------------------
		// 27/03/2016 ECU the app is up and running so can process this alarm
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU receives incoming alarms
		// -------------------------------------------------------------------------
		int 				localAlarmID 			= StaticData.NO_RESULT;
		int [] 				localArguments			= null;
		MethodDefinition<?>	localMethodDefinition 	= null;
		// -------------------------------------------------------------------------
		// 30/01/2017 ECU Note - check if there are any parameters in the incoming
		//                       intent
		// -------------------------------------------------------------------------
		Bundle extras = intent.getExtras();
		
		if (extras != null)
		{
			// ---------------------------------------------------------------------
			// 04/0/2014 ECU get the identifier of the alarm
			// ---------------------------------------------------------------------
			localAlarmID = intent.getIntExtra (StaticData.PARAMETER_ALARM_ID,StaticData.NO_RESULT); 
			// ---------------------------------------------------------------------
			// 04/03/2014 ECU check if there are any associated arguments
			// ---------------------------------------------------------------------
			localArguments = intent.getIntArrayExtra (StaticData.PARAMETER_ARGUMENTS);
			// ---------------------------------------------------------------------
			// 19/12/2015 ECU check if a method definition has been provided
			// ---------------------------------------------------------------------
			localMethodDefinition 
				= (MethodDefinition<?>) extras.getSerializable (StaticData.PARAMETER_METHOD_DEFINITION);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 27/03/2016 ECU check if the app is running when this alarm is received
		// -------------------------------------------------------------------------
		if ((PublicData.storedData != null) && PublicData.storedData.initialised)
		{
			// ---------------------------------------------------------------------
			// 23/02/2014 ECU log useful information
			// ---------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG,"Alarm ID = " + localAlarmID);
			// ---------------------------------------------------------------------
			// 23/02/2014 ECU handle the individual alarms
			// ---------------------------------------------------------------------
			switch (localAlarmID)
			{
				// -----------------------------------------------------------------	
				case StaticData.ALARM_ID_APPOINTMENT_REMINDER :
					// -------------------------------------------------------------
					// 04/03/2014 ECU an appointment reminder has arrived           
					// -------------------------------------------------------------
					AppointmentsActivity.AppointmentReminder (context, localArguments [0]);
					// -------------------------------------------------------------
				
					// -------------------------------------------------------------
					// 09/03/2014 ECU include the fix because events were not happening
					//                on time - just like the speaking clock
					// -------------------------------------------------------------
					APIIssues.Fix001 (android.os.Build.VERSION.SDK_INT);
					// -------------------------------------------------------------
				
					break;
				// -----------------------------------------------------------------	
				case StaticData.ALARM_ID_APPOINTMENT_TIME :
				
					// -------------------------------------------------------------
					// 04/03/2014 ECU the time of an appointment has arrived
					// -------------------------------------------------------------
					AppointmentsActivity.AppointmentTime (context, localArguments[0]);
					// -------------------------------------------------------------
				
					// -------------------------------------------------------------
					// 09/03/2014 ECU include the fix because events were not happening
					//                on time - just like the speaking clock
					// -------------------------------------------------------------
					APIIssues.Fix001 (android.os.Build.VERSION.SDK_INT);
					// -------------------------------------------------------------
				
					break;
				// -----------------------------------------------------------------
				case StaticData.ALARM_ID_CARE_VISIT:
					// -------------------------------------------------------------
					// 02/10/2016 ECU created to handle the alarm associated with a
					//                care plan visit
					//            ECU an alarm can be received for the start of a
					//                visit or an advanced warning of a visit
					// 07/12/2016 ECU added try...catch
					// 08/12/2016 ECU add localActions and use carerRecord
					// -------------------------------------------------------------
					try
					{
						Carer  carerRecord  = PublicData.carePlan.visits [localArguments[1]].get(localArguments[2]).CarerRecord();
						String localActions	= null;
						String localMessage = carerRecord.name + StaticData.NEWLINE;
						// ---------------------------------------------------------
						// 02/10/2016 ECU decide which message is required
						// ---------------------------------------------------------
						switch (localArguments [0])
						{
							// -----------------------------------------------------
							case StaticData.CARE_VISIT_ARRIVAL:
								localActions	=  PublicData.storedData.visit_start_warning_actions;
								localMessage   +=  context.getString (R.string.arrive_shortly);
								break;
							// -----------------------------------------------------
							case StaticData.CARE_VISIT_DEPARTURE:
								localActions	=  PublicData.storedData.visit_end_warning_actions;
								localMessage   +=  context.getString (R.string.depart_shortly);
								break;
							// -----------------------------------------------------
							case StaticData.CARE_VISIT_WARNING:
								localMessage += String.format (context.getString(R.string.arrive_warning_format),
															     localArguments [3]) + Utilities.AddAnS (localArguments [3]);
								break;
							// -----------------------------------------------------	
						}
						// ---------------------------------------------------------
						// 02/10/2016 ECU now display and speak the message
						// 08/12/2016 ECU changed to use carerRecord
						// ---------------------------------------------------------
						MessageHandler.popToastAndSpeakwithPhoto (localMessage,
															      PublicData.projectFolder + carerRecord.photo);
						// ---------------------------------------------------------
						// 08/12/2016 ECU if actions have been defined then process
						//                them
						//            ECU changed to use carerRecord
						// ---------------------------------------------------------
						if (localActions != null)
						{
							Utilities.actionHandler (context,localActions.replaceAll(StaticData.CARER_REPLACEMENT,carerRecord.name));
						}
						// ---------------------------------------------------------
					}
					catch (Exception theException)
					{
						// ---------------------------------------------------------
						// 07/12/2016 ECU log the problem that occurred
						// ---------------------------------------------------------
						Utilities.LogToProjectFile (TAG,"Exception : " + theException);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.ALARM_ID_DAILY_SCHEDULER:
					// -------------------------------------------------------------
					// 04/03/2014 ECU actions to be taken each time get an alarm
					// 
					//      1) reinitialise this alarm
					//      2) process any medication reminders
					//      3) process appointments				// 29/01/2017 ECU see notes at
					//                                          //                top of class
					//      4) process care plan visits			// 08/12/2016 ECU added
					//
					// -------------------------------------------------------------
					Initialise (context,PublicData.storedData.schedulerHour,PublicData.storedData.schedulerMinute);
					// -------------------------------------------------------------
					// 04/03/2014 ECU now do the necessary processing
					// -------------------------------------------------------------
					ProcessMedicationDetails (context);			// medication details
					// -------------------------------------------------------------
					// 29/01/2017 ECU the call is commented out (see notes at the
					//                head of the class. Left here just for
					//                historical reasons
					// -------------------------------------------------------------
					//ProcessAppointments (context);				// appointments
					// -------------------------------------------------------------
					// 02/10/2016 ECU process any care plan visits - commented out
					// 08/12/2016 ECU remove comment so that processing takes place
					// -------------------------------------------------------------
					ProcessCarePlanVisits (context);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.ALARM_ID_DOSAGE_ALARM:
				
					// -------------------------------------------------------------
					// 04/03/2014 ECU alarm associated with a dosage
					// -------------------------------------------------------------
					// 04/03/2014 ECU check for any associated arguments
					// -------------------------------------------------------------
					if (localArguments != null)
					{
						// ---------------------------------------------------------
						// 04/03/2014 ECU start the activity to inform the user
						// ---------------------------------------------------------
						Calendar calendar = Calendar.getInstance ();
						Utilities.TimeForMedication (context,
													 calendar.get(Calendar.HOUR_OF_DAY),
													 calendar.get(Calendar.MINUTE),
													 localArguments[0],		// medication index
													 localArguments[1],		// dose time index
													 localArguments[2]);	// dose index
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------	
					break;
				// -----------------------------------------------------------------
				case StaticData.ALARM_ID_METHOD:
					// -------------------------------------------------------------
					// 19/12/2015 ECU added to handle an alarm with a defined method to
					//                be called
					// -------------------------------------------------------------
					if (localMethodDefinition != null)
					{
						// ---------------------------------------------------------
						// 19/12/2015 ECU create a method from the definition
						// ---------------------------------------------------------
						Method localMethod = localMethodDefinition.ReturnMethod (); 
						// ---------------------------------------------------------
						// 19/12/2015 ECU if a valid method has been defined then invoke
						//                it
						// ---------------------------------------------------------
						try 
						{
							// -----------------------------------------------------
							// 10/04/2015 ECU call the method that handles item selection
							// -----------------------------------------------------
							localMethod.invoke (null);
							// -----------------------------------------------------
						} 
						catch (Exception theException) 
						{		
						} 
						// ---------------------------------------------------------
					}
					break;
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 27/03/2016 ECU the app is not running so try and start it
			// 03/11/2016 ECU changed to use the global alarm manager
			// 04/11/2016 ECU went back to a local alarm manager because was
			//                being called before the global alarm manager set up -
			//                remember reach here if PublicData.stored data
			//                is null or not initialised
			// ---------------------------------------------------------------------
			AlarmManager  localAlarmManager  = (AlarmManager)context.getSystemService (Context.ALARM_SERVICE);
			PendingIntent alarmPendingIntent = PendingIntent.getBroadcast (context,
													localAlarmID,intent,Intent.FLAG_ACTIVITY_NEW_TASK);  
			// ---------------------------------------------------------------------
			// 24/12/2015 ECU changed to use the new method
			// ---------------------------------------------------------------------
			Utilities.SetAnExactAlarm (localAlarmManager,
									   (Calendar.getInstance()).getTimeInMillis() + (StaticData.RESTART_TIME + StaticData.RESTART_INTERRUPT),
									   alarmPendingIntent);
			// ---------------------------------------------------------------------
			// 07/03/2016 ECU added the 'true' argument to indicate that the user
			//                interface should be started without user input
			// ---------------------------------------------------------------------
			MainActivity.restartThisApp (context,true);
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
}

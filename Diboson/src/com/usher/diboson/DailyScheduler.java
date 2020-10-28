package com.usher.diboson;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
	// 20/07/2019 ECU added the code to obtain the 'public IP address'
	// 30/04/2020 ECU change the logic of warning the user of carer visits. Previously
	//                the alarms for all of the events
	//                     warnings each minute prior to the visit
	//                     notice of the visit starting
	//                     notice of the visit ending
	//                were generated in one go. This was changed so that only an
	//                alarm for the first warning event will be set and then it is
	//                down to this event to generate subsequent events. The number
	//                of warnings is set by StaticData.CARE_VISIT_WARNING_PERIOD.
	/* ============================================================================= */
	private final static String TAG = "DailyScheduler";
	/* ============================================================================= */

	// =============================================================================
	// 30/04/2020 ECU for 'carer visits' some arguments are passed in an 'int []'
	//                which is passed in the intent. The position of the arguments
	//                within the array are declared below
	// 03/05/2020 ECU added ...CARER_INDEX
	// -----------------------------------------------------------------------------
	private final static int ARGUMENT_ALARM_TYPE		= 0;
	private final static int ARGUMENT_DAY_OF_WEEK		= 1;
	private final static int ARGUMENT_VISIT_INDEX		= 2;
	private final static int ARGUMENT_WARNING_NUMBER	= 3;
	private final static int ARGUMENT_VISIT_DURATION	= 4;
	private final static int ARGUMENT_ALARM_ID			= 5;
	private final static int ARGUMENT_CARER_INDEX		= 6;
	// =============================================================================

	/* ============================================================================= */
	static	PendingIntent 		alarmPendingIntent;
	static  boolean             initialised				= false;
	/* ============================================================================= */
	public static void Initialise (Context theContext,int theHour,int theMinute)
	{
		// -------------------------------------------------------------------------
		// 04/05/2020 ECU indicate that scheduler is initialised
		// -------------------------------------------------------------------------
		initialised = true;
		// -------------------------------------------------------------------------
		// 04/03/2014 ECU sets up the daily scheduler alarm
		// 14/12/2015 ECU add the 'true' flag to check if time is earlier than current
		//                time
		// -------------------------------------------------------------------------
		SetAnAlarm (theContext,StaticData.ALARM_ID_DAILY_SCHEDULER,
					StaticData.ALARM_ID_DAILY_SCHEDULER,GetTime (theHour,theMinute,true));
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void Initialise (Context theContext,long theTimeWanted)
	{
		// -------------------------------------------------------------------------
		// 04/05/2020 ECU indicate that scheduler is initialised
		// -------------------------------------------------------------------------
		initialised = true;
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
	/* =============================================================================== */
	public static void CancelAlarm (Context theContext,int theID)
	{
		// ---------------------------------------------------------------------------
		// 02/05/2020 ECU added to cancel a specific alarm
		// ---------------------------------------------------------------------------
		PendingIntent localPendingIntent = PendingIntent.getBroadcast (theContext,
				                                                       theID,
																	   new Intent (theContext,DailyScheduler.class),
				                                                       Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_UPDATE_CURRENT);
		// ---------------------------------------------------------------------------
		Utilities.cancelAnAlarm (theContext,localPendingIntent,false);
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
					// 30/04/2020 ECU change the logic of the alarms for the visit
					//                so that instead of setting them all in one go
					//                only set the earliest one and rely on it to set
					//                the next one in the sequence
					// -------------------------------------------------------------
					// 30/04/2020 ECU at this stage 'localAlarmTime' is when the
					//                visit is to start - the earliest event will be
					//                localAlarmTime -
					//                   (StaticData.CARE_VISIT_WARNING_PERIOD * StaticData.MILLISECONDS_PER_MINUTE)
					// -------------------------------------------------------------
					// 30/04/2020 ECU check if there is time for the warnings
					// 04/05/2020 ECU work out the gap between the larm time and the
					//                current time in minutes
					// -------------------------------------------------------------
					int localGapInMinutes = (int) ((localAlarmTime - currentTime) / StaticData.MILLISECONDS_PER_MINUTE);
					// -------------------------------------------------------------
					// 04/05/2020 ECU check if there is time to give the required
					//                number of warnings
					// -------------------------------------------------------------
					if (localGapInMinutes > 0)
					{
						if (localGapInMinutes > StaticData.CARE_VISIT_WARNING_PERIOD)
						{
							// -----------------------------------------------------
							// 04/05/2020 ECU there is enough time to have the full
							//                number of warnings so set to that
							//                value
							// -----------------------------------------------------
							localGapInMinutes = StaticData.CARE_VISIT_WARNING_PERIOD;
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
						// 04/05/2020 ECU changed to use 'localGap..' rather than the
						//                .._WARNING_PERIOD
						// ---------------------------------------------------------
						localAlarmTime = localAlarmTime - (localGapInMinutes * StaticData.MILLISECONDS_PER_MINUTE);
						// ---------------------------------------------------------
						// 04/05/2020 ECU check is there is time
						// ---------------------------------------------------------
						if (localAlarmTime > currentTime)
						{
							// -----------------------------------------------------
							// 30/04/2020 ECU it looks as if the time of the first
							//                warning for the visit has passed
							// 03/05/2020 ECU added the carer index
							// 04/05/2020 ECU changed to use localGapInMinutes
							// -----------------------------------------------------
							SetAnAlarm (theContext,
									localAlarmId,
									StaticData.ALARM_ID_CARE_VISIT,
									localAlarmTime,
									new int [] {StaticData.CARE_VISIT_WARNING,
											localDayOfWeek,
											index,
											localGapInMinutes,
											localVisits.get (index).duration,
											localAlarmId,
											localVisits.get (index).carerIndex});
							// -----------------------------------------------------
							// 02/05/2020 ECU store the id of the alarm for this visit
							// -----------------------------------------------------
							PublicData.carePlan.visits [localDayOfWeek].get (index).AlarmID (theContext,localAlarmId);
							// -----------------------------------------------------
							// 30/04/2020 ECU increment the alarm identifier
							// -----------------------------------------------------
							localAlarmId++;
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
					}
					else
					{
							// ----------------------------------------------------
							// 04/05/2020 ECU there is no time for a warning or the
							//                alarm time has already passed
							// ----------------------------------------------------
							// ----------------------------------------------------
					}
					// ------------------------------------------------------------
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
							SetAnAlarm (theContext,(localAlarmId++),
										StaticData.ALARM_ID_DOSAGE_ALARM,
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
		// -------------------------------------------------------------------------
		SetAnAlarm (theContext,theAlarmID,theParameterID,theTime,null);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	static void SetAnAlarm (Context theContext,int theAlarmID,int theParameterID,long theTime,int theArgument)
	{
		// -------------------------------------------------------------------------
		SetAnAlarm (theContext,theAlarmID,theParameterID,theTime,new int [] {theArgument});
		// -------------------------------------------------------------------------
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
		// 24/07/2017 ECU changed to use ALARM....
		// -------------------------------------------------------------------------
		Utilities.LogToProjectFile (TAG,"SetAnAlarm " + " " + theAlarmID + " " +
				new SimpleDateFormat (StaticData.ALARM_TIME_FORMAT + " " + PublicData.dateFormatDDMMYY,Locale.getDefault()).format(theTime));
		// -------------------------------------------------------------------------
		// 03/11/2016 ECU changed to use the global alarm manager
		// 30/04/2020 ECU store the alarm time in the intent
		// -------------------------------------------------------------------------
		Intent alarmIntent = new Intent (theContext,DailyScheduler.class);
		alarmIntent.putExtra (StaticData.PARAMETER_ALARM_ID,theParameterID);
		alarmIntent.putExtra (StaticData.PARAMETER_ALARM_TIME,theTime);
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
		// 15/03/2019 ECU added FLAG_UPDATE_CURRENT
		// 09/05/2020 ECU changed to use 'ALARM...FLAGS'
		// -------------------------------------------------------------------------
		alarmPendingIntent = PendingIntent.getBroadcast (theContext,
														theAlarmID,
														alarmIntent,
														StaticData.ALARM_PENDING_INTENT_FLAGS);
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
		// 30/04/2020 ECU added localTime
		// -------------------------------------------------------------------------
		int 				localAlarmID 			= StaticData.NO_RESULT;
		int [] 				localArguments			= null;
		MethodDefinition<?>	localMethodDefinition 	= null;
		long				localTime				= StaticData.NOT_SET;
		// -------------------------------------------------------------------------
		// 30/01/2017 ECU Note - check if there are any parameters in the incoming
		//                       intent
		// -------------------------------------------------------------------------
		Bundle extras = intent.getExtras();
		// -------------------------------------------------------------------------
		if (extras != null)
		{
			// ---------------------------------------------------------------------
			// 04/03/2014 ECU get the identifier of the alarm
			// 30/04/2020 ECU get the time the alarm was scheduled for
			// ---------------------------------------------------------------------
			localAlarmID = intent.getIntExtra  (StaticData.PARAMETER_ALARM_ID,StaticData.NOT_SET);
			localTime	 = intent.getLongExtra (StaticData.PARAMETER_ALARM_TIME,StaticData.NOT_SET);
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
		// 04/05/2020 ECU added the check on 'initialised'
		// 08/05/2020 ECU changed to use 'Check...'
		// -------------------------------------------------------------------------
		if (initialised && StoredData.CheckIfInitialised ())
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
				// -----------------------------------------------------------------	
				case StaticData.ALARM_ID_APPOINTMENT_REMINDER :
					// -------------------------------------------------------------
					// 04/03/2014 ECU an appointment reminder has arrived           
					// -------------------------------------------------------------
					AppointmentsActivity.AppointmentReminder (context, localArguments [ARGUMENT_ALARM_TYPE]);
					// -------------------------------------------------------------
				
					// -------------------------------------------------------------
					// 09/03/2014 ECU include the fix because events were not happening
					//                on time - just like the speaking clock
					// 19/07/2017 ECU pass through the context to the fix
					// -------------------------------------------------------------
					APIIssues.Fix001 (context,android.os.Build.VERSION.SDK_INT);
					// -------------------------------------------------------------
				
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------	
				case StaticData.ALARM_ID_APPOINTMENT_TIME :
				
					// -------------------------------------------------------------
					// 04/03/2014 ECU the time of an appointment has arrived
					// -------------------------------------------------------------
					AppointmentsActivity.AppointmentTime (context, localArguments[ARGUMENT_ALARM_TYPE]);
					// -------------------------------------------------------------
				
					// -------------------------------------------------------------
					// 09/03/2014 ECU include the fix because events were not happening
					//                on time - just like the speaking clock
					// 19/07/2017 ECU pass through the context to the fix
					// -------------------------------------------------------------
					APIIssues.Fix001 (context,android.os.Build.VERSION.SDK_INT);
					// -------------------------------------------------------------
				
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case StaticData.ALARM_ID_CARE_VISIT:
					// -------------------------------------------------------------
					// 02/10/2016 ECU created to handle the alarm associated with a
					//                care plan visit
					//            ECU an alarm can be received for the start of a
					//                visit or an advanced warning of a visit
					// 07/12/2016 ECU added try...catch
					// 08/12/2016 ECU add localActions and use carerRecord
					// 01/05/2020 ECU changed to use
					// -------------------------------------------------------------
					try
					{
						// ----------------------------------------------------------
						// 03/05/2020 ECU changed the way the 'carerRecord' is obtained
						//                from :-
						//					= PublicData.carePlan.visits [localArguments[ARGUMENT_DAY_OF_WEEK]].
						//						get(localArguments[ARGUMENT_VISIT_INDEX]).CarerRecord();
						// ----------------------------------------------------------
						Carer  carerRecord  = PublicData.carers.get (localArguments[ARGUMENT_CARER_INDEX]);
						String localActions	= null;
						String localMessage = carerRecord.name + StaticData.NEWLINE;
						// ---------------------------------------------------------
						// 02/10/2016 ECU decide which message is required
						// ---------------------------------------------------------
						switch (localArguments [ARGUMENT_ALARM_TYPE])
						{
							// -----------------------------------------------------
							case StaticData.CARE_VISIT_ARRIVAL:
								// -------------------------------------------------
								// 15/07/2019 ECU check if the carer has already
								//                arrived
								// -------------------------------------------------
								if (!carerRecord.visitActive)
								{
									// ---------------------------------------------
									// 15/07/2019 ECU the carer has not arrived yet
									// 23/09/2020 ECU changed to use the phrase
									// ---------------------------------------------
									localActions	=  PublicData.storedData.visit_start_warning_actions;
									localMessage   +=  carerRecord.Phrase (context,context.getString (R.string.arrive_shortly));
									// ---------------------------------------------
									// 30/04/2020 ECU set up the reminder for the
									//                carer's departure
									// ---------------------------------------------
									localArguments [ARGUMENT_ALARM_TYPE] = StaticData.CARE_VISIT_DEPARTURE;
									// ---------------------------------------------
									SetAnAlarm (context,
												localArguments [ARGUMENT_ALARM_ID],
												StaticData.ALARM_ID_CARE_VISIT,
												localTime + (localArguments [ARGUMENT_VISIT_DURATION]
																		* StaticData.MILLISECONDS_PER_MINUTE),
												localArguments);
									// ---------------------------------------------
								}
								else
								{
									// ---------------------------------------------
									// 15/07/2019 ECU the carer is currently visiting
									// ---------------------------------------------
									localMessage = null;
									// ---------------------------------------------
								}
								break;
							// -----------------------------------------------------
							case StaticData.CARE_VISIT_DEPARTURE:
								// -------------------------------------------------
								// 15/07/2019 ECU check if the carer has already
								//                departed
								// -------------------------------------------------
								if (carerRecord.visitActive)
								{
									// ---------------------------------------------
									// 15/07/2019 ECU the carer has already left
									// 23/09/2020 ECU changed to use the phrase
									// ---------------------------------------------
									localActions	=  PublicData.storedData.visit_end_warning_actions;
									localMessage   +=  carerRecord.Phrase (context,context.getString (R.string.depart_shortly));
									// ---------------------------------------------
								}
								else
								{
									// ---------------------------------------------
									// 15/07/2019 ECU the carer has left
									// ---------------------------------------------
									localMessage = null;
									// ---------------------------------------------
								}
								break;
							// -----------------------------------------------------
							case StaticData.CARE_VISIT_WARNING:
								// -------------------------------------------------
								// 15/07/2019 ECU check if the carer has already
								//                arrived
								// -------------------------------------------------
								if (!carerRecord.visitActive)
								{
									// ---------------------------------------------
									// 23/09/2020 ECU changed to use the verb
									// ---------------------------------------------
									localMessage += carerRecord.Phrase (context,String.format (context.getString (R.string.arrive_warning_format),
												                       localArguments [ARGUMENT_WARNING_NUMBER]) + Utilities.AddAnS (localArguments [ARGUMENT_WARNING_NUMBER]));
									// ---------------------------------------------
									// 30/04/2020 ECU decide whether another warning
									//                of this message is required
									// ---------------------------------------------
									if (localArguments [ARGUMENT_WARNING_NUMBER]-- == 1)
									{
										// -----------------------------------------
										// 30/04/2020 ECU it is time for the start
										//                of the visit; the visit
										//                will start in 1 minute
										// -----------------------------------------
										localArguments [ARGUMENT_ALARM_TYPE] = StaticData.CARE_VISIT_ARRIVAL;
										// -----------------------------------------
									}
									// ---------------------------------------------
									// 30/04/2020 ECU set up the next warning or
									//                'arriving shortly' message
									// ---------------------------------------------
									SetAnAlarm (context,
												localArguments[ARGUMENT_ALARM_ID],
												StaticData.ALARM_ID_CARE_VISIT,
												localTime + StaticData.MILLISECONDS_PER_MINUTE,
												localArguments);
									// ---------------------------------------------
								}
								else
								{
									// ---------------------------------------------
									// 15/07/2019 ECU the carer is currently visiting
									// ---------------------------------------------
									localMessage = null;
									// ---------------------------------------------
								}
								break;
							// -----------------------------------------------------	
						}
						// ---------------------------------------------------------
						// 02/10/2016 ECU now display and speak the message
						// 08/12/2016 ECU changed to use carerRecord
						// 23/06/2017 ECU changed to use Absolute...
						// 15/07/2019 ECU check if anything is to be processed
						// ---------------------------------------------------------
						if (localMessage != null)
						{
							MessageHandler.popToastAndSpeakwithPhoto (localMessage,
																	  Utilities.AbsoluteFileName (carerRecord.photo));
							// -----------------------------------------------------
							// 08/12/2016 ECU if actions have been defined then process
							//                them
							//            ECU changed to use carerRecord
							// 16/11/2019 ECU changed from 'replaceAll' to 'replace' because
							//                the former requires a REGEX so not sure why it ever
							//				  worked
							// -----------------------------------------------------
							if (localActions != null)
							{
								Utilities.actionHandler (context,localActions.replace (StaticData.CARER_REPLACEMENT,carerRecord.name));
							}
							// -----------------------------------------------------
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
					//      5) trim any files in the 'noises'	// 07/12/2019 ECU added
					//         directory that are 'old'
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
				    // 20/07/2019 ECU check the state of the public IP address
				    // -------------------------------------------------------------
				    Utilities.getPublicIpAddressThread (context);
				    // -------------------------------------------------------------
				    // 07/12/2019 ECU check if any old 'noise' files need to be
				    //                deleted
				    // -------------------------------------------------------------
				    Monitor.trimFiles (context);
				    // -------------------------------------------------------------
				    // 25/03/2020 ECU check if the TV schedules need updating
				    // 08/04/2020 ECU commented out
				    // 29/07/2020 ECU added the check on 'epgDailyChec'
				    // -------------------------------------------------------------
				    if (PublicData.storedData.epgDailyCheck)
				    	ShowEPGActivity.DailyCheck (context);
				    // -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
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
			// 14/03/2019 ECU added FLAG_UPDATE_CURRENT
			// 09/05/2020 ECU changed to use the 'ALARM_PEN....FLAGS'
			// ---------------------------------------------------------------------
			AlarmManager  localAlarmManager  = (AlarmManager) context.getSystemService (Context.ALARM_SERVICE);
			PendingIntent alarmPendingIntent = PendingIntent.getBroadcast (context,
																		   localAlarmID,
																		   intent,
																		   StaticData.ALARM_PENDING_INTENT_FLAGS);
			// ---------------------------------------------------------------------
			// 24/12/2015 ECU changed to use the new method
			// ---------------------------------------------------------------------
			Utilities.SetAnExactAlarm (localAlarmManager,
									   (Calendar.getInstance()).getTimeInMillis() + (StaticData.RESTART_TIME + StaticData.RESTART_INTERRUPT),
									   alarmPendingIntent);
			// ---------------------------------------------------------------------
			// 07/03/2016 ECU added the 'true' argument to indicate that the user
			//                interface should be started without user input
			// 13/05/2020 ECU added TAG to identify which receiver did the restart
			// ---------------------------------------------------------------------
			MainActivity.restartThisApp (context,true,TAG);
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
}

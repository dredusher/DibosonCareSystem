package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AppointmentDetails implements Serializable
{
	/* ============================================================================= */
	// 06/01/2014 ECU created to contain details of appointments
	// 08/01/2014 ECU add additional fields
	//                  reminderTrigger - when reminders are to start
	//     				reminderTime - preferred time for reminders  
	//					reminderNextTime = time of next reminder
	// 09/03/2014 ECU changed reminderTime (was long for milliseconds) to
	//                'reminderTimeHour' and 'reminderTimeMinute'
	// 23/10/2016 ECU added recorded notes
	// 25/10/2016 ECU changed recorded notes to be relative to the project folder
	//                rather than being an absolute path
	// 26/10/2016 ECU changed recordedNotes from String to RecordedNote
	// 06/06/2017 ECU changed "\n" to StaticData.NEWLINE
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	public  boolean active;				// whether appointment is active or not
	public  String  name;				// name where appointment is
	public  String  address;			// address of appointment
	public  String	contactName;		// the contact name
	public  String  phoneNumber;		// phone number
	public	long	dateTime;			// stored in millisecond format
	public  String  notes;				// notes
	public  List<RecordedNote>	
					recordedNotes;		// path to file containing recorded notes
	public  long	reminderNextGap;	// gap to next reminder
	public  long    reminderNextTime;	// next reminder time
	public  int		reminderRepeat;		// index into array
	public  String  reminderTrack;		// file to be played each reminder
	public	int		reminderTimeHour;	// preferred hour for reminders
	public 	int     reminderTimeMinute; // preferred minute for reminders
	public  int		reminderTrigger;	// index into array
	public	int		type;				// index into array
	/* ============================================================================= */

	// =============================================================================
	public AppointmentDetails () 
	{
		// -------------------------------------------------------------------------
		// 05/01/2014 ECU construct for the class
		// -------------------------------------------------------------------------
		reminderNextTime = StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
		// 23/10/2016 ECU set the path for recorded notes
		// -------------------------------------------------------------------------
		recordedNotes = null;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public boolean anyAudioNotes ()
	{
		// -------------------------------------------------------------------------
		// 29/11/2016 ECU created to indicate if there are any audio notes
		// -------------------------------------------------------------------------
		if ((recordedNotes != null) && (recordedNotes.size() > 0))
			return true;
		else
			return false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public boolean anyNotes ()
	{
		// -------------------------------------------------------------------------
		// 27/10/2016 ECU created to indicate whether this appoint has any notes
		//                either written or audio
		//                    true ....... notes exist
		//                    false ...... notes do not exist
		// -------------------------------------------------------------------------
		// 27/10/2016 ECU check the written notes
		// 29/11/2016 ECU changed to use the local methods
		// -------------------------------------------------------------------------
		if (anyWrittenNotes())
			return true;
		// -------------------------------------------------------------------------
		// 27/10/2015 ECU now check the audio notes
		// -------------------------------------------------------------------------
		return anyAudioNotes ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public boolean anyWrittenNotes ()
	{
		// -------------------------------------------------------------------------
		// 29/11/2016 ECU created to indicate if there are any written notes
		// -------------------------------------------------------------------------
		return Utilities.emptyString (notes);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void cancelAlarms (Context theContext,int theAppointmentIndex)
	{
		// -------------------------------------------------------------------------
		// 03/11/2016 ECU create to cancel alarms for the specified appointment
		// -------------------------------------------------------------------------
		// 03/11/2016 ECU set up the required pending intent for the timer
		// -------------------------------------------------------------------------
		// 03/11/2016 ECU now request the alarm to be cancelled
		//            ECU changed to use the new getPen.... method
		// -------------------------------------------------------------------------
		Utilities.cancelAnAlarm (theContext,
								 getPendingIntent (theContext,StaticData.ALARM_ID_APPOINTMENT_TIME,theAppointmentIndex),
								 true);
		// -------------------------------------------------------------------------
		// 03/11/2016 ECU check if there is an associated reminder to be cancelled
		// -------------------------------------------------------------------------
		if (PublicData.appointments.get(theAppointmentIndex).reminderTrigger != 0 && 
			PublicData.appointments.get(theAppointmentIndex).reminderNextTime != 0)
		{
			// ---------------------------------------------------------------------
			// 03/11/2016 ECU set up the required pending intent
			// ---------------------------------------------------------------------
			// 03/11/2016 ECU now request the alarm to be cancelled
			//            ECU changed to use the new getPen.... method
			// ---------------------------------------------------------------------
			Utilities.cancelAnAlarm (theContext,
								     getPendingIntent (theContext,StaticData.ALARM_ID_APPOINTMENT_REMINDER,theAppointmentIndex),
								     true);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void cancelAllAlarms (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 03/11/2016 ECU create to cause all appointment alarms to be deleted
		// -------------------------------------------------------------------------
		if (PublicData.appointments.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 03/11/2016 ECU loop through all defined appointments
			// ---------------------------------------------------------------------
			for (int index = 0; index < PublicData.appointments.size(); index++)
			{
				// -----------------------------------------------------------------
				// 03/11/2016 ECU cancel the alarms for the indexed appointment
				// -----------------------------------------------------------------
				cancelAlarms (theContext,index);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static PendingIntent getPendingIntent (Context theContext,int theRequestCode,int theAppointmentIndex)
	{
		// -------------------------------------------------------------------------
		// 03/11/2016 ECU created to simplify the getting of a pending alarm
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (theContext,DailyScheduler.class);
		localIntent.putExtra (StaticData.PARAMETER_ALARM_ID,theRequestCode);
		localIntent.putExtra (StaticData.PARAMETER_ARGUMENTS, new int [] {theAppointmentIndex});
		// -------------------------------------------------------------------------
		return PendingIntent.getBroadcast (theContext, 
										   theRequestCode + theAppointmentIndex,
				                           localIntent,
				                           PendingIntent.FLAG_UPDATE_CURRENT);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public RecordedNote lastRecordedNotes ()
	{
		// -------------------------------------------------------------------------
		// 23/10/2016 ECU created to return the path to the last recorded notes or
		//                null if none exists
		// 26/10/2016 ECU changed to be RecordedNote rather than String
		// -------------------------------------------------------------------------
		if (recordedNotes == null)
		{
			recordedNotes = new ArrayList <RecordedNote> ();
			return null;
		}
		// -------------------------------------------------------------------------
		// 23/10/2016 ECU check the size
		// -------------------------------------------------------------------------
		if (recordedNotes.size () == 0)
		{
			return null;
		}		
		else
		{
			return recordedNotes.get (recordedNotes.size() - 1);
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void playAFile (String theSpokenMessage,String theFileName)
	{
		// -------------------------------------------------------------------------
		// 26/10/2016 ECU created to speak the message before playing the file
		// -------------------------------------------------------------------------
		String localActionString = StaticData.ACTION_DESTINATION_SPEAK + 
	   			 				   StaticData.ACTION_DELIMITER +  
	   			 				   theSpokenMessage + StaticData.ACTION_SEPARATOR;
		// -------------------------------------------------------------------------
		localActionString 		+= StaticData.ACTION_DESTINATION_PLAY +
			     				   StaticData.ACTION_DELIMITER + 
			     				   theFileName + 
			     				   StaticData.ACTION_SEPARATOR;
		// -------------------------------------------------------------------------
		// 23/10/2016 ECU having generated the string then process it
		// -------------------------------------------------------------------------
		Utilities.actionHandler (MainActivity.activity,localActionString);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void playAllRecordedNotes ()
	{
		// -------------------------------------------------------------------------
		// 23/10/2016 ECU created to use the action handler to speak all of the stored
		//                messages
		// 25/10/2016 ECU changed to reflect using relative (to project folder) rather 
		//                than absolute path
		// -------------------------------------------------------------------------
		if (recordedNotes == null || (recordedNotes.size() == 0))
		{
			Utilities.popToastAndSpeak ("There are no recorded notes for this appointment");
		}
		else
		{
			int localLength = recordedNotes.size ();
			// ---------------------------------------------------------------------
			// 23/10/2016 ECU generate an 'action string'
			// 04/11/2016 ECU changed the message to accommodate single entry
			// ---------------------------------------------------------------------
			String localActionString = StaticData.ACTION_DESTINATION_SPEAK + 
									   StaticData.ACTION_DELIMITER + 
									   ((localLength == 1) ? "There is only 1 recorded note for this appointment"
											   			   : "There are " + localLength + " recorded notes for this appointment") +
									   StaticData.ACTION_SEPARATOR;
			// ---------------------------------------------------------------------
			// 25/10/2016 ECU add in the project folder because the record notes is
			//                a path relative to it
			// 26/10/2016 ECU changed to use .fileName
			// ---------------------------------------------------------------------
			for (int index = 0; index < localLength; index++)
			{
				localActionString += StaticData.ACTION_DESTINATION_SPEAK + 
						   			 StaticData.ACTION_DELIMITER +  
						   			 "Entry " + (index + 1) + " created " +
									 recordedNotes.get(index).StartTime(true) + StaticData.ACTION_SEPARATOR;
				localActionString += StaticData.ACTION_DESTINATION_PLAY +
								     StaticData.ACTION_DELIMITER + 
								     PublicData.projectFolder + recordedNotes.get (index).fileName + 
								     StaticData.ACTION_SEPARATOR;
			}
			// ---------------------------------------------------------------------
			// 23/10/2016 ECU tell the user that everything has been read
			// ---------------------------------------------------------------------
			localActionString += StaticData.ACTION_DESTINATION_SPEAK + 
					   			 StaticData.ACTION_DELIMITER + 
					   			 "All of the stored notes have been spoken" + 
					   			 StaticData.ACTION_SEPARATOR;
			// ---------------------------------------------------------------------
			// 23/10/2016 ECU having generated the string then process it
			// ---------------------------------------------------------------------
			Utilities.actionHandler (MainActivity.activity,localActionString);
	 		// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public String Print (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 06/03/2014 ECU added the printing of the 'active' state
		// 09/03/2014 ECU changed with the redefinition of reminderTime
		// 10/11/2014 ECU changed Locale.UK to Locale.getDefault
		// 28/10/2016 ECU changed to use dateFormatDDMMYY
		// 30/10/2016 ECU changed to use Simple....MMYY
		// 31/10/2016 ECU added the context and used resources to provide array
		//                information
		// -------------------------------------------------------------------------
		return 	"Type : " + theContext.getResources().getStringArray(R.array.appointment_type_values) [type] + StaticData.NEWLINE +
				"Name : " + name + StaticData.NEWLINE +
				"Address : " + address + StaticData.NEWLINE +
				"Contact : " + contactName + StaticData.NEWLINE +
				"Phone : " + phoneNumber + StaticData.NEWLINE +
				"Notes : " + notes + StaticData.NEWLINE +
				// -------------------------------------------------------------------------
				// 04/11/2016 ECU check if there are audio notes to add
				// -------------------------------------------------------------------------
				returnRecordedNotesSummary ("Audio Notes : ") +
				// -------------------------------------------------------------------------
				"Active : " + active + StaticData.NEWLINE +
				"Time & Date : " +  PublicData.dateSimpleFormatHHMMDDMMYY.format(dateTime) + StaticData.NEWLINE +
				"Reminder Trigger : " + theContext.getResources().getStringArray(R.array.appointment_reminder_start_values) [reminderTrigger] + StaticData.NEWLINE +
				"Reminder Repeat : " + theContext.getResources().getStringArray(R.array.appointment_reminder_gap_values) [reminderRepeat] + StaticData.NEWLINE +
				"Reminder Preferred Time : " +  Utilities.AdjustedTime(reminderTimeHour, reminderTimeMinute);
	}
	/* ----------------------------------------------------------------------------- */
	public String Print (Context theContext,boolean theFormatFlag)
	{
		// -------------------------------------------------------------------------
		// 06/03/2014 ECU change so that reminder details are only printed if
		//                a reminder is configured
		// 10/11/2014 ECU change Locale.UK to Locale.getDefault()
		// 28/10/2016 ECU changed to use dateFormatDDMMYY
		// 30/10/2016 ECU changed to use Simple....MMYY
		// 31/10/2016 ECU added the context and used resources to provide array
		//                information
		// -------------------------------------------------------------------------
		String theString =
				"Time and Date    : " + PublicData.dateSimpleFormatHHMMDDMMYY.format(dateTime) + StaticData.NEWLINE +
				"Appointment Type : " + theContext.getResources().getStringArray(R.array.appointment_type_values) [type] + StaticData.NEWLINE +
				"Name             : " + name + StaticData.NEWLINE +
				"Address          : " + address.replace (StaticData.NEWLINE,"\n                   ") + StaticData.NEWLINE +                    
				"Contact          : " + contactName + StaticData.NEWLINE +
				"Phone            : " + phoneNumber + StaticData.NEWLINE +
				"Notes            : " + notes.replace (StaticData.NEWLINE,"\n                   ") + StaticData.NEWLINE +
		// -------------------------------------------------------------------------
		// 04/11/2016 ECU check if there are audio notes to add
		// -------------------------------------------------------------------------
				returnRecordedNotesSummary ("Audio Notes      : ") +
		// -------------------------------------------------------------------------
				"Reminder Trigger : " + theContext.getResources().getStringArray(R.array.appointment_reminder_start_values) [reminderTrigger] + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 06/03/2014 ECU if there is a reminder configured then show the details
		// 09/03/2014 ECU changed with the redefinition of reminderTime
		// 17/11/2019 ECU added the final new line
		// -------------------------------------------------------------------------
		if (reminderTrigger != StaticData.APPOINTMENT_NO_REMINDER)
		{
			theString += "Reminder Repeat  : " + theContext.getResources().getStringArray(R.array.appointment_reminder_gap_values) [reminderRepeat] + StaticData.NEWLINE +
						 "Reminder Time    : " +  Utilities.AdjustedTime(reminderTimeHour, reminderTimeMinute) + StaticData.NEWLINE;
		}
		// -------------------------------------------------------------------------
		return theString;
	}
	// =============================================================================
	public String PrintHTML (Context theContext,boolean theFormatFlag)
	{
		// -------------------------------------------------------------------------
		// 06/03/2014 ECU change so that reminder details are only printed if
		//                a reminder is configured
		// 10/11/2014 ECU change Locale.UK to Locale.getDefault()
		// 28/10/2016 ECU changed to use dateFormatDDMMYY
		// 30/10/2016 ECU changed to use Simple.....MMYY
		// 31/10/2016 ECU added the context and used resources to provide array
		//                information
		// -------------------------------------------------------------------------
		String theString =
				"<b>Time and Date</b>\n     " + PublicData.dateSimpleFormatHHMMDDMMYY.format(dateTime) + StaticData.NEWLINE +
				"<b>Appointment Type</b>\n     " + theContext.getResources().getStringArray(R.array.appointment_type_values) [type] + StaticData.NEWLINE +
				"<b>Name</b>\n     " + name + StaticData.NEWLINE +
				"<b>Address</b>\n     " + address.replace (StaticData.NEWLINE,"\n     ") + StaticData.NEWLINE +                    
				"<b>Contact</b>\n     " + contactName + StaticData.NEWLINE +
				"<b>Phone</b>\n     " + phoneNumber + StaticData.NEWLINE +
				"<b>Notes</b>\n     " + notes.replace (StaticData.NEWLINE,"\n     ") + StaticData.NEWLINE +
				// -------------------------------------------------------------------------
				// 04/11/2016 ECU check if there are audio notes to add
				// -------------------------------------------------------------------------
				"<b>Audio Notes</b>\n" + returnRecordedNotesSummary ("     ") + 
				// -------------------------------------------------------------------------
				"<b>Reminder Trigger</b>\n     " + theContext.getResources().getStringArray(R.array.appointment_reminder_start_values) [reminderTrigger] + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 06/03/2014 ECU if there is a reminder configured then show the details
		// 09/03/2014 ECU changed with the redefinition of reminderTime
		// -------------------------------------------------------------------------
		if (reminderTrigger != StaticData.APPOINTMENT_NO_REMINDER)
		{
			theString += "<b>Reminder Repeat</b>\n     " + theContext.getResources().getStringArray(R.array.appointment_reminder_gap_values) [reminderRepeat] + StaticData.NEWLINE +
						 "<b>Reminder Time</b>\n     " +  Utilities.AdjustedTime(reminderTimeHour, reminderTimeMinute);
		}
		// -------------------------------------------------------------------------
		// 28/03/2016 ECU include the 'body' envelope and do any necessary
		//                replacements
		// 30/07/2017 ECU changed to use HTML_...
		// 16/11/2019 ECU changed from 'replaceAll' to 'replace' because
		//                the former requires a REGEX so not sure why it ever
		//				  worked
		// -------------------------------------------------------------------------
		return "<body>" + 
			   theString.replace (StaticData.NEWLINE,StaticData.HTML_BREAK).replace (" ", "&nbsp;") +
			   "</body>";
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String printSelected (Context theContext,List<AppointmentDetails> theAppointments)
	{
		// -------------------------------------------------------------------------
		// 01/03/2017 ECU created to generate a summary string of the specified
		//                appointments
		// -------------------------------------------------------------------------
		String summaryString = StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
		// 01/03/2017 ECU loop through all appointments in the list
		// -------------------------------------------------------------------------
		for (int appointment = 0; appointment < theAppointments.size(); appointment++)
		{
			// ---------------------------------------------------------------------
			// 01/03/2017 ECU add a summary of the indexed appointment into the
			//                summary
			// 02/03/2017 ECU added 'true' argument to get get the correct format
			// ---------------------------------------------------------------------
			summaryString += theAppointments.get (appointment).Print (theContext,true);
			// ---------------------------------------------------------------------
			// 02/03/2017 ECU add in a separator
			// ---------------------------------------------------------------------
			summaryString += StaticData.SEPARATOR;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 01/03/2017 ECU return the generated summary
		// -------------------------------------------------------------------------
		return summaryString;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	String PrintSummary ()
	{
		// -------------------------------------------------------------------------
		// 27/01/2015 ECU return a summary of the appointment
		// 28/10/2016 ECU changed to use dateFormatDDMMYY
		// 30/10/2016 ECU changed to use Simple....MMYY
		// -------------------------------------------------------------------------
		return (PublicData.dateSimpleFormatHHMMDDMMYY.format(dateTime));
	}
	// =============================================================================
	public String returnRecordedNotesSummary (String theTitle)
	{
		// -------------------------------------------------------------------------
		// 04/11/2016 ECU create a printable summary of the recorded notes
		//                or 'empty string' if nothing exists
		// 20/03/2017 ECU changed to use BLANK....
		// -------------------------------------------------------------------------
		if (recordedNotes != null && (recordedNotes.size() > 0))
		{
			String localString = StaticData.BLANK_STRING;
			String localIndent = new String (new char [theTitle.length()]).replace('\0',' ');
			// ---------------------------------------------------------------------
			// 04/11/2016 ECU loop for all stored notes
			// ---------------------------------------------------------------------
			for (int index = 0; index < recordedNotes.size(); index++)
			{
				// -----------------------------------------------------------------
				// 25/10/2016 ECU include the project folder because notes path is now
				//                relative to it
				// 26/10/2016 ECU changed to use the stored time
				// -----------------------------------------------------------------
				localString += theTitle + recordedNotes.get(index).StartTime (false) + "  " + recordedNotes.get(index).Duration() + StaticData.NEWLINE; 
				// -----------------------------------------------------------------
				theTitle = localIndent;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 04/11/2016 ECU return the generated string
			// ---------------------------------------------------------------------
			return localString;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 04/11/2016 ECU no notes so indicate that fact
			// ---------------------------------------------------------------------
			return StaticData.BLANK_STRING;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public String [] returnRecordedNotesTitles ()
	{
		// -------------------------------------------------------------------------
		// 23/10/2016 ECU created to generated the file names for the stored
		//                recorded notes
		// -------------------------------------------------------------------------
		String [] localTitles = new String [recordedNotes.size() + 1];
		// -------------------------------------------------------------------------
		localTitles [0] = "Play all of the recorded notes";
		// -------------------------------------------------------------------------
		for (int index = 0; index < recordedNotes.size(); index++)
		{
			// ---------------------------------------------------------------------
			// 25/10/2016 ECU include the project folder because notes path is now
			//                relative to it
			// 26/10/2016 ECU changed to use the stored time
			// ---------------------------------------------------------------------
			localTitles [index + 1] = recordedNotes.get(index).StartTime (false) + "  " + recordedNotes.get(index).Duration(); 
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 23/10/2016 ECU return the generated array
		// -------------------------------------------------------------------------
		return localTitles;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public long ReminderTime ()
	{
		// -------------------------------------------------------------------------
		// 09/03/2014 ECU returns the stored reminder time components as the time
		//                in milliseconds
		// 01/09/2015 ECU changed to use StaticData
		// -------------------------------------------------------------------------
		return (reminderTimeHour * StaticData.MILLISECONDS_PER_HOUR) + (reminderTimeMinute * StaticData.MILLISECONDS_PER_MINUTE);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
}

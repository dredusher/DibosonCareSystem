package com.usher.diboson;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AlarmData implements Serializable,Cloneable
{
	/* ============================================================================= */
	// 18/05/2017 ECU added the 'repeat' facility. If the 'repeat' facility is not
	//                being used then the arguments will be set to StaticData.NO_RESULT
	// 26/11/2017 ECU changed to 'Cloneable' and added 'clone' and 'Clone' methods
	//                - see the reason in raw/documentation_notes
	// -----------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	public  	int 			action;				// the action that is to be taken
	public      String			actions;			// 30/04/2017 ECU added
	public 		int				associatedData;		// 06/02/2015 ECU added any required associated data
	public  	Calendar		calendar;			// when the alarm is to be action
	public 		long     		id;                 // the unique id
	public      String			message;			// 09/02/2015 ECU added
	public      Object          object;				// 29/09/2015 ECU added
	public		int				repeatInterval;		// 18/05/2017 ECU added - in minutes
	public      int				repeatNumber;		// 18/05/2017 ECU added 
	public 		int 			requestCode;		// unique request code sent in the pending intent to
													// the alarm manager - documentation says that it is
	                       							// not used
	/* ============================================================================= */
	public AlarmData (int 		theAction, 
					  Calendar 	theCalendar, 
					  long 		theID, 
					  int 		theRequestCode,
					  int 		theAssociatedData,
					  String 	theMessage,
					  String    theActions,
					  Object 	theObject,
					  int		theRepeatInterval,
					  int		theRepeatNumber)
	{
		// -------------------------------------------------------------------------
		// 18/05/2017 ECU Note - the 'repeat' arguments were added - making this the
		//                       main constructor
		// -------------------------------------------------------------------------
		// 04/12/2013 ECU declared the constructor rather than use an Initialise method
		// -------------------------------------------------------------------------
		action 			= theAction;
		id				= theID;
		calendar 		= theCalendar;
		requestCode		= theRequestCode;
		// -------------------------------------------------------------------------
		// 06/02/2015 ECU include the associated data which is only of use when the
		//                action is ALARM_ACTION_ACTIVITY and the associated data
		//                contains the position of the active image to be actioned.
		// -------------------------------------------------------------------------
		associatedData	= theAssociatedData;
		// -------------------------------------------------------------------------
		// 09/02/2015 ECU added any free text message
		// -------------------------------------------------------------------------
		message			= theMessage;
		// -------------------------------------------------------------------------
		// 14/07/2015 ECU added the email message
		// 09/03/2016 ECU changed to use the object rather than specifically
		//                the email message
		// -------------------------------------------------------------------------
		object			= theObject;
		// -------------------------------------------------------------------------
		// 30/04/2017 ECU added for the 'actions' type of alarm
		// -------------------------------------------------------------------------
		actions			= theActions;
		// -------------------------------------------------------------------------
		// 18/05/2017 ECU set up the 'repeat' parameters
		// -------------------------------------------------------------------------
		repeatInterval	= theRepeatInterval;
		repeatNumber	= theRepeatNumber;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public AlarmData (int 		theAction, 
					  Calendar 	theCalendar, 
					  long 		theID, 
					  int 		theRequestCode,
					  int 		theAssociatedData,
					  String 	theMessage,
					  String    theActions,
					  Object 	theObject)
	{
		// -------------------------------------------------------------------------
		// 18/05/2017 ECU Note - this used to be the main constructor before the
		//                       'repeat' arguments were added
		// -------------------------------------------------------------------------
		this (theAction,theCalendar,theID,theRequestCode,theAssociatedData,theMessage,theActions,theObject,StaticData.NO_RESULT,StaticData.NO_RESULT);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public AlarmData (int 		theAction, 
			  		  Calendar 	theCalendar, 
			  		  long 		theID, 
			  		  int 		theRequestCode,
			  		  int 		theAssociatedData,
			  		  String 	theMessage)
	{
		// -------------------------------------------------------------------------
		// 14/07/2015 ECU created - was the old master method until the emailMessage
		//                was added
		// 30/04/2017 ECU added additional 'null' with the addition of 'actions'
		// -------------------------------------------------------------------------
		this (theAction,theCalendar,theID,theRequestCode,StaticData.NO_RESULT,null,null,null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public AlarmData (int theAction, Calendar theCalendar, long theID, int theRequestCode)
	{
		// -------------------------------------------------------------------------
		// 06/02/2015 ECU call the construct which nows has Intent as an argument
		// -------------------------------------------------------------------------
		this (theAction,theCalendar,theID,theRequestCode,StaticData.NO_RESULT,null);
	}
	// -----------------------------------------------------------------------------
	public AlarmData ()
	{
		// -------------------------------------------------------------------------
		// 09/02/2015 ECU just a dummy constructor
		// 03/09/2015 ECU changed to use StaticData
		// -------------------------------------------------------------------------
		this (StaticData.ALARM_ACTION_NONE,null,0l,StaticData.NO_RESULT);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public AlarmData (int theAction,Calendar theCalendar,long theId,int theRequestCode,Object theObject)
	{
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU created to set specific EPG alarm
		// -------------------------------------------------------------------------
		this (theAction,theCalendar,theId,theRequestCode);
		// -------------------------------------------------------------------------
		this.object = theObject;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static ArrayList<ListItem> BuildList ()
	{
		// -------------------------------------------------------------------------
		// 01/04/2014 ECU created to build up the list for the Selector class
		// -------------------------------------------------------------------------
		ArrayList<ListItem> listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 27/11/2014 ECU add in the check on size
		// 08/02/2015 ECU added associated data
		// 18/12/2015 ECU for some reason was displaying a photo of first medication
		//                - took this out
		// 26/01/2017 ECU changed the displayed '.action. to be in free text
		//            ECU general tidy up of the formatting for associated data
		//                and message
		// 20/03/2017 ECU changed to use BLANK....
		// 30/04/2017 ECU added 'actions'
		// 18/05/2017 ECU added 'repeat' options
		// -------------------------------------------------------------------------
		if (PublicData.alarmData.size() > 0)
		{
			for (int theIndex = 0; theIndex < PublicData.alarmData.size(); theIndex++)
			{
				listItems.add (new ListItem (PublicData.alarmDateFormat.format (PublicData.alarmData.get(theIndex).calendar.getTime()),
						PublicData.alarmTimeFormat.format (PublicData.alarmData.get(theIndex).calendar.getTime()),
						"action : " + Utilities.AlarmActionsAsString (MainActivity.activity,PublicData.alarmData.get(theIndex).action) + 
						((PublicData.alarmData.get(theIndex).associatedData == StaticData.NO_RESULT) ? StaticData.BLANK_STRING 
								                                                                     : (StaticData.NEWLINE + "    associated data : " + PublicData.alarmData.get(theIndex).associatedData)) + 
						((PublicData.alarmData.get(theIndex).message == null) ? StaticData.BLANK_STRING 
								                                              : (StaticData.NEWLINE + "    message : " + PublicData.alarmData.get(theIndex).message)) +
						// ---------------------------------------------------------
					    // 28/11/2017 ECU print out the object
					    // ---------------------------------------------------------
						PublicData.alarmData.get(theIndex).printObject (StaticData.NEWLINE) + 		                                              
						// ---------------------------------------------------------		                                              
						((PublicData.alarmData.get(theIndex).actions == null) ? StaticData.BLANK_STRING 
								                                              : (StaticData.NEWLINE + "    actions : " + PublicData.alarmData.get(theIndex).actions)) +
			            ((PublicData.alarmData.get(theIndex).repeatInterval == StaticData.NO_RESULT) ? StaticData.BLANK_STRING
			            		                                              : (StaticData.NEWLINE + "    repeat : every " + PublicData.alarmData.get(theIndex).repeatInterval 
			            		                                            		                            + " min. (" + PublicData.alarmData.get(theIndex).repeatNumber + " times)")),
			            		                                              theIndex));
						// ---------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		return listItems;
	}
	// =============================================================================
	public void cancelAlarm (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 08/02/2015 ECU delete an alarm using the stored data
		// 31/10/2015 ECU added the try/catch
		// 03/11/2016 ECU changed to use the global alarm manager
		// 29/01/2017 ECU changed name from 'deleteAlarm'
		// -------------------------------------------------------------------------
		try
		{ 
			PublicData.alarmManager.cancel (setPendingIntent (theContext));
		}
		catch (Exception theException)
		{
			
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void cancelAllAlarms (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 29/01/2017 ECU created to be called to cancel all stored alarms but not
		//                to alter the data
		// -------------------------------------------------------------------------
		if (PublicData.alarmData != null)
		{
			// ---------------------------------------------------------------------
			// 29/01/2017 ECU loop through all alarms
			// ---------------------------------------------------------------------
			for (int alarm = 0; alarm < PublicData.alarmData.size(); alarm++)
			{
				// -----------------------------------------------------------------
				// 29/01/2017 ECU cancel this specific alarm
				// -----------------------------------------------------------------
				PublicData.alarmData.get (alarm).cancelAlarm (theContext);
				// -----------------------------------------------------------------
			}
		}
	}
	// =============================================================================
	public boolean createAlarm (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 08/02/2015 ECU create an alarm using the stored data
		// 31/10/2015 ECU added the try/catch and changed to return the result
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 24/12/2015 ECU changed to use the new method
			// 03/11/2016 ECU changed to use the global alarm manager
			// ---------------------------------------------------------------------
			Utilities.SetAnExactAlarm (PublicData.alarmManager,
									   calendar.getTimeInMillis(),
									   setPendingIntent (theContext));
			// ---------------------------------------------------------------------
			// 31/10/2015 ECU indicate that all was well
			// ---------------------------------------------------------------------
			return true;
			// ----------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 31/10/2015 ECU added to protect against a NPE that was occurring
			//                when TV program alarms were being action on start up -
			//                indicate in the return code that error occurred
			// --------------------------------------------------------------------- 
			return false;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static Calendar getCalendar (long theTimeInMillis)
	{
		// -------------------------------------------------------------------------
		// 29/09/2015 ECU created to return the Calendar corresponding to the
		//                date specified in the argument which is given in milliSecs
		// -------------------------------------------------------------------------
		Calendar localCalendar = Calendar.getInstance();
		localCalendar.setTimeInMillis(theTimeInMillis);
		return localCalendar;
		// --------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public String Print (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 18/05/2017 ECU add the 'repeat' arguments
		// -------------------------------------------------------------------------
		String theString;
		// -------------------------------------------------------------------------
		theString = "Action = " + Utilities.AlarmActionsAsString (theContext,action)    + StaticData.NEWLINE +
		            "Date = " + PublicData.alarmDateFormat.format (calendar.getTime()) + StaticData.NEWLINE +
		            "Time = " + PublicData.alarmTimeFormat.format (calendar.getTime()) + StaticData.NEWLINE +
		            "Identifier = " + id + StaticData.NEWLINE +
				    "RequestCode = " + requestCode;
		// -------------------------------------------------------------------------
		// 08/06/2017 ECU conditionally display the associated data
		// --------------------------------------------------------------------------
		if (associatedData !=StaticData.NO_RESULT)
			theString += StaticData.NEWLINE + "Associated Data = " + associatedData;
		// -------------------------------------------------------------------------
		// 09/02/2015 ECU add in the message if set
		// -------------------------------------------------------------------------
		if (message != null)
			theString += "\nMessage = " + message;
		// -------------------------------------------------------------------------
	    // 28/11/2017 ECU print out the object
	    // -------------------------------------------------------------------------
		theString += printObject (StaticData.NEWLINE); 		                                              
		// -------------------------------------------------------------------------
		// 30/04/2017 ECU add in the 'actions' if set
		// -------------------------------------------------------------------------
		if (actions != null)
			theString += StaticData.NEWLINE + "Actions = " + actions;
		// -------------------------------------------------------------------------
		// 18/05/2017 ECU add details of the 'repeat' arguments
		// -------------------------------------------------------------------------
		if (repeatInterval != StaticData.NO_RESULT)
		{
			// ---------------------------------------------------------------------
			// 18/05/2017 ECU print out the arguments
			// ---------------------------------------------------------------------
			theString += StaticData.NEWLINE + "Repeat Interval = " + repeatInterval;
			theString += StaticData.NEWLINE + "Repeat Number = "   + repeatNumber;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		return theString;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public String Print (Context theContext,boolean theFormatFlag)
	{
		// -------------------------------------------------------------------------
		// 02/03/2017 ECU added theFormatFlag to get a unique method to get required
		//                formatted layout 
		String theString;
		// --------------------------------------------------------------------------
		theString = "Action          : " + Utilities.AlarmActionsAsString(theContext,action)    + StaticData.NEWLINE +
		            "Date            : " + PublicData.alarmDateFormat.format (calendar.getTime()) + StaticData.NEWLINE +
		            "Time            : " + PublicData.alarmTimeFormat.format (calendar.getTime()) + StaticData.NEWLINE +
		            "Identifier      : " + id + StaticData.NEWLINE +
				    "RequestCode     : " + requestCode;
		// -------------------------------------------------------------------------
		// 19/05/2017 ECU added the check as to whether there is any associated data
		// -------------------------------------------------------------------------
		if (associatedData != StaticData.NO_RESULT)
			theString += StaticData.NEWLINE + "Associated Data : " + associatedData;
		// -------------------------------------------------------------------------
		// 09/02/2015 ECU add in the message if set
		// -------------------------------------------------------------------------
		if (message != null)
			theString += StaticData.NEWLINE + "Message         : " + message;
		// -------------------------------------------------------------------------
	    // 28/11/2017 ECU print out the object
	    // -------------------------------------------------------------------------
		theString += printObject (StaticData.NEWLINE); 		
		// -------------------------------------------------------------------------
		// 30/04/2017 ECU add in the 'actions' if set
		// 19/05/2017 ECU corrected an error - was showing 'message'
		// -------------------------------------------------------------------------
		if (actions != null)
			theString += StaticData.NEWLINE + "Actions         : " + actions;
		// -------------------------------------------------------------------------
		// 19/05/2017 ECU add details of the 'repeat' arguments
		// -------------------------------------------------------------------------
		if (repeatInterval != StaticData.NO_RESULT)
		{
			// ---------------------------------------------------------------------
			// 18/05/2017 ECU print out the arguments
			// ---------------------------------------------------------------------
			theString += StaticData.NEWLINE + "Repeat Interval : " + repeatInterval + " minute" + Utilities.AddAnS (repeatInterval);
			theString += StaticData.NEWLINE + "Repeat Number   : " + repeatNumber;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 02/03/2017 ECU add a newline and return the summary string
		// -------------------------------------------------------------------------
		return theString  + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static String PrintAll (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 06/02/2015 ECU print everything in the alarmData list array
		// 03/04/2016 ECU added the context
		// 19/03/2017 ECU changed to use resources
		//            ECU took out the title as an argument
		// -------------------------------------------------------------------------
		// 19/03/2017 ECU check if null or zero size - indicate to the caller
		// -------------------------------------------------------------------------
		if ((PublicData.alarmData != null) && (PublicData.alarmData.size() > 0))
		{
			// ---------------------------------------------------------------------
			String resultString = StaticData.BLANK_STRING;
			// ---------------------------------------------------------------------
			for (int theIndex = 0; theIndex < PublicData.alarmData.size(); theIndex++)
			{
				resultString += PublicData.alarmData.get(theIndex).Print (theContext) + 
									StaticData.NEWLINE + StaticData.NEWLINE;
			}
			// ---------------------------------------------------------------------
			return resultString;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 19/03/2017 ECU indicate that there are no stored alarms
			// ---------------------------------------------------------------------
			return theContext.getString (R.string.stored_alarms_none);
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	String printObject (String theHeader)
	{
		// -------------------------------------------------------------------------
		// 28/11/2017 ECU print out the appropriate 'contents' depending on what
		//                the object is being used to store
		// 29/11/2017 ECU add BLANK_STRING to indicate that the dose information is
		//                not to be indented.
		// -------------------------------------------------------------------------
		if (object != null)
		{
			// ---------------------------------------------------------------------
			if (object instanceof DoseTime)
			{
				return theHeader + "Medication : " + PublicData.medicationDetails.get(associatedData).name + StaticData.NEWLINE + 
							((DoseTime) object).PrintDoseTime (StaticData.BLANK_STRING);
			}
			else
			if (object instanceof EmailMessage)
			{
				return theHeader + ((EmailMessage) object).Print ();
			}
			else
			{
				return StaticData.BLANK_STRING;
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			return StaticData.BLANK_STRING;
		}
	}
	// =============================================================================
	public static String printSelected (Context theContext,List<AlarmData> theAlarms)
	{
		//--------------------------------------------------------------------------
		// 01/03/2017 ECU created to return a summary string of the specified alarm
		//                list
		// -------------------------------------------------------------------------
		String summaryString = StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
		// 01/03/2017 ECU loop through all alarms in the list
		// -------------------------------------------------------------------------
		for (int alarm = 0; alarm < theAlarms.size(); alarm++)
		{
			// ---------------------------------------------------------------------
			// 01/03/2017 ECU add a summary of the indexed alarm into the summary
			// 02/03/2017 ECU added the 'true' argument to get required formatting
			// ---------------------------------------------------------------------
			summaryString += theAlarms.get(alarm).Print (theContext,true);
			// ---------------------------------------------------------------------
			// 02/03/2017 ECU add in a separator
			// ---------------------------------------------------------------------
			summaryString += StaticData.SEPARATOR;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 01/03/2017 ECU return the generated summary string
		// -------------------------------------------------------------------------
		return summaryString;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public PendingIntent setPendingIntent (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 08/02/2015 ECU created to use the stored values to set the pending intent
		//                that will be used for various alarm tasks
		//            ECU changed to use the PARAMETER_
		// 09/02/2015 ECU added the message
		//            ECU changed to send the whole object rather than individual
		//                fields
		// 04/03/2017 ECU class changed from AlarmService
		// -------------------------------------------------------------------------
		Intent alarmIntent = new Intent (theContext, AlarmReceiver.class);
		// -------------------------------------------------------------------------
		// 07/10/2017 ECU because of an issue that appeared in Nougat (7.0) where
		//                passing the serializable within the intent was being 
		//                received as 'null' rather than the actual data. This is
		//                described in 'documentation_notes. By passing a named
		//                bundle that contains the serializable seems to solve the
		// 				  problem.
		// -------------------------------------------------------------------------
		Bundle alarmBundle = new Bundle ();
		// -------------------------------------------------------------------------
		// 07/10/2017 ECU store the parameter in the bundle
		// -------------------------------------------------------------------------
		alarmBundle.putSerializable (StaticData.PARAMETER_ALARM_DATA,this);
		// -------------------------------------------------------------------------
		// 07/10/2017 ECU store the bundle in the intent
		// -------------------------------------------------------------------------
		alarmIntent.putExtra (StaticData.PARAMETER_ALARM_BUNDLE,alarmBundle);
		// -------------------------------------------------------------------------
		// 08/02/2015 ECU having set the intent then return the associated pending
		//                intent
		// 14/03/2017 ECU added PendingIntent.FLAG_UPDATE_CURRENT
		// -------------------------------------------------------------------------
		return (PendingIntent.getBroadcast (theContext,(int)id,alarmIntent,Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_UPDATE_CURRENT));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void setRepeatAlarm (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 18/05/2017 ECU created to set a repeated alarm if one exists
		// -------------------------------------------------------------------------
		// 18/05/2017 ECU decrement the repeat counter
		// -------------------------------------------------------------------------
		repeatNumber--;
		// -------------------------------------------------------------------------
		// 18/05/2017 ECU check if everything done
		// -------------------------------------------------------------------------
		if (repeatNumber > 0)
		{
			// ---------------------------------------------------------------------
			// 18/05/2017 ECU get the current alarm time and add in the interval
			// ---------------------------------------------------------------------
			long nextAlarmTime = calendar.getTimeInMillis () + (repeatInterval * StaticData.MILLISECONDS_PER_MINUTE);	
			// ---------------------------------------------------------------------
			// 18/05/2017 ECU get a fresh instance of the calendar because of
			//                problems experienced in the past
			// ---------------------------------------------------------------------
			calendar = Calendar.getInstance ();
			// ---------------------------------------------------------------------
			// 18/05/2017 ECU set up the calendar for the next alarm
			// ---------------------------------------------------------------------
			calendar.setTimeInMillis (nextAlarmTime);
			// ---------------------------------------------------------------------
			// 18/05/2017 ECU create a new alarm at the required time
			// ---------------------------------------------------------------------
			createAlarm (theContext);
			// ---------------------------------------------------------------------
			// 18/05/2017 ECU add this alarm into the public list
			// ---------------------------------------------------------------------
			PublicData.alarmData.add (this);
			// ---------------------------------------------------------------------
			// 18/05/2017 ECU make sure that the disk copy is updated
			// ---------------------------------------------------------------------
			AsyncUtilities.writeObjectToDisk (PublicData.alarmFileName,PublicData.alarmData);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Time ()
	{
		// -------------------------------------------------------------------------
		// 09/02/2015 ECU generate a string when needing the summary of a free text
		//                message to display
		// 15/07/2015 ECU added the try/catch because have an occasional situation
		//                where the method is called with 'alarm..Format' null.
		// 04/03/2016 ECU used to use PublicData.alarmDate/TimeFormat but if the
		//                alarm happened when the app was not running then PublicData
		//                would be null and an exception thrown
		// -------------------------------------------------------------------------
		try 
		{
			return  (new SimpleDateFormat (StaticData.ALARM_DATE_FORMAT,Locale.getDefault())).format (calendar.getTime()) + StaticData.NEWLINE +
					(new SimpleDateFormat (StaticData.ALARM_TIME_FORMAT,Locale.getDefault())).format (calendar.getTime());
		}
		catch (Exception theException)
		{
			return "Exception : " + theException;
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	protected Object clone () throws CloneNotSupportedException
	{
		// -------------------------------------------------------------------------
		// 26/11/2017 ECU created with the implementation of cloneable in this class
		// -------------------------------------------------------------------------
		return super.clone ();
	}
	// =============================================================================
	
	// =============================================================================
	AlarmData Clone ()
	{
		// -------------------------------------------------------------------------
		// 26/11/2017 ECU use the 'clone' method and take into account any exceptions
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 26/11/2017 ECU return a clone of this object
			// ---------------------------------------------------------------------
			return (AlarmData) this.clone ();
			// ---------------------------------------------------------------------
		}
		catch (CloneNotSupportedException theException)
		{
			// ---------------------------------------------------------------------
			// 27/11/2017 ECU as this class is cloneable this should never occur
			//                but 'just in case'
			// ---------------------------------------------------------------------
			return this;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
}

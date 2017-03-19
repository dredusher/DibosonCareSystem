package com.usher.diboson;

import java.util.ArrayList;
import java.util.Calendar;

import com.belkin.wemo.localsdk.WeMoDevice;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

public class WeMoTimerActivity extends FragmentActivity 
{
	// =============================================================================
	// 02/11/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// =============================================================================
	static 	int						editRecordIndex		= StaticData.NO_RESULT;
	static 	String					friendlyName		= null;		// 27/02/2015 ECU added
	static	boolean					timeSet				= false;	// 01/03/2015 ECU added
	static 	ArrayList<WeMoDevice>	wemoDevices 		= new ArrayList<WeMoDevice> (); 
	static 	TextView				wemoTimerTextView;
	// =============================================================================
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 02/11/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity(this);
			// ---------------------------------------------------------------------
			if (WeMoActivity.serviceRunning)
			{
				setContentView (R.layout.activity_we_mo_timer);
				// -----------------------------------------------------------------
				wemoTimerTextView = (TextView) findViewById (R.id.wemo_timer_textview);
				// -----------------------------------------------------------------
				wemoDevices = WeMoService.returnDevices();
				// -----------------------------------------------------------------
				// 25/02/2015 ECU display the devices that have been found
				// -----------------------------------------------------------------
				Intent localIntent = new Intent (this,Selector.class);
				localIntent.putExtra (StaticData.PARAMETER_OBJECT_TYPE,StaticData.OBJECT_WEMO_TIMER);
				localIntent.putExtra (StaticData.PARAMETER_BACK_KEY,true);
				localIntent.putExtra (StaticData.PARAMETER_METHOD,new MethodDefinition<WeMoTimerActivity> (WeMoTimerActivity.class,"Timers"));
				startActivity (localIntent);
				// -----------------------------------------------------------------
				// 27/02/2015 ECU and can exit this activity
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 20/02/2015 ECU the service isn't running so just finish
				// -----------------------------------------------------------------
				Utilities.popToast ("Cannot start because the service is not running");
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/11/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return true;
	}
	// =============================================================================
	public static void checkTimers (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 25/02/2015 ECU called each minute to check if a timer is to be actioned
		// 11/12/2016 ECU changed to use the DayOfWeek method which adjusts the day
		//                because Android's week starts on Sunday whereas the week
		//                in this app starts on Monday and is 0 instead of 1
		// -------------------------------------------------------------------------
		Calendar currentDateTime = Calendar.getInstance();
		int day	 	 = Utilities.DayOfWeek (currentDateTime.get (Calendar.DAY_OF_WEEK));
		int hour	 = currentDateTime.get (Calendar.HOUR_OF_DAY);
		int minute	 = currentDateTime.get (Calendar.MINUTE);
		// -------------------------------------------------------------------------
		// 25/02/2015 ECU loop through each timer
		// 05/03/2015 ECU put in the check on 'null'
		// -------------------------------------------------------------------------
		if (PublicData.storedData.wemoTimers != null)
		{
			for (int theTimer = 0; theTimer < PublicData.storedData.wemoTimers.size(); theTimer++)
			{
				if (PublicData.storedData.wemoTimers.get(theTimer).checkTimer (day,hour,minute))
				{
					// -------------------------------------------------------------
					// 25/02/2015 ECU this timer is to be actioned
					// -------------------------------------------------------------
					PublicData.storedData.wemoTimers.get(theTimer).actionTimer ();
					// -------------------------------------------------------------
				}	
			}
		}	
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void exitEditMode ()
	{
		// -------------------------------------------------------------------------
		// 01/03/2015 ECU this method takes any actions to exit edit mode
		// -------------------------------------------------------------------------
		// 01/03/2015 ECU and reset the index to default back to 'add' mode
		// -------------------------------------------------------------------------
		editRecordIndex = StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void getADate (final String theTitle)
	{
		Calendar currentDateTime = Calendar.getInstance();
		int day	 	= currentDateTime.get (Calendar.DAY_OF_MONTH);
		int month	= currentDateTime.get (Calendar.MONTH);
		int year	= currentDateTime.get (Calendar.YEAR);
		// -------------------------------------------------------------------------
		DatePickerDialog datePicker;
		datePicker 	= new DatePickerDialog(Selector.context, new DatePickerDialog.OnDateSetListener() 
		{
			@Override
			public void onDateSet (DatePicker view, int selectedYear, int selectedMonth,int selectedDay) 
			{
				Utilities.popToast (theTitle + " : " + selectedDay + "/" + selectedMonth + "/" + selectedYear);
			}

		}, year, month, day);
		// -------------------------------------------------------------------------
		// 01/03/2015 ECU set the title and display the dialogue
		// -------------------------------------------------------------------------
		datePicker.setTitle (theTitle);
		datePicker.show ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void getATime (final String theTitle,final WeMoTimer theWeMoTimer)
	{
		int 	hour		=	theWeMoTimer.hour;
		int		minute		=	theWeMoTimer.minute;
		// -------------------------------------------------------------------------
		// 27/02/2015 ECU check if editing an existing timer
		// -------------------------------------------------------------------------
		if (hour == StaticData.NO_RESULT && minute == StaticData.NO_RESULT)
		{
			// ---------------------------------------------------------------------
			// 27/02/2015 ECU not editing an existing timer so display the current
			//                time
			// ---------------------------------------------------------------------
			Calendar currentDateTime = Calendar.getInstance();
			hour	 = currentDateTime.get (Calendar.HOUR_OF_DAY);
			minute	 = currentDateTime.get (Calendar.MINUTE);
		}
		// -------------------------------------------------------------------------
		// 01/03/2015 ECU indicate that the time has not been set yet
		// -------------------------------------------------------------------------
		timeSet	= false;
		// -------------------------------------------------------------------------
		TimePickerDialog timePicker;
		timePicker = new TimePickerDialog (Selector.context, new TimePickerDialog.OnTimeSetListener() 
		{
			@Override
			public void onTimeSet (TimePicker timePicker, int selectedHour, int selectedMinute) 
			{
				// -----------------------------------------------------------------
				// 08/03/2015 ECU for some reason on the Moto G (not Nexus or CnM)
				//                this was being called twice so put in the check on
				//                'timeSet' to prevent this
				// -----------------------------------------------------------------
				if (!timeSet)
				{
					theWeMoTimer.setDetails (selectedHour,selectedMinute);
					// -------------------------------------------------------------
					// 01/03/2015 ECU indicate that the time has been set
					// -------------------------------------------------------------
					timeSet = true;
					// -------------------------------------------------------------
					getSwitchState (theWeMoTimer);
				}
			}
		}, hour, minute, true);
		// -------------------------------------------------------------------------
		// 01/03/2015 ECU try and intercept the cancel button
		// -------------------------------------------------------------------------
		timePicker.setOnDismissListener (new OnDismissListener() 
		{
		    public void onDismiss (DialogInterface dialog) 
		    {
		        // -----------------------------------------------------------------
		    	// 01/03/2015 ECU exit 'edit mode'
		    	// 			  ECU put in the check on 'timeSet' because seemed to be
		    	//                getting a dismiss when pressing the set button -
		    	//                do not understand why !!!
				// -----------------------------------------------------------------
		    	if (!timeSet)
		    		exitEditMode ();
				// -----------------------------------------------------------------
		    }
		});
		// -------------------------------------------------------------------------
		// 01/03/2015 ECU set the title and display the dialogue
		// -------------------------------------------------------------------------
		timePicker.setTitle (theTitle);
		timePicker.show();
	}
	// =============================================================================
	static void getDays (final WeMoTimer theWeMoTimer)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(Selector.context);
		// -------------------------------------------------------------------------
		// 01/03/2015 ECU added the friendlyName
		// -------------------------------------------------------------------------
		builder.setTitle ("Select the days for the " + friendlyName)
		// -------------------------------------------------------------------------
		// 11/12/2016 ECU changed to use daysOfTheWeek
		// -------------------------------------------------------------------------
		.setMultiChoiceItems(PublicData.daysOfTheWeek,theWeMoTimer.days,new DialogInterface.OnMultiChoiceClickListener() 
		{
			// ---------------------------------------------------------------------
			@Override
			public void onClick (DialogInterface dialog,int which,boolean isChecked) 
			{
				// -----------------------------------------------------------------
				theWeMoTimer.days [which] = isChecked;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		})
		// -------------------------------------------------------------------------
		.setPositiveButton (R.string.confirm, new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int id) 
			{
				// -----------------------------------------------------------------
				// 01/03/2015 ECU added the friendlyName
				// -----------------------------------------------------------------
				getATime ("Set the Action Time for the " + friendlyName,theWeMoTimer);
				// -----------------------------------------------------------------
			}
		})
		// -------------------------------------------------------------------------
		.setNegativeButton (R.string.cancel, new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				// -----------------------------------------------------------------
				// 25/02/2015 ECU have the days now get the time
				// -----------------------------------------------------------------
				// 27/02/2015 ECU and reset the index to default back to 'add' mode
				// 01/03/2015 ECU changed to use the method
				// --------------------------------------------------------------
				exitEditMode ();
				// -----------------------------------------------------------------
			}
		});
		// ------------------------------------------------------------------------- 
		// 25/02/2015 ECU create the dialogue and then display it
		// -------------------------------------------------------------------------
		builder.create().show();
	}	
	// =============================================================================
	static void getSwitchState (final WeMoTimer theWeMoTimer)
	{
		// -------------------------------------------------------------------------
		// 01/03/2015 ECU create and build the dialogue 
		// -------------------------------------------------------------------------
		AlertDialog.Builder builder = new AlertDialog.Builder (Selector.context);
		// -------------------------------------------------------------------------
		// 01/03/2015 ECU added the friendlyName
		// -------------------------------------------------------------------------
		builder.setTitle ("Select The Action To Take for the " + friendlyName);
		// -------------------------------------------------------------------------
		// 27/02/2015 ECU changed the preset option from '-1' to that stored so
		// 				  that can accommodate editing
		// -------------------------------------------------------------------------
		builder.setSingleChoiceItems(WeMoTimer.ACTIONS,theWeMoTimer.action, new DialogInterface.OnClickListener() 
		{
			// ---------------------------------------------------------------------
			public void onClick(DialogInterface dialog, int item) 
			{
				switch(item)
				{
					// -------------------------------------------------------------
					case 0:
						theWeMoTimer.action = WeMoTimer.ACTION_SWITCH_OFF;
						break;
					// -------------------------------------------------------------
					case 1:
						theWeMoTimer.action = WeMoTimer.ACTION_SWITCH_ON;
						break;
					// -------------------------------------------------------------
				}  
			}
			// ---------------------------------------------------------------------
		})
		// -------------------------------------------------------------------------
		.setPositiveButton (R.string.confirm, new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int id) 
			{
				// -----------------------------------------------------------------
				if (PublicData.storedData.wemoTimers == null)
				{
					PublicData.storedData.wemoTimers = new ArrayList<WeMoTimer>();
				} 
				// -----------------------------------------------------------------
				// 27/02/2015 ECU have all of the information so add the new
				//                record
				//            ECU check if this is as a result of add or edit
				// -----------------------------------------------------------------
				if (editRecordIndex == StaticData.NO_RESULT)
				{
					// -------------------------------------------------------------
					// 27/02/2015 ECU in 'add' mode so perform that task
					// -------------------------------------------------------------
					PublicData.storedData.wemoTimers.add (theWeMoTimer);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 27/02/2015 ECU this is an edit request so modify the
					//                specified record
					// -------------------------------------------------------------
					PublicData.storedData.wemoTimers.set (editRecordIndex,theWeMoTimer);
					// -------------------------------------------------------------
					// 27/02/2015 ECU and reset the index to default back to 'add' mode
					// 01/03/2015 ECU changed to use the method
					// --------------------------------------------------------------
					exitEditMode ();
					// -------------------------------------------------------------
					
				}
				// -----------------------------------------------------------------
				// 27/02/2015 ECU try and rebuild the display
				// -----------------------------------------------------------------
				Selector.Rebuild();
				// -----------------------------------------------------------------
			}
		})
		// -------------------------------------------------------------------------
		.setNegativeButton (R.string.cancel, new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				// -----------------------------------------------------------------
				// 25/02/2015 ECU cancel
				// -----------------------------------------------------------------
				// 27/02/2015 ECU and reset the index to default back to 'add' mode
				// 01/03/2015 ECU changed to use the method
				// --------------------------------------------------------------
				exitEditMode ();
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		builder.create().show();
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
		// 01/03/2015 ECU add the 'number of timers' in the ListItem
		// -------------------------------------------------------------------------
		if (wemoDevices.size() > 0)
		{
			for (int theIndex = 0; theIndex < wemoDevices.size(); theIndex++)
			{
				listItems.add (new ListItem (wemoDevices.get (theIndex).getLogo(),
											 wemoDevices.get (theIndex).getFriendlyName(),
											 wemoDevices.get (theIndex).getUDN(),
											 "Number of timers = " + WeMoTimer.numberOfTimers(wemoDevices.get (theIndex).getFriendlyName()),
											 theIndex));
			}
		}
		// -------------------------------------------------------------------------
		return listItems;
	}
	// -----------------------------------------------------------------------------
	public static ArrayList<ListItem> BuildListForDevice ()
	{
		// -------------------------------------------------------------------------
		// 27/02/2015 ECU created to build a list of the timer information for a 
		//                particular device whose friendly name is supplied
		// -------------------------------------------------------------------------
		ArrayList<ListItem> listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 27/11/2014 ECU add in the check on size
		// 08/03/2015 ECU add in the check on 'null' as a 'belt and braces' measure.
		// -------------------------------------------------------------------------
		if ((PublicData.storedData.wemoTimers != null) && (PublicData.storedData.wemoTimers.size() > 0))
		{
			// ---------------------------------------------------------------------
			// 27/02/2015 ECU because the list array can be sorted then the original
			//                index needed to be stored in the individual item
			//                hence the need to use theDeviceIndex
			// ---------------------------------------------------------------------
			int theDeviceIndex = 0;
			// ---------------------------------------------------------------------
			for (int theIndex = 0; theIndex < PublicData.storedData.wemoTimers.size(); theIndex++)
			{
				WeMoTimer wemoTimer = PublicData.storedData.wemoTimers.get (theIndex);
				// -----------------------------------------------------------------
				// 27/02/2015 ECU check if timer is for this device
				// -----------------------------------------------------------------
				if (wemoTimer.getFriendlyName().equals(friendlyName))
				{
					listItems.add (new ListItem (wemoTimer.getFriendlyName() + "    " + wemoTimer.PrintAction(),
									wemoTimer.PrintTime(),
									wemoTimer.PrintDays(),
									theDeviceIndex++));
				}
				// ----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		return listItems;
	}
	// =============================================================================
	public static void AddATimer (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 27/02/2015 ECU this method is called to set up a timer for a WeMo device
		// 01/03/2015 ECU store the friendly name in friendlyName and use
		// -------------------------------------------------------------------------
		friendlyName = wemoDevices.get(theIndex).getFriendlyName();
		AddATimer (friendlyName);			
		// -------------------------------------------------------------------------
	}
	// ----------------------------------------------------------------------------
	public static void AddATimer (String theFriendlyName)
	{
		// -------------------------------------------------------------------------
		// 27/02/2015 ECU this method is called to set up a timer for a WeMo device
		// -------------------------------------------------------------------------
		WeMoTimer wemoTimer = new WeMoTimer (theFriendlyName);			
		getDays (wemoTimer);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void DeleteTimer (int theIndex)
	{
		int theTimerIndex = LocateTimer (theIndex);
		// -------------------------------------------------------------------------
		// 25/02/2015 ECU the method will return the index to the found record
		//                or StaticData.NO_RESULT if a match cannot be found
		// -------------------------------------------------------------------------
		if (theTimerIndex != StaticData.NO_RESULT)
		{
			// ---------------------------------------------------------------------
			// 27/02/2015 ECU remove the record
			// ---------------------------------------------------------------------
			PublicData.storedData.wemoTimers.remove (theTimerIndex);
			// ---------------------------------------------------------------------
			// 27/02/2015 ECU need to rebuild the view
			// ---------------------------------------------------------------------
			Selector.Rebuild ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void EditTimer (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 27/02/2015 ECU created for editing a device's timer
		// -------------------------------------------------------------------------
		editRecordIndex = LocateTimer (theIndex);
		// -------------------------------------------------------------------------
		// 27/02/2015 ECU the above method will return the index to the matching record
		//                or StaticData.NO_RESULT if no match is found
		// -------------------------------------------------------------------------
		if (editRecordIndex != StaticData.NO_RESULT)
		{
			// ---------------------------------------------------------------------
			// 27/02/2015 ECU start the editing process
			// ---------------------------------------------------------------------
			getDays (PublicData.storedData.wemoTimers.get (editRecordIndex));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static int LocateTimer (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 27/02/2015 ECU created for handling a device's timer
		// -------------------------------------------------------------------------
		if (PublicData.storedData.wemoTimers.size() > 0)
		{
			int indexCounter = 0;
			for (int theTimer = 0; theTimer < PublicData.storedData.wemoTimers.size(); theTimer++)
			{
				if (PublicData.storedData.wemoTimers.get(theTimer).getFriendlyName().equals(friendlyName))
				{
					if (indexCounter++ == theIndex)
					{
						// ---------------------------------------------------------
						// 27/02/2015 ECU have found the entry so return
						// ---------------------------------------------------------
						return theTimer;
						// ---------------------------------------------------------
					}
				}
			}
		}
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void Timers (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 27/02/2015 ECU created for handling a device's timer
		// -------------------------------------------------------------------------
		friendlyName = wemoDevices.get (theIndex).getFriendlyName ();
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (Selector.context,Selector.class);
		localIntent.putExtra (StaticData.PARAMETER_OBJECT_TYPE,StaticData.OBJECT_WEMO_TIMERS);
		localIntent.putExtra (StaticData.PARAMETER_INITIAL_POSITION,theIndex);
		localIntent.putExtra (StaticData.PARAMETER_BACK_KEY,true);
		// -------------------------------------------------------------------------
		// 28/02/2015 ECU added the method to be actioned when the BACK key is
		//                pressed
		// -------------------------------------------------------------------------
		localIntent.putExtra (StaticData.PARAMETER_BACK_METHOD,
				new MethodDefinition<WeMoTimerActivity> (WeMoTimerActivity.class,"TimersForAllDevices"));
		Selector.context.startActivity (localIntent);
		// -------------------------------------------------------------------------
	}
    // =============================================================================
	public static void TimersForAllDevices (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 28/02/2015 ECU called to show timers for all defined WeMo devices
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (Selector.context,Selector.class);
		localIntent.putExtra (StaticData.PARAMETER_OBJECT_TYPE,StaticData.OBJECT_WEMO_TIMER);
		localIntent.putExtra (StaticData.PARAMETER_BACK_KEY,true);
		localIntent.putExtra (StaticData.PARAMETER_METHOD,new MethodDefinition<WeMoTimerActivity> (WeMoTimerActivity.class,"Timers"));
		Selector.context.startActivity (localIntent);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

package com.usher.diboson;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CarerActivity extends DibosonActivity
{
	// =============================================================================
	// =============================================================================
	// 09/01/2014 ECU created
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	//==============================================================================
	//private static final String TAG	= "CarerActivity";
	// =============================================================================
	// 10/01/2014 ECU define the period after the last time a bluetooth device was 
	// 				  discovered when it is deemed that a visit is at an end. If during
	//				  this period the device is rediscovered then it is deemed that
	//                the last visit has been extended.
	// 28/08/2015 ECU changed to take out the 'carer spinner'
	// 01/09/2015 ECU changed to use StaticData
	// 22/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 30/03/2016 ECU use hashCodes to determine whether the data has changed and should
	//                be written to disk. Am aware that this is not perfect but good 
	//                first attempt.
	// 20/03/2017 ECU changed "" to BLANK.....
	// 24/03/2017 ECU in SelectAction indicate the state of the visit
	// 22/01/2020 ECU For the carer visit start/stop display provide the facility to
	//                send a text message to a specified carer
	// 24/01/2020 ECU when setting the start/stop of a visit manually the display of
	//                carers provides the ability to phone/SMS a particular carer.
	//                Utilities.phoneCallCapability is used to check if the device
	//                is capable of performing these tasks - if not and a 'phone
	//                server' is defined then the method will check that this device
	//                can communicate to the server and that the server is not in
	//                'airplane mode'. This introduces a delay which may or may not
	//                be acceptable - the socket time out is set as low as sensible.
	// 25/01/2020 ECU added phoneCallCapability to indicate if this device can make
	//				  a phone call, either locally or using the phone server
	// 23/04/2020 ECU There was a comment in WriteCarerDataToDisk which indicated
	// 				  that there was a problem with calling Utilities.SynchroniseNow
	// 				  - have investigated and all seems to be well so leave the call in
	// 02/05/2020 ECU When a scheduled visit is taking place then, when that visit is
	//                started, display the tasks that are to be performed.
	//            ECU Also when a scheduled visit is started then cancel any 'scheduled
	//                visit due to end' warning, which relates to the 'scheduled start'
	//                and set it relative to the 'actual start'
	// -----------------------------------------------------------------------------
	// =============================================================================
	// =============================================================================
	static 		Activity				activity;				// 04/10/2016 ECU added
				Spinner 				carerAgencySpinner;		// 14/01/2014 ECU added - agency
    static		int						carerIndex;				// 05/10/2016 ECU added
				TextView    			carerNameView;			// 05/02/2014 ECU added - actual carer name
				String []   			carerNames;				// 05/02/2014 ECU added
				TextView				carerBluetoothView;
				Button					carerCreateButton;
				TextView				carerPhoneView;
	static 		TextView				carerPhotoView;			// 05/02/2014 ECU added
				boolean					carerVisit		= false;// 02/01/2016 ECU added
	static 		Context					context;
				CustomListViewAdapter 	customListViewAdapter;
	static      boolean                 dataChanged		= false;	
				ArrayList<ListItem> 	listItems = new ArrayList<ListItem>();
																// 05/02/2014 ECU list of carers
				ListView    			listView;				// 05/02/2014 ECU added
	static 		boolean					phoneCallCapability = false;
																// 25/01/2020 ECU added
	static 		RefreshHandler			refreshHandler;			// 04/10/2016 ECU added
	static      boolean                 refreshKeepRunning;		// 04/10/2016 ECU added
	static      int						refreshObject	= StaticData.OBJECT_CARERS;
																// 25/01/2020 ECU added
				int						selectedItem	= StaticData.NO_RESULT;									
																// 28/08/2015 ECU added
	// =============================================================================
	public static	int		initialHashCode;					// 30/03/2016 ECU added
	// =============================================================================
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		// -------------------------------------------------------------------------		
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 22/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 16/02/2014 ECU call up routine to set common activity features
			// 28/11/2016 ECU add the true for full screen
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// ---------------------------------------------------------------------
			// 04/20/2016 ECU remember the activity for later use
			// ---------------------------------------------------------------------
			activity = this;
			// ---------------------------------------------------------------------
			// 28/08/2015 ECU remember the context for future use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 02/01/2016 ECU moved here from lower down to handle carer visit
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
			
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 27/11/2014 ECU feed through the selected item
				// -----------------------------------------------------------------
				selectedItem = extras.getInt (StaticData.PARAMETER_SELECTION);	
				// -----------------------------------------------------------------
				// 02/01/2016 ECU check if handling carer visits
				// -----------------------------------------------------------------
				carerVisit = extras.getBoolean (StaticData.PARAMETER_CARER_VISIT);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 02/01/2016 ECU decide whether handle carers or directly doing the
			//                carer visits
			// ---------------------------------------------------------------------
			if (!carerVisit)
			{
				// -----------------------------------------------------------------
				// 05/02/2014 ECU switch depending on required view
				// -----------------------------------------------------------------
				setContentView (R.layout.carer_details);
				// -----------------------------------------------------------------
				// 05/04/2014 ECU make sure that the soft keyboard does not pop up
				// -----------------------------------------------------------------	
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
				carerBluetoothView 	= (TextView) findViewById (R.id.input_carer_bluetooth);
				carerPhoneView 		= (TextView) findViewById (R.id.input_carer_phone);
				carerNameView       = (TextView) findViewById (R.id.input_carer_name);
				carerPhotoView 		= (TextView) findViewById (R.id.input_carer_photo);
		
				carerCreateButton = ((Button)findViewById(R.id.carer_new_button));
				carerCreateButton.setOnClickListener(ButtonListener);
				// -----------------------------------------------------------------
				// 09/01/2014 ECU try and picked up text changes in name field
				// -----------------------------------------------------------------
				carerAgencySpinner 	= (Spinner) findViewById (R.id.input_carer_agency);
				// -----------------------------------------------------------------
				// 14/01/2014 ECU declare and set the spinner's adapter
				// -----------------------------------------------------------------
				ArrayAdapter<String> carerAgencyAdapter = new ArrayAdapter<String>
					(this, R.layout.spinner_row, R.id.spinner_textview,CarePlanVisitActivity.GetAgencyNames());
							
				carerAgencySpinner.setAdapter(carerAgencyAdapter);
				// -----------------------------------------------------------------
				// 05/02/2014 ECU try and set clickable events for the photo field
				// -----------------------------------------------------------------
				carerPhotoView.setClickable (true);
				carerPhotoView.setOnClickListener (GetCarerPhoto);	
				// -----------------------------------------------------------------
				// 02/01/2016 ECU the extras handling used to be here but this has been moved
				//                to the top - the selectedItem will be set above
				// -----------------------------------------------------------------
				DisplayCarerDetails (selectedItem);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 02/01/2016 ECU called to manually register a carer's visit
				// -----------------------------------------------------------------
				// 09/09/2017 ECU check if there are any registered carers
				// -----------------------------------------------------------------
				if (Carer.Size () > 0)
				{
					// -------------------------------------------------------------
					HandleCarerVisit (this);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 09/09/2017 ECU tell the user that there are no carers
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString (R.string.carers_none_registered),true);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 22/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	private View.OnClickListener GetCarerPhoto = new View.OnClickListener() 
	{
		@Override
		public void onClick(View theView) 
		{
			// ---------------------------------------------------------------------
			// 10/12/2013 ECU try and select a photo
			// 01/09/2015 ECU changed to use StaticData
			// 14/12/2015 ECU changed to use dialogue rather than Utilities.PickAFile
			// 17/12/2015 ECU changed to use FileChooser.displayImage to determine
			//                if images are to be displayed
			//            ECU change to use select a file method
			// ---------------------------------------------------------------------
			Utilities.selectAFile (context,StaticData.EXTENSION_PHOTOGRAPH,
						new MethodDefinition <CarerActivity> (CarerActivity.class,"SelectedPhotograph"));
			// ---------------------------------------------------------------------
		}		
	};
	// =============================================================================
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// -------------------------------------------------------------------------
		// Inflate the menu; this adds items to the action bar if it is present.
		// -------------------------------------------------------------------------
		return true;
	}
	// =============================================================================
    public void onDestroy()
    {	
    	// -------------------------------------------------------------------------
		// 21/09/2013 ECU added
    	// -------------------------------------------------------------------------
		super.onDestroy ();
    }
    // =============================================================================
    private View.OnClickListener ButtonListener = new View.OnClickListener() 
	{	
		@Override
		public void onClick(View view) 
		{	
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU now process depending on which button pressed
			// ---------------------------------------------------------------------
			switch (view.getId()) 
			{
			// ---------------------------------------------------------------------
				case R.id.carer_new_button:
				{
					// -------------------------------------------------------------
					// 09/01/2014 ECU accept button pressed so create
					//                carer from entered details
					// -------------------------------------------------------------	
			    	CreateACarerEntry ();
			    	// -------------------------------------------------------------		
					break;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
		}
	};
	// =============================================================================
	public static void AddCarer (int thePosition)
	{
		// -------------------------------------------------------------------------
		// 28/08/2015 ECU start up the activity which will add the carer
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (CarerSystemActivity.context,CarerActivity.class);
		localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		CarerSystemActivity.context.startActivity (localIntent);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void BackKeyMethod (int theIndex)
	{
		// -------------------------------------------------------------------------
    	// 04/10/2016 ECU tell the refresh handler to stop
    	// -------------------------------------------------------------------------
    	refreshHandler.sendEmptyMessage (StaticData.MESSAGE_FINISH);
		// -------------------------------------------------------------------------
		// 04/10/2016 ECU created to be called when the back key pressed
		// -------------------------------------------------------------------------
		activity.finish ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static ArrayList<ListItem> BuildTheCarersList (int theObjectType)
	{
		// -------------------------------------------------------------------------
		// 25/01/2020 ECU added the object type argument so that the visibility the
		// 				  buttons that control the ability to phone or send text
		// 				  messages can be set
		// -------------------------------------------------------------------------
		SelectorUtilities.selectorParameter.listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 27/11/2014 ECU add in the check on size
		// 27/11/2016 ECU just try and speed up a bit - use localCarer rather than
		//                indexing into the main list
		// ------------------------------------------------------------------------- 
		if (PublicData.carers.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 27/11/2016 ECU set local variables to try and speed things up
			// ---------------------------------------------------------------------
			int   localCarerSize = PublicData.carers.size();
			Carer localCarer;
			// ---------------------------------------------------------------------
			// 27/11/2016 ECU loop through all entries in the carer list
			// ---------------------------------------------------------------------
			for (int theIndex = 0; theIndex < localCarerSize; theIndex++)
			{
				// -----------------------------------------------------------------
				// 27/11/2016 ECU get a local copy of the indexed carer
				// -----------------------------------------------------------------
				localCarer = PublicData.carers.get (theIndex);
				// -----------------------------------------------------------------
				// 30/03/2014 ECU added the index as an argument
				// 31/01/2016 ECU do not add carers which have been deleted
				// 23/06/2017 ECU changed to get the carer photo using Absolute....
				// -----------------------------------------------------------------
				if (!localCarer.deleted)
				{
					ListItem localListItem = new ListItem (
															Utilities.AbsoluteFileName (localCarer.photo),
															localCarer.name,
															localCarer.phone,
															localCarer.bluetooth,
															theIndex);
					// -------------------------------------------------------------
					// 04/10/2016 ECU set the colour for this item depending on
					//                whether a visit has been started
					// 27/11/2016 ECU tidy up the selection of a colour
					// -------------------------------------------------------------
					localListItem.colour 		= StaticData.DEFAULT_BACKGROUND_COLOUR;
					// -------------------------------------------------------------
					// 07/07/2020 ECU default the legend which will indicate when the
					//                current visit started
					// -------------------------------------------------------------
					localListItem.bottomLegend 	= StaticData.BLANK_STRING;
					// -------------------------------------------------------------
					// 27/11/2016 ECU now decide if the colour is to be changed
					// -------------------------------------------------------------
					if (localCarer.visitActive)
					{
						if (localCarer.visitStarted)
						{
							localListItem.colour = StaticData.CARE_VISIT_STARTED;
							// -----------------------------------------------------
							// 07/07/2020 ECU set the legend that will be displayed
							//                at the bottom of the item
							// 10/07/2020 ECU changed from 'This visit'
							// -----------------------------------------------------
							localListItem.bottomLegend
								= "Visit started at " + PublicData.dateFormatterShort.format(localCarer.startOfVisit);
							// -----------------------------------------------------
						}
						else
						{
							localListItem.colour = StaticData.CARE_VISIT_ENDING;
						}
					}
					// -------------------------------------------------------------
					// 25/01/2020 ECU indicate the phone call capability
					// -------------------------------------------------------------
					if (theObjectType == StaticData.OBJECT_CARER_VISITS)
					{
						// ---------------------------------------------------------
						// 25/01/2020 ECU on the 'carer visit' screen it is possible
						//                to phone or text the carer but only want
						//                to show the buttons if this device is
						//                capable of making a phone call
						// ---------------------------------------------------------
						localListItem.visibilityCustom 	= phoneCallCapability;
						localListItem.visibilityHelp 	= phoneCallCapability;
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 04/10/2016 ECU Note - add the new record to the list
					// -------------------------------------------------------------
					SelectorUtilities.selectorParameter.listItems.add (localListItem);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		Collections.sort (SelectorUtilities.selectorParameter.listItems);
		// -------------------------------------------------------------------------
		return SelectorUtilities.selectorParameter.listItems;
		// -------------------------------------------------------------------------
	}	
	// =============================================================================
	public static void CarerDetection (Context theContext,List<BluetoothDevice> theDiscoveredDevices)
	{
		// -------------------------------------------------------------------------
		// 11/01/2014 ECU this method is called at the end of a bluetooth discovery
		//                     and it passes the list of names of devices that have
		//                     been found this time
		// 25/01/2015 ECU changed the argument from List<String> to BluetoothDevice
		// -------------------------------------------------------------------------
		// 11/01/2014 ECU first task is to see if the bluetooth device of a registered
		//                carer has been discovered
		// -------------------------------------------------------------------------
		// 04/10/2016 ECU because the carer data may be changed within this message
		//                then set the hash code
		// -------------------------------------------------------------------------
		dataChanged = false;
		// -------------------------------------------------------------------------
		// 01/01/2016 ECU just check to ensure that the required variables have been
		//                set up - this should only happen once
		// -------------------------------------------------------------------------
		if (PublicData.storedData.visit_end_minutes == 0)
		{
			PublicData.storedData.visit_end_milliseconds = StaticData.VISIT_END_MILLISECONDS;
			PublicData.storedData.visit_end_minutes      = StaticData.VISIT_END_MINUTES;
		}
		// -------------------------------------------------------------------------
		for (BluetoothDevice theDiscoveredDevice : theDiscoveredDevices)
		{
			// ---------------------------------------------------------------------
			// 11/01/2014 ECU look for all registered carers looking for a match
			// 02/10/2016 ECU added the deleted check
			// ---------------------------------------------------------------------
			for (int theIndex = 0; theIndex < PublicData.carers.size(); theIndex++)
			{
				if (!PublicData.carers.get (theIndex).deleted && 
					 PublicData.carers.get (theIndex).bluetooth.equalsIgnoreCase (theDiscoveredDevice.getName()))
				{
					// -------------------------------------------------------------
					// 25/01/2015 ECU tell the client that carer has been found
					// 26/01/2015 ECU took out the test on bluetoothDiscovered
					//            ECU changed to use the announce method in carer
					// -------------------------------------------------------------
					PublicData.carers.get (theIndex).AnnounceWhetherInRange (theContext,true);
					// -------------------------------------------------------------
					// 10/01/2014 ECU indicate that this carer has been discovered
					// 04/10/2016 ECU changed to use the method which indicates if
					//                the state has changed
					// -------------------------------------------------------------
					dataChanged = PublicData.carers.get (theIndex).BluetoothDiscovered (true);
					// -------------------------------------------------------------
					// 10/01/2014 ECU check if a visit has already started
					// -------------------------------------------------------------
					if (!PublicData.carers.get (theIndex).visitStarted)
					{
						// ---------------------------------------------------------
						// 10/01/2014 ECU visit has not started so start in now
						// 04/10/2016 ECU changed to use the method which returns
						//                whether the state was changed
						// ---------------------------------------------------------
						dataChanged = PublicData.carers.get (theIndex).VisitStarted (true);
						// ---------------------------------------------------------
						// 10/01/2014 ECU check if this discovery is in the
						//                period when trying to determine if the
						//                previous visit has ended. If it is then
						//                this is not a new visit - just an extension of
						//                the previous one.
						//
						//				  Do this by measuring the time from now to
						//                the what has been stored as the end of the
						//                visit
						// 01/01/2016 ECU changed to use variable in static data
						//			  ECU changed to use data in storedData
						// 27/11/2016 ECU check if the last visit was manually terminated
						//                in which case the visit cannot be extended
						// ---------------------------------------------------------
						if (PublicData.carers.get (theIndex).manualTermination ||
							((Utilities.getAdjustedTime (true) - PublicData.carers.get(theIndex).endOfVisit) >= PublicData.storedData.visit_end_milliseconds))
						{
							// -----------------------------------------------------
							// 10/01/2014 ECU this is a new visit
							// -----------------------------------------------------
							// 30/11/2016 ECU check if the visit is scheduled or not
							// 03/02/2018 ECU added the context as an argument
							// -----------------------------------------------------
							checkIfVisitScheduled (theContext,theIndex);
							// -----------------------------------------------------
							// 10/01/2014 ECU set this time as start of the visit
							// 02/01/2016 ECU changed to use the new method
							// -----------------------------------------------------
							CarerVisit (theContext,theIndex,true,false);
							// -----------------------------------------------------
						}
						else
						{
							// -----------------------------------------------------
							// 10/01/2014 ECU just extending the previous visit
							// -----------------------------------------------------
						}
					}
					// -------------------------------------------------------------
					// 25/01/2015 ECU break out of the loop and just exit
					// -------------------------------------------------------------
					break;
					// -------------------------------------------------------------
				}
			}
		}	
		// -------------------------------------------------------------------------
		// 10/01/2014 ECU now check if previously discovered carers have disappeared
		// 25/01/2015 ECU problem happens in that don't want to check a carer who
		//                has just been added above
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.carers.size(); theIndex++)
		{	
			// ---------------------------------------------------------------------
			// 11/01/2014 ECU check if previously discovered
			// 02/10/2016 ECU added the deleted check
			// ---------------------------------------------------------------------
			if (!PublicData.carers.get (theIndex).deleted && 
			     PublicData.carers.get (theIndex).bluetoothDiscovered)
			{ 
				// -----------------------------------------------------------------
				// 10/01/2013 ECU previously discovered - see if has disappeared
				// -----------------------------------------------------------------
				boolean stillInRange = false;
				
				for (BluetoothDevice theDiscoveredDevice : theDiscoveredDevices)
				{
					// -------------------------------------------------------------
					// 10/01/2014 ECU check if name in the list
					// -------------------------------------------------------------
					if (PublicData.carers.get(theIndex).bluetooth.equalsIgnoreCase (theDiscoveredDevice.getName()))
					{
						// ---------------------------------------------------------
						// 10/01/2014 ECU device still in range so no need to do anything
						// ---------------------------------------------------------
						stillInRange = true;
						// ----------------------------------------------------------
						// 25/11/2016 ECU and reset the drop out counter
						// ----------------------------------------------------------
						PublicData.carers.get(theIndex).dropOuts = StaticData.BLUETOOTH_DROPOUT_NUMBER;
						// ----------------------------------------------------------
						break;
					}		
				}
				// -----------------------------------------------------------------
				if (!stillInRange)
				{
					// -------------------------------------------------------------
					// 26/01/2015 ECU the carer is out of range so irrespective of
					//                anything else just try and announce the
					//                fact
					// -------------------------------------------------------------
					// 25/11/2016 ECU check if it is time to ignore the 
					//                'drop out'
					// -------------------------------------------------------------
					if (PublicData.carers.get (theIndex).dropOuts > 0)
					{
						// ---------------------------------------------------------
						PublicData.carers.get(theIndex).dropOuts--;
						// ---------------------------------------------------------
					}
					else
					{
						PublicData.carers.get (theIndex).AnnounceWhetherInRange (theContext,false);
						// ---------------------------------------------------------
						// 10/01/2014 ECU check if a visit is currently in progress
						// ---------------------------------------------------------
						if (PublicData.carers.get(theIndex).visitStarted)
						{
							// -----------------------------------------------------
							// 10/01/2014 ECU a previously discovered device has not been discovered this
							//                time
							// -----------------------------------------------------
							PublicData.carers.get(theIndex).endOfVisit = Utilities.getAdjustedTime (true);
							// -----------------------------------------------------
							// 10/01/2014 ECU indicate that the visit has ended
							// 04/10/2016 ECU changed to use the method which returns whether
							//                the state has changed
							// -----------------------------------------------------
							dataChanged = PublicData.carers.get(theIndex).VisitStarted (false);
							// -----------------------------------------------------
						}
						else
						{
							// -----------------------------------------------------
							// 10/01/2014 ECU send report if the period has expired
							// 01/01/2016 ECU changed to use variable in static data
							//            ECU changed to use data in storedData
							// -----------------------------------------------------
							if ((Utilities.getAdjustedTime(true) - PublicData.carers.get(theIndex).endOfVisit)
									>= PublicData.storedData.visit_end_milliseconds)
							{
								// -------------------------------------------------
								// 10/01/2014 ECU send a visit report if not already
								//                done
								// 11/01/2014 ECU change to use the Visit class
								//            ECU changed - check if visit record 
								//                added
								// 02/01/2016 ECU changed to use the newly defined
								//                carer visit method
								// -------------------------------------------------
								if (!PublicData.carers.get(theIndex).visitAdded)
								{
									CarerVisit (theContext,theIndex,false,false);
								}
							}
						}
					}
				}
			}
		}
		// -------------------------------------------------------------------------
		// 04/10/2016 ECU write the carer data to disk - the method will check if 
		//                anything has changed
		// -------------------------------------------------------------------------
		if (dataChanged)
			WriteCarerDataToDisk (theContext,true);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void CarerVisit (int theCarerIndex)
	{
		// -------------------------------------------------------------------------
		// 02/01/2016 ECU created to handle the manual input of a visit
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (Selector.context,
								 "Carer Visit Registration",
								 "Press to indicate whether this is the start or end of the visit",
								 (Object) theCarerIndex, 
								 PublicData.carers.get(theCarerIndex).visitStarted,
								 "End of Visit",  
								 Utilities.createAMethod (CarerActivity.class,"VisitEndMethod",(Object) null),
								 !PublicData.carers.get(theCarerIndex).visitStarted,
								 "Start of Visit", 
								 Utilities.createAMethod (CarerActivity.class,"VisitStartMethod",(Object) null));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void CarerVisit (Context theContext, int theCarerIndex, boolean theStartStopFlag, boolean theManualFlag)
	{
		// -------------------------------------------------------------------------
		// 02/01/2016 ECU created to register the start or stop of a visit by the
		//                specified carer
		//
		//                theCarerIndex ..... the index to the carer
		//                theStartStopFlag .. true   = the start of the visit
		//                                    false  = the end of the visit
		//                theManualFlag	..... true   = visit registered manually
		//                                    false  = visit registered using bluetooth
		// -------------------------------------------------------------------------
		if (theStartStopFlag)
		{
			// ---------------------------------------------------------------------
			// 02/01/2016 ECU the start of the visit is being registered
			// ---------------------------------------------------------------------
			// 10/01/2014 ECU set this time as start of the visit
			// ---------------------------------------------------------------------
			PublicData.carers.get(theCarerIndex).startOfVisit 	= Utilities.getAdjustedTime(true);
			// ---------------------------------------------------------------------
			// 10/01/2014 ECU indicate that report will be needed
			// 11/01/2014 ECU changed - indicate a visit record to be added
			// ---------------------------------------------------------------------
			// 11/01/2014 ECU have an incomplete visit so indicate 
			// ---------------------------------------------------------------------
			PublicData.carers.get (theCarerIndex).visitAdded = false;
			// ---------------------------------------------------------------------
			// 25/11/2016 ECU set the number of bluetooth 'dropouts' that are allowed
			//                before they are taken into account
			// ---------------------------------------------------------------------
			PublicData.carers.get (theCarerIndex).dropOuts = StaticData.BLUETOOTH_DROPOUT_NUMBER;
			// ---------------------------------------------------------------------
			// 25/11/2016 ECU indicate that the visit is active
			// ---------------------------------------------------------------------
			PublicData.carers.get (theCarerIndex).visitActive = true;
			// ---------------------------------------------------------------------
			// 04/10/2016 ECU write the carer data to disk
			// ---------------------------------------------------------------------
			WriteCarerDataToDisk (theContext,true);
			// ---------------------------------------------------------------------
			// 02/01/2016 ECU process any actions associated with
			//                the start of the visit
			// 30/03/2016 ECU replace the <CARER> with the real carer's name
			// 16/11/2019 ECU changed from 'replaceAll' to 'replace' because
			//                the former requires a REGEX so not sure why it ever
			//				  worked
			// ---------------------------------------------------------------------
			Utilities.actionHandler (theContext,
					PublicData.storedData.visit_start_actions.replace
						(StaticData.CARER_REPLACEMENT,PublicData.carers.get(theCarerIndex).name));
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/01/2016 ECU the end of the visit is being registered
			// ---------------------------------------------------------------------
			PublicData.carers.get(theCarerIndex).visitAdded = true;
			// ---------------------------------------------------------------------
			// 25/11/2016 ECU indicate that the visit is inactive
			// ---------------------------------------------------------------------
			PublicData.carers.get (theCarerIndex).visitActive = false;
			// ---------------------------------------------------------------------
			// 11/01/2014 ECU reset the discovered flag
			// ---------------------------------------------------------------------
			PublicData.carers.get(theCarerIndex).bluetoothDiscovered = false;
			// ---------------------------------------------------------------------
			// 27/11/2016 ECU remember the way the visit was terminate to check
			//                whether the extension period is allowed or not
			// ---------------------------------------------------------------------
			PublicData.carers.get(theCarerIndex).manualTermination = theManualFlag;
			// ---------------------------------------------------------------------
			// 02/01/2016 ECU check whether the manual registration in which case need
			//                to set the time of the end of this visit
			// ---------------------------------------------------------------------
			if (theManualFlag)
			{
				PublicData.carers.get (theCarerIndex).endOfVisit = Utilities.getAdjustedTime (true);
			}
			// ---------------------------------------------------------------------
			// 31/01/2016 ECU changed from the carer's name to the index
			// 15/07/2017 ECU changed to create a local record rather adding to a 
			//                list
			// 16/07/2017 ECU added the manual flag as an argument
			//            ECU added the context as an argument
			// ---------------------------------------------------------------------
			Visit localVisit = new Visit (theContext,
										  theCarerIndex,
					 					  PublicData.carers.get(theCarerIndex).startOfVisit, 
					 					  PublicData.carers.get(theCarerIndex).endOfVisit,
					 					  theManualFlag);	
			// ---------------------------------------------------------------------
			// 27/08/2015 ECU write the carer data to disk
			//            ECU added context as an argument
			// 04/10/2016 ECU added the 'true' to force a write to disk
			// ---------------------------------------------------------------------
			WriteCarerDataToDisk (theContext,true);
			// ---------------------------------------------------------------------
			// 11/01/2014 ECU send a confirmation email of last visit in list
			// 25/01/2015 ECU changed to use the timed email because
			//                of issues with trying to use the
			//                network on the UI thread
			// 01/01/2016 ECU changed to use variable in static data
			//			  ECU changed to use data in storedData
			// 02/01/2016 ECU the message about when registered depends on how the
			//                visit was recorded, i.e. manually or via bluetooth
			// 31/03/2016 ECU changed to use resources
			// 28/11/2016 ECU added the carer's name in the email subject
			// 15/07/2017 ECU changed to use 'localVisit' rather than the last
			//                entry in the 'visits list' which has been deleted
			// 16/07/2017 ECU added the context as an argument to Print
			// ---------------------------------------------------------------------
			PublicData.emailDetails.TimedEmail (theContext, 
					                            "Visit Confirmation - " +  PublicData.carers.get(theCarerIndex).name,
				                                localVisit.Print (theContext,true),
				(theManualFlag ? theContext.getString (R.string.visit_manual)
				               : String.format (theContext.getString (R.string.visit_confirmation),PublicData.storedData.visit_end_minutes)));
			// ---------------------------------------------------------------------
			// 02/01/2016 ECU process any actions associated with
			//                the end of the visit
			// 30/03/2016 ECU replace the <CARER> with the actual name
			// 16/11/2019 ECU changed from 'replaceAll' to 'replace' because
			//                the former requires a REGEX so not sure why it ever
			//				  worked
			// ---------------------------------------------------------------------
			Utilities.actionHandler (theContext,PublicData.storedData.visit_end_actions.replace(StaticData.CARER_REPLACEMENT,PublicData.carers.get(theCarerIndex).name));
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static void CancelTasksMethod (boolean [] theOptions)
	{
	}
	// =============================================================================
	public static void checkIfVisitScheduled (Context theContext,int theCarerIndex)
	{
		// -------------------------------------------------------------------------
		// 30/11/2016 ECU created to check if there is a scheduled visit for the
		//                specified carer.
		//            ECU NOTE - this code used to be in the VisitStartMethod
		// 03/02/2018 ECU added the context as an arguemnt
		// 30/04/2020 ECU changed because '...getPlan' now returns a list
		// -------------------------------------------------------------------------
		List<CarePlanVisit> localVisits = CarePlan.getPlan ();
		//--------------------------------------------------------------------------
   		if (localVisits.size() > 0)
   		{
   			// ---------------------------------------------------------------------
   			// 30/04/2020 ECU scan through the scheduled visits to see if one of them
   			//                relates to 'this' carer
   			// ---------------------------------------------------------------------
   			for (CarePlanVisit visit : localVisits)
			{
				// -----------------------------------------------------------------
				// 30/04/2020 ECU check this visit to see if it relates to this carer
				// -----------------------------------------------------------------
				if (visit.carerIndex == theCarerIndex)
				{
					// -------------------------------------------------------------
					// 30/04/2020 ECU this visit is for this carer
					// -------------------------------------------------------------
					// 05/10/2016 ECU confirm the visit as scheduled
					// 17/07/2019 ECU changed to use format and indicate the length of the visit
					// -------------------------------------------------------------
					MessageHandler.popToastAndSpeakwithPhoto (String.format (theContext.getString (R.string.scheduled_visit_format),
															  PublicData.carers.get(theCarerIndex).name,
							                                  visit.duration),
															  Utilities.AbsoluteFileName (PublicData.carers.get (theCarerIndex).photo));
					// -------------------------------------------------------------
					// 30/04/2020 ECU set the scheduled tasks to be performed
					// -------------------------------------------------------------
					PublicData.carers.get (theCarerIndex).Tasks (visit.tasks);
					// -------------------------------------------------------------
					// 02/05/2020 ECU now want to display the tasks to be performed
					// -------------------------------------------------------------
					PublicData.carers.get (theCarerIndex).DisplayTasksToPerform (theContext);
					// -------------------------------------------------------------
					// 02/05/2020 ECU cancel any associated alarms for this visit
					// 03/05/2020 ECU remove the 'Cancel...' because the existing
					//                alarm, if it exists, will be reset to what
					//                follows
					// -------------------------------------------------------------
					// visit.CancelAnyAlarms (theContext);
					// -------------------------------------------------------------
					// 02/05/2020 ECU set up an alarm for the end of the visit
					// -------------------------------------------------------------
					DailyScheduler.SetAnAlarm (theContext,
											   visit.alarmID,
							                   StaticData.ALARM_ID_CARE_VISIT,
							                   Utilities.getAdjustedTime(true) +
							                   		(visit.duration * StaticData.MILLISECONDS_PER_MINUTE),
											   new int [] {StaticData.CARE_VISIT_DEPARTURE,
														   visit.carerIndex,
													       StaticData.NOT_SET,
													       StaticData.CARE_VISIT_WARNING_PERIOD,
														   StaticData.NOT_SET,
													       StaticData.NOT_SET,
													       visit.carerIndex});

					// -------------------------------------------------------------
					// 30/04/2020 ECU nothing more to do
					// -------------------------------------------------------------
					return;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 30/04/2020 ECU have scanned through the scheduled visits and there
			//                isn't one for this carer so tell the users about this
			//            ECU there may be multiple scheduled visits so indicate
			//                each one
			// 01/05/2020 ECU set the tasks for this 'unscheduled' visit as all the
			//                tasks for the 'scheduled' visits.
   			// ---------------------------------------------------------------------
   			// 01/05/2020 ECU initialise the tasks to be performed
   			// ---------------------------------------------------------------------
			PublicData.carers.get (theCarerIndex).Tasks (null);
			// ---------------------------------------------------------------------
			for (CarePlanVisit visit : localVisits)
			{
				// -----------------------------------------------------------------
				// 30/04/2020 ECU show the details of each scheduled visit
				// -----------------------------------------------------------------
				MessageHandler.popToastAndSpeakwithPhoto (String.format (theContext.getString (R.string.different_carer_format),
															PublicData.carers.get (theCarerIndex).name,
   																PublicData.carers.get (visit.carerIndex).name,
																	visit.duration),
																		Utilities.AbsoluteFileName (PublicData.carers.get (theCarerIndex).photo));
				// -----------------------------------------------------------------
				// 05/10/2016 ECU set the tasks that have been set for this visit
				// 30/11/2016 ECU whether it is the right or wrong carer then the tasks
				//                associated with this visit need to be copied across
				// 30/04/2020 ECU really want to concatenate the tasks from all
				//                scheduled visits - at the moment only the last
				//                set of tasks will be set
				// 01/05/2020 ECU merge the tasks from each scheduled visit
				// ------------------------------------------------------------------
				PublicData.carers.get (theCarerIndex).TasksMerge (visit.tasks);
				// ------------------------------------------------------------------
			}
			// ----------------------------------------------------------------------
			// 02/05/2020 ECU now want to display the tasks to be performed
			// ----------------------------------------------------------------------
			PublicData.carers.get (theCarerIndex).DisplayTasksToPerform (theContext);
			// ----------------------------------------------------------------------
   		}
   		else
   		{
   			// ---------------------------------------------------------------------
   			// 02/10/2016 ECU this appears to be an unscheduled visit
   			// ---------------------------------------------------------------------
   			MessageHandler.popToastAndSpeakwithPhoto (PublicData.carers.get(theCarerIndex).name + StaticData.NEWLINE + 
   													  theContext.getString (R.string.unscheduled_visit),
   													  Utilities.AbsoluteFileName (PublicData.carers.get (theCarerIndex).photo));
   			// ---------------------------------------------------------------------
   			// 05/10/2016 ECU indicate that no tasks have been set for this visit
   			// ---------------------------------------------------------------------
   			PublicData.carers.get (theCarerIndex).Tasks (null);
   			// ---------------------------------------------------------------------
   		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	private static void CheckPhoneCapability (final Context theContext, final int theTimeOut)
	{
		// -------------------------------------------------------------------------
		// 25/01/2020 ECU created to use a thread to see if this device is
		//                capable of making a phone call, either locally or using
		//                the 'phone server'. Performed as a thread because there
		//                could be delays if trying to contact the 'phone server'
		//                and there is no route - a timeout will occur - do not
		//                want the delay to impact on the user.
		// -------------------------------------------------------------------------
		// 25/01/2020 ECU define the thread that will do the checking
		// -------------------------------------------------------------------------
		Thread phoneThread = new Thread()
		{
			@Override
			public void run()
			{
				// -----------------------------------------------------------------
				// 25/01/2020 ECU check the capability and store so that it can be
				//                used to modify the 'user display'
				// -----------------------------------------------------------------
				phoneCallCapability = Utilities.phoneCallCapability (theContext,theTimeOut);
				// -----------------------------------------------------------------
				// 25/01/2020 ECU request a refresh of the display
				// -----------------------------------------------------------------
				requestDisplayRefresh (StaticData.OBJECT_CARER_VISITS);
				// -----------------------------------------------------------------
			}
		};
		// -------------------------------------------------------------------------
		// 25/01/2020 ECU now start the thread
		// -------------------------------------------------------------------------
		phoneThread.start();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void ConfirmTasksMethod (boolean [] theOptions)
	{
		// -------------------------------------------------------------------------
		// 27/11/2016 ECU it is possible to be called with a 'null' method - see
		//                'VisitEndMethod'
		// -------------------------------------------------------------------------
		if (theOptions != null)
		{
			String localString = StaticData.BLANK_STRING;	
			// ---------------------------------------------------------------------
			// 06/12/2016 ECU changed to use PublicData.tas... which is set during
			//                app initialisation and sets the patient's preferred 
			//                name
			// ---------------------------------------------------------------------
			for (int theIndex = 0; theIndex < PublicData.tasksToDo.length; theIndex++)
			{
				if (theOptions [theIndex])
					localString += PublicData.tasksToDo [theIndex] + " ... performed\n";
			}
			// ---------------------------------------------------------------------
			// 17/07/2019 ECU only show the tasks if some have been done
			// ---------------------------------------------------------------------
			if (!localString.equalsIgnoreCase (StaticData.BLANK_STRING))
				Utilities.popToast (localString);
			// ---------------------------------------------------------------------
			// 05/10/2016 ECU store the confirmed tasks in the record
			// ---------------------------------------------------------------------
			PublicData.carers.get (carerIndex).Tasks (theOptions);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
  		// 02/01/2016 ECU indicate that the visit has ended
   		// -------------------------------------------------------------------------
   		PublicData.carers.get (carerIndex).visitStarted = false;
		// -------------------------------------------------------------------------
  		CarerVisit (Selector.context,carerIndex,false,true);
  		// -------------------------------------------------------------------------
   		// 04/10/2016 ECU get the display refreshed
   		// ------------------------------------------------------------------------- 
   		Selector.Rebuild();
   		// -------------------------------------------------------------------------
	}
    // =============================================================================
    void CreateACarerEntry ()
    {
    	// -------------------------------------------------------------------------
    	// 03/01/2016 ECU declare a flag to indicate whether the selector activity
    	//                needs to be started (true) or not (false). When carers
    	//                are being added from a list then normally needs to be false
    	//                but for the very first carer then need to set to true
    	// -------------------------------------------------------------------------
    	boolean startSelectorFlag = false;
    	// -------------------------------------------------------------------------
    	
    	if (!carerNameView.getText().toString().equalsIgnoreCase(StaticData.BLANK_STRING))
    	{
    		// ---------------------------------------------------------------------
    		// 14/01/2014 ECU added the index to the carer's agency
    		// ---------------------------------------------------------------------
    		Carer localCarer = new Carer (carerNameView.getText().toString(),
    									  carerPhoneView.getText().toString(),
    									  carerBluetoothView.getText().toString(),
    									  carerAgencySpinner.getSelectedItemPosition(),
    									  carerPhotoView.getText().toString());						
    		// ---------------------------------------------------------------------
    		// 09/01/2014 ECU check if the carer already has an entry
    		// ---------------------------------------------------------------------
    		boolean existingEntry = false;
    		
    		if (selectedItem == StaticData.NO_RESULT)
    		{
    			// -----------------------------------------------------------------
    			// 28/08/2015 ECU this is a new carer to be added
    			// 09/09/2017 ECU take into account the deleted flag and use Size
    			//                which returns the number of 'non-deleted' entries
    			// -----------------------------------------------------------------
    			if (Carer.Size() > 0)
    			{
    				for (int theIndex = 0; theIndex < PublicData.carers.size(); theIndex++)
    				{
    					// ---------------------------------------------------------
    					// 09/09/2017 ECU only check records that are not deleted
    					// ---------------------------------------------------------
    					if (!PublicData.carers.get (theIndex).deleted && PublicData.carers.get (theIndex).name.equalsIgnoreCase(localCarer.name))
    					{
    						// -----------------------------------------------------
    						// 28/08/2015 ECU update the entry that matches the name
    						// -----------------------------------------------------
    						PublicData.carers.set (theIndex, localCarer);
    						// -----------------------------------------------------
    						existingEntry = true;
    						// -----------------------------------------------------
    						break;
    					}
    		
    				}
    			}
    			else
    			{
    				// -------------------------------------------------------------
    				// 03/01/2016 ECU this is the very first carer so need to 
    				//                indicate that need to start selector
    				// -------------------------------------------------------------
    				startSelectorFlag = true;
    				// -------------------------------------------------------------
    			}
    			// -----------------------------------------------------------------
    			// 31/01/2016 ECU changed to use the new method for adding a carer
    			// -----------------------------------------------------------------
    			if (!existingEntry) 	
    				Carer.Add (localCarer);
    			// -----------------------------------------------------------------
    		}
    		else
    		{
    			// -----------------------------------------------------------------
    			// 28/08/2015 ECU have edited an existing entry
    			// -----------------------------------------------------------------
    			PublicData.carers.set (selectedItem,localCarer);
    		}
    		// ---------------------------------------------------------------------
    		// 09/01/2014 ECU write updated to data
    		// 27/08/2015 ECU added the context as an argument
    		// 04/10/2016 ECU added the 'false' argument
    		// ---------------------------------------------------------------------
    		WriteCarerDataToDisk (this,false);
    		// ---------------------------------------------------------------------
    		// 28/08/2015 ECU terminate this activity
    		// ---------------------------------------------------------------------
    		finish ();
    		// ---------------------------------------------------------------------
    		// 01/09/2015 ECU try and display the carers as a list
    		//            ECU use method with 'false' to indicate that display is
    		//                to be rebuilt without starting the activity
    		// 03/01/2016 ECU changed the argument from false to startSelectorFlag
    		//                because of an issue when the very first carer is
    		//                created
    		// ---------------------------------------------------------------------
    		HandleCarers (this,startSelectorFlag);
    		// ---------------------------------------------------------------------
    	}
    	else
    	{
    		Utilities.popToast (getString (R.string.carers_name_needed));
    	}
    }
    // =============================================================================
	void DisplayCarerDetails (int thePosition)
	{
		if (thePosition == StaticData.NO_RESULT)
		{
			// ---------------------------------------------------------------------
			carerNameView.setText (StaticData.BLANK_STRING);
			carerPhoneView.setText (StaticData.BLANK_STRING);
			carerBluetoothView.setText (StaticData.BLANK_STRING);
			carerPhotoView.setText(StaticData.BLANK_STRING);
			// ---------------------------------------------------------------------	
			// 04/02/2014 ECU display the carer's agency
			// ---------------------------------------------------------------------	
			carerAgencySpinner.setSelection(0);
			// ---------------------------------------------------------------------
			// 05/02/2014 ECU update the button legend
			// ---------------------------------------------------------------------
			carerCreateButton.setText ("Enter Details of New Carer");
		}
		else
		{
			if (PublicData.carers.size() > 0)
			{
				Carer localCarer = PublicData.carers.get(thePosition);
				
				carerNameView.setText (localCarer.name);
				carerPhoneView.setText (localCarer.phone);
				carerBluetoothView.setText (localCarer.bluetooth);
				carerPhotoView.setText (localCarer.photo);
				// -----------------------------------------------------------------	
				// 04/02/2014 ECU display the carer's agency
				// -----------------------------------------------------------------	
				carerAgencySpinner.setSelection(localCarer.agencyIndex);
				// -----------------------------------------------------------------
				// 05/02/2014 ECU update the button legend
				// -----------------------------------------------------------------
				carerCreateButton.setText ("Confirm Changes to Carer");
			}
		}
	}
	// =============================================================================
	void DisplayCarersAsList (final Context theContext)
	{
		setContentView(R.layout.activity_list);
	 
		listView = (ListView) findViewById (R.id.grid_list_view);

		// -------------------------------------------------------------------------
		// 26/01/2014 ECU play around with a custom grid
		// 20/03/2014 ECU changed to use the method to get the absolute file path of
		//                the photo
		// 30/03/2014 ECU include the index
		// -------------------------------------------------------------------------
		for (int theCarerIndex = 0; theCarerIndex < PublicData.carers.size(); theCarerIndex++)
		{
			listItems.add (new ListItem (Utilities.AbsoluteFileName(PublicData.carers.get(theCarerIndex).photo),
											PublicData.carers.get(theCarerIndex).name,
											PublicData.carers.get(theCarerIndex).phone,
											PublicData.agencies.get(PublicData.carers.get(theCarerIndex).agencyIndex).name,
											theCarerIndex));
		}
 
		customListViewAdapter = new CustomListViewAdapter (this, R.layout.list_carer_row, listItems); //R.layout.list_entry, listItems);
	
		listView.setAdapter (customListViewAdapter);
	
		listView.setOnItemClickListener(new OnItemClickListener() 
    	{
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
				int position, long id) 
			{
				Utilities.popToast (PublicData.carers.get(position).Print());
			}
    	});
		// -------------------------------------------------------------------------
		// 10/09/2013 ECU create the method for handling long click events
		// -------------------------------------------------------------------------
		listView.setOnItemLongClickListener(new OnItemLongClickListener() 
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					int position, long id) 
			{
    		                          	
				return true;
			}
		});
	}
    // =============================================================================
	public static String [] GetCarerNames ()
	{
		String [] localNames = new String [PublicData.carers.size() + 1];
		
		for (int theIndex = 1; theIndex <= PublicData.carers.size (); theIndex++)
		{
			localNames [theIndex] = "Edit details of '" + PublicData.carers.get(theIndex - 1).name + "'";
		}
		// -------------------------------------------------------------------------
		// 05/02/2014 ECU put last entry to indicate create
		// -------------------------------------------------------------------------
		localNames [0] = "Click here to select Carer to Edit";
		// -------------------------------------------------------------------------
		return localNames;
	}
	// =============================================================================
	public static void HandleCarers (Context theContext,boolean theStartActivityFlag)
	{
		// -------------------------------------------------------------------------
		// 22/11/2016 ECU have to modify because deleted carer objects remain in the
		//                chain. Use the 'Size' method which takes this into account
		// -------------------------------------------------------------------------
		if (Carer.Size () > 0)
		{
			// ---------------------------------------------------------------------
			// 25/01/2020 ECU change to pass through the object type
			// ---------------------------------------------------------------------
			BuildTheCarersList (StaticData.OBJECT_CARERS);
			SelectorUtilities.selectorParameter.rowLayout 				= R.layout.carer_row;
			SelectorUtilities.selectorParameter.backMethodDefinition 	= new MethodDefinition<CarerActivity> (CarerActivity.class,"BackKeyMethod");
			SelectorUtilities.selectorParameter.customMethodDefinition 	= new MethodDefinition<CarerActivity> (CarerActivity.class,"AddCarer");
			// ---------------------------------------------------------------------
			// 05/11/2016 ECU declare the method to handle the help key - here it
			//                is being used to call the carer
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.helpMethodDefinition 	
				= new MethodDefinition<CarerActivity> (CarerActivity.class,"PhoneCallAction");
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.customLegend 			= theContext.getString (R.string.add);
			SelectorUtilities.selectorParameter.classToRun 				= CarerActivity.class;
			SelectorUtilities.selectorParameter.swipeMethodDefinition	= new MethodDefinition<CarerActivity> (CarerActivity.class,"SwipeAction");
			SelectorUtilities.selectorParameter.type 					= StaticData.OBJECT_CARERS;
			// ----------------------------------------------------------------------
			// 28/11/2016 ECU indicate that the 'select' action must not 'finish'
			//                the 'Selector' activity
			// ----------------------------------------------------------------------
			SelectorUtilities.selectorParameter.finishOnSelect          = false;
			// ----------------------------------------------------------------------
			// 02/01/2016 ECU declare the long press on item
			// 28/11/2016 ECU removed
			//                   SelectorUtilities.selectorParameter.longSelectMethodDefinition 
			//                           = new MethodDefinition<CarerActivity> (CarerActivity.class,"CarerVisit");
			//                because visits cannot be registered when editing the
			//                carer system
			// ----------------------------------------------------------------------
			if (theStartActivityFlag)
			{
				SelectorUtilities.StartSelector (theContext,
												 new MethodDefinition<CarerActivity> (CarerActivity.class,"SelectAction"),
												 StaticData.OBJECT_CARERS);
			}	
			else	
			{
				// -----------------------------------------------------------------
				// 31/08/2015 ECU just rebuild the carer display
				// -----------------------------------------------------------------
				Selector.SetFromSelectorParameter (SelectorUtilities.selectorParameter);
				Selector.Rebuild ();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 04/10/2016 ECU create and then start the refresh handler
			//            ECU use the new definition of RefreshHandler to initiate
			//                the refresh
			// ---------------------------------------------------------------------
			refreshHandler = new RefreshHandler (StaticData.MESSAGE_REFRESH);
			// ----------------------------------------------------------------------
		}
		else
		{
			AddCarer (StaticData.NO_RESULT);
		}		
	}
	// =============================================================================
	public static void HandleCarerVisit (Context theContext)
	{
		if (PublicData.carers != null && PublicData.carers.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 23/07/2019 ECU make sure the 'selector' utility is initialised
			// ---------------------------------------------------------------------
			SelectorUtilities.Initialise ();
			// ---------------------------------------------------------------------
			// 25/01/2020 ECU changed the object type to ....CARER_VISITS
			//            ECU add the true to indicate phone call capability
			//                monitoring wanted
			// ---------------------------------------------------------------------
			BuildTheCarersList (StaticData.OBJECT_CARER_VISITS);
			SelectorUtilities.selectorParameter.rowLayout 				= R.layout.carer_visit_row;
			SelectorUtilities.selectorParameter.classToRun 				= CarerActivity.class;
			SelectorUtilities.selectorParameter.type 					= StaticData.OBJECT_CARER_VISITS;
			// ---------------------------------------------------------------------
			// 02/01/2016 ECU declare the long press on item
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.longSelectMethodDefinition 
				= new MethodDefinition<CarerActivity> (CarerActivity.class,"CarerVisit");
			// ---------------------------------------------------------------------
			// 28/11/2016 ECU indicate that the 'select' action must not 'finish'
			//                the 'Selector' activity
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.finishOnSelect          = false;
			// ---------------------------------------------------------------------
			// 23/07/2019 ECU indicate that sorting is required
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.sort	= true;
			// ---------------------------------------------------------------------
			// 24/01/2020 ECU check if this device is capable of making a phone call
			//                before providing the provision to make one and send an
			//                SMS message
			// 25/01/2020 ECU added the second argument which is the timeout when
			//                creating the socket to the 'phone server'
			// ---------------------------------------------------------------------
			// 05/11/2016 ECU declare the method to handle the help key - here it
			//                is being used to call the carer
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.helpMethodDefinition
					= new MethodDefinition<CarerActivity> (CarerActivity.class,"PhoneCallAction");
			// ---------------------------------------------------------------------
			// 22/01/2020 ECU set up the custom button
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.customLegend
					= theContext.getString (R.string.send_text_message);
			SelectorUtilities.selectorParameter.customMethodDefinition
					= new MethodDefinition<CarerActivity> (CarerActivity.class,"SMSMethod");
			// ---------------------------------------------------------------------
			// 24/01/2020 ECU Note - start up the selector activity
			// ---------------------------------------------------------------------
			SelectorUtilities.StartSelector (theContext,
											 new MethodDefinition<CarerActivity> (CarerActivity.class,"SelectAction"),
											 StaticData.OBJECT_CARERS);
			// ---------------------------------------------------------------------
			// 28/11/2016 ECU create and then start the refresh handler
			//            ECU use the new definition of RefreshHandler to initiate
			//                the refresh
			// ---------------------------------------------------------------------
			refreshHandler = new RefreshHandler (StaticData.MESSAGE_REFRESH);
			// ---------------------------------------------------------------------
			// 03/01/2016 ECU tell the user what to do to report the visit
			// 06/11/2016 ECU add the 'true' for centering
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (theContext.getString (R.string.carer_visit_long_press),true);
			// ---------------------------------------------------------------------
			// 25/01/2020 ECU the buttons that control making a phone call or sending
			//                a text message are dependent on whether the device can
			//                make a phone call either locally or using the 'phone
			//                server', if defined. The following method will sort this
			//                out - the second argument being the 'timeout' in mS if
			//                there is a need to create a socket to communicate to the
			//                'phone server'.
			// ---------------------------------------------------------------------
			CheckPhoneCapability (theContext,1000);
			// ---------------------------------------------------------------------
		}
	}
    // ============================================================================= 
    public static void PhoneCallAction (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 29/08/2015 ECU created to handle the selection of an item
    	// -------------------------------------------------------------------------
    	// 05/11/2016 ECU tell the user what is happening
    	// 06/11/2016 ECU changed to use resource
    	// -------------------------------------------------------------------------
    	Utilities.popToastAndSpeak (Selector.context.getString (R.string.phone_call_about_to_make) + 
    			                    PublicData.carers.get(thePosition).name,true);
    	// -------------------------------------------------------------------------
    	// 29/10/2015 ECU at the moment just call the carer
    	// -------------------------------------------------------------------------
		Utilities.makePhoneCall (Selector.context, PublicData.carers.get(thePosition).phone);
    	// -------------------------------------------------------------------------   
    }
    // =============================================================================
	static class RefreshHandler extends Handler
    {
		// -------------------------------------------------------------------------
		RefreshHandler (int theInitialMessage)
		{
			// ---------------------------------------------------------------------
			// 28/11/2016 ECU created this constructor to start the ball rolling
			//            ECU indicate that want the refresh to keep running and then
			//                send a message to get the refresh 
			// ---------------------------------------------------------------------
			refreshKeepRunning = true;
			// ---------------------------------------------------------------------
			sendEmptyMessage (theInitialMessage);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 04/10/2016 ECU created to handle any screen updates
		// -------------------------------------------------------------------------
        @Override
        public void handleMessage (Message theMessage) 
        {   
        	// ---------------------------------------------------------------------
        	// 04/10/2016 ECU change to switch on the type of message received
        	//                which is in '.what'
        	// ---------------------------------------------------------------------
        	switch (theMessage.what)
        	{
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_FINISH:
        			// -------------------------------------------------------------
        			// 04/10/2016 ECU stop the refresh
        			// -------------------------------------------------------------
        			refreshKeepRunning = false;
        			// -------------------------------------------------------------
        			// 04/10/2016 ECU cancel any pending messages
        			// -------------------------------------------------------------
        			removeMessages (StaticData.MESSAGE_REFRESH);
        			// -------------------------------------------------------------
        			break;
        		// -----------------------------------------------------------------
        		case StaticData.MESSAGE_REFRESH:
        			// -------------------------------------------------------------
        			// 04/10/2016 ECU called to do a refresh of the display if
        			//                requested
        			// -------------------------------------------------------------
        			if (PublicData.carerRefreshWanted)
        			{
        				// ---------------------------------------------------------
        				// 04/10/2016 ECU try and refresh the display
        				// 24/11/2016 ECU can be called from the service when the
        				//                state of the carer has changed but before
        				//                the parameters associated with the Selector
        				//                class has been initialised so added a
        				//                new version of the Rebuild method to check for
        				//                this
        				// 25/01/2020 ECU pass through the refresh object
        				// ---------------------------------------------------------
        				Selector.Rebuild (refreshObject);
        				// ---------------------------------------------------------
        				// 04/10/2016 ECU reset the flag to indicate action done
        				// ---------------------------------------------------------
        				PublicData.carerRefreshWanted = false;
        				// ---------------------------------------------------------
        			}
        			// -------------------------------------------------------------
        			// 04/10/2016 ECU keep looping
        			// -------------------------------------------------------------
        			if (refreshKeepRunning)
        				this.sendEmptyMessageDelayed (StaticData.MESSAGE_REFRESH,1000);
        			// -------------------------------------------------------------
        			break;
        			// -------------------------------------------------------------
        	}
        	// ---------------------------------------------------------------------
        }
    };
    // =============================================================================
    public static void requestDisplayRefresh (int theObjectType)
    {
    	// -------------------------------------------------------------------------
    	// 24/03/2017 ECU  called to try and get the display refreshed
    	// -------------------------------------------------------------------------
    	PublicData.carerRefreshWanted = true;
    	// -------------------------------------------------------------------------
    	// 25/01/2020 ECU remember the type of object being refreshed
    	// -------------------------------------------------------------------------
    	refreshObject = theObjectType;
    	// -------------------------------------------------------------------------
    }
    // ============================================================================= 
    public static void SelectAction (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 29/08/2015 ECU created to handle the selection of an item
    	// -------------------------------------------------------------------------
    	// 05/11/2016 ECU tell the user what is happening
    	// 24/03/2017 ECU give some information about the carer's visit
    	// -------------------------------------------------------------------------
    	Carer localCarer = PublicData.carers.get(thePosition);
    	// -------------------------------------------------------------------------
    	// 24/03/2017 ECU initialise a response string
    	// -------------------------------------------------------------------------
    	String localResult = localCarer.name;
    	// -------------------------------------------------------------------------
    	// 24/03/2017 ECU check if there is a visit in progress
    	// -------------------------------------------------------------------------
    	if (localCarer.visitActive)
    	{
    		// ---------------------------------------------------------------------
    		// 24/03/2017 ECU the visit is still active but this can mean
    		//               	1) the visit has been started
    		//                or
    		//					2) the visit has ended but are still in the 'grace'
    		//                     period in which the carer could reappear and the
    		//                     existing visit will be extended rather than start
    		//                     a new visit. This is to take account of detecting
    		//                     a carer using 'bluetooth' which can drop out
    		//                     occasionally
    		// ---------------------------------------------------------------------
    		if (localCarer.visitStarted)
    		{
    			// -----------------------------------------------------------------
    			// 24/03/2017 ECU the visit has been started
    			// -----------------------------------------------------------------
    			localResult += String.format (context.getString (R.string.carer_visit_started_format),
    					PublicData.dateFormatterShort.format (localCarer.startOfVisit));
    			// -----------------------------------------------------------------
    		}
    		else
    		{
    			// -----------------------------------------------------------------
    			// 24/03/2017 ECU visit has ended but are in the 'grace' period
    			// -----------------------------------------------------------------
    			localResult += String.format (context.getString(R.string.carer_visit_ended_format),
    					PublicData.dateFormatterShort.format (localCarer.endOfVisit),
    					PublicData.dateFormatterShort.format (localCarer.endOfVisit + PublicData.storedData.visit_end_milliseconds));
    			// -----------------------------------------------------------------
    		}
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 24/03/2017 ECU the selected carer is not visiting at the moment
    		// 23/09/2020 ECU changed to use 'Phrase'
    		// ---------------------------------------------------------------------
    		localResult += localCarer.Phrase (context,context.getString (R.string.carer_visit_none));
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    	// 24/03/2017 ECU tell the user the situation
    	// -------------------------------------------------------------------------
    	Utilities.popToastAndSpeak (localResult,true);
    	// -------------------------------------------------------------------------   
    }
    // =============================================================================
    public static void SendSMSMethod (Object [] theObjects)
	{
		// -------------------------------------------------------------------------
		// 22/01/2020 ECU called when the user has entered a message to send
		// 31/01/2020 ECU changed from (int) to (Integer) because the former was
		//                flagged as an error with Eclipse
		// -------------------------------------------------------------------------
		int    localPosition 	= (Integer) theObjects [0];
		String localMessage 	= (String) theObjects [1];
		// -------------------------------------------------------------------------
		// 22/01/2020 ECU check if anything entered
		// -------------------------------------------------------------------------
		if (!Utilities.isStringBlank (localMessage))
		{
			// ---------------------------------------------------------------------
			// 22/01/2020 ECU tell the user what is going on
			// 23/01/2020 ECU changed to use the new method
			// ---------------------------------------------------------------------
			MessageHandler.popToastAndSpeakDelayed (Selector.context.getString (R.string.text_message_about_to_send) +
											 PublicData.carers.get(localPosition).name);
			// ---------------------------------------------------------------------
			// 22/02/2020 ECU now cause the message to be sent
			// ---------------------------------------------------------------------
			Utilities.sendSMSMessage (context,
									  PublicData.carers.get (localPosition).phone,
					                  localMessage);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 22/01/2020 ECU there is nothing to send
			// 23/01/2020 ECU changed to use the new method
			// ---------------------------------------------------------------------
			MessageHandler.popToastAndSpeakDelayed (Selector.context.getString (R.string.nothing_to_send));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
    // =============================================================================
	public static void SMSMethod (int thePosition)
	{
		// -------------------------------------------------------------------------
		// 22/01/2020 ECU called up when the custom button is clicked
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (((CarerActivity) context),GetMessage.class);
		// -------------------------------------------------------------------------
		// 22/01/2020 ECU request the SMS method
		// -------------------------------------------------------------------------
		localIntent.putExtra (StaticData.PARAMETER_METHOD,new MethodDefinition<CarerActivity> (CarerActivity.class,"SendSMSMethod"));
		localIntent.putExtra (StaticData.PARAMETER_DATA,thePosition);
		localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		((CarerActivity) context).startActivityForResult (localIntent,StaticData.REQUEST_CODE_FILE);
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
    public static void SwipeAction (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 09/06/2015 ECU created to handle swipe actions on a list view item
    	// -------------------------------------------------------------------------
    	// 05/10/2016 ECU check if the carer is visiting at the moment
    	// -------------------------------------------------------------------------
    	if (PublicData.carers.get (thePosition).visitStarted)
    	{
    		// ---------------------------------------------------------------------
    		// 05/10/2016 ECU cannot delete because the carer is visiting
    		// ---------------------------------------------------------------------
    		Utilities.popToastAndSpeak (String.format (Selector.context.getString (R.string.carer_unable_to_delete_format),
                    					PublicData.carers.get (thePosition).name, 
    									Selector.context.getString (R.string.carer_visiting)),true);
    		// ---------------------------------------------------------------------
    		// 05/10/2016 ECU no more processing needed
    		// ---------------------------------------------------------------------
    		return;
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    	// 31/01/2016 ECU before trying to delete a carer check whether that carer
    	//                if registered against any visits
    	// -------------------------------------------------------------------------
    	for (int theDayIndex = 0; theDayIndex < StaticData.DAYS_PER_WEEK; theDayIndex++)
		{
    		if (PublicData.carePlan.visits [theDayIndex].size() > 0)
    		{
    			// -----------------------------------------------------------------
    			for (int theVisit = 0; theVisit < PublicData.carePlan.visits [theDayIndex].size(); theVisit++)
    			{
    				if (PublicData.carePlan.visits [theDayIndex].get(theVisit).carerIndex == thePosition)
    				{
    					// -------------------------------------------------------------
        				// 31/01/2016 ECU cannot delete this carer
    					// 05/10/2016 ECU changed to use resources
        				// -------------------------------------------------------------
    					Utilities.popToastAndSpeak (String.format (Selector.context.getString (R.string.carer_unable_to_delete_format),
            													   PublicData.carers.get (thePosition).name, 
            													   Selector.context.getString (R.string.carer_has_visits)),true);
        				// -------------------------------------------------------------
        				// 31/01/2016 ECU no more processing needed
        				// -------------------------------------------------------------
        				return;
        				// -------------------------------------------------------------
    				}
    			}
    		}
		}
    	// -------------------------------------------------------------------------
		// 10/06/2015 ECU created to initiate the dialogue
    	// 05/10/2016 ECU changed to use format resource
    	// 07/06/2019 ECU changed from 'carer_delete_confirmation_format'
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (Selector.context,"Item Deletion",
				   				 String.format (Selector.context.getString (R.string.delete_confirmation_format),PublicData.carers.get (thePosition).name),
				   				 (Object) thePosition,
				   				 Utilities.createAMethod (CarerActivity.class,"YesMethod",(Object) null),
				   				 Utilities.createAMethod (CarerActivity.class,"NoMethod",(Object) null)); 
		// -------------------------------------------------------------------------  
    }
    // =============================================================================
    public static void WriteCarerDataToDisk (Context theContext,boolean theAlwaysFlag)
    {
    	// -------------------------------------------------------------------------
    	// 09/01/2014 ECU created
    	// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
    	// 27/08/2015 ECU added theContext as an argument
    	// 04/10/2016 ECU added the always flag
    	// -------------------------------------------------------------------------
    	// 30/03/2016 ECU only write if the hashcode indicates a change
    	// 04/10/2016 ECU or the always flag is set true
    	//--------------------------------------------------------------------------
    	if ((initialHashCode != PublicData.carers.hashCode()) || theAlwaysFlag)
    	{
    		AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + 
				theContext.getString (R.string.carers_file),PublicData.carers);
    		// ---------------------------------------------------------------------
    		// 04/10/2016 ECU indicate that any carer display is updated
    		// ---------------------------------------------------------------------
    		PublicData.carerRefreshWanted = true;
    		// ---------------------------------------------------------------------
    		// 25/03/2017 ECU want to trigger an immediate synchronisation but because
    		//                the writeObject.. is async then leave a slight delay
    		//            ECU seems to be an issue here which needs investigation
    		// 23/04/2020 ECU investigated the issue mentioned above and all seems
    		//                to be working well - so leave as it is
    		// ---------------------------------------------------------------------
    		Utilities.synchroniseNow (StaticData.ONE_SECOND * 5);
    		// ---------------------------------------------------------------------
    	}
    }
    // =============================================================================
    
    
    
  	// =============================================================================
  	// 10/06/2015 ECU declare the methods used for the dialogue
  	// -----------------------------------------------------------------------------
  	public static void NoMethod (Object theSelection)
  	{
  	}
  	// =============================================================================
 	public static void SelectedPhotograph (String theFileName)
 	{
 		// -------------------------------------------------------------------------
 		// 14/12/2015 ECU created to be called when a file is selected in the dialogue
 		// -------------------------------------------------------------------------
 		carerPhotoView.setText (theFileName);  	
 		// -------------------------------------------------------------------------
 	}
	// =============================================================================
  	public static void VisitEndMethod (Object theSelection)
  	{
  		// -------------------------------------------------------------------------
  		// 02/01/2016 ECU created to register the end of a visit
  		//            ECU need to manual set the time for the end of the visit
  		// -------------------------------------------------------------------------
  		// 05/10/2016 ECU remember the index to this selected carer
  		// -------------------------------------------------------------------------
  		carerIndex = (Integer) theSelection;
   		// -------------------------------------------------------------------------
   		// 02/10/2016 ECU need to check if all of the tasks for this visit have
   		//                been completed
   		// 05/10/2016 ECU check if this was a scheduled visit then take the tasks
   		//                from the record
   		// -------------------------------------------------------------------------
   		boolean [] localTasks = PublicData.carers.get (carerIndex).tasks;
   		// -------------------------------------------------------------------------
   		// 27/11/2016 ECU if terminating a visit that was started using bluetooth
   		//                then the tasks may not be set up
   		// -------------------------------------------------------------------------
   		if (localTasks != null)
   		{
   			// ---------------------------------------------------------------------
   	   		// 25/03/2017 ECU check if there is a mismatch between the data
   	   		// ---------------------------------------------------------------------
   	   		if (PublicData.tasksToDo.length != localTasks.length)
   	   		{
   	   			// -----------------------------------------------------------------
   	   			// 25/03/2017 ECU for some reason there has been a problem when syncing
   	   			//                between devices so that the stored list of tasks is
   	   			//                different in length to that stored in the carer
   	   			// -----------------------------------------------------------------
   	   			// 25/03/2017 ECU confirm the visit using the tasks stored in the carer's
   	   			//                record
   	   			// -----------------------------------------------------------------
   	   			ConfirmTasksMethod (null);
   	   			// -----------------------------------------------------------------
   	   		}
   	   		else
   	   		{
   	   			// -----------------------------------------------------------------
   	   			// 05/10/2016 ECU changed to use resource
   	   			//            ECU set the 'null' cancel method
   	   			// 06/12/2016 ECU changed to use PublicData.tas...
   	   			// -----------------------------------------------------------------
   	   			DialogueUtilities.multipleChoice (Selector.context,
   	   											  Selector.context.getString (R.string.carer_tasks_performed),
   	   											  PublicData.tasksToDo, 
   	   											  localTasks,
   	   											  Utilities.createAMethod (CarerActivity.class,"ConfirmTasksMethod",localTasks),
   	   											  Utilities.createAMethod (CarerActivity.class,"CancelTasksMethod",localTasks));
   	   			// -----------------------------------------------------------------
   	   		}
   		}
   		else
   		{
   			// ---------------------------------------------------------------------
   			// 27/11/2016 ECU notify the end of the visit
   			// ---------------------------------------------------------------------
   			ConfirmTasksMethod (null);
   			// ---------------------------------------------------------------------
   		}
  		// -------------------------------------------------------------------------
  	}
  	// =============================================================================
   	public static void VisitStartMethod (Object theSelection)
   	{
   		// -------------------------------------------------------------------------
   		// 02/01/2016 ECU created to register the start of a visit
   		// -------------------------------------------------------------------------
   		int localCarerIndex = (Integer) theSelection;
   		// -------------------------------------------------------------------------
   		// 02/10/2016 ECU check if there is a visit scheduled for this time or, if
   		//                it is, then is this the scheduled carer
   		// 30/11/2016 ECU changed to use new method - code used to be here
   		// 03/02/2018 ECU added the context as an argument
   		// -------------------------------------------------------------------------
   		checkIfVisitScheduled (Selector.context,localCarerIndex);  		
   		// -------------------------------------------------------------------------
   		// 02/01/2016 ECU indicate that the visit has started
   		// 02/10/2016 ECU changed to use localCarerIndex
   		// -------------------------------------------------------------------------
   		PublicData.carers.get (localCarerIndex).visitStarted = true;
   		CarerVisit (Selector.context,localCarerIndex,true,true);
   		// -------------------------------------------------------------------------
   		// 04/10/2016 ECU get the display refreshed
   		// ------------------------------------------------------------------------- 
   		Selector.Rebuild ();
  		// -------------------------------------------------------------------------
   	}
  	// =============================================================================
  	public static void YesMethod (Object theSelection)
  	{
  		// -------------------------------------------------------------------------
  		// 10/06/2015 ECU the selected item can be deleted
  		// -------------------------------------------------------------------------
  		int localSelection = (Integer) theSelection;
  		// -------------------------------------------------------------------------
  		// 31/01/2016 ECU changed to use the new method for deleting a carer
  		// -------------------------------------------------------------------------
  		PublicData.carers.get (localSelection).Delete();
  		// -------------------------------------------------------------------------
  		// 04/10/2016 ECU added the 'false' argument
  		// 06/09/2017 ECU changed the final flag to 'true' because of the new
  		//                way that delete works means that the hash value does not
  		//                change
  		// 08/09/2017 ECU restore the final argument to 'false' because the Carer
  		//                class now overrides the hashCode method and it accommodates
  		//                the delete flag
  		// -------------------------------------------------------------------------
  		WriteCarerDataToDisk (Selector.context,false);
  		// -------------------------------------------------------------------------
  		// 09/09/2017 ECU check if all of the carers have been deleted
  		// -------------------------------------------------------------------------
  		if (Carer.Size () > 0)
  		{
  			// ---------------------------------------------------------------------
  			// 10/06/2015 ECU rebuild and then display the updated list view
  			// ---------------------------------------------------------------------
  			Selector.Rebuild ();
  			// ---------------------------------------------------------------------
  		}
  		else
  		{
  			// ---------------------------------------------------------------------
  			// 09/09/2017 ECU all carers have been deleted so inform the use and
  			//                terminate the activity
  			// ---------------------------------------------------------------------
  			Utilities.popToastAndSpeak (Selector.context.getString (R.string.carers_all_deleted));
  			Selector.Finish ();
  			// ---------------------------------------------------------------------
  		}
  	}
  	// =============================================================================
}

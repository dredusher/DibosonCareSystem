package com.usher.diboson;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class LiquidActivity extends DibosonActivity 
{
	// -----------------------------------------------------------------------------
	// 18/09/2016 ECU handle any actions that may have been defined for the liquid
	// 10/04/2018 ECU changed to use ListViewSelector instead of Selector and reduce
	//                the number of statics
	// -----------------------------------------------------------------------------
	Activity			activity;						// 24/09/2016 ECU added
	float				ambientLightLevelDefault;
	TextView			ambientLightValueTextView;
	Context				context;
	Liquid				liquid;
	EditText			liquidActions;
	int					liquidMatched	= StaticData.NO_RESULT;
														// 31/05/2016 ECU added
	EditText			liquidName;
	ImageButton			liquidPhotoButton;				// 19/09/2016 ECU added
	ImageView			liquidPhotoImageView;			// 19/09/2016 ECU added
	String				liquidPhotoPath = null;			// 19/09/2016 ECU added
	Button				liquidProcess;					// 18/09/2016 ECU added
	ListViewSelector 	listViewSelector;
	boolean				status 			= false;		// false = process, true = register
	boolean				terminate 		= false;
	UpdateHandler		updateHandler;				
	// -----------------------------------------------------------------------------
	
	// =============================================================================
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU created to handle the registration and processing of
		//                liquids
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null) 
		{
			// ---------------------------------------------------------------------
			// 30/05/2016 ECU check if this device has an ambient light sensor
			// ---------------------------------------------------------------------
			if (SensorService.lightSensor != null)
			{
				Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
				// -----------------------------------------------------------------
				// 24/09/2016 ECU remember the activity for future use
				// -----------------------------------------------------------------
				activity = this;
				// -----------------------------------------------------------------
				// 18/05/2016 ECU remember the context for future use
				// -----------------------------------------------------------------
				context = this;
				// -----------------------------------------------------------------
				// 24/09/2016 ECU preset any statics
				// -----------------------------------------------------------------
				liquidMatched	= StaticData.NO_RESULT;
				status 			= false;
				terminate 		= false;
				updateHandler 	= new UpdateHandler ();	
				// -----------------------------------------------------------------
				// 24/09/2016 ECU check if there are any stored liquids
				// -----------------------------------------------------------------
				if (PublicData.storedData.liquids == null || PublicData.storedData.liquids.size() == 0)
				{
					// -------------------------------------------------------------
					displayLayout ();
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 24/09/2016 ECU there are stored liquids so use the selector
					// -------------------------------------------------------------
					initialiseDisplay (this);
					// -------------------------------------------------------------
				}
			}
			else
			{
				// -----------------------------------------------------------------
				// 30/05/2016 ECU inform the user that there is no light sensor
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (getString (R.string.ambient_light_sensor_none),true);
				// -----------------------------------------------------------------
				// 30/05/2016 ECU just exit this activity
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 18/05/2016 ECU the activity has been recreated after being destroyed
			//                by Android
			// ---------------------------------------------------------------------
			finish ();
			// ---------------------------------------------------------------------
		}
	}
	//==============================================================================
	@Override
	public void onActivityResult (int theRequestCode, int theResultCode, Intent theIntent) 
	{
		// ------------------------------------------------------------------------
		// 10/04/2018 ECU called when an activity returns a result. In this case
		//                the only activity that will be returning a result is
		//                FileChooser which is activated by PickAFile which is
		//                being used to select a photo file for the liquid being
		//                added.
		// ------------------------------------------------------------------------	
		// 10/04/2018 ECU check if the correct activity is returning a result
		// ------------------------------------------------------------------------
		if (theRequestCode == StaticData.REQUEST_CODE_FILE)
		{
			// --------------------------------------------------------------------
			// 10/04/2018 ECU check if a file was selected
			// --------------------------------------------------------------------
			if (theResultCode == RESULT_OK)
			{
				// ----------------------------------------------------------------
				// 10/04/2018 ECU get the path of the selected photograph - this is
				//                stored in the returned intent
				// ----------------------------------------------------------------
				liquidPhotoPath = theIntent.getStringExtra (StaticData.PARAMETER_FILE_PATH);
				// -----------------------------------------------------------------
		 		// 10/04/2018 ECU display the selected photograph
		 		// -----------------------------------------------------------------
		 		Utilities.displayAnImage (liquidPhotoButton,liquidPhotoPath);
		 		// -------------------------------------------------------------------------
			}
			// --------------------------------------------------------------------
		}
	}
	// =============================================================================
	@Override
	public void onBackPressed () 
	{
		// -------------------------------------------------------------------------
		// 18/09/2016 ECU terminate this activity
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
		// 18/09/2016 ECU now call the super for this method
		// -------------------------------------------------------------------------
		super.onBackPressed();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    View.OnClickListener buttonListener = new View.OnClickListener() 
	{
    	// -------------------------------------------------------------------------
		@Override
		public void onClick (View theView) 
		{	
			//----------------------------------------------------------------------
			// 17/03/2015 ECU now process depending on which button pressed
			// 10/04/2018 ECU changed to be 'non static'
			//----------------------------------------------------------------------
			switch (theView.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.liquid_actions_button:
					DialogueUtilitiesNonStatic.multilineTextInput (context,
														  		   activity,
														  		   getString (R.string.panic_alarm_actions_title),
														  		   getString (R.string.action_command_summary),
														  		   5,
														  		   StaticData.BLANK_STRING,
														  		   Utilities.createAMethod (LiquidActivity.class,"LiquidActions",StaticData.BLANK_STRING),
														  		   null,
														  		   StaticData.NO_RESULT,
														  		   getString (R.string.press_to_define_command));
					break;
				// -----------------------------------------------------------------
				case R.id.liquid_clear:
					// -------------------------------------------------------------
					// 31/05/2016 ECU clear the stored liquids
					// -------------------------------------------------------------
					PublicData.storedData.liquids = new ArrayList<Liquid>();
					Utilities.popToastAndSpeak (getString (R.string.liquids_cleared), true);
					// -------------------------------------------------------------
					// 19/09/2016 ECU hide the process button
					// -------------------------------------------------------------
					liquidProcess.setVisibility (View.INVISIBLE);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.liquid_display:
					// -------------------------------------------------------------
					// 31/05/2016 ECU display the stored liquids
					// -------------------------------------------------------------
					String summary = StaticData.BLANK_STRING;
					// -------------------------------------------------------------
					// 31/05/2016 ECU check if there are any stored liquids
					// 16/06/2016 ECU added the check on null
					// -------------------------------------------------------------
					if (PublicData.storedData.liquids != null && PublicData.storedData.liquids.size() > 0)
					{
						for (int liquid = 0; liquid < PublicData.storedData.liquids.size(); liquid++)
						{
							summary += PublicData.storedData.liquids.get (liquid).Print () + "\n\n";
						}
					}
					else
					{
						summary = getString (R.string.liquid_none_stored);
					}
					// -------------------------------------------------------------
					Utilities.popToast (theView,summary);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.liquid_process:
					// -------------------------------------------------------------
					// 25/09/2016 ECU Note - try and find a liquid which matches the 
					//                stoired items
					// --------------------------------------------------------------
					status = false;
					processTheLiquids ();
					// --------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.liquid_process_button:
					// -------------------------------------------------------------
					// 19/09/2016 ECU clear any fields
					// -------------------------------------------------------------
					liquidName.setText (getString (R.string.blank_textview));
					// -------------------------------------------------------------
					// 19/09/2016 ECU hide the photograph
					// -------------------------------------------------------------
					liquidPhotoImageView.setVisibility (View.INVISIBLE);
					// -------------------------------------------------------------
					updateHandler.sendEmptyMessage(StaticData.MESSAGE_SLEEP);
					updateHandler.sendEmptyMessage (StaticData.MESSAGE_START);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.liquid_register:
					// -------------------------------------------------------------
					// 25/09/2016 ECU Note - a particular liquid is to be registered
					// -------------------------------------------------------------
					status = true;
					registerALiquid ();
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.liquid_register_button:
					// -------------------------------------------------------------
					// 25/09/2016 ECU Note - start the process to register a liquid
					// -------------------------------------------------------------
					registerTheLiquid ();
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.liquid_tolerance:
					// -------------------------------------------------------------
					// 31/05/2016 ECU set the tolerance
					// -------------------------------------------------------------
					DialogueUtilitiesNonStatic.sliderChoice (context,
															 activity,
															 getString (R.string.liquid_tolerance_title),
															 getString (R.string.liquid_tolerance_summary),
															 R.drawable.liquid,
															 null,
															 (int) PublicData.storedData.liquidTolerance,
															 0,
															 (int) (3 * StaticData.LIQUID_TOLERANCE),
															 getString (R.string.press_to_confirm),
															 Utilities.createAMethod (LiquidActivity.class,"SetToleranceMethod",0),
															 StaticData.BLANK_STRING,
															 null);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	View.OnLongClickListener buttonListenerLong = new View.OnLongClickListener () 
	{		
		@Override
		public boolean onLongClick (View theView) 
		{	
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU now process depending on which button pressed
			// ---------------------------------------------------------------------
			switch (theView.getId()) 
			{
				// ------------------------------------------------------------------
				case R.id.liquid_photo_button:
					// -------------------------------------------------------------
					// 19/09/2016 ECU added to select a photo to associate with the
					//                liquid
					// 10/04/2017 ECU change from 'Utilities.selectAFile' which 
					//                passes the selected photograph path using
					//                a specified 'static' method - am trying to
					//                get away from using 'static' methods with
					//                'reflect'. Please note that the final 'true'
					//                means that the FileChooser activity will immediately
					//                return the path when a photograph is 'clicked'
					// -------------------------------------------------------------
					Utilities.PickAFile (activity,
										 PublicData.projectFolder,
										 StaticData.EXTENSION_PHOTOGRAPH,
										 true);
					// -------------------------------------------------------------
					break;	
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			return true;
		}
	};
	// =============================================================================
	@Override
    public void onDestroy ()
    {	
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU added
		// -------------------------------------------------------------------------
		terminate = true;
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
    }
	// =============================================================================
	public void AddAction (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 24/09/2016 ECU display the layout required to register a new liquid
		// -------------------------------------------------------------------------
		displayLayout ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void displayLayout ()
	{
		// -------------------------------------------------------------
		setContentView (R.layout.activity_liquid);
		// -------------------------------------------------------------
		// 19/09/2016 ECU set up the process button
		// -------------------------------------------------------------
		liquidProcess = (Button) activity.findViewById (R.id.liquid_process);
		// -------------------------------------------------------------
		// 18/05/2016 ECU set up the listeners for the buttons
		// 31/05/2016 ECU added 'clear' and 'display'
		// 18/09/2016 ECU changed to use 'liquidProcess'
		// -------------------------------------------------------------
		((Button) activity.findViewById (R.id.liquid_clear)).setOnClickListener (buttonListener);
		((Button) activity.findViewById (R.id.liquid_display)).setOnClickListener (buttonListener);
		liquidProcess.setOnClickListener (buttonListener);
		((Button) activity.findViewById (R.id.liquid_register)).setOnClickListener (buttonListener);
		((Button) activity.findViewById (R.id.liquid_tolerance)).setOnClickListener (buttonListener);
		// -------------------------------------------------------------
		// 19/09/2016 ECU check if there are any liquids to process
		// -------------------------------------------------------------
		if ((PublicData.storedData.liquids == null) || (PublicData.storedData.liquids.size() == 0))
		{
			liquidProcess.setVisibility (View.INVISIBLE);
		}
		// -------------------------------------------------------------
	
	}
	// =============================================================================
	void initialiseDisplay (Activity theActivity)
	{
		// -------------------------------------------------------------------------
		// 09/04/2018 ECU changed to use 'ListViewSelector' object
		// 10/04/2018 ECU changed the name to make easier to understand
		//            ECU changed to have the activity as an argument
		// -------------------------------------------------------------------------
		listViewSelector = new ListViewSelector (theActivity,
				   								 R.layout.liquids_row,
				   								 Utilities.createAMethod (LiquidActivity.class, "PopulateTheList"),
				   								 false,
				   								 StaticData.NO_HANDLING_METHOD,
				   								 Utilities.createAMethod (LiquidActivity.class, "ProcessAction",0),
				   								 StaticData.NO_HANDLING_METHOD,
				   								 getString (R.string.add),
				   								 Utilities.createAMethod (LiquidActivity.class, "AddAction",0),
				   								 StaticData.NO_HANDLING_METHOD,
				   								 Utilities.createAMethod (LiquidActivity.class, "SwipeAction",0)
				   								);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    public void LiquidActions (String theActions)
    {
    	// -------------------------------------------------------------------------
    	// 27/11/2015 ECU created to store commands that are required for the panic
    	//                alarm
    	// -------------------------------------------------------------------------
    	liquidActions.setText (theActions);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
   	public void NoMethod (Object theSelection)
   	{
   	}
	// =============================================================================
	public ArrayList<ListItem> PopulateTheList ()
	{
		// -------------------------------------------------------------------------
		// 24/09/2016 ECU build up the list of liquids
		// -------------------------------------------------------------------------
		ArrayList<ListItem> listItems = new ArrayList<ListItem> ();
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.storedData.liquids.size(); theIndex++)
		{
			// ---------------------------------------------------------------------
			// 03/08/2016 ECU populate the list that will be displayed
			// 25/09/2016 ECU changed to use the resource
			//            ECU add the image path
			// 10/04/2018 ECU check if there is a path to a photgraph - if not then
			//                display the icon
			// ---------------------------------------------------------------------
			if (PublicData.storedData.liquids.get(theIndex).photographPath == null)
			{
				// -----------------------------------------------------------------
				// 10/04/2018 ECU no photograph has been specified so use the liquid
				//                icon
				// -----------------------------------------------------------------
				listItems.add (new ListItem  (R.drawable.liquid,
										      PublicData.storedData.liquids.get(theIndex).name,
										      PublicData.storedData.liquids.get(theIndex).actions,
										      String.format (getString (R.string.liquid_scaled_light_level_format),
										    		  PublicData.storedData.liquids.get(theIndex).ambientLightLevelScaled),
											  theIndex));
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 10/04/2018 ECU a photograph has been specified so pass this through
				//                to the display adapter
				// -----------------------------------------------------------------
				listItems.add (new ListItem  (PublicData.storedData.liquids.get(theIndex).photographPath,
						 					  PublicData.storedData.liquids.get(theIndex).name,
						 					  PublicData.storedData.liquids.get(theIndex).actions,
						 					  String.format (getString (R.string.liquid_scaled_light_level_format),
						 							  PublicData.storedData.liquids.get(theIndex).ambientLightLevelScaled),
						 					  theIndex));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		return listItems;
		// -------------------------------------------------------------------------
	}
   	// =============================================================================
   	public void ProcessAction (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 24/09/2016 ECU process the 'long select' action
		// -------------------------------------------------------------------------
   		// 24/09/2016 ECU display the normal processing layout
   		// 25/09/2016 ECU changed to go straight to the processing bit
   		// -------------------------------------------------------------------------
   		status = false;
		processTheLiquids ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	 boolean processLightLevel (float theLightLevel)
	{
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU process the light level
		// 31/05/2016 ECU changed to used value in 'storedData'
		// 18/09/2016 ECU changed to 'boolean' so that can indicate if a liquid
		//                was detected (return true) or not (return false)
		// -------------------------------------------------------------------------
		for (int liquidIndex = 0; liquidIndex < PublicData.storedData.liquids.size(); liquidIndex++)
		{
			liquid = PublicData.storedData.liquids.get(liquidIndex);
			if (liquid.getLightLevel(ambientLightLevelDefault) > (theLightLevel - PublicData.storedData.liquidTolerance) &&
				liquid.getLightLevel(ambientLightLevelDefault) < (theLightLevel + PublicData.storedData.liquidTolerance))
			{
				if (liquidIndex != liquidMatched)
				{
					// -------------------------------------------------------------
					// 24/09/2016 ECU changed to use the resource
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString (R.string.liquid_detected) + liquid.name);	
					// -------------------------------------------------------------
					// 31/05/2016 ECU remember this liquid so that do not repeat
					// -------------------------------------------------------------
					liquidMatched = liquidIndex;
					// -------------------------------------------------------------
					// 31/05/2016 ECU update the name field
					// -------------------------------------------------------------
					liquidName.setText (liquid.name);
					// -------------------------------------------------------------
					// 19/09/2016 ECU check if there is a photograph to display
					// -------------------------------------------------------------
					if (liquid.photographPath != null)
					{
						Utilities.displayAnImage (liquidPhotoImageView,liquid.photographPath);
						// ---------------------------------------------------------
						// 19/09/2016 ECU make the image view visible
						// ---------------------------------------------------------
						liquidPhotoImageView.setVisibility (View.VISIBLE);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 18/09/2016 ECU check if there are any actions to be taken - the
					//                method checks if they are defined or not
					// -------------------------------------------------------------
					Utilities.actionHandler (context,liquid.actions);
					// -------------------------------------------------------------
					// 18/09/2016 ECU hide the processing button
					// -------------------------------------------------------------
					liquidProcess.setVisibility (View.INVISIBLE);
					// -------------------------------------------------------------
					// 18/09/2016 ECU start up the message that will check if the
					//                process button is to re-appear
					// -------------------------------------------------------------
					updateHandler.sendEmptyMessageDelayed (StaticData.MESSAGE_ACTION_FINISHED,500);
					// -------------------------------------------------------------
					// 18/09/2016 ECU want to stop any more processing until the
					//                user intervenes
					// -------------------------------------------------------------
					return true;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 31/05/2016 ECU get here if nothing matched so reset matched flag
		// -------------------------------------------------------------------------
		liquidMatched = StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
		// 31/05/2016 ECU clear the name field
		// -------------------------------------------------------------------------
		liquidName.setText (StaticData.BLANK_STRING);
		// -------------------------------------------------------------------------
		// 18/09/2016 ECU indicate no match found
		// -------------------------------------------------------------------------
		return false;
		// --------------------------------------------------------------------------
	}
	// =============================================================================
	void processTheLiquids ()
	{
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU created to process the selection of stored liquid information
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU created to process the registration of liquid information
		// -------------------------------------------------------------------------
		activity.setContentView (R.layout.liquid_process);
		ambientLightValueTextView = (TextView) activity.findViewById (R.id.ambient_light_value_textview);
		// -------------------------------------------------------------------------
		// 18/09/2016 ECU set up the button which can 
		// -------------------------------------------------------------------------
		liquidProcess = (Button) activity.findViewById (R.id.liquid_process_button);
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU set up the listener for the registration button
		//            ECU and 'actions' button
		// -------------------------------------------------------------------------
		((Button) activity.findViewById (R.id.liquid_process_button)).setOnClickListener (buttonListener);
		// -------------------------------------------------------------------------
		liquidName 	  = (EditText) activity.findViewById (R.id.liquid_name);
		liquidName.setHint (getString (R.string.detected_liquid_display));
		// -------------------------------------------------------------------------
		// 19/09/2016 ECU set up the view for the photograph and initially make
		//                invisible
		// -------------------------------------------------------------------------
		liquidPhotoImageView = (ImageView) activity.findViewById (R.id.liquid_photograph);
		liquidPhotoImageView.setVisibility (View.INVISIBLE);
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU set up the handler which will display the ambient light
		//                level value
		// -------------------------------------------------------------------------
		updateHandler.sendEmptyMessage (StaticData.MESSAGE_AMBIENT_LIGHT);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void registerALiquid ()
	{
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU created to process the registration of liquid information
		// -------------------------------------------------------------------------
		activity.setContentView (R.layout.liquid_registration);
		ambientLightValueTextView = (TextView) activity.findViewById (R.id.ambient_light_value_textview);
		// -------------------------------------------------------------------------
		// 19/09/2016 ECU set up the button for defining an associated photo
		// -------------------------------------------------------------------------
		liquidPhotoButton = (ImageButton) activity.findViewById (R.id.liquid_photo_button);
		// -------------------------------------------------------------------------
		// 19/09/2016 ECU default to no associated photograph
		// -------------------------------------------------------------------------
		liquidPhotoPath = null;
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU set up the listener for the registration button
		//            ECU and 'actions' button
		// 19/09/2016 ECU added the photo button
		// -------------------------------------------------------------------------
		((Button) activity.findViewById (R.id.liquid_register_button)).setOnClickListener (buttonListener);
		((Button) activity.findViewById (R.id.liquid_actions_button)).setOnClickListener (buttonListener);
		liquidPhotoButton.setOnLongClickListener (buttonListenerLong);
		// -------------------------------------------------------------------------
		liquidActions = (EditText) activity.findViewById (R.id.liquid_actions);
		liquidName 	  = (EditText) activity.findViewById (R.id.liquid_name);
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU set up the handler which will display the ambient light
		//                level value
		// -------------------------------------------------------------------------
		updateHandler.sendEmptyMessage (StaticData.MESSAGE_AMBIENT_LIGHT);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void registerTheLiquid ( )
	{
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU check if the list of liquids has been initialised
		// -------------------------------------------------------------------------
		if (PublicData.storedData.liquids == null)
			PublicData.storedData.liquids = new ArrayList<Liquid>();
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU store the new liquid
		// -------------------------------------------------------------------------
		// 18/09/2016 ECU check if a name has been specified
		// -------------------------------------------------------------------------
		String localLiquidName = liquidName.getText().toString();
		// -------------------------------------------------------------------------
		// 18/09/2016 ECU check if a name has been given for this liquid
		//            ECU use 'trim' in case the user enters a field of spaces
		// -------------------------------------------------------------------------
		if (!localLiquidName.trim().equals (StaticData.BLANK_STRING))
		{
			// ---------------------------------------------------------------------
			// 18/09/2016 ECU a name has been given - check if it has already
			//                been used - in which case replace the old entry
			// ---------------------------------------------------------------------
			for (int index = 0; index < PublicData.storedData.liquids.size(); index++)
			{
				if (PublicData.storedData.liquids.get(index).name.equalsIgnoreCase (localLiquidName))
				{
					// -------------------------------------------------------------
					// 18/09/2016 ECU tell the user what is going to happen
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (String.format (getString (R.string.liquid_being_replaced_format),localLiquidName), true);
					// -------------------------------------------------------------
					// 18/09/2016 ECU the liquid already exists so replace the record
					// 19/09/2016 ECU added the path to an associated photo
					// --------------------------------------------------------------
					PublicData.storedData.liquids.set (index,new Liquid (SensorService.lightLevel,
							   						   					 ambientLightLevelDefault,
							   						   					 localLiquidName,
							   						   					 liquidActions.getText().toString(),
							   						   					 liquidPhotoPath)); 
					// -------------------------------------------------------------
					// 18/09/2016 ECU need to indicate that an existing record has
					//                been replaced
					// -------------------------------------------------------------
					localLiquidName = null;
					// -------------------------------------------------------------
					// 18/09/2016 ECU break out of the loop
					// -------------------------------------------------------------
					break;
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
			// 18/09/2016 ECU if an existing record hasn't already been replaced
			//                then add a new record
			// 19/09/2016 ECU added the path to an associated photo
			// ---------------------------------------------------------------------
			if (localLiquidName != null)
				PublicData.storedData.liquids.add (new Liquid (SensorService.lightLevel,
														   	   ambientLightLevelDefault,
														       localLiquidName,
														       liquidActions.getText().toString(),
														       liquidPhotoPath)); 
			// ---------------------------------------------------------------------
			// 18/09/2016 ECU finish this activity before restarting this app - just
			//                being lazy at the moment
			// ---------------------------------------------------------------------
			finish ();
			// ---------------------------------------------------------------------
			// 18/09/2016 ECU restart this activity
			// ---------------------------------------------------------------------
			Intent localIntent = getIntent ();
			activity.startActivity (localIntent);
			// ---------------------------------------------------------------------
		}
		else
		{
			// --------------------------------------------------------------------
			// 18/09/2016 ECU indicate that no name has been given
			// --------------------------------------------------------------------
			Utilities.popToastAndSpeak (getString (R.string.liquid_no_name), true);
			// --------------------------------------------------------------------
		}
	}
	// =============================================================================
	public void SetToleranceMethod (int theTolerance)
	{
		// -------------------------------------------------------------------------
		// 31/05/2016 ECU created to set the detection tolerance
		// -------------------------------------------------------------------------
		PublicData.storedData.liquidTolerance = (float) theTolerance;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void SwipeAction (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 24/09/2016 ECU process the swipe action
		// 10/04/2018 ECU changed to be 'non static'
		// 07/06/2019 ECU changed from 'R.string.liquid_delete_format'
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.yesNo (context,
										  activity,
										  "Item Deletion",
										  String.format (getString (R.string.delete_confirmation_format), 
										  PublicData.storedData.liquids.get (theIndex).name),
										  (Object) theIndex,
										  Utilities.createAMethod (LiquidActivity.class,"YesMethod",(Object) null),
										  Utilities.createAMethod (LiquidActivity.class,"NoMethod",(Object) null)); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	class UpdateHandler extends Handler
	{
		@Override
	    public void handleMessage (Message theMessage) 
	    {   
			switch (theMessage.what)
			{
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_ACTION_FINISHED:
					// -------------------------------------------------------------
					// 18/09/2016 ECU check if there are any actions running
					// -------------------------------------------------------------
					if (PublicData.actions.size() > 0)
					{
						this.sendEmptyMessageDelayed (StaticData.MESSAGE_ACTION_FINISHED, 500);
					}
					else
					{
						// ---------------------------------------------------------
						// 18/09/2016 ECU make the button visible again
						// ---------------------------------------------------------
						liquidProcess.setVisibility (View.VISIBLE);
						// ---------------------------------------------------------
						// 18/09/2016 ECU tell the user what is going on
						// ---------------------------------------------------------
						Utilities.popToastAndSpeak (getString (R.string.press_another_liquid), true);
						// ---------------------------------------------------------
					}
				break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_AMBIENT_LIGHT:
					// -------------------------------------------------------------
					// 18/05/2016 ECU measure the default ambient light
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString (R.string.default_light_level_measurement),true);
					// -------------------------------------------------------------
					sleep (StaticData.MESSAGE_REFRESH,5000);
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_REFRESH:
					if (SensorService.lightLevel == StaticData.NO_RESULT)
					{
						sleep (StaticData.MESSAGE_REFRESH,1000);
					}
					else
					{
						// ---------------------------------------------------------
						ambientLightLevelDefault = SensorService.lightLevel;
						// ---------------------------------------------------------
						// 18/09/2016 ECU changed to use resource
						// ---------------------------------------------------------
						Utilities.popToastAndSpeak (String.format (getString (R.string.ambient_light_level_format),
																		Math.round (SensorService.lightLevel)), true);
						// ---------------------------------------------------------
						// 18/05/2016 ECU now kick off the monitoring of the liquid
						// --------------------------------------------------------
						if (status)
						{
							// -----------------------------------------------------
							// 10/04/2018 ECU tell the user about changing the photograph
							// -----------------------------------------------------
							Utilities.popToastAndSpeak (getString (R.string.photograph_long_press), true);
							// -----------------------------------------------------
							sleep (StaticData.MESSAGE_SLEEP,2000);
							// -----------------------------------------------------
						}
						else
						{
							// -----------------------------------------------------
							Utilities.popToastAndSpeak (getString (R.string.press_when_ready), true);
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
					}
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_SLEEP:
					// -------------------------------------------------------------
					// 05/03/2015 ECU display the updated value which at the moment is just
					//                the current light level
					// 20/09/2016 ECU change to use resource format
					// -------------------------------------------------------------
					ambientLightValueTextView.setText (String.format (getString (R.string.liquid_ambient_light_format),
																			SensorService.lightLevel));
					// -------------------------------------------------------------
					// 18/05/2016 ECU keep looping
					// -------------------------------------------------------------
					sleep (StaticData.MESSAGE_SLEEP,200);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_START:
					// -------------------------------------------------------------
					// 18/09/2016 ECU if no liquid found then keep looping
					// -------------------------------------------------------------
					if (!processLightLevel (SensorService.lightLevel))
					{
						sleep (StaticData.MESSAGE_START,1000);
					}
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}
	    }
	    /* ------------------------------------------------------------------------ */
	    public void sleep (int theMessageType,long theDelayInMilliseconds)
	    {		
	    	// --------------------------------------------------------------------
	    	// 04/05/2016 ECU change to _SLEEP from 0
	    	// --------------------------------------------------------------------
	        this.removeMessages (theMessageType);
	        // --------------------------------------------------------------------
	        // 18/05/2016 ECU check if the activity is being terminated - if not then
	        //                just send a delayed message
	        // --------------------------------------------------------------------
	        if (!terminate)
	        	sendMessageDelayed (obtainMessage (theMessageType), theDelayInMilliseconds);
	        // ---------------------------------------------------------------------
	    }
	};
	// =============================================================================
   	public void YesMethod (Object theSelection)
   	{
   		// -------------------------------------------------------------------------
   		// 24/09/2016 ECU the selected item can be deleted
   		// -------------------------------------------------------------------------
   		int localSelection = (Integer) theSelection;
   		PublicData.storedData.liquids.remove (localSelection);
   		// -------------------------------------------------------------------------
   		// 03/08/2016 ECU check if everything has been deleted
   		// -------------------------------------------------------------------------
   		if (PublicData.storedData.liquids.size () > 0)
   		{
   			// ---------------------------------------------------------------------
   			listViewSelector.refresh ();
   			// ---------------------------------------------------------------------
   		}
   		else
   		{
   			// ---------------------------------------------------------------------
   			// 03/08/2016 ECU everything has been deleted
   			// 20/09/2016 ECU changed to use resource
   			// ---------------------------------------------------------------------
   			Utilities.popToastAndSpeak (activity.getString (R.string.liquid_all_deleted));
   			// ---------------------------------------------------------------------
   			// 03/08/2016 ECU terminate this activity
   			// ---------------------------------------------------------------------
   			finish ();
   			// ---------------------------------------------------------------------
   		}
   		// -------------------------------------------------------------------------
   	}
   	// =============================================================================
   	
    
	// =============================================================================
	public static boolean validation (int theArgument)
	{
		// -------------------------------------------------------------------------
		// 30/05/2016 ECU created to check if device has an ambient light monitor
		// -------------------------------------------------------------------------
		if (SensorService.lightSensor != null)
			return true;
		else
			return false;
		// -------------------------------------------------------------------------
	}
   	// =============================================================================
}

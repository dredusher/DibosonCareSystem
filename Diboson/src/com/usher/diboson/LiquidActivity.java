package com.usher.diboson;

import java.util.ArrayList;

import android.annotation.SuppressLint;
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
	// -----------------------------------------------------------------------------
	static	Activity		activity;						// 24/09/2016 ECU added
	static  float			ambientLightLevelDefault;
	static  TextView		ambientLightValueTextView;
	static	Context			context;
	static 	Liquid			liquid;
	static 	EditText		liquidActions;
	static	int				liquidMatched	= StaticData.NO_RESULT;
															// 31/05/2016 ECU added
	static	EditText		liquidName;
	static  ImageButton		liquidPhotoButton;				// 19/09/2016 ECU added
	static	ImageView		liquidPhotoImageView;			// 19/09/2016 ECU added
	static  String			liquidPhotoPath = null;			// 19/09/2016 ECU added
	static	Button			liquidProcess;					// 18/09/2016 ECU added
	static	boolean			status 			= false;		// false = process, true = register
	static 	boolean			terminate 		= false;
	static UpdateHandler	updateHandler;				
	// -----------------------------------------------------------------------------
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		// 18/05/2016 ECU created to handle the registration and processing of
		//                liquids
		// -------------------------------------------------------------------------
		super.onCreate(savedInstanceState);
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
					// -------------------------------------------------------------
					// 03/08/2016 ECU there are already a number of named actions
					// -------------------------------------------------------------
					// 05/08/2016 ECU initialise the selector data
					// -------------------------------------------------------------
					SelectorUtilities.Initialise();
					// -------------------------------------------------------------
					// 04/08/2016 ECU added the 'back' method
					// 05/08/2016 ECU added the 'long select' method
					// 25/09/2016 ECU changed to use the liquids row
					// -------------------------------------------------------------
					BuildList ();
					SelectorUtilities.selectorParameter.rowLayout 					= R.layout.liquids_row;
					SelectorUtilities.selectorParameter.classToRun 					= LiquidActivity.class;
					SelectorUtilities.selectorParameter.type 						= StaticData.OBJECT_LIQUIDS;
					SelectorUtilities.selectorParameter.sort						= false;
					SelectorUtilities.selectorParameter.backMethodDefinition 		= new MethodDefinition<LiquidActivity> (LiquidActivity.class,"BackKeyAction");
					SelectorUtilities.selectorParameter.customMethodDefinition 		= new MethodDefinition<LiquidActivity> (LiquidActivity.class,"AddAction");
					SelectorUtilities.selectorParameter.customLegend 				= getString (R.string.add);
					SelectorUtilities.selectorParameter.longSelectMethodDefinition 	= new MethodDefinition<LiquidActivity> (LiquidActivity.class,"ProcessAction");
					SelectorUtilities.selectorParameter.selectMethodDefinition 		= new MethodDefinition<LiquidActivity> (LiquidActivity.class,"SelectAction");
					SelectorUtilities.selectorParameter.swipeMethodDefinition 		= new MethodDefinition<LiquidActivity> (LiquidActivity.class,"SwipeAction");
					// -------------------------------------------------------------
					// 24/09/2016 ECU show the drawable to start processing
					// --------------------------------------------------------------
					SelectorUtilities.selectorParameter.drawableInitial 			= R.drawable.liquid_long_press;
					// -------------------------------------------------------------
					SelectorUtilities.StartSelector (this,StaticData.OBJECT_LIQUIDS);
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
    private static View.OnClickListener buttonListener = new View.OnClickListener() 
	{
    	// -------------------------------------------------------------------------
		@Override
		public void onClick (View theView) 
		{	
			//----------------------------------------------------------------------
			// 17/03/2015 ECU now process depending on which button pressed
			//----------------------------------------------------------------------
			switch (theView.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.liquid_actions_button:
					DialogueUtilities.multilineTextInput (context,
							 							  context.getString (R.string.panic_alarm_actions_title),
							 							  context.getString (R.string.action_command_summary),
							 							  5,
							 							  "",
							 							  Utilities.createAMethod (LiquidActivity.class,"LiquidActions",""),
							 							  null,
							 							  StaticData.NO_RESULT,
							 							  context.getString (R.string.press_to_define_command));
					break;
				// -----------------------------------------------------------------
				case R.id.liquid_clear:
					// -------------------------------------------------------------
					// 31/05/2016 ECU clear the stored liquids
					// -------------------------------------------------------------
					PublicData.storedData.liquids = new ArrayList<Liquid>();
					Utilities.popToastAndSpeak (context.getString (R.string.liquids_cleared), true);
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
					String summary = "";
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
						summary = context.getString (R.string.liquid_none_stored);
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
					liquidName.setText (context.getString (R.string.blank_textview));
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
					DialogueUtilities.sliderChoice (context,
							  						context.getString (R.string.liquid_tolerance_title),
							  						context.getString (R.string.liquid_tolerance_summary),
							  						R.drawable.liquid,
							  						null,
							  						(int) PublicData.storedData.liquidTolerance,
							  						0,
							  						(int) (3 * StaticData.LIQUID_TOLERANCE),
							  						context.getString (R.string.press_to_confirm),
							  						Utilities.createAMethod (LiquidActivity.class,"SetToleranceMethod",0),
							  						"",
							  						null);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	private static View.OnLongClickListener buttonListenerLong = new View.OnLongClickListener () 
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
					// -------------------------------------------------------------
					Utilities.selectAFile (context,StaticData.EXTENSION_PHOTOGRAPH,
							new MethodDefinition <LiquidActivity> (LiquidActivity.class,"SelectedPhotograph"));
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
	public static void AddAction (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 24/09/2016 ECU add a new liquid
		// -------------------------------------------------------------------------
		// 24/09/2016 ECU terminate the selector activity
		// -------------------------------------------------------------------------
		Selector.Finish();
		// -------------------------------------------------------------------------
		// 24/09/2016 ECU display the layout required to register a new liquid
		// -------------------------------------------------------------------------
		displayLayout ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void BackKeyAction (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 24/09/2016 ECU created to be called when the back key pressed
		//            ECU just 'finish' this activity
		// -------------------------------------------------------------------------
		activity.finish ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static ArrayList<ListItem> BuildList ()
	{
		// -------------------------------------------------------------------------
		// 24/09/2016 ECU build up the list of liquids
		// -------------------------------------------------------------------------
		SelectorUtilities.selectorParameter.listItems = new ArrayList<ListItem> ();
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.storedData.liquids.size(); theIndex++)
		{
			// ---------------------------------------------------------------------
			// 03/08/2016 ECU populate the list that will be displayed
			// 25/09/2016 ECU changed to use the resource
			//            ECU add the image path
			// ---------------------------------------------------------------------
			SelectorUtilities.selectorParameter.listItems.add (new ListItem (
									PublicData.storedData.liquids.get(theIndex).photographPath,
									PublicData.storedData.liquids.get(theIndex).name,
									PublicData.storedData.liquids.get(theIndex).actions,
									String.format (context.getString (R.string.liquid_scaled_light_level_format),
														PublicData.storedData.liquids.get(theIndex).ambientLightLevelScaled),
									theIndex));
			// ---------------------------------------------------------------------
		}
		return SelectorUtilities.selectorParameter.listItems;
	}
	// =============================================================================
	public static void displayLayout ()
	{
		// -------------------------------------------------------------
		activity.setContentView (R.layout.activity_liquid);
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
    public static void LiquidActions (String theActions)
    {
    	// -------------------------------------------------------------------------
    	// 27/11/2015 ECU created to store commands that are required for the panic
    	//                alarm
    	// -------------------------------------------------------------------------
    	liquidActions.setText (theActions);
    	// -------------------------------------------------------------------------
    }
 // ================================================================================
   	public static void NoMethod (Object theSelection)
   	{
   	}
   	// =============================================================================
   	public static void ProcessAction (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 24/09/2016 ECU process the 'long select' action
		// -------------------------------------------------------------------------
   		Selector.Finish();
   		// -------------------------------------------------------------------------
   		// 24/09/2016 ECU display the normal processing layout
   		// 25/09/2016 ECU changed to go straight to the processing bit
   		// -------------------------------------------------------------------------
   		status = false;
		processTheLiquids ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static boolean processLightLevel (float theLightLevel)
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
					Utilities.popToastAndSpeak (context.getString (R.string.liquid_detected) + liquid.name);	
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
		liquidName.setText ("");
		// -------------------------------------------------------------------------
		// 18/09/2016 ECU indicate no match found
		// -------------------------------------------------------------------------
		return false;
		// --------------------------------------------------------------------------
	}
	// =============================================================================
	static void processTheLiquids ()
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
		liquidName.setHint (context.getString (R.string.detected_liquid_display));
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
	static void registerALiquid ()
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
		updateHandler.sendEmptyMessage(StaticData.MESSAGE_AMBIENT_LIGHT);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void registerTheLiquid ( )
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
		if (!localLiquidName.trim().equals (""))
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
					Utilities.popToastAndSpeak (String.format (context.getString (R.string.liquid_being_replaced_format),localLiquidName), true);
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
			activity.finish ();
			// ---------------------------------------------------------------------
			// 18/09/2016 ECU restart this activity
			// ---------------------------------------------------------------------
			Intent localIntent = activity.getIntent ();
			activity.startActivity (localIntent);
			// ---------------------------------------------------------------------
		}
		else
		{
			// --------------------------------------------------------------------
			// 18/09/2016 ECU indicate that no name has been given
			// --------------------------------------------------------------------
			Utilities.popToastAndSpeak (context.getString (R.string.liquid_no_name), true);
			// --------------------------------------------------------------------
		}
	}
	// =============================================================================
 	public static void SelectedPhotograph (String theFileName)
 	{
 		// -------------------------------------------------------------------------
 		// 19/09/2016 ECU created to be called when a file is selected in the dialogue
 		// -------------------------------------------------------------------------
 		liquidPhotoPath = theFileName;  	
 		// -------------------------------------------------------------------------
 		// 19/09/2016 ECU display the selected photo
 		// -------------------------------------------------------------------------
 		Utilities.displayAnImage (liquidPhotoButton,liquidPhotoPath);
 		// -------------------------------------------------------------------------
 	}
	// =============================================================================
	public static void SetToleranceMethod (int theTolerance)
	{
		// -------------------------------------------------------------------------
		// 31/05/2016 ECU created to set the detection tolerance
		// -------------------------------------------------------------------------
		PublicData.storedData.liquidTolerance = (float) theTolerance;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SwipeAction (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 24/09/2016 ECU process the swipe action
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (Selector.context,"Item Deletion",
				   				 String.format (Selector.context.getString (R.string.liquid_delete_format), 
						   				PublicData.storedData.liquids.get (theIndex).name),
						   		 (Object) theIndex,
						   		 Utilities.createAMethod (LiquidActivity.class,"YesMethod",(Object) null),
						   		 Utilities.createAMethod (LiquidActivity.class,"NoMethod",(Object) null)); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@SuppressLint("HandlerLeak")
	static class UpdateHandler extends Handler
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
						Utilities.popToastAndSpeak (context.getString (R.string.press_another_liquid), true);
						// ---------------------------------------------------------
					}
				break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_AMBIENT_LIGHT:
					// -------------------------------------------------------------
					// 18/05/2016 ECU measure the default ambient light
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (context.getString (R.string.default_light_level_measurement),true);
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
						Utilities.popToastAndSpeak (String.format (context.getString (R.string.ambient_light_level_format),
																		Math.round (SensorService.lightLevel)), true);
						// ---------------------------------------------------------
						// 18/05/2016 ECU now kick off the monitoring of the liquid
						// --------------------------------------------------------
						if (status)
						{
							sleep (StaticData.MESSAGE_SLEEP,2000);
						}
						else
						{
							Utilities.popToastAndSpeak (context.getString (R.string.press_when_ready), true);
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
					ambientLightValueTextView.setText (String.format (context.getString (R.string.liquid_ambient_light_format),
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
   	public static void YesMethod (Object theSelection)
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
   			// 03/08/2016 ECU rebuild and then display the updated list view
   			// ---------------------------------------------------------------------
   			Selector.Rebuild();
   			// ----------------------------------------------------------------------
   		}
   		else
   		{
   			// ---------------------------------------------------------------------
   			// 03/08/2016 ECU everything has been deleted
   			// 20/09/2016 ECU changed to use resource
   			// ---------------------------------------------------------------------
   			Utilities.popToastAndSpeak (activity.getString (R.string.liquid_all_deleted));
   			// ---------------------------------------------------------------------
   			// 03/08/2016 ECU finish the selector activity
   			// ---------------------------------------------------------------------
   			Selector.Finish ();
   			// ---------------------------------------------------------------------
   			// 03/08/2016 ECU terminate this activity
   			// ---------------------------------------------------------------------
   			activity.finish ();
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

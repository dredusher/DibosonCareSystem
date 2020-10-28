package com.usher.diboson;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

// =================================================================================
public class TheftActivity extends DibosonActivity 
{
	// =============================================================================
	// 06/10/2017 ECU created to handle 'theft protection' of the device
	// =============================================================================

	// =============================================================================
	public static Context 	context;
	public static Movement 	movement = null;
	// =============================================================================
	
	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		if (savedInstanceState == null)
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_theft);
			// ---------------------------------------------------------------------
			// 06/10/2017 ECU check if the movement parameters need initialising
			// ---------------------------------------------------------------------
			if (PublicData.storedData.movementParameters == null)
			{
				PublicData.storedData.movementParameters = new MovementParameters ();
			}
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 06/10/2017 ECU check if in configuration mode
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
			if (extras != null) 
			{
				if (extras.getBoolean(StaticData.PARAMETER_CONFIGURATION,false))
				{
					// -------------------------------------------------------------
					// 06/10/2017 ECU start up the configuration dialogue
					// -------------------------------------------------------------
					DialogueUtilities.sliderChoice (this,
													getString (R.string.movement_title_initial_delay),
													getString (R.string.movement_summary_initial_delay),
													R.drawable.theft,
													null,
													PublicData.storedData.movementParameters.initialDelay / 1000,
													0,
													600,
													getString (R.string.set_time),
													Utilities.createAMethod (TheftActivity.class,"InitialDelayMethod",0),
													getString (R.string.cancel_operation),
													Utilities.createAMethod (TheftActivity.class,"CancelMethod",0));
					// -------------------------------------------------------------
				}
			}
			else
			{
				if (movement == null)
				{
					// -------------------------------------------------------------
					// 06/10/2017 ECU start up the protection system
					// -------------------------------------------------------------
					movement = new Movement ();
					// -------------------------------------------------------------
					// 06/10/2017 ECU tell the user what has happened 
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString (R.string.movement_activated),true);
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 06/10/2017 ECU the detector is already active so 'finish' it
					// -------------------------------------------------------------
					movement.Finish ();
					// -------------------------------------------------------------
					// 06/10/2017 ECU tell the user what has happened 
					// -------------------------------------------------------------
					Utilities.popToastAndSpeak (getString (R.string.movement_deactivated),true);
					// -------------------------------------------------------------
					// 06/10/2017 ECU indicate that the detector has been cleared
					// -------------------------------------------------------------
					movement = null;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 06/10/2017 ECU nothing else to do - just exit
				// -----------------------------------------------------------------
				finish ();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 06/10/2017 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}		
	}
	// =============================================================================
	public static void UpdateIcon (ImageView theImageView)
	{
		if (movement != null)
			theImageView.setImageResource (movement.active ? R.drawable.theft_protected : R.drawable.theft);
	}
	// =============================================================================
	
	// =============================================================================
	public static void ActionsMethod (String theActions)
	{
		PublicData.storedData.movementParameters.actions = theActions;
		// -------------------------------------------------------------------------
		// 06/10/2017 ECU now get the gap between triggers
		// -------------------------------------------------------------------------
		DialogueUtilities.sliderChoice (context,
										context.getString (R.string.movement_title_trigger),
										context.getString (R.string.movement_summary_trigger),
										R.drawable.theft,
										null,
										(int)(PublicData.storedData.movementParameters.trigger * 100.0f),
										0,
										60 * 100,
										context.getString (R.string.set_value),
										Utilities.createAMethod (TheftActivity.class,"TriggerMethod",0),
										context.getString (R.string.cancel_operation),
										Utilities.createAMethod (TheftActivity.class,"CancelMethod",0));
		// -------------------------------------------------------------------------

	}
	// =============================================================================
	public static void CancelMethod (int theArgument)
	{
		// -------------------------------------------------------------------------
		// 06/10/2017 ECU cancel the parameter dialogue
		// -------------------------------------------------------------------------
		((Activity) context).finish ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void DurationMethod (int theDelay)
	{
		// -------------------------------------------------------------------------
		// 30/12/2016 ECU created to accept the delay time (in seconds) and set the
		//                required action - the delay will be in milliseconds
		// -------------------------------------------------------------------------
		PublicData.storedData.movementParameters.duration = theDelay * StaticData.MILLISECONDS_PER_MINUTE;
		// -------------------------------------------------------------------------
		// 06/10/2017 ECU now get the gap between triggers
		// -------------------------------------------------------------------------
		DialogueUtilities.sliderChoice (context,
										context.getString (R.string.movement_title_gap),
										context.getString (R.string.movement_summary_gap),
										R.drawable.theft,
										null,
										PublicData.storedData.movementParameters.gap / 1000,
										0,
										60,
										context.getString (R.string.set_time),
										Utilities.createAMethod (TheftActivity.class,"GapMethod",0),
										context.getString (R.string.cancel_operation),
										Utilities.createAMethod (TheftActivity.class,"CancelMethod",0));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void GapMethod (int theDelay)
	{
		// -------------------------------------------------------------------------
		// 30/12/2016 ECU created to accept the delay time (in seconds) and set the
		//                required action - the delay will be in milliseconds
		// -------------------------------------------------------------------------
		PublicData.storedData.movementParameters.gap = theDelay * 1000;
		// -------------------------------------------------------------------------
		// 06/10/2017 ECU now get the gap between triggers
		// -------------------------------------------------------------------------
		DialogueUtilities.multilineTextInput (context,
				  							  context.getString (R.string.movement_title_actions),
				  							  context.getString (R.string.action_command_summary),
				  							  5,
				  							  PublicData.storedData.movementParameters.actions,
				  							  Utilities.createAMethod (TheftActivity.class,"ActionsMethod",StaticData.BLANK_STRING),
				  							  null,
				  							  StaticData.NO_RESULT,
				  							  context.getString (R.string.press_to_define_command));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void InitialDelayMethod (int theDelay)
	{
		// -------------------------------------------------------------------------
		// 30/12/2016 ECU created to accept the delay time (in seconds) and set the
		//                required action - the delay will be in milliseconds
		// -------------------------------------------------------------------------
		PublicData.storedData.movementParameters.initialDelay = theDelay * 1000;
		// -------------------------------------------------------------------------
		// 06/10/2017 ECU now get the duration
		// -------------------------------------------------------------------------
		DialogueUtilities.sliderChoice (context,
										context.getString (R.string.movement_title_duration),
										context.getString (R.string.movement_summary_duration),
										R.drawable.theft,
										null,
										PublicData.storedData.movementParameters.duration / StaticData.MILLISECONDS_PER_MINUTE,
										0,
										StaticData.MILLISECONDS_PER_MINUTE * 12,
										context.getString (R.string.set_time),
										Utilities.createAMethod (TheftActivity.class,"DurationMethod",0),
										context.getString (R.string.cancel_operation),
										Utilities.createAMethod (TheftActivity.class,"CancelMethod",0));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void TriggerMethod (int theDelay)
	{
		// -------------------------------------------------------------------------
		// 30/12/2016 ECU created to accept the delay time (in seconds) and set the
		//                required action - the delay will be in milliseconds
		// -------------------------------------------------------------------------
		PublicData.storedData.movementParameters.trigger = (float) theDelay / 100.0f;
		// -------------------------------------------------------------------------
		// 06/10/2017 ECU now get the duration
		// -------------------------------------------------------------------------
		((Activity) context).finish ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}
// =================================================================================

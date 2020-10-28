package com.usher.diboson;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class BarCodeEntry extends DibosonActivity 
{
	// =============================================================================== 
	// 15/09/2013 ECU created - to enter the description associated with
	//                the supplied bar code
	// 22/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 13/06/2016 ECU added the 'actions' handling
	// =============================================================================== 
	   			String		actions;						// 13/06/2016 ECU added
	   static 	EditText 	actionsEditText;				// 13/06/2016 ECU added
	   			Button		actionsButton;					// 13/06/2016 ECU added
	   			String 		barCode;
	   			TextView 	barCodeTextView;
	   			Context		context;						// 13/06/2016 ECU added
	   			Button   	createButton;					// 08/02/2014 ECU added
	   			String		description;
	   			EditText	descriptionEditText;
	   			Button   	searchButton;
	// =============================================================================== 
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 22/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_bar_code_entry);
			// ---------------------------------------------------------------------
			// 13/06/2016 ECU save the context for future use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
		
			if (extras != null) 
			{
				barCode 	= extras.getString (StaticData.PARAMETER_BARCODE); 
				// -----------------------------------------------------------------
				// 08/02/2014 ECU added the description and the default
				// -----------------------------------------------------------------
				description = extras.getString (StaticData.PARAMETER_BARCODE_DESC); 
				// -----------------------------------------------------------------
				// 13/06/2016 ECU added the actions
				// -----------------------------------------------------------------
				actions = extras.getString (StaticData.PARAMETER_BARCODE_ACTIONS);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 15/09/2013 ECU get the views
			// 13/06/2016 ECU added 'actions'
			// ---------------------------------------------------------------------
			actionsEditText 	= (EditText)findViewById (R.id.bar_code_actions);
			barCodeTextView  	= (TextView)findViewById (R.id.bar_code_entered);
			descriptionEditText = (EditText)findViewById (R.id.bar_code_description);
			// ---------------------------------------------------------------------
			// 08/02/2014 ECU changed so that can get access to view to change the legend
			// ---------------------------------------------------------------------
			createButton = 	(Button)findViewById(R.id.bar_code_button);
			createButton.setOnClickListener(barCodeButtonListener);
			// ---------------------------------------------------------------------
			// 15/09/2013 ECU if there is access to the network then give the option to search
			//                for product details
			// ---------------------------------------------------------------------
			Button searchButton = (Button)findViewById(R.id.bar_code_search);
			// ---------------------------------------------------------------------
			// 15/09/2013 ECU modify the dialogue depending on whether there is network access
			// ---------------------------------------------------------------------
			if (Utilities.checkForNetwork(this))
			{
				// -----------------------------------------------------------------
				// 15/09/2013 ECU enable the listener to enable a web search
				// -----------------------------------------------------------------
				searchButton.setOnClickListener(barCodeSearchButtonListener);
			}
			else
			{
				// -----------------------------------------------------------------
				// 15/09/2013 ECU hide the search button as there is no network access
				// -----------------------------------------------------------------
				searchButton.setVisibility (View.INVISIBLE);
			}
			// ---------------------------------------------------------------------
			// 13/06/2016 ECU handle the 'actions' button
			// ---------------------------------------------------------------------
			actionsButton = 	(Button)findViewById(R.id.bar_code_actions_button);
			actionsButton.setOnClickListener(barCodeActionsButtonListener);
			// ---------------------------------------------------------------------
			barCodeTextView.setText (barCode);
			// ---------------------------------------------------------------------
			// 08/02/2014 ECU display the existing description
			// ---------------------------------------------------------------------
			if (description != null)
			{
				// -----------------------------------------------------------------
				// 08/02/2014 ECU this must be an existing bar code so show existing details
				//                and change legend
				// -----------------------------------------------------------------
				descriptionEditText.setText (description);
				createButton.setText ("Confirm Changes");
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 13/06/2016 ECU display any actions
			// ---------------------------------------------------------------------
			if (actions != null)
			{
				// -----------------------------------------------------------------
				// 08/02/2014 ECU this must be an existing bar code so show existing details
				//                and change legend
				// -----------------------------------------------------------------
				actionsEditText.setText (actions);
				createButton.setText ("Confirm Changes");
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
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
	@Override
	public void onBackPressed ()
	{
		// -------------------------------------------------------------------------
		// 24/11/2015 ECU created to handle the BACK key
		// -------------------------------------------------------------------------
		setResult (RESULT_CANCELED);
		// -------------------------------------------------------------------------
		// 22/10/2015 ECU now call the super for this method
		// -------------------------------------------------------------------------
		super.onBackPressed();
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	private View.OnClickListener barCodeButtonListener = new View.OnClickListener() 
	{
		@Override
		public void onClick(View theView) 
		{
			// ---------------------------------------------------------------------
			// 15/09/2013 ECU pull in the entered description
			// 13/06/2016 ECU pull in the entered actions
			// ---------------------------------------------------------------------
			actions		 	= actionsEditText.getText ().toString();
			description 	= descriptionEditText.getText ().toString();
			// ---------------------------------------------------------------------
			// 13/06/2016 ECU check if the description field is empty
			// ---------------------------------------------------------------------
			if (!Utilities.isStringBlank (description))
			{
				// -----------------------------------------------------------------
				// 15/09/2013 ECU add the new barcode
				// 07/02/2014 ECU changed to reflect new use of a 'List'
				// 08/02/2014 ECU check if barcode already exists
				// 13/06/2016 ECU change the object definition to include 'actions'
				// -----------------------------------------------------------------
				boolean	barCodeExists = false;
			
				if (PublicData.barCodes.size() > 0)
				{
					for (int theBarCode = 0; theBarCode < PublicData.barCodes.size(); theBarCode++)
					{
						if (PublicData.barCodes.get(theBarCode).barCode.equalsIgnoreCase(barCode))
						{
							PublicData.barCodes.set (theBarCode,new BarCode (barCode,description,actions));
						
							barCodeExists = true;
						
							break;
						}
					}
				}
				// -----------------------------------------------------------------
				// 08/02/2014 ECU if barcode is new then add an entry
				// -----------------------------------------------------------------
				if (!barCodeExists)
					PublicData.barCodes.add (new BarCode (barCode,description,actions));
				// -----------------------------------------------------------------
				// 15/09/2013 ECU update the data on disk
				// 07/02/2014 ECU change to use existing method
				// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
				// -----------------------------------------------------------------
				AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + getBaseContext().getString (R.string.bar_code_data),PublicData.barCodes);
				// -----------------------------------------------------------------
				// 21/11/2015 ECU set the result 
				// -----------------------------------------------------------------
				setResult (RESULT_OK);
				// -----------------------------------------------------------------		
				// 15/09/2013 ECU terminate this activity
				// -----------------------------------------------------------------
				finish ();	
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 13/06/2016 ECU the description field is empty which is not
				//                allowed
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (context.getString (R.string.bar_code_description_needed),true);
				// -----------------------------------------------------------------
			}
		}
	};
	// =============================================================================
	private View.OnClickListener barCodeActionsButtonListener = new View.OnClickListener() 
	{
		@Override
		public void onClick(View theView) 
		{
			// ---------------------------------------------------------------------
			// 13/06/2016 ECU process the definition of actions
			// ---------------------------------------------------------------------
			DialogueUtilities.multilineTextInput (context,
												  context.getString (R.string.barcode_actions_title),
												  context.getString (R.string.action_command_summary),
												  3,
												  actions,
												  Utilities.createAMethod (BarCodeEntry.class,"ActionsMethod",StaticData.BLANK_STRING),
												  null,
												  StaticData.NO_RESULT,
												  context.getString (R.string.press_to_define_command));
			// ---------------------------------------------------------------------
		}
	};
	/* ============================================================================= */
	private View.OnClickListener barCodeSearchButtonListener = new View.OnClickListener() 
	{
		@Override
		public void onClick(View theView) 
		{
			// ---------------------------------------------------------------------
			// 15/09/2013 ECU search google for details of product
			// ---------------------------------------------------------------------
			Intent localIntent = new Intent (getBaseContext(),DisplayURL.class);
			// ---------------------------------------------------------------------
			// 16/06/2013 ECU pass the required URL through
			// 19/12/2016 ECU changed to use _URL
			// ---------------------------------------------------------------------
			localIntent.putExtra (StaticData.PARAMETER_URL,getBaseContext().getString (R.string.google_product_search) + "\"" + barCode + "\"");
			startActivity (localIntent);
			// -----------------------------------------------------------------------
		}
	};
	/* =============================================================================== */
	@Override
	public boolean onCreateOptionsMenu (Menu theMenu)
	{
		return true;
	}
	// =============================================================================
	public static void ActionsMethod (String theActions)
	{
		// ------------------------------------------------------------------------
		// 11/06/2016 ECU created to store the actions of the tag
		// ------------------------------------------------------------------------
		actionsEditText.setText (theActions);
		// ------------------------------------------------------------------------
	}
	// =============================================================================
}

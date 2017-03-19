package com.usher.diboson;


import java.util.ArrayList;
import java.util.Collections;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AgencyActivity extends DibosonActivity
{
	/* =============================================================================== */
	// ===============================================================================
	// 12/01/2014 ECU created
	// 21/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 30/03/2016 ECU use hashCodes to determine whether the data has changed and should
	//                be written to disk. Am aware that this is not perfect but good 
	//                first attempt.
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//================================================================================
	/* =============================================================================== */
	//final static String TAG = "AgencyActivity";
	/* =============================================================================== */	
				TextView	agencyAddressView;
				TextView	agencyContactNameView;
				Button		agencyCreateButton;
				TextView	agencyEmailAddressView;
				TextView	agencyNameView;
				TextView	agencyNotesView;
				TextView	agencyPhoneNumberView;
	static		Context		context;
				int			selectedItem			= StaticData.NO_RESULT; // 28/08/2015 ECU added
	/* =============================================================================== */
	public static	int		initialHashCode;								// 30/03/2016 ECU added
	// ===============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 21/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 16/02/2014 ECU call up routine to set common activity features
			//---------------------------------------------------------------------- 
			Utilities.SetUpActivity (this);
					
			setContentView(R.layout.activity_agency);
			// ---------------------------------------------------------------------
			// 28/08/2015 ECU save the context for later
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 05/04/2014 ECU make sure that the soft keyboard does not pop up
			// ---------------------------------------------------------------------
			getWindow().setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
			agencyAddressView 		= (TextView) findViewById (R.id.input_agency_address);
			agencyContactNameView 	= (TextView) findViewById (R.id.input_agency_contactname);
			agencyEmailAddressView 	= (TextView) findViewById (R.id.input_agency_emailaddress);
			agencyNameView 			= (TextView) findViewById (R.id.input_agency_name);
			agencyNotesView 		= (TextView) findViewById (R.id.input_agency_notes);
			agencyPhoneNumberView 	= (TextView) findViewById (R.id.input_agency_phonenumber);
		
			agencyCreateButton = ((Button)findViewById(R.id.agency_new_button));
			agencyCreateButton.setOnClickListener(ButtonListener);
			//---------------------------------------------------------------------- 
			// 22/11/2015 ECU initialise the stored data
			// ---------------------------------------------------------------------
			SelectorUtilities.Initialise ();
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
			
			if(extras !=null) 
			{
				// -----------------------------------------------------------------
				// 17/01/2014 ECU set up the title
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				// 27/11/2014 ECU feed through the selected item
				// -----------------------------------------------------------------
				selectedItem = extras.getInt (StaticData.PARAMETER_SELECTION);
				DisplayAgencyDetails (selectedItem);
				// -----------------------------------------------------------------		
			}
			else
			{
				DisplayAgencyDetails (StaticData.NO_RESULT);
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 21/10/2015 ECU the activity has been recreated after having been
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
	/* ============================================================================= */
    private View.OnClickListener ButtonListener = new View.OnClickListener() 
	{
    	// -------------------------------------------------------------------------
		@Override
		public void onClick(View view) 
		{	
			//----------------------------------------------------------------------
			// 07/01/2014 ECU now process depending on which button pressed
			//----------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.agency_new_button:
				{
					//--------------------------------------------------------------
					// 09/01/2014 ECU accept button pressed so create
					//                carer from entered details
					//-------------------------------------------------------------- 	
			    	CreateAnAgencyEntry ();
					// -------------------------------------------------------------		
					break;
				}
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	public static void AddAgency (int thePosition)
	{
		// -------------------------------------------------------------------------
		// 28/08/2015 ECU start up the activity which will add the agency
		// -------------------------------------------------------------------------
		Intent myIntent = new Intent (CarerSystemActivity.context,AgencyActivity.class);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		CarerSystemActivity.context.startActivity (myIntent);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static ArrayList<ListItem> BuildTheAgenciesList ()
	{
		SelectorUtilities.selectorParameter.listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 27/11/2014 ECU add in the check on size
		// -------------------------------------------------------------------------  
		if (PublicData.agencies.size() > 0)
			for (int theIndex = 0; theIndex < PublicData.agencies.size(); theIndex++)
			{
				// ---------------------------------------------------------------------
				// 30/03/2014 ECU added the index as an argument
				// ---------------------------------------------------------------------
				SelectorUtilities.selectorParameter.listItems.add (new ListItem (
						"",
						PublicData.agencies.get(theIndex).name,
						PublicData.agencies.get(theIndex).contactName,
						PublicData.agencies.get(theIndex).phoneNumber,
						theIndex));
			}
			Collections.sort (SelectorUtilities.selectorParameter.listItems);	
			return SelectorUtilities.selectorParameter.listItems;
	}	
	/* ============================================================================= */
	void CreateAnAgencyEntry ()
	{
    	// -------------------------------------------------------------------------
    	// 03/01/2016 ECU declare a flag to indicate whether the selector activity
    	//                needs to be started (true) or not (false). When carers
    	//                are being added from a list then normally needs to be false
    	//                but for the very first carer then need to set to true
    	// -------------------------------------------------------------------------
    	boolean startSelectorFlag = false;
    	// -------------------------------------------------------------------------
	   	Agency localAgency = new Agency (agencyNameView.getText().toString(),
	   									 agencyAddressView.getText().toString(),
	   									 agencyPhoneNumberView.getText().toString(),
	   									 agencyEmailAddressView.getText().toString(),
	   									 agencyContactNameView.getText().toString(),
	   									 agencyNotesView.getText().toString());
	   	//--------------------------------------------------------------------------	
	   	// 09/01/2014 ECU check if the agency already has an entry
		//--------------------------------------------------------------------------
	   	if (selectedItem == StaticData.NO_RESULT)
	   	{
	   		// ---------------------------------------------------------------------
	   		// 28/08/2015 ECU a new entry to be added
	   		// ---------------------------------------------------------------------
	   		boolean existingEntry = false;
	    	
	   		if (PublicData.agencies.size() > 0)
	   		{
	   			for (int theIndex = 0; theIndex < PublicData.agencies.size(); theIndex++)
	   			{
	   				if (PublicData.agencies.get(theIndex).name.equalsIgnoreCase(localAgency.name))
	   				{
	   					PublicData.agencies.set(theIndex, localAgency);
	   				
	   					existingEntry = true;
	   				
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
	    	// ---------------------------------------------------------------------
	   		if (!existingEntry) 	
	   			PublicData.agencies.add (localAgency);
	   	}
	   	else
	   	{
	   		// ---------------------------------------------------------------------
	   		// 28/08/2015 ECU edit the selected agency
	   		// ---------------------------------------------------------------------
	   		PublicData.agencies.set(selectedItem, localAgency);
	   		// ---------------------------------------------------------------------
	   	}	
		//--------------------------------------------------------------------------	
	   	// 09/01/2014 ECU write updated to data
		//--------------------------------------------------------------------------	
	   	WriteAgencyDataToDisk (context);
	   	// -------------------------------------------------------------------------
		// 28/08/2015 ECU terminate this activity
		// -------------------------------------------------------------------------
		finish ();
		// -------------------------------------------------------------------------
		// 01/09/2015 ECU try and display the agencies as a list
		//            ECU use method with 'false' to indicate that display is
		//                to be rebuilt without starting the activity
		// 03/01/2016 ECU changed the argument from false to startSelectorFlag
		//                because of an issue when the very first carer is
		//                created
		// -------------------------------------------------------------------------
		HandleAgencies (this,startSelectorFlag);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	void DisplayAgencyDetails (int thePosition)
	{
		if (thePosition == StaticData.NO_RESULT)
		{
			agencyAddressView.setText ("");
			agencyContactNameView.setText ("");
			agencyEmailAddressView.setText ("");
			agencyNameView.setText ("");
			agencyNotesView.setText ("");;
			agencyPhoneNumberView.setText ("");
			//----------------------------------------------------------------------
			// 05/02/2014 ECU update the button legend
			//----------------------------------------------------------------------
			agencyCreateButton.setText("Enter Details of New Agency");
		}
		else
		{
			if (PublicData.agencies.size() > 0)
			{
				Agency localAgency = PublicData.agencies.get(thePosition);
				
				agencyAddressView.setText (localAgency.address);
				agencyContactNameView.setText (localAgency.contactName);
				agencyEmailAddressView.setText (localAgency.emailAddress);
				agencyNameView.setText (localAgency.name);
				agencyNotesView.setText (localAgency.notes);;
				agencyPhoneNumberView.setText (localAgency.phoneNumber);
				//------------------------------------------------------------------
				// 05/02/2014 ECU update the button legend
				//------------------------------------------------------------------
				agencyCreateButton.setText("Confirm Changes to Agency");
			}
		}
	}
	/* ============================================================================= */
	public static String [] GetAgencyNames ()
	{
		String [] localNames = new String [PublicData.agencies.size() + 1];
		
		for (int theIndex = 1; theIndex <= PublicData.agencies.size (); theIndex++)
		{
			localNames [theIndex] = "Edit details of '" + PublicData.agencies.get(theIndex - 1).name + "'";
		}
		//--------------------------------------------------------------------------
		// 05/02/2014 ECU put last entry to indicate create
		//--------------------------------------------------------------------------
		localNames [0] = "Click here to select Agency to Edit";
		
		return localNames;
	}
	// =============================================================================
	public static void HandleAgencies (Context theContext,boolean theStartActivityFlag)
	{
		if (PublicData.agencies != null && PublicData.agencies.size() > 0)
		{
			BuildTheAgenciesList ();
			SelectorUtilities.selectorParameter.rowLayout 				= R.layout.agency_row;
			SelectorUtilities.selectorParameter.customMethodDefinition 	= new MethodDefinition<AgencyActivity> (AgencyActivity.class,"AddAgency");
			SelectorUtilities.selectorParameter.customLegend 			= theContext.getString (R.string.add);
			SelectorUtilities.selectorParameter.classToRun 				= AgencyActivity.class;
			SelectorUtilities.selectorParameter.swipeMethodDefinition	= new MethodDefinition<AgencyActivity> (AgencyActivity.class,"SwipeAction");
			SelectorUtilities.selectorParameter.type 					= StaticData.OBJECT_AGENCIES;
			SelectorUtilities.selectorParameter.sort					= true;		// 31/01/2016 ECU added
			// ----------------------------------------------------------------------
			if (theStartActivityFlag)
			{
				SelectorUtilities.StartSelector (theContext,StaticData.OBJECT_AGENCIES);
			}
			else
			{
				// --------------------------------------------------------------------
				// 01/09/2015 ECU just rebuild the agency display
				// --------------------------------------------------------------------
				Selector.SetFromSelectorParameter (SelectorUtilities.selectorParameter);
				Selector.Rebuild ();
				// --------------------------------------------------------------------
			}
		}
		else
		{
			AddAgency (StaticData.NO_RESULT);
		}		
	}
	// ============================================================================= 
    public static void SwipeAction (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 09/06/2015 ECU created to handle swipe actions on a list view item
    	// -------------------------------------------------------------------------
    	// 31/01/2016 ECU check if this agency has any associated carers
    	// -------------------------------------------------------------------------
    	if (PublicData.carers.size() > 0)
    	{
    		for (int theCarer = 0; theCarer < PublicData.carers.size(); theCarer++)
    		{
    			if (PublicData.carers.get(theCarer).agencyIndex == thePosition)
    			{
    				// -------------------------------------------------------------
    				// 31/01/2016 ECU cannot delete this agency
    				// -------------------------------------------------------------
    				Utilities.popToastAndSpeak ("Unable to delete " + 
    				                             PublicData.agencies.get (thePosition).name + 
    				                             " as it has associated carers");
    				// -------------------------------------------------------------
    				// 31/01/2016 ECU no more processing needed
    				// -------------------------------------------------------------
    				return;
    				// -------------------------------------------------------------
    			}
    		}
    	}
    	// -------------------------------------------------------------------------
		// 10/06/2015 ECU created to initiate the dialogue
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (Selector.context,"Item Deletion",
	    		   "Do you really want to delete the entry for '" + PublicData.agencies.get(thePosition).name + "'",
	    		   (Object) thePosition,
	    		   Utilities.createAMethod (AgencyActivity.class,"YesMethod",(Object) null),
	    		   Utilities.createAMethod (AgencyActivity.class,"NoMethod",(Object) null)); 
		// -------------------------------------------------------------------------  
    }
    /* ============================================================================= */
    static void WriteAgencyDataToDisk (Context theContext)
    {
    	//--------------------------------------------------------------------------
    	// 09/01/2014 ECU created
    	// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
    	// 30/03/2016 ECU only write if the hashcode indicates a change
    	//--------------------------------------------------------------------------
    	if (initialHashCode != PublicData.agencies.hashCode())
    	{
    		AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + 
    			theContext.getString (R.string.agencies_file),PublicData.agencies);
    	}
    }
    /* ============================================================================= */
    
    
    
   	// =============================================================================
   	// 10/06/2015 ECU declare the methods used for the dialogue
   	// -----------------------------------------------------------------------------
   	public static void NoMethod (Object theSelection)
   	{
   	}
   	// =============================================================================
   	public static void YesMethod (Object theSelection)
   	{
   		// -------------------------------------------------------------------------
   		// 10/06/2015 ECU the selected item can be deleted
   		// -------------------------------------------------------------------------
   		int localSelection = (Integer) theSelection;
   		PublicData.agencies.remove (localSelection);
   		WriteAgencyDataToDisk (Selector.context);
   		// -------------------------------------------------------------------------
   		// 10/06/2015 ECU rebuild and then display the updated list view
   		// -------------------------------------------------------------------------
   		Selector.Rebuild();
   	}
   	// =============================================================================
}

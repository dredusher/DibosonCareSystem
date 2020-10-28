package com.usher.diboson;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

// ---------------------------------------------------------------------------------
// 08/09/2017 ECU The following suppress was added because the class uses Gravity.LEFT
//				  and this generates a warning stating that Gravity.START should be
//				  used. However '.START' came in at API 14 and this is later than the
//				  lowest API that this app is to support
//----------------------------------------------------------------------------------
@SuppressLint ("RtlHardcoded")
// ---------------------------------------------------------------------------------

public class CarerSystemActivity extends DibosonActivity 
{
	/* ============================================================================= */
	// =============================================================================
	// 12/01/2014 ECU created
	// 22/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 29/01/2016 ECU add the 'display visits log' option
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	//==============================================================================
	//private static final String TAG					  = "CarerSystemActivity";
	/* ============================================================================= */
	public static 	Context	context;
	// =============================================================================
	Button	displayVisitsLogButton;					// 29/01/2016 ECU added
	Button	printCarePlanButton;
	Button	registerAgencyButton;
	Button  registerCarerButton;
	Button  registerCarePlanButton;
	/* ============================================================================= */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 22/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 16/02/2014 ECU call up routine to set common activity features
			// 14/11/2016 ECU added the 'true' to get full screen
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// ---------------------------------------------------------------------
			// 28/08/2015 ECU remember the context for later use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 22/11/2015 ECU initialise the stored data
			// ---------------------------------------------------------------------
			SelectorUtilities.Initialise ();
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
	    
			if (extras != null)
			{
				// -----------------------------------------------------------------
				// 29/03/2014 ECU get the type of input
				// -----------------------------------------------------------------
				//int type	 = extras.getInt (MainActivity.PARAMETER_TYPE,MainActivity.NO_RESULT);  
 	   			// -----------------------------------------------------------------
 	   			// 24/03/2014 ECU get the medication index from the intent
 	   			// -----------------------------------------------------------------
 	   			//int selection	 = extras.getInt (MainActivity.PARAMETER_SELECTION,MainActivity.NO_RESULT);  
 	   			// -----------------------------------------------------------------  
 	   			finish ();
 	   			// -----------------------------------------------------------------
			}
			else
			{
		
				// -----------------------------------------------------------------
				// 29/01/2016 ECU added the 'display visit log' button
				// -----------------------------------------------------------------
				setContentView(R.layout.activity_carer_system);
				// -----------------------------------------------------------------
				displayVisitsLogButton	= (Button) findViewById (R.id.display_care_visit_log_button);
				printCarePlanButton 	= (Button) findViewById (R.id.print_care_plan_button);
				registerAgencyButton 	= (Button) findViewById (R.id.register_agency_button);
				registerCarerButton 	= (Button) findViewById (R.id.register_carer_button);
				registerCarePlanButton	= (Button) findViewById (R.id.register_care_plan_button);
		
				displayVisitsLogButton.setOnClickListener (buttonListener);	
				printCarePlanButton.setOnClickListener    (buttonListener);	
				registerAgencyButton.setOnClickListener   (buttonListener);				
				registerCarerButton.setOnClickListener    (buttonListener);
				registerCarePlanButton.setOnClickListener (buttonListener);
				// -----------------------------------------------------------------
				// 16/07/2017 ECU set the text of the button to show the options
				//                available
				// -----------------------------------------------------------------
				displayVisitsLogButton.setText (Utilities.twoLineButtonLegend (this,
												getString (R.string.display_care_visit_log_button), 
												getString (R.string.display_short_care_visit_log)));
				// -----------------------------------------------------------------
				// 27/08/2015 ECU put in option to show the visits so far
				// 16/07/2017 ECU changed the button to be 'long pressed'
				// -----------------------------------------------------------------
				displayVisitsLogButton.setOnLongClickListener (buttonListenerLong);
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
	/* ============================================================================= */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.

		return true;
	}
	/* ============================================================================= */	
	private View.OnClickListener buttonListener = new View.OnClickListener() 
	{
		// -------------------------------------------------------------------------
		@Override
		public void onClick(View view) 
		{	
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU now process depending on which button pressed
			// ---------------------------------------------------------------------
			switch (view.getId()) 
			{ 
				// -----------------------------------------------------------------
				case R.id.display_care_visit_log_button:
					// -------------------------------------------------------------
					// 29/01/2016 ECU created to handle the display of the visits log
					// 16/07/2017 ECU pass through the name of the required visits
					//                file
					// -------------------------------------------------------------
					DisplayTheVisitsLog (PublicData.visitLogFile);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.print_care_plan_button:
					// -------------------------------------------------------------
					// 12/01/2014 ECU print the current care plan
					// 27/08/2015 ECU changed to use scrollable layout rather than
					//                popToast
					// -------------------------------------------------------------
					setTitle (getString (R.string.current_care_plan));
					setContentView (R.layout.activity_system_info);
					TextView careInformationTextview  = (TextView)findViewById (R.id.system_info_textview);
					careInformationTextview.setGravity (Gravity.LEFT);
					// -------------------------------------------------------------
					// 31/08/2015 ECU set to fixed spacing for a better look
					// -------------------------------------------------------------
					careInformationTextview.setTypeface(Typeface.MONOSPACE);    
					// -------------------------------------------------------------
					// 31/08/2015 ECU now display the care plan
					// -------------------------------------------------------------
					careInformationTextview.setText (PublicData.carePlan.Print());
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.register_agency_button: 
				{
					// -------------------------------------------------------------
					// 28/08/2015 ECU handle agencies
					// -------------------------------------------------------------
					// 30/03/2016 ECU initial the hashcode
					// -------------------------------------------------------------
					AgencyActivity.initialHashCode = PublicData.agencies.hashCode();
					// -------------------------------------------------------------
					// 01/09/2015 ECU added the 'true' argument to indicate that the
					//                activity is to be started
					// -------------------------------------------------------------
					AgencyActivity.HandleAgencies (context,true);
					// -------------------------------------------------------------					
					break;
				}
				// -----------------------------------------------------------------
				case R.id.register_carer_button: 
				{
					// -------------------------------------------------------------
					// 06/01/2014 ECU try and create a new carer
					// -------------------------------------------------------------
					// 30/03/2016 ECU initial the hashcode
					// 25/11/2016 ECU for some reason was wrongly setting to 
					//                .agencies.has.... changed
					// -------------------------------------------------------------
					CarerActivity.initialHashCode = PublicData.carers.hashCode();
					// -------------------------------------------------------------
					// 14/01/2014 ECU only permit if there is an agency registered
					// -------------------------------------------------------------
					if (PublicData.agencies.size () > 0)
					{
						// ---------------------------------------------------------
						// 28/08/2015 ECU handle carers
						// 01/09/2015 ECU added 'true' to method to indicate activity
						//                is to be started
						// ---------------------------------------------------------
						CarerActivity.HandleCarers (context,true);
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 14/01/2014 ECU indicate that cannot create a carer without an agency
						// ---------------------------------------------------------
						Utilities.popToast (getString (R.string.no_agencies));
						// ---------------------------------------------------------
					}
					
					break;
				}
				// -----------------------------------------------------------------
				case R.id.register_care_plan_button: 
				{
					// -------------------------------------------------------------
					// 06/01/2014 ECU try and create a new care plan 
					// -------------------------------------------------------------
					// 30/03/2016 ECU initial the hashcode
					// -------------------------------------------------------------
					CarePlanActivity.initialHashCode = PublicData.carePlan.HashCode();
					// -------------------------------------------------------------
					// 13/01/2014 ECU do not action if no carers or agencies
					// -------------------------------------------------------------
					if ((PublicData.carers.size() > 0) && (PublicData.agencies.size() > 0))
					{
						// ---------------------------------------------------------
						// 13/01/2014 ECU OK to generate a care plan
						// 29/08/2015 ECU changed to use 'selector' method
						// 31/08/2015 ECU add the 'true' to indicate that activity is
						//                to be started
						// ---------------------------------------------------------
						CarePlanActivity.HandleDailyCarePlanList (context,true);
						// ---------------------------------------------------------
					}
					else
					{
						// 13/01/2014 ECU indicate that cannot do the care plan
						
						if (PublicData.carers.size() == 0)
							Utilities.popToast (getString(R.string.no_carers));
						
						if (PublicData.agencies.size() == 0)
							Utilities.popToast (getString(R.string.no_agencies));
					}
					// -------------------------------------------------------------
					break;			
				}			
			}
		}
	};
	/* =============================================================================== */
	private View.OnLongClickListener buttonListenerLong = new View.OnLongClickListener() 
	{		
		@Override
		public boolean onLongClick(View view) 
		{	
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU now process depending on which button pressed
			// ---------------------------------------------------------------------
			switch (view.getId()) 
			{
				// ------------------------------------------------------------------
				case R.id.display_care_visit_log_button: 
				{
					// -------------------------------------------------------------
					// 15/07/2017 ECU display the contents of the 'visit log' with
					//				  full details
					// 16/07/2017 ECU show the abbreviated carer visit file
					// -------------------------------------------------------------
					DisplayTheVisitsLog (PublicData.carerLogFile);
					// -------------------------------------------------------------
					break;
				}		
			}
			// ---------------------------------------------------------------------
			return true;
		}
	};
	/* ============================================================================= */
	
	// =============================================================================
	void DisplayTheVisitsLog (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 29/01/2016 ECU created to display the contents of the display log
		// 16/07/2017 ECU added the file name as an argument
		// -------------------------------------------------------------------------
		setContentView (R.layout.activity_system_info);
		// -------------------------------------------------------------------------
		// 23/09/2020 ECU changed to use resource
		// -------------------------------------------------------------------------
		setTitle (getString (R.string.careplan_visits_log_title));
		// -------------------------------------------------------------------------
		// 29/01/2016 ECU set up the text view which will display the visits log 
		// 20/03/2017 ECU changed to use BLANK....
		// -------------------------------------------------------------------------
		TextView visitsLogTextView  = (TextView) findViewById (R.id.system_info_textview);
		visitsLogTextView.setGravity (Gravity.LEFT);
		visitsLogTextView.setTypeface (Typeface.MONOSPACE,Typeface.BOLD); 
		visitsLogTextView.setText (StaticData.BLANK_STRING);
		// -------------------------------------------------------------------------
		// 29/01/2016 ECU set to auto scroll the text
		// -------------------------------------------------------------------------
		final ScrollView visitsLogScrollView = (ScrollView) findViewById (R.id.system_info_scrollview);
		visitsLogScrollView.post (new Runnable ()
		{
			public void run()
			{
				visitsLogScrollView.fullScroll (View.FOCUS_DOWN);
			}
		});
		// -------------------------------------------------------------------------
		// 29/01/2016 ECU now display the visits data
		// 15/07/2017 ECU changed to use the display of the new 'full details' visit
		//                log file
		// 16/07/2017 ECU use the file name so as to be a bit more flexible
		// ------------------------------------------------------------------------- ;
		visitsLogTextView.append (new String (Utilities.readLinesFromEndOfFile (theFileName,StaticData.SYSTEM_INFO_MAX_LINES)));
		// -------------------------------------------------------------------------
	}
	// =============================================================================

}

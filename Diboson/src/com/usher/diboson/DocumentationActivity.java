package com.usher.diboson;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;

public class DocumentationActivity extends Activity 
{
	// =============================================================================
	// 04/11/2015 ECU created to enable notes to be recorded against a particular 
	//                activity - the name of which is passed through as a parameter
	// =============================================================================
	
	// =============================================================================
	static	String		activityName = null;
	static	Context 	context;
	static 	String  	directoryName;
	static  TextView	documentationTextView;
	static  int			testingLevel;				// 11/11/2015 ECU added
	// =============================================================================
	
	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// ------------------------------------------------------------------------
		// 05/11/2015 ECU set to full screen
		// ------------------------------------------------------------------------
		Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
		// ------------------------------------------------------------------------
		setContentView(R.layout.documentation_activity);
		// ------------------------------------------------------------------------
		// 04/11/2015 ECU get the name of the activity from the intent
		// ------------------------------------------------------------------------
		Bundle extras = getIntent().getExtras();
		// ------------------------------------------------------------------------
		// 18/08/2015 ECU check if tones have been supplied with the intent
		// ------------------------------------------------------------------------
		if (extras != null)
		{
			// --------------------------------------------------------------------
			// 04/11/2015 ECU remember the context for later use
			// --------------------------------------------------------------------
			context = this;
			// --------------------------------------------------------------------
			// 04/11/2015 ECU get the name of the activity
			// --------------------------------------------------------------------
	   		activityName  =  extras.getString (StaticData.PARAMETER_ACTIVITY); 
	   		// --------------------------------------------------------------------
	   		// 11/11/2015 ECU get the testing level
	   		// --------------------------------------------------------------------
	   		testingLevel  = extras.getInt (StaticData.PARAMETER_TESTING_LEVEL, StaticData.NO_RESULT);
	   		// --------------------------------------------------------------------
	   		// 13/11/2015 ECU convert the testing level to a string and check if
	   		//                an error occurs when trying to retrieve the associated
	   		//                string
	   		// --------------------------------------------------------------------
	   		String testingLevelString = StaticData.BLANK_STRING;
	   		// --------------------------------------------------------------------
	   		try
	   		{
	   			if (testingLevel != StaticData.NO_RESULT)
	   				testingLevelString = StaticData.NEWLINE + getResources().getStringArray(R.array.testing_levels)[testingLevel];
	   		}
	   		catch (Exception theException)
	   		{
	   			// ----------------------------------------------------------------
	   			// 13/11/2015 ECU get this if testingLevel doesn't have an associated
	   			//                string
	   			// ----------------------------------------------------------------
	   			testingLevelString = "\nThe testing level of '" + testingLevel + "' does not have an associated string";
	   			// ----------------------------------------------------------------
	   		}
   			// --------------------------------------------------------------------
			// 04/11/2015 ECU display the activity name
	   		// 13/11/2015 ECU changed to use testingLevelString
			// --------------------------------------------------------------------
			((TextView)findViewById (R.id.activity_name_textview)).setText ("Activity : " + activityName + testingLevelString);
					
			// --------------------------------------------------------------------
			// 04/11/2015 ECU declare the listener for the 'add note' button
			// --------------------------------------------------------------------
			((Button) findViewById (R.id.add_documentation_button)).setOnClickListener (buttonListener);
			((Button) findViewById (R.id.cancel_documentation_button)).setOnClickListener (buttonListener);
			// --------------------------------------------------------------------
			// 04/11/2015 ECU set up the name of the directory where the documentation
			//                is held
			// --------------------------------------------------------------------
			directoryName = PublicData.projectFolder + getString (R.string.documentation_directory);
			// --------------------------------------------------------------------
			documentationTextView = (TextView)findViewById (R.id.documentation_textview);
			// --------------------------------------------------------------------	
			// 04/11/2015 ECU show the existing documentation
			// 03/06/2019 ECU pass through the context
			// --------------------------------------------------------------------
			showDocumentation (this,activityName,documentationTextView);
			// --------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 08/11/2015 ECU nothing to do so just finish
			// ---------------------------------------------------------------------
			finish ();
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	
	// =============================================================================
	private View.OnClickListener buttonListener = new View.OnClickListener() 
	{
		// -------------------------------------------------------------------------
		@Override
		public void onClick(View view) 
		{	
			//----------------------------------------------------------------------
			// 04/11/2015 ECU now process depending on which button pressed
			//----------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.add_documentation_button:
					// -------------------------------------------------------------
					// 04/11/2015 ECU add any notes - the 25 is the maximum number of
					//                lines that can be entered
					// -------------------------------------------------------------
					 DialogueUtilities.multilineTextInput (context,
							 							   getString (R.string.notes_title),
							 							   getString (R.string.notes_summary),
							 							   25,
							 							   StaticData.HINT + "Enter any notes you want to add",
							 							   Utilities.createAMethod (DocumentationActivity.class,"AddNote",StaticData.BLANK_STRING),
							 							   Utilities.createAMethod (DocumentationActivity.class,"Cancel",StaticData.BLANK_STRING)); 
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.cancel_documentation_button:
					// -------------------------------------------------------------
					// 04/11/2015 ECU just finish the activity
					// -------------------------------------------------------------
					finish ();
					// -------------------------------------------------------------
					break;
			}
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	
	// =============================================================================
	static void showDocumentation (Context theContext,String theActivityName,TextView theTextView)
	{
		// -------------------------------------------------------------------------
		// 04/11/2015 ECU created to populate the specified text view with the
		//                current documentation
		//            ECU the documentation is held in the 'Documentation' folder
		//                in a file named 'theActivityName'
		// 03/06/2019 ECU added the context as an argument and pass through
		// -------------------------------------------------------------------------
		byte [] localRawDocumentation = Utilities.readAFile (theContext,directoryName + theActivityName);
		// -------------------------------------------------------------------------
		// 04/11/2015 ECU check if there is any documentation
		// -------------------------------------------------------------------------
		if (localRawDocumentation != null)
		{
			theTextView.setText (new String (localRawDocumentation));
		}
		else
		{
			theTextView.setText ("There is no documentation yet");
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public static void AddNote (String theNotes)
	{
		// ------------------------------------------------------------------------
		// 11/07/2015 ECU created to handle the entered message
		// ------------------------------------------------------------------------
		Utilities.AppendToFile (directoryName + activityName,
								StaticData.NEWLINE + Utilities.getAdjustedTime (PublicData.dateFormatterFull) + 
								"  " + PublicData.storedData.developerName + StaticData.NEWLINE + theNotes + StaticData.NEWLINE);
		// ------------------------------------------------------------------------
		// 04/11/2015 ECU refresh the display of recorded notes
		// 03/06/2019 ECU pass through the context
		// -----------------------------------------------------------------------
		showDocumentation (context,activityName,documentationTextView);
		// ------------------------------------------------------------------------
	}
	// ----------------------------------------------------------------------------
	public static void Cancel (String theNotes)
	{
		// ------------------------------------------------------------------------
		// 11/07/2015 ECU cancel the operation
		// ------------------------------------------------------------------------
	}
	// =============================================================================
}

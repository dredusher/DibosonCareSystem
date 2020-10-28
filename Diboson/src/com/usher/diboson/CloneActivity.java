package com.usher.diboson;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CloneActivity extends DibosonActivity 
{
	// =======================================================================
	// see the Notes file for useful information
	// =======================================================================
	// Revision History
	// ================
	// 06/04/2014 ECU created
	// 09/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 14/12/2016 ECU changed all literals of "" to BLANK_STRING
	// -----------------------------------------------------------------------
	// Testing
	// =======
	/* ======================================================================= */
	//public static final String TAG				 = "CloneActivity";
	// =======================================================================
	
	// =======================================================================
	public static CloneRefreshHandler cloneRefreshHandler = null;
	/* ======================================================================= */
	static boolean		cloningDone			= false;
	static String       cloningErrorFiles;				// 14/12/2016 ECU added
	static TextView 	cloneIndicator;
	static TextView 	cloneSummary;
	static String		cloneSummaryDetails;			// 17/10/2014 ECU added
	static Context		context;						// 16/01/2016 ECU added
	static String		fileName			= null;
	static boolean		fileNameDone		= false;
	static String	 	listOfFiles			= StaticData.BLANK_STRING;
	static int			numberCloned        = 0;		// 17/10/2014 ECU added
	/* ======================================================================= */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_cloner_activity);	
			// ---------------------------------------------------------------------
			// 08/04/2014 ECU added the option to keep the screen active when
			//                cloning is taking place (3rd argument = true)
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,!StaticData.ACTIVITY_FULL_SCREEN,StaticData.ACTIVITY_SCREEN_ON);
			// ---------------------------------------------------------------------
			// 16/01/2016 ECU save the context for use in the handler
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 14/12/2016 ECU ensure static variables are set correctly
			// ---------------------------------------------------------------------
			cloningDone			= false;
			cloningErrorFiles	= StaticData.BLANK_STRING;	
			fileName			= null;
			fileNameDone		= false;
			// ---------------------------------------------------------------------
			// 14/12/2016 ECU change to use resources
			// ---------------------------------------------------------------------
			cloneIndicator = (TextView) findViewById (R.id.file_list);
			cloneIndicator.setText (StaticData.BLANK_STRING);
			cloneIndicator.setHint (getString (R.string.clone_list));
			cloneSummary   = (TextView) findViewById (R.id.file_summary);
			cloneSummary.setText (getString (R.string.clone_waiting));		
			// ---------------------------------------------------------------------
			// 06/04/2014 ECU hide a couple of elements
			// 03/05/2015 ECU removed the spinner - no longer exists
			// ---------------------------------------------------------------------
			((Button) findViewById (R.id.button_clone_files)).setVisibility (View.GONE);
			// ---------------------------------------------------------------------
			// 06/04/2014 ECU indicate that in the correct mode to be cloned
			// 03/05/2015 ECU changed to use the 'status' record
			// ---------------------------------------------------------------------
			PublicData.status.cloneMode = true;
			// ---------------------------------------------------------------------
			// 16/01/2016 ECU initialise the refresh handler
			// ---------------------------------------------------------------------
			cloneRefreshHandler = new CloneRefreshHandler ();
			// ---------------------------------------------------------------------
			cloneRefreshHandler.sleep(500);	
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 09/10/2015 ECU the activity has been recreated after having been
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
	static class CloneRefreshHandler extends Handler
	{
		@Override
	    public void handleMessage (Message theMessage) 
	    {   
			switch (theMessage.what)
			{
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_SLEEP:
					// -------------------------------------------------------------
					// 16/01/2016 ECU Note - called when a 'sleep' message is 
					//                received
					// -------------------------------------------------------------
					if (PublicData.cloningInProgress)
					{
						// ---------------------------------------------------------
						// 06/04/2014 ECU check if need to update displayed messages
						// 16/10/2014 ECU include the cloner's IP address
						// 16/12/2016 ECU changed to use the resource
						// ---------------------------------------------------------
						if (!cloningDone)
						{
							cloneSummaryDetails = context.getString (R.string.cloning_progress) + PublicData.clonerIPAddress;
							// ----------------------------------------------------- 
							// 09/01/2015 ECU add the details from the 'cloningDetails' 
							//                object
							// 16/12/2016 ECU changed to use NEWLINE
							// -----------------------------------------------------
							cloneSummaryDetails += StaticData.NEWLINE + ClonerActivity.cloningDetails.Print();
							// -----------------------------------------------------
							cloneSummary.setText (cloneSummaryDetails);
							// -----------------------------------------------------
							// 17/10/2014 ECU indicate that the display has been updated
							// -----------------------------------------------------
							cloningDone = true;
							// -----------------------------------------------------
							// 17/10/2014 ECU inform the user that cloning is complete and
							//                that the application will be restarted
							// -----------------------------------------------------
							Utilities.popToastAndSpeak (context.getString(R.string.cloning_complete)); 
							// -----------------------------------------------------
							// 17/10/2014 ECU initialise the number of files
							// -----------------------------------------------------
							numberCloned = 0;
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
						// 06/04/2014 ECU check if a new file name has been added to
						//                the list
						// ---------------------------------------------------------
						if (!fileNameDone)
						{
							// -----------------------------------------------------
							// 08/01/2015 ECU update the list of files received and indicate
							//                that it has been done
							// -----------------------------------------------------
							cloneIndicator.append (listOfFiles);
							fileNameDone = true;
							// -----------------------------------------------------
							// 17/10/2014 ECU update details on the screen
							// 16/12/2016 ECU changed to use NEWLINE and use resource
							// -----------------------------------------------------
							cloneSummary.setText (cloneSummaryDetails + StaticData.NEWLINE + 
													context.getString (R.string.cloned_number) + numberCloned);
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
						// 06/04/2014 ECU wait a bit
						// ---------------------------------------------------------
						sleep (500);
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 06/04/2014 ECU if cloning has occurred then terminate this activity
						//                otherwise just wait a bit before trying again
						// ---------------------------------------------------------
						if (!cloningDone)
						{	
							sleep (500);
						}
						else
						{
							// -----------------------------------------------------
							// 14/12/2016 ECU check if an error occurred
							// -----------------------------------------------------
							if (cloningErrorFiles.equals (StaticData.BLANK_STRING))
							{
								// -------------------------------------------------
								// 14/12/2016 ECU no error occurred so restart the
								//                app
								// -------------------------------------------------
								// 06/04/2014 ECU indicate to the caller that this activity 
								//                finished OK
								// 17/10/2014 ECU change to use the correct result code
								// -------------------------------------------------
								((Activity) context).setResult (StaticData.RESULT_CODE_FINISH);	
								// -------------------------------------------------
								// 06/04/2014 ECU just finish this activity
								// -------------------------------------------------
								((Activity) context).finish ();
								// -------------------------------------------------
							}
							else
							{
								// -------------------------------------------------
								// 14/12/2016 ECU an error occurred
								// -------------------------------------------------
								Utilities.popToastAndSpeak (context.getString (R.string.cloning_errors),true);
								// -------------------------------------------------
								// 14/12/2016 ECU display the list of files that were
								//                not cloned
								// --------------------------------------------------
								cloneIndicator.setText (context.getString (R.string.cloning_error_files) + cloningErrorFiles);
								// -------------------------------------------------
							}
							// -----------------------------------------------------
						}
					}
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_DISPLAY:
					// -------------------------------------------------------------
					// 15/01/2016 ECU created to append a message to the displayed
					//                textview
					// -------------------------------------------------------------
					cloneIndicator.append ((String)theMessage.obj);
					// -------------------------------------------------------------
					// 16/01/2016 ECU check whether the status of the files is to be
					//                displayed
					// -------------------------------------------------------------
					if (theMessage.arg1 != StaticData.NO_RESULT)
					{
						// ---------------------------------------------------------
						// 16/01/2016 ECU display the status of the file transfer
						//                arg1 contains the pointer into the
						//                     list of files - so add 1 to make it
						//                     a counter
						//                arg2 contains the size of the list of files
						// 14/12/2016 ECU changed to use format and resources
						// ---------------------------------------------------------
						cloneSummary.setText (String.format (context.getString (R.string.cloning_processing_format),
																					(theMessage.arg1 + 1),
																					theMessage.arg2));
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_FINISH:
					// -------------------------------------------------------------
					// 16/01/2016 ECU created to be called by the client to indicate 
					//                that the transfer has finished
					// 16/12/2016 ECU the installation file will have 
					//                been copied from the 'cloner' so
					//                delete it so that it needs to be
					//                re-activated
					// ------------------------------------------------
					Installation.delete (context);
					// -------------------------------------------------
					// 16/01/2016 ECU terminate this activity
					// -------------------------------------------------------------
					((Activity) context).finish ();
					// -------------------------------------------------------------
					break;
					// -------------------------------------------------------------
			}		
	    }
	    /* ------------------------------------------------------------------------ */
	    public void sleep (long delayMillis)
	    {		
	        this.removeMessages (StaticData.MESSAGE_SLEEP);
	        sendMessageDelayed (obtainMessage (StaticData.MESSAGE_SLEEP), delayMillis);
	    }
	};
	/* ============================================================================= */
	public static void FileName (String theFileName,String theErrorMessage)
	{
		// -------------------------------------------------------------------------
		// 14/12/2016 ECU changed to include an optional message on the file name
		// -------------------------------------------------------------------------
		if (fileNameDone)
			listOfFiles = StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
		// 14/12/2016 ECU optionally add the message and indicate an error occurred
		// -------------------------------------------------------------------------
		listOfFiles += StaticData.NEWLINE + theFileName;
		// -------------------------------------------------------------------------
		// 14/12/2016 ECU check if there is an error message
		// -------------------------------------------------------------------------
		if (theErrorMessage != null)
		{
			// ---------------------------------------------------------------------
			// 14/12/2016 ECU indicate that an error occurred while cloning
			//            ECU generate a list of file names that caused the errors
			// ---------------------------------------------------------------------
			cloningErrorFiles += theFileName + StaticData.NEWLINE;
			// ---------------------------------------------------------------------
			// 14/12/2016 ECU add the message to the file entry
			// ---------------------------------------------------------------------
			listOfFiles += " - " + theErrorMessage;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		fileNameDone = false;
		// -------------------------------------------------------------------------
		// 17/10/2014 ECU increment the number of files cloned
		// -------------------------------------------------------------------------
		numberCloned++;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static void FileName (String theFileName)
	{
		// -------------------------------------------------------------------------
		// 14/12/2016 ECU changed to use the new 'main' method
		// -------------------------------------------------------------------------
		FileName (theFileName,null);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
}

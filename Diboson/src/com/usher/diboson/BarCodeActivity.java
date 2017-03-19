package com.usher.diboson;

import java.util.ArrayList;
import java.util.Collections;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.widget.ListView;

public class BarCodeActivity extends DibosonActivity
{
	/* =============================================================================== */
	// ===============================================================================
	// 07/02/2014 ECU created to handle bar code issues
	// 22/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 22/11/2015 ECU try and tidy the way the SelectorParameter is set up to handle
	//                all methods - included the BACK method.
	// 30/03/2016 ECU use hashcode's to determine whether the data has changed and should
	//                be written to disk. Am aware that this is not perfect but good 
	//                first attempt.
	// 13/06/2016 ECU mods to accommodate the addition of 'actions'
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//================================================================================
	/* =============================================================================== */
	final static String TAG = "BarCodeActivity";
	/* =============================================================================== */	
	final static String BARCODE_PACKAGE     = "com.google.zxing.client.android";
	final static String BARCODE_SCAN        = BARCODE_PACKAGE + ".SCAN";
	/* =============================================================================== */
	static 	Activity					activity;			// 08/02/2014 ECU added
	        boolean                     captureImmediately = false;
	static	Context						context;			// 21/11/2015 ECU added
	static 	CustomListViewAdapter 		customListViewAdapter;
			int							initialHashCode;	// 30/03/2016 ECU added
	ArrayList<ListItem> 				listItems = new ArrayList<ListItem>();
															// 07/02/2014 ECU list of barcodes
	ListView    						listView;			// 07/02/2014 ECU added
	/* =============================================================================== */
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
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this);
			// ---------------------------------------------------------------------
			// 13/06/2016 ECU check if any parameters fed through
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
			if (extras != null) 
			{
				captureImmediately = extras.getBoolean(StaticData.PARAMETER_BARCODE, false);
			}
			// ---------------------------------------------------------------------
			// 21/11/2015 ECU remember the context and activity for later use
			// ---------------------------------------------------------------------
			context	= this;
			activity = (Activity) this;
			// ---------------------------------------------------------------------
			// 30/03/2016 ECU get the initial hascode for the data
			// ---------------------------------------------------------------------
			initialHashCode = PublicData.barCodes.hashCode();
			// ---------------------------------------------------------------------
			// 07/02/2014 ECU display the existing bar codes as a list
			// 14/02/2014 ECU put in the check for null and size which would imply
			//                that there are no stored bar codes. In which case
			//                try and capture a bar code
			//133/06/2016 ECU added the check on capture immediately
			// ---------------------------------------------------------------------	
			if ((PublicData.barCodes != null) && (PublicData.barCodes.size() > 0) && !captureImmediately)
			{
				// -----------------------------------------------------------------
				// 14/02/2014 ECU display existing bar codes
				// 21/11/2015 ECU changed to use selector class
				// -----------------------------------------------------------------
				selectBarCode ();
				// -----------------------------------------------------------------
				// 21/11/2015 ECU tell the user what to do
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (getString(R.string.start_scanner));
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 14/02/2014 ECU capture a new barcode (the first)
				// -----------------------------------------------------------------
				CaptureBarCode (this);
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
		return true;
	}
	/* ============================================================================= */
	@Override
	public void onActivityResult (int theRequestCode, int theResultCode, Intent theIntent) 
	{
		// -------------------------------------------------------------------------
		// 30/08/2013 ECU called when an activity returns a result
		// -------------------------------------------------------------------------
		// 30/08/2013 ECU check if the result of the barcode activity
		// 20/11/2015 ECU changed to use StaticData
		// -------------------------------------------------------------------------
	    if (theRequestCode == StaticData.RESULT_CODE_BARCODE) 
	    {
	        if (theResultCode == RESULT_OK) 
	        {
	        	// -----------------------------------------------------------------
	        	// 30/08/2013 ECU a barcode was successfully read
	        	// -----------------------------------------------------------------
	            String barcodeRead   = theIntent.getStringExtra ("SCAN_RESULT");
	            String barcodeFormat = theIntent.getStringExtra ("SCAN_RESULT_FORMAT");
	            // -----------------------------------------------------------------
	            // 30/08/2013 ECU call the appropriate handler
	            // -----------------------------------------------------------------
	            barcodeHandler (getBaseContext(),barcodeRead,barcodeFormat);
				// -----------------------------------------------------------------
	        } 
	        else 
	        if (theResultCode == RESULT_CANCELED) 
	        {
	        	// -----------------------------------------------------------------
	            // 30/08/2013 ECU Handle cancel
	        	// -----------------------------------------------------------------
	        	// 24/11/2015 ECU the scanning for a barcode has been cancelled so
	        	//                just redisplay the list of current barcodes
	        	// -----------------------------------------------------------------
	        	selectBarCode ();
	        	// -----------------------------------------------------------------
	        }
	    }
	    else
	    // -------------------------------------------------------------------------
	    // 21/11/2015 ECU check if a new bar code and its details have been entered
	    // -------------------------------------------------------------------------
	    if (theRequestCode == StaticData.RESULT_CODE_BARCODE_NEW) 
	    {
	    	// ---------------------------------------------------------------------
	    	// 24/11/2015 ECU whatever the result then just restart the Selector
	    	//                activity
	    	// ---------------------------------------------------------------------
	        if (theResultCode == RESULT_OK || theResultCode == RESULT_CANCELED) 
	        {
	        	// -----------------------------------------------------------------
	        	// 21/11/2015 ECU a barcode was added.
	        	//            ECU restart the Selector activity
	        	// -----------------------------------------------------------------
	        	selectBarCode ();
	            // -----------------------------------------------------------------
	        } 
	    }
	}
	/* ============================================================================= */
	public void onDestroy()
	{	
		// -------------------------------------------------------------------------
		// 08/02/2014 ECU make sure the disk is update
		// -------------------------------------------------------------------------
		// 30/03/2016 ECU only update the data on disk if the hashcode indicates a
		//                data change
		// -------------------------------------------------------------------------
		if (initialHashCode != PublicData.barCodes.hashCode())
		{
			// ---------------------------------------------------------------------
			// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
			// ---------------------------------------------------------------------
			AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + getBaseContext().getString (R.string.bar_code_data),
				PublicData.barCodes);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		super.onDestroy();
	}
	// ============================================================================= 
	public static void barcodeHandler (Context theContext,String theBarCode,String theBarCodeFormat)
	{
		// -------------------------------------------------------------------------
		// 30/08/2013 ECU added - handle incoming barcodes
		// 21/11/2015 ECU moved here from Utilities
		// -------------------------------------------------------------------------
		BarCode localBarCode;
		// -------------------------------------------------------------------------
		// 15/09/2013 ECU check whether the read product has already been registered
		// 07/02/2014 ECU change to reflect new use of a 'List'
		// 13/06/2016 ECU changed the logic
		// -------------------------------------------------------------------------
		if (PublicData.barCodes.size() > 0)
		{
			for (int theIndex = 0; theIndex < PublicData.barCodes.size(); theIndex++)
			{
				localBarCode = PublicData.barCodes.get (theIndex);
			
				if (localBarCode.barCode.equalsIgnoreCase(theBarCode))
				{
					// -------------------------------------------------------------
					// 13/06/2016 ECU a match has been found
					//            ECU if actions are defined the process them - if
					//                not then process the description
					// -------------------------------------------------------------
					// 13/06/2016 ECU check for any actions
					// -------------------------------------------------------------
					if (localBarCode.actions != null)
					{
						Utilities.actionHandler(context,localBarCode.actions);
					}
					else
					{
						// ---------------------------------------------------------
						// 15/09/2013 ECU have a product registered so just give its details
						// 21/11/2015 ECU changed from popToast following move here from
						//                Utilities
						// ---------------------------------------------------------
						Utilities.popToastAndSpeak (localBarCode.description);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 21/11/2015 ECU need to restart the Selector activity
					// -------------------------------------------------------------
					selectBarCode ();
					// -------------------------------------------------------------
					return;
				}
			}
		}
		// -------------------------------------------------------------------------
		// 13/06/2016 ECU get here if the bar code does not exist
		// ---------------------------------------------------------------------
		// 15/09/2013 ECU product is not known so register it
		// ---------------------------------------------------------------------
		Intent localIntent = new Intent (theContext,BarCodeEntry.class);
		// ---------------------------------------------------------------------
		// 10/08/2013 ECU added the NEW_TASK flag
		// ---------------------------------------------------------------------
		//localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		// ---------------------------------------------------------------------
		// 15/09/2013 ECU feed through the barcode that has just been read
		// 08/02/2014 ECU changed to use PARAMETER_ rather than literal
		// ---------------------------------------------------------------------
		localIntent.putExtra (StaticData.PARAMETER_BARCODE,theBarCode);
		activity.startActivityForResult (localIntent,StaticData.RESULT_CODE_BARCODE_NEW);
	}
	// =============================================================================
	public static ArrayList<ListItem> BuildTheBarCodeList ()
	{
		// -------------------------------------------------------------------------
		// 20/11/2015 ECU created to build the bar code list to be used with the
		//                custom adapter
		// -------------------------------------------------------------------------
		SelectorUtilities.selectorParameter.listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 21/11/2015 ECU add in the check on size
		// -------------------------------------------------------------------------  
		if (PublicData.barCodes.size() > 0)
		{
			for (int theIndex = 0; theIndex < PublicData.barCodes.size(); theIndex++)
			{
				// -----------------------------------------------------------------
				// 21/11/2015 ECU added the index as an argument
				// 13/06/2016 ECU added 'actions' in the display
				// -----------------------------------------------------------------
				SelectorUtilities.selectorParameter.listItems.add (new ListItem (
								"",
								PublicData.barCodes.get (theIndex).description,
								PublicData.barCodes.get (theIndex).barCode,
								((PublicData.barCodes.get (theIndex).actions == null) ? "" : "Actions Defined"),
								theIndex));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 21/11/2015 ECU sort the items by the description
			// ---------------------------------------------------------------------
			Collections.sort (SelectorUtilities.selectorParameter.listItems);
		}
		// -------------------------------------------------------------------------
		// 21/11/2015 ECU return the list of barcodes that have been generated
		// -------------------------------------------------------------------------
		return SelectorUtilities.selectorParameter.listItems;
		// -------------------------------------------------------------------------
	}	
	// =============================================================================
	static void CaptureBarCode (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 08/02/2014 ECU indicate what is going on
		// 13/06/2016 ECU changed to use the resource
		// -------------------------------------------------------------------------
		TextToSpeechService.SpeakAPhrase (theContext.getString (R.string.bar_code_place_camera));
		
		if (Utilities.checkIfAppInstalled (theContext,BARCODE_PACKAGE))
		{
			Intent localIntent = new Intent (BARCODE_SCAN);
			// ---------------------------------------------------------------------
			// 30/08/2013 ECU do not specify a SCAN_MODE so that any barcodes, QR
			//				  codes can be scanned
			//
			//                if you want to specify a particular scan mode then
			//                put in a line similar to
			//                   myIntent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			//				  check on the internet for particular codes
			// 20/11/2015 ECU changed to use StaticData
			// 21/11/2015 ECU added 'activity.' after making static
			// ---------------------------------------------------------------------
			activity.startActivityForResult (localIntent,StaticData.RESULT_CODE_BARCODE);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 30/08/2013 ECU the required barcode software is not installed
			// ---------------------------------------------------------------------
			Utilities.popToast (BARCODE_PACKAGE + " is not installed");
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	static void selectBarCode ()
	{
		// -------------------------------------------------------------------------
		// 21/11/2015 ECU before activating the Selector activity then need to set up
		//                various parameters
		// -------------------------------------------------------------------------
		// 22/11/2015 ECU initialise the stored arguments
		// -------------------------------------------------------------------------
		SelectorUtilities.Initialise ();
		// -------------------------------------------------------------------------
		// 21/11/2015 ECU build up the list of items that will be displayed
		// -------------------------------------------------------------------------
		BuildTheBarCodeList ();
		// -------------------------------------------------------------------------
		// 21/11/2015 ECU set up the variables that control the interaction with
		//                the Selector activity
		// -------------------------------------------------------------------------
		SelectorUtilities.selectorParameter.classToRun 				= BarCodeEntry.class;
		SelectorUtilities.selectorParameter.customLegend 			= context.getString (R.string.edit);
		SelectorUtilities.selectorParameter.rowLayout 				= R.layout.barcode_row;
		SelectorUtilities.selectorParameter.type 					= StaticData.OBJECT_BARCODES;
		// -------------------------------------------------------------------------
		// 21/11/2015 ECU declare the methods that will be called by the Selector
		//                activity when certain key strokes occur
		// -------------------------------------------------------------------------
		SelectorUtilities.selectorParameter.backMethodDefinition 	
							= new MethodDefinition<BarCodeActivity> (BarCodeActivity.class,"BackAction");
		SelectorUtilities.selectorParameter.customMethodDefinition 	
							= new MethodDefinition<BarCodeActivity> (BarCodeActivity.class,"EditAction");
		SelectorUtilities.selectorParameter.helpMethodDefinition 	
							= new MethodDefinition<BarCodeActivity> (BarCodeActivity.class,"HelpAction");
		SelectorUtilities.selectorParameter.longSelectMethodDefinition 	
							= new MethodDefinition<BarCodeActivity> (BarCodeActivity.class,"SelectAction");
		SelectorUtilities.selectorParameter.swipeMethodDefinition	
							= new MethodDefinition<BarCodeActivity> (BarCodeActivity.class,"SwipeAction");
		// -------------------------------------------------------------------------
		// 21/11/2015 ECU now start up the Selector activity which will be
		//                configured using the arguments in the
		//                'SelectorUtilities.selectorParameter'
		// -------------------------------------------------------------------------
		SelectorUtilities.StartSelector (context,StaticData.OBJECT_BARCODES);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	
	// =============================================================================
	// =============================================================================
	// 21/11/2015 ECU create methods that are called up as a result of a call from
	//                the Selector activity
	// =============================================================================
	// =============================================================================
	
	// =============================================================================
	public static void BackAction (int theDummyArgument)
	{
		// -------------------------------------------------------------------------
		// 22/11/2015 ECU created to handle the BACK key when called from the
		//                Selector activity. The argument is included but is not 
		//                needed.
		// -------------------------------------------------------------------------
		// 22/11/2015 ECU just finish the activity
		// -------------------------------------------------------------------------
		activity.finish ();
		//--------------------------------------------------------------------------
	}
	// =============================================================================
    public static void CancelBarcodeMethod (String theBarcode)
    {
    	// -------------------------------------------------------------------------
    	// 23/11/2015 ECU create to cancel after barcode input
    	// -------------------------------------------------------------------------
    	
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
    public static void EditAction (int theBarcodeSelected)
    {
    	// -------------------------------------------------------------------------
    	// 21/11/2015 ECU create to handle the custom button
    	// -------------------------------------------------------------------------
		// 08/0/2014 ECU want to edit the selected barcode
		// -------------------------------------------------------------------------
		// 15/09/2013 ECU product is not known so register it
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (context,BarCodeEntry.class);
		// -------------------------------------------------------------------------
		// 10/08/2013 ECU added the NEW_TASK flag
		// -------------------------------------------------------------------------
		localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		// -------------------------------------------------------------------------
		// 15/09/2013 ECU feed through the barcode that has just been read
		// 08/02/2014 ECU changed to use PARAMETER_ rather than literal
		// 13/06/2016 ECU include the 'actions'
		// -------------------------------------------------------------------------
		localIntent.putExtra (StaticData.PARAMETER_BARCODE,PublicData.barCodes.get(theBarcodeSelected).barCode);
		localIntent.putExtra (StaticData.PARAMETER_BARCODE_DESC,PublicData.barCodes.get(theBarcodeSelected).description);
		localIntent.putExtra (StaticData.PARAMETER_BARCODE_ACTIONS,PublicData.barCodes.get(theBarcodeSelected).actions);
		// ------------------------------------------------------------------------
		activity.startActivity (localIntent);
		// ------------------------------------------------------------------------
    }
	// =============================================================================
    public static void HelpAction (int theBarcodeSelected)
    {
    	// -------------------------------------------------------------------------
    	// 21/11/2015 ECU create to process the selection of a barcode
    	// -------------------------------------------------------------------------
		Utilities.popToast (PublicData.barCodes.get(theBarcodeSelected).Print());
		// -------------------------------------------------------------------------
		// 08/02/2014 ECU now speak the description
		// -------------------------------------------------------------------------
		TextToSpeechService.SpeakAPhrase (PublicData.barCodes.get(theBarcodeSelected).description);
    }
    // =============================================================================
    public static void InputBarcodeMethod (String theBarcode)
    {
    	// -------------------------------------------------------------------------
    	// 23/11/2015 ECU create to accept the input barcode
    	//            ECU initialise 'finish' the Selector activity because the
    	//                'barcodeHandler' will start it again as required
    	// 24/11/2015 ECU changed to use the 'Finish' method
    	// -------------------------------------------------------------------------
    	Selector.Finish();
    	// -------------------------------------------------------------------------
    	barcodeHandler (context,theBarcode,"");
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
  	public static void ManualMethod (Object theSelection)
  	{
  		// -------------------------------------------------------------------------
  		// 23/11/2015 ECU created to manually input a barcode
  		// -------------------------------------------------------------------------
  		DialogueUtilities.textInput (Selector.context,
				   					 "Manually Enter a Barcode",
				   					 "Just type in the information that defines a barcode",
				   					 StaticData.HINT + "Type in the barcode",
				   					 Utilities.createAMethod (BarCodeActivity.class,"InputBarcodeMethod",""),
				   					 Utilities.createAMethod (BarCodeActivity.class,"CancelBarcodeMethod",""));
  		// -------------------------------------------------------------------------
  	}
	// =============================================================================
	public static void NoMethod (Object theSelection)
  	{
  	}
	// =============================================================================
	public static void ScanMethod (Object theSelection)
	{
		// -------------------------------------------------------------------------
		// 23/11/2015 ECU created to obtain the barcode by scanning
		//            ECU initially close the Selector activity because the methods
		//                called by CaptureBarCode will restart Selector as necessary
		// 24/11/2015 ECU changed to use the new 'Finish' method
		// -------------------------------------------------------------------------
		Selector.Finish();
		// -------------------------------------------------------------------------
		// 23/11/2015 ECU now scan or manually input a barcode
		// -------------------------------------------------------------------------
		CaptureBarCode (context);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    public static void SelectAction (int theBarcodeSelected)
    {
    	// -------------------------------------------------------------------------
    	// 21/11/2015 ECU create to handle the custom button
    	// -------------------------------------------------------------------------
    	DialogueUtilities.yesNo (Selector.context,"Barcode Source",
	    		   				 "Do you want to manually enter a barcode ?",
	    		   				 (Object) theBarcodeSelected,
	    		   				 Utilities.createAMethod (BarCodeActivity.class,"ManualMethod",(Object) null),
	    		   				 Utilities.createAMethod (BarCodeActivity.class,"ScanMethod",(Object) null)); 
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
    public static void SwipeAction (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 09/06/2015 ECU created to handle swipe actions on a list view item
    	// -------------------------------------------------------------------------
		// 10/06/2015 ECU created to initiate the dialogue
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (Selector.context,"Item Deletion",
	    		   				 "Do you really want to delete the entry for '" + PublicData.barCodes.get (thePosition).description + "'",
	    		   				 (Object) thePosition,
	    		   				 Utilities.createAMethod (BarCodeActivity.class,"YesMethod",(Object) null),
	    		   				 Utilities.createAMethod (BarCodeActivity.class,"NoMethod",(Object) null)); 
		// -------------------------------------------------------------------------  
    }
  	// =============================================================================
  	public static void YesMethod (Object theSelection)
  	{
  		// -------------------------------------------------------------------------
  		// 10/06/2015 ECU the selected item can be deleted
  		// -------------------------------------------------------------------------
  		int localSelection = (Integer) theSelection;
  		PublicData.barCodes.remove (localSelection);
  		// -------------------------------------------------------------------------
  		// 10/06/2015 ECU rebuild and then display the updated list view
  		// -------------------------------------------------------------------------
  		Selector.Rebuild();
  		// -------------------------------------------------------------------------
  	}
  	// =============================================================================
}

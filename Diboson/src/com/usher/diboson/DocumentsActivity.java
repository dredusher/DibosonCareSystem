package com.usher.diboson;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class DocumentsActivity extends DibosonActivity 
{
	// =============================================================================
	// 13/04/2018 ECU changed the activity to use ListViewSelector rather than Selector
	//                so as to reduce the number of static's
	// =============================================================================
			Activity	activity;
			Context		context;
			EditText	documentPath;
			EditText	documentTitle;
	ListViewSelector	listViewSelector;
	// =============================================================================
	
	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		// 06/10/2016 ECU put in the check as to whether the activity has been created
		//                anew or is being recreated after having been destroyed by
		//                the Android OS
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 13/04/2018 ECU added the full screen option
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			// ---------------------------------------------------------------------
			// 18/10/2016 ECU remember the activity and context for later use
			// ---------------------------------------------------------------------
			activity	= this;
			context 	= this;
			// ---------------------------------------------------------------------
			// 18/10/2016 ECU check if the object has been initialised
			// ---------------------------------------------------------------------
			if (PublicData.storedData.documents == null)
			{
				// -----------------------------------------------------------------
				// 18/10/2016 ECU create an empty list
				// -----------------------------------------------------------------
				PublicData.storedData.documents = new ArrayList<Document> ();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			if (PublicData.storedData.documents.size() > 0)
			{
				// -----------------------------------------------------------------
				// 13/04/2018 ECU initialist the display - changed to use ListViewSelector
				// -----------------------------------------------------------------
				initialiseDisplay (this);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 18/10/2016 ECU no documents registered yet
				// -----------------------------------------------------------------
				registerADocument (this,StaticData.NO_RESULT);
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 18/10/2016 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
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
				// 13/04/2018 ECU get the path of the selected document - this is
				//                stored in the returned intent
				// ----------------------------------------------------------------
				documentPath.setText (theIntent.getStringExtra (StaticData.PARAMETER_FILE_PATH));
		 		// -----------------------------------------------------------------
			}
			// --------------------------------------------------------------------
		}
	}
	// ============================================================================= 
    public void AddDocument (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 18/10/2016 ECU called to display the selected file
    	// -------------------------------------------------------------------------
    	registerADocument (context,StaticData.NO_RESULT);
    	// -------------------------------------------------------------------------
    }
 // ============================================================================= 
    public void EditDocument (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	registerADocument (context,thePosition);
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
	private View.OnClickListener buttonListener = new View.OnClickListener () 
	{
		@Override
		public void onClick (View view) 
		{	
			// ---------------------------------------------------------------------
			// 15/11/2014 ECU handle the button
			// ---------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.document_browse_button: 
				{
					// -------------------------------------------------------------
					// 18/10/2016 ECU browse for the required file
					// -------------------------------------------------------------
					Utilities.PickAFile (activity,
							 			 PublicData.projectFolder,
							 			 StaticData.EXTENSION_DOCUMENT,
							 			 true);
					// -------------------------------------------------------------
					break;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				case R.id.document_register_button: 
				{
					// -------------------------------------------------------------
					// 18/10/2016 ECU register the document using the input data
					// -------------------------------------------------------------
					String localPath  = documentPath.getText().toString();
					String localTitle = documentTitle.getText().toString();
					// -------------------------------------------------------------
					// 18/10/2016 ECU check that there is data
					// -------------------------------------------------------------
					if (!Utilities.emptyString(localTitle) || !Utilities.emptyString(localPath))
					{
						Utilities.popToastAndSpeak (getString (R.string.document_data_needed), true);
					}
					else
					{
						// ---------------------------------------------------------
						// 13/04/2018 ECU add the new document into the list
						// ---------------------------------------------------------
						Document.Add (localTitle,localPath);
						// ---------------------------------------------------------
						// 13/04/2018 ECU now redisplay the screen
						// ---------------------------------------------------------
						// 13/04/2018 ECU because the layout has been changed within
						//                this activity then need to do a complete
						//                rebuild of the display
						initialiseDisplay (activity);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					break;
				}
				// -----------------------------------------------------------------
			}
		}
	};
	// =============================================================================
	void registerADocument (Context theContext,int theDocument)
	{
		// -------------------------------------------------------------------------
		// 18/10/2016 ECU created to register a document to the system
		//            ECU for a new entry then theDocument is NO_RESULT, if an edit
		//                then the document is the document being adited
		// -------------------------------------------------------------------------
		((Activity) theContext).setContentView (R.layout.activity_documents);
		// -------------------------------------------------------------------------
		// 18/10/2016 ECU set up the listeners to the buttons
		// -------------------------------------------------------------------------
		((Button) ((Activity) theContext).findViewById (R.id.document_browse_button)).setOnClickListener(buttonListener);
		((Button) ((Activity) theContext).findViewById (R.id.document_register_button)).setOnClickListener(buttonListener);
		// -------------------------------------------------------------------------
		// 18/10/2016 ECU declare the fields that will contain the required data
		// -------------------------------------------------------------------------
		documentPath 	= (EditText) ((Activity) theContext).findViewById (R.id.document_path_edittext);
		documentTitle 	= (EditText) ((Activity) theContext).findViewById (R.id.document_title_edittext);
		// -------------------------------------------------------------------------
		// 18/10/2016 ECU check if a document is being edited
		// -------------------------------------------------------------------------
		if (theDocument != StaticData.NO_RESULT)
		{
			documentPath.setText (PublicData.storedData.documents.get(theDocument).path);
			documentTitle.setText (PublicData.storedData.documents.get(theDocument).title);
		}
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
    public void SelectAction (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 18/10/2016 ECU called to display the selected file
    	// -------------------------------------------------------------------------
    	Utilities.displayDocument (context,PublicData.storedData.documents.get(thePosition).path);
    	// -------------------------------------------------------------------------
    	// 18/10/2016 ECU just finish this activity
    	// -------------------------------------------------------------------------
    	finish ();
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
    public void SwipeAction (int thePosition)
    {
    	// -------------------------------------------------------------------------
		// 18/10/2016 ECU created to confirm the deletion
    	// 13/04/2018 ECU changed to use the non-static version
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.yesNo (context,
										  activity,
										  "Item Deletion",
										  String.format (getString (R.string.delete_confirmation_format),PublicData.storedData.documents.get (thePosition).title),
										  					(Object) thePosition,
										  Utilities.createAMethod (DocumentsActivity.class,"YesMethod",(Object) null),
										  null); 
		// -------------------------------------------------------------------------  
    }
	// =============================================================================
  	public void YesMethod (Object theSelection)
  	{
  		// -------------------------------------------------------------------------
  		// 18/10/2016 ECU the selected item can be deleted
  		// -------------------------------------------------------------------------
  		int localSelection = (Integer) theSelection;
  		// --------------------------------------------------------------------------
    	// 18/10/2016 ECU called to display the selected file
    	// -------------------------------------------------------------------------
    	PublicData.storedData.documents.remove (localSelection);
  		// -------------------------------------------------------------------------
  		// 13/04/2018 ECU check whether everything has been deleted or not
  		// -------------------------------------------------------------------------
  		if (PublicData.storedData.documents.size () > 0)
  		{
  			// ---------------------------------------------------------------------
  			// 13/04/2018 ECU rebuild the display
  			// ---------------------------------------------------------------------
  			refreshDisplay ();
  			// ---------------------------------------------------------------------
  		}
  		else
  		{
  			// ---------------------------------------------------------------------
  			// 13/04/2018 ECU tell the user that all documents have been deleted
  			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (getString (R.string.documents_all_deleted),true);
			// ---------------------------------------------------------------------
			// 13/04/2018 ECU cannot do any more so terminate this activity
			// ---------------------------------------------------------------------
			finish ();
			// ---------------------------------------------------------------------
  		}
    	// -------------------------------------------------------------------------
  	}
    // ============================================================================= 
  	
  	
  	// =============================================================================
  	// =============================================================================
  	// ListViewSelector
  	// ================
  	//
  	//		Declare methods associated with the use of ListViewSelector
  	//
  	// ============================================================================
  	// ============================================================================
  	
	// =============================================================================
	void initialiseDisplay (Activity theActivity)
	{
		// -------------------------------------------------------------------------
		// 13/04/2018 ECU created to generate the display of stored documents
		// -------------------------------------------------------------------------
		listViewSelector = new ListViewSelector (theActivity,
				   								 R.layout.document_row,
				   								 Utilities.createAMethod (DocumentsActivity.class, "PopulateTheList"),
				   								 true,
				   								 Utilities.createAMethod (DocumentsActivity.class,"SelectAction",0),
				   								 StaticData.NO_HANDLING_METHOD,
				   								 Utilities.createAMethod (DocumentsActivity.class,"EditDocument",0),
				   								 getString (R.string.add),
				   								 Utilities.createAMethod (DocumentsActivity.class,"AddDocument",0),
				   								 StaticData.NO_HANDLING_METHOD,
				   								 Utilities.createAMethod (DocumentsActivity.class,"SwipeAction",0)
				   								);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public ArrayList<ListItem> PopulateTheList ()
	{
		// -------------------------------------------------------------------------
		// 13/04/2018 ECU created to build a list of the currently stored documents
		// -------------------------------------------------------------------------
		ArrayList<ListItem> listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 13/04/2018 ECU add in the check on size
		// ------------------------------------------------------------------------- 
		if (PublicData.storedData.documents.size() > 0)
		{
			for (int theIndex = 0; theIndex < PublicData.storedData.documents.size(); theIndex++)
			{
				// -----------------------------------------------------------------
				// 13/04/2018 ECU create a new item with the required data
				// -----------------------------------------------------------------
				ListItem localListItem = new ListItem (null,
													   PublicData.storedData.documents.get (theIndex).title,
													   StaticData.BLANK_STRING,
													   PublicData.storedData.documents.get (theIndex).path,
													   theIndex);
				// -----------------------------------------------------------------
				// 13/04/2018 ECU add the new item into the list
				// -----------------------------------------------------------------
				listItems.add (localListItem);
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 13/04/2018 ECU sort the generated items
		// -------------------------------------------------------------------------
		Collections.sort (listItems);
		// -------------------------------------------------------------------------
		// 13/04/2018 ECU return the generated list
		// -------------------------------------------------------------------------
		return listItems;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void refreshDisplay ()
	{
		// -------------------------------------------------------------------------
		// 13/04/2018 ECU created to refresh the display if it exists or create the
		//                display if not
		// -------------------------------------------------------------------------
		if (listViewSelector == null)
		{
			// ---------------------------------------------------------------------
			// 13/04/2018 ECU need to build the display
			// ---------------------------------------------------------------------
			initialiseDisplay (this);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 13/04/2018 ECU display already initialised so just refresh it
			// ---------------------------------------------------------------------
			listViewSelector.refresh ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

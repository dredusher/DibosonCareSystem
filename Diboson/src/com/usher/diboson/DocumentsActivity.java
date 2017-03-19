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
	static	Activity	activity;
	static	Context		context;
	static	EditText	documentPath;
	static	EditText	documentTitle;
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
				HandleDocuments (this);
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
	// ============================================================================= 
    public static void AddDocument (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 18/10/2016 ECU finish the 'select' activity before continuing
    	// -------------------------------------------------------------------------
    	Selector.Finish();
    	// -------------------------------------------------------------------------
    	// 18/10/2016 ECU called to display the selected file
    	// -------------------------------------------------------------------------
    	registerADocument (context,StaticData.NO_RESULT);
    	// -------------------------------------------------------------------------
    }
 // ============================================================================= 
    public static void EditDocument (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 18/10/2016 ECU finish the 'select' activity before continuing
    	// -------------------------------------------------------------------------
    	Selector.Finish ();
    	// -------------------------------------------------------------------------
    	registerADocument (context,thePosition);
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
	public static void BackKeyMethod (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 18/10/2016 ECU created to be called when the back key pressed
		// -------------------------------------------------------------------------
		activity.finish ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static ArrayList<ListItem> BuildTheDocumentsList ()
	{
		SelectorUtilities.selectorParameter.listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 18/10/2016 ECU add in the check on size
		// ------------------------------------------------------------------------- 
		if (PublicData.storedData.documents.size() > 0)
		{
			for (int theIndex = 0; theIndex < PublicData.storedData.documents.size(); theIndex++)
			{
				// -----------------------------------------------------------------
				// 30/03/2014 ECU added the index as an argument
				// 31/01/2016 ECU do not add carers which have been deleted
				// -----------------------------------------------------------------
				ListItem localListItem = new ListItem (null,
													   PublicData.storedData.documents.get (theIndex).title,
													   "",
													   PublicData.storedData.documents.get (theIndex).path,
													   theIndex);
				
				// -----------------------------------------------------------------
				SelectorUtilities.selectorParameter.listItems.add (localListItem);
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		Collections.sort (SelectorUtilities.selectorParameter.listItems);
		// -------------------------------------------------------------------------
		return SelectorUtilities.selectorParameter.listItems;
	}	
	// =============================================================================
	private static View.OnClickListener buttonListener = new View.OnClickListener() 
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
					Utilities.selectAFile (context,StaticData.EXTENSION_DOCUMENT,
							new MethodDefinition <DocumentsActivity> (DocumentsActivity.class,"SelectedDocument"));
					// -------------------------------------------------------------
					break;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				case R.id.document_register_button: 
				{
					// -------------------------------------------------------------
					// 18/10/2016 ECU register the docoument using the input data
					// -------------------------------------------------------------
					String localPath 	= documentPath.getText().toString();
					String localTitle = documentTitle.getText().toString();
					// -------------------------------------------------------------
					// 18/10/2016 ECU check that there is data
					// -------------------------------------------------------------
					if (!Utilities.emptyString(localTitle) || !Utilities.emptyString(localPath))
					{
						Utilities.popToastAndSpeak(context.getString (R.string.document_data_needed), true);
					}
					else
					{
						Document.Add (localTitle,localPath);
						// ---------------------------------------------------------
						// 18/10/2016 ECU want to stop and restart this activity
						// ---------------------------------------------------------
						activity.finish ();
						// ---------------------------------------------------------
						// 18/09/2016 ECU restart this activity
						// ---------------------------------------------------------
						Intent localIntent = activity.getIntent ();
						activity.startActivity (localIntent);
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
	public static void HandleDocuments (Context theContext)
	{
		// -------------------------------------------------------------------------
		SelectorUtilities.Initialise();
		// -------------------------------------------------------------------------
		BuildTheDocumentsList ();
		SelectorUtilities.selectorParameter.rowLayout 				= R.layout.document_row;
		SelectorUtilities.selectorParameter.backMethodDefinition 	= new MethodDefinition<DocumentsActivity> (DocumentsActivity.class,"BackKeyMethod");
		SelectorUtilities.selectorParameter.customMethodDefinition 	= new MethodDefinition<DocumentsActivity> (DocumentsActivity.class,"AddDocument");
		SelectorUtilities.selectorParameter.editMethodDefinition 	= new MethodDefinition<DocumentsActivity> (DocumentsActivity.class,"EditDocument");
		SelectorUtilities.selectorParameter.customLegend 			= theContext.getString (R.string.add);
		SelectorUtilities.selectorParameter.classToRun 				= DocumentsActivity.class;
		SelectorUtilities.selectorParameter.swipeMethodDefinition	= new MethodDefinition<DocumentsActivity> (DocumentsActivity.class,"SwipeAction");
		SelectorUtilities.selectorParameter.type 					= StaticData.OBJECT_DOCUMENTS;
		// ----------------------------------------------------------------------
		SelectorUtilities.StartSelector (theContext,
									     new MethodDefinition<DocumentsActivity> (DocumentsActivity.class,"SelectAction"),
									     StaticData.OBJECT_DOCUMENTS);
		// -------------------------------------------------------------------------
	
	}
	// =============================================================================
	static void registerADocument (Context theContext,int theDocument)
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
    public static void SelectAction (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 18/10/2016 ECU called to display the selected file
    	// -------------------------------------------------------------------------
    	Utilities.displayDocument (context,PublicData.storedData.documents.get(thePosition).path);
    	// -------------------------------------------------------------------------
    	// 18/10/2016 ECU just finish this activity
    	// -------------------------------------------------------------------------
    	activity.finish ();
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
 	public static void SelectedDocument (String theFileName)
 	{
 		// -------------------------------------------------------------------------
 		// 18/10/2016 ECU copy the filename to the relevant field
 		// -------------------------------------------------------------------------
 		documentPath.setText (theFileName);  	
 		// -------------------------------------------------------------------------
 	}
	// =============================================================================
    public static void SwipeAction (int thePosition)
    {
    	// -------------------------------------------------------------------------
		// 18/10/2016 ECU created to confirm the deletion
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (Selector.context,"Item Deletion",
				   				 String.format (Selector.context.getString (R.string.delete_confirmation_format),PublicData.storedData.documents.get (thePosition).title),
				   				 (Object) thePosition,
				   				 Utilities.createAMethod (DocumentsActivity.class,"YesMethod",(Object) null),
				   				 null); 
		// -------------------------------------------------------------------------  
    }
	// =============================================================================
  	public static void YesMethod (Object theSelection)
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
    	// 18/10/2016 ECU now rebuild the display
    	// -------------------------------------------------------------------------
    	Selector.Rebuild ();
    	// -------------------------------------------------------------------------
  	}
    // ============================================================================= 
}

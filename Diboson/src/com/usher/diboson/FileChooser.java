package com.usher.diboson;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import android.content.Context;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class FileChooser extends ListActivity 
{
	// =============================================================================
	// 05/12/2013 ECU created
	// 01/01/2014 ECU added the flag 'displayImage' to decide whether the FileArrayAdapter
	//                should display the image of a graphics file or just the file
	//                icon. Changing the flag is achieved using the menu option
	// 17/03/2015 ECU added the handling of the PARAMETER_IMMEDIATE flag
	// 29/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 31/10/2015 ECU put in the option to read a file
	// 18/12/2015 ECU general tidy up
	// 23/05/2017 ECU changed to allow multiple extensions to be specified in the
	//                extensions
	// 06/01/2020 ECU changed MENU_READ_FILE to .._PROCESS_..
	//            ECU provide the option, via the PARAMETER_MENU, to either display,
	//                or not, the menu option
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	//==============================================================================
	/* ============================================================================= */
	//private static final String TAG					  = "FileChooser";
	/* ============================================================================= */
	private static final int MENU_DISPLAY_IMAGE     = 1;
	private static final int MENU_PROCESS_FILE		= 2;	// 31/10/2015 ECU added
	/* ============================================================================= */

	/* ============================================================================= */
	private FileArrayAdapter 	adapter;
	private Context				context;							// 08/01/2020 ECU added
	private File			 	currentDir;
	private	boolean	 			displayImage 		= false;		// 01/01/2014 ECU added
	private String []		 	extensionWanted 	= null;			// 23/05/2017 ECU changed from String
	private String			 	folder;
	private boolean			 	immediateResponse 	= false;		// 17/03/2015 ECU added
	private boolean   		 	processTheFile 		= false;		// 31/10/2015 ECU added
	private boolean   		 	processMenu 		= false;		// 06/01/2020 ECU added
	private	Method				selectMethod		= null;			// 17/12/2015 ECU added
	/* ============================================================================= */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 08/01/2020 ECU save the context for future use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 06/12/2013 ECU default the root directory to the project's folder
			// ---------------------------------------------------------------------
			folder = PublicData.projectFolder;
			// ---------------------------------------------------------------------
			// 09/12/2013 ECU check if any parameters have been passed through
			// ---------------------------------------------------------------------
			Bundle extras = getIntent().getExtras();
 		
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 06/12/2013 ECU see if a new 'starting' folder has been specified
				// -----------------------------------------------------------------
				if (extras.getString(StaticData.PARAMETER_FOLDER) != null)
				{
					folder = extras.getString(StaticData.PARAMETER_FOLDER);
				} 
				// -----------------------------------------------------------------
				// 06/12/2013 ECU get the filter which will be used for file selection.
				//                At the moment it will filter on the file's extension
				// -----------------------------------------------------------------
				Object extensionWantedObject = extras.getSerializable (StaticData.PARAMETER_FILTER);
				// -----------------------------------------------------------------
				// 23/05/2017 ECU check if an object was obtained
				// ------------------------------------------------------------------
				if (extensionWantedObject != null)
				{
					// --------------------------------------------------------------
					// 11/04/2018 ECU Note - check if a single 'extension' has been 
					//                       specified
					// --------------------------------------------------------------
					if (extensionWantedObject instanceof String) 
					{
						extensionWanted 	= new String [1];
						extensionWanted [0] = (String) extensionWantedObject;
					}
					// -------------------------------------------------------------
					// 11/04/2018 ECU Note - check if a number of 'extensions' have
					//                       been specified
					// -------------------------------------------------------------
					else
					if (extensionWantedObject instanceof String [])
					{
						extensionWanted = (String []) extensionWantedObject;
					}
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 17/03/2015 ECU handle the immediate flag
				// 18/12/2015 ECU changed the logic because the parameter is now passed
				//                as a boolean
				// -----------------------------------------------------------------
				immediateResponse = extras.getBoolean (StaticData.PARAMETER_IMMEDIATE,false);
				// -----------------------------------------------------------------
				// 17/12/2015 ECU try and get the definition for the select method
				// ------------------------------------------------------------------
				MethodDefinition <?> selectMethodDefinition 
					= (MethodDefinition<?>) extras.getSerializable (StaticData.PARAMETER_SELECT);
 	   			// -----------------------------------------------------------------
				// 17/12/2015 ECU if the definition is supplied that create a
				//                method which has a string argument
				// 11/04/2018 ECU changed to use BLANK...
				// -----------------------------------------------------------------
 	   			if (selectMethodDefinition != null)
 	   				selectMethod	= selectMethodDefinition.ReturnMethod (StaticData.BLANK_STRING);
 	   			else
 	   				selectMethod	= null;
				// -----------------------------------------------------------------
 	   			// 23/06/2017 ECU check if display parameter has been specified
 	   			//------------------------------------------------------------------
 	   			displayImage = extras.getBoolean (StaticData.PARAMETER_DISPLAY,false);
 	   			// -----------------------------------------------------------------
 	   			// 06/01/2020 ECU check if the menus are to be displayed
 	   			// -----------------------------------------------------------------
 	   			processMenu  = extras.getBoolean (StaticData.PARAMETER_MENU,false);
 	   			// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 09/12/2013 ECU now build the screen started at the specified folder
			// ---------------------------------------------------------------------
			currentDir = new File (folder);
			// ---------------------------------------------------------------------
			listTheFiles (currentDir,extensionWanted);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 29/10/2015 ECU the activity has been recreated after having been
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
		// -------------------------------------------------------------------------
		// 01/01/2014 ECU added
		//            ECU change the legend displayed
		// 06/01/2020 ECU changed legend from read to process
		//            ECU check if options are to be shown
		// -------------------------------------------------------------------------
		if (processMenu)
		{
			menu.add (0,MENU_DISPLAY_IMAGE,0,displayImage ? R.string.menu_display_icon
													      : R.string.menu_display_image);
			menu.add (0,MENU_PROCESS_FILE,0,processTheFile   ? R.string.menu_process_file_off
													         : R.string.menu_process_file_on);
		}
		// -------------------------------------------------------------------------
		return true;
	}
	// =============================================================================
	@Override
	public void onDestroy()
	{
		// -------------------------------------------------------------------------
		// 17/12/2015 ECU added
		// -------------------------------------------------------------------------
		// 17/12/2015 ECU reset the select method - belt and braces
		// -------------------------------------------------------------------------
		selectMethod = null;
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
	}
    /* ============================================================================= */ 
    @Override
	protected void onListItemClick (ListView theListView, View theView, int thePosition, long theId) 
    {
    	// -------------------------------------------------------------------------
    	// 09/12/2013 ECU created to be call when an item in the list is clicked
    	//                if it is a directory then display the files in it
    	// -------------------------------------------------------------------------
		super.onListItemClick (theListView,theView, thePosition, theId);
		// -------------------------------------------------------------------------
		FileOptions fileOptions = adapter.getItem (thePosition);
		// -------------------------------------------------------------------------
		// 09/12/2013 ECU if the path exists and it points to a directory or the
		//                parent then want to display its contents
		// -------------------------------------------------------------------------
		if ((fileOptions.isDirectory() || fileOptions.isParent()) 
					&& fileOptions.getPath() != null)
		{
			// ---------------------------------------------------------------------
			// 05/12/2013 ECU reset the current directory to this one
			// ---------------------------------------------------------------------
			currentDir = new File (fileOptions.getPath());
			// ---------------------------------------------------------------------
			// 05/12/2013 ECU build up a new file list based on the current directory
			// ---------------------------------------------------------------------
			listTheFiles (currentDir,extensionWanted);	
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 05/12/2013 ECU user has clicked on a file so handle it
			// ---------------------------------------------------------------------
			handleFileClick (fileOptions);
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
    public boolean onOptionsItemSelected (MenuItem item)
	{
    	// -------------------------------------------------------------------------
		// 16/06/2013 ECU take the actions depending on which menu is selected
		// -------------------------------------------------------------------------
		switch (item.getItemId())
		{
			// =====================================================================
			case MENU_DISPLAY_IMAGE:
				// -----------------------------------------------------------------
				// 01/01/2014 ECU toggle the display state
				// -----------------------------------------------------------------
				displayImage = !displayImage;
				// -----------------------------------------------------------------
				// 15/12/2015 ECU set the adapter's image flag
				// -----------------------------------------------------------------
				adapter.setShowImageFlag (displayImage);
				// -----------------------------------------------------------------
				break;
			// =====================================================================
			case MENU_PROCESS_FILE:
				// -----------------------------------------------------------------
				// 31/10/2015 ECU toggle the 'read file' flag
				// 06/01/2020 ECU changed to process from read
				// -----------------------------------------------------------------
				processTheFile = !processTheFile;
				// -----------------------------------------------------------------
				break;
			// =====================================================================
		}
		return true;
	}
	// =============================================================================
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) 
	{
		// -------------------------------------------------------------------------
		// 28/05/2013 ECU clear the displayed menu
		// -------------------------------------------------------------------------
		menu.clear ();
		// -------------------------------------------------------------------------	
		// 01/01/2014 ECU use the method to build menu
		// 06/01/2020 ECU check if the options are to be displayed
		// -------------------------------------------------------------------------
		if (processMenu)
		{
			onCreateOptionsMenu (menu);
		}
		// -------------------------------------------------------------------------	
		return true;
	}
	// =============================================================================
	FileFilter filter = new FileFilter ()
	{
		// -------------------------------------------------------------------------
		@Override
		public boolean accept (File thePathName) 
		{
			// ---------------------------------------------------------------------
			// 09/12/2013 ECU created to handle the filtering - if the path is of
			//                a directory then always include. If a file then 
			//                only include if it has the required extension
			// 23/05/2017 ECU changed the extension to be 'String []' from 'String'
			// ---------------------------------------------------------------------
			if (thePathName.isDirectory())
			{
				return true;
			}
			else
			{
				// -----------------------------------------------------------------
				// 10/11/2014 ECU added the Locale to the method call
				//            ECU changed to use Locale.getDefault instead of Locale.UK
				// 23/05/2017 ECU loop for all of the extensions that have been 
				//                supplied
				// -----------------------------------------------------------------
				for (int extension = 0; extension < extensionWanted.length; extension ++)
				{
					if (thePathName.getName().toLowerCase (Locale.getDefault()).endsWith (extensionWanted [extension]))
					{
						// ---------------------------------------------------------
						// 23/05/2017 ECU the extension matches so indicate this fact
						// ---------------------------------------------------------
						return true;
						// ---------------------------------------------------------
					}
				}
				// -----------------------------------------------------------------
				// 23/05/2017 ECU none of the extensions matched so indicate no match
				// -----------------------------------------------------------------
				return false;
				// -----------------------------------------------------------------
			}
		}
		// --------------------------------------------------------------------------
	};
	// =============================================================================
	private void actionTheFile (Context theContext, String theFileName)
	{
		// -------------------------------------------------------------------------
		// 08/01/2020 ECU created to action the specified file
		// -------------------------------------------------------------------------
		// 31/10/2015 ECU try and speak the selected file - only allow files
		//                with EXTENSION_TEXT)
		// -------------------------------------------------------------------------
		if (theFileName.toLowerCase(Locale.getDefault()).endsWith (StaticData.EXTENSION_TEXT))
		{
			// ---------------------------------------------------------------------
			// 31/10/2015 ECU speak out the contents and indicate that want
			//                to use the TTS service (the 'true' flag) if
			//                it is running
			// ---------------------------------------------------------------------
			Utilities.readAFile (theFileName,null,true);
			// ---------------------------------------------------------------------
		}
		else
		// -------------------------------------------------------------------------
		// 06/01/2020 ECU if the selected file is a 'video' then play it
		// -------------------------------------------------------------------------
		if (theFileName.toLowerCase(Locale.getDefault()).endsWith (StaticData.EXTENSION_VIDEO))
		{
			//----------------------------------------------------------------------
			// 06/01/2020 ECU if a video then play it
			// ---------------------------------------------------------------------
			Intent localIntent = new Intent (theContext,VideoViewer.class);
			localIntent.putExtra (StaticData.PARAMETER_FILE_NAME,theFileName);
			localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
			theContext.startActivity (localIntent);
			// ---------------------------------------------------------------------
		}
		else
		// -------------------------------------------------------------------------
		// 06/01/2020 ECU if the selected file is a 'music' then play it
		// 08/01/2020 ECU added the AUDIO type
		// -------------------------------------------------------------------------
		if ((theFileName.toLowerCase(Locale.getDefault()).endsWith (StaticData.EXTENSION_MUSIC)) ||
			(theFileName.toLowerCase(Locale.getDefault()).endsWith (StaticData.EXTENSION_AUDIO)))
		{
			//----------------------------------------------------------------------
			// 06/01/2020 ECU if a music file then play it
			// ---------------------------------------------------------------------
			Utilities.PlayAFile (theContext,theFileName);
			// ---------------------------------------------------------------------
		}
		else
		// -------------------------------------------------------------------------
		// 07/01/2020 ECU if the selected file is a 'pdf document' then
		//                display it
		// -------------------------------------------------------------------------
		if (theFileName.toLowerCase(Locale.getDefault()).endsWith (StaticData.EXTENSION_DOCUMENT))
		{
			//----------------------------------------------------------------------
			// 07/01/2020 ECU if a document file then display it
			// ---------------------------------------------------------------------
			Utilities.displayDocument (theContext,theFileName);
			// ---------------------------------------------------------------------
		}
		else
		// -------------------------------------------------------------------------
		// 07/01/2020 ECU Note - have clicked on a file which does not have
		//                       an extension that was processed above
		// -------------------------------------------------------------------------
		{
			// ---------------------------------------------------------------------
			// 06/01/2020 ECU changed to use the resource
			// ---------------------------------------------------------------------
			Utilities.popToast (getString (R.string.file_cannot_be_processed),true);
			// ---------------------------------------------------------------------
		}
	}
    // =============================================================================
    private void handleFileClick (FileOptions fileOptions)
    {
    	// -------------------------------------------------------------------------
    	// 05/12/2013 ECU indicate which file was selected
    	// 10/11/2014 ECU added the Locale to the method call
    	//      	  ECU changed to use Locale.getDefault instead of Locale.UK
    	// 31/10/2015 ECU changed to use EXTENSION....
    	// 17/12/2015 ECU check if the selectMethod has been specified
    	// 18/12/2015 ECU changed name from 'onFileClick' which implied it was a
    	//                public method of ListActivity
    	// -------------------------------------------------------------------------
    	if (selectMethod == null)
    	{
    		// ---------------------------------------------------------------------
    		// 17/12/2015 ECU no select method defined so normal use
    		// 18/12/2015 ECU Note - display information about the file - if it is
    		//                       of an image then display a thumbnail of it
    		// ---------------------------------------------------------------------
    		if (fileOptions.getPath().toLowerCase(Locale.getDefault()).endsWith (StaticData.EXTENSION_PHOTOGRAPH))
    		{
    			Utilities.popToast ("Image File Selected\n" + fileOptions.Print(),true,Toast.LENGTH_LONG,fileOptions.getPath());
    		}
    		else
    		{
    		    // -----------------------------------------------------------------
    			Utilities.popToast ("File Selected\n" + fileOptions.Print(),true);
    			// -----------------------------------------------------------------
    			// 31/10/2015 ECU check if the contents of the file is to be spoken
    			// 06/01/2020 ECU changed to process from read
    			// -----------------------------------------------------------------
    			if (processTheFile)
    			{
    				// -------------------------------------------------------------
    				// 08/01/2020 ECU changed to use the new method to process the
    				//                selected file
    				// -------------------------------------------------------------
    				actionTheFile (context,fileOptions.getFullFileName());
    				// -------------------------------------------------------------
    			}
    			// -----------------------------------------------------------------
    		}
    		// ---------------------------------------------------------------------
    		// 06/12/2013 ECU pass back the file name and full path
    		// ---------------------------------------------------------------------
    		Intent resultData = new Intent ();
    		resultData.putExtra (StaticData.PARAMETER_FILE_PATH,fileOptions.getPath());
    		resultData.putExtra (StaticData.PARAMETER_FILE_NAME,fileOptions.getName());
    		setResult (RESULT_OK,resultData);	
    		// ---------------------------------------------------------------------
    		// 17/03/2015 ECU check if clicking on the file causes the activity to
    		//                finish and hence pass back the result immediately
    		// ---------------------------------------------------------------------
    		if (immediateResponse)
    		{
    			// -----------------------------------------------------------------
    			// 17/03/2015 ECU terminate this activity
    			// -----------------------------------------------------------------
    			finish ();
    			// -----------------------------------------------------------------
    		}
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 17/12/2015 ECU a select method has been defined so process it
    		//                and then finish this activity
    		// ---------------------------------------------------------------------
    		try 
			{ 
				// -------------------------------------------------------------
				// 16/03/2015 ECU call up the method that will handle the 
				//                input text
    			// 22/03/2018 ECU note that by specifying 'null' then this means
    			//                that the method being called is static
    			// 28/06/2019 ECU changed from 'selectMethod.invoke' to accommodate calls
    			//                to a method which may or may not be static
				// -------------------------------------------------------------
    			Utilities.invokeMethod (selectMethod,new Object [] {fileOptions.getFullFileName()});
				// -------------------------------------------------------------
			} 
			catch (Exception theException) 
			{	
			} 
    		// ---------------------------------------------------------------------
    		// 17/12/2015 ECU just finish this activity
    		// ---------------------------------------------------------------------
    		finish ();
    		// ---------------------------------------------------------------------
    	}
    }
    // =============================================================================
    private void listTheFiles (File theFileName,final String [] theExtensionWanted)
    {
    	// -------------------------------------------------------------------------
    	// 05/12/2015 ECU created to build a list of files that are in the directory
    	//                whose path is passed as the argument
    	// 18/12/2015 ECU changed the name from 'fill'
    	// 23/05/2017 ECU changed theExtensionWanted from 'String'
    	// 07/10/2017 ECU check if the specified directory is 'readable'
    	// -------------------------------------------------------------------------
    	// 05/12/2013 ECU get list of files in the specified directory
    	// -------------------------------------------------------------------------
    	File [] fileList;
    	// -------------------------------------------------------------------------
    	// 05/12/2013 ECU set up the list of files and directories
    	// 18/12/2015 ECU note - want to display directories first then files. The
    	//                first entry will be the parent of the specified directory,
    	//                if it exists
    	// -------------------------------------------------------------------------
    	List<FileOptions> directories 	= new ArrayList<FileOptions>();
    	List<FileOptions> files 		= new ArrayList<FileOptions>();
    	// -------------------------------------------------------------------------
    	// 07/10/2017 ECU check if the directory is 'readable'
    	// -------------------------------------------------------------------------
    	if (theFileName.canRead())
    	{
    		// ---------------------------------------------------------------------
    		// 09/12/2013 ECU check if any filtering is required
    		// ---------------------------------------------------------------------
    		if (theExtensionWanted == null)
    		{
    			fileList = theFileName.listFiles ();
    		}
    		else
    		{
    			fileList = theFileName.listFiles (filter); 
    		}
    		// ---------------------------------------------------------------------
    		// 05/12/2013 ECU build up the lists
    		// ---------------------------------------------------------------------
    		try
    		{
    			// -----------------------------------------------------------------
    			// 05/12/2013 ECU loop for all files in the (un)filtered list
    			// -----------------------------------------------------------------
    			for (File fileIndex : fileList)
    			{
    				if (fileIndex.isDirectory())
    				{
    					// ---------------------------------------------------------
    					// 05/12/2013 ECU file name is of directory so add to that list
    					// ---------------------------------------------------------
    					directories.add (new FileOptions(fileIndex));
    					// ---------------------------------------------------------
    				}
    				else
    				{
    					// ---------------------------------------------------------
    					// 05/12/2013 ECU file name is of a file so add to that list
    					// ---------------------------------------------------------
    					files.add (new FileOptions(fileIndex));
    					// ---------------------------------------------------------
    				}
    			}
    		}
    		catch(Exception theException)
    		{			 
    		}
    		// ---------------------------------------------------------------------
    		// 05/12/2013 ECU sort the lists into ascending order
    		// ---------------------------------------------------------------------
    		Collections.sort (directories);
    		Collections.sort (files);
    		// ---------------------------------------------------------------------
    		// 05/12/2013 ECU add the lists together
    		// ---------------------------------------------------------------------
    		directories.addAll (files);
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    	// 09/12/2013 ECU check if there is a parent.
    	// 09/10/2017 ECU Note - the 'true' indicates that it is the parent
    	// -------------------------------------------------------------------------
    	if (theFileName.getParent() != null)
			 directories.add (0,new FileOptions (new File (theFileName.getParent()),true));
    	// -------------------------------------------------------------------------
    	// 09/12/2014 ECU set up the adapter to display the files
    	// 15/12/2015 ECU added displayImage as argument
    	// 17/12/2015 ECU pass the list view as 'null' so that no scroll listener
    	//                is set
    	//            ECU changed from file_view to file_list_view
    	// 07/10/2017 ECU can only adjust the adapter if there is something to
    	//                display
    	// -------------------------------------------------------------------------
    	if (directories.size() > 0)
    	{
    		// ---------------------------------------------------------------------
        	// 05/12/2013 ECU set the current file name in the title
    		// 09/10/2017 ECU changed to just show the path not a 'Current Directory'
    		//                introduction
        	// ---------------------------------------------------------------------
        	this.setTitle (theFileName.getPath());
        	// ---------------------------------------------------------------------
        	// 08/01/2020 ECU added process menu
        	//            ECU changed to pass through 'context' with the method
        	//                creation
    		// ---------------------------------------------------------------------
    		adapter = new FileArrayAdapter (FileChooser.this,
    										null,
    										R.layout.file_list_view,directories,displayImage,
    										processMenu ? Utilities.createAMethod (FileChooser.class,"ActionMethod",context,StaticData.BLANK_STRING)
    										            : null);
    		// ---------------------------------------------------------------------
    		// 09/12/2013 ECU display the files via the adapter
    		// ---------------------------------------------------------------------
    		this.setListAdapter (adapter);
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    }
    // =============================================================================

    // =============================================================================
    public void ActionMethod (Context theContext,String theFileName)
	{
		// -------------------------------------------------------------------------
		// 08/01/2020 ECU action the file - NOT happy to use MainActivity.....
		//                need to investigate
		//            ECU resolved the issue above by including 'context' with
		//                the createMethod used when creating the FileArrayAdapter
		//                and adding this as the first argument to this method
		// -------------------------------------------------------------------------
		actionTheFile (theContext,theFileName);
		// -------------------------------------------------------------------------
	}
    // =============================================================================
}
package com.usher.diboson;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// =================================================================================
// WebUtilities
// ============
// This class provides facilities whereby this device can be accessed externally
// using an internet browser.
//
// The device has an 'htdocs' directory which is searched first for the requested
// page. If it cannot be found then a default page that is stored in the project's
// raw directory will be displayed.
// =================================================================================

// =================================================================================
public class WebUtilities 
{
	// =============================================================================
	// 09/03/2017 ECU added the handling of the DYNAMIC_COPYRIGHT
	// 10/03/2017 ECU changed to use information stored in 'raw' resource directory
	//                because this means that the app is 'complete' and is not
	//                dependent on files being transferred to the htdocs directory
	// =============================================================================
	
	// ----------------------------------------------------------------------------- 
	//static final String TAG = "WebUtilities";
	// -----------------------------------------------------------------------------
	static final String COMMAND_EXTENSION 	= ".command";
	static final int	COMMAND_OFFSET		= 100;
	static final String DYNAMIC_COPYRIGHT	= "DYNAMIC_COPYRIGHT";
	static final String DYNAMIC_IP_ADDRESS	= "DYNAMIC_IP_ADDRESS";
	static final String HOME_PAGE 			= "/home_page.html";
	static final String HTML_EXTENSION 		= ".html";
	static final String REFRESH				= "/refresh.html";
	static final String	SOCKET_NUMBER		= "SOCKET_NUMBER";
	// -----------------------------------------------------------------------------
	// 24/12/2014 ECU Created to handle all aspects of the web browser access
	// -----------------------------------------------------------------------------
	
	// =============================================================================
	public static String commandList ()
	{
		// -------------------------------------------------------------------------
		// 15/01/2015 ECU declare the array which will be used for sorting
		// ------------------------------------------------------------------------
		List<CommandListItem>	commandsList = SortedCommands ();
		// -------------------------------------------------------------------------
		// 31/12/2014 ECU this method will generate the HTML commands that will
		//                enable activities to be started from a device on the
		//                public network
		// 15/01/2014 ECU change to use the sorted ArrayList
		// 30/07/2017 ECU changed to use HTML_...
		// -------------------------------------------------------------------------
		String commandString = "<p><font face=\"Arial\" >";
		for (int theIndex = 0; theIndex < commandsList.size(); theIndex++)
		{
			// ---------------------------------------------------------------------
			commandString += "<a href=\"http://" + PublicData.storedData.dynamicIPAddress + ":" + 
					PublicData.socketNumberForWeb + "/" + (COMMAND_OFFSET + 
					commandsList.get (theIndex).number) + COMMAND_EXTENSION + "\">" +
					commandsList.get (theIndex).legend + "</a>" + StaticData.HTML_BREAK;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 20/10/2019 ECU Note - return the generated command list
		// -------------------------------------------------------------------------
		return commandString + "</font></p>";
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String commandList (String theLineTerminator)
	{
		// -------------------------------------------------------------------------
		// 22/10/2019 ECU create a 'command string' that can be used by the telnet
		//                server
		//            ECU get the sorted list of commands
		// ------------------------------------------------------------------------
		List<CommandListItem>	commandsList = SortedCommands ();
		// -------------------------------------------------------------------------
		// 22/10/2019 ECU now loop through the commands building up a summary that
		//				  can be returned
		// -------------------------------------------------------------------------
		String commandString = StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < commandsList.size(); theIndex++)
		{
			// ---------------------------------------------------------------------
			commandString += String.format("%3d ",commandsList.get(theIndex).number) + 
							commandsList.get (theIndex).legend + theLineTerminator;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 22/10/2019 ECUreturn the generated command list
		// -------------------------------------------------------------------------
		return commandString;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String getPage (Context theContext,String thePage)
	{
		// -------------------------------------------------------------------------
		// 30/07/2017 ECU changed to use HTML_PREF....
		// -------------------------------------------------------------------------
		int		commandNumber;
		String  commandString 	= null;
		String	returnString	= StaticData.HTML_PREFORMATTED_START;
		// -------------------------------------------------------------------------
		
		if (thePage.endsWith (HTML_EXTENSION))
		{
			// ---------------------------------------------------------------------
			// 29/12/2014 ECU please note that 'thePage' will have a leading '/' so 
			//                therefore 'webFolder' does not need a trailing '/'
			// 10/03/2017 ECU readAPage now does any error handling so can just
			//                return with what it returns
			// ---------------------------------------------------------------------
			return readAPage (theContext,PublicData.webFolder + thePage,R.raw.html_home_page);
			// ---------------------------------------------------------------------
		}
		else
		if (thePage.endsWith (COMMAND_EXTENSION))
		{
			// ---------------------------------------------------------------------
			// 30/12/2014 ECU the incoming command string is of the form
			//                /<number as string>.command e.g. '/1.command'
			// ---------------------------------------------------------------------
			try 
			{
				// -----------------------------------------------------------------
				commandString =	thePage.substring (1).replaceFirst (COMMAND_EXTENSION,StaticData.BLANK_STRING);
				commandNumber = Integer.parseInt (commandString);
				// -----------------------------------------------------------------
			} 
			catch (NumberFormatException nfe) 
			{
				// -----------------------------------------------------------------
				// 30/12/2014 ECU the entered number is of invalid format
				// -----------------------------------------------------------------
				commandNumber = StaticData.NO_RESULT;
				// -----------------------------------------------------------------
			} 	
			// ---------------------------------------------------------------------
			// 30/12/2014 ECU process the request command
			// ---------------------------------------------------------------------
			switch (commandNumber)
			{
				// -----------------------------------------------------------------
				// 06/01/2020 ECU changed to use resource
				// -----------------------------------------------------------------
				case StaticData.NO_RESULT:
					returnString += String.format (theContext.getString (R.string.webutilities_command_invalid_format),commandString);
					break;
				// -----------------------------------------------------------------
				case 1:
					// -------------------------------------------------------------
					// 14/09/2020 ECU changed so that the facility to refresh the
					//                page is embedded
					// -------------------------------------------------------------
					returnString = readAPage (theContext,PublicData.webFolder + REFRESH,R.raw.html_refresh) +
							StaticData.HTML_PREFORMATTED_START +
							SystemInfoActivity.projectData (theContext);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case 2:
					// -------------------------------------------------------------
					// 10/03/2017 ECU changed to use 'raw' resource rather than
					//                a file in the directory
					// 30/07/2017 ECU changed to use HTML_PREF....
					// -------------------------------------------------------------
					returnString = readAPage (theContext,PublicData.webFolder + REFRESH,R.raw.html_refresh) + 
												StaticData.HTML_PREFORMATTED_START + 
												SystemInfoActivity.projectLogData (theContext);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case 3:
					// -------------------------------------------------------------
					// 31/12/2014 ECU display a list of the activities that can be 
					//                started
					// -------------------------------------------------------------
					returnString = commandList ();
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				default:
					// -------------------------------------------------------------
					// 30/12/2014 ECU if commands are >= COMMAND_OFFSET then the number
					//                (-100) will indicate which activity in
					//                GridActivity is to be actioned
					// -------------------------------------------------------------
					commandNumber -= COMMAND_OFFSET;
					// -------------------------------------------------------------
					// 31/12/2014 ECU check if the tablet is ready for a remote 
					//                device
					// -------------------------------------------------------------
					if (PublicData.gridActivityEntered)
					{
						// ---------------------------------------------------------
						// 30/12/2014 ECU check that the number indicates a valid activity
						// 06/01/2020 ECU changed to use the resource
						// 24/06/2020 ECU if the manifest has 'android:launchMode="singleTop"'
						//                for GridActivity then the selected activity will
						//                only be started if the user has already started
						//                an activity so that GridActivity is not
						//                'on top'
						// ---------------------------------------------------------
						if (commandNumber < GridActivity.gridImages.length)
						{
							// -----------------------------------------------------
							// 24/06/2020 ECU because of 'launch mode' issues then
							//                change to the way that a selected
							//                activity is started - was just lazy to
							//                start GridActivity again - if the launch
							//                mode was 'singleTop' then this would not
							//                work
							// -----------------------------------------------------
							//Intent localIntent = new Intent (theContext,GridActivity.class);
							//localIntent.putExtra (StaticData.PARAMETER_POSITION,commandNumber);
							//localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							//theContext.startActivity (localIntent);
							// -----------------------------------------------------
							// 24/06/2020 ECU instead of starting GridActivity, which
							//                is already running, just pass it a message
							//                to start the specified activity
							// 25/06/2020 ECU changed to use the new class
							// 07/09/2020 ECU start within a thread so that the server
							//                can continue
							//            ECU copy across the variable because cannot
							//                really make 'commandNumber' final because
							//                it is modified with COMMAND_OFFSET
							// -----------------------------------------------------
							final int commandNumberThread = commandNumber;
							Thread thread = new Thread ()
							{
								// -------------------------------------------------
								@Override
								public void run ()
								{
									// ---------------------------------------------
									// 07/09/2020 ECU start the activity as a thread
									// ---------------------------------------------
									Utilities.startASpecficActivity (commandNumberThread);
									// ---------------------------------------------
								}
								// -------------------------------------------------
							};
							// -----------------------------------------------------
							// 07/09/2020 ECU start up the thread
							// -----------------------------------------------------
							thread.start();
							// -----------------------------------------------------
							// 25/06/2020 ECU Note - generate the response string
							// 09/09/2020 ECU added the legend of the activity being
							//                started
							// -----------------------------------------------------
							returnString += String.format (theContext.getString (R.string.webutilities_activity_started_format),
													commandNumber,SortedCommands().get (commandNumber).legend);
							// -----------------------------------------------------
						}
						else
						{
							// -----------------------------------------------------
							// 30/12/2014 ECU entered command number is too big
							// 06/01/2020 ECU changed to use resource
							// -----------------------------------------------------
							returnString += String.format (theContext.getString(R.string.webutilities_no_activity_format),(commandNumber + COMMAND_OFFSET));
							// -----------------------------------------------------
						}
					}
					else
					{
						// ---------------------------------------------------------
						// 06/01/2020 ECU changed to use the resource
						// ---------------------------------------------------------
						returnString += theContext.getString (R.string.webutilities_device_not_ready);
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					break;
					// -------------------------------------------------------------			
			}
			// ---------------------------------------------------------------------
			// 31/12/2014 ECU add the hyperlink back to the 'home page'
			// 10/03/2017 ECU changed to use the 'raw' resource if the page cannot
			//                be found
			// 30/07/2017 ECU changed to use HTML_PREF....
			// ---------------------------------------------------------------------
			return returnString + StaticData.HTML_PREFORMATTED_END + readAPage (theContext,PublicData.webFolder + HOME_PAGE,R.raw.html_home_page_link);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 31/12/2014 ECU the requested URL does not have an extension which can be
		//                handled here
		// 20/10/2019 ECU change the message return to a resource
		// -------------------------------------------------------------------------
		return theContext.getString (R.string.web_extensions_handled);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static String readAPage (Context theContext,String thePage,int theResourceID)
	{
		// -------------------------------------------------------------------------
		// -------------------------------------------------------------------------
		// 20/10/2019 ECU Note - This method is used to read the file that is
		//                       specified in 'thePage'. If that file does not exist
		//                       on the device then 'theResourceID' will point to a
		//                       'raw resource' which can be used instead. The fact that
		//                       the original page cannot be found will be logged.
		// -------------------------------------------------------------------------
		// -------------------------------------------------------------------------
		String pageString = StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
		// 31/12/2014 ECU read the page from disk - if no such file exists then a
		//                'null' is returned
		// 10/03/2017 ECU changed so that if a page is not found then the 'resource ID'
		//                that is held in 'raw' resources is returned
		// 03/06/2019 ECU pass through the context
		// -------------------------------------------------------------------------
		byte [] pageDetails = Utilities.readAFile (theContext,thePage);
		// -------------------------------------------------------------------------
		if (pageDetails != null)
		{
			// ---------------------------------------------------------------------
			// 10/03/2017 ECU Note - the page exists so just generate the string
			//                       form what was read
			// ---------------------------------------------------------------------
			pageString 	= new String (pageDetails);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 10/03/2017 ECU the page does not exist in the directory so just
			//                return the 'resource ID' that is held as a 'raw resource'
			// ---------------------------------------------------------------------
			pageString = Utilities.readRawResource (theContext,theResourceID);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 10/03/2017 ECU make sure that some of the dynamic data in the page
		//                is updated
		// 16/11/2019 ECU changed from 'replaceAll' to 'replace' because
		//                the former requires a REGEX so not sure why it ever
		//				  worked
		// -------------------------------------------------------------------------
		pageString 	= pageString.replace (DYNAMIC_IP_ADDRESS, PublicData.storedData.dynamicIPAddress);
		pageString 	= pageString.replace (SOCKET_NUMBER,Integer.toString (PublicData.socketNumberForWeb));
		// -------------------------------------------------------------------------
		// 09/03/2017 ECU make sure the copyright is updated and that the copyright
		//                symbol is adjusted correctly
		// -------------------------------------------------------------------------
		pageString 	= pageString.replace (DYNAMIC_COPYRIGHT,PublicData.copyrightMessage.replace (StaticData.COPYRIGHT_CODE,StaticData.COPYRIGHT_CODE_HTML));
		// -------------------------------------------------------------------------
		// 10/03/2017 ECU return the updated string to the caller
		// -------------------------------------------------------------------------
		return pageString;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static List <CommandListItem> SortedCommands ()
	{
		// -------------------------------------------------------------------------
		// 15/01/2015 ECU declare the array which will be used for sorting
		// ------------------------------------------------------------------------
		List<CommandListItem>	commandsList = new ArrayList<CommandListItem> ();
		// -------------------------------------------------------------------------
		// 15/01/2015 ECU populate the ArrayList form the gridImages array that is
		//                defined in GridActivity
		// 16/01/2015 ECU changed to used gridItems which takes account of the
		//                development mode of the app
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < GridActivity.gridItems.size(); theIndex++)
		{
			commandsList.add (new CommandListItem (GridActivity.gridItems.get(theIndex).legend,theIndex));
		}
		// -------------------------------------------------------------------------
		// 15/01/2015 ECU now sort the ArrayList
		// -------------------------------------------------------------------------
		Collections.sort (commandsList);
		// -------------------------------------------------------------------------
		return commandsList;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

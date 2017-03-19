package com.usher.diboson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.Intent;

public class WebUtilities 
{
	// =============================================================================
	// 09/03/2017 ECU added the handling of the DYNAMIC_COPYRIGHT
	// 10/03/2017 ECU changed to use information stored in 'raw' resource directory
	//                because this means that the app is 'complete' and is not
	//                dependent on files being transferred to the htdocs directory
	// =============================================================================
	
	// ----------------------------------------------------------------------------- 
	static final String TAG = "WebUtilities";
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
		// -------------------------------------------------------------------------
		String commandString = "<p><font face=\"Arial\" >";
		for (int theIndex = 0; theIndex < commandsList.size(); theIndex++)
		{
			commandString += "<a href=\"http://" + PublicData.storedData.dynamicIPAddress + ":" + 
					PublicData.socketNumberForWeb + "/" + (COMMAND_OFFSET + 
					commandsList.get (theIndex).number) + COMMAND_EXTENSION + "\">" +
					commandsList.get (theIndex).legend + "</a><br>";
		}
		return commandString + "</font></p>";
	}
	// =============================================================================
	public static String getPage (Context theContext,String thePage)
	{
		// -------------------------------------------------------------------------
		int		commandNumber;
		String  commandString 	= null;
		String	returnString	= "<pre>";
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
				commandString =	thePage.substring(1).replaceFirst (COMMAND_EXTENSION,"");
				commandNumber = Integer.parseInt (commandString);
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
				case StaticData.NO_RESULT:
					returnString += "The entered command number '" + commandString + "' is invalid";
					break;
				// -----------------------------------------------------------------
				case 1:
					returnString += SystemInfoActivity.projectData (theContext);
					break;
				// -----------------------------------------------------------------
				case 2:
					// -------------------------------------------------------------
					// 10/03/2017 ECU changed to use 'raw' resource rather than
					//                a file in the directory
					// -------------------------------------------------------------
					returnString = readAPage (theContext,PublicData.webFolder + REFRESH,R.raw.html_refresh) + "<pre>" + 
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
						// ---------------------------------------------------------
						if (commandNumber < GridActivity.gridImages.length)
						{
							Intent localIntent = new Intent (theContext,GridActivity.class);
							localIntent.putExtra (StaticData.PARAMETER_POSITION,commandNumber);
							localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							theContext.startActivity (localIntent);
							returnString += "Activity '" + commandNumber + "' has been started";
						}
						else
						{
							// -----------------------------------------------------
							// 30/12/2014 ECU entered command number is too big
							// -----------------------------------------------------
							returnString += "No Activity corresponds to '" + (commandNumber + COMMAND_OFFSET) + "'";
							// ---------------------------------------------------------
						}
					}
					else
					{
						returnString += "The device is not ready yet for your command";
					}
					break;
					// -------------------------------------------------------------			
			}
			// ---------------------------------------------------------------------
			// 31/12/2014 ECU add the hyperlink back to the 'home page'
			// 10/03/2017 ECU changed to use the 'raw' resource if the page cannot
			//                be found
			// ---------------------------------------------------------------------
			return returnString + "</pre>" + readAPage (theContext,PublicData.webFolder + HOME_PAGE,R.raw.html_home_page_link);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 31/12/2014 ECU the requested URL does not have an extension which can be
		//                handled here
		// -------------------------------------------------------------------------
		return "invalid page type";
	}
	// =============================================================================
	static String readAPage (Context theContext,String thePage,int theResourceID)
	{
		// -------------------------------------------------------------------------
		String pageString = "";
		// -------------------------------------------------------------------------
		// 31/12/2014 ECU read the page from disk - if no such file exists then a
		//                'null' is returned
		// 10/03/2017 ECU changed so that if a page is not found then the 'resource ID'
		//                that is held in 'raw' resources is returned
		// -------------------------------------------------------------------------
		byte [] pageDetails = Utilities.readAFile (thePage);
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
		}
		// -------------------------------------------------------------------------
		// 10/03/2017 ECU make sure that some of the dynamic data in the page
		//                is updated
		// -------------------------------------------------------------------------
		pageString 	= pageString.replaceAll (DYNAMIC_IP_ADDRESS, PublicData.storedData.dynamicIPAddress);
		pageString 	= pageString.replaceAll (SOCKET_NUMBER,Integer.toString (PublicData.socketNumberForWeb));
		// -------------------------------------------------------------------------
		// 09/03/2017 ECU make sure the copyright is updated and that the copyright
		//                symbol is adjusted correctly
		// -------------------------------------------------------------------------
		pageString 	= pageString.replaceAll (DYNAMIC_COPYRIGHT,PublicData.copyrightMessage.replace (StaticData.COPYRIGHT_CODE,StaticData.COPYRIGHT_CODE_HTML));
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
		List<CommandListItem>	commandsList = new ArrayList<CommandListItem>();
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
	}
	// =============================================================================
}

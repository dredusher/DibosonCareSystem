package com.usher.diboson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

public class SchedulesDirect 
{
	// =============================================================================
	// 05/03/2017 ECU IMPORTANT issues with the encoding - see the notes in sendPOST
	//                =========
	// 06/03/2017 ECU the encoding issue was resolved - see the comments in sendPOST
	// =============================================================================
	
	// =============================================================================
	private final static String TAG			= "SchedulesDirect";
	// =============================================================================
	static final String	TOKEN_REQUEST		= "{\"username\":\"%s\", \"password\":\"%s\"}";
	static final String URL_LINEUP_FORMAT	= "lineups/%s";
	static final String URL_PROGRAMS		= "programs";
	static final String URL_SCHEDULES		= "schedules";
	static final String URL_STATUS			= "status";
	static final String URL_TOKEN			= "token";
	static final int	OBJECT_LINEUP    	= 0;
	static final int	OBJECT_PROGRAMS		= 1;
	static final int	OBJECT_SCHEDULES    = 2;
	static final int	OBJECT_STATUS    	= 3;
	static final int	OBJECT_TOKEN    	= 4;
	// =============================================================================
	
	// =============================================================================
	static int								numberOfProgramRequests;		// 22/07/2016 ECU added
	static StringBuffer 					programRequests;
	static List<SchedulesDirectProgram> 	programList	 = new ArrayList<SchedulesDirectProgram> ();
	static List<SchedulesDirectSchedule> 	scheduleList = new ArrayList<SchedulesDirectSchedule> ();
	static int          					TVChannelNumber;
	// =============================================================================
	
	// =============================================================================
	public static void buildTVChannelsList ()
	{
		// -------------------------------------------------------------------------
		// 22/07/2016 ECU changed to use a thread to do the work
		// -------------------------------------------------------------------------
	  	Thread thread = new Thread()
    	{
    		@Override
	        public void run()
	        {
	            try 
	            {
	            	// -------------------------------------------------------------
	        		// 21/07/2016 ECU obtain the contents of the line up from the 
	            	//            server - get a new token first 
	        		// -------------------------------------------------------------
	        		getToken ();
	        		getLineUp ();
	        		// -------------------------------------------------------------
	        		// 22/07/2016 ECU write the list of available channels to disk
	        		// -------------------------------------------------------------
	        		Utilities.writeObjectToDisk (PublicData.projectFolder + MainActivity.activity.getString (R.string.epg_channels_available_file),TVChannelsActivity.TVChannelsAvailable);
	        		// -------------------------------------------------------------
	        		// 22/07/2016 ECU show the token that has been sent
	        		// -------------------------------------------------------------
	        		TVChannelsActivity.sendMessage ("The list of channels has been built");
	        		// -------------------------------------------------------------
	        		// 12/02/2017 ECU make sure any buttons are updated
	        		// -------------------------------------------------------------
	        		TVChannelsActivity.buttonVisibilityUpdateRequest ();
	        		// -------------------------------------------------------------
	            }
	            catch(Exception theException)
	            { 	                 
	            }       
	        }
	    };
	    // -------------------------------------------------------------------------
	    // 22/07/2016 ECU start up the thread
	    // -------------------------------------------------------------------------
	    thread.start();
	}
	// =============================================================================
	static void createSchedules (int theTVChannelNumber)
	{
		// -------------------------------------------------------------------------
		// 21/07/2016 ECU created to merge the lists to generate the data that is
		//                to be viewed
		// -------------------------------------------------------------------------
		if (scheduleList.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 21/07/2016 ECU initialise the stored EPG entries
			// ---------------------------------------------------------------------
			TVChannelsActivity.TVChannelsSelected.get(theTVChannelNumber).EPGEntries 
				= new ArrayList<ArrayList<EPGEntry>> ();
			// ---------------------------------------------------------------------
			// 21/07/2016 ECU declare the EPG fields that will receive the data
			// ---------------------------------------------------------------------
			String [] epgFields = EPGEntry.emptyEPGArray();
			// ---------------------------------------------------------------------
			// 21/07/2016 ECU define the index when searching for programs
			// ---------------------------------------------------------------------
			int programIndex;
			// ---------------------------------------------------------------------
			// 21/07/2016 ECU declare variables used when checking dates
			// 04/08/2017 ECU changed to use BLANK....
			// ---------------------------------------------------------------------
			String	localDate			= StaticData.BLANK_STRING;
			int		localDateIndex  	= StaticData.NO_RESULT;
			// ---------------------------------------------------------------------
			// 21/07/2016 ECU loop through each stored schedule
			// 04/08/2017 ECU changed to use BLANK....
			// ---------------------------------------------------------------------
			for (int theSchedule = 0; theSchedule < scheduleList.size(); theSchedule++)
			{
				epgFields [StaticData.EPG_DATE] 		= scheduleList.get(theSchedule).date;
				epgFields [StaticData.EPG_START_TIME] 	= scheduleList.get(theSchedule).time;
				epgFields [StaticData.EPG_END_TIME] 	= scheduleList.get(theSchedule).endTime;
				epgFields [StaticData.EPG_DURATION]		= StaticData.BLANK_STRING + scheduleList.get(theSchedule).duration;
				// -----------------------------------------------------------------
				// 21/07/2016 ECU try and locate the associated program data
				// -----------------------------------------------------------------
				programIndex = findProgram (scheduleList.get(theSchedule).programID);
				if (programIndex != StaticData.NO_RESULT)
				{
					// -------------------------------------------------------------
					// 21/07/2016 ECU found a match so can complete some more details
					// -------------------------------------------------------------
					epgFields [StaticData.EPG_PROGRAM_TITLE]	= programList.get(programIndex).title;
					epgFields [StaticData.EPG_GENRE]			= programList.get(programIndex).genre;
					epgFields [StaticData.EPG_DESCRIPTION]		= programList.get(programIndex).description;
					// -------------------------------------------------------------
				}	
				// -----------------------------------------------------------------
				// 21/07/2016 ECU check if the date has altered
				// -----------------------------------------------------------------
				if (!epgFields[StaticData.EPG_DATE].equalsIgnoreCase (localDate))
				{
					// -------------------------------------------------------------
					// 21/07/2016 ECU the date has changed so generate new ArrayList
					// -------------------------------------------------------------
					TVChannelsActivity.TVChannelsSelected.get(theTVChannelNumber).EPGEntries.add (new ArrayList<EPGEntry>());
					// -------------------------------------------------------------
					// 21/07/2016 ECU store and increment the working variables
					// -------------------------------------------------------------
					localDate = epgFields [StaticData.EPG_DATE];
					localDateIndex++;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 25/06/2016 ECU add the entry into the list
				// -----------------------------------------------------------------
				TVChannelsActivity.TVChannelsSelected.get (theTVChannelNumber).EPGEntries.get (localDateIndex).add (new EPGEntry (epgFields));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 21/07/2016 ECU put status message on screen
			// ---------------------------------------------------------------------
			TVChannelsActivity.sendMessage ("Writing EPG data for " + TVChannelsActivity.TVChannelsSelected.get(theTVChannelNumber).channelName + " to disk");
			// ---------------------------------------------------------------------
			// 21/07/2016 ECU write the data to disk
			// 14/11/2017 ECU add 'true' to indicate async write
			// ---------------------------------------------------------------------
			TVChannelsActivity.TVChannelsSelected.get (theTVChannelNumber).writeToDisk (true);
			// ---------------------------------------------------------------------
		}	
	}
	// =============================================================================
    static int findProgram (String theProgramID)
    {
    	if (programList.size() > 0)
    	{
    		for (int theProgram = 0; theProgram < programList.size(); theProgram++)
    		{
    			if (programList.get(theProgram).ID.equalsIgnoreCase(theProgramID))
    				return theProgram;
    		}
    	}
    	return StaticData.NO_RESULT;
    }
	// =============================================================================
	@SuppressWarnings("unchecked")
	public static void generateEPG (final ArrayList <TVChannel> theChannels)
	{
		// -------------------------------------------------------------------------
		// 21/07/2016 ECU generate the EPG entries for the selected channels
		// 22/07/2016 ECU changed to use a thread
		// -------------------------------------------------------------------------
		Thread thread = new Thread()
    	{
    		@Override
	        public void run()
	        {
	            try 
	            {
	            	// -------------------------------------------------------------
	        		// 22/07/2016 ECU check if there are channels for which an EPG 
	            	//                is to be generated
	        		// -------------------------------------------------------------
	        		if (theChannels.size () > 0)
	        		{
	        			TVChannelsActivity.sendMessage ("Starting to generate the EPG");
	        			// ---------------------------------------------------------
	        			// 21/07/2016 ECU always get a fresh token
	        			// 24/07/2016 ECU check whether token was obtained
	        			// ---------------------------------------------------------
	        			if (getToken ())
	        			{
	        				// -----------------------------------------------------
	        				// 24/07/2016 ECU a token was obtained OK so can
	        				//                continue
	        				// -----------------------------------------------------
	        				// 21/07/2016 ECU declare a buffer that will contain the id's
	        				//	              of programs that the schedules says are needed
	        				// -----------------------------------------------------
	        				StringBuffer request;
	        				// -----------------------------------------------------
	        				// 22/07/2016 ECU load up the programs list
	        				// -----------------------------------------------------
	        				TVChannelsActivity.sendMessage ("Loading programs list from disk");
	        				programList = (List<SchedulesDirectProgram>) Utilities.readObjectFromDisk (PublicData.storedData.schedulesDirectData.folder + 
	        									MainActivity.activity.getString (R.string.programs_list));
	        				// -----------------------------------------------------
	        				// 22/07/2016 ECu check if there is no data on disk
	        				// -----------------------------------------------------
	        				if (programList == null)
	        				{
	        					// -------------------------------------------------
	        					// 22/07/2016 ECU nothing on disk so create a new list
	        					// -------------------------------------------------
	        					TVChannelsActivity.sendMessage ("Creating a new program list");
	        					programList = new ArrayList<SchedulesDirectProgram> ();
	        				}
	        				// -----------------------------------------------------
							// 12/02/2017 ECU display a separator
							// -----------------------------------------------------
	        				TVChannelsActivity.sendMessage (MainActivity.activity.getString (R.string.separating_line));
	        				// -----------------------------------------------------
	        				// 21/07/2016 ECU loop for all channels
	        				// -----------------------------------------------------
	        				for (TVChannelNumber = 0; TVChannelNumber < theChannels.size (); TVChannelNumber++)
	        				{
	        					TVChannelsActivity.sendMessage ("Generating for " + theChannels.get(TVChannelNumber).channelName);
	        					// -------------------------------------------------
	        					// 21/07/2016 ECU reset the working lists
	        					// -------------------------------------------------
	        					scheduleList = new ArrayList<SchedulesDirectSchedule> ();
	        					request = new StringBuffer ();
	        					// -------------------------------------------------
	        					// 22/07/2016 ECU use the method to return the dates that are to be
	        					//                retrieved
	        					// -------------------------------------------------			
	        					request.append ("[\n{\"stationID\":\"" + 
	        										theChannels.get(TVChannelNumber).channelID + 
	        										"\",\n\"date\":[" + 
	        										PublicData.storedData.schedulesDirectData.returnDates() + 
	        									"]}]");
	        					// -------------------------------------------------
	        					TVChannelsActivity.sendMessage ("Requesting schedules from server");
	        					// -------------------------------------------------
	        					String response = sendPOST (URL_SCHEDULES,request.toString()); 
	        					writeToFile (URL_SCHEDULES,response);
	        					// -------------------------------------------------
	        					TVChannelsActivity.sendMessage ("Analysing schedules data returned by server");
	        					// -------------------------------------------------
	        					parseBuffer (OBJECT_SCHEDULES,response);
	        					// -------------------------------------------------
	        					// 22/07/2016 ECU at this point the schedules have been 
	        					//                stored in its list - programRequest will
	        					//                contain a list, if any, of program 
	        					//                information that needs to be obtained.  
	        					//            ECU numberOfProgramRequests says how many 
	        					//                programs needing to be requested
	        					// -------------------------------------------------
	        					if (numberOfProgramRequests > 0)
	        					{
	        						// ---------------------------------------------
	        						// 04/08/2017 ECU changed to use AddAnS
	        						// ---------------------------------------------
	        						TVChannelsActivity.sendMessage ("Requesting information for " + numberOfProgramRequests + " program" + Utilities.AddAnS (numberOfProgramRequests));
	        						// ---------------------------------------------
	        						// 22/07/2016 ECU some programs are required so 
	        						//                request that information
	        						// ---------------------------------------------
	        						response = sendPOST (URL_PROGRAMS,programRequests.toString());
	        						writeToFile (URL_PROGRAMS,response);
	        						parseBuffer (OBJECT_PROGRAMS,response);
	        					}
	        					// -------------------------------------------------
	        					TVChannelsActivity.sendMessage ("Processing lists to generate EPG entries");
	        					// -------------------------------------------------
	        					// 21/07/2016 ECU merge the lists to get required data
	        					// -------------------------------------------------
	        					createSchedules (TVChannelNumber);
	        					// -------------------------------------------------
								// 12/02/2017 ECU display a separator
								// -------------------------------------------------
		        				TVChannelsActivity.sendMessage (MainActivity.activity.getString (R.string.separating_line));
		        				// -------------------------------------------------
	        				}
	        				// -----------------------------------------------------
	        				// 22/07/2016 ECU the programs list may have changed to 
	        				//                write the new copy to disk
	        				// -----------------------------------------------------
	        				TVChannelsActivity.sendMessage ("Writing programs list to disk");
	        				Utilities.writeObjectToDisk (PublicData.storedData.schedulesDirectData.folder + 
	        												MainActivity.activity.getString (R.string.programs_list),
	        												programList);
	        				// -----------------------------------------------------
	        				// 22/07/2016 ECU tell the user that everything done
	        				// -----------------------------------------------------
	        				TVChannelsActivity.sendMessage ("All of the EPG entries have been generated");
	        				// -----------------------------------------------------
	        				// 02/08/2016 ECU now need to check if this activity
	        				//                needs to be terminated and the TV
	        				//                guide activity restarted which will
	        				//                be called if the TV guide found that
	        				//                the data was out of date and a refresh
	        				//                was requested
	        				// -----------------------------------------------------
	        				if (TVChannelsActivity.epgRefresh)
	        				{
	        					TVChannelsActivity.restartTVGuideActivity ();
	        				}
	        				// -----------------------------------------------------
	        			}
	        			else
	        			{
	        				// -----------------------------------------------------
	        				// 24/07/2016 ECU indicate an error
	        				// -----------------------------------------------------
	        				TVChannelsActivity.sendMessage ("Unable to generate the EPG entries");
	        				// -----------------------------------------------------
	        			}
	        		}
	            }
	            catch(Exception theException)
	            { 	 
	            	TVChannelsActivity.sendMessage ("Exception : " + theException);
	            }       
	        }
	    };
	    // -------------------------------------------------------------------------
	    // 22/07/2016 ECU start up the thread
	    // -------------------------------------------------------------------------
	    thread.start();	
	}
	// =============================================================================
	public static void getLineUp ()
	{
		// -------------------------------------------------------------------------
		// 22/07/2016 ECU show the token that has been sent
		// -------------------------------------------------------------------------
		TVChannelsActivity.sendMessage ("Getting channels for lineup " + PublicData.storedData.schedulesDirectData.lineUp);
		// -------------------------------------------------------------------------
		// 22/07/2016 ECU changed to use the line up that is stored in the object
		// -------------------------------------------------------------------------
		String response = sendGET (String.format (URL_LINEUP_FORMAT, PublicData.storedData.schedulesDirectData.lineUp));
		// -------------------------------------------------------------------------
		// 19/11/2017 ECU save the response for later information
		//--------------------------------------------------------------------------
		writeToFile (PublicData.storedData.schedulesDirectData.lineUp,response);
		// -------------------------------------------------------------------------
		// 22/07/2016 ECU now generate the TV channels from the retrieved information
		// -------------------------------------------------------------------------
		parseBuffer (OBJECT_LINEUP,response);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean getToken ()
	{
		// -------------------------------------------------------------------------
		// 21/07/2016 ECU check if data has been initialised
		// 24/07/2016 ECU return whether the token was returned OK
		//            ECU changed to use the resource values rather than literals
		// -------------------------------------------------------------------------
		if (PublicData.storedData.schedulesDirectData == null)
		{
			PublicData.storedData.schedulesDirectData  
					= new SchedulesDirectData (MainActivity.activity.getString (R.string.schedules_direct_username_default),
											   MainActivity.activity.getString (R.string.schedules_direct_password_default));
		}
		// -------------------------------------------------------------------------
		TVChannelsActivity.sendMessage ("Obtaining the token");
		// --------------------------------------------------------------------------
		// 21/07/2016 ECU send the message to get the token
		// --------------------------------------------------------------------------
		String postData = String.format(SchedulesDirect.TOKEN_REQUEST,PublicData.storedData.schedulesDirectData.userName,PublicData.storedData.schedulesDirectData.passwordEncrypted);
		String response = SchedulesDirect.sendPOST (URL_TOKEN, postData);
		// -------------------------------------------------------------------------
		// 21/07/2016 ECU parse the response to get the token
		// 24/07/2016 ECU check whether the parsing worked
		// -------------------------------------------------------------------------
		if (parseBuffer (OBJECT_TOKEN,response))
		{
			// ---------------------------------------------------------------------
			// 22/07/2016 ECU show the token that has been sent
			// ---------------------------------------------------------------------
			TVChannelsActivity.sendMessage ("Token : " + PublicData.storedData.schedulesDirectData.token);
			// ---------------------------------------------------------------------
			// 24/07/2016 ECU return that all was well
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 24/07/2016 ECU an error occurred so display the response
			// ---------------------------------------------------------------------
			TVChannelsActivity.sendMessage ("Error : " + response);
			// ---------------------------------------------------------------------
			// 24/07/2016 ECU indicate a problem occurred
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static void getStatus ()
	{
		String response = sendGET (URL_STATUS);
		
		Utilities.popToast("response : " + response);
	}
	// =============================================================================
	static boolean parseBuffer (int theObjectType,String theData)
	{
		// -------------------------------------------------------------------------
		// 21/07/2016 ECU declare any variables needed
		// 24/07/2016 ECU changed to return 'boolean' to indicate whether everything
		//                was OK (true) or not (false)
		// -------------------------------------------------------------------------
		JSONObject json;
		// -------------------------------------------------------------------------
		try
		{
			switch (theObjectType)
			{
				// -----------------------------------------------------------------
				case OBJECT_LINEUP:
	    			json = new JSONObject (theData);
	    			// -------------------------------------------------------------
	    			JSONArray maps = json.getJSONArray ("map");
	    			// -------------------------------------------------------------
					for (int theMap = 0; theMap < maps.length(); theMap++)
					{
						//String stationID = (maps.getJSONObject(theMap)).getString ("stationID");
						//String channel   = (maps.getJSONObject(theMap)).getString ("channel");
					}
					// -------------------------------------------------------------
					JSONArray stationsLineup = json.getJSONArray ("stations");
					// -------------------------------------------------------------
					for (int theStation = 0; theStation < stationsLineup.length(); theStation++)
					{
						String stationID 	= (stationsLineup.getJSONObject(theStation)).getString ("stationID");
						//String callSign   	= (stationsLineup.getJSONObject(theStation)).getString ("callsign");
						String name   		= (stationsLineup.getJSONObject(theStation)).getString ("name");
						// ---------------------------------------------------------
						// 21/07/2016 ECU search for the channel number
						// ---------------------------------------------------------
						int channel   = Integer.parseInt ((maps.getJSONObject(theStation)).getString ("channel"));
						// ---------------------------------------------------------
						TVChannelsActivity.TVChannelsAvailable.add (new TVChannel (channel,name,stationID));
					}
				
					break;
				// -----------------------------------------------------------------
				case OBJECT_PROGRAMS:
	    			JSONArray programs = new JSONArray (theData);
	    			
	    			String title= StaticData.BLANK_STRING;
	    	    			
	    			for (int theProgram = 0; theProgram < programs.length(); theProgram++)
					{
						String programID = (programs.getJSONObject(theProgram)).getString ("programID");
												
						JSONArray titles = (programs.getJSONObject(theProgram)).getJSONArray ("titles");
						
						for (int theTitle = 0; theTitle < titles.length(); theTitle++)
						{
							title = (titles.getJSONObject(theTitle)).getString ("title120");
						}
						
						JSONObject descriptions = (programs.getJSONObject(theProgram)).getJSONObject ("descriptions");
						JSONArray description = descriptions.getJSONArray ("description1000");
						
						JSONArray genres = (programs.getJSONObject(theProgram)).getJSONArray ("genres");
						
						String genre = genres.getString (0);
						
						String desc = StaticData.BLANK_STRING;
						for (int theDescription = 0; theDescription < description.length(); theDescription++)
						{
							desc = (description.getJSONObject(theDescription)).getString ("description");
						}
						
						programList.add (new SchedulesDirectProgram (programID,title,desc,genre));
					}		
					break;
				// -----------------------------------------------------------------
				case OBJECT_SCHEDULES:
					// -------------------------------------------------------------
					// 21/07/2016 ECU parse the incoming schedule information
					// -------------------------------------------------------------
					// 22/07/2016 ECU because the schedules will be requesting
					//                program information then initialise the variables
					//                that check for this
					// -------------------------------------------------------------
					numberOfProgramRequests = 0;
					programRequests 		= new StringBuffer ();
	    			programRequests.append ("[");
	    			// -------------------------------------------------------------
	    			// 21/07/2016 ECU now parse the received data
	    			// -------------------------------------------------------------
	    			JSONArray stations = new JSONArray (theData);
					// -------------------------------------------------------------
	    			// 21/07/2016 ECU loop for all stations for which the schedules
	    			//                were requested
	    			// -------------------------------------------------------------
					for (int theStation = 0; theStation < stations.length(); theStation++)
					{
						//String stationID = (stations.getJSONObject(theStation)).getString ("stationID");
						// ---------------------------------------------------------
						// 21/07/2016 ECU get the associated schedules
						// ---------------------------------------------------------
						JSONArray programsSchedules = stations.getJSONObject (theStation).getJSONArray ("programs");
		    			// ---------------------------------------------------------
						// 21/07/2016 ECU loop through each associated program entry
						// ---------------------------------------------------------
						for (int theProgram = 0; theProgram < programsSchedules.length (); theProgram++)
						{
							// -----------------------------------------------------
							// 21/07/2016 ECU get the timings of the program
							// -----------------------------------------------------
							String airDateTime = (programsSchedules.getJSONObject(theProgram)).getString ("airDateTime");
							// -----------------------------------------------------
							// 21/07/2016 ECU change the format of the date and time
							// -----------------------------------------------------
							SimpleDateFormat UTCFormat = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss",Locale.getDefault());
							UTCFormat.setTimeZone (TimeZone.getTimeZone ("UTC"));
							Date utcDate = UTCFormat.parse (airDateTime);
							// -----------------------------------------------------
							// 27/10/2016 ECU changed to use dateSimpleFormat rather
							//                than creating a new one
							// -----------------------------------------------------
							SimpleDateFormat dateFormat = PublicData.dateSimpleFormat;
							// -----------------------------------------------------
							dateFormat.setTimeZone (TimeZone.getDefault());
							SimpleDateFormat timeFormat = new SimpleDateFormat ("HH:mm",Locale.getDefault());
							timeFormat.setTimeZone (TimeZone.getDefault());
							int duration = (programsSchedules.getJSONObject(theProgram)).getInt ("duration");
							// -----------------------------------------------------
							// 21/07/2016 ECU store the program ID because it may need
							//                to be requested
							// -----------------------------------------------------
							String programID = (programsSchedules.getJSONObject(theProgram)).getString ("programID");
							// -----------------------------------------------------
							// 22/07/2016 ECU check if the required program is already
							//                stored in the list
							// -----------------------------------------------------
							if (findProgram (programID) == StaticData.NO_RESULT)
							{
								// -------------------------------------------------
								// 22/07/2016 ECU the required program is not
								//                current stored so request it
								// -------------------------------------------------
								programRequests.append ("\"" + programID + "\",");
								// -------------------------------------------------
								// 22/07/2016 ECU increment the number of programs 
								//                that will need to be requested
								// -------------------------------------------------
								numberOfProgramRequests++;
								// -------------------------------------------------
							}
							// -----------------------------------------------------
							// 27/10/2016 ECU changed to use dateSimpleFormat rather
							//                than creating a new one
							// 24/07/2017 ECU changed to use ALARM...
							// -----------------------------------------------------
							scheduleList.add (new SchedulesDirectSchedule (utcDate.getTime(),
																		   PublicData.dateSimpleFormat.format(utcDate),
									   			                           (new SimpleDateFormat (StaticData.ALARM_TIME_FORMAT,Locale.getDefault())).format(utcDate),
									   			                           duration,
									   			                           programID));
							// -----------------------------------------------------
						}
					}
					programRequests.deleteCharAt (programRequests.length()-1);
					programRequests.append("]");
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case OBJECT_STATUS:
	    			json = new JSONObject (theData);
	    			//JSONObject accountObject = json.getJSONObject("account");
	    		
	        		//String expires = accountObject.getString("expires");
	        		//int    maxlineUps = accountObject.getInt ("maxLineups");
	        		JSONArray lineUps = json.getJSONArray("lineups");
	        			
	        		for (int theIndex = 0; theIndex < lineUps.length(); theIndex++)
					{
						//String lineup = (lineUps.getJSONObject(theIndex)).getString ("lineup");
					}
					break;
				// -----------------------------------------------------------------
				case OBJECT_TOKEN:
					json = new JSONObject (theData);
					PublicData.storedData.schedulesDirectData.token = json.getString("token");
					break;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 24/07/2016 ECU everything seemed OK so return that fact
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
	    }
	    catch (Exception theException)
	    {
	    	// ---------------------------------------------------------------------
	    	// 24/07/2016 ECU an error occurred so return that fact
	    	// ---------------------------------------------------------------------
	    	return false;
	    	// ---------------------------------------------------------------------
	    }
	}
	// =============================================================================
	static String readFromFile (String theFileName)
	{
		// -------------------------------------------------------------------------
		String responseString = null;
		try
		{
			// ---------------------------------------------------------------------
			FileInputStream inputStream = new FileInputStream (PublicData.projectFolder + "SchedulesDirect/" + theFileName);
			byte [] localBuffer = new byte [500000];
			int numberRead = inputStream.read (localBuffer);
			inputStream.close ();	
			// ---------------------------------------------------------------------
			// 29/03/2020 ECU set up the string to be returned
			// ---------------------------------------------------------------------
			responseString = new String (localBuffer,0,numberRead);
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
		}
		return responseString;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String sendGET (String theURL)
	{
		// -------------------------------------------------------------------------
		// 19/07/2016 ECU created to use the GET method to obtain information
		// 22/07/2016 ECU changed to use the base URL stored in the data
		// 16/12/2017 ECU changed to public
		// -------------------------------------------------------------------------
		try
		{
			URL url = new URL (PublicData.storedData.schedulesDirectData.baseURL + theURL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod ("GET");
			connection.setRequestProperty ("token",PublicData.storedData.schedulesDirectData.token);
			// ---------------------------------------------------------------------
			// 19/07/2016 check the response code
			// ---------------------------------------------------------------------
			int responseCode = connection.getResponseCode ();
			// ---------------------------------------------------------------------
			if (responseCode == HttpURLConnection.HTTP_OK) 
			{ 
				// -----------------------------------------------------------------
				// 19/07/2016 ECU read in the associated data
				// -----------------------------------------------------------------
				// 06/03/2017 ECU set up the input stream for incoming URL data
				// -----------------------------------------------------------------
				InputStream inputStream = connection.getInputStream();
				// -----------------------------------------------------------------
				// 06/03/2017 ECU want to check for the corrected encoding
				// 07/03/2017 ECU the encoding could return 'null' so check for
				//                this
				// -----------------------------------------------------------------
				String contentEncoding = connection.getContentEncoding ();
				if ((contentEncoding != null) && contentEncoding.equalsIgnoreCase ("gzip"))
				{
					// -------------------------------------------------------------
					// 06/03/2017 ECU the incoming data is 'gzip'-ed so make sure
					//                it is unzipped
					// -------------------------------------------------------------
					inputStream = new GZIPInputStream (inputStream);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				BufferedReader bufferedReader 
					= new BufferedReader (new InputStreamReader (inputStream));
				// -----------------------------------------------------------------
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = bufferedReader.readLine()) != null) 
				{
					response.append(inputLine);
				}
				bufferedReader.close();
				// -----------------------------------------------------------------
				// 20/07/2016 ECU return the response as a string
				// -----------------------------------------------------------------
				return response.toString();
				// -----------------------------------------------------------------
			} 
			else 
			{
				Utilities.LogToProjectFile (TAG,"GET request not worked");
			}
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 29/03/2020 ECU add the URL to the exception output
			// ----------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG,"sendGET : "  + theURL + "  : " + theException);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 21/07/2016 ECU if get here then an error occurred
		// -------------------------------------------------------------------------
		return null;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static String sendPOST (String theURL,String theData)
	{
		// -------------------------------------------------------------------------
		// 19/07/2016 ECU created to use the POST method to send and receive data 
		// 22/07/2016 ECU changed to use the base URL stored in the data
		// -------------------------------------------------------------------------
		try
		{
			URL url = new URL (PublicData.storedData.schedulesDirectData.baseURL + theURL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod   ("POST");
			connection.setRequestProperty ("token",PublicData.storedData.schedulesDirectData.token);
			connection.setRequestProperty ("Content-Type","text/html"); 
			connection.setRequestProperty ("charset", "utf-8");
			// ---------------------------------------------------------------------
			// 05/03/2017 ECU IMPORTANT found that getting encoded response from
			//                ========= Schedules Direct server - if the following
			//                          line commented out then everything seems
			//                          OK - however the SD documentation says
			//                          that the client MUST include this. Something
			//                          seems to have changed on the server as even
			//                          devices with older 'working' software have
			//                          now stopped' working
			// 06/03/2017 ECU the comments from yesterday are valid but I believe
			//                it highlighted a problem in that the encoding of
			//                incoming data was not being taken into account. This
			//                means that incoming 'gzip'-ed data was not being
			//                unzipped before being processed. Changed the code to 
			//                take the encoding into account
			// ---------------------------------------------------------------------
			connection.setRequestProperty ("Accept-Encoding", "gzip,deflate");
			// ---------------------------------------------------------------------
			connection.setUseCaches (false);
			connection.setDoOutput (true);
			// ---------------------------------------------------------------------
			// 19/07/2016 ECU write out the associated data
			// ---------------------------------------------------------------------
			OutputStream outputStream = connection.getOutputStream ();
			outputStream.write(theData.getBytes ("UTF-8"));
			outputStream.flush ();
			outputStream.close ();
			// ---------------------------------------------------------------------
			// 19/07/2016 ECU now get the response
			// ---------------------------------------------------------------------
			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) 
			{ 
				// -----------------------------------------------------------------
				// 06/03/2017 ECU set up the input stream for incoming URL data
				// -----------------------------------------------------------------
				InputStream inputStream = connection.getInputStream();
				// -----------------------------------------------------------------
				// 06/03/2017 ECU want to check for the corrected encoding
				// 07/03/2017 ECU the encoding could return 'null' so check for
				//                this
				// -----------------------------------------------------------------
				String contentEncoding = connection.getContentEncoding();
				if ((contentEncoding != null) && contentEncoding.equalsIgnoreCase ("gzip"))
				{
					// -------------------------------------------------------------
					// 06/03/2017 ECU the incoming data is 'gzip'-ed so make sure
					//                it is unzipped
					// -------------------------------------------------------------
					inputStream = new GZIPInputStream (inputStream);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				BufferedReader bufferedReader 
					= new BufferedReader(new InputStreamReader (inputStream));
				// -----------------------------------------------------------------
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = bufferedReader.readLine()) != null) 
				{
					response.append(inputLine);
				}
				// -----------------------------------------------------------------
				// 07/03/2017 ECU Note - close down the incoming data stream
				// -----------------------------------------------------------------
				bufferedReader.close ();
				// -----------------------------------------------------------------
				// 20/07/2016 ECU write the response to the specified file
				// -----------------------------------------------------------------
				return response.toString();
				// -----------------------------------------------------------------
			} 
			else 
			{
				Utilities.LogToProjectFile (TAG,"POST request not worked");
			}
		}
		catch (Exception theException)
		{
			Utilities.LogToProjectFile (TAG,"sendGET : " + theException);
		}
		// -------------------------------------------------------------------------
		// 21/07/2016 ECU if get here then a problem occurred so indicate the fact
		// -------------------------------------------------------------------------
		return null;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void writeToFile (String theFileName,String theData)
	{
		// -------------------------------------------------------------------------
		// 21/07/2016 ECU write the data to the specified file whose name is an
		//                offset within the Schedules Direct folder
		// -------------------------------------------------------------------------
		try
		{
			FileOutputStream outputStream = new FileOutputStream (PublicData.storedData.schedulesDirectData.folder + theFileName);
			outputStream.write (theData.getBytes());
			outputStream.close ();			
		}
		catch (Exception theException)
		{
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

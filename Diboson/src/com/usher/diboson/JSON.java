package com.usher.diboson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.widget.TextView;

public class JSON 
{
	// =============================================================================
	// 22/06/2016 ECU created to handle various aspects of the JavaScript Object
	//                Notation (JSON)
	// 24/06/2016 ECU lots more tidying up
	// =============================================================================
	//private static final String TAG = "JSON";
	// =============================================================================
	
	// =============================================================================
	private static final String JSON_BROADCAST			 	= "broadcast";
	private static final String JSON_BROADCAST_DURATION	 	= "broadcast_duration";
	private static final String JSON_CHANNEL			 	= "channel";
	private static final String JSON_CHANNEL_GROUP		 	= "channel_group";
	private static final String JSON_CHANNELS			 	= "channels";
	private static final String JSON_DESCRIPTION			= "description";
	private static final String JSON_DISPLAY_TITLE		 	= "display_title";
	private static final String JSON_ENTRIES			 	= "entries";
	private static final String JSON_ITEM			 	 	= "item";
	private static final String JSON_NULL			 	 	= "null";
	private static final String JSON_SCHEDULE			 	= "schedule";
	private static final String JSON_TITLE				 	= "title";
	private static final String JSON_TRANSMISSION_END_TIME	= "transmission_end_time";
	private static final String JSON_TRANSMISSION_TIME	 	= "transmission_time";
	// =============================================================================
	
	// =============================================================================
	static	SimpleDateFormat 	dateFormat;						// 24/06/2016 ECU added
			Method				processMethod;
			DownloadWebPageTask task;
	static  SimpleDateFormat 	timeFormat;						// 24/06/2016 ECU added
			Method				urlMethod;						// 23/06/2016 ECU added
	static 	Date				utcDate;						// 24/06/2015 ECU added
	static	SimpleDateFormat    UTCFormat;
			String				webPageContents;
	// =============================================================================
	public JSON ()
	{
		task = new DownloadWebPageTask();
		// -------------------------------------------------------------------------
		// 24/06/2016 ECU set up the relevant formats for date handling.
		//                Needed because the times provided by Metabroadcast are
		//                in UTC format and want it as GMT with daylight saving
		//                taken into account.
		// -------------------------------------------------------------------------
		UTCFormat = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss",Locale.getDefault());
		UTCFormat.setTimeZone(TimeZone.getTimeZone ("UTC"));
		// -------------------------------------------------------------------------
		// 27/10/2016 ECU changed to use dateSimpleFormat instead of creating a new one
		// -------------------------------------------------------------------------
		dateFormat = PublicData.dateSimpleFormat;
		dateFormat.setTimeZone (TimeZone.getDefault());
		
		timeFormat = new SimpleDateFormat ("HH:mm",Locale.getDefault());
		timeFormat.setTimeZone (TimeZone.getDefault());
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	String addKeyToURL (String theURL)
	{
		// -------------------------------------------------------------------------
		// 22/06/2016 ECU add the Metabroadcast key to the URL
		// 09/07/2016 ECU changed to use the stored key rather than a hardcoded value
		// -------------------------------------------------------------------------
		return String.format (theURL, PublicData.storedData.metaBroadcast.apiKey);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static String convertDate (String theISOdate,boolean theDateTimeFlag)
	{
		// -------------------------------------------------------------------------
		// 24/06/2016 ECU created to manipulate the date which is supplied in ISO
		//                format (e.g. 2016-06-24T12:00:00.000Z) into the form that 
		//                is required
		//                    theDateTimeFlag  = true   return date
		//                                     = false  return time
		// -------------------------------------------------------------------------
		try
		{	
			utcDate = UTCFormat.parse (theISOdate);
		}
		catch (Exception theException)
		{
			
		}
		// -------------------------------------------------------------------------
		// 24/06/2016 ECU return what is required
		// -------------------------------------------------------------------------
		if (theDateTimeFlag)
		{
			// ---------------------------------------------------------------------
			// 24/06/2016 ECU return the date
			// ---------------------------------------------------------------------
			return dateFormat.format (utcDate);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 24/06/2016 ECU return the time
			// ---------------------------------------------------------------------
			return timeFormat.format (utcDate);
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public void getResponse (URLMethod theURLMethod)
	{
		// -------------------------------------------------------------------------
		// 22/06/2016 ECU get the response to the supplied URL into which the key
		//                must be added
		// 23/06/2016 ECU changed to use URLMethod
		// -------------------------------------------------------------------------
		urlMethod = theURLMethod.URLMethod;
		task.execute (new String[] {addKeyToURL (theURLMethod.URL)});
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean parseChannelJSON (String theJSONString,TextView theTextView)
	{
		// -------------------------------------------------------------------------
		// 22/06/2016 ECU created to parse the JSON string
		// 27/06/2016 ECU changed to 'boolean' so that the result of the operation
		//                can be passed back to the caller
		// -------------------------------------------------------------------------
		String	localDate			= "";
		int		localDateIndex  	= StaticData.NO_RESULT;
		String 	localDescription;
		// -------------------------------------------------------------------------
		try
		{
			TVChannelsActivity.TVChannelsSelected.get(TVChannelsActivity.channelNumber).EPGEntries 
					= new ArrayList<ArrayList<EPGEntry>> ();
			// ---------------------------------------------------------------------
			// 23/06/2016 ECU convert the string to an object so that it can be parsed
			// ---------------------------------------------------------------------
			JSONObject json = (new JSONObject(theJSONString)).getJSONObject(JSON_SCHEDULE);
				
			JSONArray entries  = json.getJSONArray (JSON_ENTRIES);
				
			for (int theEntry = 0; theEntry < entries.length(); theEntry++)
			{
				String [] epgFields = EPGEntry.emptyEPGArray();
					
				JSONObject entry = entries.getJSONObject(theEntry);
					
				JSONObject broadcast = entry.getJSONObject (JSON_BROADCAST);
					
				JSONObject item = entry.getJSONObject (JSON_ITEM);
					
				JSONObject displayTitle = item.getJSONObject (JSON_DISPLAY_TITLE);
					
				epgFields [StaticData.EPG_DATE] = convertDate (broadcast.getString(JSON_TRANSMISSION_TIME),true);
				// -----------------------------------------------------------------
				// 24/06/2016 ECU sort out the description field in case it is null
				// -----------------------------------------------------------------
				localDescription = item.getString (JSON_DESCRIPTION);
				if (localDescription.equalsIgnoreCase (JSON_NULL))
					localDescription = "";
				epgFields [StaticData.EPG_DESCRIPTION] = localDescription;
				// -----------------------------------------------------------------
				epgFields [StaticData.EPG_PROGRAM_TITLE] = displayTitle.getString (JSON_TITLE);
				epgFields [StaticData.EPG_START_TIME] = convertDate (broadcast.getString (JSON_TRANSMISSION_TIME),false);
				epgFields [StaticData.EPG_END_TIME] = convertDate (broadcast.getString (JSON_TRANSMISSION_END_TIME),false);
				epgFields [StaticData.EPG_DURATION] = broadcast.getString (JSON_BROADCAST_DURATION);
				// -----------------------------------------------------------------
				// 25/06/2016 ECU check if the date has altered
				// -----------------------------------------------------------------
				if (!epgFields[StaticData.EPG_DATE].equalsIgnoreCase(localDate))
				{
					// -------------------------------------------------------------
					// 25/06/2016 ECU the date has changed so generate new ArrayList
					// -------------------------------------------------------------
					TVChannelsActivity.TVChannelsSelected.get(TVChannelsActivity.channelNumber).EPGEntries.add (new ArrayList<EPGEntry>());
					// -------------------------------------------------------------
					// 25/06/2016 ECU store and increment the working variables
					// -------------------------------------------------------------
					localDate = epgFields[StaticData.EPG_DATE];
					localDateIndex++;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 25/06/2016 ECU add the entry into the list
				// -----------------------------------------------------------------
				TVChannelsActivity.TVChannelsSelected.get(TVChannelsActivity.channelNumber).EPGEntries.get (localDateIndex).add (new EPGEntry (epgFields));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 27/06/2016 ECU indicate that everything seems OK
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 27/06/2016 ECU display an error message and tell the caller of the
			//                problem
			// ---------------------------------------------------------------------
			TVChannelsActivity.sendMessage ("Parse error has occurred" + StaticData.NEWLINE +
											"     " + theJSONString);
			// ---------------------------------------------------------------------
			// 27/06/2016 ECU log the data for future use
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void parseChannelsJSON (String theJSONString,TextView theTextView)
	{
		// -------------------------------------------------------------------------
		// 22/06/2016 ECU created to parse the JSON string
		// -------------------------------------------------------------------------
		try
		{
			// ---------------------------------------------------------------------
			// 23/06/2016 ECU convert the string to an object so that it can be parsed
			// ---------------------------------------------------------------------
			JSONObject json = new JSONObject(theJSONString);
		 
			JSONArray channels  = (json.getJSONObject(JSON_CHANNEL_GROUP)).getJSONArray (JSON_CHANNELS);
		 	     
			for (int theChannel = 0; theChannel < channels.length(); theChannel++)
			{
				JSONObject channel = (channels.getJSONObject(theChannel)).getJSONObject(JSON_CHANNEL);
	    	 
				theTextView.append ("Channel ID : " + channel.getString("id") + "  Name : " + channel.getString("title") + "\n");
				
				TVChannelsActivity.TVChannelsAvailable.add (new TVChannel (theChannel,channel.getString("title"),channel.getString("id")));
			}
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	private class DownloadWebPageTask extends AsyncTask <String, Void, String> 
	{
		@Override
		protected String doInBackground (String... urls) 
		{
			String	localInput;

			webPageContents = "";
			
			for (String url : urls) 
			{
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				try 
				{
					HttpResponse execute = client.execute(httpGet);
					InputStream content = execute.getEntity().getContent();

					BufferedReader buffer = new BufferedReader(	new InputStreamReader(content));
					while ((localInput = buffer.readLine()) != null) 
					{
						webPageContents += localInput;
					}

				} 
				catch (Exception theException) 
				{
				}
			}
			return webPageContents;
		}
		// -------------------------------------------------------------------------
		@Override
		protected void onPostExecute (String thePageContents) 
		{
			try 
			{ 
				// -------------------------------------------------------------
				// 16/03/2015 ECU call up the method that will handle the 
				//                input text
				// -------------------------------------------------------------
				urlMethod.invoke (null,thePageContents);
				// -------------------------------------------------------------
			} 
			catch (Exception theException) 
			{	
				theException.printStackTrace();
			} 
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
}

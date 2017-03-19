package com.usher.diboson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;

public class TVChannelsActivity extends DibosonActivity 
{
	// =============================================================================
	// =============================================================================
	// 07/02/2014 ECU created to handle a TV channel guide (like an electronic
	//                program guide
	// 22/09/2015 ECU playing with threads and add 'buttonsEnabled'
	// 26/09/2015 ECU added 'dataChanged'
	// 11/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 23/06/2016 ECU V E R Y  I M P O R T A N T
	//       		  ==========================
	//				  On 16/06/2016 Metabroadcast terminated their provision of
	//                the TV information for Radio Times in XMLTV format. To move
	//                forward the code has had to be changed to the JSON api provided
	//                by Metabroadcast through its 'atlas' system. To try and retain
	//                both system have used XMLTV to indicate 'true' for the old
	//                system and 'false' for the JSON system
	// 21/07/2016 ECU found a new source of data from Schedules Direct so modified the
	//                code to use that mechanism
	// 12/02/2017 ECU changed so that the visibility of buttons depends on what data
	//                is available
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	// =============================================================================
	public static final boolean METABROADCAST	 = false;			// 22/07/2016 ECU added
	public static final boolean	SCHEDULES_DIRECT = true;			// 21/07/2016 ECU added
	public static final boolean	XMLTV 			 = false;			// 23/06/2016 ECU added
	// =============================================================================
	
	// =============================================================================
			static	Activity				activity;						// 26/06/2016 ECU added
					boolean					buttonsEnabled 		= false;
	public	static  int						channelNumber;					// 24/06/2016 ECU added
	public	static 	TextView 				channelsTextView;
			static  Context					context;
			static	boolean					dataChanged			= false;
					ArrayList <String> 		displayString;
			static  Button					epgChannelsButton;				// 12/02/2017 ECU added
			static  Button					epgGenerateButton;				// 26/06/2016 ECU added
			static  Button					epgSelectChannelsButton;		// 12/02/2017 ECU added
			static  Button					epgSelectedChannelsButton;		// 12/02/2017 ECU added
			static  Button					epgShowEPGButton;				// 12/02/2017 ECU added
	public	static	boolean					epgRefresh			= false;	// 26/06/2016 ECU added
																			// 02/08/2016 ECU changed to public
			static  Button					epgSortButton;					// 26/06/2016 ECU added
			static  int						retryCounter;					// 26/06/2016 ECU added
	public  static  TextViewHandler			textViewHandler;				// 16/10/2015 ECU added
	public 	static 	ArrayList <TVChannel>	TVChannelsAvailable;
	public 	static 	ArrayList <TVChannel>	TVChannelsSelected;
	// =============================================================================

	// =============================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 11/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 15/08/2015 ECU set up common aspects of the activity
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity(this,StaticData.ACTIVITY_FULL_SCREEN);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_tvchannels);
			// ---------------------------------------------------------------------
			// 18/09/2015 ECU remember the context for latter use
			// 26/06/2016 ECU added the setting of the activity
			// ---------------------------------------------------------------------
			activity = this;
			context  = this;
			// ---------------------------------------------------------------------
			// 09/07/2016 ECU check if the metabroadcast data needs initialising
			//                - if appropriate
			// 02/08/2016 ECU changed from !XMLTV to METABROADCAST
			// ---------------------------------------------------------------------
			if (METABROADCAST) 
			{
				// -----------------------------------------------------------------
				// 02/08/2016 ECU Note - check if associated data has been initialised
				//                       - if not then do so
				// -----------------------------------------------------------------
				if (PublicData.storedData.metaBroadcast == null)
				{
					PublicData.storedData.metaBroadcast 
						= new MetaBroadcast (getString (R.string.api_key),
										 	 getString (R.string.channel_groups_url),
										 	 getString (R.string.channel_url));
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			channelsTextView = (TextView) findViewById (R.id.tv_channels_textview);
			channelsTextView.setMovementMethod(new ScrollingMovementMethod()); 
			// ---------------------------------------------------------------------
			// 22/09/2015 ECU set the text space to fixed-font spacing
			// ---------------------------------------------------------------------
			channelsTextView.setTypeface(Typeface.MONOSPACE);    
			// ---------------------------------------------------------------------
			// 19/09/2015 ECU clear out any initial text
			// ---------------------------------------------------------------------
			channelsTextView.setText ("");
			// ---------------------------------------------------------------------
			// 26/06/2016 ECU get the EPG generate button
			// ---------------------------------------------------------------------
			epgGenerateButton = (Button) findViewById (R.id.epg_generate);
			// ---------------------------------------------------------------------
			// 26/06/2016 ECU get the EPG generate button
			// 12/02/2017 ECU added the definition of all buttons
			// ---------------------------------------------------------------------
			epgChannelsButton			= (Button) findViewById (R.id.epg_channels);
			epgSelectChannelsButton		= (Button) findViewById (R.id.epg_select_channels);
			epgSelectedChannelsButton 	= (Button) findViewById (R.id.epg_selected_channels);
			epgShowEPGButton 			= (Button) findViewById (R.id.epg_show_epg);
			epgSortButton 				= (Button) findViewById (R.id.epg_sort_channels);
			// ---------------------------------------------------------------------
			// 18/09/2015 ECU set up the button handlers
			// 22/09/2015 ECU added 'show_epg'
			// 26/06/2016 ECU added 'sort'
			// 12/02/2017 ECU added 'channels', 'selectChannels', 'selectedChannels'
			//                'showEPG' buttons
			// ---------------------------------------------------------------------		
			epgChannelsButton.setOnClickListener (buttonListener);
			epgGenerateButton.setOnClickListener (buttonListener);
			epgSelectChannelsButton.setOnClickListener (buttonListener);
			epgSelectedChannelsButton.setOnClickListener (buttonListener);
			epgShowEPGButton.setOnClickListener (buttonListener);
			epgSortButton.setOnClickListener (buttonListener);
			// ---------------------------------------------------------------------
			// 26/06/2016 ECU decide the legend to be displayed on the 'sort' button
			// ---------------------------------------------------------------------
			epgSortButton.setText (PublicData.storedData.tvChannelsSorted ? getString (R.string.epg_do_not_sort_channels)
					                                                      : getString (R.string.epg_sort_channels));
			// ---------------------------------------------------------------------
			// 16/10/2015 ECU set up the handler
			// ---------------------------------------------------------------------
			textViewHandler = new TextViewHandler ();
			// ---------------------------------------------------------------------
			// 22/11/2015 ECU initialise the stored data
			// ---------------------------------------------------------------------
			SelectorUtilities.Initialise ();
			// ---------------------------------------------------------------------
			// 26/06/2016 ECU default any static variables
			// ---------------------------------------------------------------------
			epgRefresh = false;
			// ---------------------------------------------------------------------
			// 26/09/2015 ECU indicate that data has not changed
			// ---------------------------------------------------------------------
			dataChanged = false;
			//----------------------------------------------------------------------
			Thread thread = new Thread()
			{
				@Override
				public void run()
				{
					// -------------------------------------------------------------
					TVChannelsAvailable = getTVChannelsAvailable ();
					TVChannelsSelected	= getTVChannelsSelected (context);
					// -------------------------------------------------------------
					// 22/09/2015 ECU indicate that buttons are now available
					// -------------------------------------------------------------
					buttonsEnabled = true;
					// -------------------------------------------------------------
					// 19/10/2015 ECU check if any parameters have been received
					// -------------------------------------------------------------
					Bundle localExtras = getIntent().getExtras();
					// -------------------------------------------------------------
					if (localExtras != null) 
					{
						// ---------------------------------------------------------
						// 19/10/2015 ECU check if an EPG refresh is required
						// 26/06/2016 ECU changed to use epgRefresh variable
						// ---------------------------------------------------------
					    epgRefresh	= localExtras.getBoolean (StaticData.PARAMETER_EPG_REFRESH,false); 
					    // ---------------------------------------------------------
					    if (epgRefresh)
					    {
					    	// -----------------------------------------------------
					    	// 19/10/2015 ECU a new version of the EPG is required - the
					    	//                'true' indicates that the activity is to finish
					    	//                on completion
					    	// 24/06/2016 ECU check which version is required
					    	// 26/07/2016 ECU include the call to the Schedules Direct
					    	//                generator
					    	// -----------------------------------------------------
					    	if (XMLTV)
					    		generateEPG (true);
					    	else
					    	{
					    		// -------------------------------------------------
					    		// 26/07/2016 ECU check for MetaBroadcast
					    		// -------------------------------------------------
					    		if (METABROADCAST)
					    			generateEPGJSON ();
					    		// -------------------------------------------------
					    		// 26/07/2016 ECU check for Schedules Direct
					    		// -------------------------------------------------
					    		if (SCHEDULES_DIRECT)
					    			SchedulesDirect.generateEPG (TVChannelsSelected);
					    		// -------------------------------------------------
					    	}
					    	// -----------------------------------------------------
					    }
					    // ---------------------------------------------------------
					}
					// -------------------------------------------------------------
					// 12/02/2017 ECU make sure that the visibility of the buttons is
					//                updated
					// -------------------------------------------------------------
					buttonVisibilityUpdateRequest ();
					// -------------------------------------------------------------
				}
			};
			// ---------------------------------------------------------------------
			// 14/02/2017 ECU Note - start up the initialisation thread
			// ---------------------------------------------------------------------
			thread.start();  
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 11/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	private View.OnClickListener buttonListener = new View.OnClickListener() 
	{
		// -------------------------------------------------------------------------
		@Override
		public void onClick(View view) 
		{	
			if (buttonsEnabled)
			{	
				Message localMessage;
				//------------------------------------------------------------------
				// 18/09/2015 ECU now process depending on which button pressed
				//------------------------------------------------------------------
				switch (view.getId()) 
				{
					// -------------------------------------------------------------
					case R.id.epg_channels:
						// ---------------------------------------------------------
						// 18/09/2015 ECU get the list of available channels
						// 23/06/2016 ECU changed to handle XML versus JSON
						// ---------------------------------------------------------
						if (XMLTV)
						{
							localMessage = Message.obtain(null,StaticData.MESSAGE_WEB_PAGE);
							localMessage.obj = new URLMethod (StaticData.EPG_CHANNELS_URL,
									Utilities.createAMethod (TVChannelsActivity.class,"TVChannelsMethod",(ArrayList<String>) null));
							PublicData.messageHandler.sendMessage (localMessage);  
						}
						else
						{
							if (SCHEDULES_DIRECT)
							{
								// -------------------------------------------------
								// S C H E D U L E S   D I R E C T
								// -------------------------------------------------
								TVChannelsAvailable = new ArrayList<TVChannel>();
								SchedulesDirect.buildTVChannelsList ();
								// -------------------------------------------------
							}
							else
							{
								// -------------------------------------------------
								// M E T A B R O A D C A S T
								// -------------------------------------------------
								// 23/06/2016 ECU using JSON to obtain the TV channels
								// 09/07/2016 ECU changed to use the data stored in
								//                metaBroadcast rather than string resource
								// -------------------------------------------------
								JSON localJSON = new JSON ();
								localJSON.getResponse (new URLMethod (PublicData.storedData.metaBroadcast.channelGroupsURL,
										Utilities.createAMethod (TVChannelsActivity.class,"TVChannelsJSONMethod","")));
								// -------------------------------------------------
							}
						}
						// --------------------------------------------------------- 
						break;
					// -------------------------------------------------------------
					case R.id.epg_select_channels:
						// ---------------------------------------------------------
						// 18/09/2015 ECU select the channels that are to be used
						//                for EPG
						// ---------------------------------------------------------
						selectTVChannels();
						// ---------------------------------------------------------
						break;
					// -------------------------------------------------------------
					case R.id.epg_generate:
						// ---------------------------------------------------------
						// 22/09/2015 ECU generate the EPG for the selected channels
						// ---------------------------------------------------------
						// 22/07/2016 ECU check if any channels have been selected
						// ---------------------------------------------------------
						if (TVChannelsSelected.size() > 0)
						{
							if (XMLTV)
								generateEPG (false);
							else
							{
								if (SCHEDULES_DIRECT)
								{
									// -------------------------------------------------
									// 21/07/2016 ECU Schedules Direct
									// -------------------------------------------------
									AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + 
											getString (R.string.epg_channels_selected_file),TVChannelsSelected);
									SchedulesDirect.generateEPG (TVChannelsSelected);
								}
								else
								{
									// -------------------------------------------------
									// 21/07/2016 ECU MetaBroadcast
									// -------------------------------------------------
									generateEPGJSON ();
									// -------------------------------------------------
								}
							}
						}
						else
						{
							// -----------------------------------------------------
							// 22/07/2016 ECU no TV channels have been selected
							// -----------------------------------------------------
							Utilities.popToastAndSpeak (getString (R.string.epg_no_selected_channels));
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
						break;
					// -------------------------------------------------------------
					case R.id.epg_selected_channels:
						// ---------------------------------------------------------
						// 18/09/2015 ECU select the channels that are to be used for EPG
						// ---------------------------------------------------------
						deselectTVChannels();
						// ---------------------------------------------------------
						break;
					// -------------------------------------------------------------
					case R.id.epg_show_epg:
						// ---------------------------------------------------------
						// 18/09/2015 ECU show the current EPG
						// ---------------------------------------------------------
						showCurrentEPG (channelsTextView);
						// ---------------------------------------------------------
						break;
					// -------------------------------------------------------------
					case R.id.epg_sort_channels:
						// ---------------------------------------------------------
						// 26/06/2016 ECU toggle the sort/not sort flag
						// ---------------------------------------------------------
						PublicData.storedData.tvChannelsSorted 
							= !PublicData.storedData.tvChannelsSorted;
						// ---------------------------------------------------------
						// 26/06/2016 ECU update the displayed legend
						// ---------------------------------------------------------
						epgSortButton.setText (PublicData.storedData.tvChannelsSorted ? getString (R.string.epg_do_not_sort_channels)
                                													  : getString (R.string.epg_sort_channels));
						// ---------------------------------------------------------
						// 26/06/2016 ECU tell the user what is happening
						// ---------------------------------------------------------
						Utilities.popToastAndSpeak(PublicData.storedData.tvChannelsSorted ? getString (R.string.epg_channels_sorted)
                                													      : getString (R.string.epg_channels_not_sorted),true);
						// ---------------------------------------------------------
						break;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 12/02/2017 ECU change the visibility of the buttons as appropriate
				// -----------------------------------------------------------------
				buttonVisibility ();
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 22/09/2015 ECU indicate that still retrieving data
				// 14/02/2017 ECU changed to use the resource
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (getString (R.string.data_retrieving));
				// -----------------------------------------------------------------		
			}
		}
		// -------------------------------------------------------------------------
	};
	// =============================================================================
	@Override
    public void onDestroy()
    {	
		// -------------------------------------------------------------------------
		// 19/09/2015 ECU added
		// -------------------------------------------------------------------------
		// 12/02/2017 ECU make sure that information on the selected channels is
		//                written to disk
		// 14/02/2017 ECU take into account the data changed flag
		// -------------------------------------------------------------------------
		if (dataChanged)
		{
			AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + context.getString (R.string.epg_channels_selected_file),TVChannelsSelected);
		}
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
    }
	// =============================================================================
	
	// =============================================================================
	public static ArrayList<ListItem> BuildTheChannelsList ()
	{
		SelectorUtilities.selectorParameter.listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 18/09/2015 ECU add in the check on size
		// 12/02/2017 ECU this check is not really required because the button is
		//                only visible if the channels exist
		// -------------------------------------------------------------------------  
		if (TVChannelsAvailable.size() > 0)
		{
			for (int theIndex = 0; theIndex < TVChannelsAvailable.size(); theIndex++)
			{
				// -----------------------------------------------------------------
				// 12/02/2017 ECU only list channels which are not already selected
				// -----------------------------------------------------------------
				if (!checkIfChannelSelected (theIndex))
				{
					// -----------------------------------------------------------------
					// 18/09/2015 ECU added the index as an argument
					// 23/06/2016 ECU add in the ID, if defined
					// 14/02/2017 ECU changed to use blank string
					// -----------------------------------------------------------------
					SelectorUtilities.selectorParameter.listItems.add (new ListItem (
										StaticData.BLANK_STRING,
										TVChannelsAvailable.get(theIndex).channelName,
										"     Channel Number : " + TVChannelsAvailable.get(theIndex).channelNumber,
										((TVChannelsAvailable.get(theIndex).channelID == null) ? "" 
																							   : ("        ID : " + TVChannelsAvailable.get(theIndex).channelID)),
										theIndex));
				// -----------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
			// 12/02/2017 ECU sort the list into alphabetical order
			// ---------------------------------------------------------------------
			Collections.sort (SelectorUtilities.selectorParameter.listItems);
			// ---------------------------------------------------------------------
		}
		return SelectorUtilities.selectorParameter.listItems;
	}	
	// =============================================================================
	public static ArrayList<ListItem> BuildTheSelectedChannelsList ()
	{
		SelectorUtilities.selectorParameter.listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		// 18/09/2015 ECU add in the check on size
		// 12/02/2017 ECU the following check is not required because the button
		//                is only visible if selected channels exist
		// -------------------------------------------------------------------------  
		if (TVChannelsSelected.size() > 0)
		{
			for (int theIndex = 0; theIndex < TVChannelsSelected.size(); theIndex++)
			{
				// -----------------------------------------------------------------
				// 18/09/2015 ECU added the index as an argument
				// 23/06/2016 ECU added the channel's ID
				// -----------------------------------------------------------------
				SelectorUtilities.selectorParameter.listItems.add (new ListItem (
								"",
								TVChannelsSelected.get(theIndex).channelName,
								"     Channel Number : " + TVChannelsSelected.get(theIndex).channelNumber,
								((TVChannelsSelected.get(theIndex).channelID == null) ? "" 
                                        											  : ("        ID : " + TVChannelsSelected.get(theIndex).channelID)),
								theIndex));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 26/06/2016 ECU do the sorting dependent on the the flag
			// ---------------------------------------------------------------------
			if (PublicData.storedData.tvChannelsSorted)
				Collections.sort (SelectorUtilities.selectorParameter.listItems);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 12/02/2017 ECU all 'selected' channels have been 'deselected'
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (context.getString (R.string.tv_channels_all_deselected), true);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		return SelectorUtilities.selectorParameter.listItems;
	}
	// =============================================================================
	static void buttonVisibility ()
	{
		// -------------------------------------------------------------------------
		// 12/02/2017 ECU created to adjust the visibility of the buttons
		// ------------------------------------------------------------------------- 
		int availableVisibility = (TVChannelsAvailable == null ||TVChannelsAvailable.size() == 0) 
																	? View.INVISIBLE
																	: View.VISIBLE;
		int selectedVisibility = (TVChannelsSelected == null ||TVChannelsSelected.size() == 0) 
																	? View.INVISIBLE
																	: View.VISIBLE;
		// -------------------------------------------------------------------------
		epgSelectChannelsButton.setVisibility (availableVisibility);
		epgGenerateButton.setVisibility (selectedVisibility);
		epgSelectedChannelsButton.setVisibility (selectedVisibility);
		epgShowEPGButton.setVisibility (selectedVisibility);
		epgSortButton.setVisibility (selectedVisibility);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void ButtonVisibilityMethod (int theAction)
	{
		// -------------------------------------------------------------------------
		// 12/02/2017 ECU created to be called to update the visibility of the buttons
		// -------------------------------------------------------------------------
		buttonVisibility ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void buttonVisibilityUpdateRequest ()
	{
		// -------------------------------------------------------------------------
		// 12/02/2017 ECU created to send the message to get the visibility of the
		//                buttons updated
		// -------------------------------------------------------------------------
		Message localMessage = PublicData.messageHandler.obtainMessage (StaticData.MESSAGE_METHOD,
				new MethodDefinition<TVChannelsActivity> (TVChannelsActivity.class,"ButtonVisibilityMethod"));
		PublicData.messageHandler.sendMessage (localMessage);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static boolean checkIfChannelSelected (int theChannel)
	{
		// -------------------------------------------------------------------------
		// 12/02/2017 ECU created to check if a specified channel is already selected
		//                and to return the answer
		//                		true .............. channel selected
		//         				false ............. channel not selected
		// -------------------------------------------------------------------------
		if (TVChannelsSelected.size() > 0)
		{
			for (int channel = 0; channel < TVChannelsSelected.size(); channel++)
			{
				if (TVChannelsSelected.get(channel).channelName.equalsIgnoreCase(TVChannelsAvailable.get (theChannel).channelName))
				{
					// -------------------------------------------------------------
					// 12/02/2017 ECU indicate that a match has been found
					// -------------------------------------------------------------
					return true;
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
			// 12/02/2017 ECU none of the selected channels match that specified
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
    	}
		else
		{
			// ---------------------------------------------------------------------
			// 12/02/2017 ECU there are no selected channels
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	ArrayList <TVChannel> createTVChannelsAvailable (ArrayList<String> theInputData)
	{
		// -------------------------------------------------------------------------
		// 19/09/2015 ECU created to build the available TV channel objects from
		//                the input data
		// -------------------------------------------------------------------------
		if (theInputData.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 19/09/2014 ECU declare the data needed
			//				
			//				  Each line consists of <channel number> | <channel name>
			// ---------------------------------------------------------------------
			ArrayList<TVChannel> localChannels = new ArrayList<TVChannel> ();
			String fields [];
			// ---------------------------------------------------------------------
			// 19/09/2015 ECU loop through the input data
			// ---------------------------------------------------------------------
			for (int line = 0;line < theInputData.size();line++)
			{
				fields = theInputData.get(line).split("[|]");
				// ----------------------------------------------------------------
				// 19/09/2015 ECU only lines with both fields are valid
				// ----------------------------------------------------------------
				if (fields.length == 2)
				{	
					localChannels.add (new TVChannel(Integer.parseInt(fields[0]),fields[1]));
				}
			}
			// ---------------------------------------------------------------------
			// 19/09/2015 ECU return the generated TV channel list
			// ---------------------------------------------------------------------
			return localChannels;
		}
		else
		{
			// ---------------------------------------------------------------------
			// 19/09/2015 ECU if there was no input data then return a 'null'
			// ---------------------------------------------------------------------
			return null;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    public static void DeselectAction (int theChannelSelected)
    {
    	// -------------------------------------------------------------------------
    	//29/08/2015 ECU created to handle the deselection of the channel
    	//           ECU at the moment just call the channel removal
    	// -------------------------------------------------------------------------
    	TVChannelsSelected.remove (theChannelSelected);
    	// -------------------------------------------------------------------------
    	// 26/09/2015 ECU indicate that data has been changed
    	// -------------------------------------------------------------------------
    	dataChanged = true;
    	// -------------------------------------------------------------------------
    	// 14/02/2017 ECU chacked if there are any selected channels left
    	// -------------------------------------------------------------------------
    	if (TVChannelsSelected.size() > 0)
    	{
    		// ---------------------------------------------------------------------
    		// 14/02/2017 ECU still selected channels so redisplay them
    		// ---------------------------------------------------------------------
    		Selector.Rebuild();
    		// ---------------------------------------------------------------------
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 14/02/2017 ECU all selected channels have been deselected
    		// ---------------------------------------------------------------------
    		Utilities.popToast (MainActivity.activity.getString (R.string.tv_channels_none_selected));
    		// ---------------------------------------------------------------------
    		// 14/02/2017 ECU make sure that the selector activity is terminated
    		// ---------------------------------------------------------------------
    		Selector.Finish ();
    		// ---------------------------------------------------------------------
    		// 14/02/2017 ECU make sure that the buttons are updated
    		// ---------------------------------------------------------------------
    		TVChannelsActivity.buttonVisibilityUpdateRequest ();
    		// ---------------------------------------------------------------------
    	} 
    }
	// =============================================================================
	static void deselectTVChannels ()
	{
		// -------------------------------------------------------------------------
		// 12/02/2017 ECU check if there are any selected channels
		// -------------------------------------------------------------------------
		if (TVChannelsSelected.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 26/06/2016 ECU add the 'sort' flag
			// 12/02/2017 ECU add the 'back method'
			// ---------------------------------------------------------------------
			BuildTheSelectedChannelsList ();
			SelectorUtilities.selectorParameter.rowLayout 				= R.layout.channels_row;
			SelectorUtilities.selectorParameter.classToRun 				= TVChannelsActivity.class;
			SelectorUtilities.selectorParameter.type 					= StaticData.OBJECT_SELECTED_CHANNELS;
			SelectorUtilities.selectorParameter.sort					= PublicData.storedData.tvChannelsSorted;
			SelectorUtilities.selectorParameter.backMethodDefinition 	= new MethodDefinition<TVChannelsActivity> (TVChannelsActivity.class,"ButtonVisibilityMethod");
			SelectorUtilities.selectorParameter.customMethodDefinition 	= new MethodDefinition<TVChannelsActivity> (TVChannelsActivity.class,"DeselectAction");
			SelectorUtilities.selectorParameter.customLegend 			= context.getString (R.string.deselect);
			// ---------------------------------------------------------------------
			SelectorUtilities.StartSelector (context,StaticData.OBJECT_SELECTED_CHANNELS);
		}
		else
		{
			// ---------------------------------------------------------------------
			// 12/02/2017 ECU there are no selected channels - inform the user
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (context.getString (R.string.tv_channels_none_selected),true);
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static void EPGMethod (ArrayList<String> thePage)
	{
		if (thePage.size() > 0)
		{
			String words [];
			for (int theChannel=0;theChannel < thePage.size();theChannel++)
			{
				new EPGEntry (thePage.get(theChannel));
				words = thePage.get(theChannel).split("[~]");
				if (words.length == StaticData.EPG_FIELD_COUNT)
						channelsTextView.append ("Program : " + words [StaticData.EPG_PROGRAM_TITLE] + 
								"  Date : " + words [StaticData.EPG_DATE] + 
								"  Start Time : " + words [StaticData.EPG_START_TIME] + "\n");
			}
		}
	}
	// =============================================================================
	void generateEPG (final boolean theFinishFlag)
	{
		// -------------------------------------------------------------------------
		// 19/09/2015 ECU created to generate the EPG for the selected channels
		// 19/10/2015 ECU added theFinishFlag
		// 24/12/2015 ECU put in the check on whether a network is available
		// -------------------------------------------------------------------------
		if (Utilities.checkForNetwork (context))
		{
			if (TVChannelsSelected.size() > 0)
			{
				// -----------------------------------------------------------------
				// 16/10/2015 ECU put status message on screen
				// 14/02/2017 ECU changed to use resources
				// -----------------------------------------------------------------
				sendMessage (getString (R.string.epg_start_generating));
				sendMessage (getString (R.string.epg_writing));
				// -----------------------------------------------------------------
				// 16/10/2015 ECU channels have been selected but want to write the
				//                data to disk before it is populated with EPG data
				// -----------------------------------------------------------------
				AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + getString (R.string.epg_channels_selected_file),TVChannelsSelected);
				// -----------------------------------------------------------------
				// 22/09/2015 ECU changed to run most of the work in a thread
				// -----------------------------------------------------------------
				Thread thread = new Thread()
				{
					@SuppressLint ("DefaultLocale")
					@Override
					public void run()
					{
						// ---------------------------------------------------------
						// 22/09/2015 ECU do not allow the buttons while generating
						// ---------------------------------------------------------
						buttonsEnabled = false;
						// ---------------------------------------------------------
						// 19/09/2015 ECU loop through the selected channels
						// ---------------------------------------------------------
						for (int channel = 0; channel < TVChannelsSelected.size(); channel++)
						{
							// -----------------------------------------------------
							// 16/10/2015 ECU put status message on screen
							// 14/02/2017 ECU changed to use resources
							// -----------------------------------------------------
							sendMessage (getString (R.string.epg_getting) + TVChannelsSelected.get(channel).channelName);
							// -----------------------------------------------------
							// 16/10/2015 ECU get the EPG data for the specified TV channel
							// -----------------------------------------------------
							ArrayList <String> 	inputData 
								= Utilities.getWebPage (String.format (StaticData.EPG__URL,TVChannelsSelected.get(channel).channelNumber));
							// ------------------------------------------------------
							if (inputData == null)
								sendMessage (getString (R.string.epg_error_getting) + TVChannelsSelected.get(channel).channelName);
							else
							if (inputData.size() == 0)
								sendMessage (getString (R.string.epg_no_data) + TVChannelsSelected.get(channel).channelName);
							else
							{
								// -------------------------------------------------
								// 16/10/2015 ECU try and generate EPG entries from 
								//                the received data
								// -------------------------------------------------
								TVChannelsSelected.get (channel).parseURLData (inputData);
								// -------------------------------------------------
							}
						}
						// ---------------------------------------------------------
						// 12/02/2017 ECU display a separator
						// ---------------------------------------------------------
						sendMessage (getString (R.string.separating_line));
						// ---------------------------------------------------------
						// 16/10/2015 ECU now write the EPG data to disk
						// ---------------------------------------------------------
						for (int theChannel = 0; theChannel < TVChannelsSelected.size(); theChannel++)
						{
							// -----------------------------------------------------
							// 16/10/2015 ECU put status message on screen
							// 14/02/2017 ECU changed to use resource
							// -----------------------------------------------------
							sendMessage (getString (R.string.epg_writing_data) + TVChannelsSelected.get(theChannel).channelName + " to disk");
							// -----------------------------------------------------
							TVChannelsSelected.get(theChannel).writeToDisk();
						}
						// ---------------------------------------------------------
						// 12/02/2017 ECU display a separator
						// ---------------------------------------------------------
						sendMessage (getString (R.string.separating_line));
						// ---------------------------------------------------------
						// 22/09/2015 ECU reenable the buttons
						// ---------------------------------------------------------
						buttonsEnabled = true;
						// ---------------------------------------------------------
						// 16/10/2015 ECU put status message on screen
						// 14/02/2017 ECU changed to use resource
						// ---------------------------------------------------------
						sendMessage (getString (R.string.epg_complete));
						// ---------------------------------------------------------
						// 26/09/2015 ECU indicate that data has been changed
						// ---------------------------------------------------------
						dataChanged = true;
						// ---------------------------------------------------------
						// 19/10/2015 ECU check whether the activity is to be 'finish'-ed
						// ---------------------------------------------------------
						if (theFinishFlag)
						{
							// -----------------------------------------------------
							// 19/10/2015 ECU restart the TV guide activity
							// 19/02/2016 ECU pass back the argument to indicate that
							//                the ShowEPG... activity is being called
							//                from here
							// 02/08/2016 ECU changed to call the method which contains
							//                the code that was here
							// -----------------------------------------------------
							restartTVGuideActivity ();
							// -----------------------------------------------------
						}
						// ---------------------------------------------------------
					}
				};
				thread.start();  
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 19/09/2015 ECU no channels have been selected
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 24/12/2015 ECU indicate that there is no connection to a network
			// 14/02/2017 ECU changed to use resource
			// ---------------------------------------------------------------------
			Utilities.popToast (getString (R.string.network_no_access));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void generateEPGJSON ()
	{
		if (Utilities.checkForNetwork(context))
		{
			if (TVChannelsSelected.size() > 0)
			{
				// -----------------------------------------------------------------
				// 26/06/2016 ECU hide the button
				// 28/06/2016 ECU only if not an automatic refresh
				// -----------------------------------------------------------------
				if (!epgRefresh)
					epgGenerateButton.setVisibility (View.INVISIBLE);
				// -----------------------------------------------------------------
				// 24/06/2016 ECU reset the running channel number
				// -----------------------------------------------------------------
				channelNumber = 0;
				// -----------------------------------------------------------------
				// 14/02/2017 ECU changed to use resource
				// -----------------------------------------------------------------
				sendMessage (getString (R.string.epg_obtaining) + TVChannelsSelected.get(channelNumber).channelName);
				// -----------------------------------------------------------------
				// 09/07/2016 ECU change to use the metaBroadcast object rather than
				//                a string resource
				// -----------------------------------------------------------------
				JSON localJSON = new JSON ();
				localJSON.getResponse (new URLMethod (String.format (PublicData.storedData.metaBroadcast.channelURL,TVChannelsSelected.get(0).channelID,"now","now.plus.24h","%s"),
						Utilities.createAMethod (TVChannelsActivity.class,"TVChannelDataJSONMethod","")));
				// -----------------------------------------------------------------
				// 27/06/2016 ECU reset the retry counter
				// -----------------------------------------------------------------
				retryCounter = 0;
				// -----------------------------------------------------------------
			}
		}
	}
	// =============================================================================
	ArrayList <TVChannel> getTVChannelsAvailable ()
	{
		// -------------------------------------------------------------------------
		// 19/09/2015 ECU created to obtain the TV channels that are available for
		//                the EPG. First of all an attempt will be made to read the
		//                information from disc. If this fails then the information
		//                will be obtained from the specified data feed
		// -------------------------------------------------------------------------
		String fileName = PublicData.projectFolder + getString (R.string.epg_channels_available_file);
		// -------------------------------------------------------------------------
		// 19/09/2015 ECU try and get the data from disk
		// -------------------------------------------------------------------------
		@SuppressWarnings ("unchecked")
		ArrayList<TVChannel> localChannels 
			= (ArrayList<TVChannel>) AsyncUtilities.readObjectFromDisk (getBaseContext(),fileName);
		// -------------------------------------------------------------------------
		// 19/09/2015 ECU check if data read successfully
		// -------------------------------------------------------------------------
		if (localChannels == null)
		{
			// ---------------------------------------------------------------------
			// 24/06/2016 ECU take into account whether using XMLTV or not
			// ---------------------------------------------------------------------
			if (XMLTV)
			{
				// -----------------------------------------------------------------
				// 24/06/2016 ECU using XMLTV methods
				// ------------------------------------------------------------------
				ArrayList <String> 	inputData = Utilities.getWebPage (StaticData.EPG_CHANNELS_URL);
				// -----------------------------------------------------------------
				// 19/09/2015 ECU convert the retrieved data into the required objects
				// -----------------------------------------------------------------
				localChannels = createTVChannelsAvailable (inputData);
				// -----------------------------------------------------------------
				// 19/09/2015 ECU if anything was obtained then write the data to disk
				// -----------------------------------------------------------------
				if (localChannels != null)
				{
					AsyncUtilities.writeObjectToDisk (fileName,localChannels);
				}
			}
			else
			{
				// -----------------------------------------------------------------
				// 24/06/2016 ECU using JSON methods
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 19/09/2015 ECU return the retrieved data
		// -------------------------------------------------------------------------
		return localChannels;
	}
	// =============================================================================
	public static ArrayList <TVChannel> getTVChannelsSelected (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 19/09/2015 ECU created to obtain the TV channels that have been selected for
		//                the EPG. Read the information from disc - if the file does
		//				  not exist then just create an empty list
		// 23/09/2015 ECU changed to 'public static' and added context as an
		//                argument
		// -------------------------------------------------------------------------
		String fileName = PublicData.projectFolder + theContext.getString (R.string.epg_channels_selected_file);
		// -------------------------------------------------------------------------
		// 19/09/2015 ECU try and get the data from disk
		// -------------------------------------------------------------------------
		@SuppressWarnings ("unchecked")
		ArrayList<TVChannel> localChannels 
		= (ArrayList<TVChannel>) AsyncUtilities.readObjectFromDisk (theContext,fileName);
		// -------------------------------------------------------------------------
		// 19/09/2015 ECU check if data read successfully
		// -------------------------------------------------------------------------
		if (localChannels == null)
		{
			// ---------------------------------------------------------------------
			// 19/09/2015 ECU file does not exist so just return an empty list
			// ---------------------------------------------------------------------
			localChannels = new ArrayList<TVChannel> ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 19/09/2015 ECU return the retrieved data
		// -------------------------------------------------------------------------
		return localChannels;
	}
	// =============================================================================
	public static void restartTVGuideActivity ()
	{
		// -------------------------------------------------------------------------
		// 02/08/2016 ECU created as a separate method to start the TV guide
		//                activity and then to terminate this activity
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (context,ShowEPGActivity.class);
		localIntent.putExtra (StaticData.PARAMETER_EPG_REFRESH,true);
		context.startActivity (localIntent);
		// -------------------------------------------------------------------------
		// 26/06/2016 ECU terminate this activity
		// -------------------------------------------------------------------------
		activity.finish ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    public static void SelectAction (int theChannelSelected)
    {
    	// -------------------------------------------------------------------------
    	// 29/08/2015 ECU created to handle the selection of an item
    	//            ECU at the moment just call the care
    	// 16/10/2015 ECU changed to use the names rather than the whole record
    	// -------------------------------------------------------------------------
    	// 16/10/2015 ECU check if the channel already selected
    	// 12/02/2017 ECU changed to use the new checkIfChannelSelected method
    	// -------------------------------------------------------------------------
    	if (!checkIfChannelSelected (theChannelSelected))
    	{
    		// ---------------------------------------------------------------------
    		// 16/10/2015 ECU if no match found then can add the channel
    		// ---------------------------------------------------------------------
    		TVChannelsSelected.add (TVChannelsAvailable.get (theChannelSelected));
    		// -------------------------------------------------------------------------
    		TVChannelsAvailable.remove (theChannelSelected);
    		// -------------------------------------------------------------------------
    		// 26/09/2015 ECU indicate that data has been changed
    		// -------------------------------------------------------------------------
    		dataChanged = true;
    		// ------------------------------------------------------------------------- 
    		Selector.Rebuild();
    	}
    	else
    	{
    		Utilities.popToast ("Channel " + TVChannelsAvailable.get (theChannelSelected).channelName + " is already selected");
    	}
    }
	// =============================================================================
	static void selectTVChannels ()
	{
		// -------------------------------------------------------------------------
		// 12/02/2017 ECU put up a warning about not showing channels which are
		//                already selected 
		//            ECU but only if there are some selected
		//			  ECU added the 'back method'
		// -------------------------------------------------------------------------
		if (TVChannelsSelected.size() > 0)
			Utilities.popToastAndSpeak (context.getString (R.string.tv_channels_selected_not_displayed),true);
		// -------------------------------------------------------------------------
		BuildTheChannelsList ();
		SelectorUtilities.selectorParameter.rowLayout 				= R.layout.channels_row;
		SelectorUtilities.selectorParameter.classToRun 				= TVChannelsActivity.class;
		SelectorUtilities.selectorParameter.type 					= StaticData.OBJECT_CHANNELS;
		SelectorUtilities.selectorParameter.backMethodDefinition 	= new MethodDefinition<TVChannelsActivity> (TVChannelsActivity.class,"ButtonVisibilityMethod");
		SelectorUtilities.selectorParameter.customMethodDefinition 	= new MethodDefinition<TVChannelsActivity> (TVChannelsActivity.class,"SelectAction");
		SelectorUtilities.selectorParameter.customLegend 			= context.getString (R.string.select);
		// ----------------------------------------------------------------------
		SelectorUtilities.StartSelector (context,StaticData.OBJECT_CHANNELS);
	}
	// =============================================================================
	public static void sendMessage (String theMessage)
	{
		// -------------------------------------------------------------------------
		// 16/10/2015 ECU created to send a message to the handler
		// 27/06/2016 ECU made public
		// -------------------------------------------------------------------------
		Message localMessage = textViewHandler.obtainMessage (StaticData.MESSAGE_DATA);
		localMessage.obj 	= theMessage + "\n";
		textViewHandler.sendMessage (localMessage);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void showCurrentEPG (TextView theTextView)
	{
		// -------------------------------------------------------------------------
		// 22/09/2015 ECU display the EPG for the currently selected channels
		// -------------------------------------------------------------------------
		if (TVChannelsSelected.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 27/10/2016 ECU changed to use dateSimpleFormat instead of creating a
			//                new one
			// ---------------------------------------------------------------------
			String dateString = PublicData.dateSimpleFormat.format( (Calendar.getInstance()).getTimeInMillis());
			// ---------------------------------------------------------------------
			// 22/09/2015 ECU loop for each selected channel
			// ---------------------------------------------------------------------
			for (int channel = 0; channel < TVChannelsSelected.size(); channel++)
			{
				// -----------------------------------------------------------------
				// 22/09/2015 ECU display details of the channel
				// -----------------------------------------------------------------
				theTextView.append (TVChannelsSelected.get(channel).Print (dateString) + "\n");
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 22/09/2015 ECU there are no selected channels
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak ("There are no selected channels");
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class TextViewHandler extends Handler 
	{
		// -------------------------------------------------------------------------
		// 15/10/2015 ECU handle created to check timer message
		// -------------------------------------------------------------------------
		@Override
	    public void handleMessage (Message theMessage) 
		{	
			switch (theMessage.what) 
			{
				// -----------------------------------------------------------------
				case StaticData.MESSAGE_DATA:
					// -------------------------------------------------------------
					// 15/10/2015 ECU transmit the string that is held within the
					//                message
					// -------------------------------------------------------------
					channelsTextView.append((String) theMessage.obj);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------   
	        }
		}
	}
	// =============================================================================
	public static void TVChannelsMethod (ArrayList<String> thePage)
	{
		if (thePage.size() > 0)
		{
			String words [];
			TVChannelsAvailable = new ArrayList<TVChannel>();
			for (int theChannel=0;theChannel < thePage.size();theChannel++)
			{
				words = thePage.get(theChannel).split("[|]");
				if (words.length == 2)
				{
					channelsTextView.append ("Channel : " + words [0] + "  Name : " + words [1] + "\n");
					
					TVChannelsAvailable.add( new TVChannel (Integer.parseInt(words[0]),words[1]));
				}
			}
		}
	}
	// =============================================================================
	public static void TVChannelsJSONMethod (String thePage)
	{
		// -------------------------------------------------------------------------
		// 23/06/2016 ECU want to build up a new list from the information provided
		// -------------------------------------------------------------------------
		TVChannelsAvailable = new ArrayList<TVChannel>();
		// -------------------------------------------------------------------------
		JSON.parseChannelsJSON (thePage,channelsTextView);
		// -------------------------------------------------------------------------
		AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + context.getString (R.string.epg_channels_available_file),TVChannelsAvailable);
	}
	// =============================================================================
	public static void TVChannelDataJSONMethod (String thePage)
	{
		// -------------------------------------------------------------------------
		// 24/06/2016 ECU want to build up a new list from the information provided
		// 27/06/2016 ECU take into account the status return from the parser
		//            ECU put in check on retry limit
		// -------------------------------------------------------------------------
		if (JSON.parseChannelJSON (thePage,channelsTextView) || (retryCounter > StaticData.EPG_RETRY_LIMIT))
		{
			// ---------------------------------------------------------------------
			// 27/06/2016 ECU the parsing was successful so move on to the next
			//                TV channel
			// ---------------------------------------------------------------------
			// 24/06/2016 ECU check if there are more channels to get
			// ---------------------------------------------------------------------
			if (++channelNumber < TVChannelsSelected.size())
			{
				sendMessage ("Obtaining EPG data for " + TVChannelsSelected.get(channelNumber).channelName);
			
				JSON localJSON = new JSON ();
				localJSON.getResponse (new URLMethod (String.format (context.getString(R.string.channel_url),TVChannelsSelected.get(channelNumber).channelID,"now","now.plus.24h","%s"),
										Utilities.createAMethod (TVChannelsActivity.class,"TVChannelDataJSONMethod","")));
				// -----------------------------------------------------------------
				// 27/06/2016 ECU reset the retry counter
				// -----------------------------------------------------------------
				retryCounter = 0;
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 24/06/2016 ECU channels have been selected but want to write the
				//                data to disk before it is populated with EPG data
				// -----------------------------------------------------------------
				AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + context.getString (R.string.epg_channels_selected_file),TVChannelsSelected);
				// -----------------------------------------------------------------
				for (int theChannel = 0; theChannel < TVChannelsSelected.size(); theChannel++)
				{
					// -------------------------------------------------------------
					// 16/10/2015 ECU put status message on screen
					// 26/06/2016 ECU changed to use resource
					// -------------------------------------------------------------
					sendMessage (String.format (context.getString (R.string.epg_writing_channel),TVChannelsSelected.get(theChannel).channelName));
					// -------------------------------------------------------------
					TVChannelsSelected.get(theChannel).writeToDisk();
				}
				// -----------------------------------------------------------------
				// 26/06/2016 ECU indicate that everything has been done
				// -----------------------------------------------------------------
				sendMessage (context.getString (R.string.epg_finished_writing));
				// -----------------------------------------------------------------
				// 26/06/2016 ECU show the button
				// 28/06/2016 ECU only if not an automatic refresh
				// -----------------------------------------------------------------
				if (!epgRefresh)
					epgGenerateButton.setVisibility (View.VISIBLE);
				// -----------------------------------------------------------------
				// 26/06/2016 ECU decide what to do now that the generation of the 
				//			      EPG is complete
				// -----------------------------------------------------------------
				if (epgRefresh)
				{
					// -------------------------------------------------------------
					// 26/06/2016 ECU the activity was called so exit this activity
					//                and restart the guide viewer
					// 02/08/2016 ECU moved the code that was here into the restart...
					//                method which is called from here
					// -------------------------------------------------------------
					restartTVGuideActivity ();
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 27/06/2016 ECU an error occurred so need to retry the current channel
			// ---------------------------------------------------------------------
			// 27/06/2016 ECU increment the retry counter
			// ---------------------------------------------------------------------
			retryCounter++;
			sendMessage ("Retrying [" + retryCounter + "] for " + TVChannelsSelected.get(channelNumber).channelName);
			
			JSON localJSON = new JSON ();
			localJSON.getResponse (new URLMethod (String.format (context.getString(R.string.channel_url),TVChannelsSelected.get(channelNumber).channelID,"now","now.plus.24h","%s"),
									Utilities.createAMethod (TVChannelsActivity.class,"TVChannelDataJSONMethod","")));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

package com.usher.diboson;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

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
	//                both systems have used XMLTV to indicate 'true' for the old
	//                system and 'false' for the JSON system
	// 21/07/2016 ECU found a new source of data from Schedules Direct so modified the
	//                code to use that mechanism
	// 12/02/2017 ECU changed so that the visibility of buttons depends on what data
	//                is available
	// 28/04/2018 ECU changed to use ListViewSelector and reduce the number of 'statics'
	// 25/03/2020 ECU take into account the calling of this activity for the 'daily
	//                check' by ShowEPGActivity
	// 06/07/2020 ECU added the button that will enable to assign a TV channel number
	//                to be set for a particular channel. This is needed because the
	//                channel provided by the EPG may differ from the number needed
	//                by the 'remote controller' to control the physical TV.
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	// =============================================================================
	public static final boolean METABROADCAST	 = false;			// 22/07/2016 ECU added
	public static final boolean	SCHEDULES_DIRECT = true;			// 21/07/2016 ECU added
	public static final boolean	XMLTV 			 = false;			// 23/06/2016 ECU added
	// =============================================================================

	// =============================================================================
	public	static	Activity				activity;						// 26/06/2016 ECU added
					boolean					buttonsEnabled 		= false;
	public	static  int						channelNumber;					// 24/06/2016 ECU added
	public	static 	TextView 				channelsTextView;
			static  Context					context;
			static	boolean					dailyCheck;						// 25/03/2020 ECU added
			static	boolean					dataChanged			= false;
					ArrayList <String> 		displayString;
			static  Button					epgChannelsButton;				// 12/02/2017 ECU added
			static  Button					epgGenerateButton;				// 26/06/2016 ECU added
			static  Button					epgSelectChannelsButton;		// 12/02/2017 ECU added
			static  Button					epgSelectedChannelsButton;		// 12/02/2017 ECU added
			static  Button					epgShowEPGButton;				// 12/02/2017 ECU added
	public	static	boolean					epgRefresh			= false;	// 26/06/2016 ECU added
																			// 02/08/2016 ECU changed to public
			static  Button					epgSetChannelNumberButton;		// 06/07/2020 ECU added
			static  Button					epgSortButton;					// 26/06/2016 ECU added
					ListViewSelector		listViewSelector;				// 28/04/2018 ECU added
			static List<String>				presetChannelNumbers;			// 06/07/2020 ECU added
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
			// 25/03/2020 ECU need to find out early whether this is being called
			//                as part of a 'daily check'. Need to preset to false
			//                as it is a static
			// ---------------------------------------------------------------------
			dailyCheck = false;
			Bundle localExtras = getIntent().getExtras();
			// ---------------------------------------------------------------------
			if (localExtras != null)
			{
				// -----------------------------------------------------------------
				// 25/03/2020 ECU get the flag to indicate if being called
				//                as part of the 'daily check'
				// -----------------------------------------------------------------
				dailyCheck	= localExtras.getBoolean (StaticData.PARAMETER_DAILY_CHECK,false);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 15/08/2015 ECU set up common aspects of the activity
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			// ---------------------------------------------------------------------
			// 18/09/2015 ECU remember the context for latter use
			// 26/06/2016 ECU added the setting of the activity
			// ---------------------------------------------------------------------
			activity 		 = this;
			context  		 = this;
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
					// -------------------------------------------------------------
					PublicData.storedData.metaBroadcast
						= new MetaBroadcast (getString (R.string.api_key),
										 	 getString (R.string.channel_groups_url),
										 	 getString (R.string.channel_url));
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 28/04/2018 ECU display the main menu and indicate that it is the first
			//                time through
			// ---------------------------------------------------------------------
			DisplayMainLayout (true);
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
										Utilities.createAMethod (TVChannelsActivity.class,"TVChannelsJSONMethod",StaticData.BLANK_STRING)));
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
						selectTVChannels ();
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
									// ---------------------------------------------
									// 21/07/2016 ECU Schedules Direct
									// ---------------------------------------------
									// 15/11/2017 ECU Note - write the 'selected'
									//                       channels data to disk
									// ---------------------------------------------
									AsyncUtilities.writeObjectToDisk (PublicData.projectFolder +
											getString (R.string.epg_channels_selected_file),TVChannelsSelected);
									// ---------------------------------------------
									// 15/11/2017 ECU get the EPG generated
									// ---------------------------------------------
									SchedulesDirect.generateEPG (TVChannelsSelected);
									// ---------------------------------------------
								}
								else
								{
									// ---------------------------------------------
									// 21/07/2016 ECU MetaBroadcast
									// ---------------------------------------------
									generateEPGJSON ();
									// ---------------------------------------------
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
						deselectTVChannels ();
						// ---------------------------------------------------------
						break;
					// -------------------------------------------------------------
					// -------------------------------------------------------------
					case R.id.epg_set_channel_number:
						// ---------------------------------------------------------
						// 06/07/2020 ECU added to enable a TV channel number to be
						//                set for a particular channel
						// ---------------------------------------------------------
						setChannelNumber ();
						// ---------------------------------------------------------
						break;
					// -------------------------------------------------------------
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
	public void onBackPressed ()
	{
		// -------------------------------------------------------------------------
		// 28/04/2018 ECU check if on the main display or not
		// -------------------------------------------------------------------------
		if (listViewSelector == null)
		{
			// ---------------------------------------------------------------------
			// 28/04/2018 ECU am on the main display so just exit
			// ---------------------------------------------------------------------
			// 03/08/2016 ECU terminate this activity
			// ---------------------------------------------------------------------
			finish ();
			// ---------------------------------------------------------------------
			// 03/08/2016 ECU now call the super for this method
			// ---------------------------------------------------------------------
			super.onBackPressed();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 28/04/2018 ECU want to display the main layout
			// ---------------------------------------------------------------------
			DisplayMainLayout (false);
			// ---------------------------------------------------------------------
		}
	}
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
	public ArrayList<ListItem> BuildTheChannelsList ()
	{
		// -------------------------------------------------------------------------
		// 28/04/2018 ECU changed to use a local list
		// -------------------------------------------------------------------------
		ArrayList<ListItem> listItems = new ArrayList<ListItem>();
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
				if (checkIfChannelSelected(theIndex))
				{
					// -------------------------------------------------------------
					// 18/09/2015 ECU added the index as an argument
					// 23/06/2016 ECU add in the ID, if defined
					// 14/02/2017 ECU changed to use blank string
					// -------------------------------------------------------------
					listItems.add (new ListItem (
										StaticData.BLANK_STRING,
										TVChannelsAvailable.get(theIndex).channelName,
										"     Channel Number : " + TVChannelsAvailable.get(theIndex).channelNumber,
										((TVChannelsAvailable.get(theIndex).channelID == null) ? StaticData.BLANK_STRING
																							   : ("        ID : " + TVChannelsAvailable.get(theIndex).channelID)),
										theIndex));
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
			// 12/02/2017 ECU sort the list into alphabetical order
			// ---------------------------------------------------------------------
			Collections.sort (listItems);
			// ---------------------------------------------------------------------
		}
		return listItems;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public ArrayList<ListItem> BuildTheSelectedChannelsList ()
	{
		// -------------------------------------------------------------------------
		// 28/04/2018 ECU change to use a local list
		// -------------------------------------------------------------------------
		ArrayList<ListItem> listItems = new ArrayList<ListItem>();
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
				listItems.add (new ListItem (
						StaticData.BLANK_STRING,
						TVChannelsSelected.get(theIndex).channelName,
						"     Channel Number : " + TVChannelsSelected.get(theIndex).channelNumber,
						((TVChannelsSelected.get(theIndex).channelID == null) ? StaticData.BLANK_STRING
								: ("        ID : " + TVChannelsSelected.get(theIndex).channelID)),
						theIndex));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 26/06/2016 ECU do the sorting dependent on the the flag
			// ---------------------------------------------------------------------
			if (PublicData.storedData.tvChannelsSorted)
				Collections.sort (listItems);
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
		return listItems;
	}
	// =============================================================================
	public ArrayList<ListItem> BuildTheSelectedChannelsListForChange ()
	{
		// -------------------------------------------------------------------------
		// 28/04/2018 ECU change to use a local list
		// -------------------------------------------------------------------------
		ArrayList<ListItem> listItems = new ArrayList<ListItem>();
		// -------------------------------------------------------------------------
		int presetChannelNumber;
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU get the details from disk of channels where the number
		//                obtained from the EPG is not as required by the remote
		//                controller
		// -------------------------------------------------------------------------
		presetChannelNumbers = Television.getStoredTVChannels (this,PublicData.projectFolder + getString (R.string.tv_channels_file));
		// -------------------------------------------------------------------------
		// 18/09/2015 ECU add in the check on size
		// 12/02/2017 ECU the following check is not required because the button
		//                is only visible if selected channels exist
		// -------------------------------------------------------------------------
		if (TVChannelsSelected.size() > 0)
		{
			for (int theIndex = 0; theIndex < TVChannelsSelected.size(); theIndex++)
			{
				// ----------------------------------------------------------------
				// 06/07/2020 ECU check if there is an entry for this channel stored
				//                in 'raw' or the 'text' file
				// ----------------------------------------------------------------
				presetChannelNumber = findChannelOnDisk(TVChannelsSelected.get(theIndex).channelName);
				// -----------------------------------------------------------------
				// 18/09/2015 ECU added the index as an argument
				// 23/06/2016 ECU added the channel's ID
				// -----------------------------------------------------------------
				listItems.add (new ListItem (
						StaticData.BLANK_STRING,
						TVChannelsSelected.get(theIndex).channelName,
						"     Channel Number : " + TVChannelsSelected.get(theIndex).channelNumber +
								((presetChannelNumber != StaticData.NOT_SET) ? "   (Remote Controller : " + presetChannelNumber + ")"
																		    : StaticData.BLANK_STRING),
						((TVChannelsSelected.get(theIndex).channelID == null) ? StaticData.BLANK_STRING
								: ("        ID : " + TVChannelsSelected.get(theIndex).channelID)),
						theIndex));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 26/06/2016 ECU do the sorting dependent on the the flag
			// ---------------------------------------------------------------------
			if (PublicData.storedData.tvChannelsSorted)
				Collections.sort (listItems);
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
		return listItems;
	}
	// =============================================================================
	static void buttonVisibility ()
	{
		// -------------------------------------------------------------------------
		// 12/02/2017 ECU created to adjust the visibility of the buttons
		// 28/04/2018 ECU added 'checkIfAll....' in the condition
		// -------------------------------------------------------------------------
		int availableVisibility = ((TVChannelsAvailable == null) ||
										(TVChannelsAvailable.size() == 0) ||
										 	checkIfAllChannelsSelected ())
																	? View.INVISIBLE
																	: View.VISIBLE;
		int selectedVisibility = (TVChannelsSelected == null ||TVChannelsSelected.size() == 0)
																	? View.INVISIBLE
																	: View.VISIBLE;
		// -------------------------------------------------------------------------
		epgSelectChannelsButton.setVisibility (availableVisibility);
		epgGenerateButton.setVisibility (selectedVisibility);
		epgSelectedChannelsButton.setVisibility (selectedVisibility);
		epgSetChannelNumberButton.setVisibility (selectedVisibility);
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
					return false;
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
			// 12/02/2017 ECU none of the selected channels match that specified
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
    	}
		else
		{
			// ---------------------------------------------------------------------
			// 12/02/2017 ECU there are no selected channels
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static boolean checkIfAllChannelsSelected ()
	{
		// -------------------------------------------------------------------------
		// 28/04/2018 ECU check whether or not all channels have been selected
		//				     returns  true ...... all channels have been selected
		//                            false ..... at least one channel is not selected
		// -------------------------------------------------------------------------
		if (TVChannelsAvailable.size() > 0)
		{
			for (int theIndex = 0; theIndex < TVChannelsAvailable.size(); theIndex++)
			{
				// -----------------------------------------------------------------
				// 28/04/2018 ECU check if the channel is selected
				// -----------------------------------------------------------------
				if (checkIfChannelSelected(theIndex))
				{
					// -------------------------------------------------------------
					// 28/04/2018 ECU a channel is not selected so indicate this fact
					// -------------------------------------------------------------
					return false;
					// --------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 28/04/2018 ECU eithere there are no channels or they are all selected
		// -------------------------------------------------------------------------
		return true;
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
    public void DeselectAction (int theChannelSelected)
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
    		// 28/04/2018 ECU use the new method and indicate that only want
    		//                selected channels
    		// ---------------------------------------------------------------------
    		refreshDisplay (true);
    		// ---------------------------------------------------------------------
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 14/02/2017 ECU all selected channels have been deselected
    		// ---------------------------------------------------------------------
    		Utilities.popToast (MainActivity.activity.getString (R.string.tv_channels_none_selected));
    		// ---------------------------------------------------------------------
    		// 28/04/2018 ECU redisplay the main menu
    		// ---------------------------------------------------------------------
    		DisplayMainLayout (false);
    		// ---------------------------------------------------------------------
    	}
    }
	// =============================================================================
	void deselectTVChannels ()
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
			initialiseDisplay (activity,true);
			// ---------------------------------------------------------------------
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
	void DisplayMainLayout (final boolean theFullFlag)
	{
		// -------------------------------------------------------------------------
		// 28/04/2018 ECU created to display the main layout
		//            ECU if theFullFlag is true then this is the first time through
		// -------------------------------------------------------------------------
		// 28/04/2018 ECU indicate that the main display is being displayed
		// -------------------------------------------------------------------------
		listViewSelector = null;
		// -------------------------------------------------------------------------
		setContentView (R.layout.activity_tvchannels);
		// -------------------------------------------------------------------------
		channelsTextView = (TextView) findViewById (R.id.tv_channels_textview);
		channelsTextView.setMovementMethod(new ScrollingMovementMethod());
		// -------------------------------------------------------------------------
		// 22/09/2015 ECU set the text space to fixed-font spacing
		// -------------------------------------------------------------------------
		channelsTextView.setTypeface(Typeface.MONOSPACE);
		// -------------------------------------------------------------------------
		// 19/09/2015 ECU clear out any initial text
		// -------------------------------------------------------------------------
		channelsTextView.setText (StaticData.BLANK_STRING);
		// -------------------------------------------------------------------------
		// 26/06/2016 ECU get the EPG generate button
		// -------------------------------------------------------------------------
		epgGenerateButton = (Button) findViewById (R.id.epg_generate);
		// -------------------------------------------------------------------------
		// 26/06/2016 ECU get the EPG generate button
		// 12/02/2017 ECU added the definition of all buttons
		// 06/07/2020 ECU added 'set channel number'
		// -------------------------------------------------------------------------
		epgChannelsButton			= (Button) findViewById (R.id.epg_channels);
		epgSelectChannelsButton		= (Button) findViewById (R.id.epg_select_channels);
		epgSelectedChannelsButton 	= (Button) findViewById (R.id.epg_selected_channels);
		epgSetChannelNumberButton	= (Button) findViewById (R.id.epg_set_channel_number);
		epgShowEPGButton 			= (Button) findViewById (R.id.epg_show_epg);
		epgSortButton 				= (Button) findViewById (R.id.epg_sort_channels);
		// -------------------------------------------------------------------------
		// 25/03/2020 ECU if doing a 'daily check' then don't display any more
		// --------------------------------------------------------------------------
		if (!dailyCheck)
		{
			// ---------------------------------------------------------------------
			// 18/09/2015 ECU set up the button handlers
			// 22/09/2015 ECU added 'show_epg'
			// 26/06/2016 ECU added 'sort'
			// 12/02/2017 ECU added 'channels', 'selectChannels', 'selectedChannels'
			//                'showEPG' buttons
			// 06/07/2020 ECU added 'set channel number' button
			// ---------------------------------------------------------------------
			epgChannelsButton.setOnClickListener (buttonListener);
			epgGenerateButton.setOnClickListener (buttonListener);
			epgSelectChannelsButton.setOnClickListener (buttonListener);
			epgSelectedChannelsButton.setOnClickListener (buttonListener);
			epgSetChannelNumberButton.setOnClickListener (buttonListener);
			epgShowEPGButton.setOnClickListener (buttonListener);
			epgSortButton.setOnClickListener (buttonListener);
			// ---------------------------------------------------------------------
			// 26/06/2016 ECU decide the legend to be displayed on the 'sort' button
			// ---------------------------------------------------------------------
			epgSortButton.setText (PublicData.storedData.tvChannelsSorted ? getString (R.string.epg_do_not_sort_channels)
		    	                                                          : getString (R.string.epg_sort_channels));
		}
		else
		{
			// ---------------------------------------------------------------------
			// 25/03/2020 ECU do not want to show the buttons
			// 06/07/2020 ECU added 'set channel number'
			// ---------------------------------------------------------------------
			epgGenerateButton.setVisibility (View.INVISIBLE);
			epgChannelsButton.setVisibility (View.INVISIBLE);
			epgSelectChannelsButton.setVisibility (View.INVISIBLE);
			epgSelectedChannelsButton.setVisibility (View.INVISIBLE);
			epgSetChannelNumberButton.setVisibility (View.INVISIBLE);
			epgShowEPGButton.setVisibility (View.INVISIBLE);
			epgSortButton.setVisibility (View.INVISIBLE);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 28/04/2018 ECU check if first time through
		// -------------------------------------------------------------------------
		if (theFullFlag)
		{
			// ---------------------------------------------------------------------
			// 16/10/2015 ECU set up the handler
			// ---------------------------------------------------------------------
			textViewHandler = new TextViewHandler ();
			// ---------------------------------------------------------------------
			// 26/06/2016 ECU default any static variables
			// ---------------------------------------------------------------------
			epgRefresh = false;
			// ---------------------------------------------------------------------
			// 26/09/2015 ECU indicate that data has not changed
			// ---------------------------------------------------------------------
			dataChanged = false;
			// ---------------------------------------------------------------------
		}
		//--------------------------------------------------------------------------
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				// ----------------------------------------------------------------
				// 28/04/2018 ECU check if doing a full rebuild
				// ----------------------------------------------------------------
				if (theFullFlag)
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
					}
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				// 12/02/2017 ECU make sure that the visibility of the buttons is
				//                updated
				// 25/03/2020 ECU check if do the 'daily check'
				// -----------------------------------------------------------------
				if (!dailyCheck)
					buttonVisibilityUpdateRequest ();
				// -----------------------------------------------------------------
			}
		};
		// -------------------------------------------------------------------------
		// 14/02/2017 ECU Note - start up the initialisation thread
		// -------------------------------------------------------------------------
		thread.start();
		// -------------------------------------------------------------------------
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
								"  Start Time : " + words [StaticData.EPG_START_TIME] + StaticData.NEWLINE);
			}
		}
	}
	// =============================================================================
	int findChannelOnDisk (String theChannelName)
	{
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU check if the specified channel has an entry in 'raw' or
		//                in the 'text' file on disk
		// -------------------------------------------------------------------------
		String [] components;
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU loop through the stored entries
		// -------------------------------------------------------------------------
		for (String channelEntry : presetChannelNumbers)
		{
			// ---------------------------------------------------------------------
			components = channelEntry.split (StaticData.ACTION_DELIMITER);
			// ---------------------------------------------------------------------
			// 06/07/2020 ECU check for a valid entry for the specified channel
			// ---------------------------------------------------------------------
			if ((components.length == 2) && components [Television.CHANNEL_NAME].equalsIgnoreCase(theChannelName))
			{
				// -----------------------------------------------------------------
				// 06/07/2020 ECU have found the channel so return the stored
				//                channel number
				// -----------------------------------------------------------------
				return Integer.parseInt (components [Television.CHANNEL_NUMBER]);
				// -----------------------------------------------------------------
			}
		}
		// ------------------------------------------------------------------------
		// 06/07/2020 ECU no entry found so indicate this fact
		// ------------------------------------------------------------------------
		return StaticData.NOT_SET;
		// ------------------------------------------------------------------------
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
							// 14/11/2017 ECU add 'true' to indicate async write
							// -----------------------------------------------------
							TVChannelsSelected.get(theChannel).writeToDisk (true);
							// -----------------------------------------------------
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
						Utilities.createAMethod (TVChannelsActivity.class,"TVChannelDataJSONMethod",StaticData.BLANK_STRING)));
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
		// 25/03/2020 ECU do not redisplay the TV guide if this is part of the
		//                'daily check'
		// -------------------------------------------------------------------------
		if (!dailyCheck)
		{
			// ---------------------------------------------------------------------
			// 25/03/2020 ECU not part of the 'daily check' so redisplay the TV
			//                channel guide
			// ---------------------------------------------------------------------
			Intent localIntent = new Intent (context,ShowEPGActivity.class);
			localIntent.putExtra (StaticData.PARAMETER_EPG_REFRESH,true);
			context.startActivity (localIntent);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 26/06/2016 ECU terminate this activity
		// -------------------------------------------------------------------------
		activity.finish ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    public void SelectAction (int theChannelSelected)
    {
    	// -------------------------------------------------------------------------
    	// 29/08/2015 ECU created to handle the selection of an item
    	//            ECU at the moment just call the care
    	// 16/10/2015 ECU changed to use the names rather than the whole record
    	// -------------------------------------------------------------------------
    	// 16/10/2015 ECU check if the channel already selected
    	// 12/02/2017 ECU changed to use the new checkIfChannelSelected method
    	// -------------------------------------------------------------------------
    	if (checkIfChannelSelected(theChannelSelected))
    	{
    		// ---------------------------------------------------------------------
    		// 16/10/2015 ECU if no match found then can add the channel
    		// ---------------------------------------------------------------------
    		TVChannelsSelected.add (TVChannelsAvailable.get (theChannelSelected));
    		// ---------------------------------------------------------------------
    		TVChannelsAvailable.remove (theChannelSelected);
    		// ---------------------------------------------------------------------
    		// 26/09/2015 ECU indicate that data has been changed
    		// ---------------------------------------------------------------------
    		dataChanged = true;
    		// ---------------------------------------------------------------------
    		// 28/04/2018 ECU check if all of the available channels have been selected
    		// ---------------------------------------------------------------------
    		if (!checkIfAllChannelsSelected ())
    		{
    			// -----------------------------------------------------------------
    			//28/03/2018 ECU there are still some 'unselected' channels
    			// -----------------------------------------------------------------
    			// 28/04/2018 ECU rebuild the display and indicate that want 'all' channels
    			// -----------------------------------------------------------------
    			refreshDisplay (false);
    			// -----------------------------------------------------------------
    		}
    		else
    		{
    			// -----------------------------------------------------------------
    			// 28/04/2018 ECU all available channels have been selected
    			// -----------------------------------------------------------------
    			// 28/04/2018 ECU tell the user before returning to the main display
    			// ------------------------------------------------------------------
    			Utilities.popToastAndSpeak (getString (R.string.tv_channels_all_selected));
    			DisplayMainLayout (false);
    			// -----------------------------------------------------------------
    		}
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		Utilities.popToast ("Channel " + TVChannelsAvailable.get (theChannelSelected).channelName + " is already selected");
    		// ---------------------------------------------------------------------
    	}
    }
	// =============================================================================
	void selectTVChannels ()
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
		// 28/04/2018 ECU initialise the display and indicate that want all channels
		//                to be displayed
		// -------------------------------------------------------------------------
		initialiseDisplay (activity,false);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void sendMessage (String theMessage)
	{
		// -------------------------------------------------------------------------
		// 16/10/2015 ECU created to send a message to the handler
		// 27/06/2016 ECU made public
		// 25/03/2020 ECU only send a message if not in 'daily check' mode
		// -------------------------------------------------------------------------
		Message localMessage = textViewHandler.obtainMessage (StaticData.MESSAGE_DATA);
		localMessage.obj 	= theMessage + StaticData.NEWLINE;
		textViewHandler.sendMessage (localMessage);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void setChannelNumber ()
	{
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU created to enable a channel number to be set for a
		//                particular EPG channel
		// -------------------------------------------------------------------------

		// -------------------------------------------------------------------------
		// 06/07/2020 ECU display only SELECTED TV channels
		// -------------------------------------------------------------------------
		listViewSelector = new ListViewSelector (activity,
												 R.layout.channels_row,
												 "BuildTheSelectedChannelsListForChange",
											     PublicData.storedData.tvChannelsSorted,
												 null,
												 null,
												 null,
												 getString (R.string.set_channel_number),
												 "SetChannelNumberAction",
												 null,
												 null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void SetChannelNumberAction (int theChannel)
	{
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU created to set the channel number associated with the EPG
		//                channel
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU remember this selected channel
		// -------------------------------------------------------------------------
		channelNumber = theChannel;
		// -------------------------------------------------------------------------
		// 06/07/2020 ECU request the required channel number
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.sliderChoice (this,
												 activity,
												 getString (R.string.title_tv_channel_change),
												 getString (R.string.summary_tv_channel_change),
												 R.drawable.television,
												 null,
												 TVChannelsSelected.get (theChannel).channelNumber,
												 1,
												 800,
												 getString (R.string.set_channel_number),
												 Utilities.createAMethod (TVChannelsActivity.class,"SetChannelNumberChangeAction",0),
												 getString (R.string.cancel),
												 null,
												 true);
		// -------------------------------------------------------------------------
	}
	// ============================================================================
	public void SetChannelNumberChangeAction (int theChannelNumber)
	{
		// ------------------------------------------------------------------------
		// 06/07/2020 ECU at this point have the necessary data to be able to set
		//                the channel number, that will be used with the remote
		//                controller, to be associated with the EPG channel name
		// ------------------------------------------------------------------------
		Television.changeStoredTVChannels (this,TVChannelsSelected.get (channelNumber).channelName,theChannelNumber);
		// ------------------------------------------------------------------------
		// 06/07/2020 ECU confirm the change to the user
		// ------------------------------------------------------------------------
		Utilities.popToastAndSpeak(String.format(getString (R.string.tv_channel_change_format),
						TVChannelsSelected.get (channelNumber).channelName,theChannelNumber),true);
		// ------------------------------------------------------------------------
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
				theTextView.append (TVChannelsSelected.get(channel).Print (dateString) + StaticData.NEWLINE);
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
					channelsTextView.append ("Channel : " + words [0] + "  Name : " + words [1] + StaticData.NEWLINE);

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
										Utilities.createAMethod (TVChannelsActivity.class,"TVChannelDataJSONMethod",StaticData.BLANK_STRING)));
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
					// 14/11/2017 ECU add 'true' to indicate async write
					// -------------------------------------------------------------
					TVChannelsSelected.get (theChannel).writeToDisk (true);
					// -------------------------------------------------------------
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
									Utilities.createAMethod (TVChannelsActivity.class,"TVChannelDataJSONMethod",StaticData.BLANK_STRING)));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================



	// =============================================================================
	void initialiseDisplay (Activity theActivity,boolean theSelectionFlag)
	{
		// -------------------------------------------------------------------------
		// 28/04/2018 ECU created to generate the display of stored documents
		//			  ECU theSelectionFlag indicates which type of display is
		//                wanted
		//                     false .......... ALL channels
		//                     true ........... only SELECTED channels
		// -------------------------------------------------------------------------
		if (!theSelectionFlag)
		{
			// ---------------------------------------------------------------------
			// 28/04/2018 ECU display ALL TV channels
			// ---------------------------------------------------------------------
			listViewSelector = new ListViewSelector (theActivity,
													 R.layout.channels_row,
													 "BuildTheChannelsList",
													 PublicData.storedData.tvChannelsSorted,
													 null,
													 null,
													 null,
													 getString (R.string.select),
													 "SelectAction",
													 null,
													 null);
			// ----------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 28/04/2018 ECU display only SELECTED TV channels
			// ---------------------------------------------------------------------
			listViewSelector = new ListViewSelector (theActivity,
					 								 R.layout.channels_row,
					 								 "BuildTheSelectedChannelsList",
					 								 PublicData.storedData.tvChannelsSorted,
					 								 null,
					 								 null,
					 								 null,
					 								 getString (R.string.deselect),
					 								 "DeselectAction",
					 								 null,
					 								 null);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void refreshDisplay (boolean theSelectionFlag)
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
			initialiseDisplay (this,theSelectionFlag);
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

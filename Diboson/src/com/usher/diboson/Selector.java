package com.usher.diboson;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Selector extends Activity 
{
	/* ============================================================================= */
	// 23/03/2014 ECU created
	//            ECU provide a generic selector of an object
	// 09/06/2015 ECU put in some swipe handling
	// 11/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 21/11/2015 ECU added OBJECT_BARCODES
	// 19/12/2015 ECU changed the ReturnMethod from () to (0) because of a change
	//                in MethodDefinition which means that () means no arguments
	//                whereas it used to mean an integer argument
	//            ECU added OBJECT_APPOINTMENTS
	// 07/03/2016 ECU added OBJECT_MEDICATION_TIMERactivity is created
	// 05/08/2016 ECU added 'drawableInitial' which is the resource ID of a drawable
	//                that is to be displayed when the 
	// 15/08/2016 ECU changed the handling of the 'back key' - used to process
	//                the 'onKeyDown' event and then detect the key but was then
	//                getting warnings to do with the event associated with 'key up'
	//                on the 'back key' being cancelled because the window has lost
	//                focus so changed to use onBackPressed. The actual error seems
	//                to differ for different devices - the above was for the MotoG
	//                on the Samsung Tablet the error relates to the view being lost
	//                (cancelling event due to no window focus : mStopped=false
	//                mHasWindowFocus=false).
	// 24/09/2016 ECU added OBJECT_LIQUIDS
	// 18/10/2016 ECU added OBJECT_DOCUMENTS
	// 02/03/2017 ECU added OBJECT_DAILY_SUMMARIES
	// ----------------------------------------------------------------------------- 
	// -----------------------------------------------------------------------------
	// I M P O R T A N T   read this to understand how the class works
	// =================
	// This class provides a generic means of using a 'listview' displayed using
	// a custom adapter (CustomListViewAdapter) and using a configurable row layout,
	// which if not supplied will be defaulted to 'R.layout.selector_row'.
	//
	// Different layouts can be specified for the row but whatever design is chosen
	// must be based on 'R.layout.selector_row' with the following elements 
	//
	//     	ImageView	list_button_entry_imageview
	//	   	TextView	list_button_entry_textview
	//	   	TextView	list_button_entry_textview2
	//	   	TextView	list_button_entry_textview3
	//		ImageButton	list_button_help
	//		Button		list_button_custom
	//		Button		list_button_phone
	//
	// The custom adapter takes its data from an 'ArrayList<ListItem>' of items.
	//
	// The aim of this class is to provide flexibility in the way that clickable
	// elements are actioned. This is achieved by the calling activity defining
	// Methods which are passed through as arguments. The following details the
	// customisable features.
	//
	// When the activity is started there are a number of parameters which can be
	// supplied in extras. These are as follows :-
	//
	// MainActivity.PARAMETER_OBJECT_TYPE  		'int' 
	//			defines the type of object being handled
	//			Possible values are :-
	//					MainActivity.OBJECT_DAYS
	//					MainActivity.OBJECT_DOSES
	//					MainActivity.OBJECT_MEDICATION
	//					MainActivity.OBJECT_SELECTOR
	//					MainActivity.OBJECT_TIMER
	//					MainActivity.OBJECT_WEMO_TIMER
	//					MainActivity.OBJECT_WEMO_TIMERS
	// MainActivity.PARAMETER_BACK_KEY			'boolean'
	//          specifies whether the 'back' key causes the activity to 'finish'
	// MainActivity.PARAMETER_MEDICATION		'int'
	//			specifies the index of a medication being processed
	// MainActivity.PARAMETER_DOSE				'int'
	//			specifies the index of a medication dose being handled
	// MainActivity.PARAMETER_INITIAL_POSITION	'int'
	//			specifies the initial starting position within the supplied list
	// MainActivity.PARAMETER_SORT				'boolean'
	//			specifies whether the supplied list is to be sorted or not
	// MainActivity.PARAMETER_METHOD			'(MethodDefinition<?>)'
	//			defines the method to be actioned when an item is selected
	// MainActivity.PARAMETER_BACK_METHOD		'(MethodDefinition<?>)'
	//          defines the method to be actioned when the BACK key is pressed
	//
	// Note :- apart from PARAMETER_OBJECT_TYPE the other arguments are optional.
	//
	// Depending on the value of PARAMETER_OBJECT_TYPE then the following will
	// be set up, not all of which are mandatory :-
	//			the ArrayList of data will be built
	//          the class to be run when an item is 'clicked'
	//          the layout (R.layout..) to be used for each row
	//          the drawable that is to be used in the row's ImageView
	//          the Method to be actioned when the 'list_button_custom' button
	// 				is pressed
	//			the legend on the 'list_button_custom' button
	//          the Method to be actioned when the 'list_button_phone' button
	//				is pressed
	//			the activity (class) to be started if the above Methods are not
	//          	defined
	//          the Method to be actioned when the 'BACK' key is pressed
	//          the Method to be actioned when the ''image' is pressed
	//			the Method to be actioned when the 'help icon' key is pressed
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	/* ============================================================================= */
	//private final static String TAG = "Selector";
	/* ============================================================================= */
	public static CustomListViewAdapter customListViewAdapter; 
																// 08/02/2015 ECU changed
																//                to public
	// =============================================================================
			static	boolean				backKeyAllowed 	= false;		// 24/03/2014 ECU added
			static	Method 				backMethod 		= null;			// 28/02/2015 ECU added	
			static 	Class <?> 			classToRun		= null;			// 07/03/2016 ECU added the null preset
	public 	static 	Context				context;
			static 	String				customLegend	= null;			// 24/03/2014 ECU added
			static 	Method				customMethod    = null;			// 24/03/2014 ECU added
			static 	int					doseIndex		= StaticData.NO_RESULT;
																		// 25/03/2014 ECU added
			static  int				    drawableInitial	= StaticData.NO_RESULT;
																		// 05/08/2016 ECU added
			static	Method 				editMethod 		= null;			// 27/02/2015 ECU added	
																		// 18/10/2016 ECU changed to 'static'
			static  boolean             finishOnSelect	= true;			// 08/03/2016 ECU added
			static 	Method 				helpMethod 		= null;			// 15/06/2015 ECU added	
					int					imageDefault	= R.drawable.medication;
																		// 07/02/2015 ECU added
					Method				imageMethod		= null;			// 31/03/2014 ECU added
					int					initialPosition	= StaticData.NO_RESULT;
																		// 27/02/2015 ECU added
			static 	ArrayList<ListItem> listItems		= new ArrayList<ListItem>();
			static	ListView			listView;
			static  Method				longSelectMethod= null;			// 21/11/2015 ECU added
			static 	int					medicationIndex	= StaticData.NO_RESULT;
																		// 25/03/2014 ECU added
			static 	int					objectType 		= StaticData.NO_RESULT;
					boolean				paused			= false;		// 01/04/2014 ECU added
			static 	int					rowLayoutID;					// 25/03/2014 ECU added
					int					selectedIndex 	= StaticData.NO_RESULT;
			static  Method				selectMethod	= null;			// 25/03/2014 ECU added
			static 	SelectorParameter	selectorParameter 
														= null;			// 10/06/2015 ECU added
			static 	boolean				sortList		= false;		// 30/03/2014 ECU added
			static 	Method				swipeMethod		= null;			// 09/06/2015 ECU added
			static 	int					type			= StaticData.NO_RESULT;
																		// 29/03/2014 ECU added
	/* ============================================================================= */
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{	
		super.onCreate(savedInstanceState);	
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 11/10/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 24/03/2014 ECU set up consistent activity parameters
			// 30/03/2014 ECU added the 'true' option for full screen
			// 08/04/2014 ECU changed to use the variable
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_list);
			// ---------------------------------------------------------------------
			// 22/11/2015 ECU preset some of the static methods
			// 05/08/2016 ECU added drawableInitial
			// 18/10/2016 ECU added the edit method
			// 02/03/2017 ECU added finishOnSelect
			// ---------------------------------------------------------------------
			backMethod			=	null;
			customMethod		=	null;
			drawableInitial		=   StaticData.NO_RESULT;
			editMethod			= 	null;
			finishOnSelect		= 	true;
			helpMethod			=	null;
			longSelectMethod	=	null;
			selectMethod		=	null;
			swipeMethod			=	null;
			// ---------------------------------------------------------------------
			// 24/03/2014 ECU try and retrieve the parameter to indicate what is being
			//                selected. Do it this way rather than trying to send an
			//                object which would require setting up a Parcelable
			// ---------------------------------------------------------------------	
			Bundle extras = getIntent().getExtras();
	    
			if (extras != null)
			{
 	   			// -----------------------------------------------------------------
 	   			// 24/03/2014 ECU get the medication index from the intent
 	   			// -----------------------------------------------------------------
 	   			objectType	 = extras.getInt (StaticData.PARAMETER_OBJECT_TYPE,StaticData.NO_RESULT); 
 	   			// -----------------------------------------------------------------  
 	   			// 24/03/2014 ECU indicate whether the 'back key' is allowed
 	   			// -----------------------------------------------------------------
 	   			backKeyAllowed = extras.getBoolean(StaticData.PARAMETER_BACK_KEY, false);
 	   			// -----------------------------------------------------------------
 	   			medicationIndex = extras.getInt (StaticData.PARAMETER_MEDICATION, StaticData.NO_RESULT);
 	   			doseIndex 		= extras.getInt (StaticData.PARAMETER_DOSE, StaticData.NO_RESULT);
 	   			// -----------------------------------------------------------------
 	   			// 27/02/2015 ECU check if an initial position specified
 	   			// -----------------------------------------------------------------
 	   			initialPosition	= extras.getInt (StaticData.PARAMETER_INITIAL_POSITION,StaticData.NO_RESULT);
 	   			// -----------------------------------------------------------------
 	   			// 30/03/2014 ECU check if the 'sort' parameter included
 	   			// -----------------------------------------------------------------
 	   			sortList = extras.getBoolean (StaticData.PARAMETER_SORT, false);
 	   			// -----------------------------------------------------------------
 	   			// 25/03/2014 ECU get the method details for a 'select method'
 	   			// -----------------------------------------------------------------
 	   			MethodDefinition<?> methodDefinition = (MethodDefinition<?>) extras.getSerializable (StaticData.PARAMETER_METHOD);
 	   			// -----------------------------------------------------------------
 	   			if (methodDefinition != null)
 	   				selectMethod					  =  methodDefinition.ReturnMethod (0);
 	   			// -----------------------------------------------------------------
 	   			// 28/02/2015 ECU get the method details for the 'BACK method'
 	   			// -----------------------------------------------------------------
 	   			methodDefinition = (MethodDefinition<?>) extras.getSerializable (StaticData.PARAMETER_BACK_METHOD);
 	   			// -----------------------------------------------------------------
 	   			if (methodDefinition != null)
 	   				backMethod					  =  methodDefinition.ReturnMethod (0);
 	   			// -----------------------------------------------------------------
 	   			// 09/06/2015 ECU get the method details for the 'swipe method'
 	   			// -----------------------------------------------------------------
 	   			methodDefinition = (MethodDefinition<?>) extras.getSerializable (StaticData.PARAMETER_SWIPE_METHOD);
 	   			// -----------------------------------------------------------------
 	   			if (methodDefinition != null)
 	   				swipeMethod					  =  methodDefinition.ReturnMethod (0);
 	   			// -----------------------------------------------------------------
 	   			// 13/06/2015 ECU get the method details for the 'image handler'
 	   			// -----------------------------------------------------------------
 	   			methodDefinition = (MethodDefinition<?>) extras.getSerializable (StaticData.PARAMETER_IMAGE_HANDLER);
 	   			// -----------------------------------------------------------------
 	   			if (methodDefinition != null)
 	   				imageMethod					  =  methodDefinition.ReturnMethod (0);
 	   			// -----------------------------------------------------------------
 	   			// 15/06/2015 ECU get the method details for the 'help icon'
 	   			// -----------------------------------------------------------------
 	   			methodDefinition = (MethodDefinition<?>) extras.getSerializable (StaticData.PARAMETER_HELP_METHOD);
 	   			// -----------------------------------------------------------------
 	   			if (methodDefinition != null)
 	   				helpMethod					  =  methodDefinition.ReturnMethod (0);
 	   			// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 23/03/2014 ECU remember the context
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------		 
			listView = (ListView) findViewById (R.id.grid_list_view);	
			// ---------------------------------------------------------------------
			// 09/06/2015 ECU use the side swipe action to cause a deletion
			// ---------------------------------------------------------------------
			if (swipeMethod != null)
			{
				SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener (
								listView,
								new SwipeDismissListViewTouchListener.OnDismissCallback () 
				{
					@Override
					public void onDismiss (ListView listView,int [] reverseSortedPositions) 
					{
						try 
						{
							// -----------------------------------------------------
							// 09/06/2015 ECU the position that is returned depends on
							//                whether the list has been sorted
							//                if the list has not been sorted then return
							//                actual position pressed. If sorted then
							//                return the index held within the record
							// -----------------------------------------------------
							// 10/06/2015 ECU store the legend in the selector object
							// -----------------------------------------------------
							ShoppingActivity.selectorParameter.name = listItems.get (reverseSortedPositions [0]).legend;
							// -----------------------------------------------------
							swipeMethod.invoke (null, new Object [] {sortList ? listItems.get (reverseSortedPositions [0]).index 
																			  : reverseSortedPositions [0]});
							// -----------------------------------------------------
						}
						catch (Exception theException) 
						{
	               					
						}
					}
				});
				// -----------------------------------------------------------------
				listView.setOnTouchListener (touchListener);
				listView.setOnScrollListener (touchListener.makeScrollListener());
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 25/03/2014 ECU set up the default row layout
			// ---------------------------------------------------------------------	
			rowLayoutID = R.layout.selector_row;
			// ---------------------------------------------------------------------
			// 27/02/2015 ECU set up any default methods 
			// ---------------------------------------------------------------------
			editMethod = Utilities.createAMethod (Selector.class,"EditEntry",0);
			// ---------------------------------------------------------------------
			// 26/01/2014 ECU play around with a custom grid
			// 23/03/2014 ECU add the photo into the displayed list
			// 24/03/2014 ECU build up parameters dependent of the type of object
			// ---------------------------------------------------------------------
			switch (objectType)
			{
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case StaticData.OBJECT_DAYS:
					// -------------------------------------------------------------
					// 24/03/2014 ECU build the displayed list from medication records
					//            ECU changed to get Absolute path
					// 30/03/2014 ECU added the index as an argument
					// 01/04/2014 ECU added the call to BuildList
					// -------------------------------------------------------------
					listItems = DoseDaily.BuildList (medicationIndex);
					// -------------------------------------------------------------
					// 24/03/2014 ECU set up the class to run when item selected
					// -------------------------------------------------------------
					classToRun = MedicationInput.class;
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case StaticData.OBJECT_DOSES:
					// -------------------------------------------------------------
					// 24/03/2014 ECU build the displayed list from medication records
					//            ECU changed to get Absolute path
					// 25/03/2014 ECU change the formatting
					// 30/03/2014 ECU add 'theIndex' as an argument
					// 01/04/2014 ECU added the call to BuildList
					// -------------------------------------------------------------
					listItems = DoseTime.BuildList (medicationIndex,doseIndex);
					// -------------------------------------------------------------
					// 24/03/2014 ECU set up the class to run when item selected
					// -------------------------------------------------------------
					classToRun = MedicationInput.class;
					// -------------------------------------------------------------
					rowLayoutID = R.layout.dose_time_row;
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case	StaticData.OBJECT_MEDICATION:
					// -------------------------------------------------------------
					// 24/03/2014 ECU build the displayed list from medication records
					//            ECU changed to get Absolute path
					// 30/03/2014 ECU added the index as an argument
					// 01/04/2014 ECU added the call to BuildList
					// -------------------------------------------------------------
					listItems = MedicationDetails.BuildList();			
					// -------------------------------------------------------------
					// 24/03/2014 ECU set up the class to run when item selected
					// -------------------------------------------------------------
					classToRun = MedicationInput.class;
					// -------------------------------------------------------------
					// 24/03/2014 ECU set up custom details
					// -------------------------------------------------------------
					customMethod = Utilities.createAMethod (Selector.class,"CustomMethod",0);
					customLegend = "New";
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case    StaticData.OBJECT_AGENCIES:
				case    StaticData.OBJECT_APPOINTMENTS:
				case	StaticData.OBJECT_BARCODES:
				case	StaticData.OBJECT_CARE_PLANS:
				case	StaticData.OBJECT_CARE_VISITS:
				case    StaticData.OBJECT_CARERS:
				case    StaticData.OBJECT_CHANNELS:
				case    StaticData.OBJECT_DAILY_SUMMARIES:
				case    StaticData.OBJECT_DOCUMENTS:
				case    StaticData.OBJECT_LIQUIDS:
				case    StaticData.OBJECT_NAMED_ACTIONS:
				case    StaticData.OBJECT_NOTIFICATIONS:
				case    StaticData.OBJECT_SELECTED_CHANNELS:
				case	StaticData.OBJECT_SELECTOR:
				case    StaticData.OBJECT_SHOPPING:
					// -------------------------------------------------------------
					// 29/03/2014 ECU build the displayed list from shopping product records
					// 10/06/2015 ECU changed to use the common variable rather than a local
					//                instance
					// 28/08/2015 ECU added ....CARERS and ....AGENCIES
					// 29/08/2015 ECU added ....CARE_PLANS 
					//            ECU added ....CARE_VISITS
					// 18/09/2015 ECU added ....CHANNELS and ....SELECTED_CHANNELS
					// 11/10/2015 ECU added ....SHOPPING
					// 21/11/2015 ECU added ....BARCODES
					// 19/12/2015 ECU added ....APPOINTMENTS
					// 24/09/2016 ECU added ....LIQUIDS
					// 18/10/2016 ECU added ....DOCUMENTS
					// 02/03/2017 ECU added ....DAILY_SUMMARIES
					// -------------------------------------------------------------
					// 01/09/2015 ECU change the default image when not replaced
					// -------------------------------------------------------------
					imageDefault 	= R.drawable.no_photo;
					// -------------------------------------------------------------
					// 25/09/2016 ECU put in any local tailoring
					// -------------------------------------------------------------
					switch (objectType)
					{
						// ---------------------------------------------------------
						case StaticData.OBJECT_LIQUIDS:
								imageDefault = R.drawable.liquid;
								break;
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------
	 	   			selectorParameter 
	 	   						= (SelectorParameter) extras.getSerializable (StaticData.PARAMETER_SELECTOR);
	 	   			// -------------------------------------------------------------
	 	   			// 31/08/2015 ECU changed to use a method to set up other variables
	 	   			// -------------------------------------------------------------
	 	   			SetFromSelectorParameter (selectorParameter);
	 	   			// -------------------------------------------------------------
	 	   			break;
	 	   		// -----------------------------------------------------------------
	 	   		// -----------------------------------------------------------------
				case	StaticData.OBJECT_TIMER:
					// -------------------------------------------------------------
					// 07/02/2015 ECU build the displayed list from timer records
					// -------------------------------------------------------------
					listItems = AlarmData.BuildList();			
					// -------------------------------------------------------------
					// 07/02/2015 ECU set up the class to run when item selected
					// -------------------------------------------------------------
					classToRun = TimerActivity.class;
					// -------------------------------------------------------------
					// 07/02/2015 ECU define the specific row and image
					// -------------------------------------------------------------
					rowLayoutID 	= R.layout.alarm_row;
					imageDefault 	= R.drawable.timer;
					// -------------------------------------------------------------
					// 05/06/2015 ECU changed to use resource string
					// -------------------------------------------------------------
					customMethod = Utilities.createAMethod (TimerActivity.class,"DeleteTheTimer",0);
					customLegend = getString (R.string.delete);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case	StaticData.OBJECT_WEMO_TIMER:
					// -------------------------------------------------------------
					// 25/02/2015 ECU build the displayed list from timer records
					// -------------------------------------------------------------
					listItems = WeMoTimerActivity.BuildList();			
					// -------------------------------------------------------------
					// 07/02/2015 ECU set up the class to run when item selected
					// -------------------------------------------------------------
					classToRun = WeMoTimerActivity.class;
					// -------------------------------------------------------------
					// 25/02/2015 ECU define the specific row and image
					// -------------------------------------------------------------
					rowLayoutID 	= R.layout.wemo_timer_row;
					imageDefault 	= R.drawable.timer;
					// -------------------------------------------------------------
					// 05/06/2015 ECU changed to use resource string
					// -------------------------------------------------------------
					customMethod = Utilities.createAMethod (WeMoTimerActivity.class,"AddATimer",0);
					customLegend = getString (R.string.add);
					// -------------------------------------------------------------
					// 27/02/2015 ECU by setting the editMethod to null tells the adapter
					//                not to show the button
					// -------------------------------------------------------------
					editMethod 	 = null;
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
				case	StaticData.OBJECT_WEMO_TIMERS:
					// -------------------------------------------------------------
					// 25/02/2015 ECU build the displayed list from timer records
					// -------------------------------------------------------------
					listItems = WeMoTimerActivity.BuildListForDevice();			
					// -------------------------------------------------------------
					// 07/02/2015 ECU set up the class to run when item selected
					// -------------------------------------------------------------
					classToRun = WeMoTimerActivity.class;
					// -------------------------------------------------------------
					// 25/02/2015 ECU define the specific row and image
					// -------------------------------------------------------------
					rowLayoutID 	= R.layout.wemo_timer_row;
					imageDefault 	= R.drawable.timer;
					// -------------------------------------------------------------
					// 05/06/2015 ECU changed to use resource string
					// -------------------------------------------------------------
					customMethod 	= Utilities.createAMethod (WeMoTimerActivity.class,"DeleteTimer",0);
					editMethod  	= Utilities.createAMethod (WeMoTimerActivity.class,"EditTimer",0);
					customLegend 	= getString (R.string.delete);
					// -------------------------------------------------------------
					break;
			}
			// ---------------------------------------------------------------------
			// 30/03/2014 ECU try and sort the displayed entries - if requested
			// ---------------------------------------------------------------------
			if (sortList)
				Collections.sort (listItems);
			// ---------------------------------------------------------------------
			// 25/03/2014 ECU default to use 'rowLayoutID'
			// ---------------------------------------------------------------------
			customListViewAdapter = new CustomListViewAdapter (this,rowLayoutID,listItems); 
			// ---------------------------------------------------------------------
			// 07/02/2014 ECU change any defaults
			// ---------------------------------------------------------------------
			Method helpMethod = Utilities.createAMethod (Selector.class,"HandleHelp",0);
			// ---------------------------------------------------------------------
			// 31/03/2014 ECU added temporarily to define image method
			// 13/06/2015 ECU added the check on null
			// ---------------------------------------------------------------------
			if (imageMethod == null)
				imageMethod = Utilities.createAMethod (ShoppingActivity.class,"ImageHandler",0);
			// ---------------------------------------------------------------------
			// 08/02/2014 ECU added the 'deleteMethod' in the method call
			// 24/03/2014 ECU added the 'custom' arguments
			// 31/03/2014 ECU added methodImage argument
			// 07/04/2015 ECU changed to use imageDefault instead of R.drawable.medication
			// ---------------------------------------------------------------------
			customListViewAdapter.ChangeDefaults (imageDefault,helpMethod,editMethod,customMethod,customLegend,imageMethod);
			// ---------------------------------------------------------------------
			listView.setAdapter (customListViewAdapter);
			// ---------------------------------------------------------------------
			// 05/08/2016 ECU check whether an 'initial drawable' is to be displayed
			//                when this activity is created - the idea is that the
			//                drawable has information that will help the user
			// ---------------------------------------------------------------------
			if (drawableInitial != StaticData.NO_RESULT)
			{
				// -----------------------------------------------------------------
				// 05/08/2016 ECU a drawable has been specified so display it
				// -----------------------------------------------------------------
				Utilities.DisplayADrawable (this,drawableInitial);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 23/03/2014 ECU handle the clicking of an item which just returns the
			//                position of the item being clicked to the caller
			// ---------------------------------------------------------------------
			listView.setOnItemClickListener (new OnItemClickListener() 
			{
				@Override
				public void onItemClick (AdapterView<?> adapterView, View view,int position, long id) 
				{	
					// -------------------------------------------------------------
					// 23/03/2014 ECU set up the intent for returning the result
					// 24/03/2014 ECU changed from _SELECT to _SELECTION
					// 30/03/2014 ECU changed to use the index embedded in the listItems
					// -------------------------------------------------------------
					Intent resultIntent = new Intent ();
					resultIntent.putExtra (StaticData.PARAMETER_SELECTION,listItems.get(position).index);
					setResult (RESULT_OK,resultIntent);	
					// -------------------------------------------------------------
					// 25/03/2014 ECU check if there is a method to invoke on selecting
					//                an item
					// 07/06/2015 ECU put in the check on 'sort'
					// -------------------------------------------------------------
					if (selectMethod != null)
					{
						try 
						{
							// -----------------------------------------------------
							// 07/06/2015 ECU the position that is returned depends on
							//                whether the list has been sorted
							//                if the list has not been sorted then return
							//                actual position pressed. If sorted then
							//                return the index held within the record
							// 10/06/2015 ECU changed to use SelectedItem object
							// -----------------------------------------------------
							selectMethod.invoke (null, new Object [] {sortList ? listItems.get(position).index 
								                                               : position});
							// -----------------------------------------------------
						}
						catch (Exception theException) 
						{
						}
						// ---------------------------------------------------------
						// 23/03/2014 ECU can just finish the activity
						// 13/06/2015 ECU moved the selection into the 'null' check
						// 08/03/2016 ECU put in the check on whether the activity
						//                should be 'finish'ed
						// ---------------------------------------------------------
						if (finishOnSelect)
							finish ();
						// ---------------------------------------------------------
					}
					else
					{
						// ---------------------------------------------------------
						// 07/03/2015 ECU if there is no selection method
						// ---------------------------------------------------------
						
						// ---------------------------------------------------------
					}
					// -------------------------------------------------------------			
				}
			});
			// ---------------------------------------------------------------------
			// 21/11/2015 ECU add a listener for a long click on an item
			// ---------------------------------------------------------------------
			listView.setOnItemLongClickListener (new OnItemLongClickListener() 
			{
				@Override
				public boolean onItemLongClick (AdapterView<?> adapterView, View view,int position, long id)
				{
					if (longSelectMethod != null)
					{
						try 
						{
							// -----------------------------------------------------
							// 07/06/2015 ECU the position that is returned depends on
							//                whether the list has been sorted
							//                if the list has not been sorted then return
							//                actual position pressed. If sorted then
							//                return the index held within the record
							// 10/06/2015 ECU changed to use SelectedItem object
							// -----------------------------------------------------
							longSelectMethod.invoke (null, new Object [] {sortList ? listItems.get(position).index 
								                                           		   : position});
							// -----------------------------------------------------
							// 21/11/2015 ECU and just finish the activity
							//            ECU at the moment this is only used by the
							//                BarCodeActivity which will be envoking the
							//                scanner and will restart this activity
							//                when it requires additional user input
							// 23/11/2015 ECU removed the 'finish' because the method
							//                called will be responsible to terminating
							//                this activity as required.
							// -----------------------------------------------------
							// 21/11/2015 ECU finish ();
							// -----------------------------------------------------
							return true;
						}
						catch (Exception theException) 
						{
							return false;
						}
						// ---------------------------------------------------------
					}
					else
						return false;
				}			
			});
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
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return true;
	}
	/* ============================================================================= */
	public void onDestroy()
	{	
		super.onDestroy();
	}	
	/* ============================================================================= */
	@Override
	public void onBackPressed () 
	{
		// -------------------------------------------------------------------------
		// 24/03/2014 ECU added to process the back key
		//			  ECU included the 'backKeyAllowed' option
		// 15/08/2016 ECU IMPORTANT - changed to this method from 'onKeyDown' - see
		//                =========   the notes at the top of this class
		// -------------------------------------------------------------------------
	    if (!backKeyAllowed) 
	    {	  
	    	// ---------------------------------------------------------------------
	    	// 08/04/2014 ECU changed to use resource
	    	// ---------------------------------------------------------------------
	       	Utilities.popToast (getString (R.string.item_must_be_selected),true);
	       	// ---------------------------------------------------------------------
	       	// 18/09/2013 ECU indicate that the key has been processed
	       	// ---------------------------------------------------------------------
	    }
	    else
	    {
	    	// ---------------------------------------------------------------------
	    	// 28/02/2015 ECU check if a method has to be actioned on the BACK key
	    	// ---------------------------------------------------------------------
	    	if (backMethod != null)
	    	{
	    		try 
	    		{
	    			// -------------------------------------------------------------
	    			// 28/02/2015 ECU action the defined method
	    			// -------------------------------------------------------------
					backMethod.invoke (null, new Object [] {0});
					// -------------------------------------------------------------
					finish ();
					// -------------------------------------------------------------
				} 
	    		catch (Exception theException) 
	    		{
				} 
	    	}
	    	// ---------------------------------------------------------------------
	    	// 15/08/2016 ECU Note - pass the event through for further processing
	    	// ---------------------------------------------------------------------
	        super.onBackPressed ();
	        // ---------------------------------------------------------------------
	    }
	}
	/* ============================================================================= */
	@Override 
	protected void onPause() 
	{
		// -------------------------------------------------------------------------
		// 01/04/2014 ECU use the 'paused' flag because initially get an 'onResume'
		//                which I do not want to process
		// -------------------------------------------------------------------------
		paused = true;
		// -------------------------------------------------------------------------
	   	super.onPause(); 
	} 
	/* ============================================================================= */
	@Override 
	protected void onResume() 
	{ 	
		// ------------------------------------------------------------------------
		// 01/04/2014 ECU use the 'paused' flag because initially get an 'onResume'
		//                which I do not want to process
		// ------------------------------------------------------------------------
		if (paused)
		{
			paused = false;

			// ---------------------------------------------------------------------
			// 01/04/2014 ECU rebuild the displayed list for the SELECTOR object
			//                only
			// 27/02/2015 ECU changed to use the method rather than having code
			//                in-line
			// ---------------------------------------------------------------------
			Rebuild ();	
			// ---------------------------------------------------------------------
		}

	   	super.onResume(); 
	} 
	/* ============================================================================ */
	public static void CustomMethod (int thePosition)
	{
		// -------------------------------------------------------------------------
		// 24/03/2014 ECU include 'thePosition' as an argument
		// 13/06/2015 ECU put in the 'null' check
		// -------------------------------------------------------------------------
		if (classToRun != null)
		{
			Intent intent = new Intent (context,classToRun);
			context.startActivity (intent);
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void EditEntry (int thePosition)
	{		
		// -------------------------------------------------------------------------
		// 24/03/2014 ECU include 'thePosition' as an argument
		// 30/03/2014 ECU thePosition received will be the index that is stored in
		//                the record rather than the position as displayed
		// 13/06/2015 ECU do the check on 'null'
		// -------------------------------------------------------------------------
		if (classToRun != null)
		{
			// ---------------------------------------------------------------------
			// 27/11/2014 ECU feed through the medicationIndex if appropriate
			// ---------------------------------------------------------------------
			Intent intent = new Intent (context,classToRun);
			intent.putExtra (StaticData.PARAMETER_SELECTION,
					(medicationIndex == StaticData.NO_RESULT) ? thePosition 
															  : medicationIndex);
			// ---------------------------------------------------------------------
			// 29/03/2014 ECU decide whether a type is to be passed across
			// ---------------------------------------------------------------------
			if (type != StaticData.NO_RESULT)
				intent.putExtra(StaticData.PARAMETER_TYPE,type);
			// ---------------------------------------------------------------------	
			context.startActivity (intent);
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	public static void HandleHelp (int thePosition)
	{
		// -------------------------------------------------------------------------
		// 08/04/2014 ECU use resource
		// -------------------------------------------------------------------------
		if (helpMethod == null)
		{
			Utilities.popToast (context.getString (R.string.handle_help));
		}
		else
		{
			try 
			{
				// ---------------------------------------------------------
				// 15/06/2015 ECU the position that is returned depends on
				//                whether the list has been sorted
				//                if the list has not been sorted then return
				//                actual position pressed. If sorted then
				//                return the index held within the record
				// ---------------------------------------------------------
				helpMethod.invoke (null, new Object [] {sortList ? listItems.get(thePosition).index 
						                                         : thePosition});
				// ---------------------------------------------------------
			}
			catch (Exception theException) 
			{
			}
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public static void Rebuild ()
	{
		switch (objectType)
		{
			// =====================================================================
			case StaticData.NO_RESULT:
				// -----------------------------------------------------------------
				// 24/11/2016 ECU added just in case the method called before
				//                initialisation has been occurred
				// -----------------------------------------------------------------
				return;
			// =====================================================================
			case StaticData.OBJECT_AGENCIES:
				listItems = AgencyActivity.BuildTheAgenciesList();
				break;
			// =====================================================================
			case StaticData.OBJECT_APPOINTMENTS:
				listItems = AppointmentsActivity.BuildTheAppointmentsList();
				break;
			// =====================================================================
			case StaticData.OBJECT_BARCODES:
				listItems = BarCodeActivity.BuildTheBarCodeList();
				break;
			// =====================================================================
			case StaticData.OBJECT_CARE_PLANS:
				listItems = CarePlanActivity.BuildTheDailyCarePlanList();
				break;
			// =====================================================================
			case StaticData.OBJECT_CARE_VISITS:
				listItems = CarePlanActivity.BuildTheDailyCarePlanVisitsList();
				break;
			// =====================================================================
			case StaticData.OBJECT_CARERS:
				listItems = CarerActivity.BuildTheCarersList ();
				break;
			// =====================================================================
			case StaticData.OBJECT_CHANNELS:
				listItems = TVChannelsActivity.BuildTheChannelsList ();
				break;
			// =====================================================================
			case StaticData.OBJECT_DAILY_SUMMARIES:
				// -----------------------------------------------------------------
				// 02/03/2017 ECU added to rebuild the daily summaries information
				// -----------------------------------------------------------------
				listItems = DailySummaryActivity.BuildTheDaysList ();
				// -----------------------------------------------------------------
				break;
			// =====================================================================
			case StaticData.OBJECT_DAYS:
				listItems = DoseDaily.BuildList(medicationIndex);
				break;
			// =====================================================================
			case StaticData.OBJECT_DOCUMENTS:
				listItems = DocumentsActivity.BuildTheDocumentsList ();
				break;
			// =====================================================================
			case StaticData.OBJECT_DOSES:
				listItems = DoseTime.BuildList(medicationIndex, doseIndex);
				break;
			// =====================================================================
			case StaticData.OBJECT_LIQUIDS:
				// -----------------------------------------------------------------
				// 24/09/2016 ECU added to rebuild the liquid information
				// -----------------------------------------------------------------
				listItems = LiquidActivity.BuildList ();
				// -----------------------------------------------------------------
				break;
			// =====================================================================	
			case StaticData.OBJECT_MEDICATION:
				listItems = MedicationDetails.BuildList();
				break;
			// =====================================================================
			case StaticData.OBJECT_NAMED_ACTIONS:
				listItems = NamedActionsActivity.BuildTheNamedActionsList();
				break;
			// =====================================================================
			case StaticData.OBJECT_NOTIFICATIONS:
				// -----------------------------------------------------------------
				// 13/07/2016 ECU added
				// -----------------------------------------------------------------
				listItems = NotificationsActivity.BuildTheNotificationsList ();
				// -----------------------------------------------------------------
				break;
			// =====================================================================
			case StaticData.OBJECT_SELECTED_CHANNELS:
				listItems = TVChannelsActivity.BuildTheSelectedChannelsList ();
				break;
			// =====================================================================
			case StaticData.OBJECT_SELECTOR:
				listItems = ShoppingActivity.RebuildList ();
				break;
			// =====================================================================
			case StaticData.OBJECT_TIMER:
				// -----------------------------------------------------------------
				// 07/02/2015 ECU 
				// -----------------------------------------------------------------
				listItems = AlarmData.BuildList();
				break;
			// =====================================================================
			case StaticData.OBJECT_WEMO_TIMER:
				// -----------------------------------------------------------------
				// 25/02/2015 ECU 
				// -----------------------------------------------------------------
				listItems = WeMoTimerActivity.BuildList();
				break;
			// =====================================================================	
			case StaticData.OBJECT_WEMO_TIMERS:
				// -----------------------------------------------------------------
				// 27/02/2015 ECU 
				// -----------------------------------------------------------------
				listItems = WeMoTimerActivity.BuildListForDevice();
				break;
			// =====================================================================
		}
		// -------------------------------------------------------------------------
		// 01/04/2014 ECU tell the adapter about the updated list
		// -------------------------------------------------------------------------
		customListViewAdapter.RebuildList (listItems);	
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void Rebuild (int theObjectType)
	{
		// -------------------------------------------------------------------------
		// 24/11/2016 ECU created to handle an issue in CarerActivity when the
		//                'displayed' status of a carer was being updated by the
		//                bluetooth discovery task before the carer display, via
		//                this class, has been initialised
		// -------------------------------------------------------------------------
		if (theObjectType == objectType)
		{
			// ---------------------------------------------------------------------
			// 24/11/2016 ECU this object can be rebuilt so do it
			// ---------------------------------------------------------------------
			Selector.Rebuild ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void SetFromSelectorParameter (SelectorParameter theSelectorParameter)
	{
		// -------------------------------------------------------------------------
		// 31/08/2015 ECU created to reset the 'selector parameter' to a new value
		//                and to change associated variables
		// -------------------------------------------------------------------------
		selectorParameter = theSelectorParameter;
		// -------------------------------------------------------------------------
		classToRun 		= selectorParameter.classToRun;
	   	// -------------------------------------------------------------------------
	   	// 18/04/2014 ECU put in the 'null' check
		// 05/08/2016 ECU added 'drawableInitial'
	   	// -------------------------------------------------------------------------
	   	if (selectorParameter.customMethodDefinition != null)
	   		customMethod 	= selectorParameter.customMethodDefinition.ReturnMethod(0);
	   	customLegend 		= selectorParameter.customLegend;
	   	drawableInitial		= selectorParameter.drawableInitial;
	   	listItems 			= selectorParameter.listItems;
	   	rowLayoutID 		= selectorParameter.rowLayout;
	   	// -------------------------------------------------------------------------
	   	// 31/08/2015 ECU set up the swipe method
	   	//            ECU this does not affect the 'OnTouch' listener
	   	// 11/10/2015 ECU check if the swipe method has already been set
	   	// 22/11/2015 ECU change the logic
	   	// -------------------------------------------------------------------------
	   	if (swipeMethod == null)
	   	{
	   		if (selectorParameter.swipeMethodDefinition != null)
	   		{
	   			swipeMethod 	= selectorParameter.swipeMethodDefinition.ReturnMethod(0);
	   		}
	   	}
	   	// -------------------------------------------------------------------------
	   	// 21/11/2015 ECU set up the help method if defined
	   	// -------------------------------------------------------------------------
		if (selectorParameter.helpMethodDefinition != null)
	   		helpMethod 	= selectorParameter.helpMethodDefinition.ReturnMethod(0);
		// -------------------------------------------------------------------------
	   	// 21/11/2015 ECU set up the select method if defined
	   	// -------------------------------------------------------------------------
		if (selectorParameter.selectMethodDefinition != null)
	   		selectMethod 	= selectorParameter.selectMethodDefinition.ReturnMethod(0);
		// -------------------------------------------------------------------------
	   	// 21/11/2015 ECU set up the long select method if defined
	   	// -------------------------------------------------------------------------
		if (selectorParameter.longSelectMethodDefinition != null)
	   		longSelectMethod 	= selectorParameter.longSelectMethodDefinition.ReturnMethod(0);
		// -------------------------------------------------------------------------
		// 18/10/2016 ECU check for an edit method
		// -------------------------------------------------------------------------
		if (selectorParameter.editMethodDefinition != null)
	   		editMethod 	= selectorParameter.editMethodDefinition.ReturnMethod (0);
		// -------------------------------------------------------------------------
	   	// 22/11/2015 ECU set up the back method if defined
		//            ECU adding the setting to null
		//			  ECU took out the reset to null as this is done at the start of
		//                the activity
		//            ECU tidy up the logic in case the method is previous set by
		//                a PARAMETER_BACK_METHOD in extras
	   	// -------------------------------------------------------------------------
		if (backMethod == null)
	   	{
	   		if (selectorParameter.backMethodDefinition != null)
	   		{
	   			backMethod 	= selectorParameter.backMethodDefinition.ReturnMethod (0);
	   			// -----------------------------------------------------------------
	   			// 22/11/2015 ECU and indicate that the key is allowed
	   			// -----------------------------------------------------------------
	   			backKeyAllowed = true;
	   			// -----------------------------------------------------------------
	   		}
	   	}
	   	// -------------------------------------------------------------------------
	   	type				= selectorParameter.type;
	   	// -------------------------------------------------------------------------
	   	// 31/08/2015 ECU and copy into the object identifier
	   	// 11/10/2015 ECU put in the check on ....SELECTOR
	   	// -------------------------------------------------------------------------
	   	if (objectType != StaticData.OBJECT_SELECTOR)
	   		objectType			= type;
	   	// -------------------------------------------------------------------------
	   	// 08/03/2016 ECU set the finish on select flag
	   	// -------------------------------------------------------------------------
	    finishOnSelect = selectorParameter.finishOnSelect;
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void Finish ()
	{
		// -------------------------------------------------------------------------
		// 24/11/2015 ECU created to simplify other activities that are called
		//                by this activity by 'defined methods' and which need to
		//                'finish' this activity
		// -------------------------------------------------------------------------
		((Activity)Selector.context).finish();
		// -------------------------------------------------------------------------
	}
	// ============================================================================
}

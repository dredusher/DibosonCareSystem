package com.usher.diboson;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

// =================================================================================
public class ListViewSelector 
{
	// =============================================================================
	Activity				activity;
	CustomListViewAdapter 	customListViewAdapter;
	ArrayList<ListItem> 	listItems				= new ArrayList<ListItem>();
	Method 					populateMethod;
	boolean					sortFlag;
	// =============================================================================
	
	// =============================================================================
	public ListViewSelector (       Activity 	theActivity,
					  		 	   	int 		theLayoutID,
					  		 	   	Method 		thePopulateMethod,
					  		  final boolean		theSortFlag,
					  		  final Method		theSelectMethod,
					  		  final Method 		theLongSelectMethod,
					  				Method 		theEditMethod,
					  				String 		theCustomLegend,
					  				Method 		theCustomMethod,
					  				Method		theHelpMethod,
					  		  final Method 		theSwipeMethod)
	{
		// -------------------------------------------------------------------------
		// 10/04/2018 ECU remember some variables for later use
		// -------------------------------------------------------------------------
		activity		= theActivity;
		populateMethod 	= thePopulateMethod;
		sortFlag		= theSortFlag;
		// -------------------------------------------------------------------------
		// 07/04/2018 ECU set up the display with a list view
		// -------------------------------------------------------------------------
		theActivity.setContentView (R.layout.activity_list);
		
		ListView listView = (ListView) theActivity.findViewById (R.id.grid_list_view);
		// -------------------------------------------------------------------------
		// 07/04/2018 ECU build up the initial list of items to display
		// -------------------------------------------------------------------------
		listItems = populateListView (theActivity,thePopulateMethod);
		// -------------------------------------------------------------------------
		// 11/04/2018 ECU put in the checks on 'null' and 'size'
		// -------------------------------------------------------------------------
		if (listItems != null & (listItems.size() > 0))
		{
			// ---------------------------------------------------------------------
			// 07/04/2018 ECU check if the obtained items is to be sorted
			// ---------------------------------------------------------------------
			if (theSortFlag)
				Collections.sort (listItems);
			// ---------------------------------------------------------------------
			// 07/04/2017 ECU  set up the adapter that will handle the list
			// ---------------------------------------------------------------------
			customListViewAdapter = new CustomListViewAdapter (theActivity,theLayoutID,listItems); 
		
			listView.setAdapter (customListViewAdapter);
			// ---------------------------------------------------------------------
			customListViewAdapter.ChangeDefaults (theActivity,
					                              StaticData.NOT_SET,
					                              theHelpMethod,
					                              theEditMethod,
					                              theCustomMethod,
					                              theCustomLegend,
					                              null);
			// ---------------------------------------------------------------------
			// 07/04/2018 ECU now declare the listeners that will interact with the user
			// ---------------------------------------------------------------------
			// 07/04/2018 ECU 'click' on item
			//                ===============
			// 10/04/2018 ECU add the check on NO_....
			// ---------------------------------------------------------------------
			if (theSelectMethod != StaticData.NO_HANDLING_METHOD)
			{
				listView.setOnItemClickListener (new OnItemClickListener() 
				{
					@Override
					public void onItemClick (AdapterView<?> adapterView, View view,int position, long id) 
					{	
						invokeMethod (activity,theSelectMethod, theSortFlag ? listItems.get(position).index 
																		    : position );					
					}
				});
			}
			// ---------------------------------------------------------------------
			// 07/04/2018 ECU 'long click' on item
			//                ====================
			// 10/04/2018 ECU add the check on NO_....
			// ---------------------------------------------------------------------
			if (theLongSelectMethod != StaticData.NO_HANDLING_METHOD)
			{
				listView.setOnItemLongClickListener (new OnItemLongClickListener() 
				{
					@Override
					public boolean onItemLongClick (AdapterView<?> adapterView, View view,int position, long id)
					{
						invokeMethod (activity,theLongSelectMethod, theSortFlag ? listItems.get (position).index 
								                                                : position );
						return true;
					}			
				});
			}
			// ---------------------------------------------------------------------
			// 07/04/2018 ECU 'swipe' on item
			//                ================
			// 10/04/2018 ECU put in the check on the method
			// ---------------------------------------------------------------------
			if (theSwipeMethod != StaticData.NO_HANDLING_METHOD)
			{
				SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener (
																				listView,
																				new SwipeDismissListViewTouchListener.OnDismissCallback () 
				{
					@Override
					public void onDismiss (ListView listView,int [] reverseSortedPositions) 
					{
						invokeMethod (activity,theSwipeMethod,listItems.get (reverseSortedPositions [0]).index);
					}
				});
				// -----------------------------------------------------------------
				listView.setOnTouchListener (touchListener);
				listView.setOnScrollListener (touchListener.makeScrollListener());
				// -----------------------------------------------------------------
			}
		}
	}
	// -----------------------------------------------------------------------------
	public ListViewSelector (Activity 	theActivity,
		 	   				 int 		theLayoutID,
		 	   				 String 	thePopulateMethodName,
		 	   				 boolean	theSortFlag,
		 	   				 String		theSelectMethodName,
		 	   				 String 	theLongSelectMethodName,
		 	   				 String 	theEditMethodName,
		 	   				 String 	theCustomLegend,
		 	   				 String 	theCustomMethodName,
		 	   				 String		theHelpMethodName,
		 	   				 String 	theSwipeMethodName)
	{
		// -------------------------------------------------------------------------
		// 14/04/2018 ECU created as a constructor when all of the methods are in the
		//                calling activity
		// -------------------------------------------------------------------------
		this (theActivity,
			  theLayoutID,
			  createMethod (theActivity,thePopulateMethodName),
			  theSortFlag,
			  createMethod (theActivity,theSelectMethodName,0),
			  createMethod (theActivity,theLongSelectMethodName,0),
			  createMethod (theActivity,theEditMethodName,0),
			  theCustomLegend,
			  createMethod (theActivity,theCustomMethodName,0),
			  createMethod (theActivity,theHelpMethodName,0),
			  createMethod (theActivity,theSwipeMethodName,0));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@SuppressWarnings("unchecked")
	ArrayList<ListItem> populateListView (Activity theActivity,Method thePopulateMethod)
	{
		try 
		{
			// ---------------------------------------------------------------------
			// 07/04/2018 ECU invoke the specified method to get the
			//                list of items
			// ---------------------------------------------------------------------
			return (ArrayList<ListItem>) thePopulateMethod.invoke (theActivity);
			// ---------------------------------------------------------------------
		}
		catch (Exception theException) 
		{
			// ---------------------------------------------------------------------
			// ---------------------------------------------------------------------
			return null;
		}
	}
	// =============================================================================
	static Method createMethod (Activity theActivity,String theMethodName,int theIntArgument)
	{
		// -------------------------------------------------------------------------
		// 14/04/2018 ECU created to create a method from the argument taking into
		//                account whether a method is required
		// 15/04/2018 ECU changed to static because this is used to generate an
		//                argument in the constructor
		// -------------------------------------------------------------------------
		if (theMethodName != null)
		{
			// ---------------------------------------------------------------------
			// 14/04/2018 ECU check if an argument has been supplied
			// ---------------------------------------------------------------------
			if (theIntArgument == StaticData.NOT_SET)
			{
				// -----------------------------------------------------------------
				// 14/04/2018 ECU no argument is to be supplied
				// -----------------------------------------------------------------
				return Utilities.createAMethod (theActivity.getClass(),theMethodName);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 14/04/2018 ECU an argument is required
				// -----------------------------------------------------------------
				return Utilities.createAMethod (theActivity.getClass(),theMethodName,0);
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 14/04/2018 ECU indicate that this method is not to be handled
			// ---------------------------------------------------------------------
			return StaticData.NO_HANDLING_METHOD;
			// ---------------------------------------------------------------------
		}
	}
	// -----------------------------------------------------------------------------
	static Method createMethod (Activity theActivity,String theMethodName)
	{
		// -------------------------------------------------------------------------
		// 14/04/2018 ECU created to create a method with no arguments
		// 15/04/2018 ECU changed to static because this is used to generate an
		//                argument in the constructor
		// -------------------------------------------------------------------------
		return createMethod (theActivity,theMethodName,StaticData.NOT_SET);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void invokeMethod (Activity theActivity,Method theMethod,int theItemPosition)
	{
		// -------------------------------------------------------------------------
		// 07/04/2018 ECU because this class has a standardised method to be
		//                invoked (i.e. a single argument with the position of
		//                the item within the list
		// -------------------------------------------------------------------------
		if (theMethod != null)
		{
			try 
			{
				// ------------------------------------------------------------------
				// 07/04/2018 ECU invoke the specifiued method to get the
				//                list of items
				// -----------------------------------------------------------------
				theMethod.invoke (theActivity,new Object [] {theItemPosition});
				// -----------------------------------------------------------------
			}
			catch (Exception theException) 
			{
				// -----------------------------------------------------------------
				// -----------------------------------------------------------------
			}
		}
	}
	// =============================================================================
	public void refresh ()
	{
		// -------------------------------------------------------------------------
		// 10/04/2018 ECU created to refresh the display 
		// -------------------------------------------------------------------------
		// 10/04/2018 ECU regenerate the list of items
		// -------------------------------------------------------------------------
		listItems = populateListView (activity,populateMethod);
		// -------------------------------------------------------------------------
		// 10/04/2018 ECU tell the adapter about the updated list
		// 11/04/2018 ECU put in the null and size checks
		// -------------------------------------------------------------------------
		if (listItems != null && (listItems.size() > 0))
		{
			// ---------------------------------------------------------------------
			// 07/04/2018 ECU check if the obtained items is to be sorted
			// ---------------------------------------------------------------------
			if (sortFlag)
				Collections.sort (listItems);
			// ---------------------------------------------------------------------
			customListViewAdapter.RebuildList (listItems);	
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================	
}
// =================================================================================

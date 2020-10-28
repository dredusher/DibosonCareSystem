package com.usher.diboson;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class EPGListViewAdapter extends ArrayAdapter<EPGEntry>
{
	// ===============================================================================
	// 25/09/2015 ECU created
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//================================================================================
	//private static final String TAG = "EPGListViewAdapter";
	/* ============================================================================ */
	
	// ============================================================================
	public 	int							channel;
			Context						context;
			ArrayList<EPGEntry> 		data = new ArrayList<EPGEntry>();
			int							layoutResourceId;
			ArrayList<EPGEntry> 		originalData = new ArrayList<EPGEntry>();
	// ============================================================================
	
	/* ============================================================================ */
	public EPGListViewAdapter (Context theContext,
							   int theLayoutResourceId,
							   int theChannel,
							   ArrayList<EPGEntry> theData) 
	{
		super (theContext, theLayoutResourceId, theData);
		// -------------------------------------------------------------------------
		// 26/01/2014 ECU save the variables for later
		// -------------------------------------------------------------------------
		context				= theContext;
		data				= theData;
		layoutResourceId 	= theLayoutResourceId;
		// -------------------------------------------------------------------------
		// 26/09/2015 ECU save the index to the TV channel to which these EPG entries
		//                apply
		// -------------------------------------------------------------------------
		channel = theChannel;
		// -------------------------------------------------------------------------
		// 28/09/2015 ECU copy the data into the original array
		// -------------------------------------------------------------------------
		// 28/09/2015 ECU the whole of the original data is to be used
		// -------------------------------------------------------------------------
		// 10/11/2014 ECU the following 'addAll' came in at API 11
		// -------------------------------------------------------------------------
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			originalData.addAll (theData);	
		}
		else
		{
			// ---------------------------------------------------------------------
			// 10/11/2014 ECU cannot use the 'addAll' method so add each item one
			//                at a time
			// ---------------------------------------------------------------------
			for (int theIndex  = 0; theIndex < data.size(); theIndex++)
					originalData.add (data.get(theIndex));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public View getView (final int thePosition, View theConvertView, ViewGroup theParent) 
	{
		// -------------------------------------------------------------------------
		View 		listCellView 	= theConvertView;
		ItemHolder 	itemHolder 		= null;
		// -------------------------------------------------------------------------
		if (listCellView == null) 
		{
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			listCellView = inflater.inflate(layoutResourceId, theParent, false);

			itemHolder = new ItemHolder();
			// ---------------------------------------------------------------------
			itemHolder.date			= (TextView) listCellView.findViewById(R.id.epg_date);
			itemHolder.description	= (TextView) listCellView.findViewById(R.id.epg_description);
			itemHolder.endTime 		= (TextView) listCellView.findViewById(R.id.epg_end_time);
			itemHolder.genre 		= (TextView) listCellView.findViewById(R.id.epg_genre);
			itemHolder.startTime 	= (TextView) listCellView.findViewById(R.id.epg_start_time);
			itemHolder.title 		= (TextView) listCellView.findViewById(R.id.epg_title);
			// ---------------------------------------------------------------------
			listCellView.setTag (itemHolder);
		} 
		else 
		{
			itemHolder = (ItemHolder) listCellView.getTag();
		}
		// -------------------------------------------------------------------------
		// 26/01/2014 ECU update the view from the supplied data
		// -------------------------------------------------------------------------
		EPGEntry listItem = data.get (thePosition);
		// -------------------------------------------------------------------------
		itemHolder.date.setText (listItem.fields[StaticData.EPG_DATE]);
		itemHolder.endTime.setText (listItem.fields[StaticData.EPG_END_TIME]);
		itemHolder.startTime.setText (listItem.fields[StaticData.EPG_START_TIME]);	
		itemHolder.title.setText (listItem.fields[StaticData.EPG_PROGRAM_TITLE]);
		// -------------------------------------------------------------------------
		// 26/09/2015 ECU group the details which are only shown if required
		// -------------------------------------------------------------------------
		if (TVEPGFragment.showDetails)
		{
			itemHolder.description.setText (listItem.fields[StaticData.EPG_DESCRIPTION]);
			itemHolder.genre.setText (listItem.fields[StaticData.EPG_GENRE]);
		}
		else
		{
			// ---------------------------------------------------------------------
			// 26/09/2015 ECU clear the fields
			// ---------------------------------------------------------------------
			itemHolder.description.setText (R.string.blank_textview);
			itemHolder.genre.setText (R.string.blank_textview);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 26/09/2015 ECU set the background of the view - remember that the view
		//                can be recycled so don't assume it will be reset
		// -------------------------------------------------------------------------
		if (listItem.selected)
			listCellView.setBackgroundColor (Color.GREEN);
		else
			listCellView.setBackgroundColor (Color.LTGRAY);
		// -------------------------------------------------------------------------
		return listCellView;
	}
	/* ============================================================================= */
	static class ItemHolder 
	{
		TextView 	date;
		TextView 	description;
		TextView 	endTime;
		TextView 	genre;
		TextView  	startTime;	
		TextView 	title;
	}
	// ============================================================================
	public void UpdateData (ArrayList<EPGEntry> theData)
	{
		data = theData;
	}
	/* ============================================================================ */
	void RebuildList (ArrayList<EPGEntry> theEntries)
	{
		// ------------------------------------------------------------------------
		// 01/04/2014 ECU created to rebuild the lists in use
		// ------------------------------------------------------------------------
		clear ();
		// ------------------------------------------------------------------------
		// 28/09/2015 ECU rebuild the list depending on the filter string
		// 24/07/2016 ECU changed because filterString moved to Show....
		//            ECU PROBLEM - really need to build from theEntries rather
		//                          than theEntries - needs looking at
		// 18/11/2017 ECU change from checking for 'null' which used to mean
		//                'search all'
		// ------------------------------------------------------------------------
		if (!ShowEPGActivity.filterString.equalsIgnoreCase (StaticData.SEARCH_ALL))
		{
			for (int theEntry = 0; theEntry < originalData.size(); theEntry++)
			{
				if (originalData.get (theEntry).search (ShowEPGActivity.filterString))
				{
					// -------------------------------------------------------------
					// 27/09/2015 ECU the item does not match the filter so make
					//                invisible
					// -------------------------------------------------------------
					add (originalData.get (theEntry));
					// -------------------------------------------------------------
				}	
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 28/09/2015 ECU the whole of the original data is to be used
			// ---------------------------------------------------------------------
			// 10/11/2014 ECU the following 'addAll' came in at API 11
			// ---------------------------------------------------------------------
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
			{
				addAll (originalData);	
			}
			else
			{
				// -----------------------------------------------------------------
				// 10/11/2014 ECU cannot use the 'addAll' method so add each item one
				//                at a time
				// -----------------------------------------------------------------
				for (int theIndex  = 0; theIndex < originalData.size(); theIndex++)
					add (originalData.get(theIndex));
				// -----------------------------------------------------------------
			}
		}
	}
	// =============================================================================
	public void toggleShowDetails ()
	{
		// -------------------------------------------------------------------------
		// 26/09/2015 ECU created to toggle the state of the 'showDetails' variable
		// -------------------------------------------------------------------------
		TVEPGFragment.showDetails = !TVEPGFragment.showDetails;
		// -------------------------------------------------------------------------
	}
	// ============================================================================
}

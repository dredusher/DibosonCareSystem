package com.usher.diboson;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CustomGridViewAdapter extends ArrayAdapter<GridItem>
{
	// ===============================================================================
	// 26/01/2014 ECU created
	// 12/10/2014 ECU only display the help item if there is an associated help file
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//================================================================================
	//private static final String TAG = "CustomGridViewAdapter";
	/* ============================================================================ */
	Context						context;
	ArrayList<GridItem> 		data 					= new ArrayList<GridItem>();
	RelativeLayout.LayoutParams layoutParams;
	int							layoutResourceId;
	/* ============================================================================ */
	public CustomGridViewAdapter (Context theContext,int theLayoutResourceId,ArrayList<GridItem> theData) 
	{
		super (theContext,theLayoutResourceId,theData);
		// -------------------------------------------------------------------------
		// 26/01/2014 ECU save the variables for later
		// -------------------------------------------------------------------------
		context				= theContext;
		data				= theData;
		layoutResourceId 	= theLayoutResourceId;
		// -------------------------------------------------------------------------
		// 28/01/2014 ECU set the size of the image view
		// -------------------------------------------------------------------------
		layoutParams = new RelativeLayout.LayoutParams (PublicData.screenWidth/4,PublicData.screenWidth/4);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	@Override
	public View getView (final int thePosition, View theConvertView, ViewGroup theParent) 
	{
		// -------------------------------------------------------------------------
		View 		gridCellView 	= theConvertView;
		ItemHolder 	itemHolder 		= null;
		// -------------------------------------------------------------------------
		if (gridCellView == null) 
		{
			// ---------------------------------------------------------------------
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			gridCellView = inflater.inflate (layoutResourceId, theParent, false);
			// ---------------------------------------------------------------------
			// 27/01/2015 ECU added the long press view
			// 12/04/2015 ECU added the subTitle
			// ---------------------------------------------------------------------
			itemHolder 						= new ItemHolder();
			itemHolder.gridLegend 			= (TextView)  gridCellView.findViewById (R.id.grid_cell_textview);
			itemHolder.gridImage 			= (ImageView) gridCellView.findViewById (R.id.grid_cell_imageview);
			itemHolder.gridSubtitle			= (TextView)  gridCellView.findViewById (R.id.grid_cell_subtextview);
			itemHolder.helpIcon  			= (ImageView) gridCellView.findViewById (R.id.grid_cell_help);
			itemHolder.longPressIcon		= (ImageView) gridCellView.findViewById (R.id.grid_cell_long_press);
			// ----------------------------------------------------------------------
			gridCellView.setTag (itemHolder);
		} 
		else 
		{
			// ---------------------------------------------------------------------
			itemHolder = (ItemHolder) gridCellView.getTag();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 26/01/2014 ECU update the view from the supplied data
		// -------------------------------------------------------------------------
		GridItem gridItem = data.get(thePosition);
		// -------------------------------------------------------------------------
		itemHolder.gridLegend.setText (gridItem.GetLegend());	
		itemHolder.gridImage.setImageResource (gridItem.GetImage());	
		// -------------------------------------------------------------------------	
		// 06/02/2014 ECU adjust some of the fields because the image is resized
		// -------------------------------------------------------------------------
		itemHolder.gridImage.setLayoutParams (layoutParams);
		// -------------------------------------------------------------------------
		// 20/01/2015 ECU VERY IMPORTANT
		//                ==============
		//                in the following list only R.drawable.music is correct
		//                - the others will fail if any scrolling occurs
		// 21/01/2015 ECU removed all but the 'music' reference - torch, slide show,
		//                and compass removed
		// 29/12/2016 ECU added 'torch' handling
		// -------------------------------------------------------------------------
		// 31/12/2016 ECU changed to use the new method to modify the displayed details
		//                of an activity. The code that was here and its associated
		//                comments are included in the new method.
		// -------------------------------------------------------------------------
		UserInterface.activityUpdate (gridItem.image,itemHolder.gridImage,itemHolder.gridSubtitle);
		// -------------------------------------------------------------------------
		// 27/01/2015 ECU sort out the long press icon
		// -------------------------------------------------------------------------
		itemHolder.longPressIcon.setVisibility (gridItem.GetLongPress() ? View.VISIBLE : View.INVISIBLE);
		// -------------------------------------------------------------------------
		// 12/10/2014 ECU decide whether the help icon should be displayed
		// -------------------------------------------------------------------------
		itemHolder.helpIcon.setVisibility (Utilities.gridHelp (context,gridItem.image,true) ? View.VISIBLE : View.INVISIBLE);
	    // -------------------------------------------------------------------------  
		// 06/02/2014 ECU added to action the 'help' button
		// -------------------------------------------------------------------------	
		itemHolder.helpIcon.setOnClickListener (new OnClickListener() 
		{
		   @Override
		   public void onClick(View view) 
		   {
			   // ------------------------------------------------------------------
			   // 06/02/2014 ECU display any associated help
			   // ------------------------------------------------------------------			   
			   Utilities.gridHelp (context,GridActivity.activeImages[0][thePosition]);
			   // ------------------------------------------------------------------
		   }
		});
		// -------------------------------------------------------------------------
		return gridCellView;
	}
	/* ============================================================================ */
	static class ItemHolder 
	{
		View		viewInHolder;	 		// 27/01/2015 ECU renamed
		TextView 	gridLegend;
		ImageView 	gridImage;
		TextView	gridSubtitle;			// 12/04/2015 ECU added
		ImageView	helpIcon;				// 06/02/2014 ECU added
		ImageView   longPressIcon;			// 27/01/2015 ECU added
	}
	// =============================================================================
	// 12/04/2015 ECU the following were included to prevent the recycling of views
	//                which was causing issues with MARQUEE animation. I know it
	//                is the wrong thing to do but it solved the problem and further
	//                investigation will be needed
	// =============================================================================
	@Override 
	public int getViewTypeCount () 
	{                 
		return getCount();
	}
	// =============================================================================
	@Override
	public int getItemViewType (int position)
	{
		return position;
	}
	// =============================================================================
}

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
	// 23/01/2019 ECU put in the listener for a 'long click' on the image - at the moment
	//                nothing to do
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
	public CustomGridViewAdapter (Context 	theContext,
								  int 		theLayoutResourceId,
								  ArrayList<GridItem> theData) 
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
			LayoutInflater inflater = ((Activity) context).getLayoutInflater ();
			gridCellView = inflater.inflate (layoutResourceId,theParent,false);
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
			// 05/01/2019 ECU added the usage field
			// ----------------------------------------------------------------------
			itemHolder.gridUsage 			= (TextView)  gridCellView.findViewById (R.id.grid_cell_usage);
			// ----------------------------------------------------------------------
			gridCellView.setTag (itemHolder);
		} 
		else 
		{
			// ---------------------------------------------------------------------
			itemHolder = (ItemHolder) gridCellView.getTag ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 26/01/2014 ECU update the view from the supplied data
		// -------------------------------------------------------------------------
		GridItem gridItem = data.get (thePosition);
		// -------------------------------------------------------------------------
		// 19/07/2017 ECU check if there is a 'long press' legend
		//            ECU check if the user has configured the display of the 'long
		//                press' legend
		// 27/09/2017 ECU added the check on 'GetLongPress'
		// -------------------------------------------------------------------------
		if (gridItem.GetLegendLong() == null || 
			!PublicData.storedData.longPressLegend || 
			!gridItem.GetLongPress())
		{
			// ---------------------------------------------------------------------
			// 19/07/2017 ECU there is no 'long press' legend 
			// ---------------------------------------------------------------------
			itemHolder.gridLegend.setText (gridItem.GetLegend ());	
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 19/07/2017 ECU there is a 'long press' legend 
			// 20/07/2017 ECU add the required colours
			// 28/07/2017 ECU add the 'getResources' on getColour to accommodate
			//                older versions of Android (i.e. those prior to API 23)
			// ---------------------------------------------------------------------
			itemHolder.gridLegend.setText (Utilities.threeLineButtonLegend (context,
					                                                        gridItem.GetLegend(),
					                                                        	context.getResources().getColor (R.color.black),
				                                                            context.getString (R.string.long_press),
				                                                            	context.getResources().getColor (R.color.gray),
				                                                            StaticData.LEGEND_INDENT + gridItem.GetLegendLong (),
				                                                            	context.getResources().getColor (R.color.dark_slate_gray)));	
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 05/01/2019 ECU check if the usage is to be displayed but only after
		//                checking that the recipient text view exists
		// -------------------------------------------------------------------------
		if (itemHolder.gridUsage != null)
		{
			if (PublicData.storedData.usageDisplay)
			{
				// -----------------------------------------------------------------
				// 05/01/2019 ECU need to make the field 'visible' before showing the
				//                usage information
				// 23/01/2019 ECU changed to use the resource
				// -----------------------------------------------------------------
				itemHolder.gridUsage.setVisibility (View.VISIBLE);
				itemHolder.gridUsage.setText (String.format (context.getString (R.string.usage_format),
													GridImages.GetUsage (gridItem.image))); 
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 05/01/2019 ECU make sure that the 'usage' field is hidden
				// -----------------------------------------------------------------
				itemHolder.gridUsage.setVisibility (View.GONE);
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
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
		// 04/06/2017 ECU pass through the context
		// 15/01/2018 ECU pass through the gridLegend
		// -------------------------------------------------------------------------
		UserInterface.activityUpdate (context,gridItem.image,itemHolder.gridImage,itemHolder.gridLegend,itemHolder.gridSubtitle);
		// -------------------------------------------------------------------------
		// 27/01/2015 ECU sort out the long press icon
		// -------------------------------------------------------------------------
		itemHolder.longPressIcon.setVisibility (gridItem.GetLongPress() ? View.VISIBLE 
																		: View.INVISIBLE);
		// -------------------------------------------------------------------------
		// 12/10/2014 ECU decide whether the help icon should be displayed
		// -------------------------------------------------------------------------
		itemHolder.helpIcon.setVisibility (Utilities.gridHelp (context,gridItem.image,true) ? View.VISIBLE 
																							: View.INVISIBLE);
	    // -------------------------------------------------------------------------  
		// 06/02/2014 ECU added to action the 'help' button
		// -------------------------------------------------------------------------	
		itemHolder.helpIcon.setOnClickListener (new OnClickListener () 
		{
		   @Override
		   public void onClick(View view) 
		   {
			   // ------------------------------------------------------------------
			   // 06/02/2014 ECU display any associated help
			   // ------------------------------------------------------------------			   
			   Utilities.gridHelp (context,GridActivity.activeImages [0][thePosition]);
			   // ------------------------------------------------------------------
		   }
		});
		// -------------------------------------------------------------------------
		// 23/01/2019 ECU set up the listener when the user clicks on the image
		// 25/01/2019 ECU Note - first do 'click' then the 'long click'
		// -------------------------------------------------------------------------
		itemHolder.gridImage.setOnClickListener(new View.OnClickListener ()
		{
			// ---------------------------------------------------------------------
			@Override
			public void onClick(View theView) 
			{	
				 if (PublicData.storedData.taskImageClick)
					   PositiveFeedback.UserAction (context,theView);
			}
			// ---------------------------------------------------------------------
		});
		// -------------------------------------------------------------------------
		itemHolder.gridImage.setOnLongClickListener (new View.OnLongClickListener () 
		{
		   @Override
		   public boolean onLongClick (View theView) 
		   {
			   // ------------------------------------------------------------------
			   // 23/01/2019 ECU put any actions here that may be required
			   // 25/01/2019 ECU put in check on 'taskIm...'
			   // ------------------------------------------------------------------
			   if (PublicData.storedData.taskImageLongClick)
				   PositiveFeedback.UserAction (context,theView);
			   // ------------------------------------------------------------------
			   return true;
			   // ------------------------------------------------------------------
		   }
		});
		// -------------------------------------------------------------------------
		// 23/01/2019 ECU Note - return the 'converted view'
		// -------------------------------------------------------------------------
		return gridCellView;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	static class ItemHolder 
	{
		// -------------------------------------------------------------------------
		View		viewInHolder;	 		// 27/01/2015 ECU renamed
		TextView 	gridLegend;
		ImageView 	gridImage;
		TextView	gridSubtitle;			// 12/04/2015 ECU added
		TextView	gridUsage;				// 05/01/2019 ECU added
		ImageView	helpIcon;				// 06/02/2014 ECU added
		ImageView   longPressIcon;			// 27/01/2015 ECU added
		// -------------------------------------------------------------------------
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
		// -------------------------------------------------------------------------
		return getCount ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public int getItemViewType (int thePosition)
	{
		// -------------------------------------------------------------------------
		// 23/01/2019 ECU changed from 'position' to 'thePosition
		// -------------------------------------------------------------------------
		return thePosition;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

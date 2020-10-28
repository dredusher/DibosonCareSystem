package com.usher.diboson;

import java.lang.reflect.Method;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CustomListViewAdapter extends ArrayAdapter<ListItem>
{
	// ===============================================================================
	// 05/02/2014 ECU created
	// 04/04/2018 ECU changed all '.invoke' calls to use the new method in Utilities
	// 08/04/2018 ECU changed so that the calling activity is remembered and can be
	//                passed to the 'invokeMethod' method so that calls to non-static
	//                methods does not force a creation of a new instance.
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//================================================================================
	//private static final String TAG = "CustomListViewAdapter";
	/* ============================================================================ */
	Activity					callingActivity = null;			// 08/04/2018 ECU added
	Context						context;
	String						customLegend = null;			// 24/03/2014 ECU added
	ArrayList<ListItem> 		data = new ArrayList<ListItem>();
	int							defaultImageID;					// 07/02/2014 ECU added
	boolean						defaultsChanged = false;		// 27/03/2015 ECU added
	RelativeLayout.LayoutParams layoutParams;
	int							layoutResourceId;
	Method						methodCustom;					// 24/03/2014 ECU added
	Method						methodHelp;						// 07/02/2014 ECU added
	Method						methodImage;					// 30/03/2014 ECU added
	Method						methodButton;					// 08/02/2014 ECU added
	/* ============================================================================ */
	public CustomListViewAdapter (Context theContext,int theLayoutResourceId,ArrayList<ListItem> theData) 
	{
		super (theContext, theLayoutResourceId, theData);
		// -------------------------------------------------------------------------
		// 26/01/2014 ECU save the variables for later
		// -------------------------------------------------------------------------
		context				= theContext;
		data				= theData;
		layoutResourceId 	= theLayoutResourceId;
		// -------------------------------------------------------------------------
		// 28/01/2014 ECU set the size of the image view
		// -------------------------------------------------------------------------
		layoutParams 	= new RelativeLayout.LayoutParams (PublicData.screenWidth/4,PublicData.screenWidth/4);
		// -------------------------------------------------------------------------
		// 07/02/2014 ECU set up some defaults
		// -------------------------------------------------------------------------
		defaultImageID 	= R.drawable.no_photo;	
		
		methodHelp		= null;
		methodButton	= null;						// 08/02/2014 ECU added
		
		methodCustom	= null;						// 24/03/2014 ECU added
		
	}
	/* ============================================================================= */
	public void ChangeDefaults (Activity 	theActivity,
								int 		theDefaultImageID,
								Method 		theMethodHelp,
								Method 		theMethodButton,
								Method 		theMethodCustom,
								String 		theCustomLegend,
								Method 		theMethodImage)
	{
		// -------------------------------------------------------------------------
		// 07/02/2014 ECU added - copy across the new defaults
		// 24/03/2014 ECU changed to include theMethodCustom argument
		// 31/03/2014 ECU added theMethodImage
		// -------------------------------------------------------------------------
		customLegend	= theCustomLegend;			// 24/03/2014 ECU added
		defaultImageID 	= theDefaultImageID;	
		methodButton	= theMethodButton;			// 08/02/2014 ECU added
		methodCustom	= theMethodCustom;			// 24/03/2014 ECU added
		methodHelp		= theMethodHelp;
		methodImage		= theMethodImage;			// 31/03/2014 ECU added
		// -------------------------------------------------------------------------
		// 08/04/2018 ECU added the storage of the calling activity
		// -------------------------------------------------------------------------
		callingActivity = theActivity;
		// -------------------------------------------------------------------------
		// 27/03/2015 ECU indicate that the defaults have been changed
		// -------------------------------------------------------------------------
		defaultsChanged	= true;
		// -------------------------------------------------------------------------
	}
	/* ----------------------------------------------------------------------------- */
	public void ChangeDefaults (Activity theActivity,int theDefaultImageID,Method theMethodHelp,Method theMethodButton)
	{
		ChangeDefaults (theActivity,theDefaultImageID,theMethodHelp,theMethodButton,null,null,null);
	}
	/* ----------------------------------------------------------------------------- */
	public void ChangeDefaults (int 		theDefaultImageID,
								Method 		theMethodHelp,
								Method 		theMethodButton,
								Method 		theMethodCustom,
								String 		theCustomLegend,
								Method 		theMethodImage)
	{
		// -------------------------------------------------------------------------
		// 08/04/2018 ECU created when no calling activity is wanted
		// -------------------------------------------------------------------------
		ChangeDefaults (null,
						theDefaultImageID,
						theMethodHelp,
						theMethodButton,
						theMethodCustom,
						theCustomLegend,
						theMethodImage);
	}
	/* ============================================================================= */
	@Override
	public View getView (final int thePosition, View theConvertView, ViewGroup theParent) 
	{
		View 		listCellView 	= theConvertView;
		ItemHolder 	itemHolder 		= null;

		if (listCellView == null) 
		{
			// ---------------------------------------------------------------------
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			listCellView = inflater.inflate (layoutResourceId, theParent, false);
			// ---------------------------------------------------------------------
			itemHolder = new ItemHolder ();
			itemHolder.listLegend 	= (TextView)  listCellView.findViewById (R.id.list_button_entry_textview);
			itemHolder.listImage 	= (ImageView) listCellView.findViewById (R.id.list_button_entry_imageview);
			itemHolder.listSummary 	= (TextView)  listCellView.findViewById (R.id.list_button_entry_textview2);
			itemHolder.listExtras 	= (TextView)  listCellView.findViewById (R.id.list_button_entry_textview3);
			itemHolder.buttonPhone 	= (Button)    listCellView.findViewById (R.id.list_button_phone);
			itemHolder.buttonText 	= (Button)    listCellView.findViewById (R.id.list_button_text);
			itemHolder.buttonCustom = (Button)    listCellView.findViewById (R.id.list_button_custom);	
			// ---------------------------------------------------------------------
			// 24/03/2014 ECU changed the legend of the custom button if supplied
			// ---------------------------------------------------------------------
			if (customLegend != null)
			{
				// -----------------------------------------------------------------
				itemHolder.buttonCustom.setText (customLegend);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 24/03/2014 ECU hide the button if not wanted
				// -----------------------------------------------------------------
				if (itemHolder.buttonCustom != null)
					itemHolder.buttonCustom.setVisibility (View.GONE);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 27/02/2015 ECU check if the buttonPhone is to be made invisible
			// 27/03/2015 ECU added the check on 'defaultsChanged' because the
			//                default adapter should not make the 'phone' button
			//                'gone'
			// ---------------------------------------------------------------------
			if (defaultsChanged && (methodButton == null))
			{
				// -----------------------------------------------------------------
				// 27/02/2015 ECU the caller has requested that the button should
				//                be removed
				// -----------------------------------------------------------------
				if (itemHolder.buttonPhone != null)
					itemHolder.buttonPhone.setVisibility (View.GONE);
			}
			// ---------------------------------------------------------------------
			// 06/02/2014 ECU add in the help icon
			// ---------------------------------------------------------------------
			itemHolder.helpIcon   = (ImageView) listCellView.findViewById (R.id.list_button_help);
			// ---------------------------------------------------------------------
			// 30/03/2014 ECU check for the clickable image
			// ---------------------------------------------------------------------
			itemHolder.imageIcon   = (ImageView) listCellView.findViewById (R.id.list_image);
			// ---------------------------------------------------------------------	
			listCellView.setTag (itemHolder);
		} 
		else 
		{
			itemHolder = (ItemHolder) listCellView.getTag ();
		}
		// -------------------------------------------------------------------------
		// 26/01/2014 ECU update the view from the supplied data
		// -------------------------------------------------------------------------
		ListItem listItem = data.get (thePosition);
		// -------------------------------------------------------------------------
		itemHolder.listLegend.setText  (listItem.GetLegend ());
		itemHolder.listSummary.setText (listItem.GetSummary ());
		itemHolder.listExtras.setText  (listItem.GetExtras ());
		// -------------------------------------------------------------------------
		// 04/10/2016 ECU add the setting of the associated colour
		// -------------------------------------------------------------------------
		if (listItem.GetColour () != StaticData.NO_RESULT)
		{
			// ---------------------------------------------------------------------
			// 04/10/2016 ECU the colour has been specified so change the background
			//                colour
			// ---------------------------------------------------------------------
			listCellView.setBackgroundResource (listItem.GetColour());
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 09/05/2019 ECU check if this item has it's own custom legend
		// 10/05/2019 ECU added the check on 'customLegend'
		// -------------------------------------------------------------------------
		if (customLegend == null)
		{
			if (listItem.GetCustomLegend() != null)
			{
				// -----------------------------------------------------------------
				// 09/05/2019 ECU this item has its own legend
				// -----------------------------------------------------------------
				itemHolder.buttonCustom.setText (listItem.GetCustomLegend());
				// -----------------------------------------------------------------
				// 09/05/2019 ECU make sure that the button is visible
				// -----------------------------------------------------------------
				itemHolder.buttonCustom.setVisibility (View.VISIBLE);
				// -----------------------------------------------------------------
			}
			else
			{
				// -----------------------------------------------------------------
				// 10/05/2019 ECU remove the button
				// -----------------------------------------------------------------
				itemHolder.buttonCustom.setVisibility (View.GONE); 
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// -------------------------------------------------------------------------
		// 31/03/2014 ECU sort out the initial images
		// -------------------------------------------------------------------------
		if (itemHolder.imageIcon != null)
		{
			itemHolder.imageIcon.setImageResource (data.get(thePosition).selected ? R.drawable.tick
																				  : R.drawable.cross);	
		}
		// -------------------------------------------------------------------------
		// 08/02/2014 ECU put in the check on null
		// -------------------------------------------------------------------------
		if (itemHolder.listImage != null)
		{
			// ---------------------------------------------------------------------
			// 27/02/2015 ECU put in the 'else' case against 'null' so that the
			//                default image is displayed
			// 01/09/2015 ECU changed to use StaticData
			// 06/05/2017 ECU added the handling of imageURL
			// ---------------------------------------------------------------------
			if (listItem.imagePath != null)
				Utilities.displayAnImage (itemHolder.listImage,listItem.imagePath,StaticData.IMAGE_SAMPLE_SIZE,defaultImageID);
			else
			if (listItem.imageURL != null)
			{
				new AsyncUtilities.ImageLoadTask (listItem.imageURL,itemHolder.listImage).execute();
			}
			else
			{
				// -----------------------------------------------------------------
				// 19/12/2015 ECU check if a resource ID has been specified
				// -----------------------------------------------------------------
				if (listItem.imageResourceId == StaticData.NO_RESULT)
					itemHolder.listImage.setImageResource (defaultImageID);
				else
					itemHolder.listImage.setImageResource (listItem.imageResourceId);
			}
			// ---------------------------------------------------------------------
			// 05/02/2014 ECU make sure that the image size is correct
			// ---------------------------------------------------------------------
			itemHolder.listImage.setLayoutParams (layoutParams);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 07/02/2014 ECU put in check for null
		// -------------------------------------------------------------------------	
		if (itemHolder.buttonPhone != null)
		{
			itemHolder.buttonPhone.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View view) 
				{
					// -------------------------------------------------------------
					// 08/02/2014 ECU changed to handle an external method
					// -------------------------------------------------------------
					if (methodButton == null)
					{
						// ---------------------------------------------------------
						// 06/02/2014 ECU changed form 'context' to 'MainActivity.activity'
						// ---------------------------------------------------------   
						Utilities.popToast ("Phoning : " +  PublicData.carers.get(thePosition).phone);
				   
						Utilities.makePhoneCall(context,PublicData.carers.get(thePosition).phone);
					}
					else
					{
						try 
						{
							// -----------------------------------------------------
							// 30/03/2014 ECU change to send back the index stored in
							//                the record - this handles any sorting
							//                that may have occurred
							// 04/04/2018 ECU changed to use new invoke method
							// 08/04/2018 ECU changed method to include the activity
							// ------------------------------------------------------
							Utilities.invokeMethod (callingActivity,methodButton,new Object [] {data.get(thePosition).index});
							// ------------------------------------------------------
						}
						catch (Exception theException) 
						{
						}
					}
				}
			});
		}
		// -------------------------------------------------------------------------
		// 24/03/2014 ECU checking for the custom button
		// -------------------------------------------------------------------------
		if (itemHolder.buttonCustom != null)
		{
			itemHolder.buttonCustom.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick (View view) 
				{
					// -------------------------------------------------------------
					// 08/02/2014 ECU changed to handle an external method
					// -------------------------------------------------------------
					if (methodCustom != null)
					{
						try 
						{
							// -----------------------------------------------------
							// 30/03/2014 ECU change to send back the index stored in
							//                the record - this handles any sorting
							//                that may have occurred
							// 04/04/2018 ECU changed to use new invoke method
							// 08/04/2018 ECU changed method to include the activity
							// -----------------------------------------------------
							Utilities.invokeMethod (callingActivity,methodCustom,new Object [] {data.get(thePosition).index});
							// -----------------------------------------------------
						}
						catch (Exception theException) 
						{
						}
					}
				}
			});
		}
		// -------------------------------------------------------------------------
		//07/02/2014 ECU put in check for null
		// -------------------------------------------------------------------------
		if (itemHolder.buttonText != null)
		{
			itemHolder.buttonText.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View view) 
				{
					// -------------------------------------------------------------
					// 06/02/2014 ECU changed from 'context' to 'MainActivity.activity'
					// -------------------------------------------------------------
					Utilities.popToast ("Texting : " +  PublicData.carers.get(thePosition).phone);
			   
					Utilities.sendSMSMessage(context,PublicData.carers.get(thePosition).phone,"Please ring me");
				}
			});
		}
		// -------------------------------------------------------------------------
		// 20/12/2017 ECU added the check on null - just in case
		// -------------------------------------------------------------------------
		if (itemHolder.helpIcon != null)
		{
			itemHolder.helpIcon.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View view) 
				{
					// -------------------------------------------------------------
					if (methodHelp == null)
					{
						Utilities.popToast ("Help Icon",Toast.LENGTH_SHORT);
					}
					else
					{
						try 
						{
							// -----------------------------------------------------
							// 04/04/2018 ECU changed to use new invoke method
							// 08/04/2018 ECU changed method to include the activity
							//            ECU changed from just passing 'thePosition'
							//                as this does not take account of 'sorting'
							// -----------------------------------------------------
							Utilities.invokeMethod (callingActivity,methodHelp,new Object [] {data.get(thePosition).index});
							// -----------------------------------------------------
						}
						catch (Exception theException) 
						{
						}	
					}
				}
			});	
		}
		// -------------------------------------------------------------------------
		if (itemHolder.imageIcon != null)
		{		
			itemHolder.imageIcon.setOnClickListener(new OnClickListener() 
			{
			   @Override
			   public void onClick(View view) 
			   {
				   // --------------------------------------------------------------
				   // 14/06/2015 ECU removed the toggling of the image which is handled
				   //                by the appropriate data source
				   // --------------------------------------------------------------
				   //ImageView imageView  = (ImageView) view.findViewById(R.id.list_image);
				   //imageView.setImageResource(data.get(thePosition).ToggleSelected() ? R.drawable.tick : R.drawable.cross);	
				   // --------------------------------------------------------------
				   if (methodImage != null)
				   {
					   try 
					   {
						   // ------------------------------------------------------
						   // 31/03/2014 ECU send the embedded index to take into
						   //                account any sorting
						   // 04/04/2018 ECU changed to use new invoke method
						   // 08/04/2018 ECU changed method to include the activity
						   // ------------------------------------------------------
						   Utilities.invokeMethod (callingActivity,methodImage,new Object [] {data.get(thePosition).index});
						   // ------------------------------------------------------
					   }
					   catch (Exception theException) 
					   {
					   }
				   }
			   }
			});
		}
		// -------------------------------------------------------------------------		
		return listCellView;
	}
	/* ============================================================================ */
	static class ItemHolder 
	{
		TextView  	listLegend;
		TextView 	listSummary;
		TextView 	listExtras;
		ImageView 	listImage;
		
		Button 		buttonPhone;;
		Button 		buttonText;
		Button		buttonCustom;				// 24/03/2014 ECU added
		
		ImageView   helpIcon;					// 06/02/2014 ECU added
		ImageView   imageIcon;					// 30/03/2014 ECU added
		
		int         imageID;

	}
	/* ============================================================================ */
	public void UpdateData (ArrayList<ListItem> theData)
	{
		data = theData;
	}
	/* ============================================================================ */
	void RebuildList (ArrayList<ListItem> theListItems)
	{
		// ------------------------------------------------------------------------
		// 01/04/2014 ECU created to rebuild the lists in use
		// ------------------------------------------------------------------------
		// 23/11/2016 ECU do a check just in case an 'undefined' list has been passed 
		//                through
		// ------------------------------------------------------------------------
		if (theListItems != null)
		{
			clear ();
			// ------------------------------------------------------------------------
			// 10/11/2014 ECU the following 'addAll' came in at API 11
			// ------------------------------------------------------------------------
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
			{
				addAll (theListItems);	
			}
			else
			{
				// ---------------------------------------------------------------------
				// 10/11/2014 ECU cannot use the 'addAll' method so add each item one
				//                at a time
				// ---------------------------------------------------------------------
				for (int theIndex  = 0; theIndex < theListItems.size(); theIndex++)
					add (theListItems.get(theIndex));
				// ---------------------------------------------------------------------
			}
		}
	}
	/* ============================================================================ */
}

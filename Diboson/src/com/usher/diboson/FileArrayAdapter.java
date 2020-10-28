package com.usher.diboson;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/* =============================================================================== */
public class FileArrayAdapter extends ArrayAdapter<FileOptions>
{
	/* =========================================================================== */
	private Method 				actionMethod;				// 08/01/2020 ECU added
	private Context 			context;
	private int 				id;
	private List<FileOptions>	items;
	BitmapFactory.Options		options;
	private boolean				showImageFlag;				// 15/12/2015 ECU added
	/* ============================================================================= */
	private class ItemHolder 
	{
		Button		actionButton;
		FileOptions fileOptions;
		Bitmap	 	imageBitmap;
		ImageView 	imageView;
		TextView 	textViewDetails;	
		TextView	textViewName;
		TextView 	textViewType;
    }
	// =============================================================================
	public FileArrayAdapter (Context 			theContext,
							 ListView 			theListView,
							 int 				textViewResourceId,
							 List<FileOptions> 	objects,
							 boolean 			theShowImageFlag,
							 Method 			theActionMethod)
	{
		// -------------------------------------------------------------------------
		// 08/01/2020 ECU added 'theShowActionButton'
		// -------------------------------------------------------------------------
		super (theContext, textViewResourceId, objects);
		// -------------------------------------------------------------------------
		// 08/01/2020 ECU the flag that indicates if the action button is to be displayed
		// -------------------------------------------------------------------------
		actionMethod			= theActionMethod;
		// -------------------------------------------------------------------------
		context 				= theContext;
		id 						= textViewResourceId;
		items 					= objects;
		// -------------------------------------------------------------------------
		// 15/12/2015 ECU set up the bitmap options
		// -------------------------------------------------------------------------
		options 				= new BitmapFactory.Options ();
		options.inSampleSize 	= 8;
		// -------------------------------------------------------------------------
		// 15/12/2015 ECU the flag that indicates if images are to be displayed
		// -------------------------------------------------------------------------
		showImageFlag			= theShowImageFlag;
		// -------------------------------------------------------------------------
		// 17/12/2015 ECU if a list view has been included in the arguments then
		//                try and set the scroll listener
		// -------------------------------------------------------------------------
		if (theListView != null)
		{
			theListView.setOnScrollListener (new OnScrollListener() 
			{
				// -----------------------------------------------------------------
				@Override
				public void onScrollStateChanged(AbsListView theView, int theScrollState) 
				{
					// -------------------------------------------------------------
					// 17/12/2015 ECU called when the scroll state changes
					// -------------------------------------------------------------
					
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				@Override
				public void onScroll (AbsListView theView,  
						int theFirstVisibleItem,
						int theVisibleItemCount, 
						int theTotalItemCount) 
				{
					
				}
				// -----------------------------------------------------------------
			});
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------

	}
	/* ============================================================================= */
	public FileOptions getItem(int theItem)
	{
		return items.get (theItem);
	}
	/* ============================================================================= */
	@Override
	public View getView (final int position, View theView, ViewGroup parent)
	{
		// -------------------------------------------------------------------------
		// 08/01/2020 ECU added the full path to a file and action button legend
		// -------------------------------------------------------------------------
		String		actionButtonLegend;
		String 		fileFullPath;
		ItemHolder 	itemHolder = null;
		// -------------------------------------------------------------------------
		if (theView == null)
		{
			// ---------------------------------------------------------------------
			LayoutInflater localViewInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			theView = localViewInflater.inflate (id, null);
			// ---------------------------------------------------------------------
			itemHolder 					= new ItemHolder ();
			// ---------------------------------------------------------------------
			// 08/01/2020 ECU added the action button
			// ---------------------------------------------------------------------
			itemHolder.actionButton     = (Button)    theView.findViewById (R.id.ActionButton);
			itemHolder.imageView		= (ImageView) theView.findViewById (R.id.FileImageView);
			itemHolder.textViewName		= (TextView)  theView.findViewById (R.id.FileView01);
			itemHolder.textViewType 	= (TextView)  theView.findViewById (R.id.FileView02);
			itemHolder.textViewDetails	= (TextView)  theView.findViewById (R.id.FileView03);
			// ---------------------------------------------------------------------
			theView.setTag(itemHolder);
		}
		else
		{
			// ---------------------------------------------------------------------
			itemHolder = (ItemHolder) theView.getTag();      
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 17/12/2015 ECU store the details of the file to which this item relates
		// -------------------------------------------------------------------------
		itemHolder.fileOptions = items.get (position);
		// -------------------------------------------------------------------------
		itemHolder.textViewName.setText (itemHolder.fileOptions.getName ());
		itemHolder.textViewType.setText (itemHolder.fileOptions.getData ());
		itemHolder.textViewDetails.setText (itemHolder.fileOptions.getModificationTime());
		// -------------------------------------------------------------------------
		// 10/12/2013 ECU decide which icon is to be displayed
		// -------------------------------------------------------------------------
		if (itemHolder.fileOptions.isParent ())
			itemHolder.imageView.setImageResource (R.drawable.parent);
		else
		if (itemHolder.fileOptions.isDirectory ())
			itemHolder.imageView.setImageResource (R.drawable.folder);   
		else
		{
			// ---------------------------------------------------------------------
			// 10/12/2013 ECU this is a file try and display a more meaningful
			//                icon to display
			// 01/01/2014 ECU changed to use displayImage
			// 15/12/2015 ECU add locale to 'toLowerCase'
			//            ECU changed from FileChooser.displayImage to
			//                showImageFlag
			// ---------------------------------------------------------------------
			if (showImageFlag && itemHolder.fileOptions.getName().toLowerCase(Locale.getDefault()).endsWith(StaticData.EXTENSION_PHOTOGRAPH))
			{
				// -----------------------------------------------------------------
				// 15/12/2015 ECU read the image from disk and then update the
				//                displayed image
				// -----------------------------------------------------------------
				new DownloadAsyncTask().execute (itemHolder);
				// -----------------------------------------------------------------
			}	
			else
			{
				itemHolder.imageView.setImageResource (R.drawable.file);
			}
			// ---------------------------------------------------------------------
			// 08/01/2020 ECU now decide if the action button is to be displayed
			// ---------------------------------------------------------------------
			if (actionMethod != null)
			{
				// -----------------------------------------------------------------
				// 08/01/2020 ECU get the full path name of the file
				// -----------------------------------------------------------------
				fileFullPath = items.get (position).getFullFileName ();
				// -----------------------------------------------------------------
				// 08/01/2020 ECU decide which legend to display depending on
				//                the extension of the file
				//            ECU changed to use resource
				// -----------------------------------------------------------------
				actionButtonLegend = null;
				//------------------------------------------------------------------
				if (fileFullPath.endsWith (StaticData.EXTENSION_MUSIC) ||
					fileFullPath.endsWith (StaticData.EXTENSION_VIDEO) ||
					fileFullPath.endsWith (StaticData.EXTENSION_AUDIO))
					actionButtonLegend = context.getString (R.string.action_play);
				else
				if (fileFullPath.endsWith (StaticData.EXTENSION_DOCUMENT))
					actionButtonLegend = context.getString (R.string.action_display);
				else
				if (fileFullPath.endsWith (StaticData.EXTENSION_TEXT))
					actionButtonLegend = context.getString (R.string.action_read);
				// -----------------------------------------------------------------
				// 08/01/2020 ECU if a legend has been set then make the button visible
				//                and display the appropriate legend
				// -----------------------------------------------------------------
				if (actionButtonLegend != null)
				{
					// -------------------------------------------------------------
					// 08/01/2020 ECU set the legend appropriately
					// -------------------------------------------------------------
					itemHolder.actionButton.setText (actionButtonLegend);
					// -------------------------------------------------------------
					// 08/01/2020 ECU first of all make the button visible
					// -------------------------------------------------------------
					itemHolder.actionButton.setVisibility (View.VISIBLE);
					// -------------------------------------------------------------
					// 08/01/2020 ECU set up a 'click listener' for the button
					// -------------------------------------------------------------
					itemHolder.actionButton.setOnClickListener (new View.OnClickListener ()
					{
						@Override
						public void onClick (View view)
						{
							try
							{
								// -------------------------------------------------
								// 08/01/2020 ECU invoke the method that has been
								//                defined as the action to take
								// -------------------------------------------------
								Utilities.invokeMethod (actionMethod,new Object [] {context,items.get (position).getFullFileName()});
								// -------------------------------------------------
							}
							catch (Exception theException)
							{
							}
							// -----------------------------------------------------
						}
					});
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}               
		return theView;
	}
	//==============================================================================
	public void setShowImageFlag (boolean theShowImageFlag)
	{
		// -------------------------------------------------------------------------
		// 15/12/2015 ECU created to update the flag and to try and get a refresh
		// -------------------------------------------------------------------------
		showImageFlag = theShowImageFlag;
		notifyDataSetInvalidated ();
		// -------------------------------------------------------------------------
	}	
	// =============================================================================	
	private class DownloadAsyncTask extends AsyncTask<ItemHolder, Void, ItemHolder> 
	{
		// -------------------------------------------------------------------------
		@Override
		protected ItemHolder doInBackground (ItemHolder... params) 
		{
			// ---------------------------------------------------------------------
			// 15/12/2015 ECU get the bitmap from disk
			// ---------------------------------------------------------------------
			ItemHolder itemHolder  = params [0];
			// ---------------------------------------------------------------------
			// 22/11/2017 ECU changed to use 'getBitMap' which will take account of 
			//                any Exif tags
			// ---------------------------------------------------------------------
			itemHolder.imageBitmap = Utilities.getBitMap (itemHolder.fileOptions.getPath(),options);
			// ---------------------------------------------------------------------
			// 15/12/2015 ECU return the updated item holder on completion
			// ---------------------------------------------------------------------
			return itemHolder;
		}
		// -------------------------------------------------------------------------
		@Override
		protected void onPostExecute (ItemHolder resultItemHolder) 
		{
			// ---------------------------------------------------------------------
			// 15/12/2015 ECU if an image was read then update the image
			// ---------------------------------------------------------------------
			if (resultItemHolder.imageBitmap != null) 
			{
				resultItemHolder.imageView.setImageBitmap (resultItemHolder.imageBitmap);
			}
			// ---------------------------------------------------------------------
		}
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

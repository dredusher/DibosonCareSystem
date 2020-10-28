package com.usher.diboson;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class UPnPListItem extends FrameLayout 
{
	// =============================================================================
	// 21/02/2015 ECU removed the TextView deviceState
	// -----------------------------------------------------------------------------
	private UPnPDevice 	device = null;
	private TextView 	deviceName;
	private ImageView	deviceImage;
	private ImageView	deviceStatusImage;
	// =============================================================================
	public UPnPListItem (Context context) 
	{
		super (context);
		init (context);
	}
	// =============================================================================
	private void init (final Context context) 
	{
		LayoutInflater.from(context).inflate (R.layout.wemo_list_item_layout, this);

		deviceName 			= (TextView)  findViewById (R.id.name);
		deviceImage 		= (ImageView) findViewById (R.id.logo);
		deviceStatusImage 	= (ImageView) findViewById (R.id.status_image);
		// -------------------------------------------------------------------------
		// 07/09/2016 ECU set up a click listener on an image
		// -------------------------------------------------------------------------
		deviceStatusImage.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick (View view) 
			{
				// ----------------------------------------------------------------
				// 07/09/2016 ECU toggle the state of the device
				// 13/09/2016 ECU the 'false' indicates that this is not a call due
				//                to 'recursion'
				// ----------------------------------------------------------------
				device.changeState (false); 
				// ----------------------------------------------------------------
				deviceStatusImage.setImageDrawable (context.getResources().getDrawable (R.drawable.switch_undefined));
				// ----------------------------------------------------------------
			}
		});
		// ------------------------------------------------------------------------
	}
	// =============================================================================
	public void setDevice (UPnPDevice theDevice) 
	{
		device = theDevice;
		deviceName.setText (device.upnpDevice.getFriendlyName());	
		// -------------------------------------------------------------------------
		//          ECU try and get the image of the device from storage
		// -------------------------------------------------------------------------
		if (device.deviceImageBitmap != null) 
		{
			// ---------------------------------------------------------------------
		    //     ECU load the image from local storage
			// ---------------------------------------------------------------------
			deviceImage.setImageBitmap (device.deviceImageBitmap);
		    // ---------------------------------------------------------------------
		} 
		// -------------------------------------------------------------------------
		// 07/09/2016 ECU set up the status window
		// -------------------------------------------------------------------------
		deviceStatusImage.setImageDrawable (MainActivity.activity.getResources().getDrawable ((device.deviceState == 0) ? R.drawable.switch_off : R.drawable.switch_on ));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public UPnPDevice getDevice ()
	{
		return device;
	}
	// =============================================================================
}
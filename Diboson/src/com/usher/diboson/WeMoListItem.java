package com.usher.diboson;

import java.io.File;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.belkin.wemo.localsdk.WeMoDevice;

public class WeMoListItem extends FrameLayout 
{
	// =============================================================================
	// 21/02/2015 ECU removed the TextView deviceState
	// -----------------------------------------------------------------------------
	private WeMoDevice 	device = null;
	private TextView 	deviceName;
	private ImageView	deviceImage;
	private ImageView	deviceStatus;
	// =============================================================================
	public WeMoListItem(Context context) 
	{
		super (context);
		init (context);
	}
	// =============================================================================
	private void init (Context context) 
	{
		LayoutInflater.from(context).inflate (R.layout.wemo_list_item_layout, this);

		deviceName 		= (TextView)  findViewById (R.id.name);
		deviceImage 	= (ImageView) findViewById (R.id.logo);
		deviceStatus 	= (ImageView) findViewById (R.id.status_image);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void setDevice (WeMoDevice theDevice) 
	{
		device = theDevice;
		deviceName.setText(device.getFriendlyName());	
		setState (device.getState().split("\\|")[0]);
		// -------------------------------------------------------------------------
		//          ECU try and get the image of the device from storage
		// -------------------------------------------------------------------------
		File logo = new File (device.getLogo());
		// -------------------------------------------------------------------------
		if (logo.exists ()) 
		{
			// ---------------------------------------------------------------------
			//          ECU load the image from local storage
			// ---------------------------------------------------------------------
		    Bitmap bitmap = BitmapFactory.decodeFile (logo.getAbsolutePath());
		    deviceImage.setImageBitmap(bitmap);
		    // ---------------------------------------------------------------------
		} 
	}
	// =============================================================================
	public WeMoDevice getDevice()
	{
		return device;
	}
	// =============================================================================
	public void setState (String state) 
	{
		int		statusImage = StaticData.NO_RESULT;
		
		String type = device.getType();
		
		if (type.equals(WeMoDevice.SWITCH) || type.equals(WeMoDevice.LIGHT_SWITCH)) 
		{
			// ---------------------------------------------------------------------
			// 16/02/2015 ECU SWITCH or LIGHT SWITCH
			//                ======================
			// ---------------------------------------------------------------------
			if (state.equals(WeMoDevice.WEMO_DEVICE_ON)) 
			{
				statusImage = R.drawable.switch_on;	
			} 
			else if (state.equals(WeMoDevice.WEMO_DEVICE_OFF)) 
			{
				statusImage = R.drawable.switch_off;
			} 
			else if (state.equals(WeMoDevice.WEMO_DEVICE_UNDEFINED)) 
			{
				statusImage = R.drawable.switch_undefined;
			}
		} 
		else if (type.equals(WeMoDevice.SENSOR)) 
		{
			// ---------------------------------------------------------------------
			// 16/02/2015 ECU SENSOR
			//                ======
			// ---------------------------------------------------------------------
			//stateText = state.equals(WeMoDevice.WEMO_DEVICE_ON) ? getResources().getString (R.string.state_motion)
			//													: getResources().getString (R.string.state_wait);
			// ---------------------------------------------------------------------
		} 
		else if (type.equals(WeMoDevice.INSIGHT)) 
		{
			// ---------------------------------------------------------------------
			// 16/02/2015 ECU INSIGHT
			//                =======
			// ---------------------------------------------------------------------
			if (state.equals(WeMoDevice.WEMO_DEVICE_ON)) 
			{
				statusImage = R.drawable.switch_on;
			} 
			else if (state.equals(WeMoDevice.WEMO_DEVICE_OFF)) 
			{
				statusImage = R.drawable.switch_off;
			}
			else if (state.equals(WeMoDevice.WEMO_DEVICE_STAND_BY)) 
			{
				statusImage = R.drawable.switch_standby;
			} 
			else if (state.equals(WeMoDevice.WEMO_DEVICE_UNDEFINED)) 
			{
				
			}
		}
		// -------------------------------------------------------------------------
		// 17/02/2015 ECU now change the displayed information
		// 21/02/2015 ECU removed the setting up of deviceState text and colour
		// -------------------------------------------------------------------------
		if (statusImage != StaticData.NO_RESULT)
		{
			Drawable myDrawable = getResources().getDrawable(statusImage);
			deviceStatus.setImageDrawable(myDrawable);
		}
	}
	// =============================================================================
}
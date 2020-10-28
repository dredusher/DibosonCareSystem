package com.usher.diboson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener
{
	// =============================================================================
	private int 		initialValue 		= 0;		// 04/05/2016 ECU added
	private int 		maximumValue	 	= 0;
	private int			minimumValue 		= 0;
	private int 		scale	 			= StaticData.NO_RESULT;		
														// 10/12/2015 ECU added
	private SeekBar 	seekBar 	 		= null;
	private int 		stepSize 			= 1;
	private String		subTitle			= null;
	private TextView    subTitleTextView	= null;
	private String 		units 				= null;
	private String		update				= null;		// 05/03/2015 ECU added
	UpdateHandler		updateHandler;					// 05/03/2015 ECU added
	private TextView	updateTextView		= null;		// 05/03/2015 ECU added
	private int 		value 				= 0;
	private TextView 	valueTextView		= null;
	// -----------------------------------------------------------------------------

	// =============================================================================
	public SeekBarPreference (Context theContext, AttributeSet theAttributes) 
	{
		// -------------------------------------------------------------------------
		super (theContext,theAttributes);
		// -------------------------------------------------------------------------
		// 20/11/2014 ECU check for any variables that have been fed through
		//            ECU added subTitle
		// -------------------------------------------------------------------------
		TypedArray typedArray 
			= theContext.obtainStyledAttributes (theAttributes,R.styleable.SeekBarDialogPreference);
		// -------------------------------------------------------------------------
		maximumValue = typedArray.getInteger (R.styleable.SeekBarDialogPreference_maximumValue, 0);
		minimumValue = typedArray.getInteger (R.styleable.SeekBarDialogPreference_minimumValue, 0);
		scale		 = typedArray.getInteger (R.styleable.SeekBarDialogPreference_scale,StaticData.NO_RESULT);
		stepSize	 = typedArray.getInteger (R.styleable.SeekBarDialogPreference_stepSize, 1);
		subTitle	 = typedArray.getString  (R.styleable.SeekBarDialogPreference_subTitle);
		units 		 = typedArray.getString  (R.styleable.SeekBarDialogPreference_units);
		// -------------------------------------------------------------------------
		// 05/03/2015 ECU check if there is an 'update' field which needs updating
		// -------------------------------------------------------------------------
		update 		 = typedArray.getString  (R.styleable.SeekBarDialogPreference_update);
		// -------------------------------------------------------------------------
		// 06/03/2015 ECU if the units are supplied then adjust to a single unit
		//                i.e. if 'seconds' is specified then change to 'second'
		//                so that it can be adjusted later when displaying the
		//                value
		// -------------------------------------------------------------------------
		if (units != null && units.endsWith ("s"))
		{
			// ---------------------------------------------------------------------
			// 06/03/2015 ECU strip off the trailing 's'
			// ---------------------------------------------------------------------
			units = units.substring (0,units.length() - 1);
		}
		// -------------------------------------------------------------------------
		// 20/11/2014 ECU release the array - must not use again
		// -------------------------------------------------------------------------
		typedArray.recycle ();
		// -------------------------------------------------------------------------
		// 20/11/2014 ECU set up the text of the buttons
		// -------------------------------------------------------------------------
	    setPositiveButtonText ("Set");
	    setNegativeButtonText ("Cancel");
	    // -------------------------------------------------------------------------
	}
	// =============================================================================
	@SuppressLint ("InflateParams") 
	protected View onCreateDialogView () 
	{
		LayoutInflater layoutInflater = LayoutInflater.from (getContext());
		// -------------------------------------------------------------------------
		// 20/11/2014 ECU display the custom layout for the seek bar
		// -------------------------------------------------------------------------
		View view = layoutInflater.inflate (R.layout.seekbar_preference, null);
		// -------------------------------------------------------------------------
		// 20/11/2014 ECU set up the views for the seek bar and the text field which
		//                will display the value
		//			  ECU added subTitle.....
		// 05/03/2015 ECU added update.....
		// -------------------------------------------------------------------------
		seekBar 			= (SeekBar) view.findViewById (R.id.seekbar);
		subTitleTextView	= (TextView)view.findViewById (R.id.subTitleText);
		updateTextView		= (TextView)view.findViewById (R.id.updateText);
		valueTextView 		= (TextView)view.findViewById (R.id.valueText);
		// -------------------------------------------------------------------------
		// 20/11/2014 ECU sort out the subtitle field
		// -------------------------------------------------------------------------
		if (subTitle != null)
		{
			subTitleTextView.setText (subTitle);
			// ---------------------------------------------------------------------
			// 20/11/2014 ECU and make the field visible
			// ---------------------------------------------------------------------
			subTitleTextView.setVisibility(View.VISIBLE);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 05/03/2015 ECU check if there is an 'update' field that needs updating
		//            ECU at the moment only the light level is available for update
		//                so check that the SensorService is running - if its
		//                lightLevel variable is still on MainActivity.NO_RESULT
		//                then the service is not running which means that the
		//                device does not have a light sensor
		// 07/03/2015 ECU changed the logic (see above) to check whether the
		//                device has the sensor directly rather than through 
		//                the lightLevel
		// -------------------------------------------------------------------------
		if ((update != null) && (SensorService.ambientLightSensor))
		{
			// ---------------------------------------------------------------------
			// 20/11/2014 ECU and make the field visible
			// ---------------------------------------------------------------------
			updateTextView.setVisibility(View.VISIBLE);
			// ---------------------------------------------------------------------
			// 05/03/2015 ECU start up the handler which will update the value 
			// ---------------------------------------------------------------------
			updateHandler = new UpdateHandler ();
			updateHandler.sleep (1000);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 20/11/2014 ECU get the persistent value and adjust for the minimum
		//                value
		// -------------------------------------------------------------------------
		value = getPersistedInt (value + minimumValue) - minimumValue;
		// -------------------------------------------------------------------------
		// 20/11/2014 ECU just check that the impossible doesn't cause issues
		// -------------------------------------------------------------------------
		if (value < 0) 
			value = 0;
		// -------------------------------------------------------------------------
		seekBar.setKeyProgressIncrement (stepSize);
		seekBar.setMax (maximumValue - minimumValue);
		seekBar.setProgress (value);
		// -------------------------------------------------------------------------
		// 20/11/2014 ECU make sure that the initial display is correct
		// -------------------------------------------------------------------------
		updateDisplay (value);
		// -------------------------------------------------------------------------
		seekBar.setOnSeekBarChangeListener (this);
		// -------------------------------------------------------------------------
		return view;
	}
	// =============================================================================
	@Override
	protected Object onGetDefaultValue(TypedArray theArray, int theIndex) 
	{
		return (theArray.getString(theIndex));
	}
	// =============================================================================
	public void onProgressChanged (SeekBar theSeekBar, int theNewValue,boolean fromTouch) 
	{
		// -------------------------------------------------------------------------
		// 20/11/2014 ECU make sure that the display is updated
		// -------------------------------------------------------------------------
		updateDisplay (theNewValue);
		// -------------------------------------------------------------------------
		// 20/11/2014 ECU let the listener know the new value
		// -------------------------------------------------------------------------
		callChangeListener (value + minimumValue);
	}
	// =============================================================================
    @Override
    protected void onSetInitialValue (boolean restoreValue, Object defaultValue)
    {
    	// -------------------------------------------------------------------------
    	// 19/11/2014 ECU called up to set the initial value
    	// -------------------------------------------------------------------------
    	value = 0;
        // -------------------------------------------------------------------------
        // 19/11/2014 ECU check whether the stored value is to be restored
        // -------------------------------------------------------------------------
        if (restoreValue) 
        {
        	value = getPersistedInt ((defaultValue == null) ? 0 : (Integer) defaultValue);
        }
        else 
        {
        	value = (defaultValue == null) ? 0 : (Integer) defaultValue; 
           
            if (shouldPersist()) 
            {
                persistInt (value + minimumValue) ;
            }
        }
        // -------------------------------------------------------------------------
        // 04/05/2016 ECU remember the initial value
        // -------------------------------------------------------------------------
        initialValue = value;
        // -------------------------------------------------------------------------
    }
	// =============================================================================
	public void onStartTrackingTouch(SeekBar seek) 
	{
	}
	// =============================================================================
	public void onStopTrackingTouch(SeekBar seek) 
	{
	}
	// =============================================================================
	public void onClick (DialogInterface theDialogue, int which) 
	{
		// -------------------------------------------------------------------------
		// 20/11/2014 ECU if the 'Set' button is pressed then have the required
		//                value
		// 04/05/2016 ECU added the negative to redisplay the initial value 
		// -------------------------------------------------------------------------
		if (which == DialogInterface.BUTTON_POSITIVE) 
		{
			if (shouldPersist()) 
			{
				persistInt (value + minimumValue);
			}
		}
		else
		if (which == DialogInterface.BUTTON_NEGATIVE) 
		{
			// ---------------------------------------------------------------------
			// 04/05/2016 ECU on a cancel then make sure that the displayed value
			//                is that before any changes were made
			// 12/12/2016 ECU remove the '+ minimumValue'
			// ---------------------------------------------------------------------
			callChangeListener (initialValue);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 05/03/2015 ECU if the thread is running then interrupt it
		// 06/03/2015 ECU put in the check on NO_RESULT for those devices which
		//				  do not have an ambient light sensor
		// 07/03/2015 ECU changed to check for the sensor directly rather than the
		//                default on the 'lightLevel'
		// -------------------------------------------------------------------------
		if (update != null && SensorService.ambientLightSensor)
		{
			// ---------------------------------------------------------------------
			// 05/03/2015 ECU stop the handler from running
			// ---------------------------------------------------------------------
			updateHandler.removeMessages (0);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		super.onClick (theDialogue, which);
	}
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class UpdateHandler extends Handler
	{
		@Override
	    public void handleMessage (Message theMessage) 
	    {   
			// ---------------------------------------------------------------------
			// 05/03/2015 ECU display the updated value which at the moment is just
			//                the current light level
			// ---------------------------------------------------------------------
			updateTextView.setText (update + " " + String.format ("%.0f",SensorService.lightLevel));
			// ---------------------------------------------------------------------			
			sleep (200);
	    }
	    /* ------------------------------------------------------------------------ */
	    public void sleep (long theDelayInMilliseconds)
	    {		
	    	// --------------------------------------------------------------------
	    	// 04/05/2016 ECU change to _SLEEP from 0
	    	// --------------------------------------------------------------------
	        this.removeMessages (StaticData.MESSAGE_SLEEP);
	        sendMessageDelayed (obtainMessage (StaticData.MESSAGE_SLEEP), theDelayInMilliseconds);
	        // ---------------------------------------------------------------------
	    }
	};
	// =============================================================================
	void updateDisplay (int theValue)
	{
		// -------------------------------------------------------------------------
		// 10/12/2015 ECU check if the units specifies float
		// -------------------------------------------------------------------------
		if (scale != StaticData.NO_RESULT)
		{
			// ---------------------------------------------------------------------
			// 10/12/2015 ECU this seekbar is using 'float' variable
			// ---------------------------------------------------------------------
			value = ((stepSize >= 1) ? Math.round (theValue/stepSize)*stepSize : theValue);
			float localValue = ((float) value) / ((float) scale);
			valueTextView.setText (String.valueOf(1.0f + localValue));
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 20/11/2014 ECU adjust the value by the step size, i.e. round down
			// ---------------------------------------------------------------------
			value = ((stepSize >= 1) ? Math.round (theValue/stepSize)*stepSize : theValue);
			// ---------------------------------------------------------------------
			// 20/11/2014 ECU display the updated value
			// 06/03/2015 ECU adjust the display of units to accommodate a trailing 's'
			// 31/03/2016 ECU use the method to decide on the trailing 's'
			// ---------------------------------------------------------------------
			valueTextView.setText (String.valueOf (value + minimumValue) + 
									(units == null ? StaticData.BLANK_STRING 
											       : (" " + units + Utilities.AddAnS (value + minimumValue))));
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
}


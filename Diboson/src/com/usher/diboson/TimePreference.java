package com.usher.diboson;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

@SuppressLint ({"DefaultLocale", "InflateParams" })
public class TimePreference extends DialogPreference 
{
	// =============================================================================
	protected boolean 		is24HourFormat;
    protected int 			lastHour	= 0;
    protected int 			lastMinute	= 0;
    private   String		subTitle = null;				// 22/11/2014 ECU added
    protected TextView		subTitleTextView = null;		// 22/11/2014 ECU added
    protected TextView 		timeDisplay;
    protected TimePicker	timePicker  = null;
    // =============================================================================
    public TimePreference (Context theContext) 
    {
        this (theContext, null);
    }
    // =============================================================================
    public TimePreference (Context theContext, AttributeSet theAttributes) 
    {
    	// -------------------------------------------------------------------------
    	// 19/11/2014 ECU please note that originally I had ..,0); but this
    	//                resulted in the title font being a little larger than
    	//                for other preferences. By using R.attr... the
    	//                problem seemed to be solved
    	// -------------------------------------------------------------------------
        this (theContext,theAttributes,android.R.attr.dialogPreferenceStyle);
        // -------------------------------------------------------------------------
        // 22/11/2014 ECU try and get the supplied sub title that will be supplied
        //                in the .xml file
        // -------------------------------------------------------------------------
        TypedArray typedArray 
			= theContext.obtainStyledAttributes(theAttributes,R.styleable.TimeDialogPreference);
        
		subTitle	 = typedArray.getString(R.styleable.TimeDialogPreference_timeSubTitle);
		// -------------------------------------------------------------------------
		// 22/11/2014 ECU release the array - must not use again
		// -------------------------------------------------------------------------
		typedArray.recycle ();
		// -------------------------------------------------------------------------
    }
    // =============================================================================
    public TimePreference (Context theContext, AttributeSet theAttributeSet, int theStyle) 
    {
        super (theContext,theAttributeSet,theStyle);
        // -------------------------------------------------------------------------
        // 19/11/2014 ECU get the format that is being used for the time, i.e.
        //                either 12 hour or 24 hour
        // -------------------------------------------------------------------------
        is24HourFormat = DateFormat.is24HourFormat (theContext);
        // -------------------------------------------------------------------------
        // 19/11/2014 ECU initialise the displayed buttons
        // -------------------------------------------------------------------------
        setPositiveButtonText ("Set");
        setNegativeButtonText ("Cancel");
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public String toString () 
    {	
        if(is24HourFormat) 
        {
            // ---------------------------------------------------------------------
            // 03/12/2019 ECU changed to use TIME...
            // ---------------------------------------------------------------------
            return (String.format(StaticData.TIME_FORMAT,lastHour,lastMinute));
        } 
        else 
        {
        	// ---------------------------------------------------------------------
        	// 19/11/2014 ECU adjust for a 12 hour time format
        	// ---------------------------------------------------------------------
            int myHour = lastHour % 12;
            if (myHour == 0) myHour = 12;
            // ---------------------------------------------------------------------
            // 03/12/2019 ECU changed to use TIME...
            // ---------------------------------------------------------------------
            return (String.format(StaticData.TIME_FORMAT,myHour,lastMinute) +
            				((lastHour >= 12) ? " PM" : " AM"));
            // ---------------------------------------------------------------------
        }
    }
    // =============================================================================
    @Override
    protected View onCreateDialogView () 
    {
    	// -------------------------------------------------------------------------
    	// 22/11/2014 ECU try and use my own layout so that a subtitle can be
    	//                displayed
    	// -------------------------------------------------------------------------
    	LayoutInflater layoutInflater = LayoutInflater.from (getContext());
		// -------------------------------------------------------------------------
		// 20/11/2014 ECU display the custom layout for the seek bar
    	// 20/07/2019 ECU the 'suppressLint' added because of the use of 'null'
		// -------------------------------------------------------------------------
		View view = layoutInflater.inflate (R.layout.time_preference, null);
    	// -------------------------------------------------------------------------
    	// 19/11/2014 ECU set up any attributes required of the time picker
    	// -------------------------------------------------------------------------
		timePicker			= (TimePicker) view.findViewById (R.id.timePicker);
       	subTitleTextView	= (TextView) view.findViewById   (R.id.subTitleText);
		// -------------------------------------------------------------------------
		// 22/11/2014 ECU sort out the subtitle field
		// -------------------------------------------------------------------------
		if (subTitle != null)
		{
			subTitleTextView.setText(subTitle);
			// ---------------------------------------------------------------------
			// 20/11/2014 ECU and make the field visible
			// ---------------------------------------------------------------------
			subTitleTextView.setVisibility(View.VISIBLE);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
        return (view);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    protected void onBindDialogView (View theView) 
    {
        super.onBindDialogView (theView);
        // -------------------------------------------------------------------------
        // 19/11/2014 ECU initialise the displayed time picker
        // -------------------------------------------------------------------------
        timePicker.setIs24HourView	(is24HourFormat);
        timePicker.setCurrentHour  	(lastHour);
        timePicker.setCurrentMinute	(lastMinute);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public void onBindView (View view) 
    {
    	// -------------------------------------------------------------------------
        View widgetLayout;
        int childCounter = 0;
        // -------------------------------------------------------------------------
        do {
            widgetLayout = ((ViewGroup) view).getChildAt (childCounter);
            childCounter++;
        }
        // -------------------------------------------------------------------------
        while (widgetLayout.getId() != android.R.id.widget_frame);
        ((ViewGroup) widgetLayout).removeAllViews();
        // -------------------------------------------------------------------------
        timeDisplay = new TextView (widgetLayout.getContext());
        timeDisplay.setText (toString());
        // -------------------------------------------------------------------------
        ((ViewGroup) widgetLayout).addView (timeDisplay);
        super.onBindView (view);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    protected void onDialogClosed (boolean positiveResult) 
    {
        // -------------------------------------------------------------------------
        // 19/11/2014 ECU check whether 'Set' button pressed
        // 20/07/2019 ECU added the try/catch because getting a NPE on the clear 
        //                focus and just checking that 'timePicker != null' did
        //                not solve it
        // -------------------------------------------------------------------------
        if (positiveResult) 
        {
        	// ---------------------------------------------------------------------
        	try
        	{
        		// -----------------------------------------------------------------
        		// 19/11/2014 ECU make sure that the displayed value is updated
        		// -----------------------------------------------------------------
        		timePicker.clearFocus ();
        		// -----------------------------------------------------------------
        		// 19/11/2014 ECU get the input values from the picker
        		// -----------------------------------------------------------------
        		lastHour	=	timePicker.getCurrentHour ();
        		lastMinute	=	timePicker.getCurrentMinute ();
                // -----------------------------------------------------------------
                // 03/12/2019 ECU changed to use TIME...
                // -----------------------------------------------------------------
        		String time = String.format (StaticData.TIME_FORMAT,lastHour,lastMinute);
        		// -----------------------------------------------------------------
        		if (callChangeListener (time)) 
        		{
        			// -------------------------------------------------------------
        			persistString (time);
        			timeDisplay.setText (toString());
        			// -------------------------------------------------------------
        		}
        		// -----------------------------------------------------------------
        	}
        	catch (Exception theException)
        	{
        		// -----------------------------------------------------------------
        		// -----------------------------------------------------------------
        	}
        	// ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 20/07/2019 ECU moved here from the start of this method - because of the
        //                NPE mentioned above
        // -------------------------------------------------------------------------
        super.onDialogClosed (positiveResult);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    protected Object onGetDefaultValue (TypedArray theArray, int theIndex) 
    {
        // -------------------------------------------------------------------------
        return (theArray.getString(theIndex));
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    protected void onSetInitialValue (boolean restoreValue, Object defaultValue)
    {
    	// -------------------------------------------------------------------------
    	// 19/11/2014 ECU called up to set the initial value
    	// -------------------------------------------------------------------------
        String time=null;
        // -------------------------------------------------------------------------
        // 19/11/2014 ECU check whether the stored value is to be restored
        // -------------------------------------------------------------------------
        if (restoreValue) 
        {
        	time = getPersistedString ((defaultValue == null) ? "00:00" : defaultValue.toString());
        }
        else 
        {
        	time = (defaultValue == null) ? "00:00" : defaultValue.toString(); 
           
            if (shouldPersist()) 
            {
                persistString (time);
            }
        }
        // -------------------------------------------------------------------------
        // 19/11/2014 ECU get the time components from the string
        // 13/12/2019 ECU changed to use Static....
        // -------------------------------------------------------------------------
        String[] timeParts = time.split (StaticData.ACTION_DELIMITER);
        // -------------------------------------------------------------------------
        lastHour	=	Integer.parseInt (timeParts[0]);
        lastMinute	=	Integer.parseInt (timeParts[1]);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
}

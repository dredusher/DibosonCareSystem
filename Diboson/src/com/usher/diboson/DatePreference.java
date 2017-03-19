package com.usher.diboson;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

@SuppressLint("DefaultLocale")
public class DatePreference extends DialogPreference 
{
	// =============================================================================
	protected int 			lastDay				= 0;
	protected int 			lastMonth			= 0;
	protected int			lastYear			= 0;
    private   String		subTitle 			= null;		// 22/11/2014 ECU added
    protected TextView		subTitleTextView 	= null;		// 22/11/2014 ECU added
    protected TextView 		dateDisplay;
    protected DatePicker	datePicker  		= null;
    // =============================================================================
    public DatePreference (Context theContext) 
    {
        this (theContext, null);
    }
    // =============================================================================
    public DatePreference (Context theContext, AttributeSet theAttributes) 
    {
    	// -------------------------------------------------------------------------
    	// 22/04/2015 ECU please note that originally I had ..,0); but this
    	//                resulted in the title font being a little larger than
    	//                for other preferences. By using R.attr... the
    	//                problem seemed to be solved
    	// -------------------------------------------------------------------------
        this (theContext,theAttributes,android.R.attr.dialogPreferenceStyle);
        // -------------------------------------------------------------------------
        // 22/04/2015 ECU try and get the supplied sub title that will be supplied
        //                in the .xml file
        // -------------------------------------------------------------------------
        TypedArray typedArray 
			= theContext.obtainStyledAttributes(theAttributes,R.styleable.DateDialogPreference);
        
		subTitle	 = typedArray.getString(R.styleable.DateDialogPreference_dateSubTitle);
		// -------------------------------------------------------------------------
		// 22/11/2014 ECU release the array - must not use again
		// -------------------------------------------------------------------------
		typedArray.recycle();
		// -------------------------------------------------------------------------
    }
    // =============================================================================
    public DatePreference (Context theContext, AttributeSet theAttributeSet, int theStyle) 
    {
        super (theContext,theAttributeSet,theStyle);
        // -------------------------------------------------------------------------
        // 22/04/2015 ECU initialise the displayed buttons
        // -------------------------------------------------------------------------
        setPositiveButtonText ("Set");
        setNegativeButtonText ("Cancel");
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public String toString () 
    {	
        return (String.format ("%02d/%02d/%4d",lastDay,lastMonth,lastYear));
    }
    // =============================================================================
    @Override
    protected View onCreateDialogView () 
    {
    	// -------------------------------------------------------------------------
    	// 22/04/2015 ECU try and use my own layout so that a subtitle can be
    	//                displayed
    	// -------------------------------------------------------------------------
    	LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		// -------------------------------------------------------------------------
		// 22/04/2015 ECU display the custom layout for the date picker
		// -------------------------------------------------------------------------
		View view = layoutInflater.inflate (R.layout.date_preference, null);
    	// -------------------------------------------------------------------------
    	// 22/04/2015 ECU set up any attributes required of the time picker
    	// -------------------------------------------------------------------------
		datePicker			= (DatePicker)	view.findViewById(R.id.datePicker);
       	subTitleTextView	= (TextView)	view.findViewById(R.id.subTitleText);
		// -------------------------------------------------------------------------
		// 22/04/2015 ECU sort out the subtitle field
		// -------------------------------------------------------------------------
		if (subTitle != null)
		{
			subTitleTextView.setText(subTitle);
			// ---------------------------------------------------------------------
			// 22/04/2015 ECU and make the field visible
			// ---------------------------------------------------------------------
			subTitleTextView.setVisibility(View.VISIBLE);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
        return (view);
    }
    // =============================================================================
    @Override
    protected void onBindDialogView (View theView) 
    {
        super.onBindDialogView (theView);
        // -------------------------------------------------------------------------
        // 22/04/2015 ECU initialise the displayed time picker
        //            ECU dob month is stored with January = 1 but datePicker uses
        //                January = 0
        // -------------------------------------------------------------------------
        datePicker.updateDate (lastYear,lastMonth-1, lastDay);
       // --------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public void onBindView (View view) 
    {
        View widgetLayout;
        int childCounter = 0;
        do 
        {
            widgetLayout = ((ViewGroup) view).getChildAt(childCounter);
            childCounter++;
        } 
        while (widgetLayout.getId() != android.R.id.widget_frame); 
        	((ViewGroup) widgetLayout).removeAllViews();
        
        dateDisplay = new TextView (widgetLayout.getContext());
        dateDisplay.setText(toString());
        
        ((ViewGroup) widgetLayout).addView (dateDisplay);
        super.onBindView (view);
    }
    // =============================================================================
    @Override
    protected void onDialogClosed (boolean positiveResult) 
    {
        super.onDialogClosed(positiveResult);
        // -------------------------------------------------------------------------
        // 22/04/2015 ECU check whether 'Set' button pressed
        // -------------------------------------------------------------------------
        if (positiveResult) 
        {
        	// ---------------------------------------------------------------------
        	// 19/11/2014 ECU make sure that the displayed value is updated
        	// ---------------------------------------------------------------------
            datePicker.clearFocus();
            // ---------------------------------------------------------------------
            // 22/04/2015 ECU get the input values from the picker. Remember that
            //                'getMonth' starts with January = 0, I need it as 1
            // ---------------------------------------------------------------------
            lastDay		=	datePicker.getDayOfMonth();
            lastMonth 	= 	datePicker.getMonth() + 1;
            lastYear	= 	datePicker.getYear();
            // ---------------------------------------------------------------------
            String date = String.format ("%02d/%02d/%4d",lastDay,lastMonth,lastYear);

            if (callChangeListener (date)) 
            {
                persistString (date);
                dateDisplay.setText (toString());
            }
        }
    }
    // =============================================================================
    @Override
    protected Object onGetDefaultValue(TypedArray theArray, int theIndex) 
    {
        return (theArray.getString(theIndex));
    }
    // =============================================================================
    @Override
    protected void onSetInitialValue (boolean restoreValue, Object defaultValue)
    {
    	// -------------------------------------------------------------------------
    	// 22/04/2015 ECU called up to set the initial value
    	// -------------------------------------------------------------------------
        String date=null;
        // -------------------------------------------------------------------------
        // 22/04/2015 ECU check whether the stored value is to be restored
        // 09/11/2015 ECU use DEFAULT_DATE rather than literal
        // -------------------------------------------------------------------------
        if (restoreValue) 
        {
        	date = getPersistedString ((defaultValue == null) ? StaticData.DEFAULT_DATE 
        													  : defaultValue.toString());
        }
        else 
        {
        	date = (defaultValue == null) ? StaticData.DEFAULT_DATE 
        			                      : defaultValue.toString(); 
           
            if (shouldPersist()) 
            {
                persistString (date) ;
            }
        }
        // -------------------------------------------------------------------------
        // 22/04/2015 ECU get the time components from the string
        // -------------------------------------------------------------------------
        String[] dateParts = date.split ("/");
        // -------------------------------------------------------------------------
        // 09/11/2015 ECU check for any parsing errors
        // -------------------------------------------------------------------------
        try
        {
        	lastDay		=	Integer.parseInt (dateParts[0]);
        	lastMonth	=	Integer.parseInt (dateParts[1]);
        	lastYear	= 	Integer.parseInt (dateParts[2]);
        }
        catch (Exception theException)
        {
        	lastDay		= 	1;
        	lastMonth	= 	1;
        	lastYear	= 	1900;
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
}

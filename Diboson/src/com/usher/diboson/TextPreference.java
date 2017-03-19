package com.usher.diboson;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

@SuppressLint("DefaultLocale")
public class TextPreference extends DialogPreference 
{
	// =============================================================================
	// 25/04/2015 ECU created to provide the dialogue for obtaining text input
	// =============================================================================
	protected String	    defaultText				= null;		// 26/04/2015 ECU added
	protected TextView	    defaultAdviceTextView	= null;		// 26/04/2015 ECU added
	protected TextView	    defaultTextView			= null;		// 26/04/2015 ECU added
	protected String		input;
	protected int			inputLines				= 1;		// 26/04/2015 ECU added
	protected TextView		inputTextView 			= null;
	protected int			inputType				= 1;		// 22/02/2016 ECU added
    private   String		subTitle 				= null;		// 22/11/2014 ECU added
    protected TextView		subTitleTextView 		= null;		// 22/11/2014 ECU added
    // =============================================================================
    public TextPreference (Context theContext) 
    {
        this (theContext, null);
    }
    // =============================================================================
    public TextPreference (Context theContext, AttributeSet theAttributes) 
    {
    	// -------------------------------------------------------------------------
    	// 25/04/2015 ECU please note that originally I had ..,0); but this
    	//                resulted in the title font being a little larger than
    	//                for other preferences. By using R.attr... the
    	//                problem seemed to be solved
    	// -------------------------------------------------------------------------
        this (theContext,theAttributes,android.R.attr.dialogPreferenceStyle);
        // -------------------------------------------------------------------------
        // 25/04/2015 ECU try and get the supplied sub title that will be supplied
        //                in the .xml file
        // -------------------------------------------------------------------------
        TypedArray typedArray 
			= theContext.obtainStyledAttributes (theAttributes,R.styleable.TextDialogPreference);
        // -------------------------------------------------------------------------
        // 26/04/2015 ECU added .... defaultText
        //            ECU added .... textLines
        // 22/02/2016 ECU added .... textInput to specify the type of text input - only
        //                           the additional bits will be specified which need
        //                           to be ORed in to the default text field
        // -------------------------------------------------------------------------
        defaultText	 = typedArray.getString (R.styleable.TextDialogPreference_defaultText);
        inputLines	 = typedArray.getInteger(R.styleable.TextDialogPreference_textLines,1);
        inputType	 = typedArray.getInteger(R.styleable.TextDialogPreference_textInput,InputType.TYPE_CLASS_TEXT);
		subTitle	 = typedArray.getString (R.styleable.TextDialogPreference_textSubTitle);
		// -------------------------------------------------------------------------
		// 25/04/2015 ECU release the array - must not use again
		// -------------------------------------------------------------------------
		typedArray.recycle ();
		// -------------------------------------------------------------------------
    }
    // =============================================================================
    public TextPreference (Context theContext, AttributeSet theAttributeSet, int theStyle) 
    {
        super (theContext,theAttributeSet,theStyle);
        // -------------------------------------------------------------------------
        // 25/04/2015 ECU initialise the displayed buttons
        // 19/02/2016 ECU changed to use resources
        // -------------------------------------------------------------------------
        setPositiveButtonText (theContext.getString (R.string.set));
        setNegativeButtonText (theContext.getString (R.string.cancel));
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @SuppressLint("InflateParams") 
    @Override
    protected View onCreateDialogView () 
    {
    	// -------------------------------------------------------------------------
    	// 25/04/2015 ECU try and use my own layout so that a subtitle can be
    	//                displayed
    	// -------------------------------------------------------------------------
    	LayoutInflater layoutInflater = LayoutInflater.from (getContext());
		// -------------------------------------------------------------------------
		// 25/04/2015 ECU display the custom layout for the text field
    	// 25/07/2016 ECU choose the layout depending on whether there is default
    	//                text
    	//            ECU NOTE - originally had fields which initially had a
    	//                       visibility of GONE - if those fields were needed
    	//                       then the visibility was changed to VISIBLE but for
    	//                       some reason the layout never seemed correct and
    	//                       the tailing EditText field was either missing or
    	//                       the height was wrong.
		// -------------------------------------------------------------------------
		View view = layoutInflater.inflate ((defaultText == null) ? R.layout.text_preference
																  : R.layout.text_preference_with_default, null);
    	// -------------------------------------------------------------------------
    	// 25/04/2015 ECU set up any attributes required of the text field
		// 26/04/2015 ECU added the 'default text'
		//            ECU added advice text view 
    	// -------------------------------------------------------------------------
		inputTextView			= (TextView) view.findViewById (R.id.editText);
       	subTitleTextView		= (TextView) view.findViewById (R.id.subTitleText);
       	// -------------------------------------------------------------------------
       	// 26/04/2015 ECU adjust the input field dependent on the lines specified
       	// -------------------------------------------------------------------------
       	if (inputLines > 1)
       	{
       		// ---------------------------------------------------------------------
       		// 26/04/2015 ECU set the field to the required size and input type
       		// 22/02/2016 ECU changed to specify the input type from the variable
       		// ---------------------------------------------------------------------
       		inputTextView.setLines (inputLines);
       		inputTextView.setMaxLines (inputLines);
       		inputTextView.setInputType (inputType | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
       		// ---------------------------------------------------------------------
       	}
       	else
       	{
       		// ---------------------------------------------------------------------
       		// 22/02/2016 ECU changed to specify the input type from the variable
       		// ---------------------------------------------------------------------
       		inputTextView.setInputType (inputType);
       		// ---------------------------------------------------------------------
       	}
		// -------------------------------------------------------------------------
		// 22/11/2014 ECU sort out the subtitle field
		// -------------------------------------------------------------------------
		if (subTitle != null)
		{
			subTitleTextView.setText (subTitle);
			// ---------------------------------------------------------------------
			// 25/04/2015 ECU and make the field visible
			// ---------------------------------------------------------------------
			subTitleTextView.setVisibility (View.VISIBLE);
			// ---------------------------------------------------------------------
		} 
		// -------------------------------------------------------------------------
		// 26/04/2015 ECU added the default text for the preset value
		// 25/07/2016 ECU no need to play with the visibility because using a different
		//                layout
		// -------------------------------------------------------------------------
		if (defaultText != null)
		{
			// ---------------------------------------------------------------------
			// 25/07/2016 ECU tidy up now that two separate layouts are used
			// ---------------------------------------------------------------------
			defaultTextView			= (TextView) view.findViewById (R.id.defaultText);
			// ---------------------------------------------------------------------
			defaultTextView.setText (defaultText);
			// ---------------------------------------------------------------------
			// 26/04/2015 ECU add in the click listener
			// ---------------------------------------------------------------------
			defaultTextView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick (View view) 
				{	
					// -------------------------------------------------------------
					// 26/04/2015 ECU copy the default text into the field
					// -------------------------------------------------------------
					inputTextView.setText (defaultText);
					// -------------------------------------------------------------
				}
			});
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
        // 25/04/2015 ECU initialise the displayed text field
        // -------------------------------------------------------------------------
        inputTextView.setText (input);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public void onBindView (View view) 
    {
        super.onBindView (view);
    }
    // =============================================================================
    @Override
    protected void onDialogClosed (boolean positiveResult) 
    {
        super.onDialogClosed (positiveResult);
        // -------------------------------------------------------------------------
        // 25/04/2015 ECU check whether 'Set' button pressed
        // -------------------------------------------------------------------------
        if (positiveResult) 
        {
        	// ---------------------------------------------------------------------
        	// 25/04/2015 ECU get the text that has been entered into the field
        	// ---------------------------------------------------------------------
        	input = inputTextView.getText ().toString();
            // ---------------------------------------------------------------------
        	// 25/04/2015 ECU set the value from what was entered
        	// ---------------------------------------------------------------------
        	if (callChangeListener (input)) 
            {
               persistString (input);
            }
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    protected Object onGetDefaultValue (TypedArray theArray, int theIndex) 
    {
        return (theArray.getString(theIndex));
    }
    // =============================================================================
    @Override
    protected void onSetInitialValue (boolean restoreValue, Object defaultValue)
    {
        // -------------------------------------------------------------------------
        // 25/04/2015 ECU check whether the stored value is to be restored
    	// 22/02/2016 ECU Note - Implemented to set the initial value of the Preference. 
    	//	  	    	  If restoreValue is true, the Preference value is restored 
    	//                from the SharedPreferences. If restoreValue is false, the 
    	//				  Preference value is set to the default value that is given
    	//				  (and possibly store to SharedPreferences if shouldPersist()
    	//                is true). 
    	// 22/02/2016 ECU tidied up this section because was not really working
        // -------------------------------------------------------------------------   	
    	if (restoreValue) 
    	{
    		// ---------------------------------------------------------------------
    		// 22/02/2016 ECU get the persisted value or if not found then return the
    		//                default value, which is in the argument
    		// ---------------------------------------------------------------------
    		input = getPersistedString ((defaultValue == null) ? "" : defaultValue.toString());
    		// ---------------------------------------------------------------------
    	}
    	else 
    	{
    		// ---------------------------------------------------------------------
    		// 22/02/2016 ECU set the preference to the supplied value
    		// ---------------------------------------------------------------------
    		input = (defaultValue == null) ? "" : defaultValue.toString(); 
    		// ---------------------------------------------------------------------
    		// 22/02/2016 ECU store the value in the shared preferences if required
    		// ---------------------------------------------------------------------
    		if (shouldPersist()) 
    		{
    			persistString (input) ;
    		}
    		// ---------------------------------------------------------------------
    	}   	
    }
    // =============================================================================
    public String getText ()
    {
    	return input;
    }
    // =============================================================================
}

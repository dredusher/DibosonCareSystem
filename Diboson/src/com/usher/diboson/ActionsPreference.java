package com.usher.diboson;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

@SuppressLint("DefaultLocale")
public class ActionsPreference extends DialogPreference 
{
	// =============================================================================
	// 25/04/2015 ECU created to provide the dialogue for obtaining text input
	// =============================================================================
	protected Button		actionsButton			= null;		// 23/03/2016 ECU added
	protected Context		context;							// 23/03/2016 ECU added
	protected String	    defaultText				= null;		// 26/04/2015 ECU added
	protected TextView	    defaultAdviceTextView	= null;		// 26/04/2015 ECU added
	protected TextView	    defaultTextView			= null;		// 26/04/2015 ECU added
	protected String		input;
	protected int			inputLines				= 1;		// 26/04/2015 ECU added
	static    TextView		inputTextView 			= null;
	protected int			inputType				= 1;		// 22/02/2016 ECU added
    private   String		subTitle 				= null;		// 22/11/2014 ECU added
    protected TextView		subTitleTextView 		= null;		// 22/11/2014 ECU added
    // =============================================================================
    public ActionsPreference (Context theContext) 
    {
        this (theContext, null);
    }
    // =============================================================================
    public ActionsPreference (Context theContext, AttributeSet theAttributes) 
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
		typedArray.recycle();
		// -------------------------------------------------------------------------
        // 23/03/2016 ECU remember the context for future use
        // -------------------------------------------------------------------------
        context = theContext;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public ActionsPreference (Context theContext, AttributeSet theAttributeSet, int theStyle) 
    {
        super (theContext,theAttributeSet,theStyle);
        // -------------------------------------------------------------------------
        // 25/04/2015 ECU initialise the displayed buttons
        // 19/02/2016 ECU changed to use resources
        // -------------------------------------------------------------------------
        setPositiveButtonText (theContext.getString (R.string.set));
        setNegativeButtonText (theContext.getString (R.string.cancel));
        // -------------------------------------------------------------------------
        // 23/03/2016 ECU remember the context for future use
        // -------------------------------------------------------------------------
        context = theContext;
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
		// -------------------------------------------------------------------------
		View view = layoutInflater.inflate (R.layout.actions_preference, null);
    	// -------------------------------------------------------------------------
    	// 25/04/2015 ECU set up any attributes required of the text field
		// 26/04/2015 ECU added the 'default text'
		//            ECU added advice text view 
    	// -------------------------------------------------------------------------
		defaultAdviceTextView	= (TextView) view.findViewById (R.id.defaultTextAdvice);
		defaultTextView			= (TextView) view.findViewById (R.id.defaultText);
		inputTextView			= (TextView) view.findViewById (R.id.editText);
       	subTitleTextView		= (TextView) view.findViewById (R.id.subTitleText);
       	// -------------------------------------------------------------------------
       	// 23/03/2016 ECU get the button that will enable actions to be defined
       	// -------------------------------------------------------------------------
       	actionsButton			= (Button) view.findViewById (R.id.define_actions_button);
       	actionsButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick (View view) 
			{	
				// -------------------------------------------------------------
				// 23/03/2016 ECU try and define the action commands
				// -------------------------------------------------------------
				DialogueUtilities.multilineTextInput (context,
						  "Action Definition",
						  context.getString (R.string.action_command_summary),
						  5,
						  defaultText,
						  Utilities.createAMethod (ActionsPreference.class,"DefineActionCommand",""),
						  null,
						  StaticData.NO_RESULT,
						  context.getString (R.string.press_to_define_command));
				// -------------------------------------------------------------
			}
		});
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
		// -------------------------------------------------------------------------
		if (defaultText != null)
		{
			defaultTextView.setText (defaultText);
			// ---------------------------------------------------------------------
			// 26/04/2015 ECU and make the field visible
			// ---------------------------------------------------------------------
			defaultTextView.setVisibility (View.VISIBLE);
			// ---------------------------------------------------------------------
			// 26/04/2015 ECU also make the advice field visible
			// ---------------------------------------------------------------------
			defaultAdviceTextView.setVisibility (View.VISIBLE);
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
    
    
    // =============================================================================
    public static void DefineActionCommand (String theAction)
    {
    	// -------------------------------------------------------------------------
    	// 23/03/2016 ECU created to handle the returned action
    	// -------------------------------------------------------------------------
    	String localString = inputTextView.getText().toString();
    	// -------------------------------------------------------------------------
    	// 23/03/2016 ECU check if need to add a delimiter
    	// -------------------------------------------------------------------------
    	if (localString.length() == 0)
    		inputTextView.setText (theAction);
    	else
    		inputTextView.append (StaticData.ACTION_SEPARATOR + theAction);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
}

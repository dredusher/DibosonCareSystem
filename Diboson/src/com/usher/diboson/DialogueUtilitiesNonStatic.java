package com.usher.diboson;

import java.lang.reflect.Method;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.media.MediaPlayer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

@SuppressLint("InflateParams") 
public class DialogueUtilitiesNonStatic
{
	// =============================================================================
	// I M P O R T A N T   I M P O R T A N T   I M P O R T A N T
	// =================   =================   =================
	//
	// 22/03/2018 ECU This contains the code that was in 'DialogueUtilities' and
	//                which only 'invoked' calls to 'static' methods (the underlying
	//                object in the invoke call being set to 'null'). This class
	//                supports the 'invoke'-ing of both static and non-static
	//                methods - the underlying object being passed as an argument
	// =============================================================================
	
	// =============================================================================
	// 24/03/2016 ECU when methods are to be envoked then check that have been defined
	// 05/11/2016 ECU on all new AlertDialog.Builder then use
	//					= new AlertDialog.Builder (new ContextThemeWrapper (theContext, DIALOGUE_THEME));
	//                rather than
	//                  = new AlertDialog.Builder (theContext);
	// 13/11/2017 ECU 'listChoice' - changes made - revision 4.02.57
	// 22/03/2018 ECU IMPORTANT NOTE - when invoking a method then the first argument
	//                ==============   is the underlying object - if set to 'null'
	//                                 then the method is 'static'
	// 04/04/2018 ECU changed to use Utilities.invokeMethod rather than just '.invoke'
	// 08/08/2019 ECU 'sliderChoice' added the facility to set an exact value
	// 09/08/2019 ECU added EditText in slider dialogue
	// 10/08/2019 ECU sliderChoice - added the 'cancellable' flag and created
	//                               a new method (the old master method) which has
	//                               this flag set to 'false'
	// =============================================================================
	
	// =============================================================================
	final static int DIALOGUE_THEME = android.R.style.Theme_Holo_Light;
	// =============================================================================
	
	// =============================================================================
		   static AlertDialog 			alertDialog;				// 14/12/2015 ECU moved here from adjustFonts
	public static Context				context;					// 07/04/2015 ECU added
	public static EditText 				helpTextInput;				// 22/01/2016 ECU added
	public static Button				negativeButton  = null;		// 10/04/2015 ECU added
	public static Button				positiveButton  = null;		// 10/04/2015 ECU added
	public static AlertDialog.Builder 	seekBarBuilder;
	public static int					seekBarValue;
	public static int					selectedOption;
	public static TextView				sliderTextView;
	public static TextView				sliderValueTextView;		// 12/02/2016 ECU added
	// =============================================================================
	
	// =============================================================================
	static void adapterListChoice (Context 				theContext,
								   final Object 		theUnderlyingObject,
								   int 					theLayoutID,
								   String 				theTitle,
								   ArrayList<ListItem> 	theListItems,
								   final Method			theSelectMethod,
								   String 				theCancelLegend,
								   final Method 		theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 07/04/2015 ECU change for those dialogues which are recursive - see
		//                MusicPlayer
		// -------------------------------------------------------------------------
		context = theContext;
		// -------------------------------------------------------------------------
		// 02/04/2015 ECU created and build the dialogue 
		// -------------------------------------------------------------------------
		// 05/11/2016 ECU changed to use a theme rather than just '(theContext)'
		// -------------------------------------------------------------------------
		AlertDialog.Builder builder 
			= new AlertDialog.Builder (new ContextThemeWrapper (theContext, DIALOGUE_THEME));
		// -------------------------------------------------------------------------
		// 01/03/2015 ECU added the friendlyName
		// -------------------------------------------------------------------------
		builder.setTitle (theTitle);
		// -------------------------------------------------------------------------
		// 07/07/2016 ECU set up the adapter that will be used
		// -------------------------------------------------------------------------
		CustomListViewAdapter customListViewAdapter = new CustomListViewAdapter (context,theLayoutID,theListItems); 
		// -------------------------------------------------------------------------
		// 27/02/2015 ECU changed the preset option from '-1' to that stored so
		// 				  that can accommodate editing
		// 07/07/2016 ECU changed to use the custom adapter for the listview
		// -------------------------------------------------------------------------
		builder.setAdapter (customListViewAdapter,new DialogInterface.OnClickListener() 
		{
			// ---------------------------------------------------------------------
			public void onClick (DialogInterface dialog, int item) 
			{
				try 
				{
					// -------------------------------------------------------------
					// 10/04/2015 ECU call the method that handles item selection
					// 24/03/2016 ECU put in the check on null
					// 09/04/2018 ECU add the underlying object as an argument
					// -------------------------------------------------------------
					if (theSelectMethod != null)
						Utilities.invokeMethod ((Activity) theUnderlyingObject,theSelectMethod,new Object [] {item});
					// -------------------------------------------------------------
				} 
				catch (Exception theException) 
				{	
					theException.printStackTrace ();
				} 
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		})
		// -------------------------------------------------------------------------
		.setNegativeButton (theCancelLegend, new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick (DialogInterface dialog, int id)
			{
				// -----------------------------------------------------------------
				try 
				{
					// -------------------------------------------------------------
					// 10/04/2015 ECU call the method that handles the cancellation
					// 24/03/2016 ECU put in the check on null
					// 09/04/2018 ECU add the underlying object as an argument
					// -------------------------------------------------------------
					if (theCancelMethod != null)
						Utilities.invokeMethod ((Activity) theUnderlyingObject,theCancelMethod,new Object [] {id});
				} 
				catch (Exception theException) 
				{	
					theException.printStackTrace();
				} 
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		// 08/04/2015 ECU call the common method to set font sizes
		// -------------------------------------------------------------------------
		adjustFonts (builder);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void getDate (final Context 	theContext,
								final Object 	theUnderlyingObject,
						 			  String	theTitle,
						 			  String	theSubTitle,
						 			  String 	theConfirmLegend,
						 		final Method 	theConfirmMethod,
						 			  String    theCancelLegend,
						 		final Method    theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 27/02/2017 ECU created to use an AlertDialog to select and return a date
		// 22/05/2017 ECU added the 'negative' handling
		//            ECU changed 'theContext' to 'final'
		// ------------------------------------------------------------------------- 
	    LayoutInflater inflater = (LayoutInflater) theContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View layoutView = inflater.inflate(R.layout.get_date_layout, null, false);
	    // -------------------------------------------------------------------------
	    // 27/02/2017 ECU display the layout to get the date
	    //--------------------------------------------------------------------------
	    final DatePicker datePicker = (DatePicker) layoutView.findViewById (R.id.date_picker);
	    // -------------------------------------------------------------------------
	    // 27/02/2017 ECU set up the subtitle
	    // -------------------------------------------------------------------------
	    ((TextView) layoutView.findViewById (R.id.date_picker_subtitle)).setText (theSubTitle);
	    // -------------------------------------------------------------------------
	    // 27/02/2017 ECU now handle the alert dialogue
	    // -------------------------------------------------------------------------
	    AlertDialog.Builder builder 
			= new AlertDialog.Builder (new ContextThemeWrapper (theContext, DIALOGUE_THEME));
	    // -------------------------------------------------------------------------
	    builder.setTitle (theTitle).setView (layoutView);
	    // -------------------------------------------------------------------------
	    // 22/05/2017 ECU set the cancel button which calls the cancel method
	    // -------------------------------------------------------------------------
		builder.setNegativeButton(theCancelLegend, new DialogInterface.OnClickListener() 
		{
			public void onClick (DialogInterface dialog, int whichButton) 
			{
		   		// -----------------------------------------------------------------
	    		// 22/05/2017 ECU cancel the dialogue
	    		// -----------------------------------------------------------------
	    		dialog.cancel();
	    		// -----------------------------------------------------------------
	    		// 27/02/2017 ECU now call the method to pass through the information
	    		// ------------------------------------------------------------------
	    		try 
				{ 
					// -------------------------------------------------------------
					// 22/05/2017 ECU call the method and pass the data
	    			// 09/04/2018 ECU add the underlying object as an argument
					// -------------------------------------------------------------
	    			Utilities.invokeMethod ((Activity) theUnderlyingObject,theCancelMethod,new Object [] {null});
					// --------------------------------------------------------------
				} 
				catch (Exception theException) 
				{	
					
				} 
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		// 22/05/2017 ECU try and process the 'BACK' key 
		// -------------------------------------------------------------------------
		builder.setOnKeyListener (new DialogInterface.OnKeyListener() 
		{
			// ---------------------------------------------------------------------
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) 
            {
            	// -----------------------------------------------------------------
                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                	// -------------------------------------------------------------
                	// 22/05/2017 ECU cancel the dialogue and exit
                	// -------------------------------------------------------------
                	dialog.cancel();
                	// -------------------------------------------------------------
                	// 22/05/2017 ECU terminate the calling activity
                	// -------------------------------------------------------------
                	((Activity)theContext).finish ();
                	// -------------------------------------------------------------
                	return true;
                	// -------------------------------------------------------------
                }
                // -----------------------------------------------------------------
                return false;
            }
        });
	    // -------------------------------------------------------------------------
	    builder.setPositiveButton (theConfirmLegend, new DialogInterface.OnClickListener() 
	    {	    	
	    	public void onClick(DialogInterface dialog, int id) 
	    	{
	    		int month 	= datePicker.getMonth();
	    		int day 	= datePicker.getDayOfMonth();
	    		int year 	= datePicker.getYear();
	    		// -----------------------------------------------------------------
	    		// 27/02/2017 ECU cancel the dialogue
	    		// -----------------------------------------------------------------
	    		dialog.cancel();
	    		// -----------------------------------------------------------------
	    		// 27/02/2017 ECU now call the method to pass through the information
	    		// ------------------------------------------------------------------
	    		try 
				{ 
					// -------------------------------------------------------------
					// 27/02/2017 ECU call the method and pass the data
	    			// 09/04/2018 ECU add the underlying object as an argument
					// -------------------------------------------------------------
	    			Utilities.invokeMethod ((Activity) theUnderlyingObject,theConfirmMethod,new Object [] {(Object)(new int [] {day,month,year})});
					// --------------------------------------------------------------
				} 
				catch (Exception theException) 
				{	
				} 
				// -----------------------------------------------------------------

	    	}

	    }).show();
	    // ------------------------------------------------------------------------
	}
	// =============================================================================
	static void IPAddressInput (Context 		theContext,
			 					final Object 	theUnderlyingObject,
								String  		theTitle,
								String 			theSubTitle,
								String 			theDefaultAddress,
								final Method 	theConfirmMethod,
								final Method 	theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 12/11/2016 ECU created to input an IP address
		// -------------------------------------------------------------------------
		context = theContext;
		// -------------------------------------------------------------------------
		LayoutInflater layoutInflater = LayoutInflater.from (context);
		// -------------------------------------------------------------------------
		// 25/04/2015 ECU display the custom layout for the text field
		// 11/10/2016 ECU in layout changed to set text colour to 'black' from
		//                the default attribute
		// -------------------------------------------------------------------------
		View layoutView = layoutInflater.inflate (R.layout.ip_address_input,null);
		// -------------------------------------------------------------------------
		// 25/04/2015 ECU set up any attributes required of the text field
		// 26/04/2015 ECU added the 'default text'
		//            ECU added advice text view 
		// -------------------------------------------------------------------------
		final EditText textInput				= (EditText) layoutView.findViewById (R.id.ip_address_input);
		final TextView subTitleTextView			= (TextView) layoutView.findViewById (R.id.multiline_subtitle);
		// -------------------------------------------------------------------------
		subTitleTextView.setText (theSubTitle);
		// -------------------------------------------------------------------------
		// 12/07/2015 ECU set the default text if required
		// 03/10/2015 ECU if the default text starts with 'StaticData.HINT' then
		//                treat the remainder of the text as a hint - otherwise
		//                the text will be set as the default in the field
		// -------------------------------------------------------------------------
		if (theDefaultAddress != null)
		{
			textInput.setText (theDefaultAddress);
		}
		// -------------------------------------------------------------------------
		// 12/11/2016 ECU use a text water to validate the input data
		// -------------------------------------------------------------------------
		textInput.addTextChangedListener(new TextWatcher() 
		{
			private String previousText = StaticData.BLANK_STRING;  
			@Override
			public void afterTextChanged(Editable theData) 
			{
		        if (StaticData.PARTIAl_IP_ADDRESS.matcher(theData).matches()) 
		        {
		        	// -------------------------------------------------------------
		        	// 12/11/2016 ECU the input data matches the pattern so store
		        	//                this as the 'valid' data
		        	// -------------------------------------------------------------
		        	previousText = theData.toString();
		        	// -------------------------------------------------------------
		        } 
		        else 
		        {
		        	// -------------------------------------------------------------
		        	// 12/11/2016 ECU the entered data causes an invalid IP address
		        	//                so copy the last valid data back
		        	// -------------------------------------------------------------
		        	theData.replace(0,theData.length(),previousText);
		        	// -------------------------------------------------------------
		        }
			}
			// ---------------------------------------------------------------------
			@Override
			public void beforeTextChanged (CharSequence s, int start, int count,int after) 
			{
			}
			// ---------------------------------------------------------------------
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{	
			}
			// ---------------------------------------------------------------------	
        });
		// -------------------------------------------------------------------------
		// 16/03/2015 ECU create and build the dialogue 
		// -------------------------------------------------------------------------
		// 05/11/2016 ECU changed to use a theme rather than just '(theContext)'
		// -------------------------------------------------------------------------
		AlertDialog.Builder builder 
			= new AlertDialog.Builder (new ContextThemeWrapper (theContext, DIALOGUE_THEME));
		// -------------------------------------------------------------------------
		// 16/03/2015 ECU set the components of the dialogue
		// -------------------------------------------------------------------------
		builder.setTitle(theTitle).setView (layoutView)
		// -------------------------------------------------------------------------
		.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() 
		{
			public void onClick (DialogInterface dialog, int whichButton) 
			{
				// -----------------------------------------------------------------
				try 
				{ 
					// -------------------------------------------------------------
					// 16/03/2015 ECU call up the method that will handle the 
					//                input text
					// 24/03/2016 ECU put in the check on null
					// 09/04/2018 ECU add the underlying object as an argument
					// -------------------------------------------------------------
					if (theConfirmMethod != null)
						Utilities.invokeMethod ((Activity) theUnderlyingObject,theConfirmMethod,new Object [] {textInput.getText().toString()});
					// -------------------------------------------------------------
				} 
				catch (Exception theException) 
				{	
					theException.printStackTrace();
				} 
				// -----------------------------------------------------------------
				
			}
		})
		// -------------------------------------------------------------------------
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() 
		{
			public void onClick (DialogInterface dialog, int whichButton) 
			{
				// -----------------------------------------------------------------
				try 
				{ 
					// -------------------------------------------------------------
					// 10/04/2015 ECU call the method that will handle the cancellation
					// 24/03/2016 ECU put in the check on null
					// 09/04/2018 ECU add the underlying object as an argument
					// -------------------------------------------------------------
					if (theCancelMethod != null)
						Utilities.invokeMethod ((Activity) theUnderlyingObject,theCancelMethod,new Object [] {textInput.getText().toString()});
					// --------------------------------------------------------------
				} 
				catch (Exception theException) 
				{	
					theException.printStackTrace();
				} 
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		// 08/04/2015 ECU call the common method to set fonts
		// -------------------------------------------------------------------------
		adjustFonts (builder);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void listChoice (Context 		theContext,
			 				final Object 	theUnderlyingObject,
							String 			theTitle,
							String [] 		theItems,
							final Method 	theSelectMethod,
							String 			theCancelLegend,
							final Method 	theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 07/04/2015 ECU change for those dialogues which are recursive - see
		//                MusicPlayer
		//            ECU changed to use the master method but with no positive button
		// -------------------------------------------------------------------------
		listChoice (theContext,theUnderlyingObject,theTitle,theItems,theSelectMethod,null,null,theCancelLegend,theCancelMethod);
	}
	// =============================================================================
	static void listChoice (Context 		theContext,
			 				final Object 	theUnderlyingObject,
							String 			theTitle,
							String [] 		theItems,
							final Method 	theSelectMethod,
							String 			theConfirmLegend,
							final Method 	theConfirmMethod,
							String 			theCancelLegend,
							final Method 	theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 13/11/2017 ECU until 4.02.57 this used to be the master method until the
		//                option to dismiss the dialogue before the associated method
		//                is called
		// -------------------------------------------------------------------------
		listChoice (theContext,
					theUnderlyingObject,
					theTitle,
					theItems,
					theSelectMethod,
					theConfirmLegend,theConfirmMethod,
					theCancelLegend,theCancelMethod,
					false);
	}
	// =============================================================================
	static void listChoice (Context 		theContext,
			 				final Object 	theUnderlyingObject,
							String 			theTitle,
							String [] 		theItems,
							final Method 	theSelectMethod,
							String 			theConfirmLegend,
							final Method 	theConfirmMethod,
							String 			theCancelLegend,
							final Method 	theCancelMethod,
							final boolean 	theDismissDialogueFlag)
	{
		// -------------------------------------------------------------------------
		// 07/04/2015 ECU change for those dialogues which are recursive - see
		//                MusicPlayer
		// 13/11/2017 ECU as of 4.02.57 this is the new master method with the
		//                'dismiss dialogue' flag
		// -------------------------------------------------------------------------
		context = theContext;
		// -------------------------------------------------------------------------
		// 02/04/2015 ECU created and build the dialogue 
		// 05/11/2016 ECU changed to use a theme rather than just '(theContext)'
		// -------------------------------------------------------------------------
		AlertDialog.Builder builder 
			= new AlertDialog.Builder (new ContextThemeWrapper (theContext, DIALOGUE_THEME));
		// -------------------------------------------------------------------------
		// 01/03/2015 ECU added the friendlyName
		// -------------------------------------------------------------------------
		builder.setTitle (theTitle);
		// -------------------------------------------------------------------------
		// 27/02/2015 ECU changed the preset option from '-1' to that stored so
		// 				  that can accommodate editing
		// -------------------------------------------------------------------------
		builder.setItems (theItems,new DialogInterface.OnClickListener() 
		{
			// ---------------------------------------------------------------------
			public void onClick (DialogInterface dialog, int item) 
			{
				try 
				{
					// -------------------------------------------------------------
					// 13/11/2017 ECU before calling any method then optionally dismiss
					//                the current dialogue
					// -------------------------------------------------------------
					if (theDismissDialogueFlag)
						dialog.dismiss ();
					// -------------------------------------------------------------
					// 10/04/2015 ECU call the method that handles item selection
					// 24/03/2016 ECU put in the check on null
					// 09/04/2018 ECU add the underlying object as an argument
					// -------------------------------------------------------------
					if (theSelectMethod != null)
						Utilities.invokeMethod ((Activity) theUnderlyingObject,theSelectMethod,new Object [] {item});
					// -------------------------------------------------------------
				} 
				catch (Exception theException) 
				{	
					theException.printStackTrace ();
				} 
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		})
		// -------------------------------------------------------------------------
		.setNegativeButton (theCancelLegend, new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick (DialogInterface dialog, int id)
			{
				// -----------------------------------------------------------------
				try 
				{
					// -------------------------------------------------------------
					// 13/11/2017 ECU before calling any method then optionally dismiss 
					//				  the current dialogue
					// -------------------------------------------------------------
					if (theDismissDialogueFlag)
						dialog.dismiss ();
					// -------------------------------------------------------------
					// 10/04/2015 ECU call the method that handles the cancellation
					// 24/03/2016 ECU put in the check on null
					// 09/04/2018 ECU add the underlying object as an argument
					// -------------------------------------------------------------
					if (theCancelMethod != null)
						Utilities.invokeMethod ((Activity) theUnderlyingObject,theCancelMethod,new Object [] {id});
					// -------------------------------------------------------------
				} 
				catch (Exception theException) 
				{	
					theException.printStackTrace();
				} 
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		// 07/04/2015 ECU check if the positive button is to be included
		// -------------------------------------------------------------------------
		if (theConfirmLegend != null)
		{
			builder.setPositiveButton (theConfirmLegend, new DialogInterface.OnClickListener() 
			{
				@Override
				public void onClick (DialogInterface dialog, int id) 
				{
					// -------------------------------------------------------------
					// 13/11/2017 ECU before calling any method then optionally dismiss 
					//                the current dialogue
					// -------------------------------------------------------------
					if (theDismissDialogueFlag)
						dialog.dismiss ();
					// -----------------------------------------------------------------
					// 24/03/2016 ECU put in the check on null
					// 09/04/2018 ECU add the underlying object as an argument
					// ------------------------------------------------------------------
					try 
					{
						if (theConfirmMethod != null)
							Utilities.invokeMethod ((Activity) theUnderlyingObject,theConfirmMethod,new Object [] {selectedOption});
					} 
					catch (Exception theException) 
					{	
						theException.printStackTrace();
					} 
					// -----------------------------------------------------------------
				}
			});
		}
		// -------------------------------------------------------------------------
		// 08/04/2015 ECU call the common method to set font sizes
		// -------------------------------------------------------------------------
		adjustFonts (builder);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void multilineTextInput (		Context 	theContext,
									final   Object 		theUnderlyingObject,
											String  	theTitle,
											String 		theSubTitle,
											int 		theNumberOfLines,
											String 		theDefaultText,
									final 	Method 		theConfirmMethod,
									final 	Method 		theCancelMethod,
											int			theInputType,
											String  	theHelpText,
									final	boolean 	theMandatoryFlag)
	{
		// -------------------------------------------------------------------------
		// 10/04/2015 ECU remember the context for future use
		// 12/07/2015 ECU added theDefaultText
		// 22/01/2016 ECU added theInputFlag and theHelpFlag
		// 23/01/2016 ECU changed to use a layout so that the help actions can be
		//                presented in a better way
		//            ECU change to use theHelpText
		// 14/07/2017 ECU added the mandatory flag
		// -------------------------------------------------------------------------
		context = theContext;
		// -------------------------------------------------------------------------
	 	LayoutInflater layoutInflater = LayoutInflater.from (context);
		// -------------------------------------------------------------------------
		// 25/04/2015 ECU display the custom layout for the text field
	 	// 11/10/2016 ECU in layout changed to set text colour to 'black' from
	 	//                the default attribute
		// -------------------------------------------------------------------------
		View layoutView = layoutInflater.inflate (R.layout.multiline_input,null);
	    // -------------------------------------------------------------------------
	    // 25/04/2015 ECU set up any attributes required of the text field
		// 26/04/2015 ECU added the 'default text'
		//            ECU added advice text view 
	    // -------------------------------------------------------------------------
		final Button   helpButton				= (Button)   layoutView.findViewById (R.id.multiline_help_button);
		final EditText textInput				= (EditText) layoutView.findViewById (R.id.multiline_input);
	    final TextView subTitleTextView			= (TextView) layoutView.findViewById (R.id.multiline_subtitle);
		// -------------------------------------------------------------------------
		// 11/07/2015 ECU set the number of lines of the field
	    // 30/05/2016 ECU set the maximum number of lines
		// -------------------------------------------------------------------------
		textInput.setLines    (theNumberOfLines);
		textInput.setMaxLines (theNumberOfLines);
		// -------------------------------------------------------------------------
		// 30/05/2016 ECU check whether to remove the 'dummy' text view.
		//            ECU Note - not sure why the dummy view was added
		// -------------------------------------------------------------------------
		if (theNumberOfLines == 1)
		{
			((TextView) layoutView.findViewById (R.id.dummy_textview)).setVisibility (View.GONE);
		}
		// -------------------------------------------------------------------------
		// 14/02/2018 ECU set up the 'long click' listener
		// -------------------------------------------------------------------------
		//textInput.setOnLongClickListener (new View.OnLongClickListener() 
		//{
		//	@Override
		//	public boolean onLongClick (View view) 
		//	{	
		//		// -----------------------------------------------------------------
		//		// 14/02/2018 ECU process a long click on the
		//		//                input field
		//		// -----------------------------------------------------------------
		//		byte [] input = textInput.getText ().toString ().getBytes();
		//		// -----------------------------------------------------------------
		//		// 14/02/2018 ECU check if there is any data
		//		// -----------------------------------------------------------------
		//		if (input.length > 0)
		//		{
		//			// -------------------------------------------------------------
		//			// 14/02/2018 ECU get the current cursor position
		//			// -------------------------------------------------------------
		//			int cursorPointer = textInput.getSelectionStart();
		//			// -------------------------------------------------------------
		//			// 14/02/2018 ECU check for the start of the command
		//			// -------------------------------------------------------------
		//			int start;
		//			for (start = cursorPointer; start >=0; start--)
		//			{
		//				if (input [start] == ';')
		//					break;	
		//			}
		//			// -------------------------------------------------------------
		//			// 14/02/2018 ECU at this point 'start' either points to the ';'
		//			//                or is '-1' - in either case need to step on 1
		//			// -------------------------------------------------------------
		//			start++;
		//			// -------------------------------------------------------------
		//			// 14/02/2018 ECU now look for the end
		//			// -------------------------------------------------------------
		//			int end;
		//			for (end = cursorPointer; end < input.length; end++)
		//			{
		//				if (input [end] == ';')
		//					break;	
		//			}
		//			// -------------------------------------------------------------
		//			// 14/02/2018 ECU at this point 'end' is either on the terminating
		//			//                ';' or is 'input.length' - in either case need
		//			//                to step back 1
		//			// -------------------------------------------------------------
		//			//end--;
		//			// -------------------------------------------------------------
		//			// 14/02/2018 ECU display the command
		//			// -------------------------------------------------------------
		//			Utilities.popToast ("Command : '" + (new String (Arrays.copyOfRange (input,start,end))) + "'",true);
		//			// -------------------------------------------------------------
		//		}
		//		// -----------------------------------------------------------------
		//		// 14/02/2018 ECU indicate that this callback has consumed the long 
		//		//                click event
		//		// -----------------------------------------------------------------
		//		return true;
		//		// -----------------------------------------------------------------
		//	}
		//});
		// -------------------------------------------------------------------------
		subTitleTextView.setText (theSubTitle);
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU check if the input type is to be set
		// -------------------------------------------------------------------------
		if (theInputType != StaticData.NO_RESULT)
			textInput.setInputType (theInputType);
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU check whether there is help wanted
		// 23/01/2016 ECU changed to the text rather than a flag
		// -------------------------------------------------------------------------
		if (theHelpText != null)
		{
			// ---------------------------------------------------------------------
			// 26/04/2015 ECU and make the field visible
			// ---------------------------------------------------------------------
			helpButton.setVisibility (View.VISIBLE);
			// ---------------------------------------------------------------------
			// 23/01/2016 ECU change the text of the button
			// ---------------------------------------------------------------------
			helpButton.setText (theHelpText);
			// ---------------------------------------------------------------------
			// 26/04/2015 ECU add in the click listener
			// ---------------------------------------------------------------------
			// 22/01/2016 ECU remember this text view for later use
			// ---------------------------------------------------------------------
			helpTextInput = textInput;
			// ---------------------------------------------------------------------
			// 22/01/2016 ECU make the field 'long clickable
			// ---------------------------------------------------------------------
			helpButton.setClickable (true);
			helpButton.setOnClickListener (new View.OnClickListener() 
			{
				@Override
				public void onClick (View theView) 
				{
					// -------------------------------------------------------------
					try 
					{ 
						// ---------------------------------------------------------
						// 22/01/2016 ECU call up the method that will build the
						//                action command
						// ---------------------------------------------------------
						ActionCommandUtilities.SelectCommand (context,
								Utilities.createAMethod (DialogueUtilitiesNonStatic.class,"ActionCommand",StaticData.BLANK_STRING));
						// ---------------------------------------------------------
					} 
					catch (Exception theException) 
					{	
						theException.printStackTrace ();
					} 
					// -------------------------------------------------------------
				}
			});
		}
		// -------------------------------------------------------------------------
		// 12/07/2015 ECU set the default text if required
		// 03/10/2015 ECU if the default text starts with 'StaticData.HINT' then
		//                treat the remainder of the text as a hint - otherwise
		//                the text will be set as the default in the field
		// -------------------------------------------------------------------------
		if (theDefaultText != null)
		{
			if (theDefaultText.startsWith (StaticData.HINT))
			{
				textInput.setHint (theDefaultText.replace(StaticData.HINT,StaticData.BLANK_STRING));
			}
			else
			{
				textInput.setText (theDefaultText);
			}
		}
		// -------------------------------------------------------------------------
		// 16/03/2015 ECU create and build the dialogue 
		// -------------------------------------------------------------------------
		// 05/11/2016 ECU changed to use a theme rather than just '(theContext)'
		// -------------------------------------------------------------------------
		AlertDialog.Builder builder 
			= new AlertDialog.Builder (new ContextThemeWrapper (theContext, DIALOGUE_THEME));
		// -------------------------------------------------------------------------
		// 16/03/2015 ECU set the components of the dialogue
		// -------------------------------------------------------------------------
		builder.setTitle (theTitle).setView (layoutView)
		// -------------------------------------------------------------------------
		.setPositiveButton (R.string.confirm, new DialogInterface.OnClickListener() 
		{
			public void onClick (DialogInterface dialog, int whichButton) 
			{
				// -----------------------------------------------------------------
				// 14/07/2017 ECU check if this call requires 'mandatory' input. If set
				//                then do not action here because the dialogue will
				//                be dismissed either way so handle it lower down
				// -----------------------------------------------------------------
				if (!theMandatoryFlag)
				{
					// -------------------------------------------------------------
					try 
					{ 
						// ---------------------------------------------------------
						// 16/03/2015 ECU call up the method that will handle the 
						//                input text
						// 24/03/2016 ECU put in the check on null
						// 09/04/2018 ECU add the underlying object as an argument
						// ---------------------------------------------------------
						if (theConfirmMethod != null)
							Utilities.invokeMethod ((Activity) theUnderlyingObject,theConfirmMethod,new Object [] {textInput.getText().toString()});
						// ---------------------------------------------------------
					} 
					catch (Exception theException) 
					{	
						theException.printStackTrace();
					} 
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
		})
		// -------------------------------------------------------------------------
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() 
		{
			public void onClick (DialogInterface dialog, int whichButton) 
			{
				// -----------------------------------------------------------------
				// 14/07/2017 ECU check if this call requires 'mandatory' input. If set
				//                then do not action here because the dialogue will
				//                be dismissed either way so handle it lower down
				// -----------------------------------------------------------------
				if (!theMandatoryFlag)
				{
				// -----------------------------------------------------------------
					try 
					{ 
						// ---------------------------------------------------------
						// 10/04/2015 ECU call the method that will handle the cancellation
						// 24/03/2016 ECU put in the check on null
						// 09/04/2018 ECU add the underlying object as an argument
						// ---------------------------------------------------------
						if (theCancelMethod != null)
							Utilities.invokeMethod ((Activity) theUnderlyingObject,theCancelMethod,new Object [] {textInput.getText().toString()});
						// ---------------------------------------------------------
					} 
					catch (Exception theException) 
					{	
						theException.printStackTrace();
					} 
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		// 08/04/2015 ECU call the common method to set fonts
		// -------------------------------------------------------------------------
		adjustFonts (builder);
		// -------------------------------------------------------------------------
		// 14/07/2017 ECU check if the input has to be 'non empty' in which case
		//                do the validation and processing here because if do
		//                on the button's click listener then the dialogue will
		//                be dismissed no matter what the result is
		// -------------------------------------------------------------------------
		if (theMandatoryFlag)
		{
			// ---------------------------------------------------------------------
			// 14/07/2017 ECU if the mandatory flag is set then do not show the
			//                cancel / negative button and no need to do any processing
			// ---------------------------------------------------------------------
			negativeButton.setVisibility(View.GONE);
			// ---------------------------------------------------------------------
			// 14/07/2017 ECU handle the 'confirm / positive' button
			// ---------------------------------------------------------------------
			positiveButton.setOnClickListener (new View.OnClickListener()
		    {            
		          @Override
		          public void onClick (View theView)
		          {
		        	  if (!Utilities.emptyString (textInput.getText().toString()))
		        	  {
		        		  	// -----------------------------------------------------
		        		  	// 14/07/2017 ECU the field is empty tell the user
		        		  	// -----------------------------------------------------
		        		  	Utilities.popToast (context.getString (R.string.mandatory_input),true);
		        		  	// -----------------------------------------------------
		        	  }	
		        	  else
		        	  {
		        		  	try 
		  					{ 
		        		  		// -------------------------------------------------
		        		  		// 14/07/2017 ECU before processing any specified 
		        		  		//                method then dismiss the dialogue
		        		  		// -------------------------------------------------
		        		  		alertDialog.dismiss();
		        		  		// -------------------------------------------------
		        		  		// 16/03/2015 ECU call up the method that will handle the 
		        		  		//                input text
		        		  		// 24/03/2016 ECU put in the check on null
		        		  		// 09/04/2018 ECU add the underlying object as an argument
		        		  		// -------------------------------------------------
		        		  		if (theConfirmMethod != null)
		        		  			Utilities.invokeMethod ((Activity) theUnderlyingObject,theConfirmMethod,new Object [] {textInput.getText().toString()});
		        		  		// -------------------------------------------------
		  					} 
		        		  	catch (Exception theException) 
		        		  	{	
		        		  		theException.printStackTrace();
		        		  	} 
		        		  	// -----------------------------------------------------
		        	  }
		          }
		      });
		}
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	static void multilineTextInput (Context 	theContext,
			 						final Object theUnderlyingObject,
									String  	theTitle,
									String 		theSubTitle,
									int 		theNumberOfLines,
									String 		theDefaultText,
									final Method theConfirmMethod,
									final Method theCancelMethod,
									int			theInputType,
									String  	theHelpText)
	{
		// -------------------------------------------------------------------------
		// 14/07/2017 ECU this used to be the master method until the mandatory
		//                flag was added
		//            ECU call the new master method but with 'false' for the
		//                mandatory flag
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,
							theUnderlyingObject,
							theTitle,
							theSubTitle,
							theNumberOfLines,
							theDefaultText,
							theConfirmMethod,
							theCancelMethod,
							theInputType,
							theHelpText,
							false);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	static void multilineTextInput (Context theContext,
			 						final Object theUnderlyingObject,
									String  theTitle,
									String 	theSubTitle,
									int 	theNumberOfLines,
									String 	theDefaultText,
									final Method theConfirmMethod,
									final Method theCancelMethod,
									int		theInputType)		
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU created to make use of the new master method
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,
							theUnderlyingObject,
							theTitle,
							theSubTitle,
							theNumberOfLines,
							theDefaultText,
							theConfirmMethod,
							theCancelMethod,
							theInputType,
							null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	static void multilineTextInput (Context theContext,
									final Object theUnderlyingObject,
								    String  theTitle,
								    String 	theSubTitle,
								    int 	theNumberOfLines,
								    String 	theDefaultText,
								    final Method theConfirmMethod,
								    final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU created to call the new master method which has the input
		//                type
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,
							theUnderlyingObject,
							theTitle,
							theSubTitle,
							theNumberOfLines,
							theDefaultText,
							theConfirmMethod,
							theCancelMethod,
							StaticData.NO_RESULT,
							null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	static void multilineTextInput (Context theContext,
									final Object theUnderlyingObject,
									String  theTitle,
									String 	theSubTitle,
									int 	theNumberOfLines,
									final Method theConfirmMethod,
									final Method theCancelMethod,
									int 	theInputType)
	{
		// -------------------------------------------------------------------------
		// 12/07/2015 ECU created to call the generic method with no preset text
		// 22/01/2016 ECU added the input type as an argument
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,
							theUnderlyingObject,
							theTitle,
							theSubTitle,
							theNumberOfLines,
							null,
							theConfirmMethod,
							theCancelMethod,
							theInputType,
							null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	static void multilineTextInput (Context theContext,
			 						final Object theUnderlyingObject,
									String  theTitle,
									String 	theSubTitle,
									int 	theNunberOfLines,
									final Method theConfirmMethod,
									final Method theCancelMethod,
									int 	theInputType,
									String  theHelpText)
	{
		// -------------------------------------------------------------------------
		// 12/07/2015 ECU created to call the generic method with no preset text
		// 22/01/2016 ECU added the input type as an argument
		//            ECU and the click method
		//            ECU changed to the Help flag
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,
							theUnderlyingObject,
							theTitle,
							theSubTitle,
							theNunberOfLines,
							null,
							theConfirmMethod,
							theCancelMethod,
							theInputType,
							theHelpText);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	static void multilineTextInput (Context theContext,
									final Object theUnderlyingObject,
									String  theTitle,
									String 	theSubTitle,
									int 	theNunberOfLines,
									final Method theConfirmMethod,
									final Method theCancelMethod)
		{
			// ---------------------------------------------------------------------
			// 12/07/2015 ECU created to call the generic method with no preset text
			// 22/01/2016 ECU added the input type as an argument
			// ---------------------------------------------------------------------
			multilineTextInput (theContext,
								theUnderlyingObject,
								theTitle,
								theSubTitle,
								theNunberOfLines,
								null,
								theConfirmMethod,
								theCancelMethod,
								StaticData.NO_RESULT,
								null);
			// ---------------------------------------------------------------------
		}
	// =============================================================================
	static void multipleChoice (Context 	theContext,
			 					final Object theUnderlyingObject,
				                String 		theTitle,
				                String []	theOptions,
				                final boolean [] theInitialOptions,
				                final Method theConfirmMethod,
				                final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 17/03/2015 ECU created to handle multiple choice selection
		// -------------------------------------------------------------------------
		// 10/04/2015 ECU remember the context for future use
		// -------------------------------------------------------------------------
		context = theContext;
		// -------------------------------------------------------------------------
		// 05/11/2016 ECU changed to use a theme rather than just '(theContext)'
		// -------------------------------------------------------------------------
		AlertDialog.Builder builder 
			= new AlertDialog.Builder (new ContextThemeWrapper (theContext, DIALOGUE_THEME));
		// -------------------------------------------------------------------------
		// 17/03/2015 ECU build up the dialogue
		//            ECU do NOT use 'setMessage' because this seems to overwrite
		//                the options that are being displayed for 'selection'
		// -------------------------------------------------------------------------
		builder.setTitle (theTitle)
		// -------------------------------------------------------------------------
		.setMultiChoiceItems (theOptions,theInitialOptions,new DialogInterface.OnMultiChoiceClickListener() 
		{
			// ---------------------------------------------------------------------
			@Override
			public void onClick (DialogInterface dialog,int which,boolean isChecked) 
			{
				// -----------------------------------------------------------------
				theInitialOptions [which] = isChecked;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		})
		// -------------------------------------------------------------------------
		.setPositiveButton (R.string.confirm, new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int id) 
			{
				// -----------------------------------------------------------------
				try 
				{
					// -------------------------------------------------------------
					// 10/04/2015 ECU call the method that handles the confirmation
					// 24/03/2016 ECU put in the check on null
					// 09/04/2018 ECU add the underlying object as an argument
					// -------------------------------------------------------------
					if (theConfirmMethod != null)
						Utilities.invokeMethod ((Activity) theUnderlyingObject,theConfirmMethod,new Object [] {theInitialOptions});
					// -------------------------------------------------------------
				} 
				catch (Exception theException) 
				{	
					theException.printStackTrace();
				} 
				// -----------------------------------------------------------------
			}
		})
		// -------------------------------------------------------------------------
		.setNegativeButton (R.string.cancel, new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				// -----------------------------------------------------------------
				try 
				{
					// -------------------------------------------------------------
					// 10/04/2015 ECU call the method that handles the cancellation
					// 24/03/2016 ECU put in the check on null
					// 09/04/2018 ECU add the underlying object as an argument
					// -------------------------------------------------------------
					if (theCancelMethod != null)
						Utilities.invokeMethod ((Activity) theUnderlyingObject,theCancelMethod,new Object [] {theInitialOptions});
				} 
				catch (Exception theException) 
				{	
					theException.printStackTrace();
				} 
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		// 08/04/2015 ECU call the common method to set font sizes
		// -------------------------------------------------------------------------
		adjustFonts (builder);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void prompt (Context theContext,
			 			final Object theUnderlyingObject,
						String  theTitle,
						String  theSummary,
						String  thePrompt,
						String  theAcknowledgeLegend)
	{
		// -------------------------------------------------------------------------
		// 20/12/2015 ECU created to provide a simple prompt dialogue with no
		//                user input - just an acknowledge button
		// -------------------------------------------------------------------------
		context = theContext;
		// -------------------------------------------------------------------------
		// 05/11/2016 ECU changed to use a theme rather than just '(theContext)'
		// -------------------------------------------------------------------------
		AlertDialog.Builder builder 
			= new AlertDialog.Builder (new ContextThemeWrapper (theContext, DIALOGUE_THEME));
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU get the required layout
		// -------------------------------------------------------------------------
		final LayoutInflater inflater 	= (LayoutInflater) theContext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
		final View builderLayout 		= inflater.inflate (R.layout.prompt_layout,null);
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU set up the various fields
		// -------------------------------------------------------------------------
		final TextView promptTextView = (TextView) builderLayout.findViewById (R.id.promptTextView);
		// -------------------------------------------------------------------------
		// 20/12/2015 ECU now start handling the arguments
		// -------------------------------------------------------------------------
		builder.setTitle (theTitle);
		// -------------------------------------------------------------------------
		// 08/05/2016 ECU check whether to set the message or the textview
		//            ECU if the summary is null then the message to display is in
		//                'thePrompt' and the layout will be used. If non-null then
		//                 it will be displayed as the builder's message
		// -------------------------------------------------------------------------
		if (theSummary == null)
			promptTextView.setText (thePrompt);
		else
			builder.setMessage (theSummary);
		// -------------------------------------------------------------------------
		builder.setView  (builderLayout);
		// -------------------------------------------------------------------------
		builder.setPositiveButton (theAcknowledgeLegend,new DialogInterface.OnClickListener() 
		{
			@Override
			// ---------------------------------------------------------------------
			public void onClick (DialogInterface dialog,int which) 
			{
				// -----------------------------------------------------------------
				// 20/12/2015 ECU just dismiss the dialogue
				// -----------------------------------------------------------------
				dialog.dismiss();
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		});
		// -------------------------------------------------------------------------
		// 20/12/2015 ECU adjust font 
		// -------------------------------------------------------------------------
		adjustFonts (builder);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	static void prompt (Context theContext,
			 			Object 	theUnderlyingObject,
						String  theTitle,
						String  thePrompt,
						String  theAcknowledgeLegend)
	{
		// -------------------------------------------------------------------------
		// 08/05/2016 ECU created to use the new method where the layout is specified
		// 06/07/2018 ECU pass through the underlying object
		// -------------------------------------------------------------------------
		prompt (theContext,theUnderlyingObject,theTitle,null,thePrompt,theAcknowledgeLegend);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void searchChoice (Context 	theContext,
			 				  final Object 	theUnderlyingObject,
							  String 	theTitle,
							  String 	theConfirmLegend,
							  final Method theConfirmMethod,
							  String 	theCancelLegend,
							  final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 10/04/2015 ECU created to have a search string and options dialogue
		//            ECU remember the context for later use if recursive
		// -------------------------------------------------------------------------
		context = theContext;
		// -------------------------------------------------------------------------
		// 10/04/2015 ECU declare the object that will contain the search parameters
		// -------------------------------------------------------------------------
		final SearchParameters searchParameters = new SearchParameters (MusicPlayer.SEARCH_PARAMETER_NUMBER);
		// -------------------------------------------------------------------------
		// 05/11/2016 ECU changed to use a theme rather than just '(theContext)'
		// -------------------------------------------------------------------------
		AlertDialog.Builder builder 
			= new AlertDialog.Builder (new ContextThemeWrapper (theContext, DIALOGUE_THEME));
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU get the required layout
		// -------------------------------------------------------------------------
		final LayoutInflater inflater 	= (LayoutInflater) theContext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
		final View builderLayout 		= inflater.inflate (R.layout.album_search_layout,null);
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU set up the various fields
		// -------------------------------------------------------------------------
		final CheckBox albumCheckBox 	= (CheckBox) builderLayout.findViewById (R.id.album_checkBox);
		final CheckBox artistCheckBox 	= (CheckBox) builderLayout.findViewById (R.id.artist_checkBox);
		final CheckBox composerCheckBox = (CheckBox) builderLayout.findViewById (R.id.composer_checkBox);
		final CheckBox titleCheckBox	= (CheckBox) builderLayout.findViewById (R.id.title_checkBox);
		final EditText searchText		= (EditText) builderLayout.findViewById (R.id.album_search_testview);
		// -------------------------------------------------------------------------
		// 10/04/2015 ECU set up a listener on the search field
		// -------------------------------------------------------------------------
		searchText.addTextChangedListener (new TextWatcher ()
		{
			// ---------------------------------------------------------------------
			@Override
			public void afterTextChanged (Editable sequence) 
			{	
			}
			// ---------------------------------------------------------------------
			@Override
			public void beforeTextChanged (CharSequence sequence, int start,int count,int after) 
			{
			}
			// ---------------------------------------------------------------------
			@Override
			public void onTextChanged(CharSequence sequence,int start,int before,int count) 
			{
				// -----------------------------------------------------------------
				// 10/04/2015 ECU enable or disable the positive button depending
				//                on whether the field has any characters
				// -----------------------------------------------------------------
				positiveButton.setEnabled (!(sequence.length() == 0));
			}
			// ---------------------------------------------------------------------	
		});
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU set the title and display the seekbar
		// -------------------------------------------------------------------------
		builder.setTitle (theTitle);
		builder.setView  (builderLayout);
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU set up the confirmation buttons
		// -------------------------------------------------------------------------
		builder.setPositiveButton (theConfirmLegend,new DialogInterface.OnClickListener() 
		{
			@Override
			// ---------------------------------------------------------------------
			public void onClick (DialogInterface dialog,int which) 
			{
				// -----------------------------------------------------------------
				// 03/04/2015 ECU returned the search details
				// -----------------------------------------------------------------
				try 
				{
					// -------------------------------------------------------------
					searchParameters.searchString = searchText.getText().toString();
					// -------------------------------------------------------------
					// 10/04/2015 ECU no need to check if search is empty because
					//                the button would not have been enabled
					// -------------------------------------------------------------
					searchParameters.searchOptions [MusicPlayer.SEARCH_PARAMETER_ALBUM] 	= albumCheckBox.isChecked();
					searchParameters.searchOptions [MusicPlayer.SEARCH_PARAMETER_ARTIST] 	= artistCheckBox.isChecked();
					searchParameters.searchOptions [MusicPlayer.SEARCH_PARAMETER_COMPOSER] 	= composerCheckBox.isChecked();
					searchParameters.searchOptions [MusicPlayer.SEARCH_PARAMETER_TITLE] 	= titleCheckBox.isChecked();
					// -------------------------------------------------------------
					// 10/04/2015 ECU call the method that will do the actual search
					// 24/03/2016 ECU put in the check on null
					// 09/04/2018 ECU add the underlying object as an argument
					// -------------------------------------------------------------
					if (theConfirmMethod != null)
						Utilities.invokeMethod ((Activity) theUnderlyingObject,theConfirmMethod,new Object [] {(Object) searchParameters});	
					// -----------------------------------------------------------------
					// 03/04/2015 ECU remove the dialogue
					// -----------------------------------------------------------------
					dialog.dismiss();
					// -----------------------------------------------------------------
				} 
				catch (Exception theException) 
				{	
					theException.printStackTrace();
				} 
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		})
		// -------------------------------------------------------------------------
		.setNegativeButton (theCancelLegend, new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick (DialogInterface dialog, int id)
			{
				// -----------------------------------------------------------------
				try 
				{
					// -------------------------------------------------------------
					// 10/04/2015 ECU call up the method that will handle the cancellation
					// 24/03/2016 ECU put in the check on null
					// 09/04/2018 ECU add the underlying object as an argument
					// -------------------------------------------------------------
					if (theCancelMethod != null)
						Utilities.invokeMethod ((Activity) theUnderlyingObject,theCancelMethod,new Object [] {(Object)searchParameters});
					// -------------------------------------------------------------
					// 10/04/2015 ECU dismiss the dialogue
					// -------------------------------------------------------------
					dialog.dismiss();
					// -------------------------------------------------------------
				} 
				catch (Exception theException) 
				{	
					theException.printStackTrace();
				} 
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		// 08/04/2015 ECU call the common method to set font sizes
		// 10/04/2015 ECU call the new method with the positive button initially
		//				  disabled
		// -------------------------------------------------------------------------
		adjustFonts (builder,false,true);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void securityInput (Context 	theContext,
			 				   final Object theUnderlyingObject,
							   String	theTitle,
							   String 	theExistingCode,
							   String 	theConfirmLegend,
							   final Method theConfirmMethod,
							   String theCancelLegend,
							   final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 06/12/2015 ECU created to input the security code for the Panic Alarm
		// -------------------------------------------------------------------------
		context = theContext;
		// -------------------------------------------------------------------------
		// 05/11/2016 ECU changed to use a theme rather than just '(theContext)'
		// -------------------------------------------------------------------------
		AlertDialog.Builder builder 
			= new AlertDialog.Builder (new ContextThemeWrapper (theContext, DIALOGUE_THEME));
		// -------------------------------------------------------------------------
		// 06/12/2015 ECU get the required layout
		// -------------------------------------------------------------------------
		final LayoutInflater inflater 	= (LayoutInflater) theContext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
		final View builderLayout 		= inflater.inflate (R.layout.activity_panic_alarm_security,null);
		// -------------------------------------------------------------------------
		// 06/12/2015 ECU set up the field to display the input security code
		// -------------------------------------------------------------------------
		final TextView securityString		= (TextView) builderLayout.findViewById(R.id.security_string);
		// -------------------------------------------------------------------------
		// 06/12/2015 ECU set the existing code as a 'hint'
		// -------------------------------------------------------------------------
		if (theExistingCode != null)
			securityString.setHint (theExistingCode);
		// -------------------------------------------------------------------------
		// 06/12/2015 ECU declare the listeners for the various image buttons
		//                pressing the image will cause the appropriate character
		//                to be stored in the string
		// -------------------------------------------------------------------------
		((ImageButton) builderLayout.findViewById (R.id.panic_alarm_clubs)).setOnClickListener (new OnClickListener() 
		{
			@Override
			public void onClick(View view) 
			{
				// -----------------------------------------------------------------
				// 06/12/2015 ECU add the clubs character
				// -----------------------------------------------------------------
				securityString.append(StaticData.PANIC_ALARM_CLUBS);
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		((ImageButton) builderLayout.findViewById (R.id.panic_alarm_diamonds)).setOnClickListener (new OnClickListener() 
		{
			@Override
			public void onClick(View view) 
			{
				// -----------------------------------------------------------------
				// 06/12/2015 ECU add the diamonds character
				// -----------------------------------------------------------------
				securityString.append(StaticData.PANIC_ALARM_DIAMONDS);
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		((ImageButton) builderLayout.findViewById (R.id.panic_alarm_hearts)).setOnClickListener (new OnClickListener() 
		{
			@Override
			public void onClick(View view) 
			{
				// -----------------------------------------------------------------
				// 06/12/2015 ECU add the hearts character
				// -----------------------------------------------------------------
				securityString.append(StaticData.PANIC_ALARM_HEARTS);
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		((ImageButton) builderLayout.findViewById (R.id.panic_alarm_spades)).setOnClickListener (new OnClickListener() 
		{
			@Override
			public void onClick(View view) 
			{
				// -----------------------------------------------------------------
				// 06/12/2015 ECU add the spades character
				// -----------------------------------------------------------------
				securityString.append(StaticData.PANIC_ALARM_SPADES);
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		// 06/12/2015 ECU set the title and display the layout
		// -------------------------------------------------------------------------
		builder.setTitle (theTitle);
		builder.setView  (builderLayout);
		// -------------------------------------------------------------------------
		// 06/12/2015 ECU set up the confirmation buttons
		// -------------------------------------------------------------------------
		builder.setPositiveButton (theConfirmLegend,new DialogInterface.OnClickListener() 
		{
			@Override
			// ---------------------------------------------------------------------
			public void onClick (DialogInterface dialog,int which) 
			{
				// -----------------------------------------------------------------
				// 06/12/2015 ECU returned the input security string
				// -----------------------------------------------------------------
				try 
				{
					// -------------------------------------------------------------
					// 10/04/2015 ECU call the method that will do the actual search
					// 24/03/2016 ECU put in the check on null
					// 09/04/2018 ECU add the underlying object as an argument
					// -------------------------------------------------------------
					if (theConfirmMethod != null)
						Utilities.invokeMethod ((Activity) theUnderlyingObject,theConfirmMethod,new Object [] {securityString.getText().toString()});
					// -------------------------------------------------------------
					// 06/12/2015 ECU remove the dialogue
					// -------------------------------------------------------------
					dialog.dismiss();
					// -------------------------------------------------------------
				} 
				catch (Exception theException) 
				{	
					theException.printStackTrace();
				} 
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		})
		// -------------------------------------------------------------------------
		.setNegativeButton (theCancelLegend, new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick (DialogInterface dialog, int id)
			{
				// -----------------------------------------------------------------
				try 
				{
					// -------------------------------------------------------------
					// 06/12/2015 ECU call up the method that will handle the cancellation
					//                and give it null as the security input string
					// 24/03/2016 ECU put in the check on null
					// 09/04/2018 ECU add the underlying object as an argument
					// -------------------------------------------------------------
					if (theCancelMethod != null)
						Utilities.invokeMethod ((Activity) theUnderlyingObject,theCancelMethod,new Object [] {null});
					// -------------------------------------------------------------
					// 10/04/2015 ECU dismiss the dialogue
					// -------------------------------------------------------------
					dialog.dismiss();
					// --------------------------------------------------------------
				} 
				catch (Exception theException) 
				{	
					theException.printStackTrace();
				} 
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		// 06/12/2015 ECU create the dialogue and then show it
		// -------------------------------------------------------------------------
		adjustFonts (builder);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void singleChoice (Context 	theContext,
			 				  final Object theUnderlyingObject,
							  String 	theTitle,
							  String [] theOptions,
							  int theInitialOption,
							  String theConfirmLegend,
							  final Method theConfirmMethod,
							  String theCancelLegend,
							  final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 15/03/2015 ECU create and build the dialogue 
		// 07/05/2016 ECU put in the check on the length of theOptions
		// 15/06/2016 ECU changed to add the button legends
		// -------------------------------------------------------------------------
		if (theOptions != null && theOptions.length > 0)
		{
			selectedOption	= theInitialOption;
			// ---------------------------------------------------------------------
			// 10/04/2015 ECU created to have a search string and options dialogue
			//            ECU remember the context for later use if recursive
			// ---------------------------------------------------------------------
			context = theContext;
			// ---------------------------------------------------------------------
			// 05/11/2016 ECU changed to use a theme rather than just '(theContext)'
			// -------------------------------------------------------------------------
			AlertDialog.Builder builder 
				= new AlertDialog.Builder (new ContextThemeWrapper (theContext, DIALOGUE_THEME));
			// ---------------------------------------------------------------------
			// 01/03/2015 ECU added the friendlyName
			// ---------------------------------------------------------------------
			builder.setTitle (theTitle);
			// ---------------------------------------------------------------------
			// 27/02/2015 ECU changed the preset option from '-1' to that stored so
			// 				  that can accommodate editing
			// ---------------------------------------------------------------------
			builder.setSingleChoiceItems (theOptions,theInitialOption, new DialogInterface.OnClickListener() 
			{
				// -----------------------------------------------------------------
				public void onClick(DialogInterface dialog, int item) 
				{
					selectedOption = item;
				}
				// -----------------------------------------------------------------
			})
			// ---------------------------------------------------------------------
			.setPositiveButton (((theConfirmLegend == null ) ? theContext.getString (R.string.confirm) 
														     : theConfirmLegend), new DialogInterface.OnClickListener() 
			{
				@Override
				public void onClick (DialogInterface dialog, int id) 
				{
					// -------------------------------------------------------------
					try 
					{
						// ---------------------------------------------------------
						// 15/03/2015 ECU call the method that will handle the selection
						// 24/03/2016 ECU put in the check on null
						// 09/04/2018 ECU add the underlying object as an argument
						// ---------------------------------------------------------
						if (theConfirmMethod != null)
							Utilities.invokeMethod ((Activity) theUnderlyingObject,theConfirmMethod,new Object [] {selectedOption});
						// ---------------------------------------------------------
					} 
					catch (Exception theException) 
					{	
						theException.printStackTrace();
					} 
					// -------------------------------------------------------------
				}
			})
			// ---------------------------------------------------------------------
			.setNegativeButton (((theCancelLegend == null ) ? theContext.getString (R.string.cancel) 
					                                        : theCancelLegend), new DialogInterface.OnClickListener() 
			{
				@Override
				public void onClick (DialogInterface dialog, int id)
				{
					// -------------------------------------------------------------
					try 
					{
						// ---------------------------------------------------------
						// 15/03/2015 ECU call up the method that handles the cancellation
						// 24/03/2016 ECU put in the check on null
						// 09/04/2018 ECU add the underlying object as an argument
						// ---------------------------------------------------------
						if (theCancelMethod != null)
							Utilities.invokeMethod ((Activity) theUnderlyingObject,theCancelMethod,new Object [] {selectedOption});
						// ---------------------------------------------------------
					} 
					catch (Exception theException) 
					{	
						theException.printStackTrace();
					} 
					// -------------------------------------------------------------
				}
			});
			// ---------------------------------------------------------------------
			// 08/04/2015 ECU call the common method to set font sizes
			// ---------------------------------------------------------------------
			adjustFonts (builder);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 07/05/2016 ECU there are no items to select from - inform the user
			//                then exit
			// 08/05/2016 ECU changed to use 'prompt' instead of popToast
			//            ECU the summary will be used to display the message - the
			//                'null' implies that there is no prompt
			// 06/07/2018 ECU pass through the underlying object
			// ---------------------------------------------------------------------
			prompt (theContext,
					theUnderlyingObject,
					theTitle,
					theContext.getString (R.string.no_items_to_select_from),
					null,
					theContext.getString (R.string.legend_ok));
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	static void singleChoice (Context 	theContext,
			 				  final Object 	theUnderlyingObject,
			  				  String 	theTitle,
			  				  String [] theOptions,
			  				  int 		theInitialOption,
			  				  final Method theConfirmMethod,
			  				  final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 15/06/2016 ECU created to call the main method with no changes to the
		//                legends on the button
		// -------------------------------------------------------------------------
		singleChoice (theContext,
				      theUnderlyingObject,
				      theTitle,
				      theOptions,
				      theInitialOption,
				      null,theConfirmMethod,
				      null,theCancelMethod);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void sliderChoice (final Context theContext,
			 				  final Object 	theUnderlyingObject,
							  String theTitle,
							  final String theSubTitle,
							  int theIconId,
							  final MediaPlayer theMediaPlayer,
							  int theInitialValue,
							  final int theMinimumValue,
							  final int theMaximumValue,
							  String theConfirmLegend,
							  final Method theConfirmMethod,
							  String theCancelLegend,
							  final Method theCancelMethod,
							  boolean theCancellableFlag)
	{
		// -------------------------------------------------------------------------
		// 10/04/2015 ECU created to have a search string and options dialogue
		//            ECU remember the context for later use if recursive
		// 09/06/2017 ECU made changes because there was an inconsistency with the
		//                use of 'seekBarValue' - made changes so that it is now
		//                always the value used by, and returned by, the 'seekBar'
		//                widget. Before displaying or returning the value to the
		//                caller then 'theMinimumValue' must be added.
		// 08/08/2019 ECU provide a facility to set an exact value
		// 10/08/2019 ECU added the EditText fields and associated listeners
		//            ECU added the 'cancellable' flag
		// -------------------------------------------------------------------------
		// 19/05/2017 ECU declare any local variables
		// -------------------------------------------------------------------------
		final ImageButton minusButton;
		final ImageButton plusButton;
		// -------------------------------------------------------------------------
		// 15/08/2015 ECU added theIconId
		// 06/03/2016 ECU added theMinimumValue
		// -------------------------------------------------------------------------
		context = theContext;
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU set the initial value
		// 06/03/2016 ECU adjust by the minimum value
		// -------------------------------------------------------------------------
		seekBarValue = theInitialValue - theMinimumValue;
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU created to have a slider dialogue
		// -------------------------------------------------------------------------
		// 05/11/2016 ECU changed to use a theme rather than just '(theContext)'
		// -------------------------------------------------------------------------
		seekBarBuilder 
			= new AlertDialog.Builder (new ContextThemeWrapper (theContext, DIALOGUE_THEME));
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU get the required layout
		// -------------------------------------------------------------------------
		final LayoutInflater inflater 	= (LayoutInflater) theContext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
		final View seekBarLayout 		= inflater.inflate (R.layout.dialogue_slider_layout,null);
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU set up the various fields
		// 12/02/2016 ECU add the value text field
		// -------------------------------------------------------------------------
		sliderTextView 					= (TextView) seekBarLayout.findViewById (R.id.slider_textview);
		sliderValueTextView				= (TextView) seekBarLayout.findViewById (R.id.slider_value_textview);
		final SeekBar seekBar 			= (SeekBar)  seekBarLayout.findViewById (R.id.slider_seekbar);
		// -------------------------------------------------------------------------
		// 09/08/2019 ECU set up the field to enter a value
		// -------------------------------------------------------------------------
		final Button sliderValueButton				= (Button) seekBarLayout.findViewById (R.id.slider_button);
		final EditText sliderValueEditText			= (EditText) seekBarLayout.findViewById (R.id.slider_seek_value);
		// -------------------------------------------------------------------------
		// 19/05/2017 ECU set up the plus and minus buttons
		// -------------------------------------------------------------------------
		minusButton = (ImageButton) seekBarLayout.findViewById (R.id.slider_minus_button);
		plusButton  = (ImageButton) seekBarLayout.findViewById (R.id.slider_plus_button);
		// -------------------------------------------------------------------------
		// 19/05/2017 ECU set up click listeners for the plus/minus buttons
		// -------------------------------------------------------------------------
		minusButton.setOnClickListener (new OnClickListener() 
		{
			@Override
			public void onClick (View view) 
			{
				// -----------------------------------------------------------------
				// 19/05/2017 ECU changed to handle click of the 'minus' button
				// 09/06/2017 ECU removed '- theMinimumValue'
				// -----------------------------------------------------------------
				seekBarValue--;
				seekBar.setProgress (seekBarValue);
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		plusButton.setOnClickListener (new OnClickListener() 
		{
			@Override
			public void onClick (View view) 
			{
				// -----------------------------------------------------------------
				// 19/05/2017 ECU changed to handle click of the 'plus' button
				// 09/06/2017 ECU removed '- theMinimumValue'
				// -----------------------------------------------------------------
				seekBarValue++;
				seekBar.setProgress (seekBarValue);
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		sliderValueButton.setOnClickListener (new OnClickListener() 
		{
			@Override
			public void onClick (View view) 
			{
				// -----------------------------------------------------------------
				// 09/08/2019 ECU set up the string that will be used as a hint
				// -----------------------------------------------------------------
				String localHint = String.format (context.getString (R.string.seek_value_enter_format),theMinimumValue,theMaximumValue);
				// -----------------------------------------------------------------
				// 09/08/2019 ECU check if the edit text field is visible
				// -----------------------------------------------------------------
				if (sliderValueEditText.getVisibility() != View.VISIBLE)
				{
					// -------------------------------------------------------------
					// 09/08/2019 ECU make the entry field visible
					// -------------------------------------------------------------
					sliderValueEditText.setVisibility (View.VISIBLE);
					sliderValueEditText.setText (StaticData.BLANK_STRING);
					// -------------------------------------------------------------
					sliderValueEditText.setHint (localHint);
					// -------------------------------------------------------------
					sliderValueEditText.requestFocus ();
					// -------------------------------------------------------------
					// 09/08/2019 ECU display the soft keyboard
					// -------------------------------------------------------------
					Utilities.softKeyboard (context,view,true);
					// -------------------------------------------------------------
				}
				else
				{
					String inputString = sliderValueEditText.getText().toString();
					// -------------------------------------------------------------
					if (!Utilities.isStringBlank (inputString))
					{
						int inputNumber = Integer.valueOf (inputString);
						// --------------------------------------------------------
						if ((inputNumber >= theMinimumValue) && (inputNumber <= theMaximumValue))
						{
							// ----------------------------------------------------
							// 09/08/2019 ECU remove the entry field
							// ----------------------------------------------------	
							sliderValueEditText.setVisibility (View.GONE);
							// ----------------------------------------------------
							// 09/08/2019 ECU get the entered value
							// ----------------------------------------------------
							seekBarValue = inputNumber - theMinimumValue;
							seekBar.setProgress (seekBarValue);
							// ----------------------------------------------------
							// 09/08/2019 ECU remove the soft keyboard
							// ----------------------------------------------------
							Utilities.softKeyboard (context,view,false);
							// ----------------------------------------------------
						}
						else
						{
							// ----------------------------------------------------
							// 09/08/2019 ECU an invalid number has been entered
							// ----------------------------------------------------
							Utilities.popToastAndSpeak (localHint);
							//-----------------------------------------------------
						}
					}
				}
			}
		});
		// -------------------------------------------------------------------------
		// 10/08/2019 ECU add the listener for key strokes
		// -------------------------------------------------------------------------
		sliderValueEditText.setOnKeyListener (new View.OnKeyListener ()
		{
			@Override
			public boolean onKey (View theView,int theKeyCode,KeyEvent theEvent)
			{
				// -----------------------------------------------------------------
				// 10/08/2019 ECU only interested in key down
				// -----------------------------------------------------------------
				if (theEvent.getAction() == KeyEvent.ACTION_DOWN)
				{
					// -------------------------------------------------------------
					// 10/08/2019 ECU now check for the 'enter'/'done' key
					// -------------------------------------------------------------
					if (theKeyCode == KeyEvent.KEYCODE_ENTER)
					{
						// ---------------------------------------------------------
						// 10/08/2019 ECU the 'enter'/'done' key has been pressed so
						//                take the required action
						// ---------------------------------------------------------
						sliderValueButton.performClick ();
						// ---------------------------------------------------------
					}
				}
				// -----------------------------------------------------------------
				// 10/08/2019 ECU allow the 'super' to handle
				// -----------------------------------------------------------------
				return false;
				// -----------------------------------------------------------------
			}
		});
		// ------------------------------------------------------------------------- 
		// 15/08/2015 ECU sort out the icon that will be displayed
		// -------------------------------------------------------------------------
		if (theIconId != StaticData.NO_RESULT)
		{
			// ---------------------------------------------------------------------
			// 15/08/2015 ECU the caller has specified the id of an icon that should
			//                be displayed
			// ---------------------------------------------------------------------
			ImageView seekBarIcon	= (ImageView) seekBarLayout.findViewById (R.id.slider_icon);
			// ---------------------------------------------------------------------
			// 15/08/2015 ECU change the icon if a new one has been specified
			// ---------------------------------------------------------------------
			seekBarIcon.setImageDrawable (context.getResources ().getDrawable (theIconId));
		}
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU initialise the text view
		// -------------------------------------------------------------------------
		sliderTextView.setText (StaticData.BLANK_STRING);
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU declare the range of the seekbar
		// 20/05/2017 ECU there was an error - needed to delete the minimum value
		// -------------------------------------------------------------------------
		seekBar.setMax (theMaximumValue - theMinimumValue);
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU set up the initial value and display the value on the screen
		// 26/07/2015 ECU changed to use theSubTitle
		// 12/02/2016 ECU display the value in the appropriate field and remove from
		//                the subtitle field
		// 06/03/2015 ECU adjust based on theMinimumValue
		// 30/05/2016 ECU changed to use resource
		// 20/05/2017 ECU changed to set progress using seek....
		//            ECU moved the setProgress to the end of this method
		// -------------------------------------------------------------------------
		sliderTextView.setText (theSubTitle);
		sliderValueTextView.setText (theContext.getString (R.string.initial_value) + theInitialValue);
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU clear the dummy text view field
		// 18/05/2017 ECU changed to use BLANK_STRING
		// -------------------------------------------------------------------------
		((TextView) seekBarLayout.findViewById (R.id.slider_dummy_textview)).setText (StaticData.BLANK_STRING);
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU set the title and display the seekbar
		// -------------------------------------------------------------------------
		seekBarBuilder.setTitle (theTitle);
		seekBarBuilder.setView  (seekBarLayout);
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU set up a listener for changes to the slider
		// -------------------------------------------------------------------------
		seekBar.setOnSeekBarChangeListener (new SeekBar.OnSeekBarChangeListener () 
		{
			// ---------------------------------------------------------------------
			public void onProgressChanged (SeekBar theSeekBar,int theProgress, boolean fromUser)
			{
				// -----------------------------------------------------------------
				// 03/04/2015 ECU store the value that will be returned
				// 06/03/2016 ECU add in the adjustment for theMinimumValue
				// 09/06/2017 ECU removed '+ theMinimumValue'
				//            ECU adjust rest of the code to be consistent with the
				//                above change
				// -----------------------------------------------------------------
				seekBarValue = theProgress;
				// -----------------------------------------------------------------
				// 19/05/2017 ECU set up the visibility of the plus/minus buttons
				// -----------------------------------------------------------------
				minusButton.setVisibility (seekBarValue <= 0 ? View.INVISIBLE
						 									 : View.VISIBLE);
				plusButton.setVisibility  (seekBarValue >= (theMaximumValue - theMinimumValue) ? View.INVISIBLE
						   												                       : View.VISIBLE);
				// -----------------------------------------------------------------
				// 03/04/2015 ECU try and display the current value
				// 12/02/2016 ECU change to use the value text field
				// 30/05/2016 ECU changed to use resource
				// -----------------------------------------------------------------
				sliderValueTextView.setText (theContext.getString (R.string.current_value) + (seekBarValue + theMinimumValue));
				// -----------------------------------------------------------------
				// 03/04/2015 ECU try and show the new volume
				// 04/04/2015 ECU changed to use the method
				// 26/07/2015 ECU added the check on null
				// 09/06/2017 ECU added '+ theMinimumValue' to be consistent with
				//                the above changes
				// -----------------------------------------------------------------
				if (theMediaPlayer != null)
					MusicPlayer.setVolume (theMediaPlayer,(seekBarValue + theMinimumValue), theMaximumValue);
			}
			// ---------------------------------------------------------------------
			public void onStartTrackingTouch (SeekBar theSeekBar) 
			{

			}
			// ---------------------------------------------------------------------
			public void onStopTrackingTouch (SeekBar theSeekBar) 
			{

			}
			// ---------------------------------------------------------------------
		});
		// -------------------------------------------------------------------------
		seekBarBuilder.setOnCancelListener(new OnCancelListener() 
	    {                   
			@Override
			public void onCancel (DialogInterface dialog) 
			{
				// -----------------------------------------------------------------
				// 15/08/2015 ECU dismiss the dialogue on cancel
				// -----------------------------------------------------------------
				dialog.dismiss();    
				// -----------------------------------------------------------------
			}
	    }); 
		// -------------------------------------------------------------------------
		// 18/08/2015 ECU set up the cancellation button - if required
		// -------------------------------------------------------------------------
		if (theCancelLegend != null)
		{
			seekBarBuilder.setNegativeButton (theCancelLegend,new DialogInterface.OnClickListener() 
			{
				// ---------------------------------------------------------------------
				public void onClick (DialogInterface dialog,int which) 
				{
					// -----------------------------------------------------------------
					// 03/04/2015 ECU remove the dialogue
					// -----------------------------------------------------------------
					dialog.dismiss();
					// -----------------------------------------------------------------
					// 17/01/2016 ECU check if a custom method is to be actioned
					// -----------------------------------------------------------------
					if (theCancelMethod != null)
					{
						try 
						{
							// -------------------------------------------------------------
							// 17/01/2016 ECU call up the method to handle the cancel action
							// 09/06/2017 ECU added '+ theMinimumValue'
							// 09/04/2018 ECU add the underlying object as an argument
							// -------------------------------------------------------------
							Utilities.invokeMethod ((Activity) theUnderlyingObject,theCancelMethod,new Object [] {(seekBarValue + theMinimumValue)});
							// -------------------------------------------------------------
						} 
						catch (Exception theException) 
						{	
							theException.printStackTrace ();
						} 
						// -----------------------------------------------------------------
					}
					// -----------------------------------------------------------------
				}
				// ---------------------------------------------------------------------
			});
		}
		// -------------------------------------------------------------------------
		// 03/04/2015 ECU set up the confirmation buttons
		// -------------------------------------------------------------------------
		seekBarBuilder.setPositiveButton (theConfirmLegend,new DialogInterface.OnClickListener() 
		{
			// ---------------------------------------------------------------------
			public void onClick (DialogInterface dialog,int which) 
			{
				// -----------------------------------------------------------------
				// 03/04/2015 ECU remove the dialogue
				// -----------------------------------------------------------------
				dialog.dismiss();
				// -----------------------------------------------------------------
				// 03/04/2015 ECU returned the slider value
				// -----------------------------------------------------------------
				try 
				{
					// -------------------------------------------------------------
					// 03/04/2015 ECU call up the method to handle the setting of
					//                the value
					// 24/03/2016 ECU put in the check on null
					// 09/06/2017 ECU added '+ theMinimumValue'
					// 09/04/2018 ECU add the underlying object as an argument
					// -------------------------------------------------------------
					if (theConfirmMethod != null)
						Utilities.invokeMethod ((Activity) theUnderlyingObject,theConfirmMethod,new Object [] {(seekBarValue + theMinimumValue)});
					// -------------------------------------------------------------
				} 
				catch (Exception theException) 
				{	
					theException.printStackTrace ();
				} 
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		});
		// -------------------------------------------------------------------------
		// 20/05/2017 ECU moved here from higher up
		// -------------------------------------------------------------------------
		seekBar.setProgress (seekBarValue);
		// -------------------------------------------------------------------------
		// 09/06/2017 ECU the initial setProgress does not trigger the listener
		//                so set up the visibility of the plus/minus buttons here
		//            ECU NOTE - yes I know that this is duplicating code
		// -------------------------------------------------------------------------
		minusButton.setVisibility (seekBarValue <= 0 ? View.INVISIBLE
				 									 : View.VISIBLE);
		plusButton.setVisibility  (seekBarValue >= (theMaximumValue - theMinimumValue) ? View.INVISIBLE
				   												                       : View.VISIBLE);
		// -------------------------------------------------------------------------
		// 18/08/2015 ECU prevent dialogue being cancelled
		//                NOTE - only did this because pressing the back 
		// 10/08/2019 ECU changed from 'false' to use 'theCancellableFlag'
		// -------------------------------------------------------------------------
		seekBarBuilder.setCancelable (theCancellableFlag);
		// -------------------------------------------------------------------------
		// 08/04/2015 ECU call the common method to set font sizes
		// -------------------------------------------------------------------------
		adjustFonts (seekBarBuilder);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// =============================================================================
	static void sliderChoice (final Context theContext,
							  final Object 	theUnderlyingObject,
							  String theTitle,
							  final String theSubTitle,
							  int theIconId,
							  final MediaPlayer theMediaPlayer,
							  int theInitialValue,
							  final int theMinimumValue,
							  final int theMaximumValue,
							  String theConfirmLegend,
							  final Method theConfirmMethod,
							  String theCancelLegend,
							  final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 10/08/2019 ECU this was the master method until the cancellable flag was
		//                added; when calling the new method this flag is set to
		//				  false
		// -------------------------------------------------------------------------
		sliderChoice (theContext,
				  	  theUnderlyingObject,
				  	  theTitle,
				  	  theSubTitle,
				  	  theIconId,
				  	  theMediaPlayer,
				  	  theInitialValue,
				  	  theMinimumValue,
				  	  theMaximumValue,
				  	  theConfirmLegend,
				  	  theConfirmMethod,
				  	  theCancelLegend,
				  	  theCancelMethod,
				  	  false);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void sliderChoice (Context 	theContext,
			 				  final Object 	theUnderlyingObject,
			 				  String theTitle,
			 				  String theSubTitle,
			 				  final MediaPlayer theMediaPlayer,
			 				  int theInitialValue,
			 				  final int theMaximumValue,
			 				  String theConfirmLegend,
			 				  final Method theConfirmMethod)
	{
		// -------------------------------------------------------------------------
		// 15/08/2015 ECU created to call the new master method but indicating that
		//                the default icon should be used
		// 18/08/2015 ECU added the final 'null' to indicate that no 'cancel' option
		//                is required
		// 06/03/2016 ECU added the '0' to be the minimum value
		// -------------------------------------------------------------------------
		sliderChoice (theContext,
				      theUnderlyingObject,
				      theTitle,
				      theSubTitle,
				      StaticData.NO_RESULT,	// use default icon
				      theMediaPlayer,
				      theInitialValue,
				      0,
				      theMaximumValue,
				      theConfirmLegend,
				      theConfirmMethod,
				      null,
				      null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	static void sliderChoice (Context theContext,
			 				  final Object theUnderlyingObject,
			  				  String theTitle,
			  				  String theSubTitle,
			  				  int theIconId,
			  				  final MediaPlayer theMediaPlayer,
			  				  int theInitialValue,
			  				  final int theMinimumValue,
			  				  final int theMaximumValue,
			  				  String theConfirmLegend,
			  				  final Method theConfirmMethod,
			  				  String theCancelLegend)
	{
		// -------------------------------------------------------------------------
		// 17/01/2016 ECU created to call the master method
		// 06/03/2016 ECU added the minimum value
		// -------------------------------------------------------------------------
		sliderChoice (theContext,
					  theUnderlyingObject,
					  theTitle,
					  theSubTitle,
					  theIconId,
					  theMediaPlayer,
					  theInitialValue,
					  theMinimumValue,
					  theMaximumValue,
					  theConfirmLegend,
					  theConfirmMethod,
					  theCancelLegend,
					  null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================

	// =============================================================================
	static void textInput (Context theContext,
			 			   final Object theUnderlyingObject,
						   String theTitle,
						   String theSubTitle,
						   final Method theConfirmMethod,
						   final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 11/07/2015 ECU changed to use the new multiline method but just specify a
		//                single line
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,theUnderlyingObject,theTitle,theSubTitle,1,theConfirmMethod,theCancelMethod);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	static void textInput (Context theContext,
			 			   final Object theUnderlyingObject,
			   			   String theTitle,
			   			   String theSubTitle,
			   			   final Method theConfirmMethod,
			   			   final Method theCancelMethod,
			   			   int	theInputType)
	{
		// -------------------------------------------------------------------------
		// 11/07/2015 ECU changed to use the new multiline method but just specify a
		//                single line
		// 22/01/2016 ECU added the input type
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,theUnderlyingObject,theTitle,theSubTitle,1,theConfirmMethod,theCancelMethod,theInputType);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	static void textInput (Context  theContext,
			 			   final Object theUnderlyingObject,
			   			   String 	theTitle,
			   			   String 	theSubTitle,
			   			   final 	Method theConfirmMethod,
			   			   final	Method theCancelMethod,
			   			   int		theInputType,
			   			   String 	theHelpText)
	{
		// -------------------------------------------------------------------------
		// 11/07/2015 ECU changed to use the new multiline method but just specify a
		//                single line
		// 22/01/2016 ECU added the input type and the click method
		//            ECU changed to use theHelpFlag
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,theUnderlyingObject,theTitle,theSubTitle,1,theConfirmMethod,theCancelMethod,theInputType,theHelpText);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void textInput (Context theContext,
			 			   final Object theUnderlyingObject,
						   String theTitle,
						   String theSubTitle,
						   String theDefaultText,
						   final Method theConfirmMethod,
						   final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 12/07/2015 ECU created to set the default text and then use the generic
		//                method
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,theUnderlyingObject,theTitle,theSubTitle,1,theDefaultText,theConfirmMethod,theCancelMethod);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	static void textInput (Context theContext,
			 			   final Object  theUnderlyingObject,
			   			   String theTitle,
			   			   String theSubTitle,
			   			   String theDefaultText,
			   			   final Method theConfirmMethod,
			   			   final Method theCancelMethod,
			   			   int theInputType)
	{
		// -------------------------------------------------------------------------
		// 12/07/2015 ECU created to set the default text and then use the generic
		//                method
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,theUnderlyingObject,theTitle,theSubTitle,1,theDefaultText,theConfirmMethod,theCancelMethod,theInputType);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void yesNo (Context theContext,
			 		   final Object theUnderlyingObject,
			   		   String theTitle,
			   		   String theMessage,
			   		   final Object theChosenObject,
			   		   final boolean theConfirmState,
			   		   final String theConfirmLegend,
			   		   final Method theConfirmMethod,
			   		   final boolean theCancelState,
			   		   final String theCancelLegend,
			   		   final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 10/06/2015 ECU created to handle a simple yes no decision
		// 02/01/2016 ECU added arguments to tailor the buttons
		// -------------------------------------------------------------------------
		context = theContext;
		// -------------------------------------------------------------------------
		// 10/06/2015 ECU set up text view
		// -------------------------------------------------------------------------
		final TextView messageView = new TextView (theContext);
		// -------------------------------------------------------------------------
		// 16/03/2015 ECU create and build the dialogue 
		// -------------------------------------------------------------------------
		// 05/11/2016 ECU changed to use a theme rather than just '(theContext)'
		// -------------------------------------------------------------------------
		AlertDialog.Builder builder 
			= new AlertDialog.Builder (new ContextThemeWrapper (theContext, DIALOGUE_THEME));
		// -------------------------------------------------------------------------
		// 10/06/2015 ECU set the components of the dialogue
		// -------------------------------------------------------------------------
		builder.setTitle (theTitle).setView (messageView)
		// -------------------------------------------------------------------------
		.setPositiveButton (theConfirmLegend, new DialogInterface.OnClickListener() 
		{
			public void onClick (DialogInterface dialog, int whichButton) 
			{
				// -----------------------------------------------------------------
				try 
				{ 
					// -------------------------------------------------------------
					// 16/03/2015 ECU call up the method that will handle the 
					//                input text
					// 24/03/2016 ECU put in the check on null
					// 09/04/2018 ECU changed to include the underlying object
					// -------------------------------------------------------------
					if (theConfirmMethod != null)
						Utilities.invokeMethod ((Activity)theUnderlyingObject,theConfirmMethod,new Object [] {theChosenObject});
					// -------------------------------------------------------------
				} 
				catch (Exception theException) 
				{	
					theException.printStackTrace();
				} 
				// -----------------------------------------------------------------
			}
		})
		// -------------------------------------------------------------------------
		.setNegativeButton (theCancelLegend, new DialogInterface.OnClickListener() 
		{
			public void onClick (DialogInterface dialog, int whichButton) 
			{
				// -----------------------------------------------------------------
				try 
				{ 
					// -------------------------------------------------------------
					// 10/06/2015 ECU call the method that will handle the cancellation
					// 24/03/2016 ECU put in the check on null
					// 09/04/2018 ECU added the underlying object
					// -------------------------------------------------------------
					if (theCancelMethod != null)
						Utilities.invokeMethod ((Activity)theUnderlyingObject,theCancelMethod,new Object [] {theChosenObject});
					// --------------------------------------------------------------
				} 
				catch (Exception theException) 
				{	
					theException.printStackTrace();
				} 
				// -----------------------------------------------------------------
			}
		});
		// -------------------------------------------------------------------------
		// 10/06/2015 ECU set the message
		// 18/12/2016 ECU put a NEWLINE before and after the message as a spacer
		// -------------------------------------------------------------------------
		messageView.setText (StaticData.NEWLINE + theMessage + StaticData.NEWLINE); 
		// -------------------------------------------------------------------------
		// 10/06/2015 ECU set up the font size
		// 18/12/2016 ECU changed the text size from 20 to 18
		//            ECU changed to use the resource
		// -------------------------------------------------------------------------
		messageView.setTextSize (TypedValue.COMPLEX_UNIT_PX,theContext.getResources ().getDimension (R.dimen.default_font_size));
		messageView.setGravity (Gravity.CENTER);
		// -------------------------------------------------------------------------
		// 10/12/2015 ECU set the background colour - needed because on the htc
		//                it was black so trying to display black on black
		// -------------------------------------------------------------------------
		messageView.setBackgroundColor (theContext.getResources().getColor (R.color.white));
		// -------------------------------------------------------------------------
		// 10/06/2015 ECU call the common method to set fonts
		// 02/01/2016 ECU pass through the button states
		// -------------------------------------------------------------------------
		adjustFonts (builder,theConfirmState,theCancelState);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	static void yesNo (Context theContext,
			 		   final Object  theUnderlyingObject,
			   		   String theTitle,
			   		   String theMessage,
			   		   final Object theChosenObject,
			   		   final Method theConfirmMethod,
			   		   final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 02/01/2016 ECU changed to call the master method which allows the button
		//                legends to be changed
		// -------------------------------------------------------------------------
		yesNo (theContext,
			   theUnderlyingObject,
			   theTitle,
			   theMessage,
			   theChosenObject,
			   true,theContext.getString(R.string.yes),theConfirmMethod,
			   true,theContext.getString(R.string.no),theCancelMethod);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	static void adjustFonts (AlertDialog.Builder theBuilder,boolean thePositiveState,boolean theNegativeState)
	{
		// --------------------------------------------------------------------------
		// 08/04/2015 ECU call to adjust the fonts for buttons after showing the
		//                dialogue
		// 10/04/2015 ECU added the state of the buttons
		// 14/12/2015 ECU moved the definition of 'alertDialog'
		// --------------------------------------------------------------------------
		alertDialog = theBuilder.create();
		alertDialog.show();
		// --------------------------------------------------------------------------
		// 10/04/2015 ECU get the buttons
		// --------------------------------------------------------------------------
		negativeButton = (alertDialog.getButton (DialogInterface.BUTTON_NEGATIVE));
		positiveButton = (alertDialog.getButton (DialogInterface.BUTTON_POSITIVE));
		// --------------------------------------------------------------------------
		// 10/04/2015 ECU process each button, if it exists
		//            ECU set the colour of the positive / negative buttons
		//            ECU decide whether to enable or disable the buttons
		//            ECU took out font changes because it did not look good on small
		//                devices
		// --------------------------------------------------------------------------
		if (negativeButton != null)
		{
			//negativeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
			//negativeButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP,context.getResources().getDimension(R.dimen.default_font_size_smaller));
			negativeButton.setBackgroundColor (context.getResources().getColor(R.color.light_grey));
			negativeButton.setEnabled (theNegativeState);
		}
		if (positiveButton != null)
		{
			//positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
			//positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP,context.getResources().getDimension(R.dimen.default_font_size_smaller));
			positiveButton.setBackgroundColor (context.getResources().getColor(R.color.light_grey));
			positiveButton.setEnabled (thePositiveState);
		}
		// --------------------------------------------------------------------------
	}
	// ------------------------------------------------------------------------------
	static void adjustFonts (AlertDialog.Builder theBuilder)
	{
		// --------------------------------------------------------------------------
		// 10/04/2015 ECU call the master method with the buttons enabled
		// --------------------------------------------------------------------------
		adjustFonts (theBuilder,true,true);
		// --------------------------------------------------------------------------
	}
	// ==============================================================================
	
	
	
	// ==============================================================================
	// ==============================================================================
	// 22/01/2016 ECU add any methods that are used internally to complete the
	//                dialogue methods defined above
	// ==============================================================================
	// ==============================================================================
	public static void ActionCommand (String theActionCommandString)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU get the current contents of the field
		// -------------------------------------------------------------------------
		String textInField = helpTextInput.getText().toString();
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU check if need to add the separator
		// -------------------------------------------------------------------------
		if (textInField.length() > 0)
			textInField += StaticData.ACTION_SEPARATOR;
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU this is called when the whole of an action command has been
		//                acquired
		// -------------------------------------------------------------------------
		helpTextInput.setText (textInField + theActionCommandString);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	// =============================================================================
	// =============================================================================
	
	// =============================================================================
}

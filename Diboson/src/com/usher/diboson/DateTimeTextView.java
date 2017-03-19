package com.usher.diboson;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import android.widget.TextView;

public class DateTimeTextView implements Serializable
{
	// =============================================================================
	// 21/01/2017 ECU created when the date and time is to be stored in a specified
	//                textview using the specified format and to be updated at the
	//                specified interval
	// 22/01/2017 ECU added the flag for the time adjustment
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	String				originalText;
	SimpleDateFormat	simpleDateFormat;
	TextView			textView;
	boolean				timeAdjustment;
	int					updateInterval;
	// =============================================================================
	
	// =============================================================================
	public DateTimeTextView (TextView theTextView,String theDateTimeFormat,int theUpdateInterval,boolean theTimeAdjustment)
	{
		// -------------------------------------------------------------------------
		// 21/01/2017 ECU created as the main constructor to set up the local
		//                variables
		// 22/01/2017 ECU added the time adjustment
		// -------------------------------------------------------------------------
		// 21/01/2017 ECU set up the date format from the supplied string
		// -------------------------------------------------------------------------
		simpleDateFormat = new SimpleDateFormat (theDateTimeFormat,Locale.getDefault());
		simpleDateFormat.setTimeZone (TimeZone.getTimeZone ("UTC"));
		// -------------------------------------------------------------------------
		textView		= theTextView;
		timeAdjustment	= theTimeAdjustment;
		updateInterval	= theUpdateInterval;
		// -------------------------------------------------------------------------
		// 21/01/2017 ECU remember the original text
		// -------------------------------------------------------------------------
		originalText = textView.getText ().toString ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public void display ()
	{
		// -------------------------------------------------------------------------
		// 21/01/2017 ECU created to display the current time in the specified text
		//                view
		// 22/01/2017 ECU added the time adjustment option
		// -------------------------------------------------------------------------
		String localString = simpleDateFormat.format (Utilities.getAdjustedTime (timeAdjustment));
		textView.setText (localString);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void originalDisplay (int theVisibility)
	{
		// -------------------------------------------------------------------------
		// 21/01/2017 ECU restore the original text
		// 25/01/2017 ECU added the visibility as an argument
		// -------------------------------------------------------------------------
		textView.setText (originalText);
		// -------------------------------------------------------------------------
		// 25/01/2017 ECU set the required visibility
		// -------------------------------------------------------------------------
		textView.setVisibility (theVisibility);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

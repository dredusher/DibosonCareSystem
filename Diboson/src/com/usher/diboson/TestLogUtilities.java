package com.usher.diboson;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.util.Log;

// =================================================================================
public class TestLogUtilities 
{
	
	// =============================================================================
	private static final String TAG			  = "TestLogUtilities";
	@SuppressLint ("SdCardPath") 
	private static final String TEST_LOG_FILE = "/sdcard/DibosonCareSystem/TestLogFile";
	// =============================================================================
	
	// =============================================================================
	private static SimpleDateFormat simpleDateFormat = null;
	private static FileWriter 		testLogFileWriter;
	// =============================================================================
	
	// =============================================================================
	public static void Log (String theMessage)
	{
		// -------------------------------------------------------------------------
		// 03/03/2017 ECU check if need to initialise the date format
		// -------------------------------------------------------------------------
		if (simpleDateFormat == null)
		{
			simpleDateFormat = new SimpleDateFormat ("dd/MM HH:mm:ss.SSS",Locale.getDefault());
		}
		// -------------------------------------------------------------------------
		// 03/03/2017 ECU log the message to 'logcat'
		// -------------------------------------------------------------------------
		Log.i (TAG,theMessage);
		// -------------------------------------------------------------------------
		// 03/03/2017 ECU write the time stamped message to the log
		// -------------------------------------------------------------------------
		try 
		{
			// ---------------------------------------------------------------------
			// 10/01/2014 ECU open to the file in append mode
			// ---------------------------------------------------------------------
			testLogFileWriter = new FileWriter (TEST_LOG_FILE,true);
			
			testLogFileWriter.write (simpleDateFormat.format(Calendar.getInstance().getTime()) + " : " + theMessage + StaticData.NEWLINE);
			// ---------------------------------------------------------------------
			// 10/01/2014 ECU flush out the data and close
			// ---------------------------------------------------------------------
			testLogFileWriter.flush ();
			testLogFileWriter.close ();
			// ---------------------------------------------------------------------
		}
		catch (IOException theException)
		{
			// ---------------------------------------------------------------------
			// 03/03/2017 ECU problem occurred but no need to handle it
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
}
// =================================================================================

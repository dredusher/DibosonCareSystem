package com.usher.diboson;

import java.io.Serializable;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SchedulesDirectData implements Serializable
{
	// =============================================================================
	// 20/07/2016 ECU created to hold any data associated with the interface to
	//                the Schedules Direct system
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	String	baseURL;				// the base URL for all data
	String	folder;
	String	lineUp;					// the line up that is being used
	int		numberOfDays;			// the number of days of schedules to be obtained
	String	password;
	String  passwordEncrypted;
	String	token;
	String	userName;
	// =============================================================================
	
	// =============================================================================
	public SchedulesDirectData (String theUserName,String thePassword)
	{
		// -------------------------------------------------------------------------
		// 20/07/2016 ECU public constructor
		// -------------------------------------------------------------------------
		userName			=	theUserName;
		// -------------------------------------------------------------------------
		// 20/07/2016 ECU generate the encrypted password
		// 22/07/2016 ECU changed to use the method
		// -------------------------------------------------------------------------
		setExtras (thePassword);
		// -------------------------------------------------------------------------
		// 22/07/2016 ECU default the number of days retrieved - just today
		// -------------------------------------------------------------------------
		numberOfDays		= 	1;
		// -------------------------------------------------------------------------
		// 22/07/2016 ECU preset the line up that is to be used
		// -------------------------------------------------------------------------
		lineUp = MainActivity.activity.getString (R.string.schedules_direct_lineup_default);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	
	 // =============================================================================
    String generateSecureString (String theAlgorithm,String theInputString)
    {
    	// -------------------------------------------------------------------------
    	// 16/07/2016 ECU created to generate a secure string using the specified
    	//                algorithm
    	// -------------------------------------------------------------------------
    	try
    	{
    		MessageDigest messageDigest = MessageDigest.getInstance (theAlgorithm);
 	        byte[] result = messageDigest.digest (theInputString.getBytes());
 	        // ---------------------------------------------------------------------
 	        // 16/07/2016 ECU convert to the correct format to be returned
 	        // ---------------------------------------------------------------------
 	        StringBuffer stringBuffer = new StringBuffer();
 	        for (int index = 0; index < result.length; index++) 
 	        {
 	            stringBuffer.append (Integer.toString ((result[index] & 0xff) + 0x100, 16).substring(1));
 	        }
 	        // --------------------------------------------------------------------- 
 	        return stringBuffer.toString();
    	}
    	catch (Exception theException)
    	{
    		return null;
    	}
    }
    // =============================================================================
    public String Print ()
    {
    	// -------------------------------------------------------------------------
    	// 22/07/2016 ECU returns a printed version of the stored data
    	// -------------------------------------------------------------------------
    	return "Username : " 	+ userName 				+ "\n" +
    	       "Password : " 	+ password 				+ "\n" +
    	       "Encrypted : " 	+ passwordEncrypted 	+ "\n" +
    	       "Base URL : "	+ baseURL 				+ "\n" +
    	       "Days : " 		+ numberOfDays;
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public String returnDates ()
    {
    	// -------------------------------------------------------------------------
    	// 22/07/2016 ECU created to return a string of dates of format '"yyyy-mm-dd"'
    	//                based on today's date and the number of days stored
    	// -------------------------------------------------------------------------
    	long currentTime = Utilities.getAdjustedTime (false);
    	SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd",Locale.getDefault());
    	String returnString = "";
    	// -------------------------------------------------------------------------
    	// 24/07/2016 ECU on a brand installation it seems that numberOfDays could
    	//                be 0 so just check for this
    	//            ECU only occurs during development when the variable was added
    	//                to a device which had an old version of the object - but
    	//                leave anyway.
    	// -------------------------------------------------------------------------
    	if (numberOfDays == 0)
    		 numberOfDays = 1;
    	// -------------------------------------------------------------------------
    	for (int theDay = 0; theDay < numberOfDays; theDay++)
    	{
    		// ---------------------------------------------------------------------
    		// 22/07/2016 ECU add this particular date
    		// ---------------------------------------------------------------------
    		returnString += "\"" + dateFormat.format (new Date (currentTime)) + "\",";
    		// ---------------------------------------------------------------------
    		// 22/07/2016 ECU step to the next day
    		// ---------------------------------------------------------------------
    		currentTime += StaticData.MILLISECONDS_PER_DAY;
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    	// 22/07/2016 ECU there will be an extra ',' at the end which needs to be stripped
    	// -------------------------------------------------------------------------
    	// 22/07/2016 ECU return the generated string
    	// -------------------------------------------------------------------------
    	return returnString.substring (0, returnString.length () - 1);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public void setExtras (String thePassword)
    {
    	// -------------------------------------------------------------------------
    	// 22/07/2016 ECU created to set the plain text password and then generate
    	//                the encrypted password for storage.
    	//            ECU added the setting of the folder
    	// -------------------------------------------------------------------------
    	password			=  thePassword;
    	// -------------------------------------------------------------------------
    	// 20/07/2016 ECU generate the encrypted password
    	// -------------------------------------------------------------------------
    	passwordEncrypted 	= generateSecureString ("SHA1",password);
    	// -------------------------------------------------------------------------
    	// 22/07/2016 ECU set up the folder
    	// -------------------------------------------------------------------------
    	folder 				= PublicData.projectFolder + 
									MainActivity.activity.getString (R.string.schedules_direct_folder);
    	// -------------------------------------------------------------------------
    	// 26/07/2016 ECU initialise the base URL
    	// -------------------------------------------------------------------------
    	baseURL				= MainActivity.activity.getString (R.string.schedules_direct_base_url);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================

}

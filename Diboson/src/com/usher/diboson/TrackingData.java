package com.usher.diboson;

public class TrackingData 
{
	// =============================================================================
	// 20/02/2017 ECU created to be built from stored tracking data
	// =============================================================================
	
	// =============================================================================
	int		accuracy;					// accuracy set when the data was captured
	String	fileName;					// file name within the directory, NOT full path
	double	latitude;					// location latitude
	double  longitude;					// location longitude
	boolean type;						// type of data
										//     false ...... recording
										//     true	....... photograph
	// =============================================================================
	
	// =============================================================================
	public TrackingData (double theLatitude,double theLongitude,int theAccuracy,String theFileName)
	{
		// -------------------------------------------------------------------------
		// 20/02/2017 ECU public constructor to initialise the class data
		// -------------------------------------------------------------------------
		accuracy	= theAccuracy;
		fileName	= theFileName;
		latitude	= theLatitude;
		longitude	= theLongitude;
		// -------------------------------------------------------------------------
		// 20/02/2017 ECU decide the type of data depending on the file name
		// -------------------------------------------------------------------------
		type = fileName.startsWith (MainActivity.activity.getString (R.string.image_header));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

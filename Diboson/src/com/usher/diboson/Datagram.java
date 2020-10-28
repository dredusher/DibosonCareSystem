package com.usher.diboson;

import java.io.Serializable;

public class Datagram  implements Serializable 
{
	// =============================================================================
	// 02/08/2013 ECU this object will be used for transferring data between a server
	//                and registered listeners. Methods are provided to manipulate
	//                the data in a clean and secure way
	// 21/10/2014 ECU if the broadcastFlag is false then the datagram is destined
	//                for the first registered device. If set to true then it
	//                will be sent to all registered devices. In both cases the
	//                sending device will never be in the receiver list.
	//            ECU included 'type' so that the receiver can determine the
	//                reason for the datagram
	// 06/06/2017 ECU changed "\n" to StaticData.NEWLINE
	/* ============================================================================ */
	private static final long serialVersionUID = 1L;	
	/* ============================================================================ */	
	public        double          altitude;
	public        double          azimuth;
	public        String          fileName;
	public        double          latitude;
	public        double          longitude;
	public		  String          message;
	public        double          pitch;
	public        double          roll;
	public        String          sender;
	public 		  long			  time;					// 21/10/2014 ECU added
	public        int			  type;					// 21/10/2014 ECU added
	public 		  boolean		  broadcastFlag = false;	
														// 21/10/2014 ECU added
	/* ============================================================================ */
	public void Initialise (double theLongitude, double theLatitude, double theAltitude)
	{
		longitude 		= theLongitude;
		latitude  		= theLatitude;
		altitude  		= theAltitude;
		fileName 		= StaticData.BLANK_STRING;
		sender    	 	= StaticData.BLANK_STRING;
		message  	 	= StaticData.BLANK_STRING;
		
		broadcastFlag	= false;
	}
	/* ============================================================================ */
	public void UpdateCompass (String theSender,double theAzimuth, double thePitch, double theRoll)
	{
		sender   = theSender;
		azimuth  = theAzimuth;
		pitch    = thePitch;
		roll     = theRoll;
		// -------------------------------------------------------------------------
		// 21/10/2014 ECU indicate that the compass position is to be broadcast
		//                rather than sent to a particular device
		// -------------------------------------------------------------------------
		broadcastFlag	= true;
		// -------------------------------------------------------------------------
		// 21/10/2014 ECU indicate why this datagram is being sent
		// -------------------------------------------------------------------------
		type	= StaticData.DATAGRAM_COMPASS;
		// -------------------------------------------------------------------------
		// 02/08/2013 ECU now indicate need to send datagram
		
		Send ();
	}
	/* ============================================================================ */
	public void UpdateFileName (String theFileName,String theIPAddress)
	{
		fileName = theFileName;
		// -------------------------------------------------------------------------
		// 21/10/2014 ECU indicate why this datagram is being sent
		// -------------------------------------------------------------------------
		type	= StaticData.DATAGRAM_FILENAME;
		// -------------------------------------------------------------------------
		SendAndAction (theIPAddress);
	}
	/* ============================================================================ */
	public void UpdateLocation (String theSender,double theLongitude, double theLatitude, double theAltitude,long theTime)
	{
		sender    = theSender;
		longitude = theLongitude;
		latitude  = theLatitude;
		altitude  = theAltitude;
		time	  = theTime;					// 21/10/2014 ECU added
		// -------------------------------------------------------------------------
		// 21/10/2014 ECU indicate that want the datagram to be sent to all
		//                registered devices
		// -------------------------------------------------------------------------
		broadcastFlag = true;
		// -------------------------------------------------------------------------
		// 21/10/2014 ECU indicate why this datagram is being sent
		// -------------------------------------------------------------------------
		type	= StaticData.DATAGRAM_LOCATION;
		// -------------------------------------------------------------------------
		// 02/08/2013 ECU now indicate need to send datagram
		// -------------------------------------------------------------------------
		Send ();
	}
	/* ============================================================================= */
	private void Send ()
	{
		// -------------------------------------------------------------------------
		// 02/08/2013 ECU only send if the recipient is not this device
		// 20/08/2013 ECU check for the first device that is not me
		// -------------------------------------------------------------------------
		if (PublicData.datagramEnabled)
		{
			if (PublicData.datagramReceiver == null)
			{
				// -----------------------------------------------------------------
				// 20/08/2013 ECU find first device that can receive datagram
				//				  returns 'null' if there is not one
				// -----------------------------------------------------------------
				PublicData.datagramReceiver = Utilities.findFirstDevice();
			}
			// ---------------------------------------------------------------------
			// 20/08/2013 ECU added the check on null
			// ---------------------------------------------------------------------
			if (PublicData.datagramReceiver != null && !PublicData.datagramReceiver.equalsIgnoreCase(PublicData.ipAddress))
			{
				// -----------------------------------------------------------------
				// 03/08/2013 ECU indicate type of message being sent
				// 21/03/2015 ECU changed to datagramType from socketMessageType
				// -----------------------------------------------------------------
				PublicData.datagramType 	= StaticData.SOCKET_MESSAGE_DATAGRAM;
				PublicData.datagramToSend 	= true;
			}
		}
	}
	/* ============================================================================= */
	private void SendAndAction (String theIPAddress)
	{
		// -------------------------------------------------------------------------
		// 02/08/2013 ECU only send if the recipient is not this device
		// 21/03/2015 ECU changed to datagramType from socketMessageType
		
		PublicData.datagramType 		= StaticData.SOCKET_MESSAGE_DATAGRAM_ACTION;
		PublicData.datagramToSend 	= true;
		PublicData.datagramIPAddress 	= theIPAddress;
	}
	/* ============================================================================ */
	public void Message (String theMessage)
	{
		message = theMessage;
	}
	/* ============================================================================ */	
	public String Print ()
	{
		String theString = StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
		// 21/10/2014 ECU added in the 'type'
		// -------------------------------------------------------------------------
		theString += "Sender    = " + sender + StaticData.NEWLINE +
					 "Type      = " + type + StaticData.NEWLINE +
					 "Longitude = " + longitude + StaticData.NEWLINE +
					 "Latitude  = " + latitude + StaticData.NEWLINE +
					 "Altitude  = " + altitude + StaticData.NEWLINE +
					 "Azimuth   = " + azimuth + StaticData.NEWLINE +
					 "Pitch     = " + pitch + StaticData.NEWLINE +
					 "Roll  	= " + roll + StaticData.NEWLINE +
					 "Message   = " + message;	
		
		return theString;
	}
	/* ============================================================================ */
}

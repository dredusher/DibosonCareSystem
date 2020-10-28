package com.usher.diboson;

import java.io.Serializable;

public class Agency implements Serializable
{
	/* ============================================================================= */
	// 12/01/2014 ECU created to contain details of an agency
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	public String name;				// name of the agency
	public String address;			// address of the agency
	public String phoneNumber;		// contact phone number
	public String emailAddress;		// contact email address
	public String contactName;		// name of contact at agency
	public String notes;			// any relevant notes
	/* ============================================================================= */
	public Agency (String theName,
			       String theAddress,
			       String thePhoneNumber,
			       String theEmailAddress,
			       String theContactName,
			       String theNotes)
	{
		//-------------------------------------------------------------------------- 
		// 12/01/2014 ECU copy across the data into the class variables
		//-------------------------------------------------------------------------- 
		name 			= theName;
		address			= theAddress;
		phoneNumber 	= thePhoneNumber;
		emailAddress 	= theEmailAddress;
		contactName 	= theContactName;
		notes 			= theNotes;	
		// -------------------------------------------------------------------------
	}	
	/* ============================================================================= */
	public String Print ()
	{
		// -------------------------------------------------------------------------
		return "Name : " + name + StaticData.NEWLINE +
			   "Address : " + address + StaticData.NEWLINE +
			   "Phone Number : " + phoneNumber + StaticData.NEWLINE + 
			   "Email Address : " + emailAddress + StaticData.NEWLINE +
			   "Contact Name : " + contactName + StaticData.NEWLINE +
			   "Notes : " + notes;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
}

package com.usher.diboson;

import java.io.Serializable;

public class AppointmentTypeDetails implements Serializable
{
	/* ============================================================================= */
	// 06/01/2014 ECU created to contain details of appointments
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	public  String  name;				// name where appointment is
	public  String  address;			// address of appointment
	public  String	contactName;		// the contact name
	public  String  phoneNumber;		// phone number
	public	int		type;				// index into array
	/* ============================================================================= */
	public AppointmentTypeDetails() 
	{
		//--------------------------------------------------------------------------
		// 05/01/2014 ECU construct for the class
		//--------------------------------------------------------------------------
	}
	/* ============================================================================= */
}

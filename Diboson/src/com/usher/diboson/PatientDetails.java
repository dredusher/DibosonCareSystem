package com.usher.diboson;

import java.io.Serializable;

public class PatientDetails implements Serializable
{
	/* ========================================================================== */
	// 05/01/2014 ECU created
	// 09/11/2015 ECU preset the variables in the default constructor
	/* ========================================================================== */
	private static final long serialVersionUID = 1L;
	/* ========================================================================== */
	public String   foreName;			// forename
	public String   middleName;			// middle name(s)
	public String	surname;			// surname
	public String   preferredName;		// preferred form of address
	public String   dateOfBirth;		// date of birth
	public String	address;			// address
    public String   phoneNumber;		// phone number
    public String   referenceNumber;	// reference number 
    /* ========================================================================== */
    public PatientDetails (String theForeName,String theMiddleName,String theSurname,
    				String thePreferredName,String theDOB,String theAddress,
    				String thePhoneNumber,String theReferenceNumber)
    {
    	foreName 			= theForeName;				// forename
    	middleName 			= theMiddleName;			// middle name(s)
    	surname				= theSurname;				// surname
    	preferredName 		= thePreferredName;			// preferred name to use
    	dateOfBirth 		= theDOB;					// date of birth
    	address 			= theAddress;				// address
        phoneNumber		 	= thePhoneNumber;			// phone number
        referenceNumber		= theReferenceNumber;		// reference number 
    }
    /* -------------------------------------------------------------------------- */
    public PatientDetails ()
    {
    	// ----------------------------------------------------------------------
    	// 09/11/2015 ECU preset the variables to basically 'nothing'
    	// ----------------------------------------------------------------------
    	foreName 			= "";						// forename
    	middleName 			= "";						// middle name(s)
    	surname				= "";						// surname
    	preferredName 		= "";						// preferred name to use
    	dateOfBirth 		= StaticData.DEFAULT_DATE;	// date of birth
    	address 			= "";						// address
        phoneNumber		 	= "";						// phone number
        referenceNumber		= "";						// reference number 
    	// ---------------------------------------------------------------------- 	
    }
    /* ========================================================================== */
    public boolean CheckForChanges (PatientDetails thePatientDetails)
    {
    	// ----------------------------------------------------------------------
    	// 03/02/2014 ECU created - checks whether supplied record differs from
    	//                the current one
    	// ----------------------------------------------------------------------
    	if ((!thePatientDetails.foreName.equalsIgnoreCase(foreName)) ||
    			(!thePatientDetails.middleName.equalsIgnoreCase(middleName)) ||
    			(!thePatientDetails.surname.equalsIgnoreCase(surname)) ||
    			(!thePatientDetails.preferredName.equalsIgnoreCase(preferredName)) ||
    			(!thePatientDetails.dateOfBirth.equalsIgnoreCase(dateOfBirth)) ||
    			(!thePatientDetails.address.equalsIgnoreCase(address)) ||
    			(!thePatientDetails.phoneNumber.equalsIgnoreCase(phoneNumber)) ||
    			(!thePatientDetails.referenceNumber.equalsIgnoreCase(referenceNumber)))
    		return false;
    	else
    		return true;
    }
    /* ========================================================================== */
    public String Name ()
    {
    	// 05/01/2014 ECU join the names to return one single name
    	
    	return foreName + " " + middleName + " " + surname;
    }
    /* ========================================================================== */
    public String Print ()
    {
    	return "Name : " + foreName + " " + middleName + " " + surname + "\n" +
    			"Preferred Name : " + preferredName + "\n" +
    			"Address : " + address + "\n" +
    			"Phone : " + phoneNumber + "\n" +
    			"Number : " + referenceNumber;
    }
    /* ========================================================================== */
}

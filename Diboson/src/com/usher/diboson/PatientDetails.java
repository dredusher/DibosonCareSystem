package com.usher.diboson;

import android.content.Context;

import java.io.Serializable;
import java.lang.reflect.Method;

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
    public PatientDetails (String theForeName,
    					   String theMiddleName,
    					   String theSurname,
    					   String thePreferredName,
    					   String theDOB,
    					   String theAddress,
    					   String thePhoneNumber,
    					   String theReferenceNumber)
    {
    	// -------------------------------------------------------------------------
    	foreName 			= theForeName;				// forename
    	middleName 			= theMiddleName;			// middle name(s)
    	surname				= theSurname;				// surname
    	preferredName 		= thePreferredName;			// preferred name to use
    	dateOfBirth 		= theDOB;					// date of birth
    	address 			= theAddress;				// address
        phoneNumber		 	= thePhoneNumber;			// phone number
        referenceNumber		= theReferenceNumber;		// reference number
        // ----------------------------------------------------------------------
    }
    /* -------------------------------------------------------------------------- */
    public PatientDetails ()
    {
    	// ----------------------------------------------------------------------
    	// 09/11/2015 ECU preset the variables to basically 'nothing'
    	// ----------------------------------------------------------------------
    	foreName 			= StaticData.BLANK_STRING;	// forename
    	middleName 			= StaticData.BLANK_STRING;	// middle name(s)
    	surname				= StaticData.BLANK_STRING;	// surname
    	preferredName 		= StaticData.BLANK_STRING;	// preferred name to use
    	dateOfBirth 		= StaticData.DEFAULT_DATE;	// date of birth
    	address 			= StaticData.BLANK_STRING;	// address
        phoneNumber		 	= StaticData.BLANK_STRING;	// phone number
        referenceNumber		= StaticData.BLANK_STRING;	// reference number 
    	// ---------------------------------------------------------------------- 	
    }
    // --------------------------------------------------------------------------
    public PatientDetails (Context theContext,Method theCompletionMethod)
    {
    	// ----------------------------------------------------------------------
    	// 14/07/2017 ECU created constructor when initial data is to be requested
    	//                from the user. The 'theCompletionMethod' will be called
    	//                when all of the information has been retrieved
    	// ----------------------------------------------------------------------
    	// 14/07/2017 ECU first of all create a blank object
    	// ----------------------------------------------------------------------
    	this ();
    	// ----------------------------------------------------------------------
    	// 14/07/2017 ECU now request the various bits of information
    	// ----------------------------------------------------------------------
    	PatientDetailsInput.RequestPatientDetails (theContext,this,theCompletionMethod);
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
    /* ============================================================================= */
    public String Name ()
    {
    	// -------------------------------------------------------------------------
    	// 05/01/2014 ECU join the names to return one single name
    	// 07/05/2020 ECU changed to use '..SPACE_....' and check on existence of
    	//                'middlename'
    	// -------------------------------------------------------------------------
    	return foreName +
    				(Utilities.emptyString (middleName) ? (StaticData.SPACE_STRING + middleName)
    													: StaticData.BLANK_STRING) +
						StaticData.SPACE_STRING + surname;
    	// -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public String Print ()
    {
    	// -------------------------------------------------------------------------
    	// 07/05/2020 ECU changed to use 'Name'
    	// -------------------------------------------------------------------------
    	return "Name : " + Name () + StaticData.NEWLINE +
    			"Preferred Name : " + preferredName + StaticData.NEWLINE +
    			"Address : " + address + StaticData.NEWLINE +
    			"Phone : " + phoneNumber + StaticData.NEWLINE +
    			"Number : " + referenceNumber;
    	// -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    void setName (String theInputName)
    {
    	// -------------------------------------------------------------------------
    	// 14/07/2017 ECU created to set up the name components (i.e. forename(s) and
    	//                surname) from the input name
    	// -------------------------------------------------------------------------
    	String [] localNames = theInputName.split ("[ ]");
		// -------------------------------------------------------------------------
    	// 14/07/2017 ECU if there is a single word then take this to be the forename
    	// -------------------------------------------------------------------------
		if (localNames.length == 1)
		{
			foreName		= localNames [0];
			middleName 		= StaticData.BLANK_STRING;
			surname 		= StaticData.BLANK_STRING;
		}
		else
		// -------------------------------------------------------------------------
		// 14/07/2017 ECU if two words supplied then the first is taken as the
		//				  forename, the second as the surname
		// -------------------------------------------------------------------------
		if (localNames.length == 2)
		{
			foreName		= localNames [0];
			middleName 		= StaticData.BLANK_STRING;
			surname 		= localNames [1];
		}
		// -------------------------------------------------------------------------
		// 14/07/2017 ECU if the input name is made up of more than 2 words then
		//                the first word is taken as the forename, the last is taken
		//                as the surname, the middle words are joined together to
		//                form the 'middle name'
		// -------------------------------------------------------------------------
		else
		{
			foreName	= localNames [0];
			// ---------------------------------------------------------------------
			// 14/07/2017 ECU now build up the 'middle name'
			// ---------------------------------------------------------------------
			String localMiddleName = StaticData.BLANK_STRING;
			
			for (int theIndex = 1; theIndex < localNames.length - 1; theIndex++)
				localMiddleName += localNames [theIndex] + StaticData.SPACE_STRING;
			// --------------------------------------------------------------------
			// 06/01/2014 ECU added the trim to removing leading/trailing spaces
			// ---------------------------------------------------------------------
			middleName 	= localMiddleName.trim();
			// ---------------------------------------------------------------------
			// 14/07/2017 ECU now set the surname to the last input word
			// ---------------------------------------------------------------------
			surname 		= localNames [localNames.length - 1];
			// ---------------------------------------------------------------------
		}
    }
    // =============================================================================
}

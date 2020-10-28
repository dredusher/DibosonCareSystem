package com.usher.diboson;

import java.lang.reflect.Method;

import android.content.Context;
import android.text.InputType;

public class PatientDetailsInput 
{
	// =============================================================================
	static Method			completionMethod;
	static Context 			context;
	static PatientDetails	patientDetails;
	// =============================================================================
	
	// =============================================================================
	public static void RequestPatientDetails (Context theContext,PatientDetails thePatientDetails,Method theCompletionMethod)
	{
		// -------------------------------------------------------------------------
		// 14/07/2017 ECU created to initiate the dialogue to obtain the patient
		//                details
		// -------------------------------------------------------------------------
		completionMethod	= theCompletionMethod;
		context				= theContext;
		patientDetails		= thePatientDetails;
		// -------------------------------------------------------------------------
		// 14/07/2017 ECU initiate the dialogue
		// -------------------------------------------------------------------------
		SetPatientName ();
		// -------------------------------------------------------------------------		
	}
	// =============================================================================
	public static void RequestPatientDetailsComplete ()
	{
		// -------------------------------------------------------------------------
    	// 14/07/2017 ECU everything has been obtained so call up the relevant method
    	// -------------------------------------------------------------------------
    	try 
		{ 
			// -------------------------------------------------------------
			// 22/05/2017 ECU call the method and pass the data
			// -------------------------------------------------------------
			completionMethod.invoke (null,new Object [] {patientDetails});
			// --------------------------------------------------------------
		} 
		catch (Exception theException) 
		{	
			
		} 
    	// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
    public static void SetPatientAddress ()
    {
    	// -------------------------------------------------------------------------
    	// 14/07/2017 ECU created to get the patient's address
    	// -------------------------------------------------------------------------
    	DialogueUtilities.multilineTextInput (context,
											  context.getString (R.string.title_patient_address),
											  context.getString (R.string.summary_patient_address),
											  10,
											  StaticData.BLANK_STRING,
											  Utilities.createAMethod (PatientDetailsInput.class,"PatientAddressMethod",StaticData.BLANK_STRING),
											  null,
											  InputType.TYPE_CLASS_TEXT | 
											  			InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | 
											  			InputType.TYPE_TEXT_FLAG_CAP_WORDS | 
											  			InputType.TYPE_TEXT_FLAG_MULTI_LINE,
											  null,
											  true);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void PatientAddressMethod (String theAddress)
    {
    	// -------------------------------------------------------------------------
    	// 14/07/2017 ECU stored the address
    	// -------------------------------------------------------------------------
    	patientDetails.address = theAddress;
    	// -------------------------------------------------------------------------
    	// 14/07/2017 ECU request the patient's phone number
    	// -------------------------------------------------------------------------
    	SetPatientPhoneNumber ();
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
	public static void SetPatiendDateOfBirth ()
	{
		// -------------------------------------------------------------------------
    	// 14/07/2017 ECU created to get the patient's date of birth
    	// -------------------------------------------------------------------------
		DialogueUtilities.getDate (context, 
				  				   context.getString (R.string.title_patient_dob), 
				  				   context.getString (R.string.summary_patient_dob), 
				  				   context.getString (R.string.select),
				  				   Utilities.createAMethod (PatientDetailsInput.class,"PatientDOBMethod",(Object) null),
						  		   null,
						  		   null);	
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void PatientDOBMethod (Object theObject)
	{
		// -------------------------------------------------------------------------
		// 14/07/2017 ECU created to handle the 'selected' date
		// -------------------------------------------------------------------------
		int [] date = (int []) theObject;
		// -------------------------------------------------------------------------
		// 27/02/2017 ECU now get the required date in milliseconds
		// -------------------------------------------------------------------------
		patientDetails.dateOfBirth = String.format (context.getString(R.string.date_of_birth_format),date[0],(date[1] + 1),date[2]);
		// -------------------------------------------------------------------------
		// 14/07/2017 ECU now request the patient's address
		// -------------------------------------------------------------------------
		SetPatientAddress ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
    public static void SetPatientName ()
    {
    	// -------------------------------------------------------------------------
    	// 14/07/2017 ECU created to get the patient's full name
    	// -------------------------------------------------------------------------
    	DialogueUtilities.multilineTextInput (context,
											  context.getString (R.string.title_patient_name),
											  context.getString (R.string.summary_patient_name),
											  1,
											  StaticData.BLANK_STRING,
											  Utilities.createAMethod (PatientDetailsInput.class,"PatientNameMethod",StaticData.BLANK_STRING),
											  null,
											  InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS,
											  null,
											  true);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void PatientNameMethod (String theName)
    {
    	// -------------------------------------------------------------------------
    	// 14/07/2017 ECU created to store the patient's full name
    	// -------------------------------------------------------------------------
    	patientDetails.setName (theName);
    	// -------------------------------------------------------------------------
    	// 14/07/2017 ECU now request the patient's preferred name
    	// -------------------------------------------------------------------------
    	SetPatientPreferredName ();
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void SetPatientPhoneNumber ()
    {
    	// -------------------------------------------------------------------------
    	// 14/07/2017 ECU created to get the patient's phone number
    	// -------------------------------------------------------------------------
    	DialogueUtilities.multilineTextInput (context,
											  context.getString (R.string.title_patient_phone_number),
											  context.getString (R.string.summary_patient_phone_number),
											  1,
											  StaticData.BLANK_STRING,
											  Utilities.createAMethod (PatientDetailsInput.class,"PatientPhoneNumberMethod",StaticData.BLANK_STRING),
											  null,
											  InputType.TYPE_CLASS_PHONE,
											  null,
											  true);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void PatientPhoneNumberMethod (String thePhoneNumber)
    {
    	// -------------------------------------------------------------------------
    	// 14/07/2017 ECU store the input phone number
    	// -------------------------------------------------------------------------
    	patientDetails.phoneNumber = thePhoneNumber;
    	// -------------------------------------------------------------------------
    	// 14/07/2017 ECU now request the patient's reference number
    	// -------------------------------------------------------------------------
    	SetPatientReferenceNumber ();
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void SetPatientPreferredName ()
    {
    	// -------------------------------------------------------------------------
    	// 14/07/2017 ECU created to get the patient's preferred name
    	// -------------------------------------------------------------------------
    	DialogueUtilities.multilineTextInput (context,
											  context.getString (R.string.title_patient_preferred_name),
											  context.getString (R.string.summary_patient_preferred_name),
											  1,
											  StaticData.BLANK_STRING,
											  Utilities.createAMethod (PatientDetailsInput.class,"PatientPreferredNameMethod",StaticData.BLANK_STRING),
											  null,
											  InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS,
											  null,
											  true);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void PatientPreferredNameMethod (String theName)
    {
    	patientDetails.preferredName = theName;
    	
    	SetPatiendDateOfBirth ();
    }
    // =============================================================================
    public static void SetPatientReferenceNumber ()
    {
    	DialogueUtilities.multilineTextInput (context,
											  context.getString (R.string.title_patient_reference_number),
											  context.getString (R.string.summary_patient_reference_number),
											  1,
											  StaticData.BLANK_STRING,
											  Utilities.createAMethod (PatientDetailsInput.class,"PatientReferenceNumberMethod",StaticData.BLANK_STRING),
											  null,
											  InputType.TYPE_CLASS_TEXT,
											  null,
											  true);
    }
    // =============================================================================
    public static void PatientReferenceNumberMethod (String theNumber)
    {
    	// -------------------------------------------------------------------------
    	// 14/07/2017 ECU store the input reference number
    	// -------------------------------------------------------------------------
    	patientDetails.referenceNumber = theNumber;
    	// -------------------------------------------------------------------------
    	// 14/07/2017 ECU all of the request data has been obtained so call the 
    	//                method to handle this
    	// -------------------------------------------------------------------------
    	RequestPatientDetailsComplete ();
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    
    // =============================================================================
    public static void WriteToDisk (Object thePatientDetails)
    {
    	// -------------------------------------------------------------------------
    	// 14/07/2017 ECU created to write the specified data to disk
    	// -------------------------------------------------------------------------
    	AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + context.getString (R.string.patient_details_file),
											(PatientDetails)thePatientDetails);
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
}

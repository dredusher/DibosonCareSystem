package com.usher.diboson;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class DatabaseUtilities 
{
	// =============================================================================
	// 15/11/2015 ECU created to contain methods which are used to access the
	//                databases held on the device
	// =============================================================================
	

	// =============================================================================
	public static List<String> findName (Context theContext,String theName)
	{
		// -------------------------------------------------------------------------
		// 15/11/2015 created to see if the specified name has an entry, or entries,
		//            in the contacts database
		// -------------------------------------------------------------------------
		List<String> localNames = new ArrayList<String> ();
		// -------------------------------------------------------------------------
		ContentResolver contentResolver = theContext.getContentResolver(); 
		Cursor cursor = contentResolver.query (ContactsContract.Contacts.CONTENT_URI, 
											   new String [] {ContactsContract.Contacts.DISPLAY_NAME,ContactsContract.Contacts._ID}, 
											   ContactsContract.Contacts.DISPLAY_NAME + " LIKE ? ",
											   new String [] {theName},
											   null); 
		// ------------------------------------------------------------------------
		// 15/11/2015 ECU if entries are found then return the ID's which
		//                correspond to each of the names found
		// ------------------------------------------------------------------------
		if (cursor.getCount() > 0) 
		{ 
			while (cursor.moveToNext()) 
			{
				// -----------------------------------------------------------------
				// 15/11/2015 ECU get the id of this entry which can be used to get
				//                related information
				// -----------------------------------------------------------------
				localNames.add (cursor.getString (cursor.getColumnIndex(ContactsContract.Contacts._ID)));
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 15/11/2015 ECU return the results
		// -------------------------------------------------------------------------
		return localNames;
		// -------------------------------------------------------------------------	
	}
	// =============================================================================
	public static List<String> getEmailAddress (Context theContext,String theNameID)
	{
		// -------------------------------------------------------------------------
		// 15/11/2015 ECU created to return the email address or addresses that
		//                correspond to the specified name ID 
		// -------------------------------------------------------------------------
		List<String> localAddresses = new ArrayList<String> ();
		// -------------------------------------------------------------------------
		ContentResolver contentResolver = theContext.getContentResolver(); 
		// -------------------------------------------------------------
		Cursor cursor = contentResolver.query (ContactsContract.CommonDataKinds.Email.CONTENT_URI,
											   new String [] {ContactsContract.CommonDataKinds.Email.ADDRESS,ContactsContract.CommonDataKinds.Email.CONTACT_ID}, 
											   ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
											   new String[] {theNameID}, null); 
		// -------------------------------------------------------------------------
		// 15/11/2015 ECU check if anything was found
		// -------------------------------------------------------------------------
		if (cursor.getCount() > 0)
		{
			// -------------------------------------------------------------
			// 15/11/2015 ECU loop through the phone entries
			// -------------------------------------------------------------
			while (cursor.moveToNext ()) 
			{ 
				localAddresses.add (cursor.getString (cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));
			}
		}
		// -------------------------------------------------------------------------
		// 15/11/2015 ECU return the results
		// -------------------------------------------------------------------------
		return localAddresses;
		// -------------------------------------------------------------------------		
	}
	// =============================================================================
	public static List<String> getPhoneNumber (Context theContext,String theNameID)
	{
		// -------------------------------------------------------------------------
		// 15/11/2015 ECU created to return the phone number or phone numbers that
		//                correspond to the specified name ID 
		// -------------------------------------------------------------------------
		List<String> localPhoneNumbers = new ArrayList<String> ();
		// -------------------------------------------------------------------------
		ContentResolver contentResolver = theContext.getContentResolver(); 
		// -------------------------------------------------------------
		Cursor cursor = contentResolver.query (ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
											   new String [] {ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.CONTACT_ID},
											   ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
											   new String[] {theNameID}, 
											   null); 
		// -------------------------------------------------------------------------
		// 15/11/2015 ECU check if anything was found
		// -------------------------------------------------------------------------
		if (cursor.getCount() > 0)
		{
			// -------------------------------------------------------------
			// 15/11/2015 ECU loop through the phone entries
			// -------------------------------------------------------------
			while (cursor.moveToNext()) 
			{ 
				localPhoneNumbers.add(cursor.getString (cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
			}
		}
		// -------------------------------------------------------------------------
		// 15/11/2015 ECU return the results
		// -------------------------------------------------------------------------
		return localPhoneNumbers;
		// -------------------------------------------------------------------------		
	}
	// =============================================================================
	public static String summary (Context theContext,String theNameID)
	{
		// -------------------------------------------------------------------------
		// 15/11/2015 ECU created to return a summary of the record whose name id
		//                is supplied
		// -------------------------------------------------------------------------
		String resultsString = "Name ID : " + theNameID + "\n";
		// -------------------------------------------------------------------------
		// 15/11/2015 ECU get the phones for this ID
		// -------------------------------------------------------------------------
		List<String>phones = DatabaseUtilities.getPhoneNumber(theContext, theNameID);
		
		for (int thePhone = 0; thePhone < phones.size(); thePhone++)
		{
			resultsString += "     Phone : " + phones.get (thePhone) + "\n";;
		}
		
		List<String>addresses = DatabaseUtilities.getEmailAddress(theContext, theNameID);
		
		for (int theAddress = 0; theAddress < addresses.size(); theAddress++)
		{
			resultsString += "          Email : " + addresses.get(theAddress) + "\n";
		}
		// -------------------------------------------------------------------------
		return resultsString;
	}
	// -----------------------------------------------------------------------------
	public static String summary (Context theContext,List<String> theNameIDs)
	{
		// -------------------------------------------------------------------------
		// 15/11/2015 ECU created to return a summary of the record whose name id
		//                is supplied
		// -------------------------------------------------------------------------
		String resultsString = "";
		// -------------------------------------------------------------------------
		for (int theID = 0; theID < theNameIDs.size(); theID++)
		{
			resultsString += DatabaseUtilities.summary (theContext,theNameIDs.get (theID));
		}
		// -------------------------------------------------------------------------
		return resultsString;
	}
	// =============================================================================
}

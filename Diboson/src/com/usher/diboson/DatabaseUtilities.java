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
	private final static int DATA_ID 	= 0;
	private final static int DATA_NAME	= 1;
	// =============================================================================
	
	// =============================================================================
	private static Context context;			// 20/12/2017 ECU added
	private static String  name = null;			// 20/12/2017 ECU added
	// =============================================================================
	
	// =============================================================================
	public static ArrayList<ListItem> BuildTheContactsList ()
	{
		// -------------------------------------------------------------------------
		// 20/12/2017 ECU created to build a list of the contents ready to use
		//                with the Selector class
		// --------------------------------------------------------------------------
		SelectorUtilities.selectorParameter.listItems = new ArrayList<ListItem>();
		// --------------------------------------------------------------------------
		// 20/12/2017 ECU search for the name that will have previously been set
		//---------------------------------------------------------------------------
		ContentResolver contentResolver = context.getContentResolver(); 
		Cursor cursor = contentResolver.query (ContactsContract.Contacts.CONTENT_URI, 
											   new String [] {ContactsContract.Contacts.DISPLAY_NAME,
				                                              ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
				                                              ContactsContract.Contacts._ID}, 
											   ContactsContract.Contacts.DISPLAY_NAME + " LIKE ? ",
											   new String [] {name},
											   null); 
		// -------------------------------------------------------------------------
		// 15/11/2015 ECU if entries are found then return the ID's which
		//                correspond to each of the names found
		// -------------------------------------------------------------------------
		if (cursor.getCount() > 0) 
		{ 
			// ---------------------------------------------------------------------
			// 20/12/2017 ECU initialise the 'index' will point to the name at
			//                a later stage
			// ---------------------------------------------------------------------
			int index = 0;
			// ---------------------------------------------------------------------
			while (cursor.moveToNext()) 
			{
				// -----------------------------------------------------------------
				// 19/12/2017 ECU add the entry into the list
				// -----------------------------------------------------------------
				String contactID = cursor.getString (cursor.getColumnIndex (ContactsContract.Contacts._ID));
				
				SelectorUtilities.selectorParameter.listItems.add (new ListItem (
																	StaticData.BLANK_STRING,
																	cursor.getString (cursor.getColumnIndex (ContactsContract.Contacts.DISPLAY_NAME)),
																	stringConvert (getPhoneNumber (context,contactID)),
																	stringConvert (getEmailAddress (context,contactID)),
																	index++));
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 18/12/2017 ECU close down various components
		// -------------------------------------------------------------------------
		cursor.close ();
		// -------------------------------------------------------------------------
		// 20/12/2017 ECU return the generated list
		// -------------------------------------------------------------------------  
		return SelectorUtilities.selectorParameter.listItems;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public static ArrayList<ListItem> BuildTheContactsList (Context theContext,String theSearchString)
	{
		// -------------------------------------------------------------------------
		// 21/12/2017 ECU just set the context before calling the main method
		// -------------------------------------------------------------------------
		context = theContext;
		name	= theSearchString;
		// -------------------------------------------------------------------------
		// 21/12/2017 ECU call up the main method
		// -------------------------------------------------------------------------
		return BuildTheContactsList ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static List<String []> findName (Context theContext,String theName)
	{
		// -------------------------------------------------------------------------
		// 15/11/2015 created to see if the specified name has an entry, or entries,
		//            in the contacts database
		// 19/12/2017 ECU changed to String [] from String
		// -------------------------------------------------------------------------
		// 20/12/2017 ECU remember the context and name for any future use
		// -------------------------------------------------------------------------
		context = theContext;
		name 	= theName;
		// -------------------------------------------------------------------------
		List<String []> localNames = new ArrayList<String []> ();
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
				// 19/12/2017 ECU declare the string array for the retrieved data
				// -----------------------------------------------------------------
				String [] localNameData = new String [2];
				// -----------------------------------------------------------------
				// 15/11/2015 ECU get the id of this entry which can be used to get
				//                related information
				// -----------------------------------------------------------------
				localNameData [DATA_ID]   = cursor.getString (cursor.getColumnIndex (ContactsContract.Contacts._ID));
				localNameData [DATA_NAME] = cursor.getString (cursor.getColumnIndex (ContactsContract.Contacts.DISPLAY_NAME));
				// -----------------------------------------------------------------
				// 19/12/2017 ECU add the entry into the list
				// -----------------------------------------------------------------
				localNames.add (localNameData);
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 18/12/2017 ECU close down various components
		// -------------------------------------------------------------------------
		cursor.close ();
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
		// 18/12/2017 ECU close down various components
		// -------------------------------------------------------------------------
		cursor.close ();
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
			// ---------------------------------------------------------------------
			// 15/11/2015 ECU loop through the phone entries
			// ---------------------------------------------------------------------
			while (cursor.moveToNext()) 
			{ 
				localPhoneNumbers.add (cursor.getString (cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
			}
		}
		// -------------------------------------------------------------------------
		// 18/12/2017 ECU close down various components
		// -------------------------------------------------------------------------
		cursor.close ();
		// -------------------------------------------------------------------------
		// 15/11/2015 ECU return the results
		// -------------------------------------------------------------------------
		return localPhoneNumbers;
		// -------------------------------------------------------------------------		
	}
	// =============================================================================
	static String stringConvert (List<String> theStringList)
	{
		// -------------------------------------------------------------------------
		// 20/12/2017 ECU created to convert a string list to a string
		// -------------------------------------------------------------------------
		String localString = StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
		// 20/12/2017 ECU check if the arguments is valid
		// -------------------------------------------------------------------------
		if (theStringList != null && theStringList.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 20/12/2017 ECU loop through each entry in the list
			// ---------------------------------------------------------------------
			for (int index = 0; index < theStringList.size(); index++)
				localString += theStringList.get(index) + StaticData.NEWLINE;
			// ---------------------------------------------------------------------
			// 20/12/2017 ECU remove the very last delimiter
			// ---------------------------------------------------------------------
			localString = localString.substring (0, localString.length() - 1);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 20/12/2017 ECU return the generated list 
		// -------------------------------------------------------------------------
		return localString;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String summary (Context theContext,String [] theNameData)
	{
		// -------------------------------------------------------------------------
		// 15/11/2015 ECU created to return a summary of the record whose name id
		//                is supplied
		// 19/12/2017 ECU changed from String to String []
		// -------------------------------------------------------------------------
		String resultsString = "Name ID : " + theNameData [DATA_ID]   + StaticData.NEWLINE +
				               "  Name : "  + theNameData [DATA_NAME] + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 15/11/2015 ECU get the phones for this ID
		// -------------------------------------------------------------------------
		List<String>phones = DatabaseUtilities.getPhoneNumber (theContext, theNameData [0]);
		
		for (int thePhone = 0; thePhone < phones.size(); thePhone++)
		{
			resultsString += "     Phone : " + phones.get (thePhone) + StaticData.NEWLINE;
		}
		
		List<String>addresses = DatabaseUtilities.getEmailAddress (theContext, theNameData [0]);
		
		for (int theAddress = 0; theAddress < addresses.size(); theAddress++)
		{
			resultsString += "          Email : " + addresses.get(theAddress) + StaticData.NEWLINE;
		}
		// -------------------------------------------------------------------------
		return resultsString;
	}
	// -----------------------------------------------------------------------------
	public static String summary (Context theContext,List<String []> theNameData)
	{
		// -------------------------------------------------------------------------
		// 15/11/2015 ECU created to return a summary of the record whose name id
		//                is supplied
		// 19/12/2017 ECU changed the argument to String [] from String
		// -------------------------------------------------------------------------
		String resultsString = StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
		for (int theID = 0; theID < theNameData.size(); theID++)
		{
			resultsString += DatabaseUtilities.summary (theContext,theNameData.get (theID));
		}
		// -------------------------------------------------------------------------
		return resultsString;
	}
	// =============================================================================
}

package com.usher.diboson;

import java.io.Serializable;
import java.util.Arrays;

import android.content.Context;
import android.widget.Toast;

public class Carer implements Serializable
{
	/* ============================================================================= */
	// 09/01/2014 ECU created to contain details of a carer
	// 31/01/2016 ECU IMPORTANT the position of a carer in the list is stored elsewhere
	//                ========= in the system so that when a carer is deleted then
	//                          the object in the list cannot just be deleted as this
	//                          would invalidate all other objects in the list. So
	//                          introduce the 'deleted' variable which will indicate
	//                          that the carer has been deleted but its object will
	//                          remain with this variable set to true.
	// 22/11/2016 ECU added the Size method to return the number of 'non-deleted'
	//                objects - if the list exists.
	// 27/11/2016 ECU added 'visitActive' which is active for the whole duration of
	//                the visit from 'arrival message'
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	// 11/01/2014 ECU the following are the main variables of the class
	// -----------------------------------------------------------------------------
	public int      agencyIndex;	// 14/01/2014 ECU index to agency
	public String	bluetooth;		// 09/01/2014 ECU bluetooth name
	public boolean  deleted;		// 31/01/2016 ECU added - this carer has been deleted
	public String	name;			// 09/01/2014 ECU name of the carer
	public String	phone;			// 09/01/2014 ECU contact phone number
	public String	photo;			// 05/02/2014 ECU added - path to photo
	/* ============================================================================= */
	// 11/01/2014 ECU the following are just temporary variables
	// -----------------------------------------------------------------------------
	private boolean  announcedInRange    = false;// 26/01/2015 ECU whether in range message
		                                        //                spoken
	public 	boolean	 bluetoothDiscovered = false;
												// 10/01/2014 ECU indicate still in discovered
												//                list
	// -----------------------------------------------------------------------------
	// 25/11/2016 ECU with bluetooth the discovery seems to 'miss' a device which
	//                is still visible. To save putting up a message each time this
	//                happens then use 'dropOuts' to indicate how many are allowed in
	//                succession before a device is deemed to be 'out of range'
	// -----------------------------------------------------------------------------
	public  int      dropOuts			= 0;	// 25/11/2016 ECU added see note above
	// -----------------------------------------------------------------------------
	public  long     endOfVisit 		= 0;	// 10/01/2014 ECU end date/time of visit
	// -----------------------------------------------------------------------------
	// 27/11/2016 ECU manualTermination is used to decided whether the current visit,
	//                which has ended, because the carer has reappeared within
	//                the 'grace period' - this is really for use with bluetooth
	//                detection - for manual input then the extension period
	//                is not allowed
	// -----------------------------------------------------------------------------
	public  boolean	 manualTermination	= false;// 27/11/2016 ECU added
	public 	long     startOfVisit;				// 10/01/2014 ECU start date/time of a visit
	public  boolean  tasks [];					// 05/10/2016 ECU tasks to be performed during
	                                            //                visit
	public  boolean  visitActive		= false;// 27/11/2016 ECU added
	public	boolean  visitAdded 		= true;
												// 10/01/2014 ECU whether a visit record added
	                                            // 25/11/2016 ECU changed the default to true
	public 	boolean  visitStarted 		= false;
												// 10/01/2014 ECU indicates that a visit
												//                has started
	/* ============================================================================= */
	public Carer (String theName, String thePhone, String theBluetooth,int theAgencyIndex,String thePhotoPath)
	{
		// -------------------------------------------------------------------------
		// 09/01/2014 ECU copy into class variables
		// 14/01/2014 ECU added the agency index
		// 05/02/2014 ECU added the photo path
		// 20/03/2014 ECU the photo path is stored relative to the project folder
		// 25/01/2015 ECU included the 'inRange' flag
		// 31/01/2016 ECU added the 'deleted' flag
		// 16/11/2016 ECU changed to use 'getRelat..' rather than 'Relat...'
		// -------------------------------------------------------------------------	
		agencyIndex	= theAgencyIndex;
		bluetooth	= theBluetooth;
		deleted		= false;
		name 		= theName;
		phone		= thePhone;
		photo		= Utilities.getRelativeFileName (thePhotoPath);									
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void Add (Carer theCarerObject)
	{
		// -------------------------------------------------------------------------
		// 31/01/2016 ECU created to add a new carer object, taking into account the
		//                reuse of 'deleted' objects
		// -------------------------------------------------------------------------
		if (PublicData.carers.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 31/01/2016 ECU scan the existing list to see if there is a deleted
			//                entry
			// ---------------------------------------------------------------------
			for (int theCarer = 0; theCarer < PublicData.carers.size(); theCarer++)
			{
				// -----------------------------------------------------------------
				// 31/01/2016 ECU check if this entry has been deleted
				// -----------------------------------------------------------------
				if (PublicData.carers.get (theCarer).deleted)
				{
					// -------------------------------------------------------------
					// 31/01/2016 ECU have found a deleted entry so 're-use' it
					// -------------------------------------------------------------
					PublicData.carers.set (theCarer, theCarerObject);
					// -------------------------------------------------------------
					// 31/01/2016 ECU just return as no more processing is needed
					// -------------------------------------------------------------
					return;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 31/01/2016 ECU reach here if no deleted object has been found so just
			//                need to add the entry
			// ---------------------------------------------------------------------
			PublicData.carers.add (theCarerObject);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 31/01/2016 ECU this is an empty list so just add the object
			// ---------------------------------------------------------------------
			PublicData.carers.add (theCarerObject);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void AnnounceWhetherInRange (Context theContext,boolean theInRangeFlag)
	{
		// -------------------------------------------------------------------------
		// 26/01/2015 ECU decides whether to announce the presence of a carer
		//                    theInRangeFlag = true      in range
		//                                   = false     out of range
		// -------------------------------------------------------------------------
		if (theInRangeFlag)
		{
			// ---------------------------------------------------------------------
			// 26/01/2015 ECU the carer is in range so decide whether to make the
			//                announcement
			// ---------------------------------------------------------------------
			if (!announcedInRange)
			{
				Utilities.SpeakAPhrase (theContext,name + theContext.getString (R.string.in_range));
				// -----------------------------------------------------------------
				// 25/08/2015 ECU pop up toast
				// 27/11/2016 ECU removed '+ " " + ' as added into resource
				// -----------------------------------------------------------------
				Utilities.popToast (name + theContext.getString (R.string.in_range),
									true,
									Toast.LENGTH_LONG,
									PublicData.projectFolder + photo);
				// -----------------------------------------------------------------
				announcedInRange = true;
			}
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 26/01/2015 ECU the carer is out of range so decide whether to make the
			//                announcement
			// ---------------------------------------------------------------------
			if (announcedInRange)
			{
				Utilities.SpeakAPhrase (theContext,name + theContext.getString (R.string.out_of_range));
				// -----------------------------------------------------------------
				// 26/08/2015 ECU pop up toast
				// -----------------------------------------------------------------
				Utilities.popToast (name + " " + theContext.getString (R.string.out_of_range),
										true,Toast.LENGTH_LONG,PublicData.projectFolder + photo);
				// -----------------------------------------------------------------
				announcedInRange = false;
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public boolean BluetoothDiscovered (boolean theNewState)
	{
		// -------------------------------------------------------------------------
		// 04/10/2016 ECU created to change the bluetooth state - if required
		// -------------------------------------------------------------------------
		if (bluetoothDiscovered != theNewState)
		{
			// ---------------------------------------------------------------------
			// 04/10/2016 ECU set the new state and indicate this to the caller
			// ---------------------------------------------------------------------
			bluetoothDiscovered = theNewState;
			return true;
		}
		else
		{
			// ---------------------------------------------------------------------
			// 04/10/2016 ECU no state change so indicate this to the caller
			// ---------------------------------------------------------------------
			return false;
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void Delete ()
	{
		// -------------------------------------------------------------------------
		// 31/01/2016 ECU created to indicate that the object has been deleted
		// -------------------------------------------------------------------------
		deleted = true;
		// -------------------------------------------------------------------------
		// 31/01/2016 ECU and invalidate all other relevant fields
		// -------------------------------------------------------------------------
		agencyIndex	= StaticData.NO_RESULT;
		bluetooth	= "";
		name 		= "";
		phone		= "";
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 14/01/2014 ECU print the agency name
		// -------------------------------------------------------------------------
		return "Name      : " + name + "\n" +
			   "Phone     : " + phone + "\n" +
			   "Bluetooth : " + bluetooth + "\n" +
			   "Agency    : " + PublicData.agencies.get(agencyIndex).name + "\n" +
			   "Photo     : " + photo;
	}
	// -----------------------------------------------------------------------------
	public String PrintAll ()
	{
		// -------------------------------------------------------------------------
		// 14/01/2014 ECU print the agency name
		// 31/01/2016 ECU added the 'deleted' flag
		// -------------------------------------------------------------------------
		return "Name      : " + name + "\n" +
			   "Phone     : " + phone + "\n" +
			   "Bluetooth : " + bluetooth + "\n" +
			   "Agency    : " + PublicData.agencies.get(agencyIndex).name + "\n" +
			   "Photo     : " + photo + "\n" +
			   "Start of Visit : " + PublicData.dateFormatter.format(startOfVisit) + "\n" +
			   "End   of Visit : " + PublicData.dateFormatter.format(endOfVisit) + "\n" +
			   "Deleted : " + deleted + "\n";
	}
	// =============================================================================
	public static int Size ()
	{
		int localCounter = 0;
		// -------------------------------------------------------------------------
		// 22/11/2016 ECU created to return the size of the carer list taking into
		//                account whether an object is deleted or not
		// -------------------------------------------------------------------------
		if (PublicData.carers != null)
		{
			// ---------------------------------------------------------------------
			// 22/11/2016 ECU now loop through the list counting objects which have
			//                not been deleted
			// ---------------------------------------------------------------------
			if (PublicData.carers.size () > 0)
			{
				for (int index = 0; index < PublicData.carers.size(); index++)
				{
					// -------------------------------------------------------------
					// 22/11/2016 ECU only count objects which are not deleted
					// -------------------------------------------------------------
					if (!PublicData.carers.get(index).deleted)
					{
						localCounter++;
					}
					// -------------------------------------------------------------
				}
			}
		}
		// -------------------------------------------------------------------------
		// 22/11/2016 ECU return the number of 'non-deleted' carers or 0 if the list
		//                has not been set yet or there are none
		// -------------------------------------------------------------------------
		return localCounter;
	}
	// =============================================================================
	public void Tasks (boolean [] theTasks)
	{
		// -------------------------------------------------------------------------
		// 05/10/2016 ECU created to set the stored tasks
		// -------------------------------------------------------------------------
		if (theTasks != null)
		{
			// ---------------------------------------------------------------------
			// 05/10/2016 ECU tasks have been set so copy them across
			// 30/11/2016 ECU use copyOf because '=' wrong
			// ---------------------------------------------------------------------
			tasks = Arrays.copyOf (theTasks,theTasks.length);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 05/10/2016 ECU no tasks have been set so preset the array
			// 06/12/2016 ECU changed to use the PublicData.tas....
			// ---------------------------------------------------------------------
			tasks = new boolean [PublicData.tasksToDo.length];
   			for (int index = 0; index < tasks.length; index++)
   			{
   				tasks [index] = false;
   			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String TasksPerformed (String theTitle)
	{
		boolean localTitle = true;
		String  localString = "";
		String  localIndent = new String(new char[theTitle.length()]).replace('\0', ' ');

		// -------------------------------------------------------------------------
		// 05/10/2016 ECU build up a printable list of the tasks done
		// 01/11/2016 ECU put in the null check
		// -------------------------------------------------------------------------
		if (tasks != null)
		{
			for (int index = 0; index < tasks.length; index++)
			{
				if (tasks [index])
				{
					// -------------------------------------------------------------
					// 06/12/2016 ECU changed to use PublicData.tas.... which has 
					//                already set up the patient's preferred name
					// -------------------------------------------------------------
					if (localTitle)
					{
						localString += StaticData.NEWLINE + theTitle + PublicData.tasksToDo [index] + StaticData.NEWLINE;
						// ---------------------------------------------------------
						// 05/10/2016 ECU indicate that the title has been included
						// ---------------------------------------------------------
						localTitle = false;
						// ---------------------------------------------------------
					}
					else
					{
						localString += localIndent + PublicData.tasksToDo [index] + StaticData.NEWLINE;
					}
				}
			}
		}
		// -------------------------------------------------------------------------
		// 05/10/2016 ECU return the generated string
		// -------------------------------------------------------------------------
		return localString;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public boolean VisitStarted (boolean theNewState)
	{
		// -------------------------------------------------------------------------
		// 04/10/2016 ECU created to change the 'visit started' state - if required
		// -------------------------------------------------------------------------
		if (visitStarted != theNewState)
		{
			// ---------------------------------------------------------------------
			// 04/10/2016 ECU set the new state and indicate this to the caller
			// ---------------------------------------------------------------------
			visitStarted = theNewState;
			return true;
		}
		else
		{
			// ---------------------------------------------------------------------
			// 04/10/2016 ECU no state change so indicate this to the caller
			// ---------------------------------------------------------------------
			return false;
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

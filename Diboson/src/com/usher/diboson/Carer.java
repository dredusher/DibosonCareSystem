package com.usher.diboson;

import android.content.Context;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Arrays;

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
	// 20/03/2017 ECU changed from "" to BLANK.....
	// 06/06/2017 ECU changed "\n" to StaticData.NEWLINE
	// 08/09/2017 ECU added the 'hashCode' method to take account of the delete flag
	// 23/09/2020 ECU it is possible that a user may choose to include more than one
	//                carer in one record, e.g. if the same carers work in pairs. In
	//                these cases the 'name' will contain each carer's name separated
	//                by a 'conjunction' like 'and' or '&'. This does not cause any
	//                issues but when the app speaks or displays phrases to the user
	//                the the wrong verb will be used - e.g. if the name is stored
	//                as 'John Smith and Joan Black' then the verb would be 'is'
	//                instead of 'are'. The field 'multipleCarers' has been added to
	//                sort this out.
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	// 11/01/2014 ECU the following are the main variables of the class
	// -----------------------------------------------------------------------------
	public int      agencyIndex;	// 14/01/2014 ECU index to agency
	public String	bluetooth;		// 09/01/2014 ECU bluetooth name
	public boolean  deleted;		// 31/01/2016 ECU added - this carer has been deleted
	public boolean  multipleCarers; // 23/09/2020 ECU added - indicate whether more than
	                                //                carer is defined in the name
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
		// 23/09/2020 ECU decide if more that one carer is defined in the name
		// -------------------------------------------------------------------------
		multipleCarers = Utilities.checkForPlural (name,
			MainActivity.activity.getResources().getStringArray (R.array.conjunctions));
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
			// 23/09/2020 ECU changed to use Phrase
			// ---------------------------------------------------------------------
			if (!announcedInRange)
			{
				Utilities.SpeakAPhrase (theContext,name + Phrase (theContext,theContext.getString (R.string.in_range)));
				// -----------------------------------------------------------------
				// 25/08/2015 ECU pop up toast
				// 27/11/2016 ECU removed '+ " " + ' as added into resource
				// -----------------------------------------------------------------
				Utilities.popToast (name +  Phrase (theContext,theContext.getString (R.string.in_range)),
									true,
									Toast.LENGTH_LONG,
									PublicData.projectFolder + photo);
				// -----------------------------------------------------------------
				// 15/11/2019 ECU Note - indicate that the 'in range' announcement
				//                       has been made
				// ------------------------------------------------------------------
				announcedInRange = true;
				// -----------------------------------------------------------------
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
				// -----------------------------------------------------------------
				// 15/11/2019 ECU Note - the bluetooth device is out of range
				// 23/09/2020 ECU changed to use Phrase
				// ------------------------------------------------------------------
				Utilities.SpeakAPhrase (theContext,name + Phrase (theContext,theContext.getString (R.string.out_of_range)));
				// -----------------------------------------------------------------
				// 26/08/2015 ECU pop up toast
				// 23/09/2020 ECU changed to use Phrase
				// -----------------------------------------------------------------
				Utilities.popToast (name + Phrase (theContext,theContext.getString (R.string.out_of_range)),
									true,
									Toast.LENGTH_LONG,
									PublicData.projectFolder + photo);
				// -----------------------------------------------------------------
				// 15/11/2019 ECU Note - indicate that the 'out of range' announcement
				//                       has been made
				// ------------------------------------------------------------------
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
		bluetooth	= StaticData.BLANK_STRING;
		name 		= StaticData.BLANK_STRING;
		phone		= StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void DisplayTasksToPerform (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 02/05/2020 ECU created to display the tasks that are currently set for
		//                this carer
		// -------------------------------------------------------------------------
		Utilities.popToast (Utilities.getRootView (theContext),
							PrintTasks (theContext,theContext.getString (R.string.carer_visit_tasks)),
							theContext.getString (R.string.press_to_clear),
							StaticData.MILLISECONDS_PER_MINUTE,
							true);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public int hashCode ()
	{
		// -------------------------------------------------------------------------
		// 08/09/2017 ECU override the default method so that the hashcode can
		//                accommodate the 'delete' flag
		// -------------------------------------------------------------------------
		return super.hashCode () + (deleted ? 1 : 0);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Phrase (Context theContext,String thePhrase)
	{
		// ------------------------------------------------------------------------
		// 23/09/2020 ECU returns a phrase of the form
		//
		//                <verb depending on 'mulipleCarers'><thePhrase>
		// ------------------------------------------------------------------------
		return StaticData.SPACE_STRING +
					theContext.getString (multipleCarers ? R.string.plural_verb_are
		                                                 : R.string.single_verb_is)
		                       + thePhrase;
		// ------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 14/01/2014 ECU print the agency name
		// -------------------------------------------------------------------------
		return "Name      : " + name + StaticData.NEWLINE +
			   "Phone     : " + phone + StaticData.NEWLINE +
			   "Bluetooth : " + bluetooth + StaticData.NEWLINE +
			   "Agency    : " + PublicData.agencies.get(agencyIndex).name + StaticData.NEWLINE +
			   "Photo     : " + photo;
	}
	// -----------------------------------------------------------------------------
	public String PrintAll ()
	{
		// -------------------------------------------------------------------------
		// 14/01/2014 ECU print the agency name
		// 31/01/2016 ECU added the 'deleted' flag
		// -------------------------------------------------------------------------
		return "Name      : " + name + StaticData.NEWLINE +
			   "Phone     : " + phone + StaticData.NEWLINE +
			   "Bluetooth : " + bluetooth + StaticData.NEWLINE +
			   "Agency    : " + PublicData.agencies.get(agencyIndex).name + StaticData.NEWLINE +
			   "Photo     : " + photo + StaticData.NEWLINE +
			   "Start of Visit : " + PublicData.dateFormatter.format(startOfVisit) + StaticData.NEWLINE +
			   "End   of Visit : " + PublicData.dateFormatter.format(endOfVisit) + StaticData.NEWLINE +
			   "Deleted : " + deleted + StaticData.NEWLINE;
	}
	// =============================================================================
	public String PrintTasks (Context theContext,String theTitle)
	{
		// -------------------------------------------------------------------------
		// 02/05/2020 ECU created to print the tasks
		// -------------------------------------------------------------------------
		String 		localTasks = theTitle + StaticData.NEWLINEx2;
		boolean		localCheck = false;
		// -------------------------------------------------------------------------
		if (tasks != null)
		{
			// ---------------------------------------------------------------------
			// 02/05/2020 ECU loop through the stored tasks
			// ---------------------------------------------------------------------
			for (int index = 0; index < tasks.length; index++)
			{
				// -----------------------------------------------------------------
				// 02/05/2020 ECU check if this task is set
				// -----------------------------------------------------------------
				if (tasks [index])
				{
					// -------------------------------------------------------------
					// 02/05/2020 ECU add this task into the list
					// -------------------------------------------------------------
					localTasks += StaticData.INDENT + PublicData.tasksToDo [index] + StaticData.NEWLINE;
					// -------------------------------------------------------------
					// 02/05/2020 ECU indicate that some tasks have been set
					// -------------------------------------------------------------
					localCheck = true;
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 02/05/2020 ECU check if any tasks have been added
		// -------------------------------------------------------------------------
		if (!localCheck)
		{
			// ---------------------------------------------------------------------
			// 02/05/2020 ECU no tasks have been set for this carer
			// ---------------------------------------------------------------------
			localTasks = theContext.getString (R.string.carer_visit_no_tasks) + StaticData.NEWLINE;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 02/05/2020 ECU return the generated string
		// -------------------------------------------------------------------------
		return localTasks;
		// -------------------------------------------------------------------------
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
	public void TasksMerge (boolean [] theTasks)
	{
		// -------------------------------------------------------------------------
		// 01/05/2020 ECU this method is called when the carer is performing an
		//                unscheduled visit and there are already scheduled visits
		//                for this period. The tasks for all of the scheduled visits
		//                will be merged to be performed on this unscheduled visit
		// -------------------------------------------------------------------------
		// -------------------------------------------------------------------------
		// 01/05/2020 ECU check if the lengths are the same - if so then scan the
		//                supplied array with that stored
		// -------------------------------------------------------------------------
		if (tasks.length == theTasks.length)
		{
			// ---------------------------------------------------------------------
			// 01/05/2020 ECU the arrays are of the same length so doing the merging
			// ---------------------------------------------------------------------
			for (int index = 0; index < tasks.length; index++)
			{
				// -----------------------------------------------------------------
				// 01/05/2020 ECU only interested in adding tasks rather than resetting
				//                any that were set
				// -----------------------------------------------------------------
				if (!tasks [index])
				{
					// --------------------------------------------------------------
					// 01/05/2020 ECU this task is not set so can overwrite with that
					//                supplied
					// --------------------------------------------------------------
					tasks [index] = theTasks [index];
					// --------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String TasksPerformed (String theTitle)
	{
		boolean localTitle = true;
		String  localString = StaticData.BLANK_STRING;
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
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 04/10/2016 ECU no state change so indicate this to the caller
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

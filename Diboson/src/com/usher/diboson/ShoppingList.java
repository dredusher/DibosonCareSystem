package com.usher.diboson;

import java.io.Serializable;
import java.util.List;


public class ShoppingList implements Serializable,Comparable<ShoppingList>
{
	/* ============================================================================= */
	// 29/03/2014 ECU created
	// 13/06/2015 ECU added 'order' which contains the order in which the items are
	//                bought
	// 01/03/2016 ECU IMPORTANT the position of a list in the list is stored elsewhere
	//                ========= in the system so that when a product is deleted then
	//                          the object in the list cannot just be deleted as this
	//                          would invalidate all other objects in the list. So
	//                          introduce the 'deleted' variable which will indicate
	//                          that the brand has been deleted but its object will
	//                          remain with this variable set to true.
	// 08/09/2017 ECU override the hashCode method to accommodate the deleted flag
	//                which the root method was not doing
	// 16/04/2019 ECU if the 'amount', which is a free text field, is blank then
	//                do not print anything out
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	/* ============================================================================= */
	public 	String	amount;
	public	boolean deleted;							// 01/03/2016 ECU added
	public 	int		order;								// 13/06/2015 ECU added
	public 	boolean selected;							// 31/03/2014 ECU added
	public 	int		transactionIndex;
	/* ============================================================================= */
	
	// =============================================================================
	public ShoppingList (int theTransactionIndex,String theAmount)
	{
		amount				= theAmount;
		transactionIndex	= theTransactionIndex;
		
		// -------------------------------------------------------------------------
		// 31/03/2014 ECU default the 'selected' flag
		// -------------------------------------------------------------------------
		selected            = false;
		// -------------------------------------------------------------------------
		// 13/06/2015 ECU initialise the order
		// -------------------------------------------------------------------------
		order				= 0;
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU added the deleted flag and initialise it
		// -------------------------------------------------------------------------
		deleted				= false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void Add (ShoppingList theListObject)
	{
		// -------------------------------------------------------------------------
		// 31/01/2016 ECU created to add a new list object, taking into account the
		//                reuse of 'deleted' objects
		// -------------------------------------------------------------------------
		if (PublicData.shoppingData.lists.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 01/03/2016 ECU scan the existing list to see if there is a deleted
			//                entry
			// ---------------------------------------------------------------------
			for (int theList = 0; theList < PublicData.shoppingData.lists.size(); theList++)
			{
				// -----------------------------------------------------------------
				// 01/03/2016 ECU check if this entry has been deleted
				// -----------------------------------------------------------------
				if (PublicData.shoppingData.lists.get (theList).deleted)
				{
					// -------------------------------------------------------------
					// 31/01/2016 ECU have found a deleted entry so 're-use' it
					// -------------------------------------------------------------
					PublicData.shoppingData.lists.set (theList,theListObject);
					// -------------------------------------------------------------
					// 31/01/2016 ECU just return as no more processing is needed
					// -------------------------------------------------------------
					return;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 01/03/2016 ECU reach here if no deleted object has been found so just
			//                need to add the entry
			// ---------------------------------------------------------------------
			PublicData.shoppingData.lists.add (theListObject);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 01/03/2016 ECU this is an empty list so just add the object
			// ---------------------------------------------------------------------
			PublicData.shoppingData.lists.add (theListObject);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
	@Override
	public int compareTo (ShoppingList shoppingList) 
	{
		// -------------------------------------------------------------------------
		// 13/06/2015 ECU added the ability to sort the list
		// -------------------------------------------------------------------------
		return  this.order - shoppingList.order;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean CheckTransactionIndex (int theTransactionIndex)
	{
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU created to check if the brand, whose index is given, is used
		//                by any of the stored items
		// --------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.shoppingData.lists.size(); theIndex++)
		{
			if (!PublicData.shoppingData.lists.get(theIndex).deleted)
			{
				if (PublicData.shoppingData.lists.get(theIndex).transactionIndex == theTransactionIndex)
					return true;
			}
		}
		return false;
	}
	// =============================================================================
	public String GetShopName ()
	{
		// -------------------------------------------------------------------------
		// 23/04/2018 ECU return the name of the shop where this item is being 
		//                purchased - first step is to get the index.
		// -------------------------------------------------------------------------
		int shopIndex = PublicData.shoppingData.transactions.get (transactionIndex).shopIndex;
		// -------------------------------------------------------------------------
		// 23/04/2018 ECU return the name corresponding to the index
		// -------------------------------------------------------------------------
		return PublicData.shoppingData.shops.get (shopIndex).name;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void Delete ()
	{
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU created to indicate that the object has been deleted
		// -------------------------------------------------------------------------
		deleted 			= true;
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU and invalidate all other relevant fields
		// 09/09/2017 ECU changed to use BLANK_STRING
		// -------------------------------------------------------------------------
		amount 				= StaticData.BLANK_STRING;
		order 				= StaticData.NO_RESULT;				// 13/06/2015 ECU added
		selected 			= false;							// 31/03/2014 ECU added
		transactionIndex	= StaticData.NO_RESULT;
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
	/* ============================================================================= */
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 16/04/2019 ECU change so that if the amount is blank then do not print
		//                anything
		// -------------------------------------------------------------------------
		return PublicData.shoppingData.transactions.get (transactionIndex).Print() + 
				(!Utilities.isStringBlank(amount) ? ("\nAmount : " + amount) : StaticData.BLANK_STRING);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String PrintAll (List<ShoppingList> theList,String theTitleMessage)
	{
		// -------------------------------------------------------------------------
		// 14/06/2015 ECU created to print all the whole of the list
		// -------------------------------------------------------------------------
		String resultString = theTitleMessage + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
		// 23/04/2018 ECU include the order
		// 16/04/2019 ECU change so that if the amount is blank then do not print
		//                anything
		// -------------------------------------------------------------------------
		if (theList != null && theList.size() > 0)
		{
			for (int theIndex = 0; theIndex < theList.size(); theIndex++)
			{
				resultString += PublicData.shoppingData.transactions.get (theList.get(theIndex).transactionIndex).Print() +
						(!Utilities.isStringBlank(theList.get(theIndex).amount) ? (" " + theList.get(theIndex).amount) 
								                                                : StaticData.BLANK_STRING) +
						" " + theList.get(theIndex).selected + StaticData.NEWLINE +
						"Order : " + theList.get(theIndex).order + StaticData.NEWLINE;			
			}
		}
		// -------------------------------------------------------------------------
		// 14/06/2014 ECU return the summary string
		// --------------------------------------------------------------------------
		return resultString;
		// --------------------------------------------------------------------------
	}
	// =============================================================================
	String PrintRecord (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU changed the format
		// -------------------------------------------------------------------------
		return String.format ("Index : %3d  Transaction Index : %3d  Amount : %-20s  Deleted : %b",
										theIndex,transactionIndex,amount,deleted);
		//--------------------------------------------------------------------------						
	}
	// =============================================================================
	public static String PrintAllRecords ()
	{
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU changed the format
		// -------------------------------------------------------------------------
		String printString = "List\n====\n";
		for (int theIndex=0; theIndex < PublicData.shoppingData.lists.size(); theIndex++)
		{
			printString += PublicData.shoppingData.lists.get(theIndex).PrintRecord (theIndex) + StaticData.NEWLINE;
		}
		return printString;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void SetOrder (int theOrder)
	{
		// -------------------------------------------------------------------------
		// 13/06/2015 ECU created to set the order of purchase
		// -------------------------------------------------------------------------
		order = theOrder;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public void setState (boolean theState)
	{
		// -------------------------------------------------------------------------
		// 13/06/2015 ECU created to force the state
		// -------------------------------------------------------------------------
		selected = theState;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static int Size ()
	{
		int localCounter = 0;
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU created to return the size of the list list taking into
		//                account whether an object is deleted or not
		// -------------------------------------------------------------------------
		if (PublicData.shoppingData.lists != null)
		{
			// ---------------------------------------------------------------------
			// 09/09/2017 ECU now loop through the list counting objects which have
			//                not been deleted
			// ---------------------------------------------------------------------
			if (PublicData.shoppingData.lists.size () > 0)
			{
				for (int index = 0; index < PublicData.shoppingData.lists.size(); index++)
				{
					// -------------------------------------------------------------
					// 09/09/2017 ECU only count objects which are not deleted
					// -------------------------------------------------------------
					if (!PublicData.shoppingData.lists.get(index).deleted)
					{
						localCounter++;
					}
					// -------------------------------------------------------------
				}
			}
		}
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU return the number of 'non-deleted' lists or 0 if the list
		//                has not been set yet or there are none
		// -------------------------------------------------------------------------
		return localCounter;
	}
	/* ============================================================================= */
	public boolean ToggleSelected ()
	{
		// -------------------------------------------------------------------------
		// 30/03/2014 ECU toggle the state of the 'selected' flag and return
		//                the new state
		// -------------------------------------------------------------------------
		selected = !selected;
		// -------------------------------------------------------------------------
		return selected;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
}

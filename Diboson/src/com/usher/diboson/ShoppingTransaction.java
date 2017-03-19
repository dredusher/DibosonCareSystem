package com.usher.diboson;

import java.io.Serializable;
import java.util.Arrays;


public class ShoppingTransaction implements Serializable
{
	/* ============================================================================= */
	// 29/03/2014 ECU created
	// 01/03/2016 ECU IMPORTANT the position of a transaction in the list is stored elsewhere
	//                ========= in the system so that when a product is deleted then
	//                          the object in the list cannot just be deleted as this
	//                          would invalidate all other objects in the list. So
	//                          introduce the 'deleted' variable which will indicate
	//                          that the brand has been deleted but its object will
	//                          remain with this variable set to true.
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	
	// =============================================================================
	public 	int			itemIndex;
	public	boolean 	deleted;		// 01/03/2016 ECU added
	public 	int			shopIndex;
	// =============================================================================
	
	/* ============================================================================= */
	public ShoppingTransaction (int theItemIndex,int theShopIndex)
	{
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU added the deleted flag and initialise it
		// -------------------------------------------------------------------------
		deleted			= false;
		// -------------------------------------------------------------------------
		itemIndex	= theItemIndex;
		shopIndex	= theShopIndex;
	}
	// =============================================================================
	public static void Add (ShoppingTransaction theTransactionObject)
	{
		// -------------------------------------------------------------------------
		// 31/01/2016 ECU created to add a new transaction object, taking into account the
		//                reuse of 'deleted' objects
		// -------------------------------------------------------------------------
		if (PublicData.shoppingData.transactions.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 01/03/2016 ECU scan the existing list to see if there is a deleted
			//                entry
			// ---------------------------------------------------------------------
			for (int theTransaction = 0; theTransaction < PublicData.shoppingData.transactions.size(); theTransaction++)
			{
				// -----------------------------------------------------------------
				// 01/03/2016 ECU check if this entry has been deleted
				// -----------------------------------------------------------------
				if (PublicData.shoppingData.transactions.get (theTransaction).deleted)
				{
					// -------------------------------------------------------------
					// 31/01/2016 ECU have found a deleted entry so 're-use' it
					// -------------------------------------------------------------
					PublicData.shoppingData.transactions.set (theTransaction,theTransactionObject);
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
			PublicData.shoppingData.transactions.add (theTransactionObject);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 01/03/2016 ECU this is an empty list so just add the object
			// ---------------------------------------------------------------------
			PublicData.shoppingData.transactions.add (theTransactionObject);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean CheckItemIndex (int theItemIndex)
	{
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU created to check if the brand, whose index is given, is used
		//                by any of the stored items
		// --------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.shoppingData.transactions.size(); theIndex++)
		{
			if (!PublicData.shoppingData.transactions.get(theIndex).deleted)
			{
				if (PublicData.shoppingData.transactions.get(theIndex).itemIndex == theItemIndex)
					return true;
			}
		}
		return false;
	}
	// =============================================================================
	public static boolean CheckShopIndex (int theShopIndex)
	{
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU created to check if the brand, whose index is given, is used
		//                by any of the stored items
		// --------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.shoppingData.transactions.size(); theIndex++)
		{
			if (!PublicData.shoppingData.transactions.get(theIndex).deleted)
			{
				if (PublicData.shoppingData.transactions.get(theIndex).shopIndex == theShopIndex)
					return true;
			}
		}
		return false;
	}
	// =============================================================================
	public void Delete ()
	{
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU created to indicate that the object has been deleted
		// -------------------------------------------------------------------------
		deleted = true;
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU and invalidate all other relevant fields
		// -------------------------------------------------------------------------
		itemIndex		= StaticData.NO_RESULT;
		shopIndex		= StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
	public static int GetIndex (String theName)
	{
		// -------------------------------------------------------------------------
		// 31/03/2014 ECU return the index from the supplied string
		// 01/03/2016 ECU added the 'deleted' check
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.shoppingData.transactions.size(); theIndex++)
		{
			if (!PublicData.shoppingData.transactions.get(theIndex).deleted)
			{
				if (PublicData.shoppingData.transactions.get(theIndex).Print().equalsIgnoreCase(theName))
					return theIndex;
			}
		}
		// -------------------------------------------------------------------------
		// 31/03/2014 ECU indicate no match found
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
	}
	/* ============================================================================= */
	public static int GetIndex (int theItem,int theShop)
	{
		// -------------------------------------------------------------------------
		// 31/03/2014 ECU return the index from the supplied string
		// 01/03/2016 ECU added the 'deleted' check
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.shoppingData.transactions.size(); theIndex++)
		{
			if (!PublicData.shoppingData.transactions.get(theIndex).deleted)
			{
				if (PublicData.shoppingData.transactions.get(theIndex).itemIndex == theItem &&
							PublicData.shoppingData.transactions.get(theIndex).shopIndex == theShop)					
					return theIndex;
			}
		}
		// -------------------------------------------------------------------------
		// 31/03/2014 ECU indicate no match found
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
	}
	/* ============================================================================= */
	public String Print ()
	{
		return PublicData.shoppingData.items.get(itemIndex).Print() + " from " +
				PublicData.shoppingData.shops.get(shopIndex).name;
	}
	// =============================================================================
	String PrintRecord ()
	{
		return "Item Index : " + itemIndex + " Shop Index : " + shopIndex + "  Deleted : " + deleted;
	}
	// =============================================================================
	public static String PrintAll ()
	{
		String printString = "Transaction\n============\n";
		for (int theIndex=0; theIndex < PublicData.shoppingData.transactions.size(); theIndex++)
		{
			printString += "Index : " + theIndex + "\n";
			printString += PublicData.shoppingData.transactions.get(theIndex).PrintRecord () + "\n";
		}
		return printString;
	}
	// =============================================================================
	public static String [] ReturnNames ()
	{
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU return an array with those entries which have not been deleted
		// -------------------------------------------------------------------------
		String [] localNames 	 = new String [PublicData.shoppingData.transactions.size()];
		int		  localNameIndex = 0;
		// -------------------------------------------------------------------------
		// 02/03/2016 ECU build up the list of names taking into account deleted entries
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.shoppingData.transactions.size(); theIndex++)
		{
			if (!PublicData.shoppingData.transactions.get(theIndex).deleted)
			{
				localNames [localNameIndex++] = PublicData.shoppingData.transactions.get(theIndex).Print();
			}
		}
		// -------------------------------------------------------------------------
		// 02/03/2016 ECU return the populated part of the array
		// -------------------------------------------------------------------------
		return Arrays.copyOf (localNames,localNameIndex);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static int Size ()
	{
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU return the size of the array taking into account any
		//                deleted entries
		// -------------------------------------------------------------------------
		int localSize = PublicData.shoppingData.transactions.size ();
		if (localSize == 0)
		{
			return 0;
		}
		else
		{
			int	localCounter = 0;
			for (int theIndex = 0; theIndex < localSize; theIndex++)
			{
				if (!PublicData.shoppingData.transactions.get(theIndex).deleted)
					localCounter++;
			}
			return localCounter;
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

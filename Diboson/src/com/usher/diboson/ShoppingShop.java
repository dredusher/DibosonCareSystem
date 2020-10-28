package com.usher.diboson;

import java.io.Serializable;
import java.util.Arrays;

public class ShoppingShop implements Serializable
{
	/* ============================================================================= */
	// 29/03/2014 ECU created
	// 01/03/2016 ECU IMPORTANT the position of a shop in the list is stored elsewhere
	//                ========= in the system so that when a product is deleted then
	//                          the object in the list cannot just be deleted as this
	//                          would invalidate all other objects in the list. So
	//                          introduce the 'deleted' variable which will indicate
	//                          that the brand has been deleted but its object will
	//                          remain with this variable set to true.
	// 08/09/2017 ECU override the hashCode method to accommodate the deleted flag
	//                which the root method was not doing
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	
	// =============================================================================
	public	boolean 	deleted;		// 01/03/2016 ECU added
			String		name;
	// =============================================================================
	
	/* ============================================================================= */
	public ShoppingShop (String theName)
	{
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU added the deleted flag and initialise it
		// -------------------------------------------------------------------------
		deleted			= false;
		// -------------------------------------------------------------------------
		name = theName;
	}
	// =============================================================================
	public static void Add (ShoppingShop theShopObject)
	{
		// -------------------------------------------------------------------------
		// 31/01/2016 ECU created to add a new shop object, taking into account the
		//                reuse of 'deleted' objects
		// -------------------------------------------------------------------------
		if (PublicData.shoppingData.shops.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 01/03/2016 ECU scan the existing list to see if there is a deleted
			//                entry
			// ---------------------------------------------------------------------
			for (int theShop = 0; theShop < PublicData.shoppingData.shops.size(); theShop++)
			{
				// -----------------------------------------------------------------
				// 01/03/2016 ECU check if this entry has been deleted
				// -----------------------------------------------------------------
				if (PublicData.shoppingData.shops.get (theShop).deleted)
				{
					// -------------------------------------------------------------
					// 31/01/2016 ECU have found a deleted entry so 're-use' it
					// -------------------------------------------------------------
					PublicData.shoppingData.shops.set (theShop,theShopObject);
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
			PublicData.shoppingData.shops.add (theShopObject);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 01/03/2016 ECU this is an empty list so just add the object
			// ---------------------------------------------------------------------
			PublicData.shoppingData.shops.add (theShopObject);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
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
		name		= StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
	public static int GetIndex (String theName)
	{
		// -------------------------------------------------------------------------
		// 31/03/2014 ECU return the index from the supplied string
		// 01/03/2016 ECU put in the check on 'deleted'
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.shoppingData.shops.size(); theIndex++)
		{
			if (!PublicData.shoppingData.shops.get(theIndex).deleted)
			{
				if (PublicData.shoppingData.shops.get(theIndex).name.equalsIgnoreCase(theName))
					return theIndex;
			}
		}
		// -------------------------------------------------------------------------
		// 31/03/2014 ECU indicate no match found
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
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
		return "Name : " + name;
	}
	// =============================================================================
	String PrintRecord (int theIndex)
	{
		return String.format ("Index : %3d  Name %-30s  Deleted : %b",
				theIndex,name,deleted);
	}
	// =============================================================================
	public static String PrintAll ()
	{
		String printString = "Shops\n=====\n";
		for (int theIndex=0; theIndex < PublicData.shoppingData.shops.size(); theIndex++)
		{
			printString += PublicData.shoppingData.shops.get(theIndex).PrintRecord (theIndex) + StaticData.NEWLINE;
		}
		return printString;
	}
	// =============================================================================
	public static String [] ReturnNames ()
	{
		// -------------------------------------------------------------------------
		// 02/03/2016 ECU return an array with those entries which have not been deleted
		// -------------------------------------------------------------------------
		String [] localNames 	 = new String [PublicData.shoppingData.shops.size()];
		int		  localNameIndex = 0;
		// -------------------------------------------------------------------------
		// 02/03/2016 ECU build up the list of names taking into account deleted entries
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.shoppingData.shops.size(); theIndex++)
		{
			if (!PublicData.shoppingData.shops.get(theIndex).deleted)
			{
				localNames [localNameIndex++] = PublicData.shoppingData.shops.get(theIndex).name;
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
		int localCounter = 0;
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU created to return the size of the shop list taking into
		//                account whether an object is deleted or not
		// -------------------------------------------------------------------------
		if (PublicData.shoppingData.shops != null)
		{
			// ---------------------------------------------------------------------
			// 09/09/2017 ECU now loop through the list counting objects which have
			//                not been deleted
			// ---------------------------------------------------------------------
			if (PublicData.shoppingData.shops.size () > 0)
			{
				for (int index = 0; index < PublicData.shoppingData.shops.size(); index++)
				{
					// -------------------------------------------------------------
					// 09/09/2017 ECU only count objects which are not deleted
					// -------------------------------------------------------------
					if (!PublicData.shoppingData.shops.get(index).deleted)
					{
						localCounter++;
					}
					// -------------------------------------------------------------
				}
			}
		}
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU return the number of 'non-deleted' shops or 0 if the list
		//                has not been set yet or there are none
		// -------------------------------------------------------------------------
		return localCounter;
	}
	// =============================================================================
}

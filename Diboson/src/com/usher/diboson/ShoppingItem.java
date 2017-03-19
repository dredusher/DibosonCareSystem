package com.usher.diboson;

import java.io.Serializable;
import java.util.Arrays;

public class ShoppingItem implements Serializable
{
	/* ============================================================================= */
	// 29/03/2014 ECU created
	// 01/03/2016 ECU IMPORTANT the position of a item in the list is stored elsewhere
	//                ========= in the system so that when a product is deleted then
	//                          the object in the list cannot just be deleted as this
	//                          would invalidate all other objects in the list. So
	//                          introduce the 'deleted' variable which will indicate
	//                          that the brand has been deleted but its object will
	//                          remain with this variable set to true.
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
			int			brandIndex;
	public	boolean 	deleted;		// 01/03/2016 ECU added
			int			productIndex;
	/* ============================================================================= */
	public ShoppingItem (int theBrandIndex,int theProductIndex)
	{
		brandIndex		= theBrandIndex;
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU added the deleted flag and initialise it
		// -------------------------------------------------------------------------
		deleted			= false;
		// -------------------------------------------------------------------------
		productIndex	= theProductIndex;
	}
	// =============================================================================
	public static void Add (ShoppingItem theItemObject)
	{
		// -------------------------------------------------------------------------
		// 31/01/2016 ECU created to add a new item object, taking into account the
		//                reuse of 'deleted' objects
		// -------------------------------------------------------------------------
		if (PublicData.shoppingData.items.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 01/03/2016 ECU scan the existing list to see if there is a deleted
			//                entry
			// ---------------------------------------------------------------------
			for (int theItem = 0; theItem < PublicData.shoppingData.items.size(); theItem++)
			{
				// -----------------------------------------------------------------
				// 01/03/2016 ECU check if this entry has been deleted
				// -----------------------------------------------------------------
				if (PublicData.shoppingData.items.get (theItem).deleted)
				{
					// -------------------------------------------------------------
					// 31/01/2016 ECU have found a deleted entry so 're-use' it
					// -------------------------------------------------------------
					PublicData.shoppingData.items.set (theItem,theItemObject);
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
			PublicData.shoppingData.items.add (theItemObject);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 01/03/2016 ECU this is an empty list so just add the object
			// ---------------------------------------------------------------------
			PublicData.shoppingData.items.add (theItemObject);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean CheckBrandIndex (int theBrandIndex)
	{
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU created to check if the brand, whose index is given, is used
		//                by any of the stored items
		// --------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.shoppingData.items.size(); theIndex++)
		{
			if (!PublicData.shoppingData.items.get(theIndex).deleted)
			{
				if (PublicData.shoppingData.items.get(theIndex).brandIndex == theBrandIndex)
					return true;
			}
		}
		return false;
	}
	// =============================================================================
	public static boolean CheckProductIndex (int theProductIndex)
	{
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU created to check if the brand, whose index is given, is used
		//                by any of the stored items
		// --------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.shoppingData.items.size(); theIndex++)
		{
			if (!PublicData.shoppingData.items.get(theIndex).deleted)
			{
				if (PublicData.shoppingData.items.get(theIndex).productIndex == theProductIndex)
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
		brandIndex 		= StaticData.NO_RESULT;
		productIndex 	= StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
	public String Print ()
	{
		return PublicData.shoppingData.products.get(productIndex).name +
				" (" + PublicData.shoppingData.brands.get(brandIndex).name + ")";
	}
	// =============================================================================
	String PrintRecord ()
	{
		return "Brand Index : " + brandIndex + " Product Index : " + productIndex + "  Deleted : " + deleted;
	}
	// =============================================================================
	public static String PrintAll ()
	{
		String printString = "Items\n======\n";
		for (int theIndex=0; theIndex < PublicData.shoppingData.items.size(); theIndex++)
		{
			printString += "Index : " + theIndex + "\n";
			printString += PublicData.shoppingData.items.get(theIndex).PrintRecord () + "\n";
		}
		return printString;
	}
	// =============================================================================
	public static int GetIndex (String theName)
	{
		// -------------------------------------------------------------------------
		// 31/03/2014 ECU return the index from the supplied string
		// 01/03/2016 ECU put in the check on deleted
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.shoppingData.items.size(); theIndex++)
		{
			if (!PublicData.shoppingData.items.get(theIndex).deleted)
			{
				if (PublicData.shoppingData.items.get(theIndex).Print().equalsIgnoreCase(theName))
					return theIndex;
			}
		}
		// -------------------------------------------------------------------------
		// 31/03/2014 ECU indicate no match found
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
	}
	/* ============================================================================= */
	public static int GetIndex (int theBrand,int theProduct)
	{
		// -------------------------------------------------------------------------
		// 31/03/2014 ECU return the index from the supplied string
		// 01/03/2016 ECU put in the check on deleted
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.shoppingData.items.size(); theIndex++)
		{
			if (!PublicData.shoppingData.items.get(theIndex).deleted)
			{
				if (PublicData.shoppingData.items.get(theIndex).brandIndex == theBrand &&
						PublicData.shoppingData.items.get(theIndex).productIndex == theProduct)					
							return theIndex;
			}
		}
		// -------------------------------------------------------------------------
		// 31/03/2014 ECU indicate no match found
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
	}
	// =============================================================================
	public static String [] ReturnNames ()
	{
		// -------------------------------------------------------------------------
		// 02/03/2016 ECU return an array with those entries which have not been deleted
		// -------------------------------------------------------------------------
		String [] localNames 	 = new String [PublicData.shoppingData.items.size()];
		int		  localNameIndex = 0;
		// -------------------------------------------------------------------------
		// 02/03/2016 ECU build up the list of names taking into account deleted entries
		// -------------------------------------------------------------------------
		for (int theIndex = 0; theIndex < PublicData.shoppingData.items.size(); theIndex++)
		{
			if (!PublicData.shoppingData.items.get(theIndex).deleted)
			{
				localNames [localNameIndex++] = PublicData.shoppingData.items.get(theIndex).Print ();
			}
		}
		// -------------------------------------------------------------------------
		// 02/03/2016 ECU return the populated part of the array
		// -------------------------------------------------------------------------
		return Arrays.copyOf (localNames,localNameIndex);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

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
	// 08/09/2017 ECU override the hashCode method to pick up the deleted flag which
	//                the root method was not doing
	// 11/01/2020 ECU added the barcode for this item
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	// =============================================================================
	static boolean invalidateHash = false;
	/* ============================================================================= */
			String	    barcode;				// 11/01/2020 ECU added
			int			brandIndex;
	public	boolean 	deleted;				// 01/03/2016 ECU added
			int			productIndex;
	/* ============================================================================= */
	public ShoppingItem (int theBrandIndex,int theProductIndex,String theBarcode)
	{
		// -------------------------------------------------------------------------
		// 11/01/2020 ECU set the item's barcode
		// -------------------------------------------------------------------------
		barcode			= theBarcode;
		// -------------------------------------------------------------------------
		brandIndex		= theBrandIndex;
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU added the deleted flag and initialise it
		// -------------------------------------------------------------------------
		deleted			= false;
		// -------------------------------------------------------------------------
		productIndex	= theProductIndex;
		// -------------------------------------------------------------------------
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
	public String Barcode ()
	{
		// -------------------------------------------------------------------------
		// 11/01/2020 ECU return the barcode data, with suitable title, if set or a
		//                blank string
		// -------------------------------------------------------------------------
		if (!Utilities.isStringBlank(barcode))
			return "Barcode : " + barcode;
		else
			return StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public boolean BarcodeUpdate (String theNewBarcode)
	{
		// -------------------------------------------------------------------------
		// 11/01/2020 ECU update the barcode if differs from the that stored
		//            ECU returns
		//					   true ...... an update took place
		//                     false ..... no update needed
		// -------------------------------------------------------------------------
		if ((barcode == null) || !barcode.equalsIgnoreCase(theNewBarcode))
		{
			// ---------------------------------------------------------------------
			// 11/01/2020 ECU the new bar code is different
			// ---------------------------------------------------------------------
			barcode = theNewBarcode;
			// ---------------------------------------------------------------------
			// 11/01/2020 ECU try and make sure that the change is reflected in the
			//                hash code
			// ---------------------------------------------------------------------
			invalidateHash = true;
			// ---------------------------------------------------------------------
			// 11/01/2020 ECU indicate that the barcode was updated
			// ---------------------------------------------------------------------
			return true;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 11/01/2020 ECU the barcode was the same so no need to update
			// ---------------------------------------------------------------------
			return false;
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
			// ---------------------------------------------------------------------
			if (!PublicData.shoppingData.items.get(theIndex).deleted)
			{
				if (PublicData.shoppingData.items.get(theIndex).brandIndex == theBrandIndex)
					return true;
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		return false;
		// -------------------------------------------------------------------------
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
		// -------------------------------------------------------------------------
		return false;
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
		brandIndex 		= StaticData.NO_RESULT;
		productIndex 	= StaticData.NO_RESULT;
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
		int hashCode = super.hashCode();
		// -------------------------------------------------------------------------
		// 11/01/2020 ECU the hash method does not always pick up data changes so
		//                force a difference and then reset the flag
		// -------------------------------------------------------------------------
		if (invalidateHash)
		{
			// ---------------------------------------------------------------------
			// 11/01/2020 ECU make sure that the hash code is changed
			// ---------------------------------------------------------------------
			hashCode += 1;
			// ---------------------------------------------------------------------
			// 11/01/2020 ECU reset the flag so that hash code will generated
			//                correctly next time
			// ---------------------------------------------------------------------
			invalidateHash = false;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		return hashCode + (deleted ? 1 : 0);
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
	public String Print ()
	{
		// -------------------------------------------------------------------------
		return PublicData.shoppingData.products.get (productIndex).name +
				" (" + PublicData.shoppingData.brands.get (brandIndex).name + ")";
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	String PrintRecord (int theIndex)
	{
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU changed the format
		// -------------------------------------------------------------------------
		return String.format ("Index : %3d  Brand Index : %3d  ProductIndex : %3d  Deleted : %b",
									theIndex,brandIndex,productIndex,deleted);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String PrintAll ()
	{
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU changed the format
		// -------------------------------------------------------------------------
		String printString = "Items\n======\n";
		for (int theIndex=0; theIndex < PublicData.shoppingData.items.size(); theIndex++)
		{
			printString += PublicData.shoppingData.items.get(theIndex).PrintRecord (theIndex) + StaticData.NEWLINE;
		}
		// -------------------------------------------------------------------------
		return printString;
		// -------------------------------------------------------------------------
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
		// -------------------------------------------------------------------------
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
		// -------------------------------------------------------------------------
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
	public static int Size ()
	{
		int localCounter = 0;
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU created to return the size of the item list taking into
		//                account whether an object is deleted or not
		// -------------------------------------------------------------------------
		if (PublicData.shoppingData.items != null)
		{
			// ---------------------------------------------------------------------
			// 09/09/2017 ECU now loop through the list counting objects which have
			//                not been deleted
			// ---------------------------------------------------------------------
			if (PublicData.shoppingData.items.size () > 0)
			{
				for (int index = 0; index < PublicData.shoppingData.items.size(); index++)
				{
					// -------------------------------------------------------------
					// 09/09/2017 ECU only count objects which are not deleted
					// -------------------------------------------------------------
					if (!PublicData.shoppingData.items.get(index).deleted)
					{
						localCounter++;
					}
					// -------------------------------------------------------------
				}
			}
		}
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU return the number of 'non-deleted' items or 0 if the list
		//                has not been set yet or there are none
		// -------------------------------------------------------------------------
		return localCounter;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ShoppingData implements Serializable
{
	/* ============================================================================= */
	// 29/03/2014 ECU created
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	public List<ShoppingProduct>  		products 		= new ArrayList <ShoppingProduct>();
	public List<ShoppingBrand>  		brands 			= new ArrayList <ShoppingBrand>();
	public List<ShoppingItem>  			items 			= new ArrayList <ShoppingItem>();
	public List<ShoppingList>  			lists 			= new ArrayList <ShoppingList>();
	public List<ShoppingShop>  			shops 	 		= new ArrayList <ShoppingShop>();
	public List<ShoppingTransaction>  	transactions 	= new ArrayList <ShoppingTransaction>();
	// -----------------------------------------------------------------------------
	// 14/06/2015 ECU remember the current shopping list
	// -----------------------------------------------------------------------------
	public List<ShoppingList>  			currentShoppingList	= new ArrayList <ShoppingList>();
	/* ============================================================================= */
	
	// =============================================================================
	public int HashCode ()
	{
		// -------------------------------------------------------------------------
		// 30/03/2016 ECU return a sum of component hashcode's - clearly changes in
		//                hashcode's may cancel out but this is a reasonable first
		//                attempt. Am doing it this way because the hashcode for
		//                'shoppingData' was not reflecting changes to the constituent
		//                lists.
		// -------------------------------------------------------------------------
		try
		{
			int localHashCode = PublicData.shoppingData.products.hashCode() +
								PublicData.shoppingData.brands.hashCode() +
								PublicData.shoppingData.items.hashCode() +
								PublicData.shoppingData.lists.hashCode() +
								PublicData.shoppingData.shops.hashCode() +
								PublicData.shoppingData.transactions.hashCode();
			// ---------------------------------------------------------------------
			// 01/11/2016 ECU add in the current shopping list if it exists
			// ---------------------------------------------------------------------
			if (PublicData.shoppingData.currentShoppingList != null)
				   localHashCode += PublicData.shoppingData.currentShoppingList.hashCode();
			// ---------------------------------------------------------------------
			// 01/11/2016 ECU return the calculated hash code
			// ---------------------------------------------------------------------
			return localHashCode;
			// ---------------------------------------------------------------------
		}
		catch (Exception theException)
		{
			// ---------------------------------------------------------------------
			// 01/11/2016 ECU an error, for some reason, occurred so return an error
			//                condition
			// ---------------------------------------------------------------------
			return StaticData.NO_RESULT;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

package com.usher.diboson;

import java.util.ArrayList;
import java.util.Collections;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

public class ShoppingActivity extends DibosonActivity 
{
	/* ============================================================================= */
	// =============================================================================
	// 29/03/2014 ECU created
	// 05/06/2015 ECU changed fixed strings to use resource strings
	// 14/06/2015 ECU added the 'MainActivity.shoppingData.currentShoppingList'
	// 11/09/2015 ECU added the RESULT_CODE into StaticData
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 02/11/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 29/02/2016 ECU add sorting for the initial display of items
	// 30/03/2016 ECU use hasCodes to determine whether the data has changed and should
	//                be written to disk. Am aware that this is not perfect but good 
	//                first attempt.
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	//==============================================================================
	//private static final String TAG	= "ShoppingActivity";
	/* ============================================================================= */
	
	/* ============================================================================= */
			Button		brands;
			Button		completeList;						// 13/06/2015 ECU added
	static	Context		context;
			int			initialHashCode;					// 30/03/2016 ECU added
	static 	int			itemOrder;							// 13/05/2015 ECU added
			Button  	items;
			Button		list;								// 31/03/2014 ECU added
			Button		lists;								// 30/03/2014 ECU added
			Button		products;
	static 	SelectorParameter 	
						selectorParameter = new SelectorParameter ();
	static 	String		shoppingList = "";					// 01/04/2014 ECU added
			Button  	shops;
			Button		transactions;
	static 	String		whereToShop;						// 29/02/2016 ECU added
	/* ============================================================================= */
	
	/* ============================================================================= */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 02/11/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 28/11/2016 ECU call method to set the screen up
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// ---------------------------------------------------------------------
			setContentView(R.layout.activity_shopping);
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 29/03/2014 ECU set up the buttons
			// 31/03/2014 ECU added 'list'
			// 13/06/2015 ECU added 'completeList'
			// ---------------------------------------------------------------------
			brands			= (Button) findViewById (R.id.button_shopping_brands);
			completeList	= (Button) findViewById (R.id.button_shopping_list_complete);
			items			= (Button) findViewById (R.id.button_shopping_items);
			list			= (Button) findViewById (R.id.button_shopping_list);
			lists			= (Button) findViewById (R.id.button_shopping_lists);
			products		= (Button) findViewById (R.id.button_shopping_products);
			shops			= (Button) findViewById (R.id.button_shopping_shops);
			transactions	= (Button) findViewById (R.id.button_shopping_transactions);
			// ---------------------------------------------------------------------
			brands.setOnClickListener (ButtonListener);
			completeList.setOnClickListener (ButtonListener);
			items.setOnClickListener (ButtonListener);
			list.setOnClickListener (ButtonListener);
			lists.setOnClickListener (ButtonListener);
			products.setOnClickListener (ButtonListener);
			shops.setOnClickListener (ButtonListener);
			transactions.setOnClickListener (ButtonListener);
			// ---------------------------------------------------------------------
			// 30/03/2016 ECU initialise the has code
			// ---------------------------------------------------------------------
			initialHashCode = PublicData.shoppingData.HashCode();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/11/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
    private View.OnClickListener ButtonListener = new View.OnClickListener() 
	{	
		@Override
		public void onClick(View view) 
		{	
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU now process depending on which button pressed
			// ---------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.button_shopping_brands:
				{		
					ShoppingBrands ();
					break;
				}
				// -----------------------------------------------------------------
				case R.id.button_shopping_items:
				{
					ShoppingItems ();						
					break;
				}
				// -----------------------------------------------------------------
				case R.id.button_shopping_list:
				{
					BuildShoppingList (view,getBaseContext());
					break;
				}
				// -----------------------------------------------------------------
				case R.id.button_shopping_list_complete:
				{
					// -------------------------------------------------------------
					// 13/06/2015 ECU added to handle online shopping list completion
					//            ECU initialise the purchase order
					// -------------------------------------------------------------
					if (PublicData.shoppingData.currentShoppingList == null)
					{
						// ---------------------------------------------------------
						PublicData.shoppingData.currentShoppingList = new ArrayList <ShoppingList>();
						// ---------------------------------------------------------
						// 14/06/2015 ECU copy across the selected items from the list
						// ----------------------------------------------------------
						if (PublicData.shoppingData.lists != null && PublicData.shoppingData.lists.size() > 0)
						{
							for (int theIndex = 0; theIndex < PublicData.shoppingData.lists.size(); theIndex++)
							{
								// -------------------------------------------------
								// 14/06/2015 ECU build up a current shopping list 
								//                from selected items
								// -------------------------------------------------
								if (PublicData.shoppingData.lists.get(theIndex).selected)
								{
									// ---------------------------------------------
									// 14/06 2015 ECU generate a new shopping list entry
									//                from the existing list and add
									//                to the list being generated
									//            ECU NOTE : originally did
									//                   .add (PublicData.shoppingData.lists.get(theIndex)
									//                but this was causing problems so
									//                changed to the current version
									// ----------------------------------------------
									PublicData.shoppingData.currentShoppingList.add (new ShoppingList (PublicData.shoppingData.lists.get(theIndex).transactionIndex,
																									   PublicData.shoppingData.lists.get(theIndex).amount));
								}
							}
						}
					}
					// -------------------------------------------------------------
					if (PublicData.shoppingData.currentShoppingList != null && 
						PublicData.shoppingData.currentShoppingList.size() > 0)
					{
						for (int theIndex = 0; theIndex < PublicData.shoppingData.currentShoppingList.size(); theIndex++)
						{
							// --------------------------------------------------
							// 14/06/2015 ECU preset some variables
							// --------------------------------------------------
							PublicData.shoppingData.currentShoppingList.get(theIndex).SetOrder (StaticData.NO_RESULT);
							PublicData.shoppingData.currentShoppingList.get(theIndex).setState (false);
							// --------------------------------------------------
						}
					}
					// -------------------------------------------------------------
					// 29/02/2016 ECU default the shop where items are to be bought
					// -------------------------------------------------------------
					whereToShop = "";
					// -------------------------------------------------------------
					ShoppingList ();
					// -------------------------------------------------------------
					break;
				}
				// -----------------------------------------------------------------
				case R.id.button_shopping_lists:
				{
					// -------------------------------------------------------------
					// 14/06/2015 ECU reset the 'currentShoppingList' so that it
					//                gets rebuilt
					// -------------------------------------------------------------
					PublicData.shoppingData.currentShoppingList = null;
					// -------------------------------------------------------------
					ShoppingLists ();						
					break;
				}
				// -----------------------------------------------------------------
				case R.id.button_shopping_products:
				{
					ShoppingProducts ();						
					break;
				}
				// -----------------------------------------------------------------
				case R.id.button_shopping_shops:
				{
					ShoppingShops ();						
					break;
				}
				// -----------------------------------------------------------------
				case R.id.button_shopping_transactions:
				{
					ShoppingTransactions ();
					break;
				}
				// -----------------------------------------------------------------
			}
		}
	};
	/* ============================================================================= */
    public void onDestroy()
    {	
    	// -------------------------------------------------------------------------
    	// 29/03/2014 ECU write all shopping data to disk
    	// 03/04/2014 ECU changed to use 'AsyncUtilities' rather than 'Utilities'
    	// 30/03/2016 ECU use the hash code to determine if data is to be updated
    	// 01/11/2016 ECU put in the null check
    	// -------------------------------------------------------------------------
    	if (PublicData.shoppingData != null && initialHashCode != PublicData.shoppingData.HashCode())
    	{
    		// ---------------------------------------------------------------------
    		// 30/03/2016 ECU it appears that the data has changed so update the disk
    		// ----------------------------------------------------------------------
    		AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + 
    				getBaseContext().getString (R.string.shopping_file),PublicData.shoppingData);
    	}
		// --------------------------------------------------------------------------
		
		super.onDestroy();
    }
    /* ============================================================================= */
    @Override
	public void onActivityResult(int theRequestCode, int theResultCode, Intent theIntent) 
	{
    	// -------------------------------------------------------------------------
		// 01/04/2014 ECU called when an activity returns a result
    	// 11/09/2015 ECU changed to used result code in StaticData
    	// -------------------------------------------------------------------------
		
	    if (theRequestCode == StaticData.RESULT_CODE_SHOPPING)
	    {
	    	if (theResultCode == RESULT_OK)
	    	{	    		
	    		// Returned OK
	    	}
	    	else 
	 	    if (theResultCode == RESULT_CANCELED) 
	 	    {
	 	       // Handle cancel
	 	    }
	    }   
	}
    /* ============================================================================= */
    public static void AddBrand (int thePosition)
    {
    	Intent intent = new Intent (context,ShoppingInputActivity.class);
    	intent.putExtra(StaticData.PARAMETER_TYPE,ShoppingInputActivity.TYPE_BRAND);
		context.startActivity (intent);
    }
    /* ============================================================================= */
    public static void AddItem (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 10/06/2015 ECU an item is made up of a brand and product - if either is
    	//                missing then cannot do anything
    	// -------------------------------------------------------------------------
    	if (PublicData.shoppingData.brands.size() > 0 &&
    		PublicData.shoppingData.products.size() > 0)
    	{
    		Intent intent = new Intent (context,ShoppingInputActivity.class);
    		intent.putExtra(StaticData.PARAMETER_TYPE,ShoppingInputActivity.TYPE_ITEM);
			context.startActivity (intent);
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 10/06/2015 ECU indicate why am unable to start activity
    		// ---------------------------------------------------------------------
    		Utilities.popToast (context.getString(R.string.unable_to_create_item),true);
    		// ---------------------------------------------------------------------
    	}
    }
    /* ============================================================================= */
    public static void AddList (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 10/06/2015 ECU a list needs transactions - if transactions are
    	//                missing then cannot do anything
    	// -------------------------------------------------------------------------
    	if (PublicData.shoppingData.transactions.size() > 0)
    	{
    		Intent intent = new Intent (context,ShoppingInputActivity.class);
    		intent.putExtra (StaticData.PARAMETER_TYPE,ShoppingInputActivity.TYPE_LIST);
    		context.startActivity (intent);
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 10/06/2015 ECU indicate why am unable to start activity
    		// ---------------------------------------------------------------------
    		Utilities.popToast (context.getString (R.string.unable_to_create_list),true);
    		// ---------------------------------------------------------------------
    	}
    }
    /* ============================================================================= */
    public static void AddProduct (int thePosition)
    {
    	Intent intent = new Intent (context,ShoppingInputActivity.class);
    	intent.putExtra (StaticData.PARAMETER_TYPE,ShoppingInputActivity.TYPE_PRODUCT);
		context.startActivity (intent);
    }
    /* ============================================================================= */
    public static void AddShop (int thePosition)
    {
    	Intent intent = new Intent (context,ShoppingInputActivity.class);
    	intent.putExtra (StaticData.PARAMETER_TYPE,ShoppingInputActivity.TYPE_SHOP);
		context.startActivity (intent);
    }
    /* ============================================================================= */
    public static void AddTransaction (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 10/06/2015 ECU an transaction is made up of an item and shop - if either is
    	//                missing then cannot do anything
    	// -------------------------------------------------------------------------
    	if (PublicData.shoppingData.items.size() > 0 &&
    		PublicData.shoppingData.shops.size() > 0)
    	{
    		Intent intent = new Intent (context,ShoppingInputActivity.class);
    		intent.putExtra (StaticData.PARAMETER_TYPE,ShoppingInputActivity.TYPE_TRANSACTION);
			context.startActivity (intent);
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 10/06/2015 ECU indicate why am unable to start activity
    		// ---------------------------------------------------------------------
    		Utilities.popToast (context.getString (R.string.unable_to_create_transaction),true);
    		// ---------------------------------------------------------------------
    	}
    }
    /* ============================================================================= */
    static void BuildShoppingList (View view,Context theContext)
	{
    	int 		itemIndex;  
    	boolean 	shopAdded = false;
     	int 		transactionIndex;
     	
     	// -------------------------------------------------------------------------
     	// 29/02/2016 ECU clear the message that will be build
     	// -------------------------------------------------------------------------
     	shoppingList = "";
     	// -------------------------------------------------------------------------
     	// 29/02/2016 ECU check if there is a current shopping list if so then use
     	//                that one
     	// -------------------------------------------------------------------------
     	if ((PublicData.shoppingData.currentShoppingList == null) ||
     		(PublicData.shoppingData.currentShoppingList.size() == 0))
     	{
     		// ---------------------------------------------------------------------
     		// 29/02/2016 ECU no shopping list has been defined
     		// ---------------------------------------------------------------------
     		// 27/11/2014 ECU add in the check on size
     		// ---------------------------------------------------------------------  
     		if (PublicData.shoppingData.shops.size() > 0)
     		{
     			for (int theShop = 0; theShop < PublicData.shoppingData.shops.size(); theShop++)
     			{
     				shopAdded = false;
			
     				for (int theList = 0; theList < PublicData.shoppingData.lists.size(); theList++)
     				{
     					if (PublicData.shoppingData.lists.get(theList).selected)
     					{					
     						if (PublicData.shoppingData.transactions.get (PublicData.shoppingData.lists.get(theList).transactionIndex).shopIndex == theShop)
     						{
     							if (!shopAdded)
     							{
     								shopAdded = true;
							
     								shoppingList += String.format (context.getString(R.string.shop_info_format), 
     																PublicData.shoppingData.shops.get (theShop).name);
     							}
     							// -------------------------------------------------
     							// 29/02/2016 ECU Note - does the details of the 
     							//                transaction
     							// -------------------------------------------------
     							transactionIndex = PublicData.shoppingData.lists.get(theList).transactionIndex;
     							itemIndex        = PublicData.shoppingData.transactions.get(transactionIndex).itemIndex;
						
     							shoppingList += String.format("%-40s",PublicData.shoppingData.items.get(itemIndex).Print()) + 
     															PublicData.shoppingData.lists.get(theList).amount + "\n";
     							// -------------------------------------------------
     						}
     					}
     				}
     			}
     		}
     	}
     	else
     	{
     		// ---------------------------------------------------------------------
     		// 29/02/2016 ECU there is a current shopping list
     		// ---------------------------------------------------------------------
     		shoppingList += context.getString (R.string.generated_from_shopping_list);
     		// ---------------------------------------------------------------------
     		// 29/02/2016 ECU default the shop name
     		// ---------------------------------------------------------------------
     		whereToShop = "";
     		String localShopName;
     		// ---------------------------------------------------------------------
     		for (int theIndex = 0; theIndex < PublicData.shoppingData.currentShoppingList.size(); theIndex++)
     		{
     			transactionIndex = PublicData.shoppingData.currentShoppingList.get(theIndex).transactionIndex;
     			itemIndex        = PublicData.shoppingData.transactions.get(transactionIndex).itemIndex;
     			// -----------------------------------------------------------------
     			// 29/02/2016 ECU check if need to display the changed shop name
     			// -----------------------------------------------------------------
     			localShopName = PublicData.shoppingData.shops.get(PublicData.shoppingData.transactions.get(transactionIndex).shopIndex).name;
     			if (!localShopName.equalsIgnoreCase (whereToShop))
     			{
     				whereToShop = localShopName;
     				shoppingList += String.format (context.getString(R.string.shop_info_format),whereToShop);
     			}
     			// -----------------------------------------------------------------
     			shoppingList += String.format("%-40s",PublicData.shoppingData.items.get (itemIndex).Print()) + 
										PublicData.shoppingData.currentShoppingList.get (theIndex).amount + "\n";
     			// -----------------------------------------------------------------
     		}
     	}
    	// -------------------------------------------------------------------------
    	// 29/02/2016 ECU check if a shopping list has been generated
    	// -------------------------------------------------------------------------
     	if (!shoppingList.equalsIgnoreCase (""))
     	{
		
     		// ---------------------------------------------------------------------
     		// 01/04/2014 ECU check whether the shopping list is to be emailed
     		// ---------------------------------------------------------------------
     		Utilities.popToast (view,
     							context.getString (R.string.email_shopping_list),
     							Utilities.createAMethod (ShoppingActivity.class,"EmailShoppingList",0),null);
     		// ---------------------------------------------------------------------
     	}
     	else
     	{
     		// ---------------------------------------------------------------------
     		// 29/02/2016 ECU a shopping list has not been defined yet
     		// ---------------------------------------------------------------------
     		Utilities.popToastAndSpeak (context.getString (R.string.no_shopping_list),true);
     		// ---------------------------------------------------------------------
     	}
	}
    /* ============================================================================= */
    static void BuildTheBrandsList ()
    {
    	selectorParameter.listItems = new ArrayList<ListItem>();
    	
    	// -------------------------------------------------------------------------
     	// 27/11/2014 ECU add in the check on size
    	// 01/03/2016 ECU change to exclude deleted items
     	// -------------------------------------------------------------------------  
     	if (PublicData.shoppingData.brands.size() > 0)
    	for (int theIndex = 0; theIndex < PublicData.shoppingData.brands.size(); theIndex++)
    	{
    		if (!PublicData.shoppingData.brands.get (theIndex).deleted)
			{
    			// -----------------------------------------------------------------
    			// 30/03/2014 ECU added the index as an argument
    			// -----------------------------------------------------------------
    			selectorParameter.listItems.add (new ListItem ("",
    											 PublicData.shoppingData.brands.get(theIndex).name,
    											 "",
    											 "",
    											 theIndex));
			}
    	}	
	}
    /* ============================================================================= */
    static void BuildTheItemsList ()
    {
    	selectorParameter.listItems = new ArrayList<ListItem>();
    	// -------------------------------------------------------------------------
     	// 27/11/2014 ECU add in the check on size
    	// 01/03/2016 ECU change to exclude deleted items
     	// -------------------------------------------------------------------------  
     	if (PublicData.shoppingData.items.size() > 0)
    	for (int theIndex = 0; theIndex < PublicData.shoppingData.items.size(); theIndex++)
    	{
    		if (!PublicData.shoppingData.items.get (theIndex).deleted)
			{
    			// -----------------------------------------------------------------
    			// 30/03/2014 ECU added the index as an argument
    			// -----------------------------------------------------------------
    			selectorParameter.listItems.add (new ListItem ("",
												 PublicData.shoppingData.items.get(theIndex).Print(),
												 "",
												 "",
												 theIndex));
			}
    	}	
	}
    // =============================================================================
    static void BuildTheListList ()
    {
    	// -------------------------------------------------------------------------
    	// 13/06/2015 ECU created to build the shopping list for completion from
    	//                the device
    	// 14/06/2015 ECU change to use 'PublicData.shoppingData.currentShoppingList'
    	// -------------------------------------------------------------------------
    	Collections.sort (PublicData.shoppingData.currentShoppingList);
    	// -------------------------------------------------------------------------
    	selectorParameter.listItems = new ArrayList<ListItem> ();
    	// -------------------------------------------------------------------------
    	// 13/06/2015 ECU build up the list
    	// -------------------------------------------------------------------------
    	if (PublicData.shoppingData.currentShoppingList.size () > 0)
    	{
    		for (int theIndex = 0; theIndex < PublicData.shoppingData.currentShoppingList.size(); theIndex++)
        	{
        		// ---------------------------------------------------------------------
        		// 30/03/2014 ECU added the index as an argument
        		// 31/03/2014 ECU include the current 'selected' state
        		// ---------------------------------------------------------------------
    			ShoppingList localShoppingList = PublicData.shoppingData.currentShoppingList.get (theIndex);
    			// ---------------------------------------------------------------------
        		selectorParameter.listItems.add (new ListItem ("",
    						PublicData.shoppingData.shops.get (PublicData.shoppingData.transactions.get (localShoppingList.transactionIndex).shopIndex).name,
    						PublicData.shoppingData.items.get (PublicData.shoppingData.transactions.get (localShoppingList.transactionIndex).itemIndex).Print(),
    						localShoppingList.amount,
    						theIndex,
    						PublicData.shoppingData.currentShoppingList.get(theIndex).selected));
        	}
    		// ---------------------------------------------------------------------
    		// 13/06/2015 ECU indicate sorting not wanted
    		// ---------------------------------------------------------------------
    		selectorParameter.sort = false;
    		// ---------------------------------------------------------------------
    		// 29/02/2016 ECU now indicate which item is to be bought next
    		// ---------------------------------------------------------------------
    		SpeakItemToBuy ();
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    static void BuildTheListsList ()
    {
    	selectorParameter.listItems = new ArrayList<ListItem>();
    	// -------------------------------------------------------------------------
     	// 27/11/2014 ECU add in the check on size
    	// 01/03/2016 ECU change to exclude deleted items
     	// -------------------------------------------------------------------------  
     	if (PublicData.shoppingData.lists.size() > 0)
    	for (int theIndex = 0; theIndex < PublicData.shoppingData.lists.size(); theIndex++)
    	{
    		if (!PublicData.shoppingData.lists.get (theIndex).deleted)
			{
    			// -----------------------------------------------------------------
    			// 30/03/2014 ECU added the index as an argument
    			// 31/03/2014 ECU include the current 'selected' state
    			// -----------------------------------------------------------------
    			selectorParameter.listItems.add (new ListItem ("",
    											 PublicData.shoppingData.lists.get(theIndex).Print(),
    											 "",
    											 "",
    											 theIndex,
    											 PublicData.shoppingData.lists.get(theIndex).selected));
    			// -----------------------------------------------------------------
			}
    	}	
	}
    /* ============================================================================= */
    static void BuildTheProductsList ()
    {
    	selectorParameter.listItems = new ArrayList<ListItem>();
    	// -------------------------------------------------------------------------
     	// 27/11/2014 ECU add in the check on size
    	// 01/03/2016 ECU change to exclude deleted items
     	// -------------------------------------------------------------------------  
     	if (PublicData.shoppingData.products.size() > 0)
    	for (int theIndex = 0; theIndex < PublicData.shoppingData.products.size(); theIndex++)
    	{
    		if (!PublicData.shoppingData.products.get (theIndex).deleted)
			{
    			// ---------------------------------------------------------------------
    			// 30/03/2014 ECU added the index as an argument
    			// ---------------------------------------------------------------------
    			selectorParameter.listItems.add (new ListItem ("",
												 PublicData.shoppingData.products.get(theIndex).name,
												 "",
												 "",
												 theIndex));
			}
    	}
	}
    /* ============================================================================= */
   static void BuildTheShopsList ()
    {
    	selectorParameter.listItems = new ArrayList<ListItem>();
    	// -------------------------------------------------------------------------
     	// 27/11/2014 ECU add in the check on size
    	// 01/03/2016 ECU change to exclude deleted items
     	// -------------------------------------------------------------------------  
     	if (PublicData.shoppingData.shops.size() > 0)
    	for (int theIndex = 0; theIndex < PublicData.shoppingData.shops.size(); theIndex++)
    	{
    		if (!PublicData.shoppingData.shops.get (theIndex).deleted)
			{
    			// -----------------------------------------------------------------
    			// 30/03/2014 ECU added the index as an argument
    			// -----------------------------------------------------------------
    			selectorParameter.listItems.add (new ListItem ("",
    											 PublicData.shoppingData.shops.get(theIndex).name,
    											 "",
    											 "",
    											 theIndex));
			}
    	}
     	// -------------------------------------------------------------------------
	}
    /* ============================================================================= */
    static void BuildTheTransactionsList ()
    {
    	selectorParameter.listItems = new ArrayList<ListItem>();
    	// -------------------------------------------------------------------------
     	// 27/11/2014 ECU add in the check on size
    	// 01/03/2016 ECU change to exclude deleted items
     	// -------------------------------------------------------------------------  
     	if (PublicData.shoppingData.transactions.size() > 0)
    	for (int theIndex = 0; theIndex < PublicData.shoppingData.transactions.size(); theIndex++)
    	{
    		if (!PublicData.shoppingData.transactions.get (theIndex).deleted)
			{
    			// -----------------------------------------------------------------
    			// 30/03/2014 ECU added the index as an argument
    			// -----------------------------------------------------------------
    			selectorParameter.listItems.add (new ListItem ("",
    											 PublicData.shoppingData.transactions.get(theIndex).Print(),
    											 "",
    											 "",
    											 theIndex));
			}
    	}	
	}
    /* ============================================================================= */
    public static void BuyItem (int thePosition)
    {
    	if (!PublicData.shoppingData.currentShoppingList.get(thePosition).selected)
    	{
    		// ---------------------------------------------------------------------
    		// 13/06/2015 ECU created to handle the purchase of an item
    		// ---------------------------------------------------------------------
    		PublicData.shoppingData.currentShoppingList.get(thePosition).ToggleSelected();
    		// ---------------------------------------------------------------------
    		// 13/06/2015 ECU increment the 'order' in which the item was bought
    		// ---------------------------------------------------------------------
    		PublicData.shoppingData.currentShoppingList.get(thePosition).SetOrder (itemOrder++);
    		// ---------------------------------------------------------------------
    		// 13/06/2015 ECU try and rebuild the data
    		// ---------------------------------------------------------------------
    		Selector.Rebuild ();
    		// ---------------------------------------------------------------------
    		// 14/06/2015 ECU check if everything has been bought
    		// ---------------------------------------------------------------------
    		boolean allBought = true;
    		for (int theIndex = 0; theIndex < PublicData.shoppingData.currentShoppingList.size(); theIndex++)
    		{
    			if (!PublicData.shoppingData.currentShoppingList.get(theIndex).selected)
    			{
    				// -------------------------------------------------------------
    				// 14/06/2015 ECU found an 'unbought' item
    				// -------------------------------------------------------------
    				allBought = false;
    				// -------------------------------------------------------------
    				break;
    			}
    		}
    		// ---------------------------------------------------------------------
    		//14/06/2015 ECU if all items have been bought then tell the user
    		// ---------------------------------------------------------------------
    		if (allBought)
    			Utilities.popToastAndSpeak (context.getString(R.string.all_items_bought));
    		// ---------------------------------------------------------------------
    	}
    }
    // =============================================================================
    public static void EmailShoppingList (int theList)
    {
    	// -------------------------------------------------------------------------
    	// 01/04/2014 ECU confirm the action to the user
    	// 08/03/2016 ECU change to use resource
    	// -------------------------------------------------------------------------
    	Utilities.popToast (context.getString (R.string.emailing_shopping_list) + PublicData.emailDetails.recipients,true);
    	// -------------------------------------------------------------------------
    	// 01/04/2014 ECU send the generated shopping list by email
    	// -------------------------------------------------------------------------	
    	Utilities.SendEmailMessage (context, context.getString (R.string.shopping_list_subject),shoppingList,true);	
    }
    /* ============================================================================= */
    public static void ImageHandler (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 31/03/2014 ECU toggle the 'selected' entry
    	// -------------------------------------------------------------------------
    	PublicData.shoppingData.lists.get(thePosition).ToggleSelected();
    	// -------------------------------------------------------------------------
    	// 14/06/2015 ECU rebuild the displayed list
    	// -------------------------------------------------------------------------
    	Selector.Rebuild();
    	// -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public static void ListSelector (int thePosition)
    {	
    }
    /* ============================================================================= */
    public static ArrayList<ListItem> RebuildList ()
    {
    	// -------------------------------------------------------------------------
    	// 01/04/2014 ECU rebuild the list that is used by the array adapter - the
    	//                list is stored in 'selectorParameter' as 'listItems'
    	// 13/06/2015 ECU added ..COMPLETE_LIST
    	// -------------------------------------------------------------------------
    	switch (selectorParameter.type)
    	{
    		//	--------------------------------------------------------------------
    		case ShoppingInputActivity.TYPE_BRAND:
    			BuildTheBrandsList ();
    			break;
    		// ---------------------------------------------------------------------
    		case ShoppingInputActivity.TYPE_COMPLETE_LIST:
    			// -----------------------------------------------------------------
    			// 13/06/2015 ECU created to handle the completion of the shopping
    			//                list in the device
    			// -----------------------------------------------------------------
    			BuildTheListList ();
    			break;
    		//	--------------------------------------------------------------------
    		case ShoppingInputActivity.TYPE_ITEM:
    			BuildTheItemsList ();
    			break;
    		// 	--------------------------------------------------------------------
    		case ShoppingInputActivity.TYPE_LIST:
    			BuildTheListsList ();
    			break;
    		//	--------------------------------------------------------------------
    		case ShoppingInputActivity.TYPE_PRODUCT:
    			BuildTheProductsList ();
    			break;
    		// --------------------------------------------------------------------
    		case ShoppingInputActivity.TYPE_SHOP:
    			BuildTheShopsList ();
    			break;
    		// --------------------------------------------------------------------
    		case ShoppingInputActivity.TYPE_TRANSACTION:
    			BuildTheTransactionsList ();
    			break;
    		// --------------------------------------------------------------------
    	}
    	
    	// ------------------------------------------------------------------------
    	// 01/04/2014 ECU sort the list before returning that list
    	// ------------------------------------------------------------------------
    	Collections.sort (selectorParameter.listItems);	
    	return selectorParameter.listItems;
    	// ------------------------------------------------------------------------
    }
    // =============================================================================
    public static void SelectItem (int theItem)
    {
    	// -------------------------------------------------------------------------
    	// 08/03/2016 ECU created to handle the selection of an item when completing
    	//                the shopping list
    	//            ECU changed to use resources
    	// --------------------------------------------------------------------------
    	Utilities.SpeakAPhrase (context,
								String.format(context.getString (R.string.item_is_format),
												selectorParameter.listItems.get(theItem).summary, 
												selectorParameter.listItems.get(theItem).extras));
    	// --------------------------------------------------------------------------
    }
    // ============================================================================= 
	void ShoppingBrands ()
	{
		if (PublicData.shoppingData.brands != null && PublicData.shoppingData.brands.size() > 0)
		{
			BuildTheBrandsList ();
			selectorParameter.rowLayout 				= R.layout.dose_time_row;
			selectorParameter.customMethodDefinition 	= new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"AddBrand");
			selectorParameter.customLegend 				= getString (R.string.add);
			selectorParameter.classToRun 				= ShoppingInputActivity.class;
			selectorParameter.type 						= ShoppingInputActivity.TYPE_BRAND;
			// ---------------------------------------------------------------------
			// 08/03/2016 ECU make sure that no selection required
			//----------------------------------------------------------------------
			selectorParameter.selectMethodDefinition	= null;
			// ---------------------------------------------------------------------
			// 29/02/2016 ECU added the sort option
			// ---------------------------------------------------------------------
			selectorParameter.sort						= true;
			// ---------------------------------------------------------------------
			StartSelector ();
		}
		else
		{
			AddBrand (0);
		}		
	}
	/* ============================================================================= */
	void ShoppingItems ()
	{
		if (PublicData.shoppingData.items != null && PublicData.shoppingData.items.size() > 0)
		{
			BuildTheItemsList ();
			selectorParameter.rowLayout 				=  R.layout.dose_time_row;
			selectorParameter.customMethodDefinition	= new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"AddItem");
			selectorParameter.customLegend 				= getString (R.string.add);
			selectorParameter.classToRun 				= ShoppingInputActivity.class;
			selectorParameter.type 						= ShoppingInputActivity.TYPE_ITEM;
			// ---------------------------------------------------------------------
			// 08/03/2016 ECU make sure that no selection required
			//----------------------------------------------------------------------
			selectorParameter.selectMethodDefinition	= null;
			// ---------------------------------------------------------------------
			// 29/02/2016 ECU added the sort option
			// ---------------------------------------------------------------------
			selectorParameter.sort						= true;
			// ---------------------------------------------------------------------
			StartSelector ();
	
		}
		else
		{
			AddItem (0);
		}
	}
	/* ============================================================================= */
	void ShoppingList ()
	{
		// -------------------------------------------------------------------------
		// 13/06/2015 ECU created to handle the online completion of the shopping list
		//            ECU check that a list to complete exists
		// -------------------------------------------------------------------------
		if (PublicData.shoppingData.currentShoppingList != null && PublicData.shoppingData.currentShoppingList.size() > 0)
		{
			// ---------------------------------------------------------------------
			// 13/06/2015 ECU initialise the order when the item was bought
			// ---------------------------------------------------------------------
			itemOrder = 0;
			// ---------------------------------------------------------------------
			BuildTheListList ();
			selectorParameter.rowLayout 				= R.layout.selector_complete_list_row;
			selectorParameter.classToRun 				= null;
			selectorParameter.type 						= ShoppingInputActivity.TYPE_COMPLETE_LIST;
			// ---------------------------------------------------------------------
			// 08/03/2016 ECU declare the method to be called when the item is clicked 
			//                and indicate that the select activity must not finish
			//                when the item is selected
			// ---------------------------------------------------------------------
			selectorParameter.finishOnSelect			= false;
			selectorParameter.selectMethodDefinition 	= new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"SelectItem");
			// ---------------------------------------------------------------------
			// 13/06/2015 ECU purchase of an item occurs when the image is pressed
			// ---------------------------------------------------------------------
			StartSelector (null,new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"BuyItem"));
			// ----------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 29/02/2016 ECU a shopping list has not been defined yet
			// ---------------------------------------------------------------------
			Utilities.popToastAndSpeak (context.getString (R.string.no_shopping_list),true);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void ShoppingLists ()
	{
		// -------------------------------------------------------------------------
		// 01/03/2016 ECU changed to use the new Size method
		// -------------------------------------------------------------------------
		if (PublicData.shoppingData.lists != null && ShoppingList.Size() > 0)
		{
			BuildTheListsList ();
			selectorParameter.rowLayout 				= R.layout.selector_list_row;
			selectorParameter.customMethodDefinition	= new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"AddList");
			selectorParameter.customLegend 				= getString (R.string.add);
			selectorParameter.classToRun 				= ShoppingInputActivity.class;
			selectorParameter.type 						= ShoppingInputActivity.TYPE_LIST;
			// ---------------------------------------------------------------------
			// 08/03/2016 ECU make sure that no selection required
			//----------------------------------------------------------------------
			selectorParameter.selectMethodDefinition	= null;
			// ---------------------------------------------------------------------
			// 29/02/2016 ECU added the sort option
			// ---------------------------------------------------------------------
			selectorParameter.sort						= true;
			// ---------------------------------------------------------------------
			// 29/02/2016 ECU took out the method definition to ListSelector because
			//                do not want clicking on the item to have any effect
			//				  removed ... new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"ListSelector")
			// ---------------------------------------------------------------------
			StartSelector (); 
			// ---------------------------------------------------------------------
		}
		else
		{
			AddList (0);
		}
	}
	/* ============================================================================= */
	void ShoppingProducts ()
	{
		if (PublicData.shoppingData.products != null && PublicData.shoppingData.products.size() > 0)
		{
			BuildTheProductsList ();
			selectorParameter.rowLayout 				= R.layout.dose_time_row;
			selectorParameter.customMethodDefinition	= new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"AddProduct");
			selectorParameter.customLegend 				= getString (R.string.add);
			selectorParameter.classToRun 				= ShoppingInputActivity.class;
			selectorParameter.type 						= ShoppingInputActivity.TYPE_PRODUCT;
			// ---------------------------------------------------------------------
			// 08/03/2016 ECU make sure that no selection required
			//----------------------------------------------------------------------
			selectorParameter.selectMethodDefinition	= null;
			// ---------------------------------------------------------------------
			// 29/02/2016 ECU added the sort option
			// ---------------------------------------------------------------------
			selectorParameter.sort						= true;
			// ---------------------------------------------------------------------
			StartSelector ();
		}
		else
		{
			AddProduct (0);
		}		
	}
	/* ============================================================================= */
	void ShoppingShops ()
	{
		if (PublicData.shoppingData.shops != null && PublicData.shoppingData.shops.size() > 0)
		{
			BuildTheShopsList ();
			selectorParameter.rowLayout 				=  R.layout.dose_time_row;
			selectorParameter.customMethodDefinition 	= new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"AddShop");
			selectorParameter.customLegend 				= getString (R.string.add);
			selectorParameter.classToRun 				= ShoppingInputActivity.class;
			selectorParameter.type 						= ShoppingInputActivity.TYPE_SHOP;
			// ---------------------------------------------------------------------
			// 08/03/2016 ECU make sure that no selection required
			//----------------------------------------------------------------------
			selectorParameter.selectMethodDefinition	= null;
			// ---------------------------------------------------------------------
			// 29/02/2016 ECU added the sort option
			// ---------------------------------------------------------------------
			selectorParameter.sort						= true;
			// ---------------------------------------------------------------------
			StartSelector ();
		}
		else
		{
			AddShop (0);
		}		
	}
	/* ============================================================================= */
	void ShoppingTransactions ()
	{
		if (PublicData.shoppingData.transactions != null && PublicData.shoppingData.transactions.size() > 0)
		{
			BuildTheTransactionsList ();
			selectorParameter.rowLayout 				= R.layout.dose_time_row;
			selectorParameter.customMethodDefinition 	= new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"AddTransaction");
			selectorParameter.customLegend 				= getString (R.string.add);
			selectorParameter.classToRun 				= ShoppingInputActivity.class;
			selectorParameter.type 						= ShoppingInputActivity.TYPE_TRANSACTION;
			// ---------------------------------------------------------------------
			// 08/03/2016 ECU make sure that no selection required
			//----------------------------------------------------------------------
			selectorParameter.selectMethodDefinition	= null;
			// ---------------------------------------------------------------------
			// 29/02/2016 ECU added the sort option
			// ---------------------------------------------------------------------
			selectorParameter.sort						= true;
			// ---------------------------------------------------------------------
			StartSelector ();
		}
		else
		{
			AddTransaction (0);
		}		
	}
	// =============================================================================
	static void SpeakItemToBuy ()
	{
		// -------------------------------------------------------------------------
		// 29/02/2016 ECU created to speak the name of the item
		// -------------------------------------------------------------------------
		// 29/02/2016 ECU get the size of the array because it is used numerous
		//                times
		// -------------------------------------------------------------------------
		int listSize = selectorParameter.listItems.size();
		// -------------------------------------------------------------------------
		if (itemOrder < listSize)
		{
			// ---------------------------------------------------------------------
			// 29/02/2016 ECU flush previous messages to ensure no buffering
			// ---------------------------------------------------------------------
			TextToSpeechService.Flush ();
			// ---------------------------------------------------------------------
			// 29/02/2016 ECU check if need to say which shop need to go to
			// ---------------------------------------------------------------------
			if (!selectorParameter.listItems.get(0).legend.equalsIgnoreCase(whereToShop))
			{
				// -----------------------------------------------------------------
				// 29/02/2016 ECU store the new shop name
				// -----------------------------------------------------------------
				whereToShop = selectorParameter.listItems.get(0).legend;
				// -----------------------------------------------------------------
				// 29/02/2016 ECU the shop has changed tell the use this
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (String.format (context.getString (R.string.where_to_buy),whereToShop),true);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 29/02/2016 ECU now decide the type of message to speak
			// 08/03/2016 ECU changed to use the ID
			// ---------------------------------------------------------------------
			int localID = R.string.item_next;
			if (listSize == 1)
				localID = R.string.item_only;
			else
			if (itemOrder == 0)
				localID = R.string.item_first;
			else
			if (itemOrder == (listSize - 1))
				localID = R.string.item_last;
			// ---------------------------------------------------------------------
			// 29/02/2016 ECU now speak the generated phrase
			// 08/03/2016 ECU change to use resource
			// ---------------------------------------------------------------------
			Utilities.SpeakAPhrase (context,
						String.format(context.getString (R.string.item_to_buy_format),
										context.getString (localID), 
										selectorParameter.listItems.get(0).summary, 
										selectorParameter.listItems.get(0).extras));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void StartSelector (MethodDefinition<?>theMethodDefinition,MethodDefinition<?>theImageHandlerDefinition)
	{
		// -------------------------------------------------------------------------
		// 30/03/2014 ECU start up the selector 
		// -------------------------------------------------------------------------
		Intent intent = new Intent (getBaseContext(),Selector.class);
		intent.putExtra (StaticData.PARAMETER_OBJECT_TYPE,StaticData.OBJECT_SELECTOR);
		intent.putExtra (StaticData.PARAMETER_SELECTOR,selectorParameter);
		intent.putExtra (StaticData.PARAMETER_BACK_KEY,true);
		// -------------------------------------------------------------------------
		// 13/06/2015 ECU check if sorting is required
		// -------------------------------------------------------------------------
		if (selectorParameter.sort)
			intent.putExtra (StaticData.PARAMETER_SORT,true);
		// -------------------------------------------------------------------------
		if (theMethodDefinition != null)
		{
			intent.putExtra (StaticData.PARAMETER_METHOD,theMethodDefinition);
		}
		// --------------------------------------------------------------------------
		// 13/06/2015 ECU check if a handler for the image has been supplied
		// --------------------------------------------------------------------------
		if (theImageHandlerDefinition != null)
		{
			intent.putExtra (StaticData.PARAMETER_IMAGE_HANDLER,theImageHandlerDefinition);
		}
		// -------------------------------------------------------------------------
		// 09/06/2015 ECU set up the swipe method
		// 29/02/2016 ECU do not want to swipe when completing the shopping list
		// -------------------------------------------------------------------------
		if (selectorParameter.type != ShoppingInputActivity.TYPE_COMPLETE_LIST)
			intent.putExtra (StaticData.PARAMETER_SWIPE_METHOD,
						 new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"SwipeAction"));
		// -------------------------------------------------------------------------
		// 11/09/2015 ECU changed to used result code in StaticData
		// -------------------------------------------------------------------------
		startActivityForResult (intent,StaticData.RESULT_CODE_SHOPPING);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	void StartSelector (MethodDefinition<?>theMethodDefinition)
	{
		// -------------------------------------------------------------------------
		// 30/03/2014 ECU start up the selector 
		// 13/06/2015 ECU changed to use the new master method
		// -------------------------------------------------------------------------
		StartSelector (theMethodDefinition,null);
		// -------------------------------------------------------------------------
	}
	/* ----------------------------------------------------------------------------- */
	void StartSelector ()
	{
		// -------------------------------------------------------------------------
		// 05/06/2015 ECU changed the method from 'null' to '....SelectMethod'
		// 07/06/2015 ECU revert to 'null' because logic for deletion was changed
		// -------------------------------------------------------------------------
		StartSelector (null);
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
    public static void SwipeAction (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 09/06/2015 ECU created to handle swipe actions on a list view item
    	// -------------------------------------------------------------------------
    	ShoppingInputActivity.deleteItem (selectorParameter.type,thePosition);
    	// -------------------------------------------------------------------------	
    }
	// =============================================================================
}

package com.usher.diboson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;

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
	// 30/03/2016 ECU use hashCodes to determine whether the data has changed and should
	//                be written to disk. Am aware that this is not perfect but good 
	//                first attempt.
	// 09/09/2017 ECU change to occurrences of "" to BLANK_STRING
	//            ECU proper handling of 'deleted' records
	//            ECU add the facility to display the data structure but only if
	//                in debug mode
	// 25/04/2018 ECU tried to sort out a problem caused when completing a shopping list
	//                - namely giving the 'go to shop' announcement when items are
	//                being bought out of order; this is the reason for introducing
	//                the variable 'boughtPosition'
	// 08/06/2019 ECU some general tidying up
	// 14/01/2020 ECU put in the code to pass a method definition to BarcodeActivity
	//                which will cause the method to be invoked when the barcode
	//                is scan correctly
	// 18/01/2020 ECU added 'button..' handling to SelectParameter so make changes
	//                to take advantage. The button is only used to activate the
	//                barcode scanner when the shopping list is being completed.
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	//==============================================================================
	//private static final String TAG	= "ShoppingActivity";
	/* ============================================================================= */
	
	/* ============================================================================= */
	static	int			boughtPosition;						// 25/04/2018 ECU added
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
	static 	String		shoppingList = StaticData.BLANK_STRING;
															// 01/04/2014 ECU added
			Button  	shops;
			boolean		startShopping = false;
			Button		transactions;
	static 	String		whereToShop;						// 29/02/2016 ECU added
	/* ============================================================================= */
	
	/* ============================================================================= */
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		// -------------------------------------------------------------------------
		super.onCreate (savedInstanceState);
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
			// 24/04/2018 ECU check if need to start shopping immediately
			// --------------------------------------------------------------------
			Bundle extras = getIntent ().getExtras ();
			if (extras != null) 
			{
				// -----------------------------------------------------------------
				// 24/04/2018 ECU check for immediate start of shopping
				// -----------------------------------------------------------------
				startShopping = extras.getBoolean (StaticData.PARAMETER_SHOP,false);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_shopping);
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------
			// 29/03/2014 ECU set up the buttons
			// 31/03/2014 ECU added 'list'
			// 13/06/2015 ECU added 'completeList'
			// 09/09/2017 ECU added 'button_shopping_data'
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
			// 08/06/2019 ECU Note - set up associated listener
			// ---------------------------------------------------------------------
			brands.setOnClickListener 		(ButtonListener);
			completeList.setOnClickListener (ButtonListener);
			items.setOnClickListener 		(ButtonListener);
			list.setOnClickListener 		(ButtonListener);
			lists.setOnClickListener 		(ButtonListener);
			products.setOnClickListener 	(ButtonListener);
			shops.setOnClickListener 		(ButtonListener);
			transactions.setOnClickListener (ButtonListener);
			// ---------------------------------------------------------------------
			// 09/09/2017 ECU handle the data button which is only valid in 'debug'
			//                mode
			// ---------------------------------------------------------------------
			if (PublicData.storedData.debugMode)
			{
				Button shoppingData	= (Button) findViewById (R.id.button_shopping_data);
				// -----------------------------------------------------------------
				// 09/09/2017 ECU make the button visible
				// -----------------------------------------------------------------
				shoppingData.setVisibility (View.VISIBLE);
				// -----------------------------------------------------------------
				// 09/09/2017 ECU handle the click on the button
				// -----------------------------------------------------------------
				shoppingData.setOnClickListener (new View.OnClickListener ()
				{
					@Override
					public void onClick (View theView) 
					{	
						// ---------------------------------------------------------
						// 09/09/2017 ECU copy the default text into the field
						// ---------------------------------------------------------
						DisplayTheShoppingData ();
						// ---------------------------------------------------------
					}
				});
			}
			// ---------------------------------------------------------------------
			// 30/03/2016 ECU initialise the has code
			// ---------------------------------------------------------------------
			initialHashCode = PublicData.shoppingData.HashCode ();
			// ---------------------------------------------------------------------
			// 24/04/2018 ECU check if want to start completing the shopping list
			//                immediately
			// ----------------------------------------------------------------------
			if (startShopping)
			{
				// -----------------------------------------------------------------
				// 24/04/2018 ECU want to immediately start completing the currently
				//                set shopping list
				// -----------------------------------------------------------------
				StartShopping ();
				// -----------------------------------------------------------------
			}
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
	// =============================================================================
    private View.OnClickListener ButtonListener = new View.OnClickListener () 
	{	
		@Override
		public void onClick (View theView) 
		{	
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU now process depending on which button pressed
			// ---------------------------------------------------------------------
			switch (theView.getId()) 
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
					BuildShoppingList (theView,getBaseContext());
					break;
				}
				// -----------------------------------------------------------------
				case R.id.button_shopping_list_complete:
				{
					// -------------------------------------------------------------
					// 13/06/2015 ECU added to handle online shopping list completion
					//            ECU initialise the purchase order
					// 24/04/2018 ECU the code that was here has been put into the
					//                StartShopping method.
					// -------------------------------------------------------------
					StartShopping ();
					// -------------------------------------------------------------
					break;
					// -------------------------------------------------------------
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
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				case R.id.button_shopping_products:
				{
					// -------------------------------------------------------------
					ShoppingProducts ();						
					break;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				case R.id.button_shopping_shops:
				{
					// -------------------------------------------------------------
					ShoppingShops ();						
					break;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				case R.id.button_shopping_transactions:
				{
					// -------------------------------------------------------------
					ShoppingTransactions ();
					break;
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
			}
		}
	};
	/* ============================================================================= */
    public void onDestroy ()
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
    		// ---------------------------------------------------------------------
    		AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + 
    				getBaseContext().getString (R.string.shopping_file),PublicData.shoppingData);
    		// ---------------------------------------------------------------------
    	}
		// -------------------------------------------------------------------------
		super.onDestroy();
		// -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    @Override
	public void onActivityResult (int theRequestCode, int theResultCode, Intent theIntent) 
	{
    	// -------------------------------------------------------------------------
		// 01/04/2014 ECU called when an activity returns a result
    	// 11/09/2015 ECU changed to used result code in StaticData
    	// 11/01/2020 ECU changed to swicth and include barcode
    	// -------------------------------------------------------------------------
    	switch (theRequestCode)
		{
			// =====================================================================
			case StaticData.RESULT_CODE_SHOPPING:
	    		if (theResultCode == RESULT_OK)
	    		{
	    			// -------------------------------------------------------------
	    			// Returned OK
	    			// -------------------------------------------------------------
	    		}
	    		else
	 	    	if (theResultCode == RESULT_CANCELED)
	 	    	{
	 	    		// -------------------------------------------------------------
	 	    		// Handle cancel
	 	    		// -------------------------------------------------------------
	 	    	}
	    		break;
	    	// =====================================================================
			case StaticData.RESULT_CODE_BARCODE:
				// -----------------------------------------------------------------
				// 11/01/2020 ECU to be called when the barcode scanning has completed
				// -----------------------------------------------------------------
				if (theResultCode == RESULT_OK)
				{
					// -------------------------------------------------------------
					// 01/01/2020 ECU a barcode was returned so display it in the 'input'
					//                field
					// -------------------------------------------------------------
					String barcodeRead   = theIntent.getStringExtra (StaticData.PARAMETER_BARCODE);
					Utilities.popToast ("Barcode : " + barcodeRead);
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				break;
			// =====================================================================
			default:
				break;
		}
		// -------------------------------------------------------------------------
	}
    /* ============================================================================= */
    public static void AddBrand (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 08/06/2019 ECU Note - start the activity to add a brand
    	// -------------------------------------------------------------------------
    	Intent intent = new Intent (context,ShoppingInputActivity.class);
    	intent.putExtra(StaticData.PARAMETER_TYPE,ShoppingInputActivity.TYPE_BRAND);
		context.startActivity (intent);
		// -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public static void AddItem (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 10/06/2015 ECU an item is made up of a brand and product - if either is
    	//                missing then cannot do anything
    	// 09/09/2017 ECU change to use Size
    	// -------------------------------------------------------------------------
    	if (ShoppingBrand.Size () > 0 && ShoppingProduct.Size () > 0)
    	{
    		// ---------------------------------------------------------------------
    		Intent intent = new Intent (context,ShoppingInputActivity.class);
    		intent.putExtra(StaticData.PARAMETER_TYPE,ShoppingInputActivity.TYPE_ITEM);
			context.startActivity (intent);
			// ---------------------------------------------------------------------
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 10/06/2015 ECU indicate why am unable to start activity
    		// ---------------------------------------------------------------------
    		Utilities.popToast (context.getString (R.string.unable_to_create_item),true);
    		// ---------------------------------------------------------------------
    	}
    }
    /* ============================================================================= */
    public static void AddList (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 10/06/2015 ECU a list needs transactions - if transactions are
    	//                missing then cannot do anything
    	// 09/09/2017 ECU changed to use Size
    	// -------------------------------------------------------------------------
    	if (ShoppingTransaction.Size () > 0)
    	{
    		// ---------------------------------------------------------------------
    		Intent intent = new Intent (context,ShoppingInputActivity.class);
    		intent.putExtra (StaticData.PARAMETER_TYPE,ShoppingInputActivity.TYPE_LIST);
    		context.startActivity (intent);
    		// ---------------------------------------------------------------------
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
    	// -------------------------------------------------------------------------
    	Intent intent = new Intent (context,ShoppingInputActivity.class);
    	intent.putExtra (StaticData.PARAMETER_TYPE,ShoppingInputActivity.TYPE_PRODUCT);
		context.startActivity (intent);
		// -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public static void AddShop (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	Intent intent = new Intent (context,ShoppingInputActivity.class);
    	intent.putExtra (StaticData.PARAMETER_TYPE,ShoppingInputActivity.TYPE_SHOP);
		context.startActivity (intent);
		// -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public static void AddTransaction (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 10/06/2015 ECU an transaction is made up of an item and shop - if either is
    	//                missing then cannot do anything
    	// 09/09/2017 ECU changed to use the Size
    	// -------------------------------------------------------------------------
    	if (ShoppingItem.Size () > 0 && ShoppingShop.Size () > 0)
    	{
    		// ---------------------------------------------------------------------
    		Intent intent = new Intent (context,ShoppingInputActivity.class);
    		intent.putExtra (StaticData.PARAMETER_TYPE,ShoppingInputActivity.TYPE_TRANSACTION);
			context.startActivity (intent);
			// ---------------------------------------------------------------------
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
    // =============================================================================
    public static void BarCodeMethod (String theBarCode)
	{
		// -------------------------------------------------------------------------
		// 14/01/2020 ECU created to be called when the barcode activity has
		//                scanned in a code
		// -------------------------------------------------------------------------
		// 15/01/2020 ECU search google for details of product
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (context,DisplayURL.class);
		// -------------------------------------------------------------------------
		// 15/01/2020 ECU use the barcode in the search
		// -------------------------------------------------------------------------
		localIntent.putExtra (StaticData.PARAMETER_URL,context.getString (R.string.google_product_search) + "\"" + theBarCode + "\"");
		// -------------------------------------------------------------------------
		// 18/01/2020 ECU set the flag to indicate that want webview client added
		//                so that back key works as required
		// -------------------------------------------------------------------------
		localIntent.putExtra (StaticData.PARAMETER_WEBVIEW_CLIENT,true);
		// -------------------------------------------------------------------------
		context.startActivity (localIntent);
		// -------------------------------------------------------------------------
	}
    /* ============================================================================= */
    static void BuildShoppingList (View view,Context theContext)
	{
    	// -------------------------------------------------------------------------
    	int 		itemIndex;  
    	boolean 	shopAdded = false;
     	int 		transactionIndex;
     	// -------------------------------------------------------------------------
     	// 29/02/2016 ECU clear the message that will be build
     	// -------------------------------------------------------------------------
     	shoppingList = StaticData.BLANK_STRING;
     	// -------------------------------------------------------------------------
     	// 29/02/2016 ECU check if there is a current shopping list if so then use
     	//                that one
     	// -------------------------------------------------------------------------
     	if ((PublicData.shoppingData.currentShoppingList == null) ||
     		(PublicData.shoppingData.currentShoppingList.size () == 0))
     	{
     		// ---------------------------------------------------------------------
     		// 29/02/2016 ECU no shopping list has been defined
     		// ---------------------------------------------------------------------
     		// 27/11/2014 ECU add in the check on size
     		// ---------------------------------------------------------------------  
     		if (PublicData.shoppingData.shops.size () > 0)
     		{
     			for (int theShop = 0; theShop < PublicData.shoppingData.shops.size (); theShop++)
     			{
     				shopAdded = false;
			
     				for (int theList = 0; theList < PublicData.shoppingData.lists.size (); theList++)
     				{
     					if (PublicData.shoppingData.lists.get (theList).selected)
     					{					
     						if (PublicData.shoppingData.transactions.get (PublicData.shoppingData.lists.get (theList).transactionIndex).shopIndex == theShop)
     						{
     							if (!shopAdded)
     							{
     								shopAdded = true;
							
     								shoppingList += String.format (context.getString (R.string.shop_info_format), 
     																PublicData.shoppingData.shops.get (theShop).name);
     							}
     							// -------------------------------------------------
     							// 29/02/2016 ECU Note - does the details of the 
     							//                transaction
     							// -------------------------------------------------
     							transactionIndex = PublicData.shoppingData.lists.get(theList).transactionIndex;
     							itemIndex        = PublicData.shoppingData.transactions.get(transactionIndex).itemIndex;
						
     							shoppingList += String.format ("%-40s",PublicData.shoppingData.items.get(itemIndex).Print()) + 
     															PublicData.shoppingData.lists.get (theList).amount + 
     																StaticData.NEWLINE;
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
     		// 25/04/2018 ECU reset the position
     		// ---------------------------------------------------------------------
     		whereToShop 	= StaticData.BLANK_STRING;
     		boughtPosition 	= StaticData.FIRST_IN_LIST;
     		// ---------------------------------------------------------------------
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
     				shoppingList += String.format (context.getString (R.string.shop_info_format),whereToShop);
     			}
     			// -----------------------------------------------------------------
     			shoppingList += String.format("%-40s",PublicData.shoppingData.items.get (itemIndex).Print ()) + 
										PublicData.shoppingData.currentShoppingList.get (theIndex).amount + 
											StaticData.NEWLINE;
     			// -----------------------------------------------------------------
     		}
     	}
    	// -------------------------------------------------------------------------
    	// 29/02/2016 ECU check if a shopping list has been generated
    	// -------------------------------------------------------------------------
     	if (!shoppingList.equalsIgnoreCase (StaticData.BLANK_STRING))
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
    			selectorParameter.listItems.add (new ListItem (StaticData.BLANK_STRING,
    											 PublicData.shoppingData.brands.get(theIndex).name,
    											 StaticData.BLANK_STRING,
    											 StaticData.BLANK_STRING,
    											 theIndex));
    			// -----------------------------------------------------------------
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
    			// 11/01/2020 ECU added the barcode
    			// -----------------------------------------------------------------
    			selectorParameter.listItems.add (new ListItem (StaticData.BLANK_STRING,
												 PublicData.shoppingData.items.get (theIndex).Print (),
												 StaticData.BLANK_STRING,
						                         PublicData.shoppingData.items.get (theIndex).Barcode (),
												 theIndex));
    			// -----------------------------------------------------------------
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
    	// 25/04/2018 ECU sort according to the order in which items have been bought
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
        		selectorParameter.listItems.add (new ListItem (StaticData.BLANK_STRING,
    						PublicData.shoppingData.shops.get (PublicData.shoppingData.transactions.get (localShoppingList.transactionIndex).shopIndex).name,
    						PublicData.shoppingData.items.get (PublicData.shoppingData.transactions.get (localShoppingList.transactionIndex).itemIndex).Print (),
    						localShoppingList.amount,
    						theIndex,
    						PublicData.shoppingData.currentShoppingList.get (theIndex).selected));
    			// -----------------------------------------------------------------
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
    	// -------------------------------------------------------------------------
    	selectorParameter.listItems = new ArrayList<ListItem> ();
    	// -------------------------------------------------------------------------
     	// 27/11/2014 ECU add in the check on size
    	// 01/03/2016 ECU change to exclude deleted items
     	// -------------------------------------------------------------------------  
     	if (PublicData.shoppingData.lists.size () > 0)
    	for (int theIndex = 0; theIndex < PublicData.shoppingData.lists.size (); theIndex++)
    	{
    		if (!PublicData.shoppingData.lists.get (theIndex).deleted)
			{
    			// -----------------------------------------------------------------
    			// 30/03/2014 ECU added the index as an argument
    			// 31/03/2014 ECU include the current 'selected' state
    			// -----------------------------------------------------------------
    			selectorParameter.listItems.add (new ListItem (StaticData.BLANK_STRING,
    											 PublicData.shoppingData.lists.get (theIndex).Print (),
    											 StaticData.BLANK_STRING,
    											 StaticData.BLANK_STRING,
    											 theIndex,
    											 PublicData.shoppingData.lists.get (theIndex).selected));
    			// -----------------------------------------------------------------
			}
    	}	
	}
    /* ============================================================================= */
    static void BuildTheProductsList ()
    {
    	selectorParameter.listItems = new ArrayList<ListItem> ();
    	// -------------------------------------------------------------------------
     	// 27/11/2014 ECU add in the check on size
    	// 01/03/2016 ECU change to exclude deleted items
     	// -------------------------------------------------------------------------  
     	if (PublicData.shoppingData.products.size() > 0)
    	for (int theIndex = 0; theIndex < PublicData.shoppingData.products.size(); theIndex++)
    	{
    		if (!PublicData.shoppingData.products.get (theIndex).deleted)
			{
    			// -----------------------------------------------------------------
    			// 30/03/2014 ECU added the index as an argument
    			// -----------------------------------------------------------------
    			selectorParameter.listItems.add (new ListItem (StaticData.BLANK_STRING,
												 PublicData.shoppingData.products.get (theIndex).name,
												 StaticData.BLANK_STRING,
												 StaticData.BLANK_STRING,
												 theIndex));
    			// -----------------------------------------------------------------
			}
    	}
	}
    /* ============================================================================= */
   static void BuildTheShopsList ()
    {
    	selectorParameter.listItems = new ArrayList<ListItem> ();
    	// -------------------------------------------------------------------------
     	// 27/11/2014 ECU add in the check on size
    	// 01/03/2016 ECU change to exclude deleted items
     	// -------------------------------------------------------------------------  
     	if (PublicData.shoppingData.shops.size () > 0)
    	for (int theIndex = 0; theIndex < PublicData.shoppingData.shops.size (); theIndex++)
    	{
    		if (!PublicData.shoppingData.shops.get (theIndex).deleted)
			{
    			// -----------------------------------------------------------------
    			// 30/03/2014 ECU added the index as an argument
    			// -----------------------------------------------------------------
    			selectorParameter.listItems.add (new ListItem (StaticData.BLANK_STRING,
    											 PublicData.shoppingData.shops.get (theIndex).name,
    											 StaticData.BLANK_STRING,
    											 StaticData.BLANK_STRING,
    											 theIndex));
    			// -----------------------------------------------------------------
			}
    	}
     	// -------------------------------------------------------------------------
	}
    /* ============================================================================= */
    static void BuildTheTransactionsList ()
    {
    	selectorParameter.listItems = new ArrayList<ListItem> ();
    	// -------------------------------------------------------------------------
     	// 27/11/2014 ECU add in the check on size
    	// 01/03/2016 ECU change to exclude deleted items
     	// -------------------------------------------------------------------------  
     	if (PublicData.shoppingData.transactions.size() > 0)
    	for (int theIndex = 0; theIndex < PublicData.shoppingData.transactions.size (); theIndex++)
    	{
    		if (!PublicData.shoppingData.transactions.get (theIndex).deleted)
			{
    			// -----------------------------------------------------------------
    			// 30/03/2014 ECU added the index as an argument
    			// -----------------------------------------------------------------
    			selectorParameter.listItems.add (new ListItem (StaticData.BLANK_STRING,
    											 PublicData.shoppingData.transactions.get (theIndex).Print (),
    											 StaticData.BLANK_STRING,
    											 StaticData.BLANK_STRING,
    											 theIndex));
			}
    	}	
	}
	// =============================================================================
	public static void ButtonClick (int theItem)
	{
		// -------------------------------------------------------------------------
		// 11/01/2020 ECU called when a 'long click' on the item happens
		//            ECU handling the request to scan in a barcode
		// 14/01/2020 ECU added the PARAMETER_METHOD
		// 18/01/2020 ECU changed to 'ButtonClick'
		//            ECU added WEBVIEW_CLIENT option
		// -------------------------------------------------------------------------
		Intent localIntent = new Intent (context,BarCodeActivity.class);
		localIntent.putExtra (StaticData.PARAMETER_BARCODE,true);
		localIntent.putExtra (StaticData.PARAMETER_BARCODE_ONLY,true);
		localIntent.putExtra (StaticData.PARAMETER_WEBVIEW_CLIENT,true);
		localIntent.putExtra (StaticData.PARAMETER_METHOD_DEFINITION,
				new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"BarCodeMethod"));
		((Activity) context).startActivityForResult (localIntent,StaticData.RESULT_CODE_BARCODE);
		// -------------------------------------------------------------------------
	}
    /* ============================================================================= */
    public static void BuyItem (int thePosition)
    {
    	if (!PublicData.shoppingData.currentShoppingList.get(thePosition).selected)
    	{
    		// ---------------------------------------------------------------------
    		// 13/06/2015 ECU created to handle the purchase of an item
    		// ---------------------------------------------------------------------
    		PublicData.shoppingData.currentShoppingList.get (thePosition).ToggleSelected ();
    		// ---------------------------------------------------------------------
    		// 13/06/2015 ECU increment the 'order' in which the item was bought
    		// ---------------------------------------------------------------------
    		PublicData.shoppingData.currentShoppingList.get (thePosition).SetOrder (itemOrder++);
    		// ---------------------------------------------------------------------
    		// 23/04/2018 ECU in case the item was bought of sequence then remember
    		//                this shop
    		// ---------------------------------------------------------------------
    		whereToShop = PublicData.shoppingData.currentShoppingList.get (thePosition).GetShopName ();
    		// ---------------------------------------------------------------------
    		// 25/04/2018 ECU remember the position of this item
    		// ---------------------------------------------------------------------
    		boughtPosition = thePosition;
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
    			if (!PublicData.shoppingData.currentShoppingList.get (theIndex).selected)
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
    			Utilities.popToastAndSpeak (context.getString (R.string.all_items_bought));
    		// ---------------------------------------------------------------------
    	}
    }
    // =============================================================================
    void DisplayTheShoppingData ()
    {
    	// -------------------------------------------------------------------------
    	// 09/09/2017 ECU created to display all of the shopping data to the log file
    	// -------------------------------------------------------------------------
		setContentView (R.layout.activity_system_info);
		// -------------------------------------------------------------------------
		TextView summaryTextview  = (TextView) findViewById (R.id.system_info_textview);
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU set up the parameters of the field
		// -------------------------------------------------------------------------
		summaryTextview.setMaxLines (StaticData.SYSTEM_INFO_MAX_LINES);
		summaryTextview.setTypeface (Typeface.MONOSPACE); 
		summaryTextview.setGravity  (Gravity.LEFT);
		summaryTextview.setMovementMethod (new ScrollingMovementMethod ());
		// -------------------------------------------------------------------------
		// 30/08/2017 ECU adjust the font size
		// -------------------------------------------------------------------------
		summaryTextview.setTextSize (TypedValue.COMPLEX_UNIT_PX,
                 getResources ().getDimension (R.dimen.data_font));
		// -------------------------------------------------------------------------
		// 27/08/2017 ECU check if there are any entries and give correct
		//                display
		// -------------------------------------------------------------------------
		summaryTextview.setText (ShoppingProduct.PrintAll () 	+ StaticData.NEWLINE +
								 ShoppingBrand.PrintAll () 		+ StaticData.NEWLINE +
								 ShoppingItem.PrintAll () 		+ StaticData.NEWLINE +
								 ShoppingShop.PrintAll () 		+ StaticData.NEWLINE +
								 ShoppingTransaction.PrintAll ()+ StaticData.NEWLINE +
								 ShoppingList.PrintAllRecords ());
    	// -------------------------------------------------------------------------
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
	// =============================================================================
	public static void HelpBrandMethod (int theBrand)
	{
		// -------------------------------------------------------------------------
		// 245/01/2020 ECU created to handle help for the brand
		// -------------------------------------------------------------------------
		Utilities.popToast (PublicData.shoppingData.brands.get (theBrand).Print ());
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void HelpItemMethod (int theItem)
	{
		// -------------------------------------------------------------------------
		// 245/01/2020 ECU created to handle help for the item
		// -------------------------------------------------------------------------
		Utilities.popToast (PublicData.shoppingData.items.get (theItem).Print ());
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void HelpListMethod (int theList)
	{
		// -------------------------------------------------------------------------
		// 245/01/2020 ECU created to handle help for the list
		// -------------------------------------------------------------------------
		Utilities.popToast (PublicData.shoppingData.lists.get (theList).Print ());
		// -------------------------------------------------------------------------
	}
    // =============================================================================
	public static void HelpProductMethod (int theProduct)
	{
		// -------------------------------------------------------------------------
		// 245/01/2020 ECU created to handle help for the product
		// -------------------------------------------------------------------------
		Utilities.popToast (PublicData.shoppingData.products.get (theProduct).Print ());
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void HelpShopMethod (int theShop)
	{
		// -------------------------------------------------------------------------
		// 245/01/2020 ECU created to handle help for the shop
		// -------------------------------------------------------------------------
		Utilities.popToast (PublicData.shoppingData.shops.get (theShop).Print ());
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void HelpTransactionMethod (int theTransaction)
	{
		// -------------------------------------------------------------------------
		// 245/01/2020 ECU created to handle help for the transaction
		// -------------------------------------------------------------------------
		Utilities.popToast (PublicData.shoppingData.transactions.get (theTransaction).Print ());
		// -------------------------------------------------------------------------
	}
    /* ============================================================================= */
    public static void ImageHandler (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 31/03/2014 ECU toggle the 'selected' entry
    	// -------------------------------------------------------------------------
    	PublicData.shoppingData.lists.get (thePosition).ToggleSelected ();
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
    	// 23/04/2018 ECU only sort if the parameter indicates that it is required
    	// ------------------------------------------------------------------------
    	if (selectorParameter.sort)
    		Collections.sort (selectorParameter.listItems);	
    	// ------------------------------------------------------------------------
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
    	// 16/04/2019 ECU changed so that if there is no quantity then do not say
    	//                anything
    	// 18/04/2019 ECU changed to use 'spokenSummary'
    	// --------------------------------------------------------------------------
    	Utilities.SpeakAPhrase (context,
    							String.format (context.getString (R.string.item_is_format),
    									spokenSummary (context,selectorParameter.listItems.get (theItem).summary), 
    										(!Utilities.isStringBlank (selectorParameter.listItems.get (theItem).extras)
    												? String.format (context.getString (R.string.item_to_buy_quantity_format),selectorParameter.listItems.get (theItem).extras)
    												: StaticData.BLANK_STRING)));
    	// --------------------------------------------------------------------------
    }
    // ============================================================================= 
	void ShoppingBrands ()
	{
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU use Size which takes into account deleted records
		// 30/01/2020 ECU change row from dose_time_row
		// -------------------------------------------------------------------------
		if (ShoppingBrand.Size () > 0)
		{
			BuildTheBrandsList ();
			selectorParameter.rowLayout 				= R.layout.shopping_details_row;
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
			// 24/01/2020 ECU set up the method for the brand
			// ---------------------------------------------------------------------
			selectorParameter.helpMethodDefinition  	= new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"HelpBrandMethod");
			// ---------------------------------------------------------------------
			// 18/01/2020 ECU ensure that the scanner is not displayed
			// ---------------------------------------------------------------------
			selectorParameter.buttonMethodDefinition	= null;
			// ---------------------------------------------------------------------
			StartSelector ();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ----------------------------------------------------------------------
			AddBrand (0);
			// ----------------------------------------------------------------------
		}		
	}
	/* ============================================================================= */
	void ShoppingItems ()
	{
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU use Size which takes into account deleted records
		// 30/01/2020 ECU change row from dose_time_row
		// -------------------------------------------------------------------------
		if (ShoppingItem.Size () > 0)
		{
			BuildTheItemsList ();
			selectorParameter.rowLayout 				=  R.layout.shopping_details_row;
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
			// 24/01/2020 ECU set up the method for the item
			// ---------------------------------------------------------------------
			selectorParameter.helpMethodDefinition  	= new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"HelpItemMethod");
			// ---------------------------------------------------------------------
			// 18/01/2020 ECU ensure that the scanner is not displayed
			// ---------------------------------------------------------------------
			selectorParameter.buttonMethodDefinition	= null;
			// ---------------------------------------------------------------------
			StartSelector ();
			// ---------------------------------------------------------------------
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
			// 23/04/2018 ECU do not want to change the order of the list
			// ---------------------------------------------------------------------
			selectorParameter.sort						= false;
			// ---------------------------------------------------------------------
			// 08/03/2016 ECU declare the method to be called when the item is clicked 
			//                and indicate that the select activity must not finish
			//                when the item is selected
			// ---------------------------------------------------------------------
			selectorParameter.finishOnSelect			= false;
			selectorParameter.selectMethodDefinition 	= new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"SelectItem");
			// ---------------------------------------------------------------------
			// 19/01/2020 ECU set up the settings for the 'button'
			// ---------------------------------------------------------------------
			selectorParameter.buttonMethodDefinition	= new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"ButtonClick");
			selectorParameter.buttonResourceId			= R.drawable.start_scanner;
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
		if (ShoppingList.Size() > 0)
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
			// 24/01/2020 ECU set up the method for the list
			// ---------------------------------------------------------------------
			selectorParameter.helpMethodDefinition  	= new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"HelpListMethod");
			// ---------------------------------------------------------------------
			// 18/01/2020 ECU ensure that the scanner is not displayed
			// ---------------------------------------------------------------------
			selectorParameter.buttonMethodDefinition	= null;
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
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU use Size which takes into account deleted records
		// 30/01/2020 ECU change the row from dose_time_row
		// -------------------------------------------------------------------------
		if (ShoppingProduct.Size () > 0)
		{
			BuildTheProductsList ();
			selectorParameter.rowLayout 				= R.layout.shopping_details_row;
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
			// 24/01/2020 ECU set up the method for the products
			// ---------------------------------------------------------------------
			selectorParameter.helpMethodDefinition  	= new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"HelpProductMethod");
			// ---------------------------------------------------------------------
			// 18/01/2020 ECU ensure that the scanner is not displayed
			// ---------------------------------------------------------------------
			selectorParameter.buttonMethodDefinition	= null;
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
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU use Size which takes into account deleted records
		// 30/01/2020 ECU changed the row from dose_time_row
		// -------------------------------------------------------------------------
		if (ShoppingShop.Size () > 0)
		{
			BuildTheShopsList ();
			selectorParameter.rowLayout 				=  R.layout.shopping_details_row;
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
			// 24/01/2020 ECU set up the method for the shops
			// ---------------------------------------------------------------------
			selectorParameter.helpMethodDefinition  	= new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"HelpShopMethod");
			// ---------------------------------------------------------------------
			// 18/01/2020 ECU ensure that the scanner is not displayed
			// ---------------------------------------------------------------------
			selectorParameter.buttonMethodDefinition	= null;
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
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU use Size which takes into account deleted records
		// 30/01/2020 ECU changed the row from dose_time_row
		// -------------------------------------------------------------------------
		if (ShoppingTransaction.Size () > 0)
		{
			BuildTheTransactionsList ();
			selectorParameter.rowLayout 				= R.layout.shopping_details_row;
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
			// 24/01/2020 ECU set up the method for the transaction
			// ---------------------------------------------------------------------
			selectorParameter.helpMethodDefinition  	= new MethodDefinition<ShoppingActivity> (ShoppingActivity.class,"HelpTransactionMethod");
			// ---------------------------------------------------------------------
			// 18/01/2020 ECU ensure that the scanner is not displayed
			// ---------------------------------------------------------------------
			selectorParameter.buttonMethodDefinition	= null;
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
		int listSize = selectorParameter.listItems.size ();
		// -------------------------------------------------------------------------
		if (itemOrder < listSize)
		{
			// ---------------------------------------------------------------------
			// 29/02/2016 ECU flush previous messages to ensure no buffering
			// ---------------------------------------------------------------------
			TextToSpeechService.Flush ();
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
    		// 25/04/2018 ECU check if the next item to buy has already been 'bought'
    		// ----------------------------------------------------------------------
    		if (PublicData.shoppingData.currentShoppingList.get (boughtPosition).selected)
    		{
    			// -----------------------------------------------------------------
    			// 25/04/2018 ECU reset the position for the next purchase
    			// -----------------------------------------------------------------
    			boughtPosition = StaticData.FIRST_IN_LIST;
    			// -----------------------------------------------------------------
    		}
			// ---------------------------------------------------------------------
			// 29/02/2016 ECU check if need to say which shop need to go to
			// 25/04/2018 ECU changed from '(0)' to '(boughtPosition)'
			// ---------------------------------------------------------------------
			if (!selectorParameter.listItems.get (boughtPosition).legend.equalsIgnoreCase (whereToShop))
			{
				// -----------------------------------------------------------------
				// 29/02/2016 ECU store the new shop name
				// -----------------------------------------------------------------
				whereToShop = selectorParameter.listItems.get (boughtPosition).legend;
				// -----------------------------------------------------------------
				// 29/02/2016 ECU the shop has changed tell the use this
				// 23/04/2018 ECU add string depending on whether the item is the
				//                'first', 'only' or 'last' item
				// -----------------------------------------------------------------
				Utilities.popToastAndSpeak (String.format (context.getString (R.string.where_to_buy),
																whereToShop,context.getString (localID)),
																	true);
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 29/02/2016 ECU now speak the generated phrase
			// 08/03/2016 ECU change to use resource
			// 16/04/2019 ECU change so that if there is 'no' quantity then speak
			//                nothing
			// 18/04/2019 ECU changed to used the 'spokenSummary' method
			// ---------------------------------------------------------------------
			Utilities.SpeakAPhrase (context,
						String.format (context.getString (R.string.item_to_buy_format),
										context.getString (localID), 
										spokenSummary (context,selectorParameter.listItems.get (boughtPosition).summary), 
										(!Utilities.isStringBlank (selectorParameter.listItems.get (boughtPosition).extras)
													? String.format (context.getString (R.string.item_to_buy_quantity_format),selectorParameter.listItems.get (boughtPosition).extras)
													: StaticData.BLANK_STRING)));
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static String spokenSummary (Context theContext,String theSummary)
	{
		// -------------------------------------------------------------------------
		// 18/04/2019 ECU on entry theSummary is of the form
		//                    'product name (the brand)', e.g. beans (Heinz)
		//                want to convert this to a spoken form of
		//                    'product name produced by the brand e.g. 'beans produced by Heinz'
		//            ECU NOTE - this is very clumsy but is a quick fix
		// -------------------------------------------------------------------------
		return theSummary.replace ("(",context.getString (R.string.produced_by)).replace (", newChar)",StaticData.BLANK_STRING);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	void StartSelector (MethodDefinition<?>theMethodDefinition,MethodDefinition<?>theImageHandlerDefinition)
	{
		// -------------------------------------------------------------------------
		// 30/03/2014 ECU start up the selector 
		// -------------------------------------------------------------------------
		Intent intent = new Intent (getBaseContext (),Selector.class);
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
	void StartShopping ()
	{
		// -------------------------------------------------------------------------
		// 24/04/2018 ECU created this method to hold the code that was in the button
		//                listener and which starts the shoping task.
		// -------------------------------------------------------------------------
		// 13/06/2015 ECU added to handle online shopping list completion
		//            ECU initialise the purchase order
		// -------------------------------------------------------------------------
		if (PublicData.shoppingData.currentShoppingList == null)
		{
			// ---------------------------------------------------------------------
			PublicData.shoppingData.currentShoppingList = new ArrayList <ShoppingList> ();
			// ---------------------------------------------------------------------
			// 14/06/2015 ECU copy across the selected items from the list
			// ---------------------------------------------------------------------
			if (PublicData.shoppingData.lists != null && PublicData.shoppingData.lists.size() > 0)
			{
				for (int theIndex = 0; theIndex < PublicData.shoppingData.lists.size (); theIndex++)
				{
					// -------------------------------------------------------------
					// 14/06/2015 ECU build up a current shopping list 
					//                from selected items
					// -------------------------------------------------------------
					if (PublicData.shoppingData.lists.get (theIndex).selected)
					{
						// ---------------------------------------------------------
						// 14/06 2015 ECU generate a new shopping list entry
						//                from the existing list and add
						//                to the list being generated
						//            ECU NOTE : originally did
						//                   .add (PublicData.shoppingData.lists.get(theIndex)
						//                but this was causing problems so
						//                changed to the current version
						// ---------------------------------------------------------
						PublicData.shoppingData.currentShoppingList.add (new ShoppingList (PublicData.shoppingData.lists.get (theIndex).transactionIndex,
																						   PublicData.shoppingData.lists.get (theIndex).amount));
						// ---------------------------------------------------------
					}
				}
			}
		}
		// -------------------------------------------------------------------------
		if (PublicData.shoppingData.currentShoppingList != null && 
			PublicData.shoppingData.currentShoppingList.size () > 0)
		{
			for (int theIndex = 0; theIndex < PublicData.shoppingData.currentShoppingList.size (); theIndex++)
			{
				// -----------------------------------------------------------------
				// 14/06/2015 ECU preset some variables
				// -----------------------------------------------------------------
				PublicData.shoppingData.currentShoppingList.get(theIndex).SetOrder (StaticData.NO_RESULT);
				PublicData.shoppingData.currentShoppingList.get(theIndex).setState (false);
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 29/02/2016 ECU default the shop where items are to be bought
		// -------------------------------------------------------------------------
		whereToShop = StaticData.BLANK_STRING;
		// -------------------------------------------------------------------------
		ShoppingList ();
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
    public void SwipeAction (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 09/06/2015 ECU created to handle swipe actions on a list view item
    	// -------------------------------------------------------------------------
    	ShoppingInputActivity.deleteItem (selectorParameter.type,thePosition);
    	// -------------------------------------------------------------------------	
    }
	// =============================================================================
}

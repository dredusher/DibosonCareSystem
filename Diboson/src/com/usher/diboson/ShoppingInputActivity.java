package com.usher.diboson;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;

public class ShoppingInputActivity extends DibosonActivity 
{
	/* ============================================================================= */
	// 29/03/2014 ECU created
	//            ECU handle the input of various shopping details
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 02/11/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 07/06/2019 ECU Some general tidying up
	// 11/01/2020 ECU added the barcode handling
	/* ============================================================================= */
	//private static final String TAG = "ShoppingInputActivity";
	/* ============================================================================= */
	public static final int TYPE_BRAND			= 0;
	public static final int TYPE_COMPLETE_LIST	= 1;		// 13/06/2015 ECU added
	public static final int TYPE_ITEM			= 2;
	public static final int TYPE_LIST			= 3;		// 30/03/2014 ECU added
	public static final int TYPE_PRODUCT 		= 4;
	public static final int TYPE_SHOP 			= 5;
	public static final int TYPE_TRANSACTION	= 6;		// 30/03/2014 ECU added
	/* ============================================================================= */
			Button		barcodeButton;								// 11/01/2020 ECU added
			Button		confirmButton;
	static 	Context		context;									// 10/06/2015 ECU added
			String		currentName;
			Button		deleteButton;								// 07/06/2015 ECU added
			TextView 	inputBarcode;								// 11/01/2020 ECU added
			TextView 	inputName;
			int			selectedBrand;
			int			selectedItem;								// 30/03/2014 ECU added
			int			selectedProduct;
			int         selectedShop;								// 30/03/2014 ECU added
			String		selectedString;								// 31/03/2014 ECU added
			int         selectedTransaction;						// 30/03/2014 ECU added
			int			selection 		= StaticData.NO_RESULT;
			int			type 			= StaticData.NO_RESULT;
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
			// 10/06/2015 ECU store the context for future use
			// ---------------------------------------------------------------------
			context = this;
			// ---------------------------------------------------------------------		
			Bundle extras = getIntent().getExtras();
	    
			if (extras != null)
			{
				// -----------------------------------------------------------------
				// 29/03/2014 ECU get the type of input
				// -----------------------------------------------------------------
				type	  = extras.getInt (StaticData.PARAMETER_TYPE,StaticData.NO_RESULT);  
 	   			// -----------------------------------------------------------------
 	   			// 24/03/2014 ECU get the medication index from the intent
 	   			// -----------------------------------------------------------------
 	   			selection	 = extras.getInt (StaticData.PARAMETER_SELECTION,StaticData.NO_RESULT);  
 	   			// -----------------------------------------------------------------  
			}
			// ---------------------------------------------------------------------
			// 28/11/2016 ECU call method to set the screen up
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true,true,false);
			// ---------------------------------------------------------------------
			// 29/03/2014 ECU decide what initial action is to be taken
			// ---------------------------------------------------------------------
			switch (type)
			{
	   			// -----------------------------------------------------------------
	   			case TYPE_BRAND:
	   				setContentView (R.layout.shopping_brand);
	   				if (selection != StaticData.NO_RESULT)
	   					currentName = PublicData.shoppingData.brands.get(selection).name;
	   				break;
	   			// -----------------------------------------------------------------
	   			case TYPE_ITEM:
	   				setContentView (R.layout.shopping_item);
	   				Spinners (type);
	   				break;
	   			// -----------------------------------------------------------------
	   			case TYPE_LIST:
	   				setContentView (R.layout.shopping_list);
	   				if (selection != StaticData.NO_RESULT)
	   					currentName = PublicData.shoppingData.lists.get(selection).amount;
	   				Spinners (type);
	   				break;
	   			// -----------------------------------------------------------------
	   			case TYPE_PRODUCT:
	   				setContentView (R.layout.shopping_product);
	   				if (selection != StaticData.NO_RESULT)
	   					currentName = PublicData.shoppingData.products.get(selection).name;
	   				break;
	   			// -----------------------------------------------------------------
	   			case TYPE_SHOP:
	   				setContentView (R.layout.shopping_shop);
	   				if (selection != StaticData.NO_RESULT)
	   					currentName = PublicData.shoppingData.shops.get(selection).name;
	   				break;
	   			// -----------------------------------------------------------------
	   			case TYPE_TRANSACTION:
	   				setContentView(R.layout.shopping_transaction);
	   				Spinners (type);
	   				break;
	   			// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			inputName = (TextView) findViewById (R.id.input_name);
			// ---------------------------------------------------------------------
			// 07/06/2015 ECU set up the buttons
			// ---------------------------------------------------------------------
			confirmButton = (Button) findViewById (R.id.confirm_button);
			confirmButton.setOnClickListener (ButtonListener);
			// ---------------------------------------------------------------------
			// 07/06/2015 ECU set up the visibility of the delete button - default to 
			//                invisible
			// ---------------------------------------------------------------------
			deleteButton = (Button) findViewById (R.id.delete_button);
			deleteButton.setVisibility (View.INVISIBLE);
			// ---------------------------------------------------------------------
			// 07/06/2015 ECU check if the activity is being activated for editing an
			//                existing item or adding a new one
			// ---------------------------------------------------------------------
			if (inputName != null && selection != StaticData.NO_RESULT)
			{
				inputName.setText (currentName);
				// -----------------------------------------------------------------
				// 07/06/2015 ECU set up the 'delete' button which is only necessary
				//                when an item is being edited NOT added
				// -----------------------------------------------------------------
				deleteButton.setOnClickListener (ButtonListener);
				deleteButton.setVisibility (View.VISIBLE);
				// -----------------------------------------------------------------
				// 10/06/2015 ECU copy across the name for future use
				// 07/06/2019 ECU with the addition of 'getLegend' - so no need to
				//                have here
				// -----------------------------------------------------------------
				//ShoppingActivity.selectorParameter.name = currentName;
				// -----------------------------------------------------------------
			}
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
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return true;
	}
	// =============================================================================
	@Override
	public void onActivityResult (int theRequestCode, int theResultCode, Intent theIntent)
	{
		// -------------------------------------------------------------------------
		// 11/01/2020 ECU to be called when the barcode scanning has completed
		// -------------------------------------------------------------------------
		if (theResultCode == RESULT_OK)
		{
			// ---------------------------------------------------------------------
			// 01/01/2020 ECU a barcode was returned so display it in the 'input'
			//                field
			// ---------------------------------------------------------------------
			String barcodeRead   = theIntent.getStringExtra (StaticData.PARAMETER_BARCODE);
			inputBarcode.setText (barcodeRead);
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
    private View.OnClickListener ButtonListener = new View.OnClickListener() 
	{	
		@Override
		public void onClick (View view) 
		{	
			// ---------------------------------------------------------------------
			// 07/01/2014 ECU now process depending on which button pressed
			// 07/06/2015 ECU changed to used formatted resource string for displaying
			//                the 'already exists' message
			// ---------------------------------------------------------------------
			switch (view.getId()) 
			{
				// -----------------------------------------------------------------
				case R.id.barcode_button:
					// -------------------------------------------------------------
					// 11/01/2020 ECU handling the request to scan in a barcode
					// -------------------------------------------------------------
					Intent localIntent = new Intent (context,BarCodeActivity.class);
					localIntent.putExtra (StaticData.PARAMETER_BARCODE,true);
					localIntent.putExtra (StaticData.PARAMETER_BARCODE_ONLY,true);
					startActivityForResult (localIntent,StaticData.RESULT_CODE_BARCODE);
					// -------------------------------------------------------------
					break;
				// -----------------------------------------------------------------
				case R.id.confirm_button:
				{
					String	barcode	= null;
		   			String	name 	= null;;
		   			// -------------------------------------------------------------
		   			// 07/01/2014 ECU get the name from the displayed field - if that
		   			//                field exists
		   			// -------------------------------------------------------------
		   			if (inputName != null)
		   			{
		   				name = inputName.getText().toString();
		   				// ---------------------------------------------------------
			   			// 16/04/2019 ECU do not allow an empty field
			   			// ---------------------------------------------------------
			   			if (Utilities.isStringBlank (name))
			   			{
			   				// -----------------------------------------------------
			   				// 16/04/2019 ECU the name is blank
			   				// -----------------------------------------------------
			   				Utilities.popToastAndSpeak (getString (R.string.empty_field_not_allowed),true);
			   				// -----------------------------------------------------
			   				// 16/04/2019 ECU ignore the key click
			   				// -----------------------------------------------------
			   				return;
			   				// -----------------------------------------------------
			   			}
		   			}
		   			// -------------------------------------------------------------
		   			if (inputBarcode != null)
					{
						// ---------------------------------------------------------
						// 11/01/2020 ECU get the barcode from the input field
						// ---------------------------------------------------------
						barcode = inputBarcode.getText().toString();
						// ---------------------------------------------------------
					}
		   			// -------------------------------------------------------------
		   			// 07/01/2014 ECU process depending on the type of item
		   			// -------------------------------------------------------------
					switch (type)
				   	{
				   		// ---------------------------------------------------------
				   		case TYPE_BRAND:
				   			// -----------------------------------------------------
				   			// 31/03/2014 ECU check if the name already exists
				   			// 01/03/2016 ECU changed to use new Add method
				   			// -----------------------------------------------------
				   			if (ShoppingBrand.GetIndex (name) == StaticData.NO_RESULT)
				   			{
				   				if (selection == StaticData.NO_RESULT)
				   					ShoppingBrand.Add (new ShoppingBrand(name));
				   				else
				   					PublicData.shoppingData.brands.set (selection,new ShoppingBrand(name));
				   			}
				   			else
				   			{
				   				// -------------------------------------------------
				   				// 03/07/2020 ECU correct an error in the format
				   				// -------------------------------------------------
				   				Utilities.popToast (String.format (getString (R.string.already_exists_message),name));
				   			}
				   			break;
				   		// ---------------------------------------------------------
				   		case TYPE_ITEM:
				   			// -----------------------------------------------------
				   			// 31/03/2014 ECU check if the name already exists
				   			// 01/03/2016 ECU changed to use Add method
				   			// 11/01/2020 ECU added the barcode
				   			//            ECU want to accommodate a change to the barcode
				   			//                even if everything else is the same
				   			// -----------------------------------------------------
				   			int itemFound;
				   			if ((itemFound = ShoppingItem.GetIndex (selectedBrand,selectedProduct)) == StaticData.NO_RESULT)
				   			{
				   				if (selection == StaticData.NO_RESULT)
				   					ShoppingItem.Add (new ShoppingItem (selectedBrand,selectedProduct,barcode));
				   				else
				   					PublicData.shoppingData.items.set (selection,new ShoppingItem (selectedBrand,selectedProduct,barcode));
				   			}
				   			else
				   			{
				   				// -------------------------------------------------
				   				// 11/01/2020 ECU there is an item with same brand
				   				//                and product but need to check if
				   				//                the barcode has changed - the
				   				//                method will do this
				   				// -------------------------------------------------
				   				if (!PublicData.shoppingData.items.get(itemFound).BarcodeUpdate(barcode))
								{
									// ---------------------------------------------
									// 11/01/2020 ECU Note - tell user that nothing
									//                       has changed
									// ---------------------------------------------
				   					Utilities.popToast (getString (R.string.item_already_exists));
				   					// ---------------------------------------------
								}
				   			}
				   			break;
				   		// ---------------------------------------------------------
				   		case TYPE_LIST:
				   			// -----------------------------------------------------
				   			// 31/03/2014 ECU check if the name already exists
				   			// 01/03/2016 ECU change to use new Add method
				   			// -----------------------------------------------------
				   			if (selection == StaticData.NO_RESULT)
				   				ShoppingList.Add (new ShoppingList (selectedTransaction,name));
				   			else
				   				PublicData.shoppingData.lists.set (selection,new ShoppingList (selectedTransaction,name));
				   			break;
				   		// ---------------------------------------------------------
				   		case TYPE_PRODUCT:
				   			// -----------------------------------------------------
				   			// 31/03/2014 ECU check if the name already exists
				   			// 01/03/2016 ECU change to use new Add method
				   			// -----------------------------------------------------
				   			if (ShoppingProduct.GetIndex (name) == StaticData.NO_RESULT)
				   			{
				   				if (selection == StaticData.NO_RESULT)
				   					ShoppingProduct.Add (new ShoppingProduct (name));
				   				else
				   					PublicData.shoppingData.products.set(selection,new ShoppingProduct (name));
				   			}
				   			else
				   			{
				   				// -------------------------------------------------
				   				// 03/07/2020 ECU correct an error in the format
				   				// -------------------------------------------------
				   				Utilities.popToast (String.format (getString (R.string.already_exists_message),name));
				   			}
				   			break;
				   		// ---------------------------------------------------------
				   		case TYPE_SHOP:
				   			// -----------------------------------------------------
				   			// 31/03/2014 ECU check if the name already exists
				   			// 01/03/2016 ECU change to use new Add method
				   			// -----------------------------------------------------
				   			if (ShoppingShop.GetIndex (name) == StaticData.NO_RESULT)
				   			{
				   				if (selection == StaticData.NO_RESULT)
				   					ShoppingShop.Add (new ShoppingShop(name));
				   				else
				   					PublicData.shoppingData.shops.set(selection,new ShoppingShop(name));
				   			}
				   			else
				   			{
								// -------------------------------------------------
								// 03/07/2020 ECU correct an error in the format
								// -------------------------------------------------
				   				Utilities.popToast (String.format (getString (R.string.already_exists_message),name));
				   			}
				   			break;
				   		// ---------------------------------------------------------
				   		case TYPE_TRANSACTION:
				   			// -----------------------------------------------------
				   			// 31/03/2014 ECU check if the name already exists
				   			// 01/03/2016 ECU change to use the Add method
				   			// -----------------------------------------------------
				   			if (ShoppingTransaction.GetIndex (selectedItem,selectedShop) == StaticData.NO_RESULT)
				   			{
				   				if (selection == StaticData.NO_RESULT)
				   					ShoppingTransaction.Add (new ShoppingTransaction(selectedItem,selectedShop));
				   				else
				   					PublicData.shoppingData.transactions.set(selection,new ShoppingTransaction(selectedItem,selectedShop));
				   			}
				   			else
				   			{
				   				// -------------------------------------------------
				   				// 08/06/2019 ECU changed to use the resource
				   				// -------------------------------------------------
				   				Utilities.popToast (getString (R.string.transaction_already_exists));
				   				// -------------------------------------------------
				   			}
				   			break;
				   		// ---------------------------------------------------------
				   	}
				   	// -------------------------------------------------------------
				   	// 11/01/2020 ECU terminate this activity
				   	// --------------------------------------------------------------
					finish ();
					// -------------------------------------------------------------
					break;
				}
				// -----------------------------------------------------------------
				case R.id.delete_button:
				{	
					// -------------------------------------------------------------
					// 07/06/2015 ECU added to delete the displayed item
					// -------------------------------------------------------------
					deleteItem (type,selection);
					// -------------------------------------------------------------
					// 09/06/2015 ECU and terminate this activity
					// -------------------------------------------------------------
					finish ();
					// -------------------------------------------------------------
					break;	
				}
				// -----------------------------------------------------------------
			}
		}
	};
	// =============================================================================
	public static void deleteItem (int theType,int theSelection)
	{
		// -------------------------------------------------------------------------
		// 10/06/2015 ECU created to initiate the dialogue
		// 07/06/2019 ECU changed to use getLegend
		//            ECU changed to use the resource
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (Selector.context,
				   				 Selector.context.getString (R.string.item_deletion),
				   				 String.format (Selector.context.getString (R.string.delete_confirmation_format),getLegend (theType,theSelection)),
				   				 (Object) theSelection,
				   				 Utilities.createAMethod (ShoppingInputActivity.class,"YesMethod",(Object) null),
				   				 Utilities.createAMethod (ShoppingInputActivity.class,"NoMethod",(Object) null)); 
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean deleteItemConfirmed (int theType,int theSelection)
	{
		// -------------------------------------------------------------------------
		// 07/06/2015 ECU created to delete the selected item
		// 09/09/2017 ECU changed to boolean so that can return the result
		// -------------------------------------------------------------------------
		boolean result = true;
		// -------------------------------------------------------------------------
		switch (theType)
	   	{
			// =====================================================================
	   		case TYPE_BRAND:
	   			// -----------------------------------------------------------------
	   			// 07/06/2015 ECU delete the selected brand
	   			// 01/03/2016 ECU change to use Delete method
	   			//            ECU check if the brand is being used in an item
	   			// -----------------------------------------------------------------
	   			if (!ShoppingItem.CheckBrandIndex (theSelection))
	   			{
	   				PublicData.shoppingData.brands.get (theSelection).Delete ();
	   				// -------------------------------------------------------------
	   				// 09/09/2017 ECU check if any brands are left
	   				// -------------------------------------------------------------
	   				if (ShoppingBrand.Size () == 0)
	   					return false;
	   				// -------------------------------------------------------------
	   			}
	   			else
	   			{
	   				Utilities.popToastAndSpeak (Selector.context.getString (R.string.cannot_delete_brand),true); 
	   			}
	   			// -----------------------------------------------------------------
	   			break;
	   		// =====================================================================
	   		case TYPE_ITEM:
	   			// -----------------------------------------------------------------
	   			// 07/06/2015 ECU delete the selected item
	   			// 01/03/2016 ECU change to use Delete method
	   			//            ECU check if the item is being used
	   			// -----------------------------------------------------------------
	   			if (!ShoppingTransaction.CheckItemIndex (theSelection))
	   			{
	   				PublicData.shoppingData.items.get (theSelection).Delete ();
	   				// -------------------------------------------------------------
	   				// 09/09/2017 ECU check if any items are left
	   				// -------------------------------------------------------------
	   				if (ShoppingItem.Size () == 0)
	   					return false;
	   				// -------------------------------------------------------------
	   			}
	   			else
	   			{
	   				Utilities.popToastAndSpeak (Selector.context.getString (R.string.cannot_delete_item),true); 
	   			}
	   			break;
	   		// =====================================================================
	   		case TYPE_LIST:
	   			// -----------------------------------------------------------------
	   			// 07/06/2015 ECU delete the selected list
	   			// 01/03/2016 ECU changed to use new Delete method
	   			// -----------------------------------------------------------------
	   		   	PublicData.shoppingData.lists.get (theSelection).Delete ();
	   		   	// -------------------------------------------------------------
   				// 09/09/2017 ECU check if any brands are left
   				// -------------------------------------------------------------
   				if (ShoppingList.Size () == 0)
   					return false;
   				// -------------------------------------------------------------
	   			break;
	   		// =====================================================================
	   		case TYPE_PRODUCT:
	   			// -----------------------------------------------------------------
	   			// 07/06/2015 ECU delete the selected product
	   			// 01/03/2016 ECU changed to use new Delete method
	   			//            ECU check if the product is being used
	   			// -----------------------------------------------------------------
	   			if (!ShoppingItem.CheckProductIndex(theSelection))
	   			{
	   				PublicData.shoppingData.products.get (theSelection).Delete ();
	   				// -------------------------------------------------------------
	   				// 09/09/2017 ECU check if any products are left
	   				// -------------------------------------------------------------
	   				if (ShoppingProduct.Size () == 0)
	   					return false;
	   				// -------------------------------------------------------------
	   			}
	   			else
	   			{
		   			Utilities.popToastAndSpeak (Selector.context.getString (R.string.cannot_delete_product),true); 
	   			}
	   			// -----------------------------------------------------------------
	   			break;
	   		// =====================================================================
	   		case TYPE_SHOP:
	   			// -----------------------------------------------------------------
	   			// 07/06/2015 ECU delete the selected shop
	   			// 01/03/2016 ECU changed to use the Delete method
	   			//            ECU check if the shop is being used
	   			// -----------------------------------------------------------------
	   			if (!ShoppingTransaction.CheckShopIndex (theSelection))
	   			{
	   				PublicData.shoppingData.shops.get (theSelection).Delete ();
	   				// -------------------------------------------------------------
	   				// 09/09/2017 ECU check if any shops are left
	   				// -------------------------------------------------------------
	   				if (ShoppingShop.Size () == 0)
	   					return false;
	   				// -------------------------------------------------------------
	   			}
	   			else
	   			{
		   			Utilities.popToastAndSpeak (Selector.context.getString (R.string.cannot_delete_shop),true); 
	   			}
	   			// -----------------------------------------------------------------
	   			break;
	   		// =====================================================================
	   		case TYPE_TRANSACTION:
	   			// -----------------------------------------------------------------
	   			// 07/06/2015 ECU delete the selected transaction
	   			// 01/03/2016 ECU changed to use the Delete method
	   			//            ECU check if the transaction is being used
	   			// -----------------------------------------------------------------
	   			if (!ShoppingList.CheckTransactionIndex (theSelection))
	   			{
	   				PublicData.shoppingData.transactions.get (theSelection).Delete ();
	   				// -------------------------------------------------------------
	   				// 09/09/2017 ECU check if any transactions are left
	   				// -------------------------------------------------------------
	   				if (ShoppingTransaction.Size () == 0)
	   					return false;
	   				// -------------------------------------------------------------
	   			}
	   			else
	   			{
	   				Utilities.popToastAndSpeak (Selector.context.getString (R.string.cannot_delete_transaction),true); 	
	   			}
	   			break;
	   		// =====================================================================
	   	}
		// -------------------------------------------------------------------------
		// 09/09/2017 ECU return with the result
		// -------------------------------------------------------------------------
		return result;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static String getLegend (int theType,int thePosition)
	{
		// -------------------------------------------------------------------------
		// 07/06/2019 ECU created to return the legend of the specified record
		// -------------------------------------------------------------------------
		// 07/06/2019 ECU 'switch' depending on the type of 'shopping item' that has
		//                been specified
		// -------------------------------------------------------------------------
		switch (theType)
		{
			// ---------------------------------------------------------------------
			case TYPE_BRAND:
				return PublicData.shoppingData.brands.get (thePosition).name;
			// ---------------------------------------------------------------------
			case TYPE_ITEM:
				return PublicData.shoppingData.items.get (thePosition).Print ();
		    // ---------------------------------------------------------------------
			case TYPE_LIST:
				return PublicData.shoppingData.lists.get (thePosition).Print ();
			// ---------------------------------------------------------------------
			case TYPE_PRODUCT:
				return PublicData.shoppingData.products.get (thePosition).name;
			// ---------------------------------------------------------------------
			case TYPE_SHOP:
				return PublicData.shoppingData.shops.get (thePosition).name;
			// ---------------------------------------------------------------------
			case TYPE_TRANSACTION:
				return PublicData.shoppingData.transactions.get (thePosition).Print ();
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 07/06/2019 ECU an invalid type was received so just return an indicator
		//                so that the caller knows what has happened
		// -------------------------------------------------------------------------
		return null;
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
	void Spinners (int theType)
	{
		if (theType == TYPE_ITEM)
		{
			// ---------------------------------------------------------------------
			// 05/02/2014 ECU try and picked up text changes in name field
			// ---------------------------------------------------------------------
			Spinner brandNameSpinner 	= (Spinner) findViewById (R.id.brand_name_spinner);
			// ---------------------------------------------------------------------	
			// 05/02/2014 ECU declare and set the spinner's adapter
			// 02/03/2016 ECU changed to use ReturnNames method
			// ---------------------------------------------------------------------
			String [] localNames = ShoppingBrand.ReturnNames ();
			// ---------------------------------------------------------------------
			// 31/03/2014 ECU sort the entries in the spinner
			// ---------------------------------------------------------------------
			Arrays.sort (localNames);
			// ---------------------------------------------------------------------
			// 08/06/2019 ECU Note - set up the spinner and associated adapter for
			//                the brands
			// ---------------------------------------------------------------------
			ArrayAdapter<String> brandNameAdapter = new ArrayAdapter<String>(
					this, R.layout.spinner_row_gray, R.id.spinner_textview,localNames);
								
			brandNameSpinner.setAdapter (brandNameAdapter);
			// ---------------------------------------------------------------------
			if (selection != StaticData.NO_RESULT)
			{
				// -----------------------------------------------------------------
				// 01/04/2014 ECU take into account the sorting
				// -----------------------------------------------------------------						
				brandNameSpinner.setSelection (brandNameAdapter.getPosition (PublicData.shoppingData.brands.get (PublicData.shoppingData.items.get(selection).brandIndex).name));
			}
			// ---------------------------------------------------------------------
			brandNameSpinner.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener() 
			{
				// -----------------------------------------------------------------
				public void onItemSelected (AdapterView<?> theAdapterView, View theView,int thePosition,long theId) 
				{
					// -------------------------------------------------------------
					// 31/03/2014 ECU get the appropriate index remembering that the
					//                spinner is sorted
					// -------------------------------------------------------------
					selectedBrand = ShoppingBrand.GetIndex (theAdapterView.getItemAtPosition (thePosition).toString ());	
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				public void onNothingSelected (AdapterView<?> theAdapterView) 
				{
				}
				// -----------------------------------------------------------------
			});
			// =====================================================================
			// 05/02/2014 ECU try and picked up text changes in name field
			// ---------------------------------------------------------------------
			Spinner productNameSpinner 	= (Spinner) findViewById (R.id.product_name_spinner);
			// ---------------------------------------------------------------------	
			// 05/02/2014 ECU declare and set the spinner's adapter
			// ---------------------------------------------------------------------
			localNames = ShoppingProduct.ReturnNames ();
			// ---------------------------------------------------------------------
			// 31/03/2014 ECU sort the entries in the spinner
			// ---------------------------------------------------------------------
			Arrays.sort (localNames);
			// ---------------------------------------------------------------------
			// 08/06/2019 ECU Note - set up the product spinner and its associated
			//                adapter
			// ---------------------------------------------------------------------
			ArrayAdapter<String> productNameAdapter = new ArrayAdapter<String>(
				this, R.layout.spinner_row_gray, R.id.spinner_textview,localNames);
								
			productNameSpinner.setAdapter(productNameAdapter);
			// ---------------------------------------------------------------------
			if (selection != StaticData.NO_RESULT)
			{
				// -----------------------------------------------------------------
				// 01/04/2014 ECU take into account the sorting
				// -----------------------------------------------------------------						
				productNameSpinner.setSelection (productNameAdapter.getPosition (PublicData.shoppingData.products.get (PublicData.shoppingData.items.get(selection).productIndex).name));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			productNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() 
			{
				// -----------------------------------------------------------------
				public void onItemSelected (AdapterView<?> theAdapterView, View theView,int thePosition, long theId) 
				{
					// -------------------------------------------------------------
					// 31/03/2014 ECU get the appropriate index remembering that the
					//                spinner is sorted
					// -------------------------------------------------------------
					
					selectedProduct = ShoppingProduct.GetIndex (theAdapterView.getItemAtPosition (thePosition).toString ());	
				}
				// -----------------------------------------------------------------
				public void onNothingSelected(AdapterView<?> theAdapterView) 
				{
				}
				// -----------------------------------------------------------------
			});
			// ---------------------------------------------------------------------
			// 11/01/2020 ECU add the bits to do with the barcode entry
			// ---------------------------------------------------------------------
			inputBarcode = (TextView) findViewById (R.id.input_barcode);
			if (selection != StaticData.NO_RESULT)
			{
				inputBarcode.setText (PublicData.shoppingData.items.get(selection).barcode);
			}
			// ---------------------------------------------------------------------
			// 11/01/2020 ECU define button and listener for barcode scanning
			// ---------------------------------------------------------------------
			barcodeButton = (Button) findViewById (R.id.barcode_button);
			barcodeButton.setOnClickListener (ButtonListener);
			// =====================================================================
		}
		else
		if (theType == TYPE_TRANSACTION)
		{
			// =====================================================================
			// 05/02/2014 ECU try and picked up text changes in name field
			// ---------------------------------------------------------------------
			Spinner itemSpinner 	= (Spinner) findViewById(R.id.item_spinner);
			// ---------------------------------------------------------------------		
			// 05/02/2014 ECU declare and set the spinner's adapter
			// 02/03/2016 ECU changed to use ReturnNames method
			// ---------------------------------------------------------------------
			String [] localNames = ShoppingItem.ReturnNames ();
			// ---------------------------------------------------------------------
			// 31/03/2014 ECU sort the entries in the spinner
			// ---------------------------------------------------------------------
			Arrays.sort(localNames);
			
			ArrayAdapter<String> itemAdapter = new ArrayAdapter<String>(
					this, R.layout.spinner_row_gray, R.id.spinner_textview,localNames);
									
			itemSpinner.setAdapter (itemAdapter);
			// ---------------------------------------------------------------------
			if (selection != StaticData.NO_RESULT)
			{
				// -----------------------------------------------------------------
				// 01/04/2014 ECU take into account the sorting
				// -----------------------------------------------------------------						
				itemSpinner.setSelection(itemAdapter.getPosition (PublicData.shoppingData.items.get (PublicData.shoppingData.transactions.get(selection).itemIndex).Print()));
			}
			// ---------------------------------------------------------------------
			itemSpinner.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener () 
			{
				// -----------------------------------------------------------------
				public void onItemSelected(AdapterView<?> theAdapterView, View theView,int thePosition, long theId) 
				{
					// -------------------------------------------------------------
					// 31/03/2014 ECU get the appropriate index remembering that the
					//                spinner is sorted
					// -------------------------------------------------------------
					selectedItem = ShoppingItem.GetIndex (theAdapterView.getItemAtPosition (thePosition).toString ());
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				public void onNothingSelected (AdapterView<?> theAdapterView) 
				{
				}
				// -----------------------------------------------------------------
			});
			// =====================================================================
			// 05/02/2014 ECU try and picked up text changes in name field
			// ---------------------------------------------------------------------
			Spinner shopSpinner 	= (Spinner) findViewById (R.id.shop_spinner);
			// ---------------------------------------------------------------------	
			// 05/02/2014 ECU declare and set the spinner's adapter
			// ---------------------------------------------------------------------
			localNames = ShoppingShop.ReturnNames ();
			// ---------------------------------------------------------------------
			// 31/03/2014 ECU sort the entries in the spinner
			// ---------------------------------------------------------------------
			Arrays.sort (localNames);
			// ---------------------------------------------------------------------
			// 08/06/2019 ECU Note - set up the spinner and the associated adapter
			// ---------------------------------------------------------------------
			ArrayAdapter<String> shopAdapter = new ArrayAdapter<String> (this, 
													R.layout.spinner_row_gray,
													R.id.spinner_textview,localNames);
									
			shopSpinner.setAdapter(shopAdapter);
			// ---------------------------------------------------------------------
			if (selection != StaticData.NO_RESULT)
			{
				// -----------------------------------------------------------------
				// 01/04/2014 ECU take into account the sorting
				// -----------------------------------------------------------------						
				shopSpinner.setSelection (shopAdapter.getPosition (PublicData.shoppingData.shops.get (PublicData.shoppingData.transactions.get(selection).shopIndex).name));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 08/06/2019 ECU Note - set up the listener for 'item selection'
			// ---------------------------------------------------------------------
			shopSpinner.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener () 
			{
				// -----------------------------------------------------------------
				public void onItemSelected(AdapterView<?> theAdapterView, View theView,int thePosition, long theId) 
				{
					// -------------------------------------------------------------
					// 31/03/2014 ECU get the appropriate index remembering that the
					//                spinner is sorted
					// -------------------------------------------------------------
					
					selectedShop = ShoppingShop.GetIndex (theAdapterView.getItemAtPosition (thePosition).toString ());	
				}
				// -----------------------------------------------------------------
				public void onNothingSelected (AdapterView<?> theAdapterView) 
				{
				}
				// -----------------------------------------------------------------
			});
			// =====================================================================
		}
		else
		if (theType == TYPE_LIST)
		{
			// =====================================================================
			// 05/02/2014 ECU try and picked up text changes in name field
			// ---------------------------------------------------------------------
			Spinner transactionSpinner 	= (Spinner) findViewById (R.id.transaction_spinner);
			// ---------------------------------------------------------------------			
			// 05/02/2014 ECU declare and set the spinner's adapter
			// 01/03/2016 ECU changed to use the Size method which takes into account
			//                deleted entries
			// 02/03/2016 ECU changed to use new ReturnNames method
			// ---------------------------------------------------------------------
			String [] localNames = ShoppingTransaction.ReturnNames ();
			// ---------------------------------------------------------------------
			ArrayAdapter<String> transactionAdapter = new ArrayAdapter<String> (this,
														R.layout.spinner_row_gray, 
														R.id.spinner_textview,localNames);
			// ---------------------------------------------------------------------
			// 31/03/2014 ECU sort the entries in the spinner
			// ---------------------------------------------------------------------
			Arrays.sort (localNames);
			// ---------------------------------------------------------------------
			// 08/06/2019 ECU Note - set the adapter for the associated spinner
			// ---------------------------------------------------------------------
			transactionSpinner.setAdapter (transactionAdapter);
			// ---------------------------------------------------------------------
			if (selection != StaticData.NO_RESULT)
			{
				// -----------------------------------------------------------------
				// 01/04/2014 ECU take into account the sorting
				// -----------------------------------------------------------------						
				transactionSpinner.setSelection (transactionAdapter.getPosition (PublicData.shoppingData.transactions.get (PublicData.shoppingData.lists.get(selection).transactionIndex).Print()));
				// -----------------------------------------------------------------
			}
			// ---------------------------------------------------------------------
			// 08/06/2019 ECU Note - set up the listener for 'item selection'
			// ---------------------------------------------------------------------
			transactionSpinner.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener () 
			{
				// -----------------------------------------------------------------
				public void onItemSelected (AdapterView<?> theAdapterView, View theView,int thePosition,long theId) 
				{
					// -------------------------------------------------------------
					// 31/03/2014 ECU get the appropriate index remembering that the
					//                spinner is sorted
					// -------------------------------------------------------------				
					selectedTransaction = ShoppingTransaction.GetIndex (theAdapterView.getItemAtPosition (thePosition).toString ());	
					// -------------------------------------------------------------
				}
				// -----------------------------------------------------------------
				public void onNothingSelected (AdapterView<?> theAdapterView) 
				{
				}
				// -----------------------------------------------------------------
				});
				// =================================================================
			}
	}
	// =============================================================================
	// 10/06/2015 ECU declare the methods used for the dialogue
	// -----------------------------------------------------------------------------
	public static void NoMethod (Object theSelection)
	{
	}
	// =============================================================================
	public static void YesMethod (Object theSelection)
	{
		// -------------------------------------------------------------------------
		// 10/06/2015 ECU the selected item can be deleted
		// 09/09/2017 ECU look at the returned result to determine whether the
		//                screen is to be rebuilt or the selector activity is to
		//                be terminated
		// -------------------------------------------------------------------------
		if (deleteItemConfirmed (ShoppingActivity.selectorParameter.type,(Integer) theSelection))
		{
			// ---------------------------------------------------------------------
			// 10/06/2015 ECU rebuild and then display the updated list view
			// ---------------------------------------------------------------------
			Selector.Rebuild ();
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 09/09/2017 ECU everything has been deleted so terminate the selector
			//                activity
			// ---------------------------------------------------------------------
			Selector.Finish ();
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
}

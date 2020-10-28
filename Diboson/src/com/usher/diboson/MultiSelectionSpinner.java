package com.usher.diboson;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class MultiSelectionSpinner extends Spinner implements OnMultiChoiceClickListener
{
	/* =============================================================================== */
	// ===============================================================================
	// 13/01/2014 ECU created - copied basically from internet but not happy with its
	//                standard
	// -------------------------------------------------------------------------------
	// Testing
	// =======
	//================================================================================
	/* =============================================================================== */
	ArrayAdapter<String> simpleAdapter;
	boolean [] selectedItems = null;
	String [] selectedItemString = null;
	/* =============================================================================== */
	public MultiSelectionSpinner (Context theContext)
	{
		super (theContext);
		
		simpleAdapter = new ArrayAdapter <String> (theContext,R.layout.spinner_textview);
	
		super.setAdapter (simpleAdapter);
		
	}
	/* =============================================================================== */
	public MultiSelectionSpinner (Context theContext, AttributeSet theAttributes)
	{
		super (theContext,theAttributes);
		
		simpleAdapter = new ArrayAdapter <String> (theContext,R.layout.spinner_textview);
		
		super.setAdapter (simpleAdapter);
	}
	/* =============================================================================== */
	public void onClick(DialogInterface dialog, int which, boolean isChecked) 
	{  
		if (selectedItems != null && which < selectedItems.length) 
		{  
			selectedItems[which] = isChecked;  
			simpleAdapter.clear();  
			simpleAdapter.add(buildSelectedItemString());  
		} 
		else 
		{  
			throw new IllegalArgumentException(  
					"Argument 'which' is out of bounds.");  
		}  
	} 
	/* =============================================================================== */
	private String buildSelectedItemString() 
	{  
		StringBuilder localStringBuilder = new StringBuilder();  
		boolean foundOne = false;  
	   
		for (int i = 0; i < selectedItemString.length; ++i) 
		{  
			if (selectedItems[i]) 
		 	{  
				if (foundOne) 
		 		{  
					localStringBuilder.append(", ");  
		 		} 
		 		 
		 		foundOne = true;  
	    
		 		localStringBuilder.append(selectedItemString[i]);  
		 	}  
		} 
	    return localStringBuilder.toString();  
	}
	/* =============================================================================== */ 
	public List<Integer> getSelectedIndicies() 
	{  
		List<Integer> selection = new LinkedList<Integer>();  
		for (int i = 0; i < selectedItemString.length; ++i) 
		{  
			if (selectedItems[i]) 
			{  
				selection.add(i);  
			} 
		}  
		return selection;  
	}  		  
	/* =============================================================================== */
	public String getSelectedItemsAsString() 
	{  
		// 13/01/2014 ECU build up a string that can be displayed in the field
		   
		StringBuilder localStringBuilder = new StringBuilder();  
		boolean foundASelectedItem = false;  
		  
		for (int theIndex = 0; theIndex < selectedItemString.length; ++theIndex) 
		{  
			if (selectedItems[theIndex]) 
			{  
				if (foundASelectedItem) 
				{  
					localStringBuilder.append(", ");  
				} 
				foundASelectedItem = true;  
				localStringBuilder.append(selectedItemString[theIndex]);  
			}  
		}  
		return localStringBuilder.toString();  
	}  
	/* =============================================================================== */
	public boolean [] getSelection ()
	{
		// 13/01/2014 ECU return the boolean array with selection results
		  
		return selectedItems;
	}
	/* =============================================================================== */
	public List<String> getSelectedStrings() 
	{  
		List<String> selection = new LinkedList<String>();  
		for (int i = 0; i < selectedItemString.length; ++i) 
		{  
			if (selectedItems[i]) 
			{  
				selection.add(selectedItemString[i]);  
			}  
		}  
		return selection;  
	}  
	/* =============================================================================== */	
	@Override  
	public boolean performClick() 
	{  
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());  
		builder.setMultiChoiceItems(selectedItemString, selectedItems, this);  
		builder.show();  
		return true;  
	}  
	/* =============================================================================== */
	@Override  
	public void setAdapter(SpinnerAdapter adapter) 
	{  
		throw new RuntimeException(  
				"setAdapter is not supported by MultiSelectSpinner.");  
	} 
	/* =============================================================================== */
	public void setItems(String[] items) 
	{  
		selectedItemString	= items;  
		selectedItems 		= new boolean[selectedItemString.length];  
		simpleAdapter.clear();  
		simpleAdapter.add(selectedItemString[0]);  
		Arrays.fill(selectedItems, false);  
	}  
	/* =============================================================================== */	
	public void setItems(List<String> items) 
	{  
		selectedItemString = items.toArray(new String[items.size()]);  
		selectedItems 	   = new boolean[selectedItemString.length];  
		simpleAdapter.clear();  
		simpleAdapter.add(selectedItemString[0]);  
		Arrays.fill(selectedItems, false);  
	}  
	/* =============================================================================== */
	public void setSelection(String[] selection) 
	{  
		for (String cell : selection)
		{  
			for (int theIndex = 0; theIndex < selectedItemString.length; ++theIndex) 
			{  
				if (selectedItemString[theIndex].equals(cell)) 
				{  
					selectedItems[theIndex] = true;  
				}  
			}  
		}  
	}  
	/* =============================================================================== */	
	public void setSelection(List<String> selection) 
	{  
		for (int theIndex = 0; theIndex < selectedItems.length; theIndex++) 
		{  
			selectedItems[theIndex] = false;  
		}
		 
		for (String sel : selection) 
		{  
			for (int theIndex = 0; theIndex < selectedItemString.length; ++theIndex) 
			{  
				if (selectedItemString[theIndex].equals(sel)) 
				{  
					selectedItems[theIndex] = true;  
				}  
			}  
		}  
		 
		// 14/01/2014 ECU redo the display
		 
		simpleAdapter.clear();  
		simpleAdapter.add(buildSelectedItemString());  
	}  
	/* =============================================================================== */
	public void setSelection(int index) 
	{  
		for (int i = 0; i < selectedItems.length; i++) 
		{  
			selectedItems[i] = false;  
		}  
		if (index >= 0 && index < selectedItems.length) 
		{  
			selectedItems[index] = true;  
		}
		else 
		{  
			throw new IllegalArgumentException("Index " + index + " is out of bounds.");  
		}  
		  
		// 14/01/2014 ECU redo the display
		  
		simpleAdapter.clear();  
		simpleAdapter.add(buildSelectedItemString());  
	}   
	/* =============================================================================== */
	public void setSelection(int[] selectedIndicies) 
	{  
		for (int i = 0; i < selectedItems.length; i++) 
		{  
			selectedItems[i] = false;  
		}
		  
		for (int index : selectedIndicies) 
		{  
			if (index >= 0 && index < selectedItems.length) 
			{  
				selectedItems[index] = true;  
			} 
			else 
			{  
				throw new IllegalArgumentException("Index " + index  + " is out of bounds.");  
			}     
		}  
		  
		simpleAdapter.clear();  
		simpleAdapter.add(buildSelectedItemString());  
	} 		
	/* =============================================================================== */
	public void setSelection(boolean [] theSelectionFlags) 
	{  
		// 13/01/2014 ECU build according to the supplied flags
		//            ECU put in the null option
		  
		if (theSelectionFlags != null)
		{
			selectedItems = theSelectionFlags;
		}
		else
		{
			for (int theIndex = 0; theIndex < selectedItems.length; theIndex++)
				selectedItems [theIndex] = false;
		}
		  
		// 14/01/2014 ECU redo the display
		  
		simpleAdapter.clear();  
		simpleAdapter.add(buildSelectedItemString());  
	} 		
	/* =============================================================================== */ 
}



 









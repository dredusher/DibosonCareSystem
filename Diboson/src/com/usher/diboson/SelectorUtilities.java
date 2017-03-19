package com.usher.diboson;

import android.content.Context;
import android.content.Intent;

public class SelectorUtilities 
{
	// =============================================================================
	public static SelectorParameter selectorParameter = new SelectorParameter ();
	// =============================================================================
	
	// =============================================================================
	static void Initialise ()
	{
		// -------------------------------------------------------------------------
		// 22/11/2015 ECU create to initialise the stored 'selectorParameter'
		// -------------------------------------------------------------------------
		selectorParameter = new SelectorParameter ();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void StartSelector (Context	theContext,
							   MethodDefinition<?>theMethodDefinition,
							   MethodDefinition<?>theImageHandlerDefinition,
							   MethodDefinition<?>theSwipeHandlerDefinition,
							   int		theObjectType)
	{
		// -------------------------------------------------------------------------
		// 30/03/2014 ECU start up the selector 
		// -------------------------------------------------------------------------
		Intent intent = new Intent (theContext,Selector.class);
		intent.putExtra (StaticData.PARAMETER_OBJECT_TYPE,theObjectType);
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
			intent.putExtra (StaticData.PARAMETER_METHOD,
							 theMethodDefinition);
		}
		// --------------------------------------------------------------------------
		// 13/06/2015 ECU check if a handler for the image has been supplied
		// --------------------------------------------------------------------------
		if (theImageHandlerDefinition != null)
		{
			intent.putExtra (StaticData.PARAMETER_IMAGE_HANDLER,
						     theImageHandlerDefinition);
		}
		// -------------------------------------------------------------------------
		// 09/06/2015 ECU set up the swipe method
		// -------------------------------------------------------------------------
		if (theSwipeHandlerDefinition != null)
		{
			intent.putExtra (StaticData.PARAMETER_SWIPE_METHOD,
					         theSwipeHandlerDefinition);
		}
		// -------------------------------------------------------------------------
		// 08/03/2016 ECU check if need to start as a new task
		// -------------------------------------------------------------------------
		if (selectorParameter.newTask)
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		// -------------------------------------------------------------------------
		theContext.startActivity (intent);
		// --------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	static void StartSelector (Context theContext,
							   MethodDefinition<?>theMethodDefinition,
							   int theObjectType)
	{
		// -------------------------------------------------------------------------
		// 30/03/2014 ECU start up the selector 
		// 13/06/2015 ECU changed to use the new master method
		// -------------------------------------------------------------------------
		StartSelector (theContext,
					   theMethodDefinition,
					   null,
					   selectorParameter.swipeMethodDefinition,
					   theObjectType);
		// -------------------------------------------------------------------------
	}
	/* ----------------------------------------------------------------------------- */
	static void StartSelector (Context theContext,int theObjectType)
	{
		// -------------------------------------------------------------------------
		// 05/06/2015 ECU changed the method from 'null' to '....SelectMethod'
		// 07/06/2015 ECU revert to 'null' because logic for deletion was changed
		// -------------------------------------------------------------------------
		StartSelector (theContext,null,theObjectType);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

package com.usher.diboson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.content.Context;
import android.content.Intent;

public class CarePlanActivity extends DibosonActivity 
{
	/* ============================================================================= */
	// =============================================================================
	// 12/01/2014 ECU created
	// 30/08/2015 ECU modified as no longer needed as a standalone activity
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	// 30/03/2016 ECU use hashCodes to determine whether the data has changed and should
	//                be written to disk. Am aware that this is not perfect but good 
	//                first attempt.
	// -----------------------------------------------------------------------------
	// Testing
	// =======
	//==============================================================================
	//final static String TAG = "CarePlanActivity";
	/* ============================================================================= */
	public static int initialHashCode;						// 30/03/2016 ECU added
	// =============================================================================


	// =============================================================================
	public static void AddVisit (int thePosition)
	{
		// -------------------------------------------------------------------------
		// 29/08/2015 ECU start up the activity which will add the visit
		// -------------------------------------------------------------------------
		Intent myIntent = new Intent (CarerSystemActivity.context,CarePlanVisitActivity.class);
		myIntent.putExtra (StaticData.PARAMETER_TYPE,StaticData.OBJECT_CARE_VISITS);
		myIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		CarerSystemActivity.context.startActivity (myIntent);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static ArrayList<ListItem> BuildTheDailyCarePlanList ()
	{
		SelectorUtilities.selectorParameter.listItems = new ArrayList<ListItem>();	
		// -------------------------------------------------------------------------
		// 29/08/2015 ECU build up the summary of visits per day
		// 11/12/2016 ECU changed to use daysOfWeek
		// 20/03/2017 ECU changed to use BLANK....
		// -------------------------------------------------------------------------
		for (int theDayIndex = 0; theDayIndex < PublicData.daysOfTheWeek.length; theDayIndex++)
		{
			SelectorUtilities.selectorParameter.listItems.add (new ListItem (
					StaticData.BLANK_STRING,
					PublicData.daysOfTheWeek [theDayIndex],
					StaticData.BLANK_STRING,
					"Number of Visits = " + PublicData.carePlan.visits [theDayIndex].size(),
					theDayIndex));
		}
		// -------------------------------------------------------------------------
		return SelectorUtilities.selectorParameter.listItems;
	}
	// =============================================================================
	public static ArrayList<ListItem> BuildTheDailyCarePlanVisitsList ()
	{
		SelectorUtilities.selectorParameter.listItems = new ArrayList<ListItem>();	
		// -------------------------------------------------------------------------
		// 29/08/2015 ECU build up the summary of visits on the day which is held
		//                in SelectorUtilities.selectorParameter.dataObject
		// -------------------------------------------------------------------------
		int	localDay = (Integer) SelectorUtilities.selectorParameter.dataObject;
		// -------------------------------------------------------------------------
		List<CarePlanVisit> localVisits = PublicData.carePlan.visits [localDay]; 
		// -------------------------------------------------------------------------
		// 29/08/2015 ECU now display the visit details
		// -------------------------------------------------------------------------
		for (int theVisit = 0; theVisit < localVisits.size(); theVisit++)
		{
			SelectorUtilities.selectorParameter.listItems.add (new ListItem (
					StaticData.BLANK_STRING + localVisits.get (theVisit).PrintStartTime(),
					"Duration : " + localVisits.get (theVisit).duration + " minutes",
					"Carer :  " + PublicData.carers.get(localVisits.get (theVisit).carerIndex).name +
					" from " + PublicData.agencies.get(localVisits.get (theVisit).agencyIndex).name,
					theVisit));
		}
		// ---------------------------------------------------------------------
		// 29/08/2015 ECU make sure that the list is sorted - by time of visit
		// ---------------------------------------------------------------------
		Collections.sort (SelectorUtilities.selectorParameter.listItems);
		// ---------------------------------------------------------------------
		return SelectorUtilities.selectorParameter.listItems;
	}
	// =============================================================================
	public static List<CarePlanVisit> generateSummary (long theDate)
	{
		// -------------------------------------------------------------------------
		// 28/02/2017 ECU created to return a summary of care plan for the specified
		//                date
		// -------------------------------------------------------------------------
		return PublicData.carePlan.visits [Utilities.DayOfWeek (theDate)];
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void HandleDailyCarePlanList (Context theContext,boolean theStartActivityFlag)
	{
		// -------------------------------------------------------------------------
		// 24/07/2019 ECU make sure that the 'selector' parameter is initialised
		// -------------------------------------------------------------------------
		SelectorUtilities.Initialise ();
		// -------------------------------------------------------------------------
		BuildTheDailyCarePlanList ();
		SelectorUtilities.selectorParameter.rowLayout 				= R.layout.care_plan_row;
		SelectorUtilities.selectorParameter.sort 					= false;
		SelectorUtilities.selectorParameter.type 					= StaticData.OBJECT_CARE_PLANS;
		SelectorUtilities.selectorParameter.classToRun				= CarePlanVisitActivity.class;
		SelectorUtilities.selectorParameter.swipeMethodDefinition	= null;				// 30/08/2015 ECU added
		// ---------------------------------------------------------------------
		// 24/07/2019 ECU declare the help function
		// ---------------------------------------------------------------------
		SelectorUtilities.selectorParameter.helpMethodDefinition 	
						= new MethodDefinition<CarePlanActivity> (CarePlanActivity.class,"HelpButtonListAction");
		// ----------------------------------------------------------------------
		// 31/08/2015 ECU check if the activity is to be started or just want a
		//                rebuild of the display
		// ----------------------------------------------------------------------
		if (theStartActivityFlag)
		{
			// ------------------------------------------------------------------
			// 31/08/2015 ECU start the selector activity
			// ------------------------------------------------------------------
			SelectorUtilities.StartSelector (theContext,
										 new MethodDefinition<CarePlanActivity> (CarePlanActivity.class,"SelectAction"),
										 StaticData.OBJECT_CARE_PLANS);
			// --------------------------------------------------------------------
		}
		else
		{
			// --------------------------------------------------------------------
			// 31/08/2015 ECU just rebuild the care plan display
			// --------------------------------------------------------------------
			Selector.SetFromSelectorParameter (SelectorUtilities.selectorParameter);
			Selector.Rebuild ();
			// --------------------------------------------------------------------
		}
		// ------------------------------------------------------------------------
	}
	// =============================================================================
	public static void HandleDailyCarePlanVisitsList (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 24/07/2019 ECU normally it would be expected to initialise the 'selector'
		//                parameter, using SelectorUtilities.Initialise, but this
		//                method relies on previous information being retained
		// -------------------------------------------------------------------------
		BuildTheDailyCarePlanVisitsList ();
		SelectorUtilities.selectorParameter.rowLayout 				= R.layout.care_plan_visit_row;
		SelectorUtilities.selectorParameter.sort 					= true;
		SelectorUtilities.selectorParameter.type 					= StaticData.OBJECT_CARE_VISITS;
		SelectorUtilities.selectorParameter.classToRun				= CarePlanVisitActivity.class;
		SelectorUtilities.selectorParameter.customMethodDefinition 	= new MethodDefinition<CarePlanActivity> (CarePlanActivity.class,"AddVisit");
		SelectorUtilities.selectorParameter.customLegend 			= theContext.getString (R.string.add);
		SelectorUtilities.selectorParameter.swipeMethodDefinition	= new MethodDefinition<CarePlanActivity> (CarePlanActivity.class,"SwipeAction");
		// ---------------------------------------------------------------------
		// 24/07/2019 ECU declare the help function
		// ---------------------------------------------------------------------
		SelectorUtilities.selectorParameter.helpMethodDefinition 	
						= new MethodDefinition<CarePlanActivity> (CarePlanActivity.class,"HelpButtonAction");
		// ----------------------------------------------------------------------
		SelectorUtilities.StartSelector (theContext,
										 new MethodDefinition<CarePlanActivity> (CarePlanActivity.class,"SelectActionVisit"),
										 StaticData.OBJECT_CARE_VISITS);
		// ------------------------------------------------------------------------
	}
	// =============================================================================
	public static void HelpButtonAction (int thePosition)
	{
		// -------------------------------------------------------------------------
		// 24/07/2019 ECU created to process the 'help button'
		// -------------------------------------------------------------------------
		Utilities.popToast (PublicData.carePlan.visits [(Integer) SelectorUtilities.selectorParameter.dataObject].get (thePosition).Print(),true);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void HelpButtonListAction (int thePosition)
	{
		// -------------------------------------------------------------------------
		// 24/07/2019 ECU created to process the 'help button'
		// 25/07/2019 ECU changed to display a scrollable 'popToast' based on
		//                the current context
		// 26/07/2019 ECU added 'mono spaced'
		// 27/07/2019 ECU specify the number of lines to use
		// -------------------------------------------------------------------------
		String carerString = StaticData.MONO_SPACED + PublicData.carePlan.Print (thePosition);
		Utilities.popToast (Utilities.getRootView (Selector.context),
							carerString,
							Utilities.getNumberOfLines (carerString,StaticData.SCROLL_FIELD_LINES_MAX),
							true);
		// -------------------------------------------------------------------------
	}
	// ===============================================================================
    public static void SelectAction (int theDaySelected)
    {
    	// -------------------------------------------------------------------------
    	//29/08/2015 ECU created to handle the selection of an item
    	//           ECU at the moment just call the care
    	// -------------------------------------------------------------------------
    	SelectorUtilities.selectorParameter.dataObject = theDaySelected;
    	// -------------------------------------------------------------------------
    	// 29/08/2015 ECU check if there are any visits for this day
    	// -------------------------------------------------------------------------
    	if (PublicData.carePlan.visits[theDaySelected].size() > 0)
    	{
    		HandleDailyCarePlanVisitsList (Selector.context);
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 29/08/2015 ECU there are no visits for this day so create a visit
    		// ---------------------------------------------------------------------
    		AddVisit (0);
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------   
    }
 // ===============================================================================
    public static void SelectActionVisit (int theVisitSelected)
    {
    	// -------------------------------------------------------------------------
    	//29/08/2015 ECU created to handle the selection of an item
    	//           ECU at the moment just call the care
    	//			 ECU added PARAMETER_SELECTION
    	// -------------------------------------------------------------------------
    	int localDay = (Integer) SelectorUtilities.selectorParameter.dataObject;
    	Intent myIntent = new Intent (Selector.context,CarePlanActivity.class);
		myIntent.putExtra(StaticData.PARAMETER_DAY,localDay);
		myIntent.putExtra(StaticData.PARAMETER_SELECTION,theVisitSelected);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Selector.context.startActivity (myIntent);
    	// -------------------------------------------------------------------------   
    }
	// ============================================================================= 
    public static void SwipeAction (int thePosition)
    {
    	// -------------------------------------------------------------------------
    	// 09/06/2015 ECU created to handle swipe actions on a list view item
    	// -------------------------------------------------------------------------
    	// 30/08/2015 ECU get the relevant day from the selectorParamter
    	// -------------------------------------------------------------------------
       	int localDay = (Integer) SelectorUtilities.selectorParameter.dataObject;
    	// -------------------------------------------------------------------------
		// 10/06/2015 ECU created to initiate the dialogue
		// -------------------------------------------------------------------------
		DialogueUtilities.yesNo (Selector.context,"Item Deletion",
	    		   "Do you really want to delete the entry for '" + PublicData.carePlan.visits[localDay].get (thePosition).PrintStartTime() + "'",
	    		   (Object) thePosition,
	    		   Utilities.createAMethod (CarePlanActivity.class,"YesMethod",(Object) null),
	    		   Utilities.createAMethod (CarePlanActivity.class,"NoMethod",(Object) null)); 
		// -------------------------------------------------------------------------  
    }
    // =============================================================================
    public static void WriteCarePlanToDisk (Context theContext)
    {
    	// -------------------------------------------------------------------------
    	// 29/08/2015 ECU created to write the care plan to disk
    	// -------------------------------------------------------------------------
    	// 30/03/2016 ECU only write if the hashcode's indicate a data change
    	// -------------------------------------------------------------------------
    	if (initialHashCode != PublicData.carePlan.HashCode())
    	{
    		// ---------------------------------------------------------------------
    		AsyncUtilities.writeObjectToDisk (PublicData.projectFolder + 
    				theContext.getString (R.string.care_plan_file),PublicData.carePlan);
    		// ---------------------------------------------------------------------
    		// 30/08/2015 ECU send the confirmation message
    		// ---------------------------------------------------------------------
    		Utilities.SendEmailMessage (theContext,"Updated Care Plan",PublicData.carePlan.Print());
    		// ---------------------------------------------------------------------
    		// 02/10/2016 ECU make sure any alarms are updated
    		// ---------------------------------------------------------------------
			DailyScheduler.ProcessCarePlanVisits (theContext);
    		// ---------------------------------------------------------------------
    	}
    	// -------------------------------------------------------------------------
    }
    // =============================================================================
    
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
   		// -------------------------------------------------------------------------
   		int localSelection = (Integer) theSelection;
   		int localDay	   = (Integer) SelectorUtilities.selectorParameter.dataObject;
   		PublicData.carePlan.visits [localDay].remove (localSelection);
   		// -------------------------------------------------------------------------
   		// 29/08/2015 ECU update the details on disk
   		// -------------------------------------------------------------------------
   		WriteCarePlanToDisk (Selector.context);
   		// -------------------------------------------------------------------------
   		// 31/08/2015 ECU check whether there are any more visits to display - if not
   		//                then show the care plan
   		// -------------------------------------------------------------------------
   		if (PublicData.carePlan.visits [localDay].size() > 0)
   		{
   			// ---------------------------------------------------------------------
   			// 10/06/2015 ECU rebuild and then display the updated list view
   			// ---------------------------------------------------------------------
   			Selector.Rebuild ();
   		}
   		else
   		{
   			// ---------------------------------------------------------------------
   			// 31/08/2015 ECU there are no visits remaining for the current day
   			//            ECU added the 'false' to indicate that the Selector
   			//                activity is not to be started - just want a rebuild of
   			//                the display
   			// ---------------------------------------------------------------------
   			HandleDailyCarePlanList (Selector.context,false);
   			// ---------------------------------------------------------------------
   		}
   	}
   	// =============================================================================
}

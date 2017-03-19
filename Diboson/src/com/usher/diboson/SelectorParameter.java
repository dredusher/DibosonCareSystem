package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;

public class SelectorParameter implements Serializable
{
	/* ============================================================================= */
	private static final long serialVersionUID = 1L;
	/* ============================================================================= */
	// 05/06/2015 ECU added the 'selectMethodDefinition'
	// 10/06/2015 ECU added 'index' and 'name'
	// 13/06/2015 ECU added 'sort' which is defaulted to true
	// 29/08/2015 ECU added 'dataObject'
	// 21/11/2015 ECU added 'helpMethodDefinition' and 'selectMethodDefinition'
	//            ECU added 'longSelectMethodDefinition'
	// 22/11/2015 ECU added 'backMethodDefinition'
	// 08/03/2016 ECU added 'newTask'
	//            ECU added 'finishOnSelect'
	// 05/08/2016 ECU added 'drawableInitial'
	// 18/10/2016 ECU added 'editMethodDefinition'
	// =============================================================================
	public MethodDefinition<?> backMethodDefinition 	= null;
	public Class <?> 		   classToRun;
	public String			   customLegend 			= null;
	public MethodDefinition<?> customMethodDefinition 	= null;
	public Object			   dataObject				= null;
	public int				   drawableInitial			= StaticData.NO_RESULT;
	public MethodDefinition<?> editMethodDefinition 	= null;
	public boolean			   finishOnSelect			= true;
	public MethodDefinition<?> helpMethodDefinition		= null;
	public int				   index					= StaticData.NO_RESULT;
	public ArrayList<ListItem> listItems				= new ArrayList<ListItem>();  
	public MethodDefinition<?> longSelectMethodDefinition
														= null;
	public String			   name  					= null;
	public boolean			   newTask					= false;
	public int				   rowLayout 				= 0;
	public MethodDefinition<?> selectMethodDefinition	= null;
	public boolean			   sort					    = true;
	public MethodDefinition<?> swipeMethodDefinition 	= null;
	public int				   type 					= StaticData.NO_RESULT;
	/* ============================================================================= */
}

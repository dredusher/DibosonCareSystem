package com.usher.diboson;

public class IRFunction 
{
	/* ======================================================================= */
	// 20/12/2013 ECU added
	//            ECU added drawable
	/* ======================================================================= */
	public	int		function;
	public	String	meaning;
	public  int		drawable;
	/* ======================================================================= */
	public IRFunction (int theFunction,String theMeaning,int theDrawable)
	{
		drawable    = theDrawable;
		function	= theFunction;
		meaning		= theMeaning;
	}
	/* ======================================================================= */
}

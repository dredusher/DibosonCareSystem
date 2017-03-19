package com.usher.diboson;

public class SpokenPhrase 
{
	/* ================================================================= */
	// 20/02/2014 ECU created to contain arguments passed to the
	//                text to speech service
	// 03/06/2016 ECU added 'actionFlag' to indicate if being called from
	//                within an action
	/* ================================================================= */
	public  boolean	actionFlag	= false;
	public	String	phrase		= null;
	public	int		silence		= StaticData.NO_RESULT;
	/* ================================================================= */
	public SpokenPhrase (String thePhrase, int thePeriodOfSilence)
	{
		phrase		= thePhrase;
		silence		= thePeriodOfSilence;
	}
	/* ----------------------------------------------------------------- */
	public SpokenPhrase (String thePhrase)
	{
		phrase		= thePhrase;
		silence		= StaticData.NO_RESULT;
	}
	/* ----------------------------------------------------------------- */
	public SpokenPhrase (String thePhrase,boolean theActionFlag)
	{
		// -------------------------------------------------------------
		// 03/06/2016 ECU created to set the action flag
		// -------------------------------------------------------------
		actionFlag	= theActionFlag;
		phrase		= thePhrase;
		silence		= StaticData.NO_RESULT;
		// --------------------------------------------------------------
	}
	/* ----------------------------------------------------------------- */
	public SpokenPhrase (int thePeriodOfSilence)
	{
		phrase		= null;
		silence		= thePeriodOfSilence;
	}
	/* ================================================================= */
}

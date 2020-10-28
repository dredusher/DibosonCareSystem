package com.usher.diboson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;

public class VoiceCommandPhrases implements Serializable 
{
	// =============================================================================
	// 21/05/2016 ECU created to hold user defined phrases used in the voice command
	//                activity and the associated actions
	// -----------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	String		actions;
	String []	phrases;
	// =============================================================================
	
	// =============================================================================
	public VoiceCommandPhrases (String [] theSpokenPhrases,String theActions)
	{
		// -------------------------------------------------------------------------
		// 21/05/2016 ECU created to define the object with the specified arguments
		// -------------------------------------------------------------------------
		actions		= theActions;
		phrases		= theSpokenPhrases;
		// -------------------------------------------------------------------------
		// 04/06/2016 ECU want to check if this spoken phrase already exists
		// -------------------------------------------------------------------------
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public VoiceCommandPhrases (String theSpokenPhrasesAsString,String theActions)
	{
		// -------------------------------------------------------------------------
		// 21/05/2016 ECU created to define the object when the spoken phrases are
		//                supplied in a single string with newlines as the separator
		// -------------------------------------------------------------------------
		this (theSpokenPhrasesAsString.split (StaticData.NEWLINE),theActions);
		// -------------------------------------------------------------------------
	}
	// ============================================================================= 
	
	// =============================================================================
	public int checkForAMatch (String theStringToMatch)
	{
		// -------------------------------------------------------------------------
		// 04/06/2016 ECU created to check if any of the stored phrases for this
		//                object start with the specified string
		// 06/06/2016 ECU as all comparisons are to be 'case insensitive' then work
		//                in lower case
		// -------------------------------------------------------------------------
		String localStringToMatch = theStringToMatch.toLowerCase(Locale.getDefault());
		// -------------------------------------------------------------------------
		for (int storedPhrase = 0; storedPhrase < phrases.length; storedPhrase++)
		{
			// ---------------------------------------------------------------------
			// 04/06/2016 ECU check for starting match and indicate if found
			//            ECU use lower case because basically want to ignore case
			// 06/06/2016 ECU changed the search criteria fro 'startsWith' to
			//                'contains'
			// ---------------------------------------------------------------------
			if ((phrases [storedPhrase].toLowerCase(Locale.getDefault())).contains (localStringToMatch))
				return storedPhrase;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 04/06/2016 ECU no match found
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static List<int []> checkAllForAMatch (String theStringToMatch)
	{
		// -------------------------------------------------------------------------
		// 04/06/2016 ECU created to check all stored phrases for a match
		// -------------------------------------------------------------------------
		List<int []> results = new ArrayList<int []>();
		// -------------------------------------------------------------------------
		if (PublicData.voiceCommandPhrases.size () > 0)
		{
			// ---------------------------------------------------------------------
			// 21/05/2016 ECU loop through all user defined phrases looking for a
			// 				  match
			// ---------------------------------------------------------------------
			for (int thePhraseIndex = 0; thePhraseIndex < PublicData.voiceCommandPhrases.size (); thePhraseIndex++)
			{
				// -----------------------------------------------------------------
				// 21/05/2016 ECU check if any of the stored phrases matched
				// -----------------------------------------------------------------
				int matchingPhrase = PublicData.voiceCommandPhrases.get (thePhraseIndex).checkForAMatch (theStringToMatch);
				if (matchingPhrase != StaticData.NO_RESULT)
				{
					// -------------------------------------------------------------
					// 04/06/2016 ECU add the results into the list
					// -------------------------------------------------------------
					results.add (new int [] {thePhraseIndex,matchingPhrase});
					// -------------------------------------------------------------
				}
			}
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 04/06/2016 ECU no matches found so indicate the fact
		// -------------------------------------------------------------------------
		return results;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public boolean checkIfAllExists (String [] thePhrasesToCheck)
	{
		// -------------------------------------------------------------------------
		// 04/06/2016 ECU created to check if this object has a phrase which matches
		//                one of those supplied as an argument
		// -------------------------------------------------------------------------
		for (int inputPhrase = 0; inputPhrase < thePhrasesToCheck.length; inputPhrase++)
		{
			for (int storedPhrase = 0; storedPhrase < phrases.length; storedPhrase++)
			{
				// -----------------------------------------------------------------
				// 04/06/2016 ECU check for a match
				// -----------------------------------------------------------------
				if (thePhrasesToCheck [inputPhrase].equalsIgnoreCase (phrases [storedPhrase]))
				{
					// -------------------------------------------------------------
					// 04/06/2016 ECU a match has been found
					// -------------------------------------------------------------
					return true;
					// -------------------------------------------------------------
				}	
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 04/06/2016 ECU no match found
		// -------------------------------------------------------------------------
		return false;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public boolean checkIfAllExists (String thePhrasesToCheckAsString)
	{
		// -------------------------------------------------------------------------
		// 04/06/2016 ECU created to handle when phrases are specified in a string
		// -------------------------------------------------------------------------
		return checkIfAllExists (thePhrasesToCheckAsString.split (StaticData.NEWLINE));
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String phrasesAsString ()
	{
		// -------------------------------------------------------------------------
		// 04/06/2016 ECU created to return the stored phrases as a NEWLINE delimited
		//                string
		// -------------------------------------------------------------------------
		String localString = StaticData.BLANK_STRING;
		for (int phraseIndex = 0; phraseIndex < phrases.length; phraseIndex++)
		{
			localString += phrases [phraseIndex] + StaticData.NEWLINE;
		}
		// -------------------------------------------------------------------------
		// 04/06/2016 ECU return the string less the terminating NEWLINE
		// -------------------------------------------------------------------------
		return localString.substring(0,localString.length()-1);
		// -------------------------------------------------------------------------

	}
	// =============================================================================
	public String Print ()
	{
		// -------------------------------------------------------------------------
		// 21/05/2016 ECU created to print the contents of the object
		// -------------------------------------------------------------------------
		String localString = StaticData.BLANK_STRING;
		
		for (int phraseIndex = 0; phraseIndex < phrases.length; phraseIndex++)
		{
			localString += "phrase " + phraseIndex + " : " + phrases [phraseIndex] + StaticData.NEWLINE;
		}
		
		return localString + ("actions : " + actions);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String Print (int thePhraseIndex)
	{
		// -------------------------------------------------------------------------
		// 04/06/2016 ECU created to print a specific phrase and the actions
		// 05/06/2016 ECU tidy up the formatting
		// -------------------------------------------------------------------------
		return phrases [thePhraseIndex] + StaticData.NEWLINE + StaticData.NEWLINE +
			   "     actions : " + actions + StaticData.NEWLINE;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public boolean SearchForPhrase (Context theContext,String thePhraseToMatch)
	{
		// -------------------------------------------------------------------------
		// 21/05/2016 ECU search the user defined spoken phrases to see if there is
		//                a match to the specified phrase. If there is then action
		// 				  the defined actions (and return true) or indicate if there
		//                is no match then return false
		// -------------------------------------------------------------------------
		for (int thePhrase = 0; thePhrase < phrases.length; thePhrase++)
		{
			// ---------------------------------------------------------------------
			// 21/05/2016 ECU now check for the match
			// ---------------------------------------------------------------------
			if (phrases [thePhrase].equalsIgnoreCase (thePhraseToMatch))
			{
				// -----------------------------------------------------------------
				// 21/05/2016 ECU match found so process the associated actions
				// -----------------------------------------------------------------
				Utilities.actionHandler (theContext,actions);
				// -----------------------------------------------------------------
				// 21/05/2016 ECU and indicate that processing has taken place
				// -----------------------------------------------------------------
				return true;
				// -----------------------------------------------------------------
			}
		}
		// -------------------------------------------------------------------------
		// 21/05/2016 ECU indicate that no match found
		// -------------------------------------------------------------------------
		return false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public static String PrintAll ()
	{
		// -------------------------------------------------------------------------
		// 21/05/2016 ECU print for all spoken phrases
		// -------------------------------------------------------------------------
		String localString = StaticData.BLANK_STRING;
		for (int phraseIndex = 0; phraseIndex < PublicData.voiceCommandPhrases.size(); phraseIndex++)
		{
			localString += PublicData.voiceCommandPhrases.get (phraseIndex).Print () + StaticData.NEWLINE + StaticData.NEWLINE;
					         
		}
		// -------------------------------------------------------------------------
		return localString;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean SearchAllPhrasesForMatch(Context theContext,String thePhraseToMatch)
	{
		// -------------------------------------------------------------------------
		// 21/05/2016 ECU created to search all stored user defined spoken phrases
		//                looking for a match
		// -------------------------------------------------------------------------
		if (PublicData.voiceCommandPhrases.size () > 0)
		{
			// ---------------------------------------------------------------------
			// 21/05/2016 ECU loop through all user defined phrases looking for a
			// 				  match
			// ---------------------------------------------------------------------
			for (int thePhraseIndex = 0; thePhraseIndex < PublicData.voiceCommandPhrases.size (); thePhraseIndex++)
			{
				// -----------------------------------------------------------------
				// 21/05/2016 ECU check if any of the stored phrases matched
				// -----------------------------------------------------------------
				if (PublicData.voiceCommandPhrases.get (thePhraseIndex).SearchForPhrase (theContext,thePhraseToMatch))
					return true;
			}
			// ---------------------------------------------------------------------
			// 21/05/2016 ECU no matches found so indicate the fact
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 21/05/2016 ECU nothing defined so indicate that fact
			// ---------------------------------------------------------------------
			return false;
			// ---------------------------------------------------------------------
		}
	}
	// -----------------------------------------------------------------------------
	public static boolean SearchAllPhrasesForMatch(Context theContext,List<String>thePhrasesToMatch)
	{
		// -------------------------------------------------------------------------
		// 01/06/2016 ECU created to loop through the list of phrases to check if
		//                a match is found or not. If found then the associated
		//                actions will be processed
		// -------------------------------------------------------------------------
		for (int index=0; index < thePhrasesToMatch.size(); index++)
		{
			// ---------------------------------------------------------------------
			// 01/06/2016 ECU check for a match on a particular phrase. If phrase
			//                found then return with that fact because the associated
			//                actions will have been processed
			// ---------------------------------------------------------------------
			if (SearchAllPhrasesForMatch (theContext,thePhrasesToMatch.get (index)))
					return true;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 01/06/2016 ECU nothing found so indicate that fact
		// -------------------------------------------------------------------------
		return false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void WriteToDisk (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 21/05/2016 ECU created to write the user defined phrases to disk
		// -------------------------------------------------------------------------
		Utilities.writeObjectToDisk (PublicData.projectFolder + theContext.getString(R.string.voice_command_phrases_file),
									 PublicData.voiceCommandPhrases);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

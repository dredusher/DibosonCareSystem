package com.usher.diboson;

import java.io.Serializable;

public class Document implements Serializable
{
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================

	// =============================================================================
	String	path;
	String	title;
	// =============================================================================
	
	// =============================================================================
	public Document (String theTitle,String thePath)
	{
		path	=	thePath;
		title	= 	theTitle;
	}
	// =============================================================================
	
	// =============================================================================
	public static void Add (String theTitle,String thePath)
	{
		// -------------------------------------------------------------------------
		// 18/10/2016 ECU created to create a record for the arguments provided and
		//                add to the list
		// -------------------------------------------------------------------------
		Document localDocument = new Document (theTitle,thePath);
		// -------------------------------------------------------------------------
		// 18/10/2016 ECU check if there is already a record with the same title
		// -------------------------------------------------------------------------
		if (PublicData.storedData.documents.size() > 0)
		{
			for (int index = 0; index < PublicData.storedData.documents.size(); index++)
			{
				if (PublicData.storedData.documents.get (index).title.equalsIgnoreCase (theTitle))
				{
					// -------------------------------------------------------------
					// 18/10/2016 ECU a match has been found so replace this record
					//                with that specified
					// -------------------------------------------------------------
					PublicData.storedData.documents.set (index, localDocument);
					// -------------------------------------------------------------
					// 18/10/2016 ECU and just exit
					// -------------------------------------------------------------
					return;
					// -------------------------------------------------------------
				}
			}
		}
		// -------------------------------------------------------------------------
		// 18/10/2016 ECU create a new entry
		// -------------------------------------------------------------------------
		PublicData.storedData.documents.add (localDocument);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

package com.usher.diboson;

import java.io.Serializable;

public class SchedulesDirectProgram implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	String  description;
	String  genre;
	String	ID;
	String	title;
	
	public SchedulesDirectProgram (String theID,String theTitle,String theDescription,String theGenre)
	{
		description = theDescription;
		genre		= theGenre;
		ID			= theID;
		title		= theTitle;
	}
	
	public String Print ()
	{
		return " Title : " + title + 
			     "\n    Description : " + description + 
			     "\n       Genre : " + genre + "\n\n";
	}
}

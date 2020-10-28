package com.usher.diboson;

public class NoteFrequency 
{
	// =============================================================================
	String	note;
	double	frequency;
	// =============================================================================
	public NoteFrequency (String theNote,double theFrequency)
	{
		// -------------------------------------------------------------------------
		// 20/08/2015 ECU created to store frequency against a note
		// -------------------------------------------------------------------------
		frequency	= theFrequency;
		note		= theNote;
		// -------------------------------------------------------------------------
	}
}

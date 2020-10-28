package com.usher.diboson;

import java.io.Serializable;

public class Tone implements Serializable
{
	// =============================================================================
	// 18/08/2015 ECU created to hold the data associated with a 'tone'
	// -----------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;
	// -----------------------------------------------------------------------------
	int		duration;		// duration of the tone in milliSeconds
	int		frequency;		// frequency of the tone in Hz
	// =============================================================================
	
	// =============================================================================
	private final static NoteFrequency NOTES [] = {
								new NoteFrequency ("C0",		16.35),
								new NoteFrequency ("C#0/Db0",	17.32),
								new NoteFrequency ("D0",		18.35),
								new NoteFrequency ("D#0/Eb0",	19.45),
								new NoteFrequency ("E0",		20.60),
								new NoteFrequency ("F0",		21.83),
								new NoteFrequency ("F#0/Gb0",	23.12),
								new NoteFrequency ("G0",		24.50),
								new NoteFrequency ("G#0/Ab0",	25.96),
								new NoteFrequency ("A0",		27.50),
								new NoteFrequency ("A#0/Bb0",	29.14),
								new NoteFrequency ("B0",		30.87),
								new NoteFrequency ("C1",		32.70),
								new NoteFrequency ("C#1/Db1",	34.65),
								new NoteFrequency ("D1",		36.71),
								new NoteFrequency ("D#1/Eb1",	38.89),
								new NoteFrequency ("E1",		41.20),
								new NoteFrequency ("F1",		43.65),
								new NoteFrequency ("F#1/Gb1",	46.25),
								new NoteFrequency ("G1",		49.00),
								new NoteFrequency ("G#1/Ab1",	51.91),
								new NoteFrequency ("A1",		55.00),
								new NoteFrequency ("A#1/Bb1",	58.27), 
								new NoteFrequency ("B1",		61.74),
								new NoteFrequency ("C2",		65.41), 
								new NoteFrequency ("C#2/Db2",	69.30), 
								new NoteFrequency ("D2",		73.42), 
								new NoteFrequency ("D#2/Eb2",	77.78), 
								new NoteFrequency ("E2",		82.41),
								new NoteFrequency ("F2",		87.31),
								new NoteFrequency ("F#2/Gb2",	92.50), 
								new NoteFrequency ("G2",		98.00),
								new NoteFrequency ("G#2/Ab2",	103.83),
								new NoteFrequency ("A2",		110.00), 
								new NoteFrequency ("A#2/Bb2",	116.54), 
								new NoteFrequency ("B2",		123.47), 
								new NoteFrequency ("C3",		130.81), 
								new NoteFrequency ("C#3/Db3",	138.59), 
								new NoteFrequency ("D3",		146.83), 
								new NoteFrequency ("D#3/Eb3",	155.56), 
								new NoteFrequency ("E3",		164.81),
								new NoteFrequency ("F3",		174.61), 
								new NoteFrequency ("F#3/Gb3",	185.00), 
								new NoteFrequency ("G3",		196.00),
								new NoteFrequency ("G#3/Ab3",	207.65), 
								new NoteFrequency ("A3",		220.00), 
								new NoteFrequency ("A#3/Bb3",	233.08), 
								new NoteFrequency ("B3",		246.94),
								new NoteFrequency ("C4",		261.63), 
								new NoteFrequency ("C#4/Db4",	277.18), 
								new NoteFrequency ("D4",		293.66), 
								new NoteFrequency ("D#4/Eb4",	311.13), 
								new NoteFrequency ("E4",		329.63),
								new NoteFrequency ("F4",		349.23), 
								new NoteFrequency ("F#4/Gb4",	369.99), 
								new NoteFrequency ("G4",		392.00), 
								new NoteFrequency ("G#4/Ab4",	415.30), 
								new NoteFrequency ("A4",		440.00), 
								new NoteFrequency ("A#4/Bb4",	466.16), 
								new NoteFrequency ("B4",		493.88), 
								new NoteFrequency ("C5",		523.25), 
								new NoteFrequency ("C#5/Db5",	554.37), 
								new NoteFrequency ("D5",		587.33), 
								new NoteFrequency ("D#5/Eb5",	622.25), 
								new NoteFrequency ("E5",		659.25), 
								new NoteFrequency ("F5",		698.46), 
								new NoteFrequency ("F#5/Gb5",	739.99), 
								new NoteFrequency ("G5",		783.99), 
								new NoteFrequency ("G#5/Ab5",	830.61), 
								new NoteFrequency ("A5",		880.00), 
								new NoteFrequency ("A#5/Bb5",	932.33), 
								new NoteFrequency ("B5",		987.77),
								new NoteFrequency ("C6",		1046.50), 
								new NoteFrequency ("C#6/Db6",	1108.73), 
								new NoteFrequency ("D6",		1174.66), 
								new NoteFrequency ("D#6/Eb6",	1244.51), 
								new NoteFrequency ("E6",		1318.51), 
								new NoteFrequency ("F6",		1396.91), 
								new NoteFrequency ("F#6/Gb6",	1479.98), 
								new NoteFrequency ("G6",		1567.98), 
								new NoteFrequency ("G#6/Ab6",	1661.22), 
								new NoteFrequency ("A6",		1760.00), 
								new NoteFrequency ("A#6/Bb6",	1864.66), 
								new NoteFrequency ("B6",		1975.53), 
								new NoteFrequency ("C7",		2093.00), 
								new NoteFrequency ("C#7/Db7",	2217.46), 
								new NoteFrequency ("D7",		2349.32), 
								new NoteFrequency ("D#7/Eb7",	2489.02), 
								new NoteFrequency ("E7",		2637.02),
								new NoteFrequency ("F7",		2793.83), 
								new NoteFrequency ("F#7/Gb7",	2959.96), 
								new NoteFrequency ("G7",		3135.96), 
								new NoteFrequency ("G#7/Ab7",	3322.44), 
								new NoteFrequency ("A7",		3520.00), 
								new NoteFrequency ("A#7/Bb7",	3729.31), 
								new NoteFrequency ("B7",		3951.07),
								new NoteFrequency ("C8",		4186.01), 
								new NoteFrequency ("C#8/Db8",	4434.92), 
								new NoteFrequency ("D8",		4698.63),
								new NoteFrequency ("D#8/Eb8",	4978.03), 
								new NoteFrequency ("E8",		5274.04),
								new NoteFrequency ("F8",		5587.65), 
								new NoteFrequency ("F#8/Gb8",	5919.91), 
								new NoteFrequency ("G8",		6271.93),
								new NoteFrequency ("G#8/Ab8",	6644.88), 
								new NoteFrequency ("A8",		7040.00),
								new NoteFrequency ("A#8/Bb8",	7458.62), 
								new NoteFrequency ("B8",		7902.13)
		 };
	// -----------------------------------------------------------------------------
	private static final String NOTE_SEPARATOR	= "/";
	// =============================================================================
	
	// =============================================================================
	public Tone (int theFrequency,int theDuration)
	{
		// -------------------------------------------------------------------------
		// 18/08/2015 ECU created to initialise the object with the specified values
		// -------------------------------------------------------------------------
		duration	=	theDuration;
		frequency	=	theFrequency;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public Tone (String theNote,int theDuration)
	{
		// -------------------------------------------------------------------------
		// 20/08/2015 ECU created to supply the strings which define the note
		// -------------------------------------------------------------------------
		duration  = theDuration;
		frequency = (int) getFrequencyFromNote (theNote);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	double getFrequencyFromNote (String theNote)
	{
		// -------------------------------------------------------------------------
		// 20/08/2015 ECU created to convert a string note into its frequency
		// -------------------------------------------------------------------------
		// 20/08/2015 ECU scan the array of NOTES to try and find a match
		// -------------------------------------------------------------------------
		for (int theNoteIndex = 0; theNoteIndex < NOTES.length; theNoteIndex++)
		{
			// ----------------------------------------------------------------------
			// 20/08/2015 ECU each entry in the array may be a number of notes
			//                each separated by NOTE_SEPARATOR
			// ----------------------------------------------------------------------
			String [] theNotes = NOTES [theNoteIndex].note.split (NOTE_SEPARATOR);
			// ----------------------------------------------------------------------
			// 20/08/2015 ECU now check each 'note' looking for a match
			// ----------------------------------------------------------------------
			for (int theEntry = 0; theEntry < theNotes.length; theEntry++)
			{
				if (theNote.equalsIgnoreCase (theNotes[theEntry]))
				{
					// -------------------------------------------------------------
					// 20/08/2015 ECU return the frequency associated with 'theNote'
					// -------------------------------------------------------------
					return NOTES [theNoteIndex].frequency;
					// -------------------------------------------------------------
				}
			}
		}
		// -------------------------------------------------------------------------
		// 20/08/2015 ECU no match found so return an 'invalid' frequency
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}

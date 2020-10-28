package com.usher.diboson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;


public class FileReaderInReverse extends InputStream 
{
	// =============================================================================
	// 15/04/2016 ECU created to read a text file in reverse
	//
	//			  ECU implementation would look like
	//
	//					try
	//					{
	//						BufferedReader inputFile = new BufferedReader 	
	//										(new InputStreamReader (new FileReaderInReverse 
	//											(new File ([.... the file's string name .....]))));
	//
	//						String inputLine;
	//						while (true) 
	//						{
	//							inputLine = inputFile.readLine();
	//							if (inputLine == null) 
	//								break;
	//                          [ ...... do something with the 'line' ..... ]
	//						}
	//						inputFile.close();
	//					}
	//					catch (Exception theException)
	//					{		
	//					}
	// =============================================================================
	
	// =============================================================================
	byte				byteRead;						// 16/04/2016 ECU added
	long 				currentLineStart 	= -1;
	long 				currentLineEnd 		= -1;
    long 				currentPosition		= -1;
    RandomAccessFile 	inputFile;
    long 				lastPositionInFile 	= -1;
    // =============================================================================
 
    // =============================================================================
    public FileReaderInReverse (File theFile)
    {
    	// -------------------------------------------------------------------------
    	// 15/04/2016 ECU constructor created
    	// -------------------------------------------------------------------------
    	try
    	{
    		// ---------------------------------------------------------------------
    		// 15/04/2016 ECU need a random access file so that positioning can be
    		//                used
    		// ---------------------------------------------------------------------
    		inputFile 			= new RandomAccessFile (theFile, "r");
    		// ---------------------------------------------------------------------
    		// 15/04/2016 ECU set up pointers from the file information
    		// ---------------------------------------------------------------------
    		currentLineStart 	= theFile.length();
    		currentLineEnd 		= theFile.length();
       		currentPosition		= currentLineEnd; 
    		lastPositionInFile 	= theFile.length() - 1;
    		// ---------------------------------------------------------------------
    	}
    	catch (Exception theException)
    	{
    		// ---------------------------------------------------------------------
    		// 15/04/2016 ECU an exception occurred
    		// ---------------------------------------------------------------------
    	}
    }
    // =============================================================================  
    public void findPreviousLine()
    {
    	// -------------------------------------------------------------------------
    	// 15/04/2016 ECU created to find the previous line
    	// -------------------------------------------------------------------------
    	try
    	{
    		// ---------------------------------------------------------------------
    		// 15/04/2016 ECU set the end of the line to the start of the current line
    		// ---------------------------------------------------------------------
    		currentLineEnd = currentLineStart; 
    		// ---------------------------------------------------------------------
    		// 15/04/2016 ECU check if at the beginning of the file
    		// ---------------------------------------------------------------------
    		if (currentLineEnd == 0) 
    		{
    			// -----------------------------------------------------------------
    			// 15/04/2016 ECU at the beginning of the file
    			// -----------------------------------------------------------------
    			currentLineEnd 		= -1;
    			currentLineStart 	= -1;
    			currentPosition		= -1;
    			// -----------------------------------------------------------------
    			return; 
    		}
    		// ---------------------------------------------------------------------
    		long filePointer = currentLineStart - 1;

    		while (true) 
    		{
    			// -----------------------------------------------------------------
    			// 15/04/2016 ECU step back looking for a line terminator
    			// -----------------------------------------------------------------
    			filePointer--;
    			// -----------------------------------------------------------------
    			// 15/04/2016 ECU check if reached the start of the file
    			// -----------------------------------------------------------------
    			if (filePointer < 0) 
    			{  
    				break; 
    			}
    			// -----------------------------------------------------------------
    			// 15/04/2016 ECU position within the input file and then read the
    			//                byte which is at that point
    			// -----------------------------------------------------------------
    			inputFile.seek (filePointer);
    			byteRead = inputFile.readByte();
    			// -----------------------------------------------------------------
    			if (byteRead == StaticData.LINE_TERMINATOR && filePointer != lastPositionInFile ) 
    			{   
    				break;
    			}
    			// -----------------------------------------------------------------
    		}
    		// ---------------------------------------------------------------------
    		// 15/04/2016 ECU want to add 1 to the current 'filePointer' which is 
    		//                either pointing to the previous line terminator or is 
    		//                at the start of the file
    		// ---------------------------------------------------------------------  
    		currentLineStart 	= filePointer + 1;
    		currentPosition 	= currentLineStart;
    		// ---------------------------------------------------------------------
    		// 15/04/2016 ECU the currentPosition will be pointing to the start of the
    		//                'previous' line
    		// ---------------------------------------------------------------------
    	}
    	catch (Exception theException)
    	{
    		
    	}
    	// -------------------------------------------------------------------------
    }
    // ==============================================================================
    public int read () throws IOException
    {
    	// --------------------------------------------------------------------------
    	// 15/04/2016 ECU created to read in a single byte
    	// --------------------------------------------------------------------------
        if (currentPosition < currentLineEnd) 
        {
            inputFile.seek (currentPosition++);
            int readByte = inputFile.readByte();
            return readByte;
        }
        else 
        if (currentPosition < 0) 
        {
        	// ---------------------------------------------------------------------
        	// 15/04/2016 ECU reached the start of the file so indicate the fact
        	// ---------------------------------------------------------------------
            return -1;
            // ---------------------------------------------------------------------
        }
        else 
        {
        	// ---------------------------------------------------------------------
        	// 15/04/2016 ECU need to reposition the pointers to the start of the
        	//                previous line before reading in the byte
        	// ---------------------------------------------------------------------
            findPreviousLine ();
            // ---------------------------------------------------------------------
            // 15/04/2016 ECU having repositioned to the start of the previous line
            //                then read in a byte
            // ---------------------------------------------------------------------
            return read ();
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
}


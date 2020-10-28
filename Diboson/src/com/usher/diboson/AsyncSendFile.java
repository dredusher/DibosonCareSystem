package com.usher.diboson;

import java.io.RandomAccessFile;

import android.content.Context;
import android.os.AsyncTask;

public class AsyncSendFile extends AsyncTask<Void, Integer, Boolean>
{
	/* ============================================================================= */
	//private static final String TAG = "AsyncSendFile";
	/* ============================================================================= */
	private static final int    ACK_WAIT  	 = 50;		// milliseconds
	private static final int    ACK_COUNTER  = (60 * 1000)/ACK_WAIT;
														// number of ACK_WAIT in 10 secs
	/* ============================================================================= */
	int					chunkCounter;
	int					chunkNumber;						// 07/11/2014 ECU added
	int					chunkSize;
	Context				context;
	RandomAccessFile 	file;								// 09/11/2015 ECU added
	FileDetails			fileDetails;
	String				IPAddress;
	/* ============================================================================= */
	public AsyncSendFile (Context theContext,String theReceiver,
							FileDetails theFileDetails,int theChunkSize)
	{
		// -------------------------------------------------------------------------
		chunkSize			= theChunkSize;
		context				= theContext;
		fileDetails			= theFileDetails;
		IPAddress			= theReceiver;
		// -------------------------------------------------------------------------
		// 07/04/2014 ECU initialise the running counter
		// -------------------------------------------------------------------------
		chunkCounter 		= 0;
		// -------------------------------------------------------------------------
		// 07/11/2014 ECU work out the number of chunks expected
		// -------------------------------------------------------------------------
		chunkNumber = (int) (theFileDetails.fileSize / (long) theChunkSize);
		// -------------------------------------------------------------------------
	}
	/* ============================================================================= */
    protected void onPreExecute ()
    {
    }
    /* ============================================================================= */	
    protected Boolean doInBackground(Void...arg0) 
    {
    	try 
    	{
    		// ---------------------------------------------------------------------
    		// 07/04/2014 ECU indicate to the receiver that chunks are to be sent
    		// 31/01/2015 ECU change the method called
    		// 21/03/2015 ECU pass through the message type as an argument
    		// ---------------------------------------------------------------------
    		Utilities.sendSocketMessageSendTheObject (context,
    												  IPAddress,
    												  PublicData.socketNumberForData,
    												  StaticData.SOCKET_MESSAGE_SEND_CHUNKS,
    												  (Object) fileDetails);
    		// ---------------------------------------------------------------------
			// 07/04/2014 ECU want to wait for a response to the sent chunk
			// ---------------------------------------------------------------------
			if (!WaitForACK (ACK_COUNTER))
					return false;
			// ---------------------------------------------------------------------

    		// ---------------------------------------------------------------------
    		// 07/04/2014 ECU open to the specified file
			// 09/11/2015 ECU change 'file' from a local variable to being declared
			//                in the outer block
    		// ---------------------------------------------------------------------
			try
			{
				file = new RandomAccessFile (fileDetails.GetFileName(), "r");
			}
			catch (Exception theException)
			{
				// -----------------------------------------------------------------
				// 09/11/2015 ECU indicate that the file cannot be found or that
				//                the mode is wrong
				// -----------------------------------------------------------------
				return false;
				// -----------------------------------------------------------------
			}
    	    // ---------------------------------------------------------------------  
    	    // 07/04/2014 ECU read all the data in
    		// ---------------------------------------------------------------------	 
    		int 	numberOfBytesRead 	= 0;
    	    byte [] inputBuffer 		= new byte [chunkSize];  
    	    // ---------------------------------------------------------------------    
    	    // 07/04/2014 ECU now loop for all chunks
    	    // ---------------------------------------------------------------------
    	    while ((numberOfBytesRead = file.read(inputBuffer,0,chunkSize)) > 0)
    	    {
    	    	// -----------------------------------------------------------------
    	    	chunkCounter++;
    	    	// -----------------------------------------------------------------
    	    	// 07/11/2014 ECU publish the current progress
    	    	// -----------------------------------------------------------------
    	    	publishProgress (chunkCounter);
    	    	// -----------------------------------------------------------------
    	    	// 31/01/2015 ECU changed the name of the method called
    	    	// 21/03/2015 ECU include the message type as an argument
    	    	// -----------------------------------------------------------------
				Utilities.sendSocketMessageSendTheObject (context,
														  IPAddress,
														  PublicData.socketNumberForData, 
														  StaticData.SOCKET_MESSAGE_SEND_CHUNK,
														  (Object) new ChunkDetails (inputBuffer,numberOfBytesRead));
				// -----------------------------------------------------------------
				// 07/04/2014 ECU want to wait for a response to the sent chunk
				// 09/11/2015 ECU make sure that the file is closed
				// -----------------------------------------------------------------
				if (!WaitForACK (ACK_COUNTER))
				{
					file.close ();
					return false;
				}
				// -----------------------------------------------------------------	
    	    }
    	    // ---------------------------------------------------------------------
    	    // 07/04/2014 ECU close down the file
    	    // ---------------------------------------------------------------------
    	    file.close ();
    	    // ---------------------------------------------------------------------
        	// 07/04/2014 ECU tell the receiver that everything done
    	    // ---------------------------------------------------------------------
    	    Utilities.sendDatagramType (context,IPAddress,StaticData.SOCKET_MESSAGE_SEND_CHUNK_END);
    	    // ---------------------------------------------------------------------
    	    // 07/04/2014 ECU wait for a positive response
    	    // ---------------------------------------------------------------------
    	    WaitForACK (ACK_COUNTER);
    	    // ---------------------------------------------------------------------
  	    }
    	catch (Exception theException)
    	{
    		// ---------------------------------------------------------------------
    		// 09/11/2015 ECU an exception may have occurred
    		// --------------------------------------------------------------------- 
    		return false;
    	}   	
    	return true;
    }
    /* ============================================================================= */	
    protected void onProgressUpdate(Integer...theArguments)
    {
    	// -------------------------------------------------------------------------
    	// 07/11/2014 ECU this method is called if a 'publishProgress (<value>)' 
    	//                call is made from 'doInBackground'. The <value>
    	//                will be passed through in 'theArguments [0]'
    	// -------------------------------------------------------------------------
    	
    	// -------------------------------------------------------------------------
    }
    /* ============================================================================= */	
    protected void onPostExecute (Boolean theBoolean) 
    {
    	
    }
    /* ============================================================================= */
    boolean WaitForACK (int theCounter)
    {
    	// -------------------------------------------------------------------------
    	try 
		{
    		// ---------------------------------------------------------------------
    		PublicData.chunkResponse = StaticData.NO_RESULT;
    	
    		int waitCounter = ACK_COUNTER;
    		// ---------------------------------------------------------------------
    		// 07/04/2014 ECU loop until a response is received or until a timeout
    		//                occurs
    		// ---------------------------------------------------------------------
    		while (PublicData.chunkResponse == StaticData.NO_RESULT)
			{
				Thread.sleep (ACK_WAIT);
			
				if (waitCounter-- == 0)
				{
					// -------------------------------------------------------------
					// 07/04/2014 ECU have timed out
					// -------------------------------------------------------------
					return false;
				}	
			}
    		// ---------------------------------------------------------------------
    		// 07/04/2014 ECU seem to have received the ack OK
    		// ---------------------------------------------------------------------
    		return true;
		} 
		catch (InterruptedException exception) 
		{
			
			return false;
		}
    }
    /* ============================================================================= */
}

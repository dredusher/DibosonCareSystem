package com.usher.diboson;

import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;

public class ReceiveFile 
{
	/* ============================================================================= */
	//private static final String TAG = "ReceiveFile";
	/* ============================================================================= */
	int				 chunkCounter = 0;
	Context			 context;
	RandomAccessFile fileHandle = null;
	String			 sender;
	/* ============================================================================= */
	public ReceiveFile (Context theContext,String theSender,String theFileName)
	{
		try
		{
			// ---------------------------------------------------------------------
			context		= theContext;
			sender		= theSender;
			// ---------------------------------------------------------------------
			// 07/04/2014 ECU set up the file handle for the specified file
			// 07/11/2014 ECU include theSender for debug purposes
			// ---------------------------------------------------------------------
			fileHandle = new RandomAccessFile (theFileName + "_(" + theSender + ")", "rw");
			// ---------------------------------------------------------------------
			// 07/04/2014 ECU clear the file
			// ---------------------------------------------------------------------
			fileHandle.setLength(0);
			// ---------------------------------------------------------------------
			// 07/04/2014 ECU confirm that ready to go
			// ---------------------------------------------------------------------
			Utilities.sendDatagramType(theContext,theSender,StaticData.SOCKET_MESSAGE_CHUNK_ACK);
			// ---------------------------------------------------------------------
		}
		catch (IOException theException)
		{
		}
	}
	/* ============================================================================= */
	public void ProcessChunk (Context theContext,String theSender,ChunkDetails localChunk)
	{
		try
		{
			chunkCounter++;
			// ---------------------------------------------------------------------
			// 07/04/2014 ECU write the incoming chunk into the file
			// ---------------------------------------------------------------------
			fileHandle.write(localChunk.data,0,localChunk.chunkSize);
			// ---------------------------------------------------------------------
			// 07/04/2014 ECU indicate that everything is OK
			// ---------------------------------------------------------------------
			Utilities.sendDatagramType(theContext,theSender,StaticData.SOCKET_MESSAGE_CHUNK_ACK);
			// ---------------------------------------------------------------------	
		}
		catch (IOException theException)
		{
			// ---------------------------------------------------------------------
			// 07/04/2014 ECU indicate that something went wrong
			// ---------------------------------------------------------------------
			Utilities.sendDatagramType(theContext,theSender,StaticData.SOCKET_MESSAGE_CHUNK_NAK);
			// ---------------------------------------------------------------------
		}
	}
	/* ============================================================================= */
	public void Close ()
	{
		try
		{
			// ---------------------------------------------------------------------
			// 07/04/2014 ECU close the receiving file
			// ---------------------------------------------------------------------	
			fileHandle.close();
			// ---------------------------------------------------------------------
			// 07/04/2014 ECU indicate that everything is OK
			// ---------------------------------------------------------------------
			Utilities.sendDatagramType(context,sender,StaticData.SOCKET_MESSAGE_CHUNK_ACK);
			// ---------------------------------------------------------------------
		}
		catch (IOException theException)
		{
			
		}
	}
	/* ============================================================================= */
	
	
}

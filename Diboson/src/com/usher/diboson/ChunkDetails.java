package com.usher.diboson;

import java.io.Serializable;

public class ChunkDetails implements Serializable 
{
	/* ========================================================================== */
	private static final long serialVersionUID = 1L;
	/* ========================================================================== */
	public static final int	ACK		= 0;
	public static final int	NAK		= 1;
	/* ========================================================================== */
	int		chunkSize;
	byte []	data;
	/* ============================================================================= */
	public ChunkDetails (byte [] theChunk,int theChunkSize)
	{
		chunkSize	=	theChunkSize;
		data		=   new byte [chunkSize];
		
		System.arraycopy (theChunk,0,data,0,chunkSize);	
	}
	/* ============================================================================= */
}

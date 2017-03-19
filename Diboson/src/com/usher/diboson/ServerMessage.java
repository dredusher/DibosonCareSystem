package com.usher.diboson;

import java.io.Serializable;

public class ServerMessage implements Serializable
{
	// =============================================================================
	// 30/01/2015 ECU this class defines the object which is passed between client
	//                and server on the socket for data exchange
	// =============================================================================
	private static final long serialVersionUID = 1L;
	// -----------------------------------------------------------------------------
	public Object	data;		// 30/01/2015 ECU the actual data part of the message
	public byte		flags;		// 30/01/2015 ECU any associated flags for controlling
								//                the handling of the message
	public int		type;		// 30/01/2015 ECU defines the type of message being
								//                exchanged
	// =============================================================================
	public ServerMessage (int theType,byte theFlags,Object theData)
	{
		// -------------------------------------------------------------------------
		// 30/01/2015 ECU copy across the supplied settings
		// -------------------------------------------------------------------------
		data 	= theData;
		flags 	= theFlags;
		type	= theType;
		// -------------------------------------------------------------------------	
	}
	// =============================================================================
	public ServerMessage (int theType,Object theData)
	{
		// -------------------------------------------------------------------------
		// 30/01/2015 ECU constructor when no flags are needed
		// -------------------------------------------------------------------------
		this (theType,(byte) 0,theData);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
		public ServerMessage (int theType)
		{
			// -------------------------------------------------------------------------
			// 31/01/2015 ECU constructor only type is to be set
			// -------------------------------------------------------------------------
			this (theType,(byte) 0,null);
			// -------------------------------------------------------------------------
		}
	// =============================================================================
}

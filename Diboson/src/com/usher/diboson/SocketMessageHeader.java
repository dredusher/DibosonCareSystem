package com.usher.diboson;

import java.io.Serializable;

public class SocketMessageHeader implements Serializable
{
	/* ========================================================= */
	private static final long serialVersionUID = 1L;
	/* ========================================================= */
	int		type;					// the type of socket message
	int		flags;					// flags associated with the message
	int		length;					// the length of the socket message
	/* ========================================================= */
	public SocketMessageHeader ()
    {
        type	 = StaticData.SOCKET_MESSAGE_UNSET;
        flags 	 = 0;
        length	 = 0;
    }
	/* ========================================================= */
}

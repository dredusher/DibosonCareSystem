package com.usher.diboson;

import java.io.Serializable;

public class MovementParameters implements Serializable 
{
	// -----------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;
	// -----------------------------------------------------------------------------
	
	// -----------------------------------------------------------------------------
	public String	actions			= StaticData.MOVEMENT_ACTIONS;
	public int		duration		= StaticData.MOVEMENT_DURATION;
	public int		gap				= StaticData.MOVEMENT_GAP;
	public int		initialDelay	= StaticData.MOVEMENT_INITIAL_DELAY;
	public float	trigger			= StaticData.MOVEMENT_TRIGGER;
	// -----------------------------------------------------------------------------

}

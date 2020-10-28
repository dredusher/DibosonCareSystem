package com.usher.diboson;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RadarImageView extends ImageView
{
	// =============================================================================
	static boolean	initialised = false;
	// =============================================================================
    public RadarImageView(Context theContext)
    {
        super (theContext);
    }
    // =============================================================================
    public RadarImageView(Context theContext, AttributeSet theAttributeSet)
    {
        super (theContext, theAttributeSet);
    }
    // =============================================================================
     public RadarImageView (Context theContext, AttributeSet theAttributeSet, int theDefaultStyle)
    {
        super (theContext, theAttributeSet, theDefaultStyle);
    }
     // =============================================================================
    @Override
    protected void onDraw (Canvas theCanvas)
    {
    	if (!initialised)
    	{
    		super.onDraw (theCanvas);
    		// ---------------------------------------------------------------------
    		// 16/12/2014 ECU indicate that the view has been initialised
    		// ---------------------------------------------------------------------
    		initialised = true;	
    	}
    	else
    	{
    		// ---------------------------------------------------------------------
    		// 16/12/2014 ECU update the position of the pointer
    		// ---------------------------------------------------------------------
 			RadarActivity.updateTheRadarPointer (theCanvas);
    	}
    }
    // =============================================================================
    @Override
    public void invalidateDrawable (Drawable theDrawable) 
    {
         super.invalidateDrawable (theDrawable);
    }
    // =============================================================================
}

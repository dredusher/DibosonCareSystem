package com.usher.diboson;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

// =================================================================================
public class ExplosionView extends View
{
	// -----------------------------------------------------------------------------
    private Bitmap originalBitmap;
    private int    xPointer;
    // -----------------------------------------------------------------------------
    
   
    // =============================================================================
    public ExplosionView(Context context) 
    {
        this (context, null);
    }
    // =============================================================================
    public ExplosionView (Context context, AttributeSet attrs) 
    {
        this(context, attrs, 0);
    }
    // =============================================================================
    public ExplosionView (Context context, AttributeSet attrs, int defStyle) 
    {
        super(context, attrs, defStyle);
        init(context);
    }
    // =============================================================================
    private void init (Context context) 
    {
    	// -------------------------------------------------------------------------
        originalBitmap = BitmapFactory.decodeResource (context.getResources(), R.drawable.compass);
        originalBitmap = originalBitmap.copy( Bitmap.Config.ARGB_8888 , true);
        // -------------------------------------------------------------------------
        xPointer = 0;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public void draw (Canvas canvas)
     {
        // -------------------------------------------------------------------------
        // 03/07/2020 ECU added the 'super' call
        // -------------------------------------------------------------------------
        super.draw (canvas);
        // -------------------------------------------------------------------------
        boolean refresh = false;
        // -------------------------------------------------------------------------
        xPointer += 2;
        if (xPointer < originalBitmap.getWidth() / 2) {
            refresh = true;
            for (int y = 0; y < originalBitmap.getHeight(); y += 2) {
                originalBitmap.setPixel(xPointer, y, 0);
                originalBitmap.setPixel(originalBitmap.getWidth() - xPointer - 1, y, 0);
            }
        }
        // ------------------------------------------------------------------------------
        if (refresh)
        {
            // -------------------------------------------------------------------------
            postInvalidate();
            // -------------------------------------------------------------------------
            canvas.save();
            canvas.drawBitmap(originalBitmap, 0, 0, null);
            canvas.restore();
        }
        // =========================================================================
    }
}

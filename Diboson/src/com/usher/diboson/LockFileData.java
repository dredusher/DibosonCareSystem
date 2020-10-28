package com.usher.diboson;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class LockFileData
{
    // =============================================================================
    //private static final String TAG = "LockFileData";
    // =============================================================================

    // =============================================================================
    // 26/03/2020 ECU declare any data that may cause issues if the app is
    //                forcibly stopped
    // ----------------------------------------------------------------------------
    File        file;
    Drawable    wallPaper;
    // =============================================================================

    // =============================================================================
    public LockFileData (Context theContext)
    {
        // -------------------------------------------------------------------------
        // 26/03/2020 ECU constructor to set the lock file name
        // -------------------------------------------------------------------------
        file = new File (PublicData.projectFolder + theContext.getString (R.string.lock_file));
        // -------------------------------------------------------------------------
    }
    // =============================================================================

    // =============================================================================
    public static boolean LockFile (Context theContext, boolean theAction)
    {
        // -------------------------------------------------------------------------
        // 26/03/2020 ECU created to handle the detection / deletion of the lock
        //                file
        // 						theAction ...... true ....... create the 'lock file'
        //                                       false ...... delete the 'lock file'
        // --------------------------------------------------------------------------
        // 26/03/2020 ECU decide what needs to be done
        // --------------------------------------------------------------------------
        if (theAction)
        {
            // ----------------------------------------------------------------------
            // 26/03/2020 ECU need to initialise the object
            // ----------------------------------------------------------------------
            PublicData.lockFileData = new LockFileData (theContext);
            // ----------------------------------------------------------------------
            // 26/03/2020 ECU want to create the 'lock file'
            // ----------------------------------------------------------------------
            // 26/03/2020 ECU check if the lock file already exists
            // ----------------------------------------------------------------------
            if (!PublicData.lockFileData.file.exists())
            {
                // ------------------------------------------------------------------
                // 26/03/2020 ECU the file does not exist so create it
                // ------------------------------------------------------------------
                try
                {
                    // --------------------------------------------------------------
                    PublicData.lockFileData.file.createNewFile();
                    // --------------------------------------------------------------
                }
                catch (IOException theException)
                {

                }
                // ------------------------------------------------------------------
                // 26/03/2020 ECU indicate that all is well
                // ------------------------------------------------------------------
                return true;
                // ------------------------------------------------------------------
            }
            else
            {
                // ------------------------------------------------------------------
                // 26/03/2020 ECU the lock file already exists which means that the
                //                app must have been 'forcibly stopped'
                // ------------------------------------------------------------------
                return false;
                // ------------------------------------------------------------------
            }
            // ----------------------------------------------------------------------
        }
        else
        {
            // ----------------------------------------------------------------------
            // 26/03/2020 ECU want to delete the 'lock file'
            // 31/03/2020 ECU add the try/catch just in case the data or file
            //                is undefined. This could occur if there are multiple
            //                instances of the app.
            // ----------------------------------------------------------------------
            try
            {
                if (PublicData.lockFileData.file.exists())
                {
                    // ------------------------------------------------------------------
                    // 26/03/2020 ECU the 'lock file' exists so delete it
                    // ------------------------------------------------------------------
                    PublicData.lockFileData.file.delete ();
                    // ------------------------------------------------------------------
                }
            }
            catch (Exception theException)
            {
                // ------------------------------------------------------------------
                // 31/03/2020 ECU an exception may occur if the data or file are
                //                undefined
                // ------------------------------------------------------------------
            }
            // ----------------------------------------------------------------------
            // 26/03/2020 ECU indicate that deletion has happened
            // ----------------------------------------------------------------------
            return true;
            // ----------------------------------------------------------------------
        }
        // --------------------------------------------------------------------------
    }
    // ==============================================================================
    public void Recover (Context theContext)
    {
        // -------------------------------------------------------------------------
        // 26/03/2020 ECU try and recover after a 'forced stopped'
        // 28/03/2020 ECU check if the file has any data
        // -------------------------------------------------------------------------
        if (file.length() > 0)
        {
            // ---------------------------------------------------------------------
            // 28/03/2020 ECU the file has data to be processed
            // ---------------------------------------------------------------------
            try
            {
                // -----------------------------------------------------------------
                // 26/03/2020 ECU read the stored wallpaper from file
                // -----------------------------------------------------------------
                Bitmap bitmap =  BitmapFactory.decodeFile (file.getAbsolutePath());
                // -----------------------------------------------------------------
                // 07/04/2017 ECU Note - get the wallpaper manager associated with
                //                       the specified context
                // -----------------------------------------------------------------
                WallpaperManager wallpaperManager = WallpaperManager.getInstance (theContext);
                // -----------------------------------------------------------------
                // 26/03/2020 ECU now set the wall paper to that read from the file
                // -----------------------------------------------------------------
                wallpaperManager.setBitmap (bitmap);
                // -----------------------------------------------------------------
            }
            catch (Exception theException)
            {
            }
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void setWallPaper (Drawable theDrawable)
    {
        // -------------------------------------------------------------------------
        // 26/03/2020 ECU store the drawble
        // -------------------------------------------------------------------------
        wallPaper = theDrawable;
        // -------------------------------------------------------------------------
        // 26/03/2020 ECU write the data to disk
        // -------------------------------------------------------------------------
        WriteToDisk ();
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void SetWallPaper (Drawable theDrawable)
    {
        // -------------------------------------------------------------------------
        // 09/05/2020 ECU created to set the wallpaper provided that the data
        //                structure has been initialised
        // -------------------------------------------------------------------------
        if (PublicData.lockFileData != null)
        {
            // ---------------------------------------------------------------------
            // 09/05/2020 ECU the data structure exists so can store the data
            // ---------------------------------------------------------------------
            PublicData.lockFileData.setWallPaper (theDrawable);
            // ----------------------------------------------------------------------
        }
        // --------------------------------------------------------------------------
    }
    // ==============================================================================
    public void WriteToDisk ()
    {
        // --------------------------------------------------------------------------
        // 26/03/2020 ECU write the current 'lock file' data to disk
        // --------------------------------------------------------------------------
        try
        {
            Bitmap bitmap =  ((BitmapDrawable)wallPaper).getBitmap();
            // ----------------------------------------------------------------------
            // 26/03/2020 ECU set up the output stream
            // ----------------------------------------------------------------------
            FileOutputStream outputStream = new FileOutputStream (file);
            // ----------------------------------------------------------------------
            // 26/03/2020 ECU write the data out at best quality
            // ----------------------------------------------------------------------
            bitmap.compress (Bitmap.CompressFormat.PNG, 100, outputStream);
            // ----------------------------------------------------------------------
            // 26/03/2020 ECU flush out the data and close the stream
            // ----------------------------------------------------------------------
            outputStream.flush();
            outputStream.close();
            // ----------------------------------------------------------------------
        }
        catch (Exception theException)
        {
        }
        // --------------------------------------------------------------------------
    }
    // ==============================================================================
}

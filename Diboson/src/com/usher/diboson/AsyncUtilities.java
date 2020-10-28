package com.usher.diboson;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsyncUtilities 
{
	/* ============================================================================= */
	// 03/04/2014 ECU created to provide methods, that normally exist in Utilities
	//                to be run from within an Async Task
	//            ECU put in the '.get' because do not want the caller to proceed
	//                until the async task has completed - want to investigate the
	//                need for .get on writing
	/* ============================================================================= */
	//private static final String TAG = "AsyncUtilities";
	/* ============================================================================= */
	
	// =============================================================================
    public static class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> 
    {
    	// -------------------------------------------------------------------------
    	private String url;
    	private ImageView imageView;
    	// -------------------------------------------------------------------------
    	public ImageLoadTask (String url, ImageView imageView) 
    	{
    		this.url = url;
    		this.imageView = imageView;
    	}
    	// -------------------------------------------------------------------------
    	@Override
    	protected Bitmap doInBackground (Void... params) 
    	{
    		try 
    		{
    			URL urlConnection = new URL (url);
    			HttpURLConnection connection 
    				= (HttpURLConnection) urlConnection.openConnection();
    			connection.setDoInput(true);
    			connection.connect();
    			InputStream input = connection.getInputStream();
    			Bitmap myBitmap = BitmapFactory.decodeStream(input);
    			return myBitmap;
    		} 
    		catch (Exception e) 
    		{
    			e.printStackTrace();
    		}
    		return null;
    	}
    	// -------------------------------------------------------------------------
    	@Override
    	protected void onPostExecute(Bitmap result) 
    	{
    		super.onPostExecute (result);
    		imageView.setImageBitmap (result);
    	}
    	// -------------------------------------------------------------------------
    }
	// =============================================================================
	public static Object readObjectFromDisk (Context theContext,String theFileName)
	{
		Object localObject;

		try 
		{
			localObject = new AsyncClass (Utilities.createAMethod (Utilities.class,"readObjectFromDisk",theContext,StaticData.BLANK_STRING),
					theContext,theFileName).execute().get();
		} 
		catch (Exception theException) 
		{
			localObject = null;
		} 
		
		return localObject;
	}
	/* ============================================================================= */
	public static boolean writeObjectToDisk (String theFileName,Object theObject)
	{
		try
		{
			new AsyncClass (Utilities.createAMethod (Utilities.class, "writeObjectToDisk",StaticData.BLANK_STRING,(Object)StaticData.BLANK_STRING),
					theFileName,theObject).execute().get();
	
			return true;
		}
		catch (Exception theException)
		{
			return false;
		}
	}
	// =============================================================================
	public static boolean writeObjectToDiskAndBackup (String theFileName,Object theObject)
	{
		// -------------------------------------------------------------------------
		// 29/08/2020 ECU created to perform a 'write' after making backup of the
		//                specified file, if it exists
		// -------------------------------------------------------------------------
		try
		{
			new AsyncClass (Utilities.createAMethod (Utilities.class, "writeObjectToDiskAndBackup",StaticData.BLANK_STRING,(Object)StaticData.BLANK_STRING),
					theFileName,theObject).execute().get();

			return true;
		}
		catch (Exception theException)
		{
			return false;
		}
	}
	// =============================================================================
}

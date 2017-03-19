package com.usher.diboson;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.webkit.WebView;

public class DisplayURL extends DibosonActivity 
{
	// =======================================================================
	// 22/10/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	// 22/10/2015 ECU changed to 'extends DibosonActivity'
	/* ======================================================================= */
	private String 		url;					// 06/02/2014 ECU changed name from
												//                theURL
	private WebView 	webview;
	/* ======================================================================= */
	@SuppressLint ("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate (savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 22/10/2015 ECU the activity has been created anew
			// 19/12/2016 ECU use the method for setting up the screen
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true);
			// ---------------------------------------------------------------------
			// 16/06/2013 ECU the URL should be fed through in the extras
			// ---------------------------------------------------------------------
			Bundle localExtras = getIntent().getExtras();
	    
			if (localExtras != null)
			{
				// -----------------------------------------------------------------
				// 05/11/2013 ECU get the URL from the intent
				// 19/12/2016 ECU changed to use _URL
				// -----------------------------------------------------------------
 	   			url = getIntent().getStringExtra (StaticData.PARAMETER_URL); 	
			}
			else
			{
				// -----------------------------------------------------------------
				// 05/11/2013 ECU is no URl was pass through then display the default
				// -----------------------------------------------------------------
				url = getResources().getString (R.string.URL_To_Display);
			}
			// ---------------------------------------------------------------------
			// 05/11/2013 ECU display the appropriate layout
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_display_url);
			// ---------------------------------------------------------------------
			// 05/11/2013 ECU display the retrieved URL in the web view
			// ---------------------------------------------------------------------
			webview = (WebView) findViewById (R.id.webView1);
			webview.getSettings ().setJavaScriptEnabled (true);
			webview.loadUrl (url);
			// ---------------------------------------------------------------------
		}
		else
		{
			// ---------------------------------------------------------------------
			// 22/10/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}	    
	}
	// =============================================================================
	@Override
	public void onConfigurationChanged (Configuration theNewConfiguration) 
	{
		// -------------------------------------------------------------------------
		// 03/10/2015 ECU created - called when a configuration change occurs
		// -------------------------------------------------------------------------
		super.onConfigurationChanged (theNewConfiguration);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
	public void onBackPressed()
	{
		// -------------------------------------------------------------------------
		// 20/01/2015 ECU added to try and fix an 
		//					"Unimplemented WebView method onKeyDown called ...... "
		//                error that was been logged
		//			  ECU this did not solve the issue that comes up when the back
		//                key is pressed from the blank screen that comes up after
		//                leaving the browser
		// -------------------------------------------------------------------------
	    if (webview.canGoBack ()) 
	    {
	        webview.goBack ();
	        return;
	    }
	    // ------------------------------------------------------------------------
	    super.onBackPressed ();   
	    // ------------------------------------------------------------------------
	}
	// ============================================================================
	   
}

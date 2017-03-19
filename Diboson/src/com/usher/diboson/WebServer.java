package com.usher.diboson;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import android.content.Context;

public class WebServer 
{
	// -----------------------------------------------------------------------------
	// 24/12/2014 ECU a very simple web server
	// -----------------------------------------------------------------------------
	final static String					TAG = "WebServer";
	// -----------------------------------------------------------------------------
	final static String					HANDLE_PATTERN = "*";
	// -----------------------------------------------------------------------------
	private Context 					context 	= null;
	private BasicHttpProcessor 			httpproc 	= null;
	private BasicHttpContext 			httpContext = null;
	private HttpService 				httpService = null;
	private HttpRequestHandlerRegistry	registry 	= null;
	DefaultHttpServerConnection 		serverConnection;
	private ServerSocket 				serverSocket;
	private boolean                     webThreadRunning = false;
	// =============================================================================
	public WebServer(Context context) 
	{
		this.setContext (context);

		httpproc	 	= new BasicHttpProcessor ();

		httpContext 	= new BasicHttpContext ();

		httpproc.addInterceptor (new ResponseDate ());
		httpproc.addInterceptor (new ResponseServer ());
		httpproc.addInterceptor (new ResponseContent ());
		httpproc.addInterceptor (new ResponseConnControl ());

		httpService = new HttpService (httpproc,
		    new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());

		registry = new HttpRequestHandlerRegistry();
		// -------------------------------------------------------------------------
		// 31/12/2014 ECU indicate which URL's are to be handled locally
		// -------------------------------------------------------------------------
		registry.register (HANDLE_PATTERN,new RequestHandler(context));
		// -------------------------------------------------------------------------
		httpService.setHandlerResolver (registry);
	}
	// =============================================================================
	void webServerAsThread ()
	{
		try 
		{
			serverSocket = new ServerSocket (PublicData.socketNumberForWeb);

			serverSocket.setReuseAddress(true);

			while (webThreadRunning) 
			{
				try 
				{
					final Socket socket = serverSocket.accept();

					serverConnection = new DefaultHttpServerConnection();

					serverConnection.bind (socket,new BasicHttpParams());
					// -------------------------------------------------------------
					httpService.handleRequest (serverConnection,httpContext);

					serverConnection.shutdown();
				} 
				catch (Exception theException)
				{
					// -------------------------------------------------------------
					// 18/01/2015 ECU took out the printing of the stack because if
					//                the 'accept' is interrupted then it will throw a
					//                'socket closed' exception which is fine
					// -------------------------------------------------------------
				} 			
			}
			// ---------------------------------------------------------------------
			// 27/12/2014 ECU close the socket being used
			// ---------------------------------------------------------------------
			serverSocket.close();
		} 
		catch (Exception theException) 
		{
			// --------------------------------------------------------------------
			// 18/01/2015 ECU took out the printing of the stack because if
			//                the 'accept' is interrupted then it will throw a
			//                'socket closed' exception which is fine
			// ---------------------------------------------------------------------
			
		} 
		// -------------------------------------------------------------------------
		// 28/12/2014 ECU indicate that the thread is no longer running
		// -------------------------------------------------------------------------
		webThreadRunning = false;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public synchronized void startServer() 
	{
		// -------------------------------------------------------------------------
		// 28/12/2014 ECU indicate that web server thread is running
		// -------------------------------------------------------------------------
		webThreadRunning = true;
		// -------------------------------------------------------------------------
		// 28/12/2014 ECU because the networking should run on the main UI thread 
		//                then run it as a separate thread
		// -------------------------------------------------------------------------
		Thread thread = new Thread()
		 {
			@Override
			public void run()
		  	{
				// -----------------------------------------------------------------
				// 28/12/2014 ECU start the web server as a thread
				// -----------------------------------------------------------------
				webServerAsThread ();  
				// -----------------------------------------------------------------
			 }
		 };
		 // ------------------------------------------------------------------------
		 // 28/12/2014 ECU start up the thread
		 // ------------------------------------------------------------------------
		 thread.start();   
	}
	// =============================================================================
	public synchronized void stopServer()
	{
		webThreadRunning = false;
		if (serverSocket != null) 
		{
			try 
			{
				serverSocket.close();
			} 
			catch (IOException theException) 
			{
				theException.printStackTrace();
			}
		}
	}
	// =============================================================================
	public void setContext(Context context) 
	{
		this.context = context;
	}
	// =============================================================================
	public Context getContext() 
	{
		return context;
	}
	// =============================================================================
	
	// =============================================================================
	class RequestHandler implements HttpRequestHandler 
	{
		// -------------------------------------------------------------------------
		private Context context = null;
		// -------------------------------------------------------------------------
		String	URI;
		// -------------------------------------------------------------------------
		
		// =========================================================================
		public RequestHandler(Context context) 
		{
			this.context = context;
		}
		// =========================================================================
		@Override
		public void handle (HttpRequest request, HttpResponse response,HttpContext httpContext) 
				throws HttpException, IOException 
		{
			URI = request.getRequestLine().getUri();
			// ---------------------------------------------------------------------
			// 31/12/2014 ECU log the page being requested
			// ---------------------------------------------------------------------
			// 31/12/2014 ECU log the address of the connecting device
			// --------------------------------------------------------------------
			Utilities.LogToProjectFile (TAG, "URI '" + URI + "' requested from '" 
						+ serverConnection.getRemoteAddress().getHostAddress() + "'");
			// ---------------------------------------------------------------------
			HttpEntity entity = new EntityTemplate (new ContentProducer() 
			{
				public void writeTo(final OutputStream outstream) throws IOException 
				{
					OutputStreamWriter writer = new OutputStreamWriter (outstream, "UTF-8");
					// -------------------------------------------------------------
					// 28/12/2014 ECU try and retrieve the stored page
					// -------------------------------------------------------------
					writer.write (WebUtilities.getPage (context,URI));
					// -------------------------------------------------------------
					writer.flush();
				}
			});
			// ---------------------------------------------------------------------
			response.setHeader ("Content-Type", "text/html");
			response.setEntity (entity);
			// ---------------------------------------------------------------------
		}
		// =========================================================================
		public Context getContext () 
		{
			return context;
		}
		// =========================================================================
	}
}

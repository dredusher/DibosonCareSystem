package com.usher.diboson;

import java.io.BufferedOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;

public class TestActivity extends DibosonActivity implements OnInitListener,OnUtteranceCompletedListener
{
	/* ==================================================================== */
	// 16/09/2013 ECU implemented pinch open/close
	// 02/11/2015 ECU put in the check as to whether the activity has been created
	//                anew or is being recreated after having been destroyed by
	//                the Android OS
	/* ==================================================================== */
	public static String TAG = "TestActivity";
	/* ==================================================================== */
	static Activity	activity;
	static String [] actionCommands;
	static String [] actionParameters;
	Button			button;
	static ComponentName
					componentName;
	static Context	context;
	int 			counter = 0;
	DevicePolicyManager deviceManger; 
	int 			uiCounter = 0;
	int 			threadCounter = 0;
	RefreshHandler 	refreshHandler;
	TextView 		activityView;
	TextView 		handlerView;
	ImageView		imageView;	
	TextView 		marqueeView;
	TextView 		marquee2View;
	TextView 		threadView;
	TextView 		runOnUIView;
	TextView 		datagramView;
	ScaleGestureDetector scaleGestureDetector;	// 16/09/2013 ECU added for creating pinch events
	ScreenHandler   screenHandler = new ScreenHandler ();
	// ====================================================================
	

	/* ==================================================================== */
    static String  [] options;
    static boolean [] initialOptions;
    // ====================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// -------------------------------------------------------------------------
		if (savedInstanceState == null)
		{
			// ---------------------------------------------------------------------
			// 02/11/2015 ECU the activity has been created anew
			// ---------------------------------------------------------------------
			// 28/11/2016 ECU call method to set the screen up
			// ---------------------------------------------------------------------
			Utilities.SetUpActivity (this,true,true,false);
			// ---------------------------------------------------------------------
			setContentView (R.layout.activity_test);
			
			activity	= this;
			context 	= this;

			// gets the activity's default ActionBar  

			activityView  	= (TextView)findViewById (R.id.test_activity_view);
			button			= (Button)findViewById (R.id.test_button);
			datagramView  	= (TextView)findViewById (R.id.test_datagram_view);
			handlerView   	= (TextView)findViewById (R.id.test_handler_view);
			imageView		= (ImageView)findViewById (R.id.test_image_view);
			marqueeView	  	= (TextView)findViewById (R.id.test_marquee);
			marquee2View	= (TextView)findViewById (R.id.test_marquee2);
			runOnUIView   	= (TextView)findViewById (R.id.test_runonui_view);
			threadView    	= (TextView)findViewById (R.id.test_thread_view);
			// ---------------------------------------------------------------------
			// 02/04/2015 ECU the selection is needed to get marquee scrolling to work
			// ---------------------------------------------------------------------
			marqueeView.setSelected(true);
			marquee2View.setSelected(true);
			waitABit (1000);	
			
			button.setOnClickListener (buttonListener);	
		
			// ---------------------------------------------------------------------
	
			//Utilities.SynchroniseFilesInDirectory (this, PublicData.projectFolder + PublicData.appointmentsSubFolder);
			// ---------------------------------------------------------------------
			//Thread thread = new Thread()
			//{
			//	@Override
			//	public void run()
			//	{
			//		int counter = 0;
			//		try 
			//		{
			//			while (counter++ < 20)
			//			{
			//				UPnPUtilities.WeMoSwitch ("http://192.168.1.110:49153","/upnp/control/basicevent1",0);
			//				Thread.sleep(10 * 1000);
			//				UPnPUtilities.WeMoSwitch ("http://192.168.1.110:49153","/upnp/control/basicevent1",1);
			//				Thread.sleep(10 * 1000);
			//			}
			//		}
			//		catch(InterruptedException ex){                    
			//		}       
			//	}
			//};
			//
			//thread.start();      
			// ---------------------------------------------------------------------
			
			//NotificationMessage.Add ("test notification");
					
			//SchedulesDirect.getToken();
			//SchedulesDirect.getLineUp();
			
			//PopulateFile (PublicData.projectLogFile,100000);
			
			//Utilities.sendSocketMessageSendTheObject (this,
			//		  "192.168.1.104", 
			//		  PublicData.socketNumberForData,
			//		  StaticData.SOCKET_MESSAGE_OBJECT, 
			//		  new RemoteControllerRequest (12,
			//				                       "command string"));
			// ----------------------------------------------------------------------
			//FileSendDetails.transmitFileSendDetails = new FileSendDetails("192.168.1.104",PublicData.socketNumberForData,new File(PublicData.projectFolder + "orient"),1000,true);

			// ----------------------------------------------------------------------
			//Utilities.promptMessage("the legend", "the prompt body", "the button legend");
			//finish ();
			// ----------------------------------------------------------------------
			//DialogueUtilities.prompt (this,
			//		"Prompt Title",
			//		"1\n2\n3\n4n5\n6\n7",
			//		"Acknowledge Button");
			// ---------------------------------------------------------------------
			//Calendar calendar = Calendar.getInstance();		
			//long currentTime = calendar.getTimeInMillis();		
			//DailyScheduler.SetAnAlarm (this,StaticData.ALARM_ID_METHOD,StaticData.ALARM_ID_METHOD,currentTime + (1000 * 60 * 2),
			//		null,new MethodDefinition<TestActivity> (TestActivity.class,"IncomingAlarm"));
			// ---------------------------------------------------------------------
			//ActionCommandUtilities.SelectCommand (this,
			//		Utilities.createAMethod (TestActivity.class,"ActionCommand",""));
			// ---------------------------------------------------------------------
			// 27/11/2015 ECU device administration
			// ---------------------------------------------------------------------
			//componentName = new ComponentName (this, DeviceAdministrator.class);   
			//Intent intent = new Intent (DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);   
	        //intent.putExtra (DevicePolicyManager.EXTRA_DEVICE_ADMIN,componentName);     
	        //startActivityForResult (intent,StaticData.REQUEST_CODE_DEVICE_ADMIN);   
			// ---------------------------------------------------------------------
			//screenHandler.sendEmptyMessageDelayed (10,10000);
			
			// ----------------------------------------------------------------------

			//scrollerTest (this);
			//
			//activityView.setText("activity view updated");
			//
			//refreshHandler = new RefreshHandler ();
			//refreshHandler.sleep (10000);
			//
			//theThread.start();
			//
			// 16/09/2013 ECU declare the detector for gestures that will be used to handle
			//                pinch open and close actions
			//  
			//scaleGestureDetector = 
			//         new ScaleGestureDetector(this, 
			//                new onScaleGestureListener());
			//
			// 19/09/2013 ECU created just to test the passing of a method
			//
			//try
			//{
			//  	waitABitMethod (10000,"TESTlog","hello there ");
			//	waitABitMethod (5000,"TESTlog","faster ");
			//	waitABitMethod (1000,"TESTlog","even faster ");
			//
			//Method [] theMethods = {
			//		Utilities.createAMethod("logMessage",""),
			//		Utilities.createAMethod("logMessage",""),
			//		Utilities.createAMethod("logMessage",""),
			//		Utilities.createAMethod("logMessage",""),
			//		Utilities.createAMethod("logMessage","")
			//		};
			//
			//	theMethods [2].invoke(null, new Object [] {"this is a test - method 2"});
			//	theMethods [4].invoke(null, new Object [] {"this is a test - method 4"});
			//	theMethods [0].invoke(null, new Object [] {"this is a test - method 0"});
			//
			//}
			//catch (Exception theException)
			//{
			//	Utilities.popToast ("Exception "+ theException);
			//}
			//
			//threadsTest ();	
			// ---------------------------------------------------------------------
			// 05/11/2013 ECU try some animation on the image view
			// ---------------------------------------------------------------------
			//Utilities.AnimateAnImageView(imageView, 1000, 1000, 1000,10);
			// ---------------------------------------------------------------------
		
			// =====================================================================
			// =====================================================================
			// ---------------------------------------------------------------------
			// 30/01/2015 ECU testing the new ServerMessage code
			// ---------------------------------------------------------------------
			//String source = "this is a test message using the new object";
			//byte[] byteArray = source.getBytes();
			//APIIssues.NetworkOnMainUIThread (android.os.Build.VERSION.SDK_INT);
			//sendSocketMessage ("192.168.1.4",MainActivity.socketNumberForData,new ServerMessage (99,(Object)byteArray));
			// =====================================================================
			// =====================================================================
			// 30/01/2015 ECU the following is what goes into ServerThreadForData to
			//                handle the new ServerMessage class (as a test only)
			// ---------------------------------------------------------------------
			//ObjectInputStream inputObject = new ObjectInputStream (input);
			//try 
			//{
			//	ServerMessage serverMessage = (ServerMessage) inputObject.readObject();
			//	byte [] receivedArray = (byte []) serverMessage.data;
			//} 
			//catch (ClassNotFoundException e) 
			//{	
			//	e.printStackTrace();
			//}
			// =====================================================================
			// =====================================================================
	
			//DialogueUtilities.textInput(this,"Send a regular HELLO message",
			//		"Just press the button to start sending HELLO messages every 2 minutes",
			//		Utilities.createAMethod (TestActivity.class,"Confirm",""),
			//		Utilities.createAMethod (TestActivity.class,"Cancel",""));
	      
			//options = Utilities.deviceListAsArray(true);
			//initialOptions = new boolean [options.length];

			//DialogueUtilities.multipleChoice(this,"Title Test",options, initialOptions,
			//		Utilities.createAMethod (TestActivity.class,"ConfirmMultiple",initialOptions),
			//		Utilities.createAMethod (TestActivity.class,"CancelMultiple",initialOptions));
		
			//DialogueUtilities.searchChoice (this,
			//		  						"Set up Search String",
			//		  						"Positive",
			//								Utilities.createAMethod (TestActivity.class,"PositiveSearch",(Object)(new SearchParameters())),
			//								"Negative",
			//								Utilities.createAMethod (TestActivity.class,"NegativeSearch",(Object)(new SearchParameters())));
		}
		else
		{
			// ---------------------------------------------------------------------
			// 02/11/2015 ECU the activity has been recreated after having been
			//                destroyed by the Android OS
			// ---------------------------------------------------------------------
			finish (); 
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	 protected void onActivityResult (int requestCode, int resultCode, Intent data) 
	 {   
		 switch (requestCode) 
		 {   
		 	// ---------------------------------------------------------------------
		 	case StaticData.REQUEST_CODE_DEVICE_ADMIN:   
		 		if (resultCode == Activity.RESULT_OK) 
		 		{   
		 			Utilities.popToast ("Admin enabled!");  
		 			
		 			screenHandler.sendEmptyMessageDelayed (0,10000);
		 		} 
		 		else 
		 		{   
		 			Utilities.popToast("Admin enable FAILED!");   
		 		}   
		 		return;   
		 } 
		 // ------------------------------------------------------------------------
		 super.onActivityResult(requestCode, resultCode, data);   
	}  
	// =============================================================================
	@Override
	public void onBackPressed ()
	{
		// -------------------------------------------------------------------------
		super.onBackPressed();
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	@Override
    public void onDestroy()
    {
		// -------------------------------------------------------------------------
		// 23/03/2015 ECU interrupt the thread if alive
		// -------------------------------------------------------------------------
		if (sendHelloMessageThread.isAlive())
			sendHelloMessageThread.interrupt();
		// -------------------------------------------------------------------------
		super.onDestroy();
    }
	/* ============================================================================= */
	@SuppressLint("HandlerLeak")
	class RefreshHandler extends Handler
	{
		@Override
	    public void handleMessage(Message msg) 
	    {   
			handlerView.setText("handler view updated " + counter++ + "(" + threadCounter + ")" + "(" + uiCounter + ")");
			
			// 02/08/2013 ECU display the datagram details
			
			datagramView.setText(PublicData.datagram.Print());
			
			// 22/06/2013 ECU if we are to keep running then wait a bit
			
			sleep (1000);
	    }
	    /* ------------------------------------------------------------------------ */
	    public void sleep(long delayMillis)
	    {		
	        this.removeMessages(0);
	        sendMessageDelayed(obtainMessage(0), delayMillis);
	    }
	};
	// =============================================================================
	@SuppressLint("HandlerLeak")
	class ScreenHandler extends Handler
	{
		@Override
	    public void handleMessage (Message theMessage) 
	    {   
			switch (theMessage.what)
        	{
        		case 0:
        			((DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE)).lockNow(); 
        			sendMessageDelayed (obtainMessage (1),10000);
        			break;
        		case 1:
        			activity.getWindow().addFlags (WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	            	activity.getWindow().addFlags (WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
	    	        activity.getWindow().addFlags (WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	    	        activity.getWindow().addFlags (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    	        sendMessageDelayed (obtainMessage (2),10000);
	    	        break;
        		case 2:
        			((DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE)).removeActiveAdmin(componentName); 
        			finish ();
        			break;
        		case 10:
        			Utilities.makePhoneCall (context, "02085555256");
        			sendMessageDelayed (obtainMessage (11),10000);
        			break;
        		case 11:
        			Utilities.cancelPhoneCall (context);
        			break;		
        	}			
	    }
	    /* ------------------------------------------------------------------------ */
	    public void sleep(long delayMillis)
	    {		
	        this.removeMessages(0);
	        sendMessageDelayed(obtainMessage(0), delayMillis);
	    }
	};
	// =============================================================================
	Thread theThread = new Thread() 
	{
	    public void run() 
	    {
	        runOnUiThread(new Runnable()
	        {
	            @Override
	            public void run() 
	            {
	            	runOnUIView.setText("in runOnUIThread " + uiCounter++);		
	            }
	        });
	    }
	};
	/* ==================================================================== */
	private void waitABit (final int theWaitTime)
	{
		Thread thread = new Thread()
		 {
		        @Override
		        public void run()
		        {
		            try 
		            {
		                synchronized(this)
		                {
		                	threadView.setText("thread view updated " + threadCounter++);
		                	
		                	while (true)
		                	{
		                		Thread.sleep(theWaitTime);
		                		threadCounter++;
		                	}
		                }
		            }
		            catch(InterruptedException ex){                    
		            }       
		        }
		    };

		    thread.start();        
	}
	/* ======================================================================== */
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
	     //scaleGestureDetector.onTouchEvent(event);
	     return true;
	}
	/* ======================================================================== */
	public class onScaleGestureListener extends SimpleOnScaleGestureListener 
    {
		@Override
		public boolean onScale(ScaleGestureDetector detector) 
		{

			float scaleFactor = detector.getScaleFactor();
 
			if (scaleFactor > 1) 
			{
				Utilities.popToast ("Zooming out");
			} 
			else 
			{
				Utilities.popToast ("Zooming In");
			}
			return true;
		}
		/* ==================================================================== */
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) 
		{
			Utilities.popToast ("onScaleBegin");
			return true;
		}
		/* ========================================================================= */
		@Override
		public void onScaleEnd(ScaleGestureDetector detector) 
		{
			Utilities.popToast ("onScaleEnd");
		}
    }
	/* ============================================================================= */
	public static void TESTlog ()
	{
		// -------------------------------------------------------------------------
		// 10/03/2015 ECU changed to use 'debugMode in 'storedData'
		// -------------------------------------------------------------------------
		PublicData.storedData.debugMode = true;
		Utilities.debugMessage (TAG,"no string");
		PublicData.storedData.debugMode = false;
	}
	public static void TESTlog (String theString)
	{
		PublicData.storedData.debugMode = true;
		Utilities.debugMessage (TAG,theString);
		PublicData.storedData.debugMode = false;
	}
	/* ============================================================================= */
	void scrollerTest (Context theContext)
	{
		final Scroller scroller = new Scroller (theContext);
		
		scroller.startScroll(0,0, 1000,1000, 10000);
				
		Thread thread = new Thread()
		 {
		        @Override
		        public void run()
		        {
		            try 
		            {
		               	while (!scroller.isFinished())
		               	{	
		               		scroller.computeScrollOffset();
		               		
		               		Thread.sleep(200);   
		                }
		            }
		            catch(InterruptedException ex){                    
		            }       
		        }
		 };

		 thread.start();        
		
	}
	/* ======================================================================== */
	void threadsTest ()
	{
		 int poolSize =200;
		 int maxPoolSize =200;
		 long keepAliveTime = 10;
		 
		 ThreadPoolExecutor threadPoolExecutor = null;
		 
		 final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(5);
		 
		 threadPoolExecutor = new ThreadPoolExecutor(poolSize, maxPoolSize,keepAliveTime,TimeUnit.SECONDS, queue);
		 
		 for (int theIndex= 1; theIndex<200;theIndex++)
			 threadPoolExecutor.execute(threadsTestThread(1000,"192.168.1." + theIndex));
			 
	}
	/* ======================================================================== */
	private Runnable threadsTestThread (final int theWaitTime,final String IPAddress)
	{
		Thread thread = new Thread()
		 {
		        @Override
		        public void run()
		        {
		        	boolean result;
		        	
		            try 
		            {
		                synchronized(this)
		                {    
		                	// 07/08/2013 ECU if thread is running for this device then result = true
		                	// -----------------------------------------------------
							// 07/08/2013 ECU not this device so need to check via ICMP
		                	// 01/09/2015 ECU changed to use StaticData
		                	// -----------------------------------------------------
							InetAddress serverAddress = InetAddress.getByName(IPAddress); 
							result	 = serverAddress.isReachable(StaticData.DISCOVERY_TIMEOUT);
							// ----------------------------------------------------
							// 05/08/2013 ECU optionally display entry in log
							// 23/08/2015 ECU changed from logging to logcat
							//	---------------------------------------------------- 
							Utilities.LogToProjectFile ("Network","Discovery " + IPAddress + " Result = " + result);
							// -----------------------------------------------------
		                }
		            }
		            catch(Exception theException)
		            {                    
		            }       
		        }
		 };
		 
		return thread;      
	}
	/* ======================================================================== */
	//private void waitABitMethod (final int theWaitTime, String theMethod,final String theString)
	//{
	//	Thread thread = new Thread()
	//	 {
	//	        @Override
	//	        public void run()
	//	        {
	//	        	int counter = 0;
	//	            try 
	//	            {
	//	                synchronized(this)
	//	                {    
	//	                	while (true)
	//	                	{
	//	                		Thread.sleep(theWaitTime);
	//	                		
	//	                		try
	//	                		{
	//	                			Utilities.createAndInvokeMethod(TestActivity.class,"TESTlog",theString + counter++);
	//	                		}
	//	                		catch (Exception theException)
	//	                		{
	//	                		
	//	                		}
	//	                	}
	//	                }
	//	            }
	//	            catch(InterruptedException ex){                    
	//	            }       
	//	        }
	//	 };
	//
	//	 thread.start();        
	//}
	/* ============================================================================= */
	private View.OnClickListener buttonListener = new View.OnClickListener() 
	{
		@Override
		public void onClick(View view) 
		{	
			Utilities.popToast (view,"This is just a popupWindow test");
		}
	};
	// =============================================================================
	static Thread sendHelloMessageThread = new Thread()
	{	
		@Override
		public void run()
		{
			try 
			{          	
				while (!this.isInterrupted())
		        {
					// -------------------------------------------------------------
					// 23/03/2015 ECU wait two minutes before initiating the send
					// 01/09/2015 ECU changed to use StaticData
					// -------------------------------------------------------------
					sleep (StaticData.MILLISECONDS_PER_MINUTE * 2);
					// -------------------------------------------------------------
					// 23/03/2015 ECU initiate the multicast 'hello' message
					// -------------------------------------------------------------
					PublicData.broadcastMessage = StaticData.BROADCAST_MESSAGE_HELLO; 
					// -------------------------------------------------------------
					// 23/03/2013 ECU confirm the fact to the user
					// -------------------------------------------------------------
					Utilities.SpeakAPhrase (MainActivity.activity,"Hello Message initiated");
					// -------------------------------------------------------------
				}
			}
			catch(InterruptedException theException)
			{    
				// -----------------------------------------------------------------
				// 04/04/2014 ECU Restore interrupt flag after catching 
				//				  InterruptedException to make loop condition false
				// -----------------------------------------------------------------
				Thread.currentThread().interrupt();
				// -----------------------------------------------------------------
			}       
		}
	};
	// =============================================================================
	public static void sendSocketMessage (String theIPAddress, int thePort, Object theObject)
	{
		// -------------------------------------------------------------------------
		// 30/01/2015 ECU try and send the object on the appropriate stream 
		//			  ECU first check that a valid object has been supplied
		// -------------------------------------------------------------------------
		if (theObject != null)
		{
			// ---------------------------------------------------------------------
			try 
			{   
				// -----------------------------------------------------------------
				// 30/01/2015 ECU convert the input destination IP address from a
				//                string into the required format
				// -----------------------------------------------------------------
				InetAddress serverAddress = InetAddress.getByName(theIPAddress);    
				// -----------------------------------------------------------------
				// 30/01/2015 ECU try and get the socket for the output stream
				// -----------------------------------------------------------------
				Socket theSocket = new Socket (serverAddress,thePort); 
				// -----------------------------------------------------------------
				// 30/01/2015 ECU get the relevant output stream
				// -----------------------------------------------------------------
				BufferedOutputStream outputStream = new BufferedOutputStream (theSocket.getOutputStream());
				// -----------------------------------------------------------------
				// 30/01/2015 ECU as an Object is being output then get the correct
				//			      stream
				// ------------------------------------------------------------------
				ObjectOutputStream outputObject = new ObjectOutputStream (outputStream);
				// ------------------------------------------------------------------
				// 30/01/2015 ECU now write out the supplied object and flush it
				//                through
				// ------------------------------------------------------------------
				outputObject.writeObject (theObject);
				outputObject.flush ();	
				// ------------------------------------------------------------------
				// 30/01 2015 ECU everything has been done so close the streams and
				//                the socket
				// ------------------------------------------------------------------
				outputObject.close();
				outputStream.close();
				theSocket.close ();
			} 
			catch (Exception theException) 
			{             
			} 		
		}
	}
	// =============================================================================

	/* ============================================================================= */
	public static void Cancel (String theText)
	{
		Utilities.popToast("Cancel " + theText);
	}
	// =============================================================================
	public static void Confirm (String theText)
	{
		
		// -------------------------------------------------------------------------
		// 20/03/2015 ECU at this point want to announce 'to the world' that this
		//                device is 'up and running'
		// -------------------------------------------------------------------------
		sendHelloMessageThread.start();
	}
	/* ============================================================================= */
	public static void CancelMultiple (boolean [] theOptions)
	{
		Utilities.popToast("CancelMultiple ");
	}
	// =============================================================================
	public static void ConfirmMultiple (boolean [] theOptions)
	{
		String localString = "";
		
		for (int theIndex = 0; theIndex < options.length; theIndex++)
		{
			localString += "Entry " + options [theIndex] + " " + (theOptions[theIndex] ? "selected" : "not selected") + "\n";
		}
		Utilities.popToast(localString);
	}
	// =============================================================================
	public static void ConfirmSlider (int theSliderValue)
	{
		Utilities.popToast("Return value is " + theSliderValue);
	}
	// ==============================================================================
	public static void NegativeSearch (Object theSearchParameters)
	{
		Utilities.popToast ("NegativeSearch" + ((SearchParameters) theSearchParameters).Print());
	}
	// ===============================================================================
	public static void PositiveSearch (Object theSearchParameters)
	{
		Utilities.popToast ("PositiveSearch" + ((SearchParameters) theSearchParameters).Print());
	}
	// ===============================================================================
	
	// =============================================================================
	public static void SelectedParameter (int theCommandIndex)
	{
		Utilities.popToast("Selected " + actionParameters [theCommandIndex]);
		
	}
	// =============================================================================
	public static void IncomingAlarm ()
	{
		Utilities.popToastAndSpeak ("The alarm has been actioned ");
	}
	/* ============================================================================= */
	public static void ActionCommand (String theActionString)
	{
		Utilities.popToast("Received '" + theActionString + "'");
	}

	/* ============================================================================= */
	 public static void IPAddressMethod (String theIPAddress)
	 {
	   	Utilities.popToast("IP Address : " + theIPAddress);
	 }
	 // ============================================================================
	
	
	// =============================================================================
	void PopulateFile (String theFileName,int theNumberOfLines)
	{	
		FileWriter fileWriter;
			
		try 
		{
			// ---------------------------------------------------------------------
			// 10/01/2014 ECU open to the file in append mode
			// ---------------------------------------------------------------------
			fileWriter = new FileWriter (theFileName);
			// ---------------------------------------------------------------------
			for (int theLine = 1; theLine <= theNumberOfLines; theLine++)
			{
				fileWriter.write ("line number " + theLine + "\n");
			}
			// ---------------------------------------------------------------------
			// 10/01/2014 ECU flush out the data and close
			// ---------------------------------------------------------------------
			fileWriter.flush();
			fileWriter.close();
		}
		catch (IOException theException)
		{
				
		}
	}
	@Override
	public void onUtteranceCompleted(String utteranceId) 
	{

		
	}
	@Override
	public void onInit(int arg0) 
	{

		
	}
	
	

}

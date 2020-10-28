package com.usher.diboson;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;


public class ArduinoCommunicatorService extends Service 
{
	// ============================================================================
	// 16/12/2013 ECU created
	//            ECU service for communicating with the Arduino
	// ----------------------------------------------------------------------------
	// Testing
	// =======
	//=============================================================================
	//private final static String TAG = "ArduinoCommunicatorService";
	/* ============================================================================ */
    private boolean mIsRunning = false;
    private SenderThread mSenderThread;

    private volatile UsbDevice mUsbDevice = null;
    private volatile UsbDeviceConnection mUsbConnection = null;
    private volatile UsbEndpoint mInUsbEndpoint = null;
    private volatile UsbEndpoint mOutUsbEndpoint = null;


    final static String DATA_RECEIVED_INTENT = "DATA_RECEIVED";
    final static String SEND_DATA_INTENT = "END_DATA";
    final static String DATA_SENT_INTERNAL_INTENT = "DATA_SENT";
    final static String DATA_EXTRA = "DATA_EXTRA";
    /* ============================================================================ */
    @Override
    public IBinder onBind(Intent arg0) 
    {
    	//--------------------------------------------------------------------------
    	// 16/12/2013 ECU return the communication channel to this service
    	//--------------------------------------------------------------------------
        return null;
    }
    /* ============================================================================ */
    @Override
    public void onCreate() 
    {
    	super.onCreate();
    	//--------------------------------------------------------------------------
    	// 16/12/2013 ECU register a receiver with an appropriate filter
    	//--------------------------------------------------------------------------
        IntentFilter filter = new IntentFilter();
        filter.addAction(SEND_DATA_INTENT);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mReceiver, filter);
    }
    /* ============================================================================ */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) 
    {
    	//--------------------------------------------------------------------------
    	// 16/12/2013 ECU Called by the system every time a client explicitly starts 
    	//				  the service by calling startService(Intent), providing the 
    	//                arguments it supplied and a unique integer token representing 
    	//                the start request.
    	//--------------------------------------------------------------------------
    	if (mIsRunning) 
    	{
    		//----------------------------------------------------------------------
    		// 16/12/2013 ECU indicate that the service is already running
    		//----------------------------------------------------------------------
            return Service.START_REDELIVER_INTENT;
        }
    	//--------------------------------------------------------------------------
    	// 16/12/2013 ECU indicate that the service is up and running
    	//--------------------------------------------------------------------------
        mIsRunning = true;
    	//--------------------------------------------------------------------------
        // 16/12/2013 ECU check if permission to the device has been granted
    	//--------------------------------------------------------------------------
        if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) 
        {
        	//----------------------------------------------------------------------
        	// 16/12/2013 ECU permission has been denied
        	//----------------------------------------------------------------------
            Utilities.popToast	("permission Denied");
            stopSelf();
            return Service.START_REDELIVER_INTENT;
        }

        mUsbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (!initDevice())
        {
            stopSelf();
            return Service.START_REDELIVER_INTENT;
        }
    	//--------------------------------------------------------------------------
        // 09/04/2014 ECU changed to use resource
    	//--------------------------------------------------------------------------
        Utilities.popToast (getString(R.string.receiving));
    	//--------------------------------------------------------------------------
        // 16/12/2013 ECU start both receive and transmit threads
    	//--------------------------------------------------------------------------
        startReceiverThread();
        startSenderThread();

        return Service.START_REDELIVER_INTENT;
    }
    /* ============================================================================ */
    @Override
    public void onDestroy() 
    {
        super.onDestroy();
    	//--------------------------------------------------------------------------
        // 16/12/2013 ECU unregister the receiver and close the USB connection
    	//--------------------------------------------------------------------------
        unregisterReceiver(mReceiver);
        
        mUsbDevice = null;
        
        if (mUsbConnection != null) {
            mUsbConnection.close();
        }
    }
    /* ============================================================================ */
    private byte[] getLineEncoding(int baudRate) 
    {
        final byte[] lineEncodingRequest = { (byte) 0x80, 0x25, 0x00, 0x00, 0x00, 0x00, 0x08 };
        
        switch (baudRate) 
        {
        	case 14400:
        		lineEncodingRequest[0] = 0x40;
        		lineEncodingRequest[1] = 0x38;
        		break;

        	case 19200:
        		lineEncodingRequest[0] = 0x00;
        		lineEncodingRequest[1] = 0x4B;
        		break;
        }
        
        return lineEncodingRequest;
    }
    /* ============================================================================ */
    private boolean initDevice() 
    {
    	//--------------------------------------------------------------------------
    	// 16/12/2013 ECU initialise the device
    	//--------------------------------------------------------------------------
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    	//--------------------------------------------------------------------------
        // 16/12/2013 ECU try and open to the USB device
    	//--------------------------------------------------------------------------
        mUsbConnection = usbManager.openDevice(mUsbDevice);
        
        if (mUsbConnection == null) 
        {   
            Utilities.popToast (getString(R.string.opening_device_failed));
            return false;
        }
    	//--------------------------------------------------------------------------
        // 16/12/2013 ECU get an interface to the USB device (1 is just an index)
    	//--------------------------------------------------------------------------
        UsbInterface usbInterface = mUsbDevice.getInterface(1);
        
        if (!mUsbConnection.claimInterface(usbInterface, true)) 
        {
            Utilities.popToast (getString(R.string.claiming_interface_failed));
            mUsbConnection.close();
            return false;
        }
    	//--------------------------------------------------------------------------
        // 16/12/2013 ECU set up the Arduino USB serial converter
    	//--------------------------------------------------------------------------
        // 16/12/2013 ECU set the control line states
    	//--------------------------------------------------------------------------
        mUsbConnection.controlTransfer(0x21, 0x22, 0, 0, null, 0, 0);
    	//--------------------------------------------------------------------------
        // 16/12/2013 ECU set line encoding at 9600 baud
    	//--------------------------------------------------------------------------
        mUsbConnection.controlTransfer(0x21, 0x20, 0, 0, getLineEncoding(9600), 7, 0);
    	//--------------------------------------------------------------------------
        // 16/12/2013 ECU get the end points
    	//--------------------------------------------------------------------------
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) 
        {
            if (usbInterface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) 
            {
                if (usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) 
                {
                    mInUsbEndpoint = usbInterface.getEndpoint(i);
                } 
                else 
                if (usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_OUT) 
                {
                    mOutUsbEndpoint = usbInterface.getEndpoint(i);
                }
            }
        }
    	//--------------------------------------------------------------------------
        // 16/12/2013 ECU if no endpoints then report the fact and close the connection
    	//--------------------------------------------------------------------------
        if (mInUsbEndpoint == null)
        {
            Utilities.popToast (getString(R.string.no_in_endpoint_found));
            mUsbConnection.close();
            return false;
        }

        if (mOutUsbEndpoint == null) 
        {
            Utilities.popToast (getString(R.string.no_out_endpoint_found));
            mUsbConnection.close();
            return false;
        }

        return true;
    }
    /* ============================================================================ */
    BroadcastReceiver mReceiver = new BroadcastReceiver() 
    {
        @Override
        public void onReceive(Context context, Intent intent) 
        {
        	//----------------------------------------------------------------------
        	// 16/12/2013 ECU get the action included in the intent
        	//----------------------------------------------------------------------
            final String action = intent.getAction();
        	//----------------------------------------------------------------------
            // 16/12/2013 ECU check for sending data
        	//----------------------------------------------------------------------
            if (SEND_DATA_INTENT.equals(action)) 
            {
                final byte[] dataToSend = intent.getByteArrayExtra(DATA_EXTRA);
                
                if (dataToSend == null) 
                {     
                    String text = String.format ("No %1$s extra in intent!", DATA_EXTRA);
                    Utilities.popToast (text);
                    return;
                }
                
                mSenderThread.mHandler.obtainMessage(10, dataToSend).sendToTarget();
            }
            else 
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) 
            {
            	Utilities.popToast (getString(R.string.device_detached));
                mSenderThread.mHandler.sendEmptyMessage(11);
                stopSelf();
            }
        }
    };
    /* ============================================================================ */
    private void startReceiverThread() 
    {
        new Thread("arduino_receiver") 
        {
            public void run() 
            {
                byte[] inBuffer = new byte[4096];
                while(mUsbDevice != null ) 
                {
                    final int len = mUsbConnection.bulkTransfer(mInUsbEndpoint, inBuffer, inBuffer.length, 0);
                    
                    if (len > 0) 
                    {
                        Intent intent = new Intent(DATA_RECEIVED_INTENT);
                        byte[] buffer = new byte[len];
                        System.arraycopy(inBuffer, 0, buffer, 0, len);
                        intent.putExtra(DATA_EXTRA, buffer);
                        sendBroadcast(intent);
                    } 
                    else 
                    {
                    	//----------------------------------------------------------
                        // 16/12/2013 ECU no data read
                    	//----------------------------------------------------------
                    }
                }

            }
        }.start();
    }
    /* ============================================================================ */
    private void startSenderThread() 
    {
        mSenderThread = new SenderThread("arduino_sender");
        mSenderThread.start();
    }
    /* ============================================================================ */
    private class SenderThread extends Thread 
    {
        public Handler mHandler;

        public SenderThread(String string) 
        {
            super(string);
        }

        public void run()
        {
            Looper.prepare();

            mHandler = new Handler() 
            {
                public void handleMessage(Message message) 
                {
                	if (message.what == 10) 
                	{
                        final byte[] dataToSend = (byte[]) message.obj;

                        //final int len = mUsbConnection.bulkTransfer(mOutUsbEndpoint, dataToSend, dataToSend.length, 0);
                        Intent sendIntent = new Intent(DATA_SENT_INTERNAL_INTENT);
                        sendIntent.putExtra(DATA_EXTRA, dataToSend);
                        sendBroadcast(sendIntent);
                	} 
                	else 
                	if (message.what == 11)
                	{
                        Looper.myLooper().quit();
                    }
                }
            };
            Looper.loop();
        }
    }
    /* ============================================================================ */
}
